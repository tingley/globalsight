package com.globalsight.selenium.testcases.smoketest;

import java.io.File;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.globalsight.selenium.functions.CommonFuncs;
import com.globalsight.selenium.functions.DownloadFileRead.FileRead;
import com.globalsight.selenium.pages.CommentsReportsWebForm;
import com.globalsight.selenium.pages.MainFrame;
import com.globalsight.selenium.properties.ConfigUtil;
import com.thoughtworks.selenium.Selenium;

/**
 * Comments Report
 */
public class CommentsReports
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
        selenium.click(CommentsReportsWebForm.REPORT_LINK);

        selenium.waitForPopUp(CommentsReportsWebForm.POPUP_WINDOW_NAME,
                CommonFuncs.SHORT_WAIT);
        selenium.selectWindow("name="
                + CommentsReportsWebForm.POPUP_WINDOW_NAME);

        initOptions();

        selenium.click(CommentsReportsWebForm.SUBMIT_BUTTON);

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
        File file = fileRead.getFile(CommentsReportsWebForm.REPORT_FILE_NAME);
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
        String[] commentOptionsInclude = ConfigUtil.getDataInCase(className,
                "commentOptionsInclude").split(",");
        String startTime = ConfigUtil.getDataInCase(className, "startTime");
        String startTimeUnits = ConfigUtil.getDataInCase(className,
                "startTimeUnits");
        String endsTime = ConfigUtil.getDataInCase(className, "endsTime");
        String endsTimeUnits = ConfigUtil.getDataInCase(className,
                "endsTimeUnits");
        String displayFormat = ConfigUtil.getDataInCase(className,
                "displayFormat");

        selenium.removeSelection(CommentsReportsWebForm.PROJECT_SELECTOR,
                "label=<ALL>");
        selenium.removeSelection(CommentsReportsWebForm.JOBSTATUS_SELECTOR,
                "label=<ALL>");
        selenium.removeSelection(CommentsReportsWebForm.TARGETLOCALE_SELECTOR,
                "label=<ALL>");

        // Project
        for (int i = 0; i < projects.length; i++)
        {
            selenium.addSelection(CommentsReportsWebForm.PROJECT_SELECTOR,
                    "label=" + projects[i]);
        }
        // Job Status
        for (int i = 0; i < jobStatus.length; i++)
        {
            selenium.addSelection(CommentsReportsWebForm.JOBSTATUS_SELECTOR,
                    "label=" + jobStatus[i]);
        }
        // Target Locales
        for (int i = 0; i < targetLocales.length; i++)
        {
            selenium.addSelection(CommentsReportsWebForm.TARGETLOCALE_SELECTOR,
                    "label=" + targetLocales[i]);
        }
        // commentOptionsInclude
        for (int i = 0; i < commentOptionsInclude.length; i++)
        {
            if ("Job Comments".equals(commentOptionsInclude[i]))
            {
                selenium.click(CommentsReportsWebForm.INCLUDE_JOBCOMMENTS);
            }
            else if ("Activity Comments".equals(commentOptionsInclude[i]))
            {
                selenium.click(CommentsReportsWebForm.INCLUDE_ACTIVITYCOMMENTS);
            }
            else if ("Segment Priority".equals(commentOptionsInclude[i]))
            {
                selenium.click(CommentsReportsWebForm.INCLUDE_SEGMENTPRIVORITY);
            }
            else if ("Segment Category".equals(commentOptionsInclude[i]))
            {
                selenium.click(CommentsReportsWebForm.INCLUDE_SEGMENTCATEGORY);
            }
            else if ("Segment Status Open".equals(commentOptionsInclude[i]))
            {
                selenium.click(CommentsReportsWebForm.INCLUDE_SEGMENTSTATUSOPEN);
            }
            else if ("Segment Status Query".equals(commentOptionsInclude[i]))
            {
                selenium.click(CommentsReportsWebForm.INCLUDE_SEGMENTSTATUSQUERY);
            }
            else if ("Segment Status Closed".equals(commentOptionsInclude[i]))
            {
                selenium.click(CommentsReportsWebForm.INCLUDE_SEGMENTSTATUSCLOSED);
            }
        }

        // Date Range
        selenium.type(CommentsReportsWebForm.STARTSTIME, startTime);
        selenium.select(CommentsReportsWebForm.STARTSTIMEUNITS, "label="
                + startTimeUnits);
        selenium.type(CommentsReportsWebForm.ENDSTIME, endsTime);
        selenium.select(CommentsReportsWebForm.ENDSTIMEUNITS, "label="
                + endsTimeUnits);
        // Time Format
        selenium.select(CommentsReportsWebForm.DATEFORMAT, "label="
                + displayFormat);
    }
}
