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
package com.globalsight.everest.webapp.pagehandler.administration.tmprofile;


public interface TMProfileConstants
{
  
    /**
     * Constant used as a key for a list of workflow templates.
     */
    public static final String TMPROFILES = "tmProfiles";

    /**
     * Constant used as a key for a workflow template info object.
     */
    public static final String TM_PROFILE = "tmProfile";


    //////////////////////////////////////////////////////////////////////
    //  Begin: UI Fields
    //////////////////////////////////////////////////////////////////////
    // fields for the first page of tm profile creation.
    public static final String NAME_FIELD = "nameField";
    public static final String DESCRIPTION_FIELD = "descField";
    public static final String PROJECT_TM_ID_TO_SAVE = "projectTMIdToSave";
    public static final String SAVE_UNLOC_SEGS_TO_PROJECT_TM = "isSaveToProjectTm";
    public static final String SAVE_ALL_UNLOC_SEGS_TO_PROJECT_TM = "isSaveAllUnlocSegToProjectTm";
    public static final String SAVE_UN_LOC_SEGS_TO_PROJECT_TM = "isSaveUnlocSegToProjectTm";
    public static final String SAVE_LOC_SEGS_TO_PROJECT_TM = "isSaveLocSegToProjectTm";
    public static final String SAVE_APPROVED_SEGS_TO_PROJECT_TM = "isSaveApprovedToProjectTm";
    public static final String SAVE_EXACT_MATCH_SEGS_TO_PROJECT_TM = "isSaveExactMatchToProjectTm";
    public static final String SAVE_UNLOC_SEGS_TO_PAGE_TM = "isSaveToPageTm";
    public static final String SAVE_WHOLLY_INTERNAL_TEXT_TM = "isSaveWhollyInternalTextTm";
    public static final String LEVERAGE_EXCLUDE_TYPES = "excludeItemType";
    public static final String LEVERAGE_LOCALIZABLES = "levLocalizable";
    public static final String LEVERAGE_EXACT_MATCH_ONLY = "levExactMatches";
    public static final String LEVERAGE_FROM_PROJECT_TM = "leveragePTM";
    public static final String TYPE_SENSITIVE_LEVERAGING = "typeSensitiveLeveraging";
    public static final String TYPE_DIFFERENCE_PENALTY = "typeDiffPenalty";
    public static final String CASE_SENSITIVE_LEVERAGING = "caseSensitiveLeveraging";
    public static final String CASE_DIFFERENCE_PENALTY = "caseDiffPenalty";
    public static final String WHITESPACE_SENSITIVE_LEVERAGING = "whitespaceSensitiveLeveraging";
    public static final String WHITESPACE_DIFFERENCE_PENALTY = "whiteDiffPenalty";
    public static final String CODE_SENSITIVE_LEVERAGING = "codeSensitiveLeveraging";
    public static final String CODE_DIFFERENCE_PENALTY = "codeDiffPenalty";
    public static final String MULTILINGUAL_LEVERAGING = "multiLingualLeveraging";
    public static final String AUTO_REPAIR = "autoRepair";
    public static final String MULTIPLE_EXACT_MATCHES = "multEM";
    public static final String MULTIPLE_EXACT_MATCH_PENALTY = "multDiffPenalty";
    public static final String FUZZY_MATCH_THRESHOLD = "fuzzyMatchThreshold";
    public static final String MATCHES_RETURNED = "numberOfMatches";
    public static final String LATEST_MATCH_FOR_REIMPORT = "latestMatchForReimport";
    public static final String TYPE_SENSITIVE_LEVERAGING_REIMPORT = "typeSensitiveLeveragingReimport";
    public static final String TYPE_DIFFERENCE_PENALTY_REIMPORT = "typeDiffPenaltyReimport";
    public static final String MULTIPLE_EXACT_MATCHES_REIMPORT = "multLGEM";
    public static final String MULTIPLE_EXACT_MATCHES_PENALTY_REIMPORT = "multMatchesPenaltyReimport";
    public static final String DYN_LEV_GOLD = "dynLevGold";
    public static final String DYN_LEV_STOP_SEARCH = "dynLevStopSearch";
    public static final String DYN_LEV_IN_PROGRESS = "dynLevInProgress";
    public static final String DYN_LEV_POPULATION = "dynLevPopulation";
    public static final String DYN_LEV_REFERENCE = "dynLevReference";
    // Constant used as the default List of Exclude Item types
    public static final String EXCLUDE_ITEM_TYPES_LONG_LIST = 
        "url-a|url-animation|url-applet|url-applet-codebase|url-area|url-audio|url-base|url-bgsound|url-blockquote|url-body|url-del|url-embed|url-form|url-frame|url-frame-longdesc|url-head|url-iframe|url-iframe-longdesc|url-img|url-img-longdesc|url-img-usemap|url-input|url-input-usemap|url-ins|url-layer|url-link|url-media|url-q|url-object-data|url-object-classid|url-object-codebase|url-object-usemap|url-script|url-style|url-table|url-td|url-th|url-video|url-xml|img-height|img-width|meta-content|charset|css-background-color|css-background-image|css-border|css-border-color|css-color|css-font|css-font-family|css-font-size|css-font-style|css-font-variant|css-font-weight|css-letter-spacing|css-margin|css-margin-bottom|css-margin-left|css-margin-right|css-margin-top|css-padding|css-padding-bottom|css-padding-left|css-padding-right|css-padding-top|css-text-align|css-text-decoration|css-text-indent|css-text-transform|css-word-spacing|css-border-bottom-color|css-border-left-color|css-border-right-color|css-border-top-color|css-content|css-counter-increment|css-counter-reset|css-cue|css-cue-after|css-cue-before|css-cursor|css-direction|css-font-size-adjust|css-font-stretch|css-orphans|css-outline|css-outline-color|css-outline-style|css-outline-width|css-overflow|css-page-break-after|css-page-break-before|css-page-break-inside|css-pause|css-pause-after|css-pause-before|css-pitch|css-pitch-range|css-quotes|css-richness|css-speak|css-speak-date|css-speak-header|css-speak-punctuation|css-speak-time|css-speech-rate|css-stress|css-unicode-bidi|css-voice-family|css-volume|css-widows|css-behavior|css-filter|css-ime-mode|css-overflow-x|css-overflow-y|css-ruby-align|css-ruby-overhang|css-ruby-position|css-text-autospace|css-text-justify|css-word-break|style-url";

    /**
     * Constant used as a key for a translation memory profile id..
     */
    public static final String TM_PROFILE_ID = "tmProfileId";


    // For paging/sorting
    public static final String TMPS_LIST = "tmProfiles";
    public static final String TMP_KEY = "tmProfile";

    //////////////////////////////////////////////////////////////////////
    //  End: UI Fields
    //////////////////////////////////////////////////////////////////////

     //////////////////////////////////////////////////////////////////////
    //  Begin: Action Parameters
    //////////////////////////////////////////////////////////////////////
    /**
     * Constant used as an action string for a request.
     */
    public static final String ACTION = "formAction";
    
    public static final String NEW_ACTION = "new";
    /**
     * Constant used for an edit action.
     */
    public static final String EDIT_ACTION = "edit";
    /**
     * Constant used for a duplicate action.
     */
    public static final String DUPLICATE_ACTION = "duplicate";
    /**
     * Constant used for a cancel action.
     */
    public static final String CANCEL_ACTION = "cancel";
    /**
     * Constant used for a populating workflow action.
     */
    public static final String POPULATE_TM_PROFILE_ACTION = "populateTranslationMemoryProfile";
    /**
     * Constant used for a remove action.
     */
    public static final String REMOVE_ACTION = "remove";
    /**
     * Constant used for machine translation edit action.
     */
    public static final String MT_EDIT_ACTION = "mt_edit";
    /**
     * Constant used for a save action.
     */
    public static final String SAVE_ACTION = "save";
    /**
     * Constant used for a save all workflows action.
     */
    public static final String SAVE_ALL_TRANSLATION_MEMORY_PROFILES_ACTION = "saveTranslationMemoryProfiles";
    /**
     * Constants used for save or cancel a MT options editing results.
     */
    public static final String SAVE_MT_OPTIONS_ACTION = "saveMTOptions";
    public static final String CANCEL_MT_OPTIONS_ACTION = "cancelMTOptions";
    
    public static final String SEARCH_ACTION = "search";

    //////////////////////////////////////////////////////////////////////
    //  End: Action Parameters
    //////////////////////////////////////////////////////////////////////
    /**
     * Constant used for a save workflow action.
     */
    public static final String SAVE_TRANSLATION_MEMORY_PROFILE = "saveTranslationMemoryProfile";
    /**
     * Constant used for a selected translation memory profile
     */
    public static final String SELECTED_TRANSLATION_MEMORY_PROFILE = "selectedTranslationMemoryProfile";

    // for Segmentation Rule
    public static final String SELECTED_SR =  "selectedSR";
    
    public static final String MATCH_PERCENTAGE = "matchPercentage";
    
    public static final String TM_PROCENDENCE = "tmProcendence";
    
    /**
     * Constants for Machine Translation configurations in TM profile Moved from
     * SystemConfigParamNames line 505-509 The four parameters were in
     * envoy.properties and system_parameter table, now are integrated with TM
     * profile.
     * 
     * Note that below MT related constants have been moved to
     * "MTProfileConstants.java" since 8.5.
     */
    public static final String MT_USE_MT="machineTranslation.useMT";
    public static final String MT_ENGINE = "machineTranslation.engine";
//    public static final String MT_AUTOCOMMIT_TO_TM = "machineTranslation.autoCommitToTM";
    public static final String MT_SHOW_IN_EDITOR = "machineTranslation.showInEditor";
    
    public static final String MT_PTSURL = "ptsurl";
    public static final String MT_LocalPairs_Map = "directionsMap";
    public static final String MT_LocalPairs_List = "directionsList";
    public static final String MT_DIRECTION_TOPICTEMPLATE_MAP = "dirToTplMap";
    public static final String MT_PTS_USERNAME = "username";
    public static final String MT_PTS_PASSWORD = "password";
    public static final String MT_PTS_URL_FLAG = "pts_url_flag";
    public static final String MT_PTS_URL_FLAG_V8 = "0";
    public static final String MT_PTS_URL_FLAG_V9 = "1";
    
    public static final String MT_MS_URL = "ms_mt_url";
    public static final String MT_MS_APPID = "ms_mt_appid";
    public static final String MT_MS_CLIENT_ID = "ms_mt_client_id";
    public static final String MT_MS_CLIENT_SECRET = "ms_mt_client_secret";
    public static final String MT_MS_ACCESS_TOKEN = "ms_mt_access_token";
    public static final String MT_MS_GRANT_TYPE = "client_credentials";
    public static final String MT_MS_SCOPE = "http://api.microsofttranslator.com";
    public static final String MT_MS_GET_ACCESS_TOKEN_URL = "https://datamarket.accesscontrol.windows.net/v2/OAuth2-13";
    public static final String MT_MS_CATEGORY = "ms_mt_category";
    public static final String MT_MS_URL_FLAG = "ms_mt_url_flag";
    public static final int MT_MS_MAX_CHARACTER_NUM = 1000;
    
    public static final String MT_AO_URL = "ao_mt_url";
    public static final String MT_AO_PORT = "ao_mt_port";
    public static final String MT_AO_USERNAME = "ao_mt_username";
    public static final String MT_AO_PASSWORD = "ao_mt_password";
    public static final String MT_AO_ACCOUNT_NUMBER = "ao_mt_account_number";
    
    public static final String MT_SAFA_HOST = "safa_mt_host";
    public static final String MT_SAFA_PORT = "safa_mt_port";
    public static final String MT_SAFA_COMPANY_NAME = "safa_mt_company_name";
    public static final String MT_SAFA_PASSWORD = "safa_mt_password";
    public static final String MT_SAFA_CLIENT = "safaba_client";
    
    /**
     * Currently 'Google' and "Promt" engines can be used.
     */
    public static final String MT_ENGINE_GOOGLE = "Google";
    public static final String MT_ENGINE_PROMT = "Promt";
    public static final String MT_ENGINE_MSTRANSLATOR = "MS_Translator";
    
    /**
     * For TM accessing control
     */
    public static final String TM_ENABLE_ACCESS_CONTROL = "enableTMAccessControl";
    
    public static final String FILTER_NAME = "tmProfileNameFilter";
    public static final String FILTER_STORAGE_TM = "tmProfileStorageTmFilter";
    public static final String FILTER_COMPANY_NAME = "tmProfileCompanyFilter";
}
