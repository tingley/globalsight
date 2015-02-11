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
import com.globalsight.selenium.pages.MainFrame;
import com.globalsight.selenium.properties.ConfigUtil;
import com.globalsight.selenium.testcases.BaseTestCase;
import com.thoughtworks.selenium.Selenium;

//Created by Shenyang 2011-6-22

public class RemoveActivityType extends BaseTestCase{
	  /*
     * Common variables initialization.
     */
    private Selenium selenium;
    private ActivityTypeFuncs rActivityTypeFuncs = new ActivityTypeFuncs();

    /*
     * Remove "ACTIVITYNAME" type
     */
  @Test
  public void removeActivityType() throws Exception{
	  selenium.click(MainFrame.Setup_MENU);
      selenium.click(MainFrame.ActivityTypes_SUBMENU);
      selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
      
      String iActivtiyName = ConfigUtil.getDataInCase(getClassName(), "ACTIVITYNAME");
      rActivityTypeFuncs.ActivityTypeRemove(selenium, iActivtiyName);
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
  
}
