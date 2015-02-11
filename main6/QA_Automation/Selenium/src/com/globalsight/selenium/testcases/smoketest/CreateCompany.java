package com.globalsight.selenium.testcases.smoketest;

/*
 * TestCaseName: CreateCompany.java
 * Author:Jester
 * Tests:Create()
 * 
 * History:
 * Date       Comments       Updater
 * 2011-5-23  First Version  Jester
 */

import org.testng.Assert;
import org.testng.annotations.Test;

import com.globalsight.selenium.functions.CommonFuncs;
import com.globalsight.selenium.functions.CompanyFuncs;
import com.globalsight.selenium.pages.MainFrame;
import com.globalsight.selenium.pages.Users;
import com.globalsight.selenium.testcases.ConfigUtil;
import com.globalsight.selenium.testcases.BaseTestCase;

public class CreateCompany extends BaseTestCase
{
    /*
     * Common variables initialization.
     */
    private CompanyFuncs companyFuncs = new CompanyFuncs();

    /*
     * Sign in with superAdmin and create a new company. Verify the company can
     * be created.
     */
    @Test
    public void createSuperAdminCompany() throws Exception
    {
        CommonFuncs.logoutSystem(selenium);
        CommonFuncs.loginSystemWithSuperAdmin(selenium);

        openMenuItemAndWait(selenium, MainFrame.SETUP_MENU,
                MainFrame.COMPANIES_SUBMENU);

        companyFuncs.newCompany(selenium, testCaseName);

        CommonFuncs.logoutSystem(selenium);
    }

    /*
     * Verify three users for the company are available.
     */
    @Test(dependsOnMethods =
    { "createSuperAdminCompany" })
    public void verfiySuperAmdinUsers()
    {
        CommonFuncs.loginSystemWithSuperAdmin(selenium);

        openMenuItemAndWait(selenium, MainFrame.SETUP_MENU,
                MainFrame.USERS_SUBMENU);

        // Do a search with the companyname in the Users page.
        selenium.select(Users.USER_SEARCH_NAME_TYPE_SELECT,
                getDataInCase("nameTypeOption"));
        selenium.select(Users.USER_SEARCH_NAME_OPTION_SELECT, getDataInCase("nameOption"));
        selenium.type(Users.USER_SEARCH_NAME_TEXT,
                ConfigUtil.getConfigData("company"));
        clickAndWait(selenium, Users.SEARCH_VALUE_BUTTON);

        // Check if all the three users name are present in the search result.
        Assert.assertEquals(selenium
                .isElementPresent("//input[@name='radioBtn' and @value='"
                        + ConfigUtil.getConfigData("adminName") + "']"),
                true);
        Assert.assertEquals(
                selenium.isElementPresent("//input[@name='radioBtn' and @value='"
                        + ConfigUtil.getConfigData("anyoneName") + "']"),
                true);
        Assert.assertEquals(selenium
                .isElementPresent("//input[@name='radioBtn' and @value='"
                        + ConfigUtil.getConfigData("pmName") + "']"),
                true);
        CommonFuncs.logoutSystem(selenium);
    }

    /**
     * Log in with an administrator account and then log out.
     */
    @Test
    // (dependsOnMethods={"verfiySuperAmdinUsers"})
    public void AdminLogin()
    {
        CommonFuncs.loginSystemWithAdmin(selenium);
        Assert.assertEquals(selenium.isElementPresent(MainFrame.HOME_LINK),
                true);
    }

    /**
     * Log in with a PM account and then log out.
     */
    @Test
    // (dependsOnMethods={"verfiySuperAmdinUsers"})
    public void PMLogin()
    {
        CommonFuncs.loginSystemWithPM(selenium);
        Assert.assertEquals(selenium.isElementPresent(MainFrame.HOME_LINK),
                true);
    }

    /**
     * Log in with a common user account and then log out.
     */
    @Test
    // (dependsOnMethods={"verfiySuperAmdinUsers"})
    public void AnyOneLogin()
    {
        CommonFuncs.loginSystemWithAnyone(selenium);
        Assert.assertEquals(selenium.isElementPresent(MainFrame.HOME_LINK),
                true);
    }
}
