package com.globalsight.selenium.testcases.smoketest;

import java.io.File;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.globalsight.selenium.functions.CommonFuncs;
import com.globalsight.selenium.functions.DownloadFileRead.FileRead;
import com.globalsight.selenium.pages.JobStatusReportWebForm;
import com.globalsight.selenium.pages.MainFrame;
import com.globalsight.selenium.properties.ConfigUtil;
import com.thoughtworks.selenium.Selenium;

/**
 * Job Status Report
 * 
 * @author leon
 */
public class JobStatusReport
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
        selenium.click(JobStatusReportWebForm.REPORT_LINK);

        selenium.waitForPopUp(JobStatusReportWebForm.POPUP_WINDOW_NAME,
                CommonFuncs.SHORT_WAIT);
        selenium.selectWindow("name="
                + JobStatusReportWebForm.POPUP_WINDOW_NAME);

        initOptions();

        selenium.click(JobStatusReportWebForm.SUBMIT_BUTTON);

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
        File file = fileRead.getFile(JobStatusReportWebForm.REPORT_FILE_NAME);
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

        selenium.removeSelection(JobStatusReportWebForm.PROJECTS_SELECTOR,
                "label=<ALL>");
        selenium.removeSelection(JobStatusReportWebForm.JOBSTATUS_SELECTOR,
                "label=<ALL>");
        selenium.removeSelection(JobStatusReportWebForm.TARGETLOCALE_SELECTOR,
                "label=<ALL>");

        // Projects
        for (int i = 0; i < projects.length; i++)
        {
            selenium.addSelection(JobStatusReportWebForm.PROJECTS_SELECTOR,
                    "label=" + projects[i]);
        }
        // Job Status
        for (int i = 0; i < jobStatus.length; i++)
        {
            selenium.addSelection(JobStatusReportWebForm.JOBSTATUS_SELECTOR,
                    "label=" + jobStatus[i]);
        }
        // Target Locales
        for (int i = 0; i < targetLocales.length; i++)
        {
            selenium.addSelection(JobStatusReportWebForm.TARGETLOCALE_SELECTOR,
                    "label=" + targetLocales[i]);
        }
        // Date Range
        selenium.type(JobStatusReportWebForm.STARTSTIME, startTime);
        selenium.select(JobStatusReportWebForm.STARTSTIMEUNITS, "label="
                + startTimeUnits);
        selenium.type(JobStatusReportWebForm.ENDSTIME, endsTime);
        selenium.select(JobStatusReportWebForm.ENDSTIMEUNITS, "label="
                + endsTimeUnits);
        // Time Format
        selenium.select(JobStatusReportWebForm.DATEFORMAT, "label="
                + displayFormat);
    }
}
