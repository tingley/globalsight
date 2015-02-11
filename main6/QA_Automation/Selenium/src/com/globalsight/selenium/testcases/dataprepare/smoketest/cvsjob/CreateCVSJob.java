package com.globalsight.selenium.testcases.dataprepare.smoketest.cvsjob;

import org.testng.annotations.Test;

import com.globalsight.selenium.functions.CVSFuncs;
import com.globalsight.selenium.functions.CommonFuncs;
import com.globalsight.selenium.functions.JobActivityOperationFuncs;
import com.globalsight.selenium.pages.MainFrame;
import com.globalsight.selenium.testcases.BaseTestCase;

/**
 * Create CVS JOB and complete the job, prepare data for CVSJob
 * 
 * @author leon
 * 
 */
public class CreateCVSJob extends BaseTestCase
{
    private JobActivityOperationFuncs jobActivityOperationFuncs = new JobActivityOperationFuncs();
    private String jobName = "CVSJob001";
    private String targetLocale = "French (France) [fr_FR]";

    private CVSFuncs cvsFunc = new CVSFuncs();

    /**
     * Create cvs jobs(7 different jobs according to ,mappings)
     * @throws InterruptedException 
     */
    @Test
    public void createJob() throws InterruptedException
    {
        String sourceLocale = "English (United States) [en_US]";
        String project = "Template";
        String cvsModule = "CVSServer-CVSTest-module01";
        cvsFunc.createCVSJob(selenium, jobName, sourceLocale, project,
                cvsModule, targetLocale);
        // Wait until the job created successful
        Thread.sleep((long) 50000);

        // complete the job
        completeJob();

        Thread.sleep((long) 50000);
    }

    private void completeJob()
    {
        String[] workflows = { targetLocale };
        CommonFuncs.loginSystemWithAdmin(selenium);
        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
        jobActivityOperationFuncs.dispatchJob(selenium, jobName, workflows);
        selenium.click(MainFrame.LOG_OUT_LINK);
        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
        CommonFuncs.loginSystemWithAnyone(selenium);
        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
        jobActivityOperationFuncs.acceptActivity(selenium, jobName);
        jobActivityOperationFuncs.completeActivity(selenium, jobName);
        jobActivityOperationFuncs.acceptActivity(selenium, jobName);
        jobActivityOperationFuncs.completeActivity(selenium, jobName);
        selenium.click(MainFrame.LOG_OUT_LINK);
        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
    }
}
