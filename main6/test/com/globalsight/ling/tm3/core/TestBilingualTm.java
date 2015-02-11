package com.globalsight.ling.tm3.core;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Set;
import java.util.HashSet;

import org.hibernate.Transaction;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.globalsight.persistence.hibernate.HibernateUtil;

public class TestBilingualTm extends TM3Tests {

    @BeforeClass
    public static void setup() throws Exception {
        init();
    }
    
    // Set up a bilingual TM for each test, start a fresh hibernate session, etc
    @Before
    public void beforeTest() throws Exception {
        currentSession = HibernateUtil.getSession();
        Transaction tx = null;
        try {
            tx = currentSession.beginTransaction();
            System.out.println("Creating TM id " + currentTestId);
            TM3Tm<TestData> tm = manager.createBilingualTm(currentSession, FACTORY, inlineAttrs(), EN_US, FR_FR);
            currentSession.flush();
            currentTestId = tm.getId();
            currentTestEvent = tm.addEvent(0, "test", "test " + currentTestId);
            tx.commit();

        }
        catch (Exception e) {
            tx.rollback();
            throw e;
        }
    }
    
    // This leaves the tm in the db for later inspection
    @After
    public void afterTest() throws Exception {
        if (currentSession.isOpen()) {
            currentSession.close();
        }
    }
    
    @Test
    public void testCreateBilingualTm() throws Exception {
        Transaction tx = null;
        try {
            tx = currentSession.beginTransaction();            
            TM3Tm<TestData> tm2 = manager.getTm(currentSession, FACTORY, currentTestId);
            assertNotNull(tm2);
            assertTrue(tm2 instanceof TM3BilingualTm);
            
            cleanupTestDb(manager);
            
            tx = currentSession.beginTransaction();
            TM3Tm<TestData> tm3 = manager.getTm(currentSession, FACTORY, currentTestId);
            assertNull(tm3);
            tx.commit();
        }
        catch (Exception e) {
            tx.rollback();
            throw e;
        }
    }
    
}
