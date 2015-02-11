package com.globalsight.ling.tm3.integration.segmenttm;

import static org.junit.Assert.*;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import javax.transaction.Synchronization;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.globalsight.everest.projecthandler.ProjectTM;
import com.globalsight.everest.tm.Tm;
import com.globalsight.everest.util.system.MockSystemConfiguration;
import com.globalsight.everest.util.system.SystemConfiguration;
import com.globalsight.ling.tm2.BaseTmTu;
import com.globalsight.ling.tm2.SegmentTmTu;
import com.globalsight.ling.tm2.SegmentTmTuv;
import com.globalsight.ling.tm2.TmCoreManager;
import com.globalsight.ling.tm3.core.MockTM3Saver;
import com.globalsight.ling.tm3.core.MockTM3Tm;
import com.globalsight.ling.tm3.core.TM3Event;
import com.globalsight.ling.tm3.core.TM3Locale;
import com.globalsight.ling.tm3.core.TM3Saver;
import com.globalsight.ling.tm3.core.TM3Tm;
import com.globalsight.ling.tm3.core.TM3Tu;
import com.globalsight.ling.tm3.integration.GSTuvData;
import com.globalsight.util.GlobalSightLocale;

public class TestTm3SegmentTmInfo {
    
    @Before
    public void setup() {
        // Install our custom system configuration so that Tm3SegmentTmInfo's
        // static intializer doesn't try to touch the database
        SystemConfiguration.setDebugInstance(new MockSystemConfiguration(
                new HashMap<String, String>() {{
                    put("leverager.targetIndexing", "false");
                }}));
    }
    
    @After
    public void teardown() {
        // Remove the custom SystemConfiguration
        SystemConfiguration.setDebugInstance(null);
    }
    
    // GBS-1804
    @Test
    public void testSavedSegmentCreationIds() throws Exception {
        
        TestTmInfo info = new TestTmInfo();
        
        MockTM3Tm mockTm = createMockTm();
        // Install it so it will be retrieved during the test
        info.setTm3Tm(mockTm);
        
        GlobalSightLocale sourceLocale = new GlobalSightLocale("en", "US", true);
        GlobalSightLocale targetLocale = new GlobalSightLocale("fr", "FR", true);
        // GlobalSightLocale.equals() just checks the id!
        sourceLocale.setId(1);
        targetLocale.setId(2);
        Collection<BaseTmTu> segmentsToSave = 
            getSegmentsToSave(1, sourceLocale, targetLocale, "srcUser", "tgtUser");
        Tm tm = new ProjectTM();
        Set<GlobalSightLocale> targetLocales = Collections.singleton(targetLocale); 
        
        // Call the method I am actually testing
        info.saveToSegmentTm(null, segmentsToSave, sourceLocale, tm,
                        targetLocales, TmCoreManager.SYNC_MERGE, false);
        
        // Get the saver and inspect the results
        MockTM3Saver<GSTuvData> saver = mockTm.getLastSaver();
        assertNotNull(saver);
        
        //List<TM3Saver<GSTuvData>.Tu> saved = saver.getSavedTus();
        List<MockTM3Saver<GSTuvData>.MockTu> saved = saver.getSavedTus();
        for (int i = 0; i < saved.size(); i++) { 
            checkSavedTu(i, saved.get(i), 
                    new GSTuvData(getSourceText(i), sourceLocale),
                    new GSTuvData(getTargetText(i), targetLocale), 
                    sourceLocale, targetLocale,
                    "srcUser", "tgtUser");
        }
    }

    // GBS-1830
    @Test
    public void testTMImportCreationIds() throws Exception {
        
        TestTmInfo info = new TestTmInfo();
        
        MockTM3Tm mockTm = createMockTm();
        // Install it so it will be retrieved during the test
        info.setTm3Tm(mockTm);
        
        GlobalSightLocale sourceLocale = new GlobalSightLocale("en", "US", true);
        GlobalSightLocale targetLocale = new GlobalSightLocale("fr", "FR", true);
        // GlobalSightLocale.equals() just checks the id!
        sourceLocale.setId(1);
        targetLocale.setId(2);
        Collection<BaseTmTu> segmentsToSave = 
            getSegmentsToSave(1, sourceLocale, targetLocale, "srcUser", "tgtUser");
        Tm tm = new ProjectTM();
        Set<GlobalSightLocale> targetLocales = Collections.singleton(targetLocale); 
        
        // Call the method I am actually testing
        info.saveToSegmentTm(null, segmentsToSave, sourceLocale, tm,
                        targetLocales, TmCoreManager.SYNC_MERGE, true);
        
        // Get the saver and inspect the results
        MockTM3Saver<GSTuvData> saver = mockTm.getLastSaver();
        assertNotNull(saver);

        // Note: more work needs to be done for this to be able to test
        // multiple segments.  
        // 1) saveToSegmentTm() produces a new saver for every segment
        // 2) saveToSegmentTm() may not save TUs in the same segment the are
        //    passed in, because of internal hashing used by 
        //    the UniqueSegmentRepository
        List<MockTM3Saver<GSTuvData>.MockTu> saved = saver.getSavedTus();
        for (int i = 0; i < saved.size(); i++) { 
            checkSavedTu(i, saved.get(i), 
                    new GSTuvData(getSourceText(i), sourceLocale),
                    new GSTuvData(getTargetText(i), targetLocale), 
                    sourceLocale, targetLocale,
                    "srcUser", "tgtUser");
        }
    }

    
    public void checkSavedTu(int index, MockTM3Saver<GSTuvData>.MockTu tu,
            GSTuvData srcData, GSTuvData tgtData, TM3Locale srcLocale, 
            TM3Locale tgtLocale, String srcUser, String tgtUser) {
        String msg = "mismatch for tu " + index;
        assertEquals(msg, srcLocale, tu.getSourceTuv().getLocale());
        assertEquals(msg, srcData, tu.getSourceTuv().getContent());
        TM3Event srcEvent = tu.getSourceTuv().getEvent();
        assertEquals(msg, srcUser, srcEvent.getUsername());
        
        List<TM3Saver<GSTuvData>.Tuv> tuvs = tu.getTargets();
        boolean foundTarget = false;
        for (TM3Saver<GSTuvData>.Tuv tuv : tuvs) {
            if (tuv.getLocale().equals(srcLocale)) {
                continue;
            }
            if (tuv.getLocale().equals(tgtLocale)) {
                foundTarget = true;
                // It's the target
                assertEquals(msg, tgtData, tuv.getContent());
                assertEquals(msg, tgtUser, tuv.getEvent().getUsername());
                continue;
            }
            // What is this?
            fail(msg + ": unexpected tuv locale " + tuv.getLocale());
            assertTrue(msg + ": missing target tuv", foundTarget);
        }
    }
    // stop in com.globalsight.ling.tm3.integration.segmenttm.TestTm3SegmentTmInfo.testSavedSegmentCreationIds
    // stop in com.globalsight.ling.tm3.integration.segmenttm.TestTm3SegmentTmInfo.getSegmentsToSave
    
    // Set up the dummy TM, including all the built-in attributes
    MockTM3Tm createMockTm() {
        MockTM3Tm tm = new MockTM3Tm();
        for (SegmentTmAttribute attr : SegmentTmAttribute.values()) {
            tm.addAttribute(attr.name());
        }
        return tm;
    }
    
    private String getSourceText(int index) {
        return "source " + index;
    }
    private String getTargetText(int index) {
        return "target " + index;
    }
    
    List<BaseTmTu> getSegmentsToSave(int count, GlobalSightLocale srcLocale,
             GlobalSightLocale tgtLocale, String srcUser, String tgtUser) {
        List<BaseTmTu> tus = new ArrayList<BaseTmTu>();
        int nextTuId = 1, nextTuvId = 1;
        
        for (int i = 0; i < count; i++) {
            SegmentTmTu tu = new SegmentTmTu(nextTuId++, 1, "plaintext", "text", 
                        true, srcLocale);
            tu.setTranslatable();
            SegmentTmTuv srcTuv = new SegmentTmTuv(nextTuvId++, getSourceText(i), srcLocale);
            srcTuv.setCreationDate(new Timestamp(new Date().getTime()));
            srcTuv.setCreationUser(srcUser);
            tu.addTuv(srcTuv);
            SegmentTmTuv tgtTuv = new SegmentTmTuv(nextTuvId++, getTargetText(i), tgtLocale);
            tgtTuv.setCreationDate(new Timestamp(new Date().getTime()));
            tgtTuv.setCreationUser(tgtUser);
            tu.addTuv(tgtTuv);
            tus.add(tu);
        }
        return tus;
    }
    
    class TestTmInfo extends Tm3SegmentTmInfo {
    
        private TM3Tm<GSTuvData> _tm3tm;
        
        private void setTm3Tm(TM3Tm<GSTuvData> tm) {
            _tm3tm = tm;
        }
        
        @Override
        protected TM3Tm<GSTuvData> getTM3Tm(Session session, Tm tm) {
            return _tm3tm;
        }
        
        @Override
        public void luceneIndexTus(long tmId, Collection<TM3Tu<GSTuvData>> tus) {
            // No-op for testing
        }
        
        @Override
        protected Transaction getTransaction() {
            return new Transaction() {
                @Override
                public void begin() throws HibernateException {
                }
        
                @Override
                public void commit() throws HibernateException {
                }
        
                @Override
                public boolean isActive() throws HibernateException {
                    return false;
                }
        
                @Override
                public void registerSynchronization(Synchronization arg0)
                        throws HibernateException {
                }
        
                @Override
                public void rollback() throws HibernateException {
                }
        
                @Override
                public void setTimeout(int arg0) {
                }
        
                @Override
                public boolean wasCommitted() throws HibernateException {
                    return false;
                }
        
                @Override
                public boolean wasRolledBack() throws HibernateException {
                    return false;
                }
            };        
        }
    }
    
    public static void main(String[] args) {
        org.junit.runner.JUnitCore.main("com.globalsight.ling.tm3.integration.segmenttm.TestTm3SegmentTmInfo");
    }
}
