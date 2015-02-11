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

public class TMCorpusBrowser extends BaseTestCase
{   
    private Selenium selenium;
    TMFuncs iTMFuncs = new TMFuncs();
    CreateTM t = new CreateTM();
    
    @Test
    public void verifyCorpusBrowser() throws Exception
    {
        selenium.click(MainFrame.Setup_MENU);
        selenium.click(MainFrame.TranslationMemory_SUBMENU);
        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
        
        iTMFuncs.selectRadioButtonFromTable(selenium, TMManagement.TMMangement_TABLE, ConfigUtil.getDataInCase(t.getClassName(), "COMMNTM"));
        
        selenium.click(TMManagement.CorpusBrowser_BUTTON);
        selenium.waitForPopUp(TMManagement.CorpusBrowser_TAG, CommonFuncs.SHORT_WAIT);
        
        selenium.selectWindow("name="+TMManagement.CorpusBrowser_TAG);
        selenium.click(TMManagement.FullText_RADIO);
        selenium.type(TMManagement.Search_TEXT_FIELD, ConfigUtil.getDataInCase(t.getClassName(), "CORPUSSEARCH"));
        selenium.select(TMManagement.LocalePair_SELECT,ConfigUtil.getDataInCase(t.getClassName(), "CORPUSLOCALE"));
        selenium.addSelection(TMManagement.TranslationMemory_SELECTION, ConfigUtil.getDataInCase(t.getClassName(), "COMMNTM"));
        
        selenium.click(TMManagement.Search_BUTTON);
        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
        
        Assert.assertEquals(selenium.getText(TMManagement.StatusMessageTop_TEXT), ConfigUtil.getDataInCase(t.getClassName(), "CORPUSVERIFY"));
        selenium.click(TMManagement.Close_BUTTON_CORPUSBROWSER);
        selenium.selectWindow(null);
        
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
