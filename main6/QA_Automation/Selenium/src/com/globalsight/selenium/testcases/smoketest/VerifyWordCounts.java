package com.globalsight.selenium.testcases.smoketest;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.AfterMethod;
import com.globalsight.selenium.functions.CommonFuncs;
import com.globalsight.selenium.pages.MainFrame;
import com.globalsight.selenium.pages.MyActivities;
import com.globalsight.selenium.testcases.BaseTestCase;
import com.globalsight.selenium.testcases.dataprepare.smoketest.job.CreatedJob;

/*
 * TestCaseName: VerifyWrodCounts.java
 * Author:Jester
 * Tests:verifyWordCounts()
 * 
 * History:
 * Date       Comments       Updater
 * 2011-6-21  First Version  Jester
 */

public class VerifyWordCounts extends BaseTestCase
{
    /*
     * Common Variables.
     */
    @Test
    public void verifyWordCounts()
    {
        openMenuItemAndWait(selenium, MainFrame.MY_ACTIVITIES_MENU,
                MainFrame.MY_ACTIVITIES_AVAILABLE_SUBMENU);

        String jobName = getDataInCase("activityJobName");
        jobName = CreatedJob.getCreatedJobName(jobName);
        clickAndWait(selenium, "link=" + jobName);

        clickAndWait(selenium, MyActivities.DETAILED_WORD_COUNT_IN_JOB_BUTTON);

        Assert.assertEquals(
                selenium.getText(MyActivities.DETAILED_STATISTICS_TABLE
                        + "/tr[2]/td[3]"), getDataInCase("100%"));
        Assert.assertEquals(
                selenium.getText(MyActivities.DETAILED_STATISTICS_TABLE
                        + "/tr[2]/td[4]"), getDataInCase("95%99%"));
        Assert.assertEquals(
                selenium.getText(MyActivities.DETAILED_STATISTICS_TABLE
                        + "/tr[2]/td[5]"), getDataInCase("85%94%"));
        Assert.assertEquals(
                selenium.getText(MyActivities.DETAILED_STATISTICS_TABLE
                        + "/tr[2]/td[6]"), getDataInCase("75%84%"));
        Assert.assertEquals(
                selenium.getText(MyActivities.DETAILED_STATISTICS_TABLE
                        + "/tr[2]/td[7]"), getDataInCase("50%74%"));
        Assert.assertEquals(
                selenium.getText(MyActivities.DETAILED_STATISTICS_TABLE
                        + "/tr[2]/td[8]"), getDataInCase("nomatch"));
        Assert.assertEquals(
                selenium.getText(MyActivities.DETAILED_STATISTICS_TABLE
                        + "/tr[2]/td[9]"), getDataInCase("repetitions"));
        Assert.assertEquals(
                selenium.getText(MyActivities.DETAILED_STATISTICS_TABLE
                        + "/tr[2]/td[10]"), getDataInCase("incontextMatch"));
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
