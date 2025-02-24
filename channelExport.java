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

public class channelExport
{
    private static ArrayList extractedCMDArray = new ArrayList();
    //private static ArrayList extractedCMD = new ArrayList<>();

    //Arrays to track channel names, metadata associated with the channels, reordered CMD, all channelXML exports, and channel ids
    private static ArrayList channelNames = new ArrayList<>();
    private static ArrayList channelIDs = new ArrayList<>();
    private static ArrayList extractedCMDArrayREORDERED = new ArrayList<>();
    private static ArrayList masterChannelXML = new ArrayList<>();

    //Arrays to track code template library names, library XML
    //NOTE: This only needs to be ran once as all channels utilize all code template libraries
    private static ArrayList codeTemplateNames = new ArrayList<>();
    private static ArrayList codeTemplateLibraryNames = new ArrayList<>();
    private static ArrayList codeTemplateIDs = new ArrayList<>();
    private static ArrayList completeCodeTemplates = new ArrayList<>();
    private static ArrayList currentCodeTemplateLib = new ArrayList<>();
    private static ArrayList curExtractedCT = new ArrayList<>();
    private static ArrayList allCTLArray = new ArrayList<>();

    //boolean to determine if code templates are included
    private static boolean includeCTLs;
    private static boolean isFullMirthExport = false;

    public static boolean includeCodeTemplates(String yesNo)
    {
      if(yesNo == "YES")
      {
        includeCTLs = true;
        return includeCTLs;
      }
      else
      {
        includeCTLs = false;
        return includeCTLs;
      }
    }

    public static boolean isFullMirthExportCheck(String yesNo)
    {
      if(yesNo == "YES")
      {
        isFullMirthExport = true;
        return isFullMirthExport;
      }
      else
      {
        isFullMirthExport = false;
        return false;
      }
    }

    public static String exportChannels(String host)
    {
        //exports the channels (standard Merby export)
        try(Connection conn = DriverManager.getConnection(host); Statement stmt = conn.createStatement())
        {
            String query = "SELECT * FROM CHANNEL";
            ResultSet rs = stmt.executeQuery(query);
            ResultSetMetaData RSMD = rs.getMetaData();
            int columns = RSMD.getColumnCount();

            //creates folder path if it does not yet exist:
            File dir = new File("C:\\AntekHW\\CALEBMIRTHTESTING\\FullChannelExport\\channelBackup");
            dir.mkdirs();

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
               
                    //column containing CLOB
                    String XMLdata = rs.getString(4);
                    //exports all channel export files to the specified directory
                    try (PrintWriter XMLout = new PrintWriter("C:\\AntekHW\\CALEBMIRTHTESTING\\FullChannelExport\\channelBackup\\" + fileName+".xml")) 
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
        catch (Exception e) 
        {
          e.printStackTrace();
        }
        return "channels exported";
    }


    public static String exportMetadata(String host) throws FileNotFoundException
    {
      //Exports and edits the channelMetadata
      try (Connection conn = DriverManager.getConnection(host); Statement stmt = conn.createStatement()) 
      {
          String query = "SELECT * FROM CONFIGURATION";
          ResultSet rs = stmt.executeQuery(query);
          ResultSetMetaData RSMD = rs.getMetaData();
          int columns = RSMD.getColumnCount();

          File configFilesDir = new File("C:\\AntekHW\\CALEBMIRTHTESTING\\FullChannelExport\\fullMirthExport\\configurationFiles");
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
              // String XMLdata = rs.getString(3);
              String XMLdata = rs.getString(3);

              //exports all CONFIGURATION files to the specified directory
              try (PrintWriter XMLout = new PrintWriter("C:\\AntekHW\\CALEBMIRTHTESTING\\FullChannelExport\\fullMirthExport\\configurationFiles\\" + fileName)) 
              {
                  XMLout.println(XMLdata);
                  XMLout.close();
              } 
              catch (FileNotFoundException fileExcept2) 
              {
                  System.out.println("First channel export");
                  System.out.println("I DIDN'T FIND THE FILE");
              }
            
              if(rs.getString(2).equals("channelMetadata"))
              {
                //Captured Channel ID
                String capturedChannelID = "";

                //creates the path if it doesn't exist
                File dir = new File("C:\\AntekHW\\CALEBMIRTHTESTING\\FullChannelExport\\channelBackup");
                dir.getParentFile().mkdirs();
                dir.mkdirs();
                channelNames.clear(); //clears the current arraylist for channel names
                channelIDs.clear(); //clears the current captured channel IDs

                /**
                 * This loop:
                 * 1. Finds all files in the file backup directory from above
                 * 2. Reads each file and finds the channel ID
                 * 3. Adds the ID to an array for use later when exporting channelMetadata
                 */
                File[] channelDirectory = dir.listFiles();
                String currentReadChannelXML = "";
                for(int b=0;b<channelDirectory.length;b++)
                {
                  channelNames.add(channelDirectory[b].getName());
                }
                for(int c=0;c<channelNames.size();c++)
                {
                  File currentChannelXML = new File("C:\\AntekHW\\CALEBMIRTHTESTING\\FullChannelExport\\channelBackup\\"+channelNames.get(c));
                  try(Scanner channelDirReader = new Scanner(currentChannelXML))
                  {
                    while(channelDirReader.hasNext())
                    {
                      String line = channelDirReader.nextLine();
                      if(line.contains("<id>"))
                      {
                        capturedChannelID = line.replace("<id>","").replace("</id>","").trim();
                        channelIDs.add(capturedChannelID);
                      }
                      currentReadChannelXML += line+"\n";
                    }
                    masterChannelXML.add(currentReadChannelXML);
                    currentReadChannelXML = "";
                    channelDirReader.close();
                  }
                }

                String channelMetadataXML = rs.getString(3);
                String[] splitByRootCMD = channelMetadataXML.split("<entry>");

                /**
                 * This section:
                 * 1. takes the ID of each channel
                 * 2. Finds the metadata associated with the channel
                 * 3. Adds the metadata to an array
                 */
                for (int x=0; x<splitByRootCMD.length;x++)
                {
                  for(int f=0;f<channelIDs.size();f++)
                  {
                    if(splitByRootCMD[x].contains((channelIDs.get(f)).toString()))
                    {
                      //cleans up the found root from channelMetadata
                      String channelMetadataCleanup = splitByRootCMD[x];
                      channelMetadataCleanup.replace("<string>" + channelIDs.get(f) + "</string>","").replace("</entry>","").replace("<com.mirth.connect.model.ChannelMetadata>","").replace("</com.mirth.connect.model.ChannelMetadata>", "");
                      String cmdString = "";
                      String[] splitFoundRoot = channelMetadataCleanup.split("\n");
                      for (int b=0;b<splitFoundRoot.length;b++)
                      {
                        if(!splitFoundRoot[b].matches("com.mirth.connect.model.ChannelMetadata"))
                        {
                          if(!splitFoundRoot[b].contains("</entry>"))
                          {
                            if(!splitFoundRoot[b].matches("string"))
                            {
                              cmdString += splitFoundRoot[b]+"\n";
                            }
                          }
                        }
                      }
                      extractedCMDArray.add(cmdString.trim());
                    }
                  }
                }

                /**
                 * This loop takes the channel ID from channelIDs and compares it to the current extractedCMDArray
                 * Then it will reorder the captured XML in the extractedCMDArray and reorder it to match the names and channel IDs
                 */
                for(int z=0;z<extractedCMDArray.size();z++)
                {
                  int zPlaceholder = 0;
                  String channelIDFromArray = channelIDs.get(z).toString();
                  while(!extractedCMDArray.get(zPlaceholder).toString().contains(channelIDFromArray))
                  {
                    zPlaceholder++;
                  }
                  extractedCMDArrayREORDERED.add(extractedCMDArray.get(zPlaceholder));
                }

                //Reorders extractedCMDArray to match above loop
                for(int q=0;q<extractedCMDArray.size();q++)
                {
                  extractedCMDArray.set(q, extractedCMDArrayREORDERED.get(q));
                }                

                /**
                 * 1. Grabs the data from the extractedCMDArray
                 * 2. Splits the array by newlines
                 * 3. Skips the "string" line
                 * 4. recreates the "cmdString" and adds it back to the extractedCMDArray
                 * 
                 * NOTE: Updated to skip "</map> in some mappings"
                 */
                for(int h=0;h<extractedCMDArray.size();h++)
                {
                    String cmdString = "";
                    String[] splitFoundRoot = extractedCMDArray.get(h).toString().split("\n");
                    for(int d=1;d<splitFoundRoot.length;d++)
                    {
                      if(splitFoundRoot[d].contains("</map>"))
                      { 
                        System.out.println("MAP DETECTED");
                      }
                      else
                      {
                        cmdString += splitFoundRoot[d]+"\n";
                        cmdString.trim();
                      }
                      
                    }
                    extractedCMDArray.set(h,cmdString);
                    splitFoundRoot = new String[splitFoundRoot.length];
                }

                //creates the directory if it does not yet exist:
                File cmdDir = new File("C:\\AntekHW\\CALEBMIRTHTESTING\\FullChannelExport\\channelMetadataBackup\\");
                cmdDir.getParentFile().mkdirs();
                cmdDir.mkdirs();

                //Prints files to the channelMetadata backup directory
                for(int v=0;v<extractedCMDArray.size();v++)
                {
                  try (PrintWriter channelMetadataOut = new PrintWriter("C:\\AntekHW\\CALEBMIRTHTESTING\\FullChannelExport\\channelMetadataBackup\\" + channelIDs.get(v)+".xml"))
                  {
                    channelMetadataOut.print(extractedCMDArray.get(v));
                  }
                  catch (FileNotFoundException fileExcept2) 
                  {
                    System.out.println("I DIDN'T FIND THE channelMetadata FILE");
                  }
                }

                //Reformats the XML for the channelMetadata. Preps for appending to the channel XML
                try 
                {
                  // Step 1: Create a DocumentBuilderFactory and DocumentBuilder
                  DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                  DocumentBuilder builder = factory.newDocumentBuilder();
          
                  // Step 2: Parse the XML file
                  File metadataDir = new File("C:\\AntekHW\\CALEBMIRTHTESTING\\FullChannelExport\\channelMetadataBackup");
                  metadataDir.getParentFile().mkdirs();
                  File[] metadataDirectory = metadataDir.listFiles();
                  for(int m=0;m<metadataDirectory.length;m++)
                  {
                    Document document = builder.parse(new File("C:\\AntekHW\\CALEBMIRTHTESTING\\FullChannelExport\\channelMetadataBackup\\"+metadataDirectory[m].getName()));
          
                    // Step 3: Get the current root element
                    org.w3c.dom.Element oldRoot = document.getDocumentElement();
            
                    // Step 4: Create new root element and metadata element
                    org.w3c.dom.Element newRoot = document.createElement("exportData");
                    org.w3c.dom.Element metadataElement = document.createElement("metadata");
            
                    // Step 5: Move all child nodes from old root to new root
                    NodeList children = oldRoot.getChildNodes();
                    for (int y=0; y<children.getLength(); y++) 
                    {
                      Node child = children.item(y);
                     // Import the node to the new document and append it to the new root
                      Node importedNode = document.importNode(child, true);
                      metadataElement.appendChild(importedNode);
                    }
            
                    // Step 6: Add metadata to the renamed root and replace the old root with the new root in the document
                    newRoot.appendChild(metadataElement);
                    document.replaceChild(newRoot, oldRoot);
            
                    // Step 7: Write the modified document to a new XML file
                    TransformerFactory transformerFactory = TransformerFactory.newInstance();
                    Transformer transformer = transformerFactory.newTransformer();
  
                    transformer.setOutputProperty("indent", "yes");
                    transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2"); // Amount of indentation
                    transformer.setOutputProperty("method", "xml");
  
                    // Step 8: Process the output to add newlines without extra indentation
                    // Write to a ByteArrayOutputStream instead of directly to a file
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    StreamResult result = new StreamResult(byteArrayOutputStream);
                    transformer.transform(new DOMSource(document), result);
  
                    String xmlOutput = byteArrayOutputStream.toString("UTF-8");
                    // Replace multiple carriage returns with a single linefeed
                    xmlOutput = xmlOutput.replaceAll("(?m)^[ \t]*\r?\n", ""); // Remove empty lines
                    xmlOutput = xmlOutput.replaceAll("(<[^/>]+>)\\s*(<)", "$1\n$2"); // Add newline after opening tags
  
                    // Write the final processed XML to a file
                    FileOutputStream fos = new FileOutputStream("C:\\AntekHW\\CALEBMIRTHTESTING\\FullChannelExport\\channelMetadataBackup\\"+metadataDirectory[m].getName());
                    fos.write(xmlOutput.getBytes("UTF-8"));
                    fos.close();
                  }                       
                } 
                catch (Exception e) 
                {
                  e.printStackTrace();
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
        catch (Exception e) 
        {
            e.printStackTrace();
        }
        /** updated logic will:
         * 1. Initialize 2 array lists: currentChannelXML and currentMetadata
         * Those will be used to read from the master array lists. They'll be cycled and cleared after each iteration
         * 2. Capture and remove the end of the current array
         * 3. Adds the channelMetadata to the channel
         * 4. Writes the full files to the channelBackup folder
         */

        //calls codeTemplates
        if(includeCTLs == true || isFullMirthExport == true)
        {
          exportCodeTemplates(host);
        }
        
        //1. Creates new arrays and loop
        ArrayList currentChannelXML = new ArrayList<>();
        ArrayList currentMetadata = new ArrayList<>();
        int currentChannelSize = 0;
        for(int c=0;c<extractedCMDArray.size();c++)
        {
          //splits the targeted full channel XML
          String[] splitCurrentChannelXML = masterChannelXML.get(c).toString().split("\n");
          for(int n=0;n<splitCurrentChannelXML.length;n++)
          {
            currentChannelXML.add(splitCurrentChannelXML[n]);
          }
          //spilts the corresponding channelMetadata XML
          File currentCMDFile = new File("C:\\AntekHW\\CALEBMIRTHTESTING\\FullChannelExport\\channelMetadataBackup\\" + channelIDs.get(c) + ".xml");
          try(Scanner channelReader = new Scanner(currentCMDFile))
          {                      
            while(channelReader.hasNext())
            {
              String line = channelReader.nextLine();
              currentMetadata.add(line);
            }
            channelReader.close();
          }
          currentChannelSize = currentChannelXML.size();

          //2. Removes the last item from the channel array, stores it for later, and adds the channelMetadata to the channel array
          String endOfChannelArray = currentChannelXML.get(currentChannelSize-1).toString();
          currentChannelXML.remove(currentChannelSize-1);
          for(int p=1;p<currentMetadata.size();p++)
          {
            currentChannelXML.add(currentMetadata.get(p));
          }
          currentChannelXML.add(endOfChannelArray);

 
          //3. Overwrites the data to the existing channel backup XML files
          try (PrintWriter channelExportFinal = new PrintWriter("C:\\AntekHW\\CALEBMIRTHTESTING\\FullChannelExport\\channelBackup\\" + channelNames.get(c)))
          {
            for (int h=0; h<currentChannelXML.size(); h++)
            {
             channelExportFinal.print(currentChannelXML.get(h)+"\n");
             if(currentChannelXML.get(h).toString().contains("</metadata>") && includeCTLs == true)
             {
              for(int b=0;b<allCTLArray.size();b++)
              {
                channelExportFinal.print(allCTLArray.get(b));
              }
             }
            }
            channelExportFinal.close();
          }
          catch (FileNotFoundException fileExcept2) 
          {
            System.out.println("I DIDN'T FIND THE channelMetadata FILE");
          }
          
          //clears both arrays for the next loop
          currentChannelXML.clear();
          currentMetadata.clear();
        }             
        //END channelExport Appending
        curExtractedCT.clear();
        currentCodeTemplateLib.clear();
        completeCodeTemplates.clear();
        codeTemplateIDs.clear();
        codeTemplateLibraryNames.clear();
        codeTemplateNames.clear();
        allCTLArray.clear();
        extractedCMDArray.clear();
        masterChannelXML.clear();
        extractedCMDArrayREORDERED.clear();
        //logCommands.returnArchivedChannels("C:\\AntekHW\\CALEBMIRTHTESTING\\FullChannelExport\\channelBackup\\");
        return "metaDataExported";
    }

    public static String exportCodeTemplates(String host) throws FileNotFoundException
    {
      //creates backup directory if it does not exist
      File dir = new File("C:\\AntekHW\\CALEBMIRTHTESTING\\FullChannelExport\\channelCodeTemplatesBackup\\codeTemplateLibraries\\");
      dir.mkdirs();
      dir = new File("C:\\AntekHW\\CALEBMIRTHTESTING\\FullChannelExport\\channelCodeTemplatesBackup\\codeTemplates\\");
      dir.mkdirs();

      //int to track if the first query has ran
      int queryCount = 0;

      String query="";
      while(queryCount <= 1)
      {
        //exports CodeTemplateLibraries
        try(Connection conn = DriverManager.getConnection(host); Statement stmt = conn.createStatement())
        {
          //sets queries
          if(queryCount == 0)
          {
            query = "SELECT * FROM CODE_TEMPLATE_LIBRARY";
          }
          else
          {
            query = "SELECT * FROM CODE_TEMPLATE";
          }
          ResultSet rs = stmt.executeQuery(query);
          ResultSetMetaData RSMD = rs.getMetaData();
          int columns = RSMD.getColumnCount();


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
                
              //column containing CLOB
              String XMLdata = rs.getString(4);
              //exports all channel export files to the specified directory
              String destination = "";
              if(queryCount == 0)
              {
                query = "SELECT * FROM CODE_TEMPLATE_LIBRARY";
                destination = "C:\\AntekHW\\CALEBMIRTHTESTING\\FullChannelExport\\channelCodeTemplatesBackup\\codeTemplateLibraries\\";

              }
              else
              {
                query = "SELECT * FROM CODE_TEMPLATE";
                destination = "C:\\AntekHW\\CALEBMIRTHTESTING\\FullChannelExport\\channelCodeTemplatesBackup\\codeTemplates\\";
              }
              try (PrintWriter XMLout = new PrintWriter(destination + fileName+".xml")) 
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
        catch (Exception e) 
        {
          e.printStackTrace();
        }
        queryCount++;
        System.out.println("Query Count: " + queryCount);
      }

      //captures all code template XML data and stores it in an array
      File[] codeTemplateDirectory = dir.listFiles();
      String currentCodeTemplateXML = "";
      for(int b=0;b<codeTemplateDirectory.length;b++)
      {
        codeTemplateNames.add(codeTemplateDirectory[b].getName());
      }
      for(int c=0;c<codeTemplateNames.size();c++)
      {        
        File currentCTXML = new File("C:\\AntekHW\\CALEBMIRTHTESTING\\FullChannelExport\\channelCodeTemplatesBackup\\codeTemplates\\"+codeTemplateNames.get(c));
        try(Scanner channelDirReader = new Scanner(currentCTXML))
        {
          while(channelDirReader.hasNext())
          {
            String line = channelDirReader.nextLine();
            currentCodeTemplateXML += line+"\n";
          }
          completeCodeTemplates.add(currentCodeTemplateXML);
          currentCodeTemplateXML = "";
          channelDirReader.close();
        }
      }

      //reads the code template library and appends the code template information
      dir = new File("C:\\AntekHW\\CALEBMIRTHTESTING\\FullChannelExport\\channelCodeTemplatesBackup\\codeTemplateLibraries\\");
      File[] codeTemplateLibraryDir = dir.listFiles();
      //gets names of code template libraries
      for(int j=0;j<codeTemplateLibraryDir.length;j++)
      {
        codeTemplateLibraryNames.add(codeTemplateLibraryDir[j].getName());
      }

      //adds the opening "codeTemplateLibraries" tag
      allCTLArray.add("<codeTemplateLibraries>"+"\n");

      //parses and reads each file
      //appends the channel template to the channel template library 
      for(int v=0;v<codeTemplateLibraryNames.size();v++)
      {
        File currentCodeTemplateLibXML = new File("C:\\AntekHW\\CALEBMIRTHTESTING\\FullChannelExport\\channelCodeTemplatesBackup\\codeTemplateLibraries\\"+codeTemplateLibraryNames.get(v));
        try(Scanner channelDirReader = new Scanner(currentCodeTemplateLibXML))
        {
          //this loop does the following:
          /**
           * 1. declares an idCount to skip the Code Template Library ID
           * 2. reads the specified code template IDs from the code template library
           * 3. Trims and adds them to an array
           * 4. clears the codeTemplateIDs array when finished
           */
          int idCount = 0;
          while(channelDirReader.hasNext())
          {
            String capturedCTLID = "";
            String line = channelDirReader.nextLine();
            //checks if the ID is from the "channel template library" vs the "channel template"
            if(line.contains("<id>") && idCount == 0)
            {
              idCount++;
            }
            else if(line.contains("<id>"))
            {
              capturedCTLID = line.replace("<id>","").replace("</id>","").trim();
              codeTemplateIDs.add(capturedCTLID);
            }
            currentCodeTemplateLib.add(line);
          }
          idCount = 0;
          channelDirReader.close();

          /**
           * 1. take and read the "currentCodeTemplateLib" array
           * 2. Check if the current line.contains(<id>)
           * 3. If so, trim the captured value and see if "completeCodeTemplates" has the trimmed value
           * 4. Replace the current ID line with the full Code Template Library XML
           * 5. Add to array and write it out
           */
          int idSkipper = 0;
          boolean foundIDInCT = false;
          for(int y=0;y<currentCodeTemplateLib.size();y++)
          {
            String currentPoint = currentCodeTemplateLib.get(y).toString();
            if(currentPoint.contains("<id>") && idSkipper == 0)
            {
              idSkipper++;
            }
            else if(currentPoint.contains("<id>"))
            {
              String currentCapID = currentPoint.replace("<id>","").replace("</id>","").trim();
              for(int s=0;s<completeCodeTemplates.size();s++)
              {
                if(completeCodeTemplates.get(s).toString().contains(currentCapID))
                {
                  //added for evaluation to see if found ID is in a code template library
                  foundIDInCT = true;

                  String editedCTString = "";
                  String[] splitCTByNL = completeCodeTemplates.get(s).toString().split("\n");
    
                  for(int u=0;u<splitCTByNL.length;u++)
                  {
                    if(u>0 && u<splitCTByNL.length-1)
                    {
                      curExtractedCT.add(splitCTByNL[u]);
                    }
                  }
                  for(int r=0;r<curExtractedCT.size();r++)
                  {
                    editedCTString = editedCTString + curExtractedCT.get(r)+"\n";
                  }
                  currentCodeTemplateLib.set(y, editedCTString);
                  editedCTString = "";
                  curExtractedCT.clear();
                }
                else if(s == completeCodeTemplates.size()-1 && foundIDInCT == false)
                {
                  System.out.println("I WAS NOT DETECTED");
                  System.out.println("ID NOT FOUND: " + currentPoint);

                  for(int n=0;n<3;n++)
                  {
                    if(n==0)
                    {
                      System.out.println("DELETED y+1: " + currentCodeTemplateLib.get(y+1));
                      currentCodeTemplateLib.remove(y+1);
                    }
                    else if(n==1)
                    { 
                      System.out.println("DELETED y: " + currentCodeTemplateLib.get(y));
                      currentCodeTemplateLib.remove(y);
                    }
                    else if (n==2)
                    {
                      System.out.println("DELETED y-1: " + currentCodeTemplateLib.get(y-1));
                      currentCodeTemplateLib.remove(y-1);
                    }
                    else
                    {
                      System.out.println("FIRED ON 3");
                    }
                  }
                  y = y-3;
                }
              }            
              foundIDInCT = false;  
            }
          }
          idSkipper = 0;

          //adds the first tag

          //writes to file
          try (PrintWriter cTLOutput = new PrintWriter("C:\\AntekHW\\CALEBMIRTHTESTING\\FullChannelExport\\channelCodeTemplatesBackup\\codeTemplateLibraries\\"+codeTemplateLibraryNames.get(v)))
          {
            String appendedCTLMasterAddition = "";
            for(int c=0;c<currentCodeTemplateLib.size();c++)
            {
              cTLOutput.print(currentCodeTemplateLib.get(c));
              appendedCTLMasterAddition += (currentCodeTemplateLib.get(c)+"\n");
            }
            allCTLArray.add(appendedCTLMasterAddition);
            appendedCTLMasterAddition = "";
            cTLOutput.close();
          }
          codeTemplateIDs.clear();
        }
        currentCodeTemplateLib.clear();
      }

      //adds the closing "codeTemplateLibraries" tag
      allCTLArray.add("</codeTemplateLibraries>");

      //the below code creates a master codeTemplateLibrary String from all x CTLs
      String allCodeTemplateLibraries = "";
      File ctLDIR = new File("C:\\AntekHW\\CALEBMIRTHTESTING\\FullChannelExport\\channelCodeTemplatesBackup\\codeTemplateLibraries\\");
      ctLDIR.getParentFile().mkdirs();
      File[] codeTemplateLibraryDirectoryList = ctLDIR.listFiles();
      for(int m=0;m<codeTemplateLibraryDirectoryList.length;m++)
      {
        File currentCTLFile = new File("C:\\AntekHW\\CALEBMIRTHTESTING\\FullChannelExport\\channelCodeTemplatesBackup\\codeTemplateLibraries\\"+codeTemplateLibraryDirectoryList[m].getName());
        try(Scanner channelDirReader = new Scanner(currentCTLFile))
        {
          while(channelDirReader.hasNext())
          {
            String line = channelDirReader.nextLine();
            allCodeTemplateLibraries = allCodeTemplateLibraries + line + "\n";
          }
        }
      }
      return "codeTemplates and Libraries Exported";
    }
}
