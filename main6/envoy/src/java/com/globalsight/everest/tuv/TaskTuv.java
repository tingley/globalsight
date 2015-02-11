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

import java.io.Serializable;

import com.globalsight.everest.persistence.PersistentObject;
import com.globalsight.everest.persistence.tuv.SegmentTuvUtil;
import com.globalsight.everest.taskmanager.Task;

/**
 * Represents the association of a workflow Task with a Tuv at the time the task
 * completed. It is keyed by the current TuvId.
 * <p>
 * At the completion of a task, a copy of each Tuv is made with new ids. These
 * are added to the TUV tables and linked by TaskTuv.
 */
public class TaskTuv extends PersistentObject implements Serializable
{

    private static final long serialVersionUID = 1L;

    /**
     * Constants used for TopLink's query. The constant value has to be exactly
     * the same as the variable defined in the mapping.
     */
    public static final String ID = PersistentObject.M_ID;
    public static final String VERSION = "m_version";
    public static final String CURRENT_TUV = "m_currentTuv";
    public static final String PREVIOUS_TUV = "m_previousTuv";
    public static final String TASK = "m_task";
    public static final String TASKNAME = "m_taskName";

    private int m_version = -1;
    private Tuv m_currentTuv = null;
    private Tuv m_previousTuv = null;
    private Task m_task = null;
    private String m_taskName = "UNKNOWN";

    private long m_taskId;
    private long m_currentTuvId;
    private long m_previousTuvId;

    /**
     * Public no-args constructor for TopLink.
     */
    public TaskTuv()
    {
    }

    /**
     * Return the Task associated with the final version of the Tuv for that
     * task.
     * 
     * @return the Task.
     */
    public Task getTask()
    {
        return m_task;
    }

    /**
     * Return the final version of the Tuv for the task.
     * 
     * @return the final Tuv for the task.
     */
    public Tuv getTuv(long p_jobId)
    {
        return getPreviousTuv(p_jobId);
    }

    public void setTaskId(long p_taskId)
    {
        m_taskId = p_taskId;
    }

    public long getTaskId()
    {
        return m_taskId;
    }

    public void setCurrentTuvId(long p_currentTuvId)
    {
        m_currentTuvId = p_currentTuvId;
    }

    public long getCurrentTuvId()
    {
        return m_currentTuvId;
    }

    public void setVersion(int p_version)
    {
        m_version = p_version;
    }

    public void setPreviousTuvId(long p_previousTuvId)
    {
        m_previousTuvId = p_previousTuvId;
    }

    public void setPreviousTuv(Tuv p_previousTuv)
    {
        m_previousTuvId = p_previousTuv.getId();
        m_previousTuv = p_previousTuv;
        if (m_previousTuv != null)
        {
            m_previousTuvId = m_previousTuv.getId();
        }

    }

    public long getPreviousTuvId()
    {
        return m_previousTuvId;
    }

    public Tuv getPreviousTuv(long p_jobId)
    {
        if (m_previousTuv == null && m_previousTuvId > 0 && p_jobId > 0)
        {
            try
            {
                m_previousTuv = SegmentTuvUtil.getTuvById(m_previousTuvId,
                        p_jobId);
            }
            catch (Exception e)
            {
            }
        }
        return m_previousTuv;
    }

    public void setTaskName(String p_name)
    {
        m_taskName = p_name;
    }

    public String getTaskName()
    {
        return m_taskName;
    }

    /*
     * This method is overwritten for TOPLink. TOPLink doesn't query all
     * collections of objects within an object. So if a TaskTuv is serialized -
     * the mapped objects may not be available (because they haven't been
     * queried yet). Overwriting the method forces the query to happen so when
     * it is serialized all pieces of the object are serialized and availble to
     * the client.
     */
    protected void writeObject(java.io.ObjectOutputStream out)
            throws java.io.IOException
    {
        // touch all the mapped objects
        // if (m_currentTuv != null)
        // {
        // m_currentTuv.getId();
        // }
        // if (m_previousTuv != null)
        // {
        // m_previousTuv.getId();
        // }
        if (m_task != null)
        {
            m_task.getId();
        }

        // call the default writeObject
        out.defaultWriteObject();
    }

    /**
     * Return a string representation of the object.
     * 
     * @return a string representation of the object.
     */
    public String toString()
    {
        return super.toString() + " m_currentTuv=" + m_currentTuv
                + " m_previousTuv=" + m_previousTuv + " m_task=" + m_task;
    }

    //
    // PACKAGE METHODS
    //

    public TaskTuv(Tuv p_currentTuv, Tuv p_previousTuv, int p_version,
            Task p_task)
    {
        setCurrentTuv(p_currentTuv);
        setPreviousTuv(p_previousTuv);
        m_version = p_version;
        m_task = p_task;

        m_taskName = p_task.getTaskName();
    }

    public int getVersion()
    {
        return m_version;
    }

    public void setCurrentTuv(Tuv tuv)
    {
        m_currentTuvId = tuv.getId();
        m_currentTuv = tuv;
        if (m_currentTuv != null)
        {
            m_currentTuvId = m_currentTuv.getId();
        }

    }

    public void setTask(Task m_task)
    {
        this.m_task = m_task;
        if (m_task != null)
        {
            m_taskId = m_task.getId();
        }

    }
}
