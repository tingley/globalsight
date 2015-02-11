package com.globalsight.selenium.testcases.smoketest;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.globalsight.selenium.functions.TMFuncs;
import com.globalsight.selenium.pages.MainFrame;
import com.globalsight.selenium.pages.TMManagement;
import com.globalsight.selenium.testcases.BaseTestCase;

public class TMEdit extends BaseTestCase
{
    TMFuncs iTMFuncs = new TMFuncs();

    @Test
    public void verifyEdit() throws Exception
    {
        openMenuItemAndWait(selenium, MainFrame.SETUP_MENU,
                MainFrame.TRANSLATION_MEMORY_SUBMENU);

        iTMFuncs.selectRadioButtonFromTable(selenium,
                TMManagement.TM_MANAGEMENT_TABLE,
                getProperty("tm.duplicate.name"));

        clickAndWait(selenium, TMManagement.EDIT_BUTTON);

        String editMsg = getProperty("tm.edit");
        selenium.type(TMManagement.DOMAIN_TEXT, editMsg);
        selenium.type(TMManagement.ORGANIZATION_TEXT, editMsg);
        selenium.type(TMManagement.DESCRIPTION_TEXT, editMsg);

        clickAndWait(selenium, TMManagement.SAVE_BUTTON);

        Assert.assertTrue(iTMFuncs.isPresentInTable(selenium,
                TMManagement.TM_MANAGEMENT_TABLE, editMsg, 3));
    }
}
