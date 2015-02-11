package com.globalsight.selenium.testcases.dataprepare.smoketest.cvsjob;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.globalsight.selenium.functions.CVSFuncs;
import com.globalsight.selenium.functions.CommonFuncs;
import com.globalsight.selenium.functions.JobActivityOperationFuncs;
import com.globalsight.selenium.properties.ConfigUtil;
import com.thoughtworks.selenium.Selenium;

/**
 * Create CVS JOB and complete the job, prepare data for CVSJob
 * 
 * @author leon
 * 
 */
public class CreateCVSJob
{
    private JobActivityOperationFuncs jobActivityOperationFuncs = new JobActivityOperationFuncs();
    private String jobName = "CVSJob001";
    private String targetLocale = "French (France) [fr_FR]";

    private Selenium selenium;
    private CVSFuncs cvsFunc;

    @BeforeClass
    public void beforeClass()
    {
        selenium = CommonFuncs.initSelenium();
        CommonFuncs.loginSystemWithAdmin(selenium);
        cvsFunc = new CVSFuncs();
    }

    @AfterClass
    public void afterClass()
    {
        selenium.stop();
    }

    /**
     * Create cvs jobs(7 different jobs according to ,mappings)
     */
    @Test
    public void createJob()
    {
        String sourceLocale = "English (United States) [en_US]";
        String project = "Template";
        String cvsModule = "CVSServer-CVSTest-module01";
        cvsFunc.createCVSJob(selenium, jobName, sourceLocale, project,
                cvsModule, targetLocale);
        // Wait until the job created successful
        try
        {
            Thread.sleep((long) 50000);
        }
        catch (InterruptedException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        // complete the job
        completeJob();
        
        try
        {
            Thread.sleep((long) 50000);
        }
        catch (InterruptedException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void completeJob()
    {
        String admin = ConfigUtil.getConfigData("admin_login_name");
        String anyone = ConfigUtil.getConfigData("anyone_login_name");
        String[] workflows =
        { targetLocale };
        jobActivityOperationFuncs.dispatchJob(selenium, admin, jobName,
                workflows);
        jobActivityOperationFuncs.acceptActivity(selenium, anyone, jobName);
        jobActivityOperationFuncs.completeActivity(selenium, anyone, jobName);
        jobActivityOperationFuncs.acceptActivity(selenium, anyone, jobName);
        jobActivityOperationFuncs.completeActivity(selenium, anyone, jobName);
    }
}
