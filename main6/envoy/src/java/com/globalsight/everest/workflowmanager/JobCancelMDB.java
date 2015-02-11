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

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

import org.apache.log4j.Logger;
import org.hibernate.Transaction;

import com.globalsight.cxe.adaptermdb.EventTopicMap;
import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.jobhandler.JobImpl;
import com.globalsight.everest.page.DataSourceType;
import com.globalsight.everest.page.Page;
import com.globalsight.everest.page.PageState;
import com.globalsight.everest.permission.Permission;
import com.globalsight.everest.projecthandler.WorkflowTemplateInfo;
import com.globalsight.everest.secondarytargetfile.SecondaryTargetFile;
import com.globalsight.everest.secondarytargetfile.SecondaryTargetFileState;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.taskmanager.Task;
import com.globalsight.everest.util.jms.GenericQueueMDB;
import com.globalsight.everest.util.jms.JmsHelper;
import com.globalsight.everest.webapp.pagehandler.administration.company.CompanyRemoval;
import com.globalsight.everest.workflow.TaskEmailInfo;
import com.globalsight.everest.workflow.WorkflowServerWLRemote;
import com.globalsight.everest.workflow.WorkflowTaskInstance;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.AmbFileStoragePathUtils;

@MessageDriven(messageListenerInterface = MessageListener.class, activationConfig =
{
        @ActivationConfigProperty(propertyName = "destination", propertyValue = EventTopicMap.QUEUE_PREFIX_JBOSS
                + JmsHelper.JMS_CANCEL_JOB_QUEUE),
        @ActivationConfigProperty(propertyName = "destinationType", propertyValue = JmsHelper.JMS_TYPE_QUEUE) })
@TransactionManagement(value = TransactionManagementType.BEAN)
public class JobCancelMDB extends GenericQueueMDB
{
    private static final long serialVersionUID = 1L;

    private static Logger log = Logger.getLogger(JobCancelMDB.class.getName());

    public JobCancelMDB()
    {
        super(log);
    }

    /**
     * Listens message and cancel a job.
     * 
     * @param message
     */
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public void onMessage(Message message)
    {
        String oldState = null;
        Transaction tx = null;
        Long jobId = null;

        try
        {
            if (message.getJMSRedelivered())
            {
                log.warn("Ignoring duplicate JMS message.");
                return;
            }

            ArrayList<?> msg = (ArrayList<?>) ((ObjectMessage) message)
                    .getObject();

            jobId = (Long) msg.get(0);
            oldState = (String) msg.get(1);
            Boolean reimport = (Boolean) msg.get(2);

            log.info("Starting to cancel job: " + jobId);

            tx = HibernateUtil.getTransaction();

            JobImpl job = HibernateUtil.get(JobImpl.class, jobId);
            long companyId = job.getCompanyId();
            CompanyThreadLocal.getInstance().setIdValue(companyId);

            Iterator it = job.getWorkflows().iterator();
            Object[] tasks = null;
            List<Object[]> taskList = new ArrayList<Object[]>();
            while (it.hasNext())
            {
                Workflow wf = (Workflow) it.next();
                String state = wf.getState();
                Workflow wfClone = HibernateUtil.get(WorkflowImpl.class,
                        wf.getIdAsLong());
                wfClone.setState(Workflow.CANCELLED);

                // only update the target page state if not LOCALIZED or
                // EXPORTED yet
                if (Workflow.PENDING.equals(state)
                        || Workflow.IMPORT_FAILED.equals(state)
                        || Workflow.READY_TO_BE_DISPATCHED.equals(state)
                        || Workflow.DISPATCHED.equals(state)
                        || Workflow.BATCHRESERVED.equals(state))
                {
                    updatePageState(wfClone.getTargetPages(),
                            PageState.NOT_LOCALIZED);
                    // also update the secondary target files (if any)
                    updateSecondaryTargetFileState(
                            wfClone.getSecondaryTargetFiles(),
                            SecondaryTargetFileState.CANCELLED);
                }

                Map activeTasks = ServerProxy.getWorkflowServer()
                        .getActiveTasksForWorkflow(wf.getId());
                if (activeTasks != null && activeTasks.size() != 0)
                {
                    tasks = activeTasks.values().toArray();
                    taskList.add(tasks);
                    updateTaskState(tasks, wfClone.getTasks(),
                            Task.STATE_DEACTIVE);

                    WorkflowManagerLocal.removeReservedTimes(tasks);
                }

                WorkflowTemplateInfo wfti = job.getL10nProfile()
                        .getWorkflowTemplateInfo(wfClone.getTargetLocale());

                TaskEmailInfo emailInfo = new TaskEmailInfo(
                        job.getL10nProfile().getProject().getProjectManagerId(),
                        wf.getWorkflowOwnerIdsByType(Permission.GROUP_WORKFLOW_MANAGER),
                        wfti.notifyProjectManager(), job.getPriority());

                emailInfo.setJobName(job.getJobName());
                emailInfo.setProjectIdAsLong(new Long(job.getL10nProfile().getProjectId()));
                emailInfo.setSourceLocale(wfClone.getJob().getSourceLocale().toString());
                emailInfo.setTargetLocale(wfClone.getTargetLocale().toString());
                emailInfo.setCompanyId(String.valueOf(companyId));

                ServerProxy.getWorkflowServer().suspendWorkflow(
                        wfClone.getId(), emailInfo);

                HibernateUtil.saveOrUpdate(wfClone);
            }

            String jobState = resetJobState(job, job.getWorkflows(), reimport);
            HibernateUtil.commit(tx);

            if (Job.CANCELLED.equals(jobState))
            {
                // WorkflowManagerLocal.cleanCorpus(jobId);
                // WorkflowManagerLocal.deleteInProgressTmData(job);
                // GBS-2915, discard a job to remove all job data
                CompanyRemoval removal = new CompanyRemoval(job.getCompanyId());
                removal.removeJob(job);
            }

            // for gbs-1302, cancel interim activities
            // TaskInterimPersistenceAccessor.cancelInterimActivities(taskList);
            log.info("Finished to cancel job: " + jobId);
        }
        catch (Exception oe)
        {
            log.info("Failed to cancel job: " + jobId);

            HibernateUtil.rollback(tx);
            HibernateUtil.getSession().flush();

            if (jobId != null && oldState != null)
            {
                Job job = HibernateUtil.get(JobImpl.class, jobId);
                job.setState(oldState);
                HibernateUtil.saveOrUpdate(job);
            }

            log.error(oe.getMessage(), oe);
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
    private void updatePageState(Collection p_pages, String p_state)
            throws Exception
    {
        for (Object p : p_pages)
        {
            Page page = (Page) p;
            if (page.getPageState().equals(PageState.IMPORT_FAIL))
            {
                continue;
            }
            page.setPageState(p_state);
            HibernateUtil.update(page);
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

    public void cancelWorkflow(Message message)
    {

        Workflow wf = null;
        Object[] tasks = null;
        Long workflowId = null;
        Transaction tx = null;
        String oldJobState = null;

        try
        {
            ArrayList<?> msg = (ArrayList<?>) ((ObjectMessage) message)
                    .getObject();

            workflowId = (Long) msg.get(0);
            boolean isDispatched = (Boolean) msg.get(1);
            oldJobState = (String) msg.get(2);

            tx = HibernateUtil.getTransaction();

            wf = HibernateUtil.get(WorkflowImpl.class, workflowId);
            long companyId = wf.getCompanyId();
            CompanyThreadLocal.getInstance().setIdValue(companyId);

            if (isDispatched)
            {
                Map activeTasks = getWFServer().getActiveTasksForWorkflow(
                        workflowId);
                if (activeTasks != null)
                {
                    tasks = activeTasks.values().toArray();
                    updateTaskState(tasks, wf.getTasks(), Task.STATE_DEACTIVE);
                }
            }

            updatePageState(wf.getTargetPages(), PageState.NOT_LOCALIZED);
            updateSecondaryTargetFileState(wf.getSecondaryTargetFiles(),
                    SecondaryTargetFileState.CANCELLED);

            WorkflowTemplateInfo wfti = wf.getJob().getL10nProfile()
                    .getWorkflowTemplateInfo(wf.getTargetLocale());

            TaskEmailInfo emailInfo = new TaskEmailInfo(
                    wf.getJob().getL10nProfile().getProject()
                            .getProjectManagerId(),
                    wf.getWorkflowOwnerIdsByType(Permission.GROUP_WORKFLOW_MANAGER),
                    wfti.notifyProjectManager(), wf.getJob().getPriority());
            emailInfo.setJobName(wf.getJob().getJobName());
            emailInfo.setProjectIdAsLong(new Long(wf.getJob().getL10nProfile()
                    .getProjectId()));
            emailInfo.setSourceLocale(wf.getJob().getSourceLocale().toString());
            emailInfo.setTargetLocale(wf.getTargetLocale().toString());
            emailInfo.setCompanyId(String.valueOf(companyId));

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

            HibernateUtil.commit(tx);
            log.info("Workflow " + wf.getId() + " was cancelled");
        }
        catch (Exception we)
        {
            HibernateUtil.rollback(tx);
            HibernateUtil.getSession().flush();

            if (wf != null && oldJobState != null)
            {
                JobImpl job = (JobImpl) wf.getJob();
                job.setState(oldJobState);
                HibernateUtil.saveOrUpdate(job);
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
     * After a workflow has been cancelled, the Job's state must be reset to the
     * lowest state of the remaining workflows, provided in the p_wfs collection
     * 
     * @param p_uow
     * @param p_job
     * @param p_wfs
     * @param p_reimport
     * @return job state
     * @exception Exception
     */
    private String resetJobState(JobImpl p_job, Collection p_wfs,
            boolean p_reimport) throws Exception
    {
        if (p_wfs.size() == 0
                || WorkflowManagerLocal.workflowsHaveState(p_wfs,
                        Workflow.CANCELLED))
        {
            p_job.setState(Workflow.CANCELLED);
            if (p_reimport)
            {
                updatePageState(p_job.getSourcePages(), PageState.NOT_LOCALIZED);
            }
            else
            {
                updatePageState(p_job.getSourcePages(), PageState.OUT_OF_DATE);
            }
        }
        else
        {
            int lowest = WorkflowManagerLocal.findLowestStateIndex(p_wfs);
            p_job.setState(WorkflowManagerLocal.ORDERED_STATES[lowest]);
            if (lowest >= WorkflowManagerLocal.LOCALIZED_STATE)
            {
                updatePageState(
                        p_job.getSourcePages(),
                        (lowest == WorkflowManagerLocal.LOCALIZED_STATE ? PageState.LOCALIZED
                                : PageState.EXPORTED));
            }
        }
        String jobState = p_job.getState();

        /*
         * for desktop icon download Ambassador.getDownloadableJobs(...)
         */
        if (jobState.equals(Job.EXPORTED)
                && p_job.getWorkflows().iterator().hasNext())
        {
            Workflow wf = p_job.getWorkflows().iterator().next();
            String dataSourceType = (wf.getTargetPages().iterator().next())
                    .getDataSourceType();
            boolean isAutoImport = dataSourceType
                    .equals(DataSourceType.FILE_SYSTEM_AUTO_IMPORT);
            if (isAutoImport)
            {
                File diExportedDir = AmbFileStoragePathUtils
                        .getDesktopIconExportedDir(p_job.getCompanyId());
                File jobDir = new File(diExportedDir, String.valueOf(p_job
                        .getId()));
                if (!jobDir.exists())
                {
                    jobDir.mkdirs();
                }
            }
        }
        return jobState;
    }
}
