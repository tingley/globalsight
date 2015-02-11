package com.globalsight.selenium.pages;

public class FilterConfiguration
{

    // Filter Configuration
    public static final String CheckAll_CHECKBOX = "//input[@type='checkbox']";
    public static final String ExpnadAll_CHECKBOX = "expandAllFilters";
    public static final String CollapseAll_CHECKBOX = "collapseAllFilters";
    public static final String Remove_BUTTON="//input[@value='Remove']";
    
    public static final String FiltersConfiguration_TABLE="//span[@id='filterConfigurationTable']/table/tbody";
  
    public static final String HtmlFilter_IMG = "//span[@id='filterConfigurationTable']/table/tbody/tr/td/img";  
    public static final String InDesignIDMLFilter_IMG = "//span[@id='filterConfigurationTable']/table/tbody/tr[4]/td/img";
    public static final String InternalTextFilter_IMG = "//span[@id='filterConfigurationTable']/table/tbody/tr[7]/td/img";
    public static final String JavaPropertiesFilter_IMG = "//span[@id='filterConfigurationTable']/table/tbody/tr[10]/td/img";
    public static final String JavaScriptFilter_IMG = "//span[@id='filterConfigurationTable']/table/tbody/tr[13]/td/img";
    public static final String JspFilter_IMG = "//span[@id='filterConfigurationTable']/table/tbody/tr[16]/td/img";
    public static final String MS2010Filter_IMG = "//span[@id='filterConfigurationTable']/table/tbody/tr[19]/td/img";
    public static final String MSDocFilter_IMG = "//span[@id='filterConfigurationTable']/table/tbody/tr[22]/td/img";
    public static final String MSExcelFilter_IMG = "//span[@id='filterConfigurationTable']/table/tbody/tr[25]/td/img";
    public static final String MSPowerPointFilter_IMG = "//span[@id='filterConfigurationTable']/table/tbody/tr[28]/td/img";
    public static final String OpenOfficeFilter_IMG = "//span[@id='filterConfigurationTable']/table/tbody/tr[31]/td/img";
    public static final String PortableObjectFilter_IMG = "//span[@id='filterConfigurationTable']/table/tbody/tr[34]/td/img";
    public static final String XmlFilter_IMG = "//span[@id='filterConfigurationTable']/table/tbody/tr[37]/td/img";

    public static final String HtmlFilter_BUTTON = "//span[@id='filterConfigurationTable']/table/tbody/tr/td/input";
    public static final String InDesignIDMLFilter_BUTTON = "//span[@id='filterConfigurationTable']/table/tbody/tr[4]/td/input";
    public static final String InternalTextFilter_BUTTON = "//span[@id='filterConfigurationTable']/table/tbody/tr[7]/td/input";
    public static final String JavaPropertiesFilter_BUTTON = "//span[@id='filterConfigurationTable']/table/tbody/tr[10]/td/input";
    public static final String JavaScriptFilter_BUTTON = "//span[@id='filterConfigurationTable']/table/tbody/tr[13]/td/input";
    public static final String JspFilter_BUTTON = "//span[@id='filterConfigurationTable']/table/tbody/tr[16]/td/input";
    public static final String MS2010Filter_BUTTON = "//span[@id='filterConfigurationTable']/table/tbody/tr[19]/td/input";
    public static final String MSDocFilter_BUTTON = "//span[@id='filterConfigurationTable']/table/tbody/tr[22]/td/input";
    public static final String MSExcelFilter_BUTTON = "//span[@id='filterConfigurationTable']/table/tbody/tr[25]/td/input";
    public static final String MSPowerPointFilter_BUTTON = "//span[@id='filterConfigurationTable']/table/tbody/tr[28]/td/input";
    public static final String OpenOfficeFilter_BUTTON = "//span[@id='filterConfigurationTable']/table/tbody/tr[31]/td/input";
    public static final String PortableObjectFilter_BUTTON = "//span[@id='filterConfigurationTable']/table/tbody/tr[34]/td/input";
    public static final String XmlFilter_BUTTON = "//span[@id='filterConfigurationTable']/table/tbody/tr[37]/td/input";

    // Html Filter
    public static final String FilterName_HtmlFilter_TEXT_FIELD = "htmlFilterName";
    public static final String Cancel_HtmlFilter_BUTTON = "//input[@id='exit' and @value='Cancel' and @type='button' and @onclick=\"closePopupDialog('htmlFilterDialog')\"]";
    public static final String Save_HtmlFilter_BUTTON = "//input[@value='Save' and @type='button' and @onclick='saveHtmlFilter()']";
    public static final String Html_CheckAll = "checkAll";
    public static final String Html_Tags_Delete_BUTTON = "//input[@value='Delete']";
    public static final String Html_Delete_Save_BUTTON = "//input[@value='Save' and @type='button' and @onclick='htmlFilter.deleteTags()']";
    public static final String Basefont_CHECKBOX = "tags_4";  
    public static final String a_CHECKBOX = "tags_0";
    public static final String tag1_CHECKBOX = "tags_1";
    public static final String InternalText_Post_Filter = "html_filter_baseFilterSelect";
    public static final String Html_Choosing_Box = "htmlTranslateRule";
    public static final String Html_Add_Tag_BUTTON = "//input[@value='Add' and @type='button' and @onclick='htmlFilter.addTag()']";
    public static final String Html_Add_Tag_Field = "singleTagNameToAdd";
    public static final String Html_Add_InternalTag_Field = "InternalTagToAdd";
    public static final String Html_Add_InternalTag_Save_BUTTON = "//input[@value='Save' and @type='button' and @onclick='htmlFilter.addInternalTag()']";
    public static final String Html_Add_Tag_Save_BUTTON = "//input[@value='Save' and @type='button' and @onclick='htmlFilter.addSingleTag()']";
    
    // InDesignIDML Filter
    public static final String FilterName_InDesignIDMLFilter_TEXT_FIELD = "inddFilterName";
    public static final String ignoreLineBreak_CHECKBOX = "//*[@id='ignoreLineBreak']";
    public static final String Cancel_InDesignIDMLFilter_BUTTON = "//input[@id='exit' and @value='Cancel' and @type='button' and @onclick=\"closePopupDialog('inddFilterDialog')\"]";
    public static final String Save_InDesignIDMLFilter_BUTTON = "//input[@value='Save' and @type='button' and @onclick='saveInddFilter()']";
    
    //InternalText Filter
    public static final String FilterName_Internaltext_TEXT_FIELD = "baseFilterName";
    public static final String Save_Internaltext_BUTTON = "//input[@value='Save' and @type='button' and @onclick='saveBaseFilter()']";
    public static final String Internaltext_Content_Save_BUTTON = "//input[@value='Save' and @type='button' and @onclick='baseFilter.saveInternalText()']";
    public static final String Internaltext_Type_Content = "baseFilter_InternalText";
    public static final String Internaltext_ADD_BUTTON = "//input[@value='Add...']";
    public static final String Internaltext_IS_RE = "baseFilter_InternalText_isRE";
    public static final String Internaltext_CheckAll = "checkAllBaseFilter";
    
    // java Properties Filter
    public static final String FilterName_JavaPropertiesFilter_TEXT_FIELD = "javaPropertiesFilterName";
    public static final String Cancel_JavaPropertiesFilter_BUTTON = "//input[@id='exit' and @value='Cancel' and @type='button' and @onclick=\"closePopupDialog('javaPropertiesFilterDialog')\"]";
    public static final String Save_JavaPropertiesFilter_BUTTON = "//input[@value='Save' and @type='button' and @onclick='saveJavaProperties()']";
    public static final String enableSIDSupport_CHECKBOX = "//input[@type='checkbox' and @name='supportSid' and @id='isSupportSid']"; 
    public static final String enableUnicodeEscap_CHECKBOX = "//input[@type='checkbox' and @name='unicodeEscape' and @id='isSupportSid']";
    public static final String preserveTrailingSpaces_CHECKBOX = "//input[@type='checkbox' and @name='preserveSpaces' and @id='isPreserveSpaces']";
    
    // java Script Filter
    public static final String FilterName_JavaScriptFilter_TEXT_FIELD = "javaScriptFilterName";
    public static final String Cancel_JavaScriptFilter_BUTTON = "//input[@id='exit' and @value='Cancel' and @type='button' and @onclick=\"closePopupDialog('javaScriptFilterDialog')\"]";
    public static final String Save_JavaScriptFilter_BUTTON = "//input[@value='Save' and @type='button' and @onclick='saveJavaScript()']";
    public static final String JSFunction_TEXT_FIELD = "javaScriptJsFunctionText";
    public static final String enableUnicodeEscape_CHECKBOX ="//INPUT[@id='enableUnicodeEscape' and @type='checkbox' and @name='enableUnicodeEscape']";

    // Jsp Filter
    public static final String FilterName_JspFilter_TEXT_FIELD = "jspFilterName";
    public static final String Cancel_JspFilter_BUTTON = "//input[@id='exit' and @value='Cancel' and @type='button' and @onclick=\"closePopupDialog('jspFilterDialog')\"]";
    public static final String Save_JspFilter_BUTTON = "//input[@value='Save' and @type='button' and @onclick='saveJSPFilter()']";
    public static final String addAdditionalHead_CHECKBOX = "//input[@type='checkbox' and @name='addAdditionalHead' and @id='addAdditionalHead']";

    // MS Office 2010 Filter
    public static final String FilterName_MS2010Filter_TEXT_FIELD = "o2010FilterName";
    public static final String Cancel_MS2010Filter_BUTTON = "//input[@id='exit' and @value='Cancel' and @type='button' and @onclick=\"closePopupDialog('msoffice2010FilterDialog')\"]";
    public static final String Save_MS2010Filter_BUTTON = "//input[@value='Save' and @type='button' and @onclick='saveMSOffice2010DocFilter()']";
    public static final String headerTranslate_CHECKBOX = "//input[@type='checkbox' and @name='headerTranslate' and @id='headerTranslate']";
    public static final String masterTranslate_CHECKBOX = "//input[@type='checkbox' and @name='masterTranslate' and @id='masterTranslate']";
    public static final String unextractableWordStyle_SELECT ="//select[@onchange='msoffice2010DocFilter.switchTags(this)' and @id='O2010UnextractableRule']";
    public static final String add_MS2010Filter_BUTTON = "//input[@type='button' and @onclick='msoffice2010DocFilter.onAdd()' and @value='Add']";
    public static final String delete_MS2010Filter_BUTTON = "//input[@type='button' and @onclick='msoffice2010DocFilter.deleteTag()' and @id='O2010DeleteButton' and @value='Delete']";
    public static final String DONOTTRANSLATE_para_CHECKBOX = "//input[@type='checkbox' and @id='styles_0' and @name='DONOTTRANSLATE_para' and @onclick='msoffice2010DocFilter.checkthis(this)']";

    //Add Style window for office 2010 filter
    public static final String addStyle_TEXT_FIELD = "o2010styleToAdd";
    public static final String Save_AddStyle2010_BUTTON = "//input[@type='button' and @onclick='msoffice2010DocFilter.addStyle()' and @value='Save']";
    public static final String Cancel_AddStyle2010_BUTTON = "//input[@type='button' and @value='Cancel' and @id='exit']";
    	
    // MS Office Doc Filter
    public static final String FilterName_MSDocFilter_TEXT_FIELD = "docFilterName";
    public static final String Cancel_MSDocFilter_BUTTON = "//input[@id='exit' and @value='Cancel' and @type='button' and @onclick=\"closePopupDialog('msOfficeDocFilterDialog')\"]";
    public static final String Save_MSDocFilter_BUTTON = "//input[@value='Save' and @type='button' and @onclick='saveMsOfficeDocFilter()']";
    public static final String SecondaryFilter_MSDocFilter_BUTTON = "//select[@id='secondaryFilterSelect']";
    public static final String HeaderInformation_MSDocFilter_CHECKBOX = "//input[@type='checkbox' and @name='headerTranslate' and @id='headerTranslate']";
    public static final String unextractableDocStyle_SELECT = "//select[@onchange='officeDocFilter.switchTags(this)' and @id='DocUnextractableRule']";
    public static final String Style_Choose = "DocUnextractableRule";
    public static final String Style_Add = "styleToAdd";
    public static final String Style_Add_BUTTON = "//input[@value='Add' and @type='button' and @onclick='officeDocFilter.onAdd()']";
    public static final String Style_Save_BUTTON = "//input[@value='Save' and @type='button' and @onclick='officeDocFilter.addStyle()']";
    public static final String MS_Doc_CheckAll = "MsCheckAll";
    public static final String MS_Delete_BUTTON = "MSDeleteButton";
    public static final String MS_Delete_Save_BUTTOn = "//input[@value='Save' and @type='button' and @onclick='officeDocFilter.deleteStyles()']";

    // MS Office Excel Filter
    public static final String FilterName_MSExcelFilter_TEXT_FIELD = "excelFilterName";
    public static final String Cancel_MSExcelFilter_BUTTON = "//input[@id='exit' and @value='Cancel' and @type='button' and @onclick=\"closePopupDialog('msOfficeExcelFilterDialog')\"]";
    public static final String Save_MSExcelFilter_BUTTON = "//input[@value='Save' and @type='button' and @onclick='saveMsOfficeExcelFilter()']";
    public static final String Content_PostFilter_Choose = "excelContentPostFilterSelect";
    public static final String InternalText_PostFilter_Choose = "ms_office_excel_filter_baseFilterSelect";
    
    // MS Office PowerPoint Filter
    public static final String FilterName_MSPowerPointFilter_TEXT_FIELD = "pptFilterName";
    public static final String Cancel_MSPowerPointFilter_BUTTON = "//input[@id='exit' and @value='Cancel' and @type='button' and @onclick=\"closePopupDialog('msOfficePPTFilterDialog')\"]";
    public static final String Save_MSPowerPointFilter_BUTTON = "//input[@value='Save' and @type='button' and @onclick='saveMSPPTFilter()']";
    public static final String AltPPT_CHECKBOX = "//input[@type='checkbox' and @name='extractAlt' and @id='ExtractAlt']";

    // OpenOffice Filter
    public static final String FilterName_OpenOfficeFilter_TEXT_FIELD = "ooFilterName";
    public static final String Cancel_OpenOfficeFilter_BUTTON = "//input[@id='exit' and @value='Cancel' and @type='button' and @onclick=\"closePopupDialog('openofficeFilterDialog')\"]";
    public static final String Save_OpenOfficeFilter_BUTTON = "//input[@value='Save' and @type='button' and @onclick='saveOpenOfficeDocFilter()']";
    public static final String headerInformationOpenOffice_CHECKBOX = "//input[@type='checkbox' and @name='headerTranslate' and @id='headerTranslate']";

    // Portable Object Filter
    public static final String FilterName_PortableObjectFilter_TEXT_FIELD = "poFilterName";
    public static final String Cancel_PortableObjectFilter_BUTTON = "//input[@id='exit' and @value='Cancel' and @type='button' and @onclick=\"closePopupDialog('poFilterDialog')\"]";
    public static final String Save_PortableObjectFilter_BUTTON = "//input[@value='Save' and @type='button' and @onclick='savePOFilter()']";
    public static final String SecondaryFilterPO_SELECT = "secondaryFilterSelect";
   
    // Xml Filter
    public static final String FilterName_XmlFilter_TEXT_FIELD = "xmlRuleFilterName";
    public static final String Cancel_XmlFilter_BUTTON = "//input[@id='exit' and @value='Cancel' and @type='button' and @onclick=\"closePopupDialog('xmlRuleFilterDialog');xmlFilter.closeConfiguredTagDialog('xmlRuleFilter_configured_tag_Dialog');closePopupDialog('deleteXmlTagDialog')\"]";
    public static final String Save_XmlFilter_BUTTON = "//input[@value='Save' and @type='button' and @onclick='saveXmlRuleFilter()']";
}
