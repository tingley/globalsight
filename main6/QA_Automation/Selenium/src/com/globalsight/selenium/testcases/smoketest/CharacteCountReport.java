package com.globalsight.selenium.testcases.smoketest;

import java.io.File;

import org.testng.Assert;
import org.testng.Reporter;
import org.testng.annotations.Test;

import com.globalsight.selenium.functions.CommonFuncs;
import com.globalsight.selenium.functions.DownloadFileRead.FileRead;
import com.globalsight.selenium.pages.CharacteCountReportWebForm;
import com.globalsight.selenium.pages.MainFrame;
import com.globalsight.selenium.testcases.BaseTestCase;

/**
 * Characte Count Report
 * 
 * @author leon
 */
public class CharacteCountReport extends BaseTestCase
{
    @Test
    public void generateReport()
    {
        openMenuItemAndWait(selenium, MainFrame.REPORTS_MENU,
                MainFrame.REPORTS_MAIN_SUBMENU);

        selenium.click(CharacteCountReportWebForm.REPORT_LINK);

        selenium.waitForPopUp(CharacteCountReportWebForm.POPUP_WINDOW_NAME,
                CommonFuncs.SHORT_WAIT);
        selenium.selectWindow("name="
                + CharacteCountReportWebForm.POPUP_WINDOW_NAME);

        initOptions();

        selenium.click(CharacteCountReportWebForm.SUBMIT_BUTTON);

        // Wait for the download progress finish.
        try
        {
            Thread.sleep((long) 10000);
        }
        catch (InterruptedException e)
        {
            Reporter.log("Error found in CharacteCountReport. "
                    + e.getMessage());
        }

        // Verify the file exists or not
        FileRead fileRead = new FileRead();
        File file = fileRead
                .getFile(CharacteCountReportWebForm.REPORT_FILE_NAME);
        Assert.assertTrue(file.exists());
        // Moved the file to the sub folder.
        fileRead.moveFile(file);
    }

    /**
     * Init the options of the report
     */
    private void initOptions()
    {
        // JobName if needed
        String jobName = getDataInCase("jobName");
        String targetLocale = getDataInCase("targetLocale");

        selenium.select(CharacteCountReportWebForm.JOBID, "label=" + jobName);
        selenium.select(CharacteCountReportWebForm.TARGETLOCALE_SELECTOR,
                "label=" + targetLocale);
    }
}
