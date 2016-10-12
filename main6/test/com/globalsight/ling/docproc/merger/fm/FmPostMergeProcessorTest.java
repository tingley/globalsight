package com.globalsight.ling.docproc.merger.fm;

import org.junit.Assert;
import org.junit.Test;

public class FmPostMergeProcessorTest
{
    @Test
    public void testProcess()
    {
        // <FPlatformName `W.Times New Roman.R.400'>
        // <FFamily `Times New Roman'>
        String content = "<FPlatformName `W.Arial.R.400'>\n" + "<FFamily `Calibri'>\n"
                + "<FPlatformName `W.Times New Roman.R.500'>";
        String targetLocale = "zh_cn";
        FmPostMergeProcessor p = new FmPostMergeProcessor();
        p.setTargetLocale(targetLocale);

        FontMappingHelper helper = new FontMappingHelper();
        String processed = p.process(content, null);
        System.out.println("FontMappingList : " + helper.getFontMappingList());
        System.out.println(processed);

        if (helper.isLocaleWithFonts(targetLocale))
        {
            Assert.assertEquals("<FPlatformName `W.MingLiU.R.400'>\n" + "<FFamily `MingLiU'>\n"
                    + "<FPlatformName `W.MingLiU.R.500'>\n", processed);
        }
    }

    /**
     * @param args
     */
    public static void main(String[] args)
    {
        org.junit.runner.JUnitCore.runClasses(FmPostMergeProcessorTest.class);
    }

}
