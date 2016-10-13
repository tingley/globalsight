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
package com.globalsight.cxe.util.workflow;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.hibernate.Transaction;

import com.globalsight.everest.company.MultiCompanySupportedThread;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.jobhandler.JobImpl;
import com.globalsight.everest.qachecks.DITAQAChecker;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.taskmanager.TaskInterimPersistenceAccessor;
import com.globalsight.everest.webapp.pagehandler.administration.company.CompanyRemoval;
import com.globalsight.everest.webapp.pagehandler.administration.reports.ReportConstants;
import com.globalsight.everest.webapp.pagehandler.projects.workflows.WorkflowHandlerHelper;
import com.globalsight.everest.workflow.TaskEmailInfo;
import com.globalsight.everest.workflow.WorkflowTaskInstance;
import com.globalsight.everest.workflowmanager.Workflow;
import com.globalsight.everest.workflowmanager.WorkflowCancelHelper;
import com.globalsight.everest.workflowmanager.WorkflowImpl;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.AmbFileStoragePathUtils;
import com.globalsight.util.FileUtil;

/**
 * Class {@code WorkflowCancelUtil} is used for canceling workflows without
 * using JMS.
 * 
 * @since GBS-4400
 */
public class WorkflowCancelUtil
{
    static private final Logger logger = Logger.getLogger(WorkflowCancelUtil.class);

    /**
     * Processes the workflow cancel asynchronously with thread instead of JMS.
     */
    static public void cancelWorkflowWithThread(Map<String, Object> data)
    {
        WorkflowCancelRunnable runnable = new WorkflowCancelRunnable(data);
        Thread t = new MultiCompanySupportedThread(runnable);
        t.start();
    }

    /**
     * Processes the workflow cancel synchronously.
     */
    static public void cancelWorkflow(Map<String, Object> p_data)
    {
        Workflow wf = null;
        long workflowId = -1;
        String oldJobState = null;
        String oldWorkflowState = null;
        List<WorkflowTaskInstance> taskList = new ArrayList<WorkflowTaskInstance>();
        Transaction tx = null;
        try
        {
            workflowId = (Long) p_data.get("workflowId");
            oldJobState = (String) p_data.get("oldJobState");
            oldWorkflowState = (String) p_data.get("oldWorkflowState");
            boolean isDispatched = (Boolean) p_data.get("isDispatched");

            tx = HibernateUtil.getTransaction();

            wf = HibernateUtil.get(WorkflowImpl.class, workflowId);
            logger.info("Starting to cancel workflow " + wf.getTargetLocale() + " from job "
                    + wf.getJob().getJobId());
            if (isDispatched)
            {
                Map<Long, WorkflowTaskInstance> activeTasks = ServerProxy.getWorkflowServer()
                        .getActiveTasksForWorkflow(workflowId);
                if (activeTasks != null && activeTasks.size() > 0)
                {
                    Collection<WorkflowTaskInstance> wfTaskInstances = activeTasks.values();
                    taskList.addAll(wfTaskInstances);
                    // updateTaskState(tasks, wf.getTasks(),
                    // Task.STATE_DEACTIVE);
                    WorkflowHandlerHelper.removeReservedTimes(wfTaskInstances);
                }
            }
            TaskEmailInfo emailInfo = WorkflowHandlerHelper.getTaskEmailInfo(wf);
            ServerProxy.getWorkflowServer().suspendWorkflow(workflowId, emailInfo);

            JobImpl job = (JobImpl) wf.getJob();
            HibernateUtil.saveOrUpdate(job);

            // If this is the last workflow, needs to clean all job data.
            if (Job.CANCELLED.equals(job.getState()))
            {
                CompanyRemoval removal = new CompanyRemoval(wf.getCompanyId());
                removal.removeJob(job);
            }
            else
            {
                WorkflowCancelHelper.cancelWorkflow(wf);
                TaskInterimPersistenceAccessor.cancelInterimActivities(taskList);
            }

            // delete QA Checks report and DITA QA Checks report files (McAfee).
            deleteQAChecksReportFiles(wf);

            logger.info("Done cancelling workflow " + wf.getTargetLocale() + " from job "
                    + job.getJobId());
        }
        catch (Exception e)
        {
            logger.error("Failed to cancel workflow " + wf.getTargetLocale(), e);

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
        }
        finally
        {
            HibernateUtil.closeSession();
        }
    }

    /**
     * Deletes QA Checks report and DITA QA Checks report files (for McAfee).
     */
    private static void deleteQAChecksReportFiles(Workflow wf)
    {
        long companyId = wf.getCompanyId();
        long jobId = wf.getJob().getId();
        String basePath = AmbFileStoragePathUtils.getReportsDir(companyId).getAbsolutePath();

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
