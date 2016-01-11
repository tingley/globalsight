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
package com.globalsight.reports;

import inetsoft.report.ReportSheet;
import inetsoft.report.TableLens;
import inetsoft.report.TextElement;
import inetsoft.report.io.Builder;
import inetsoft.sree.BasicReplet;
import inetsoft.sree.RepletException;
import inetsoft.sree.RepletParameters;
import inetsoft.sree.RepletRequest;
import inetsoft.sree.SreeLog;

import java.awt.Color;
import java.awt.Font;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import com.globalsight.diplomat.util.database.ConnectionPool;
import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.costing.Cost;
import com.globalsight.everest.costing.Currency;
import com.globalsight.everest.foundation.UserImpl;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.taskmanager.Task;
import com.globalsight.everest.util.system.SystemConfigParamNames;
import com.globalsight.everest.util.system.SystemConfiguration;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.pagehandler.projects.workflows.JobManagementHandler;
import com.globalsight.everest.workflowmanager.Workflow;
import com.globalsight.reports.handler.BasicReportHandler;
import com.globalsight.reports.util.LabeledValueHolder;
import com.globalsight.reports.util.ReportsPackage;

public abstract class GlobalSightReplet extends BasicReplet
{

    protected static Logger c_category = Logger
            .getLogger(GlobalSightReplet.class.getName());

    public static final String[] SUPPORTED_UI_LOCALES = new String[]
    { "en_US", "fr_FR", "es_ES", "de_DE", "ja_JP" };
    public static final String DEFAULT_LOCALE = "en_US";
    public static final String UI_LOCALE_PARAM_NAME = "uilocale";
    public static final String COMMON_MESSAGES = BasicReportHandler.BUNDLE_LOCATION
            + "common";

    // colors and fonts
    public static final Color COLOR_TABLE_HDR_BG = new Color(0x0C1476);
    public static final String FONT_NAME = "Arial";
    public static final Font FONT_CHART_TITLE = new Font(FONT_NAME, Font.BOLD,
            12);
    public static final Font FONT_LABEL = new Font(FONT_NAME, Font.BOLD, 11);
    public static final Font FONT_NORMAL = new Font(FONT_NAME, Font.PLAIN, 11);
    public static final Font FONT_SMALL = new Font(FONT_NAME, Font.PLAIN, 10);
    public static final Font FONT_VERY_SMALL = new Font(FONT_NAME, Font.PLAIN,
            6);
    public static final Font FONT_ITALIC = new Font(FONT_NAME, Font.ITALIC, 11);

    // workflow states
    public static final String DISPATCHED = "DISPATCHED";
    public static final String PENDING = "PENDING";
    public static final String EXPORTED = "EXPORTED";
    public static final String READY = "READY";
    public static final String ARCHIVED = "ARCHIVED";
    public static final String LOCALIZED = "LOCALIZED";

    // protected member data
    protected ReportSheet theReport = null;
    protected RepletParameters theParameters = null;
    protected ResourceBundle commonBundle = null;
    protected Locale theUiLocale = null;
    protected String theUsername = null;
    protected HttpSession theSession = null;
    protected SessionManager theSessionMgr = null;

    // private member data
    private static boolean s_jobCostingIsOn = false;
    private static boolean s_jobRevenueIsOn = false;

    protected String companyId = null;

    private String[] headers = null;

    static
    {
        findIfJobCostingisOn();
        findIfJobRevenueisOn();
    }

    /**
     * Creates the common replet parameters including the UI Locale <br>
     * 
     * @param RepletRequest
     */
    public void init(RepletRequest req) throws RepletException
    {
        theSession = GlobalSightReplet.getUserSessionCache();
        if (theSession != null)
        {
            theUiLocale = (Locale) theSession
                    .getAttribute(WebAppConstants.UILOCALE);
            theUsername = (String) theSession
                    .getAttribute(WebAppConstants.USER_NAME);
            theSessionMgr = (SessionManager) theSession
                    .getAttribute(WebAppConstants.SESSION_MANAGER);

            // For "Cost reports crashing Amb06" issue
            companyId = CompanyThreadLocal.getInstance().getValue();
            if (companyId == null)
            {
                UserImpl companyName = (UserImpl) theSessionMgr
                        .getAttribute(JobManagementHandler.USER);
                companyId = CompanyWrapper.getCompanyIdByName(companyName
                        .getCompanyName());
                CompanyThreadLocal.getInstance().setIdValue(companyId);
            }

            if (c_category.isDebugEnabled())
            {
                c_category.debug("hashcode:" + this.hashCode());
                c_category.debug("companyId:" + companyId);
                c_category.debug("ClassName:" + this.getClass().getName());
                c_category.debug("ThreadName:" + Thread.currentThread().getName());                
            }
        }
        else
        {
            System.out.println("null****");
            theUiLocale = Locale.US;
            theUsername = "gsAdmin";
        }

        loadCommonBundle();
        theParameters = new RepletParameters(RepletRequest.CREATE);
    }

    public void addRepletParameters(RepletParameters p_params)
    {
        if (p_params.getParameterCount() > 0)
        {
            // localize the submit and close buttons on the replet parameter
            // screen
            // by replacing the English buttons with the right text
            StringBuffer html = new StringBuffer();
            html.append("\n<script language=\"JavaScript\">\n");
            html.append("var count = document.request.length;\n");
            html.append("for (var i = 0; i < count; i++) {\n");
            html.append("var formObj = document.request.elements[i];\n");
            html.append("if(formObj.type == 'button') {\n");
            html.append("if (formObj.value == \"Send Request\") {\n");
            html.append("formObj.value = \"");
            html.append(commonBundle.getString("submitForm"));
            html.append("\";\n");
            html.append("}\n");
            html.append("if (formObj.value== \"Cancel\") {\n");
            html.append("formObj.value = \"");
            html.append(commonBundle.getString("cancelForm"));
            html.append("\";\n");
            html.append("}\n");
            html.append("}\n");
            html.append("}\n");
            html.append("</script>\n");
            p_params.setRequestDialogHTML(html.toString());
        }
        super.addRepletParameters(p_params);
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
                p_statement = null;
            }

        }
        catch (Exception ex)
        {
            ReportsPackage.logError(ex);
        }
    }

    /**
     * Closes the ResultSet without throwing any exceptions. Errors are logged
     * out using the ReportsPackage.logError() <br>
     */
    protected void closeResultSet(ResultSet p_result)
    {
        try
        {
            if (p_result != null)
            {
                p_result.close();
                p_result = null;
            }
        }
        catch (Exception ex)
        {
            ReportsPackage.logError(ex);
        }
    }

    protected void readTemplate()
    {
        try
        {
            InputStream input = getClass().getResourceAsStream(
                    getTemplateName());
            Builder builder = Builder.getBuilder(Builder.TEMPLATE, input);
            theReport = (ReportSheet) builder.read(".");
            input.close();
        }
        catch (Exception e)
        {
            SreeLog.print("Could not read template: " + getTemplateName());
            SreeLog.print(e);
        }
    }

    protected void loadCommonBundle()
    {
        if (commonBundle == null)
            commonBundle = ResourceBundle.getBundle(COMMON_MESSAGES,
                    theUiLocale);
    }

    protected void setCommonMessages(RepletRequest req)
    {

        TextElement txtFooter = (TextElement) theReport.getElement("txtFooter");
        txtFooter.setText(commonBundle.getString("txtFooter"));

        // ReportSheet report = new StyleSheet();
        theReport.setPageNumberingStart(0); // page numbering from from second
                                            // page

        TextElement txtPageNumber = (TextElement) theReport
                .getElement("txtPageNumber");
        txtPageNumber.setText(commonBundle.getString("txtPageNumber"));
        // txtPageNumber.setText("{P}"+" of {N}" );

        // theReport.addHeaderText("Page {P} of {N}");

        TextElement txtDate = (TextElement) theReport.getElement("txtDate");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm z");
        txtDate.setText(sdf.format(new Date()));
    }

    /**
     * Adds all the world's currencies as a choice for the display currency,
     * with the pivot currency as the default.
     */
    protected void addCurrencyParameter(ResourceBundle p_bundle)
            throws Exception
    {
        Collection currencies = ServerProxy.getCostingEngine().getCurrencies();
        Currency pivotCurrency = ServerProxy.getCostingEngine()
                .getPivotCurrency();

        LabeledValueHolder labeledPivot = null;
        ArrayList labeledCurrencies = new ArrayList();
        Iterator iter = currencies.iterator();
        while (iter.hasNext())
        {
            Currency c = (Currency) iter.next();
            LabeledValueHolder lvh = new LabeledValueHolder(c,
                    c.getDisplayName());
            if (c.equals(pivotCurrency))
                labeledPivot = lvh;
            labeledCurrencies.add(lvh);
        }
        theParameters.addChoice("currency", labeledPivot,
                labeledCurrencies.toArray());
        theParameters.setAlias("currency",
                ReportsPackage.getMessage(p_bundle, "currency"));

        if (c_category.isDebugEnabled())
        {
            c_category.debug("currency:" + labeledCurrencies.toArray());
            c_category.debug("currency:"
                    + ReportsPackage.getMessage(p_bundle, "currency"));            
        }
    }

    /**
     * Fully loads a TableLens object because InetSoft 5.0 tables are only
     * filled on demand.
     * 
     * @param p_table
     *            tablelens
     */
    protected void fullyLoadTable(TableLens p_table)
    {
        int x = 0;
        boolean hasMoreRows = true;
        while (hasMoreRows)
        {
            hasMoreRows = p_table.moreRows(x);
            ++x;
        }
    }

    public abstract String getTemplateName();

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
     * 
     * @return true|false
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

    // temporary hack to allow the init method to get some values
    // because inetsoft doesn't pass in the HttpServletRequest to Replet.init()
    private static HttpSession userSessionCache = null;

    public static synchronized void setUserSessionCache(HttpSession p_session)
    {
        userSessionCache = p_session;
    }

    public static synchronized HttpSession getUserSessionCache()
    {
        return userSessionCache;
    }

    protected String[] getHeaders()
    {
        return headers;
    }

    protected void setHeaders(Iterator iter)
    {
        String[] headers = new String[1];
        while (iter.hasNext())
        {
            Object o = iter.next();
            Job job = null;
            if (o instanceof Job)
            {
                job = (Job) iter.next();
            }
            else
            {
                if (o instanceof Workflow)
                {
                    Workflow workflow = (Workflow) iter.next();
                    job = workflow.getJob();
                }
            }
            try
            {
                if (PageHandler.isInContextMatch(job))
                {
                    // hava tm profile contains in context match
                    headers[0] = "In Context Match";
                }
            }
            catch (Exception e)
            {
                ReportsPackage.logError(
                        "Problem getting value of in context match ", e);
            }
        }
        this.headers = headers;
    }

    protected void setHeaders(ArrayList jobs)
    {
        String[] headers = new String[1];
        for (int i = 0; i < jobs.size(); i++)
        {
            Job job = (Job) jobs.get(i);
            try
            {
                if (PageHandler.isInContextMatch(job))
                {
                    // hava tm profile contains in context match
                    headers[0] = "In Context Match";
                }
            }
            catch (Exception e)
            {
                ReportsPackage.logError(
                        "Problem getting value of in context match ", e);
            }
        }
        this.headers = headers;
    }

    protected void setHeaders(Job job)
    {
        String[] headers = new String[1];
        try
        {
            if (PageHandler.isInContextMatch(job))
            {
                // hava tm profile contains in context match
                headers[0] = "In Context Match";
            }
        }
        catch (Exception e)
        {
            ReportsPackage.logError(
                    "Problem getting value of in context match ", e);
        }
        this.headers = headers;
    }
}
