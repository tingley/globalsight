package com.globalsight.selenium.testcases.smoketest;

import java.io.File;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.globalsight.selenium.functions.CommonFuncs;
import com.globalsight.selenium.functions.DownloadFileRead.FileRead;
import com.globalsight.selenium.pages.CharacteCountReportWebForm;
import com.globalsight.selenium.pages.MainFrame;
import com.globalsight.selenium.properties.ConfigUtil;
import com.thoughtworks.selenium.Selenium;

/**
 * Characte Count Report
 * 
 * @author leon
 */
public class CharacteCountReport
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
            // TODO Auto-generated catch block
            e.printStackTrace();
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
        String className = getClass().getName();
        // JobName if needed
        String jobName = ConfigUtil.getDataInCase(className, "jobName");
        String targetLocale = ConfigUtil.getDataInCase(className,
                "targetLocale");

        selenium.select(CharacteCountReportWebForm.JOBID, "label=" + jobName);
        selenium.select(CharacteCountReportWebForm.TARGETLOCALE_SELECTOR,
                "label=" + targetLocale);
    }
}
