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
package com.globalsight.everest.persistence.codeset;

/**
 * Specifies the names of all the named queries for CodeSet.
 */
public interface CodeSetQueryNames
{
    //
    // CONSTANTS REPRESENTING NAMES OF REGISTERED NAMED-QUERIES
    //
    /**
     * A named query to return all available code sets
     * <p>
     * Arguments: None.
     */
    public static String ALL_CODE_SETS = "getAllCodeSets";
 

    /**
     * A named query to return all code sets asssociated with a 
     * locale specified by an id.
     * <p>
     * Arguments: None.
     */
    public static String ALL_CODES_SETS_BY_LOCALE_ID = "getCodeSetsByLocaleId";
}
