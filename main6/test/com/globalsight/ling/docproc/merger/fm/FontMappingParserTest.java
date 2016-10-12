package com.globalsight.ling.docproc.merger.fm;

import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class FontMappingParserTest
{
    @Test
    public void testParse()
    {
        List<FontMapping> fps = FontMappingParser.parse("zh_cn", "Arial|Arial Unicode MS,Times New Roman|Arial Unicode MS");
        String fpsss = fps.toString();
        System.out.println(fpsss);
        Assert.assertTrue(fpsss.contains("zh_cn=Arial|Arial Unicode MS"));
        Assert.assertTrue(fpsss.contains("zh_cn=Times New Roman|Arial Unicode MS"));
    }
    
    @Test
    public void testParseOne()
    {
        FontMapping fm = FontMappingParser.parseOne("zh_cn_default", "MingLiU");
        System.out.println(fm.toString());
        Assert.assertTrue(fm.getTargetLocale().equals("zh_cn"));
        Assert.assertTrue(fm.getTargetFont().equals("MingLiU"));
        Assert.assertTrue(fm.isDefault());
    }

    /**
     * @param args
     */
    public static void main(String[] args)
    {
        org.junit.runner.JUnitCore.runClasses(FontMappingParserTest.class);
    }

}
