package main;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

//import main.GUIController.enableFeatures;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class SQLSearch extends JFrame 
{
	private static JTextArea focusedResultSpace;
	private static JTextArea SQLQuerySpace;
    private static JTable table;
    private static JPanel northPanel;
    private static JPanel centerPanel;
    private static ArrayList activeChannelNames = new ArrayList();
    private static ArrayList activeChannelIDs = new ArrayList();
    private static ArrayList activeChannelLocalIDs = new ArrayList();
    private static int chosenChannel = 0;
    private static JLabel SQLQueryName;
    private static JButton searchQuery;
    private static JTextField startDateSpace;
    private static JTextField endDateSpace;
    private static JTextField containsTextSpace;
    private static JButton clearDataChannel;
    private static JButton clearDataCustom;
    private static JButton searchData;
    private static JPanel southPanel;
    private static JButton tableInfo;
    
    //search related arrays for channel searching
    //first channel search
    private static ArrayList firstMetadataIDs = new ArrayList();    
    private static ArrayList messageIDs = new ArrayList();
    private static ArrayList contentTypes = new ArrayList();
    private static ArrayList contents = new ArrayList();
    
    //second search
    private static ArrayList secondMetadataIDs = new ArrayList();
    private static ArrayList secondMessageIDs = new ArrayList();
    private static ArrayList receivedDates = new ArrayList();
    private static ArrayList statuses = new ArrayList();
    private static ArrayList connectorNames = new ArrayList();
    private static ArrayList sentDates = new ArrayList();
    private static ArrayList responseDates = new ArrayList();    
    
    //ArrayLists for channel search results
    //first channel search
    private static ArrayList SEARCHfirstMetadataIDs = new ArrayList();    
    private static ArrayList SEARCHmessageIDs = new ArrayList();
    private static ArrayList SEARCHcontentTypes = new ArrayList();
    private static ArrayList SEARCHcontents = new ArrayList();
    
    //second search
    private static ArrayList SEARCHsecondMetadataIDs = new ArrayList();
    private static ArrayList SEARCHsecondMessageIDs = new ArrayList();
    private static ArrayList SEARCHreceivedDates = new ArrayList();
    private static ArrayList SEARCHstatuses = new ArrayList();
    private static ArrayList SEARCHconnectorNames = new ArrayList();
    private static ArrayList SEARCHsentDates = new ArrayList();
    private static ArrayList SEARCHresponseDates = new ArrayList();
    
    private static ArrayList SEARCHimportantIDs = new ArrayList();
    private static ArrayList validMsgIDs = new ArrayList();
    
    private static int chosenQuery;

    public SQLSearch(int choice) 
    {
    	
    	//Checks if Mirth is >=3.5 as SQL Searching is not available in older Mirth
    	String host = Main.returnHost();
    	String mirthVersion = fullConfigExport.getMirthVersion(host).replace("\"", "");
    	String[] splitVersion = mirthVersion.split("\\.");
    	
    	if(Integer.parseInt(splitVersion[0]) < 4 && Integer.parseInt(splitVersion[1]) < 5 || Integer.parseInt(splitVersion[0]) < 3)
    	{
    		System.out.println("Mirth database version is less than 3.5");
    		logCommands.exportToLog("SQL Searching is not available in Mirth version <3.5.1. Action cancelled");
    	}
    	else
    	{
    		System.out.println("Mirth database version is 3.5 or higher");
    		
    		setTitle("Search Results");
            chosenQuery = choice;
            
            
            if(choice == 0)
            {
            	//Channel message search
            	setSize(875, 915);
            }
            else if(choice == 1)
            {
            	//Custom SQL query
            	setSize(830, 670);
            }
            initUI();
            setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            System.out.println("Chosen Query: " + chosenQuery);        

            setVisible(true);
    	}
    }
    

    private void initUI() 
    {
    	SQLCommand.clearArraysForActiveChannels();
        table = new JTable();
        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.SOUTH);
        
        northPanel = new JPanel();
        northPanel.setPreferredSize(new Dimension(200, 280));
        northPanel.setBackground(new java.awt.Color(148, 187, 255));
        focusedResultSpace = new JTextArea(10, 67);
        JScrollPane focuseScroll = new JScrollPane(focusedResultSpace);
        northPanel.add(focuseScroll);
        if(chosenQuery == 0)
        {
        	centerPanel = new JPanel(new GridBagLayout());
        	centerPanel.setPreferredSize(new Dimension(100, 110));
        	centerPanel.setBackground(new java.awt.Color(255, 213, 148));
        	
        	Panel().setVisible(true);
        	northPanel.add(Panel());
            
            //gridbag constraints
            GridBagConstraints topGBC = new GridBagConstraints();
            GridBagLayout topPanelGBL = new GridBagLayout();
            topGBC.fill = GridBagConstraints.BOTH;
            topGBC.weightx = 1.0;
            topGBC.weighty = 1.0;
            topGBC.insets = new Insets(9, 9, 9, 9);
            
            centerPanel.setLayout(topPanelGBL);
            
            topGBC.gridx = 0;
            topGBC.gridy = 0;
            topGBC.weightx = 0;
            JLabel dateFormat1 = new JLabel("Please format date like:");
            centerPanel.add(dateFormat1, topGBC);            
            topGBC.gridx = 1;
            topGBC.gridy = 0;
            topGBC.weightx = 0;
            JLabel dateFormat2 = new JLabel("YYYYMMDD");
            centerPanel.add(dateFormat2, topGBC);
            
            topGBC.gridx = 0;
            topGBC.gridy = 1;
            topGBC.weightx = 0;          
            JLabel start = new JLabel("START DATE (AFTER THIS): ");
            centerPanel.add(start, topGBC);
            topGBC.gridx = 1;
            topGBC.gridy = 1;
            topGBC.weightx = 0;
            startDateSpace = new JTextField(15);
            centerPanel.add(startDateSpace, topGBC);
            topGBC.gridx = 0;
            topGBC.gridy = 2;
            topGBC.weightx = 0;
            JLabel end = new JLabel("END DATE (BEFORE THIS): ");
            centerPanel.add(end, topGBC);
            topGBC.gridx = 1;
            topGBC.gridy = 2;
            topGBC.weightx = 0;
            endDateSpace = new JTextField(15);
            centerPanel.add(endDateSpace, topGBC);
            
            topGBC.gridx = 0;
            topGBC.gridy = 3;
            topGBC.weightx = 0;
            JLabel containsThis = new JLabel("CONTAINS TEXT: ");
            centerPanel.add(containsThis, topGBC);            
            topGBC.gridx = 1;
            topGBC.gridy = 3;
            topGBC.weightx = 0;
            containsTextSpace = new JTextField(15);
            centerPanel.add(containsTextSpace, topGBC);
            
            topGBC.gridx = 0;
            topGBC.gridy = 4;
            topGBC.weightx = 0;
            clearDataChannel = new JButton("CLEAR ALL");
            clearDataChannel.addActionListener(new clearChannelSearch());
            clearDataChannel.setForeground(Color.BLACK);
            clearDataChannel.setBackground(new java.awt.Color(252, 80, 80));
            centerPanel.add(clearDataChannel, topGBC);
            topGBC.gridx = 1;
            topGBC.gridy = 4;
            topGBC.weightx = 0;
            searchData = new JButton("SEARCH");
            searchData.addActionListener(new searchChannelData());
            searchData.setForeground(Color.BLACK);
            searchData.setBackground(new java.awt.Color(80, 156, 252));
            centerPanel.add(searchData, topGBC);
            add(centerPanel, BorderLayout.CENTER);
        }        
        else if(chosenQuery == 1)
        {
        	SQLQueryName = new JLabel("SQL QUERY:");
        	northPanel.add(SQLQueryName);
        	SQLQuerySpace = new JTextArea(3, 25);
        	northPanel.add(SQLQuerySpace);
        	searchQuery = new JButton("SEARCH");        	
        	searchQuery.setForeground(Color.BLACK);
        	searchQuery.setBackground(Color.YELLOW);
        	searchQuery.addActionListener(new searchQueryText());
        	northPanel.add(searchQuery);
        	clearDataCustom = new JButton("CLEAR ALL");
        	clearDataCustom.addActionListener(new clearCustomSQLInformation());
        	clearDataCustom.setForeground(Color.BLACK);
        	clearDataCustom.setBackground(new java.awt.Color(252, 80, 80));
        	northPanel.add(clearDataCustom);
        	tableInfo = new JButton("TABLE INFO");
        	tableInfo.addActionListener(new returnTableNames());
        	tableInfo.setForeground(Color.BLACK);
        	tableInfo.setBackground(new java.awt.Color(0, 255, 100));
        	northPanel.add(tableInfo);
        }
        add(northPanel, BorderLayout.NORTH);
        
        // Add a MouseListener to detect cell clicks
        table.addMouseListener(new MouseAdapter() 
        {
            @Override
            public void mouseClicked(MouseEvent e) 
            {
                int selectedRow = table.getSelectedRow();
                int selectedColumn = table.getSelectedColumn();

                if (selectedRow != -1 && selectedColumn != -1) 
                {
                    Object cellValue = table.getValueAt(selectedRow, selectedColumn);
                    if(cellValue == null)
                    {
                    	System.out.println("Selected Cell Value: null");
                    	focusedResultSpace.setText("");
                    }
                    else
                    {
                    	//System.out.println("Selected Cell Value: " + cellValue);
                    	if(cellValue.toString().startsWith("MSH|"))
                    	{
                    		//checks to see if it's an HL7 message
                    		String reformatHL7 = reformatHL7Message(cellValue.toString());
                    		focusedResultSpace.setText(reformatHL7);
                    	}
                    	else
                    	{
                    		focusedResultSpace.setText(cellValue.toString().replace("\r", "\n"));
                    	}                        
                    }                    
                } 
                else 
                {
                    System.out.println("No cell selected.");
                }
            }
        });
    }
    
    public Component Panel() 
    {    	
    	activeChannelNames.clear();
    	activeChannelIDs.clear();
    	activeChannelLocalIDs.clear();
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(new java.awt.Color(148, 187, 255));
        buttonPanel.setLayout(new java.awt.GridLayout(4, 6));

        int numberOfChannels = SQLCommand.channelStatusListSize();
        
        // Calls the SQLCommand class to make use of the channel status check
        String host = Main.returnHost();
        SQLCommand.channelStatusBuilder(host);
        
        // Captures the names of all active channels for the arraylist
        for (int i = 0; i < numberOfChannels; i++) 
        {
            if (SQLCommand.returnChannelStatus(i).toString().equals("[ACTIVE]"))
            {
                activeChannelNames.add(SQLCommand.returnChannelName(i));
            }
        }
        
        // Captures the ID of the active channel and the ID of the LOCAL_CHANNEL_ID
        for (int b = 0; b < activeChannelNames.size(); b++) 
        {
            try (Connection conn = DriverManager.getConnection(host);
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT ID FROM CHANNEL WHERE NAME = '" + activeChannelNames.get(b) + "'")) 
            {
                while (rs.next()) 
                {
                    activeChannelIDs.add(rs.getString(1));
                }
            } 
            catch (SQLException e1) 
            {
                e1.printStackTrace();
            }
            
            try (Connection conn = DriverManager.getConnection(host);
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT LOCAL_CHANNEL_ID FROM D_CHANNELS WHERE CHANNEL_ID = '" + activeChannelIDs.get(b) + "'")) 
            {
                while (rs.next()) 
                {
                    activeChannelLocalIDs.add(rs.getString(1));
                }
            } 
            catch (SQLException e1) 
            {
                e1.printStackTrace();
            }
        }
        
        for (int i = 0; i < activeChannelNames.size(); ++i) 
        {
            int currentChannel = i;
            JButton b = new JButton(String.valueOf(activeChannelNames.get(i)));
            b.addActionListener(new java.awt.event.ActionListener() 
            {
                public void actionPerformed(java.awt.event.ActionEvent e) 
                {
                    System.out.println("Local Channel ID: " + activeChannelLocalIDs.get(currentChannel));
                    chosenChannel = Integer.parseInt((String) activeChannelLocalIDs.get(currentChannel));
                    loadDataChannels();
                }
            });
            buttonPanel.add(b);
        }
        
        return buttonPanel;
    }

    //private void loadDataChannels(chosenChannel) 
    private static void loadDataChannels() 
    {
    	formatChannelSearch();
    	
//    	System.out.println("================= Array Sizes Check =================");
//    	System.out.println("firstMetadataIDs.size()       = " + firstMetadataIDs.size());
//    	System.out.println("messageIDs.size()             = " + messageIDs.size());
//    	System.out.println("contentTypes.size()           = " + contentTypes.size());
//    	System.out.println("contents.size()               = " + contents.size());
//    	System.out.println("secondMetadataIDs.size()      = " + secondMetadataIDs.size());
//    	System.out.println("secondMessageIDs.size()       = " + secondMessageIDs.size());
//    	System.out.println("receivedDates.size()          = " + receivedDates.size());
//    	System.out.println("statuses.size()               = " + statuses.size());
//    	System.out.println("connectorNames.size()         = " + connectorNames.size());
//    	System.out.println("sentDates.size()              = " + sentDates.size());
//    	System.out.println("responseDates.size()          = " + responseDates.size());
//    	System.out.println("SEARCHfirstMetadataIDs.size() = " + SEARCHfirstMetadataIDs.size());
//    	System.out.println("SEARCHmessageIDs.size()       = " + SEARCHmessageIDs.size());
//    	System.out.println("SEARCHcontentTypes.size()     = " + SEARCHcontentTypes.size());
//    	System.out.println("SEARCHcontents.size()         = " + SEARCHcontents.size());
//    	System.out.println("SEARCHsecondMetadataIDs.size()= " + SEARCHsecondMetadataIDs.size());
//    	System.out.println("SEARCHsecondMessageIDs.size() = " + SEARCHsecondMessageIDs.size());
//    	System.out.println("SEARCHreceivedDates.size()    = " + SEARCHreceivedDates.size());
//    	System.out.println("SEARCHstatuses.size()         = " + SEARCHstatuses.size());
//    	System.out.println("SEARCHconnectorNames.size()   = " + SEARCHconnectorNames.size());
//    	System.out.println("SEARCHsentDates.size()        = " + SEARCHsentDates.size());
//    	System.out.println("SEARCHresponseDates.size()    = " + SEARCHresponseDates.size());
//    	System.out.println("SEARCHimportantIDs.size()     = " + SEARCHimportantIDs.size());
//    	System.out.println("=====================================================");
    	
    	DefaultTableModel model = new DefaultTableModel();
    	model.addColumn("MESSAGE_ID");
    	model.addColumn("RECEIVED_DATE");
    	model.addColumn("SEND_DATE");
    	model.addColumn("RESPONSE_DATE");
    	model.addColumn("CONTENT");
    	model.addColumn("CONTENT_TYPE");
    	model.addColumn("CONNECTOR_NAME");
    	
    	for(int display=0;display<messageIDs.size();display++)
    	{
    		Object[] rowData = new Object[7];
    		
    		for(int messagesTwo=0;messagesTwo<secondMessageIDs.size();messagesTwo++)
    		{
    			if(messageIDs.get(display).equals(secondMessageIDs.get(messagesTwo)) && firstMetadataIDs.get(display).equals(secondMetadataIDs.get(messagesTwo)))
    			{
    				rowData[0] = messageIDs.get(display);
    				rowData[1] = receivedDates.get(messagesTwo);
    	    		rowData[2] = sentDates.get(messagesTwo);
    	    		rowData[3] = responseDates.get(messagesTwo);
    	    		rowData[4] = contents.get(display);
    	    		rowData[5] = contentTypeConvert(Integer.valueOf((String) contentTypes.get(display)));
    	    		rowData[6] = connectorNames.get(messagesTwo);
    			}
    		}    		
    		model.addRow(rowData);
    	}
    	
    	table.setModel(model);
        System.out.println("Number of rows in model: " + model.getRowCount());
    }
    
    private void loadDataChannelsOld() 
    {
        String host = Main.returnHost();
        //try (Connection conn = DriverManager.getConnection(host); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery("SELECT MESSAGE_ID, CONTENT_TYPE, CONTENT, DATA_TYPE FROM D_MC5"))
        try (Connection conn = DriverManager.getConnection(host); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery("SELECT MESSAGE_ID, CONTENT_TYPE, CONTENT, DATA_TYPE FROM D_MC" + chosenChannel)) 
        {        	
        	ResultSetMetaData rsmd = rs.getMetaData();            
            DefaultTableModel model = new DefaultTableModel();
            int columnCount = rsmd.getColumnCount();
            
            for(int i=1; i<=columnCount;i++)
            {
            	model.addColumn(rsmd.getColumnName(i));
            }
            
//            model.addColumn("ID");
//            model.addColumn("NAME");
//            model.addColumn("REVISION");
//            model.addColumn("CHANNEL");

            while (rs.next()) 
            {
            	Object[] rowData = new Object[columnCount];
            	for(int j=1; j<=columnCount;j++)
            	{
            		String currentColumnName = rsmd.getColumnName(j);
            		
            		if("CLOB".equalsIgnoreCase(rsmd.getColumnTypeName(j))) 
            		{
                        Clob clob = rs.getClob(j);
                        if (clob != null) 
                        {
                            // Convert CLOB to String
                            String clobData = clobToString(clob);
                            rowData[j - 1] = clobData; 
                        } 
                        else 
                        {
                        	rowData[j - 1] = null;
                        }
                    }
            		else if ("CONTENT_TYPE".equalsIgnoreCase(currentColumnName))
                    {
                        int contentTypeId = rs.getInt(j);
                        
                        // Perform safe conversion check
                        if (contentTypeId > 0 && contentTypeId <= 15)
                        {
                            rowData[j - 1] = contentTypeConvert(contentTypeId);
                        }
                        else
                        {
                            rowData[j - 1] = null;
                        }
                    }
            		else
            		{
                        rowData[j - 1] = rs.getObject(j);
                    }
            	}
            	model.addRow(rowData);                
            }

            table.setModel(model);
            System.out.println("Number of rows in model: " + model.getRowCount());
        } 
        catch (SQLException e) 
        {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading data: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        applicationWindow.killConnection(host);
    }
    
    private static String clobToString(Clob clob) throws SQLException 
    {
        StringBuilder result = new StringBuilder();
        try (Reader reader = clob.getCharacterStream();
             BufferedReader bufferedReader = new BufferedReader(reader)) 
        {
            String line;
            while ((line = bufferedReader.readLine()) != null) 
            {
                result.append(line+"\n");
            }
        } 
        catch (IOException e) 
        {
            e.printStackTrace();
        }
        return result.toString();
    }
    
    private String reformatHL7Message(String message)
    {
    	String reformattedHL7 = "";
    	
    	String[] segmentHeaders = { "NTE|", "PID|", "PV1|", "GT1|", "IN1|", "IN2|", "ORC|", "FT1|", "DG1|", "OBR|", "OBX|", "ZEF|", "ZUD|" };
    	for(int iterate=0;iterate<segmentHeaders.length;iterate++)
    	{
    		if(message.contains(segmentHeaders[iterate]))
    		{
    			message = message.replace(segmentHeaders[iterate], "\n"+segmentHeaders[iterate]);
    		}
    	}
    	reformattedHL7 = message;
    	
    	return reformattedHL7; 
    }
    
    private static String contentTypeConvert(int id)
    {    	
    	id = id-1;
    	String[] contentTypes = { "RAW", "PROCESSED_RAW", "TRANSFORMED", "ENCODED", "SENT", "RESPONSE", "RESPONSE_TRANSFORMED", "PROCESSED_RESPONSE", "CONNECTOR_MAP", "CHANNEL_MAP", "RESPONSE_MAP", "PROCESSING_ERROR", "POSTPROCESSOR_ERROR", "RESPONSE_ERROR", "SOURCE_MAP" };
    	String contentName = contentTypes[id];
    	
    	return contentName;
    }
    
    private static void loadDataCustom(String queryText) 
    {
        String host = Main.returnHost();
        try (Connection conn = DriverManager.getConnection(host); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(queryText)) 
        {        	
        	ResultSetMetaData rsmd = rs.getMetaData();            
            DefaultTableModel model = new DefaultTableModel();
            int columnCount = rsmd.getColumnCount();
            
            for(int i=1; i<=columnCount;i++)
            {
            	model.addColumn(rsmd.getColumnName(i));
            }

            while (rs.next()) 
            {
            	Object[] rowData = new Object[columnCount];
            	for(int j=1; j<=columnCount;j++)
            	{
            		String currentColumnName = rsmd.getColumnName(j);
            		
            		if("CLOB".equalsIgnoreCase(rsmd.getColumnTypeName(j))) 
            		{
                        Clob clob = rs.getClob(j);
                        if (clob != null) 
                        {
                            // Convert CLOB to String
                            String clobData = clobToString(clob);
                            rowData[j - 1] = clobData; 
                        } 
                        else 
                        {
                        	rowData[j - 1] = null;
                        }
                    }
            		else if ("CONTENT_TYPE".equalsIgnoreCase(currentColumnName))
                    {
                        int contentTypeId = rs.getInt(j);
                        
                        // Perform safe conversion check
                        if (contentTypeId > 0 && contentTypeId <= 15)
                        {
                            rowData[j - 1] = contentTypeConvert(contentTypeId);
                        }
                        else
                        {
                            rowData[j - 1] = null;
                        }
                    }
            		else
            		{
                        rowData[j - 1] = rs.getObject(j);
                    }
            	}
            	model.addRow(rowData);                
            }

            table.setModel(model);
            System.out.println("Number of rows in model: " + model.getRowCount());
        } 
        catch (SQLException e) 
        {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error loading data: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        applicationWindow.killConnection(host);
    }
    
    private static String formatChannelSearch()
    {
    	secondMetadataIDs.clear();
    	secondMessageIDs.clear();
    	receivedDates.clear();
    	statuses.clear();
    	connectorNames.clear();
    	sentDates.clear();
    	responseDates.clear();
    	firstMetadataIDs.clear();
    	messageIDs.clear();
    	contentTypes.clear();
    	contents.clear();
    	
    	String host = Main.returnHost();
    	//String[] queries = { "SELECT METADATA_ID, MESSAGE_ID, CONTENT_TYPE, CONTENT FROM D_MC" + chosenChannel + " where DATA_TYPE like '%HL7%'", "SELECT ID, MESSAGE_ID, RECEIVED_DATE, STATUS, CONNECTOR_NAME, SEND_DATE, RESPONSE_DATE FROM D_MM" + chosenChannel };
    	String[] queries = { "SELECT METADATA_ID, MESSAGE_ID, CONTENT_TYPE, CONTENT FROM D_MC" + chosenChannel + " where DATA_TYPE like '%HL7%' ORDER BY MESSAGE_ID", "SELECT ID, MESSAGE_ID, RECEIVED_DATE, STATUS, CONNECTOR_NAME, SEND_DATE, RESPONSE_DATE FROM D_MM" + chosenChannel + " ORDER BY MESSAGE_ID"};
    	
    	for(int i=0;i<queries.length;i++)
    	{
    		if(i==0)
    		{
    			try (Connection conn = DriverManager.getConnection(host); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(queries[i])) 
    	        {
    				ResultSetMetaData rsmd = rs.getMetaData();
    	            while (rs.next()) 
    	            {
    	            	firstMetadataIDs.add(rs.getString(1));
    	            	messageIDs.add(rs.getString(2));
    	            	contentTypes.add(rs.getString(3));
    	            	contents.add(rs.getString(4));
    	            }
    	       } 
    	       catch (SQLException e1) 
    	       {
    	           e1.printStackTrace();
    	       }
    		}
    		else
    		{
    			try (Connection conn = DriverManager.getConnection(host); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(queries[i])) 
    	        {
    				ResultSetMetaData rsmd = rs.getMetaData();
    	            while (rs.next()) 
    	            {
    	            	secondMetadataIDs.add(rs.getString(1));
    	            	secondMessageIDs.add(rs.getString(2));
    	            	receivedDates.add(rs.getString(3));
    	            	statuses.add(rs.getString(4));
    	            	connectorNames.add(rs.getString(5));
    	            	sentDates.add(rs.getString(6));
    	            	responseDates.add(rs.getString(7));
    	            }
    	       } 
    	       catch (SQLException e1) 
    	       {
    	           e1.printStackTrace();
    	       }
    		}
    	}  
    	applicationWindow.killConnection(host);
       return "formatted";
    }
    
    private static class searchQueryText implements ActionListener
    {
        @Override
        public void actionPerformed(ActionEvent e)
        {
        	focusedResultSpace.setText("");
        	String queryToRun = SQLQuerySpace.getText();
        	loadDataCustom(queryToRun);
        }
    }
    
    private static class clearCustomSQLInformation implements ActionListener
    {
    	@Override
        public void actionPerformed(ActionEvent e)
        {
        	focusedResultSpace.setText("");
        	SQLQuerySpace.setText("");
        	DefaultTableModel model = (DefaultTableModel) table.getModel();
        	model.setRowCount(0);
        }
    }
    
    private static class clearChannelSearch implements ActionListener
    {
    	@Override
        public void actionPerformed(ActionEvent e)
        {
        	focusedResultSpace.setText("");
        	containsTextSpace.setText("");
        	endDateSpace.setText("");
        	startDateSpace.setText("");
        	DefaultTableModel model = (DefaultTableModel) table.getModel();
        	model.setRowCount(0);
        	
        	if(chosenChannel != 0)
        	{
        		loadDataChannels();
        	}
        }
    }
    
    private static class searchChannelData implements ActionListener
    {
    	@Override
        public void actionPerformed(ActionEvent e)
        {
    		//clears the ArrayList
    		SEARCHfirstMetadataIDs.clear();
    		SEARCHmessageIDs.clear();
    		SEARCHcontentTypes.clear();
    		SEARCHcontents.clear();
    		SEARCHsecondMetadataIDs.clear();
    		SEARCHsecondMessageIDs.clear();
    		SEARCHreceivedDates.clear();
    		SEARCHstatuses.clear();
    		SEARCHconnectorNames.clear();
    		SEARCHsentDates.clear();
    		SEARCHresponseDates.clear();
    		SEARCHimportantIDs.clear();
    		validMsgIDs.clear();
    		
    		//captures text from the proper spaces
        	String startDateText = startDateSpace.getText().trim();
        	String endDateText = endDateSpace.getText().trim();
        	String containsText = containsTextSpace.getText().trim();
        	
        	//boolean variables
    		boolean canISearch = false;
    		boolean didDatesPass = false;
        	//evaluates the dates to ensure they're valid
        	boolean validStart = isValidDateInput(startDateText, "Start Date");
        	boolean validEnd = isValidDateInput(endDateText, "End Date");;
        	
        	//validates criteria, then executes search
        	if(startDateText.isEmpty() && endDateText.isEmpty() && containsText.isEmpty())
        	{
        		System.out.println("No criteria entered. searching entire channel");
				searchChannelCriteria("null", "null", "null");
        	}
        	else if(startDateText.isEmpty() && endDateText.isEmpty())
        	{
        		System.out.println("Search text not empty. Searching based on 'containsText'");
				searchChannelCriteria("null", "null", containsText);
        	}
        	else if(validStart == true && containsText.isEmpty() && endDateText.isEmpty())
        	{
        		System.out.println("Start date valid and containsText is empty. Searching by start date");
				searchChannelCriteria(startDateText, "null", "null");
        	}
        	else if(validStart == true && !containsText.isEmpty() && endDateText.isEmpty())
        	{
        		System.out.println("Start date valid and containsText has data. Searching by start date and search text");
				searchChannelCriteria(startDateText, "null", containsText);
        	}
        	else if(validEnd == true && containsText.isEmpty() && startDateText.isEmpty())
        	{
        		System.out.println("End date valid and containsText is empty. Searching by end date");
				searchChannelCriteria("null", endDateText, "null");
        	}
        	else if(validEnd == true && !containsText.isEmpty() && startDateText.isEmpty())
        	{
        		System.out.println("End date valid and containsText has data. Searching by end date and search text");
				searchChannelCriteria("null", endDateText, containsText);
        	}
        	else if(validStart == true && validEnd == true)
        	{
        		if(Integer.valueOf(startDateText) > Integer.valueOf(endDateText))
        		{
        			System.out.println("Start date cannot be greater than the end date");
        		}
        		else
        		{
        			didDatesPass = true;
        		}
        		
        		if(didDatesPass == true)
        		{
        			if(containsText.isEmpty())
        			{
        				System.out.println("Both dates valid and containsText is empty. Searching by dates");
        				searchChannelCriteria(startDateText, endDateText, "null");
        			}
        			else
        			{
        				System.out.println("Both dates valid and containsText has data. Searching by dates and search text");
        				searchChannelCriteria(startDateText, endDateText, containsText);
        			}        			
        		}
        	} 
        }
    }
    
    public static String searchChannelCriteria(String startDate, String endDate, String searchText)
    {
    	System.out.println("Start Date: " + startDate);
    	System.out.println("End Date: " + endDate);
    	System.out.println("Search Text: " + searchText);
    	
    	//first check is to see if everything is null. If so, it copies the standard arraylists into the search ones
    	if(startDate.equals("null") && endDate.equals("null") && searchText.equals("null"))
    	{
    		SEARCHfirstMetadataIDs = new ArrayList(firstMetadataIDs);
    		SEARCHmessageIDs = new ArrayList(messageIDs);
    		SEARCHcontentTypes = new ArrayList(contentTypes);
    		SEARCHcontents = new ArrayList(contents);
    		SEARCHsecondMetadataIDs = new ArrayList(secondMetadataIDs);
    		SEARCHsecondMessageIDs = new ArrayList(secondMessageIDs);
    		SEARCHreceivedDates = new ArrayList(receivedDates);
    		SEARCHstatuses = new ArrayList(statuses);
    		SEARCHconnectorNames = new ArrayList(connectorNames);
    		SEARCHsentDates = new ArrayList(sentDates);
    		SEARCHresponseDates = new ArrayList(responseDates);
    		
    		//test for 2.2.6 message searching
    		validMsgIDs = new ArrayList(messageIDs);
    	}
    	
    	//2 then checks if startDate is valid. If not null, grabs all messages after the start date
    	if(!startDate.equals("null"))
    	{
    		for(int rdc=0;rdc<receivedDates.size();rdc++)
    		{
    			String[] splitTime = receivedDates.get(rdc).toString().split(" ");
    			String currentTime = splitTime[0].replace("-", "");
    			if(Integer.valueOf(currentTime) >= Integer.valueOf(startDate))
    			{    				
    				SEARCHfirstMetadataIDs.add(firstMetadataIDs.get(rdc)); 
    	    		SEARCHmessageIDs.add(messageIDs.get(rdc)); 
    	    		SEARCHcontentTypes.add(contentTypes.get(rdc)); 
    	    		SEARCHcontents.add(contents.get(rdc)); 
    				
    				SEARCHreceivedDates.add(receivedDates.get(rdc));    				
    				SEARCHsecondMetadataIDs.add(secondMetadataIDs.get(rdc));
    				SEARCHsecondMessageIDs.add(secondMessageIDs.get(rdc));
    				SEARCHstatuses.add(statuses.get(rdc));
    				SEARCHconnectorNames.add(connectorNames.get(rdc));
    				SEARCHsentDates.add(sentDates.get(rdc));
    				SEARCHresponseDates.add(responseDates.get(rdc));
    				
    				//test for 2.2.6 message searching
    				if(!validMsgIDs.contains(messageIDs.get(rdc)))
    				{
        	    		validMsgIDs.add(messageIDs.get(rdc));
    				}
    			}
    		}
    	}
    	
    	//then checks if the end date isn't null and if start date is
    	//if true, then it populates the ArrayLists for the first time
    	//if false, then it checks is startDate isn't null. If it's not, then 
    	if(!endDate.equals("null") && startDate.equals("null"))
    	{
    		for(int rdc=0;rdc<receivedDates.size();rdc++)
    		{
    			String[] splitTime = receivedDates.get(rdc).toString().split(" ");
    			String currentTime = splitTime[0].replace("-", "");
    			if(Integer.valueOf(currentTime) <= Integer.valueOf(endDate))
    			{    				
    				SEARCHfirstMetadataIDs.add(firstMetadataIDs.get(rdc)); 
    	    		SEARCHmessageIDs.add(messageIDs.get(rdc)); 
    	    		SEARCHcontentTypes.add(contentTypes.get(rdc)); 
    	    		SEARCHcontents.add(contents.get(rdc)); 
    				
    				SEARCHreceivedDates.add(receivedDates.get(rdc));    				
    				SEARCHsecondMetadataIDs.add(secondMetadataIDs.get(rdc));
    				SEARCHsecondMessageIDs.add(secondMessageIDs.get(rdc));
    				SEARCHstatuses.add(statuses.get(rdc));
    				SEARCHconnectorNames.add(connectorNames.get(rdc));
    				SEARCHsentDates.add(sentDates.get(rdc));
    				SEARCHresponseDates.add(responseDates.get(rdc));
    				
    				//test for 2.2.6 message searching
    				if(!validMsgIDs.contains(messageIDs.get(rdc)))
    				{
        	    		validMsgIDs.add(messageIDs.get(rdc));
    				}
    			}
    		}
    	}
    	else if(!endDate.equals("null") && !startDate.equals("null"))
    	{
    		//in this case, there is a start date and an end date. We will remove any results in the array before the end date
    		for(int rdc=0;rdc<SEARCHreceivedDates.size();rdc++)
    		{
    			String[] splitTime = SEARCHreceivedDates.get(rdc).toString().split(" ");
    			String currentTime = splitTime[0].replace("-", "");
    			if(Integer.valueOf(currentTime) >= Integer.valueOf(endDate))
    			{    				
    				SEARCHfirstMetadataIDs.remove(firstMetadataIDs.get(rdc)); 
    	    		SEARCHmessageIDs.remove(messageIDs.get(rdc)); 
    	    		SEARCHcontentTypes.remove(contentTypes.get(rdc)); 
    	    		SEARCHcontents.remove(contents.get(rdc)); 
    				
    				SEARCHreceivedDates.remove(receivedDates.get(rdc));    				
    				SEARCHsecondMetadataIDs.remove(secondMetadataIDs.get(rdc));
    				SEARCHsecondMessageIDs.remove(secondMessageIDs.get(rdc));
    				SEARCHstatuses.remove(statuses.get(rdc));
    				SEARCHconnectorNames.remove(connectorNames.get(rdc));
    				SEARCHsentDates.remove(sentDates.get(rdc));
    				SEARCHresponseDates.remove(responseDates.get(rdc));
    				
    				//test for 2.2.6 message searching
    				if(validMsgIDs.contains(messageIDs.get(rdc)))
    				{
        	    		validMsgIDs.remove(messageIDs.get(rdc));
    				}
    			}
    		}
    	}
    	
    	//final search that filters for text if any was entered
    	if(!searchText.isEmpty())
    	{
    		System.out.println("Searching by text");
    		//checks if one of the dates is not empty. If not it will filter off of currently captured information. 
    		if(!endDate.equals("null") || !startDate.equals("null"))
    		{
    			for(int mts=0;mts<SEARCHcontents.size();mts++)
    			{
    				String currentMessage = SEARCHcontents.get(mts).toString();
    				if(currentMessage.contains(searchText.toUpperCase()) && !SEARCHimportantIDs.contains(SEARCHmessageIDs.get(mts)))
    				{
    					SEARCHimportantIDs.add(SEARCHmessageIDs.get(mts));
    				}
    				
    				//test for 2.2.6 message searching
    				if(currentMessage.contains(searchText.toUpperCase()) && !validMsgIDs.contains(messageIDs.get(mts)))
    				{
        	    		validMsgIDs.add(messageIDs.get(mts));
    				}
    			}
    		}
    		else
    		{
    			//if the dates are empty, it will search the whole channel fresh for the containsText
    			System.out.println("Both dates are empty");
    			for(int msg=0;msg<contents.size();msg++)
    			{
    				String currentMessage = contents.get(msg).toString().toUpperCase();
    				if(currentMessage.contains(searchText.toUpperCase()) && !SEARCHimportantIDs.contains(messageIDs.get(msg)))
    				{
    					SEARCHimportantIDs.add(messageIDs.get(msg));
    				}
    				
    				//test for 2.2.6 message searching
    				if(currentMessage.contains(searchText.toUpperCase()) && !validMsgIDs.contains(messageIDs.get(msg)))
    				{
        	    		validMsgIDs.add(messageIDs.get(msg));
    				}
    			}
    		}
    	} 	
    	
    	//if SEARCHimportantIDs is not null, it will remove all messages from the arraylists that are not
    	if(!SEARCHimportantIDs.isEmpty())
    	{    		
    		//checks if any of the other arrays have been populated
    		if(!SEARCHfirstMetadataIDs.isEmpty())
    		{
    			System.out.println("A date field was populated");
    			for(int idc=0;idc<SEARCHimportantIDs.size();idc++)
    			{    			
    				String currentMID = SEARCHmessageIDs.get(idc).toString();
    				for(int idk=0;idk<SEARCHsecondMessageIDs.size();idk++)
    				{
        				//removes the data if the ID isn't in the ID list
        				if(!SEARCHimportantIDs.contains(currentMID))
        				{
        					SEARCHfirstMetadataIDs.remove(idc); 
            	    		SEARCHmessageIDs.remove(idc); 
            	    		SEARCHcontentTypes.remove(idc); 
            	    		SEARCHcontents.remove(idc); 
            				
            				SEARCHreceivedDates.remove(idk);    				
            				SEARCHsecondMetadataIDs.remove(idk);
            				SEARCHsecondMessageIDs.remove(idk);
            				SEARCHstatuses.remove(idk);
            				SEARCHconnectorNames.remove(idk);
            				SEARCHsentDates.remove(idk);
            				SEARCHresponseDates.remove(idk);
        				}
    				}    
    			}
    		}
    		else
    		{
    			System.out.println("No date field was populated");
    			//this is for if both dates were null, it does the population of the SEARCH Arrays here
    			for(int idc=0;idc<messageIDs.size();idc++)
    			{
    				String currentMID = messageIDs.get(idc).toString();
    				for(int idk=0;idk<SEARCHimportantIDs.size();idk++)
    				{
    					//removes the data if the ID isn't in the ID list
        				if(secondMessageIDs.get(idk).equals(currentMID))
        				{
        					SEARCHfirstMetadataIDs.add(firstMetadataIDs.get(idc)); 
            	    		SEARCHmessageIDs.add(messageIDs.get(idc)); 
            	    		SEARCHcontentTypes.add(contentTypes.get(idc)); 
            	    		SEARCHcontents.add(contents.get(idc)); 
            				
            				SEARCHreceivedDates.add(receivedDates.get(idk));    				
            				SEARCHsecondMetadataIDs.add(secondMetadataIDs.get(idk));
            				SEARCHsecondMessageIDs.add(secondMessageIDs.get(idk));
            				SEARCHstatuses.add(statuses.get(idk));
            				SEARCHconnectorNames.add(connectorNames.get(idk));
            				SEARCHsentDates.add(sentDates.get(idk));
            				SEARCHresponseDates.add(responseDates.get(idk));
        				}
    				}
    			}
    		}
    	}
    	
    	System.out.println("================= Array Sizes Check =================");
    	System.out.println("firstMetadataIDs.size()       = " + firstMetadataIDs.size());
    	System.out.println("messageIDs.size()             = " + messageIDs.size());
    	System.out.println("contentTypes.size()           = " + contentTypes.size());
    	System.out.println("contents.size()               = " + contents.size());
    	System.out.println("secondMetadataIDs.size()      = " + secondMetadataIDs.size());
    	System.out.println("secondMessageIDs.size()       = " + secondMessageIDs.size());
    	System.out.println("receivedDates.size()          = " + receivedDates.size());
    	System.out.println("statuses.size()               = " + statuses.size());
    	System.out.println("connectorNames.size()         = " + connectorNames.size());
    	System.out.println("sentDates.size()              = " + sentDates.size());
    	System.out.println("responseDates.size()          = " + responseDates.size());
    	System.out.println("SEARCHfirstMetadataIDs.size() = " + SEARCHfirstMetadataIDs.size());
    	System.out.println("SEARCHmessageIDs.size()       = " + SEARCHmessageIDs.size());
    	System.out.println("SEARCHcontentTypes.size()     = " + SEARCHcontentTypes.size());
    	System.out.println("SEARCHcontents.size()         = " + SEARCHcontents.size());
    	System.out.println("SEARCHsecondMetadataIDs.size()= " + SEARCHsecondMetadataIDs.size());
    	System.out.println("SEARCHsecondMessageIDs.size() = " + SEARCHsecondMessageIDs.size());
    	System.out.println("SEARCHreceivedDates.size()    = " + SEARCHreceivedDates.size());
    	System.out.println("SEARCHstatuses.size()         = " + SEARCHstatuses.size());
    	System.out.println("SEARCHconnectorNames.size()   = " + SEARCHconnectorNames.size());
    	System.out.println("SEARCHsentDates.size()        = " + SEARCHsentDates.size());
    	System.out.println("SEARCHresponseDates.size()    = " + SEARCHresponseDates.size());
    	System.out.println("SEARCHimportantIDs.size()     = " + SEARCHimportantIDs.size());
    	System.out.println("validMsgIDs.size()            = " + validMsgIDs.size());
    	System.out.println("=====================================================");
    	
    	//Experimental 2.2.6 search function
    	
    	SEARCHfirstMetadataIDs.clear();
		SEARCHmessageIDs.clear();
		SEARCHcontentTypes.clear();
		SEARCHcontents.clear();
		SEARCHsecondMetadataIDs.clear();
		SEARCHsecondMessageIDs.clear();
		SEARCHreceivedDates.clear();
		SEARCHstatuses.clear();
		SEARCHconnectorNames.clear();
		SEARCHsentDates.clear();
		SEARCHresponseDates.clear();
    	
    	//iterates through all captured message IDs
    	for(int firstSearch=0;firstSearch<validMsgIDs.size();firstSearch++)
    	{
    		String currentIDForSearch = validMsgIDs.get(firstSearch).toString();
    		//First iteration over the entire first messageIDs ArrayList, capturing the right information
    		for(int firstSubSearch=0;firstSubSearch<messageIDs.size();firstSubSearch++)
    		{
    			if(currentIDForSearch.equals(messageIDs.get(firstSubSearch).toString()))
    			{
    				SEARCHfirstMetadataIDs.add(firstMetadataIDs.get(firstSubSearch));
    	            SEARCHmessageIDs.add(messageIDs.get(firstSubSearch));
    	            SEARCHcontentTypes.add(contentTypes.get(firstSubSearch));
    	            SEARCHcontents.add(contents.get(firstSubSearch));
    			}
    		}
    		
    		for(int secondSubSearch=0;secondSubSearch<secondMessageIDs.size();secondSubSearch++)
    		{
    			if(currentIDForSearch.equals(secondMessageIDs.get(secondSubSearch).toString()))
    			{
    				SEARCHsecondMetadataIDs.add(secondMetadataIDs.get(secondSubSearch));
    	            SEARCHsecondMessageIDs.add(secondMessageIDs.get(secondSubSearch));
    	            SEARCHreceivedDates.add(receivedDates.get(secondSubSearch));
    	            SEARCHstatuses.add(statuses.get(secondSubSearch));
    	            SEARCHconnectorNames.add(connectorNames.get(secondSubSearch));
    	            SEARCHsentDates.add(sentDates.get(secondSubSearch));
    	            SEARCHresponseDates.add(responseDates.get(secondSubSearch));
    			}
    		}
    	}
    	
    	//function call to re display the search results
    	loadDataChannelsSearch();
    	
    	return "Search Complete";
    }
    
    public static boolean isValidDateInput(String input, String whichOne)
    {
        //1. Check if string is empty
        if (input == null || input.isEmpty()) 
        {
            System.out.println("Input is empty.");
            return false;
        }

        //2. Check if the string matches exactly 8 digits (YYYYMMDD)
        if (!input.matches("^\\d{8}$")) 
        {
            System.out.println("Input does not match required format (YYYYMMDD).");
            return false;
        }
        
        return true;
    }
    
    private static void loadDataChannelsSearch() 
    {
    	DefaultTableModel model = (DefaultTableModel) table.getModel();
    	model.setRowCount(0);
    	
    	model = new DefaultTableModel();
    	model.addColumn("MESSAGE_ID");
    	model.addColumn("RECEIVED_DATE");
    	model.addColumn("SEND_DATE");
    	model.addColumn("RESPONSE_DATE");
    	model.addColumn("CONTENT");
    	model.addColumn("CONTENT_TYPE");
    	model.addColumn("CONNECTOR_NAME");
    	
    	boolean runme = true;
    	for(int blah=0; blah<validMsgIDs.size();blah++)
    	{
    		if(runme == true)
    		{
    			System.out.println("FOUND #" + (blah+1) + ": " + validMsgIDs.get(blah));
    		}    		
    	}
    	
    	for(int display=0;display<SEARCHmessageIDs.size();display++)
    	{
    		Object[] rowData = new Object[7];
    		
    		for(int messagesTwo=0;messagesTwo<SEARCHsecondMessageIDs.size();messagesTwo++)
    		{
    			if(SEARCHmessageIDs.get(display).equals(SEARCHsecondMessageIDs.get(messagesTwo)) && SEARCHfirstMetadataIDs.get(display).equals(SEARCHsecondMetadataIDs.get(messagesTwo)))
    			{
    				rowData[0] = SEARCHmessageIDs.get(display);
    				rowData[1] = SEARCHreceivedDates.get(messagesTwo);
    	    		rowData[2] = SEARCHsentDates.get(messagesTwo);
    	    		rowData[3] = SEARCHresponseDates.get(messagesTwo);
    	    		rowData[4] = SEARCHcontents.get(display);
    	    		rowData[5] = contentTypeConvert(Integer.valueOf((String) SEARCHcontentTypes.get(display)));
    	    		rowData[6] = SEARCHconnectorNames.get(messagesTwo);
    			}
    		}    		
    		model.addRow(rowData);
    	}
    	
    	table.setModel(model);
        System.out.println("Number of rows in model: " + model.getRowCount());
    }
    
    private static class returnTableNames implements ActionListener
    {
        @Override
        public void actionPerformed(ActionEvent e)
        {
        	JOptionPane.showMessageDialog(SQLQueryName, "==========================Mirth Table Structure==========================\nNOTE: Tables are for Mirth Versions >3.5.1\n\nThese are the standard table names in the Mirth database. Not all tables\nmay be included in this list\n\nTables:\n==========\n-ALERT\n-CHANNEL\n-CHANNEL_GROUP\n-CODE_TEMPLATE\n-CODE_TEMPLATE_LIBRARY\n-CONFIGURATION\n-DEBUGGER_USAGE\n-D_CHANNELS\n-EVENT\n-PERSON\n-PERSON_PASSWORD\n-PERSON_PREFERENCE\n-SCHEMA_INFO\n-SCRIPT");
        }
    }
}