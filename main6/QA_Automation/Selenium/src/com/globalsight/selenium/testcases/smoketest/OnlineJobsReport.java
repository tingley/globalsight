package com.globalsight.selenium.testcases.smoketest;

import java.io.File;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.globalsight.selenium.functions.CommonFuncs;
import com.globalsight.selenium.functions.DownloadFileRead.FileRead;
import com.globalsight.selenium.pages.MainFrame;
import com.globalsight.selenium.pages.OnlineJobsReportWebForm;
import com.globalsight.selenium.properties.ConfigUtil;
import com.thoughtworks.selenium.Selenium;

/**
 * Online Jobs Report
 * 
 * @author leon
 */
public class OnlineJobsReport
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
        selenium.click(OnlineJobsReportWebForm.REPORT_LINK);

        selenium.waitForPopUp(OnlineJobsReportWebForm.POPUP_WINDOW_NAME,
                CommonFuncs.SHORT_WAIT);
        selenium.selectWindow("name="
                + OnlineJobsReportWebForm.POPUP_WINDOW_NAME);

        initOptions();

        selenium.click(OnlineJobsReportWebForm.SUBMIT_BUTTON);

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
        File file = fileRead.getFile(OnlineJobsReportWebForm.REPORT_FILE_NAME);
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
        String recalulate = ConfigUtil.getDataInCase(className, "recalulate");
        String displayJobId = ConfigUtil.getDataInCase(className,
                "displayJobId");
        String includeReviewPrices = ConfigUtil.getDataInCase(className,
                "includeReviewPrices");
        String displayFormat = ConfigUtil.getDataInCase(className,
                "displayFormat");
        String currency = ConfigUtil.getDataInCase(className, "currency");
        String matchesTrados = ConfigUtil.getDataInCase(className,
                "matchesTrados");

        String reportForYearOrDate = ConfigUtil.getDataInCase(className,
                "reportForYearOrDate");

        selenium.removeSelection(OnlineJobsReportWebForm.PROJECT_SELECTOR,
                "label=<ALL>");
        selenium.removeSelection(OnlineJobsReportWebForm.JOBSTATUS_SELECTOR,
                "label=<ALL>");
        selenium.removeSelection(OnlineJobsReportWebForm.TARGETLOCALE_SELECTOR,
                "label=<ALL>");

        // Project
        for (int i = 0; i < projects.length; i++)
        {
            selenium.addSelection(OnlineJobsReportWebForm.PROJECT_SELECTOR,
                    "label=" + projects[i]);
        }
        // Job Status
        for (int i = 0; i < jobStatus.length; i++)
        {
            selenium.addSelection(OnlineJobsReportWebForm.JOBSTATUS_SELECTOR,
                    "label=" + jobStatus[i]);
        }
        // Target Locales
        for (int i = 0; i < targetLocales.length; i++)
        {
            selenium.addSelection(
                    OnlineJobsReportWebForm.TARGETLOCALE_SELECTOR, "label="
                            + targetLocales[i]);
        }
        // Re-calculate costs for finished workflows?
        if ("true".equals(recalulate))
        {
            selenium.select(OnlineJobsReportWebForm.RECALC_SELECTOR, recalulate);
        }
        // Display job id?
        selenium.select(OnlineJobsReportWebForm.DISPLAYJOBID_SLECTOR,
                displayJobId);
        // Include External Review Prices
        if ("true".equals(includeReviewPrices))
        {
            selenium.click(OnlineJobsReportWebForm.INCLUDEREVIEW_CHECKBOX);
        }
        //
        if ("year".equals(reportForYearOrDate))
        {
            // Run the report for this year:
            String year = ConfigUtil.getDataInCase(className, "year");
            selenium.select(OnlineJobsReportWebForm.YEAR, year);

        }
        else
        {
            selenium.click(OnlineJobsReportWebForm.DETAILREPORT);
            // Creation Date Range:
            String startTime = ConfigUtil.getDataInCase(className, "startTime");
            String startTimeUnits = ConfigUtil.getDataInCase(className,
                    "startTimeUnits");
            String endsTime = ConfigUtil.getDataInCase(className, "endsTime");
            String endsTimeUnits = ConfigUtil.getDataInCase(className,
                    "endsTimeUnits");
            selenium.type(OnlineJobsReportWebForm.STARTSTIME, startTime);
            selenium.select(OnlineJobsReportWebForm.STARTSTIMEUNITS, "label="
                    + startTimeUnits);
            selenium.type(OnlineJobsReportWebForm.ENDSTIME, endsTime);
            selenium.select(OnlineJobsReportWebForm.ENDSTIMEUNITS, "label="
                    + endsTimeUnits);
        }

        // Time Format
        selenium.select(OnlineJobsReportWebForm.DATEFORMAT, "label="
                + displayFormat);
        // Currency:
        selenium.select(OnlineJobsReportWebForm.CURRENCY, "label=" + currency);
        // Matches
        if ("false".equals(matchesTrados))
        {
            selenium.click(OnlineJobsReportWebForm.MATCHES);
        }

    }
}
