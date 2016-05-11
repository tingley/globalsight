package com.globalsight.selenium.testcases.smoketest;

import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import com.globalsight.selenium.functions.CommonFuncs;
import com.globalsight.selenium.functions.ProjectsFuncs;
import com.globalsight.selenium.functions.UsersFuncs;
import com.globalsight.selenium.pages.MainFrame;
import com.globalsight.selenium.pages.Projects;
import com.globalsight.selenium.pages.Users;
import com.globalsight.selenium.testcases.ConfigUtil;
import com.globalsight.selenium.testcases.BaseTestCase;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.AfterTest;

public class AddDefaultRoles extends BaseTestCase
{

    private UsersFuncs usersFuncs = new UsersFuncs();
    private ProjectsFuncs projectsFuncs = new ProjectsFuncs();

    @Test
    public void addDefaultRolse() throws Exception
    {
    	
    	 

        openMenuItemAndWait(selenium, MainFrame.SETUP_MENU,
                MainFrame.USERS_SUBMENU);
        String newUsername = usersFuncs.newSuperUsers(selenium, getProperty("defaultRole.user"));
        

//         Edit DefaultRoles
        usersFuncs.editDefaultRoles(selenium,
                newUsername,
                getProperty("defaultRole.sourceLocale"),
                getProperty("defaultRole.targetLocale"));



        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);

        // Verify Default Roles feature works.
        openMenuItemAndWait(selenium, MainFrame.SETUP_MENU,
                MainFrame.USERS_SUBMENU);
        usersFuncs.verifyRoles(selenium, newUsername,
                getProperty("defaultRole.sourceLocale"),
                getProperty("defaultRole.targetLocale"));

    }
   
    @BeforeTest
    private void beforeTest() {
        CommonFuncs.loginSystemWithSuperAdmin(selenium);
    }
    
    @AfterTest
    private void afterTest() {
    	if (selenium.isElementPresent("link=Logout"))
    		CommonFuncs.logoutSystem(selenium);
        CommonFuncs.loginSystemWithAdmin(selenium);
    }

    

}
