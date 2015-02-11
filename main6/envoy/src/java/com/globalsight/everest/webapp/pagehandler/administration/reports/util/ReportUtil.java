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

import java.util.HashMap;
import java.util.Map;

import com.globalsight.util.StringUtil;

/**
 * <code>ReportUtil</code> provides the help method for report generation.
 * 
 */
public class ReportUtil
{

	private static Map<String, String> map = new HashMap<String, String>();

	static
	{
		map.put("US Dollar (USD)", "$");
		map.put("Euro (EUR)", "\u20AC");
		map.put("Chinese Renminbi-Yuan (CNY)", "\u00a5");
	}

	public static final String CURRENCY_REG = "\\(.*\\)";

	/**
     * Gets the currency symbol of the currency. <br>
     * If the currency is not listed in the customized map, return empty string.
     * 
     * @param currency
     *            The currency.
     * @return The sysbol, if not exist return empty string.
     */
	public static String getCurrencySymbol(String currency)
	{
		String symbol = map.get(currency);
		if (StringUtil.isEmpty(symbol))
		{
			return StringUtil.EMPTY_STRING;
		}

		return symbol;
	}

	/**
     * Gets the currency name to match the database value. <br>
     * eg. <br>
     * If the currency is US Dollar (USD) return US Dollar
     * 
     * @param currency
     * @return
     */
	public static String getCurrencyName(String currency)
	{
		if(StringUtil.isEmpty(currency)){
			throw new IllegalArgumentException("The currency is null");
		}
		
		return currency.replaceAll(CURRENCY_REG, StringUtil.EMPTY_STRING)
				.trim();
	}

    /**
     * 
     * 0, 1, ..., 51 -> "A", "B", ..., "Z", "AA", "AB", ..., "AZ"
     * 
     * @param i
     * 
     * @return
     */
    public static String toChar(int i)
    {
        String s = "";
        
        if (i >= 0 && i < 26)
        {
            s = s + (char) ('A' + i);
        }
        else if (i >= 26 && i < 52)
        {
            s = "A" + (char) ('A' + i - 26);
        }

        return s;
    }
}