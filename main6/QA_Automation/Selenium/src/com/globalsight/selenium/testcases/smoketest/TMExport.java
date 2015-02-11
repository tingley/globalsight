package com.globalsight.selenium.testcases.smoketest;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.globalsight.selenium.functions.CommonFuncs;
import com.globalsight.selenium.functions.TMFuncs;
import com.globalsight.selenium.pages.MainFrame;
import com.globalsight.selenium.pages.TMManagement;
import com.globalsight.selenium.testcases.BaseTestCase;

public class TMExport extends BaseTestCase
{
    TMFuncs iTMFuncs = new TMFuncs();

    @Test
    public void VerifyExport() throws Exception
    {
        openMenuItemAndWait(selenium, MainFrame.SETUP_MENU,
                MainFrame.TRANSLATION_MEMORY_SUBMENU);

        iTMFuncs.selectRadioButtonFromTable(selenium,
                TMManagement.TM_MANAGEMENT_TABLE, getProperty("tm.tmName"));

        clickAndWait(selenium, TMManagement.EXPORT_BUTTON);

        clickAndWait(selenium, TMManagement.EXPORT_NEXT_BUTTON);
        
        clickAndWait(selenium, TMManagement.EXPORT_NEXT_BUTTON);

        selenium.waitForCondition("var imsg=selenium.getText(\""
                + TMManagement.EXPORT_PROGRESS_MSG
                + "\"); imsg==\"41 entries (100%)\"", CommonFuncs.SHORT_WAIT);
        Assert.assertEquals(selenium.getText(TMManagement.EXPORT_MESSAGE),
                getProperty("tm.export.verify"));
        selenium.click(TMManagement.DOWNLOAD_FILE_BUTTON);
        Thread.sleep(5000);
        clickAndWait(selenium, TMManagement.EXPORT_OK_BUTTON);
    }
}
