package com.globalsight.selenium.testcases.smoketest;

import junit.framework.Assert;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.globalsight.selenium.functions.BasicFuncs;
import com.globalsight.selenium.functions.CommonFuncs;
import com.globalsight.selenium.pages.JobDetails;
import com.globalsight.selenium.pages.MainFrame;
import com.globalsight.selenium.pages.MyActivities;
import com.globalsight.selenium.pages.MyJobs;
import com.globalsight.selenium.testcases.BaseTestCase;
import com.globalsight.selenium.testcases.dataprepare.smoketest.job.CreateJobs;
import com.globalsight.selenium.testcases.dataprepare.smoketest.job.CreatedJob;

public class FileFormatVerify extends BaseTestCase
{
    BasicFuncs basicFuncs = new BasicFuncs();
    CreateJobs createJobs = new CreateJobs();

    @Test
    public void fileFormatDispatch() throws Exception
    {
        openMenuItemAndWait(selenium, MainFrame.MY_JOBS_MENU,
                MainFrame.MY_JOBS_READY_SUBMENU);

        String jobName = getDataInCase(createJobs.getClassName(), "jobName5");
        jobName = CreatedJob.getCreatedJobName(jobName);
        clickAndWait(selenium, "link=" + jobName);
        selenium.click(MyJobs.CHECK_ALL_LINK);
        clickAndWait(selenium, JobDetails.DISPATCH_BUTTON);

        CommonFuncs.logoutSystem(selenium);
        CommonFuncs.loginSystemWithAnyone(selenium);

        openMenuItemAndWait(selenium, MainFrame.MY_ACTIVITIES_MENU,
                MainFrame.MY_ACTIVITIES_AVAILABLE_SUBMENU);

        selenium.click("link=" + jobName);
        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);

        clickAndWait(selenium, MyActivities.ACCEPT_JOB_BUTTON);
        selenium.click(MyActivities.TASK_COMPLETED_BUTTON);
        if (selenium.isConfirmationPresent())
        {
            selenium.getConfirmation();
        }

        openMenuItemAndWait(selenium, MainFrame.MY_ACTIVITIES_MENU,
                MainFrame.MY_ACTIVITIES_FINISHED_SUBMENU);
        Assert.assertEquals(basicFuncs.isPresentInTable(selenium,
                MyActivities.MY_ACTIVITIES_TABLE, CreatedJob.getCreatedJobName("allFormatJob"), 5), true);
    }

    @BeforeMethod
    public void beforeMethod()
    {
        CommonFuncs.loginSystemWithPM(selenium);
    }
}
