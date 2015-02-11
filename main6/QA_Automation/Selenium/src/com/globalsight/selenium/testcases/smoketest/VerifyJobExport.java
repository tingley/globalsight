package com.globalsight.selenium.testcases.smoketest;

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
 * TestCaseName: VerifyJobExport.java
 * Author:Jester
 * Tests:verifyJobExport()
 * 
 * History:
 * Date       Comments       Updater
 * 2011-6-30  First Version  Jester
 */

public class VerifyJobExport extends BaseTestCase
{
    /*
     * Common Variables.
     */
    BasicFuncs basicFuncs = new BasicFuncs();
    CreateJobs createJobs = new CreateJobs();

    @Test
    public void verifyJobExport() throws Exception
    {
        openMenuItemAndWait(selenium, MainFrame.MY_JOBS_MENU,
                MainFrame.MY_JOBS_INPROGRESS_SUBMENU);

        String jobName = ConfigUtil.getDataInCase(createJobs.getClassName(),
                "jobName1");
        jobName = CreatedJob.getCreatedJobName(jobName);
        clickAndWait(selenium, "link=" + jobName);

        basicFuncs.selectRadioButtonFromTable(selenium,
                JobDetails.WORKFLOWS_TABLE, getDataInCase("workflow"));

        clickAndWait(selenium, JobDetails.EXPORT_BUTTON);

        clickAndWait(selenium, JobDetails.EXPORT_BUTTON);
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
