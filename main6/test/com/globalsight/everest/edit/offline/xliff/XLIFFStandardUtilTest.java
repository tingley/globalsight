package com.globalsight.everest.edit.offline.xliff;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.globalsight.everest.edit.offline.page.OfflineSegmentData;

public class XLIFFStandardUtilTest
{

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception
    {
    }

    @Before
    public void setUp() throws Exception
    {
    }

    @After
    public void tearDown() throws Exception
    {
    }

    /**
     * For GBS-4425: Removed extra whitespaces between XLF attributes.
     */
    @Test
    public final void testConvertToStandard()
    {
        String segment = "18-29 July<ph i=\"1\" type=\"tag\" x=\"1\">&lt;wr&gt;&lt;w:r&gt;&lt;w:rPr&gt;&lt;w:rFonts w:ascii=\"Gill Sans MT\" w:cs=\"Gill Sans Light\" w:hAnsi=\"Gill Sans MT\"&gt;&lt;/w:rFonts&gt;&lt;w:color w:themeColor=\"text2\" w:themeShade=\"80\" w:val=\"0F243E\"&gt;&lt;/w:color&gt;&lt;w:sz w:val=\"26\"&gt;&lt;/w:sz&gt;&lt;w:szCs w:val=\"26\"&gt;&lt;/w:szCs&gt;&lt;/w:rPr&gt;&lt;w:tab&gt;&lt;/w:tab&gt;&lt;/w:r&gt;&lt;/wr&gt;</ph>";
        String expected = "18-29 July<ph id=\"119462_GS_1\" i=\"1\" type=\"tag\" x=\"1\">&lt;wr&gt;&lt;w:r&gt;&lt;w:rPr&gt;&lt;w:rFonts w:ascii=\"Gill Sans MT\" w:cs=\"Gill Sans Light\" w:hAnsi=\"Gill Sans MT\"&gt;&lt;/w:rFonts&gt;&lt;w:color w:themeColor=\"text2\" w:themeShade=\"80\" w:val=\"0F243E\"&gt;&lt;/w:color&gt;&lt;w:sz w:val=\"26\"&gt;&lt;/w:sz&gt;&lt;w:szCs w:val=\"26\"&gt;&lt;/w:szCs&gt;&lt;/w:rPr&gt;&lt;w:tab&gt;&lt;/w:tab&gt;&lt;/w:r&gt;&lt;/wr&gt;</ph>";
        segment = XLIFFStandardUtil.addAtts(segment, XLIFFStandardUtil.ph_start,
                XLIFFStandardUtil.ph_end, XLIFFStandardUtil.att_x, XLIFFStandardUtil.att_id,
                "119462");
        Assert.assertEquals(expected, segment);
    }
    
    /**
     * For GBS-4432 xliff ctype not compliant
     */
    @Test
    public final void testConvertToStandard2()
    {
        String segment = "18-29 July<ph i=\"1\" type=\"tag\" x=\"1\">&lt;wr&gt;&lt;w:r&gt;&lt;w:rPr&gt;&lt;w:rFonts w:ascii=\"Gill Sans MT\" w:cs=\"Gill Sans Light\" w:hAnsi=\"Gill Sans MT\"&gt;&lt;/w:rFonts&gt;&lt;w:color w:themeColor=\"text2\" w:themeShade=\"80\" w:val=\"0F243E\"&gt;&lt;/w:color&gt;&lt;w:sz w:val=\"26\"&gt;&lt;/w:sz&gt;&lt;w:szCs w:val=\"26\"&gt;&lt;/w:szCs&gt;&lt;/w:rPr&gt;&lt;w:tab&gt;&lt;/w:tab&gt;&lt;/w:r&gt;&lt;/wr&gt;</ph>";
        String expected = "18-29 July<ph id=\"-1_GS_1\" i=\"1\" ctype=\"x-tag\" xid=\"1\">&lt;wr&gt;&lt;w:r&gt;&lt;w:rPr&gt;&lt;w:rFonts w:ascii=\"Gill Sans MT\" w:cs=\"Gill Sans Light\" w:hAnsi=\"Gill Sans MT\"&gt;&lt;/w:rFonts&gt;&lt;w:color w:themeColor=\"text2\" w:themeShade=\"80\" w:val=\"0F243E\"&gt;&lt;/w:color&gt;&lt;w:sz w:val=\"26\"&gt;&lt;/w:sz&gt;&lt;w:szCs w:val=\"26\"&gt;&lt;/w:szCs&gt;&lt;/w:rPr&gt;&lt;w:tab&gt;&lt;/w:tab&gt;&lt;/w:r&gt;&lt;/wr&gt;</ph>";
        OfflineSegmentData osd = new OfflineSegmentData("-1");
        segment = XLIFFStandardUtil.convertToStandard(osd, segment);
       
        Assert.assertEquals(expected, segment);
    }
    
    /**
     * For GBS-4432 xliff ctype not compliant
     */
    @Test
    public final void testConvertToTmx()
    {
        String segment = "18-29 July<ph i=\"1\" type=\"tag\" x=\"1\">&lt;wr&gt;&lt;w:r&gt;&lt;w:rPr&gt;&lt;w:rFonts w:ascii=\"Gill Sans MT\" w:cs=\"Gill Sans Light\" w:hAnsi=\"Gill Sans MT\"&gt;&lt;/w:rFonts&gt;&lt;w:color w:themeColor=\"text2\" w:themeShade=\"80\" w:val=\"0F243E\"&gt;&lt;/w:color&gt;&lt;w:sz w:val=\"26\"&gt;&lt;/w:sz&gt;&lt;w:szCs w:val=\"26\"&gt;&lt;/w:szCs&gt;&lt;/w:rPr&gt;&lt;w:tab&gt;&lt;/w:tab&gt;&lt;/w:r&gt;&lt;/wr&gt;</ph>";
        String expected = "18-29 July<ph i=\"1\" type=\"tag\" x=\"1\">&lt;wr&gt;&lt;w:r&gt;&lt;w:rPr&gt;&lt;w:rFonts w:ascii=\"Gill Sans MT\" w:cs=\"Gill Sans Light\" w:hAnsi=\"Gill Sans MT\"&gt;&lt;/w:rFonts&gt;&lt;w:color w:themeColor=\"text2\" w:themeShade=\"80\" w:val=\"0F243E\"&gt;&lt;/w:color&gt;&lt;w:sz w:val=\"26\"&gt;&lt;/w:sz&gt;&lt;w:szCs w:val=\"26\"&gt;&lt;/w:szCs&gt;&lt;/w:rPr&gt;&lt;w:tab&gt;&lt;/w:tab&gt;&lt;/w:r&gt;&lt;/wr&gt;</ph>";
        segment = XLIFFStandardUtil.convertToTmx(segment);
        Assert.assertEquals(expected, segment);
    }
    
}
