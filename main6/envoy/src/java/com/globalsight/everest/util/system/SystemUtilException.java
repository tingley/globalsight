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
 * This exception is the root class for all exceptions in the Envoy
 * system utility.  <p>
 *
 * @version     1.0, (8/14/00)
 * @author      Marvin Lau, mlau@globalsight.com
 */
public class SystemUtilException
    extends GeneralException
{
    /**
     * Indicates an error in the system configuration with regard to
     * server object class names.
     */
    public static final int EX_SERVERCLASSNAMES       = 1001;

    /**
     * Indicates a failure in the creation of a server object.
     */
    public static final int EX_FAILEDTOCREATESERVER   = 1002;

    /**
     * Indicates a failure in the initialization of a server object
     */
    public static final int EX_FAILEDTOINITSERVER     = 1003;

    /**
     * Constructs an instance using the given exception identification.
     *
     * @param p_exceptionId Reason for the exception.
     */
    public SystemUtilException(int p_exceptionId)
    {
        super(COMP_ENVOYSYSTEM, p_exceptionId);
    }

    /**
     * Constructs an instance using the given exception id, and the
     * message id.<p>
     * @param p_exceptionId The id that specifies the exception.
     * @param p_messageId The id that specifies the explanation for
     * the exception.
     */
    public SystemUtilException(int p_exceptionId, int p_messageId)
    {
        super(COMP_ENVOYSYSTEM, p_exceptionId, p_messageId);
    }

    /**
     * Constructs an instance using the given exception id, and the
     * message id.  <p>
     * @param p_exceptionId The id that specifies the exception.
     * @param p_message The explanation for the exception.
     */
    public SystemUtilException(int p_exceptionId, String p_message)
    {
        super(COMP_ENVOYSYSTEM, p_exceptionId, p_message);
    }

    /**
     * Constructs an instance using the given exception id, and the
     * original exception.  <p>
     * @param p_exceptionId The id that specifies the exception.
     * @param p_originalException The original exception that this
     * exception identifies.
     */
    public SystemUtilException(int p_exceptionId, Exception p_originalException)
    {
        super(COMP_ENVOYSYSTEM, p_exceptionId, p_originalException);
    }

    /**
     * Constructs an instance using the exception id, and the original
     * exception.  <p>
     * @param p_exceptionId The id that specifies the exception.
     * @param p_messageId The id that specifies the explanation for
     * the exception.
     * @param p_originalException The original exception that this
     * exception identifies.
     */
    public SystemUtilException(int p_exceptionId, int p_messageId,
        Exception p_originalException)
    {
        super(COMP_ENVOYSYSTEM, p_exceptionId, p_messageId,
            p_originalException);
    }

    /**
     * Constructs an instance using the given component, exception
     * identification, message identification, and the original
     * exception.  NOTE: THIS CONSTRUCTOR IS USED FOR COMPOUND ERROR
     * MESSAGES.  <p>
     * @param p_exceptionId The id that specifies the exception.
     * @param p_messageId The id that specifies the explanation for
     * the exception.
     * @param p_messageArguments The message arguments in order.
     * @param p_originalException The original exception that this
     * exception edentifies.
     */
    public SystemUtilException(int p_exceptionId, int p_messageId,
        String[] p_messageArguments, Exception p_originalException)
    {
        super(COMP_ENVOYSYSTEM, p_exceptionId, p_messageId,
            p_messageArguments, p_originalException);
    }


    /**
     * <p>Constructs an instance using the given key in the error
     * message property file, the arguments to the message, the
     * Exception object and the property file name. This is used in
     * the sub-classes to explicitly designate the property file for
     * the message in the object.</p>
     *
     * @param p_messageKey key in properties file
     * @param p_messageArguments Arguments to the message. It can be
     * null.
     * @param p_originalException Original exception that caused the
     * error. It can be null.
     * @param p_propertyFileName Property file base name. If the
     * property file is LingMessage.properties, the parameter should
     * be "LingMessage".
     */
    protected SystemUtilException(String p_messageKey,
        String[] p_messageArguments, Exception p_originalException,
        String p_propertyFileName)
    {
        super(p_messageKey, p_messageArguments, p_originalException, p_propertyFileName);
    }
}


