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

// Java
import java.io.File;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.ResourceBundle;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.globalsight.cxe.adapter.documentum.DocumentumOperator;
import com.globalsight.cxe.entity.fileprofile.FileProfile;
import com.globalsight.cxe.entity.knownformattype.KnownFormatType;
import com.globalsight.cxe.message.CxeMessageType;
import com.globalsight.cxe.util.CxeProxy;
import com.globalsight.cxe.util.XmlUtil;
import com.globalsight.cxe.util.fileImport.eventFlow.Category;
import com.globalsight.cxe.util.fileImport.eventFlow.EventFlowXml;
import com.globalsight.everest.corpus.CorpusDoc;
import com.globalsight.everest.corpus.CorpusDocGroup;
import com.globalsight.everest.corpus.CorpusManagerWLRemote;
import com.globalsight.everest.foundation.BasicL10nProfile;
import com.globalsight.everest.foundation.L10nProfile;
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.jobhandler.JobHandler;
import com.globalsight.everest.jobhandler.JobImpl;
import com.globalsight.everest.jobhandler.jobmanagement.JobDispatchEngine;
import com.globalsight.everest.page.AddingSourcePageManager;
import com.globalsight.everest.page.DataSourceType;
import com.globalsight.everest.page.PageEventObserver;
import com.globalsight.everest.page.PageState;
import com.globalsight.everest.page.SourcePage;
import com.globalsight.everest.page.TargetPage;
import com.globalsight.everest.page.UpdateSourcePageManager;
import com.globalsight.everest.persistence.PersistenceException;
import com.globalsight.everest.projecthandler.MachineTranslationProfile;
import com.globalsight.everest.projecthandler.TranslationMemoryProfile;
import com.globalsight.everest.projecthandler.WorkflowTemplateInfo;
import com.globalsight.everest.request.BatchInfo;
import com.globalsight.everest.request.Request;
import com.globalsight.everest.request.RequestHandler;
import com.globalsight.everest.request.RequestImpl;
import com.globalsight.everest.request.reimport.ActivePageReimporter;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.taskmanager.TaskImpl;
import com.globalsight.everest.util.system.SystemConfigParamNames;
import com.globalsight.everest.util.system.SystemConfiguration;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.pagehandler.administration.config.xmldtd.XmlDtdManager;
import com.globalsight.everest.webapp.pagehandler.administration.mtprofile.MTProfileHandlerHelper;
import com.globalsight.everest.webapp.pagehandler.edit.inctxrv.pdf.CreatePdfThread;
import com.globalsight.everest.webapp.pagehandler.edit.inctxrv.pdf.PreviewPDFHelper;
import com.globalsight.everest.workflow.EventNotificationHelper;
import com.globalsight.everest.workflowmanager.Workflow;
import com.globalsight.everest.workflowmanager.WorkflowImpl;
import com.globalsight.everest.workflowmanager.WorkflowOwner;
import com.globalsight.ling.tm2.persistence.DbUtil;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.persistence.jobcreation.AddRequestToJobCommand;
import com.globalsight.persistence.jobcreation.JobCreationQuery;
import com.globalsight.persistence.jobcreation.UpdateWorkflowAndPageStatesCommand;
import com.globalsight.util.GeneralException;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.RuntimeCache;
import com.globalsight.util.mail.MailerConstants;
import com.globalsight.util.modules.Modules;
import com.globalsight.util.resourcebundle.LocaleWrapper;
import com.globalsight.util.resourcebundle.ResourceBundleConstants;
import com.globalsight.util.resourcebundle.SystemResourceBundle;

/**
 * JobCreatorLocal implements JobCreator and is responsible for importing the
 * request, creating jobs and adding requests (and pages) to the appropriate
 * job. It also notifies the JobDispatcher of the job.
 */
public class JobCreatorLocal implements JobCreator
{
    private static Logger c_logger = Logger.getLogger(JobCreatorLocal.class);
    private static SystemResourceBundle m_sysResBundle = SystemResourceBundle
            .getInstance();

    private RequestProcessor m_requestProcessor;
    private JobAdditionEngine m_jobAdditionEngine;

    private List m_specialFormatTypes = new ArrayList();

    // determines whether the system-wide notification is enabled
    private boolean m_systemNotificationEnabled = EventNotificationHelper
            .systemNotificationEnabled();

    //
    // PUBLIC CONSTRUCTOR
    //
    /**
     * Creates the JobCreator.
     * 
     * @throws JobCreationException
     *             if an error occurs
     */
    public JobCreatorLocal() throws JobCreationException
    {
        m_requestProcessor = new RequestProcessor();
        m_jobAdditionEngine = new JobAdditionEngine();

        loadValidFormatTypes();
    }

    /**
     * Processes the request (ie. Imports the SourcePage and creates the Target
     * Pages) and adds the Request to a Job.
     * 
     * @param p_request
     *            the request object that will be processed and added to a job.
     * 
     * @throws JobCreationException
     *             if any errors occur
     * @throws RemoteException
     *             if there is a network issue
     */
    public void addRequestToJob(Request p_request) throws RemoteException,
            JobCreationException
    {
        HashMap pages = null;
        Job job = null;

        try
        {
            BatchInfo info = p_request.getBatchInfo();
            boolean isBatch = (info != null);

            EventFlowXml e = XmlUtil.string2Object(EventFlowXml.class, p_request.getEventFlowXml());
            String theJobId = e.getBatchInfo().getJobId();
            if (theJobId != null)
            {
                job = JobCreationMonitor
                        .loadJobFromDB(Long.parseLong(theJobId));
                c_logger.info("debug info: job state is: " + job.getState());
                // Update the job to "LEVERAGING" state (GBS-2137)
                if (Job.EXTRACTING.equals(job.getState()))
                {
                	c_logger.info("Update job state from 'EXTRACTING' to 'LEVERAGING' for job ID: " + theJobId);
                    JobCreationMonitor.updateJobState(Long.parseLong(theJobId),
                            Job.LEVERAGING);
                }
            }

            // Import source page, target page and do leveraging
            pages = m_requestProcessor.processRequest(p_request);

            SourcePage sp = (SourcePage) pages.remove(p_request
                    .getL10nProfile().getSourceLocale().getIdAsLong());
            BatchMonitor monitor = new BatchMonitor();
            if (theJobId != null)
            {
                // from GBS-2137, job has already been initialized earlier
                job = processJob(theJobId, p_request, pages);
            }
            else
            {
                job = availableJob(p_request, monitor, isBatch, pages);
            }

            boolean isBatchComplete = isBatchComplete(job, monitor);
            if (isBatchComplete)
            {
                // GBS-3389: If all source files are handled, all requests
                // should be in DB, set this flag to true;
                job.setIsAllRequestGenerated(true);
                HibernateUtil.update(job);
                updateForWorkflowsWithoutTargetPages(job);
            }

            // Add the source language document to the CorpusTM
            if (p_request.getType() == Request.EXTRACTED_LOCALIZATION_REQUEST)
            {
                addSourceDocToCorpus(sp, p_request, job,
                        p_request.getBatchInfo());
            }

            // Update job state
            updateJobState(job, isBatch, isBatchComplete, sp, e);

            // Handle Documentum job
            if (DocumentumOperator.DCTM_CATEGORY.equalsIgnoreCase(p_request
                    .getDataSourceType()))
            {
                priorHandleDocumentumJob(e, job);
            }

            // remove job cache after batch complete
            if (isBatchComplete)
            {
                String uuid = e.getBatchInfo().getUuid();
                if (uuid == null && job != null && job instanceof JobImpl)
                {
                    uuid = ((JobImpl) job).getUuid();
                }

                if (uuid != null)
                {
                    RuntimeCache.clearJobAttributes(uuid);
                }
            }

            // Validates all XML files included in the job.
            if (isBatchComplete)
            {
                XmlDtdManager.validateJob(job);

                // in context review tool - Auto-generate PDFs
                // check if this funtion enabled
                boolean enabled = PreviewPDFHelper.isInContextReviewEnabled();
                if (enabled)
                {
                    CreatePdfThread t = new CreatePdfThread(job, c_logger);
                    t.start();
                }
            }
        }
        catch (Exception e)
        {
            c_logger.debug("Exception in job creation", e);
            c_logger.error("addRequestToJob: ", e);

            if (job != null)
            {
                JobCreationMonitor.updateJobState(job, Job.IMPORTFAILED);
            }

            // with this failure the pages need to be marked as import
            // failed because they aren't successfully added to a job.
            // remove the source page from the collection of pages -
            // should just hold the target pages
            try
            {
                getPageEventObserver().notifyImportFailEvent(
                        p_request.getSourcePage(), pages.values());
            }
            catch (Exception pe)
            {
            }

            String[] args =
            { Long.toString(p_request.getId()),
                    job == null ? null : Long.toString(job.getId()) };
            throw new JobCreationException(
                    JobCreationException.MSG_FAILED_TO_ADD_REQUEST_TO_JOB,
                    args, e);
        }
    }

    //
    // PRIVATE SUPPORT METHODS
    //

    /**
     * Continues to proceed with the creation of a job on the initial one.
     * <p>
     * Since GBS-2137, creating job will use this method instead of
     * availableJob().
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private synchronized Job processJob(String p_jobId, Request p_request,
            HashMap p_targetPages) throws JobCreationException
    {
        Job job = JobCreationMonitor.refreshJobFromDB(Long.parseLong(p_jobId));

        Transaction transaction = HibernateUtil.getTransaction();
        try
        {
        	// If workflows have not been created, create them...
            Collection<Workflow> listOfWorkflows = job.getWorkflows();
            if (listOfWorkflows == null || listOfWorkflows.size() == 0)
            {
                listOfWorkflows = m_jobAdditionEngine.createWorkflowInstances(
                        p_request, (JobImpl) job);
                persistJob((JobImpl) job, (RequestImpl) p_request,
                        (List<Workflow>) listOfWorkflows);
            }

            String hql = "from WorkflowImpl w where w.job.id = :jobId "
                    + "and w.targetLocale.id = :targetLocaleId";
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("jobId", job.getId());

            for (Iterator it = p_targetPages.values().iterator(); it.hasNext();)
            {
                TargetPage tp = (TargetPage) it.next();
                map.put("targetLocaleId", tp.getLocaleId());
                List<WorkflowImpl> ws = (List<WorkflowImpl>) HibernateUtil
                        .search(hql, map);
                for (WorkflowImpl w : ws)
                {
                    if (Workflow.CANCELLED.equalsIgnoreCase(w.getState()))
                    {
                        continue;
                    }
                    tp.setWorkflowInstance(w);
                    w.addTargetPage(tp);
                    tp.setCVSTargetModule(m_jobAdditionEngine.getTargetModule(tp));
                    tp.setTimestamp(new Timestamp(System.currentTimeMillis()));
                    HibernateUtil.update(tp);
                    HibernateUtil.update(w);
                    break;
                }
            }
            job.addRequest(p_request);
            HibernateUtil.update(job);

            HibernateUtil.commit(transaction);
        }
        catch (Exception e)
        {
            c_logger.error("Failed to create a new job for request: "
                    + p_request.getId(), e);
            try
            {
                transaction.rollback();
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

        try
        {
            getJobDispatchEngine().createDispatcher(job);
        }
        catch (Exception e)
        {
            c_logger.error(
                    "Failed to create a dispatcher for job: "
                            + job.getJobName(), e);
        }
        JobAdditionEngine.addJobNote(p_targetPages, (JobImpl) job);

        return job;
    }

    /**
     * Persists the job with attributes set into database.
     */
    private void persistJob(JobImpl job, RequestImpl request,
            List<Workflow> listOfWorkflows) throws PersistenceException
    {
        Map<GlobalSightLocale, Long> wfmap = new HashMap<GlobalSightLocale, Long>();
        try
        {
            TranslationMemoryProfile tmp = request.getL10nProfile()
                    .getTranslationMemoryProfile();
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
            job.setIsWordCountReached(false);
            job.setLeverageMatchThreshold((int) tmp.getFuzzyMatchThreshold());
            if (PageHandler.isInContextMatch(request))
            {
                job.setLeverageOption(Job.IN_CONTEXT);
            }
            else
            {
                job.setLeverageOption(Job.EXACT_ONLY);
            }

            for (Iterator<Workflow> it = listOfWorkflows.iterator(); it
                    .hasNext();)
            {
                WorkflowImpl workflow = (WorkflowImpl) it.next();
                workflow.setTimestamp(new Timestamp(System.currentTimeMillis()));
                workflow.setCompanyId(job.getCompanyId());
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
                    t.setCompanyId(job.getCompanyId());
                }
                job.addWorkflowInstance(workflow);
                HibernateUtil.saveOrUpdate(workflow);
            }
        }
        catch (Exception e)
        {
            c_logger.error("Error updating job: " + job.getJobName()
                    + " in database.", e);
            throw new PersistenceException(e);
        }
    }

    /**
     * Return an appropriate existing job, or create a new one if none exists.
     * This method uses a try-finally (no catch required) block to ensure that
     * all waiting threads are notified when the method is finished.
     * 
     * [York] When add many files via "Add Files", there will be deadlock(since
     * GBS-3042). As this method is seldom used, an easy fix is adding
     * "synchronized" to this.A possible better fix is to put the jobId in if
     * the job has been existed, but that need more test too.
     */
    private synchronized Job availableJob(Request p_request,
            BatchMonitor p_monitor, boolean p_isBatch, HashMap p_targetPages)
            throws JobCreationException
    {
        JobImpl job = null;
        Connection connection = null;
        int numberOfRowsUpdated = 0;
        Session session = HibernateUtil.getSession();
        Transaction transaction = session.beginTransaction();

        try
        {
            connection = DbUtil.getConnection();
            boolean autoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            // add the request (and source page) to the job
            AddRequestToJobCommand arjc = new AddRequestToJobCommand(
                    (RequestImpl) p_request);
            arjc.persistObjects(connection);

            numberOfRowsUpdated = arjc.getNumberOfRowsUpdated();

            if (numberOfRowsUpdated > 0)
            {
                // get the job
                c_logger.debug("The number of rows is " + numberOfRowsUpdated);
                job = (JobImpl) HibernateUtil.get(JobImpl.class,
                        arjc.getJobId());
                job.setId(arjc.getJobId());
                job.setState(p_isBatch ? Job.BATCHRESERVED : Job.PENDING);

                if (p_request.getPriority() != null
                        && !"".equals(p_request.getPriority())
                        && !"null".equals(p_request.getPriority()))
                {
                    job.setPriority(Integer.parseInt(p_request.getPriority()));
                }

                session.update(job);

                // verify if there are target pages add them to the workflow(s)
                // if only error pages then target pages won't exist.
                if (p_targetPages != null && p_targetPages.size() > 0)
                {
                    Iterator it = p_targetPages.values().iterator();
                    String hql = "from WorkflowImpl w where w.job.id = :jId "
                            + "and w.targetLocale.id = :tId";
                    Map<String, Long> map = new HashMap<String, Long>();
                    map.put("jId", job.getIdAsLong());
                    while (it.hasNext())
                    {
                        TargetPage tp = (TargetPage) it.next();
                        map.put("tId", new Long(tp.getLocaleId()));
                        List<WorkflowImpl> ws = (List<WorkflowImpl>) HibernateUtil
                                .search(hql, map);

                        for (WorkflowImpl w : ws)
                        {
                            // if
                            // (Workflow.CANCELLED.equalsIgnoreCase(w.getState()))
                            // {
                            // continue;
                            // }

                            tp.setWorkflowInstance(w);
                            w.addTargetPage(tp);
                            tp.setTimestamp(new Timestamp(System
                                    .currentTimeMillis()));
                            session.update(tp);
                            session.update(w);
                            break;
                        }
                    }

                    // Add job notes for uploaded files if it had.
                    JobAdditionEngine.addJobNote(p_targetPages, job);
                }
            }

            connection.commit();
            // commit all together
            transaction.commit();
            connection.setAutoCommit(autoCommit);

        }
        catch (Exception e)
        {
            c_logger.debug("exception in job creator local", e);

            try
            {
                connection.rollback();
                transaction.rollback();
            }
            catch (Exception sqle)
            {
            }

            String args[] = new String[2];
            args[0] = Long.toString(p_request.getId());
            if (job != null)
            {
                args[1] = Long.toString(p_request.getId());
            }

            throw new JobCreationException(
                    JobCreationException.MSG_FAILED_TO_ADD_REQUEST_TO_JOB,
                    args, e);
        }
        finally
        {
            DbUtil.silentReturnConnection(connection);
        }

        // A job does not exist yet, create one.
        if (numberOfRowsUpdated == 0)
        {
            try
            {
                job = (JobImpl) newJob(p_request, p_monitor, p_isBatch,
                        p_targetPages);

                if (p_request.getPriority() != null
                        && !"".equals(p_request.getPriority())
                        && !"null".equals(p_request.getPriority()))
                {
                    job.setPriority(Integer.parseInt(p_request.getPriority()));
                }
            }
            catch (Exception e)
            {
                String args[] = new String[1];
                args[0] = Long.toString(p_request.getId());
                throw new JobCreationException(
                        JobCreationException.MSG_FAILED_TO_CREATE_NEW_JOB,
                        args, e);
            }
        }

        job.setPageCount(job.getSourcePages().size());

        return job;
    }

    /**
     * Create and persist a new job whose name and state depend on the contents
     * of the given request. Return the job.
     */
    private Job newJob(Request p_request, BatchMonitor p_monitor,
            boolean p_isBatch, HashMap p_targetPages)
            throws JobCreationException
    {
        return m_jobAdditionEngine.createNewJob(p_request,
                (p_isBatch ? Job.BATCHRESERVED : Job.PENDING),
                (p_isBatch ? p_monitor.generateJobName(p_request) : null),
                p_targetPages);
    }

    private void updateJobState(Job p_job, boolean p_isBatch,
            boolean p_isBatchComplete, SourcePage p_sp, EventFlowXml ex)
            throws JobCreationException
    {
        try
        {
            if (p_isBatch)
            {
                // if batch and complete
                if (p_isBatchComplete)
                {
                    AddingSourcePageManager.removeAllAddingFiles(p_job.getId());
                    UpdateSourcePageManager
                            .removeAllUpdatedFiles(p_job.getId());

                    List<Workflow> dispatchedWK = new ArrayList<Workflow>();
                    Job job = null;

                    Collection<Workflow> wks = p_job.getWorkflows();
                    for (Workflow w : wks)
                    {
                        if (Workflow.DISPATCHED.equals(w.getState()))
                        {
                            dispatchedWK.add(w);
                        }
                    }

                    updateWorkflowAndSourcePageStates(p_job);

                    boolean containsFailedImports = false;
                    boolean shouldNotify = false;
                    if (containsImportFailRequests(p_job))
                    {
                        containsFailedImports = true;
                        job = loadJobIntoCacheFromDB(p_job);

                        if (isAlreadyInImportFailState(job.getState()))
                        {
                            // doNothing block
                        }
                        else
                        {
                            shouldNotify = handleImportFailure(job, ex);
                            job = loadJobIntoCacheFromDB(job); // refresh with
                            // state change
                        }
                    }
                    else
                    {
                        job = loadJobIntoCacheFromDB(p_job);

                        if (isAlreadyInDispatchedState(job.getState()))
                        {
                            // doNothing block
                        }
                        else
                        {
                            // As word-counts have been calculated previous,
                            // here
                            // need not do it again.
                            getJobDispatchEngine().dispatchBatchJob(job);

                            String orgState = job.getOrgState();
                            if (orgState != null)
                            {
                                job.setOrgState(null);
                                job.setState(orgState);
                                // HibernateUtil.update(job);
                                HibernateUtil.merge(job);

                                for (Workflow w : dispatchedWK)
                                {
                                    if (Workflow.READY_TO_BE_DISPATCHED
                                            .equals(w.getState()))
                                    {
                                        w.setState(Workflow.DISPATCHED);
                                        HibernateUtil.update(w);
                                    }
                                }
                            }
                        }
                    }

                    // job.setPageCount(job.getSourcePages().size());
                    String details = getJobContentInfo(job);
                    // if there are errors in the job send an email to
                    // the appropriate people
                    if ((containsFailedImports || job.containsFailedWorkflow())
                            && shouldNotify)
                    {
                        // Import Failed
                        sendEmailFromAdmin(job, false);
                    }
                }
                else
                {
                    // for GBS-2137, one page is to be finished, update the job
                    // to "PROCESSING" state
                    if (Job.LEVERAGING.equals(p_job.getState()))
                    {
                        JobCreationMonitor
                        		.updateJobState(p_job, Job.PROCESSING);
                    }
                }
            }
            else if (containsImportFailRequests(p_job))
            {
                Job job = loadJobIntoCacheFromDB(p_job);

                if (isAlreadyInImportFailState(job.getState()))
                {
                    // doNothing block
                }
                else
                {
                    handleImportFailure(job, ex);
                }
            }
            else
            {
                Job job = loadJobIntoCacheFromDB(p_job);

                if (isAlreadyInDispatchedState(job.getState()))
                {
                    // doNothing block
                }
                else
                {
                    getJobDispatchEngine().wordCountIncreased(job);
                }
            }
            if (c_logger.isDebugEnabled())
            {
                c_logger.info("Done update job state for job " + p_job.getId());
            }
        }
        catch (Exception e)
        {
            String[] args = new String[2];
            args[0] = "" + p_job.getId();
            args[1] = Job.IMPORTFAILED;
            throw new JobCreationException(
                    JobCreationException.MSG_FAILED_TO_UPDATE_JOB_STATE, args,
                    e);
        }
    }

    private boolean isAlreadyInDispatchedState(String p_currentState)
    {
        boolean isDispatched = false;

        if (p_currentState.equals(Job.DISPATCHED))
        {
            isDispatched = true;
        }

        return isDispatched;
    }

    private boolean isAlreadyInImportFailState(String p_currentState)
    {
        boolean isImportFail = false;

        if (p_currentState.equals(Job.IMPORTFAILED))
        {
            isImportFail = true;
        }

        return isImportFail;
    }

    private boolean isBatchComplete(Job p_job, BatchMonitor p_monitor)
            throws JobCreationException
    {
        try
        {
            JobCreationQuery query = new JobCreationQuery();

            List requestList = query.getRequestListByJobId(p_job.getId());

            return p_monitor.isBatchComplete(requestList);
        }
        catch (Exception ex)
        {
            String[] args = new String[1];
            args[0] = String.valueOf(p_job.getId());
            throw new JobCreationException(
                    JobCreationException.MSG_FAILED_TO_GET_REQUEST_LIST, args,
                    ex);
        }
    }

    /**
     * If one work-flow has no any target pages, remove it from job.
     * 
     * @param job
     * @throws Exception
     */
    private void updateForWorkflowsWithoutTargetPages(Job job) throws Exception
    {
        List<Workflow> rWorkflows = new ArrayList<Workflow>();

        Collection<Workflow> ws = job.getWorkflows();
        for (Workflow w : ws)
        {
            if (w.getAllTargetPages().size() == 0)
            {
                rWorkflows.add(w);
            }
        }

        ws.removeAll(rWorkflows);
        HibernateUtil.saveOrUpdate(job);

        for (Workflow w : rWorkflows)
        {
            HibernateUtil.delete(w);
        }
    }

    /**
     * Return true if the job itself or any of its requests have failed import.
     */
    private boolean containsImportFailRequests(Job p_job)
            throws JobCreationException
    {
        boolean importFailed = false;
        List requestList = null;

        try
        {
            JobCreationQuery jcq = new JobCreationQuery();
            requestList = jcq.getRequestListByJobId(p_job.getId());

            Iterator it = requestList.iterator();
            while (!importFailed && it.hasNext())
            {
                Request r = (Request) it.next();
                importFailed = (r.getType() < 0); // if the type is less than
                // '0' then it is a negative error type.
            }
        }
        catch (Exception e)
        {
            String[] args = new String[1];
            args[0] = "" + p_job.getId();
            throw new JobCreationException(
                    JobCreationException.MSG_FAILED_TO_GET_REQUEST_LIST, args,
                    e);
        }

        return importFailed;
    }

    /**
     * Load the job stored in the DB into the TOPLink cache. JDBC was used to
     * create the job in the DB - need to load it into the cache once it has
     * been created.
     */
    private Job loadJobIntoCacheFromDB(Job p_job) throws JobCreationException
    {
        Job job = null;

        try
        {
            job = getJobHandler().getJobById(p_job.getId());
        }
        catch (Exception e)
        {
            String[] args = new String[1];
            args[0] = "" + p_job.getId();
            throw new JobCreationException(
                    JobCreationException.MSG_FAILED_TO_FIND_JOB_IN_DB, args, e);
        }

        return job;
    }

    /* Return the job dispatch engine. */
    private JobDispatchEngine getJobDispatchEngine()
            throws JobCreationException
    {

        try
        {
            return ServerProxy.getJobDispatchEngine();
        }
        catch (Exception e)
        {
            c_logger.error("Unable to retrieve JobDispatch Engine", e);

            throw new JobCreationException(
                    JobCreationException.MSG_FAILED_TO_FIND_JOB_DISPATCHER,
                    null, e);
        }
    }

    /**
     * Update the workflows and source page states according to the state of the
     * target pages in the job.
     */
    private void updateWorkflowAndSourcePageStates(Job p_job)
            throws JobCreationException
    {
        // only perform this if reimport is set up to handle it
        int reimportOption = ActivePageReimporter.getReimportOption();
        if (reimportOption == ActivePageReimporter.REIMPORT_NEW_TARGETS)
        {
            Connection conn = null;
            try
            {
                conn = DbUtil.getConnection();
                conn.setAutoCommit(false);
                UpdateWorkflowAndPageStatesCommand updateState = new UpdateWorkflowAndPageStatesCommand(
                        p_job);
                updateState.persistObjects(conn);
                conn.commit();

                // check if there are any requests that should be marked as
                // failed
                List failedRequestIds = updateState.getFailedRequestsById();
                RequestHandler rh = ServerProxy.getRequestHandler();
                for (Iterator fri = failedRequestIds.iterator(); fri.hasNext();)
                {
                    long requestId = ((Long) fri.next()).intValue();
                    JobCreationException jce = new JobCreationException(
                            JobCreationException.MSG_FAILED_TO_IMPORT_ALL_TARGETS_SUCCESSFULLY,
                            null, null);
                    rh.setExceptionInRequest(requestId, jce);
                }
            }
            catch (Exception e)
            {
                c_logger.debug(
                        "exception while updating the workflow and source page states depending on the target.",
                        e);
                String[] args =
                { Long.toString(p_job.getId()) };
                throw new JobCreationException(
                        JobCreationException.MSG_FAILED_TO_UPDATE_WORKFLOW_AND_PAGE_STATE,
                        args, e);
            }
            finally
            {
                DbUtil.silentReturnConnection(conn);
            }
        }
    }

    /**
     * Wraps the code for getting the page manager and handling any exceptions.
     */
    private PageEventObserver getPageEventObserver()
            throws JobCreationException
    {
        PageEventObserver peo = null;

        try
        {
            peo = ServerProxy.getPageEventObserver();
        }
        catch (GeneralException ge)
        {
            c_logger.error("Couldn't find the PageEventObserver", ge);

            throw new JobCreationException(
                    JobCreationException.MSG_FAILED_TO_FIND_PAGE_EVENT_OBSERVER,
                    null, ge);
        }

        return peo;
    }

    private JobHandler getJobHandler() throws Exception
    {
        return ServerProxy.getJobHandler();
    }

    /**
     * Adds the source language document to the CorpusTM. Does not impact the
     * import process if there are CorpusTM errors. Those errors get logged.
     * 
     * @param p_sourcePage
     * @param p_request
     * @param p_job
     * @param p_isBatchComplete
     *            the source page that has successfully imported.
     */
    private void addSourceDocToCorpus(SourcePage p_sourcePage,
            Request p_request, Job p_job, BatchInfo p_batchInfo)
    {
        if (!Modules.isCorpusInstalled())
        {
            // need to delete the original source file that would have
            // been used as the native format corpus doc
            String fileName = p_request.getOriginalSourceFileContent();

            if (fileName != null)
            {
                File originalSourceFile = new File(fileName);

                if (originalSourceFile.exists())
                {
                    originalSourceFile.delete();
                }
            }

            return;
        }

        try
        {
            if (c_logger.isDebugEnabled())
            {
                c_logger.info("Begin adding source page to corpusTM : "
                        + p_sourcePage.getExternalPageId());
            }

            boolean deleteOriginal = true;
            String gxml = p_request.getGxml();
            File originalSourceFile = new File(
                    p_request.getOriginalSourceFileContent());

            CorpusManagerWLRemote corpusManager = ServerProxy
                    .getCorpusManager();
            CorpusDoc sourceCorpusDoc = corpusManager
                    .addNewSourceLanguageCorpusDoc(p_sourcePage, gxml,
                            originalSourceFile, deleteOriginal);
            CorpusDocGroup cdg = sourceCorpusDoc.getCorpusDocGroup();

            StringBuffer msg = new StringBuffer("Done adding source page ");
            msg.append(p_sourcePage.getExternalPageId());
            msg.append(" (");
            msg.append(cdg.getId()).append("/");
            msg.append(p_sourcePage.getGlobalSightLocale().toString());
            msg.append(") to corpusTM");
            c_logger.info(msg.toString());

            updateSourcePageCuvId(p_sourcePage, sourceCorpusDoc, p_job);

            c_logger.debug("p_sourcePage cuv_id = " + p_sourcePage.getCuvId());
        }
        catch (Throwable ex)
        {
            // Tue Oct 04 21:11:58 2005 CvdL: when I did a delayed
            // reimport of an office file into an existing job and
            // restarted GlobalSight to fix some errors, when the
            // reimport finally ran the call above "new File(
            // p_request.getOriginalSourceFileContent())" encountered
            // a null pointer exception.
            c_logger.error(
                    "Could not add source page "
                            + p_sourcePage.getExternalPageId() + " to corpus.",
                    ex);
        }
    }

    /**
     * Updates the cuv id in the SourcePage
     * 
     * @param p_sourcePage
     * @param p_corpusDoc
     * @exception Exception
     */
    private void updateSourcePageCuvId(SourcePage p_sourcePage,
            CorpusDoc p_sourceCorpusDoc, Job p_job) throws Exception
    {
        // first get the SourcePage object refreshed in the cache
        SourcePage sp = ServerProxy.getPageManager().getSourcePage(
                p_sourcePage.getId());
        Session session = null;
        Transaction transaction = null;
        try
        {
            session = HibernateUtil.getSession();
            transaction = session.beginTransaction();
            SourcePage clone = (SourcePage) session.get(SourcePage.class,
                    sp.getIdAsLong());
            clone.setCuvId(p_sourceCorpusDoc.getIdAsLong());
            session.update(clone);
            transaction.commit();
        }
        catch (Exception e)
        {
            if (transaction != null)
            {
                transaction.rollback();
            }
            throw new Exception(e);
        }
        finally
        {
            if (session != null)
            {
                // session.close();
            }
        }

        // replace the clone in the job's list of pages
        p_job.getSourcePages();

    }

    // Sends mail from the Admin to the PM about a Job that contains Import
    // Failures
    private void sendEmailFromAdmin(Job p_job, boolean p_reimportAsUnextracted)
            throws Exception
    {
        if (!m_systemNotificationEnabled)
        {
            return;
        }

        String companyIdStr = String.valueOf(p_job.getCompanyId());
        SystemConfiguration config = SystemConfiguration.getInstance();
        String capLoginUrl = config
                .getStringParameter(SystemConfigParamNames.CAP_LOGIN_URL);

        String messageArgs[] = new String[4];
        // send email to project manager
        messageArgs[0] = Long.toString(p_job.getId());
        messageArgs[1] = p_job.getJobName();
        messageArgs[3] = capLoginUrl;

        Request r = (Request) p_job.getRequestList().iterator().next();
        L10nProfile l10nProfile = r.getL10nProfile();
        GlobalSightLocale[] targetLocales = r.getTargetLocalesToImport();
        boolean shouldNotifyPm = false;
        Locale loc = null;
        String subject = p_reimportAsUnextracted ? MailerConstants.JOB_IMPORT_CORRECTION_SUBJECT
                : MailerConstants.JOB_IMPORT_FAILED_SUBJECT;
        String message = p_reimportAsUnextracted ? "jobImportCorrectionMessage"
                : "jobImportFailedMessage";
        String messageBody = new String();
        List<String> mailList = new ArrayList<String>();
        for (int i = 0; i < targetLocales.length; i++)
        {
            WorkflowTemplateInfo wfti = l10nProfile
                    .getWorkflowTemplateInfo(targetLocales[i]);
            if (!shouldNotifyPm && wfti.notifyProjectManager())
            {
                shouldNotifyPm = true;
            }
            List userIds = wfti.getWorkflowManagerIds();
            mailList.addAll(userIds);
            if (userIds != null)
            {
                for (Iterator uii = userIds.iterator(); uii.hasNext();)
                {
                    User user = ServerProxy.getUserManager().getUser(
                            (String) uii.next());
                    Locale userLocale = LocaleWrapper.getLocale(user
                            .getDefaultUILocale());
                    // if not generated yet or if they aren't the same
                    // regenerate the messageBody
                    if (loc == null || userLocale != loc)
                    {
                        loc = userLocale;
                        messageBody = createEmailMessageBody(p_job, loc,
                                p_reimportAsUnextracted);
                    }
                    messageArgs[2] = messageBody;

                    sendEmail(user, messageArgs, subject, message, companyIdStr);
                }
            }
        }
        // if at least one of the wfInfos had the pm notify flag on, notify PM.
        if (shouldNotifyPm)
        {
            String pmUserId = l10nProfile.getProject().getProjectManagerId();
            if (!mailList.contains(pmUserId))
            {
                User pm = ServerProxy.getUserManager().getUser(pmUserId);
                Locale pmLocale = LocaleWrapper.getLocale(pm
                        .getDefaultUILocale());

                if (pmLocale != loc)
                {
                    messageArgs[2] = createEmailMessageBody(p_job, pmLocale,
                            p_reimportAsUnextracted);
                }

                sendEmail(pm, messageArgs, subject, message, companyIdStr);
            }
        }
    }

    // send mail to user (Project manager / Workflow Manager)
    private void sendEmail(User p_user, String[] p_messageArgs,
            String p_subject, String p_message, String p_companyIdStr)
            throws Exception
    {
        if (!m_systemNotificationEnabled)
        {
            return;
        }

        ServerProxy.getMailer().sendMailFromAdmin(p_user, p_messageArgs,
                p_subject, p_message, p_companyIdStr);
    }

    // create the localized email message for a job that contains failed imports
    private String createEmailMessageBody(Job p_job, Locale p_locale,
            boolean p_reimportAsUnextracted)
    {
        ResourceBundle bundle = m_sysResBundle.getResourceBundle(
                ResourceBundleConstants.LOCALE_RESOURCE_NAME, p_locale);

        StringBuffer msgBody = new StringBuffer();

        // if the job is marked as import failure then
        // atleast one of its source pages has failed
        if (p_job.getState().equals(Job.IMPORTFAILED)
                || p_reimportAsUnextracted)
        {
            msgBody.append(bundle
                    .getString(p_reimportAsUnextracted ? "msg_import_correction_source_pages"
                            : "msg_import_fail_source_pages"));

            msgBody.append(":\r\n\r\n");

            Collection sourcePages = p_job.getSourcePages();
            for (Iterator spi = sourcePages.iterator(); spi.hasNext();)
            {
                SourcePage sp = (SourcePage) spi.next();

                // if there was an error
                if (sp.getRequest().getType() < 0)
                {
                    msgBody.append(bundle.getString("lb_source_page"));
                    msgBody.append(": ");
                    msgBody.append(sp.getExternalPageId());
                    msgBody.append("\r\n   ");
                    msgBody.append(bundle.getString("lb_failed_due_to"));
                    msgBody.append(": ");
                    msgBody.append(sp.getRequest().getException()
                            .getLocalizedMessage());
                    msgBody.append("\r\n");
                }
            }
        }

        if (p_job.containsFailedWorkflow())
        {
            msgBody.append("\r\n");
            msgBody.append(bundle.getString("msg_import_fail_workflows"));
            msgBody.append(":\r\n\r\n");

            Collection workflows = p_job.getWorkflows();
            for (Iterator wi = workflows.iterator(); wi.hasNext();)
            {
                Workflow w = (Workflow) wi.next();

                if (w.getState().equals(Workflow.IMPORT_FAILED))
                {
                    msgBody.append(bundle.getString("lb_target_locale"));
                    msgBody.append(": ");
                    msgBody.append(w.getTargetLocale().toString());
                    msgBody.append("\r\n");

                    Collection targetPages = w.getAllTargetPages();
                    for (Iterator tpi = targetPages.iterator(); tpi.hasNext();)
                    {
                        TargetPage tp = (TargetPage) tpi.next();
                        if (tp.getPageState().equals(PageState.IMPORT_FAIL))
                        {
                            msgBody.append("   ");
                            msgBody.append(bundle.getString("lb_target_page"));
                            msgBody.append(": ");
                            msgBody.append(tp.getSourcePage()
                                    .getExternalPageId());
                            msgBody.append("\r\n      ");
                            msgBody.append(bundle.getString("lb_failed_due_to"));
                            msgBody.append(": ");
                            msgBody.append(tp.getImportError()
                                    .getLocalizedMessage());
                            msgBody.append("\r\n");
                        }
                    }
                }
            }
        }
        return msgBody.toString();
    }

    /**
     * This method handles a special case with an import failure. If the
     * imported content's format type is equal to the ones listed in the
     * property file, an unextracted reimport is enforced and the failed job
     * would be discarded.
     */
    private boolean handleImportFailure(Job p_job, EventFlowXml ex) throws Exception
    {
        boolean notifyFailure = p_job.getRequestList().size() == 0
                || m_specialFormatTypes.size() == 0;

        // if no format types are set in property file, just update job state
        if (notifyFailure)
        {
            updateJobState(p_job, Job.IMPORTFAILED);
        }
        else
        {
            Request r = (Request) (p_job.getRequestList().iterator().next());

            FileProfile fp = ServerProxy.getFileProfilePersistenceManager()
                    .readFileProfile(r.getDataSourceId());

            KnownFormatType format = ServerProxy
                    .getFileProfilePersistenceManager().queryKnownFormatType(
                            fp.getKnownFormatTypeId());
            String name = format.getName().toLowerCase();

            String preMergeEvent = ex.getPreMergeEvent();

            notifyFailure = preMergeEvent == null
                    || CxeMessageType
                            .getCxeMessageType(
                                    CxeMessageType.UNEXTRACTED_LOCALIZED_EVENT)
                            .getName().equals(preMergeEvent)
                    || !m_specialFormatTypes.contains(name);
            if (notifyFailure)
            {
                updateJobState(p_job, Job.IMPORTFAILED);
            }
            else
            {
                notifyFailure = importByDataSourceType(ex, p_job);
            }
        }

        return notifyFailure;
    }

    private boolean importByDataSourceType(EventFlowXml ex,
            Job p_job) throws Exception
    {
        boolean isInvalidDataSource = false;
        String jobName = p_job.getJobName();
        String batchId = jobName + System.currentTimeMillis();
        String dataSourceType = ex.getSource().getDataSourceType();
        if (DataSourceType.VIGNETTE.equals(dataSourceType))
        {
            Category c = ex.getCategory().get(0);
            CxeProxy.importFromVignette(
                    jobName,
                    ex.getBatchInfo().getBatchId(),
                    1,
                    1,
                    1,
                    1,
                    c.getValue("ObjectId"),
                    c.getValue("Path"),
                    ex.getSource().getDataSourceId(),
                    c.getValue("SourceProjectMid") + "|" + c.getValue("TargetProjectMid"),
                    c.getValue("ReturnStatus"),
                    c.getValue("VersionFlag"),
                    Boolean.TRUE, CxeProxy.IMPORT_TYPE_L10N);
        }
        else if (dataSourceType != null
                && dataSourceType.startsWith(DataSourceType.FILE_SYSTEM))
        {
            boolean isAutoImport = dataSourceType.indexOf("AutoImport") > 0;
            String fileName =  ex.getSource().getValue("Filename");
            String initiatorId = ex.getSource().getValue("importInitiator");
            CxeProxy.importFromFileSystem(fileName, jobName, null, batchId,
                    ex.getSource().getDataSourceId(), new Integer(1),
                    new Integer(1), new Integer(1), new Integer(1),
                    new Boolean(isAutoImport), Boolean.TRUE,
                    CxeProxy.IMPORT_TYPE_L10N, initiatorId, new Integer(0));
        }
        else
        {
            isInvalidDataSource = true;
        }

        if (!isInvalidDataSource)
        {
            sendImportCorrectionEmail(p_job);
        }
        return isInvalidDataSource;
    }

    /*
     * Notify the PM and WFMs about the re-import of the failed job.
     */
    private void sendImportCorrectionEmail(Job p_job) throws Exception
    {
        // notify the PM about the new un-extracted import
        sendEmailFromAdmin(p_job, true);
        try
        {
            // now discard the old job (that had failed)
            getJobDispatchEngine().cancelJob(p_job);
        }
        catch (Exception e)
        {
            c_logger.error("Failed to discard the job with import failed state. "
                    + e);
        }
    }

    /*
     * Update the job state to be the given state.
     */
    private void updateJobState(Job p_job, String p_state) throws Exception
    {
        Session session = null;
        Transaction transaction = null;
        try
        {
            session = HibernateUtil.getSession();
            transaction = session.beginTransaction();
            Job jobClone = (Job) session.get(p_job.getClass(),
                    new Long(p_job.getId()));
            jobClone.setState(p_state);
            session.update(jobClone);
            transaction.commit();
        }
        catch (Exception e)
        {
            transaction.rollback();
            throw e;
        }
        finally
        {
            if (session != null)
            {
                // session.close();
            }
        }
    }

    /*
     * Load the valid format types that require special handling during an
     * extracted import failure.
     */
    private void loadValidFormatTypes()
    {
        try
        {
            List validFormatTypeNames = new ArrayList();
            validFormatTypeNames.add(KnownFormatType.PDF.toLowerCase());
            validFormatTypeNames.add(KnownFormatType.WORD.toLowerCase());
            validFormatTypeNames.add(KnownFormatType.EXCEL.toLowerCase());
            validFormatTypeNames.add(KnownFormatType.POWERPOINT.toLowerCase());

            SystemConfiguration sc = SystemConfiguration.getInstance();
            String value = sc
                    .getStringParameter(SystemConfiguration.HANDLE_IMPORT_FAILURE);

            if (value == null || value.length() == 0)
            {
                c_logger.debug("No format types to handle during import failure.");
                return;
            }

            String[] formatTypes = value.split(",");

            for (int i = 0; i < formatTypes.length; i++)
            {
                String s = formatTypes[i].trim().toLowerCase();
                if (validFormatTypeNames.contains(s))
                {
                    m_specialFormatTypes.add(s);
                }
                else
                {
                    c_logger.info("Invalid format type: " + s);
                }
            }
        }
        catch (Exception e)
        {
            c_logger.error("Failed to get the format types that should "
                    + "be handled different upon an import failure.", e);
        }
    }

    // package methods

    private String getJobContentInfo(Job p_job)
    {
        Locale uiLocale = new Locale("en", "US");
        StringBuffer sB = new StringBuffer();
        @SuppressWarnings("unchecked")
        List sourcePages = new ArrayList(p_job.getSourcePages());
        for (int i = 0; i < sourcePages.size(); i++)
        {
            SourcePage curPage = (SourcePage) sourcePages.get(i);
            // Page Name
            sB.append("Page Name =" + curPage.getExternalPageId() + "\n");
            // Word Count
            sB.append("Word Count =" + curPage.getWordCount() + "\n");
            // Status
            String state = curPage.getPageState();
            sB.append("Status =" + state + "\n");
            // Message
            sB.append("Message="
                    + ((state.equals(PageState.IMPORT_FAIL) || (curPage
                            .getRequest().getType() < 0)) ? curPage
                            .getRequest().getException()
                            .getTopLevelMessage(uiLocale) : "") + "\n");
        }
        return sB.toString();
    }

    /**
     * Prior real Documentum job handling.
     */
    private void priorHandleDocumentumJob(EventFlowXml e, Job job)
    {
        try
        {
            Category c = e.getCategory(DocumentumOperator.DCTM_CATEGORY);
            c_logger.debug("Starting to create a documentum job......");

            String dctmObjId = c.getValue(DocumentumOperator.DCTM_OBJECTID);
            String isAttrFileStr = c.getValue(DocumentumOperator.DCTM_ISATTRFILE);
            String userId = c.getValue(DocumentumOperator.DCTM_USERID);
            Boolean isAttrFile = Boolean.valueOf(isAttrFileStr);

            if (!isAttrFile.booleanValue())
            {
                handleDocumentumJob(job, dctmObjId, userId);
            }
            c_logger.debug("Finish to create a documentum job");
        }
        catch (NoSuchElementException nex)
        {
            c_logger.debug("Not a valid Documentum job");
        }
        catch (Exception ex)
        {
            c_logger.error(
                    "Failed to write attribute back to Documentum server", ex);
        }
    }

    /**
     * Write the custom attributes back to Documentum Server, including jobId,
     * Workflow Ids.
     * 
     * @param job
     *            - The new job created just now.
     * @param objId
     *            - Documentum Object Id.
     * @throws Exception
     */
    private void handleDocumentumJob(Job job, String objId, String userId)
            throws Exception
    {
        Connection connection = null;
        PreparedStatement psQueryWfIds = null;
        ResultSet rs = null;
        try
        {
            Collection<Long> wfIdsList = new ArrayList<Long>();
            connection = DbUtil.getConnection();
            StringBuffer debugInfo = new StringBuffer();

            // Find all the workflows for this Documentum Job.
            String jobId = String.valueOf(job.getJobId());
            debugInfo.append("JobId=").append(jobId).append(", ");
            debugInfo.append("WorkflowIds=");

            psQueryWfIds = connection
                    .prepareStatement(DocumentumOperator.DCTM_SELWFI_SQL);
            psQueryWfIds.setLong(1, job.getId());
            rs = psQueryWfIds.executeQuery();
            while (rs.next())
            {
                long wfId = rs.getLong(1);
                wfIdsList.add(new Long(wfId));
                debugInfo.append(wfId).append(" ");
            }

            c_logger.debug("Writing the custom attributes(jobId, workflow ids) back to Documentum server");
            // Write custom attributes(jobId, workflow ids) back to Documentum
            // Server.
            DocumentumOperator.getInstance().writeCustomAttrsBack(userId,
                    objId, jobId, wfIdsList);
            c_logger.debug(debugInfo.toString());
        }
        finally
        {
            DbUtil.silentClose(rs);
            DbUtil.silentClose(psQueryWfIds);
            DbUtil.silentReturnConnection(connection);
        }
    }
}
