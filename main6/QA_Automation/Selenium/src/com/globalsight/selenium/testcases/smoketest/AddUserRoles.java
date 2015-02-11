package com.globalsight.selenium.testcases.smoketest;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.globalsight.selenium.functions.CommonFuncs;
import com.globalsight.selenium.functions.UsersFuncs;
import com.globalsight.selenium.pages.MainFrame;
import com.globalsight.selenium.properties.ConfigUtil;
import com.thoughtworks.selenium.Selenium;

public class AddUserRoles {
	/*
	 * Common variables initialization.
	 */
	private Selenium selenium;
	private UsersFuncs iUsersFuncs = new UsersFuncs();
	String testCaseName = getClass().getName();
    
	@Test
	public void addUserRoles() throws Exception {
		selenium.click(MainFrame.Setup_MENU);
		selenium.click(MainFrame.Users_SUBMENU);
		selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
		iUsersFuncs.editAddUserRoles(selenium,
				ConfigUtil.getDataInCase(testCaseName, "USER1"));
		
	}

	@BeforeMethod
	public void beforeMethod() {
		CommonFuncs.loginSystemWithAdmin(selenium);
	}

	@AfterMethod
	public void afterMethod() {
		CommonFuncs.logoutSystem(selenium);
	}

	@BeforeTest
	public void beforeTest() {
		selenium = CommonFuncs.initSelenium();
	}

	@AfterTest
	public void afterTest() {
		CommonFuncs.endSelenium(selenium);
	}

}
