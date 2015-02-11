package com.globalsight.selenium.testcases.smoketest;

import java.io.File;

import junit.framework.Assert;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.globalsight.selenium.functions.CommonFuncs;
import com.globalsight.selenium.functions.DownloadFileRead.FileRead;
import com.globalsight.selenium.pages.MainFrame;
import com.globalsight.selenium.pages.StandardReportsTaskDurationReportWebForm;
import com.globalsight.selenium.properties.ConfigUtil;
import com.thoughtworks.selenium.Selenium;

/**
 * Standard Reports Task Duration Report
 * 
 * @author leon
 * 
 */
public class StandardReportsTaskDurationReport
{
    private Selenium selenium;
    private int i = 0;

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
        selenium.click(StandardReportsTaskDurationReportWebForm.REPORT_LINK);
        selenium.selectWindow(StandardReportsTaskDurationReportWebForm.POPUP_WINDOW_NAME);
        selenium.windowMaximize();

        takePicture();

        while (selenium
                .isEditable(StandardReportsTaskDurationReportWebForm.NEXT_BUTTON))
        {
            selenium.click(StandardReportsTaskDurationReportWebForm.NEXT_BUTTON);
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
        //selenium.getElementPositionLeft(StandardReportsTaskDurationReportWebForm.TMREPORT_TABLE_NAME);
        Number y = 0;
        //selenium.getElementPositionTop(StandardReportsTaskDurationReportWebForm.TMREPORT_TABLE_NAME);
        String ecal = "window.scrollTo(" + x + "," + y + ");";
        selenium.getEval(ecal);
        selenium.captureScreenshot(ConfigUtil.getConfigData("Base_Path_Result")
                + "files\\StandardReportsTaskDurationReport\\StandardReportsTaskDurationReport_"
                + i + ".jpg");

        FileRead fileRead = new FileRead();
        File file = fileRead
                .getFile("files\\StandardReportsTaskDurationReport\\StandardReportsTaskDurationReport_"
                        + i + ".jpg");
        Assert.assertTrue(file.exists());

        i++;
    }
}
