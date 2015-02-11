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

package com.globalsight.everest.nativefilestore;

import com.globalsight.util.GeneralException;
import com.globalsight.util.GeneralExceptionConstants;

/**
 * Exception class for Native files and their persistence manager.
 */
public class NativeFileManagerException extends GeneralException
{
    public final static String PROPERTY_FILE_NAME = "NavtiveFileManagerException";

    public static final String MSG_FAILED_TO_SAVE_FILE = "failedToSave";
    public static final String MSG_FAILED_TO_GET_FILE = "failedToGet";
    public static final String MSG_FAILED_TO_INIT_FILESYSTEM_STORAGE = "failedToInitFileSysStorage";    
    public static final String MSG_INVALID_FILENAME = "invalidFileName";        
    public static final String MSG_FAILED_TO_MOVE_FILE = "failedToMoveFile";
    public static final String MSG_FAILED_TO_COPY_FILE = "failedToCopyFile";
    public static final String MSG_INVALID_ARGS = "invalidArgs";

    /**
     * Constructor that wraps an existing exception with
     * NativeFileException.
     *
     * @param p_exception Original exception object.
     */
    public NativeFileManagerException(Exception p_exception)
    {
        super(p_exception);
    }

    /**
     * Constructs an instance of NativeFileException using the new
     * message scheme <p>
     * @param p_messageKey key in properties file
     * @param p_messageArguments Arguments to the message. Can be
     * null.
     * @param p_originalException Original exception that caused the
     * error. Can be null.
     */
    public NativeFileManagerException(String p_messageKey, String[] p_messageArguments, Exception p_originalException)
    {
        super(p_messageKey, p_messageArguments, p_originalException);
    }
}
  