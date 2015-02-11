package com.globalsight.selenium.testcases.smoketest;

import junit.framework.Assert;

import org.testng.annotations.Test;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.AfterTest;

import com.globalsight.selenium.functions.BasicFuncs;
import com.globalsight.selenium.functions.CommonFuncs;
import com.globalsight.selenium.pages.MainFrame;
import com.globalsight.selenium.properties.ConfigUtil;
import com.globalsight.selenium.testcases.dataprepare.smoketest.job.CreateJobs;
import com.thoughtworks.selenium.Selenium;

/*
 * TestCaseName: MyActivityFinishedVerify.java 
 * Author:Jester
 * Tests:verifyActivityFinished()
 * 
 * History: Date Comments Updater 
 * 2011-6-22 First Version Jester
 */

public class MyActivitiesFinishedVerify extends BasicFuncs{
	/**
	 * Common variables
	 */
	private Selenium selenium;
	CreateJobs c = new CreateJobs();
	String jn = ConfigUtil.getDataInCase(c.getClassName(), "jobName1");
	private static final String column = "//div[@id='contentLayer']/table[2]/tbody/tr[2]/td/table/tbody/tr/td/form/table/col[4]";
	
	@Test
	public void verifyActivityFinished() throws Exception{
		selenium.click(MainFrame.MyJobs_MENU);
		selenium.click(MainFrame.Exported_SUBMENU);
		selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);

		Assert.assertTrue(selenium.isElementPresent(column));
	}

	@BeforeMethod
	public void beforeMethod() {
		CommonFuncs.loginSystemWithPM(selenium);
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
