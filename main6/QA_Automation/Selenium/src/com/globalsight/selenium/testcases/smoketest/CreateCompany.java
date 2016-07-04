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
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
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

        openMenuItemAndWait(selenium, MainFrame.SETUP_MENU,
                MainFrame.COMPANIES_SUBMENU);

        companyFuncs.newCompany(selenium, testCaseName);
        Thread.sleep(Long.valueOf(CommonFuncs.MEDIUM_WAIT));
        CommonFuncs.logoutSystem(selenium);
        
        
        CommonFuncs.loginSystemWithAdmin(selenium);
        Assert.assertEquals(selenium.isElementPresent(MainFrame.HOME_LINK),
                true);
        
        CommonFuncs.loginSystemWithPM(selenium);
        Assert.assertEquals(selenium.isElementPresent(MainFrame.HOME_LINK),
                true);
        
        CommonFuncs.loginSystemWithAnyone(selenium);
        Assert.assertEquals(selenium.isElementPresent(MainFrame.HOME_LINK),
                true);
        
        
    }   
    
    @BeforeTest
    private void beforeTest() {
        CommonFuncs.loginSystemWithSuperAdmin(selenium);
    }
    
    @AfterTest
    private void afterTest() {
    	if (selenium.isElementPresent("link=Logout"))
    		CommonFuncs.logoutSystem(selenium);
        CommonFuncs.loginSystemWithAdmin(selenium);
    }
}
