package com.globalsight.selenium.testcases.smoketest;

import junit.framework.Assert;

import org.testng.annotations.Test;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.AfterSuite;

import com.globalsight.selenium.functions.BasicFuncs;
import com.globalsight.selenium.functions.CommonFuncs;
import com.globalsight.selenium.pages.JobDetails;
import com.globalsight.selenium.pages.MainFrame;
import com.globalsight.selenium.pages.MyJobs;
import com.globalsight.selenium.properties.ConfigUtil;
import com.globalsight.selenium.testcases.BaseTestCase;
import com.globalsight.selenium.testcases.dataprepare.smoketest.job.CreateJobs;
import com.thoughtworks.selenium.Selenium;

/*
 * TestCaseName: MyJobsDetailsDispatch.java 
 * Author:Jester
 * Tests:verifyJobsDetailsDispatch()
 * 
 * History: Date Comments Updater 
 * 2011-6-24 First Version Jester
 */

public class MyJobsDetailsDispatch extends BaseTestCase
{
    /*
     * Common Variables
     */
    private Selenium selenium;
    BasicFuncs iBasicFuncs = new BasicFuncs();
    CreateJobs c = new CreateJobs();
    @Test
    public void verifyJobsDetailsDispatch() throws Exception
    {

        selenium.click(MainFrame.MyJobs_MENU);
        selenium.click(MainFrame.Ready_SUBMENU);
        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);

        selenium.click("link="
                + ConfigUtil.getDataInCase(c.getClassName(), "jobName1"));
        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
//        iBasicFuncs.selectRadioButtonFromTable(selenium,
//                JobDetails.Workflows_TABLE,
//                ConfigUtil.getDataInCase(getClassName(), "WORKFLOW"));
        selenium.click(MyJobs.CheckAll_LINK);
        selenium.click(JobDetails.Dispatch_BUTTON);
        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);

        Assert.assertTrue(iBasicFuncs.isPresentInTable(selenium,
                JobDetails.Workflows_TABLE,
                ConfigUtil.getDataInCase(getClassName(), "DISPATCHED"), 5));
    }

    @BeforeMethod
    public void beforeMethod()
    {
        CommonFuncs.loginSystemWithPM(selenium);
    }

    @AfterMethod
    public void afterMethod()
    {
        CommonFuncs.logoutSystem(selenium);
    }

    @BeforeClass
    public void beforeClass()
    {
    }

    @AfterClass
    public void afterClass()
    {
    }

    @BeforeTest
    public void beforeTest()
    {
        selenium = CommonFuncs.initSelenium();
    }

    @AfterTest
    public void afterTest()
    {
        CommonFuncs.endSelenium(selenium);
    }

    @BeforeSuite
    public void beforeSuite()
    {
    }

    @AfterSuite
    public void afterSuite()
    {
    }

}
