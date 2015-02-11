package com.globalsight.selenium.testcases.smoketest;

import junit.framework.Assert;

import org.testng.annotations.Test;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.AfterMethod;
import com.globalsight.selenium.functions.CommonFuncs;
import com.globalsight.selenium.pages.MainFrame;
import com.globalsight.selenium.pages.MyJobs;
import com.globalsight.selenium.testcases.BaseTestCase;

/*
 * TestCaseName: MyJobsReadyVerify.java 
 * Author:Jester
 * Tests:verifyJobsReady()
 * 
 * History: Date Comments Updater 
 * 2011-6-24 First Version Jester
 */

public class MyJobsReadyVerify extends BaseTestCase
{
    /*
     * Common Variables
     */

    /*
     * Verify MyJobs-->Ready page, the button and link status. Author:Jester
     */
    @Test
    public void verifyJobsReady()
    {
        openMenuItemAndWait(selenium, MainFrame.MY_JOBS_MENU,
                MainFrame.MY_JOBS_READY_SUBMENU);

        // Check the buttons or links are presented.
        Assert.assertTrue(selenium.isElementPresent(MyJobs.CHECK_ALL_LINK));
        Assert.assertTrue(selenium
                .isElementPresent(MyJobs.CHECK_ALL_PAGES_LINK));
        Assert.assertTrue(selenium.isElementPresent(MyJobs.CLEAR_ALL_LINK));
        Assert.assertTrue(selenium
                .isElementPresent(MyJobs.SEARCH_AND_REPLACE_BUTTON));
        Assert.assertTrue(selenium
                .isElementPresent(MyJobs.CHANGE_WF_MANAGER_BUTTON));
        Assert.assertTrue(selenium.isElementPresent(MyJobs.DISCARD_BUTTON));
        Assert.assertTrue(selenium.isElementPresent(MyJobs.DISPATCH_BUTTON));

        selenium.click(MyJobs.CHECK_ALL_LINK);
        selenium.click(MyJobs.CLEAR_ALL_LINK);

        // Check if the button Change Work flow Managers button is disabled.
        Assert.assertFalse(selenium.isEditable(MyJobs.CHANGE_WF_MANAGER_BUTTON));

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
