package com.globalsight.selenium.testcases.smoketest;

/*
 * TestCaseName: CreateProject.java
 * Author:Jester
 * Tests:Create_Project()
 * 
 * History:
 * Date       Comments       Updater
 * 2011-5-30  First Version  Jester
 */

import org.testng.Assert;

import org.testng.Reporter;
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
import com.globalsight.selenium.functions.ProjectsFuncs;
import com.globalsight.selenium.pages.MainFrame;
import com.globalsight.selenium.pages.Projects;
import com.globalsight.selenium.properties.ConfigUtil;
import com.thoughtworks.selenium.Selenium;

public class CreateProject {
	/*
	 * Common variables initialization.
	 */
	private Selenium selenium;
	private ProjectsFuncs iProjectsFuncs = new ProjectsFuncs();
	String testCaseName = getClass().getName();

	@Test
	public void createProject() throws Exception {
		selenium.click(MainFrame.Setup_MENU);
		selenium.click(MainFrame.Projects_SUBMENU);
		selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
		selenium.click(Projects.New_BUTTON);
		selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
		selenium.select(Projects.ProjectManager_SELECT, ConfigUtil.getConfigData("pm"));
		
		String projectname = iProjectsFuncs.newProject(selenium,
				ConfigUtil.getDataInCase(testCaseName, "PROJECT1"));
		
	
		if (projectname != null) {
			Assert.assertEquals(iProjectsFuncs.isPresentInTable(selenium,
					ConfigUtil.getDataInCase(testCaseName, "PROJECTTABLE"),
					projectname), true);
		} else {
			Reporter.log("the project creation failed!");
		}
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
