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
    public static final String MT_APPID = "idMsMtAppid";
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
