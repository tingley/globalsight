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
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hibernate.Transaction;

import com.globalsight.everest.company.MultiCompanySupportedThread;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.jobhandler.JobImpl;
import com.globalsight.everest.page.DataSourceType;
import com.globalsight.everest.page.Page;
import com.globalsight.everest.page.PageState;
import com.globalsight.everest.secondarytargetfile.SecondaryTargetFile;
import com.globalsight.everest.secondarytargetfile.SecondaryTargetFileState;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.taskmanager.Task;
import com.globalsight.everest.webapp.pagehandler.administration.company.CompanyRemoval;
import com.globalsight.everest.webapp.pagehandler.projects.workflows.WorkflowHandlerHelper;
import com.globalsight.everest.workflow.TaskEmailInfo;
import com.globalsight.everest.workflow.WorkflowTaskInstance;
import com.globalsight.everest.workflowmanager.Workflow;
import com.globalsight.everest.workflowmanager.WorkflowImpl;
import com.globalsight.everest.workflowmanager.WorkflowManagerLocal;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.AmbFileStoragePathUtils;

/**
 * Class {@code JobCancelUtil} is used for canceling jobs without using JMS.
 * 
 * @since GBS-4400
 */
public class JobCancelUtil
{
    static private final Logger logger = Logger.getLogger(JobCancelUtil.class);

    /**
     * Processes the job cancel asynchronously with thread instead of JMS.
     */
    static public void cancelJobWithThread(Map<String, Object> data)
    {
        JobCancelRunnable runnable = new JobCancelRunnable(data);
        Thread t = new MultiCompanySupportedThread(runnable);
        t.start();
    }

    /**
     * Processes the job cancel synchronously.
     */
    static public void cancelJob(Map<String, Object> p_data)
    {
        String oldState = null;
        long jobId = -1;
        Transaction tx = null;
        try
        {
            jobId = (Long) p_data.get("jobId");
            oldState = (String) p_data.get("oldState");
            boolean reimport = (Boolean) p_data.get("reimport");

            logger.info("Starting to cancel job " + jobId);
            tx = HibernateUtil.getTransaction();

            JobImpl job = HibernateUtil.get(JobImpl.class, jobId);
            Collection<Workflow> workflows = job.getWorkflows();
            for (Workflow wf : workflows)
            {
                String state = wf.getState();
                Workflow wfClone = HibernateUtil.get(WorkflowImpl.class, wf.getId());
                wfClone.setState(Workflow.CANCELLED);

                // only update the target page state if not LOCALIZED or
                // EXPORTED yet
                if (Workflow.PENDING.equals(state) || Workflow.IMPORT_FAILED.equals(state)
                        || Workflow.READY_TO_BE_DISPATCHED.equals(state)
                        || Workflow.DISPATCHED.equals(state)
                        || Workflow.BATCHRESERVED.equals(state))
                {
                    updatePageState(wfClone.getTargetPages(), PageState.NOT_LOCALIZED);
                    updateSecondaryTargetFileState(wfClone.getSecondaryTargetFiles(),
                            SecondaryTargetFileState.CANCELLED);
                }

                Map<Long, WorkflowTaskInstance> activeTasks = ServerProxy.getWorkflowServer()
                        .getActiveTasksForWorkflow(wf.getId());
                if (activeTasks != null && activeTasks.size() > 0)
                {
                    Collection<WorkflowTaskInstance> wfTaskInstances = activeTasks.values();
                    WorkflowHandlerHelper.updateTaskState(wfTaskInstances, wfClone.getTasks(),
                            Task.STATE_DEACTIVE);
                    WorkflowHandlerHelper.removeReservedTimes(wfTaskInstances);
                }
                TaskEmailInfo emailInfo = WorkflowHandlerHelper.getTaskEmailInfo(wfClone);
                ServerProxy.getWorkflowServer().suspendWorkflow(wfClone.getId(), emailInfo);

                HibernateUtil.saveOrUpdate(wfClone);
            }

            String jobState = resetJobState(job, job.getWorkflows(), reimport);
            HibernateUtil.commit(tx);

            if (Job.CANCELLED.equals(jobState))
            {
                // GBS-2915, discard a job to remove all job data
                CompanyRemoval removal = new CompanyRemoval(job.getCompanyId());
                removal.removeJob(job);
            }

            logger.info("Done cancelling job " + jobId);
        }
        catch (Exception e)
        {
            logger.error("Failed to cancel job " + jobId, e);

            HibernateUtil.rollback(tx);
            HibernateUtil.getSession().flush();

            if (jobId != -1 && oldState != null)
            {
                Job job = HibernateUtil.get(JobImpl.class, jobId);
                job.setState(oldState);
                HibernateUtil.saveOrUpdate(job);
            }
        }
        finally
        {
            HibernateUtil.closeSession();
        }
    }

    /**
     * Updates the secondary target files to the specified state.
     */
    private static void updateSecondaryTargetFileState(Set<SecondaryTargetFile> p_stfs,
            String p_state)
    {
        for (SecondaryTargetFile stf : p_stfs)
        {
            stf.setState(p_state);
            HibernateUtil.update(stf);
        }
    }

    /**
     * Updates the pages to the specified state.
     */
    private static void updatePageState(Collection<?> p_pages, String p_state)
    {
        for (Object o : p_pages)
        {
            Page page = (Page) o;
            if (PageState.IMPORT_FAIL.equals(page.getPageState()))
            {
                continue;
            }
            page.setPageState(p_state);
            HibernateUtil.update(page);
        }
    }

    /**
     * After a workflow is cancelled, the job's state must be reset to the
     * lowest state of the remaining workflows, provided in the p_wfs
     * collection.
     */
    public static String resetJobState(JobImpl p_job, Collection<Workflow> p_wfs,
            boolean p_reimport)
    {
        if (p_wfs.size() == 0
                || WorkflowHandlerHelper.workflowsAllHaveState(p_wfs, Workflow.CANCELLED))
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
            int lowest = WorkflowHandlerHelper.findLowestStateIndex(p_wfs);
            p_job.setState(WorkflowManagerLocal.ORDERED_STATES[lowest]);
            if (lowest >= WorkflowManagerLocal.LOCALIZED_STATE)
            {
                updatePageState(p_job.getSourcePages(),
                        (WorkflowManagerLocal.LOCALIZED_STATE == lowest ? PageState.LOCALIZED
                                : PageState.EXPORTED));
            }
        }
        String jobState = p_job.getState();
        // for desktop icon download, Ambassador.getDownloadableJobs(...)
        if (Job.EXPORTED.equals(jobState) && p_job.getWorkflows().iterator().hasNext())
        {
            Workflow wf = p_job.getWorkflows().iterator().next();
            String dataSourceType = DataSourceType.FILE_SYSTEM_AUTO_IMPORT;
            try
            {
                dataSourceType = (wf.getTargetPages().iterator().next()).getDataSourceType();
            }
            catch (Exception ignore)
            {

            }
            boolean isAutoImport = DataSourceType.FILE_SYSTEM_AUTO_IMPORT.equals(dataSourceType);
            if (isAutoImport)
            {
                File diExportedDir = AmbFileStoragePathUtils
                        .getDesktopIconExportedDir(p_job.getCompanyId());
                File jobDir = new File(diExportedDir, String.valueOf(p_job.getId()));
                if (!jobDir.exists())
                {
                    jobDir.mkdirs();
                }
            }
        }
        else
        {
            WorkflowHandlerHelper.deleteFolderForDI(p_job.getCompanyId(), p_job.getJobId());
        }

        return jobState;
    }
}
