package com.globalsight.everest.qachecks;

import java.lang.reflect.Method;
import java.util.ArrayList;

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
    public void testGetState1() throws Exception
    {
        ArrayList<String> tuvStateNames = new ArrayList<String>();
        tuvStateNames.add(TuvState.APPROVED.getName());
        tuvStateNames.add(TuvState.LOCALIZED.getName());
        Method method = checker.getClass().getDeclaredMethod("getState", String.class);
        method.setAccessible(true);
        for (String tuvStateName : tuvStateNames)
        {
            Object resultObject = method.invoke(checker, tuvStateName);
            String expected = ReportConstants.SOURCE_EQUAL_TARGET_TRANSLATED_OR_APPROVED;
            Assert.assertEquals(expected, resultObject);
        }
    }
    
    @Test
    public void testGetState2() throws Exception
    {
        Method method = checker.getClass().getDeclaredMethod("getState", String.class);
        method.setAccessible(true);
        Object resultObject = method.invoke(checker, TuvState.NOT_LOCALIZED.getName());
        String expected = ReportConstants.SOURCE_EQUAL_TARGET_UNTRANSLATED;
        Assert.assertEquals(expected, resultObject);
    }
    
    @Test
    public void testGetState3() throws Exception
    {
        Method method = checker.getClass().getDeclaredMethod("getState", String.class);
        method.setAccessible(true);
        Object resultObject = method.invoke(checker, TuvState.EXACT_MATCH_LOCALIZED.getName());
        String expected = ReportConstants.SOURCE_EQUAL_TARGET_EXACT_MATCH;
        Assert.assertEquals(expected, resultObject);
    }
    
    @Test
    public void testGetState4() throws Exception
    {
        Method method = checker.getClass().getDeclaredMethod("getState", String.class);
        method.setAccessible(true);
        Object resultObject = method.invoke(checker, TuvState.DO_NOT_TRANSLATE.getName());
        String expected = ReportConstants.SOURCE_EQUAL_TARGET_DO_NOT_TRANSLATE;
        Assert.assertEquals(expected, resultObject);
    }
    
    @Test
    public void testGetState5() throws Exception
    {
        Method method = checker.getClass().getDeclaredMethod("getState", String.class);
        method.setAccessible(true);
        Object resultObject = method.invoke(checker, TuvState.OUT_OF_DATE.getName());
        String expected = "";
        Assert.assertEquals(expected, resultObject);
    }
}
