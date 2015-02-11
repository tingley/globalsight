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

import com.globalsight.diplomat.util.Logger;

import com.globalsight.diplomat.util.database.DbAccessException;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

/**
 * Monitors the changing state of task queue tables in the client's
 * database.  When told, it reads the contents of each table and organizes
 * the individual tasks according to the criteria of the appropriate task-
 * classifier
 * <p>
 * Basic logic is as follows:<p>
 *   1.  Read all the available task queue profiles.<br>
 *   2.  For each task queue profile, read the corresponding TaskClassifier.<br>
 *   3.  For each task queue profile<br>
 *       1. read the actual task queue<br>
 *       2. classify all tasks<br>
 *       3. save the task classifier<br>
 *       4. delete the tasks from the task queue.<br>
 *   4.  Construct a vector of TaskXml objects for each task classifier:<br>
 *       1. Construct batches of TaskXml and add them to the vector.<br>
 *       2. Remove the tasks from the classifier<br>
 *       3. Re-save the classifier<br>
 *   5.  Return the vector of TaskXml objects to the caller.<br>
 *   6.  Wait for the next call.
 */
public class TaskQueueTableMonitor
{
    //
    // PRIVATE CONSTANTS
    //
    private static final int MAX_TASKS = 100;

    //
    // PRIVATE MEMBER VARIABLES
    //
    private transient HashMap m_taskClassifiers;
    private transient TaskXmlBuilder m_txBuilder;
    private transient Logger m_logger;

    //
    // PUBLIC CONSTRUCTORS
    //
    public TaskQueueTableMonitor()
    {
        super();
        m_txBuilder = new TaskXmlBuilder();
        m_logger = Logger.getLogger();
    }

    //
    // PUBLIC ACCESSORS
    //
    /**
     * Read the database tables and return the vector of task xml objects
     * corresponding to the current set of tasks that need to be processed.
     * After the xml has been generated, save the task classifiers to ensure
     * that the xml won't be generated again.
     *
     * @return a vector of task xml objects (may be empty)
     */                                                   
    public Vector readDatabase()
    throws TaskQueueException
    {
        loadTaskClassifiers();
        Vector v = taskXmlVector();
        saveTaskClassifiers();
        return v;
    }

    /* For each task queue profile in the database, load the corresponding */
    /* task classifier; populate it with all unprocessed tasks in the task */
    /* queue.  Re-save the task classifier and remove the tasks from the */
    /* task queue. */
    private void loadTaskClassifiers()
    throws TaskQueueException
    {
        m_taskClassifiers = new HashMap();
        try
        {
            Vector profiles = TaskQueueProfileDbAccessor.readAllTaskQueueProfiles();
            for (int i = 0 ; i < profiles.size() ; i++)
            {
                TaskQueueProfile tqp = (TaskQueueProfile)profiles.elementAt(i);
                TaskClassifier tc = TaskClassifierDbAccessor.readTaskClassifier(tqp.getId());
                m_taskClassifiers.put(tqp, tc);
                try
                {
                    Vector tasks = TaskDbAccessor.readTasks(tqp);
                    m_logger.println(Logger.DEBUG_D,
                                     "TaskQueueProfile, id=" + tqp.getId() +
                                     ": got " + tasks.size() + " tasks");
                    if (tasks.size() > 0)
                    {
                        m_logger.println(Logger.DEBUG_D, "classifying tasks, saving classifier, deleting tasks");
                        tc.addTasks(tasks);
                        saveTaskClassifier(tc, tqp);
                        TaskDbAccessor.removeTasks(tqp);
                    }
                    m_logger.println(Logger.DEBUG_D, "done with TQP #" + i);
                }
                catch (Exception e)
                {
                    // we basically just log this exception and continue with
                    // the rest of the task queues
                    StringBuffer sb = new StringBuffer();
                    sb.append("TaskQueueTableMonitor.loadTaskClassifiers(): ");
                    sb.append("Unable to read task queue #" + i);
                    sb.append(", " + tqp.detailString() + ": ");
                    m_logger.printStackTrace(Logger.ERROR, sb.toString(), e);
                }
            }
        }
        catch (Exception e)
        {
            throw new TaskQueueException("Unable to access task queue tables", e);
        }
    }

    /* Save all the task classifiers out to the database. */
    private void saveTaskClassifiers()
    throws TaskQueueException
    {
        Iterator it = m_taskClassifiers.keySet().iterator();
        while (it.hasNext())
        {
            TaskQueueProfile tqp = (TaskQueueProfile)it.next();
            TaskClassifier tc = (TaskClassifier)m_taskClassifiers.get(tqp);
            try
            {
                saveTaskClassifier(tc, tqp);
            }
            catch (Exception e)
            {
                throw new TaskQueueException("Unable to save task classifier", e);
            }
        }

    }

    /* Save the given task classifier. */
    private void saveTaskClassifier(TaskClassifier p_tc, TaskQueueProfile p_tqp)
    throws DbAccessException
    {
        TaskClassifierDbAccessor.saveTaskClassifier(p_tc, p_tqp.getId());
    }

    //
    // PRIVATE SUPPORT METHODS
    //
    /* For each task classifier, determine which tasks need to be processed, */
    /* and construct xml for all of them.  Process no more than the maximum */
    /* number of batches specified (per task list). */
    private Vector taskXmlVector()
    throws TaskQueueException
    {
        Vector xmlVec = new Vector();
        Iterator it = m_taskClassifiers.keySet().iterator();
        while (it.hasNext())
        {
            TaskQueueProfile tqp = (TaskQueueProfile)it.next();
            TaskClassifier tc = (TaskClassifier)m_taskClassifiers.get(tqp);
            int rpp = (int)tqp.getRecordsPerPage();
            int ppb = (int)tqp.getPagesPerBatch();
            int maxBatches = MAX_TASKS / (rpp * ppb);
            if (maxBatches < 1)
            {
                maxBatches = 1;
            }
            long millis = tqp.getMaxElapsedMillis();
            m_logger.println(Logger.DEBUG_D, "calling tc.tasksToBeProcessed(rpp=" + rpp +
                             ", ppb=" + ppb + ", millis=" + millis + 
                             ", maxBatches=" + maxBatches + ")");
            TaskClassifier toBeProcessed = tc.tasksToBeProcessed(rpp, ppb, millis, maxBatches);
            try
            {
                m_logger.println(Logger.DEBUG_D, "calling txBuilder.createXmlFor()");
                xmlVec.addAll(m_txBuilder.createXmlFor(toBeProcessed, rpp, ppb));
            }
            catch (Exception e)
            {
                throw new TaskQueueException("Unable to create XML", e);
            }
        }
        return xmlVec;
    }
}

