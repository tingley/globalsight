package com.globalsight.selenium.testcases.smoketest;

import junit.framework.Assert;

import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.globalsight.selenium.functions.CommonFuncs;
import com.globalsight.selenium.pages.FilterConfiguration;
import com.globalsight.selenium.pages.MainFrame;
import com.thoughtworks.selenium.Selenium;

public class RemoveFilter
{
    private Selenium selenium;

    @Test
    public void removefilter() throws Exception
    {
        selenium.click(MainFrame.DataSources_MENU);
        selenium.click(MainFrame.FilterConfiguration_SUBMENU);
        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
        selenium.click(FilterConfiguration.ExpnadAll_CHECKBOX);
        selenium.uncheck(FilterConfiguration.CheckAll_CHECKBOX);
        selenium.click(FilterConfiguration.CheckAll_CHECKBOX);
        selenium.click(FilterConfiguration.Remove_BUTTON);
        Assert.assertTrue(selenium
                .getConfirmation()
                .matches(
                        "^By clicking the 'OK' button, this filter will be deleted forever\\. Are you sure to continue[\\s\\S]$"));
        Assert.assertEquals(selenium.getAlert(),
                "The filter has been deleted sucessfully.");
        // if (selenium.isAlertPresent())
        // selenium.getAlert();
    }

    @BeforeMethod
    public void beforeMethod()
    {
        CommonFuncs.loginSystemWithAdmin(selenium);

    }

    @AfterMethod
    public void afterMethod()
    {
        selenium.click(MainFrame.LogOut_LINK);
    }

    @BeforeClass
    public void beforeClass()
    {
    }

    @AfterClass
    public void afterClass()
    {
    }

    @BeforeTest
    public void beforeTest()
    {
        selenium = CommonFuncs.initSelenium();
    }

    @AfterTest
    public void afterTest()
    {
        CommonFuncs.endSelenium(selenium);
    }
}
