package com.globalsight.selenium.testcases.smoketest;

import org.testng.annotations.Test;

import com.globalsight.selenium.functions.TMFuncs;
import com.globalsight.selenium.pages.MainFrame;
import com.globalsight.selenium.pages.TMManagement;
import com.globalsight.selenium.testcases.BaseTestCase;

public class TMMaintenance extends BaseTestCase
{
    TMFuncs iTMFuncs = new TMFuncs();

    @Test
    public void verifyMaintenance() throws Exception
    {
        openMenuItemAndWait(selenium, MainFrame.SETUP_MENU,
                MainFrame.TRANSLATION_MEMORY_SUBMENU);

        iTMFuncs.selectRadioButtonFromTable(selenium,
                TMManagement.TM_MANAGEMENT_TABLE, getProperty("tm.tmName"));

        clickAndWait(selenium, TMManagement.MAINTENANCE_BUTTON);

        selenium.type(TMManagement.SEARCH_TEXT, getProperty("tm.search"));
        selenium.select(TMManagement.SOURCE_LOCALE_SELECT,
                getProperty("tm.maintenance.sourceLocale"));
        selenium.select(TMManagement.TARGET_LOCALE_SELECT,
                getProperty("tm.maintenance.targetLocale"));

        clickAndWait(selenium, TMManagement.NEXT_BUTTON);

        Thread.sleep(15000);

        clickAndWait(selenium, TMManagement.CONTINUE_BUTTON);

        clickAndWait(selenium, TMManagement.CANCEL_TOP_BUTTON);
    }
}
