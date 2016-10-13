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
package com.globalsight.config;

/**
 * Specifies the names of all the named queries for SystemParameter.
 */
public interface SystemParameterQueryNames
{
    //
    // CONSTANTS REPRESENTING NAMES OF REGISTERED NAMED-QUERIES
    //
    /**
     * A named query to return all system parameters.
     * <p>
     * Arguments: none.
     */
    public static String ALL_SYSTEM_PARAMETERS = "getAllSystemParameters";

    /**
     * A named query to return the system parameter specified by the given id.
     * <p>
     * Arguments: 1: System Parameter id.
     */
    public static String SYSTEM_PARAMETER_BY_ID = "getSystemParameterById";

    /**
     * A named query to return the system parameter specified by the given name.
     * <p>
     * Arguments: 1: System Parameter name.
     */
    public static String SYSTEM_PARAMETER_BY_NAME = "getSystemParameterByName";
}
