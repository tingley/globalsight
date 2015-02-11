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
package com.globalsight.everest.workflow;

// java
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.dom4j.Document;

import com.globalsight.everest.taskmanager.Task;
import com.globalsight.everest.taskmanager.TaskInfo;
import com.globalsight.everest.workflowmanager.ArrorInfo;
import com.globalsight.everest.workflowmanager.DefaultPathTasks;
import com.globalsight.everest.workflowmanager.Workflow;
import com.globalsight.util.Entry;

/**
 * This is an interface to the underlying workflow engine. It uses whatever
 * mechanism or API that the workflow engine supports or provide to communicate
 * with the workflow engine.
 */
public interface WorkflowServer
{
    //
    // PUBLIC CONSTANTS
    //
    public final static int NO_EMAIL_NOTIFICATION = 0;

    public final static int REASSIGN_USER = 1;

    public static final String SERVICE_NAME = "WorkflowServer";

    //
    // PUBLIC API METHODS
    //

    /**
     * Accept the specified task for the specified user.
     * 
     * @param wfClone
     *            -The {@code Workflow}
     * 
     * @param p_assignee
     *            - The person who a particular activity has been assigned to.
     * @param p_nodeInstanceId
     *            - The id of the task.
     * @param p_taskInfo
     *            - The object containing the estimated date info that's used
     *            for notification purposes. Note that the dates are based on
     *            system's default calendar.
     * @paramp_emailInfo Information to be used or included in any emails sent
     *                   out.
     * @param isSkipped
     *            If this task is being skipped.
     * 
     * @exception RemoteException
     *                Network related exception.
     * @exception WorkflowException
     *                - Wraps workflow's exceptions.
     * 
     */
    public void acceptTask(Workflow wfClone, String p_assignee,
            long p_nodeInstanceId, TaskInfo p_taskInfo,
            TaskEmailInfo m_emailInfo, boolean isSkipped)
            throws RemoteException, WorkflowException;

    /**
     * Set the current task as finished and activate the next task(s).
     * 
     * @param wfClone
     *            -The {@code Workflow}
     * @param p_assignee
     *            - The task's assignee id.
     * @param p_nodeInstanceId
     *            - The id of the workflow instance that should be advanced.
     * @param p_arrowLabel
     *            - The arrow name of a condition node pointing to the possible
     *            activity node. If there's no condition node following this
     *            activity, the value is null.
     * @param p_taskInfos
     *            - The task info objects that are part the default path. Note
     *            that it only contains tasks following the one that was just
     *            completed.
     * @paramp_emailInfo Information to be used or included in any emails sent
     *                   out.
     * @param skipping
     *            Indicates this task is being skipped.
     * 
     * @return A lightweight workflow instance as WorkflowInstanceInfo object.
     * 
     * @exception RemoteException
     *                Network related exception.
     * @exception WorkflowException
     *                - Wraps workflow's exceptions.
     * 
     */
    public WorkflowInstanceInfo advanceTask(Workflow p_wfClone,
            String p_assignee, long p_nodeInstanceId, String p_arrowLabel,
            DefaultPathTasks p_taskInfos, TaskEmailInfo p_emailInfo,
            String skipping) throws RemoteException, WorkflowException;

    /**
     * Create a new workflow template.
     * 
     * @param p_wfTemplate
     *            - The template to be created.
     * @param doc
     *            The workflow xml template document.
     * 
     * @return The created template with a valid id.
     * @exception RemoteException
     *                Network related exception.
     * @exception WorkflowException
     *                - Wraps jbpm's exceptions.
     */
    WorkflowTemplate importWorkflowTemplate(WorkflowTemplate p_wfTemplate,
            Document doc) throws RemoteException, WorkflowException;

    /**
     * Create a new workflow template.
     * 
     * @param p_wfTemplate
     *            - The template to be created.
     * @param p_worklfowOwners
     *            - The owner (s) of the workflow instances.
     * 
     * @return The created template with a valid id.
     * @exception RemoteException
     *                Network related exception.
     * @exception WorkflowException
     *                - Wraps jbpm's exceptions.
     */
    WorkflowTemplate createWorkflowTemplate(WorkflowTemplate p_wfTemplate,
            WorkflowOwners p_worklfowOwners) throws RemoteException,
            WorkflowException;

    /**
     * Create a new workflow instance based on the given template id. The
     * initiator of the instance is based on the singleton workflow session.
     * 
     * @param p_wfTemplateId
     *            - The id of the template that will be used for the creation of
     *            a process.
     * 
     * @return The id of the newly created workflow instance.
     * 
     * @exception RemoteException
     *                Network related exception.
     * @exception WorkflowException
     *                - Wraps jbpm's exceptions.
     */
    WorkflowInstance createWorkflowInstance(long p_wfTemplateId)
            throws RemoteException, WorkflowException;

    /**
     * @see WorkflowServerLocal.filterTasksForPM(String, int).
     */
    public Map filterTasksForPM(String p_userId, int p_taskState)
            throws RemoteException, WorkflowException;

    /**
     * Get a list of currently active tasks of the specified workflow instance.
     * 
     * @return A Map of active tasks (WorkflowTaskInstance objects) of the
     *         specified workflow instance with the task id as the key.
     * 
     * @param p_workflowInstanceId
     *            - The id of the workflow instance.
     * @exception RemoteException
     *                Network related exception.
     * @exception WorkflowException
     *                - Wraps jbpm's exceptions.
     */
    Map<Long, WorkflowTaskInstance> getActiveTasksForWorkflow(
            long p_workflowInstanceId) throws RemoteException,
            WorkflowException;

    /**
     * Returns an array of task ids that map to all the activity nodes in the
     * default path of thw workflow.
     * <p>
     * This is used for costing.
     * 
     * @param p_workflowInstanceId
     *            The id of the workflow to return the rates for.
     * @return An array of longs (ids of the tasks).
     * 
     * @exception RemoteException
     *                Network related exception.
     * @exception WorkflowException
     *                - Wraps jbpm's exceptions.
     */
    long[] taskIdsInDefaultPath(long p_workflowInstanceId)
            throws WorkflowException, RemoteException;

    /**
     * Returns an array of longs that are the duration of each activity in the
     * default path. The duration is in milliseconds.
     * <p>
     * This is used for calculating the time complete percentage.
     * 
     * @param p_workflowInstanceId
     *            The id of the workflow to return the durations for.
     * @param p_startNodeId
     *            - The id of the node that's considered as the starting node
     *            for a default path (the prior nodes are ignored).
     * @param p_destinationArrow
     *            - The outgoing arrow name for a condition node which
     *            determines the next node.
     * 
     * @return A list of WfTaskInfo objects within the default path of the
     *         workflow containing required info about each task of the workflow
     *         (i.e. the durations of the activities). Note that this list
     *         within the default path of the workflow.
     * 
     * @exception RemoteException
     *                Network related exception.
     * @exception WorkflowException
     *                - Wraps jbpm's exceptions.
     */
    public List timeDurationsInDefaultPath(String p_destinationArrow,
            long p_workflowInstanceId, long p_startNodeId)
            throws WorkflowException, RemoteException;

    /**
     * Returns an array of longs that are the duration of each activity in the
     * default path. The duration is in milliseconds.
     * <p>
     * This is used for calculating the time complete percentage.
     * 
     * @param p_workflowInstanceId
     *            The id of the workflow to return the durations for.
     * @param p_startNodeId
     *            - The id of the node that's considered as the starting node
     *            for a default path (the prior nodes are ignored).
     * @param p_destinationArrow
     *            - The outgoing arrow name for a condition node which
     *            determines the next node.
     * @param wfi
     *            {@code WorkflowInstance}
     * @return
     * @throws WorkflowException
     * @throws RemoteException
     */
    public List timeDurationsInDefaultPath(long p_workflowInstanceId,
            long p_startNodeId, ArrorInfo p_destinationArrow,
            WorkflowInstance wfi) throws WorkflowException, RemoteException;

    /**
     * Get a list of tasks that are assigned to/by a specified user based on the
     * task state.
     * 
     * @return A list of tasks (as WorkflowTask objects) based on the task
     *         state.
     * 
     * @param p_userId
     *            - The current user id.
     * @param p_taskState
     *            - The task state.
     * @exception RemoteException
     *                Network related exception.
     * @exception WorkflowException
     *                - Wraps jbpm's exceptions.
     */
    public Map getTasksForUser(String p_userId, int p_taskState)
            throws RemoteException, WorkflowException;

    /**
     * Get a list of tasks (as WorkflowTask objects) for the specified workflow
     * instance. Note that all returned tasks are TYPE_ACTIVTY.
     * 
     * @param p_workflowInstanceId
     *            - The id of the workflow instance.
     * 
     * @return A list of all tasks (as WorkflowTask objects) of the specified
     *         workflow instance.
     * @exception RemoteException
     *                Network related exception.
     * @exception WorkflowException
     *                - Wraps jbpm's exceptions.
     */
    public Vector getTasksForWorkflow(long p_workflowInstanceId)
            throws RemoteException, WorkflowException;

    /**
     * Get a list of tasks (as WorkflowTaskInstance objects) for the specified
     * workflow instance that have been visited. This means all the tasks that
     * have been completed or accepted (currently active). Note that all
     * returned tasks are TYPE_ACTIVTY.
     * 
     * @param p_workflowInstanceId
     *            - The id of the workflow instance.
     * 
     * @return A list of all tasks (as WorkflowTaskInstance objects) of the
     *         specified workflow instance that have been completed or are
     *         accepted.
     * @exception RemoteException
     *                Network related exception.
     * @exception WorkflowException
     *                - Wraps jbpm's exceptions.
     */
    public List getVisitedTasksForWorkflow(long p_workflowInstanceId)
            throws RemoteException, WorkflowException;

    /**
     * Get a list of tasks (as WorkflowTaskInstance objects) for the specified
     * workflow instance that have not been visited. Note that all returned
     * tasks are TYPE_ACTIVTY.
     * 
     * @param p_workflowInstanceId
     *            - The id of the workflow instance.
     * 
     * @return A list of all tasks (as WorkflowTaskInstance objects) of the
     *         specified workflow instance that have been completed or are
     *         accepted.
     * @exception RemoteException
     *                Network related exception.
     * @exception WorkflowException
     *                -
     */
    public List<WorkflowTaskInstance> getUnVisitedTasksForWorkflow(
            long p_workflowInstanceId) throws RemoteException,
            WorkflowException;

    /**
     * Get a particular workflow instance based on the given id.
     * 
     * @param p_workflowInstanceId
     *            - The id of the workflow instance to be retreived.
     * @return A WorkflowInstance object (if it exists).
     * @exception RemoteException
     *                Network related exception.
     * @exception WorkflowException
     *                - Wraps jbpm's exceptions.
     */
    public WorkflowInstance getWorkflowInstanceById(long p_workflowInstanceId)
            throws RemoteException, WorkflowException;

    /**
     * Get a workflow task info (a lightweight workflow task instance) based on
     * the given task id.
     * 
     * @param p_taskId
     *            - The id of the workflow instance task.
     * 
     * @exception RemoteException
     *                Network related exception.
     * @exception WorkflowException
     *                - Wraps jbpm's exceptions.
     */
    WfTaskInfo getWorkflowTaskInfo(long p_workflowInstanceId, long p_taskId)
            throws RemoteException, WorkflowException;

    /**
     * @see WorkflowServerLocal.getWorkflowTaskInstance(String, long, int).
     */
    public WorkflowTaskInstance getWorkflowTaskInstance(String p_userId,
            long p_taskId, int p_state) throws RemoteException,
            WorkflowException;

    /**
     * Get a particular task of a workflow instance. This method is used when
     * retrieving a workflow task from web services - when there is no logged in
     * session.
     * 
     * @param p_workflowId
     *            - The id of the workflow that the task is in.
     * @param p_taskId
     *            - The id of the workflow instance task.
     * 
     * @return A specified task (node) of a workflow instance.
     * @exception java.rmi.RemoteException
     *                Network related exception.
     * @exception WorkflowException
     *                - Wraps jbpm's exceptions.
     */
    public WorkflowTaskInstance getWorkflowTaskInstance(long p_workflowId,
            long p_taskId) throws RemoteException, WorkflowException;

    /**
     * Get a task of a workflow instance for a particular user. This method is
     * used when retrieving a workflow task for a translator/reviewer/etc. (for
     * a project manager use getWorkflowTaskInstance).
     * 
     * @return A the specified task (node) of a workflow instance.
     * 
     * @param p_assignee
     *            - The task's assignee.
     * @param p_taskId
     *            - The id of the workflow instance task.
     * @param p_state
     *            - The task state.
     * @exception RemoteException
     *                Network related exception.
     * @exception WorkflowException
     *                - Wraps jbpm's exceptions.
     */
    public WorkflowTaskInstance getWorkflowTaskInstanceForAssignee(
            String p_assignee, long p_taskId, int p_state)
            throws RemoteException, WorkflowException;

    /**
     * Get a particular workflow template based on the given id. This method is
     * only used for server side process where no client interaction is
     * required.
     * 
     * @param p_templateId
     *            - The id of the template to be retreived.
     * @return A WorkflowTemplate object (if it exists).
     * @exception RemoteException
     *                Network related exception.
     * @exception WorkflowException
     *                - Wraps jbpm's exceptions.
     */
    WorkflowTemplate getWorkflowTemplateById(long p_templateId)
            throws RemoteException, WorkflowException;

    /**
     * Modify an existing workflow template. Since i-Flow does not allow the
     * modification of a template that has an active associated instance, we
     * create a new template and return the new id for updating the workflow
     * template info object.
     * 
     * @param p_wfTemplate
     *            - The template to be modified.
     * @param p_worklfowOwners
     *            - The owner (s) of the workflow instances.
     * @return The updated workflow template.
     * 
     * @exception RemoteException
     *                Network related exception.
     * @exception WorkflowException
     *                - Wraps jbpms's exceptions.
     */
    WorkflowTemplate modifyWorkflowTemplate(WorkflowTemplate p_wfTemplate,
            WorkflowOwners p_worklfowOwners) throws RemoteException,
            WorkflowException;

    /**
     * Modify an existing workflow instance.
     * 
     * @param p_sessionId
     *            - The client's http session id. This id is used for obtaining
     *            a Workflow Session object (i-Flow object).
     * @param p_wfInstance
     *            - The workflow instance to be modified.
     * @param p_taskInfos
     *            - Contains the task info object for the reassigned activity
     *            (if any) with notification date info.
     * @paramp_emailInfo Information to be used or included in any emails sent
     *                   out.
     * 
     * @return a Map of WorkflowTaskInstances from the process instance as
     *         values to the key of WorkflowConstant.IS_NEW, or IS_DELETED
     * 
     * @exception RemoteException
     *                Network related exception.
     * @exception WorkflowException
     *                - Wraps jbpm's exceptions.
     */
    Map modifyWorkflowInstance(String p_sessionId,
            WorkflowInstance p_wfInstance, DefaultPathTasks p_taskInfos,
            TaskEmailInfo p_emailInfo) throws RemoteException,
            WorkflowException;

    /**
     * Reject the specified task.
     * 
     * @param p_assignee
     *            - The person who a particular activity has been assigned to.
     * @param p_nodeInstanceId
     *            - The id of the task.
     * @param p_taskInfo
     *            - The object containing the estimated date info that's used
     *            for notification purposes. Note that the dates are based on
     *            system's default calendar.
     * @paramp_emailInfo Information to be used or included in any emails sent
     *                   out.
     * 
     * @exception RemoteException
     *                Network related exception.
     * @exception WorkflowException
     *                - Wraps jbpm's exceptions.
     */
    public void rejectTask(String p_assignee, long p_nodeInstanceId,
            TaskInfo p_taskInfo, TaskEmailInfo p_emailInfo)
            throws RemoteException, WorkflowException;

    /**
     * Reassign all active/deactive activities of the given user to the project
     * manager. Note that this method is invoked on the iFlow admin user and
     * will ONLY reassign one particular work item of an activity and will not
     * have any effect on the other generate work items of the same activity.
     * 
     * @param p_userId
     *            - The user id of the assignee.
     * 
     * @exception RemoteException
     *                Network related exception.
     * @exception WorkflowException
     *                - Wraps jbpm's exceptions.
     */
    void reassignUserActivitiesToPm(String p_userId) throws RemoteException,
            WorkflowException;

    /**
     * Start the specified workflow and notify the participant via email. The
     * email is sent based on the timer criteria of a particular node.
     * 
     * @param p_workflowInstanceId
     * @param p_taskInfos
     * @param p_emailInfo
     * @return
     * @throws RemoteException
     * @throws WorkflowException
     */
    public List<WfTaskInfo> startWorkflow(long p_workflowInstanceId,
            DefaultPathTasks p_taskInfos, TaskEmailInfo p_emailInfo)
            throws RemoteException, WorkflowException;

    /**
     * Suspend the specified workflow instance. This is done by de-activating
     * the currently active task.
     * 
     * @param p_workflowInstanceId
     *            - The id of the workflow instance.
     * @paramp_emailInfo Information to be used or included in any emails sent
     *                   out.
     * 
     * @exception RemoteException
     *                Network related exception.
     * @exception WorkflowException
     *                - Wraps jbpm's exceptions.
     */
    public void suspendWorkflow(long p_workflowInstanceId,
            TaskEmailInfo p_emailInfo) throws RemoteException,
            WorkflowException;

    /**
     * Notify (send email) to the participants of the active task of the
     * workflow specified.
     * 
     * @param p_workflowInstanceId
     *            - The id of the workflow instance.
     * @param p_taskActionType
     *            - the action that is being done that an email should be sent
     *            out for. See WorkflowMailerConstants for the types.
     * @param p_emailInfo
     *            Information to be used or included in any emails sent out.
     * 
     * @exception RemoteException
     *                Network related exception.
     * @exception WorkflowException
     *                - Wraps jbpm's exceptions.
     */
    public void notifyTaskParticipants(long p_workflowInstanceId,
            int p_taskActionType, TaskEmailInfo p_emailInfo)
            throws RemoteException, WorkflowException;

    /**
     * Sets the skip activity to the jbpm 's processinstance.
     * 
     * @param list
     *            The entry list.
     * @throws RemoteException
     * @throws WorkflowException
     */
    public void setSkipActivity(List<Entry> list, String userId,
            boolean internal) throws RemoteException, WorkflowException;

    public void setSkipActivity(List<Entry> list, String userId)
            throws RemoteException, WorkflowException;

    /**
     * Set the workflow owners to be the specided user(s). The method will
     * perform an enactment edit, which means that the workflow will not be
     * locked and no private template will be created. Only the workflow will
     * have a set of new owner(s).
     * 
     * @param p_workflowInstanceId
     *            - The id of the workflow which its new owners should be set.
     * @param p_projectManagerId
     *            - The project manager's username (optional).
     * @param p_workflowOwners
     *            - An array of user names for the new workflow owners.
     * 
     * @exception RemoteException
     *                Network related exception.
     * @exception WorkflowException
     *                - Wraps jbpm's exceptions.
     */
    public void reassignWorkflowOwners(long p_workflowInstanceId,
            String p_projectManagerId, String[] p_workflowOwners)
            throws RemoteException, WorkflowException;

    /**
     * Gets the next node in the workflow, from Task.
     * 
     * @param p_task
     *            Currenct task
     * @param p_arrowLabel
     *            Next label name(optional)
     * @param p_skipToAcitivity
     *            activity name(optional)
     * @return
     * @throws Exception
     */
    public WorkflowTaskInstance nextNodeInstances(Task p_task,
            String p_arrowLabel, String p_skipToAcitivity) throws Exception;

    public void advanceWorkFlowNotification(String key, String state);

    public void sendJobActionEmailToUser(String p_fromUserId,
            TaskEmailInfo p_emailInfo, int p_taskActionType);
}
