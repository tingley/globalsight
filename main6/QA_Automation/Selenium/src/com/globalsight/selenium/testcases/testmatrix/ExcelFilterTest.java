package com.globalsight.selenium.testcases.testmatrix;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import junit.framework.Assert;

import org.testng.annotations.Test;

import com.globalsight.selenium.functions.BasicFuncs;
import com.globalsight.selenium.functions.CommonFuncs;
import com.globalsight.selenium.functions.CreateJobsFuncs;
import com.globalsight.selenium.functions.FilterConfigurationFuncs;
import com.globalsight.selenium.pages.FileProfile;
import com.globalsight.selenium.pages.MainFrame;
import com.globalsight.selenium.pages.MyJobs;
import com.globalsight.selenium.testcases.ConfigUtil;
import com.globalsight.selenium.testcases.BaseTestCase;
import com.globalsight.selenium.testcases.util.SeleniumUtils;

public class ExcelFilterTest extends BaseTestCase
{
    private FilterConfigurationFuncs iFilterConfig = new FilterConfigurationFuncs();
    private BasicFuncs basic = new BasicFuncs();
    String testCaseName = ConfigUtil.getDataInCase(getClass().getName(),
            "AddTags");
    String testCaseName1 = ConfigUtil.getDataInCase(getClass().getName(),
            "AddTags1");
    String interName = ConfigUtil.getDataInCase(getClassName(),
            "InternalFilterName");
    String interName1 = ConfigUtil.getDataInCase(getClassName(),
            "InternalFilterName1");
    String iFilterName = ConfigUtil.getDataInCase(getClassName(),
            "HtmlFilterName");
    String cpf = ConfigUtil.getDataInCase(getClassName(),
            "InternalTextPostFilter");
    String str = ConfigUtil.getDataInCase(getClassName(), "AddingTag");
    String excel_filterName = ConfigUtil.getDataInCase(getClassName(),
            "ExcelFilterName");
    String fpname = ConfigUtil.getDataInCase(getClassName(),
            "fileProfileNamesStr");
    String wordCountResultPath_jobDetails = ConfigUtil
            .getConfigData("Base_Path_Result")
            + ConfigUtil.getDataInCase(getClassName(), "WordCountPath");
    String[] fp = fpname.split(",");
    String dir = ConfigUtil.getDataInCase(getClassName(), "SourceFile");;
    String targetLocales = ConfigUtil.getDataInCase(getClassName(),
            "targetLocales");

    public static String getStringToday()
    {
        Date currentTime = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMM-dd-HHmmss");
        String dateString = formatter.format(currentTime);
        return dateString;
    }

    @Test
    public void initfilter() throws Exception
    {
        /*
         * Initiate all filters used in testing xls jobs.
         */
        selenium.click(MainFrame.DATA_SOURCES_MENU);
        selenium.click(MainFrame.FILTER_CONFIGURATION_SUBMENU);
        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
        iFilterConfig.InternalText(selenium, testCaseName, interName);
        iFilterConfig.InternalText(selenium, testCaseName1, interName1);
        iFilterConfig.htmlFilter(selenium, iFilterName, cpf, str);
        iFilterConfig.excelFilter(selenium, excel_filterName, iFilterName,
                interName1);

        // Add filter to the corresponding file profile
        selenium.click(MainFrame.DATA_SOURCES_MENU);
        selenium.click(MainFrame.FILE_PROFILES_SUBMENU);
        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);

        selenium.type(FileProfile.SEARCH_CONTENT_TEXT, fp[0]);
        selenium.keyPressNative(Integer.toString(java.awt.event.KeyEvent.VK_ENTER));
//        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
        SeleniumUtils.selectRadioButtonFromTable(selenium,FileProfile.MAIN_TABLE, fp[0]);
        selenium.click("link=" + fp[0]);
        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
        selenium.select(FileProfile.FILTER_SELECT, "label=" + excel_filterName);
        selenium.click(FileProfile.SAVE_BUTTON);
        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);

        // Read all test cases to "testcases", and create xls job with
        // corresponding filter.
        ArrayList<String[]> testCases = new ArrayList<String[]>();
        String filePath = ConfigUtil.getConfigData("Base_Path")
                + ConfigUtil
                        .getDataInCase(getClassName(), "FilterTestCasePath");
        File file = new File(filePath);
        Assert.assertTrue(file.exists());
        BufferedReader br = new BufferedReader(new FileReader(file));
        String line;
        while ((line = br.readLine()) != null)
        {
            String[] testCase = line.split("\t");
            testCases.add(testCase);
        }
        br.close();

        String contentPostFilter;
        String embeddableTags;
        String internalTags;
        String translatableAttibute;
        String internalTextFilter;
        String internalTextPostFilter;
        String internalTextPostFilterChoose;
        String filterJob = "excelJob" + getStringToday();

        CreateJobsFuncs tmp = new CreateJobsFuncs();
        int ljobNames = testCases.size();
        String[] jobNames = new String[ljobNames];

        SimpleDateFormat format = new SimpleDateFormat();
        String time = format.format(new Date());
        String wordcount = "\r\n" + "\r\n" + time;

        jobNames[0] = "Case title";

        selenium.click(MainFrame.DATA_SOURCES_MENU);
        selenium.click(MainFrame.FILTER_CONFIGURATION_SUBMENU);
        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
        for (int i = 1; i < testCases.size(); i++)
        {
            contentPostFilter = testCases.get(i)[1];
            embeddableTags = testCases.get(i)[2];
            internalTags = testCases.get(i)[3];
            translatableAttibute = testCases.get(i)[4];
            internalTextFilter = testCases.get(i)[5];
            internalTextPostFilter = testCases.get(i)[6];
            internalTextPostFilterChoose = testCases.get(i)[7];

            selenium.click(MainFrame.DATA_SOURCES_MENU);
            selenium.click(MainFrame.FILTER_CONFIGURATION_SUBMENU);
            selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
            iFilterConfig.filterOperation(selenium, interName, interName1,
                    iFilterName, excel_filterName, contentPostFilter,
                    embeddableTags, internalTags, translatableAttibute,
                    internalTextFilter, internalTextPostFilter,
                    internalTextPostFilterChoose, "excel");

            tmp.createJob(filterJob + i, dir, fp[0], targetLocales);
            Thread.sleep(1000);
            jobNames[i] = filterJob + i;

        }

        // for (int i=1; i<29; i++)
        // {
        // jobNames[i]="excelJob201110-18-163855"+i;
        // }
        // for (int i=29; i<testCases.size(); i++)
        // {
        // jobNames[i]="excelJob201110-18-173939"+i;
        // }

        // getWordCount operation
        for (int i = 1; i < testCases.size(); i++)
        {

            selenium.click(MainFrame.MY_JOBS_MENU);
            selenium.click(MainFrame.MY_JOBS_ALL_STATUS_SUBMENU);
            selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);

            selenium.select(MyJobs.JobName_SELECTION,
                    MyJobs.JobName_Slection_Ends_With);
            selenium.type("name=" + MyJobs.SEARCH_JOB_NAME_TEXT, jobNames[i]);
            selenium.click(MyJobs.SEARCH_BUTTON);
            selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);

            if (selenium.isElementPresent("link=" + jobNames[i]))
            {
                String wordCountGot = basic.jobgetWordCount(selenium,
                        MyJobs.MyJobs_AllStatus_TABLE, jobNames[i], 7);

                wordcount = wordcount + " \r\n" + jobNames[i] + ", "
                        + wordCountGot;
            }

        }

        FileWriter writer = new FileWriter(wordCountResultPath_jobDetails, true);
        writer.write(wordcount);
        writer.close();
    }
}
