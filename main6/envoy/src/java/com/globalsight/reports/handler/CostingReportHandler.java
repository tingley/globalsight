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

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Vector;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import com.globalsight.everest.costing.AmountOfWork;
import com.globalsight.everest.costing.Cost;
import com.globalsight.everest.costing.Currency;
import com.globalsight.everest.costing.CurrencyFormat;
import com.globalsight.everest.costing.FlatSurcharge;
import com.globalsight.everest.costing.Money;
import com.globalsight.everest.costing.PercentageSurcharge;
import com.globalsight.everest.costing.Rate;
import com.globalsight.everest.costing.Surcharge;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.page.SourcePage;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.taskmanager.Task;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.administration.users.UserUtil;
import com.globalsight.everest.webapp.pagehandler.projects.workflows.JobManagementHandler;
import com.globalsight.everest.webapp.tags.TableConstants;
import com.globalsight.everest.workflow.WfTaskInfo;
import com.globalsight.everest.workflow.WorkflowConstants;
import com.globalsight.everest.workflow.WorkflowInstance;
import com.globalsight.everest.workflow.WorkflowTaskInstance;
import com.globalsight.everest.workflowmanager.Workflow;
import com.globalsight.reports.Constants;
import com.globalsight.reports.JobTableModel2;
import com.globalsight.reports.WorkflowTableModel2;
import com.globalsight.reports.datawrap.CostingReportDataWrap;
import com.globalsight.reports.util.ReportHandlerFactory;
import com.globalsight.reports.util.ReportsPackage;
import com.globalsight.util.SortUtil;
import com.globalsight.util.date.DateHelper;

/**
 * Presents a report on workflow status
 */
public class CostingReportHandler extends BasicReportHandler
{
    private static final String MY_MESSAGES = BUNDLE_LOCATION + "costing";

    private static final String JOBS_STATUS_MSG = "jobstatus";

    private static final String JOB_NAME_MSG = "jobname";

    private static final String CURRENCY_MSG = "currency";

    private static final String LOCAL_PROFILE_MSG = "locProfile";

    private static final String ALL_WORK_FLOWS_MSG = "allWorkflows";

    private static final String HIDDEN_ACTIVITY_ID = "hiddenActivityId";

    private static final String ACTIVITY_NAME_MSG = "activityName";

    private static final String ACCEPTER_MSG = "accepter";

    private static final String DURATION_MSG = "duration";

    private static final String COMPLETE_DATE_MSG = "compDate";

    private static final String ACTIVITY_BREAKDOWN_MSG = "activityBreakdown";

    private static final String NOT_ACCEPTED_YET_MSG = "notAcceptedYet";

    private static final String LB_ABBREVIATION_DAY_MSG = "lb_abbreviation_day";

    private static final String LB_ABBREVIATION_HOUR_MSG = "lb_abbreviation_hour";

    private static final String LB_ABBREVIATION_MINUTE_MSG = "lb_abbreviation_minute";

    private static final String PAGES_IN_JOB_MSG = "pagesInJob";

    private static final String COST_MSG = "cost";

    private static final String ACTUAL_COST_MSG = "actualCost";

    private static final String ACTUAL_REVENUE_MSG = "actualRevenue";

    private static final String HOURS_MSG = "hours";

    private static final String ACTUAL_HOURS_MSG = "actualHours";

    private static final String OVERRIDE_EXPENSES_COST_NOTE = "overrideExpensesCostNote";

    private static final String OVERRIDE_REVENUE_COST_NOTE = "overrideRevenueCostNote";

    private static final String EXPENSES_SURCHARGE_COST_NOTE = "expensesSurchargeCostNote";

    private static final String REVENUE_SURCHARGE_COST_NOTE = "revenueSurchargeCostNote";

    private static final String NO_EXPENSES_SURCHARGE_COST_NOTE = "noExpensesSurchargeCostNote";

    private static final String NO_REVENUE_SURCHARGE_COST_NOTE = "noRevenueSurchargeCostNote";

    private static final String EMPTY_STRING = "";

    private static final String FLAT_SURCHARGE = "FlatSurcharge";

    private static final String FLAT_SURCHARGE_MSG = "flatSurcharge";

    private static final String PERCENTAGE_SURCHARGE_MSG = "percentageSurcharge";

    private static final String EXPENSES_SURCHARGES_IN_JOB = "expensesSurchargesInJob";

    private static final String REVENUE_SURCHARGES_IN_JOB = "revenueSurchargesInJob";

    private static final String BREAKDOWN_ON_NEXT_PAGE = "breakdownOnNextPage";

    private static final String NO_DATE_OR_TIME = "--";

    private static final String LEFT_PARENTHESES = " (";

    private static final String RIGHT_PARENTHESES = "%)";

    private static final String PURE_NUM_REGEX = "[0-9]+";

    // values for accessing completed activity information

    private static final int CA_TASK_USER = 2;

    // Add JobID parameter for single report
    // where job.id=jobId needs to be inserted

    private static final String LABEL_SUFFIX = ": ";

    protected ResourceBundle m_bundle = null;

    private Currency m_currency = null;

    private Long m_jobId = null;

    private CostingReportDataWrap reportDataWrap = null;

    private static final int ACTIVITY_TABLE_ROW_HIGH_PX = 72;

    private static final int ACTIVITY_TABLE_LABEL_HIGH_PX = 30;

    private static final int ACTIVITY_TABLE_TITLE_HIGH_PX = 20;

    private static final int ACTIVITY_TABLE_LINE_HIGH_PX = 35;

    private static final int PAGENAME_TABLE_ROW_HIGH_PX = 16;

    private static final int PAGENAME_TABLE_LABEL_HIGH_PX = 20;

    private int pageRestHighPx = -1;

    /**
     * Initializes the report and sets all the required parameters
     */
    public void init()
    {
        try
        {
            super.init(); // get the common parameters
            m_bundle = ResourceBundle.getBundle(MY_MESSAGES, theUiLocale);
            this.reportKey = Constants.COSTING_REPORT_KEY;
        }
        catch (Exception e)
        {
            ReportsPackage.logError(e);
        }
    }

    public void invokeHandler(HttpServletRequest req, HttpServletResponse res,
            ServletContext p_context) throws Exception
    {
        super.invokeHandler(req, res, p_context);

        String act = (String) req.getParameter(Constants.REPORT_ACT);
        s_category
                .debug("Perfoem CostingReportHandler.invokeHandler with action "
                        + act);

        if (Constants.REPORT_ACT_CREATE.equalsIgnoreCase(act))
        {
            theSessionMgr = (SessionManager) theSession
                    .getAttribute(WebAppConstants.SESSION_MANAGER);
            if (theSessionMgr == null)
            {
                ReportsPackage
                        .logError("Cannot get theSessionMgr for current user.");
                return;
            }
            m_jobId = (Long) this.theSessionMgr
                    .getAttribute(JobManagementHandler.JOB_ID);

            // the web page is first created .
            cleanSession(theSession);
            createReport(req);
        }
        else if (Constants.REPORT_ACT_TURNPAGE.equalsIgnoreCase(act))
        {
            // the web page was turn to other page .
            reportDataWrap = (CostingReportDataWrap) getSessionAttribute(
                    theSession, reportKey + Constants.REPORT_DATA_WRAP);
            // do pagenation function .
            String pageStr = (String) req.getParameter(reportKey
                    + TableConstants.PAGE_NUM);
            int pageNum = 1;
            if (pageStr != null)
            {
                if (pageStr.matches(PURE_NUM_REGEX))
                    pageNum = Integer.parseInt(pageStr);
                else
                {
                    pageNum = -1;
                }
                if (pageNum < 1 || pageNum > reportDataWrap.getTotalPageNum())
                {
                    pageNum = reportDataWrap.getCurrentPageNum();
                }
            }
            reportDataWrap.setCurrentPageNum(pageNum);
        }

        dispatcherForward(
                ReportHandlerFactory.getTargetUrl(reportKey
                        + Constants.REPORT_ACT_CREATE), req, res, p_context);
    }

    /**
     * Creates the actual report and fills it with data and messages. Also
     * determines the grouping styles. <br>
     * 
     * @param HttpServletRequest
     */
    public void createReport(HttpServletRequest req)
    {
        super.setUseInContext(false);

        try
        {
            reportDataWrap = new CostingReportDataWrap();
            bindMessages();
            bindData(req);
            reportDataWrap.setCurrentPageNum(Constants.FIRST_PAGE_NUM);
            int totalPageNum = ((List) reportDataWrap.getDataList()).size();
            if (totalPageNum < 1)
            {
                ReportsPackage
                        .logError("Wrong : totalPageNum is less then '1'!\r\n TotalPageNum is set to '1' now.");
                totalPageNum = 1;
            }

            reportDataWrap.setTotalPageNum(totalPageNum);
            setSessionAttribute(theSession, reportKey
                    + Constants.REPORT_DATA_WRAP, reportDataWrap);
        }
        catch (Exception e)
        {
            ReportsPackage.logError(e);
        }
    }

    public void bindMessages()
    {
        reportDataWrap.setReportTitle(ReportsPackage.getMessage(m_bundle,
                Constants.REPORT_TITLE));
        setCommonMessages(reportDataWrap);
    }

    /**
     * Gets the data from the DB and binds it to tables <br>
     * 
     * @param HttpServletRequest
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    public void bindData(HttpServletRequest req) throws Exception
    {
        LinkedHashMap tempLinkedMap = null;
        if (isJobCostingOn())
        {
            String currencyName = (String) theSessionMgr
                    .getAttribute(JobManagementHandler.CURRENCY);
            m_currency = (Currency) ServerProxy.getCostingEngine().getCurrency(
                    currencyName);
        }
        else
        {
            m_currency = null;
        }

        Long jobId = m_jobId;
        Job job = ServerProxy.getJobHandler().getJobById(jobId.longValue());

        super.setUseInContext(job);

        Collection c = ServerProxy.getJobReportingManager()
                .getWorkflowsByJobId(job.getId());
        List workflows = new ArrayList(c);
        WorkflowTableModel2 wtm = new WorkflowTableModel2(workflows,
                theSession, m_currency, false);
        wtm.setUseInContext(super.isUseInContext());
        wtm.fillAllData(null, workflows);

        ArrayList jobs = new ArrayList();
        jobs.add(job);
        JobTableModel2 jtm = new JobTableModel2(jobs, theUiLocale, m_currency);
        // add job form data in this function .
        addJobForm(jtm, 0, job);

        addTotalCostNote(job, Cost.EXPENSE);
        if (isJobRevenueOn())
        {
            addTotalCostNote(job, Cost.REVENUE);
        }
        // add Page Break .that is , the other contents will be put in the next
        // page .
        // 'FIRST_PAGE = 1' is the currentPageNum .
        tempLinkedMap = (LinkedHashMap) reportDataWrap
                .gainCurrentPageData(Constants.FIRST_PAGE_NUM);
        String note = reportDataWrap.getTotalCostNote();
        tempLinkedMap.put(Constants.CONTENT_TYPE_NOTE, note);
        // set '(LinkedHashMap)tempLinkedMap' to null when the first page data
        // is completed.
        tempLinkedMap = null;
        this.pageRestHighPx = -1;

        // following contents belongs to the other web page after one .
        addTableWithSurcharges(job, Cost.EXPENSE);
        if (isJobRevenueOn())
        {
            addTableWithSurcharges(job, Cost.REVENUE);
        }

        addWorkflowSummaryTable(wtm);

        // add DetailedWorkflowBreakdownTables
        addDetailedWorkflowBreakdownTables(jobId.longValue(), wtm, workflows);

        addTableWithPageNames(job);
    }

    /**
     * Adds a form to the report containing the job information <br>
     * 
     * @param p_table
     *            -- a table model
     * @param r
     *            -- the current row
     */
    @SuppressWarnings("unchecked")
    private void addJobForm(TableModel p_table, int r, Job p_job)
    {
        List labels = new ArrayList();
        List fields = new ArrayList();
        String origLabel = null;
        String field = null;
        String label = null;

        int maxLabelLen = 0;

        labels.add(ReportsPackage.getMessage(m_bundle, JOB_NAME_MSG)
                + LABEL_SUFFIX);
        labels.add(ReportsPackage.getMessage(m_bundle, JOBS_STATUS_MSG)
                + LABEL_SUFFIX);
        fields.add(p_job.getJobName());
        fields.add(ReportsPackage.getMessage(m_bundle, p_job.getState()));
        if (isJobCostingOn())
        {
            labels.add(ReportsPackage.getMessage(m_bundle, CURRENCY_MSG)
                    + LABEL_SUFFIX);
            fields.add(m_currency.getDisplayName());
        }
        // start with 1 to skip jobid and then skip jobname in the loop
        for (int c = 1; c < p_table.getColumnCount(); c++)
        {
            if (c == JobTableModel2.JOBNAME || c == JobTableModel2.JOB_OBJECT)
            {
                // don't want jobname since it's in the criteria
                // don't want job object
                continue;
            }

            if (isJobCostingOn() && isCostingRelated(c))
            {
                // this should handle estimated , actual , and total costs and
                // billing.
                double cost = ((Float) p_table.getValueAt(r, c)).doubleValue();
                field = CurrencyFormat.getCurrencyFormat(m_currency).format(
                        cost);
            }
            else
            {
                field = p_table.getValueAt(r, c).toString();
            }

            origLabel = (String) p_table.getColumnName(c);
            label = origLabel + LABEL_SUFFIX;
            if (!labels.contains(label))
            {
                labels.add(label);
                fields.add(field);
            }
        }

        // insert l10nprofile near data source profile
        label = ReportsPackage.getMessage(m_bundle, LOCAL_PROFILE_MSG)
                + LABEL_SUFFIX;
        if (labels.contains(label))
        {
            int n = labels.indexOf(label);
            labels.remove(n);
            fields.remove(n);
        }

        maxLabelLen = (label.length() > maxLabelLen) ? label.length()
                : maxLabelLen;
        labels.add(5, label);
        fields.add(5, p_job.getL10nProfile().getName());

        // figure out the max label length
        for (int j = 0; j < labels.size(); j++)
        {
            String l = (String) labels.get(j);
            maxLabelLen = (l.length() > maxLabelLen) ? l.length() : maxLabelLen;
        }

        // add the "Label - Value pairs" to
        // (CostingReportDataWrap)reportDataWrap
        // NOTE : here multi thread is not safe !
        LinkedHashMap jobFormLinkedMap = new LinkedHashMap();
        jobFormLinkedMap.put(Constants.CONTENT_TYPE_LABEL, labels);
        jobFormLinkedMap.put(Constants.CONTENT_TYPE_FIELD, fields);
        this.reportDataWrap.addPageData(jobFormLinkedMap);
    }

    /**
     * Returns true if the given column is costing related
     */
    private boolean isCostingRelated(int c)
    {
        if (c == JobTableModel2.COST || c == JobTableModel2.ACTUALCOST
                || c == JobTableModel2.TOTALCOST
                || c == JobTableModel2.ACTUAL_REVENUE
                || c == JobTableModel2.TOTAL_REVENUE)
        {
            return true;
        }

        return false;
    }

    /**
     * Adds a note whether the final cost is an override or actual with/without
     * surcharges. Also introduces the surcharge table
     */
    private void addTotalCostNote(Job p_job, int p_costType) throws Exception
    {
        Cost finalCost = BasicReportHandler.calculateJobCost(p_job, m_currency,
                p_costType);
        String label;
        if (finalCost.isOverriden())
        {
            if (p_costType == Cost.EXPENSE)
            {
                label = ReportsPackage.getMessage(m_bundle,
                        OVERRIDE_EXPENSES_COST_NOTE);
            }
            else
            {
                label = ReportsPackage.getMessage(m_bundle,
                        OVERRIDE_REVENUE_COST_NOTE);
            }
        }
        else
        {
            if (finalCost.getSurcharges().size() > 0)
            {
                if (p_costType == Cost.EXPENSE)
                {
                    label = ReportsPackage.getMessage(m_bundle,
                            EXPENSES_SURCHARGE_COST_NOTE);
                }
                else
                {
                    label = ReportsPackage.getMessage(m_bundle,
                            REVENUE_SURCHARGE_COST_NOTE);
                }
            }
            else
            {
                if (p_costType == Cost.EXPENSE)
                {
                    label = ReportsPackage.getMessage(m_bundle,
                            NO_EXPENSES_SURCHARGE_COST_NOTE);
                }
                else
                {
                    label = ReportsPackage.getMessage(m_bundle,
                            NO_REVENUE_SURCHARGE_COST_NOTE);
                }
            }
        }
        // add the total cost note to 'reportDataWrap'
        this.reportDataWrap.setTotalCostNote(label);
    }

    /**
     * Prints out a two column table containing the surcharge names and their
     * values. Use the JSP as a model for the code. <br>
     * 
     * @param p_job
     *            -- the Job
     * @param p_costType
     *            - the cost type
     */
    @SuppressWarnings(
    { "unchecked", "serial" })
    private void addTableWithSurcharges(Job p_job, int p_costType)
    {
        Cost cost = BasicReportHandler.calculateJobCost(p_job, m_currency,
                p_costType);
        Collection c = cost.getSurcharges();
        final int numRows = c.size();
        if (numRows > 0)
        {
            Iterator iter = c.iterator();
            TableModel tm = new AbstractTableModel()
            {

                Object[][] data = new Object[numRows][3];

                String[] columnNames =
                { ReportsPackage.getMessage(m_bundle, "surchargeName"),
                        ReportsPackage.getMessage(m_bundle, "surchargeType"),
                        ReportsPackage.getMessage(m_bundle, "surchargeAmount") };

                public int getRowCount()
                {
                    return data.length;
                }

                public int getColumnCount()
                {
                    return 3;
                }

                public String getColumnName(int i)
                {
                    return columnNames[i];
                }

                public Object getValueAt(int r, int i)
                {
                    return data[r][i];
                }

                public void setValueAt(Object o, int r, int i)
                {
                    data[r][i] = o;
                }
            };
            int row = 0;
            // Added by Vincent Yan 2009/07/20
            float tempTotalSurcharge = 0;
            while (iter.hasNext())
            {
                Surcharge surcharge = (Surcharge) iter.next();
                if (surcharge.getType().equals(FLAT_SURCHARGE))
                {
                    FlatSurcharge flatSurcharge = (FlatSurcharge) surcharge;
                    tempTotalSurcharge += flatSurcharge.getAmount().getAmount();
                }
            }
            iter = c.iterator();
            // End of 2009/07/20

            while (iter.hasNext())
            {
                Surcharge surcharge = (Surcharge) iter.next();
                String amount = EMPTY_STRING;
                String type = EMPTY_STRING;

                if (surcharge.getType().equals(FLAT_SURCHARGE))
                {
                    FlatSurcharge flatSurcharge = (FlatSurcharge) surcharge;
                    float surchargeAmount = flatSurcharge.getAmount()
                            .getAmount();
                    amount = CurrencyFormat.getCurrencyFormat(m_currency)
                            .format(surchargeAmount);
                    type = ReportsPackage.getMessage(m_bundle,
                            FLAT_SURCHARGE_MSG);
                }
                else
                {
                    // PercentageSurcharge
                    PercentageSurcharge percentageSurcharge = (PercentageSurcharge) surcharge;
                    float percentage = Money.roundOff(percentageSurcharge
                            .getPercentage() * 100);
                    // float percentageAmount = percentageSurcharge
                    // .surchargeAmount(cost.getActualCost()).getAmount();
                    float percentageAmount = percentageSurcharge
                            .surchargeAmount(
                                    new Money(cost.getActualCost().getAmount()
                                            + tempTotalSurcharge)).getAmount();
                    amount = CurrencyFormat.getCurrencyFormat(m_currency)
                            .format(percentageAmount);
                    type = ReportsPackage.getMessage(m_bundle,
                            PERCENTAGE_SURCHARGE_MSG)
                            + LEFT_PARENTHESES
                            + percentage + RIGHT_PARENTHESES;
                }

                // add in the rate in parens near the type
                tm.setValueAt(surcharge.getName(), row, 0);
                tm.setValueAt(type, row, 1);
                tm.setValueAt(amount, row, 2);
                row++;
            }
            // add data and relating label
            // ss.addNewline(1);
            // textId
            // tm;
            LinkedHashMap tmpLinkedMap = gainLinkMapOfPage(tm,
                    Constants.SURCHARGES_TABLE);
            String textId = EMPTY_STRING;
            if (p_costType == Cost.EXPENSE)
            {
                // "EXPENSE" just for discrimenate the same kind of 'label' or
                // 'table'
                textId = ReportsPackage.getMessage(m_bundle,
                        EXPENSES_SURCHARGES_IN_JOB);
                tmpLinkedMap.put(Constants.CONTENT_TYPE_LABEL + "EXPENSE",
                        textId);
                tmpLinkedMap.put(Constants.SURCHARGES_TABLE + "EXPENSE", tm);
            }
            else
            {
                // "REVENUE" just for discrimenate the same kind of 'label' or
                // 'table'
                textId = ReportsPackage.getMessage(m_bundle,
                        REVENUE_SURCHARGES_IN_JOB);
                tmpLinkedMap.put(Constants.CONTENT_TYPE_LABEL + "REVENUE",
                        textId);
                tmpLinkedMap.put(Constants.SURCHARGES_TABLE + "REVENUE", tm);
            }
        }
    }

    /**
     * Adds the given WorkflowTableModel to the report <br>
     * 
     * @param WorkflowTableModel2
     *            p_wtm
     */
    @SuppressWarnings("unchecked")
    private void addWorkflowSummaryTable(WorkflowTableModel2 p_wtm)
    {
        List<Integer> subcolsList = new ArrayList<Integer>();

        subcolsList.add(WorkflowTableModel2.TRGLOCALE);
        subcolsList.add(WorkflowTableModel2.WFSTATE);
        subcolsList.add(WorkflowTableModel2.SEGMENT_TM_WC);
        subcolsList.add(WorkflowTableModel2.FUZZY_HI_WC);
        subcolsList.add(WorkflowTableModel2.FUZZY_MED_HI_WC);
        subcolsList.add(WorkflowTableModel2.FUZZY_MED_WC);
        subcolsList.add(WorkflowTableModel2.FUZZY_LOW_WC);
        subcolsList.add(WorkflowTableModel2.NO_MATCH);
        subcolsList.add(WorkflowTableModel2.REP_WC);
        if (super.isUseInContext())
        {
            subcolsList.add(WorkflowTableModel2.IN_CONTEXT_WC);
        }

        // subcols[i++] = WorkflowTableModel2.SUBLEVREPS;
        // subcols[i++] = WorkflowTableModel2.SUBLEVMATCHES;
        subcolsList.add(WorkflowTableModel2.PER_COMPLETE);

        if (isJobCostingOn())
        {
            subcolsList.add(WorkflowTableModel2.COST);
            subcolsList.add(WorkflowTableModel2.ACTUALCOST);
            if (isJobRevenueOn())
            {
                // subcols[i++] = WorkflowTableModel2.REVENUE;
                subcolsList.add(WorkflowTableModel2.ACTUAL_REVENUE);
            }
            NumberFormat currencyFormat = CurrencyFormat
                    .getCurrencyFormat(m_currency);

            // now run through the table and replace the cost with a formatted
            // cost
            for (int r = 0; r < p_wtm.getRowCount(); r++)
            {
                double cost = ((Float) p_wtm.getValue(r,
                        WorkflowTableModel2.COST)).doubleValue();
                double actualCost = ((Float) p_wtm.getValue(r,
                        WorkflowTableModel2.ACTUALCOST)).doubleValue();
                String formattedExpensesCurrency = currencyFormat.format(cost);
                String formattedExpensesActualCurrency = currencyFormat
                        .format(actualCost);
                if (isJobRevenueOn())
                {
                    // double revenue =
                    // ((Float)p_wtm.getValueAt(r,WorkflowTableModel2.REVENUE)).doubleValue();
                    double actualRevenue = ((Float) p_wtm.getValue(r,
                            WorkflowTableModel2.ACTUAL_REVENUE)).doubleValue();
                    // String formattedRevenueCurrency =
                    // currencyFormat.format(revenue);
                    String formattedRevenueActualCurrency = currencyFormat
                            .format(actualRevenue);
                    // p_wtm.setValue(formattedRevenueCurrency,r,WorkflowTableModel2.REVENUE);
                    p_wtm.setValue(formattedRevenueActualCurrency, r,
                            WorkflowTableModel2.ACTUAL_REVENUE);
                }
                p_wtm.setValue(formattedExpensesCurrency, r,
                        WorkflowTableModel2.COST);
                p_wtm.setValue(formattedExpensesActualCurrency, r,
                        WorkflowTableModel2.ACTUALCOST);
            }
        }
        // add data and relating label
        String textId = ReportsPackage.getMessage(m_bundle, ALL_WORK_FLOWS_MSG);
        LinkedHashMap tempLinkedMap = gainLinkMapOfPage(p_wtm,
                Constants.ACTIVITY_TABLE);
        tempLinkedMap.put(Constants.CONTENT_TYPE_LABEL
                + Constants.CONTENT_TYPE_TABLE_MODEL, textId);
        tempLinkedMap.put(Constants.CONTENT_TYPE_INTETER_ARRAY,
                convertIntArray(subcolsList));
        tempLinkedMap.put(Constants.CONTENT_TYPE_TABLE_MODEL, p_wtm);
        // add "textNextPage"
        String textNextPage = ReportsPackage.getMessage(m_bundle,
                BREAKDOWN_ON_NEXT_PAGE);
        tempLinkedMap.put(Constants.CONTENT_TYPE_NOTE, textNextPage);
        tempLinkedMap = null;
        // to make sure the next page is a new page .
        this.pageRestHighPx = -1;
    }

    private int[] convertIntArray(List<Integer> p_list)
    {
        int[] result = new int[p_list.size()];
        for (int i = 0; i < p_list.size(); i++)
        {
            result[i] = p_list.get(i);
        }
        return result;
    }

    /**
     * Outputs a table containing the activities for each workflow <br>
     * 
     * @param ArrayList
     *            p_workflows -- the workflows for this job
     */
    @SuppressWarnings("unchecked")
    private void addDetailedWorkflowBreakdownTables(long p_jobId,
            WorkflowTableModel2 p_wtm, List p_workflows) throws Exception
    {
        Object[] allColumnNames = new Object[]
        { HIDDEN_ACTIVITY_ID, // 0
                ReportsPackage.getMessage(m_bundle, ACTIVITY_NAME_MSG), // 1
                ReportsPackage.getMessage(m_bundle, ACCEPTER_MSG), // 2
                // ReportsPackage.getMessage(m_bundle,ASSIGNEE_ROLE_MSG), //3
                ReportsPackage.getMessage(m_bundle, DURATION_MSG), // 4
                ReportsPackage.getMessage(m_bundle, COMPLETE_DATE_MSG), // 5
                ReportsPackage.getMessage(m_bundle, COST_MSG), // 6
                ReportsPackage.getMessage(m_bundle, ACTUAL_COST_MSG), // 7
                // ReportsPackage.getMessage(m_bundle,REVENUE_MSG), //8
                ReportsPackage.getMessage(m_bundle, ACTUAL_REVENUE_MSG), // 9
                ReportsPackage.getMessage(m_bundle, HOURS_MSG), // 10
                ReportsPackage.getMessage(m_bundle, ACTUAL_HOURS_MSG) // 11
        };

        Object[] noRevenueColumnNames = new Object[]
        { HIDDEN_ACTIVITY_ID, // 0
                ReportsPackage.getMessage(m_bundle, ACTIVITY_NAME_MSG), // 1
                ReportsPackage.getMessage(m_bundle, ACCEPTER_MSG), // 2
                // ReportsPackage.getMessage(m_bundle,ASSIGNEE_ROLE_MSG), //3
                ReportsPackage.getMessage(m_bundle, DURATION_MSG), // 4
                ReportsPackage.getMessage(m_bundle, COMPLETE_DATE_MSG), // 5
                ReportsPackage.getMessage(m_bundle, COST_MSG), // 6
                ReportsPackage.getMessage(m_bundle, ACTUAL_COST_MSG), // 7
                ReportsPackage.getMessage(m_bundle, HOURS_MSG), // 8
                ReportsPackage.getMessage(m_bundle, ACTUAL_HOURS_MSG) // 9
        };

        Map<String, String> lineInGreenMap = new HashMap<String, String>();

        // add the 'Activity Tabel' label to current page .
        String textId = ReportsPackage.getMessage(m_bundle,
                ACTIVITY_BREAKDOWN_MSG);
        // to create a new page .
        this.pageRestHighPx = PAGE_CONTENT_HEIGTH_PX;
        Map<String, Object> tmpLinkMap = new LinkedHashMap<String, Object>();
        tmpLinkMap.put(Constants.CONTENT_TYPE_LABEL + Constants.ACTIVITY_TABLE,
                textId);
        this.pageRestHighPx = this.pageRestHighPx
                - ACTIVITY_TABLE_LABEL_HIGH_PX;
        reportDataWrap.addPageData(tmpLinkMap);
        // set 'tmpLinkMap' to null after the current data is dealed .
        tmpLinkMap = null;

        // query out all the completed activities for all the workflows for this
        // job
        HashMap completedActivities = findCompletedActivities(p_jobId);
        for (int i = 0; i < p_workflows.size(); i++)
        {
            Workflow w = (Workflow) p_workflows.get(i);
            WorkflowInstance wfi = ServerProxy.getWorkflowServer()
                    .getWorkflowInstanceById(w.getId());
            Vector wfiTasks = wfi.getWorkflowInstanceTasks();
            Hashtable tasks = w.getTasks();
            boolean isCurrentActivity = false;
            DefaultTableModel table = null;
            if (isJobRevenueOn())
            {
                table = new DefaultTableModel(tasks.size(),
                        allColumnNames.length);
                table.setColumnIdentifiers(allColumnNames);
            }
            else
            {
                table = new DefaultTableModel(tasks.size(),
                        noRevenueColumnNames.length);
                table.setColumnIdentifiers(noRevenueColumnNames);
            }

            // keep separate lists of completed and uncompleted tasks
            List completedTasks = new ArrayList();
            List uncompletedTasks = new ArrayList();
            List defaultTaskIds = findDefaultTaskIds(w.getId());
            int jcol = -1;
            for (int j = 0; j < wfiTasks.size(); j++)
            {
                WorkflowTaskInstance wfTask = (WorkflowTaskInstance) wfiTasks
                        .get(j);
                // only process real user activities, not start nodes, etc.
                if (wfTask.getType() != WorkflowConstants.ACTIVITY)
                    continue;
                else
                    jcol++;

                Long taskId = new Long(wfTask.getTaskId());
                Task task = (Task) tasks.get(taskId);

                String accepter = ReportsPackage.getMessage(m_bundle,
                        NOT_ACCEPTED_YET_MSG);

                if (wfTask.getTaskState() == Task.STATE_ACTIVE)
                {
                    isCurrentActivity = true;
                    Map activeTasks = ServerProxy.getWorkflowServer()
                            .getActiveTasksForWorkflow(w.getId());
                    WorkflowTaskInstance thisTask = (WorkflowTaskInstance) activeTasks
                            .get(taskId);

                    if (thisTask != null)
                        accepter = UserUtil.getUserNameById(thisTask
                                .getAcceptUser());

                    if (accepter == null || accepter.length() == 0)
                    {
                        accepter = ReportsPackage.getMessage(m_bundle,
                                NOT_ACCEPTED_YET_MSG);
                    }

                }
                else
                {
                    isCurrentActivity = false;
                    // see if there is a completed activity for this task
                    if (completedActivities.containsKey(taskId))
                    {
                        Object[] values = (Object[]) completedActivities
                                .get(taskId);
                        accepter = (String) values[CA_TASK_USER];
                    }
                }

                // completed date
                String completedDate;
                boolean isCompleted = false;
                if (task == null || task.getCompletedDate() == null)
                {
                    completedDate = NO_DATE_OR_TIME;
                }
                else
                {
                    completedDate = DateHelper.getFormattedDateAndTime(
                            task.getCompletedDate(), theUiLocale);
                    isCompleted = true;
                }

                // cost related values
                String estimatedCost = NO_DATE_OR_TIME;
                String actualCost = NO_DATE_OR_TIME;
                // String estimatedRevenue = NO_DATE_OR_TIME;
                String actualRevenue = NO_DATE_OR_TIME;
                String estimatedHours = NO_DATE_OR_TIME;
                String actualHours = NO_DATE_OR_TIME;
                if (task != null)
                {
                    // get expense details
                    Cost cost = BasicReportHandler.calculateTaskCost(task,
                            m_currency, Cost.EXPENSE);
                    if (cost.getEstimatedCost() != null)
                        estimatedCost = Float.toString(cost.getEstimatedCost()
                                .getAmount());
                    if (cost.getActualCost() != null)
                        actualCost = Float.toString(cost.getActualCost()
                                .getAmount());

                    if (isJobRevenueOn())
                    {
                        // get revenue details
                        Cost revenue = BasicReportHandler.calculateTaskCost(
                                task, m_currency, Cost.REVENUE);
                        if (revenue.getEstimatedCost() != null)
                            // estimatedRevenue = Float.toString(revenue
                            // .getEstimatedCost().getAmount());
                            if (revenue.getActualCost() != null)
                                actualRevenue = Float.toString(revenue
                                        .getActualCost().getAmount());
                    }

                    AmountOfWork aow = task
                            .getAmountOfWork(Rate.UnitOfWork.HOURLY);
                    // Estimated Hours
                    if (aow != null)
                    {
                        estimatedHours = Double.toString(aow
                                .getEstimatedAmount());
                        actualHours = Double.toString(aow.getActualAmount());
                    }
                }

                // duration
                long timeInDays = wfTask.getCompletedTime();
                String duration;
                if (timeInDays > 0)
                {
                    duration = DateHelper.daysHoursMinutes(timeInDays,
                            ReportsPackage.getMessage(m_bundle,
                                    LB_ABBREVIATION_DAY_MSG), ReportsPackage
                                    .getMessage(m_bundle,
                                            LB_ABBREVIATION_HOUR_MSG),
                            ReportsPackage.getMessage(m_bundle,
                                    LB_ABBREVIATION_MINUTE_MSG));
                }
                else
                    duration = NO_DATE_OR_TIME;

                List<Object> columns = new ArrayList<Object>();
                columns.add(taskId);
                columns.add(wfTask.getActivityDisplayName());
                columns.add(accepter);
                columns.add(duration);
                columns.add(completedDate);
                columns.add(estimatedCost);
                columns.add(actualCost);

                if (isJobRevenueOn())
                {
                    // columns.add(estimatedRevenue);
                    columns.add(actualRevenue);
                }
                columns.add(estimatedHours);
                columns.add(actualHours);

                if (isCompleted)
                {
                    // make sure the last value is the completed date
                    columns.add(task.getCompletedDate());
                    completedTasks.add(columns);
                }
                else
                {
                    columns.add(new Boolean(isCurrentActivity));
                    uncompletedTasks.add(columns);
                }
            }

            // sort the completed tasks by Date
            SortUtil.sort(completedTasks, new MyCompletedTaskComparator());
            // sort the uncompleted tasks (current one goes first)
            SortUtil.sort(uncompletedTasks, new MyUnCompletedTaskComparator(
                    defaultTaskIds));

            // fill the table with the completed tasks first (date order)
            int idx = 0;
            for (int k = 0; k < completedTasks.size(); k++)
            {
                ArrayList a = (ArrayList) completedTasks.get(idx++);
                for (int n = 0; n < a.size() - 1; n++)
                {
                    // put all the values into the table except for the actual
                    // completed date
                    table.setValueAt(a.get(n), k, n);
                }
            }

            // now add all the uncompleted tasks (tasks in default task first)
            idx = 0;
            int currentRow = -1;
            for (int k = completedTasks.size(); k < completedTasks.size()
                    + uncompletedTasks.size(); k++)
            {
                ArrayList a = (ArrayList) uncompletedTasks.get(idx++);
                for (int n = 0; n < a.size() - 1; n++)
                {
                    // put all the values into the table except for the actual
                    // completed date
                    table.setValueAt(a.get(n), k, n);
                }

                Boolean b_isCurrentActivity = (Boolean) a.get(a.size() - 1);
                if (b_isCurrentActivity.booleanValue() == true)
                {
                    currentRow = k;
                }
            }

            // now set the current row as green
            if (currentRow > -1)
            {
                // Set the current line to the reportDataWrap to show the line
                // in green
                lineInGreenMap.put(Constants.ACTIVITY_TABLE + i,
                        String.valueOf(currentRow));
            }

            // deal with data !
            String localeName = w.getTargetLocale().getDisplayName(theUiLocale);
            tmpLinkMap = gainLinkMapOfPage(table, Constants.ACTIVITY_TABLE);
            // add 'i' in linkedhashmap key to distinguish multi table which is
            // the same category .
            tmpLinkMap.put(Constants.CONTENT_TYPE_TITLE + i, localeName);
            tmpLinkMap.put(Constants.ACTIVITY_TABLE + i, table);
            tmpLinkMap = null;
        }
        reportDataWrap.setLineInGreenMap(lineInGreenMap);
    }

    private LinkedHashMap gainLinkMapOfPage(TableModel curTableModel,
            String tableCategory)
    {
        // '1' is the head row of this table model .
        int rows = curTableModel.getRowCount() + 1;
        int allPageNum = -1;
        int tableHigh = 0;
        if (tableCategory.equalsIgnoreCase(Constants.ACTIVITY_TABLE))
        {
            tableHigh += ACTIVITY_TABLE_TITLE_HIGH_PX;
            tableHigh += ACTIVITY_TABLE_ROW_HIGH_PX * rows;
            tableHigh += ACTIVITY_TABLE_LINE_HIGH_PX;
        }
        else if (tableCategory.equalsIgnoreCase(Constants.PAGENAME_TABLE))
        {
            tableHigh += PAGENAME_TABLE_LABEL_HIGH_PX;
            tableHigh += PAGENAME_TABLE_ROW_HIGH_PX * rows;
        }

        if (!(this.pageRestHighPx > (PAGE_CONTENT_HEIGTH_PX - 50) || tableHigh < this.pageRestHighPx))
        {
            this.pageRestHighPx = PAGE_CONTENT_HEIGTH_PX;
            LinkedHashMap newPageLinkMap = new LinkedHashMap();
            reportDataWrap.addPageData(newPageLinkMap);
        }
        // gainCurrentPageData(currentPageNum)
        // 'currentPageNum' parameter is num of current page
        // not the arraylist index .
        this.pageRestHighPx -= tableHigh;
        allPageNum = reportDataWrap.getDataList().size();
        return reportDataWrap.gainCurrentPageData(allPageNum);
    }

    /**
     * Prints out a two column table containing the pagename and the source word
     * count. <br>
     * 
     * @param p_job
     *            -- the Job
     */
    @SuppressWarnings(
    { "serial", "unchecked" })
    private void addTableWithPageNames(Job p_job)
    {
        Collection c = p_job.getSourcePages();
        Iterator iter = c.iterator();
        final int numRows = c.size();
        TableModel tm = new AbstractTableModel()
        {
            Object[][] data = new Object[numRows][2];

            String[] columnNames =
            { ReportsPackage.getMessage(m_bundle, "pageName"),
                    ReportsPackage.getMessage(m_bundle, "srcWordCount") };

            public int getRowCount()
            {
                return data.length;
            }

            public int getColumnCount()
            {
                return 2;
            }

            public String getColumnName(int i)
            {
                return columnNames[i];
            }

            public Object getValueAt(int r, int i)
            {
                return data[r][i];
            }

            public void setValueAt(Object o, int r, int i)
            {
                data[r][i] = o;
            }
        };
        int row = 0;
        while (iter.hasNext())
        {
            SourcePage sourcePage = (SourcePage) iter.next();

            String pageName = getMainFileName(sourcePage.getExternalPageId());
            String subName = getSubFileName(sourcePage.getExternalPageId());

            if (subName != null)
            {
                pageName = pageName + " " + subName;
            }

            tm.setValueAt(pageName, row, 0);
            tm.setValueAt(new Integer(sourcePage.getWordCount()), row, 1);
            row++;
        }

        String textId = ReportsPackage.getMessage(m_bundle, PAGES_IN_JOB_MSG);
        LinkedHashMap tmpLinkMap = gainLinkMapOfPage(tm,
                Constants.PAGENAME_TABLE);
        tmpLinkMap.put(Constants.CONTENT_TYPE_LABEL + Constants.PAGENAME_TABLE,
                textId);
        tmpLinkMap.put(Constants.PAGENAME_TABLE, tm);
        tmpLinkMap = null;
    }

    /**
     * Returns a List of Longs for the IDs of tasks in the default path.
     * 
     * @param p_wfId
     *            -- the particular workflow
     * @return
     */
    private List findDefaultTaskIds(long p_wfId) throws Exception
    {
        List defaultTasks = ServerProxy.getWorkflowServer()
                .timeDurationsInDefaultPath(null, p_wfId, -1);

        List<Long> defaultTaskIds = new ArrayList<Long>();

        for (Iterator liter = defaultTasks.iterator(); liter.hasNext();)
        {
            WfTaskInfo wftaskinfo = (WfTaskInfo) liter.next();
            defaultTaskIds.add(new Long(wftaskinfo.getId()));
        }

        return defaultTaskIds;
    }

    // compares completed tasks by completed date
    class MyCompletedTaskComparator implements Comparator
    {
        public int compare(Object p_a, Object p_b)
        {
            ArrayList a = (ArrayList) p_a;
            ArrayList b = (ArrayList) p_b;

            // the completed date is the last item we put in the arraylist
            Date adate = (Date) a.get(a.size() - 1);
            Date bdate = (Date) b.get(a.size() - 1);
            return adate.compareTo(bdate);
        }
    }

    // compares uncompleted tasks by taskId, but active tasks go first
    class MyUnCompletedTaskComparator implements Comparator
    {
        private List m_defaultTaskIds = null;

        public MyUnCompletedTaskComparator(List p_defaultTaskIds)
        {
            m_defaultTaskIds = p_defaultTaskIds;
        }

        public int compare(Object p_a, Object p_b)
        {
            ArrayList a_cols = (ArrayList) p_a;
            ArrayList b_cols = (ArrayList) p_b;
            Long a_taskId = (Long) a_cols.get(0);
            Long b_taskId = (Long) b_cols.get(0);
            // now see if one of them is in the default path or not
            boolean a_isInDefault = m_defaultTaskIds.contains(a_taskId);
            boolean b_isInDefault = m_defaultTaskIds.contains(b_taskId);
            if (a_isInDefault && !b_isInDefault)
                return -1;
            else if (!a_isInDefault && b_isInDefault)
                return 1;
            else if (a_isInDefault && b_isInDefault)
            {
                // they're both in default path, so find whichever comes first
                int a_pos = m_defaultTaskIds.indexOf(a_taskId);
                int b_pos = m_defaultTaskIds.indexOf(b_taskId);
                return a_pos - b_pos;
            }
            else
            {
                // neither is in default path, so do old comparison
                return a_taskId.compareTo(b_taskId);
            }
        }
    }

    private String getMainFileName(String p_filename)
    {
        int index = p_filename.indexOf(")");
        if (index > 0 && p_filename.startsWith("("))
        {
            index++;
            while (Character.isWhitespace(p_filename.charAt(index)))
            {
                index++;
            }

            return p_filename.substring(index, p_filename.length());
        }

        return p_filename;
    }

    private String getSubFileName(String p_filename)
    {
        int index = p_filename.indexOf(")");
        if (index > 0 && p_filename.startsWith("("))
        {
            return p_filename.substring(0, p_filename.indexOf(")") + 1);
        }

        return null;
    }

}
