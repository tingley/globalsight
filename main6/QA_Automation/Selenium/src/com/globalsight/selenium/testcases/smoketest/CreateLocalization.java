package com.globalsight.selenium.testcases.smoketest;

import org.testng.annotations.Test;

import com.globalsight.selenium.functions.LocalizationFuncs;
import com.globalsight.selenium.pages.MainFrame;
import com.globalsight.selenium.testcases.BaseTestCase;

public class CreateLocalization extends BaseTestCase
{

    private LocalizationFuncs localizationfuncs = new LocalizationFuncs();

    @Test
    public void create()
    {
        openMenuItemAndWait(selenium, MainFrame.SETUP_MENU,
                MainFrame.LOCALIZATION_PROFILES_SUBMENU);
        
        String lpName = getProperty("localization.name");
        String tmpName = getProperty("localization.tmProfile");

        localizationfuncs.create(selenium, lpName, tmpName);
    }
}
