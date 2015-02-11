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
import com.globalsight.selenium.pages.JobDetails;
import com.globalsight.selenium.pages.MainFrame;
import com.globalsight.selenium.pages.MyActivities;
import com.globalsight.selenium.pages.MyJobs;
import com.globalsight.selenium.properties.ConfigUtil;
import com.globalsight.selenium.testcases.BaseTestCase;
import com.globalsight.selenium.testcases.dataprepare.smoketest.job.CreateJobs;
import com.thoughtworks.selenium.Selenium;


/*
 * TestCaseName: MyJobsDetailsDiscard.java 
 * Author:Jester
 * Tests:verifyJobsDetailsDiscard()
 * 
 * History: Date Comments Updater 
 * 2011-6-26 First Version Jester
 */

public class MyJobsDetailsDiscard extends BaseTestCase {
	/*
	 * Common Variables
	 */
	private Selenium selenium;
	BasicFuncs iBasicFuncs=new BasicFuncs();
	CreateJobs c = new CreateJobs();
  @Test
  public void verifyJobsDetailsDiscard() throws Exception {
		
	  selenium.click(MainFrame.MyJobs_MENU);
	  selenium.click(MainFrame.Ready_SUBMENU);
	  selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
	  selenium.click("link="+ConfigUtil.getDataInCase(c.getClassName(), "jobName1"));
	  selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
	  iBasicFuncs.selectRadioButtonFromTable(selenium, JobDetails.Workflows_TABLE, ConfigUtil.getDataInCase(getClassName(), "WORKFLOW"));
	  selenium.click(JobDetails.Discard_BUTTON);
	  Assert.assertEquals((selenium.getConfirmation().
	          matches("Warning!!\n\nThis will permanently remove the selected Workflows from the system.\nNote: There may be a short delay when Workflows jobs are being discarded.")),true);

	  Assert.assertFalse(iBasicFuncs.isPresentInTable(selenium, JobDetails.Workflows_TABLE, ConfigUtil.getDataInCase(getClassName(), "WORKFLOW")));
	 
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
