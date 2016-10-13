package com.globalsight.everest.edit.offline.ttx;

public interface TTXConstants 
{
	String FRONTMATTER = "FrontMatter";
	//
	String TOOLSETTINGS = "ToolSettings";
	String TOOLSETTINGS_ATT_CREATIONDATE = "CreationDate";//REQUIRED
	String TOOLSETTINGS_ATT_CREATIONTOOL = "CreationTool";//REQUIRED
	String TOOLSETTINGS_ATT_CREATIONTOOLVERSION = "CreationToolVersion";//REQUIRED
	
	String TOOLSETTINGS_ATT_CUSTOM = "Custom";//IMPLIED
	
	//
	String USERSETTINGS = "UserSettings";
    String USERSETTINGS_SOURCE_lANGUAGE = "SourceLanguage";//REQUIRED
    String USERSETTINGS_O_ENCODING = "O-Encoding";//REQUIRED
    String USERSETTINGS_DATA_TYPE = "DataType";//REQUIRED
    
    String USERSETTINGS_TARGET_LANGUAGE = "TargetLanguage";//IMPLIED    
    String USERSETTINGS_SOURCE_DOCUMENT_PATH = "SourceDocumentPath";//IMPLIED
    String USERSETTINGS_USERID = "UserId";//IMPLIED
    String USERSETTINGS_DATA_TYPE_VERSION = "DataTypeVersion";//IMPLIED
    String USERSETTINGS_SETTINGS_PATH = "SettingsPath";//IMPLIED
    String USERSETTINGS_SETTINGS_NAME = "SettingsName";//IMPLIED
    String USERSETTINGS_TARGET_DEFAULT_FONT = "TargetDefaultFont";//IMPLIED
    String USERSETTINGS_CUSTOM = "Custom";//IMPLIED
    
	String BODY = "Body";
	String RAW = "Raw";

	String UT = "ut";
	String UT_ATT_DISPLAYTEXT = "DisplayText";//CDATA #IMPLIED
	
	String UT_ATT_TYPE = "Type";//#IMPLIED
	String UT_ATT_TYPE_START = "start";
	String UT_ATT_TYPE_END = "end";
	String UT_ATT_TYPE_STANDALONE = "standalone";
	String UT_ATT_TYPE_UNDEFINED = "undefined";
	
	String UT_ATT_CLASS = "Class";//#IMPLIED
	String UT_ATT_CLASS_NORMAL = "normal";
	String UT_ATT_CLASS_COMMENT = "comment";
	String UT_ATT_CLASS_PROCINSTR = "procinstr";
	String UT_ATT_CLASS_ENTITYREFERENCE = "entityreference";
	String UT_ATT_CLASS_PLACEHOLDER = "placeholder";
	String UT_ATT_CLASS_SERVERSCRIPT = "serverscript";
	String UT_ATT_CLASS_UNDEFINED = "undefined";

	String UT_ATT_LEFTEDGE = "LeftEdge";//#IMPLIED
	String UT_ATT_LEFTEDGE_ROUND = "round";
	String UT_ATT_LEFTEDGE_ANGLE = "angle";
	String UT_ATT_LEFTEDGE_SPLIT = "split";
	String UT_ATT_LEFTEDGE_UNDEFINED = "undefined";

	String UT_ATT_RIGHTEDGE = "RightEdge";//#IMPLIED
	String UT_ATT_RIGHTEDGE_ROUND = "round";
	String UT_ATT_RIGHTEDGE_ANGLE = "angle";
	String UT_ATT_RIGHTEDGE_SPLIT = "split";
	String UT_ATT_RIGHTEDGE_UNDEFINED = "undefined";
	
	String UT_ATT_STYLE = "Style";//#IMPLIED
	String UT_ATT_STYLE_INTERNAL = "internal";
	String UT_ATT_STYLE_EXTERNAL = "external";
	String UT_ATT_STYLE_NONXLATABLE = "nonxlatable";
	String UT_ATT_STYLE_UNDEFINED = "undefined";
	
	String DF = "df";
	
	String TU = "Tu";
	
	String TUV = "Tuv";
	String TUV_ATT_LANG = "Lang";
	
	String NEW_LINE = "\r\n";
	char NORMALIZED_LINEBREAK = '\n';
	String ONE_SPACE = " ";
	String HASH_MARK = "# ";
	
	String TU_ID = "TuId";
	String GS = "GS";
	String IN_TU = "in_tu";
	String IN_TUV = "in_tuv";
	String IN_SOURCE_TUV = "in_source_tuv";
	String IN_TARGET_TUV = "in_target_tuv";
	
	String GS_ENCODING = "gs:Encoding";
	String GS_DOCUMENT_FORMAT = "gs:DocumentFormat";
	String GS_PLACEHOLDER_FORMAT = "gs:PlaceholderFormat";
	String GS_SOURCE_LOCALE = "gs:SourceLocale";
	String GS_TARGET_LOCALE = "gs:TargetLocale";
	String GS_PAGEID = "gs:PageID";
	String GS_WORKFLOW_ID = "gs:WorkflowID";
	String GS_TASK_ID = "gs:TaskID";
	String GS_EXACT_MATCH_WORD_COUNT = "gs:ExactMatchWordCount";
	String GS_FUZZY_MATCH_WORD_COUNT = "gs:FuzzyMatchWordCount";
	String GS_EDIT_ALL = "gs:EditAll";
	String GS_LOCKED_SEGMENT = "GS:Locked Segment";
	String GS_POPULATE_100_TARGET_SEGMENTS = "GS:Populate100TargetSegments";
	
	//Values of "Origin" attribute of "Tu" element from "Trados-Tag 2.0.dtd".
	String TTX_TU_ORIGIN_MANUAL = "manual";
	String TTX_TU_ORIGIN_BATCH100 = "batch100";
	String TTX_TU_ORIGIN_BATCHFUZZY = "batchfuzzy";
	String TTX_TU_ORIGIN_XTRANSLATE = "xtranslate";
	String TTX_TU_ORIGIN_MT = "mt";
	String TTX_TU_ORIGIN_UNTRANSLATED = "untranslated";
	String TTX_TU_ORIGIN_UNDEFINED = "undefined";
	
	String GS_INI_FILE = "GS.ini";
	
	String GS_DOUBLE_LEFT_BRACKETS = "\uE000" + "_DOUBLE-BRACKETS_" + "\uE000";
	String GS_DOUBLE_RIGHT_BRACKETS = "\uE000" + "_DOUBLE-RIGHT-BRACKETS_" + "\uE000";
}
