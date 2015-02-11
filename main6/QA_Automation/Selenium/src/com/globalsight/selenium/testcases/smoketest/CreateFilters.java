package com.globalsight.selenium.testcases.smoketest;

/*
 * TestCaseName: CreateFilters.java
 * Author:Jester
 * Tests:Create_Filters()
 * 
 * History:
 * Date       Comments       Updater
 * 2011-5-31  First Version  Jester
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
import com.globalsight.selenium.functions.FilterConfigurationFuncs;
import com.globalsight.selenium.pages.MainFrame;
import com.globalsight.selenium.properties.ConfigUtil;
import com.thoughtworks.selenium.Selenium;

public class CreateFilters {
	/*
	 * Common variables initialization.
	 */
	private Selenium selenium;
	private FilterConfigurationFuncs iFilterConfigurationFuncs = new FilterConfigurationFuncs();
	String testCaseName = getClass().getName();

	// Create the filters with the filters be provided.
	@Test
	public void createFilters() throws Exception {

		selenium.click(MainFrame.DataSources_MENU);
		selenium.click(MainFrame.FilterConfiguration_SUBMENU);
		selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);

		iFilterConfigurationFuncs.newFilters(selenium,
				ConfigUtil.getDataInCase(testCaseName, "FILTERS"));
	}

	@BeforeMethod
	public void beforeMethod() {
		CommonFuncs.loginSystemWithAdmin(selenium);
	}

	@AfterMethod
	public void afterMethod() {
		selenium.click(MainFrame.LogOut_LINK);
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
