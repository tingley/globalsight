package com.globalsight.ling.docproc.merger.fm;

import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class FontMappingTest
{
    @Test
    public void testFontMapping()
    {
        FontMapping fm = new FontMapping();
        fm.setDefault(true);
        fm.setSourceLocale("en_US");
        fm.setSourceFont("Arial");
        fm.setTargetLocale("zh_CN");
        fm.setTargetFont("MingLiU");
        
        Assert.assertTrue(!fm.accept(null));
        Assert.assertTrue(!fm.accept("zh_tw"));
        Assert.assertTrue(fm.accept("zh_cn"));
        
        Assert.assertTrue(!fm.accept(null, null));
        Assert.assertTrue(!fm.accept(null, "zh_cn"));
        Assert.assertTrue(!fm.accept("Arial", null));
        Assert.assertTrue(!fm.accept("Arial", "zh_tw"));
        Assert.assertTrue(fm.accept("Arial", "zh_cn"));        
    }

    /**
     * @param args
     */
    public static void main(String[] args)
    {
        org.junit.runner.JUnitCore.runClasses(FontMappingTest.class);
    }

}
