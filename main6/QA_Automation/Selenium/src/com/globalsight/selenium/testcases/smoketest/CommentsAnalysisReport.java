package com.globalsight.selenium.testcases.smoketest;

import java.io.File;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.globalsight.selenium.functions.CommonFuncs;
import com.globalsight.selenium.functions.DownloadFileRead.FileRead;
import com.globalsight.selenium.pages.CommentsAnalysisReportWebForm;
import com.globalsight.selenium.pages.MainFrame;
import com.globalsight.selenium.testcases.ConfigUtil;
import com.thoughtworks.selenium.Selenium;

/**
 * Comments Analysis Report
 * 
 * @author leon
 */
public class CommentsAnalysisReport
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
        selenium.click(CommentsAnalysisReportWebForm.REPORT_LINK);

        selenium.waitForPopUp(CommentsAnalysisReportWebForm.POPUP_WINDOW_NAME,
                CommonFuncs.SHORT_WAIT);
        selenium.selectWindow("name="
                + CommentsAnalysisReportWebForm.POPUP_WINDOW_NAME);

        initOptions();

        selenium.click(CommentsAnalysisReportWebForm.SUBMIT_BUTTON);

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
                .getFile(CommentsAnalysisReportWebForm.REPORT_FILE_NAME);
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
        // String jobName = ConfigUtil.getDataInCase(className, "jobName");
        String targetLocale = ConfigUtil.getDataInCase(className,
                "targetLocale");
        String displayFormat = ConfigUtil.getDataInCase(className,
                "displayFormat");

        // selenium.select(CommentsAnalysisReportWebForm.JOBID, "label="+
        // jobName);
        selenium.select(CommentsAnalysisReportWebForm.TARGETLOCALE_SELECTOR,
                "label=" + targetLocale);
        // Time Format
        selenium.select(CommentsAnalysisReportWebForm.DATEFORMAT, "label="
                + displayFormat);
    }
}
