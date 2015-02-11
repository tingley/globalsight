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

package com.globalsight.terminology;

import com.globalsight.util.GeneralException;

import java.util.*;

/**
 * <p>The Terminology Exception class.</p>
 */
public class TermbaseException
    extends GeneralException
    implements TermbaseExceptionMessages
{
    /**
     * Terminology related messages are stored in the following
     * property file.
     */
    private final static String PROPERTY_FILE_NAME = "TermbaseException";

    /**
     * <p>This constructor is used when a subclass of GeneralException
     * is wrapped.  In this case the wrapped exception already has the
     * message related information (unless a new message or arguments
     * are needed).</p>
     *
     * @see GeneralException#GeneralException(Exception)
     *
     * @param p_originalException Original exception that caused the error
     */
    public TermbaseException(Exception p_originalException)
    {
        super(p_originalException);
    }

    /**
     * @see GeneralException#GeneralException(String,String[],Exception,String)
     *
     * @param p_messageKey key in properties file
     * @param p_messageArguments Arguments to the message. Can be null.
     * @param p_originalException Original exception that caused the
     * error.  Can be null.
     */
    public TermbaseException(String p_messageKey,
        String[] p_messageArguments, Exception p_originalException)
    {
        this(p_messageKey, p_messageArguments, p_originalException,
            PROPERTY_FILE_NAME);
    }

    /**
     * @see GeneralException#GeneralException(String,String[],Exception,String)
     *
     * @param p_messageKey key in properties file
     * @param p_messageArguments Arguments to the message. Can be null.
     * @param p_originalException Original exception that caused the
     * error.  Can be null.
     * @param p_propertyFileName Property file base name. If the
     * property file is LingMessage.properties, the parameter should
     * be "LingMessage".
     */
    protected TermbaseException(String p_messageKey,
        String[] p_messageArguments, Exception p_originalException,
        String p_propertyFileName)
    {
        super(p_messageKey, p_messageArguments, p_originalException,
            p_propertyFileName);
    }
}
