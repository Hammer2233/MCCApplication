//import org.w3c.dom.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class logCommands 
{
    //logs entire active session's text for updating log window in app
    private static ArrayList activeSessionLog = new ArrayList<>();

    //boolean to determine if checkLogFile is necessary
    private static boolean checkLogNeeded = true;
    //Established print writer
    private static PrintWriter logThis;

    public static String checkLogFile() throws FileNotFoundException
    {
        //This will call for the current date and time. The log file will be created if it doesn't exist and a "session start" indicator will print
        String currentDate = getDateTime();
        logThis = new PrintWriter(new FileOutputStream(new File("MCC-BETA.log"), true));
        logThis.print(currentDate+" -Log Established-\n");
        activeSessionLog.add(currentDate+ " -Log Established-\n");
        logThis.flush();

        exportCurrentLogToWindow();
        return "";
    }

    public static String getDateTime()
    {
        //Grabs the date/time and formats for log output
        //currently only one format
        SimpleDateFormat dateFormat;
        Date currentDate = new Date();

        dateFormat = new SimpleDateFormat("yyyy-MM-dd|HH:mm|");

        return dateFormat.format(currentDate);
    }

    public static String returnArchivedChannels(String path)
    {
        //boolean to see if the log file and logThis has been initalized
        if(checkLogNeeded == true)
        {
            try 
            {
                checkLogFile();
                checkLogNeeded = false;
            } 
            catch (FileNotFoundException e) 
            {
                System.out.println("FAILED TO CHECK LOG FILE");
            }
        }

        //checks the backed up channel directory for file names
        File channelDir = new File(path);
        File[] channelDirectory = channelDir.listFiles();
        for(int i=0;i<channelDirectory.length;i++)
        {
            String line = getDateTime() + " CHANNEL BACKUP TAKEN: " + channelDirectory[i]+"\n";
            logThis.print(line);
            activeSessionLog.add(line);
        }
        logThis.print(getDateTime()+ " CHANNEL BACKUP COMPLETE\n");
        activeSessionLog.add(getDateTime()+ " CHANNEL BACKUP COMPLETE\n");
        logThis.flush();
        exportCurrentLogToWindow();
        return "";        
    }

    public static String exportToLog(String given)
    {
        //boolean to see if the log file and logThis has been initalized
        if(checkLogNeeded == true)
        {
            try 
            {
                checkLogFile();
                checkLogNeeded = false;
            } 
            catch (FileNotFoundException e) 
            {
                System.out.println("FAILED TO CHECK LOG FILE");
            }
        }
        logThis.print(getDateTime()+ " " + given + "\n");
        activeSessionLog.add(getDateTime()+ " " + given + "\n");
        logThis.flush();
        applicationWindow.setLogWindow();
        return "";
    }

    public static String exportCurrentLogToWindow()
    {
        //exports current log as string for the application window to process
        String fullLog = "";
        for(int i=0;i<activeSessionLog.size();i++)
        {
            fullLog = fullLog + (activeSessionLog.get(i));
        }
        return fullLog;
    }
}
