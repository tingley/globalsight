package com.globalsight.selenium.testcases.smoketest;

import org.testng.annotations.Test;

import com.globalsight.selenium.functions.AutomaticActionsFuncs;
import com.globalsight.selenium.pages.MainFrame;
import com.globalsight.selenium.testcases.BaseTestCase;

public class EditAutomaticActions extends BaseTestCase
{
    private AutomaticActionsFuncs automaticActionsFuncs = new AutomaticActionsFuncs();

    @Test
    public void editAutomaticAction() throws Exception
    {
        openMenuItemAndWait(selenium, MainFrame.SETUP_MENU,
                MainFrame.AUTOMATIC_ACTIONS_SUBMENU);

        String actionName = getProperty("autoActions.name");
        String newName = getProperty("autoActions.newName");
        automaticActionsFuncs
                .modify(selenium, actionName, newName);
    }
}
