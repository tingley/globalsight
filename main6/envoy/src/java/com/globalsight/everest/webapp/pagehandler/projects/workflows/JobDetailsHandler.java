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
package com.globalsight.everest.webapp.pagehandler.projects.workflows;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.rmi.RemoteException;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.Vector;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.jbpm.taskmgmt.exe.TaskInstance;
import org.w3c.dom.Element;

import com.globalsight.config.UserParamNames;
import com.globalsight.config.UserParameter;
import com.globalsight.cxe.adapter.msoffice.OfficeXmlHelper;
import com.globalsight.cxe.adapter.openoffice.OpenOfficeHelper;
import com.globalsight.cxe.adapter.passolo.PassoloUtil;
import com.globalsight.cxe.engine.util.XmlUtils;
import com.globalsight.cxe.entity.fileprofile.FileProfile;
import com.globalsight.cxe.persistence.databaseprofile.DatabaseProfilePersistenceManager;
import com.globalsight.cxe.persistence.fileprofile.FileProfilePersistenceManager;
import com.globalsight.everest.comment.CommentFilesDownLoad;
import com.globalsight.everest.comment.CommentManager;
import com.globalsight.everest.comment.Issue;
import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.company.MultiCompanySupportedThread;
import com.globalsight.everest.costing.Cost;
import com.globalsight.everest.costing.Currency;
import com.globalsight.everest.edit.EditHelper;
import com.globalsight.everest.edit.offline.download.JobPackageZipper;
import com.globalsight.everest.foundation.ContainerRole;
import com.globalsight.everest.foundation.EmailInformation;
import com.globalsight.everest.foundation.L10nProfile;
import com.globalsight.everest.foundation.Timestamp;
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.page.AddingSourcePage;
import com.globalsight.everest.page.AddingSourcePageManager;
import com.globalsight.everest.page.Page;
import com.globalsight.everest.page.PageState;
import com.globalsight.everest.page.PrimaryFile;
import com.globalsight.everest.page.SourcePage;
import com.globalsight.everest.page.TargetPage;
import com.globalsight.everest.page.UnextractedFile;
import com.globalsight.everest.page.UpdateSourcePageManager;
import com.globalsight.everest.page.UpdatedSourcePage;
import com.globalsight.everest.permission.Permission;
import com.globalsight.everest.permission.PermissionSet;
import com.globalsight.everest.persistence.tuv.SegmentTuTuvCacheManager;
import com.globalsight.everest.projecthandler.Project;
import com.globalsight.everest.projecthandler.TranslationMemoryProfile;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.statistics.StatisticsService;
import com.globalsight.everest.taskmanager.Task;
import com.globalsight.everest.util.comparator.CurrencyComparator;
import com.globalsight.everest.util.comparator.SurchargeComparator;
import com.globalsight.everest.util.system.SystemConfigParamNames;
import com.globalsight.everest.util.system.SystemConfiguration;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.javabean.NavigationBean;
import com.globalsight.everest.webapp.pagehandler.ControlFlowHelper;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.pagehandler.administration.customer.download.DownloadFileHandler;
import com.globalsight.everest.webapp.pagehandler.administration.projects.ProjectHandlerHelper;
import com.globalsight.everest.webapp.pagehandler.projects.l10nprofiles.LocProfileHandlerHelper;
import com.globalsight.everest.webapp.pagehandler.tasks.TaskHelper;
import com.globalsight.everest.webapp.pagehandler.tasks.UpdateLeverageHelper;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.everest.workflow.Activity;
import com.globalsight.everest.workflow.WorkflowInstance;
import com.globalsight.everest.workflow.WorkflowTaskInstance;
import com.globalsight.everest.workflowmanager.TaskJbpmUtil;
import com.globalsight.everest.workflowmanager.Workflow;
import com.globalsight.everest.workflowmanager.WorkflowAdditionSender;
import com.globalsight.everest.workflowmanager.WorkflowManagerLocal;
import com.globalsight.ling.common.URLDecoder;
import com.globalsight.ling.common.URLEncoder;
import com.globalsight.ling.common.XmlEntities;
import com.globalsight.scheduling.SchedulerConstants;
import com.globalsight.util.Entry;
import com.globalsight.util.FileUtil;
import com.globalsight.util.FormUtil;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.SortUtil;
import com.globalsight.util.date.DateHelper;
import com.globalsight.util.modules.Modules;
import com.globalsight.util.zip.ZipIt;
import com.sun.jndi.toolkit.url.UrlUtil;

public class JobDetailsHandler extends PageHandler implements UserParamNames
{
    private static final Logger CATEGORY = Logger
            .getLogger(JobDetailsHandler.class);

    private static final String EDITOR_BEAN = "editor";

    private static final String SOURCE_EDITOR_BEAN = "sourceEditor";

    private static final String JOB_ATTRIBUTE_BEAN = "jobAttributes";

    private static final String JOB_REPORT_BEAN = "jobReports";

    private static final String WF_DETAILS_BEAN = "workflowActivities";

    private static final String WF_COMMENTS_BEAN = "workflowComments";

    private static final String WF_RATE_VENDOR_BEAN = "rateVendor";

    public static final String CHANGE_CURRENCY_BEAN = "changeCurrencyBean";

    public static final String EXPORT_ERROR_BEAN = "exportError";

    public static final String WF_IMPORT_ERROR_BEAN = "workflowImportError";

    public static final String WORDCOUNTLIST_BEAN = "wordcountList";

    public static final String ASSIGN_BEAN = "assign";

    public static final String SKIP_BEAN = "skip";

    private static final String ADD_SOURCE_FILES = "addSourceFiles";

    private static final String ALL_STATUS = "allStatus";

    public static final String DOWNLOAD_SOURCE_PAGES = "downloadSourcePages";

    public static final String UPDATE_LEVERAGE = "updateLeverage";

    // For sla report issue
    public static final String ECD_BEAN = "estimatedCompletionDate";

    public static final String ETCD_BEAN = "estimatedTranslateCompletionDate";

    private static final String DOWNLOAD_BEAN = "download";

    protected static final String TABLE_ENTRY = "standardText";

    protected static final String TABLE_ENTRY_RED = "warningText";

    protected static boolean s_isCostingEnabled = false;

    protected static boolean s_isRevenueEnabled = false;

    protected static boolean s_isGxmlEditorEnabled = false;

    private static boolean s_isSpecialCustomer = false; // Dell specific!

    private NavigationBean selfBean = null;
    private NavigationBean baseBean = null;
    private NavigationBean modifyBean = null;
    private NavigationBean viewCommentsBean = null;
    private NavigationBean editSourcePageWcBean = null;
    private NavigationBean wordCountListBean = null;
    private NavigationBean downloadBean = null;
    private NavigationBean assignBean = null;
    private NavigationBean editEstimatedCompletionDateBean = null;// For SLA
                                                                  // report
                                                                  // issue
    private NavigationBean editEstimatedTranslateCompletionDateBean = null;

    /**
     * Indicate update word-counts progress for jobs. HashMap<Long, Integer>:
     * jobid--percentage
     */
    private static HashMap<Long, Integer> updateWordCountsPercentageMap = new HashMap<Long, Integer>();

    static
    {
        try
        {
            SystemConfiguration sc = SystemConfiguration.getInstance();
            s_isCostingEnabled = sc
                    .getBooleanParameter(SystemConfigParamNames.COSTING_ENABLED);
            s_isRevenueEnabled = sc
                    .getBooleanParameter(SystemConfigParamNames.REVENUE_ENABLED);

            s_isGxmlEditorEnabled = EditHelper.isGxmlEditorInstalled();

            s_isSpecialCustomer = sc
                    .getBooleanParameter(SystemConfiguration.IS_DELL);
        }
        catch (Throwable e)
        {
            CATEGORY.error("JobHandlerMain::invokeJobControlPage(): "
                    + "Problem getting costing parameter from database.", e);
        }
    }

    /**
     * Invokes this EntryPageHandler object.
     *
     * @param p_pageDescriptor
     *            the description of the page to be produced.
     * @param p_request
     *            original request sent from the browser.
     * @param p_response
     *            original response object.
     * @param p_context
     *            the Servlet context.
     */
    @Override
    public void invokePageHandler(WebPageDescriptor p_pageDescriptor,
            HttpServletRequest p_request, HttpServletResponse p_response,
            ServletContext p_context) throws ServletException, IOException,
            RemoteException, EnvoyServletException
    {
        String pageName = p_pageDescriptor.getPageName();
        String curr = p_request
                .getParameter(JobManagementHandler.CURRENCY);
        HttpSession session = p_request.getSession(false);
        SessionManager sessionMgr = (SessionManager) session
                .getAttribute(SESSION_MANAGER);
        // Set the navigation beans into Request and initialize some beans.
        setNavBeansIntoRequest(p_request, pageName);
        //
        performAppropriateOperation(p_request, sessionMgr);

        // store the search text that the pages are filtered by
        p_request.setAttribute(JobManagementHandler.PAGE_SEARCH_PARAM,
                p_request.getParameter(JobManagementHandler.PAGE_SEARCH_PARAM));
        // fix the null pointer exception when going back to job detail page
        // after some operation.
        sessionMgr.setAttribute("destinationPage",
                JobManagementHandler.DETAILS_BEAN);

        sessionMgr.setAttribute(JobManagementHandler.EXPORT_INIT_PARAM,
                JobManagementHandler.DETAILS_BEAN);

        // clear the surchargesFor attribute here
        sessionMgr.setAttribute(JobManagementHandler.SURCHARGES_FOR, "");
        // clear the session for download job from joblist page
        sessionMgr.setAttribute(DownloadFileHandler.DOWNLOAD_JOB_LOCALES, null);
        sessionMgr.setAttribute(DownloadFileHandler.DESKTOP_FOLDER, null);

        session = p_request.getSession(false);

        if (p_request.getParameter("changePriority") != null)
        {
            if (p_request.getParameter("changePriority").equals("true"))
            {
                long wfId = Long.parseLong(p_request.getParameter("wfId"));
                int priority = Integer.parseInt(p_request
                        .getParameter("priority" + wfId));
                WorkflowManagerLocal wfManager = new WorkflowManagerLocal();
                wfManager.updatePriority(wfId, priority);
            }
        }

        // Get current time from server.
        if (p_request.getParameter(JobManagementHandler.OBTAIN_TIME) != null)
        {
            PrintWriter out = p_response.getWriter();
            p_response.setContentType("text/html");
            out.write(String.valueOf(new Date().getTime()));
            out.close();

            return;
        }

        String action = p_request.getParameter("action");
        if (DOWNLOAD_SOURCE_PAGES.equals(action))
        {
            downloadSourcePages(p_request, p_response);
            return;
        }
        if ("getUpdateWCPercentage".equals(action))
        {
            long jobId = getJobId(p_request, sessionMgr);
            getUpdateWordCountsPercentage(p_response, jobId);
            return;
        }

        Locale uiLocale = (Locale) session
                .getAttribute(WebAppConstants.UILOCALE);

        Job job = null;
        try
        {
            job = WorkflowHandlerHelper.getJobById(getJobId(p_request,
                    sessionMgr));
        }
        catch (Exception e)
        {
            jobNotFound(p_request, p_response, p_context, null);
            return;
        }
        if (Job.CANCELLED.equals(job.getState()))
        {
            jobNotFound(p_request, p_response, p_context, job);
            return;
        }

        String companyId = CompanyThreadLocal.getInstance().getValue();
        if (!String.valueOf(job.getCompanyId()).equals(companyId))
        {
            return;
        }

        TaskHelper.storeObject(session, WORK_OBJECT, job);

        if (s_isCostingEnabled)
        {
            if (curr == null)
            {
                curr = (String) sessionMgr
                        .getAttribute(JobManagementHandler.CURRENCY);
                if (curr == null)
                {
                    // Get the pivot currency;
                    try
                    {
                        Currency c = ServerProxy.getCostingEngine()
                                .getPivotCurrency();
                        curr = c.getIsoCode();
                    }
                    catch (Exception e)
                    {
                        CATEGORY.error("Problem getting pivot currency ", e);
                    }
                }
            }
            sessionMgr.setAttribute(JobManagementHandler.CURRENCY, curr);
            session.setAttribute(JobManagementHandler.CURRENCY, curr);
            session.setAttribute(JobManagementHandler.CURRENCY_XML,
                    getCurrenciesHTML(uiLocale));
        }

        int openSegmentCount = 0;
        int closedSegmentCount = 0;

        // Get the number of open and closed issues.
        // get just the number of issues in OPEN state
        // query is also considered a subset of the OPEN state
        List<String> oStates = new ArrayList<String>();
        oStates.add(Issue.STATUS_OPEN);
        oStates.add(Issue.STATUS_QUERY);
        oStates.add(Issue.STATUS_REJECTED);
        openSegmentCount = getIssueCount(job, session, oStates);
        // get just the number of issues in CLOSED state
        List<String> cStates = new ArrayList<String>();
        cStates.add(Issue.STATUS_CLOSED);
        closedSegmentCount = getIssueCount(job, session, cStates);
        sessionMgr.setAttribute(
                JobManagementHandler.OPEN_AND_QUERY_SEGMENT_COMMENTS,
                new Integer(openSegmentCount).toString());
        sessionMgr.setAttribute(JobManagementHandler.CLOSED_SEGMENT_COMMENTS,
                new Integer(closedSegmentCount).toString());

        p_request.setAttribute(SystemConfigParamNames.COSTING_ENABLED,
                new Boolean(s_isCostingEnabled));
        p_request.setAttribute(SystemConfigParamNames.REVENUE_ENABLED,
                new Boolean(s_isRevenueEnabled));
        p_request.setAttribute(WebAppConstants.GXML_EDITOR, new Boolean(
                s_isGxmlEditorEnabled));
        p_request.setAttribute(SystemConfigParamNames.IS_DELL, new Boolean(
                s_isSpecialCustomer));
        p_request.setAttribute(JobManagementHandler.CHANGE_CURRENCY_URL,
                baseBean.getPageURL() + "&" + JobManagementHandler.JOB_ID + "="
                        + job.getId());

        // Set the download delay time for this company
        sessionMgr.setAttribute(
                SystemConfigParamNames.DOWNLOAD_JOB_DELAY_TIME,
                SystemConfiguration.getInstance().getStringParameter(
                        SystemConfigParamNames.DOWNLOAD_JOB_DELAY_TIME));
        sessionMgr.setAttribute(JobManagementHandler.JOB_ID,
                new Long(job.getId()));
        sessionMgr.setAttribute(JobManagementHandler.QUOTE_DATE,
                job.getQuoteDate());

        // For "Quote process webEx" issue
        sessionMgr.setAttribute(JobManagementHandler.QUOTE_APPROVED_DATE,
                job.getQuoteApprovedDate());
        String quotePoNumberSession = job.getQuotePoNumber();
        quotePoNumberSession = quotePoNumberSession == null ? ""
                : quotePoNumberSession;
        sessionMgr.setAttribute(JobManagementHandler.QUOTE_PO_NUMBER,
                quotePoNumberSession);

        sessionMgr.setAttribute(JobManagementHandler.AUTHORISER_USER,
                job.getUser());

        Project project = WorkflowHandlerHelper.getProjectById(job
                .getProjectId());

        sessionMgr.setAttribute("poRequired", project.getPoRequired());

        if (p_request.getParameter(JobManagementHandler.DATE_CHANGED) != null
                && p_request.getParameter(JobManagementHandler.DATE_CHANGED) == "")
        {
            sessionMgr.setAttribute(JobManagementHandler.DATE_CHANGED, "");
        }

        p_request.setAttribute(
                JobManagementHandler.WORKFLOW_SCRIPTLET,
                getWorkflowText(
                        p_request,
                        uiLocale,
                        s_isCostingEnabled,
                        s_isRevenueEnabled,
                        baseBean.getPageURL(),
                        modifyBean.getPageURL(),
                        getJobDetailsInfo(p_request, s_isCostingEnabled,
                                s_isRevenueEnabled)));
        // turn on cache for previous button.
        // p_response.setHeader("Pragma", "no-cache"); // HTTP 1.0
        // p_response.setHeader("Cache-Control", "no-cache"); // HTTP 1.1
        // p_response.addHeader("Cache-Control", "no-store"); // tell proxy not
        // to cache p_response.addHeader("Cache-Control", "max-age=0");

        // Update the session with this most recently used job
        updateMRUJob(p_request, session, job, p_response);

        User user = (User) sessionMgr.getAttribute(WebAppConstants.USER);

        // Update the Quote PO Number.
        if (p_request.getParameter(JobManagementHandler.QUOTE_PO_NUMBER) != null)
        {
            String quotePoNumber = p_request
                    .getParameter(JobManagementHandler.QUOTE_PO_NUMBER);
            if (!quotePoNumber.equals(session
                    .getAttribute(JobManagementHandler.QUOTE_PO_NUMBER)))
            {
                updateQuotePoNumber(job, quotePoNumber);
                p_request.setAttribute(
                        JobManagementHandler.QUOTE_SAVE_PO_NUMBER, "true");
                sessionMgr.setAttribute(JobManagementHandler.QUOTE_PO_NUMBER,
                        quotePoNumber);
            }
        }
        String approveFlag = p_request
                .getParameter(JobManagementHandler.QUOTE_APPROVED_DATE_MODIFY_FLAG);

        if (p_request.getParameter(JobManagementHandler.QUOTE_DATE) != null)
        {
            if (approveFlag != null && approveFlag.equals("false"))
            {
                String quoteDate = getDateString();

                if (!quoteDate.equals("")
                        && !quoteDate.equals(sessionMgr
                                .getAttribute(JobManagementHandler.QUOTE_DATE)))
                {
                    updateQuoteDate(job, quoteDate);
                    sendEmail(p_request, uiLocale, user, job);

                    sessionMgr.setAttribute(JobManagementHandler.QUOTE_DATE,
                            quoteDate);

                }
            }
        }

        // For "Quote process webEx" issue
        // Update the Quote Approved date
        if ("true"
                .equalsIgnoreCase(p_request
                        .getParameter(JobManagementHandler.QUOTE_APPROVED_DATE_MODIFY_FLAG))
                && p_request
                        .getParameter(JobManagementHandler.QUOTE_APPROVED_DATE) != null)
        {
            String quoteApprovedDate = getDateString();
            String dateChanged = p_request
                    .getParameter(JobManagementHandler.DATE_CHANGED);
            if (!quoteApprovedDate.equals("")
                    && !quoteApprovedDate
                            .equals(sessionMgr
                                    .getAttribute(JobManagementHandler.QUOTE_APPROVED_DATE)))
            {
                updateAuthoriserUser(job, user);
                updateQuoteApprovedDate(job, quoteApprovedDate);

                if (project.getPoRequired() == 0)
                {
                    if (job.getQuoteDate() == null
                            || job.getQuoteDate().equals(""))
                    {
                        updateQuoteDate(job, quoteApprovedDate);
                    }
                }
                // Approved quote Date Default value, it will be set when
                // reset these charges.
                String quoteApproveDelaultValue = "0000";
                if (!quoteApprovedDate.equals(quoteApproveDelaultValue))
                {
                    sendEmail(p_request, uiLocale, user, job);
                }
                sessionMgr.setAttribute(JobManagementHandler.AUTHORISER_USER,
                        user);
                sessionMgr.setAttribute(
                        JobManagementHandler.QUOTE_APPROVED_DATE,
                        quoteApprovedDate);
                sessionMgr.setAttribute(JobManagementHandler.DATE_CHANGED,
                        dateChanged);
            }
        }

        // Update the Quote PO Number.
        if (p_request.getParameter(JobManagementHandler.QUOTE_PO_NUMBER) != null)
        {
            String quotePoNumber = p_request
                    .getParameter(JobManagementHandler.QUOTE_PO_NUMBER);
            if (!quotePoNumber.equals(session
                    .getAttribute(JobManagementHandler.QUOTE_PO_NUMBER)))
            {
                updateQuotePoNumber(job, quotePoNumber);
                sessionMgr.setAttribute(JobManagementHandler.QUOTE_PO_NUMBER,
                        quotePoNumber);
            }
        }

        sessionMgr.setAttribute(JobManagementHandler.ALL_READY_WORKFLOW_IDS,
                getReadyWorkflowIds(job));
        sessionMgr.setAttribute(JobManagementHandler.HAS_READY_WORKFLOW,
                hasReadyWorkflow(job));
        // forward to the jsp page.
        RequestDispatcher dispatcher = p_context
                .getRequestDispatcher(p_pageDescriptor.getJspURL());

        dispatcher.forward(p_request, p_response);
    }

    private String getReadyWorkflowIds(Job job)
    {
        StringBuffer ids = new StringBuffer();
        Collection<Workflow> workflows = job.getWorkflows();
        if (workflows != null)
        {
            for (Workflow workflow : workflows)
            {
                if (Job.READY_TO_BE_DISPATCHED.equals(workflow.getState()))
                {
                    if (ids.length() > 0)
                    {
                        ids.append(",");
                    }
                    ids.append(workflow.getId());
                }
            }
        }

        return ids.toString();
    }

    private boolean hasReadyWorkflow(Job job)
    {
        Collection<Workflow> workflows = job.getWorkflows();
        if (workflows != null)
        {
            for (Workflow workflow : workflows)
            {
                if (Job.READY_TO_BE_DISPATCHED.equals(workflow.getState()))
                {
                    return true;
                }
            }
        }

        return false;
    }

    private void downloadSourcePages(HttpServletRequest p_request,
            HttpServletResponse p_response) throws IOException
    {
        HttpSession session = p_request.getSession(false);
        SessionManager sessionMgr = (SessionManager) session
                .getAttribute(SESSION_MANAGER);
        ResourceBundle bundle = getBundle(session);

        Job p_job = WorkflowHandlerHelper.getJobById(getJobId(p_request,
                sessionMgr));
        List sourcePages = (List) p_job.getSourcePages();

        Iterator it = sourcePages.iterator();
        String m_cxeDocsDir = SystemConfiguration.getInstance()
                .getStringParameter(SystemConfigParamNames.CXE_DOCS_DIR,
                		String.valueOf(p_job.getCompanyId()));
        ArrayList<String> fileNames = new ArrayList<String>();
        ArrayList<String> filePaths = new ArrayList<String>();
        Map<String, String> mapOfNamePath = new HashMap<String, String>();
        while (it.hasNext())
        {
            SourcePage sourcePage = (SourcePage) it.next();

            if (sourcePage.hasRemoved())
            {
                continue;
            }

            String pageName = sourcePage.getDisplayPageName();

            StringBuffer sourceSb = new StringBuffer().append(m_cxeDocsDir)
                    .append("/");
            String externalPageId = sourcePage.getExternalPageId();
            externalPageId = externalPageId.replace("\\", "/");

            if (PassoloUtil.isPassoloFile(sourcePage))
            {
                externalPageId = externalPageId.substring(0,
                        externalPageId.lastIndexOf(".lpu/") + 4);
            }

            externalPageId = SourcePage.filtSpecialFile(externalPageId);

            if (filePaths.contains(externalPageId))
                continue;

            sourceSb = sourceSb.append(externalPageId);
            filePaths.add(externalPageId);
            fileNames.add(sourceSb.toString());
            mapOfNamePath.put(sourceSb.toString(), externalPageId);
        }
        Map<String, String> entryNamesMap = new HashMap<String, String>();
        String jobName = p_job.getJobName();
        String zipFileName = URLEncoder.encode(jobName + ".zip");
        File tmpFile = File.createTempFile("GSDownloadSource", ".zip");
        try
        {
            JobPackageZipper m_zipper = new JobPackageZipper();
            m_zipper.createZipFile(tmpFile);
            entryNamesMap = ZipIt.getEntryNamesMap(filePaths);
            for (int i = 0; i < fileNames.size(); i++)
            {
                filePaths.set(i,
                        entryNamesMap.get(mapOfNamePath.get(fileNames.get(i))));
            }
            addSourcePages(m_zipper, fileNames, filePaths, zipFileName);
            m_zipper.closeZipFile();
            CommentFilesDownLoad commentFilesDownload = new CommentFilesDownLoad();
            commentFilesDownload.sendFileToClient(p_request, p_response,
                    jobName + ".zip", tmpFile);
        }
        finally
        {
            FileUtil.deleteTempFile(tmpFile);
        }
    }

    private void addSourcePages(JobPackageZipper m_zipper,
            List<String> fileNames, List<String> filePaths, String zipFileName)
    {
        for (int i = 0; i < fileNames.size(); i++)
        {
            File file = new File(fileNames.get(i));

            if (!file.exists())
            {
                XmlEntities entity = new XmlEntities();
                File dir = file.getParentFile();
                if (dir.isDirectory())
                {
                    File[] files = dir.listFiles();
                    for (File f : files)
                    {
                        if (file.getName().equals(
                                entity.decodeStringBasic(f.getName())))
                        {
                            file = f;
                            break;
                        }
                    }
                }
            }

            if (file.exists())
            {
                FileInputStream input = null;
                try
                {
                    input = new FileInputStream(file);

                    if (input == null)
                    {
                        CATEGORY.warn("Could not locate comment file: " + file);
                    }
                    else
                    {
                        m_zipper.writePath(filePaths.get(i));
                        m_zipper.writeFile(input);
                    }
                    input.close();
                }
                catch (IOException ex)
                {
                    CATEGORY.warn("cannot write comment file: " + file
                            + " to zip stream.", ex);
                }
                finally
                {
                    try
                    {
                        if (input != null)
                        {
                            input.close();
                        }
                    }
                    catch (IOException e)
                    {
                        CATEGORY.warn("cannot close comment file: " + file
                                + " to zip stream.", e);
                    }
                }
            }
        }
    }

    /**
     * Format current time as MM/dd/yyyy HH:mm;
     *
     * @return
     */
    private String getDateString()
    {
        Calendar now = Calendar.getInstance();

        Format f = new SimpleDateFormat("MM/dd/yyyy HH:mm");

        return f.format(now.getTime());
    }

    /**
     *
     * @param p_job
     *            specifies the job to be updated
     * @param p_quoteDate
     *            specifies the quote date to be updated This method is used to
     *            update the quote date in database
     */
    private void updateQuoteDate(Job p_job, String p_quoteDate)
    {
        try
        {
            ServerProxy.getJobHandler().updateQuoteDate(p_job, p_quoteDate);
        }
        catch (Exception e)
        {
            CATEGORY.error("Problem updating quotation email date ", e);
        }
    }

    /**
     * For "Quote process webEx" issue Update the Quote Approved date
     *
     * @param p_job
     *            specifies the job to be updated
     * @param p_quoteApprovedDate
     *            specifies the quote date to be updated This method is used to
     *            update the quote approved date in database
     */
    private void updateQuoteApprovedDate(Job p_job, String p_quoteApprovedDate)
    {
        try
        {
            ServerProxy.getJobHandler().updateQuoteApprovedDate(p_job,
                    p_quoteApprovedDate);
        }
        catch (Exception e)
        {
            CATEGORY.error("Problem updating quotation email date ", e);
        }
    }

    /**
     * For "Quote process webEx" issue Update the Quote PO Number.
     *
     * @param p_job
     *            specifies the job to be updated
     * @param p_quotePoNumber
     *            specifies the quote PO Number to be updated This method is
     *            used to update the quote PO Number in database
     */
    private void updateQuotePoNumber(Job p_job, String p_quotePoNumber)
    {
        try
        {
            ServerProxy.getJobHandler().updateQuotePoNumber(p_job,
                    p_quotePoNumber);
        }
        catch (Exception e)
        {
            CATEGORY.error("Problem updating quotation email date ", e);
        }
    }

    private void updateAuthoriserUser(Job p_job, User user)
    {
        try
        {
            ServerProxy.getJobHandler().updateAuthoriserUser(p_job, user);
        }
        catch (Exception e)
        {
            CATEGORY.error(
                    "Problem updating user who approved the cost of job.", e);
        }
    }

    /**
     * This method is used to prepare data for email sending
     */
    private void sendEmail(HttpServletRequest p_request, Locale p_uiLocale,
            User p_user, Job p_job)
    {
        String newPO = p_job.getQuotePoNumber();

        if (p_request.getParameter(JobManagementHandler.QUOTE_PO_NUMBER) != null)
        {
            newPO = p_request
                    .getParameter(JobManagementHandler.QUOTE_PO_NUMBER);
        }

        try
        {
            Project project = WorkflowHandlerHelper.getProjectById(p_job
                    .getL10nProfile().getProjectId());
            String companyIdStr = String.valueOf(project.getCompanyId());
            User pm = project.getProjectManager();
            User quotePerson = null;
            if (project.getQuotePersonId() != null
                    && !"".equals(project.getQuotePersonId()))
            {
                if ("0".equals(project.getQuotePersonId()))
                {
                    // "0" indicates the quote person is set to the job
                    // submitter.
                    quotePerson = p_job.getCreateUser();
                }
                else
                {
                    quotePerson = ProjectHandlerHelper.getUser(project
                            .getQuotePersonId());
                }
            }
            String approveFlag = p_request
                    .getParameter(JobManagementHandler.QUOTE_APPROVED_DATE_MODIFY_FLAG);
            String savePONumber = (String) p_request
                    .getAttribute(JobManagementHandler.QUOTE_SAVE_PO_NUMBER);
            EmailInformation from = ServerProxy.getUserManager()
                    .getEmailInformationForUser(p_user.getUserId());
            if (savePONumber != null && "true".equals(savePONumber))
            {
                if (!pm.getUserId().equals(p_user.getUserId()))
                {
                    EmailInformation pmEmailInfo = ServerProxy.getUserManager()
                            .getEmailInformationForUser(pm.getUserId());
                    // send email to PM
                    ServerProxy.getMailer().sendMail(
                            from,
                            pmEmailInfo,
                            SchedulerConstants.NOTIFY_PONUMBER_SUBJECT,
                            SchedulerConstants.NOTIFY_PONUMBER_BODY,
                            getArguments(p_request, p_uiLocale, p_user, p_job,
                                    newPO), companyIdStr);
                }
            }
            else if (approveFlag != null && approveFlag.equals("false"))
            {
                if (!pm.getUserId().equals(p_user.getUserId()))
                {
                    EmailInformation pmEmailInfo = ServerProxy.getUserManager()
                            .getEmailInformationForUser(pm.getUserId());
                    // send email to PM
                    ServerProxy.getMailer().sendMail(
                            from,
                            pmEmailInfo,
                            SchedulerConstants.NOTIFY_QUOTE_PERSON_SUBJECT,
                            SchedulerConstants.NOTIFY_QUOTE_PERSON_BODY,
                            getArguments(p_request, p_uiLocale, p_user, p_job,
                                    newPO), companyIdStr);
                }

                if (quotePerson != null
                        && !quotePerson.getUserId().equals(p_user.getUserId()))
                {
                    EmailInformation qpEmailInfo = ServerProxy
                            .getUserManager()
                            .getEmailInformationForUser(quotePerson.getUserId());
                    p_request.setAttribute(WebAppConstants.LOGIN_NAME_FIELD,
                            quotePerson.getUserId());
                    // send email to quote person
                    ServerProxy.getMailer().sendMail(
                            from,
                            qpEmailInfo,
                            SchedulerConstants.NOTIFY_QUOTE_PERSON_SUBJECT,
                            SchedulerConstants.NOTIFY_QUOTE_PERSON_BODY,
                            getArguments(p_request, p_uiLocale, p_user, p_job,
                                    newPO), companyIdStr);
                }
            }
            else if (approveFlag != null && approveFlag.equals("true"))
            {
                if (quotePerson != null && quotePerson.getUserId() != null)
                {
                    EmailInformation qpEmailInfo = ServerProxy.getUserManager()
                            .getEmailInformationForUser(pm.getUserId());
                    // send email to PM for approving
                    ServerProxy.getMailer().sendMail(
                            from,
                            qpEmailInfo,
                            SchedulerConstants.NOTIFY_QUOTEAPPROVED_SUBJECT,
                            SchedulerConstants.NOTIFY_QUOTEAPPROVED_BODY,
                            getArguments(p_request, p_uiLocale, p_user, p_job,
                                    newPO), companyIdStr);
                }

            }
        }
        catch (Exception e)
        {
            CATEGORY.error("Problem sending quotation email ", e);
        }
    }

    /**
     *
     * @return the arguments of the email message.
     */
    private String[] getArguments(HttpServletRequest p_request,
            Locale p_uiLocale, User p_user, Job p_job, String p_po)
    {
        String[] messageArgs = new String[7];
        messageArgs[0] = p_job.getJobName();
        messageArgs[1] = String.valueOf(p_job.getJobId());
        Collection workflows = p_job.getWorkflows();
        StringBuffer sb = new StringBuffer();
        Workflow workflowInstance = null;
        GlobalSightLocale targetLocal = null;

        // Session dbSession = HibernateUtil.getSession();
        Iterator iterator = workflows.iterator();
        while (iterator.hasNext())
        {
            workflowInstance = (Workflow) iterator.next();
            // Remove the workflow instance that be cancelled
            if (workflowInstance.getState().equals(Workflow.CANCELLED))
            {
                continue;
            }
            targetLocal = workflowInstance.getTargetLocale();

            // targetLocal = (GlobalSightLocale) dbSession.get(
            // GlobalSightLocale.class, targetLocal.getIdAsLong());
            if (iterator.hasNext())
            {
                sb.append(targetLocal.getDisplayName(p_uiLocale) + ", ");
            }
            else
            {
                sb.append(targetLocal.getDisplayName(p_uiLocale));
            }
        }

        // dbSession.close();

        messageArgs[2] = sb.toString();
        messageArgs[3] = p_user.getUserName();
        messageArgs[4] = (String) p_request
                .getAttribute(JobManagementHandler.FINAL_REVENUE);
        messageArgs[5] = p_po;
        messageArgs[6] = makeUrlToJobDetail(p_request, p_job);

        return messageArgs;
    }

    /**
     * Makes a link to go to job detail page directly.
     *
     * @param p_request
     * @param p_job
     *
     * @return the url
     */
    private String makeUrlToJobDetail(HttpServletRequest p_request, Job p_job)
    {
        StringBuilder sb = new StringBuilder("");
        sb.append(getCapLoginUrl());
        sb.append("?");
        sb.append(WebAppConstants.LOGIN_FROM);
        sb.append("=");
        sb.append(WebAppConstants.LOGIN_FROM_EMAIL);
        sb.append("&");
        sb.append(WebAppConstants.LOGIN_NAME_FIELD);
        sb.append("=");
        sb.append(p_request.getAttribute(WebAppConstants.LOGIN_NAME_FIELD));
        sb.append("&");
        sb.append(WebAppConstants.LOGIN_FORWARD_URL);
        sb.append("=");
        String forwardUrl = "/ControlServlet?linkName=jobDetails&pageName=DTLS&jobId="
                + p_job.getId();
        forwardUrl = URLEncoder.encode(forwardUrl, "UTF-8");
        sb.append(forwardUrl);

        return sb.toString();
    }

    /**
     * Gets GlobalSight login URL.
     *
     * @return the URL.
     */
    private String getCapLoginUrl()
    {
        SystemConfiguration config = SystemConfiguration.getInstance();
        return config.getStringParameter(SystemConfigParamNames.CAP_LOGIN_URL);
    }

    /**
     * This method is inherited by other sub-classes to get the job detail
     * information for the UI. Some sub-classes may override it or some may use
     * it. The signature can't be changed without changing the sub-class
     * signatures.
     */
    protected Job getJobDetailsInfo(HttpServletRequest p_request,
            boolean p_jobCosting, boolean p_jobRevenue)
            throws EnvoyServletException
    {
        HttpSession session = p_request.getSession(false);
        SessionManager sessionMgr = (SessionManager) session
                .getAttribute(SESSION_MANAGER);

        Job job = WorkflowHandlerHelper.getJobById(getJobId(p_request,
                sessionMgr));

        // job ID
        p_request.setAttribute(JobManagementHandler.JOB_ID,
                Long.toString(job.getId()));

        // job name
        p_request.setAttribute(JobManagementHandler.JOB_NAME_SCRIPTLET,
                job.getJobName());
        long profileId = job.getL10nProfileId();
        // l10nProfile name
        L10nProfile l10nProfile = LocProfileHandlerHelper
                .getL10nProfile(profileId);

        // boolean isUseInContext =
        // l10nProfile.getTranslationMemoryProfile().getIsContextMatchLeveraging();
        boolean isInContextMatch = isInContextMatch(job);
        p_request.setAttribute(JobManagementHandler.L10NPROFILE_NAME_SCRIPTLET,
                l10nProfile.getName());

        // project name
        Project project = WorkflowHandlerHelper.getProjectById(l10nProfile
                .getProjectId());

        p_request.setAttribute(JobManagementHandler.PROJECT_NAME_SCRIPTLET,
                project.getName());

        User initiator = job.getCreateUser();
        if (initiator == null)
        {
            initiator = project.getProjectManager();
        }

        // initiator
        p_request.setAttribute(JobManagementHandler.JOB_INITIATOR_SCRIPTLET,
                initiator.getUserName());

        // source locale
        Locale uiLocale = (Locale) session
                .getAttribute(WebAppConstants.UILOCALE);
        TimeZone timeZone = (TimeZone) session
                .getAttribute(WebAppConstants.USER_TIME_ZONE);
        GlobalSightLocale sourceLocale = job.getSourceLocale();
        p_request.setAttribute(JobManagementHandler.SRC_LOCALE_SCRIPTLET,
                sourceLocale.getDisplayName(uiLocale));

        // date created
        p_request.setAttribute(JobManagementHandler.JOB_DATE_CREATED_SCRIPTLET,
                DateHelper.getFormattedDateAndTime(job.getCreateDate(),
                        uiLocale, timeZone));

        // source word count total
        p_request.setAttribute(JobManagementHandler.TOTAL_SOURCE_PAGE_WC,
                Long.toString(job.getWordCount()));
        p_request.setAttribute(JobManagementHandler.TOTAL_WC_OVERRIDEN,
                new Boolean(job.isWordCountOverriden()));

        // Job Cost
        // Here we have to calculate the job cost.
        if (p_jobCosting)
        {
            try
            {
                Cost revenue = null;
                String curr = (String) sessionMgr
                        .getAttribute(JobManagementHandler.CURRENCY);
                Currency oCurrency = ServerProxy.getCostingEngine()
                        .getCurrency(curr);
                // Calculate Expenses
                Cost cost = ServerProxy.getCostingEngine().calculateCost(job,
                        oCurrency, true, Cost.EXPENSE);

                // Get the number of pages in job
                int numPages = job.getPageCount();

                // set the num of pages as an int - no decimal should be shown
                String numPagesStr = Integer.toString(numPages);
                p_request.setAttribute(JobManagementHandler.PAGES_IN_JOB,
                        numPagesStr);

                // cost is only null, when all workflows of a job are discarded
                // from
                // job details UI.
                if (cost != null)
                {
                    // Get the Estimated cost
                    String formattedEstimatedCost = (isInContextMatch) ? cost
                            .getEstimatedCost().getFormattedAmount()
                            : cost.getNoUseEstimatedCost().getFormattedAmount();
                    p_request.setAttribute(JobManagementHandler.ESTIMATED_COST,
                            formattedEstimatedCost);

                    // Get the Actual cost
                    String formattedActualCost = cost.getActualCost()
                            .getFormattedAmount();
                    p_request.setAttribute(JobManagementHandler.ACTUAL_COST,
                            formattedActualCost);

                    // Get the Final cost
                    String formattedFinalCost = cost.getFinalCost()
                            .getFormattedAmount();
                    p_request.setAttribute(JobManagementHandler.FINAL_COST,
                            formattedFinalCost);

                    // Put the cost object on the sessionMgr so other cost
                    // screens
                    // will have easy access to it
                    sessionMgr.setAttribute(JobManagementHandler.COST_OBJECT,
                            cost);
                    sessionMgr.setAttribute(
                            JobManagementHandler.CURRENCY_OBJECT, oCurrency);

                    // Sort the Surcharges as pass them as an ArrayList to the
                    // JSP
                    Collection surchargesAll = cost.getSurcharges();
                    ArrayList surchargesList = new ArrayList(surchargesAll);
                    SurchargeComparator comp = new SurchargeComparator(
                            SurchargeComparator.NAME, uiLocale);
                    SortUtil.sort(surchargesList, comp);

                    p_request.setAttribute(JobManagementHandler.SURCHARGES_ALL,
                            surchargesList);
                }
                if (p_jobRevenue)
                {
                    // Calculate Revenue
                    revenue = ServerProxy.getCostingEngine().calculateCost(job,
                            oCurrency, true, Cost.REVENUE);
                    if (revenue != null)
                    {
                        // Get the Estimated cost
                        String formattedEstimatedCost = (isInContextMatch) ? revenue
                                .getEstimatedCost().getFormattedAmount()
                                : revenue.getNoUseEstimatedCost().getFormattedAmount();
                        p_request.setAttribute(
                                JobManagementHandler.ESTIMATED_REVENUE,
                                formattedEstimatedCost);

                        // Get the Actual cost
                        String formattedActualCost = revenue.getActualCost()
                                .getFormattedAmount();
                        p_request.setAttribute(
                                JobManagementHandler.ACTUAL_REVENUE,
                                formattedActualCost);

                        // Get the Final cost
                        String formattedFinalCost = revenue.getFinalCost()
                                .getFormattedAmount();
                        p_request.setAttribute(
                                JobManagementHandler.FINAL_REVENUE,
                                formattedFinalCost);

                        // Put the cost object on the sessionMgr so other cost
                        // screens
                        // will have easy access to it
                        sessionMgr.setAttribute(
                                JobManagementHandler.REVENUE_OBJECT, revenue);
                        sessionMgr
                                .setAttribute(
                                        JobManagementHandler.CURRENCY_OBJECT,
                                        oCurrency);

                        // Sort the Surcharges as pass them as an ArrayList to
                        // the JSP
                        Collection surchargesAll = revenue.getSurcharges();
                        ArrayList surchargesList = new ArrayList(surchargesAll);
                        SurchargeComparator comp = new SurchargeComparator(
                                SurchargeComparator.NAME, uiLocale);
                        SortUtil.sort(surchargesList, comp);

                        p_request.setAttribute(
                                JobManagementHandler.REVENUE_SURCHARGES_ALL,
                                surchargesList);
                    }
                }
            }
            catch (Exception e)
            {
                throw new EnvoyServletException(
                        EnvoyServletException.EX_GENERAL, e);
            }
        }

        // GET THE JOB CONTENT!!!
        p_request.setAttribute(JobManagementHandler.JOB_CONTENT_SCRIPTLET,
                getJobContentInfo(job, p_request));

        // job priority
        p_request.setAttribute(JobManagementHandler.JOB_PRIORITY_SCRIPTLET,
                Integer.toString(job.getPriority()));

        return job;
    }

    /**
     * Overide getControlFlowHelper so we can do processing and redirect the
     * user correctly.
     *
     * @return the name of the link to follow
     */
    @Override
    public ControlFlowHelper getControlFlowHelper(HttpServletRequest p_request,
            HttpServletResponse p_response)
    {
        return new JobDetailsControlFlowHelper(p_request, p_response);
    }

    private String getWorkflowText(HttpServletRequest p_request,
            Locale p_uiLocale, boolean p_jobCosting, boolean p_jobRevenue,
            String p_baseURL, String p_modifyURL, Job p_job)
            throws RemoteException, EnvoyServletException
    {
        HttpSession session = p_request.getSession();
        StringBuilder sb = new StringBuilder();
        List workflows = new ArrayList(p_job.getWorkflows());
        Collections
                .sort(workflows, new WorkflowComparator(Locale.getDefault()));
        ResourceBundle bundle = getBundle(session);
        Locale uiLocale = (Locale) session.getAttribute(UILOCALE);
        TimeZone timezone = (TimeZone) session.getAttribute(USER_TIME_ZONE);

        // need to store job name for Graphical Workflow UI
        SessionManager sessionMgr = (SessionManager) session
                .getAttribute(SESSION_MANAGER);
        sessionMgr.setAttribute(JobManagementHandler.JOB_NAME_SCRIPTLET,
                p_job.getJobName());
        PermissionSet perms = (PermissionSet) session
                .getAttribute(WebAppConstants.PERMISSIONS);

        // does user have permission to see start date of review activity?
        boolean viewReviewStartDate = perms
                .getPermissionFor(Permission.JOB_WORKFLOWS_ESTREVIEWSTART);

        User user = (User) sessionMgr.getAttribute(USER);

        Timestamp ts = new Timestamp(Timestamp.DATE, timezone);
        ts.setLocale(uiLocale);
        int wfSize = workflows.size();
        // Session dbSession = HibernateUtil.getSession();

        for (int i = 0; i < wfSize; i++)
        {
            Workflow curWF = (Workflow) workflows.get(i);
            // curWF = (WorkflowImpl) dbSession.get(WorkflowImpl.class, curWF
            // .getIdAsLong());

            // if the workflow is canceled, then skip it
            if (curWF.getState().equals(Workflow.CANCELLED))
                continue;
            // skip - if the user has no Show All Jobs and Show My Jobs
            // permission,
            // and the user is not the PM of the job's project
            if (!perms.getPermissionFor(Permission.JOB_SCOPE_ALL)
                    && !perms.getPermissionFor(Permission.JOB_SCOPE_MYPROJECTS)
                    && !(ProjectHandlerHelper.getProjectById(
                            p_job.getProjectId()).getProjectManagerId() == user
                            .getUserId())
                    && PageHandler.invalidForWorkflowOwner(user.getUserId(),
                            perms, curWF))
            {
                continue;
            }

            boolean isRowWhite = i % 2 == 0;
            long wfId = curWF.getId();
            String rowClass = "standardText";

            if (curWF.getState().equals("EXPORT_FAILED")
                    || curWF.getState().equals("IMPORT_FAILED"))
            {
                rowClass = "warningText";
            }

            sb.append("<TR CLASS="
                    + rowClass
                    + " BGCOLOR=\""
                    + (isRowWhite ? JobManagementHandler.WHITE_BG
                            : JobManagementHandler.GREY_BG) + "\">\n");

            // Check box
            boolean jobWorkflowPriorityPermission = perms
                    .getPermissionFor(Permission.JOB_WORKFLOWS_PRIORITY);
            boolean isVendorManagementInstalled = Modules
                    .isVendorManagementInstalled();
            if (jobWorkflowPriorityPermission)
            {
                sb.append("<TD><INPUT TYPE=checkbox ONCLICK='setButtonState("
                        + isVendorManagementInstalled + ");showPriorityDiv("
                        + wfId + ");' ");
            }
            else
            {
                sb.append("<TD><INPUT TYPE=checkbox ONCLICK='setButtonState("
                        + isVendorManagementInstalled + ");' ");
            }
            sb.append("NAME=wfId id='wfId_" + wfId + "' VALUE=\"");
            sb.append("wfId=" + Long.toString(wfId));
            sb.append("&");
            sb.append("wfState=" + curWF.getState());
            sb.append("&");
            sb.append("wfIsEditable=" + isWorkflowEditable(perms, curWF));
            sb.append("\">");

            // target locale.
            sb.append("<TD>");
            GlobalSightLocale targetLocale = curWF.getTargetLocale();
            sb.append(targetLocale.getDisplayName(p_uiLocale));
            sb.append("</TD>\n");

            // word count
            sb.append("<TD ALIGN=\"CENTER\">");
            if (perms.getPermissionFor(Permission.JOB_WORKFLOWS_WORDCOUNT))
            {
                sb.append("<a class=standardHREF href='");
                sb.append(wordCountListBean.getPageURL());
                sb.append("&");
                sb.append("wfId=" + Long.toString(wfId));
                sb.append("&action=one'>");
            }
            sb.append(JobManagementHandler.getTotalWordCount(curWF,
                    JobManagementHandler.TOTAL_WF_WORD_CNT));
            if (perms.getPermissionFor(Permission.JOB_WORKFLOWS_WORDCOUNT))
            {
                sb.append("</a>");
            }
            sb.append("</TD>\n");

            // % complete
            sb.append("<TD ALIGN=\"CENTER\">");
            sb.append(curWF.getPercentageCompletion());
            sb.append("%</TD>\n");

            // Workflow state
            sb.append("<TD id='currentWorkflowState_" + wfId + "'>"
                    + bundle.getString(curWF.getState()));
            sb.append("&nbsp;&nbsp;&nbsp;</TD>\n");

            // Currently active task...
            Hashtable hTasks = curWF.getTasks();
            // List taskList = new ArrayList(hTasks.values());
            // int size = taskList.size();
            // boolean found = false;

            sb.append("<TD STYLE=\"padding-right: 10px;\">");
            // now sort tasks....
            String name = " ";
            if (!Workflow.SKIPPING.equals(curWF.getState()))
            {
                TaskInstance task = WorkflowManagerLocal.getCurrentTask(curWF
                        .getId());
                if (task != null)
                {
                    name = TaskJbpmUtil.getTaskDisplayName(task.getName());
                }
            }
            sb.append(name);
            sb.append("</TD>\n");

            // Estimated Review Start
            // Display the start date of the very first review-only task
            if (s_isSpecialCustomer && viewReviewStartDate)
            {
                Date estimatedStart = getFirstReviewStartDate(wfId, hTasks,
                        curWF.getDispatchedDate());

                if (estimatedStart != null)
                {
                    ts.setDate(estimatedStart);
                    sb.append("<TD>");
                    sb.append(ts);
                }
                else
                {
                    sb.append("<TD>--");
                }
                sb.append("&nbsp;&nbsp;&nbsp;</TD>\n");
            }

            // For sla report issue
            boolean enableEdit = Workflow.READY_TO_BE_DISPATCHED.equals(curWF
                    .getState())
                    || Workflow.DISPATCHED.equals(curWF.getState());

            // Estimated translate completion date
            if (perms
                    .getPermissionFor(Permission.JOBS_ESTIMATEDTRANSLATECOMPDATE)
                    && enableEdit)
            {
                sb.append("<TD><A STYLE=\"word-wrap:break-word;word-break:break-all\" CLASS=\"");
                sb.append("standard");
                sb.append("HREF\" HREF=\"");
                sb.append(editEstimatedTranslateCompletionDateBean.getPageURL());
                sb.append("&");
                sb.append(JOB_ID);
                sb.append("=");
                sb.append(p_job.getJobId());
                sb.append("\">");

                if (curWF.getEstimatedTranslateCompletionDate() != null)
                {
                    ts.setDate(curWF.getEstimatedTranslateCompletionDate());
                    sb.append(ts);
                    sb.append(" ");
                    sb.append(ts.getHour() + ":");
                    if (ts.getMinute() < 10)
                    {
                        sb.append("0");
                    }
                    sb.append(ts.getMinute());
                    sb.append(" ");
                    sb.append(ts.getTimeZone().getDisplayName(uiLocale));
                }
                else
                {
                    sb.append("--");
                }

                sb.append("</A>");
                sb.append("</TD>\n");
            }
            else
            {
                sb.append("<TD  STYLE=\"padding-right: 10px;\"><SPAN CLASS=\"");
                sb.append("standardText");
                sb.append("\">");

                if (curWF.getEstimatedTranslateCompletionDate() != null)
                {
                    ts.setDate(curWF.getEstimatedTranslateCompletionDate());
                    sb.append(ts);
                    sb.append(" ");
                    sb.append(ts.getHour() + ":");
                    if (ts.getMinute() < 10)
                    {
                        sb.append("0");
                    }
                    sb.append(ts.getMinute());
                    sb.append(" ");
                    sb.append(ts.getTimeZone().getDisplayName(uiLocale));
                }
                else
                {
                    sb.append("--");
                }

                sb.append("</SPAN>&nbsp;&nbsp;&nbsp;</TD>\n");
            }

            // Estimated workflow completion date
            if (perms.getPermissionFor(Permission.JOBS_ESTIMATEDCOMPDATE)
                    && enableEdit)
            {
                sb.append("<TD><A STYLE=\"word-wrap:break-word;word-break:break-all\" CLASS=\"");
                sb.append("standard");
                sb.append("HREF\" HREF=\"");
                sb.append(editEstimatedCompletionDateBean.getPageURL());
                sb.append("&");
                sb.append(JOB_ID);
                sb.append("=");
                sb.append(p_job.getJobId());
                sb.append("\">");

                if (curWF.getEstimatedCompletionDate() != null)
                {
                    ts.setDate(curWF.getEstimatedCompletionDate());
                    sb.append(ts);
                    sb.append(" ");
                    sb.append(ts.getHour() + ":");
                    if (ts.getMinute() < 10)
                    {
                        sb.append("0");
                    }
                    sb.append(ts.getMinute());
                    sb.append(" ");
                    sb.append(ts.getTimeZone().getDisplayName(uiLocale));
                }
                else
                {
                    sb.append("--");
                }

                sb.append("</A>");
                sb.append("</TD>\n");

            }
            else
            {
                sb.append("<TD  STYLE=\"padding-right: 10px;\"><SPAN CLASS=\"");
                sb.append("standardText");
                sb.append("\">");

                if (curWF.getEstimatedCompletionDate() != null)
                {
                    ts.setDate(curWF.getEstimatedCompletionDate());
                    sb.append(ts);
                    sb.append(" ");
                    sb.append(ts.getHour() + ":");
                    if (ts.getMinute() < 10)
                    {
                        sb.append("0");
                    }
                    sb.append(ts.getMinute());
                    sb.append(" ");
                    sb.append(ts.getTimeZone().getDisplayName(uiLocale));
                }
                else
                {
                    sb.append("--");
                }

                sb.append("</SPAN>&nbsp;&nbsp;&nbsp;</TD>\n");
            }

            // priority
            if (jobWorkflowPriorityPermission)
            {
                sb.append("<TD ALIGN=\"CENTER\">");
                sb.append("<div id=prioritySelect" + wfId
                        + " style=\"display:none\">");
                sb.append("<select name=priority" + wfId + ">");

                for (int x = 1; x < 6; x++)
                {
                    if (curWF.getPriority() == x)
                    {
                        sb.append("<OPTION VALUE=\"" + x + "\" SELECTED>" + x);
                    }
                    else
                    {
                        sb.append("<OPTION VALUE=\"" + x + "\">" + x);
                    }
                }
                sb.append("</select>");
                sb.append("</div>");
                sb.append("<div id=priorityLabel" + wfId
                        + " style=\"display:block\">");
                sb.append(curWF.getPriority() + "");
                sb.append("</div>");
                sb.append("</TD>\n");
            }
            else
            {
                sb.append("<TD ALIGN=\"CENTER\">");
                sb.append(curWF.getPriority());
                sb.append("</TD>\n");
            }

            sb.append("</TR>\n");
        }
        // dbSession.close();

        return sb.toString();
    }

    /**
     * Determine whether the workflow is editable or not. Note that only a
     * workflow owner can modify a workflow. In our case, a Project Manager and
     * a Workflow Manager can modify a workflow that they are assiciated with.
     */
    private boolean isWorkflowEditable(PermissionSet p_perms, Workflow p_wf)
    {
        boolean canEditWorkflow = p_perms
                .getPermissionFor(Permission.PROJECTS_MANAGE)
                || p_perms
                        .getPermissionFor(Permission.PROJECTS_MANAGE_WORKFLOWS);
        return WorkflowHandlerHelper.isWorkflowModifiable(p_wf.getState())
                && canEditWorkflow;
    }

    /**
     * Returns an HTML servlet, that is an HTML string consisting of table rows
     * with page names and word counts.
     * <p>
     * This method is inherited by other sub-classes to get the job detail
     * information for the UI. Some sub-classes may override it or some may use
     * it. The signature can't be changed without changing the sub-class
     * signatures.
     */
    protected String getJobContentInfo(Job p_job, HttpServletRequest p_request)
            throws EnvoyServletException
    {
        StringBuffer sb = new StringBuffer();

        Object[] wfs = p_job.getWorkflows().toArray();
        boolean cancelled = true;

        // If all workflows of a job are discarded, don't show the
        // pages as links.
        for (int i = 0; cancelled && i < wfs.length; i++)
        {
            Workflow workflow = (Workflow) wfs[i];
            cancelled = workflow.getState().equals(Workflow.CANCELLED);
        }

        HttpSession session = p_request.getSession(false);
        PermissionSet userPerms = (PermissionSet) session
                .getAttribute(WebAppConstants.PERMISSIONS);
        boolean editAllowed = userPerms
                .getPermissionFor(Permission.JOB_FILES_EDIT);
        ResourceBundle bundle = getBundle(session);
        UserParameter param = PageHandler.getUserParameter(session,
                UserParamNames.PAGENAME_DISPLAY);
        String pagenameDisplay = param.getValue();

        boolean atLeastOneError = false;
        boolean wordCountOverridenAtAll = false;
        List<SourcePage> sourcePages = (List<SourcePage>) p_job
                .getSourcePages();
        for (int i = sourcePages.size() - 1; i >= 0; i--)
        {
            SourcePage sp = sourcePages.get(i);
            if (sp.hasRemoved())
            {
                sourcePages.remove(i);
            }
        }

        List<UpdatedSourcePage> uSourcdPages = UpdateSourcePageManager
                .getAllUpdatedSourcePage(p_job);
        List<AddingSourcePage> aSourcdPages = AddingSourcePageManager
                .getAllAddingSourcePage(p_job);
        // sorts the pages in the correct order and store the column and sort
        // order
        // also filters them according to the search params
        sourcePages = filterPagesByName(p_request, session, sourcePages);
        sortPages(p_request, session, sourcePages);

        uSourcdPages = filterPagesByName(p_request, session, uSourcdPages);
        UpdateSourcePageManager.sort(uSourcdPages);

        aSourcdPages = filterPagesByName(p_request, session, aSourcdPages);
        AddingSourcePageManager.sort(aSourcdPages);

        boolean addCheckBox = userPerms
                .getPermissionFor(Permission.EDIT_SOURCE_FILES)
                || userPerms.getPermissionFor(Permission.DELETE_SOURCE_FILES);

        Iterator it = sourcePages.iterator();
        while (it.hasNext())
        {
            SourcePage sourcePage = (SourcePage) it.next();
            boolean hasError = sourcePage.getPageState().equals(
                    PageState.IMPORT_FAIL);

            String pageName = sourcePage.getDisplayPageName();
            String pageUrl;

            boolean isUnextractedFile = false;
            boolean isWordCountOverriden = sourcePage.isWordCountOverriden();
            if (sourcePage.getPrimaryFileType() == PrimaryFile.UNEXTRACTED_FILE)
            {
                isUnextractedFile = true;
            }
            String fileIcon = bundle.getString("img_file_extracted");
            String fileIconAlt = bundle.getString("lb_file_extracted");
            if (isUnextractedFile)
            {
                fileIcon = bundle.getString("img_file_unextracted");
                fileIconAlt = bundle.getString("lb_file_unextracted");
            }

            sb.append("<TR VALIGN=TOP>\n");
            sb.append("<TD STYLE=\"word-wrap: break-word;word-break:break-all; width:70%\">");
            sb.append("<SCRIPT language=\"javascript\">if (navigator.userAgent.indexOf(\'Firefox\') >= 0){document.write(\"<DIV style=\'width:100%\'>\");}</SCRIPT>");

            if (addCheckBox)
            {
                sb.append("<input type=\"checkbox\" name=\"pageIds\" value=\""
                        + sourcePage.getId() + "\"> &nbsp;");
            }
            sb.append("<IMG SRC=\"" + fileIcon + "\" title=\"" + fileIconAlt
                    + "\" WIDTH=13 HEIGHT=15> ");

            if (hasError || cancelled)
            {
                String textColor = hasError ? "<SPAN CLASS=\"warningText\">"
                        : "<SPAN CLASS=\"standardText\">";

                sb.append(textColor);
                sb.append(pageName);
                sb.append("</SPAN>");
                if (hasError)
                    atLeastOneError = true;
            }
            else
            {
                if (isUnextractedFile)
                {
                    UnextractedFile uf = (UnextractedFile) sourcePage
                            .getPrimaryFile();
                    pageUrl = WebAppConstants.UNEXTRACTED_FILES_URL_MAPPING
                            + uf.getStoragePath();
                }
                else
                {
                    pageUrl = "&" + WebAppConstants.SOURCE_PAGE_ID + "="
                            + sourcePage.getId() + "&" + WebAppConstants.JOB_ID
                            + "=" + p_job.getJobId();
                }

                if (pagenameDisplay.equals(PAGENAME_DISPLAY_FULL))
                {
                    printPageLink(sb, pageName, pageUrl, isUnextractedFile,
                            editAllowed);
                }
                else if (pagenameDisplay.equals(PAGENAME_DISPLAY_SHORT))
                {
                    printPageLinkShort(sb, pageName,
                            sourcePage.getShortPageName(), pageUrl,
                            isUnextractedFile, editAllowed);
                }
            }
            sb.append("<SCRIPT language=\"javascript\">if (navigator.userAgent.indexOf(\'Firefox\') >= 0){document.write(\"</DIV>\")}</SCRIPT>");
            sb.append("</TD>\n");

            // file profile type
            sb.append("<TD ALIGN=\"center\" style=\"padding-center: 8px;\"> "
                    + "<SPAN CLASS=\"standardText\">");
            sb.append(getDataSourceName(sourcePage));
            sb.append("</SPAN></TD>\n");

            sb.append("<TD ALIGN=\"center\" style=\"padding-right: 8px;\" >"
                    + "<SPAN CLASS=\"standardText\">");
            if (isWordCountOverriden)
            {
                sb.append("<B><I>");

            }
            sb.append(sourcePage.getWordCount());
            if (isWordCountOverriden)
            {
                sb.append("</B></I>");
                wordCountOverridenAtAll = true;
            }

            sb.append("</SPAN></TD>\n");

            // Add a link to view the source file content.
            sb.append("<TD ALIGN=\"center\" style=\"padding-right: 8px;\" >"
                    + "<SPAN CLASS=\"standardText\">");
            sb.append("<A CLASS=\"standardHREF\" HREF=\"");
            StringBuffer sourceSb = new StringBuffer()
                    .append("/globalsight")
                    .append(WebAppConstants.VIRTUALDIR_CXEDOCS2)
                    .append(CompanyWrapper.getCompanyNameById(sourcePage
                            .getCompanyId())).append("/");
            String externalPageId = sourcePage.getExternalPageId();

            externalPageId = SourcePage.filtSpecialFile(externalPageId);
            sourceSb = sourceSb.append(externalPageId);
            String s = sourceSb.toString();
            s = s.replace("\\", "/");
            try
            {
                s = UrlUtil.encode(s, "utf-8");
            }
            catch (Exception e)
            {
                s = URLEncoder.encode(s, "utf-8");
            }
            s = s.replace("%2F", "/");
            sb.append(s);
            sb.append("\" target=\"_blank\">");
            sb.append(bundle.getString("lb_click_to_view"));
            sb.append("</A></SPAN></TD>\n");
            sb.append("</TR>\n");
        }

        addAddingPages(sb, aSourcdPages, pagenameDisplay, bundle, addCheckBox);
        addUpdatedPages(sb, uSourcdPages, pagenameDisplay, bundle, addCheckBox);

        if (atLeastOneError)
        {
            String errString = bundle.getString("action_view_errors") + "...";
            sb.append("<TR><TD colspan='2' align='right'><INPUT CLASS='standardText' TYPE='BUTTON' NAME='PageError' VALUE='"
                    + errString
                    + "' ONCLICK=\"submitForm(\'PageError\');\"></TD></TR>");
        }

        sb.append("</table>\n</div>\n</TD>\n</TR>");
        boolean download = userPerms
                .getPermissionFor(Permission.JOB_FILES_DOWNLOAD);
        if (addCheckBox || download)
        {
            sb.append("<TR VALIGN=TOP>\n<TD COLSPAN='3'>");

            if (addCheckBox)
            {
                /*
                 * for bug gbs-2599,and the javascript function quote a wrong
                 * attribute:attributeForm sb.append(
                 * "<DIV ID=\"CheckAllLayer\" style=\"float: left; margin-left:3px; margin-top:8px;\">"
                 * ); sb.append(
                 * "<A CLASS=\"standardHREF\" HREF=\"javascript:checkAllWithName('attributeForm', 'pageIds');\">"
                 * ); sb.append(bundle.getString("lb_check_all"));
                 * sb.append("</A> | "); sb.append(
                 * "<A CLASS=\"standardHREF\" HREF=\"javascript:clearAll('attributeForm');\">"
                 * ); sb.append(bundle.getString("lb_clear_all"));
                 * sb.append("</DIV>\n");
                 */
            }

            sb.append("</TD><TD align=\"right\">");
            if (download)
            {
                String downloadUrl = selfBean.getPageURL() + "&action="
                        + DOWNLOAD_SOURCE_PAGES;
                sb.append("<input type=\"Button\"");
                if (sourcePages.size() == 0)
                {
                    sb.append(" disabled ");
                }

                sb.append("value=\"");
                sb.append(bundle.getString("lb_download_files_in_job_detail"));
                sb.append("\" onClick=\"location.href='");
                sb.append(downloadUrl);
                sb.append("'\"/>");

            }
            sb.append("</TD></TR>");
        }

        // // add download all source pages.
        // if (userPerms.getPermissionFor(Permission.JOB_FILES_DOWNLOAD))
        // {
        // String downloadUrl = m_selfBean.getPageURL() + "&action="
        // + DOWNLOAD_SOURCE_PAGES;
        // sb.append("<TR VALIGN=TOP>\n");
        // sb.append("<TD></TD><TD></TD><TD></TD>");
        // sb.append("<TD> <input type=\"Button\" value=\"");
        // sb.append(bundle.getString("lb_download_files_in_job_detail"));
        // sb.append("\" onClick=\"location.href='");
        // sb.append(downloadUrl);
        // sb.append("'\"/> </TD></TR>");
        // }

        // add a line to put the total underneath
        sb.append("<TR height=3></TR>");
        sb.append("<TR><TD height=1 BGCOLOR=\"000000\" COLSPAN=4></TD></TR>");
        sb.append("<TR height=3></TR>");

        sb.append("<TR VALIGN=TOP>\n");
        sb.append("<TD STYLE=\"word-wrap: break-word;word-break:break-all\">");
        sb.append("<SPAN CLASS=\"standardText\">");
        sb.append(bundle.getString("lb_source_word_count_total"));

        sb.append("</SPAN>");
        sb.append("</TD>\n");
        sb.append("<TD> </TD>\n");
        sb.append("<TD ALIGN=\"RIGHT\" style=\"padding-right: 8px;\" ><SPAN CLASS=\"standardText\">");

        if (p_job.isWordCountOverriden())
        {
            sb.append("<B><I>");
        }
        sb.append(p_job.getWordCount());
        if (p_job.isWordCountOverriden())
        {
            sb.append("</B></I>");
            wordCountOverridenAtAll = true;
        }

        // add allowing the source word count to be modified if job in active
        // state
        if (p_job.getState().equals(Job.DISPATCHED)
                || p_job.getState().equals(Job.PENDING)
                || p_job.getState().equals(Job.READY_TO_BE_DISPATCHED)
                || p_job.getState().equals(Job.BATCHRESERVED)
                || p_job.getState().equals(Job.LOCALIZED))
        {
            if (editAllowed
                    && userPerms
                            .getPermissionFor(Permission.JOB_SOURCE_WORDCOUNT_TOTAL))
            {
                String editTotalWcUrl = editSourcePageWcBean.getPageURL();
                sb.append(" (");
                sb.append("<A HREF=\"" + editTotalWcUrl
                        + "\" CLASS=standardHREFDetail>");
                sb.append(bundle.getString("lb_edit"));
                sb.append("</A>");
                sb.append(")");
            }
        }
        sb.append("</SPAN></TD><TD></TD></TR>");

        // For AMB-116 Add the source file number into job detail page issue
        sb.append("<TR ID=\'fileCounts\'>");
        sb.append("<TD STYLE=\'word-wrap: break-word;word-break:break-all\'>");
        sb.append("<SPAN CLASS=\"standardText\">");
        sb.append(bundle.getString("lb_primary_source_files_number"));
        sb.append("</SPAN>");
        sb.append("</TD>\n");
        sb.append("<TD> </TD>\n");
        sb.append("<TD ALIGN=\'RIGHT\' style=\'padding-right: 8px;\' >");
        sb.append("<SPAN CLASS=\'standardText\'>");
        sb.append(sourcePages.size());
        sb.append("</SPAN>");
        sb.append("</TD");
        sb.append("<TD> </TD>\n");
        sb.append("</TR>");

        if (wordCountOverridenAtAll)
        {
            // text explainging about the bold and italics
            sb.append("<P><TR><TD>");
            sb.append("<SPAN CLASS=\"smallTextGray\">");
            sb.append("<B><I>");
            sb.append(bundle.getString("helper_text_override_word_count"));
            sb.append("</SPAN>");
            sb.append("</TD><TD></TD>\n</TR><P>");
        }
        return sb.toString();
    }

    private void addAddingPages(StringBuffer sb,
            List<AddingSourcePage> aSourcdPages, String pagenameDisplay,
            ResourceBundle bundle, boolean addCheckBox)
    {
        String fileIcon = "/globalsight/images/file_update.gif";

        for (AddingSourcePage page : aSourcdPages)
        {
            String pageName = page.getDisplayPageName();
            if (pagenameDisplay.equals(PAGENAME_DISPLAY_SHORT))
            {
                pageName = page.getShortPageName();
            }

            pageName = pageName.replace("\\", "/");
            pageName = pageName.replace("/", File.separator);

            String fileIconAlt = bundle.getString("lb_file_adding");

            sb.append("<TR VALIGN=TOP>\n");
            sb.append("<TD STYLE=\"word-wrap: break-word;word-break:break-all; width:70%\">");
            sb.append("<SCRIPT language=\"javascript\">if (navigator.userAgent.indexOf(\'Firefox\') >= 0){document.write(\"<DIV style=\'width:500px\'>\");}</SCRIPT>");
            if (addCheckBox)
            {
                sb.append("<input type=\"checkbox\" name=\"notCheck\" value=\""
                        + page.getId() + "\" disabled> &nbsp;");
            }

            sb.append("<IMG SRC=\"" + fileIcon + "\" title=\"" + fileIconAlt
                    + "\" WIDTH=13 HEIGHT=15> ");
            sb.append("<SPAN CLASS=\"standardText\" style=\"color:gray\">"
                    + pageName + "</span>");
            sb.append("<SCRIPT language=\"javascript\">if (navigator.userAgent.indexOf(\'Firefox\') >= 0){document.write(\"</DIV>\")}</SCRIPT>");
            sb.append("</TD>\n");

            // file profile type
            sb.append("<TD ALIGN=\"CENTER\" style=\"padding-center: 8px;\"> "
                    + "<SPAN CLASS=\"standardText\" style=\"color:gray\"> ");
            sb.append(page.getDataSource());
            sb.append("</SPAN></TD>\n");

            sb.append("<TD ALIGN=\"RIGHT\" style=\"padding-right: 8px;\" >"
                    + "<SPAN CLASS=\"standardText\">");
            sb.append("--");

            sb.append("</SPAN></TD>\n");

            sb.append("<TD ALIGN=\"RIGHT\" style=\"padding-right: 8px;\" >"
                    + "<SPAN CLASS=\"standardText\" style=\"color:gray\">");
            sb.append(bundle.getString("lb_adding_file_status"));
            sb.append("</SPAN></TD>\n");

            sb.append("</TR>\n");
        }
    }

    private void addUpdatedPages(StringBuffer sb,
            List<UpdatedSourcePage> uSourcdPages, String pagenameDisplay,
            ResourceBundle bundle, boolean addCheckBox)
    {
        String fileIcon = "/globalsight/images/file_update.gif";

        for (UpdatedSourcePage page : uSourcdPages)
        {
            String pageName = page.getDisplayPageName();
            if (pagenameDisplay.equals(PAGENAME_DISPLAY_SHORT))
            {
                pageName = page.getShortPageName();
            }

            pageName = pageName.replace("\\", "/");
            pageName = pageName.replace("/", File.separator);

            String fileIconAlt = bundle.getString("lb_file_updating");

            sb.append("<TR VALIGN=TOP>\n");
            sb.append("<TD STYLE=\"word-wrap: break-word;word-break:break-all; width:70%\">");
            sb.append("<SCRIPT language=\"javascript\">if (navigator.userAgent.indexOf(\'Firefox\') >= 0){document.write(\"<DIV style=\'width:500px\'>\");}</SCRIPT>");
            if (addCheckBox)
            {
                sb.append("<input type=\"checkbox\" name=\"notCheck\" value=\""
                        + page.getId() + "\" disabled> &nbsp;");
            }

            sb.append("<IMG SRC=\"" + fileIcon + "\" title=\"" + fileIconAlt
                    + "\" WIDTH=13 HEIGHT=15> ");
            sb.append("<SPAN CLASS=\"standardText\" style=\"color:gray\">"
                    + pageName + "</span>");
            sb.append("<SCRIPT language=\"javascript\">if (navigator.userAgent.indexOf(\'Firefox\') >= 0){document.write(\"</DIV>\")}</SCRIPT>");
            sb.append("</TD>\n");

            // file profile type
            sb.append("<TD ALIGN=\"CENTER\" style=\"padding-center: 8px;\"> "
                    + "<SPAN CLASS=\"standardText\" style=\"color:gray\"> ");
            sb.append(page.getDataSource());
            sb.append("</SPAN></TD>\n");

            sb.append("<TD ALIGN=\"RIGHT\" style=\"padding-right: 8px;\" >"
                    + "<SPAN CLASS=\"standardText\">");
            sb.append("--");

            sb.append("</SPAN></TD>\n");

            sb.append("<TD ALIGN=\"RIGHT\" style=\"padding-right: 8px;\" >"
                    + "<SPAN CLASS=\"standardText\" style=\"color:gray\">");
            sb.append(bundle.getString("lb_updating_file_status"));
            sb.append("</SPAN></TD>\n");

            sb.append("</TR>\n");
        }
    }

    /**
     * This method is inherited by other sub-classes to get the job detail
     * information for the UI. Some sub-classes may override it or some may use
     * it. The signature can't be changed without changing the sub-class
     * signatures.
     */
    protected void performAppropriateOperation(HttpServletRequest p_request,
            SessionManager p_sessionMgr) throws EnvoyServletException
    {
        String wfIdParam = p_request.getParameter(JobManagementHandler.WF_ID);
        if (isRefresh(p_sessionMgr, wfIdParam, JobManagementHandler.WF_ID)
                && isSameAction(p_sessionMgr, p_request, wfIdParam))
        {
            return;
        }
        String wfId = null;
        if (p_request.getParameter(JobManagementHandler.DISCARD_WF_PARAM) != null)
        {
            p_sessionMgr.setAttribute(JobManagementHandler.WF_ID, wfIdParam);
            p_sessionMgr.setAttribute(JobManagementHandler.WF_PREVIOUS_ACTION,
                    JobManagementHandler.DISCARD_WF_PARAM);
            // Discard the selected workflows
            StringTokenizer tokenizer = new StringTokenizer(wfIdParam);
            while (tokenizer.hasMoreTokens())
            {
                wfId = tokenizer.nextToken();
                String userId = ((User) p_sessionMgr
                        .getAttribute(WebAppConstants.USER)).getUserId();

                WorkflowHandlerHelper.cancelWF(userId, WorkflowHandlerHelper
                        .getWorkflowById(Long.parseLong(wfId)));
            }
            p_sessionMgr.setAttribute(JobManagementHandler.ADDED_WORKFLOWS,
                    null);
        }
        else if (p_request.getParameter(JobManagementHandler.DISPATCH_WF_PARAM) != null)
        {
            p_sessionMgr.setAttribute(JobManagementHandler.WF_ID, wfIdParam);
            // Dispatch the selected workflows
            StringTokenizer tokenizer = new StringTokenizer(wfIdParam);
            while (tokenizer.hasMoreTokens())
            {
                wfId = tokenizer.nextToken();
                WorkflowHandlerHelper.dispatchWF(WorkflowHandlerHelper
                        .getWorkflowById(Long.parseLong(wfId)));
            }

            p_sessionMgr.setAttribute(JobManagementHandler.WF_PREVIOUS_ACTION,
                    JobManagementHandler.DISPATCH_WF_PARAM);
        }
        else if (p_request
                .getParameter(JobManagementHandler.DISPATCH_ALL_WF_PARAM) != null)
        {
            String readyWorkflowIds = p_request
                    .getParameter(JobManagementHandler.ALL_READY_WORKFLOW_IDS);
            if (readyWorkflowIds != null && readyWorkflowIds.length() > 0)
            {
                for (String id : readyWorkflowIds.split(","))
                {
                    WorkflowHandlerHelper.dispatchWF(WorkflowHandlerHelper
                            .getWorkflowById(Long.parseLong(id)));
                }
            }
        }
        else if (p_request
                .getParameter(JobManagementHandler.UPDATE_WORD_COUNTS) != null)
        {
            if (wfIdParam != null)
            {
                updateWordCounts(wfIdParam);
                p_request.setAttribute("isUpdatingWordCounts", "yes");
                return;
            }
        }
        else if (p_request.getParameter(JobManagementHandler.ARCHIVE_WF_PARAM) != null)
        {
            p_sessionMgr.setAttribute(JobManagementHandler.WF_ID, wfIdParam);
            // Archive the selected workflows
            StringTokenizer tokenizer = new StringTokenizer(wfIdParam);
            while (tokenizer.hasMoreTokens())
            {
                wfId = tokenizer.nextToken();
                Workflow wf = WorkflowHandlerHelper.getWorkflowById(Long
                        .parseLong(wfId));
                WorkflowHandlerHelper.archiveWorkflow(wf);
            }

            p_sessionMgr.setAttribute(JobManagementHandler.WF_PREVIOUS_ACTION,
                    JobManagementHandler.ARCHIVE_WF_PARAM);
        }
        else if (p_request.getParameter(JobManagementHandler.ASSIGN_PARAM) != null)
        {
            if ("saveAssign".equalsIgnoreCase(p_request
                    .getParameter(JobManagementHandler.ASSIGN_PARAM)))
            {
                doSaveAssign(p_request, p_sessionMgr);
            }

            p_sessionMgr.setAttribute(JobManagementHandler.WF_ID, null);
        }
        else if (p_request.getParameter(JobManagementHandler.SKIP_PARAM) != null)
        {
            if (FormUtil.isNotDuplicateSubmisson(p_request,
                    FormUtil.Forms.SKIP_ACTIVITIES))
            {
                doSkip(p_request, p_sessionMgr);
            }
        }
        if (p_request.getParameter(JobManagementHandler.ADD_WF_PARAM) != null)
        {
            // Get a comma separated string of WorkflowTemplatInfo ids
            String buf = p_request
                    .getParameter(JobManagementHandler.ADD_WF_PARAM);
            if (isRefresh(p_sessionMgr, buf,
                    JobManagementHandler.ADDED_WORKFLOWS))
            {
                return;
            }
            long jobId = getJobId(p_request, p_sessionMgr);
            // first validate the state of the existing pages of the job
            WorkflowHandlerHelper.validateStateOfPagesByJobId(jobId);

            p_sessionMgr
                    .setAttribute(JobManagementHandler.ADDED_WORKFLOWS, buf);
            // Convert to List
            String[] wfInfosArray = buf.split(",");
            ArrayList<Long> wfInfos = new ArrayList<Long>();
            for (int i = 0; i < wfInfosArray.length; i++)
            {
                wfInfos.add(Long.decode(wfInfosArray[i]));
            }
            try
            {
                WorkflowAdditionSender sender = new WorkflowAdditionSender(
                        wfInfos, jobId);
                sender.sendToAddWorkflows();
            }
            catch (Exception e)
            {
                throw new EnvoyServletException(e);
            }
            List<SourcePage> sps = new ArrayList<SourcePage>();
            try
            {
                sps.addAll(ServerProxy.getJobHandler().getJobById(jobId)
                        .getSourcePages());
            }
            catch (Exception e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            List<String> targetLocales = new ArrayList<String>();
            for (Long workflowId : wfInfos)
            {
                try
                {
                    targetLocales.add(ServerProxy.getProjectHandler()
                            .getWorkflowTemplateInfoById(workflowId)
                            .getTargetLocale().toString());
                }
                catch (Exception e)
                {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }

            copyFilesToTargetDir(sps, targetLocales);
        }
        else
        {
            // Don't do anything if they are just viewing the table
            // and not performing an action on a workflow
            return;
        }
    }

    private boolean isSameAction(SessionManager p_sessionMgr,
            HttpServletRequest p_request, String wfIdParam)
    {
        String preAction = (String) p_sessionMgr
                .getAttribute(JobManagementHandler.WF_PREVIOUS_ACTION);

        if (p_request.getParameter(JobManagementHandler.DISCARD_WF_PARAM) != null)
        {
            if (JobManagementHandler.DISCARD_WF_PARAM.equals(preAction))
            {
                return true;
            }
            else
            {
                return false;
            }
        }

        return true;
    }

    /**
     * Set the navigation beans into Request and initialize some beans.
     *
     * @param p_request
     * @param pageName
     */
    private void setNavBeansIntoRequest(HttpServletRequest p_request,
            String pageName)
    {
        this.baseBean = new NavigationBean(JobManagementHandler.DETAILS_BEAN,
                pageName);
        this.editEstimatedCompletionDateBean = new NavigationBean(ECD_BEAN,
                pageName);
        this.editEstimatedTranslateCompletionDateBean = new NavigationBean(
                ETCD_BEAN, pageName);

        this.selfBean = new NavigationBean(JobManagementHandler.SELF_BEAN,
                pageName);
        p_request.setAttribute(JobManagementHandler.SELF_BEAN, selfBean);

        this.wordCountListBean = new NavigationBean(WORDCOUNTLIST_BEAN,
                pageName);
        p_request.setAttribute(WORDCOUNTLIST_BEAN, wordCountListBean);

        this.downloadBean = new NavigationBean(DOWNLOAD_BEAN, pageName);
        p_request.setAttribute(DOWNLOAD_BEAN, downloadBean);

        this.assignBean = new NavigationBean(ASSIGN_BEAN, pageName);
        p_request.setAttribute(ASSIGN_BEAN, assignBean);

        this.viewCommentsBean = new NavigationBean(
                WorkflowCommentsHandler.VIEW_COMMENTS_BEAN, pageName);
        p_request.setAttribute(WF_COMMENTS_BEAN, viewCommentsBean);

        this.editSourcePageWcBean = new NavigationBean(
                JobManagementHandler.EDIT_SOURCE_PAGE_WC_BEAN, pageName);
        p_request.setAttribute(JobManagementHandler.EDIT_SOURCE_PAGE_WC_BEAN,
                editSourcePageWcBean);

        this.modifyBean = new NavigationBean(JobManagementHandler.MODIFY_BEAN,
                pageName);
        p_request.setAttribute(JobManagementHandler.MODIFY_BEAN, modifyBean);

        NavigationBean updateLeverageBean = new NavigationBean(UPDATE_LEVERAGE,
                pageName);
        p_request.setAttribute(UPDATE_LEVERAGE, updateLeverageBean);

        NavigationBean pendingBean = new NavigationBean(
                JobManagementHandler.PENDING_BEAN, pageName);
        p_request.setAttribute(JobManagementHandler.PENDING_BEAN, pendingBean);

        NavigationBean detailsBean = new NavigationBean(
                JobManagementHandler.DETAILS_BEAN, pageName);
        p_request.setAttribute(JobManagementHandler.DETAILS_BEAN, detailsBean);

        // beans for searching/sorting the list of source pages
        NavigationBean pageListBean = new NavigationBean(
                JobManagementHandler.DETAILS_BEAN, pageName);
        p_request.setAttribute(JobManagementHandler.PAGE_LIST_BEAN,
                pageListBean);

        NavigationBean pageSearchBean = new NavigationBean(
                JobManagementHandler.DETAILS_BEAN, pageName);
        p_request.setAttribute(JobManagementHandler.PAGE_SEARCH_BEAN,
                pageSearchBean);

        NavigationBean changeCurrencyBean = new NavigationBean(
                JobManagementHandler.CHANGE_CURRENCY_BEAN, pageName);
        p_request.setAttribute(JobManagementHandler.CHANGE_CURRENCY_BEAN,
                changeCurrencyBean);

        NavigationBean worflowActivities = new NavigationBean(WF_DETAILS_BEAN,
                pageName);
        p_request.setAttribute(WF_DETAILS_BEAN, worflowActivities);

        NavigationBean editPagesBean = new NavigationBean(
                JobManagementHandler.EDIT_PAGES_BEAN, pageName);
        p_request.setAttribute(JobManagementHandler.EDIT_PAGES_BEAN,
                editPagesBean);

        NavigationBean editTotalSourcePageWcBean = new NavigationBean(
                JobManagementHandler.EDIT_TOTAL_SOURCE_PAGE_WC_BEAN, pageName);
        p_request.setAttribute(
                JobManagementHandler.EDIT_TOTAL_SOURCE_PAGE_WC_BEAN,
                editTotalSourcePageWcBean);

        NavigationBean editFinalCostBean = new NavigationBean(
                JobManagementHandler.EDIT_FINAL_COST_BEAN, pageName);
        p_request.setAttribute(JobManagementHandler.EDIT_FINAL_COST_BEAN,
                editFinalCostBean);

        NavigationBean addWorkflowBean = new NavigationBean(
                JobManagementHandler.ADD_WF_BEAN, pageName);
        p_request.setAttribute(JobManagementHandler.ADD_WF_BEAN,
                addWorkflowBean);

        NavigationBean surchargesBean = new NavigationBean(
                JobManagementHandler.SURCHARGES_BEAN, pageName);
        p_request.setAttribute(JobManagementHandler.SURCHARGES_BEAN,
                surchargesBean);

        NavigationBean exportErrorBean = new NavigationBean(EXPORT_ERROR_BEAN,
                pageName);
        p_request.setAttribute(EXPORT_ERROR_BEAN, exportErrorBean);

        NavigationBean wfImportErrorBean = new NavigationBean(
                WF_IMPORT_ERROR_BEAN, pageName);
        p_request.setAttribute(WF_IMPORT_ERROR_BEAN, wfImportErrorBean);

        NavigationBean exportBean = new NavigationBean(
                JobManagementHandler.EXPORT_BEAN, pageName);
        p_request.setAttribute(JobManagementHandler.EXPORT_BEAN, exportBean);

        NavigationBean jobCommentsBean = new NavigationBean(
                JobManagementHandler.JOB_COMMENTS_BEAN, pageName);
        p_request.setAttribute(JobManagementHandler.JOB_COMMENTS_BEAN,
                jobCommentsBean);

        NavigationBean rateVendorBean = new NavigationBean(WF_RATE_VENDOR_BEAN,
                pageName);
        p_request.setAttribute(WF_RATE_VENDOR_BEAN, rateVendorBean);

        NavigationBean editorBean = new NavigationBean(EDITOR_BEAN, pageName);
        p_request.setAttribute(EDITOR_BEAN, editorBean);

        NavigationBean sourceEditorBean = new NavigationBean(
                SOURCE_EDITOR_BEAN, pageName);
        p_request.setAttribute(SOURCE_EDITOR_BEAN, sourceEditorBean);

        NavigationBean jobAttributesBean = new NavigationBean(
                JOB_ATTRIBUTE_BEAN, pageName);
        p_request.setAttribute(JOB_ATTRIBUTE_BEAN, jobAttributesBean);

        NavigationBean jobReports = new NavigationBean(JOB_REPORT_BEAN,
                pageName);
        p_request.setAttribute(JOB_REPORT_BEAN, jobReports);

        NavigationBean addSourceFiles = new NavigationBean(ADD_SOURCE_FILES,
                pageName);
        p_request.setAttribute(ADD_SOURCE_FILES, addSourceFiles);

        NavigationBean allStatusBean = new NavigationBean(ALL_STATUS, pageName);
        p_request.setAttribute(ALL_STATUS, allStatusBean);

        NavigationBean skipBean = new NavigationBean(SKIP_BEAN, pageName);
        p_request.setAttribute(SKIP_BEAN, skipBean);
    }

    /**
     * Update word counts for specified workflow and target pages of this
     * workflow based on latest leverage matches.
     *
     * @param p_workflowId
     */
    private void updateWordCounts(final String p_workflowIds)
    {
        Runnable runnable = new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    List<Long> wfIds = new ArrayList<Long>();
                    wfIds = UpdateLeverageHelper.getWfIds(p_workflowIds);

                    long jobId = -1;
                    int wfNumber = wfIds.size();
                    if (wfNumber > 0)
                    {
                        Workflow wf = ServerProxy.getWorkflowManager()
                                .getWorkflowById(wfIds.get(0));
                        jobId = wf.getJob().getId();
                        // Initialize this job's percentage to 0.
                        updateWordCountsPercentageMap.put(jobId, 0);
                    }

                    int count = 0;
                    for (Iterator it = wfIds.iterator(); it.hasNext();)
                    {
                        Workflow wf = ServerProxy.getWorkflowManager()
                                .getWorkflowById((Long) it.next());

                        TranslationMemoryProfile tmProfile = wf.getJob()
                                .getL10nProfile().getTranslationMemoryProfile();
                        Vector<String> jobExcludeTuTypes = tmProfile
                                .getJobExcludeTuTypes();

                        StatisticsService.calculateTargetPagesWordCount(wf,
                                jobExcludeTuTypes);

                        List<Workflow> wfList = new ArrayList<Workflow>();
                        wfList.add(wf);
                        StatisticsService.calculateWorkflowStatistics(wfList,
                                jobExcludeTuTypes);

                        count++;
                        updateWordCountsPercentageMap.put(jobId,
                                Math.round(count * 100 / wfNumber));
                    }
                    // Add this to ensure the progressBar will go to end 100.
                    updateWordCountsPercentageMap.put(jobId, 100);
                }
                catch (Exception e)
                {
                    throw new EnvoyServletException(e);
                }
                finally
                {
                    SegmentTuTuvCacheManager.clearCache();
                }
            }
        };

        Thread t = new MultiCompanySupportedThread(runnable);
        t.setName("Update WordCounts for workflows: " + p_workflowIds);
        t.start();
    }

    /**
     * Get values from request and session. Assign the selected user to the
     * task.
     */
    private void doSaveAssign(HttpServletRequest p_request,
            SessionManager p_sessionMgr) throws EnvoyServletException
    {
        try
        {
            String srcLocale = (String) p_sessionMgr.getAttribute("srcLocale");
            String targLocale = (String) p_sessionMgr
                    .getAttribute("targLocale");
            Hashtable taskUserHash = (Hashtable) p_sessionMgr
                    .getAttribute("taskUserHash");
            String wfId = (String) p_sessionMgr.getAttribute("wfId");

            Enumeration keys = taskUserHash.keys();
            HashMap roleMap = new HashMap();
            while (keys.hasMoreElements())
            {
                Task task = (Task) keys.nextElement();
                String taskId = String.valueOf(task.getId());
                Activity activity = ServerProxy.getJobHandler()
                        .getActivityByCompanyId(task.getTaskName(),
                                String.valueOf(task.getCompanyId()));
                ContainerRole containerRole = ServerProxy.getUserManager()
                        .getContainerRole(activity, srcLocale, targLocale);
                String userParam = p_request.getParameter("users" + taskId);
                /*
                 * userParam =
                 * userId1,user1Name:userId2,user2Name:userId3,user3Name: when
                 * the above parameters are split on ":", it gives users
                 * users[0] = userId1, user1Name users[1] = userId1, user1Name
                 * users[2] = userId1, user1Name It's required to split users on
                 * "," to get userInfo. So userInfos[0] = "userId1" and
                 * userInfos[1]=user1Name userInfos[0] = "userId2" and
                 * userInfos[1]=user2Name userInfos[0] = "userId3" and
                 * userInfos[1]=user3Name
                 */
                String[] users = userParam.split(":");
                String[] userInfos = null;
                Vector newAssignees = new Vector();
                String[] roles = new String[users.length];
                String displayRole = "";
                for (int k = 0; k < users.length; k++)
                {
                    userInfos = users[k].split(",");
                    roles[k] = containerRole.getName() + " " + userInfos[0];
                    if (k == users.length - 1)
                    {
                        displayRole += userInfos[1];
                    }
                    else
                    {
                        displayRole += userInfos[1] + ",";
                    }
                }
                newAssignees.addElement(new NewAssignee(roles, displayRole,
                        true));
                roleMap.put(taskId, newAssignees);
            }

            boolean shouldModifyWf = false;
            Long id = Long.valueOf(wfId);
            WorkflowInstance wi = ServerProxy.getWorkflowServer()
                    .getWorkflowInstanceById(id.longValue());

            Vector tasks = wi.getWorkflowInstanceTasks();

            int sz = tasks == null ? -1 : tasks.size();
            for (int j = 0; j < sz; j++)
            {
                WorkflowTaskInstance wti = (WorkflowTaskInstance) tasks.get(j);
                Vector newAssignees = (Vector) roleMap.get(String.valueOf(wti
                        .getTaskId()));

                if (newAssignees != null)
                {
                    for (int r = 0; r < newAssignees.size(); r++)
                    {
                        NewAssignee na = (NewAssignee) newAssignees
                                .elementAt(r);
                        if (na != null
                                && !areSameRoles(wti.getRoles(), na.m_roles))
                        {
                            shouldModifyWf = true;
                            wti.setRoleType(na.m_isUserRole);
                            wti.setRoles(na.m_roles);
                            wti.setDisplayRoleName(na.m_displayRoleName);
                        }
                    }
                }

            }

            // modify one workflow at a time and reset the flag
            if (shouldModifyWf)
            {
                shouldModifyWf = false;
                ServerProxy.getWorkflowManager().modifyWorkflow(null, wi, null,
                        null);
            }
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }
    }

    /**
     * Does the skip job for the activities.
     *
     * @param p_request
     * @param p_sessionMgr
     * @throws EnvoyServletException
     */
    private void doSkip(HttpServletRequest p_request,
            SessionManager p_sessionMgr) throws EnvoyServletException
    {

        final User user = (User) p_sessionMgr
                .getAttribute(WebAppConstants.USER);
        final List<Entry> list = getSkipParameter(p_request);

        if (list == null)
        {
            return;
        }

        Runnable runnable = new Runnable()
        {
            @Override
            public void run()
            {
                // run the skip process in a new thread in order to return to
                // job detail page quickly without waiting for the skip process
                // to be completed
                try
                {
                    ServerProxy.getWorkflowManager().setSkip(list,
                            user.getUserId());
                }
                catch (Exception e)
                {
                    CATEGORY.error("Skip activity error", e);
                    throw new EnvoyServletException(e);
                }
            }
        };
        Thread t = new MultiCompanySupportedThread(runnable);
        t.start();
    }

    private List<Entry> getSkipParameter(HttpServletRequest p_request)
    {
        String[] workflowIds = p_request.getParameterValues("workflowId");

        if (workflowIds == null)
        {
            return null;
        }

        List<Entry> list = new ArrayList<Entry>(workflowIds.length);

        String activity;
        Entry<String, String> entry;
        for (String workflowId : workflowIds)
        {
            activity = p_request.getParameter("activity" + workflowId);
            entry = new Entry<String, String>(workflowId, activity);
            entry.setHelp(p_request.getParameter("activity_" + workflowId));
            list.add(entry);
        }
        return list;
    }

    /**
     * Outputs a page name using the full path. See also
     * envoy/tasks/taskDetail.jsp.
     */
    private void printPageLink(StringBuffer p_sb, String p_page, String p_url,
            boolean isUnExtracted, boolean editAllowed)
    {
        if (editAllowed)
        {
            if (isUnExtracted)
            {
                p_sb.append("<a class=\"standardHREF\" href=\"");
            }
            else
            {
                p_sb.append("<a href='#' onclick=\"openViewerWindow('");
            }

            String new_url = p_url.replace('\\', '/');
            p_sb.append(new_url);
            if (isUnExtracted)
            {
                p_sb.append("\" target=\"_blank\">");
            }
            else
            {
                p_sb.append("'); return false;\" oncontextmenu=\"contextForPage('");
                p_sb.append(new_url);
                p_sb.append("', event)\" onfocus='this.blur();'");
                p_sb.append(" CLASS='standardHREF'>");
            }
        }
        p_sb.append(p_page);

        if (editAllowed)
        {
            p_sb.append("</a>");
        }
    }

    /**
     * Outputs a page name as file name with full path in a tooltip. See also
     * envoy/tasks/taskDetail.jsp.
     */
    private void printPageLinkShort(StringBuffer p_sb, String p_page,
            String p_shortName, String p_url, boolean isUnExtracted,
            boolean editAllowed)
    {
        // Preserve any MsOffice prefixes: (header) en_US/foo/bar.ppt but
        // show them last so the main file names are grouped together
        if (editAllowed)
        {
            if (isUnExtracted)
            {
                p_sb.append("<a href=\"");
            }
            else
            {
                p_sb.append("<a href='#' onclick=\"openViewerWindow('");
            }
            p_sb.append(p_url);

            if (isUnExtracted)
            {
                p_sb.append("\"");
            }
            else
            {
                p_sb.append("'); return false;\" oncontextmenu=\"contextForPage('");
                p_sb.append(p_url);
                p_sb.append("', event)\" onfocus='this.blur();'");
            }
            p_sb.append(" CLASS='standardHREF' TITLE='");
            p_sb.append(p_page.replace("\'", "&apos;"));
            p_sb.append("'>");
        }
        p_sb.append(p_shortName);

        if (editAllowed)
        {
            p_sb.append("</a>");
        }
    }

    /**
     * Returns the id of the job that an action is being performed on.
     */
    private long getJobId(HttpServletRequest p_request,
            SessionManager p_sessionMgr)
    {
        long jobId = 0;
        // Get the jobId from the sessionMgr if it's not in the request.
        // This means you are coming to the Job Details screen from
        // somewhere other than the My Jobs screens.
        if (p_request.getParameterValues(WebAppConstants.JOB_ID) == null)
        {
            try
            {
                Object oJobId = p_sessionMgr
                        .getAttribute(WebAppConstants.JOB_ID);
                // The type of oJobId may be Long or String.
                if (oJobId instanceof Long)
                {
                    jobId = ((Long) oJobId).longValue();
                }
                else
                {
                    jobId = Long.parseLong((String) oJobId);
                }
            }
            catch (Exception e)
            {
                CATEGORY.error("Failed to get job id from session manager ", e);
            }
        }
        else
        {
            String[] jobIdInfo = p_request
                    .getParameterValues(JobManagementHandler.JOB_ID);
            String jobIdString = jobIdInfo[0];
            jobId = Long.parseLong(jobIdString);
            p_sessionMgr.setAttribute(JobManagementHandler.JOB_ID, new Long(
                    jobId));

        }
        return jobId;
    }

    /*
     * Update the session with this most recently used job. It will become the
     * first in the list and all the rest moved down. Also check that it wasn't
     * already in the list. Don't allow more than 3 items in the list.
     */
    private void updateMRUJob(HttpServletRequest request, HttpSession session,
            Job job, HttpServletResponse response)
    {
        String cookieName = JobSearchConstants.MRU_JOBS_COOKIE
                + session.getAttribute(WebAppConstants.USER_NAME).hashCode();
        String jobName = job.getJobName();
        String thisJob = job.getId() + ":" + jobName;
        StringBuffer newCookie = new StringBuffer(thisJob);
        int count = 1;
        Cookie[] cookies = request.getCookies();
        if (cookies != null)
        {
            for (int i = 0; i < cookies.length; i++)
            {
                Cookie cookie = cookies[i];
                if (cookie.getName().equals(cookieName))
                {
                    String mruJobStr = cookie.getValue();
                    try
                    {
                        mruJobStr = URLDecoder.decode(mruJobStr);
                    }
                    catch (Exception e)
                    {
                        continue;
                    }

                    StringTokenizer st = new StringTokenizer(mruJobStr, "|");
                    while (st.hasMoreTokens() && count < 3)
                    {
                        String value = st.nextToken();
                        if (!value.equals(thisJob))
                        {
                            newCookie.append("|");
                            newCookie.append(value);
                            count++;
                        }
                    }
                    break;
                }
            }
        }

        session.setAttribute(JobSearchConstants.MRU_JOBS, newCookie.toString());
        String value = newCookie.toString();
        value = URLEncoder.encode(value);
        try
        {
            response.addCookie(new Cookie(cookieName, value));
        }
        catch (Exception e)
        {
            response.addCookie(new Cookie(cookieName, ""));
        }
    }

    private void removeMRUjob(HttpServletRequest request, HttpSession session,
            String thisJob, HttpServletResponse response)
    {
        String cookieName = JobSearchConstants.MRU_JOBS_COOKIE
                + session.getAttribute(WebAppConstants.USER_NAME).hashCode();
        StringBuffer newCookie = new StringBuffer();
        Cookie[] cookies = request.getCookies();
        if (cookies != null)
        {
            for (int i = 0; i < cookies.length; i++)
            {
                Cookie cookie = cookies[i];
                if (cookie.getName().equals(cookieName))
                {
                    String mruJobStr = cookie.getValue();
                    mruJobStr = URLDecoder.decode(mruJobStr);
                    StringTokenizer st = new StringTokenizer(mruJobStr, "|");
                    while (st.hasMoreTokens())
                    {
                        String value = st.nextToken();
                        if (!value.equals(thisJob))
                        {
                            newCookie.append("|");
                            newCookie.append(value);
                        }
                    }
                    break;
                }
            }
            session.setAttribute(JobSearchConstants.MRU_JOBS,
                    newCookie.toString());

            String value = newCookie.toString();
            value = URLEncoder.encode(value);
            try
            {
                response.addCookie(new Cookie(cookieName, value));
            }
            catch (Exception e)
            {
                CATEGORY.error("Failed to add cookie value: " + value, e);
                response.addCookie(new Cookie(cookieName, ""));
            }
        }
    }

    private void jobNotFound(HttpServletRequest p_request,
            HttpServletResponse p_response, ServletContext p_context, Job job)
            throws ServletException, IOException, EnvoyServletException
    {
        HttpSession session = p_request.getSession(false);
        SessionManager sessionMgr = (SessionManager) session
                .getAttribute(SESSION_MANAGER);
        ResourceBundle bundle = getBundle(session);

        String jobname = null;
        if (job == null)
        {
            jobname = Long.toString(getJobId(p_request, sessionMgr));
        }
        else
        {
            jobname = job.getJobName();
            // remove from MRU list
            removeMRUjob(p_request, session,
                    job.getId() + ":" + job.getJobName(), p_response);
        }
        p_request.setAttribute("badresults", bundle.getString("lb_job") + " "
                + jobname + " " + bundle.getString("msg_cannot_be_found"));
        // forward to the jsp page.
        RequestDispatcher dispatcher = p_context
                .getRequestDispatcher("/envoy/projects/workflows/jobSearch.jsp");

        dispatcher.forward(p_request, p_response);
    }

    /**
     * Filter the pages by their name. Compare against the filter string.
     */
    protected List filterPagesByName(HttpServletRequest p_request,
            HttpSession p_session, List p_pages)
    {
        String thisFileSearch = (String) p_request
                .getAttribute(JobManagementHandler.PAGE_SEARCH_PARAM);

        if (thisFileSearch == null)
            thisFileSearch = (String) p_session
                    .getAttribute(JobManagementHandler.PAGE_SEARCH_PARAM);

        if (thisFileSearch != null)
        {
            ArrayList filteredFiles = new ArrayList();
            for (Iterator fi = p_pages.iterator(); fi.hasNext();)
            {
                Page p = (Page) fi.next();
                if (p.getExternalPageId().indexOf(thisFileSearch) >= 0)
                {
                    filteredFiles.add(p);
                }
            }
            return filteredFiles;
        }
        else
        {
            // just return all - no filter
            return p_pages;
        }
    }

    private PageComparator getPageComparator(HttpSession p_session)
    {
        PageComparator comparator = (PageComparator) p_session
                .getAttribute(JobManagementHandler.PAGE_COMPARATOR);
        if (comparator == null)
        {
            comparator = new PageComparator(PageComparator.EXTERNAL_PAGE_ID);
            p_session.setAttribute(JobManagementHandler.PAGE_COMPARATOR,
                    comparator);
        }

        return comparator;
    }

    protected void sortPages(HttpServletRequest p_request,
            HttpSession p_session, List p_pages)
    {
        // first get job comparator from session
        PageComparator comparator = getPageComparator(p_session);

        String criteria = p_request
                .getParameter(JobManagementHandler.PAGE_SORT_PARAM);
        if (criteria != null)
        {
            int sortCriteria = Integer.parseInt(criteria);
            if (comparator.getSortColumn() == sortCriteria)
            {
                // just reverse the sort order
                comparator.reverseSortingOrder();
            }
            else
            {
                // set the sort column
                comparator.setSortColumn(sortCriteria);
            }
        }

        SortUtil.sort(p_pages, comparator);
        p_session.setAttribute(JobManagementHandler.PAGE_SORT_COLUMN,
                new Integer(comparator.getSortColumn()));
        p_session.setAttribute(JobManagementHandler.PAGE_SORT_ASCENDING,
                new Boolean(comparator.getSortAscending()));
    }

    private String getDataSourceName(SourcePage p_sp)
    {
        String dataSourceType = p_sp.getDataSourceType();
        long dataSourceId = p_sp.getRequest().getDataSourceId();

        String currentRetString;
        try
        {
            if (dataSourceType.equals("db"))
            {
                currentRetString = getDBProfilePersistenceManager()
                        .getDatabaseProfile(dataSourceId).getName();
            }
            else
            {
                currentRetString = getFileProfilePersistenceManager()
                        .readFileProfile(dataSourceId).getName();
                // If source file is XLZ,here show the XLZ file profile name
                // instead of the XLF file profile name.
                boolean isXlzReferFP = getFileProfilePersistenceManager()
                        .isXlzReferenceXlfFileProfile(currentRetString);
                if (isXlzReferFP)
                {
                    currentRetString = currentRetString.substring(0,
                            currentRetString.length() - 4);
                }
            }
        }
        catch (Exception e)
        {
            currentRetString = "Unknown";
        }
        return currentRetString;
    }

    private DatabaseProfilePersistenceManager getDBProfilePersistenceManager()
            throws Exception
    {
        return ServerProxy.getDatabaseProfilePersistenceManager();
    }

    private FileProfilePersistenceManager getFileProfilePersistenceManager()
            throws Exception
    {
        return ServerProxy.getFileProfilePersistenceManager();
    }

    private int getIssueCount(Job job, HttpSession session, List<String> states)
            throws EnvoyServletException
    {
        List<Long> targetPageIds = new ArrayList<Long>();
        ArrayList workflows = new ArrayList(job.getWorkflows());
        for (int i = 0; i < workflows.size(); i++)
        {
            Workflow wf = (Workflow) workflows.get(i);
            if (wf.getState().equals(Workflow.CANCELLED))
            {
                continue;
            }

            List pages = wf.getTargetPages();
            for (int j = 0; j < pages.size(); j++)
            {
                TargetPage tPage = (TargetPage) pages.get(j);
                String state = tPage.getPageState();
                if (!PageState.IMPORT_FAIL.equals(state))
                {
                    targetPageIds.add(tPage.getId());
                }
            }
        }
        if (targetPageIds.size() == 0)
        {
            return 0;
        }

        try
        {
            CommentManager manager = ServerProxy.getCommentManager();
            return manager.getIssueCount(Issue.TYPE_SEGMENT, targetPageIds,
                    states);
        }
        catch (Exception ex)
        {
            throw new EnvoyServletException(ex);
        }
    }

    /**
     * Determines whether the two array of roles contain the same set of role
     * names.
     */
    private boolean areSameRoles(String[] p_workflowRoles,
            String[] p_selectedRoles)
    {
        // First need to sort since Arrays.equals() requires
        // the parameters to be sorted
        Arrays.sort(p_workflowRoles);
        Arrays.sort(p_selectedRoles);
        return Arrays.equals(p_workflowRoles, p_selectedRoles);
    }

    /*
     * Need to display the start date of the first review-only activity in a
     * workflow's default path (ONLY when a system-wide flag is on).
     */
    private Date getFirstReviewStartDate(long p_workflowId, Hashtable p_tasks,
            Date p_startDate) throws EnvoyServletException
    {
        try
        {
            Date result = null;

            // get all the task ids for the path in the workflow
            long[] taskIds = ServerProxy.getWorkflowServer()
                    .taskIdsInDefaultPath(p_workflowId);

            for (int i = 0; i < taskIds.length; i++)
            {
                Task t = (Task) p_tasks.get(new Long(taskIds[i]));

                if (t.isType(Task.TYPE_REVIEW)
                        || t.isType(Task.TYPE_REVIEW_EDITABLE))
                {
                    result = p_startDate;
                    break;
                }

                p_startDate = t.getEstimatedCompletionDate();
            }

            return result;
        }
        catch (Exception ex)
        {
            throw new EnvoyServletException(ex);
        }
    }

    // ////////////////////////////////////////////////////////////////////
    // Begin: Inner Class
    // ////////////////////////////////////////////////////////////////////
    class NewAssignee
    {
        String m_displayRoleName = null;

        String[] m_roles = null;

        boolean m_isUserRole = false;

        NewAssignee(String[] p_roles, String p_displayRoleName,
                boolean p_isUserRole)
        {
            m_displayRoleName = p_displayRoleName;
            m_roles = p_roles;
            m_isUserRole = p_isUserRole;
        }
    }

    /**
     * The method has been moved to SourcePage.java.
     *
     * @param p_fileName
     * @return
     */
    public static String filtSpecialFile(String p_fileName)
    {
        return SourcePage.filtSpecialFile(p_fileName);
    }

    /**
     * Get the xml of currency options
     *
     * @param locale
     * @return
     */
    private String getCurrenciesHTML(Locale locale)
    {
        StringBuffer sb = new StringBuffer();
        try
        {
            sb.append("<currencyOptions>");

            Collection allCurrencies = ServerProxy.getCostingEngine()
                    .getCurrencies();
            // fix for GBS-1693
            ArrayList<Currency> curremcies = new ArrayList<Currency>();
            Iterator<Currency> it = allCurrencies.iterator();
            while (it.hasNext())
            {
                curremcies.add(it.next());
            }
            SortUtil.sort(curremcies,
                    new CurrencyComparator(Locale.getDefault()));

            it = curremcies.iterator();
            while (it.hasNext())
            {
                Currency c = it.next();
                String currencyName = c.getDisplayName(locale);
                String isoCode = c.getIsoCode();
                sb.append("<currency><name>");
                sb.append(currencyName); // iso code is included in properties
                // file
                sb.append("</name><value>");
                sb.append(isoCode);
                sb.append("</value></currency>");
            }
            sb.append("</currencyOptions>");
        }
        catch (Exception e)
        {
            CATEGORY.error(
                    "JobHandlerMain::invokeJobControlPage():Problem getting Currencies from the system ",
                    e);
        }
        return sb.toString();
    }

    private void getUpdateWordCountsPercentage(HttpServletResponse p_response,
            long p_jobId) throws IOException
    {
        ServletOutputStream out = p_response.getOutputStream();
        try
        {
            p_response.setContentType("text/plain");
            out = p_response.getOutputStream();
            StringBuffer sb = new StringBuffer();
            sb.append("{\"updateWCPercentage\":");
            sb.append(updateWordCountsPercentageMap.get(p_jobId)).append("}");
            out.write(sb.toString().getBytes("UTF-8"));
        }
        catch (Exception e)
        {
            CATEGORY.error(e.getMessage(), e);
        }
        finally
        {
            out.close();
        }
    }

    /**
     * Copy files from source converter folder to target converter folder
     *
     * Fix for GBS-1815
     *
     * @param sourcePages
     * @param targetLocales
     */
    private void copyFilesToTargetDir(List<SourcePage> sourcePages,
            List<String> targetLocales)
    {
        SystemConfiguration sc = SystemConfiguration.getInstance();
        String fileStorageDir = sc.getStringParameter(
                SystemConfigParamNames.FILE_STORAGE_DIR,
                CompanyWrapper.SUPER_COMPANY_ID);
        List<String> copiedFiles = new ArrayList<String>();
        for (SourcePage sp : sourcePages)
        {
            String eventFlowXml = sp.getRequest().getEventFlowXml();
            Element rootElement = XmlUtils.findRootElement(eventFlowXml);

            long fileProfileId = sp.getRequest().getFileProfileId();
            String formatType = getFormatType(fileProfileId);

            String sourceLocale = sp.getGlobalSightLocale().toString();

            List<String> copyFilesName = null;
            String baseConv = null;

            if ("OpenOffice document".equals(formatType))
            {
                // Open office
                copyFilesName = getAllCopiedFilesForOpenOffice(rootElement);
                baseConv = fileStorageDir + File.separator + "OpenOffice-Conv";
            }
            else if ("Office2010 document".equals(formatType))
            {
                // Office 2010
                copyFilesName = getAllCopiedFilesForOffice2010(rootElement);
                baseConv = fileStorageDir + File.separator + "OfficeXml-Conv";
            }
            else if ("Word2007".equals(formatType))
            {
                // Word2007
                copyFilesName = getAllCopiedFilesForMSOffice20032007(rootElement);
                String office2007Conv = sc.getStringParameter(
                        SystemConfigParamNames.MSOFFICE_CONV_DIR,
                        CompanyWrapper.SUPER_COMPANY_ID);
                baseConv = office2007Conv + File.separator + "word";
            }
            else if ("Excel2007".equals(formatType))
            {
                // Excel2007
                copyFilesName = getAllCopiedFilesForMSOffice20032007(rootElement);
                String office2007Conv = sc.getStringParameter(
                        SystemConfigParamNames.MSOFFICE_CONV_DIR,
                        CompanyWrapper.SUPER_COMPANY_ID);
                baseConv = office2007Conv + File.separator + "excel";
            }
            else if ("PowerPoint2007".equals(formatType))
            {
                // PowerPoint2007
                copyFilesName = getAllCopiedFilesForMSOffice20032007(rootElement);
                String office2007Conv = sc.getStringParameter(
                        SystemConfigParamNames.MSOFFICE_CONV_DIR,
                        CompanyWrapper.SUPER_COMPANY_ID);
                baseConv = office2007Conv + File.separator + "powerpoint";
            }
            else if ("Word2003".equals(formatType))
            {
                // Word2003
                copyFilesName = getAllCopiedFilesForMSOffice20032007(rootElement);
                String office2003Conv = sc.getStringParameter(
                        SystemConfigParamNames.MSOFFICE2003_CONV_DIR,
                        CompanyWrapper.SUPER_COMPANY_ID);
                baseConv = office2003Conv + File.separator + "word";
            }
            else if ("Excel2003".equals(formatType))
            {
                // Excel2003
                copyFilesName = getAllCopiedFilesForMSOffice20032007(rootElement);
                String office2003Conv = sc.getStringParameter(
                        SystemConfigParamNames.MSOFFICE2003_CONV_DIR,
                        CompanyWrapper.SUPER_COMPANY_ID);
                baseConv = office2003Conv + File.separator + "excel";
            }
            else if ("PowerPoint2003".equals(formatType))
            {
                // PowerPoint2003
                copyFilesName = getAllCopiedFilesForMSOffice20032007(rootElement);
                String office2003Conv = sc.getStringParameter(
                        SystemConfigParamNames.MSOFFICE2003_CONV_DIR,
                        CompanyWrapper.SUPER_COMPANY_ID);
                baseConv = office2003Conv + File.separator + "powerpoint";
            }
            else if ("InDesign Markup (IDML)".equals(formatType))
            {
                // IDML
                String safeBaseFileName = getSafeBaseFileName(rootElement,
                        "IdmlAdapter");
                String filesDir = safeBaseFileName + ".unzip";
                copyFilesName = new ArrayList<String>();
                copyFilesName.add(filesDir);

                baseConv = fileStorageDir + File.separator + "Idml-Conv";
            }
            else if ("INDD (CS5)".equals(formatType))
            {
                // INDD (CS5)
                copyFilesName = getAllCopiedFilesForIndd(rootElement);
                baseConv = sc.getStringParameter(
                        SystemConfigParamNames.ADOBE_CONV_DIR_CS5,
                        CompanyWrapper.SUPER_COMPANY_ID)
                        + File.separator + "indd";
            }
            else if ("INDD (CS4)".equals(formatType))
            {
                // INDD (CS4)
                copyFilesName = getAllCopiedFilesForIndd(rootElement);
                baseConv = sc.getStringParameter(
                        SystemConfigParamNames.ADOBE_CONV_DIR_CS4,
                        CompanyWrapper.SUPER_COMPANY_ID)
                        + File.separator + "indd";

            }
            else if ("INDD (CS3)".equals(formatType))
            {
                // INDD (CS3)
                copyFilesName = getAllCopiedFilesForIndd(rootElement);
                baseConv = sc.getStringParameter(
                        SystemConfigParamNames.ADOBE_CONV_DIR_CS3,
                        CompanyWrapper.SUPER_COMPANY_ID)
                        + File.separator + "indd";
            }
            else if ("INDD (CS5.5)".equals(formatType))
            {
                // INDD (CS5.5)
                copyFilesName = getAllCopiedFilesForIndd(rootElement);
                baseConv = sc.getStringParameter(
                        SystemConfigParamNames.ADOBE_CONV_DIR_CS5_5,
                        CompanyWrapper.SUPER_COMPANY_ID)
                        + File.separator + "indd";
            }
            else if ("INX (CS3)".equals(formatType))
            {
                // INX(CS3)
                copyFilesName = getAllCopiedFilesForIndd(rootElement);
                baseConv = sc.getStringParameter(
                        SystemConfigParamNames.ADOBE_CONV_DIR_CS3,
                        CompanyWrapper.SUPER_COMPANY_ID)
                        + File.separator + "inx";
            }
            else if ("Windows Portable Executable".equals(formatType))
            {
                String companyName = CompanyWrapper.getCompanyNameById(sp
                        .getCompanyId());

                copyFilesName = getAllCopiedFilesForWinPE(rootElement);
                baseConv = sc.getStringParameter(
                        SystemConfigParamNames.WINDOWS_PE_DIR,
                        CompanyWrapper.SUPER_COMPANY_ID)
                        + File.separator
                        + "winpe"
                        + File.separator
                        + companyName;
            }

            // Copy files to target folder
            if (baseConv != null)
            {
                for (String fileName : copyFilesName)
                {
                    String filePath = baseConv + File.separator + sourceLocale
                            + File.separator + fileName;
                    if (copiedFiles.contains(filePath))
                    {
                        continue;
                    }
                    else
                    {
                        copiedFiles.add(filePath);
                    }
                    File file = new File(filePath);
                    for (String targetLocale : targetLocales)
                    {
                        if (file.exists())
                        {
                            File target = new File(baseConv + File.separator
                                    + targetLocale + File.separator + fileName);
                            try
                            {
                                if (file.isDirectory())
                                {
                                    FileUtil.copyFolder(file, target);
                                }
                                else
                                {
                                    FileUtil.copyFile(file, target);
                                }
                            }
                            catch (IOException e)
                            {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Get all needed files to copy
     *
     * @param rootElement
     * @return
     */
    private List<String> getAllCopiedFilesForMSOffice20032007(
            Element rootElement)
    {
        List<String> list = new ArrayList<String>();
        String safeBaseFileName = getSafeBaseFileName(rootElement,
                "MicrosoftApplicationAdapter");
        String name = safeBaseFileName.substring(0,
                safeBaseFileName.lastIndexOf("."));
        String htmlFile = name + ".html";
        String filesDir = name + "_files";
        String filesDir1 = name + ".files";
        list.add(htmlFile);
        list.add(filesDir);
        list.add(filesDir1);
        return list;
    }

    /**
     * Get all needed files to copy
     *
     * @param rootElement
     * @return
     */
    private List<String> getAllCopiedFilesForOpenOffice(Element rootElement)
    {
        List<String> list = new ArrayList<String>();
        String safeBaseFileName = getSafeBaseFileName(rootElement,
                "OpenOfficeAdapter");
        String fileDir;
        if (safeBaseFileName.endsWith("odp"))
        {
            fileDir = safeBaseFileName + "." + OpenOfficeHelper.OPENOFFICE_ODP;
        }
        else if (safeBaseFileName.endsWith("ods"))
        {
            fileDir = safeBaseFileName + "." + OpenOfficeHelper.OPENOFFICE_ODS;
        }
        else
        {
            fileDir = safeBaseFileName + "." + OpenOfficeHelper.OPENOFFICE_ODT;
        }
        list.add(safeBaseFileName);
        list.add(fileDir);
        return list;
    }

    /**
     * Get all needed files to copy
     *
     * @param rootElement
     * @return
     */
    private List<String> getAllCopiedFilesForIndd(Element rootElement)
    {
        List<String> list = new ArrayList<String>();
        String safeBaseFileName = getSafeBaseFileName(rootElement,
                "AdobeAdapter");
        String fileName = safeBaseFileName.substring(0,
                safeBaseFileName.lastIndexOf("."));
        list.add(safeBaseFileName);
        list.add(fileName + ".pdf");
        list.add(fileName + ".xmp");
        return list;
    }

    /**
     * Get all needed files to copy
     *
     * @param rootElement
     * @return
     */
    private List<String> getAllCopiedFilesForOffice2010(Element rootElement)
    {
        List<String> list = new ArrayList<String>();
        String safeBaseFileName = getSafeBaseFileName(rootElement,
                "OfficeXmlAdapter");

        String fileDir;
        if (safeBaseFileName.endsWith("docx"))
        {
            fileDir = safeBaseFileName + "." + OfficeXmlHelper.OFFICE_DOCX;
        }
        else if (safeBaseFileName.endsWith("pptx"))
        {
            fileDir = safeBaseFileName + "." + OfficeXmlHelper.OFFICE_PPTX;
        }
        else
        {
            fileDir = safeBaseFileName + "." + OfficeXmlHelper.OFFICE_XLSX;
        }
        list.add(safeBaseFileName);
        list.add(fileDir);
        return list;
    }

    /**
     * Get all needed files to copy
     *
     * @param rootElement
     * @return
     */
    private List<String> getAllCopiedFilesForWinPE(Element rootElement)
    {
        List<String> list = new ArrayList<String>();
        String safeBaseFileName = getSafeBaseFileName(rootElement,
                "WindowsPEAdapter");

        list.add(safeBaseFileName);
        return list;
    }

    /**
     * Get format type
     *
     * @param fileProfileId
     * @return
     */
    private String getFormatType(long fileProfileId)
    {
        FileProfilePersistenceManager fpPM;
        FileProfile fp;
        String formatType = null;
        try
        {
            fpPM = ServerProxy.getFileProfilePersistenceManager();
            fp = fpPM.getFileProfileById(fileProfileId, false);
            formatType = fpPM.getKnownFormatTypeById(fp.getKnownFormatTypeId(),
                    false).getName();
        }
        catch (Exception e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return formatType;
    }

    /**
     * Get safeBaseFileName to get base directory name
     *
     * @param eventFlow
     * @param adapter
     * @return
     */
    private String getSafeBaseFileName(Element rootElement, String adapter)
    {
        String safeBaseFileName = "";
        List categoryElements = XmlUtils.getChildElements(rootElement,
                "category");
        for (Iterator itor = categoryElements.iterator(); itor.hasNext();)
        {
            Element categoryElement = (Element) itor.next();
            if (adapter.equals(categoryElement.getAttribute("name")))
            {
                List daElements = XmlUtils.getChildElements(categoryElement,
                        "da");
                for (Iterator it = daElements.iterator(); it.hasNext();)
                {
                    Element daElement = (Element) it.next();
                    String name = daElement.getAttribute("name");
                    if ("safeBaseFileName".equals(name))
                    {
                        safeBaseFileName = XmlUtils.getChildElementValue(
                                daElement, "dv");
                        break;
                    }
                }
            }
        }
        return safeBaseFileName;
    }
}
