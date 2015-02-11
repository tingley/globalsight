package com.globalsight.selenium.testcases.smoketest;

/*
 * TestCaseName: CreateTMProfile.java
 * Author:Jester
 * Tests:Create_TMProfile()
 * 
 * History:
 * Date       Comments       Updater
 * 2011-6-1  First Version  Jester
 */

import org.testng.annotations.Test;
import com.globalsight.selenium.functions.TMProfileFuncs;
import com.globalsight.selenium.pages.MainFrame;
import com.globalsight.selenium.testcases.BaseTestCase;

public class CreateTMProfile extends BaseTestCase
{

    /*
     * Common Variables
     */
    TMProfileFuncs tmProfileFuncs = new TMProfileFuncs();

    @Test
    public void createTMProfile() throws Exception
    {

        openMenuItemAndWait(selenium, MainFrame.SETUP_MENU,
                MainFrame.TRANSLATION_MEMORY_PROFILES_SUBMENU);

        tmProfileFuncs.newTMProfile(selenium, getProperty("tmprofile.TMProfile"));
    }
}
