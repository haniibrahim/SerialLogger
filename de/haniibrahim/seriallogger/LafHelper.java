package de.haniibrahim.seriallogger;

import java.util.Arrays;
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
    private static final String[] lafStrs = new String[LAF_LENGTH]; // LaF names as Strings
    
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
    public static String[] getLafStrs(){
        int i = 0;
        for (UIManager.LookAndFeelInfo laf : UIManager.getInstalledLookAndFeels()) {
            lafStrs[i] = laf.getName();
            i++;
        }
        return lafStrs;
    }
    
    /**
     * Returns the available Look and Feels class names as String array
     * 
     * @return an array of LookAndFeelInfo objects
     */
    public static String[] getLafClassNames(){
        int i = 0;
        for (UIManager.LookAndFeelInfo laf : UIManager.getInstalledLookAndFeels()) {
            lafStrs[i] = laf.getClassName();
            i++;
        }
        return lafStrs;
    }
    
    
    /**
     * Get current Look and Feel name or null
     *
     * @return Current Look and Feel name
     */
    public static String getCurrentLafName() {
        String lafName = UIManager.getLookAndFeel().getName();
        
        // Workaround for Java and GTK look and feel:
        // - UIManager.getLookAndFeel().getName() = "GTK look and feel"
        // - UIManager.LookAndFeelInfo.getName() = "GTK+"
        if (lafName.equalsIgnoreCase("GTK look and feel")) {
            lafName = "GTK+";
        }
        return lafName;
    }

    /**
     * Get current Lok and Feel classname
     *
     * @return Current Look and Feel classname
     */
    public static String getCurrentLafClassName(){
//        setAvailableLafs();
        int idx = Arrays.asList(getLafStrs()).indexOf(getCurrentLafName());
        return getLafs()[idx].getClassName();
    }
    
    /**
     * If the underlying platform has a "native" look and feel, and this is an 
     * implementation of it, return true.
     * 
     * @return true if this look and feel represents the underlying platform look and feel
     */
    public static boolean isNativeLookAndFeel() {
        // Get current LaF
        String lafName = UIManager.getLookAndFeel().getName();        
        // Workaround for Java and GTK look and feel:
        // - UIManager.getLookAndFeel().getName() = "GTK look and feel"
        // - UIManager.LookAndFeelInfo.getName() = "GTK+"
        if (lafName.equalsIgnoreCase("GTK look and feel")) {
            lafName = "GTK+";
        }
        return !(lafName.equals("Metal") || lafName.equals("Nimbus") || lafName.equals("CDE/Motif"));
    }
}
