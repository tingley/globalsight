package com.globalsight.selenium.testcases.smoketest;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.AfterMethod;
import com.globalsight.selenium.functions.BasicFuncs;
import com.globalsight.selenium.functions.CommonFuncs;
import com.globalsight.selenium.pages.MainFrame;
import com.globalsight.selenium.pages.MyActivities;
import com.globalsight.selenium.testcases.ConfigUtil;
import com.globalsight.selenium.testcases.BaseTestCase;
import com.globalsight.selenium.testcases.dataprepare.smoketest.job.CreateJobs;
import com.globalsight.selenium.testcases.dataprepare.smoketest.job.CreatedJob;

/*
 * TestCaseName: ActivityJobFinishedReviewer.java 
 * Author:Jester
 * Tests:finishActivityJob()
 * 
 * History: Date Comments Updater 
 * 2011-6-22 First Version Jester
 */

public class ActivityJobFinishedReviewer extends BaseTestCase
{

    /**
     * Common variables
     */
    BasicFuncs basicFuncs = new BasicFuncs();
    CreateJobs createJobs = new CreateJobs();

    @Test
    public void finishAcitityJob() throws Exception
    {
        openMenuItemAndWait(selenium, MainFrame.MY_ACTIVITIES_MENU,
                MainFrame.MY_ACTIVITIES_INPROGRESS_SUBMENU);

        String jobName = getDataInCase(createJobs.getClassName(), "jobName1");
        jobName = CreatedJob.getCreatedJobName(jobName);
        clickAndWait(selenium, "link=" + jobName);

        selenium.click(MyActivities.TASK_COMPLETED_BUTTON);
        if (selenium.isConfirmationPresent())
        {
            Assert.assertTrue(selenium
                    .getConfirmation()
                    .matches(
                            "^Do you want to finish the activity and advance it to the next stage[\\s\\S]$"));
        }
        openMenuItemAndWait(selenium, MainFrame.MY_ACTIVITIES_MENU,
                MainFrame.MY_ACTIVITIES_FINISHED_SUBMENU);

        Assert.assertTrue(selenium
                .isElementPresent(MyActivities.DETAILED_WORD_COUNTS_BUTTON));

    }

    @BeforeMethod
    public void beforeMethod()
    {
        CommonFuncs.login(selenium, ConfigUtil.getConfigData("company")
                + ConfigUtil.getConfigData("reviewerName"),
                ConfigUtil.getConfigData("reviewerPassword"));
    }

    @AfterMethod
    public void afterMethod()
    {
        CommonFuncs.logoutSystem(selenium);
    }
}
