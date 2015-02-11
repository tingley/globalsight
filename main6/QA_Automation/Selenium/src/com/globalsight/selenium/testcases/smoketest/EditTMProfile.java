package com.globalsight.selenium.testcases.smoketest;

import org.testng.annotations.Test;
import com.globalsight.selenium.functions.TMProfileFuncs;
import com.globalsight.selenium.pages.MainFrame;
import com.globalsight.selenium.testcases.BaseTestCase;

public class EditTMProfile extends BaseTestCase
{

    /*
     * Common Variables
     */
    TMProfileFuncs tmProfileFuncs = new TMProfileFuncs();

    @Test
    public void createMTOptions() throws Exception
    {
        openMenuItemAndWait(selenium, MainFrame.SETUP_MENU,
                MainFrame.TRANSLATION_MEMORY_PROFILES_SUBMENU);

        tmProfileFuncs.editTMProfile(selenium, getProperty("tmprofile.edit"));
    }
}
