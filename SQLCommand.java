package main;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;


public class SQLCommand 
{
    static String backupFolderPath ="";
    private static ArrayList usernameList = new ArrayList();
    private static String mirthConfigUN = "";
    private static String mirthConfigPW = "";
    
    private static ArrayList allChannelNames = new ArrayList();
    private static ArrayList allTotalBytes = new ArrayList();
    private static ArrayList allTableNames = new ArrayList();
    private static ArrayList totalBytesPerChannel = new ArrayList();
    
    private static ArrayList channelIDList = new ArrayList();
    private static ArrayList channelStatusList = new ArrayList();
    private static ArrayList channelNameList = new ArrayList();
    
    private static ArrayList channelName = new ArrayList();
    private static ArrayList channelXML = new ArrayList();

    public static String checkUN(String host)
    {
        backupFolderPath = Main.getBackupFolder();
        String currentUn = "";
        try(Connection conn = DriverManager.getConnection(host); Statement stmt = conn.createStatement())
        {
        	usernameList.clear();
            String query = "SELECT * FROM PERSON";
            ResultSet rs = stmt.executeQuery(query);

            try
            {
                while (rs.next()) 
                {                 
                    //column containing Username
                    String XMLdata = rs.getString(2);
                    currentUn = XMLdata;
                    usernameList.add(currentUn);
                    System.out.println("currentUsername:" + currentUn);                
                }
            } 
            catch (SQLException sqlExcept) 
            {
                System.out.println("FAILED MISERABLY");
                System.out.println(sqlExcept);
            }
            conn.close();
        }
        catch (Exception e) 
        {
            e.printStackTrace();
        }
        
        if(currentUn == "")
        {
            currentUn = "NONE FOUND";
            return currentUn;
        }
        else
        {
            return currentUn;
        }
    }
    
    public static int unArraySize()
    {
    	int size = usernameList.size();
    	System.out.println("Username Array Size: " + size);
    	return size;
    }
    
    public static String readUsernameArraylist(int currentCall)
    {
    	String returnMe = usernameList.get(currentCall).toString();
    	return returnMe;
    }
    
    public static ArrayList returnArraylist()
    {
    	return usernameList;
    }

    public static String resetUsernamePassword(String host, int usernameToChange)
    {
        int queryCount = 0;
        String chosenUn = readUsernameArraylist(usernameToChange-1).toString();
        logCommands.exportToLog("Username selected to reset credentials: " + chosenUn);

        while(queryCount <= 1)
        {
            try(Connection conn = DriverManager.getConnection(host); Statement stmt = conn.createStatement())
            {
            	//Changed logic in 2.2.1 to update password first, then changing username
                if(queryCount == 0)
                {                	
                	String query = "UPDATE PERSON_PASSWORD set PASSWORD = 'YzKZIAnbQ5m+3llggrZvNtf5fg69yX7pAplfYg0Dngn/fESH93OktQ==' WHERE PERSON_ID = "+usernameToChange;
                	//String query = "UPDATE PERSON_PASSWORD set PASSWORD = 'YzKZIAnbQ5m+3llggrZvNtf5fg69yX7pAplfYg0Dngn/fESH93OktQ==' WHERE USERNAME = '" + chosenUn + "'";
                    int modifiedRows = stmt.executeUpdate(query);
                    if(modifiedRows > 0)
                    {
                        logCommands.exportToLog("Password update SUCCESSFUL");
                    }
                    else
                    {
                        logCommands.exportToLog("Password update FAILED");
                    }
                    
                }
                else if(queryCount == 1)
                {
                	String query = "UPDATE PERSON set USERNAME = 'admin' where ID = "+usernameToChange;
                	//String query = "UPDATE PERSON set USERNAME = 'admin' where USERNAME = '" + chosenUn + "'";
                    int modifiedRows = stmt.executeUpdate(query);
                    if(modifiedRows > 0)
                    {
                        logCommands.exportToLog("Username update SUCCESSFUL");
                    }
                    else
                    {
                        logCommands.exportToLog("Username update FAILED");
                    } 	                    
                }
            }
            catch (SQLException e) 
            {
                System.out.println("Encountered SQL Error");
                if(queryCount == 0)
                {
                    logCommands.exportToLog("Credential update FAILED");
                    logCommands.exportToLog("CURRENT OPERATION ENCOUNTERED A SQL ERROR - No changes were made to the Mirth username");
                    logCommands.exportToLog("ERROR : " + e.toString());
                }
                else
                {
                    logCommands.exportToLog("CURRENT OPERATION ENCOUNTERED A SQL ERROR - No changes were made to the Mirth user's password");
                    logCommands.exportToLog("ERROR : " + e.toString());
                    if(e.toString().contains("read-only connection"))
                    {
                    	logCommands.exportToLog("Tip: 'read-only' errors are likely permission based. Ensure the program has been ran as admin via the batch file");
                    }
                }
                
            }
            queryCount++;
        }
        return "reset un and pw";
    }
    
    public static String changeUNandPWCMD(String host, int targetUsername)
    {
        int queryCount = 0;
        String chosenUn = readUsernameArraylist(targetUsername-1).toString();
        logCommands.exportToLog("Username selected to reset credentials: " + chosenUn);

        while(queryCount <= 1)
        {
            try(Connection conn = DriverManager.getConnection(host); Statement stmt = conn.createStatement())
            {
            	//Changed logic in 2.2.1 to update password first, then changing username
                if(queryCount == 0)
                {
                	String query = "UPDATE PERSON_PASSWORD set PASSWORD = 'sPPaxXTtAA7M1tbOy7Ied7spHufmXpU6W5ER/TT2DSY/DjIkv+UEDQ==' WHERE PERSON_ID = "+targetUsername;
                	//String query = "UPDATE PERSON_PASSWORD set PASSWORD = 'sPPaxXTtAA7M1tbOy7Ied7spHufmXpU6W5ER/TT2DSY/DjIkv+UEDQ==' WHERE USERNAME = '" + chosenUn + "'";
                	System.out.println("Cred Query: " + query);
                    int modifiedRows = stmt.executeUpdate(query);
                    if(modifiedRows > 0)
                    {
                        logCommands.exportToLog("Password update SUCCESSFUL - 2025 Mirth Password");
                    }
                    else
                    {
                        logCommands.exportToLog("Password update FAILED");
                    }
                }
                else if(queryCount == 1)
                {
                	String query = "UPDATE PERSON set USERNAME = 'labdaq' where ID = "+targetUsername;
                	//String query = "UPDATE PERSON set USERNAME = 'labdaq' where USERNAME = '" + chosenUn + "'";
                    System.out.println("Username Query: " + query);
                    int modifiedRows = stmt.executeUpdate(query);
                    if(modifiedRows > 0)
                    {
                        logCommands.exportToLog("Username update SUCCESSFUL - 'labdaq' username");
                    }
                    else
                    {
                        logCommands.exportToLog("Username update FAILED");
                    }                    
                }
            }
            catch (SQLException e) 
            {
                System.out.println("Encountered SQL Error");
                if(queryCount == 0)
                {
                    logCommands.exportToLog("Credential update FAILED");
                    logCommands.exportToLog("CURRENT OPERATION ENCOUNTERED A SQL ERROR - No changes were made to the Mirth username");
                    logCommands.exportToLog("ERROR : " + e.toString());
                }
                else
                {
                    logCommands.exportToLog("CURRENT OPERATION ENCOUNTERED A SQL ERROR - No changes were made to the Mirth user's password");
                    logCommands.exportToLog("ERROR : " + e.toString());
                }
                
            }
            queryCount++;
        }
        return "reset un and pw";   
    }
    
    public static String returnDBPathFromConfig()
    {
    	String configPath = "";
    	String dbConnection = "";
        try 
        {
            Runtime rt = Runtime.getRuntime();
            Process pr = rt.exec("sc qc \"Mirth Connect Service\"");
            BufferedReader input = new BufferedReader(new InputStreamReader(pr.getInputStream()));
            do 
            {
            	configPath = input.readLine();
            } 
            while (!configPath.contains("BINARY_PATH_NAME") && !configPath.contains("FAILED"));
        } 
        catch (IOException runtimeError) 
        {
            System.out.println(runtimeError);
        } 

        if (configPath.contains("BINARY_PATH_NAME")) 
        {
            //creates path substring components
            int pos1 = configPath.indexOf(":\\") - 1;
            int pos2 = configPath.indexOf("\\mirthc") + 1;
            if (pos2 < 1)
            {
                pos2 = configPath.indexOf("\\mcserv") + 1;
            }
            if (pos1 > 0 && pos2 > 0) 
            {
            	configPath = configPath.substring(pos1, pos2);
            } 
            else 
            {
                System.out.println("Error in finding path");
                configPath = "";
            } 
        } 
        else 
        {
        	configPath = "";
        }
          
        //This following section of code sets the subfolder for the conf filepath
        File confSubPath = new File(configPath+"conf");
        if(confSubPath.exists())
        {
        	configPath = configPath+"conf";
        	//This searches the conf directory for the 'mirth.properties' file and grabs the db connection
        	File[] confDir = confSubPath.listFiles();
        	for (File current : confDir)
            {
                if (current.getName().equals("mirth.properties"))
                {
                    System.out.println("FOUND mirth.properties FILE");
                    try (BufferedReader br = new BufferedReader(new FileReader(current))) 
                    {
                        String line;
                        while ((line = br.readLine()) != null) 
                        {
                        	//grabs dbPath, username, and password
                            if(line.contains("database.url"))
                            {
                            	dbConnection = line.replace("database.url = ", "");
                            	System.out.println("dbConnection From Conf: " + dbConnection);
                            }
                            if(line.contains("database.username"))
                            {
                            	mirthConfigUN = line.replace("database.username =", "");
                            }
                            if(line.contains("database.password"))
                            {
                            	mirthConfigPW = line.replace("database.password =", "");
                            }
                        }
                    } 
                    catch (IOException e) 
                    {
                        e.printStackTrace();
                    }
                }
            }
        }
        else
        {
            System.out.println("CONF folder not found");
        }        
        return dbConnection;
    }
    
    public static String queryTableSpace(String host)
    {
    	try(Connection conn = DriverManager.getConnection(host, mirthConfigUN, mirthConfigPW); Statement stmt = conn.createStatement())    	
        {
            String query = "SELECT NAME AS CHANNEL_NAME\r\n"
            		+ "	, TOTAL_BYTES\r\n"
            		+ "	, TABLE_NAME\r\n"
            		+ "FROM (\r\n"
            		+ "	WITH RECURSIVE PG_INHERIT(INHRELID, INHPARENT) AS (\r\n"
            		+ "			SELECT INHRELID, INHPARENT\r\n"
            		+ "			FROM PG_INHERITS\r\n"
            		+ "			UNION\r\n"
            		+ "			SELECT CHILD.INHRELID, PARENT.INHPARENT\r\n"
            		+ "			FROM PG_INHERIT CHILD, PG_INHERITS PARENT\r\n"
            		+ "			WHERE CHILD.INHPARENT = PARENT.INHRELID\r\n"
            		+ "			)\r\n"
            		+ "		, PG_INHERIT_SHORT AS (\r\n"
            		+ "			SELECT *\r\n"
            		+ "			FROM PG_INHERIT\r\n"
            		+ "			WHERE INHPARENT NOT IN (SELECT INHRELID FROM PG_INHERIT)\r\n"
            		+ "			)\r\n"
            		+ "	SELECT TABLE_SCHEMA\r\n"
            		+ "		, TABLE_NAME\r\n"
            		+ "		, NULLIF(REGEXP_REPLACE(TABLE_NAME, '\\D', '', 'g'), '')::NUMERIC AS MIRTH_ID\r\n"
            		+ "		, SUBSTRING(TABLE_NAME FROM 'd_(\\D+)\\d+') AS MIRTH_TYPE\r\n"
            		+ "		, ROW_ESTIMATE\r\n"
            		+ "		, PG_SIZE_PRETTY(TOTAL_BYTES) AS TOTAL_BYTES\r\n"
            		+ "		, PG_SIZE_PRETTY(INDEX_BYTES) AS INDEX_BYTES\r\n"
            		+ "		, PG_SIZE_PRETTY(TOAST_BYTES) AS TOAST_BYTES\r\n"
            		+ "		, PG_SIZE_PRETTY(TABLE_BYTES) AS TABLE_BYTES\r\n"
            		+ "	FROM (\r\n"
            		+ "		SELECT *, TOTAL_BYTES - INDEX_BYTES - COALESCE(TOAST_BYTES, 0) AS TABLE_BYTES\r\n"
            		+ "		FROM (\r\n"
            		+ "			SELECT C.OID\r\n"
            		+ "				, NSPNAME AS TABLE_SCHEMA\r\n"
            		+ "				, RELNAME AS TABLE_NAME\r\n"
            		+ "				, SUM(C.RELTUPLES) OVER (PARTITION BY PARENT) AS ROW_ESTIMATE\r\n"
            		+ "				, SUM(PG_TOTAL_RELATION_SIZE(C.OID)) OVER (PARTITION BY PARENT) AS TOTAL_BYTES\r\n"
            		+ "				, SUM(PG_INDEXES_SIZE(C.OID)) OVER (PARTITION BY PARENT) AS INDEX_BYTES\r\n"
            		+ "				, SUM(PG_TOTAL_RELATION_SIZE(RELTOASTRELID)) OVER (PARTITION BY PARENT) AS TOAST_BYTES\r\n"
            		+ "				, PARENT\r\n"
            		+ "			FROM (\r\n"
            		+ "				SELECT PG_CLASS.OID\r\n"
            		+ "					, RELTUPLES\r\n"
            		+ "					, RELNAME\r\n"
            		+ "					, RELNAMESPACE\r\n"
            		+ "					, PG_CLASS.RELTOASTRELID\r\n"
            		+ "					, COALESCE(INHPARENT, PG_CLASS.OID) PARENT\r\n"
            		+ "				FROM PG_CLASS\r\n"
            		+ "				LEFT JOIN PG_INHERIT_SHORT ON INHRELID = OID\r\n"
            		+ "				WHERE RELKIND IN ('r', 'p')\r\n"
            		+ "				) C\r\n"
            		+ "			LEFT JOIN PG_NAMESPACE N ON N.OID = C.RELNAMESPACE\r\n"
            		+ "			ORDER BY TOTAL_BYTES DESC\r\n"
            		+ "			) A\r\n"
            		+ "		WHERE OID = PARENT\r\n"
            		+ "		) A\r\n"
            		+ "	) TABLE_SIZES\r\n"
            		+ "LEFT JOIN D_CHANNELS ON D_CHANNELS.LOCAL_CHANNEL_ID = TABLE_SIZES.MIRTH_ID\r\n"
            		+ "LEFT JOIN CHANNEL ON CHANNEL.ID = D_CHANNELS.CHANNEL_ID\r\n"
            		+ "LEFT JOIN (\r\n"
            		+ "	SELECT (XPATH('string/text()', ENTRY)) [1]::TEXT AS CID\r\n"
            		+ "		, (XPATH('com.mirth.connect.model.ChannelMetadata/enabled/text()', ENTRY)) [1]::TEXT::boolean AS PRUNE_ENABLED\r\n"
            		+ "		, (XPATH('com.mirth.connect.model.ChannelMetadata/pruningSettings/pruneMetaDataDays/text()', ENTRY)) [1]::TEXT::INT AS PRUNE_DAYS\r\n"
            		+ "		, (XPATH('com.mirth.connect.model.ChannelMetadata/pruningSettings/archiveEnabled/text()', ENTRY)) [1]::TEXT::boolean AS ARCHIVE_ENABLED\r\n"
            		+ "	FROM (\r\n"
            		+ "		SELECT UNNEST(XPATH('/map/entry', VALUE::XML)) AS ENTRY\r\n"
            		+ "		FROM CONFIGURATION\r\n"
            		+ "		WHERE CATEGORY = 'core' AND NAME = 'channelMetadata'\r\n"
            		+ "		) X\r\n"
            		+ "	) AS M ON M.CID = D_CHANNELS.CHANNEL_ID;";
            ResultSet rs = stmt.executeQuery(query);
            try
            {
                while (rs.next()) 
                {   
                	String channelName = rs.getString(1);                	
                	if(channelName != null)
                	{               		
                		allChannelNames.add(channelName);
                    	String totalBytes = rs.getString(2);
                    	allTotalBytes.add(totalBytes);
                    	String tableName = rs.getString(3);
                    	allTableNames.add(tableName); 
                   	}                	               	          
                }
            } 
            catch (SQLException sqlExcept) 
            {
                System.out.println("FAILED MISERABLY");
                System.out.println(sqlExcept);
            }
            conn.close();
        }
        catch (SQLException e) 
        {
            System.out.println("Encountered SQL Error");
            System.out.println("ERROR: " + e);
            logCommands.exportToLog("ERROR: " + e);
            logCommands.exportToLog("Ensure you select the correct database when running the command");
        }
    	return "Space Checked";
    }
    
    public static int channelSizeArraySize()
    {
    	int arraySize = allChannelNames.size();
    	System.out.println(arraySize + " channel sizes queried");
    	return arraySize;
    }
    
    public static String returnQueryRow(int rowNum)
    {
    	//variables and formatting the current string
    	String doctoredRow = "";
    	
    	int currChannelLength = 35;
    	int currByteLength = 35;
    	int currTableLength = 35;
    	
    	String currByte = allTotalBytes.get(rowNum).toString();
    	if(currByte.contains("bytes"))
    	{
    		currByte = currByte.replace(" bytes", "");
    		int convertedByte = Integer.valueOf(currByte)/1000;
    		currByte = String.valueOf(convertedByte) + " kB";
    	}
    	
    	String currChannel = String.format("%-" + currChannelLength + "s", allChannelNames.get(rowNum).toString());
    	currByte = String.format("%-" + currByteLength + "s", currByte);
    	String currTable = String.format("%-" + currTableLength + "s", allTableNames.get(rowNum).toString());
    	
    	doctoredRow = currChannel + currByte + currTable;    	
    	return doctoredRow;
    }
    
    public static String databaseInformationCall(String host)
    {
    	channelIDList.clear();
		channelStatusList.clear();
		channelNameList.clear();
    	String dbInformationText = "Mirth Database Version: ";
    	try(Connection conn = DriverManager.getConnection(host); Statement stmt = conn.createStatement())    	
        {
    		String[] commands = { "SELECT COUNT(*) AS TotalRows FROM CHANNEL", "SELECT NAME FROM CHANNEL ORDER BY NAME" };
    		String mirthVersion = fullConfigExport.getMirthVersion(host);
    		mirthVersion.replace("\"", "");
    		dbInformationText = dbInformationText + mirthVersion + "\nMirth DB Path: " + host.replace("jdbc:derby:", "").replace(";", "")+"\n";
    		String serviceCheck = Main.checkMirthService();
    		if(!serviceCheck.equals("FAILED"))
    		{
    			dbInformationText += ("Service State: " + serviceCheck + "\n"); 
    		}
    		else
    		{
    			dbInformationText += ("Service State: FAILED TO FIND SERVICE\n"); 
    		}
    		
    		
    		//gets size of the appdata and MirthDB folder
    		File directory = new File(host.replace("jdbc:derby:", "").replace("\\mirthdb;", ""));
    		long appdataSize = getFolderSize(directory);
    		dbInformationText = dbInformationText + "'appdata' folder size: " + appdataSize/1000.0 + "kb / " + appdataSize/1000000.0 + "mb / " + appdataSize/1000000000.0 + "gb\n\n";
    		
            String query = "";
            int noOfRanQueries = 0;
            
            for(int i=0;i<commands.length;i++)
            {
            	//creates the version and channel section
            	while (noOfRanQueries <= 1)
            	{
            		System.out.println("Queries Ran: " + noOfRanQueries);
            		query = commands[noOfRanQueries];
            		ResultSet rs = stmt.executeQuery(query);
            		    
            		try
                    {
                        while (rs.next()) 
                        {   
                        	if(noOfRanQueries == 0)
                    		{
                        		String channelNo = rs.getString(1);   
                        		if(channelNo != null)
                            	{               		
                            		if(channelNo == "1")
                            		{
                            			dbInformationText = dbInformationText + channelNo + " Channel: \n============\n";
                            		}
                            		else
                            		{
                            			dbInformationText = dbInformationText + channelNo + " Channels: \n============\n";
                            		}
                               	} 
                    		}
                    		else
                    		{
                    			String channelName = rs.getString(1);
                    			if(serviceCheck == "STOPPED")
                    			{
                    				//adds the information outside of this loop
                    			}
                    			else
                    			{
                        			dbInformationText += channelName + "\n";
                    			}
                    		}
                        }
                        noOfRanQueries++;
                        i=noOfRanQueries;
                    } 
                    catch (SQLException sqlExcept) 
                    {
                        System.out.println("FAILED MISERABLY");
                        System.out.println(sqlExcept);
                    }
            	}
            	
            	if(serviceCheck == "STOPPED")
    			{
            		//evaluates Mirth version to account for pre Mirth 3.5.0 channel generation
                	String currMirthVersion = fullConfigExport.getMirthVersion(host).replace("\"", "");;
                	String[] splitVersion = currMirthVersion.split("\\.");
                	
            		if(Integer.parseInt(splitVersion[0]) < 4 && Integer.parseInt(splitVersion[1]) < 5 || Integer.parseInt(splitVersion[0]) < 3)
                	{
                		System.out.println("Mirth database version is less than 3.5");
                		if(channelExport.getForceNewChannelGeneration() == true)
                		{
                			//generated if Mirth version is <3.5 and choice to use the new generation was chosen under MORE FUNCTIONS
                    		channelStatusBuilder(host);
            				for(int e=0;e<channelNameList.size();e++)
            				{
                				dbInformationText += channelNameList.get(e) + " - " + channelStatusList.get(e) + "\n";
            				}
                		}
                		else
                		{
                			//generation for Mirth versions <3.5
                			channelStatusBuilderOLD(host);
                    		System.out.println("channelNameList size: " + channelNameList.size());
            				for(int e=0;e<channelNameList.size();e++)
            				{
                				dbInformationText += channelNameList.get(e) + " - " + channelStatusList.get(e) + "\n";
            				}
                		}    		
                	}
                	else
                	{
                		//generated for Mirth versions >=3.5
                		channelStatusBuilder(host);
        				for(int e=0;e<channelNameList.size();e++)
        				{
            				dbInformationText += channelNameList.get(e) + " - " + channelStatusList.get(e) + "\n";
        				}
                	}  
    			}            	
            	
            	//User list
            	checkUN(host);
            	if(noOfRanQueries == 2)
            	{
            		int usernameCount = unArraySize();
            		if(usernameCount >1)
            		{
            			dbInformationText = dbInformationText + "\nUSERS:\n======\n";
            			for(int u=0;u<usernameCount;u++)
            			{
            				dbInformationText = dbInformationText + readUsernameArraylist(u)+ "\n";
            			}
            		}
            		else
            		{
            			dbInformationText = dbInformationText + "\nUSER:\n=====\n" + readUsernameArraylist(usernameCount-1) + "\n";
            		}
            		dbInformationText += "\n";
            	}            	
            }            
            conn.close();
        }
        catch (SQLException e) 
        {
            System.out.println("Encountered SQL Error");
            System.out.println("ERROR: " + e);
            logCommands.exportToLog("ERROR: " + e);
            logCommands.exportToLog("Error running DB information query.");
            dbInformationText = "ENCOUNTERED SQL ERROR\nReport did not generate successfully\n\nTry running with the Mirth Service stopped";
        }
    	return dbInformationText;
    }
    
    private static long getFolderSize(File folder) 
    {
        long length = 0;
        File[] files = folder.listFiles();

        int count = files.length;

        for (int i = 0; i < count; i++) 
        {
            if (files[i].isFile()) 
            {
                length += files[i].length();
            }
            else 
            {
                length += getFolderSize(files[i]);
            }
        }
        return length;
    }
    
    private static String channelStatusBuilder(String host)
    {
    	try(Connection conn = DriverManager.getConnection(host); Statement stmt = conn.createStatement())
        {
    		String[] queries = { "SELECT NAME, VALUE FROM CONFIGURATION WHERE NAME LIKE '%channelMetadata%'", "SELECT ID, NAME FROM CHANNEL ORDER BY ID" };
            
            for(int n=0;n<queries.length;n++)
            {
            	ResultSet rs = stmt.executeQuery(queries[n]);
            	if(n==0)
            	{
            		try
                    {
            			String cmdXML = "";
                        while (rs.next()) 
                        {                 
                            //adds channel id and status to arrays
                            cmdXML = rs.getString(2);                                      
                        }
                        String[] splitCMD = cmdXML.split("\n");
                        for(int c=0;c<splitCMD.length;c++)
                        {
                        	String line = splitCMD[c];
                        	if(line.contains("string"))
                        	{
                        		channelIDList.add(line.replace("<string>", "").replace("</string>", "").trim());
                        		//adds the id to the names array. To be replaced when the 2nd query runs
                        		channelNameList.add(line.replace("<string>", "").replace("</string>", "").trim());
                        	}
                        	else if(line.contains("enabled"))
                        	{
                        		String enabledState = line.replace("<enabled>", "").replace("</enabled>", "").trim();
                        		if(enabledState.equals("true"))
                        		{
                        			channelStatusList.add("[ACTIVE]");
                        		}
                        		else
                        		{
                        			channelStatusList.add("[DISABLED]");
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
            	else if (n==1)
            	{
            		try
                    {
                        while (rs.next()) 
                        {                 
                            //adds channel id and status to arrays
                            String channelName = rs.getString(2);     
                            String channelID = rs.getString(1);
                            
                            int currentRowNum = 0;
                            boolean insertedName = false;
                            while(insertedName == false)
                            {
                            	if(channelID.equals(channelIDList.get(currentRowNum)))
                                {
                            		channelNameList.set(currentRowNum, channelName);
                            		insertedName = true;
                                }
                            	currentRowNum++;
                            }
                        }
                    } 
                    catch (SQLException sqlExcept) 
                    {
                        System.out.println("FAILED MISERABLY");
                        System.out.println(sqlExcept);
                    } 
            	}
            	
            }
            conn.close();
        }
        catch (Exception e) 
        {
            e.printStackTrace();
        }
    	return "Arrays Built";
    }
    
    private static String channelStatusBuilderOLD(String host)
    {
    	try(Connection conn = DriverManager.getConnection(host); Statement stmt = conn.createStatement())
        {
    		String query = "SELECT NAME, CHANNEL FROM CHANNEL ORDER BY NAME";
    		
    		ResultSet rs = stmt.executeQuery(query);
    		try
            { 
                while (rs.next()) 
                {      
                	boolean detectedChannelStatus = false;
                	String channelName = "";
        			String channelXML = "";
                	
                    //adds channel name to arrays
                	channelName = rs.getString(1);     
                	channelXML = rs.getString(2);
                	
                	channelNameList.add(channelName);
                	
                    String[] splitChannel = channelXML.split("\n");
                    while(detectedChannelStatus == false)
                    {
                    	for(int c=0;c<splitChannel.length;c++)
                        {
                        	String line = splitChannel[c];
                        	if(line.contains("enabled"))
                        	{
                        		String enabledState = line.replace("<enabled>", "").replace("</enabled>", "").trim();
                        		if(enabledState.equals("true"))
                        		{
                        			channelStatusList.add("[ACTIVE]");
                        		}
                        		else
                        		{
                        			channelStatusList.add("[DISABLED]");
                        		}   
                        		c = (splitChannel.length + 1);
                        		detectedChannelStatus = true;
                        	}
                        } 
                    }
                }                
                conn.close();
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
    	return "Arrays Built";
    }
    
    public static String liteChannelExport(String host)
    {
    	channelName.clear();
        channelXML.clear();
        backupFolderPath = Main.getBackupFolder();
        System.out.println("backupFolderPath: " + backupFolderPath);
        try(Connection conn = DriverManager.getConnection(host); Statement stmt = conn.createStatement())
        {
            String query = "SELECT NAME, CHANNEL FROM CHANNEL";
            ResultSet rs = stmt.executeQuery(query);

            try
            {
                while (rs.next()) 
                {                 
                    //column containing Username
                	channelName.add(rs.getString(1));
                	channelXML.add(rs.getString(2));             
                }
            } 
            catch (SQLException sqlExcept) 
            {
                System.out.println("FAILED MISERABLY");
                System.out.println(sqlExcept);
            }
            conn.close();
        }
        catch (Exception e) 
        {
            e.printStackTrace();
        }
        
        File channelFileDir = new File(backupFolderPath+"channelBackup\\");
        channelFileDir.mkdirs();
    	for(int m=0;m<channelName.size();m++)
    	{
    		File currentCTLFile = new File(backupFolderPath+"channelBackup\\"+channelName.get(m)+".xml");
    		try 
    		{
				currentCTLFile.createNewFile();
			} 
    		catch (IOException e) 
    		{
				e.printStackTrace();
			}
    		try (PrintWriter channelOut = new PrintWriter(currentCTLFile)) 
            {
    			channelOut.println(channelXML.get(m));
    			channelOut.close();
            } 
            catch (FileNotFoundException fileExcept2) 
            {
                System.out.println("Second channel export");
                System.out.println("I DIDN'T FIND THE FILE");
            }
    	}
        
        return "lite export complete";
    }
}
