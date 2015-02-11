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

// globalsight
import java.io.File;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.globalsight.everest.corpus.CorpusDoc;
import com.globalsight.everest.corpus.CorpusTm;
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.page.GenericPage;
import com.globalsight.everest.page.Page;
import com.globalsight.everest.page.PageManager;
import com.globalsight.everest.page.SourcePage;
import com.globalsight.everest.page.TargetPage;
import com.globalsight.everest.permission.Permission;
import com.globalsight.everest.request.Request;
import com.globalsight.everest.secondarytargetfile.SecondaryTargetFile;
import com.globalsight.everest.secondarytargetfile.SecondaryTargetFileMgr;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.taskmanager.Task;
import com.globalsight.everest.taskmanager.TaskManager;
import com.globalsight.everest.usermgr.UserManager;
import com.globalsight.everest.util.system.SystemConfigParamNames;
import com.globalsight.everest.util.system.SystemConfiguration;
import com.globalsight.everest.workflowmanager.Workflow;
import com.globalsight.everest.workflowmanager.WorkflowExportingHelper;
import com.globalsight.everest.workflowmanager.WorkflowPersistenceAccessor;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.XmlParser;
import com.globalsight.util.date.DateHelper;
import com.globalsight.util.mail.MailerConstants;
import com.globalsight.util.modules.Modules;
import com.globalsight.util.resourcebundle.LocaleWrapper;
import com.globalsight.util.resourcebundle.ResourceBundleConstants;
import com.globalsight.util.resourcebundle.SystemResourceBundle;

/**
 * This class represents an observer of events which occur for Export. The
 * caller notifies the observer of export events that require tracking and
 * notification of completed exports.
 */
public class ExportEventObserverLocal implements ExportEventObserver
{
    static private final Logger s_logger = Logger
            .getLogger(ExportEventObserverLocal.class);

    static private SystemResourceBundle s_sysResBundle = SystemResourceBundle
            .getInstance();
    static private boolean s_removeInfo = true;
    static private boolean s_isDell = false;

    //
    // Constructor
    //

    public ExportEventObserverLocal() throws ExportEventObserverException
    {
        try
        {
            SystemConfiguration sc = SystemConfiguration.getInstance();
            s_removeInfo = sc
                    .getBooleanParameter(SystemConfigParamNames.EXPORT_REMOVE_INFO_ENABLED);
            s_isDell = sc.getBooleanParameter(SystemConfigParamNames.IS_DELL);
        }
        catch (Exception ex)
        {
            throw new ExportEventObserverException(
                    ExportEventObserverException.MSG_FAILED_TO_GET_SYS_PARAM,
                    null, ex);
        }

        // remove the old events (older than a month)
        // sometimes GS will generate some dirty data. Removing these data will
        // result in exception.!!!
        // so disable this.
        // removeOldEvents();
    }

    //
    // Public Methods
    //

    /**
     * See interface
     */
    public long notifyBeginExportSourcePage(Job p_job, User p_user,
            long p_pageId, Long p_taskId) throws RemoteException,
            ExportEventObserverException
    {
        ArrayList pageIds = new ArrayList();
        pageIds.add(Long.toString(p_pageId));

        return notifyBeginExportSourceBatch(p_job, p_user, pageIds, p_taskId);
    }

    /**
     * See ExportEventObserver interface
     */
    public long notifyBeginExportSourceBatch(Job p_job, User p_user,
            List p_pageIds, Long p_taskId) throws RemoteException,
            ExportEventObserverException
    {
        if (p_job == null || p_user == null || p_pageIds == null
                || p_pageIds.size() == 0)
        {
            String s = "notifyBeginExportSourceBatch: JobId=" + p_job.getId()
                    + "UserName=" + p_user.getUserName() + ", pageIds="
                    + p_pageIds + ", taskId=" + p_taskId;
            String[] args =
            { s };

            throw new ExportEventObserverException(
                    ExportEventObserverException.MSG_INVALID_PARAMS, args, null);
        }

        return addEvent(p_user, p_pageIds, p_job, null, p_taskId,
                ExportBatchEvent.EXPORT_SOURCE);
    }

    /**
     * See ExportEventObserver interface
     */
    public long notifyBeginExportTargetPage(Job p_job, User p_user,
            long p_pageId, Long p_wfId, Long p_taskId, String p_exportType)
            throws RemoteException, ExportEventObserverException
    {
        ArrayList pageIds = new ArrayList();
        pageIds.add(new Long(p_pageId));

        ArrayList wfIds = new ArrayList();
        wfIds.add(p_wfId);

        return notifyBeginExportTargetBatch(p_job, p_user, pageIds, wfIds,
                p_taskId, p_exportType);
    }

    /**
     * See ExportEventObserver interface
     */
    public long notifyBeginExportTargetBatch(Job p_job, User p_user,
            List p_pageIds, List p_wfIds, Long p_taskId, String p_exportType)
            throws RemoteException, ExportEventObserverException
    {
        if (p_job == null || p_user == null || p_pageIds == null
                || p_pageIds.size() <= 0 || p_wfIds == null
                || p_wfIds.size() <= 0
                || p_exportType.equals(ExportBatchEvent.EXPORT_SOURCE))
        {
            String s = "notifyBeginExportTarget: JobId=" + p_job.getId()
                    + "UserName=" + p_user.getUserName() + ", pageIds="
                    + p_pageIds + ", wfIds=" + p_wfIds + ", taskId=" + p_taskId
                    + ", exportType=" + p_exportType;
            String[] args =
            { s };

            throw new ExportEventObserverException(
                    ExportEventObserverException.MSG_INVALID_PARAMS, args, null);
        }

        return addEvent(p_user, p_pageIds, p_job, p_wfIds, p_taskId,
                p_exportType);
    }

    /**
     * See ExportEventObserver interface
     */
    synchronized public void notifyPageExportComplete(long p_exportBatchId,
            String p_pageId, HttpServletRequest p_request)
            throws RemoteException, ExportEventObserverException
    {
        if (p_exportBatchId <= 0 || p_pageId == null || p_request == null)
        {
            String s = "notifyPageExportComplete: p_exportId="
                    + p_exportBatchId + ", p_pageId=" + p_pageId
                    + ", p_request=" + p_request;
            String[] args =
            { s };

            throw new ExportEventObserverException(
                    ExportEventObserverException.MSG_INVALID_PARAMS, args, null);
        }

        // locate and update the ExportingPage
        boolean found = updateExportingPage(p_exportBatchId, p_pageId,
                p_request);

        if (!found)
        {
            return;
        }

        // If the exporting page was found we know the batchId is good.
        ExportBatchEvent event = getExportBatchEventById(p_exportBatchId, false);
        boolean batchComplete = event.isCompleted();

        if (ExportBatchEvent.FINAL_PRIMARY.equals(event.getExportType()))
        {
            if (Modules.isCorpusInstalled())
            {
                s_logger.debug("Calling updateCorpusWithTargetPage()");

                updateCorpusWithTargetPage(p_pageId, p_request, batchComplete);
            }
        }

        if (!batchComplete)
        {
            return;
        }
        
        List<ExportingPage> ps = event.getExportingPages();
        for (ExportingPage p : ps)
        {
    		GenericPage tp = p.getPage();
    		if (tp instanceof TargetPage) {
				TargetPage tpage = (TargetPage) tp;
				WorkflowExportingHelper.setPageAsNotExporting(tpage.getId());
			}
    		else if(tp instanceof SecondaryTargetFile)
    		{
    			SecondaryTargetFile stf = (SecondaryTargetFile)tp;
    			WorkflowExportingHelper.setStfAsNotExporting(stf.getId());
    		}
    		
        }
        
        boolean shouldRemove = s_removeInfo
                || event.getExportType().equals(ExportBatchEvent.CREATE_STF);

        // No need to update ExportBatchEvent if it'll be removed.
        if (!shouldRemove)
        {
            updateExportBatchEvent(p_exportBatchId, System.currentTimeMillis());
        }

        // If stf creation - set final state of creation process.
        if (event.getExportType().equals(ExportBatchEvent.CREATE_STF))
        {
            try
            {
                TaskManager mgr = ServerProxy.getTaskManager();
                mgr.updateStfCreationState(event.getTaskId().longValue(),
                        event.isExportSuccess() ? Task.COMPLETED : Task.FAILED);

                s_logger.debug("STF export (" + p_exportBatchId
                        + ") has completed.");
            }
            catch (Exception ex)
            {
                throw new ExportEventObserverException(
                        ExportEventObserverException.MSG_FAILED_TO_FINISH_STF_CREATION,
                        null, ex);
            }
        }
        else
        // otherwise generate e-mail
        {
            // NOTE: "CREATE_STF" is an internal System Action that creates
            // SecondaryTargetFiles. We need to track this type of export to
            // know when it is done but we do not want to send an e-mail
            // upon completion. Users receive notice by way of the UI.
            reportBatchResults(event);
        }

        // Check if ok to remove this export history from the DB
        if (shouldRemove)
        {
            deleteExportBatchEvent(p_exportBatchId);
        }
    }

    /**
     * See ExportEventObserver interface
     */
    public void cancelExportBatchEvent(long p_exportBatchId)
            throws RemoteException, ExportEventObserverException
    {
        deleteExportBatchEvent(p_exportBatchId);

        s_logger.debug("removeExportTrackingRequest: exportBatch="
                + p_exportBatchId + " was successfully removed.");
    }

    /**
     * @see ExportEventObserver.getExportBatchEventById(long, boolean)
     */
    public ExportBatchEvent getExportBatchEventById(long p_eventId,
            boolean p_editable) throws RemoteException,
            ExportEventObserverException
    {
        try
        {
            return (ExportBatchEvent) HibernateUtil.get(ExportBatchEvent.class,
                    new Long(p_eventId));
        }
        catch (Exception ex)
        {
            String args[] =
            { String.valueOf(p_eventId) };

            throw new ExportEventObserverException(
                    ExportEventObserverException.MSG_FAILED_TO_GET_EBE, args,
                    ex);
        }
    }

    /**
     * @see ExportEventObserver.removeExportBatchEvents(long)
     */
    public void removeExportBatchEvents(long p_jobId) throws RemoteException,
            ExportEventObserverException
    {
        try
        {
            String hql = "from ExportBatchEvent e where e.job.id = :jId";
            Map map = new HashMap();
            map.put("jId", new Long(p_jobId));
            List events = HibernateUtil.search(hql, map);
            HibernateUtil.delete(events);
        }
        catch (Exception ex)
        {
            String[] args =
            { String.valueOf(p_jobId) };

            throw new ExportEventObserverException(
                    ExportEventObserverException.MSG_FAILED_TO_REMOVE_BY_JOB_ID,
                    args, ex);
        }
    }

    /**
     * Returns the requested ExportingPage (by id).
     */
    private ExportingPage getExportingPageById(long p_batchId, long p_pageId,
            boolean p_editable) throws Exception
    {
        Vector arg = new Vector();
        arg.add(new Long(p_batchId));
        arg.add(new Long(p_pageId));

        // ExportBatchEvent.class;
        String hql = "from ExportingPage p where p.exportBatchEvent.id = :bId and p.page.id = :pId";
        Map map = new HashMap();
        map.put("bId", new Long(p_batchId));
        map.put("pId", new Long(p_pageId));

        List pages = HibernateUtil.search(hql, map);
        if (pages.size() > 0)
        {
            return (ExportingPage) pages.get(0);
        }
        else
        {
            return null;
        }
    }

    /**
     * Creates and persists an ExportBatchEvent based on these params.
     */
    private long addEvent(User p_userId, List p_pageIds, Job p_job,
            List p_wfIds, Long p_taskId, String p_exportType)
            throws ExportEventObserverException
    {
        ExportBatchEvent event = new ExportBatchEvent();
        event.setExportType(p_exportType);
        event.setJob(p_job);
        event.setResponsibleUserId(p_userId.getUserId());
        event.setStartTime(System.currentTimeMillis());
        event.setWorkflowIds((p_wfIds == null || p_wfIds.size() == 0) ? null
                : p_wfIds);
        event.setTaskId(p_taskId);

        for (int i = 0; i < p_pageIds.size(); i++)
        {
            ExportingPage page = new ExportingPage();
            page.setExportBatchEvent(event);

            long pageId = ((Long) p_pageIds.get(i)).longValue();

            try
            {
                if (p_exportType.equals(ExportBatchEvent.EXPORT_SOURCE))
                {
                    PageManager mgr = ServerProxy.getPageManager();
                    page.setPage(mgr.getSourcePage(pageId));
                }
                else if (p_exportType
                        .equals(ExportBatchEvent.INTERIM_SECONDARY)
                        || p_exportType
                                .equals(ExportBatchEvent.FINAL_SECONDARY))
                {
                    SecondaryTargetFileMgr mgr = ServerProxy
                            .getSecondaryTargetFileManager();
                    page.setPage(mgr.getSecondaryTargetFile(pageId));
                }
                else
                {
                    PageManager mgr = ServerProxy.getPageManager();
                    page.setPage(mgr.getTargetPage(pageId));
                }
            }
            catch (Exception ex)
            {
                String msg = "addEvent(): pageId=" + pageId + ", export type="
                        + p_exportType + ", BatchExportId=" + event.getId();

                // only throw if we are creating STFs. For the other export
                // types,
                // we only use the page to get information for the e-mail. If we
                // do not have the page we will leave it out of the e-mail but
                // not
                // throw an exception.
                if (p_exportType.equals(ExportBatchEvent.CREATE_STF))
                {
                    String[] args =
                    { msg };

                    throw new ExportEventObserverException(
                            ExportEventObserverException.MSG_FAILED_TO_GET_PAGE,
                            args, ex);
                }
                else
                {
                    s_logger.error("Failed to get page:" + msg);
                }
            }

            page.setState(ExportingPage.EXPORT_IN_PROGRESS);
            event.addExportingPages(page);
        }

        event = createExportBatchEvent(event);

        return event.getId();
    }

    /**
     * Persists the export batch event.
     */
    private ExportBatchEvent createExportBatchEvent(ExportBatchEvent p_event)
            throws ExportEventObserverException
    {
        try
        {
            HibernateUtil.save(p_event);
            if (s_logger.isDebugEnabled())
            {
                s_logger.debug("Created export batch event "
                        + p_event.getIdAsLong() + ": " + p_event.toString());                
            }

            return p_event;
        }
        catch (Exception ex)
        {
            String[] args =
            { p_event.toString() };

            throw new ExportEventObserverException(
                    ExportEventObserverException.MSG_FAILED_TO_PERSIST_EXPORT_INFO,
                    args, ex);
        }
    }

    /**
     * Removes the specified ExportBatchEvent from the database.
     */
    private void deleteExportBatchEvent(long p_eventId)
            throws ExportEventObserverException
    {
        ExportBatchEvent event = null;

        Session session = null;
        Transaction transaction = null;
        try
        {
            session = HibernateUtil.getSession();
            transaction = session.beginTransaction();

            event = getExportBatchEventById(p_eventId, false);
            session.delete(event);
            transaction.commit();

            s_logger.debug("deleteExportBatchEvent: Event "
                    + event.getIdAsLong() + " has been removed.");
        }
        catch (Exception ex)
        {
            if (transaction != null)
            {
                transaction.rollback();
            }

            String[] args = new String[1];

            if (event != null)
            {
                args[0] = event.toString();
            }
            else
            {
                args[0] = "??";
            }

            throw new ExportEventObserverException(
                    ExportEventObserverException.MSG_FAILED_TO_REMOVE, args, ex);
        }
        finally
        {
            if (session != null)
            {
                // session.close();
            }
        }
    }

    /**
     * Updates the exporting page in the specified Export Batch.
     */
    private boolean updateExportingPage(long p_batchId, String p_pageId,
            HttpServletRequest p_request) throws ExportEventObserverException
    {
        ExportingPage page = null;

        Session session = null;
        Transaction transaction = null;
        try
        {

            s_logger.debug("Looking up export batch id: " + p_batchId
                    + ", pageId: " + p_pageId);

            String responseType = p_request
                    .getParameter(ExportConstants.RESPONSE_TYPE);
            String newState = responseType.equals(ExportConstants.SUCCESS) ? ExportingPage.EXPORTED
                    : ExportingPage.EXPORT_FAIL;
            long endTime = Long.parseLong(p_request
                    .getParameter(ExportConstants.EXPORTED_TIME));
            String details = p_request
                    .getParameter(ExportConstants.RESPONSE_DETAILS);
            String exportPath = p_request
                    .getParameter(ExportConstants.ABSOLUTE_EXPORT_PATH);
            String isComponentPage = p_request
                    .getParameter(ExportConstants.IS_COMPONENT_PAGE);

            page = getExportingPageById(p_batchId, Long.parseLong(p_pageId),
                    false);

            if (page != null)
            {
                session = HibernateUtil.getSession();
                transaction = session.beginTransaction();
                session.evict(page);
                page = (ExportingPage) session.get(
                        ExportingPage.class, page.getIdAsLong());
                page.setEndTime(endTime);
                page.setErrorMessage(details);
                page.setState(newState);
                page.setExportPath(exportPath);
                char isComp = (isComponentPage == null || isComponentPage
                        .equalsIgnoreCase("false")) ? 'N' : 'Y';
                page.setComponentPage(isComp);

                session.saveOrUpdate(page);
                transaction.commit();

                s_logger.debug("updateExportingPage: ExportBatchId "
                        + p_batchId
                        + " was found and the results were recorded "
                        + "for pageId " + p_pageId);

                return true;
            }
            else
            {
                s_logger.warn("updateExportingPage: PageId "
                        + p_pageId
                        + " in ExportBatch "
                        + p_batchId
                        + " was NOT found. Either the BatchId or the PageId are not valid.");
                return false;
            }
        }
        catch (Exception ex)
        {
            if (transaction != null)
            {
                transaction.rollback();
            }

            // just warn, but do not throw an exception
            s_logger.warn("updateExportingPage: PageId "
                    + p_pageId
                    + "in ExportBatch "
                    + p_batchId
                    + " was NOT found. Either the BatchId or the PageId are not valid.");
        }
        finally
        {
            if (session != null)
            {
                // session.close();
            }
        }

        return false;
    }

    /**
     * Updates the specified ExportBatchEvent. Right now this only updates the
     * completion time.
     */
    private void updateExportBatchEvent(long p_eventId, long p_endTime)
            throws ExportEventObserverException
    {
        ExportBatchEvent event = null;

        Session session = null;
        Transaction transaction = null;
        try
        {
            session = HibernateUtil.getSession();
            session.beginTransaction();

            event = getExportBatchEventById(p_eventId, false);

            if (event != null)
            {
                ExportBatchEvent eventClone = (ExportBatchEvent) session.get(
                        ExportBatchEvent.class, event.getIdAsLong());
                eventClone.setEndTime(p_endTime);
                session.update(eventClone);

                s_logger.debug("ExportBatch " + p_eventId + " has completed.");
            }
            else
            {
                s_logger.info("updateExportBatchEvent: WARNING!: ExportBatch "
                        + p_eventId + " was NOT found.");
            }
        }
        catch (Exception ex)
        {
            if (transaction != null)
            {
                transaction.rollback();
            }

            String[] args =
            { event.toString() };

            throw new ExportEventObserverException(
                    ExportEventObserverException.MSG_FAILED_TO_UPDATE, args, ex);
        }
        finally
        {
            if (session != null)
            {
                // session.close();
            }
        }
    }

    /**
     * Sends the export completion e-mail.
     */
    private void reportBatchResults(ExportBatchEvent p_batchEvent)
    {
        s_logger.debug("Sending completion e-mail for BatchId="
                + p_batchEvent.getIdAsLong());
        try
        {
            // get args for e-mail message
            UserManager mgr = ServerProxy.getUserManager();
            User user = mgr.getUser(p_batchEvent.getResponsibleUserId());
            Locale userLocale = LocaleWrapper.getLocale(user
                    .getDefaultUILocale());
            String[] results = makePageResults(p_batchEvent, userLocale);
            String status = results[0];
            String msgBody = results[1];

            String dateBatchStarted = DateHelper.getFormattedDateAndTime(
                    new Date(p_batchEvent.getStartTime()), userLocale);

            // Get date completed....
            // note: end date is not updated if the job is done and about
            // to be removed. In that case we create the date here just for
            // the e-mail.
            long time = p_batchEvent.getEndTime();
            if (time == 0)
            {
                time = System.currentTimeMillis();
            }

            String dateBatchCompleted = DateHelper.getFormattedDateAndTime(
                    new Date(time), userLocale);
            Job job = p_batchEvent.getJob();
            String companyIdStr = String.valueOf(job.getCompanyId());

            String[] args = new String[]
            {
                    job.getJobName(),
                    p_batchEvent.getIdAsLong().toString(),
                    dateBatchStarted,
                    dateBatchCompleted,
                    user.getDisplayName(userLocale),
                    msgBody,
                    getLocalizedExportType(p_batchEvent.getExportType(),
                            userLocale),
                    Long.toString(p_batchEvent.getJob().getId()) };

            String subject = (status == null ? MailerConstants.REPORT_EXPORT_COMPLETED_SUBJECT
                    : MailerConstants.REPORT_EXPORT_FAILED_SUBJECT);

            ExportEventObserverHelper.sendEmail(user, args, subject,
                    "message_export_completed", companyIdStr);

            // For email changes issue
            // also send the mails to workflow managers
            List workflowsIds = p_batchEvent.getWorkflowIds();

            int wfSize = workflowsIds.size();
            // get exported workflows
            for (int i = 0; i < wfSize; i++)
            {
                Workflow workflow = WorkflowPersistenceAccessor
                        .getWorkflowById(((Long) workflowsIds.get(i))
                                .intValue());
                List wfManagerIds = workflow
                        .getWorkflowOwnerIdsByType(Permission.GROUP_WORKFLOW_MANAGER);
                int size = wfManagerIds.size();
                // notify workflow managers (if any)
                for (int j = 0; j < size; j++)
                {
                    User wfuser = mgr.getUser((String) wfManagerIds.get(j));

                    ExportEventObserverHelper.sendEmail(wfuser, args, subject,
                            "message_export_completed", companyIdStr);

                }
            }
            // End modification for email changes issue

            if (s_isDell)
            {
                notifyImportInitiator(p_batchEvent, args, subject,
                        "message_export_completed");
            }
        }
        catch (Exception ex)
        {
            s_logger.error(
                    "Failed to send e-mail. The export process has completed but\r\n"
                            + "the user will not receive e-mail notification due to the following exception:\r\n",
                    ex);
        }
    }

    /**
     * Notifies the person who initiated the job about the export being
     * complete. The job's first request's event flow xml is retrieved and the
     * importInitiatorId plucked from that.
     * 
     * @param p_batchEvent
     * @param p_args
     * @param p_subject
     * @param p_msgKey
     */
    private void notifyImportInitiator(ExportBatchEvent p_batchEvent,
            String[] p_args, String p_subject, String p_msgKey)
    {
        XmlParser xmlParser = null;
        try
        {
            // first get back the event flow xml
            Job job = p_batchEvent.getJob();
            Collection requests = job.getRequestList();
            Iterator iter = requests.iterator();
            Request firstRequest = (Request) iter.next();
            String efxml = firstRequest.getEventFlowXml();
            String companyIdStr = String.valueOf(job.getCompanyId());

            // parse the efxml for the import initiator id
            xmlParser = XmlParser.hire();

            Document document = xmlParser.parseXml(efxml);
            Element root = document.getRootElement();
            Node node = root
                    .selectSingleNode("/eventFlowXml/source/@importInitiatorId");
            if (node != null)
            {
                String importInitatorId = node.getText();
                if (importInitatorId != null && importInitatorId.length() > 0)
                {
                    User user = ServerProxy.getUserManager().getUser(
                            importInitatorId);
                    s_logger.debug("Emailing import initator "
                            + importInitatorId + " at email address "
                            + user.getEmail());
                    ExportEventObserverHelper.sendEmail(user, p_args,
                            p_subject, p_msgKey, companyIdStr);
                }
            }
        }
        catch (Exception ex)
        {
            s_logger.error(
                    "Failed to send e-mail to the import initiator. The export process has completed but\r\n"
                            + "the user will not receive e-mail notification due to the following exception:\r\n",
                    ex);
        }
        finally
        {
            if (xmlParser != null)
            {
                XmlParser.fire(xmlParser);
            }
        }
    }

    /**
     * Builds the page results section of the export completion e-mail.
     */
    private String[] makePageResults(ExportBatchEvent p_event,
            Locale p_userLocale)
    {
        Hashtable mainComponentResultsText = new Hashtable();
        Hashtable mainComponentFinalStatus = new Hashtable();
        Hashtable subComponentResultsText = new Hashtable();
        StringBuffer buff = null;
        StringBuffer combinedResults = new StringBuffer();
        String tmp;
        List epList = p_event.getExportingPages();
        ExportingPage ep = null;
        Long unknownLocale = new Long(-1);
        GlobalSightLocale locale = null;
        Long localeId = unknownLocale;
        String pageName = null;
        boolean batchFailed = false;

        // build page results sections - and save them per locale
        for (int i = 0; i < epList.size(); i++)
        {
            ep = (ExportingPage) epList.get(i);
            pageName = "??";
            boolean isComponentPage = ep.isComponentPage();

            if (p_event.getExportType().equals(ExportBatchEvent.EXPORT_SOURCE))
            {
                Page page = (SourcePage) ep.getPage();

                locale = page.getGlobalSightLocale();
                localeId = locale.getIdAsLong();
                String exportPath = ep.getExportPath(); // see "note" below...

                pageName = exportPath
                        .equals(ExportConstants.ABSOLUTE_EXPORT_PATH_UNKNOWN) ? page
                        .getExternalPageId() : exportPath;
                if (s_logger.isDebugEnabled())
                {
                    s_logger.debug("Page is a source page (" + pageName + ","
                            + locale.getDisplayName() + ", EP_ExportPath"
                            + exportPath + ", isComponentPage="
                            + ep.isComponentPage() + ")");                    
                }
            }
            else
            {
                GenericPage genPage = (GenericPage) ep.getPage();

                if (genPage instanceof TargetPage)
                {
                    Page page = (TargetPage) genPage;

                    locale = page.getGlobalSightLocale();
                    localeId = locale.getIdAsLong();
                    String exportPath = ep.getExportPath();

                    // Note: Export path could be null if the given
                    // adapter has not been updated to set the
                    // Absolute export path in the CXE response.
                    // If this is the case, we show the ExternalPageId
                    // (as we did in the past).
                    pageName = exportPath
                            .equals(ExportConstants.ABSOLUTE_EXPORT_PATH_UNKNOWN) ? page
                            .getExternalPageId() : exportPath;

                    s_logger.debug("Page is a target page (" + pageName + ","
                            + locale.getDisplayName() + ", EP_ExportPath"
                            + exportPath + ", isComponentPage="
                            + ep.isComponentPage() + ")");
                }
                else if (genPage instanceof SecondaryTargetFile)
                {
                    SecondaryTargetFile stf = (SecondaryTargetFile) genPage;

                    locale = stf.getWorkflow().getTargetLocale();
                    localeId = locale.getIdAsLong();
                    String storagePath = stf.getStoragePath();
                    String wfId = String.valueOf(stf.getWorkflow().getId());
                    // need to display the storage path without the
                    // preceding job id and workflow id (to look
                    // similar to a target page).
                    int startIndex = storagePath.lastIndexOf(wfId)
                            + wfId.length() + 1;
                    pageName = storagePath.substring(startIndex);

                    if (s_logger.isDebugEnabled())
                    {
                        s_logger.debug("Page is a secondary target file ("
                                + pageName + "," + locale.getDisplayName()
                                + ")");                        
                    }
                }
            }

            // append to ongoing results for this locale
            if (isComponentPage)
            {
                buff = (StringBuffer) subComponentResultsText.get(localeId);
            }
            else
            {
                buff = (StringBuffer) mainComponentResultsText.get(localeId);
            }

            if (buff == null)
            {
                if (isComponentPage)
                {
                    buff = new StringBuffer();
                    buff.append("\r\n    ");
                    buff.append(locale.getDisplayName());
                    buff.append("\r\n    ");
                    buff.append(getLocalizedComponentHeading(p_userLocale));
                    buff.append("\r\n");
                    subComponentResultsText.put(localeId, buff);
                }
                else
                {
                    buff = new StringBuffer();
                    buff.append("\r\n  ");
                    buff.append(locale.getDisplayName());
                    buff.append("\r\n");
                    mainComponentResultsText.put(localeId, buff);
                    mainComponentFinalStatus.put(localeId, new StringBuffer());
                }
            }

            buff.append(isComponentPage ? "      " : "    ");
            tmp = getLocalizedExportState(p_userLocale, ep.getState());
            buff.append(tmp);

            if (ep.getState().equals(ExportingPage.EXPORT_FAIL))
            {
                batchFailed = true;

                StringBuffer statusBuff = (StringBuffer) mainComponentFinalStatus
                        .get(localeId);

                statusBuff.append("    ");
                statusBuff
                        .append(getLocalizedCompositeExportStatusMsg(p_userLocale));
            }

            // may not always have the FileSystem date
            long endTime = ep.getEndTime();
            if (endTime > 0)
            {
                buff.append(" - ");
                buff.append(DateHelper.getFormattedDateAndTime(
                        new Date(endTime), p_userLocale));
            }

            buff.append(" - ");
            buff.append(pageName);
            buff.append("\r\n");
        }

        // assemble all locale based result sections into one message
        Object[] keys = mainComponentResultsText.keySet().toArray();
        int size = keys.length;
        for (int j = 0; j < size; j++)
        {
            // main page (of composite ms-office file) and/or a normal single
            // page
            combinedResults.append(((StringBuffer) mainComponentResultsText
                    .get(keys[j])).toString());
            combinedResults.append(((StringBuffer) mainComponentFinalStatus
                    .get(keys[j])).toString());

            // optional component results (of an ms-office file export)
            if (subComponentResultsText.get(keys[j]) != null)
            {
                combinedResults.append(((StringBuffer) subComponentResultsText
                        .get(keys[j])).toString());
            }
        }

        if (size == 0 && !subComponentResultsText.isEmpty())
        {
            // No main component
            keys = subComponentResultsText.keySet().toArray();
            size = keys.length;
            for (int j = 0; j < size; j++)
            {
                combinedResults.append(((StringBuffer) subComponentResultsText
                        .get(keys[j])).toString());
            }
        }

        String[] statusAndResults =
        { batchFailed ? "batchFailed" : null, combinedResults.toString() };
        return statusAndResults;
    }

    /**
     * Gets a localized string of the export type constant.
     */
    private String getLocalizedExportType(String p_exportType, Locale p_locale)
    {
        ResourceBundle bundle = s_sysResBundle.getResourceBundle(
                ResourceBundleConstants.LOCALE_RESOURCE_NAME, p_locale);

        if (p_exportType.equals(ExportBatchEvent.EXPORT_SOURCE))
        {
            return bundle.getString("msg_export_source");
        }
        if (p_exportType.equals(ExportBatchEvent.CREATE_STF))
        {
            return bundle.getString("msg_export_create_stf");
        }
        if (p_exportType.equals(ExportBatchEvent.FINAL_PRIMARY))
        {
            return bundle.getString("msg_export_final_primary");
        }
        if (p_exportType.equals(ExportBatchEvent.FINAL_SECONDARY))
        {
            return bundle.getString("msg_export_final_secondary");
        }
        if (p_exportType.equals(ExportBatchEvent.INTERIM_PRIMARY))
        {
            return bundle.getString("msg_export_interim_primary");
        }
        if (p_exportType.equals(ExportBatchEvent.INTERIM_SECONDARY))
        {
            return bundle.getString("msg_export_interim_secondary");
        }

        return "??";
    }

    /**
     * Gets a localized version of the final export state constant.
     */
    private String getLocalizedExportState(Locale p_locale, String p_state)
    {
        ResourceBundle bundle = s_sysResBundle.getResourceBundle(
                ResourceBundleConstants.LOCALE_RESOURCE_NAME, p_locale);

        if (p_state.equals(ExportingPage.EXPORT_FAIL))
        {
            return bundle.getString("msg_failed");
        }
        if (p_state.equals(ExportingPage.EXPORTED))
        {
            return bundle.getString("msg_ok");
        }
        if (p_state.equals(ExportingPage.EXPORT_IN_PROGRESS))
        {
            return bundle.getString("lb_inprogress");
        }

        return "??";
    }

    /**
     * Gets a localized composite export status message.
     */
    private String getLocalizedCompositeExportStatusMsg(Locale p_locale)
    {
        ResourceBundle bundle = s_sysResBundle.getResourceBundle(
                ResourceBundleConstants.LOCALE_RESOURCE_NAME, p_locale);

        return bundle.getString("msg_composite_export_failed");
    }

    /**
     * Gets a localized component heading.
     */
    private String getLocalizedComponentHeading(Locale p_locale)
    {
        ResourceBundle bundle = s_sysResBundle.getResourceBundle(
                ResourceBundleConstants.LOCALE_RESOURCE_NAME, p_locale);

        return bundle.getString("lb_component_status_heading");
    }

    /**
     * Upon server startup, removes all the export batch events that are older
     * than a month.
     */
    private void removeOldEvents()
    {
        try
        {
            long monthInMilliSecs = 30l * (24 * 60 * 60 * 1000);
            Long threshold = new Long((System.currentTimeMillis())
                    - monthInMilliSecs);

            Vector arg = new Vector();
            arg.add(threshold);

            String hql = "from ExportBatchEvent e where e.startTime <= :time";
            Map map = new HashMap();
            map.put("time", threshold);
            List events = HibernateUtil.search(hql, map);
            HibernateUtil.delete(events);
        }
        catch (Exception ex)
        {
            s_logger.error("Failed to delete old export batch events: ", ex);
        }
    }

    private void updateCorpusWithTargetPage(String p_pageId,
            HttpServletRequest p_request, boolean p_batchComplete)
    {
        try
        {
            String tempExportPath = p_request
                    .getParameter(ExportConstants.TEMP_EXPORT_PATH);

            if (tempExportPath == null)
            {
                // nothing to do
                return;
            }

            // get target page
            long targetPageId = Long.valueOf(p_pageId).longValue();
            TargetPage tp = ServerProxy.getPageManager().getTargetPage(
                    targetPageId);

            if (Modules.isCorpusInstalled()
                    && CorpusTm.isStoringNativeFormatDocs()
                    && tp.getCuvId() != null)
            {
                // get cuv
                CorpusDoc cuv = ServerProxy.getCorpusManager().getCorpusDoc(
                        tp.getCuvId());

                // This used to check if the file existed and then did
                // not over-write it. This was changed to accomodate
                // STF files.
                s_logger.info("Saving target doc "
                        + cuv.getCorpusDocGroup().getCorpusName()
                        + " for locale " + cuv.getLocale().toString()
                        + " to the corpus.");

                ServerProxy.getNativeFileManager()
                        .copyFileToStorage(tempExportPath,
                                cuv.getNativeFormatPath(), true/* delete */);
            }
            else
            {
                // delete the temp file because it will not be used
                File f = new File(tempExportPath);
                if (f.exists())
                {
                    f.delete();
                }
            }

            if (p_batchComplete)
            {
                // Make all target pages in the job use the same
                // native format target file for their workflow if
                // it's an ms-office job.
                s_logger.debug("cleaning up ms office target pages.");

                ServerProxy.getCorpusManager().cleanUpMsOfficeJobTargetPages(
                        targetPageId);
            }
        }
        catch (Exception ex)
        {
            // report the exception but don't hurt export
            s_logger.error(
                    "Could not save target corpus doc in native format.", ex);
        }
    }
}
