package com.globalsight.selenium.testcases.smoketest;

import java.io.File;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.globalsight.selenium.functions.CommonFuncs;
import com.globalsight.selenium.functions.DownloadFileRead.FileRead;
import com.globalsight.selenium.pages.CustomerReportsWebForm;
import com.globalsight.selenium.pages.MainFrame;
import com.globalsight.selenium.testcases.ConfigUtil;
import com.thoughtworks.selenium.Selenium;

/**
 * Customize Report
 * 
 * @author leon
 */
public class CustomizeReports
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
        selenium.click(CustomerReportsWebForm.REPORT_LINK);

        selenium.waitForPopUp(CustomerReportsWebForm.POPUP_WINDOW_NAME,
                CommonFuncs.SHORT_WAIT);
        selenium.selectWindow("name="
                + CustomerReportsWebForm.POPUP_WINDOW_NAME);

        initOptions();

        selenium.click(CustomerReportsWebForm.NEXT_BUTTON);
        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
        selenium.click(CustomerReportsWebForm.JOBINFO_CHECKBOX);
        selenium.click(CustomerReportsWebForm.DONE_BUTTON);

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
        File file = fileRead.getFile(CustomerReportsWebForm.REPORT_FILE_NAME);
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

        selenium.removeSelection(CustomerReportsWebForm.PROJECT_SELECTOR,
                "label=<All>");
        selenium.removeSelection(CustomerReportsWebForm.JOBSTATUS_SELECTOR,
                "label=<All>");
        selenium.removeSelection(CustomerReportsWebForm.TARGETLOCALE_SELECTOR,
                "label=<All>");

        // Project
        for (int i = 0; i < projects.length; i++)
        {
            selenium.addSelection(CustomerReportsWebForm.PROJECT_SELECTOR,
                    "label=" + projects[i]);
        }
        // Job Status
        for (int i = 0; i < jobStatus.length; i++)
        {
            selenium.addSelection(CustomerReportsWebForm.JOBSTATUS_SELECTOR,
                    "label=" + jobStatus[i]);
        }
        // Target Locales
        for (int i = 0; i < targetLocales.length; i++)
        {
            selenium.addSelection(CustomerReportsWebForm.TARGETLOCALE_SELECTOR,
                    "label=" + targetLocales[i]);
        }
        // Date Range
        selenium.type(CustomerReportsWebForm.STARTSTIME, startTime);
        selenium.select(CustomerReportsWebForm.STARTSTIMEUNITS, "label="
                + startTimeUnits);
        selenium.type(CustomerReportsWebForm.ENDSTIME, endsTime);
        selenium.select(CustomerReportsWebForm.ENDSTIMEUNITS, "label="
                + endsTimeUnits);
        // Time Format
        selenium.select(CustomerReportsWebForm.DATEFORMAT, "label="
                + displayFormat);
    }
}
