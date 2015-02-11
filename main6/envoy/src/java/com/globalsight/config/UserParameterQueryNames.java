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
 * Specifies the names of all the named queries for UserParameter.
 */
public interface UserParameterQueryNames
{
    //
    // CONSTANTS REPRESENTING NAMES OF REGISTERED NAMED-QUERIES
    //
    /**
     * A named query to return all user parameters by user id.
     * <p>
     * Arguments: user id.
     */
    public static String ALL_USER_PARAMETERS_BY_USER =
        "getAllUserParametersByUser";

    /**
     * A named query to return the user parameter specified by the given id.
     * <p>
     * Arguments: 1: User Parameter id.
     */
    public static String USER_PARAMETER_BY_ID = "getUserParameterById";

    /**
     * A named query to return the user parameter specified by the
     * given name for the given user.
     *
     * <p> Arguments: 1: User name.
     * <p> Arguments: 2: User Parameter name.
     */
    public static String USER_PARAMETER_BY_USER_NAME =
        "getUserParameterByUserName";
}
