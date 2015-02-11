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
package com.globalsight.everest.persistence.vendor;


/**
 * Specifies the names of all the named queries for Vendor.
 */
public interface VendorQueryNames
{
   
    //
    // CONSTANTS REPRESENTING NAMES OF REGISTERED NAMED-QUERIES
    // 

    /**
     * A named query to return all vendors.
     */
    public static String ALL_VENDORS = "getAllVendors";

    /** 
     * A named query to return all vendors that are in a specific
     * project.
     */
    public static String VENDORS_IN_PROJECT = "getVendorsInProject";

    /**
     * A named query to return all vendors that are in the same
     * project(s) as the specified USER.
     */
    public static String VENDORS_IN_SAME_PROJECTS = "getVendorInSameProjects";

    /**
     * A named query to return a vendor by its id.
     * <p>
     * Arguments: The id of the vendor.                          
     */
    public static String VENDOR_BY_ID = "getVendorById"; 

    /**
     * A named query to return a vendor by its custom id.
     * <p>
     * Arguments: The custom id of the vendor.                          
     */
    public static String VENDOR_BY_CUSTOM_ID = "getVendorByCustomId";   

    /**
     * A named query to return a vendor by the specified user id.
     *
     * Arguments: The user id that is associated with a vendor.
     */
    public static String VENDOR_BY_USER_ID = "getVendorByUserId";

    /**
     * A named query to return a list of user ids that the vendors
     * are already associated with.
     */
    public static String USER_IDS_OF_VENDORS = "getVendorsUserIds";

    /**
     * A named query to return a report query that contains all the
     * distinct company names that vendors are a part of.
     */
    public static String VENDOR_COMPANY_NAMES = "getVendorCompanyNames";

    /**
     * A named query to return a report query that contains all the
     * custom vendor ids (needed to check uniqueness).
     */
    public static String VENDOR_CUSTOM_IDS = "getVendorCustomIds";

    /**
     * A named query to return a report query that contains all the
     * vendor pseudonyms (needed to check uniqueness).
     */
    public static String VENDOR_PSEUDONYMS = "getVendorPseudonyms";

    /**
     * A named query to return all vendors who are marked to be added
     * to all projects.
     */
    public static String VENDORS_IN_ALL_PROJECTS = "getVendorsInAllProjects";

    //////////////////////////////////////////////////////////////////////
    //  Rating Query Names
    //////////////////////////////////////////////////////////////////////
    /**
     * A named query to return a rating by its id.
     * <p>
     * Arguments: 
     * 1. The id of the rating.                          
     */
    public static String RATING_BY_ID = "getRatingById";

    /**
     * A named query to return a list of ratings by a modifier user id.
     * <p>
     * Arguments: 
     * 1. The modifier user id.                          
     */
    public static String RATINGS_BY_MODIFY_USER_ID = "getRatingsByModifierUserId";

    /**
     * A named query to return a list of ratings by the task id.
     * <p>
     * Arguments: 
     * 1. The id of the task
     */
    public static String RATINGS_BY_TASK_ID = "getRatingsByTaskId";

    /**
     * A named query to return a list of ratings by the vendor id.
     * <p>
     * Arguments: 
     * 1. The id of the vendor
     */
    public static String RATINGS_BY_VENDOR_ID = "getRatingsByVendorId";

}
