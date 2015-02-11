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
import inetsoft.report.filter.DefaultSortedTable;
import inetsoft.report.filter.SortFilter;
import inetsoft.report.lens.DefaultFormLens;
import inetsoft.report.lens.SubTableLens;
import inetsoft.report.lens.TableChartLens;
import inetsoft.report.lens.swing.TableModelLens;
import inetsoft.report.style.Professional;
import inetsoft.sree.RepletException;
import inetsoft.sree.RepletRequest;
import inetsoft.sree.SreeLog;

import java.awt.Font;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ResourceBundle;
import java.util.TreeSet;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.swing.table.AbstractTableModel;

import com.globalsight.diplomat.util.database.ConnectionPool;
import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.costing.Currency;
import com.globalsight.everest.costing.CurrencyFormat;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.persistence.PersistenceException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.usermgr.UserLdapHelper;
import com.globalsight.reports.handler.BasicReportHandler;
import com.globalsight.reports.util.LabeledValueHolder;
import com.globalsight.reports.util.ReportsPackage;

/**
 * Presents a report on workflow status
 */
public class WorkflowStatusReplet extends GlobalSightReplet
{
    private static final String MY_TEMPLATE = "/templates/basicFlowReport.srt";
    private static final String MY_MESSAGES = BasicReportHandler.BUNDLE_LOCATION
            + "workflowStatus";

    private static final String LABEL_SUFFIX = ": ";
    protected ResourceBundle m_bundle = null;
    private StyleSheet ss = null;
    private Font m_labelFont = GlobalSightReplet.FONT_LABEL;
    private Font m_fieldFont = GlobalSightReplet.FONT_NORMAL;
    private Currency m_currency = null;

    private String getPmQuery()
    {
        String companyName = (String) theSession
                .getAttribute(UserLdapHelper.LDAP_ATTR_COMPANY);
        CompanyThreadLocal.getInstance().setValue(companyName);
        c_category.debug("companyId:"
                + CompanyThreadLocal.getInstance().getValue());

        StringBuffer sb = new StringBuffer();
        sb.append("select distinct manager_user_id from project ");
        try
        {
            Vector arg = CompanyWrapper.addCompanyIdBoundArgs(new Vector());
            sb.append(" where project.companyid >= ");
            sb.append((arg.get(0)));
            sb.append(" and project.companyid <= ");
            sb.append(arg.get(1));
        }
        catch (PersistenceException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        sb.append(" order by manager_user_id");
        c_category.debug(sb.toString());

        return sb.toString();
    }

    public WorkflowStatusReplet()
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
            addMoreRepletParameters();
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
        addWorkflowStatusParameter();
        addProjectManagerParameter();
        if (isJobCostingOn())
            addCurrencyParameter(m_bundle);

        addRepletParameters(theParameters);
    }

    /**
     * Adds a parameter for workflow status to the replet parameters <br>
     */
    protected void addWorkflowStatusParameter()
    {
        Object[] wfstates = new Object[6];
        Object dispatched = new LabeledValueHolder(DISPATCHED,
                ReportsPackage.getMessage(m_bundle, DISPATCHED));
        int i = 0;
        wfstates[i++] = new LabeledValueHolder(PENDING,
                ReportsPackage.getMessage(m_bundle, PENDING));
        wfstates[i++] = new LabeledValueHolder(READY,
                ReportsPackage.getMessage(m_bundle, READY));
        wfstates[i++] = dispatched;
        wfstates[i++] = new LabeledValueHolder(LOCALIZED,
                ReportsPackage.getMessage(m_bundle, LOCALIZED));
        wfstates[i++] = new LabeledValueHolder(EXPORTED,
                ReportsPackage.getMessage(m_bundle, EXPORTED));
        wfstates[i++] = new LabeledValueHolder(ARCHIVED,
                ReportsPackage.getMessage(m_bundle, ARCHIVED));
        theParameters.addChoice("wfstatus", dispatched, wfstates);
        theParameters.setAlias("wfstatus",
                ReportsPackage.getMessage(m_bundle, "wfstatus"));
    }

    /**
     * Adds the user ids of the project managers who actually have projects as
     * replet parameters <br>
     */
    protected void addProjectManagerParameter() throws Exception
    {
        Connection c = null;
        PreparedStatement ps = null;
        try
        {
            ArrayList projectManagers = new ArrayList();
            c = ConnectionPool.getConnection();
            ps = c.prepareStatement(getPmQuery());
            ResultSet rs = ps.executeQuery();
            projectManagers.add(ReportsPackage.getMessage(m_bundle,
                    "criteria_allPms"));
            while (rs.next())
                projectManagers.add(rs.getString(1));
            ps.close();

            String defvalue = null;
            if (projectManagers.contains(theUsername))
                defvalue = theUsername;
            else
                defvalue = (String) projectManagers.get(0);

            theParameters.addChoice("projectMgr", defvalue,
                    projectManagers.toArray());
            theParameters.setAlias("projectMgr",
                    ReportsPackage.getMessage(m_bundle, "projectMgr"));
        }
        finally
        {
            this.closeStatement(ps);
            this.returnConnection(c);
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
        HttpServletRequest http_request = (HttpServletRequest) req
                .getParameter(RepletRequest.HTTP_REQUEST);
        theSession = http_request.getSession(false);
        if (isJobCostingOn())
        {
            LabeledValueHolder chosenCurrency = (LabeledValueHolder) req
                    .getParameter("currency");
            m_currency = (Currency) chosenCurrency.getValue();
        }
        else
            m_currency = null;

        // add a form at the top to show the criteria that was selected for the
        // report
        LabeledValueHolder v = (LabeledValueHolder) req
                .getParameter("wfstatus");
        String projectManager = req.getString("projectMgr");
        String wfstate = v.getValue().toString();
        addCriteriaFormAtTop(projectManager, wfstate);

        ArrayList jobs = null;
        String companyId = CompanyThreadLocal.getInstance().getValue();
        System.out.println("companyId:" + companyId);
        System.out.println("ClassName:" + this.getClass().getName());
        System.out.println("ThreadName:" + Thread.currentThread().getName());
        if (companyId == null)
        {
            String companyName = (String) theSession
                    .getAttribute(UserLdapHelper.LDAP_ATTR_COMPANY);
            CompanyThreadLocal.getInstance().setValue(companyName);
        }
        if (projectManager.equals(ReportsPackage.getMessage(m_bundle,
                "criteria_allPms")))
        {
            jobs = new ArrayList(ServerProxy.getJobHandler().getJobsByState(
                    wfstate));
        }
        else
        {
            jobs = new ArrayList(ServerProxy.getJobHandler()
                    .getJobsByManagerIdAndState(projectManager, wfstate));
        }

        super.setHeaders(jobs);

        // put all the job and workflow data into tables
        ArrayList jobWorkflows = getAllWorkflowsForAllJobs(jobs);
        JobTableModel jtm = new JobTableModel(jobs, theUiLocale, m_currency);

        if (jtm.getRowCount() == 0)
        {
            // no jobs in the system
            return;
        }

        String[] headers = super.getHeaders();
        HashMap workflowTableModels = makeWorkflowTableModels(jtm,
                jobWorkflows, wfstate, headers);

        // add a chart showing how many jobs are in each activity if we're
        // showing jobs in progress
        if (wfstate.equals(DISPATCHED))
            showJobsPerActivityChart(jtm, workflowTableModels);

        // now tell how many jobs there are
        String textId = ss.addText(ReportsPackage.getMessage(m_bundle,
                "totalNumJobs") + " " + jtm.getRowCount());
        ReportElement re = ss.getElement(textId);
        re.setFont(m_labelFont);
        re.setAlignment(StyleConstants.H_CENTER);
        ss.addNewline(2);

        // sort the job table model by job_id,
        // priority,project,pm,create_date,jobname
        TableModelLens tml = new TableModelLens(jtm);
        int[] sortCols = new int[]
        { JobTableModel.PRIORITY, JobTableModel.PROJECT, JobTableModel.PM,
                JobTableModel.CREATE_DATE, JobTableModel.JOBNAME,
                JobTableModel.ID };
        SortFilter sf = new SortFilter(tml, sortCols, true);
        DefaultSortedTable dst = new DefaultSortedTable(sf, sortCols);

        ss.setCurrentAlignment(StyleConstants.H_LEFT);

        // now run through the sorted table and add a form at the top for each
        // job
        for (int r = 0; r < jtm.getRowCount(); r++)
        {
            Long jobId = (Long) dst.getObject(r + 1, JobTableModel.ID); // ignore
            // header
            // row
            WorkflowTableModel wtm = (WorkflowTableModel) workflowTableModels
                    .get(jobId);

            // only show jobs with workflows
            if (wtm.getRowCount() > 0)
            {
                TableModelLens model = new TableModelLens(wtm);
                model.setColAutoSize(true);

                // only show dispatched related columns if the workflow is
                // dispatched.
                // and only show cost if jobcosting is on.
                int numCols = 11;
                boolean hasDispatchInfo = wfstate.equals(DISPATCHED);
                boolean hasCostInfo = isJobCostingOn();
                if (hasDispatchInfo)
                    numCols += 3;
                if (hasCostInfo)
                    numCols++;
                int subcols[] = new int[numCols];
                int l = 0;
                subcols[l++] = WorkflowTableModel.TRGLOCALE;
                if (hasDispatchInfo)
                {
                    subcols[l++] = WorkflowTableModel.CURACTIVITY;
                    subcols[l++] = WorkflowTableModel.ACCEPTER;
                }
                subcols[l++] = WorkflowTableModel.SEGMENT_TM_WC;
                if (headers[0] != null)
                {
                    subcols[l++] = WorkflowTableModel.IN_CONTEXT_WC;
                }
                subcols[l++] = WorkflowTableModel.FUZZY_HI_WC;
                subcols[l++] = WorkflowTableModel.FUZZY_MED_HI_WC;
                subcols[l++] = WorkflowTableModel.FUZZY_MED_WC;
                subcols[l++] = WorkflowTableModel.FUZZY_LOW_WC;
                subcols[l++] = WorkflowTableModel.NO_MATCH;
                subcols[l++] = WorkflowTableModel.REP_WC;
                subcols[l++] = WorkflowTableModel.PER_COMPLETE;
                if (hasDispatchInfo)
                    subcols[l++] = WorkflowTableModel.DURATION;
                if (hasCostInfo)
                {
                    subcols[l++] = WorkflowTableModel.COST;
                    NumberFormat currencyFormat = CurrencyFormat
                            .getCurrencyFormat(m_currency);
                    // now run through the table and replace the cost with a
                    // formatted cost
                    for (int rx = 0; rx < wtm.getRowCount(); rx++)
                    {
                        double cost = ((Float) wtm.getValueAt(rx,
                                WorkflowTableModel.COST)).doubleValue();
                        String formattedCurrency = currencyFormat.format(cost);
                        wtm.setValueAt(formattedCurrency, rx,
                                WorkflowTableModel.COST);
                    }
                }

                SubTableLens stl = new SubTableLens(model, null, subcols);
                ss.addSeparator(StyleConstants.THIN_LINE);
                addJobForm(dst, r);
                Professional workflowtable = new Professional(stl);
                workflowtable.setRowBackground(0,
                        GlobalSightReplet.COLOR_TABLE_HDR_BG);
                workflowtable.setFont(GlobalSightReplet.FONT_NORMAL);
                ss.addTable(workflowtable);
                ss.addNewline(2);
                ss.addConditionalPageBreak(3.0);
            }
        }
    }

    /**
     * Displays a simple bar chart showing the number of jobs per activity <br>
     */
    private void showJobsPerActivityChart(JobTableModel p_jtm,
            HashMap p_workflowTableModels)
    {
        // run through the jobs and workflows and count up how many jobs are in
        // each activity
        ActivityCountTableModel actm = new ActivityCountTableModel(p_jtm,
                p_workflowTableModels);
        TableChartLens tcl = new TableChartLens(new TableModelLens(actm));
        tcl.setYTitle(ReportsPackage.getMessage(m_bundle, "numberOfWorkflows"));
        tcl.setXTitle(ReportsPackage.getMessage(m_bundle, "currentActivity"));
        tcl.setStyle(StyleConstants.CHART_BAR);
        tcl.setTitleFont(GlobalSightReplet.FONT_CHART_TITLE);
        ss.addChart(tcl);
    }

    /**
     * Returns an arraylist containing workflowtablemodels for each job <br>
     * 
     * @param p_jobWorkflows
     *            -- an arraylist of arraylists for each job containing the
     *            workflows
     * @return ArrayList of WorkflowTableModels
     */
    private HashMap makeWorkflowTableModels(JobTableModel p_jtm,
            ArrayList p_jobWorkflows, String p_wfstate, String[] headers)
    {
        HashMap wtms = new HashMap();
        for (int i = 0; i < p_jobWorkflows.size(); i++)
        {
            ArrayList workflows = (ArrayList) p_jobWorkflows.get(i);
            WorkflowTableModel wtm = new WorkflowTableModel(workflows,
                    p_wfstate, theSession, m_currency, false);
            String[] columns =
            { "TRGLOCALE", "WFSTATE", "CURACTIVITY", "ACCEPTER",
                    "SEGMENT_TM_WC", "CONTEXT_WC", "FUZZY_LOW_WC",
                    "FUZZY_MED_WC", "FUZZY_MED_HI_WC", "FUZZY_HI_WC",
                    "NO_MATCH", "REP_WC", "PER_COMPLETE", "DURATION", "COST",
                    "REVENUE" };
            if (headers[0] == null)
            {
                wtm.setColumnNames(columns);
                wtm.setUseInContext(false);
            }
            else
            {
                wtm.setUseInContext(true);
            }
            Long jobid = (Long) p_jtm.getValueAt(i, 0);
            wtm.fillAllData(p_wfstate, workflows);
            wtms.put(jobid, wtm);

        }
        return wtms;
    }

    /**
     * Returns an array list containing an array list for each job containing
     * its workflows <br>
     * 
     * @param p_jobs
     *            -- ArrayList of jobs
     */
    private ArrayList getAllWorkflowsForAllJobs(ArrayList p_jobs)
    {
        ArrayList jobWorkflows = new ArrayList(); // contains an array list
        // for each job
        for (int i = 0; i < p_jobs.size(); i++)
        {
            Job job = (Job) p_jobs.get(i);
            ArrayList workflows = null; // the workflows for this job
            try
            {
                workflows = new ArrayList(ServerProxy.getJobReportingManager()
                        .getWorkflowsByJobId(job.getId()));
            }
            catch (Exception e)
            {
                workflows = new ArrayList();
                ReportsPackage.logError(e);
            }
            jobWorkflows.add(workflows);
        }
        return jobWorkflows;
    }

    /**
     * Adds a form at the top with the selected PM and workflow state <br>
     * 
     * @param projectmgr
     *            -- the selected PM
     * @param wfstate
     *            -- the selected WF state
     */
    private void addCriteriaFormAtTop(String projectManager, String wfstate)
    {
        ArrayList labels = new ArrayList();
        ArrayList fields = new ArrayList();

        labels.add(ReportsPackage.getMessage(m_bundle, "wfstatus")
                + LABEL_SUFFIX);
        labels.add(ReportsPackage.getMessage(m_bundle, "projectMgr")
                + LABEL_SUFFIX);

        fields.add(ReportsPackage.getMessage(m_bundle, wfstate));
        fields.add(projectManager);

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
        ss.addForm(dfl);
        ss.addNewline(1);
        ss.addSeparator(StyleConstants.THIN_LINE);
    }

    /**
     * Adds a form to the report containing the job information <br>
     * 
     * @param p_jtm
     *            -- the JobTableModel
     * @param r
     *            -- the current row
     */
    private void addJobForm(DefaultSortedTable p_dst, int r)
    {
        ArrayList labels = new ArrayList();
        ArrayList fields = new ArrayList();

        // make sure that Job Name is first
        Job job = (Job) p_dst.getObject(r + 1, JobTableModel.JOB_OBJECT);
        String origLabel = (String) p_dst.getObject(0, JobTableModel.JOBNAME);
        String newLabel = origLabel + LABEL_SUFFIX;
        labels.add(newLabel);
        fields.add(p_dst.getObject(r + 1, JobTableModel.JOBNAME));

        // set the other fields and labels
        Object field = null;
        for (int c = 1; c < p_dst.getColCount(); c++)
        {
            if (c == JobTableModel.JOBNAME || c == JobTableModel.JOB_OBJECT)
            {
                // don't want jobname since it's in the criteria
                // don't want job object
                continue;
            }

            if (isJobCostingOn()
                    && (c == JobTableModel.COST || c == JobTableModel.REVENUE))
            {
                double cost = ((Float) p_dst.getObject(r + 1, c)).doubleValue();
                String formattedCost = CurrencyFormat.getCurrencyFormat(
                        m_currency).format(cost);
                field = formattedCost;
            }
            else
            {
                field = p_dst.getObject(r + 1, c);
            }

            origLabel = (String) p_dst.getObject(0, c);
            labels.add(origLabel + LABEL_SUFFIX);
            fields.add(field);
        }

        DefaultFormLens dfl = new DefaultFormLens(labels.toArray(),
                fields.toArray());
        dfl.setLabelFont(m_labelFont);
        dfl.setFont(m_fieldFont);
        dfl.setUnderline(StyleConstants.NONE);
        ss.addForm(dfl);
        ss.addNewline(2);
    }

    private static final Integer ONE = new Integer(1);

    /**
     * Inner class to handle counting the number of jobs per activity <br>
     * 
     * @param
     * @return
     * @throws
     */
    class ActivityCountTableModel extends AbstractTableModel
    {
        private static final long serialVersionUID = 1232575590115333045L;

        private String[] m_columnNames;
        private Integer[] m_data;

        public ActivityCountTableModel(JobTableModel p_jtm, HashMap p_workflows)
        {
            HashMap map = new HashMap();
            String activity;
            for (int j = 0; j < p_jtm.getRowCount(); j++)
            {
                Long jobid = (Long) p_jtm.getValueAt(j, 0);
                WorkflowTableModel wtm = (WorkflowTableModel) p_workflows
                        .get(jobid);
                for (int w = 0; w < wtm.getRowCount(); w++)
                {
                    activity = (String) wtm.getValueAt(w,
                            WorkflowTableModel.CURACTIVITY);
                    if (map.containsKey(activity) == false)
                    {
                        map.put(activity, ONE); // first count
                    }
                    else
                    {
                        Integer oldValue = (Integer) map.get(activity);
                        Integer newValue = new Integer(oldValue.intValue() + 1);
                        map.put(activity, newValue);
                    }
                }

                m_columnNames = new String[map.size()];
                m_data = new Integer[map.size()];

                TreeSet activities = new TreeSet(map.keySet());
                Iterator iter = activities.iterator();
                int i = 0;
                Integer totalcount;
                while (iter.hasNext())
                {
                    activity = (String) iter.next();
                    totalcount = (Integer) map.get(activity);
                    m_columnNames[i] = activity;
                    m_data[i] = totalcount;
                    i++;
                }
            }
        }

        public Object getValueAt(int r, int c)
        {
            return m_data[c];
        }

        public int getRowCount()
        {
            return 1;
        }

        public int getColumnCount()
        {
            return m_columnNames.length;
        }

        public String getColumnName(int c)
        {
            return m_columnNames[c];
        }

        public void dump()
        {
            System.out
                    .println("--------------------------------DUMP-----------------");
            for (int c = 0; c < getColumnCount(); c++)
            {
                System.out.println(getColumnName(c) + " ");
            }

            for (int xr = 0; xr < getRowCount(); xr++)
            {
                for (int xc = 0; xc < getColumnCount(); xc++)
                {
                    System.out.println(getValueAt(xr, xc));
                }
            }
            System.out
                    .println("--------------------------------DUMP-----------------");
        }
    }
}
