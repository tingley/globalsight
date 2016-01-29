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

import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.TimeZone;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.jms.JMSException;
import javax.naming.NamingException;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.jbpm.JbpmContext;
import org.jbpm.graph.def.Node;
import org.jbpm.graph.exe.ProcessInstance;
import org.jbpm.taskmgmt.exe.TaskInstance;

import com.globalsight.calendar.BaseFluxCalendar;
import com.globalsight.calendar.CalendarManager;
import com.globalsight.calendar.FluxCalendar;
import com.globalsight.calendar.ReservedTime;
import com.globalsight.calendar.UserFluxCalendar;
import com.globalsight.cxe.adapter.documentum.DocumentumOperator;
import com.globalsight.cxe.entity.fileprofile.FileProfile;
import com.globalsight.cxe.util.EventFlowXmlParser;
import com.globalsight.everest.comment.CommentException;
import com.globalsight.everest.comment.CommentFile;
import com.globalsight.everest.comment.CommentImpl;
import com.globalsight.everest.comment.CommentManager;
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.company.MultiCompanySupportedThread;
import com.globalsight.everest.corpus.CorpusManagerWLRemote;
import com.globalsight.everest.costing.AmountOfWork;
import com.globalsight.everest.costing.CostingEngine;
import com.globalsight.everest.costing.Rate;
import com.globalsight.everest.coti.util.COTIUtilEnvoy;
import com.globalsight.everest.edit.offline.AmbassadorDwUpConstants;
import com.globalsight.everest.edit.offline.AmbassadorDwUpException;
import com.globalsight.everest.edit.offline.OEMProcessStatus;
import com.globalsight.everest.edit.offline.OfflineEditManager;
import com.globalsight.everest.edit.offline.download.DownloadParams;
import com.globalsight.everest.edit.offline.download.JobPackageZipper;
import com.globalsight.everest.foundation.EmailInformation;
import com.globalsight.everest.foundation.L10nProfile;
import com.globalsight.everest.foundation.Timestamp;
import com.globalsight.everest.integration.ling.LingServerProxy;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.jobhandler.JobException;
import com.globalsight.everest.jobhandler.JobImpl;
import com.globalsight.everest.jobhandler.jobcreation.JobCreationMonitor;
import com.globalsight.everest.page.DataSourceType;
import com.globalsight.everest.page.Page;
import com.globalsight.everest.page.PageState;
import com.globalsight.everest.page.PageStateValidator;
import com.globalsight.everest.page.PrimaryFile;
import com.globalsight.everest.page.SourcePage;
import com.globalsight.everest.page.TargetPage;
import com.globalsight.everest.page.pageexport.ExportBatchEvent;
import com.globalsight.everest.page.pageexport.ExportConstants;
import com.globalsight.everest.page.pageexport.ExportEventObserverHelper;
import com.globalsight.everest.page.pageexport.ExportParameters;
import com.globalsight.everest.permission.Permission;
import com.globalsight.everest.permission.PermissionSet;
import com.globalsight.everest.persistence.tuv.SegmentTuTuvIndexUtil;
import com.globalsight.everest.persistence.tuv.SegmentTuUtil;
import com.globalsight.everest.persistence.tuv.SegmentTuvUtil;
import com.globalsight.everest.persistence.tuv.TuvQueryConstants;
import com.globalsight.everest.projecthandler.Project;
import com.globalsight.everest.projecthandler.WorkflowTemplateInfo;
import com.globalsight.everest.projecthandler.WorkflowTypeConstants;
import com.globalsight.everest.request.Request;
import com.globalsight.everest.secondarytargetfile.SecondaryTargetFile;
import com.globalsight.everest.secondarytargetfile.SecondaryTargetFileState;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.taskmanager.Task;
import com.globalsight.everest.taskmanager.TaskImpl;
import com.globalsight.everest.taskmanager.TaskInfo;
import com.globalsight.everest.tuv.TaskTuv;
import com.globalsight.everest.tuv.Tuv;
import com.globalsight.everest.tuv.TuvImpl;
import com.globalsight.everest.tuv.TuvState;
import com.globalsight.everest.usermgr.UserManager;
import com.globalsight.everest.util.jms.JmsHelper;
import com.globalsight.everest.util.system.SystemConfigParamNames;
import com.globalsight.everest.util.system.SystemConfiguration;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.javabean.TaskInfoBean;
import com.globalsight.everest.webapp.pagehandler.administration.company.CompanyRemoval;
import com.globalsight.everest.webapp.pagehandler.administration.users.UserHandlerHelper;
import com.globalsight.everest.webapp.pagehandler.administration.users.UserUtil;
import com.globalsight.everest.webapp.pagehandler.offline.OfflineConstants;
import com.globalsight.everest.webapp.pagehandler.offline.download.SendDownloadFileHelper;
import com.globalsight.everest.webapp.pagehandler.projects.l10nprofiles.LocProfileStateConstants;
import com.globalsight.everest.webapp.pagehandler.projects.workflows.JobDataMigration;
import com.globalsight.everest.webapp.pagehandler.tasks.TaskHelper;
import com.globalsight.everest.workflow.Activity;
import com.globalsight.everest.workflow.EventNotificationHelper;
import com.globalsight.everest.workflow.SkipActivityVo;
import com.globalsight.everest.workflow.SystemAction;
import com.globalsight.everest.workflow.SystemActionPerformer;
import com.globalsight.everest.workflow.TaskEmailInfo;
import com.globalsight.everest.workflow.WfTaskInfo;
import com.globalsight.everest.workflow.WorkflowConfiguration;
import com.globalsight.everest.workflow.WorkflowConstants;
import com.globalsight.everest.workflow.WorkflowHelper;
import com.globalsight.everest.workflow.WorkflowInstance;
import com.globalsight.everest.workflow.WorkflowInstanceInfo;
import com.globalsight.everest.workflow.WorkflowJbpmPersistenceHandler;
import com.globalsight.everest.workflow.WorkflowJbpmUtil;
import com.globalsight.everest.workflow.WorkflowMailerConstants;
import com.globalsight.everest.workflow.WorkflowNodeParameter;
import com.globalsight.everest.workflow.WorkflowServerWLRemote;
import com.globalsight.everest.workflow.WorkflowTaskInstance;
import com.globalsight.ling.inprogresstm.InProgressTmManager;
import com.globalsight.ling.tm2.persistence.DbUtil;
import com.globalsight.ling.tw.PseudoConstants;
import com.globalsight.log.GlobalSightCategory;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.scheduling.SchedulerConstants;
import com.globalsight.util.AmbFileStoragePathUtils;
import com.globalsight.util.Entry;
import com.globalsight.util.GeneralException;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.ProcessRunner;
import com.globalsight.util.mail.MailerConstants;
import com.globalsight.util.modules.Modules;
import com.globalsight.util.zip.ZipIt;

public class WorkflowManagerLocal implements WorkflowManager
{
    //
    // PRIVATE CONSTANTS
    //
    private static String ACTIVE_NODE_ID = "activeNodeId";

    private static String REASSIGNED_NODE_ID = "reassignedNodeId";

    private static String JOBCREATION_SCRIPT_LOG = null;

    private static String JOBCREATION_SCRIPT_ERR_LOG = null;

    private static final String WF_IMPORT_FAILED = Workflow.IMPORT_FAILED;

    private static final String WF_READY = Workflow.READY_TO_BE_DISPATCHED;

    private static final String WF_DISPATCHED = Workflow.DISPATCHED;

    private static final String WF_LOCALIZED = Workflow.LOCALIZED;

    private static final String WF_EXPORTING = Workflow.EXPORTING;

    private static final String WF_SKIPPING = Workflow.SKIPPING;

    private static final String WF_EXPORTED = Workflow.EXPORTED;

    private static final String WF_ARCHIVED = Workflow.ARCHIVED;

    private static final String WF_CANCELLED = Workflow.CANCELLED;

    private static final String PG_ACTIVE_JOB = PageState.ACTIVE_JOB;

    private static final String PG_LOCALIZED = PageState.LOCALIZED;

    private static final String PG_NOT_LOCALIZED = PageState.NOT_LOCALIZED;

    private static final String PG_EXPORTED = PageState.EXPORTED;

    private static final String PG_OUT_OF_DATE = PageState.OUT_OF_DATE;

    private static final String PG_IMPORT_FAIL = PageState.IMPORT_FAIL;

    private static int ADVANCE_ACTION = 0;

    private static int DISPATCH_ACTION = 1;

    private static int MODIFY_ACTION = 2;

    public static final int LOCALIZED_STATE = 3;

    public static final int EXPORTING_STATE = 4;
    
    private static final int MAX_THREAD = 5;

    public static final String[] ORDERED_STATES =
    { WF_IMPORT_FAILED, WF_READY, WF_DISPATCHED, WF_LOCALIZED, WF_EXPORTING,
            WF_SKIPPING, WF_EXPORTED, WF_ARCHIVED };

    private static final Logger s_logger = Logger
            .getLogger(WorkflowManagerLocal.class.getName());

    private int m_isNotificationActive = -1;
    private Float m_threshold = null;

    static
    {
        initParas();
    }

    // determines whether the system-wide notification is enabled
    private boolean m_systemNotificationEnabled = EventNotificationHelper
            .systemNotificationEnabled();

    public WorkflowManagerLocal() throws WorkflowManagerException
    {
    }

    private static void initParas()
    {
        try
        {
            // set the log directory for the error files
            String logDirectory = SystemConfiguration.getInstance()
                    .getStringParameter(
                            SystemConfigParamNames.SYSTEM_LOGGING_DIRECTORY);
            // set the absolute path
            JOBCREATION_SCRIPT_LOG = logDirectory + "/jobCreationScript.log";
            JOBCREATION_SCRIPT_ERR_LOG = logDirectory
                    + "/jobCreationScript.err";
        }
        catch (Exception e)
        {
            s_logger.warn(
                    "The log directory couldn't be found in the system configuration "
                            + " for JobCreation logging purposes.", e);
        }
    }

    /**
     * Get a list of workflow objects based on a particular workflow id.
     * <p>
     * 
     * @param p_workflowId
     *            - The id of the workflow.
     * @return A vector of only ONE workflow (always a vector is returned since
     *         TOPLink is unaware of querying for one or more objects.)
     * @exception JobException
     *                Component related exception.
     * @exception java.rmi.RemoteException
     *                Network related exception.
     */
    public Workflow getWorkflowById(long p_workflowId) throws RemoteException,
            WorkflowManagerException
    {
        Workflow wf = null;
        try
        {
            wf = HibernateUtil.get(WorkflowImpl.class, p_workflowId);
        }
        catch (Exception e)
        {
            s_logger.error(e);
            String[] args = new String[1];
            args[0] = new Long(p_workflowId).toString();
            throw new WorkflowManagerException(
                    WorkflowManagerException.MSG_FAILED_TO_GET_WORKFLOW_BY_ID,
                    args, e, WorkflowManagerException.PROPERTY_FILE_NAME);

        }
        return wf;
    }

    /**
     * Get a list of workflow objects based on a particular workflow id. Also
     * ensures that the workflow has a pointer to its WorkflowInstance.
     * <p>
     * 
     * @param p_workflowId
     *            - The id of the workflow.
     * @exception JobException
     *                Component related exception.
     * @exception java.rmi.RemoteException
     *                Network related exception.
     */
    // TODO Is the difference between this and getWorkflowById significant?
    // Can they be combined?
    public Workflow getWorkflowByIdRefresh(long p_workflowId)
            throws RemoteException, WorkflowManagerException
    {
        Workflow wf = null;
        try
        {
            wf = getWorkflowById(p_workflowId);
            refreshWorkflowInstance(wf);
        }
        catch (Exception e)
        {
            String[] args = new String[1];
            args[0] = new Long(p_workflowId).toString();
            throw new WorkflowManagerException(
                    WorkflowManagerException.MSG_FAILED_TO_GET_WORKFLOW_BY_ID,
                    args, e, WorkflowManagerException.PROPERTY_FILE_NAME);

        }
        return wf;
    }

    /**
     * @see WorkflowManager.cancel(String, Workflow)
     */
    public void cancel(String p_idOfUserRequestingCancel, Workflow p_workflow)
            throws RemoteException, WorkflowManagerException
    {
        String nameOfUserRequestingCancel = UserUtil
                .getUserNameById(p_idOfUserRequestingCancel);
        if (allowedToCancelWorkflows(p_idOfUserRequestingCancel))
        {
            validateStateOfPages(p_workflow);

            Workflow wf = null;
            Session session = HibernateUtil.getSession();
            Transaction tx = session.beginTransaction();

            try
            {
                wf = (Workflow) session.get(WorkflowImpl.class,
                        p_workflow.getIdAsLong());
                
                boolean isDispatched = WF_DISPATCHED.equals(wf.getState());
                String oldWorkflowState = wf.getState();
                wf.setState(WF_CANCELLED);

                JobImpl job = (JobImpl) wf.getJob();
                String oldJobState = job.getState();
                resetJobState(session, job, job.getWorkflows());
                session.saveOrUpdate(job);
                session.saveOrUpdate(wf);

                tx.commit();

                ArrayList msg = new ArrayList();
                msg.add(p_workflow.getIdAsLong());
                msg.add(isDispatched);
                msg.add(oldJobState);
                msg.add(oldWorkflowState);
                JmsHelper.sendMessageToQueue(msg,
                        JmsHelper.JMS_CANCEL_WORKFLOW_QUEUE);

                s_logger.info("Workflow " + p_workflow.getId()
                        + " was cancelled by user "
                        + nameOfUserRequestingCancel);
            }
            catch (Exception we)
            {
                tx.rollback();
                s_logger.error("WorkflowManagerLocal::cancel(Workflow)", we);
                String[] args = new String[1];
                args[0] = new Long(p_workflow.getId()).toString();
                throw new WorkflowManagerException(
                        WorkflowManagerException.MSG_FAILED_TO_CANCEL_WORKFLOW,
                        args, we);
            }
        }
        else
        {
            s_logger.error("The user " + nameOfUserRequestingCancel
                    + " doesn't have the permission to cancel the workflow.");
            String[] args =
            { Long.toString(p_workflow.getId()), nameOfUserRequestingCancel };
            throw new WorkflowManagerException(
                    WorkflowManagerException.MSG_FAILED_TO_CANCEL_USER_NOT_ALLOWED,
                    args, null);
        }
    }

    /**
     * For GBS-495, "Discard" Job(s) took long time to complete especially if
     * the job contain large files or too many files.
     * 
     * @param p_job
     * @param reimport
     */
    @SuppressWarnings("unchecked")
    private void cancel(Job p_job, boolean reimport)
    {
        JobImpl job = HibernateUtil.get(JobImpl.class, new Long(p_job.getId()));
        String oldState = job.getState();
        job.setState(Job.CANCELLED);
        HibernateUtil.saveOrUpdate(job);

        ArrayList msg = new ArrayList();
        msg.add(job.getIdAsLong());
        msg.add(oldState);
        msg.add(reimport);

        try
        {
            JmsHelper.sendMessageToQueue(msg, JmsHelper.JMS_CANCEL_JOB_QUEUE);
        }
        catch (JMSException e)
        {
            job.setState(oldState);
            HibernateUtil.saveOrUpdate(job);
            s_logger.error(e.getMessage(), e);
        }
        catch (NamingException e)
        {
            s_logger.error(e.getMessage(), e);
        }
    }

    /**
     * @see WorkflowManager.cancel(String, Job, String)
     */
    public void cancel(String p_idOfUserRequestingCancel, Job p_job,
            String p_state) throws RemoteException, WorkflowManagerException
    {
        boolean reimport = false;
        cancel(p_idOfUserRequestingCancel, p_job, p_state, reimport);
    }

    /**
     * @see WorkflowManager.cancel(String, Job, String, boolean)
     */
    public void cancel(String p_idOfUserRequestingCancel, Job p_job,
            String p_state, boolean p_reimport) throws RemoteException,
            WorkflowManagerException
    {
        if (p_state == null)
        {
            cancel(p_job, p_reimport);
            return;
        }

        Session session = HibernateUtil.getSession();
        Transaction tx = session.beginTransaction();

        String nameOfUserRequestingCancel = UserUtil
                .getUserNameById(p_idOfUserRequestingCancel);
        if (allowedToCancelJobs(p_idOfUserRequestingCancel))
        {
            validateStateOfPagesInJob(p_job);
            try
            {
                Collection<Workflow> wfs = new ArrayList<Workflow>();
                Iterator<Workflow> it = p_job.getWorkflows().iterator();
                Object[] tasks = null;
                List<Object[]> taskList = new ArrayList<Object[]>();
                while (it.hasNext())
                {
                    Workflow wf = (Workflow) it.next();
                    
                    boolean updateIFlow = wf.getState().equals(
                            Workflow.DISPATCHED);

                    // if the states are equal - or no state was specified so
                    // cancel all workflows
                    if (p_state == null
                            || p_state.equals(wf.getState())
                            || (p_state.equals(Job.PENDING) && wf.getState()
                                    .equals(Workflow.IMPORT_FAILED)))
                    {
                        Workflow wfClone = (Workflow) session.get(
                                WorkflowImpl.class, wf.getIdAsLong());
                        wfClone.setState(WF_CANCELLED);

                        // only update the target page state if not LOCALIZED or
                        // EXPORTED yet
                        if ((wf.getState().equals(Workflow.PENDING))
                                || (wf.getState()
                                        .equals(Workflow.IMPORT_FAILED))
                                || (wf.getState()
                                        .equals(Workflow.READY_TO_BE_DISPATCHED))
                                || (wf.getState().equals(Workflow.DISPATCHED))
                                || (wf.getState()
                                        .equals(Workflow.BATCHRESERVED)))
                        {
                            updatePageState(session, wfClone.getTargetPages(),
                                    PG_NOT_LOCALIZED);
                            // also update the secondary target files (if any)
                            updateSecondaryTargetFileState(session,
                                    wfClone.getSecondaryTargetFiles(),
                                    SecondaryTargetFileState.CANCELLED);
                        }
                        if (updateIFlow)
                        {

                            Map activeTasks = getWFServer()
                                    .getActiveTasksForWorkflow(wf.getId());
                            if (activeTasks != null)
                            {
                                tasks = activeTasks.values().toArray();
                                taskList.add(tasks);
                                updateTaskState(session, tasks,
                                        wfClone.getTasks(), Task.STATE_DEACTIVE);

                                removeReservedTimes(tasks);
                            }
                            WorkflowTemplateInfo wfti = p_job.getL10nProfile()
                                    .getWorkflowTemplateInfo(
                                            wfClone.getTargetLocale());

                            TaskEmailInfo emailInfo = new TaskEmailInfo(
                                    p_job.getL10nProfile().getProject()
                                            .getProjectManagerId(),
                                    wf.getWorkflowOwnerIdsByType(Permission.GROUP_WORKFLOW_MANAGER),
                                    wfti.notifyProjectManager(), p_job
                                            .getPriority());
                            emailInfo.setJobName(p_job.getJobName());
                            emailInfo.setProjectIdAsLong(new Long(p_job
                                    .getL10nProfile().getProjectId()));
                            emailInfo.setSourceLocale(wfClone.getJob()
                                    .getSourceLocale().toString());
                            emailInfo.setTargetLocale(wfClone.getTargetLocale()
                                    .toString());
                            emailInfo.setCompanyId(String.valueOf(p_job
                                    .getCompanyId()));

                            getWFServer().suspendWorkflow(wfClone.getId(),
                                    emailInfo);
                        }
                        wfs.add(wfClone);
                        session.saveOrUpdate(wfClone);
                    }
                    else
                    {
                        wfs.add(wf);
                    }
                }

                JobImpl jobClone = (JobImpl) session.get(JobImpl.class,
                        new Long(p_job.getId()));
                String lastJobState = resetJobState(session, jobClone, wfs,
                        p_reimport);
                session.saveOrUpdate(jobClone);
                

                tx.commit();

                // for gbs-1302, cancel interim activities
                // TaskInterimPersistenceAccessor.cancelInterimActivities(taskList);

                if (Job.CANCELLED.equals(lastJobState))
                {
                    // cleanCorpus(jobId);
                    // deleteInProgressTmData(jobClone);
                    // GBS-2915, discard a job to remove all job data
                    CompanyRemoval removal = new CompanyRemoval(
                            jobClone.getCompanyId());
                    removal.removeJob(jobClone);
                }

                // remove all export batch events for this job
                // BB 7/9/03 Quick fix: This is disabled because, for an EXPORT
                // SOURCE, it
                // prevents the batch from from being updated (during export)
                // and the export email from being sent. Right now all events
                // are automatically removed after completion. In the future
                // if we enable the history preservation, we need
                // to revisit this issue. Source export history should be
                // removed after the e-mail.
                // ServerProxy.getExportEventObserver()
                // .removeExportBatchEvents(p_job.getId());

                s_logger.info("Job " + p_job.getId()
                        + " was cancelled by user "
                        + nameOfUserRequestingCancel);
            }
            catch (Exception e)
            {
                tx.rollback();
                s_logger.error("WorkflowManagerLocal::cancel(Job)", e);
                String[] args = new String[1];
                args[0] = Long.toString(p_job.getId());
                throw new WorkflowManagerException(
                        WorkflowManagerException.MSG_FAILED_TO_CANCEL_WORKFLOW,
                        args, e, WorkflowManagerException.PROPERTY_FILE_NAME);

            }
        }
        else
        {
            s_logger.error("User " + nameOfUserRequestingCancel
                    + " doesn't have permissions to cancel job "
                    + p_job.getId() + " so the cancellation failed.");
            String[] args =
            { Long.toString(p_job.getId()), nameOfUserRequestingCancel };
            throw new WorkflowManagerException(
                    WorkflowManagerException.MSG_FAILED_TO_CANCEL_USER_NOT_ALLOWED,
                    args, null);
        }
    }

    /**
     * This method allows a client to archive a single workflow
     * 
     * @param String
     *            Workflow p_workflow
     * @throws RemoteException
     *             , WorkflowManagerException
     */
    public void archiveWorkflow(Workflow p_workflow) throws RemoteException,
            WorkflowManagerException
    {
        Session session = HibernateUtil.getSession();
        Transaction tx = session.beginTransaction();
        boolean canMigrateJobData = false;
        Job job = null;
        try
        {
            job = (JobImpl) p_workflow.getJob();
            p_workflow.setState(WF_ARCHIVED);
            // If all workflows are in "archived" state...
            if (workflowsHaveState(job.getWorkflows(), WF_ARCHIVED))
            {
                job.setState(WF_ARCHIVED);
                canMigrateJobData = true;
            }
            session.saveOrUpdate(p_workflow);
            session.saveOrUpdate(job);
            tx.commit();
        }
        catch (Exception e)
        {
            tx.rollback();
            throw new WorkflowManagerException(
                    WorkflowManagerException.MSG_FAILED_TO_ARCHIVE_WORKFLOW,
                    null, e);
        }

        if (canMigrateJobData)
        {
            // Migrate data for this job here.
            try
            {
                JobDataMigration.migrateJobData(job);
            }
            catch (Exception e)
            {

            }

            deleteFolderForDI(job.getCompanyId(), job.getJobId());
        }
    }

    public boolean archive(Job p_job) throws RemoteException,
            WorkflowManagerException
    {
        Session session = HibernateUtil.getSession();
        Transaction tx = session.beginTransaction();
        boolean canMigrateJobData = false;
        try
        {
            for (Workflow wf : p_job.getWorkflows())
            {
                String wfState = wf.getState();
                if (wfState.equals(WF_EXPORTED))
                {
                    wf.setState(WF_ARCHIVED);
                    session.saveOrUpdate(wf);
                }
            }
            if (workflowsHaveState(p_job.getWorkflows(), WF_ARCHIVED))
            {
                p_job.setState(WF_ARCHIVED);
                canMigrateJobData = true;
                session.saveOrUpdate(p_job);
            }

            tx.commit();
        }
        catch (Exception e2)
        {
            tx.rollback();
            s_logger.error("WorkflowManagerLocal::archive(Job)", e2);
            String[] args = new String[1];
            args[0] = new Long(p_job.getId()).toString();
            throw new WorkflowManagerException(
                    WorkflowManagerException.MSG_FAILED_TO_ARCHIVE_WORKFLOW,
                    args, e2, WorkflowManagerException.PROPERTY_FILE_NAME);
        }

        if (canMigrateJobData)
        {
            // Migrate data for this job here.
            // The migration "CAN" fail, it does not impact normal features.
            try
            {
                JobDataMigration.migrateJobData(p_job);
            }
            catch (SQLException e)
            {

            }

            deleteFolderForDI(p_job.getCompanyId(), p_job.getJobId());
        }
        return canMigrateJobData;
    }

    /**
     * Delete folder
     * "[fileStore]\[companyName]\GlobalSight\DesktopIcon\exported\[jobID]" when
     * job is archived.
     * 
     * This is for GBS-3652 and "getDownloadableJobs()" webservice API.
     */
    private void deleteFolderForDI(long companyId, long jobId)
    {
        File diExportedDir = AmbFileStoragePathUtils
                .getDesktopIconExportedDir(companyId);
        File jobDir = new File(diExportedDir, String.valueOf(jobId));
        if (jobDir.exists())
        {
            jobDir.delete();
        }
    }

    /**
     * This method allows a client to dispatch a workflow
     * 
     * @param String
     *            Workflow p_workflow
     * @throws RemoteException
     *             , WorkflowManagerException
     */
    public void dispatch(Workflow p_workflow) throws RemoteException,
            WorkflowManagerException
    {
        Session session = HibernateUtil.getSession();
        Transaction tx = session.beginTransaction();
        JbpmContext ctx = null;

        try
        {
            if (s_logger.isDebugEnabled())
            {
                s_logger.debug("dispatch " + p_workflow.getId());
            }

            Workflow wfClone = (Workflow) session.get(WorkflowImpl.class,
                    p_workflow.getIdAsLong());

            long taskId = -1;
            String actionType = null;

            if (WF_READY.equals(wfClone.getState())
                    || Workflow.PENDING.equals(wfClone.getState()))
            {
                Job j = wfClone.getJob();
                TaskEmailInfo emailInfo = createTaskEmailInfo(j, wfClone);
                String pm = emailInfo.getProjectManagerId();

                ArrayList returnValue = dispatchWorkflow(wfClone, session,
                        new Date(), emailInfo);

                taskId = ((Long) (returnValue).get(0)).longValue();
                actionType = returnValue.get(3) != null ? (String) returnValue
                        .get(3) : null;

                // For sla issue
                if (wfClone.isEstimatedTranslateCompletionDateOverrided())
                {
                    updateEstimatedTranslateCompletionDate(wfClone.getId(),
                            wfClone.getEstimatedTranslateCompletionDate());
                }

                possiblyUpdateJob(session, wfClone, WF_DISPATCHED);
                session.saveOrUpdate(wfClone);
                tx.commit();

                if (((Boolean) returnValue.get(1)).booleanValue())
                {
                    exportForStfCreation(new Long(taskId), wfClone, pm);
                }
                // GBS-3002
                if (actionType != null)
                {
                    SystemActionPerformer.perform(actionType, taskId, pm);
                }

                Task task = (Task) wfClone.getTasks().get(taskId);
                long jobId = task.getJobId();
                L10nProfile l10nProfile = ServerProxy.getJobHandler()
                        .getL10nProfileByJobId(jobId);
                long wfStatePostId = l10nProfile.getWfStatePostId();
                if (wfStatePostId != -1)
                {
                    WfStatePostThread myTask = new WfStatePostThread(task, null, true);
                    Thread t = new MultiCompanySupportedThread(myTask);
                    t.start();
                }

                if (task != null)
                {
                    task.setProjectManagerName(pm);
                    TaskHelper.autoAcceptTask(task);
                }
            }
        }
        catch (Exception e2)
        {
            if (tx != null && tx.isActive())
            {
                tx.rollback();
            }
            s_logger.error(
                    "Failed to dispatch workflow: " + p_workflow.getId()
                            + " p_workflow="
                            + WorkflowHelper.toDebugString(p_workflow), e2);
            String[] args = new String[1];
            args[0] = new Long(p_workflow.getId()).toString();
            throw new WorkflowManagerException(
                    WorkflowManagerException.MSG_FAILED_TO_DISPATCH_WORKFLOW,
                    args, e2, WorkflowManagerException.PROPERTY_FILE_NAME);
        }
        finally
        {
            if (ctx != null)
            {
                ctx.close();
            }
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private DownloadParams getDownloadParams(Task task, Job p_job)
            throws GeneralException, NamingException, IOException
    {
        SendDownloadFileHelper help = new SendDownloadFileHelper();
        int fileFormat = AmbassadorDwUpConstants.DOWNLOAD_FILE_FORMAT_XLF;
        int editorId = AmbassadorDwUpConstants.EDITOR_XLIFF;
        String encoding = OfflineConstants.ENCODING_DEFAULT;
        int ptagFormat = PseudoConstants.PSEUDO_COMPACT;
        int platformId = AmbassadorDwUpConstants.PLATFORM_WIN32;
        int resInsMode = AmbassadorDwUpConstants.MAKE_RES_ATNS;
        String displayExactMatch = OfflineConstants.DISPLAY_EXACT_MATCH_YES;
        String consolidate = "false";

        List pageIdList = new ArrayList();
        List pageNameList = new ArrayList();
        List<Boolean> canUseUrlList = new ArrayList<Boolean>();
        List pages = task.getSourcePages();

        for (Iterator it = pages.iterator(); it.hasNext();)
        {
            SourcePage page = (SourcePage) it.next();
            pageIdList.add(new Long(page.getId()));
            pageNameList.add(page.getExternalPageId());
        }

        if (pageIdList != null)
        {
            for (int i = 0; i < pageIdList.size(); i++)
            {
                canUseUrlList.add(Boolean.FALSE);
            }
        }

        long workflowId = task.getWorkflow().getId();
        L10nProfile l10nProfile = task.getWorkflow().getJob().getL10nProfile();
        int downloadEditAll = 0;
        if (l10nProfile.getTmChoice() == LocProfileStateConstants.ALLOW_EDIT_TM_USAGE)
        {
            downloadEditAll = 1;
        }
        Vector excludeTypes = l10nProfile.getTranslationMemoryProfile()
                .getJobExcludeTuTypes();
        List primarySourceFiles = help.getAllPSFList(task);
        List stfList = help.getAllSTFList(task);
        List supportFileList = help.getAllSupportFileList(task);
        String uiLocale = task.getSourceLocale().getLanguage() + "_"
                + task.getSourceLocale().getCountry();
        DownloadParams downloadParams = new DownloadParams(task.getJobName(),
                null, "", Long.toString(workflowId),
                Long.toString(task.getId()), pageIdList, pageNameList,
                canUseUrlList, primarySourceFiles, stfList, editorId,
                platformId, encoding, ptagFormat, uiLocale,
                task.getSourceLocale(), task.getTargetLocale(), true,
                fileFormat, excludeTypes, downloadEditAll, supportFileList,
                resInsMode, UserHandlerHelper.getUser(task.getAcceptor()));

        // activity type
        Activity act = new Activity();

        try
        {
            act = ServerProxy.getJobHandler().getActivity(task.getTaskName());
        }
        catch (Exception e)
        {
        }

        downloadParams.setJob(p_job);
        downloadParams.setActivityType(act.getDisplayName());

        downloadParams.setConsolidateTmxFiles("yes"
                .equalsIgnoreCase(consolidate));
        downloadParams.setDisplayExactMatch(displayExactMatch);

        return downloadParams;
    }

    public File downloadOfflineFiles(Task task, Job p_job,
            ArrayList p_nodeEmail, String lockedSegEditType,
            boolean isIncludeXmlNodeContextInformation)
            throws GeneralException, NamingException, IOException
    {
        DownloadParams downloadParams = getDownloadParams(task, p_job);
        downloadParams.setIncludeXmlNodeContextInformation(
        		isIncludeXmlNodeContextInformation);
        downloadParams.setAutoActionNodeEmail(p_nodeEmail);
        if (lockedSegEditType != null)
        {
            try
            {
                int type = Integer.parseInt(lockedSegEditType);
                if (type >= 1 && type <= 4)
                {
                    downloadParams.setTMEditType(type);
                }
            }
            catch (Exception e)
            {
                // still use default "tmEditType".
            }
        }

        File tmpDir = AmbFileStoragePathUtils.getCustomerDownloadDir(String
                .valueOf(task.getCompanyId()));
        String fileName = downloadParams.getTruncatedJobName() + "_"
                + task.getSourceLocale() + "_" + task.getTargetLocale()
                + ".zip";
        File temp = new File(tmpDir, fileName);

        JobPackageZipper zipper = new JobPackageZipper();
        zipper.createZipFile(temp);
        downloadParams.setZipper(zipper);
        downloadParams.setAutoActionNodeEmail(p_nodeEmail);

        try
        {
            downloadParams.verify();
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }

        OfflineEditManager odm = ServerProxy.getOfflineEditManager();
        OEMProcessStatus status = new OEMProcessStatus(downloadParams);
        odm.attachListener(status);
        odm.runProcessDownloadRequest(downloadParams);

        zipper.closeZipFile();

        return temp;
    }

    // get a system configuration value for a given name.
    private String getSystemConfigValue(String p_paramName)
    {
        String value = null;
        try
        {
            SystemConfiguration config = SystemConfiguration.getInstance();
            value = config.getStringParameter(p_paramName);
        }
        catch (Exception e)
        {
            s_logger.error(
                    "WorkflowManagerLocal :: getSystemConfigValue for parameter name: "
                            + p_paramName, e);
        }

        return value;
    }

    public void dispatch(Job p_job) throws RemoteException,
            WorkflowManagerException
    {
        JobImpl jobClone = null;
        Session session = HibernateUtil.getSession();
        Transaction transaction = null;
        JbpmContext ctx = null;

        try
        {
            transaction = HibernateUtil.getTransaction();
            jobClone = (JobImpl) session.get(JobImpl.class,
                    new Long(p_job.getId()));
            if (jobClone != null)
            {
                // refresh job object in the session
                session.evict(jobClone);
                jobClone = (JobImpl) session.get(JobImpl.class,
                        new Long(p_job.getId()));
            }
            Iterator it = jobClone.getWorkflows().iterator();
            // a Map containing task id as key and workflow as value.
            // This is used for possible creation of STF.
            HashMap<Long, Workflow> map = new HashMap<Long, Workflow>(1);
            HashMap<Long, String> etfMap = new HashMap<Long, String>(1);
            Date startDate = new Date();
            ExecutorService pool = Executors.newFixedThreadPool(MAX_THREAD);
            while (it.hasNext())
            {
                Workflow wf = (Workflow) it.next();
                if (WF_READY.equals(wf.getState())
                        || Workflow.PENDING.equals(wf.getState()))
                {
                    Workflow wfClone = (Workflow) session.get(
                            WorkflowImpl.class, wf.getIdAsLong());
                    TaskEmailInfo emailInfo = createTaskEmailInfo(jobClone,
                            wfClone);

                    ArrayList returnValue = dispatchWorkflow(wfClone, session,
                            startDate, emailInfo);

                    long taskId = ((Long) returnValue.get(0)).longValue();
					if (taskId != -1)
					{
						Object actionType = returnValue.get(3);
						if (actionType != null)
						{
							etfMap.put(taskId, (String) actionType);
						}

						Task task = (Task) wfClone.getTasks().get(taskId);
						long jobId = task.getJobId();
						L10nProfile l10nProfile = ServerProxy.getJobHandler()
								.getL10nProfileByJobId(jobId);
						long wfStatePostId = l10nProfile.getWfStatePostId();
						if (wfStatePostId != -1)
						{
                            WfStatePostThread myTask = new WfStatePostThread(task, null, true);
							pool.execute(myTask);
						}

						// For sla issue
						if (wfClone
								.isEstimatedTranslateCompletionDateOverrided())
						{
							updateEstimatedTranslateCompletionDate(
									wfClone.getId(),
									wfClone.getEstimatedTranslateCompletionDate());
						}

						// prepare the map for possible creation of secondary
						// target
						// files
						if (((Boolean) returnValue.get(1)).booleanValue())
						{
							map.put(new Long(taskId), wfClone);
						}
					}
					session.saveOrUpdate(wfClone);
                }
            }
            pool.shutdown();

            jobClone.setState(WF_DISPATCHED);
            updatePageState(session, jobClone.getSourcePages(), PG_ACTIVE_JOB);
            session.saveOrUpdate(jobClone);

            HibernateUtil.commit(transaction);

            String pmId = p_job.getL10nProfile().getProject()
                    .getProjectManagerId();
            if (map.size() > 0)
            {
                Object[] keys = map.keySet().toArray();
                for (int i = 0; i < keys.length; i++)
                {
                    Long stfTaskId = (Long) keys[i];
                    Workflow wf = map.get(stfTaskId);
                    exportForStfCreation(stfTaskId, wf, pmId);
                }
            }
            // GBS-3002
            if (etfMap.size() > 0)
            {
                Object[] keys = etfMap.keySet().toArray();
                for (int i = 0; i < keys.length; i++)
                {
                    Long taskId = (Long) keys[i];
                    String actionType = etfMap.get(taskId);
                    SystemActionPerformer.perform(actionType, taskId, pmId);
                }
            }
        }
        catch (Exception e2)
        {
            HibernateUtil.rollback(transaction);
            s_logger.error("Failed to dispatch: " + p_job.getJobName(), e2);
            String[] args = new String[1];
            args[0] = new Long(p_job.getId()).toString();
            throw new WorkflowManagerException(
                    WorkflowManagerException.MSG_FAILED_TO_DISPATCH_WORKFLOW,
                    args, e2, WorkflowManagerException.PROPERTY_FILE_NAME);
        }
        finally
        {
            if (ctx != null)
            {
                ctx.close();
            }
        }

        runJobCreationScript(p_job);
    }

    /**
     * This method sets the percentage completion of a particular workflow
     * 
     * @param p_destinationArrow
     *            - The name of the outgoing arrow of a condition node (if the
     *            next node of this task is a condition node). This is used for
     *            making decision.
     * @param skipping
     *            Indicates this task is being skipped.
     * 
     * @throws RemoteException
     *             , WorkflowManagerException
     */
    public void setTaskCompletion(String p_userId, Task p_task,
            String p_destinationArrow, String skipping) throws RemoteException,
            WorkflowManagerException
    {
        if (s_logger.isDebugEnabled())
        {
            String s = "null task";
            if (p_task != null)
                s = p_task.toString();
        }

        for (int i = 0; i < 3; i++)
        {
            try
            {
                completeTask(p_userId, p_task, p_destinationArrow, skipping);
                break;
            }
            catch (Exception e)
            {
                s_logger.warn("Ignoring Exception");
                Thread.yield();
            }
        }
        
        // for COTI api finish job
        try
        {
            Workflow wfClone = (Workflow) p_task.getWorkflow();
            JobImpl curJob = HibernateUtil.get(JobImpl.class, wfClone.getJob()
                    .getId());
            String jobStatus = curJob.getState();
            if (Job.LOCALIZED.equals(jobStatus)
                    || Job.EXPORTED.equals(jobStatus)
                    || Job.EXPORTING.equals(jobStatus))
            {
                COTIUtilEnvoy.finishCOTIJob(curJob);
            }
        }
        catch (Throwable t)
        {
            // log the error but don't let it affect job completion
            s_logger.error("Error trying to finish COTI job.", t);
        }
        
        try
        {
            long jobId = p_task.getJobId();
            L10nProfile l10nProfile = ServerProxy.getJobHandler()
                    .getL10nProfileByJobId(jobId);
            long wfStatePostId = l10nProfile.getWfStatePostId();
            if (wfStatePostId != -1)
            {
                WfStatePostThread myTask = new WfStatePostThread(p_task, p_destinationArrow, false);
                Thread t = new MultiCompanySupportedThread(myTask);
                t.start();
            }
        }
        catch (Exception e)
        {
            s_logger.error("Error trying to finish COTI job.", e);
        }
    }


    
    /**
     * @see WorkflowManager.getTaskInfoByTaskId(Workflow, List, long)
     */
    public TaskInfo getTaskInfoByTaskId(Workflow p_workflow,
            List p_wfTaskInfos, long p_taskId, boolean p_acceptedOnly)
            throws RemoteException, WorkflowManagerException
    {
        String state = p_workflow.getState();
        if (Workflow.PENDING.equals(state) || WF_CANCELLED.equals(state)
                || Workflow.IMPORT_FAILED.equals(state)
                || WF_READY.equals(state))
        {
            return null;
        }

        TaskInfo taskInfo = null;
        try
        {
            int size = p_wfTaskInfos.size();
            Hashtable ht = p_workflow.getTasks();
            Date estimatedDate = p_workflow.getDispatchedDate();
            boolean found = false;
            TaskImpl task = null;
            for (int i = 0; !found && i < size; i++)
            {
                WfTaskInfo wfTaskInfo = (WfTaskInfo) p_wfTaskInfos.get(i);
                long id = wfTaskInfo.getId();
                found = id == p_taskId;

                task = (TaskImpl) ht.get(new Long(id));

                if (!found)
                {
                    estimatedDate = (wfTaskInfo.getState() == WorkflowConstants.STATE_COMPLETED) ? task
                            .getCompletedDate() : task
                            .getEstimatedCompletionDate();
                }
                else
                {
                    taskInfo = estimateDatesForDefaultPath(estimatedDate, task,
                            wfTaskInfo, p_acceptedOnly);
                }
            }
        }
        catch (Exception e)
        {
            s_logger.error(e.getMessage(), e);
            String[] args =
            { String.valueOf(p_taskId) };
            throw new WorkflowManagerException(
                    WorkflowManagerException.MSG_FAILED_TO_GET_TASKINFO_IN_DEFAULT_PATH,
                    args, e);
        }
        return taskInfo;
    }

    /**
     * @see WorkflowManager.getTaskInfosInDefaultPath(Workflow)
     */
    public List getTaskInfosInDefaultPath(Workflow p_workflow)
            throws RemoteException, WorkflowManagerException
    {
        List<TaskInfo> taskInfos = null;
        String state = p_workflow.getState();
        if (Workflow.PENDING.equals(state) || WF_CANCELLED.equals(state)
                || Workflow.IMPORT_FAILED.equals(state)
                || WF_READY.equals(state))
        {
            return null;
        }

        try
        {
            taskInfos = new ArrayList<TaskInfo>();
            List wfTaskInfos = ServerProxy.getWorkflowServer()
                    .timeDurationsInDefaultPath(null, p_workflow.getId(), -1);

            int size = wfTaskInfos.size();
            Hashtable ht = p_workflow.getTasks();
            Date estimatedDate = p_workflow.getDispatchedDate();
            for (int i = 0; i < size; i++)
            {
                WfTaskInfo wfTaskInfo = (WfTaskInfo) wfTaskInfos.get(i);
                TaskImpl task = (TaskImpl) ht.get(new Long(wfTaskInfo.getId()));
                if (task != null)
                {
                    TaskInfo ti = estimateDatesForDefaultPath(estimatedDate,
                            task, wfTaskInfo, true);
                    estimatedDate = (ti.getState() == WorkflowConstants.STATE_COMPLETED) ? ti
                            .getCompletedDate() : ti.getCompleteByDate();

                    taskInfos.add(ti);
                }
            }
        }
        catch (Exception e)
        {
            String[] args =
            { String.valueOf(p_workflow.getId()) };
            throw new WorkflowManagerException(
                    WorkflowManagerException.MSG_FAILED_TO_GET_TASKS_IN_DEFAULT_PATH,
                    args, e);
        }
        return taskInfos;
    }

    @SuppressWarnings("unchecked")
    public List getTaskInfosInDefaultPathWithSkip(Workflow p_workflow)
            throws RemoteException, WorkflowManagerException
    {

        JbpmContext ctx = WorkflowConfiguration.getInstance().getJbpmContext();
        List<String> taskList = null;
        try
        {
            taskList = WorkflowJbpmPersistenceHandler.getSkippedTaskInstance(
                    p_workflow.getId(), ctx);

        }
        finally
        {
            ctx.close();
        }

        List<TaskInfo> list = getTaskInfosInDefaultPath(p_workflow);
        TaskInfo lastTaskInfo = null;
        if (list != null)
        {
            for (TaskInfo taskInfo : list)
            {
                if (isSkipped(taskInfo, taskList))
                {
                    taskInfo.setState(Task.STATE_SKIP);

                }
                lastTaskInfo = taskInfo;
            }
        }

        if (lastTaskInfo != null)
        {
            lastTaskInfo.setExportDate(((WorkflowImpl) p_workflow)
                    .getExportDate());
        }

        return list;
    }

    private boolean isSkipped(TaskInfo taskInfo, List<String> taskList)
    {
        for (String name : taskList)
        {
            if (WorkflowJbpmUtil.getActivityName(name).equals(
                    taskInfo.getName()))
            {
                return true;
            }
        }

        return false;
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
        Map addedAndDeleted = null;
        Session session = HibernateUtil.getSession();
        Transaction tx = session.beginTransaction();
        try
        {
            Workflow workflow = (Workflow) session.get(WorkflowImpl.class,
                    new Long(p_wfInstance.getId()));
            Job job = workflow.getJob();

            validateStateOfPages(workflow);
            TaskEmailInfo emailInfo = createTaskEmailInfo(job, workflow);

            Date date = new Date();
            addedAndDeleted = modifyWorkflowInstance(date, p_sessionId,
                    p_wfInstance, emailInfo,
                    String.valueOf(workflow.getCompanyId()));

            // update state for a workflow that was in completed state.
            if (Workflow.EXPORT_FAILED.equals(workflow.getState()))
            {
                workflow.setState(WF_DISPATCHED);
                workflow.setDispatchedDate(date);
                updatePageState(session, workflow.getTargetPages(),
                        PG_ACTIVE_JOB);
                updateSecondaryTargetFileState(session,
                        workflow.getSecondaryTargetFiles(),
                        SecondaryTargetFileState.ACTIVE_JOB);
                possiblyUpdateJob(session, workflow, Workflow.EXPORT_FAILED);
            }

            // get a list of tasks in the workflow's default path
            // -1 indicates that the default path would be from the
            // beginning of the workflow (form START node)
            List wfTaskInfos = getWFServer().timeDurationsInDefaultPath(null,
                    p_wfInstance.getId(), -1);

            if (WF_DISPATCHED.equals(workflow.getState())
                    || WF_READY.equals(workflow.getState()))
            {
                updateWorkflowChanges(p_modifiedTasks, p_wfInstance,
                        wfTaskInfos, session, workflow, addedAndDeleted, date);
            }

            updateDuration(p_wfInstance.getId(), wfTaskInfos, session);
            session.update(workflow);
            tx.commit();
        }
        catch (GeneralException ge)
        {
            s_logger.error("modifyWorkflow failed. " + ge.toString(), ge);
            // any instance of GeneralException should have its own meaningful
            // message that should be displayed to the user.
            throw new WorkflowManagerException(ge);
        }
        catch (Exception we)
        {
            tx.rollback();
            s_logger.error("modifyWorkflow failed.  " + we.toString(), we);

            String[] args =
            { String.valueOf(p_wfInstance.getId()) };
            throw new WorkflowManagerException(
                    WorkflowManagerException.MSG_FAILED_TO_MODIFY_WORKFLOW,
                    args, we);
        }
    }

    /**
     * @see WorkflowManager.startStfCreationForWorkflow(long, Workflow, String)
     */
    public void startStfCreationForWorkflow(long p_taskId, Workflow p_workflow,
            String p_userId) throws RemoteException, WorkflowManagerException
    {
        try
        {
            exportForStfCreation(new Long(p_taskId), p_workflow, p_userId);
        }
        catch (Exception e)
        {
            String[] args =
            { String.valueOf(p_taskId) };
            throw new WorkflowManagerException(
                    WorkflowManagerException.MSG_FAILED_TO_START_CSTF_PROCESS,
                    args, e);
        }
    }

    /**
     * @deprecated For sla report issue.
     * @see WorkflowManager.updatePlannedCompletionDate(String, long, Date)
     */
    public void updatePlannedCompletionDate(long p_workflowId,
            Date p_plannedCompletionDate) throws WorkflowManagerException,
            RemoteException
    {
        Session session = HibernateUtil.getSession();
        Transaction tx = session.beginTransaction();
        try
        {
            Workflow wf = (Workflow) session.get(WorkflowImpl.class, new Long(
                    p_workflowId));
            wf.setPlannedCompletionDate(p_plannedCompletionDate);
            session.saveOrUpdate(wf);
            tx.commit();

            // send notification if the estimate completion date exceeds planned
            // date
            sendNotification(wf);
        }
        catch (Exception e)
        {
            tx.rollback();
            String[] args =
            { String.valueOf(p_workflowId) };
            throw new WorkflowManagerException(
                    WorkflowManagerException.MSG_FAILED_TO_UPDATE_PCD, args, e);
        }
    }

    /**
     * For sla report issue User can override the estimatedCompletionDate.
     * 
     * @see WorkflowManager.updateEstimatedCompletionDate(String, long, Date)
     */
    public void updateEstimatedCompletionDate(long p_workflowId,
            Date p_estimatedCompletionDate) throws WorkflowManagerException,
            RemoteException
    {
        Session session = HibernateUtil.getSession();
        Transaction tx = session.beginTransaction();
        try
        {
            Workflow wf = (Workflow) session.get(WorkflowImpl.class, new Long(
                    p_workflowId));
            wf.setEstimatedCompletionDate(p_estimatedCompletionDate);
            wf.setEstimatedCompletionDateOverrided(true);
            session.saveOrUpdate(wf);
            tx.commit();

            // send notification if the estimate completion date exceeds planned
            // date
            // sendNotification(clone);
            sendNotification(wf);
        }
        catch (Exception e)
        {
            String[] args =
            { String.valueOf(p_workflowId) };
            throw new WorkflowManagerException(
                    WorkflowManagerException.MSG_FAILED_TO_UPDATE_PCD, args, e);
        }
    }

    /**
     * @see WorkflowManager.updateEstimatedTranslateCompletionDate(long, Date)
     */
    public void updateEstimatedTranslateCompletionDate(long p_workflowId,
            Date p_estimatedTranslateCompletionDate)
            throws WorkflowManagerException, RemoteException
    {
        Session session = HibernateUtil.getSession();
        Transaction tx = HibernateUtil.getTransaction();
        try
        {
            Workflow wf = (Workflow) session.get(WorkflowImpl.class, new Long(
                    p_workflowId));
            refreshWorkflowInstance(wf);

            Date oriEstimatedTranslateCompletionDate = wf
                    .getEstimatedTranslateCompletionDate();
            wf.setEstimatedTranslateCompletionDate(p_estimatedTranslateCompletionDate);
            wf.setEstimatedTranslateCompletionDateOverrided(true);

            if (Workflow.READY_TO_BE_DISPATCHED.equals(wf.getState())
                    && !wf.isEstimatedCompletionDateOverrided())
            {
                List wfTaskInfos = ServerProxy.getWorkflowServer()
                        .timeDurationsInDefaultPath(null, wf.getId(), -1);
                FluxCalendar defaultCalendar = ServerProxy.getCalendarManager()
                        .findDefaultCalendar(String.valueOf(wf.getCompanyId()));

                Hashtable tasks = wf.getTasks();

                long workflowDuration = 0l;

                for (int i = wfTaskInfos.size() - 1; i >= 0; i--)
                {
                    WfTaskInfo wfTaskInfo = (WfTaskInfo) wfTaskInfos.get(i);
                    TaskImpl task = (TaskImpl) tasks.get(new Long(wfTaskInfo
                            .getId()));
                    if (task == null)
                        continue;

                    Activity act = ServerProxy.getJobHandler().getActivity(
                            task.getTaskName());

                    if (Activity.isTranslateActivity(act.getType()))
                    {
                        updateTaskTimeToComplete(task,
                                oriEstimatedTranslateCompletionDate,
                                p_estimatedTranslateCompletionDate, session);
                        break;
                    }

                    workflowDuration += wfTaskInfo.getTotalDuration();
                }

                wf.setEstimatedCompletionDate(ServerProxy.getEventScheduler()
                        .determineDate(p_estimatedTranslateCompletionDate,
                                defaultCalendar, workflowDuration));
            }
            else if (Workflow.DISPATCHED.equals(wf.getState()))
            {
                Task translateTask = null;
                List taskInfos = getTaskInfosInDefaultPath(wf);
                if (taskInfos != null)
                {
                    for (int i = taskInfos.size() - 1; i >= 0; i--)
                    {
                        TaskInfo taskInfo = (TaskInfo) taskInfos.get(i);
                        Task task = (Task) wf.getTasks().get(
                                new Long(taskInfo.getId()));
                        Activity act = ServerProxy.getJobHandler().getActivity(
                                task.getTaskName());

                        if (act.isType(Task.TYPE_TRANSLATE))
                        {
                            translateTask = task;
                            break;
                        }
                    }
                }

                // Update translate task & workflow
                if ((translateTask != null)
                        && (translateTask.getState() != Task.STATE_COMPLETED))
                {
                    translateTask
                            .setEstimatedCompletionDate(p_estimatedTranslateCompletionDate);

                    List wfTaskInfos = getWFServer()
                            .timeDurationsInDefaultPath(null, p_workflowId, -1);

                    updateDefaultPathTasks(MODIFY_ACTION, new Date(),
                            wfTaskInfos, wf, translateTask.getId(), false,
                            p_estimatedTranslateCompletionDate, session);
                    session.saveOrUpdate(translateTask);

                    updateTaskTimeToComplete(translateTask,
                            oriEstimatedTranslateCompletionDate,
                            p_estimatedTranslateCompletionDate, session);
                }
            }
            session.saveOrUpdate(wf);
            HibernateUtil.commit(tx);
        }
        catch (Exception e)
        {
            HibernateUtil.rollback(tx);
            String[] args =
            { String.valueOf(p_workflowId) };
            throw new WorkflowManagerException(
                    WorkflowManagerException.MSG_FAILED_TO_UPDATE_PCD, args, e);
        }
    }

    /**
     * Updates the "Time to Complete" in the delegation configuration xml for
     * the task.
     * <p>
     * For GBS-3456.
     */
    private void updateTaskTimeToComplete(Task task,
            Date originalEstimatedCompletionDate,
            Date estimatedTranslateCompletionDate, Session session)
            throws Exception
    {
        long oriEstimatedTranslateCompletionDate = originalEstimatedCompletionDate
                .getTime();
        long newEstimatedTranslateCompletionDate = estimatedTranslateCompletionDate
                .getTime();
        if (newEstimatedTranslateCompletionDate == oriEstimatedTranslateCompletionDate)
        {
            return;
        }
        JbpmContext ctx = null;
        try
        {
            ctx = WorkflowConfiguration.getInstance().getJbpmContext();

            TaskInstance taskInstance = WorkflowJbpmPersistenceHandler
                    .getTaskInstance(task.getId(), ctx);
            Node n = null;
            if (taskInstance == null)
            {
                // this task instance has not been initialized
                WorkflowTaskInstance workflowTaskInstance = task.getWorkflow()
                        .getIflowInstance().getWorkflowTaskById(task.getId());
                ProcessInstance processInstance = ctx.getProcessInstance(task
                        .getWorkflow().getId());

                n = WorkflowJbpmUtil.getNodeByNodeName(
                        processInstance.getProcessDefinition(),
                        workflowTaskInstance.getNodeName());
            }
            else
            {
                n = taskInstance.getTask().getTaskNode();
            }

            String config = WorkflowJbpmUtil.getConfigure(n);
            WorkflowNodeParameter param = WorkflowNodeParameter
                    .createInstance(config);
            long timeToComplete = param.getLongAttribute(
                    WorkflowConstants.FIELD_COMPLETED_TIME,
                    WorkflowTaskInstance.NO_RATE);

            long changedTime = determineInterval(task,
                    originalEstimatedCompletionDate,
                    estimatedTranslateCompletionDate);
            long updatedTimeToComplete = changedTime + timeToComplete;
            // at least one minute
            if (updatedTimeToComplete <= 60000)
            {
                updatedTimeToComplete = 60000;
            }

            param.setAttribute(WorkflowConstants.FIELD_COMPLETED_TIME,
                    String.valueOf(updatedTimeToComplete));
            WorkflowJbpmUtil.setConfigure(n, param.restore());

            // update the scheduler with new estimated translate completion date
            // if the task has already been accepted
            if (task.getAcceptedDate() != null)
            {
                updateReservedTimes(task, session);
                if (isNotificationActive())
                {
                    TaskInfo ti = new TaskInfo(task.getId(),
                            task.getTaskName(), task.getState(),
                            task.getEstimatedAcceptanceDate(),
                            task.getEstimatedCompletionDate(),
                            task.getAcceptedDate(), task.getCompletedDate(),
                            task.getType());
                    int actionType = SchedulerConstants.ACCEPT_ACTIVITY;

                    EventNotificationHelper.performSchedulingProcess(
                            new Integer(actionType),
                            task.getId(),
                            (Integer) SchedulerConstants.s_eventTypes
                                    .get(SchedulerConstants.ACCEPT_TYPE),
                            n,
                            ti,
                            task.getAcceptedDate().getTime(),
                            (Integer) SchedulerConstants.s_eventTypes
                                    .get(SchedulerConstants.COMPLETE_TYPE),
                            getWarningThreshold(),
                            createTaskEmailInfo(task.getWorkflow().getJob(),
                                    task.getWorkflow()));
                }
            }
        }
        finally
        {
            ctx.close();
        }
    }

    /**
     * Updates the reserved time associated with the given task from the user's
     * calendar.
     */
    private void updateReservedTimes(Task task, Session session)
            throws Exception
    {
        removeReservedTime(task.getId());
        createReservedTime(task, ReservedTime.TYPE_ACTIVITY, session);
    }

    /**
     * Creates a reserved time along with a possible buffer and add it to the
     * acceptor's calendar.
     */
    private void createReservedTime(Task task, String reservedTimeType,
            Session session) throws Exception
    {
        UserFluxCalendar userCalendar = ServerProxy.getCalendarManager()
                .findUserCalendarByOwner(task.getAcceptor());
        UserFluxCalendar calClone = (UserFluxCalendar) session.get(
                UserFluxCalendar.class, userCalendar.getIdAsLong());
        addReservedTimeToUserCal(task, reservedTimeType, session, calClone,
                task.getAcceptedDate(), task.getEstimatedCompletionDate());

        session.saveOrUpdate(calClone);
    }

    /**
     * Creates and add a reserved time based on the specified type to the given
     * user calendar.
     */
    private void addReservedTimeToUserCal(Task task, String reservedTimeType,
            Session session, UserFluxCalendar calClone, Date baseDate,
            Date estimatedDate)
    {
        // if calendaring module is not installed, don't create reserved time.
        if (!Modules.isCalendaringInstalled())
        {
            return;
        }

        TimeZone tz = calClone.getTimeZone();
        Timestamp start = new Timestamp(tz);
        start.setDate(baseDate);
        Timestamp end = new Timestamp(tz);
        end.setDate(estimatedDate);

        StringBuffer sb = new StringBuffer();
        sb.append("[");
        sb.append(task.getTaskName());
        sb.append("]");
        sb.append("[");
        sb.append(task.getJobName());
        sb.append("]");
        sb.append("[");
        sb.append(task.getProjectManagerId());
        sb.append("]");

        String taskName = sb.toString();

        ReservedTime rt = new ReservedTime(taskName, reservedTimeType, start,
                start.getHour(), start.getMinute(), end, end.getHour(),
                end.getMinute(), null, task.getId());

        calClone.addReservedTime(rt);
        session.save(rt);

        // now add the buffer (if not set to zero)
        if (calClone.getActivityBuffer() > 0)
        {
            Timestamp bufferEnd = new Timestamp(tz);
            bufferEnd.setDate(end.getDate());
            bufferEnd.add(Timestamp.HOUR, calClone.getActivityBuffer());
            ReservedTime buffer = new ReservedTime(taskName,
                    ReservedTime.TYPE_BUFFER, end, end.getHour(),
                    end.getMinute(), bufferEnd, bufferEnd.getHour(),
                    bufferEnd.getMinute(), null, task.getId());

            calClone.addReservedTime(buffer);
            session.save(buffer);
        }
    }

    /**
     * Determines the interval changed from original date to new one.
     */
    private long determineInterval(Task task, Date oriDate, Date newDate)
    {
        long changedTime = 0;

        try
        {
            CalendarManager cm = ServerProxy.getCalendarManager();
            FluxCalendar companyCalendar = cm.findDefaultCalendar(String
                    .valueOf(task.getCompanyId()));
            changedTime = cm.computeInterval(companyCalendar, oriDate, newDate);
        }
        catch (Exception ex)
        {
            long oriEstimatedCompletionDate = oriDate.getTime();
            changedTime = newDate.getTime() - oriEstimatedCompletionDate;
        }

        return changedTime;
    }

    private Float getWarningThreshold()
    {
        if (m_threshold == null)
        {
            try
            {
                String threshold = getSystemConfigValue(SystemConfigParamNames.TIMER_THRESHOLD);
                m_threshold = Float.valueOf(threshold);
            }
            catch (NumberFormatException e)
            {
                s_logger.error(
                        "Invalid warning threshold. It's been reset to 75%", e);
                m_threshold = Float.valueOf(".75");
            }
        }
        return m_threshold;
    }

    /**
     * Determines whether the notification feature is active.
     */
    private boolean isNotificationActive()
    {
        if (m_isNotificationActive == -1)
        {
            try
            {
                SystemConfiguration config = SystemConfiguration.getInstance();
                m_isNotificationActive = config
                        .getIntParameter(SystemConfigParamNames.USE_WARNING_THRESHOLDS);
            }
            catch (Exception e)
            {
                s_logger.error("Error checking warningThresholds.enabled", e);
            }
        }
        return m_isNotificationActive == 1;
    }

    /**
     * @see WorkflowManager.reassignWorkflowOwners(long, List)
     */
    public void reassignWorkflowOwners(long p_workflowId, List p_workflowOwners)
            throws RemoteException, WorkflowManagerException
    {
        Session session = HibernateUtil.getSession();
        Transaction tx = session.beginTransaction();
        try
        {
            Workflow wf = (Workflow) session.get(WorkflowImpl.class, new Long(
                    p_workflowId));
            // first delete all the previous owners (EXCEPT FOR 'PM')
            for (Iterator it = wf.getWorkflowOwners().iterator(); it.hasNext();)
            {
                WorkflowOwner owner = (WorkflowOwner) it.next();
                if (!Permission.GROUP_PROJECT_MANAGER.equals(owner
                        .getOwnerType()))
                {
                    wf.removeWorkflowOwner(owner);
                    session.delete(owner);
                    it.remove();
                }
            }

            // now add the newly assigned owners
            int size = p_workflowOwners.size();
            for (int i = 0; i < size; i++)
            {
                WorkflowOwner wfo = (WorkflowOwner) p_workflowOwners.get(i);
                wf.addWorkflowOwner(wfo);
            }

            // set the new owners in the iFlow's process instance
            assignWorkflowInstanceOwners(p_workflowId, wf.getWorkflowOwners());

            session.saveOrUpdate(wf);
            tx.commit();
        }
        catch (Exception e)
        {
            tx.rollback();
            throw new WorkflowManagerException(e);
        }
    }

    /**
     * Assign the new workflow owners.
     * 
     * @param p_workflowId
     *            - The id of the workflow that the new owners should be
     *            assigned.
     * @param p_newWorkflowOwners
     *            - A list of new workflow owners to be assigned.
     */
    private void assignWorkflowInstanceOwners(long p_workflowId,
            List p_newWorkflowOwners) throws Exception
    {
        // get all of the owners now and pass the array of ids to iFlow
        int size = p_newWorkflowOwners.size();
        String[] ownerIds = new String[size];
        for (int i = 0; i < size; i++)
        {
            WorkflowOwner wfo = (WorkflowOwner) p_newWorkflowOwners.get(i);
            ownerIds[i] = wfo.getOwnerId();
        }
        getWFServer().reassignWorkflowOwners(p_workflowId, null, ownerIds);
    }

    /**
     * Update the completion fraction. This is used when the number of tasks has
     * changed.
     * <p>
     * Set the denominator to the reflect this change. Leave the numerator
     * alone.
     */
    private void updateCompletionFraction(Workflow p_workflow,
            List p_wfTaskInfos)
    {
        try
        {
            int size = p_wfTaskInfos.size();

            long longDenominator = 0;
            for (int i = 0; i < size; i++)
            {
                WfTaskInfo wfTaskInfo = (WfTaskInfo) p_wfTaskInfos.get(i);
                // get the total duration (acceptance + completion)
                longDenominator += wfTaskInfo.getTotalDuration();
            }

            // convert the millisec to minutes since it's the smallest unit
            // of time used for each activity of a workflow.
            int denominator = (int) (longDenominator / 60000);

            // the numerator stays the same or is moved to match the denominator
            // if
            // it is bigger.
            int numerator = (p_workflow.getCompletionNumerator() > denominator ? denominator
                    : p_workflow.getCompletionNumerator());
            p_workflow.setCompletionFraction(numerator, denominator);
        }
        catch (Exception e)
        {
            // just log - no exception
            s_logger.error(
                    "Failed to update the completion fraction for workflow "
                            + p_workflow.getId(), e);
        }
    }

    /**
     * Update the completion fraction. The task being passed in has been
     * accepted or completed. And updates the numerator and denominator
     * accordingly.
     * 
     * @param p_completed
     *            specifies 'true' if the complete time should be added to the
     *            fraction. 'false' if the accept time should be added to the
     *            fraction.
     */
    private void updateCompletionFraction(Workflow p_workflow,
            Task p_completedOrAcceptedTask, List p_wfTaskInfos,
            boolean p_isCompleted)
    {
        try
        {
            int size = p_wfTaskInfos.size();
            long longDenominator = 0;
            for (int i = 0; i < size; i++)
            {
                WfTaskInfo wfTaskInfo = (WfTaskInfo) p_wfTaskInfos.get(i);
                // get the total duration (acceptance + completion)
                longDenominator += wfTaskInfo.getTotalDuration();
            }

            // calculate denominator in 'minutes'
            int denominator = (int) (longDenominator / 60000);

            int numerator = p_workflow.getCompletionNumerator();
            // calculate for the completion
            if (p_isCompleted)
            {
                numerator = denominator;
            }
            else
            {
                if (((TaskImpl) p_completedOrAcceptedTask).getWorkflowTask() == null)
                {
                    return;
                }

                int duration = (int) (p_completedOrAcceptedTask
                        .getTaskDuration() / 60000)
                        + (int) (p_completedOrAcceptedTask
                                .getTaskAcceptDuration() / 60000);

                int totalDuration = duration + numerator;

                numerator = totalDuration < denominator ? totalDuration
                        : numerator;

                // for workflows with condition node, the default path could
                // return a duration that might be less than the current
                // numerator.
                if (numerator >= denominator)
                {
                    numerator = denominator - duration;
                }
            }
            p_workflow.setCompletionFraction(numerator, denominator);
        }
        catch (Exception e)
        {
            // just log - no exception
            s_logger.error(
                    "Failed to update the completion percentage for workflow "
                            + p_workflow.getId(), e);
        }
    }

    /* Create tasks for the given workflow (a cloned object) based on the */
    /* i-Flow task instances */
    private void createTasks(Session p_session, Vector p_workflowTaskInstances,
            Hashtable p_modifiedTasks, Workflow p_workflow, List p_wfTaskInfos)
            throws Exception
    {
        TaskImpl task = null;
        try
        {

            int size = p_workflowTaskInstances.size();
            CostingEngine ce = ServerProxy.getCostingEngine();
            for (int i = 0; i < size; i++)
            {
                WorkflowTaskInstance inst = (WorkflowTaskInstance) p_workflowTaskInstances
                        .elementAt(i);
                TaskInfoBean taskInfo = null;
                taskInfo = (TaskInfoBean) p_modifiedTasks.get(new Long(inst
                        .getSequence()));
                task = new TaskImpl();
                task.setId(inst.getTaskId());
                task.setWorkflow(p_workflow);
                p_workflow.addTask(task);
                task.setName(inst.getActivityName());
                task.setState(TaskImpl.STATE_DEACTIVE);
                task.setType(getActivityType(task.getName()));
                task.setCompanyId(p_workflow.getCompanyId());
                Rate pageBasedRate = null;
                String hourAmount = null;
                // For Expenses
                long rateId = inst.getExpenseRateId();
                task.setRateSelectionCriteria(inst.getRateSelectionCriteria());
                if (rateId > 0)
                {
                    // need to create a clone because the rate already exists -
                    // so
                    // don't want to try and insert but just set up the
                    // relationship
                    Rate rateClone = (Rate) ce.getRate(rateId);
                    task.setExpenseRate(rateClone);
                    if (rateClone.getRateType().equals(
                            Rate.UnitOfWork.PAGE_COUNT))
                    {
                        pageBasedRate = rateClone;
                    }
                    else
                    {
                        if (rateClone.getRateType().equals(
                                Rate.UnitOfWork.HOURLY))
                        {
                            AmountOfWork cloneNewAow = null;
                            if (taskInfo != null)
                            {
                                hourAmount = taskInfo.getEstimatedHours();
                            }
                            if (hourAmount != null)
                            {
                                AmountOfWork newAow = rateClone
                                        .createAmountOfWork();
                                // set the hour amount as estimated hours
                                newAow.setEstimatedAmount(Double.parseDouble((hourAmount
                                        .length() == 0) ? "0" : hourAmount));
                                cloneNewAow = newAow;
                                // p_session.save(cloneNewAow);
                            }
                            task.setAmountOfWork(cloneNewAow);
                        }
                    }
                }
                // for Revenue
                rateId = inst.getRevenueRateId();
                if (rateId > 0)
                {
                    // need to create a clone because the rate already exists -
                    // so
                    // don't want to try and insert but just set up the
                    // relationship
                    // Rate rateClone = (Rate) p_uow.registerObject(ce
                    // .getRate(rateId));
                    Rate rateClone = (Rate) ce.getRate(rateId);
                    task.setRevenueRate(rateClone);
                    if (pageBasedRate == null
                            && rateClone.getRateType().equals(
                                    Rate.UnitOfWork.PAGE_COUNT))
                    {
                        pageBasedRate = rateClone;
                    }
                    else
                    {
                        if (rateClone.getRateType().equals(
                                Rate.UnitOfWork.HOURLY))
                        {
                            AmountOfWork cloneNewAow = null;
                            if (hourAmount == null)
                            {
                                if (taskInfo != null)
                                {
                                    hourAmount = taskInfo.getEstimatedHours();
                                }
                                if (hourAmount != null)
                                {
                                    AmountOfWork newAow = rateClone
                                            .createAmountOfWork();
                                    // set the hour amount as estimated hours
                                    newAow.setEstimatedAmount(Double
                                            .parseDouble((hourAmount.length() == 0) ? "0"
                                                    : hourAmount));
                                    cloneNewAow = newAow;
                                    // p_session.save(cloneNewAow);
                                }
                                task.setAmountOfWork(cloneNewAow);
                            }
                        }
                    }
                }
                if (s_logger.isDebugEnabled())
                {
                    s_logger.debug("createTasks : " + " workflow="
                            + p_workflow.getId() + " WorkflowTaskInstance="
                            + WorkflowHelper.toDebugString(inst)
                            + GlobalSightCategory.getLineContinuation()
                            + " task=" + task.toString());
                }

                // if a page based rate is assigned to one of the expenses
                // then need to add that amount of work to the task class.
                if (pageBasedRate != null)
                {
                    AmountOfWork pageAow = pageBasedRate.createAmountOfWork();
                    int numOfPages = task.getWorkflow().getJob().getPageCount();
                    pageAow.setEstimatedAmount(numOfPages);
                    // p_session.save(pageAow);
                    task.setAmountOfWork(pageAow);
                }
                p_session.saveOrUpdate(task);

                for (Object ob : task.getWorkSet())
                {
                    if (ob instanceof AmountOfWork)
                    {
                        AmountOfWork aWork = (AmountOfWork) ob;
                        p_session.saveOrUpdate(aWork);
                    }
                }
            }
        }
        catch (Exception e)
        {
            s_logger.error("Failed to get a rate for a task.", e);
            // if a task was set then specify the task - otherwise specify all
            // because
            // the costing engine couldn't be retrieved to get rates.
            String taskId = task != null ? Long.toString(task.getId()) : "all";
            String args[] =
            { taskId };
            throw new WorkflowManagerException(
                    WorkflowManagerException.MSG_FAILED_TO_FIND_RATE_FOR_TASK,
                    args, e);
        }

        if (s_logger.isDebugEnabled())
        {
            s_logger.debug("createTasks : "
                    + " setCompletionFraction numerator="
                    + p_workflow.getCompletionNumerator() + " denominator="
                    + p_workflow.getCompletionDenominator());
        }
    }

    /* modify the tasks of the given workflow (a cloned object) */
    /* based on the i-Flow task instances */
    private void modifyTasks(Session p_session, Hashtable p_modifiedTasks,
            Workflow p_workflow, List p_wfTaskInfos) throws Exception
    {
        Task task = null;
        try
        {
            // if there are modified tasks
            if (p_modifiedTasks != null && p_modifiedTasks.size() > 0)
            {
                Collection modifiedTasks = p_modifiedTasks.values();
                for (Iterator i = modifiedTasks.iterator(); i.hasNext();)
                {
                    TaskInfoBean taskInfo = (TaskInfoBean) i.next();
                    // Get a task from Toplink
                    task = ServerProxy.getTaskManager().getTask(
                            taskInfo.getTaskId());
                    // since we can also modify a workflow in Ready state, we
                    // need
                    // to check before deletion (in Ready state Task has not
                    // been
                    // created yet).
                    if (task != null)
                    {
                        task = modifyTask(task, taskInfo, p_session);
                    }
                }
                if (s_logger.isDebugEnabled())
                {
                    s_logger.debug("modifyTasks : " + " workflow="
                            + p_workflow.getId() + " Task="
                            + WorkflowHelper.toDebugString(task)
                            + GlobalSightCategory.getLineContinuation()
                            + " task=" + task.toString());
                }
            }
        }
        catch (Exception e)
        {
            s_logger.error("Failed to get a rate for a task.", e);
            // if a task was set then specify the task - otherwise specify all
            // because
            // the costing engine couldn't be retrieved to get rates.
            String taskId = task != null ? Long.toString(task.getId()) : "all";
            String args[] =
            { taskId };
            throw new WorkflowManagerException(
                    WorkflowManagerException.MSG_FAILED_TO_FIND_RATE_FOR_TASK,
                    args, e);
        }
    }

    /**
     * Modify one task.
     */
    private Task modifyTask(Task p_task, TaskInfoBean p_tib, Session p_session)
            throws Exception
    {
        // modify the rate - since it is the only thing on a task
        // that can be modified at this place
        Task taskClone = (Task) p_session.get(TaskImpl.class,
                new Long(p_task.getId()));
        Rate expenseRate = null;
        Rate oldExpenseRate = null;
        Rate revenueRate = null;
        Rate oldRevenueRate = null;
        boolean allowRemovalOfAmountOfWork = false;

        // Rate pageBasedRate = null;
        // Get the Expense Rate
        expenseRate = p_tib.getExpenseRate();
        oldExpenseRate = p_task.getExpenseRate();
        // Get the Revenue Rate
        revenueRate = p_tib.getRevenueRate();
        oldRevenueRate = p_task.getRevenueRate();

        taskClone.setRateSelectionCriteria(p_tib.getRateSelectionCriteria());
        taskClone.setIsReportUploadCheck(p_tib.getIsReportUploadCheck());
        // update task name and type
        String taskName = p_tib.getActivityName();
        taskClone.setTaskName(taskName);
        Activity activity = ServerProxy.getJobHandler().getActivity(taskName);
        if (activity != null)
        {
            taskClone.setType(activity.getActivityType());
        }

        if (expenseRate == null && revenueRate == null)
        {
            allowRemovalOfAmountOfWork = true;
        }
        else if (expenseRate == null && revenueRate != null)
        {
            if (oldExpenseRate != null)
            {
                if (oldExpenseRate.getRateType() != revenueRate.getRateType())
                {
                    allowRemovalOfAmountOfWork = true;
                }
            }
        }
        else if (expenseRate != null && revenueRate == null)
        {
            if (oldRevenueRate != null)
            {
                if (oldRevenueRate.getRateType() != expenseRate.getRateType())
                {
                    allowRemovalOfAmountOfWork = true;
                }
            }
        }

        // if a expenseRate is being set
        if (expenseRate != null)
        {
            Rate oldRate = null;
            oldRate = p_task.getExpenseRate();
            AmountOfWork oldAow = (oldRate == null) ? null : p_task
                    .getAmountOfWork(oldRate.getRateType());

            // need to create a clone because the rate already exists - so
            // don't want to try and insert but just set up the relationship
            taskClone.setExpenseRate(expenseRate);
            if (expenseRate.getRateType().equals(Rate.UnitOfWork.PAGE_COUNT))
            {
                // pageBasedRate = expenseRate;
            }
            else
            {
                if (expenseRate.getRateType().equals(Rate.UnitOfWork.HOURLY))
                {
                    AmountOfWork cloneNewAow = null;
                    String hourAmount = null;
                    boolean isEstimatedAmount = false;

                    // if the task is complete then need to be updating the
                    // actual hours - if not complete yet then update the
                    // estimated hours.
                    if ((hourAmount = p_tib.getActualHours()) == null)
                    {
                        isEstimatedAmount = true; // specifies that the
                        // estimated amount
                        // is being updated
                        hourAmount = p_tib.getEstimatedHours();
                    }

                    // if one exists
                    if (oldAow != null)
                    {
                        // if the amount is cleared and it is persisted
                        // delete the amount
                        if (hourAmount == null)
                        {
                            p_session.delete(oldAow);
                        }
                        else
                        // there is a new amount
                        {
                            cloneNewAow = oldAow;
                            // set the unit of work just in case it changed
                            cloneNewAow
                                    .setUnitOfWork(expenseRate.getRateType());
                            if (isEstimatedAmount)
                            {
                                cloneNewAow
                                        .setEstimatedAmount(Double
                                                .parseDouble((hourAmount
                                                        .length() == 0) ? "0"
                                                        : hourAmount));
                            }
                            else
                            {
                                cloneNewAow
                                        .setActualAmount(Double
                                                .parseDouble((hourAmount
                                                        .length() == 0) ? "0"
                                                        : hourAmount));
                            }
                            p_session.update(cloneNewAow);
                        }
                    }
                    else
                    // no previous uow
                    {
                        // if an estimate is to be set
                        if (hourAmount != null)
                        {
                            AmountOfWork newAow = taskClone.getExpenseRate()
                                    .createAmountOfWork();
                            if (isEstimatedAmount)
                            {
                                newAow.setEstimatedAmount(Double.parseDouble((hourAmount
                                        .length() == 0) ? "0" : hourAmount));
                            }
                            else
                            {
                                newAow.setActualAmount(Double.parseDouble((hourAmount
                                        .length() == 0) ? "0" : hourAmount));
                            }
                            cloneNewAow = newAow;
                            // p_session.save(cloneNewAow);
                        }
                    }
                    taskClone.setAmountOfWork(cloneNewAow);
                }
            }

        }
        else
        // NO RATE
        {
            // if there was a rate AND an amount of work specified
            // remove the amount of work
            if (p_task.getExpenseRate() != null
                    && p_task.getAmountOfWork(p_task.getExpenseRate()
                            .getRateType()) != null
                    && allowRemovalOfAmountOfWork)
            {
                AmountOfWork aow = p_task.getAmountOfWork(p_task
                        .getExpenseRate().getRateType());
                // remove relationship and delete
                taskClone.removeAmountOfWork(aow.getUnitOfWork());
                // if persistent
                if (aow != null)
                {
                    p_session.delete(aow);
                }
            }
            taskClone.setExpenseRate(null);
        }

        // if a revenueRate is being set
        if (revenueRate != null)
        {
            Rate oldRate = null;
            oldRate = p_task.getRevenueRate();
            AmountOfWork oldAow = (oldRate == null) ? null : p_task
                    .getAmountOfWork(oldRate.getRateType());

            // need to create a clone because the revenueRate already exists -
            // so
            // don't want to try and insert but just set up the relationship
            taskClone.setRevenueRate(revenueRate);
            if (revenueRate.getRateType().equals(Rate.UnitOfWork.PAGE_COUNT))
            {
                // pageBasedRate = revenueRate;
            }
            else
            {
                if (revenueRate.getRateType().equals(Rate.UnitOfWork.HOURLY))
                {
                    AmountOfWork cloneNewAow = null;
                    String hourAmount = null;
                    boolean isEstimatedAmount = false;

                    // if the task is complete then need to be updating the
                    // actual hours - if not complete yet then update the
                    // estimated hours.
                    if ((hourAmount = p_tib.getActualHours()) == null)
                    {
                        isEstimatedAmount = true; // specifies that the
                        // estimated amount
                        // is being updated
                        hourAmount = p_tib.getEstimatedHours();
                    }

                    // if one exists
                    if (oldAow != null)
                    {
                        // if the amount is cleared and it is persisted
                        // delete the amount
                        if (hourAmount == null)
                        {
                            p_session.delete(oldAow);
                        }
                        else
                        // there is a new amount
                        {
                            cloneNewAow = oldAow;
                            // set the unit of work just in case it changed
                            cloneNewAow
                                    .setUnitOfWork(revenueRate.getRateType());
                            if (isEstimatedAmount)
                            {
                                cloneNewAow
                                        .setEstimatedAmount(Double
                                                .parseDouble((hourAmount
                                                        .length() == 0) ? "0"
                                                        : hourAmount));
                            }
                            else
                            {
                                cloneNewAow
                                        .setActualAmount(Double
                                                .parseDouble((hourAmount
                                                        .length() == 0) ? "0"
                                                        : hourAmount));
                            }
                            p_session.update(cloneNewAow);
                        }
                    }
                    else
                    // no previous uow
                    {
                        // if an estimate is to be set
                        if (hourAmount != null)
                        {
                            AmountOfWork newAow = taskClone.getRevenueRate()
                                    .createAmountOfWork();
                            if (isEstimatedAmount)
                            {
                                newAow.setEstimatedAmount(Double.parseDouble((hourAmount
                                        .length() == 0) ? "0" : hourAmount));
                            }
                            else
                            {
                                newAow.setActualAmount(Double.parseDouble((hourAmount
                                        .length() == 0) ? "0" : hourAmount));
                            }
                            // cloneNewAow = (AmountOfWork) p_uow
                            // .registerNewObject(newAow);
                            cloneNewAow = newAow;
                            // p_session.save(cloneNewAow);
                        }
                    }
                    taskClone.setAmountOfWork(cloneNewAow);
                }
            }

        }
        else
        // No Rate
        {
            // if there was a rate AND an amount of work specified
            // remove the amount of work
            if (p_task.getRevenueRate() != null
                    && p_task.getAmountOfWork(p_task.getRevenueRate()
                            .getRateType()) != null
                    && allowRemovalOfAmountOfWork)
            {
                AmountOfWork aow = p_task.getAmountOfWork(p_task
                        .getRevenueRate().getRateType());
                // remove relationship and delete
                taskClone.removeAmountOfWork(aow.getUnitOfWork());
                // if persistent
                if (aow != null)
                {
                    p_session.delete(aow);
                }
            }
            taskClone.setRevenueRate(null);
        }
        p_session.saveOrUpdate(taskClone);

        if (taskClone instanceof TaskImpl)
        {
            TaskImpl task = (TaskImpl) taskClone;
            for (Object ob : task.getWorkSet())
            {
                if (ob instanceof AmountOfWork)
                {
                    AmountOfWork aWork = (AmountOfWork) ob;
                    p_session.saveOrUpdate(aWork);
                }
            }
        }

        return p_task;
    }

    /* delete tasks for the given workflow (a cloned object) based on the */
    /* i-Flow task instances */
    private void deleteTasks(Session p_session, Vector p_workflowTaskInstances,
            Workflow p_workflow, List p_wfTaskInfos) throws Exception
    {
        int size = p_workflowTaskInstances.size();
        for (int i = 0; i < size; i++)
        {
            WorkflowTaskInstance inst = (WorkflowTaskInstance) p_workflowTaskInstances
                    .elementAt(i);
            // Get a task from Toplink
            Task task = ServerProxy.getTaskManager().getTask(inst.getTaskId());
            if (s_logger.isDebugEnabled())
            {
                s_logger.debug("deleteTasks : " + " workflow="
                        + p_workflow.getId() + " WorkflowTaskInstance="
                        + WorkflowHelper.toDebugString(inst)
                        + GlobalSightCategory.getLineContinuation() + " task="
                        + task.toString());
            }
            // since we can also modify a workflow in Ready state, we need
            // to check before deletion (in Ready state Task has not been
            // created yet).
            if (task != null)
            {
                p_session.delete(task);
                p_workflow.removeTask(task);
            }
        }
    }

    private WorkflowServerWLRemote getWFServer() throws Exception
    {
        return ServerProxy.getWorkflowServer();
    }

    /*
     * return a list of WfTaskInfo object. The arrayList value: first is task
     * id, second is boolean value if need create second target file.
     */
    private ArrayList startWorkflow(Workflow p_workflow,
            DefaultPathTasks p_dpt, TaskEmailInfo p_emailInfo, Session p_session)
            throws Exception
    {
        List<WfTaskInfo> wfTaskInfos = getWFServer().startWorkflow(
                p_workflow.getId(), p_dpt, p_emailInfo);

        // set the state of the first task(s) of workflow to ACTIVE.
        // The returned tasks are all cloned.
        List<Task> nextTasks = updateTaskState(p_session, wfTaskInfos,
                p_workflow.getTasks(), Task.STATE_ACTIVE);

        return possiblyPerformSystemActions(wfTaskInfos, nextTasks, p_workflow,
                p_emailInfo.getProjectManagerId());
    }

    /**
     * Performs system action for a workflow.
     * <p>
     * The arrayList value: first is task id, second is boolean value if need
     * create second target file, third one is use emails, fourth one is action
     * type.
     */
    private ArrayList possiblyPerformSystemActions(
            List<WfTaskInfo> p_nextWfTaskInfos, List<Task> p_nextTasksCloned,
            Workflow p_workflow, String p_userId) throws Exception
    {
        ArrayList array = new ArrayList();
        if (!WorkflowTypeConstants.TYPE_TRANSLATION.equals(p_workflow
                .getWorkflowType()))
        {
            array.add(new Long(-1));
            array.add(false);
            array.add(null);
            array.add(null);
            return array;
        }
        Set<SecondaryTargetFile> stfs = p_workflow.getSecondaryTargetFiles();
        List extractedFiles = p_workflow
                .getTargetPages(PrimaryFile.EXTRACTED_FILE);
        // If the job doesn't have any extracted files in it then
        // don't create STFs. Un-extracted files are already in native/binary
        // format.
        if (extractedFiles.size() == 0)
        {
            array.add(new Long(-1));
            array.add(false);
            array.add(null);
            array.add(null);
            return array;
        }

        int size = p_nextWfTaskInfos == null ? 0 : p_nextWfTaskInfos.size();
        long taskId = -1;
        boolean flagSTF = false;
        String actionType = null;
        ArrayList userEmails = new ArrayList();
        // fix for GBS-1594
        Boolean stfState = true;
        Hashtable tasks = p_workflow.getTasks();
        Iterator it = tasks.keySet().iterator();
        while (it.hasNext())
        {
            Task task = (Task) tasks.get(it.next());
            if (Task.IN_PROGRESS.equals(task.getStfCreationState()))
            {
                stfState = false;
                break;
            }
        }
        for (int i = 0; i < size; i++)
        {
            WfTaskInfo wti = (WfTaskInfo) p_nextWfTaskInfos.get(i);
            actionType = wti.getActionType();

            if (((SystemAction.CSTF.equals(actionType) && stfs.size() == 0) || SystemAction.RSTF
                    .equals(actionType)) && stfState)
            {
                flagSTF = true;
            }

            taskId = wti.getId();
            userEmails = wti.userEmail;
        }

        if (flagSTF && taskId > 0)
        {
            // set the tasks creation of stf state to InProgress
            updateStfCreationStateForTask(p_nextTasksCloned, taskId,
                    Task.IN_PROGRESS);

        }

        array.add(taskId);
        array.add(flagSTF);
        array.add(userEmails);
        array.add(actionType);

        return array;
    }

    /**
     * Get the activity's type.
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

    /**
     * @param p_dispositionLists
     *            - Map of List of WorkflowTaskInstances keyed by one of
     *            WorkflowConstant.IS_NEW or IS_DELETED.
     * @param p_modified
     *            Tasks Hashtable of modified tasks - keyed by taskid. Value is
     *            TaskInfoBean with modifications
     */
    @SuppressWarnings("unchecked")
    private void persistWorkflowTaskInstanceChanges(Map p_dispositionLists,
            Hashtable p_modifiedTasks, WorkflowInstance p_wfInstance,
            List p_wfTaskInfos, Session p_session, Workflow p_workflowClone)
            throws Exception
    {
        List added = (List) p_dispositionLists.get(WorkflowConstants.IS_NEW);
        if (added != null && added.size() > 0)
        {
            createTasks(p_session, new Vector(added), p_modifiedTasks,
                    p_workflowClone, p_wfTaskInfos);
        }
        List deleted = (List) p_dispositionLists
                .get(WorkflowConstants.IS_DELETED);
        if (deleted != null && deleted.size() > 0)
        {
            deleteTasks(p_session, new Vector(deleted), p_workflowClone,
                    p_wfTaskInfos);
        }

        if (p_modifiedTasks != null && p_modifiedTasks.size() > 0)
        {
            modifyTasks(p_session, p_modifiedTasks, p_workflowClone,
                    p_wfTaskInfos);
        }
    }

    /*
     * Update the duration in the workflow - it may have changed.
     */
    private void updateDuration(long p_wfiId, List p_wfTaskInfos,
            Session session) throws WorkflowManagerException
    {
        try
        {
            long durationInMilli = 0;
            int size = p_wfTaskInfos.size();
            for (int i = 0; i < size; i++)
            {
                WfTaskInfo wfTaskInfo = (WfTaskInfo) p_wfTaskInfos.get(i);
                // get all the complete durations in the default path (not the
                // accept time)
                durationInMilli += wfTaskInfo.getCompletionDuration();
            }

            // convert the millisec to minutes since it's the smallest unit
            // of time used for each activity of a workflow.
            int duration = (int) (durationInMilli / 60000);

            Workflow wf = (Workflow) session.get(WorkflowImpl.class, new Long(
                    p_wfiId));
            wf.setDuration(duration);
            session.saveOrUpdate(wf);
        }
        catch (Exception e)
        {
            String eArgs[] = new String[1];
            eArgs[0] = Long.toString(p_wfiId);
            throw new WorkflowManagerException(
                    WorkflowManagerException.MSG_FAILED_TO_UPDATE_WORKFLOW_DURATION,
                    eArgs, e);
        }
    }

    /* Ensure that the given workflow has its i-flow instance set properly */
    private void refreshWorkflowInstance(Workflow p_workflow) throws Exception
    {
        if (p_workflow.getIflowInstance() == null)
        {
            p_workflow.setIflowInstance(getWFServer().getWorkflowInstanceById(
                    p_workflow.getId()));
        }
    }

    /* Change each page in the collection to the desired state. */
    private void updatePageState(Session p_session, Collection p_pages,
            String p_state) throws Exception
    {
        Iterator it = p_pages.iterator();
        while (it.hasNext())
        {
            // check if this is a valid state transition
            // Leave a page as IMPORT_FAIL
            // can't be NOT_LOCALIZED or OUT_OF_DATE because
            // this can be leveraged againt
            // currently can't re-import the file to change
            // to IMPORT_SUCCESS - so leave as a failure
            Page p = (Page) it.next();
            if (p.getPageState().equals(PG_IMPORT_FAIL))
            {
                continue; // skip to the next page
                // don't change this page state
            }
            p.setPageState(p_state);
            p_session.update(p);
        }
    }

    /* Change the state of each secondary target file. */
    private void updateSecondaryTargetFileState(Session p_session,
            Set<SecondaryTargetFile> p_stfs, String p_state) throws Exception
    {
        for (SecondaryTargetFile stf : p_stfs)
        {
            stf.setState(p_state);
            p_session.update(stf);
        }
    }

    /* Change the STF creation state of tasks. */
    private void updateStfCreationStateForTask(List p_nextTasksCloned,
            long p_taskId, String p_state) throws Exception
    {
        int size = p_nextTasksCloned.size();
        boolean found = false;
        for (int i = 0; !found && i < size; i++)
        {
            Task taskClone = (Task) p_nextTasksCloned.get(i);
            found = taskClone.getId() == p_taskId;

            if (found)
            {
                taskClone.setStfCreationState(p_state);
                // for GBS-3331: createSTF regard as exporting
                if (p_state.equals("IN_PROGRESS"))
                {
                    ArrayList<Long> workflowIds = new ArrayList<Long>();
                    workflowIds.add(taskClone.getWorkflow().getIdAsLong());
                    WorkflowExportingHelper.setAsExporting(workflowIds);
                }
            }
        }
    }

    /*
     * Update the task state to the specified state. The return a list of cloned
     * tasks (with updated state) which will be used for more possible updates.
     */
    private List<Task> updateTaskState(Session p_session,
            List<WfTaskInfo> p_nextWfTaskInfos, Hashtable p_tasks, int p_state)
            throws Exception
    {
        int size = p_nextWfTaskInfos.size();
        List<Task> tasks = new ArrayList<Task>(size);
        for (int i = 0; i < size; i++)
        {
            WfTaskInfo wfti = p_nextWfTaskInfos.get(i);
            Task task = (Task) p_tasks.get(new Long(wfti.getId()));
            Task taskClone = (Task) p_session.get(TaskImpl.class,
                    new Long(task.getId()));
            taskClone.setState(p_state);
            tasks.add(taskClone);
            p_session.saveOrUpdate(taskClone);
        }

        return tasks;
    }

    /*
     * Update the task state to the specified state. Each element of the object
     * array is of type WorkflowTaskInstance.
     */
    private void updateTaskState(Session p_session, Object[] p_activeTasks,
            Hashtable p_wfTasks, int p_state) throws Exception
    {
        int size = p_activeTasks == null ? -1 : p_activeTasks.length;
        for (int i = 0; i < size; i++)
        {
            WorkflowTaskInstance wfti = (WorkflowTaskInstance) p_activeTasks[i];
            Task task = (Task) p_wfTasks.get(new Long(wfti.getTaskId()));
            task.setState(p_state);
            p_session.saveOrUpdate(task);
        }
    }

    /* If the workflows all have the given p_wf state, then update the job */
    /* with the same state, and update the job's source pages with the */
    /* given pgState */
    private void possiblyUpdateJob(Session p_session, Workflow p_wf,
            String p_wfState) throws Exception
    {
        JobImpl job = (JobImpl) p_wf.getJob();
        String jobState = job.getState();
        int jobStateIndex = -1;
        for (int i = 0; i < ORDERED_STATES.length && jobStateIndex < 0; i++)
        {
            if (jobState.equals(ORDERED_STATES[i]))
            {
                jobStateIndex = i;
            }
        }
        int lowest = findLowestStateIndex(job.getWorkflows());
        if (jobStateIndex < lowest)
        {
            JobImpl jobClone = (JobImpl) p_session.get(JobImpl.class,
                    job.getIdAsLong());
            jobClone.setState(ORDERED_STATES[lowest]);
            if (!Workflow.EXPORT_FAILED.equals(p_wfState)
                    && WorkflowTypeConstants.TYPE_TRANSLATION.equals(p_wf
                            .getWorkflowType()))
            {
                updatePageState(
                        p_session,
                        jobClone.getSourcePages(),
                        ((lowest == LOCALIZED_STATE || lowest == EXPORTING_STATE) ? PG_LOCALIZED
                                : PG_ACTIVE_JOB));
            }
            p_session.saveOrUpdate(jobClone);
        }
        if (lowest >= LOCALIZED_STATE)
        {
            TaskEmailInfo p_emailInfo = createTaskEmailInfo(job, p_wf);
            p_emailInfo.setAssigneesName(job.getCreateUser().getFirstName());
            getWFServer().sendJobActionEmailToUser(
                    job.getCreateUser().getUserId(), p_emailInfo,
                    WorkflowMailerConstants.COMPLETED_JOB);
        }
    }

    /*
     * Return true if all workflows are in the given state.
     * 
     * If the given state is "CANCELLED" or "IMPORT_FAIL", check ALL workflows
     * against it; Otherwise, only check the given state against non-CANCELLED
     * workflows.
     */
    public static boolean workflowsHaveState(Collection p_workflows,
            String p_state)
    {
        boolean workflowsHaveState = true;
        Iterator it = p_workflows.iterator();
        if (p_state.equals(Workflow.CANCELLED)
                || p_state.equals(Workflow.IMPORT_FAILED))
        {
            while (workflowsHaveState && it.hasNext())
            {
                Workflow wf = (Workflow) it.next();
                workflowsHaveState &= (wf.getState().equals(Workflow.CANCELLED) || wf
                        .getState().equals(Workflow.IMPORT_FAILED));
            }
        }
        else
        {
            while (workflowsHaveState && it.hasNext())
            {
                String wfState = ((Workflow) it.next()).getState();
                if (!wfState.equals(Workflow.CANCELLED)
                        && !wfState.equals(Workflow.IMPORT_FAILED))
                {
                    workflowsHaveState &= wfState.equals(p_state);
                }
            }
        }
        return workflowsHaveState;
    }

    /**
     * Create the task tuvs for the given collection of tuvs and given task;
     * register all objects as part of the transaction of the given unit of work
     */
    private void createTaskTuvs(Workflow p_wf, Task p_task, String p_taskName,
            Session session) throws Exception
    {
        List targetPages = p_wf.getTargetPages(PrimaryFile.EXTRACTED_FILE);
        // Touch to load all TUs for pages to improve performance.
        for (Iterator it = targetPages.iterator(); it.hasNext();)
        {
            TargetPage tp = (TargetPage) it.next();
            SegmentTuUtil.getTusBySourcePageId(tp.getSourcePage().getId());
        }
        // get the TUVs of all the pages associated with extracted files
        Iterator tuvIt = getTuvsOfPages(targetPages).iterator();
        Iterator prevIt = getPreviousTaskTuvs(p_task.getId()).iterator();

        List<Tuv> tuvs = new ArrayList<Tuv>();
        List<TaskTuv> taskTuvs = new ArrayList<TaskTuv>();
        while (tuvIt.hasNext())
        {
            TuvImpl tuv = (TuvImpl) tuvIt.next();
            TaskTuv previousTaskTuv = getPreviousTaskTuvForThisTuv(tuv.getId(),
                    prevIt);
            TuvImpl tuvClone = new TuvImpl(tuv);
            tuvClone.setState(TuvState.OUT_OF_DATE);
            tuvClone.setIsIndexed(false);
            tuvClone.setCreatedDate(new Date());
            tuvClone.setTimestamp(new java.sql.Timestamp(System
                    .currentTimeMillis()));
            tuvClone.setLastModified(new Date());
            // set TuvId here to avoid "previousTuvId" = -1 below:
            // taskTuv.setPreviousTuv(tuvClone);
            SegmentTuTuvIndexUtil.setTuvId(tuvClone);
            tuvs.add(tuvClone);

            TaskTuv taskTuv = new TaskTuv();
            taskTuv.setCurrentTuv(tuv);
            taskTuv.setPreviousTuv(tuvClone);
            taskTuv.setVersion(getVersion(previousTaskTuv));
            taskTuv.setTask(p_task);
            taskTuv.setTaskName(p_taskName);
            taskTuvs.add(taskTuv);
        }

        Connection conn = DbUtil.getConnection();
        conn.setAutoCommit(false);
        try
        {
            SegmentTuvUtil.saveTuvs(conn, tuvs, p_wf.getJob().getId());
            conn.commit();
            HibernateUtil.save(taskTuvs);
        }
        finally
        {
            DbUtil.silentReturnConnection(conn);
        }
    }

    private TaskTuv getPreviousTaskTuvForThisTuv(long p_tuvId, Iterator p_it)
    {
        TaskTuv taskTuv = null;
        while (p_it.hasNext())
        {
            taskTuv = (TaskTuv) p_it.next();
            if (taskTuv.getCurrentTuvId() == p_tuvId)
            {
                return taskTuv;
            }
        }
        return taskTuv;
    }

    /* Get the tuvs of a collection of pages */
    @SuppressWarnings("unchecked")
    private Collection getTuvsOfPages(Collection p_pages) throws Exception
    {
        Iterator it = p_pages.iterator();
        Collection tuvs = new ArrayList();
        while (it.hasNext())
        {
            TargetPage p = (TargetPage) it.next();
            tuvs.addAll(getTuvsOfPage(p));
        }
        return tuvs;
    }

    private Collection getTuvsOfPage(TargetPage p_targetPage) throws Exception
    {
        return ServerProxy.getTuvManager().getTargetTuvsForStatistics(
                p_targetPage);
    }

    /* Get the specified number of previous task tuvs for the given tuv */
    private Collection getPreviousTaskTuvs(long p_taskId) throws Exception
    {
        HashMap<String, Long> map = new HashMap<String, Long>();
        map.put(TuvQueryConstants.TASK_ID_ARG, new Long(p_taskId));
        return HibernateUtil.searchWithSql(
                TuvQueryConstants.PREVIOUS_TASK_TUV_BY_TASK_ID, map,
                TaskTuv.class);
    }

    /* Get the version number for the given task tuv */
    private int getVersion(TaskTuv p_tt)
    {
        return (p_tt == null ? 1 : p_tt.getVersion() + 1);
    }

    // get the task email info for the given job.
    @SuppressWarnings("unchecked")
    private TaskEmailInfo createTaskEmailInfo(Job p_job, Workflow p_workflow)
            throws CommentException, RemoteException
    {
        WorkflowTemplateInfo wfti = null;
        if (WorkflowTypeConstants.TYPE_TRANSLATION.equals(p_workflow
                .getWorkflowType()))
        {
            wfti = p_job.getL10nProfile().getWorkflowTemplateInfo(
                    p_workflow.getTargetLocale());
        }
        else
        {
            wfti = p_job.getL10nProfile().getDtpWorkflowTemplateInfo(
                    p_workflow.getTargetLocale());
        }
        // get Job comments
        List commentsList = p_job.getJobComments();

        // comments don't include restricted attachment informations.
        StringBuffer comments = new StringBuffer();

        // restrictComments include restricted attachment informations.
        StringBuffer restrictComments = new StringBuffer();

        ArrayList list = new ArrayList();
        CommentManager manager = ServerProxy.getCommentManager();

        // attachment don't include restricted attachments.
        List attachment = new ArrayList();

        // restrictAttachment include restricted attachments.
        List restrictAttachment = new ArrayList();

        if (commentsList != null && commentsList.size() > 0)
        {
            list = new ArrayList(commentsList);
            for (int i = 0; i < list.size(); i++)
            {
                // Adds comment informations to comments.
                comments.append("\r\n " + (i + 1) + " -- ");
                CommentImpl aComment = (CommentImpl) list.get(i);
                String userName = UserUtil.getUserNameById(aComment
                        .getCreatorId());
                comments.append("Comment Creator: " + userName + "    ");
                comments.append("Date Created: " + aComment.getCreatedDate()
                        + "    ");
                comments.append("Comments: " + aComment.getComment() + "    ");
                comments.append("Attached Files: ");

                // Adds comment informations to restrictComments.
                restrictComments.append("\r\n " + (i + 1) + " -- ");
                restrictComments
                        .append("Comment Creator: " + userName + "    ");
                restrictComments.append("Date Created: "
                        + aComment.getCreatedDate() + "    ");
                restrictComments.append("Comments: " + aComment.getComment()
                        + "    ");
                restrictComments.append("Attached Files: ");

                ArrayList reference = manager.getCommentReferences(
                        String.valueOf(aComment.getId()),
                        WebAppConstants.COMMENT_REFERENCE_RESTRICTED_ACCESS,
                        true);

                boolean attachmentAdded = false;
                boolean restrictAttachmentAdded = false;

                // Adds attachments to comments, attachment, restrictComments
                // and restrictAttachment.
                for (Iterator it = reference.iterator(); it.hasNext();)
                {
                    CommentFile file = (CommentFile) it.next();
                    long filesize = file.getFileSize() < 3 ? 0 : file
                            .getFileSize();
                    if (filesize != 0)
                    {
                        filesize = (filesize % 1024 != 0) ? ((filesize / 1024) + 1)
                                : filesize / 1024;
                    }

                    // Adds all attachments information to restrictComments and
                    // restrictAttachment.
                    if (restrictAttachmentAdded)
                    {
                        restrictComments.append(", ");
                    }
                    else
                    {
                        restrictAttachmentAdded = true;
                    }

                    String fileName = file.getFilename();
                    if (file.getFileAccess()
                            .equals(WebAppConstants.COMMENT_REFERENCE_RESTRICTED_ACCESS))
                    {
                        fileName = fileName
                                + "("
                                + WebAppConstants.COMMENT_REFERENCE_RESTRICTED_ACCESS
                                + ")";
                    }

                    restrictComments.append(fileName + "  " + "size: "
                            + filesize + "k");
                    restrictAttachment.add(new File(AmbFileStoragePathUtils
                            .getCommentReferenceDir().getAbsoluteFile()
                            + File.separator
                            + String.valueOf(aComment.getId())
                            + File.separator
                            + file.getFileAccess()
                            + File.separator + file.getFilename()));

                    // Adds attachments to comments and attachment only if the
                    // attachment is not restricted.
                    if (!WebAppConstants.COMMENT_REFERENCE_RESTRICTED_ACCESS
                            .equals(file.getFileAccess()))
                    {
                        if (attachmentAdded)
                        {
                            comments.append(", ");
                        }
                        else
                        {
                            attachmentAdded = true;
                        }

                        comments.append(fileName + "  " + "size: " + filesize
                                + "k");
                        attachment.add(new File(AmbFileStoragePathUtils
                                .getCommentReferenceDir().getAbsoluteFile()
                                + File.separator
                                + String.valueOf(aComment.getId())
                                + File.separator
                                + file.getFileAccess()
                                + File.separator + file.getFilename()));
                    }
                }

                if (!attachmentAdded)
                {
                    comments.append("N/A");
                }

                if (!restrictAttachmentAdded)
                {
                    restrictComments.append("N/A");
                }
            }
        }

        TaskEmailInfo emailInfo = new TaskEmailInfo(
                p_job.getL10nProfile().getProject().getProjectManagerId(),
                p_workflow
                        .getWorkflowOwnerIdsByType(Permission.GROUP_WORKFLOW_MANAGER),
                wfti.notifyProjectManager(), p_job.getPriority(), comments
                        .toString());
        emailInfo.setJobName(p_job.getJobName());
        emailInfo.setProjectIdAsLong(new Long(p_job.getL10nProfile()
                .getProjectId()));
        emailInfo.setWfIdAsLong(p_workflow.getIdAsLong());
        emailInfo.setSourceLocale(p_job.getSourceLocale().toString());
        emailInfo.setTargetLocale(p_workflow.getTargetLocale().toString());
        emailInfo.setAttachment(attachment);
        emailInfo.setRestrictComments(restrictComments.toString());
        emailInfo.setRestrictAttachment(restrictAttachment);

        if (WorkflowTypeConstants.TYPE_TRANSLATION.equals(p_workflow
                .getWorkflowType()))
        {
            setWordCountDetails(p_job, p_workflow, emailInfo);
        }
        emailInfo.setJobId(new Long(p_job.getId()).toString());
        String projectName = p_job.getL10nProfile().getProject().getName();
        emailInfo.setProjectName(projectName);
        emailInfo.setCompanyId(String.valueOf(p_job.getCompanyId()));

        return emailInfo;
    }

    // determine exoprt type for creation of export batch event
    private String determineExportType(ExportParameters p_exportParams,
            Workflow p_workflow)
    {
        String exportType = null;
        if (p_exportParams.getExportType().equals(
                ExportConstants.EXPORT_FOR_STF_CREATION))
        {
            exportType = ExportBatchEvent.CREATE_STF;
        }
        else if (p_exportParams.getExportType().equals(
                ExportConstants.EXPORT_STF))
        {
            exportType = p_workflow.getState().equals(Workflow.LOCALIZED) ? ExportBatchEvent.FINAL_SECONDARY
                    : ExportBatchEvent.INTERIM_SECONDARY;
        }
        else
        {
            exportType = p_workflow.getState().equals(Workflow.LOCALIZED) ? ExportBatchEvent.FINAL_PRIMARY
                    : ExportBatchEvent.INTERIM_PRIMARY;
        }
        return exportType;
    }

    /**
     * After a workflow has been cancelled, the Job's state must be reset to the
     * lowest state of the remaining workflows, provided in the p_wfs collection
     * 
     * @param p_uow
     * @param p_job
     * @param p_wfs
     * @return job state
     * @exception Exception
     */
    private String resetJobState(Session p_session, JobImpl p_job,
            Collection p_wfs) throws Exception
    {
        return resetJobState(p_session, p_job, p_wfs, false);
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
    private String resetJobState(Session p_session, JobImpl p_job,
            Collection p_wfs, boolean p_reimport) throws Exception
    {
        if (p_wfs.size() == 0 || workflowsHaveState(p_wfs, WF_CANCELLED))
        {
            p_job.setState(WF_CANCELLED);
            if (p_reimport)
            {
                updatePageState(p_session, p_job.getSourcePages(),
                        PG_NOT_LOCALIZED);
            }
            else
            {
                updatePageState(p_session, p_job.getSourcePages(),
                        PG_OUT_OF_DATE);
            }
        }
        else
        {
            int lowest = findLowestStateIndex(p_wfs);
            p_job.setState(ORDERED_STATES[lowest]);
            if (lowest >= LOCALIZED_STATE)
            {
                updatePageState(
                        p_session,
                        p_job.getSourcePages(),
                        (lowest == LOCALIZED_STATE ? PG_LOCALIZED : PG_EXPORTED));
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
            String dataSourceType = DataSourceType.FILE_SYSTEM_AUTO_IMPORT;
            try {
                dataSourceType = (wf.getTargetPages().iterator().next())
                        .getDataSourceType();
            } catch (Exception ignore) {
            	
            }
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
        else
        {
            deleteFolderForDI(p_job.getCompanyId(), p_job.getJobId());
        }

        return jobState;
    }

    /* Find the index of the "lowest" state of any of the given workflows */
    public static int findLowestStateIndex(Collection p_wfs)
    {
        int lowest = ORDERED_STATES.length - 1;
        Iterator it = p_wfs.iterator();
        while (lowest >= 0 && it.hasNext())
        {
            String state = ((Workflow) it.next()).getState();
            for (int i = 0; i <= lowest; i++)
            {
                if (ORDERED_STATES[i].equals(state))
                {
                    lowest = i;
                }
            }
        }
        return lowest;
    }

    private void runJobCreationScript(Job p_job)
    {
        if (p_job.getL10nProfile().getNameOfJobCreationScript() != null)
        {
            String jobCreationScript = p_job.getL10nProfile()
                    .getNameOfJobCreationScript();
            try
            {
                String command = jobCreationScript + " "
                        + new Long(p_job.getId()).toString();
                s_logger.info("Running job creation script: " + command);

                // execute this job creation script in a separate process and
                // dedicate a
                // separate thread to watch its stdout and stderr
                ProcessRunner pr = new ProcessRunner(command,
                        JOBCREATION_SCRIPT_LOG, JOBCREATION_SCRIPT_ERR_LOG,
                        true);
                Date d = new Date();
                String threadName = "jobCreationScript" + d.getTime();
                Thread t = new Thread(pr, threadName);
                t.start();

                // wait for the job creation script to finish
                try
                {
                    t.join();
                }
                catch (InterruptedException e)
                {
                }
                s_logger.info("Job creation script finished.");

            }
            catch (Exception ioe)
            {
                s_logger.error("Unable to run job creation script "
                        + jobCreationScript + " job=" + p_job.toString(), ioe);
            }
        }
    }

    private void completeTask(String p_userId, Task p_task,
            String p_destinationArrow, String skipping)
            throws WorkflowManagerException
    {
        boolean isCompleted = false; // true if all workflows are complete
        boolean isManualImportFileSystemJob = true;
        boolean isTranslationWorkflow = false;
        boolean isDbJob = false;
        Workflow wfClone = null;
        TaskEmailInfo emailInfo = null;
        long jobId = -2;
        long taskId = -2;
        long wfId = -2;

        if (p_task == null)
        {
            s_logger.error("Task is null in _setTaskCompletion() for user: "
                    + UserUtil.getUserNameById(p_userId));
        }
        else
        {
            taskId = p_task.getId();
        }
        Session session = HibernateUtil.getSession();
        Transaction tx = session.beginTransaction();
        JbpmContext ctx = null;

        try
        {
            Date currentTime = new Date();
            TaskImpl task = (TaskImpl) session.get(TaskImpl.class, new Long(
                    p_task.getId()));
            task.setCompletedDate(currentTime);
            task.setState(TaskImpl.STATE_COMPLETED);
            wfClone = (Workflow) task.getWorkflow();
            isTranslationWorkflow = WorkflowTypeConstants.TYPE_TRANSLATION
                    .equals(wfClone.getWorkflowType());
            wfId = wfClone.getId();
            if (isTranslationWorkflow)
            {
                for (TargetPage tPage : wfClone.getTargetPages())
                {
                    String type = tPage.getDataSourceType();
                    if (!DataSourceType.FILE_SYSTEM.equals(type))
                    {
                        isManualImportFileSystemJob = false;
                        isDbJob = DataSourceType.DATABASE.equals(type);
                        break;
                    }

                }
            }
            else
            {
                isManualImportFileSystemJob = false;
                isDbJob = false;
            }

            ArrorInfo arrorInfo = new ArrorInfo(p_task.getId(),
                    p_destinationArrow);

            // -1 indicates that the default path would be from the
            // beginning of the workflow (form START node)
            List wfTaskInfos = getWFServer().timeDurationsInDefaultPath(
                    wfClone.getId(), -1, arrorInfo,
                    getWFServer().getWorkflowInstanceById(wfClone.getId()));

            updateDuration(wfClone.getId(), wfTaskInfos, session);

            DefaultPathTasks dpt = updateDefaultPathTasks(ADVANCE_ACTION,
                    currentTime, wfTaskInfos, wfClone, task.getId(), session);

            Job job = wfClone.getJob();
            jobId = job.getId();
            emailInfo = createTaskEmailInfo(job, wfClone);
            if (job != null && p_task != null)
            {
                if (s_logger.isDebugEnabled())
                {
                    s_logger.debug("Advancing task " + p_task.getTaskName()
                            + ", taskId " + taskId + ", wfId " + wfId
                            + ", job " + job.getJobName() + ", jobId " + jobId
                            + ", user " + UserUtil.getUserNameById(p_userId));
                }
            }

            WorkflowInstanceInfo wfInstInfo = getWFServer().advanceTask(
                    wfClone, p_userId, task.getId(), p_destinationArrow, dpt,
                    emailInfo, skipping);

            int wfState = wfInstInfo.getState();
            isCompleted = wfState == WorkflowConstants.STATE_COMPLETED;
            if (isCompleted)
            {
                if (s_logger.isDebugEnabled())
                {
                    s_logger.debug("workflow state is completed for workflow instance "
                            + wfInstInfo.getId());
                }
            }

            // update the completion fraction
            updateCompletionFraction(wfClone, p_task, wfTaskInfos, isCompleted);
            long stfTaskId = -1;
            boolean createSTF = false;
            String actionType = null;
            long nextTaskId = -1;

            if (isCompleted)
            {
                if (isManualImportFileSystemJob)
                {
                    wfClone.setState(WF_LOCALIZED);
                }
                else
                {
                    if (skipping == null)
                    {
                        wfClone.setState(WF_LOCALIZED);
                    }
                    else
                    {
                        wfClone.setState(WF_EXPORTING);
                    }
                }
                if (WorkflowTypeConstants.TYPE_DTP.equals(wfClone
                        .getWorkflowType()))
                {
                    wfClone.setState(WF_EXPORTED);
                }
                wfClone.setCompletedDate(currentTime);
                if (isTranslationWorkflow)
                {
                    updatePageState(session, wfClone.getTargetPages(),
                            PG_LOCALIZED);
                    updateSecondaryTargetFileState(session,
                            wfClone.getSecondaryTargetFiles(),
                            SecondaryTargetFileState.LOCALIZED);
                }
                possiblyUpdateJob(session, wfClone, WF_LOCALIZED);

                // Set up completed date of job
                job.setCompletedDate(currentTime);
                session.saveOrUpdate(job);
            }
            else
            {
                if (isTranslationWorkflow)
                {
                    createTaskTuvs(wfClone, task, p_task.getTaskName(), session);
                }

                List<WfTaskInfo> nextTaskInfos = wfInstInfo.getNextTaskInfos();
                // update the state of the next active tasks
                List<Task> nextTasks = updateTaskState(session, nextTaskInfos,
                        wfClone.getTasks(), Task.STATE_ACTIVE);
                // perform the creation of STF if necessary
                ArrayList returnValue = possiblyPerformSystemActions(
                        nextTaskInfos, nextTasks, wfClone,
                        emailInfo.getProjectManagerId());

                if (isTranslationWorkflow)
                {
                    stfTaskId = ((Long) (returnValue.get(0))).longValue();
                    createSTF = ((Boolean) returnValue.get(1)).booleanValue();
                    actionType = returnValue.get(3) != null ? (String) returnValue
                            .get(3) : null;
                }

                nextTaskId = ((Long) returnValue.get(0)).longValue();
            }

            session.saveOrUpdate(task);
            tx.commit();

            if (createSTF)
            {
                s_logger.debug("Exporting for STF creation, taskId="
                        + stfTaskId);
                exportForStfCreation(new Long(stfTaskId), wfClone, p_userId);
            }
            // GBS-3002
            if (actionType != null
                    && (skipping == null || skipping.startsWith("LAST")))
            {
                SystemActionPerformer.perform(actionType, stfTaskId, p_userId);
            }

            // now remove the reserved time from the user's calendar.
            // This does not need to be part of the same transaction
            removeReservedTime(task.getId(), p_userId);
        }
        catch (AmbassadorDwUpException e)
        {
            s_logger.error("Failed to create job on GS Edtion", e);
        }
        catch (Exception e)
        {
            tx.rollback();
            s_logger.error("Failed to complete task " + taskId + ", workflow "
                    + wfId + ", job " + jobId, e);
            String taskIdString = "null task id!";
            if (p_task != null)
            {
                Long id = new Long(p_task.getId());
                taskIdString = id.toString();
            }
            String[] args = new String[1];
            args[0] = taskIdString;
            throw new WorkflowManagerException(
                    WorkflowManagerException.MSG_FAILED_TO_SET_TASK_COMPLETION,
                    args, e);
        }
        finally
        {
            if (ctx != null)
            {
                ctx.close();
            }
            // despite errors above, if the job is complete, then attempt the
            // export
            // but re-check job state from TopLink instead of relying on the
            // above boolean
            if (!isManualImportFileSystemJob)
            {
                if (wfClone != null)
                {
                    String projectMgrUserId = null;
                    if (emailInfo != null)
                        projectMgrUserId = emailInfo.getProjectManagerId();
                    else
                    {
                        s_logger.warn("Project Mgr Id is null for task "
                                + taskId + ", workflow " + wfId + ", job "
                                + jobId);
                    }

                    try
                    {
						exportLocalizedWorkflow(wfClone.getId(),
								projectMgrUserId, isDbJob, isCompleted);
                    }
                    catch (Throwable t)
                    {
                        // log the error but don't let it affect job completion
                        s_logger.error(
                                "Error trying to export for completed workflow.",
                                t);
                    }
                }
                else
                {
                    s_logger.warn("Not exporting wf for task " + taskId
                            + ", workflow " + wfId + ", job " + jobId);
                }
            }
        }
    }

    /**
     * Process the files if the source file is with XLZ file format
     * 
     * @param p_wf
     * @author Vincent Yan, 2011/01/27
     */
    private void processXLZFiles(Workflow p_wf)
    {
        if (p_wf == null || p_wf.getAllTargetPages().size() == 0)
            return;

        TargetPage tp = null;
        String externalId = "";
        String tmp = "", tmpFile = "";
        String sourceFilename = "", targetFilename = "";
        String sourceDir = "", targetDir = "";
        File sourceFile = null, targetFile = null;
        File sourcePath = null, targetPath = null;
        ArrayList<String> xlzFiles = new ArrayList<String>();

        try
        {
            Vector targetPages = p_wf.getAllTargetPages();
            String baseDir = AmbFileStoragePathUtils.getCxeDocDirPath().concat(
                    File.separator);

            Job job = p_wf.getJob();
            String companyId = String.valueOf(job.getCompanyId());
            String companyName = CompanyWrapper.getCompanyNameById(companyId);

            if ("1".equals(CompanyWrapper.getCurrentCompanyId())
                    && !"1".equals(job.getCompanyId()))
            {
                baseDir += companyName + File.separator;
            }

            for (int i = 0; i < targetPages.size(); i++)
            {
                tp = (TargetPage) targetPages.get(i);
                externalId = tp.getSourcePage().getExternalPageId();

                if (externalId.toLowerCase().endsWith(".xlf")
                        || externalId.toLowerCase().endsWith(".xliff"))
                {
                    tmp = externalId.substring(0,
                            externalId.lastIndexOf(File.separator));
                    sourceFilename = baseDir + tmp + ".xlz";
                    sourceFile = new File(sourceFilename);
                    if (sourceFile.exists() && sourceFile.isFile())
                    {
                        // source file is with xlz file format
                        targetDir = baseDir + tp.getExportSubDir()
                                + tmp.substring(tmp.indexOf(File.separator));
                        if (!xlzFiles.contains(targetDir))
                            xlzFiles.add(targetDir);

                        // Get exported target path
                        targetPath = new File(targetDir);

                        // Get source path
                        sourceDir = baseDir + tmp;
                        sourcePath = new File(sourceDir);

                        // Copy all files extracted from xlz file from source
                        // path to exported target path
                        // Because xliff files can be exported by GS
                        // auotmatically, then ignore them and
                        // just copy the others file to target path
                        File[] files = sourcePath.listFiles();
                        for (File f : files)
                        {
                            if (f.isDirectory())
                                continue;
                            tmpFile = f.getAbsolutePath().toLowerCase();
                            if (tmpFile.endsWith(".xlf")
                                    || tmpFile.endsWith(".xliff"))
                                continue;
                            FileUtils.copyFileToDirectory(f, targetPath);
                        }
                    }
                }

                // Verify if the exported file is generated
                targetFilename = baseDir + tp.getExportSubDir()
                        + File.separator;
                targetFilename += externalId.substring(externalId
                        .indexOf(File.separator) + 1);
                targetFile = new File(targetFilename);
                while (!targetFile.exists())
                {
                    Thread.sleep(1000);
                }
            }

            // Generate exported XLZ file and remove temporary folders
            for (int i = 0; i < xlzFiles.size(); i++)
            {
                targetDir = xlzFiles.get(i);
                targetPath = new File(targetDir);

                ZipIt.addEntriesToZipFile(new File(targetDir + ".xlz"),
                        targetPath.listFiles(), true, "");
            }
        }
        catch (Exception e)
        {
            s_logger.error("Error in WorkflowManagerLocal.processXLZFiles. "
                    + e.toString());
        }
    }

    /**
     * Set the workflow word count details for email notification.
     */
    private void setWordCountDetails(Job p_job, Workflow p_workflow,
            TaskEmailInfo emailInfo)
    {
        int totalFuzzy = p_workflow.getThresholdHiFuzzyWordCount()
                + p_workflow.getThresholdLowFuzzyWordCount()
                + p_workflow.getThresholdMedFuzzyWordCount()
                + p_workflow.getThresholdMedHiFuzzyWordCount();
        int repetitionCount = p_workflow.getRepetitionWordCount();

        emailInfo.setWordCountDetails(p_job.getLeverageMatchThreshold(),
                p_workflow.getThresholdNoMatchWordCount(), repetitionCount,
                totalFuzzy, p_workflow.getSegmentTmWordCount(),
                p_workflow.getTotalWordCount());
    }

    /**
     * Performs the export if the workflow is localized, otherwise does nothing.
     */
    private void exportLocalizedWorkflow(long p_wfId, String p_userId,
            boolean p_isDbJob, boolean isCompleted) throws Exception
    {
        if (s_logger.isDebugEnabled())
        {
            s_logger.debug("exportLocalizedWorkflow: " + " p_wfId=" + p_wfId);
        }

        Workflow wf = getWorkflowByIdRefresh(p_wfId);
        if (WorkflowTypeConstants.TYPE_DTP.equals(wf.getWorkflowType()))
        {
            return;
        }
        if (!isCompleted)
        {
            if (s_logger.isDebugEnabled())
            {
                s_logger.debug("Workflow " + p_wfId
                        + " is not localized yet; not exporting it.");
            }
            return;
        }
        if (Workflow.LOCALIZED.equals(wf.getState()))
        {
            if (s_logger.isDebugEnabled())
            {
                s_logger.debug("Workflow " + p_wfId
                        + " is localized; exporting it.");
            }
            // from GBS-2137, add "Exporting" state before export is done
            JobCreationMonitor.updateWorkflowState(wf, Workflow.EXPORTING);
            JobCreationMonitor.updateJobStateToExporting(wf.getJob());
        }

        // since at FileProfile level a flag is set to determine whether
        // primary or secondary target pages should be automatically exported
        // we need to check for this flag here (only for non-DB datasource).
        boolean shouldExportStf = false;
        if (!p_isDbJob)
        {
            Object[] requests = wf.getJob().getRequestList().toArray();
            long fpId = ((Request) requests[0]).getDataSourceId();
            FileProfile fp = ServerProxy.getFileProfilePersistenceManager()
                    .readFileProfile(fpId);
            shouldExportStf = fp.byDefaultExportStf();
        }

        ExportParameters exportParams = shouldExportStf ? new ExportParameters(
                wf, null, null, null, ExportConstants.NOT_SELECTED,
                ExportConstants.EXPORT_STF) : new ExportParameters(wf);

        String exportEncoding = exportParams.getExportCodeset();

        if (exportEncoding != null)
        {
            exportParams.setExportCodeset(exportEncoding += "_di");
        }

        ArrayList<Long> workflowIds = new ArrayList<Long>();
        workflowIds.add(p_wfId);
        WorkflowExportingHelper.setAsExporting(workflowIds);

        performExport(wf, p_userId, null, exportParams, shouldExportStf);
    }

    // perform an interim export for the purpose of secondary target file
    // creation.
    private void exportForStfCreation(Long p_taskId, Workflow p_workflow,
            String p_userId) throws Exception
    {
        try
        {
            ExportParameters exportParams = new ExportParameters(p_workflow,
                    null, null, null, ExportConstants.NO_UTF_BOM,
                    ExportConstants.EXPORT_FOR_STF_CREATION);

            performExport(p_workflow, p_userId, p_taskId, exportParams, false);
        }
        catch (Exception e)
        {
            // update task state to "FAILED"
            ServerProxy.getTaskManager().updateStfCreationState(
                    p_taskId.longValue(), Task.FAILED);
            throw e;
        }
    }

    // perform export process for target pages or Secondary Target Files.
    private void performExport(Workflow p_workflow, String p_userId,
            Long p_taskId, ExportParameters p_exportParams,
            boolean p_shouldExportStf) throws Exception
    {
        List genericPages = null;
        List<Long> ids = new ArrayList<Long>();
        boolean doRegularExport = true;
        ExportParameters exportParams = p_exportParams;

        if (p_shouldExportStf)
        {
            Set<SecondaryTargetFile> stfs = p_workflow
                    .getSecondaryTargetFiles();
            // only perform export if there are stfs
            if (stfs.size() > 0)
            {
                doRegularExport = false;
                for (SecondaryTargetFile stf : stfs)
                {
                    ids.add(stf.getIdAsLong());
                }
                // register for export notification
                long exportBatchId = createExportBatchId(exportParams,
                        p_workflow, p_userId, ids, p_taskId);
                ServerProxy.getPageManager().exportSecondaryTargetFiles(
                        exportParams, ids, exportBatchId);
            }
            else
            {
                s_logger.warn("The file profile says to export STFs as a default, yet there are no STFs\r\n"
                        + "for this workflow either because an STF stage was bypassed in the path taken\r\n"
                        + "through the workflow, or because of workflow modification and deletion of an STF\r\n"
                        + "task. The primary target files will be exported instead.");
                doRegularExport = true;
                // create new export params since the original export params
                // were wrong
                exportParams = new ExportParameters(p_workflow);
            }
        }

        if (doRegularExport)
        {
            genericPages = p_workflow.getTargetPages();
            int size = genericPages == null ? 0 : genericPages.size();

            for (int i = 0; i < size; i++)
            {
                ids.add(((TargetPage) genericPages.get(i)).getIdAsLong());
            }

            // register for export notification
            long exportBatchId = createExportBatchId(exportParams, p_workflow,
                    p_userId, ids, p_taskId);

            // Export Documentum Workflows
            TargetPage trgpage = (TargetPage) genericPages.get(0);
            SourcePage srcPage = trgpage.getSourcePage();
            String category = srcPage.getDataSourceType();
            // Are these documentum workflows.
            if (category.equalsIgnoreCase(DocumentumOperator.DCTM_CATEGORY))
            {

                String eventFlowXml = srcPage.getRequest().getEventFlowXml();
                EventFlowXmlParser parser = new EventFlowXmlParser();
                parser.parse(eventFlowXml);

                try
                {
                    org.w3c.dom.Element msCategory = parser
                            .getCategory(DocumentumOperator.DCTM_CATEGORY);
                    s_logger.debug("Starting to export a documentum workflow......");
                    String srcObjId = parser.getCategoryDaValue(msCategory,
                            DocumentumOperator.DCTM_OBJECTID)[0];
                    String userId = parser.getCategoryDaValue(msCategory,
                            DocumentumOperator.DCTM_USERID)[0];

                    // Copy a file object and return a new object id.
                    String newObjId = DocumentumOperator.getInstance().doCopy(
                            userId, srcObjId, null);
                    if (newObjId == null)
                    {
                        s_logger.error("Failed to execute copy operation for DCTM Object");
                        throw new Exception();
                    }
                    s_logger.debug("New DCTM file object Id :" + newObjId);
                    exportParams.setNewObjectId(newObjId);
                }
                catch (NoSuchElementException nsex)
                {
                    s_logger.debug("Not a documentum Workflow");
                }
            }

            boolean isTargetPage = true;
            ServerProxy.getPageManager().exportPage(exportParams, ids,
                    isTargetPage, exportBatchId);
        }
    }

    // create the export batch id for this export process.
    private long createExportBatchId(ExportParameters p_exportParams,
            Workflow p_workflow, String p_userId, List ids, Long p_taskId)
            throws Exception
    {
        // just to get a "list" for the notification call below.
        List<Long> workflowIds = new ArrayList<Long>();
        workflowIds.add(p_workflow.getIdAsLong());

        String exportType = determineExportType(p_exportParams, p_workflow);

        long exportBatchId = ExportEventObserverHelper
                .notifyBeginExportTargetBatch(p_workflow.getJob(),
                        ExportEventObserverHelper.getUser(p_userId), ids,
                        workflowIds, p_taskId, exportType);

        return exportBatchId;
    }

    /*
     * Returns a TaskInfo that contains the estimated dates (mainly estimated
     * completion date) based on the user calendar. For a task that has not been
     * accepted yet, it'll associate a list of possible assignees with their
     * respective estimated completion date (as TaskAssignee object added to
     * TaskInfo).
     */
    private TaskInfo estimateDatesForDefaultPath(Date p_baseDate, Task p_task,
            WfTaskInfo p_wfTaskInfo, boolean p_acceptedOnly) throws Exception
    {
        TaskInfo ti = null;
        // the NodeInstance state (deactive, active, complete)
        int state = p_wfTaskInfo.getState();

        if (state == WorkflowConstants.STATE_INITIAL)
        {
            ti = getTaskInfobyRole(p_task, p_baseDate, p_wfTaskInfo);

        }
        else if (state == WorkflowConstants.TASK_ACTIVE)
        {
            String acceptor = p_task.getAcceptor();
            if (acceptor == null || !p_acceptedOnly)
            {
                ti = getTaskInfobyRole(p_task, p_baseDate, p_wfTaskInfo);
            }
            else
            {
                ti = getTaskInfoForUser(acceptor, p_task, p_wfTaskInfo,
                        WorkflowConstants.TASK_ACCEPTED,
                        p_task.getEstimatedCompletionDate());
            }
        }
        else if (state == WorkflowConstants.STATE_COMPLETED)
        {
            if (!p_acceptedOnly)
            {
                ti = getTaskInfobyRole(p_task, p_baseDate, p_wfTaskInfo);
            }
            else
            {
                String user = p_task.getAcceptor();
                ti = getTaskInfoForUser(user, p_task, p_wfTaskInfo, state,
                        p_task.getCompletedDate());
            }
        }

        return ti;
    }

    /*
     * Start the workflow and set the appropriate attributes for dispatch. The
     * arrayList value: first is task id, second is boolean value if need create
     * second target file.
     */
    @SuppressWarnings("deprecation")
    private ArrayList dispatchWorkflow(Workflow p_wfClone, Session p_session,
            Date p_startDate, TaskEmailInfo p_emailInfo) throws Exception
    {
        p_wfClone.setState(WF_DISPATCHED);
        p_wfClone.setDispatchedDate(p_startDate);
        if (WorkflowTypeConstants.TYPE_TRANSLATION.equals(p_wfClone
                .getWorkflowType()))
        {
            updatePageState(p_session, p_wfClone.getTargetPages(),
                    PG_ACTIVE_JOB);
        }
        // '-1' indicates that the default path would be from the
        // beginning of the workflow (form START node)
        List wfTaskInfos = getWFServer().timeDurationsInDefaultPath(null,
                p_wfClone.getId(), -1);

        DefaultPathTasks dpt = updateDefaultPathTasks(DISPATCH_ACTION,
                p_startDate, wfTaskInfos, p_wfClone, -1, p_session);
        p_wfClone.setPlannedCompletionDate(p_wfClone
                .getEstimatedCompletionDate());
        updateCompletionFraction(p_wfClone, wfTaskInfos);

        // For sla report issue
        p_wfClone.updateTranslationCompletedDates();

        return startWorkflow(p_wfClone, dpt, p_emailInfo, p_session);
    }

    /*
     * Remove the reserved times for the active tasks of a workflow.
     */
    public static void removeReservedTimes(Object[] p_activeWfTaskInstances)
    {
        int size = p_activeWfTaskInstances == null ? -1
                : p_activeWfTaskInstances.length;

        for (int i = 0; i < size; i++)
        {
            WorkflowTaskInstance wfti = (WorkflowTaskInstance) p_activeWfTaskInstances[i];

            removeReservedTime(wfti.getTaskId());
        }
    }

    /*
     * Find the task that's before the activity with the given id. @param
     * p_nodeId - The node id used for finding the node before it. @param
     * p_wfTaskInfos - A list of tasks in the default path. @param p_tasks - The
     * workflow tasks.
     */
    private Task findPreviousTask(long p_nodeId, List p_wfTaskInfos,
            Hashtable p_tasks)
    {
        Task previousTask = null;

        int size = p_wfTaskInfos.size();
        boolean found = false;
        for (int i = 0; (!found && i < size); i++)
        {
            WfTaskInfo wfTaskInfo = (WfTaskInfo) p_wfTaskInfos.get(i);
            found = wfTaskInfo.getId() == p_nodeId;
            if (!found)
            {
                previousTask = (Task) p_tasks.get(new Long(wfTaskInfo.getId()));
            }
        }

        return previousTask;
    }

    /*
     * Get the task info for a particular user.
     */
    private TaskInfo getTaskInfoForUser(String p_userId, Task p_task,
            WfTaskInfo p_wfTaskInfo, int p_state, Date p_completionDate)
            throws Exception
    {
        // if accepted, only show acceptor, estimated completion
        EmailInformation ei = ServerProxy.getUserManager()
                .getEmailInformationForUser(p_userId);
        String userFullName = p_userId;
        if (ei != null)
        {
            userFullName = ei.getUserFullName();
        }
        else
        {
            s_logger.warn("User '" + UserUtil.getUserNameById(p_userId)
                    + "' appears to have no email information.");
        }

        TaskInfo ti = new TaskInfo(p_wfTaskInfo.getId(),
                p_wfTaskInfo.getName(), p_state,
                p_task.getEstimatedAcceptanceDate(),
                p_task.getEstimatedCompletionDate(), p_task.getAcceptedDate(),
                p_completionDate, p_task.getType());
        ti.addTaskAssignee(p_userId, userFullName,
                p_task.getEstimatedCompletionDate());
        return ti;
    }

    /**
     * Get the task info based on a role associated with the task. The task info
     * will have a list of TaskAssignee objects representing each possible
     * assignee of the task.
     */
    private TaskInfo getTaskInfobyRole(Task p_task, Date p_baseDate,
            WfTaskInfo p_wfTaskInfo) throws Exception
    {
        Project project = p_task.getWorkflow().getJob().getL10nProfile()
                .getProject();
        UserManager um = ServerProxy.getUserManager();
        String[] p_userIds = um.getUserIdsFromRoles(p_wfTaskInfo.getRoles(),
                project);
        List info = um.getEmailInformationForUsers(p_userIds);
        int size = 0;
        if (info != null)
            size = info.size();
        TaskInfo ti = new TaskInfo(p_wfTaskInfo.getId(),
                p_wfTaskInfo.getName(), p_wfTaskInfo.getState(),
                p_task.getEstimatedAcceptanceDate(),
                p_task.getEstimatedCompletionDate(), null,
                p_task.getEstimatedCompletionDate(), p_task.getType());

        for (int i = 0; i < size; i++)
        {
            EmailInformation ei = (EmailInformation) info.get(i);
            UserFluxCalendar cal = ServerProxy.getCalendarManager()
                    .findUserCalendarByOwner(ei.getUserId());

            Date completeBy = ServerProxy.getEventScheduler().determineDate(
                    p_baseDate, cal, p_wfTaskInfo.getTotalDuration());
            ti.addTaskAssignee(ei.getUserId(), ei.getUserFullName(), completeBy);
        }
        return ti;
    }

    /**
     * Update the default path tasks by setting their estimated acceptance, and
     * completion times. Also set the estimated completion time for the
     * workflow.
     */
    @SuppressWarnings("unchecked")
    private Map modifyWorkflowInstance(Date p_baseDate, String p_sessionId,
            WorkflowInstance p_wfInstance, TaskEmailInfo p_emailInfo,
            String p_companyId) throws Exception
    {
        Vector wfTaskInstances = p_wfInstance.getWorkflowInstanceTasks();
        int size = wfTaskInstances.size();
        DefaultPathTasks dpt = null;
        boolean isReassigned = false;
        HashMap<String, Object> map = new HashMap<String, Object>(1);
        for (int i = 0; (!isReassigned && i < size); i++)
        {
            WorkflowTaskInstance wft = (WorkflowTaskInstance) wfTaskInstances
                    .get(i);
            boolean isActive = wft.getTaskState() == WorkflowConstants.TASK_ACTIVE;

            if (isActive)
            {
                // add the id of the active node to the map. This is used for
                // updating the default path (so it starts from the active
                // node).
                map.put(ACTIVE_NODE_ID, new Long(wft.getTaskId()));
            }

            isReassigned = isActive && wft.isReassigned();

            if (isReassigned)
            {
                dpt = new DefaultPathTasks();
                FluxCalendar calendar = ServerProxy.getCalendarManager()
                        .findDefaultCalendar(p_companyId);

                Date acceptBy = ServerProxy.getEventScheduler().determineDate(
                        p_baseDate, calendar, wft.getAcceptTime());

                Date completeBy = ServerProxy.getEventScheduler()
                        .determineDate(p_baseDate, calendar,
                                (wft.getAcceptTime() + wft.getCompletedTime()));

                dpt.addTaskInfo(new TaskInfo(wft.getTaskId(), wft
                        .getActivityName(), wft.getTaskState(), acceptBy,
                        completeBy, null, null, wft.getActivity().getType()));

                // This is used to determine the base date in updating
                // the default path
                map.put(REASSIGNED_NODE_ID, Boolean.TRUE);
            }
        }
        map.putAll(getWFServer().modifyWorkflowInstance(p_sessionId,
                p_wfInstance, dpt, p_emailInfo));

        return map;
    }

    /**
     * Update the default path tasks by setting their estimated acceptance, and
     * completion times. Also set the estimated completion time for the
     * workflow. Note that if the p_completedTaskId is <= 0, the dates for all
     * of the nodes in the path would be udpated. However, a valid id for
     * p_completedTaskId means that only the task with the given id and the ones
     * after it will be updated.
     * 
     * @param p_baseDate
     *            - The based date used for finding dates based on durations.
     * @param p_wfTaskInfos
     *            - The tasks (as WfTaskInfo objects) in default path.
     * @param p_wfClone
     *            - The workflow that needs to be udpated.
     * @param p_completedTaskId
     *            - The id of the base task.
     * @param p_uow
     *            - Unit of work for persistance purposes.
     */
    private DefaultPathTasks updateDefaultPathTasks(int p_actionType,
            Date p_baseDate, List p_wfTaskInfos, Workflow p_wfClone,
            long p_completedTaskId, Session p_session) throws Exception
    {
        return updateDefaultPathTasks(p_actionType, p_baseDate, p_wfTaskInfos,
                p_wfClone, p_completedTaskId, false, p_baseDate, p_session);
    }

    /*
     * Update the default path tasks by setting their estimated acceptance, and
     * completion times. Also set the estimated completion time for the
     * workflow. Note that if the p_completedTaskId is <= 0, the dates for all
     * of the nodes in the path would be udpated. However, a valid id for
     * p_completedTaskId means that only the task with the given id and the ones
     * after it will be updated.
     * 
     * @param p_baseDate - The based date used for finding dates based on
     * durations. @param p_wfTaskInfos - The tasks (as WfTaskInfo objects) in
     * default path. @param p_wfClone - The workflow that needs to be udpated.
     * 
     * @param p_completedTaskId - The id of the base task. @param
     * p_isActiveTaskReassigned - True if the active task is reassigned. @param
     * p_originalBaseDate - Either the completion date of previous task or
     * simply the workflow's dispatch date. @param p_uow - Unit of work for
     * persistance purposes.
     */
    private DefaultPathTasks updateDefaultPathTasks(int p_actionType,
            Date p_baseDate, List p_wfTaskInfos, Workflow p_wfClone,
            long p_completedTaskId, boolean p_isActiveTaskReassigned,
            Date p_originalBaseDate, Session p_session) throws Exception
    {
        int size = p_wfTaskInfos.size();
        Hashtable ht = p_wfClone.getTasks();
        FluxCalendar defaultCalendar = ServerProxy.getCalendarManager()
                .findDefaultCalendar(String.valueOf(p_wfClone.getCompanyId()));
        UserFluxCalendar userCalendar = null;

        DefaultPathTasks dpt = new DefaultPathTasks();
        Date estimatedDate = p_isActiveTaskReassigned ? p_baseDate
                : p_originalBaseDate;

        boolean found = p_completedTaskId <= 0;
        // loop thru the tasks following the given start task for updating
        // the estimated dates for the tasks and possibly workflow.
        boolean firstTime = true;
        boolean activeTaskAccepted = false;
        for (int i = 0; i < size; i++)
        {
            WfTaskInfo wfTaskInfo = (WfTaskInfo) p_wfTaskInfos.get(i);
            if (!found)
            {
                found = p_completedTaskId == wfTaskInfo.getId();
            }
            else
            {
                TaskImpl task = (TaskImpl) ht.get(new Long(wfTaskInfo.getId()));

                if (task != null)
                {
                    Date acceptBy = ServerProxy.getEventScheduler()
                            .determineDate(estimatedDate, defaultCalendar,
                                    wfTaskInfo.getAcceptanceDuration());

                    task.setEstimatedAcceptanceDate(acceptBy);

                    long duration = wfTaskInfo.getTotalDuration();

                    if (firstTime)
                    {
                        firstTime = false;
                        if (activeTaskAccepted = (task.getAcceptor() != null && task
                                .getAcceptedDate()
                                .compareTo(p_originalBaseDate) >= 0))
                        {
                            removeReservedTime(task.getId(), task.getAcceptor());

                            userCalendar = ServerProxy
                                    .getCalendarManager()
                                    .findUserCalendarByOwner(task.getAcceptor());

                            if (p_isActiveTaskReassigned)
                            {
                                // reset acceptor and acceptance date
                                task.setAcceptor(null);
                                task.setAcceptedDate(null);
                                task.setState(TaskImpl.STATE_ACTIVE);
                            }
                            else
                            {
                                estimatedDate = task.getAcceptedDate();
                                duration = wfTaskInfo.getCompletionDuration();
                            }
                        }
                        // possibly create proposed type reserved times
                        createProposedReservedTimes(p_actionType,
                                estimatedDate, wfTaskInfo, task,
                                ReservedTime.TYPE_PROPOSED,
                                p_isActiveTaskReassigned, p_session);
                    }

                    Date completeBy = ServerProxy
                            .getEventScheduler()
                            .determineDate(
                                    estimatedDate,
                                    userCalendar == null ? (BaseFluxCalendar) defaultCalendar
                                            : (BaseFluxCalendar) userCalendar,
                                    duration);

                    task.setEstimatedCompletionDate(completeBy);

                    if (activeTaskAccepted && !p_isActiveTaskReassigned)
                    {
                        addReservedTimeToUserCalendar(
                                userCalendar,
                                buildReservedTimeName(wfTaskInfo.getName(),
                                        task), ReservedTime.TYPE_ACTIVITY,
                                task.getAcceptedDate(), completeBy,
                                task.getIdAsLong(), p_session);
                    }

                    estimatedDate = completeBy;
                    activeTaskAccepted = false; // reset it
                    TaskInfo ti = new TaskInfo(task.getId(),
                            task.getTaskName(), task.getState(), acceptBy,
                            completeBy, null, null, task.getType());

                    if (wfTaskInfo.getOverdueToPM() != 0)
                    {
                        ti.setOverdueToPM(wfTaskInfo.getOverdueToPM());
                    }

                    if (wfTaskInfo.getOverdueToUser() != 0)
                    {
                        ti.setOverdueToUser(wfTaskInfo.getOverdueToUser());
                    }

                    dpt.addTaskInfo(ti);
                    p_session.saveOrUpdate(task);
                }
            }

            // There's an edge case where all nodes could go thru on common
            // decision node. Therefore, let's start from the START node...
            if (i == (size - 1) && dpt.size() == 0
                    && !wfTaskInfo.followedByExitNode())
            {
                i = -1;
                found = true;
            }
        }

        // For sla report issue.
        // No need to set estimated completion date for a workflow if
        // the advanced task happens to be the last one.
        // User can override the estimatedCompletionDate.
        if ((!estimatedDate.equals(p_baseDate))
                && (!p_wfClone.isEstimatedCompletionDateOverrided()))
        {
            p_wfClone.setEstimatedCompletionDate(estimatedDate);
            sendNotification(p_wfClone);
        }

        // For sla report issue.
        p_wfClone.updateTranslationCompletedDates();

        return dpt;
    }

    /*
     * Remove the reserved time associated with the given task from the user's
     * calendar. The user is the finisher of the activity.
     */
    private void removeReservedTime(long p_taskId, String p_userId)
    {
        try
        {
            ServerProxy.getCalendarManager().removeScheduledActivity(p_taskId,
                    p_userId);
        }
        catch (Exception e)
        {
            s_logger.error("Could not remove reserved times for task id "
                    + p_taskId, e);
            throw new GeneralException(e);
        }
    }

    /*
     * Remove the reserved time associated with the given task from the user's
     * calendar. The user is the finisher of the activity.
     */
    private static void removeReservedTime(long p_taskId)
    {
        try
        {
            ServerProxy.getCalendarManager()
                    .removeScheduledActivities(p_taskId);
        }
        catch (Exception e)
        {
            s_logger.error("Failed to remove reserved times for task id "
                    + p_taskId, e);
        }
    }

    /**
     * Update the structural changes that may have happened to the modified
     * workflow. Also update the durations and completion fraction of the
     * workflow. If the workflow is in dispatched state, all the estimated
     * acceptance/completion times starting from the active node should be
     * updated.
     * 
     * @param p_modifiedTasks
     *            - A hashtable of the modified tasks. The key is the Task id
     *            and the value is a TaskInfoBean (contians modified
     *            attributes).
     * @param p_wfInstance
     *            - The workflow instance that was modified.
     * @param p_wfTaskInfos
     *            - The list of tasks in the default path.
     * @param p_uow
     *            - Unit of work.
     * @param p_wfClone
     *            - A clones Workflow object (workflow to be modified).
     * @param p_addedAndDeleted
     *            - A map containing the added/deleted nodes.
     * @param p_baseDate
     *            - Current date that might be used as a base date.
     */
    private void updateWorkflowChanges(Hashtable p_modifiedTasks,
            WorkflowInstance p_wfInstance, List p_wfTaskInfos,
            Session p_session, Workflow p_wfClone, Map p_addedAndDeleted,
            Date p_baseDate) throws Exception
    {
        // persist all the structural changes
        persistWorkflowTaskInstanceChanges(p_addedAndDeleted, p_modifiedTasks,
                p_wfInstance, p_wfTaskInfos, p_session, p_wfClone);

        updateCompletionFraction(p_wfClone, p_wfTaskInfos);

        // only update the estimated dates for a dispatched workflow
        if (WF_DISPATCHED.equals(p_wfClone.getState()))
        {
            Long nodeId = (Long) p_addedAndDeleted.get(ACTIVE_NODE_ID);
            // check for reassignment of the active node
            Object reassigned = p_addedAndDeleted.get(REASSIGNED_NODE_ID);

            Task previousTask = findPreviousTask(nodeId.longValue(),
                    p_wfTaskInfos, p_wfClone.getTasks());
            // if no reassignment is taken place, the based date should
            // be the completion date of the node before the active task
            // or the workflow dispatch date if the active node happens to
            // be the first activity node in the workflow.
            Date originalBaseDate = previousTask == null ? p_wfClone
                    .getDispatchedDate() : previousTask.getCompletedDate();

            if (originalBaseDate != null)
            {
                // now update the estimated acceptance/completion times
                updateDefaultPathTasks(MODIFY_ACTION, p_baseDate,
                        p_wfTaskInfos, p_wfClone, previousTask == null ? -1
                                : previousTask.getId(), reassigned != null,
                        originalBaseDate, p_session);
            }
            else
            {
                s_logger.error("Not updating default path tasks because originalBaseDate is null.");
            }
        }
    }

    /**
     * Removes the source corpus docs from the corpus and any applicable corpus
     * mappings, and removes the link between the source page and the corpus
     * doc. The assumption is that there is nothing to do with target pages
     * because they only get created in the corpus at TM population time, and
     * then it is too late to remove them.
     * 
     * @param p_job
     *            canceled Job
     */
    public static void cleanCorpus(Long p_jobId)
    {
        if (!Modules.isCorpusInstalled())
            return;

        try
        {
            Job job = ServerProxy.getJobHandler().getJobById(
                    p_jobId.longValue());
            Iterator iter = job.getSourcePages().iterator();
            CorpusManagerWLRemote corpusMgr = ServerProxy.getCorpusManager();
            while (iter.hasNext())
            {
                SourcePage sp = (SourcePage) iter.next();
                try
                {
                    corpusMgr.removeSourceCorpusDoc(sp);
                }
                catch (Exception e)
                {
                    s_logger.error("Could not remove corpus doc for page "
                            + sp.getExternalPageId());
                }
            }
        }
        catch (Exception e)
        {
            s_logger.error("Could not remove corpus docs for job " + p_jobId, e);
        }
    }

    /*
     * Create 'proposed' reserved time for the assignees of the specified task.
     */
    private void createProposedReservedTimes(int p_actionType, Date p_baseDate,
            WfTaskInfo p_wfTaskInfo, TaskImpl p_taskClone, String p_type,
            boolean p_isActiveTaskReassigned, Session p_session)
            throws Exception
    {
        // if calendaring module is not installed, don't create reserved times.
        if (!Modules.isCalendaringInstalled())
        {
            return;
        }

        if (p_actionType == ADVANCE_ACTION || p_actionType == DISPATCH_ACTION)
        {
            createReservedTimes(p_baseDate, p_wfTaskInfo, p_taskClone, p_type,
                    p_session);
        }
        else if (p_actionType == MODIFY_ACTION && p_isActiveTaskReassigned)
        {
            // remove the old proposed/actual reserved times
            ServerProxy.getCalendarManager().removeScheduledActivities(
                    p_taskClone.getId());

            createReservedTimes(p_baseDate, p_wfTaskInfo, p_taskClone, p_type,
                    p_session);
        }
    }

    /*
     * Create reserved times based on the given type.
     */
    private void createReservedTimes(Date p_baseDate, WfTaskInfo p_wfTaskInfo,
            TaskImpl p_taskClone, String p_type, Session p_session)
            throws Exception
    {
        String[] userIds = ServerProxy.getUserManager().getUserIdsFromRoles(
                p_wfTaskInfo.getRoles(),
                p_taskClone.getWorkflow().getJob().getL10nProfile()
                        .getProject());
        for (int i = 0; i < userIds.length; i++)
        {
            UserFluxCalendar cal = ServerProxy.getCalendarManager()
                    .findUserCalendarByOwner(userIds[i]);
            Date completeBy = ServerProxy.getEventScheduler().determineDate(
                    p_baseDate, cal, p_wfTaskInfo.getTotalDuration());

            addReservedTimeToUserCalendar(cal,
                    buildReservedTimeName(p_wfTaskInfo.getName(), p_taskClone),
                    p_type, p_baseDate, completeBy, p_taskClone.getIdAsLong(),
                    p_session);
        }
    }

    /*
     * Build a user friendly name for an activity type reserved time.
     */
    private String buildReservedTimeName(String p_activityName, TaskImpl p_task)
    {
        StringBuffer taskName = new StringBuffer();
        taskName.append("[");
        taskName.append(p_activityName);
        taskName.append("]");
        taskName.append("[");
        taskName.append(p_task.getJobName());
        taskName.append("]");
        taskName.append("[");
        taskName.append(p_task.getProjectManagerId());
        taskName.append("]");
        return taskName.toString();
    }

    /*
     * Create a reserved time and add it to the specified user calendar.
     */
    private void addReservedTimeToUserCalendar(UserFluxCalendar p_userCalendar,
            String p_name, String p_type, Date p_startDate, Date p_completeBy,
            Long p_taskId, Session p_session) throws Exception
    {
        // if calendaring module is not installed, don't create reserved times.
        if (!Modules.isCalendaringInstalled())
        {
            return;
        }

        // create new reserved time for user
        TimeZone tz = p_userCalendar.getTimeZone();
        Timestamp start = new Timestamp(tz);
        start.setDate(p_startDate);
        Timestamp end = new Timestamp(tz);
        end.setDate(p_completeBy);

        ReservedTime rt = new ReservedTime(p_name, p_type, start,
                start.getHour(), start.getMinute(), end, end.getHour(),
                end.getMinute(), null, p_taskId);

        // cloneCalendar.addReservedTime(rtClone);
        p_userCalendar.addReservedTime(rt);
        p_session.save(rt);

        // now add the buffer (if not set to zero)
        if (ReservedTime.TYPE_ACTIVITY.equals(p_type)
                && p_userCalendar.getActivityBuffer() > 0)
        {
            Timestamp bufferEnd = new Timestamp(tz);
            bufferEnd.setDate(end.getDate());
            bufferEnd.add(Timestamp.HOUR, p_userCalendar.getActivityBuffer());
            ReservedTime buffer = new ReservedTime(p_name,
                    ReservedTime.TYPE_BUFFER, end, end.getHour(),
                    end.getMinute(), bufferEnd, bufferEnd.getHour(),
                    bufferEnd.getMinute(), null, p_taskId);
            p_userCalendar.addReservedTime(buffer);
            p_session.save(buffer);
        }
        p_session.saveOrUpdate(p_userCalendar);
    }

    /*
     * Notify the PM and WFM if the estimated completion date exceeds planned
     * date. It's deprecated For sla report issue.
     */
    @SuppressWarnings("deprecation")
    private void sendNotification(Workflow p_workflow)
    {
        if (!m_systemNotificationEnabled)
        {
            return;
        }

        if (p_workflow.getPlannedCompletionDate() != null
                && p_workflow.getEstimatedCompletionDate().after(
                        p_workflow.getPlannedCompletionDate()))
        {
            try
            {
                SystemConfiguration config = SystemConfiguration.getInstance();
                GlobalSightLocale targetLocale = p_workflow.getTargetLocale();
                Job job = p_workflow.getJob();
                String companyIdStr = String.valueOf(job.getCompanyId());

                WorkflowTemplateInfo wfti = job.getL10nProfile()
                        .getWorkflowTemplateInfo(targetLocale);
                Date estimatedCompletionDate = p_workflow
                        .getEstimatedCompletionDate();
                Date plannedDate = p_workflow.getPlannedCompletionDate();
                List wfManagerIds = p_workflow
                        .getWorkflowOwnerIdsByType(Permission.GROUP_WORKFLOW_MANAGER);

                // Job -> name (id)
                StringBuffer jobInfo = new StringBuffer();
                jobInfo.append(job.getJobName());
                jobInfo.append(" (");
                jobInfo.append(job.getId());
                jobInfo.append(")");

                String[] messageArguments = new String[5];
                messageArguments[0] = jobInfo.toString();
                messageArguments[4] = config
                        .getStringParameter(SystemConfigParamNames.CAP_LOGIN_URL);

                EmailInformation emailInfo = null;
                Timestamp ts = null;
                int size = wfManagerIds.size();
                // notify workflow managers (if any)
                for (int i = 0; i < size; i++)
                {
                    emailInfo = ServerProxy.getUserManager()
                            .getEmailInformationForUser(
                                    (String) wfManagerIds.get(i));

                    Locale userLocale = emailInfo.getEmailLocale();
                    ts = new Timestamp(emailInfo.getUserTimeZone());
                    ts.setDate(estimatedCompletionDate);
                    ts.setLocale(userLocale);
                    // workflow name (target locale)
                    messageArguments[1] = targetLocale
                            .getDisplayName(userLocale);
                    // estimated completion date
                    messageArguments[2] = ts.toString();

                    ts.setDate(plannedDate);
                    // planned date
                    messageArguments[3] = ts.toString();

                    ServerProxy.getMailer().sendMail((EmailInformation) null,
                            emailInfo,
                            MailerConstants.ESTIMATED_EXCEEDS_PLANNED_DATE,
                            "estimatedExceedsPlanned", messageArguments,
                            companyIdStr);
                }

                if (wfti.notifyProjectManager())
                {
                    emailInfo = ServerProxy.getUserManager()
                            .getEmailInformationForUser(
                                    wfti.getProjectManagerId());

                    Locale userLocale = emailInfo.getEmailLocale();
                    ts = new Timestamp(emailInfo.getUserTimeZone());
                    ts.setDate(estimatedCompletionDate);
                    ts.setLocale(userLocale);
                    // workflow name (target locale)
                    messageArguments[1] = targetLocale
                            .getDisplayName(userLocale);
                    // estimated completion date
                    messageArguments[2] = ts.toString();

                    ts.setDate(plannedDate);
                    // planned date
                    messageArguments[3] = ts.toString();

                    ServerProxy.getMailer().sendMail((EmailInformation) null,
                            emailInfo,
                            MailerConstants.ESTIMATED_EXCEEDS_PLANNED_DATE,
                            "estimatedExceedsPlanned", messageArguments,
                            companyIdStr);
                }
            }
            catch (Exception e)
            {
                s_logger.error(
                        "Failed to notify Project Manager about workflow's estimated completion date exceeding planned date.",
                        e);
            }
        }
    }

    /**
     * Returns true if the user is allowed to cancel jobs and/or workflows.
     */
    private boolean allowedToCancelJobs(String p_userId)
    {
        boolean allowedToCancel = false;
        try
        {
            PermissionSet userPerms = Permission.getPermissionManager()
                    .getPermissionSetForUser(p_userId);
            allowedToCancel = userPerms
                    .getPermissionFor(Permission.JOBS_DISCARD);
        }
        catch (Exception e)
        {
            s_logger.error(
                    "Failed to see if user "
                            + UserUtil.getUserNameById(p_userId)
                            + " has permission to cancel workflows.", e);
            allowedToCancel = false;
        }
        return allowedToCancel;
    }

    /**
     * Returns true if the user is allowed to cancel workflows. Currently this
     * checks if the user can manage workflows.
     * (Permission.PROJECTS_MANAGE_WORKFLOWS)
     */
    private boolean allowedToCancelWorkflows(String p_userId)
    {
        boolean allowedToCancel = false;
        try
        {
            PermissionSet userPerms = Permission.getPermissionManager()
                    .getPermissionSetForUser(p_userId);
            allowedToCancel = userPerms
                    .getPermissionFor(Permission.PROJECTS_MANAGE_WORKFLOWS);
        }
        catch (Exception e)
        {
            s_logger.error(
                    "Failed to see if user "
                            + UserUtil.getUserNameById(p_userId)
                            + " has permission to cancel workflows.", e);
            allowedToCancel = false;
        }
        return allowedToCancel;
    }

    public static void deleteInProgressTmData(Job p_job)
            throws WorkflowManagerException
    {
        try
        {
            InProgressTmManager inProgressTmManager = LingServerProxy
                    .getInProgressTmManager();
            inProgressTmManager.deleteSegments(p_job.getId());
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new WorkflowManagerException(e);
        }
    }

    /**
     * Make sure no page is in UPDATING state for the given workflow.
     */
    private void validateStateOfPages(Workflow p_workflow)
            throws WorkflowManagerException
    {
        try
        {
            PageStateValidator.validateStateOfPagesInWorkflow(p_workflow);
        }
        catch (Exception e)
        {
            throw new WorkflowManagerException(e);
        }
    }

    /**
     * Make sure no page is in UPDATING state for the given job.
     */
    private void validateStateOfPagesInJob(Job p_job)
            throws WorkflowManagerException
    {
        try
        {
            PageStateValidator.validateStateOfPagesInJob(p_job);
        }
        catch (Exception e)
        {
            throw new WorkflowManagerException(e);
        }
    }

    public List<SkipActivityVo> getLocalActivity(String[] workflowIds)
            throws WorkflowManagerException, RemoteException
    {
        return getLocalActivity(workflowIds, null);
    }

    public List<SkipActivityVo> getLocalActivity(String[] workflowIds,
            Locale locale) throws WorkflowManagerException, RemoteException
    {
        List<SkipActivityVo> list = new ArrayList<SkipActivityVo>(
                workflowIds.length);
        SkipActivityVo vo = null;
        Long workflowId = null;

        for (String sworkflowId : workflowIds)
        {
            vo = new SkipActivityVo();
            workflowId = Long.parseLong(sworkflowId);
            Workflow workflow = getWorkflowById(workflowId);
            List<WorkflowTaskInstance> workflowInstances = ServerProxy
                    .getWorkflowServer().getUnVisitedTasksForWorkflow(
                            workflowId);
            List<Entry> entryList = getEntryList(workflowInstances);
            vo.setList(entryList);
            String targetLocale;
            if (locale == null)
            {
                targetLocale = workflow.getTargetLocale().getDisplayName();
            }
            else
            {
                targetLocale = workflow.getTargetLocale()
                        .getDisplayName(locale);
            }
            vo.setTargetLocale(targetLocale);
            vo.setWorkflowId(workflowId);
            list.add(vo);

        }
        return list;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.globalsight.everest.workflowmanager.WorkflowManager#setSkip(java.
     * util.List)
     */
    public void setSkip(List<Entry> list, String userId)
            throws RemoteException, WorkflowManagerException
    {
        try
        {
            ServerProxy.getWorkflowServer().setSkipActivity(list, userId, true);
        }
        catch (Exception e)
        {
            s_logger.error("Skip activity error", e);
        }
    }

    private List<Entry> getEntryList(
            List<WorkflowTaskInstance> workflowInstances)
    {
        List<Entry> list = new ArrayList<Entry>(workflowInstances.size() + 1);
        Entry<String, String> entry;

        for (WorkflowTaskInstance workflowTaskInstance : workflowInstances)
        {
            String value = workflowTaskInstance.getActivityName();
            String key = WorkflowConstants.END_NODE.equals(value) ? value
                    : workflowTaskInstance.getActivity().getDisplayName();
            entry = new Entry<String, String>(key, value);
            list.add(entry);
        }

        return list;
    }

    /**
     * Change workflow priority.
     * 
     */
    public void updatePriority(long p_workflowId, int p_priority)
            throws WorkflowManagerException, RemoteException
    {
        Session session = HibernateUtil.getSession();
        Transaction tx = session.beginTransaction();

        try
        {
            Workflow wf = (Workflow) session.get(WorkflowImpl.class, new Long(
                    p_workflowId));
            wf.setPriority(p_priority);
            session.saveOrUpdate(wf);
            tx.commit();
        }
        catch (Exception e)
        {
            String[] args =
            { String.valueOf(p_workflowId) };
            throw new WorkflowManagerException(
                    WorkflowManagerException.MSG_FAILED_TO_UPDATE_PCD, args, e);
        }
    }

    public static TaskInstance getCurrentTask(long wfid)
    {
        List<TaskInstance> tasks = getTaskHistoryByWorkflowId(wfid);
        TaskInstance task = null;
        if (tasks.size() > 0)
        {
            task = tasks.get(tasks.size() - 1);

            if (task.getEnd() != null)
            {
                task = null;
            }
        }

        return task;
    }

    /**
     * Return tasks in sequence for specified workflow, skipped tasks are
     * ignored.
     * 
     * @param id
     *            -- workflow ID.
     * @return -- List<TaskInstance>
     */
    public static List<TaskInstance> getTaskHistoryByWorkflowId(long id)
    {
        JbpmContext ctx = WorkflowConfiguration.getInstance().getJbpmContext();
        try
        {
            Session dbSession = ctx.getSession();
            String hql = "from TaskInstance t where t.token.id = :wid order by t.id asc";
            Query query = dbSession.createQuery(hql);
            query.setParameter("wid", id);
            List<TaskInstance> tasks = query.list();
            if (tasks != null && tasks.size() > 0)
            {
                Set<Long> skipTaskIds = getSkippedTaskIds(tasks);
                for (Iterator<TaskInstance> it = tasks.iterator(); it.hasNext();)
                {
                    TaskInstance task = it.next();
                    if (skipTaskIds.contains(task.getId()))
                    {
                        it.remove();
                    }
                }
            }

            return tasks;
        }
        finally
        {
            ctx.close();
        }
    }

    /**
     * Return task IDs that are skipped.
     * 
     * @param tasks
     * @return
     */
    private static Set<Long> getSkippedTaskIds(List<TaskInstance> tasks)
    {
        Set<Long> skippedTaskIds = new HashSet<Long>();

        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try
        {
            StringBuilder taskIds = new StringBuilder();
            for (TaskInstance task : tasks)
            {
                taskIds.append(task.getId()).append(",");
            }
            String taskIdsInStr = taskIds.substring(0, taskIds.length() - 1);

            String sql = "SELECT DISTINCT taskinstance_id FROM jbpm_gs_variable "
                    + "WHERE taskinstance_id IN ("
                    + taskIdsInStr
                    + ") "
                    + "AND name = 'skip' ";

            con = DbUtil.getConnection();
            ps = con.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs != null && rs.next())
            {
                skippedTaskIds.add(rs.getLong(1));
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            DbUtil.silentClose(rs);
            DbUtil.silentClose(ps);
            DbUtil.silentReturnConnection(con);
        }

        return skippedTaskIds;
    }

    /* Get email address of a user based on the user name */
    private EmailInformation getEmailInfo(String p_userName) throws Exception
    {
        return ServerProxy.getUserManager().getEmailInformationForUser(
                p_userName);
    }
}
