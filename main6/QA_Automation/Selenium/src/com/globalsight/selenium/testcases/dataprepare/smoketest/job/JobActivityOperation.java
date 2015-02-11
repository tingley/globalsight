package com.globalsight.selenium.testcases.dataprepare.smoketest.job;

import org.testng.annotations.Test;

import com.globalsight.selenium.functions.CommonFuncs;
import com.globalsight.selenium.functions.JobActivityOperationFuncs;
import com.globalsight.selenium.testcases.BaseTestCase;

/**
 * Dispatch job or accept activity as need
 * 
 * @author leon
 * 
 */
public class JobActivityOperation extends BaseTestCase
{
    private JobActivityOperationFuncs jobActivityOperationFuncs = new JobActivityOperationFuncs();

    @Test
    public void jobDispatch()
    {
        // Dispatch
        int i = 0;
        while (i > -1)
        {
            i++;
            String jobName = getDataInCase("jobToDispatch" + i);
            if (jobName == null)
                break;
            
			jobName = CreatedJob.getCreatedJobName(jobName);
            String[] workflows = getDataInCase("jobToDispatchWorkflow" + i)
                    .split(",");
            jobActivityOperationFuncs.dispatchJob(selenium, jobName, workflows);
        }

        // Accept
        CommonFuncs.loginSystemWithAnyone(selenium);
        i = 0;
        while (i > -1)
        {
            i++;

            String activityName = getDataInCase("activityToAccept" + i);
            if (activityName == null)
                break;
            
			activityName = CreatedJob.getCreatedJobName(activityName);
            jobActivityOperationFuncs.acceptActivity(selenium, activityName);
        }
    }

}