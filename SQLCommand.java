import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Scanner;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class SQLCommand 
{
    static String backupFolderPath ="";
    public static String checkUN(String host)
    {
        backupFolderPath = Main.getBackupFolder();
        String currentUn = "";
        //exports the channels (standard Merby export)
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
}
