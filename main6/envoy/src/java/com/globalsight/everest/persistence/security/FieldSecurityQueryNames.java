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
 
package com.globalsight.everest.persistence.security;

/**
 * Specifies the names of all the named queries for FieldSecurity.
 */
public interface FieldSecurityQueryNames
{
    //
    // CONSTANTS REPRESENTING NAMES OF REGISTERED NAMED-QUERIES
    //
    /**
     * A named query to return the field security for a vendor.
     * <p>
     * Arguments: vendor id
     */
    public static String VENDOR_FIELD_SECURITY = "getFieldSecurityForVendor";

    /**
     * A named query to return the field security for a user.
     * <p>
     * Arguments: username (i.e userId)
     */
    public static String USER_FIELD_SECURITY = "getFieldSecurityForUser";
}
