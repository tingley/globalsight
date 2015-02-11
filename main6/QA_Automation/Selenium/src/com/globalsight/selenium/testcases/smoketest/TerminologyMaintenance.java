package com.globalsight.selenium.testcases.smoketest;
//author : ShenYang  2011-07-04

import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import com.globalsight.selenium.functions.CommonFuncs;
import com.globalsight.selenium.functions.TerminologyFuncs;
import com.globalsight.selenium.pages.MainFrame;
import com.globalsight.selenium.properties.ConfigUtil;
import com.globalsight.selenium.testcases.BaseTestCase;
import com.thoughtworks.selenium.Selenium;

public class TerminologyMaintenance extends BaseTestCase{
	private Selenium selenium;
	private TerminologyFuncs maintain = new TerminologyFuncs();
	
	@Test	
	public void tbMaintain() throws Exception{
	selenium.click(MainFrame.Setup_MENU);
	selenium.click(MainFrame.Terminology_SUBMENU);
	String iTBName = ConfigUtil.getDataInCase(getClassName(), "TB_NAME");
	String fieldName = ConfigUtil.getDataInCase(getClassName(), "FIELD_NAME");
	String searchStr = ConfigUtil.getDataInCase(getClassName(), "SEARCH_STRING");
	String newStr = ConfigUtil.getDataInCase(getClassName(), "NEW_STRING");
	maintain.maintenance(selenium, iTBName, fieldName, searchStr, newStr);
	
	}
	@BeforeMethod
	public void beforeMethod() {CommonFuncs.loginSystemWithAdmin(selenium);
	}

	@AfterMethod
	public void afterMethod() {CommonFuncs.logoutSystem(selenium);
	}
	
	@BeforeTest
	public void beforeTest() {selenium = CommonFuncs.initSelenium();
	}

	@AfterTest
	public void afterTest() {CommonFuncs.endSelenium(selenium);
	}

}
