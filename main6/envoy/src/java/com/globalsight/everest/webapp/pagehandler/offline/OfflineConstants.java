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
package com.globalsight.everest.webapp.pagehandler.offline;

/**
 * This interface contains offline related constants.
 * 
 */
public interface OfflineConstants
{
    // HttpSession attribute name
    String ERROR_MESSAGE = "offline.upload.errorMessage";
    // String DOWNLOAD_PAGE_LIST = "offline.download.pages";
    String DOWNLOAD_GLOSSARY_STATE = "offline.download.glossaryState";

    // ServletRequest attribute name
    String UPLOAD_SUCCESS = "offline.upload.success";
    String UPLOAD_FILE_NAME = "offline.upload.fileName";
    String DOWNLOAD_ENCODING_OPTIONS = "offline.download.encodings";
    String DOWNLOAD_EDIT_EXACT = "offline.download.editExact";
    String DOWNLOAD_TM_EDIT_TYPE = "offline.download.TMEditType";
    String DOWNLOAD_JOB_NAME = "offline.download.jobName";
    String DOWNLOAD_ACCEPT_DOWNLOAD = "offline.download.acceptdownload";
    String DOWNLOAD_HAS_EXTRACTED_FILES = "offline.download.hasextractedfiles";

    // control names in upload.jsp
    String UPLOAD_FILE_FIELD = "fileField";

    // control names in download.jsp
    String FORMAT_SELECTOR = "format";
    String EDITOR_SELECTOR = "editor";
    String ENCODING_SELECTOR = "encoding";
    String PTAG_SELECTOR = "placeholder";
    // String EDIT_EXACT_SELECTOR = "editExact";
    String TM_EDIT_TYPE = "TMEditType";
    String PAGE_CHECKBOXES = "pageCheckBoxes";
    String GLOSSARY_CHECKBOXES = "supportFileCheckBoxes";
    String STF_CHECKBOXES = "stfCheckBoxes";
    String PRI_SOURCE_CHECKBOXES = "priSrcCheckBoxes";

    // Option values in download.jsp
    // format selector value
    String FORMAT_RTF = "rtf";
    String FORMAT_RTF_PARA_VIEW = "rtfParaView";
    String FORMAT_RTF_TRADOS = "rtfTrados";
    String FORMAT_RTF_TRADOS_OPTIMIZED = "rtfTradosOptimized";
    String FORMAT_TEXT = "text";

    // editor selector value
    String EDITOR_WIN2000 = "WinWord2000";
    String EDITOR_WIN2000_ANDABOVE = "WinWord2000AndAbove";
    String EDITOR_WIN97 = "WinWord97";
    String EDITOR_MAC2001 = "MacWord2001";
    String EDITOR_MAC98 = "MacWord98";
    String EDITOR_OTHER = "Other";
    String EDITOR_TRADOS_TAGEDITOR = "TradosTagEditor";
    String EDITOR_OMEGAT = "OmegaT";

    // ptag format selector value
    String PTAG_COMPACT = "compact";
    String PTAG_VERBOSE = "verbose";

    // edit exact selector value
    String EDIT_EXACT_NO = "no";
    String EDIT_EXACT_YES = "yes";

    // encoding selector value
    String ENCODING_DEFAULT = "defaultEncoding";

    // resource insertion value
    String RES_INS_SELECTOR = "resInsSelector";
    String RES_INS_ATNS = "resInsAtns";
    String RES_INS_LINK = "resInsLink";
    String RES_INS_NONE = "resInsNone";
    String RES_INS_TMX_PLAIN = "resInsTmxPlain";
    String RES_INS_TMX_14B = "resInsTmx14b";
    String RES_INX_TMX_BOTH = "resInsTmxBoth";

    // terminology insertion value
    String TERM_SELECTOR = "termSelector";
    String TERM_GLOBALSIGHT = "termGlobalsight";
    String TERM_TRADOS = "termTrados";
    String TERM_NONE = "termNone";
    String TERM_HTML = "termHtml";
    String TERM_TBX = "tbx";
    String TERM_TXT = "termTxt";

    // Cookie names
    String COOKIE_FILE_FORMAT = "DownloadFileFormat";
    String COOKIE_EDITOR = "DownloadEditor";
    String COOKIE_ENCODING = "DownloadEncoding";
    String COOKIE_PTAG_FORMAT = "DownloadPtagFormat";
    String COOKIE_TM_EDIT_TYPE = "DownloadTMEditType";
    String COOKIE_RES_INS_MODE = "DownloadResInsMode";

    // xlf names
    String FORMAT_XLF_NAME_12 = "xlf12";
    String FORMAT_XLF_VALUE_12 = "Xliff 1.2";
    String EDITOR_XLF_NAME = "xlfEditor";
    String EDITOR_XLF_VALUE = "Xliff Editor";
    String FORMAT_XLF_VALUE_20 = "Xliff 2.0";
    

    // ttx names(only support Trados 7 TTX)
    String FORMAT_TTX_NAME = "Trados 7 TTX";
    String FORMAT_TTX_VALUE = "TTX";

    String FORMAT_OMEGAT_NAME = "OmegaT";
    String FORMAT_OMEGAT_VALUE = "OmegaT";

    String CONSOLIDATE_TMX = "consolidate";
    String CONSOLIDATE_TERM = "consolidateTerm";

    String DISPLAY_EXACT_MATCH_NO = "no";
    String DISPLAY_EXACT_MATCH_YES = "yes";
    String DISPLAY_EXACT_MATCH = "displayExactMatch";

    String CHANGE_CREATION_ID_FOR_MT_SEGMENTS = "changeCreationIdForMT";
    String CHANGE_SEPARATE_TM_FILE = "separateTMfile";

    String POPULATE_100 = "populate100";
    String POPULATE_FUZZY = "populatefuzzy";

    String NEED_CONSOLIDATE = "needConsolidate";
    String CONSOLIDATE_FILE_TYPE = "consolidateFileType";
    String WORD_COUNT_FOR_DOWNLOAD = "wordCountForDownload";
    String PRESERVE_SOURCE_FOLDER = "preserveSourceFolder";
    String INCLUDE_XML_NODE_CONTEXT_INFORMATION = "includeXmlNodeContextInformation";
    String INCLUDE_REPETITIONS = "includeRepetitions";
    String EXCLUDE_FULLY_LEVERAGED_FILES = "excludeFullyLeveragedFiles";

    // If the task is TASK_UPLOADING_STATUS, can't complete the task.
    String TASK_UPLOADSTATUS_UPLOADING = "taskUploading";
    String TASK_UPLOADSTATUS_FINISHED = "taskFinished";
    String PONUD_SIGN = "\uE000" + "_POUND_SIGN_" + "\uE000";
}
