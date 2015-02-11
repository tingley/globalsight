package com.globalsight.selenium.testcases.smoketest;
// file created by ShenYang  2011-06-24

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
import com.globalsight.selenium.functions.XMLRulesFuncs;
import com.globalsight.selenium.pages.MainFrame;
import com.globalsight.selenium.properties.ConfigUtil;
import com.globalsight.selenium.testcases.BaseTestCase;
import com.thoughtworks.selenium.Selenium;

public class EditXMLRule extends BaseTestCase{
	 private Selenium selenium;
	 private XMLRulesFuncs eXMLRule = new XMLRulesFuncs();
	
	
	    @Test
	    public void editXMRule() throws Exception {
	    	selenium.click(MainFrame.DataSources_MENU);
	        selenium.click(MainFrame.XMLRules_SUBMENU);	
	        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
	        
	        String iRuleName = ConfigUtil.getDataInCase(getClassName(), "NAME");
	        String iRule = ConfigUtil.getDataInCase(getClassName(), "RULE1");
	        String typeStr = "This is the new Rule.";
	        
	        eXMLRule.editXMLRule(selenium, iRuleName, typeStr, iRule);
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
