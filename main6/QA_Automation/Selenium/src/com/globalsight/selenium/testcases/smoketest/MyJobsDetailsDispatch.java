package com.globalsight.selenium.testcases.smoketest;

import junit.framework.Assert;

import org.testng.annotations.Test;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.AfterMethod;
import com.globalsight.selenium.functions.BasicFuncs;
import com.globalsight.selenium.functions.CommonFuncs;
import com.globalsight.selenium.pages.JobDetails;
import com.globalsight.selenium.pages.MainFrame;
import com.globalsight.selenium.pages.MyJobs;
import com.globalsight.selenium.testcases.ConfigUtil;
import com.globalsight.selenium.testcases.BaseTestCase;
import com.globalsight.selenium.testcases.dataprepare.smoketest.job.CreateJobs;
import com.globalsight.selenium.testcases.dataprepare.smoketest.job.CreatedJob;

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
    BasicFuncs basicFuncs = new BasicFuncs();
    CreateJobs createJobs = new CreateJobs();

    @Test
    public void verifyJobsDetailsDispatch() throws Exception
    {

        openMenuItemAndWait(selenium, MainFrame.MY_JOBS_MENU,
                MainFrame.MY_JOBS_READY_SUBMENU);

        String oriJobName = ConfigUtil.getDataInCase(createJobs.getClassName(),
                "jobName1");
        String jobName = CreatedJob.getCreatedJobName(oriJobName);
        clickAndWait(selenium, "link=" + jobName);
        selenium.click(MyJobs.CHECK_ALL_LINK);
        clickAndWait(selenium, JobDetails.DISPATCH_BUTTON);

        Assert.assertTrue(basicFuncs.isPresentInTable(selenium,
                JobDetails.WORKFLOWS_TABLE, getDataInCase("dispatched"), 5));
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
