package com.globalsight.selenium.testcases.smoketest;

/*
 * TestCaseName: CreateFileExtension.java
 * Author:Jester
 * Tests:Create_FileExtension()
 * 
 * History:
 * Date       Comments       Updater
 * 2011-5-30  First Version  Jester
 */

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
import com.globalsight.selenium.functions.FileExtensionFuncs;
import com.globalsight.selenium.pages.MainFrame;
import com.globalsight.selenium.properties.ConfigUtil;
import com.thoughtworks.selenium.Selenium;

public class CreateFileExtension {

	/*
	 * Common variables initialization.
	 */
	private Selenium selenium;
	private FileExtensionFuncs iFileExtensionFuncs = new FileExtensionFuncs();
	String testCaseName = getClass().getName();

	@Test
	public void createFileExtension() throws Exception {

		selenium.click(MainFrame.DataSources_MENU);
		selenium.click(MainFrame.FileExtension_SUBMENU);
		selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);

		iFileExtensionFuncs.newFileExtension(selenium,
				ConfigUtil.getDataInCase(testCaseName, "EXTENSION"));
		Assert.assertEquals(iFileExtensionFuncs.isPresentInTable(selenium,
				ConfigUtil.getDataInCase(testCaseName, "FILEEXTENSIONTABLE"),
				ConfigUtil.getDataInCase(testCaseName, "EXTENSION")), true);
	}

	@BeforeMethod
	public void beforeMethod() {
		CommonFuncs.loginSystemWithAdmin(selenium);

	}

	@AfterMethod
	public void afterMethod() {
		selenium.click(MainFrame.LogOut_LINK);
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
