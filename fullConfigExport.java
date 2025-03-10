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

    public static String exportChannelGroups(String host) throws SQLException, FileNotFoundException
    {
        connection = host;
        backupFolderPath = Main.getBackupFolder();
        //serverSettingCreation();
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
                    //exports all channel groups to the specified directory
                    try (PrintWriter XMLout = new PrintWriter(mainPath + fileName+".xml")) 
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
        String channelGroupDir = backupFolderPath+"\\fullMirthExport\\channelGroups\\";
        File channelGroupDirList = new File(channelGroupDir);
        //channelGroupDirList.mkdir();
        File[] channelGroupList = channelGroupDirList.listFiles();
        //if/else added to account for no channelGroups
        if(channelGroupList == null || channelGroupList.length < 1 )
        {
            masterMirthExport.add("<channelGroups/>");
        }
        else
        {
            masterMirthExport.add("<channelGroups>");

            for(int k=0;k<channelGroupList.length;k++)
            {
                try(Scanner channelGroupReader = new Scanner(channelGroupList[k]))
                {
                  while(channelGroupReader.hasNext())
                  {
                    String line = channelGroupReader.nextLine();
                    masterMirthExport.add(line);
                  }
                  channelGroupReader.close();
                }
                catch(FileNotFoundException e)
                {
                    System.out.println("I COULDN'T FIND THE channelGroup FILE");
                }
            }
            masterMirthExport.add("</channelGroups>");
        }

        //adds channelData to the master Array
        masterMirthExport.add("<channels>");

        String channelDir = backupFolderPath+"\\channelBackup\\";
        File channelDirFile = new File(channelDir);
        File[] channelDirList = channelDirFile.listFiles();
        for(int u=0;u<channelDirList.length;u++)
        {
            try(Scanner channelReader = new Scanner(channelDirList[u]))
            {
              while(channelReader.hasNext())
              {
                String line = channelReader.nextLine();
                masterMirthExport.add(line);
              }
              channelReader.close();
            }
            catch(FileNotFoundException e)
            {
                System.out.println("I COULDN'T FIND THE channel XML FILE");
            }
        }
        masterMirthExport.add("</channels>");

        //adds channelTags to the master Array
        ArrayList channelTagArray = new ArrayList<>();
        String channelTagDir = backupFolderPath+"\\fullMirthExport\\configurationFiles\\channelTags";
        File channelTagDirFile = new File(channelTagDir);

        if(channelTagDirFile.exists())
        {
            try(Scanner channelTagsReader = new Scanner(channelTagDirFile))
            {
                while(channelTagsReader.hasNext())
                {
                    String line = channelTagsReader.nextLine();
                    channelTagArray.add(line);
                }
                channelTagsReader.close();
            }
            //evaluates the trimming and replacement of "set" tags with "channelTags"
            if(channelTagArray.size()<=1)
            {
                masterMirthExport.add("<channelTags/>");
            }
            else
            {
                channelTagArray.set(0, "<channelTags>");
                channelTagArray.set(channelTagArray.size()-1, "</channelTags>");
                for(int n=0;n<channelTagArray.size();n++)
                {
                    masterMirthExport.add(channelTagArray.get(n));
                }
            }
        }
        else
        {   
            masterMirthExport.add("<channelTags/>");
        }
        channelTagArray.clear();

        //call to add Alerts to the master Array
        try 
        {
            exportAlerts(connection);
        } 
        catch (SQLException e) 
        {
            e.printStackTrace();
        }

        //adds codeTemplateLibraries to the masterArray
        logCommands.exportToLog("EXPORTED - Code Template Libraries");
        masterMirthExport.add("<codeTemplateLibraries>");
        String codeTemplateLibsPath = backupFolderPath+"\\channelCodeTemplatesBackup\\codeTemplateLibraries\\";
        File codeTemplateLibsFile = new File(codeTemplateLibsPath);
        File[] codeTemplateLibsList = codeTemplateLibsFile.listFiles();
        for(int p=0;p<codeTemplateLibsList.length;p++)
        {
            try(Scanner codeTemplateLibsReader = new Scanner(codeTemplateLibsList[p]))
            {
              while(codeTemplateLibsReader.hasNext())
              {
                String line = codeTemplateLibsReader.nextLine();
                masterMirthExport.add(line);
              }
              codeTemplateLibsReader.close();
            }
            catch(FileNotFoundException e)
            {
                System.out.println("I COULDN'T FIND THE codeTemplateLibrary XML FILE");
            }
        }
        masterMirthExport.add("</codeTemplateLibraries>");

        //begin creating the serverSettings portion
        serverSettingsArray.add("<serverSettings>");
        for(int ii=0;ii<targetFiles.length;ii++)
        {
            String configFilesDir = backupFolderPath+"\\fullMirthExport\\configurationFiles\\";
            File configFilesCurrent = new File(configFilesDir+targetFiles[ii]);
            try(Scanner configFileReader = new Scanner(configFilesCurrent))
            {
                while(configFileReader.hasNext())
                {
                    String line = configFileReader.nextLine();
                    if(ii == 0)
                    {
                        serverSettingsArray.add("<environmentName>" + line.trim() + "</environmentName>");
                    }
                    else if(ii == 1)
                    {
                        String combinedClearGlobalMap = "<clearGlobalMap>";
                        //serverSettingsArray.add("<clearGlobalMap>");
                        if(line.trim() == "1")
                        {
                            combinedClearGlobalMap += "true";
                        }
                        else
                        {
                            combinedClearGlobalMap += "false";
                        }
                        serverSettingsArray.add(combinedClearGlobalMap+"</clearGlobalMap>");
                    }
                    else if(ii == 2)
                    {
                        serverSettingsArray.add("<queueBufferSize>" + line.trim() + "</queueBufferSize>");
                    }
                    else if(ii == 3)
                    {
                        if(line.contains("<list>"))
                        {
                            serverSettingsArray.add("<defaultMetaDataColumns>");
                        }
                        else if(line.contains("</list>"))
                        {
                            serverSettingsArray.add("</defaultMetaDataColumns>");
                        }
                        else
                        {
                            serverSettingsArray.add(line);
                        }
                    }
                    else if(ii == 4)
                    {
                        if(line.contains("<awt-color>"))
                        {
                            serverSettingsArray.add("<defaultAdministratorBackgroundColor>");
                        }
                        else if(line.contains("</awt-color>"))
                        {
                            serverSettingsArray.add("</defaultAdministratorBackgroundColor>");
                        }
                        else
                        {
                            serverSettingsArray.add(line);
                        }
                    }
                    else if(ii == 5)
                    {
                        serverSettingsArray.add("<smtpHost>"+ line.trim() + "</smtpHost>");
                    }
                    else if(ii == 6)
                    {
                        serverSettingsArray.add("<smtpPort>" + line.trim() + "</smtpPort>");
                    }
                    else if(ii == 7)
                    {
                        serverSettingsArray.add("<smtpTimeout>" + line.trim() + "</smtpTimeout>");
                    }
                    else if(ii == 8)
                    {
                        serverSettingsArray.add("<smtpFrom>" + line.trim() + "</smtpFrom>");
                    }
                    else if(ii == 9)
                    {
                        serverSettingsArray.add("<smtpSecure>" + line.trim() + "</smtpSecure>");
                    }
                    else if(ii == 10)
                    {
                        serverSettingsArray.add("<smtpAuth>" + line.trim() + "</smtpAuth>");
                    }
                    else if(ii == 11)
                    {
                        serverSettingsArray.add("<smtpUsername>" + line.trim() + "</smtpUsername>");
                    }
                    else if(ii == 12)
                    {
                        serverSettingsArray.add("<smtpPassword>" + line.trim() + "</smtpPassword>");
                    }
                    else if(ii == 13)
                    {
                        serverSettingsArray.add("<loginNotificationEnabled>" + line.trim() + "</loginNotificationEnabled>");
                    }
                    else if(ii == 14)
                    {
                        serverSettingsArray.add("<loginNotificationMessage>" + line.trim() + "</loginNotificationMessage>");
                    }
                    else if(ii == 15)
                    {
                        serverSettingsArray.add("<administratorAutoLogoutIntervalEnabled>" + line.trim() + "</administratorAutoLogoutIntervalEnabled>");
                    }
                    else if(ii == 16)
                    {
                        serverSettingsArray.add("<administratorAutoLogoutIntervalField>" + line.trim() + "</administratorAutoLogoutIntervalField>");
                    }

                }    
                configFileReader.close(); 
            }
            catch(FileNotFoundException e)
            {
                System.out.println("I COULDN'T FIND THE " + configFilesCurrent + " XML FILE");
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
        String updateSettingsDir = backupFolderPath+"\\fullMirthExport\\configurationFiles\\";

        for(int bb=0;bb<updateSettings.length;bb++)
        {
            File updateSettingsFile = new File(updateSettingsDir+updateSettings[bb]);
            try(Scanner channelReader = new Scanner(updateSettingsFile))
            {
                while(channelReader.hasNext())
                {
                    String line = channelReader.nextLine();
                    String combinedLine = "";
                    if(bb==0)
                    {
                        combinedLine = "<statsEnabled>" + line.trim() + "</statsEnabled>";
                    }
                    else
                    {
                        combinedLine = "<lastStatsTime>" + line.trim() + "</lastStatsTime>";
                    }
                    masterMirthExport.add(combinedLine);
                }
                channelReader.close();
            }
            catch(FileNotFoundException e)
            {
                System.out.println("I COULDN'T FIND THE channel XML FILE");
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

            try
            {
                while (rs.next()) 
                {
                    //creates folder path if it does not yet exist:
                    String mainPath = backupFolderPath+"\\fullMirthExport\\globalScripts\\";
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
                    String XMLdata = rs.getString(3);
                    //exports all channel groups to the specified directory
                    try (PrintWriter XMLout = new PrintWriter(mainPath + fileName)) 
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

            //adds the globalScripts to the main Array
            masterMirthExport.add("<globalScripts>");
            String gsPath = backupFolderPath+"\\fullMirthExport\\globalScripts\\";
            File gsDir = new File(gsPath);
            gsDir.mkdir();
            File[] globalScriptsDir = gsDir.listFiles();
            for(int a=0;a<globalScriptsDir.length;a++)
            {
                String combinedLine = "<string>";
                masterMirthExport.add("<entry>");
                masterMirthExport.add("<string>" + globalScriptsDir[a].getName() + "</string>");
                try(Scanner gsReader = new Scanner(globalScriptsDir[a]))
                {
                    while(gsReader.hasNext())
                    {
                        String line = gsReader.nextLine();
                        combinedLine += line+"\n";
                    }
                    gsReader.close();
                }
                masterMirthExport.add(combinedLine+"</string>");
                masterMirthExport.add("</entry>");
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
            String pluginFilesDir = backupFolderPath+"\\fullMirthExport\\configurationFiles\\";
            File currentPluginFile = new File(pluginFilesDir+pluginPropertiesFiles[ii]);
            try(Scanner configFileReader = new Scanner(currentPluginFile))
            {
                while(configFileReader.hasNext())
                {
                    String line = configFileReader.nextLine();
                    String currentPluginName = currentPluginFile.getName();
                    if(currentPluginName.equals("archiverOptions") || currentPluginName.equals("pollingProperties"))
                    {
                        if(line.contains("PollConnectorProperties version") || line.contains("<com.mirth.connect.util.messagewriter.MessageWriterOptions"))
                        {
                            masterMirthExport.add("<property name=\""+currentPluginName+"\">" + line.replace(">","&gt;").replace("<","&lt;"));
                        }
                        else if(line.contains("PollConnectorProperties>") || line.contains("/com.mirth.connect.util.messagewriter.MessageWriterOptions"))
                        {
                            masterMirthExport.add(line.replace(">","&gt;").replace("<","&lt;") + "</property>");
                        }
                        else
                        {
                            masterMirthExport.add(line.replace(">","&gt;").replace("<","&lt;"));
                        }
                    }
                    else
                    {
                        masterMirthExport.add("<property name=\""+ currentPluginName +"\">" + line.trim() + "</property>");
                    }
                }
            }
            catch(FileNotFoundException e)
            {
                System.out.println("UNABLE TO FIND FILE: " + pluginPropertiesFiles[ii]);
            }
        }
        
        masterMirthExport.add("</properties>");
        masterMirthExport.add("</entry>");
        masterMirthExport.add("</pluginProperties>");

        //adds resourceProperties to the main Array
        String resourcePropertiesPath = backupFolderPath+"\\fullMirthExport\\configurationFiles\\resources";
        File resourcePropertiesFile = new File(resourcePropertiesPath);

        try(Scanner resourcePropReader = new Scanner(resourcePropertiesFile))
        {
            while(resourcePropReader.hasNext())
            {
                String line = resourcePropReader.nextLine();
                if(line.contains("<resourcePropertiesList"))
                {
                    masterMirthExport.add("<resourceProperties>");
                }
                else if(line.contains("</resourcePropertiesList"))
                {
                    masterMirthExport.add("</resourceProperties>");
                }
                else
                {
                    masterMirthExport.add(line);
                }                
            }
            resourcePropReader.close();
        }
        catch(FileNotFoundException e)
        {
            System.out.println("I COULDN'T FIND THE resources FILE");
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
        return "version found";
    }

    public static String exportAlerts(String host) throws FileNotFoundException, SQLException
    {
        //Exports and edits the channelMetadata
      try (Connection conn = DriverManager.getConnection(host); Statement stmt = conn.createStatement()) 
      {
        String query = "SELECT * FROM ALERT";
        ResultSet rs = stmt.executeQuery(query);
        ResultSetMetaData RSMD = rs.getMetaData();
        int columns = RSMD.getColumnCount();

        File configFilesDir = new File(backupFolderPath+"fullMirthExport\\Alerts");
        configFilesDir.mkdirs();

        try 
        {
        while (rs.next()) 
        {
            String fileName = rs.getString(2);
            if (columns == 2 || fileName.length() > 100) 
            {
            int pos1 = fileName.indexOf("<name>") + 6;
            int pos2 = fileName.indexOf("</name>");
            fileName = fileName.substring(pos1, pos2);
            }
            fileName = fileName.replace("/", "-FW_SLASH-").replace("\\", "-BK_SLASH-").replace(":", "-COLON-").replace("*", "-ASTERISK-").replace("?", "-QUESTION_MARK-").replace("\"", "-QUOT_MARK-").replace("<", "-LESS_THAN-").replace(">", "-GREATER_THAN-").replace("|", "-VERTICAL_BAR-");
                
            //CHANGE THIS BELOW
            //THIS COLUMN INDEX chooses what data to convert to XML
            String XMLdata = rs.getString(3);

            //exports all CONFIGURATION files to the specified directory
            try (PrintWriter XMLout = new PrintWriter(backupFolderPath+"fullMirthExport\\Alerts\\" + fileName)) 
            {
                XMLout.println(XMLdata);
                XMLout.close();
            } 
            catch (FileNotFoundException fileExcept2) 
            {
                System.out.println("First channel export");
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

        
        //adds Alerts to master Array
        String alertSettings = backupFolderPath+"\\fullMirthExport\\Alerts\\";
        File alertSettingList = new File(alertSettings);
        alertSettingList.mkdir();
        File[] alertFileList = alertSettingList.listFiles();
        //if/else added to account for no alerts
        if(alertFileList.length < 1)
        {
            masterMirthExport.add("<alerts/>");
        }
        else
        {
            masterMirthExport.add("<alerts>");

            for(int k=0;k<alertFileList.length;k++)
            {
                try(Scanner channelGroupReader = new Scanner(alertFileList[k]))
                {
                  while(channelGroupReader.hasNext())
                  {
                    String line = channelGroupReader.nextLine();
                    masterMirthExport.add(line);
                  }
                  channelGroupReader.close();
                }
                catch(FileNotFoundException e)
                {
                    System.out.println("I COULDN'T FIND THE channelGroup FILE");
                }
            }
            masterMirthExport.add("</alerts>");
            logCommands.exportToLog("EXPORTED - Alerts");
        }
    return "Alerts exported";
    }
}
