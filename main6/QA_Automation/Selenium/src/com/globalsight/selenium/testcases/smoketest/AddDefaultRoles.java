package com.globalsight.selenium.testcases.smoketest;

import org.testng.Assert;
import org.testng.Reporter;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.globalsight.selenium.functions.CommonFuncs;
import com.globalsight.selenium.functions.ProjectsFuncs;
import com.globalsight.selenium.functions.UsersFuncs;
import com.globalsight.selenium.functions.WorkflowsFuncs;
import com.globalsight.selenium.pages.MainFrame;
import com.globalsight.selenium.pages.Projects;
import com.globalsight.selenium.properties.ConfigUtil;
import com.globalsight.selenium.testcases.BaseTestCase;
import com.thoughtworks.selenium.Selenium;

public class AddDefaultRoles extends BaseTestCase {

	private Selenium selenium;
	private UsersFuncs iUsersFuncs = new UsersFuncs();
	private ProjectsFuncs iProjectsFuncs = new ProjectsFuncs();
	String testCaseName = getClass().getName();

	@Test
	public void addDefaultRolse() throws Exception {

		// Create a new project
		selenium.click(MainFrame.Projects_SUBMENU);
		selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
		selenium.click(Projects.New_BUTTON);
        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
        selenium.select(Projects.ProjectManager_SELECT, ConfigUtil.getConfigData("pm"));
		String projectname = iProjectsFuncs.newProject(selenium,
				ConfigUtil.getDataInCase(testCaseName, "project"));
		if (projectname != null) {
			Assert.assertEquals(iProjectsFuncs.isPresentInTable(selenium, Projects.Project_TABLE,
					projectname), true);
		} else {
			Reporter.log("the project creation failed!");
		}
			selenium.click(MainFrame.Workflows_SUBMENU);
			selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);		
			//import workflow
			WorkflowsFuncs iWorkflowsFuncs = new WorkflowsFuncs();
			iWorkflowsFuncs.importWorkFlow(selenium,
					ConfigUtil.getPath(getClassName(), "FILEPATH"),
					ConfigUtil.getDataInCase(getClassName(), "IMPORTPROFILE"),"wf_suser_en_US_de_DE");
			//Create Superusers
			CommonFuncs.logoutSystem(selenium);
			CommonFuncs.loginSystemWithSuperAdmin(selenium);
			selenium.click(MainFrame.Setup_MENU);
			selenium.click(MainFrame.Users_SUBMENU);
			selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
			iUsersFuncs.newSuperUsers(selenium,
					ConfigUtil.getDataInCase(testCaseName, "USER"));
			//Edit DefaultRoles
			iUsersFuncs.editDefaultRoles(selenium,
					ConfigUtil.getDataInCase(testCaseName, "iDefaulRole"),
					ConfigUtil.getDataInCase(testCaseName, "sourcelocale"),
					ConfigUtil.getDataInCase(testCaseName, "targetlocale"));

			CommonFuncs.logoutSystem(selenium);
			CommonFuncs.loginSystemWithAdmin(selenium);
			
			selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
			selenium.click(MainFrame.Projects_SUBMENU);
			selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
			if (iProjectsFuncs.searchProject(selenium,
					ConfigUtil.getDataInCase(testCaseName, "project")) == true) {
				selenium.click(Projects.User_BUTTON);
				selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
				selenium.click(Projects.Done_User_BUTTON);
				selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
				selenium.click(Projects.Save_BUTTON);
				selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
			}

			CommonFuncs.logoutSystem(selenium);
			CommonFuncs.loginSystemWithSuperAdmin(selenium);

			// Verify Default Roles feature works.
			selenium.click(MainFrame.Setup_MENU);
			selenium.click(MainFrame.Users_SUBMENU);
			selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
			iUsersFuncs.verifyRoles(selenium,
					ConfigUtil.getDataInCase(testCaseName, "USER"),
					ConfigUtil.getDataInCase(testCaseName, "sourcelocale"),
					ConfigUtil.getDataInCase(testCaseName, "targetlocale"));
		
	}

	@BeforeMethod
	public void beforeMethod() {
	    CommonFuncs.loginSystemWithAdmin(selenium);
	 		
	}

	@AfterMethod
	public void afterMethod() {
		CommonFuncs.logoutSystem(selenium);
	}

	@BeforeTest
	public void beforeTest() {
		selenium = CommonFuncs.initSelenium();
	}

	@AfterTest
	public void afterTest() {
		CommonFuncs.endSelenium(selenium);
	}

}
