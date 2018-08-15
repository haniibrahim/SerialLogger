package de.haniibrahim.seriallogger;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Helper Class
 *
 * @author hi
 */
final class Helper {

    /**
     * Return current year as YYYY
     *
     * @return year
     */
    static int getCurrentYear() {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        int year = cal.get(Calendar.YEAR);
        return year;
    }

    /**
     * Detect platform (Windows, macOS, ...)
     *
     * @return platform name as short string: win, mac, linux, noarch. If
     * platform cannot detected "noarch" is returned
     */
    static String getOS() {
        String osname = System.getProperty("os.name");
        if (osname != null && osname.toLowerCase().contains("mac")) {
            return "mac";
        }
        if (osname != null && osname.toLowerCase().contains("windows")) {
            return "win";
        }
        if (osname != null && osname.toLowerCase().contains("linux")) {
            return "linux";
        }
        return "noarch";
    }
    
    /**
     * Return a timestamp in ISO 8601, format: yyyy-MM-dd'T'HH:mm:ssZ
     * 
     * @return ISO 8601 timestamp
     */
    static String getIsoTimestamp() {
        TimeZone tz = TimeZone.getDefault();
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        df.setTimeZone(tz);
        return df.format(new Date()); // ISO 8601 Timestamp format
    }

}
