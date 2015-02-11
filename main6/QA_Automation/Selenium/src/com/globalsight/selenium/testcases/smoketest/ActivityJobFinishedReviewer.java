package com.globalsight.selenium.testcases.smoketest;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.AfterTest;
import com.globalsight.selenium.functions.BasicFuncs;
import com.globalsight.selenium.functions.CommonFuncs;
import com.globalsight.selenium.pages.MainFrame;
import com.globalsight.selenium.pages.MyActivities;
import com.globalsight.selenium.properties.ConfigUtil;
import com.globalsight.selenium.testcases.BaseTestCase;
import com.globalsight.selenium.testcases.dataprepare.smoketest.job.CreateJobs;
import com.thoughtworks.selenium.Selenium;

/*
 * TestCaseName: ActivityJobFinishedReviewer.java 
 * Author:Jester
 * Tests:finishActivityJob()
 * 
 * History: Date Comments Updater 
 * 2011-6-22 First Version Jester
 */

public class ActivityJobFinishedReviewer extends BaseTestCase {

	/**
	 * Common variables
	 */
	private Selenium selenium;
	BasicFuncs iBasicFuncs = new BasicFuncs();
	CreateJobs c = new CreateJobs();
	@Test
	public void finishAcitityJob() throws Exception {
		selenium.click(MainFrame.MyActivities_MENU);
		selenium.click(MainFrame.InProgress2_SUBMENU);
		selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
		selenium.click("link="+ConfigUtil.getDataInCase(c.getClassName(), "jobName1"));
		selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);

		selenium.click(MyActivities.TaskCompleted_BUTTON);
		Assert.assertTrue(selenium
				.getConfirmation()
				.matches(
						"^Do you want to finish the activity and advance it to the next stage[\\s\\S]$"));
//		selenium.wait(30000);
		selenium.click(MainFrame.MyActivities_MENU);
		selenium.click(MainFrame.Finished_SUBMENU);
		selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
		
	      Assert.assertTrue(selenium
	                .isElementPresent(MyActivities.DetailedWordCounts_BUTTON));
	      
//		Assert.assertEquals(iBasicFuncs.isPresentInTable(selenium,
//				MyActivities.MyActivities_TABLE,
//				ConfigUtil.getDataInCase(a.getClassName(), "ACTIVITYJOBNAME"), 5), true);

	}

	@BeforeMethod
	public void beforeMethod() {
	    CommonFuncs.login(selenium, ConfigUtil.getConfigData("COMPANY_NAME")+ConfigUtil.getConfigData("reviewer_login_name"), ConfigUtil.getConfigData("reviewer_password"));
//	    CommonFuncs.loginSystemWithAnyone(selenium);
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
