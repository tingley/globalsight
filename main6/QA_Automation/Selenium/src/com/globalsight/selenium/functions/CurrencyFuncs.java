package com.globalsight.selenium.functions;

/*
 * FileName: CurrencyFuncs.java
 * Author:Jester
 * Methods: CurrencyNew() 
 * 
 */

import org.testng.Assert;
import org.testng.Reporter;

import com.globalsight.selenium.pages.Currency;
import com.thoughtworks.selenium.Selenium;

public class CurrencyFuncs extends BasicFuncs
{
    /**
     * Create a new Currency.
     */
    public void create(Selenium selenium, String currency, String factor)
            throws Exception
    {
        if (selectRadioButtonFromTable(selenium,true, Currency.currencyNameFilter, currency))
        {
            Reporter.log("The currency " + currency + " has already exists!");
        }
        else
        {
            selenium.click(Currency.NEW_BUTTON);
            selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);

            selenium.select(Currency.CURRENCY_SELECT, currency);
            selenium.type(Currency.FACTOR_TEXT, factor);

            selenium.click(Currency.SAVE_BUTTON);
            selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);

            Assert.assertEquals(selectRadioButtonFromTable(selenium,true, Currency.currencyNameFilter, currency), true);
                    
        }
    }

    public void modify(Selenium selenium, String currency, String factor)
            throws Exception
    {
        boolean selected = selectRadioButtonFromTable(selenium,true, Currency.currencyNameFilter, currency);
        if (!selected)
        {
            Reporter.log("Cannot find a proper currency to edit.");
            return;
        }
        clickAndWait(selenium, "link="+currency);
        selenium.type(Currency.FACTOR_TEXT, factor);
        clickAndWait(selenium, Currency.SAVE_BUTTON);
    }
}
