import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.IOException;

public class Main 
{   
  private static String dbPath;
  private static String dbSubfolder;
  private static String fullDBPath = "jdbc:derby:";
  private static String serviceState = "";

  private static String backupPath = "";
  private static boolean changedBackupPath = false;
  private static String changedBUpPathString = "";
  private static boolean changedMirthDBDirPath = false;
  private static String changedMDBDirPath = "";

    public static void main(String args[]) throws ClassNotFoundException, SQLException, FileNotFoundException
    {
      String driver = "org.apache.derby.jdbc.EmbeddedDriver";
      Class.forName(driver);
      //default Mirth DB Connection (hardcoded)
      //String host = "jdbc:derby:C:\\Program Files\\Mirth Connect\\appdata\\mirthdb";

      //calls connection to DB and applies it to the host for SQL queries
      determineDBLocation();
      String host = fullDBPath;

      //Call full channel backups
      channelExport exportChannels = new channelExport();
      channelExport.exportChannels(host);
      channelExport exportMetadata = new channelExport();
      channelExport.exportMetadata(host);
      checkMirthService();
    }

    //This code searches and finds the path that the Mirth Connect Service is pointing to
    //The BufferedReader reads the input stream, and finds the path
    private static void determineDBLocation()
    {
      if(changedMirthDBDirPath == true)
      {
        System.out.println("DB Path was Changed");
        dbPath = changedMDBDirPath;
      }
      else
      {
        try 
        {
          Runtime rt = Runtime.getRuntime();
          Process pr = rt.exec("sc qc \"Mirth Connect Service\"");
          BufferedReader input = new BufferedReader(new InputStreamReader(pr.getInputStream()));
          do 
          {
            dbPath = input.readLine();
          } 
          while (!dbPath.contains("BINARY_PATH_NAME") && !dbPath.contains("FAILED"));
        } 
        catch (IOException runtimeError) 
        {
          System.out.println(runtimeError);
        } 

        if (dbPath.contains("BINARY_PATH_NAME")) 
        {
          //creates path substring components
          int pos1 = dbPath.indexOf(":\\") - 1;
          int pos2 = dbPath.indexOf("\\mirthc") + 1;
          if (pos2 < 1)
          {
            pos2 = dbPath.indexOf("\\mcserv") + 1;
          }
          if (pos1 > 0 && pos2 > 0) 
          {
            dbPath = dbPath.substring(pos1, pos2);
          } 
          else 
          {
            System.out.println("Error in finding path");
            dbPath = "";
          } 
        } 
        else 
        {
          dbPath = "";
        } 
      }
      
      //This following section of code determines the subfolder for the DB filepath
      //It checks to see if the "appdata" folder exists. If not it connects right to "mirthdb"
      File dbSubPath = new File(dbPath+"appdata");
      if(dbSubPath.exists())
      {
        dbSubfolder = "appdata\\mirthdb;";
        fullDBPath = fullDBPath + dbPath + dbSubfolder;
      }
      else
      {
        dbSubfolder = "mirthdb";
        fullDBPath = fullDBPath + dbPath + dbSubfolder;
      }
      System.out.println("Full DB Path: " + fullDBPath);
    }

    //verifies if the Mirth service is running
    public static String checkMirthService()
    {
      String serviceStatus = "";
      try 
      {
        Runtime rt = Runtime.getRuntime();
        Process pr = rt.exec("sc query \"mirth connect service\"");
        BufferedReader input = new BufferedReader(new InputStreamReader(pr.getInputStream()));
        do 
        {
          serviceStatus = input.readLine();
        } 
        while (!serviceStatus.contains("STATE") && !serviceStatus.contains("FAILED"));
      } 
      catch (IOException runtimeError) 
      {
        System.out.println(runtimeError);
      } 

      //returns the status of the service
      if (!serviceStatus.contains("STOPPED"))
      {
        if (serviceStatus.contains("FAILED")) 
        {
          System.out.println("SERVICE NOT INSTALLED");
          serviceState = "FAILED";
        } 
        else 
        {
          System.out.println("SERVICE STARTED");
          serviceState = "STARTED";
        }
      }
      else
      {
        System.out.println("I AM STOPPED");
        serviceState = "STOPPED";
      }
      return serviceState;
    }

    public static String returnHost()
    {
      fullDBPath = "jdbc:derby:";
      determineDBLocation();
      String host = fullDBPath;
      return host;
    }

    public static String setChangedMirthDB(String newPath)
    {
      String chosenPath = newPath;
      if(chosenPath == "" && changedMDBDirPath != null)
      {
        
      }
      else if(chosenPath == "")
      {
        
      }
      else
      {
        changedMirthDBDirPath = true;
        changedMDBDirPath = chosenPath;
      }
      return "changed target DB";
    }

    public static String setChangedBackup(String newPath)
    {
      String chosenPath = newPath;
      if(chosenPath == "" && changedBUpPathString != null)
      {
        
      }
      else if(chosenPath == "")
      {

      }
      else
      {
        changedBackupPath = true;
        changedBUpPathString = chosenPath;
      }
      return "changed backup";
    }

    public static String setBackupFolder()
    {
      if(changedBackupPath == true)
      {
        System.out.println("CHANGED");
        backupPath = changedBUpPathString+"Backup " + getDateTime()+"\\";
      }
      else
      {
        String backupPathDriveLetter = returnHost().replace("jdbc:derby:", "").substring(0,1);
        backupPath = backupPathDriveLetter+":\\ -Mirth Backup Export-\\Backup " + getDateTime()+"\\";
      }
      return backupPath;
    }

    public static String getBackupFolder()
    {
      String backupPathGrab = backupPath;
      return backupPathGrab;
    }

    public static String getDateTime()
    {
        //Grabs the date/time and formats for log output
        SimpleDateFormat dateFormat;
        Date currentDate = new Date();

        dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        return dateFormat.format(currentDate);
    }

    public static String deleteBuildingBlockFiles()
    {
      String[] files = {"\\channelCodeTemplatesBackup\\codeTemplates", "\\channelMetadataBackup", "\\fullMirthExport\\globalScripts", "\\fullMirthExport\\configurationFiles" , "\\fullMirthExport\\channelGroups", "\\fullMirthExport\\Alerts"};
      for(int i=0;i<files.length;i++)
      {
        File currentFile = new File(getBackupFolder()+files[i]);
        deleteDirectory(currentFile);
      }
      return "files deleted";
    }

    public static boolean deleteDirectory(File directoryToBeDeleted) 
    {
      File[] allContents = directoryToBeDeleted.listFiles();
      if (allContents != null) 
      {
          for (File file : allContents) 
          {
              deleteDirectory(file);
          }
      }
      return directoryToBeDeleted.delete();
    }

}


