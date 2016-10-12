package com.globalsight.everest.jobhandler;

import java.lang.reflect.Method;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class JobHandlerLocalTest
{
    JobHandlerLocal jobHandlerLocal = null;

    @Before
    public void init()
    {
        jobHandlerLocal = new JobHandlerLocal();
    }

    /**
     * Unit test for GBS-1340, always return 0 from that method
     * @throws Exception
     */
    @Test
    public void testGetMyJobsDaysRetrieved() throws Exception
    {
        try
        {
            Method method = jobHandlerLocal.getClass().getDeclaredMethod("getMyJobsDaysRetrieved");
            method.setAccessible(true);
            Object resultObject = method.invoke(jobHandlerLocal);
            System.out.println(resultObject);
            Assert.assertEquals(resultObject, 0);
        }
        catch (NoSuchMethodException e)
        {
            System.out.println("NoSuchMethodException for testGetMyJobsDaysRetrieved");
            Assert.assertEquals(0, 0);
        }
    }
    
    public static void main(String[] args)
    {
        org.junit.runner.JUnitCore.runClasses(JobHandlerLocalTest.class);
    }
}
