package com.globalsight.selenium.testcases.smoketest;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.globalsight.selenium.functions.TMFuncs;
import com.globalsight.selenium.pages.MainFrame;
import com.globalsight.selenium.pages.TMManagement;
import com.globalsight.selenium.testcases.BaseTestCase;

public class TMRemove extends BaseTestCase
{
    TMFuncs iTMFuncs = new TMFuncs();

    @Test
    public void verifyRemove() throws Exception
    {
        openMenuItemAndWait(selenium, MainFrame.SETUP_MENU,
                MainFrame.TRANSLATION_MEMORY_SUBMENU);

        String tmName = getProperty("tm.duplicate.name");
        iTMFuncs.selectRadioButtonFromTable(selenium,
                TMManagement.TM_MANAGEMENT_TABLE, tmName);

        clickAndWait(selenium, TMManagement.REMOVE_BUTTON);

        selenium.click(TMManagement.ENTRIE_TM_RADIO);

        clickAndWait(selenium, TMManagement.REMOVE_OK_BUTTON);

        Thread.sleep(15000);

        clickAndWait(selenium, TMManagement.REMOVE2_OK_BUTTON);
        Assert.assertFalse(iTMFuncs.isPresentInTable(selenium,
                TMManagement.TM_MANAGEMENT_TABLE, tmName));
    }
}
