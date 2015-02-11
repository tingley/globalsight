package com.globalsight.selenium.testcases.smoketest;


import org.testng.annotations.Test;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.AfterSuite;

import com.globalsight.selenium.functions.ActivityTypeFuncs;
import com.globalsight.selenium.functions.CommonFuncs;
import com.globalsight.selenium.pages.ActivityType;
import com.globalsight.selenium.pages.MainFrame;
import com.globalsight.selenium.properties.ConfigUtil;
import com.globalsight.selenium.testcases.BaseTestCase;
import com.thoughtworks.selenium.Selenium;

//Created by Shenyang 2011-6-22

public class EditActivityType extends BaseTestCase{
	  /*
     * Common variables initialization.
     */
    private Selenium selenium;
    private ActivityTypeFuncs eActivityTypeFuncs = new ActivityTypeFuncs();

    /*
     * Edit "ACTIVITYNAME" type to the new type--ReviewOnly
     */
  @Test
  public void editActivityType() throws Exception{
	  selenium.click(MainFrame.Setup_MENU);
      selenium.click(MainFrame.ActivityTypes_SUBMENU);
      selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
      
      String iActivtiyName = ConfigUtil.getDataInCase(getClassName(), "ACTIVITYNAME");
      
      /*boolean isPresent = eActivityTypeFuncs.isPresentInTable(selenium, ActivityType.Activity_TABLE, iActivtiyName);
      if(!isPresent){
    	  Reporter.log("Cannot find the activityname:"+iActivtiyName+".");
          return;
      }
     
      String clickStr = "//input[@name='radioBtn' and @value='"+iActivtiyName+"_10']";
      selenium.click(clickStr);*/
      
      String typeStr = "Change as new type";
      eActivityTypeFuncs.ActivityTypeEdit(selenium, iActivtiyName, typeStr, ActivityType.ReviewOnly_RADIO);
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
