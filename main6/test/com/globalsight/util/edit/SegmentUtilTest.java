package com.globalsight.util.edit;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class SegmentUtilTest
{
    private String oriString = null;
    private String resultString = null;
    
    @Before
    public void init()
    {
        oriString = "Hi there is &#x100088;";
        resultString = "Hi there is <gs_xml_placeholder type=\"InvalidUnicodeChar\" value=\"100088\"/>";
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
    
    public static void main(String[] args)
    {
        org.junit.runner.JUnitCore.runClasses(SegmentUtilTest.class);
    }
}
