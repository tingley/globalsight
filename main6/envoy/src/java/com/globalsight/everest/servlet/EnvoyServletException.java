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

package com.globalsight.everest.servlet;

import com.globalsight.util.GeneralException;
import com.globalsight.util.GeneralExceptionConstants;

/**
 * This exception is thrown for any server related problems such as a
 * remote exceptions.
 */
public class EnvoyServletException
    extends GeneralException
    implements GeneralExceptionConstants
{

    /**
     * Servlet (PageHandler) related messages are stored in the
     * following property file
     */
    public final static String PROPERTY_FILE_NAME = "EnvoyServletException";

    //The message keys
    public static final String MSG_FAILED_TO_LOGIN = "failedToLogin";
    public static final String MSG_FAILED_TO_LOGOUT = "failedToLogout";
    public static final String MSG_FAILED_TO_DOWNLOAD = "failedToDownload";
    public static final String MSG_FAILED_TO_GET_WORKFLOW = "failedToGetWorkflowForTask";
    public static final String MSG_FAILED_TO_GET_GLOSSARIES = "failedToGetGlossaries";
    public static final String MSG_FAILED_TO_GET_COMMENT_REFERENCES = "failedToGetCommentReferences";
    public static final String MSG_FAILED_TO_UPDATE_WORD_COUNT = "failedToUpdateWordCount";
    public static final String MSG_FAILED_TO_UPLOAD_IMAGE = "failedToUploadImage";
    public static final String MSG_INVALID_CUSTOMER_KEY = "invalidCustomerKey";

    /**
     * Construct an instance of EnvoyServletException wrapping around
     * an Exception <p>
     * @param p_originalException the Original exception that this
     * exception edentifies.
     */
    public EnvoyServletException(Exception p_originalException)
    {
        super(p_originalException);
    }

    /**
     * Construct an instance of EnvoyServletException using this
     * component id, and the specified exception code.  <p>
     * @param p_exceptionId The type of exception.
     */
    public EnvoyServletException(int p_exceptionId)
    {
        super(COMP_SERVLET, p_exceptionId);
    }

    /**
     * Constructs an instance of EnvoyServletException using this
     * component id, specified exception code, and exception message.
     * <p>
     * @param p_exceptionId The type of exception.
     * @param p_message The exception message.
     */
    public EnvoyServletException(int p_exceptionId, String p_message)
    {
        super(COMP_SERVLET, p_exceptionId, p_message);
    }

    /**
     * Constructs an instance of EnvoyServletException using this
     * component id, specified exception code, and message id.  <p>
     * @param p_exceptionId The type of exception.
     * @param p_messageId The id for the message that explains the
     * details of the exception.
     */
    public EnvoyServletException(int p_exceptionId, int p_messageId)
    {
        super(COMP_SERVLET, p_exceptionId, p_messageId);
    }

    /**
     * Construct an instance of EnvoyServletException using this
     * component id, specified exception code and original exception.
     * <p>
     * @param p_exceptionId The type of exception.
     * @param p_originalException The originating exception.
     */
    public EnvoyServletException(int p_exceptionId,
        Exception p_originalException)
    {
        super(COMP_SERVLET, p_exceptionId, p_originalException);
    }

    /**
     * Constructs an instance of EnvoyServletException using this
     * component id, specified exception code, message id, and the
     * originating exception.  <p>
     * @param p_exceptionId The type of exception.
     * @param p_messageId The id for the message that explains the
     * details of the exception.
     * @param p_originalException The originating exception.
     */
    public EnvoyServletException(int p_exceptionId, int p_messageId,
        Exception p_originalException)
    {
        super(COMP_SERVLET, p_exceptionId, p_messageId, p_originalException);
    }

    /**
     * Constructs an instance of EnvoyServletException using this
     * component id, exception id, message id, and the originating
     * exception.  NOTE: THIS CONSTRUCTOR IS USED FOR COMPOUND ERROR
     * MESSAGES.  <p>
     * @param p_exceptionId Reason for the exception.
     * @param p_messageId Explanation of the exception.
     * @param p_messageArguments The message arguments in order.
     * @param p_originalException Original exception that this
     * exception edentifies.
     */
    public EnvoyServletException(int p_exceptionId, int p_messageId,
        String[] p_messageArguments, Exception p_originalException)
    {
        super(COMP_SERVLET, p_exceptionId, p_messageId, p_messageArguments,
            p_originalException);
    }

    /**
     * Constructs an instance of EnvoyServletException using the new
     * message scheme <p>
     * @param p_messageKey key in properties file
     * @param p_messageArguments Arguments to the message. It can be
     * null.
     * @param p_originalException Original exception that caused the
     * error. It can be null.
     */
    public EnvoyServletException(String p_messageKey,
        String[] p_messageArguments, Exception p_originalException)
    {
        super(p_messageKey, p_messageArguments, p_originalException);
    }


    /**
     * @see GeneralException#GeneralException(String, String[],
     * Exception, String)
     *
     * @param p_messageKey key in properties file
     * @param p_messageArguments Arguments to the message. It can be null.
     * @param p_originalException Original exception that caused the
     * error.  It can be null.
     * @param p_propertyFileName Property file base name. If the
     * property file is LingMessage.properties, the parameter should
     * be "LingMessage".
     */
    public EnvoyServletException(String p_messageKey,
        String[] p_messageArguments, Exception p_originalException,
        String p_propertyFileName)
    {
        super(p_messageKey, p_messageArguments, p_originalException,
              p_propertyFileName);
    }
}
