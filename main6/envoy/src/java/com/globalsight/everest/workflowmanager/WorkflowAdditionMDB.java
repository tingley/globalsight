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

package com.globalsight.everest.workflowmanager;

import java.io.File;
import java.sql.Connection;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.Vector;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.globalsight.calendar.FluxCalendar;
import com.globalsight.cxe.adaptermdb.EventTopicMap;
import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.foundation.L10nProfile;
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.foundation.UserImpl;
import com.globalsight.everest.integration.ling.LingServerProxy;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.page.PagePersistenceAccessor;
import com.globalsight.everest.page.PageState;
import com.globalsight.everest.page.SourcePage;
import com.globalsight.everest.page.TargetPage;
import com.globalsight.everest.page.UnextractedFile;
import com.globalsight.everest.page.pageimport.TargetPagePersistence;
import com.globalsight.everest.persistence.tuv.SegmentTuvUtil;
import com.globalsight.everest.projecthandler.Project;
import com.globalsight.everest.projecthandler.TranslationMemoryProfile;
import com.globalsight.everest.projecthandler.WorkflowTemplateInfo;
import com.globalsight.everest.request.Request;
import com.globalsight.everest.request.WorkflowRequest;
import com.globalsight.everest.request.WorkflowRequestImpl;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.statistics.StatisticsService;
import com.globalsight.everest.taskmanager.TaskImpl;
import com.globalsight.everest.tuv.Tuv;
import com.globalsight.everest.util.jms.GenericQueueMDB;
import com.globalsight.everest.util.jms.JmsHelper;
import com.globalsight.everest.util.system.SystemConfigParamNames;
import com.globalsight.everest.util.system.SystemConfiguration;
import com.globalsight.everest.workflow.Activity;
import com.globalsight.everest.workflow.EventNotificationHelper;
import com.globalsight.everest.workflow.WfTaskInfo;
import com.globalsight.ling.tm.ExactMatchedSegments;
import com.globalsight.ling.tm.LeveragingLocales;
import com.globalsight.ling.tm2.TmCoreManager;
import com.globalsight.ling.tm2.leverage.LeverageDataCenter;
import com.globalsight.ling.tm2.leverage.LeverageOptions;
import com.globalsight.ling.tm2.persistence.DbUtil;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.terminology.ITermbase;
import com.globalsight.terminology.ITermbaseManager;
import com.globalsight.terminology.termleverager.TermLeverageOptions;
import com.globalsight.terminology.termleverager.TermLeverageResult;
import com.globalsight.util.AmbFileStoragePathUtils;
import com.globalsight.util.GeneralException;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.mail.MailerConstants;
import com.globalsight.util.resourcebundle.LocaleWrapper;
import com.globalsight.util.system.ConfigException;

/**
 * This is the concrete implementation of the WorkflowAdditionListener.
 */
@MessageDriven(messageListenerInterface = MessageListener.class, activationConfig =
{
        @ActivationConfigProperty(propertyName = "destination", propertyValue = EventTopicMap.QUEUE_PREFIX_JBOSS
                + JmsHelper.JMS_WORKFLOW_ADDITION_QUEUE),
        @ActivationConfigProperty(propertyName = "destinationType", propertyValue = JmsHelper.JMS_TYPE_QUEUE) })
@TransactionManagement(value = TransactionManagementType.BEAN)
public class WorkflowAdditionMDB extends GenericQueueMDB
{

    private static final long serialVersionUID = 0L;

    private static final Logger c_logger = Logger
            .getLogger(WorkflowAdditionMDB.class);

    static public final String UNEXTRACTED_SUB_DIRECTORY = "GlobalSight"
            + File.separator + "UnextractedFiles";

    static private final String IMPORT_FAILURE = "importFailure";

    static private Boolean s_autoReplaceTerms = null;

    // private MachineTranslator m_machineTranslator = null;

    private HashMap<String, List<Workflow>> m_map = null;

    // determines whether the system-wide notification is enabled
    private boolean m_systemNotificationEnabled = EventNotificationHelper
            .systemNotificationEnabled();

    public WorkflowAdditionMDB()
    {
        super(c_logger);

        if (s_autoReplaceTerms == null)
        {
            s_autoReplaceTerms = getLeverageOptions();
        }

        // initMachineTranslation();
    }

    private void resetInstanceVariables()
    {
        s_autoReplaceTerms = null;
        // m_machineTranslator = null;
        m_map = null;
    }

    /**
     * JMS public interface javax.jms.MessageListener implementation method
     * Receives a request from CXE, parses out the parameters and calls
     * "submitRequest" to pass the request on for processing
     * 
     * @param p_cxeRequest
     *            The JMS message containing the request for localization.
     */
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public void onMessage(Message p_cxeRequest)
    {
        Hashtable ht = null;

        try
        {
            ObjectMessage msg = (ObjectMessage) p_cxeRequest;
            ht = (Hashtable) msg.getObject();

            CompanyThreadLocal.getInstance().setIdValue(
                    (String) ht.get(CompanyWrapper.CURRENT_COMPANY_ID));

            ht.remove(CompanyWrapper.CURRENT_COMPANY_ID);

            resetInstanceVariables();
        }
        catch (JMSException jmse)
        {
            c_logger.error("Failed to get MapMessage or retrieving one "
                    + "of the parameters " + p_cxeRequest.toString(), jmse);
            HibernateUtil.closeSession();
            return;
        }

        long jobId = ((Long) ht.get("JOB")).longValue();
        L10nProfile l10nProfile = null;
        Job job = null;
        ArrayList workflowTemplates = new ArrayList();

        try
        {
            job = ServerProxy.getJobHandler().getJobById(jobId);

            Collection sourcePages = job.getSourcePages();
            Iterator it = sourcePages.iterator();

            // CvdL: isn't the profile object already loaded???
            long profileId = job.getL10nProfile().getId();
            l10nProfile = ServerProxy.getProjectHandler().getL10nProfile(
                    profileId);

            ht.remove("JOB");

            WorkflowRequest workflowRequest = new WorkflowRequestImpl();
            workflowRequest
                    .setType(WorkflowRequest.ADD_WORKFLOW_REQUEST_TO_EXISTING_JOB);

            Iterator it2 = ht.values().iterator();
            while (it2.hasNext())
            {
                long wfTempId = ((Long) it2.next()).longValue();
                WorkflowTemplateInfo wfTempInfo = ServerProxy
                        .getProjectHandler().getWorkflowTemplateInfoById(
                                wfTempId);
                workflowTemplates.add(wfTempInfo);
            }

            long workflowRequestId = ServerProxy.getRequestHandler()
                    .createWorkflowRequest(workflowRequest, job,
                            workflowTemplates);

            m_map = new HashMap<String, List<Workflow>>();

            while (it.hasNext())
            {
                SourcePage sourcePage = (SourcePage) it.next();
                addWorkflowsToExistingJob(workflowRequestId, job, sourcePage,
                        l10nProfile, workflowTemplates);
            }
            // refresh the job after adding workflows.
            job = ServerProxy.getJobHandler().getJobById(job.getId());

            // calculateWorkflowStatistics() commits statistics to DB
            calculateNewWorkflowStatistics(job);

            updateJobState(workflowRequestId, job);
        }
        catch (Exception wfme)
        {
            c_logger.error("cannot add workflows", wfme);

            try
            {
                sendEmail(job, m_map.get("newworkflows"),
                        MailerConstants.WF_IMPORT_FAILURE_SUBJECT,
                        IMPORT_FAILURE);
            }
            catch (Exception e)
            {
                c_logger.error("Unable to send email", e);
            }
        }
        finally
        {
            HibernateUtil.closeSession();
        }
    }

    private void addWorkflowsToExistingJob(long p_workflowRequestId, Job p_job,
            SourcePage p_sourcePage, L10nProfile p_l10nProfile,
            Collection p_workflowTemplates) throws WorkflowManagerException
    {
        HashMap pages = new HashMap();
        pages.put(p_sourcePage.getGlobalSightLocale().getIdAsLong(),
                p_sourcePage);
        ArrayList targetLocales = getTargetLocales(p_workflowTemplates);

        switch (p_sourcePage.getRequest().getType())
        {
            case Request.EXTRACTED_LOCALIZATION_REQUEST:

                WorkflowManagerException exception = null;
                Collection extractedTargetPages = null;

                try
                {
                    ExactMatchedSegments esm = leveragePageWithNewTargetLocales(
                            p_sourcePage, p_l10nProfile, p_workflowTemplates);
                    TermLeverageResult termMatches = leverageTermsWithNewTargetLocales(
                            p_sourcePage, p_l10nProfile, p_workflowTemplates);

                    extractedTargetPages = createExtractedTargetPages(p_job,
                            p_sourcePage, p_l10nProfile, targetLocales,
                            termMatches, esm);

                    for (Iterator it2 = extractedTargetPages.iterator(); it2
                            .hasNext();)
                    {
                        TargetPage tp = (TargetPage) it2.next();
                        pages.put(tp.getGlobalSightLocale().getIdAsLong(), tp);
                    }

                    if (extractedTargetPages.size() > 0)
                    {
                        ServerProxy.getPageEventObserver()
                                .notifyImportSuccessNewTargetPagesEvent(
                                        extractedTargetPages);
                    }
                    // no successful target pages but the request isn't
                    // marked as a failure yet so set the exception in it
                    else
                    {
                        c_logger.info("Import failed - updating the state.");

                        // set the entire request to a failure since all
                        // target pages failed

                        setExceptionInRequest(
                                p_workflowRequestId,
                                new WorkflowManagerException(
                                        WorkflowManagerException.MSG_FAILED_IMPORT_ALL_TARGETS_ACTIVE,
                                        null, null));
                        ServerProxy.getPageEventObserver()
                                .notifyImportFailEvent(extractedTargetPages);
                    }
                }
                catch (WorkflowManagerException wfme)
                {
                    exception = wfme;
                }
                catch (GeneralException ge)
                {
                    String[] args =
                    { Long.toString(p_sourcePage.getId()),
                            p_sourcePage.getExternalPageId() };

                    exception = new WorkflowManagerException(
                            WorkflowManagerException.MSG_FAILED_TO_IMPORT_PAGE,
                            args, ge);
                }
                catch (Throwable ex)
                {
                    String[] args =
                    { Long.toString(p_sourcePage.getId()),
                            p_sourcePage.getExternalPageId() };

                    exception = new WorkflowManagerException(
                            WorkflowManagerException.MSG_FAILED_TO_IMPORT_PAGE,
                            args, new Exception(ex.toString()));
                }

                if (exception != null)
                {
                    c_logger.error(
                            "Import failed for page "
                                    + p_sourcePage.getExternalPageId() + "\n",
                            exception);

                    try
                    {
                        setExceptionInRequest(p_workflowRequestId, exception);

                        ServerProxy.getPageEventObserver()
                                .notifyImportFailEvent(extractedTargetPages);
                    }
                    catch (Throwable ex)
                    {
                        c_logger.error("Cannot mark pages as IMPORT_FAILED.",
                                ex);
                        throw exception;
                    }
                }
                else
                {
                    c_logger.info("Done importing page: "
                            + p_sourcePage.getExternalPageId());
                }

                break;

            case Request.UNEXTRACTED_LOCALIZATION_REQUEST:
                Collection unextractedTargetPages = null;

                try
                {
                    String userId = p_l10nProfile.getProject()
                            .getProjectManagerId();
                    UnextractedFile sourceUf = (UnextractedFile) p_sourcePage
                            .getPrimaryFile();
                    // String contentFileName =
                    // getAbsolutePath() + sourceUf.getStoragePath();
                    String contentFileName = AmbFileStoragePathUtils
                            .getUnextractedParentDir()
                            + File.separator
                            + sourceUf.getStoragePath();

                    unextractedTargetPages = createUnExtractedTargetPages(
                            p_sourcePage, targetLocales);

                    for (Iterator i = unextractedTargetPages.iterator(); i
                            .hasNext();)
                    {
                        TargetPage targetPage = (TargetPage) i.next();
                        UnextractedFile uf = (UnextractedFile) targetPage
                                .getPrimaryFile();

                        // if there is another one
                        if (i.hasNext())
                        {
                            storeFile(contentFileName, uf, userId, false);
                        }
                        else
                        {
                            // this is the last one - specify to delete it
                            storeFile(contentFileName, uf, userId, true);
                        }

                        pages.put(targetPage.getGlobalSightLocale()
                                .getIdAsLong(), targetPage);
                    }
                }
                catch (GeneralException e)
                {
                    c_logger.warn("Import failed for the un-extracted page.");
                    setExceptionInRequest(p_workflowRequestId, e);
                }

                try
                {
                    if (p_sourcePage.getRequest().getType() == Request.UNEXTRACTED_LOCALIZATION_REQUEST)
                    {
                        ServerProxy.getPageEventObserver()
                                .notifyImportSuccessNewTargetPagesEvent(
                                        unextractedTargetPages);
                    }
                    else
                    {
                        c_logger.info("Import failed - updating the state.");

                        ServerProxy.getPageEventObserver()
                                .notifyImportFailEvent(unextractedTargetPages);
                    }
                }
                catch (Exception e)
                {
                    c_logger.info("Failed to update the state of the page.");
                }

                break;

            default:
                // throw a page importing exception - invalid type
        }

        if (m_map.size() == 0)
        {
            List<Workflow> workflows = addTargetPageToExistingJob(pages, p_job,
                    p_workflowTemplates);
            m_map.put("newworkflows", workflows);
        }
        else if (m_map.size() > 0)
        {
            List<Workflow> workflows = m_map.get("newworkflows");
            addTargetPagesToWorkflows(pages, workflows, p_job);
        }
    }

    private void addTargetPagesToWorkflows(HashMap p_pages, List p_workflows,
            Job p_job) throws WorkflowManagerException
    {
        SourcePage sourcePage = null;

        Session session = null;
        Transaction transaction = null;

        try
        {
            session = HibernateUtil.getSession();
            transaction = session.beginTransaction();
            HashMap targetPages = new HashMap(p_pages);

            // get the source page from the map
            sourcePage = (SourcePage) targetPages.remove(p_job.getL10nProfile()
                    .getSourceLocale().getIdAsLong());
            Vector toplinkTargetPages = PagePersistenceAccessor
                    .getTargetPages(sourcePage.getId());
            HashMap mapTargetPages = convertVectorIntoMap(toplinkTargetPages);
            Iterator it = p_workflows.iterator();

            while (it.hasNext())
            {
                Workflow workflow = (Workflow) it.next();

                Workflow wfClone = (WorkflowImpl) session.load(
                        WorkflowImpl.class, workflow.getIdAsLong());
                TargetPage targetPage = (TargetPage) targetPages.get(workflow
                        .getTargetLocale().getIdAsLong());
                TargetPage toplinkTargetPage = (TargetPage) mapTargetPages
                        .get(targetPage.getIdAsLong());
                TargetPage targetPageClone = (TargetPage) session.load(
                        TargetPage.class, toplinkTargetPage.getIdAsLong());

                wfClone.addTargetPage(targetPageClone);
                session.update(wfClone);
            }

            transaction.commit();
        }
        catch (Exception pe)
        {
            if (transaction != null)
            {
                transaction.rollback();
            }
            c_logger.error("Unable to add workflows to given job" + pe);
            String args[] = new String[1];
            args[0] = Long.toString(sourcePage.getId());

            throw new WorkflowManagerException(
                    WorkflowManagerException.MSG_FAILED_TO_ADD_WORKFLOW, args,
                    pe);
        }
        finally
        {
            if (session != null)
            {
                // session.close();
            }
        }
    }

    private HashMap convertVectorIntoMap(Vector p_toplinkTargetPages)
    {
        Iterator it = p_toplinkTargetPages.iterator();
        HashMap map = new HashMap();

        while (it.hasNext())
        {
            TargetPage targetPage = (TargetPage) it.next();
            map.put(targetPage.getIdAsLong(), targetPage);
        }

        return map;
    }

    private void updateJobState(long p_workflowRequestId, Job p_job)
            throws WorkflowManagerException
    {
        WorkflowRequest workflowRequest = null;

        try
        {
            workflowRequest = ServerProxy.getRequestHandler()
                    .findWorkflowRequest(p_workflowRequestId);
        }
        catch (Exception e)
        {
            String args[] = new String[1];
            args[1] = new Long(p_workflowRequestId).toString();
            c_logger.error("Unable to retrieve workflow request id" + e);
            throw new WorkflowManagerException(
                    WorkflowManagerException.MSG_FAILED_TO_RETRIEVE_WORKFLOW_REQUEST,
                    args, e);
        }

        if (workflowRequest.getType() == WorkflowRequest.WORKFLOW_REQUEST_FAILURE)
        {
            Session session = null;
            Transaction transaction = null;

            try
            {
                session = HibernateUtil.getSession();
                transaction = session.beginTransaction();

                Iterator<Workflow> it = (m_map.get("newworkflows")).iterator();

                while (it.hasNext())
                {
                    Workflow workflow = (Workflow) it.next();
                    Workflow wfClone;
                    if (workflow.getId() < 1)
                    {
                        session.save(workflow);
                        wfClone = workflow;
                    }
                    else
                    {
                        wfClone = (Workflow) session.get(WorkflowImpl.class,
                                workflow.getIdAsLong());
                    }

                    wfClone.setState(Workflow.IMPORT_FAILED);
                    Vector v = wfClone.getAllTargetPages();
                    Iterator it2 = v.iterator();

                    while (it2.hasNext())
                    {
                        TargetPage tp = (TargetPage) it2.next();
                        if (!tp.getPageState().equals(PageState.IMPORT_FAIL))
                        {
                            tp.setPageState(PageState.IMPORT_FAIL);
                        }
                    }
                    session.update(wfClone);
                }

                transaction.commit();

                sendEmail(p_job, m_map.get("newworkflows"),
                        MailerConstants.WF_IMPORT_FAILURE_SUBJECT,
                        IMPORT_FAILURE);
            }
            catch (Exception e)
            {
                transaction.rollback();
                String args[] = new String[1];
                args[0] = new Long(p_job.getId()).toString();
                throw new WorkflowManagerException(
                        WorkflowManagerException.MSG_FAILED_TO_UPDATE_WORKFLOWS,
                        args, e);
            }
            finally
            {
                if (session != null)
                {
                    // session.close();
                }
            }
        }
        else
        {
            Session session = null;
            Transaction transaction = null;

            try
            {
                boolean isAutoDispatch = p_job.getL10nProfile()
                        .dispatchIsAutomatic();
                Job job = ServerProxy.getJobHandler().getJobById(p_job.getId());
                Iterator<Workflow> it = (m_map.get("newworkflows")).iterator();

                if (isAutoDispatch && job.hasSetCostCenter())
                {
                    while (it.hasNext())
                    {
                        Workflow workflow = (Workflow) it.next();

                        if (workflow.getState().equals(Workflow.PENDING))
                        {
                            ServerProxy.getWorkflowManager().dispatch(workflow);
                        }
                    }
                }
                else
                {
                    session = HibernateUtil.getSession();
                    transaction = session.beginTransaction();

                    while (it.hasNext())
                    {
                        Workflow workflow = (Workflow) it.next();
                        if (workflow.getId() < 1)
                        {
                            session.save(workflow);
                        }
                        else
                        {
                            workflow = (Workflow) session.get(
                                    WorkflowImpl.class, workflow.getIdAsLong());
                        }

                        if (workflow.getState().equals(Workflow.PENDING))
                        {
                            workflow.setState(Workflow.READY_TO_BE_DISPATCHED);
                        }
                        // calculate estimated completion date for a workflow of
                        // READY_TO_BE_DISPATCHED state
                        calculateEstimatedCompletionDate(workflow);
                    }

                    transaction.commit();

                    sendEmail(job, m_map.get("newworkflows"),
                            MailerConstants.DISPATCH_SUBJECT,
                            MailerConstants.DISPATCH_MESSAGE);
                }
            }
            catch (Exception e)
            {
                transaction.rollback();
                c_logger.info("The error in dispatching the workflow is " + e);
                String args[] = new String[1];
                args[0] = new Long(p_job.getId()).toString();
                throw new WorkflowManagerException(
                        WorkflowManagerException.MSG_FAILED_TO_DISPATCH_WORKFLOW,
                        args, e);
            }
            finally
            {
                if (session != null)
                {
                    // session.close();
                }
            }
        }
    }

    private void calculateEstimatedCompletionDate(Workflow p_workflowClone)
            throws Exception
    {
        List wfTaskInfos = ServerProxy.getWorkflowServer()
                .timeDurationsInDefaultPath(null, p_workflowClone.getId(), -1);
        FluxCalendar defaultCalendar = ServerProxy.getCalendarManager()
                .findDefaultCalendar(
                        String.valueOf(p_workflowClone.getCompanyId()));

        Hashtable tasks = p_workflowClone.getTasks();
        long translateDuration = 0l;
        long workflowDuration = 0l;

        for (int i = 0; i < wfTaskInfos.size(); i++)
        {
            WfTaskInfo wfTaskInfo = (WfTaskInfo) wfTaskInfos.get(i);
            TaskImpl task = (TaskImpl) tasks.get(new Long(wfTaskInfo.getId()));
            if (task == null)
                continue;

            workflowDuration += wfTaskInfo.getTotalDuration();

            Activity act = ServerProxy.getJobHandler().getActivity(
                    task.getTaskName());

            if (Activity.isTranslateActivity(act.getType()))
            {
                translateDuration = workflowDuration;
            }
        }

        Date date = new Date();
        p_workflowClone.setEstimatedTranslateCompletionDate(ServerProxy
                .getEventScheduler().determineDate(date, defaultCalendar,
                        translateDuration));
        p_workflowClone.setEstimatedCompletionDate(ServerProxy
                .getEventScheduler().determineDate(date, defaultCalendar,
                        workflowDuration));

        // p_workflowClone.updateTranslationCompletedDates();
    }

    private ExactMatchedSegments leveragePageWithNewTargetLocales(
            SourcePage p_sourcePage, L10nProfile p_l10nProfile,
            Collection p_workflowTemplates) throws WorkflowManagerException
    {
        ExactMatchedSegments exactMatchedSegments = null;

        if (p_l10nProfile.getTmChoice() != L10nProfile.NO_TM)
        {
            LeveragingLocales leveragingLocales = new LeveragingLocales();
            Iterator it = p_workflowTemplates.iterator();

            while (it.hasNext())
            {
                WorkflowTemplateInfo wfTempInfo = (WorkflowTemplateInfo) it
                        .next();
                Set<GlobalSightLocale> c = wfTempInfo.getLeveragingLocales();
                leveragingLocales.setLeveragingLocale(
                        wfTempInfo.getTargetLocale(), c);
            }

            if (leveragingLocales.size() > 0)
            {
                long jobId = p_sourcePage.getJobId();
                TranslationMemoryProfile tmProfile = p_l10nProfile
                        .getTranslationMemoryProfile();

                LeverageOptions leverageOptions = new LeverageOptions(
                        tmProfile, leveragingLocales);

                TmCoreManager tmCoreManager = null;

                try
                {
                    tmCoreManager = LingServerProxy.getTmCoreManager();

                    // create LeverageDataCenter
                    LeverageDataCenter leverageDataCenter = tmCoreManager
                            .createLeverageDataCenterForPage(p_sourcePage,
                                    leverageOptions, jobId);

                    // leverage
                    tmCoreManager
                            .leveragePage(p_sourcePage, leverageDataCenter);

                    // save the matches results to leverage_match table
                    Connection conn = null;
                    try
                    {
                        conn = DbUtil.getConnection();
                        LingServerProxy.getLeverageMatchLingManager()
                                .saveLeverageResults(conn, p_sourcePage,
                                        leverageDataCenter);
                    }
                    finally
                    {
                        DbUtil.silentReturnConnection(conn);
                    }

                    // retrieve exact match results
                    exactMatchedSegments = leverageDataCenter
                            .getExactMatchedSegments(jobId);
                }
                catch (Exception e)
                {
                    c_logger.error("Exception when leveraging.", e);

                    String[] args = new String[1];
                    args[0] = Long.toString(p_sourcePage.getId());
                    throw new WorkflowManagerException(
                            WorkflowManagerException.MSG_FAILED_TO_LEVERAGE_SOURCE_PAGE,
                            args, e);
                }
                catch (Throwable e)
                {
                    c_logger.error("Unexpected exception when leveraging.", e);

                    String[] args = new String[1];
                    args[0] = Long.toString(p_sourcePage.getId());
                    throw new WorkflowManagerException(
                            WorkflowManagerException.MSG_FAILED_TO_LEVERAGE_SOURCE_PAGE,
                            args, new Exception(e.toString()));
                }
            }
        }

        c_logger.info("Finished leveraging successfuly");

        return exactMatchedSegments;
    }

    /**
     * Leverages terms for all translatable segments in the specified source
     * page and persists the result.
     * 
     * @return collection of TermLeverageMatch objects, grouped by source tuv
     *         id, or null when the database does not exist or an error happens.
     */
    private TermLeverageResult leverageTermsWithNewTargetLocales(
            SourcePage p_sourcePage, L10nProfile p_l10nProfile,
            Collection p_workflowTemplates)
    {
        TermLeverageResult result = null;

        String project = p_l10nProfile.getProject().getName();
        String termbaseName = p_l10nProfile.getProject().getTermbaseName();

        try
        {
            ArrayList<Tuv> sourceTuvs = SegmentTuvUtil.getSourceTuvs(p_sourcePage);

            if (sourceTuvs.size() > 0 && termbaseName != null
                    && termbaseName.length() > 0)
            {
                TermLeverageOptions options = getTermLeverageOptions(
                        p_l10nProfile, p_workflowTemplates, termbaseName);

                if (c_logger.isDebugEnabled())
                {
                    c_logger.debug("Termbase leveraging options = " + options);
                }

                if (options == null)
                {
                    c_logger.warn("Project " + project
                            + " refers to unknown termbase `" + termbaseName
                            + "'. Term leverage skipped.");
                }
                else if (options.getAllTargetPageLocales().size() == 0
                        || options.getSourcePageLangNames().size() == 0)
                {
                    c_logger.warn("No specified locale found in termbase. "
                            + "Term leverage skipped.");
                }
                else
                {
                    result = ServerProxy
                            .getTermLeverageManager()
                            .leverageTerms(sourceTuvs, options,
                                    String.valueOf(p_sourcePage.getCompanyId()));
                }

            }
        }
        catch (Exception e)
        {
            c_logger.warn("Exception when leveraging terms, ignoring.", e);
        }

        return result;
    }

    /**
     * Populates a term leverage options object.
     */
    private TermLeverageOptions getTermLeverageOptions(
            L10nProfile p_l10nProfile, Collection p_workflowTemplates,
            String p_termbaseName) throws GeneralException
    {
        TermLeverageOptions options = null;

        Locale sourceLocale = p_l10nProfile.getSourceLocale().getLocale();
        String companyId = String.valueOf(p_l10nProfile.getCompanyId());

        try
        {
            ITermbaseManager manager = ServerProxy.getTermbaseManager();

            long termbaseId = manager.getTermbaseId(p_termbaseName, companyId);

            // If termbase does not exist, return null options.
            if (termbaseId == -1)
            {
                return null;
            }

            options = new TermLeverageOptions();
            options.addTermBase(p_termbaseName);
            options.setSaveToDatabase(true);

            // fuzzy threshold set by object constructor - use defaults.
            // options.setFuzzyThreshold(50);

            ITermbase termbase = manager.connect(p_termbaseName,
                    ITermbase.SYSTEM_USER, "", companyId);

            // add source locale and lang names
            options.setSourcePageLocale(sourceLocale);

            List sourceLangNames = termbase.getLanguagesByLocale(sourceLocale
                    .toString());
            for (Iterator it = sourceLangNames.iterator(); it.hasNext();)
            {
                options.addSourcePageLocale2LangName((String) it.next());
            }

            // add target locales and lang names
            for (Iterator tli = p_workflowTemplates.iterator(); tli.hasNext();)
            {
                WorkflowTemplateInfo wti = (WorkflowTemplateInfo) tli.next();
                Locale targetLocale = ((GlobalSightLocale) wti
                        .getTargetLocale()).getLocale();
                List targetLangNames = termbase
                        .getLanguagesByLocale(targetLocale.toString());

                for (Iterator it = targetLangNames.iterator(); it.hasNext();)
                {
                    String langName = (String) it.next();
                    options.addTargetPageLocale2LangName(targetLocale, langName);
                    options.addLangName2Locale(langName, targetLocale);
                }
            }
        }
        catch (Exception e)
        {
            throw new GeneralException(e);
        }

        return options;
    }

    /**
     * Creates the target pages associated with the source page and the target
     * locales specified in the L10nProfile as part of the request.
     */
    private Collection createExtractedTargetPages(Job p_job,
            SourcePage p_sourcePage, L10nProfile p_l10nProfile,
            Collection p_targetLocales, TermLeverageResult p_termMatches,
            ExactMatchedSegments p_exactMatchedSegments)
            throws WorkflowManagerException
    {
        Collection targetPages = new ArrayList();
        boolean useLeveragedSegments = false;
        boolean useLeveragedTerms = false;

        try
        {
            if (p_l10nProfile.getTmChoice() != L10nProfile.NO_TM)
            {
                useLeveragedSegments = true;
            }

            // hook to set the auto replace term behavior
            if (/* choose the condition here */true)
            {
                if (s_autoReplaceTerms == null)
                {
                    s_autoReplaceTerms = getLeverageOptions();
                }

                useLeveragedTerms = s_autoReplaceTerms.booleanValue();
            }

            if (p_targetLocales.size() > 0)
            {

                TargetPagePersistence tpPersistence = new TargetPageWorkflowAdditionPersistence();

                targetPages = tpPersistence.persistObjectsWithExtractedFile(
                        p_sourcePage, p_targetLocales, p_termMatches,
                        useLeveragedSegments, useLeveragedTerms,
                        p_exactMatchedSegments);
            }
        }
        catch (Exception e)
        {
            c_logger.error("Exception occurred when trying to "
                    + "create target pages.", e);
            String[] args = new String[1];
            args[0] = Long.toString(p_sourcePage.getId());
            throw new WorkflowManagerException(
                    WorkflowManagerException.MSG_FAIL_TO_CREATE_TARGET_PAGE,
                    args, e);
        }

        return targetPages;
    }

    private Collection createUnExtractedTargetPages(SourcePage p_sourcePage,
            Collection p_targetLocales) throws WorkflowManagerException
    {
        Collection targetPages = null;

        try
        {

            TargetPagePersistence tpPersistence = new TargetPageWorkflowAdditionPersistence();

            targetPages = tpPersistence.persistObjectsWithUnextractedFile(
                    p_sourcePage, p_targetLocales);
        }
        catch (Exception pe)
        {
            c_logger.error("Exception occurred when trying to "
                    + "create target pages.", pe);

            String[] args = new String[1];
            args[0] = Long.toString(p_sourcePage.getId());
            throw new WorkflowManagerException(
                    WorkflowManagerException.MSG_FAIL_TO_CREATE_TARGET_PAGE,
                    args, pe);
        }

        return targetPages;
    }

    private List<Workflow> addTargetPageToExistingJob(HashMap p_pages,
            Job p_job, Collection p_workflowTemplates)
            throws WorkflowManagerException
    {
        AddWorkflowPersistenceHandler awph = new AddWorkflowPersistenceHandler();

        return awph.createWorkflows(p_pages, p_job, p_workflowTemplates);
    }

    private Boolean getLeverageOptions()
    {
        Boolean result = Boolean.FALSE;

        try
        {
            SystemConfiguration sc = SystemConfiguration.getInstance();
            boolean autoReplaceTerms = sc
                    .getBooleanParameter(SystemConfigParamNames.AUTO_REPLACE_TERMS);

            if (autoReplaceTerms == true)
            {
                result = Boolean.TRUE;
            }
        }
        catch (ConfigException ce)
        {
            // not specified - default is false
        }
        catch (GeneralException ge)
        {
            c_logger.error("A general exception was thrown when trying "
                    + "to read the system configuration file "
                    + "for system-wide leverage options.");
        }

        return result;
    }

    /**
     * Wraps the code for setting an exception in a request and catching the
     * appropriate exception.
     */
    protected void setExceptionInRequest(long p_workflowRequestId,
            GeneralException p_exception) throws WorkflowManagerException
    {
        try
        {
            WorkflowRequest workflowRequest = ServerProxy.getRequestHandler()
                    .findWorkflowRequest(p_workflowRequestId);

            ServerProxy.getRequestHandler().setExceptionInWorkflowRequest(
                    workflowRequest, p_exception);
        }
        catch (Exception e)
        {
            String[] args = new String[1];
            args[0] = Long.toString(p_workflowRequestId);
            throw new WorkflowManagerException(
                    WorkflowManagerException.MSG_FAILED_TO_SET_EXCEPTION_IN_REQUEST,
                    args, e);
        }
    }

    /**
     * Store the file out to the application's storage directory.
     */
    private void storeFile(String p_fileName, UnextractedFile p_uf,
            String p_modifierId, boolean p_removeOriginal)
            throws WorkflowManagerException
    {
        try
        {
            UnextractedFile uf = ServerProxy.getNativeFileManager()
                    .copyFileToStorage(p_fileName, p_uf, p_removeOriginal);

            uf.setLastModifiedBy(p_modifierId);

            c_logger.debug("File " + p_fileName + " copied to storage.");
        }
        catch (Exception e)
        {
            c_logger.error("Failed to copy file " + p_fileName
                    + " to the storage location");
            String args[] =
            { p_fileName };
            throw new WorkflowManagerException(
                    WorkflowManagerException.MSG_FAILED_TO_COPY_FILE_TO_STORAGE,
                    args, e);
        }
    }

    private ArrayList getTargetLocales(Collection p_workflowTemplates)
    {
        ArrayList result = new ArrayList();
        Iterator it = p_workflowTemplates.iterator();

        while (it.hasNext())
        {
            GlobalSightLocale targetLocale = ((WorkflowTemplateInfo) it.next())
                    .getTargetLocale();

            result.add(targetLocale);
        }

        return result;
    }

    private void sendEmail(Job p_job, List p_workflows, String p_subject,
            String p_message) throws WorkflowManagerException
    {
        if (!m_systemNotificationEnabled)
        {
            return;
        }

        L10nProfile l10nProfile = p_job.getL10nProfile();
        long projectId = l10nProfile.getProjectId();
        String capLoginUrl = null;
        User user = null;

        try
        {
            Project project = ServerProxy.getProjectHandler().getProjectById(
                    projectId);
            SystemConfiguration config = SystemConfiguration.getInstance();
            capLoginUrl = config
                    .getStringParameter(SystemConfigParamNames.CAP_LOGIN_URL);

            String[] msgArgs = new String[6];
            msgArgs[0] = p_job.getJobName();
            msgArgs[1] = Long.toString(p_job.getDuration());
            msgArgs[3] = Integer.toString(p_job.getWordCount());
            msgArgs[4] = capLoginUrl;
            msgArgs[5] = Integer.toString(p_job.getPriority());

            // first go thru all wftInfos and send an email to WFM (if any)
            boolean shouldNotifyPm = false;
            if (p_workflows != null && p_workflows.size() > 0)
            {
                Iterator it = p_workflows.iterator();
                while (it.hasNext())
                {

                    Workflow workflow = (Workflow) it.next();
                    GlobalSightLocale targetLocale = workflow.getTargetLocale();
                    WorkflowTemplateInfo wfti = l10nProfile
                            .getWorkflowTemplateInfo(targetLocale);

                    if (!shouldNotifyPm && wfti.notifyProjectManager())
                    {
                        shouldNotifyPm = true;
                    }

                    List userIds = wfti.getWorkflowManagerIds();
                    if (userIds != null)
                    {
                        for (Iterator uii = userIds.iterator(); uii.hasNext();)
                        {
                            String userId = (String) uii.next();
                            sendMailFromAdmin(userId, p_job, msgArgs,
                                    p_subject, p_message);
                        }
                    }
                }
            }

            // if at least one of the wfInfos had the pm notify flag on, notify
            // PM.
            if (shouldNotifyPm)
            {
                sendMailFromAdmin(project.getProjectManagerId(), p_job,
                        msgArgs, p_subject, p_message);
            }
        }
        catch (Exception e)
        {
            c_logger.error("Unable to send email after a dispatch for Job id: "
                    + p_job.getId()
                    + " capLoginUrl="
                    + (capLoginUrl != null ? capLoginUrl : "null")
                    + " user="
                    + (user != null ? ((UserImpl) user).toDebugString()
                            : "null") + " p_subject="
                    + (p_subject != null ? p_subject : "null") + " p_message="
                    + (p_message != null ? p_message : "null"), e);
        }
    }

    // send email from Admin
    private void sendMailFromAdmin(String p_userId, Job p_job,
            String[] p_msgArgs, String p_subject, String p_message)
            throws Exception
    {
        if (!m_systemNotificationEnabled)
        {
            return;
        }

        User user = ServerProxy.getUserManager().getUser(p_userId);
        String userLocale = user.getDefaultUILocale();
        Locale locale = LocaleWrapper.getLocale(userLocale);
        DateFormat dateformat = DateFormat.getDateInstance(DateFormat.MEDIUM,
                locale);
        p_msgArgs[2] = dateformat.format(p_job.getCreateDate());

        ServerProxy.getMailer().sendMailFromAdmin(user, p_msgArgs, p_subject,
                p_message, String.valueOf(p_job.getCompanyId()));
    }

    private void calculateNewWorkflowStatistics(Job p_job) throws Exception
    {
        // Get just the NEW workflows. Must get them from the cache
        // though. The ones in the map are not stored in the cache
        // so must get them from the job itself.
        List<Workflow> newWorkflows = m_map.get("newworkflows");
        List<Workflow> realWorkflows = new ArrayList<Workflow>();
        // the workflows attached to the job in the cache
        Collection<Workflow> jobWorkflows = p_job.getWorkflows();

        // go through all the new workflows that were added
        for (Iterator<Workflow> lr = newWorkflows.iterator(); lr.hasNext();)
        {
            Workflow w = (Workflow) lr.next();
            boolean found = false;
            for (Iterator<Workflow> lr1 = jobWorkflows.iterator(); lr1
                    .hasNext() && !found;)
            {
                Workflow wf = (Workflow) lr1.next();
                if (w.getTargetLocale().equals(wf.getTargetLocale())
                        && !Workflow.CANCELLED.equals(wf.getState()))
                {
                    realWorkflows.add(wf);
                    found = true;
                }
            }
        }

        for (Workflow workflow : realWorkflows)
        {
            StatisticsService.calculateTargetPagesWordCount(workflow, p_job
                    .getL10nProfile().getTranslationMemoryProfile()
                    .getJobExcludeTuTypes());
        }

        // calculate the statistics just for the new workflows
        StatisticsService.calculateWorkflowStatistics(realWorkflows, p_job
                .getL10nProfile().getTranslationMemoryProfile()
                .getJobExcludeTuTypes());
    }
}
