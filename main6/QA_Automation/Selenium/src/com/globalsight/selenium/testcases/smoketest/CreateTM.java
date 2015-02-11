package com.globalsight.selenium.testcases.smoketest;

/*
 * TestCaseName: CreateTM.java
 * Author:Jester
 * Tests:Create_TM()
 * 
 * History:
 * Date       Comments       Updater
 * 2011-6-1  First Version  Jester
 */

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
import com.globalsight.selenium.functions.TMFuncs;
import com.globalsight.selenium.pages.MainFrame;
import com.globalsight.selenium.properties.ConfigUtil;
import com.globalsight.selenium.testcases.BaseTestCase;
import com.thoughtworks.selenium.Selenium;

public class CreateTM extends BaseTestCase{

	/*
	 * Common Variables
	 */
	private Selenium selenium;
	TMFuncs iTMFuncs = new TMFuncs();
	
	/*
	 * Create a new TM.
	 */
	@Test
	public void createandimportTM() throws Exception {
		selenium.click(MainFrame.Setup_MENU);
		selenium.click(MainFrame.TranslationMemory_SUBMENU);
		selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);

		iTMFuncs.newTM(selenium, ConfigUtil.getDataInCase(getClassName(), "TM"));
		iTMFuncs.newTM(selenium, ConfigUtil.getDataInCase(getClassName(), "TM2"));
		iTMFuncs.importTM(selenium, ConfigUtil.getDataInCase(getClassName(),"TMIMPORT"));
		
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
		try
        {
            Thread.sleep(15000);
        }
        catch (InterruptedException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
	}

	@BeforeSuite
	public void beforeSuite() {
	}

	@AfterSuite
	public void afterSuite() {
	}

}
