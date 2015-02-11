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
import inetsoft.report.StyleConstants;
import inetsoft.report.StyleSheet;
import inetsoft.report.TextElement;
import inetsoft.report.filter.AverageFormula;
import inetsoft.report.filter.DefaultSortedTable;
import inetsoft.report.filter.GroupFilter;
import inetsoft.report.filter.SortFilter;
import inetsoft.report.lens.DefaultFormLens;
import inetsoft.report.lens.SubTableLens;
import inetsoft.report.lens.TableChartLens;
import inetsoft.report.lens.swing.TableModelLens;
import inetsoft.report.style.Professional;
import inetsoft.sree.RepletException;
import inetsoft.sree.RepletRequest;

import java.awt.Font;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.ResourceBundle;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;

import com.globalsight.diplomat.util.database.ConnectionPool;
import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.persistence.PersistenceException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.usermgr.UserLdapHelper;
import com.globalsight.reports.handler.BasicReportHandler;
import com.globalsight.reports.util.ReportsPackage;

/**
 * Presents a report on the average percent completion by target locale for all
 * workflows for jobs that are DISPATCHED.
 */
public class AvgPercentCompletion extends GlobalSightReplet
{
    private static final String MY_TEMPLATE = "/templates/basicFlowReport.srt";
    private static final String MY_MESSAGES = BasicReportHandler.BUNDLE_LOCATION
            + "avgPercentCompletion";

    private static final String LABEL_SUFFIX = ": ";
    protected ResourceBundle m_bundle = null;
    private StyleSheet ss = null;
    private Font m_labelFont = GlobalSightReplet.FONT_LABEL;
    private Font m_fieldFont = GlobalSightReplet.FONT_NORMAL;

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

    public AvgPercentCompletion()
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
        addProjectManagerParameter();
        addRepletParameters(theParameters);
    }

    /**
     * Adds the user ids of the project managers who actually have projects as
     * replet parameters <br>
     */
    protected void addProjectManagerParameter() throws Exception
    {
        Connection c = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try
        {
            ArrayList projectManagers = new ArrayList();
            c = ConnectionPool.getConnection();
            ps = c.prepareStatement(getPmQuery());
            rs = ps.executeQuery();
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
            if (rs != null)
            {
                try
                {
                    rs.close();
                }
                catch (Exception e)
                {
                }
            }
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
            ReportsPackage.logError("Problem creating report", e);
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
        // add a form at the top to show the criteria that was selected for the
        // report
        String projectManager = req.getString("projectMgr");
        System.out.println("projectMgr:" + projectManager);
        addCriteriaFormAtTop(projectManager);

        HttpServletRequest http_request = (HttpServletRequest) req
                .getParameter(RepletRequest.HTTP_REQUEST);
        theSession = http_request.getSession(false);
        String sessionId = theSession.getId();

        String companyId = CompanyThreadLocal.getInstance().getValue();
        if (companyId == null)
        {
            String companyName = (String) theSession
                    .getAttribute(UserLdapHelper.LDAP_ATTR_COMPANY);
            CompanyThreadLocal.getInstance().setValue(companyName);
        }
        // get all localized and dispatched jobs
        ArrayList jobs = null;
        if (projectManager.equals(ReportsPackage.getMessage(m_bundle,
                "criteria_allPms")))
        {
            jobs = new ArrayList(ServerProxy.getJobHandler().getJobsByState(
                    DISPATCHED));
            jobs.addAll(ServerProxy.getJobHandler().getJobsByState(LOCALIZED));
        }
        else
        {
            jobs = new ArrayList(ServerProxy.getJobHandler()
                    .getJobsByManagerIdAndState(projectManager, DISPATCHED));
            jobs.addAll(ServerProxy.getJobHandler().getJobsByManagerIdAndState(
                    projectManager, LOCALIZED));
        }

        // put all the job and workflow data into tables
        ArrayList jobWorkflows = getAllWorkflowsForAllJobs(jobs);
        JobTableModel jtm = new JobTableModel(jobs, theUiLocale, null);
        HashMap workflowTableModels = makeWorkflowTableModels(jtm, jobWorkflows);

        // add a chart showing how many jobs are in each activity if we're
        // showing jobs in progress
        showAvgPercentCompletionChart(jtm, workflowTableModels);

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
            Long jobId = (Long) dst.getObject(r + 1, 0); // ignore header row
            WorkflowTableModel wtm = (WorkflowTableModel) workflowTableModels
                    .get(jobId);

            // only show jobs with workflows
            if (wtm.getRowCount() > 0)
            {
                TableModelLens model = new TableModelLens(wtm);
                model.setColAutoSize(true);

                int subcols[] = new int[]
                { WorkflowTableModel.TRGLOCALE, WorkflowTableModel.PER_COMPLETE };
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
    private void showAvgPercentCompletionChart(JobTableModel p_jtm,
            HashMap p_workflowTableModels)
    {
        // run through the jobs and workflows and create a table containing the
        // target locales
        // and percent completion
        PercentCompletionTable pct = new PercentCompletionTable(p_jtm,
                p_workflowTableModels);
        TableModelLens tml = new TableModelLens(pct);
        SortFilter sf = new SortFilter(tml, new int[]
        { 0 }, true);
        GroupFilter gf = new GroupFilter(sf, 1, new AverageFormula(),
                new AverageFormula());
        gf.setGrandLabel(ReportsPackage.getMessage(m_bundle, "allLocales"));
        fullyLoadTable(gf);

        // create a table with one row with all the data in it (one dataset)
        ArrayList rowData = new ArrayList();
        ArrayList colNames = new ArrayList();
        int numCols = 0;
        for (int r = 0; r < gf.getRowCount(); r++)
        {
            if (gf.isSummaryRow(r) && !gf.isGroupHeaderRow(r))
            {
                Object targetLocale = gf.getObject(r, 0);
                Object perComp = gf.getObject(r, 1);
                colNames.add(targetLocale);
                rowData.add(perComp);
                numCols++;
            }
        }
        DefaultTableModel dtm = new DefaultTableModel(1, numCols);
        for (int i = 0; i < rowData.size(); i++)
            dtm.setValueAt(rowData.get(i), 0, i);
        dtm.setColumnIdentifiers(colNames.toArray());

        TableChartLens tcl = new TableChartLens(new TableModelLens(dtm));
        tcl.setYTitle(ReportsPackage.getMessage(m_bundle, "chartY"));
        tcl.setXTitle(ReportsPackage.getMessage(m_bundle, "chartX"));
        tcl.setStyle(StyleConstants.CHART_BAR);
        tcl.setTitleFont(GlobalSightReplet.FONT_CHART_TITLE);
        tcl.setMaximum(new Integer(100));
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
            ArrayList p_jobWorkflows)
    {
        HashMap wtms = new HashMap();
        for (int i = 0; i < p_jobWorkflows.size(); i++)
        {
            ArrayList workflows = (ArrayList) p_jobWorkflows.get(i);
            WorkflowTableModel wtm = new WorkflowTableModel(workflows,
                    theSession, null);
            Long jobid = (Long) p_jtm.getValueAt(i, 0);
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
        ArrayList jobWorkflows = new ArrayList(); // contains an array list for
                                                  // each job
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
                ReportsPackage.logError("Problem getting workflows", e);
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
     */
    private void addCriteriaFormAtTop(String projectManager)
    {
        String wfstatusLabel = ReportsPackage.getMessage(m_bundle, "wfstatus");
        String labels[] = new String[]
        {
                wfstatusLabel + LABEL_SUFFIX,
                ReportsPackage.getMessage(m_bundle, "projectMgr")
                        + LABEL_SUFFIX };
        String wfstate = ReportsPackage.getMessage(m_bundle, DISPATCHED) + ","
                + ReportsPackage.getMessage(m_bundle, LOCALIZED);
        String fields[] = new String[]
        { wfstate, projectManager };
        DefaultFormLens dfl = new DefaultFormLens(labels, fields);
        dfl.setLabelFont(m_labelFont);
        dfl.setFont(m_fieldFont);
        dfl.setUnderline(StyleConstants.NONE);
        ss.addForm(dfl);
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
        String origLabel = (String) p_dst.getObject(0, JobTableModel.JOBNAME);
        String newLabel = origLabel + LABEL_SUFFIX;
        labels.add(newLabel);
        fields.add(p_dst.getObject(r + 1, JobTableModel.JOBNAME)); /*
                                                                    * ignore the
                                                                    * header row
                                                                    */

        // set the other fields and labels
        Object field = null;
        for (int c = 1; c < p_dst.getColCount(); c++)
        {
            if (c == JobTableModel.JOBNAME || c == JobTableModel.JOB_OBJECT
                    || c == JobTableModel.COST || c == JobTableModel.REVENUE)
            {
                // don't want jobname since it's in the criteria
                // don't want job object
                // don't want cost
                // don't want revenue either
                continue;
            }

            field = p_dst.getObject(r + 1, c);
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
        ss.addNewline(1);
    }

    /**
     * Inner class to handle keeping track of the percent completion by workflow <br>
     */
    class PercentCompletionTable extends AbstractTableModel
    {
        private String[] m_columnNames;
        private ArrayList m_dataRows;

        public PercentCompletionTable(JobTableModel p_jtm, HashMap p_workflows)
        {
            m_columnNames = new String[]
            { ReportsPackage.getMessage(m_bundle, "avgTableLocaleCol"),
                    ReportsPackage.getMessage(m_bundle, "avgTableCompleteCol"), };

            m_dataRows = new ArrayList();

            for (int j = 0; j < p_jtm.getRowCount(); j++)
            {
                Long jobid = (Long) p_jtm.getValueAt(j, 0);
                WorkflowTableModel wtm = (WorkflowTableModel) p_workflows
                        .get(jobid);
                for (int w = 0; w < wtm.getRowCount(); w++)
                {
                    Object targetLocale = wtm.getValueAt(w,
                            WorkflowTableModel.TRGLOCALE);
                    Object percentComplete = wtm.getValueAt(w,
                            WorkflowTableModel.PER_COMPLETE);
                    Object thiscolumn[] =
                    { targetLocale, percentComplete };
                    m_dataRows.add(thiscolumn);
                }
            }
        }

        public Object getValueAt(int r, int c)
        {
            Object[] column = (Object[]) m_dataRows.get(r);
            return column[c];
        }

        public int getRowCount()
        {
            return m_dataRows.size();
        }

        public int getColumnCount()
        {
            return 2;
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
