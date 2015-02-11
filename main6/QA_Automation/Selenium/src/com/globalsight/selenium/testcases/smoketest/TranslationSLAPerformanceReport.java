package com.globalsight.selenium.testcases.smoketest;

import java.io.File;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.globalsight.selenium.functions.CommonFuncs;
import com.globalsight.selenium.functions.DownloadFileRead.FileRead;
import com.globalsight.selenium.pages.MainFrame;
import com.globalsight.selenium.pages.TranslationSLAPerformanceReportWebForm;
import com.globalsight.selenium.properties.ConfigUtil;
import com.thoughtworks.selenium.Selenium;

/**
 * Translation SLA Performance Report
 * 
 * @author leon
 * 
 */
public class TranslationSLAPerformanceReport
{
    private Selenium selenium;

    @BeforeClass
    public void beforeClass()
    {
        selenium = CommonFuncs.initSelenium();
        CommonFuncs.loginSystemWithAdmin(selenium);
    }

    @AfterClass
    public void afterClass()
    {
        selenium.stop();
    }

    @Test
    public void generateReport()
    {
        selenium.click(MainFrame.Reports_MENU);
        selenium.click(MainFrame.MainReportsPage_SUBMENU);
        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
        selenium.click(TranslationSLAPerformanceReportWebForm.REPORT_LINK);

        selenium.waitForPopUp(
                TranslationSLAPerformanceReportWebForm.POPUP_WINDOW_NAME,
                CommonFuncs.SHORT_WAIT);
        selenium.selectWindow("name="
                + TranslationSLAPerformanceReportWebForm.POPUP_WINDOW_NAME);

        initOptions();

        selenium.click(TranslationSLAPerformanceReportWebForm.Submit_BUTTON);

        // Wait for the download progress finish.
        try
        {
            Thread.sleep((long) 10000);
        }
        catch (InterruptedException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // Verify the file exists or not
        FileRead fileRead = new FileRead();
        File file = fileRead
                .getFile(TranslationSLAPerformanceReportWebForm.REPORT_FILE_NAME);
        Assert.assertTrue(file.exists());
        // Moved the file to the sub folder.
        fileRead.moveFile(file);
    }

    /**
     * Init the options of the report
     */
    private void initOptions()
    {
        String className = getClass().getName();

        String[] projects = ConfigUtil.getDataInCase(className, "project")
                .split(",");
        String[] jobStatus = ConfigUtil.getDataInCase(className, "jobStatus")
                .split(",");
        String[] targetLocales = ConfigUtil.getDataInCase(className,
                "targetLocale").split(",");
        String startTime = ConfigUtil.getDataInCase(className, "startTime");
        String startTimeUnits = ConfigUtil.getDataInCase(className,
                "startTimeUnits");
        String endsTime = ConfigUtil.getDataInCase(className, "endsTime");
        String endsTimeUnits = ConfigUtil.getDataInCase(className,
                "endsTimeUnits");
        String displayFormat = ConfigUtil.getDataInCase(className,
                "displayFormat");

        selenium.removeSelection(
                TranslationSLAPerformanceReportWebForm.PROJECT_SELECTOR,
                "label=<ALL>");
        selenium.removeSelection(
                TranslationSLAPerformanceReportWebForm.JOBSTATUS_SELECTOR,
                "label=<ALL>");
        selenium.removeSelection(
                TranslationSLAPerformanceReportWebForm.TARGETLOCALE_SELECTOR,
                "label=<ALL>");

        // Project
        for (int i = 0; i < projects.length; i++)
        {
            selenium.addSelection(
                    TranslationSLAPerformanceReportWebForm.PROJECT_SELECTOR,
                    "label=" + projects[i]);
        }
        // Job Status
        for (int i = 0; i < jobStatus.length; i++)
        {
            selenium.addSelection(
                    TranslationSLAPerformanceReportWebForm.JOBSTATUS_SELECTOR,
                    "label=" + jobStatus[i]);
        }
        // Target Locales
        for (int i = 0; i < targetLocales.length; i++)
        {
            selenium.addSelection(
                    TranslationSLAPerformanceReportWebForm.TARGETLOCALE_SELECTOR,
                    "label=" + targetLocales[i]);
        }
        // Date Range
        selenium.type(TranslationSLAPerformanceReportWebForm.STARTSTIME,
                startTime);
        selenium.select(TranslationSLAPerformanceReportWebForm.STARTSTIMEUNITS,
                "label=" + startTimeUnits);
        selenium.type(TranslationSLAPerformanceReportWebForm.ENDSTIME, endsTime);
        selenium.select(TranslationSLAPerformanceReportWebForm.ENDSTIMEUNITS,
                "label=" + endsTimeUnits);
        // Time Format
        selenium.select(TranslationSLAPerformanceReportWebForm.DATEFORMAT,
                "label=" + displayFormat);
    }
}
