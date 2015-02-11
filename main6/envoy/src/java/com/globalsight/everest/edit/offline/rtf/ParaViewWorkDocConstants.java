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

package com.globalsight.everest.edit.offline.rtf;

/**
 * Shared constants used to read/write the work doc rtf.
 */
public interface ParaViewWorkDocConstants
{
    // RTF document variables
    /** RTF DocVar name: the format of the original document */
    static public final String DOCVAR_NAME_NATIVEDOCFMT = "nativeDocFormat";
    /** RTF DocVar name: system page id */
    static public final String DOCVAR_NAME_PAGEID = "pageId";
    /** RTF DocVar name: system task id */
    static public final String DOCVAR_NAME_TASKID = "taskId";
    /** RTF DocVar name: system workflow id */
    static public final String DOCVAR_NAME_WORKFLOWID = "workflowId";
    /** RTF DocVar name: source locale of the page being written*/
    static public final String DOCVAR_NAME_SRCLOCALE = "srcLocale";
    /** RTF DocVar name: target locale of the page being written*/
    static public final String DOCVAR_NAME_TRGLOCALE = "trgLocale";
    /** RTF DocVar name: exact match count*/
    static public final String DOCVAR_NAME_EXACTCNT = "exactCnt";
    /** RTF DocVar name: fuzzy match count*/
    static public final String DOCVAR_NAME_FUZZYCNT = "fuzzyCnt";
    /** RTF DocVar name: nomatch match count*/
    static public final String DOCVAR_NAME_NOMATCHCNT = "noMatchCnt";
    /** RTF DocVar name: ptag format (COMPACT or VERBOSE )*/
    static public final String DOCVAR_NAME_PTAGFMT = "tagFormat";
     /** RTF DocVar name: holds the name of the corresponding index document **/
    static public final String DOCVAR_NAME_RESIDX = "indexFile";
    /** RTF DocVar name: holds the name of the corresponding res document **/
    static public final String DOCVAR_NAME_BINRES = "resFile";
    /** RTF DocVar name: name of the corresponding SRC resource document*/
    static public final String DOCVAR_NAME_SRCDOC = "srcRes";
    /** RTF DocVar name: name of the corresponding TM resource document*/
    static public final String DOCVAR_NAME_TMDOC = "dataDoc";
    /** RTF DocVar name: name of the corresponding TAG INFO resource document*/
    static public final String DOCVAR_NAME_TAGDOC = "tagInfoDoc";
    /** RTF DocVar name: name of the corresponding Term resource document*/
    static public final String DOCVAR_NAME_TERMDOC = "termDoc";
    /** RTF DocVar name: encoding of the file being written */
    static public final String DOCVAR_NAME_ENCODING = "encoding";
    /** RTF DocVar name: Default client option - show source paragraph*/
    static public final String DOCVAR_NAME_SHOWSRCPARA = "ShowSourceAsPara";
    /** RTF DocVar name: Default client option - highlight exact match segments*/
    static public final String DOCVAR_NAME_COLOREXACT = "HighlightExact";
    /** RTF DocVar name: Default client option - highlight fuzzy match segments*/
    static public final String DOCVAR_NAME_COLORFUZZY = "HighlightFuzzy";
    /** RTF DocVar name: Default client option - ptag display format*/
    static public final String DOCVAR_NAME_PTAGPRESENTATION = "tagPresentation";
    /** RTF DocVar name: Default client option - show warning in popup dialog*/
    static public final String DOCVAR_NAME_BMKPOPUPWARNINGS = "stopAnnoyingMeAboutBookmarks";
    /** RTF DocVar name: Client parameter - the merge record*/
    static public final String DOCVAR_NAME_MERGEDATA = "mergeData";
    /**
     * A unique string that identifies this file format.
     *
     * This string is stored in the RTF info section under "title".
     * This string is used regular expressions to recognize our files
     * programatically.
     *
     * See UploadPageHandler where document recognition is performed.
     */
    static public final String WORK_DOC_TITLE = "GlobalSight Extracted Paragraph view Export";
}
