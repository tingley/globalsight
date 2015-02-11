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
import org.testng.annotations.Test;

import com.globalsight.selenium.functions.CommonFuncs;
import com.globalsight.selenium.pages.MainFrame;

public class Login extends BaseTestCase
{
    /**
     * Common variables initialization.
     */

    /**
     * Log in with an administrator account and then log out.
     */
    @Test
    public void AdminLogin()
    {
        CommonFuncs.loginSystemWithSuperAdmin(selenium);
        Assert.assertEquals(selenium.isElementPresent(MainFrame.HOME_LINK),
                true);
    }

    /**
     * Log in with a common user account and then log out.
     */
    @Test
    public void AnyOneLogin()
    {
        CommonFuncs.loginSystemWithAnyone(selenium);
        Assert.assertEquals(selenium.isElementPresent(MainFrame.HOME_LINK),
                true);
    }

    /**
     * Log in with a PM account and then log out.
     */
    @Test
    public void PMLogin()
    {
        CommonFuncs.loginSystemWithAdmin(selenium);
        Assert.assertEquals(selenium.isElementPresent(MainFrame.HOME_LINK),
                true);
    }

    @AfterMethod
    public void afterMethod()
    {
        selenium.click(MainFrame.LOG_OUT_LINK);
    }

}
