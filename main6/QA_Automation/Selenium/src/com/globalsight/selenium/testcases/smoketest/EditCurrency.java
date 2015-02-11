package com.globalsight.selenium.testcases.smoketest;

import org.testng.annotations.Test;
import com.globalsight.selenium.functions.CurrencyFuncs;
import com.globalsight.selenium.pages.MainFrame;
import com.globalsight.selenium.testcases.BaseTestCase;

public class EditCurrency extends BaseTestCase
{

    /*
     * Common variables initialization.
     */
    private CurrencyFuncs currencyFuncs = new CurrencyFuncs();

    @Test
    public void editCurrency() throws Exception
    {
        openMenuItemAndWait(selenium, MainFrame.SETUP_MENU,
                MainFrame.CURRENCY_SUBMENU);

        // Edit the first one.
        String currency = getProperty("currency.JPY");
        String factor = getProperty("factor.JPY.edit");
        
        currencyFuncs.modify(selenium, currency, factor);
    }
}
