package com.globalsight.ling.tm3.core;

import static org.junit.Assert.assertEquals;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

import com.globalsight.ling.tm3.core.persistence.DistributedId;
import com.globalsight.ling.tm3.core.persistence.SQLUtil;

public class TestDistributedIds {

    static SessionFactory sessionFactory;
    
    @BeforeClass
    public static void init() throws Exception {
        long start = System.currentTimeMillis();
        sessionFactory = HibernateTestConfig.createSessionFactory(20);
        System.out.println("Hibernate initialization took " + 
                (System.currentTimeMillis() - start) + "ms");
        Session session = sessionFactory.openSession();
        Statement s = session.connection().createStatement();
        s.execute("DELETE FROM tm3_id");
        s.close();
    }
    
    @Test
    public void testDistributedIds() throws Exception {
        Session session = null;
        try {
            session = sessionFactory.openSession();
            Connection conn = session.connection();
            DistributedId id1 = new DistributedId("id1", 100);
            // Get some ids
            for (int i = 0; i < 1000; i++) {
                long next = id1.getId(conn);
                assertEquals(i, next);
            }
            id1.destroy(conn);
        }
        catch (Exception e) {
            throw e;
        }
        finally {
            if (session.isOpen()) {
                session.close();
            }
        }
    }
    
    
    // Test multiple threads simultaneously accessing a single id
    @Test
    public void testTenThreads() throws Exception {
        testMultipleThreads(10, 10000, 100, 3000);
    }
    
    //@Test
    public void testHundredThreads() throws Exception {
        testMultipleThreads(100, 100000, 25, 5000);
    }
    
    public void testMultipleThreads(int numThreads, int numVals, int increment, int timeout) throws Exception {
        int[] values = new int[numVals];
        for (int i = 0; i < numVals; i++) {
            values[i] = -1;
        }
        List<Thread> threads = new ArrayList<Thread>();
        for (int i = 0; i < numThreads; i++) {
            threads.add(new Thread(new IDFetcher("testMultipleThreads", values, 
                                        i, numVals / numThreads, increment)));
            threads.get(i).run();
        }
        Thread.sleep(timeout); // Better way to do this?
        for (int i = 0; i < numVals; i++ ) {
            if (values[i] == -1) {
                fail("values[" + i + "] was never set");
            }
        }
        Session session = sessionFactory.openSession();
        new DistributedId("testMultipleThreads").destroy(session.connection());
    }
    
    static class IDFetcher implements Runnable {
        private int[] values;
        private DistributedId id;
        private int self;
        private int count;
        
        public IDFetcher(String idName, int[] values, int self, int count, int increment) {
            this.values = values;
            this.id = new DistributedId(idName, increment);
            this.self = self;
            this.count = count;
        }
        
        @Override
        public void run() {
            Session session = sessionFactory.openSession();
            try {
                Connection conn = session.connection();
                for (int i = 0; i < count; i++) {
                    int next = (int)id.getId(conn);
                    if (values[next] != -1) {
                        fail("(Thread " + self + "): values[" + next + 
                             "] already written by thread " + values[self]);
                    }
                    values[next] = self;
                }
            } catch (SQLException e) {
                fail("(Thread " + self + "): " + e.getMessage());
                throw new RuntimeException(e);
            }
            finally {
                if (session.isOpen()) {
                    session.close();
                }
            }
        }
        
    }
}
