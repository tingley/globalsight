package com.globalsight.selenium.testcases.smoketest;

import java.io.File;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.globalsight.selenium.functions.CommonFuncs;
import com.globalsight.selenium.functions.DownloadFileRead.FileRead;
import com.globalsight.selenium.pages.MainFrame;
import com.globalsight.selenium.pages.TranslationsEditReportWebForm;
import com.globalsight.selenium.properties.ConfigUtil;
import com.thoughtworks.selenium.Selenium;

/**
 * Translations Edit Report
 * 
 * Based on the job "ReportJob"(data prepare)
 * 
 * @author leon
 */
public class TranslationsEditReport
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
        selenium.click(TranslationsEditReportWebForm.REPORT_LINK);

        selenium.waitForPopUp(TranslationsEditReportWebForm.POPUP_WINDOW_NAME,
                CommonFuncs.SHORT_WAIT);
        selenium.selectWindow("name="
                + TranslationsEditReportWebForm.POPUP_WINDOW_NAME);

        initOptions();

        selenium.click(TranslationsEditReportWebForm.SUBMIT_BUTTON);

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
                .getFile(TranslationsEditReportWebForm.REPORT_FILE_NAME);
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
        String jobName = "ReportJob";
        String targetLocale = "French (France) [fr_FR]";
        String displayFormat = ConfigUtil.getDataInCase(className,
                "displayFormat");

        selenium.select(TranslationsEditReportWebForm.JOBID, "label=" + jobName);
        selenium.select(TranslationsEditReportWebForm.TARGETLOCALE_SELECTOR,
                "label=" + targetLocale);
        selenium.select(TranslationsEditReportWebForm.DATEFORMAT, displayFormat);
    }
}
