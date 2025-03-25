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

                if(queryCount == 0)
                {
                    String query = "UPDATE PERSON set USERNAME = 'admin' where ID = "+usernameToChange;
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
                else if(queryCount == 1)
                {
                    String query = "UPDATE PERSON_PASSWORD set PASSWORD = 'YzKZIAnbQ5m+3llggrZvNtf5fg69yX7pAplfYg0Dngn/fESH93OktQ==' WHERE PERSON_ID = "+usernameToChange;
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
            }
            catch (SQLException e) 
            {
                System.out.println("Encountered SQL Error");
                if(queryCount == 0)
                {
                    logCommands.exportToLog("Credential update FAILED");
                    logCommands.exportToLog("CURRENT OPERATION ENCOUNTERED A SQL ERROR - No changes were made to the Mirth username");
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

                if(queryCount == 0)
                {
                    String query = "UPDATE PERSON set USERNAME = 'labdaq' where ID = "+targetUsername;
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
                else if(queryCount == 1)
                {
                    String query = "UPDATE PERSON_PASSWORD set PASSWORD = 'sPPaxXTtAA7M1tbOy7Ied7spHufmXpU6W5ER/TT2DSY/DjIkv+UEDQ==' WHERE PERSON_ID = "+targetUsername;
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
            }
            catch (SQLException e) 
            {
                System.out.println("Encountered SQL Error");
                if(queryCount == 0)
                {
                    logCommands.exportToLog("Credential update FAILED");
                    logCommands.exportToLog("CURRENT OPERATION ENCOUNTERED A SQL ERROR - No changes were made to the Mirth username");
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
}
