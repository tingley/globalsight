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

package com.globalsight.everest.webapp.javabean;

import com.globalsight.everest.servlet.EnvoyServletException;

/**
 * This bean class carries error information to error reporting JSP
 * page.
 */
public class ErrorBean
{
    private int m_type;
    private String m_message = "";
    private EnvoyServletException m_exception = null;

    public ErrorBean()
    {
    }

    public ErrorBean(int p_type, EnvoyServletException p_exception)
    {
        this(p_type, "", p_exception);
    }

    public ErrorBean(int p_type, String p_message)
    {
        this(p_type, p_message, null);
    }

    public ErrorBean(int p_type, String p_message,
        EnvoyServletException p_exception)
    {
        m_type = p_type;
        m_message = p_message;
        m_exception = p_exception;
    }

    public void setType(int p_type)
    {
        m_type = p_type;
    }

    public int getType()
    {
        return m_type;
    }

    public void setMessage(String p_message)
    {
        m_message = p_message;
    }

    public String getMessage()
    {
        return m_message;
    }

    public void setException(EnvoyServletException p_exception)
    {
        m_exception = p_exception;
    }

    public EnvoyServletException getException()
    {
        return m_exception;
    }

}



