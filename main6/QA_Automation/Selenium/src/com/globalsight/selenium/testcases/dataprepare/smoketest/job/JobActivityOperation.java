package com.globalsight.selenium.testcases.dataprepare.smoketest.job;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.globalsight.selenium.functions.CommonFuncs;
import com.globalsight.selenium.functions.JobActivityOperationFuncs;
import com.globalsight.selenium.properties.ConfigUtil;
import com.thoughtworks.selenium.Selenium;

/**
 * Dispatch job or accept activity as need
 * 
 * @author leon
 * 
 */
public class JobActivityOperation
{
    private JobActivityOperationFuncs jobActivityOperationFuncs = new JobActivityOperationFuncs();
    private Selenium selenium;

    @BeforeClass
    public void beforeClass()
    {
        selenium = CommonFuncs.initSelenium();
    }

    @AfterClass
    public void afterClass()
    {
        selenium.stop();
    }

    @Test
    public void jobDispatch()
    {
        // Dispatch
        int i = 0;
        while (i > -1)
        {
            i++;
            String jobName = ConfigUtil.getDataInCase(getClass().getName(),
                    "jobToDispatch" + i);
            if (jobName == null)
            {
                break;
            }
            String[] workflows = ConfigUtil.getDataInCase(getClass().getName(),
                    "jobToDispatchWorkflow" + i).split(",");
            jobActivityOperationFuncs.dispatchJob(selenium,
                    ConfigUtil.getConfigData("admin_login_name"), jobName,
                    workflows);
        }

        // Accept
        i = 0;
        while (i > -1)
        {
            i++;
            String activityName = ConfigUtil.getDataInCase(
                    getClass().getName(), "activityToAccept" + i);
            if (activityName == null)
            {
                break;
            }
            jobActivityOperationFuncs
                    .acceptActivity(selenium,
                            ConfigUtil.getConfigData("anyone_login_name"),
                            activityName);
        }
    }

}