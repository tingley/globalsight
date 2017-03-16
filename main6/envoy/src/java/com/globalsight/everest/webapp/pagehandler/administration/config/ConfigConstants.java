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
package com.globalsight.everest.webapp.pagehandler.administration.config;

import java.util.HashMap;
import java.util.Map;

/**
 * This interface contains all of the configuration export and import related
 * constants.
 */
public interface ConfigConstants
{
    Map<String, String> config_error_map = new HashMap<String, String>();
    Map<String, Integer> config_percentage_map = new HashMap<String, Integer>();

    public static final String LOCALEPAIR_FILE_NAME = "LocalePairs_";
    public static final String USER_FILE_NAME = "User_";
    public static final String MT_FILE_NAME = "MachineTranslationProfiles_";
    public static final String FILTER_FILE_NAME = "FilterConfiguration_";
    public static final String ACTIVITY_FILE_NAME = "ActivityTypes_";
    public static final String PERMISSION_GROUP_NAME = "PermissionGroups_";
    public static final String TM_FILE_NAME = "TranslationMemories_";
    public static final String SEGMENT_RULE_FILE_NAME = "SegmentRlues_";
    public static final String TM_PROFILE_FILE_NAME = "TranslationMemoryProfiles_";
    public static final String PROJECT_FILE_NAME = "Projects_";
    public static final String WORKFLOW_TEMPLATE_FILE_NAME = "Workflows_";
    public static final String LOC_PROFILE_FILE_NAME = " LocalizationProfiles_";
    public static final String XML_RULE_FILE_NAME = "XMLRules_";
    public static final String FILE_PROFILE_FILE_NAME = "FileProfiles_";
    public static final String ATTRIBUTE_FILE_NAME = "Attributes_";
    public static final String ATTRIBUTE_SET_FILE_NAME = "AttributeSets_";
    public static final String CURRENCY_FILE_NAME = "Currency_";
    public static final String RATE_FILE_NAME = "Rates_";
    public static final String TERMINOLOGY_FILE_NAME = "Termbases_";
    public static final String WORKFLOW_STATE_POST_PROFILE_FILE_NAME = "WfStateProfiles_";
    public static final String PERPLEXITY_SERVICE = "PerplexityService_";

}
