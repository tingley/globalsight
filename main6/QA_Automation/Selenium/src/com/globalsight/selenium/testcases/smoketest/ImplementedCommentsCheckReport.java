
package com.globalsight.selenium.testcases.smoketest;

import java.io.File;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.globalsight.selenium.functions.CommonFuncs;
import com.globalsight.selenium.functions.DownloadFileRead.FileRead;
import com.globalsight.selenium.pages.CustomerReportsWebForm;
import com.globalsight.selenium.pages.ImplementedCommentsCheckReportWebForm;
import com.globalsight.selenium.pages.MainFrame;
import com.globalsight.selenium.testcases.ConfigUtil;
import com.thoughtworks.selenium.Selenium;

/**
 * ImplementedCommentsCheckReport Test Case
 * 
 * Based on the job "ReportJob"(dataprepare)
 * 
 * @author leon
 * 
 */
public class ImplementedCommentsCheckReport
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
        selenium.click(ImplementedCommentsCheckReportWebForm.REPORT_LINK);

        selenium.waitForPopUp(
                ImplementedCommentsCheckReportWebForm.POPUP_WINDOW_NAME,
                CommonFuncs.SHORT_WAIT);
        selenium.selectWindow("name="
                + ImplementedCommentsCheckReportWebForm.POPUP_WINDOW_NAME);

        initOptions();

        selenium.click(ImplementedCommentsCheckReportWebForm.SUBMIT_BUTTON);

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
        File file = fileRead.getFile(ImplementedCommentsCheckReportWebForm.REPORT_FILE_NAME);
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

        String jobName = "ReportJob";
        String targetLocale = "French (France) [fr_FR]";
        String dateFormat = ConfigUtil
                .getDataInCase(className, "displayFormat");

        selenium.select(ImplementedCommentsCheckReportWebForm.JOBNAME, "label="
                + jobName);
        selenium.select(ImplementedCommentsCheckReportWebForm.TARGETLOCALE,
                "label=" + targetLocale);
        selenium.select(CustomerReportsWebForm.DATEFORMAT, "label="
                + dateFormat);
    }

}
