package com.globalsight.ling.docproc;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.internal.runners.statements.Fail;

import com.globalsight.everest.unittest.util.FileUtil;
import com.globalsight.ling.docproc.TranslatableElement;
import com.globalsight.ling.docproc.extractor.xliff.Extractor;
import com.globalsight.util.ClassUtil;

/**
 * Test class for {@link DiplomatWordCounter}
 */
public class DiplomatWordCounterTest
{
    private DiplomatWordCounter obj = null;

    @Before
    public void setUp() throws Exception
    {
        HashMap<String, String> map1 = new HashMap();
        map1.put("wordcounter_count_numerics", "false");
        map1.put("wordcounter_count_placeholders", "false");
        
        HashMap<String, HashMap> map2 = new HashMap();
        map2.put(String.valueOf(1000), map1);
        
        obj = new DiplomatWordCounter(map2);
    }
    
    @Test
    public void testGetWordCountForXLFElement()
    {
        TranslatableElement element = new TranslatableElement();
        int generalValue = 100;
        
        // Not XLF file, return 100
        Integer result = (Integer) ClassUtil.testMethod(obj,
                "getWordCountForXLFElement", element, generalValue);
        assertEquals(new Integer(100), result);
        
        // Common case, return 101
        Map xliffPart = new HashMap();
        xliffPart.put(Extractor.XLIFF_PART, Extractor.XLIFF_PART_SOURCE);
        xliffPart.put(Extractor.IWS_WORDCOUNT, new String("101"));
        element.setXliffPart(xliffPart);
        result = (Integer) ClassUtil.testMethod(obj,
                "getWordCountForXLFElement", element, generalValue);
        assertEquals(new Integer(101), result);
        
        // Is "source" section, but has no "ws_word_count" attribute, return 100
        xliffPart.clear();
        xliffPart.put(Extractor.XLIFF_PART, Extractor.XLIFF_PART_SOURCE);
        element.setXliffPart(xliffPart);
        result = (Integer) ClassUtil.testMethod(obj,
                "getWordCountForXLFElement", element, generalValue);
        assertEquals(new Integer(100), result);
        
        // Not "source" section, return 0
        xliffPart.clear();
        xliffPart.put(Extractor.XLIFF_PART, Extractor.XLIFF_PART_ALT_SOURCE);
        element.setXliffPart(xliffPart);
        result = (Integer) ClassUtil.testMethod(obj,
                "getWordCountForXLFElement", element, generalValue);
        assertEquals(new Integer(0), result);
    }
    
    @Test
    public void testCountInternalTag() throws Exception
    {
        String gxmlFile = FileUtil.getResourcePath(DiplomatWordCounterTest.class, "Welocalize_Company.html.gxml");
        
        if (gxmlFile != null && (new File(gxmlFile)).exists())
        {
            File f = new File(gxmlFile);
            String fileContent = com.globalsight.util.FileUtil.readFile(f, "UTF-8");
            
            Properties p = new Properties();
            p.put("wordcounter_count_numerics", "false");
            p.put("wordcounter_count_dashed_tokens", "false");
            p.put("wordcounter_count_placeholders", "false");
            HashMap hm = new HashMap();
            hm.put(1000, p);
            
            DiplomatWordCounter c = new DiplomatWordCounter(hm);
            c.countDiplomatDocument(fileContent);
            int count = c.getOutput().getWordCount();
            assertEquals("Word count not same.", 4, count);
        }
        else
        {
            String f = gxmlFile == null ? "null" : gxmlFile;
            Assert.fail("gxmlFile does not exists : " +  f);
        }
    }
}
