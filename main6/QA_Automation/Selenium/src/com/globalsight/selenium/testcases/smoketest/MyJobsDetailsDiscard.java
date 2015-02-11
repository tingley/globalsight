package com.globalsight.selenium.testcases.smoketest;

import junit.framework.Assert;

import org.testng.annotations.Test;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.AfterMethod;
import com.globalsight.selenium.functions.BasicFuncs;
import com.globalsight.selenium.functions.CommonFuncs;
import com.globalsight.selenium.pages.JobDetails;
import com.globalsight.selenium.pages.MainFrame;
import com.globalsight.selenium.testcases.ConfigUtil;
import com.globalsight.selenium.testcases.BaseTestCase;
import com.globalsight.selenium.testcases.dataprepare.smoketest.job.CreateJobs;
import com.globalsight.selenium.testcases.dataprepare.smoketest.job.CreatedJob;

/*
 * TestCaseName: MyJobsDetailsDiscard.java 
 * Author:Jester
 * Tests:verifyJobsDetailsDiscard()
 * 
 * History: Date Comments Updater 
 * 2011-6-26 First Version Jester
 */

public class MyJobsDetailsDiscard extends BaseTestCase
{
    /*
     * Common Variables
     */
    BasicFuncs basicFuncs = new BasicFuncs();
    CreateJobs createJobs = new CreateJobs();

    @Test
    public void verifyJobsDetailsDiscard() throws Exception
    {
        openMenuItemAndWait(selenium, MainFrame.MY_JOBS_MENU,
                MainFrame.MY_JOBS_READY_SUBMENU);

        String oriJobName = ConfigUtil.getDataInCase(createJobs.getClassName(),
                "jobName1");
        String jobName = CreatedJob.getCreatedJobName(oriJobName);
        selenium.click("link=" + jobName);
        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);

        basicFuncs.selectRadioButtonFromTable(selenium,
                JobDetails.WORKFLOWS_TABLE, getDataInCase("workflow"));
        selenium.click(JobDetails.DISCARD_BUTTON);
        if (selenium.isConfirmationPresent())
        {
            Assert.assertEquals(
                    (selenium.getConfirmation()
                            .matches("Warning!!\n\nThis will permanently remove the selected Workflows from the system.\nNote: There may be a short delay when Workflows jobs are being discarded.")),
                    true);
        }

        Assert.assertFalse(basicFuncs.isPresentInTable(selenium,
                JobDetails.WORKFLOWS_TABLE, getDataInCase("workflow")));
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
