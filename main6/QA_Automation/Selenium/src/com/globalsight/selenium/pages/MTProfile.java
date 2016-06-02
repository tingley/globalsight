package com.globalsight.selenium.pages;

/*
 * This file contents the web elements definition about the TMProfile affairs.
 */
public class MTProfile implements BasePage
{
	// Translation Memory Profiles
    public static final String MTP_PROFILE_LIST_TABLE = "//div[@id='MTProfileConstants']/table/tbody/tr[2]/td/table/tbody/";
 
    public static final String MTP_REMOVE_BUTTON = "id=idRemoveBtn";
    public static final String MTP_NEW_BUTTON = "id=idNewBtn";
    public static final String MTP_EXPORT_BUTTON = "id=exportBtn";
    public static final String MTP_IMPORT_BUTTON = "id=importBtn";
    public static final String MTP_SEARCH_CONTENT_TEXT = "id=mtProfileNameFilter";
    
    //TM PROFILE EDIT PAGE
    public static final String MTP_NAME_TEXT = "id=MtProfileName";
    public static final String MTP_DESCRIPTION_TEXT = "id=description";
    public static final String MTP_ENGINE_SELECT = "id=mtEngine";
    public static final String MTP_ENGINE_MSMT_lABEL = "label=MS_Translator";
    public static final String MTP_CONFIDENCE_SCORE_TEXT = "id=mtConfidenceScore";
    public static final String MTP_IGNORE_TM_MATCHES_CHECKBOX = "id=ignoreTmMatches";
    public static final String MTP_LOG_DEBUG_INFO_CHECKBOX = "id=logDebugInfo";
    public static final String MTP_INCLUDE_MT_IDENTIFIERS_CHECKBOX = "id=includeMTIdentifiers";
    public static final String MTP_IDENTIFIER_LEADING_TEXT = "id=mtIdentifierLeading";
    public static final String MTP_IDENTIFIER_TRAILING_TEXT = "id=mtIdentifierTrailing";
    public static final String MSMT_URL_TEXT = "id=idMsMtUrl";
    public static final String MSMT_CLIENT_ID_TEXT = "id=idMsMtClientid";
    public static final String MSMT_CLIENT_SECRET_TEXT = "id=idMsMtClientSecret";
    public static final String MSMT_CATEGORY_TEXT = "id=idMsMtCategory";
    public static final String MSMT_SR_RS_SELECT = "id=sr_RS";
    public static final String MSMT_SR_LATN_LABEL = "sr-Latn";
    public static final String MSMT_SR_LYRL_LABEL = "sr-Cyrl";
    
    public static final String MSMT_SR_YU_SELECT = "id=sr_YU";
    public static final String MSMT_SEND_TEXT_BETWEEN_TAGS_RADIO_BUTTON = "id=type1";
    public static final String MSMT_SEND_TEXT_INCLUDING_TAGS_RADIO_BUTTON = "id=type2";
    public static final String MSMT_MAX_CHUNK_LENGTH_TEXT = "id=msMaxLength";
    public static final String MTP_SAVE_BUTTON = "id=OK";
}
