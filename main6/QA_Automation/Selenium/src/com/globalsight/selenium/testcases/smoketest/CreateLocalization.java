package com.globalsight.selenium.testcases.smoketest;

import org.testng.annotations.Test;

import com.globalsight.selenium.functions.LocalizationFuncs;
import com.globalsight.selenium.pages.MainFrame;
import com.globalsight.selenium.testcases.BaseTestCase;

public class CreateLocalization extends BaseTestCase
{

    private LocalizationFuncs localizationfuncs = new LocalizationFuncs();

    @Test
    public void create() throws Exception
    {
        openMenuItemAndWait(selenium, MainFrame.SETUP_MENU,
                MainFrame.LOCALIZATION_PROFILES_SUBMENU);
        
        String lpName = getProperty("localization.name");
        String tmpName = getProperty("localization.tmProfile");
        String targetLocalName=getProperty("localization.workflow");
        String targetLocalCode=getProperty("localization.workflow.targetCode");
        

        localizationfuncs.create(selenium, lpName, tmpName, targetLocalName, targetLocalCode);
    }
}
