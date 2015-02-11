package com.globalsight.selenium.testcases.smoketest;

//author : ShenYang   2011-06-27
//edit project name and project manager.

import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.globalsight.selenium.functions.CommonFuncs;
import com.globalsight.selenium.functions.ProjectsFuncs;
import com.globalsight.selenium.pages.MainFrame;
import com.globalsight.selenium.properties.ConfigUtil;
import com.globalsight.selenium.testcases.BaseTestCase;
import com.thoughtworks.selenium.Selenium;

public class EditProject extends BaseTestCase{
	 private Selenium selenium;
	 private ProjectsFuncs eProjectFuncs = new ProjectsFuncs();
	    
@Test
	public void editProject() throws Exception{
		 	selenium.click(MainFrame.Setup_MENU);
			selenium.click(MainFrame.Projects_SUBMENU);
			selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
			String iProjectName = ConfigUtil.getDataInCase(getClassName(), "PROJECTNAME");
			String iNewProName = ConfigUtil.getDataInCase(getClassName(), "NEW_PROJECTNAME");
			String iNewProManager = ConfigUtil.getConfigData("admin");
			eProjectFuncs.editProject(selenium, iProjectName, iNewProName, iNewProManager);
	 }
@BeforeMethod
public void beforeMethod() {
	  CommonFuncs.loginSystemWithAdmin(selenium);
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
