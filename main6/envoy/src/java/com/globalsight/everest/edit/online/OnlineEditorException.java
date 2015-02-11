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

package com.globalsight.everest.edit.online;

import com.globalsight.util.GeneralException;
import com.globalsight.util.GeneralExceptionConstants;

/**
 * This exception is thrown for any exception that is related to the
 * internal working of the Online Editor Manager component.
 */
public class OnlineEditorException
    extends GeneralException
{
    // Page related messages are stored in the following property file
    final static String PROPERTY_FILE_NAME = "OnlineEditorException";

    //
    // Component Specific Error Message keys
    //

    public static final String MSG_FAILED_TO_INIT_SERVER =
        "failedToInitServer";
    public static final String MSG_FAILED_TO_GET_PAGEVIEW =
        "failedToGetPageView";
    public static final String MSG_FAILED_TO_GET_SEGMENTVIEW =
        "failedToGetSegmentView";
    public static final String MSG_FAILED_TO_GET_COMMENTVIEW =
        "failedToGetCommentView";
    public static final String MSG_FAILED_TO_CREATE_COMMENT =
        "failedToUpdateComment";
    public static final String MSG_FAILED_TO_UPDATE_COMMENT =
        "failedToUpdateComment";
    public static final String MSG_FAILED_TO_UPDATE_SEGMENT =
        "failedToUpdateSegment";
    public static final String MSG_FAILED_TO_INDEX_SEGMENT =
        "failedToIndexSegment";
    public static final String MSG_FAILED_TO_UPLOAD_IMAGE =
        "failedToUploadImage";
    public static final String MSG_FAILED_TO_REPLACE_IMAGE =
        "failedToReplaceImage";
    public static final String MSG_FAILED_TO_GET_PAGEINFO =
        "failedToGetPageInfo";
    public static final String MSG_FAILED_TO_GET_TUIDS =
        "failedToGetTuIds";
    public static final String MSG_FAILED_TO_UPDATE_IMAGE_MAP =
        "failedToUpdateImageMap";
    public static final String MSG_FAILED_TO_MERGE =
        "failedToMerge";
    public static final String MSG_FAILED_TO_SPLIT =
        "failedToSplit";
    public static final String MSG_SOURCEPAGE_NOT_EDITABLE =
        "sourcePageNotEditable";
    public static final String MSG_FAILED_TO_LOAD_SOURCEPAGE =
        "failedToLoadSourcePage";

    /**
     * @see GeneralException#GeneralException(Exception)
     * This constructor is used when a subclass of GeneralException is wrapped.
     * In this case the wrapped exception already has the message related
     * information (unless a new message or arguments are needed).
     *
     * @param p_originalException Original exception that caused the error
     */
    public OnlineEditorException(Exception p_originalException)
    {
        super(p_originalException);
    }

    /**
     * @see GeneralException#GeneralException(String, String[], Exception, String)
     *
     * @param p_messageKey key in properties file
     * @param p_messageArguments Arguments to the message. It can be null.
     * @param p_originalException Original exception that caused the
     * error. It can be null.
     */
    public OnlineEditorException(String p_messageKey,
        String[] p_messageArguments, Exception p_originalException)
    {
        super(p_messageKey, p_messageArguments, p_originalException,
            PROPERTY_FILE_NAME);
    }
}
