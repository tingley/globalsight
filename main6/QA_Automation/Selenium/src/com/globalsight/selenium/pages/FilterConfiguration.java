package com.globalsight.selenium.pages;

public class FilterConfiguration implements BasePage
{

    // Filter Configuration
    public static final String CHECK_ALL_CHECKBOX = "//input[@type='checkbox']";
    public static final String EXPAND_ALL_BUTTON = "//input[@id='expandAllFilters']";
    public static final String COLLAPSE_ALL_BUTTON = "collapseAllFilters";
    public static final String EXPORT_BUTTON = "//input[@value='Export']";
    public static final String IMPORT_BUTTON = "//input[@value='Import']";
    public static final String IMPORT_DIRECTORY_TYPE = "id=fileInput";
    public static final String IMPORT_UPLOAD_BUTTON = "id=uploadBtn";
    public static final String IMPORT_UPLOAD_OK_BUTTON = "id=okBtn";
    		
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
	public static final String JAVASCRIPT_FILTER_ADD_BUTTON = "id=java_script_filter_Add";
	public static final String JSP_FILTER_ADD_BUTTON = "//input[@id='jsp_filter_Add']";
	public static final String OFFICE_2010_FILTER_ADD_BUTTON = "//input[@id='office2010_filter_Add']";
	public static final String OFFICE_DOC_FILTER_ADD_BUTTON = "//input[@id='ms_office_doc_filter_Add']";
	public static final String OFFICE_XLS_FILTER_ADD_BUTTON = "//input[@id='ms_office_excel_filter_Add']";
	public static final String OFFICE_PPT_FILTER_ADD_BUTTON = "//input[@id='ms_office_ppt_filter_Add']";
	public static final String OPEN_OFFICE_FILTER_ADD_BUTTON = "//input[@id='openoffice_filter_Add']";
	public static final String PLAIN_TEXT_FILTER_ADD_BUTTON = "id=plain_text_filter_Add";
	public static final String PO_FILTER_ADD_BUTTON = "//input[@id='po_filter_Add']";
	public static final String QA_FILTER_ADD_BUTTON = "id=qa_filter_Add";
	public static final String XML_FILTER_ADD_BUTTON = "//input[@id='xml_rule_filter_Add']";
	public static final String FRAME_MAKER_FILTER_ADD_BUTTON = "//input[@id='frame_maker_filter_Add']";

    // FrameMaker 9 Mif 9 Filter
    public static final String FM_9_FILTER_NAME_TEXT = "fmFilterName";
    public static final String FM_9_FILTER_DESCRIPTION_TEXT = "fmDesc";
    public static final String FM_9_FILTER_CANCEL_BUTTON = "xpath=(//input[@id='exit'])[50]";
    public static final String FM_9_FILTER_SAVE_BUTTON = "xpath=(//input[@value='Save'])[48]";
    public static final String FM_9_TRANSLATE_LEFT_MASTER_PAGE_CHECKBOX = "id=checkLeftMasterPage";
    public static final String FM_9_TRANSLATE_RIGHT_MASTER_PAGE_CHECKBOX = "id=checkRightMasterPage";
    public static final String FM_9_TRANSLATE_OTHER_MASTER_PAGE_CHECKBOX = "id=checkOtherMasterPage";
    
    // Html Filter
    public static final String HTML_FILTER_NAME_TEXT = "htmlFilterName";
    public static final String HTML_FILTER_CANCEL_BUTTON = "//input[@id='exit' and @value='Cancel' and @type='button' and @onclick=\"closePopupDialog('htmlFilterDialog')\"]";
    public static final String HTML_FILTER_SAVE_BUTTON = "//input[@value='Save' and @type='button' and @onclick='saveHtmlFilter()']";
    public static final String HTML_FILTER_CHECK_ALL_CHECKBOX = "id=checkAll";
    public static final String HTML_FILTER_TAG_SAVE_BUTTON = "//input[@value='Save' and @type='button' and @onclick='htmlFilter.deleteTags()']";
    public static final String HTML_FILTER_TAG_SAVE_BUTTON_SWITCHTAG = "xpath=(//input[@value='Save'])[35]";
    public static final String HTML_FILTER_TAG_SAVE_BUTTON_INTERNAL = "xpath=(//input[@value='Save'])[34]";
    public static final String HTML_FILTER_BASE_FONT_CHECKBOX = "tags_4";  
    public static final String HTML_FILTER_A_CHECKBOX = "tags_0";
    public static final String HTML_FILTER_TAG1_CHECKBOX = "tags_1";
    public static final String HTML_FILTER_INTERNAL_TEXT_POST_FILTER_SELECT = "html_filter_baseFilterSelect";
    public static final String HTML_FILTER_TAG_TYPE_SELECT = "id=htmlTranslateRule";
    public static final String HTML_FILTER_TAG_ADD_BUTTON = "//input[@value='Add' and @type='button' and @onclick='htmlFilter.addTag()']";
    public static final String HTML_FILTER_TAG_ADD_BUTTON_2 = "xpath=(//input[@value='Add'])[21]";
    public static final String HTML_FILTER_TAG_NAME_TEXT = "id=singleTagNameToAdd";
    public static final String HTML_FILTER_TAG_INTERNAL_NAME_TEXT = "InternalTagToAdd";
    public static final String HTML_FILTER_TAG_INTERNAL_ADD_BUTTON = "//input[@value='Save' and @type='button' and @onclick='htmlFilter.addInternalTag()']";
    public static final String HTML_FILTER_TAG_ADD_SAVE_BUTTON = "//input[@value='Save' and @type='button' and @onclick='htmlFilter.addSingleTag()']";
    public static final String HTML_FILTER_TAG_ADD_CANCEL_BUTTON = "//input[@value='Cancel' and @type='button' and @onclick='htmlFilter.addSingleTag()']";
    public static final String HTML_FILTER_CONVERT_ENTITY_CHECKBOX = "//input[@id='convertHtmlEntry']";
    public static final String HTML_FILTER_IGNORE_INVALID_TAGS_CHECKBOX = "//input[@id='ignoreInvalideHtmlTags']";
    public static final String HTML_FILTER_ADD_RTL_DIRECTIONALITY_CHECKBOX = "id=addRtlDirectionality";
    public static final String HTML_FILTER_WHITESPACE_HANDLING_RADIO_1 = "name=wsHandleModeHTML";
    public static final String HTML_FILTER_WHITESPACE_HANDLING_RADIO_2 = "document.fpForm.wsHandleModeHTML[";
    public static final String HTML_FILTER_LOCALIZE_FUNTION_TEXT = "//input[@id='localizeFunction']";
    public static final String HTML_FILTER_BASE_TEXT_POST_FILTER_SELECT = "id=html_filter_baseFilterSelect";
    public static final String HTML_FILTER_TAG_MAP_KEY_TEXT="tagKeyToAdd";
    public static final String HTML_FILTER_TAG_MAP_VALUE_TEXT="tagValueToAdd";
    public static final String HTML_FILTER_TAG_MAP_SAVE_BUTTON="//input[@value='Save' and @type='button' and @onclick='htmlFilter.addMapTag()']";
    public static final String HTML_FILTER_TAG_EMBEDDABLE_LABEL = "label=Embeddable Tags";
    public static final String HTML_FILTER_TAG_INTERNAL_LABEL = "label=Internal Tag";
    public static final String HTML_FILTER_TAG_PAIRED_LABEL = "label=Paired Tags";
    public static final String HTML_FILTER_TAG_SWITCH_MAP_LABEL = "label=Switch Tag Map";
    public static final String HTML_FILTER_TAG_TRANSLATABLE_ATTRIBUTE_LABEL = "label=Translatable Attribute";
    public static final String HTML_FILTER_TAG_UNPAIRED_LABEL = "label=Unpaired Tags";
    public static final String HTML_FILTER_TAG_WHITE_PRESERVING_LABEL = "label=White Preserving Tags";
    
    // InDesignIDML Filter
    public static final String INDD_FILTER_NAME_TEXT = "inddFilterName";
    public static final String INDD_FILTER_DESCRIPTION_TEXT = "id=inddFilterDesc";
    public static final String INDD_FILTER_CANCEL_BUTTON = "xpath=(//input[@id='exit'])[51]]";
    public static final String INDD_FILTER_SAVE_BUTTON = "xpath=(//input[@value='Save'])[49]";
    public static final String INDD_FILTER_TRANSLATE_HIDDEN_LAYERS_CHECKBOX = "//*[@id='transInddHiddenLayer']";
    public static final String INDD_FILTER_TRANSLATE_MASTER_LAYERS_CHECKBOX = "//*[@id='transInddMasterLayer']";
    public static final String INDD_FILTER_TRANSLATE_FILE_INFO_CHECKBOX = "//*[@id='transInddFileInfo']";
    public static final String INDD_FILTER_TRANSLATE_HYPERLINKS_CHECKBOX = "id=transHyperlinks";
    public static final String INDD_FILTER_TRANSLATE_HIDDEN_CONDITIONAL_TEXT_CHECKBOX = "id=transHiddenCondText";
    public static final String INDD_FILTER_IGNORE_TRACKING_AND_KERNING_TEXT_CHECKBOX = "id=skipTrackingKerningId";
    public static final String INDD_FILTER_IGNORE_LINE_BREAK_CHECKBOX = "//*[@id='ignoreLineBreak']";
    public static final String INDD_FILTER_REPLACE_NON_BREAKING_SPACE_CHECKBOX = "//*[@id='replaceNonbreakingSpace']";
    
    //InternalText Filter
    public static final String BASE_FILTER_NAME_TEXT = "//*[@id='baseFilterName']";
    public static final String BASE_FILTER_SAVE_BUTTON = "//input[@value='Save' and @type='button' and @onclick='saveBaseFilter()']";
    public static final String BASE_FILTER_ADD_CONTENT_BUTTON = "//input[@value='Add...']";
    public static final String BASE_FILTER_CONTENT_SAVE_BUTTON = "//input[@value='Save' and @type='button' and @onclick='baseFilter.saveInternalText()']";
    public static final String BASE_FILTER_ESCAPING_SAVE_BUTTON = "xpath=(//input[@value='Save'])[24]";
    public static final String BASE_FILTER_CONTENT_NAME_TEXT = "id=baseFilter_InternalText";
    public static final String BASE_FILTER_PRIORITY_TEXT = "id=baseFilter_InternalText_priority";
    public static final String BASE_FILTER_CONTENT_RE_CHECKBOX = "baseFilter_InternalText_isRE";
    public static final String BASE_FILTER_CHECK_ALL_CHECKBOX = "checkAllBaseFilter";
    public static final String BASE_FILTER_ESCAPING_CONTENT_NAME_TEXT = "id=baseFilter_escaping_char";
    public static final String BASE_FILTER_ESCAPING_IMPORT_CHECKBOX ="id=baseFilter_escaping_import";
    public static final String BASE_FILTER_ESCAPING_EXPORT_CHECKBOX ="id=baseFilter_escaping_export";
    public static final String BASE_FILTER_ESCAPING_PRIORITY = "id=baseFilter_escaping_priority";
    
    public static final String BASE_FILTER_SELECT = "id=baseFilterRulesSection";
    public static final String BASE_FILTER_INTERNAL_TEXT = "Internal Text";
    public static final String BASE_FILTER_ESCAPING = "Escaping";
    
    // java Properties Filter
    public static final String JAVA_PROPERTIES_FILTER_NAME_TEXT = "javaPropertiesFilterName";
    public static final String JAVA_PROPERTIES_FILTER_CANCEL_BUTTON = "//input[@id='exit' and @value='Cancel' and @type='button' and @onclick=\"closePopupDialog('javaPropertiesFilterDialog')\"]";
    public static final String JAVA_PROPERTIES_FILTER_SAVE_BUTTON = "//input[@value='Save' and @type='button' and @onclick='saveJavaProperties()']";
    public static final String JAVA_PROPERTIES_FILTER_SID_SUPPORT_CHECKBOX = "//input[@type='checkbox' and @name='supportSid' and @id='isSupportSid']"; 
    public static final String JAVA_PROPERTIES_FILTER_UNICODE_ESCAPE_CHECKBOX = "//input[@type='checkbox' and @name='unicodeEscape' and @id='isUnicodeEscape']";
    public static final String JAVA_PROPERTIES_FILTER_PRESERVE_TRAILING_SPACE_CHECKBOX = "//input[@type='checkbox' and @name='preserveSpaces' and @id='isPreserveSpaces']";
    public static final String JAVA_PROPERTIES_FILTER_SECONDARY_FILTER_SELECT = "id=secondaryFilterSelect";
    public static final String JAVA_PROPERTIES_FILTER_SECONDARY_FILTER_DEFAULT_FILTER_LABEL = "label=HTML_Filter(Default)";
    public static final String JAVA_PROPERTIES_FILTER_TEXT_POST_FILTER_SELECT = "java_properties_filter_baseFilterSelect";
    
    // java Script Filter
    public static final String JAVASCRIPT_FILTER_NAME_TEXT = "javaScriptFilterName";
    public static final String JAVASCRIPT_FILTER_CANCEL_BUTTON = "//input[@id='exit' and @value='Cancel' and @type='button' and @onclick=\"closePopupDialog('javaScriptFilterDialog')\"]";
    public static final String JAVASCRIPT_FILTER_SAVE_BUTTON = "//input[@value='Save' and @type='button' and @onclick='saveJavaScript()']";
    public static final String JAVASCRIPT_FILTER_FUNCTION_TEXT = "javaScriptJsFunctionText";
    public static final String JAVASCRIPT_FILTER_UNICODE_ESCAPE_CHECKBOX ="id=enableUnicodeEscape";
    public static final String JAVASCRIPT_FILTER_TEXT_POST_FILTER_SELECT = "id=java_script_filter_baseFilterSelect";

    // Jsp Filter
    public static final String JSP_FILTER_NAME_TEXT = "jspFilterName";
    public static final String JSP_FILTER_CANCEL_BUTTON = "//input[@id='exit' and @value='Cancel' and @type='button' and @onclick=\"closePopupDialog('jspFilterDialog')\"]";
    public static final String JSP_FILTER_SAVE_BUTTON = "//input[@value='Save' and @type='button' and @onclick='saveJSPFilter()']";
    public static final String JSP_FILTER_ADD_ADDITIONAL_HEAD_CHECKBOX = "//input[@type='checkbox' and @name='addAdditionalHead' and @id='addAdditionalHead']";
    public static final String JSP_FILTER_ESCAPE_ENTITY_CHECKBOX = "//input[@type='checkbox' and @name='isEscapeEntity' and @id='isEscapeEntity']";
    public static final String JSP_FILTER_TEXT_POST_FILTER_SELECT = "id=jsp_filter_baseFilterSelect";
    
    // MS Office 2010 Filter
    public static final String OFFICE_2010_FILTER_NAME_TEXT = "o2010FilterName";
    public static final String OFFICE_2010_FILTER_DESC_TEXT = "o2010FilterDesc";
    public static final String OFFICE_2010_FILTER_CHECKALL_CHECKBOX = "o2010CheckAll";
    public static final String OFFICE_2010_FILTER_HEADER_TRANSLATE_CHECKBOX = "id=headerTranslate";
    public static final String OFFICE_2010_FILTER_PPT_NOTES_TRANSLATE_CHECKBOX = "id=notemasterTranslate";
    public static final String OFFICE_2010_FILTER_FOOTER_NOTE_TRANSLATE_CHECKBOX = "id=footendnoteTranslate";
    public static final String OFFICE_2010_FILTER_PPT_SPEAKERS_NOTES_TRANSLATE_CHECKBOX = "id=notesTranslate";
    public static final String OFFICE_2010_FILTER_PPT_SLIDE_MASTER_TRANSLATE_CHECKBOX = "id=masterTranslate";
    public static final String OFFICE_2010_FILTER_PPT_SLIDE_LAYOUT_TRANSLATE_CHECKBOX = "id=pptlayoutTranslate";
    public static final String OFFICE_2010_FILTER_PPT_NOTES_MASTER_TRANSLATE_CHECKBOX = "id=notemasterTranslate";
    public static final String OFFICE_2010_FILTER_PPT_HANDOUT_TRANSLATE_CHECKBOX = "id=handoutmasterTranslate";
    public static final String OFFICE_2010_FILTER_EXCEL_TAB_NAMES_TRANSLATE_CHECKBOX = "id=excelTabNamesTranslate";
    public static final String OFFICE_2010_FILTER_HIDDEN_TEXT_TRANSLATE_CHECKBOX = "id=hiddenTextTranslate";
    public static final String OFFICE_2010_FILTER_TOOLTIPS_TRANSLATE_CHECKBOX = "id=toolTipsTranslate";
    public static final String OFFICE_2010_FILTER_URL_TRANSLATE_CHECKBOX = "id=urlTranslate";
    public static final String OFFICE_2010_FILTER_TOC_TRANSLATE_CHECKBOX = "id=tableOfContentTranslate";
    public static final String OFFICE_2010_FILTER_COMMENT_TRANSLATE_CHECKBOX = "id=commentTranslate";
    public static final String OFFICE_2010_FILTER_EXCEL_SEGMENT_ORDER_FOR_V20_ONLY_RADEO_1 = "name=excelOrder";
    public static final String OFFICE_2010_FILTER_EXCEL_SEGMENT_ORDER_FOR_V20_ONLY_RADEO_2 = "xpath=(//input[@name='excelOrder'])[2]";
    public static final String OFFICE_2010_FILTER_EXCEL_SEGMENT_ORDER_FOR_V20_ONLY_RADEO_3 = "xpath=(//input[@name='excelOrder'])[3]";
    public static final String OFFICE_2010_FILTER_CONTENT_POST_FILTER_SELECT = "id=office2010ContentPostFilterSelect";
    public static final String OFFICE_2010_FILTER_TAG_CDATA_POST_FILTER_DEFAULT_FILTER_LABEL = "label=HTML_Filter(Default)";
    public static final String OFFICE_2010_FILTER_BASE_TEXT_POST_FILTER_SELECT = "id=office2010_filter_baseFilterSelect";
    public static final String OFFICE_2010_FILTER_BASE_TEXT_POST_FILTER_MESSAGE = "Only the Base Text post-filter setting will be used for the base text function. Base Text settings from other secondary filter or post-filter will be ignored";
    public static final String OFFICE_2010_FILTER_TAG_TYPE_SELECT = "id=O2010UnextractableRule";
    public static final String OFFICE_2010_FILTER_TAG_UNEXTRACTABLE_WORD_PARAGRAPH_STYLES_LABEL = "label=Unextractable Word Paragraph Styles";
    public static final String OFFICE_2010_FILTER_TAG_UNEXTRACTABLE_WORD_CHARACTER_STYLES_LABEL = "label=Unextractable Word Character Styles";
    public static final String OFFICE_2010_FILTER_TAG_UNEXTRACTABLE_EXCEL_CELL_STYLES_LABEL = "label=Unextractable Excel Cell Styles";
    public static final String OFFICE_2010_FILTER_TAG_WORD_INTERNAL_TEXT_CHARACTER_STYLES_LABEL = "label=Word Internal Text Character Styles";
    public static final String OFFICE_2010_FILTER_TAG_EXCEL_INTERNAL_TEXT_CELL_STYLES_LABEL = "label=Excel Internal Text Cell Styles";
    
    public static final String OFFICE_2010_FILTER_TAG_STYLES_ADD_BUTTON = "xpath=(//input[@value='Add'])[17]";
    public static final String OFFICE_2010_FILTER_TAG_STYLES_SAVE_BUTTON = "xpath=(//input[@value='Save'])[39]";
    public static final String OFFICE_2010_FILTER_TAG_STYLES_CHECKALL_CHECKBOX = "id=o2010CheckAll";
    public static final String OFFICE_2010_FILTER_TAG_STYLES_NAME_TEXT = "id=o2010styleToAdd";

    		
    public static final String OFFICE_2010_FILTER_WORD_STYLE_SELECT ="//select[@onchange='msoffice2010DocFilter.switchTags(this)' and @id='O2010UnextractableRule']";
    public static final String OFFICE_2010_FILTER_DONOTTRANSLATE_PARA_CHECKBOX = "//input[@type='checkbox' and @id='styles_0' and @name='DONOTTRANSLATE_para' and @onclick='msoffice2010DocFilter.checkthis(this)']";
	public static final String OFFICE_2010_FILTER_DONOTTRANSLATE_CHAR_CHECKBOX = "//input[@type='checkbox' and @id='styles_0' and @name='DONOTTRANSLATE_char' and @onclick='msoffice2010DocFilter.checkthis(this)']";

    public static final String OFFICE_2010_FILTER_STYLE_ADD_BUTTON = "//input[@type='button' and @onclick='msoffice2010DocFilter.onAdd()' and @value='Add']";
    public static final String OFFICE_2010_FILTER_SAVE_BUTTON = "xpath=(//input[@value='Save'])[9]";
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
    public static final String OFFICE_WORD_FILTER_SAVE_BUTTON = "xpath=(//input[@value='Save'])[4]";
    public static final String OFFICE_WORD_FILTER_SECONDARY_FILTER_SELECT = "//select[@id='secondaryFilterSelect']";
    public static final String OFFICE_WORD_FILTER_HEADER_CHECKBOX = "id=docHeaderTranslate";
    public static final String OFFICE_WORD_FILTER_STYLE_SELECT = "id=DocUnextractableRule";
    public static final String OFFICE_WORD_FILTER_STYLE_PARAGRAPH = "Unextractable Word Paragraph Styles";
    public static final String OFFICE_WORD_FILTER_STYLE_CHARACTER = "Unextractable Word Character Styles";
    public static final String OFFICE_WORD_FILTER_STYLE_INTERNAL_TEXT = "Internal Text Character Styles";
    public static final String OFFICE_WORD_FILTER_STYLE_NAME_TEXT = "styleToAdd";
    public static final String OFFICE_WORD_FILTER_STYLE_ADD_BUTTON = "//input[@value='Add' and @type='button' and @onclick='officeDocFilter.onAdd()']";
    public static final String OFFICE_WORD_FILTER_TAG_STYLES_NAME_TEXT = "id=styleToAdd";
    public static final String OFFICE_WORD_FILTER_STYLE_SAVE_BUTTON = "//input[@value='Save' and @type='button' and @onclick='officeDocFilter.addStyle()']";
    public static final String OFFICE_WORD_FILTER_STYLE_DELETE_BUTTON = "MSDeleteButton";
    public static final String OFFICE_WORD_FILTER_STYLE_DELETE_SAVE_BUTTOn = "//input[@value='Save' and @type='button' and @onclick='officeDocFilter.deleteStyles()']";
    
    public static final String OFFICE_WORD_FILTER_HEADER_INFO_CHECKBOX = "docHeaderTranslate";
    public static final String OFFICE_WORD_FILTER_TOOLTIPS_CHECKBOX = "docAltTranslate";
    public static final String OFFICE_WORD_FILTER_TOC_TRANSLATE_CHECKBOX = "TOCTranslate";
    public static final String OFFICE_WORD_FILTER_CONTENT_POST_FILTER_SELECT = "docContentPostFilterSelect";
    public static final String OFFICE_WORD_FILTER_TAG_CDATA_POST_FILTER_DEFAULT_FILTER_LABEL = "label=HTML_Filter(Default)";
    public static final String OFFICE_WORD_FILTER_TEXT_POST_FILTER_SELECT = "ms_office_doc_filter_baseFilterSelect";
    
    // MS Office Excel Filter
    public static final String OFFICE_EXCEL_FILTER_NAME_TEXT = "excelFilterName";
    public static final String OFFICE_EXCEL_FILTER_CANCEL_BUTTON = "//input[@id='exit' and @value='Cancel' and @type='button' and @onclick=\"closePopupDialog('msOfficeExcelFilterDialog')\"]";
    public static final String OFFICE_EXCEL_FILTER_SAVE_BUTTON = "xpath=(//input[@value='Save'])[5]";
    public static final String OFFICE_EXCEL_FILTER_TOOLTIPS_CHECKBOX = "excelAltTranslate";
    public static final String OFFICE_EXCEL_FILTER_TABNAMES_CHECKBOX = "id=excelTabNamesTranslate";
    public static final String OFFICE_EXCEL_FILTER_CONTENT_POST_FILTER_SELECT = "excelContentPostFilterSelect";
    public static final String OFFICE_EXCEL_FILTER_TEXT_POST_FILTER_SELECT = "ms_office_excel_filter_baseFilterSelect";
    
    
    // MS Office PowerPoint Filter
    public static final String OFFICE_POWERPOINT_FILTER_NAME_TEXT = "pptFilterName";
    public static final String OFFICE_POWERPOINT_FILTER_CANCEL_BUTTON = "//input[@id='exit' and @value='Cancel' and @type='button' and @onclick=\"closePopupDialog('msOfficePPTFilterDialog')\"]";
    public static final String OFFICE_POWERPOINT_FILTER_SAVE_BUTTON = "xpath=(//input[@value='Save'])[6]";
    public static final String OFFICE_POWERPOINT_FILTER_EXTRACT_ALT_CHECKBOX = "//input[@type='checkbox' and @name='extractAlt' and @id='ExtractAlt']";
    public static final String OFFICE_POWERPOINT_FILTER_TOOLTIPS_CHECKBOX = "pptAltTranslate";
    public static final String OFFICE_POWERPOINT_FILTER_NOTES_CHECKBOX = "pptNotesTranslate";
    public static final String OFFICE_POWERPOINT_FILTER_CONTENT_POST_FILTER_SELECT = "pptContentPostFilterSelect";
    public static final String OFFICE_POWERPOINT_FILTER_TEXT_SELECT = "ms_office_ppt_filter_baseFilterSelect";
    
    // OpenOffice Filter
    public static final String OPENOFFICE_FILTER_NAME_TEXT = "ooFilterName";
    public static final String OPENOFFICE_FILTER_CANCEL_BUTTON = "//input[@id='exit' and @value='Cancel' and @type='button' and @onclick=\"closePopupDialog('openofficeFilterDialog')\"]";
    public static final String OPENOFFICE_FILTER_SAVE_BUTTON = "//input[@value='Save' and @type='button' and @onclick='saveOpenOfficeDocFilter()']";
    public static final String OPENOFFICE_FILTER_HEADER_INFO_CHECKBOX = "id=headerTranslate";
    public static final String OPENOFFICE_FILTER_STYLE_SELECT = "id=OOUnextractableRule";
    public static final String OPENOFFICE_FILTER_STYLE_PARAGRAPH_VALUE = "label=Unextractable Word Paragraph Styles";
    public static final String OPENOFFICE_FILTER_STYLE_CHARACTER_VALUE = "label=Unextractable Word Character Styles";
    public static final String OPENOFFICE_FILTER_CHECK_ALL_CHECKBOX = "id=ooCheckAll";
    public static final String OPENOFFICE_FILTER_STYLE_ADD_BUTTON = "xpath=(//input[@value='Add'])[17]"; 
    public static final String OPENOFFICE_FILTER_TAG_STYLES_NAME_TEXT = "id=oostyleToAdd";
    public static final String OPENOFFICE_FILTER_STYLE_SAVE_BUTTON = "xpath=(//input[@value='Save'])[38]";
    
    // Plain Text Filter
    public static final String PLAIN_TEXT_FILTER_NAME_TEXT = "id=plaintextFilterName";
    public static final String PLAIN_TEXT_FILTER_SAVE_BUTTON = "xpath=(//input[@value='Save'])[25]";
    public static final String PLAIN_TEXT_FILTER_TEXT_POST_FILTER_SELECT = "id=plain_text_filter_baseFilterSelect";
    public static final String PLAIN_TEXT_FILTER_SECONDARY_FILTER_SELECT = "id=elementPostFilter";
    public static final String PLAIN_TEXT_FILTER_SECONDARY_FILTER_DEFAULT_FILTER_LABEL = "label=HTML_Filter(Default)";
    public static final String PLAIN_TEXT_FILTER_RULE_ADD_BUTTON = "//input[@value='Add...']";
    
    public static final String PLAIN_TEXT_FILTER_RULE_SELECT = "id=plaintextFilterRulesSection";
    public static final String PLAIN_TEXT_FILTER_RULE_SELECT_VALUE_CUSTOM_SID_RULE= "label=Custom SID Rule";
    public static final String PLAIN_TEXT_FILTER_RULE_SELECT_VALUE_CUSTOM_TEXT_RULE = "label=Custom Text Rule";
    public static final String PLAIN_TEXT_FILTER_RULE_CHECK_ALL_CHECKBOX = "id=checkAllPlainTextFilter";

    public static final String PLAIN_TEXT_FILTER_CUSTOM_TEXT_RULE_START_STR_TEXT = "id=plainTextFilter_customTextRule_startStr";
    public static final String PLAIN_TEXT_FILTER_CUSTOM_TEXT_RULE_START_IS_REGEX_CHECKBOX = "id=plainTextFilter_customTextRule_startIs";
    public static final String PLAIN_TEXT_FILTER_CUSTOM_TEXT_RULE_START_OCCURRENCE_FIRST_RADIO_BUTTON = "id=plainTextFilter_customTextRule_startOcc1";
    public static final String PLAIN_TEXT_FILTER_CUSTOM_TEXT_RULE_START_OCCURRENCE_LAST_RADIO_BUTTON = "id=plainTextFilter_customTextRule_startOcc2";
    public static final String PLAIN_TEXT_FILTER_CUSTOM_TEXT_RULE_START_OCCURRENCE_CUSTOM_RADIO_BUTTON = "id=plainTextFilter_customTextRule_startOcc3";
	public static final String PLAIN_TEXT_FILTER_CUSTOM_TEXT_RULE_START_OCCURRENCE_CUSTOM_TEXT = "id=plainTextFilter_customTextRule_startOccTimes";
	public static final String PLAIN_TEXT_FILTER_CUSTOM_TEXT_RULE_FINISH_STR_TEXT = "id=plainTextFilter_customTextRule_finishStr";
	public static final String PLAIN_TEXT_FILTER_CUSTOM_TEXT_RULE_FINISH_IS_REGEX_CHECKBOX = "id=plainTextFilter_customTextRule_finishIs";
	public static final String PLAIN_TEXT_FILTER_CUSTOM_TEXT_RULE_FINISH_OCCURRENCE_FIRST_RADIO_BUTTON = "id=plainTextFilter_customTextRule_finishOcc1";
	public static final String PLAIN_TEXT_FILTER_CUSTOM_TEXT_RULE_FINISH_OCCURRENCE_LAST_RADIO_BUTTON = "id=plainTextFilter_customTextRule_finishOcc2";
	public static final String PLAIN_TEXT_FILTER_CUSTOM_TEXT_RULE_FINISH_OCCURRENCE_CUSTOM_RADIO_BUTTON = "id=plainTextFilter_customTextRule_finishOcc3";
	public static final String PLAIN_TEXT_FILTER_CUSTOM_TEXT_RULE_FINISH_OCCURRENCE_CUSTOM_TEXT = "id=plainTextFilter_customTextRule_finishOccTimes";
	public static final String PLAIN_TEXT_FILTER_CUSTOM_TEXT_RULE_IS_MULTILINE_TEXT = "id=plainTextFilter_customTextRule_isMultiline";
	public static final String PLAIN_TEXT_FILTER_CUSTOM_TEXT_RULE_PRIORITY_TEXT = "id=plainTextFilter_customTextRule_priority";
	public static final String PLAIN_TEXT_FILTER_CUSTOM_TEXT_RULE_SAVE_BUTTON = "xpath=(//input[@value='Save'])[26]";

	public static final String PLAIN_TEXT_FILTER_CUSTOM_SID_RULE_START_STR_TEXT = "id=plainTextFilter_customSidRule_startStr";
    public static final String PLAIN_TEXT_FILTER_CUSTOM_SID_RULE_START_IS_REGEX_CHECKBOX = "id=plainTextFilter_customSidRule_startIs";
    public static final String PLAIN_TEXT_FILTER_CUSTOM_SID_RULE_START_OCCURRENCE_FIRST_RADIO_BUTTON = "id=plainTextFilter_customSidRule_startOcc1";
    public static final String PLAIN_TEXT_FILTER_CUSTOM_SID_RULE_START_OCCURRENCE_LAST_RADIO_BUTTON = "id=plainTextFilter_customSidRule_startOcc2";
    public static final String PLAIN_TEXT_FILTER_CUSTOM_SID_RULE_START_OCCURRENCE_CUSTOM_RADIO_BUTTON = "id=plainTextFilter_customSidRule_startOcc3";
	public static final String PLAIN_TEXT_FILTER_CUSTOM_SID_RULE_START_OCCURRENCE_CUSTOM_TEXT = "id=plainTextFilter_customSidRule_startOccTimes";
	public static final String PLAIN_TEXT_FILTER_CUSTOM_SID_RULE_FINISH_STR_TEXT = "id=plainTextFilter_customSidRule_finishStr";
	public static final String PLAIN_TEXT_FILTER_CUSTOM_SID_RULE_FINISH_IS_REGEX_CHECKBOX = "id=plainTextFilter_customSidRule_finishIs";
	public static final String PLAIN_TEXT_FILTER_CUSTOM_SID_RULE_FINISH_OCCURRENCE_FIRST_RADIO_BUTTON = "id=plainTextFilter_customSidRule_finishOcc1";
	public static final String PLAIN_TEXT_FILTER_CUSTOM_SID_RULE_FINISH_OCCURRENCE_LAST_RADIO_BUTTON = "id=plainTextFilter_customSidRule_finishOcc2";
	public static final String PLAIN_TEXT_FILTER_CUSTOM_SID_RULE_FINISH_OCCURRENCE_CUSTOM_RADIO_BUTTON = "id=plainTextFilter_customSidRule_finishOcc3";
	public static final String PLAIN_TEXT_FILTER_CUSTOM_SID_RULE_FINISH_OCCURRENCE_CUSTOM_TEXT = "id=plainTextFilter_customSidRule_finishOccTimes";
	public static final String PLAIN_TEXT_FILTER_CUSTOM_SID_RULE_SAVE_BUTTON = "xpath=(//input[@value='Save'])[27]";
	
	
    
	
    // Portable Object Filter
    public static final String PO_FILTER_NAME_TEXT = "poFilterName";
    public static final String PO_FILTER_CANCEL_BUTTON = "//input[@id='exit' and @value='Cancel' and @type='button' and @onclick=\"closePopupDialog('poFilterDialog')\"]";
    public static final String PO_FILTER_SAVE_BUTTON = "//input[@value='Save' and @type='button' and @onclick='savePOFilter()']";
    public static final String PO_FILTER_SECONDARY_FILTER_SELECT = "secondaryFilterSelect";
    public static final String PO_FILTER_SECONDARY_FILTER_DEFAULT_FILTER_LABEL = "label=HTML_Filter(Default)";
    public static final String PO_FILTER_TEXT_POST_FILTER_SELECT = "id=po_filter_baseFilterSelect";
    

 // QA Filter
    public static final String QA_FILTER_NAME_TEXT = "id=qaFilterName";
    public static final String QA_FILTER_SAVE_BUTTON = "xpath=(//input[@value='Save'])[18]";
    public static final String QA_FILTER_SOURCE_EQUAL_TO_TARGET_STRING_EXPANSION_OF_CHECKBOX = "//table[@id='qaRuleContentTable']/tbody/tr[";
    public static final String QA_FILTER_RULE_ADD_BUTTON = "//input[@value='Add...']";
    
    public static final String QA_FILTER_RULE_CHECK_TEXT = "id=qaFilter_rule_check";
    public static final String QA_FILTER_RULE_CHECK_IS_REGEX_CHECKBOX = "id=qaFilter_rule_isRE";
    public static final String QA_FILTER_RULE_DESCRIPTION_TEXT = "id=qaFilter_rule_desc";
    public static final String QA_FILTER_RULE_PRIORITY_TEXT = "id=qaFilter_rule_priority";
    public static final String QA_FILTER_RULE_SAVE_BUTTON = "xpath=(//input[@value='Save'])[19]";
    public static final String QA_FILTER_RULE_ADD_EXCEPTION_BUTTON = "//input[@value='Add Exception...']";
    public static final String QA_FILTER_RULE_EXCEPTION_EXCEPTION_TEXT = "id=qaFilter_rule_exception";
    public static final String QA_FILTER_RULE_EXCEPTION_EXCEPTION_IS_REGEX_CHECKBOX = "id=qaFilter_rule_exception_is_regex";
    public static final String QA_FILTER_RULE_EXCEPTION_EXCEPTION_LANGUAGE_SELECT = "id=qaFilter_rule_language";
    public static final String QA_FILTER_RULE_EXCEPTION_EXCEPTION_SAVE_BUTTON = "xpath=(//input[@value='Save'])[20]";
    public static final String QA_FILTER_RULE_CHECK_ALL_CHECKBOX = "id=checkAllRules";
    
    	
    
    // Xml Filter
    public static final String XML_FILTER_NAME_TEXT = "xmlRuleFilterName";
    public static final String XML_FILTER_CANCEL_BUTTON = "//input[@id='exit' and @value='Cancel' and @type='button' and @onclick=\"closePopupDialog('xmlRuleFilterDialog');xmlFilter.closeConfiguredTagDialog('xmlRuleFilter_configured_tag_Dialog');closePopupDialog('deleteXmlTagDialog')\"]";
    public static final String XML_FILTER_SAVE_BUTTON = "//input[@value='Save' and @type='button' and @onclick='saveXmlRuleFilter()']";
    
    public static final String XML_FILTER_XML_RULE_SELECT = "id=xmlRuleSelect";
    public static final String XML_FILTER_CONVERT_ENTITY_CHECKBOX = "//input[@id='isEnableConvertHtmlEntity']";
    public static final String XML_FILTER_IMPORT_EXPORT_ENTITIES_SELECT = "id=entityHandleModeSelect";
    public static final String XML_FILTER_EXTENDED_WHITESPACE_CHARACTERS_TEXT = "//input[@id='exSpaceChars']";
    public static final String XML_FILTER_PLACEHOLDER_CONSOLIDATION_SELECT = "id=phConsolidateMode";
    public static final String XML_FILTER_PLACEHOLDER_CONSOLIDATION_DO_NOT_CONSOLIDATE_LABEL = "label=Do not consolidate";
    public static final String XML_FILTER_PLACEHOLDER_CONSOLIDATION_CONSOLIDATE_ADJACENT_LABEL = "label=Consolidate adjacent";
    public static final String XML_FILTER_PLACEHOLDER_CONSOLIDATION_CONSOLIDATE_ADJACENT_IGNORE_WHITESPACE_LABEL = "label=Consolidate adjacent ignore whitespace";
    public static final String XML_FILTER_PLACEHOLDER_TRIMMING_SELECT = "id=phTrimMode";
    public static final String XML_FILTER_PLACEHOLDER_TRIMMING_DO_NOT_TRIM_LABEL = "label=Do not trim']";
    public static final String XML_FILTER_PLACEHOLDER_TRIMMING_TRIM_SELECT = "label=Trim";
    public static final String XML_FILTER_SAVE_NON_ASCII_CHARACTERS_AS_1_RADIO = "xpath=(//input[@name='nonasciiAs'])[1]";
    public static final String XML_FILTER_SAVE_NON_ASCII_CHARACTERS_AS_2_RADIO = "xpath=(//input[@name='nonasciiAs'])[2]";
    public static final String XML_FILTER_WHITESPACE_HANDLING_1_RADIO = "xpath=(//input[@name='wsHandleMode'])[1]";
    public static final String XML_FILTER_WHITESPACE_HANDLING_2_RADIO = "xpath=(//input[@name='wsHandleMode'])[2]";
    public static final String XML_FILTER_EMPTY_TAG_FORMAT_1_RADIO = "xpath=(//input[@name='emptyTagFormat'])[1]";
    public static final String XML_FILTER_EMPTY_TAG_FORMAT_2_RADIO = "xpath=(//input[@name='emptyTagFormat'])[2]";
    public static final String XML_FILTER_EMPTY_TAG_FORMAT_3_RADIO = "xpath=(//input[@name='emptyTagFormat'])[3]";
    public static final String XML_FILTER_ELEMENT_POST_FILTER_SELECT = "id=elementPostFilter";
    public static final String XML_FILTER_CDATA_POST_FILTER_SELECT = "id=cdataPostFilter";
    public static final String XML_FILTER_SID_SUPPORT_TAG_NAME_TEXT = "//input[@id='sidSupportTagNameEle']";
    public static final String XML_FILTER_SID_SUPPORT_ATT_NAME_TEXT = "//input[@id='sidSupportAttNameEle']";
    public static final String XML_FILTER_CHECK_WELL_FORMEDNESS_CHECKBOX = "//input[@id='isEnableCheckWellFormed']";
    public static final String XML_FILTER_GENERATE_LANGUAGE_INFORMATION_CHECKBOX = "//input[@id='isEnableGerateLangInfo']";
    public static final String XML_FILTER_Base_TEXT_POST_FILTER_SELECT =  "id=xml_rule_filter_baseFilterSelect";
    
    public static final String XML_FILTER_TAG_TYPE_SELECT = "id=xmlFilterRulesSection"; 
    public static final String XML_FILTER_TAG_EMBEDDABLE_LABEL = "label=Embeddable Tags";
    public static final String XML_FILTER_TAG_CONTENT_INCLUSION_LABEL = "label=Content Inclusion Tags";
    public static final String XML_FILTER_TAG_TRANSLATABLE_ATTRIBUTE_LABEL = "label=Translatable Attribute Tags";
    public static final String XML_FILTER_TAG_PRESERVE_WHITESPACE_LABEL = "label=Preserve Whitespace Tags";
    public static final String XML_FILTER_TAG_CDATA_POST_FILTER_LABEL = "label=CDATA post-filter tags";
    public static final String XML_FILTER_TAG_ENTITIES_LABEL = "label=Entities";
    public static final String XML_FILTER_TAG_PROCESSING_INSTRUCTIONS_LABEL = "label=Processing Instructions";
    public static final String XML_FILTER_TAG_INTERNAL_Tag_LABEL = "label=Internal Tag";
    public static final String XML_FILTER_TAG_SOURCE_COMMENT_FROM_XML_COMMENT_LABEL = "label=Source Comment from XML Comment";
    public static final String XML_FILTER_TAG_SOURCE_COMMENT_FROM_XML_TAG_LABEL = "label=Source Comment from XML Tag";
    public static final String XML_FILTER_CHECK_ALL_CHECKBOX = "id=checkAllXmlFilter";
    public static final String XML_FILTER_TAG_ADD_BUTTON = "//input[@value='Add...']";
    public static final String XML_FILTER_TAG_NAME_TEXT = "id=xmlRuleConfiguredTag_tag_name";   
    public static final String XML_FILTER_TAG_COND_ATT_ITEM_TEXT = "id=xmlRuleConfiguredTag_cond_attributes_item";
    public static final String XML_FILTER_TAG_COND_ATT_RES_TEXT = "id=xmlRuleConfiguredTag_cond_attributes_res";    
    public static final String XML_FILTER_TAG_COND_ATT_OPERATION_SELECT = "id=xmlRuleConfiguredTag_cond_attributes_Operation";
    public static final String XML_FILTER_TAG_COND_ATT_OPERATION_MATCH_LABEL =  "label=match";
    public static final String XML_FILTER_TAG_COND_ATT_OPERATION_EQUAL_LABEL =  "label=equal";
    public static final String XML_FILTER_TAG_COND_ATT_OPERATION_NOT_EQUAL_LABEL =  "label=not equal";
    public static final String XML_FILTER_TAG_COND_ATT_ADD_BUTTON = "id=xmlRuleConfiguredTag_add_item";
    public static final String XML_FILTER_TAG_COND_SAVE_BUTTON = "xpath=(//input[@value='Save'])[11]";
    public static final String XML_FILTER_TAG_COND_ATT_NAME_TEXT = "id=xmlRuleConfiguredTag_trans_attribute";
    public static final String XML_FILTER_TAG_ATT_ADD_BUTTON = "id=xmlRuleConfiguredTag_add_TransAttr";
    public static final String XML_FILTER_TAG_COND_TYPE_RADIO_INCLUDE = "id=xmlRuleConfiguredTag_inclType_1";
    public static final String XML_FILTER_TAG_COND_TYPE_RADIO_EXCLUDE = "id=xmlRuleConfiguredTag_inclType_2";
    public static final String XML_FILTER_TAG_CONFIGURED_ENTITY_NAME_TEXT = "id=xmlRuleFilter_configuredentity_EntityName";
    public static final String XML_FILTER_TAG_CONFIGURED_ENTITY_TYPE_SELECT = "id=xmlRuleFilter_configuredentity_Type";
    public static final String XML_FILTER_TAG_CONFIGURED_ENTITY_TYPE_PLACEHOLDER_LABEL = "label=PlaceHolder";
    public static final String XML_FILTER_TAG_CONFIGURED_ENTITY_TYPE_TEXT_LABEL = "label=Text";
    public static final String XML_FILTER_TAG_CONFIGURED_ENTITY_CODE_TEXT = "id=xmlRuleFilter_configuredentity_EntityCode"; 
    public static final String XML_FILTER_TAG_CONFIGURED_ENTITY_SAVE_AS_ENTITY_RADIO = "id=xmlRuleFilter_configuredentity_SaveAs_0";
    public static final String XML_FILTER_TAG_CONFIGURED_ENTITY_SAVE_AS_CHARACTER_RADIO = "id=xmlRuleFilter_configuredentity_SaveAs_1";
    public static final String XML_FILTER_TAG_CONFIGURED_ENTITY_SAVE_BUTTON = "xpath=(//input[@value='Save'])[13]"; 
    public static final String XML_FILTER_TAG_PROCESSING_INSTRUCTION_NAME_TEXT = "id=xmlRuleFilter_pi_name";
    public static final String XML_FILTER_TAG_PROCESSING_INSTRUCTION_TYPE_SELECT = "id=xmlRuleFilter_pi_Type";
    public static final String XML_FILTER_TAG_PROCESSING_INSTRUCTION_TYPE_AS_MARKUP_LABEL = "label=As Markup";
    public static final String XML_FILTER_TAG_PROCESSING_INSTRUCTION_TYPE_AS_EMBEDDABLE_MARKUP_LABEL = "label=As Embeddable Markup";
    public static final String XML_FILTER_TAG_PROCESSING_INSTRUCTION_TYPE_REMOVE_FROM_TARGET_LABEL = "label=Remove from Target";
    public static final String XML_FILTER_TAG_PROCESSING_INSTRUCTION_TYPE_EXTRACT_FOR_TRANSLATION_LABEL = "label=Extract for translation";
    public static final String XML_FILTER_TAG_PROCESSING_INSTRUCTION_ATT_TEXT = "id=xmlRuleFilter_pi_trans_attribute";
    public static final String XML_FILTER_TAG_PROCESSING_INSTRUCTION_ATT_ADD_BUTTON = "id=xmlRuleFilter_pi_add_TransAttr";
    public static final String XML_FILTER_TAG_PROCESSING_INSTRUCTION_SAVE_BUTTON = "xpath=(//input[@value='Save'])[14]";
    public static final String XML_FILTER_TAG_CDATA_POST_FILTER_NAME_TEXT = "id=xmlFilterCdatapostFilterName";
    public static final String XML_FILTER_TAG_CDATA_POST_FILTER_COND_RES_TEXT = "id=xmlFilterCdatapostFilter_cond_res";
    public static final String XML_FILTER_TAG_CDATA_POST_FILTER_ADD_BUTTON = "id=xmlFilterCdatapostFilter_add_item";
    public static final String XML_FILTER_TAG_CDATA_POST_FILTER_FILTER_SELECT = "id=cdataPostFilter_filter";
    public static final String XML_FILTER_TAG_CDATA_POST_FILTER_DEFAULT_FILTER_LABEL = "label=HTML_Filter(Default)";
    public static final String XML_FILTER_TAG_CDATA_POST_FILTER_TRANSLATABLE_CHECKBOX = "id=xmlFilterCdatapostFilter_trans";
    public static final String XML_FILTER_TAG_CDATA_POST_FILTER_SAVE_BUTTON = "xpath=(//input[@value='Save'])[12]";
    public static final String XML_FILTER_TAG_SRC_CMT_FROM_XML_CMT_NAME = "id=xmlRuleFilter_srcCmtXmlComment_name";
    public static final String XML_FILTER_TAG_SRC_CMT_FROM_XML_CMT_IS_RE_CHECKBOX = "id=xmlRuleFilter_srcCmtXmlComment_isRE";
    public static final String XML_FILTER_TAG_SRC_CMT_FROM_XML_CMT_SAVE_BUTTON = "xpath=(//input[@value='Save'])[15]";
    public static final String XML_FILTER_TAG_SRC_CMT_FROM_XML_TAG_FROM_ATT_RADIO = "id=xmlRuleConfiguredTag_fromAttribute";
    public static final String XML_FILTER_TAG_SRC_CMT_FROM_XML_TAG_FROM_ATT_TEXT = "id=xmlRuleConfiguredTag_attributeName";
    public static final String XML_FILTER_TAG_SRC_CMT_FROM_XML_TAG_FROM_TAG_RADIO = "id=xmlRuleConfiguredTag_fromTagContent";
    
    


}
