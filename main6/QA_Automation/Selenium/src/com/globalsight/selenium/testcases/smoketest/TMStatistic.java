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

public class TMStatistic extends BaseTestCase
{
    private Selenium selenium;
    TMFuncs iTMFuncs = new TMFuncs();
    CreateTM t = new CreateTM();
    
    @Test
    public void getStaticsTM() throws Exception
    {
        selenium.click(MainFrame.Setup_MENU);
        selenium.click(MainFrame.TranslationMemory_SUBMENU);
        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
        String tmName = ConfigUtil.getDataInCase(t.getClassName(), "COMMNTM");
        iTMFuncs.statistic(selenium, tmName);
        Assert.assertEquals(selenium.getText(TMManagement.TotalTUs_TEXT_FIELD), "41");
        Assert.assertEquals(selenium.getText(TMManagement.TotalTUVs_TEXT_FIELD), "119");
        selenium.click(TMManagement.Close_BUTTON);
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
