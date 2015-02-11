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


/**
 * The class wraps exception information needed for the applet.  The
 * client can get the localized message and also get the whole exception
 * stack trace if the debug mode is enabled.
 */

public class ExceptionMessage implements java.io.Serializable
{
    private String m_message = null;
    private String m_stackTarce = null;
    private boolean m_isDebugMode = false;

    //////////////////////////////////////////////////////////////////////
    //  Begin: Constructor
    //////////////////////////////////////////////////////////////////////
    public ExceptionMessage(String p_message, String p_stackTrace, 
                            boolean p_isDebugMode)
    {
        super();
        m_message = p_message;
        m_stackTarce = p_stackTrace;
        m_isDebugMode = p_isDebugMode;
    }
    //////////////////////////////////////////////////////////////////////
    //  End: Constructor
    //////////////////////////////////////////////////////////////////////

    /**
     * Get the localized message to be displayed.
     * @return The localized message.
     */
    public String getMessage()
    {
        return m_message;
    }

    /**
     * Get the exception stack trace for debug mode.
     * @return The exception stack trace for debugging purposes.
     */
    public String getStackTrace()
    {
        return m_stackTarce;
    }

    /**
     * Determines whether the debug mode is enabled.
     * @return True if the debugging mode is enabled.  Otherwise return false.
     */
    public boolean isDebugMode()
    {
        return m_isDebugMode;
    }
}
