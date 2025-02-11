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
import javax.swing.GroupLayout;
import java.awt.Desktop;

import javax.swing.JButton;
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

    private JButton archiveChannels;

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
        
        westPanel.setBackground(new java.awt.Color(214, 216, 233));
        westPanel.setPreferredSize(new Dimension(200, 100)); // Preferred size for the panel

        add(westPanel, BorderLayout.WEST);

        //Log area (center)
        JPanel centerPanel = new JPanel();
        logTextArea = new JTextArea(5,10);
        logTextArea.setLineWrap(true);

        logTextScroll = new JScrollPane(logTextArea);
        logTextScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        logTextScroll.setPreferredSize(new Dimension(900,300));

        centerPanel.setVisible(true);
        centerPanel.add(logTextScroll);
        centerPanel.setPreferredSize(new Dimension(910, 310));
        add(centerPanel, BorderLayout.CENTER);
        
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
            System.out.println("PERFORMING CHANNEL BACKUP");
            String host = Main.returnHost();
            channelExport exportChannels = new channelExport();
            channelExport.exportChannels(host);
            channelExport exportMetadata = new channelExport();
            try 
            {
                channelExport.exportMetadata(host);
            } catch (FileNotFoundException e1) 
            {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
            setLogWindow();
        }
    }

    public static String setLogWindow()
    {
        String fullLog = logCommands.exportCurrentLogToWindow();
        logTextArea.setText(fullLog);
        return "";
    }
}
