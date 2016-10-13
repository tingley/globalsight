/**
 *  Copyright 2009 Welocalize, Inc. 
 *  
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  
 *  You may obtain a copy of the License at 
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  
 */
package com.globalsight.ling.tm3.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.criterion.Restrictions;
import org.junit.Test;

import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.GlobalSightLocale;

/**
 * Base class for storage testing.
 */
public abstract class TM3Tests
{

    static long currentTestId = 0;

    static TM3Manager manager;

    static TestDataFactory FACTORY = new TestDataFactory();

    // Initialization method. This should be called as part of a
    // @BeforeClass method by all implementations!
    public static void init() throws SQLException
    {
        System.out.println("TM3Tests.init");
        cleanAllDbs();

        EN_US = FACTORY.getLocaleByCode("en_US");
        FR_FR = FACTORY.getLocaleByCode("fr_FR");
        DE_DE = FACTORY.getLocaleByCode("de_de");

        manager = DefaultManager.create();
    }

    static GlobalSightLocale EN_US, FR_FR, DE_DE;

    static Set<TM3Attribute> inlineAttrs()
    {
        Set<TM3Attribute> r = new HashSet<TM3Attribute>();
        r.add(new TM3Attribute("inlineString", new StringType(),
                "inlineString", true));
        r.add(new TM3Attribute("inlineBoolean", new BooleanType(),
                "inlineBoolean", true));
        r.add(new TM3Attribute("optionalString", new StringType(),
                "optionalString", false));
        return r;
    }

    public static class BooleanType extends TM3AttributeValueType.BooleanType
    {
        public BooleanType()
        {
            super(false);
        }
    }

    public static class StringType extends TM3AttributeValueType.StringType
    {
        public StringType()
        {
            super(10, false);
        }
    }

    @SuppressWarnings("unchecked")
    public static void cleanAllDbs() throws SQLException
    {
        Session session = HibernateUtil.getSession();
        TM3Manager manager = DefaultManager.create();
        Transaction tx = null;
        try
        {
            tx = session.beginTransaction();

            List<BaseTm> tms = session.createCriteria(BaseTm.class).list();
            for (BaseTm tm : tms)
            {
                System.out.println("Cleaning up TM " + tm.getId());
                manager.removeTm(tm);
            }

            tx.commit();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            if (tx != null)
            {
                tx.rollback();
            }
        }
        finally
        {
            if (session.isOpen())
            {
                session.close();
            }
        }
    }

    // Test TUV data class -- just a string
    static class TestData implements TM3Data
    {
        private String data;

        TestData(String data)
        {
            this.data = data;
        }

        @Override
        public String getSerializedForm()
        {
            return data;
        }

        @Override
        public long getFingerprint()
        {
            return Fingerprint.fromString(data);
        }

        @Override
        public Iterable<Long> tokenize()
        {
            String[] words = data.split(" ");
            ArrayList fingerprints = new ArrayList(words.length);
            for (String word : words)
            {
                fingerprints.add(Fingerprint.fromString(word));
            }
            return fingerprints;
        }

        @Override
        public boolean equals(Object o)
        {
            return (o instanceof TestData && ((TestData) o).data.equals(data));
        }

        @Override
        public String toString()
        {
            return "TestData(" + data + ")";
        }
    }

    static class TestDataFactory implements TM3DataFactory<TestData>
    {
        private TM3FuzzyMatchScorer<TestData> scorer = new TestScorer();

        @Override
        public TestData fromSerializedForm(TM3Locale locale, String value)
        {
            return new TestData(value);
        }

        @Override
        public TM3FuzzyMatchScorer<TestData> getFuzzyMatchScorer()
        {
            return scorer;
        }

        public void setFuzzyMatchScorer(TM3FuzzyMatchScorer<TestData> scorer)
        {
            this.scorer = scorer;
        }

        @Override
        public GlobalSightLocale getLocaleById(long id)
        {
            return (GlobalSightLocale) HibernateUtil.get(
                    GlobalSightLocale.class, id);
        }

        @Override
        public GlobalSightLocale getLocaleByCode(String code)
        {
            Session session = HibernateUtil.getSession();
            String[] parts = code.split("_");
            if (parts.length != 2)
            {
                return null;
            }
            return (GlobalSightLocale) session
                    .createCriteria(GlobalSightLocale.class)
                    .add(Restrictions.eq("language", parts[0]))
                    .add(Restrictions.eq("country", parts[1])).uniqueResult();
        }

        @Override
        public Configuration extendConfiguration(Configuration cfg)
        {
            return cfg;
        }

    }

    // Variant that returns a specified fingerprint rather
    // than one computed from the content, and also reimplements
    // equals() to just compare the fingerprint.
    static class FixedValueTestData extends TestData
    {
        private long fingerprint;

        FixedValueTestData(String data, long fingerprint)
        {
            super(data);
            this.fingerprint = fingerprint;
        }

        @Override
        public long getFingerprint()
        {
            return fingerprint;
        }

        @Override
        public boolean equals(Object o)
        {
            if (!(o instanceof FixedValueTestData))
            {
                return false;
            }
            FixedValueTestData d = (FixedValueTestData) o;
            return getFingerprint() == d.getFingerprint();
        }

        @Override
        public String toString()
        {
            return "Fixed(" + fingerprint + ")[" + getSerializedForm() + "]";
        }
    }

    static class FixedValueTestDataFactory extends TestDataFactory
    {
        private long fingerprint;

        FixedValueTestDataFactory(long fingerprint)
        {
            this.fingerprint = fingerprint;
        }

        @Override
        public TestData fromSerializedForm(TM3Locale locale, String value)
        {
            return new FixedValueTestData(value, fingerprint);
        }
    }

    // Remove the DB used by the current test
    void cleanupTestDb(TM3Manager manager) throws Exception
    {
        TM3Tm<TestData> tm = manager.getTm(FACTORY, currentTestId);
        if (tm != null)
        {
            manager.removeTm(tm);
        }
    }

    // TODO: handle attribute comparisons
    void expectResults(TM3LeverageResults<TestData> results, Expected... e)
    {
        List<Expected> expected = Arrays.asList(e);
        Iterator<TM3LeverageMatch<TestData>> it = results.getMatches()
                .iterator();
        for (int i = 0; i < expected.size(); i++)
        {
            Expected ex = expected.get(i);
            assertTrue("No result for " + ex, it.hasNext());
            TM3LeverageMatch<TestData> match = it.next();
            ex.check(match, "Mismatch for result " + i);
        }
        assertFalse("More results than expected", it.hasNext());
    }

    Expected expected(TestData data, boolean exact)
    {
        return new Expected(data, exact, new HashMap<TM3Attribute, Object>());
    }

    Expected expected(TestData data, boolean exact,
            Map<TM3Attribute, Object> attributes)
    {
        return new Expected(data, exact, attributes);
    }

    static class Expected
    {
        boolean exact;
        TestData data;
        Map<TM3Attribute, Object> attributes;

        Expected(TestData data, boolean exact,
                Map<TM3Attribute, Object> attributes)
        {
            this.exact = exact;
            this.data = data;
            this.attributes = attributes;
        }

        @Override
        public String toString()
        {
            return "Expected(exact=" + exact + ", " + data + ")";
        }

        public void check(TM3LeverageMatch<TestData> match, String message)
        {
            assertEquals(message, exact, match.isExact());
            assertEquals(message, data, match.getTuv().getContent());
        }
    }

    static class TestScorer implements TM3FuzzyMatchScorer<TestData>
    {

        @Override
        public float score(TestData matchKey, TestData candidate,
                TM3Locale locale)
        {

            // use list instead of set so we count duplicate terms..,
            List<String> w1 = Arrays.asList(matchKey.data.split(" "));
            List<String> w2 = Arrays.asList(candidate.data.split(" "));

            int total = 0;
            for (String s : w1)
            {
                if (w2.contains(s))
                {
                    total++;
                }
            }
            return ((float) total * 2f) / (float) (w1.size() + w2.size());
        }

    }

    //
    // Test methods
    // These just call into the implementations below
    //
//    @Test
//    public void testExactMatch() throws Exception
//    {
//        testExactMatch(manager.getTm(FACTORY, currentTestId), EN_US, FR_FR);
//    }

//    @Test
//    public void testUnicodeContent() throws Exception
//    {
//        testUnicodeContent(manager.getTm(FACTORY, currentTestId), EN_US, FR_FR);
//    }

//    @Test
//    public void testExactMatchingWithAttrs() throws Exception
//    {
//        testExactMatchingWithAttrs(manager.getTm(FACTORY, currentTestId),
//                EN_US, FR_FR);
//    }

//    @Test
//    public void testExactMachingWithNonIdentityAttributes() throws Exception
//    {
//        testExactMachingWithNonIdentityAttributes(
//                manager.getTm(FACTORY, currentTestId), EN_US, FR_FR);
//    }

//    @Test
//    public void testExactMatchingWithInlineAttrs() throws Exception
//    {
//        testExactMatchingWithInlineAttrs(manager.getTm(FACTORY, currentTestId),
//                EN_US, FR_FR);
//    }

    @Test
    public void testExactMatchingWithNoResults() throws Exception
    {
        testExactMatchingWithNoResults(manager.getTm(FACTORY, currentTestId),
                EN_US, FR_FR);
    }

//    @Test
//    public void testExactMatchWithTargetLocales() throws Exception
//    {
//        testExactMatchWithTargetLocales(manager.getTm(FACTORY, currentTestId),
//                EN_US, FR_FR, DE_DE);
//    }

//    @Test
//    public void testFuzzyMatching() throws Exception
//    {
//        testFuzzyMatching(manager.getTm(FACTORY, currentTestId), EN_US, FR_FR);
//    }

//    @Test
//    public void testFuzzyTargetMatching() throws Exception
//    {
//        testFuzzyTargetMatching(manager.getTm(FACTORY, currentTestId), EN_US,
//                FR_FR);
//    }

//    @Test
//    public void testFuzzyMatchingWithAttributes() throws Exception
//    {
//        testFuzzyMatchingWithAttributes(manager.getTm(FACTORY, currentTestId),
//                EN_US, FR_FR);
//    }

//    @Test
//    public void testFuzzyMatchingWithInlineAttributes() throws Exception
//    {
//        testFuzzyMatchingWithInlineAttributes(
//                manager.getTm(FACTORY, currentTestId), EN_US, FR_FR);
//    }

//    @Test
//    public void testFuzzyMatchingWithNoResults() throws Exception
//    {
//        testFuzzyMatchingWithNoResults(manager.getTm(FACTORY, currentTestId),
//                EN_US, FR_FR);
//    }

//    @Test
//    public void testFuzzyMatchingWithTargetLocales() throws Exception
//    {
//        testFuzzyMatchingWithTargetLocales(
//                manager.getTm(FACTORY, currentTestId), EN_US, FR_FR, DE_DE);
//    }

//    @Test
//    public void testTuIdentityWithAttributes() throws Exception
//    {
//        TM3Tm<TestData> tm = manager.getTm(FACTORY, currentTestId);
//        testTuIdentityWithAttributes(tm, EN_US, FR_FR);
//    }

//    @Test
//    public void testModifyTuv() throws Exception
//    {
//        TM3Tm<TestData> tm = manager.getTm(FACTORY, currentTestId);
//        testModifyTuv(tm, EN_US, FR_FR);
//    }

//    @Test
//    public void testAddDeleteTuv() throws Exception
//    {
//        TM3Tm<TestData> tm = manager.getTm(FACTORY, currentTestId);
//        testAddDeleteTuv(tm, EN_US, FR_FR);
//    }
//
//    @Test
//    public void testUpdateTuAttrs() throws Exception
//    {
//        TM3Tm<TestData> tm = manager.getTm(FACTORY, currentTestId);
//        testUpdateTuAttrs(tm, EN_US, FR_FR);
//    }

//    @Test
//    public void testUpdateSourceTuv() throws Exception
//    {
//        TM3Tm<TestData> tm = manager.getTm(FACTORY, currentTestId);
//        testUpdateSourceTuv(tm, EN_US, FR_FR);
//    }
//
//    @Test
//    public void testDontReturnRedundantResults() throws Exception
//    {
//        TM3Tm<TestData> tm = manager.getTm(FACTORY, currentTestId);
//        testDontReturnRedundantResults(tm, EN_US, FR_FR);
//    }
//
//    @Test
//    public void testTuvEvents() throws Exception
//    {
//        TM3Tm<TestData> tm = manager.getTm(FACTORY, currentTestId);
//        testTuvEvents(tm, EN_US, FR_FR);
//    }
//
//    @Test
//    public void testIdempotentMergeMode() throws Exception
//    {
//        TM3Tm<TestData> tm = manager.getTm(FACTORY, currentTestId);
//        testIdempotentMergeMode(tm, EN_US, FR_FR);
//    }

//    @Test
//    public void testMatchType() throws Exception
//    {
//        TM3Tm<TestData> tm = manager.getTm(FACTORY, currentTestId);
//        testMatchType(tm, EN_US, FR_FR);
//    }
//
//    @Test
//    public void testFuzzyMatchThreshold() throws Exception
//    {
//        TM3Tm<TestData> tm = manager.getTm(FACTORY, currentTestId);
//        testFuzzyMatchThresholdAndLimit(tm, EN_US, FR_FR);
//    }
//
//    @Test
//    public void testEmptyFuzzyQuery() throws Exception
//    {
//        TM3Tm<TestData> tm = manager.getTm(FACTORY, currentTestId);
//        testEmptyFuzzyQuery(tm, EN_US, FR_FR);
//    }
//
//    @Test
//    public void testIdenticalScores() throws Exception
//    {
//        TM3Tm<TestData> tm = manager.getTm(FACTORY, currentTestId);
//        testIdenticalScores(tm, EN_US, FR_FR);
//    }
//
//    @Test
//    public void testMaxResults() throws Exception
//    {
//        TM3Tm<TestData> tm = manager.getTm(FACTORY, currentTestId);
//        testMaxResultsFilterOrdering(tm, EN_US, FR_FR);
//    }
//
//    @Test
//    public void testGetAllTuData() throws Exception
//    {
//        TM3Tm<TestData> tm = manager.getTm(FACTORY, currentTestId);
//        testGetAllTuData(tm, EN_US, FR_FR);
//    }
//
//    @Test
//    public void testGetAllTuDataWithDateRange() throws Exception
//    {
//        TM3Tm<TestData> tm = manager.getTm(FACTORY, currentTestId);
//        testGetAllTuDataWithDateRange(tm, EN_US, FR_FR);
//    }
//
//    @Test
//    public void testGetTuDataByLocale() throws Exception
//    {
//        TM3Tm<TestData> tm = manager.getTm(FACTORY, currentTestId);
//        testGetTuDataByLocale(tm, EN_US, FR_FR);
//    }
//
//    @Test
//    public void testGetTuDataByLocaleWithDateRange() throws Exception
//    {
//        TM3Tm<TestData> tm = manager.getTm(FACTORY, currentTestId);
//        testGetAllTuDataWithDateRange(tm, EN_US, FR_FR);
//    }
//
//    @Test
//    public void testGetTuDataById() throws Exception
//    {
//        TM3Tm<TestData> tm = manager.getTm(FACTORY, currentTestId);
//        testGetTuDataById(tm, EN_US, FR_FR);
//    }
//
//    @Test
//    public void testLockOnSave() throws Exception
//    {
//        TM3Tm<TestData> tm = manager.getTm(FACTORY, currentTestId);
//        testLockOnSave(tm, EN_US, FR_FR);
//    }
//
//    @Test
//    public void testLockOnModify() throws Exception
//    {
//        TM3Tm<TestData> tm = manager.getTm(FACTORY, currentTestId);
//        testLockOnModify(tm, EN_US, FR_FR);
//    }
//
//    @Test
//    public void testMultipleTargets() throws Exception
//    {
//        TM3Tm<TestData> tm = manager.getTm(FACTORY, currentTestId);
//        testMultipleTargets(tm, EN_US, FR_FR);
//    }
//
//    @Test
//    public void testIdenticalTargets() throws Exception
//    {
//        TM3Tm<TestData> tm = manager.getTm(FACTORY, currentTestId);
//        testIdenticalTargets(tm, EN_US, FR_FR);
//    }
//
//    @Test
//    public void testMerge() throws Exception
//    {
//        TM3Tm<TestData> tm = manager.getTm(FACTORY, currentTestId);
//        testMerge(tm, EN_US, FR_FR);
//    }
//
//    @Test
//    public void testMergeIdentical() throws Exception
//    {
//        TM3Tm<TestData> tm = manager.getTm(FACTORY, currentTestId);
//        testMergeIdentical(tm, EN_US, FR_FR);
//    }
//
//    @Test
//    public void testMergeWithIdenticalTargets() throws Exception
//    {
//        TM3Tm<TestData> tm = manager.getTm(FACTORY, currentTestId);
//        testMergeWithIdenticalTargets(tm, EN_US, FR_FR);
//    }
//
//    @Test
//    public void testOverwrite() throws Exception
//    {
//        TM3Tm<TestData> tm = manager.getTm(FACTORY, currentTestId);
//        testOverwrite(tm, EN_US, FR_FR);
//    }
//
//    @Test
//    public void testOverwriteOfMultipleTargets() throws Exception
//    {
//        TM3Tm<TestData> tm = manager.getTm(FACTORY, currentTestId);
//        testOverwriteOfMultipleTargets(tm, EN_US, FR_FR);
//    }
//
//    @Test
//    public void testOverwriteOfMultipleTargetsWithIdentical() throws Exception
//    {
//        TM3Tm<TestData> tm = manager.getTm(FACTORY, currentTestId);
//        testOverwriteOfMultipleTargetsWithIdentical(tm, EN_US, FR_FR);
//    }
//
//    @Test
//    public void testOverwriteWithMultipleTargets() throws Exception
//    {
//        TM3Tm<TestData> tm = manager.getTm(FACTORY, currentTestId);
//        testOverwriteWithMultipleTargets(tm, EN_US, FR_FR);
//    }
//
//    @Test
//    public void testOverwriteWithIdenticalTargets() throws Exception
//    {
//        TM3Tm<TestData> tm = manager.getTm(FACTORY, currentTestId);
//        testOverwriteWithIdenticalTargets(tm, EN_US, FR_FR);
//    }
//
//    @Test
//    public void testFixedFingerprintTuvs() throws Exception
//    {
//        testFixedFingerprintTuvs(EN_US, FR_FR);
//    }
//
//    @Test
//    public void testDataByLocaleOrdering() throws Exception
//    {
//        testDataByLocaleOrdering(manager.getTm(FACTORY, currentTestId), EN_US,
//                FR_FR, DE_DE);
//    }

    //
    // Test implementations
    //

//    private void verifyExact(TM3Tm<TestData> tm, TestData src,
//            TM3Locale srcLocale, TestData tgt, TM3Locale tgtLocale,
//            boolean lookupTarget)
//    {
//        TM3LeverageResults<TestData> results = tm.findMatches(src, srcLocale,
//                null, null, TM3MatchType.EXACT, lookupTarget);
//        expectResults(results, expected(src, true));
//        TM3LeverageMatch<TestData> match = results.getMatches().first();
//        assertEquals(100, match.getScore());
//        assertEquals(tgt, getLocaleTuv(match.getTu(), tgtLocale).getContent());
//
//        // Now do the query again and make sure that the same result isn't
//        // returned as both an exact and a fuzzy match.
//        results = tm.findMatches(src, srcLocale, null, null, TM3MatchType.ALL,
//                lookupTarget);
//        Iterator<TM3LeverageMatch<TestData>> it = results.getMatches()
//                .iterator();
//        expected(src, true).check(it.next(),
//                "exact match was not the first result for ALL search");
//        while (it.hasNext())
//        {
//            match = it.next();
//            assertFalse("same result returned twice", match.getTuv()
//                    .getContent().equals(src));
//        }
//    }

    public void testExactMatchingWithNoResults(TM3Tm<TestData> tm,
            GlobalSightLocale srcLocale, final GlobalSightLocale tgtLocale)
            throws Exception
    {
        Session session = HibernateUtil.getSession();
        Transaction tx = null;
        try
        {
            tx = session.beginTransaction();
            tm.addAttribute("test");
            session.flush();

            final TM3Attribute attr1 = tm.getAttributeByName("test");
            assertNotNull(attr1);

            TM3LeverageResults<TestData> results = tm.findMatches(new TestData(
                    "blah blah"), srcLocale, null, TM3Attributes.NONE,
                    TM3MatchType.EXACT, false);
            assertEquals(0, results.getMatches().size());
            tx.commit();

            cleanupTestDb(manager);
        }
        catch (Exception e)
        {
            tx.rollback();
            throw e;
        }
    }

//    public void testFuzzyMatchingWithInlineAttributes(TM3Tm<TestData> tm,
//            GlobalSightLocale srcLocale, final GlobalSightLocale tgtLocale)
//            throws Exception
//    {
//        Session session = HibernateUtil.getSession();
//        Transaction tx = null;
//        try
//        {
//            tx = session.beginTransaction();
//            tm.addAttribute("test");
//            session.flush();
//
//            final TM3Attribute attr1 = tm.getAttributeByName("test");
//            final TM3Attribute attr2 = tm.getAttributeByName("inlineString");
//            final TM3Attribute attr3 = tm.getAttributeByName("inlineBoolean");
//            assertNotNull(attr1);
//
//            // Create some segments.
//            TestData srcData1 = new TestData(
//                    "The quick brown fox ran up the stairs.");
//            TestData srcData2 = new TestData("foo bar baz");
//            TestData srcData3 = new TestData(
//                    "The quick brown fox jumped over the lazy cats.");
//            TestData tgtData1 = new TestData("This is target 1");
//            TestData tgtData2 = new TestData("This is target 2");
//            TestData tgtData3 = new TestData("This is target 3");
//            tm.save(srcLocale, srcData1, TM3Attributes.many(
//                    TM3Attributes.entry(attr1, "yes"),
//                    TM3Attributes.entry(attr2, "YES"),
//                    TM3Attributes.entry(attr3, true)), tgtLocale, tgtData1,
//                    TM3SaveMode.MERGE, currentTestEvent);
//            tm.save(srcLocale, srcData2, TM3Attributes.many(
//                    TM3Attributes.entry(attr1, "yes"),
//                    TM3Attributes.entry(attr2, "YES"),
//                    TM3Attributes.entry(attr3, true)), tgtLocale, tgtData2,
//                    TM3SaveMode.MERGE, currentTestEvent);
//            tm.save(srcLocale, srcData3, TM3Attributes.many(
//                    TM3Attributes.entry(attr1, "no"),
//                    TM3Attributes.entry(attr2, "NO"),
//                    TM3Attributes.entry(attr3, false)), tgtLocale, tgtData3,
//                    TM3SaveMode.MERGE, currentTestEvent);
//            tx.commit();
//
//            // Do a fuzzy query matching on the attribute value 'yes'. This
//            // should match
//            // src1 (attr + fuzzy), but not src2 (attr, but no fuzzy) or src3
//            // (no attr)
//            tx = session.beginTransaction();
//            TestData key = new TestData(
//                    "The quick brown fox jumped over the lazy dogs.");
//
//            // First, search with attributes -- this should catch both
//            TM3LeverageResults<TestData> results = tm.findMatches(key,
//                    srcLocale, null, TM3Attributes.many(
//                            TM3Attributes.entry(attr1, "yes"),
//                            TM3Attributes.entry(attr2, "YES"),
//                            TM3Attributes.entry(attr3, true)),
//                    TM3MatchType.ALL, false);
//            assertEquals(1, results.getMatches().size());
//            TM3LeverageMatch<TestData> first = results.getMatches().first();
//            TM3Tu<TestData> match = first.getTu();
//            System.out.println("Matched [" + first.getTuv().getContent()
//                    + "] with score " + first.getScore());
//            assertEquals(srcData1, first.getTuv().getContent());
//            assertEquals(3, match.getAttributes().size());
//            assertNotNull(match.getAttribute(attr1));
//            assertEquals("yes", match.getAttribute(attr1));
//            tx.commit();
//
//            cleanupTestDb(manager);
//        }
//        catch (Exception e)
//        {
//            tx.rollback();
//            throw e;
//        }
//    }

//    public void testFuzzyMatchingWithNoResults(TM3Tm<TestData> tm,
//            GlobalSightLocale srcLocale, final GlobalSightLocale tgtLocale)
//            throws Exception
//    {
//        Session session = HibernateUtil.getSession();
//        Transaction tx = null;
//        try
//        {
//            tx = session.beginTransaction();
//            tm.addAttribute("test");
//            session.flush();
//
//            final TM3Attribute attr1 = tm.getAttributeByName("test");
//            assertNotNull(attr1);
//
//            TM3LeverageResults<TestData> results = tm.findMatches(new TestData(
//                    "blah blah"), srcLocale, null, TM3Attributes.one(attr1,
//                    "yes"), TM3MatchType.ALL, false);
//            assertEquals(0, results.getMatches().size());
//            tx.commit();
//
//            cleanupTestDb(manager);
//        }
//        catch (Exception e)
//        {
//            tx.rollback();
//            throw e;
//        }
//    }

//    public void testFuzzyMatchingWithTargetLocales(TM3Tm<TestData> tm,
//            GlobalSightLocale srcLocale, final GlobalSightLocale tgtLocale,
//            final GlobalSightLocale altLocale) throws Exception
//    {
//        Session session = HibernateUtil.getSession();
//        Transaction tx = null;
//        try
//        {
//            tx = session.beginTransaction();
//            tm.addAttribute("test");
//            session.flush();
//
//            // Create some segments.
//            TestData srcData1 = new TestData(
//                    "The quick brown fox ran up the stairs.");
//            TestData tgtData1 = new TestData("This is target 1");
//            tm.setIndexTarget(true);
//            tm.save(srcLocale, srcData1, TM3Attributes.NONE, tgtLocale,
//                    tgtData1, TM3SaveMode.MERGE, currentTestEvent);
//            tx.commit();
//
//            tx = session.beginTransaction();
//            TestData key = new TestData(
//                    "The quick brown fox jumped over the lazy dogs.");
//
//            // fuzzy query with the correct target locale
//            TM3LeverageResults<TestData> results = tm.findMatches(key,
//                    srcLocale, Collections.singleton(tgtLocale),
//                    TM3Attributes.NONE, TM3MatchType.ALL, false);
//            expectResults(results, expected(srcData1, false));
//
//            // fuzzy query with the correct target locale plus another
//            Set<GlobalSightLocale> tgtPlusAltLocales = new HashSet<GlobalSightLocale>();
//            Collections.addAll(tgtPlusAltLocales, tgtLocale, altLocale);
//            results = tm.findMatches(key, srcLocale, tgtPlusAltLocales,
//                    TM3Attributes.NONE, TM3MatchType.ALL, false);
//            expectResults(results, expected(srcData1, false));
//
//            // fuzzy query with the wrong target locale
//            results = tm.findMatches(key, srcLocale,
//                    Collections.singleton(altLocale), TM3Attributes.NONE,
//                    TM3MatchType.ALL, false);
//            expectResults(results);
//
//            key = new TestData("This is target");
//
//            // fuzzy target query with the correct source locale
//            results = tm.findMatches(key, tgtLocale,
//                    Collections.singleton(srcLocale), TM3Attributes.NONE,
//                    TM3MatchType.ALL, true);
//            expectResults(results, expected(tgtData1, false));
//
//            // fuzzy target query with the correct source locale plus another
//            Set<GlobalSightLocale> srcPlusAltLocales = new HashSet<GlobalSightLocale>();
//            Collections.addAll(srcPlusAltLocales, srcLocale, altLocale);
//            results = tm.findMatches(key, tgtLocale, tgtPlusAltLocales,
//                    TM3Attributes.NONE, TM3MatchType.ALL, true);
//            expectResults(results, expected(tgtData1, false));
//
//            // fuzzy target query with the wrong source locale
//            results = tm.findMatches(key, tgtLocale,
//                    Collections.singleton(altLocale), TM3Attributes.NONE,
//                    TM3MatchType.ALL, true);
//            expectResults(results);
//
//            tx.commit();
//
//            cleanupTestDb(manager);
//        }
//        catch (Exception e)
//        {
//            tx.rollback();
//            throw e;
//        }
//        finally
//        {
//            tm.setIndexTarget(false);
//        }
//
//    }

    // Make sure the different values for TM3MatchType do what they're supposed
    // to.
//    public void testMatchType(TM3Tm<TestData> tm, GlobalSightLocale srcLocale,
//            final GlobalSightLocale tgtLocale) throws Exception
//    {
//        Session session = HibernateUtil.getSession();
//        Transaction tx = null;
//        try
//        {
//            tx = session.beginTransaction();
//
//            // Create some segments.
//            TestData srcData1 = new TestData("I like to eat flying fish");
//            TestData srcData2 = new TestData("I like to eat soup");
//            TestData srcData3 = new TestData("I swam across the Thames");
//            TestData tgtData1 = new TestData("This is target 1");
//            TestData tgtData2 = new TestData("This is target 2");
//            TestData tgtData3 = new TestData("This is target 3");
//            tm.save(srcLocale, srcData1, TM3Attributes.NONE, tgtLocale,
//                    tgtData1, TM3SaveMode.MERGE, currentTestEvent);
//            tm.save(srcLocale, srcData2, TM3Attributes.NONE, tgtLocale,
//                    tgtData2, TM3SaveMode.MERGE, currentTestEvent);
//            tm.save(srcLocale, srcData3, TM3Attributes.NONE, tgtLocale,
//                    tgtData3, TM3SaveMode.MERGE, currentTestEvent);
//            tx.commit();
//
//            tx = session.beginTransaction();
//            TM3LeverageResults<TestData> results;
//
//            // Test EXACT mode
//            results = tm.findMatches(new TestData("I like to eat flying fish"),
//                    srcLocale, null, TM3Attributes.NONE, TM3MatchType.EXACT,
//                    false);
//            expectResults(results, expected(srcData1, true));
//            results = tm.findMatches(new TestData("I like to eat soup"),
//                    srcLocale, null, TM3Attributes.NONE, TM3MatchType.EXACT,
//                    false);
//            expectResults(results, expected(srcData2, true));
//
//            // Test ALL
//            results = tm.findMatches(new TestData("I like to eat flying fish"),
//                    srcLocale, null, TM3Attributes.NONE, TM3MatchType.ALL,
//                    false);
//            expectResults(results, expected(srcData1, true),
//                    expected(srcData2, false));
//
//            // Test fallback
//            results = tm.findMatches(
//                    new TestData("I like to eat flying toast"), srcLocale,
//                    null, TM3Attributes.NONE, TM3MatchType.FALLBACK, false);
//            expectResults(results, expected(srcData1, false),
//                    expected(srcData2, false));
//
//            tx.commit();
//
//            cleanupTestDb(manager);
//        }
//        catch (Exception e)
//        {
//            tx.rollback();
//            throw e;
//        }
//
//    }

//    public void testTuIdentityWithAttributes(TM3Tm<TestData> tm,
//            GlobalSightLocale srcLocale, final GlobalSightLocale tgtLocale)
//            throws Exception
//    {
//        Session session = HibernateUtil.getSession();
//        Transaction tx = null;
//        try
//        {
//            tx = session.beginTransaction();
//            final TM3Attribute attr1 = tm.addAttribute("test1");
//            final TM3Attribute attr2 = tm.addAttribute("inlineString");
//            final TM3Attribute attr3 = tm.addAttribute("inlineBoolean");
//            session.flush();
//
//            TestData srcData1 = new TestData("source data 1"), tgtData1 = new TestData(
//                    "tgt data 1"), tgtData2 = new TestData("tgt data 2"), tgtData3 = new TestData(
//                    "tgt data 3"), tgtData4 = new TestData("tgt data 4");
//
//            Map<TM3Attribute, Object> attrSet1 = TM3Attributes
//                    .one(attr1, "yes");
//            Map<TM3Attribute, Object> attrSet2 = TM3Attributes.many(
//                    TM3Attributes.entry(attr1, "yes"),
//                    TM3Attributes.entry(attr2, "YES"),
//                    TM3Attributes.entry(attr3, true));
//
//            // Note: these have the same source, but different attribute sets =>
//            // different identities
//            tm.save(srcLocale, srcData1, attrSet1, tgtLocale, tgtData1,
//                    TM3SaveMode.MERGE, currentTestEvent);
//            tm.save(srcLocale, srcData1, attrSet2, tgtLocale, tgtData2,
//                    TM3SaveMode.MERGE, currentTestEvent);
//
//            // These should add to the version with the matching attribute set
//            tm.save(srcLocale, srcData1, attrSet1, tgtLocale, tgtData3,
//                    TM3SaveMode.MERGE, currentTestEvent);
//            tm.save(srcLocale, srcData1, attrSet2, tgtLocale, tgtData4,
//                    TM3SaveMode.MERGE, currentTestEvent);
//
//            TM3LeverageResults<TestData> results = tm.findMatches(srcData1,
//                    srcLocale, null, attrSet1, TM3MatchType.EXACT, false);
//            assertEquals(2, results.getMatches().size());
//            for (TM3LeverageMatch<TestData> match : results.getMatches())
//            {
//                TM3Tu<TestData> tu = match.getTu();
//                if (tu.getAttributes().equals(attrSet1))
//                {
//                    List<TM3Tuv<TestData>> targets = tu.getTargetTuvs();
//                    assertEquals(2, targets.size());
//                    TestData foundData1 = targets.get(0).getContent();
//                    assertTrue(foundData1.equals(tgtData1)
//                            || foundData1.equals(tgtData3));
//                    TestData foundData2 = targets.get(1).getContent();
//                    assertTrue(foundData2.equals(tgtData1)
//                            || foundData2.equals(tgtData3));
//                    assertNotSame(foundData1, foundData2);
//                }
//                else if (tu.getAttributes().equals(attrSet2))
//                {
//                    List<TM3Tuv<TestData>> targets = tu.getTargetTuvs();
//                    assertEquals(2, targets.size());
//                    TestData foundData1 = targets.get(0).getContent();
//                    assertTrue(foundData1.equals(tgtData2)
//                            || foundData1.equals(tgtData4));
//                    TestData foundData2 = targets.get(1).getContent();
//                    assertTrue(foundData2.equals(tgtData2)
//                            || foundData2.equals(tgtData4));
//                    assertNotSame(foundData1, foundData2);
//                }
//                else
//                {
//                    fail("Unexpected attribute set for tu " + tu + ": "
//                            + tu.getAttributes());
//                }
//            }
//
//            tx.commit();
//
//            cleanupTestDb(manager);
//        }
//        catch (Exception e)
//        {
//            tx.rollback();
//            throw e;
//        }
//    }

//    public void testModifyTuv(TM3Tm<TestData> tm, GlobalSightLocale srcLocale,
//            final GlobalSightLocale tgtLocale) throws Exception
//    {
//        Session session = HibernateUtil.getSession();
//        Transaction tx = null;
//        try
//        {
//            tx = session.beginTransaction();
//
//            // Now let's create some segments.
//            TestData srcData1 = new TestData("This is source 1");
//            TestData tgtData1 = new TestData("a b c");
//            TestData tgtData2 = new TestData("c b a");
//            tm.setIndexTarget(true);
//            tm.save(srcLocale, srcData1, TM3Attributes.NONE, tgtLocale,
//                    tgtData1, TM3SaveMode.MERGE, currentTestEvent);
//            tx.commit();
//
//            // Make sure the data is all intact
//            tx = session.beginTransaction();
//            TestData key = new TestData("This is source 1");
//            TM3LeverageResults<TestData> results = tm.findMatches(key,
//                    srcLocale, null, null, TM3MatchType.EXACT, false);
//            assertEquals(1, results.getMatches().size());
//            TM3LeverageMatch<TestData> match = results.getMatches().first();
//            assertTrue(match.isExact());
//            TM3Tu<TestData> tu = match.getTu();
//            assertNotNull(getLocaleTuv(tu, tgtLocale));
//            assertEquals(tgtData1, getLocaleTuv(tu, tgtLocale).getContent());
//
//            // Now update one of the tuvs, but not the other
//            getLocaleTuv(tu, tgtLocale).setContent(tgtData2);
//            tm.modifyTu(tu, currentTestEvent);
//            tx.commit();
//
//            // Now make sure it persisted
//            tx = session.beginTransaction();
//            results = tm.findMatches(key, srcLocale, null, null,
//                    TM3MatchType.EXACT, false);
//            assertEquals(1, results.getMatches().size());
//            tu = results.getMatches().first().getTu();
//            assertNotNull(getLocaleTuv(tu, tgtLocale));
//            assertEquals(tgtData2, getLocaleTuv(tu, tgtLocale).getContent());
//
//            // make sure the indexes were updated
//            // note that tgtData1 and tgtData2, while similar according to
//            // TestScorer, have no trigrams in common, in order to isolate the
//            // trigram indexes from the scoring
//
//            // fuzzy matches on the new target
//            key = new TestData("d c b a");
//            results = tm.findMatches(key, tgtLocale, null, TM3Attributes.NONE,
//                    TM3MatchType.ALL, true);
//            expectResults(results, expected(tgtData2, false));
//
//            // no fuzzy matches on the old target
//            key = new TestData("a b c d");
//            results = tm.findMatches(key, tgtLocale, null, TM3Attributes.NONE,
//                    TM3MatchType.ALL, true);
//            expectResults(results);
//
//            tx.commit();
//            cleanupTestDb(manager);
//        }
//        catch (Exception e)
//        {
//            tx.rollback();
//            throw e;
//        }
//        finally
//        {
//            tm.setIndexTarget(false);
//        }
//    }

//    public void testAddDeleteTuv(TM3Tm<TestData> tm,
//            GlobalSightLocale srcLocale, final GlobalSightLocale tgtLocale)
//            throws Exception
//    {
//        Session session = HibernateUtil.getSession();
//        Transaction tx = null;
//        try
//        {
//            tx = session.beginTransaction();
//
//            // Now let's create some segments.
//            TestData srcData1 = new TestData("This is source 1");
//            TestData tgtData1 = new TestData("a b c");
//            TestData tgtData2 = new TestData("c b a");
//            tm.setIndexTarget(true);
//            tm.save(srcLocale, srcData1, TM3Attributes.NONE, tgtLocale,
//                    tgtData1, TM3SaveMode.MERGE, currentTestEvent);
//            tx.commit();
//
//            // Make sure the data is all intact
//            tx = session.beginTransaction();
//            TestData key = new TestData("This is source 1");
//            TM3LeverageResults<TestData> results = tm.findMatches(key,
//                    srcLocale, null, null, TM3MatchType.EXACT, false);
//            assertEquals(1, results.getMatches().size());
//            TM3LeverageMatch<TestData> match = results.getMatches().first();
//            assertTrue(match.isExact());
//            TM3Tu<TestData> tu = match.getTu();
//            assertNotNull(getLocaleTuv(tu, tgtLocale));
//            assertEquals(tgtData1, getLocaleTuv(tu, tgtLocale).getContent());
//
//            // Delete the old TUV. Add another.
//            tu.removeTargetTuvs();
//            tu.addTargetTuv(FR_FR, tgtData2, currentTestEvent);
//            tm.modifyTu(tu, currentTestEvent);
//            tx.commit();
//
//            // Now make sure it persisted
//            tx = session.beginTransaction();
//            results = tm.findMatches(key, srcLocale, null, null,
//                    TM3MatchType.EXACT, false);
//            assertEquals(1, results.getMatches().size());
//            tu = results.getMatches().first().getTu();
//            assertNotNull(getLocaleTuv(tu, tgtLocale));
//            assertEquals(tgtData2, getLocaleTuv(tu, tgtLocale).getContent());
//
//            // make sure the indexes were updated
//            // note that tgtData1 and tgtData2, while similar according to
//            // TestScorer, have no trigrams in common, in order to isolate the
//            // trigram indexes from the scoring
//
//            // fuzzy matches on the new target
//            key = new TestData("d c b a");
//            results = tm.findMatches(key, tgtLocale, null, TM3Attributes.NONE,
//                    TM3MatchType.ALL, true);
//            expectResults(results, expected(tgtData2, false));
//
//            // no fuzzy matches on the old target
//            key = new TestData("a b c d");
//            results = tm.findMatches(key, tgtLocale, null, TM3Attributes.NONE,
//                    TM3MatchType.ALL, true);
//            expectResults(results);
//
//            cleanupTestDb(manager);
//        }
//        catch (Exception e)
//        {
//            tx.rollback();
//            throw e;
//        }
//        finally
//        {
//            tm.setIndexTarget(false);
//        }
//    }

//    public void testUpdateTuAttrs(TM3Tm<TestData> tm,
//            GlobalSightLocale srcLocale, final GlobalSightLocale tgtLocale)
//            throws Exception
//    {
//        Session session = HibernateUtil.getSession();
//        Transaction tx = null;
//        try
//        {
//            tx = session.beginTransaction();
//            TM3Attribute attr1 = tm.addAttribute("attr1");
//            TM3Attribute attr2 = tm.addAttribute("inlineString");
//
//            // Now let's create some segments.
//            TestData srcData1 = new TestData("This is source 1");
//            TestData tgtData1 = new TestData("This is target 1");
//            tm.save(srcLocale, srcData1, TM3Attributes.many(
//                    TM3Attributes.entry(attr1, "attr1_val1"),
//                    TM3Attributes.entry(attr2, "attr2_val1")), tgtLocale,
//                    tgtData1, TM3SaveMode.MERGE, currentTestEvent);
//            tx.commit();
//
//            // Make sure the data is all intact
//            tx = session.beginTransaction();
//            TestData key = new TestData("This is source 1");
//            TM3LeverageResults<TestData> results = tm.findMatches(key,
//                    srcLocale, null, null, TM3MatchType.EXACT, false);
//            assertEquals(1, results.getMatches().size());
//            TM3LeverageMatch<TestData> match = results.getMatches().first();
//            assertTrue(match.isExact());
//            TM3Tu<TestData> tu = match.getTu();
//            assertNotNull(getLocaleTuv(tu, tgtLocale));
//            assertEquals(tgtData1, getLocaleTuv(tu, tgtLocale).getContent());
//            Map<TM3Attribute, Object> attrs = tu.getAttributes();
//            assertEquals(2, attrs.entrySet().size());
//            assertEquals("attr1_val1", attrs.get(attr1));
//            assertEquals("attr2_val1", attrs.get(attr2));
//
//            // Now update the attributes
//            tu.setAttribute(attr1, "attr1_val2");
//            attrs.put(attr2, "attr2_val2");
//            tm.modifyTu(tu, currentTestEvent);
//            tx.commit();
//
//            // Now make sure it persisted
//            tx = session.beginTransaction();
//            results = tm.findMatches(key, srcLocale, null, null,
//                    TM3MatchType.EXACT, false);
//            assertEquals(1, results.getMatches().size());
//            tu = results.getMatches().first().getTu();
//            assertNotNull(getLocaleTuv(tu, tgtLocale));
//            assertEquals(tgtData1, getLocaleTuv(tu, tgtLocale).getContent());
//            attrs = tu.getAttributes();
//            assertEquals(2, attrs.entrySet().size());
//            assertEquals("attr1_val2", attrs.get(attr1));
//            assertEquals("attr2_val2", attrs.get(attr2));
//            tx.commit();
//
//            cleanupTestDb(manager);
//        }
//        catch (Exception e)
//        {
//            tx.rollback();
//            throw e;
//        }
//    }

//    public void testUpdateSourceTuv(TM3Tm<TestData> tm,
//            GlobalSightLocale srcLocale, final GlobalSightLocale tgtLocale)
//            throws Exception
//    {
//        Session session = HibernateUtil.getSession();
//        Transaction tx = null;
//        try
//        {
//            tx = session.beginTransaction();
//
//            // Now let's create some segments.
//            TestData srcData1 = new TestData("a b c");
//            TestData srcData2 = new TestData("c b a");
//            TestData tgtData1 = new TestData("This is target 1");
//            tm.setIndexTarget(true);
//            tm.save(srcLocale, srcData1, TM3Attributes.NONE, tgtLocale,
//                    tgtData1, TM3SaveMode.MERGE, currentTestEvent);
//            tx.commit();
//
//            // Make sure the data is all intact
//            tx = session.beginTransaction();
//            TM3LeverageResults<TestData> results = tm.findMatches(srcData1,
//                    srcLocale, null, null, TM3MatchType.EXACT, false);
//            assertEquals(1, results.getMatches().size());
//            TM3LeverageMatch<TestData> match = results.getMatches().first();
//            assertTrue(match.isExact());
//            TM3Tu<TestData> tu = match.getTu();
//            assertNotNull(getLocaleTuv(tu, tgtLocale));
//            assertEquals(tgtData1, getLocaleTuv(tu, tgtLocale).getContent());
//
//            // Update the source
//            tu.getSourceTuv().setContent(srcData2);
//            tm.modifyTu(tu, currentTestEvent);
//            tx.commit();
//
//            // Now make sure it persisted
//            tx = session.beginTransaction();
//            results = tm.findMatches(srcData2, srcLocale, null, null,
//                    TM3MatchType.EXACT, false);
//            assertEquals(1, results.getMatches().size());
//            tu = results.getMatches().first().getTu();
//            assertNotNull(getLocaleTuv(tu, tgtLocale));
//            assertEquals(tgtData1, getLocaleTuv(tu, tgtLocale).getContent());
//
//            // make sure the indexes were updated
//            // note that srcData1 and srcData2, while similar according to
//            // TestScorer, have no trigrams in common, in order to isolate the
//            // trigram indexes from the scoring
//
//            // fuzzy matches on the new source
//            TestData key = new TestData("d c b a");
//            results = tm.findMatches(key, srcLocale, null, TM3Attributes.NONE,
//                    TM3MatchType.ALL, false);
//            expectResults(results, expected(srcData2, false));
//
//            // no fuzzy matches on the old source
//            key = new TestData("a b c d");
//            results = tm.findMatches(key, srcLocale, null, TM3Attributes.NONE,
//                    TM3MatchType.ALL, false);
//            expectResults(results);
//
//            // fuzzy matches on the new target
//            key = new TestData("This is target");
//            results = tm.findMatches(key, tgtLocale, null, TM3Attributes.NONE,
//                    TM3MatchType.ALL, true);
//            expectResults(results, expected(tgtData1, false));
//
//            cleanupTestDb(manager);
//        }
//        catch (Exception e)
//        {
//            tx.rollback();
//            throw e;
//        }
//        finally
//        {
//            tm.setIndexTarget(false);
//        }
//    }

//    public void testDontReturnRedundantResults(TM3Tm<TestData> tm,
//            GlobalSightLocale srcLocale, final GlobalSightLocale tgtLocale)
//            throws Exception
//    {
//        Session session = HibernateUtil.getSession();
//        Transaction tx = null;
//        try
//        {
//            tx = session.beginTransaction();
//            // Now let's create some segments.
//            TestData srcData1 = new TestData("This is source 1");
//            TestData tgtData1 = new TestData("This is target 1");
//            tm.save(srcLocale, srcData1, null, tgtLocale, tgtData1,
//                    TM3SaveMode.MERGE, currentTestEvent);
//            tx.commit();
//
//            // Now let's do an exact match query
//            tx = session.beginTransaction();
//            TM3LeverageResults<TestData> results = tm.findMatches(srcData1,
//                    srcLocale, null, null, TM3MatchType.ALL, false);
//            assertEquals(1, results.getMatches().size());
//            expectResults(results, expected(srcData1, true));
//            tx.commit();
//
//            cleanupTestDb(manager);
//        }
//        catch (Exception e)
//        {
//            tx.rollback();
//            throw e;
//        }
//    }

    // Make sure that we don't store identical target TUVs for a single TU
//    @SuppressWarnings("serial")
//    public void testIdempotentMergeMode(TM3Tm<TestData> tm,
//            GlobalSightLocale srcLocale, final GlobalSightLocale tgtLocale)
//            throws Exception
//    {
//        Session session = HibernateUtil.getSession();
//        Transaction tx = null;
//        try
//        {
//            tx = session.beginTransaction();
//            TestData srcData1 = new TestData("This is source 1");
//            TestData tgtData1 = new TestData("This is target 1");
//            tm.save(srcLocale, srcData1, TM3Attributes.NONE, tgtLocale,
//                    tgtData1, TM3SaveMode.MERGE, currentTestEvent);
//            tx.commit();
//
//            tx = session.beginTransaction();
//            TestData tgtData2 = new TestData("This is target 1"); // identical
//                                                                  // to tgtData1
//            tm.save(srcLocale, srcData1, TM3Attributes.NONE, tgtLocale,
//                    tgtData2, TM3SaveMode.MERGE, currentTestEvent);
//            tx.commit();
//
//            tx = session.beginTransaction();
//            TM3LeverageResults<TestData> results = tm.findMatches(srcData1,
//                    srcLocale, null, TM3Attributes.NONE, TM3MatchType.EXACT,
//                    false);
//            expectResults(results, expected(srcData1, true));
//            TM3LeverageMatch<TestData> match = results.getMatches().first();
//            TM3Tu<TestData> tu = match.getTu();
//            assertNotNull(tu);
//            List<TM3Tuv<TestData>> tuvs = tu.getTargetTuvs();
//            assertEquals(1, tuvs.size());
//            assertEquals(tgtLocale, tuvs.get(0).getLocale());
//            assertEquals(tgtData1, tuvs.get(0).getContent());
//            tx.commit();
//
//            // Now do the same thing again, but using the multi-target save
//            // signature.
//            tx = session.beginTransaction();
//            TestData srcData2 = new TestData("This is source 2");
//            final TestData target2 = new TestData("Target 2");
//            tm.save(srcLocale, srcData2, TM3Attributes.NONE, tgtLocale,
//                    target2, TM3SaveMode.MERGE, currentTestEvent);
//            tx.commit();
//
//            tx = session.beginTransaction();
//            // This should fail
//            tm.save(srcLocale, srcData2, TM3Attributes.NONE,
//                    new HashMap<TM3Locale, TestData>()
//                    {
//                        {
//                            put(tgtLocale, target2);
//                        }
//                    }, TM3SaveMode.MERGE, currentTestEvent);
//            tx.commit();
//
//            tx = session.beginTransaction();
//            results = tm.findMatches(srcData2, srcLocale, null,
//                    TM3Attributes.NONE, TM3MatchType.EXACT, false);
//            expectResults(results, expected(srcData2, true));
//            match = results.getMatches().first();
//            tu = match.getTu();
//            assertNotNull(tu);
//            tuvs = tu.getTargetTuvs();
//            assertEquals(1, tuvs.size());
//            assertEquals(tgtLocale, tuvs.get(0).getLocale());
//            assertEquals(target2, tuvs.get(0).getContent());
//            tx.commit();
//        }
//        catch (Exception e)
//        {
//            tx.rollback();
//            throw e;
//        }
//    }

//    public void testTuvEvents(TM3Tm<TestData> tm, GlobalSightLocale srcLocale,
//            GlobalSightLocale tgtLocale) throws Exception
//    {
//        Session session = HibernateUtil.getSession();
//        Transaction tx = null;
//        try
//        {
//            tx = session.beginTransaction();
//            final TM3Event event1 = tm
//                    .addEvent(0, "tester", "Initial creation");
//
//            TestData srcData1 = new TestData("This is source 1");
//            TestData tgtData1 = new TestData("This is target 1");
//            TestData tgtData2 = new TestData("This is target 2");
//            tm.save(srcLocale, srcData1, TM3Attributes.NONE, tgtLocale,
//                    tgtData1, TM3SaveMode.MERGE, event1);
//            tx.commit();
//
//            // Make sure the data is all intact
//            tx = session.beginTransaction();
//            TM3Event loadedEvent = tm.getEvent(event1.getId());
//            assertNotNull(loadedEvent);
//            assertEquals("tester", loadedEvent.getUsername());
//            assertEquals("Initial creation", loadedEvent.getArgument());
//            TestData key = new TestData("This is source 1");
//            TM3LeverageResults<TestData> results = tm.findMatches(key,
//                    srcLocale, null, TM3Attributes.NONE, TM3MatchType.EXACT,
//                    false);
//            assertEquals(1, results.getMatches().size());
//            TM3LeverageMatch<TestData> match = results.getMatches().first();
//            assertTrue(match.isExact());
//            TM3Tu<TestData> tu = match.getTu();
//            assertNotNull(getLocaleTuv(tu, tgtLocale));
//            TM3Tuv<TestData> targetTuv = getLocaleTuv(tu, tgtLocale);
//            assertEquals(tgtData1, targetTuv.getContent());
//
//            assertEquals(event1, targetTuv.getLatestEvent());
//            assertEquals(event1, targetTuv.getFirstEvent());
//
//            // Sleep a little bit to bump timestamps
//            Thread.sleep(1000);
//
//            // Now update one of the tuvs, but not the other
//            final TM3Event event2 = tm
//                    .addEvent(0, "tester", "TUV modification");
//            getLocaleTuv(tu, tgtLocale).setContent(tgtData2);
//            tm.modifyTu(tu, event2);
//            tx.commit();
//
//            tx = session.beginTransaction();
//            results = tm.findMatches(key, srcLocale, null, null,
//                    TM3MatchType.EXACT, false);
//            assertEquals(1, results.getMatches().size());
//            tu = results.getMatches().first().getTu();
//            targetTuv = getLocaleTuv(tu, tgtLocale);
//            assertNotNull(targetTuv);
//            assertEquals(tgtData2, targetTuv.getContent());
//
//            assertEquals(event2, targetTuv.getLatestEvent());
//            assertEquals(event1, targetTuv.getFirstEvent());
//
//            tx.commit();
//
//            cleanupTestDb(manager);
//        }
//        catch (Exception e)
//        {
//            tx.rollback();
//            throw e;
//        }
//    }

//    @SuppressWarnings("serial")
//    public void testFuzzyMatchThresholdAndLimit(TM3Tm<TestData> tm,
//            GlobalSightLocale srcLocale, GlobalSightLocale tgtLocale)
//            throws Exception
//    {
//        Session session = HibernateUtil.getSession();
//        Transaction tx = null;
//        try
//        {
//            tx = session.beginTransaction();
//
//            TestData srcData1 = new TestData("A B E F");
//            TestData tgtData1 = new TestData("This is target 1");
//            TestData srcData2 = new TestData("A B C G");
//            TestData tgtData2 = new TestData("This is target 2");
//            tm.save(srcLocale, srcData1, TM3Attributes.NONE, tgtLocale,
//                    tgtData1, TM3SaveMode.MERGE, currentTestEvent);
//            tm.save(srcLocale, srcData2, TM3Attributes.NONE, tgtLocale,
//                    tgtData2, TM3SaveMode.MERGE, currentTestEvent);
//            tx.commit();
//
//            tx = session.beginTransaction();
//
//            // Search with threshold 50 -- this should pick up both
//            TM3LeverageResults<TestData> results = tm.findMatches(new TestData(
//                    "A B C D"), srcLocale, null, TM3Attributes.NONE,
//                    TM3MatchType.ALL, false, 1000, 50);
//            assertEquals(2, results.getMatches().size());
//            expectResults(results, expected(srcData2, false),
//                    expected(srcData1, false));
//
//            // Now seach with threshold 75 -- this will only pick up the better
//            // match
//            results = tm.findMatches(new TestData("A B C D"), srcLocale, null,
//                    TM3Attributes.NONE, TM3MatchType.ALL, false, 1000, 75);
//            assertEquals(1, results.getMatches().size());
//            expectResults(results, expected(srcData2, false));
//
//            // Now search with the generous threshold, but only allow one result
//            results = tm.findMatches(new TestData("A B C D"), srcLocale, null,
//                    TM3Attributes.NONE, TM3MatchType.ALL, false, 1, 50);
//            assertEquals(1, results.getMatches().size());
//            expectResults(results, expected(srcData2, false));
//
//            tx.commit();
//
//            cleanupTestDb(manager);
//        }
//        catch (Exception e)
//        {
//            tx.rollback();
//            throw e;
//        }
//    }

//    public void testEmptyFuzzyQuery(TM3Tm<TestData> tm,
//            GlobalSightLocale srcLocale, GlobalSightLocale tgtLocale)
//            throws Exception
//    {
//        Session session = HibernateUtil.getSession();
//        Transaction tx = null;
//        try
//        {
//            tx = session.beginTransaction();
//
//            TestData srcData1 = new TestData("A B C D E F");
//            TestData tgtData1 = new TestData("This is target 1");
//            tm.save(srcLocale, srcData1, TM3Attributes.NONE, tgtLocale,
//                    tgtData1, TM3SaveMode.MERGE, currentTestEvent);
//            tx.commit();
//
//            tx = session.beginTransaction();
//
//            TM3LeverageResults<TestData> results = tm.findMatches(new TestData(
//                    "   "), srcLocale, null, TM3Attributes.NONE,
//                    TM3MatchType.ALL, false, 1000, 50);
//            assertEquals(0, results.getMatches().size());
//            tx.commit();
//
//            cleanupTestDb(manager);
//        }
//        catch (Exception e)
//        {
//            tx.rollback();
//            throw e;
//        }
//    }

//    public void testIdenticalScores(TM3Tm<TestData> tm,
//            GlobalSightLocale srcLocale, GlobalSightLocale tgtLocale)
//            throws Exception
//    {
//        TM3FuzzyMatchScorer<TestData> oldScorer = FACTORY.getFuzzyMatchScorer();
//        Session session = HibernateUtil.getSession();
//        Transaction tx = null;
//        try
//        {
//            tx = session.beginTransaction();
//
//            // Use a new scorer that only returns one result
//            FACTORY.setFuzzyMatchScorer(new TM3FuzzyMatchScorer<TestData>()
//            {
//                @Override
//                public float score(TestData matchKey, TestData candidate,
//                        TM3Locale locale)
//                {
//                    return 0.78f;
//                }
//            });
//
//            tm.save(srcLocale, new TestData("A B C D E"), TM3Attributes.NONE,
//                    tgtLocale, new TestData("This is target 1"),
//                    TM3SaveMode.MERGE, currentTestEvent);
//            tm.save(srcLocale, new TestData("A B C D F"), TM3Attributes.NONE,
//                    tgtLocale, new TestData("This is target 2"),
//                    TM3SaveMode.MERGE, currentTestEvent);
//            tx.commit();
//
//            tx = session.beginTransaction();
//
//            // Search with threshold 50 -- this should pick up both
//            TM3LeverageResults<TestData> results = tm.findMatches(new TestData(
//                    "A B C"), srcLocale, null, TM3Attributes.NONE,
//                    TM3MatchType.ALL, false, 1000, 50);
//            assertEquals(2, results.getMatches().size());
//            for (TM3LeverageMatch<TestData> match : results.getMatches())
//            {
//                assertFalse(match.isExact());
//                assertEquals(78, match.getScore());
//            }
//            tx.commit();
//
//            cleanupTestDb(manager);
//        }
//        catch (Exception e)
//        {
//            tx.rollback();
//            throw e;
//        }
//        finally
//        {
//            FACTORY.setFuzzyMatchScorer(oldScorer);
//        }
//    }

//    public void testMaxResultsFilterOrdering(TM3Tm<TestData> tm,
//            GlobalSightLocale srcLocale, GlobalSightLocale tgtLocale)
//            throws Exception
//    {
//        TM3FuzzyMatchScorer<TestData> oldScorer = FACTORY.getFuzzyMatchScorer();
//        Session session = HibernateUtil.getSession();
//        Transaction tx = null;
//        try
//        {
//            tx = session.beginTransaction();
//
//            // Use a new scorer that only returns one result
//            FACTORY.setFuzzyMatchScorer(new TM3FuzzyMatchScorer<TestData>()
//            {
//                private float next = 0.99f;
//
//                @Override
//                public float score(TestData matchKey, TestData candidate,
//                        TM3Locale locale)
//                {
//                    float f = next;
//                    next -= 0.01f;
//                    return f;
//                }
//            });
//
//            for (int i = 0; i < 10; i++)
//            {
//                tm.save(srcLocale, new TestData("A B C " + i),
//                        TM3Attributes.NONE, tgtLocale, new TestData("Target "
//                                + i), TM3SaveMode.MERGE, currentTestEvent);
//            }
//            tx.commit();
//
//            tx = session.beginTransaction();
//
//            // Search with threshold 50 -- this should pick up both
//            TM3LeverageResults<TestData> results = tm.findMatches(new TestData(
//                    "A B C"), srcLocale, null, TM3Attributes.NONE,
//                    TM3MatchType.ALL, false, 5, 50);
//            assertEquals(5, results.getMatches().size());
//            Iterator<TM3LeverageMatch<TestData>> it = results.getMatches()
//                    .iterator();
//            assertEquals(99, it.next().getScore());
//            assertEquals(98, it.next().getScore());
//            assertEquals(97, it.next().getScore());
//            assertEquals(96, it.next().getScore());
//            assertEquals(95, it.next().getScore());
//            tx.commit();
//
//            cleanupTestDb(manager);
//        }
//        catch (Exception e)
//        {
//            tx.rollback();
//            throw e;
//        }
//        finally
//        {
//            FACTORY.setFuzzyMatchScorer(oldScorer);
//        }
//    }

//    public void testGetAllTuData(TM3Tm<TestData> tm,
//            GlobalSightLocale srcLocale, GlobalSightLocale tgtLocale)
//            throws Exception
//    {
//        Session session = HibernateUtil.getSession();
//        Transaction tx = null;
//        try
//        {
//            tx = session.beginTransaction();
//            // Populate the TM with a bunch of stuff
//            for (int i = 0; i < 1000; i++)
//            {
//                tm.save(srcLocale, new TestData(Integer.toString(i)),
//                        TM3Attributes.NONE, tgtLocale,
//                        new TestData(Integer.toString(i)), TM3SaveMode.MERGE,
//                        currentTestEvent);
//            }
//            tx.commit();
//            tx = session.beginTransaction();
//
//            // Verify the data without range restriction
//            TM3Handle<TestData> handle = tm.getAllData(null, null);
//            assertEquals("unexpected tu count", 1000, handle.getCount());
//            Iterator<TM3Tu<TestData>> it = handle.iterator();
//            for (int i = 0; i < 1000; i++)
//            {
//                assertTrue("No tu " + i, it.hasNext());
//                assertEquals("Tu value mismatch", Integer.toString(i), it
//                        .next().getSourceTuv().getContent().data);
//            }
//            assertFalse("Too many TU returned", it.hasNext());
//            handle.purge();
//            assertEquals("purge failed", 0, handle.getCount());
//            tx.commit();
//            cleanupTestDb(manager);
//        }
//        catch (Exception e)
//        {
//            tx.rollback();
//            throw e;
//        }
//    }

//    public void testGetAllTuDataWithDateRange(TM3Tm<TestData> tm,
//            GlobalSightLocale srcLocale, GlobalSightLocale tgtLocale)
//            throws Exception
//    {
//        Session session = HibernateUtil.getSession();
//        Transaction tx = null;
//        try
//        {
//            tx = session.beginTransaction();
//
//            // Make a couple fake events
//            Date now = new Date();
//            TM3Event first = tm.addEvent(0, "test", "first event");
//            TM3Event second = tm.addEvent(0, "test", "second event");
//            TM3Event third = tm.addEvent(0, "test", "third event");
//
//            // Set the second and third timestamps in the future
//            second.setTimestamp(new Date(
//                    second.getTimestamp().getTime() + 60 * 1000));
//            third.setTimestamp(new Date(
//                    third.getTimestamp().getTime() + 120 * 1000));
//
//            session.flush();
//
//            // Populate the TM with a bunch of stuff
//            for (int i = 0; i < 350; i++)
//            {
//                tm.save(srcLocale, new TestData(Integer.toString(i)),
//                        TM3Attributes.NONE, tgtLocale,
//                        new TestData(Integer.toString(i)), TM3SaveMode.MERGE,
//                        first);
//            }
//            for (int i = 350; i < 710; i++)
//            {
//                tm.save(srcLocale, new TestData(Integer.toString(i)),
//                        TM3Attributes.NONE, tgtLocale,
//                        new TestData(Integer.toString(i)), TM3SaveMode.MERGE,
//                        second);
//            }
//            for (int i = 710; i < 1000; i++)
//            {
//                tm.save(srcLocale, new TestData(Integer.toString(i)),
//                        TM3Attributes.NONE, tgtLocale,
//                        new TestData(Integer.toString(i)), TM3SaveMode.MERGE,
//                        third);
//            }
//            tx.commit();
//            tx = session.beginTransaction();
//
//            // The date range should encompass only the 'second' event
//            Date start = new Date(now.getTime() + 30 * 1000);
//            Date end = new Date(now.getTime() + 90 * 1000);
//
//            TM3Handle<TestData> handle = tm.getAllData(start, end);
//            assertEquals("unexpected tu count", 360, handle.getCount());
//            Iterator<TM3Tu<TestData>> it = handle.iterator();
//            for (int i = 350; i < 710; i++)
//            {
//                assertTrue("No tu " + i, it.hasNext());
//                assertEquals("Tu value mismatch", Integer.toString(i), it
//                        .next().getSourceTuv().getContent().data);
//            }
//            assertFalse("Too many TU returned", it.hasNext());
//
//            handle.purge();
//            assertEquals("purge failed", 0, handle.getCount());
//
//            // Now get a handle to everything
//
//            handle = tm.getAllData(null, null);
//            assertEquals("unexpected post-purge tu count", 640,
//                    handle.getCount());
//
//            tx.commit();
//            cleanupTestDb(manager);
//        }
//        catch (Exception e)
//        {
//            tx.rollback();
//            throw e;
//        }
//    }

//    public void testGetTuDataByLocale(TM3Tm<TestData> tm,
//            GlobalSightLocale srcLocale, GlobalSightLocale tgtLocale)
//            throws Exception
//    {
//        Session session = HibernateUtil.getSession();
//        Transaction tx = null;
//        try
//        {
//            tx = session.beginTransaction();
//            // Populate the TM with a bunch of stuff
//            for (int i = 0; i < 1000; i++)
//            {
//                tm.save(srcLocale, new TestData(Integer.toString(i)),
//                        TM3Attributes.NONE, tgtLocale,
//                        new TestData(Integer.toString(i)), TM3SaveMode.MERGE,
//                        currentTestEvent);
//            }
//            tx.commit();
//            tx = session.beginTransaction();
//
//            // Verify the data without range restriction
//            TM3Handle<TestData> handle = tm.getDataByLocale(tgtLocale, null,
//                    null);
//            assertEquals("unexpected tu count", 1000, handle.getCount());
//            Iterator<TM3Tu<TestData>> it = handle.iterator();
//            for (int i = 0; i < 1000; i++)
//            {
//                assertTrue("No tu " + i, it.hasNext());
//                assertEquals("Tu value mismatch", Integer.toString(i), it
//                        .next().getSourceTuv().getContent().data);
//            }
//            assertFalse("Too many TU returned", it.hasNext());
//            tx.commit();
//            cleanupTestDb(manager);
//        }
//        catch (Exception e)
//        {
//            tx.rollback();
//            throw e;
//        }
//    }

//    public void testGetTuDataByLocaleWithDateRange(TM3Tm<TestData> tm,
//            GlobalSightLocale srcLocale, GlobalSightLocale tgtLocale)
//            throws Exception
//    {
//        Session session = HibernateUtil.getSession();
//        Transaction tx = null;
//        try
//        {
//            tx = session.beginTransaction();
//
//            // Make a couple fake events
//            Date now = new Date();
//            TM3Event first = tm.addEvent(0, "test", "first event");
//            TM3Event second = tm.addEvent(0, "test", "second event");
//            TM3Event third = tm.addEvent(0, "test", "third event");
//
//            // Set the second and third timestamps in the future
//            second.setTimestamp(new Date(
//                    second.getTimestamp().getTime() + 60 * 1000));
//            third.setTimestamp(new Date(
//                    third.getTimestamp().getTime() + 120 * 1000));
//
//            session.flush();
//
//            // Populate the TM with a bunch of stuff
//            for (int i = 0; i < 350; i++)
//            {
//                tm.save(srcLocale, new TestData(Integer.toString(i)),
//                        TM3Attributes.NONE, tgtLocale,
//                        new TestData(Integer.toString(i)), TM3SaveMode.MERGE,
//                        first);
//            }
//            for (int i = 350; i < 710; i++)
//            {
//                tm.save(srcLocale, new TestData(Integer.toString(i)),
//                        TM3Attributes.NONE, tgtLocale,
//                        new TestData(Integer.toString(i)), TM3SaveMode.MERGE,
//                        second);
//            }
//            for (int i = 710; i < 1000; i++)
//            {
//                tm.save(srcLocale, new TestData(Integer.toString(i)),
//                        TM3Attributes.NONE, tgtLocale,
//                        new TestData(Integer.toString(i)), TM3SaveMode.MERGE,
//                        third);
//            }
//            tx.commit();
//            tx = session.beginTransaction();
//
//            // The date range should encompass only the 'second' event
//            Date start = new Date(now.getTime() + 30 * 1000);
//            Date end = new Date(now.getTime() + 90 * 1000);
//
//            TM3Handle<TestData> handle = tm.getDataByLocale(tgtLocale, start,
//                    end);
//            assertEquals("unexpected tu count", 360, handle.getCount());
//            Iterator<TM3Tu<TestData>> it = handle.iterator();
//            for (int i = 350; i < 710; i++)
//            {
//                assertTrue("No tu " + i, it.hasNext());
//                assertEquals("Tu value mismatch", Integer.toString(i), it
//                        .next().getSourceTuv().getContent().data);
//            }
//            assertFalse("Too many TU returned", it.hasNext());
//
//            tx.commit();
//            cleanupTestDb(manager);
//        }
//        catch (Exception e)
//        {
//            tx.rollback();
//            throw e;
//        }
//    }

//    public void testGetTuDataById(TM3Tm<TestData> tm,
//            GlobalSightLocale srcLocale, GlobalSightLocale tgtLocale)
//            throws Exception
//    {
//        Session session = HibernateUtil.getSession();
//        Transaction tx = null;
//        try
//        {
//            tx = session.beginTransaction();
//
//            TM3Event event = tm.addEvent(0, "test", "event");
//            session.flush();
//
//            // Populate the TM with a bunch of stuff
//            List<Long> ids = new ArrayList<Long>();
//            for (int i = 0; i < 20; i++)
//            {
//                TM3Tu<TestData> tu = tm.save(srcLocale,
//                        new TestData(Integer.toString(i)), TM3Attributes.NONE,
//                        tgtLocale, new TestData(Integer.toString(i)),
//                        TM3SaveMode.MERGE, event);
//                if (tu.getId() % 2 == 0)
//                {
//                    ids.add(tu.getId());
//                }
//            }
//            tx.commit();
//            tx = session.beginTransaction();
//
//            TM3Handle<TestData> handle = tm.getDataById(ids);
//
//            assertEquals("unexpected tu count", ids.size(), handle.getCount());
//
//            for (TM3Tu<TestData> tu : handle)
//            {
//                assertTrue("unexpected tu id " + tu.getId(),
//                        tu.getId() % 2 == 0);
//                assertTrue("unexpected tu id " + tu.getId(),
//                        ids.contains(tu.getId()));
//            }
//
//            handle.purge();
//            tx.commit();
//
//            tx = session.beginTransaction();
//
//            int actualCount = 0;
//            for (TM3Tu<TestData> tu : tm.getDataById(ids))
//            {
//                actualCount++;
//            }
//
//            assertEquals("failed to delete " + actualCount + " tu", 0,
//                    actualCount);
//
//            tx.commit();
//            cleanupTestDb(manager);
//        }
//        catch (Exception e)
//        {
//            tx.rollback();
//            throw e;
//        }
//    }

    // Engineer a 2-thread race to insert the same source TUV with separate
    // target TUVs.
    // Ensure that this only produes a single TU.
//    public void testLockOnSave(final TM3Tm<TestData> testTm,
//            final GlobalSightLocale srcLocale, final GlobalSightLocale tgtLocale)
//            throws Exception
//    {
//
//        final TM3Event event = testTm.addEvent(1, "test", null);
//        final TM3Attribute attr = testTm.addAttribute("test");
//        Session session = HibernateUtil.getSession();
//        Transaction tx = null;
//        try
//        {
//            tx = session.beginTransaction();
//            session.flush();
//            tx.commit();
//        }
//        catch (Exception e)
//        {
//            throw new RuntimeException(e);
//        }
//        finally
//        {
//            if (tx != null)
//            {
//                if (tx.isActive())
//                {
//                    tx.rollback();
//                }
//            }
//        }
//
//        class Saver extends TesterThread
//        {
//            Saver(int id)
//            {
//                super(id);
//            }
//
//            @Override
//            public void test() throws Exception
//            {
//                TM3Tm<TestData> tm = manager.getTm(FACTORY, testTm.getId());
//                tm.createSaver()
//                        .tu(new TestData("src1"), srcLocale, event)
//                        .attr(attr, "value")
//                        .target(new TestData("tgt" + getId()), tgtLocale, event)
//                        .save(TM3SaveMode.MERGE);
//                // Pause so whoever got here first will wait for the other
//                // thread to also start trying to save
//                Thread.sleep(250);
//            }
//        }
//
//        Thread t1 = new Thread(new Saver(1));
//        Thread t2 = new Thread(new Saver(2));
//        t1.start();
//        t2.start();
//        Thread.sleep(2000);
//
//        List<TM3Tu<TestData>> alltu = collect(testTm.getAllData(null, null)
//                .iterator());
//        assertEquals(1, alltu.size());
//        List<TM3Tuv<TestData>> alltuv = alltu.get(0).getAllTuv();
//        assertEquals(3, alltuv.size());
//    }

    // Create a TU with a single translation. Then race to add a second
    // translation while
    // also changing the original translation to have the save content as the
    // proposed second one.
//    public void testLockOnModify(final TM3Tm<TestData> testTm,
//            final GlobalSightLocale srcLocale, final GlobalSightLocale tgtLocale)
//            throws Exception
//    {
//
//        final TM3Event event = testTm.addEvent(1, "test", null);
//        final TestData srcData = new TestData("src1");
//        final TestData origTgtData = new TestData("dst1");
//        final TestData newTgtData = new TestData("dst2");
//
//        Session session = HibernateUtil.getSession();
//        Transaction tx = null;
//        try
//        {
//            tx = session.beginTransaction();
//            testTm.createSaver().tu(srcData, srcLocale, event)
//                    .target(origTgtData, tgtLocale, event)
//                    .save(TM3SaveMode.MERGE);
//            session.flush();
//            tx.commit();
//        }
//        catch (Exception e)
//        {
//            throw new RuntimeException(e);
//        }
//        finally
//        {
//            if (tx != null)
//            {
//                if (tx.isActive())
//                {
//                    tx.rollback();
//                }
//            }
//        }

//        class Saver extends TesterThread
//        {
//            Saver(int id)
//            {
//                super(id);
//            }
//
//            @Override
//            public void test() throws Exception
//            {
//                TM3Tm<TestData> tm = manager.getTm(FACTORY, testTm.getId());
//                tm.createSaver().tu(srcData, srcLocale, event)
//                        .target(newTgtData, tgtLocale, event)
//                        .save(TM3SaveMode.MERGE);
//                // Pause so whoever got here first will wait for the other
//                // thread to also start trying to save
//                Thread.sleep(250);
//            }
//        }

//        class Modifier extends TesterThread
//        {
//            Modifier(int id)
//            {
//                super(id);
//            }
//
//            @Override
//            public void test() throws Exception
//            {
//                TM3Tm<TestData> tm = manager.getTm(FACTORY, testTm.getId());
//                TM3LeverageResults<TestData> results = tm.findMatches(srcData,
//                        srcLocale, Collections.singleton(tgtLocale),
//                        TM3Attributes.NONE, TM3MatchType.EXACT, false);
//                assertEquals(1, results.getMatches().size());
//                TM3Tu<TestData> tu = results.getMatches().first().getTu();
//                assertEquals(srcData, tu.getSourceTuv().getContent());
//                getLocaleTuv(tu, tgtLocale).setContent(newTgtData);
//                tm.modifyTu(tu, event);
//                // Pause so whoever got here first will wait for the other
//                // thread to also start trying to save
//                Thread.sleep(250);
//            }
//        }
//
//        Thread t1 = new Thread(new Modifier(1));
//        Thread t2 = new Thread(new Saver(2));
//        t2.start();
//        t1.start();
//        Thread.sleep(2000);
//
//        List<TM3Tu<TestData>> alltu = collect(testTm.getAllData(null, null)
//                .iterator());
//        assertEquals(1, alltu.size());
//        List<TM3Tuv<TestData>> alltuv = alltu.get(0).getAllTuv();
//        assertEquals(2, alltuv.size());
//        assertEquals(newTgtData, getLocaleTuv(alltu.get(0), tgtLocale)
//                .getContent());
//    }

    /**
     * Runnable to perform an action inside a session transaction.
     */
    abstract class TesterThread implements Runnable
    {
        private int id;

        TesterThread(int id)
        {
            this.id = id;
        }

        @Override
        public void run()
        {
            try
            {
                test();
            }
            catch (Exception e)
            {
                throw new RuntimeException(e);
            }
        }

        public int getId()
        {
            return id;
        }

        abstract void test() throws Exception;
    }

    private <T> List<T> collect(Iterator<T> it)
    {
        List<T> l = new ArrayList<T>();
        while (it.hasNext())
        {
            l.add(it.next());
        }
        return l;
    }

//    public void testMultipleTargets(TM3Tm<TestData> tm,
//            GlobalSightLocale srcLocale, final GlobalSightLocale tgtLocale)
//            throws Exception
//    {
//        Session session = HibernateUtil.getSession();
//        Transaction tx = null;
//        try
//        {
//            tx = session.beginTransaction();
//            // Now let's create some segments.
//            TestData srcData1 = new TestData("This is source 1");
//            TestData tgtData1 = new TestData("This is target 1");
//            TestData tgtData2 = new TestData("This is target 2");
//            TM3Saver<TestData> saver = tm.createSaver();
//            saver.tu(srcData1, srcLocale, currentTestEvent)
//                    .target(tgtData1, tgtLocale, currentTestEvent)
//                    .target(tgtData2, tgtLocale, currentTestEvent)
//                    .save(TM3SaveMode.MERGE);
//            tx.commit();
//
//            tx = session.beginTransaction();
//            // make sure both target tuvs are there
//            TM3LeverageResults<TestData> results = tm.findMatches(srcData1,
//                    srcLocale, null, TM3Attributes.NONE, TM3MatchType.EXACT,
//                    false);
//            expectResults(results, expected(srcData1, true));
//            TM3Tu<TestData> tu = results.getMatches().first().getTu();
//            List<TM3Tuv<TestData>> targetTuvs = tu.getLocaleTuvs(tgtLocale);
//            assertEquals(2, targetTuvs.size());
//            assertTrue((targetTuvs.get(0).getContent().equals(tgtData1) && targetTuvs
//                    .get(1).getContent().equals(tgtData2))
//                    || (targetTuvs.get(0).getContent().equals(tgtData2) && targetTuvs
//                            .get(1).getContent().equals(tgtData1)));
//            tx.commit();
//
//            cleanupTestDb(manager);
//        }
//        catch (Exception e)
//        {
//            tx.rollback();
//            throw e;
//        }
//    }

//    public void testIdenticalTargets(TM3Tm<TestData> tm,
//            GlobalSightLocale srcLocale, final GlobalSightLocale tgtLocale)
//            throws Exception
//    {
//        Session session = HibernateUtil.getSession();
//        Transaction tx = null;
//        try
//        {
//            tx = session.beginTransaction();
//            // Now let's create some segments.
//            TestData srcData1 = new TestData("This is source 1");
//            TestData tgtData1 = new TestData("This is target 1");
//            TM3Saver<TestData> saver = tm.createSaver();
//            saver.tu(srcData1, srcLocale, currentTestEvent)
//                    .target(tgtData1, tgtLocale, currentTestEvent)
//                    .target(tgtData1, tgtLocale, currentTestEvent)
//                    .save(TM3SaveMode.MERGE);
//            tx.commit();
//
//            tx = session.beginTransaction();
//            // make sure there is only one target tuv
//            TM3LeverageResults<TestData> results = tm.findMatches(srcData1,
//                    srcLocale, null, TM3Attributes.NONE, TM3MatchType.EXACT,
//                    false);
//            expectResults(results, expected(srcData1, true));
//            TM3Tu<TestData> tu = results.getMatches().first().getTu();
//            List<TM3Tuv<TestData>> targetTuvs = tu.getLocaleTuvs(tgtLocale);
//            assertEquals(1, targetTuvs.size());
//            assertTrue(targetTuvs.get(0).getContent().equals(tgtData1));
//            tx.commit();
//
//            cleanupTestDb(manager);
//        }
//        catch (Exception e)
//        {
//            tx.rollback();
//            throw e;
//        }
//    }

//    public void testMerge(TM3Tm<TestData> tm, GlobalSightLocale srcLocale,
//            final GlobalSightLocale tgtLocale) throws Exception
//    {
//        Session session = HibernateUtil.getSession();
//        Transaction tx = null;
//        try
//        {
//            tx = session.beginTransaction();
//            // Now let's create some segments.
//            TestData srcData1 = new TestData("This is source 1");
//            TestData tgtData1 = new TestData("This is target 1");
//            TestData tgtData2 = new TestData("This is target 2");
//            TM3Saver<TestData> saver = tm.createSaver();
//            saver.tu(srcData1, srcLocale, currentTestEvent)
//                    .target(tgtData1, tgtLocale, currentTestEvent)
//                    .save(TM3SaveMode.MERGE);
//            tx.commit();
//
//            tx = session.beginTransaction();
//            saver = tm.createSaver();
//            saver.tu(srcData1, srcLocale, currentTestEvent)
//                    .target(tgtData2, tgtLocale, currentTestEvent)
//                    .save(TM3SaveMode.MERGE);
//            tx.commit();
//
//            tx = session.beginTransaction();
//            // make sure both target tuvs are there
//            TM3LeverageResults<TestData> results = tm.findMatches(srcData1,
//                    srcLocale, null, TM3Attributes.NONE, TM3MatchType.EXACT,
//                    false);
//            expectResults(results, expected(srcData1, true));
//            TM3Tu<TestData> tu = results.getMatches().first().getTu();
//            List<TM3Tuv<TestData>> targetTuvs = tu.getLocaleTuvs(tgtLocale);
//            assertEquals(2, targetTuvs.size());
//            assertTrue((targetTuvs.get(0).getContent().equals(tgtData1) && targetTuvs
//                    .get(1).getContent().equals(tgtData2))
//                    || (targetTuvs.get(0).getContent().equals(tgtData2) && targetTuvs
//                            .get(1).getContent().equals(tgtData1)));
//            tx.commit();
//
//            cleanupTestDb(manager);
//        }
//        catch (Exception e)
//        {
//            tx.rollback();
//            throw e;
//        }
//    }

//    public void testMergeIdentical(TM3Tm<TestData> tm,
//            GlobalSightLocale srcLocale, final GlobalSightLocale tgtLocale)
//            throws Exception
//    {
//        Session session = HibernateUtil.getSession();
//        Transaction tx = null;
//        try
//        {
//            tx = session.beginTransaction();
//            // Now let's create some segments.
//            TestData srcData1 = new TestData("This is source 1");
//            TestData tgtData1 = new TestData("This is target 1");
//            TM3Saver<TestData> saver = tm.createSaver();
//            saver.tu(srcData1, srcLocale, currentTestEvent)
//                    .target(tgtData1, tgtLocale, currentTestEvent)
//                    .save(TM3SaveMode.MERGE);
//            tx.commit();
//
//            tx = session.beginTransaction();
//            saver = tm.createSaver();
//            saver.tu(srcData1, srcLocale, currentTestEvent)
//                    .target(tgtData1, tgtLocale, currentTestEvent)
//                    .save(TM3SaveMode.MERGE);
//            tx.commit();
//
//            tx = session.beginTransaction();
//            // make sure there is only one target tuv
//            TM3LeverageResults<TestData> results = tm.findMatches(srcData1,
//                    srcLocale, null, TM3Attributes.NONE, TM3MatchType.EXACT,
//                    false);
//            expectResults(results, expected(srcData1, true));
//            TM3Tu<TestData> tu = results.getMatches().first().getTu();
//            List<TM3Tuv<TestData>> targetTuvs = tu.getLocaleTuvs(tgtLocale);
//            assertEquals(1, targetTuvs.size());
//            assertTrue(targetTuvs.get(0).getContent().equals(tgtData1));
//            tx.commit();
//
//            cleanupTestDb(manager);
//        }
//        catch (Exception e)
//        {
//            tx.rollback();
//            throw e;
//        }
//    }

//    public void testMergeWithIdenticalTargets(TM3Tm<TestData> tm,
//            GlobalSightLocale srcLocale, final GlobalSightLocale tgtLocale)
//            throws Exception
//    {
//        Session session = HibernateUtil.getSession();
//        Transaction tx = null;
//        try
//        {
//            tx = session.beginTransaction();
//            // Now let's create some segments.
//            TestData srcData1 = new TestData("This is source 1");
//            TestData tgtData1 = new TestData("This is target 1");
//            TestData tgtData2 = new TestData("This is target 2");
//            TM3Saver<TestData> saver = tm.createSaver();
//            saver.tu(srcData1, srcLocale, currentTestEvent)
//                    .target(tgtData1, tgtLocale, currentTestEvent)
//                    .save(TM3SaveMode.MERGE);
//            tx.commit();
//
//            tx = session.beginTransaction();
//            saver = tm.createSaver();
//            saver.tu(srcData1, srcLocale, currentTestEvent)
//                    .target(tgtData2, tgtLocale, currentTestEvent)
//                    .target(tgtData2, tgtLocale, currentTestEvent)
//                    .save(TM3SaveMode.MERGE);
//            tx.commit();
//
//            tx = session.beginTransaction();
//            // make sure there are just two target tuvs
//            TM3LeverageResults<TestData> results = tm.findMatches(srcData1,
//                    srcLocale, null, TM3Attributes.NONE, TM3MatchType.EXACT,
//                    false);
//            expectResults(results, expected(srcData1, true));
//            TM3Tu<TestData> tu = results.getMatches().first().getTu();
//            List<TM3Tuv<TestData>> targetTuvs = tu.getLocaleTuvs(tgtLocale);
//            assertEquals(2, targetTuvs.size());
//            assertTrue((targetTuvs.get(0).getContent().equals(tgtData1) && targetTuvs
//                    .get(1).getContent().equals(tgtData2))
//                    || (targetTuvs.get(0).getContent().equals(tgtData2) && targetTuvs
//                            .get(1).getContent().equals(tgtData1)));
//            tx.commit();
//
//            cleanupTestDb(manager);
//        }
//        catch (Exception e)
//        {
//            tx.rollback();
//            throw e;
//        }
//    }

//    public void testOverwrite(TM3Tm<TestData> tm, GlobalSightLocale srcLocale,
//            final GlobalSightLocale tgtLocale) throws Exception
//    {
//        Session session = HibernateUtil.getSession();
//        Transaction tx = null;
//        try
//        {
//            tx = session.beginTransaction();
//            // Now let's create some segments.
//            TestData srcData1 = new TestData("This is source 1");
//            TestData tgtData1 = new TestData("This is target 1");
//            TestData tgtData2 = new TestData("This is target 1");
//            TM3Saver<TestData> saver = tm.createSaver();
//            saver.tu(srcData1, srcLocale, currentTestEvent)
//                    .target(tgtData1, tgtLocale, currentTestEvent)
//                    .save(TM3SaveMode.MERGE);
//            tx.commit();
//
//            tx = session.beginTransaction();
//            saver = tm.createSaver();
//            saver.tu(srcData1, srcLocale, currentTestEvent)
//                    .target(tgtData2, tgtLocale, currentTestEvent)
//                    .save(TM3SaveMode.OVERWRITE);
//            tx.commit();
//
//            tx = session.beginTransaction();
//            // make sure there is only one target tuv
//            TM3LeverageResults<TestData> results = tm.findMatches(srcData1,
//                    srcLocale, null, TM3Attributes.NONE, TM3MatchType.EXACT,
//                    false);
//            expectResults(results, expected(srcData1, true));
//            TM3Tu<TestData> tu = results.getMatches().first().getTu();
//            List<TM3Tuv<TestData>> targetTuvs = tu.getLocaleTuvs(tgtLocale);
//            assertEquals(1, targetTuvs.size());
//            assertTrue(targetTuvs.get(0).getContent().equals(tgtData2));
//            tx.commit();
//
//            cleanupTestDb(manager);
//        }
//        catch (Exception e)
//        {
//            tx.rollback();
//            throw e;
//        }
//    }

    // not all old targets were being removed due to a bug in
    // TM3Tu.removeTargetTuvByLocale
//    public void testOverwriteOfMultipleTargets(TM3Tm<TestData> tm,
//            GlobalSightLocale srcLocale, final GlobalSightLocale tgtLocale)
//            throws Exception
//    {
//        Session session = HibernateUtil.getSession();
//        Transaction tx = null;
//        try
//        {
//            tx = session.beginTransaction();
//            // Now let's create some segments.
//            TestData srcData1 = new TestData("This is source 1");
//            TestData tgtData1 = new TestData("This is target 1");
//            TestData tgtData2 = new TestData("This is target 2");
//            TestData tgtData3 = new TestData("This is target 3");
//            TM3Saver<TestData> saver = tm.createSaver();
//            saver.tu(srcData1, srcLocale, currentTestEvent)
//                    .target(tgtData1, tgtLocale, currentTestEvent)
//                    .target(tgtData2, tgtLocale, currentTestEvent)
//                    .save(TM3SaveMode.MERGE);
//            tx.commit();
//
//            tx = session.beginTransaction();
//            saver = tm.createSaver();
//            saver.tu(srcData1, srcLocale, currentTestEvent)
//                    .target(tgtData3, tgtLocale, currentTestEvent)
//                    .save(TM3SaveMode.OVERWRITE);
//            tx.commit();
//
//            tx = session.beginTransaction();
//            // make sure there is only one target tuv
//            TM3LeverageResults<TestData> results = tm.findMatches(srcData1,
//                    srcLocale, null, TM3Attributes.NONE, TM3MatchType.EXACT,
//                    false);
//            expectResults(results, expected(srcData1, true));
//            TM3Tu<TestData> tu = results.getMatches().first().getTu();
//            List<TM3Tuv<TestData>> targetTuvs = tu.getLocaleTuvs(tgtLocale);
//            assertEquals(1, targetTuvs.size());
//            assertTrue(targetTuvs.get(0).getContent().equals(tgtData3));
//            tx.commit();
//
//            cleanupTestDb(manager);
//        }
//        catch (Exception e)
//        {
//            tx.rollback();
//            throw e;
//        }
//    }

    // not all old targets were being removed due to a bug in
    // TM3Tu.removeTargetTuvByLocale
//    public void testOverwriteOfMultipleTargetsWithIdentical(TM3Tm<TestData> tm,
//            GlobalSightLocale srcLocale, final GlobalSightLocale tgtLocale)
//            throws Exception
//    {
//        Session session = HibernateUtil.getSession();
//        Transaction tx = null;
//        try
//        {
//            tx = session.beginTransaction();
//            // Now let's create some segments.
//            TestData srcData1 = new TestData("This is source 1");
//            TestData tgtData1 = new TestData("This is target 1");
//            TestData tgtData2 = new TestData("This is target 2");
//            TM3Saver<TestData> saver = tm.createSaver();
//            saver.tu(srcData1, srcLocale, currentTestEvent)
//                    .target(tgtData1, tgtLocale, currentTestEvent)
//                    .target(tgtData2, tgtLocale, currentTestEvent)
//                    .save(TM3SaveMode.MERGE);
//            tx.commit();
//
//            tx = session.beginTransaction();
//            saver = tm.createSaver();
//            saver.tu(srcData1, srcLocale, currentTestEvent)
//                    .target(tgtData1, tgtLocale, currentTestEvent)
//                    .target(tgtData2, tgtLocale, currentTestEvent)
//                    .save(TM3SaveMode.OVERWRITE);
//            tx.commit();
//
//            tx = session.beginTransaction();
//            // make sure both target tuvs are there
//            TM3LeverageResults<TestData> results = tm.findMatches(srcData1,
//                    srcLocale, null, TM3Attributes.NONE, TM3MatchType.EXACT,
//                    false);
//            expectResults(results, expected(srcData1, true));
//            TM3Tu<TestData> tu = results.getMatches().first().getTu();
//            List<TM3Tuv<TestData>> targetTuvs = tu.getLocaleTuvs(tgtLocale);
//            assertEquals(2, targetTuvs.size());
//            assertTrue((targetTuvs.get(0).getContent().equals(tgtData1) && targetTuvs
//                    .get(1).getContent().equals(tgtData2))
//                    || (targetTuvs.get(0).getContent().equals(tgtData2) && targetTuvs
//                            .get(1).getContent().equals(tgtData1)));
//            tx.commit();
//
//            cleanupTestDb(manager);
//        }
//        catch (Exception e)
//        {
//            tx.rollback();
//            throw e;
//        }
//    }

    // this used to fail because with every new target, we removed all existing
    // targets in that locale, even if they were also new
//    public void testOverwriteWithMultipleTargets(TM3Tm<TestData> tm,
//            GlobalSightLocale srcLocale, final GlobalSightLocale tgtLocale)
//            throws Exception
//    {
//        Session session = HibernateUtil.getSession();
//        Transaction tx = null;
//        try
//        {
//            tx = session.beginTransaction();
//            // Now let's create some segments.
//            TestData srcData1 = new TestData("This is source 1");
//            TestData tgtData1 = new TestData("This is target 1");
//            TestData tgtData2 = new TestData("This is target 2");
//            TestData tgtData3 = new TestData("This is target 3");
//            TM3Saver<TestData> saver = tm.createSaver();
//            saver.tu(srcData1, srcLocale, currentTestEvent)
//                    .target(tgtData1, tgtLocale, currentTestEvent)
//                    .save(TM3SaveMode.MERGE);
//            tx.commit();
//
//            tx = session.beginTransaction();
//            saver = tm.createSaver();
//            saver.tu(srcData1, srcLocale, currentTestEvent)
//                    .target(tgtData2, tgtLocale, currentTestEvent)
//                    .target(tgtData3, tgtLocale, currentTestEvent)
//                    .save(TM3SaveMode.OVERWRITE);
//            tx.commit();
//
//            tx = session.beginTransaction();
//            // make sure both target tuvs are there
//            TM3LeverageResults<TestData> results = tm.findMatches(srcData1,
//                    srcLocale, null, TM3Attributes.NONE, TM3MatchType.EXACT,
//                    false);
//            expectResults(results, expected(srcData1, true));
//            TM3Tu<TestData> tu = results.getMatches().first().getTu();
//            List<TM3Tuv<TestData>> targetTuvs = tu.getLocaleTuvs(tgtLocale);
//            assertEquals(2, targetTuvs.size());
//            assertTrue((targetTuvs.get(0).getContent().equals(tgtData2) && targetTuvs
//                    .get(1).getContent().equals(tgtData3))
//                    || (targetTuvs.get(0).getContent().equals(tgtData3) && targetTuvs
//                            .get(1).getContent().equals(tgtData2)));
//            tx.commit();
//
//            cleanupTestDb(manager);
//        }
//        catch (Exception e)
//        {
//            tx.rollback();
//            throw e;
//        }
//    }

    // This used to crash because we didn't check for null when adding the
    // second identical target, so tried to save a null tuv
//    public void testOverwriteWithIdenticalTargets(TM3Tm<TestData> tm,
//            GlobalSightLocale srcLocale, final GlobalSightLocale tgtLocale)
//            throws Exception
//    {
//        Session session = HibernateUtil.getSession();
//        Transaction tx = null;
//        try
//        {
//            tx = session.beginTransaction();
//            // Now let's create some segments.
//            TestData srcData1 = new TestData("This is source 1");
//            TestData tgtData1 = new TestData("This is target 1");
//            TestData tgtData2 = new TestData("This is target 2");
//            TM3Saver<TestData> saver = tm.createSaver();
//            saver.tu(srcData1, srcLocale, currentTestEvent)
//                    .target(tgtData1, tgtLocale, currentTestEvent)
//                    .save(TM3SaveMode.MERGE);
//            tx.commit();
//
//            tx = session.beginTransaction();
//            saver = tm.createSaver();
//            saver.tu(srcData1, srcLocale, currentTestEvent)
//                    .target(tgtData2, tgtLocale, currentTestEvent)
//                    .target(tgtData2, tgtLocale, currentTestEvent)
//                    .save(TM3SaveMode.OVERWRITE);
//            tx.commit();
//
//            tx = session.beginTransaction();
//            // make sure there is only one target tuv
//            TM3LeverageResults<TestData> results = tm.findMatches(srcData1,
//                    srcLocale, null, TM3Attributes.NONE, TM3MatchType.EXACT,
//                    false);
//            expectResults(results, expected(srcData1, true));
//            TM3Tu<TestData> tu = results.getMatches().first().getTu();
//            List<TM3Tuv<TestData>> targetTuvs = tu.getLocaleTuvs(tgtLocale);
//            assertEquals(1, targetTuvs.size());
//            assertTrue(targetTuvs.get(0).getContent().equals(tgtData2));
//            tx.commit();
//
//            cleanupTestDb(manager);
//        }
//        catch (Exception e)
//        {
//            tx.rollback();
//            throw e;
//        }
//    }

    // In GlobalSight, it's possible to have logically identical TUVs with
    // content
    // that is not string-identical (because of optional inline XML attributes).
    // We simulate with a test here.
//    public void testFixedFingerprintTuvs(GlobalSightLocale srcLocale,
//            final GlobalSightLocale tgtLocale) throws Exception
//    {
//        FixedValueTestDataFactory factory = new FixedValueTestDataFactory(2L);
//        TM3Tm<TestData> tm = manager.getTm(factory, currentTestId);
//        Session session = HibernateUtil.getSession();
//        Transaction tx = null;
//        try
//        {
//            tx = session.beginTransaction();
//            // Now let's create some segments.
//            TestData srcData1 = new TestData("This is source 1");
//            // These two targets are "different", but they are logically the
//            // same
//            // in the opinion of the TM3Data implementation
//            TestData tgtData1 = new FixedValueTestData("This is target 1", 2L);
//            TestData tgtData2 = new FixedValueTestData("This is target 2", 2L);
//            TM3Saver<TestData> saver = tm.createSaver();
//            saver.tu(srcData1, srcLocale, currentTestEvent)
//                    .target(tgtData1, tgtLocale, currentTestEvent)
//                    .save(TM3SaveMode.MERGE);
//            tx.commit();
//
//            tx = session.beginTransaction();
//            saver = tm.createSaver();
//            saver.tu(srcData1, srcLocale, currentTestEvent)
//                    .target(tgtData2, tgtLocale, currentTestEvent)
//                    .save(TM3SaveMode.MERGE);
//            tx.commit();
//
//            tx = session.beginTransaction();
//            // make sure there is only one target tuv
//            TM3LeverageResults<TestData> results = tm.findMatches(srcData1,
//                    srcLocale, null, TM3Attributes.NONE, TM3MatchType.EXACT,
//                    false);
//            expectResults(results, expected(srcData1, true));
//            TM3Tu<TestData> tu = results.getMatches().first().getTu();
//            List<TM3Tuv<TestData>> targetTuvs = tu.getLocaleTuvs(tgtLocale);
//            assertEquals(1, targetTuvs.size());
//            assertTrue(targetTuvs.get(0).getContent().equals(tgtData1));
//            tx.commit();
//
//            cleanupTestDb(manager);
//        }
//        catch (Exception e)
//        {
//            tx.rollback();
//            throw e;
//        }
//    }

//    /**
//     * Make sure that we don't skip TUVs or return them in the wrong order when
//     * using getDataByLocale(). (GBS-2328).
//     */
//    public void testDataByLocaleOrdering(TM3Tm<TestData> tm,
//            GlobalSightLocale srcLocale, GlobalSightLocale tgtLocale1,
//            GlobalSightLocale tgtLocale2) throws Exception
//    {
//
//        Session session = HibernateUtil.getSession();
//        Transaction tx = null;
//        try
//        {
//            tx = session.beginTransaction();
//            // Create three segments in such a way that the TUVs for
//            // French are out of order relative to the TUs.
//            TestData srcData1 = new TestData("This is source 1");
//            TestData srcData2 = new TestData("This is source 2");
//            TestData srcData3 = new TestData("This is source 3");
//            TestData tgtData1 = new TestData("This is target 1");
//            TestData tgtData2 = new TestData("This is target 2");
//            TestData tgtData3 = new TestData("This is target 3");
//
//            TM3Saver<TestData> saver = tm.createSaver();
//            // TU 1, en -> fr tuv
//            TM3Tu<TestData> tu1 = saver
//                    .tu(srcData1, srcLocale, currentTestEvent)
//                    .target(tgtData1, tgtLocale1, currentTestEvent)
//                    .save(TM3SaveMode.MERGE).get(0);
//            session.flush();
//            // TU 2, en -> de tuv
//            saver = tm.createSaver();
//            TM3Tu<TestData> tu2 = saver
//                    .tu(srcData2, srcLocale, currentTestEvent)
//                    .target(tgtData2, tgtLocale2, currentTestEvent)
//                    .save(TM3SaveMode.MERGE).get(0);
//            session.flush();
//            // TU 3, en -> fr tuv
//            saver = tm.createSaver();
//            TM3Tu<TestData> tu3 = saver
//                    .tu(srcData3, srcLocale, currentTestEvent)
//                    .target(tgtData3, tgtLocale1, currentTestEvent)
//                    .save(TM3SaveMode.MERGE).get(0);
//            session.flush();
//            // now go back to tu 2 and add an en -> fr tuv
//            saver.tu(srcData2, srcLocale, currentTestEvent)
//                    .target(tgtData2, tgtLocale1, currentTestEvent)
//                    .save(TM3SaveMode.MERGE);
//
//            session.flush();
//            tx.commit();
//            tx = session.beginTransaction();
//
//            // now when we ask for all fr data, we should get tuvs from
//            // tus 1, 2, and 3 in that order.
//            TM3Handle<TestData> handle = tm.getDataByLocale(tgtLocale1, null,
//                    null);
//            assertEquals("unexpected tu count", 3, handle.getCount());
//            ((LocaleDataHandle) handle).setIncrement(1);
//            Iterator<TM3Tu<TestData>> it = handle.iterator();
//            TM3Tu<TestData> tu = it.next();
//            assertNotNull(tu);
//            assertEquals(tu1.getId(), tu.getId());
//            assertEquals(srcData1, tu.getSourceTuv().getContent());
//            assertTrue(it.hasNext());
//            tu = it.next();
//            assertNotNull(tu);
//            assertEquals(tu2.getId(), tu.getId());
//            assertEquals(srcData2, tu.getSourceTuv().getContent());
//            assertTrue(it.hasNext());
//            tu = it.next();
//            assertNotNull(tu);
//            assertEquals(tu3.getId(), tu.getId());
//            assertEquals(srcData3, tu.getSourceTuv().getContent());
//            assertFalse("Too many TU returned", it.hasNext());
//            tx.commit();
//            cleanupTestDb(manager);
//        }
//        catch (Exception e)
//        {
//            tx.rollback();
//            throw e;
//        }
//    }

    // when it's safe to assume at most one tuv per locale
    private static <T extends TM3Data> TM3Tuv<T> getLocaleTuv(TM3Tu<T> tu,
            TM3Locale locale)
    {
        List<TM3Tuv<T>> tuvs = tu.getLocaleTuvs(locale);
        if (tuvs.size() == 0)
        {
            return null;
        }
        else if (tuvs.size() == 1)
        {
            return tuvs.get(0);
        }
        else
        {
            throw new RuntimeException("unexpected multiple target tuvs");
        }
    }

}
