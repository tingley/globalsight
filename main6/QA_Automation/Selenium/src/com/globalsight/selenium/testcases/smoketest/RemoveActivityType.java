package com.globalsight.selenium.testcases.smoketest;

import org.testng.annotations.Test;

import com.globalsight.selenium.functions.ActivityTypeFuncs;
import com.globalsight.selenium.pages.MainFrame;
import com.globalsight.selenium.testcases.BaseTestCase;
import com.thoughtworks.selenium.Selenium;

//Created by Shenyang 2011-6-22

public class RemoveActivityType extends BaseTestCase
{
    /*
     * Common variables initialization.
     */
    private ActivityTypeFuncs rActivityTypeFuncs = new ActivityTypeFuncs();

    /*
     * Remove "ACTIVITYNAME" type
     */
    @Test
    public void removeActivityType() throws Exception
    {
        openMenuItemAndWait(selenium, MainFrame.SETUP_MENU,
                MainFrame.ACTIVITY_TYPE_SUBMENU);

        String activities = getProperty("activityType.activities");
        for (String activity : activities.split(","))
        {
            rActivityTypeFuncs.remove(selenium, activity);
        }
    }
}
