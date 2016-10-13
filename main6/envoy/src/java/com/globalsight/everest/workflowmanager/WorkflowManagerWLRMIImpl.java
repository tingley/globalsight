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
import com.globalsight.everest.util.system.RemoteServer;
import com.globalsight.everest.workflow.SkipActivityVo;
import com.globalsight.everest.workflow.WorkflowInstance;
import com.globalsight.util.Entry;

/**
 * WorkflowManager remote implementation. All methods are just passed through to
 * the real WorkflowManager.
 */
public class WorkflowManagerWLRMIImpl extends RemoteServer implements
        WorkflowManagerWLRemote
{
    WorkflowManager m_localReference;

    public WorkflowManagerWLRMIImpl() throws java.rmi.RemoteException,
            WorkflowManagerException
    {
        super(WorkflowManager.SERVICE_NAME);
        m_localReference = new WorkflowManagerLocal();
    }

    public Object getLocalReference()
    {
        return m_localReference;
    }

    // public Collection getWorkflows(Expression p_criteriaExpression)
    // throws RemoteException, WorkflowManagerException
    // {
    // return(m_localReference.getWorkflows(p_criteriaExpression));
    // }

    public void dispatch(Workflow p_workflow) throws RemoteException,
            WorkflowManagerException
    {
        m_localReference.dispatch(p_workflow);
    }

    public void dispatch(Job p_job) throws RemoteException,
            WorkflowManagerException
    {
        m_localReference.dispatch(p_job);
    }

    public void archiveWorkflow(Workflow p_workflow) throws RemoteException,
            WorkflowManagerException
    {
        m_localReference.archiveWorkflow(p_workflow);
    }

    public boolean archive(Job p_job) throws RemoteException,
            WorkflowManagerException
    {
        return m_localReference.archive(p_job);
    }

    public void setTaskCompletion(String p_userId, Task p_task,
            String p_destinationArrow, String skipping) throws RemoteException,
            WorkflowManagerException
    {
        m_localReference.setTaskCompletion(p_userId, p_task,
                p_destinationArrow, skipping);
    }

    public void cancel(String p_idOfUserRequestingCancel, Workflow p_workflow)
            throws RemoteException, WorkflowManagerException
    {
        m_localReference.cancel(p_idOfUserRequestingCancel, p_workflow);
    }

    public void cancel(String p_idOfUserRequestingCancel, Job p_job,
            String p_state) throws RemoteException, WorkflowManagerException
    {
        m_localReference.cancel(p_idOfUserRequestingCancel, p_job, p_state);
    }

    public void cancel(String p_idOfUserRequestingCancel, Job p_job,
            String p_state, boolean p_reimport) throws RemoteException,
            WorkflowManagerException
    {
        m_localReference.cancel(p_idOfUserRequestingCancel, p_job, p_state,
                p_reimport);
    }

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
            WorkflowManagerException
    {
        m_localReference.modifyWorkflow(p_sessionId, p_wfInstance,
                p_projectManagerId, p_modifiedTasks);
    }

    /**
     * @see WorkflowManager.getTaskInfoByTaskId(Workflow, List, long, boolean)
     */
    public TaskInfo getTaskInfoByTaskId(Workflow p_workflow,
            List p_wfTaskInfos, long p_taskId, boolean p_acceptedOnly)
            throws RemoteException, WorkflowManagerException
    {
        return m_localReference.getTaskInfoByTaskId(p_workflow, p_wfTaskInfos,
                p_taskId, p_acceptedOnly);
    }

    /**
     * @see WorkflowManager.getTaskInfosInDefaultPath(Workflow)
     */
    public List getTaskInfosInDefaultPath(Workflow p_workflow)
            throws RemoteException, WorkflowManagerException
    {
        return m_localReference.getTaskInfosInDefaultPath(p_workflow);
    }

    public List getTaskInfosInDefaultPathWithSkip(Workflow p_workflow)
            throws RemoteException, WorkflowManagerException
    {
        return m_localReference.getTaskInfosInDefaultPathWithSkip(p_workflow);
    }

    public Workflow getWorkflowByIdRefresh(long p_workflowId)
            throws RemoteException, WorkflowManagerException
    {
        return m_localReference.getWorkflowByIdRefresh(p_workflowId);
    }

    public Workflow getWorkflowById(long p_workflowId) throws RemoteException,
            WorkflowManagerException
    {
        return m_localReference.getWorkflowById(p_workflowId);
    }

    /**
     * @see WorkflowManager.startStfCreationForWorkflow(long, Workflow, String)
     */
    public void startStfCreationForWorkflow(long p_taskId, Workflow p_workflow,
            String p_userId) throws RemoteException, WorkflowManagerException
    {
        m_localReference.startStfCreationForWorkflow(p_taskId, p_workflow,
                p_userId);
    }

    /**
     * @see WorkflowManager.updatePlannedCompletionDate(long, Date)
     */
    @SuppressWarnings("deprecation")
    public void updatePlannedCompletionDate(long p_workflowId,
            Date p_plannedCompletionDate) throws WorkflowManagerException,
            RemoteException
    {
        m_localReference.updatePlannedCompletionDate(p_workflowId,
                p_plannedCompletionDate);
    }

    /**
     * For sla report issue. User can override the estimatedCompletionDate.
     * 
     * @see WorkflowManager.updateEstimatedCompletionDate(long, Date)
     */
    public void updateEstimatedCompletionDate(long p_workflowId,
            Date p_estimatedCompletionDate) throws WorkflowManagerException,
            RemoteException
    {
        m_localReference.updateEstimatedCompletionDate(p_workflowId,
                p_estimatedCompletionDate);
    }

    /**
     * @see WorkflowManager.updateEstimatedTranslateCompletionDate(long, Date)
     */
    public void updateEstimatedTranslateCompletionDate(long p_workflowId,
            Date p_estimatedTranslateCompletionDate)
            throws WorkflowManagerException, RemoteException
    {
        m_localReference.updateEstimatedTranslateCompletionDate(p_workflowId,
                p_estimatedTranslateCompletionDate);
    }

    /**
     * @see WorkflowManager.reassignWorkflowOwners(long, List)
     */
    public void reassignWorkflowOwners(long p_workflowId, List p_workflowOwners)
            throws RemoteException, WorkflowManagerException
    {
        m_localReference.reassignWorkflowOwners(p_workflowId, p_workflowOwners);
    }

    public List<SkipActivityVo> getLocalActivity(String[] workflowIds)
            throws WorkflowManagerException, RemoteException
    {
        return m_localReference.getLocalActivity(workflowIds);
    }

    public void setSkip(List<Entry> list, String userId)
            throws RemoteException, WorkflowManagerException
    {
        m_localReference.setSkip(list, userId);
    }

    @Override
    public List<SkipActivityVo> getLocalActivity(String[] workflowIds,
            Locale locale) throws WorkflowManagerException, RemoteException
    {
        return m_localReference.getLocalActivity(workflowIds, locale);
    }
}
