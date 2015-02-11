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
package com.globalsight.everest.edit.offline.upload;

import com.globalsight.util.GeneralException;
import com.globalsight.util.GeneralExceptionConstants;

/**
 * An exception handling object for OfflinePageManagement component.
 */
public class UploadPageSaverException
    extends GeneralException
    implements GeneralExceptionConstants
{
    // GENERAL upload errors

    /** takes one arg - the unknown encoding */
    static public final String MSG_UNKNOWN_ENCODING =
        "UnknownEncoding";
    /** takes two args - source tuv Id and target tuv Id */
    static public final String MSG_UNMATCHED_PARENTS_OF_SUBS =
        "UnmatchedParentsOfSubs";
    /** takes two args - source tuv Id and target tuv Id */
    static public final String MSG_UNMATCHED_SUBS =
        "UnmatchedSubs";
    /** takes two args - the page id and the target locale */
    static public final String MSG_FAILED_TO_GET_INTERPRETED_TUIDS =
        "FailedToGetInterpretedTuIds";
    /** takes no arguments */
    static public final String MSG_FAILED_ARGS_GET_REFPAGE =
        "FailedArgsGetReferencePage";
    /** takes no arguments */
    static public final String MSG_FAILED_TO_GET_SRC_TUV_MAP =
        "FailedToGetSrcTuvMap";
    /** takes no arguments */
    static public final String MSG_FAILED_TO_GET_TRG_TUV_MAP =
        "FailedToGetTrgTuvMap";
    /** takes no arguments */
    static public final String MSG_INVALID_UPLOAD_MATCH_TYPE =
        "InvalidUploadMatchType";
    /** takes no arguments */
    static public final String MSG_NO_OFFLINE_REFERENCE_SEGMENT =
        "NoOfflineReferenceSegment";
    /** takes one arg - the segment id that failedpage id and the target locale */
    static public final String MSG_INVALID_MERGE_ID =
        "InvalidMergedSegmentId";
    /** takes no args -  */
    static public final String MSG_FAILED_TO_POST_JMS_UPLOAD_SAVE =
        "FailedToPostJmsUploadSave";

    // RTF COMMON errors

    /** Takes two args: 1) inner bookmark 2) outer bookmark */
    static public final String MSG_EMBEDDED_BOOKMARKS =
        "OverlappingOrEmbeddedBookmarks";

    // RTF PARAGRAPH VIEW ONE(PV1) errors

    /** Takes a series of arguments. Please see UploadPageSaver.properties. */
    static public final String MSG_FAILED_TO_GET_PV1_DOC_VARS =
        "FaliedToGetPV1DocVars";


    // SYNCHRONIZATION errors

    /** Takes 1 arg, the page id */
    static public final String MSG_PAGE_IS_BEING_UPLOADED =
        "PageBeingUploaded";

    /** Takes 1 arg, the page id */
    static public final String MSG_PAGE_IS_BEING_UPDATED =
        "PageBeingUpdated";


    /**
     * Constructor.
     */
    public UploadPageSaverException(Exception p_originalException)
    {
        super(p_originalException);
    }


    /**
     * Constructor.
     */
    public UploadPageSaverException(String p_messageKey,
        String[] p_messageArguments, Exception p_originalException)
    {
        super(p_messageKey, p_messageArguments, p_originalException);
    }
}
