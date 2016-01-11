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
package com.globalsight.reports.handler;

import java.io.IOException;
import java.sql.Connection;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.jbpm.JbpmContext;
import org.jbpm.graph.exe.ProcessInstance;
import org.jbpm.taskmgmt.exe.TaskInstance;

import com.globalsight.diplomat.util.database.ConnectionPool;
import com.globalsight.everest.costing.Cost;
import com.globalsight.everest.costing.Currency;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.projecthandler.TranslationMemoryProfile;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.taskmanager.Task;
import com.globalsight.everest.util.system.SystemConfigParamNames;
import com.globalsight.everest.util.system.SystemConfiguration;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.pagehandler.administration.users.UserUtil;
import com.globalsight.everest.webapp.tags.TableConstants;
import com.globalsight.everest.workflow.WorkflowConfiguration;
import com.globalsight.everest.workflowmanager.Workflow;
import com.globalsight.reports.Constants;
import com.globalsight.reports.datawrap.BaseDataWrap;
import com.globalsight.reports.util.ReportsPackage;
import com.globalsight.util.SortUtil;
import com.globalsight.util.resourcebundle.ResourceBundleConstants;

public abstract class BasicReportHandler
{
    static final Logger s_category = Logger.getLogger(BasicReportHandler.class);

    public static final String[] SUPPORTED_UI_LOCALES = new String[]
    { "en_US", "fr_FR", "es_ES", "de_DE", "ja_JP" };

    public static final String BUNDLE_LOCATION = ResourceBundleConstants.BUNDLE_LOCATION;
    public static final String COMMON_MESSAGES = BUNDLE_LOCATION + "common";
    public static final String DEFAULT_LOCALE = "en_US";
    public static final String UI_LOCALE_PARAM_NAME = "uilocale";

    // workflow states
    public static final String DISPATCHED = "DISPATCHED";
    public static final String PENDING = "PENDING";
    public static final String EXPORTED = "EXPORTED";
    public static final String READY = "READY_TO_BE_DISPATCHED";
    public static final String ARCHIVED = "ARCHIVED";
    public static final String LOCALIZED = "LOCALIZED";
    public static final String DEFAULT_USERNAME = "gsAdmin";

    // protected member data
    protected ResourceBundle commonBundle = null;
    protected Locale theUiLocale = null;
    protected String theUsername = null;
    protected HttpSession theSession = null;
    protected SessionManager theSessionMgr = null;

    // private member data
    private static boolean s_jobCostingIsOn = false;
    private static boolean s_jobRevenueIsOn = false;
    private HashMap<String, Currency> currency_map = null;
    protected String reportKey = "BasicReport";

    private boolean useInContext = false;

    // for pagination
    public static final int PAGE_CONTENT_HEIGTH_PX = 700;

    static
    {
        findIfJobCostingisOn();
        findIfJobRevenueisOn();
    }

    /**
     * Creates the common parameters including the UI Locale <br>
     */
    public void init()
    {
        // Need to get these information from session.
        if (theUiLocale == null)
        {
            theUiLocale = Locale.US;
        }

        theUsername = DEFAULT_USERNAME;
        loadCommonBundle();
    }

    public void invokeHandler(HttpServletRequest req, HttpServletResponse res,
            ServletContext p_context) throws Exception
    {
        theSession = req.getSession(false);
        if (theSession == null)
        {
            ReportsPackage.logError("Cannot get the session for current user");
            String sendTo = WebAppConstants.PROTOCOL_HTTP + "://"
                    + req.getServerName() + ":" + WebAppConstants.HTTP_PORT
                    + "/globalsight/ControlServlet";
            res.sendRedirect(sendTo);
        }
        else
        {
            Locale locale = (Locale) req.getSession().getAttribute(
                    WebAppConstants.UILOCALE);
            if (locale == null)
            {
                locale = Locale.getDefault();
            }

            if (locale != null && !locale.equals(theUiLocale))
            {
                theUiLocale = locale;
                init();
            }
        }
    }

    public void dispatcherForward(String jspURL, HttpServletRequest p_request,
            HttpServletResponse p_response, ServletContext p_context)
            throws ServletException, IOException
    {
        s_category.debug("Will dispatch to URL: " + jspURL);
        RequestDispatcher dispatcher = p_context.getRequestDispatcher(jspURL);
        dispatcher.forward(p_request, p_response);
    }

    /**
     * Returns a DB connection without throwing any exceptions. Errors are
     * logged out using the ReportsPackage.logError()
     */
    protected void returnConnection(Connection p_connection)
    {
        try
        {
            ConnectionPool.returnConnection(p_connection);
        }
        catch (Exception cpe)
        {
            ReportsPackage.logError(cpe);
        }
    }

    /**
     * Closes the statement without throwing any exceptions. Errors are logged
     * out using the ReportsPackage.logError() <br>
     */
    protected void closeStatement(Statement p_statement)
    {
        try
        {
            if (p_statement != null)
            {
                p_statement.close();
            }
        }
        catch (Exception ex)
        {
            ReportsPackage.logError(ex);
        }
    }

    protected void loadCommonBundle()
    {
        commonBundle = ResourceBundle.getBundle(COMMON_MESSAGES, theUiLocale);
    }

    /**
     * Initializes all the world's currencies for the display currency, with the
     * pivot currency as the default.
     */
    protected void addCurrencyParameter(ResourceBundle p_bundle,
            HttpServletRequest req) throws Exception
    {
        Collection<?> currencies = ServerProxy.getCostingEngine()
                .getCurrencies();
        Currency pivotCurrency = ServerProxy.getCostingEngine()
                .getPivotCurrency();

        ArrayList<String> labeledCurrencies = new ArrayList<String>();
        ArrayList<String> valueCurrencies = new ArrayList<String>();
        Iterator<?> iter = currencies.iterator();
        currency_map = new HashMap<String, Currency>();

        while (iter.hasNext())
        {
            Currency c = (Currency) iter.next();
            currency_map.put(c.getDisplayName(), c);
            if (!valueCurrencies.contains(c.getDisplayName()))
            {
                valueCurrencies.add(c.getDisplayName());
            }
            if (c.equals(pivotCurrency))
            {
                s_category.debug("Will set PIVOT_CURRENCY_PM_DEFVALUE: "
                        + c.getDisplayName());
                req.setAttribute(Constants.PIVOT_CURRENCY_PM_DEFVALUE,
                        c.getDisplayName());
            }
        }

        SortUtil.sort(valueCurrencies);

        for (String value : valueCurrencies)
        {
            Currency c = currency_map.get(value);
            labeledCurrencies.add(c.getDisplayName(theUiLocale));
        }

        req.setAttribute(Constants.CURRENCY_ARRAY, valueCurrencies);
        req.setAttribute(Constants.CURRENCY_ARRAY_LABEL, labeledCurrencies);
        req.setAttribute(Constants.CURRENCY_DISPLAYNAME_LABEL,
                ReportsPackage.getMessage(p_bundle, Constants.CURRENCY));
    }

    protected Currency getCurrencyByDisplayname(String displayName)
    {
        if (currency_map != null)
        {
            return (Currency) currency_map.get(displayName);
        }
        else
        {
            return null;
        }
    }

    protected void setCommonMessages(BaseDataWrap baseDataWrap)
    {
        baseDataWrap.setTxtFooter(commonBundle
                .getString(Constants.REPORT_TXT_FOOT));
        baseDataWrap.setTxtPageNumber(commonBundle
                .getString(Constants.REPORT_TXT_PAGENUM));
        SimpleDateFormat sdf = new SimpleDateFormat(
                Constants.REPORT_TXT_DATEFORMAT);
        baseDataWrap.setTxtDate(sdf.format(new Date()));
    }

    /**
     * Sets s_jobCostingIsOn based on whether job costing is enabled <br>
     */
    private static void findIfJobCostingisOn()
    {
        s_jobCostingIsOn = false;
        try
        {
            SystemConfiguration sc = SystemConfiguration.getInstance();
            s_jobCostingIsOn = sc
                    .getBooleanParameter(SystemConfigParamNames.COSTING_ENABLED);
        }
        catch (Throwable e)
        {
            ReportsPackage.logError(
                    "Problem getting costing parameter from database ", e);
        }
    }

    /**
     * Sets s_jobRevenueIsOn based on whether job revenue is enabled <br>
     */
    private static void findIfJobRevenueisOn()
    {
        s_jobRevenueIsOn = false;
        try
        {
            SystemConfiguration sc = SystemConfiguration.getInstance();
            s_jobRevenueIsOn = sc
                    .getBooleanParameter(SystemConfigParamNames.REVENUE_ENABLED);
        }
        catch (Throwable e)
        {
            ReportsPackage.logError(
                    "Problem getting costing parameter from database ", e);
        }
    }

    /**
     * Returns true if job costing is on <br>
     * 
     * @return true|false
     */
    public static boolean isJobCostingOn()
    {
        return s_jobCostingIsOn;
    }

    /**
     * Returns true if job revenue is on <br>
     * false otherwise
     * 
     */
    public static boolean isJobRevenueOn()
    {
        return s_jobRevenueIsOn;
    }

    /**
     * Calculates the job cost. Logs out errors. <br>
     * 
     * @param p_job
     *            -- the Job
     * @param p_currency
     *            -- the chosen currency
     * @return Cost
     */
    public static Cost calculateJobCost(Job p_job, Currency p_currency,
            int p_costType)
    {
        Cost cost = null;
        try
        {
            cost = ServerProxy.getCostingEngine().calculateCost(p_job,
                    p_currency, false, p_costType);
        }
        catch (Exception e)
        {
            ReportsPackage.logError(e);
        }

        return cost;
    }

    /**
     * Returns the cost of this workflow using the current currency. <br>
     * 
     * @param p_workflow
     *            -- the workflow
     * @param p_currency
     *            -- the Currency
     * @param p_costType
     *            -- the cost type
     * @return Cost
     */
    public static Cost calculateWorkflowCost(Workflow p_workflow,
            Currency p_currency, int p_costType)
    {
        Cost cost = null;
        try
        {
            cost = ServerProxy.getCostingEngine().calculateCost(p_workflow,
                    p_currency, false, p_costType);
        }
        catch (Exception e)
        {
            ReportsPackage.logError("Problem getting workflow cost", e);
        }

        return cost;
    }

    /**
     * Returns the cost of this task using the current currency. <br>
     * 
     * @param p_task
     *            -- the task
     * @param p_currency
     *            -- the Currency
     * @param p_costType
     *            -- the cost type
     * @return Cost How do I make this class work? Is it needed?
     */
    public static Cost calculateTaskCost(Task p_task, Currency p_currency,
            int p_costType)
    {
        Cost cost = null;
        try
        {
            cost = ServerProxy.getCostingEngine().calculateCost(p_task,
                    p_currency, false, p_costType);
        }
        catch (Exception e)
        {
            ReportsPackage.logError("Problem getting activity cost", e);
        }

        return cost;
    }

    /**
     * Given a list of data, this method sorts it and creates a sublist to be
     * displayed in a jsp.
     * 
     * @param data
     *            --- Data for the table
     * @param comp
     *            --- Comparator for sorting table data
     * @param numItemsDisplayed
     *            --- Number of displayed items per page
     * @param listname
     *            --- A name for the list to be used in the jsp (via useBean)
     * @param key
     *            --- A unique id which is used to pass hidden data between here
     *            and jsp so the jsp knows which column is sorted, etc.
     * @param baseDataWrap
     */
    public void setTableNavigation(HttpServletRequest request,
            HttpSession session, int numItemsDisplayed, String listname,
            String key, BaseDataWrap baseDataWrap) throws EnvoyServletException
    {
        setTableNavigation(request, session, numItemsDisplayed, key
                + TableConstants.NUM_PER_PAGE_STR, key
                + TableConstants.NUM_PAGES, listname, key
                + TableConstants.PAGE_NUM, key + TableConstants.LAST_PAGE_NUM,
                key + TableConstants.LIST_SIZE, baseDataWrap);
    }

    /*
     * Set request and session information needed in the UI for displaying the
     * navigation of tables. ie: Displaying 1-10 of 15
     */
    public void setTableNavigation(HttpServletRequest request,
            HttpSession session, int numItemsDisplayed, String numPerPageStr,
            String numOfPagesStr, String listStr, String thisPageNumStr,
            String lastPageNumStr, String sizeStr, BaseDataWrap baseDataWrap)
            throws EnvoyServletException
    {
        List<?> data = baseDataWrap.getDataList();
        Integer lastPageNumber = (Integer) getSessionAttribute(session,
                lastPageNumStr);
        String pageStr = (String) request.getParameter(thisPageNumStr);

        int pageNum = 1;
        if (pageStr != null)
        {
            if (pageStr.matches("[0-9]+"))
            {
                try
                {
                    pageNum = Integer.parseInt(pageStr);
                }
                catch (Exception ex)
                {
                    pageNum = baseDataWrap.getCurrentPageNum();
                }
            }
            else
            {
                pageNum = -1;
            }

            if (pageNum < 1 || pageNum > baseDataWrap.getTotalPageNum())
            {
                pageNum = baseDataWrap.getCurrentPageNum();
            }
        }
        else if (lastPageNumber != null)
        {
            pageNum = lastPageNumber.intValue();
        }

        Integer currentPageNumber = new Integer(pageNum);
        if (lastPageNumber == null)
        {
            lastPageNumber = currentPageNumber;
            setSessionAttribute(session, lastPageNumStr, lastPageNumber);
        }

        List<?> subList = null;
        int size = 0;
        if (data != null)
        {
            size = data.size();
            if (size > numItemsDisplayed)
            {
                int start = getStartIndex(pageNum, size, numItemsDisplayed);
                int end = getEndingIndex(pageNum, size, numItemsDisplayed);
                subList = data.subList(start, end);
            }
            else
            {
                subList = data;
            }
        }

        if (subList == null)
        {
            request.setAttribute(listStr, new ArrayList<Object>());
        }
        else
        {
            request.setAttribute(listStr, new ArrayList<Object>(subList));
        }

        request.setAttribute(thisPageNumStr, new Integer(pageNum));
        int numOfPages = getNumOfPages(size, numItemsDisplayed);
        baseDataWrap.setTotalPageNum(numOfPages);

        request.setAttribute(numOfPagesStr, new Integer(numOfPages));
        request.setAttribute(numPerPageStr, new Integer(numItemsDisplayed));
        request.setAttribute(sizeStr, new Integer(size));

        // remember the and pageNumber
        int current = currentPageNumber.intValue();
        if (current > numOfPages && numOfPages != 0)
        {
            setSessionAttribute(session, lastPageNumStr, new Integer(
                    current - 1));
            baseDataWrap.setCurrentPageNum(current - 1);
        }
        else
        {
            setSessionAttribute(session, lastPageNumStr, currentPageNumber);
        }

        baseDataWrap.setCurrentPageNum(current);
    }

    public void cleanSession(HttpSession session)
    {
        if (session != null)
        {
            SessionManager sessionManager = (SessionManager) session
                    .getAttribute(WebAppConstants.SESSION_MANAGER);
            sessionManager.removeElementOfReportMap(reportKey);
            s_category.debug("Will clean the " + reportKey + " in session");
        }
    }

    @SuppressWarnings("unchecked")
    public void setSessionAttribute(HttpSession session, String key, Object obj)
    {
        if (session != null)
        {
            SessionManager sessionManager = (SessionManager) session
                    .getAttribute(WebAppConstants.SESSION_MANAGER);
            Object map = sessionManager.getReportAttribute(reportKey);

            if (map != null)
            {
                s_category.debug("Will set " + key + " in session under "
                        + reportKey);
                ((HashMap<String, Object>) map).put(key, obj);
            }
            else
            {
                s_category
                        .debug("Will set " + key
                                + " in a new hashmap in the session under "
                                + reportKey);
                HashMap<String, Object> sessionMap = new HashMap<String, Object>();
                sessionMap.put(key, obj);
                sessionManager.setReportAttribute(reportKey, sessionMap);
            }
        }

    }

    public Object getSessionAttribute(HttpSession session, String key)
    {
        if (session != null)
        {
            SessionManager sessionManager = (SessionManager) session
                    .getAttribute(WebAppConstants.SESSION_MANAGER);
            Object map = sessionManager.getReportAttribute(reportKey);
            if (map != null)
            {
                return ((HashMap<?, ?>) map).get(key);
            }
            else
            {
                ReportsPackage.logError("Cannot get " + key + " from "
                        + reportKey + " map in session");
            }
        }
        return null;
    }

    // get total number of pages that can be displayed.
    private int getNumOfPages(int numOfItems, int perPage)
    {
        // List of templates
        int pageNum = (numOfItems + perPage - 1) / perPage;
        return pageNum == 0 ? 1 : pageNum;
    }

    // get the start index of the collection (this is inclusive)
    private int getStartIndex(int pageNum, int collectionSize, int perPage)
    {
        int startIndex = (pageNum - 1) * perPage;
        startIndex = (startIndex < collectionSize) ? startIndex
                : collectionSize;

        startIndex = (startIndex > 0) ? startIndex : 0;

        return startIndex;
    }

    // get the ending index for this page (this is exclusive).
    private int getEndingIndex(int pageNum, int collectionSize, int perPage)
    {
        int endIndex = pageNum * perPage;
        endIndex = (endIndex < collectionSize) ? endIndex : collectionSize;

        endIndex = (endIndex > 0) ? endIndex : 0;
        return endIndex;
    }

    protected HashMap<Long, Object[]> findCompletedActivities(Job job)
            throws Exception
    {
        HashMap<Long, Object[]> map = new HashMap<Long, Object[]>();

        JbpmContext ctx = null;
        try
        {
            ctx = WorkflowConfiguration.getInstance().getJbpmContext();
            for (Workflow wf : job.getWorkflows())
            {
                long instanceId = wf.getId();
                ProcessInstance instance = ctx.getProcessInstance(instanceId);
                Collection<?> taskCollection = instance.getTaskMgmtInstance()
                        .getTaskInstances();
                for (Iterator<?> taskIt = taskCollection.iterator(); taskIt
                        .hasNext();)
                {
                    TaskInstance taskInstance = (TaskInstance) taskIt.next();
                    Long taskId = new Long(taskInstance.getTask().getTaskNode()
                            .getId());
                    String activityName = taskInstance.getName();
                    String userResponsible = UserUtil
                            .getUserNameById(taskInstance.getActorId());
                    Object[] values = new Object[]
                    { taskId, activityName, userResponsible };
                    map.put(taskId, values);
                }
            }
        }
        finally
        {
            ctx.close();
        }

        return map;
    }

    protected HashMap<?, ?> findCompletedActivities(long p_jobid)
            throws Exception
    {
        Job job = ServerProxy.getJobHandler().getJobById(p_jobid);
        return findCompletedActivities(job);
    }

    protected boolean isUseInContext()
    {
        return useInContext;
    }

    protected void setUseInContext(boolean isUseInContext)
    {
        useInContext = isUseInContext;
    }

    /**
     * This method judges if the In Context Match Information should display
     * according to the list iterator passed in.
     * 
     * @param iter
     */
    protected void setUseInContext(Iterator<?> iter)
    {
        while (iter.hasNext())
        {
            Object o = iter.next();
            Job job = null;
            if (o instanceof Job)
            {
                job = (Job) iter.next();
            }
            else if (o instanceof Workflow)
            {
                Workflow workflow = (Workflow) iter.next();
                job = workflow.getJob();
            }
            else
            {
                throw new IllegalArgumentException("The argument is not right");
            }

            if (PageHandler.isInContextMatch(job))
            {
                useInContext = true;
            }
        }
    }

    /**
     * This method judges if the In Context Match Information should display
     * according to the jobs passed in.
     * 
     * @param jobs
     */
    protected void setUseInContext(ArrayList<Job> jobs)
    {
        for (Job job : jobs)
        {
            if (PageHandler.isInContextMatch(job))
            {
                useInContext = true;
            }
        }
    }

    /**
     * This method judges if the In Context Match Information should display
     * according to the job passed in.
     * 
     * @param job
     */
    protected void setUseInContext(Job job)
    {
    	useInContext = false;
        if (PageHandler.isInContextMatch(job))
        {
            useInContext = true;
        }
    }
}
