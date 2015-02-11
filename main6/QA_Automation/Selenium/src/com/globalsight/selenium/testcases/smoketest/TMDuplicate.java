package com.globalsight.selenium.testcases.smoketest;

import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.globalsight.selenium.functions.CommonFuncs;
import com.globalsight.selenium.functions.TMFuncs;
import com.globalsight.selenium.pages.MainFrame;
import com.globalsight.selenium.pages.TMManagement;
import com.globalsight.selenium.properties.ConfigUtil;
import com.globalsight.selenium.testcases.BaseTestCase;
import com.thoughtworks.selenium.Selenium;

public class TMDuplicate extends BaseTestCase
{   
    private Selenium selenium;
    TMFuncs iTMFuncs = new TMFuncs();
    CreateTM t = new CreateTM();
    
    @Test 
    public void verifyDuplicate() throws Exception
    {
        selenium.click(MainFrame.Setup_MENU);
        selenium.click(MainFrame.TranslationMemory_SUBMENU);
        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
        
        iTMFuncs.selectRadioButtonFromTable(selenium, TMManagement.TMMangement_TABLE, ConfigUtil.getDataInCase(t.getClassName(), "COMMNTM"));
        
        selenium.click(TMManagement.Duplicate_BUTTON);
        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
        selenium.type(TMManagement.Name_TEXT_FIELD_DUPLICATE, ConfigUtil.getDataInCase(t.getClassName(), "DUPLICATENAME"));
        selenium.click(TMManagement.Ok_BUTTON_DUPLICATE);
        
        if (selenium.isAlertPresent())
        {
            selenium.getAlert();
            selenium.click(TMManagement.Cancel_BUTTON_DUPLICATE);
            selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
        }
        else
        {
            selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
        }
        
        Assert.assertTrue(iTMFuncs.isPresentInTable(selenium, TMManagement.TMMangement_TABLE, ConfigUtil.getDataInCase(t.getClassName(), "DUPLICATENAME")));
    }
    @BeforeMethod
    public void beforeMethod() {CommonFuncs.loginSystemWithAdmin(selenium);
    }

    @AfterMethod
    public void afterMethod() {CommonFuncs.logoutSystem(selenium);
    }
    @BeforeTest
    public void beforeTest() {selenium = CommonFuncs.initSelenium();
    }

    @AfterTest
    public void afterTest() {CommonFuncs.endSelenium(selenium);
    }
}
