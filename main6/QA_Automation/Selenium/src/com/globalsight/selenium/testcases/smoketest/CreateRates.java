package com.globalsight.selenium.testcases.smoketest;

/*
 * TestCaseName: CreateRates.java
 * Author:Jester
 * Tests:Create_Rates()
 * 
 * History:
 * Date       Comments       Updater
 * 2011-6-8  First Version  Jester
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

import com.thoughtworks.selenium.Selenium;
import com.globalsight.selenium.functions.CommonFuncs;
import com.globalsight.selenium.functions.RatesFuncs;
import com.globalsight.selenium.pages.MainFrame;
import com.globalsight.selenium.properties.ConfigUtil;

public class CreateRates {

	private Selenium selenium;
	private RatesFuncs iRatesFuncs = new RatesFuncs();
	String testCaseName = getClass().getName();

	@Test
	public void createRates() throws Exception {
		selenium.click(MainFrame.Setup_MENU);
		selenium.click(MainFrame.Rates_SUBMENU);
		selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);

		iRatesFuncs.newRate(selenium,
				ConfigUtil.getDataInCase(testCaseName, "FIXEDRATE"));
		iRatesFuncs.newRate(selenium,
				ConfigUtil.getDataInCase(testCaseName, "HOURLYRATE"));
		iRatesFuncs.newRate(selenium,
				ConfigUtil.getDataInCase(testCaseName, "PAGERATE"));
		iRatesFuncs.newRate(selenium,
				ConfigUtil.getDataInCase(testCaseName, "WORDCOUNTRATE"));
		iRatesFuncs.newRate(selenium, 
				ConfigUtil.getDataInCase(testCaseName, "WORDCOUNTByPERCENT"));
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
