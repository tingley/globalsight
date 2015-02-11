package com.globalsight.selenium.testcases.smoketest;

import org.testng.annotations.Test;

import com.globalsight.selenium.functions.AutomaticActionsFuncs;
import com.globalsight.selenium.pages.MainFrame;
import com.globalsight.selenium.testcases.BaseTestCase;

public class RemoveAutomaticActions extends BaseTestCase
{
    private AutomaticActionsFuncs automaticActionsFuncs = new AutomaticActionsFuncs();

    @Test
    public void removeAutomaticAction() throws Exception
    {
        openMenuItemAndWait(selenium, MainFrame.SETUP_MENU,
                MainFrame.AUTOMATIC_ACTIONS_SUBMENU);

        automaticActionsFuncs.remove(selenium,
                getProperty("autoActions.newName"));
    }
}