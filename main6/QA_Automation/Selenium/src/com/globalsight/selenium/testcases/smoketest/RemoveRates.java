package com.globalsight.selenium.testcases.smoketest;

import org.testng.annotations.Test;

import com.globalsight.selenium.functions.RatesFuncs;
import com.globalsight.selenium.pages.MainFrame;
import com.globalsight.selenium.testcases.BaseTestCase;

public class RemoveRates extends BaseTestCase
{
    private RatesFuncs rateFunc = new RatesFuncs();

    @Test
    public void remove() throws Exception
    {
        openMenuItemAndWait(selenium, MainFrame.SETUP_MENU,
                MainFrame.RATES_SUBMENU);

        String iRateName = getProperty("rate.toRemove");
        rateFunc.removeRate(selenium, iRateName);
    }
}
