package com.globalsight.selenium.testcases.smoketest;

import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.testng.Reporter;

import com.globalsight.selenium.functions.CommonFuncs;
import com.globalsight.selenium.functions.LocalePairsFuncs;
import com.globalsight.selenium.pages.MainFrame;
import com.globalsight.selenium.testcases.BaseTestCase;

public class RemoveLocalePairs extends BaseTestCase
{
    /*
     * Common variables initialization.
     */
    private LocalePairsFuncs localePairsFuncs = new LocalePairsFuncs();



    /**
     * 
     * Author Totti
     * Modify Jack
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
