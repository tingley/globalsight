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
import com.globalsight.selenium.pages.MyJobs;
import com.globalsight.selenium.testcases.BaseTestCase;
import com.thoughtworks.selenium.Selenium;

/*
 * TestCaseName: MyJobsAllStatusVerify.java 
 * Author:Jester
 * Tests:verifyJobsAllStatus()
 * 
 * History: Date Comments Updater 
 * 2011-6-24 First Version Jester
 */

public class MyJobsAllStatusVerify extends BasicFuncs {
	/*
	 * Common Variables
	 */
	private Selenium selenium;
	
  @Test
  public void verifyJobsAllStatus() {
	  selenium.click(MainFrame.MyJobs_MENU);
	  selenium.click(MainFrame.AllStatus_SUBMENU);
	  selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
	  
	  //Check to make sure no button presents on the page.
	 
	  Assert.assertFalse(selenium.isElementPresent(MyJobs.AllButtons_BUTTON));
		
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
