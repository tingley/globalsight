package com.globalsight.selenium.testcases.smoketest;

/*
 * TestCaseName: CreateAutomaticActions.java
 * Author:Jester
 * Tests:Create_AutomaticAction()
 * 
 * History:
 * Date       Comments       Updater
 * 2011-6-1  First Version  Jester
 */
import org.testng.annotations.Test;
import com.globalsight.selenium.functions.AutomaticActionsFuncs;
import com.globalsight.selenium.pages.MainFrame;
import com.globalsight.selenium.testcases.BaseTestCase;

public class CreateAutomaticActions extends BaseTestCase
{
    /*
     * Common variables
     */
    AutomaticActionsFuncs automaticActionsFuncs = new AutomaticActionsFuncs();

    /*
     * Create a new AutomaticAction Test.
     */
    @Test
    public void createAutomaticAction() throws Exception
    {
        openMenuItemAndWait(selenium, MainFrame.SETUP_MENU,
                MainFrame.AUTOMATIC_ACTIONS_SUBMENU);
        
        String name = getProperty("autoActions.name");
        String email = getProperty("autoActions.email");
        String description = getProperty("autoActions.description");
        
        automaticActionsFuncs.create(selenium,
                name, email, description);
    }
}
