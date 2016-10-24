package com.globalsight.cxe.entity.filterconfiguration;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class InternalTextHelperTest
{
    @Test
    public void testHandleOne()
    {
        InternalText it1 = new InternalText("internal", false);
        List<InternalText> its = new ArrayList<InternalText>();
        its.add(it1);
        String ori = "this is a internal text test for internal text filter.";
        String expected = "this is a <bpt internal=\"yes\" i=\"1\"></bpt>internal<ept i=\"1\"></ept> text test for <bpt internal=\"yes\" i=\"2\"></bpt>internal<ept i=\"2\"></ept> text filter.";

        String result = InternalTextHelper.handleString(ori, its, null, true);
        assertTrue(result.length() > 0);
        assertEquals(expected, result);
    }

    @Test
    public void testHandleOneRE()
    {
        InternalText it1 = new InternalText("\\{[^{]+\\}", true);
        List<InternalText> its = new ArrayList<InternalText>();
        its.add(it1);
        String ori = "this is a {internal} text test for {internal} text filter.";
        String expected = "this is a <bpt internal=\"yes\" i=\"1\"></bpt>{internal}<ept i=\"1\"></ept> text test for <bpt internal=\"yes\" i=\"2\"></bpt>{internal}<ept i=\"2\"></ept> text filter.";

        String result = InternalTextHelper.handleString(ori, its, null, true);
        assertTrue(result.length() > 0);
        assertEquals(expected, result);
    }

    @Test
    public void testHandleTwo()
    {
        InternalText it1 = new InternalText("internal", false);
        InternalText it2 = new InternalText("text", false);
        List<InternalText> its = new ArrayList<InternalText>();
        its.add(it1);
        its.add(it2);
        String ori = "this is a internal text test for internal text filter.";
        String expected = "this is a <bpt internal=\"yes\" i=\"1\"></bpt>internal<ept i=\"1\"></ept> <bpt internal=\"yes\" i=\"2\"></bpt>text<ept i=\"2\"></ept> test for <bpt internal=\"yes\" i=\"3\"></bpt>internal<ept i=\"3\"></ept> <bpt internal=\"yes\" i=\"4\"></bpt>text<ept i=\"4\"></ept> filter.";

        String result = InternalTextHelper.handleString(ori, its, null, true);
        assertTrue(result.length() > 0);
        assertEquals(expected, result);
    }

    @Test
    public void testHandleTwoRE()
    {
        InternalText it1 = new InternalText("\\{[^{]+\\}", true);
        InternalText it2 = new InternalText("#[^#]+#", true);
        List<InternalText> its = new ArrayList<InternalText>();
        its.add(it1);
        its.add(it2);
        String ori = "this is a {internal} text #test# for {internal} text #filter#.";
        String expected = "this is a <bpt internal=\"yes\" i=\"1\"></bpt>{internal}<ept i=\"1\"></ept> text <bpt internal=\"yes\" i=\"2\"></bpt>#test#<ept i=\"2\"></ept> for <bpt internal=\"yes\" i=\"3\"></bpt>{internal}<ept i=\"3\"></ept> text <bpt internal=\"yes\" i=\"4\"></bpt>#filter#<ept i=\"4\"></ept>.";

        String result = InternalTextHelper.handleString(ori, its, null, true);
        assertTrue(result.length() > 0);
        assertEquals(expected, result);
    }

    @Test
    public void testHandleTwoMixed()
    {
        InternalText it1 = new InternalText("internal", false);
        InternalText it2 = new InternalText("#[^#]+#", true);
        List<InternalText> its = new ArrayList<InternalText>();
        its.add(it1);
        its.add(it2);
        String ori = "this is a internal text #test# for internal text #filter#.";
        String expected = "this is a <bpt internal=\"yes\" i=\"1\"></bpt>internal<ept i=\"1\"></ept> text <bpt internal=\"yes\" i=\"2\"></bpt>#test#<ept i=\"2\"></ept> for <bpt internal=\"yes\" i=\"3\"></bpt>internal<ept i=\"3\"></ept> text <bpt internal=\"yes\" i=\"4\"></bpt>#filter#<ept i=\"4\"></ept>.";

        String result = InternalTextHelper.handleString(ori, its, null, true);
        assertTrue(result.length() > 0);
        assertEquals(expected, result);
    }
    
    @Test
    public void testHandleTwoMixedWithUnicde()
    {
        InternalText it1 = new InternalText("内部字符", false);
        InternalText it2 = new InternalText("#[^#]+#", true);
        List<InternalText> its = new ArrayList<InternalText>();
        its.add(it1);
        its.add(it2);
        String ori = "这是内部字符的#测试# 为了内部字符的#filter所写#。";
        String expected = "这是<bpt internal=\"yes\" i=\"1\"></bpt>内部字符<ept i=\"1\"></ept>的<bpt internal=\"yes\" i=\"2\"></bpt>#测试#<ept i=\"2\"></ept> 为了<bpt internal=\"yes\" i=\"3\"></bpt>内部字符<ept i=\"3\"></ept>的<bpt internal=\"yes\" i=\"4\"></bpt>#filter所写#<ept i=\"4\"></ept>。";

        String result = InternalTextHelper.handleString(ori, its, null, true);
        assertTrue(result.length() > 0);
        assertEquals(expected, result);
    }
    
    @Test
    public void testIsSegmentAllInternalTag()
    {
        String seg1 = "<segment segmentId=\"1\"><bpt i=\"1\" internal=\"yes\"/>Title for stats section for Posts &amp; Pages<ept i=\"1\"/></segment>";
        assertTrue(InternalTextHelper.isSegmentAllInternalTag(seg1));
        
        String seg2 = "<segment segmentId=\"1\"><bpt i=\"1\" internal=\"yes\"/>Title for stats section for Posts &amp; Pages<ept i=\"2\"/></segment>";
        assertFalse(InternalTextHelper.isSegmentAllInternalTag(seg2));
        
        String seg3 = "<segment segmentId=\"1\"><bpt i=\"1\" internal=\"yes\"/>Title for stats section for Posts &amp; Pages<ept i=\"1\"/>3</segment>";
        assertFalse(InternalTextHelper.isSegmentAllInternalTag(seg3));
        
        String seg4 = "<segment segmentId=\"1\">1<bpt i=\"1\" internal=\"yes\"/>Title for stats section for Posts &amp; Pages<ept i=\"1\"/></segment>";
        assertFalse(InternalTextHelper.isSegmentAllInternalTag(seg4));
    }
}
