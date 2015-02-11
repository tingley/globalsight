package com.globalsight.selenium.testcases.dataprepare.smoketest.job;

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

    @Test
    public void createJobs()
    {
        CreateJobsFuncs tmp = new CreateJobsFuncs();
        tmp.create(getClassName());
        try
        {
            Thread.sleep(30000);
        }
        catch (InterruptedException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
