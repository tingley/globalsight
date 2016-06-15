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

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.globalsight.cxe.engine.util.FileUtils;
import com.globalsight.cxe.util.EventFlowXmlParser;
import com.globalsight.diplomat.util.database.ConnectionPool;
import com.globalsight.everest.comment.Comment;
import com.globalsight.everest.costing.CostingEngine;
import com.globalsight.everest.costing.Rate;
import com.globalsight.everest.foundation.BasicL10nProfile;
import com.globalsight.everest.foundation.L10nProfile;
import com.globalsight.everest.foundation.L10nProfileWFTemplateInfo;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.jobhandler.JobImpl;
import com.globalsight.everest.jobhandler.jobmanagement.JobDispatchEngine;
import com.globalsight.everest.page.TargetPage;
import com.globalsight.everest.permission.Permission;
import com.globalsight.everest.persistence.PersistenceException;
import com.globalsight.everest.persistence.tuv.BigTableUtil;
import com.globalsight.everest.projecthandler.MachineTranslationProfile;
import com.globalsight.everest.projecthandler.WorkflowTemplateInfo;
import com.globalsight.everest.request.Request;
import com.globalsight.everest.request.RequestImpl;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.taskmanager.TaskImpl;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.pagehandler.administration.mtprofile.MTProfileHandlerHelper;
import com.globalsight.everest.workflow.Activity;
import com.globalsight.everest.workflow.WfTaskInfo;
import com.globalsight.everest.workflow.WorkflowConstants;
import com.globalsight.everest.workflow.WorkflowInstance;
import com.globalsight.everest.workflow.WorkflowServer;
import com.globalsight.everest.workflow.WorkflowTaskInstance;
import com.globalsight.everest.workflowmanager.Workflow;
import com.globalsight.everest.workflowmanager.WorkflowImpl;
import com.globalsight.everest.workflowmanager.WorkflowOwner;
import com.globalsight.ling.tm2.persistence.DbUtil;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.AmbFileStoragePathUtils;
import com.globalsight.util.GeneralException;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.edit.EditUtil;

/**
 * This class handles the Rules for creating a job and adding requests to a job.
 * 
 * It also provides functionality for finding a pending job to add a request to.
 * <p>
 */
public class JobAdditionEngine
{
    private static final Logger c_logger = Logger
            .getLogger(JobAdditionEngine.class.getName());

    private static WorkflowServer c_wfServer = null;

    private static CostingEngine c_ce = null;

    public JobAdditionEngine() throws JobCreationException
    {
        lookupWFServer();
    }

    private void persistJobs(JobImpl job, RequestImpl request,
            List listOfWorkflows, Session session) throws PersistenceException
    {
        Map<GlobalSightLocale, Long> wfmap = new HashMap<GlobalSightLocale, Long>();
        try
        {
            BasicL10nProfile l10nProfile = (BasicL10nProfile) request
                    .getL10nProfile();
            long lpId = l10nProfile.getId();
            Set<WorkflowTemplateInfo> workflowTemplateInfos = l10nProfile
                    .getWorkflowTemplates();
            for (Iterator<WorkflowTemplateInfo> it = workflowTemplateInfos
                    .iterator(); it.hasNext();)
            {
                WorkflowTemplateInfo workflowInfo = (WorkflowTemplateInfo) it
                        .next();
                wfmap.put(workflowInfo.getTargetLocale(), workflowInfo.getId());
            }
            long companyId = request.getCompanyId();

            job.setCreateDate(new Timestamp(System.currentTimeMillis()));
            job.setPriority(request.getL10nProfile().getPriority());
            job.setIsWordCountReached(false);
            job.setTimestamp(new Timestamp(System.currentTimeMillis()));
            job.setPageCount(1);
            job.setCompanyId(companyId);
            request.setJob(job);
            if (PageHandler.isInContextMatch(request))
            {
                job.setLeverageOption(Job.IN_CONTEXT);
            }
            else
            {
                job.setLeverageOption(Job.EXACT_ONLY);
            }
            request.setTimestamp(new Timestamp(System.currentTimeMillis()));
            List<Request> requtests = new ArrayList<Request>();
            requtests.add(request);
            job.setRequestList(requtests);

            List<WorkflowImpl> workflows = new ArrayList<WorkflowImpl>();
            Iterator it = listOfWorkflows.iterator();
            while (it.hasNext())
            {
                WorkflowImpl workflow = (WorkflowImpl) it.next();
                workflow.setJob(job);
                workflow.setTimestamp(new Timestamp(System.currentTimeMillis()));
                workflow.setCompanyId(companyId);
                workflow.setPriority(job.getPriority());

                long wfId = (Long) wfmap.get(workflow.getTargetLocale());
                MachineTranslationProfile mtProfile = MTProfileHandlerHelper
                        .getMTProfileByRelation(lpId, wfId);
                boolean useMT = false;
                long mtConfidenceScore = 0;
                String mtProfileName = null;
                if (mtProfile != null && mtProfile.isActive())
                {
                    useMT = true;
                    mtConfidenceScore = mtProfile.getMtConfidenceScore();
                    mtProfileName = mtProfile.getMtProfileName();
                }
                workflow.setUseMT(useMT);
                workflow.setMtConfidenceScore((int) mtConfidenceScore);
                workflow.setMtProfileName(mtProfileName);

                workflows.add(workflow);

                // create the workflow owners for each workflow
                List wfOwners = workflow.getWorkflowOwners();

                for (int i = 0; i < wfOwners.size(); i++)
                {
                    WorkflowOwner wfo = (WorkflowOwner) wfOwners.get(i);
                    wfo.setWorkflow(workflow);
                }

                // go through all tasks for the workflow
                Collection tasks = workflow.getTasks().values();
                for (Iterator i = tasks.iterator(); i.hasNext();)
                {
                    TaskImpl t = (TaskImpl) i.next();
                    t.setStateStr("DEACTIVE");
                    t.setCompanyId(companyId);
                }
            }
            job.setWorkflowInstances(workflows);

            session.save(job);

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
            e.printStackTrace();
            throw new PersistenceException(e);
        }
    }

    /*
     * Create a new job with the specified state and job name. Add the request
     * to the job and create workflow instances.
     * 
     * @param p_request The request to add to the job. @param p_state The state
     * the new job should be in. @param p_jobName The name of the job (this can
     * be null, then a job name will be generated).
     */
    Job createNewJob(Request p_request, String p_state, String p_jobName,
            HashMap p_targetPages) throws JobCreationException
    {
        Session session = null;
        Transaction transaction = null;
        JobImpl newJob = null;
        List listOfWorkflows = null;
        if (p_jobName == null || p_jobName.length() == 0)
        {
            p_jobName = generateJobName(p_request);
        }
        try
        {
            session = HibernateUtil.getSession();
            transaction = session.beginTransaction();

            newJob = new JobImpl();
            String priority = p_request.getPriority();
            if (priority != null && !("null").equals(priority))
            {
                newJob.setPriority(Integer.parseInt(priority));
            }

            EventFlowXmlParser parser = new EventFlowXmlParser();
            parser.parse(p_request.getEventFlowXml());
            newJob.setCreateUserId(parser.getSourceImportInitiatorId());

            String uuid = parser.getJobUuid();
            if (uuid == null || uuid.length() == 0)
            {
                uuid = JobImpl.createUuid();
            }
            newJob.setUuid(uuid);

            newJob.setState(p_state);
            newJob.setJobName(EditUtil.removeCRLF(p_jobName));
            newJob.setLeverageMatchThreshold((int) p_request.getL10nProfile()
                    .getTranslationMemoryProfile().getFuzzyMatchThreshold());
            newJob.setSourceLocale(p_request.getL10nProfile().getSourceLocale());
            newJob.setCreateDate(new Date());
            listOfWorkflows = createWorkflowInstances(p_request, newJob);
            RequestImpl request = (RequestImpl) p_request;
            persistJobs(newJob, request, listOfWorkflows, session);

            // verify if there are target pages add them to the workflow(s)
            // if only error pages then target pages won't exist.
            if (p_targetPages != null && p_targetPages.size() > 0)
            {
                Collection c = p_targetPages.values();
                Iterator it = c.iterator();
                String hql = "from WorkflowImpl w where w.job.id = :jId "
                        + "and w.targetLocale.id = :tId";
                Map map = new HashMap();
                map.put("jId", newJob.getIdAsLong());
                try
                {
                    while (it.hasNext())
                    {
                        TargetPage tp = (TargetPage) it.next();
                        map.put("tId", new Long(tp.getLocaleId()));
                        WorkflowImpl w = (WorkflowImpl) HibernateUtil.search(
                                hql, map).get(0);
                        tp.setWorkflowInstance(w);
                        w.addTargetPage(tp);
                        tp.setCVSTargetModule(getTargetModule(tp));
                        tp.setTimestamp(new Timestamp(System
                                .currentTimeMillis()));
                        session.update(tp);
                        session.update(w);
                    }
                }
                catch (Exception e)
                {
                    c_logger.error("Error found in createJob.", e);
                }
            }

            transaction.commit();
        }
        catch (Exception e)
        {
            try
            {
                transaction.rollback();
                c_logger.error("Failed to create a new job for request "
                        + p_request.getId(), e);
                String args[] = new String[1];
                args[0] = Long.toString(p_request.getId());

                throw new JobCreationException(
                        JobCreationException.MSG_FAILED_TO_CREATE_NEW_JOB,
                        args, e);
            }
            catch (Exception sqle)
            {
                String args[] = new String[1];
                args[0] = Long.toString(p_request.getId());
                throw new JobCreationException(
                        JobCreationException.MSG_FAILED_TO_CREATE_NEW_JOB,
                        args, e);
            }
        }
        finally
        {
            if (session != null)
            {
                // session.close();
            }
        }

        try
        {
            getJobDispatchEngine().createDispatcher(newJob);
        }
        catch (Exception e)
        {
            long id = newJob == null ? -1 : newJob.getId();
            c_logger.error("Failed to create a dispatcher for the job " + id, e);
        }
        addJobNote(p_targetPages, newJob);

        return newJob;
    }

    /**
     * To add comment and attached file to a job according with target pages
     * 
     * @param p_targetPages
     *            Target pages
     * @param p_job
     *            Job entity which will be attached the comment
     */
    static void addJobNote(HashMap p_targetPages, JobImpl p_job)
    {
        // Add job notes for uploaded files if it had.
        if (p_targetPages != null && p_targetPages.size() > 0)
        {
            try
            {
                TargetPage targetPage = null;
                Collection c = p_targetPages.values();
                String source_locale_key = null;
                String externalPageId = null;
                int index = 0;
                boolean isFromDI = false;
                String temp = null;
                String uploadedJobName = null;
                File jobnotesFile, srcFolder, tarFolder;
                File[] files = null;
                String noteInfo = null;
                char token = ',';
                int index_1 = 0;
                int index_2 = 0;
                String userName = "";
                String date_str = "";
                String note = "";
                Comment comment = null;
                StringBuffer finalPath = null;

                Connection conn = null;
                PreparedStatement pstmt = null;
                PreparedStatement pstmt2 = null;
                ResultSet rs = null;

                for (Iterator iter = c.iterator(); iter.hasNext();)
                {
                    targetPage = (TargetPage) iter.next();
                    source_locale_key = targetPage.getSourcePage()
                            .getGlobalSightLocale().toString()
                            + "\\";
                    externalPageId = targetPage.getExternalPageId().replace(
                            '/', '\\');
                    index = externalPageId.indexOf(source_locale_key);
                    // check whether this job has probability of be uploaded
                    if (index == -1)
                    {
                        continue;
                    }
                    else
                    {
                        uploadedJobName = p_job.getJobName();
                        // If the 'uploadedJobName' is 'webservice', it means
                        // that the job is created by DI
                        if (externalPageId.indexOf("\\webservice\\") > 0)
                            isFromDI = true;
                        
                        jobnotesFile = new File(
                                AmbFileStoragePathUtils.getCxeDocDir(),
                                uploadedJobName + ".txt");
                        if (jobnotesFile.exists())
                        {
                            // read job note, content formart
                            // <userName>,<Date>,<note>
                            noteInfo = FileUtils.read(jobnotesFile, "utf-8");
                            token = ',';
                            index_1 = noteInfo.indexOf(token);
                            index_2 = noteInfo.indexOf(token, index_1 + 1);
                            userName = noteInfo.substring(0, index_1);
                            date_str = noteInfo.substring(index_1 + 1, index_2);
                            note = noteInfo.substring(index_2 + 1);

                            comment = ServerProxy.getCommentManager()
                                    .saveComment(p_job, p_job.getJobId(),
                                            userName, note,
                                            new Date(Long.parseLong(date_str)));

                            if (isFromDI)
                            {
                                // Process the attached file uploaded from DI
                                // Copy the attached file from CXE folder to
                                // CommentReference folder and delete the temp
                                // files
                                String srcf = AmbFileStoragePathUtils
                                        .getCxeDocDirPath(
                                                String.valueOf(p_job
                                                        .getCompanyId()))
                                        .concat(File.separator)
                                        .concat(p_job.getJobName());
                                srcFolder = new File(srcf);
                                if (srcFolder.isDirectory())
                                {
                                    finalPath = new StringBuffer(
                                            AmbFileStoragePathUtils
                                                    .getCommentReferenceDir()
                                                    .getAbsolutePath());
                                    finalPath.append(File.separator)
                                            .append(comment.getId())
                                            .append(File.separator)
                                            .append("General");
                                    tarFolder = new File(finalPath.toString());
                                    tarFolder.mkdirs();
                                    files = srcFolder.listFiles();
                                    for (File f : files)
                                    {
                                        FileUtils.copyFile(f, new File(
                                                tarFolder, f.getName()));
                                        f.delete();
                                    }
                                    srcFolder.delete();
                                }
                            }

                            jobnotesFile.delete();
                        }
                        try
                        {
                            // Update CVS files status
                            conn = ConnectionPool.getConnection();
                            conn.setAutoCommit(false);
                            String sql = "select * from cvs_source_files where status=1 and job_name = ?";
                            pstmt = conn.prepareStatement(sql);
                            pstmt.setString(1, uploadedJobName);
                            rs = pstmt.executeQuery();
                            if (rs.next())
                            {
                                // Change the status of uploaded cvs files
                                String sql2 = "update cvs_source_files set status=2, job_id=? where job_name=?";
                                pstmt2 = conn.prepareStatement(sql2);
                                pstmt2.setLong(1, p_job.getJobId());
                                pstmt2.setString(2, uploadedJobName);
                                pstmt2.executeUpdate();
                                conn.commit();
                            }
                        }
                        catch (SQLException se)
                        {
                            c_logger.error("Can NOT update CVS files status. ",
                                    se);
                        }
                        finally
                        {
                            try
                            {
                                if (pstmt != null)
                                    pstmt.close();

                                if (pstmt2 != null)
                                    pstmt2.close();

                                ConnectionPool.returnConnection(conn);
                            }
                            catch (Exception e)
                            {
                            }
                        }
                    }
                }
            }
            catch (Exception e)
            {
                // do nothing but write log, because this exception
                // is not important
                c_logger.info(
                        "Error when add "
                                + p_job.getJobName()
                                + " ("
                                + p_job.getJobId()
                                + ")"
                                + " job's notes (added when uploading) into GlobalSight DB",
                        e);
            }
        }
    }

    String generateJobName(Request p_request)
    {
        String jobName = new String();
        jobName += p_request.getL10nProfile().getName();
        jobName += " " + p_request.getDataSourceType();
        jobName += " " + p_request.getId();
        return jobName;
    }

    /*
     * Create the workflow instances that are part of the new job.
     */
	List<Workflow> createWorkflowInstances(Request p_request, JobImpl p_job)
			throws JobCreationException
    {
        GlobalSightLocale[] targetLocales = p_request
                .getTargetLocalesToImport();

        L10nProfile l10n = p_request.getL10nProfile();
        List<Workflow> listOfWorkflows = new ArrayList<Workflow>();
        try
        {
            for (int i = 0; i < targetLocales.length; i++)
            {
                WorkflowTemplateInfo wfInfo = l10n
                        .getWorkflowTemplateInfo(targetLocales[i]);
                L10nProfileWFTemplateInfo l10nProfileWFTemplateInfo = ServerProxy
                        .getProjectHandler().getL10nProfileWfTemplateInfo(
                                l10n.getId(), wfInfo.getId());
                // just make translation workflow instance
                if (!l10nProfileWFTemplateInfo.getIsActive())
                {
                    continue;
                }
                if ((!l10nProfileWFTemplateInfo.getIsActive())
                        && !wfInfo.getWorkflowType().equals(
                                WorkflowTemplateInfo.TYPE_TRANSLATION))
                {
                    continue;
                }
                long wfTemplateId = wfInfo.getWorkflowTemplateId();
                WorkflowInstance wfInstance = c_wfServer
                        .createWorkflowInstance(wfTemplateId);

                Workflow wf = new WorkflowImpl();
                wf.setWorkflowType(wfInfo.getWorkflowType());
                wf.setScorecardShowType(wfInfo.getScorecardShowType());
                wf.setId(wfInstance.getId());
                wf.setIflowInstance(wfInstance);
                wf.setState(Workflow.PENDING);
                wf.setTargetLocale(targetLocales[i]);
                wf.setDuration(calculateDuration(wfInstance));

                // set workflow owners (PM and WFM)
                wf.addWorkflowOwner(new WorkflowOwner(wfInfo
                        .getProjectManagerId(),
                        Permission.GROUP_PROJECT_MANAGER));

                List wfms = wfInfo.getWorkflowManagerIds();
                if (wfms != null)
                {
                    for (Iterator wfi = wfms.iterator(); wfi.hasNext();)
                    {
                        wf.addWorkflowOwner(new WorkflowOwner((String) wfi
                                .next(), Permission.GROUP_WORKFLOW_MANAGER));
                    }
                }

                // create the tasks and add them to the workflow
                createTasks(wf);
                listOfWorkflows.add(wf);
            }
        }
        catch (Exception e)
        {
            c_logger.error("Failed to create workflow instances for the new "
                    + "of request " + p_request.getId(), e);
            String args[] = new String[1];
            args[0] = Long.toString(p_request.getId());
            throw new JobCreationException(
                    JobCreationException.MSG_FAILED_TO_CREATE_WORKFLOW_INSTANCES,
                    args, e);

        }
        return listOfWorkflows;
    }

    /**
     * Create all the tasks of the workflow from the WorkflowTaskInstances. This
     * creates a task for each task within iflow - and provides a place to store
     * information in the System4 DB and not in iflow. Like rates, accept time,
     * complete time, hours to work on task, etc..
     */
    private void createTasks(Workflow p_wf)
    {
        Vector tasks = p_wf.getIflowInstance().getWorkflowInstanceTasks();
        for (int i = 0; i < tasks.size(); i++)
        {
            WorkflowTaskInstance wti = (WorkflowTaskInstance) tasks.get(i);
            // no need to create task for start, exit, and condition node.
            if (wti.getType() == WorkflowConstants.ACTIVITY)
            {
                TaskImpl task = new TaskImpl(p_wf);
                task.setId(wti.getTaskId());
                task.setName(wti.getActivityName());
                task.setType(getActivityType(task.getName()));
                task.setCompanyId(p_wf.getCompanyId());
                // set Task Type; Translation Task or DTP Task
                task.setTaskType(p_wf.getWorkflowType());
                task.setRateSelectionCriteria(wti.getRateSelectionCriteria());
                task.setIsReportUploadCheck(wti.getReportUploadCheck());
                task.setIsReportUploaded(0);
                // if an expense rate is specified
                if (wti.getExpenseRateId() > 0)
                {
                    try
                    {
                        Rate r = lookupCostingEngine().getRate(
                                wti.getExpenseRateId());
                        task.setExpenseRate(r);
                    }
                    catch (Exception e)
                    {
                        // couldn't find the rate so left to be null
                        c_logger.error("Couldn't find the expense rate for task "
                                + wti.getTaskId()
                                + " of workflow "
                                + p_wf.getId());
                    }
                }
                // if a revenuve rate is specified
                if (wti.getRevenueRateId() > 0)
                {
                    try
                    {
                        Rate r = lookupCostingEngine().getRate(
                                wti.getRevenueRateId());
                        task.setRevenueRate(r);
                    }
                    catch (Exception e)
                    {
                        // couldn't find the rate so left to be null
                        c_logger.error("Couldn't find the rate for task "
                                + wti.getTaskId() + " of workflow "
                                + p_wf.getId());
                    }
                }
                task.setIsUploading('N');// for GBS-1939
                p_wf.addTask(task);
            }

        }

    }

    /*
     * Calculate the duration of the workflow as number of 'minutes'.
     */
    private long calculateDuration(WorkflowInstance wfi)
    {
        long durationInMilli = 0;
        long minutes = 0;
        try
        {
            // -1 indicates that the default path would begin from the
            // START node.
            List wfTaskInfos = c_wfServer.timeDurationsInDefaultPath(
                    wfi.getId(), -1, null, wfi);

            for (Iterator it = wfTaskInfos.iterator(); it.hasNext();)
            {
                WfTaskInfo taskInfo = (WfTaskInfo) it.next();
                {
                    durationInMilli += taskInfo.getCompletionDuration();
                }
            }
            // convert the millisec to minutes since it's the smallest unit
            // of time used for each activity of a workflow.
            minutes = (long) durationInMilli / 60000L;

        }
        catch (Exception e)
        {
            // if this fails just flag an error - and leave the cost at 0
            c_logger.error(
                    "Failed to calculate the cost for workflow " + wfi.getId(),
                    e);
        }
        return minutes;
    }

    /*
     * Get the reference to the workflow server.
     */
    private void lookupWFServer() throws JobCreationException
    {
        try
        {
            if (c_wfServer == null)
                c_wfServer = ServerProxy.getWorkflowServer();
        }
        catch (GeneralException ge)
        {
            c_logger.error("Failed to lookup the Workflow server.", ge);
            throw new JobCreationException(
                    JobCreationException.MSG_FAILED_TO_FIND_WORKFLOW_SERVER,
                    null, ge);
        }
    }

    /**
     * Return the activity's type.
     */
    private int getActivityType(String p_activityName)
    {
        // default
        int type = TaskImpl.TYPE_TRANSLATE;
        try
        {
            Activity act = ServerProxy.getJobHandler().getActivity(
                    p_activityName);
            type = act.getType();

            // for sla report issue
            if ((type == Activity.TYPE_REVIEW) && act.getIsEditable())
            {
                type = TaskImpl.TYPE_REVIEW_EDITABLE;
            }
        }
        catch (Exception e)
        {
            // do nothing just return the default
        }
        return type;
    }

    /*
     * Get the reference to the costing engine.
     */
    private CostingEngine lookupCostingEngine() throws JobCreationException
    {
        try
        {
            if (c_ce == null)
                c_ce = ServerProxy.getCostingEngine();
            return c_ce;
        }
        catch (GeneralException ge)
        {
            c_logger.error("Failed to lookup the Costing Engine.", ge);
            throw new JobCreationException(
                    JobCreationException.MSG_FAILED_TO_FIND_COSTING_ENGINE,
                    null, ge);
        }
    }

    /**
     * Get the reference to the job dispatcher.
     */
    private JobDispatchEngine getJobDispatchEngine()
            throws JobCreationException
    {
        JobDispatchEngine jobDispatchEngine = null;
        try
        {
            jobDispatchEngine = ServerProxy.getJobDispatchEngine();
        }
        catch (Exception e)
        {
            c_logger.error("Unable to retrieve JobDispatch Engine", e);
            throw new JobCreationException(
                    JobCreationException.MSG_FAILED_TO_FIND_JOB_DISPATCHER,
                    null, e);
        }
        return jobDispatchEngine;
    }

    String getTargetModule(TargetPage p_tp)
    {
        if (p_tp == null)
        {
            return "";
        }
        Connection conn = null;
        Statement stmt = null;
        try
        {
            String sourceLocale = p_tp.getSourcePage().getGlobalSightLocale()
                    .toString();
            String targetLocale = p_tp.getGlobalSightLocale().toString();
            String jobName = String.valueOf(p_tp.getWorkflowInstance().getJob().getId());

            // get "sourceModule"
            String tmp = p_tp.getExternalPageId();
            tmp = tmp.replace("\\", "/");
            int fileNameIndex = tmp.lastIndexOf("/") + 1;
            tmp = tmp.substring(0, fileNameIndex);
            int index = tmp.indexOf(jobName) + jobName.length() + 1;
            tmp = tmp.substring(index);
            String sourceModule = tmp.substring(tmp.indexOf("/"));

            conn = DbUtil.getConnection();
            stmt = conn.createStatement();
            StringBuffer sql = new StringBuffer();
            sql.append(
                    "select * from module_mapping where is_active='1' and source_locale='")
                    .append(sourceLocale).append("' and target_locale='");
            sql.append(targetLocale).append("' and source_module='")
                    .append(sourceModule).append("'");
            ResultSet rs = stmt.executeQuery(sql.toString());
            if (rs.next())
                return rs.getString("Target_Module");
            else
                return "";
        }
        catch (Exception se)
        {
            return "";
        }
        finally
        {
        	DbUtil.silentClose(stmt);
            DbUtil.silentReturnConnection(conn);
        }
    }
}
