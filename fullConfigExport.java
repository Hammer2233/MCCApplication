package main;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Scanner;

public class fullConfigExport 
{
    //String for Mirth Version
    private static String versionInfo;

    //arraylists for each portion of the Mirth Config Export
    private static ArrayList masterMirthExport = new ArrayList<>();
    private static ArrayList serverSettingsArray = new ArrayList<>();

    private static String backupFolderPath;
    private static String connection = "";
    
    //ArrayLists added in version 2.2.3 \/\/
    private static ArrayList channelGroup_XML = new ArrayList();
    private static ArrayList configurationFile_NAMES = new ArrayList();
    private static ArrayList configuration_Data = new ArrayList();
    private static ArrayList alertsArray = new ArrayList();
    //ArrayLists added in version 2.2.3 /\/\

    public static String exportChannelGroups(String host) throws SQLException, FileNotFoundException
    {
        backupFolderPath = Main.getBackupFolder();
        try(Connection conn = DriverManager.getConnection(host); Statement stmt = conn.createStatement())
        {
            String query = "SELECT * FROM CHANNEL_GROUP";
            ResultSet rs = stmt.executeQuery(query);
            ResultSetMetaData RSMD = rs.getMetaData();
            int columns = RSMD.getColumnCount();

            try
            {
                while (rs.next()) 
                {
                    //creates folder path if it does not yet exist:
                    String mainPath = backupFolderPath+"\\fullMirthExport\\channelGroups\\";
                    File dir = new File(mainPath);
                    dir.mkdirs();

                    //column containing CLOB
                    String XMLdata = rs.getString(4);
                    channelGroup_XML.add(XMLdata);
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
        serverSettingCreation();
        return "Channel Groups Exported";
    }

    public static String exportScripts(String host) throws SQLException
    {
        try(Connection conn = DriverManager.getConnection(host); Statement stmt = conn.createStatement())
        {
            String query = "SELECT * FROM SCRIPT";
            ResultSet rs = stmt.executeQuery(query);
            ResultSetMetaData RSMD = rs.getMetaData();
            int columns = RSMD.getColumnCount();

            try
            {
                while (rs.next()) 
                {
                    //creates folder path if it does not yet exist:
                    String mainPath = backupFolderPath+"\\fullMirthExport\\scripts\\";
                    File dir = new File(mainPath);
                    dir.mkdirs();

                    String fileName = rs.getString(2);
                    if (columns == 2 || fileName.length() > 100) 
                    {
                    int pos1 = fileName.indexOf("<name>") + 6;
                    int pos2 = fileName.indexOf("</name>");
                    fileName = fileName.substring(pos1, pos2);
                    }
                    fileName = fileName.replace("/", "-FW_SLASH-").replace("\\", "-BK_SLASH-").replace(":", "-COLON-").replace("*", "-ASTERISK-").replace("?", "-QUESTION_MARK-").replace("\"", "-QUOT_MARK-").replace("<", "-LESS_THAN-").replace(">", "-GREATER_THAN-").replace("|", "-VERTICAL_BAR-");
                
                    //column containing CLOB
                    String XMLdata = rs.getString(4);
                    //exports all script files to the specified directory
                    try (PrintWriter XMLout = new PrintWriter(dir + fileName+".xml")) 
                    {
                        XMLout.println(XMLdata);
                        XMLout.close();
                    } 
                    catch (FileNotFoundException fileExcept2) 
                    {
                        System.out.println("I DIDN'T FIND THE FILE");
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
        return "Scripts Exported";
    }

    //Creates the serverSettings portion of the full Mirth config export
    public static String serverSettingCreation() throws FileNotFoundException
    {    	
    	//Added in 2.2.3
    	//Calls the configuration and copies out all necessary files	
    	String host = Main.returnHost();
    	try(Connection conn = DriverManager.getConnection(host); Statement stmt = conn.createStatement())
        {
            String query = "SELECT * FROM CONFIGURATION";
            ResultSet rs = stmt.executeQuery(query);
            ResultSetMetaData RSMD = rs.getMetaData();
            int columns = RSMD.getColumnCount();

            try
            {
                while (rs.next()) 
                {
                    //column containing CLOB
                	String configurationName = rs.getString(2);
                	configurationFile_NAMES.add(configurationName);
                    String configurationValue = rs.getString(3);
                	configuration_Data.add(configurationValue);                    
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
    	
        //declares names of all files needed
        String[] targetFiles = {"environment.name", "server.resetglobalvariables", "server.queuebuffersize", "server.defaultmetadatacolumns", "server.defaultadministratorcolor",
         "smtp.host", "smtp.port", "smtp.timeout", "smtp.from", "smtp.secure", "smtp.auth", "smtp.username", "smtp.password", "loginnotification.enabled",
        "loginnotification.message", "administratorautologoutinterval.enabled", "administratorautologoutinterval.field"};

        String path = backupFolderPath+"\\fullMirthExport\\configurationFiles\\";
        File configFileDir = new File(path);

        //start creating the master array and child arrays
        getMirthVersion(Main.returnHost());
        masterMirthExport.add("<serverConfiguration version="+versionInfo+">");

        //establishes date/time for date tag
        LocalDateTime currTime = LocalDateTime.now();
        DateTimeFormatter currTimeFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formattedCurrTime = currTime.format(currTimeFormat);
        masterMirthExport.add("<date>"+formattedCurrTime+"</date>");

        //adds channelGroups to master Array
        //changed in 2.2.3
        if(channelGroup_XML == null || channelGroup_XML.size() < 1 )
        {
        	masterMirthExport.add("<channelGroups/>");
        }
        else
        {
        	masterMirthExport.add("<channelGroups>");

        	for(int k=0;k<channelGroup_XML.size();k++)
        	{
        		masterMirthExport.add(channelGroup_XML.get(k));
        	}
        	masterMirthExport.add("</channelGroups>");
        }

        //adds channelData to the master Array
        masterMirthExport.add("<channels>");
        
        int channelArraySize = channelExport.retrunChannelArraySize();
        for(int u=0;u<channelArraySize;u++)
        {
        	masterMirthExport.add(channelExport.returnCurrChannel(u).replace("<map>", ""));
        }

        masterMirthExport.add("</channels>");

        //adds channelTags to the master Array
        if(configurationFile_NAMES.contains("channelTags"))
        {
        	masterMirthExport.add("<channelTags>");
        	int indexOfResult = configurationFile_NAMES.indexOf("channelTags");
        	masterMirthExport.add(configuration_Data.get(indexOfResult).toString().replace("<set>", "").replace("</set>", ""));
        	masterMirthExport.add("</channelTags>");
        }
        else
        {
        	masterMirthExport.add("<channelTags/>");
        }

        //call to add Alerts to the master Array
        try 
        {
            exportAlerts(host);
        } 
        catch (SQLException e) 
        {
            e.printStackTrace();
        }

        //adds codeTemplateLibraries to the masterArray
        int ctlArraySize = channelExport.retrunCodeTemplateLibrarySize();
        if(ctlArraySize<1)
        {
        	masterMirthExport.add("<codeTemplateLibraries/>");
        }
        else
        {
        	logCommands.exportToLog("EXPORTED - Code Template Libraries");   
        	masterMirthExport.add("<codeTemplateLibraries>");
        	for(int vn=0;vn<ctlArraySize;vn++)
        	{
        		masterMirthExport.add(channelExport.returnCurrCTL(vn));
        	}
        	masterMirthExport.add("</codeTemplateLibraries>");
        }

        //begin creating the serverSettings portion
        serverSettingsArray.add("<serverSettings>");
        for(int fbf=0;fbf<targetFiles.length;fbf++)
        {
        	if(configurationFile_NAMES.contains(targetFiles[fbf]))
        	{
        		int indexInArray = configurationFile_NAMES.indexOf(targetFiles[fbf]);
        		String currFile = targetFiles[fbf];
        		String currCMData = configuration_Data.get(indexInArray).toString();
        		
        		if(currFile.equals(targetFiles[0]))
                {
                    serverSettingsArray.add("<environmentName>" + currCMData + "</environmentName>");
                }
                else if(currFile.equals(targetFiles[1]))
                {
                    String combinedClearGlobalMap = "<clearGlobalMap>";
                    //serverSettingsArray.add("<clearGlobalMap>");
                    if(currCMData.trim() == "1")
                    {
                        combinedClearGlobalMap += "true";
                    }
                    else
                    {
                        combinedClearGlobalMap += "false";
                    }
                    serverSettingsArray.add(combinedClearGlobalMap+"</clearGlobalMap>");
                }
                else if(currFile.equals(targetFiles[2]))
                {
                    serverSettingsArray.add("<queueBufferSize>" + currCMData + "</queueBufferSize>");
                }
                else if(currFile.equals(targetFiles[3]))
                {
                	serverSettingsArray.add("<defaultMetaDataColumns>");
                	serverSettingsArray.add(currCMData.replace("<list>", "").replace("</list>", ""));
                	serverSettingsArray.add("</defaultMetaDataColumns>");
                }
                else if(currFile.equals(targetFiles[4]))
                {
                	serverSettingsArray.add("<defaultAdministratorBackgroundColor>");
                	serverSettingsArray.add(currCMData.replace("<awt-color>", "").replace("</awt-color>", ""));
                	serverSettingsArray.add("</defaultAdministratorBackgroundColor>");
                }
                else if(currFile.equals(targetFiles[5]))
                {
                    serverSettingsArray.add("<smtpHost>"+ currCMData + "</smtpHost>");
                }
                else if(currFile.equals(targetFiles[6]))
                {
                    serverSettingsArray.add("<smtpPort>" + currCMData.trim() + "</smtpPort>");
                }
                else if(currFile.equals(targetFiles[7]))
                {
                    serverSettingsArray.add("<smtpTimeout>" + currCMData.trim() + "</smtpTimeout>");
                }
                else if(currFile.equals(targetFiles[8]))
                {
                    serverSettingsArray.add("<smtpFrom>" + currCMData.trim() + "</smtpFrom>");
                }
                else if(currFile.equals(targetFiles[9]))
                {
                    serverSettingsArray.add("<smtpSecure>" + currCMData.trim() + "</smtpSecure>");
                }
                else if(currFile.equals(targetFiles[10]))
                {
                    serverSettingsArray.add("<smtpAuth>" + currCMData.trim() + "</smtpAuth>");
                }
                else if(currFile.equals(targetFiles[11]))
                {
                    serverSettingsArray.add("<smtpUsername>" + currCMData.trim() + "</smtpUsername>");
                }
                else if(currFile.equals(targetFiles[12]))
                {
                    serverSettingsArray.add("<smtpPassword>" + currCMData.trim() + "</smtpPassword>");
                }
                else if(currFile.equals(targetFiles[13]))
                {
                    serverSettingsArray.add("<loginNotificationEnabled>" + currCMData.trim() + "</loginNotificationEnabled>");
                }
                else if(currFile.equals(targetFiles[14]))
                {
                    serverSettingsArray.add("<loginNotificationMessage>" + currCMData.trim() + "</loginNotificationMessage>");
                }
                else if(currFile.equals(targetFiles[15]))
                {
                    serverSettingsArray.add("<administratorAutoLogoutIntervalEnabled>" + currCMData.trim() + "</administratorAutoLogoutIntervalEnabled>");
                }
                else if(currFile.equals(targetFiles[16]))
                {
                    serverSettingsArray.add("<administratorAutoLogoutIntervalField>" + currCMData.trim() + "</administratorAutoLogoutIntervalField>");
                }
        	}
        	else
        	{
        		System.out.println("I COULDN'T FIND THE " + targetFiles[fbf] + " FILE");
        	}
        }      
        
        for(int nn=0;nn<serverSettingsArray.size();nn++)
        {
            masterMirthExport.add(serverSettingsArray.get(nn));
        }
        masterMirthExport.add("</serverSettings>");

        //adds updateSettings to the master Array
        masterMirthExport.add("<updateSettings version="+versionInfo+">");

        String[] updateSettings = {"stats.enabled", "stats.time"};
        for(int bb=0;bb<updateSettings.length;bb++)
        {
        	if(configurationFile_NAMES.contains(updateSettings[bb]))
        	{
        		int currIndex = configurationFile_NAMES.indexOf(updateSettings[bb]);
        		if(bb==0)
        		{
        			masterMirthExport.add("<statsEnabled>" + configuration_Data.get(currIndex).toString().trim() + "</statsEnabled>");
        		}
        		else
        		{
        			masterMirthExport.add("<lastStatsTime>" + configuration_Data.get(currIndex).toString().trim() + "</lastStatsTime>");
        		}
        	}
        }        

        masterMirthExport.add("</updateSettings>");      
        /*
         * This point, we have created all XML tags from the main <serverConfiguration version="X.X.X"> tag to the </updateSettings> tab
         */

        getGlobalScripts(Main.returnHost());

         //FULL EXPORT of MIRTH CONFIG
         masterMirthExport.add("</serverConfiguration>"); 
         
         File testMirthFulExport = new File(backupFolderPath+"\\fullMirthExport\\");
         //testMirthFulExport.mkdir();
        try (PrintWriter fullConfigOut = new PrintWriter(testMirthFulExport+"\\"+Main.getDateTime()+" Config Export.xml"))
        {
        	//String appendedCTLMasterAddition = "";
        	for(int c=0;c<masterMirthExport.size();c++)
        	{
        		fullConfigOut.print(masterMirthExport.get(c)+"\n");
        	}
        	fullConfigOut.close();
        }   
        Main.deleteBuildingBlockFiles(); //RE-ENABLE ME: 
        //FULL EXPORT of MIRTH CONFIG

        //clears arraylists
        masterMirthExport.clear();
        serverSettingsArray.clear();
        alertsArray.clear();
        configuration_Data.clear();
        configurationFile_NAMES.clear();
        channelGroup_XML.clear();

        logCommands.exportToLog("EXPORTED - Server settings");
        return "created serverSettings";
    }

    public static String getGlobalScripts(String host) throws FileNotFoundException
    {
        try(Connection conn = DriverManager.getConnection(host); Statement stmt = conn.createStatement())
        {
            String query = "SELECT * FROM SCRIPT";
            ResultSet rs = stmt.executeQuery(query);
            ResultSetMetaData RSMD = rs.getMetaData();
            int columns = RSMD.getColumnCount();

            masterMirthExport.add("<globalScripts>");
            try
            {
                while (rs.next()) 
                {                    
                    //column containing CLOB
                	String idName = rs.getString(2);
                    String scriptText = rs.getString(3);
                    
                    masterMirthExport.add("<entry>");
                    masterMirthExport.add("<string>" + idName + "</string>");
                    masterMirthExport.add("<string>" + scriptText + "</string>");
                    masterMirthExport.add("</entry>");
                }
            } 
            catch (SQLException sqlExcept) 
            {
                System.out.println("FAILED MISERABLY");
                System.out.println(sqlExcept);
            }
            masterMirthExport.add("</globalScripts>");
        }
        catch (Exception e) 
        {
            e.printStackTrace();
        }

        //creates and adds the plugin properties to the master Array
        String[] pluginPropertiesFiles = {"archiveEnabled", "archiverBlockSize", "archiverOptions", "enabled", "includeAttachments", 
        "maxEventAge", "pollingProperties", "pruneEvents", "pruningBlockSize"};
        masterMirthExport.add("<pluginProperties>");
        masterMirthExport.add("<entry>");
        masterMirthExport.add("<string>Data Pruner</string>");
        masterMirthExport.add("<properties>");

        for(int ii=0;ii<pluginPropertiesFiles.length;ii++)
        {
        	String currFile = pluginPropertiesFiles[ii];
        	int currentPluginPropertyText = configurationFile_NAMES.indexOf(currFile);
        	
        	//added in 2.2.3 to check if the file from pluginPropertiesFiles was found in the DB
        	if (currentPluginPropertyText != -1) 
        	{

            	String currText = configuration_Data.get(currentPluginPropertyText).toString();
        		if(configurationFile_NAMES.contains(currFile))
            	{
            		if(currFile.equals("archiverOptions") || currFile.equals("pollingProperties"))
                    {
            			String[] splitProperties = configuration_Data.get(currentPluginPropertyText).toString().split("\n");
            			for(int jj=0;jj<splitProperties.length;jj++)
            			{
            				if(splitProperties[jj].contains("PollConnectorProperties version") || splitProperties[jj].contains("<com.mirth.connect.util.messagewriter.MessageWriterOptions"))
                            {
                                masterMirthExport.add("<property name=\""+currFile+"\">" + splitProperties[jj].replace(">","&gt;").replace("<","&lt;"));
                            }
                            else if(splitProperties[jj].contains("PollConnectorProperties>") || splitProperties[jj].contains("/com.mirth.connect.util.messagewriter.MessageWriterOptions"))
                            {
                                masterMirthExport.add(splitProperties[jj].replace(">","&gt;").replace("<","&lt;") + "</property>");
                            }
                            else
                            {
                                masterMirthExport.add(splitProperties[jj].replace(">","&gt;").replace("<","&lt;"));
                            }
            			}                    
                    }
                    else
                    {
                        masterMirthExport.add("<property name=\""+ currFile +"\">" + currText.trim() + "</property>");
                    }
            	}
        	}
        	else
        	{
        		System.out.println("File '" + pluginPropertiesFiles[ii] + "' not found");
        	}
        }  
        
        masterMirthExport.add("</properties>");
        masterMirthExport.add("</entry>");
        masterMirthExport.add("</pluginProperties>");

        //adds resourceProperties to the main Array
        if(configurationFile_NAMES.indexOf("resources") != -1)
        {
        	masterMirthExport.add(configuration_Data.get(configurationFile_NAMES.indexOf("resources")).toString().replace("resourcePropertiesList", "resourceProperties"));
        }
        
        //currently hardcoding channelDependencies and configurationMapping (will add after further research)
        masterMirthExport.add("<channelDependencies/>");
        masterMirthExport.add("<configurationMap/>");

        logCommands.exportToLog("EXPORTED - Pruning data");
        return "Set GlobalScripts";
    }

    public static String getMirthVersion(String host)
    {
        try(Connection conn = DriverManager.getConnection(host); Statement stmt = conn.createStatement())
        {
            String query = "SELECT * FROM SCHEMA_INFO";
            ResultSet rs = stmt.executeQuery(query);
            try
            {
                while (rs.next()) 
                {
                    //column containing version
                    versionInfo = rs.getString(1);
                }
            } 
            catch (SQLException sqlExcept) 
            {
                System.out.println("FAILED MISERABLY");
                System.out.println(sqlExcept);
            }
            versionInfo = "\""+versionInfo+"\"";
        }
        catch (Exception e) 
        {
            e.printStackTrace();
        }
        return versionInfo;
    }

    public static String exportAlerts(String host) throws SQLException
    {
        //Exports and edits the channelMetadata
    	try (Connection conn = DriverManager.getConnection(host); Statement stmt = conn.createStatement()) 
    	{
    		String query = "SELECT * FROM ALERT";
    		ResultSet rs = stmt.executeQuery(query);

    		try 
    		{
    			while (rs.next()) 
    			{		                
    				//THIS COLUMN INDEX chooses what data to convert to XML
    				String XMLdata = rs.getString(3);
    				alertsArray.add(XMLdata);
		        }
	        }
	        catch (SQLException sqlExcept) 
	        {
	        	System.out.println("FAILED MISERABLY");
	        	System.out.println(sqlExcept);
	        }
        }

        
        //adds Alerts to master Array
    	if(alertsArray == null || alertsArray.size() < 1)
    	{
    		masterMirthExport.add("<alerts/>");
    	}
    	else
    	{
    		masterMirthExport.add("<alerts>");
    		for(int bs=0;bs<alertsArray.size();bs++)
    		{
    			masterMirthExport.add(alertsArray.get(bs));
    		}
    		masterMirthExport.add("</alerts>");
    	}
        return "Alerts exported";
    }
}
