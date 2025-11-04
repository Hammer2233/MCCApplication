package main;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Scanner;

public class channelExport
{
    //Arrays to track code template library names, library XML
    //NOTE: This only needs to be ran once as all channels utilize all code template libraries    
    
	//ArrayList changed in 2.2.3 Update
    private static ArrayList channelIDs = new ArrayList<>();
    private static ArrayList channelNames = new ArrayList<>();
    private static ArrayList rawChannelCLOB = new ArrayList();
    private static ArrayList channelMetadata = new ArrayList();
    private static ArrayList codeTemplateLibrary_NAME = new ArrayList();
    private static ArrayList codeTemplateLibrary_XML = new ArrayList();
    private static ArrayList codeTemplate_ID = new ArrayList();
    private static ArrayList codeTemplate_XML = new ArrayList();
    private static ArrayList allCTLArray = new ArrayList();
    
    //Arraylist and boolean to determine if an SFTP restart channel should be implemented
    private static String sftpChannelNames = "";
    private static ArrayList sftpConnectedChannels = new ArrayList();
    private static ArrayList sftpRestartSplit = new ArrayList();
    private static boolean isSFTPChannelNeeded = false;
    private static boolean sftpGenChoice = true;
    private static boolean generateSFTPTag = false;

    //boolean to determine if code templates are included
    private static boolean includeCTLs;
    private static boolean isFullMirthExport = false;

    private static String backupFolderPath = Main.getBackupFolder();
    
    //added in 2.2.4 to properly generate channels if Mirth version is less than 3.5
    private static boolean skipCMD = false;
    private static boolean forceNewChannelGeneration = false;
    
    //added in 2.2.5 to store the active channel names
    private static ArrayList activeChannelNames = new ArrayList();
    private static boolean isMyMirthVersionOver35 = true;

    public static boolean includeCodeTemplates(String yesNo)
    {
    	if(yesNo == "YES")
    	{
    		includeCTLs = true;
    		return includeCTLs;
    	}
    	else
    	{
    		includeCTLs = false;
    		return includeCTLs;
    	}
    }

    public static boolean isFullMirthExportCheck(String yesNo)
    {
    	if(yesNo == "YES")
    	{
    		isFullMirthExport = true;
    		return isFullMirthExport;
    	}
    	else
    	{
    		isFullMirthExport = false;
    		return false;
    	}
    }

    public static String exportChannels(String host)
    {
    	//clears the channels folder before next write
    	backupFolderPath = Main.getBackupFolder();
    	clearChannelFolder();
    	
    	//clears arraylists
    	channelIDs.clear();
    	channelNames.clear();
    	rawChannelCLOB.clear();
    	channelMetadata.clear();
    	codeTemplateLibrary_NAME.clear();
    	codeTemplateLibrary_XML.clear();
    	codeTemplate_ID.clear();
    	codeTemplate_XML.clear();
    	allCTLArray.clear();
    	sftpConnectedChannels.clear();
    	sftpRestartSplit.clear();
    	sftpChannelNames = "";
    	activeChannelNames.clear();
    	
    	//evaluates Mirth version to account for pre Mirth 3.5.0 channel generation
    	String mirthVersion = fullConfigExport.getMirthVersion(host).replace("\"", "");;
    	String[] splitVersion = mirthVersion.split("\\.");
    	
    	if(Integer.parseInt(splitVersion[0]) < 4 && Integer.parseInt(splitVersion[1]) < 5 || Integer.parseInt(splitVersion[0]) < 3)
    	{
    		isMyMirthVersionOver35 = false;
    		System.out.println("Mirth database version is less than 3.5");
    		if(forceNewChannelGeneration == true)
    		{
    			System.out.println("User chose to use newer channel generation type");
    			skipCMD = false;
    		}
    		else
    		{
    			skipCMD = true;
    		}    		
    	}
    	else
    	{
    		isMyMirthVersionOver35 = true;
    		System.out.println("Mirth database version is 3.5 or higher");
    		skipCMD = false;
    	}
    	
    	//added in 2.2.5 - This grabs a list of all active channels used in evaluation for the SFTP Restart Channel addition    	
    	//Check added in 2.2.6 to only run this if Mirth DB > 3.5
    	if(isMyMirthVersionOver35 == true)
    	{
    		SQLCommand.channelStatusBuilder(host);
            int numberOfChannels = SQLCommand.channelStatusListSize();
            System.out.println("Total Channel Count: " + numberOfChannels);
            for (int i = 0; i < numberOfChannels; i++) 
            {
                if (SQLCommand.returnChannelStatus(i).toString().equals("[ACTIVE]"))
                {
                    activeChannelNames.add(SQLCommand.returnChannelName(i));
                    System.out.println("ACTIVE CHANNEL: " + SQLCommand.returnChannelName(i));
                }
            }
    	}
        
        SQLCommand.clearArraysForActiveChannels();    	
    	
    	try(Connection conn = DriverManager.getConnection(host); Statement stmt = conn.createStatement())
		{
		    String query = "SELECT * FROM CHANNEL";
		    ResultSet rs = stmt.executeQuery(query);
		    ResultSetMetaData RSMD = rs.getMetaData();
		    int columns = RSMD.getColumnCount();
		
		    //creates folder path if it does not yet exist:
		    File dir = new File(backupFolderPath+"channelBackup");
		    dir.mkdirs();
		
		    try
		    {
		        while (rs.next()) 
		        {  
		            //column containing CLOB
		            String XMLdata = rs.getString(4);
		            rawChannelCLOB.add(XMLdata.trim());
		            channelNames.add(rs.getString(2));
		            channelIDs.add(rs.getString(1));		            
		        }
		    } 
		    catch (SQLException sqlExcept) 
		    {
		        System.out.println("FAILED MISERABLY");
		        System.out.println(sqlExcept);
		    }
		}
		catch (Exception e) 
		{
			if(e.toString().contains("not found"))
			{
				logCommands.exportToLog("ERROR: " + e);
				logCommands.exportToLog("Unable to locate the Mirth database. Please verify the location of the 'mirthdb' folder.");
				
				if(e.toString().contains("Database") && e.toString().contains("not found"))
	            {
	            	Main.configPathReader();
	            }
			}
		  e.printStackTrace();
		}
		return "channels exported";
    }


    public static String exportMetadata(String host) throws FileNotFoundException
    {
      //Exports and edits the channelMetadata
      try (Connection conn = DriverManager.getConnection(host); Statement stmt = conn.createStatement()) 
      {
          String query = "SELECT * FROM CONFIGURATION where NAME LIKE '%channelMetadata%'";
          ResultSet rs = stmt.executeQuery(query);
          ResultSetMetaData RSMD = rs.getMetaData();
          int columns = RSMD.getColumnCount();

          File configFilesDir = new File(backupFolderPath+"fullMirthExport\\configurationFiles");
          configFilesDir.mkdirs();

          try 
          {
        	  while (rs.next()) 
        	  { 
	              //CHANGE THIS BELOW
	              //THIS COLUMN INDEX chooses what data to convert to XML
	              String XMLdata = rs.getString(3);
	              
	              String channelMetadataXML = XMLdata;
	              
	              //added in 2.2.3
	              String[] channelMetadataXMLSplit = channelMetadataXML.split("\n");
	              String currentCMDSplitLine = "";
	              for(int yy=0;yy<channelMetadataXMLSplit.length;yy++)
	              {
	            	  String thisLine = channelMetadataXMLSplit[yy];
	            	  if(thisLine.contains("map>") || thisLine.contains("<map") || thisLine.contains("<com.mirth.connect.model.ChannelMetadata>") || thisLine.contains("</com.mirth.connect.model.ChannelMetadata>"))
	            	  {
	            		  //skips any "map" segments
	            	  }
	            	  else if(thisLine.contains("</entry>"))
	            	  {
	            		  //ends the current CMD and adds it to the array
	            		  currentCMDSplitLine += thisLine;
	            		  channelMetadata.add(currentCMDSplitLine);
	            		  currentCMDSplitLine = "";
	            	  }
	            	  else
	            	  {
	            		  currentCMDSplitLine += thisLine + "\n";
	            	  }
	              }                    
        	  } 
          } 
          catch (SQLException sqlExcept) 
          {
        	  System.out.println("FAILED MISERABLY");
          		System.out.println(sqlExcept);
          }
       } 
       catch (Exception e) 
       {
           e.printStackTrace();
           
           if(e.toString().contains("Database") && e.toString().contains("not found"))
           {
           	Main.configPathReader();
           }
       }

       //calls codeTemplates
        if(includeCTLs == true || isFullMirthExport == true)
        {
        	exportCodeTemplates(host);
        }
         
        /**Updated 2.2.3 logic
         * 1. Iterates over the channelNames array and creates the XML file
         * 2. Grabs the channel and splits it into lines
         * 3. Checks if the current line contains a Mirth channel ID. If so, it will add the proper metaData to the XML
         * 4. Adds CTLs if necessary
         * 5. Exports the channel XML
         */
        boolean sftpCheck = true;
        for(int cm=0;cm<channelNames.size();cm++)
        {          	
        	File currentChannelFile = new File(backupFolderPath+"channelBackup\\" + channelNames.get(cm)+".xml");
            try 
    		{
            	currentChannelFile.createNewFile();
    		} 
    		catch (IOException e) 
    		{
    			e.printStackTrace();
    		}
            
            //builds the channel with/without CTLs
            String[] currentChannelSplit = rawChannelCLOB.get(cm).toString().split("\n");
            String channelXMLOutput = "";
            for(int kl=0;kl<currentChannelSplit.length;kl++)
            {
            	String line = currentChannelSplit[kl].trim();
            	
            	//re added the portion to account for bad ascii characters
            	String[] badCharacters = {"…", "\u00A0", "\u0085"};
            	String[] goodReplacements = {"...", " ", " "};
            	
            	for(int charCheck=0; charCheck<badCharacters.length; charCheck++)
            	{
            		if(line.contains(badCharacters[charCheck]))
            		{
            			line = line.replace(badCharacters[charCheck], goodReplacements[charCheck]);
            		}
            	}
            	
            	if(kl == currentChannelSplit.length-1)
            	{
            		for(int jk=0;jk<channelIDs.size();jk++)
            		{
            			if(skipCMD == false && channelMetadata.get(jk).toString().contains(channelIDs.get(cm).toString()))
            			{
            				String[] editCMD = channelMetadata.get(jk).toString().split("\n");
            				for(int po=0;po<editCMD.length;po++)
            				{            					            					
            					if(editCMD[po].contains("<entry>"))
            					{
            						channelXMLOutput += "<exportData>\n<metadata>\n";
            					}
            					else if(editCMD[po].contains("</entry>"))
            					{         						
            						channelXMLOutput += "</metadata>\n";
            					}
            					else if(editCMD[po].contains("string>"))
            					{
            						//skip the string line
            					}
            					else
            					{
            						channelXMLOutput += editCMD[po].trim() + "\n";
            					}
            				}
            			}
            		}
            	}
            	else
            	{
            		channelXMLOutput += line + "\n";
            		if(line.contains("<scheme>"))
            		{
            			String schemeType = line.replace("<scheme>", "").replace("</scheme>", "").toUpperCase().trim();
            			if(schemeType.contains("FTP"))
            			{   
            				if(!sftpConnectedChannels.contains(channelNames.get(cm)) && activeChannelNames.contains(channelNames.get(cm))) //added activeChannelNames check in 2.2.5
            				{
            					isSFTPChannelNeeded = true;
            					System.out.println(channelNames.get(cm) + " has FTP/SFTP connection. Type: " + schemeType);
                				sftpConnectedChannels.add(channelNames.get(cm));                				
            				} 
            				else
            				{
            					if(!activeChannelNames.contains(channelNames.get(cm)))
            					{
            						System.out.println("Inactive channel using SFTP connection caught for '" + channelNames.get(cm) + "'");
            					}
            					else
            					{
            						System.out.println("Dupe SFTP connection caught for channel '" + channelNames.get(cm) + "'");
            					}            					
            				}
            			}
            		}
            	}
            }
            
            if(includeCTLs == true)
            {
            	//channelXMLOutput += "</codeTemplateLibraries>\n";
            	if(!channelNames.get(cm).equals("SFTP Restart Channel") || generateSFTPTag == false && channelNames.get(cm).equals("SFTP Restart Channel"))
            	{
            		channelXMLOutput += "<codeTemplateLibraries>\n";
                	
                	for(int hi=0;hi<codeTemplateLibrary_XML.size();hi++)
                	{
                		channelXMLOutput += codeTemplateLibrary_XML.get(hi);
                	}
                	System.out.println("skipCMD: " + skipCMD);
                	if(skipCMD == false)
                	{
                		channelXMLOutput += "</codeTemplateLibraries>\n</exportData>\n";
                	}
                	else
                	{
                		channelXMLOutput += "</codeTemplateLibraries>\n";
                	}            		
            	}
            	else
            	{
            		//skip CTLs for the SFTP restart channel if the channel has not been imported into the database
            	}            	
            }
            else
            {
            	//below checks if this is a new SFTP channel generation. If so, it will skip adding the exportdata tag
            	//if it is not a new generation (aka, SFTP restart was already in the DB), then it will add the tag
            	//added skip to exportdata if Mirth version is <3.5            	
            	if(skipCMD == false && !channelNames.get(cm).equals("SFTP Restart Channel") || skipCMD == false && generateSFTPTag == false && channelNames.get(cm).equals("SFTP Restart Channel"))
            	{
            		channelXMLOutput += "</exportData>\n";
            	}
            }
            channelXMLOutput += "</channel>";
            
            try (PrintWriter ctlOut = new PrintWriter(currentChannelFile)) 
            {
    			ctlOut.println(channelXMLOutput);
    			ctlOut.close();
            } 
            catch (FileNotFoundException fileExcept2) 
            {
                System.out.println("Second channel export");
                System.out.println("I DIDN'T FIND THE FILE");
            }
            
            //replaces the rawChannelCLOB with the new result
            rawChannelCLOB.set(cm, channelXMLOutput);
            
            //new in 2.2.3 - Evaluating if an SFTP restart channel is needed
            //changed location in the loop to be at the bottom as it was skipping the last channel for the check
        	if(cm == channelNames.size() -1 && sftpCheck == true)
        	{ 
                sftpChannelBuilder(host);
                sftpCheck = false;
        	}
        }                       
        //END channelExport Appending      
        
        return "metaDataExported";
    }

    public static String exportCodeTemplates(String host) throws FileNotFoundException
    {
    	//creates backup directory if it does not exist
    	File dir = new File(backupFolderPath+"channelCodeTemplatesBackup\\codeTemplateLibraries\\");
    	dir.mkdirs();
    	dir = new File(backupFolderPath+"channelCodeTemplatesBackup\\codeTemplates\\");
    	dir.mkdirs();

    	//int to track if the first query has ran
    	int queryCount = 0;
    	
    	String query="";
    	while(queryCount <= 1)
    	{
    		//exports CodeTemplateLibraries
    		try(Connection conn = DriverManager.getConnection(host); Statement stmt = conn.createStatement())
    		{
    			//sets queries
    			if(queryCount == 0)
    			{
    				query = "SELECT * FROM CODE_TEMPLATE_LIBRARY";
    			}
    			else
    			{
    				query = "SELECT * FROM CODE_TEMPLATE";
    			}
    			ResultSet rs = stmt.executeQuery(query);
    			ResultSetMetaData RSMD = rs.getMetaData();
    			int columns = RSMD.getColumnCount();

    			try
    			{
    				while (rs.next()) 
    				{
    					//column containing CLOB
    					String XMLdata = rs.getString(4);
    					//exports all channel export files to the specified directory
    					String destination = "";
    					if(queryCount == 0)
    					{
    						codeTemplateLibrary_NAME.add(rs.getString(2));
    						codeTemplateLibrary_XML.add(rs.getString(4));				
    					}
    					else
    					{
    						codeTemplate_ID.add(rs.getString(1));
    						codeTemplate_XML.add(rs.getString(4));
    					}
    				}
    			} 
    			catch (SQLException sqlExcept) 
    			{
    				System.out.println("FAILED MISERABLY");
    				System.out.println(sqlExcept);
    			}
    		}
    		catch (Exception e) 
    		{
    			if(e.toString().contains("CODE_TEMPLATE_LIBRARY"))
    			{
    				System.out.println("Error: " + e);
    				logCommands.exportToLog("Error: java.sql.SQLSyntaxErrorException: Table/View 'CODE_TEMPLATE_LIBRARY' does not exist.");
    				logCommands.exportToLog("Unable to properly export Code Template Libraries. The full/channel(s) export likely has generated without issue. Please open the XML file(s) to confirm");
    			}
    			
    			if(e.toString().contains("Database") && e.toString().contains("not found"))
                {
                	Main.configPathReader();
                }
    			e.printStackTrace();
    		}
    		queryCount++;
    		System.out.println("Query Count: " + queryCount);
    	}   	

    	//adds the opening "codeTemplateLibraries" tag
    	allCTLArray.add("<codeTemplateLibraries>"+"\n");
    	
    	//New for 2.2.3
    	//Check that will read the code template library XML, find the IDs, and then replace them with the specifig information
    	for(int bb=0;bb<codeTemplateLibrary_XML.size();bb++)
    	{
    		String currentCTL_XML = codeTemplateLibrary_XML.get(bb).toString();
    		String fullCurrentCTL_XML = "";
    		String[] splitCurrentCTL_XML = currentCTL_XML.split("\n");
    		
    		int idCount = 0;
    		for(int aa=0;aa<splitCurrentCTL_XML.length;aa++)
    		{
    			String line = splitCurrentCTL_XML[aa].trim();
    			if(!line.contains("<id>"))
    			{
    				fullCurrentCTL_XML += line + "\n";
    			}
    			else if(line.contains("<id>") && idCount == 0)
    			{
    				idCount++;
    				fullCurrentCTL_XML += line + "\n";
    			}
    			else if(line.contains("<codeTemplate "))
    			{
    				System.out.println(line);
    			}
    			else if(idCount>0 && line.contains("<id>"))
    			{
    				String editedLine = line.replace("<id>", "").replace("</id>", "");
    				String lineToReplaceID = "";
    				for(int cc=0;cc<codeTemplate_ID.size();cc++)
    				{    					
    					if(editedLine.trim().equals(codeTemplate_ID.get(cc).toString().trim()))
    					{
    						String[] removedParentTagsSplit = codeTemplate_XML.get(cc).toString().split("\n");
    						String removedParentTags = "";
    						for(int vv=0;vv<removedParentTagsSplit.length;vv++)
    						{
    							if(!removedParentTagsSplit[vv].toString().contains("codeTemplate"))
    							{
    								removedParentTags += removedParentTagsSplit[vv].toString() + "\n";
    							}
    						}
    						
    						fullCurrentCTL_XML += removedParentTags;
    					}
    				}
    			}
    		}
    		codeTemplateLibrary_XML.set(bb, fullCurrentCTL_XML);
    	}
    	
    	//Added in 2.2.3
    	//adds the edited code templates to the allCTLArray
    	for (int ww=0;ww<codeTemplateLibrary_XML.size();ww++)
    	{
    		allCTLArray.add(codeTemplateLibrary_XML.get(ww));
    	}

    	//adds the closing "codeTemplateLibraries" tag
    	allCTLArray.add("</codeTemplateLibraries>");
    	
    	//the below code creates a master codeTemplateLibrary String from all x CTLs
    	String allCodeTemplateLibraries = "";
    	File ctLDIR = new File(backupFolderPath+"channelCodeTemplatesBackup\\codeTemplateLibraries\\");
    	ctLDIR.getParentFile().mkdirs();
    	for(int m=0;m<codeTemplateLibrary_NAME.size();m++)
    	{
    		File currentCTLFile = new File(backupFolderPath+"channelCodeTemplatesBackup\\codeTemplateLibraries\\"+codeTemplateLibrary_NAME.get(m)+".xml");
    		try 
    		{
				currentCTLFile.createNewFile();
			} 
    		catch (IOException e) 
    		{
				e.printStackTrace();
			}
    		try (PrintWriter ctlOut = new PrintWriter(currentCTLFile)) 
            {
    			ctlOut.println(codeTemplateLibrary_XML.get(m));
    			ctlOut.close();
            } 
            catch (FileNotFoundException fileExcept2) 
            {
                System.out.println("Second channel export");
                System.out.println("I DIDN'T FIND THE FILE");
            }
    	}    	
    	return "codeTemplates and Libraries Exported";
    }

    public static String clearChannelFolder()
    {
    	//added in 2.2.3 to clear the CTL and Full folders as well
    	String[] folderPaths = {"channelBackup\\", "channelCodeTemplatesBackup\\codeTemplateLibraries\\", "fullMirthExport\\", "channelCodeTemplatesBackup\\"};
    	for(int clear=0;clear<folderPaths.length;clear++)
    	{
    		File channelDirFileList = new File(backupFolderPath+folderPaths[clear]);
        	if(channelDirFileList.exists())
        	{
        		String[] entries = channelDirFileList.list();
        		for (String s : entries) 
        		{
        			File currentFile = new File(channelDirFileList.getPath(), s);
        			currentFile.delete();
        		}
        		channelDirFileList.delete();
        	}
    	}
    	return "channel folder cleared";
    }
    
    //calls for fullConfigExport
    public static int retrunChannelArraySize()
    {
    	return rawChannelCLOB.size();
    }
    
    public static String returnCurrChannel(int place)
    {
    	return rawChannelCLOB.get(place).toString();
    }
        
    public static int retrunCodeTemplateLibrarySize()
    {
    	return codeTemplateLibrary_XML.size();
    }
    
    public static String returnCurrCTL(int place)
    {
    	return codeTemplateLibrary_XML.get(place).toString();
    }

    public static String sftpChannelBuilder(String host)
    {
    	String mirthVersion = fullConfigExport.getMirthVersion(host).replace("\"", "");
    	String[] splitVersion = mirthVersion.split("\\.");
    	
    	if(Integer.parseInt(splitVersion[0]) > 3 || Integer.parseInt(splitVersion[0]) >= 3 && Integer.parseInt(splitVersion[1]) >= 10)
    	{
    		System.out.println("I am greater than/equal to Mirth version 3.10.0");
    		if(!channelNames.contains("SFTP Restart Channel") && !channelIDs.contains("07f073af-c1b5-43a1-be3b-6d211f08cabb"))
    		{
    			System.out.println("isSFTPChannelNeeded: " + isSFTPChannelNeeded);
				System.out.println("sftpGenChoice: " + sftpGenChoice);
    			if(isSFTPChannelNeeded == true && sftpGenChoice == true)
    			{
    				generateSFTPTag = true;
	    			sftpChannelNames += "[";
	    			for(int nn=0;nn<sftpConnectedChannels.size();nn++) 
	    			{
	    				if(sftpConnectedChannels.size() == 1)
	    				{
	    					sftpChannelNames += "'" + sftpConnectedChannels.get(nn) + "'";
	    				}
	    				else
	    				{
	    					if(nn == sftpConnectedChannels.size()-1)
	    					{
	    						sftpChannelNames += "'" + sftpConnectedChannels.get(nn) + "'";
	    					}
	    					else
	    					{
	    						sftpChannelNames += "'" + sftpConnectedChannels.get(nn) + "', ";	
	    					}    					
	    				}
	    			}
	    			sftpChannelNames += "]";
	    			System.out.println("Replacement for XML File: " + sftpChannelNames);
	    			
	    			InputStream inputStream = channelExport.class.getResourceAsStream("/SFTP Restart Channel.xml");
	    			if (inputStream != null) 
	    			{
	    			    try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) 
	    			    {
	    			        String line;
	    			        while ((line = reader.readLine()) != null) 
	    			        {
	    			        	sftpRestartSplit.add(line);
	    			        }
	    			    } 
	    			    catch (IOException e) 
	    			    {
	    			        e.printStackTrace();
	    			    }
	    			} 
	    			else 
	    			{
	    			    System.err.println("File not found in resources.");
	    			}
	    			
	    			//compiles the full XML and swaps out description and the target channels
	    			String fullSFTPwithReplacements = "";
	    			for(int fun=0;fun<sftpRestartSplit.size();fun++) 
	    			{
	    				String currentLine = sftpRestartSplit.get(fun).toString();
	    				if(currentLine.contains("[&apos;CHANNEL NAME GOES HERE&apos;]"))
	    				{
	    					fullSFTPwithReplacements += currentLine.replace("[&apos;CHANNEL NAME GOES HERE&apos;]", sftpChannelNames) + "\n";
	    				}
	    				else if(currentLine.contains("CHANGETHATDATE"))
	    				{
	    					fullSFTPwithReplacements += currentLine.replace("CHANGETHATDATE", logCommands.getDateTime()) + "\n";
	    				}
	    				else
	    				{
	    					fullSFTPwithReplacements += currentLine + "\n";
	    				}
	    			}
	    			channelNames.add("SFTP Restart Channel");
	    			rawChannelCLOB.add(fullSFTPwithReplacements);
	    			channelIDs.add("07f073af-c1b5-43a1-be3b-6d211f08cabb");
	    			channelMetadata.add("nullPlaceholder");
    			}
    			else
    			{
    				if(sftpGenChoice == false)
    				{
    					System.out.println("User chose to disable SFTP generation via MORE FUNCTIONS");
    				}
    				else
    				{
    					System.out.println("No SFTP connection type was found. No need to create the channel");
    				}   				
    			}
    		}
    		else
    		{
    			System.out.println("SFTP Restart Channel already exists in client's database");
    		}		
    	}
    	else
    	{
    		System.out.println("Mirth version is '" + mirthVersion + "'. Mirth 3.10.0 or higher is required for an SFTP Restart channel");
    	}
    	
    	return "SFTP Restart Channel Added";
    }
    
    public static boolean setSFTPGeneration(boolean choice)
    {
    	sftpGenChoice = choice;
    	return sftpGenChoice;
    }
    
    public static boolean allowSFTPGeneration()
    {
    	return sftpGenChoice;
    }
    
    public static boolean setForceNewChannelGeneration(boolean choice)
    {
    	System.out.println("Mirth will use the new channel generation: " + choice);
    	forceNewChannelGeneration = choice;
    	return forceNewChannelGeneration;
    }
    
    public static boolean getForceNewChannelGeneration()
    {
    	return forceNewChannelGeneration;
    }
    
    public static boolean setSkipCMD(boolean setter)
    {
    	skipCMD = setter;
    	return skipCMD;
    }
    
    public static boolean getSkipCMD()
    {
    	return skipCMD;
    }
}