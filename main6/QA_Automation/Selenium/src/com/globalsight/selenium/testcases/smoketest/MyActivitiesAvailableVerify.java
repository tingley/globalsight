package com.globalsight.selenium.testcases.smoketest;

/*
 * TestCaseName: MyActivitiesAvailableVerify.java 
 * Author:Jester
 * Tests:verifyActivityAvailable()
 * 
 * History: Date Comments Updater 
 * 2011-6-22 First Version Jester
 */

import junit.framework.Assert;

import org.testng.annotations.Test;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.AfterMethod;
import com.globalsight.selenium.functions.CommonFuncs;
import com.globalsight.selenium.pages.MainFrame;
import com.globalsight.selenium.pages.MyActivities;
import com.globalsight.selenium.testcases.BaseTestCase;

public class MyActivitiesAvailableVerify extends BaseTestCase
{
    /*
     * Common Variables
     */

    @Test
    public void verifyActivityAvailable()
    {
        openMenuItemAndWait(selenium, MainFrame.MY_ACTIVITIES_MENU,
                MainFrame.MY_ACTIVITIES_AVAILABLE_SUBMENU);

        // Check if all the links and buttons are present.
        Assert.assertTrue(selenium
                .isElementPresent(MyActivities.CHECK_ALL_LINK));
        Assert.assertTrue(selenium
                .isElementPresent(MyActivities.CLEAR_ALL_LINK));
        Assert.assertTrue(selenium.isElementPresent(MyActivities.ACCEPT_ALL_BUTTON));
        Assert.assertTrue(selenium
                .isElementPresent(MyActivities.DETAILED_WORD_COUNTS_BUTTON));
        Assert.assertTrue(selenium
                .isElementPresent(MyActivities.DOWNLOAD_BUTTON));

        // Make sure bellow two buttons are disable.
        Assert.assertFalse(selenium
                .isEditable(MyActivities.DETAILED_WORD_COUNTS_BUTTON));
        
        // Check the first job.
        selenium.click(MyActivities.MY_ACTIVITIES_TABLE + "/tr[2]//input");

        // make sure bellow two buttons are changed to enable.
        Assert.assertTrue(selenium
                .isEditable(MyActivities.DETAILED_WORD_COUNTS_BUTTON));
    }

    @BeforeMethod
    public void beforeMethod()
    {
        CommonFuncs.loginSystemWithAnyone(selenium);
    }

    @AfterMethod
    public void afterMethod()
    {
        CommonFuncs.logoutSystem(selenium);
    }
}
