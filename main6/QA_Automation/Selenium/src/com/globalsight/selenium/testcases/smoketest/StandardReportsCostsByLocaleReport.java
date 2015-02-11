package com.globalsight.selenium.testcases.smoketest;

import java.io.File;

import junit.framework.Assert;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.globalsight.selenium.functions.CommonFuncs;
import com.globalsight.selenium.functions.DownloadFileRead.FileRead;
import com.globalsight.selenium.pages.MainFrame;
import com.globalsight.selenium.pages.StandardCostsByLocaleReportWebForm;
import com.globalsight.selenium.properties.ConfigUtil;
import com.thoughtworks.selenium.Selenium;

/**
 * Standard Reports/Costs By Locale Report
 * 
 * @author leon
 * 
 */
public class StandardReportsCostsByLocaleReport
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
        selenium.click(StandardCostsByLocaleReportWebForm.REPORT_LINK);
        selenium.selectWindow(StandardCostsByLocaleReportWebForm.POPUP_WINDOW_NAME);
        selenium.windowMaximize();

        takePicture();

        while (selenium
                .isEditable(StandardCostsByLocaleReportWebForm.NEXT_BUTTON))
        {
            selenium.click(StandardCostsByLocaleReportWebForm.NEXT_BUTTON);
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
                .getElementPositionLeft(StandardCostsByLocaleReportWebForm.TMREPORT_TABLE_NAME);
        Number y = selenium
                .getElementPositionTop(StandardCostsByLocaleReportWebForm.TMREPORT_TABLE_NAME);
        String ecal = "window.scrollTo(" + x + "," + y + ");";
        selenium.getEval(ecal);
        selenium.captureScreenshot(ConfigUtil.getConfigData("Base_Path_Result")
                + "files\\StandardReportsCostsByLocaleReport\\StandardReportsCostsByLocaleReport_"
                + i + ".jpg");

        FileRead fileRead = new FileRead();
        File file = fileRead
                .getFile("files\\StandardReportsCostsByLocaleReport\\StandardReportsCostsByLocaleReport_"
                        + i + ".jpg");
        Assert.assertTrue(file.exists());

        i++;
    }
}
