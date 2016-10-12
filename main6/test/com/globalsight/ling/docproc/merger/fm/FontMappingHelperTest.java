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
    FontMappingHelper helper = new FontMappingHelper();
    private String inddXml = null;

    @Before
    public void init() throws Exception
    {
        String xmlFile = FileUtil.getResourcePath(FontMappingHelperTest.class,
                "files/indd_markup.xml");
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
        helper.initForDebug(fms);
    }

    @Test
    public void testCheckInddXml()
    {
        Assert.assertTrue(FontMappingHelper.isInddXml("xml", inddXml));
    }

    @Test
    public void testProcessInddXml()
    {
        String processed = helper.processInddXml("zh_CN", inddXml);
        Assert.assertTrue(processed.contains("InddFontFamily=\"MingLiU\""));
        Assert.assertTrue(processed.contains("InddFontFamily=\"MS Mincho\""));
        Assert.assertTrue(processed.contains("[Medium-10-MingLiU]"));
        Assert.assertTrue(processed.contains("[Bold-10-MS Mincho]"));
        Assert.assertTrue(processed.contains("[55 Roman-11.3086700439453-MingLiU]"));
        Assert.assertTrue(processed.contains("[/55 Roman-11.3086700439453-MingLiU]"));
        Assert.assertTrue(processed.contains("[65 Bold-10-MingLiU]"));
        Assert.assertTrue(processed.contains("[/65 Bold-10-MingLiU]"));
        Assert.assertTrue(processed.contains("[Book-9.5-MingLiU]"));
        Assert.assertTrue(processed.contains("[/Book-9.5-MingLiU]"));
    }

    /**
     * @param args
     */
    public static void main(String[] args)
    {
        org.junit.runner.JUnitCore.runClasses(FontMappingHelperTest.class);
    }

}
