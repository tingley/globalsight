package com.globalsight.selenium.testcases.smoketest;

import org.testng.Assert;
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
 * TestCaseName: VerifyJobWrodCounts.java
 * Author:Jester
 * Tests:verifyJobWordCounts()
 * 
 * History:
 * Date       Comments       Updater
 * 2011-6-30  First Version  Jester
 */

public class VerifyJobWordCounts extends BaseTestCase
{

    /*
     * Common Variables.
     */
    BasicFuncs basicFuncs = new BasicFuncs();
    CreateJobs createJobs = new CreateJobs();

    /*
     * Verify the Job details word counts.
     */
    @Test
    public void verifyJobWordCounts() throws Exception
    {
        openMenuItemAndWait(selenium, MainFrame.MY_JOBS_MENU,
                MainFrame.MY_JOBS_INPROGRESS_SUBMENU);
        String jobName = ConfigUtil.getDataInCase(createJobs.getClassName(),
                "jobName1");
        jobName = CreatedJob.getCreatedJobName(jobName);
        clickAndWait(selenium, "link=" + jobName);
        basicFuncs.selectRadioButtonFromTable(selenium,
                JobDetails.WORKFLOWS_TABLE, getDataInCase("workflow"));
        clickAndWait(selenium, JobDetails.DETAILED_WORD_COUNTS_BUTTON);

        Assert.assertEquals(
                selenium.getText(JobDetails.DETAILED_STATISTICS_TABLE
                        + "/tr[2]/td[3]"), getDataInCase("100%"));
        Assert.assertEquals(
                selenium.getText(JobDetails.DETAILED_STATISTICS_TABLE
                        + "/tr[2]/td[4]"), getDataInCase("95%99%"));
        Assert.assertEquals(
                selenium.getText(JobDetails.DETAILED_STATISTICS_TABLE
                        + "/tr[2]/td[5]"), getDataInCase("85%94%"));
        Assert.assertEquals(
                selenium.getText(JobDetails.DETAILED_STATISTICS_TABLE
                        + "/tr[2]/td[6]"), getDataInCase("75%84%"));
        Assert.assertEquals(
                selenium.getText(JobDetails.DETAILED_STATISTICS_TABLE
                        + "/tr[2]/td[7]"), getDataInCase("50%74%"));
        Assert.assertEquals(
                selenium.getText(JobDetails.DETAILED_STATISTICS_TABLE
                        + "/tr[2]/td[8]"), getDataInCase("nomatch"));
        Assert.assertEquals(
                selenium.getText(JobDetails.DETAILED_STATISTICS_TABLE
                        + "/tr[2]/td[9]"), getDataInCase("repetitions"));
        Assert.assertEquals(
                selenium.getText(JobDetails.DETAILED_STATISTICS_TABLE
                        + "/tr[2]/td[10]"), getDataInCase("incontextMathes"));

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
