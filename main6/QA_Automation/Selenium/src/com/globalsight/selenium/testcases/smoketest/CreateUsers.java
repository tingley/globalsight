package com.globalsight.selenium.testcases.smoketest;

import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.globalsight.selenium.functions.CommonFuncs;
import com.globalsight.selenium.functions.UsersFuncs;
import com.globalsight.selenium.pages.MainFrame;
import com.globalsight.selenium.properties.ConfigUtil;
import com.thoughtworks.selenium.Selenium;

public class CreateUsers
{
    /*
     * Common variables initialization.
     */
    private Selenium selenium;
    private UsersFuncs iUsersFuncs = new UsersFuncs();
    String testCaseName = getClass().getName();

    @Test
    public void createUsers() throws Exception
    {
        String user1, user2;
        selenium.click(MainFrame.Setup_MENU);
        selenium.click(MainFrame.Users_SUBMENU);
        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);

        user1 = iUsersFuncs.newUsers(selenium,
                ConfigUtil.getDataInCase(testCaseName, "USER1"));
        user2 = iUsersFuncs.newUsers(selenium,
                ConfigUtil.getDataInCase(testCaseName, "USER2"));
     
        CommonFuncs.login(selenium, user1,
                ConfigUtil.getConfigData("anyone_password"));
        Assert.assertEquals(selenium.isElementPresent(MainFrame.LogOut_LINK),
                true);
        selenium.click(MainFrame.LogOut_LINK);

        CommonFuncs.login(selenium, user2,
                ConfigUtil.getConfigData("anyone_password"));
        Assert.assertEquals(selenium.isElementPresent(MainFrame.LogOut_LINK),
                true);
        // selenium.click(MainFrame.LogOut_LINK);

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

    @BeforeMethod
    public void beforeMethod()
    {
        CommonFuncs.loginSystemWithAdmin(selenium);
    }

    @AfterMethod
    public void afterMethod()
    {
        CommonFuncs.logoutSystem(selenium);
    }

    @BeforeTest
    public void beforeTest()
    {
        selenium = CommonFuncs.initSelenium();
    }

    @AfterTest
    public void afterTest()
    {
        CommonFuncs.endSelenium(selenium);
    }

}
