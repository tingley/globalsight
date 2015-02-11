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
	private static final String MAIN_TABLE = "//div[@id='contentLayer']/form/table/tbody/tr[2]/td/table/tbody";
    /**
     * Create a new Currency.
     */
    public void newCurrency(Selenium selenium, String iCurrency, String iFactor)
            throws Exception
    {
        if (isPresentInTable(selenium, Currency.Currency_TABLE, iCurrency
                .replace("label=", "")))
        {
            Reporter.log("The currency " + iCurrency + " has already exists!");
        }
        else
        {
            selenium.click(Currency.New_BUTTON);
            selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
            selenium.select(Currency.DisplayCurrency_SELECT, iCurrency);
            selenium.type(Currency.ConversionFactor_TEXT_FIELD, iFactor);
            selenium.click(Currency.Save_BUTTON);
            selenium.waitForPageToLoad(CommonFuncs.SHORT_WAIT);
            Assert.assertEquals(isPresentInTable(selenium,
                    Currency.Currency_TABLE, iCurrency.replace("label=", "")),
                    true);
        }
    }
    public void editCurrency (Selenium selenium, String Currencies) throws Exception
    {
    	String[] iCurrencies = Currencies.split(",");
		        
		for (String iCurrency : iCurrencies) 
    	{
    		try 
    		{
    			String[] ivalue = iCurrency.split("=");
    			String iFieldName = ivalue[0].trim();
    			String iFieldValue = ivalue[1].trim();

    			if (iFieldName.equals("name")) 
    			{
    				boolean selected = selectRadioButtonFromTable(selenium, MAIN_TABLE, iFieldValue);
    		        if (!selected)
    		        {
    		            Reporter.log("Cannot find a proper currency to edit.");
    		            return;
    		        }
    		        clickAndWait(selenium,Currency.Edit_BUTTON);
    			}else if (iFieldName.equals("ConversionFactor")) {
    				selenium.type(Currency.ConversionFactor_TEXT_FIELD, iFieldValue);
    				clickAndWait(selenium,Currency.Save_BUTTON);
    			} 

    		} catch (Exception e) {
    			Reporter.log(e.getMessage());
    		}
    	}
    
    }
}
