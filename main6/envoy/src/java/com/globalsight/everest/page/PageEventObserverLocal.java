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

package com.globalsight.everest.page;

// globalsight imports

import java.rmi.RemoteException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.globalsight.everest.foundation.L10nProfile;
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.page.pageexport.DelayedExporter;
import com.globalsight.everest.permission.Permission;
import com.globalsight.everest.persistence.PersistenceService;
import com.globalsight.everest.projecthandler.Project;
import com.globalsight.everest.projecthandler.WorkflowTemplateInfo;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.tuv.Tuv;
import com.globalsight.everest.tuv.TuvEventObserver;
import com.globalsight.everest.util.system.SystemConfiguration;
import com.globalsight.everest.workflow.EventNotificationHelper;
import com.globalsight.everest.workflowmanager.Workflow;
import com.globalsight.everest.workflowmanager.WorkflowEventObserver;
import com.globalsight.everest.workflowmanager.WorkflowExportingHelper;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.persistence.pageimport.UpdateSourcePageCommand;
import com.globalsight.persistence.pageimport.UpdateTargetPageCommand;
import com.globalsight.persistence.pageimport.delayedimport.DelayedImportQuery;
import com.globalsight.util.GeneralException;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.mail.MailerConstants;

/**
 * This class represents an observer of events that affect Page. The callers
 * notify the observer of an event that could have an affect on the state of the
 * page.
 */
public class PageEventObserverLocal implements PageEventObserver
{
    private static final Logger s_category = Logger
            .getLogger(PageEventObserverLocal.class.getName());

    static final String EXPORT_FAILED_MESSAGE = "exportFailedMessage";

    // determines whether the system-wide notification is enabled
    private boolean m_systemNotificationEnabled = EventNotificationHelper
            .systemNotificationEnabled();

    // ////////////////////////////////////////////////////////////////
    // Begin: PageEventObserver Implementation
    // ////////////////////////////////////////////////////////////////

    /**
     * Changes state of all source pages and target pages (that aren't in the
     * IMPORT_FAILED state to ACTIVE_JOB.
     * 
     * @see PageEventObserver#notifyWorkflowDispatchEvent(Collection)
     * @param p_sourcePages
     *            source pages.
     * @param p_targetPages
     *            target pages.
     * @throws PageException
     *             when an error occurs.
     */
    public void notifyWorkflowDispatchEvent(Collection p_sourcePages,
            Collection p_targetPages) throws PageException, RemoteException
    {
        // combine two collections - only target pages that aren't in
        // the IMPORT_FAIL state though
        for (Iterator tpi = p_targetPages.iterator(); tpi.hasNext();)
        {
            TargetPage tp = (TargetPage) tpi.next();
            if (!tp.getPageState().equals(PageState.IMPORT_FAIL))
            {
                p_sourcePages.add(p_targetPages);
            }
        }

        PagePersistenceAccessor.updateStateOfPages(p_sourcePages,
                PageState.ACTIVE_JOB);
    }

    /**
     * Set state of target pages to LOCALIZED.
     * 
     * @see PageEventObserver#notifyLastTaskCompleteEvent(Collection)
     * @param p_targetPages
     *            target pages.
     * @throws PageException
     *             when an error occurs.
     */
    public void notifyLastTaskCompleteEvent(Collection p_targetPages)
            throws PageException, RemoteException
    {
        PagePersistenceAccessor.updateStateOfPages(p_targetPages,
                PageState.LOCALIZED);
    }

    /**
     * Set state of source pages to LOCALIZED.
     * 
     * @see PageEventObserver#notifyLastTaskCompleteAllWorkflowsEvent(Collection)
     * @param p_sourcePages
     *            source pages.
     * @throws PageException
     *             when an error occurs.
     */
    public void notifyLastTaskCompleteAllWorkflowsEvent(Collection p_sourcePages)
            throws PageException, RemoteException
    {
        PagePersistenceAccessor.updateStateOfPages(p_sourcePages,
                PageState.LOCALIZED);
    }

    /**
     * Call TuvEventObserver.notifyTaskCompleteEvent() on all tuvs in the target
     * pages.
     * 
     * @see PageEventObserver#notifyTaskCompleteEvent(Collection, long)
     * @param p_targetPages
     *            target pages.
     * @param p_taskId
     *            task identifier that completed.
     * @throws PageException
     *             when an error occurs.
     */
    public void notifyTaskCompleteEvent(Collection p_targetPages, long p_taskId)
            throws PageException, RemoteException
    {
        try
        {
            List targetTuvs = getTuvsOfTargetPages(p_targetPages);
            getTuvEventObserver().notifyTaskCompleteEvent(targetTuvs, p_taskId);
        }
        catch (Exception e)
        {
            throw new PageException(e);
        }

    }

    /**
     * Sets the state of a target page to EXPORT_IN_PROGRESS.
     * 
     * @see PageEventObserver#notifyExportInProgressEvent(TargetPage)
     * @param p_targetPage
     *            target page that is being exported.
     * @throws PageException
     *             when an error occurs.
     */
    public void notifyExportInProgressEvent(TargetPage p_targetPage)
            throws PageException, RemoteException
    {
        try
        {
            long id = p_targetPage.getId();

            PagePersistenceAccessor.updateStateOfPage(p_targetPage,
                    PageState.EXPORT_IN_PROGRESS);

            // Does this refresh anything or is it useless?
            TargetPage updatedPage = ServerProxy.getPageManager()
                    .getTargetPage(id);
        }
        catch (GeneralException e)
        {
            String[] args =
            { p_targetPage.getExternalPageId() };
            throw new PageException(PageException.MSG_FAILED_TO_GET_PAGE_BY_ID,
                    args, e);
        }
    }

    /**
     * Set state of target page to EXPORTED. Call
     * TuvEventObserver.notifyPageExportedEvent() on all its tuvs. (Used to
     * index all target tuvs, reversing the TM.)
     * 
     * @see PageEventObserver#notifyExportSuccessEvent(Page)
     * @param p_targetPage
     *            target page that exported successfully.
     * @throws PageException
     *             when an error occurs.
     */
    public void notifyExportSuccessEvent(TargetPage p_targetPage)
            throws PageException, RemoteException
    {
        try
        {
            long id = p_targetPage.getId();
            PagePersistenceAccessor.updateStateOfPage(p_targetPage,
                    PageState.EXPORTED);

            TargetPage updatedPage = ServerProxy.getPageManager()
                    .getTargetPage(id);

            if (updatedPage.getPrimaryFileType() == ExtractedSourceFile.EXTRACTED_FILE)
            {
                // get all non deleted TUVs in the page
                Collection tuvs = getNonDeletedTuvsOfTargetPage(updatedPage);

                // notify and update the state of the TUVs that are not deleted
                long jobId = p_targetPage.getSourcePage().getJobId();
                getTuvEventObserver().notifyPageExportedEvent(tuvs, jobId);
            }

            // if it's the last page, let WorkflowEventObserver know...
            notifyWorkflowIfLastPage(updatedPage.getWorkflowInstance());
        }
        catch (Exception e) // TuvException and WorkflowManagerException
        {
            throw new PageException(e);
        }
    }

    /**
     * Completes the export for update when notified about the source page's
     * successful export.
     * 
     * @param p_sourcePage
     *            the source page
     * @exception PageException
     * @exception RemoteException
     */
    public void notifyExportForUpdateSuccessEvent(SourcePage p_sourcePage)
            throws PageException, RemoteException
    {
        try
        {
            DelayedImportQuery query = new DelayedImportQuery();
            long jobid = query.findJobIdOfPage(p_sourcePage.getId());
            Job job = ServerProxy.getJobHandler().getJobById(jobid);

            if (s_category.isDebugEnabled())
            {
                s_category.debug("src page id is " + p_sourcePage.getId());
                s_category.debug("job id is " + job.getId());
                s_category.debug("job name is " + job.getJobName());
            }

            DelayedExporter.getInstance().completeExportForUpdate(job);
        }
        catch (Exception e)
        {
            throw new PageException(e);
        }
    }

    /**
     * Handles notifying the PM that an export for update has failed.
     * 
     * @param p_sourcePage
     *            the source page
     * @exception PageException
     * @exception RemoteException
     */
    public void notifyExportForUpdateFailEvent(SourcePage p_sourcePage)
            throws PageException, RemoteException
    {
        try
        {
            DelayedImportQuery query = new DelayedImportQuery();
            long jobid = query.findJobIdOfPage(p_sourcePage.getId());
            Job job = ServerProxy.getJobHandler().getJobById(jobid);
            DelayedExporter.getInstance().handleFailedExportForUpdate(job,
                    p_sourcePage);
        }
        catch (Exception e)
        {
            throw new PageException(e);
        }
    }

    /**
     * Notification that the page export got cancelled.
     * 
     * @param p_targetPage
     *            - The target page of the export cancelled action.
     * @throws PageException
     *             when an error occurs.
     */
    public void notifyExportFailCancelEvent(TargetPage p_targetPage)
            throws PageException, RemoteException
    {
        PagePersistenceAccessor.updateStateOfPage(p_targetPage,
                PageState.EXPORT_CANCELLED);
    }

    /**
     * Set state of source pages to EXPORTED. Call
     * TuvEventObserver.notifyJobExportedEvent().
     * 
     * @see PageEventObserver#notifyExportSuccessAllSourceFPagesEvent(Page)
     * @param p_sourcePages
     *            source pages that all target pages exported successfully.
     * @throws PageException
     *             when an error occurs.
     */
    public void notifyAllSourcePagesExportedEvent(Collection p_sourcePages)
            throws PageException, RemoteException
    {
        try
        {
            PagePersistenceAccessor.updateStateOfPages(p_sourcePages,
                    PageState.EXPORTED);

            for (Iterator it = p_sourcePages.iterator(); it.hasNext();)
            {
                SourcePage sp = (SourcePage) it.next();
                getTuvEventObserver().notifyJobExportedEvent(
                        getTuvsOfSourcePage(sp), sp.getJobId());
            }
        }
        catch (Exception e)
        {
            throw new PageException(e);
        }
    }

    /**
     * Set state of target page to EXPORT_FAILED.
     * 
     * @see PageEventObserver#notifyExportFailEvent(Page)
     * @param p_targetPage
     *            the target page that export failed.
     * @param p_exceptionXml
     *            GeneralException XML for why the export failed
     * @throws PageException
     *             when an error occurs.
     */
    public void notifyExportFailEvent(TargetPage p_targetPage,
            String p_exceptionXml) throws PageException, RemoteException
    {
        notifyExportFailEvent(p_targetPage, p_exceptionXml, true);
    }

    /**
     * Set state of target page to EXPORT_FAILED.
     * 
     * @see PageEventObserver#notifyExportFailEvent(Page)
     * @param p_targetPage
     *            the target page that export failed.
     * @param p_exceptionXml
     *            GeneralException XML for why the export failed
     * @throws PageException
     *             when an error occurs.
     */
    public void notifyExportFailEvent(TargetPage p_targetPage,
            String p_exceptionXml, boolean sendEmail) throws PageException,
            RemoteException
    {
        WorkflowExportingHelper.setPageAsNotExporting(p_targetPage.getId());

        try
        {
            Workflow wf = p_targetPage.getWorkflowInstance();

            // an interim export can happen during dispatch without
            // updating an states.
            if (!inProgressWorkflow(wf.getState()))
            {
                PagePersistenceAccessor.updateStateOfPage(p_targetPage,
                        PageState.EXPORT_FAIL, p_exceptionXml);

                getWorkflowEventObserver().notifyWorkflowExportFailedEvent(wf);
            }

            if (sendEmail)
            {
                notifyProjectManager(p_targetPage);
            }
        }
        catch (Exception e)
        {
            // if it fails, send an email to admin email address in
            // property file?
            String[] args =
            { p_targetPage.toString() };
            throw new PageException(
                    PageException.MSG_FAILED_TO_UPDATE_PAGE_STATE, args, e);
        }
    }

    private boolean inProgressWorkflow(String workflowState)
    {
        return Workflow.READY_TO_BE_DISPATCHED.equals(workflowState)
                || Workflow.PENDING.equals(workflowState)
                || Workflow.DISPATCHED.equals(workflowState);
    }

    /**
     * @see PageEventObserver#notifyImportSuccessEvent(Page, Collection, Page,
     *      Collection) <p>
     *      Set state IMPORT_SUCCESS for new pages. Set state OUT_OF_DATE for
     *      old pages.
     * @param p_sourcePage
     *            source page that imported successfully.
     * @param p_targetPages
     *            target pages that imported successfully.
     * @throws PageException
     *             when an error occurs.
     */
    public void notifyImportSuccessEvent(SourcePage p_sourcePage,
            Collection p_targetPages) throws PageException, RemoteException
    {
        Connection connection = null;

        try
        {
            connection = PersistenceService.getInstance().getConnection();
            connection.setAutoCommit(false);

            p_sourcePage.setPageState(PageState.IMPORT_SUCCESS);

            UpdateSourcePageCommand uspc = new UpdateSourcePageCommand(
                    p_sourcePage);

            uspc.persistObjects(connection);

            UpdateTargetPageCommand utpc = new UpdateTargetPageCommand(
                    p_sourcePage, p_targetPages);

            utpc.persistObjects(connection);

            connection.commit();
        }
        catch (Exception e)
        {
            s_category
                    .error("The notify import success event could not be persisted");

            try
            {
                connection.rollback();
            }
            catch (Exception sqle)
            {
            }

            throw new PageException(e);
        }
        finally
        {
            try
            {
                PersistenceService.getInstance().returnConnection(connection);
            }
            catch (Exception e)
            {
                s_category.error("The connection could not be returned");
            }
        }
    }

    /**
     * Set state IMPORT_FAIL. Note that deletion of LeverageGroups will also
     * delete all dependent objects (i.e. Tus, and Tuvs) since they are
     * privately owned by a LeverageGroup (part of TopLink mapping).
     * 
     * @param p_sourcePage
     *            source page that import failed.
     * @param p_targetPages
     *            target pages that import failed (can be empty).
     * @throws PageException
     *             when an error occurs.
     * @see PageEventObserver#notifyImportFailEvent(Page, Collection)
     */
    public void notifyImportFailEvent(SourcePage p_sourcePage,
            Collection p_targetPages) throws PageException, RemoteException
    {
        Connection connection = null;

        try
        {
            connection = PersistenceService.getInstance().getConnection();
            connection.setAutoCommit(false);

            p_sourcePage.setPageState(PageState.IMPORT_FAIL);

            UpdateSourcePageCommand uspc = new UpdateSourcePageCommand(
                    p_sourcePage);

            uspc.persistObjects(connection);

            if (p_targetPages != null && p_targetPages.size() > 0)
            {
                UpdateTargetPageCommand utpc = new UpdateTargetPageCommand(
                        p_sourcePage, p_targetPages);

                utpc.persistObjects(connection);
            }

            connection.commit();
        }
        catch (Exception e)
        {
            s_category.error("Could not persist page state failure.");

            try
            {
                connection.rollback();
            }
            catch (Exception sqle)
            {
            }

            throw new PageException(e);
        }
        finally
        {
            try
            {
                PersistenceService.getInstance().returnConnection(connection);
            }
            catch (Exception e)
            {
                s_category
                        .error("The connection could not be returned to the pool");
            }
        }
    }

    public void notifyImportSuccessNewTargetPagesEvent(Collection p_targetPages)
            throws PageException, RemoteException
    {
        Connection connection = null;

        try
        {
            connection = PersistenceService.getInstance().getConnection();
            connection.setAutoCommit(false);

            for (Iterator i = p_targetPages.iterator(); i.hasNext();)
            {
                TargetPage tp = (TargetPage) i.next();
                tp.setPageState(PageState.IMPORT_SUCCESS);

                UpdateTargetPageCommand utpc = new UpdateTargetPageCommand(tp);
                utpc.persistObjects(connection);
            }
            connection.commit();
        }
        catch (Exception e)
        {
            s_category.error("Could not persist page state failure.");

            try
            {
                connection.rollback();
            }
            catch (Exception sqle)
            {
            }

            throw new PageException(e);
        }
        finally
        {
            try
            {
                PersistenceService.getInstance().returnConnection(connection);
            }
            catch (Exception e)
            {
                s_category
                        .error("The connection could not be returned to the pool");
            }
        }
    }

    public void notifyImportFailureNewTargetPagesEvent(Collection p_targetPages)
            throws PageException, RemoteException
    {
        Session session = null;
        Transaction transaction = null;
        try
        {
            session = HibernateUtil.getSession();
            transaction = session.beginTransaction();

            Iterator it = p_targetPages.iterator();
            while (it.hasNext())
            {
                TargetPage tp = (TargetPage) it.next();
                TargetPage targetPageClone = (TargetPage) session.get(
                        TargetPage.class, tp.getIdAsLong());
                targetPageClone.setPageState(PageState.IMPORT_FAIL);
                session.update(targetPageClone);
            }
            transaction.commit();
        }
        catch (Exception e)
        {
            if (transaction != null)
            {
                transaction.rollback();
            }

            s_category.error("Could not persist page state failure.");
            throw new PageException(e);
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
     * @see PageEventObserver.notifyImportFailEvent(Collection)
     */
    public void notifyImportFailEvent(Collection p_targetPages)
            throws PageException, RemoteException
    {
        Connection connection = null;

        try
        {
            connection = PersistenceService.getInstance().getConnection();
            connection.setAutoCommit(false);

            for (Iterator i = p_targetPages.iterator(); i.hasNext();)
            {
                TargetPage tp = (TargetPage) i.next();
                tp.setPageState(PageState.IMPORT_FAIL);

                UpdateTargetPageCommand utpc = new UpdateTargetPageCommand(tp);
                utpc.persistObjects(connection);
            }
            connection.commit();
        }
        catch (Exception e)
        {
            s_category.error("Could not persist page state failure.");

            try
            {
                connection.rollback();
            }
            catch (Exception sqle)
            {
            }

            throw new PageException(e);
        }
        finally
        {
            try
            {
                PersistenceService.getInstance().returnConnection(connection);
            }
            catch (Exception e)
            {
                s_category
                        .error("The connection could not be returned to the pool");
            }
        }
    }

    // ////////////////////////////////////////////////////////////////
    // End: PageEventObserver Implementation
    // ////////////////////////////////////////////////////////////////

    // ////////////////////////////////////////////////////////////////
    // Begin: Local Methods
    // ////////////////////////////////////////////////////////////////

    // get TuvEventObserver remote object
    private TuvEventObserver getTuvEventObserver() throws PageException
    {
        try
        {
            return ServerProxy.getTuvEventObserver();
        }
        catch (GeneralException ge)
        {
            throw new PageException(
                    PageException.MSG_FAILED_TO_LOCATE_TUV_EVENT_OBSERVER,
                    null, ge);
        }
    }

    // get WorkflowEventObserver remote object
    private WorkflowEventObserver getWorkflowEventObserver()
            throws PageException
    {
        try
        {
            return ServerProxy.getWorkflowEventObserver();
        }
        catch (GeneralException ge)
        {
            throw new PageException(
                    PageException.MSG_FAILED_TO_LOCATE_WF_EVENT_OBSERVER, null,
                    ge);
        }
    }

    // get the tuvs of a collection of pages.
    // private List getTuvsOfSourcePages(Collection p_pages) throws Exception
    // {
    // Object[] sourcePages = p_pages.toArray();
    // int size = sourcePages.length;
    // List tuvs = new ArrayList();
    //
    // for (int i = 0; i < size; i++)
    // {
    // SourcePage page = (SourcePage) sourcePages[i];
    // tuvs.addAll(getTuvsOfSourcePage(page));
    // }
    //
    // return tuvs;
    // }

    // get the tuvs of a collection of pages.
    private List getTuvsOfTargetPages(Collection p_pages) throws Exception
    {
        Object[] targetPages = p_pages.toArray();
        int size = targetPages.length;
        List tuvs = new ArrayList();

        for (int i = 0; i < size; i++)
        {
            TargetPage page = (TargetPage) targetPages[i];
            tuvs.addAll(getTuvsOfTargetPage(page));
        }

        return tuvs;
    }

    // get a collection of tuvs for a page (tuvs are retrieved based on
    // the page id).
    private Collection getTuvsOfSourcePage(SourcePage p_page) throws Exception
    {
        return ServerProxy.getTuvManager().getSourceTuvsForStatistics(p_page);
    }

    private Collection getTuvsOfTargetPage(TargetPage p_page) throws Exception
    {
        return ServerProxy.getTuvManager().getTargetTuvsForStatistics(p_page);
    }

    // get all the TUVs that are in a target page and have not been deleted
    private Collection getNonDeletedTuvsOfTargetPage(TargetPage p_targetPage)
            throws Exception
    {
        // get all TUVs in the page
        Collection tuvs = getTuvsOfTargetPage(p_targetPage);

        // if the page has deletable content - check for any
        // deleted tuvs and remove from the collection

        ExtractedSourceFile esf = (ExtractedSourceFile) p_targetPage
                .getSourcePage().getPrimaryFile();
        if (esf.containGsTags())
        {
            HashSet tuIds = getNonDeletedTuIds(p_targetPage.getSourcePage(),
                    p_targetPage.getGlobalSightLocale());
            // if they are less - so some were deleted
            if (tuIds.size() < tuvs.size())
            {
                boolean removedAll = false;

                for (Iterator tuvi = tuvs.iterator(); tuvi.hasNext()
                        && !removedAll;)
                {
                    Tuv t = (Tuv) tuvi.next();
                    // if it can't find th TU the TUV is associated with - then
                    // remove from the
                    // collection
                    if (!tuIds.contains(t.getTuId()))
                    {
                        // remove the current one
                        tuvi.remove();
                        // the sizes are the same - so found and removed
                        // all deleted TUVs
                        if (tuIds.size() == tuvs.size())
                        {
                            removedAll = true;
                        }
                    }
                }
            }
            // else none were deleted so just fall out and continue
        }
        return tuvs;
    }

    // notify the project manager
    private void notifyProjectManager(TargetPage p_targetPage)
    {
        if (!m_systemNotificationEnabled)
        {
            return;
        }
        try
        {
            Job job = p_targetPage.getWorkflowInstance().getJob();
            // long l10nProfileId = job.getL10nProfileId();
            // ProjectHandler ph = ServerProxy.getProjectHandler();
            L10nProfile l10nProfile = job.getL10nProfile();// ph.getL10nProfile(l10nProfileId);
            // TomyD workaround - project object has both the PM's id and
            // User object. Project is populated using TopLink and since
            // User is stored in LDAP, it won't be set in Project object and
            // it's value will be null. It's safer to use user name.
            Project project = ServerProxy.getProjectHandler().getProjectById(
                    l10nProfile.getProjectId());
            GlobalSightLocale gslocale = p_targetPage.getGlobalSightLocale();
            SystemConfiguration config = SystemConfiguration.getInstance();
            String capLoginUrl = config
                    .getStringParameter(SystemConfiguration.CAP_LOGIN_URL);
            String companyIdStr = String.valueOf(job.getCompanyId());

            String[] messageArguments = new String[6];
            messageArguments[0] = p_targetPage.getExternalPageId();
            messageArguments[1] = job.getDataSourceName();
            messageArguments[2] = l10nProfile.getName();
            messageArguments[3] = project.getName();
            messageArguments[4] = gslocale.getDisplayName();
            messageArguments[5] = capLoginUrl;
            WorkflowTemplateInfo wfti = l10nProfile
                    .getWorkflowTemplateInfo(gslocale);

            List wfManagerIds = p_targetPage.getWorkflowInstance()
                    .getWorkflowOwnerIdsByType(
                            Permission.GROUP_WORKFLOW_MANAGER);
            int size = wfManagerIds.size();

            // notify all workflow managers (if any)
            for (int i = 0; i < size; i++)
            {
                User user = ServerProxy.getUserManager().getUser(
                        (String) wfManagerIds.get(i));
                ServerProxy.getMailer().sendMailFromAdmin(user,
                        messageArguments,
                        MailerConstants.EXPORT_FAILED_SUBJECT,
                        EXPORT_FAILED_MESSAGE, companyIdStr);
            }

            if (wfti.notifyProjectManager())
            {

                String userName = project.getProjectManagerId();
                User user = ServerProxy.getUserManager().getUser(userName);
                ServerProxy.getMailer().sendMailFromAdmin(user,
                        messageArguments,
                        MailerConstants.EXPORT_FAILED_SUBJECT,
                        EXPORT_FAILED_MESSAGE, companyIdStr);
            }
        }
        catch (Exception e)
        {
            s_category
                    .error("Failed to notify Project Manager about an export failure.",
                            e);
        }
    }

    // check to see if all pages of a workflow have been exported...
    private boolean isExported(Collection p_targetPages) throws PageException,
            RemoteException
    {
        Object[] targetPages = p_targetPages.toArray();
        int size = targetPages.length;
        for (int i = 0; i < size; i++)
        {
            TargetPage page = (TargetPage) targetPages[i];
            if (!PageState.EXPORTED.equals(page.getPageState()))
            {
                return false;
            }
        }

        return true;
    }

    /**
     * Notifies the workflow event observer if all pages have been exported;
     * also call the notify all target pages exported method.
     */
    private void notifyWorkflowIfLastPage(Workflow p_workflow) throws Exception
    {
        if (isExported(p_workflow.getTargetPages()))
        {
            if (s_category.isDebugEnabled())
            {
                s_category.debug("All target pages are exported!!!");
            }

            getWorkflowEventObserver().notifyWorkflowExportedEvent(p_workflow);
        }
        else
        {
            if (s_category.isDebugEnabled())
            {
                s_category.debug("All target pages are NOT exported!!!");
            }
        }
    }

    /**
     * Return the list of TU ids that are part of the page and have not been
     * deleted. tbd -
     */
    private HashSet getNonDeletedTuIds(SourcePage p_page,
            GlobalSightLocale p_locale) throws PageException
    {
        HashSet result = new HashSet();

        try
        {
            // Just return Caller guarantees source page contains GS tags.

            // if the page contains an extracted file
            if (p_page.getPrimaryFileType() == ExtractedSourceFile.EXTRACTED_FILE)
            {
                if (((ExtractedSourceFile) p_page.getPrimaryFile())
                        .containGsTags())
                {
                    // get the Page Template

                    PageTemplate template = ((ExtractedFile) p_page
                            .getPrimaryFile())
                            .getPageTemplate(PageTemplate.TYPE_DETAIL);
                    String locale = p_locale.toString();
                    template = new SnippetPageTemplate(template, locale);

                    Collection parts = ServerProxy.getPageManager()
                            .getTemplatePartsForSourcePage(
                                    p_page.getIdAsLong(),
                                    template.getTypeAsString());

                    // ALWAYS set the template parts before getting the page
                    // data
                    template.setTemplateParts(new ArrayList(parts));
                    result = template.getInterpretedTuIds();
                }
            }
        }
        catch (Exception e)
        {
            String[] args =
            { "Failed to get the snippet page template." };

            s_category.error(args[0], e);
            // tbd
            throw new PageException(e); // OnlineEditorException.MSG_FAILED_TO_GET_PAGEVIEW,
            // args, ge);
        }

        return result;
    }

    // ////////////////////////////////////////////////////////////////
    // End: Local Methods
    // ////////////////////////////////////////////////////////////////
}
