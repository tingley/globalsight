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
 * TestCaseName: MyJobsAllStatusVerify.java 
 * Author:Jester
 * Tests:verifyJobsAllStatus()
 * 
 * History: Date Comments Updater 
 * 2011-6-24 First Version Jester
 */

public class MyJobsAllStatusVerify extends BaseTestCase
{
    /*
     * Common Variables
     */

    @Test
    public void verifyJobsAllStatus()
    {
        openMenuItemAndWait(selenium, MainFrame.MY_JOBS_MENU,
                MainFrame.MY_JOBS_ALL_STATUS_SUBMENU);

        Assert.assertFalse(selenium.isElementPresent(MyJobs.ALL_BUTTONS_BUTTON));
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
