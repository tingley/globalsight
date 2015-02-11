package com.globalsight.selenium.testcases.smoketest;

import java.io.File;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.globalsight.selenium.functions.CommonFuncs;
import com.globalsight.selenium.functions.DownloadFileRead.FileRead;
import com.globalsight.selenium.pages.MainFrame;
import com.globalsight.selenium.pages.TranslationProgressReportWebForm;
import com.globalsight.selenium.testcases.ConfigUtil;
import com.thoughtworks.selenium.Selenium;

public class TranslationProgressReport
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
        selenium.click(TranslationProgressReportWebForm.REPORT_LINK);

        selenium.waitForPopUp(TranslationProgressReportWebForm.POPUP_WINDOW_NAME,
                CommonFuncs.SHORT_WAIT);
        selenium.selectWindow("name="
                + TranslationProgressReportWebForm.POPUP_WINDOW_NAME);

        initOptions();

        selenium.click(TranslationProgressReportWebForm.SUBMIT_BUTTON);

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
        File file = fileRead.getFile(TranslationProgressReportWebForm.REPORT_FILE_NAME);
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

        String[] projects = ConfigUtil.getDataInCase(className, "project")
                .split(",");
        String src = ConfigUtil.getDataInCase(className, "sourcelocale");
        String tar = ConfigUtil.getDataInCase(className, "targetlocale");
        String startTime = ConfigUtil.getDataInCase(className, "startTime");
        String startTimeUnits = ConfigUtil.getDataInCase(className,
                "startTimeUnits");
        String endsTime = ConfigUtil.getDataInCase(className, "endsTime");
        String endsTimeUnits = ConfigUtil.getDataInCase(className,
                "endsTimeUnits");
       
        selenium.removeSelection(TranslationProgressReportWebForm.PROJECTS_SELECTOR,
                "label=<ALL>");
     
        // Projects
        for (int i = 0; i < projects.length; i++)
        {
            selenium.addSelection(TranslationProgressReportWebForm.PROJECTS_SELECTOR,
                    "label=" + projects[i]);
        }
        //Source locale
        selenium.select(TranslationProgressReportWebForm.SOURCELOCALE_SELECTOR,src);
        //Target locale
        selenium.select(TranslationProgressReportWebForm.TARGETLOCALE_SELECTOR, tar);  
        // Date Range
        selenium.type(TranslationProgressReportWebForm.STARTSTIME, startTime);
        selenium.select(TranslationProgressReportWebForm.STARTSTIMEUNITS, "label="
                + startTimeUnits);
        selenium.type(TranslationProgressReportWebForm.ENDSTIME, endsTime);
        selenium.select(TranslationProgressReportWebForm.ENDSTIMEUNITS, "label="
                + endsTimeUnits);

    }
}
