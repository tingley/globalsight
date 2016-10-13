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
package com.globalsight.everest.workflow;

import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.dom4j.Document;

import com.globalsight.everest.taskmanager.Task;
import com.globalsight.everest.taskmanager.TaskInfo;
import com.globalsight.everest.util.system.RemoteServer;
import com.globalsight.everest.util.system.SystemShutdownException;
import com.globalsight.everest.util.system.SystemStartupException;
import com.globalsight.everest.workflowmanager.ArrorInfo;
import com.globalsight.everest.workflowmanager.DefaultPathTasks;
import com.globalsight.everest.workflowmanager.Workflow;
import com.globalsight.util.Entry;

/**
 * WorkflowServers remote implementation. All methods are just pass throughs to
 * the real WorkflowServer. If any method returns a L10nJob or a list of them
 * then it creates L10nJobJDKRMIImpl from the L10nJobs and passes this to the
 * client.
 * 
 */
public class WorkflowServerWLRMIImpl extends RemoteServer implements
        WorkflowServerWLRemote
{
    private WorkflowServerLocal m_localInstance = null;

    // ////////////////////////////////////////////////////////////////////////////
    // Begin: Constructor
    // ////////////////////////////////////////////////////////////////////////////
    /**
     * Construct a remote Job handler.
     * 
     * @param p_localInstance
     *            The local instance of the WorkflowServer)
     * @exception java.rmi.RemoteException
     *                Network related exception.
     */
    public WorkflowServerWLRMIImpl() throws RemoteException
    {
        super(WorkflowServer.SERVICE_NAME);
        m_localInstance = new WorkflowServerLocal();
    }

    // ////////////////////////////////////////////////////////////////////////////
    // End: Constructor
    // ////////////////////////////////////////////////////////////////////////////

    // ////////////////////////////////////////////////////////////////////
    // Begin: WorkflowServer Implementation
    // ////////////////////////////////////////////////////////////////////

    public void acceptTask(Workflow wfClone, String p_assignee,
            long p_nodeInstanceId, TaskInfo p_taskInfo,
            TaskEmailInfo p_emailInfo, boolean isSkipped)
            throws RemoteException, WorkflowException
    {
        m_localInstance.acceptTask(wfClone, p_assignee, p_nodeInstanceId,
                p_taskInfo, p_emailInfo, isSkipped);
    }

    /**
     * @see WorkflowServer.advanceTask(String, String, long, String,
     *      DefaultPathTasks, TaskEmailInfo);
     * 
     */
    public WorkflowInstanceInfo advanceTask(Workflow p_wfClone,
            String p_assignee, long p_nodeInstanceId, String p_arrowLabel,
            DefaultPathTasks p_taskInfos, TaskEmailInfo p_emailInfo,
            String skipping) throws RemoteException, WorkflowException
    {
        return m_localInstance.advanceTask(p_wfClone, p_assignee,
                p_nodeInstanceId, p_arrowLabel, p_taskInfos, p_emailInfo,
                skipping);
    }

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
     *                - Wraps iflow's exceptions.
     */
    public WorkflowInstance createWorkflowInstance(long p_wfTemplateId)
            throws RemoteException, WorkflowException
    {
        return m_localInstance.createWorkflowInstance(p_wfTemplateId);
    }

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
     *                - Wraps iflow's exceptions.
     */
    public WorkflowTemplate createWorkflowTemplate(
            WorkflowTemplate p_wfTemplate, WorkflowOwners p_workflowOwners)
            throws RemoteException, WorkflowException
    {
        return m_localInstance.createWorkflowTemplate(p_wfTemplate,
                p_workflowOwners);
    }

    /**
     * Import a workflow template.
     * 
     * @param p_wfTemplate
     *            - The template to be created.
     * @param doc
     *            The workflow xml template document.
     * 
     * @return The created template with a valid id.
     * @exception java.rmi.RemoteException
     *                Network related exception.
     * @exception WorkflowException
     *                - Wraps iflow's exceptions.
     */
    public WorkflowTemplate importWorkflowTemplate(
            WorkflowTemplate p_wfTemplate, Document doc)
            throws RemoteException, WorkflowException
    {
        return m_localInstance.importWorkflowTemplate(p_wfTemplate, doc);
    }

    /**
     * @see WorkflowServerLocal.filterTasksForPM(String, int).
     */
    public Map filterTasksForPM(String p_userId, int p_taskState)
            throws RemoteException, WorkflowException
    {
        return m_localInstance.filterTasksForPM(p_userId, p_taskState);
    }

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
     *                - Wraps iflow's exceptions.
     */
    @SuppressWarnings("unchecked")
    public Map getActiveTasksForWorkflow(long p_workflowInstanceId)
            throws RemoteException, WorkflowException
    {
        return m_localInstance.getActiveTasksForWorkflow(p_workflowInstanceId);
    }

    /**
     * @see WorkflowServer.taskIdsInDefaultPath (long)
     */
    public long[] taskIdsInDefaultPath(long p_workflowInstanceId)
            throws WorkflowException, RemoteException
    {
        return m_localInstance.taskIdsInDefaultPath(p_workflowInstanceId);
    }

    /*
     * @see
     * com.globalsight.everest.workflow.WorkflowServer#timeDurationsInDefaultPath
     * (java.lang.String, long, long)
     */
    public List timeDurationsInDefaultPath(String p_destinationArrow,
            long p_workflowInstanceId, long p_startNodeId)
            throws WorkflowException, RemoteException
    {
        return m_localInstance.timeDurationsInDefaultPath(p_destinationArrow,
                p_workflowInstanceId, p_startNodeId);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.globalsight.everest.workflow.WorkflowServer#timeDurationsInDefaultPath
     * (long, long, java.lang.String, com.fujitsu.iflow.model.workflow.Node[])
     */
    public List timeDurationsInDefaultPath(long p_workflowInstanceId,
            long p_startNodeId, ArrorInfo p_destinationArrow,
            WorkflowInstance wfi) throws WorkflowException, RemoteException
    {
        return m_localInstance.timeDurationsInDefaultPath(p_workflowInstanceId,
                p_startNodeId, p_destinationArrow, wfi);
    }

    /**
     * Get a list of tasks that are assigned to/by a specified user based on the
     * task state.
     * 
     * @return A list of tasks (as WorkflowTask objects) based on the task state
     * 
     * @param p_userId
     *            - The current user id.
     * @param p_taskState
     *            - The task state.
     * @exception RemoteException
     *                Network related exception.
     * @exception WorkflowException
     *                - Wraps iflow's exceptions.
     */
    public Map getTasksForUser(String p_userId, int p_taskState)
            throws RemoteException, WorkflowException
    {
        return m_localInstance.getTasksForUser(p_userId, p_taskState);
    }

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
     *                - Wraps iflow's exceptions.
     */
    public Vector getTasksForWorkflow(long p_workflowInstanceId)
            throws RemoteException, WorkflowException
    {
        return m_localInstance.getTasksForWorkflow(p_workflowInstanceId);
    }

    /**
     * @see WorkflowServer.getVisitedTasksForWorkflow(WFSession, long)
     */
    public List getVisitedTasksForWorkflow(long p_workflowInstanceId)
            throws RemoteException, WorkflowException
    {
        return m_localInstance.getVisitedTasksForWorkflow(p_workflowInstanceId);
    }

    /**
     * @see WorkflowServer.getVisitedTasksForWorkflow(WFSession, long)
     */
    public List<WorkflowTaskInstance> getUnVisitedTasksForWorkflow(
            long p_workflowInstanceId) throws RemoteException,
            WorkflowException
    {
        return m_localInstance
                .getUnVisitedTasksForWorkflow(p_workflowInstanceId);
    }

    /**
     * Get a particular workflow instance based on the given id.
     * 
     * @param p_workflowInstanceId
     *            - The id of the workflow instance to be retreived.
     * @return A WorkflowInstance object (if it exists).
     * @exception RemoteException
     *                Network related exception.
     * @exception WorkflowException
     *                - Wraps iflow's exceptions.
     */
    public WorkflowInstance getWorkflowInstanceById(long p_workflowInstanceId)
            throws RemoteException, WorkflowException
    {
        return m_localInstance.getWorkflowInstanceById(p_workflowInstanceId);
    }

    /**
     * @see WorkflowServerLocal.getWorkflowTaskInstance(String, long, int).
     */
    public WorkflowTaskInstance getWorkflowTaskInstance(String p_userId,
            long p_taskId, int p_state) throws RemoteException,
            WorkflowException
    {
        return m_localInstance.getWorkflowTaskInstance(p_userId, p_taskId,
                p_state);
    }

    /**
     * @see WorkflowServer.getWorkflowTaskInstance(long, long)
     */
    public WorkflowTaskInstance getWorkflowTaskInstance(long p_workflowId,
            long p_taskId) throws RemoteException, WorkflowException
    {
        return m_localInstance.getWorkflowTaskInstance(p_workflowId, p_taskId);
    }

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
     *                - Wraps iflow's exceptions.
     */
    public WorkflowTaskInstance getWorkflowTaskInstanceForAssignee(
            String p_assignee, long p_taskId, int p_state)
            throws RemoteException, WorkflowException
    {
        return m_localInstance.getWorkflowTaskInstanceForAssignee(p_assignee,
                p_taskId, p_state);
    }

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
     * @return The modified workflow template.
     * 
     * @exception RemoteException
     *                Network related exception.
     * @exception WorkflowException
     *                - Wraps iflow's exceptions.
     */
    public WorkflowTemplate modifyWorkflowTemplate(
            WorkflowTemplate p_wfTemplate, WorkflowOwners p_workflowOwners)
            throws RemoteException, WorkflowException
    {
        return m_localInstance.modifyWorkflowTemplate(p_wfTemplate,
                p_workflowOwners);
    }

    /**
     * Modify an existing workflow instance.
     * 
     * @param p_sessionId
     *            - The client's http session id. This id is used for obtaining
     *            a Workflow Session object (i-Flow object).
     * @param p_wfInstance
     *            - The workflow instance to be modified.
     * @paramp_emailInfo Information to be used or included in any emails sent
     *                   out.
     * 
     * @return a Map of WorkflowTaskInstances from the process instance as keys
     *         to the value of WorkflowConstant.IS_NEW, IS_DELETED, or
     *         IS_REASSIGNED.
     * 
     * @exception RemoteException
     *                Network related exception.
     * @exception WorkflowException
     *                - Wraps iflow's exceptions.
     */
    public Map modifyWorkflowInstance(String p_sessionId,
            WorkflowInstance p_wfInstance, DefaultPathTasks p_taskInfos,
            TaskEmailInfo p_emailInfo) throws RemoteException,
            WorkflowException
    {
        return m_localInstance.modifyWorkflowInstance(p_sessionId,
                p_wfInstance, p_taskInfos, p_emailInfo);
    }

    /**
     * @see WorkflowServer.getWorkflowTemplateById(long)
     */
    public WorkflowTemplate getWorkflowTemplateById(long p_templateId)
            throws RemoteException, WorkflowException
    {
        return m_localInstance.getWorkflowTemplateById(p_templateId);
    }

    /**
     * @see WorkflowServer.rejectTask(String, String, long, TaskInfo,
     *      TaskEmailInfo)
     */
    public void rejectTask(String p_assignee, long p_nodeInstanceId,
            TaskInfo p_taskInfo, TaskEmailInfo p_emailInfo)
            throws RemoteException, WorkflowException
    {
        m_localInstance.rejectTask(p_assignee, p_nodeInstanceId, p_taskInfo,
                p_emailInfo);
    }

    public List<WfTaskInfo> startWorkflow(long p_workflowInstanceId,
            DefaultPathTasks p_taskInfos, TaskEmailInfo p_emailInfo)
            throws RemoteException, WorkflowException
    {
        return m_localInstance.startWorkflow(p_workflowInstanceId, p_taskInfos,
                p_emailInfo);
    }

    /**
     * Suspend the specified workflow instance. This process is done by
     * de-activating the currently active task.
     * 
     * @param p_workflowInstanceId
     *            - The id of the workflow instance.
     * @param p_emailInfo
     *            Information to be used or included in any emails sent out.
     * 
     * @exception RemoteException
     *                Network related exception.
     * @exception WorkflowException
     *                - Wraps iflow's exceptions.
     */
    public void suspendWorkflow(long p_workflowInstanceId,
            TaskEmailInfo p_emailInfo) throws RemoteException,
            WorkflowException
    {
        m_localInstance.suspendWorkflow(p_workflowInstanceId, p_emailInfo);
    }

    /**
     * Notify (send email) to the participants of the active task of the
     * workflow specified.
     * 
     * @param p_WFSession
     *            - The WFSession object created through a singleton object.
     * @param p_workflowInstanceId
     *            - The id of the workflow instance.
     * @param p_taskActionType
     *            - the action that is being done that an email should be sent
     *            out for. See WorkflowMailerConstants for the types.
     * @paramp_emailInfo Information to be used or included in any emails sent
     *                   out.
     * 
     * @exception java.rmi.RemoteException
     *                Network related exception.
     * @exception WorkflowException
     *                - Wraps iflow's exceptions.
     */
    public void notifyTaskParticipants(long p_workflowInstanceId,
            int p_taskActionType, TaskEmailInfo p_emailInfo)
            throws RemoteException, WorkflowException
    {
        m_localInstance.notifyTaskParticipants(p_workflowInstanceId,
                p_taskActionType, p_emailInfo);
        return;
    }

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
     *                - Wraps iflow's exceptions.
     */
    public void reassignUserActivitiesToPm(String p_userId)
            throws RemoteException, WorkflowException
    {
        m_localInstance.reassignUserActivitiesToPm(p_userId);
    }

    /**
     * @see WorkflowServer.assignWorkflowOwners(long, String, String[])
     */
    public void reassignWorkflowOwners(long p_workflowInstanceId,
            String p_projectManagerId, String[] p_workflowOwners)
            throws RemoteException, WorkflowException
    {
        m_localInstance.reassignWorkflowOwners(p_workflowInstanceId,
                p_projectManagerId, p_workflowOwners);
    }

    // ////////////////////////////////////////////////////////////////////////////////
    // End: WorkflowServer Implementation
    // ////////////////////////////////////////////////////////////////////////////////

    // ////////////////////////////////////////////////////////////////////////////
    // Begin: Local Methods
    // ////////////////////////////////////////////////////////////////////////////

    /**
     * Get the reference to the local implementation of the server.
     * 
     * @return The reference to the local implementation of the server.
     */
    public Object getLocalReference()
    {
        return m_localInstance;
    }

    // ////////////////////////////////////////////////////////////////////////////
    // End: Local Methods
    // ////////////////////////////////////////////////////////////////////////////

    // ////////////////////////////////////////////////////////////////////
    // Begin: RemoteServer's Overrides
    // ////////////////////////////////////////////////////////////////////
    /**
     * Override: Invoke the local implementation of workflow start-up.
     */
    public void init() throws SystemStartupException
    {
        // First bind the remote object and then call the startup
        // (due to dependency on remote object).
        super.init();
        m_localInstance.startup();
    }

    /**
     * Override: Invoke the local implementation of workflow shutdown.
     */
    public void destroy() throws SystemShutdownException
    {
        // first invoke the shutdown before unbinding the remote object
        m_localInstance.shutdown();
        super.destroy();
    }

    // ////////////////////////////////////////////////////////////////////
    // End: RemoteServer's Overrides
    // ////////////////////////////////////////////////////////////////////

    public WfTaskInfo getWorkflowTaskInfo(long p_workflowInstanceId,
            long p_taskId) throws RemoteException, WorkflowException
    {
        return m_localInstance.getWorkflowTaskInfo(p_workflowInstanceId,
                p_taskId);
    }

    public void setSkipActivity(List<Entry> list, String userId,
            boolean internal) throws RemoteException, WorkflowException
    {
        m_localInstance.setSkipActivity(list, userId, internal);

    }

    public void setSkipActivity(List<Entry> list, String userId)
            throws RemoteException, WorkflowException
    {
        m_localInstance.setSkipActivity(list, userId);

    }

    public WorkflowTaskInstance nextNodeInstances(Task p_task,
            String p_arrowLabel, String p_skipToAcitivity) throws Exception
    {
        return m_localInstance.nextNodeInstances(p_task, p_arrowLabel,
                p_skipToAcitivity);
    }

    public void advanceWorkFlowNotification(String key, String state)
    {
        m_localInstance.advanceWorkFlowNotification(key, state);

    }

    public void sendJobActionEmailToUser(String p_fromUserId,
            TaskEmailInfo p_emailInfo, int p_taskActionType)
    {
        m_localInstance.sendJobActionEmailToUser(p_fromUserId, p_emailInfo,
                p_taskActionType);

    }
}
