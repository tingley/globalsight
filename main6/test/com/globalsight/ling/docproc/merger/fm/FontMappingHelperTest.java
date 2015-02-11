package com.globalsight.ling.docproc.merger.fm;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.globalsight.everest.unittest.util.FileUtil;

public class FontMappingHelperTest
{
    private String inddXml = null;
    
    @Before
    public void init() throws Exception
    {
        String xmlFile = FileUtil.getResourcePath(FontMappingHelperTest.class, "files/indd_markup.xml");
        inddXml = com.globalsight.util.FileUtil.readFile(new File(xmlFile), "UTF-8");
        
        List<FontMapping> fms = new ArrayList<FontMapping>();
        FontMapping fm1 = new FontMapping();
        fm1.setTargetFont("MingLiU");
        fm1.setDefault(true);
        fm1.setTargetLocale("zh_CN");
        FontMapping fm2 = new FontMapping();
        fm2.setSourceFont("Arial");
        fm2.setTargetFont("MS Mincho");
        fm2.setTargetLocale("zh_CN");
        fms.add(fm1);
        fms.add(fm2);
        FontMappingHelper.initForDebug(fms);
    }
    
    @Test
    public void testCheckInddXml()
    {
        Assert.assertTrue(FontMappingHelper.isInddXml("xml", inddXml));
    }
    
    @Test
    public void testProcessInddXml()
    {       
        String processed = FontMappingHelper.processInddXml("zh_CN", inddXml);
        Assert.assertTrue(processed.contains("InddFontFamily=\"MingLiU\""));
        Assert.assertTrue(processed.contains("InddFontFamily=\"MS Mincho\""));
    }

    /**
     * @param args
     */
    public static void main(String[] args)
    {
        org.junit.runner.JUnitCore.runClasses(FontMappingHelperTest.class);
    }

}
