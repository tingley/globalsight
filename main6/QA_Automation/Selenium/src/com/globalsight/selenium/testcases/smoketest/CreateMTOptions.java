package com.globalsight.selenium.testcases.smoketest;

/*
 * TestCaseName: CreateTM.java
 * Author:Jester
 * Tests:Create_TM()
 * 
 * History:
 * Date       Comments       Updater
 * 2011-6-1  First Version  Jester
 */
import org.testng.annotations.Test;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.AfterSuite;

import com.globalsight.selenium.functions.CommonFuncs;
import com.globalsight.selenium.functions.TMProfileFuncs;
import com.globalsight.selenium.pages.MainFrame;
import com.globalsight.selenium.properties.ConfigUtil;
import com.thoughtworks.selenium.Selenium;

public class CreateMTOptions {

	/*
	 * Common Variables
	 */
	private Selenium selenium;
	TMProfileFuncs iTMProfileFuncs = new TMProfileFuncs();
	String testCaseName = getClass().getName();

	/*
	 * Create a new TM.
	 */
	@Test
	public void createMTOptions() throws Exception {
		selenium.click(MainFrame.Setup_MENU);
		selenium.click(MainFrame.TranslationMemoryProfiles_SUBMENU);
		selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
		String className = getClass().getName();

		iTMProfileFuncs.createMTOptions(selenium,
				ConfigUtil.getDataInCase(testCaseName, "MTOptons1"), className);

	}

	@BeforeMethod
	public void beforeMethod() {
		CommonFuncs.loginSystemWithAdmin(selenium);
	}

	@AfterMethod
	public void afterMethod() {
		CommonFuncs.logoutSystem(selenium);
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
