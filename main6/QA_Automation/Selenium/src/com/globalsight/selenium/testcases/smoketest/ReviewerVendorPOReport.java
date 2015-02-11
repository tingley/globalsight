package com.globalsight.selenium.testcases.smoketest;

import java.io.File;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.globalsight.selenium.functions.CommonFuncs;
import com.globalsight.selenium.functions.DownloadFileRead.FileRead;
import com.globalsight.selenium.pages.MainFrame;
import com.globalsight.selenium.pages.ReviewerVendorPOReportWebForm;
import com.globalsight.selenium.testcases.ConfigUtil;
import com.thoughtworks.selenium.Selenium;

public class ReviewerVendorPOReport
{
    private Selenium selenium;

    @BeforeClass
    public void beforeClass()
    {
        selenium = CommonFuncs.getSelenium();
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
        selenium.click(MainFrame.REPORTS_MENU);
        selenium.click(MainFrame.REPORTS_MAIN_SUBMENU);
        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
        selenium.click(ReviewerVendorPOReportWebForm.REPORT_LINK);

        selenium.waitForPopUp(ReviewerVendorPOReportWebForm.POPUP_WINDOW_NAME,
                CommonFuncs.SHORT_WAIT);
        selenium.selectWindow("name="
                + ReviewerVendorPOReportWebForm.POPUP_WINDOW_NAME);

        initOptions();

        selenium.click(ReviewerVendorPOReportWebForm.SUBMIT_BUTTON);

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
        File file = fileRead.getFile(ReviewerVendorPOReportWebForm.REPORT_FILE_NAME);
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
        String[] activityname = ConfigUtil.getDataInCase(className,
                "activityname").split(",");
        String startTime = ConfigUtil.getDataInCase(className, "startTime");
        String startTimeUnits = ConfigUtil.getDataInCase(className,
                "startTimeUnits");
        String endsTime = ConfigUtil.getDataInCase(className, "endsTime");
        String endsTimeUnits = ConfigUtil.getDataInCase(className,
                "endsTimeUnits");
        String currency = ConfigUtil.getDataInCase(className,
                "currency");
        String recal = ConfigUtil.getDataInCase(className,
                "recal");

        selenium.removeSelection(ReviewerVendorPOReportWebForm.PROJECTS_SELECTOR,
                "label=<ALL>");
        selenium.removeSelection(ReviewerVendorPOReportWebForm.JOBSTATUS_SELECTOR,
                "label=<ALL>");
        selenium.removeSelection(ReviewerVendorPOReportWebForm.TARGETLOCALE_SELECTOR,
                "label=<ALL>");
        selenium.removeSelection(ReviewerVendorPOReportWebForm.ACTIVITY_SELECTOR, 
                "label=<ALL>");

        // Projects
        for (int i = 0; i < projects.length; i++)
        {
            selenium.addSelection(ReviewerVendorPOReportWebForm.PROJECTS_SELECTOR,
                    "label=" + projects[i]);
        }
        // Job Status
        for (int i = 0; i < jobStatus.length; i++)
        {
            selenium.addSelection(ReviewerVendorPOReportWebForm.JOBSTATUS_SELECTOR,
                    "label=" + jobStatus[i]);
        }
        // Target Locales
        for (int i = 0; i < targetLocales.length; i++)
        {
            selenium.addSelection(ReviewerVendorPOReportWebForm.TARGETLOCALE_SELECTOR,
                    "label=" + targetLocales[i]);
        }
        // Activity Name
        for (int i = 0; i< activityname.length; i++)
        {
            selenium.addSelection(ReviewerVendorPOReportWebForm.ACTIVITY_SELECTOR, 
                    "label=" + activityname[i]);
        }
        // Date Range
        selenium.type(ReviewerVendorPOReportWebForm.STARTSTIME, startTime);
        selenium.select(ReviewerVendorPOReportWebForm.STARTSTIMEUNITS, "label="
                + startTimeUnits);
        selenium.type(ReviewerVendorPOReportWebForm.ENDSTIME, endsTime);
        selenium.select(ReviewerVendorPOReportWebForm.ENDSTIMEUNITS, "label="
                + endsTimeUnits);
        // Currency
        selenium.select(ReviewerVendorPOReportWebForm.CURRENCY, "label=" + currency);
        // Re-caculate
        selenium.select(ReviewerVendorPOReportWebForm.Re_CALCULATE, "label=" + recal);
    }
}
