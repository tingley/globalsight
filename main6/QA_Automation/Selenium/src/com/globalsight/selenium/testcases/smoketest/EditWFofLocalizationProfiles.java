package com.globalsight.selenium.testcases.smoketest;

//author Shenyang 2011-06-27

import org.testng.annotations.Test;
import com.globalsight.selenium.functions.LocalizationFuncs;
import com.globalsight.selenium.pages.MainFrame;
import com.globalsight.selenium.testcases.BaseTestCase;

//Created by Shenyang 2011-6-23

public class EditWFofLocalizationProfiles extends BaseTestCase
{
    /*
     * Common variables initialization.
     */
    LocalizationFuncs localizationFuncs = new LocalizationFuncs();

    /*
     * Edit workflow of LocalizationProfile
     */

    @Test
    public void editWorkflow() throws Exception
    {
        openMenuItemAndWait(selenium, MainFrame.SETUP_MENU,
                MainFrame.LOCALIZATION_PROFILES_SUBMENU);

        String localizationName = getProperty("localization.name");
        String iWFName = getProperty("localization.workflow");

        localizationFuncs.editWorkflow(selenium, localizationName, iWFName);
    }
}
