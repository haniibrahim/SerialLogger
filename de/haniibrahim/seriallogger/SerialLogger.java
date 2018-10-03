package de.haniibrahim.seriallogger;

//<editor-fold defaultstate="collapsed" desc="import statements">
import com.fazecast.jSerialComm.*;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.filechooser.FileNameExtensionFilter;
//</editor-fold>

/**
 *
 * "SerialLogger" simply logs the data stream received from a serial interface
 * to a GUI interface, the console and optionally to a file
 *
 * @author Hani Andreas Ibrahim
 * @version 1.1.0-alpha
 */
public class SerialLogger extends JFrame {

    static String version = "1.1.0-alpha"; // CHANGE VERSION NUMBER AS NECESSARY - Shown in info dialog

    private static SerialPort chosenPort;
    private static String stdLogfileName;
    private static String portName;
    private SerialReadTask serialReader;
    private Preferences prefs;

    private boolean appendFlag = false; // If append data to existing file was chosen
    private boolean printLogFlag;       // If data was saved or not

    // Set program icon
    private final Icon icon = new ImageIcon(Toolkit.getDefaultToolkit().getImage(getClass().getResource("serial_th.png")));

    /**
     * Creates new form SerialPrinter
     */
    public SerialLogger() {

        initComponents();

        // Checking before close frame
        this.addWindowListener(new WindowAdapter() {
            /**
             * Checks if buffer is empty and saved before closinf window If not
             * it asks whether it should proceed to quit
             *
             * @param e WindowEvent
             */
            @Override
            public void windowClosing(WindowEvent e) {
                // Check for unsaved buffer
                if (!ta_LogPanel.getText().isEmpty() && printLogFlag == false) {
                    Object[] options = {"Delete Buffer and Quit", "Cancel"};
                    int ans = JOptionPane.showOptionDialog(SerialLogger.getFrames()[0],
                            "Buffer is not empty and not saved\n",
                            "Buffer not empty",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.WARNING_MESSAGE,
                            null, options, options[1]);
                    if (ans == 0) {
                        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
                    } else {
                        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
                    }
                } else {
                    setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
                }
            }
        });

        // Set preferences to default or from storePrefs()
        setPrefs();

        // To-Do just before app shutdown
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                // Store preferences
                try {
                    storePrefs();
                } catch (BackingStoreException ex) {

                }
                // Close port at shutdown if button "Close Port" was not pressed before
                if (chosenPort != null && chosenPort.isOpen()) {
                    chosenPort.closePort();
                }
            }
        }));

        // Set OK-Button to default
        this.getRootPane().setDefaultButton(bt_OpenPort);

        // Add Contextmenu
        ta_LogPanel.addMouseListener(new ContextMenuMouseListener());
        tf_Logfile.addMouseListener(new ContextMenuMouseListener());

        // Open and close button disabled till updatePortList finished
        bt_OpenPort.setEnabled(false);
        bt_ClosePort.setEnabled(false);
        
        // Hide Info Button for macOS
        if (Helper.getOS().equals("mac")) {
            bt_Info.setVisible(false);
        }

        // Initial values for CommPorts in combobox
        updatePortList();
    }

    /**
     * Inner SwingWorker class: Reading serial data
     * <li>Read serial data
     * <li>Write it to the GUI
     * <li>Write it to the console
     * <li>Write it to file, if desired by user
     */
    private class SerialReadTask extends SwingWorker<Boolean, String> {

        BufferedReader serialBufferedReader;
        PrintWriter pw;

        @Override
        protected Boolean doInBackground() {
            // Get values from the GUI
            portName = cb_Commport.getSelectedItem().toString();
            int baud = Integer.parseInt(cb_Baud.getSelectedItem().toString());
            int databits = Integer.parseInt(cb_DataBits.getSelectedItem().toString());
            String stopbit_s = cb_StopBits.getSelectedItem().toString();
            String parity_s = cb_Parity.getSelectedItem().toString();
            String handshake_s = cb_Handshake.getSelectedItem().toString();

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

            // Handshake-Parser
            int handshake;
            if (handshake_s.equals("none")) {
                handshake = SerialPort.FLOW_CONTROL_DISABLED;
            } else if (handshake_s.equals("RTS/CTS")) {
                handshake = SerialPort.FLOW_CONTROL_RTS_ENABLED + SerialPort.FLOW_CONTROL_CTS_ENABLED;
            } else if (handshake_s.equals("XON/XOFF")) {
                handshake = SerialPort.FLOW_CONTROL_XONXOFF_IN_ENABLED;
            } else {
                System.err.println("ERROR: Handshake not specified, set to NONE");
                handshake = SerialPort.FLOW_CONTROL_DISABLED;
            }
            // Do not try to register an "empty" port 
            if (portName.equals("")) {
                System.err.println("ERROR: CommPort is empty!");
                JOptionPane.showMessageDialog(SerialLogger.getFrames()[0],
                        "CommPort is empty",
                        "Error", JOptionPane.ERROR_MESSAGE);
                toggleGuiElements(true);
                return false;
            }

            chosenPort = SerialPort.getCommPort(portName); // Register chosen port
            chosenPort.setComPortParameters(baud, databits, stopbits, parity); // Set serial parameters to the chosen port
            chosenPort.setFlowControl(handshake); // set flow control (RTS/CTS or XON/XOFF)
            chosenPort.setComPortTimeouts(SerialPort.TIMEOUT_SCANNER, 0, 0);
            chosenPort.openPort();
            if (chosenPort.isOpen()) {
                try { // Problems with log file - IOException
                    boolean logFlag;
                    if (ck_Logfile.isSelected()) {
                        if (appendFlag) {
                            pw = new PrintWriter(new FileWriter(tf_Logfile.getText(), true)); // append data to existing file
                        } else {
                            pw = new PrintWriter(new FileWriter(tf_Logfile.getText(), false)); // write data to a empty file
                        }
                        logFlag = true;
                        printLogFlag = true;
                    } else {
                        logFlag = false;
                        printLogFlag = false;
                    }
                    String line;
                    serialBufferedReader = new BufferedReader(new InputStreamReader(chosenPort.getInputStream()));
                    while (((line = serialBufferedReader.readLine()) != null) && !isCancelled()) {
                        publish(line);
                        if (logFlag) {
                            pw.println(getTimestamp() + line); // save to buffer (file)
                            pw.flush(); // flush buffer and tries to save every line to the file immediately
                        }
                    }
                    return false;
                } catch (IOException ex) {
                    System.err.println(ex.getMessage());
                    Helper.showLogIOException(ex.getMessage());
                    // Clean up
                    toggleGuiElements(true);
                    chosenPort.closePort();
                    return false;
                }
            } else {
                JOptionPane.showMessageDialog(SerialLogger.getFrames()[0],
                        "Could not open serial port\n" + portName + "\n",
                        "Info", JOptionPane.ERROR_MESSAGE);
                System.err.println("Could not open port " + portName);
                toggleGuiElements(true);
                return false;
            }
        }

        @Override
        protected void process(List<String> chunk) {
            String timestamp;
            for (String line : chunk) {
                ta_LogPanel.append(getTimestamp() + line + "\n");
                System.out.println(getTimestamp() + line);
            }
        }

        @Override
        protected void done() {
            try {
                if (get() == true) {
                    // Disable GUI elements
                    toggleGuiElements(false);
                }
            } catch (InterruptedException ex) {
                System.err.println(ex.getMessage());
            } catch (ExecutionException ex) {
                System.err.println(ex.getMessage());
            } catch (CancellationException ex) { // important
                System.err.println(ex.getMessage());
                if (chosenPort.isOpen()) {
                    // Close serial port
                    chosenPort.closePort();
                } else {
                    System.err.println("Final close port fails, port was not open");
                }
                if (ck_Logfile.isSelected()) {
                    if (pw != null) { // if PrintWriter bw exists
                        pw.close();
                    }
                }
            }
        }
    }

    /**
     * SwingWorker Thread: Read list of commports and show it in the combobox of
     * the GUI
     */
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
                    System.err.println(ex.getMessage());
                } catch (ExecutionException ex) {
                    System.err.println(ex.getMessage());
                }
            }
        };
        worker.execute();
    }
    
    /**
     * Get delimiter string from combobox which is used between timestamp and
     * data.
     * 
     * @return delimiter delimiter string
     */
    private String getDelimiter(){
        if (cb_Delimiter.getSelectedItem().toString().equals("blank")){
            return " ";
        } else if (cb_Delimiter.getSelectedItem().toString().equals("komma")){
            return ",";
        } else if (cb_Delimiter.getSelectedItem().toString().equals("semicolon")){
            return ";";
        } else {
            return "";
        }
    }
    
    /**
     * Get a timestamp string of the current date & time in different formats
     * <ul>
     * <li>ISO 8601
     * <li>Date, time, timezone
     * <li>Date, time
     * <li>Time
     * <li>Modified Julian Date
     * <li>Year, day of the year, time
     * </ul>
     * 
     * @return timestamp 
     */
    private String getTimestamp(){
        String delimiter = getDelimiter();
        if (cb_Timestamp.getSelectedItem().toString().equals("ISO 8601")){
            return Helper.getIsoTimestamp(delimiter);
        } else if (cb_Timestamp.getSelectedItem().toString().equals("Date|Time|Timezone")){
            return Helper.getDateTimeTz(delimiter);
        } else if (cb_Timestamp.getSelectedItem().toString().equals("Date|Time")){
            return Helper.getDateTimeTimestamp(delimiter);
        } else if (cb_Timestamp.getSelectedItem().toString().equals("Time")){
            return Helper.getTimeTimestamp(delimiter);
        } else if (cb_Timestamp.getSelectedItem().toString().equals("Mod. Julian Date")){
            return Helper.getMjd(delimiter);
        } else if (cb_Timestamp.getSelectedItem().toString().equals("Year|Day of year|Time")){
            return Helper.getDayOfYearTimestamp(delimiter);
         } else if (cb_Timestamp.getSelectedItem().toString().equals("yyyy MM dd HH mm ss Tz")){
            return Helper.getYMDhmsTz(delimiter);
        } else {
            return "";
        }
    }

    /**
     * Store settings in preferences - Window position and size - Serial
     * settings - Log file name and path
     *
     * @throws BackingStoreException
     */
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
        prefs.put("handshake", cb_Handshake.getSelectedItem().toString());
        prefs.put("timestamp", cb_Timestamp.getSelectedItem().toString());
        prefs.put("delimiter", cb_Delimiter.getSelectedItem().toString());

        // Save Logfile name
        if (tf_Logfile.getText().isEmpty()) {
            prefs.put("logfile", stdLogfileName);
        } else {
            prefs.put("logfile", tf_Logfile.getText());
        }
        prefs.putBoolean("logto", ck_Logfile.isSelected());

        prefs.flush();
    }

    /**
     * Set settings from stored preferences
     * <li>Window position and size
     * <li>Serial settings
     * <li>Log file name and path
     */
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

        // Initial logfilename => ~/serial.log
        stdLogfileName = System.getProperty("user.home")
                + System.getProperty("file.separator")
                + "serial.log";

        // Set serial parameters and logfile name
        String baud = prefs.get("baud", "9600");
        String databits = prefs.get("databits", "8");
        String stopbits = prefs.get("stopbits", "1");
        String parity = prefs.get("parity", "none");
        String handshake = prefs.get("handshake", "none");
        String timestamp = prefs.get("timestamp", "none");
        String delimiter = prefs.get("delimiter", "blank");
        String logfile = prefs.get("logfile", stdLogfileName);
        boolean logto = prefs.getBoolean("logto", false);

        // Put default/stored serialparameters/logfile in GUI
        cb_Baud.setSelectedItem(baud);
        cb_DataBits.setSelectedItem(databits);
        cb_StopBits.setSelectedItem(stopbits);
        cb_Parity.setSelectedItem(parity);
        cb_Handshake.setSelectedItem(handshake);
        cb_Timestamp.setSelectedItem(timestamp);
        cb_Delimiter.setSelectedItem(delimiter);
        tf_Logfile.setText(logfile);
        ck_Logfile.setSelected(logto);
        
        // No timestamp diabled delimiter combobox
        if (timestamp.equals("none")){
            cb_Delimiter.setEnabled(false);
        }
    }

    /**
     * Enables or disables GUI elements
     *
     * @param toggle Enables or disables GUI elements (T/F)
     */
    private void toggleGuiElements(boolean toggle) {
        // ClosePort button toggling
        if (toggle == true) {
            bt_ClosePort.setEnabled(false);
        } else if (toggle == false) {
            bt_ClosePort.setEnabled(true);
        }

        // other elements
        cb_Commport.setEnabled(toggle);
        bt_Update.setEnabled(toggle);
        cb_Baud.setEnabled(toggle);
        cb_DataBits.setEnabled(toggle);
        cb_StopBits.setEnabled(toggle);
        cb_Parity.setEnabled(toggle);
        cb_Handshake.setEnabled(toggle);
        cb_Timestamp.setEnabled(toggle);
        bt_OpenPort.setEnabled(toggle);
        ck_Logfile.setEnabled(toggle);
        tf_Logfile.setEnabled(toggle);
        bt_Fileselector.setEnabled(toggle);
        
        // Delimiter combobox toggling
        if (toggle && cb_Timestamp.getSelectedItem().equals("none")){
            cb_Delimiter.setEnabled(false);
        } else {
            cb_Delimiter.setEnabled(toggle);
        }
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
        ta_LogPanel = new javax.swing.JTextArea();
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
        cb_Timestamp = new javax.swing.JComboBox();
        cb_Delimiter = new javax.swing.JComboBox();
        lb_Timestamp = new javax.swing.JLabel();
        lb_Delimiter = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("SerialLogger");
        setIconImage(new ImageIcon(getClass().getResource("serial.png")).getImage());
        setLocationByPlatform(true);
        setMinimumSize(getPreferredSize());

        lb_Commport.setFont(lb_Commport.getFont());
        lb_Commport.setText("CommPort:");

        cb_Commport.setEditable(true);
        cb_Commport.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Update Ports ..." }));

        lb_Baud.setText("Baud:");

        cb_Baud.setEditable(true);
        cb_Baud.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "300", "600", "1200", "2400", "4800", "9600", "19200", "38400", "57600", "115200" }));

        lb_DataBits.setText("Data Bits:");

        cb_DataBits.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "5", "6", "7", "8" }));

        lb_StopBits.setText("Stop Bits:");

        cb_StopBits.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "1", "1.5", "2" }));

        lb_Parity.setText("Parity:");

        cb_Parity.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "none", "even", "odd", "mark", "space" }));

        ta_LogPanel.setEditable(false);
        ta_LogPanel.setColumns(20);
        ta_LogPanel.setRows(5);
        sp_VirtualPrint.setViewportView(ta_LogPanel);

        bt_Update.setText("Update");
        bt_Update.setToolTipText("Update CommPort list");
        bt_Update.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bt_UpdateActionPerformed(evt);
            }
        });

        bt_ClosePort.setText("Close Port");
        bt_ClosePort.setToolTipText("Close serial port");
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
        bt_OpenPort.setToolTipText("Open serial port");
        bt_OpenPort.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bt_OpenPortActionPerformed(evt);
            }
        });

        lb_Handshake.setText("Handshake:");

        cb_Handshake.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "none", "RTS/CTS", "XON/XOFF" }));
        cb_Handshake.setToolTipText("Flow Control (RTS/CTS=Hardware | XON/XOFF=Software)");

        ck_Logfile.setText("Log to:");
        ck_Logfile.setToolTipText("Enable/Disable logging");

        bt_Fileselector.setText("...");
        bt_Fileselector.setToolTipText("Choose log file");
        bt_Fileselector.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bt_FileselectorActionPerformed(evt);
            }
        });

        cb_Timestamp.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "none", "ISO 8601", "Date|Time|Timezone", "Date|Time", "Time", "Mod. Julian Date", "Year|Day of year|Time", "yyyy MM dd HH mm ss Tz" }));
        cb_Timestamp.setToolTipText("Timestamp (before each committed line)");
        cb_Timestamp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cb_TimestampActionPerformed(evt);
            }
        });

        cb_Delimiter.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "blank", "komma", "semicolon", "none" }));
        cb_Delimiter.setToolTipText("Delimiter between timestamp and data");

        lb_Timestamp.setText("Timestamp:");

        lb_Delimiter.setText("Delimiter:");
        lb_Delimiter.setToolTipText("Delimiter between timestamp and data");

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
                        .addGap(0, 131, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(ck_Logfile)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(tf_Logfile)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(bt_Fileselector))
                            .addComponent(sp_VirtualPrint))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(bt_Info, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(bt_OpenPort, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(bt_ClosePort, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(lb_Baud, javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addComponent(lb_DataBits))
                                    .addComponent(lb_StopBits))
                                .addComponent(lb_Handshake)
                                .addComponent(lb_Parity, javax.swing.GroupLayout.Alignment.TRAILING))
                            .addComponent(lb_Timestamp, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lb_Delimiter))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(cb_Delimiter, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(cb_Timestamp, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
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
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(cb_Timestamp, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lb_Timestamp))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(cb_Delimiter, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lb_Delimiter))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 118, Short.MAX_VALUE)
                        .addComponent(bt_OpenPort))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(sp_VirtualPrint)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(bt_ClosePort)
                    .addComponent(tf_Logfile, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(ck_Logfile)
                    .addComponent(bt_Fileselector, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
    /**
     * Update Button event
     *
     * @param evt
     */
    private void bt_UpdateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bt_UpdateActionPerformed
        updatePortList();
    }//GEN-LAST:event_bt_UpdateActionPerformed

    /**
     * Close port button event
     *
     * @param evt
     */
    private void bt_ClosePortActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bt_ClosePortActionPerformed
        // Enable GUI elements
        toggleGuiElements(true);

        // flag main swingworker class as cancel
        serialReader.cancel(true);
    }//GEN-LAST:event_bt_ClosePortActionPerformed

    /**
     * Info button event
     *
     * @param evt
     */
    private void bt_InfoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bt_InfoActionPerformed
        JOptionPane.showMessageDialog(this,
                "<html><span style=\"font-size:large;\"><b>SerialLogger</b></span></html>\n"
                + "Logs data received from a serial interface\n"
                + "to GUI, console or file.\n\n"
                + "Version: " + version + "\n\n"
                + "(c) 2013-" + Helper.getCurrentYear() + " Hani Ibrahim\n"
                + "<html><a href=\"mailto:hani.ibrahim@gmx.de>\">hani.ibrahim@gmx.de</a>\n\n"
                + "GNU Public License 3.0\n\n",
                "Info", JOptionPane.INFORMATION_MESSAGE, icon);
//        InfoDialog infoDialog = new InfoDialog(this, true);
//        infoDialog.setLocationRelativeTo(this);
//        infoDialog.setVisible(true);
    }//GEN-LAST:event_bt_InfoActionPerformed

    /**
     * Open port button event
     *
     * @param evt
     */
    private void bt_OpenPortActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bt_OpenPortActionPerformed
        // Clear textarea if desired
        if (!ta_LogPanel.getText().isEmpty() && printLogFlag == false) { // Buffer not empty
            Object[] options = {"Delete Buffer", "Cancel"};
            int ans = JOptionPane.showOptionDialog(this,
                    "Buffer is not empty and not saved",
                    "Buffer not empty",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE,
                    null, options, options[1]);
            if (ans == 0) {
                ta_LogPanel.setText("");
            } else {
                return;
            }
        }
        ta_LogPanel.setText("");

        // Check for consistent logging settings
        if ((ck_Logfile.isSelected() && tf_Logfile.getText().equals(""))) {
            JOptionPane.showMessageDialog(this,
                    "Logfile name is empty",
                    "Error", JOptionPane.ERROR_MESSAGE);
            System.err.println("Logfile name is empty");
        } else if (ck_Logfile.isSelected() && (new File(tf_Logfile.getText())).exists()) {
            // Log file exists, append or cancel?
            Object[] options = {
                "Append",
                "Cancel"
            };
            int ans = JOptionPane.showOptionDialog(this,
                    "Logfile already exists\n"
                    + "Do you want to append data to this file\n",
                    "Append or Cancel?",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null, options, options[1]);
            if (ans == 0) { // append data to file
                appendFlag = true;
                toggleGuiElements(false);
                serialReader = new SerialReadTask();
                serialReader.execute();
            } else { // cancel
                appendFlag = false;
            }
        } else { // no problems
            // Disable GUI elements
            toggleGuiElements(false);
            // Read from the serial interface
            serialReader = new SerialReadTask();
            serialReader.execute();
        }
    }//GEN-LAST:event_bt_OpenPortActionPerformed

    /**
     * Logfile's fileselector button event
     *
     * @param evt
     */
    private void bt_FileselectorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bt_FileselectorActionPerformed

        String dialogTitle = "Specify log file ..";
        String newLogfilePath;

        // Logfilename: Extract pathname (w/o filename) from tf_Logfile
        String oldLogfilePath = tf_Logfile.getText();
        if (oldLogfilePath.isEmpty()) { // if tf_Logfile is empty set standard logfile path
            oldLogfilePath = System.getProperty("user.home")
                    + System.getProperty("file.separator")
                    + "serial.log";
        }
        String pathName = oldLogfilePath.substring(0, // Extract path w/o filename
                oldLogfilePath.lastIndexOf(System.getProperty("file.separator")) + 1);

        // If Windows or Linux AND JRE 6 than use JFilechooser() otherwise the native FileDialog()
        // the native filechooser is ugly on Linux with Jave 6
        if ((Helper.getOS().equals("linux")
                && System.getProperty("java.version").startsWith("1.6"))
                || (Helper.getOS().equals("win"))) {
            JFileChooser fd = new JFileChooser(pathName);
            fd.setDialogTitle(dialogTitle);
            fd.setDialogType(JFileChooser.SAVE_DIALOG);
            FileNameExtensionFilter logFilter = new FileNameExtensionFilter(
                    "Logfile (*.log, *.txt)", "log", "txt");
            fd.setFileFilter(logFilter);
            fd.setVisible(true);
            int retVal = fd.showSaveDialog(this);
            if (retVal == JFileChooser.APPROVE_OPTION) {
                newLogfilePath = fd.getSelectedFile().toString();
            } else {
                newLogfilePath = oldLogfilePath;
            }
        } else {
            FileDialog fd = new FileDialog(this, dialogTitle, FileDialog.SAVE);
            FilenameFilter filter = new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return (name.endsWith(".txt") || name.endsWith(".log"));
                }
            };
            fd.setFilenameFilter(filter);

            fd.setDirectory(pathName);  // Set default path to last selected path
            fd.setVisible(true);
            if (fd.getFile() != null) {
                newLogfilePath = fd.getDirectory() + fd.getFile();
            } else {
                newLogfilePath = oldLogfilePath;
            }
        }
        tf_Logfile.setText(newLogfilePath);
    }//GEN-LAST:event_bt_FileselectorActionPerformed

    private void cb_TimestampActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cb_TimestampActionPerformed
        if (!cb_Timestamp.getSelectedItem().toString().equals("none")){
            cb_Delimiter.setEnabled(true);
        } else {
            cb_Delimiter.setEnabled(false);
        }
    }//GEN-LAST:event_cb_TimestampActionPerformed
    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {

        //<editor-fold defaultstate="collapsed" desc="Look and Feel">
        // Try GTK-LaF on GNU/Linux first, then System-LaF. System-LaF on all other platforms
        if (System.getProperty("os.name").toLowerCase().contains("linux")) {
            try {
                UIManager.setLookAndFeel("com.sun.java.swing.plaf.gtk.GTKLookAndFeel");
            } catch (Exception e1) {
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (Exception e2) {
                    System.err.println("Look & Feel Error\n" + e2.getMessage());
                }
            }
        } else {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e2) {
                System.err.println("Look & Feel Error\n" + e2.getMessage());
            }
        }
        //</editor-fold>

        /* Create and display the form */
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new SerialLogger().setVisible(true);
                if (Helper.getOS().equals("mac")) {
                    MacImpl macImpl = new MacImpl();
                }
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
    private javax.swing.JComboBox cb_Delimiter;
    private javax.swing.JComboBox cb_Handshake;
    private javax.swing.JComboBox cb_Parity;
    private javax.swing.JComboBox cb_StopBits;
    private javax.swing.JComboBox cb_Timestamp;
    private javax.swing.JCheckBox ck_Logfile;
    private javax.swing.JLabel lb_Baud;
    private javax.swing.JLabel lb_Commport;
    private javax.swing.JLabel lb_DataBits;
    private javax.swing.JLabel lb_Delimiter;
    private javax.swing.JLabel lb_Handshake;
    private javax.swing.JLabel lb_Parity;
    private javax.swing.JLabel lb_StopBits;
    private javax.swing.JLabel lb_Timestamp;
    private javax.swing.JLabel lb_VirtalPrint;
    private javax.swing.JScrollPane sp_VirtualPrint;
    private javax.swing.JTextArea ta_LogPanel;
    private javax.swing.JTextField tf_Logfile;
    // End of variables declaration//GEN-END:variables
//</editor-fold>
}
