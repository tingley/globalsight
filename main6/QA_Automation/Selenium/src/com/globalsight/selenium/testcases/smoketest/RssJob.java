package com.globalsight.selenium.testcases.smoketest;

import junit.framework.Assert;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.globalsight.selenium.functions.BasicFuncs;
import com.globalsight.selenium.functions.CommonFuncs;
import com.globalsight.selenium.functions.JobActivityOperationFuncs;
import com.globalsight.selenium.pages.MainFrame;
import com.globalsight.selenium.pages.MyJobs;
import com.globalsight.selenium.pages.RssJobPage;
import com.globalsight.selenium.pages.RssJobProgress;
import com.globalsight.selenium.properties.ConfigUtil;
import com.thoughtworks.selenium.Selenium;

/**
 * Test case for Rss Job
 * 
 * @author leon
 * 
 */
public class RssJob
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
    public void rssJob()
    {
        String className = getClass().getName();
        String url = ConfigUtil.getDataInCase(className, "url");
        String urlTitle = ConfigUtil.getDataInCase(className, "urlTitle");
        String jobName = ConfigUtil.getDataInCase(className, "jobName");
        String sourceLocale = ConfigUtil.getDataInCase(className,
                "sourceLocale");
        String project = ConfigUtil.getDataInCase(className, "project");
        String targetLocale = ConfigUtil.getDataInCase(className,
                "targetLocale");

        // Add RSS reader
        selenium.click(MainFrame.DataSources_MENU);
        selenium.click(MainFrame.RSSREADER);
        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
        selenium.type(RssJobPage.RSSURL_INPUT, url);
        selenium.click(RssJobPage.ADD_BUTTON);
        selenium.chooseOkOnNextConfirmation();
        try
        {
            Thread.sleep((long) 10000);
        }
        catch (InterruptedException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        selenium.stop();
        selenium = CommonFuncs.initSelenium();

        CommonFuncs.loginSystemWithAdmin(selenium);
        selenium.click(MainFrame.DataSources_MENU);
        selenium.click(MainFrame.RSSREADER);
        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);

        selenium.click(urlTitle);
        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
        selenium.click(RssJobPage.OPENITEM);
        selenium.click(RssJobPage.TRANSLATE);
        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
        selenium.getConfirmation();

        // Create an RSS job
        selenium.type(RssJobProgress.JOBNAME, jobName);
        selenium.select(RssJobProgress.SRCLOCALES, "label=" + sourceLocale);
        selenium.select(RssJobProgress.PROJECTS, "label=" + project);
        selenium.click(RssJobProgress.NEXT_BUTTON);
        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
        selenium.click("//div[@id='Filehtml']/div[" + 1 + "]");
        selenium.click(RssJobProgress.MAP_BUTTON);
        selenium.click(RssJobProgress.NEXT2_BUTTON);
        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);

        selenium.type(RssJobProgress.JOBNAME, jobName);
        selenium.removeAllSelections(RssJobProgress.TARGETLOCALES);
        selenium.addSelection(RssJobProgress.TARGETLOCALES, targetLocale);
        selenium.click(RssJobProgress.CREATEJOB_BUTTON);
        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);

        CommonFuncs.logoutSystem(selenium);
        // job dispatc and complete
        JobActivityOperationFuncs jFuncs = new JobActivityOperationFuncs();
        String admin = ConfigUtil.getConfigData("admin_login_name");
        jFuncs.dispatchJob(selenium, admin, jobName, targetLocale.split(","));
        String anyone = ConfigUtil.getConfigData("anyone_login_name");
        jFuncs.acceptActivity(selenium, anyone, jobName);
        jFuncs.completeActivity(selenium, anyone, jobName);
        jFuncs.acceptActivity(selenium, anyone, jobName);
        jFuncs.completeActivity(selenium, anyone, jobName);
        CommonFuncs.loginSystemWithAdmin(selenium);
        selenium.click(MainFrame.Localized_SUBMENU);
        BasicFuncs basicFuncs = new BasicFuncs();
        // Verify the job is completed
        try
        {
            Assert.assertTrue(basicFuncs.isPresentInTable(selenium,
                    MyJobs.MyJobs_TABLE, jobName, 4));
        }
        catch (Exception e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
