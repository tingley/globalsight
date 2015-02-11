package com.globalsight.selenium.testcases.smoketest;

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

public class TMMaintenance extends BaseTestCase
{
    private Selenium selenium;
    TMFuncs iTMFuncs = new TMFuncs();
    CreateTM t = new CreateTM();
    
    @Test
    public void verifyMaintenance() throws Exception
    {
        selenium.click(MainFrame.Setup_MENU);
        selenium.click(MainFrame.TranslationMemory_SUBMENU);
        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
        
        iTMFuncs.selectRadioButtonFromTable(selenium, TMManagement.TMMangement_TABLE, ConfigUtil.getDataInCase(t.getClassName(), "COMMNTM"));
        
        selenium.click(TMManagement.Maintenance_BUTTON);
        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
        selenium.type(TMManagement.FIND_TEXTFIELD, ConfigUtil.getDataInCase(t.getClassName(), "MAINTENANCESEARCH"));
        selenium.select(TMManagement.InSourceLocale_SELECT, ConfigUtil.getDataInCase(t.getClassName(), "MAINTENANCESOURCE"));
        selenium.select(TMManagement.ShowTargetLocale_SELECT,ConfigUtil.getDataInCase(t.getClassName(), "MAINTENANCETARGET"));
        selenium.click(TMManagement.Next_BUTTON);
        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
        Thread.sleep(15000);
        selenium.click(TMManagement.Continue_BUTTON);
        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
        
        selenium.click(TMManagement.Cancel_BUTTON_TOP);
        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
        
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
