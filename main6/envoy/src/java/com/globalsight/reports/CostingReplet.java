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

import inetsoft.report.ReportElement;
import inetsoft.report.ReportSheet;
import inetsoft.report.StyleConstants;
import inetsoft.report.StyleSheet;
import inetsoft.report.TextElement;
import inetsoft.report.lens.RotatedTableLens;
import inetsoft.report.lens.SubTableLens;
import inetsoft.report.lens.swing.TableModelLens;
import inetsoft.report.style.Grid1;
import inetsoft.report.style.Professional;
import inetsoft.sree.RepletException;
import inetsoft.sree.RepletRequest;
import inetsoft.sree.SreeLog;

import java.awt.Color;
import java.awt.Font;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import com.globalsight.diplomat.util.database.ConnectionPool;
import com.globalsight.everest.company.CompanyThreadLocal;
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
import com.globalsight.everest.permission.Permission;
import com.globalsight.everest.permission.PermissionSet;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.taskmanager.Task;
import com.globalsight.everest.util.system.SystemConfigParamNames;
import com.globalsight.everest.util.system.SystemConfiguration;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.administration.users.UserUtil;
import com.globalsight.everest.webapp.pagehandler.projects.workflows.JobManagementHandler;
import com.globalsight.everest.workflow.WfTaskInfo;
import com.globalsight.everest.workflow.WorkflowConstants;
import com.globalsight.everest.workflow.WorkflowInstance;
import com.globalsight.everest.workflow.WorkflowTaskInstance;
import com.globalsight.everest.workflowmanager.Workflow;
import com.globalsight.reports.handler.BasicReportHandler;
import com.globalsight.reports.util.ReportsPackage;
import com.globalsight.util.SortUtil;
import com.globalsight.util.date.DateHelper;

/**
 * Presents a report on workflow status
 */
public class CostingReplet extends GlobalSightReplet
{
    private static final String MY_TEMPLATE = "/templates/basicFlowReport.srt";
    private static final String MY_MESSAGES = BasicReportHandler.BUNDLE_LOCATION
            + "costing";
    // For "Cost reports crashing Amb06" issue
    private static final String DEFAULT_SYMPOL = "--";
    // Add JobID parameter for single report
    // private Long jobId = theJobId;

    // where job.id=jobId needs to be inserted
    // private static final String JOB_QUERY =
    // "select job.id, job.name, job.state from job";

    private static final String LABEL_SUFFIX = ": ";
    protected ResourceBundle m_bundle = null;
    private StyleSheet ss = null;
    private Font m_labelFont = GlobalSightReplet.FONT_LABEL;
    // private Font m_fieldFont = GlobalSightReplet.FONT_NORMAL;

    private Currency m_currency = null;
    private Long m_jobId = null;
    private boolean isInternalCostReview;

    public CostingReplet()
    {
        readTemplate();
    }

    /**
     * Initializes the report and sets all the required parameters <br>
     * 
     * @param RepletRequest
     * @throws RepletException
     */
    public void init(RepletRequest req) throws RepletException
    {
        try
        {
            super.init(req); // get the common parameters
            m_bundle = ResourceBundle.getBundle(MY_MESSAGES, theUiLocale);
            m_jobId = (Long) this.theSessionMgr
                    .getAttribute(JobManagementHandler.JOB_ID);
            // added the permission. if internal cost is unchecked then hide the
            // information of internal cost
            PermissionSet userPerms = (PermissionSet) this.theSession
                    .getAttribute(WebAppConstants.PERMISSIONS);
            isInternalCostReview = userPerms
                    .getPermissionFor(Permission.COSTING_EXPENSE_VIEW);
        }
        catch (RepletException re)
        {
            ReportsPackage.logError(re);
            throw re;
        }
        catch (Exception e)
        {
            ReportsPackage.logError(e);
            throw new RepletException(e.getMessage());
        }
    }

    /**
     * Returns the Style Report Template name
     */
    public String getTemplateName()
    {
        return MY_TEMPLATE;
    }

    /**
     * Creates the actual report and fills it with data and messages. Also
     * determines the grouping and chart styles. <br>
     * 
     * @param RepletRequest
     * @return ReportSheet
     * @throws RepletException
     */
    public ReportSheet createReport(RepletRequest req) throws RepletException
    {
        // For "Cost reports crashing Amb06" issue
        CompanyThreadLocal.getInstance().setIdValue(companyId);
        HttpServletRequest http_request = (HttpServletRequest) req
                .getParameter(RepletRequest.HTTP_REQUEST);
        theSession = http_request.getSession(false);
        ss = (StyleSheet) theReport;
        try
        {
            bindMessages(req);
            bindData(req);
        }
        catch (Exception e)
        {
            SreeLog.print(e);
            throw new RepletException(e.getMessage());
        }
        return theReport;
    }

    /**
     * Fills out the messages on the report <br>
     * 
     * @param RepletRequest
     */
    private void bindMessages(RepletRequest req)
    {
        setCommonMessages(req);
        TextElement txtReportTitle = (TextElement) theReport
                .getElement("txtReportTitle");
        txtReportTitle.setText(ReportsPackage.getMessage(m_bundle,
                "txtReportTitle"));
    }

    /**
     * Gets the data from the DB and binds it to charts and tables <br>
     * 
     * @param RepletRequest
     * @throws Exception
     */
    private void bindData(RepletRequest req) throws Exception
    {
        if (isJobCostingOn())
        {
            String currencyName = (String) theSessionMgr
                    .getAttribute(JobManagementHandler.CURRENCY);
            m_currency = ServerProxy.getCostingEngine().getCurrency(
                    currencyName);
        }
        else
            m_currency = null;

        Long jobId = m_jobId;
        Job job = ServerProxy.getJobHandler().getJobById(jobId.longValue());

        Collection c = job.getWorkflows();
        ArrayList workflows = new ArrayList(c);
        WorkflowTableModel2 wtm = new WorkflowTableModel2(workflows,
                theSession, m_currency);

        super.setHeaders(job);
        if (super.getHeaders()[0] == null)
        {
            wtm.headers = super.getHeaders();
        }

        ArrayList jobs = new ArrayList();
        jobs.add(job);
        JobTableModel2 jtm = new JobTableModel2(jobs, theUiLocale, m_currency);
        ss.setCurrentAlignment(StyleConstants.H_LEFT);
        ss.setCurrentWrapping(ReportSheet.WRAP_TOP_BOTTOM);
        addJobForm(jtm);
        jtm = null; // Shorten the object's life cycle.
        addTotalCostNote(job, Cost.EXPENSE);
        if (isJobRevenueOn())
        {
            addTotalCostNote(job, Cost.REVENUE);
        }
        ss.addPageBreak();
        addTableWithSurcharges(job, Cost.EXPENSE);
        if (isJobRevenueOn())
        {
            addTableWithSurcharges(job, Cost.REVENUE);
        }
        ss.addSeparator(StyleConstants.THIN_LINE);
        ss.addNewline(2);

        addWorkflowSummaryTable(wtm);
        wtm = null; // Shorten the object's life cycle.
        ss.addNewline(2);
        ss.addSeparator(StyleConstants.THIN_LINE);
        String textId = ss.addText(ReportsPackage.getMessage(m_bundle,
                "breakdownOnNextPage"));
        ss.getElement(textId).setFont(GlobalSightReplet.FONT_LABEL);
        ss.getElement(textId).setForeground(Color.red);
        ss.addPageBreak();
        addDetailedWorkflowBreakdownTables(jobId.longValue(), workflows);
        addTableWithPageNames(job);

    }

    /**
     * Adds the given WorkflowTableModel to the report <br>
     * 
     * @param WorkflowTableModel2
     *            p_wtm
     */
    private void addWorkflowSummaryTable(WorkflowTableModel2 p_wtm)
    {
        int[] subcols;
        int i = 0;
        if (isJobCostingOn())
        {
            if (isInternalCostReview) // Internal Cost Review premission based
                // For job costing issue, the original is 17
                subcols = new int[17];
            else
                subcols = new int[15];
        }
        else
        {
            if (isInternalCostReview) // Internal Cost Review premission based
                subcols = new int[13];
            else
                subcols = new int[11];
        }

        subcols[i++] = WorkflowTableModel2.TRGLOCALE;
        subcols[i++] = WorkflowTableModel2.WFSTATE;
        subcols[i++] = WorkflowTableModel2.SEGMENT_TM_WC;
        subcols[i++] = WorkflowTableModel2.FUZZY_HI_WC;
        subcols[i++] = WorkflowTableModel2.FUZZY_MED_HI_WC;
        subcols[i++] = WorkflowTableModel2.FUZZY_MED_WC;
        subcols[i++] = WorkflowTableModel2.FUZZY_LOW_WC;
        subcols[i++] = WorkflowTableModel2.NO_MATCH;
        subcols[i++] = WorkflowTableModel2.REP_WC;
        subcols[i++] = WorkflowTableModel2.IN_CONTEXT_WC;
        subcols[i++] = WorkflowTableModel2.SUBLEVREPS;
        subcols[i++] = WorkflowTableModel2.SUBLEVMATCHES;
        subcols[i++] = WorkflowTableModel2.PER_COMPLETE;
        if (isJobCostingOn())
        {
            // Internal Cost Review permission based
            if (isInternalCostReview)
            {
                subcols[i++] = WorkflowTableModel2.COST;
                subcols[i++] = WorkflowTableModel2.ACTUALCOST;
            }
            if (isJobRevenueOn())
            {
                // subcols[i++] = WorkflowTableModel2.REVENUE;
                subcols[i++] = WorkflowTableModel2.ACTUAL_REVENUE;
            }
            if (super.getHeaders()[0] == null)
            {
                subcols = updateCols(subcols);
            }

            NumberFormat currencyFormat = CurrencyFormat
                    .getCurrencyFormat(m_currency);

            // now run through the table and replace the cost with a formatted
            // cost
            for (int r = 0; r < p_wtm.getRowCount(); r++)
            {
                // Internal Cost Review permission based
                if (!isInternalCostReview
                        && (r == WorkflowTableModel2.COST || r == WorkflowTableModel2.ACTUAL_REVENUE))
                {
                    continue;
                }
                if (isInternalCostReview)
                {
                    double cost = ((Float) p_wtm.getValueAt(r,
                            WorkflowTableModel2.COST)).doubleValue();
                    double actualCost = ((Float) p_wtm.getValueAt(r,
                            WorkflowTableModel2.ACTUALCOST)).doubleValue();
                    String formattedExpensesCurrency = currencyFormat
                            .format(cost);
                    String formattedExpensesActualCurrency = currencyFormat
                            .format(actualCost);
                    p_wtm.setValueAt(formattedExpensesCurrency, r,
                            WorkflowTableModel2.COST);
                    p_wtm.setValueAt(formattedExpensesActualCurrency, r,
                            WorkflowTableModel2.ACTUALCOST);
                }

                if (isJobRevenueOn())
                {
                    // double revenue =
                    // ((Float)p_wtm.getValueAt(r,WorkflowTableModel2.REVENUE)).doubleValue();
                    double actualRevenue = ((Float) p_wtm.getValueAt(r,
                            WorkflowTableModel2.ACTUAL_REVENUE)).doubleValue();
                    // String formattedRevenueCurrency =
                    // currencyFormat.format(revenue);
                    String formattedRevenueActualCurrency = currencyFormat
                            .format(actualRevenue);
                    // p_wtm.setValueAt(formattedRevenueCurrency,r,WorkflowTableModel2.REVENUE);
                    p_wtm.setValueAt(formattedRevenueActualCurrency, r,
                            WorkflowTableModel2.ACTUAL_REVENUE);
                }

            }
        }
        TableModelLens model = new TableModelLens(p_wtm);
        model.setColAutoSize(true);
        SubTableLens stl = new SubTableLens(model, null, subcols);
        Professional workflowtable = new Professional(stl);
        workflowtable.setFont(GlobalSightReplet.FONT_NORMAL);
        workflowtable.setRowBackground(0, GlobalSightReplet.COLOR_TABLE_HDR_BG);
        String textId = ss.addText(ReportsPackage.getMessage(m_bundle,
                "allWorkflows"));
        ss.getElement(textId).setFont(GlobalSightReplet.FONT_LABEL);
        ss.addNewline(1);
        ss.addTable(workflowtable);
    }

    private int[] updateCols(int[] subcols)
    {
        int[] cols = new int[subcols.length - 1];
        for (int i = 0; i < cols.length; i++)
        {
            if (i < 10)
            {
                cols[i] = subcols[i];
            }
            else
            {
                cols[i] = subcols[i + 1];
            }
        }

        return cols;
    }

    /**
     * Adds a form to the report containing the job information <br>
     * 
     * @param p_table
     *            -- a table model
     * @param r
     *            -- the current row
     */
    private void addJobForm(JobTableModel2 p_table)
    {
        ArrayList labels = new ArrayList();
        ArrayList fields = new ArrayList();
        String origLabel = null;
        String field = null;
        String label = null;

        int maxLabelLen = 0;
        ss.addSeparator(StyleConstants.THIN_LINE);
        /**
         * Discription for the "sequence" array. It is defined a sequence about
         * some data of what you want to show in the job form
         */
        int sequence[] = new int[]
        { JobTableModel2.JOBNAME, JobTableModel2.JOBSTATUS,
                JobTableModel2.CURRENCY, JobTableModel2.PRIORITY,
                JobTableModel2.PROJECT, JobTableModel2.LOCPROFILE,
                JobTableModel2.PM, JobTableModel2.SRCLOCALE,
                JobTableModel2.DATASRC, JobTableModel2.WORDCNT,
                JobTableModel2.CREATE_DATE, JobTableModel2.COST,
                JobTableModel2.ACTUALCOST, JobTableModel2.TOTALCOST,
                JobTableModel2.ACTUAL_REVENUE, JobTableModel2.TOTAL_REVENUE };
        // start to get the table name and failed data in the loop
        for (int c = 0; c < sequence.length; c++)
        {

            if (!isInternalCostReview
                    && (sequence[c] == JobTableModel2.COST
                            || sequence[c] == JobTableModel2.ACTUALCOST || sequence[c] == JobTableModel2.TOTALCOST))
            {
                // do not want internal cost if has not the privilege of
                // Internal Cost Review
                continue;
            }

            if (sequence[c] == JobTableModel2.CURRENCY && !isJobCostingOn())
            {
                continue;
            }
            if (isJobCostingOn() && isCostingRelated(sequence[c]))
            {
                // this should handle estimated, actual and total costs and
                // billing.
                double cost = ((Float) p_table.getValueAt(sequence[c]))
                        .doubleValue();
                NumberFormat numberFormat = CurrencyFormat
                        .getCurrencyFormat(m_currency);
                field = numberFormat.format(cost);
            }
            else
            {
                // field = p_table.getValueAt(r,c).toString();
                field = p_table.getValueAt(sequence[c]).toString();

                // get the job state display name for job form
                if (sequence[c] == JobTableModel2.JOBSTATUS)
                {
                    field = ReportsPackage.getMessage(m_bundle, field);
                }
            }
            if (sequence[c] != -1)
            {
                // if does not select in context match
                origLabel = (String) p_table.getColumnName(sequence[c]);
            }

            label = origLabel + LABEL_SUFFIX;
            // figure out the max label length into para "maxLabelLen"
            maxLabelLen = (label.length() > maxLabelLen) ? label.length()
                    : maxLabelLen;
            labels.add(label);
            fields.add(field);
        }

        Vector tableData = new Vector();
        tableData.add(new Vector(fields));
        DefaultTableModel dtm = new DefaultTableModel(tableData, new Vector(
                labels));
        TableModelLens tml = new TableModelLens(dtm);
        RotatedTableLens rt = new RotatedTableLens(tml);
        Grid1 t = new Grid1(rt);
        t.setColFont(0, m_labelFont);
        t.setColAlignment(0, StyleConstants.H_LEFT);
        t.setColAlignment(1, StyleConstants.H_LEFT);
        t.setColWidth(0, maxLabelLen + 20);
        t.setColBackground(0, Color.white);
        t.setColBorder(StyleConstants.NO_BORDER);
        t.setRowBorder(StyleConstants.NO_BORDER);
        ss.addTable(t);
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
            return true;
        return false;
    }

    /**
     * Prints out a two column table containing the pagename and the source word
     * count. <br>
     * 
     * @param p_job
     *            -- the Job
     */
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

        TableModelLens tml = new TableModelLens(tm);
        Professional prof = new Professional(tml);
        prof.setRowBackground(0, GlobalSightReplet.COLOR_TABLE_HDR_BG);
        prof.setFont(GlobalSightReplet.FONT_NORMAL);

        String textId = ss.addText(ReportsPackage.getMessage(m_bundle,
                "pagesInJob"));
        ss.getElement(textId).setFont(GlobalSightReplet.FONT_LABEL);
        ss.addNewline(1);
        ss.addTable(prof);
    }

    /**
     * Prints out a two column table containing the surcharge names and their
     * values. Use the JSP as a model for the code. <br>
     * 
     * @param p_job
     *            -- the Job
     * @param m_currency
     *            - the currency
     */
    private void addTableWithSurcharges(Job p_job, int p_costType)
    {
        Cost cost = GlobalSightReplet.calculateJobCost(p_job, m_currency,
                p_costType);
        Collection c = cost.getSurcharges();
        final int numRows = c.size();
        if (numRows > 0)
        {
            ss.addSeparator(StyleConstants.THIN_LINE);
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

            // For job costing issue
            String amount = "";
            String type = "";
            Iterator iter = null;
            Money totalFlatSurchage = new Money(0);

            // 1st, calculate the flat surchage
            iter = c.iterator();
            while (iter.hasNext())
            {
                Surcharge surcharge = (Surcharge) iter.next();
                if (surcharge.getType().equals("FlatSurcharge"))
                {
                    FlatSurcharge flatSurcharge = (FlatSurcharge) surcharge;
                    float surchargeAmount = flatSurcharge.getAmount()
                            .getAmount();
                    amount = CurrencyFormat.getCurrencyFormat(m_currency)
                            .format(surchargeAmount);
                    type = ReportsPackage.getMessage(m_bundle, "flatSurcharge");

                    // add in the rate in parens near the type
                    tm.setValueAt(surcharge.getName(), row, 0);
                    tm.setValueAt(type, row, 1);
                    tm.setValueAt(amount, row, 2);
                    row++;

                    totalFlatSurchage = totalFlatSurchage.add(flatSurcharge
                            .getAmount());
                }
            }

            // 2nd, calculate the percentage surchage base on totalcost
            iter = c.iterator();
            while (iter.hasNext())
            {
                Surcharge surcharge = (Surcharge) iter.next();
                if (surcharge.getType().equals("PercentageSurcharge"))
                {
                    PercentageSurcharge percentageSurcharge = (PercentageSurcharge) surcharge;
                    float percentage = Money.roundOff(percentageSurcharge
                            .getPercentage() * 100);
                    float percentageAmount = percentageSurcharge
                            .surchargeAmount(
                                    cost.getActualCost().add(totalFlatSurchage))
                            .getAmount();
                    amount = CurrencyFormat.getCurrencyFormat(m_currency)
                            .format(percentageAmount);
                    type = ReportsPackage.getMessage(m_bundle,
                            "percentageSurcharge") + " (" + percentage + "%)";

                    // add in the rate in parens near the type
                    tm.setValueAt(surcharge.getName(), row, 0);
                    tm.setValueAt(type, row, 1);
                    tm.setValueAt(amount, row, 2);
                    row++;
                }
            }

            TableModelLens tml = new TableModelLens(tm);
            Professional prof = new Professional(tml);
            prof.setRowBackground(0, GlobalSightReplet.COLOR_TABLE_HDR_BG);
            prof.setFont(GlobalSightReplet.FONT_NORMAL);

            String textId = "";
            if (p_costType == Cost.EXPENSE)
            {
                textId = ss.addText(ReportsPackage.getMessage(m_bundle,
                        "expensesSurchargesInJob"));
            }
            else
            {
                textId = ss.addText(ReportsPackage.getMessage(m_bundle,
                        "revenueSurchargesInJob"));
            }
            ss.getElement(textId).setFont(GlobalSightReplet.FONT_LABEL);
            ss.addNewline(1);
            ss.addTable(prof);
        }
    }

    /**
     * Adds a note whether the final cost is an override or actual with/without
     * surcharges. Also introduces the surcharge table
     */
    private void addTotalCostNote(Job p_job, int p_costType) throws Exception
    {
        Cost finalCost = GlobalSightReplet.calculateJobCost(p_job, m_currency,
                p_costType);
        String label;
        if (finalCost.isOverriden())
        {
            if (p_costType == Cost.EXPENSE)
            {
                label = ss.addText(ReportsPackage.getMessage(m_bundle,
                        "overrideExpensesCostNote"));
            }
            else
            {
                label = ss.addText(ReportsPackage.getMessage(m_bundle,
                        "overrideRevenueCostNote"));
            }
        }
        else
        {
            if (finalCost.getSurcharges().size() > 0)
            {
                if (p_costType == Cost.EXPENSE)
                {
                    label = ss.addText(ReportsPackage.getMessage(m_bundle,
                            "expensesSurchargeCostNote"));
                }
                else
                {
                    label = ss.addText(ReportsPackage.getMessage(m_bundle,
                            "revenueSurchargeCostNote"));
                }
            }
            else
            {
                if (p_costType == Cost.EXPENSE)
                {
                    label = ss.addText(ReportsPackage.getMessage(m_bundle,
                            "noExpensesSurchargeCostNote"));
                }
                else
                {
                    label = ss.addText(ReportsPackage.getMessage(m_bundle,
                            "noRevenueSurchargeCostNote"));
                }
            }
        }
        ss.getElement(label).setFont(GlobalSightReplet.FONT_LABEL);
        ss.getElement(label).setForeground(Color.red);
    }

    /**
     * Outputs a table containing the activities for each workflow <br>
     * 
     * @param ArrayList
     *            p_workflows -- the workflows for this job
     */

    private void addDetailedWorkflowBreakdownTables(long p_jobId,
            ArrayList p_workflows) throws Exception
    {
        ss.setCurrentFont(GlobalSightReplet.FONT_NORMAL);
        // For "Cost reports crashing Amb06" issue
        Object[] allColumnNames = null;
        Object[] noRevenueColumnNames = null;
        if (!isInternalCostReview)
        {
            allColumnNames = new Object[]
            { "hiddenActivityId", // 0
                    ReportsPackage.getMessage(m_bundle, "activityName"), // 1
                    ReportsPackage.getMessage(m_bundle, "accepter"), // 2
                    // ReportsPackage.getMessage(m_bundle,"assigneeRole"), //3
                    ReportsPackage.getMessage(m_bundle, "duration"), // 4
                    ReportsPackage.getMessage(m_bundle, "compDate"), // 5
                    // ReportsPackage.getMessage(m_bundle,"cost"), //6
                    // ReportsPackage.getMessage(m_bundle,"actualCost"), //7
                    // ReportsPackage.getMessage(m_bundle,"revenue"), //8
                    ReportsPackage.getMessage(m_bundle, "actualRevenue"), // 9
                    ReportsPackage.getMessage(m_bundle, "hours"), // 10
                    ReportsPackage.getMessage(m_bundle, "actualHours") // 11
            };

            noRevenueColumnNames = new Object[]
            { "hiddenActivityId", // 0
                    ReportsPackage.getMessage(m_bundle, "activityName"), // 1
                    ReportsPackage.getMessage(m_bundle, "accepter"), // 2
                    // ReportsPackage.getMessage(m_bundle,"assigneeRole"), //3
                    ReportsPackage.getMessage(m_bundle, "duration"), // 4
                    ReportsPackage.getMessage(m_bundle, "compDate"), // 5
                    // ReportsPackage.getMessage(m_bundle,"cost"), //6
                    // ReportsPackage.getMessage(m_bundle,"actualCost"), //7
                    ReportsPackage.getMessage(m_bundle, "hours"), // 8
                    ReportsPackage.getMessage(m_bundle, "actualHours") // 9
            };
        }
        else
        {
            allColumnNames = new Object[]
            { "hiddenActivityId", // 0
                    ReportsPackage.getMessage(m_bundle, "activityName"), // 1
                    ReportsPackage.getMessage(m_bundle, "accepter"), // 2
                    // ReportsPackage.getMessage(m_bundle,"assigneeRole"), //3
                    ReportsPackage.getMessage(m_bundle, "duration"), // 4
                    ReportsPackage.getMessage(m_bundle, "compDate"), // 5
                    ReportsPackage.getMessage(m_bundle, "cost"), // 6
                    ReportsPackage.getMessage(m_bundle, "actualCost"), // 7
                    // ReportsPackage.getMessage(m_bundle,"revenue"), //8
                    ReportsPackage.getMessage(m_bundle, "actualRevenue"), // 9
                    ReportsPackage.getMessage(m_bundle, "hours"), // 10
                    ReportsPackage.getMessage(m_bundle, "actualHours") // 11
            };

            noRevenueColumnNames = new Object[]
            { "hiddenActivityId", // 0
                    ReportsPackage.getMessage(m_bundle, "activityName"), // 1
                    ReportsPackage.getMessage(m_bundle, "accepter"), // 2
                    // ReportsPackage.getMessage(m_bundle,"assigneeRole"), //3
                    ReportsPackage.getMessage(m_bundle, "duration"), // 4
                    ReportsPackage.getMessage(m_bundle, "compDate"), // 5
                    ReportsPackage.getMessage(m_bundle, "cost"), // 6
                    ReportsPackage.getMessage(m_bundle, "actualCost"), // 7
                    ReportsPackage.getMessage(m_bundle, "hours"), // 8
                    ReportsPackage.getMessage(m_bundle, "actualHours") // 9
            };
        }

        String textId = ss.addText(ReportsPackage.getMessage(m_bundle,
                "activityBreakdown"));
        // set the label font after adding the table so it doesn't affect the
        // table font
        ss.getElement(textId).setFont(GlobalSightReplet.FONT_LABEL);
        ss.addNewline(2);

        // query out all the completed activities for all the workflows for this
        // job
        HashMap completedActivities = findCompletedActivities(p_jobId);

        int workflowCounts = p_workflows.size();
        for (int i = 0; i < workflowCounts; i++)
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
            ArrayList completedTasks = new ArrayList();
            ArrayList uncompletedTasks = new ArrayList();
            List defaultTaskIds = findDefaultTaskIds(w.getId()); // discover
                                                                 // this
                                                                 // statement is
                                                                 // too slow
                                                                 // when
                                                                 // debuging
            int taskCounts = wfiTasks.size();
            // below is generating the activity table for per workflow in the
            // loop
            for (int j = 0; j < taskCounts; j++)
            {
                WorkflowTaskInstance wfTask = (WorkflowTaskInstance) wfiTasks
                        .get(j);
                // only process real user activities, not start nodes, etc.
                if (wfTask.getType() != WorkflowConstants.ACTIVITY)
                {
                    continue;
                }
                Long taskId = new Long(wfTask.getTaskId());
                Task task = (Task) tasks.get(taskId);
                // only care the curent activity and completed activity
                // because the uncompleted activity has no accepter.
                String accepter = ReportsPackage.getMessage(m_bundle,
                        "notAcceptedYet");
                if (wfTask.getTaskState() == Task.STATE_ACTIVE)
                {
                    isCurrentActivity = true;

                    if (task != null)
                    {
                        accepter = UserUtil.getUserNameById(task.getAcceptor());
                    }

                    if (accepter == null || accepter.length() == 0)
                    {
                        accepter = ReportsPackage.getMessage(m_bundle,
                                "notAcceptedYet");
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
                // set completed date
                String completedDate;
                boolean isCompleted = false;
                if (task == null || task.getCompletedDate() == null)
                {
                    // For "Cost reports crashing Amb06" issue
                    completedDate = DEFAULT_SYMPOL;
                }
                else
                {
                    completedDate = DateHelper.getFormattedDateAndTime(
                            task.getCompletedDate(), theUiLocale);
                    isCompleted = true;
                }

                // cost related values
                // For "Cost reports crashing Amb06" issue
                String estimatedCost = DEFAULT_SYMPOL;
                String actualCost = DEFAULT_SYMPOL;
                String estimatedRevenue = DEFAULT_SYMPOL;
                String actualRevenue = DEFAULT_SYMPOL;
                String estimatedHours = DEFAULT_SYMPOL;
                String actualHours = DEFAULT_SYMPOL;
                if (task != null)
                {
                    // get expense details
                    Cost cost = GlobalSightReplet.calculateTaskCost(task,
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
                        Cost revenue = GlobalSightReplet.calculateTaskCost(
                                task, m_currency, Cost.REVENUE);
                        if (revenue.getEstimatedCost() != null)
                            estimatedRevenue = Float.toString(revenue
                                    .getEstimatedCost().getAmount());
                        if (revenue.getActualCost() != null)
                            actualRevenue = Float.toString(revenue
                                    .getActualCost().getAmount());
                    }

                    // Rate r = task.getExpenseRate();
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
                                    "lb_abbreviation_day"), ReportsPackage
                                    .getMessage(m_bundle,
                                            "lb_abbreviation_hour"),
                            ReportsPackage.getMessage(m_bundle,
                                    "lb_abbreviation_minute"));
                }
                else
                    // For "Cost reports crashing Amb06" issue
                    duration = DEFAULT_SYMPOL;

                ArrayList columns = new ArrayList();
                columns.add(taskId);
                columns.add(wfTask.getActivityDisplayName());
                columns.add(accepter);
                columns.add(duration);
                columns.add(completedDate);
                // Internal Cost Review permission based
                if (this.isInternalCostReview)
                {
                    columns.add(estimatedCost);
                    columns.add(actualCost);
                }

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
                    currentRow = k;
            }
            TableModelLens tml = new TableModelLens(table);

            // SortFilter sf = new SortFilter(tml, new int[]{0}, true);
            SubTableLens stl = new SubTableLens(tml, 0, 1, tml.getRowCount(),
                    tml.getColCount() - 1);
            Professional prof = new Professional(stl);
            prof.setRowFont(0, GlobalSightReplet.FONT_NORMAL);
            prof.setRowBackground(0, GlobalSightReplet.COLOR_TABLE_HDR_BG);
            // set all completed tasks to use normal font
            for (int u = 0; u < completedTasks.size(); u++)
            {
                prof.setRowFont(u + 1, GlobalSightReplet.FONT_NORMAL);
            }

            // now set all the uncompleted rows to use italics except for
            // default path
            for (int u = 0; u < uncompletedTasks.size(); u++)
            {
                ArrayList columns = (ArrayList) uncompletedTasks.get(u);
                Long taskId = (Long) columns.get(0);
                if (defaultTaskIds.contains(taskId))
                    prof.setRowFont(completedTasks.size() + u + 1,
                            GlobalSightReplet.FONT_NORMAL);
                else
                    prof.setRowFont(completedTasks.size() + u + 1,
                            GlobalSightReplet.FONT_ITALIC);
            }

            // now set the current row as green
            if (currentRow > -1)
            {
                prof.setRowForeground(currentRow + 1, new Color(0x009966));
            }

            String localeName = w.getTargetLocale().getDisplayName(theUiLocale);
            textId = ss.addText(localeName);
            ReportElement localeElement = ss.getElement(textId);
            localeElement.setFont(GlobalSightReplet.FONT_LABEL);
            localeElement.setAlignment(StyleConstants.CENTER);
            ss.addNewline(1);
            ss.addTable(prof);
            ss.addSeparator(StyleConstants.THIN_LINE);
        }
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
        Iterator liter = defaultTasks.iterator();
        ArrayList defaultTaskIds = new ArrayList();
        while (liter.hasNext())
        {
            WfTaskInfo wftaskinfo = (WfTaskInfo) liter.next();
            defaultTaskIds.add(new Long(wftaskinfo.getId()));
        }
        return defaultTaskIds;
    }

    private String m_completedActivitiesQuery = null;
    private static final int CA_TASK_USER = 2;

    /**
     * Returns a HashMap filled with an Object array containing the taskid,
     * taskname, and responsible user for each completed activity for all the
     * workflows for the specified job <br>
     * 
     * @param p_jobid
     *            -- the ID of the job
     * @return HashMap of Activities
     * @throws Exception
     */
    private HashMap findCompletedActivities(long p_jobid) throws Exception
    {
        if (m_completedActivitiesQuery == null)
        {
            SystemConfiguration sc = SystemConfiguration.getInstance();
            String twfuser = sc
                    .getStringParameter(SystemConfigParamNames.IFLOW_DB_USERNAME);
            StringBuffer sb = new StringBuffer();
            sb.append("SELECT AI.activityinstanceid, AD.label, H.responsible FROM ");
            sb.append(twfuser);
            sb.append(".history H, workflow W, ");
            sb.append(twfuser);
            sb.append(".activityinstance AI, ");
            sb.append(twfuser);
            sb.append(".activitydefinition AD WHERE W.job_id=? and H.processinstanceid=W.iflow_instance_id");
            sb.append(" and H.eventcode=2 and AI.processinstanceid=H.processinstanceid and AD.activitydefinitionid=AI.activitydefinitionid and AD.activitytypeid=2 and AI.activityinstanceid=H.consumerid order by AI.activityinstanceid");
            m_completedActivitiesQuery = sb.toString();
        }

        HashMap map = new HashMap();
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try
        {
            con = ConnectionPool.getConnection();
            ps = con.prepareStatement(m_completedActivitiesQuery);
            ps.setLong(1, p_jobid);

            rs = ps.executeQuery();
            while (rs.next())
            {
                Long instanceId = new Long(rs.getLong(1));
                String activityName = rs.getString(2);
                String userResponsible = UserUtil.getUserNameById(rs
                        .getString(3));
                Object[] values = new Object[]
                { instanceId, activityName, userResponsible };
                map.put(instanceId, values);
            }
            rs.close();
            rs = null;
            ps.close();
            ps = null;
        }
        finally
        {
            this.closeResultSet(rs);
            this.closeStatement(ps);
            this.returnConnection(con);
        }
        return map;
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
            while (Character.isSpace(p_filename.charAt(index)))
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
