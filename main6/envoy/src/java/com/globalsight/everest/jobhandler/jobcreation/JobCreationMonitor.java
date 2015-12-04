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
package com.globalsight.everest.jobhandler.jobcreation;

import java.sql.Timestamp;
import java.util.Collection;

import org.apache.log4j.Logger;

import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.jobhandler.JobImpl;
import com.globalsight.everest.persistence.tuv.BigTableUtil;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.workflow.WorkflowException;
import com.globalsight.everest.workflowmanager.Workflow;
import com.globalsight.ling.tm2.persistence.DbUtil;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.edit.EditUtil;

/**
 * This class monitors the job creation process with different states before
 * created successfully.
 */
public class JobCreationMonitor
{
    private static final Logger c_logger = Logger
            .getLogger(JobCreationMonitor.class.getName());

    /**
     * Cleans up the jobs that were stopped because the system was shutdown.
     * <p>
     * Only invoked when system starts up.
     */
    public static void cleanupIncompleteJobs()
    {
        String sql = "update JOB set STATE = '" + Job.IMPORTFAILED
                + "' where STATE in ('" + Job.UPLOADING + "', '" + Job.IN_QUEUE
                + "', '" + Job.EXTRACTING + "', '" + Job.LEVERAGING + "', '"
                + Job.CALCULATING_WORD_COUNTS + "', '" + Job.PROCESSING + "')";
        try
        {
            HibernateUtil.executeSql(sql);
        }
        catch (Exception e)
        {
            c_logger.error("Missed cleaning up incomplete jobs.", e);
            // not blocking the following processes.
        }
        cleanupSkippingWorkflows();
    }

    /**
     * Cleans up the workflows that were being skipped because the system was
     * shutdown.
     * <p>
     * Only invoked when system starts up.
     */
    private static void cleanupSkippingWorkflows()
    {
        String sql = "update WORKFLOW set STATE = '" + Workflow.DISPATCHED
                + "' where STATE = '" + Workflow.SKIPPING + "'";
        try
        {
            HibernateUtil.executeSql(sql);
        }
        catch (Exception e)
        {
            c_logger.error("Missed cleaning up skipping workflows.", e);
            // not blocking the following processes.
        }
    }

    /**
     * Initializes a new job when a file is uploaded.
     */
    public static Job initializeJob(String jobName, String userId,
            long l10nProfileId, String priority, String state)
            throws JobCreationException
    {
        String uuid = jobName;
        return initializeJob(jobName, uuid, userId, l10nProfileId, priority,
                state);
    }

    public static Job initializeJob(String jobName, String uuid, String userId,
            long l10nProfileId, String priority, String state)
            throws JobCreationException
    {
    	String jobType = null;
        return initializeJob(jobName, uuid, userId, l10nProfileId, priority,
                state, jobType);
    }

    /**
     * Initializes a new job when a file is uploaded.
     */
    public static Job initializeJob(String jobName, String uuid, String userId,
            long l10nProfileId, String priority, String state, String jobType)
            throws JobCreationException
    {
        JobImpl job = null;
        try
        {
            job = new JobImpl();
            job.setJobName(EditUtil.removeCRLF(jobName));
            job.setCreateUserId(userId);
            job.setL10nProfileId(l10nProfileId);
            job.setSourceLocale(ServerProxy.getProjectHandler()
                    .getL10nProfile(l10nProfileId).getSourceLocale());
            job.setPriority(Integer.parseInt(priority));
            job.setState(state);
            if (uuid == null)
            {
                uuid = jobName;
            }
            job.setUuid(uuid);
            Timestamp ts = new Timestamp(System.currentTimeMillis());
            job.setCreateDate(ts);
            job.setTimestamp(ts);
            job.setJobType(jobType);
            long companyId = Long.parseLong(CompanyThreadLocal.getInstance()
                    .getValue());
            job.setCompanyId(companyId);

            HibernateUtil.save(job);

            long jobId = job.getId();
            String tuTable = BigTableUtil.decideTuWorkingTableForJobCreation(
                    companyId, jobId);
            String tuArchiveTable = BigTableUtil
                    .decideTuArchiveTableForJobCreation(companyId);
            String tuvTable = BigTableUtil.decideTuvWorkingTableForJobCreation(
                    companyId, jobId);
            String tuvArchiveTable = BigTableUtil
                    .decideTuvArchiveTableForJobCreation(companyId);
            String lmTable = BigTableUtil.decideLMWorkingTableForJobCreation(
                    companyId, jobId);
            String lmArchiveTable = BigTableUtil
                    .decideLMArchiveTableForJobCreation(companyId);
            String tuTuvAttrTable = "translation_tu_tuv_attr_" + companyId;
            String lmExtTable = BigTableUtil
					.decideLMExtWorkingTableForJobCreation(companyId, jobId);
			String lmExtArchiveTable = BigTableUtil
					.decideLMExtArchiveTableForJobCreation(companyId);
			job.setTuTable(tuTable);
            job.setTuArchiveTable(tuArchiveTable);
            job.setTuvTable(tuvTable);
            job.setTuvArchiveTable(tuvArchiveTable);
            job.setLmTable(lmTable);
            job.setLmArchiveTable(lmArchiveTable);
            job.setLmExtTable(lmExtTable);
            job.setLmExtArchiveTable(lmExtArchiveTable);
            HibernateUtil.saveOrUpdate(job);

            if (!DbUtil.isTableExisted(tuTable))
            {
                BigTableUtil.createTuTable(tuTable);
            }
            if (!DbUtil.isTableExisted(tuArchiveTable))
            {
                BigTableUtil.createTuTable(tuArchiveTable);
            }
            if (!DbUtil.isTableExisted(tuvTable))
            {
                BigTableUtil.createTuvTable(tuvTable);
            }
            if (!DbUtil.isTableExisted(tuvArchiveTable))
            {
                BigTableUtil.createTuvTable(tuvArchiveTable);
            }
            if (!DbUtil.isTableExisted(lmTable))
            {
                BigTableUtil.createLMTable(lmTable);
            }
            if (!DbUtil.isTableExisted(lmArchiveTable))
            {
                BigTableUtil.createLMTable(lmArchiveTable);
            }
            if (!DbUtil.isTableExisted(tuTuvAttrTable))
            {
            	BigTableUtil.createTuTuvAttrTable(tuTuvAttrTable);
            }
            if (!DbUtil.isTableExisted(lmExtTable))
            {
            	BigTableUtil.createLMExtTable(lmExtTable);
            }
            if (!DbUtil.isTableExisted(lmExtArchiveTable))
            {
            	BigTableUtil.createLMExtTable(lmExtArchiveTable);
            }
        }
        catch (Exception e)
        {
            c_logger.error("Error initializing new job: " + jobName, e);
            throw new JobCreationException(
                    JobCreationException.MSG_FAILED_TO_INITIALIZE_NEW_JOB,
                    null, e);
        }

        return job;
    }

    public static Job loadJobFromDB(long jobId)
    {
        return HibernateUtil.get(JobImpl.class, jobId);
    }

    /**
     * Loads real job from database.
     */
    public static Job refreshJobFromDB(long jobId)
    {
        // force to close session in order to get the latest job from database
        HibernateUtil.closeSession();
        return HibernateUtil.get(JobImpl.class, jobId);
    }

    /**
     * Updates the job state.
     */
    public static void updateJobState(long jobId, String state)
    {
        Job job = HibernateUtil.get(JobImpl.class, jobId);
        updateJobState(job, state);
    }

    /**
     * Updates the job state.
     */
    public static void updateJobState(Job job, String state)
    {
        try
        {
            job.setState(state);
            HibernateUtil.update(job);
        }
        catch (Exception e)
        {
            String[] args = new String[2];
            args[0] = String.valueOf(job.getId());
            args[1] = state;
            throw new JobCreationException(
                    JobCreationException.MSG_FAILED_TO_UPDATE_JOB_STATE, args,
                    e);
        }
    }

    /**
     * Updates the job to "EXPORTING" state according to workflows' states.
     */
    public static void updateJobStateToExporting(Job job)
    {
        Collection<Workflow> wfs = job.getWorkflows();
        for (Workflow wf : wfs)
        {
            if (!Workflow.EXPORTING.equals(wf.getState()))
            {
                return;
            }
        }
        updateJobState(job, Job.EXPORTING);
    }

    /**
     * Updates the workflow state.
     */
    public static void updateWorkflowState(Workflow wf, String state)
    {
        try
        {
            wf.setState(state);
            HibernateUtil.saveOrUpdate(wf);
        }
        catch (Exception e)
        {
            throw new WorkflowException(
                    WorkflowException.MSG_FAILED_TO_UPDATE_WORKFLOW_STATE,
                    new String[]
                    { state }, e);
        }
    }
}
