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

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * TaskManager is an interface that defines serves to handlle and delegate task
 * related processes.
 */
public interface TaskManager
{
    /**
     * The name bound to the remote object.
     */
    public static final String SERVICE_NAME = "TaskManagerServer";

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
            throws RemoteException, TaskException;

    /**
     * @see TaskManagerLocal.acceptTask(String, Task, Map<String, Object>)
     */
    public void acceptTask(String p_userId, Task p_task,
            Map<String, Object> data) throws RemoteException, TaskException;

    /**
     * To complete a Task. It invokes Iflow to complete a task, also saved
     * completed time to database task_info table.
     * 
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
            TaskException;

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
            throws RemoteException, TaskException;

    /**
     * Get a list of current tasks of the workflow (that are in ACTIVE, or
     * ACCEPTED state) for the given workflow id.
     * 
     * @param p_workflowId
     *            - The id of the workflow where its current tasks are being
     *            queried.
     * 
     * @return A list of current tasks (active or accepted) if any.
     * 
     * @throws RemoteException
     *             , TaskException
     */
    Collection getCurrentTasks(long p_workflowId) throws RemoteException,
            TaskException;

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
            throws RemoteException, TaskException;

    /**
     * To retrieve a Task purely by task Id. This task is read-only (not
     * editable).
     * 
     * @param p_taskId
     *            The id of the target task.
     * 
     * @return a Task object
     * @throws RemoteException
     *             , TaskException
     */
    public Task getTask(long p_taskId) throws RemoteException, TaskException;

    /**
     * To retrieve a Task purely by task Id. and specify if it is editable
     * (cloned) or not.
     * 
     * @param p_taskId
     *            The id of the target task.
     * @param p_isEditable
     *            Whether it should cloned so it can be edited or not.
     * 
     * @return a Task object
     * @throws RemoteException
     *             , TaskException
     */
    public Task getTask(long p_taskId, boolean p_isEditable)
            throws RemoteException, TaskException;

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
            throws RemoteException, TaskException;

    /**
     * Retrieves all the tasks in a job that are associated with a particular
     * rate type (like HOURLY, PAGE_COUNT, etc..)
     */
    public Collection getTasks(long p_jobId, Integer p_rateType,
            Integer p_costType) throws RemoteException, TaskException;

    /**
     * Get a collection of tasks based on the given task name and job id. Note
     * that a workflow could have multiple tasks with the same name (besides the
     * possiblity of having a task with same name in multiple workflows of a
     * job).
     * 
     * @param p_taskName
     *            - The name of the tasks to be retrieved.
     * @param p_jobId
     *            - The id of the job which the tasks belong to.
     * @return A collection of tasks based on the given criteria.
     * 
     * @throws RemoteException, TaskException
     */
    public Collection getTasks(String p_taskName, long p_jobId)
            throws RemoteException, TaskException;

    /**
     * Get a collection of tasks based on the given task name and job id. Note
     * that a workflow could have multiple tasks with the same name (besides the
     * possiblity of having a task with same name in multiple workflows of a
     * job).
     * 
     * @param p_taskName
     *            - The name of the tasks to be retrieved.
     * @param p_jobId
     *            - The id of the job which the tasks belong to.
     * @param p_attachWorkflowTaskInstance
     *            - load the workflow task instance to tasks or not. As this is
     *            an expensive query, if not required, should set it to false.
     * 
     * @return A collection of tasks based on the given criteria.
     * 
     * @throws RemoteException, TaskException
     */
    public Collection getTasks(String p_taskName, long p_jobId,
            boolean p_attachWorkflowTaskInstance) throws RemoteException,
            TaskException;

    /**
     * Searchs for tasks based on the given criteria
     * 
     * @param p_criteria
     * @return
     */
    public Collection getTasks(TaskSearchParameters p_params) throws RemoteException,
            TaskException;

    /**
     * @return List Returns a list of tasks from the workflow that can be rated.
     *         This means the task is complete and it was completed by a user
     *         that is a vendor. The list is returned with the tasks in order of
     *         completion.
     */
    public List getTasksForRating(long p_workflowId) throws RemoteException,
            TaskException;

    /**
     * Return information about all the accepted tasks within the specified
     * workflow.
     * 
     * @param p_workflowId
     *            The id of the workflow the tasks are part of.
     * 
     * @return A collection of TaskInfo objects that contain information about
     *         all the accepted tasks. This collection could be empty if no
     *         tasks have been accepted yet. TaskInfo is strictly an entitiy
     *         object that is serializable and can be used by clients.
     */
    public Collection getAcceptedTaskInfosInWorkflow(long p_workflowId)
            throws RemoteException, TaskException;

    /**
     * Removes the user as a task acceptor and reassigns all existing task
     * assigned to that user to the workflow owner.
     * 
     * @param p_userId
     *            - The user id of the removed user.
     * 
     * @throws RemoteException
     *             , TaskException
     */
    void removeUserAsTaskAcceptor(String p_userId) throws RemoteException,
            TaskException;

    /**
     * Update the secondary target file creation state for this task. The valid
     * states are 'IN_PROGRESS', 'COMPLETED', and 'FAILED'.
     * 
     * @param p_taskId
     *            - The id of the task to be updated.
     * @param p_stfCreationState
     *            - The state to be set.
     * 
     * @exception RemoteException
     *                Network related exception.
     * @exception TaskException
     *                Component related exception.
     */
    void updateStfCreationState(long p_taskId, String p_stfCreationState)
            throws RemoteException, TaskException;

    /**
     * Get all active tasks by Job ID.
     * 
     * @param p_jobId
     *            job id
     * @return List<TaskInfo>
     */
    public List<TaskImpl> getActivieTasks(long p_jobId);
}
