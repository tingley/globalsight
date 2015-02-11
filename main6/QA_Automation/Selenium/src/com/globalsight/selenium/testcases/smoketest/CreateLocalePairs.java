package com.globalsight.selenium.testcases.smoketest;

import org.testng.Assert;

import org.testng.annotations.Test;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.AfterSuite;
import org.testng.Reporter;

import com.globalsight.selenium.functions.CommonFuncs;
import com.globalsight.selenium.functions.LocalePairsFuncs;
import com.globalsight.selenium.pages.MainFrame;
import com.globalsight.selenium.properties.ConfigUtil;
import com.thoughtworks.selenium.Selenium;

public class CreateLocalePairs {
	/*
	 * Common variables initialization.
	 */
	private Selenium selenium;
	private LocalePairsFuncs iLocalePairsFuncs = new LocalePairsFuncs();
	String testCaseName = getClass().getName();

	@Test
	public void createLocalPairs() {
		selenium.click(MainFrame.Setup_MENU);
		selenium.click(MainFrame.LocalePairs_SUBMENU);
		selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);

		iLocalePairsFuncs.newLocalPairs(selenium,
				ConfigUtil.getDataInCase(testCaseName, "SOURCELOCALE"),
				ConfigUtil.getDataInCase(testCaseName, "TARGETLOCALE"));
		
		iLocalePairsFuncs.newLocalPairs(selenium,
				ConfigUtil.getDataInCase(testCaseName, "SOURCELOCALE0"),
				ConfigUtil.getDataInCase(testCaseName, "TARGETLOCALE0"));

		// Verify if there is at least one Locale Paris exists.
		Assert.assertEquals(
				selenium.isElementPresent("//input[@name='radioBtn']"), true);
	}

	@Test
	/**
	 * 
	 * Author Totti
	 */
	public void removeLocalPairs() throws Exception {
		// this.AdminLogin();
		selenium.click(MainFrame.Setup_MENU);
		selenium.click(MainFrame.LocalePairs_SUBMENU);
		selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);

		iLocalePairsFuncs.localPairsRemove(selenium,
				ConfigUtil.getDataInCase(testCaseName, "SOURCELOCALE1"),
				ConfigUtil.getDataInCase(testCaseName, "TARGETLOCALE1"));
		Reporter.log("removed successfully");
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
