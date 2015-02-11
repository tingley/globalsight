package com.globalsight.selenium.testcases.smoketest;
//author :ShenYang   2011-07-04
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
import com.globalsight.selenium.functions.TerminologyFuncs;
import com.globalsight.selenium.pages.MainFrame;
import com.globalsight.selenium.properties.ConfigUtil;
import com.globalsight.selenium.testcases.BaseTestCase;
import com.thoughtworks.selenium.Selenium;

public class TerminologyEdit extends BaseTestCase{
	private Selenium selenium;
	private TerminologyFuncs eTerminology = new TerminologyFuncs();
	
	@Test
	public void editTB() throws Exception{
	selenium.click(MainFrame.Setup_MENU);
	selenium.click(MainFrame.Terminology_SUBMENU);
	Terminology t = new Terminology();
	String editProfile = ConfigUtil.getDataInCase(t.getClass().getName(), "for_edit_terminology");
	eTerminology.edit(selenium, editProfile);
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
