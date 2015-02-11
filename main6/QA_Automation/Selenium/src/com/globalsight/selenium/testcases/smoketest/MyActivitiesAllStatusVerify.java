package com.globalsight.selenium.testcases.smoketest;

import junit.framework.Assert;

import org.testng.annotations.Test;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.AfterMethod;
import com.globalsight.selenium.functions.CommonFuncs;
import com.globalsight.selenium.pages.MainFrame;
import com.globalsight.selenium.pages.MyActivities;
import com.globalsight.selenium.testcases.BaseTestCase;

/*
 * TestCaseName: MyActivityAllStatusVerify.java 
 * Author:Jester
 * Tests:verifyActivityAllStatus()
 * 
 * History: Date Comments Updater 
 * 2011-6-22 First Version Jester
 */

public class MyActivitiesAllStatusVerify extends BaseTestCase
{

    /**
     * Common variables
     */

    /**
     * Verify the button Detailed Word Counts and Export exist under all Status
     * page.
     */
    @Test
    public void verifyActivityFinished()
    {
        openMenuItemAndWait(selenium, MainFrame.MY_ACTIVITIES_MENU,
                MainFrame.MY_ACTIVITIES_ALL_STATUS_SUBMENU);

        Assert.assertTrue(selenium
                .isElementPresent(MyActivities.DETAILED_WORD_COUNTS_BUTTON));
        Assert.assertTrue(selenium.isElementPresent(MyActivities.EXPORT_BUTTON));
    }

    @BeforeMethod
    public void beforeMethod()
    {
        CommonFuncs.loginSystemWithPM(selenium);
    }

    @AfterMethod
    public void afterMethod()
    {
        CommonFuncs.logoutSystem(selenium);
    }
}
