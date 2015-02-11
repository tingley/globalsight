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

package com.globalsight.everest.page.pageexport;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.foundation.L10nProfile;
import com.globalsight.everest.foundation.Timestamp;
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.page.PagePersistenceAccessor;
import com.globalsight.everest.page.PageState;
import com.globalsight.everest.page.SourcePage;
import com.globalsight.everest.page.TargetPage;
import com.globalsight.everest.permission.Permission;
import com.globalsight.everest.persistence.PersistenceException;
import com.globalsight.everest.projecthandler.WorkflowTemplateInfo;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.usermgr.UserManager;
import com.globalsight.everest.util.system.SystemConfiguration;
import com.globalsight.everest.workflow.EventNotificationHelper;
import com.globalsight.everest.workflow.TaskEmailInfo;
import com.globalsight.everest.workflow.WorkflowMailerConstants;
import com.globalsight.everest.workflow.WorkflowServer;
import com.globalsight.everest.workflowmanager.Workflow;
import com.globalsight.scheduling.SchedulerConstants;
import com.globalsight.scheduling.SchedulingInformation;
import com.globalsight.util.mail.MailerConstants;

/**
 * Provides the capabilities for delaying the export of source pages for update,
 * notifying particpants, updating the job state, etc.
 */
public class DelayedExporter
{
    // for logging purposes
    private static Logger s_logger = Logger.getLogger(DelayedExporter.class
            .getName());

    // singleton
    private static DelayedExporter c_instance = null;

    // email message keys
    private static String PAGE_EFU_TO_PM_MESSAGE = "pageExportForUpdateToPmMessage";
    private static String PAGE_FAILED_TO_EFU_MESSAGE = "pageFailedExportForUpdateToPmMessage";

    // keeps state for each job with the number of source pages in the job
    // this count will get decremented for every successful page export
    // when the count is 0, then the item will be removed from the hashtable
    // and the export for update will be completed
    // successive attempts to export the same job for update are ok, since the
    // entry in the hashtable will be overwritten since it is based on jobid
    private Hashtable m_pagesPerJob = new Hashtable();

    // used to synchronize on when a get and put need to be
    // done together in a transaction on the "m_pagesPerJob"
    private static Boolean m_pagesPerJobTxn = Boolean.TRUE;

    // determines whether the system-wide notification is enabled
    private boolean m_systemNotificationEnabled = EventNotificationHelper
            .systemNotificationEnabled();

    /**
     * Returns the singleton instance
     */
    static public DelayedExporter getInstance()
    {
        if (c_instance == null)
        {
            // two or more threads may be here
            synchronized (DelayedExporter.class)
            {
                if (c_instance == null)
                {
                    c_instance = new DelayedExporter();
                }
            }
        }

        return c_instance;
    }

    /**
     * Delay the export of this request for an export of a source page(s) and
     * notify particpants about the job cancellation.
     */
    public void delayExport(DelayedExportRequest p_request, Job p_job)
            throws PageExportException
    {
        s_logger.info("Delaying export for update for Job "
                + p_job.getJobName() + "(id=" + p_job.getId() + ") until "
                + p_request.getTime().toString());
        startTimer(p_request);

        // notify all the participants about the export for update
        notifyParticipants(p_job, p_request, p_request.getTime());

        SourcePage sp = null;
        Object sourcePages[] = p_job.getSourcePages().toArray();

        if (sourcePages.length == 1)
            sp = (SourcePage) sourcePages[0];
        notifyProjectManager(p_job, p_request, p_request.getTime(),
                MailerConstants.PAGE_EFU_TO_PM_SUBJECT, PAGE_EFU_TO_PM_MESSAGE,
                sp);
    }

    /**
     * Exports the source pages and cancels the job
     */
    public void startExport(DelayedExportRequest p_delayedRequest)
            throws Exception
    {
        Job job = ServerProxy.getJobHandler().getJobById(
                p_delayedRequest.getJobId());

        // Export the source pages. If there is an exception, the job
        // won't be canceled.
        s_logger.info("Exporting source pages for job " + job.getJobName());

        CompanyThreadLocal.getInstance().setIdValue(
                String.valueOf(job.getCompanyId()));
        exportPages(p_delayedRequest, job);
    }

    /**
     * This is called on start-up. It loads all delayed export requests and
     * starts them. No timer is created.
     */
    public void startDelayedExports() throws PageExportException
    {
        Iterator it = null;

        while (it != null && it.hasNext())
        {
            DelayedExportRequest der = (DelayedExportRequest) it.next();
            // start the timer again - if it has already passed the timer
            // will expire right away and trigger the import
            startTimer(der);
        }
    }

    /**
     * private constructor for singleton
     */
    private DelayedExporter()
    {
    }

    /**
     * Start the timer to delay the import.
     */
    private void startTimer(DelayedExportRequest p_delayedRequest)
            throws PageExportException
    {
        try
        {
            long objId = p_delayedRequest.getJobId();
            Integer objType = SchedulerConstants.getKeyForClass(Job.class);
            Integer eventTypeKey = SchedulerConstants
                    .getKeyForType(SchedulerConstants.EXPORT_SOURCE_TYPE);
            HashMap eventInfo = new HashMap();
            eventInfo.put("delayedExportRequest", p_delayedRequest);
            Date startDate = p_delayedRequest.getTime().getDate();
            s_logger.debug("Time is now  :" + new java.util.Date());
            s_logger.debug("timer expires:" + startDate);
            SchedulingInformation schedulingInformation = new SchedulingInformation();
            schedulingInformation.setListener(DelayedExportEventHandler.class);
            schedulingInformation.setStartDate(startDate);
            String recurrance = "+3600s";
            int repeatCount = 2;
            s_logger.debug("Using recurrance '" + recurrance + "' and repeat: "
                    + repeatCount);
            schedulingInformation.setRecurranceExpression(recurrance);
            schedulingInformation.setRepeatCount(repeatCount);
            schedulingInformation.setEventInfo(eventInfo);
            schedulingInformation
                    .setEventTypeName(SchedulerConstants.EXPORT_SOURCE_TYPE);
            schedulingInformation.setEventType(eventTypeKey.intValue());
            schedulingInformation.setObjectId(objId);
            schedulingInformation.setObjectType(objType.intValue());

            s_logger.debug("calling scheduler...");
            ServerProxy.getEventScheduler()
                    .scheduleEvent(schedulingInformation);
            s_logger.debug("scheduled...");
        }
        catch (Exception e)
        {
            s_logger.error("Failed to delay the export of request.", e);
        }
    }

    /**
     * Send email to all job participants to notify them of the export for
     * update.
     * 
     * If the email fails an error is logged but no exception is thrown. The
     * process will continue.
     */
    private void notifyParticipants(Job p_job, DelayedExportRequest p_request,
            Timestamp p_time)
    {
        // notify all the participants about the export for update

        try
        {
            WorkflowServer ws = ServerProxy.getWorkflowServer();

            Iterator wfs = p_job.getWorkflows().iterator();

            while (wfs.hasNext())
            {
                String pmId = p_job.getL10nProfile().getProject()
                        .getProjectManagerId();
                SourcePage sp = (SourcePage) p_job.getSourcePages().toArray()[0];

                Workflow w = (Workflow) wfs.next();
                WorkflowTemplateInfo wfti = p_job.getL10nProfile()
                        .getWorkflowTemplateInfo(w.getTargetLocale());

                TaskEmailInfo emailInfo = new TaskEmailInfo(
                        pmId,
                        w.getWorkflowOwnerIdsByType(Permission.GROUP_WORKFLOW_MANAGER),
                        wfti.notifyProjectManager(), p_job.getPriority());

                emailInfo.setTime(p_time);
                emailInfo.setJobName(p_job.getJobName());
                emailInfo.setPageName(sp.getExternalPageId());

                // new fields
                emailInfo.setProjectIdAsLong(new Long(p_job.getL10nProfile()
                        .getProjectId()));
                emailInfo.setWfIdAsLong(w.getIdAsLong());
                emailInfo.setSourceLocale(p_job.getSourceLocale().toString());
                emailInfo.setTargetLocale(w.getTargetLocale().toString());

                ws.notifyTaskParticipants(w.getId(),
                        WorkflowMailerConstants.PAGE_EXPORTED_FOR_UPDATE,
                        emailInfo);
            }
        }
        catch (Exception e)
        {
            s_logger.error("Failed to notify participants of the "
                    + "export for update", e);
        }
    }

    /**
     * Send email to the project manager to notify them of the export for update
     * 
     * If the email fails an error is logged but no exception is thrown. The
     * process will continue.
     * 
     * Assumes the message arguments are the same for any of the messages
     * specified.
     */
    private void notifyProjectManager(Job p_job,
            DelayedExportRequest p_request, Timestamp p_time,
            String p_emailSubjectKey, String p_emailMessageKey,
            SourcePage p_sourcePage)
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
            String[] args = new String[7];
            args[0] = null;
            args[1] = p_job.getJobName();
            args[2] = Integer.toString(p_job.getPriority());

            if (p_sourcePage == null)
                args[3] = "--";
            else
                args[3] = p_sourcePage.getExternalPageId();

            args[4] = p_time.toString();
            args[5] = null;
            args[6] = capLoginUrl;

            L10nProfile l10nProfile = p_job.getL10nProfile();

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

                // notify the workflow managers (if any)
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
            s_logger.error("Failed to notify the project manager of the "
                    + "export for update", e);
        }
    }

    /**
     * Actually exports the pages
     * 
     * @param p_request
     *            the delayed export request
     * @param p_job
     *            the job
     */
    private void exportPages(DelayedExportRequest p_delayedRequest, Job p_job)
    {
        Job job = null;
        SourcePage sp = null;
        try
        {
            User exportingUser = ServerProxy.getUserManager().getUser(
                    p_delayedRequest.getExportingUserId());

            ExportParameters ep = p_delayedRequest.getExportParameters();
            List pageIds = p_delayedRequest.getPageIds();
            boolean isTargetPage = p_delayedRequest.getIsTargetPage();
            long jobid = p_delayedRequest.getJobId();

            job = ServerProxy.getJobHandler().getJobById(jobid);

            Collection sourcePages = job.getSourcePages();
            sp = (SourcePage) sourcePages.toArray()[0];
            // if the pages are still marked as ACTIVE - change to LOCALIZED
            // so they will be placed in PAGE_TM and picked up on re-import
            if (sp.getPageState().equals(PageState.ACTIVE_JOB))
            {
                PagePersistenceAccessor.updateStateOfPages(
                        p_job.getSourcePages(), PageState.LOCALIZED);
            }

            Integer count = new Integer(sourcePages.size());
            Long jobId = new Long(p_job.getJobId());
            m_pagesPerJob.put(jobId, count);

            // register for export notification
            long exportBatchId;
            String exportType = null;
            Workflow wkf = null;
            if (isTargetPage)
            {
                ArrayList wkfIds = new ArrayList();
                for (int i = 0; i < pageIds.size(); i++)
                {
                    wkf = ((TargetPage) pageIds.get(i)).getWorkflowInstance();
                    wkfIds.add(wkf.getIdAsLong());
                }
                // use the last workflow to determine the export type
                // TODO: don't know how to detect secondary export types ???
                exportType = wkf.getState().equals(Workflow.LOCALIZED) ? ExportBatchEvent.FINAL_PRIMARY
                        : ExportBatchEvent.INTERIM_PRIMARY;
                // register
                exportBatchId = ExportEventObserverHelper
                        .notifyBeginExportTargetBatch(job, exportingUser,
                                pageIds, wkfIds, null, exportType);
            }
            else
            {
                exportBatchId = ExportEventObserverHelper
                        .notifyBeginExportSourceBatch(job, exportingUser,
                                pageIds, null);
            }

            ServerProxy.getPageManager().exportPage(ep, pageIds, isTargetPage,
                    exportBatchId);

            // remove the delayed request since it has been imported now
            // PersistenceService.getInstance().deleteObject(p_delayedRequest);
        }
        catch (PersistenceException pe)
        {
            s_logger.error("Failed to remove the delayed export request "
                    + p_delayedRequest.getId() + " from the queue.", pe);
        }
        catch (Exception e)
        {
            s_logger.error("Failed to start export on delayed request "
                    + p_delayedRequest.getId(), e);

            notifyProjectManager(job, p_delayedRequest, new Timestamp(),
                    MailerConstants.PAGE_FAILED_TO_EFU_SUBJECT,
                    PAGE_FAILED_TO_EFU_MESSAGE, sp);
        }
    }

    /**
     * Completes the export for update by updating the TUVs and canceling the
     * Job.
     * 
     * Updates all TUVs that are LOCALIZED to COMPLETED so that when the page is
     * reimported, whatever has been translated so far can be leveraged
     * 
     * @param p_job
     *            job
     */
    public void completeExportForUpdate(Job p_job) throws Exception
    {

        // first check the count of pages that were to be exported
        Long jobId = new Long(p_job.getJobId());
        // synchronize the get and put
        synchronized (m_pagesPerJobTxn)
        {
            Integer count = (Integer) m_pagesPerJob.get(jobId);
            Integer newCount = new Integer(count.intValue() - 1);
            if (newCount.intValue() > 0)
            {
                s_logger.info("Waiting for " + newCount.intValue()
                        + " source pages in job " + p_job.getJobName()
                        + " to export successfully before canceling job.");
                m_pagesPerJob.put(jobId, newCount);
                return;
            }
        }

        m_pagesPerJob.remove(jobId);

        // now cancel the job regardless of state
        // the job and pages should no longer appear on the UI to the user's
        // allowing them to export source again
        s_logger.info("Canceling job " + p_job.getJobName()
                + " for export for update.");

        // need to pass 'true' as an indication to re-import (since for export
        // source no resetting of tuv state should take place).
        ServerProxy.getJobHandler().cancelJob(p_job, true);
    }

    /**
     * Notifies the PM that the export for update has failed.
     * 
     * @param p_job
     *            the job
     */
    public void handleFailedExportForUpdate(Job p_job, SourcePage p_sourcePage)
    {
        notifyProjectManager(p_job, null, new Timestamp(),
                MailerConstants.PAGE_FAILED_TO_EFU_SUBJECT,
                PAGE_FAILED_TO_EFU_MESSAGE, p_sourcePage);
    }
}
