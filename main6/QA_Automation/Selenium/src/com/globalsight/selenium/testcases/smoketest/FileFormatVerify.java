package com.globalsight.selenium.testcases.smoketest;
//author: ShenYang  2011-08-15
import junit.framework.Assert;

import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;

import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;

import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.globalsight.selenium.functions.BasicFuncs;
import com.globalsight.selenium.functions.CommonFuncs;
import com.globalsight.selenium.pages.JobDetails;
import com.globalsight.selenium.pages.MainFrame;
import com.globalsight.selenium.pages.MyActivities;
import com.globalsight.selenium.pages.MyJobs;
import com.globalsight.selenium.properties.ConfigUtil;
import com.globalsight.selenium.testcases.BaseTestCase;
import com.globalsight.selenium.testcases.dataprepare.smoketest.job.CreateJobs;
import com.thoughtworks.selenium.Selenium;

public class FileFormatVerify extends BaseTestCase
{
    private Selenium selenium;
    BasicFuncs iBasicFuncs = new BasicFuncs();
    CreateJobs c = new CreateJobs();
    String jn = ConfigUtil.getDataInCase(c.getClassName(), "jobName5");
    @Test
    public void fileFormatDispatch() throws Exception{
        selenium.click(MainFrame.MyJobs_MENU);
        selenium.click(MainFrame.Ready_SUBMENU);
        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
        selenium.click("link="
                + ConfigUtil.getDataInCase(c.getClassName(), "jobName5"));
        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
        selenium.click(MyJobs.CheckAll_LINK);
        selenium.click(JobDetails.Dispatch_BUTTON);
        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
        
        CommonFuncs.logoutSystem(selenium);
        CommonFuncs.loginSystemWithAnyone(selenium);
        
        selenium.click(MainFrame.MyActivities_MENU);
        selenium.click(MainFrame.Available_SUBMENU);
        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);

        selenium.click("link="+ jn);
        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
        
        selenium.click(MyActivities.Accept_BUTTON_Job);
        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
        selenium.click(MyActivities.TaskCompleted_BUTTON);
        if (selenium.isConfirmationPresent()) {
            selenium.getConfirmation();
        }

        selenium.click(MainFrame.MyActivities_MENU);
        selenium.click(MainFrame.Finished_SUBMENU);
        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
        Assert.assertEquals(iBasicFuncs.isPresentInTable(selenium, MyActivities.MyActivities_TABLE, "allFormatJob", 5), true);
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

 
}
