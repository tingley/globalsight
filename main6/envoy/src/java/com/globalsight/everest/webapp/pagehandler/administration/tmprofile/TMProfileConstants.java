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
    public static final String SAVE_MTED_SEGS_TO_PROJECT_TM = "isSaveMTedSegToProjectTm";
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
     * Constant used for a save action.
     */
    public static final String SAVE_ACTION = "save";
    /**
     * Constant used for a save all workflows action.
     */
    public static final String SAVE_ALL_TRANSLATION_MEMORY_PROFILES_ACTION = "saveTranslationMemoryProfiles";
    
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
     * For TM accessing control
     */
    public static final String TM_ENABLE_ACCESS_CONTROL = "enableTMAccessControl";
    
    public static final String FILTER_NAME = "tmProfileNameFilter";
    public static final String FILTER_STORAGE_TM = "tmProfileStorageTmFilter";
    public static final String FILTER_COMPANY_NAME = "tmProfileCompanyFilter";
}
