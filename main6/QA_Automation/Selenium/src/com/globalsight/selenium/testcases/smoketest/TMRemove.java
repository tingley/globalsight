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

public class TMRemove extends BaseTestCase
{
    private Selenium selenium;
    TMFuncs iTMFuncs = new TMFuncs();
    CreateTM t = new CreateTM();
    String rname = ConfigUtil.getDataInCase(t.getClassName(), "DUPLICATENAME");
    @Test
    public void verifyRemove() throws Exception
    {
        selenium.click(MainFrame.Setup_MENU);
        selenium.click(MainFrame.TranslationMemory_SUBMENU);
        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
        
        iTMFuncs.selectRadioButtonFromTable(selenium, TMManagement.TMMangement_TABLE, rname);
        
        selenium.click(TMManagement.Remove_BUTTON);
        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
        selenium.click(TMManagement.EntrieTM_RADIO);
        selenium.click(TMManagement.OK_BUTTON_REMOVE);
        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
        
//      selenium.waitForCondition("var imsg=selenium.getText(\""+TMManagement.RemoveProgress_MSG+"\"); imsg==\"100% /\"",CommonFuncs.SHORT_WAIT);
//      Assert.assertEquals(selenium.getText(TMManagement.RemoveMessages_MSG), ConfigUtil.getDataInCase(getClassName(), "REMOVEVERIFY"));
        Thread.sleep(15000);
        selenium.click(TMManagement.OK_BUTTON_REMOVE2);
        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);     
        Assert.assertFalse(iTMFuncs.isPresentInTable(selenium, TMManagement.TMMangement_TABLE, rname));
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
