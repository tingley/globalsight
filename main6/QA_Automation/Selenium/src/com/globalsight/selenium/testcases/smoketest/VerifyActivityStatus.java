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
import com.globalsight.selenium.testcases.dataprepare.smoketest.job.CreatedJob;

/*
 * TestCaseName: VerfiyActivityStatus.java
 * Author:Jester
 * Tests:verifyActivityStatus()
 * 
 * History:
 * Date       Comments       Updater
 * 2011-6-21  First Version  Jester
 */
public class VerifyActivityStatus extends BaseTestCase
{
    /*
     * Common Variables.
     */
    @Test
    public void verifyActivityStatus()
    {
        openMenuItemAndWait(selenium, MainFrame.MY_ACTIVITIES_MENU,
                MainFrame.MY_ACTIVITIES_AVAILABLE_SUBMENU);

        String jobName = getDataInCase("activityJobName");
        jobName = CreatedJob.getCreatedJobName(jobName);
        clickAndWait(selenium, "link=" + jobName);

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
