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

import inetsoft.report.FormElement;
import inetsoft.report.ReportElement;
import inetsoft.report.ReportSheet;
import inetsoft.report.StyleConstants;
import inetsoft.report.StyleSheet;
import inetsoft.report.TextElement;
import inetsoft.report.lens.DefaultFormLens;
import inetsoft.report.lens.RotatedTableLens;
import inetsoft.report.lens.SubTableLens;
import inetsoft.report.lens.TableChartLens;
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
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import com.globalsight.diplomat.util.database.ConnectionPool;
import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.costing.Currency;
import com.globalsight.everest.costing.CurrencyFormat;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.page.SourcePage;
import com.globalsight.everest.persistence.PersistenceException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.taskmanager.Task;
import com.globalsight.everest.usermgr.UserLdapHelper;
import com.globalsight.everest.util.system.SystemConfigParamNames;
import com.globalsight.everest.util.system.SystemConfiguration;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.workflow.WfTaskInfo;
import com.globalsight.everest.workflow.WorkflowConstants;
import com.globalsight.everest.workflow.WorkflowInstance;
import com.globalsight.everest.workflow.WorkflowTaskInstance;
import com.globalsight.everest.workflowmanager.Workflow;
import com.globalsight.reports.handler.BasicReportHandler;
import com.globalsight.reports.util.LabeledValueHolder;
import com.globalsight.reports.util.ReportsPackage;
import com.globalsight.util.SortUtil;
import com.globalsight.util.date.DateHelper;

/**
 * Presents a report on workflow status
 */
public class JobDetailsReplet extends GlobalSightReplet
{
    private static final String MY_TEMPLATE = "/templates/basicFlowReport.srt";
    private static final String MY_MESSAGES = BasicReportHandler.BUNDLE_LOCATION
            + "jobDetails";

    private static final String LABEL_SUFFIX = ": ";
    protected ResourceBundle m_bundle = null;
    private StyleSheet ss = null;
    private Font m_labelFont = GlobalSightReplet.FONT_LABEL;
    private Font m_fieldFont = GlobalSightReplet.FONT_NORMAL;

    private ArrayList m_dispatchedJobs = new ArrayList();
    private ArrayList m_localizedJobs = new ArrayList();
    private ArrayList m_exportedJobs = new ArrayList();
    private ArrayList m_archivedJobs = new ArrayList();
    private Currency m_currency = null;

    private String getJobQuery()
    {
        String companyName = (String) theSession
                .getAttribute(UserLdapHelper.LDAP_ATTR_COMPANY);
        CompanyThreadLocal.getInstance().setValue(companyName);

        if (c_category.isDebugEnabled())
        {
            c_category.debug("companyId:"
                    + CompanyThreadLocal.getInstance().getValue());            
        }

        StringBuffer sb = new StringBuffer(
                "select job.id, job.name, job.state from job ");
        sb.append(" where (job.state='LOCALIZED' ");
        sb.append(" or job.state='EXPORTED' ");
        sb.append(" or job.state='DISPATCHED' ");
        sb.append(" or job.state='ARCHIVED') ");
        try
        {
            Vector arg = CompanyWrapper.addCompanyIdBoundArgs(new Vector());
            sb.append(" and job.company_id >= ");
            sb.append(arg.get(0));
            sb.append(" and job.company_id <= ");
            sb.append(arg.get(1));
        }
        catch (PersistenceException e)
        {
            e.printStackTrace();
        }
        sb.append(" order by job.id ");

        if (c_category.isDebugEnabled())
        {
            c_category.debug(sb.toString());            
        }

        return sb.toString();

    }

    public JobDetailsReplet()
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
            c_category.debug("addMoreRepletPrameters++");
            addMoreRepletParameters();
            c_category.debug("addMoreRepletPrameters--");
        }
        catch (RepletException re)
        {
            throw re;
        }
        catch (Exception e)
        {
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
     * Adds additional report creation parameters to the replet parameters
     */
    private void addMoreRepletParameters() throws Exception
    {
        queryAllJobs();
        addJobStatusParameter();
        addJobListParameters();
        if (isJobCostingOn())
            addCurrencyParameter(m_bundle);
        addRepletParameters(theParameters);
    }

    /**
     * Queries for all job ids,names,and states for (D,L,E,A) jobs <br>
     * 
     * @throws Exception
     */
    private void queryAllJobs() throws Exception
    {
        Connection c = null;
        PreparedStatement ps = null;

        try
        {
            c = ConnectionPool.getConnection();
            ps = c.prepareStatement(getJobQuery());
            ResultSet rs = ps.executeQuery();
            while (rs.next())
            {
                Long jobid = new Long(rs.getLong(1));
                String jobname = rs.getString(2);
                String state = rs.getString(3);
                StringBuffer displayJobName = new StringBuffer(jobname);
                displayJobName.append(" (").append(jobid.toString())
                        .append(")");
                LabeledValueHolder lvh = new LabeledValueHolder(jobid,
                        displayJobName.toString());
                if (state.equals(DISPATCHED))
                    m_dispatchedJobs.add(lvh);
                else if (state.equals(LOCALIZED))
                    m_localizedJobs.add(lvh);
                else if (state.equals(EXPORTED))
                    m_exportedJobs.add(lvh);
                else if (state.equals(ARCHIVED))
                    m_archivedJobs.add(lvh);
            }
            ps.close();
        }
        finally
        {
            this.closeStatement(ps);
            this.returnConnection(c);
        }
    }

    /**
     * Adds a radio button parameter for job status to the replet parameters so
     * that we know which job list they selected from <br>
     */
    protected void addJobStatusParameter()
    {
        Object dispatched = new LabeledValueHolder(DISPATCHED,
                ReportsPackage.getMessage(m_bundle, DISPATCHED));
        Object localized = new LabeledValueHolder(LOCALIZED,
                ReportsPackage.getMessage(m_bundle, LOCALIZED));
        Object exported = new LabeledValueHolder(EXPORTED,
                ReportsPackage.getMessage(m_bundle, EXPORTED));
        Object archived = new LabeledValueHolder(ARCHIVED,
                ReportsPackage.getMessage(m_bundle, ARCHIVED));
        ArrayList states = new ArrayList();
        if (m_dispatchedJobs.size() > 0)
            states.add(dispatched);
        if (m_localizedJobs.size() > 0)
            states.add(localized);
        if (m_exportedJobs.size() > 0)
            states.add(exported);
        if (m_archivedJobs.size() > 0)
            states.add(archived);
        if (states.size() > 0)
        {
            theParameters
                    .addRadio("jobstatus", states.get(0), states.toArray());
            theParameters.setAlias("jobstatus",
                    ReportsPackage.getMessage(m_bundle, "jobstatus"));
        }
    }

    /**
     * Adds a choice list of all the jobs in the given state as a replet
     * parameter. Adds four lists as replet parameters
     * (dispatched,localized,exported,archived)
     */
    private void addJobListParameters()
    {

        if (m_dispatchedJobs.size() > 0)
        {
            theParameters.addChoice("dispatchedJobs", m_dispatchedJobs.get(0),
                    m_dispatchedJobs.toArray());
            theParameters.setAlias("dispatchedJobs",
                    ReportsPackage.getMessage(m_bundle, "dispatchedJobs"));
        }

        if (m_localizedJobs.size() > 0)
        {
            theParameters.addChoice("localizedJobs", m_localizedJobs.get(0),
                    m_localizedJobs.toArray());
            theParameters.setAlias("localizedJobs",
                    ReportsPackage.getMessage(m_bundle, "localizedJobs"));
        }

        if (m_exportedJobs.size() > 0)
        {
            theParameters.addChoice("exportedJobs", m_exportedJobs.get(0),
                    m_exportedJobs.toArray());
            theParameters.setAlias("exportedJobs",
                    ReportsPackage.getMessage(m_bundle, "exportedJobs"));
        }

        if (m_archivedJobs.size() > 0)
        {
            theParameters.addChoice("archivedJobs", m_archivedJobs.get(0),
                    m_archivedJobs.toArray());
            theParameters.setAlias("archivedJobs",
                    ReportsPackage.getMessage(m_bundle, "archivedJobs"));
        }
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
        HttpServletRequest http_request = (HttpServletRequest) req
                .getParameter(RepletRequest.HTTP_REQUEST);
        theSession = http_request.getSession(false);
        String sessionId = theSession.getId();
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
        String companyId = CompanyThreadLocal.getInstance().getValue();
        if (companyId == null)
        {
            String companyName = (String) theSession
                    .getAttribute(UserLdapHelper.LDAP_ATTR_COMPANY);
            CompanyThreadLocal.getInstance().setValue(companyName);
        }

        HttpServletRequest http_request = (HttpServletRequest) req
                .getParameter(RepletRequest.HTTP_REQUEST);
        theSession = http_request.getSession(false);
        // add a form at the top to show the criteria that was selected for the
        // report
        LabeledValueHolder chosenStateHolder = (LabeledValueHolder) req
                .getParameter("jobstatus");
        if (chosenStateHolder == null)
        {
            // there are no jobs!
            return;
        }

        if (isJobCostingOn())
        {
            LabeledValueHolder chosenCurrency = (LabeledValueHolder) req
                    .getParameter("currency");
            m_currency = (Currency) chosenCurrency.getValue();
        }
        else
            m_currency = null;

        String chosenState = (String) chosenStateHolder.getValue();
        LabeledValueHolder chosenJobHolder = null;
        if (chosenState.equals(DISPATCHED))
            chosenJobHolder = (LabeledValueHolder) req
                    .getParameter("dispatchedJobs");
        else if (chosenState.equals(LOCALIZED))
            chosenJobHolder = (LabeledValueHolder) req
                    .getParameter("localizedJobs");
        else if (chosenState.equals(EXPORTED))
            chosenJobHolder = (LabeledValueHolder) req
                    .getParameter("exportedJobs");
        else if (chosenState.equals(ARCHIVED))
            chosenJobHolder = (LabeledValueHolder) req
                    .getParameter("archivedJobs");

        Long jobId = (Long) chosenJobHolder.getValue();
        String jobName = chosenJobHolder.getLabel();
        // remove the jobid that was appended at the end
        jobName = jobName.substring(0, jobName.lastIndexOf(" ("));

        addCriteriaFormAtTop(chosenStateHolder.getLabel(), jobName);

        Job job = ServerProxy.getJobHandler().getJobById(jobId.longValue());
        super.setHeaders(job);
        Collection c = ServerProxy.getJobReportingManager()
                .getWorkflowsByJobId(job.getId());
        ArrayList workflows = new ArrayList(c);
        WorkflowTableModel wtm = new WorkflowTableModel(workflows, theSession,
                m_currency, false);
        boolean isInContextMatch = PageHandler.isInContextMatch(job);
        if (!isInContextMatch)
        {
            String[] columns =
            { "TRGLOCALE", "WFSTATE", "CURACTIVITY", "ACCEPTER",
                    "SEGMENT_TM_WC", "CONTEXT_WC", "FUZZY_LOW_WC",
                    "FUZZY_MED_WC", "FUZZY_MED_HI_WC", "FUZZY_HI_WC",
                    "NO_MATCH", "REP_WC", "PER_COMPLETE", "DURATION", "COST",
                    "REVENUE" };
            wtm.setColumnNames(columns);
        }
        wtm.fillAllData(null, workflows);
        ArrayList jobs = new ArrayList();
        jobs.add(job);
        JobTableModel jtm = new JobTableModel(jobs, theUiLocale, m_currency);

        ss.setCurrentAlignment(StyleConstants.H_LEFT);
        ss.setCurrentWrapping(ReportSheet.WRAP_TOP_BOTTOM);
        addJobForm(jtm, 0, job);

        addWorkflowSummaryChart(wtm);
        ss.addSeparator(StyleConstants.THIN_LINE);
        ss.addNewline(2);

        addWorkflowSummaryTable(wtm, isInContextMatch);
        ss.addNewline(2);
        ss.addSeparator(StyleConstants.THIN_LINE);

        addDetailedWorkflowBreakdownTables(jobId.longValue(), wtm, workflows);
        ss.addNewline(2);
        ss.addSeparator(StyleConstants.THICK_LINE);
        addTableWithPageNames(job);
    }

    /**
     * Adds the given WorkflowTableModel to the report <br>
     * 
     * @param WorkflowTableModel
     *            p_wtm
     */
    private void addWorkflowSummaryTable(WorkflowTableModel p_wtm,
            boolean isInContextMatch)
    {
        int[] subcols;
        int i = 0;
        if (isJobCostingOn())
            subcols = new int[13];
        else
            subcols = new int[12];

        subcols[i++] = WorkflowTableModel.TRGLOCALE;
        subcols[i++] = WorkflowTableModel.WFSTATE;
        subcols[i++] = WorkflowTableModel.SEGMENT_TM_WC;

        if (isInContextMatch)
        {
            subcols[i++] = WorkflowTableModel.IN_CONTEXT_WC;
        }
        subcols[i++] = WorkflowTableModel.IN_CONTEXT_WC;
        // subcols[i++] = WorkflowTableModel.CONTEXT_WC;
        subcols[i++] = WorkflowTableModel.FUZZY_HI_WC;
        subcols[i++] = WorkflowTableModel.FUZZY_MED_HI_WC;
        subcols[i++] = WorkflowTableModel.FUZZY_MED_WC;
        subcols[i++] = WorkflowTableModel.FUZZY_LOW_WC;
        subcols[i++] = WorkflowTableModel.NO_MATCH;
        subcols[i++] = WorkflowTableModel.REP_WC;
        subcols[i++] = WorkflowTableModel.PER_COMPLETE;
        if (isJobCostingOn())
        {
            subcols[i++] = WorkflowTableModel.COST;
            NumberFormat currencyFormat = CurrencyFormat
                    .getCurrencyFormat(m_currency);

            // now run through the table and replace the cost with a formatted
            // cost
            for (int r = 0; r < p_wtm.getRowCount(); r++)
            {
                double cost = ((Float) p_wtm.getValueAt(r,
                        WorkflowTableModel.COST)).doubleValue();
                String formattedCurrency = currencyFormat.format(cost);
                p_wtm.setValueAt(formattedCurrency, r, WorkflowTableModel.COST);
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

    /**
     * Adds the given WorkflowTableModel to the report as a chart <br>
     * 
     * @param WorkflowTableModel
     *            p_wtm
     * @param p_chosenState
     *            -- the job state
     */
    private void addWorkflowSummaryChart(WorkflowTableModel p_wtm)
    {
        TableModelLens model = new TableModelLens(p_wtm);
        model.setColAutoSize(true);

        int numRows = model.getRowCount() - 1;
        int[] subrows = new int[numRows];
        String[] labels = new String[numRows];
        for (int r = 0; r < numRows; r++)
        {
            labels[r] = (String) model.getObject(r + 1,
                    WorkflowTableModel.TRGLOCALE);
            subrows[r] = r + 1;
        }

        int[] subcols = new int[]
        { WorkflowTableModel.PER_COMPLETE };
        SubTableLens stl = new SubTableLens(model, subrows, subcols);
        TableChartLens tcl = new TableChartLens(stl, false);
        tcl.setLabels(labels);
        tcl.setYTitle(ReportsPackage.getMessage(m_bundle, "wfSummaryChartY"));
        tcl.setXTitle(ReportsPackage.getMessage(m_bundle, "wfSummaryChartX"));
        tcl.setStyle(StyleConstants.CHART_BAR);
        tcl.setTitleFont(GlobalSightReplet.FONT_CHART_TITLE);
        tcl.setMaximum(new Integer(100));
        tcl.setIncrement(new Integer(10));
        ss.addChart(tcl);
    }

    /**
     * Adds a form at the top with the selected job state and job name <br>
     * 
     * @param projectmgr
     *            -- the selected PM
     * @param wfstate
     *            -- the selected WF state
     */
    private void addCriteriaFormAtTop(String p_jobstate, String p_jobname)
    {
        ArrayList labels = new ArrayList();
        labels.add(ReportsPackage.getMessage(m_bundle, "jobstatus")
                + LABEL_SUFFIX);
        labels.add(ReportsPackage.getMessage(m_bundle, "jobname")
                + LABEL_SUFFIX);

        ArrayList fields = new ArrayList();
        fields.add(p_jobstate);
        fields.add(p_jobname);

        if (c_category.isDebugEnabled())
        {
            c_category.debug("p_jobState:" + p_jobstate);
            c_category.debug("p_jobname:" + p_jobname);            
        }

        if (isJobCostingOn())
        {
            labels.add(ReportsPackage.getMessage(m_bundle, "currency")
                    + LABEL_SUFFIX);
            fields.add(m_currency.getDisplayName());
        }

        DefaultFormLens dfl = new DefaultFormLens(labels.toArray(),
                fields.toArray());
        dfl.setLabelFont(m_labelFont);
        dfl.setFont(m_fieldFont);
        dfl.setUnderline(StyleConstants.NONE);
        dfl.setFieldPerRow(3);
        String formid = ss.addForm(dfl);
        FormElement form = (FormElement) ss.getElement(formid);
        form.setLayout(ReportSheet.TABLE_FIT_CONTENT);
        ss.addNewline(1);
        ss.addSeparator(StyleConstants.THIN_LINE);
    }

    /**
     * Adds a form to the report containing the job information <br>
     * 
     * @param p_table
     *            -- a table model
     * @param r
     *            -- the current row
     */
    private void addJobForm(TableModel p_table, int r, Job p_job)
    {
        // don't include job id and job name
        ArrayList labels = new ArrayList();
        ArrayList fields = new ArrayList();
        String origLabel;
        Object field = null;

        // start with 1 to skip jobid and then skip jobname in the loop
        for (int c = 1; c < p_table.getColumnCount(); c++)
        {
            if (c == JobTableModel.JOBNAME || c == JobTableModel.JOB_OBJECT)
            {
                // don't want jobname since it's in the criteria
                // don't want job object
                continue;
            }

            if (c == JobTableModel.COST && isJobCostingOn())
            {
                double cost = ((Float) p_table.getValueAt(r, c)).doubleValue();
                String formattedCost = CurrencyFormat.getCurrencyFormat(
                        m_currency).format(cost);
                field = formattedCost;
            }
            else
            {
                field = p_table.getValueAt(r, c);
            }

            origLabel = p_table.getColumnName(c);
            labels.add(origLabel + LABEL_SUFFIX);
            fields.add(field);
        }
        // add additional fields to the form
        labels.add(ReportsPackage.getMessage(m_bundle, "locProfile")
                + LABEL_SUFFIX);
        fields.add(p_job.getL10nProfile().getName());

        // figure out max label length
        int maxLabelLen = 0;
        for (int j = 0; j < labels.size(); j++)
        {
            String l = (String) labels.get(j);
            maxLabelLen = (l.length() > maxLabelLen) ? l.length() : maxLabelLen;
        }

        // add the form as a table so we have better control over layout
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

    private void dump(ArrayList p_a)
    {
        Iterator iter = p_a.iterator();
        while (iter.hasNext())
        {
            WorkflowTaskInstance wft = (WorkflowTaskInstance) iter.next();
            System.out.println("Task " + wft.getTaskId() + " "
                    + wft.getActivityName());
        }
    }

    /**
     * Outputs a table containing the activities for each workflow <br>
     * 
     * @param ArrayList
     *            p_workflows -- the workflows for this job
     */
    private void addDetailedWorkflowBreakdownTables(long p_jobId,
            WorkflowTableModel p_wtm, ArrayList p_workflows) throws Exception
    {
        ss.setCurrentFont(GlobalSightReplet.FONT_NORMAL);
        Object[] columnNames = new Object[]
        { "hiddenActivityId",
                ReportsPackage.getMessage(m_bundle, "activityName"),
                ReportsPackage.getMessage(m_bundle, "accepter"),
                ReportsPackage.getMessage(m_bundle, "assigneeRole"),
                ReportsPackage.getMessage(m_bundle, "duration"),
                ReportsPackage.getMessage(m_bundle, "compDate") };

        String textId = ss.addText(ReportsPackage.getMessage(m_bundle,
                "activityBreakdown"));
        // set the label font after adding the table so it doesn't affect the
        // table font
        ss.getElement(textId).setFont(GlobalSightReplet.FONT_LABEL);
        ss.addNewline(2);

        // query out all the completed activities for all the workflows for this
        // job
        HashMap completedActivities = findCompletedActivities(p_jobId);

        for (int i = 0; i < p_workflows.size(); i++)
        {
            Workflow w = (Workflow) p_workflows.get(i);
            WorkflowInstance wfi = ServerProxy.getWorkflowServer()
                    .getWorkflowInstanceById(w.getId());
            ArrayList wfiTasks = new ArrayList(wfi.getWorkflowInstanceTasks());
            List defaultTasks = findDefaultTaskIds(w.getId());
            Comparator comparator = new MyTaskComparator(defaultTasks,
                    completedActivities);
            SortUtil.sort(wfiTasks, comparator);
            Hashtable tasks = w.getTasks();
            int activeRow = -1;
            DefaultTableModel table = new DefaultTableModel(tasks.size(),
                    columnNames.length);
            table.setColumnIdentifiers(columnNames);

            int jcol = -1;
            for (int j = 0; j < wfiTasks.size(); j++)
            {
                WorkflowTaskInstance wfTask = (WorkflowTaskInstance) wfiTasks
                        .get(j);
                if (wfTask.getType() != WorkflowConstants.ACTIVITY)
                    continue;
                else
                    jcol++;

                Long taskId = new Long(wfTask.getTaskId());
                Task task = (Task) tasks.get(taskId);
                String accepter = ReportsPackage.getMessage(m_bundle,
                        "notAcceptedYet");
                if (wfTask.getTaskState() == Task.STATE_ACTIVE)
                {
                    activeRow = jcol;
                    Map activeTasks = ServerProxy.getWorkflowServer()
                            .getActiveTasksForWorkflow(w.getId());
                    WorkflowTaskInstance thisTask = (WorkflowTaskInstance) activeTasks
                            .get(taskId);
                    if (thisTask != null)
                        accepter = thisTask.getAccepter();

                    if (accepter == null || accepter.length() == 0)
                    {
                        accepter = ReportsPackage.getMessage(m_bundle,
                                "notAcceptedYet");
                    }

                }
                else
                {
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
                if (task == null || task.getCompletedDate() == null)
                {
                    completedDate = "--";
                }
                else
                {
                    completedDate = DateHelper.getFormattedDateAndTime(
                            task.getCompletedDate(), theUiLocale);
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
                    duration = "--";

                int k = 0;
                table.setValueAt(taskId, jcol, k++);
                table.setValueAt(wfTask.getActivityDisplayName(), jcol, k++);
                table.setValueAt(accepter, jcol, k++);
                table.setValueAt(wfTask.getRolesAsString(), jcol, k++);
                table.setValueAt(duration, jcol, k++);
                table.setValueAt(completedDate, jcol, k++);
            }

            TableModelLens tml = new TableModelLens(table);
            SubTableLens stl = new SubTableLens(tml, 0, 1, tml.getRowCount(),
                    tml.getColCount() - 1);
            Professional prof = new Professional(stl);
            prof.setRowFont(0, GlobalSightReplet.FONT_NORMAL);
            prof.setRowBackground(0, GlobalSightReplet.COLOR_TABLE_HDR_BG);

            // now set the current row as green
            if (activeRow > -1)
            {
                int row = activeRow + 1;
                prof.setRowForeground(row, new Color(0x009966));
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

    private StringBuffer m_completedActivitiesQuery = null;
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
            sb.append("SELECT");
            sb.append(" AI.activityinstanceid, AD.label, H.responsible \n");
            sb.append("FROM ");
            sb.append(twfuser);
            sb.append(".history H, workflow W, ");
            sb.append(twfuser);
            sb.append(".activityinstance AI, ");
            sb.append(twfuser);
            sb.append(".activitydefinition AD \n");
            sb.append("WHERE");
            sb.append(" W.job_id=? and H.processinstanceid=W.iflow_instance_id \n");
            sb.append(" and H.eventcode=2 and AI.processinstanceid=H.processinstanceid and AD.activitydefinitionid=AI.activitydefinitionid and AD.activitytypeid=2 and AI.activityinstanceid=H.consumerid \n");
            sb.append("order by AI.activityinstanceid");

            if (c_category.isDebugEnabled())
            {
                c_category.debug(sb.toString());                
            }
            m_completedActivitiesQuery = sb;
        }

        HashMap map = new HashMap();
        Connection c = null;
        PreparedStatement ps = null;
        try
        {
            c = ConnectionPool.getConnection();
            ps = c.prepareStatement(m_completedActivitiesQuery.toString());
            ps.setLong(1, p_jobid);

            ResultSet rs = ps.executeQuery();
            while (rs.next())
            {
                Long instanceId = new Long(rs.getLong(1));
                String activityName = rs.getString(2);
                String userResponsible = rs.getString(3);
                Object[] values = new Object[]
                { instanceId, activityName, userResponsible };
                map.put(instanceId, values);
            }
            ps.close();
        }
        finally
        {
            this.closeStatement(ps);
            this.returnConnection(c);
        }

        return map;
    }

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

    class MyTaskComparator implements Comparator
    {
        private List m_defaultTaskIds = null;
        private HashMap m_completedActivities = null;

        public MyTaskComparator(List p_defaultTaskIds,
                HashMap p_completedActivities)
        {
            m_defaultTaskIds = p_defaultTaskIds;
            m_completedActivities = p_completedActivities;
        }

        public int compare(Object p_a, Object p_b)
        {
            WorkflowTaskInstance a_wfTask = (WorkflowTaskInstance) p_a;
            WorkflowTaskInstance b_wfTask = (WorkflowTaskInstance) p_b;
            Long a_taskId = new Long(a_wfTask.getTaskId());
            Long b_taskId = new Long(b_wfTask.getTaskId());
            // compare only activities
            boolean a_isActivity = (a_wfTask.getType() == WorkflowConstants.ACTIVITY);
            boolean b_isActivity = (b_wfTask.getType() == WorkflowConstants.ACTIVITY);
            if (a_isActivity && !b_isActivity)
            {
                return 1;
            }
            else if (!a_isActivity && b_isActivity)
            {
                return -1;
            }
            else if (!a_isActivity && !b_isActivity)
            {
                return a_taskId.compareTo(b_taskId);
            }

            // see if one is completed
            boolean a_isCompleted = m_completedActivities.containsKey(a_taskId);
            boolean b_isCompleted = m_completedActivities.containsKey(b_taskId);
            if (a_isCompleted && !b_isCompleted)
            {
                return -1;
            }
            else if (!a_isCompleted && b_isCompleted)
            {
                return 1;
            }

            // now see if one of them is in the default path or not
            boolean a_isInDefault = m_defaultTaskIds.contains(a_taskId);
            boolean b_isInDefault = m_defaultTaskIds.contains(b_taskId);
            if (a_isInDefault && !b_isInDefault)
            {
                return -1;
            }
            else if (!a_isInDefault && b_isInDefault)
            {
                return 1;
            }
            else if (a_isInDefault && b_isInDefault)
            {
                // they're both in default path, so find whichever comes first
                int a_pos = m_defaultTaskIds.indexOf(a_taskId);
                int b_pos = m_defaultTaskIds.indexOf(b_taskId);
                return a_pos - b_pos;
            }
            else
            {
                // neither is in default path, so just compare task IDs
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
