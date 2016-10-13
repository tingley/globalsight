package com.globalsight.everest.webapp.pagehandler.tm.corpus;

import org.junit.Assert;
import org.junit.Test;

public class SegmentMangerTest
{
    private SegmentManager sm = new SegmentManager();
    private String source1 = "<bpt type=\"x-span\" x=\"1\" i=\"1\">&lt;span style=&apos;font-family:&quot;Arial&quot;,&quot;sans-serif&quot;&apos;&gt;</bpt>Sample Document<ept i=\"1\">&lt;/span&gt;</ept>";
    private String source2 = "<ph id=\"0\" xmlns=\"\">&lt;span contenteditable=\"false\"&gt;</ph>Update the following table as necessary when this document is changed:<ph id=\"1\" xmlns=\"\">&lt;/span&gt;</ph>";
    private String source1Tag = "[g1]Sample Document[/g1]";
    private String source2Tag = "[x1]Update the following table as necessary when this document is changed:[x2]";

    @Test
    public void testGetCompact() throws Exception
    {
        sm.setInputSegment(source1, "", "html");
        String result1 = sm.getCompact();
        Assert.assertTrue(source1Tag.equals(result1));

        sm.setInputSegment(source2, "", "html");
        String result2 = sm.getCompact();
        Assert.assertTrue(source2Tag.equals(result2));
    }

    @Test
    public void testErrorCheck() throws Exception
    {
        sm.setInputSegment(source1, "", "html");
        sm.getCompact();
        String result = sm.errorCheck("[g1]Sdsdasdat[/g1]", source1, 0, "UTF8",
                0, "UTF8");
        Assert.assertNull(result);

        result = sm.errorCheck("[g1]Sdsdasdat", source1, 0, "UTF8", 0, "UTF8");
        Assert.assertNotNull(result);
    }

    @Test
    public void testGetPtagString() throws Exception
    {
        sm.setInputSegment(source1, "", "html");
        sm.getCompact();
        String result = sm.getPtagString();
        Assert.assertEquals("[g1],[/g1]", result);
    }

    @Test
    public void testGetTargetDiplomat() throws Exception
    {
        sm.setInputSegment(source1, "", "html");
        sm.getCompact();
        String result = sm.getTargetDiplomat("[g1]Sdsdasdat[/g1]");
        String expect = "<bpt type=\"x-span\" x=\"1\" i=\"1\">&lt;span style=&apos;font-family:&quot;Arial&quot;,&quot;sans-serif&quot;&apos;&gt;</bpt>Sdsdasdat<ept i=\"1\">&lt;/span&gt;</ept>";
        Assert.assertEquals(expect, result);
    }

}
