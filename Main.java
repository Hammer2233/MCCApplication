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

import javax.lang.model.element.Element;
//import javax.swing.text.Document;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class Main 
{
    private static ArrayList channelXMLArray = new ArrayList();
    private static ArrayList channelMetadataXMLArray = new ArrayList();
    private static ArrayList extractedCMD = new ArrayList<>();
    private static String XMLdata = "";
    public static void main(String args[]) throws ClassNotFoundException, SQLException, FileNotFoundException
    {
      String driver = "org.apache.derby.jdbc.EmbeddedDriver";
      Class.forName(driver);
      String host = "jdbc:derby:C:\\Program Files\\Mirth Connect\\appdata\\/mirthdb";

      try (Connection conn = DriverManager.getConnection(host); Statement stmt = conn.createStatement()) 
      {
          String query = "SELECT * FROM CONFIGURATION";
          //String query = "SELECT channelMetadata from CONFIGURATION";
          ResultSet rs = stmt.executeQuery(query);
          ResultSetMetaData RSMD = rs.getMetaData();
          int columns = RSMD.getColumnCount();

          for(int i=1;i<=columns;i++)
          {
              System.out.println(RSMD.getColumnLabel(i));
          }

          System.out.println(RSMD);
          System.out.println("COLUMNS:" + columns);

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
            
              /**
                * What this current function is to:
                * 1. Grab the channelMetadata XML file
                * 2. Grabs and reads the channel's XML backup
                * 3. Split it by the newlines ("\n"'s) to break the XML down to each line
                * 4. Writes each line to an XML file to rebuild the data
                */
              if(rs.getString(2).equals("channelMetadata"))
              {
                System.out.println("FOUND THE PRUNING DATA");

                //Captured Channel ID
                String capturedChannelID = "";

                File channelXML = new File("C:\\AntekHW\\CALEBMIRTHTESTING\\ARCHIVED\\Comparison\\APPENDXMLTESTING\\ChannelBackup" + "\\ArkStone Result Export.xml");
                try(Scanner channelReader = new Scanner(channelXML))
                {                      
                  while(channelReader.hasNext())
                  {
                    String line = channelReader.nextLine();

                    if(line.contains("<id>"))
                    {
                      System.out.println("FOUND CHANNEL ID");
                      capturedChannelID = line.replace("<id>","").replace("</id>","").trim();
                    }
                    channelXMLArray.add(line);
                   }
                  channelReader.close();
                }
                System.out.println("CHANNEL ID: " + capturedChannelID);

                String channelMetadataXML = rs.getString(3);
                String[] splitByRootCMD = channelMetadataXML.split("<entry>");
                String[] splitChannelMetadata = channelMetadataXML.split("\n");

                //Finds the split root (split by "<entry>") containing the channelMetadata for the found channel ID
                for (int k=0; k<splitByRootCMD.length;k++)
                {
                  if(splitByRootCMD[k].toString().contains(capturedChannelID))
                  {
                    System.out.println("FOUND MATCHING channelMetadata. Stored in: " + k);
                    //System.out.println(splitByRootCMD[k]);

                    //cleans up the found root from channelMetadata
                    String channelMetadataCleanup = splitByRootCMD[k];
                    channelMetadataCleanup.replace("<string>" + capturedChannelID + "</string>","").replace("</entry>","").replace("<com.mirth.connect.model.ChannelMetadata>","").replace("</com.mirth.connect.model.ChannelMetadata>", "");

                    String[] splitFoundRoot = channelMetadataCleanup.split("\n");
                    for (int b=0;b<splitFoundRoot.length;b++)
                    {
                      if(!splitFoundRoot[b].matches("com.mirth.connect.model.ChannelMetadata"))
                      {
                        if(!splitFoundRoot[b].contains("</entry>"))
                        {
                          extractedCMD.add(splitFoundRoot[b]);
                        }
                      }
                    }

                  }
                }
                    
                int extractedCMDSize = extractedCMD.size();
                extractedCMD.remove(extractedCMDSize-1);

                try (PrintWriter channelMetadataOut = new PrintWriter("C:\\AntekHW\\CALEBMIRTHTESTING\\ARCHIVED\\Comparison\\APPENDXMLTESTING\\FinalDestination" + "\\testExtracted.xml"))
                {
                  for (int o=2; o<extractedCMD.size(); o++)
                  {
                    channelMetadataOut.print(extractedCMD.get(o));
                  }
                  channelMetadataOut.close();
                }
                catch (FileNotFoundException fileExcept2) 
                {
                  System.out.println("I DIDN'T FIND THE channelMetadata FILE");
                } 
                    
                try 
                {
                  // Step 1: Create a DocumentBuilderFactory and DocumentBuilder
                  DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                  DocumentBuilder builder = factory.newDocumentBuilder();
          
                  // Step 2: Parse the XML file
                  Document document = builder.parse(new File("C:\\AntekHW\\CALEBMIRTHTESTING\\ARCHIVED\\Comparison\\APPENDXMLTESTING\\FinalDestination\\testExtracted.xml"));
          
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
                  FileOutputStream fos = new FileOutputStream("C:\\AntekHW\\CALEBMIRTHTESTING\\ARCHIVED\\Comparison\\APPENDXMLTESTING\\FinalDestination\\testExtracted-ROOT.xml");
                  fos.write(xmlOutput.getBytes("UTF-8"));
                  fos.close();


                  // DOMSource source = new DOMSource(document);
                  // StreamResult result = new StreamResult(new File("C:\\AntekHW\\CALEBMIRTHTESTING\\ARCHIVED\\Comparison\\APPENDXMLTESTING\\FinalDestination\\testExtracted-ROOT.xml"));
                  // transformer.transform(source, result);          
                } 
                catch (Exception e) 
                {
                  e.printStackTrace();
                }

                try (PrintWriter channelMetadataOut = new PrintWriter("C:\\AntekHW\\CALEBMIRTHTESTING\\ARCHIVED\\Comparison\\APPENDXMLTESTING\\FinalDestination" + "\\test1.xml"))
                {
                  for (int c=0; c<splitChannelMetadata.length; c++)
                  {
                    channelMetadataOut.print(splitChannelMetadata[c]);
                  }
                  channelMetadataOut.close();
                }
                catch (FileNotFoundException fileExcept2) 
                {
                  System.out.println("I DIDN'T FIND THE channelMetadata FILE");
                } 
              }
              //END channelMetadata Extraction ^^^                  

              try (PrintWriter XMLout = new PrintWriter("C:\\AntekHW\\CALEBMIRTHTESTING" + "\\" + fileName)) 
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
        //new here 
        /**
        * The below section will utilize the previously captured "channelXMLArray" to:
        * 1. Read the entire channel XML file and split on newlines
        * 2. Utilize a search that will look for the last 2 XML tags to determine the end of the document
        * 3. Add the items to an arraylist
        * 4. Print out to a new XML file
        */
                  
        //1. Determines the size of the array. That is used to find the end of the file and where to append the channelMetatdata
        int sizeOfChannelXML = channelXMLArray.size();
        System.out.println("\n");
        System.out.println("ChannelXML Array Size: " + sizeOfChannelXML);

        //2. Reads and copies out the contents of the formatted channelMetadata file
        File cMDFinalXML = new File("C:\\AntekHW\\CALEBMIRTHTESTING\\ARCHIVED\\Comparison\\APPENDXMLTESTING\\FinalDestination" + "\\testExtracted-ROOT.xml");
        //String cMDFinalXMLString = "";
        try(Scanner channelReader = new Scanner(cMDFinalXML))
        {                      
          while(channelReader.hasNext())
          {
            String line = channelReader.nextLine();
            //System.out.println("CURRENTL LINE: " + line);
            channelMetadataXMLArray.add(line);
          }
          channelReader.close();
        }
        //cMDFinalXMLString.replace(, "");
        //String[] splitChannelMetadata = cMDFinalXMLString.split("\n");
        System.out.println("Finished #2");
        
        //3. Captures the last item from the channel array, stores it for later, and adds the channelMetadata to the channel array
        String endOfChannelArray = channelXMLArray.get(sizeOfChannelXML-1).toString();
        System.out.println("endOfChannelArray: " + endOfChannelArray);

        channelXMLArray.remove(sizeOfChannelXML-1);
        System.out.println("channelMetadataXMLArray size: " + channelMetadataXMLArray.size());
        for(int p=1;p<channelMetadataXMLArray.size();p++)
        {
          System.out.println(channelMetadataXMLArray.get(p));
          channelXMLArray.add(channelMetadataXMLArray.get(p));
        }
        channelXMLArray.add(endOfChannelArray);

        //4. Prints the data to an XML file
         try (PrintWriter channelExportFinal = new PrintWriter("C:\\AntekHW\\CALEBMIRTHTESTING\\ARCHIVED\\Comparison\\APPENDXMLTESTING\\FinalDestination" + "\\FinalChannelExport.xml"))
         {
           for (int h=0; h<channelXMLArray.size(); h++)
           {
            channelExportFinal.print(channelXMLArray.get(h));
           }
           channelExportFinal.close();
         }
         catch (FileNotFoundException fileExcept2) 
         {
           System.out.println("I DIDN'T FIND THE channelMetadata FILE");
         }
        //END channelExport Appending

        // DocumentCommands callAppend = new DocumentCommands();
        // callAppend.appendXML();
    }

}
