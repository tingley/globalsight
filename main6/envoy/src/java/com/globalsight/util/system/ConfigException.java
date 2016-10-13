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
package com.globalsight.util.system;

import com.globalsight.util.GeneralException;

/**
 * This exception is thrown for all access to configuration
 * parameters.
 *
 * @version     1.0, (7/12/00 10:00:35 AM)
 * @author      Marvin Lau, mlau@globalsight.com
 */
public class ConfigException
    extends GeneralException
{
    /**
     * The type of the configuration parameter implementation is
     * invalid.
     */
    public static final int EX_INVALIDTYPE   = 1001;

    /**
     * Specified parameter is not found.
     */
    public static final int EX_PARAMNOTFOUND = 1002;

    /**
     * The value of the specified parameter is invalid.  E.g. the
     * client expects the parameter is an integer while the parameter
     * value is not in a valid integer format.
     */
    public static final int EX_BADPARAM      = 1003;

    /**
     * Constructs an instance using the given exception identification.
     *
     * @param p_exceptionId Reason for the exception.
     */
    public ConfigException(int p_exceptionId)
    {
        super(COMP_SYSUTIL, p_exceptionId);
    }

    /**
     * Constructs an instance using the given exception
     * and message identification.
     *
     * @param p_exceptionId int
     * @param p_messageId int
     */
    public ConfigException(int p_exceptionId, int p_messageId)
    {
        super(COMP_SYSUTIL, p_exceptionId, p_messageId);
    }

    /**
     * Constructs an instance using the given component and exception
     * identification, and the original exception.
     *
     * @param p_exceptionId Reason for the exception.
     * @param p_messageId Explanation of the exception.
     * @param p_originalException Original exception that this
     * exception identifies.
     */
    public ConfigException(int p_exceptionId, int p_messageId,
        Exception p_originalException)
    {
        super(COMP_SYSUTIL, p_exceptionId, p_messageId, p_originalException);
    }

    /**
     * Constructs an instance using the given exception identification
     * and the original exception.
     *
     * @param p_exceptionId Reason for the exception.
     * @param p_originalException Original exception that this
     * exception identifies.
     */
    public ConfigException(int p_exceptionId, Exception p_originalException)
    {
        super(COMP_SYSUTIL, p_exceptionId, p_originalException);
    }

    /**
     * Constructs an instance using the given exception
     * identification and message.
     *
     * @param p_exceptionId Reason for the exception.
     * @param p_message java.lang.Explanation of the exception.
     */
    public ConfigException(int p_exceptionId, String p_message)
    {
        super(COMP_SYSUTIL, p_exceptionId, p_message);
    }
}
