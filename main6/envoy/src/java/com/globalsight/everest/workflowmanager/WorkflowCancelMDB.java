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
import java.util.List;
import java.util.Map;

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
import com.globalsight.everest.permission.Permission;
import com.globalsight.everest.projecthandler.WorkflowTemplateInfo;
import com.globalsight.everest.qachecks.DITAQAChecker;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.taskmanager.TaskInterimPersistenceAccessor;
import com.globalsight.everest.util.jms.GenericQueueMDB;
import com.globalsight.everest.util.jms.JmsHelper;
import com.globalsight.everest.webapp.pagehandler.administration.company.CompanyRemoval;
import com.globalsight.everest.webapp.pagehandler.administration.reports.ReportConstants;
import com.globalsight.everest.workflow.TaskEmailInfo;
import com.globalsight.everest.workflow.WorkflowServerWLRemote;
import com.globalsight.everest.workflow.WorkflowTaskInstance;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.AmbFileStoragePathUtils;
import com.globalsight.util.FileUtil;

@MessageDriven(messageListenerInterface = MessageListener.class, activationConfig =
{
        @ActivationConfigProperty(propertyName = "destination", propertyValue = EventTopicMap.QUEUE_PREFIX_JBOSS
                + JmsHelper.JMS_CANCEL_WORKFLOW_QUEUE),
        @ActivationConfigProperty(propertyName = "destinationType", propertyValue = JmsHelper.JMS_TYPE_QUEUE) })
@TransactionManagement(value = TransactionManagementType.BEAN)
public class WorkflowCancelMDB extends GenericQueueMDB
{
    private static final long serialVersionUID = 1L;

    private static Logger log = Logger.getLogger(JobCancelMDB.class.getName());

    public WorkflowCancelMDB()
    {
        super(log);
    }

    /**
     * Listens message and cancel a workflow.
     * 
     * @param message
     */
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public void onMessage(Message message)
    {
        Workflow wf = null;
        Object[] tasks = null;
        List<Object[]> taskList = new ArrayList<Object[]>();
        Long workflowId = null;
        Transaction tx = null;
        String oldJobState = null;
        String oldWorkflowState = null;
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
			// Maybe this workflow has been discarded (JMS can not ensure it is
			// done in real time).
            if (wf == null)
            	return;
            long companyId = wf.getCompanyId();
            CompanyThreadLocal.getInstance().setIdValue(companyId);

            if (isDispatched)
            {
                Map activeTasks = getWFServer().getActiveTasksForWorkflow(
                        workflowId);
                if (activeTasks != null)
                {
                    tasks = activeTasks.values().toArray();
                    taskList.add(tasks);
//                    updateTaskState(tasks, wf.getTasks(), Task.STATE_DEACTIVE);
                    accepter = ((WorkflowTaskInstance) tasks[0])
                            .getAcceptUser();
                }
            }

//            updatePageState(wf.getTargetPages(), PageState.NOT_LOCALIZED);
//            updateSecondaryTargetFileState(wf.getSecondaryTargetFiles(),
//                    SecondaryTargetFileState.CANCELLED);

            TaskEmailInfo emailInfo = getTaskEmailInfo(wf, accepter);
            getWFServer().suspendWorkflow(workflowId, emailInfo);

            JobImpl job = (JobImpl) wf.getJob();
            HibernateUtil.saveOrUpdate(job);

            // If this is the last work-flow, it need clean all job data.
            if (Job.CANCELLED.equals(job.getState()))
            {
                CompanyRemoval removal = new CompanyRemoval(companyId);
                removal.removeJob(job);
            }

            // now remove from user calendar
            if (isDispatched)
            {
                WorkflowManagerLocal.removeReservedTimes(tasks);
            }

            if (!Job.CANCELLED.equals(job.getState()))
            {
                WorkflowCancelHelper.cancelWorkflow(wf);
                TaskInterimPersistenceAccessor.cancelInterimActivities(taskList);
            }

            // delete QA Checks report and DITA QA Checks report files (McAfee).
            deleteQAChecksReportFiles(wf);

            // HibernateUtil.commit(tx);
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
//    private void updateSecondaryTargetFileState(
//            Set<SecondaryTargetFile> p_stfs, String p_state) throws Exception
//    {
//        for (SecondaryTargetFile stf : p_stfs)
//        {
//            stf.setState(p_state);
//            HibernateUtil.update(stf);
//        }
//    }

    /**
     * Change each page in the collection to the desired state.
     */
//    private void updatePageState(Collection<TargetPage> p_pages, String p_state)
//            throws Exception
//    {
//        for (Page p : p_pages)
//        {
//            if (p.getPageState().equals(PageState.IMPORT_FAIL))
//            {
//                continue;
//            }
//            p.setPageState(p_state);
//            HibernateUtil.update(p);
//        }
//    }

    /**
     * Update the task state to the specified state. Each element of the object
     * array is of type WorkflowTaskInstance.
     */
//    private void updateTaskState(Object[] p_activeTasks, Hashtable p_wfTasks,
//            int p_state) throws Exception
//    {
//        int size = p_activeTasks == null ? -1 : p_activeTasks.length;
//        for (int i = 0; i < size; i++)
//        {
//            WorkflowTaskInstance wfti = (WorkflowTaskInstance) p_activeTasks[i];
//            Task task = (Task) p_wfTasks.get(new Long(wfti.getTaskId()));
//            task.setState(p_state);
//            HibernateUtil.saveOrUpdate(task);
//        }
//    }

    private TaskEmailInfo getTaskEmailInfo(Workflow wf, String accepter)
    {
        WorkflowTemplateInfo wfti = wf.getJob().getL10nProfile()
                .getWorkflowTemplateInfo(wf.getTargetLocale());

        TaskEmailInfo emailInfo = new TaskEmailInfo(
                wf.getJob().getL10nProfile().getProject().getProjectManagerId(),
                wf.getWorkflowOwnerIdsByType(Permission.GROUP_WORKFLOW_MANAGER),
                wfti.notifyProjectManager(), wf.getJob().getPriority());
        emailInfo.setJobName(wf.getJob().getJobName());
        emailInfo.setProjectIdAsLong(new Long(wf.getJob().getL10nProfile()
                .getProjectId()));
        emailInfo.setSourceLocale(wf.getJob().getSourceLocale().toString());
        emailInfo.setTargetLocale(wf.getTargetLocale().toString());
        emailInfo.setCompanyId(String.valueOf(wf.getCompanyId()));
        if (accepter != null)
        {
            emailInfo.setAccepterName(accepter);
        }

        return emailInfo;
    }

    private WorkflowServerWLRemote getWFServer() throws Exception
    {
        return ServerProxy.getWorkflowServer();
    }

    private void deleteQAChecksReportFiles(Workflow wf)
    {
        long companyId = wf.getCompanyId();
        long jobId = wf.getJob().getId();
        String basePath = AmbFileStoragePathUtils.getReportsDir(companyId)
                .getAbsolutePath();

        StringBuilder ditaChecksPath = new StringBuilder(basePath);
        ditaChecksPath.append(File.separator);
        ditaChecksPath.append(DITAQAChecker.DITA_QA_CHECKS_REPORT);
        ditaChecksPath.append(File.separator);
        ditaChecksPath.append(jobId);
        ditaChecksPath.append(File.separator);
        ditaChecksPath.append(wf.getTargetLocale().toString());
        File file = new File(ditaChecksPath.toString());
        FileUtil.deleteFile(file);

        StringBuilder qaChecksPath = new StringBuilder(basePath);
        qaChecksPath.append(File.separator);
        qaChecksPath.append(ReportConstants.REPORT_QA_CHECKS_REPORT);
        qaChecksPath.append(File.separator);
        qaChecksPath.append(jobId);
        qaChecksPath.append(File.separator);
        qaChecksPath.append(wf.getTargetLocale().toString());
        file = new File(qaChecksPath.toString());
        FileUtil.deleteFile(file);
    }
}
