package com.globalsight.everest.persistence;

import static org.junit.Assert.*;

import java.sql.Connection;
import java.util.Vector;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.globalsight.util.ClassUtil;

public class StoredProcCallerTest
{

    @Before
    public void setUp() throws Exception
    {
    }

    @After
    public void tearDown() throws Exception
    {
    }

    @Test
    /**
     * If no exception is thrown out, that should be fine.
     */
    public void testExcuteProcFindTargetTerm()
    {
        try {
            StoredProcCaller sp = new StoredProcCaller();
            // Useless parameter
            Connection connection = null;
            // Add CID
            Vector numberParams = new Vector();
            numberParams.add(new Long(1));
            numberParams.add(new Long(2));
            numberParams.add(new Long(3));
            // Add "TBID" and "language"
            Vector stringParams = new Vector();
            stringParams.add("1000");
            stringParams.add("French");
            stringParams.add("");
            
            ClassUtil.testMethod(sp, "excuteProcFindTargetTerm", connection, numberParams, stringParams);            
        } catch (Exception ex) {
            fail("test excuteProcFindTargetTerm() fail.");
        }
    }

}
