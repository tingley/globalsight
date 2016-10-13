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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.ResourceBundle;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.globalsight.diplomat.util.database.ConnectionPool;
import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.costing.Currency;
import com.globalsight.everest.costing.CurrencyFormat;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.pagehandler.administration.users.UserUtil;
import com.globalsight.everest.workflowmanager.Workflow;
import com.globalsight.reports.Constants;
import com.globalsight.reports.JobTableModel;
import com.globalsight.reports.WorkflowTableModel;
import com.globalsight.reports.datawrap.WorkflowStatusReportDataWrap;
import com.globalsight.reports.util.ReportHandlerFactory;
import com.globalsight.reports.util.ReportsPackage;

/**
 * Presents a report on workflow status
 */
public class WorkflowStatusReportHandler extends BasicReportHandler
{
    private static final String MY_MESSAGES = BUNDLE_LOCATION
            + "workflowStatus";
    private static final String PM_QUERY = "select distinct(p.manager_user_id) "
            + "from project p, project_user pu "
            + "where p.project_seq = pu.project_id and pu.user_id = ? "
            + "order by manager_user_id";
//    private static final String PM_QUERY_GS = "select distinct(manager_user_id) "
//            + "from project order by manager_user_id";

    private static final String LABEL_SUFFIX = ": ";

    private static final int REPORTHEADHEIGHT = 130;
    private static final int FIXJOBHEIGHT = 270;
    private static final int EVERYROWHEIGHT = 65;
    protected ResourceBundle m_bundle = null;
    private LinkedHashMap wfStatesHashMap = null;
    private Currency m_currency = null;
    private WorkflowStatusReportDataWrap reportDataWrap = null;

    public WorkflowStatusReportHandler()
    {
        // empty constructor.
    }

    public void init()
    {
        try
        {
            super.init(); // get the common parameters
            m_bundle = ResourceBundle.getBundle(MY_MESSAGES, theUiLocale);
            this.reportKey = Constants.WORKFLOWSTATUS_REPORT_KEY;
        }
        catch (Exception e)
        {
            ReportsPackage.logError(e);
        }
    }

    /**
     * The entry of the handler. This method will dispatch the
     * HttpServletRequest to different page based on the value of
     * <code>Constants.REPORT_ACT</code>. <code>Constants.REPORT_ACT_PREP</code>
     * means this is request for prepare the parameter data, then system will
     * show the parameter page. <code>Constants.REPORT_ACT_CREATE</code> means
     * create the real report based on the user input data, then system will
     * show the report page.
     */
    public void invokeHandler(HttpServletRequest req, HttpServletResponse res,
            ServletContext p_context) throws Exception
    {
        super.setUseInContext(false);

        super.invokeHandler(req, res, p_context);
        String act = (String) req.getParameter(Constants.REPORT_ACT);
        s_category
                .debug("Perfoem WorkflowStatusReportHandler.invokeHandler with action "
                        + act);
        if (Constants.REPORT_ACT_PREP.equalsIgnoreCase(act))
        {
            addMoreReportParameters(req);
            dispatcherForward(
                    ReportHandlerFactory.getTargetUrl(reportKey
                            + Constants.REPORT_ACT_PREP), req, res, p_context);
        }
        else if (Constants.REPORT_ACT_CREATE.equalsIgnoreCase(act))
        {
            createReport(req);
            dispatcherForward(
                    ReportHandlerFactory.getTargetUrl(reportKey
                            + Constants.REPORT_ACT_CREATE), req, res, p_context);
        }
        else if (Constants.REPORT_ACT_TURNPAGE.equalsIgnoreCase(act))
        {
            String pageId = req.getParameter(Constants.REPORT_SHOWPAGE_PAGEID);
            bindData(req, pageId); // bind the data to one report page
            dispatcherForward(
                    ReportHandlerFactory.getTargetUrl(reportKey
                            + Constants.REPORT_ACT_CREATE), req, res, p_context);
        }
    }

    /**
     * Initializes additional report creation parameters.
     */
    private void addMoreReportParameters(HttpServletRequest req)
            throws Exception
    {
        addWorkflowStatusParameter(req);
        addProjectManagerParameter(req);
        if (isJobCostingOn())
        {
            addCurrencyParameter(m_bundle, req);
        }
    }

    /**
     * Initializes a parameter for workflow status. <br>
     */
    protected void addWorkflowStatusParameter(HttpServletRequest req)
    {
        wfStatesHashMap = new LinkedHashMap();
        wfStatesHashMap.put(PENDING,
                ReportsPackage.getMessage(m_bundle, PENDING));
        wfStatesHashMap.put(READY, ReportsPackage.getMessage(m_bundle, READY));
        wfStatesHashMap.put(DISPATCHED,
                ReportsPackage.getMessage(m_bundle, DISPATCHED));
        wfStatesHashMap.put(LOCALIZED,
                ReportsPackage.getMessage(m_bundle, LOCALIZED));
        wfStatesHashMap.put(EXPORTED,
                ReportsPackage.getMessage(m_bundle, EXPORTED));
        wfStatesHashMap.put(ARCHIVED,
                ReportsPackage.getMessage(m_bundle, ARCHIVED));
        req.setAttribute(Constants.WFSTATUS_ARRAY, wfStatesHashMap);
        req.setAttribute(Constants.WFSTATUS_LABEL,
                ReportsPackage.getMessage(m_bundle, Constants.WFSTATUS));
    }

    /**
     * Initializes the user ids of the project managers who actually have
     * projects. <br>
     */
    protected void addProjectManagerParameter(HttpServletRequest req)
            throws Exception
    {
        Connection c = null;
        PreparedStatement ps = null;
        ArrayList projectManagers = null;
        try
        {
            projectManagers = new ArrayList();
            c = ConnectionPool.getConnection();

            String currentId = CompanyThreadLocal.getInstance().getValue();
            String currentUserId = (String) req.getSession(false).getAttribute(
                    WebAppConstants.USER_NAME);
            
            ps = c.prepareStatement(PM_QUERY);
            ps.setString(1, currentUserId);

            ResultSet rs = ps.executeQuery();
            projectManagers.add(ReportsPackage.getMessage(m_bundle,
                    Constants.CRITERIA_ALLPMS));
            while (rs.next())
            {
                projectManagers.add(rs.getString(1));
            }
            ps.close();

            String defvalue = null;
            if (projectManagers.contains(theUsername))
            {
                defvalue = theUsername;
            }
            else
            {
                defvalue = (String) projectManagers.get(0);
            }

            req.setAttribute(Constants.PROJECT_MGR_ARRAY, projectManagers);
            req.setAttribute(Constants.PROJECT_MGR_DEFVALUE, defvalue);
            req.setAttribute(Constants.PROJECT_MGR_LABEL,
                    ReportsPackage.getMessage(m_bundle, Constants.PROJECT_MGR));
        }
        finally
        {
            this.closeStatement(ps);
            this.returnConnection(c);
        }
    }

    /**
     * Creates the actual report and fills it with data and messages. <br>
     * 
     * @param HttpServletRequest
     */
    public void createReport(HttpServletRequest req)
    {
        s_category.debug("Create WorkflowStatusReport now");
        try
        {
            reportDataWrap = new WorkflowStatusReportDataWrap();
            bindMessages();
            fillAllData(req);
            req.setAttribute(Constants.WORKFLOW_REPORT_DATA, reportDataWrap);
        }
        catch (Exception e)
        {
            ReportsPackage.logError(e);
        }
    }

    /**
     * Fills out the messages on the report <br>
     * 
     */
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
    public void fillAllData(HttpServletRequest req) throws Exception
    {
        if (isJobCostingOn())
        {
            String tmp = (String) req.getParameter(Constants.CURRENCY);
            m_currency = getCurrencyByDisplayname(tmp);
        }
        else
        {
            m_currency = null;
        }
        // add a form at the top to show the criteria that was selected for the
        // report
        String projectManager = (String) req
                .getParameter(Constants.PROJECT_MGR);
        String wfstate = (String) req.getParameter(Constants.WFSTATUS);
        addCriteriaFormAtTop(projectManager, wfstate);
        String userId = (String) req.getSession(false).getAttribute(
                WebAppConstants.USER_NAME);

        ArrayList<Job> jobs = null;
        if (projectManager.equals(ReportsPackage.getMessage(m_bundle,
                Constants.CRITERIA_ALLPMS)))
        {
            jobs = new ArrayList<Job>(ServerProxy.getJobHandler()
                    .getJobsByState(wfstate, userId));
        }
        else
        {
            jobs = new ArrayList<Job>(ServerProxy.getJobHandler()
                    .getJobsByManagerIdAndState(
                            UserUtil.getUserIdByName(projectManager), wfstate));
        }

        super.setUseInContext(jobs);

        // put all the job and workflow data into tables
        JobTableModel jtm = new JobTableModel(jobs, theUiLocale, m_currency);
        ArrayList<ArrayList<Workflow>> jobWorkflows = getAllWorkflowsForAllJobs((LinkedList<Long>) jtm
                .getJobIds());
        // now tell how many jobs there are
        reportDataWrap.setTotalJobNum(new Integer(jtm.getRowCount()));
        HashMap<Long, WorkflowTableModel> workflowTableModels = makeWorkflowTableModels(
                jtm, jobWorkflows, wfstate);

        int pageId = 1;
        int pageHeight = PAGE_CONTENT_HEIGTH_PX;
        reportDataWrap.setCurrentPageNum(pageId);
        reportDataWrap.setTotalPageNum(pageId);
        // now run through the sorted table and add a form at the top for each
        // job
        HashMap dataSourceHashMap = new HashMap();
        LinkedHashMap pageHashMap = new LinkedHashMap();
        for (int r = 0; r < jtm.getRowCount(); r++)
        {
            Long jobId = (Long) jtm.getValueAt(r, JobTableModel.ID);
            Job job = ServerProxy.getJobHandler().getJobById(jobId);
            WorkflowTableModel wtm = workflowTableModels.get(jobId);

            ArrayList jobForm = new ArrayList();
            addJobForm(jtm, r, jobForm);
            // only show jobs with workflows
            if (wtm.getRowCount() > 0)
            {
                // only show dispatched related columns if the workflow is
                // dispatched.
                // and only show cost if jobcosting is on.
                // int numCols = 6;
                int numCols = 10;
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
                // subcols[l++] = WorkflowTableModel.EXACT_WC;
                // subcols[l++] = WorkflowTableModel.FUZZY_WC;
                subcols[l++] = WorkflowTableModel.SEGMENT_TM_WC;
                if (PageHandler.isInContextMatch(job))
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
                {
                    subcols[l++] = WorkflowTableModel.DURATION;
                }
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
                jobForm.add(2, subcols);
                jobForm.add(3, wtm);

                if (r == 0)
                {
                    pageHeight -= REPORTHEADHEIGHT; // report header is 130px
                }

                // jobform and workflowmodule height
                int jobFormHeight = wtm.getRowCount() * EVERYROWHEIGHT
                        + FIXJOBHEIGHT;
                int temp = pageHeight;
                temp -= jobFormHeight;
                if (temp < 0)
                {
                    pageHeight = PAGE_CONTENT_HEIGTH_PX;
                    dataSourceHashMap.put(new Integer(pageId), pageHashMap);
                    pageHashMap = new LinkedHashMap();
                    pageId = pageId + 1;
                }

                pageHashMap.put(jobId, jobForm);
                pageHeight -= jobFormHeight;
            }
            else
            {
                reportDataWrap
                        .setTotalJobNum(reportDataWrap.getTotalJobNum() - 1);
            }
            jobForm = new ArrayList();
        }
        dataSourceHashMap.put(new Integer(pageId), pageHashMap);
        reportDataWrap.setTotalPageNum(pageId);
        // fill the first page data
        reportDataWrap.setJob2WorkFlowMap((LinkedHashMap) dataSourceHashMap
                .get(new Integer(1)));
        // save the first page data (public info)
        setSessionAttribute(theSession, Constants.WORKFLOW_REPORT_DATA,
                reportDataWrap);
        // save all pages data
        setSessionAttribute(theSession, Constants.WORKFLOW_REPORT_DATASOURCE,
                dataSourceHashMap);
        req.setAttribute(Constants.WORKFLOW_REPORT_DATA, reportDataWrap);
    }

    public void bindData(HttpServletRequest req, String pageId)
    {
        HashMap dataSourceHashMap = (HashMap) getSessionAttribute(theSession,
                Constants.WORKFLOW_REPORT_DATASOURCE);
        WorkflowStatusReportDataWrap reportDataWrap2 = (WorkflowStatusReportDataWrap) getSessionAttribute(
                theSession, Constants.WORKFLOW_REPORT_DATA);
        if (dataSourceHashMap == null || reportDataWrap2 == null)
        {
            return;
        }
        int curPageId = reportDataWrap2.getCurrentPageNum();
        int nextId = 1;
        try
        {
            nextId = Integer.parseInt(pageId);
        }
        catch (Exception exc)
        {
            nextId = curPageId;
        }
        if (nextId > reportDataWrap2.getTotalPageNum() || nextId <= 0)
        {
            nextId = curPageId;
        }
        LinkedHashMap linkedHashMap = (LinkedHashMap) dataSourceHashMap
                .get(new Integer(nextId));
        reportDataWrap2.setJob2WorkFlowMap(linkedHashMap);
        reportDataWrap2.setCurrentPageNum(nextId);
        req.setAttribute(Constants.WORKFLOW_REPORT_DATA, reportDataWrap2);
    }

    /**
     * Adds a form at the top with the selected PM and workflow state <br>
     * 
     * @param projectManager
     *            -- the selected PM
     * @param wfstate
     *            -- the selected WF state
     */
    private void addCriteriaFormAtTop(String projectManager, String wfstate)
    {
        ArrayList labels = new ArrayList();
        ArrayList fields = new ArrayList();
        labels.add(ReportsPackage.getMessage(m_bundle, Constants.WFSTATUS)
                + LABEL_SUFFIX);
        labels.add(ReportsPackage.getMessage(m_bundle, Constants.PROJECT_MGR)
                + LABEL_SUFFIX);

        fields.add(ReportsPackage.getMessage(m_bundle, wfstate));
        fields.add(projectManager);

        if (isJobCostingOn())
        {
            labels.add(ReportsPackage.getMessage(m_bundle, Constants.CURRENCY)
                    + LABEL_SUFFIX);
            fields.add(m_currency.getDisplayName());
        }
        reportDataWrap.setCriteriaFormLabel(labels);
        reportDataWrap.setCriteriaFormValue(fields);
    }

    /**
     * Adds a form to the report containing the job information <br>
     * 
     * @param p_jtm
     *            -- the JobTableModel
     * @param r
     *            -- the current row
     * @param jobForm
     *            -- the current jobForm
     */
    private void addJobForm(JobTableModel p_jtm, int r, ArrayList jobForm)
    {
        ArrayList labels = new ArrayList();
        ArrayList fields = new ArrayList();
        String origLabel = (String) p_jtm.getColumnName(JobTableModel.JOBNAME);
        String newLabel = origLabel + LABEL_SUFFIX;
        labels.add(newLabel);
        fields.add(p_jtm.getValueAt(r, JobTableModel.JOBNAME));
        // set the other fields and labels
        Object field = null;
        for (int c = 1; c < p_jtm.getColumnCount(); c++)
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
                double cost = ((Float) p_jtm.getValueAt(r, c)).doubleValue();
                String formattedCost = CurrencyFormat.getCurrencyFormat(
                        m_currency).format(cost);
                field = (Object) formattedCost;
            }
            else
            {
                field = p_jtm.getValueAt(r, c);
            }
            origLabel = (String) p_jtm.getColumnName(c);
            labels.add(origLabel + LABEL_SUFFIX);
            fields.add(field);
        }

        jobForm.add(0, labels);
        jobForm.add(1, fields);
    }

    /**
     * Returns an array list containing each job's workflows <br>
     * 
     * @param p_jobs
     *            -- ArrayList of jobids
     */
    private ArrayList<ArrayList<Workflow>> getAllWorkflowsForAllJobs(
            LinkedList<Long> p_jobs)
    {
        // contains an array list for each job
        ArrayList<ArrayList<Workflow>> jobWorkflows = new ArrayList<ArrayList<Workflow>>();
        for (int i = 0; i < p_jobs.size(); i++)
        {
            Long jobId = (Long) p_jobs.get(i);
            ArrayList<Workflow> workflows = null; // the workflows for this job
            try
            {
                workflows = new ArrayList<Workflow>(ServerProxy
                        .getJobReportingManager().getWorkflowsByJobId(
                                jobId.longValue()));
            }
            catch (Exception e)
            {
                workflows = new ArrayList<Workflow>();
                ReportsPackage.logError(e);
            }

            jobWorkflows.add(workflows);
        }

        return jobWorkflows;
    }

    /**
     * Returns an HashMap containing workflowtablemodels for each job <br>
     * 
     * @param p_jtm
     * @param p_jobWorkflows
     *            -- an arraylist of arraylists for each job containing the
     *            workflows
     * @param p_wfstate
     * @return HashMap of WorkflowTableModels
     */
    private HashMap<Long, WorkflowTableModel> makeWorkflowTableModels(
            JobTableModel p_jtm, ArrayList<ArrayList<Workflow>> p_jobWorkflows,
            String p_wfstate)
    {
        HashMap<Long, WorkflowTableModel> wtms = new HashMap<Long, WorkflowTableModel>();
        for (int i = 0; i < p_jobWorkflows.size(); i++)
        {
            ArrayList<Workflow> workflows = p_jobWorkflows.get(i);
            WorkflowTableModel wtm = new WorkflowTableModel(workflows,
                    p_wfstate, theSession, m_currency, false);

            wtm.setUseInContext(super.isUseInContext());
            Long jobid = (Long) p_jtm.getValueAt(i, 0);
            wtm.fillAllData(p_wfstate, workflows);
            wtms.put(jobid, wtm);
        }

        return wtms;
    }
}
