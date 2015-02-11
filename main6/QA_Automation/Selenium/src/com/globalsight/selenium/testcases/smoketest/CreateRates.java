package com.globalsight.selenium.testcases.smoketest;

/*
 * TestCaseName: CreateRates.java
 * Author:Jester
 * Tests:Create_Rates()
 * 
 * History:
 * Date       Comments       Updater
 * 2011-6-8  First Version  Jester
 */

import org.testng.annotations.Test;
import com.globalsight.selenium.functions.RatesFuncs;
import com.globalsight.selenium.pages.MainFrame;
import com.globalsight.selenium.testcases.BaseTestCase;

public class CreateRates extends BaseTestCase
{

    private RatesFuncs ratesFuncs = new RatesFuncs();

    @Test
    public void createRates() throws Exception
    {
        openMenuItemAndWait(selenium, MainFrame.SETUP_MENU,
                MainFrame.RATES_SUBMENU);

        ratesFuncs.newRate(selenium, getProperty("rate.fixedRate"));
        ratesFuncs.newRate(selenium, getProperty("rate.hourlyRate"));
        ratesFuncs.newRate(selenium, getProperty("rate.pageRate"));
        ratesFuncs.newRate(selenium, getProperty("rate.wordCountRate"));
        ratesFuncs.newRate(selenium, getProperty("rate.WordCountByPercent"));
    }
}
