package com.globalsight.selenium.testcases.smoketest;

import org.testng.annotations.Test;
import com.globalsight.selenium.functions.CurrencyFuncs;
import com.globalsight.selenium.pages.MainFrame;
import com.globalsight.selenium.testcases.BaseTestCase;

public class CreateCurrency extends BaseTestCase
{

    /*
     * Common variables initialization.
     */
    private CurrencyFuncs currencyFuncs = new CurrencyFuncs();

    @Test
    public void createLocalPairs() throws Exception
    {
        openMenuItemAndWait(selenium, MainFrame.SETUP_MENU,
                MainFrame.CURRENCY_SUBMENU);

        // Create the first one.
        currencyFuncs.create(selenium, getProperty("currency.CNY"),
                getProperty("factor.CNY"));

        // Create the second one.
        currencyFuncs.create(selenium, getProperty("currency.JPY"),
                getProperty("factor.JPY"));
    }
}
