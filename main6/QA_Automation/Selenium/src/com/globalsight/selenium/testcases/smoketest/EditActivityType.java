package com.globalsight.selenium.testcases.smoketest;

import org.testng.annotations.Test;

import com.globalsight.selenium.functions.ActivityTypeFuncs;
import com.globalsight.selenium.pages.ActivityType;
import com.globalsight.selenium.pages.MainFrame;
import com.globalsight.selenium.testcases.BaseTestCase;

//Created by Shenyang 2011-6-22

public class EditActivityType extends BaseTestCase
{
    /*
     * Common variables initialization.
     */
    private ActivityTypeFuncs activityTypeFuncs = new ActivityTypeFuncs();

    @Test
    public void editActivityType() throws Exception
    {
        openMenuItemAndWait(selenium, MainFrame.SETUP_MENU,
                MainFrame.ACTIVITY_TYPE_SUBMENU);

        String activities = getProperty("activityType.activities");
        String newDesc = "Change as new type";
        for (String activity : activities.split(",")) {
            activityTypeFuncs.modify(selenium, activity, newDesc,
                    ActivityType.ReviewOnly_RADIO);
        }
    }
}
