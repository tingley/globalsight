package com.globalsight.selenium.testcases.smoketest;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.globalsight.selenium.functions.CommonFuncs;
import com.globalsight.selenium.functions.TMFuncs;
import com.globalsight.selenium.pages.MainFrame;
import com.globalsight.selenium.pages.TMManagement;
import com.globalsight.selenium.testcases.BaseTestCase;

public class TMStatistic extends BaseTestCase
{
    TMFuncs iTMFuncs = new TMFuncs();

    @Test
    public void getStaticsTM() throws Exception
    {
        selenium.click(MainFrame.SETUP_MENU);
        selenium.click(MainFrame.TRANSLATION_MEMORY_SUBMENU);
        selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
        openMenuItemAndWait(selenium, MainFrame.SETUP_MENU,
                MainFrame.TRANSLATION_MEMORY_SUBMENU);

        String tmName = getProperty("tm.tmName");

        iTMFuncs.statistic(selenium, tmName);

        Assert.assertEquals(selenium.getText(TMManagement.TotalTUs_TEXT_FIELD),
                "41");
        Assert.assertEquals(
                selenium.getText(TMManagement.TotalTUVs_TEXT_FIELD), "119");

        selenium.click(TMManagement.Close_BUTTON);
        selenium.selectWindow(null);

    }
}
