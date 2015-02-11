package com.globalsight.selenium.testcases.smoketest;

import junit.framework.Assert;

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
import com.globalsight.selenium.pages.MainFrame;
import com.globalsight.selenium.pages.MyActivities;
import com.thoughtworks.selenium.Selenium;

/*
 * TestCaseName: MyActivityAllStatusVerify.java 
 * Author:Jester
 * Tests:verifyActivityAllStatus()
 * 
 * History: Date Comments Updater 
 * 2011-6-22 First Version Jester
 */

public class MyActivitiesAllStatusVerify {

	/**
	 * Common variables
	 */
	private Selenium selenium;

	/**
	 * Verify the button Detailed Word Counts and Export exist under all Status
	 * page.
	 */
	@Test
	public void verifyActivityFinished() {
		selenium.click(MainFrame.MyActivities_MENU);
		selenium.click(MainFrame.AllStatus2_SUBMENU);
		selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
		Assert.assertTrue(selenium
				.isElementPresent(MyActivities.DetailedWordCounts_BUTTON));
		Assert.assertTrue(selenium.isElementPresent(MyActivities.Export_BUTTON));
	}

	@BeforeMethod
	public void beforeMethod() {
		CommonFuncs.loginSystemWithPM(selenium);
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
