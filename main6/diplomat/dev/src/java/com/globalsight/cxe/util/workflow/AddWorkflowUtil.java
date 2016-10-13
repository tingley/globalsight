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
import java.util.Map;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.globalsight.calendar.FluxCalendar;
import com.globalsight.everest.company.MultiCompanySupportedThread;
import com.globalsight.everest.foundation.L10nProfile;
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.foundation.UserImpl;
import com.globalsight.everest.integration.ling.LingServerProxy;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.page.Page;
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
import com.globalsight.everest.util.system.SystemConfigParamNames;
import com.globalsight.everest.util.system.SystemConfiguration;
import com.globalsight.everest.workflow.Activity;
import com.globalsight.everest.workflow.EventNotificationHelper;
import com.globalsight.everest.workflow.WfTaskInfo;
import com.globalsight.everest.workflowmanager.AddWorkflowPersistenceHandler;
import com.globalsight.everest.workflowmanager.TargetPageWorkflowAdditionPersistence;
import com.globalsight.everest.workflowmanager.Workflow;
import com.globalsight.everest.workflowmanager.WorkflowImpl;
import com.globalsight.everest.workflowmanager.WorkflowManagerException;
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

/**
 * Class {@code AddWorkflowUtil} is used for adding new workflows to existing
 * job without using JMS.
 * 
 * @since GBS-4400
 */
public class AddWorkflowUtil
{
    static private final Logger logger = Logger.getLogger(AddWorkflowUtil.class);

    static private final String IMPORT_FAILURE = "importFailure";

    static private boolean m_autoReplaceTerms = getLeverageOptions();

    private static boolean m_systemNotificationEnabled = EventNotificationHelper
            .systemNotificationEnabled();

    /**
     * Adds workflows to existing job asynchronously with thread instead of JMS.
     */
    static public void addWorkflowsWithThread(Map<String, Object> data)
    {
        AddWorkflowRunnable runnable = new AddWorkflowRunnable(data);
        Thread t = new MultiCompanySupportedThread(runnable);
        t.start();
    }

    /**
     * Adds workflows to existing job synchronously.
     */
    static public void addWorkflows(Map<String, Object> p_data)
    {
        long jobId = (Long) p_data.get("jobId");
        List<Long> workflowIds = (List<Long>) p_data.get("workflowIds");

        Job job = null;
        L10nProfile l10nProfile = null;
        List<Workflow> newWorkflows = new ArrayList<Workflow>();
        List<WorkflowTemplateInfo> workflowTemplates = new ArrayList<WorkflowTemplateInfo>();
        try
        {
            job = ServerProxy.getJobHandler().getJobById(jobId);
            l10nProfile = ServerProxy.getProjectHandler()
                    .getL10nProfile(job.getL10nProfile().getId());

            WorkflowRequest workflowRequest = new WorkflowRequestImpl();
            workflowRequest.setType(WorkflowRequest.ADD_WORKFLOW_REQUEST_TO_EXISTING_JOB);
            for (long workflowId : workflowIds)
            {
                WorkflowTemplateInfo wfTempInfo = ServerProxy.getProjectHandler()
                        .getWorkflowTemplateInfoById(workflowId);
                workflowTemplates.add(wfTempInfo);
            }
            long workflowRequestId = ServerProxy.getRequestHandler()
                    .createWorkflowRequest(workflowRequest, job, workflowTemplates);

            Collection<SourcePage> sourcePages = job.getSourcePages();
            for (SourcePage sourcePage : sourcePages)
            {
                addWorkflowsToExistingJob(workflowRequestId, job, sourcePage, l10nProfile,
                        workflowTemplates, newWorkflows);
            }
            // refresh the job after adding workflows.
            job = ServerProxy.getJobHandler().getJobById(job.getId());
            calculateNewWorkflowStatistics(job, newWorkflows);
            updateJobState(workflowRequestId, job, newWorkflows);
        }
        catch (Exception e)
        {
            logger.error("Failed to add workflows.", e);

            try
            {
                sendEmail(job, newWorkflows, MailerConstants.WF_IMPORT_FAILURE_SUBJECT,
                        IMPORT_FAILURE);
            }
            catch (Exception ex)
            {
                logger.error("Unable to send email.", ex);
            }
        }
        finally
        {
            HibernateUtil.closeSession();
        }
    }

    private static void updateJobState(long p_workflowRequestId, Job p_job,
            List<Workflow> p_newlyAddedWorkflows)
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
            logger.error("Unable to retrieve workflow request id.", e);
            throw new WorkflowManagerException(
                    WorkflowManagerException.MSG_FAILED_TO_RETRIEVE_WORKFLOW_REQUEST, args, e);
        }
        if (WorkflowRequest.WORKFLOW_REQUEST_FAILURE == workflowRequest.getType())
        {
            Session session = null;
            Transaction transaction = null;
            try
            {
                session = HibernateUtil.getSession();
                transaction = session.beginTransaction();
                for (Workflow workflow : p_newlyAddedWorkflows)
                {
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
                    List<TargetPage> allTargetPages = wfClone.getAllTargetPages();
                    for (TargetPage tp : allTargetPages)
                    {
                        if (!PageState.IMPORT_FAIL.equals(tp.getPageState()))
                        {
                            tp.setPageState(PageState.IMPORT_FAIL);
                        }
                    }
                    session.update(wfClone);
                }
                transaction.commit();

                sendEmail(p_job, p_newlyAddedWorkflows, MailerConstants.WF_IMPORT_FAILURE_SUBJECT,
                        IMPORT_FAILURE);
            }
            catch (Exception e)
            {
                if (transaction != null)
                {
                    transaction.rollback();
                }
                String args[] = new String[1];
                args[0] = new Long(p_job.getId()).toString();
                throw new WorkflowManagerException(
                        WorkflowManagerException.MSG_FAILED_TO_UPDATE_WORKFLOWS, args, e);
            }
        }
        else
        {
            Session session = null;
            Transaction transaction = null;
            try
            {
                boolean isAutoDispatch = p_job.getL10nProfile().dispatchIsAutomatic();
                Job job = ServerProxy.getJobHandler().getJobById(p_job.getId());
                if (isAutoDispatch && job.hasSetCostCenter())
                {
                    for (Workflow workflow : p_newlyAddedWorkflows)
                    {
                        if (Workflow.PENDING.equals(workflow.getState()))
                        {
                            ServerProxy.getWorkflowManager().dispatch(workflow);
                        }
                    }
                }
                else
                {
                    session = HibernateUtil.getSession();
                    transaction = session.beginTransaction();
                    for (Workflow workflow : p_newlyAddedWorkflows)
                    {
                        if (workflow.getId() < 1)
                        {
                            session.save(workflow);
                        }
                        else
                        {
                            workflow = (Workflow) session.get(WorkflowImpl.class,
                                    workflow.getIdAsLong());
                        }

                        if (Workflow.PENDING.equals(workflow.getState()))
                        {
                            workflow.setState(Workflow.READY_TO_BE_DISPATCHED);
                        }
                        calculateEstimatedCompletionDate(workflow);
                    }

                    transaction.commit();

                    sendEmail(job, p_newlyAddedWorkflows, MailerConstants.DISPATCH_SUBJECT,
                            MailerConstants.DISPATCH_MESSAGE);
                }
            }
            catch (Exception e)
            {
                if (transaction != null)
                {
                    transaction.rollback();
                }
                String args[] = new String[1];
                args[0] = new Long(p_job.getId()).toString();
                throw new WorkflowManagerException(
                        WorkflowManagerException.MSG_FAILED_TO_DISPATCH_WORKFLOW, args, e);
            }
        }
    }

    private static void calculateEstimatedCompletionDate(Workflow p_workflowClone) throws Exception
    {
        List wfTaskInfos = ServerProxy.getWorkflowServer().timeDurationsInDefaultPath(null,
                p_workflowClone.getId(), -1);
        FluxCalendar defaultCalendar = ServerProxy.getCalendarManager()
                .findDefaultCalendar(String.valueOf(p_workflowClone.getCompanyId()));

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

            Activity act = ServerProxy.getJobHandler().getActivity(task.getTaskName());

            if (Activity.isTranslateActivity(act.getType()))
            {
                translateDuration = workflowDuration;
            }
        }
        Date date = new Date();
        p_workflowClone.setEstimatedTranslateCompletionDate(ServerProxy.getEventScheduler()
                .determineDate(date, defaultCalendar, translateDuration));
        p_workflowClone.setEstimatedCompletionDate(ServerProxy.getEventScheduler()
                .determineDate(date, defaultCalendar, workflowDuration));
    }

    /**
     * Calculates the statistics on the newly added workflows.
     */
    private static void calculateNewWorkflowStatistics(Job p_job,
            List<Workflow> p_newlyAddedWorkflows)
    {
        List<Workflow> realWorkflows = new ArrayList<Workflow>();
        // the workflows attached to the job in the cache
        Collection<Workflow> jobWorkflows = p_job.getWorkflows();

        for (Workflow newWf : p_newlyAddedWorkflows)
        {
            boolean found = false;
            for (Workflow jobWf : jobWorkflows)
            {
                if (newWf.getTargetLocale().equals(jobWf.getTargetLocale())
                        && !Workflow.CANCELLED.equals(jobWf.getState()))
                {
                    realWorkflows.add(jobWf);
                    found = true;
                }
            }
        }
        for (Workflow workflow : realWorkflows)
        {
            StatisticsService.calculateTargetPagesWordCount(workflow,
                    p_job.getL10nProfile().getTranslationMemoryProfile().getJobExcludeTuTypes());
        }
        // just calculate the statistics on the newly added workflows
        StatisticsService.calculateWorkflowStatistics(realWorkflows,
                p_job.getL10nProfile().getTranslationMemoryProfile().getJobExcludeTuTypes());
    }

    /**
     * Adds new workflows to existing job.
     */
    private static void addWorkflowsToExistingJob(long p_workflowRequestId, Job p_job,
            SourcePage p_sourcePage, L10nProfile p_l10nProfile,
            List<WorkflowTemplateInfo> p_workflowTemplates, List<Workflow> p_newWorkflows)
    {
        Map<Long, Page> pages = new HashMap<Long, Page>();
        pages.put(p_sourcePage.getGlobalSightLocale().getId(), p_sourcePage);

        List<GlobalSightLocale> targetLocales = getTargetLocales(p_workflowTemplates);
        switch (p_sourcePage.getRequest().getType())
        {
            case Request.EXTRACTED_LOCALIZATION_REQUEST:

                WorkflowManagerException exception = null;
                Collection<TargetPage> extractedTargetPages = null;
                try
                {
                    ExactMatchedSegments esm = leveragePageWithNewTargetLocales(p_sourcePage,
                            p_l10nProfile, p_workflowTemplates);
                    TermLeverageResult termMatches = leverageTermsWithNewTargetLocales(p_sourcePage,
                            p_l10nProfile, p_workflowTemplates);

                    extractedTargetPages = createExtractedTargetPages(p_job, p_sourcePage,
                            p_l10nProfile, targetLocales, termMatches, esm);

                    for (TargetPage tp : extractedTargetPages)
                    {
                        pages.put(tp.getGlobalSightLocale().getId(), tp);
                    }

                    if (extractedTargetPages.size() > 0)
                    {
                        ServerProxy.getPageEventObserver()
                                .notifyImportSuccessNewTargetPagesEvent(extractedTargetPages);
                    }
                    else
                    {
                        logger.warn("Failed to create target pages for the new target locales.");
                        setExceptionInRequest(p_workflowRequestId,
                                new WorkflowManagerException(
                                        WorkflowManagerException.MSG_FAILED_IMPORT_ALL_TARGETS_ACTIVE,
                                        null, null));
                    }
                }
                catch (Exception e)
                {
                    String[] args =
                    { Long.toString(p_sourcePage.getId()), p_sourcePage.getExternalPageId() };
                    exception = new WorkflowManagerException(
                            WorkflowManagerException.MSG_FAILED_TO_IMPORT_PAGE, args,
                            new Exception(e.toString()));
                }

                if (exception != null)
                {
                    logger.error(
                            "Import failed for page " + p_sourcePage.getExternalPageId() + "\n",
                            exception);
                    try
                    {
                        setExceptionInRequest(p_workflowRequestId, exception);
                        if (extractedTargetPages != null && extractedTargetPages.size() > 0)
                        {
                            ServerProxy.getPageEventObserver()
                                    .notifyImportFailEvent(extractedTargetPages);
                        }
                    }
                    catch (Throwable ex)
                    {
                        logger.error("Cannot mark pages as IMPORT_FAILED.", ex);
                        throw exception;
                    }
                }
                else
                {
                    logger.info("Done importing page: " + p_sourcePage.getExternalPageId());
                }

                break;

            case Request.UNEXTRACTED_LOCALIZATION_REQUEST:
                Collection<TargetPage> unextractedTargetPages = null;
                try
                {
                    String userId = p_l10nProfile.getProject().getProjectManagerId();
                    UnextractedFile sourceUf = (UnextractedFile) p_sourcePage.getPrimaryFile();
                    String contentFileName = AmbFileStoragePathUtils.getUnextractedParentDir()
                            + File.separator + sourceUf.getStoragePath();

                    unextractedTargetPages = createUnExtractedTargetPages(p_sourcePage,
                            targetLocales);
                    for (TargetPage targetPage : unextractedTargetPages)
                    {
                        UnextractedFile uf = (UnextractedFile) targetPage.getPrimaryFile();
                        storeFile(contentFileName, uf, userId);

                        pages.put(targetPage.getGlobalSightLocale().getIdAsLong(), targetPage);
                    }
                }
                catch (GeneralException e)
                {
                    logger.warn("Import failed for the un-extracted page.");
                    setExceptionInRequest(p_workflowRequestId, e);
                }
                try
                {
                    if (p_sourcePage.getRequest()
                            .getType() == Request.UNEXTRACTED_LOCALIZATION_REQUEST)
                    {
                        ServerProxy.getPageEventObserver()
                                .notifyImportSuccessNewTargetPagesEvent(unextractedTargetPages);
                    }
                    else
                    {
                        logger.info("Import failed - updating the state.");

                        if (unextractedTargetPages != null && unextractedTargetPages.size() > 0)
                        {
                            ServerProxy.getPageEventObserver()
                                    .notifyImportFailEvent(unextractedTargetPages);
                        }
                    }
                }
                catch (Exception e)
                {
                    logger.error("Failed to update the state of the page.", e);
                }

                break;

            default:
        }

        if (p_newWorkflows.size() == 0)
        {
            List<Workflow> workflows = addTargetPageToExistingJob(pages, p_job,
                    p_workflowTemplates);
            p_newWorkflows.addAll(workflows);
        }
        else
        {
            addTargetPagesToWorkflows(pages, p_newWorkflows, p_job);
        }
    }

    private static List<Workflow> addTargetPageToExistingJob(Map<Long, Page> p_pages, Job p_job,
            List<WorkflowTemplateInfo> p_workflowTemplates)
    {
        AddWorkflowPersistenceHandler awph = new AddWorkflowPersistenceHandler();

        return awph.createWorkflows(p_pages, p_job, p_workflowTemplates);
    }

    private static void addTargetPagesToWorkflows(Map<Long, Page> p_pages,
            List<Workflow> p_newWorkflows, Job p_job)
    {
        AddWorkflowPersistenceHandler awph = new AddWorkflowPersistenceHandler();

        awph.addTargetPagesToWorkflows(p_pages, p_newWorkflows, p_job);
    }

    private static void storeFile(String p_fileName, UnextractedFile p_uf, String p_modifierId)
    {
        try
        {
            UnextractedFile uf = ServerProxy.getNativeFileManager().copyFileToStorage(p_fileName,
                    p_uf, false);

            uf.setLastModifiedBy(p_modifierId);
        }
        catch (Exception e)
        {
            logger.error("Failed to copy file " + p_fileName + " to the storage location.", e);
            String args[] =
            { p_fileName };
            throw new WorkflowManagerException(
                    WorkflowManagerException.MSG_FAILED_TO_COPY_FILE_TO_STORAGE, args, e);
        }
    }

    private static void setExceptionInRequest(long p_workflowRequestId,
            GeneralException p_exception)
    {
        try
        {
            WorkflowRequest workflowRequest = ServerProxy.getRequestHandler()
                    .findWorkflowRequest(p_workflowRequestId);
            ServerProxy.getRequestHandler().setExceptionInWorkflowRequest(workflowRequest,
                    p_exception);
        }
        catch (Exception e)
        {
            String[] args = new String[1];
            args[0] = Long.toString(p_workflowRequestId);
            throw new WorkflowManagerException(
                    WorkflowManagerException.MSG_FAILED_TO_SET_EXCEPTION_IN_REQUEST, args, e);
        }
    }

    private static ExactMatchedSegments leveragePageWithNewTargetLocales(SourcePage p_sourcePage,
            L10nProfile p_l10nProfile, List<WorkflowTemplateInfo> p_workflowTemplates)
    {
        ExactMatchedSegments exactMatchedSegments = null;
        if (p_l10nProfile.getTmChoice() != L10nProfile.NO_TM)
        {
            LeveragingLocales leveragingLocales = new LeveragingLocales();
            for (WorkflowTemplateInfo wti : p_workflowTemplates)
            {
                leveragingLocales.setLeveragingLocale(wti.getTargetLocale(),
                        wti.getLeveragingLocales());
            }

            if (leveragingLocales.size() > 0)
            {
                long jobId = p_sourcePage.getJobId();
                TranslationMemoryProfile tmProfile = p_l10nProfile.getTranslationMemoryProfile();
                LeverageOptions leverageOptions = new LeverageOptions(tmProfile, leveragingLocales);
                TmCoreManager tmCoreManager = null;
                try
                {
                    tmCoreManager = LingServerProxy.getTmCoreManager();
                    // create LeverageDataCenter
                    LeverageDataCenter leverageDataCenter = tmCoreManager
                            .createLeverageDataCenterForPage(p_sourcePage, leverageOptions, jobId);
                    // leverage
                    tmCoreManager.leveragePage(p_sourcePage, leverageDataCenter);
                    // save the matches results to leverage_match table
                    Connection conn = null;
                    try
                    {
                        conn = DbUtil.getConnection();
                        LingServerProxy.getLeverageMatchLingManager().saveLeverageResults(conn,
                                p_sourcePage, leverageDataCenter);
                    }
                    finally
                    {
                        DbUtil.silentReturnConnection(conn);
                    }
                    // retrieve exact match results
                    exactMatchedSegments = leverageDataCenter.getExactMatchedSegments(jobId);
                }
                catch (Exception e)
                {
                    logger.error("Exception when leveraging.", e);

                    String[] args = new String[1];
                    args[0] = Long.toString(p_sourcePage.getId());
                    throw new WorkflowManagerException(
                            WorkflowManagerException.MSG_FAILED_TO_LEVERAGE_SOURCE_PAGE, args, e);
                }
            }
        }

        return exactMatchedSegments;
    }

    private static TermLeverageResult leverageTermsWithNewTargetLocales(SourcePage p_sourcePage,
            L10nProfile p_l10nProfile, List<WorkflowTemplateInfo> p_workflowTemplates)
    {
        TermLeverageResult result = null;

        String project = p_l10nProfile.getProject().getName();
        String termbaseName = p_l10nProfile.getProject().getTermbaseName();
        try
        {
            ArrayList<Tuv> sourceTuvs = SegmentTuvUtil.getSourceTuvs(p_sourcePage);

            if (sourceTuvs.size() > 0 && termbaseName != null && termbaseName.length() > 0)
            {
                TermLeverageOptions options = getTermLeverageOptions(p_l10nProfile,
                        p_workflowTemplates, termbaseName);

                if (options == null)
                {
                    logger.warn("Project " + project + " refers to unknown termbase `"
                            + termbaseName + "'. Term leverage skipped.");
                }
                else if (options.getAllTargetPageLocales().size() == 0
                        || options.getSourcePageLangNames().size() == 0)
                {
                    logger.warn(
                            "No specified locale found in termbase. " + "Term leverage skipped.");
                }
                else
                {
                    result = ServerProxy.getTermLeverageManager().leverageTerms(sourceTuvs, options,
                            String.valueOf(p_sourcePage.getCompanyId()));
                }

            }
        }
        catch (Exception e)
        {
            logger.warn("Exception when leveraging terms, ignoring.", e);
        }

        return result;
    }

    private static TermLeverageOptions getTermLeverageOptions(L10nProfile p_l10nProfile,
            List<WorkflowTemplateInfo> p_workflowTemplates, String p_termbaseName)
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

            ITermbase termbase = manager.connect(p_termbaseName, ITermbase.SYSTEM_USER, "",
                    companyId);

            // add source locale and lang names
            options.setSourcePageLocale(sourceLocale);

            List sourceLangNames = termbase.getLanguagesByLocale(sourceLocale.toString());
            for (Iterator it = sourceLangNames.iterator(); it.hasNext();)
            {
                options.addSourcePageLocale2LangName((String) it.next());
            }

            // add target locales and lang names
            for (Iterator tli = p_workflowTemplates.iterator(); tli.hasNext();)
            {
                WorkflowTemplateInfo wti = (WorkflowTemplateInfo) tli.next();
                Locale targetLocale = ((GlobalSightLocale) wti.getTargetLocale()).getLocale();
                List targetLangNames = termbase.getLanguagesByLocale(targetLocale.toString());

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

    private static Collection<TargetPage> createExtractedTargetPages(Job p_job,
            SourcePage p_sourcePage, L10nProfile p_l10nProfile,
            List<GlobalSightLocale> p_targetLocales, TermLeverageResult p_termMatches,
            ExactMatchedSegments p_exactMatchedSegments)
    {
        Collection<TargetPage> targetPages = null;
        boolean useLeveragedSegments = false;
        try
        {
            if (p_l10nProfile.getTmChoice() != L10nProfile.NO_TM)
            {
                useLeveragedSegments = true;
            }

            if (p_targetLocales.size() > 0)
            {

                TargetPagePersistence tpPersistence = new TargetPageWorkflowAdditionPersistence();
                targetPages = tpPersistence.persistObjectsWithExtractedFile(p_sourcePage,
                        p_targetLocales, p_termMatches, useLeveragedSegments, m_autoReplaceTerms,
                        p_exactMatchedSegments);
            }
        }
        catch (Exception e)
        {
            logger.error("Exception occurred when trying to create target pages.", e);
            String[] args = new String[1];
            args[0] = Long.toString(p_sourcePage.getId());
            throw new WorkflowManagerException(
                    WorkflowManagerException.MSG_FAIL_TO_CREATE_TARGET_PAGE, args, e);
        }

        return targetPages;
    }

    private static Collection<TargetPage> createUnExtractedTargetPages(SourcePage p_sourcePage,
            List<GlobalSightLocale> p_targetLocales)
    {
        Collection<TargetPage> targetPages = null;
        try
        {

            TargetPagePersistence tpPersistence = new TargetPageWorkflowAdditionPersistence();
            targetPages = tpPersistence.persistObjectsWithUnextractedFile(p_sourcePage,
                    p_targetLocales);
        }
        catch (Exception e)
        {
            logger.error("Exception occurred when trying to " + "create target pages.", e);

            String[] args = new String[1];
            args[0] = Long.toString(p_sourcePage.getId());
            throw new WorkflowManagerException(
                    WorkflowManagerException.MSG_FAIL_TO_CREATE_TARGET_PAGE, args, e);
        }

        return targetPages;
    }

    private static List<GlobalSightLocale> getTargetLocales(
            List<WorkflowTemplateInfo> p_workflowTemplates)
    {
        List<GlobalSightLocale> targetLocales = new ArrayList<GlobalSightLocale>();
        for (WorkflowTemplateInfo wti : p_workflowTemplates)
        {
            GlobalSightLocale targetLocale = wti.getTargetLocale();
            targetLocales.add(targetLocale);
        }

        return targetLocales;
    }

    private static boolean getLeverageOptions()
    {
        boolean result = false;
        try
        {
            SystemConfiguration sc = SystemConfiguration.getInstance();
            boolean autoReplaceTerms = sc
                    .getBooleanParameter(SystemConfigParamNames.AUTO_REPLACE_TERMS);

            if (autoReplaceTerms == true)
            {
                result = true;
            }
        }
        catch (Exception e)
        {
            logger.error(
                    "A general exception was thrown when trying to read the system configuration file for system-wide leverage options.",
                    e);
        }

        return result;
    }

    private static void sendEmail(Job p_job, List<Workflow> p_newWorkflows, String p_subject,
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
            Project project = ServerProxy.getProjectHandler().getProjectById(projectId);
            SystemConfiguration config = SystemConfiguration.getInstance();
            capLoginUrl = config.getStringParameter(SystemConfigParamNames.CAP_LOGIN_URL);

            String[] msgArgs = new String[6];
            msgArgs[0] = p_job.getJobName();
            msgArgs[1] = Long.toString(p_job.getDuration());
            msgArgs[3] = Integer.toString(p_job.getWordCount());
            msgArgs[4] = capLoginUrl;
            msgArgs[5] = Integer.toString(p_job.getPriority());

            boolean shouldNotifyPm = false;
            if (p_newWorkflows != null && p_newWorkflows.size() > 0)
            {
                for (Workflow workflow : p_newWorkflows)
                {
                    GlobalSightLocale targetLocale = workflow.getTargetLocale();
                    WorkflowTemplateInfo wfti = l10nProfile.getWorkflowTemplateInfo(targetLocale);

                    if (!shouldNotifyPm && wfti.notifyProjectManager())
                    {
                        shouldNotifyPm = true;
                    }
                    List<String> userIds = wfti.getWorkflowManagerIds();
                    for (String userId : userIds)
                    {
                        sendMailFromAdmin(userId, p_job, msgArgs, p_subject, p_message);
                    }
                }
            }

            if (shouldNotifyPm)
            {
                sendMailFromAdmin(project.getProjectManagerId(), p_job, msgArgs, p_subject,
                        p_message);
            }
        }
        catch (Exception e)
        {
            logger.error("Unable to send email after a dispatch for Job id: " + p_job.getId()
                    + " capLoginUrl=" + (capLoginUrl != null ? capLoginUrl : "null") + " user="
                    + (user != null ? ((UserImpl) user).toDebugString() : "null") + " p_subject="
                    + (p_subject != null ? p_subject : "null") + " p_message="
                    + (p_message != null ? p_message : "null"), e);
        }
    }

    private static void sendMailFromAdmin(String p_userId, Job p_job, String[] p_msgArgs,
            String p_subject, String p_message) throws Exception
    {
        if (!m_systemNotificationEnabled)
        {
            return;
        }
        User user = ServerProxy.getUserManager().getUser(p_userId);
        String userLocale = user.getDefaultUILocale();
        Locale locale = LocaleWrapper.getLocale(userLocale);
        DateFormat dateformat = DateFormat.getDateInstance(DateFormat.MEDIUM, locale);
        p_msgArgs[2] = dateformat.format(p_job.getCreateDate());

        ServerProxy.getMailer().sendMailFromAdmin(user, p_msgArgs, p_subject, p_message,
                String.valueOf(p_job.getCompanyId()));
    }
}
