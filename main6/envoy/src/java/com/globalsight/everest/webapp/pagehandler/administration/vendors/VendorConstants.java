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
package com.globalsight.everest.webapp.pagehandler.administration.vendors;


public interface VendorConstants
{
    // Constant for saving vendor in session
    public static final String VENDOR = "vendor";

    // For tags
    public static final String VENDOR_LIST = "vendors";
    public static final String VENDOR_KEY = "vendor";
    public static final String PROJECT_LIST = "projects";
    public static final String PROJECT_KEY = "project";
    public static final String ROLE_LIST = "roles";
    public static final String ROLE_KEY = "role";
    public static final String RATE_LIST = "ratings";
    public static final String RATE_KEY = "rate";
    
    /**
     * Constants for saving field security in session
     */
    public static final String FIELD_SECURITY_CHECK_PROJS = "fs_check";
    public static final String FIELD_SECURITY_NOCHECK = "fs_nocheck";
    

    /**
     * Constant for name of custom form property file
     */
    public static final String CUSTOM_FORM_PROPERTIES = "properties/vendormanagement/customForm";

    //////////////////////////////////////////////////////////////////////
    //  Begin: Action Parameters
    //////////////////////////////////////////////////////////////////////
    /**
     * Constant used for a action.
     */
    public static final String ACTION = "action";
    /**
     * Constant used for cancel action.
     */
    public static final String CANCEL_ACTION = "cancel";
    /**
     * Constant used for details action.
     */
    public static final String DETAILS_ACTION = "details";
    /**
     * Constant used for new action.
     */
    public static final String NEW_ACTION = "new1";
    /**
     * Constant used for next action.
     */
    public static final String NEXT_ACTION = "next";

    //////////////////////////////////////////////////////////////////////
    //  End: Action Parameters
    //////////////////////////////////////////////////////////////////////

}
