package com.globalsight.selenium.testcases.smoketest;

//author : ShenYang   2011-06-25
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
import com.globalsight.selenium.functions.LocalizationFuncs;
import com.globalsight.selenium.pages.MainFrame;
import com.globalsight.selenium.properties.ConfigUtil;
import com.globalsight.selenium.testcases.BaseTestCase;
import com.thoughtworks.selenium.Selenium;

public class AddWFofLocalizationProfile extends BaseTestCase {
	private Selenium selenium;
	 private LocalizationFuncs aLocalizationFuncs = new LocalizationFuncs();
	 @Test
	 public void addWorkflow() throws Exception{
		 selenium.click(MainFrame.Setup_MENU);
			selenium.click(MainFrame.LocalizationProfiles_SUBMENU);
			selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
			String iLocName = ConfigUtil.getDataInCase(getClassName(), "LocalizationNAME");
			String localeName = ConfigUtil.getDataInCase(getClassName(), "LocaleName");
			aLocalizationFuncs.addWorkflow(selenium, iLocName, localeName);
	 }

	 @BeforeMethod
	 public void beforeMethod() {CommonFuncs.loginSystemWithAdmin(selenium);
	 }

	 @AfterMethod
	 public void afterMethod() {CommonFuncs.logoutSystem(selenium);
	 }

	 @BeforeClass
	 public void beforeClass() {
	 }

	 @AfterClass
	 public void afterClass() {
	 }

	 @BeforeTest
	 public void beforeTest() {selenium = CommonFuncs.initSelenium();
	 }

	 @AfterTest
	 public void afterTest() {CommonFuncs.endSelenium(selenium);
	 }

	 @BeforeSuite
	 public void beforeSuite() {
	 }

	 @AfterSuite
	 public void afterSuite() {
	 }

	 }
