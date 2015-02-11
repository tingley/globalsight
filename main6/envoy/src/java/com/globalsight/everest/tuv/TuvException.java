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
package com.globalsight.everest.tuv;

import com.globalsight.util.GeneralException;
import com.globalsight.util.GeneralExceptionConstants;

import java.lang.Exception;
import java.lang.String;

/**
 * An exception thrown during the process of creating and
 * processing Tuv.
 */
public class TuvException extends GeneralException
{

    /**
     * @see GeneralException#GeneralException(int, int)
     * @param p_componentId Component where the exception originated from.
     * @param p_exceptionId Reason for the exception.
     */
    public TuvException(int p_componentId, int p_exceptionId)
    {
        super(p_componentId, p_exceptionId);
    }

    /**
     * @see GeneralException#GeneralException(int, int, Exception)
     * @param p_componentId Component where the exception originated from.
     * @param p_exceptionId Reason for the exception.
     * @param p_originalException Original exception that this exception identifies.
     */
    public TuvException(int p_componentId, int p_exceptionId, Exception p_originalException)
    {
        super(p_componentId, p_exceptionId, p_originalException);
    }

    /**
     * @see GeneralException#GeneralException(int, int, String)
     * @param p_componentId Component where the exception originated from.
     * @param p_exceptionId Reason for the exception.
     * @param p_message Explanation of the exception.
     */
    public TuvException(int p_componentId, int p_exceptionId, String p_message)
    {
        super(p_componentId, p_exceptionId, p_message);
    }

    /**
     * @see GeneralException#GeneralException(int, int, int)
     * @param p_componentId Component where the exception originated from.
     * @param p_exceptionId Reason for the exception.
     * @param p_messageId Explanation of the exception.
     */
    public TuvException(int p_componentId, int p_exceptionId, int p_messageId)
    {
        super(p_componentId, p_exceptionId, p_messageId);
    }

    /**
     * @see GeneralException#GeneralException(int, int, int, Exception)
     * @param p_componentId Component where the exception originated from.
     * @param p_exceptionId Reason for the exception.
     * @param p_messageId Explanation of the exception.
     * @param p_originalException Original exception that this exception identifies.
     */
    public TuvException(int p_componentId, int p_exceptionId,
            int p_messageId, Exception p_originalException)
    {
        super(p_componentId, p_exceptionId, p_messageId, p_originalException);
    }

    /**
     * @see GeneralException#GeneralException(int, int, int, String[], Exception)
     * @param p_componentId Component where the exception originated from.
     * @param p_exceptionId Reason for the exception.
     * @param p_messageId Explanation of the exception.
     * @param p_messageArguments The message arguments in order.
     * @param p_originalException Original exception that this exception edentifies.
     */
    public TuvException(int p_componentId, int p_exceptionId, int p_messageId,
                String[] p_messageArguments, Exception p_originalException)
    {
        super(p_componentId, p_exceptionId, p_messageId,
                p_messageArguments, p_originalException);
    }

    /**
     * @see GeneralException#GeneralException(int, int, int, Exception)
     * @param p_originalException Original exception that this exception identifies.
     */
    public TuvException(Exception p_originalException)
    {
        super(GeneralExceptionConstants.EX_GENERAL, 
                GeneralExceptionConstants.COMP_FOUNDATION, p_originalException);
    }

    /**
     * @see GeneralException#GeneralException(int, int, int, String)
     * @param p_message error message.
     */
    public TuvException(String p_message)
    {
        super(GeneralExceptionConstants.EX_GENERAL, 
                GeneralExceptionConstants.COMP_FOUNDATION, p_message);
    }

    /**
     * @see GeneralException#GeneralException(int, int, int, String)
     * @param p_message error message.
     * @param p_message error message.
     */
    public TuvException(String p_message, Exception p_originalException)
    {
        super(GeneralExceptionConstants.EX_GENERAL, 
                GeneralExceptionConstants.COMP_FOUNDATION, p_message, p_originalException);
    }
}