package com.globalsight.selenium.pages;

/*
 * This file contents the web elements definition about the Account affairs.
 */


public class MyAccount {
	
	//Edit My Account-Basic Infomation
	public static final String ContactInfo_BUTTON="//input[@value='Contact Info...']";
	public static final String NotificationOptions_BUTTON="//input[@value='Notification Options...']";
    public static final String AccountOptions_BUTTON="//input[@value='Account Options...']";
    public static final String DownloadOptions_BUTTON="//input[@value='Download Options...']";
    public static final String Save_BUTTON="//input[@value='Save']";
    
    //Accout Options
    public static final String InlineEditor_RADIO="idEditor1";
    public static final String PopupEditor_RADIO="idEditor2";
    public static final String Done_BUTTON="apply";
    public static final String Cancel_BUTTON="cancel";
    
    //Download Options
    public static final String MYACCOUNT_FORMAT_SELECT = "name=formatSelector";
    public static final String MYACCOUNT_FORMAT_XLIFF20_TEXT = "label=Xliff 2.0";
    public static final String MYACCOUNT_FORMAT_XLIFF12_TEXT = "label=Xliff 1.2";
    public static final String MYACCOUNT_FORMAT_OMEGAT_TEXT = "label=OmegaT";
    public static final String MYACCOUNT_FORMAT_BILINGUAL_TRADOS_RTF_TEXT = "label=Optimized Bilingual TRADOS® RTF";
    public static final String MYACCOUNT_FORMAT_RTF_LIST_VIEW_TEXT = "label=RTF (list view)";
    public static final String MYACCOUNT_FORMAT_TRADOS_TTX_TEXT = "label=Trados 7 TTX";
    public static final String MYACCOUNT_TMX_TYPE_SELECT = "id=tmxTypeSelector";
    public static final String MYACCOUNT_TMX_TYPE_TMX14B_TEXT = "label=TMX File - 1.4b";
    public static final String MYACCOUNT_TMX_TYPE_ANNOTATIONS_TEXT = "label=Annotations";
    public static final String MYACCOUNT_TMX_TYPE_BOTH_TEXT = "label=Both (TMX File 1.4b and Annotations)";
    public static final String MYACCOUNT_TMX_TYPE_NONE_TEXT = "label=None";		
    public static final String MYACCOUNT_MT_MATCHES_INTO_SEPERATE_TM_FILE_CHECKBOX = "id=separateTMfile";
    public static final String MYACCOUNT_SEPARATE_TMX_WITH_PRE_PENALIZED_SOURCE = "id=penalizedReferenceTmPre"; 
    public static final String MYACCOUNT_SEPARATE_TMXS_PER_REFERENCE_TM_PENALTY = "id=penalizedReferenceTmPer";
    public static final String MYACCOUNT_TERMINOLOGY_SELECT = "id=resTermSelector";
    public static final String MYACCOUNT_TERMINOLOGY_TBX_LABEL = "label=TBX";
    public static final String MYACCOUNT_TERMINOLOGY_HTML_LABEL = "label=HTML";
    public static final String MYACCOUNT_TERMINOLOGY_TRADOS_LABEL = "label=TRADOS MultiTerm® iX";
    public static final String MYACCOUNT_TERMINOLOGY_TEXT_LABEL = "label=TEXT";
    public static final String MYACCOUNT_TERMINOLOGY_NONE_LABEL = "label=None";
    public static final String MYACCOUNT_ALLOW_EDIT_LOCKED_SEGMENTS_SELECT = "name=TMEditType";
    public static final String MYACCOUNT_ALLOW_EDIT_OF_ICE_MATCHES_TEXT = "label=Allow Edit of ICE matches";
    public static final String MYACCOUNT_ALLOW_EDIT_OF_ICE_AND_100_MATCHES_TEXT = "label=Allow Edit of ICE and 100% matches";
    public static final String MYACCOUNT_ALLOW_EDIT_OF_100_MATCHES_TEXT = "label=Allow Edit of 100% matches";
    public static final String MYACCOUNT_DENY_EDIT_TEXT = "label=Deny Edit";
    public static final String MYACCOUNT_POPULATE_100_TARGET_SEGMENTS_CHECKBOX = "id=populate100CheckBox";
    public static final String MYACCOUNT_POPULATE_FUZZY_TARGET_SEGMENTS_CHECKBOX = "id=populatefuzzyCheckBox";
    public static final String MYACCOUNT_PRESERVE_SOURCE_FOLDER_STRUCTURE_CHECKBOX = "id=preserveSourceFolder";
    public static final String MYACCOUNT_CONSOLIDATE_SPLIT_TYPE_SELECT = "id=consolidateFileType";
    public static final String MYACCOUNT_CONSOLIDATE_SPLIT_TYPE_FILE_BY_FILE_LABEL = "label=File by File";
    public static final String MYACCOUNT_CONSOLIDATE_SPLIT_TYPE_CONSOLIDATE_ALL_FILES_LABEL = "label=Consolidate All Files";
    public static final String MYACCOUNT_CONSOLIDATE_SPLIT_TYPE_SPLIT_FILE_PER_WORD_COUNT_LABEL = "label=Split File per Word Count";
    public static final String MYACCOUNT_INCLUDE_REPEATED_SEGMENTS_AS_SEPARATE_FILE_CHECKBOX = "id=includeRepetitions";
    public static final String MYACCOUNT_NOT_INCLUDE_FULLY_LEVERAGED_FILE_CHECKBOX = "id=excludeFullyLeveragedFiles";
    public static final String MYACCOUNT_INCLUDE_XML_NODE_CONTEXT_INFORMATION_CHECKBOX = "id=includeXmlNodeContextInformation";
    public static final String MYACCOUNT_DOWNLOAD_OPTION_DONE_BUTTON = "name=apply";
    
    
}
