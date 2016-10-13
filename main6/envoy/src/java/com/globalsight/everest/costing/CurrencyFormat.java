/**
 *  Copyright 2009 Welocalize, Inc. 
 *  
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  
 *  You may obtain a copy of the License at 
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  
 */

package com.globalsight.everest.costing;

import java.text.NumberFormat;
import java.util.Hashtable;
import java.util.Locale;

/**
 * CurrencyFormat provides support for formatting currency of multiple locales.
 */
public class CurrencyFormat
{
    
    //keep a map of the currencies and locales
    private static Hashtable s_currencyFormatMap = new Hashtable();
    private static NumberFormat s_usCurrencyFormat = NumberFormat.getCurrencyInstance(Locale.US);
    private static NumberFormat s_englishCurrencyFormat = NumberFormat.getCurrencyInstance(Locale.UK);
    private static NumberFormat s_frenchCurrencyFormat = NumberFormat.getCurrencyInstance(Locale.FRANCE);
    private static NumberFormat s_italianCurrencyFormat = NumberFormat.getCurrencyInstance(Locale.ITALY);
    private static final String DOLLAR = "dollar";
    private static final String POUND = "pound";
    private static final String FRANC = "franc";
    private static final String LIRA = "lira";

    static
    {
        //load the map with a few known $ users that may get called frequently
        s_currencyFormatMap.put("CAD", NumberFormat.getCurrencyInstance(Locale.CANADA)); //Canadian Dollar
        s_currencyFormatMap.put("USD", s_usCurrencyFormat); //US Dollar
        s_currencyFormatMap.put("JPY", NumberFormat.getCurrencyInstance(Locale.JAPAN));
        s_currencyFormatMap.put("EUR", NumberFormat.getCurrencyInstance(
            new  Locale(Locale.FRANCE.getLanguage(),Locale.FRANCE.getCountry(),"EURO")));
    }

    /**
    * <P>Returns a NumberFormat object that can be used to format currency values
    * nicely for display.</P>
    * <P>If the iso code of the currency logically maps to a known locale, then 
    * a real currency format is used. If the currency includes the word "dollar"
    * then the US currency format is used. If the currency is the Euro, then the
    * French Euro format is used. If the currency includes the word franc, pound,
    * or peso, then the French pound, English pound, or Mexican peso symbols are used.</P>
    *<P> Otherwise a normal NumberFormat with two digit decimal precision is returned.</P>
    * <br>
    * @param the currency to use
    * @return NumberFormat
    */
    public static NumberFormat getCurrencyFormat(Currency p_currency)
    {
        NumberFormat currencyFormat = null;
        String isoCode = p_currency.getIsoCode();
        currencyFormat = (NumberFormat)s_currencyFormatMap.get(isoCode);
        if (currencyFormat != null)
            return currencyFormat;

        String name = p_currency.getDisplayName().toLowerCase();
        if (name.indexOf(DOLLAR) > 0)
        {
            currencyFormat = s_usCurrencyFormat;
        }
        else if (name.indexOf(FRANC) > 0)
        {
            currencyFormat = s_frenchCurrencyFormat;
        }
        else if (name.indexOf(LIRA) > 0)
        {
            currencyFormat = s_italianCurrencyFormat;
        }
        else if (name.indexOf(POUND) > 0)
        {
            currencyFormat = s_englishCurrencyFormat;
        }
        else
        {
            //otherwise just use a regular number format
            currencyFormat = NumberFormat.getInstance();
            currencyFormat.setMaximumFractionDigits(2);
        }
        return currencyFormat;
    }
}
