package com.globalsight.selenium.pages;

public class FileProfile
{
    // page title
    public static final String TITLE = "File Profiles";
    
    // main page
    public static final String NEW_BUTTON = "//input[@value='New...']";
    public static final String EDIT_BUTTON = "editBtn";
    public static final String REMOVE_BUTTON = "//input[@value='Remove']";
    public static final String SAVE_BUTTON = "//input[@value='Save']";
    public static final String CANCEL_BUTTON = "//input[@value='Cancel']";
    
    public static final String SEARCH_CONTENT_TEXT = "//input[@id='uNameFilter']";
    public static final String SEARCH_BUTTON = "//input[@value='Search...']";

    // detail page
    public static final String NAME_TEXT = "fpName";
    public static final String DESCRIPTION_TEXT = "desc";
    public static final String LOCALIZATION_PROFILE_SELECT = "locProfileId";
    public static final String SOURCE_FILE_FORMAT_SELECT = "formatSelector";
    public static final String FILTER_SELECT = "filterInfo";
    public static final String SOURCE_FILE_ENCODING_SELECT = "codeSet";
    public static final String FILE_EXTENSION_TYPE_SELECT = "extension";
    public static final String DEFAULT_EXPORT_CHECKBOX = "exportFiles";
    public static final String SELECTED_FILE_PROFILE_RADIO = "//input[@type='radio' and @onclick='enableButtons()' and @name='radioBtn' and @id='radioBtn']";
    public static final String MAIN_TABLE = "//div[@id='contentLayer']/form/table/tbody/tr[2]/td/table/tbody";

    // elements in the detail page
    private long id = -1l;
    private String name = "";
    private String desc = "";
    private String localizationProfile = "";
    private String sourceFileFormat = "";
    private String filter = "";
    private String filterTableName = "";
    private String sourceFileEncoding = "";
    private String xlsFile = "";
    private String xmlDtd = "";
    private String utf8Bom = "";
    private String scriptOnImport = "";
    private String scriptOnExport = "";
    private String fileExtensionType = "0";
    private String fileExtensions = "";
    private String defaultExport = "1";
    private boolean terminologyApproval = false;
    private long knownFormatTypeId = -1;
    private long xmlRuleId = -1;
    private long xmlDtdId = -1;
    
    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getDesc()
    {
        return desc;
    }

    public void setDesc(String desc)
    {
        this.desc = desc;
    }

    public String getLocalizationProfile()
    {
        return localizationProfile;
    }

    public void setLocalizationProfile(String localizationProfile)
    {
        this.localizationProfile = localizationProfile;
    }

    public String getFilter()
    {
        return filter;
    }

    public void setFilter(String filter)
    {
        this.filter = filter;
    }

    public String getXlsFile()
    {
        return xlsFile;
    }

    public void setXlsFile(String xlsFile)
    {
        this.xlsFile = xlsFile;
    }

    public String getUtf8Bom()
    {
        return utf8Bom;
    }

    public void setUtf8Bom(String utf8Bom)
    {
        this.utf8Bom = utf8Bom;
    }

    public String getScriptOnImport()
    {
        return scriptOnImport;
    }

    public void setScriptOnImport(String scriptOnImport)
    {
        this.scriptOnImport = scriptOnImport;
    }

    public String getScriptOnExport()
    {
        return scriptOnExport;
    }

    public void setScriptOnExport(String scriptOnExport)
    {
        this.scriptOnExport = scriptOnExport;
    }

    public String getFileExtensions()
    {
        return fileExtensions;
    }

    public void setFileExtensions(String fileExtensions)
    {
        this.fileExtensions = fileExtensions;
    }

    public String getDefaultExport()
    {
        return defaultExport;
    }

    public void setDefaultExport(String defaultExport)
    {
        this.defaultExport = defaultExport;
    }

    public boolean isTerminologyApproval()
    {
        return terminologyApproval;
    }

    public void setTerminologyApproval(boolean terminologyApproval)
    {
        this.terminologyApproval = terminologyApproval;
    }

    public String getFileExtensionType()
    {
        return fileExtensionType;
    }

    public void setFileExtensionType(String fileExtensionType)
    {
        this.fileExtensionType = fileExtensionType;
    }

    public String getSourceFileFormat()
    {
        return sourceFileFormat;
    }

    public void setSourceFileFormat(String sourceFileFormat)
    {
        this.sourceFileFormat = sourceFileFormat;
    }

    public String getSourceFileEncoding()
    {
        return sourceFileEncoding;
    }

    public void setSourceFileEncoding(String sourceFileEncoding)
    {
        this.sourceFileEncoding = sourceFileEncoding;
    }

    public String getXmlDtd()
    {
        return xmlDtd;
    }

    public void setXmlDtd(String xmlDtd)
    {
        this.xmlDtd = xmlDtd;
    }

    public String getFilterTableName()
    {
        return filterTableName;
    }

    public void setFilterTableName(String filterTableName)
    {
        this.filterTableName = filterTableName;
    }

    public long getKnownFormatTypeId()
    {
        return knownFormatTypeId;
    }

    public void setKnownFormatTypeId(long knownFormatTypeId)
    {
        this.knownFormatTypeId = knownFormatTypeId;
    }

    public long getXmlRuleId()
    {
        return xmlRuleId;
    }

    public void setXmlRuleId(long xmlRuleId)
    {
        this.xmlRuleId = xmlRuleId;
    }

    public long getXmlDtdId()
    {
        return xmlDtdId;
    }

    public void setXmlDtdId(long xmlDtdId)
    {
        this.xmlDtdId = xmlDtdId;
    }

    public long getId()
    {
        return id;
    }

    public void setId(long id)
    {
        this.id = id;
    }
}
