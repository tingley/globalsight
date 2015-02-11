package com.globalsight.selenium.testcases.smoketest;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.globalsight.selenium.functions.CommonFuncs;
import com.globalsight.selenium.functions.UsersFuncs;
import com.globalsight.selenium.pages.MainFrame;
import com.globalsight.selenium.testcases.ConfigUtil;
import com.globalsight.selenium.testcases.BaseTestCase;

public class CreateUsers extends BaseTestCase
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

        String user1, user2;

        user1 = usersFuncs.newUsers(selenium, getProperty("user.user1"));
        user2 = usersFuncs.newUsers(selenium, getProperty("user.user2"));

        CommonFuncs.logoutSystem(selenium);

        CommonFuncs.login(selenium, user1,
                ConfigUtil.getConfigData("anyonePassword"));
        Assert.assertEquals(selenium.isElementPresent(MainFrame.LOG_OUT_LINK),
                true);

        CommonFuncs.logoutSystem(selenium);

        CommonFuncs.login(selenium, user2,
                ConfigUtil.getConfigData("anyonePassword"));
        Assert.assertEquals(selenium.isElementPresent(MainFrame.LOG_OUT_LINK),
                true);
        CommonFuncs.loginSystemWithAdmin(selenium);
        /*
         * CommonFuncs.login(selenium, user3,
         * ConfigUtil.getConfigData("anyone_password"));
         * Assert.assertEquals(selenium.isElementPresent(MainFrame.LogOut_LINK),
         * true); selenium.click(MainFrame.LogOut_LINK);
         * 
         * CommonFuncs.login(selenium, user4,
         * ConfigUtil.getConfigData("anyone_password"));
         * Assert.assertEquals(selenium.isElementPresent(MainFrame.LogOut_LINK),
         * true);
         */
    }
}
