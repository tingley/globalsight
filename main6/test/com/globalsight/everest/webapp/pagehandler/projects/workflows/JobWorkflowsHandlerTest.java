package com.globalsight.everest.webapp.pagehandler.projects.workflows;

import java.lang.reflect.Method;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class JobWorkflowsHandlerTest
{
    private JobWorkflowsHandler handler = new JobWorkflowsHandler();

    @Before
    public void setUp() throws Exception
    {
    }

    @After
    public void tearDown() throws Exception
    {
    }

    @Test
    public void testGetTmUpdatingTargetPageSql() throws Exception
    {
        Method method = handler.getClass().getDeclaredMethod("getTmUpdatingTargetPageSql",
                long.class);
        method.setAccessible(true);

        Object resultObject = method.invoke(handler, 1468);
        String expected = "SELECT COUNT(*) FROM target_page WHERE WORKFLOW_IFLOW_INSTANCE_ID = 1468 AND state = 'EXPORTED' AND EXPORTED_SUB_STATE = 2";
        Assert.assertEquals(expected, resultObject);
    }

    @Test
    public void testGetDisplayState2() throws Exception
    {
        Method method = handler.getClass().getDeclaredMethod("getDisplayState2", String.class,
                String.class, String.class, int.class);
        method.setAccessible(true);

        String wfState = "EXPORTED";
        String displayState = "Exported";
        String tmUpdatingMsg = "(Files Available | TM Updating)";
        int populatingTmTrgPageNum = 3;
        Object res = method.invoke(handler, wfState, displayState, tmUpdatingMsg,
                populatingTmTrgPageNum);
        Assert.assertEquals("Exported (Files Available | TM Updating)", res);

        populatingTmTrgPageNum = 0;
        res = method.invoke(handler, wfState, displayState, tmUpdatingMsg, populatingTmTrgPageNum);
        Assert.assertEquals("Exported", res);

        wfState = "DISPATCHED";
        displayState = "Dispatched";
        res = method.invoke(handler, wfState, displayState, tmUpdatingMsg, populatingTmTrgPageNum);
        Assert.assertEquals("Dispatched", res);
    }
}
