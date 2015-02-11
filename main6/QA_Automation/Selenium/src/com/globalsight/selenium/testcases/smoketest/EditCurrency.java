package com.globalsight.selenium.testcases.smoketest;

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
import com.globalsight.selenium.functions.CurrencyFuncs;
import com.globalsight.selenium.pages.MainFrame;
import com.globalsight.selenium.properties.ConfigUtil;
import com.thoughtworks.selenium.Selenium;

public class EditCurrency {

	/*
	 * Common variables initialization.
	 */
	private Selenium selenium;
	private CurrencyFuncs iCurrencyFuncs = new CurrencyFuncs();
	String testCaseName = getClass().getName();

	@Test
	public void editCurrency() throws Exception {
		selenium.click(MainFrame.Setup_MENU);
		iCurrencyFuncs.clickAndWait(selenium, MainFrame.Currency_SUBMENU);
		
		// Edit the first one.
		iCurrencyFuncs.editCurrency(selenium,
				ConfigUtil.getDataInCase(testCaseName, "Currency"));


		// Create the second one.
		//iCurrencyFuncs.editCurrency(selenium,
		//		ConfigUtil.getDataInCase(testCaseName, "Currency1"));

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
