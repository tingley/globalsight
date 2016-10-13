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

package com.globalsight.everest.request.reimport;

// globalsight
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;
import java.util.Vector;

import javax.servlet.ServletException;

import org.apache.log4j.Logger;

import com.globalsight.everest.comment.Comment;
import com.globalsight.everest.foundation.L10nProfile;
import com.globalsight.everest.foundation.Timestamp;
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.foundation.WorkObject;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.page.Page;
import com.globalsight.everest.page.PageException;
import com.globalsight.everest.page.PageState;
import com.globalsight.everest.page.PageStateValidator;
import com.globalsight.everest.page.SourcePage;
import com.globalsight.everest.permission.Permission;
import com.globalsight.everest.persistence.PersistenceException;
import com.globalsight.everest.projecthandler.WorkflowTemplateInfo;
import com.globalsight.everest.request.RequestImpl;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.taskmanager.Task;
import com.globalsight.everest.usermgr.UserManager;
import com.globalsight.everest.util.system.SystemConfiguration;
import com.globalsight.everest.webapp.pagehandler.tasks.TaskHelper;
import com.globalsight.everest.workflow.EventNotificationHelper;
import com.globalsight.everest.workflow.TaskEmailInfo;
import com.globalsight.everest.workflow.WorkflowMailerConstants;
import com.globalsight.everest.workflow.WorkflowServer;
import com.globalsight.everest.workflowmanager.Workflow;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.persistence.pageimport.delayedimport.DelayedImportQuery;
import com.globalsight.scheduling.SchedulerConstants;
import com.globalsight.scheduling.SchedulingInformation;
import com.globalsight.util.AmbFileStoragePathUtils;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.mail.MailerConstants;

/**
 * Provides the capabilities for re-importing a page that has a previous version
 * of it currently in an active job. The re-import is delayed and the
 * appropriate people notified.
 */
public class ActivePageReimporter
{

    // constants for various import options
    public final static int NO_REIMPORT = 0;
    public final static int DELAY_REIMPORT = 1;
    public final static int REIMPORT_NEW_TARGETS = 2;

    // for logging purposes
    private static Logger s_logger = Logger
            .getLogger(ActivePageReimporter.class.getName());

    // singleton
    private static ActivePageReimporter c_instance = null;

    // email messag keys
    private static String PAGE_REIMPORT_TO_PM_MESSAGE = "pageReimportedToPmMessage";
    private static String PAGE_FAILED_TO_REIMPORT_MESSAGE = "pageFailedReimportToPmMessage";

    // determines whether the system-wide notification is enabled
    private boolean m_systemNotificationEnabled = EventNotificationHelper
            .systemNotificationEnabled();

    /**
     * Returns the singleton instance
     */
    static public ActivePageReimporter getInstance()
    {
        if (c_instance == null)
        {
            // two or more threads may be here
            synchronized (ActivePageReimporter.class)
            {
                if (c_instance == null)
                {
                    c_instance = new ActivePageReimporter();
                }
            }
        }
        return c_instance;
    }

    /**
     * Get the number of milliseconds for the reimport delay
     */
    static public long getReimportStallTime()
    {
        long timeInMillisecs = 0;
        try
        {
            timeInMillisecs = Long.parseLong(SystemConfiguration.getInstance()
                    .getStringParameter(
                            SystemConfiguration.REIMPORT_DELAY_MILLISECONDS));
        }
        catch (Exception e)
        {
            // just log an error
            s_logger.error("Failed to get the number of minutes to delay reimiport.");
        }
        return timeInMillisecs;
    }

    /**
     * Returns the reimport option.
     */
    static public int getReimportOption()
    {
        try
        {
            SystemConfiguration config = SystemConfiguration.getInstance();
            return config.getIntParameter(SystemConfiguration.REIMPORT_OPTION);
        }
        catch (Exception e)
        {
            s_logger.error("Failed to retrive the value that specifies if reimport of an active "
                    + "page is allowed.");
            // if it can't find it then return 0 - safest not to allow
            // re-import.
            return NO_REIMPORT;
        }
    }

    /**
     * Returns 'true' if the reimport option is set up for delay re-import,
     * returns 'false' otherwise.
     */
    static public boolean isDelayReimport()
    {
        return getReimportOption() == DELAY_REIMPORT ? true : false;
    }

    /**
     * Delay the import of this request because the previous request/page is
     * already in progress.
     */
    public void delayImport(RequestImpl p_request, SourcePage p_page)
            throws ReimporterException
    {
        Job j = null;
        Exception ex = null;

        // create and persist the request
        DelayedImportRequest dir = createDelayedRequest(p_request, p_page);

        try
        {
            DelayedImportQuery diq = new DelayedImportQuery();
            long id = diq.findJobIdOfPage(p_page.getId());
            j = loadJobFromDbIntoCache(id);
        }
        catch (Exception e)
        {
            // will probably still be null - but just in case
            j = null;
            ex = e;
        }

        // this catches if an exception was thrown
        // or if the method call returns a NULL job.
        if (j == null)
        {
            s_logger.error(
                    "Failed to find the job associated with active page "
                            + p_page.getId(), ex);
            String[] args =
            { Long.toString(p_page.getId()), p_page.getExternalPageId() };
            throw new ReimporterException(
                    ReimporterException.MSG_FAILED_TO_FIND_JOB_OF_PAGE, args,
                    ex);
        }
        else
        {
            // first validate the state of pages in the existing job
            if (validateStateOfPagesInJob(j, dir))
            {
                // notify all the participants about reimport being delayed
                notifyParticipants(j, p_request, p_page, dir.getTime());
                notifyProjectManager(j, p_request, p_page, dir.getTime(),
                        MailerConstants.PAGE_REIMPORT_TO_PM_SUBJECT,
                        PAGE_REIMPORT_TO_PM_MESSAGE);

                startTimer(dir);
                s_logger.info("Delaying re-import for request "
                        + p_request.getId() + " and page "
                        + p_page.getExternalPageId());
            }
        }
    }

    /**
     * Import the request.
     */
    public void startImport(DelayedImportRequest p_delayedRequest)
    {
        s_logger.info("Starting re-import for delayed request "
                + p_delayedRequest.getId() + " and page "
                + p_delayedRequest.getExternalPageId());
        try
        {
            // load the attributes that may have been lost
            // since the request doesn't save them to the DB.
            RequestImpl r = p_delayedRequest.getRequest();
            r.setExternalPageId(p_delayedRequest.getExternalPageId());
            r.setDataSourceType(p_delayedRequest.getDataSourceType());
            r.setGxml(p_delayedRequest.getGxml());
            r.setSourceEncoding(p_delayedRequest.getSourceEncoding());
            Job oldJob = null;
            // find the job the previous page is associated with
            DelayedImportQuery diq = new DelayedImportQuery();
            long id = diq.findJobIdOfPage(p_delayedRequest.getPreviousPage()
                    .getId());
            oldJob = loadJobFromDbIntoCache(id);
            List jobComments = oldJob.getJobComments();
            Hashtable workflowComments = handlePreviousPage(r,
                    p_delayedRequest.getPreviousPage());
            ServerProxy.getRequestHandler().importPage(r);
            RequestImpl updatedRequest = (RequestImpl) ServerProxy
                    .getRequestHandler().findRequest(r.getId());
            Job newJob = updatedRequest.getJob();
            if (newJob != null)
            {
                // save task comments
                setComments(newJob, workflowComments);
                // save job comments
                if (jobComments != null)
                {
                    for (int y = 0; y < jobComments.size(); y++)
                    {
                        Comment tc = (Comment) jobComments.get(y);
                        String oldCommentId = (new Long(tc.getId())).toString();
                        Comment newComment = TaskHelper.saveComment(
                                (WorkObject) newJob, newJob.getJobId(),
                                tc.getCreatorId(), tc.getComment(),
                                tc.getCreatedDateAsDate());
                        String commentId = new Long(newComment.getId())
                                .toString();
                        // String path = CommentUpload.UPLOAD_BASE_DIRECTORY +
                        // CommentUpload.UPLOAD_DIRECTORY + oldCommentId;
                        // String f = CommentUpload.UPLOAD_BASE_DIRECTORY +
                        // CommentUpload.UPLOAD_DIRECTORY + commentId;
                        // File savedDir = new File(path);
                        // File finalPath = new File(f);
                        File savedDir = new File(
                                AmbFileStoragePathUtils
                                        .getCommentReferenceDir(),
                                oldCommentId);
                        File finalPath = new File(
                                AmbFileStoragePathUtils
                                        .getCommentReferenceDir(),
                                commentId);
                        savedDir.renameTo(finalPath);
                    }
                }
            }
            // remove the delayed request since it has been imported now
            HibernateUtil.delete(p_delayedRequest);
        }
        catch (PersistenceException pe)
        {
            s_logger.error("Failed to remove the delayed import request "
                    + p_delayedRequest.getId() + " from the queue.", pe);
        }
        catch (ReimporterException re)
        {
            s_logger.error("Failed to handle the previous page", re);
        }
        catch (Exception e)
        {
            s_logger.error("Failed to start import on delayed request "
                    + p_delayedRequest.getId(), e);
            notifyProjectManager(p_delayedRequest.getRequest().getJob(),
                    p_delayedRequest.getRequest(), null, new Timestamp(),
                    MailerConstants.PAGE_FAILED_TO_REIMPORT_SUBJECT,
                    PAGE_FAILED_TO_REIMPORT_MESSAGE);
        }
    }

    /**
     * Tests if the page is considered active. Returns true if it is in the
     * correct state considered to be active.
     */
    public boolean isActivePage(Page p_page)
    {
        if ((p_page == null)
                || p_page.getPageState().equals(PageState.EXPORTED)
                || p_page.getPageState().equals(PageState.EXPORT_IN_PROGRESS)
                || p_page.getPageState().equals(PageState.EXPORT_FAIL)
                || p_page.getPageState().equals(PageState.EXPORT_CANCELLED)
                || p_page.getPageState().equals(PageState.OUT_OF_DATE)
                || p_page.getPageState().equals(PageState.NOT_LOCALIZED))
        {
            return false;
        }
        else
        {
            return true;
        }
    }

    /**
     * This is called on start-up. It loads all delayed imports and starts them.
     * No timer is created.
     */
    public void startDelayedImports() throws ReimporterException
    {
        Iterator it = null;
        try
        {
            String hql = "from DelayedImportRequest";
            it = HibernateUtil.search(hql).iterator();
        }
        catch (PersistenceException e)
        {
            s_logger.error(
                    "Exception thrown when trying to get all delayed import requests "
                            + "from the database.", e);
            throw new ReimporterException(
                    ReimporterException.MSG_FAILED_TO_LOAD_ALL_DELAYED_IMPORTS,
                    null, e);
        }

        while (it.hasNext())
        {
            DelayedImportRequest dir = (DelayedImportRequest) it.next();
            // start the timer again - if it has already passed the timer
            // will expire right away and trigger the import
            startTimer(dir);
        }
    }

    /**
     * private constructor for singleton
     */
    private ActivePageReimporter()
    {
    }

    /**
     * Start the timer to delay the import.
     */
    private void startTimer(DelayedImportRequest p_request)
            throws ReimporterException
    {
        try
        {
            long objId = p_request.getId();
            Integer objType = SchedulerConstants
                    .getKeyForClass(DelayedImportRequest.class);
            Integer eventTypeKey = SchedulerConstants
                    .getKeyForType(SchedulerConstants.DELAYED_REIMPORT_TYPE);
            HashMap eventInfo = new HashMap();
            eventInfo.put("delayedImportRequestId", new Long(objId));
            Date startDate = p_request.getTime().getDate();
            s_logger.debug("Time is now  :" + new java.util.Date());
            s_logger.debug("timer expires:" + startDate);
            SchedulingInformation schedulingInformation = new SchedulingInformation();
            schedulingInformation
                    .setListener(ActivePageReimportEventHandler.class);
            schedulingInformation.setStartDate(startDate);
            String recurrance = "+3600s";
            int repeatCount = 2;
            s_logger.debug("Using recurrance '" + recurrance + "' and repeat: "
                    + repeatCount);
            schedulingInformation.setRecurranceExpression(recurrance);
            schedulingInformation.setRepeatCount(repeatCount);
            schedulingInformation.setEventInfo(eventInfo);
            schedulingInformation
                    .setEventTypeName(SchedulerConstants.DELAYED_REIMPORT_TYPE);
            schedulingInformation.setEventType(eventTypeKey.intValue());
            schedulingInformation.setObjectId(objId);
            schedulingInformation.setObjectType(objType.intValue());

            s_logger.debug("calling scheduler...");
            ServerProxy.getEventScheduler()
                    .scheduleEvent(schedulingInformation);
            s_logger.debug("scheduled...");

        }
        catch (Exception tte)
        {
            s_logger.error("Failed to delay the import of request "
                    + p_request.getRequest().getId()
                    + " A timer exception was thrown.", tte);
            String[] args =
            { Long.toString(p_request.getRequest().getId()) };
            throw new ReimporterException(
                    ReimporterException.MSG_FAILED_TO_DELAY_REIMPORT, args, tte);
        }
    }

    // Save comment ids of the job being reimported
    // These comments would be later copied to the
    // new job (delayed reimport)
    private Hashtable getComments(Job p_job)
    {
        Collection<Workflow> workflows = p_job.getWorkflows();
        Hashtable workflowComments = new Hashtable();

        int wfSize = workflows.size();
        for (Workflow curWF : workflows)
        {
            // if the workflow is canceled, then don't display
            // information about it
            if (curWF.getState().equals(Workflow.CANCELLED))
            {
                continue;
            }

            // get the target locale
            GlobalSightLocale target = curWF.getTargetLocale();

            Hashtable allTasks = curWF.getTasks();
            TreeMap sortedTasks = new TreeMap(allTasks);
            Iterator sortedTaskIterator = sortedTasks.values().iterator();
            Vector allComments = new Vector();
            List comments = null;
            Task t = null;

            while (sortedTaskIterator.hasNext())
            {
                t = (Task) sortedTaskIterator.next();
                if (t != null)
                {
                    comments = t.getTaskComments();
                }
                allComments.addElement(comments);
            }
            workflowComments.put(target, allComments);
        }
        return workflowComments;
    }

    private void setComments(Job p_job, Hashtable p_workflowComments)
            throws ServletException, IOException, EnvoyServletException
    {
        if (p_workflowComments == null)
        {
            return;
        }
        // Get all the workflows for the job
        Collection<Workflow> workflows = p_job.getWorkflows();

        int wfSize = workflows.size();
        for (Workflow curWF : workflows)
        {
            // get the target locale
            GlobalSightLocale target = curWF.getTargetLocale();

            // Get all the tasks in the workflow.
            Hashtable allTasks = curWF.getTasks();
            TreeMap sortedTasks = new TreeMap(allTasks);
            Iterator sortedTaskIterator = sortedTasks.values().iterator();
            Vector allComments = new Vector();
            List comments = null;
            Task t = null;
            // attach all the comments to first task only.
            while (sortedTaskIterator.hasNext())
            {
                t = (Task) sortedTaskIterator.next();
                if (t != null)
                {
                    for (Enumeration en = p_workflowComments.keys(); en
                            .hasMoreElements();)
                    {
                        GlobalSightLocale tl = (GlobalSightLocale) en
                                .nextElement();
                        if (target.equals(tl))
                        {
                            allComments = (Vector) p_workflowComments
                                    .get(target);
                            if (allComments != null)
                            {
                                for (int k = 0; k < allComments.size(); k++)
                                {
                                    comments = (List) allComments.elementAt(k);
                                    if (comments != null)
                                    {
                                        for (int y = 0; y < comments.size(); y++)
                                        {
                                            Comment tc = (Comment) comments
                                                    .get(y);
                                            String oldCommentId = (new Long(
                                                    tc.getId())).toString();
                                            Comment comment = TaskHelper
                                                    .saveComment(
                                                            (WorkObject) t,
                                                            t.getId(),
                                                            tc.getCreatorId(),
                                                            tc.getComment(),
                                                            tc.getCreatedDateAsDate());
                                            String commentId = (new Long(
                                                    comment.getId()))
                                                    .toString();
                                            // String path =
                                            // CommentUpload.UPLOAD_BASE_DIRECTORY
                                            // + CommentUpload.UPLOAD_DIRECTORY
                                            // + oldCommentId;
                                            // String f =
                                            // CommentUpload.UPLOAD_BASE_DIRECTORY
                                            // + CommentUpload.UPLOAD_DIRECTORY
                                            // + commentId;
                                            // File savedDir = new File(path);
                                            // File finalPath = new File(f);
                                            File savedDir = new File(
                                                    AmbFileStoragePathUtils
                                                            .getCommentReferenceDir(),
                                                    oldCommentId);
                                            File finalPath = new File(
                                                    AmbFileStoragePathUtils
                                                            .getCommentReferenceDir(),
                                                    commentId);
                                            savedDir.renameTo(finalPath);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                break;
            }
        }
    }

    // The request is performing a re-import on
    // a page that is part of an acive job - or was
    // when the
    private Hashtable handlePreviousPage(RequestImpl p_request,
            SourcePage p_previousPage) throws ReimporterException
    {
        Hashtable workflowComments = null;
        // check again if the page is active
        // by now it may have been finished and exported
        // only proceed if part of an active job,
        // otherwise don't need to handle the page
        if (isActivePage(p_previousPage))
        {
            // Sychronize a block of code since more than one page may come
            // through at the same time and attempt to cancel the same job.
            // This allows the cancel to happen one at a time.
            // This will slow things down a bit - however this is minimal
            // since it is just the cancel and not the entire import process.
            synchronized (Boolean.TRUE)
            {
                Job j = null;

                // find the job the previous page is associated with
                try
                {
                    DelayedImportQuery diq = new DelayedImportQuery();
                    long id = diq.findJobIdOfPage(p_previousPage.getId());
                    j = loadJobFromDbIntoCache(id);
                }
                catch (Exception e)
                {
                    // will probably still be null - but just in case
                    j = null;
                }

                // this catches if an exception was thrown
                // or if the method call returns a NULL job.
                // just log the error - no exception thrown
                if (j == null)
                {
                    s_logger.error("Failed to find the job associated with active page "
                            + p_previousPage.getId()
                            + ": "
                            + p_previousPage.getExternalPageId()
                            + " while starting to re-import the page");
                }
                else
                {
                    // if an office page is imported - it may get split into
                    // multiple
                    // source pages and all will need to go through this process
                    // if
                    // being re-imported when the previous page is part of an
                    // active job.
                    // only one of them will need to cancel the job.
                    if (!j.getState().equals(Job.CANCELLED))
                    {
                        workflowComments = getComments(j);

                        try
                        {
                            boolean fromReimport = true;
                            ServerProxy.getJobHandler().cancelJob(j,
                                    fromReimport);
                        }
                        catch (Exception e)
                        {
                            s_logger.error("Failed to cancel the job "
                                    + j.getId() + ": " + j.getJobName()
                                    + " associated with page "
                                    + p_previousPage.getId() + ": "
                                    + p_previousPage.getExternalPageId()
                                    + " when starting to reimport the page.");
                            String args[] =
                            { Long.toString(j.getId()), j.getJobName(),
                                    Long.toString(p_previousPage.getId()),
                                    p_previousPage.getExternalPageId() };
                            throw new ReimporterException(
                                    ReimporterException.MSG_FAILED_TO_CANCEL_JOB_OF_PREVIOUS_PAGE,
                                    args, e);
                        }
                    }
                } // end of else
            } // enc of synchronized
        }
        return workflowComments;
    }

    /**
     * Some information in the request is not saved to the DB during a normal
     * import. This information needs to be stored for a delayed import.
     * 
     * If this fails it still continues on. Only if the server is restarted
     * should this cause any problems.
     */
    private DelayedImportRequest createDelayedRequest(RequestImpl p_request,
            SourcePage p_page)
    {
        DelayedImportRequest dir = new DelayedImportRequest(p_request, p_page);

        try
        {
            HibernateUtil.saveOrUpdate(dir);
        }
        catch (Exception e)
        {
            s_logger.error(
                    "An exception was thrown when saving the delayed import for request "
                            + p_request.getId(), e);
        }

        return dir;
    }

    /**
     * Send email to all participants to notify them of the re-import.
     * 
     * If the email fails an error is logged but no exception is thrown. The
     * process will continue.
     */
    private void notifyParticipants(Job p_job, RequestImpl p_request,
            SourcePage p_page, Timestamp p_time)
    {
        // notify all the participants about the reimport being delayed
        try
        {
            WorkflowServer ws = ServerProxy.getWorkflowServer();

            String pmId = p_request.getL10nProfile().getProject()
                    .getProjectManagerId();

            Iterator wfs = p_job.getWorkflows().iterator();
            while (wfs.hasNext())
            {
                Workflow w = (Workflow) wfs.next();

                WorkflowTemplateInfo wfti = p_request.getL10nProfile()
                        .getWorkflowTemplateInfo(w.getTargetLocale());

                TaskEmailInfo emailInfo = new TaskEmailInfo(
                        pmId,
                        w.getWorkflowOwnerIdsByType(Permission.GROUP_WORKFLOW_MANAGER),
                        wfti.notifyProjectManager(), p_job.getPriority());
                emailInfo.setPageName(p_page.getExternalPageId());
                emailInfo.setTime(p_time);
                emailInfo.setJobName(p_job.getJobName());

                ws.notifyTaskParticipants(w.getId(),
                        WorkflowMailerConstants.PAGE_REIMPORTED, emailInfo);
            }
        }
        catch (Exception e)
        {
            s_logger.error("Failed to notify participants of re-imported page "
                    + p_page.getId(), e);
            // just log error and continue
        }
    }

    /**
     * Send email to the project manager to notify them of the re-import.
     * 
     * If the email fails an error is logged but no exception is thrown. The
     * process will continue.
     * 
     * Assumes the message arguments are the same for any of the messages
     * specified.
     */
    private void notifyProjectManager(Job p_job, RequestImpl p_request,
            SourcePage p_page, Timestamp p_time, String p_emailSubjectKey,
            String p_emailMessageKey)
    {
        if (!m_systemNotificationEnabled)
        {
            return;
        }

        try
        {
            SystemConfiguration config = SystemConfiguration.getInstance();
            String capLoginUrl = config
                    .getStringParameter(SystemConfiguration.CAP_LOGIN_URL);
            String companyIdStr = String.valueOf(p_job.getCompanyId());
            // there is an order to these arguments
            // activity name, job name, priority, page name, time, comments,
            // url)
            String[] args =
            { null, p_job.getJobName(), Integer.toString(p_job.getPriority()),
                    p_page == null ? "" : p_page.getExternalPageId(),
                    p_time.toString(), null, capLoginUrl };

            L10nProfile l10nProfile = p_request.getL10nProfile();
            Object wfs[] = p_job.getWorkflows().toArray();
            UserManager um = ServerProxy.getUserManager();
            boolean shouldNotifyPm = false;
            for (int i = 0; i < wfs.length; i++)
            {
                Workflow wf = (Workflow) wfs[i];
                WorkflowTemplateInfo wfti = l10nProfile
                        .getWorkflowTemplateInfo(wf.getTargetLocale());
                if (!shouldNotifyPm && wfti.notifyProjectManager())
                {
                    shouldNotifyPm = true;
                }

                List wfManagerIds = wf
                        .getWorkflowOwnerIdsByType(Permission.GROUP_WORKFLOW_MANAGER);
                int size = wfManagerIds.size();

                for (int h = 0; h < size; h++)
                {
                    ServerProxy.getMailer().sendMailFromAdmin(
                            um.getUser((String) wfManagerIds.get(h)), args,
                            p_emailSubjectKey, p_emailMessageKey, companyIdStr);
                }

            }
            // if at least one of the wfInfos had the pm notify flag on, notify
            // PM.
            if (shouldNotifyPm)
            {

                User pm = l10nProfile.getProject().getProjectManager();
                if (pm == null)
                {
                    pm = um.getUser(l10nProfile.getProject()
                            .getProjectManagerId());
                }
                ServerProxy.getMailer().sendMailFromAdmin(pm, args,
                        p_emailSubjectKey, p_emailMessageKey, companyIdStr);
            }
        }
        catch (Exception e)
        {
            s_logger.error(
                    "Failed to notify the project manager of re-imported request "
                            + p_request.getId(), e);
        }
    }

    private Job loadJobFromDbIntoCache(long p_jobId)
            throws PersistenceException
    {
        Job job = null;
        try
        {
            job = ServerProxy.getJobHandler().getJobById(p_jobId);
        }
        catch (Exception e)
        {
            throw new PersistenceException(e);
        }
        return job;
    }

    /**
     * Make sure no page is in UPDATING state for the given job.
     */
    private boolean validateStateOfPagesInJob(Job p_existingJob,
            DelayedImportRequest p_delayedRequest)
    {
        boolean isValid = true;
        try
        {
            PageStateValidator.validateStateOfPagesInJob(p_existingJob);
        }
        catch (PageException e)
        {
            isValid = false;
            importFailed(p_delayedRequest, e);
        }

        return isValid;
    }

    /**
     * Proceed with the failed import so the result can also be displayed on the
     * list of pending jobs. Note that the reason for performing this process in
     * three different transactions is because import is a direct JDBC process
     * and delayed request is handled thru TOPLink.
     */
    private void importFailed(DelayedImportRequest p_delayedRequest,
            PageException p_pageException)
    {
        RequestImpl req = p_delayedRequest.getRequest();
        try
        {
            // set the exception in the request
            ServerProxy.getRequestHandler().setExceptionInRequest(req,
                    p_pageException);
        }
        catch (Exception ex)
        {
            s_logger.error(
                    "Failed to set the exception in the request with id "
                            + req.getId(), ex);
        }

        try
        {
            // remove the delayed request due to the page validation error
            HibernateUtil.delete(p_delayedRequest);
        }
        catch (Exception ex)
        {
            s_logger.error(
                    "Failed to remove the delayed import request with id "
                            + p_delayedRequest.getId() + " from the queue.", ex);
        }

        try
        {
            // continue importing the failed request so the user can
            // see it in the list of pending jobs.
            ServerProxy.getRequestHandler().importPage(req);
        }
        catch (Exception ex)
        {
            s_logger.error(
                    "Failed to proceed importing the failed request with id "
                            + req.getId(), ex);
        }
    }
}
