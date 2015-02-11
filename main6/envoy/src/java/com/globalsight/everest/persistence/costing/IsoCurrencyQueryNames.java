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
package com.globalsight.everest.persistence.costing;

/**
 * Specifies the names of all the named queries for IsoCurrency.
 */
public interface IsoCurrencyQueryNames
{
    //
    // CONSTANTS REPRESENTING NAMES OF REGISTERED NAMED-QUERIES
    //
    /**
     * A named query to return all available iso currencies
     * <p>
     * Arguments: None.
     */
    public static String ALL_ISO_CURRENCIES = "getAllIsoCurrencies";
 
    /**
     * A named query to return all iso currencies that don't have a
     * conversion factor set up for them.
     * <p>
     * Arguments: None.
     */
    public static String ISO_CURRENCIES_WITHOUT_CONVERSION = 
                                "getIsoCurrenciesWithoutConversion";
    /**
     * A named query to return the iso currency associated with the code.
     * <p>
     * Arguments: The 3 character iso code. (i.e. USD - for United States dollar)
     */                  
    public static String CURRENCY_BY_CODE = "getCurrencyByCode";
}
