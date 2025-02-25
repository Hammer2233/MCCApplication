import java.awt.EventQueue;
import javax.swing.JFrame;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Scanner;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;

import javax.swing.GroupLayout;
import java.awt.Desktop;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.Border;
import javax.swing.JOptionPane;

public class applicationWindow extends JFrame implements ActionListener
{
    //declaring variables for the GUI
    public static JFrame frame = new JFrame("MCC-BETA");
    private JPanel mainAppBody;
    private static JLabel labelVersion;

    private JButton archiveChannels;
    private JButton fullMirthExport;
    private JButton checkUsernameButton;
    private JButton changeBackupPath;
    private JButton changeMirthDirPath;

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
        archiveChannels = new JButton("EXPORT CHANNELS");
        archiveChannels.addActionListener(new backupChannels());
        westPanel.add(archiveChannels);

        fullMirthExport = new JButton("EXPORT MIRTH CONFIG");
        fullMirthExport.addActionListener(new createFullMirthBackup());
        westPanel.add(fullMirthExport);

        checkUsernameButton = new JButton("CHECK USERNAME");
        checkUsernameButton.addActionListener(new checkUsername());
        westPanel.add(checkUsernameButton);

        changeBackupPath = new JButton("CHANGE BACKUP PATH");
        changeBackupPath.addActionListener(new changeBackupDir());
        westPanel.add(changeBackupPath);

        changeMirthDirPath = new JButton("CHANGE MIRTH PATH");
        changeMirthDirPath.addActionListener(new changeMirthDBDir());
        westPanel.add(changeMirthDirPath);

        
        westPanel.setBackground(new java.awt.Color(214, 216, 233));
        westPanel.setPreferredSize(new Dimension(200, 100)); // Preferred size for the panel

        add(westPanel, BorderLayout.WEST);

        //Log area (center)
        JPanel centerPanel = new JPanel();
        logTextArea = new JTextArea(5,10);
        logTextArea.setLineWrap(true);
        logTextArea.setEditable(false);

        logTextScroll = new JScrollPane(logTextArea);
        logTextScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        logTextScroll.setPreferredSize(new Dimension(900,300));
        
        centerPanel.setVisible(true);
        centerPanel.add(logTextScroll);
        centerPanel.setPreferredSize(new Dimension(910, 310));
        add(centerPanel, BorderLayout.CENTER);
        
        //Final declarations
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
        return "exported channels";
    }

    private static class createFullMirthBackup implements ActionListener
    {
        @Override
        public void actionPerformed(ActionEvent e)
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
            int exportCTLsTF = JOptionPane.showOptionDialog(labelVersion, "When choosing the new DB directory\ndo not select the 'mirthdb' folder.", "EXPORT MIRTH CHANNEL", 0, 2, null, options, options[1]);
            if (exportCTLsTF == 0)
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
            else if(exportCTLsTF == 1)
            {
                System.out.println("CANCELLED");
            }
        }
    }
}
