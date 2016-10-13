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
 * Represents the details of a Task Queue Profile as contained in the Diplomat
 * database.  The Task Queue Profile provides the ability to connect to
 * multiple task queue tables, possibly in different databases.
 */
public class TaskQueueProfile
{
    //
    // PRIVATE MEMMBER VARIABLES
    //
    private long m_id;
    private String m_name;
    private String m_tableName;
    private long m_connId;
    private long m_rpp;
    private long m_ppb;
    private long m_millis;

    //
    // PUBLIC CONSTRUCTOR
    //
    /**
     * Creates a new task queue profile initialized with default values.
     */
    public TaskQueueProfile()
    {
        m_id = -1;
        m_name = "";
        m_tableName = "";
        m_connId = -1;
        m_rpp = -1;
        m_ppb =-1;
        m_millis = -1;
    }

    //
    // PUBLIC ACCESSORS
    //
    /**
     * Return the value of the id field.
     *
     * @return the current value.
     */
    public long getId()
    {
        return m_id;
    }

    /**
     * Set the value of the id field; this should only be done once.
     *
     * @param p_long the new value.
     */
    public void setId(long p_long)
    {
        m_id = p_long;
    }

    /**
     * Return the value of the name field.
     *
     * @return the current value.
     */
    public String getName()
    {
        return m_name;
    }

    /**
     * Set the value of the name field.
     *
     * @param p_str the new value.
     */
    public void setName(String p_str)
    {
        m_name = p_str;
    }

    /**
     * Return the value of the table name field.
     *
     * @return the current value.
     */
    public String getTableName()
    {
        return m_tableName;
    }

    /**
     * Set the value of the table name field.
     *
     * @param p_str the new value.
     */
    public void setTableName(String p_str)
    {
        m_tableName = p_str;
    }

    /**
     * Return the value of the connection id field.
     *
     * @return the current value.
     */
    public long getConnectionId()
    {
        return m_connId;
    }

    /**
     * Set the value of the connection id field.
     *
     * @param p_long the new value.
     */
    public void setConnectionId(long p_long)
    {
        m_connId = p_long;
    }

    /**
     * Return the value of the records per page field.
     *
     * @return the current value.
     */
    public long getRecordsPerPage()
    {
        return m_rpp;
    }

    /**
     * Set the value of the records per page field.
     *
     * @param p_long the new value.
     */
    public void setRecordsPerPage(long p_long)
    {
        m_rpp = p_long;
    }

    /**
     * Return the value of the pages per batch field.
     *
     * @return the current value.
     */
    public long getPagesPerBatch()
    {
        return m_ppb;
    }

    /**
     * Set the value of the pages per batch field.
     *
     * @param p_long the new value.
     */
    public void setPagesPerBatch(long p_long)
    {
        m_ppb = p_long;
    }

    /**
     * Return the value of the max elapsed milliseconds field.
     *
     * @return the current value.
     */
    public long getMaxElapsedMillis()
    {
        return m_millis;
    }

    /**
     * Set the value of the max elapsed milliseconds field.
     *
     * @param p_long the new value.
     */
    public void setMaxElapsedMillis(long p_long)
    {
        m_millis = p_long;
    }

    //
    // PUBLIC METHODS
    //
    /**
     * Return a detailed string representation of the task.
     *
     * @return a string describing the task.
     */
    public String detailString()
    {
        StringBuffer sb = new StringBuffer();
        sb.append("TaskQueueProfile [");
        sb.append("key=" + getId());
        sb.append(", name=" + getName());
        sb.append(", table=" + getTableName());
        sb.append(", rpp=" + getRecordsPerPage());
        sb.append(", ppb=" + getPagesPerBatch());
        sb.append("]");
        return sb.toString();
    }

    /**
     * Return a string representation of the task.
     *
     * @return a string describing the task.
     */
    public String toString()
    {
        return ("TaskQueueProfile [key=" + getId() + 
                ", name=" + getName() + "]");
    }
}
