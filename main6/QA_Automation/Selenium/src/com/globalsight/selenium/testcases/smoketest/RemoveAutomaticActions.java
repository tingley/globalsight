package com.globalsight.selenium.testcases.smoketest;

import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.globalsight.selenium.functions.AutomaticActionsFuncs;
import com.globalsight.selenium.functions.CommonFuncs;
import com.globalsight.selenium.pages.MainFrame;
import com.globalsight.selenium.properties.ConfigUtil;
import com.thoughtworks.selenium.Selenium;

public class RemoveAutomaticActions {
	private Selenium selenium;
	private AutomaticActionsFuncs iAutomaticActionsFuncs = new AutomaticActionsFuncs();
	String testCaseName = getClass().getName();

	@Test
	public void removeAutomaticAction() throws Exception {
		selenium.click(MainFrame.Setup_MENU);
		selenium.click(MainFrame.AutomaticActions_SUBMENU);
		selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
		iAutomaticActionsFuncs.removeAutomaticAction(selenium,
				ConfigUtil.getDataInCase(testCaseName, "AUTOMATICACTIONS"));
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
}