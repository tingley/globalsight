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
import com.globalsight.selenium.pages.MyJobs;
import com.thoughtworks.selenium.Selenium;

/*
 * TestCaseName: MyJobsReadyVerify.java 
 * Author:Jester
 * Tests:verifyJobsReady()
 * 
 * History: Date Comments Updater 
 * 2011-6-24 First Version Jester
 */

public class MyJobsReadyVerify {
	/*
	 * Common Variables
	 */
	private Selenium selenium;
	
	
	/*
	 * Verify MyJobs-->Ready page, the button and link status. 
	 * Author:Jester
	 */
  @Test
  public void verifyJobsReady() {
	  
	  selenium.click(MainFrame.MyJobs_MENU);
	  selenium.click(MainFrame.Ready_SUBMENU);
	  selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
	  
	  //Check the buttons or links are presented.  
	  Assert.assertTrue(selenium.isElementPresent(MyJobs.CheckAll_LINK));
	  Assert.assertTrue(selenium.isElementPresent(MyJobs.CheckAllPages_LINK));
	  Assert.assertTrue(selenium.isElementPresent(MyJobs.ClearAll_LINK));
	  Assert.assertTrue(selenium.isElementPresent(MyJobs.SearchReplace_BUTTON));
	  Assert.assertTrue(selenium.isElementPresent(MyJobs.ChangeWorkflowManagers_BUTTON));
	  Assert.assertTrue(selenium.isElementPresent(MyJobs.Discard_BUTTON));
	  Assert.assertTrue(selenium.isElementPresent(MyJobs.Dispatch_BUTTON));
	  
	  
	  selenium.click(MyJobs.CheckAll_LINK);
	  selenium.click(MyJobs.ClearAll_LINK);
	  
	  //Check if the button Change Work flow Managers button is disabled. 
	  Assert.assertFalse(selenium.isEditable(MyJobs.ChangeWorkflowManagers_BUTTON));
	  
	  
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
