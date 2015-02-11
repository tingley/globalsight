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
import com.globalsight.selenium.testcases.dataprepare.smoketest.job.CreateJobs;
import com.thoughtworks.selenium.Selenium;

/*
 * TestCaseName: VerfiyActivityStatusReviewer.java
 * Author:Jester
 * Tests:verifyActivityStatusReviewer()
 * 
 * History:
 * Date       Comments       Updater
 * 2011-6-21  First Version  Jester
 */

public class VerifyActivityStatusReviewer extends BaseTestCase {
	
	/*
	 * Common Variables.
	 */
	private Selenium selenium;
	CreateJobs c = new CreateJobs();
	
	//Author:Jester
  @Test
  public void verifyActivityStatusReviewer() {
	  
		selenium.click(MainFrame.MyActivities_MENU);
		selenium.click(MainFrame.Available_SUBMENU);
		selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
		selenium.click("link="+ConfigUtil.getDataInCase(c.getClassName(), "jobName1"));
		selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);

		
		//Check the info displayed in the details.
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
		
		//Click Accept button.
		selenium.click(MyActivities.Accept_BUTTON_Job);
		selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
		
		//Check the Task completed button and exit option exists.
		Assert.assertEquals(selenium.isElementPresent(MyActivities.TaskCompleted_BUTTON), true);
		  }
  @BeforeMethod
  public void beforeMethod() {
//      CommonFuncs.loginSystemWithAnyone(selenium);
	  CommonFuncs.login(selenium, ConfigUtil.getConfigData("COMPANY_NAME")+ConfigUtil.getConfigData("reviewer_login_name"), ConfigUtil.getConfigData("reviewer_password"));
  }

  @AfterMethod
  public void afterMethod() {
	  CommonFuncs.logoutSystem(selenium);
  }

  @BeforeTest
  public void beforeTest() {
	  selenium=CommonFuncs.initSelenium();
  }

  @AfterTest
  public void afterTest() {
	  CommonFuncs.endSelenium(selenium);
  }

}
