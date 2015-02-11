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
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.globalsight.selenium.functions.CommonFuncs;
import com.globalsight.selenium.functions.CompanyFuncs;
import com.globalsight.selenium.pages.MainFrame;
import com.globalsight.selenium.pages.Users;
import com.globalsight.selenium.properties.ConfigUtil;
import com.thoughtworks.selenium.Selenium;

public class CreateCompany {
	/*
	 * Common variables initialization.
	 */
	private Selenium selenium;
	private CompanyFuncs iCompanyFuncs = new CompanyFuncs();
	String testCaseName = getClass().getName();

	/*
	 * Sign in with superAdmin and create a new company. Verify the company can
	 * be created.
	 */
	@Test
	public void createSuperAdminCompany() throws Exception {
		CommonFuncs.loginSystemWithSuperAdmin(selenium);
		selenium.click(MainFrame.Setup_MENU);
		selenium.click(MainFrame.Companies_SUBMENU);
		selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
		iCompanyFuncs.newCompany(selenium, testCaseName);
		
		selenium.click(MainFrame.LogOut_LINK);
	}

	/*
	 * Verify three users for the company are available.
	 */
	@Test (dependsOnMethods={"createSuperAdminCompany"})
	public void verfiySuperAmdinUsers() {
		CommonFuncs.loginSystemWithSuperAdmin(selenium);
		
		selenium.click(MainFrame.Setup_MENU);
		selenium.click(MainFrame.Users_SUBMENU);
		selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);

		// Do a search with the companyname in the Users page.
		selenium.select(Users.NameTypeOption_SELECT,
				ConfigUtil.getDataInCase(testCaseName, "NAMETYPEOPTION"));
		selenium.select(Users.NameOption_SELECT,
				ConfigUtil.getDataInCase(testCaseName, "NAMEOPTION"));
		selenium.type(Users.UserNameSearch_TEXT_FIELD,
				ConfigUtil.getConfigData("COMPANY_NAME"));
		selenium.click(Users.Search_BUTTON);
		selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);

		// Check if all the three users name are present in the search result.
		Assert.assertEquals(selenium
				.isElementPresent("//input[@name='radioBtn' and @value='"
						+ ConfigUtil.getConfigData("admin_login_name")
						+ "']"), true);
		Assert.assertEquals(selenium
				.isElementPresent("//input[@name='radioBtn' and @value='"
						+ ConfigUtil.getConfigData("anyone_login_name")
						+ "']"), true);
		Assert.assertEquals(selenium
				.isElementPresent("//input[@name='radioBtn' and @value='"
						+ ConfigUtil.getConfigData("pm_login_name")
						+ "']"), true);
		selenium.click(MainFrame.LogOut_LINK);
	}
	
    /**
     * Log in with an administrator account and then log out.
     */
	@Test //(dependsOnMethods={"verfiySuperAmdinUsers"})
    public void AdminLogin()
    {
        CommonFuncs.loginSystemWithAdmin(selenium);
        Assert.assertEquals(selenium.isElementPresent(MainFrame.Home_LINK),
                true);
    }
    
    /**
     * Log in with a PM account and then log out.
     */
	@Test //(dependsOnMethods={"verfiySuperAmdinUsers"})
    public void PMLogin()
    {
    	CommonFuncs.loginSystemWithPM(selenium);
    	Assert.assertEquals(selenium.isElementPresent(MainFrame.Home_LINK),
    			true);
    }

    /**
     * Log in with a common user account and then log out.
     */
	@Test //(dependsOnMethods={"verfiySuperAmdinUsers"})
    public void AnyOneLogin()
    {
        CommonFuncs.loginSystemWithAnyone(selenium);
        Assert.assertEquals(selenium.isElementPresent(MainFrame.Home_LINK),
                true);
    }



	@BeforeMethod
	public void beforeMethod() {
	
	}

	@AfterMethod
	public void afterMethod() {
		
	}

	@BeforeClass
	public void beforeClass() {
	}

	@AfterClass
	public void afterClass() {
	}

	@BeforeTest
	public void beforeTest() {
		selenium = CommonFuncs.initSelenium();
	}

	@AfterTest
	public void afterTest() {
		CommonFuncs.endSelenium(selenium);
	}

	@BeforeSuite
	public void beforeSuite() {
	}

	@AfterSuite
	public void afterSuite() {
	}

}
