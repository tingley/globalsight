package com.globalsight.selenium.pages;

public class FileProfileElements
{
    // main page
    public static final String MAIN_NEW_BUTTON = "//input[@value='New...']";
    public static final String MAIN_REMOVE_BUTTON = "//input[@value='Remove']";
    
    // create page
    public static final String NEW_NAME_TEXT = "fpName";
    public static final String DESCRIPTION = "desc";
    public static final String NEW_LOCALIZATION_PROFILE_SELECT = "locProfileId";
    public static final String NEW_SOURCE_FILE_FORMAT_SELECT = "formatSelector";
    public static final String filterOption_SELECT = "filterInfo";
    public static final String NEW_ENCODING_SELECT = "codeSet";
    public static final String NEW_EXTENSION_SELECT = "extension";
    public static final String NEW_PRIMARY_RADIO = "idExp1";
    public static final String NEW_SECONDARY_RADIO = "idExp2";
    public static final String NEW_SAVE_BUTTON = "//input[@value='Save']";
    public static final String NEW_CANCEL_BUTTON = "//input[@value='Cancel']";
    public static final String fileProfile_Radio = "//input[@type='radio' and @onclick='enableButtons()' and @name='radioBtn' and @id='radioBtn']";
    public static final String MAIN_TABLE = "//div[@id='contentLayer']/form/p/table/tbody/tr[2]/td/table/tbody";
    public static final String Edit_BUTTON = "editBtn";
}
