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

package com.globalsight.everest.glossaries;

import com.globalsight.util.GeneralException;
import com.globalsight.util.GeneralExceptionConstants;

/**
 * Exception class for glossary files and their persistence manager.
 */
public class GlossaryException
    extends GeneralException
{
    public final static String PROPERTY_FILE_NAME = "GlossaryException";

    public static final String MSG_FAILED_TO_UPLOAD_FILE = "failedToUpload";
    public static final String MSG_NONEXISTENT_OR_EMPTY_FILE = "nonExistentOrEmptyFile";
    public static final String MSG_FAILED_TO_DELETE_FILE = "failedToDelete";

    /**
     * Constructor that creates a exception with specified message
     * string.
     *
     * @param p_msg Exception message
     */
    public GlossaryException(String p_msg)
    {
        super(COMP_PERSISTENCE, EX_GLOSSARY, p_msg);
    }

    /**
     * Constructor that wraps an existing exception with
     * GlossaryException.
     *
     * @param p_exception Original exception object.
     */
    public GlossaryException(Exception p_exception)
    {
        super(COMP_PERSISTENCE, EX_GLOSSARY, p_exception);
    }

    /**
     * Constructs an instance of GlossaryException using the new
     * message scheme <p>
     * @param p_messageKey key in properties file
     * @param p_messageArguments Arguments to the message. Can be
     * null.
     * @param p_originalException Original exception that caused the
     * error. Can be null.
     */
    public GlossaryException(String p_messageKey,
        String[] p_messageArguments, Exception p_originalException)
    {
        super(p_messageKey, p_messageArguments, p_originalException);
    }
}
