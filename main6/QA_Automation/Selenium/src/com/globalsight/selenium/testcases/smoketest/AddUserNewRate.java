package com.globalsight.selenium.testcases.smoketest;
//author :ShenYang  2011-07-02
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
import com.globalsight.selenium.functions.RatesFuncs;
import com.globalsight.selenium.functions.UsersFuncs;
import com.globalsight.selenium.pages.MainFrame;
import com.globalsight.selenium.pages.Users;
import com.globalsight.selenium.properties.ConfigUtil;
import com.globalsight.selenium.testcases.BaseTestCase;
import com.thoughtworks.selenium.Selenium;

public class AddUserNewRate extends BaseTestCase{
	private Selenium selenium;
	private RatesFuncs a = new RatesFuncs();
	private UsersFuncs uf = new UsersFuncs();
@Test	
	public void aUserRate() throws Exception {
		selenium.click(MainFrame.Setup_MENU);
		selenium.click(MainFrame.Users_SUBMENU);
		selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
		
		String src = ConfigUtil.getDataInCase(getClassName(), "Source");
		String tar = ConfigUtil.getDataInCase(getClassName(), "Target");
		String iUserName = ConfigUtil.getConfigData("admin_login_name");
		a.addUserNewRate(selenium, iUserName);
		selenium.select(Users.SrcLocale_SELECT, src);
		selenium.addSelection(Users.TarLocale_SELECT, tar);
		selenium.fireEvent(Users.TarLocale_SELECT, "blur");   //Use this statement to implement lost focus.
		Thread.sleep(5000);
		
		EditRates tempClass = new EditRates();
		String newRateName = ConfigUtil.getDataInCase(tempClass.getClassName(), "New_RATENAME");
		
		
		selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
		a.selectRadioButtonFromTable(selenium, Users.NewRole_TABLE, "Dtp1", 1);
		String comid = uf.companyID(selenium, Users.NewRole_TABLE, 1);
		selenium.select("Dtp1_"+comid+"_expense", "label="+newRateName);  
		selenium.click(Users.NewRolw_Done);
		selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
		selenium.click(Users.Roles_Done_BUTTON);
		selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
		selenium.click(Users.EditUser_Save_BUTTON);
		selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
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
	


