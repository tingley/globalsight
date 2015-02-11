package com.globalsight.selenium.testcases.smoketest;

import org.testng.annotations.Test;
import com.globalsight.selenium.functions.CommonFuncs;
import com.globalsight.selenium.functions.UsersFuncs;
import com.globalsight.selenium.pages.MainFrame;
import com.globalsight.selenium.testcases.BaseTestCase;

public class CreateSuperUsers extends BaseTestCase
{
    /*
     * Common variables initialization.
     */
    private UsersFuncs usersFuncs = new UsersFuncs();

    @Test
    public void createSuperUsers() throws Exception
    {
        CommonFuncs.loginSystemWithSuperAdmin(selenium);

        openMenuItemAndWait(selenium, MainFrame.SETUP_MENU,
                MainFrame.USERS_SUBMENU);

        usersFuncs.newSuperUsers(selenium, getProperty("user.superUser"));

        CommonFuncs.logoutSystem(selenium);
        CommonFuncs.loginSystemWithAdmin(selenium);
    }
}
