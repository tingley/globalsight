package com.globalsight.selenium.functions;

import org.testng.Assert;
import org.testng.Reporter;

import com.globalsight.selenium.pages.ActivityType;
import com.thoughtworks.selenium.Selenium;

public class ActivityTypeFuncs extends BasicFuncs
{
    public void create(Selenium selenium, String name, String description,
            String type) throws Exception
    {
        selenium.click(ActivityType.New_BUTTON);
        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);

        selenium.type(ActivityType.Name_TEXT_FIELD, name);
        selenium.type(ActivityType.DESCRIPTION_TEXT, description);

        if ("Translate".equals(type))
            selenium.click(ActivityType.Translate_RADIO);
        else if ("ReviewEditable".equals(type))
            selenium.click(ActivityType.ReviewEditable_RADIO);
        else if ("GSEditionActions".equals(type))
            selenium.click(ActivityType.GSEditionActions_RADIO);
        else if ("ReviewOnly".equals(type))
            selenium.click(ActivityType.ReviewOnly_RADIO);
        else if ("AutomaticActions".equals(type))
            selenium.click(ActivityType.AutomaticActions_RADIO);

        selenium.click(ActivityType.SAVE_BUTTON);
        try
        {
            if (selenium.isAlertPresent()) {
                selenium.getAlert();
                selenium.click(ActivityType.Cancel_BUTTON);
                selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
            }
        }
        catch (Exception e)
        {
            selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
        }

        Assert.assertTrue(this.isPresentInTable(selenium,
                ActivityType.ACTIVITY_TABLE, name));
    }

    // author Shenyang 2011-6-22
    public void modify(Selenium selenium, String activtiyName,
            String description, String newActivityType) throws Exception
    {

        boolean selected = selectRadioButtonFromTable(selenium,
                ActivityType.ACTIVITY_TABLE, activtiyName);
        if (!selected)
        {
            Reporter.log("Cannot find a proper ActivityType to edit.");
            return;
        }

        try
        {
            selenium.click("link=" + activtiyName);
            selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);

            selenium.type(ActivityType.DESCRIPTION_TEXT, description);
            selenium.click(newActivityType);

            selenium.click(ActivityType.SAVE_BUTTON);
            selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
        }
        catch (Exception e)
        {
            Reporter.log(e.getMessage());
            //selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
        }
        Assert.assertEquals(this.getColumnText(selenium,
                ActivityType.ACTIVITY_TABLE, activtiyName, 3), description);
    }

    // author Shenyang 2011-6-22
    public void remove(Selenium selenium, String activityName) throws Exception
    {
        boolean selected = selectRadioButtonFromTable(selenium,
                ActivityType.ACTIVITY_TABLE, activityName);
        if (!selected)
        {
            Reporter.log("Cannot find a proper ActivityType to edit.");
            return;
        }

        try
        {
            selenium.click(ActivityType.REMOVE_BUTTON);
            Assert.assertEquals(
                    (selenium.getConfirmation()
                            .matches("^Are you sure you want to remove this Activity[\\s\\S]$")),
                    true);
            selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
        }
        catch (Exception e)
        {
            selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
        }

        Assert.assertEquals(this.isPresentInTable(selenium,
                ActivityType.ACTIVITY_TABLE, activityName), false);
    }
}