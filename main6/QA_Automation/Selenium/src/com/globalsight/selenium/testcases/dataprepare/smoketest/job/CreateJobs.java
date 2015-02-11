package com.globalsight.selenium.testcases.dataprepare.smoketest.job;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import jodd.util.StringUtil;

import org.testng.annotations.Test;

import com.globalsight.selenium.functions.CreateJobsFuncs;
import com.globalsight.selenium.testcases.BaseTestCase;

/**
 * Create job the smoke test needed
 * 
 * @author leon
 * 
 */
public class CreateJobs extends BaseTestCase
{

    private String jobName;
    private String jobFiles;
    private String jobFileProfiles;
    private String jobTargetLocales;
    
    @Test
    public void createJobs() throws InterruptedException
    {
        CreateJobsFuncs createJobsFunc = new CreateJobsFuncs();
		for (int i = 1; i < 6; i++)
        {
            jobName = getDataInCase("jobName" + i);

            if (StringUtil.isEmpty(jobName))
            	continue;
            
			// Generate unique job name
			jobFiles = getDataInCase("jobFiles" + i);
			jobFileProfiles = getDataInCase("jobFileProfiles" + i);
			jobTargetLocales = getDataInCase("jobTargetLocales" + i);

			createJobsFunc.createJob(jobName, jobFiles, jobFileProfiles, jobTargetLocales);
        }
    }
}
