/**
 * Copyright 2009 Welocalize, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.
 * 
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 * 
 */
package com.globalsight.everest.workflowmanager;

import java.rmi.RemoteException;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;

import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.taskmanager.Task;
import com.globalsight.everest.taskmanager.TaskInfo;
import com.globalsight.everest.workflow.SkipActivityVo;
import com.globalsight.everest.workflow.WorkflowInstance;
import com.globalsight.util.Entry;

public interface WorkflowManager
{
    // The name bound to the remote object.
    public static final String SERVICE_NAME = "WorkflowManager";

    /**
     * @return Workflow object
     * @throws RemoteException
     *             , WorkflowManagerException
     */
    public Workflow getWorkflowByIdRefresh(long p_workflowId)
            throws RemoteException, WorkflowManagerException;

    public Workflow getWorkflowById(long p_workflowId) throws RemoteException,
            WorkflowManagerException;

    /**
     * @return Collection Workflows
     * @param Expression
     *            p_CriteriaExpression
     * @throws RemoteException
     *             , WorkflowManagerException
     */
    // public Collection getWorkflows(Expression p_criteriaExpression)
    // throws RemoteException, WorkflowManagerException;
    /**
     * This method cancels a single Workflow
     * 
     * @param p_idOfUserRequestingCancel
     * @param p_workflow
     * @throws RemoteException
     *             , WorkflowManagerException
     */
    public void cancel(String p_idOfUserRequestingCancel, Workflow p_workflow)
            throws RemoteException, WorkflowManagerException;

    /**
     * This method cancels the job's workflows that are in the state specified
     * or the entire job if the state is NULL.
     * 
     * @param p_idOfUserRequestingCancel
     * @param p_job
     * @param p_state
     *            The state of the workflows to cancel or NULL if all should be
     *            cancelled.
     * @throws RemoteException
     *             , WorkflowManagerException
     */
    public void cancel(String p_idOfUserRequestingCancel, Job p_job,
            String p_state) throws RemoteException, WorkflowManagerException;

    /**
     * This method cancels the job's workflows that are in the state specified
     * or the entire job if the state is NULL.
     * 
     * @param p_idOfUserRequestingCancel
     * @param p_job
     * @param p_state
     *            The state of the workflows to cancel or NULL if all should be
     *            cancelled.
     * @param p_reimport
     * @throws RemoteException
     *             , WorkflowManagerException
     */
    public void cancel(String p_idOfUserRequestingCancel, Job p_job,
            String p_state, boolean p_reimport) throws RemoteException,
            WorkflowManagerException;

    /**
     * This method dispatches a single workflow
     * 
     * @param Workflow
     *            object
     * @throws RemoteException
     *             , WorkflowManagerException
     */
    public void dispatch(Workflow p_workflow) throws RemoteException,
            WorkflowManagerException;

    /**
     * This method dispatches a single workflow
     * 
     * @param Workflow
     *            object
     * @throws RemoteException
     *             , WorkflowManagerException
     */
    public void dispatch(Job p_job) throws RemoteException,
            WorkflowManagerException;

    /**
     * This modifies an active workflow.
     * 
     * @param p_sessionId
     *            - Users login HTTPSession id
     * @param p_wfInstance
     *            - WorkflowInstance that has been modified.
     * @param p_projectManagerId
     *            - the ProjectManager userId.
     * @param p_modifiedTasks
     *            - A hashtable of the modified tasks. The key is the Task id
     *            and the value is a TaskInfoBean that contains the
     *            modifications.
     * @throws RemoteException
     *             , WorkflowManagerException
     */
    public void modifyWorkflow(String p_sessionId,
            WorkflowInstance p_wfInstance, String p_projectManagerId,
            Hashtable p_modifiedTasks) throws RemoteException,
            WorkflowManagerException;

    /**
     * Get a task info (within the workflow's default path) based on the given
     * task id. The task info is used for getting estimated dates along with a
     * list of task assignees (with their respective estimated dates based on
     * their own calendar).
     * 
     * @param p_workflow
     *            - The workflow used for finding its default path.
     * @param p_wfTaskInfos
     *            A list of wf task info objects in the default path.
     * @param p_taskId
     *            - The id of the task for which a TaskInfo object is requested.
     * @param p_acceptedOnly
     *            - True if only want only the accepted user's data
     * @return A TaskInfo object with date related information and associated
     *         possible assignees of the task based on the role.
     * @throws RemoteException
     *             , WorkflowManagerException
     */
    public TaskInfo getTaskInfoByTaskId(Workflow p_workflow,
            List p_wfTaskInfos, long p_taskId, boolean p_acceptedOnly)
            throws RemoteException, WorkflowManagerException;

    /**
     * Get a list of tasks in the workflow's default path. The tasks are only
     * the activity nodes.
     * 
     * @param p_workflow
     *            - The workflow used for finding its default path.
     * @return A list of TaskInfo objects with date related information and
     *         associated possible assignees of the task based on the role.
     * @throws RemoteException
     *             , WorkflowManagerException
     */
    List getTaskInfosInDefaultPath(Workflow p_workflow) throws RemoteException,
            WorkflowManagerException;

    List getTaskInfosInDefaultPathWithSkip(Workflow p_workflow)
            throws RemoteException, WorkflowManagerException;

    /**
     * This method allows the client to archive a single workflow
     * 
     * @param String
     *            Workflow object
     * @throws RemoteException
     *             , WorkflowManagerException
     */
    public void archiveWorkflow(Workflow p_workflow) throws RemoteException,
            WorkflowManagerException;

    /**
     * This method sets the percentage completion of a particular workflow
     * 
     * @param Task
     *            task object
     * @param p_destinationArrow
     *            - The name of the outgoing arrow of a condition node (if the
     *            next node of this task is a condition node). This is used for
     *            making decision.
     * @param skipping
     *            Indicates this task is being skipped.
     * 
     * @throws RemoteException
     *             , WorkflowManagerException
     */
    public void setTaskCompletion(String p_userId, Task p_task,
            String p_destinationArrow, String skipping) throws RemoteException,
            WorkflowManagerException;

    /**
     * Gets the map based on the workflow ids for the skip activities operation
     * . <br>
     * The list contains the unvisited activities list is the list of name of
     * the activities which have not been activited. <br>
     * 
     * @param workflowIds
     *            The arrary of the workflow id.
     * @return The list of the {@code SkipActivityVo}
     * @throws WorkflowManagerException
     * @throws RemoteException
     */
    public List<SkipActivityVo> getLocalActivity(String[] workflowIds)
            throws WorkflowManagerException, RemoteException;

    public List<SkipActivityVo> getLocalActivity(String[] workflowIds,
            Locale locale) throws WorkflowManagerException, RemoteException;

    /**
     * 
     * This method allows the client to archive a whole job
     * 
     * @param Job
     *            p_job
     * @throws RemoteException
     *             , WorkflowManagerException
     */
    public boolean archive(Job p_job) throws RemoteException,
            WorkflowManagerException;

    /**
     * Set the skip to the workflow. <br>
     * This operation will only save the skipped activity, will not forward the
     * activity. <br>
     * The list of the entry contains the pair of the workflow id and the name
     * of the activity(with the company name).
     * 
     * @param list
     * @throws RemoteException
     * @throws WorkflowManagerException
     */
    public void setSkip(List<Entry> list, String userId)
            throws RemoteException, WorkflowManagerException;

    /**
     * Start the process of creating Secondary Target File(s) for the given
     * workflow.
     * 
     * @param p_taskId
     *            - The id of the task where this process was invoked.
     * @param p_workflow
     *            - The workflow for which the secondary target files will be
     *            generated.
     * @param p_userId
     *            - The user name of the initiator of this process.
     * 
     * @throws RemoteException
     *             , WorkflowManagerException
     */
    void startStfCreationForWorkflow(long p_taskId, Workflow p_workflow,
            String p_userId) throws RemoteException, WorkflowManagerException;

    /**
     * Update the planned completion date of the workflow with the given id.
     * 
     * @deprecated For sla report issue.
     * @param p_workflowId
     *            - The id of the workflow to be updated.
     * @param p_plannedCompletionDate
     *            - The new planned completion date.
     */
    void updatePlannedCompletionDate(long p_workflowId,
            Date p_plannedCompletionDate) throws WorkflowManagerException,
            RemoteException;

    /**
     * For sla report issue Update the estimated completion date of the workflow
     * with the given id.
     * 
     * @param p_workflowId
     *            - The id of the workflow to be updated.
     * @param p_estimatedCompletionDate
     *            - The new estimated completion date.
     */
    void updateEstimatedCompletionDate(long p_workflowId,
            Date p_estimatedCompletionDate) throws WorkflowManagerException,
            RemoteException;

    /**
     * Update the estimated translate completion date of the workflow with the
     * given id.
     * 
     * @param p_sessionId
     *            - Users login HTTPSession id
     * @param p_workflowId
     *            - The id of the workflow to be updated.
     * @param p_estimatedTranslateCompletionDate
     *            - The new date.
     */
    void updateEstimatedTranslateCompletionDate(long p_workflowId,
            Date p_estimatedTranslateCompletionDate)
            throws WorkflowManagerException, RemoteException;

    /**
     * Set the workflow owners to be the specided user(s).
     * 
     * @param p_workflowId
     *            - The id of the workflow which its new owners should be set.
     * @param p_workflowOwners
     *            - A list of WorkflowOwner objects for the new workflow owners.
     * 
     * @throws RemoteException
     *             , WorkflowManagerException
     */
    void reassignWorkflowOwners(long p_workflowId, List p_workflowOwners)
            throws RemoteException, WorkflowManagerException;
}
