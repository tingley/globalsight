package com.globalsight.selenium.testcases.smoketest;

import org.testng.Assert;

import org.testng.annotations.Test;
import org.testng.Reporter;

import com.globalsight.selenium.functions.LocalePairsFuncs;
import com.globalsight.selenium.pages.MainFrame;
import com.globalsight.selenium.testcases.BaseTestCase;

public class CreateLocalePairs extends BaseTestCase
{
    /*
     * Common variables initialization.
     */
    private LocalePairsFuncs localePairsFuncs = new LocalePairsFuncs();

    @Test
    public void createLocalPairs()
    {
        openMenuItemAndWait(selenium, MainFrame.SETUP_MENU,
                MainFrame.LOCALE_PAIRS_SUBMENU);

        localePairsFuncs.newLocalPairs(selenium, getProperty("localePairs.sourceLocale"),
                getProperty("localePairs.targetLocale"));

        localePairsFuncs.newLocalPairs(selenium,
                getProperty("localePairs.sourceLocale0"), getProperty("localePairs.targetLocale0"));

        // Verify if there is at least one Locale Paris exists.
        Assert.assertEquals(
                selenium.isElementPresent("//input[@name='radioBtn']"), true);
    }

    /**
     * 
     * Author Totti
     */
    @Test
    public void removeLocalPairs() throws Exception
    {
        openMenuItemAndWait(selenium, MainFrame.SETUP_MENU,
                MainFrame.LOCALE_PAIRS_SUBMENU);

        localePairsFuncs.localPairsRemove(selenium,
                getProperty("localePairs.sourceLocale1"), getProperty("localePairs.targetLocale1"));
        Reporter.log("removed successfully");
    }
}
