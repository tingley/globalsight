package com.globalsight.selenium.testcases.smoketest;

import java.io.File;

import junit.framework.Assert;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.globalsight.selenium.functions.CommonFuncs;
import com.globalsight.selenium.functions.DownloadFileRead.FileRead;
import com.globalsight.selenium.pages.MainFrame;
import com.globalsight.selenium.pages.TmReportWebForm;
import com.globalsight.selenium.properties.ConfigUtil;
import com.thoughtworks.selenium.Selenium;

/**
 * TM Report
 * 
 * @author leon
 * 
 */
public class TmReport
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
        selenium.click(TmReportWebForm.REPORT_LINK);
        selenium.selectWindow(TmReportWebForm.POPUP_WINDOW_NAME);
        selenium.windowMaximize();

        takePicture();
        
        while (selenium.isEditable(TmReportWebForm.NEXT_BUTTON))
        {
            selenium.click(TmReportWebForm.NEXT_BUTTON);
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
        Number x = selenium
                .getElementPositionLeft(TmReportWebForm.TMREPORT_TABLE_NAME);
        Number y = selenium
                .getElementPositionTop(TmReportWebForm.TMREPORT_TABLE_NAME);
        String ecal = "window.scrollTo(" + x + "," + y + ");";
        selenium.getEval(ecal);
        selenium.captureScreenshot(ConfigUtil.getConfigData("Base_Path_Result")
                + "files\\TmReport\\TmReport_" + i + ".jpg");
        
        FileRead fileRead = new FileRead();
        File file = fileRead
                .getFile("files\\TmReport\\TmReport_" + i
                        + ".jpg");
        Assert.assertTrue(file.exists());
        
        i++;
    }
}
