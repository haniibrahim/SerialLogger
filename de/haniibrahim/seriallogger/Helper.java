package de.haniibrahim.seriallogger;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import javax.swing.JOptionPane;

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
     * Return a timestamp in ISO 8601, format: yyyy-MM-dd'T'HH:mm:ssZ, separator
     *
     * Example: 2018-09-06T13:45:42+0200 1:45:42pm at September 6th, 2018 CEST
     * (Central European Summer Time)
     *
     * @return ISO 8601 timestamp
     */
    static String getIsoTimestamp(String separator) {
        TimeZone tz = TimeZone.getDefault();
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        df.setTimeZone(tz);
        return df.format(new Date()) + separator; // ISO 8601 Timestamp format
    }

    /**
     * Return a timestamp formated as date, sepearator, time, separator
     * timezone, separator
     *
     * This format is excellent to process data in spreadsheets apps
     *
     * Example: 06.09.2016,13:45:42,+0200 1:45:42pm at September 6th, 2018 CEST,
     * comma is the separator
     *
     * @param separator Separator sign (e.g. [blank],[,],[;])
     * @return date/separator/time timestamp
     */
    static String getDateTimeTz(String separator) {
        TimeZone tz = TimeZone.getDefault();
        DateFormat tzf = new SimpleDateFormat("Z");
        tzf.setTimeZone(tz);

        DateFormat df = new SimpleDateFormat("dd-MM-YYYY"); // Date
        DateFormat tf = new SimpleDateFormat("HH:mm:ss"); // Time
        return df.format(new Date()) + separator + tf.format(new Date())
                + separator + tzf.format(new Date()) + separator;
    }

    /**
     * Return a timestamp formated as date, sepearator, time, separator
     *
     * This format is excellent to process data in spreadsheets apps
     *
     * Example: 06.09.2016,13:45:42 1:45:42pm at September 6th, 2018, comma is
     * the separator
     *
     * @param separator Separator sign (e.g. [blank],[,],[;])
     * @return date/separator/time timestamp
     */
    static String getDateTimeTimestamp(String separator) {
        DateFormat df = new SimpleDateFormat("dd-MM-YYYY"); // Date
        DateFormat tf = new SimpleDateFormat("HH:mm:ss"); // Time
        return df.format(new Date()) + separator + tf.format(new Date()) + separator;
    }

    /**
     * Return a timestamp formated as time
     *
     * This format is excellent to process data in spreadsheets apps
     *
     * Example: 13:45:42 
     * 1:45:42pm at September 6th, 2018
     *
     * @param separator Separator sign (e.g. [blank],[,],[;])
     * @return date/separator/time timestamp
     */
    static String getTimeTimestamp(String separator) {
        DateFormat tf = new SimpleDateFormat("HH:mm:ss"); // Time
        return tf.format(new Date()) + separator;
    }
    
    /**
     * Returns timestamp with day of the year
     * 
     * Example 2018,249,13:45:42
     * 1:45:42pm at September 6th, 2018 (day 249 of 2018)
     * 
     * @param separator Separator sign (e.g. [blank],[,],[;])
     * @return 
     */
    static String getDayOfYearTimstamp(String separator){
       Calendar cal = Calendar.getInstance(); // get current date and time
        return Integer.toString(cal.get(Calendar.YEAR)) + separator 
                + Integer.toString(cal.get(Calendar.DAY_OF_YEAR)) + separator
                + Integer.toString(cal.get(Calendar.HOUR_OF_DAY)) + ":"
                + Integer.toString(cal.get(Calendar.MINUTE)) + ":"
                + Integer.toString(cal.get(Calendar.SECOND));
    }

    /**
     * Returns timestamp as modified Julian Date including time as floating point
     * 
     * @param separator Separator sign (e.g. [blank],[,],[;])
     * @return 
     */
    static String getMjd(String separator) {
        Calendar cal = Calendar.getInstance(); // get current date and time
        double sec = cal.get(Calendar.SECOND) + cal.get(Calendar.MILLISECOND) / 1000.0;
        return Double.toString(getMjd(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DATE), cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), sec))
                + separator;
    }

    /**
     * Calculate Modified Julian Date from calendar date and time elements
     *
     * @param year calendar year
     * @param month calendar month
     * @param day calendar day
     * @param hour calendar hour (0-24)
     * @param min calendar min
     * @param sec calendar sec
     * @return Modified Julian Date (UT)
     */
    private static double getMjd(int year, int month, int day, int hour, int min, double sec) {
        // Variables 
        long MjdMidnight;
        double FracOfDay;
        int b;

        if (month <= 2) {
            month += 12;
            --year;
        }

        if ((10000L * year + 100L * month + day) <= 15821004L) {
            b = -2 + ((year + 4716) / 4) - 1179;     // Julian calendar 
        } else {
            b = (year / 400) - (year / 100) + (year / 4);  // Gregorian calendar 
        }

        MjdMidnight = 365L * year - 679004L + b + (int) (30.6001 * (month + 1)) + day;
        FracOfDay = (hour + min / 60.0 + sec / 3600.0) / 24.0;

        return MjdMidnight + FracOfDay;
    } //calcMjd 

    /**
     * Show alertbox when writing in log file fails
     */
    static void showLogIOException(String ex) {
        JOptionPane.showMessageDialog(SerialLogger.getFrames()[0],
                "ERROR:\n" + ex,
                "Log file error", JOptionPane.ERROR_MESSAGE);
    }

}
