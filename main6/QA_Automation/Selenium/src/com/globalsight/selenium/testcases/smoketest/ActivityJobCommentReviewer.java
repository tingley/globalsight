package com.globalsight.selenium.testcases.smoketest;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.AfterTest;
import com.globalsight.selenium.functions.CommonFuncs;
import com.globalsight.selenium.pages.JobEditors;
import com.globalsight.selenium.pages.MainFrame;
import com.globalsight.selenium.pages.MyActivities;
import com.globalsight.selenium.properties.ConfigUtil;
import com.globalsight.selenium.testcases.BaseTestCase;
import com.globalsight.selenium.testcases.dataprepare.smoketest.job.CreateJobs;
import com.thoughtworks.selenium.Selenium;

/*
 * TestCaseName: ActivityJobComment.java 
 * Author:Jester 
 * Tests:addComment()
 * 
 * History: Date Comments Updater 
 * 2011-6-22 First Version Jester
 */

public class ActivityJobCommentReviewer extends BaseTestCase {

	/**
	 * Common Variables
	 */
	private Selenium selenium;
	CreateJobs c = new CreateJobs();
	@Test
	public void addComment() throws InterruptedException {

		selenium.click(MainFrame.MyActivities_MENU);
		selenium.click(MainFrame.InProgress2_SUBMENU);
		selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
		
		selenium.click("link=" + ConfigUtil.getDataInCase(c.getClassName(), "jobName1"));
		selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
		// open MainEditor
		selenium.click(MyActivities.TargetFiles_TABLE + "/tr/td[2]/div/a");
		selenium.waitForPopUp(JobEditors.MainEditor_TAG, CommonFuncs.SHORT_WAIT);
		selenium.selectWindow("name="+JobEditors.MainEditor_TAG);

		// Open CommentEditor
		selenium.selectFrame(JobEditors.RelativeTop_FRAME);
		selenium.selectFrame(JobEditors.content_FRAME);
		selenium.selectFrame(JobEditors.Target_FRAME);
		selenium.selectFrame(JobEditors.content_FRAME);
		selenium.click(JobEditors.Segments_TABLE + "/tr/td[3]/a");
		selenium.waitForPopUp(JobEditors.CommentEditor_TAG,
				CommonFuncs.SHORT_WAIT);
		selenium.selectWindow(JobEditors.CommentEditor_WINDOW);

		// Fill the comment.
		selenium.select(JobEditors.Priority_SELECTION,
				ConfigUtil.getDataInCase(getClassName(), "PRIORITY"));
		selenium.select(JobEditors.Category_SELECTION,
				ConfigUtil.getDataInCase(getClassName(), "CATEGORY"));
		selenium.type(JobEditors.Title_TEXT_FIELD,
				ConfigUtil.getDataInCase(getClassName(), "TITLE"));
		selenium.type(JobEditors.Comment_TEXT_FIELD,
				ConfigUtil.getDataInCase(getClassName(), "COMMENT"));

		selenium.click(JobEditors.OK_BUTTON);

		// Check if the column exists.
		selenium.selectWindow("name="+JobEditors.MainEditor_TAG);
		selenium.selectFrame(JobEditors.RelativeTop_FRAME);
		selenium.selectFrame(JobEditors.Target_FRAME);
		selenium.selectFrame(JobEditors.content_FRAME);

		Assert.assertEquals(
				selenium.isElementPresent(JobEditors.Segments_TABLE
						+ "/tr/td[2]/img"), true);

		// Check if the comments exits.

		selenium.selectFrame(JobEditors.RelativeTop_FRAME);
		selenium.selectFrame(JobEditors.Review_FRAME);

		Assert.assertNotNull(selenium.getText(JobEditors.Comments_TABLE
				+ "/tr/td[2]"));

		// close the mainEditor.
		selenium.selectFrame(JobEditors.RelativeTop_FRAME);
		selenium.selectFrame(JobEditors.Menu_WINDOW);
		selenium.click(JobEditors.Close_LINK);
		selenium.selectWindow(null);

	}

	@BeforeMethod
	public void beforeMethod() {
//		CommonFuncs.loginSystemWithAnyone(selenium);
	    CommonFuncs.login(selenium, ConfigUtil.getConfigData("COMPANY_NAME")+ConfigUtil.getConfigData("reviewer_login_name"), ConfigUtil.getConfigData("reviewer_password"));
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
