/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.haniibrahim.seriallogger;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author HI
 */
public class HelperTest {
    
    public HelperTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }

    /**
     * Test of getCurrentYear method, of class Helper.
     */
    @Test
    public void testGetCurrentYear() {
        System.out.println("getCurrentYear");
        int expResult = 2018;
        int result = Helper.getCurrentYear();
        System.out.println("   "+result);      
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
    }

    /**
     * Test of getOS method, of class Helper.
     */
    @Test
    public void testGetOS() {
        System.out.println("getOS");
        String expResult = "win";
        String result = Helper.getOS();
        System.out.println("   "+result);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
    }

    /**
     * Test of getIsoTimestamp method, of class Helper.
     */
    @Test
    public void testGetIsoTimestamp() {
        System.out.println("getIsoTimestamp");
        String delimiter = " ";
        String expResult = "";
        String result = Helper.getIsoTimestamp(delimiter);
        System.out.println("   "+result);
//        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
    }

    /**
     * Test of getDateTimeTz method, of class Helper.
     */
    @Test
    public void testGetDateTimeTz() {
        System.out.println("getDateTimeTz");
        String delimiter = " ";
        String expResult = "";
        String result = Helper.getDateTimeTz(delimiter);
        System.out.println("   "+result);
//        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
    }

    /**
     * Test of getDateTimeTimestamp method, of class Helper.
     */
    @Test
    public void testGetDateTimeTimestamp() {
        System.out.println("getDateTimeTimestamp");
        String delimiter = " ";
        String expResult = "";
        String result = Helper.getDateTimeTimestamp(delimiter);
        System.out.println("   "+result);
//        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
    }

    /**
     * Test of getTimeTimestamp method, of class Helper.
     */
    @Test
    public void testGetTimeTimestamp() {
        System.out.println("getTimeTimestamp");
        String delimiter = " ";
        String expResult = "";
        String result = Helper.getTimeTimestamp(delimiter);
        System.out.println("   "+result);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
    }

    /**
     * Test of getDayOfYearTimestamp method, of class Helper.
     */
    @Test
    public void testGetDayOfYearTimestamp() {
        System.out.println("getDayOfYearTimestamp");
        String delimiter = " ";
        String expResult = "";
        String result = Helper.getDayOfYearTimestamp(delimiter);
        System.out.println("   "+result);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
    }

    /**
     * Test of getMjd method, of class Helper.
     */
    @Test
    public void testGetMjd() {
        System.out.println("getMjd");
        String delimiter = " ";
        String expResult = "";
        String result = Helper.getMjd(delimiter);
        System.out.println("   " + result);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
    }
   

    /**
     * Test of getYMDhms method, of class Helper.
     */
    @Test
    public void testGetYMDhmsTz() {
        System.out.println("getYMDhmsTz");
        String delimiter = " ";
        String expResult = "";
        String result = Helper.getYMDhms(delimiter);
        System.out.println("   " + result);
//        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
    }

    /**
     * Test of showLogIOException method, of class Helper.
     */
    @Test
    public void testShowLogIOException() {
//        System.out.println("showLogIOException");
//        String ex = "";
//        Helper.showLogIOException(ex);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
    }
    
}
