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

import com.globalsight.selenium.functions.CommonFuncs;
import com.globalsight.selenium.pages.MainFrame;
import com.globalsight.selenium.pages.MyActivities;
import com.globalsight.selenium.properties.ConfigUtil;
import com.globalsight.selenium.testcases.BaseTestCase;
import com.thoughtworks.selenium.Selenium;

/*
 * TestCaseName: VerifyWrodCounts.java
 * Author:Jester
 * Tests:verifyWordCounts()
 * 
 * History:
 * Date       Comments       Updater
 * 2011-6-21  First Version  Jester
 */

public class VerifyWordCounts extends BaseTestCase {
	/*
	 * Common Variables.
	 */
	private Selenium selenium;

	@Test
	public void verifyWordCounts() {
		selenium.click(MainFrame.MyActivities_MENU);
		selenium.click(MainFrame.Available_SUBMENU);
		selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
		selenium.click("link="+ConfigUtil.getDataInCase(getClassName(), "ACTIVITYJOBNAME"));
		
		selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);

		selenium.click(MyActivities.DetailedWordCounts_BUTTON_Job);
		selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);

		Assert.assertEquals(
				selenium.getText(MyActivities.DetailedStatistics_TABLE
						+ "/tr[2]/td[3]"),
				ConfigUtil.getDataInCase(getClassName(), "100%"));
		Assert.assertEquals(
				selenium.getText(MyActivities.DetailedStatistics_TABLE
						+ "/tr[2]/td[4]"),
				ConfigUtil.getDataInCase(getClassName(), "95%99%"));
		Assert.assertEquals(
				selenium.getText(MyActivities.DetailedStatistics_TABLE
						+ "/tr[2]/td[5]"),
				ConfigUtil.getDataInCase(getClassName(), "85%94%"));
		Assert.assertEquals(
				selenium.getText(MyActivities.DetailedStatistics_TABLE
						+ "/tr[2]/td[6]"),
				ConfigUtil.getDataInCase(getClassName(), "75%84%"));
		Assert.assertEquals(
				selenium.getText(MyActivities.DetailedStatistics_TABLE
						+ "/tr[2]/td[7]"),
				ConfigUtil.getDataInCase(getClassName(), "50%74%"));
		Assert.assertEquals(
				selenium.getText(MyActivities.DetailedStatistics_TABLE
						+ "/tr[2]/td[8]"),
				ConfigUtil.getDataInCase(getClassName(), "NOMATCH"));
		Assert.assertEquals(
				selenium.getText(MyActivities.DetailedStatistics_TABLE
						+ "/tr[2]/td[9]"),
				ConfigUtil.getDataInCase(getClassName(), "REPETITIONS"));
		Assert.assertEquals(
				selenium.getText(MyActivities.DetailedStatistics_TABLE
						+ "/tr[2]/td[10]"),
				ConfigUtil.getDataInCase(getClassName(), "INCONTEXTMATCHES"));
	}

	@BeforeMethod
	public void beforeMethod() {
		CommonFuncs.loginSystemWithAnyone(selenium);
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
