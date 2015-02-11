package com.globalsight.selenium.testcases.smoketest;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.globalsight.selenium.functions.CommonFuncs;
import com.globalsight.selenium.functions.TMFuncs;
import com.globalsight.selenium.pages.MainFrame;
import com.globalsight.selenium.pages.TMManagement;
import com.globalsight.selenium.testcases.BaseTestCase;

public class TMCorpusBrowser extends BaseTestCase
{
    TMFuncs iTMFuncs = new TMFuncs();

    @Test
    public void verifyCorpusBrowser() throws Exception
    {
        openMenuItemAndWait(selenium, MainFrame.SETUP_MENU,
                MainFrame.TRANSLATION_MEMORY_SUBMENU);

        iTMFuncs.selectRadioButtonFromTable(selenium,
                TMManagement.TM_MANAGEMENT_TABLE, getProperty("tm.tmName"));

        selenium.click(TMManagement.CorpusBrowser_BUTTON);
        selenium.waitForPopUp(TMManagement.CorpusBrowser_TAG,
                CommonFuncs.SHORT_WAIT);

        selenium.selectWindow("name=" + TMManagement.CorpusBrowser_TAG);
        selenium.click(TMManagement.FullText_RADIO);
        selenium.type(TMManagement.Search_TEXT_FIELD,
                getProperty("tm.search"));
        selenium.select(TMManagement.LocalePair_SELECT,
                getProperty("tm.corpus.locale"));
        selenium.addSelection(TMManagement.TranslationMemory_SELECTION,
                getProperty("tm.tmName"));

        selenium.click(TMManagement.Search_BUTTON);
        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);

        Assert.assertEquals(
                selenium.getText(TMManagement.StatusMessageTop_TEXT),
                getProperty("tm.corpus.verify"));
        selenium.click(TMManagement.Close_BUTTON_CORPUSBROWSER);
        selenium.selectWindow(null);

    }
}
