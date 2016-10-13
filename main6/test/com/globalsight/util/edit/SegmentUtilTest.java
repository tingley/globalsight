package com.globalsight.util.edit;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.globalsight.ling.docproc.SegmentNode;

public class SegmentUtilTest
{
    private String oriString = null;
    private String resultString = null;
    private List<String> oriList = new ArrayList<String>();
    private List<String> expectedList = new ArrayList<String>();
    
    @Before
    public void init()
    {
        oriString = "Hi there is &#x100088;";
        resultString = "Hi there is <gs_xml_placeholder type=\"InvalidUnicodeChar\" value=\"100088\"/>";
        
        // Prepare data for test replaceHtmltagWithPH
        initReplaceHtmltagWithPH();
    }
    
    // For GBS-1789
    @Test(expected= NullPointerException.class)
    public void testProtectInvalidUnicodeChar()
    {
        // case 1, right handle
        String result = SegmentUtil.protectInvalidUnicodeChar(oriString);
        assertEquals(resultString, result);
        
        // case 2, no special char
        String str = "no special char";
        result = SegmentUtil.protectInvalidUnicodeChar(str);
        assertEquals(str, result);
        
        // case 3, null exception
        SegmentUtil.protectInvalidUnicodeChar(null);
    }
    
    // For GBS-1789
    @Test(expected= NullPointerException.class)
    public void testRestoreInvalidUnicodeChar()
    {
        // case 1, right handle
        String ori = SegmentUtil.restoreInvalidUnicodeChar(resultString);
        assertEquals(oriString, ori);
        
        // case 2, no special char
        String str = "no special char";
        String result = SegmentUtil.restoreInvalidUnicodeChar(str);
        assertEquals(str, result);
        
        // case 3, null exception
        SegmentUtil.restoreInvalidUnicodeChar(null);
    }
    
    // Prepare data for replaceHtmltagWithPH
    public void initReplaceHtmltagWithPH()
    {
        oriList.add("a");
        expectedList.add("a");
        
        oriList.add("abc<strong>def<strong>");
        expectedList.add("abc" +
                "<ph type=\"phOfGS\" id=\"1\" x=\"1\">&lt;strong&gt;</ph>" +
                "def" +
                "<ph type=\"phOfGS\" id=\"2\" x=\"2\">&lt;strong&gt;</ph>");
        
        oriList.add("<a class=\"signup\" href=\"/signup\">" +
                "Sign up" +
                "</a>" +
                " and never miss their reviews");
        expectedList.add("<ph type=\"phOfGS\" id=\"1\" x=\"1\">&lt;a class=&quot;signup&quot; href=&quot;/signup&quot;&gt;</ph>" +
                "Sign up" +
                "<ph type=\"phOfGS\" id=\"2\" x=\"2\">&lt;/a&gt;</ph>" +
                " and never miss their reviews");
    }
    
    /**
     * Test SegmentUtil.replaceHtmltagWithPH(String p_str) 
     */
    @Test
    public void testReplaceHtmltagWithPH()
    {
        List<String> actualList = new ArrayList<String>();
        for (String ori : oriList)
        {
            actualList.add(SegmentUtil.replaceHtmltagWithPH(ori));
        }

        assertArrayEquals(expectedList.toArray(), actualList.toArray());
    }
    
    /**
     * Test SegmentUtil.replaceHtmltagWithPH(SegmentNode p_node) 
     */
    @Test
    public void testReplaceHtmltagWithPH2()
    {
        List<SegmentNode> actualNodeList = new ArrayList<SegmentNode>();
        for (String ori : oriList)
        {
            SegmentNode sn = new SegmentNode();
            sn.setSegment(ori);
            SegmentUtil.replaceHtmltagWithPH(sn);
            actualNodeList.add(sn);
        }

        for (int i = 0; i < actualNodeList.size(); i++)
        {
            SegmentNode sn = actualNodeList.get(i);
            assertEquals(expectedList.get(i), sn.getSegment());
        }
    }
    
    public static void main(String[] args)
    {
        org.junit.runner.JUnitCore.runClasses(SegmentUtilTest.class);
    }
}
