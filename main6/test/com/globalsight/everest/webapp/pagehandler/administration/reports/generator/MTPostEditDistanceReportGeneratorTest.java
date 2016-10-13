package com.globalsight.everest.webapp.pagehandler.administration.reports.generator;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import org.junit.Assert;
import org.junit.Test;

import com.globalsight.util.ClassUtil;

public class MTPostEditDistanceReportGeneratorTest
{
    @Test
    public void testCalculateTER1()
    {
        LinkedHashMap<String, ArrayList<String>> hypsegs = new LinkedHashMap<String, ArrayList<String>>();
        LinkedHashMap<String, ArrayList<String>> refsegs = new LinkedHashMap<String, ArrayList<String>>();
        ArrayList<String> hyps = new ArrayList<String>();
        ArrayList<String> refs = new ArrayList<String>();
        MTPostEditDistanceReportGenerator generator = new MTPostEditDistanceReportGenerator();

        hyps.add("示例文档");
        hypsegs.put("400828", hyps);
        refs.add("示例文档1222");
        refsegs.put("400828", refs);
        Double actual = (Double) ClassUtil.testMethod(generator, "calculateTER", hypsegs, refsegs);
        Assert.assertEquals(100d, actual.doubleValue(), 0.01);
    }

    @Test
    public void testCalculateTER2()
    {
        LinkedHashMap<String, ArrayList<String>> hypsegs = new LinkedHashMap<String, ArrayList<String>>();
        LinkedHashMap<String, ArrayList<String>> refsegs = new LinkedHashMap<String, ArrayList<String>>();
        ArrayList<String> hyps = new ArrayList<String>();
        ArrayList<String> refs = new ArrayList<String>();
        MTPostEditDistanceReportGenerator generator = new MTPostEditDistanceReportGenerator();

        hyps.add("Welocalize公司 - 关于我们");
        hypsegs.put("400828", hyps);
        refs.add("Welocalize公司 - 关于我们1222");
        refsegs.put("400828", refs);
        Double actual = (Double) ClassUtil.testMethod(generator, "calculateTER", hypsegs, refsegs);
        Assert.assertEquals(33.33d, actual.doubleValue(), 0.01);
    }

    @Test
    public void testCalculateTER3()
    {
        LinkedHashMap<String, ArrayList<String>> hypsegs = new LinkedHashMap<String, ArrayList<String>>();
        LinkedHashMap<String, ArrayList<String>> refsegs = new LinkedHashMap<String, ArrayList<String>>();
        ArrayList<String> hyps = new ArrayList<String>();
        ArrayList<String> refs = new ArrayList<String>();
        MTPostEditDistanceReportGenerator generator = new MTPostEditDistanceReportGenerator();

        hyps.add("Welocalize公司始建于1997年，是一家私人控股，创业支持的公司。");
        hypsegs.put("400828", hyps);
        refs.add("111222 Welocalize公司始建于1997年，是一家私人控股，创业支持的公司。 ");
        refsegs.put("400828", refs);
        Double actual = (Double) ClassUtil.testMethod(generator, "calculateTER", hypsegs, refsegs);
        Assert.assertEquals(50d, actual.doubleValue(), 0.01);
    }
}
