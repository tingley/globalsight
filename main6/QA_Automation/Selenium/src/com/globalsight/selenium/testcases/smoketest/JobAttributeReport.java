package com.globalsight.selenium.testcases.smoketest;

import java.io.File;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.globalsight.selenium.functions.CommonFuncs;
import com.globalsight.selenium.functions.DownloadFileRead.FileRead;
import com.globalsight.selenium.pages.JobAttributeReportWebForm;
import com.globalsight.selenium.pages.MainFrame;
import com.globalsight.selenium.properties.ConfigUtil;
import com.thoughtworks.selenium.Selenium;

/**
 * Job Attribute Report
 * 
 * @author leon
 * 
 */
public class JobAttributeReport
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
        selenium.click(JobAttributeReportWebForm.REPORT_LINK);

        selenium.waitForPopUp(JobAttributeReportWebForm.POPUP_WINDOW_NAME,
                CommonFuncs.SHORT_WAIT);
        selenium.selectWindow("name="
                + JobAttributeReportWebForm.POPUP_WINDOW_NAME);

        initOptions();

        selenium.click(JobAttributeReportWebForm.SUBMIT_BUTTON);
        selenium.getConfirmation();

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
                .getFile(JobAttributeReportWebForm.REPORT_FILE_NAME);
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

        String projects = ConfigUtil.getDataInCase(className, "projects");
        String jobStatus = ConfigUtil.getDataInCase(className, "status");
        String submitters = ConfigUtil.getDataInCase(className, "submitter");
        String targetLocales = ConfigUtil.getDataInCase(className, "targetLocales");
        if(!"".equals(projects))
        {
            selenium.removeAllSelections(JobAttributeReportWebForm.PROJECT_SELECTOR);
            String[] project = projects.split(",");
            for(int i=0;i<project.length;i++)
            {
                selenium.addSelection(JobAttributeReportWebForm.PROJECT_SELECTOR, project[i]);
            }
        }
        if(!"".equals(jobStatus))
        {
            selenium.removeAllSelections(JobAttributeReportWebForm.STATUS_SELECTOR);
            String[] status = jobStatus.split(",");
            for(int i=0;i<status.length;i++)
            {
                selenium.addSelection(JobAttributeReportWebForm.STATUS_SELECTOR, status[i]);
            }
        }
        if(!"".equals(submitters))
        {
            selenium.removeAllSelections(JobAttributeReportWebForm.SUBMITTERS_SELECTOR);
            String[] submitter = submitters.split(",");
            for(int i=0;i<submitter.length;i++)
            {
                selenium.addSelection(JobAttributeReportWebForm.SUBMITTERS_SELECTOR, submitter[i]);
            }
        }
        if(!"".equals(targetLocales))
        {
            selenium.removeAllSelections(JobAttributeReportWebForm.TARGETLOCALE_SELECTOR);
            String[] targetLocale = targetLocales.split(",");
            for(int i=0;i<targetLocale.length;i++)
            {
                selenium.addSelection(JobAttributeReportWebForm.TARGETLOCALE_SELECTOR, targetLocale[i]);
            }
        }
    }
}
