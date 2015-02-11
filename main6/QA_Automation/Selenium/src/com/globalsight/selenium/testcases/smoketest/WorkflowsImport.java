package com.globalsight.selenium.testcases.smoketest;

import org.testng.annotations.Test;
import com.globalsight.selenium.functions.WorkflowsFuncs;
import com.globalsight.selenium.pages.MainFrame;
import com.globalsight.selenium.testcases.ConfigUtil;
import com.globalsight.selenium.testcases.BaseTestCase;

/*
 * TestCaseName: WorkflowsImport.java
 * Author:Jester
 * Tests:importWorkflow()
 * 
 * History:
 * Date       Comments       Updater
 * 2011-6-28  First Version  Jester
 */
public class WorkflowsImport extends BaseTestCase
{
    /*
     * Common variables initialization.
     */
    private WorkflowsFuncs workflowsFuncs = new WorkflowsFuncs();

    @Test
    public void importWorkflow() throws Exception
    {
        openMenuItemAndWait(selenium, MainFrame.SETUP_MENU,
                MainFrame.WORKFLOWS_SUBMENU);

        String filePath = ConfigUtil.getConfigData("Base_Path")
                + getProperty("workflow.import.file");

        workflowsFuncs.importWorkFlow(selenium, filePath,
                getProperty("workflow.import"),
                getProperty("workflow.import.verify"));
    }
}
