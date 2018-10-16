/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.haniibrahim.seriallogger;

import com.apple.eawt.AboutHandler;
import com.apple.eawt.AppEvent.AboutEvent;
import com.apple.eawt.AppEvent.PreferencesEvent;
import com.apple.eawt.AppEvent.QuitEvent;
import com.apple.eawt.Application;
import com.apple.eawt.PreferencesHandler;
import com.apple.eawt.QuitHandler;
import com.apple.eawt.QuitResponse;
import java.awt.Toolkit;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

/**
 *
 * @author Hani Ibrahim
 */
class MacImpl implements ClassSelector, AboutHandler, PreferencesHandler, QuitHandler {

    Application application;

    public MacImpl() {
        handleOS();
    }

    @Override
    public void handleOS() {

        try {
            application = Application.getApplication();
            application.setAboutHandler(this);
            // PreferenceHandler not used yet
            // application.setPreferencesHandler(this); 
        } catch (Throwable e) {
            System.err.println("setupMacOSXApplicationListener failed: "
                    + e.getMessage());
        }

        // Set dock icon
        application.setDockIconImage(new ImageIcon(getClass().getResource("serial.png")).getImage());
    }

    // Info Dialog
    @Override
    public void handleAbout(AboutEvent arg0) {
        Icon icon = new ImageIcon(Toolkit.getDefaultToolkit().getImage(getClass().getResource("serial_th.png")));
        JOptionPane.showMessageDialog(null,
                "<html><span style=\"font-size:large;\"><b>SerialLogger</b></span></html>\n"
                + "Logs data received from a serial interface\n"
                + "to GUI, console or file.\n\n"
                + "Version: " + SerialLogger.version + "\n\n"
                + "(c) 2013-" + Helper.getCurrentYear() + " Hani Ibrahim\n"
                + "<html><a href=\"mailto:hani.ibrahim@gmx.de>\">hani.ibrahim@gmx.de</a>\n\n"
                + "GNU Public License 3.0\n\n",
                "Info", JOptionPane.INFORMATION_MESSAGE, icon);
//        InfoDialog infoDialog = new InfoDialog(SerialLogger.getFrames()[0], true);
//        infoDialog.setLocationRelativeTo(null);
//        infoDialog.setVisible(true);
        // System.out.println("handleAbout()"); 
    }

    // Options dialog
    @Override
    public void handlePreferences(PreferencesEvent arg0) {
        Options optionsDialog = new Options(SerialLogger.getFrames()[0], true);
        optionsDialog.setLocationRelativeTo(null);
        optionsDialog.setVisible(true);
        //System.out.println("handlePreferences()"); 
    }

    @Override
    public void handleQuitRequestWith(QuitEvent arg0, QuitResponse arg1) {
        System.exit(0);
    }
}

interface ClassSelector {

    public void handleOS();
}
