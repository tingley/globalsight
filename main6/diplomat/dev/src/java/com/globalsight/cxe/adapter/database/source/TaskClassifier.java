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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Vector;

/**
 * A TaskClassifier distributes tasks into the correct task lists, according
 * to the localization profile id of each task.
 * <p>
 * This is a package scope utility class used only by the public class
 * TaskQueueTableMonitor.
 */
public class TaskClassifier
    implements Serializable
{
    private HashMap m_map;

    //
    // PUBLIC CONSTRUCTOR
    //
    public TaskClassifier()
    {
        m_map = new HashMap();
    }

    /**
     * Add the given task to the appropriate task list, assuming it does not
     * already exist.  This ensures that each task list contains only unique
     * task objects.
     *
     * @param p_task the task to be added.
     */
    public void addTask(Task p_task)
    {
        Long lpid = new Long(p_task.getLocalizationProfileId());
        TaskList tl = (TaskList)m_map.get(lpid);

        if (tl == null)
        {
            tl = new TaskList();
            m_map.put(lpid, tl);
        }
        tl.addTask(p_task);
    }

    /**
     * Add all the tasks in the given vector to the appropriate task list.
     *
     * @param p_vector the vector of tasks to be added.
     */
    public void addTasks(Vector p_vector)
    {
        for (int i = 0 ; i < p_vector.size() ; i++)
        {
            addTask((Task)p_vector.elementAt(i));
        }
    }

    /**
     * Remove the given task from the appropriate task list, if it exists.
     *
     * @param p_task the task to be removed.
     */
    public void removeTask(Task p_task)
    {
        Long lpid = new Long(p_task.getLocalizationProfileId());
        TaskList tl = (TaskList)m_map.get(lpid);

        if (tl != null)
        {
            tl.removeTask(p_task);
            if (tl.size() == 0)
            {
                m_map.remove(lpid);
            }
        }
    }

    /**
     * Return a vector containing all the task lists on the receiver.
     *
     * @return all currently defined task lists.
     */
    public Vector allTaskLists()
    {
        Vector v = new Vector();
        Iterator it = m_map.entrySet().iterator();
        while (it.hasNext())
        {
            Entry e = (Entry)it.next();
            v.addElement((TaskList)e.getValue());
        }
        return v;
    }

    /**
     * Return the total number of tasks in the entire task classifier.
     *
     * @return the current number of tasks.
     */
    public int size()
    {
        int total = 0;
        Vector v = allTaskLists();
        for (int i = 0 ; i < v.size() ; i++)
        {
            total += (((TaskList)v.elementAt(i)).size());
        }
        return total;
    }

    /**
     * Return a task classifier containing only those tasks that must be
     * processed.
     * <p>
     * The task classifier that is returned could have any number of tasks on
     * it, depending on the current state of the receiver compared to the given
     * arguments.  If the number of tasks in any task list exceeds the product
     * of p_pagesPerBatch times p_recordsPerPage, or if the elapsed time since
     * the last time the task list was updated exceeds the given max elapsed
     * time argument, then the tasks in that task list will be moved onto the
     * task classifier that is returned to the caller.
     * <p>
     * Tasks are actually moved onto the new task classifier, which means that
     * they are "removed" from the receiver.
     *
     * @param p_recordsPerPage the number of records to be put on a single page
     * @param p_pagesPerBatch the number of pages assigned to one batch
     * @param p_maxElapsedTime the maximum number of milliseconds allowed to
     * elapse before all records on a task list must be processed.
     * @param p_maxBatches the maximum number of batches to process in a single
     * pass (for each task list)
     *
     * @return a new TaskClassifier containing all the tasks that must be
     * processed.
     * 
     */
    public TaskClassifier tasksToBeProcessed(
        long p_recordsPerPage,
        long p_pagesPerBatch,
        long p_maxElapsedTime,
        int p_maxBatches)
    {
        int batchSize = (int)p_recordsPerPage * (int)p_pagesPerBatch;

        TaskClassifier newTc = new TaskClassifier();

        Vector v = allTaskLists();
        for (int i = 0 ; i < v.size() ; i++)
        {
            TaskList tl = (TaskList)v.elementAt(i);
            transferTasks(this, newTc, tl, batchSize, p_maxElapsedTime, p_maxBatches);
        }
        return newTc;
    }

    /* Transfer tasks contained in the task list from one task classifier */
    /* to the other.  Transfer no more than p_maxRecs records of each type, */
    /* unless the age of the internal vector is older than p_maxAge, in */
    /* which case, transfer all records of the internal type. */
    private void transferTasks(TaskClassifier p_from, TaskClassifier p_to,
                               TaskList p_taskList, int p_batchSize, long p_maxAge,
                               int p_maxBatches)
    {
        Vector v = p_taskList.automaticTasks();
        long age = p_taskList.autoAgeInMillis();
        transferTasks(p_from, p_to, v, p_batchSize, p_maxAge, age, p_maxBatches);
        if (v.size() == 0)
        {
            p_taskList.resetAutoTime();
        }

        v = p_taskList.manualTasks();
        age = p_taskList.manualAgeInMillis();
        transferTasks(p_from, p_to, v, p_batchSize, p_maxAge, age, p_maxBatches);
        if (v.size() == 0)
        {
            p_taskList.resetManualTime();
        }
    }

    /* Transfer tasks contained in the given vector from one task classifier */
    /* to the other, using the batchSize, maxAge, age, & maxBatches qualifiers. */
    private void transferTasks(TaskClassifier p_from, TaskClassifier p_to, Vector p_vec,
                               int p_batchSize, long p_maxAge, long p_age, int p_maxBatches)
    {
        int size = p_vec.size();
        int maxSize = p_batchSize * p_maxBatches;
        int howMany = 0;
        if (p_age > p_maxAge)
        {
            howMany = Math.min(size, maxSize);
        }
        else if (size > p_batchSize)
        {
            howMany = p_batchSize * (size / p_batchSize);
        }

        for (int i = 0 ; i < howMany ; i++)
        {
            Task t = (Task)p_vec.elementAt(0);
            p_from.removeTask(t);
            p_to.addTask(t);
        }
    }
    
    /**
     * Return all the tasks in the classifier as a denormalized vector.
     *
     * @return a vector containing all the tasks.
     */
    public Vector allTasks()
    {
        Vector v = new Vector();
        Vector lists = allTaskLists();
        for (int i = 0 ; i < lists.size() ; i++)
        {
            TaskList tl = (TaskList)lists.elementAt(i);
            v.addAll(tl.automaticTasks());
            v.addAll(tl.manualTasks());
        }
        return v;
    }

    /**
     * Return a detailed string representation of the tasklist.
     *
     * @return a description of the tasklist.
     */
    public String detailString()
    {
        int total = size();
        Iterator it = m_map.keySet().iterator();
        StringBuffer sb = new StringBuffer();

        sb.append("TaskClassifier (" + total + " task");
        sb.append(optionalS(total));
        sb.append(")");
        sb.append(total > 0 ? " [" : "");

        while (it.hasNext())
        {
            Long lpid = (Long)it.next();
            TaskList tl = (TaskList)m_map.get(lpid);
            sb.append("LPID=" + lpid + " [");
            sb.append(tl.detailString());
            sb.append("]");
            if (it.hasNext())
            {
                sb.append(", ");
            }
        }
        sb.append(total > 0 ? "]" : "");
        return sb.toString();
    }
    
    /**
     * Return a string representation of the tasklist.
     *
     * @return a description of the tasklist.
     */
    public String toString()
    {
        int total = size();
        return ("TaskClassifier (" + total + " task" + optionalS(total) + ")");
    }

    /**
     * Return true if the receiver and the given object are the same --
     * i.e. they are and instance of the same class and they contain the
     * same tasks.
     *
     * @return true if the objects are the same; false otherwise.
     */
    public boolean isSameAs(Object p_object)
    {
        boolean isSame = false;
        try
        {
            TaskClassifier tc = (TaskClassifier)p_object;
            Vector v1 = allTasks();
            Vector v2 = tc.allTasks();
            isSame = (v1.size() == v2.size() && v1.containsAll(v2));
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
    public boolean equals(Object p_object)
    {
        return (super.equals(p_object) || isSameAs(p_object));
    }

    private String optionalS(int p_int)
    {
        return (p_int == 1 ? "" : "s");
    }
}
