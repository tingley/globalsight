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
package com.globalsight.everest.webapp.pagehandler.administration.cotijob;

import java.io.IOException;
import java.rmi.RemoteException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TimeZone;
import java.util.Vector;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import com.globalsight.everest.coti.COTIJob;
import com.globalsight.everest.coti.util.COTIConstants;
import com.globalsight.everest.coti.util.COTIDbUtil;
import com.globalsight.everest.foundation.Timestamp;
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.util.system.SystemConfigParamNames;
import com.globalsight.everest.util.system.SystemConfiguration;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.javabean.NavigationBean;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.webnavigation.LinkHelper;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.util.SortUtil;

public abstract class CotiJobsManagement extends PageHandler
{
    // color
    public static final String WHITE_BG = "#FFFFFF";

    public static final String GREY_BG = "#EEEEEE";

    public static final String JOB_LIST_START_PARAM = "coti_jobListStart";

    public static final String JOB_LIST_START = "coti_jobListStart";

    private static final String COMPARATOR = "coti_comparator";

    // sort
    public static final String SORT_COLUMN = "coti_sortColumn";

    
    public static final String CHECKBOX_NAME = "coti_CHECKBOX_NAME";
    
    public static final String SORT_ASCENDING = "coti_sortAscending";

    public static final String SORT_PARAM = "coti_sort";

    public static final String NUM_OF_JOBS = "coti_numOfJobs";
    
    public static final String PAGING_SCRIPTLET = "coti_pagingJSPData";

    // table display
    public static final String TABLE_WIDTH = "613";

    public static final String TABLE_WIDTH_PARAM = "divWidth";

    public static final String COST_COL_WIDTH = "115";

    public static final String COST_COL_WIDTH_PARAM = "costColWidth";
    
    public static final String DETAIL_BEAN = "cojd";

    private static final Logger CATEGORY = Logger
            .getLogger(CotiJobsManagement.class.getName());

    // semaphore to control access to Job List query since it is performance
    // intensive
    private static String s_semaphore = new String("semaphore");

    private static int s_semCount = 0;
    
    private String jobDetailsURL = "/globalsight/ControlServlet?linkName=jobDetails&pageName=ALLS";

    /** Returns true if the execution of the My Jobs tab can start */
    protected boolean startExecution()
    {
        int maxMyJobsThreads = getMaxMyJobsThreads();
        synchronized (s_semaphore)
        {
            if (s_semCount < maxMyJobsThreads)
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

    private int getMaxMyJobsThreads()
    {
        String value = SystemConfiguration
                .getParameter(SystemConfigParamNames.MAX_MY_JOBS_THREADS);
        int maxMyJobsThreads = 5;
        if (value != null)
        {
            try
            {
                maxMyJobsThreads = Integer.parseInt(value);
            }
            catch (NumberFormatException nfe)
            {
                // use default 5 if meeting format error
            }
        }
        return maxMyJobsThreads;
    }

    /** Decrements the count * */
    protected void stopExecution()
    {
        synchronized (s_semaphore)
        {
            if (s_semCount > -1)
                s_semCount--;
            CATEGORY.debug("down count is now: " + s_semCount);
        }
    }

    // Get the Records Per Page param
    public static int s_jobsPerPage = 20;
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
            CATEGORY.error(
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
        Locale uiLocale = (Locale) session
                .getAttribute(WebAppConstants.UILOCALE);
        
        NavigationBean baseBean = null;
        NavigationBean detailBean = null;
        
        Enumeration en = p_pageDescriptor.getLinkNames();

        while (en.hasMoreElements())
        {
            String linkName = (String) en.nextElement();
            String pageName = p_pageDescriptor.getPageName();
            if (linkName.equals(p_baseBeanLinkName))
            {
                baseBean = new NavigationBean(linkName, pageName);
            }
            else if (linkName.equals(DETAIL_BEAN))
            {
                detailBean = new NavigationBean(linkName, pageName);
            }
            // create a navigation bean for each link
            NavigationBean aNavigationBean = new NavigationBean(linkName,
                    pageName);
            // each navigation bean will be labelled with the name of the link
            p_request.setAttribute(linkName, aNavigationBean);
        }

        HashMap<String, NavigationBean> beanMap = new HashMap<String, NavigationBean>(
                5);
        beanMap.put(p_baseBeanLinkName, baseBean);
        beanMap.put(DETAIL_BEAN, detailBean);

        return beanMap;
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

    // ////////////////////////////////////////////////////////////////////
    // End: Abstract Methods
    // ////////////////////////////////////////////////////////////////////

    // ////////////////////////////////////////////////////////////////////
    // Begin: Helper Methods
    // ////////////////////////////////////////////////////////////////////

    /**
     * Creates the paging widget for all My Jobs tabs so you can page through
     * the list 20 jobs at a time.
     * <P>
     * Display should look like:
     * <P>
     * Displaying 1 - 20 of 54 First | Previous | Next | Last
     * <P>
     * 
     * @param p_request
     *            -- the workflow
     * @param p_baseURL
     *            -- the URL of the tab
     * @param p_stateList
     *            -- list of acceptable job states for the given tab
     * @return a formatted string
     */
    protected String getPagingText(HttpServletRequest p_request,
            String p_baseURL, String p_jobState) throws RemoteException,
            EnvoyServletException
    {
        Vector<String> jobState = new Vector<String>();
        if (p_jobState == null)
        {
            jobState.add(COTIConstants.project_status_cancelled);
            jobState.add(COTIConstants.project_status_closed);
            jobState.add(COTIConstants.project_status_created);
            jobState.add(COTIConstants.project_status_finished);
            jobState.add(COTIConstants.project_status_rejected);
            jobState.add(COTIConstants.project_status_started);
            jobState.add(COTIConstants.project_status_unknown);
        }
        else
        {
            jobState.add(p_jobState);
        }
        return getJobsPagingText(p_request, p_baseURL, jobState);
    }

    protected String getJobsPagingText(HttpServletRequest p_request,
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

        int numPerPage = 10;
        if (sessionMgr.getMyjobsAttribute("cotinumPerPage") != null)
            numPerPage = (Integer) sessionMgr.getMyjobsAttribute("cotinumPerPage");
        int numOfPages = numOfJobs / numPerPage;
        if (numOfPages * numPerPage != numOfJobs)
            numOfPages++;
        int jobListEnd = (jobListStart + numPerPage) > numOfJobs ? numOfJobs
                : (jobListStart + numPerPage);
        if (jobListStart > jobListEnd)
        {
            jobListStart = jobListEnd / numPerPage * numPerPage;
            if (jobListEnd % numPerPage == 0)
            {
                jobListStart = jobListStart - numPerPage;
            }
        }
        else if (jobListStart == jobListEnd)
        {
            jobListStart = (jobListEnd / numPerPage - 1) * numPerPage;
        }
        int curPage = jobListStart / numPerPage + 1;
        int numOfPagesInGroup = WebAppConstants.NUMBER_OF_PAGES_IN_GROUP;
        int pagesOnLeftOrRight = numOfPagesInGroup / 2;

        /*
         * Note that these two integers, jobListStart and jobListEnd, are the
         * *indexes* of the list so for display we'll add 1 so it will look good
         * for the user. This is also why I subtract 1 from jobListEnd below, so
         * that jobListEnd will be an index value
         */
        jobListEnd = jobListEnd - 1;

        StringBuffer sb = new StringBuffer();

        // Make the Paging widget
        if (numOfJobs > 0)
        {
            Object[] args = { new Integer(jobListStart + 1),
                    new Integer(jobListEnd + 1), new Integer(numOfJobs) };

            // "Displaying 1 - 20 of 35"
            sb.append(
                    MessageFormat.format(
                            bundle.getString("lb_displaying_records"), args))
                    .append("&nbsp");

            // The "First" link
            if (jobListStart == 0)
            {
                // Don't hyperlink "First" if it's the first page
                sb.append("<SPAN CLASS=standardTextGray>"
                        + bundle.getString("lb_first") + "</SPAN> | ");
            }
            else
            {
                sb.append("<A CLASS=standardHREF onclick='return addFilters(this)' HREF="
                        + p_baseURL
                        + "&cotijobListStart=0>"
                        + bundle.getString("lb_first") + "</A> | ");
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
                sb.append("<A CLASS=standardHREF onclick='return addFilters(this)' HREF="
                        + p_baseURL
                        + "&cotijobListStart="
                        + (jobListStart - numPerPage)
                        + ">"
                        + bundle.getString("lb_previous") + "</A> | ");
            }

            // Show page numbers 1 2 3 4 5 etc...
            for (int i = 1; i <= numOfPages; i++)
            {
                int topJob = (numPerPage * i) - numPerPage;
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
                        sb.append("<A CLASS=standardHREF onclick='return addFilters(this)' HREF="
                                + p_baseURL
                                + "&cotijobListStart="
                                + (topJob)
                                + ">"
                                + i + "</A>&nbsp");
                    }
                }
            }

            // The "Next" link
            if ((jobListStart + numPerPage) >= numOfJobs)
            {
                // Don't hyperlink "Next" if it's the last page
                sb.append("| &nbsp" + "<SPAN CLASS=standardTextGray>"
                        + bundle.getString("lb_next") + "</SPAN> | ");
            }
            else
            {
                sb.append("| &nbsp"
                        + "<A CLASS=standardHREF onclick='return addFilters(this)' HREF="
                        + p_baseURL + "&cotijobListStart="
                        + (jobListStart + numPerPage) + ">"
                        + bundle.getString("lb_next") + "</A> | ");
            }

            // The "Last" link
            int lastJob = numOfJobs - 1; // Index of last job
            int numJobsOnLastPage = numOfJobs % numPerPage == 0 ? numPerPage
                    : numOfJobs % numPerPage;
            if ((lastJob - jobListStart) < numPerPage)
            {
                // Don't hyperlink "Last" if it's the Last page
                sb.append("<SPAN CLASS=standardTextGray>"
                        + bundle.getString("lb_last") + "</SPAN>");
            }
            else
            {
                sb.append("<A CLASS=standardHREF onclick='return addFilters(this)' HREF="
                        + p_baseURL
                        + "&cotijobListStart="
                        + (lastJob - (numJobsOnLastPage - 1))
                        + ">"
                        + bundle.getString("lb_last") + "</A>");
            }
        }
        else
        {
            sb.append(bundle.getString("lb_displaying_zero"));
        }

        return sb.toString();
    }

    protected void sortJobs(String p_criteria, HttpSession p_session,
            Locale p_uiLocale, List p_jobs)
    {
        // first get job comparator from session
        CotiJobComparator comparator = (CotiJobComparator) p_session
                .getAttribute(COMPARATOR);
        if (comparator == null)
        {
            // Default: Sort by Job ID, descending, so the latest job
            // will be at the top of the list
            comparator = new CotiJobComparator(CotiJobComparator.JOB_ID,
                    p_uiLocale, false);
            p_session.setAttribute(COMPARATOR, comparator);
        }

        if (p_criteria != null)
        {
            int sortCriteria = Integer.parseInt(p_criteria);

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

        SortUtil.sort(p_jobs, comparator);
        p_session.setAttribute(SORT_COLUMN,
                new Integer(comparator.getSortColumn()));
        p_session.setAttribute(SORT_ASCENDING,
                new Boolean(comparator.getSortAscending()));
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
    
    

    // get an appropriate display name for job.
    private String getJobNameHTML(String id, String name, String p_detailsURL,
            String p_state)
    {
        StringBuffer sb = new StringBuffer();

        sb.append("<TD width=\"210px\" style=\"word-wrap:break-word;word-break:break-all\">");
        sb.append("<SCRIPT language=\"javascript\">");
        sb.append("if (navigator.userAgent.indexOf(\'Firefox\') >= 0){document.write(\"<DIV style=\'width:200px\'>\");}");
        sb.append("</SCRIPT>");
        sb.append("<B><A CLASS=\"");
        if (COTIJob.isWarningText(p_state))
            sb.append("warning");
        else
            sb.append("standard");
        sb.append("HREF\" HREF=\"");
        sb.append(p_detailsURL);
        sb.append("&");
        sb.append(JOB_ID);
        sb.append("=");
        sb.append(id);
        sb.append("&fromCotiJobs=true");
        sb.append("\" ");
        sb.append(">");
        sb.append(name);
        sb.append("</A></B>");
        sb.append("<SCRIPT language=\"javascript\">");
        sb.append("if (navigator.userAgent.indexOf(\'Firefox\') >= 0){document.write(\"</DIV>\")}");
        sb.append("</SCRIPT>");
        sb.append("</TD>\n");
        return sb.toString();
    }
    
    protected void setJobSearchFilters(SessionManager sessionMgr,
            HttpServletRequest p_request, boolean stateMarch)
    {
        String npp = p_request.getParameter("npp");
        boolean isNewNpp = false;
        if (npp != null)
        {
            if (sessionMgr.getMyjobsAttribute("cotinumPerPage") != null)
            {
                int oldNpp = (Integer) sessionMgr
                        .getMyjobsAttribute("cotinumPerPage");
                if (oldNpp != Integer.valueOf(npp))
                    isNewNpp = true;
            }
            sessionMgr.setMyjobsAttribute("cotinumPerPage",
                    Integer.valueOf(npp));
        }

        String jobListStart = p_request.getParameter("cotijobListStart");
        if (isNewNpp)
            jobListStart = "0";
        if (jobListStart != null)
        {
            sessionMgr.setMyjobsAttribute("cotijobListStart", jobListStart);
        }
    }

    // determine the starting index of the job list.
    private int determineStartIndex(HttpServletRequest p_request,
            HttpSession p_session, SessionManager p_sm)
    {
        int jobListStart;
        try
        {
            String jobListStartStr = (String) p_sm
                    .getMyjobsAttribute("cotijobListStart");

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
}
