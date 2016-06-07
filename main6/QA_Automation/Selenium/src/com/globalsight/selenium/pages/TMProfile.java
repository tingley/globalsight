package com.globalsight.selenium.pages;

/*
 * This file contents the web elements definition about the TMProfile affairs.
 */
public class TMProfile implements BasePage
{
	// Translation Memory Profiles
    public static final String TM_PROFILE_LIST_TABLE = "//div[@id='contentLayer']//form//table//tbody//tr[2]//td//table//tbody";
 
    public static final String MT_OPTIONS_BUTTON = "mtEditBtn";
    public static final String TDAOptions_BUTTON = "tdaEditBtn";
    public static final String TMP_SEARCH_CONTENT_TEXT = "id=tmProfileNameFilter";
    public static final String TMP_NEW_BUTTON = "id=idNewBtn";
    
    //TM PROFILE EDIT PAGE
    public static final String TMP_NAME_TEXT = "name=nameField";
    public static final String TMP_DESCRIPTION_TEXT = "name=descField";
    public static final String TMP_SRX_RULE_SET_SELECT = "name=selectedSR";
    public static final String TMP_SRX_RULE_SET_DEFAULT_TEXT = "label=GlobalSight Predefined (Default)";
    public static final String TMP_STORAGE_TM_SELECT = "name=projectTMIdToSave";
    public static final String TMP_SAVE_UNLOCALIZED_SEGMENTS_TO_TM_CHECKBOX = "name=isSaveUnlocSegToProjectTm";
    public static final String TMP_SAVE_LOCALIZED_SEGMENTS_TO_TM_CHECKBOX = "name=isSaveLocSegToProjectTm";
    public static final String TMP_SAVE_WHOLLY_INTERNAL_TEXT_SEGMENTS_TO_TM = "name=isSaveWhollyInternalTextTm";
    public static final String TMP_SAVE_EXACT_MATCH_SEGMENTS_TO_TM_CHECKBOX = "name=isSaveExactMatchToProjectTm";
    public static final String TMP_SAVE_APPROVED_SEGMENTS_TO_TM_CHECKBOX = "name=isSaveApprovedToProjectTm";
    public static final String TMP_SAVE_UNLOCALIZED_SEGMENTS_TO_PAGE_TM_CHECKBOX = "name=isSaveToPageTm";
    public static final String TMP_LEVERAGE_LOCALIZABLE_CHECKBOX = "name=levLocalizable";
    public static final String TMP_LEVERAGE_EXACT_MATCHES_ONLY_RADIO_BUTTON = "id=idIsLevEMChecked";
    public static final String TMP_LEVERAGE_IN_CONTEXT_MATCHES_RADIO_BUTTON = "id=idLevContextMatches";
    public static final String TMP_APPLY_SID_ICE_PROMOTION_ONLY_RADIO_BUTTON = "id=idIcePromotionRules1";
    public static final String TMP_APPLY_SID_HASH_ICE_PROMOTION_RADIO_BUTTON = "id=idIcePromotionRules2";
    public static final String TMP_APPLY_SID_HASH_BRACKETED_ICE_PROMOTION_RADIO_BUTTON = "id=idIcePromotionRules3";
    public static final String TMP_LEVERAGE_APPROVED_TRANSLATIONS_FROM_SELECTED_REFERENCE_TM_CHECKBOX = "name=dynLevGold";
    public static final String TMP_LEVERAGE_IN_PROGRESS_TRANSLATION_FROM_THE_JOB_CHECKBOX = "name=dynLevInProgress";
    public static final String TMP_AND_FROM_JOBS_THAT_WRITE_TO_THE_STORAGE_TM_CHECKBOX = "name=dynLevPopulation";
    public static final String TMP_AND_FROM_JOBS_THAT_WRITE_TO_SELECTED_REFERENCE_TM_CHECKBOX = "name=dynLevReference";
    public static final String TMP_STOP_SEARCH_AFTER_HITTING_100_MATCH_CHECKBOX = "name=dynLevStopSearch";
    public static final String TMP_LEVERAGE_OPTIONS_FOR_INITIAL_IMPORT_COMBOBOX = "name=leveragePTM";
    public static final String TMP_REFERENCE_TM_BUTTON = "id=changeTmPosition";
    public static final String TMP_TYPE_SENSITIVE_LEVERAGING_CHECKBOX = "name=typeSensitiveLeveraging";
    public static final String TMP_TYPE_SENSITIVE_LEVERAGING_PERCENTAGE_TEXT = "name=typeDiffPenalty";
    public static final String TMP_CASE_SENSITIVE_LEVERAGING_CHECKBOX = "name=caseSensitiveLeveraging";
    public static final String TMP_CASE_SENSITIVE_LEVERAGING_PERCENTAGE_TEXT = "name=caseDiffPenalty";
    public static final String TMP_WHITESPACE_SENSITIVE_LEVERAGING_CHECKBOX = "name=whitespaceSensitiveLeveraging";
    public static final String TMP_WHITESPACE_SENSITIVE_LEVERAGING_PERCENTAGE_TEXT = "name=whiteDiffPenalty";
    public static final String TMP_CODE_SENSITIVE_LEVERAGING_CHECKBOX = "name=codeSensitiveLeveraging";
    public static final String TMP_CODE_SENSITIVE_LEVERAGING_PERCENTAGE_TEXT = "name=codeDiffPenalty";
    public static final String TMP_REFERENCT_TM_2_CHECKBOX = "name=isRefTm";
    public static final String TMP_REFERENCT_TM_2_PERCENTAGE_TEXT = "name=refTmPenalty";
    public static final String TMP_REFERENCT_TM_2_COMBO_BOX = "name=selectleveragedRefProjects";
    public static final String TMP_MULTILINGUAL_LEVERAGING_CHECKBOX = "name=multiLingualLeveraging";
    public static final String TMP_AUTO_REPAIR_PLACEHOLDERS_CHECKBOX = "name=autoRepair";
    public static final String TMP_GET_UNIQUE_FROM_MULTIPLE_EXACT_MATCHES_CHECKBOX = "name=uniqueFromMultTrans";
    public static final String TMP_MULTIPLE_EXACT_MATCHES_RADIO_BUTTON_LASTEST = "name=multEM";
    public static final String TMP_MULTIPLE_EXACT_MATCHES_RADIO_BUTTON_OLDEST = "xpath=(//input[@name='multEM'])[2]";
    public static final String TMP_MULTIPLE_EXACT_MATCHES_RADIO_BUTTON_DEMOTED = "xpath=(//input[@name='multEM'])[3]";
    public static final String TMP_MULTIPLE_EXACT_MATCHES_PENALTY_TEXT = "name=multDiffPenalty";
    public static final String TMP_LEVERAGE_MATCH_THRESHOLD_TEXT = "name=fuzzyMatchThreshold";
    public static final String TMP_NUMBER_OF_MATCHES_TEXT = "name=numberOfMatches";
    
    
    public static final String TMP_DISPLAY_TM_MATCHES_BY_RADIO_BUTTON_MATCHING_PERCENTAGE = "id=percentage";
    public static final String TMP_DISPLAY_TM_MATCHES_BY_RADIO_BUTTON_TM_PRECEDENCE = "id=procendence";
    public static final String TMP_DISPLAY_TM_CHOOSE_LATEST_MATCH = "name=latestMatchForReimport";
    public static final String TMP_TYPE_SENSITIVE_LEVERAGING_CHECKBOX_2 = "name=typeSensitiveLeveragingReimport";
    public static final String TMP_TYPE_SENSITIVE_LEVERAGING_PERCENTAGE_TEXT_2 = "name=typeDiffPenaltyReimport";
    public static final String TMP_NO_MULTIPLE_EXACT_MATCHES_CHECKBOX = "name=multLGEM";
    public static final String TMP_NO_MULTIPLE_EXACT_MATCHES_PECENTAGE_TEXT = "name=multMatchesPenaltyReimport";
    
    public static final String TMP_TU_ATTRIBUTES_MATCH_PRIORITISING_RULES_AND_OR_SELECT = "id=andOr";
    public static final String TMP_TU_ATTRIBUTES_MATCH_PRIORITISING_RULES_ATTRIBUTE_INTERNAL_NAME_AND_OR_SELECT = "id=attname";
    public static final String TMP_TU_ATTRIBUTES_MATCH_PRIORITISING_RULES_OPERATOR_SELECT = "id=operator";
    public static final String TMP_TU_ATTRIBUTES_MATCH_PRIORITISING_RULES_VALUE_TYPE_SELECT = "id=valueType";
    public static final String TMP_TU_ATTRIBUTES_MATCH_PRIORITISING_RULES_VALUE_TEXT = "id=valueData";
    public static final String TMP_TU_ATTRIBUTES_MATCH_PRIORITISING_RULES_ADD_BUTTON = "id=addRow";
    public static final String TMP_DISREGARD_IF_TU_ATTRIBUTES_NOT_MATCHED_RADIO_BUTTON = "name=choiceIfAttNotMatched";
    public static final String TMP_PERNALIZE_IF_TU_ATTRIBUTES_NOT_MATCHED_RADIO_BUTTON = "xpath=(//input[@name='choiceIfAttNotMatched'])[2]";
    public static final String TMP_PERNALIZE_IF_TU_ATTRIBUTES_NOT_MATCHED_RADIO_TEXT = "name=tuAttNotMatchPenalty";
    public static final String TMP_SAVE_BUTTON = "name=Save";
    public static final String TMP_CANCEL_BUTTON = "name=Cancel";
    
    
    
    
    
    
    
    
    // Create New TM Profile
    public static final String Name_TEXTFIELD = "nameField";
    public static final String Description = "descField";
    public static final String SRXRuleSet_SELECT = "selectedSR";
    public static final String StorageTM_SELECT = "projectTMIdToSave";
    public static final String ReferenceTMs_LABAL = "leveragePTM";
    public static final String Threshold_TEXTFEILD = "fuzzyMatchThreshold";

    public static final String Save_Unlocalized_Segments_to_TM = "isSaveToProjectTm";
    public static final String Save_unlocalized_segments_to_Page_TM = "isSaveToPageTm";
    public static final String exclude_Item_Types = "excludeItemType";
    public static final String leverage_Localizables = "levLocalizable";
    public static final String leverage_Default_Matches = "isLevDefaultMatch";
    public static final String leverage_Exact_Matches_only = "isLevEMChecked";
    public static final String leverage_Incontext_Matches = "levContextMatches";
    public static final String leverage_Approved_translations_from_selected_Reference_TMs = "dynLevGold";
    public static final String leverage_In_progress_translations_from_the_Job = "dynLevInProgress";
    public static final String and_from_Jobs_that_write_to_the_Storage_TM = "dynLevPopulation";
    public static final String and_from_Jobs_that_write_to_selected_Reference_TMs = "dynLevReference";
    public static final String reference_TMs = "leveragePTM";
    public static final String type_sensitive_Leveraging = "typeSensitiveLeveraging";
    public static final String penalty1 = "typeDiffPenalty";
    public static final String case_sensitive_Leveraging = "caseSensitiveLeveraging";
    public static final String penalty2 = "caseDiffPenalty";
    public static final String whitespace_sensitive_Leveraging = "whitespaceSensitiveLeveraging";
    public static final String penalty3 = "whiteDiffPenalty";
    public static final String code_sensitive_Leveraging = "codeSensitiveLeveraging";
    public static final String penalty4 = "codeDiffPenalty";
    public static final String reference_TM = "isRefTm";
    public static final String penalty5 = "refTmPenalty";
    public static final String reference_TM_Combo = "selectleveragedRefProjects";
    public static final String multilingual_Leveraging = "multiLingualLeveraging";
    public static final String auto_Repair_Placeholders = "autoRepair";
    public static final String multiple_Exact_Matches_Latest = "//input[@name='multEM' and @value='LATEST']";
    public static final String multiple_Exact_Matches_Oldest = "//input[@name='multEM' and @value='OLDEST']";
    public static final String multiple_Exact_Matches_Demoted = "//input[@name='multEM' and @value='DEMOTED']";
    public static final String penalty6 = "multDiffPenalty";
    public static final String leverage_Match_Threshold = "fuzzyMatchThreshold";
    public static final String number_of_Matches = "numberOfMatches";
    public static final String display_TM_Matches_by_percentage = "percentage";
    public static final String display_TM_Matches_by_procendence = "procendence";
    public static final String choose_Latest_Match = "latestMatchForReimport";
    public static final String type_sensitive_Leveraging2 = "typeSensitiveLeveragingReimport";
    public static final String penalty7 = "typeDiffPenaltyReimport";
    public static final String no_Multiple_Exact_Matches = "multLGEM";
    public static final String penalty8 = "multMatchesPenaltyReimport";
    
    //Google set
    public static final String MT_ENGINE_SELECT = "mtEngine";
    public static final String MT_OVERRIDE_NON_EXTRACT_CHECKBOX = "override_non_exact";
    public static final String MT_OVERRIDE_NON_EXTRACT_AUTO_COMMIT_RADIO = "auto_commit_to_tm";
    public static final String MT_OVERRIDE_NON_EXTRACT_LEVERAGING_RADIO = "mtLeveraging";    
    public static final String MT_OVERRIDE_NON_EXTRACT_LEVERAGING_PENALTY_TEXT= "mtSensitivePenalty";
    public static final String MT_SHOW_IN_EDITOR_CHECKBOX = "machineTranslation.showInEditor";
    public static final String MT_URL = "idMsMtUrl";
    public static final String MT_CLINTID = "idMsMtClientid";
    public static final String MT_CLINT_SECRET = "idMsMtClientSecret";
    public static final String MT_CATEGORY = "idMsMtCategory";
    public static final String MT_TEST_HOST_BUTTON = "test";
    
    //AO page
    public static final String AO_URL_TEXTFIELD = "idAoMtUrl";
    public static final String AO_Port_TEXTFIELD = "idAoMtPort";
    public static final String AO_UserName_TEXTFIELD = "idAoMtUsername";
    public static final String AO_Password_TEXTFIELD = "idAoMtPassword";
    public static final String AO_Account_Number_TEXTFIELD = "idAoMtAccountNumber";
    
    public static final String AO_Domain_Combination = "111";
    public static final String AO_ErrorMessage_Table = "//div[@id='aoMtDiv']/p/table/tbody/tr[8]/td/font/b";
    
    //TDA page
    public static final String TDA_Enable_CHECKBOX = "enableTda";
    public static final String TDA_HostName_TEXTFEILD = "hostName";
    public static final String TDA_UserName_TEXTFEILD = "userName";
    public static final String TDA_Password_TEXTFEILD = "password";
    
    //Confirm change allert
    public static final String confirmChangeDialog = "confirmChangeDialog";
}
