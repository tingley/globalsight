package com.globalsight.selenium.pages;

public class FilterConfiguration implements BasePage
{

    // Filter Configuration
    public static final String CHECK_ALL_CHECKBOX = "//input[@type='checkbox']";
    public static final String EXPAND_ALL_BUTTON = "//input[@id='expandAllFilters']";
    public static final String COLLAPSE_ALL_BUTTON = "collapseAllFilters";
    
    public static final String FILTER_TABLE="//span[@id='filterConfigurationTable']/table/tbody";
  
    public static final String HTML_FILTER_IMG = "//span[@id='filterConfigurationTable']/table/tbody/tr/td/img";  
    public static final String INDD_FILTER_IMG = "//span[@id='filterConfigurationTable']/table/tbody/tr[4]/td/img";
    public static final String BASE_FILTER_IMG = "//span[@id='filterConfigurationTable']/table/tbody/tr[7]/td/img";
    public static final String JAVA_PROPERTIES_FILTER_IMG = "//span[@id='filterConfigurationTable']/table/tbody/tr[10]/td/img";
    public static final String JAVASCRIPT_FILTER_IMG = "//span[@id='filterConfigurationTable']/table/tbody/tr[13]/td/img";
    public static final String JSP_FILTER_IMG = "//span[@id='filterConfigurationTable']/table/tbody/tr[16]/td/img";
    public static final String OFFICE_2010_FILTER_IMG = "//span[@id='filterConfigurationTable']/table/tbody/tr[19]/td/img";
    public static final String OFFICE_WORD_FILTER_IMG = "//span[@id='filterConfigurationTable']/table/tbody/tr[22]/td/img";
    public static final String OFFICE_EXCEL_FILTER_IMG = "//span[@id='filterConfigurationTable']/table/tbody/tr[25]/td/img";
    public static final String OFFICE_POWERPOINT_FILTER_IMG = "//span[@id='filterConfigurationTable']/table/tbody/tr[28]/td/img";
    public static final String OPEN_OFFICE_FILTER_IMG = "//span[@id='filterConfigurationTable']/table/tbody/tr[31]/td/img";
    public static final String PO_FILTER_IMG = "//span[@id='filterConfigurationTable']/table/tbody/tr[34]/td/img";
    public static final String XML_FILTER_IMG = "//span[@id='filterConfigurationTable']/table/tbody/tr[37]/td/img";

	public static final String HTML_FILTER_ADD_BUTTON = "//input[@id='html_filter_Add']";
	public static final String INDD_FILTER_ADD_BUTTON = "//input[@id='indd_filter_Add']";
	public static final String BASE_FILTER_ADD_BUTTON = "//input[@id='base_filter_Add']";
	public static final String JAVA_PROPERTIES_FILTER_ADD_BUTTON = "//input[@id='java_properties_filter_Add']";
	public static final String JAVASCRIPT_FILTER_ADD_BUTTON = "//input[@id='java_script_filter_Add']";
	public static final String JSP_FILTER_ADD_BUTTON = "//input[@id='jsp_filter_Add']";
	public static final String OFFICE_2010_FILTER_ADD_BUTTON = "//input[@id='office2010_filter_Add']";
	public static final String OFFICE_DOC_FILTER_ADD_BUTTON = "//input[@id='ms_office_doc_filter_Add']";
	public static final String OFFICE_XLS_FILTER_ADD_BUTTON = "//input[@id='ms_office_excel_filter_Add']";
	public static final String OFFICE_PPT_FILTER_ADD_BUTTON = "//input[@id='ms_office_ppt_filter_Add']";
	public static final String OPEN_OFFICE_FILTER_ADD_BUTTON = "//input[@id='openoffice_filter_Add']";
	public static final String PO_FILTER_ADD_BUTTON = "//input[@id='po_filter_Add']";
	public static final String XML_FILTER_ADD_BUTTON = "//input[@id='xml_rule_filter_Add']";
	public static final String FRAME_MAKER_FILTER_ADD_BUTTON = "//input[@id='frame_maker_filter_Add']";

    // Html Filter
    public static final String HTML_FILTER_NAME_TEXT = "htmlFilterName";
    public static final String HTML_FILTER_CANCEL_BUTTON = "//input[@id='exit' and @value='Cancel' and @type='button' and @onclick=\"closePopupDialog('htmlFilterDialog')\"]";
    public static final String HTML_FILTER_SAVE_BUTTON = "//input[@value='Save' and @type='button' and @onclick='saveHtmlFilter()']";
    public static final String HTML_FILTER_CHECK_ALL_CHECKBOX = "checkAll";
    public static final String HTML_FILTER_TAG_SAVE_BUTTON = "//input[@value='Save' and @type='button' and @onclick='htmlFilter.deleteTags()']";
    public static final String HTML_FILTER_BASE_FONT_CHECKBOX = "tags_4";  
    public static final String HTML_FILTER_A_CHECKBOX = "tags_0";
    public static final String HTML_FILTER_TAG1_CHECKBOX = "tags_1";
    public static final String HTML_FILTER_INTERNAL_TEXT_POST_FILTER_SELECT = "html_filter_baseFilterSelect";
    public static final String HTML_FILTER_TAG_TYPE_SELECT = "htmlTranslateRule";
    public static final String HTML_FILTER_TAG_ADD_BUTTON = "//input[@value='Add' and @type='button' and @onclick='htmlFilter.addTag()']";
    public static final String HTML_FILTER_TAG_NAME_TEXT = "singleTagNameToAdd";
    public static final String HTML_FILTER_TAG_INTERNAL_NAME_TEXT = "InternalTagToAdd";
    public static final String HTML_FILTER_TAG_INTERNAL_ADD_BUTTON = "//input[@value='Save' and @type='button' and @onclick='htmlFilter.addInternalTag()']";
    public static final String HTML_FILTER_TAG_ADD_SAVE_BUTTON = "//input[@value='Save' and @type='button' and @onclick='htmlFilter.addSingleTag()']";
    public static final String HTML_FILTER_CONVERT_ENTITY_CHECKBOX = "//input[@id='convertHtmlEntry']";
    public static final String HTML_FILTER_IGNORE_INVALID_TAGS_CHECKBOX = "//input[@id='ignoreInvalideHtmlTags']";
    public static final String HTML_FILTER_LOCALIZE_FUNTION_TEXT = "//input[@id='localizeFunction']";
    public static final String HTML_FILTER_TAG_MAP_KEY_TEXT="tagKeyToAdd";
    public static final String HTML_FILTER_TAG_MAP_VALUE_TEXT="tagValueToAdd";
    public static final String HTML_FILTER_TAG_MAP_SAVE_BUTTON="//input[@value='Save' and @type='button' and @onclick='htmlFilter.addMapTag()']";
    public static final String HTML_FILTER_TAG_EMBEDDABLE = "Embeddable Tags";
    public static final String HTML_FILTER_TAG_INTERNAL = "Internal Tag";
    public static final String HTML_FILTER_TAG_PAIRED = "Paired Tags";
    public static final String HTML_FILTER_TAG_SWITCH_MAP = "Switch Tag Map";
    public static final String HTML_FILTER_TAG_TRANSLATABLE_ATTRIBUTE = "Translatable Attribute";
    public static final String HTML_FILTER_TAG_UNPAIRED = "Unpaired Tags";
    public static final String HTML_FILTER_TAG_WHITE_PRESERVING = "White Preserving Tags";
    
    // InDesignIDML Filter
    public static final String INDD_FILTER_NAME_TEXT = "inddFilterName";
    public static final String INDD_FILTER_IGNORE_LINE_BREAK_CHECKBOX = "//*[@id='ignoreLineBreak']";
    public static final String INDD_FILTER_CANCEL_BUTTON = "//input[@id='exit' and @value='Cancel' and @type='button' and @onclick=\"closePopupDialog('inddFilterDialog')\"]";
    public static final String INDD_FILTER_SAVE_BUTTON = "//input[@value='Save' and @type='button' and @onclick='saveInddFilter()']";
    public static final String INDD_FILTER_TRANSLATE_HIDDEN_LAYERS_CHECKBOX = "//*[@id='transInddHiddenLayer']";
    public static final String INDD_FILTER_TRANSLATE_MASTER_LAYERS_CHECKBOX = "//*[@id='transInddMasterLayer']";
    public static final String INDD_FILTER_TRANSLATE_FILE_INFO_CHECKBOX = "//*[@id='transInddFileInfo']";
    public static final String INDD_FILTER_REPLACE_NON_BREAKING_SPACE_CHECKBOX = "//*[@id='replaceNonbreakingSpace']";
    
    //InternalText Filter
    public static final String BASE_FILTER_NAME_TEXT = "//*[@id='baseFilterName']";
    public static final String BASE_FILTER_SAVE_BUTTON = "//input[@value='Save' and @type='button' and @onclick='saveBaseFilter()']";
    public static final String BASE_FILTER_ADD_CONTENT_BUTTON = "//input[@value='Add...']";
    public static final String BASE_FILTER_CONTENT_SAVE_BUTTON = "//input[@value='Save' and @type='button' and @onclick='baseFilter.saveInternalText()']";
    public static final String BASE_FILTER_CONTENT_NAME_TEXT = "baseFilter_InternalText";
    public static final String BASE_FILTER_PRIORITY = "baseFilter_InternalText_priority";
    public static final String BASE_FILTER_CONTENT_RE_CHECKBOX = "baseFilter_InternalText_isRE";
    public static final String BASE_FILTER_CHECK_ALL_CHECKBOX = "checkAllBaseFilter";
    
    // java Properties Filter
    public static final String JAVA_PROPERTIES_FILTER_NAME_TEXT = "javaPropertiesFilterName";
    public static final String JAVA_PROPERTIES_FILTER_CANCEL_BUTTON = "//input[@id='exit' and @value='Cancel' and @type='button' and @onclick=\"closePopupDialog('javaPropertiesFilterDialog')\"]";
    public static final String JAVA_PROPERTIES_FILTER_SAVE_BUTTON = "//input[@value='Save' and @type='button' and @onclick='saveJavaProperties()']";
    public static final String JAVA_PROPERTIES_FILTER_SID_SUPPORT_CHECKBOX = "//input[@type='checkbox' and @name='supportSid' and @id='isSupportSid']"; 
    public static final String JAVA_PROPERTIES_FILTER_UNICODE_ESCAPE_CHECKBOX = "//input[@type='checkbox' and @name='unicodeEscape' and @id='isUnicodeEscape']";
    public static final String JAVA_PROPERTIES_FILTER_PRESERVE_TRAILING_SPACE_CHECKBOX = "//input[@type='checkbox' and @name='preserveSpaces' and @id='isPreserveSpaces']";
    public static final String JAVA_PROPERTIES_FILTER_SECONDARY_FILTER_SELECT = "//input[@id='secondaryFilterSelect']";
    
    // java Script Filter
    public static final String JAVASCRIPT_FILTER_NAME_TEXT = "javaScriptFilterName";
    public static final String JAVASCRIPT_FILTER_CANCEL_BUTTON = "//input[@id='exit' and @value='Cancel' and @type='button' and @onclick=\"closePopupDialog('javaScriptFilterDialog')\"]";
    public static final String JAVASCRIPT_FILTER_SAVE_BUTTON = "//input[@value='Save' and @type='button' and @onclick='saveJavaScript()']";
    public static final String JAVASCRIPT_FILTER_FUNCTION_TEXT = "javaScriptJsFunctionText";
    public static final String JAVASCRIPT_FILTER_UNICODE_ESCAPE_CHECKBOX ="//INPUT[@id='enableUnicodeEscape' and @type='checkbox' and @name='enableUnicodeEscape']";

    // Jsp Filter
    public static final String JSP_FILTER_NAME_TEXT = "jspFilterName";
    public static final String JSP_FILTER_CANCEL_BUTTON = "//input[@id='exit' and @value='Cancel' and @type='button' and @onclick=\"closePopupDialog('jspFilterDialog')\"]";
    public static final String JSP_FILTER_SAVE_BUTTON = "//input[@value='Save' and @type='button' and @onclick='saveJSPFilter()']";
    public static final String JSP_FILTER_ADD_ADDITIONAL_HEAD_CHECKBOX = "//input[@type='checkbox' and @name='addAdditionalHead' and @id='addAdditionalHead']";
    public static final String JSP_FILTER_ESCAPE_ENTITY_CHECKBOX = "//input[@type='checkbox' and @name='isEscapeEntity' and @id='isEscapeEntity']";
    
    // MS Office 2010 Filter
    public static final String OFFICE_2010_FILTER_NAME_TEXT = "o2010FilterName";
    public static final String OFFICE_2010_FILTER_DESC_TEXT = "o2010FilterDesc";
    public static final String OFFICE_2010_FILTER_CHECKALL_CHECKBOX = "o2010CheckAll";
    public static final String OFFICE_2010_FILTER_HEADER_TRANSLATE_CHECKBOX = "//input[@id='headerTranslate']";
    public static final String OFFICE_2010_FILTER_PPT_SLIDE_MASTER_TRANSLATE_CHECKBOX = "//input[@id='masterTranslate']";
    public static final String OFFICE_2010_FILTER_PPT_NOTES_TRANSLATE_CHECKBOX = "//input[@id='notesTranslate']";
    public static final String OFFICE_2010_FILTER_PPT_SLIDE_LAYOUT_TRANSLATE_CHECKBOX = "//input[@id='pptlayoutTranslate']";
    public static final String OFFICE_2010_FILTER_PPT_NOTES_MASTER_TRANSLATE_CHECKBOX = "//input[@id='notemasterTranslate']";
    public static final String OFFICE_2010_FILTER_PPT_HANDOUT_TRANSLATE_CHECKBOX = "//input[@id='handoutmasterTranslate']";
    public static final String OFFICE_2010_FILTER_EXCEL_TAB_NAMES_TRANSLATE_CHECKBOX = "//input[@id='excelTabNamesTranslate']";
    public static final String OFFICE_2010_FILTER_TOOLTIPS_TRANSLATE_CHECKBOX = "//input[@id='toolTipsTranslate']";
    
    public static final String OFFICE_2010_FILTER_WORD_STYLE_SELECT ="//select[@onchange='msoffice2010DocFilter.switchTags(this)' and @id='O2010UnextractableRule']";
    public static final String OFFICE_2010_FILTER_DONOTTRANSLATE_PARA_CHECKBOX = "//input[@type='checkbox' and @id='styles_0' and @name='DONOTTRANSLATE_para' and @onclick='msoffice2010DocFilter.checkthis(this)']";
	public static final String OFFICE_2010_FILTER_DONOTTRANSLATE_CHAR_CHECKBOX = "//input[@type='checkbox' and @id='styles_0' and @name='DONOTTRANSLATE_char' and @onclick='msoffice2010DocFilter.checkthis(this)']";

    public static final String OFFICE_2010_FILTER_STYLE_ADD_BUTTON = "//input[@type='button' and @onclick='msoffice2010DocFilter.onAdd()' and @value='Add']";
    public static final String OFFICE_2010_FILTER_SAVE_BUTTON = "//input[@value='Save' and @type='button' and @onclick='saveMSOffice2010DocFilter()']";
    public static final String OFFICE_2010_FILTER_REMOVE_BUTTON = "//input[@type='button' and @onclick='msoffice2010DocFilter.deleteTag()' and @id='O2010DeleteButton' and @value='Delete']";
    public static final String OFFICE_2010_FILTER_CANCEL_BUTTON = "//input[@id='exit' and @value='Cancel' and @type='button' and @onclick=\"closePopupDialog('msoffice2010FilterDialog')\"]";

    //Add Style window for office 2010 filter
    public static final String OFFICE_2010_FILTER_STYLE_NAME_TEXT = "o2010styleToAdd";
    public static final String OFFICE_2010_FILTER_STYLE_SAVE_BUTTON = "//input[@type='button' and @onclick='msoffice2010DocFilter.addStyle()' and @value='Save']";
    public static final String OFFICE_2010_FILTER_STYLE_CANCEL_BUTTON = "//input[@type='button' and @value='Cancel' and @id='exit']";
    	
    // MS Office Doc Filter
    public static final String OFFICE_WORD_FILTER_NAME_TEXT = "docFilterName";
    public static final String OFFICE_WORD_FILTER_CHECK_ALL_CHECKBOX = "MsCheckAll";
    public static final String OFFICE_WORD_FILTER_CANCEL_BUTTON = "//input[@id='exit' and @value='Cancel' and @type='button' and @onclick=\"closePopupDialog('msOfficeDocFilterDialog')\"]";
    public static final String OFFICE_WORD_FILTER_SAVE_BUTTON = "//input[@value='Save' and @type='button' and @onclick='saveMsOfficeDocFilter()']";
    public static final String OFFICE_WORD_FILTER_SECONDARY_FILTER_SELECT = "//select[@id='secondaryFilterSelect']";
    public static final String OFFICE_WORD_FILTER_HEADER_CHECKBOX = "//input[@type='checkbox' and @name='headerTranslate' and @id='headerTranslate']";
    public static final String OFFICE_WORD_FILTER_STYLE_SELECT = "id=DocUnextractableRule";
    public static final String OFFICE_WORD_FILTER_STYLE_PARAGRAPH = "Unextractable Word Paragraph Styles";
    public static final String OFFICE_WORD_FILTER_STYLE_CHARACTER = "Unextractable Word Character Styles";
    public static final String OFFICE_WORD_FILTER_STYLE_INTERNAL_TEXT = "Internal Text Character Styles";
    public static final String OFFICE_WORD_FILTER_STYLE_NAME_TEXT = "styleToAdd";
    public static final String OFFICE_WORD_FILTER_STYLE_ADD_BUTTON = "//input[@value='Add' and @type='button' and @onclick='officeDocFilter.onAdd()']";
    public static final String OFFICE_WORD_FILTER_STYLE_SAVE_BUTTON = "//input[@value='Save' and @type='button' and @onclick='officeDocFilter.addStyle()']";
    public static final String OFFICE_WORD_FILTER_STYLE_DELETE_BUTTON = "MSDeleteButton";
    public static final String OFFICE_WORD_FILTER_STYLE_DELETE_SAVE_BUTTOn = "//input[@value='Save' and @type='button' and @onclick='officeDocFilter.deleteStyles()']";
    
    public static final String OFFICE_WORD_FILTER_HEADER_INFO_CHECKBOX = "docHeaderTranslate";
    public static final String OFFICE_WORD_FILTER_TOOLTIPS_CHECKBOX = "docAltTranslate";
    public static final String OFFICE_WORD_FILTER_TOC_TRANSLATE_CHECKBOX = "TOCTranslate";
    public static final String OFFICE_WORD_FILTER_CONTENT_POST_FILTER_SELECT = "docContentPostFilterSelect";
    public static final String OFFICE_WORD_FILTER_TEXT_POST_FILTER_SELECT = "ms_office_doc_filter_baseFilterSelect";
    
    // MS Office Excel Filter
    public static final String OFFICE_EXCEL_FILTER_NAME_TEXT = "excelFilterName";
    public static final String OFFICE_EXCEL_FILTER_CANCEL_BUTTON = "//input[@id='exit' and @value='Cancel' and @type='button' and @onclick=\"closePopupDialog('msOfficeExcelFilterDialog')\"]";
    public static final String OFFICE_EXCEL_FILTER_SAVE_BUTTON = "//input[@value='Save' and @type='button' and @onclick='saveMsOfficeExcelFilter()']";
    public static final String OFFICE_EXCEL_FILTER_TOOLTIPS_CHECKBOX = "excelAltTranslate";
    public static final String OFFICE_EXCEL_FILTER_CONTENT_POST_FILTER_CHECKBOX = "excelContentPostFilterSelect";
    public static final String OFFICE_EXCEL_FILTER_TEXT_POST_FILTER_CHECKBOX = "ms_office_excel_filter_baseFilterSelect";
    
    // MS Office PowerPoint Filter
    public static final String OFFICE_POWERPOINT_FILTER_NAME_TEXT = "pptFilterName";
    public static final String OFFICE_POWERPOINT_FILTER_CANCEL_BUTTON = "//input[@id='exit' and @value='Cancel' and @type='button' and @onclick=\"closePopupDialog('msOfficePPTFilterDialog')\"]";
    public static final String OFFICE_POWERPOINT_FILTER_SAVE_BUTTON = "//input[@value='Save' and @type='button' and @onclick='saveMSPPTFilter()']";
    public static final String OFFICE_POWERPOINT_FILTER_EXTRACT_ALT_CHECKBOX = "//input[@type='checkbox' and @name='extractAlt' and @id='ExtractAlt']";
    public static final String OFFICE_POWERPOINT_FILTER_TOOLTIPS_CHECKBOX = "pptAltTranslate";
    public static final String OFFICE_POWERPOINT_FILTER_CONTENT_POST_FILTER_SELECT = "pptContentPostFilterSelect";
    public static final String OFFICE_POWERPOINT_FILTER_TEXT_SELECT = "ms_office_ppt_filter_baseFilterSelect";
    
    // OpenOffice Filter
    public static final String OPENOFFICE_FILTER_NAME_TEXT = "ooFilterName";
    public static final String OPENOFFICE_FILTER_CANCEL_BUTTON = "//input[@id='exit' and @value='Cancel' and @type='button' and @onclick=\"closePopupDialog('openofficeFilterDialog')\"]";
    public static final String OPENOFFICE_FILTER_SAVE_BUTTON = "//input[@value='Save' and @type='button' and @onclick='saveOpenOfficeDocFilter()']";
    public static final String OPENOFFICE_FILTER_HEADER_INFO_CHECKBOX = "//input[@type='checkbox' and @name='headerTranslate' and @id='headerTranslate']";

    // Portable Object Filter
    public static final String PO_FILTER_NAME_TEXT = "poFilterName";
    public static final String PO_FILTER_CANCEL_BUTTON = "//input[@id='exit' and @value='Cancel' and @type='button' and @onclick=\"closePopupDialog('poFilterDialog')\"]";
    public static final String PO_FILTER_SAVE_BUTTON = "//input[@value='Save' and @type='button' and @onclick='savePOFilter()']";
    public static final String PO_FILTER_SECONDARY_FILTER_SELECT = "secondaryFilterSelect";
   
    // Xml Filter
    public static final String XML_FILTER_NAME_TEXT = "xmlRuleFilterName";
    public static final String XML_FILTER_CANCEL_BUTTON = "//input[@id='exit' and @value='Cancel' and @type='button' and @onclick=\"closePopupDialog('xmlRuleFilterDialog');xmlFilter.closeConfiguredTagDialog('xmlRuleFilter_configured_tag_Dialog');closePopupDialog('deleteXmlTagDialog')\"]";
    public static final String XML_FILTER_SAVE_BUTTON = "//input[@value='Save' and @type='button' and @onclick='saveXmlRuleFilter()']";
}
