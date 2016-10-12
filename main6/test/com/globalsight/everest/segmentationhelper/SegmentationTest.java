package com.globalsight.everest.segmentationhelper;

import java.util.ArrayList;
import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class SegmentationTest
{
    private static final String THE_SEGMENTS_LENGTH_IS_WRONG = "The segments length is wrong";
    private SegmentationRule segRule = null;
    private SrxHeader defaultHeader = null;
    
    @Before
    public void setUp()
    {
        segRule = new SegmentationRule();
        segRule.setVersion("2.0");
        segRule.setRootName("srx");
        
        ArrayList<LanguageMap> lmaps = new ArrayList<LanguageMap>();
        lmaps.add(new LanguageMap(".*", "default"));
        segRule.setLanguageMap(lmaps);
        
        HashMap<String, ArrayList<Rule>> langRules = new HashMap<String, ArrayList<Rule>>();
        ArrayList<Rule> rules = new ArrayList<Rule>();
        rules.add(new Rule("\\.", "\\s", true));
        langRules.put("default", rules);
        segRule.setRules(langRules);
        
        HashMap<String, String> formathandle = new HashMap<String, String>();
        formathandle.put("start", "no");
        formathandle.put("end", "yes");
        formathandle.put("isolated", "no");
        defaultHeader = new SrxHeader(true, false, formathandle);
        segRule.setHeader(defaultHeader);
    }
    
    @Test
    public void testSegmentationDefault() throws Exception
    {
        String src = "  Part 1.  Part 2. ";
        String[] outputs = SegmentationHelper.segment(segRule, "en_US", src);
        
        assertEquals(THE_SEGMENTS_LENGTH_IS_WRONG, 2, outputs.length);
        assertEquals("  Part 1.  ", outputs[0]);
        assertEquals("Part 2. ", outputs[1]);
    }
    
    @Test
    public void testSegmentationTrimLeading() throws Exception
    {
        defaultHeader.setTrimLeadingWhitespaces(true);
        String src = "  Part 1.  Part 2. ";
        String[] outputs = SegmentationHelper.segment(segRule, "en_US", src);
        
        assertEquals(THE_SEGMENTS_LENGTH_IS_WRONG, 2, outputs.length);
        assertEquals("Part 1.  ", outputs[0]);
        assertEquals("Part 2. ", outputs[1]);
    }
    
    @Test
    public void testSegmentationTrimTrailing() throws Exception
    {
        defaultHeader.setTrimTrailingWhitespaces(true);
        String src = "  Part 1.  Part 2. ";
        String[] outputs = SegmentationHelper.segment(segRule, "en_US", src);
        
        assertEquals(THE_SEGMENTS_LENGTH_IS_WRONG, 2, outputs.length);
        assertEquals("  Part 1.", outputs[0]);
        assertEquals("Part 2.", outputs[1]);
    }
    
    @Test
    public void testSegmentationTrimLeadingAndTrailing() throws Exception
    {
        defaultHeader.setTrimLeadingWhitespaces(true);
        defaultHeader.setTrimTrailingWhitespaces(true);
        String src = "  Part 1.  Part 2. ";
        String[] outputs = SegmentationHelper.segment(segRule, "en_US", src);
        
        assertEquals(THE_SEGMENTS_LENGTH_IS_WRONG, 2, outputs.length);
        assertEquals("Part 1.", outputs[0]);
        assertEquals("Part 2.", outputs[1]);
    }
    
    @Test
    public void testSegmentationTrimLeadingAndTrailing2() throws Exception
    {
        defaultHeader.setTrimLeadingWhitespaces(true);
        defaultHeader.setTrimTrailingWhitespaces(true);
        String src = "  \rPart 1.\n  Part 2. \n \r\n";
        String[] outputs = SegmentationHelper.segment(segRule, "en_US", src);
        
        assertEquals(THE_SEGMENTS_LENGTH_IS_WRONG, 2, outputs.length);
        assertEquals("Part 1.", outputs[0]);
        assertEquals("Part 2.", outputs[1]);
    }
    
    @Test
    public void testSegmentationDefault2() throws Exception
    {
        String src = "  Part 1.  ";
        String[] outputs = SegmentationHelper.segment(segRule, "en_US", src);
        
        assertEquals(THE_SEGMENTS_LENGTH_IS_WRONG, 1, outputs.length);
        assertEquals("  Part 1.  ", outputs[0]);
    }
    
    @Test
    public void testSegmentationOneIncAllFalse() throws Exception
    {
        defaultHeader.setTrimLeadingWhitespaces(true);
        defaultHeader.setTrimTrailingWhitespaces(true);
        String src = "  Part 1.  ";
        String[] outputs = SegmentationHelper.segment(segRule, "en_US", src);
        
        assertEquals(THE_SEGMENTS_LENGTH_IS_WRONG, 1, outputs.length);
        assertEquals("Part 1.", outputs[0]);
    }
    
    @Test
    public void testSegmentationOneIncAllTrue() throws Exception
    {
        defaultHeader.setTrimLeadingWhitespaces(true);
        defaultHeader.setTrimTrailingWhitespaces(true);
        defaultHeader.setOneSegmentIncludesAll(true);
        String src = "  Part 1.  ";
        String[] outputs = SegmentationHelper.segment(segRule, "en_US", src);
        
        assertEquals(THE_SEGMENTS_LENGTH_IS_WRONG, 1, outputs.length);
        assertEquals("  Part 1.  ", outputs[0]);
    }
    
    @Test
    public void testIsWhitespaceString()
    {
        String str = "  \t \n \r \n\r";
        boolean isws = Segmentation.isWhitespaceString(str);
        assertTrue("\"" + str + "\" is not whitespace string", isws);
    }

    /**
     * @param args
     */
    public static void main(String[] args)
    {
        org.junit.runner.JUnitCore.runClasses(SegmentationTest.class);
    }

}
