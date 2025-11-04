package main;
import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
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
	private static boolean wasPropertiesFileRead = false;

    public static void main(String args[]) throws ClassNotFoundException, SQLException, FileNotFoundException
    {
    	String driver = "org.apache.derby.jdbc.EmbeddedDriver";
    	Class.forName(driver);

    	//calls connection to DB and applies it to the host for SQL queries
    	determineDBLocation();
    	String host = fullDBPath;
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
    	
    	/**
    	 * Changed in Mirth 2.2.4 to make the following checks
    	 * 1. See if appdata folder contains the mirthdb folder
    	 * 2. If not, then remove appdata from the dbpath and check if the main db folder houses the mirthdb folder
    	 */  
    	
    	String[] filesToCheck = { dbPath+"appdata", dbPath+"appdata\\mirthdb" };
    	
    	//This following section of code determines the subfolder for the DB filepath
    	//It checks to see if the "appdata" folder exists. If not it connects right to "mirthdb"
    	for(int check=0;check<filesToCheck.length;check++)
    	{
    		File dbSubPath = new File(filesToCheck[check]);
        	if(dbSubPath.exists())
        	{
        		check++;
        		File appdataCheck = new File(filesToCheck[check]);
        		if(appdataCheck.exists())
            	{
        			System.out.println("Mirth DB is housed in the appdata folder");
            		dbSubfolder = "appdata\\mirthdb;";
            		fullDBPath = fullDBPath + dbPath + dbSubfolder;
            	}
            	else
            	{
            		System.out.println("Mirth DB NOT housed in the appdata folder");
            		dbSubfolder = "mirthdb";
            		fullDBPath = fullDBPath + dbPath + dbSubfolder;
            	}
        	}
        	else
        	{
        		System.out.println("Mirth DB NOT housed in the appdata folder");
        		dbSubfolder = "mirthdb";
        		fullDBPath = fullDBPath + dbPath + dbSubfolder;        		
        	}
        	check=2;
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
    		if(currentFile.exists())
    		{
        		deleteDirectory(currentFile);
    		}
    	}

    	// Create the directory object for the fullMirthExport
        File fullMirthExportDir = new File(getBackupFolder() + "\\fullMirthExport");

        // Check if the directory exists
        if (fullMirthExportDir.exists() && fullMirthExportDir.isDirectory())
        {
            // Check if the list is not null and has zero length
            String[] contents = fullMirthExportDir.list();
            if (contents != null && contents.length == 0)
            {
                deleteDirectory(fullMirthExportDir);
            }
        }
        
//    	if(new File(getBackupFolder()+"\\fullMirthExport").list().length < 1)
//    	{
//    		deleteDirectory(new File(getBackupFolder()+"\\fullMirthExport"));
//    	}
//    	else
//    	{
//
//    	}
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
    
    public static String repairDatabaseLog(String path)
    {
    	ArrayList logFiles = new ArrayList();
    	logFiles.clear();
    	File dir = new File(path);
    	
    	//Copies out names of each file
    	File[] logFileDirectory = dir.listFiles();
        String currentLogFile = "";
        for(int b=0;b<logFileDirectory.length;b++)
        {
        	logFiles.add(logFileDirectory[b].getName());
        }
        
        //renames the log folder
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy-HH-mm-ss-SSS");
        Date currentDate = new Date();
        String newlogFolder = path + "_BACKUP_" + dateFormat.format(currentDate);
        
        File oldDir = new File(path);
        File newDir = new File(newlogFolder);
        boolean wasRenamed = false;
        try 
        {
        	Files.move(dir.toPath(), newDir.toPath(), new java.nio.file.CopyOption[0]);
        	logCommands.exportToLog("Renamed log folder to: '" + newlogFolder + "'");
        	wasRenamed = true;
        } 
        catch (IOException ex) 
        {
        	logCommands.exportToLog("Failed to rename log folder. Error: " + ex.toString());
        	if(ex.toString().contains("AccessDeniedException"))
        	{
        		logCommands.exportToLog("'AccessDeniedException' is often thrown with permission errors. Please run MCC.jar via the batch file and try again");
        	}
        }
        
        if(wasRenamed == true)
        {
        	System.out.println("BEGINNING COPY SEQUENCE");
        	//recreates the log folder
            dir.mkdirs();
        	
            /* logic updated in MCC 2.2.4
             * Found out that if you copy all of the files in the archived log folder, the database remains corrupt
             * You need to only copy out the "log.ctrl", "log1.dat", "logmirror.ctrl", "README_DO_NOT_TOUCH_FILES.txt"
             * Those files will be remade when the Mirth services starts or the DB is queried again
             * */
           
            //copies all necessary files in the backup log folder to the new folder
            for(int i=0;i<logFiles.size();i++)
            {
            	String[] manualLogFiles = { "log.ctrl", "log1.dat", "logmirror.ctrl", "README_DO_NOT_TOUCH_FILES.txt" };
            	for(int checkLogs=0;checkLogs<manualLogFiles.length;checkLogs++)
            	{            		
            		if(manualLogFiles[checkLogs].toString().equals(logFiles.get(i).toString()))
            		{
            			System.out.println("Copying this log file: " + logFiles.get(i));
                    	File source = new File(newlogFolder+"\\"+logFiles.get(i));
                    	File destination = new File(path+"\\"+logFiles.get(i));
                    	
                    	System.out.println("SOURCE: " + source);
                    	System.out.println("DESTINATION: " + destination);
                    	
                    	try
                    	{
                    		Files.copy(source.toPath(), destination.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    		logCommands.exportToLog("FILE- '" + logFiles.get(i) + "' COPIED TO '" + destination + "'");
                    	} 
                    	catch (IOException e) 
                    	{
                    		logCommands.exportToLog("ERROR: UNABLE TO COPY '" + logFiles.get(i) + "'");
                    	    e.printStackTrace();
                    	}  
            		}
            	}            	          	
            }
            logCommands.exportToLog("DATABASE REPAIR FINISHED");
        }       
    	
    	return "repair ran";
    }
    
    public static String repairKeystoreFile(String path)
    {
    	File dir = new File(path+"\\keystore.jks");
    	//renames the keystore file    	
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy-HH-mm-ss-SSS");
        Date currentDate = new Date();
        String oldKeystoreFile = path + "\\keystore_BACKUP_" + dateFormat.format(currentDate) + ".jks";
        
        File oldDir = new File(path+"\\keystore.jks");
        File newDir = new File(oldKeystoreFile);
        boolean wasRenamed = false;
        try 
        {
        	Files.move(dir.toPath(), newDir.toPath(), new java.nio.file.CopyOption[0]);
        	logCommands.exportToLog("Renamed Keystore to: '" + oldKeystoreFile + "'");
        	wasRenamed = true;
        } 
        catch (IOException ex) 
        {
        	logCommands.exportToLog("Failed to rename Keystore file. Error: " + ex.toString());
        	if(ex.toString().contains("AccessDeniedException"))
        	{
        		logCommands.exportToLog("'AccessDeniedException' is often thrown with permission errors. Please run MCC.jar via the batch file and try again");
        	}
        }
        
    	return "Keystore repair ran";
    }
    
    public static boolean changedDirTF()
    {
    	return changedMirthDBDirPath;
    }
    
    public static String configPathReader()
    {
    	//added in 2.2.6
    	//This method is called if the Mirth DB isn't found automatically. The Mirth DB path will be read from the mirth.properties file (if it exists)
    	String host = returnHost();
    	
    	String propertiesFilePath = host.trim().replace("jdbc:derby:", "").replace(";", "").replace("mirthdb", "").replace("appdata", "") + "conf\\mirth.properties";        
        File propertiesFile = new File(propertiesFilePath);
        
        if(wasPropertiesFileRead != true)
        {
        	if(propertiesFile.exists())
            {
            	System.out.println("mirth.properties file exists. Attemtping to read");
            	logCommands.exportToLog("mirth.properties file exists. Attemtping to read");
            	try (BufferedReader reader = Files.newBufferedReader(Paths.get(propertiesFilePath)))
    		    {
    		        String line;
    		        while ((line = reader.readLine()) != null) 
    		        {
    		        	if(line.contains("dir.appdata ="))
    		        	{
    		        		String capturedDBPath = line.replace("dir.appdata = ", "").trim();
    		        		changedMirthDBDirPath = true;
    		        		changedMDBDirPath = capturedDBPath+"\\\\";
    		        		System.out.println("Read DB Path: " + changedMDBDirPath);
    		        		logCommands.exportToLog("DB path '" + changedMDBDirPath + "mirthdb' read from the mirth.properties file");
    		        		logCommands.exportToLog("Please attempt to run the last operation again");
    		        	}
    		        }
    		        reader.close();
    		    } 
    		    catch (IOException e) 
    		    {
    		        e.printStackTrace();
    		    }
            	wasPropertiesFileRead = true;
            }
        	else
        	{
        		System.out.println("No mirth.properties file was found. Search for the Mirth DB manually");
        		logCommands.exportToLog("No mirth.properties file was found. Please search for the Mirth DB manually");
        	}
        }
        else
        {
        	System.out.println("This function was previously called. The mirth db path in the mirth.properties folder could not be accessed. Please manually search for the Mirth DB");
        }        
    	
    	return "config read";
    }

}