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
import com.globalsight.selenium.functions.MyAccountFuncs;
import com.globalsight.selenium.pages.JobEditors;
import com.globalsight.selenium.pages.MainFrame;
import com.globalsight.selenium.pages.MyActivities;
import com.globalsight.selenium.properties.ConfigUtil;
import com.globalsight.selenium.testcases.BaseTestCase;
import com.globalsight.selenium.testcases.dataprepare.smoketest.job.CreateJobs;
import com.thoughtworks.selenium.Selenium;

/*
 * TestCaseName: MyActivitiesJobEditorVerify.java 
 * Author:Jester
 * Tests:verifyPopupEditor()
 * 
 * History: Date Comments Updater 
 * 2011-6-22 First Version Jester
 */

public class MyActivitiesJobEditorVerify extends BaseTestCase {
	/*
	 * Common Variables
	 */
	private Selenium selenium;
	MyAccountFuncs iMyAccountFuncs = new MyAccountFuncs();
	CreateJobs c = new CreateJobs();
	@Test
	public void verifyPopupEditor() {
		selenium.click(MainFrame.MyActivities_MENU);
		selenium.click(MainFrame.InProgress2_SUBMENU);
		selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);

		selenium.click("link="+ConfigUtil.getDataInCase(c.getClassName(), "jobName1"));
		selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);

		// Open the MainEditor
		selenium.click(MyActivities.TargetFiles_TABLE + "/tr/td[2]/div/a");
		selenium.waitForPopUp(JobEditors.MainEditor_TAG, CommonFuncs.SHORT_WAIT);
		selenium.selectWindow("name=" + JobEditors.MainEditor_TAG);

		// Open SegmentsEditor
		selenium.selectFrame(JobEditors.RelativeTop_FRAME);
		selenium.selectFrame(JobEditors.content_FRAME);
		selenium.selectFrame(JobEditors.Target_FRAME);
		selenium.selectFrame(JobEditors.content_FRAME);
		selenium.click(JobEditors.Segments_TABLE + "/tr[2]/td[2]/a");
		selenium.waitForPopUp(JobEditors.SegmentEditor_TAG,
				CommonFuncs.SHORT_WAIT);
		// edit the content.
		selenium.selectWindow("name=" + JobEditors.SegmentEditor_TAG);
		selenium.selectFrame(JobEditors.RelativeTop_FRAME);
		selenium.selectFrame(JobEditors.Target_FRAME);
		selenium.selectFrame(JobEditors.Edit_FRAME);
		selenium.type(JobEditors.Edit_TextFiled,
				ConfigUtil.getDataInCase(getClassName(), "STRINGPOPUP"));

		// Close the SegmentsEditor
		selenium.selectFrame(JobEditors.RelativeTop_FRAME);
		selenium.selectFrame(JobEditors.Menu_WINDOW);
		selenium.click(JobEditors.Close_LINK_Segment);

		// Verify the change worked.
		selenium.selectWindow("name=" + JobEditors.MainEditor_TAG);
		selenium.selectFrame(JobEditors.RelativeTop_FRAME);
		selenium.selectFrame(JobEditors.content_FRAME);
		selenium.selectFrame(JobEditors.Target_FRAME);
		selenium.selectFrame(JobEditors.content_FRAME);
		Assert.assertEquals(
				selenium.getText(JobEditors.Segments_TABLE + "/tr[2]/td[2]/a"),
				ConfigUtil.getDataInCase(getClassName(), "STRINGPOPUPVERIFY"));

		// Close the MainEditor
		selenium.selectFrame(JobEditors.RelativeTop_FRAME);
		selenium.selectFrame(JobEditors.Menu_WINDOW);
		selenium.click(JobEditors.Close_LINK_Segment);
		selenium.selectWindow(null);
	}

	@Test
	public void verifyInlineEditor() {
		iMyAccountFuncs.changeDefaultEditor(selenium, "Inline");

		selenium.click(MainFrame.MyActivities_MENU);
		selenium.click(MainFrame.InProgress2_SUBMENU);
		selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);

		selenium.click("link="+ConfigUtil.getDataInCase(c.getClassName(), "jobName1"));
		selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);

		// Open the MainEditor
		selenium.click(MyActivities.TargetFiles_TABLE + "/tr/td[2]/div/a");
		selenium.waitForPopUp(JobEditors.InlineEditor_TAG,
				CommonFuncs.SHORT_WAIT);
		selenium.selectWindow("name=" + JobEditors.InlineEditor_TAG);

		selenium.click(JobEditors.Segments_TABLE_Inline + "/p[2]");
		selenium.selectFrame(JobEditors.iEditor_FRAME);
		selenium.type("//body",
				ConfigUtil.getDataInCase(getClassName(), "STRINGINLINE"));
		selenium.selectFrame(JobEditors.RelativeTop_FRAME);
		selenium.click(JobEditors.SaveChanges_BUTTON);
		Assert.assertEquals(
				selenium.getText(JobEditors.Segments_TABLE_Inline + "/p[2]"),
				ConfigUtil.getDataInCase(getClassName(), "STRINGINLINE"));

		selenium.click(JobEditors.Close_BUTTON_Inline);
		selenium.selectWindow(null);

		iMyAccountFuncs.changeDefaultEditor(selenium, "Popup");
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
