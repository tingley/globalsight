package com.globalsight.selenium.testcases.smoketest;

import java.io.File;

import junit.framework.Assert;

import org.testng.annotations.Test;

import com.globalsight.selenium.functions.DownloadFileRead.FileRead;
import com.globalsight.selenium.pages.AvgPerCompReportWebForm;
import com.globalsight.selenium.pages.MainFrame;
import com.globalsight.selenium.testcases.ConfigUtil;
import com.globalsight.selenium.testcases.BaseTestCase;

/**
 * AvgPerComp Report
 * 
 * @author leon
 * 
 */
public class AvgPerCompReport extends BaseTestCase
{
    private int pictureIndex = 1;

    @Test
    public void generateReport()
    {
        openMenuItemAndWait(selenium, MainFrame.REPORTS_MENU,
                MainFrame.REPORTS_MAIN_SUBMENU);

        selenium.click(AvgPerCompReportWebForm.REPORT_LINK);
        selenium.selectWindow(AvgPerCompReportWebForm.POPUP_WINDOW_NAME);

        clickAndWait(selenium, AvgPerCompReportWebForm.SUBMIT_BUTTON);

        selenium.windowMaximize();

        takePicture();

        while (selenium.isEditable(AvgPerCompReportWebForm.NEXT_BUTTON))
        {
            selenium.click(AvgPerCompReportWebForm.NEXT_BUTTON);
            takePicture();
        }
    }

    /**
     * Take pictures
     * 
     * @param pictureIndex
     */
    private void takePicture()
    {
        Number x = 0;
        Number y = 0;
        String ecal = "window.scrollTo(" + x + "," + y + ");";
        selenium.getEval(ecal);
        selenium.captureScreenshot(ConfigUtil.getConfigData("Base_Path_Result")
                + "files\\AvgPerCompReport\\AvgPerCompReport_" + pictureIndex
                + ".jpg");
        FileRead fileRead = new FileRead();
        File file = fileRead
                .getFile("files\\AvgPerCompReport\\AvgPerCompReport_"
                        + pictureIndex + ".jpg");
        Assert.assertTrue(file.exists());
        pictureIndex++;
    }
}
