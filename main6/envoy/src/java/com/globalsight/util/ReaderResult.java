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

package com.globalsight.util;

/**
 * A data class that holds an entry from an exported database together
 * with a status code and error message if the entry/object could not
 * be read successfully.
 */
public class ReaderResult
{
    //
    // Constants
    //
    static public final int OK = 0;
    static public final int ERROR = 1;

    //
    // Private Members
    //
    private int m_status = OK;
    private String m_message;
    private Object m_object;    // the entry/object that was read in

    //
    // Constructor
    //
    public ReaderResult()
    {
    }


    public void clear()
    {
        m_status = OK;
        m_message = null;
        m_object = null;
    }

    //
    // Public Methods
    //
    public void setResultObject(Object p_obj)
    {
        m_status = OK;
        m_object = p_obj;
    }

    public void setError(String p_message)
    {
        m_status = ERROR;
        m_message = p_message;
    }

    public int getStatus()
    {
        return m_status;
    }

    public String getMessage()
    {
        return m_message;
    }

    public Object getResultObject()
    {
        return m_object;
    }
}
