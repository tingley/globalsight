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

import com.globalsight.selenium.functions.BasicFuncs;
import com.globalsight.selenium.functions.CommonFuncs;
import com.globalsight.selenium.pages.MainFrame;
import com.globalsight.selenium.pages.MyActivities;
import com.globalsight.selenium.properties.ConfigUtil;

import com.globalsight.selenium.testcases.BaseTestCase;
import com.thoughtworks.selenium.Selenium;

/*
 * TestCaseName: VerifyJobFinished.java
 * Author:Jester
 * Tests:verifyJobFinished()
 * 
 * History:
 * Date       Comments       Updater
 * 2011-6-21  First Version  Jester
 */

public class VerifyJobFinished extends BaseTestCase {

	/*
	 * Common Variables.
	 */
	private Selenium selenium;
	
	BasicFuncs iBasicFuncs=new BasicFuncs();

	@Test
	public void verfiyJobFinished() throws Exception {

		selenium.click(MainFrame.MyActivities_MENU);
		selenium.click(MainFrame.InProgress2_SUBMENU);
		selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);

		selenium.click("link="+ConfigUtil.getDataInCase(getClassName(), "JOBNAME"));
		selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
		selenium.click(MyActivities.TaskCompleted_BUTTON);
		if (selenium.isConfirmationPresent()) {
			selenium.getConfirmation();
		}

		selenium.click(MainFrame.MyActivities_MENU);
		selenium.click(MainFrame.Finished_SUBMENU);
		selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);

		Assert.assertEquals(iBasicFuncs.isPresentInTable(selenium, MyActivities.MyActivities_TABLE, "VerifyJob", 5), true);
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
