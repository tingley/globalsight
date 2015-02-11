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
package com.globalsight.everest.webapp.pagehandler.projects.workflows;

import java.io.IOException;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.Vector;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.globalsight.diplomat.util.database.ConnectionPool;
import com.globalsight.diplomat.util.database.ConnectionPoolException;
import com.globalsight.everest.costing.Cost;
import com.globalsight.everest.costing.Currency;
import com.globalsight.everest.foundation.L10nProfile;
import com.globalsight.everest.foundation.Timestamp;
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.jobhandler.JobHandlerLocal;
import com.globalsight.everest.jobhandler.JobSearchParameters;
import com.globalsight.everest.permission.Permission;
import com.globalsight.everest.permission.PermissionSet;
import com.globalsight.everest.projecthandler.WorkflowTemplateInfo;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.util.system.SystemConfigParamNames;
import com.globalsight.everest.util.system.SystemConfiguration;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.javabean.NavigationBean;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.webnavigation.LinkHelper;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.everest.workflowmanager.Workflow;
import com.globalsight.log.GlobalSightCategory;
import com.globalsight.util.GeneralException;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.date.DateHelper;

public abstract class JobManagementHandler extends PageHandler
{
    // color
    public static final String WHITE_BG = "#FFFFFF";

    public static final String GREY_BG = "#EEEEEE";

    // gifs
    public static final String EXPAND_ARROW_WHITE_BG = "expandArrow.gif";

    public static final String EXPAND_ARROW_GREY_BG = "expandArrowg.gif";

    public static final String COLLAPSE_ARROW_WHITE_BG = "collapseArrow.gif";

    public static final String COLLAPSE_ARROW_GREY_BG = "collapseArrowg.gif";

    // parameters
    public static final String COLLAPSE_WF_DISPLAY_PARAM = "wfIdToCollapse";

    public static final String EXPANDED_WF_DISPLAY_PARAM = "wfIdToExpand";

    public static final String EXPORT_WF_SEL_ID_PARAM = "exportWFSel";

    public static final String EXPORT_WF_CODE_PARAM = "exportCode";

    public static final String EXPORT_WF_DIR_PARAM = "exportWFDir";

    public static final String EXPORT_WF_LOCATION_PARAM = "exportWFLoc";

    public static final String EXPORT_WF_LOCALE_SUBDIR_PARAM = "exportWFLocaleSubDir";

    public static final String EXPORT_PAGE_SEL_ID_PARAM = "exportPageSel";

    public static final String EXPORT_FOR_UPDATE_PARAM = "exportForUpdate";

    public static final String EXPORT_MULTIPLE_ACTIVITIES_PARAM = "exportMultAct";

    public static final String EXPORT_SELECTED_WORKFLOWS_ONLY_PARAM = "exportSelectedWorkflowsOnly";

    public static final String MODIFY_WF_PARAM = "wfIdForModify";

    public static final String SORT_PARAM = "sort";

    public static final String EXPORT_URL_PARAM = "exportUrl";

    public static final String WF_ACTIVITIES_URL_PARAM = "wfActivitiesUrl";

    public static final String JOB_LIST_START_PARAM = "jobListStart";

    public static final String ERROR_URL_PARAM = "errorUrl";

    public static final String WF_NAME = "wfName";

    public static final String EXPORT_INIT_PARAM = "exportInitParam";

    public static final String MY_JOBS_TAB = "myJobsTab";

    public static final String PLANNED_COMP_DATE = "updatePlannedCompDate";

    // For sla report issue
    public static final String ESTIMATED_COMP_DATE = "updateEstimatedWorkflowCompDate";

    public static final String ESTIMATED_TRANSLATE_COMP_DATE = "updateEstimatedTranslateCompDate";

    // bean
    public static final String SELF_BEAN = "self";

    public static final String PENDING_BEAN = "pending";

    public static final String DETAILS_BEAN = "jobDetails";

    public static final String EXPORT_BEAN = "export";

    public static final String EXPORTED_BEAN = "exported";

    public static final String LOCALIZED_BEAN = "complete";

    public static final String CHANGE_CURRENCY_BEAN = "changeCurr";

    public static final String EDIT_PAGES_BEAN = "editPages";

    public static final String EDIT_FINAL_COST_BEAN = "editFinalCost";

    public static final String SURCHARGES_BEAN = "surcharges";

    public static final String EDIT_ADD_SURCHARGES_BEAN = "editAddSurcharges";

    public static final String EDIT_TOTAL_SOURCE_PAGE_WC_BEAN = "editTotalSourcePageWc";

    public static final String EDIT_SOURCE_PAGE_WC_BEAN = "editSourcePageWc";

    public static final String JOB_COMMENTS_BEAN = "jobComments";

    public static final String JOB_COMMENTS = "JOBCOMMENTS";

    public static final String ADD_WF_BEAN = "addWF";

    public static final String PLANNED_COMPLETION_DATE_BEAN = "plannedCompletionDate";

    public static final String DOWNLOAD_BEAN = "download";

    public static final String DTP_DOWNLOAD_BEAN = "dtpDownload";

    public static final String DTP_UPLOAD_BEAN = "dtpUpload";

    // For sla report issue
    public static final String ESTIMATED_COMP_DATE_BEAN = "estimatedWorkflowCompDate";

    public static final String ESTIMATED_TRANSLATE_COMP_DATE_BEAN = "estimatedTranslateCompDate";

    // beans and params for sorting the list of pages and searching on them by
    // name
    public static final String PAGE_LIST_BEAN = "pageList";

    public static final String PAGE_SEARCH_BEAN = "pageSearch";

    // page sorting comparator
    public static final String PAGE_COMPARATOR = "pageComparator";

    //
    public static final String PAGE_SEARCH_PARAM = "pageSearchParam";

    public static final String PAGE_SORT_PARAM = "pageSort";

    public static final String PAGE_SORT_COLUMN = "pageSortColumn";

    public static final String PAGE_SORT_ASCENDING = "pageSortAscending";

    // the label to be used before the filter string on the page
    public static final String FILTER_LABEL = "filterLabel";

    // scriptlet for details and export page
    public static final String JOB_INITIATOR_SCRIPTLET = "jobInitiator";

    public static final String JOB_NAME_SCRIPTLET = "jobName";

    public static final String JOB_ID = WebAppConstants.JOB_ID;

    public static final String WF_ID = "wfId";

    public static final String JOB_STATE = "state";

    public static final String L10NPROFILE_NAME_SCRIPTLET = "l10nProfileName";

    public static final String SRC_LOCALE_SCRIPTLET = "srcLocale";

    public static final String TRGT_LOCALE_SCRIPTLET = "trgtLocale";

    public static final String JOB_DATE_CREATED_SCRIPTLET = "dateCreated";

    public static final String PROJECT_NAME_SCRIPTLET = "projectName";

    public static final String DATA_SOURCE_SCRIPTLET = "dataSource";

    public static final String FINAL_DUE_DATE_SCRIPTLET = "finalDueDate";

    public static final String JOB_CONTENT_SCRIPTLET = "jobSourcePageContent";

    public static final String JOB_PRIORITY_SCRIPTLET = "jobPriority";

    public static final String JOB_COST_SCRIPTLET = "jobCosting";

    public static final String CHANGE_CURRENCY_URL = "changeCurrency";

    // general scriptlets.
    public static final String JOB_SCRIPTLET = "jobJSPData";

    public static final String WORKFLOW_SCRIPTLET = "workflowJSPData";

    public static final String WORKFLOW_ACTIVITIES_SCRIPTLET = "workflowActivitiesData";

    public static final String WORKFLOW_PRIMARY_UNEXTRACTED_TARGET_FILES = "workflowPrimaryUnextractedTargetFiles";

    public static final String WORKFLOW_SECONDARY_TARGET_FILES = "workflowSecondaryTargetFiles";

    public static final String EXPORT_SCRIPTLET = "jobExportJSPData";

    public static final String PAGING_SCRIPTLET = "pagingJSPData";

    public static final String BUTTON_SCRIPTLET = "buttonJSPData";

    // word count types.
    public static final int EXACT_MATCH = 1; // segment tm

    public static final int FUZZY_MATCH = 2; // hi fuzzy (95-99%)

    public static final int UNMATCHED = 3;

    public static final int REPETITION = 4;

    public static final int TOTAL_WF_WORD_CNT = 5;

    public static final int CONTEXT_MATCH = 6; // segment tm

    public static final int LOW_FUZZY_MATCH = 7; // low fuzzy (50-74%)

    public static final int MED_FUZZY_MATCH = 8; // med fuzzy (75-84%)

    public static final int MED_HI_FUZZY_MATCH = 9; // med-hi fuzzy (85-94%)

    // currency
    public static final String CURRENCY = "idCurrency";

    public static final String CURRENCY_XML = "currencyXml";

    // costing
    public static final String COST_OBJECT = "costObject";

    public static final String REVENUE_OBJECT = "revenueObject";

    public static final String CURRENCY_OBJECT = "currencyObject";

    public static final String PAGES_IN_JOB = "pagesInJob";

    public static final String SURCHARGE = "surcharge";

    public static final String SURCHARGES_FOR = "surchargesFor";

    public static final String SURCHARGES_ALL = "surchargesAll";

    public static final String REVENUE_SURCHARGES_ALL = "revenueSurchargesAll";

    public static final String SURCHARGE_NAME = "surchargeName";

    public static final String SURCHARGE_TYPE = "surchargeType";

    public static final String SURCHARGE_VALUE = "surchargeValue";

    public static final String SURCHARGE_ACTION = "surchargeAction";

    public static final String ESTIMATED_COST = "estimatedCost";

    public static final String ESTIMATED_REVENUE = "estimatedRevenue";

    public static final String ACTUAL_COST = "actualCost";

    public static final String FINAL_COST = "finalCost";

    public static final String ACTUAL_REVENUE = "actualRevenue";

    public static final String FINAL_REVENUE = "finalRevenue";

    public static final String REMOVE_OVERRIDE = "removeOverride";

    // source page word counts
    public static final String SOURCE_PAGE_WC = "sourcePageWc";

    public static final String TOTAL_SOURCE_PAGE_WC = "totalSourcePageWc";

    public static final String TOTAL_WC_OVERRIDEN = "totalWcOverriden";

    public static final String REMOVE_TOTAL_WC_OVERRIDEN = "removeTotalWcOverriden";

    public static final String WORDCOUNT = "wordCount";

    public static final String SOURCE_PAGE_ID = "pageId";

    public static final String NUM_OF_PAGES_IN_JOB = "numOfPages";

    // Segment Comments
    public static final String OPEN_AND_QUERY_SEGMENT_COMMENTS = "openQuerySegComments";

    public static final String CLOSED_SEGMENT_COMMENTS = "closedSegComments";

    // quotation email date
    public static final String QUOTE_DATE = "quoteDate";

    // the user who approved the cost of job
    public static final String AUTHORISER_USER = "anthoriserUser";

    // changed date used to judge whether it is a refresh
    public static final String DATE_CHANGED = "dateChanged";

    // For "Quote process webEx" issue
    public static final String QUOTE_PO_NUMBER = "quotePoNumber";

    public static final String QUOTE_APPROVED_DATE = "quoteApprovedDate";
    
    public static final String QUOTE_APPROVED_DATE_MODIFY_FLAG = "quoteApprovedDateModifyFlag";
    
    public static final String QUOTE_SAVE_PO_NUMBER = "savePoNumber";

    // paging widget
    public static final String JOB_LIST_START = "jobListStart";

    public static final String NUM_OF_JOBS = "numOfJobs";

    // table display
    public static final String TABLE_WIDTH = "613";

    public static final String TABLE_WIDTH_PARAM = "divWidth";

    public static final String COST_COL_WIDTH = "115";

    public static final String COST_COL_WIDTH_PARAM = "costColWidth";

    // parameters
    public static final String OBTAIN_TIME = "obtainTime";
    
    public static final String ADD_WF_PARAM = "addWorkflows";

    public static final String ADDED_WORKFLOWS = "added_workflows";

    public static final String ARCHIVE_JOB_PARAM = "archiveJob";

    public static final String ASSIGN_PARAM = "assignUser";
    
    public static final String SKIP_PARAM = "skipActivities";

    public static final String DISPATCH_JOB_PARAM = "dispatchJob";

    public static final String DISCARD_JOB_PARAM = "discardJob";

    public static final String ARCHIVE_WF_PARAM = "archiveWF";

    public static final String DISPATCH_WF_PARAM = "dispatchWF";
    
    public static final String DISPATCH_ALL_WF_PARAM = "dispatchAllWF";

    public static final String DISCARD_WF_PARAM = "discardWF";

    public static final String ERROR_WF_PARAM = "errorWF";

    public static final String MAKE_READY_JOB_PARAM = "makeReadyJob";

    public static final String CANCEL_IMPORT_ERROR_PAGES_PARAM = "cancelImportErrorPages";

    // sort
    public static final String SORT_COLUMN = "sortColumn";

    public static final String SORT_ASCENDING = "sortAscending";

    // bean
    protected static final String MODIFY_BEAN = "modify";

    protected static final String ERROR_BEAN = "error";

    // comparator
    private static final String COMPARATOR = "comparator";

    // parameters
    private static final String EXPANDED_JOB_DISPLAY_PARAM = "jobIdToExpand";

    private static final String COLLAPSE_JOB_DISPLAY_PARAM = "jobIdToCollapse";

    private static final String SET_EXPANDED_JOBS = "listOfExpandedDisplayJobs";
    
    public static final String ALL_READY_WORKFLOW_IDS = "allreadyWorkfowIds";
    public static final String HAS_READY_WORKFLOW = "hasReadyWorkflow";

    // chech box name, save object checkboxName in request
    public static final String CHECKBOX_NAME = "CHECKBOX_NAME";

    boolean currencyChanged = false;

    private static final GlobalSightCategory CATEGORY = (GlobalSightCategory) GlobalSightCategory
            .getLogger(JobManagementHandler.class.getName());

    // System-wide costing feature
    private static boolean s_jobCosting = false;
    static
    {
        try
        {
            SystemConfiguration sc = SystemConfiguration.getInstance();
            s_jobCosting = sc
                    .getBooleanParameter(SystemConfigParamNames.COSTING_ENABLED);
        }
        catch (Throwable e)
        {
            CATEGORY
                    .error(
                            "JobHandlerMain::invokeJobControlPage():Problem getting costing parameter from database ",
                            e);
        }
    }

    // semaphore to control access to Job List query since it is performance
    // intensive
    private static String s_semaphore = new String("semaphore");

    private static int s_semCount = 0;

    private static int s_semMax;
    static
    {
        // read Envoy.properties and get value from maxMyJobsThreads
        try
        {
            SystemConfiguration config = SystemConfiguration.getInstance();
            s_semMax = config.getIntParameter("maxMyJobsThreads");
            CATEGORY.info("Maximum number of My Jobs threads: " + s_semMax);
        }
        catch (Exception e)
        {
            CATEGORY
                    .error("Error when trying to read max my jobs threads: ", e);
            CATEGORY.error("Using default value of 3");
            s_semMax = 3;
        }
    }

    /** Returns true if the execution of the My Jobs tab can start */
    protected boolean startExecution()
    {
        synchronized (s_semaphore)
        {
            if (s_semCount < s_semMax)
            {
                s_semCount++;
                CATEGORY.debug("up count is now: " + s_semCount);
                return true;
            }
            else
            {
                CATEGORY.info("count is locked: " + s_semCount);
                return false;
            }
        }
    }

    /** Decrements the count * */
    protected void stopExecution()
    {
        synchronized (s_semaphore)
        {
            if (s_semCount > -1) s_semCount--;
            CATEGORY.debug("down count is now: " + s_semCount);
        }
    }

    // Get the Records Per Page param
    private static int s_jobsPerPage = 20;
    static
    {
        try
        {
            SystemConfiguration sc = SystemConfiguration.getInstance();
            s_jobsPerPage = sc
                    .getIntParameter(SystemConfigParamNames.RECORDS_PER_PAGE_JOBS);
            if (s_jobsPerPage < 1)
            {
                CATEGORY.warn("Can not use records per page value of "
                        + s_jobsPerPage + ". Using 20.");
                s_jobsPerPage = 20;
            }
        }
        catch (Throwable e)
        {
            CATEGORY
                    .error(
                            "JobHandlerMain::invokeJobControlPage():Problem getting RECORDS_PER_PAGE_JOBS from database ",
                            e);
            s_jobsPerPage = 20;
        }
    }

    /**
     * Invokes the myInvokePageHandler method only if their are free servlet
     * threads to access My Jobs, otherwise the busy page is shown.
     * <p>
     * 
     * @param p_ageDescriptor
     *            the description of the page to be produced.
     * @param p_request
     *            original request sent from the browser.
     * @param p_response
     *            original response object.
     * @param p_context
     *            the Servlet context.
     */
    public void invokePageHandler(WebPageDescriptor p_thePageDescriptor,
            HttpServletRequest p_request, HttpServletResponse p_response,
            ServletContext p_context) throws ServletException, IOException,
            RemoteException, EnvoyServletException
    {
        boolean ok = startExecution();
        if (ok == false)
        {
            // forward to the busy JSP
            CATEGORY.info("forwarding to busy page");
            RequestDispatcher dispatcher = p_context
                    .getRequestDispatcher("/envoy/common/busy.jsp");
            dispatcher.forward(p_request, p_response);
        }
        else
        {
            try
            {
                myInvokePageHandler(p_thePageDescriptor, p_request, p_response,
                        p_context);
            }
            finally
            {
                stopExecution();
            }
        }
    }

    /**
     * Abstract method called by invokePageHandler(). If the number of My Jobs
     * threads is below the max, then this method is called, otherwise the busy
     * page is shown
     * 
     * @param p_thePageDescriptor
     * @param p_request
     * @param p_response
     * @param p_context
     * @exception ServletException
     * @exception IOException
     * @exception RemoteException
     * @exception EnvoyServletException
     */
    abstract public void myInvokePageHandler(
            WebPageDescriptor p_thePageDescriptor,
            HttpServletRequest p_request, HttpServletResponse p_response,
            ServletContext p_context) throws ServletException, IOException,
            RemoteException, EnvoyServletException;

    public HashMap invokeJobControlPage(WebPageDescriptor p_pageDescriptor,
            HttpServletRequest p_request, String p_baseBeanLinkName)
    {
        // ///////////////////////////////////////////////////////////////////////////////////
        // BEGIN: Setting navigation beans.
        // /////////////////////////////////////////////////
        // ///////////////////////////////////////////////////////////////////////////////////
        SessionManager sessionMgr = (SessionManager) p_request
                .getSession(false).getAttribute(SESSION_MANAGER);
        HttpSession session = p_request.getSession();
        Locale uiLocale = (Locale)session.getAttribute(WebAppConstants.UILOCALE);

        if (s_jobCosting)
        {
            session.setAttribute(JobManagementHandler.CURRENCY_XML,
                    getCurrenciesHTML(uiLocale));

            String curr = getPivodCurrency(p_request, sessionMgr);
            sessionMgr.setAttribute(JobManagementHandler.CURRENCY, curr);
            session.setAttribute(JobManagementHandler.CURRENCY, curr);
        }

        NavigationBean baseBean = null;
        NavigationBean modifyBean = null;
        NavigationBean detailsBean = null;
        NavigationBean exportBean = null;
        NavigationBean changeCurrencyBean = null;
        NavigationBean plannedCompletionDateBean = null;
        NavigationBean downloadBean = null;
        Enumeration en = p_pageDescriptor.getLinkNames();

        while (en.hasMoreElements())
        {
            String linkName = (String) en.nextElement();
            String pageName = p_pageDescriptor.getPageName();
            if (linkName.equals(p_baseBeanLinkName))
            {
                baseBean = new NavigationBean(linkName, pageName);
            }
            else if (linkName.equals(MODIFY_BEAN))
            {
                modifyBean = new NavigationBean(linkName, pageName);
            }
            else if (linkName.equals(DETAILS_BEAN))
            {
                detailsBean = new NavigationBean(linkName, pageName);
            }
            else if (linkName.equals(EXPORT_BEAN))
            {
                exportBean = new NavigationBean(linkName, pageName);
            }
            else if (linkName.equals(CHANGE_CURRENCY_BEAN))
            {
                changeCurrencyBean = new NavigationBean(linkName, pageName);
            }
            else if (linkName.equals(PLANNED_COMPLETION_DATE_BEAN))
            {
                plannedCompletionDateBean = new NavigationBean(linkName,
                        pageName);
            }
            else if (linkName.equals(DOWNLOAD_BEAN))
            {
                downloadBean = new NavigationBean(linkName, pageName);
            }
            // create a navigation bean for each link
            NavigationBean aNavigationBean = new NavigationBean(linkName,
                    pageName);
            // each navigation bean will be labelled with the name of the link
            p_request.setAttribute(linkName, aNavigationBean);
        }

        HashMap<String, NavigationBean> beanMap = new HashMap<String, NavigationBean>(5);
        beanMap.put(p_baseBeanLinkName, baseBean);
        beanMap.put(MODIFY_BEAN, modifyBean);
        beanMap.put(DETAILS_BEAN, detailsBean);
        beanMap.put(CHANGE_CURRENCY_BEAN, changeCurrencyBean);
        beanMap.put(PLANNED_COMPLETION_DATE_BEAN, plannedCompletionDateBean);
        beanMap.put(DOWNLOAD_BEAN, downloadBean);
        if (exportBean != null)
        {
            beanMap.put(EXPORT_BEAN, exportBean);
        }

        return beanMap;
    }

    // ////////////////////////////////////////////////////////////////////
    // Begin: Abstract Methods
    // ////////////////////////////////////////////////////////////////////
    abstract protected void performAppropriateOperation(
            HttpServletRequest p_request) throws EnvoyServletException;

    // ////////////////////////////////////////////////////////////////////
    // End: Abstract Methods
    // ////////////////////////////////////////////////////////////////////

    // ////////////////////////////////////////////////////////////////////
    // Begin: Helper Methods
    // ////////////////////////////////////////////////////////////////////
    // get the whole display list
    protected String getJobText(HttpServletRequest p_request, String p_baseURL,
            String p_modifyURL, String p_detailsURL, String p_pdateURL,
            HashSet p_setOfExpandedJobs, String p_jobState, boolean p_estimated)
            throws RemoteException, EnvoyServletException
    {
        return getJobText(p_request, p_baseURL, p_modifyURL, p_detailsURL,
                p_pdateURL, p_setOfExpandedJobs, p_jobState, p_estimated, false);
    }

    protected String getJobText(HttpServletRequest p_request, String p_baseURL,
            String p_modifyURL, String p_detailsURL, String p_pdateURL,
            HashSet p_setOfExpandedJobs, String p_jobState,
            boolean p_estimated, boolean p_allPages) throws RemoteException,
            EnvoyServletException
    {
        Vector<String> jobState = new Vector<String>();
        jobState.add(p_jobState);
        return getJobText(p_request, p_baseURL, p_modifyURL, p_detailsURL,
                p_pdateURL, p_setOfExpandedJobs, jobState, p_estimated,
                p_allPages);
    }

    protected String getJobText(HttpServletRequest p_request, String p_baseURL,
            String p_modifyURL, String p_detailsURL, String p_pdateURL,
            HashSet p_setOfExpandedJobs, Vector p_stateList, boolean p_estimated)
            throws RemoteException, EnvoyServletException
    {
        return getJobText(p_request, p_baseURL, p_modifyURL, p_detailsURL,
                p_pdateURL, p_setOfExpandedJobs, p_stateList, p_estimated,
                false);
    }

    protected String getJobText(HttpServletRequest p_request, String p_baseURL,
            String p_modifyURL, String p_detailsURL, String p_pdateURL,
            HashSet p_setOfExpandedJobs, Vector p_stateList,
            boolean p_estimated, boolean p_allPages) throws RemoteException,
            EnvoyServletException
    {
        return getJobText(p_request, p_baseURL, p_modifyURL, p_detailsURL,
                p_pdateURL, p_setOfExpandedJobs, p_stateList, p_estimated,
                p_allPages, false);
    }

    @SuppressWarnings("unchecked")
    protected String getJobText(HttpServletRequest p_request, String p_baseURL,
            String p_modifyURL, String p_detailsURL, String p_pdateURL,
            HashSet p_setOfExpandedJobs, Vector p_stateList,
            boolean p_estimated, boolean p_allPages, boolean p_jobstatus)
            throws RemoteException, EnvoyServletException
    {
        HttpSession session = p_request.getSession(false);

        Locale uiLocale = (Locale) session.getAttribute(UILOCALE);
        TimeZone timezone = (TimeZone) session.getAttribute(USER_TIME_ZONE);
        p_request.setAttribute(TABLE_WIDTH_PARAM, TABLE_WIDTH);
        p_request.setAttribute(COST_COL_WIDTH_PARAM, COST_COL_WIDTH);

        // searchType == stateOnly is set if coming from Jobs menu:pending, in
        // progress...
        String searchType = (String) p_request.getParameter("searchType");

        if (searchType == null)
            searchType = (String) session.getAttribute("searchType");
        else
            session.setAttribute("searchType", searchType);

        SessionManager sessionMgr = (SessionManager) session
                .getAttribute(SESSION_MANAGER);
        String curr = (String) sessionMgr
                .getAttribute(JobManagementHandler.CURRENCY);
        Currency oCurrency = null;
        if (s_jobCosting)
        {
            try
            {
                oCurrency = ServerProxy.getCostingEngine().getCurrency(curr);
            }
            catch (Exception e)
            {
                CATEGORY
                        .error(
                                "JobManagementHandler :: Error getting Currency object: ",
                                e);
            }
        }

        User user = (User) sessionMgr.getAttribute(USER);
        PermissionSet userPerms = (PermissionSet) session
                .getAttribute(WebAppConstants.PERMISSIONS);
        List jobs = new ArrayList();

        if ("stateOnly".equals(searchType))
        {
            // don't do search. just get jobs for a state. (from main menu)
            /*
             * check to see if the user is an administrator... if then return
             * all jobs. if the user is a project manager.... then return only
             * jobs associated to that proj man.
             */
            // Check if can view all jobs.
            // If user doesn't have that permission, then
            // need to check both PROJECTS_MANAGE and PROJECTS_MANAGE_WORKFLOWS
            // because
            // they could have both set.
            if (userPerms.getPermissionFor(Permission.JOB_SCOPE_ALL))
            {
                jobs = new ArrayList(WorkflowHandlerHelper
                        .getJobsByStateList(p_stateList));
                // set this to null if not a Project Manager,
                // disabling the link to modify workflow.
                p_modifyURL = null;
            }
            else
            {
                if (userPerms.getPermissionFor(Permission.PROJECTS_MANAGE))
                {
                    jobs = new ArrayList(WorkflowHandlerHelper
                            .getJobsByManagerIdAndStateList(user.getUserId(),
                                    p_stateList));
                }

                if (userPerms
                        .getPermissionFor(Permission.PROJECTS_MANAGE_WORKFLOWS))
                {
                    if (jobs == null)
                    {
                        jobs = new ArrayList(WorkflowHandlerHelper
                                .getJobsByWfManagerIdAndStateList(user
                                        .getUserId(), p_stateList));
                    }
                    else
                    {
                        List jobs2 = new ArrayList(WorkflowHandlerHelper
                                .getJobsByWfManagerIdAndStateList(user
                                        .getUserId(), p_stateList));
                        jobs = mergeLists(jobs, jobs2);
                    }
                }
				if(userPerms.getPermissionFor(Permission.JOB_SCOPE_MYPROJECTS))
				{
					JobHandlerLocal jobSearch = new JobHandlerLocal();
	                if (jobs == null)
	                {
	                	 jobs = new ArrayList(jobSearch.getJobsByUserIdAndState(user.getUserId(),
	                             p_stateList));
	                }
	                else
	                {
	                	 List jobs3 = new ArrayList(jobSearch.getJobsByUserIdAndState(user.getUserId(),
	                             p_stateList));
	                	 jobs = mergeLists(jobs, jobs3);
	                }            	
				} 
            }
        }
        else
        {
            try
            {
                JobSearchParameters searchParams = getSearchParams(p_request,
                        session, user, searchType);
                jobs = (List) ServerProxy.getJobHandler().getJobs(searchParams);

                String status = (String) p_request
                        .getParameter(JobSearchConstants.STATUS_OPTIONS);
                if (status != null && status.equals(Job.DTPINPROGRESS))
                {
                    Set tempJobs = new HashSet();
                    tempJobs.addAll(jobs);
                    tempJobs.addAll(ServerProxy.getJobHandler().getJobsByState(
                            Job.DTPINPROGRESS));
                    jobs = new ArrayList(tempJobs);
                }
            }
            catch (GeneralException e)
            {
                throw new EnvoyServletException(e);
            }
            catch (javax.naming.NamingException ne)
            {
                throw new EnvoyServletException(ne);
            }
        }

        sortJobs(p_request, session, uiLocale, jobs, oCurrency);

        int numOfJobs = jobs == null ? 0 : jobs.size();
        // store the # of jobs in the request for paging info (used in
        // getPagingText method)
        p_request.setAttribute(NUM_OF_JOBS, new Integer(numOfJobs));

        int jobListStart = determineStartIndex(p_request, session, sessionMgr);

        /*
         * Note that these two integers, jobListStart and jobListEnd, are the
         * *indexes* of the list so for display we'll add 1 so it will look good
         * for the user. This is also why I subtract 1 from jobListEnd below, so
         * that jobListEnd will be an index value
         */
        int jobListEnd = (jobListStart + s_jobsPerPage) > numOfJobs ? numOfJobs
                : (jobListStart + s_jobsPerPage);
        jobListEnd = jobListEnd - 1;

        StringBuffer sb = new StringBuffer();
        int i = 0;
        Timestamp ts = new Timestamp(Timestamp.DATE, timezone);
        ts.setLocale(uiLocale);
        boolean hasDetailPerm = userPerms
                .getPermissionFor(Permission.JOBS_DETAILS);

        // find all in progress workflow ids
        List inprogressWfs = getInProgressWorkflows();

        // Session dbSession = HibernateUtil.getSession();
        for (i = jobListStart; i <= jobListEnd; i++)
        {
            Job curJob = (Job) jobs.get(i);
            // curJob = (JobImpl) dbSession.get(JobImpl.class, new Long(curJob
            // .getId()));
            boolean isRowWhite = i % 2 == 0;
            String state = curJob.getState();
            String textType = "standardText";
            // if either are marked as failures
            // or if the job contains ONE failed import
            if (state.equals(Job.EXPORT_FAIL)
                    || curJob.containsFailedWorkflow()
                    || curJob.getState().equals(Job.IMPORTFAILED))
            {
                textType = "warningText";
            }
            sb
                    .append("<TR VALIGN=TOP STYLE=\"padding-top: 5px; padding-bottom: 5px;\" BGCOLOR=\"");
            sb.append((isRowWhite ? WHITE_BG : GREY_BG));
            sb.append("\">");

            // Radio button
            if (exportIsInProgress(curJob, inprogressWfs))
            {
                state = "JOB_EXPORT_IN_PROGRESS";
            }

            String checkboxName = "";
            List workflows = (List) curJob.getWorkflows();
            L10nProfile l10nProfile = curJob.getL10nProfile();
            for (int m = 0; m < workflows.size(); m++)
            {
                Workflow workflow = (Workflow) workflows.get(m);
                GlobalSightLocale targetLocale = workflow.getTargetLocale();
                WorkflowTemplateInfo dtpWfti = null;
                if (l10nProfile != null) {
                    dtpWfti = (WorkflowTemplateInfo) l10nProfile
                    .getDtpWorkflowTemplateInfo(targetLocale);                	
                }
                if (dtpWfti != null && Workflow.LOCALIZED.equals(workflow.getState()))
                {
                    checkboxName = "dtpCheckbox";
                }
                else if (checkboxName.equals(""))
                {
                    checkboxName = "transCheckbox";
                }
            }
            p_request.setAttribute(CHECKBOX_NAME, checkboxName);
            sb.append("<TD><INPUT onclick=\"setButtonState()\" TYPE=checkbox NAME=");
            sb.append(checkboxName);
            sb.append(" VALUE=\"jobId=");
            sb.append(curJob.getId());
            sb.append("&jobState=");
            sb.append(state);
            sb.append("\"></TD>\n");

            // Priority
            sb.append("<TD CLASS=standardText>");
            sb.append(curJob.getPriority());
            sb.append("</TD>\n");

            // Job ID
            sb.append("<TD CLASS=standardText>");
            sb.append(curJob.getId());
            sb.append("</TD>\n");

            // Job Name
            sb.append(getJobNameHTML(curJob, p_detailsURL, state, hasDetailPerm));

            // Project Name
            sb.append("<TD><SPAN CLASS=\"");
            sb.append(textType);
            sb.append("\">");
            String projectName = "";
            if (l10nProfile != null) {
            	projectName = l10nProfile.getProject().getName();
            }
            sb.append(projectName);
            sb.append("</SPAN></TD>\n");

            // Source Locale
            String srcLocale = "";
            if (l10nProfile != null) {
            	GlobalSightLocale sourceLocale = curJob.getSourceLocale();
            	if (sourceLocale != null) {
            		srcLocale = sourceLocale.getDisplayName(uiLocale);
            	}
            }
//            GlobalSightLocale sourceLocale = curJob.getSourceLocale();
            sb.append("<TD><SPAN CLASS=\"");
            sb.append(textType);
            sb.append("\">");
            sb.append(srcLocale);
            sb.append("</SPAN></TD>\n");

            // Word Count
            sb.append("<TD STYLE=\"padding-right: 10px;\"><SPAN CLASS=\"");
            sb.append(textType);
            sb.append("\">");
            sb.append(curJob.getWordCount());
            sb.append("</SPAN></TD>\n");

            // Date Created
            sb.append("<TD STYLE=\"padding-right: 10px;\"><SPAN CLASS=\"");
            sb.append(textType);
            sb.append("\">");
            sb.append(DateHelper.getFormattedDate(curJob.getCreateDate(),
                    uiLocale, timezone));
            sb.append("</SPAN></TD>\n");

            if (p_estimated)
            {
                // Estimated Translate Completion Date
                sb.append(getEstimatedTranslateDateHTML(curJob, ts));
            }

            if ((p_stateList != null)
                    && (p_stateList.contains(Job.READY_TO_BE_DISPATCHED)
                            || p_stateList.contains(Job.DISPATCHED)
                            || p_stateList.contains(Job.LOCALIZED)
                            || p_stateList.contains(Job.EXPORTED) || p_stateList
                            .contains(Job.ARCHIVED)))
            {
                // Estimated Job Completion Date
                sb.append(getEstimatedJobDateHTML(curJob, ts));
            }

            if (p_jobstatus)
            {
                sb.append("<TD STYLE=\"padding-right: 10px;\"><SPAN CLASS=\"");
                sb.append(textType);
                sb.append("\">");
                sb.append(curJob.getDisplayStateByLocale(uiLocale));
                sb.append("</SPAN></TD>\n");
            }

            sb.append("</TR>\n");
        }
        // dbSession.close();

        // If "allPages" is set, then add hidden checkbox fields for the
        // remaining jobs.
        if (p_allPages)
        {
            sb.append("<tr><td>");
            sb.append("<div id='restofjobs' style=\"display:none\">");
            for (i = 0; i < jobListStart; i++)
            {
                Job curJob = (Job) jobs.get(i);
                sb
                        .append("<input type='checkbox' name=jobIdHidden value=\"jobId=");
                sb.append(curJob.getId());
                sb.append("&jobState=notused");
                sb.append("\">\n");
            }
            for (i = jobListEnd + 1; i < numOfJobs; i++)
            {
                Job curJob = (Job) jobs.get(i);
                sb
                        .append("<input type='checkbox' name=jobIdHidden value=\"jobId=");
                sb.append(curJob.getId());
                sb.append("&jobState=notused");
                sb.append("\">\n");
            }
            sb.append("</div>");
            sb.append("</td></tr>");
        }

        return sb.toString();
    }

    /**
     * Creates the paging widget for all My Jobs tabs so you can page through
     * the list 20 jobs at a time.
     * <P>
     * Display should look like:
     * <P>
     * Displaying 1 - 20 of 54 First | Previous | Next | Last
     * <P>
     * 
     * @param p_request --
     *            the workflow
     * @param p_baseURL --
     *            the URL of the tab
     * @param p_stateList --
     *            list of acceptable job states for the given tab
     * @return a formatted string
     */
    protected String getPagingText(HttpServletRequest p_request,
            String p_baseURL, String p_jobState) throws RemoteException,
            EnvoyServletException
    {
        Vector<String> jobState = new Vector<String>();
        jobState.add(p_jobState);
        return getPagingText(p_request, p_baseURL, jobState);
    }

    protected String getPagingText(HttpServletRequest p_request,
            String p_baseURL, Vector p_stateList) throws RemoteException,
            EnvoyServletException
    {
        HttpSession session = p_request.getSession(false);
        SessionManager sessionMgr = (SessionManager) session
                .getAttribute(SESSION_MANAGER);
        ResourceBundle bundle = getBundle(session);

        int numOfJobs = ((Integer) p_request.getAttribute(NUM_OF_JOBS))
                .intValue();
        int jobListStart = determineStartIndex(p_request, session, sessionMgr);

        int numOfPages = numOfJobs / s_jobsPerPage;
        if (numOfPages * s_jobsPerPage != numOfJobs) numOfPages++;
        int curPage = jobListStart / s_jobsPerPage + 1;
        int numOfPagesInGroup = WebAppConstants.NUMBER_OF_PAGES_IN_GROUP;
        int pagesOnLeftOrRight = numOfPagesInGroup / 2;

        /*
         * Note that these two integers, jobListStart and jobListEnd, are the
         * *indexes* of the list so for display we'll add 1 so it will look good
         * for the user. This is also why I subtract 1 from jobListEnd below, so
         * that jobListEnd will be an index value
         */
        int jobListEnd = (jobListStart + s_jobsPerPage) > numOfJobs ? numOfJobs
                : (jobListStart + s_jobsPerPage);
        jobListEnd = jobListEnd - 1;

        StringBuffer sb = new StringBuffer();

        // Make the Paging widget
        if (numOfJobs > 0)
        {
            Object[] args = { new Integer(jobListStart + 1),
                    new Integer(jobListEnd + 1), new Integer(numOfJobs) };

            // "Displaying 1 - 20 of 35"
            sb.append(MessageFormat.format(bundle
                    .getString("lb_displaying_records"), args));
            sb.append("<BR>");

            // The "First" link
            if (jobListStart == 0)
            {
                // Don't hyperlink "First" if it's the first page
                sb.append("<SPAN CLASS=standardTextGray>"
                        + bundle.getString("lb_first") + "</SPAN> | ");
            }
            else
            {
                sb.append("<A CLASS=standardHREF HREF=" + p_baseURL
                        + "&jobListStart=0>" + bundle.getString("lb_first")
                        + "</A> | ");
            }

            // The "Previous" link
            if (jobListStart == 0)
            {
                // Don't hyperlink "Previous" if it's the first page
                sb.append("<SPAN CLASS=standardTextGray>"
                        + bundle.getString("lb_previous") + "</SPAN> | ");
            }
            else
            {
                sb.append("<A CLASS=standardHREF HREF=" + p_baseURL
                        + "&jobListStart=" + (jobListStart - s_jobsPerPage)
                        + ">" + bundle.getString("lb_previous") + "</A> | ");
            }

            // Show page numbers 1 2 3 4 5 etc...
            for (int i = 1; i <= numOfPages; i++)
            {
                int topJob = (s_jobsPerPage * i) - s_jobsPerPage;
                if (((curPage <= pagesOnLeftOrRight) && (i <= numOfPagesInGroup))
                        || (((numOfPages - curPage) <= pagesOnLeftOrRight) && (i > (numOfPages - numOfPagesInGroup)))
                        || ((i <= (curPage + pagesOnLeftOrRight)) && (i >= (curPage - pagesOnLeftOrRight))))
                {
                    if (jobListStart == topJob)
                    {
                        // Don't hyperlink this page if it's current
                        sb.append("<SPAN CLASS=standardTextGray>" + i
                                + "</SPAN>&nbsp");
                    }
                    else
                    {
                        sb.append("<A CLASS=standardHREF HREF=" + p_baseURL
                                + "&jobListStart=" + (topJob) + ">" + i
                                + "</A>&nbsp");
                    }
                }
            }

            // The "Next" link
            if ((jobListStart + s_jobsPerPage) >= numOfJobs)
            {
                // Don't hyperlink "Next" if it's the last page
                sb.append("| &nbsp" + "<SPAN CLASS=standardTextGray>"
                        + bundle.getString("lb_next") + "</SPAN> | ");
            }
            else
            {
                sb.append("| &nbsp" + "<A CLASS=standardHREF HREF=" + p_baseURL
                        + "&jobListStart=" + (jobListStart + s_jobsPerPage)
                        + ">" + bundle.getString("lb_next") + "</A> | ");
            }

            // The "Last" link
            int lastJob = numOfJobs - 1; // Index of last job
            int numJobsOnLastPage = numOfJobs % s_jobsPerPage == 0 ? s_jobsPerPage
                    : numOfJobs % s_jobsPerPage;
            if ((lastJob - jobListStart) < s_jobsPerPage)
            {
                // Don't hyperlink "Last" if it's the Last page
                sb.append("<SPAN CLASS=standardTextGray>"
                        + bundle.getString("lb_last") + "</SPAN>");
            }
            else
            {
                sb.append("<A CLASS=standardHREF HREF=" + p_baseURL
                        + "&jobListStart="
                        + (lastJob - (numJobsOnLastPage - 1)) + ">"
                        + bundle.getString("lb_last") + "</A>");
            }
        }
        else
        {
            sb.append(bundle.getString("lb_displaying_zero"));
        }

        return sb.toString();
    }

    @SuppressWarnings("unchecked")
    protected void sortJobs(HttpServletRequest p_request,
            HttpSession p_session, Locale p_uiLocale, List p_jobs,
            Currency p_currency)
    {
        // first get job comparator from session
        JobComparator comparator = (JobComparator) p_session
                .getAttribute(COMPARATOR);
        if (comparator == null)
        {
            // Default: Sort by Job ID, descending, so the latest job
            // will be at the top of the list
            comparator = new JobComparator(JobComparator.JOB_ID, p_uiLocale,
                    false, p_currency);
            p_session.setAttribute(COMPARATOR, comparator);
        }
        String criteria = p_request.getParameter(SORT_PARAM);

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

        Collections.sort(p_jobs, comparator);
        p_session.setAttribute(SORT_COLUMN, new Integer(comparator
                .getSortColumn()));
        p_session.setAttribute(SORT_ASCENDING, new Boolean(comparator
                .getSortAscending()));
    }

    @SuppressWarnings("unchecked")
    protected HashSet getExpJobListing(HttpServletRequest p_request)
    {
        if (p_request.getParameter(EXPANDED_JOB_DISPLAY_PARAM) != null)
        {
            getHashSet(p_request, SET_EXPANDED_JOBS).add(
                    p_request.getParameter(EXPANDED_JOB_DISPLAY_PARAM));
        }
        if (p_request.getParameter(COLLAPSE_JOB_DISPLAY_PARAM) != null)
        {
            getHashSet(p_request, SET_EXPANDED_JOBS).remove(
                    p_request.getParameter(COLLAPSE_JOB_DISPLAY_PARAM));
        }
        return getHashSet(p_request, SET_EXPANDED_JOBS);
    }

    static HashSet getHashSet(HttpServletRequest p_request, String p_setName)
    {
        SessionManager sessionMgr = (SessionManager) p_request
                .getSession(false).getAttribute(SESSION_MANAGER);
        HashSet set = (HashSet) sessionMgr.getAttribute(p_setName);
        if (set == null)
        {
            set = new HashSet();
            sessionMgr.setAttribute(p_setName, set);
        }
        return set;
    }

    /**
     * Calculates the workflow cost and returns a string in the right format for
     * the currency. If there is an error and the cost cannot be calculated, it
     * returns "--". <br>
     * 
     * @param p_workflow --
     *            the workflow
     * @param p_curr --
     *            the currency
     * @return a currency formatted string
     */
    static String getWorkflowCost(Workflow p_workflow, String p_curr,
            int p_costType)
    {
        String formattedCost = null;
        try
        {
            Currency oCurrency = (Currency) ServerProxy.getCostingEngine()
                    .getCurrency(p_curr);
            Cost cost = ServerProxy.getCostingEngine().calculateCost(
                    p_workflow, oCurrency, false, p_costType);
            formattedCost = cost.getEstimatedCost().getFormattedAmount();
        }
        catch (Exception e)
        {
            CATEGORY
                    .error(
                            "JobManagerHandler::getWorkflowCost:Problem getting Costing details ",
                            e);
            formattedCost = "--";
        }

        return formattedCost;
    }

    static int getTotalWordCount(Workflow p_workflow, int p_type)
    {
        int count = 0;

        switch (p_type)
        {
            case EXACT_MATCH:
                count = p_workflow.getSegmentTmWordCount();
                break;
            case CONTEXT_MATCH:
                count = p_workflow.getContextMatchWordCount();
                break;
            case FUZZY_MATCH:
                count = p_workflow.getHiFuzzyMatchWordCount();
                break;
            case LOW_FUZZY_MATCH:
                count = p_workflow.getLowFuzzyMatchWordCount();
                break;
            case MED_FUZZY_MATCH:
                count = p_workflow.getMedFuzzyMatchWordCount();
                break;
            case MED_HI_FUZZY_MATCH:
                count = p_workflow.getMedHiFuzzyMatchWordCount();
                break;
            case UNMATCHED:
                count = p_workflow.getNoMatchWordCount();
                break;
            case REPETITION:
                count = p_workflow.getRepetitionWordCount();
                break;
            case TOTAL_WF_WORD_CNT:
                count = p_workflow.getTotalWordCount();
                break;
        }
        return count;
    }

    // get the currency options as HTML (for the currency dialog)
    @SuppressWarnings("unchecked")
    private String getCurrenciesHTML(Locale locale)
    {
        StringBuffer sb = new StringBuffer();
        try
        {
            sb.append("<currencyOptions>");
            Vector allCurrencies = new Vector(ServerProxy.getCostingEngine()
                    .getCurrencies());
            int size = allCurrencies.size();
            for (int i = 0; i < size; i++)
            {
                Currency c = (Currency) allCurrencies.elementAt(i);
                String currencyName = c.getDisplayName(locale);
                String isoCode = c.getIsoCode();
                sb.append("<currency><name>");
                sb.append(currencyName); // iso code is included in properties file
                sb.append("</name><value>");
                sb.append(isoCode);
                sb.append("</value></currency>");
            }
            sb.append("</currencyOptions>");
        }
        catch (Exception e)
        {
            CATEGORY
                    .error(
                            "JobHandlerMain::invokeJobControlPage():Problem getting Currencies from the system ",
                            e);
        }
        return sb.toString();
    }

    // get an appropriate display name for job.
    private String getJobNameHTML(Job p_job, String p_detailsURL,
            String p_state, boolean hasDetailPerm)
    {
        StringBuffer sb = new StringBuffer();
        if (hasDetailPerm)
        {
        	sb.append("<TD width=\"210px\" style=\"word-wrap:break-word;word-break:break-all\">");
        	sb.append("<SCRIPT language=\"javascript\">");
        	sb.append("if (navigator.userAgent.indexOf(\'Firefox\') >= 0){document.write(\"<DIV style=\'width:200px\'>\");}");
        	sb.append("</SCRIPT>");
            sb.append("<B><A CLASS=\"");
            if (p_state.equals(Job.EXPORT_FAIL)
                    || p_job.getState().equals(Job.IMPORTFAILED)
                    || p_job.containsFailedWorkflow())
                sb.append("warning");
            else
                sb.append("standard");
            sb.append("HREF\" HREF=\"");
            sb.append(p_detailsURL);
            sb.append("&");
            sb.append(JOB_ID);
            sb.append("=");
            sb.append(p_job.getJobId());
            sb.append("&fromJobs=true");
            sb.append("\">");
            sb.append(p_job.getJobName());
            sb.append("</A></B>");
        }
        else
        {
            sb.append("<TD class=\"standardText\">");
            sb.append("<SCRIPT language=\"javascript\">");
        	sb.append("if (navigator.userAgent.indexOf(\'Firefox\') >= 0){document.write(\"<DIV style=\'width:200px\'>\");}");
        	sb.append("</SCRIPT>");
            sb.append(p_job.getJobName());
        }
        sb.append("<SCRIPT language=\"javascript\">");
        sb.append("if (navigator.userAgent.indexOf(\'Firefox\') >= 0){document.write(\"</DIV>\")}");
        sb.append("</SCRIPT>");
        sb.append("</TD>\n");
        return sb.toString();
    }

    @SuppressWarnings("deprecation")
//    private String getPlannedDateHTML(Job p_job, String p_pdateURL,
//            Locale p_uiLocale, Timestamp ts, boolean hasPerm)
//    {
//        // get the date
//        Collection wfs = p_job.getWorkflows();
//        Date planned = null;
//        Iterator iter = wfs.iterator();
//        while (iter.hasNext())
//        {
//            Workflow wf = (Workflow) iter.next();
//            Date d = wf.getPlannedCompletionDate();
//            // note that 'd' can be null for a non-dispatched workflow
//            if (planned == null || (d != null && planned.compareTo(d) < 0))
//            {
//                planned = d;
//            }
//        }
//        // create the html
//        StringBuffer sb = new StringBuffer();
//        if (p_job.getState().equals(Workflow.DISPATCHED) && hasPerm)
//        {
//            sb.append("<TD><A STYLE=\"word-wrap:break-word\" CLASS=\"");
//            sb.append("standard");
//            sb.append("HREF\" HREF=\"");
//            sb.append(p_pdateURL);
//            sb.append("&");
//            sb.append(JOB_ID);
//            sb.append("=");
//            sb.append(p_job.getJobId());
//            sb.append("&");
//            sb.append(JOB_STATE);
//            sb.append("=");
//            sb.append(p_job.getState());
//            sb.append("\">");
//
//            // For sla report issue
//            // Ori Code:
//            // sb.append(DateHelper.getFormattedDate(planned, p_uiLocale));
//            ts.setDate(planned);
//
//            sb.append(ts);
//            sb.append("</A>");
//            sb.append("</TD>\n");
//        }
//        else
//        {
//            sb
//                    .append("<TD  STYLE=\"padding-right: 10px;\"><SPAN CLASS=standardText>");
//            ts.setDate(planned);
//            sb.append(ts);
//            sb.append("</SPAN></TD>\n");
//        }
//
//        return sb.toString();
//    }

    private String getEstimatedJobDateHTML(Job p_job, Timestamp ts)
    {
        StringBuffer sb = new StringBuffer();
        sb.append("<TD  STYLE=\"padding-right: 10px;\"><SPAN CLASS=\"");
        sb.append("standardText");
        sb.append("\">");
        Date d = p_job.getDueDate();

        if (d != null)
        {
            ts.setDate(d);
            sb.append(ts);
        }
        else
        {
            sb.append("--");
        }

        sb.append("</SPAN></TD>\n");

        return sb.toString();
    }

    private String getEstimatedTranslateDateHTML(Job p_job, Timestamp ts)
    {
        // get the date
        Collection wfs = p_job.getWorkflows();
        Date estimated = null;
        Iterator iter = wfs.iterator();
        while (iter.hasNext())
        {
            Workflow wf = (Workflow) iter.next();
            Date d = wf.getEstimatedTranslateCompletionDate();
            // note that 'd' can be null for a non-dispatched workflow
            if (estimated == null || (d != null && estimated.compareTo(d) < 0))
            {
                estimated = d;
            }
        }
        // create the html
        StringBuffer sb = new StringBuffer();
        sb
                .append("<TD  STYLE=\"padding-right: 10px;\"><SPAN CLASS=standardText>");

        if (estimated != null)
        {
            ts.setDate(estimated);
            sb.append(ts);
        }
        else
        {
            sb.append("--");
        }

        sb.append("</SPAN></TD>\n");

        return sb.toString();
    }

    // Get the string representation of the pivot currency
    private String getPivodCurrency(HttpServletRequest p_request,
            SessionManager p_sessionMgr)
    {
        HttpSession session = p_request.getSession(false);
        String curr = (String) p_request
                .getParameter(JobManagementHandler.CURRENCY);
        curr = curr == null ? (String) session
                .getAttribute(JobManagementHandler.CURRENCY) : curr;

        if (curr == null)
        {
            // Get the pivot currency;
            try
            {
                Currency c = ServerProxy.getCostingEngine().getPivotCurrency();
                curr = c.getIsoCode();
            }
            catch (Exception e)
            {
                CATEGORY
                        .error(
                                "JobHandlerMain::invokeJobControlPage():Problem getting pivot currency ",
                                e);
            }
        }

        return curr;
    }

    private List getInProgressWorkflows()
    {
        String sql = "SELECT DISTINCT(WORKFLOW_IFLOW_INSTANCE_ID) "
                + "FROM TARGET_PAGE " + "WHERE LENGTH(STATE) = 18"; // "EXPORT_IN_PROGRESS".length()
        Connection cn = null;
        ResultSet rs = null;
        List<Long> list = new ArrayList<Long>();
        try
        {
            cn = ConnectionPool.getConnection();
            PreparedStatement ps = cn.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next())
            {
                list.add(new Long(rs.getLong(1)));
            }
            ps.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            try
            {
                ConnectionPool.returnConnection(cn);
            }
            catch (ConnectionPoolException e1)
            {
                e1.printStackTrace();
            }
        }
        return list;
    }

    // Return true if any of the target pages on the given job is in the
    // Export-in-progress state.
    private boolean exportIsInProgress(Job p_job, List inProgressWfs)
    {
        boolean inProgress = false;
        Iterator it = p_job.getWorkflows().iterator();
        while (!inProgress && it.hasNext())
        {
            // inProgress = exportIsInProgress((Workflow)it.next());
            inProgress = inProgressWfs.contains(new Long(((Workflow) it.next())
                    .getId()));
        }
        return inProgress;
    }

    // Return true if any of the target pages on the given workflow is in the
    // Export-in-progress state.
//    private boolean exportIsInProgress(Workflow p_workflow)
//    {
//        boolean inProgress = false;
//        Vector tps = p_workflow.getTargetPages();
//        for (int i = 0; !inProgress && i < tps.size(); i++)
//        {
//            TargetPage tp = (TargetPage) tps.elementAt(i);
//            inProgress = (PageState.EXPORT_IN_PROGRESS).equals(tp
//                    .getPageState());
//        }
//        return inProgress;
//    }

    // determine the starting index of the job list.
    private int determineStartIndex(HttpServletRequest p_request,
            HttpSession p_session, SessionManager p_sm)
    {
        int jobListStart;
        try
        {
            String jobListStartStr = (String) p_request
                    .getParameter(JOB_LIST_START);

            jobListStart = Integer.parseInt(jobListStartStr);
            p_sm.setAttribute(JOB_LIST_START, new Integer(jobListStart));
        }
        // this exception happens if you go to My Jobs from menu items.
        // Also when you click on sorting columns.
        catch (NumberFormatException e)
        {
            String activityName = (String) p_request
                    .getParameter(LinkHelper.ACTIVITY_NAME);
            Integer jls = null;
            // If user clicks on menu item, activityName in not null.
            // Therefore, we should not preserve paging.
            if (activityName == null)
            {
                jls = (Integer) p_sm.getAttribute(JOB_LIST_START);
            }

            jobListStart = jls == null ? 0 : jls.intValue();
        }
        return jobListStart;
    }

    /**
     * Return the search criteria, and also set it in the session.
     */
    private JobSearchParameters getSearchParams(HttpServletRequest request,
            HttpSession session, User user, String searchType)
            throws EnvoyServletException
    {
        JobSearchParameters sp = new JobSearchParameters();
        sp.setUser(user);
        SessionManager sessionMgr = (SessionManager) session
                .getAttribute(WebAppConstants.SESSION_MANAGER);

        // adding search criteria
        if (request.getParameter("fromRequest") != null)
        {
            // New search
            sessionMgr.removeElement(JOB_LIST_START);
            session.setAttribute(JobSearchConstants.LAST_JOB_SEARCH_TYPE,
                    searchType);
            return getRequestSearchParams(request, sessionMgr, sp, user
                    .getUserId(), searchType);
        }
        else
        {
            // Get search from session
            return getSessionSearchParams(request, session, sp, searchType);
        }
    }

    private JobSearchParameters getRequestSearchParams(
            HttpServletRequest request, SessionManager sessionMgr,
            JobSearchParameters sp, String userId, String searchType)
            throws EnvoyServletException
    {
        StringBuffer jobSearch = new StringBuffer();
        try
        {
            // set parameters

            // name
            String buf = (String) request
                    .getParameter(JobSearchConstants.NAME_FIELD);
            if (buf.trim().length() != 0)
            {
                sp.setJobName(buf);
                sp.setJobNameCondition((String) request
                        .getParameter(JobSearchConstants.NAME_OPTIONS));
                jobSearch
                        .append(JobSearchConstants.NAME_OPTIONS
                                + "="
                                + request
                                        .getParameter(JobSearchConstants.NAME_OPTIONS));
                jobSearch.append(":");
                jobSearch.append(JobSearchConstants.NAME_FIELD + "=" + buf);
                jobSearch.append(":");
            }

            // status
            List<String> list = new ArrayList<String>();
            String status = (String) request
                    .getParameter(JobSearchConstants.STATUS_OPTIONS);
            if (status.equals(Job.ALLSTATUS))
            {
                list.add(Job.PENDING);
                list.add(Job.BATCHRESERVED);
                list.add(Job.IMPORTFAILED);
                list.add(Job.READY_TO_BE_DISPATCHED);
                list.add(Job.DISPATCHED);
                list.add(Job.LOCALIZED);
                list.add(Job.EXPORTED);
                list.add(Job.EXPORT_FAIL);
                list.add(Job.ARCHIVED);
                list.add(Job.ADD_FILE);
            }
            else
            {
                list.add(status);
                if (status.equals(Job.PENDING))
                {
                    list.add(Job.IMPORTFAILED);
                }
                else if (status.equals(Job.EXPORTED))
                {
                    list.add(Job.EXPORT_FAIL);
                }
            }
            sp.setJobState(list);
            jobSearch.append(JobSearchConstants.STATUS_OPTIONS + "=" + status);
            jobSearch.append(":");

            if (searchType.equals(JobSearchConstants.MINI_JOB_SEARCH_COOKIE))
            {
                String cookieName = JobSearchConstants.MINI_JOB_SEARCH_COOKIE
                        + userId.hashCode();
                Cookie cookie = new Cookie(cookieName, jobSearch.toString());
                sessionMgr.setAttribute(cookieName, cookie);
                return sp;
            }

            // id
            buf = (String) request.getParameter(JobSearchConstants.ID_FIELD);
            if (buf.trim().length() != 0)
            {
                sp.setJobId(buf);
                sp.setJobIdCondition(request
                        .getParameter(JobSearchConstants.ID_OPTIONS));
                jobSearch.append(JobSearchConstants.ID_OPTIONS + "="
                        + request.getParameter(JobSearchConstants.ID_OPTIONS));
                jobSearch.append(":");
                jobSearch.append(JobSearchConstants.ID_FIELD + "=" + buf);
                jobSearch.append(":");
            }

            // project
            buf = (String) request
                    .getParameter(JobSearchConstants.PROJECT_OPTIONS);
            if (!buf.equals("-1"))
            {
                sp.setProjectId(buf);
                jobSearch
                        .append(JobSearchConstants.PROJECT_OPTIONS + "=" + buf);
                jobSearch.append(":");
            }

            // source locale
            buf = (String) request.getParameter(JobSearchConstants.SRC_LOCALE);
            if (!buf.equals("-1"))
            {
                sp.setSourceLocale(ServerProxy.getLocaleManager()
                        .getLocaleById(Long.parseLong(buf)));
                jobSearch.append(JobSearchConstants.SRC_LOCALE + "=" + buf);
                jobSearch.append(":");
            }

            // target locale
            buf = (String) request.getParameter(JobSearchConstants.TARG_LOCALE);
            if (!buf.equals("-1"))
            {
                sp.setTargetLocale(ServerProxy.getLocaleManager()
                        .getLocaleById(Long.parseLong(buf)));
                jobSearch.append(JobSearchConstants.TARG_LOCALE + "=" + buf);
                jobSearch.append(":");
            }

            // priority
            buf = (String) request
                    .getParameter(JobSearchConstants.PRIORITY_OPTIONS);
            if (!buf.equals("-1"))
            {
                sp.setPriority(buf);
                jobSearch.append(JobSearchConstants.PRIORITY_OPTIONS + "="
                        + buf);
                jobSearch.append(":");
            }

            // Creation Date start num and condition
            buf = (String) request
                    .getParameter(JobSearchConstants.CREATION_START);
            if (buf.trim().length() != 0)
            {
                sp.setCreationStart(new Integer(buf));
                sp
                        .setCreationStartCondition(request
                                .getParameter(JobSearchConstants.CREATION_START_OPTIONS));
                jobSearch.append(JobSearchConstants.CREATION_START + "=" + buf);
                jobSearch.append(":");
                jobSearch
                        .append(JobSearchConstants.CREATION_START_OPTIONS
                                + "="
                                + request
                                        .getParameter(JobSearchConstants.CREATION_START_OPTIONS));
                jobSearch.append(":");
            }

            // Creation Date end num
            buf = (String) request
                    .getParameter(JobSearchConstants.CREATION_END);
            if (buf.trim().length() != 0)
            {
                sp.setCreationEnd(new Integer(buf));
                jobSearch.append(JobSearchConstants.CREATION_END + "=" + buf);
                jobSearch.append(":");
            }

            // Creation Date end condition
            buf = (String) request
                    .getParameter(JobSearchConstants.CREATION_END_OPTIONS);
            if (buf.length() != 0)
            {
                sp.setCreationEndCondition(buf);
                jobSearch.append(JobSearchConstants.CREATION_END_OPTIONS + "="
                        + buf);
                jobSearch.append(":");
            }

            // Completion Date start num and condition
            buf = (String) request
                    .getParameter(JobSearchConstants.EST_COMPLETION_START);
            if (buf.trim().length() != 0)
            {
                sp.setEstCompletionStart(new Integer(buf));
                sp
                        .setEstCompletionStartCondition(request
                                .getParameter(JobSearchConstants.EST_COMPLETION_START_OPTIONS));
                jobSearch.append(JobSearchConstants.EST_COMPLETION_START + "="
                        + buf);
                jobSearch.append(":");
                jobSearch
                        .append(JobSearchConstants.EST_COMPLETION_START_OPTIONS
                                + "="
                                + request
                                        .getParameter(JobSearchConstants.EST_COMPLETION_START_OPTIONS));
                jobSearch.append(":");
            }

            // Completion Date end num
            buf = (String) request
                    .getParameter(JobSearchConstants.EST_COMPLETION_END);
            if (buf.trim().length() != 0)
            {
                sp.setEstCompletionEnd(new Integer(buf));
                jobSearch.append(JobSearchConstants.EST_COMPLETION_END + "="
                        + buf);
                jobSearch.append(":");
            }

            // Completion Date end condition
            buf = (String) request
                    .getParameter(JobSearchConstants.EST_COMPLETION_END_OPTIONS);
            if (buf.length() != 0)
            {
                sp.setEstCompletionEndCondition(buf);
                jobSearch.append(JobSearchConstants.EST_COMPLETION_END_OPTIONS
                        + "=" + buf);
                jobSearch.append(":");
            }

            String cookieName = JobSearchConstants.JOB_SEARCH_COOKIE
                    + userId.hashCode();
            Cookie cookie = new Cookie(cookieName, jobSearch.toString());
            sessionMgr.setAttribute(cookieName, cookie);
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }

        return sp;
    }

    private JobSearchParameters getSessionSearchParams(
            HttpServletRequest request, HttpSession session,
            JobSearchParameters sp, String searchType)
            throws EnvoyServletException
    {
        try
        {
            Cookie cookie = JobSearchHandlerHelper.getJobSearchCookie(session,
                    request, searchType);
            if (cookie == null) return sp;

            StringTokenizer strtok = new StringTokenizer(cookie.getValue(), ":");
            while (strtok.hasMoreTokens())
            {
                String tok = strtok.nextToken();
                int idx = tok.indexOf("=");
                String key = tok.substring(0, idx);
                String value = tok.substring(idx + 1);
                if (key.equals(JobSearchConstants.NAME_FIELD))
                {
                    if (!value.equals("")) sp.setJobName(value);
                }
                else if (key.equals(JobSearchConstants.NAME_OPTIONS))
                {
                    sp.setJobNameCondition(value);
                }
                else if (key.equals(JobSearchConstants.ID_FIELD))
                {
                    if (!value.equals("")) sp.setJobId(value);
                }
                else if (key.equals(JobSearchConstants.ID_OPTIONS))
                {
                    if (!value.equals("-1")) sp.setJobIdCondition(value);
                }
                else if (key.equals(JobSearchConstants.STATUS_OPTIONS))
                {
                    List<String> list = new ArrayList<String>();
                    if (value.equals(Job.ALLSTATUS))
                    {
                        list.add(Job.PENDING);
                        list.add(Job.BATCHRESERVED);
                        list.add(Job.IMPORTFAILED);
                        list.add(Job.READY_TO_BE_DISPATCHED);
                        list.add(Job.DISPATCHED);
                        list.add(Job.LOCALIZED);
                        list.add(Job.EXPORTED);
                        list.add(Job.EXPORT_FAIL);
                        list.add(Job.ARCHIVED);
                        list.add(Job.ADD_FILE);
                    }
                    else
                    {
                        list.add(value);
                        if (value.equals(Job.PENDING))
                        {
                            list.add(Job.IMPORTFAILED);
                            list.add(Job.ADD_FILE);
                        }
                        else if (value.equals(Job.EXPORTED))
                        {
                            list.add(Job.EXPORT_FAIL);
                        }
                    }
                    sp.setJobState(list);
                }
                else if (key.equals(JobSearchConstants.PROJECT_OPTIONS))
                {
                    if (!value.equals("-1")) sp.setProjectId(value);
                }
                else if (key.equals(JobSearchConstants.SRC_LOCALE))
                {
                    if (!value.equals("-1"))
                        sp.setSourceLocale(ServerProxy.getLocaleManager()
                                .getLocaleById(Long.parseLong(value)));
                }
                else if (key.equals(JobSearchConstants.TARG_LOCALE))
                {
                    if (!value.equals("-1"))
                        sp.setTargetLocale(ServerProxy.getLocaleManager()
                                .getLocaleById(Long.parseLong(value)));
                }
                else if (key.equals(JobSearchConstants.PRIORITY_OPTIONS))
                {
                    if (!value.equals("-1")) sp.setPriority(value);
                }
                else if (key.equals(JobSearchConstants.CREATION_START))
                {
                    if (!value.equals(""))
                        sp.setCreationStart(new Integer(value));
                }
                else if (key.equals(JobSearchConstants.CREATION_START_OPTIONS))
                {
                    if (!value.equals("-1"))
                        sp.setCreationStartCondition(value);
                }
                else if (key.equals(JobSearchConstants.CREATION_END))
                {
                    if (!value.equals(""))
                        sp.setCreationEnd(new Integer(value));
                }
                else if (key.equals(JobSearchConstants.CREATION_END_OPTIONS))
                {
                    if (!value.equals("-1")) sp.setCreationEndCondition(value);
                }
                else if (key.equals(JobSearchConstants.EST_COMPLETION_START))
                {
                    if (!value.equals(""))
                        sp.setEstCompletionStart(new Integer(value));
                }
                else if (key
                        .equals(JobSearchConstants.EST_COMPLETION_START_OPTIONS))
                {
                    if (!value.equals("-1"))
                        sp.setEstCompletionStartCondition(value);
                }
                else if (key.equals(JobSearchConstants.EST_COMPLETION_END))
                {
                    if (!value.equals(""))
                        sp.setEstCompletionEnd(new Integer(value));
                }
                else if (key
                        .equals(JobSearchConstants.EST_COMPLETION_END_OPTIONS))
                {
                    if (!value.equals("-1"))
                        sp.setEstCompletionEndCondition(value);
                }
            }
            return sp;
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private List mergeLists(List jobs1, List jobs2)
    {
        if (jobs2 != null)
        {
            for (int i = 0; i < jobs2.size(); i++)
            {
                Job job = (Job) jobs2.get(i);
                if (!jobs1.contains(job))
                {
                    jobs1.add(job);
                }
            }
        }
        return jobs1;
    }
}
