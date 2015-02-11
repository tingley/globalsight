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

import java.util.Vector;


/**
 * A TaskList is a collection of tasks ordered chronologically.  This is a
 * support class used only by the class TaskClassifer; each listcontains only
 * tasks with the same localization profile id.
 */
public class TaskList
    implements Serializable
{
    //
    // PRIVATE MEMBER VARIABLES
    //
    private Vector m_autoTasks;
    private Vector m_manualTasks;
    private long m_autoUpdateTime;
    private long m_manualUpdateTime;

    //
    // DEFAULT CONSTRUCTOR
    //
    /**
     * Create an initialized instance of the class.
     */
    public TaskList()
    {
        super();
        m_autoTasks = new Vector();
        m_manualTasks = new Vector();
        m_autoUpdateTime = m_manualUpdateTime = System.currentTimeMillis();
    }

    //
    // PUBLIC METHODS
    //
    /**
     * Return the time in milliseconds since the automatic tasks were last
     * updated.
     *
     * @return current time.
     */
    public long autoAgeInMillis()
    {
        return (System.currentTimeMillis() - m_autoUpdateTime);
    }

    /**
     * Return the time in milliseconds since the manual tasks were last
     * updated.
     *
     * @return current time.
     */
    public long manualAgeInMillis()
    {
        return (System.currentTimeMillis() - m_manualUpdateTime);
    }

    /**
     * Add the given task to the list, assuming it does not already
     * exist.  This ensures that the task contains only unique
     * task objects.
     *
     * @param p_task the task to be added.
     */
    public void addTask(Task p_task)
    {
        addTask(p_task.isManualMode() ? m_manualTasks : m_autoTasks, p_task);
    }

    /**
     * Remove the given task from the list, if it exists.
     *
     * @param p_task the task to be removed.
     */
    public void removeTask(Task p_task)
    {
        removeTask(p_task.isManualMode() ? m_manualTasks : m_autoTasks, p_task);
    }

    /**
     * Return a vector containing all automatic tasks on the list.
     *
     * @return the current task which are automatic.
     */
    public Vector automaticTasks()
    {
        return m_autoTasks;
    }

    /**
     * Return a vector containing all manual tasks on the list.
     *
     * @return the current task which are manual.
     */
    public Vector manualTasks()
    {
        return m_manualTasks;
    }

    /**
     * Return the number of tasks in the list.
     *
     * @return the current number of tasks.
     */
    public int size()
    {
        return m_autoTasks.size() + m_manualTasks.size();
    }

    /**
     * Return a detailed string representation of the tasklist.
     *
     * @return a description of the tasklist.
     */
    public String detailString()
    {
        int total = size();
        StringBuffer sb = new StringBuffer();
        long latest = Math.max(m_autoUpdateTime, m_manualUpdateTime);

        sb.append("TaskList, (time=" + latest + ", ");
        sb.append(total);
        sb.append(" task");
        sb.append(optionalS(total));
        sb.append(")");

        if (total > 0)
        {
            sb.append(" [");
            append(sb, m_autoTasks, "Auto");
            sb.append(", ");
            append(sb, m_manualTasks, "Manual");
            sb.append("]");
        }
        return sb.toString();
    }

    /**
     * Return a string representation of the tasklist.
     *
     * @return a description of the tasklist.
     */
    public String toString()
    {
        int size = size();
        return ("TaskList (" + size + " task" + optionalS(size) + ")");
    }

    /**
     * Return true if the receiver and the given object are the same --
     * i.e. they are and instance of the same class and they contain the
     * same tasks.
     *
     * @return true if the objects are the same; false otherwise.
     */
    public boolean isSameAs(Object anObject)
    {
        boolean isSame = false;
        TaskList t = null;
        try
        {
            t = (TaskList)anObject;
            isSame = vectorsMatch(automaticTasks(), t.automaticTasks()) &&
                vectorsMatch(manualTasks(), t.manualTasks());
        }
        catch (ClassCastException e)
        {
            e.printStackTrace();
        }
        return isSame;
    }

    /**
     * Return true if the receiver and the given object are identical, or
     * if they contain the same tasks.
     *
     * @return true if the objects are the same; false otherwise.
     */
    public boolean equals(Object anObject)
    {
        return (super.equals(anObject) || isSameAs(anObject));
    }

    /**
     * Return true if the receiver contains the given task.
     *
     * @param p_task the task to check
     *
     * @return true if p_task is in the list; false otherwise.
     */
    public boolean contains(Task p_task)
    {
        return m_autoTasks.contains(p_task) || m_manualTasks.contains(p_task);
    }

    /**
     * Reset the time for the automatic task list.
     */
    public void resetAutoTime()
    {
        m_autoUpdateTime = System.currentTimeMillis();
    }

    /**
     * Reset the time for the manual task list.
     */
    public void resetManualTime()
    {
        m_manualUpdateTime = System.currentTimeMillis();
    }
    
    /* Return true if the vectors have the same size and both contain the */
    /* elements in any order. */
    private boolean vectorsMatch(Vector p_v1, Vector p_v2)
    {
        return ((p_v1.size() == p_v2.size()) && p_v1.containsAll(p_v2));
    }

    /* Add the given task to the given vector, if it does not already exist. */
    private void addTask(Vector v, Task p_task)
    {
        if (!v.contains(p_task))
        {
            v.addElement(p_task);
        }
    }
    
    /* Remove the given task from the given vector, if exists. */
    private void removeTask(Vector v, Task p_task)
    {
        v.removeElement(p_task);
    }

    /* Append the contents of the given vector to the buffer. */
    private void append(StringBuffer p_sb, Vector p_v, String p_str)
    {
        p_sb.append(p_str + "[");
        for (int i = 0 ; i < p_v.size() ; i++)
        {
            p_sb.append(((Task)p_v.elementAt(i)).detailString());
            if (i < p_v.size() - 1)
            {
                p_sb.append(", ");
            }
        }
        p_sb.append("]");
    }
    
    /* Return "s" if the given integer is not equal to 1. */
    private String optionalS(int p_int)
    {
        return (p_int == 1 ? "" : "s");
    }
}
