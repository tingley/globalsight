package com.globalsight.selenium.testcases.smoketest;


/*
 * TestCaseName: MyActivitiesAvailableVerify.java 
 * Author:Jester
 * Tests:verifyActivityAvailable()
 * 
 * History: Date Comments Updater 
 * 2011-6-22 First Version Jester
 */

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
import com.globalsight.selenium.properties.ConfigUtil;
import com.globalsight.selenium.testcases.BaseTestCase;
import com.thoughtworks.selenium.Selenium;

public class MyActivitiesAvailableVerify extends BaseTestCase{
	
	/*
	 * Common Variables
	 */
	
	private Selenium selenium;
	
  @Test
  public void verifyActivityAvailable() {
		selenium.click(MainFrame.MyActivities_MENU);
		selenium.click(MainFrame.Available_SUBMENU);
		selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
		
		//Check if all the links and buttons are present.
		Assert.assertTrue(selenium.isElementPresent(MyActivities.CheckAll_LINK));
		Assert.assertTrue(selenium.isElementPresent(MyActivities.ClearAll_LINK));
		Assert.assertTrue(selenium.isElementPresent(MyActivities.Accept_BUTTON));
		Assert.assertTrue(selenium.isElementPresent(MyActivities.DetailedWordCounts_BUTTON));
		Assert.assertTrue(selenium.isElementPresent(MyActivities.Export_BUTTON));
		Assert.assertTrue(selenium.isElementPresent(MyActivities.Download_BUTTON));
		
		//Make sure bellow two buttons are disable.
		Assert.assertFalse(selenium.isEditable(MyActivities.DetailedWordCounts_BUTTON));
		Assert.assertFalse(selenium.isEditable(MyActivities.Export_BUTTON));
		
		
		//Check the first job.
		selenium.click(MyActivities.MyActivities_TABLE+"/tr[2]//input");
		//make sure bellow two buttons are changed to enable.
		Assert.assertTrue(selenium.isEditable(MyActivities.DetailedWordCounts_BUTTON));
		Assert.assertTrue(selenium.isEditable(MyActivities.Export_BUTTON));
		
		
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
	  selenium=CommonFuncs.initSelenium();
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
