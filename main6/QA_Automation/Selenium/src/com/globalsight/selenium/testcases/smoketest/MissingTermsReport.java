package com.globalsight.selenium.testcases.smoketest;

import java.io.File;

import junit.framework.Assert;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.globalsight.selenium.functions.CommonFuncs;
import com.globalsight.selenium.functions.DownloadFileRead.FileRead;
import com.globalsight.selenium.pages.MainFrame;
import com.globalsight.selenium.pages.MissingTermsReportWebForm;
import com.globalsight.selenium.properties.ConfigUtil;
import com.thoughtworks.selenium.Selenium;

public class MissingTermsReport
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
        selenium.click(MissingTermsReportWebForm.REPORT_LINK);
        selenium.selectWindow(MissingTermsReportWebForm.POPUP_WINDOW_NAME);

        initOptions();
        selenium.click(MissingTermsReportWebForm.Submit_BUTTON);
        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
        selenium.windowMaximize();

        takePicture();

        while (selenium.isEditable(MissingTermsReportWebForm.NEXT_BUTTON))
        {
            selenium.click(MissingTermsReportWebForm.NEXT_BUTTON);
            takePicture();
        }
    }
        /**
         * Init options
         */
        private void initOptions()
        {
            String className = getClass().getName();
            String termbase = ConfigUtil.getDataInCase(className,
                    "termbase");
            String language = ConfigUtil.getDataInCase(className,
                    "language");
            
            selenium.select(MissingTermsReportWebForm.TB_SELECT, termbase);
            selenium.select(MissingTermsReportWebForm.TB_LANGS, language);
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
                    + "files\\MissingTermsReport\\MissingTermsReport_" + i
                    + ".jpg");
            FileRead fileRead = new FileRead();
            File file = fileRead
                    .getFile("files\\MissingTermsReport\\MissingTermsReport_"
                            + i + ".jpg");
            Assert.assertTrue(file.exists());
            i++;
        }
        
        
}
