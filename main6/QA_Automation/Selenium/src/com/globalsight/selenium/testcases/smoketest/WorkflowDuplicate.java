package com.globalsight.selenium.testcases.smoketest;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.globalsight.selenium.functions.BasicFuncs;
import com.globalsight.selenium.functions.WorkflowsFuncs;
import com.globalsight.selenium.pages.MainFrame;
import com.globalsight.selenium.pages.Workflows;
import com.globalsight.selenium.testcases.BaseTestCase;

public class WorkflowDuplicate extends BaseTestCase
{
    private WorkflowsFuncs workflowsFuncs = new WorkflowsFuncs();
    private BasicFuncs basicFuncs = new BasicFuncs();

    @Test
    public void duplicateWorkflow() throws Exception
    {
        openMenuItemAndWait(selenium, MainFrame.SETUP_MENU,
                MainFrame.WORKFLOWS_SUBMENU);

        workflowsFuncs.duplicateWorkFlow(selenium,
                getProperty("workflow.duplicate.prefix"),
                getProperty("workflow.export.workflowTemplate"));

        Assert.assertTrue(basicFuncs.isPresentInTable(selenium,
                Workflows.Workflows_TABLE,
                getProperty("workflow.duplicate.newName")));
    }
}
