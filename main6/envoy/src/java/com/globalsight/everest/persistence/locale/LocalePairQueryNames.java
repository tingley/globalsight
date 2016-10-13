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
package com.globalsight.everest.persistence.locale;


/**
 * Specifies the names of all the named queries for LocalePair.
 */
public interface LocalePairQueryNames
{
   
    //
    // CONSTANTS REPRESENTING NAMES OF REGISTERED NAMED-QUERIES
    // 

    /**
     * A named query to return all locale pairs
     * <p>
     * Arguments: None.
     */
    public static String ALL_LOCALE_PAIRS = "getAllLocalePairs"; 
    
    /**
     * A named query to return a locale pair object based on its id
     * <p>
     * Arguments: 1: LocalePair Id.
     */
    public static String LOCALE_PAIR_BY_ID = "getLocalePairById"; 

    /**
     * A named query to return a locale pair object based on source/target locale ids.
     * <p>
     * Arguments: 1: Source Locale Id.
     *            2: Target Locale Id.
     */
    public static String LOCALE_PAIR_BY_SRC_TRGT_IDS = "getLocalePairBySrcTargetIds"; 

    /**
     * A named query to return a locale pair object based on source/target locale strings.
     * <p>
     * Arguments: 1- source locale string
     *            2- target locale string
     */
    public static String LOCALE_PAIR_BY_SRC_TRGT_STRINGS =
        "getLocalePairBySrcTargetStrings";
}
