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
 * TestCaseName: VerfiyActivityStatus.java
 * Author:Jester
 * Tests:verifyActivityStatus()
 * 
 * History:
 * Date       Comments       Updater
 * 2011-6-21  First Version  Jester
 */
public class VerifyActivityStatus extends BaseTestCase {
	/*
	 * Common Variables.
	 */
	private Selenium selenium;

	
	//Author:Jester
	@Test
	public void verifyActivityStatus() {
		selenium.click(MainFrame.MyActivities_MENU);
		selenium.click(MainFrame.Available_SUBMENU);
		selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);

		selenium.click("link="
				+ ConfigUtil.getDataInCase(getClassName(),"ACTIVITYJOBNAME"));
			
		selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);

		Assert.assertEquals(
				selenium.getText(MyActivities.Details_TABLE + "/tr[4]/td[2]"),
				ConfigUtil.getDataInCase(getClassName(), "ACTIVITY"));
		Assert.assertEquals(
				selenium.getText(MyActivities.Details_TABLE + "/tr[5]/td[2]"),
				ConfigUtil.getConfigData("COMPANY_NAME"));
		Assert.assertEquals(
				selenium.getText(MyActivities.Details_TABLE + "/tr[7]/td[2]"),
				ConfigUtil.getConfigData("pm"));
		Assert.assertEquals(
				selenium.getText(MyActivities.Details_TABLE + "/tr[10]/td[2]"),
				ConfigUtil.getDataInCase(getClassName(), "SOURCELOCALE"));
		Assert.assertEquals(
				selenium.getText(MyActivities.Details_TABLE + "/tr[11]/td[2]"),
				ConfigUtil.getDataInCase(getClassName(), "TARGETLOCALE"));
		Assert.assertEquals(
				selenium.getText(MyActivities.Details_TABLE + "/tr[14]/td[2]"),
				ConfigUtil.getDataInCase(getClassName(), "OVERDUE"));
		Assert.assertEquals(
				selenium.getText(MyActivities.Details_TABLE + "/tr[15]/td[2]"),
				ConfigUtil.getDataInCase(getClassName(), "STATUS"));
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
