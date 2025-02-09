import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;

public class Main 
{   
  private static String dbPath;
  private static String fullDBPath;

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
      
      //This following section of code determines the remainder of the address for the DB connection
      //hardcoded currently
      fullDBPath = "jdbc:derby:" + dbPath + "appdata\\mirthdb;";
      System.out.println("Full DB Path: " + fullDBPath);
    }

    //verifies if the Mirth service is running
    public static void checkMirthService()
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
        } 
        else 
        {
          System.out.println("SERVICE STARTED");;
        }
      }
      else
      {
        System.out.println("I AM STOPPED");
      }
      return;
    }

}


