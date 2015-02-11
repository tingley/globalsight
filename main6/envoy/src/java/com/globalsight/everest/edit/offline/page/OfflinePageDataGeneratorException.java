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
package com.globalsight.everest.edit.offline.page;

import com.globalsight.util.GeneralException;
import com.globalsight.util.GeneralExceptionConstants;

/**
 * An exception handling object for OfflinePageManagement component.
 */
public class OfflinePageDataGeneratorException
    extends GeneralException
    implements GeneralExceptionConstants
{
    //message keys - for new GeneralException

    /** Message Key" message takes one arg: the unknown encoding */
    static public final String MSG_UNKNOWN_ENCODING =
        "UnknownEncoding";
    /** Message Key" message takes two args: source tuv Id and target tuv Id */
    static public final String MSG_UNMATCHED_PARENTS_OF_SUBS =
        "UnmatchedParentsOfSubs";
    /** Message Key" message takes six args: sourceTuvId={0}, sourceNumOfSubs={1},
        srcGxml={2}, targetTuvId={3}, targetNumOfSubs={4}, targetGxml={5} */
    static public final String MSG_UNMATCHED_SUBS =
        "UnmatchedSubs";
    /** Message Key" message takes one arg: the page id */
    static public final String MSG_SOURCE_PAGE_NOT_FOUND =
        "SourcePageNotFound";
    /** Message Key" message takes one arg: the page id */
    static public final String MSG_SOURCE_PAGE_TUVID_LIST_NOT_FOUND =
        "SourcePageTuvListNotFound";
    /**
     * Message Key" message takes two args: the bad id and the subflow
     * gxml from which it came.
     */
    static public final String MSG_FAILED_TO_GET_SUBFLOW_ID =
        "FailedToGetSubId";
    /** Message Key" message takes two args: the page id and the target locale */
    static public final String MSG_FAILED_TO_GET_INTERPRETED_TUIDS =
        "FailedToGetInterpretedTuIds";
    /** Message Key" message takes one arg: the segmetn id*/
    static public final String MSG_FAILED_TO_MAP_SEGID_TO_RES_DATA =
        "FailedToMapSegIdToRessourceData";

    // takes no arguments
    /** Message Key" message takes no arguments*/
    static public final String MSG_FAILED_ARGS_GETDOWNLOADPAGE =
        "FailedArgsGetDownloadPage";
    /** Message Key" message takes no arguments*/
    static public final String MSG_FAILED_TO_GET_SRC_TUV_MAP =
        "FailedToGetSrcTuvMap";
    /** Message Key" message takes no arguments*/
    static public final String MSG_FAILED_TO_GET_TRG_TUV_MAP =
        "FailedToGetTrgTuvMap";
    /** Message Key" message takes no arguments*/
    static public final String MSG_FAILED_TO_GET_LEVERAGE_MAP =
        "FailedToGetLeverageMap";

    /**
     * Constructor.
     * @param p_originalException original exception
     */
    public OfflinePageDataGeneratorException(Exception p_originalException)
    {
        super(p_originalException);
    }


    /**
     * Constructor.
     * @param p_messageKey the message key
     * @param p_messageArguments message srgs
     * @param p_originalException original exception
     */
    public OfflinePageDataGeneratorException(String p_messageKey,
        String[] p_messageArguments, Exception p_originalException)
    {
        super(p_messageKey, p_messageArguments, p_originalException);
    }
}
