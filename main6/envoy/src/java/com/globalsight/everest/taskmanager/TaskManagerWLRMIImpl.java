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
package com.globalsight.everest.taskmanager;

// globalsight
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.globalsight.everest.util.system.RemoteServer;

/**
 * This class represents the remote implementation of a task manager that
 * manages tasks.
 */
public class TaskManagerWLRMIImpl extends RemoteServer implements
        TaskManagerWLRemote
{

    TaskManager m_localInstance = null;;

    // /////////////////////////////////////////////////////////////////////
    // Begin: Constructor
    // /////////////////////////////////////////////////////////////////////
    /**
     * Construct a remote Task Manager.
     * 
     * @exception java.rmi.RemoteException
     *                Network related exception.
     */
    public TaskManagerWLRMIImpl() throws TaskException, RemoteException
    {
        super(TaskManager.SERVICE_NAME);
        m_localInstance = new TaskManagerLocal();
    }

    // /////////////////////////////////////////////////////////////////////
    // Begin: TaskManager Implementation
    // /////////////////////////////////////////////////////////////////////

    /**
     * To accept a Task. It invokes Iflow to accept a task, also saved accepted
     * time to database task_info table.
     * 
     * @param p_userId
     *            the Id of the user who accepts the task.
     * @param p_task
     *            The task to be accepted.
     * @param isSkipped
     *            If this task is being skipped.
     * 
     * @throws RemoteException
     *             , TaskException
     */
    public void acceptTask(String p_userId, Task p_task, boolean isSkipped)
            throws RemoteException, TaskException
    {
        m_localInstance.acceptTask(p_userId, p_task, isSkipped);
    }

    /**
     * @see TaskManagerLocal.acceptTask(String, Task, Map<String, Object>)
     */
    public void acceptTask(String p_userId, Task p_task,
            Map<String, Object> data) throws RemoteException, TaskException
    {
        m_localInstance.acceptTask(p_userId, p_task, data);
    }

    /**
     * To complete a Task. It invokes Iflow to complete a task, also saved
     * completed time to database task_info table.
     * 
     * @param p_sessionId
     *            The client's http session id used for getting WFSession
     *            object.
     * @param p_userId
     *            the Id of the user who completes the task.
     * @param p_task
     *            The task to be completed.
     * @param p_destinationArrow
     *            - The name of the outgoing arrow of a condition node (if the
     *            next node of this task is a condition node). This is used for
     *            making decision.
     * @param skipping
     *            Indicates this task is being skipped.
     * 
     * @throws RemoteException
     *             , TaskException
     */
    public void completeTask(String p_userId, Task p_task,
            String p_destinationArrow, String skipping) throws RemoteException,
            TaskException
    {
        m_localInstance.completeTask(p_userId, p_task, p_destinationArrow,
                skipping);
    }

    /**
     * To reject a Task. It invokes Iflow to reject a task, also saves reject
     * comment to database task_comment table.
     * 
     * @param p_userId
     *            the Id of the user who rejects the task.
     * @param p_task
     *            The task to be rejected.
     * @param p_rejectComment
     *            the reject comment.
     * 
     * @throws RemoteException
     *             , TaskException
     */
    public void rejectTask(String p_userId, Task p_task, String p_rejectComment)
            throws RemoteException, TaskException
    {
        m_localInstance.rejectTask(p_userId, p_task, p_rejectComment);
    }

    /**
     * @see TaskManager.getCurrentTasks(long)
     */
    public Collection getCurrentTasks(long p_workflowId)
            throws RemoteException, TaskException
    {
        return m_localInstance.getCurrentTasks(p_workflowId);
    }

    /**
     * To retrieve a Task by Id for the given user.
     * 
     * @param p_userId
     *            the Id of the user.
     * @param p_taskId
     *            The id of the target task.
     * @param p_state
     *            - The state of the task to be retrieved. Since a list of
     *            activities are displayed based on their state, it's important
     *            to pass the state in order to get the appropriate details
     *            info.
     * 
     * @return a Task object
     * @throws RemoteException
     *             , TaskException
     */
    public Task getTask(String p_userId, long p_taskId, int p_state)
            throws RemoteException, TaskException
    {
        return m_localInstance.getTask(p_userId, p_taskId, p_state);
    }

    /**
     * To retrieve a Task by purely by task Id
     * 
     * @param p_taskId
     *            The id of the target task.
     * 
     * @return a Task object
     * @throws RemoteException
     *             , TaskException
     */
    public Task getTask(long p_taskId) throws RemoteException, TaskException
    {
        return m_localInstance.getTask(p_taskId);
    }

    /**
     * @see TaskManager.getTask(long, boolean)
     */
    public Task getTask(long p_taskId, boolean p_isEditable)
            throws RemoteException, TaskException
    {
        return m_localInstance.getTask(p_taskId, p_isEditable);
    }

    /**
     * To retrieve tasks of certain state for the given user.
     * 
     * @param p_userId
     *            the Id of the user.
     * @param p_taskState
     *            The state of the target tasks.
     * 
     * @return a Map of taskId and Task key value pair
     * @throws RemoteException
     *             , TaskException
     */
    public List getTasks(String p_userId, int p_taskState)
            throws RemoteException, TaskException
    {
        return m_localInstance.getTasks(p_userId, p_taskState);
    }

    /**
     * Retrieves all the tasks in a job that are associated with a particular
     * rate type (like HOURLY, PAGE_COUNT, etc..)
     */
    public Collection getTasks(long p_jobId, Integer p_rateType,
            Integer p_costType) throws RemoteException, TaskException
    {
        return m_localInstance.getTasks(p_jobId, p_rateType, p_costType);
    }

    /**
     * @see TaskManager.getTasks(String, long)
     */
    public Collection getTasks(String p_taskName, long p_jobId)
            throws RemoteException, TaskException
    {
        return m_localInstance.getTasks(p_taskName, p_jobId);
    }

    /**
     * @see TaskManager.getTasks(String, long, boolean)
     */
    public Collection getTasks(String p_taskName, long p_jobId,
            boolean p_attachWorkflowTaskInstance) throws RemoteException,
            TaskException
    {
        return m_localInstance.getTasks(p_taskName, p_jobId,
                p_attachWorkflowTaskInstance);
    }
    
    /**
     * Searchs for tasks based on the given criteria
     * 
     * @param p_criteria
     * @return
     */
    public Collection getTasks(TaskSearchParameters a) throws RemoteException,
            TaskException
    {
        return m_localInstance.getTasks(a);
    }

    /**
     * @see TaskManager.getTasksForRating(long)
     */
    public List getTasksForRating(long p_workflowId) throws RemoteException,
            TaskException
    {
        return m_localInstance.getTasksForRating(p_workflowId);
    }

    /**
     * @see TaskManager.getAcceptedTaskInfosInWorkflow(long)
     */
    public Collection getAcceptedTaskInfosInWorkflow(long p_workflowId)
            throws RemoteException, TaskException
    {
        return m_localInstance.getAcceptedTaskInfosInWorkflow(p_workflowId);
    }

    /**
     * @see TaskManager.removeUserAsTaskAcceptor(String)
     */
    public void removeUserAsTaskAcceptor(String p_userId)
            throws RemoteException, TaskException
    {
        m_localInstance.removeUserAsTaskAcceptor(p_userId);
    }

    /**
     * @see TaskManager.updateStfCreationState(long, String)
     */
    public void updateStfCreationState(long p_taskId, String p_stfCreationState)
            throws RemoteException, TaskException
    {
        m_localInstance.updateStfCreationState(p_taskId, p_stfCreationState);
    }

    /**
     * @see TaskManager.getActivieTasks(long)
     */
    public List<TaskImpl> getActivieTasks(long p_jobId)
    {
        return m_localInstance.getActivieTasks(p_jobId);
    }
}
