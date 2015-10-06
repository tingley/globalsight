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

public interface CompanyConstants
{
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
    public static final String NEXT = "next";
    public static final String DEPENDENCIES = "dependencies";
    public static final String EDIT = "edit";
    public static final String REMOVE = "remove";
    public static final String CONVERT = "convert";
    public static final String GET_MIGRATE_PROCESSING = "getMigrateProcessing";
    public static final String SEARCH = "search";

    // fields
    public static final String NAME = "nameField";
    public static final String DESC = "descField";
    public static final String EMAIL = "emailField";
    public static final String SESSIONTIME = "sessionTimeField";
    public static final String ENABLE_IP_FILTER = "enableIPFilterField";
    public static final String ENABLE_TM_ACCESS_CONTROL = "enableTMAccessControlFiled";
    public static final String ENABLE_TB_ACCESS_CONTROL = "enableTBAccessControlFiled";
    public static final String ENABLE_QA_CHECKS = "enableQAChecksField";
    public static final String ENABLE_SSO_LOGON = "enableSsoLogonField";
    public static final String SSO_IDP_URL = "ssoIdpUrlField";
    public static final String TM3_VERSION = "tm3version";
    public static final String BIG_DATA_STORE_LEVEL = "bigDataStoreLevel";
    public static final String ENABLE_DITA_CHECKS = "enableDitaChecks";
    public static final String ENABLE_WORKFLOW_STATE_POSTS = "enableWorkflowStatePosts";
    public static final String ENABLE_INCTXRV_TOOL_INDD = "enableInCtxRvToolInddField";
    public static final String ENABLE_INCTXRV_TOOL_OFFICE = "enableInCtxRvToolOfficeField";
    public static final String ENABLE_INCTXRV_TOOL_XML = "enableInCtxRvToolXMLField";

    public static final String FILTER_NAME = "companyNameFilter";
    public static final String FILTER_DESCRIPTION = "companyDescriptionFilter";

    /**
     * 0 (system level): "leverage_match", "translation_unit",
     * "translation_unit_variant".
     */
    public static final Integer BIG_DATA_STORE_LEVEL_SYSTEM = 0;

    /**
     * 1 (company level): "leverage_match_[companyId]",
     * ""translation_unit_[companyId]", "translation_unit_variant_[companyId]".
     */
    public static final Integer BIG_DATA_STORE_LEVEL_COMPNAY = 1;// default

    /**
     * 2 (job level): "leverage_match_[companyId]_[jobId]",
     * ""translation_unit_[companyId
     * ]_[jobId]", "translation_unit_variant_[companyId]_[jobId]".
     */
    public static final Integer BIG_DATA_STORE_LEVEL_JOB = 2;
}
