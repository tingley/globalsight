package com.globalsight.ling.docproc.extractor.fm;


import java.lang.reflect.Method;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class PrivateMethodTest
{
    private Extractor extractor = null;
    private static String aString = "ab\\x11 on page\\x15 cd\\x11 ef\\x12 \\t";
    private static String aResultString = "ab<ph type=\"text\" id=\"1\" x=\"1\">\\x11 " +
    		"</ph>on page<ph type=\"text\" id=\"2\" x=\"2\">\\x15 </ph>cd" +
    		"<ph type=\"text\" id=\"1\" x=\"1\">\\x11 </ph>ef<ph type=\"text\" " +
    		"id=\"3\" x=\"3\">\\x12 </ph><ph type=\"text\" id=\"4\" x=\"4\">\\t</ph>";
    private static String bString =       "<ph type=\"text\" id=\"114\" x=\"114\">" +
    		"&lt;Default Font\\&gt;</ph>BAUSCH & LOMB <ph type=\"text\" id=\"115\" x=\"115\">" +
    		"&lt;Default Font\\&gt;</ph>& ZYLINK";
    private static String bResultString = "<ph type=\"text\" id=\"114\" x=\"114\">" +
    		"&lt;Default Font\\&gt;</ph>BAUSCH &amp; LOMB <ph type=\"text\" id=\"115\" x=\"115\">" +
    		"&lt;Default Font\\&gt;</ph>&amp; ZYLINK";
    
    @Before
    public void setUp() throws Exception
    {
        extractor = new Extractor();
    }
    
    @Test
    public void testReplaceSpecialCharactor() throws Exception
    {
        Method method = extractor.getClass().getDeclaredMethod(
                "replaceSpecialCharactor", String.class, boolean.class);
        method.setAccessible(true);
        Object resultObject = method.invoke(extractor, aString, false);
        Assert.assertEquals(resultObject, aResultString);
    }
    
    @Test
    public void testEncodeMeaningful() throws Exception
    {
        Method method = extractor.getClass().getDeclaredMethod(
                "encodeMeaningful", String.class);
        method.setAccessible(true);
        Object resultObject = method.invoke(extractor, bString);
        Assert.assertEquals(resultObject, bResultString);
    }
    
}
