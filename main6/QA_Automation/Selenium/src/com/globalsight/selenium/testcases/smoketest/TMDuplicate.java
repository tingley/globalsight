package com.globalsight.selenium.testcases.smoketest;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.globalsight.selenium.functions.CommonFuncs;
import com.globalsight.selenium.functions.TMFuncs;
import com.globalsight.selenium.pages.MainFrame;
import com.globalsight.selenium.pages.TMManagement;
import com.globalsight.selenium.testcases.BaseTestCase;

public class TMDuplicate extends BaseTestCase
{
    TMFuncs iTMFuncs = new TMFuncs();

    @Test
    public void verifyDuplicate() throws Exception
    {
        openMenuItemAndWait(selenium, MainFrame.SETUP_MENU,
                MainFrame.TRANSLATION_MEMORY_SUBMENU);

        iTMFuncs.selectRadioButtonFromTable(selenium,
                TMManagement.TM_MANAGEMENT_TABLE, getProperty("tm.tmName"));

        clickAndWait(selenium, TMManagement.DUPLICATE_BUTTON);

        String duplicateName = getProperty("tm.duplicate.name");
        selenium.type(TMManagement.DUPLICATE_NAME_TEXT, duplicateName);
        selenium.click(TMManagement.DUPLICATE_OK_BUTTON);

        if (selenium.isAlertPresent())
        {
            selenium.getAlert();
            selenium.click(TMManagement.DUPLICATE_CANCEL_BUTTON);
            selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
        }
        else
        {
            selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
        }

        Assert.assertTrue(iTMFuncs.isPresentInTable(selenium,
                TMManagement.TM_MANAGEMENT_TABLE, duplicateName));
    }
}
