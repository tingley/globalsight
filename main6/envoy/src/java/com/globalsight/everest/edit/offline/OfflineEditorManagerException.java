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

import com.globalsight.util.GeneralException;
import com.globalsight.util.GeneralExceptionConstants;

/**
 * <p>This exception is thrown for any exception that is related to
 * the internal working of the Offline Editor Manager component.</p>
 *
 * <p>
 * For this request handler component the EXCEPTION ID ranges from .
 * For this request handler component the ERROR message ID ranges from.
 * For this request handler component the INFO message ID ranges from .
 * For this request handler component the DEBUG message ID ranges from .
 * </p>
 */
public class OfflineEditorManagerException
    extends GeneralException
    implements GeneralExceptionConstants
{
    /**
     * Page related messages are stored in the property file
     * OfflineEditorManagerException.properties.
     */
    static public final String PROPERTY_FILE_NAME = "OfflineEditorManagerException";

    static public final String MSG_FAILED_TO_CREATE_JMS_POOL = "FailedToCreateJMSPool";

    /** Takes three args (SourcePage name, targetLocale name, p_mergeOverrideDirective). */
     static public final String MSG_TO_GET_PAGE_SEGMENTS = "FailedToGetPageSegments";

    /** Takes no args. A general message that says "see GlobalSight.log". */
     static public final String MSG_INTERNAL_ERROR = "InternalError";

    /**
     * @see GeneralException#GeneralException(Exception)
     * This constructor is used when a subclass of GeneralException is wrapped.
     * In this case the wrapped exception already has the message related
     * information (unless a new message or arguments are needed).
     *
     * @param p_originalException Original exception that caused the error
     */
    public OfflineEditorManagerException(Exception p_originalException)
    {
        super(p_originalException);
    }

    /**
     * @see GeneralException#GeneralException(String, String[], Exception, String)
     *
     * @param p_messageKey key in properties file
     * @param p_messageArguments Arguments to the message. It can be null.
     * @param p_originalException Original exception that caused the error.
     *        It can be null.
     */
    public OfflineEditorManagerException(String p_messageKey,
        String[] p_messageArguments,
        Exception p_originalException)
    {
        super(p_messageKey, p_messageArguments, p_originalException,
            PROPERTY_FILE_NAME);
    }
}
