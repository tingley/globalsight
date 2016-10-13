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
 
package com.globalsight.everest.securitymgr;


/** 
 * Stores all the constants that define the secure fields
 * a vendor can have.  They are the keys to the FieldSecurities
 * hashtable for a vendor.
 */
public interface VendorSecureFields
{
    public static final String ADDRESS = "address";
    public static final String AMBASSADOR_ACCESS = "useInAmbassador";
    public static final String CELL_PHONE = "cellPhone";
    public static final String COMPANY = "companyName";
    public static final String CITIZENSHIP = "countries";
    public static final String COUNTRY = "country";
    public static final String CUSTOM_FIELDS = "customForm";
    public static final String CUSTOM_ID = "customVendorId";
    public static final String DOB = "dateOfBirth";
    public static final String EMAIL = "email";
    public static final String EMAIL_LANGUAGE = "emailLanguage";
    public static final String FAX = "fax";
    public static final String FIRST_NAME = "firstName";
    public static final String HOME_PHONE = "homePhone";
    public static final String IS_INTERNAL = "isInternal";
    public static final String LAST_NAME = "lastName";
    public static final String NOTES = "notes";
    public static final String PASSWORD = "password";
    public static final String PROJECTS = "projects";
    public static final String RESUME = "resume"; 
    public static final String ROLES = "roles";
    public static final String SECURITY = "security";
    public static final String STATUS = "status";
    public static final String TITLE = "title";
    public static final String USERNAME = "userName";
    public static final String WORK_PHONE = "workPhone";
}
