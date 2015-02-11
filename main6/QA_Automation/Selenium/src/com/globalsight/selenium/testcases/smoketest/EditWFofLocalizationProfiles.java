package com.globalsight.selenium.testcases.smoketest;
//author Shenyang 2011-06-27

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
import com.globalsight.selenium.functions.LocalizationFuncs;

import com.globalsight.selenium.pages.MainFrame;
import com.globalsight.selenium.properties.ConfigUtil;
import com.globalsight.selenium.testcases.BaseTestCase;
import com.thoughtworks.selenium.Selenium;

//Created by Shenyang 2011-6-23

public class EditWFofLocalizationProfiles extends BaseTestCase{
	  /*
     * Common variables initialization.
     */
    private Selenium selenium;
    LocalizationFuncs eLocalizationFuncs = new LocalizationFuncs();

    /* 
     *  Edit workflow of LocalizationProfile   */
    
  @Test
  public void editWorkflow() throws Exception{
	  selenium.click(MainFrame.Setup_MENU);
		selenium.click(MainFrame.LocalizationProfiles_SUBMENU);
		selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
		String iLocName = ConfigUtil.getDataInCase(getClassName(), "LocalizationNAME");
		String iWFName = ConfigUtil.getDataInCase(getClassName(), "WorkflowName");
	
		eLocalizationFuncs.editWorkflow(selenium, iLocName, iWFName);
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
