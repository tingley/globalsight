package com.globalsight.cxe.adapter.msoffice;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.globalsight.everest.unittest.util.FileUtil;

public class OfficeXmlRepairerTest
{
    private static String simpleDocumentXml = null;
    private static String simpleSlideXml = null;
    private static String simpleStyleXml = null;

    @BeforeClass
    public static void staticInit() throws Exception
    {
        simpleDocumentXml = FileUtil.getResourcePath(OfficeXmlRepairerTest.class, "testfiles/simple.document.xml");
        simpleDocumentXml = FileUtil.readRuleFile(simpleDocumentXml, "utf-8");
        
        simpleSlideXml = FileUtil.getResourcePath(OfficeXmlRepairerTest.class, "testfiles/simple.slide.xml");
        simpleSlideXml = FileUtil.readRuleFile(simpleSlideXml, "utf-8");
        
        simpleStyleXml = FileUtil.getResourcePath(OfficeXmlRepairerTest.class, "testfiles/simple.styles.xml");
        simpleStyleXml = FileUtil.readRuleFile(simpleStyleXml, "utf-8");
    }
    
    /**
     * Test for word
     */
    @Test
    public void testFixRtlLocaleDocx()
    {
        String result = OfficeXmlRepairer.fixRtlLocale(simpleDocumentXml);
        Assert.assertTrue(result.contains("<w:bidi/>"));
    }
    
    /**
     * Test for slide
     */
    @Test
    public void testFixRtlLocaleSlide()
    {
        String result = OfficeXmlRepairer.fixRtlLocale(simpleSlideXml);
        Assert.assertTrue(result.contains("rtl=\"1\""));
    }
    
    /**
     * Test for sheet
     */
    @Test
    public void testFixRtlLocaleSheet()
    {
        String result = OfficeXmlRepairer.fixRtlLocale(simpleStyleXml);
        Assert.assertTrue(result.contains("<alignment readingOrder=\"2\"/></xf>"));
        Assert.assertTrue(result.contains("<alignment readingOrder=\"2\" horizontal=\"left\" indent=\"2\"/></xf>"));
        
    }

    /**
     * @param args
     */
    public static void main(String[] args)
    {
        org.junit.runner.JUnitCore.runClasses(OfficeXmlRepairerTest.class);
    }

}
