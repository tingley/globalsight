/**
 *  Copyright 2009 Welocalize, Inc. 
 *  
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  
 *  You may obtain a copy of the License at 
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  
 */
package com.globalsight.everest.edit.offline;

public interface AmbassadorDwUpConstants
{
    // OfflineEditManager configuration keys (properties).
    static public final String OFFLINE_CONFIG_PROPERTY = "properties/OfflineEditorConfig";
    static public final String OFFLINE_CONFIG_KEY_MAX_FUZZY = "MaxNumOfFuzzyMatches";
    static public final String OFFLINE_CONFIG_KEY_LB_NORMALIZATION = "UploadLineBreakNormalization";
    static public final String OFFLINE_CONFIG_KEY_ATN_THRESHOLD = "AnnotationThreshold";

    // File names/extensions
    static public final String FILE_EXT_XLIFF_NO_DOT = "xlf";
    static public final String FILE_EXT_TTX_NO_DOT = "ttx";
    static public final String FILE_EXT_RTF_NO_DOT = "rtf";
    static public final String FILE_EXT_FMT1 = ".f1";
    static public final String FILE_EXT_FMT2 = ".txt";
    static public final String FILE_EXT_FMT2_UPLOAD = ".f2";
    static public final String FILE_EXT_FMT2RTF = ".rtf";
    static public final String FILE_EXT_ZIP = ".zip";
    static public final String FILE_EXT_HTML = ".html";
    static public final String FILE_EXT_TXT = "txt"; // no dot
    static public final String FILE_EXT_BIN = ".bin";
    static public final String FILE_XML = "xml";
    static public final String FILE_EXT_TMX_NO_DOT = "tmx";
    static public final String FILE_PENALTY_TMX = "PENALTY";

    static public final String FILE_NAME_BREAK = "_";

    /** Relative paths to help file (paths are escaped for rtf). */
    static public final String FILE_MAIN_HELP = "http://www.globalsight.com/wiki/index.php/Main_Page";

    // Grammar For Plain text format javacc parser. Note: the grammar
    // cannot accept translated values.
    static public final String TAG_TYPE_PTAGV = "PTAG-VERBOSE";
    static public final String TAG_TYPE_PTAGC = "PTAG-COMPACT";
    static public final String TAG_TYPE_GXML = "GXML";
    static public final String FORMAT1_ENCODING = "UTF8";

    static public final String LABEL_CURRENT_FMT = "Placeholder Format:";
    static public final String LABEL_SRCLOCALE = "Source Locale";
    static public final String LABEL_TRGLOCALE = "Target Locale";

    // Grammar for Plain text format javacc parser. Note: the grammar
    // cannot accept translated values.
    static public final String SIGNATURE = "# GlobalSight Download File";
    static public final String END_SIGNATURE = "# END GlobalSight Download File";
    static public final String HEADER_ENCODING_KEY = "# Encoding:";
    static public final String HEADER_ORIGFMT_KEY = "# Document Format:";
    static public final String HEADER_CURFMT_KEY = "# Placeholder Format:";
    static public final String HEADER_SRCLOCALE_KEY = "# " + LABEL_SRCLOCALE
            + ":";
    static public final String HEADER_TRGLOCALE_KEY = "# " + LABEL_TRGLOCALE
            + ":";
    static public final String HEADER_EXACT_COUNT_KEY = "# Exact Match word count:";
    static public final String HEADER_FUZZY_COUNT_KEY = "# Fuzzy Match word count:";
    static public final String HEADER_NOMATCH_COUNT_KEY = "# No Match word count:";
    static public final String HEADER_EDITALL_KEY = "# Edit all:";
    static public final String HEADER_HELP_KEY = "# Help:";
    static public final String HEADER_NOTES_KEY = "# Notes:";
    static public final String HEADER_JOB_NAME = "# Job Name:";
    static public final String HEADER_JOB_ID = "# Job ID:";
    static public final String HEADER_POPULATE_100_SEGMENTS = "# Populate 100% Target Segments:";

    static public final String SEGMENT_ID_KEY = "# ";
    static public final String SEGMENT_PAGE_NAME_KEY = "# Page Name:";
    static public final String SEGMENT_FILE_PATH_KEY = "# File Path:";
    static public final String SEGMENT_SOURCE_KEY = "# Src:";
    static public final String SEGMENT_TARGET_KEY = "# Trg:";
    static public final String SEGMENT_MATCH_TYPE_KEY = "# Match Type:";
    static public final String SEGMENT_NOT_COUNT_KEY = "# Note: The words surrounded with tags {0} are configured not to translate.";
    static public final String SEGMENT_MATCH_VAL_KEY = "# Match Score:";
    static public final String SEGMENT_RESOURCE_KEY = "# Resources:";
    static public final String SEGMENT_SID_KEY = "# SID:";
    static public final String SEGMENT_XLF_TARGET_STATE_KEY = "# Xliff Target State:";
    static public final String SEGMENT_INCONTEXT_MATCH_KEY = "# In-Context Match word count";
    static public final String SEGMENT_TM_PROFILE_KEY = "# GlobalSight TM Profile";
    static public final String SEGMENT_TERMBASE_KEY = "# GlobalSight Termbase";

    // Grammar for Plain text format javacc parser. Note: the grammar
    // cannot accept translated values.
    static public final String HEADER_EDITALL_VALUE_YES = "Yes";
    static public final String HEADER_EDITALL_VALUE_NO = "No";
    static public final String HEADER_EDITALL_VALUE_UNAUTHORIZED = "UNAUTHORIZED";

    static public final String HEADER_TM_EDIT_TYPE_NONE = "None";
    static public final String HEADER_TM_EDIT_TYPE_BOTH = "Incontext Match and Exact Match";
    static public final String HEADER_TM_EDIT_TYPE_ICE = "Incontext Match";
    static public final String HEADER_TM_EDIT_TYPE_100 = "Exact Match";
    static public final String HEADER_TM_EDIT_TYPE_DENY = "Deny Edit";

    static public final String LINK_NAME_MAIN_HELP = "For more information, see the Offline Help, Placeholder Map and GlobalSight Wiki.";
    static public final String LINK_NAME_UPLOAD_HELP = "Click here to learn how to save files for upload.";

    static public final String HEADER_CMT_TEMPLATE1 = "{ ";
    static public final String HEADER_CMT_TEMPLATE2 = "{\\b\\ul To create an Ambassador comment:}";
    static public final String HEADER_CMT_TEMPLATE3 = "\\tab Put your cursor in a segment (anywhere in a segment).";
    static public final String HEADER_CMT_TEMPLATE4 = "\\tab Insert a {\\ul\\i new} comment (see Word help on Comments if necessary).";
    static public final String HEADER_CMT_TEMPLATE5 = "\\tab Copy and paste the following text into the {\\ul\\i new} comment:";
    static public final String HEADER_CMT_TEMPLATE6 = " ";
    static public final String HEADER_CMT_TEMPLATE7 = "\\tab {\\b\\{MyTitle=}title{\\b\\}\\{Status=}open{\\b\\}\\{Priority=}medium{\\b\\}\\{Category=}type01{\\b\\}\\{MyReply=}reply{\\b\\}}";
    static public final String HEADER_CMT_TEMPLATE8 = " ";
    static public final String HEADER_CMT_TEMPLATE9 = "\\tab Next, insert your comment information after the equals sign (=my comment).";
    static public final String HEADER_CMT_TEMPLATE10 = "\\tab For example:";
    static public final String HEADER_CMT_TEMPLATE11 = "\\tab\\tab {\\b\\{MyTitle=}Wrong verb tense{\\b\\} \\{Status=}open{\\b\\} \\{Priority=}medium{\\b\\}} \\{Category=}type01{\\b\\}";
    static public final String HEADER_CMT_TEMPLATE12 = "\\tab\\tab {\\b\\{MyReply=}Verb must agree with subject{\\b\\}}";
    static public final String HEADER_CMT_TEMPLATE13 = " ";
    static public final String HEADER_CMT_TEMPLATE14 = "\\tab Values for {\\b\\{Status=\\}} can be {\\b open}, {\\b closed} or {\\b query}.";
    static public final String HEADER_CMT_TEMPLATE15 = "\\tab            {\\b\\{Priority=\\}} can be {\\b low}, {\\b medium}, {\\b high}, or {\\b urgent}.";
    static public final String HEADER_CMT_TEMPLATE16 = "\\tab Upload errors may result from not following these directions.";
    static public final String HEADER_CMT_TEMPLATE17 = "\\tab Click on the Offline Help link above for more information.";
    static public final String HEADER_CMT_TEMPLATE18 = "} ";
    static public final String LINK_NAME_NOTES = "Do not modify any lines that start with #.";

    /** Standard internal delimiter - see also word client delimiters below. */
    static public final char SEGMENT_ID_DELIMITER = ':';

    // Grammar for Plain text format javacc parser. Note: the grammar
    // cannot accept translated values.
    static public final String LABEL_PAGENAME = "Page Name";
    static public final String LABEL_PAGEID = "Page ID";
    static public final String LABEL_JOBID = "Workflow ID";
    static public final String LABEL_STAGEID = "Task ID";
    static public final String LABEL_SEGMENT_FORMAT = "Segment Format";
    static public final String LABEL_MAPPING_TABLE = "Mapping Table";

    // Could be translated - as these are not part of the offline grammar.
    // However, grammar is not translated so we leave them for consistancy.
    static public final String LABEL_ANNOTATION_AUTHOR = "GlobalSight";
    static public final String LABEL_ANNOTATION_ID_SOURCE = "Source";
    static public final String LABEL_ANNOTATION_ID_TERM = "Terms";
    static public final String LABEL_ANNOTATION_ID_DETAILS = "Details";
    static public final String LABEL_ANNOTATION_ID_TM = "TM";
    static public final String LABEL_LINK_SOURCE = "Source";
    static public final String LABEL_LINK_TERM = "Terms";
    static public final String LABEL_LINK_DETAILS = "Details";
    static public final String LABEL_LINK_TM = "TM";
    static public final String LABEL_LINK_MT = "MT";
    static public final String LINK_TIP_MAIN_HELP = "For more information, see the Offline Help, Placeholder Map and GlobalSight Wiki.";
    static public final String LINK_TIP_UPLOAD_HELP = "Click here to learn how to save files for upload";
    static public final String LINK_TIP_NOTES = "Do not modify any lines that start with a pound sign";
    static public final String LINK_TIP_RESPAGE = "Click here to view segment resources";
    static public final String LABEL_SERVER_INSTANCEID = "GlobalSight Instance ID";

    // Grammar for Plain text format javacc parser. Note: the grammar
    // cannot accept translated values.
    static public final String HEADER_PAGENAME_KEY = "# " + LABEL_PAGENAME
            + ":";
    static public final String HEADER_PAGEID_KEY = "# " + LABEL_PAGEID + ":";
    static public final String HEADER_JOBID_KEY = "# " + LABEL_JOBID + ":";
    static public final String HEADER_STAGEID_KEY = "# " + LABEL_STAGEID + ":";
    static public final String SEGMENT_FORMAT_KEY = "# " + LABEL_SEGMENT_FORMAT
            + ":";
    static public final String HEADER_SERVER_INSTANCEID_KEY = "# "
            + LABEL_SERVER_INSTANCEID + ":";

    // Could be translated - as these are not part of the offline grammar.
    // However, grammar is not translated so we leave them for consistancy.
    static public final String SUPPORTFILES_PACKAGE_SUFFIX = "SupportFiles";

    // PLATFORM IDs
    // add values between start and end
    static public final int PLATFORM_LIST_START = 1;
    static public final int PLATFORM_WIN32 = 2;
    static public final int PLATFORM_MAC = 3;
    static public final int PLATFORM_UNIX = 4;
    static public final int PLATFORM_LIST_END = 5;

    // EDITOR IDs ( a user selectable download option )
    // add values between start and end
    static public final int EDITOR_LIST_START = 20;
    static public final int EDITOR_WIN_WORD7 = 21;
    static public final int EDITOR_WIN_WORD97 = 22;
    static public final int EDITOR_WIN_WORD2000 = 23;
    static public final int EDITOR_MAC_WORD98 = 24;
    static public final int EDITOR_MAC_WORD2001 = 25;
    static public final int EDITOR_OTHER = 26;
    static public final int EDITOR_WIN_WORD2000_ANDABOVE = 27;
    static public final int EDITOR_XLIFF = 28;
    static public final int EDITOR_OMEGAT = 29;
    static public final int EDITOR_LIST_END = 30;

    // FILE FORMAT IDs ( a user selectable download option )
    // add values between start and end
    static public final int DOWNLOAD_FILE_FORMAT_LIST_START = 30;
    /**
     * Extracted segments presented in a list view. Read using the common list
     * view text parser (JavaCC grammer).
     */
    static public final int DOWNLOAD_FILE_FORMAT_TXT = 31;
    /**
     * Extracted segments presented in a list view. Read using the common list
     * view text parser (JavaCC grammer).
     */
    static public final int DOWNLOAD_FILE_FORMAT_RTF = 32;
    /**
     * Extracted segments presented in a list view. Segments presented with
     * embedded source - Tradso style. Read using the common list view text
     * parser (JavaCC grammer).
     */
    static public final int DOWNLOAD_FILE_FORMAT_TRADOSRTF = 33;
    /**
     * Extracted segments presented in psuedo paragraphs. Uses own form of
     * segments Ids. Read directly with RTF parser.
     */
    static public final int DOWNLOAD_FILE_FORMAT_RTF_PARAVIEW_ONE = 34;
    static public final int DOWNLOAD_FILE_FORMAT_XLF = 35;
    static public final int DOWNLOAD_FILE_FORMAT_TTX = 36;
    static public final int DOWNLOAD_FILE_FORMAT_TRADOSRTF_OPTIMIZED = 37;
    static public final int DOWNLOAD_FILE_FORMAT_OMEGAT = 39;
    static public final int DOWNLOAD_FILE_FORMAT_XLF20 = 40;
    static public final int DOWNLOAD_FILE_FORMAT_LIST_END = 41;

    // EDITALL STATE (a user selectable download option)
    static public final int DOWNLOAD_EDITALL_STATE_UNAUTHORIZED = 40;
    static public final int DOWNLOAD_EDITALL_STATE_YES = 41;
    static public final int DOWNLOAD_EDITALL_STATE_NO = 42;

    static public final int TM_EDIT_TYPE_NONE = 0;
    static public final int TM_EDIT_TYPE_BOTH = 1;
    static public final int TM_EDIT_TYPE_ICE = 2;
    static public final int TM_EDIT_TYPE_100 = 3;
    static public final int TM_EDIT_TYPE_DENY = 4;

    // RESOURCE LINKING (a user selectable download option)
    // add values between start and end
    static public final int MAKE_RES_START = 50;
    static public final int MAKE_RES_ATNS = 51;
    static public final int MAKE_RES_LINKS = 52;
    static public final int MAKE_SINGLE_RES_LINK = 53;
    static public final int MAKE_RES_NONE = 54;
    static public final int MAKE_RES_END = 55;
    static public final int MAKE_RES_TMX_PLAIN = 56;
    static public final int MAKE_RES_TMX_14B = 57;
    static public final int MAKE_RES_TMX_BOTH = 58;
    static public final int TOOL_RES_END = 59;// its name should be
                                              // "MAKE_RES_END".

    // Segment catagories
    static public final int MATCH_TYPE_UNDEFINED = 60;
    static public final int MATCH_TYPE_EXACT = 61;
    static public final int MATCH_TYPE_UNVERIFIED_EXACT = 62;
    static public final int MATCH_TYPE_FUZZY = 63;
    static public final int MATCH_TYPE_NOMATCH = 64;

    // Terminology format
    static public final int TERM_GLOGALSIGHT = 70;
    static public final int TERM_HTML = 71;
    static public final int TERM_TRADOS = 72;

    static public final int DOWNLOAD_MAX_FILE_PREFIX_LEN = 20;

    static public final String RESOURCES_DIR = "RESOURCES_DIR";
    static public final String SUPPORTFILE_DIR = "SUPPORTFILES_DIR";
    static public final String INBOX_NAME = "INBOX_NAME";
    static public final String OUTBOX_NAME = "OUTBOX_NAME";
    static public final String PTF_DIR = "PTF_DIR";
    static public final String STF_DIR = "STF_DIR";
    static public final String SOURCE_DIR = "SOURCE_DIR";
    static public final String OUTBOX_PLACEHOLDER = "OUTBOX_PLACEHOLDER";

    //
    // *****************************************************************
    // Word client: start
    //

    // NOTE: These values must be synchronized with constants declared
    // in the word template.

    /**
     * The version number of the paragraph view environment.
     * 
     * The version should be updated if changes to any of the five RTF files
     * (Tm, tag, term, src, target) requires changes to the gs4win.dot template.
     * The template is located in
     * com\globalsight\everest\edit\offline\download\MSWord.
     * 
     * NOTE: You must synchronize the VBA methods in the template that check and
     * compare this version number (see VBA macro code).
     */
    static public final String WC_VERSION = "1.1";

    /**
     * ParagraphView: prefix for the Binary resource file - Value must be
     * synchronized with offline client.
     */
    static public final String WC_PREFIX_BINRES = "res_";

    /**
     * ParagraphView: prefix for the Binary index text file - Value must be
     * synchronized with offline client.
     */
    static public final String WC_PREFIX_IDXRES = "idx_";

    /**
     * ParagraphView: prefix for the Source resource document - Value must be
     * synchronized with offline client.
     */
    static public final String WC_PREFIX_SRCDOC = "src_";

    /**
     * ParagraphView: prefix for the TM resource document - Value must be
     * synchronized with offline client.
     */
    static public final String WC_PREFIX_TMDOC = "tm_";

    /**
     * ParagraphView: prefix for the TagInfo resource document - Value must be
     * synchronized with offline client.
     */
    static public final String WC_PREFIX_TAGDOC = "tag_";

    /**
     * ParagraphView: prefix for the Term resource document - Value must be
     * synchronized with offline client.
     */
    static public final String WC_PREFIX_TERMDOC = "term_";

    /**
     * ParagraphView: segment id suffix which enables html addables in a segment
     * - Value must be synchronized with offline client.
     */
    static public final String WC_SUFFIX_SEGID_ADDABLE_HTML = "_a";

    /**
     * ParagraphView: value of empty merge record - Value must be synchronized
     * with offline client.
     */
    static public String NO_MERGE_RECORDS = "no_merged_segments";

    /**
     * ParagraphView: used instead of SEGMENT_ID_DELIMITER to create multipart
     * segment names in ms-word. Word does not allow a colon in a bookmark name.
     */
    static public final char BOOKMARK_SEG_ID_DELIM = '_';

    /**
     * ParagraphView: the merge record delimiter - must be the same used by
     * client code.
     */
    static public final char MERGE_RECORD_DELIM = ',';

    /**
     * ParagraphView: the default value for the current document text entry in
     * the TM resource file - Value must be synchronized with offline client.
     */
    static public final String WC_CUR_DOC_TEXT_DEFAULT = "empty";

    /**
     * ParagraphView: the presentation value for an empty segment - Value must
     * be synchronized with offline client.
     */
    static public final String WC_EMPTY_SEG_PLACEHOLDER_TEXT = "EmptySegment";

    /**
     * ParagraphView: the lower case version of WC_EMPTY_SEG_PLACEHOLDER_TEXT
     * used to speed comparisons.
     */
    static public final String WC_EMPTY_SEG_PLACEHOLDER_TEXT_LC = "emptysegment";

    /**
     * ParagraphView: MS-Words bookmark limit 16,379;
     * http://support.microsoft.com/default.aspx?scid=kb;en-us;211489
     */
    static public final long MSWORD2000_BOOKMARK_LIMIT = 16379;

    /**
     * ParagraphView: MS-Words field limit 32K;
     * http://support.microsoft.com/default.aspx?scid=kb;en-us;211489
     */
    static public final long MSWORD2000_FIELD_LIMIT = 32000;

    //
    // Word client: end
    // *****************************************************************
    //

    /** Split merge offsetBase. */
    static public final int SPLIT_MERGE_OFFSET_BASE = 100;

    /** Primary target file suffix */
    static public final String PRIMARY_SUFFIX = "P";
    /** Secondary target file suffix */
    static public final String SECONDARY_SUFFIX = "S";

    // Utility files
    /** MsWord Template name (AOR-Template.dot). */
    static public final String MSWORD_TEMPLATE_FNAME = "AOR-Template.dot";

    static public final String UPLOAD_IN_PROGRESS = "uploadInProgress";
    static public final String UPLOAD_DONE = "uploadDone";
}
