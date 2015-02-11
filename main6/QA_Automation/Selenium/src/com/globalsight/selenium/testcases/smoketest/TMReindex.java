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

public class TMReindex extends BaseTestCase
{   
    private Selenium selenium;
    TMFuncs iTMFuncs = new TMFuncs();
    CreateTM t = new CreateTM();
    String tmname = ConfigUtil.getDataInCase(t.getClassName(), "COMMNTM");
    @Test
    public void verifyReindex() throws Exception
    {
        selenium.click(MainFrame.Setup_MENU);
        selenium.click(MainFrame.TranslationMemory_SUBMENU);
        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
        
        iTMFuncs.selectRadioButtonFromTable(selenium, TMManagement.TMMangement_TABLE, tmname);
        
        selenium.click(TMManagement.Reindex_BUTTON);
        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
        selenium.click(TMManagement.SelectedTM_RADIO);
        selenium.click(TMManagement.Next_BUTTON_REINDEX);
        selenium.waitForCondition("var imsg=selenium.getText(\""+TMManagement.ReindexProgress_MSG+"\"); imsg==\"41 entries (100%)\"",CommonFuncs.SHORT_WAIT);
        Assert.assertEquals(selenium.getText(TMManagement.ReindexMessages_MSG), "Indexing has successfully finished.");
        selenium.click(TMManagement.OK_BUTTON_REINDEX);
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
