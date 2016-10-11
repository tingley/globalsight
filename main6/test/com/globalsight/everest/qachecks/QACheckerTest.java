package com.globalsight.everest.qachecks;

import java.lang.reflect.Method;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.globalsight.everest.tuv.TuvState;
import com.globalsight.everest.webapp.pagehandler.administration.reports.ReportConstants;


public class QACheckerTest
{
    private QAChecker checker = new QAChecker();
    
    @Before
    public void setUp() throws Exception
    {
    }

    @After
    public void tearDown() throws Exception
    {
    }
    
    @Test
    public void testGetState() throws Exception
    {
        Method method = checker.getClass().getDeclaredMethod("getState", String.class);
        method.setAccessible(true);
        Object resultObject = method.invoke(checker, TuvState.APPROVED.getName());
        String expected = ReportConstants.SOURCE_EQUAL_TARGET_TRANSLATED_OR_APPROVED;
        Assert.assertEquals(expected, resultObject);
    }
    
}
