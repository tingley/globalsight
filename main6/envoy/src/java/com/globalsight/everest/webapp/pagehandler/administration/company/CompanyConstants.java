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
package com.globalsight.everest.webapp.pagehandler.administration.company;

public interface CompanyConstants {
    // Constant for saving activity in session
    public static final String COMPANY = "company";

    // For checking dup names
    public static final String NAMES = "names";

    // For tags
    public static final String COMPANY_LIST = "companies";
    public static final String COMPANY_KEY = "company";
    
    // Actions
    public static final String CANCEL = "cancel";
    public static final String CREATE = "create";
    public static final String DEPENDENCIES = "dependencies";
    public static final String EDIT = "edit";
    public static final String REMOVE = "remove";

    // fields
    public static final String NAME = "nameField";
    public static final String DESC = "descField";
    public static final String SESSIONTIME = "sessionTimeField";
    public static final String ENABLE_IP_FILTER = "enableIPFilterField";
    public static final String ENABLE_TM_ACCESS_CONTROL = "enableTMAccessControlFiled";
    public static final String ENABLE_TB_ACCESS_CONTROL = "enableTBAccessControlFiled";
    public static final String ENABLE_SSO_LOGON = "enableSsoLogonField";
    public static final String SSO_IDP_URL = "ssoIdpUrlField";
    public static final String TM3_VERSION = "tm3version";
    
}
