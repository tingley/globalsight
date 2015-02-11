package com.globalsight.selenium.testcases.smoketest;

import org.testng.annotations.Test;

import com.globalsight.selenium.functions.UsersFuncs;
import com.globalsight.selenium.pages.MainFrame;
import com.globalsight.selenium.testcases.BaseTestCase;

public class EditUserRoles extends BaseTestCase
{
    /*
     * Common variables initialization.
     */
    private UsersFuncs usersFuncs = new UsersFuncs();

    @Test
    public void createUsers() throws Exception
    {
        openMenuItemAndWait(selenium, MainFrame.SETUP_MENU,
                MainFrame.USERS_SUBMENU);

        usersFuncs.editUserRoles(selenium, getProperty("user.editUserRole"));
    }
}
