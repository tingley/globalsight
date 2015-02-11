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
package com.globalsight.cxe.adapter.database.source;

import java.io.Serializable;

/**
 * Represents the details of a Task as contained in the TaskQueue database
 * table.  A Task has a unique id of its own, and it contains the id of a
 * localization profile, as well as the id of a record progile.  In addition,
 * it contains a parameter string which is parsed and converted into zero
 * or more run-time parameters that will be substituted into the acquisition
 * SQL.
 */
public class Task
    implements Serializable
{
    private long m_taskId;
    private long m_locProfId;
    private long m_recProfId;
    private String m_srcLang;
    private String m_charset;
    private String m_parameterString;
    private boolean m_isManualMode;

    //
    // PUBLIC CONSTRUCTOR
    //
    /**
     * Creates a new task initialized with default values.  The Task isn't
     * considered "valid" until it has been re-populated with actual data
     * from the database.
     */
    public Task()
    {
        m_taskId = -1;
        m_locProfId = -1;
        m_recProfId = -1;
        m_srcLang = "";
        m_charset = "";
        m_parameterString = "";
        m_isManualMode = false;
    }

    /**
     * Return the task id.
     *
     * @return the value of the task id.
     */
    public long getTaskId()
    {
        return m_taskId;
    }

    /**
     * Set the task id; this should only be done once.
     *
     * @param p_id the new value to be used.
     */
    public void setTaskId(long p_id)
    {
        m_taskId = p_id;
    }

    /**
     * Return the localization profile id.
     *
     * @return the value of the localization profile id.
     */
    public long getLocalizationProfileId()
    {
        return m_locProfId;
    }

    /**
     * Set the localization profile id.
     *
    * @param p_id the new value to be used.
     */
    public void setLocalizationProfileId(long p_id)
    {
        m_locProfId = p_id;
    }

    /**
     * Return the record profile id.
     *
     * @return the value of the record profile id.
     */
    public long getRecordProfileId()
    {
        return m_recProfId;
    }

    /**
     * Set the record profile id.
     *
    * @param p_id the new value to be used.
     */
    public void setRecordProfileId(long p_id)
    {
        m_recProfId = p_id;
    }

    /**
     * Return the source language string.
     *
     * @return the value of the source language string.
     */
    public String getSourceLanguage()
    {
        return m_srcLang;
    }

    /**
     * Set the source language string.
     *
     * @param p_string the new value to be used.
     */
    public void setSourceLanguage(String p_string)
    {
        m_srcLang = p_string;
    }

    /**
     * Return the charset string.
     *
     * @return the value of the charset string.
     */
    public String getCharset()
    {
        return m_charset;
    }

    /**
     * Set the charset string.
     *
     * @param p_string the new value to be used.
     */
    public void setCharset(String p_string)
    {
        m_charset = p_string;
    }

    /**
     * Return the parameter string.
     *
     * @return the value of the parameter string.
     */
    public String getParameterString()
    {
        return m_parameterString;
    }

    /**
     * Set the parameter string.
     *
     * @param p_string the new value to be used.
     */
    public void setParameterString(String p_string)
    {
        m_parameterString = p_string;
    }

    /**
     * Return true if the task should be handled in manual mode, false if
     * automatic.
     *
     * @return the value of the flag.
     */
    public boolean isManualMode()
    {
        return m_isManualMode;
    }

    /**
     * Set the value of the manual mode flag to the given value.
     *
     * @param p_bool the new value to be used.
     */
    public void setManualMode(boolean p_bool)
    {
        m_isManualMode = p_bool;
    }

    /**
     * Return a detailed string representation of the task.
     *
     * @return a string describing the task.
     */
    public String detailString()
    {
        StringBuffer sb = new StringBuffer();
        sb.append("Task [");
        sb.append("key=" + getTaskId());
        sb.append(", " + (isManualMode() ? "Manual" : "Automatic") + " mode");
        sb.append(", lpid=" + getLocalizationProfileId());
        sb.append(", rpid=" + getRecordProfileId());
        sb.append(", lang=\"" + getSourceLanguage() + "\"");
        sb.append(", parm=\"" + getParameterString() + "\"]");
        return sb.toString();
    }

    /**
     * Return a string representation of the task.
     *
     * @return a string describing the task.
     */
    public String toString()
    {
        return ("Task [key=" + getTaskId() + "]");
    }
    
    /**
     * Return true if the receiver and the given object are the same --
     * i.e. they are an instance of the same class and they have the same
     * task id.
     *
     * @return true if the objects are the same; false otherwise.
     */
    public boolean isSameAs(Object p_object)
    {
        boolean isSame = false;
        try
        {
            isSame = (getTaskId() == ((Task)p_object).getTaskId());
        }
        catch (ClassCastException e)
        {
        }
        return isSame;
    }

    /**
     * Return true if the receiver and the given object are identical, or
     * if they have the same task id.
     *
     * @return true if the objects are the same; false otherwise.
     */
    public boolean equals(Object p_object)
    {
        return (this == p_object || isSameAs(p_object));
    }
}
