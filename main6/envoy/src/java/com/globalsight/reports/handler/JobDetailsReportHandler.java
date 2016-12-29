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
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Vector;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import com.globalsight.diplomat.util.database.ConnectionPool;
import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.costing.Currency;
import com.globalsight.everest.costing.CurrencyFormat;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.page.SourcePage;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.taskmanager.Task;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.tags.TableConstants;
import com.globalsight.everest.workflow.WfTaskInfo;
import com.globalsight.everest.workflow.WorkflowConstants;
import com.globalsight.everest.workflow.WorkflowInstance;
import com.globalsight.everest.workflow.WorkflowTaskInstance;
import com.globalsight.everest.workflowmanager.Workflow;
import com.globalsight.reports.Constants;
import com.globalsight.reports.JobTableModel;
import com.globalsight.reports.WorkflowTableModel;
import com.globalsight.reports.datawrap.JobDetailsReportDataWrap;
import com.globalsight.reports.util.LabeledValueHolder;
import com.globalsight.reports.util.LabeledValueHolderComparator;
import com.globalsight.reports.util.ReportHandlerFactory;
import com.globalsight.reports.util.ReportsPackage;
import com.globalsight.util.SortUtil;
import com.globalsight.util.date.DateHelper;

public class JobDetailsReportHandler extends BasicReportHandler
{
    private static final String MY_MESSAGES = BUNDLE_LOCATION + "jobDetails";

    private static final String JOB_QUERY = "select job.id, job.name, job.state from job, l10n_profile lp, project_user pu "
            + "where job.l10n_profile_id = lp.id and lp.project_id = pu.project_id and pu.user_id = ? and (job.state='LOCALIZED' or job.state='EXPORTED' "
            + "or job.state='DISPATCHED' or job.state='ARCHIVED') order by job.id";

    private static final String JOB_QUERY_GS = "select job.id, job.name, job.state from job "
            + "where job.state='LOCALIZED' or job.state='EXPORTED' "
            + "or job.state='DISPATCHED' or job.state='ARCHIVED' order by job.id";

    private static final String LABEL_SUFFIX = ": ";
    private static final String JOB_DETAILS_REPORT_TITLE = "txtReportTitle";
    private static final String DISPATCHED_JOBS_MSG = "dispatchedJobs";
    private static final String LOCALIZED_JOBS_MSG = "localizedJobs";
    private static final String EXPORTED_JOBS_MSG = "exportedJobs";
    private static final String ARCHIVED_JOBS_MSG = "archivedJobs";
    private static final String JOBS_STATUS_MSG = "jobstatus";
    private static final String JOB_NAME_MSG = "jobname";
    private static final String CURRENCY_MSG = "currency";
    private static final String LOCAL_PROFILE_MSG = "locProfile";
    private static final String ALL_WORK_FLOWS_MSG = "allWorkflows";
    private static final String HIDDEN_ACTIVITY_ID = "hiddenActivityId";
    private static final String ACTIVITY_NAME_MSG = "activityName";
    private static final String ACCEPTER_MSG = "accepter";
    private static final String ASSIGNEE_ROLE_MSG = "assigneeRole";
    private static final String DURATION_MSG = "duration";
    private static final String COMPLETE_DATE_MSG = "compDate";
    private static final String ACTIVITY_BREAKDOWN_MSG = "activityBreakdown";
    private static final String NOT_ACCEPTED_YET_MSG = "notAcceptedYet";
    private static final String LB_ABBREVIATION_DAY_MSG = "lb_abbreviation_day";
    private static final String LB_ABBREVIATION_HOUR_MSG = "lb_abbreviation_hour";
    private static final String LB_ABBREVIATION_MINUTE_MSG = "lb_abbreviation_minute";
    private static final String PAGES_IN_JOB_MSG = "pagesInJob";
    private static final String NO_DATE_OR_TIME = "--";
    private static final String LEFT_PARENTHESES = " (";
    private static final String RIGHT_PARENTHESES = ")";
    private static final String PURE_NUM_REGEX = "[0-9]+";

    private Currency m_currency = null;
    private JobDetailsReportDataWrap reportDataWrap = null;
    protected ResourceBundle m_bundle = null;

    private List<LabeledValueHolder> m_dispatchedJobs = null;
    private List<LabeledValueHolder> m_localizedJobs = null;
    private List<LabeledValueHolder> m_exportedJobs = null;
    private List<LabeledValueHolder> m_archivedJobs = null;

    private HashMap jobstatus_map = null;
    private HashMap<String, String> jobsLists_map = null;

    // values for accessing completed activity information
    private static final int CA_TASK_USER = 2;

    // for pagination
    private static final int CRITERIA_FORM_HIGH_PX = 125;
    private static final int ARRAYLIST_ROW_HIGH_PX = 16;
    private static final int ACTIVITY_TABLE_ROW_HIGH_PX = 72;
    private static final int ACTIVITY_TABLE_TITLE_HIGH_PX = 20;
    private static final int ACTIVITY_TABLE_LINE_HIGH_PX = 35;
    private static final int PAGENAME_TABLE_ROW_HIGH_PX = 16;
    private static final int PAGENAME_TABLE_LABEL_HIGH_PX = 20;

    private int pageRestHighPx = -1;

    public JobDetailsReportHandler()
    {
        // empty constructor.
    }

    public void init()
    {
        try
        {
            super.init(); // get the common parameters
            m_bundle = ResourceBundle.getBundle(MY_MESSAGES, theUiLocale);
            this.reportKey = Constants.JOBDETAILS_REPORT_KEY;
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
                .debug("Perfoem JobDetailsReportHandler.invokeHandler with action "
                        + act);
        if (Constants.REPORT_ACT_PREP.equalsIgnoreCase(act))
        {
            cleanSession(theSession);
            addMoreReportParameters(req);
            dispatcherForward(
                    ReportHandlerFactory.getTargetUrl(reportKey
                            + Constants.REPORT_ACT_PREP), req, res, p_context);
        }
        else if (Constants.REPORT_ACT_CREATE.equalsIgnoreCase(act))
        {
            super.setUseInContext(false);
            // the web page is first created .
            createReport(req);
            dispatcherForward(
                    ReportHandlerFactory.getTargetUrl(reportKey
                            + Constants.REPORT_ACT_CREATE), req, res, p_context);
        }
        else if (Constants.REPORT_ACT_TURNPAGE.equalsIgnoreCase(act))
        {
            // the web page was turn to other page .
            reportDataWrap = (JobDetailsReportDataWrap) getSessionAttribute(
                    theSession, reportKey + Constants.REPORT_DATA_WRAP);
            // do pagenation function .
            String pageStr = (String) req.getParameter(reportKey
                    + TableConstants.PAGE_NUM);
            int pageNum = 1;
            if (pageStr != null)
            {
                if (pageStr.matches(PURE_NUM_REGEX))
                {
                    try
                    {
                        pageNum = Integer.parseInt(pageStr);
                    }
                    catch (Exception ex)
                    {
                        pageNum = reportDataWrap.getCurrentPageNum();
                    }
                }
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

            dispatcherForward(
                    ReportHandlerFactory.getTargetUrl(reportKey
                            + Constants.REPORT_ACT_CREATE), req, res, p_context);
        }
    }

    /**
     * Adds additional report creation parameters to the report parameters
     */
    private void addMoreReportParameters(HttpServletRequest req)
            throws Exception
    {
        queryAllJobs(req);
        addJobStatusParameter(req);
        addJobListParameters(req);
        if (isJobCostingOn())
        {
            addCurrencyParameter(m_bundle, req);
        }
    }

    /**
     * Queries for all job ids,names,and states for (D,L,E,A) jobs <br>
     * 
     * @throws Exception
     */
    private void queryAllJobs(HttpServletRequest req) throws Exception
    {
        String userId = (String) req.getSession(false).getAttribute(WebAppConstants.USER_NAME);
        m_dispatchedJobs = new ArrayList<LabeledValueHolder>();
        m_localizedJobs = new ArrayList<LabeledValueHolder>();
        m_exportedJobs = new ArrayList<LabeledValueHolder>();
        m_archivedJobs = new ArrayList<LabeledValueHolder>();

        Connection c = null;
        PreparedStatement ps = null;

        try
        {
            c = ConnectionPool.getConnection();

            String currentId = CompanyThreadLocal.getInstance().getValue();
            
            ps = c.prepareStatement(JOB_QUERY);
            ps.setString(1, userId);

            ResultSet rs = ps.executeQuery();
            while (rs.next())
            {
                Long jobid = new Long(rs.getLong(1));
                String jobname = rs.getString(2);
                String state = rs.getString(3);
                StringBuffer displayJobName = new StringBuffer(jobname);
                displayJobName.append(LEFT_PARENTHESES)
                        .append(jobid.toString()).append(RIGHT_PARENTHESES);
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

            SortUtil.sort(m_dispatchedJobs, new LabeledValueHolderComparator(
                    Locale.getDefault()));
            SortUtil.sort(m_localizedJobs, new LabeledValueHolderComparator(
                    Locale.getDefault()));
            SortUtil.sort(m_exportedJobs, new LabeledValueHolderComparator(
                    Locale.getDefault()));
            SortUtil.sort(m_archivedJobs, new LabeledValueHolderComparator(
                    Locale.getDefault()));
            ps.close();
        }
        finally
        {
            this.closeStatement(ps);
            this.returnConnection(c);
        }
    }

    /**
     * Adds a radio button parameter for job status to the report parameters so
     * that we know which job list they selected from <br>
     */
    @SuppressWarnings("unchecked")
    protected void addJobStatusParameter(HttpServletRequest req)
    {
        Object dispatched = (Object) new LabeledValueHolder(DISPATCHED,
                ReportsPackage.getMessage(m_bundle, DISPATCHED));
        Object localized = (Object) new LabeledValueHolder(LOCALIZED,
                ReportsPackage.getMessage(m_bundle, LOCALIZED));
        Object exported = (Object) new LabeledValueHolder(EXPORTED,
                ReportsPackage.getMessage(m_bundle, EXPORTED));
        Object archived = (Object) new LabeledValueHolder(ARCHIVED,
                ReportsPackage.getMessage(m_bundle, ARCHIVED));
        List<Object> states = new ArrayList<Object>();
        jobstatus_map = new HashMap();
        HashMap jobRadioLabelMap = new HashMap();
        HashMap jobSelectLabelMap = new HashMap();
        HashMap jobSelectNameMap = new HashMap();
        if (m_dispatchedJobs.size() > 0)
        {
            states.add(dispatched);
            jobRadioLabelMap.put(DISPATCHED,
                    ReportsPackage.getMessage(m_bundle, DISPATCHED));
            jobSelectLabelMap.put(DISPATCHED,
                    ReportsPackage.getMessage(m_bundle, DISPATCHED_JOBS_MSG));
            jobSelectNameMap.put(DISPATCHED, DISPATCHED_JOBS_MSG);
            jobstatus_map.put(DISPATCHED, dispatched);
        }

        if (m_localizedJobs.size() > 0)
        {
            states.add(localized);
            jobRadioLabelMap.put(LOCALIZED,
                    ReportsPackage.getMessage(m_bundle, LOCALIZED));
            jobSelectLabelMap.put(LOCALIZED,
                    ReportsPackage.getMessage(m_bundle, LOCALIZED_JOBS_MSG));
            jobSelectNameMap.put(LOCALIZED, LOCALIZED_JOBS_MSG);
            jobstatus_map.put(LOCALIZED, localized);
        }

        if (m_exportedJobs.size() > 0)
        {
            states.add(exported);
            jobRadioLabelMap.put(EXPORTED,
                    ReportsPackage.getMessage(m_bundle, EXPORTED));
            jobSelectLabelMap.put(EXPORTED,
                    ReportsPackage.getMessage(m_bundle, EXPORTED_JOBS_MSG));
            jobSelectNameMap.put(EXPORTED, EXPORTED_JOBS_MSG);
            jobstatus_map.put(EXPORTED, exported);
        }

        if (m_archivedJobs.size() > 0)
        {
            states.add(archived);
            jobRadioLabelMap.put(ARCHIVED,
                    ReportsPackage.getMessage(m_bundle, ARCHIVED));
            jobSelectLabelMap.put(ARCHIVED,
                    ReportsPackage.getMessage(m_bundle, ARCHIVED_JOBS_MSG));
            jobSelectNameMap.put(ARCHIVED, ARCHIVED_JOBS_MSG);
            jobstatus_map.put(ARCHIVED, archived);
        }

        if (states.size() > 0)
        {
            // Get "job status" and add "Group Radio" to show them .
            req.setAttribute(Constants.JOB_RADIO_LABEL_MAP, jobRadioLabelMap);
            req.setAttribute(Constants.JOB_SELECT_LABEL_MAP, jobSelectLabelMap);
            req.setAttribute(Constants.JOB_SELECT_NAME_MAP, jobSelectNameMap);

            req.setAttribute(Constants.JOB_STATUS_LABEL,
                    ReportsPackage.getMessage(m_bundle, JOBS_STATUS_MSG));

            // Set the "jobstatu_map" HashMap into "SESSEION"
            // in order to get the Object in the next page for different
            // instances.
            // for radio status of parameter page
            // theSession.setAttribute(Constants.JOB_STATUS_SESSION_MAP,jobstatus_map);
            this.setSessionAttribute(theSession,
                    Constants.JOB_STATUS_SESSION_MAP, jobstatus_map);
        }
    }

    /**
     * Adds a choice list of all the jobs in the given state as a report
     * parameter. Adds four lists as report parameters
     * (dispatched,localized,exported,archived)
     */
    private void addJobListParameters(HttpServletRequest req)
    {
        jobsLists_map = new HashMap<String, String>();
        LabeledValueHolder currentLvh = null;
        if (m_dispatchedJobs.size() > 0)
        {
            // show this "dispatched" job in the list .
            ArrayList<String> dispatchedJobsId = new ArrayList<String>();

            for (Iterator<?> iter = m_dispatchedJobs.iterator(); iter.hasNext();)
            {
                currentLvh = (LabeledValueHolder) iter.next();
                String jobDisplayName = currentLvh.getLabel();
                jobDisplayName = jobDisplayName.substring(0,
                        jobDisplayName.lastIndexOf(LEFT_PARENTHESES));
                jobsLists_map.put(currentLvh.getValue().toString(),
                        jobDisplayName);
                // job id list for getting job name from JOB_LIST_MAP
                dispatchedJobsId.add(currentLvh.getValue().toString());
            }

            req.setAttribute(Constants.DISPATCHED_JOBID_ARRAY, dispatchedJobsId);
        }

        if (m_localizedJobs.size() > 0)
        {
            ArrayList<String> localizedJobsId = new ArrayList<String>();
            for (Iterator<?> iter = m_localizedJobs.iterator(); iter.hasNext();)
            {
                currentLvh = (LabeledValueHolder) iter.next();
                String jobDisplayName = currentLvh.getLabel();
                jobDisplayName = jobDisplayName.substring(0,
                        jobDisplayName.lastIndexOf(" ("));
                jobsLists_map.put(currentLvh.getValue().toString(),
                        jobDisplayName);
                localizedJobsId.add(currentLvh.getValue().toString());
            }

            req.setAttribute(Constants.LOCALIZED_JOBID_ARRAY, localizedJobsId);
        }

        if (m_exportedJobs.size() > 0)
        {
            ArrayList<String> exportedJobsId = new ArrayList<String>();
            for (Iterator<?> iter = m_exportedJobs.iterator(); iter.hasNext();)
            {
                currentLvh = (LabeledValueHolder) iter.next();
                String jobDisplayName = currentLvh.getLabel();
                jobDisplayName = jobDisplayName.substring(0,
                        jobDisplayName.lastIndexOf(LEFT_PARENTHESES));
                jobsLists_map.put(currentLvh.getValue().toString(),
                        jobDisplayName);
                exportedJobsId.add(currentLvh.getValue().toString());
            }

            req.setAttribute(Constants.EXPORTED_JOBID_ARRAY, exportedJobsId);
        }

        if (m_archivedJobs.size() > 0)
        {
            ArrayList<String> archivedJobsId = new ArrayList<String>();
            for (Iterator<?> iter = m_archivedJobs.iterator(); iter.hasNext();)
            {
                currentLvh = (LabeledValueHolder) iter.next();
                String jobDisplayName = currentLvh.getLabel();
                jobDisplayName = jobDisplayName.substring(0,
                        jobDisplayName.lastIndexOf(" ("));
                jobsLists_map.put(currentLvh.getValue().toString(),
                        jobDisplayName);
                archivedJobsId.add(currentLvh.getValue().toString());
            }

            req.setAttribute(Constants.ARCHIVED_JOBID_ARRAY, archivedJobsId);
        }

        // Set the "jobslists_map" HashMap into "SESSEION"
        // in order to get the Object in the next page for different instances.
        // theSession.setAttribute(Constants.JOB_LIST_MAP,jobsLists_map);
        this.setSessionAttribute(theSession, Constants.JOB_LIST_MAP,
                jobsLists_map);
    }

    /**
     * Creates the actual report and fills it with data and messages. <br>
     * 
     * @param HttpServletRequest
     */
    public void createReport(HttpServletRequest req)
    {
        try
        {
            // define a kind of data class like WorkflowStatusReportDataWrap to
            // store data.
            reportDataWrap = new JobDetailsReportDataWrap();
            bindMessages();
            bindData(req);
            reportDataWrap.setCurrentPageNum(Constants.FIRST_PAGE_NUM);
            List<?> tmpList = (List<?>) reportDataWrap.getDataList();
            int totalPageNum = 1;
            if (tmpList != null)
            {
                totalPageNum = tmpList.size();
            }
            if (totalPageNum < 1)
            {
                ReportsPackage
                        .logError("Wrong : totalPageNum is less then '1'!\r\n TotalPageNum is set to '1' now.");
                totalPageNum = 1;
            }

            reportDataWrap.setTotalPageNum(totalPageNum);
            // set data to sessionManager atrribute here !
            setSessionAttribute(theSession, reportKey
                    + Constants.REPORT_DATA_WRAP, reportDataWrap);
        }
        catch (Exception e)
        {
            ReportsPackage.logError(e);
        }
    }

    /**
     * Fills out the messages on the report
     */
    private void bindMessages()
    {
        reportDataWrap.setReportTitle(ReportsPackage.getMessage(m_bundle,
                JOB_DETAILS_REPORT_TITLE));
        setCommonMessages(reportDataWrap);
    }

    /**
     * Gets the data from the DB and binds it to tables <br>
     * 
     * @param HttpServletRequest
     * @throws Exception
     */
    public void bindData(HttpServletRequest req) throws Exception
    {
        // add a form at the top to show the criteria that was selected for the
        // report
        HashMap<?, ?> jobstatus_hashmap = (HashMap<?, ?>) this
                .getSessionAttribute(theSession,
                        Constants.JOB_STATUS_SESSION_MAP);
        if (jobstatus_hashmap == null)
        {
            jobstatus_hashmap = new HashMap<Object, Object>();
        }
        // get "jobstatus" from radio of parameter page.
        String jobstatusReq = req.getParameter(JOBS_STATUS_MSG);
        LabeledValueHolder chosenStateHolder = (LabeledValueHolder) jobstatus_hashmap
                .get(jobstatusReq);

        if (chosenStateHolder == null)
        {
            // there are no jobs!
            return;
        }

        if (isJobCostingOn())
        {
            String chosenCurrency = (String) req
                    .getParameter(Constants.CURRENCY);
            m_currency = (Currency) getCurrencyByDisplayname(chosenCurrency);
        }
        else
            m_currency = null;

        String chosenState = (String) chosenStateHolder.getValue();
        String jobSelectedId = null;
        HashMap<?, ?> jobslists_map = (HashMap<?, ?>) this.getSessionAttribute(
                theSession, Constants.JOB_LIST_MAP);
        if (chosenState.equals(DISPATCHED))
        {
            // get "dispatchedJobs" from selection of parameter page
            jobSelectedId = req.getParameter(DISPATCHED_JOBS_MSG);
        }
        else if (chosenState.equals(LOCALIZED))
        {
            jobSelectedId = req.getParameter(LOCALIZED_JOBS_MSG);
        }
        else if (chosenState.equals(EXPORTED))
        {
            jobSelectedId = req.getParameter(EXPORTED_JOBS_MSG);
        }
        else if (chosenState.equals(ARCHIVED))
        {
            jobSelectedId = req.getParameter(ARCHIVED_JOBS_MSG);
        }

        String jobName = (String) jobslists_map.get(jobSelectedId);
        Long jobId = new Long(Long.parseLong(jobSelectedId));

        addCriteriaFormAtTop((String) chosenStateHolder.getLabel(), jobName);

        Job job = ServerProxy.getJobHandler().getJobById(jobId.longValue());

        super.setUseInContext(job);

        Collection<Workflow> c = ServerProxy.getJobReportingManager()
                .getWorkflowsByJobId(job.getId());
        ArrayList<Workflow> workflows = new ArrayList<Workflow>(c);
        WorkflowTableModel wtm = new WorkflowTableModel(workflows, theSession,
                m_currency, false);
        wtm.setUseInContext(super.isUseInContext());
        wtm.fillAllData(null, workflows);

        List<Job> jobs = new ArrayList<Job>();
        jobs.add(job);
        JobTableModel jtm = new JobTableModel(jobs, theUiLocale, m_currency);
        // the first table !
        addJobForm(jtm, 0, job);

        // the second table !
        addWorkflowSummaryTable(wtm);
        // the third table !
        addDetailedWorkflowBreakdownTables(jobId.longValue(), wtm, workflows);
        // the forth table !
        addTableWithPageNames(job);
    }

    /**
     * Adds a form at the top with the selected job state and job name <br>
     * 
     * @param p_jobstate
     *            -- the selected job status
     * @param p_jobname
     *            -- the selected job name
     */
    private void addCriteriaFormAtTop(String p_jobstate, String p_jobname)
    {
        ArrayList<String> labels = new ArrayList<String>();
        labels.add(ReportsPackage.getMessage(m_bundle, JOBS_STATUS_MSG)
                + LABEL_SUFFIX);
        labels.add(ReportsPackage.getMessage(m_bundle, JOB_NAME_MSG)
                + LABEL_SUFFIX);
        ArrayList<String> fields = new ArrayList<String>();
        fields.add(p_jobstate);
        fields.add(p_jobname);

        if (isJobCostingOn())
        {
            labels.add(ReportsPackage.getMessage(m_bundle, CURRENCY_MSG)
                    + LABEL_SUFFIX);
            fields.add(m_currency.getDisplayName(theUiLocale));
        }

        // prepare data for Criteria Form at Top
        // into (JobDetailsReportDataWrap)reportDataWrap !
        reportDataWrap.setCriteriaFormLabel(labels);
        reportDataWrap.setCriteriaFormValue(fields);

        // pagination
        this.pageRestHighPx = PAGE_CONTENT_HEIGTH_PX;
        LinkedHashMap<String, Object> tmpLinkMap = new LinkedHashMap<String, Object>();
        tmpLinkMap.put(Constants.PAGE_TITLE, reportDataWrap.getReportTitle());
        tmpLinkMap.put(Constants.CRITERIA_FORM, labels);
        reportDataWrap.addPageData(tmpLinkMap);
        tmpLinkMap = null;
        this.pageRestHighPx -= CRITERIA_FORM_HIGH_PX;
    }

    /**
     * Adds a form to the report containing the job information <br>
     * 
     * @param p_table
     *            -- a table model
     * @param r
     *            -- the current row
     * @param p_job
     *            -- the current job
     */
    private void addJobForm(TableModel p_table, int r, Job p_job)
    {
        // don't include job id and job name
        ArrayList<String> labels = new ArrayList<String>();
        ArrayList<Object> fields = new ArrayList<Object>();
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
                field = (Object) formattedCost;
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
        labels.add(ReportsPackage.getMessage(m_bundle, LOCAL_PROFILE_MSG)
                + LABEL_SUFFIX);
        fields.add(p_job.getL10nProfile().getName());

        // add data to (JobDetailsReportDataWrap)reportDataWrap !
        this.reportDataWrap.setJobFormLabel(labels);
        this.reportDataWrap.setJobFormValue(fields);

        // pagination
        LinkedHashMap<String, Object> tmpLinkMap = this
                .gainLinkMapOfPage(labels);
        tmpLinkMap.put(Constants.JOB_FORM, labels);
        tmpLinkMap = null;
    }

    /**
     * Adds the given WorkflowTableModel to the report <br>
     * 
     * @param WorkflowTableModel
     *            p_wtm
     */
    private void addWorkflowSummaryTable(WorkflowTableModel p_wtm)
    {
        int[] subcols;
        int i = 0;

        int subcolSize = 10;

        if (super.isUseInContext())
        {
            subcolSize++;
        }

        if (isJobCostingOn())
        {
            subcolSize++;
        }

        subcols = new int[subcolSize];

        subcols[i++] = WorkflowTableModel.TRGLOCALE;
        subcols[i++] = WorkflowTableModel.WFSTATE;
        subcols[i++] = WorkflowTableModel.SEGMENT_TM_WC;
        if (super.isUseInContext())
        {
            subcols[i++] = WorkflowTableModel.IN_CONTEXT_WC;
        }

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

            // now, go through the table and replace the cost with a formatted
            // cost
            for (int r = 0; r < p_wtm.getRowCount(); r++)
            {
                double cost = ((Float) p_wtm.getValueAt(r,
                        WorkflowTableModel.COST)).doubleValue();
                String formattedCurrency = currencyFormat.format(cost);
                p_wtm.setValueAt(formattedCurrency, r, WorkflowTableModel.COST);
            }
        }

        String textHead = ReportsPackage.getMessage(m_bundle,
                ALL_WORK_FLOWS_MSG);

        LinkedHashMap<String, Object> tempLinkedMap = gainLinkMapOfPage(p_wtm,
                Constants.ACTIVITY_TABLE);
        tempLinkedMap.put(Constants.CONTENT_TYPE_LABEL
                + Constants.CONTENT_TYPE_TABLE_MODEL, textHead);
        tempLinkedMap.put(Constants.CONTENT_TYPE_INTETER_ARRAY, subcols);
        tempLinkedMap.put(Constants.CONTENT_TYPE_TABLE_MODEL, p_wtm);
        tempLinkedMap = null;
    }

    /**
     * Outputs a table containing the activities for each workflow <br>
     * 
     * @param long p_jobId -- the id for this job
     * @param WorkflowTableModel
     *            p_wtm -- the WorkflowTableModel
     * @param ArrayList
     *            p_workflows -- the workflows for this job
     * 
     * @throws Exception
     */
    private void addDetailedWorkflowBreakdownTables(long p_jobId,
            WorkflowTableModel p_wtm, ArrayList<Workflow> p_workflows)
            throws Exception
    {
        Object[] columnNames = new Object[]
        { HIDDEN_ACTIVITY_ID,
                ReportsPackage.getMessage(m_bundle, ACTIVITY_NAME_MSG),
                ReportsPackage.getMessage(m_bundle, ACCEPTER_MSG),
                ReportsPackage.getMessage(m_bundle, ASSIGNEE_ROLE_MSG),
                ReportsPackage.getMessage(m_bundle, DURATION_MSG),
                ReportsPackage.getMessage(m_bundle, COMPLETE_DATE_MSG) };

        String textHead = ReportsPackage.getMessage(m_bundle,
                ACTIVITY_BREAKDOWN_MSG);
        // HashMap completedActivities =
        // ServerProxy.getJobHandler().getAllActivities();
        HashMap<?, ?> completedActivities = findCompletedActivities(p_jobId);

        for (int i = 0; i < p_workflows.size(); i++)
        {
            Workflow w = (Workflow) p_workflows.get(i);
            WorkflowInstance wfi = ServerProxy.getWorkflowServer()
                    .getWorkflowInstanceById(w.getId());
            ArrayList<WorkflowTaskInstance> wfiTasks = new ArrayList<WorkflowTaskInstance>(
                    wfi.getWorkflowInstanceTasks());
            List<Long> defaultTasks = findDefaultTaskIds(w.getId());
            Comparator<WorkflowTaskInstance> comparator = new InnerTaskComparator(
                    defaultTasks, completedActivities);
            SortUtil.sort(wfiTasks, comparator);

            Hashtable<?, ?> tasks = w.getTasks();

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
                        NOT_ACCEPTED_YET_MSG);
                if (wfTask.getTaskState() == Task.STATE_ACTIVE)
                {
                    Map<?, ?> activeTasks = ServerProxy.getWorkflowServer()
                            .getActiveTasksForWorkflow(w.getId());
                    WorkflowTaskInstance thisTask = (WorkflowTaskInstance) activeTasks
                            .get(taskId);
                    if (thisTask != null)
                        accepter = thisTask.getAccepter();
                    if (accepter == null || accepter.length() == 0)
                    {
                        accepter = ReportsPackage.getMessage(m_bundle,
                                NOT_ACCEPTED_YET_MSG);
                    }
                }
                else
                {
                    // Vector thetasks =
                    // ServerProxy.getWorkflowServer().getTasksForWorkflow(theSession.getId(),
                    // w.getId());
                    // Task thisTask = findThisTask(thetasks,taskId);
                    // see if there is a completed activity for this task
                    if (completedActivities.containsKey(taskId))
                    {
                        Object[] values = (Object[]) completedActivities
                                .get(taskId);
                        accepter = (String) values[CA_TASK_USER];
                    }
                    // if(thisTask == null){
                    // accepter =
                    // ReportsPackage.getMessage(m_bundle,NOT_ACCEPTED_YET_MSG);
                    // }else{
                    // accepter = thisTask.getAcceptor();
                    // }
                }

                // completed date
                String completedDate;
                if (task == null || task.getCompletedDate() == null)
                {
                    completedDate = NO_DATE_OR_TIME;
                }
                else
                {
                    completedDate = DateHelper.getFormattedDateAndTime(
                            task.getCompletedDate(), theUiLocale);
                }

                int k = 0;
                table.setValueAt(taskId, jcol, k++);
                /* The displayed name of the activity */
                table.setValueAt(wfTask.getActivity().getDisplayName(), jcol,
                        k++);
                table.setValueAt(accepter, jcol, k++);
                table.setValueAt(wfTask.getRolesAsString(), jcol, k++);
                table.setValueAt(task.getActualDuration(), jcol, k++);
                table.setValueAt(completedDate, jcol, k++);
            }

            String targetLocaleName = w.getTargetLocale().getDisplayName(
                    theUiLocale);
            // add data to (JobDetailsReportDataWrap)reportDataWrap !
            LinkedHashMap<String, Object> tmpLinkMap = gainLinkMapOfPage(table,
                    Constants.ACTIVITY_TABLE);
            // the first time, add this label to all this kinds of tales.
            if (i == 0)
            {
                tmpLinkMap.put(Constants.CONTENT_TYPE_LABEL
                        + Constants.ACTIVITY_TABLE, textHead);
            }
            // add 'i' in linkedhashmap key to distinguish multi table which is
            // the same category .
            tmpLinkMap.put(Constants.CONTENT_TYPE_TITLE + i, targetLocaleName);
            tmpLinkMap.put(Constants.ACTIVITY_TABLE + i, table);
            tmpLinkMap = null;
        }
    }

    @SuppressWarnings("unused")
    private Task findThisTask(Vector<Task> thetasks, Long taskId)
    {
        for (int i = 0; i < thetasks.size(); i++)
        {
            if (((Task) thetasks.get(i)).getId() == taskId.longValue())
            {
                return (Task) thetasks.get(i);
            }
        }
        return null;
    }

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
    // private HashMap findCompletedActivities(long p_jobid)
    // throws Exception
    // {
    // // if (m_completedActivitiesQuery == null)
    // // {
    // // SystemConfiguration sc = SystemConfiguration.getInstance();
    // // String twfuser =
    // sc.getStringParameter(SystemConfigParamNames.IFLOW_DB_USERNAME);
    // // StringBuffer sb = new StringBuffer();
    // // sb.append("SELECT");
    // // sb.append(" AI.activityinstanceid, AD.label, H.responsible \n");
    // // sb.append("FROM ");
    // // sb.append(twfuser);
    // // sb.append(".history H, workflow W, ");
    // // sb.append(twfuser);
    // // sb.append(".activityinstance AI, ");
    // // sb.append(twfuser);
    // // sb.append(".activitydefinition AD \n");
    // // sb.append("WHERE");
    // // sb.append(" W.job_id=? and H.processinstanceid=W.iflow_instance_id
    // \n");
    // // sb.append(" and H.eventcode=2 and
    // AI.processinstanceid=H.processinstanceid and
    // AD.activitydefinitionid=AI.activitydefinitionid and AD.activitytypeid=2
    // and AI.activityinstanceid=H.consumerid \n");
    // // sb.append("order by AI.activityinstanceid");
    // // m_completedActivitiesQuery = sb;
    // // }
    // //
    // // HashMap map = new HashMap();
    // // Connection c = null;
    // // PreparedStatement ps = null;
    // // try
    // // {
    // // c = ConnectionPool.getConnection();
    // // ps = c.prepareStatement(m_completedActivitiesQuery.toString());
    // // ps.setLong(1,p_jobid);
    // // ResultSet rs = ps.executeQuery();
    // // while (rs.next())
    // // {
    // // Long instanceId = new Long(rs.getLong(1));
    // // String activityName = rs.getString(2);
    // // String userResponsible = rs.getString(3);
    // // Object[] values = new Object[] {
    // // instanceId,activityName,userResponsible
    // // };
    // // map.put(instanceId,values);
    // // }
    // // ps.close();
    // // }
    // // finally
    // // {
    // // this.closeStatement(ps);
    // // this.returnConnection(c);
    // // }
    // HashMap map = new HashMap();
    // Job job = ServerProxy.getJobHandler().getJobById(p_jobid);
    // Collection c = job.getWorkflows();
    // for(Iterator it = c.iterator(); it.hasNext();){
    // Workflow wf = (Workflow)it.next();
    // long instanceId = wf.getId();
    // Collection taskCollection =
    // WorkflowProcessAdapter.getTaskInstances(instanceId);
    // for(Iterator taskIt = taskCollection.iterator(); taskIt.hasNext();){
    // TaskInstance taskInstance = (TaskInstance)taskIt.next();
    // Long taskId = new Long(taskInstance.getId());
    // String activityName = taskInstance.getName();
    // String userResponsible = taskInstance.getActorId();
    // Object[] values = new Object[]{
    // taskId, activityName, userResponsible
    // };
    // map.put(taskId, values);
    //
    // }
    // }
    // JbpmContext ctx = WorkflowConfiguration.getInstance().getJbpmContext();
    // if(ctx != null){
    // ctx.close();
    // }
    // return map;
    // }
    private List<Long> findDefaultTaskIds(long p_wfId) throws Exception
    {
        List<?> defaultTasks = ServerProxy.getWorkflowServer()
                .timeDurationsInDefaultPath(null, p_wfId, -1);
        Iterator<?> liter = defaultTasks.iterator();
        ArrayList<Long> defaultTaskIds = new ArrayList<Long>();
        while (liter.hasNext())
        {
            WfTaskInfo wftaskinfo = (WfTaskInfo) liter.next();
            defaultTaskIds.add(new Long(wftaskinfo.getId()));
        }

        return defaultTaskIds;
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
        Collection<?> c = p_job.getSourcePages();
        Iterator<?> iter = c.iterator();
        final int numRows = c.size();
        TableModel tm = new AbstractTableModel()
        {
            private static final long serialVersionUID = -5062562368047083672L;

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

        String textHead = ReportsPackage.getMessage(m_bundle, PAGES_IN_JOB_MSG);
        // add data to (JobDetailsReportDataWrap)reportDataWrap !
        LinkedHashMap<String, Object> tmpLinkMap = gainLinkMapOfPage(tm,
                Constants.PAGENAME_TABLE);
        tmpLinkMap.put(Constants.CONTENT_TYPE_LABEL + Constants.PAGENAME_TABLE,
                textHead);
        tmpLinkMap.put(Constants.PAGENAME_TABLE, tm);
        tmpLinkMap = null;
    }

    @SuppressWarnings("unused")
    private void dump(ArrayList<?> p_a)
    {
        Iterator<?> iter = p_a.iterator();
        while (iter.hasNext())
        {
            WorkflowTaskInstance wft = (WorkflowTaskInstance) iter.next();
            System.out.println("Task " + wft.getTaskId() + " "
                    + wft.getActivityName());
        }
    }

    private LinkedHashMap<String, Object> gainLinkMapOfPage(
            TableModel curTableModel, String tableCategory)
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
            LinkedHashMap<?, ?> newPageLinkMap = new LinkedHashMap<Object, Object>();
            reportDataWrap.addPageData(newPageLinkMap);
        }
        // gainCurrentPageData(currentPageNum)
        // 'currentPageNum' parameter is num of current page
        // not the arraylist index .
        this.pageRestHighPx -= tableHigh;
        allPageNum = reportDataWrap.getDataList().size();
        return reportDataWrap.gainCurrentPageData(allPageNum);
    }

    private LinkedHashMap<String, Object> gainLinkMapOfPage(ArrayList<?> curList)
    {
        // '1' is the head row of this table model .
        int rows = curList.size();
        int allPageNum = -1;
        int tableHigh = 0;
        tableHigh += ARRAYLIST_ROW_HIGH_PX * rows;

        if (!(this.pageRestHighPx > (PAGE_CONTENT_HEIGTH_PX - 50) || tableHigh < this.pageRestHighPx))
        {
            this.pageRestHighPx = PAGE_CONTENT_HEIGTH_PX;
            LinkedHashMap<?, ?> newPageLinkMap = new LinkedHashMap<Object, Object>();
            reportDataWrap.addPageData(newPageLinkMap);
        }
        // gainCurrentPageData(currentPageNum)
        // 'currentPageNum' parameter is num of current page
        // not the arraylist index .
        this.pageRestHighPx -= tableHigh;
        allPageNum = reportDataWrap.getDataList().size();

        return reportDataWrap.gainCurrentPageData(allPageNum);
    }

    class InnerTaskComparator implements Comparator<WorkflowTaskInstance>
    {
        private List<?> m_defaultTaskIds = null;
        private HashMap<?, ?> m_completedActivities = null;

        public InnerTaskComparator(List<?> p_defaultTaskIds,
                HashMap<?, ?> p_completedActivities)
        {
            m_defaultTaskIds = p_defaultTaskIds;
            m_completedActivities = p_completedActivities;
        }

        public int compare(WorkflowTaskInstance a_wfTask,
                WorkflowTaskInstance b_wfTask)
        {
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

    @SuppressWarnings("deprecation")
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
