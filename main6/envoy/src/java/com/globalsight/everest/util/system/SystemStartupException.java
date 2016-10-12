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
package com.globalsight.everest.util.system;

import com.globalsight.util.GeneralException;

/**
 * This exception is thrown for exceptions that happens during system
 * startup.
 */
public class SystemStartupException
    extends SystemUtilException
{
    // message file name
    private static final String PROPERTY_FILE_NAME = "SystemStartupException";


    // error messages
    public static final String MSG_FAILED_TO_START_REQUESTHANDLER = "FailedToStartupRequestHandler";
    public static final String MSG_FAILED_TO_START_PAGEMANAGER = "FailedToStartupPageManager";
    public static final String MSG_FAILED_TO_SETUP_JMS = "FailedToSetupJMS";

    
    /**
     * @see GeneralException(String, String[], Exception) constructor.
     */
    public SystemStartupException(String p_messageKey,
        String[] p_messageArguments, Exception p_originalException)
    {
        super(p_messageKey, p_messageArguments, p_originalException, PROPERTY_FILE_NAME);
    }

    /**
     * Constructs an instance using the given exception id.
     * <p>
     * @param p_exceptionId Reason for the exception.
     */
    public SystemStartupException(int p_exceptionId)
    {
        super(p_exceptionId);
    }

    /**
     * Constructs an instance using the exception and message id.
     * <p>
     * @param p_exceptionId Reason for the exception.
     * @param p_messageId Explanation of the exception.
     */
    public SystemStartupException(int p_exceptionId, int p_messageId)
    {
        super(p_exceptionId, p_messageId);
    }

    /**
     * Constructs an instance using the exception id and message.<p>
     * @param p_exceptionId Reason for the exception.
     * @param p_message Explanation of the exception.
     */
    public SystemStartupException(int p_exceptionId, String p_message)
    {
        super(p_exceptionId, p_message);
    }

    /**
     * Constructs an instance using the exception id and the original
     * exception.  <p>
     * @param p_exceptionId Reason for the exception.
     * @param p_originalException Original exception that this
     * exception identifies.
     */
    public SystemStartupException(int p_exceptionId,
        Exception p_originalException)
    {
        super(p_exceptionId, p_originalException);
    }

    /**
     * Constructs an instance using the exception id, and the original
     * exception.  <p>
     * @param p_exceptionId Reason for the exception.
     * @param p_messageId Explanation of the exception.
     * @param p_originalException Original exception that this
     * exception identifies.
     */
    public SystemStartupException(int p_exceptionId, int p_messageId,
        Exception p_originalException)
    {
        super(p_exceptionId, p_messageId, p_originalException);
    }

    /**
     * Constructs an instance using the exception id, message id, and
     * the original exception.  NOTE: THIS CONSTRUCTOR IS USED FOR
     * COMPOUND ERROR MESSAGES.  <p>
     * @param p_exceptionId The id that specifies the exception.
     * @param p_messageId The id that specifies the explanation for
     * the exception.
     * @param p_messageArguments The message arguments in order.
     * @param p_originalException The original exception that this
     * exception edentifies.
     */
    public SystemStartupException(int p_exceptionId, int p_messageId,
        String[] p_messageArguments, Exception p_originalException)
    {
        super(p_exceptionId, p_messageId, p_messageArguments,
            p_originalException);
    }
}
