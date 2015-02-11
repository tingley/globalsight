package com.globalsight.selenium.testcases.smoketest;

import org.testng.annotations.Test;

import com.globalsight.selenium.functions.WorkflowsFuncs;
import com.globalsight.selenium.pages.MainFrame;
import com.globalsight.selenium.testcases.BaseTestCase;

public class WorkflowExport extends BaseTestCase
{
    private WorkflowsFuncs workflowsFuncs = new WorkflowsFuncs();

    @Test
    public void exportWorkflow() throws Exception
    {
        openMenuItemAndWait(selenium, MainFrame.SETUP_MENU,
                MainFrame.WORKFLOWS_SUBMENU);

        workflowsFuncs.exportWorkflow(selenium,
                getProperty("workflow.export.workflowTemplate"));
    }
}
