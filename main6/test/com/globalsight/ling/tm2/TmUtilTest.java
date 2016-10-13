package com.globalsight.ling.tm2;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.globalsight.everest.tuv.TuImpl;
import com.globalsight.everest.tuv.TuvImpl;
import com.globalsight.util.GlobalSightLocale;

public class TmUtilTest
{
    @Test
    public void testCreateTmSegment1()
    {
        String text = "We have over 400 staff members<bpt erasable=\"yes\" i=\"4\" type=\"bold\" x=\"4\">&lt;b&gt;</bpt>USA<ept i=\"4\">&lt;/b&gt;</ept>, <bpt erasable=\"yes\" i=\"5\" type=\"bold\" x=\"5\">&lt;b&gt;</bpt>Ireland<ept i=\"5\">&lt;/b&gt;</ept>, <bpt erasable=\"yes\" i=\"6\" type=\"bold\" x=\"6\">&lt;b&gt;</bpt>Germany<ept i=\"6\">&lt;/b&gt;</ept>, <bpt erasable=\"yes\" i=\"7\" type=\"bold\" x=\"7\">&lt;b&gt;</bpt>The Netherlands<ept i=\"7\">&lt;/b&gt;</ept>, <bpt erasable=\"yes\" i=\"8\" type=\"bold\" x=\"8\">&lt;b&gt;</bpt>China<ept i=\"8\">&lt;/b&gt;</ept> and <bpt erasable=\"yes\" i=\"9\" type=\"bold\" x=\"9\">&lt;b&gt;</bpt>Japan<ept i=\"9\">&lt;/b&gt;</ept>.";
        long tuId = 1000;
        GlobalSightLocale locale = new GlobalSightLocale("en", "US", true);
        String type = "text";
        BaseTmTuv tuv = TmUtil.createTmSegment(text, tuId, locale, type, true);
        String expectedSeg = "<segment>We have over 400 staff members<bpt erasable=\"yes\" i=\"4\" type=\"bold\" x=\"4\"/>USA<ept i=\"4\"/>, <bpt erasable=\"yes\" i=\"5\" type=\"bold\" x=\"5\"/>Ireland<ept i=\"5\"/>, <bpt erasable=\"yes\" i=\"6\" type=\"bold\" x=\"6\"/>Germany<ept i=\"6\"/>, <bpt erasable=\"yes\" i=\"7\" type=\"bold\" x=\"7\"/>The Netherlands<ept i=\"7\"/>, <bpt erasable=\"yes\" i=\"8\" type=\"bold\" x=\"8\"/>China<ept i=\"8\"/> and <bpt erasable=\"yes\" i=\"9\" type=\"bold\" x=\"9\"/>Japan<ept i=\"9\"/>.</segment>";
        assertEquals("Segment is different!", expectedSeg, tuv.getSegment());
        assertTrue(tuv.getTu().isTranslatable());

        tuv = TmUtil.createTmSegment(text, tuId, locale, type, false);
        expectedSeg = "<localizable>We have over 400 staff members<bpt erasable=\"yes\" i=\"4\" type=\"bold\" x=\"4\"/>USA<ept i=\"4\"/>, <bpt erasable=\"yes\" i=\"5\" type=\"bold\" x=\"5\"/>Ireland<ept i=\"5\"/>, <bpt erasable=\"yes\" i=\"6\" type=\"bold\" x=\"6\"/>Germany<ept i=\"6\"/>, <bpt erasable=\"yes\" i=\"7\" type=\"bold\" x=\"7\"/>The Netherlands<ept i=\"7\"/>, <bpt erasable=\"yes\" i=\"8\" type=\"bold\" x=\"8\"/>China<ept i=\"8\"/> and <bpt erasable=\"yes\" i=\"9\" type=\"bold\" x=\"9\"/>Japan<ept i=\"9\"/>.</localizable>";
        assertEquals("Segment is different!", expectedSeg, tuv.getSegment());
        assertTrue(!tuv.getTu().isTranslatable());
    }

    @Test
    public void testCreateTmSegment2()
    {
        TuImpl tu = new TuImpl();
        tu.setTmId(-1);
        tu.setDataType("html");
        tu.setTuType("text");
        tu.setLocalizableType('T');
        tu.setPid(1);

        TuvImpl tuv = new TuvImpl();
        tuv.setWordCount(17);
        GlobalSightLocale locale = new GlobalSightLocale("en", "US", true);
        tuv.setGlobalSightLocale(locale);
        // tuv.setSourcePage(null);
        tuv.setTu(tu);
        String gxml = "<segment segmentId=\"2\" wordcount=\"20\">We have over 400 staff members in 12 offices located in the <bpt erasable=\"yes\" i=\"4\" type=\"bold\" x=\"4\">&lt;b&gt;</bpt>USA<ept i=\"4\">&lt;/b&gt;</ept>, <bpt erasable=\"yes\" i=\"5\" type=\"bold\" x=\"5\">&lt;b&gt;</bpt>Ireland<ept i=\"5\">&lt;/b&gt;</ept>, <bpt erasable=\"yes\" i=\"6\" type=\"bold\" x=\"6\">&lt;b&gt;</bpt>Germany<ept i=\"6\">&lt;/b&gt;</ept>, <bpt erasable=\"yes\" i=\"7\" type=\"bold\" x=\"7\">&lt;b&gt;</bpt>The Netherlands<ept i=\"7\">&lt;/b&gt;</ept>, <bpt erasable=\"yes\" i=\"8\" type=\"bold\" x=\"8\">&lt;b&gt;</bpt>China<ept i=\"8\">&lt;/b&gt;</ept> and <bpt erasable=\"yes\" i=\"9\" type=\"bold\" x=\"9\">&lt;b&gt;</bpt>Japan<ept i=\"9\">&lt;/b&gt;</ept>. </segment>";
        tuv.setGxml(gxml);
        tuv.setSid("sid");

        tu.addTuv(tuv);

        SegmentTmTuv segmentTmTuv = (SegmentTmTuv) TmUtil.createTmSegment(tuv,
                "0", 1000);
        assertTrue("Word count is wrong", segmentTmTuv.getWordCount() == 20);
        assertEquals("SID does not equals.", "sid", segmentTmTuv.getSid());

        SegmentTmTuv segmentTmTuv2 = (SegmentTmTuv) TmUtil.createTmSegment(tuv,
                "1", 1000);
        assertNull(segmentTmTuv2);
    }

}
