package com.globalsight.selenium.testcases.smoketest;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.AfterMethod;
import com.globalsight.selenium.functions.CommonFuncs;
import com.globalsight.selenium.pages.MainFrame;
import com.globalsight.selenium.pages.MyActivities;
import com.globalsight.selenium.testcases.ConfigUtil;
import com.globalsight.selenium.testcases.BaseTestCase;
import com.globalsight.selenium.testcases.dataprepare.smoketest.job.CreateJobs;
import com.globalsight.selenium.testcases.dataprepare.smoketest.job.CreatedJob;

/*
 * TestCaseName: VerfiyActivityStatusReviewer.java
 * Author:Jester
 * Tests:verifyActivityStatusReviewer()
 * 
 * History:
 * Date       Comments       Updater
 * 2011-6-21  First Version  Jester
 */

public class VerifyActivityStatusReviewer extends BaseTestCase
{

    /*
     * Common Variables.
     */
    CreateJobs createJobs = new CreateJobs();

    // Author:Jester
    @Test
    public void verifyActivityStatusReviewer()
    {

        openMenuItemAndWait(selenium, MainFrame.MY_ACTIVITIES_MENU,
                MainFrame.MY_ACTIVITIES_AVAILABLE_SUBMENU);

        String jobName = getDataInCase(createJobs.getClassName(), "jobName1");
        jobName = CreatedJob.getCreatedJobName(jobName);
        clickAndWait(selenium, "link=" + jobName);

        // Check the info displayed in the details.
        Assert.assertEquals(
                selenium.getText(MyActivities.ACTIVITY_DETAILS_TABLE + "/tr[4]/td[2]"),
                getDataInCase("activity"));
        Assert.assertEquals(
                selenium.getText(MyActivities.ACTIVITY_DETAILS_TABLE + "/tr[5]/td[2]"),
                ConfigUtil.getConfigData("company"));
        Assert.assertEquals(
                selenium.getText(MyActivities.ACTIVITY_DETAILS_TABLE + "/tr[7]/td[2]"),
                ConfigUtil.getConfigData("pm"));
        Assert.assertEquals(
                selenium.getText(MyActivities.ACTIVITY_DETAILS_TABLE + "/tr[10]/td[2]"),
                getDataInCase("sourceLocale"));
        Assert.assertEquals(
                selenium.getText(MyActivities.ACTIVITY_DETAILS_TABLE + "/tr[11]/td[2]"),
                getDataInCase("targetLocale"));
        Assert.assertEquals(
                selenium.getText(MyActivities.ACTIVITY_DETAILS_TABLE + "/tr[14]/td[2]"),
                getDataInCase("overdue"));
        Assert.assertEquals(
                selenium.getText(MyActivities.ACTIVITY_DETAILS_TABLE + "/tr[15]/td[2]"),
                getDataInCase("status"));

        // Click Accept button.
        clickAndWait(selenium, MyActivities.ACCEPT_JOB_BUTTON);

        // Check the Task completed button and exit option exists.
        Assert.assertEquals(
                selenium.isElementPresent(MyActivities.TASK_COMPLETED_BUTTON),
                true);
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
