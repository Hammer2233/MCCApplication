import java.awt.EventQueue;
import javax.swing.JFrame;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.awt.Insets;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;

import javax.swing.AbstractAction;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.JOptionPane;

public class applicationWindow extends JFrame implements ActionListener
{
    //declaring variables for the GUI
    public static JFrame frame = new JFrame("MCC-BETA");
    private JPanel mainAppBody;
    private static JLabel labelVersion;

    private static JButton archiveChannels;
    private static JButton fullMirthExport;
    private static JButton checkUsernameButton;
    private static JButton changeUNandPW;
    private static JButton changeBackupPath;
    private static JButton changeMirthDirPath;
    private static JButton exportChanInfo;
    private static JButton exportMirthConfigInfo;
    private JButton exitApplication;
    private static JPasswordField commandPWSpace;
    private JButton runCommand;
    private static JLabel cmdPWLabel;
    
    private static boolean toggleEnabled = false;

    private static JTextArea logTextArea;
    private JScrollPane logTextScroll;

    public static void main(String[] args)
    {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
              (new applicationWindow()).setVisible(true);
            }
          });
        System.out.println("Hello");
    }

    private applicationWindow()
    {
        guiBuilder();
    }

    private void guiBuilder()
    {
        setLayout(new BorderLayout());
        
        // button area. West of application
        setTitle("MCC-BETA");
        JPanel westPanel = new JPanel();
        JPanel fillerPanel = new JPanel();
        fillerPanel.setPreferredSize(new Dimension(100, 95));
        fillerPanel.setBackground(new java.awt.Color(214, 216, 233));
        
        //gridbag setup for top buttons
        GridBagLayout topButtonGridbag = new GridBagLayout();
        JPanel topButtonPanel = new JPanel();
        topButtonPanel.setLayout(topButtonGridbag);
        topButtonPanel.setBackground(new java.awt.Color(214, 216, 233));
        GridBagConstraints topGBC = new GridBagConstraints();

        topGBC.fill = GridBagConstraints.BOTH;
        topGBC.weightx = 1.0;
        topGBC.weighty = 1.0;
        topGBC.insets = new Insets(2, 2, 2, 2);

        //westPanel.setLayout(new FlowLayout(FlowLayout.LEFT)); //NEW
        westPanel.setLayout(new FlowLayout()); //NEW

        topGBC.gridx = 0;
        topGBC.gridy = 0;
        topGBC.gridwidth = 1;  // This button occupies 1 column
        topGBC.gridheight = 1; // This button occupies 1 row
        archiveChannels = new JButton("EXPORT CHANNELS");
        archiveChannels.addActionListener(new backupChannels());
        archiveChannels.setForeground(new java.awt.Color(0,196,3));
        archiveChannels.setBackground(new java.awt.Color(238, 255, 243));
        topButtonPanel.add(archiveChannels, topGBC);
        //westPanel.add(archiveChannels);

        topGBC.gridx = 1;
        exportChanInfo = new JButton("[?]");
        //exportChanInfo.setFont(new Font("Arial", Font.PLAIN, 15));
        exportChanInfo.addActionListener(new returnExportChannelInfo());
        topButtonPanel.add(exportChanInfo, topGBC);

        topGBC.gridx = 0; // Move back to the first column
        topGBC.gridy = 1; // Move to the second row
        fullMirthExport = new JButton("EXPORT MIRTH CONFIG");
        fullMirthExport.addActionListener(new createFullMirthBackup());
        fullMirthExport.setForeground(new java.awt.Color(205, 0, 224));
        fullMirthExport.setBackground(new java.awt.Color(253, 238, 255));
        topButtonPanel.add(fullMirthExport, topGBC);
        //westPanel.add(fullMirthExport);

        topGBC.gridx = 1;
        exportMirthConfigInfo = new JButton("[?]");
        exportMirthConfigInfo.addActionListener(new returnMirthConfigExportInfo());
        topButtonPanel.add(exportMirthConfigInfo, topGBC);

        westPanel.add(topButtonPanel);

        checkUsernameButton = new JButton("CHECK UN");
        checkUsernameButton.addActionListener(new checkUsername());
        checkUsernameButton.setForeground(new java.awt.Color(0, 165, 224));
        checkUsernameButton.setBackground(new java.awt.Color(238, 252, 255));
        westPanel.add(checkUsernameButton);

        changeUNandPW = new JButton("RESET UN/PW");
        changeUNandPW.addActionListener(new resetUNandPW());
        changeUNandPW.setForeground(new java.awt.Color(0, 165, 224));
        changeUNandPW.setBackground(new java.awt.Color(238, 252, 255));
        westPanel.add(changeUNandPW);

        changeBackupPath = new JButton("CHANGE BACKUP PATH");
        changeBackupPath.addActionListener(new changeBackupDir());
        changeBackupPath.setForeground(new java.awt.Color(140, 93, 6));
        changeBackupPath.setBackground(new java.awt.Color(252, 233, 204));
        westPanel.add(changeBackupPath);

        changeMirthDirPath = new JButton("CHANGE MIRTH PATH");
        changeMirthDirPath.addActionListener(new changeMirthDBDir());
        changeMirthDirPath.setForeground(new java.awt.Color(140, 93, 6));
        changeMirthDirPath.setBackground(new java.awt.Color(252, 233, 204));
        westPanel.add(changeMirthDirPath);
        
        //added for spacing
        //westPanel.add(fillerPanel);

        //new JPanel and GridBagLayout for left-mid section of application
        JPanel bottomMidButtonsPanel = new JPanel();
        bottomMidButtonsPanel.setBackground(new java.awt.Color(214, 216, 233));
        bottomMidButtonsPanel.setLayout(topButtonGridbag);

        topGBC.gridx = 0;
        topGBC.gridy = 0;
        bottomMidButtonsPanel.add(fillerPanel, topGBC);

        topGBC.gridx = 0; // Move back to the first column
        topGBC.gridy = 1;
        exitApplication = new JButton("EXIT");
        exitApplication.setPreferredSize(new Dimension(215,27));
        exitApplication.addActionListener(new exitApplication());
        exitApplication.setForeground(new java.awt.Color(240,40,40));
        exitApplication.setBackground(new java.awt.Color(255, 238, 238));
        bottomMidButtonsPanel.add(exitApplication, topGBC);

        westPanel.add(bottomMidButtonsPanel);

        //new JPanel for the bottom containing the command area and 
        JPanel bottomButtonPanel = new JPanel();
        bottomButtonPanel.setBackground(new java.awt.Color(214, 216, 233));
        bottomButtonPanel.setLayout(topButtonGridbag);

        topGBC.gridx = 0;
        topGBC.gridy = 0;
        cmdPWLabel = new JLabel("PW/CMD:");
        bottomButtonPanel.add(cmdPWLabel, topGBC);

        topGBC.gridx = 1;
        topGBC.gridy = 0;
        commandPWSpace = new JPasswordField(14); 
        commandPWSpace.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke("ENTER"), "enterKeyPressed");
        commandPWSpace.getActionMap().put("enterKeyPressed", new AbstractAction() 
        {
            @Override
            public void actionPerformed(ActionEvent e) 
            {
                pwCMDEntered(commandPWSpace.getText());
            }
        });

        bottomButtonPanel.add(commandPWSpace, topGBC);

        westPanel.add(bottomButtonPanel);
        
        westPanel.setBackground(new java.awt.Color(214, 216, 233));
        westPanel.setPreferredSize(new Dimension(230, 100)); // Preferred size for the panel

        add(westPanel, BorderLayout.WEST);

        //Log area (center)
        JPanel centerPanel = new JPanel();
        logTextArea = new JTextArea(5,10);
        logTextArea.setLineWrap(true);
        logTextArea.setEditable(false);

        logTextScroll = new JScrollPane(logTextArea);
        logTextScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        logTextScroll.setPreferredSize(new Dimension(820,320));
        
        centerPanel.setVisible(true);
        centerPanel.add(logTextScroll);
        centerPanel.setPreferredSize(new Dimension(830, 330));
        add(centerPanel, BorderLayout.CENTER);
        
        //Final declarations
        toggleButtons(toggleEnabled);
        pack();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) 
    {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'actionPerformed'");
    }
    
    //button action listeners

    private static class backupChannels implements ActionListener
    {
        @Override
        public void actionPerformed(ActionEvent e)
        {
            Object[] options = { "YES - INCLUDE CodeTemplates", "NO - EXPORT CHANNEL ONLY" };
            int exportCTLsTF = JOptionPane.showOptionDialog(labelVersion, "Export Mirth Channels\nIncluding Code Templates requires extra clicks when importing channels into Mirth.\nIf you choose no, be sure to import the code template library manually\nInclude the Code Templates in the channel export?", "EXPORT MIRTH CHANNEL", 0, 2, null, options, options[1]);
            if (exportCTLsTF == 0)
            {
                channelExport.includeCodeTemplates("YES");
                runChannelExport();
            }
            else if(exportCTLsTF == 1)
            {
                channelExport.includeCodeTemplates("NO");
                runChannelExport();
            }
            channelExport.includeCodeTemplates("NO");
        }
    }

    public static String runChannelExport()
    {
    	String serviceState = Main.checkMirthService();
    	if(serviceState == "STOPPED")
    	{
    		Main.setBackupFolder();
            channelExport.isFullMirthExportCheck("NO");
            System.out.println("PERFORMING CHANNEL BACKUP");
            String host = Main.returnHost();
            channelExport exportChannels = new channelExport();
            channelExport.exportChannels(host);
            channelExport exportMetadata = new channelExport();
            try 
            {
                channelExport.exportMetadata(host);
            } 
            catch (FileNotFoundException e1) 
            {
                e1.printStackTrace();
            }
            logCommands.returnArchivedChannels(Main.getBackupFolder()+"\\channelBackup\\");
            setLogWindow();
            Main.deleteBuildingBlockFiles(); //RE-ENABLE ME: 
    	}
    	else if(serviceState == "STARTED")
    	{
    		logCommands.exportToLog("Mirth Service is not stopped. Please stop the service to continue");
    		JOptionPane.showMessageDialog(labelVersion, "Mirth Service is not stopped.\nPlease stop the service to continue");
    	}
    	else
    	{
    		logCommands.exportToLog("Mirth Service error encountered. Please ensure the service is installed");
    	}
    	
        return "exported channels";
    }

    private static class createFullMirthBackup implements ActionListener
    {
        @Override
        public void actionPerformed(ActionEvent e)
        {
        	String serviceState = Main.checkMirthService();
        	if(serviceState == "STOPPED")
        	{
                channelExport.clearChannelFolder();
                Main.setBackupFolder();
                logCommands.exportToLog("PERFORMING FULL MIRTH CONFIGURATION EXPORT");
                channelExport.isFullMirthExportCheck("YES");
                
                String host = Main.returnHost();
                channelExport exportChannels = new channelExport();
                channelExport.exportChannels(host);
                channelExport exportMetadata = new channelExport();
                try 
                {
                    channelExport.exportMetadata(host);
                } 
                catch (FileNotFoundException e1) 
                {
                    e1.printStackTrace();
                }
                channelExport.isFullMirthExportCheck("NO");
                logCommands.exportToLog("EXPORTED - Channels and Metadata");

                try 
                {
                    fullConfigExport.exportChannelGroups(host);
                } 
                catch (SQLException e1) 
                {
                    e1.printStackTrace();
                }
                //setLogWindow();
                catch (FileNotFoundException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
                Main.deleteBuildingBlockFiles(); //RE-ENABLE ME: 
                logCommands.exportToLog("Full configuration exported to: " + Main.getBackupFolder() +"fullExport");
        	}
        	else if(serviceState == "STARTED")
        	{
        		logCommands.exportToLog("Mirth Service is not stopped. Please stop the service to continue");
        		JOptionPane.showMessageDialog(labelVersion, "Mirth Service is not stopped.\nPlease stop the service to continue");
        	}
        	else
        	{
        		logCommands.exportToLog("Mirth Service error encountered. Please ensure the service is installed");
        	}
        }
    }

    public static String setLogWindow()
    {
        String fullLog = logCommands.exportCurrentLogToWindow();
        logTextArea.setText(fullLog);
        return "";
    }

    private static class checkUsername implements ActionListener
    {
        @Override
        public void actionPerformed(ActionEvent e)
        {
            logCommands.exportToLog("Running Query for current Mirth username");
            String host = Main.returnHost();
            String currentUsername = SQLCommand.checkUN(host);
            logCommands.exportToLog("CURRENT USERNAME: " + currentUsername);
            JOptionPane.showMessageDialog(labelVersion, "Username is: '" + currentUsername + "'");
        }
    }

    private static class changeBackupDir implements ActionListener
    {
        @Override
        public void actionPerformed(ActionEvent e)
        {
            logCommands.exportToLog("Changing Mirth backup directory");
            String newPath = getOverrideDir("Change Mirth Export Backup Destination:");
            if(newPath == "NO CHANGES MADE")
            {
                logCommands.exportToLog("NO CHANGES MADE TO BACKUP DIRECTORY");
            }
            else
            {
                logCommands.exportToLog("Backup Path Changed: " + newPath);
                Main.setChangedBackup(newPath);
            }
        }
    }

    public static String getOverrideDir(String Dialog) 
    {
      String folderPath;
      JFileChooser changeFolder = new JFileChooser("C:\\");
      changeFolder.setDialogTitle(Dialog);
      changeFolder.setFileSelectionMode(1);
      changeFolder.setAcceptAllFileFilterUsed(false);
      if (changeFolder.showOpenDialog(labelVersion) == 0) 
      {
        folderPath = changeFolder.getSelectedFile().getAbsolutePath() + "\\";
        //Main.setChangedBackup(folderPath);
        return folderPath;
      } 
      else 
      {
        folderPath = "";
        return "NO CHANGES MADE";
      }
    }

    private static class changeMirthDBDir implements ActionListener
    {
        @Override
        public void actionPerformed(ActionEvent e)
        {
            logCommands.exportToLog("Changing Target Mirth database directory");
            Object[] options = { "CONTINUE", "CANCEL" };
            int changeMirthDBLocation = JOptionPane.showOptionDialog(labelVersion, "When choosing the new DB directory\ndo not select the 'mirthdb' folder.", "EXPORT MIRTH CHANNEL", 0, 2, null, options, options[1]);
            if (changeMirthDBLocation == 0)
            {
                System.out.println("ACKd");
                String newPath = getOverrideDir("Change target Mirth database Destination:");
                if(newPath == "NO CHANGES MADE")
                {
                    logCommands.exportToLog("NO CHANGES MADE TO TARGET DB DIRECTORY");
                }
                else
                {
                    logCommands.exportToLog("Target DB Path Changed: " + newPath);
                    Main.setChangedMirthDB(newPath);
                }
            }
            else if(changeMirthDBLocation == 1)
            {
                System.out.println("CANCELLED");
                logCommands.exportToLog("NO CHANGES MADE TO TARGET DB DIRECTORY");
            }
        }
    }

    private static class resetUNandPW implements ActionListener
    {
        @Override
        public void actionPerformed(ActionEvent e)
        {
            String host = Main.returnHost();
            logCommands.exportToLog("Changing Mirth username and password to defaults");
            Object[] options = { "CONTINUE", "CANCEL" };
            int changeMirthUN = JOptionPane.showOptionDialog(labelVersion, "Reset Mirth USERNAME and PASSWORD\nNOTES:\n -This will reset to 'admin' defaults\n -Program must be ran as admin via the batch file", "CHANGE MIRTH UN/PW", 0, 2, null, options, options[1]);
        	String serviceState = Main.checkMirthService();
        	if(serviceState == "STOPPED")
        	{
                if (changeMirthUN == 0)
                {
                    System.out.println("CHOSE TO CHANGE PW");
                    SQLCommand.resetUsernamePassword(host);
                    // logCommands.exportToLog("Reset username to 'admin'");
                    // JOptionPane.showMessageDialog(labelVersion, "Username reset to 'admin'");
                }
                else if(changeMirthUN == 1)
                {
                    System.out.println("NO CHANGE TO PW");
                    logCommands.exportToLog("Username and Password NOT changed");
                }
        	}
        	else if(serviceState == "STARTED")
        	{
        		logCommands.exportToLog("Mirth Service is not stopped. Please stop the service to continue");
        		JOptionPane.showMessageDialog(labelVersion, "Mirth Service is not stopped.\nPlease stop the service to continue");
        	}
        	else
        	{
        		logCommands.exportToLog("Mirth Service error encountered. Please ensure the service is installed");
        	}

        }
    }

    private static class returnExportChannelInfo implements ActionListener
    {
        @Override
        public void actionPerformed(ActionEvent e)
        {
            JOptionPane.showMessageDialog(labelVersion, "Exporting channels gives you 2 export options:\n\n1. Exporting WITH Code Template Libraries.\n  -Including code templates requires extra clicks during the channel import portion of the recovery\n  -Exports separate XML copies of CodeTemplate Libraries to the backup directory\n\n2. Export WITHOUT Code Template Libraries\n  -Offers a faster channel import. Be sure to import CodeTemplate Libraries manually\n  NOTE: ChannelMetadata such as enabled/disabled, pruning settings, etc. will still be included");
        }
    }

    private static class returnMirthConfigExportInfo implements ActionListener
    {
        @Override
        public void actionPerformed(ActionEvent e)
        {
            JOptionPane.showMessageDialog(labelVersion, "Exporting Mirth Configurations will include:\n\n1. Channel exports WITHOUT CodeTemplate Libraries\n2. Complete CodeTemplate Library exports\n3. 1 full Mirth Configuration XML file\n\nTIP: Run an 'EXPORT CHANNELS' after the 'EXPORT MIRTH CONFIG'\n        for a complete exported Mirth database backup");
        }
    }

    private static class exitApplication implements ActionListener
    {
        @Override
        public void actionPerformed(ActionEvent e)
        {
            logCommands.exportToLog("EXITING APPLICATION");
            System.exit(0);

        }
    }

    private static String pwCMDEntered(String currentText)
    {
        String captured = currentText;
        System.out.println("CAPTURED TEXT: " + captured);
        commandPWSpace.setText("");

        String[] validCommands = {getMCCPassword(), "lock"};
        for(int i=0;i<validCommands.length;i++)
        {
            if(captured.toLowerCase().equals(validCommands[i]) && i==0)
            {
                System.out.println("I MATCHED PASSWORD");
                toggleEnabled = true;
                toggleButtons(toggleEnabled);
                logCommands.exportToLog("Password correct - Enabling buttons");
            }
            if(captured.toLowerCase().equals(validCommands[i]) && i==1)
            {
            	System.out.println("LOCKING BUTTONS");
            	logCommands.exportToLog("LOCKING BUTTONS");
            	toggleEnabled = false;
            	toggleButtons(toggleEnabled);
            }
        }

        return "Enter Was Pressed";
    }

    private static String getMCCPassword()
    {
        String currentPassword = "";

        SimpleDateFormat dateFormat;
        Date currentDate = new Date();
        dateFormat = new SimpleDateFormat("d");
        int currDayOfMonth = Integer.valueOf(dateFormat.format(currentDate))+5;

        currentPassword = currDayOfMonth+"test";
        System.out.println("currentPassword: " + currentPassword);

        return currentPassword;
    }
    
    private static String toggleButtons(boolean toggleEnabled)
    {
    	archiveChannels.setEnabled(toggleEnabled);
    	fullMirthExport.setEnabled(toggleEnabled);
    	checkUsernameButton.setEnabled(toggleEnabled);
    	changeUNandPW.setEnabled(toggleEnabled);
    	changeBackupPath.setEnabled(toggleEnabled);
    	changeMirthDirPath.setEnabled(toggleEnabled);
    	exportChanInfo.setEnabled(toggleEnabled);
    	exportMirthConfigInfo.setEnabled(toggleEnabled);
    	return "disabled";
    }
    
}
