package com.globalsight.everest.webapp.pagehandler.tm.corpus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.globalsight.everest.unittest.util.ServerUtil;
import com.globalsight.ling.tm2.SegmentTmTu;
import com.globalsight.ling.tm2.SegmentTmTuv;
import com.globalsight.util.ClassUtil;
import com.globalsight.util.GlobalSightLocale;

public class TMSearchBroswerHandlerHelperTest
{
    private TMSearchBroswerHandlerHelper helper = new TMSearchBroswerHandlerHelper();

    @BeforeClass
    public static void setUp() throws Exception
    {
        ServerUtil
                .initServerInstance(com.globalsight.everest.localemgr.LocaleManagerWLRMIImpl.class);
    }

    @Test
    public void testGetLeverageLocales()
    {
        Locale uiLocale = Locale.ENGLISH;
        String localeId = "57";
        List<String> list = (List<String>) ClassUtil.testMethod(helper,
                "getLeverageLocales", uiLocale, localeId);
        Assert.assertFalse(list.contains(""));
        Assert.assertFalse(list.contains(""));
        Assert.assertFalse(list.contains(""));
    }

    @Test
    public void testValidLeverageLocale()
    {
        GlobalSightLocale zhcn = new GlobalSightLocale("zh", "CN", false);
        GlobalSightLocale zhtw = new GlobalSightLocale("zh", "TW", false);
        boolean result1 = (Boolean) ClassUtil.testMethod(helper,
                "validLeverageLocale", zhcn, zhtw);
        boolean result2 = (Boolean) ClassUtil.testMethod(helper,
                "validLeverageLocale", zhtw, zhcn);
        Assert.assertFalse(result1);
        Assert.assertFalse(result2);
    }

    @Test
    public void testGetDisplayResult()
    {
        List<Map<String, Object>> searchResult = new ArrayList<Map<String, Object>>();
        Map<String, Object> map1 = new HashMap<String, Object>();
        map1.put("name", "map1");
        Map<String, Object> map2 = new HashMap<String, Object>();
        map2.put("name", "map2");
        Map<String, Object> map3 = new HashMap<String, Object>();
        map3.put("name", "map3");
        Map<String, Object> map4 = new HashMap<String, Object>();
        map4.put("name", "map4");
        Map<String, Object> map5 = new HashMap<String, Object>();
        map5.put("name", "map5");
        Map<String, Object> map6 = new HashMap<String, Object>();
        map6.put("name", "map6");
        Map<String, Object> map7 = new HashMap<String, Object>();
        map7.put("name", "map7");
        Map<String, Object> map8 = new HashMap<String, Object>();
        map8.put("name", "map8");
        Map<String, Object> map9 = new HashMap<String, Object>();
        map9.put("name", "map9");
        Map<String, Object> map10 = new HashMap<String, Object>();
        map10.put("name", "map10");

        searchResult.add(map1);
        searchResult.add(map2);
        searchResult.add(map3);
        searchResult.add(map4);
        searchResult.add(map5);
        searchResult.add(map6);
        searchResult.add(map7);
        searchResult.add(map8);
        searchResult.add(map9);
        searchResult.add(map10);

        // The 2end page, 2 entries one page
        int page = 2;
        String maxEntriesPerPageStr = "2";
        Map<String, Object> result = (Map<String, Object>) ClassUtil
                .testMethod(helper, "getDisplayResult", searchResult, page,
                        maxEntriesPerPageStr);
        List<Object> list = (List<Object>) result.get("result");
        Map<String, Object> map11 = (Map<String, Object>) list.get(0);
        String name11 = (String) map11.get("name");
        Map<String, Object> map12 = (Map<String, Object>) list.get(1);
        String name12 = (String) map12.get("name");
        Assert.assertTrue(("map3").equals(name11));
        Assert.assertTrue(("map4").equals(name12));

        // The 1st page, 2 entries one page
        page = 1;
        maxEntriesPerPageStr = "2";
        result = (Map<String, Object>) ClassUtil.testMethod(helper,
                "getDisplayResult", searchResult, page, maxEntriesPerPageStr);
        list = (List<Object>) result.get("result");
        map11 = (Map<String, Object>) list.get(0);
        name11 = (String) map11.get("name");
        map12 = (Map<String, Object>) list.get(1);
        name12 = (String) map12.get("name");
        Assert.assertTrue(("map1").equals(name11));
        Assert.assertTrue(("map2").equals(name12));
    }

    @Test
    public void testGetFormattedSegment()
    {
        String findText = "leon";
        String replaceText = "new Text";
        GlobalSightLocale sourceLocale = new GlobalSightLocale("en", "US", false);
        SegmentTmTu tu = new SegmentTmTu(1, 1, "html", "en_US", true,
                sourceLocale);
        tu.setSourceTmName("TM01");
        SegmentTmTuv tuv = new SegmentTmTuv();
        tuv.setId(1);
        tuv.setTu(tu);
        tuv.setSegment("<segment>Leon is a good boy</segment>");
        tuv.setExactMatchKey(1);
        tuv.setLocale(sourceLocale);
        tuv.setSid("s");
        tu.addTuv(tuv);
        Map<String, String> result = (Map<String, String>) ClassUtil
                .testMethod(helper, "getFormattedSegment", findText,
                        replaceText, tuv);
        String expect = "<span style=\"color:blue;background-color:#C0C0C0;\"><b>Leon</b></span><span style=\"color:blue;background-color:#C2F70E;\">new Text</span> is a good boy";
        String actual = result.get("content");
        Assert.assertTrue(expect.equals(actual));
    }
}
