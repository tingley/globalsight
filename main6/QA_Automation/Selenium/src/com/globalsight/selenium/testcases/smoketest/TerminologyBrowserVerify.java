package com.globalsight.selenium.testcases.smoketest;

import junit.framework.Assert;

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
import com.globalsight.selenium.functions.TerminologyFuncs;
import com.globalsight.selenium.pages.MainFrame;
import com.globalsight.selenium.pages.TerminologyElements;
import com.globalsight.selenium.properties.ConfigUtil;
import com.globalsight.selenium.testcases.BaseTestCase;
import com.thoughtworks.selenium.Selenium;

/*
 * TestCaseName: TerminologyBrowserVerify.java
 * Author:Jester
 * Tests:verifyTerminologyBrowser()
 * 
 * History:
 * Date       Comments       Updater
 * 2011-6-30  First Version  Jester
 */
public class TerminologyBrowserVerify extends BaseTestCase {
	
	/*
	 * Common Variables. 
	 */
	private Selenium selenium;
	TerminologyFuncs iTerminologyFuncs=new TerminologyFuncs();
	
  @Test
  public void verifyTerminologyBrowser() throws Exception {
	  
	  selenium.click(MainFrame.Setup_MENU);
	  selenium.click(MainFrame.Terminology_SUBMENU);
	  selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
	  
	  iTerminologyFuncs.selectRadioButtonFromTable(selenium, TerminologyElements.MAIN_TABLE, ConfigUtil.getDataInCase(getClassName(), "TERMINOLOGYNAME"));
	  
	  selenium.click(TerminologyElements.MAIN_BROWSER_BUTTON);
	  selenium.waitForPopUp(TerminologyElements.TermbaseViewer_TAG, CommonFuncs.SHORT_WAIT);
	  selenium.selectWindow("name="+TerminologyElements.TermbaseViewer_TAG);
	  selenium.select(TerminologyElements.Source_SELECT, ConfigUtil.getDataInCase(getClassName(), "SOURCE"));
	  selenium.select(TerminologyElements.Target_SELECT, ConfigUtil.getDataInCase(getClassName(), "TARGET"));
	  selenium.type(TerminologyElements.Query_TEXT_FIELD, ConfigUtil.getDataInCase(getClassName(), "QUERY"));
	  selenium.select(TerminologyElements.SearchType_SELECT, ConfigUtil.getDataInCase(getClassName(), "SEARCHTYPE"));
	  selenium.click(TerminologyElements.Execute_BUTTON);
	  
	  Thread.sleep(100);
	  selenium.click(TerminologyElements.TermsFound_LIST+"/li/span");
	  Assert.assertTrue(selenium.getText(TerminologyElements.TermDetails_TEXT_FIELD).contains(ConfigUtil.getDataInCase(getClassName(), "CONTAINSVERIFY")));
	  selenium.click(TerminologyElements.CloseTermbase_IMG);
	  selenium.selectWindow(null);	  
	  
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
	  selenium=CommonFuncs.initSelenium();
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
