package com.globalsight.selenium.testcases.smoketest;


import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import com.globalsight.selenium.functions.CommonFuncs;
import com.globalsight.selenium.functions.WorkflowsFuncs;
import com.globalsight.selenium.pages.MainFrame;
import com.globalsight.selenium.properties.ConfigUtil;
import com.globalsight.selenium.testcases.BaseTestCase;
import com.thoughtworks.selenium.Selenium;

public class WorkflowDuplicate extends BaseTestCase
{
    private Selenium selenium;
    private WorkflowsFuncs iWorkflowsFuncs = new WorkflowsFuncs();
    @Test
    public void duplicateWorkflow() throws Exception {
        selenium.click(MainFrame.Setup_MENU);
        selenium.click(MainFrame.Workflows_SUBMENU);
        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
        iWorkflowsFuncs.duplicateWorkFlow(selenium, ConfigUtil.getDataInCase(getClassName(), "DUPLICATEPROFILE"), ConfigUtil.getDataInCase(getClassName(), "workflowTemplate"));
    }
    @BeforeMethod
    public void beforeMethod() {
        CommonFuncs.loginSystemWithAdmin(selenium);
    }

    @AfterMethod
    public void afterMethod() {
        CommonFuncs.logoutSystem(selenium);
    }

    @BeforeTest
    public void beforeTest() {
        selenium=CommonFuncs.initSelenium();
    }

    @AfterTest
    public void afterTest() {
        CommonFuncs.endSelenium(selenium);
        
    }
}
