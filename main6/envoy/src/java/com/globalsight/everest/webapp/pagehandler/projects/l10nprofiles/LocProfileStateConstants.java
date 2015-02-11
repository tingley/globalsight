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
package com.globalsight.everest.webapp.pagehandler.projects.l10nprofiles;

/**
* This interface contains the constants associated with the state of
* Localization Profile handlers.
*/
public interface LocProfileStateConstants
{

    public static final int NO_TM_USAGE = 0;
    public static final int DENY_EDIT_TM_USAGE = 1;
    public static final int ALLOW_EDIT_TM_USAGE = 2;
    
    public static final int TM_EDIT_TYPE_NONE = 0;
    public static final int TM_EDIT_TYPE_BOTH = 1;
    public static final int TM_EDIT_TYPE_ICE = 2;
    public static final int TM_EDIT_TYPE_100PERCENT = 3;
    public static final int TM_EDIT_TYPE_DENY = 4;
    

    public static final String MOD_LOC_PROFILE = "ModLocProfile";
    public static final String MOD_TARGET_LOCALE = "ModTargetLocale";
    public static final String TARGET_LOCALES = "TargetLocales";
    public static final String MODIFY_PROFILE_ACTION = "modifyProfile";
    public static final String DUPLICATE_PROFILE_ACTION = "duplicateProfile";
    public static final String CANCEL_ACTION= "cancel";
    public static final String SAVE_ACTION= "save";
    public static final String SAVEDUP_ACTION= "saveDup";
    public static final String ACTION = "action";

    public static final String DUP_LOC_PROFILE = "DupLocProfile";

    public static final String ALL_LOCALES = "LocalePairs";
    public static final String SOURCE_LOCALE_PAIRS = "SourceLocalePairs";
    public static final String PROJECT_PAIRS = "ProjectPairs";
    public static final String TM_USAGE_PAIRS = "TMUsagePairs";
    public static final String TM_PROFILE_PAIRS = "TMProfilePairs";
    public static final String LOC_PROFILE_NAME = "LocProfileName";
    public static final String SOURCE_LOCALE_ID = "SourceLocaleId";
    public static final String LOC_PROFILE_DESCRIPTION = "LocProfileDescription";
    public static final String LOC_PROFILE_PROJECT_ID = "LocProfileProjectId";
    public static final String LOC_TM_PROFILE_ID = "locTMProfileId";
    public static final String LOC_PROFILE_TM_USAGE_ID = "LocProfileTMUsageId";
    public static final String AUTOMATIC_DISPATCH = "AutomaticDispatch";
    public static final String LOC_PROFILE_SQL_SCRIPT = "LocProfileSQLScript";
    public static final String DISPATCH_CONDITION = "DispatchCondition";
    public static final String DISPATCH_WORD_COUNT = "DispatchWordCount";
    public static final String BATCH = "Batch";
    public static final String EXCLUDED_ITEM_TYPES = "ExcludedItemTypes";
    public static final String ABSOLUTE = "Absolute";
    public static final String RELATIVE = "Relative";
    public static final String JOB_PRIORITY = "JobPriority";
    public static final String JOB_COSTING = "JobCosting";
    public static final String SOURCE_LOCALE = "SourceLocale";
    public static final String TARGET_LOCALE = "TargetLocale";
    public static final String WORKFLOW_NAMES = "WorkflowNames";
    public static final String TARGET_OBJECTS = "TargetObjects";
    public static final String HAS_COSTING = "HasCosting";
    public static final String IS_SAME_PROJECT = "IsSameProject";
    public static final String IS_SAME_PROJECT_MANAGER = "IsSamePM";
    public static final String WORKFLOW_INFOS = "workflowInfos";
    
    public static final String LOCPROFILE_LIST = "locprofiles";
    public static final String LOCPROFILE_KEY = "locprofile";
    public static final String DEPENDENCIES = "deps";
}


