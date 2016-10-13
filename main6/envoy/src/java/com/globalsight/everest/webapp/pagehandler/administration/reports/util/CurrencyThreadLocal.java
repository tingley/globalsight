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

package com.globalsight.everest.webapp.pagehandler.administration.reports.util;

import java.rmi.RemoteException;

import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.costing.CostingException;
import com.globalsight.everest.costing.Currency;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.util.GeneralException;

/**
 * This class is used for defining the used currency in report.
 * 
 * If no currency is setted, US Dollar will be used.
 */
public class CurrencyThreadLocal
{
    public static ThreadLocal<String> currencyContext = new ThreadLocal<String>();
    private static String US_DOLLAR = "US Dollar (USD)";

    /**
     * Sets the currency name.
     * 
     * @param currency
     */
    public static void setCurrency(String currency)
    {
        currencyContext.set(currency);
    }

    private static String getCurrencyString()
    {
        String currency = currencyContext.get();
        if (currency == null)
        {
            currency = US_DOLLAR;
        }

        return currency;
    }

    /**
     * Gets the format of money used in report, it's according to the setted
     * currency name.
     * 
     * @return
     */
    public static String getMoneyFormatString()
    {
        String symbol = ReportUtil.getCurrencySymbol(getCurrencyString());
        return symbol + "###,###,##0.000;(" + symbol + "###,###,##0.000)";
    }

    /**
     * Gets the currency according to the setted currency name.
     * 
     * @return
     * @throws CostingException
     * @throws RemoteException
     * @throws GeneralException
     */
    public static Currency getCurrency() throws CostingException,
            RemoteException, GeneralException
    {
        return ServerProxy.getCostingEngine().getCurrencyByName(
                ReportUtil.getCurrencyName(getCurrencyString()),
                CompanyThreadLocal.getInstance().getValue());
    }
}
