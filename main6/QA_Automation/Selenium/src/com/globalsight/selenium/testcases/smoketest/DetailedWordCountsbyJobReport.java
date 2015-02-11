package com.globalsight.selenium.testcases.smoketest;

import java.io.File;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.globalsight.selenium.functions.CommonFuncs;
import com.globalsight.selenium.functions.DownloadFileRead.FileRead;
import com.globalsight.selenium.pages.FileListReportWebForm;
import com.globalsight.selenium.pages.MainFrame;
import com.globalsight.selenium.testcases.ConfigUtil;
import com.thoughtworks.selenium.Selenium;

/**
 * FileList Report
 * 
 * @author leon
 */
public class DetailedWordCountsbyJobReport
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
        selenium.click(FileListReportWebForm.REPORT_LINK);

        selenium.waitForPopUp(FileListReportWebForm.POPUP_WINDOW_NAME,
                CommonFuncs.SHORT_WAIT);
        selenium.selectWindow("name=" + FileListReportWebForm.POPUP_WINDOW_NAME);

        initOptions();
        selenium.click(FileListReportWebForm.SUBMIT_BUTTON);

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
        String exportFormat = ConfigUtil.getDataInCase(getClass().getName(),
                "exportFormat");
        String fileName = "csv".equals(exportFormat) ? FileListReportWebForm.REPORT_FILE_NAME_CSV : FileListReportWebForm.REPORT_FILE_NAME_XLS;
        FileRead fileRead = new FileRead();
        File file = fileRead.getFile(fileName);
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
        String exportFormat = ConfigUtil.getDataInCase(className,
                "exportFormat");

        selenium.removeSelection(FileListReportWebForm.PROJECT_SELECTOR,
                "label=<ALL>");
        selenium.removeSelection(FileListReportWebForm.JOBSTATUS_SELECTOR,
                "label=<ALL>");
        selenium.removeSelection(FileListReportWebForm.TARGETLOCALE_SELECTOR,
                "label=<ALL>");

        // Project
        for (int i = 0; i < projects.length; i++)
        {
            selenium.addSelection(FileListReportWebForm.PROJECT_SELECTOR,
                    "label=" + projects[i]);
        }
        // Job Status
        for (int i = 0; i < jobStatus.length; i++)
        {
            selenium.addSelection(FileListReportWebForm.JOBSTATUS_SELECTOR,
                    "label=" + jobStatus[i]);
        }
        // Target Locales
        for (int i = 0; i < targetLocales.length; i++)
        {
            selenium.addSelection(FileListReportWebForm.TARGETLOCALE_SELECTOR,
                    "label=" + targetLocales[i]);
        }
        // Date Range
        selenium.type(FileListReportWebForm.STARTSTIME, startTime);
        selenium.select(FileListReportWebForm.STARTSTIMEUNITS, "label="
                + startTimeUnits);
        selenium.type(FileListReportWebForm.ENDSTIME, endsTime);
        selenium.select(FileListReportWebForm.ENDSTIMEUNITS, "label="
                + endsTimeUnits);
        // Time Format
        selenium.select(FileListReportWebForm.DATEFORMAT, "label="
                + displayFormat);

        if ("csv".equals(exportFormat))
        {
            selenium.click(FileListReportWebForm.EXPORTFORMATCSV);
        }
    }
}
