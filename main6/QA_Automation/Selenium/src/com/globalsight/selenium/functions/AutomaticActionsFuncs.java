package com.globalsight.selenium.functions;

import org.testng.Assert;
import org.testng.Reporter;

import com.globalsight.selenium.pages.AutomaticActions;
import com.thoughtworks.selenium.Selenium;

/*
 * FileName: AutomaticActionsFuncs.java
 * Author:Jester
 * Methods: AutomaticActionsNew() 
 * 
 */

public class AutomaticActionsFuncs extends BasicFuncs
{
    /**
     * Create a new automatic actions.
     */
    public void create(Selenium selenium, String name, String email,
            String descripton) throws Exception
    {
        selenium.click(AutomaticActions.NEW_BUTTON);
        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);

        selenium.type(AutomaticActions.NAME_TEXT, name);
        selenium.type(AutomaticActions.EMAIL_TEXT, email);
        selenium.type(AutomaticActions.DESCRIPTION_TEXT, descripton);

        selenium.click(AutomaticActions.SAVE_BUTTON);
        if (selenium.isAlertPresent()) {
            selenium.getAlert();
            selenium.click(AutomaticActions.CANCEL_BUTTON);
            selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
        }
        
        if (name != null)
        {
            Assert.assertEquals(this.isPresentInTable(selenium,
                    AutomaticActions.AUTOMATIC_ACTIONS_TABLE, name), true);
        }
    }

    public void remove(Selenium selenium, String name) throws Exception
    {

        boolean result = selectRadioButtonFromTable(selenium,
                AutomaticActions.AUTOMATIC_ACTIONS_TABLE, name);
        if (!result)
        {
            Reporter.log("Cannot find the proper AutomaticAction to remove!");
            return;
        }
        else
        {
            selenium.click(AutomaticActions.REMOVE_BUTTON);
            if (selenium.isConfirmationPresent())
            {
                selenium.getConfirmation();
            }
            selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);

        }
        Assert.assertEquals(this.isPresentInTable(selenium,
                AutomaticActions.AUTOMATIC_ACTIONS_TABLE, name), false);
    }

    public void modify(Selenium selenium, String name, String newName)
            throws Exception
    {

        boolean result = selectRadioButtonFromTable(selenium,
                AutomaticActions.AUTOMATIC_ACTIONS_TABLE, name);
        if (!result)
        {
            Reporter.log("Cannot find proper AutomaticAction to edit!");
            return;
        }
        else
        {
            selenium.click(AutomaticActions.EDIT_BUTTON);
            selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);

            selenium.type(AutomaticActions.NAME_TEXT, newName);
        }
        selenium.click(AutomaticActions.SAVE_BUTTON);
        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
        if (selenium.isConfirmationPresent())
        {
            selenium.getConfirmation();
        }
        Assert.assertEquals(this.isPresentInTable(selenium,
                AutomaticActions.AUTOMATIC_ACTIONS_TABLE, newName), true);
    }

}
