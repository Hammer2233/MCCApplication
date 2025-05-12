package main;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
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

    //boolean to determine if code templates are included
    private static boolean includeCTLs;
    private static boolean isFullMirthExport = false;

    private static String backupFolderPath = Main.getBackupFolder();

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

    	backupFolderPath = Main.getBackupFolder();
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
            	if(kl == currentChannelSplit.length-1)
            	{
            		for(int jk=0;jk<channelIDs.size();jk++)
            		{
            			if(channelMetadata.get(jk).toString().contains(channelIDs.get(cm).toString()))
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
            	}
            }
            
            if(includeCTLs == true)
            {
            	channelXMLOutput += "<codeTemplateLibraries>\n";
            	
            	for(int hi=0;hi<codeTemplateLibrary_XML.size();hi++)
            	{
            		channelXMLOutput += codeTemplateLibrary_XML.get(hi);
            	}
            	
            	//channelXMLOutput += "</codeTemplateLibraries>\n";
            	channelXMLOutput += "</codeTemplateLibraries>\n</exportData>\n";
            }
            else
            {
            	channelXMLOutput += "</exportData>\n";
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
    	File channelDirFileList = new File(backupFolderPath+"channelBackup\\");
    	if(channelDirFileList.exists())
    	{
    		String[] entries = channelDirFileList.list();
    		for (String s : entries) 
    		{
    			File currentFile = new File(channelDirFileList.getPath(), s);
    			currentFile.delete();
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
}