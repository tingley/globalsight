package com.globalsight.selenium.testcases.smoketest;

import org.testng.annotations.Test;

import com.globalsight.selenium.functions.LocalizationFuncs;

import com.globalsight.selenium.pages.MainFrame;
import com.globalsight.selenium.testcases.BaseTestCase;

public class RemoveWFofLocalizationProfile extends BaseTestCase
{
    private LocalizationFuncs localizationFuncs = new LocalizationFuncs();

    @Test
    public void removeWorkflow() throws Exception
    {
        openMenuItemAndWait(selenium, MainFrame.SETUP_MENU,
                MainFrame.LOCALIZATION_PROFILES_SUBMENU);

        String iLocName = getProperty("localization.name").trim();
        String iWFName = getProperty("localization.workflow").trim();
        String iWFLangCode=getProperty("localization.workflow.targetCode").trim();
        
        
        localizationFuncs.removeWorkflow(selenium, iLocName, iWFLangCode, iWFName);

    }
}
