package de.haniibrahim.seriallogger;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
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
<<<<<<< HEAD
     * Returns the current and local timezone string in ISO 8601 format 
     * (e.g. +02:00)
=======
     * Return a timestamp in ISO 8601, format: yyyy-MM-dd'T'HH:mm:ssZ, separator
     * 
     * Example: 2018-09-06T13:45:42+0200
     * 1:45:42pm at September 6th, 2018 CEST (Central European Summer Time) 
>>>>>>> refs/stash
     * 
     * @return ISO 8601 timezone string (e.g. +02:00)
     */
    static String getIsoTzString(){
        double offsetInHrDbl; // Timezone offset in hours (double)
        int dstOffset;
        String prefix; // Prefix of the numer (+/-)

        Calendar cal = Calendar.getInstance(); // Current Date/Time
        boolean dst = cal.getTimeZone().inDaylightTime(cal.getTime()); // summertime=yes, wintertime=false
        int rawOffset = cal.getTimeZone().getRawOffset(); // Time offset of the local timezone (w/o DST) in ms
        
        // Set DST offset if summertime
        if (dst){ // summertime
            dstOffset = cal.getTimeZone().getDSTSavings(); // General local DST in ms
        } else { // wintertime
            dstOffset = 0;
        }
        
        if (rawOffset < 0) { // Negative raw offset (w/o DST)
            offsetInHrDbl = Math.abs((rawOffset - dstOffset) / 3.6e6); // Offset in decimal hr w/o prefix
            prefix = "-";
        } else { // Positive raw offset or 0 (w/o DST)
            offsetInHrDbl = (rawOffset + dstOffset) / 3.6e6; // Offset in decimal hr
            prefix = "+";
        }
        int offsetHrInt = (int) offsetInHrDbl; // Offset hr only
        int offsetMnInt = (int) ((offsetInHrDbl - (double) offsetHrInt) * 60); // Offset min only
        
        return prefix + String.format("%02d", offsetHrInt) + ":"
                + String.format("%02d", offsetMnInt);
    }

    /**
     * Return a timestamp in ISO 8601, format: yyyy-MM-dd'T'HH:mm:ssZ, delimiter 
     * 
     * Example: 2018-09-06T13:45:42+02:00 1:45:42pm at September 6th, 2018 CEST
     * (Central European Summer Time)
     * 
     * @param delimiter Separator sign (e.g. [blank],[,],[;]) 
     * @return ISO 8601 timestamp
     */
<<<<<<< HEAD
    static String getIsoTimestamp(String delimiter) {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        return df.format(new Date()) + getIsoTzString() + delimiter; // ISO 8601 Timestamp format
    }

    /**
     * Return a timestamp formated as date, sepearator, time, delimiter 
     * timezone, delimiter
     * 
     * This format is excellent to process data in spreadsheets apps
     * 
     * Example: 06.09.2016,13:45:42,+02:00 
     * 1:45:42pm at September 6th, 2018 CEST, comma is the delimiter
     *
     * @param delimiter Separator sign (e.g. [blank],[,],[;])
     * @return date/delimiter/time timestamp
     */
    static String getDateTimeTz(String delimiter) {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd"); // Date
        DateFormat tf = new SimpleDateFormat("HH:mm:ss"); // Time
        return df.format(new Date()) + delimiter + tf.format(new Date())
                + delimiter + getIsoTzString() + delimiter;
    }

    /**
     * Return a timestamp formated as date, sepearator, time, delimiter
     * 
     * This format is excellent to process data in spreadsheets apps
     * 
     * Example: 06.09.2016,13:45:42 
     * 1:45:42pm at September 6th, 2018, comma is the delimiter
     *
     * @param delimiter Separator sign (e.g. [blank],[,],[;])
     * @return date/delimiter/time timestamp
     */
    static String getDateTimeTimestamp(String delimiter) {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd"); // Date
        DateFormat tf = new SimpleDateFormat("HH:mm:ss"); // Time
        return df.format(new Date()) + delimiter + tf.format(new Date()) + delimiter;
    }

    /**
     * Return a timestamp formated as time
     *
     * This format is excellent to process data in spreadsheets apps
     *
     * Example: 13:45:42 
     * 1:45:42pm
     *
     * @param delimiter Separator sign (e.g. [blank],[,],[;])
     * @return date/delimiter/time timestamp
     */
    static String getTimeTimestamp(String delimiter) {
        DateFormat tf = new SimpleDateFormat("HH:mm:ss"); // Time
        return tf.format(new Date()) + delimiter;
    }
    
    /**
     * Returns timestamp with day of the year
     * 
     * Example 2018,249,13:45:42
     * 1:45:42pm at September 6th, 2018 (day 249 of 2018)
     * 
     * @param delimiter Separator sign (e.g. [blank],[,],[;])
     * @return day of the year timestamp
     */
    static String getDayOfYearTimestamp(String delimiter){
       Calendar cal = Calendar.getInstance(); // get current date and time
        return Integer.toString(cal.get(Calendar.YEAR)) + delimiter 
                + Integer.toString(cal.get(Calendar.DAY_OF_YEAR)) + delimiter
                + Integer.toString(cal.get(Calendar.HOUR_OF_DAY)) + ":"
                + Integer.toString(cal.get(Calendar.MINUTE)) + ":"
                + Integer.toString(cal.get(Calendar.SECOND))
                + delimiter;
    }
    /**
     * Return timestamp in seperate date and time components incl. timezone
     * 
     * Example: 2018 10 2 10 21 48 +0200
     * 10:21:48am at October 2nd, 2018 CEST, [blank] is the delimiter
     * 
     * @param delimiter Separator sign (e.g. [blank],[,],[;])
     * @return Year|month|day|hours|minutes|seconds|timezone timestamp
     */
    static String getYMDhms(String delimiter){
        Calendar cal = Calendar.getInstance(); // get current date and time
        return Integer.toString(cal.get(Calendar.YEAR)) + delimiter 
                + Integer.toString(cal.get(Calendar.MONTH)+1) + delimiter // +1 because January=0
                + Integer.toString(cal.get(Calendar.DAY_OF_MONTH)) + delimiter
                + Integer.toString(cal.get(Calendar.HOUR_OF_DAY)) + delimiter
                + Integer.toString(cal.get(Calendar.MINUTE)) + delimiter
                + Integer.toString(cal.get(Calendar.SECOND)) + delimiter;
    }

    /**
     * Returns timestamp as modified Julian Date including time as floating point
     * 
     * @param delimiter Separator sign (e.g. [blank],[,],[;])
     * @return Modified Julian Date timestamp
     */
    static String getMjd(String delimiter) {
        Calendar cal = Calendar.getInstance(); // get current date and time
        double sec = cal.get(Calendar.SECOND) + cal.get(Calendar.MILLISECOND) / 1000.0;
        return Double.toString(getMjd(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DATE), cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), sec))
                + delimiter;
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

=======
    static String getIsoTimestamp(String separator) {
        TimeZone tz = TimeZone.getDefault();
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        df.setTimeZone(tz);
        return df.format(new Date()) + separator; // ISO 8601 Timestamp format
    }
    
    /**
     * Return a timestamp formated as date, sepearator, time, separator timezone, separator
     * 
     * This format is excellent to process data in spreadsheets apps
     * 
     * Example: 06.09.2016,13:45:42,+0200
     * 1:45:42pm at September 6th, 2018 CEST, comma is the separator
     * 
     * @param separator Separator sign (e.g. [blank],[,],[;])
     * @return date/separator/time timestamp
     */
    static String getDateTimeTz(String separator){
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
     * Example: 06.09.2016,13:45:42
     * 1:45:42pm at September 6th, 2018, comma is the separator
     * 
     * @param separator Separator sign (e.g. [blank],[,],[;])
     * @return date/separator/time timestamp
     */
    static String getDateTimeTimestamp(String separator){
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
    static String getTimeTimestamp(String separator){
        DateFormat tf = new SimpleDateFormat("HH:mm:ss"); // Time
        return tf.format(new Date()) + separator;
    }
    
    static String getModJulianTimestamp(String separator){
        
        return "";
    }
   
>>>>>>> refs/stash
    /**
     * Show alertbox when writing in log file fails
     */
    static void showLogIOException(String ex) {
        JOptionPane.showMessageDialog(SerialLogger.getFrames()[0],
                "ERROR:\n" + ex,
                "Log file error", JOptionPane.ERROR_MESSAGE);
    }
}
