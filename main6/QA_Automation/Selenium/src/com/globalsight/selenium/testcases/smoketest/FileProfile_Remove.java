package com.globalsight.selenium.testcases.smoketest;

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
import com.globalsight.selenium.functions.FileProfileFuncs;
import com.globalsight.selenium.pages.MainFrame;
import com.globalsight.selenium.properties.ConfigUtil;
import com.globalsight.selenium.testcases.BaseTestCase;
import com.thoughtworks.selenium.Selenium;

public class FileProfile_Remove extends BaseTestCase
{

    private Selenium selenium;
    private FileProfileFuncs iFileProfileFuncs = new FileProfileFuncs();;
       
 
       
    @Test
    public void removeFileProfile() throws Exception
    {
    	selenium.click(MainFrame.DataSources_MENU);
        selenium.click(MainFrame.FileProfiles_SUBMENU);
        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
        iFileProfileFuncs.remove(selenium,ConfigUtil.getDataInCase(getClassName(), "fileProfileNames"));
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
