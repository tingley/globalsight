package com.globalsight.selenium.testcases.smoketest;

import org.testng.Reporter;
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
import com.globalsight.selenium.pages.TerminologyElements;
import com.globalsight.selenium.properties.ConfigUtil;
import com.globalsight.selenium.testcases.BaseTestCase;
import com.thoughtworks.selenium.Selenium;


public class SearchTermforTermbase extends BaseTestCase {

	private Selenium selenium;
	private TerminologyFuncs eTerminology = new TerminologyFuncs();
	@Test
	public void searchTermForTermbase() throws Exception
	{
		selenium.click(MainFrame.Setup_MENU);
		selenium.click(MainFrame.Terminology_SUBMENU);
		selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
		
		selenium.click(TerminologyElements.MAIN_SEARCHTERMS_BUTTON);
		selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
	
		String testCaseName = getClass().getName();
		String iterm = ConfigUtil.getDataInCase(testCaseName, "Iterm");
		String sourcelocale = ConfigUtil.getDataInCase(testCaseName, "SourceLocale");
		String targetlocale = ConfigUtil.getDataInCase(testCaseName, "TargetLocale");
		String matchtype= ConfigUtil.getDataInCase(testCaseName, "MatchType");
		String selecttermbase = ConfigUtil.getDataInCase(testCaseName, "SelectTermbase");
		
		selenium.select(TerminologyElements.Source_Locale,"label="+sourcelocale.trim());
		selenium.select(TerminologyElements.Target_Locale,"label="+targetlocale.trim());	
		selenium.addSelection(TerminologyElements.Select_Termbases, "label="+selecttermbase.trim());
		selenium.select(TerminologyElements.MatchType,"label="+matchtype.trim());
		selenium.type(TerminologyElements.SearchForTerm,iterm);
		selenium.click(TerminologyElements.Search_Button);
		selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
		int num=1;
		
		String result = eTerminology.searchterms(selenium, iterm.trim(),num);
		Reporter.log(result);
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


