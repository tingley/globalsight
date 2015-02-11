package com.globalsight.selenium.testcases.smoketest;

import java.io.File;

import junit.framework.Assert;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.globalsight.selenium.functions.CommonFuncs;
import com.globalsight.selenium.functions.DownloadFileRead.FileRead;
import com.globalsight.selenium.pages.JobDetailsReportWebForm;
import com.globalsight.selenium.pages.MainFrame;
import com.globalsight.selenium.testcases.ConfigUtil;
import com.thoughtworks.selenium.Selenium;

/**
 * JobDetails Report
 * 
 * @author leon
 * 
 */
public class JobDetailsReport
{
    private Selenium selenium;
    private int i = 0;

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
        selenium.click(JobDetailsReportWebForm.REPORT_LINK);
        selenium.selectWindow(JobDetailsReportWebForm.POPUP_WINDOW_NAME);

        selenium.click(JobDetailsReportWebForm.INPROGRESS_CHECKBOX);
        selenium.click(JobDetailsReportWebForm.Submit_BUTTON);
        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);

        selenium.windowMaximize();

        takePicture();

        while (selenium.isEditable(JobDetailsReportWebForm.NEXT_BUTTON))
        {
            selenium.click(JobDetailsReportWebForm.NEXT_BUTTON);
            takePicture();
        }
    }

    /**
     * Take pictures
     * 
     * @param i
     */
    private void takePicture()
    {
        Number x = 0;
        Number y = 0;
        String ecal = "window.scrollTo(" + x + "," + y + ");";
        selenium.getEval(ecal);
        selenium.captureScreenshot(ConfigUtil.getConfigData("Base_Path_Result")
                + "files\\JobDetailsReport\\JobDetailsReport_" + i + ".jpg");
        FileRead fileRead = new FileRead();
        File file = fileRead
                .getFile("files\\JobDetailsReport\\JobDetailsReport_" + i
                        + ".jpg");
        Assert.assertTrue(file.exists());
        i++;
    }
}
