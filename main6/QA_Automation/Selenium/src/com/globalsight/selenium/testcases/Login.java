package com.globalsight.selenium.testcases;

/*
 * TestCaseName: Login.java
 * Author:Jester
 * Tests:AdminLogin();AnyOneLogin();PMLogin()
 * 
 * History:
 * Date       Comments       Updater
 * 2011-5-13  First Version  Jester
 */

import junit.framework.Assert;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.globalsight.selenium.functions.CommonFuncs;
import com.globalsight.selenium.pages.MainFrame;
import com.thoughtworks.selenium.Selenium;

public class Login
{
    /**
     * Common variables initialization.
     */
    private Selenium selenium;

    /**
     * Log in with an administrator account and then log out.
     */
    @Test
    public void AdminLogin()
    {
        CommonFuncs.loginSystemWithSuperAdmin(selenium);
        Assert.assertEquals(selenium.isElementPresent(MainFrame.Home_LINK),
                true);
    }

    /**
     * Log in with a common user account and then log out.
     */
    @Test
    public void AnyOneLogin()
    {
        CommonFuncs.loginSystemWithAnyone(selenium);
        Assert.assertEquals(selenium.isElementPresent(MainFrame.Home_LINK),
                true);
    }

    /**
     * Log in with a PM account and then log out.
     */
    @Test
    public void PMLogin()
    {
        CommonFuncs.loginSystemWithAdmin(selenium);
        Assert.assertEquals(selenium.isElementPresent(MainFrame.Home_LINK),
                true);
    }

    @AfterMethod
    public void afterMethod()
    {
        selenium.click(MainFrame.LogOut_LINK);
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
