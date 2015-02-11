package com.globalsight.ling.docproc.extractor.fm;


import java.lang.reflect.Method;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class PrivateMethodTest
{
    private Extractor extractor = null;
    private static String aString = "ab\\x11 on page\\x15 cd\\x11 ef\\x12 \\t";
    private static String resultsString = "ab<ph type=\"text\" id=\"1\" x=\"1\">\\x11 " +
    		"</ph>on page<ph type=\"text\" id=\"2\" x=\"2\">\\x15 </ph>cd" +
    		"<ph type=\"text\" id=\"1\" x=\"1\">\\x11 </ph>ef<ph type=\"text\" " +
    		"id=\"3\" x=\"3\">\\x12 </ph><ph type=\"text\" id=\"4\" x=\"4\">\\t</ph>";
    
    @Before
    public void setUp() throws Exception
    {
        extractor = new Extractor();
    }
    
    @Test
    public void testReplaceSpecialCharactor() throws Exception
    {
        Method method = extractor.getClass().getDeclaredMethod(
                "replaceSpecialCharactor", String.class);
        method.setAccessible(true);
        Object resultObject = method.invoke(extractor, aString);
        System.out.println(resultObject);
        Assert.assertEquals(resultObject, resultsString);
    }
    
}
