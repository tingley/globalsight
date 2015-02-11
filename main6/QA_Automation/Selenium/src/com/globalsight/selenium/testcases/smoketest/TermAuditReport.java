package com.globalsight.selenium.testcases.smoketest;

import java.io.File;

import junit.framework.Assert;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.globalsight.selenium.functions.CommonFuncs;
import com.globalsight.selenium.functions.DownloadFileRead.FileRead;
import com.globalsight.selenium.pages.MainFrame;
import com.globalsight.selenium.pages.TermAuditReportWebForm;
import com.globalsight.selenium.testcases.ConfigUtil;
import com.thoughtworks.selenium.Selenium;

public class TermAuditReport
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
        selenium.click(TermAuditReportWebForm.REPORT_LINK);
        selenium.selectWindow(TermAuditReportWebForm.POPUP_WINDOW_NAME);
        
        selenium.select(TermAuditReportWebForm.LANGS_SELECT, ConfigUtil.getDataInCase(getClass().getName(), "langs"));
        selenium.click(TermAuditReportWebForm.Submit_BUTTON);
        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
        selenium.windowMaximize();

        takePicture();

        while (selenium.isEditable(TermAuditReportWebForm.NEXT_BUTTON))
        {
            selenium.click(TermAuditReportWebForm.NEXT_BUTTON);
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
                + "files\\TermAuditReport\\TermAuditReport_" + i + ".jpg");
        FileRead fileRead = new FileRead();
        File file = fileRead
                .getFile("files\\TermAuditReport\\TermAuditReport_" + i
                        + ".jpg");
        Assert.assertTrue(file.exists());
        i++;
    }
}
