package com.globalsight.util;

import static com.globalsight.util.StringUtil.join;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class StringUtilTest
{
    @BeforeClass
    public static void init()
    {
        System.out.println("BeforeClass :: init()");
    }

    @Before
    public void init2()
    {
        System.out.println("Before :: init2()");
    }

    @After
    public void clear()
    {
        System.out.println("After :: clear()");
    }
    
    @AfterClass
    public static void clear2()
    {
        System.out.println("AfterClass :: clear2()");
    }
    
    @Test
    public void testFormatPCT()
    {
        String result = StringUtil.formatPCT(new Float(0.0));
        assertEquals("0%", result);

        result = StringUtil.formatPCT(new Float(100));
        assertEquals("100%", result);
        
        result = StringUtil.formatPCT(new Float(100.00));
        assertEquals("100%", result);
        
        result = StringUtil.formatPCT(new Float(-1));
        assertEquals("-1%", result);
        
        result = StringUtil.formatPCT(new Float(123.005f));
        assertEquals("123%", result);

        result = StringUtil.formatPCT(new Float(123.015f));
        assertEquals("123%", result);
        
        result = StringUtil.formatPCT(new Float(85.28f));
        assertEquals("85%", result);
        
        result = StringUtil.formatPCT(new Float(85.20f));
        assertEquals("85%", result);
    }

    @Test
    public void testFormatPercentFloat()
    {
        String result = StringUtil.formatPercent(0.0f);
        assertEquals("0.00", result);
        
        result = StringUtil.formatPercent(0f);
        assertEquals("0.00", result);
        
        result = StringUtil.formatPercent(100f);
        assertEquals("100.00", result);
        
        result = StringUtil.formatPercent(-1);
        assertEquals("-1.00", result);
        
        result = StringUtil.formatPercent(100.00f);
        assertEquals("100.00", result);
        
        result = StringUtil.formatPercent(123.005f);
        assertEquals("123.00", result);
        
        result = StringUtil.formatPercent(123.015f);
        assertEquals("123.01", result);
        
        result = StringUtil.formatPercent(85.2f);
        assertEquals("85.20", result);
    }

    @Test
    public void testFormatPercentFloatInt()
    {
        String result = StringUtil.formatPercent(0.0f, 2);
        assertEquals("0.00", result);
        
        result = StringUtil.formatPercent(0f, 2);
        assertEquals("0.00", result);
        
        result = StringUtil.formatPercent(100f, 2);
        assertEquals("100.00", result);
        
        result = StringUtil.formatPercent(-1, 2);
        assertEquals("-1.00", result);
        
        result = StringUtil.formatPercent(100.00f, 2);
        assertEquals("100.00", result);
        
        result = StringUtil.formatPercent(123.005f, 2);
        assertEquals("123.00", result);
        
        result = StringUtil.formatPercent(123.015f, 2);
        assertEquals("123.01", result);
        
        result = StringUtil.formatPercent(85.2f, 2);
        assertEquals("85.20", result);
    }
    
    @Test
    public void testDelSuffix()
    {
        String specialStr = "_1000";
        String input = "translation_1000";
        assertEquals("translation", StringUtil.delSuffix(input, specialStr));
        
        input = "review_dtp1_1000";
        assertEquals("review_dtp1", StringUtil.delSuffix(input, specialStr));
        
        input = "translation";
        assertEquals("translation", StringUtil.delSuffix(input, specialStr));
        
        input = "translation_1000_1000";
        assertEquals("translation_1000", StringUtil.delSuffix(input, specialStr));
    }

    @Test
    public void testJoin()
    {
        assertEquals("a b c", join(" ", "a", "b", "c"));
        assertEquals("a", join(" ", "a"));
        assertEquals("", join(" "));
    }
    
    @Test
    public void testIsIncludedInArray()
    {
        String[] array = null;
        String value = null;
        
        assertEquals(false, StringUtil.isIncludedInArray(array, value));
        
        array = new String[]{"111", "222", "333"};
        value = "444";
        assertEquals(false, StringUtil.isIncludedInArray(array, value));
        
        array = new String[]{"111", "222", "3333"};
        value = "22";
        assertEquals(false, StringUtil.isIncludedInArray(array, value));
        
        array = new String[]{"111", "aAa", "3333"};
        value = "aaa";
        assertEquals(false, StringUtil.isIncludedInArray(array, value));
        
        array = new String[]{"111", "aAa", "333"};
        value = "aAa";
        assertEquals(true, StringUtil.isIncludedInArray(array, value));
    }
    
    @Test
    public void testSplit()
    {
        String msg = "a,b,a,d,e";
        Set result = StringUtil.split(msg, ",");
        assertTrue("Size Error", result.size() == 4);
    }

    @Test
    public void testGet()
    {
        assertEquals("", StringUtil.get(null));
        assertEquals("", StringUtil.get(" "));
        assertEquals("", StringUtil.get(""));

        assertEquals("test", StringUtil.get(null, "test"));
        assertEquals("test", StringUtil.get(" ", "test"));
        assertEquals("test", StringUtil.get("", "test"));
    }

    @Test
    public void testGetInt()
    {
        assertEquals(-1, StringUtil.getInt(null));
        assertEquals(-1, StringUtil.getInt(" "));
        assertEquals(-1, StringUtil.getInt("test"));

        assertEquals(0, StringUtil.getInt(null, 0));
        assertEquals(0, StringUtil.getInt(" ", 0));
        assertEquals(0, StringUtil.getInt("test", 0));
        assertEquals(1, StringUtil.getInt("1", 0));
    }

    @Test
    public void testGetLong()
    {
        assertEquals(-1l, StringUtil.getLong(null));
        assertEquals(-1l, StringUtil.getLong(" "));
        assertEquals(-1l, StringUtil.getLong("test"));

        assertEquals(0l, StringUtil.getLong(null, 0l));
        assertEquals(0l, StringUtil.getLong(" ", 0l));
        assertEquals(0l, StringUtil.getLong("test", 0l));
        assertEquals(1l, StringUtil.getLong("1", 0l));
    }

    @Test
    public void testGetBoolean()
    {
        assertEquals(false, StringUtil.getBoolean(null));
        assertEquals(false, StringUtil.getBoolean(" "));
        assertEquals(false, StringUtil.getBoolean("false"));
        assertEquals(false, StringUtil.getBoolean("no"));
        assertEquals(false, StringUtil.getBoolean("test"));

        assertTrue(StringUtil.getBoolean("true"));
        assertTrue(StringUtil.getBoolean("True "));
        assertTrue(StringUtil.getBoolean("yes"));
        assertTrue(StringUtil.getBoolean("yEs"));
    }
}
