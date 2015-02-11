package com.globalsight.selenium.testcases.smoketest;

import java.io.File;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.globalsight.selenium.functions.CommonFuncs;
import com.globalsight.selenium.functions.DownloadFileRead.FileRead;
import com.globalsight.selenium.pages.ActivityDurationReportWebForm;
import com.globalsight.selenium.pages.MainFrame;
import com.globalsight.selenium.properties.ConfigUtil;
import com.thoughtworks.selenium.Selenium;

public class ActivityDurationReport
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
        selenium.click(ActivityDurationReportWebForm.REPORT_LINK);

        selenium.waitForPopUp(ActivityDurationReportWebForm.POPUP_WINDOW_NAME,
                CommonFuncs.SHORT_WAIT);
        selenium.selectWindow("name="
                + ActivityDurationReportWebForm.POPUP_WINDOW_NAME);

        initOptions();

        selenium.click(ActivityDurationReportWebForm.SUBMIT_BUTTON);
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
        File file = fileRead.getFile(ActivityDurationReportWebForm.REPORT_FILE_NAME);
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

        selenium.removeSelection(ActivityDurationReportWebForm.PROJECTS_SELECTOR,
                "label=<ALL>");
        selenium.removeSelection(ActivityDurationReportWebForm.JOBSTATUS_SELECTOR,
                "label=<ALL>");
        selenium.removeSelection(ActivityDurationReportWebForm.TARGETLOCALE_SELECTOR,
                "label=<ALL>");
     // Projects
        for (int i = 0; i < projects.length; i++)
        {
            selenium.addSelection(ActivityDurationReportWebForm.PROJECTS_SELECTOR,
                    "label=" + projects[i]);
        }
        // Job Status
        for (int i = 0; i < jobStatus.length; i++)
        {
            selenium.addSelection(ActivityDurationReportWebForm.JOBSTATUS_SELECTOR,
                    "label=" + jobStatus[i]);
        }
        // Target Locales
        for (int i = 0; i < targetLocales.length; i++)
        {
            selenium.addSelection(ActivityDurationReportWebForm.TARGETLOCALE_SELECTOR,
                    "label=" + targetLocales[i]);
        }
        // Date Range
        selenium.type(ActivityDurationReportWebForm.STARTSTIME, startTime);
        selenium.select(ActivityDurationReportWebForm.STARTSTIMEUNITS, "label="
                + startTimeUnits);
        selenium.type(ActivityDurationReportWebForm.ENDSTIME, endsTime);
        selenium.select(ActivityDurationReportWebForm.ENDSTIMEUNITS, "label="
                + endsTimeUnits);
        // Time Format
        selenium.select(ActivityDurationReportWebForm.DATEFORMAT, "label="
                + displayFormat);
    }

}

