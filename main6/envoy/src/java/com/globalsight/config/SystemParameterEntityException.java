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
package com.globalsight.config;

import com.globalsight.util.GeneralException;

/**
 * Exception class for System Parameter entity.
 */
public class SystemParameterEntityException
    extends GeneralException
{
    private static final long serialVersionUID = 1928428640695166279L;

    public final static String PROPERTY_FILE_NAME = "SystemParameterEntityException";

    // message keys

    // Args 1:  the listener name that failed
    // Args 2:  the system parameter that is changed and the listener notified about.
    // Args 3:  the new system parameter value that failed to be processed by the listener.
    public final static String MSG_LISTENER_FAILED = "ListenerFailedToProcessChange";


    /**
     * Constructor that creates a exception with specifiec message string
     *
     * @param p_msg Exception message
     */
    public SystemParameterEntityException(String p_msg)
    {
        super(COMP_PERSISTENCE, EX_SQL, p_msg);
    }

    /**
     * Constructor that wraps an existing exception with
     * SystemParameterEntityException.
     *
     * @param p_exception Original exception object.
     */
    public SystemParameterEntityException(Exception p_exception)
    {
        super(COMP_PERSISTENCE, EX_SQL, p_exception);
    }

    /**
     * Constructor that wraps an existing exception with
     * SystemParameterEntityException.
     *
     * @param p_msg Exception message
     * @param p_exception Original exception object.
     */
    public SystemParameterEntityException(String p_msg,
            Exception p_exception)
    {
        super(p_msg, p_exception);
    }

    /**
     * Constructs a SystemParameter exception using a message key, arguments and
     * the property file where the actual message text can be found.
     */
    public SystemParameterEntityException(String p_messageKey, String[] p_messageArguments,
                                          Exception p_originalException)
    {
        super(p_messageKey, p_messageArguments,
            p_originalException, PROPERTY_FILE_NAME);
    }
}
