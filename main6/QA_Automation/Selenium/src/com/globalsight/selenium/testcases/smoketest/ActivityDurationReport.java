package com.globalsight.selenium.testcases.smoketest;

import java.io.File;

import org.testng.Assert;
import org.testng.Reporter;
import org.testng.annotations.Test;

import com.globalsight.selenium.functions.CommonFuncs;
import com.globalsight.selenium.functions.DownloadFileRead.FileRead;
import com.globalsight.selenium.pages.ActivityDurationReportWebForm;
import com.globalsight.selenium.pages.MainFrame;
import com.globalsight.selenium.testcases.BaseTestCase;

public class ActivityDurationReport extends BaseTestCase
{
    @Test
    public void generateReport()
    {
        openMenuItemAndWait(selenium, MainFrame.REPORTS_MENU,
                MainFrame.REPORTS_MAIN_SUBMENU);

        selenium.click(ActivityDurationReportWebForm.REPORT_LINK);
        selenium.waitForPopUp(ActivityDurationReportWebForm.POPUP_WINDOW_NAME,
                CommonFuncs.SHORT_WAIT);
        selenium.selectWindow("name="
                + ActivityDurationReportWebForm.POPUP_WINDOW_NAME);

        initOptions();

        selenium.click(ActivityDurationReportWebForm.SUBMIT_BUTTON);
        try
        {
            Thread.sleep((long) 10000);
        }
        catch (InterruptedException e)
        {
            Reporter.log("Error:: " + e.getMessage());
        }

        // Verify the file exists or not
        FileRead fileRead = new FileRead();
        File file = fileRead
                .getFile(ActivityDurationReportWebForm.REPORT_FILE_NAME);
        Assert.assertTrue(file.exists());
        // Moved the file to the sub folder.
        fileRead.moveFile(file);
    }

    /**
     * Init the options of the report
     */
    private void initOptions()
    {
        String[] projects = getDataInCase("project").split(",");
        String[] jobStatus = getDataInCase("jobStatus").split(",");
        String[] targetLocales = getDataInCase("targetLocale").split(",");
        String startTime = getDataInCase("startTime");
        String startTimeUnits = getDataInCase("startTimeUnits");
        String endsTime = getDataInCase("endsTime");
        String endsTimeUnits = getDataInCase("endsTimeUnits");
        String displayFormat = getDataInCase("displayFormat");

        selenium.removeSelection(
                ActivityDurationReportWebForm.PROJECTS_SELECTOR, "label=<ALL>");
        selenium.removeSelection(
                ActivityDurationReportWebForm.JOBSTATUS_SELECTOR, "label=<ALL>");
        selenium.removeSelection(
                ActivityDurationReportWebForm.TARGETLOCALE_SELECTOR,
                "label=<ALL>");
        
        // Projects
        for (int i = 0; i < projects.length; i++)
        {
            selenium.addSelection(
                    ActivityDurationReportWebForm.PROJECTS_SELECTOR, "label="
                            + projects[i]);
        }
        // Job Status
        for (int i = 0; i < jobStatus.length; i++)
        {
            selenium.addSelection(
                    ActivityDurationReportWebForm.JOBSTATUS_SELECTOR, "label="
                            + jobStatus[i]);
        }
        // Target Locales
        for (int i = 0; i < targetLocales.length; i++)
        {
            selenium.addSelection(
                    ActivityDurationReportWebForm.TARGETLOCALE_SELECTOR,
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
