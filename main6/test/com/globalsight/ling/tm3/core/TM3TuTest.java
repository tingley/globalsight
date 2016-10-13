package com.globalsight.ling.tm3.core;

import java.util.Date;

import org.junit.Assert;
import org.junit.Test;

import com.globalsight.ling.tm2.SegmentTmTuv;
import com.globalsight.ling.tm3.integration.GSTuvData;
import com.globalsight.util.GlobalSightLocale;

public class TM3TuTest<T extends TM3Data>
{
    /**
     * Have same previous and next hash values.
     */
    @SuppressWarnings(
    { "unchecked", "rawtypes" })
    @Test
    public void testIsIdenticalTuv1()
    {
        TM3Tu tu = new TM3Tu();

        TM3Locale locale1 = new GlobalSightLocale("de", "DE", false);
        SegmentTmTuv tuv1 = new SegmentTmTuv();
        tuv1.setSegment("Herunterladen");
        tuv1.setLocale((GlobalSightLocale) locale1);
        GSTuvData data = new GSTuvData(tuv1);
        TM3Tuv<T> trgTuv = new TM3Tuv<T>(locale1, (T) data, "creationUser", new Date(),
                "modifyUser", new Date(), new Date(), 1234, "jobName", 4014367614787579270L,
                5308380583665731573L, null);

        TM3Locale locale2 = new GlobalSightLocale("de", "DE", false);
        SegmentTmTuv tuv2 = new SegmentTmTuv();
        tuv2.setSegment("Herunterladen");
        tuv2.setLocale((GlobalSightLocale) locale2);
        boolean actual = tu.isIdenticalTuv(trgTuv, locale2, new GSTuvData(tuv2),
                4014367614787579270L, 5308380583665731573L);
        Assert.assertTrue(actual);
    }

    /**
     * Both previous and next hash values are "-1".
     */
    @SuppressWarnings(
    { "unchecked", "rawtypes" })
    @Test
    public void testIsIdenticalTuv3()
    {
        TM3Tu tu = new TM3Tu();

        TM3Locale locale1 = new GlobalSightLocale("de", "DE", false);
        SegmentTmTuv tuv1 = new SegmentTmTuv();
        tuv1.setSegment("Herunterladen");
        tuv1.setLocale((GlobalSightLocale) locale1);
        GSTuvData data = new GSTuvData(tuv1);
        TM3Tuv<T> trgTuv = new TM3Tuv<T>(locale1, (T) data, "creationUser", new Date(),
                "modifyUser", new Date(), new Date(), 1234, "jobName", -1, -1, null);

        TM3Locale locale2 = new GlobalSightLocale("de", "DE", false);
        SegmentTmTuv tuv2 = new SegmentTmTuv();
        tuv2.setSegment("Herunterladen");
        tuv2.setLocale((GlobalSightLocale) locale2);
        boolean actual = tu.isIdenticalTuv(trgTuv, locale2, new GSTuvData(tuv2), -1, -1);
        Assert.assertTrue(actual);
    }

    /**
     * Have different previous and next hash values.
     */
    @SuppressWarnings(
    { "unchecked", "rawtypes" })
    @Test
    public void testIsIdenticalTuv2()
    {
        TM3Tu tu = new TM3Tu();

        TM3Locale locale1 = new GlobalSightLocale("de", "DE", false);
        SegmentTmTuv tuv1 = new SegmentTmTuv();
        tuv1.setSegment("Herunterladen");
        tuv1.setLocale((GlobalSightLocale) locale1);
        GSTuvData data = new GSTuvData(tuv1);
        TM3Tuv<T> trgTuv = new TM3Tuv<T>(locale1, (T) data, "creationUser", new Date(),
                "modifyUser", new Date(), new Date(), 1234, "jobName", 4014367614787579270L,
                5308380583665731573L, null);

        TM3Locale locale2 = new GlobalSightLocale("de", "DE", false);
        SegmentTmTuv tuv2 = new SegmentTmTuv();
        tuv2.setSegment("Herunterladen");
        tuv2.setLocale((GlobalSightLocale) locale2);
        boolean actual = tu.isIdenticalTuv(trgTuv, locale2, new GSTuvData(tuv2), -1, -1);
        Assert.assertFalse(actual);
    }
}
