package de.haniibrahim.seriallogger;

import com.fazecast.jSerialComm.*;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.Toolkit;
import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

/**
 * *
 * "Virtual Serial Printer" simply logs the data stream received from a serial
 * interface to a GUI interface, the console and optionally to a file
 *
 * @author Hani Andreas Ibrahim
 * @version 0.5
 */
public class SerialLogger extends JFrame {

    static SerialPort chosenPort;
    private SerialReadTask serialReader;
    String portName;

    Preferences prefs;
    String stdLogfileName;
    PrintWriter pw;

    /**
     * Creates new form SerialPrinter
     */
    public SerialLogger() {
        // Set app icon
        this.setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("vsp.png")));
        initComponents();

        // Load prefs at startup and save at shutdown
        setPrefs();

        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    storePrefs();
                } catch (BackingStoreException ex) {

                }
            }
        }));

        // Set OK-Button to default
        this.getRootPane().setDefaultButton(bt_OpenPort);

        // Add Contextmenu
        ta_VirtualPrint.addMouseListener(new ContextMenuMouseListener());
        tf_Logfile.addMouseListener(new ContextMenuMouseListener());

        // Open and close button diabled till updatePortList finished
        bt_OpenPort.setEnabled(false);
        bt_ClosePort.setEnabled(false);

        // Handshake deactivated - NOT IMPLEMENTED YET
        lb_Handshake.setVisible(false);
        cb_Handshake.setVisible(false);

        // Initial values for CommPorts in combobox
        updatePortList();
    }

    private class SerialReadTask extends SwingWorker<Boolean, String> {

        @Override
        protected Boolean doInBackground() throws IOException {
            // Get values from the GUI
            portName = cb_Commport.getSelectedItem().toString();
            int baud = Integer.parseInt(cb_Baud.getSelectedItem().toString());
            int databits = Integer.parseInt(cb_DataBits.getSelectedItem().toString());
            String stopbit_s = cb_StopBits.getSelectedItem().toString();
            String parity_s = cb_Parity.getSelectedItem().toString();
//            String handshake_s = cb_Handshake.getSelectedItem().toString();

            // Stopbit parser
            int stopbits;
            if (stopbit_s.equals("1")) {
                stopbits = SerialPort.ONE_STOP_BIT;
            } else if (stopbit_s.equals("1.5")) {
                stopbits = SerialPort.ONE_POINT_FIVE_STOP_BITS;
            } else if (stopbit_s.equals("2")) {
                stopbits = SerialPort.TWO_STOP_BITS;
            } else {
                System.err.println("ERROR: No stopbits specified. Set to 1");
                stopbits = SerialPort.ONE_STOP_BIT;
            }

            // Parity-Parser
            int parity;
            if (parity_s.equals("none")) {
                parity = SerialPort.NO_PARITY;
            } else if (parity_s.equals("odd")) {
                parity = SerialPort.ODD_PARITY;
            } else if (parity_s.equals("even")) {
                parity = SerialPort.EVEN_PARITY;
            } else if (parity_s.equals("mark")) {
                parity = SerialPort.MARK_PARITY;
            } else if (parity_s.equals("space")) {
                parity = SerialPort.SPACE_PARITY;
            } else {
                System.err.println("ERROR: Parity not specified, set to NONE");
                parity = SerialPort.NO_PARITY;
            }

//                // Handshake-Parser - NOT IMPLEMENTED YET
//                int handshake;
//                if (handshake_s.equals("none")) {
//                    handshake = SerialPort.FLOW_CONTROL_DISABLED;
//                } else if (handshake_s.equals("RTS/CTS")) {
//                    handshake = SerialPort.FLOW_CONTROL_RTS_ENABLED;
//                } else if (handshake_s.equals("XON/XOFF")) {
//                    handshake = SerialPort.FLOW_CONTROL_XONXOFF_IN_ENABLED;
//                } else {
//                    System.err.println("ERROR: Handshake not specified, set to NONE");
//                    handshake = SerialPort.FLOW_CONTROL_DISABLED;;
//                }
            // Do not try to register an "empty" port 
            if (portName.equals("")) {
                System.err.println("ERROR: CommPort is empty!");
                JOptionPane.showMessageDialog(null,
                        "CommPort is empty",
                        "Error", JOptionPane.ERROR_MESSAGE);
                return true;
            }

            chosenPort = SerialPort.getCommPort(portName); // Register chosen port
            chosenPort.setComPortParameters(baud, databits, stopbits, parity); // Set serial parameters to the chosen port
            chosenPort.setComPortTimeouts(SerialPort.TIMEOUT_SCANNER, 0, 0);

            if (chosenPort.openPort()) {
                boolean logFlag;
                if (ck_Logfile.isSelected()) {
                    pw = new PrintWriter(new FileWriter(tf_Logfile.getText()));
                    logFlag = true;
                } else {
                    logFlag = false;
                }
                Scanner serialScanner = new Scanner(chosenPort.getInputStream());
//                while (!isCancelled()) {
                while (serialScanner.hasNextLine() || !isCancelled()) {
                    String line = serialScanner.nextLine();
                    publish(line);
                    if (logFlag) {
                        pw.println(line); // save to file
                    }
                }
//                }
                return false;
            } else {
                JOptionPane.showMessageDialog(null,
                        "Could not open serial port\n" + portName + "\n",
                        "Info", JOptionPane.ERROR_MESSAGE);
                System.err.println("Could not open port " + portName);
                return true;
            }
        }

        @Override
        protected void process(List<String> chunk) {
            if (ck_Logfile.isSelected()) { // Output on GUI, console AND logfile
                for (String line : chunk) {
                    ta_VirtualPrint.append(line + "\n"); // display in GUI
                    System.out.println(line); // print on console
                }
            } else { // Output on GUI and console only
                for (String line : chunk) {
                    ta_VirtualPrint.append(line + "\n");
                    System.out.println(line);
                }
            }
        }

        @Override
        protected void done() {
            try {
                if (get() == true) {
                    // Disable GUI elements
                    cb_Commport.setEnabled(true);
                    bt_Update.setEnabled(true);
                    cb_Baud.setEnabled(true);
                    cb_DataBits.setEnabled(true);
                    cb_StopBits.setEnabled(true);
                    cb_Parity.setEnabled(true);
                    cb_Handshake.setEnabled(true);
                    bt_OpenPort.setEnabled(true);
                    bt_ClosePort.setEnabled(false);
                    ck_Logfile.setEnabled(true);
                    tf_Logfile.setEnabled(true);
                    bt_Fileselector.setEnabled(true);
                }
            } catch (InterruptedException ex) {
                Logger.getLogger(SerialLogger.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ExecutionException ex) {
                Logger.getLogger(SerialLogger.class.getName()).log(Level.SEVERE, null, ex);
            } catch (CancellationException ex) { // important
                // do nothing, just catch CancellationException
            }
            if (ck_Logfile.isSelected()) {
                if (pw != null) { // if PrintWriter bw exists
                    pw.close();
                }
            }
        }
    }

    private void updatePortList() {
        SwingWorker<SerialPort[], Void> worker = new SwingWorker<SerialPort[], Void>() {
            @Override
            protected SerialPort[] doInBackground() throws Exception {
                SerialPort[] portNames = SerialPort.getCommPorts();
                return portNames;
            }

            @Override
            protected void done() {
                try {
                    SerialPort[] portNames = get();
                    cb_Commport.removeAllItems();

                    if (portNames.length == 0) { // If no serial ports installed
                        cb_Commport.addItem(""); // To avoid Nullpointer exception
                    } else {
                        for (SerialPort portName : portNames) {
                            cb_Commport.addItem(portName.getSystemPortName());
                        }
                    }
                    bt_OpenPort.setEnabled(true);

                } catch (InterruptedException ex) {
                    Logger.getLogger(SerialLogger.class
                            .getName()).log(Level.SEVERE, null, ex);

                } catch (ExecutionException ex) {
                    Logger.getLogger(SerialLogger.class
                            .getName()).log(Level.SEVERE, null, ex);
                }
            }
        };
        worker.execute();
    }

    private void storePrefs() throws BackingStoreException {
        // Get node
        prefs = Preferences.userNodeForPackage(getClass());

        // Save window position
        prefs.putInt("xpos", getLocation().x);
        prefs.putInt("ypos", getLocation().y);

        // Save Window size
        prefs.putInt("width", getSize().width);
        prefs.putInt("height", getSize().height);

        // Save serial parameters
        prefs.put("baud", cb_Baud.getSelectedItem().toString());
        prefs.put("databits", cb_DataBits.getSelectedItem().toString());
        prefs.put("stopbits", cb_StopBits.getSelectedItem().toString());
        prefs.put("parity", cb_Parity.getSelectedItem().toString());
//        prefs.put("handshake", cb_handshake.getSelectedItem().toString());

        // Save Logfile name
        if (tf_Logfile.getText().isEmpty()) {
            prefs.put("logfile", stdLogfileName);
        } else {
            prefs.put("logfile", tf_Logfile.getText());
        }
        prefs.putBoolean("logto", ck_Logfile.isSelected());

        prefs.flush();
    }

    private void setPrefs() {
        // Get node
        prefs = Preferences.userNodeForPackage(getClass());

        // Calculate screen-centered windows position
        final Dimension d = this.getToolkit().getScreenSize();
        int win_x = (int) ((d.getWidth() - this.getWidth()) / 2);
        int win_y = (int) ((d.getHeight() - this.getHeight()) / 2);

        // Set window position
        setLocation(prefs.getInt("xpos", win_x),
                prefs.getInt("ypos", win_y));

        // Set window size
        setSize(prefs.getInt("width", 637),
                prefs.getInt("height", 380));

        // Initial logfilename => ~/vsp.log
        stdLogfileName = System.getProperty("user.home")
                + System.getProperty("file.separator")
                + "vsp.log";

        // Set serial parameters and logfile name
        String baud = prefs.get("baud", "9600");
        String databits = prefs.get("databits", "8");
        String stopbits = prefs.get("stopbits", "1");
        String parity = prefs.get("parity", "none");
        String handshake = prefs.get("handshake", "none");
        String logfile = prefs.get("logfile", stdLogfileName);
        boolean logto = prefs.getBoolean("logto", false);

        // Put default/stored serialparameters/logfile in GUI
        cb_Baud.setSelectedItem(baud);
        cb_DataBits.setSelectedItem(databits);
        cb_StopBits.setSelectedItem(stopbits);
        cb_Parity.setSelectedItem(parity);
        cb_Handshake.setSelectedItem(handshake);
        tf_Logfile.setText(logfile);
        ck_Logfile.setSelected(logto);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lb_Commport = new javax.swing.JLabel();
        cb_Commport = new javax.swing.JComboBox();
        lb_Baud = new javax.swing.JLabel();
        cb_Baud = new javax.swing.JComboBox();
        lb_DataBits = new javax.swing.JLabel();
        cb_DataBits = new javax.swing.JComboBox();
        lb_StopBits = new javax.swing.JLabel();
        cb_StopBits = new javax.swing.JComboBox();
        lb_Parity = new javax.swing.JLabel();
        cb_Parity = new javax.swing.JComboBox();
        sp_VirtualPrint = new javax.swing.JScrollPane();
        ta_VirtualPrint = new javax.swing.JTextArea();
        bt_Update = new javax.swing.JButton();
        bt_ClosePort = new javax.swing.JButton();
        bt_Info = new javax.swing.JButton();
        lb_VirtalPrint = new javax.swing.JLabel();
        bt_OpenPort = new javax.swing.JButton();
        lb_Handshake = new javax.swing.JLabel();
        cb_Handshake = new javax.swing.JComboBox();
        ck_Logfile = new javax.swing.JCheckBox();
        tf_Logfile = new javax.swing.JTextField();
        bt_Fileselector = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Virtual Serial Printer");
        setLocationByPlatform(true);
        setMinimumSize(getPreferredSize());

        lb_Commport.setFont(lb_Commport.getFont());
        lb_Commport.setText("CommPort:");

        cb_Commport.setEditable(true);
        cb_Commport.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Update Ports ..." }));

        lb_Baud.setText("Baud:");

        cb_Baud.setEditable(true);
        cb_Baud.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "2400", "4800", "9600", "19200", "38400", "57600", "115200" }));

        lb_DataBits.setText("Data Bits:");

        cb_DataBits.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "5", "6", "7", "8" }));

        lb_StopBits.setText("Stop Bits:");

        cb_StopBits.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "1", "1.5", "2" }));

        lb_Parity.setText("Parity:");

        cb_Parity.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "none", "even", "odd", "mark", "space" }));

        ta_VirtualPrint.setEditable(false);
        ta_VirtualPrint.setColumns(20);
        ta_VirtualPrint.setRows(5);
        sp_VirtualPrint.setViewportView(ta_VirtualPrint);

        bt_Update.setText("Update");
        bt_Update.setToolTipText("Update CommPort list");
        bt_Update.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bt_UpdateActionPerformed(evt);
            }
        });

        bt_ClosePort.setText("Close Port");
        bt_ClosePort.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bt_ClosePortActionPerformed(evt);
            }
        });

        bt_Info.setText("Info");
        bt_Info.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bt_InfoActionPerformed(evt);
            }
        });

        lb_VirtalPrint.setFont(lb_VirtalPrint.getFont());
        lb_VirtalPrint.setText("Output:");

        bt_OpenPort.setText("Open Port");
        bt_OpenPort.setToolTipText("");
        bt_OpenPort.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bt_OpenPortActionPerformed(evt);
            }
        });

        lb_Handshake.setText("Handshake:");

        cb_Handshake.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "RTS/CTS", "XON/XOFF", "none" }));

        ck_Logfile.setText("Log to:");
        ck_Logfile.setToolTipText("Enable/Disable logging");

        bt_Fileselector.setText("...");
        bt_Fileselector.setToolTipText("Choose log file");
        bt_Fileselector.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bt_FileselectorActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(lb_Commport)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(cb_Commport, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(bt_Update, javax.swing.GroupLayout.PREFERRED_SIZE, 88, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(lb_VirtalPrint))
                        .addGap(0, 107, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(ck_Logfile)
                                .addGap(12, 12, 12)
                                .addComponent(tf_Logfile)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(bt_Fileselector, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(sp_VirtualPrint))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(bt_Info, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(bt_OpenPort, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(bt_ClosePort, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(lb_Baud, javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(lb_DataBits))
                                .addComponent(lb_StopBits))
                            .addComponent(lb_Handshake)
                            .addComponent(lb_Parity, javax.swing.GroupLayout.Alignment.TRAILING))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(cb_Handshake, 0, 1, Short.MAX_VALUE)
                            .addComponent(cb_Parity, 0, 1, Short.MAX_VALUE)
                            .addComponent(cb_StopBits, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(cb_DataBits, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(cb_Baud, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lb_Commport)
                    .addComponent(cb_Commport, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(bt_Update)
                    .addComponent(bt_Info))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lb_VirtalPrint)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(cb_Baud, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lb_Baud))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(cb_DataBits, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lb_DataBits))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(cb_StopBits, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lb_StopBits))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(cb_Parity, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lb_Parity))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(cb_Handshake, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lb_Handshake))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 139, Short.MAX_VALUE)
                        .addComponent(bt_OpenPort)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(bt_ClosePort)
                            .addComponent(tf_Logfile, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(bt_Fileselector)
                            .addComponent(ck_Logfile)))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(3, 3, 3)
                        .addComponent(sp_VirtualPrint)
                        .addGap(32, 32, 32)))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void bt_UpdateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bt_UpdateActionPerformed
        updatePortList();
    }//GEN-LAST:event_bt_UpdateActionPerformed

    private void bt_ClosePortActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bt_ClosePortActionPerformed
        if (chosenPort.isOpen()) {
            // Close serial port
            chosenPort.closePort();
//            System.out.println("Port " + portName + " closed");
        } else {
            JOptionPane.showMessageDialog(this,
                    "Port was not open",
                    "Error", JOptionPane.ERROR_MESSAGE);
            System.err.println("ERROR: Port was not open");
        }
        // Enable GUI elements
        cb_Commport.setEnabled(true);
        bt_Update.setEnabled(true);
        cb_Baud.setEnabled(true);
        cb_DataBits.setEnabled(true);
        cb_StopBits.setEnabled(true);
        cb_Parity.setEnabled(true);
        cb_Handshake.setEnabled(true);
        bt_OpenPort.setEnabled(true);
        bt_ClosePort.setEnabled(false);
        ck_Logfile.setEnabled(true);
        tf_Logfile.setEnabled(true);
        bt_Fileselector.setEnabled(true);

        // flag main swingworker class as cancel
        serialReader.cancel(true);
    }//GEN-LAST:event_bt_ClosePortActionPerformed

    private void bt_InfoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bt_InfoActionPerformed
        JOptionPane.showMessageDialog(this,
                "<html><span style=\"font-size:large;\"><b>Virtual Serial Printer</b></span></html>\n"
                + "Lists data received from a serial interface\n\n"
                + "(c) 2013 Hani Ibrahim <hani.ibrahim@gmx.de>\n\n",
                "Info", JOptionPane.INFORMATION_MESSAGE);
    }//GEN-LAST:event_bt_InfoActionPerformed

    private void bt_OpenPortActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bt_OpenPortActionPerformed
//        System.out.println("OpenPort gedrückt");

        // Clear textarea
        ta_VirtualPrint.setText("");

        // Check for consistent logging settings
        if ((ck_Logfile.isSelected() && tf_Logfile.getText().equals(""))) {
            JOptionPane.showMessageDialog(null,
                    "Logfile name is empty",
                    "Error", JOptionPane.ERROR_MESSAGE);
            System.err.println("Logfile name is empty");
        } else if (ck_Logfile.isSelected() && (new File(tf_Logfile.getText())).exists()) {
            JOptionPane.showMessageDialog(null,
                    "Logfile aready exists",
                    "Error", JOptionPane.ERROR_MESSAGE);
            System.err.println("ERROR: Logfile already exists");
        } else {
            // Disable GUI elements
            cb_Commport.setEnabled(false);
            bt_Update.setEnabled(false);
            cb_Baud.setEnabled(false);
            cb_DataBits.setEnabled(false);
            cb_StopBits.setEnabled(false);
            cb_Parity.setEnabled(false);
            cb_Handshake.setEnabled(false);
            bt_OpenPort.setEnabled(false);
            bt_ClosePort.setEnabled(true);
            ck_Logfile.setEnabled(false);
            tf_Logfile.setEnabled(false);
            bt_Fileselector.setEnabled(false);
            // Read from the serial interface
            serialReader = new SerialReadTask();
            serialReader.execute();
        }
    }//GEN-LAST:event_bt_OpenPortActionPerformed

    private void bt_FileselectorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bt_FileselectorActionPerformed
        FileDialog fd = new FileDialog(this, "Specify log file ...", FileDialog.SAVE);
        FilenameFilter filter = new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return (name.endsWith(".txt") || name.endsWith(".log"));
            }
        };
        fd.setFilenameFilter(filter);

        // Logfilename: Extract pathname (w/o filename) from tf_Logfile
        String oldLogfilePath = tf_Logfile.getText();
        if (oldLogfilePath.isEmpty()) { // if tf_Logfile is empty set standard logfile path
            oldLogfilePath = System.getProperty("user.home")
                    + System.getProperty("file.separator")
                    + "vsp.log";
        }
        String pathName = oldLogfilePath.substring(0, // Extract path w/o filename
                oldLogfilePath.lastIndexOf(System.getProperty("file.separator")) + 1);

        fd.setDirectory(pathName);  // Set default path to last selected path
        fd.setVisible(true);
        String newLogfilePath = fd.getDirectory() + fd.getFile();
        if (fd.getFile() != null) {
            tf_Logfile.setText(newLogfilePath);
        }
    }//GEN-LAST:event_bt_FileselectorActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {

        // Try GTK+ Look and Feel first. If fails use System LaF
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        try {
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.gtk.GTKLookAndFeel");
        } catch (ClassNotFoundException ex) {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

            } catch (ClassNotFoundException ex1) {
                Logger.getLogger(SerialLogger.class
                        .getName()).log(Level.SEVERE, null, ex1);

            } catch (InstantiationException ex1) {
                Logger.getLogger(SerialLogger.class
                        .getName()).log(Level.SEVERE, null, ex1);

            } catch (IllegalAccessException ex1) {
                Logger.getLogger(SerialLogger.class
                        .getName()).log(Level.SEVERE, null, ex1);

            } catch (UnsupportedLookAndFeelException ex1) {
                Logger.getLogger(SerialLogger.class
                        .getName()).log(Level.SEVERE, null, ex1);
            }
        } catch (InstantiationException ex) {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

            } catch (ClassNotFoundException ex1) {
                Logger.getLogger(SerialLogger.class
                        .getName()).log(Level.SEVERE, null, ex1);

            } catch (InstantiationException ex1) {
                Logger.getLogger(SerialLogger.class
                        .getName()).log(Level.SEVERE, null, ex1);

            } catch (IllegalAccessException ex1) {
                Logger.getLogger(SerialLogger.class
                        .getName()).log(Level.SEVERE, null, ex1);

            } catch (UnsupportedLookAndFeelException ex1) {
                Logger.getLogger(SerialLogger.class
                        .getName()).log(Level.SEVERE, null, ex1);
            }
        } catch (IllegalAccessException ex) {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

            } catch (ClassNotFoundException ex1) {
                Logger.getLogger(SerialLogger.class
                        .getName()).log(Level.SEVERE, null, ex1);

            } catch (InstantiationException ex1) {
                Logger.getLogger(SerialLogger.class
                        .getName()).log(Level.SEVERE, null, ex1);

            } catch (IllegalAccessException ex1) {
                Logger.getLogger(SerialLogger.class
                        .getName()).log(Level.SEVERE, null, ex1);

            } catch (UnsupportedLookAndFeelException ex1) {
                Logger.getLogger(SerialLogger.class
                        .getName()).log(Level.SEVERE, null, ex1);
            }
        } catch (UnsupportedLookAndFeelException ex) {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

            } catch (ClassNotFoundException ex1) {
                Logger.getLogger(SerialLogger.class
                        .getName()).log(Level.SEVERE, null, ex1);

            } catch (InstantiationException ex1) {
                Logger.getLogger(SerialLogger.class
                        .getName()).log(Level.SEVERE, null, ex1);

            } catch (IllegalAccessException ex1) {
                Logger.getLogger(SerialLogger.class
                        .getName()).log(Level.SEVERE, null, ex1);

            } catch (UnsupportedLookAndFeelException ex1) {
                Logger.getLogger(SerialLogger.class
                        .getName()).log(Level.SEVERE, null, ex1);
            }
        }
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new SerialLogger().setVisible(true);
            }
        });
    }
    //<editor-fold defaultstate="collapsed" desc=" GUI variables declaration ">
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton bt_ClosePort;
    private javax.swing.JButton bt_Fileselector;
    private javax.swing.JButton bt_Info;
    private javax.swing.JButton bt_OpenPort;
    private javax.swing.JButton bt_Update;
    private javax.swing.JComboBox cb_Baud;
    private javax.swing.JComboBox cb_Commport;
    private javax.swing.JComboBox cb_DataBits;
    private javax.swing.JComboBox cb_Handshake;
    private javax.swing.JComboBox cb_Parity;
    private javax.swing.JComboBox cb_StopBits;
    private javax.swing.JCheckBox ck_Logfile;
    private javax.swing.JLabel lb_Baud;
    private javax.swing.JLabel lb_Commport;
    private javax.swing.JLabel lb_DataBits;
    private javax.swing.JLabel lb_Handshake;
    private javax.swing.JLabel lb_Parity;
    private javax.swing.JLabel lb_StopBits;
    private javax.swing.JLabel lb_VirtalPrint;
    private javax.swing.JScrollPane sp_VirtualPrint;
    private javax.swing.JTextArea ta_VirtualPrint;
    private javax.swing.JTextField tf_Logfile;
    // End of variables declaration//GEN-END:variables
//</editor-fold>
}
