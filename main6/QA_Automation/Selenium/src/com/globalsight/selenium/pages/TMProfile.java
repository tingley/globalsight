package com.globalsight.selenium.pages;

/*
 * This file contents the web elements definition about the TMProfile affairs.
 */
public class TMProfile
{
	// Translation Memory Profiles
    public static final String New_BUTTON = "//input[@value='New...']";

    public static final String TMProfiles_TABLE = "//div[@id='contentLayer']//form//table//tbody//tr[2]//td//table//tbody";
 
    public static final String Edit_BUTTON = "editBtn";
    public static final String MTOptions_BUTTON = "mtEditBtn";
    public static final String TDAOptions_BUTTON = "tdaEditBtn";
    
    // Create New TM Profile
    public static final String Name_TEXTFIELD = "nameField";
    public static final String Description = "descField";
    public static final String SRXRuleSet_SELECT = "selectedSR";
    public static final String StorageTM_SELECT = "projectTMIdToSave";
    public static final String ReferenceTMs_LABAL = "leveragePTM";
    public static final String Save_BUTTON = "Save";
    public static final String Cancel_BUTTON = "Cancel";
    public static final String Threshold_TEXTFEILD = "fuzzyMatchThreshold";

  
    
    //Google set
    public static final String MTEngine_SELECT = "mtEngine";
    //selenium.select(LocalePairs.SourceLocale_SELECT, iSourceLocale);
    public static final String MTOverride_Non_Exact_CHECKBOX = "override_non_exact";
    public static final String MTAuto_Commit_To_Tm_RADIO = "auto_commit_to_tm";
    public static final String MT_Leveraging_Radio = "mtLeveraging";    
    public static final String MT_Leveraging_Penalty_T= "mtSensitivePenalty";
    public static final String MTShow_In_Editor_CHECKBOX = "machineTranslation.showInEditor";
    public static final String MT_TDA_Save_BUTTON = "OK";
    
    //AO page
    public static final String AO_URL_TEXTFIELD = "idAoMtUrl";
    public static final String AO_Port_TEXTFIELD = "idAoMtPort";
    public static final String AO_UserName_TEXTFIELD = "idAoMtUsername";
    public static final String AO_Password_TEXTFIELD = "idAoMtPassword";
    public static final String AO_Account_Number_TEXTFIELD = "idAoMtAccountNumber";
    public static final String AO_Next_BUTTON = "OK";
    
    public static final String AO_Domain_Combination = "111";
    public static final String AO_ErrorMessage_Table = "//div[@id='aoMtDiv']/p/table/tbody/tr[8]/td/font/b";
    
    //TDA page
    public static final String TDA_Enable_CHECKBOX = "enableTda";
    public static final String TDA_HostName_TEXTFEILD = "hostName";
    public static final String TDA_UserName_TEXTFEILD = "userName";
    public static final String TDA_Password_TEXTFEILD = "password";
    
    
}
