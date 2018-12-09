/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.haniibrahim.seriallogger;

import java.awt.FileDialog;
import java.awt.Frame;
import java.io.File;
import java.io.FilenameFilter;
import javax.swing.JFileChooser;
import javax.swing.UIManager;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * Provides fileselector methods for getting filenames for loading and saving
 * <p>
 * It tries to use the best Java fileselector method for the curent platform
 * (AWT or Swing)
 * </p>
 * <ul>
 * <li>Windows (any Java version), GNU/Linux (Java 1.6 or lower): Swing </li>
 * <li>macOS, GNU/Linux (Java 1.7 or higher): AWT</li>
 * </ul>
 * 
 * @author hi
 */
final class FileSelector {
    
    /**
     * Get filename incl path for saving a file using a Fileselector
     * <p>
     * It uses the native filechooser on macOS with any Java version 
     * and Linux with Java 7 or higher. 
     * </p><p>
     * On Windows and Linux with Java lower than version 7 the Swing 
     * filechooser JFileChooser() is used. Because the native filechooser Java with 
     * version 6 or lower provides on Linux is ugly (Motif-style).
     * </p><p>
     * Because the native filechooser is ugly on Linux with Jave 6 or lower.
     * </p>
     * 
     * @param frame        Name of the frame (this or null)
     * @param defFilename  Default filename
     * @param defFilepath  Default filepath incl. filename
     * @param dialogTitle  Dialog title displayed in the fileselector window title
     * @return             Filename incl. path       
     */
    static String getSaveFilename(Frame frame, String defFilename, String defFilepath, String dialogTitle ){
        String newfilePath;

        // Logfilename: Extract pathname (w/o defFilename) from tf_Logfile
        String oldFilePath = defFilepath;
        if (oldFilePath.isEmpty()) { // set standard logfile path
            oldFilePath = System.getProperty("user.home")
                    + System.getProperty("file.separator")
                    + defFilename;
        }
        String pathName = oldFilePath.substring(0, // Extract path w/o defFilename
                oldFilePath.lastIndexOf(System.getProperty("file.separator")) + 1);

        // If Linux AND JRE 6 than use JFilechooser() otherwise the native FileDialog()
        // the native filechooser is ugly on Linux with Jave 6
        if ((getOS().equals("lin")
                && 
                  (System.getProperty("java.version").startsWith("1.6")
                || System.getProperty("java.version").startsWith("1.5")
                || System.getProperty("java.version").startsWith("1.4")
                || System.getProperty("java.version").startsWith("1.3")
                || System.getProperty("java.version").startsWith("1.2")
                || System.getProperty("java.version").startsWith("1.1")
                || System.getProperty("java.version").startsWith("1.0")
                ))
                || (getOS().equals("win"))
                || (!isNativeLookAndFeel())) {
            JFileChooser fd = new JFileChooser(pathName);
            fd.setDialogTitle(dialogTitle);
            fd.setDialogType(JFileChooser.SAVE_DIALOG);
            FileNameExtensionFilter logFilter = new FileNameExtensionFilter(
                    "Logfile (*.log, *.txt)", "log", "txt");
            fd.setFileFilter(logFilter);
            fd.setVisible(true);
            int retVal = fd.showSaveDialog(frame);
            if (retVal == JFileChooser.APPROVE_OPTION) {
                newfilePath = fd.getSelectedFile().toString();
            } else {
//                newfilePath = oldFilePath;
                newfilePath = "";
            }
        } else {
            FileDialog fd = new FileDialog(frame, dialogTitle, FileDialog.SAVE);
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
                newfilePath = fd.getDirectory() + fd.getFile();
            } else {
//                newfilePath = oldFilePath;
                newfilePath = "";
            }
        }
        return newfilePath;
    }
    
    /**
     * Detect platform (Windows, macOS, ...)
     *
     * @return platform name as short string: win, mac, lin, noarch. If
     * platform cannot detected "noarch" is returned
     */
    private static String getOS() {
        String osname = System.getProperty("os.name");
        if (osname != null && osname.toLowerCase().contains("mac")) {
            return "mac";
        }
        if (osname != null && osname.toLowerCase().contains("windows")) {
            return "win";
        }
        if (osname != null && osname.toLowerCase().contains("linux")) {
            return "lin";
        }
        return "noarch";
    }

    /**
     * If the underlying platform has a "native" look and feel, and this is an 
     * implementation of it, return true.
     * 
     * @return true if this look and feel represents the underlying platform look and feel
     */
    private static boolean isNativeLookAndFeel() {
        // Get current LaF
        String lafName = UIManager.getLookAndFeel().getName();        
        // Workaround for Java and GTK look and feel:
        // - UIManager.getLookAndFeel().getName() = "GTK look and feel"
        // - UIManager.LookAndFeelInfo.getName() = "GTK+"
        if (lafName.equalsIgnoreCase("GTK look and feel")) {
            lafName = "GTK+";
        }
        if (getOS().equals("win") && lafName.contains("windows")){
            return true;
        } else if (getOS().equals("mac") && lafName.contains("Mac")){
            return true;
        } else if (getOS().equals("lin") && lafName.contains("GTK")){
            return true;
        } else {
            return false;
        }
//        return !(lafName.equals("Metal") || lafName.equals("Nimbus") || lafName.equals("CDE/Motif"));
    }
}
