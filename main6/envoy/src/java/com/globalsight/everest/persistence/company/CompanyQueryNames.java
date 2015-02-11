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
package com.globalsight.everest.persistence.company;

/**
 * Specifies the names of all the named queries for Company.
 */
public interface CompanyQueryNames
{
    //
    // CONSTANTS REPRESENTING NAMES OF REGISTERED NAMED-QUERIES
    //
    /**
     * A named query to return all activities.
     * <p>
     * Arguments: none.
     */
    public static String ALL_COMPANIES = "getAllCompanies";

    /**
     * A named query to return the company specified by the given name.
     * The name must be passed in uppercase to be found.
     * <p>
     * Arguments: 1: Uppercased Company Name.
     */
    public static String COMPANY_BY_NAME = "getCompanyByName";

    /**
     * A named query to return the company specified by the given id.
     * <p>
     * Arguments: 1 - company id 
     */
    public static String COMPANY_BY_ID = "getCompanyById";
    
    public static String COMPANY_ID_UPPER_BOUND = "getCompanyIdUpperBound";
    
    public static String COMPANY_ID_LOWER_BOUND = "getCompanyIdLowerBound";
}
