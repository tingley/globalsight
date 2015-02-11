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
package com.globalsight.everest.secondarytargetfile;

import com.globalsight.util.GeneralException;


/**
 * An exception thrown during the process of creating and
 * processing Pages.
 */
public class SecondaryTargetFileException extends GeneralException
{
    /**
     * Page related messages are stored in the following property file
     */
    public final static String PROPERTY_FILE_NAME = "SecondaryTargetFileException";

    // Message keys
    public final static String MSG_FAILED_TO_CREATE_STF = 
        "createStfFailed";

    public final static String MSG_FAILED_TO_DELETE_STF = 
        "deleteStfFailed";

    public final static String MSG_FAILED_TO_GET_EBE = 
        "getExportBatchEventFailed";

    public final static String MSG_FAILED_TO_GET_STF = 
        "getStfFailed";

    public final static String MSG_FAILED_TO_GET_WF = 
        "getWfFailed";

    public final static String MSG_FAILED_TO_UPDATE_STF = 
        "updateStfFailed";

    public final static String MSG_FAILED_TO_UPDATE_WF_STATE = 
        "updateWfStateForStfFailed";

    

    /**
     * @see GeneralException#GeneralException(Exception)
     * This constructor is used when a subclass of GeneralException is wrapped.
     * In this case the wrapped exception already has the message related
     * information (unless a new message or arguments are needed).
     *
     * @param p_originalException Original exception that caused the error
     */
    public SecondaryTargetFileException(Exception p_originalException)
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
    public SecondaryTargetFileException(String p_messageKey,
                            String[] p_messageArguments,
                            Exception p_originalException)
    {
        super(p_messageKey, p_messageArguments, 
              p_originalException, PROPERTY_FILE_NAME);
    }    
}
