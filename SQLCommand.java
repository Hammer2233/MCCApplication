import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;


public class SQLCommand 
{
    static String backupFolderPath ="";

    public static String checkUN(String host)
    {
        backupFolderPath = Main.getBackupFolder();
        String currentUn = "";
        try(Connection conn = DriverManager.getConnection(host); Statement stmt = conn.createStatement())
        {
            String query = "SELECT * FROM PERSON";
            ResultSet rs = stmt.executeQuery(query);

            try
            {
                while (rs.next()) 
                {                 
                    //column containing Username
                    String XMLdata = rs.getString(2);
                    currentUn = XMLdata;
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

    public static String resetUsernamePassword(String host)
    {
        int queryCount = 0;

        while(queryCount <= 1)
        {
            try(Connection conn = DriverManager.getConnection(host); Statement stmt = conn.createStatement())
            {

                if(queryCount == 0)
                {
                    String query = "UPDATE PERSON set USERNAME = 'admin' where ID = 1";
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
                    String query = "UPDATE PERSON_PASSWORD set PASSWORD = 'YzKZIAnbQ5m+3llggrZvNtf5fg69yX7pAplfYg0Dngn/fESH93OktQ==' WHERE PERSON_ID = 1";
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
    
    public static String changeUNandPWCMD(String host)
    {
        int queryCount = 0;

        while(queryCount <= 1)
        {
            try(Connection conn = DriverManager.getConnection(host); Statement stmt = conn.createStatement())
            {

                if(queryCount == 0)
                {
                    String query = "UPDATE PERSON set USERNAME = 'labdaq' where ID = 1";
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
                    String query = "UPDATE PERSON_PASSWORD set PASSWORD = 'sPPaxXTtAA7M1tbOy7Ied7spHufmXpU6W5ER/TT2DSY/DjIkv+UEDQ==' WHERE PERSON_ID = 1";
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
