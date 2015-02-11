package com.globalsight.selenium.testcases.smoketest;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.globalsight.selenium.functions.CommonFuncs;
import com.globalsight.selenium.functions.LocalizationFuncs;
import com.globalsight.selenium.pages.MainFrame;
import com.globalsight.selenium.testcases.BaseTestCase;
import com.thoughtworks.selenium.Selenium;

public class CreateLocalization extends BaseTestCase
{

    private Selenium selenium;
    private LocalizationFuncs funcs;
    
    @BeforeClass
    public void beforeClass()
    {
        selenium = CommonFuncs.initSelenium();
        funcs = new LocalizationFuncs();
    }
    
    @AfterClass
    public void afterClass()
    {
        funcs = null;
        selenium.stop();
    }
    
    @BeforeMethod
    public void BeforeMethod()
    {
        try
        {
            navigateToLocalization();
            if (!selenium.getTitle().equals("Terminology Management"))
            {
                CommonFuncs.loginSystemWithAdmin(selenium);
                navigateToLocalization();
            }
        }
        catch (Exception e)
        {
            CommonFuncs.loginSystemWithAdmin(selenium);
            navigateToLocalization();
        }
    }
    
    @Test
    public void create()
    {
        funcs.create(selenium, getClassName());
    }
    
    private void navigateToLocalization()
    {
        selenium.click(MainFrame.Setup_MENU);
        selenium.click(MainFrame.LocalizationProfiles_SUBMENU);
        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
    }
}
