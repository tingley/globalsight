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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.ResourceBundle;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.globalsight.diplomat.util.database.ConnectionPool;
import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.administration.users.UserUtil;
import com.globalsight.reports.Constants;
import com.globalsight.reports.JobTableModel;
import com.globalsight.reports.WorkflowTableModel;
import com.globalsight.reports.datawrap.AvgPerCompReportDataWrap;
import com.globalsight.reports.util.ReportHandlerFactory;
import com.globalsight.reports.util.ReportsPackage;

/**
 * Presents a report on the average percent completion by target locale for all
 * workflows for jobs that are DISPATCHED.
 */
public class AvgPerCompReportHandler extends BasicReportHandler
{
    private static final int REPORTHEADHEIGHT = 130;
    private static final int FIXJOBHEIGHT = 200;
    private static final int EVERYROWHEIGHT = 20;

    private static final String LABEL_SUFFIX = ": ";
    private static final String LABEL_COMMA = ",";
    private static final String MY_MESSAGES = BUNDLE_LOCATION
            + "avgPercentCompletion";
    private static final String PM_QUERY = "select distinct(manager_user_id) "
            + "from project p where p.companyid = ? order by manager_user_id";
    private static final String PM_QUERY_GS = "select distinct(manager_user_id) from project order by manager_user_id";
    private static final String JOB_QUERY = "select job.id, job.name, job.state from job "
            + "where job.state='LOCALIZED' or job.state='EXPORTED' "
            + "or job.state='DISPATCHED' or job.state='ARCHIVED' order by job.id";

    private AvgPerCompReportDataWrap reportDataWrap = null;

    protected ResourceBundle m_bundle = null;

    public void init()
    {
        try
        {
            super.init(); // get the common parameters
            m_bundle = ResourceBundle.getBundle(MY_MESSAGES, theUiLocale);
            this.reportKey = Constants.AVGPERCOMP_REPORT_KEY;
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
        super.invokeHandler(req, res, p_context);
        String act = (String) req.getParameter(Constants.REPORT_ACT);
        s_category
                .debug("AvgPerCompReportHandler invokeHandler will serve the request with REPORT_ACT: "
                        + act);
        if (Constants.REPORT_ACT_PREP.equalsIgnoreCase(act))
        {
            addMoreReportParameters(req); // prepare data for parameter web
                                          // page
            dispatcherForward(
                    ReportHandlerFactory.getTargetUrl(reportKey
                            + Constants.REPORT_ACT_PREP), req, res, p_context);
        }
        else if (Constants.REPORT_ACT_CREATE.equalsIgnoreCase(act))
        {
            createReport(req); // fill report DataWrap with data
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
        addProjectManagerParameter(req);
    }

    /**
     * Creates the actual report and fills it with data and messages. <br>
     * 
     * @param HttpServletRequest
     */
    public void createReport(HttpServletRequest req)
    {
        s_category.debug("Create AvgPerCompReport now");
        try
        {
            reportDataWrap = new AvgPerCompReportDataWrap();
            bindMessages();
            fillAllData(req);
        }
        catch (Exception e)
        {
            ReportsPackage.logError(e);
        }
    }

    /**
     * Initializes the user ids of the project managers who actually have
     * projects. <br>
     */
    private void addProjectManagerParameter(HttpServletRequest req)
            throws Exception
    {
        Connection c = null;
        PreparedStatement ps = null;
        try
        {
            // get all the project managers for parameter web page
            ArrayList projectManagers = new ArrayList();
            c = ConnectionPool.getConnection();

            String currentId = CompanyThreadLocal.getInstance().getValue();
//            if (!CompanyWrapper.SUPER_COMPANY_ID.equals(currentId))
//            {
                ps = c.prepareStatement(PM_QUERY);
                ps.setLong(1, Long.parseLong(currentId));
//            }
//            else
//            {
//                ps = c.prepareStatement(PM_QUERY_GS);
//            }

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
     * Fills out the messages on the report <br>
     * 
     * @param RepletRequest
     */
    private void bindMessages()
    {
        reportDataWrap.setReportTitle(ReportsPackage.getMessage(m_bundle,
                Constants.REPORT_TITLE));
        // set common messages for report, such as header ,footer
        setCommonMessages(reportDataWrap);
    }

    /**
     * Gets the data from the DB and binds it to tables <br>
     * 
     * @param HttpServletRequest
     * @throws Exception
     */
    private void fillAllData(HttpServletRequest req) throws Exception
    {
        String userId = (String) req.getSession(false).getAttribute(WebAppConstants.USER_NAME);
        // add a form at the top to show the criteria that was selected for the
        // report
        String projectManager = (String) req
                .getParameter(Constants.PROJECT_MGR);
        addCriteriaFormAtTop(projectManager);
        s_category.debug("User select the " + projectManager);
        
        // get all localized and dispatched jobs
        ArrayList jobs = null;
        if (projectManager.equals(ReportsPackage.getMessage(m_bundle,
                Constants.CRITERIA_ALLPMS)))
        {
            jobs = new ArrayList(ServerProxy.getJobHandler().getJobsByState(
                    DISPATCHED, userId));
            jobs.addAll(ServerProxy.getJobHandler().getJobsByState(LOCALIZED, userId));
        }
        else
        {
            jobs = new ArrayList(ServerProxy.getJobHandler()
                    .getJobsByManagerIdAndState(
                            UserUtil.getUserIdByName(projectManager),
                            DISPATCHED));
            jobs.addAll(ServerProxy.getJobHandler().getJobsByManagerIdAndState(
                    projectManager, LOCALIZED));
        }

        // put all the job and workflow data into tables
        JobTableModel jtm = new JobTableModel(jobs, theUiLocale, null);
        ArrayList jobWorkflows = getAllWorkflowsForAllJobs(jtm.getJobIds());

        HashMap workflowTableModels = makeWorkflowTableModels(jtm, jobWorkflows);
        reportDataWrap.setTotalJobNum(new Integer(jtm.getRowCount()));

        int pageId = 1;
        int pageHeight = PAGE_CONTENT_HEIGTH_PX;
        reportDataWrap.setCurrentPageNum(pageId);
        reportDataWrap.setTotalPageNum(pageId);
        HashMap dataSourceHashMap = new HashMap();
        HashMap pageHashMap = new HashMap();

        // now run through the sorted table and add a form at the top for each
        // job
        for (int r = 0; r < jtm.getRowCount(); r++)
        {
            Long jobId = (Long) jtm.getValueAt(r, JobTableModel.ID);
            WorkflowTableModel wtm = (WorkflowTableModel) workflowTableModels
                    .get(jobId);

            ArrayList jobForm = new ArrayList();
            addJobForm(jtm, r, jobForm);

            // only show jobs with workflows
            if (wtm.getRowCount() > 0)
            {
                int subcols[] = new int[]
                { WorkflowTableModel.TRGLOCALE, WorkflowTableModel.PER_COMPLETE };

                jobForm.add(2, subcols);
                jobForm.add(3, wtm);
            }

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
                pageHashMap = new HashMap();
                pageId = pageId + 1;
            }
            pageHashMap.put(jobId, jobForm);
            pageHeight -= jobFormHeight;
            jobForm = new ArrayList();
        }
        dataSourceHashMap.put(new Integer(pageId), pageHashMap);
        reportDataWrap.setTotalPageNum(pageId);
        // fill the first page data
        reportDataWrap.setAvgPerCompMap((HashMap) dataSourceHashMap
                .get(new Integer(1)));
        // save the first page data (public info)
        setSessionAttribute(theSession, Constants.AVGPERCOMP_REPORT_DATA,
                reportDataWrap);
        // save all pages data
        setSessionAttribute(theSession, Constants.AVGPERCOMP_REPORT_DATASOURCE,
                dataSourceHashMap);
        req.setAttribute(Constants.AVGPERCOMP_REPORT_DATA, reportDataWrap);
    }

    public void bindData(HttpServletRequest req, String pageId)
    {
        HashMap dataSourceHashMap = (HashMap) getSessionAttribute(theSession,
                Constants.AVGPERCOMP_REPORT_DATASOURCE);
        AvgPerCompReportDataWrap reportDataWrap2 = (AvgPerCompReportDataWrap) getSessionAttribute(
                theSession, Constants.AVGPERCOMP_REPORT_DATA);
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
        HashMap tempHashMap = (HashMap) dataSourceHashMap.get(new Integer(
                nextId));
        reportDataWrap2.setAvgPerCompMap(tempHashMap);
        reportDataWrap2.setCurrentPageNum(nextId);
        req.setAttribute(Constants.AVGPERCOMP_REPORT_DATA, reportDataWrap2);
    }

    /**
     * Adds a form at the top with the selected PM and workflow state <br>
     * 
     * @param projectManager
     *            -- the selected PM
     */
    private void addCriteriaFormAtTop(String projectManager)
    {
        String wfstatusLabel = ReportsPackage.getMessage(m_bundle,
                Constants.WFSTATUS);
        ArrayList labels = new ArrayList();
        labels.add(wfstatusLabel + LABEL_SUFFIX);
        labels.add(ReportsPackage.getMessage(m_bundle, Constants.PROJECT_MGR)
                + LABEL_SUFFIX);
        String wfstate = ReportsPackage.getMessage(m_bundle, DISPATCHED)
                + LABEL_COMMA + ReportsPackage.getMessage(m_bundle, LOCALIZED);
        ArrayList fields = new ArrayList();
        fields.add(wfstate);
        fields.add(projectManager);
        reportDataWrap.setCriteriaFormLabel(labels);
        reportDataWrap.setCriteriaFormValue(fields);
    }

    /**
     * Returns an array list containing each job's workflows <br>
     * 
     * @param p_jobs
     *            -- LinkedList of jobids
     */
    private ArrayList getAllWorkflowsForAllJobs(LinkedList p_jobs)
    {
        // contains an array list for each job
        ArrayList jobWorkflows = new ArrayList();
        for (int i = 0; i < p_jobs.size(); i++)
        {
            Long jobId = (Long) p_jobs.get(i);
            ArrayList workflows = null; // the workflows for this job
            try
            {
                workflows = new ArrayList(ServerProxy.getJobReportingManager()
                        .getWorkflowsByJobId(jobId.longValue()));
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
     * Returns a HashMap containing workflowtablemodels for each job <br>
     * 
     * @param p_jtm
     * @param p_jobWorkflows
     *            -- an arraylist of arraylists for each job containing the
     *            workflows
     * @return HashMap of WorkflowTableModels
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
     * Adds a form to the report containing the job information <br>
     * 
     * @param jtm
     *            -- the JobTableModel
     * @param r
     *            -- the current row
     * @param jobForm
     */
    private void addJobForm(JobTableModel jtm, int r, ArrayList jobForm)
    {
        ArrayList labels = new ArrayList();
        ArrayList fields = new ArrayList();

        // make sure that Job Name is first
        String origLabel = (String) jtm.getColumnName(JobTableModel.JOBNAME);
        labels.add(origLabel + LABEL_SUFFIX);
        // ignore the header row
        fields.add(jtm.getValueAt(r, JobTableModel.JOBNAME));

        // set the other fields and labels
        Object field = null;
        // ignore the jobId
        for (int c = 1; c < jtm.getColumnCount(); c++)
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

            field = jtm.getValueAt(r, c);
            origLabel = (String) jtm.getColumnName(c);
            labels.add(origLabel + LABEL_SUFFIX);
            fields.add(field);
        }

        jobForm.add(0, labels);
        jobForm.add(1, fields);
    }

}
