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

package com.globalsight.everest.workflowmanager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jms.Message;
import javax.jms.ObjectMessage;

import org.apache.log4j.Logger;

import org.hibernate.Transaction;

import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.jobhandler.JobImpl;
import com.globalsight.everest.page.Page;
import com.globalsight.everest.page.PageState;
import com.globalsight.everest.page.TargetPage;
import com.globalsight.everest.permission.Permission;
import com.globalsight.everest.projecthandler.WorkflowTemplateInfo;
import com.globalsight.everest.secondarytargetfile.SecondaryTargetFile;
import com.globalsight.everest.secondarytargetfile.SecondaryTargetFileState;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.taskmanager.Task;
import com.globalsight.everest.taskmanager.TaskInterimPersistenceAccessor;
import com.globalsight.everest.util.jms.GenericQueueMDB;
import com.globalsight.everest.workflow.TaskEmailInfo;
import com.globalsight.everest.workflow.WorkflowServerWLRemote;
import com.globalsight.everest.workflow.WorkflowTaskInstance;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.everest.workflowmanager.WorkflowManagerLocal;

public class WorkflowCancelMDB extends GenericQueueMDB
{
    private static final long serialVersionUID = 1L;

    private static Logger log = Logger
            .getLogger(JobCancelMDB.class.getName());

    public WorkflowCancelMDB()
    {
        super(log);
    }

    /**
     * Listens message and cancel a job.
     * 
     * @param message
     */
    public void onMessage(Message message)
    {
        Workflow wf = null;
        Object[] tasks = null;
        List<Object[]> taskList = new ArrayList<Object[]>();
        Long workflowId = null;
        Transaction tx = null;
        String oldJobState = null;
        String oldWorkflowState = null;
        //Activity accepter
        String accepter = null;

        try
        {
            ArrayList<?> msg = (ArrayList<?>) ((ObjectMessage) message)
                    .getObject();

            workflowId = (Long) msg.get(0);
            boolean isDispatched = (Boolean) msg.get(1);
            oldJobState = (String) msg.get(2);
            oldWorkflowState = (String) msg.get(3);

            tx = HibernateUtil.getTransaction();

            wf = HibernateUtil.get(WorkflowImpl.class, workflowId);

            if (isDispatched)
            {
                Map activeTasks = getWFServer().getActiveTasksForWorkflow(
                        workflowId);
                if (activeTasks != null)
                {
                    tasks = activeTasks.values().toArray();
                    taskList.add(tasks);
                    updateTaskState(tasks, wf.getTasks(), Task.STATE_DEACTIVE);
                    accepter = ((WorkflowTaskInstance) tasks[0]).getAcceptUser();
                }
            }

            updatePageState(wf.getTargetPages(), PageState.NOT_LOCALIZED);
            updateSecondaryTargetFileState(wf.getSecondaryTargetFiles(),
                    SecondaryTargetFileState.CANCELLED);

            WorkflowTemplateInfo wfti = wf.getJob().getL10nProfile()
                    .getWorkflowTemplateInfo(wf.getTargetLocale());

            TaskEmailInfo emailInfo = new TaskEmailInfo(
                    wf.getJob().getL10nProfile().getProject().getProjectManagerId(),
                    wf.getWorkflowOwnerIdsByType(Permission.GROUP_WORKFLOW_MANAGER),
                    wfti.notifyProjectManager(), wf.getJob().getPriority());
            emailInfo.setJobName(wf.getJob().getJobName());
            emailInfo.setProjectIdAsLong(new Long(wf.getJob().getL10nProfile().getProjectId()));
            emailInfo.setSourceLocale(wf.getJob().getSourceLocale().toString());
            emailInfo.setTargetLocale(wf.getTargetLocale().toString());
            emailInfo.setCompanyId(wf.getJob().getCompanyId());
            if(null!=accepter)
            {
            	emailInfo.setAccepterName(accepter);
            }            

            getWFServer().suspendWorkflow(workflowId, emailInfo);

            JobImpl job = (JobImpl) wf.getJob();
            Long jobId = job.getIdAsLong();
            HibernateUtil.saveOrUpdate(job);

            if (Job.CANCELLED.equals(job.getState()))
            {
                WorkflowManagerLocal.cleanCorpus(jobId);
                WorkflowManagerLocal.deleteInProgressTmData(job);
            }

            // now remove from user calendar
            if (isDispatched)
            {
                WorkflowManagerLocal.removeReservedTimes(tasks);
            }

            //HibernateUtil.commit(tx);
            // for gbs-1302, cancel interim activities
            TaskInterimPersistenceAccessor.cancelInterimActivities(taskList);
            log.info("Workflow " + wf.getId() + " was cancelled");
        }
        catch (Exception we)
        {
            HibernateUtil.rollback(tx);
            HibernateUtil.getSession().flush();

            if (wf != null && oldJobState != null && oldWorkflowState != null)
            {
                JobImpl job = (JobImpl) wf.getJob();
                job.setState(oldJobState);
                wf.setState(oldWorkflowState);
                HibernateUtil.saveOrUpdate(job);
                HibernateUtil.saveOrUpdate(wf);
            }

            log.error(we.getMessage(), we);
            String[] args = new String[1];
            if (workflowId != null)
            {
                args[0] = workflowId.toString();
            }
            else
            {
                args[0] = "unknow";
            }
            throw new WorkflowManagerException(
                    WorkflowManagerException.MSG_FAILED_TO_CANCEL_WORKFLOW,
                    args, we);
        }
        finally
        {
            HibernateUtil.closeSession();
        }
    }

    /**
     * Change the state of each secondary target file.
     */
    private void updateSecondaryTargetFileState(
            Set<SecondaryTargetFile> p_stfs, String p_state) throws Exception
    {
        for (SecondaryTargetFile stf : p_stfs)
        {
            stf.setState(p_state);
            HibernateUtil.update(stf);
        }
    }

    /**
     * Change each page in the collection to the desired state.
     */
    private void updatePageState(Collection<TargetPage> p_pages, String p_state)
            throws Exception
    {
        for (Page p : p_pages)
        {
            if (p.getPageState().equals(PageState.IMPORT_FAIL))
            {
                continue;
            }
            p.setPageState(p_state);
            HibernateUtil.update(p);
        }
    }

    /**
     * Update the task state to the specified state. Each element of the object
     * array is of type WorkflowTaskInstance.
     */
    private void updateTaskState(Object[] p_activeTasks, Hashtable p_wfTasks,
            int p_state) throws Exception
    {
        int size = p_activeTasks == null ? -1 : p_activeTasks.length;
        for (int i = 0; i < size; i++)
        {
            WorkflowTaskInstance wfti = (WorkflowTaskInstance) p_activeTasks[i];
            Task task = (Task) p_wfTasks.get(new Long(wfti.getTaskId()));
            task.setState(p_state);
            HibernateUtil.saveOrUpdate(task);
        }
    }

    private WorkflowServerWLRemote getWFServer() throws Exception
    {
        return ServerProxy.getWorkflowServer();
    }
}
