package com.globalsight.selenium.testcases.smoketest;

import junit.framework.Assert;

import org.testng.annotations.Test;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.AfterMethod;
import com.globalsight.selenium.functions.CommonFuncs;
import com.globalsight.selenium.pages.MainFrame;
import com.globalsight.selenium.testcases.BaseTestCase;
import com.globalsight.selenium.testcases.dataprepare.smoketest.job.CreateJobs;
import com.globalsight.selenium.testcases.dataprepare.smoketest.job.CreatedJob;
import com.globalsight.selenium.testcases.util.SeleniumUtils;

/*
 * TestCaseName: MyActivityFinishedVerify.java 
 * Author:Jester
 * Tests:verifyActivityFinished()
 * 
 * History: Date Comments Updater 
 * 2011-6-22 First Version Jester
 */

public class MyActivitiesFinishedVerify extends BaseTestCase
{
    /**
     * Common variables
     */

    @Test
    public void verifyActivityFinished() throws Exception
    {
        openMenuItemAndWait(selenium, MainFrame.MY_JOBS_MENU,
                MainFrame.MY_JOBS_EXPORTED_SUBMENU);
        CreateJobs createJobs = new CreateJobs();
        String jobName = getDataInCase(createJobs.getClassName(), "jobName1");
        jobName = CreatedJob.getCreatedJobName(jobName);

        Assert.assertTrue(SeleniumUtils.isTextPresent(selenium, jobName));
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
}
