package com.globalsight.selenium.testcases.smoketest;

import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.globalsight.selenium.functions.CommonFuncs;
import com.globalsight.selenium.pages.MainFrame;
import com.globalsight.selenium.pages.MyActivities;
import com.globalsight.selenium.testcases.ConfigUtil;
import com.globalsight.selenium.testcases.BaseTestCase;
import com.globalsight.selenium.testcases.dataprepare.smoketest.job.CreateJobs;
import com.globalsight.selenium.testcases.dataprepare.smoketest.job.CreatedJob;

/*
 * TestCaseName: VerifyActivityStatusAccept.java
 * Author:Jester
 * Tests:verifyActivityStatusAccept()
 * 
 * History:
 * Date       Comments       Updater
 * 2011-6-21  First Version  Jester
 */
public class VerifyActivityStatusAccept extends BaseTestCase
{
    /*
     * Common Variables.
     */
    CreateJobs createJobs = new CreateJobs();

    // author:Jester
    @Test
    public void verfiyAcitivityStatusAccept()
    {

        openMenuItemAndWait(selenium, MainFrame.MY_ACTIVITIES_MENU,
                MainFrame.MY_ACTIVITIES_AVAILABLE_SUBMENU);

        String jobName = getDataInCase(createJobs.getClassName(), "jobName1");
        jobName = CreatedJob.getCreatedJobName(jobName);
        clickAndWait(selenium, "link=" + jobName);

        clickAndWait(selenium, MyActivities.ACCEPT_JOB_BUTTON);

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
                selenium.getText(MyActivities.ACTIVITY_DETAILS_TABLE + "/tr[13]/td[2]"),
                getDataInCase("overdue"));
        Assert.assertEquals(
                selenium.getText(MyActivities.ACTIVITY_DETAILS_TABLE + "/tr[14]/td[2]"),
                getDataInCase("status"));
    }

    @BeforeMethod
    public void beforeMethod()
    {
        CommonFuncs.loginSystemWithAnyone(selenium);
    }

    @AfterMethod
    public void afterMethod()
    {
        CommonFuncs.logoutSystem(selenium);
    }
}
