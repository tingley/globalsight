package com.globalsight.selenium.testcases.smoketest;

import org.testng.annotations.Test;

import com.globalsight.selenium.functions.RatesFuncs;
import com.globalsight.selenium.pages.MainFrame;
import com.globalsight.selenium.testcases.BaseTestCase;

public class EditRates extends BaseTestCase
{
    private RatesFuncs rateFunc = new RatesFuncs();

    @Test
    public void edit() throws Exception
    {
        openMenuItemAndWait(selenium, MainFrame.SETUP_MENU,
                MainFrame.RATES_SUBMENU);

        String rateName = getProperty("rate.rateName");
        String newRateName = getProperty("rate.newRateName");
        String newCurrencyName = getProperty("rate.newCurrencyName");
        String newRateTypeName = getProperty("rate.newRateTypeName");

        rateFunc.editRate(selenium, rateName, newRateName, newCurrencyName,
                newRateTypeName);
    }
}
