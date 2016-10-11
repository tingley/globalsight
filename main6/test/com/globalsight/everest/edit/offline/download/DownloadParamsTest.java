package com.globalsight.everest.edit.offline.download;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import com.globalsight.everest.edit.offline.AmbassadorDwUpConstants;

public class DownloadParamsTest
{
    private ArrayList<Long> m_PTF_Ids = new ArrayList<Long>();
    private ArrayList<String> m_PTF_Names = new ArrayList<String>();

    @Test
    public void testGenerateUniqueFileName01()
    {
        // No repeat files

        m_PTF_Ids.clear();
        m_PTF_Names.clear();

        m_PTF_Ids.add((long) 1);
        m_PTF_Ids.add((long) 2);
        m_PTF_Ids.add((long) 3);
        m_PTF_Ids.add((long) 4);

        m_PTF_Names
                .add("en_US\\webservice\\jobname_027754009\\Welocalize.html");
        m_PTF_Names
                .add("(sheet002) en_US\\webservice\\jobname_027754009\\Welocalize.xlsx");
        m_PTF_Names
                .add("(sheet001) en_US\\webservice\\jobname_027754009\\Welocalize.xlsx");
        m_PTF_Names
                .add("(tabstrip) en_US\\webservice\\jobname_027754009\\Welocalize.xlsx");

        // Expected result
        Map<String, String> expectedResult = new HashMap<String, String>();

        expectedResult.put("1", "Welocalize.html");
        expectedResult.put("2", "Welocalize_sheet002.xlsx");
        expectedResult.put("3", "Welocalize_sheet001.xlsx");
        expectedResult.put("4", "Welocalize_tabstrip.xlsx");

        DownloadParams params = new DownloadParams();
        params.setPageIDList(m_PTF_Ids);
        params.setPageNameList(m_PTF_Names);

        params.generateUniqueFileName();
        Map<String, String> uniqueNames = params.getUniqueFileNames();
        Assert.assertTrue(uniqueNames.equals(expectedResult));
    }

    @Test
    public void testGenerateUniqueFileName02()
    {
        // repeat files

        m_PTF_Ids.clear();
        m_PTF_Names.clear();

        m_PTF_Ids.add((long) 1);
        m_PTF_Ids.add((long) 2);
        m_PTF_Ids.add((long) 3);
        m_PTF_Ids.add((long) 4);
        m_PTF_Ids.add((long) 5);
        m_PTF_Ids.add((long) 6);
        m_PTF_Ids.add((long) 7);
        m_PTF_Ids.add((long) 8);

        m_PTF_Names
                .add("en_US\\webservice\\jobname_027754009\\Welocalize.html");
        m_PTF_Names
                .add("en_US\\webservice\\jobname_027754009\\leon\\Welocalize.html");
        m_PTF_Names
                .add("(sheet002) en_US\\webservice\\jobname_027754009\\Welocalize.xlsx");
        m_PTF_Names
                .add("(sheet001) en_US\\webservice\\jobname_027754009\\Welocalize.xlsx");
        m_PTF_Names
                .add("(tabstrip) en_US\\webservice\\jobname_027754009\\Welocalize.xlsx");
        m_PTF_Names
                .add("(sheet002) en_US\\webservice\\jobname_027754009\\leon\\Welocalize.xlsx");
        m_PTF_Names
                .add("(sheet001) en_US\\webservice\\jobname_027754009\\leon\\Welocalize.xlsx");
        m_PTF_Names
                .add("(tabstrip) en_US\\webservice\\jobname_027754009\\leon\\Welocalize.xlsx");

        // Expected result
        Map<String, String> expectedResult = new HashMap<String, String>();

        expectedResult.put("1", "Welocalize_1.html");
        expectedResult.put("2", "Welocalize_2.html");
        expectedResult.put("3", "Welocalize_1_sheet002.xlsx");
        expectedResult.put("4", "Welocalize_1_sheet001.xlsx");
        expectedResult.put("5", "Welocalize_1_tabstrip.xlsx");
        expectedResult.put("6", "Welocalize_2_sheet002.xlsx");
        expectedResult.put("7", "Welocalize_2_sheet001.xlsx");
        expectedResult.put("8", "Welocalize_2_tabstrip.xlsx");

        DownloadParams params = new DownloadParams();
        params.setPageIDList(m_PTF_Ids);
        params.setPageNameList(m_PTF_Names);

        params.generateUniqueFileName();
        Map<String, String> uniqueNames = params.getUniqueFileNames();
        Assert.assertTrue(uniqueNames.equals(expectedResult));
    }

    @Test
    public void testGenerateUniqueFileName03()
    {
        DownloadParams params = new DownloadParams();

        params.generateUniqueFileName();
        Map<String, String> uniqueNames = params.getUniqueFileNames();
        Assert.assertTrue(uniqueNames.size() == 0);
    }

    @Test
    public void testSetNeedCombined()
    {
        DownloadParams params = new DownloadParams();
        params.setFileFormatId(AmbassadorDwUpConstants.DOWNLOAD_FILE_FORMAT_TRADOSRTF);
        params.setNeedCombined(true);
        boolean result = params.isNeedCombined();
        Assert.assertTrue(result);
    }
}
