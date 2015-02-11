package com.globalsight.selenium.testcases.smoketest;

import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.globalsight.selenium.functions.CommonFuncs;
import com.globalsight.selenium.functions.TerminologyFuncs;
import com.globalsight.selenium.pages.MainFrame;
import com.globalsight.selenium.properties.ConfigUtil;
import com.thoughtworks.selenium.Selenium;

public class TerminologyExport
{
    private Selenium selenium;
    TerminologyFuncs term = new TerminologyFuncs();
    
    @Test
    public void export() throws Exception
    {  
        selenium.click(MainFrame.Setup_MENU);
        selenium.click(MainFrame.Terminology_SUBMENU);
        String className = getClass().getName();
        String exportTermbaseName = ConfigUtil.getDataInCase(className, "export_termbase_name");
        if (exportTermbaseName != null && exportTermbaseName.length() != 0)
        {
            String[] nameArray = exportTermbaseName.split("\\|");
            for (String name : nameArray)
            {
                term.export(selenium, name);
            }
        }
    }
    @BeforeMethod
    public void beforeMethod() {
        CommonFuncs.loginSystemWithAdmin(selenium);
    }

    @AfterMethod
    public void afterMethod() {
        CommonFuncs.logoutSystem(selenium);
    }

    @BeforeClass
    public void beforeClass() {
    }

    @AfterClass
    public void afterClass() {
    }

    @BeforeTest
    public void beforeTest() {
        selenium = CommonFuncs.initSelenium();
    }

    @AfterTest
    public void afterTest() {
        CommonFuncs.endSelenium(selenium);
    
    }

    @BeforeSuite
    public void beforeSuite() {
    }

    @AfterSuite
    public void afterSuite() {
    }
}
