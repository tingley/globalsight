package com.globalsight.selenium.testcases.smoketest;

import org.testng.annotations.Test;

import com.globalsight.selenium.functions.ActivityTypeFuncs;
import com.globalsight.selenium.pages.MainFrame;
import com.globalsight.selenium.testcases.BaseTestCase;

public class CreateActivityType extends BaseTestCase
{
    /*
     * Common variables initialization.
     */
    private ActivityTypeFuncs activityTypeFuncs = new ActivityTypeFuncs();

    /*
     * Create three types Activity type: Translate, Review(Editable),GS Edition
     * Actions. And verify them have been created successfully.
     */
    @Test
    public void createActivityType() throws Exception
    {
        openMenuItemAndWait(selenium, MainFrame.SETUP_MENU,
                MainFrame.ACTIVITY_TYPE_SUBMENU);

        
        String activities = getProperty("activityType.activities");
        for (String activity : activities.split(",")) {
            activityTypeFuncs.create(selenium, activity, activity, activity);
        }
    }
}
