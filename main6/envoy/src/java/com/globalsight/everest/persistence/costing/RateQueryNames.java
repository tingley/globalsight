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
 * Specifies the names of all the named queries for Rate.
 */
public interface RateQueryNames
{
   
    //
    // CONSTANTS REPRESENTING NAMES OF REGISTERED NAMED-QUERIES
    // 

    /**
     * A named query to return a rate  by its id.
     * <p>
     * Arguments: The id of the rate.None.                          
     */
    public static String RATE_BY_ID = "getRateById"; 

    /**
     * A named query to return all the rates.
     * <p>
     */
    public static String ALL_RATES = "getAllRates";
}
