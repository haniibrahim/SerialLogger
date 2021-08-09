package de.haniibrahim.seriallogger;

import java.util.Arrays;
import java.util.List;
import javax.swing.UIManager;

/**
 *  Helper class for Look and Feel Handling
 * <p>
 * Get the current Laf as Name (String), as class name (String) and as class 
 * (UIManager.LookAndFeelInfo) and all availabe Look and Feels in the same types.
 * </p>
 * 
 * @author HI
 */
public final class LafHelper {
    
    /**
     * The numbers of instelled Look and Feels
     */
    public final static int LAF_LENGTH = UIManager.getInstalledLookAndFeels().length; // Amount of available LaFs
    
    private static final UIManager.LookAndFeelInfo[] lafs = new UIManager.LookAndFeelInfo[LAF_LENGTH]; // LaF classes
    private static final String[] lafNames = new String[LAF_LENGTH]; // LaF names as Strings
    private static final String[] lafClassNames = new String[LAF_LENGTH]; // LaF names as Strings
    
    // ------------------------------------------------------------------------
    // Class methods
    // ------------------------------------------------------------------------
    
    /**
     * Returns the available Look and Feels as UIManager.LookAndFeelInfo[] array
     * 
     * @return array of strings
     */
    public static UIManager.LookAndFeelInfo[] getLafs(){
        int i = 0;
        for (UIManager.LookAndFeelInfo laf : UIManager.getInstalledLookAndFeels()) {
            lafs[i] = laf;
            i++;
        }
        return lafs;
    }
    
    /**
     * Returns the available Look and Feels names as String array
     * 
     * @return array of strings
     */
    public static String[] getLafNames(){
        int i = 0;
        for (UIManager.LookAndFeelInfo laf : UIManager.getInstalledLookAndFeels()) {
            lafNames[i] = laf.getName();
            i++;
        }
        return lafNames;
    }
    
    /**
     * Returns the available Look and Feels class names as String array
     * 
     * @return an array of LookAndFeelInfo objects
     */
    public static String[] getLafClassNames(){
        int i = 0;
        for (UIManager.LookAndFeelInfo laf : UIManager.getInstalledLookAndFeels()) {
            lafClassNames[i] = laf.getClassName();
            i++;
        }
        return lafClassNames;
    }
    
    
    /**
     * Get current Look and Feel name or null
     *
     * @return Current Look and Feel name
     */
    public static String getCurrentLafName() {
        String currentLafName = UIManager.getLookAndFeel().getName();    
        // Workaround for Java and GTK look and feel:
        // - UIManager.getLookAndFeel().getName() = "GTK look and feel"
        // - UIManager.LookAndFeelInfo.getName() = "GTK+"
        if (currentLafName.equalsIgnoreCase("GTK look and feel")) {
            currentLafName = "GTK+";
        }
        return currentLafName;
    }

    /**
     * Get current Lok and Feel classname
     *
     * @return Current Look and Feel classname
     */
    public static String getCurrentLafClassName(){
//        setAvailableLafs();
        List<String> gls = Arrays.asList(getLafNames());
        String gcln = getCurrentLafName();
        int idx = Arrays.asList(getLafNames()).indexOf(getCurrentLafName());
        return getLafs()[idx].getClassName();
    }
    
    /**
     * If the underlying platform has a "native" look and feel, and this is an 
     * implementation of it, return true.
     * 
     * @return true if this look and feel represents the underlying platform look and feel
     */
    public static boolean isNativeLookAndFeel() {
        boolean nativeLaf = UIManager.getLookAndFeel().isNativeLookAndFeel();        
        return nativeLaf;
    }
}
