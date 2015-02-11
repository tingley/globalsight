package com.globalsight.ling.tm3.core;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.sql.Connection;
import org.hibernate.Transaction;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestMultilingualSharedTm extends TestTM3 {

    static final long SHARED_STORAGE_ID = 1;
    
    @BeforeClass
    public static void setup() throws Exception {
        initializeHibernate();
        System.out.println("TestMultilingualSharedTm.setup");
        currentSession = sessionFactory.openSession();
        
        TM3Manager manager = DefaultManager.create();
        Connection conn = currentSession.connection();
        // Tear down storage pool from old test
        manager.removeStoragePool(conn, SHARED_STORAGE_ID);
        // Recreate it
        manager.createStoragePool(conn, SHARED_STORAGE_ID, inlineAttrs());
        
        init();
    }
    
    // Set up a bilingual TM for each test, start a fresh hibernate session, etc
    @Before
    public void beforeTest() throws Exception {
        currentSession = sessionFactory.openSession();
        Transaction tx = null;
        try {
            tx = currentSession.beginTransaction();
            System.out.println("Creating TM id " + currentTestId);
            TM3Tm<TestData> tm = manager.createMultilingualSharedTm(
                    currentSession, FACTORY, inlineAttrs(), SHARED_STORAGE_ID);
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
    public void testCreateMultilingualSharedTm() throws Exception {
        Transaction tx = null;
        try {
            tx = currentSession.beginTransaction();            
            TM3Tm<TestData> tm2 = manager.getTm(currentSession, FACTORY, currentTestId);
            assertNotNull(tm2);
            assertTrue(tm2 instanceof MultilingualSharedTm);
            
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
