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
package com.globalsight.everest.webapp.pagehandler.administration.reports;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.format.UnderlineStyle;
import jxl.write.Blank;
import jxl.write.DateFormats;
import jxl.write.DateTime;
import jxl.write.Formula;
import jxl.write.Label;
import jxl.write.Number;
import jxl.write.NumberFormat;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableImage;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;

import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.costing.BigDecimalHelper;
import com.globalsight.everest.costing.Cost;
import com.globalsight.everest.costing.CostByWordCount;
import com.globalsight.everest.costing.Currency;
import com.globalsight.everest.costing.WordcountForCosting;
import com.globalsight.everest.foundation.SearchCriteriaParameters;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.jobhandler.JobSearchParameters;
import com.globalsight.everest.projecthandler.Project;
import com.globalsight.everest.projecthandler.TranslationMemoryProfile;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.taskmanager.Task;
import com.globalsight.everest.util.system.SystemConfigParamNames;
import com.globalsight.everest.util.system.SystemConfiguration;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.pagehandler.administration.reports.util.ReportUtil;
import com.globalsight.everest.webapp.pagehandler.projects.workflows.JobSearchConstants;
import com.globalsight.everest.workflowmanager.Workflow;
import com.globalsight.log.GlobalSightCategory;
import com.globalsight.util.AmbFileStoragePathUtils;
import com.globalsight.util.IntHolder;
import com.globalsight.util.JfreeCharUtil;

public class JobOnlineXlsReport extends XlsReports
{

    // String EMEA = CompanyWrapper.getCurrentCompanyName();
    private static GlobalSightCategory s_logger = (GlobalSightCategory) GlobalSightCategory
            .getLogger("Reports");

    // defines a 0 format for a 3 decimal precision point BigDecimal
    private static final String BIG_DECIMAL_ZERO_STRING = "0.000";

    // For "Dell Review Report Issue".
    private static String DELL_REVIEW = "Dell_Review";

    private static final int MONTH_SHEET = 0;

    private static final int MONTH_REVIEW_SHEET = 1;

    // For "Add Job Id into online job report" issue
    private boolean isJobIdVisible = false;

    // the big decimal scale to use for internal math
    private static int SCALE = 3;

    private Calendar m_calendar = Calendar.getInstance();

    private WritableWorkbook m_workbook = null;

    /* The symbol of the currency from the request */
    private String symbol = null;

    private String currency = null;
    
    private Hashtable<String, List<Integer>> totalCost = new Hashtable<String, List<Integer>>();
    private HashMap<String, Double> totalCostDate = new HashMap<String, Double>();
    private String totalCol = null;

    private class MyData
    {
        public boolean wantsAllProjects = false;

        boolean wantsAllLocales = false;
        
        HashSet<String> trgLocaleList = new HashSet<String>();

        ArrayList<Long> projectIdList = new ArrayList<Long>();

        Hashtable<Long, Project> wrongJobMap = new Hashtable<Long, Project>(); // maps
        

        // jobs
        // to
        // new
        // project

        boolean warnedAboutMissingWrongJobsFile = false;

        ArrayList<Job> wrongJobs = new ArrayList<Job>();

        ArrayList<String> wrongJobNames = new ArrayList<String>();

        String dateFormatString = "MM/dd/yy hh:mm:ss a z";

        // wrong jobs which may be queried for the project which should be
        // ignored
        ArrayList<Job> ignoreJobs = new ArrayList<Job>();

        boolean useInContext = false;

        boolean useDefaultContext = false;

        public String getDateFormatString()
        {
            return dateFormatString;
        }

        public void setDateFormatString(String dateFormatString)
        {
            this.dateFormatString = dateFormatString;
        }

    };

    public JobOnlineXlsReport(HttpServletRequest request,
            HttpServletResponse response) throws Exception
    {
        init(request);
        generateReport(request, response, new MyData());
    }

    private void init(HttpServletRequest request)
    {
        currency = request.getParameter("currency");
        symbol = ReportUtil.getCurrencySymbol(currency);
    }

    private String[] getMonths(ResourceBundle bundle)
    {
        String[] months = new String[12];
        months[0] = bundle.getString("lb_january");
        months[1] = bundle.getString("lb_february");
        months[2] = bundle.getString("lb_march");
        months[3] = bundle.getString("lb_april");
        months[4] = bundle.getString("lb_may");
        months[5] = bundle.getString("lb_june");
        months[6] = bundle.getString("lb_july");
        months[7] = bundle.getString("lb_august");
        months[8] = bundle.getString("lb_september");
        months[9] = bundle.getString("lb_october");
        months[10] = bundle.getString("lb_november");
        months[11] = bundle.getString("lb_december");
        
        return months;
    }
    
    /**
     * Generates the Excel report as a temp file and returns the temp file. The
     * report consists of all jobs for the given PM, this year, on a month by
     * month basis.
     * 
     * @return File
     * @exception Exception
     */
    private void generateReport(HttpServletRequest p_request,
            HttpServletResponse p_response, MyData p_data) throws Exception
    {
        ResourceBundle bundle = PageHandler.getBundle(p_request.getSession());
        String dateFormatString = p_request.getParameter("dateFormat");
        String[] months = getMonths(bundle);
        WorkbookSettings settings = new WorkbookSettings();
        settings.setSuppressWarnings(true);
        m_workbook = Workbook.createWorkbook(p_response.getOutputStream(),
                settings);
        int currentMonth = m_calendar.get(Calendar.MONTH);
        int year;
        if (p_request.getParameter("year") != null)
        {
            year = Integer.parseInt(p_request.getParameter("year"));
        }
        else
        {
            year = m_calendar.get(Calendar.YEAR);
        }
        if (year != m_calendar.get(Calendar.YEAR)) currentMonth = 11;

        boolean wroteSomething = false;
        boolean recalculateFinishedWorkflow = false;
        String recalcParam = p_request.getParameter("recalc");
        if (recalcParam != null && recalcParam.length() > 0)
        {
            recalculateFinishedWorkflow = java.lang.Boolean
                    .valueOf(recalcParam).booleanValue();
        }

        // For "Dell Review Report Issue"
        // Gets the tag that marks whether needs to separates "Dell_Review" or
        // not.
        // Default value is "false".
        boolean includeExReview = false;

        String reviewParam = p_request.getParameter("review");
        if ((reviewParam != null) && (reviewParam.length() > 0))
        {
            includeExReview = java.lang.Boolean.valueOf(reviewParam)
                    .booleanValue();
        }

        p_data.setDateFormatString(dateFormatString);
        setProjectIdList(p_request, p_data);
        setTargetLocales(p_request, p_data);
        // get all the jobs that were originally imported with the wrong project
        // the users want to pretend that these jobs are in this project
		if (p_request.getParameterValues("status") != null)
			getJobsInWrongProject(p_data);

        // For "Add Job Id into online job report" issue
        isJobIdVisible = isJobIdVisible(p_request);
        String isReportForYear = p_request.getParameter("reportForThisYear");
        String isReportForDetail = p_request.getParameter("reportForDetail");
        if (isReportForYear != null && isReportForYear.equals("on"))
        {
            int i = 0;
            for (i = 0; i <= currentMonth; i++)
            {
                // For "Dell Review Report Issue"
                // Add new parameter "includeExReview" to the signature.
                HashMap<String, HashMap<String, ProjectWorkflowData>> projectMap = getProjectDataForMonth(
                        p_request, i, recalculateFinishedWorkflow,
                        includeExReview, p_data);

                if (projectMap.size() > 0)
                {
                    // This sheets array has two elements.
                    // sheets[MONTH_SHEET], the original monthSheet.
                    // sheets[MONTH_REVIEW_SHEET], new monthly Dell_Review
                    // sheet.
                    WritableSheet[] sheets = new WritableSheet[2];
                    IntHolder[] rows = new IntHolder[2];

                    String sheetTitle = months[i] + " " + year;
                    sheets[MONTH_SHEET] = m_workbook.createSheet(sheetTitle, i);
                    addHeader(sheets[MONTH_SHEET], MONTH_SHEET, p_data, bundle);
                    rows[MONTH_SHEET] = new IntHolder(4);

                    if (includeExReview)
                    {
                        String sheetReviewTitle = bundle.getString("lb_review")
                                + " " + months[i] + " " + year;
                        sheets[MONTH_REVIEW_SHEET] = m_workbook.createSheet(
                                sheetReviewTitle, i);
                        addHeader(sheets[MONTH_REVIEW_SHEET],
                                MONTH_REVIEW_SHEET, p_data, bundle);
                        rows[MONTH_REVIEW_SHEET] = new IntHolder(4);
                    }

                    writeProjectData(projectMap, sheets, includeExReview, rows,
                            p_data, bundle);

                    wroteSomething = true;
                }
            }

            if (wroteSomething == false)
            {
                // just write out the first sheet to avoid Excel problems
                i = 0;
                String sheetTitle = months[currentMonth] + " " + year;
                WritableSheet monthSheet = m_workbook
                        .createSheet(sheetTitle, 0);
                addHeader(monthSheet, p_data, bundle);

                if (includeExReview)
                {
                    String sheetReviewTitle = "Review " + months[currentMonth]
                            + " " + year;
                    WritableSheet monthReviewSheet = m_workbook.createSheet(
                            sheetReviewTitle, 1);
                    addHeader(monthReviewSheet, MONTH_REVIEW_SHEET, p_data, bundle);
                }
            }
            
            WritableSheet paramsSheet = m_workbook.createSheet(bundle.getString("lb_criteria"), ++i);
            paramsSheet.addCell(new Label(0, 0, bundle.getString("lb_report_criteria")));
            paramsSheet.setColumnView(0, 50);

            if (p_data.wantsAllProjects)
            {
                paramsSheet.addCell(new Label(0, 1, bundle
                        .getString("lb_selected_projects")
                        + " " + bundle.getString("all")));
            }
            else
            {
                paramsSheet.addCell(new Label(0, 1, bundle
                        .getString("lb_selected_projects")));
                Iterator<Long> iter = p_data.projectIdList.iterator();
                int r = 3;
                while (iter.hasNext())
                {
                    Long pid = (Long) iter.next();
                    String projectName = "??";
                    try
                    {
                        Project p = ServerProxy.getProjectHandler()
                                .getProjectById(pid.longValue());
                        projectName = p.getName();
                    }
                    catch (Exception e)
                    {
                    }
                    paramsSheet.addCell(new Label(0, r, projectName));
                    paramsSheet.addCell(new Label(1, r, bundle
                            .getString("lb_id")
                            + "=" + pid.toString()));
                    r++;
                }
            }

            paramsSheet.addCell(new Label(2, 0, bundle.getString("lb_Year")));
            paramsSheet.addCell(new Label(2, 1, (String) p_request
                    .getParameter("year")));
            paramsSheet.addCell(new Label(3, 0, bundle
                    .getString("lb_re_cost_jobs")));
            paramsSheet.addCell(new Label(3, 1, java.lang.Boolean
                    .toString(recalculateFinishedWorkflow)));

            m_workbook.write();
            m_workbook.close();
        }
        if (isReportForDetail != null && isReportForDetail.endsWith("on"))
        {

            // For "Dell Review Report Issue"
            // Add new parameter "includeExReview" to the signature.
            HashMap<String, HashMap<String, ProjectWorkflowData>> projectMap = getProjectDataForMonth(
                    p_request, 0, recalculateFinishedWorkflow, includeExReview,
                    p_data);

            if (projectMap.size() > 0)
            {
                // This sheets array has two elements.
                // sheets[MONTH_SHEET], the original monthSheet.
                // sheets[MONTH_REVIEW_SHEET], new monthly Dell_Review sheet.
                WritableSheet[] sheets = new WritableSheet[2];
                IntHolder[] rows = new IntHolder[2];

                String sheetTitle = bundle.getString("lb_detail_report");
                sheets[MONTH_SHEET] = m_workbook.createSheet(sheetTitle, 0);
                addHeader(sheets[MONTH_SHEET], MONTH_SHEET, p_data, bundle);
                rows[MONTH_SHEET] = new IntHolder(4);

                if (includeExReview)
                {
                    String sheetReviewTitle = bundle.getString("lb_review_detail_report");
                    sheets[MONTH_REVIEW_SHEET] = m_workbook.createSheet(
                            sheetReviewTitle, 0);
                    addHeader(sheets[MONTH_REVIEW_SHEET], MONTH_REVIEW_SHEET,
                            p_data, bundle);
                    rows[MONTH_REVIEW_SHEET] = new IntHolder(4);
                }

                writeProjectData(projectMap, sheets, includeExReview, rows,
                        p_data, bundle);

                wroteSomething = true;
            }
            WritableSheet paramsSheet = m_workbook.createSheet(bundle.getString("lb_criteria"), 1);
            paramsSheet.addCell(new Label(0, 0, bundle.getString("lb_report_criteria")));
            paramsSheet.setColumnView(0, 50);

            if (p_data.wantsAllProjects)
            {
                paramsSheet.addCell(new Label(0, 1, bundle
                        .getString("lb_selected_projects")
                        + " " + bundle.getString("all")));
            }
            else
            {
                paramsSheet.addCell(new Label(0, 1, bundle
                        .getString("lb_selected_projects")));
                Iterator<Long> iter = p_data.projectIdList.iterator();
                int r = 3;
                while (iter.hasNext())
                {
                    Long pid = (Long) iter.next();
                    String projectName = "??";
                    try
                    {
                        Project p = ServerProxy.getProjectHandler()
                                .getProjectById(pid.longValue());
                        projectName = p.getName();
                    }
                    catch (Exception e)
                    {
                    }
                    paramsSheet.addCell(new Label(0, r, projectName));
                    paramsSheet.addCell(new Label(1, r, bundle
                            .getString("lb_id")
                            + "=" + pid.toString()));
                    r++;
                }
            }

            paramsSheet.addCell(new Label(2, 0, bundle.getString("lb_Year")));
            paramsSheet.addCell(new Label(2, 1, (String) p_request
                    .getParameter("year")));
            paramsSheet.addCell(new Label(3, 0, bundle.getString("lb_re_cost_jobs")));
            paramsSheet.addCell(new Label(3, 1, java.lang.Boolean
                    .toString(recalculateFinishedWorkflow)));

            m_workbook.write();
            m_workbook.close();
        }

    }

    /**
     * For "Dell Review Report Issue" Add a new parameter "includeExReview" to
     * the signature.
     */
    private HashMap<String, HashMap<String, ProjectWorkflowData>> getProjectDataForMonth(
            HttpServletRequest p_request, int p_month,
            boolean p_recalculateFinishedWorkflow, boolean p_includeExReview,
            MyData p_data) throws Exception
    {
        String isReportForYear = p_request.getParameter("reportForThisYear");
        String isReportForDetail = p_request.getParameter("reportForDetail");
        JobSearchParameters searchParams = new JobSearchParameters();
        if (isReportForYear != null && isReportForYear.equals("on"))
        {
            searchParams = getSearchParams(p_request, p_month, p_data);
        }
        if (isReportForDetail != null && isReportForDetail.endsWith("on"))
        {
            if (p_data.wantsAllProjects == false)
            {
                searchParams.setProjectId(p_data.projectIdList);
            }

            String paramCreateDateStartCount = p_request
                    .getParameter(JobSearchConstants.CREATION_START);
            String paramCreateDateStartOpts = p_request
                    .getParameter(JobSearchConstants.CREATION_START_OPTIONS);
            if ("-1".equals(paramCreateDateStartOpts) == false)
            {
                searchParams.setCreationStart(new Integer(
                        paramCreateDateStartCount));
                searchParams
                        .setCreationStartCondition(paramCreateDateStartOpts);
            }

            String paramCreateDateEndCount = p_request
                    .getParameter(JobSearchConstants.CREATION_END);
            String paramCreateDateEndOpts = p_request
                    .getParameter(JobSearchConstants.CREATION_END_OPTIONS);
            if (SearchCriteriaParameters.NOW.equals(paramCreateDateEndOpts))
            {
                searchParams.setCreationEnd(new java.util.Date());
            }
            else if ("-1".equals(paramCreateDateEndOpts) == false)
            {
                searchParams
                        .setCreationEnd(new Integer(paramCreateDateEndCount));
                searchParams.setCreationEndCondition(paramCreateDateEndOpts);
            }

        }

        //initial status parameter
        String[] statusParams = p_request.getParameterValues("status");
        ArrayList<String> statusList = new ArrayList<String>();
		if (statusParams != null) 
		{
			boolean allStatus = false;
			for (int i = 0; i < statusParams.length; i++) 
			{
				String status = statusParams[i];
				if (status.equals("*") == false) 
				{
					if (Job.PENDING.equals(status))
					{
						statusList.add(Job.BATCHRESERVED);
						statusList.add(Job.PENDING);
						statusList.add(Job.IMPORTFAILED);
						statusList.add(Job.ADD_FILE);
						statusList.add(Job.DELETE_FILE);
					}
					else
					{
						statusList.add(status);
					}
				} 
				else 
				{
					allStatus = true;
					break;
				}
			}
			if (!allStatus)
				searchParams.setJobState(statusList);
		}
        
		ArrayList<Job> queriedJobs = statusParams == null ? new ArrayList<Job>()
				: new ArrayList<Job>(ServerProxy.getJobHandler().getJobs(
						searchParams));
        int year = Integer.parseInt(p_request.getParameter("year"));
        ArrayList<Job> wrongJobsThisMonth = getWrongJobsForThisMonth(p_data,
                p_month, year);
        // now create a Set of all the jobs
        HashSet<Job> jobs = new HashSet<Job>();
        jobs.addAll(queriedJobs);
        jobs.addAll(wrongJobsThisMonth);
        jobs.removeAll(p_data.ignoreJobs);

        p_data.useInContext = getUseInContext(jobs);
        p_data.useDefaultContext  = getUseDefaultContext(jobs);
        // first iterate through the Jobs and group by Project/workflow because
        // Dell
        // doesn't want to see actual Jobs
        HashMap<String, HashMap<String, ProjectWorkflowData>> projectMap = new HashMap<String, HashMap<String, ProjectWorkflowData>>();
        Currency pivotCurrency = ServerProxy.getCostingEngine()
                .getCurrencyByName(ReportUtil.getCurrencyName(currency),
                        CompanyThreadLocal.getInstance().getValue());

        for (Job j : jobs)
        {
            String projectDesc = getProjectDesc(p_data, j);

            for (Workflow w : j.getWorkflows())
            {
                TranslationMemoryProfile tmProfile = w.getJob()
                        .getL10nProfile().getTranslationMemoryProfile();
//              boolean isUseInContext = tmProfile
//                      .getIsContextMatchLeveraging();
                boolean isInContextMatch = PageHandler.isInContextMatch(w.getJob(), tmProfile);
                boolean isDefaultContextMatch = PageHandler.isDefaultContextMatch(w.getJob());
                String state = w.getState();
                // skip certain workflows
                if (Workflow.IMPORT_FAILED.equals(w.getState())
                        || Workflow.CANCELLED.equals(w.getState())
                        || Workflow.BATCHRESERVED.equals(w.getState()))
                {
                    continue;
                }
                String targetLang = w.getTargetLocale().toString();
                if (!p_data.wantsAllLocales
                        && !p_data.trgLocaleList.contains(targetLang))
                {
                    continue;
                }

                String jobId = Long.toString(j.getId());
                String jobName = j.getJobName();
                HashMap<String, ProjectWorkflowData> localeMap = projectMap
                        .get(jobId);
                if (localeMap == null)
                {
                    localeMap = new HashMap<String, ProjectWorkflowData>();
                    projectMap.put(jobId, localeMap);
                }

                ProjectWorkflowData data = new ProjectWorkflowData();
                // For "Job id not showing in external review tabs" Issue
                data.jobId = jobId;
                data.jobName = jobName;
                data.projectDesc = projectDesc;
                data.targetLang = targetLang;
                data.creationDate = j.getCreateDate();
                data.status = j.getDisplayState();
                if (Workflow.EXPORTED.equals(state))
                {       
                   data.wasExported = true;                
                   data.exportDate = getExportDate(targetLang, jobName);
                }
                if (Workflow.EXPORT_FAILED.equals(state))
                {
                    // if the workflow is EXPORT_FAILED, color the line in red
                    data.wasExportFailed = true;
                    data.exportDate = getExportDate(targetLang, jobName);
                }

                // now add or amend the data in the ProjectWorkflowData based on
                // this next workflow
                if (j.getCreateDate().before(data.creationDate))
                    data.creationDate = j.getCreateDate();

                // get the word count used for costing which incorporates the
                // LMT
                WordcountForCosting wfc = new WordcountForCosting(w);
                // add the sublev rep count to the total rep count
                data.repetitionWordCount = w.getRepetitionWordCount()
                        + w.getSubLevRepetitionWordCount();
                data.lowFuzzyMatchWordCount = wfc.updatedLowFuzzyMatchCount();
                data.medFuzzyMatchWordCount = wfc.updatedMedFuzzyMatchCount();
                data.medHiFuzzyMatchWordCount = wfc
                        .updatedMedHiFuzzyMatchCount();
                data.hiFuzzyMatchWordCount = wfc.updatedHiFuzzyMatchCount();

                // the fuzzyMatchWordCount is the sum of the top 3 categories
                data.fuzzyMatchWordCount = data.medFuzzyMatchWordCount
                        + data.medHiFuzzyMatchWordCount
                        + data.hiFuzzyMatchWordCount;

                // add the lowest fuzzies and sublev match to nomatch
                data.noMatchWordCount = w.getNoMatchWordCount()
                        + data.lowFuzzyMatchWordCount
                        + w.getSubLevMatchWordCount();

                data.segmentTmWordCount = (isInContextMatch) ? w.getSegmentTmWordCount() : (isDefaultContextMatch) 
                        ? w.getNoUseExactMatchWordCount() - w.getContextMatchWordCount() 
                                : w.getNoUseExactMatchWordCount();
                data.contextMatchWordCount = (isDefaultContextMatch) ? w.getContextMatchWordCount() : 0;
                data.inContextMatchWordCount = (isInContextMatch) ? w
                        .getInContextMatchWordCount() : w
                        .getNoUseInContextMatchWordCount();
                data.totalWordCount = w.getTotalWordCount();

                // They must be in front of calculate cost for activity
                // "Dell_Review".
                Cost wfCost = ServerProxy.getCostingEngine().calculateCost(w,
                        pivotCurrency, p_recalculateFinishedWorkflow,
                        Cost.REVENUE, p_recalculateFinishedWorkflow);

                CostByWordCount costByWordCount = wfCost.getCostByWordCount();

                // For "Dell Review Report Issue".
                // Calculates the cost for activity named "Dell_Review".
                if (p_includeExReview)
                {
                    Cost dellReviewCost = null;

                    Iterator<?> it = w.getTasks().values().iterator();
                    Task task = null;
                    while (it.hasNext())
                    {
                        task = (Task) it.next();
                        Cost cost = null;
                        // For "Job id not showing in external review tabs"
                        // Issue
                        SystemConfiguration config = SystemConfiguration
                                .getInstance();
                        String reportActivityName = config
                                .getStringParameter(SystemConfigParamNames.REPORTS_ACTIVITY);
                        if (reportActivityName != null
                                && !"".equals(reportActivityName))
                        {
                            DELL_REVIEW = reportActivityName;
                        }
                        if (DELL_REVIEW.equals(task.getTaskDisplayName()))
                        {
                            data.containsDellReview = true;
                            cost = ServerProxy.getCostingEngine()
                                    .calculateCost(task, pivotCurrency,
                                            p_recalculateFinishedWorkflow,
                                            Cost.REVENUE);
                            if (dellReviewCost == null)
                            {
                                dellReviewCost = cost;
                            }
                            else
                            {
                                dellReviewCost.add(cost);
                            }
                        }
                    }

                    CostByWordCount dellReviewCostByWordCount = null;
                    if (data.containsDellReview)
                    {
                        dellReviewCostByWordCount = dellReviewCost
                                .getCostByWordCount();
                    }
                    if (dellReviewCostByWordCount != null)
                    {
                        data.repetitionWordCountCostForDellReview = add(
                                data.repetitionWordCountCostForDellReview,
                                dellReviewCostByWordCount.getRepetitionCost());
                        data.noMatchWordCountCostForDellReview = add(
                                data.noMatchWordCountCostForDellReview,
                                dellReviewCostByWordCount.getNoMatchCost());
                        data.lowFuzzyMatchWordCountCostForDellReview = add(
                                data.lowFuzzyMatchWordCountCostForDellReview,
                                dellReviewCostByWordCount
                                        .getLowFuzzyMatchCost());
                        data.medFuzzyMatchWordCountCostForDellReview = add(
                                data.medFuzzyMatchWordCountCostForDellReview,
                                dellReviewCostByWordCount
                                        .getMedFuzzyMatchCost());
                        data.medHiFuzzyMatchWordCountCostForDellReview = add(
                                data.medHiFuzzyMatchWordCountCostForDellReview,
                                dellReviewCostByWordCount
                                        .getMedHiFuzzyMatchCost());
                        data.hiFuzzyMatchWordCountCostForDellReview = add(
                                data.hiFuzzyMatchWordCountCostForDellReview,
                                dellReviewCostByWordCount.getHiFuzzyMatchCost());
                        data.segmentTmWordCountCostForDellReview = add(
                                data.segmentTmWordCountCostForDellReview,
                                (isInContextMatch) ? dellReviewCostByWordCount
                                        .getSegmentTmMatchCost()
                                        : (isDefaultContextMatch) ? dellReviewCostByWordCount.getDefaultContextExactMatchCost()
                                                : dellReviewCostByWordCount.getNoUseExactMatchCost());
                        data.contextMatchWordCountCostForDellReview = add(
                                data.contextMatchWordCountCostForDellReview,
                                (isDefaultContextMatch) ? dellReviewCostByWordCount.getContextMatchCost() : 0);
                        data.inContextMatchWordCountCostForDellReview = add(
                                data.inContextMatchWordCountCostForDellReview,
                                (isInContextMatch) ? dellReviewCostByWordCount
                                        .getInContextMatchCost()
                                        : dellReviewCostByWordCount
                                                .getNoUseInContextMatchCost());

                        // fuzzy match cost is the sum of the top three fuzzy
                        // match categories
                        data.fuzzyMatchWordCountCostForDellReview = data.medFuzzyMatchWordCountCostForDellReview
                                .add(
                                        data.medHiFuzzyMatchWordCountCostForDellReview)
                                .add(
                                        data.hiFuzzyMatchWordCountCostForDellReview);

                        // this means that no match cost should include the
                        // lowest fuzzy match category
                        data.noMatchWordCountCostForDellReview = data.noMatchWordCountCostForDellReview
                                .add(data.lowFuzzyMatchWordCountCostForDellReview);

                        data.totalWordCountCostForDellReview = data.repetitionWordCountCostForDellReview
                                .add(data.noMatchWordCountCostForDellReview)
                                .add(data.fuzzyMatchWordCountCostForDellReview)
                                .add(data.segmentTmWordCountCostForDellReview)
                                .add(
                                        data.contextMatchWordCountCostForDellReview)
                                .add(
                                        data.inContextMatchWordCountCostForDellReview);
                    }
                }

                if (costByWordCount != null)
                {
                    data.repetitionWordCountCost = BigDecimalHelper.subtract(
                            costByWordCount.getRepetitionCost(),
                            data.repetitionWordCountCostForDellReview);
                    data.noMatchWordCountCost = BigDecimalHelper.subtract(
                            costByWordCount.getNoMatchCost(),
                            data.noMatchWordCountCostForDellReview);
                    data.lowFuzzyMatchWordCountCost = BigDecimalHelper
                            .subtract(
                                    costByWordCount.getLowFuzzyMatchCost(),
                                    data.lowFuzzyMatchWordCountCostForDellReview);
                    data.medFuzzyMatchWordCountCost = BigDecimalHelper
                            .subtract(
                                    costByWordCount.getMedFuzzyMatchCost(),
                                    data.medFuzzyMatchWordCountCostForDellReview);
                    data.medHiFuzzyMatchWordCountCost = BigDecimalHelper
                            .subtract(
                                    costByWordCount.getMedHiFuzzyMatchCost(),
                                    data.medHiFuzzyMatchWordCountCostForDellReview);
                    data.hiFuzzyMatchWordCountCost = BigDecimalHelper.subtract(
                            costByWordCount.getHiFuzzyMatchCost(),
                            data.hiFuzzyMatchWordCountCostForDellReview);
                    data.segmentTmWordCountCost = BigDecimalHelper.subtract(
                            (isInContextMatch) ? costByWordCount.getSegmentTmMatchCost() : 
                                (isDefaultContextMatch) ? costByWordCount.getDefaultContextExactMatchCost()
                                            : costByWordCount.getNoUseExactMatchCost(),
                            data.segmentTmWordCountCostForDellReview);
                    data.contextMatchWordCountCost = BigDecimalHelper.subtract(
                            (isDefaultContextMatch) ? costByWordCount.getContextMatchCost() : 0,
                            data.contextMatchWordCountCostForDellReview);
                    data.inContextMatchWordCountCost = BigDecimalHelper
                            .subtract(
                                    (isInContextMatch) ? costByWordCount
                                            .getInContextMatchCost()
                                            : costByWordCount
                                                    .getNoUseInContextMatchCost(),
                                    data.inContextMatchWordCountCostForDellReview);

                    // fuzzy match cost is the sum of the top three fuzzy match
                    // categories
                    data.fuzzyMatchWordCountCost = data.medFuzzyMatchWordCountCost
                            .add(data.medHiFuzzyMatchWordCountCost).add(
                                    data.hiFuzzyMatchWordCountCost);

                    // this means that no match cost should include the lowest
                    // fuzzy match category
                    data.noMatchWordCountCost = data.noMatchWordCountCost
                            .add(data.lowFuzzyMatchWordCountCost);

                    data.totalWordCountCost = data.repetitionWordCountCost.add(
                            data.noMatchWordCountCost).add(
                            data.fuzzyMatchWordCountCost).add(
                            data.segmentTmWordCountCost).add(
                            data.contextMatchWordCountCost).add(
                            data.inContextMatchWordCountCost);
                }

                localeMap.put(targetLang, data);
            }

            // now recalculate the job level cost if the workflow was
            // recalculated
            if (p_recalculateFinishedWorkflow)
            {
                ServerProxy.getCostingEngine().reCostJob(j, pivotCurrency,
                        Cost.REVENUE);
            }
        }

        return projectMap;
    }

    private Date getExportDate(String targetLang, String jobName) {
        String path = AmbFileStoragePathUtils.getCxeDocDirPath();
        path += "/" + targetLang + "/" + jobName;
        File file = new File(path);
        while (file.exists() && file.isDirectory())
        {
           File[] files = file.listFiles();
           if (files.length > 0)
           {
               file = files[0];
           }
        }
        
        return new Date(file.lastModified());
    }

    /**
     * Class used to group data by project and target language
     */
    private class ProjectWorkflowData
    {
        // For "Job id not showing in external review tabs" Issue
        public String jobId;

        public String jobName;

        public String projectDesc;

        public Date creationDate; // earliest job creation date this month
        
        public String status;
        
        public Date exportDate;

        public String targetLang;

        /* word counts */
        public long repetitionWordCount = 0;

        public long lowFuzzyMatchWordCount = 0;

        public long medFuzzyMatchWordCount = 0;

        public long medHiFuzzyMatchWordCount = 0;

        public long hiFuzzyMatchWordCount = 0;

        public long contextMatchWordCount = 0;

        public long inContextMatchWordCount = 0;

        public long segmentTmWordCount = 0;

        public long noMatchWordCount = 0;

        public long totalWordCount = 0;

        // Dell wants to see anything 75% and above as a fuzzy match
        // so this is the sum of all except the lowest band
        public long fuzzyMatchWordCount = 0;

        /* word count costs */
        public BigDecimal fuzzyMatchWordCountCost = new BigDecimal(
                BIG_DECIMAL_ZERO_STRING);

        public BigDecimal repetitionWordCountCost = new BigDecimal(
                BIG_DECIMAL_ZERO_STRING);

        public BigDecimal lowFuzzyMatchWordCountCost = new BigDecimal(
                BIG_DECIMAL_ZERO_STRING);

        public BigDecimal medFuzzyMatchWordCountCost = new BigDecimal(
                BIG_DECIMAL_ZERO_STRING);

        public BigDecimal medHiFuzzyMatchWordCountCost = new BigDecimal(
                BIG_DECIMAL_ZERO_STRING);

        public BigDecimal hiFuzzyMatchWordCountCost = new BigDecimal(
                BIG_DECIMAL_ZERO_STRING);

        public BigDecimal contextMatchWordCountCost = new BigDecimal(
                BIG_DECIMAL_ZERO_STRING);

        public BigDecimal inContextMatchWordCountCost = new BigDecimal(
                BIG_DECIMAL_ZERO_STRING);

        public BigDecimal segmentTmWordCountCost = new BigDecimal(
                BIG_DECIMAL_ZERO_STRING);

        public BigDecimal noMatchWordCountCost = new BigDecimal(
                BIG_DECIMAL_ZERO_STRING);

        public BigDecimal totalWordCountCost = new BigDecimal(
                BIG_DECIMAL_ZERO_STRING);

        public boolean containsDellReview = false;

        /* Word count costs for activity named "Dell_Review" */
        public BigDecimal fuzzyMatchWordCountCostForDellReview = new BigDecimal(
                BIG_DECIMAL_ZERO_STRING);

        public BigDecimal repetitionWordCountCostForDellReview = new BigDecimal(
                BIG_DECIMAL_ZERO_STRING);

        public BigDecimal lowFuzzyMatchWordCountCostForDellReview = new BigDecimal(
                BIG_DECIMAL_ZERO_STRING);

        public BigDecimal medFuzzyMatchWordCountCostForDellReview = new BigDecimal(
                BIG_DECIMAL_ZERO_STRING);

        public BigDecimal medHiFuzzyMatchWordCountCostForDellReview = new BigDecimal(
                BIG_DECIMAL_ZERO_STRING);

        public BigDecimal hiFuzzyMatchWordCountCostForDellReview = new BigDecimal(
                BIG_DECIMAL_ZERO_STRING);

        public BigDecimal contextMatchWordCountCostForDellReview = new BigDecimal(
                BIG_DECIMAL_ZERO_STRING);

        public BigDecimal inContextMatchWordCountCostForDellReview = new BigDecimal(
                BIG_DECIMAL_ZERO_STRING);

        public BigDecimal segmentTmWordCountCostForDellReview = new BigDecimal(
                BIG_DECIMAL_ZERO_STRING);

        public BigDecimal noMatchWordCountCostForDellReview = new BigDecimal(
                BIG_DECIMAL_ZERO_STRING);

        public BigDecimal totalWordCountCostForDellReview = new BigDecimal(
                BIG_DECIMAL_ZERO_STRING);

        public boolean wasExportFailed = false;
        
        public boolean wasExported = false;

        public ProjectWorkflowData()
        {
        }
    }

    private void setProjectIdList(HttpServletRequest p_request, MyData p_data)
    {
        // set the project Id
        String[] projectIds = p_request.getParameterValues("projectId");

		if (projectIds != null) 
		{
			for (int i = 0; i < projectIds.length; i++) 
			{
				String id = projectIds[i];
				if (id.equals("*") == false) 
				{
					p_data.projectIdList.add(new Long(id));
				} 
				else 
				{
					p_data.wantsAllProjects = true;
					break;
				}
			}
		}
    }

    private void setTargetLocales(HttpServletRequest p_request, MyData p_data)
    {
        String[] paramTrgLocales = p_request
                .getParameterValues("targetLocalesList");
        if (paramTrgLocales != null)
        {
            for (int i = 0; i < paramTrgLocales.length; i++)
            {
                if ("*".equals(paramTrgLocales[i]))
                {
                    p_data.wantsAllLocales = true;
                    break;
                }
                else
                {
                    p_data.trgLocaleList.add(paramTrgLocales[i]);
                }
            }
        }
    }

    /**
     * Returns search params used to find the jobs based on state
     * (READY,EXPORTED,LOCALIZED,DISPATCHED), and creation date during the
     * particular month.
     * 
     * @return JobSearchParams
     */
    private JobSearchParameters getSearchParams(HttpServletRequest p_request,
            int p_month, MyData p_data)
    {
        JobSearchParameters sp = new JobSearchParameters();
        if (p_data.wantsAllProjects == false)
        {
            sp.setProjectId(p_data.projectIdList);
        }

        // job state EXPORTED,DISPATCHED,LOCALIZED
        ArrayList<String> list = new ArrayList<String>();
        list.add(Job.READY_TO_BE_DISPATCHED);
        list.add(Job.DISPATCHED);
        list.add(Job.LOCALIZED);
        list.add(Job.EXPORTED);
        list.add(Job.EXPORT_FAIL);
        sp.setJobState(list);

        // find out which year they want to run the report for
        int year = Integer.parseInt(p_request.getParameter("year"));

        // find the first day of the month
        GregorianCalendar gcal = new GregorianCalendar(year, p_month, 1);
        gcal.setLenient(true);
        Date firstOfTheMonth = gcal.getTime();
        gcal.add(Calendar.MONTH, 1);
        Date firstOfNextMonth = gcal.getTime();
        sp.setCreationStart(firstOfTheMonth);
        sp.setCreationEnd(firstOfNextMonth);
        return sp;
    }

    /**
     * Adds the table header to the sheet
     * 
     * @param p_sheet
     */
    private void addHeader(WritableSheet p_sheet, MyData p_data, ResourceBundle bundle)
            throws Exception
    {
        addHeader(p_sheet, MONTH_SHEET, p_data, bundle);
    }

    private void addHeader(WritableSheet p_sheet, final int p_sheetCategory,
            MyData p_data, ResourceBundle bundle) throws Exception
    {
        // title font is black bold on white
        String EMEA = CompanyWrapper.getCurrentCompanyName();
        WritableFont titleFont = new WritableFont(WritableFont.ARIAL, 14,
                WritableFont.BOLD, false, UnderlineStyle.NO_UNDERLINE,
                jxl.format.Colour.BLACK);
        WritableCellFormat titleFormat = new WritableCellFormat(titleFont);
        titleFormat.setWrap(false);
        titleFormat.setShrinkToFit(false);
        
        p_sheet.addCell(new Label(0, 0, EMEA + " "
                + bundle.getString("lb_online"), titleFormat));
        p_sheet.setColumnView(0, 20);

        // headerFont is black bold on light grey
        WritableFont headerFont = new WritableFont(WritableFont.ARIAL, 9,
                WritableFont.BOLD, false, UnderlineStyle.NO_UNDERLINE,
                jxl.format.Colour.BLACK);
        WritableCellFormat headerFormat = new WritableCellFormat(headerFont);
        headerFormat.setWrap(true);
        headerFormat.setBackground(jxl.format.Colour.GRAY_25);
        headerFormat.setShrinkToFit(false);
        headerFormat.setBorder(jxl.format.Border.TOP,
                jxl.format.BorderLineStyle.THIN);
        headerFormat.setBorder(jxl.format.Border.BOTTOM,
                jxl.format.BorderLineStyle.THIN);
        headerFormat.setBorder(jxl.format.Border.LEFT,
                jxl.format.BorderLineStyle.THIN);
        headerFormat.setBorder(jxl.format.Border.RIGHT,
                jxl.format.BorderLineStyle.THIN);

        WritableCellFormat langFormat = new WritableCellFormat(headerFont);
        langFormat.setWrap(true);
        langFormat.setBackground(jxl.format.Colour.GRAY_25);
        langFormat.setShrinkToFit(false);
        langFormat.setBorder(jxl.format.Border.TOP,
                jxl.format.BorderLineStyle.THIN);
        langFormat.setBorder(jxl.format.Border.BOTTOM,
                jxl.format.BorderLineStyle.THIN);
        langFormat.setBorder(jxl.format.Border.LEFT,
                jxl.format.BorderLineStyle.THIN);
        langFormat.setBorder(jxl.format.Border.RIGHT,
                jxl.format.BorderLineStyle.THICK);

        WritableCellFormat wordCountFormat = new WritableCellFormat(headerFont);
        wordCountFormat.setWrap(true);
        wordCountFormat.setBackground(jxl.format.Colour.GRAY_25);
        wordCountFormat.setShrinkToFit(false);
        wordCountFormat.setBorder(jxl.format.Border.TOP,
                jxl.format.BorderLineStyle.THIN);
        wordCountFormat.setBorder(jxl.format.Border.BOTTOM,
                jxl.format.BorderLineStyle.THICK);
        wordCountFormat.setBorder(jxl.format.Border.LEFT,
                jxl.format.BorderLineStyle.THIN);
        wordCountFormat.setBorder(jxl.format.Border.RIGHT,
                jxl.format.BorderLineStyle.THICK);

        WritableCellFormat wordCountValueFormat = new WritableCellFormat(
                headerFont);
        wordCountValueFormat.setWrap(true);
        wordCountValueFormat.setBackground(jxl.format.Colour.GRAY_25);
        wordCountValueFormat.setShrinkToFit(false);
        wordCountValueFormat.setBorder(jxl.format.Border.TOP,
                jxl.format.BorderLineStyle.THIN);
        wordCountValueFormat.setBorder(jxl.format.Border.BOTTOM,
                jxl.format.BorderLineStyle.THICK);
        wordCountValueFormat.setBorder(jxl.format.Border.LEFT,
                jxl.format.BorderLineStyle.THIN);
        wordCountValueFormat.setBorder(jxl.format.Border.RIGHT,
                jxl.format.BorderLineStyle.THIN);

        WritableCellFormat wordCountValueRightFormat = new WritableCellFormat(
                headerFont);
        wordCountValueRightFormat.setWrap(true);
        wordCountValueRightFormat.setBackground(jxl.format.Colour.GRAY_25);
        wordCountValueRightFormat.setShrinkToFit(false);
        wordCountValueRightFormat.setBorder(jxl.format.Border.TOP,
                jxl.format.BorderLineStyle.THIN);
        wordCountValueRightFormat.setBorder(jxl.format.Border.BOTTOM,
                jxl.format.BorderLineStyle.THICK);
        wordCountValueRightFormat.setBorder(jxl.format.Border.LEFT,
                jxl.format.BorderLineStyle.THIN);
        wordCountValueRightFormat.setBorder(jxl.format.Border.RIGHT,
                jxl.format.BorderLineStyle.THICK);

        // For "Add Job Id into online job report" issue
        int col = 0;
        if (isJobIdVisible)
        {
            p_sheet.addCell(new Label(col, 2, bundle.getString("lb_job_id"), headerFormat));
            p_sheet.mergeCells(col, 2, col, 3);
            col++;
        }

        p_sheet.addCell(new Label(col, 2, bundle.getString("lb_job_name"), headerFormat));
        p_sheet.mergeCells(col, 2, col, 3);
        col++;
        p_sheet.addCell(new Label(col, 2, bundle.getString("lb_description"), headerFormat));
        p_sheet.mergeCells(col, 2, col, 3);
        col++;
        p_sheet.addCell(new Label(col, 2, bundle.getString("lb_creation_date"), headerFormat));
        p_sheet.mergeCells(col, 2, col, 3);
        col++;
        p_sheet.addCell(new Label(col, 2, bundle.getString("lb_creation_time"), headerFormat));
        p_sheet.mergeCells(col, 2, col, 3);
        col++;
        p_sheet.addCell(new Label(col, 2, bundle.getString("lb_export_date"), headerFormat));
        p_sheet.mergeCells(col, 2, col, 3);
        col++;
        p_sheet.addCell(new Label(col, 2, bundle.getString("lb_export_time"), headerFormat));
        p_sheet.mergeCells(col, 2, col, 3);
        col++;
        p_sheet.addCell(new Label(col, 2, bundle.getString("lb_status"), headerFormat));
        p_sheet.mergeCells(col, 2, col, 3);
        col++;
        p_sheet.addCell(new Label(col, 2, bundle.getString("lb_lang"), langFormat));
        p_sheet.mergeCells(col, 2, col, 3);
        col++;
        p_sheet.addCell(new Label(col, 2, bundle.getString("lb_word_counts"), wordCountFormat));
        if(p_data.useInContext && p_data.useDefaultContext)
        {
            p_sheet.mergeCells(col, 2, col + 6, 2);
        }
        else
        {
              if (p_data.useInContext)
                {
                    p_sheet.mergeCells(col, 2, col + 5, 2);
                }
                else if(p_data.useDefaultContext)
                {
                    p_sheet.mergeCells(col, 2, col + 5, 2);
                } 
                else
                {
                    p_sheet.mergeCells(col, 2, col + 4, 2);
                }
        }
        p_sheet
                .addCell(new Label(col, 3, bundle.getString("jobinfo.tmmatches.wordcounts.internalreps"),
                        wordCountValueFormat));
        col++;
        p_sheet.addCell(new Label(col, 3, bundle.getString("lb_100_exact_matches"),
                wordCountValueFormat));

        if (p_data.useInContext)
        {
            col++;
            p_sheet.addCell(new Label(col, 3, bundle.getString("lb_in_context_tm"),
                    wordCountValueFormat));
        }

        if(p_data.useDefaultContext)
        {
            col++;
            p_sheet.addCell(new Label(col, 3, bundle.getString("lb_context_tm"),
                    wordCountValueFormat));
        }
        
        col++;
        p_sheet
                .addCell(new Label(col, 3, bundle.getString("jobinfo.tmmatches.wordcounts.fuzzymatches"),
                        wordCountValueFormat));

        col++;
        p_sheet.addCell(new Label(col, 3, bundle.getString("jobinfo.tmmatches.wordcounts.newwords"), wordCountValueFormat));

        col++;
        p_sheet.addCell(new Label(col, 3,  bundle.getString("lb_total_source_word_count"),
                wordCountValueRightFormat));

        col++;
        p_sheet.addCell(new Label(col, 2, bundle.getString("jobinfo.tmmatches.invoice"), wordCountFormat));

        if (p_sheetCategory == MONTH_SHEET)
        {
            if(p_data.useInContext && p_data.useDefaultContext)
            {
                p_sheet.mergeCells(col, 2, col + 7, 2);
            }
            else
            {
                if (p_data.useInContext || p_data.useDefaultContext)
                {
                    p_sheet.mergeCells(col, 2, col + 6, 2);
                }
                else
                {
                    p_sheet.mergeCells(col, 2, col + 5, 2);
                }
            }
            p_sheet.addCell(new Label(col, 3, bundle.getString("jobinfo.tmmatches.invoice.internalreps"),
                    wordCountValueFormat));
            col++;
            p_sheet.addCell(new Label(col, 3, bundle.getString("lb_100_exact_matches"),
                    wordCountValueFormat));
            col++;
            if (p_data.useInContext)
            {
                p_sheet.addCell(new Label(col, 3, bundle.getString("lb_in_context_tm"),
                        wordCountValueFormat));
                col++;
            }
            if(p_data.useDefaultContext)
            {
                 p_sheet.addCell(new Label(col, 3, bundle.getString("lb_context_tm"),
                            wordCountValueFormat));
                    col++;

            }
            p_sheet.addCell(new Label(col, 3, bundle.getString("jobinfo.tmmatches.wordcounts.fuzzymatches"),
                    wordCountValueFormat));
            col++;
            p_sheet
                    .addCell(new Label(col, 3, bundle.getString("jobinfo.tmmatches.wordcounts.newwords"),
                            wordCountValueFormat));
            col++;
            p_sheet.addCell(new Label(col, 3, bundle.getString("jobinfo.tmmatches.wordcounts.total"),
                    wordCountValueRightFormat));
            col++;
            p_sheet.addCell(new Label(col, 3, bundle.getString("jobinfo.tmmatches.invoice.jobtotal"),
                    wordCountValueRightFormat));
            col++;

            p_sheet.addCell(new Label(col, 2, bundle.getString("lb_tracking")
                    + " " + EMEA + " " + bundle.getString("lb_use") + ")",
                    headerFormat));
            p_sheet.mergeCells(col, 2, col, 3);

        }
        else if (p_sheetCategory == MONTH_REVIEW_SHEET)
        {
            if(p_data.useInContext && p_data.useDefaultContext)
            {
                p_sheet.mergeCells(col, 2, col + 8, 2);
            }
            else
            {
                if (p_data.useInContext || p_data.useDefaultContext)
                {
                    p_sheet.mergeCells(col, 2, col + 7, 2);
                }
                else
                {
                    p_sheet.mergeCells(col, 2, col + 6, 2);
                }
            }
            p_sheet.addCell(new Label(col, 3, bundle.getString("jobinfo.tmmatches.wordcounts.internalreps"),
                    wordCountValueFormat));
            col++;
            p_sheet.addCell(new Label(col, 3, bundle.getString("lb_100_exact_matches"),
                    wordCountValueFormat));
            col++;
            if (p_data.useInContext)
            {
                p_sheet.addCell(new Label(col, 3, bundle.getString("lb_in_context_tm"),
                        wordCountValueFormat));
                col++;
            }
            if (p_data.useDefaultContext)
            {
                p_sheet.addCell(new Label(col, 3, bundle.getString("lb_context_tm"),
                        wordCountValueFormat));
                col++;
            }

            p_sheet.addCell(new Label(col, 3, bundle.getString("jobinfo.tmmatches.wordcounts.fuzzymatches"),
                    wordCountValueFormat));
            col++;
            p_sheet
                    .addCell(new Label(col, 3, bundle.getString("jobinfo.tmmatches.wordcounts.newwords"),
                            wordCountValueFormat));
            col++;
            p_sheet.addCell(new Label(col, 3, bundle.getString("lb_translation_total"),
                    wordCountValueRightFormat));
            col++;
            p_sheet.addCell(new Label(col, 3, bundle.getString("lb_review"),
                    wordCountValueRightFormat));
            col++;
            p_sheet.addCell(new Label(col, 3, bundle.getString("jobinfo.tmmatches.invoice.jobtotal"),
                    wordCountValueRightFormat));

            col++;
            p_sheet.addCell(new Label(col, 2, bundle.getString("lb_tracking")
                    + " " + EMEA + " " + bundle.getString("lb_use") + ")",
                    headerFormat));
            p_sheet.mergeCells(col, 2, col, 3);
        }
        else
        {
            // Should never go here.
        }
    }

    public void writeProjectData(
            HashMap<String, HashMap<String, ProjectWorkflowData>> p_projectMap,
            WritableSheet p_sheet, IntHolder p_row, MyData p_data, ResourceBundle bundle)
            throws Exception
    {
        WritableSheet[] sheets = new WritableSheet[2];
        sheets[MONTH_SHEET] = p_sheet;
        sheets[MONTH_REVIEW_SHEET] = null;

        IntHolder[] rows = new IntHolder[2];
        rows[MONTH_SHEET] = p_row;
        rows[MONTH_REVIEW_SHEET] = null;

        writeProjectData(p_projectMap, sheets, false, rows, p_data, bundle);
    }

    public void writeProjectData(
            HashMap<String, HashMap<String, ProjectWorkflowData>> p_projectMap,
            WritableSheet[] p_sheets, boolean p_includeExReview,
            IntHolder[] p_rows, MyData p_data, ResourceBundle bundle) throws Exception
    {
        ArrayList<String> projects = new ArrayList<String>(p_projectMap
                .keySet());
        Collections.sort(projects);
        Iterator<String> projectIter = projects.iterator();

        String datetimeFormatString = p_data.getDateFormatString();
        String dateFormatString = datetimeFormatString.substring(0,datetimeFormatString.indexOf(" ") - 1);
        String timeFormatString = datetimeFormatString.substring(datetimeFormatString.indexOf(" ") + 1,datetimeFormatString.length());
        SimpleDateFormat dateFormat = new SimpleDateFormat(dateFormatString);
        SimpleDateFormat timeFormat = new SimpleDateFormat(timeFormatString);
        
        String euroJavaNumberFormat = getCurrencyNumberFormat();
        NumberFormat euroNumberFormat = new NumberFormat(euroJavaNumberFormat);
        WritableCellFormat moneyFormat = new WritableCellFormat(
                euroNumberFormat);
        moneyFormat.setWrap(false);
        moneyFormat.setShrinkToFit(false);

        WritableCellFormat wordCountValueRightFormat = new WritableCellFormat();
        wordCountValueRightFormat.setWrap(true);
        wordCountValueRightFormat.setShrinkToFit(false);
        wordCountValueRightFormat.setBorder(jxl.format.Border.RIGHT,
                jxl.format.BorderLineStyle.THICK);

        jxl.format.CellFormat normalFormat = getNormalFormat();
        jxl.format.CellFormat failedNormalFormat = getRedColorFormat();

        WritableCellFormat failed_dateFormat = new WritableCellFormat(
                DateFormats.DEFAULT);
        failed_dateFormat.setWrap(false);
        failed_dateFormat.setShrinkToFit(false);
        
        WritableCellFormat failed_timeFormat = new WritableCellFormat(
                DateFormats.FORMAT6);
        failed_dateFormat.setWrap(false);
        failed_dateFormat.setShrinkToFit(false);
        
        WritableCellFormat failed_moneyFormat = new WritableCellFormat(
                euroNumberFormat);
        failed_moneyFormat.setWrap(false);
        failed_moneyFormat.setShrinkToFit(false);

        WritableCellFormat failed_wordCountValueRightFormat = new WritableCellFormat();
        failed_wordCountValueRightFormat.setWrap(true);
        failed_wordCountValueRightFormat.setShrinkToFit(false);
        failed_wordCountValueRightFormat.setBorder(jxl.format.Border.RIGHT,
                jxl.format.BorderLineStyle.THICK);

        failed_dateFormat.setBackground(jxl.format.Colour.RED);
        failed_moneyFormat.setBackground(jxl.format.Colour.RED);
        failed_wordCountValueRightFormat.setBackground(jxl.format.Colour.RED);

        WritableCellFormat moneyRightFormat = new WritableCellFormat(
                moneyFormat);
        moneyRightFormat.setBorder(jxl.format.Border.RIGHT,
                jxl.format.BorderLineStyle.THICK);

        WritableCellFormat langFormat = new WritableCellFormat();
        langFormat.setWrap(true);
        langFormat.setShrinkToFit(false);
        langFormat.setBorder(jxl.format.Border.RIGHT,
                jxl.format.BorderLineStyle.THICK);

        WritableFont wrongJobFont = new WritableFont(WritableFont.TIMES, 11,
                WritableFont.NO_BOLD, false, UnderlineStyle.NO_UNDERLINE,
                jxl.format.Colour.GRAY_50);

        WritableCellFormat wrongJobFormat = new WritableCellFormat(wrongJobFont);

        while (projectIter.hasNext())
        {
            String jobId = projectIter.next().toString();
            boolean isWrongJob = p_data.wrongJobNames.contains(jobId);
            HashMap<String, ProjectWorkflowData> localeMap = p_projectMap
                    .get(jobId);
            ArrayList<String> locales = new ArrayList<String>(localeMap
                    .keySet());
            Collections.sort(locales);
            Iterator<String> localeIter = locales.iterator();
            BigDecimal projectTotalWordCountCost = new BigDecimal(
                    BIG_DECIMAL_ZERO_STRING);
            BigDecimal reviewTotalWordCountCost = new BigDecimal(
                    BIG_DECIMAL_ZERO_STRING);

            // Counts the review costing total and the translation costing
            // total.
            String localeName = null;
            ProjectWorkflowData data = null;

            // Marks whether needs to add a blank line.
            boolean containsDellReview = false;

            while (localeIter.hasNext())
            {
                int col = 0;
                localeName = (String) localeIter.next();
                data = (ProjectWorkflowData) localeMap.get(localeName);

                int row = p_rows[MONTH_SHEET].getValue();

                WritableCellFormat temp_moneyFormat = moneyFormat;
                jxl.format.CellFormat temp_normalFormat = normalFormat;

                if (data.wasExportFailed)
                {
                    temp_moneyFormat = failed_moneyFormat;
                    temp_normalFormat = failedNormalFormat;
                }
                if (isWrongJob)
                {
                    // For "Add Job Id into online job report" issue
                    if (isJobIdVisible)
                    {
                        p_sheets[MONTH_SHEET].addCell(new Label(col++, row,
                                jobId, wrongJobFormat));
                    }

                    p_sheets[MONTH_SHEET].addCell(new Label(col++, row,
                            data.jobName, wrongJobFormat));
                }
                else
                {
                    // For "Add Job Id into online job report" issue
                    if (isJobIdVisible)
                    {
                        p_sheets[MONTH_SHEET].addCell(new Label(col++, row,
                                jobId, temp_normalFormat));
                    }
                    p_sheets[MONTH_SHEET].addCell(new Label(col++, row,
                            data.jobName, temp_normalFormat));
                }

                p_sheets[MONTH_SHEET].setColumnView(col - 1, 50);
                p_sheets[MONTH_SHEET].addCell(new Label(col++, row,
                        data.projectDesc, temp_normalFormat));
                p_sheets[MONTH_SHEET].setColumnView(col - 1, 22);

                if (data.wasExportFailed)
                {
                    p_sheets[MONTH_SHEET].addCell(new DateTime(col++, row,
                            data.creationDate, failed_dateFormat));
                    p_sheets[MONTH_SHEET].setColumnView(col - 1, 15);
                    p_sheets[MONTH_SHEET].addCell(new DateTime(col++, row,
                            data.creationDate, failed_timeFormat));
                    p_sheets[MONTH_SHEET].setColumnView(col - 1, 18);
                    p_sheets[MONTH_SHEET].addCell(new DateTime(col++, row,
                            data.exportDate, failed_dateFormat));
                    p_sheets[MONTH_SHEET].setColumnView(col - 1, 15);
                    p_sheets[MONTH_SHEET].addCell(new DateTime(col++, row,
                            data.exportDate, failed_timeFormat));
                }
                else
                {
                    p_sheets[MONTH_SHEET].addCell(new Label(col++, row,
                            dateFormat.format(data.creationDate)));
                    p_sheets[MONTH_SHEET].setColumnView(col - 1, 15);
                    p_sheets[MONTH_SHEET].addCell(new Label(col++, row,
                            timeFormat.format(data.creationDate)));
                    p_sheets[MONTH_SHEET].setColumnView(col - 1, 18);
                    String labelExportDate = "";
                    if (data.wasExported)
                    {
                        labelExportDate = dateFormat.format(data.exportDate);
                    }
                    p_sheets[MONTH_SHEET].addCell(new Label(col++, row,
                            labelExportDate));
                    p_sheets[MONTH_SHEET].setColumnView(col - 1, 15);
                    String labelExportTime = "";
                    if (data.wasExported)
                    {
                        labelExportTime = timeFormat.format(data.exportDate);
                    }
                    p_sheets[MONTH_SHEET].addCell(new Label(col++, row,
                            labelExportTime));
                    
                }
                //status
                p_sheets[MONTH_SHEET].setColumnView(col - 1, 18);
                p_sheets[MONTH_SHEET].addCell(new Label(col++, row,
                        data.status, temp_normalFormat));
                
                p_sheets[MONTH_SHEET].setColumnView(col - 1, 18);
                p_sheets[MONTH_SHEET].addCell(new Label(col++, row,
                        data.targetLang, temp_normalFormat));
                p_sheets[MONTH_SHEET].addCell(new Number(col++, row,
                        data.repetitionWordCount, temp_normalFormat));

                int numwidth = 10;
                p_sheets[MONTH_SHEET].setColumnView(col - 1, numwidth);
                p_sheets[MONTH_SHEET].addCell(new Number(col++, row,
                        data.segmentTmWordCount, temp_normalFormat));
                p_sheets[MONTH_SHEET].setColumnView(col - 1, numwidth);

                if (p_data.useInContext)
                {
                    p_sheets[MONTH_SHEET].addCell(new Number(col++, row,
                            data.inContextMatchWordCount, temp_normalFormat));
                    p_sheets[MONTH_SHEET].setColumnView(col - 1, numwidth);
                }
                
                if(p_data.useDefaultContext)
                {
                    p_sheets[MONTH_SHEET].addCell(new Number(col++, row,
                            data.contextMatchWordCount, temp_normalFormat));
                    p_sheets[MONTH_SHEET].setColumnView(col - 1, numwidth);
                }

                p_sheets[MONTH_SHEET].addCell(new Number(col++, row,
                        data.fuzzyMatchWordCount, temp_normalFormat));
                p_sheets[MONTH_SHEET].setColumnView(col - 1, numwidth);

                p_sheets[MONTH_SHEET].addCell(new Number(col++, row,
                        data.noMatchWordCount, temp_normalFormat));
                p_sheets[MONTH_SHEET].setColumnView(col - 1, numwidth);
                p_sheets[MONTH_SHEET].addCell(new Number(col++, row,
                        data.totalWordCount, temp_normalFormat));
                p_sheets[MONTH_SHEET].setColumnView(col - 1, numwidth);

                int moneywidth = 12;
                p_sheets[MONTH_SHEET].addCell(new Number(col++, row,
                        asDouble(data.repetitionWordCountCost),
                        temp_moneyFormat));
                p_sheets[MONTH_SHEET].setColumnView(col - 1, moneywidth);
                p_sheets[MONTH_SHEET]
                        .addCell(new Number(col++, row,
                                asDouble(data.segmentTmWordCountCost),
                                temp_moneyFormat));
                p_sheets[MONTH_SHEET].setColumnView(col - 1, moneywidth);
                if (p_data.useInContext)
                {
                    p_sheets[MONTH_SHEET].addCell(new Number(col++, row,
                            asDouble(data.inContextMatchWordCountCost),
                            temp_moneyFormat));
                    p_sheets[MONTH_SHEET].setColumnView(col - 1, moneywidth);
                }
                if(p_data.useDefaultContext)
                {
                    p_sheets[MONTH_SHEET].addCell(new Number(col++, row,
                            asDouble(data.contextMatchWordCountCost),
                            temp_moneyFormat));
                    p_sheets[MONTH_SHEET].setColumnView(col - 1, moneywidth);
                }
                p_sheets[MONTH_SHEET].addCell(new Number(col++, row,
                        asDouble(data.fuzzyMatchWordCountCost),
                        temp_moneyFormat));
                p_sheets[MONTH_SHEET].setColumnView(col - 1, moneywidth);

                p_sheets[MONTH_SHEET].addCell(new Number(col++, row,
                        asDouble(data.noMatchWordCountCost), temp_moneyFormat));
                p_sheets[MONTH_SHEET].setColumnView(col - 1, moneywidth);
                p_sheets[MONTH_SHEET].addCell(new Number(col++, row,
                        asDouble(data.totalWordCountCost), temp_moneyFormat));
                p_sheets[MONTH_SHEET].setColumnView(col - 1, moneywidth);
                projectTotalWordCountCost = projectTotalWordCountCost
                        .add(data.totalWordCountCost);
                
                if (totalCol == null)
                {
                    totalCol = ReportUtil.toChar(col - 1);
                }
                
                List<Integer> indexs = totalCost.get(data.targetLang);
                if (indexs == null)
                {
                    indexs = new ArrayList<Integer>();
                    totalCost.put(data.targetLang, indexs);
                }
                indexs.add(row + 1);
                
                Double value = totalCostDate.get(data.targetLang);
                if (value ==  null)
                {
                    value = 0.0;
                }
                value += asDouble(data.totalWordCountCost);
                totalCostDate.put(data.targetLang, value);

                // add a "project total" summary cost over the locales
                // map(1,moneyFormat)
                if (localeIter.hasNext() == false)
                {
                    p_sheets[MONTH_SHEET].addCell(new Number(col++, row,
                            asDouble(projectTotalWordCountCost),
                            temp_moneyFormat));
                }
                else
                {
                    p_sheets[MONTH_SHEET].addCell(new Blank(col++, row,
                            temp_moneyFormat));
                }
                p_sheets[MONTH_SHEET].setColumnView(col - 1, moneywidth);
                p_rows[MONTH_SHEET].inc();

                // Write data into MONTH_REVIEW_SHEET
                if (p_includeExReview && data.containsDellReview)
                {
                    containsDellReview = true;

                    row = p_rows[MONTH_REVIEW_SHEET].getValue();
                    col = 0;

                    if (isWrongJob)
                    {
                        // For "Job id not showing in external review tabs"
                        // Issue
                        if (isJobIdVisible)
                        {
                            p_sheets[MONTH_REVIEW_SHEET].addCell(new Label(
                                    col++, row, data.jobId, wrongJobFormat));
                        }
                        p_sheets[MONTH_REVIEW_SHEET].addCell(new Label(col++,
                                row, data.jobName, wrongJobFormat));
                    }
                    else
                    {
                        // For "Job id not showing in external review tabs"
                        // Issue
                        if (isJobIdVisible)
                        {
                            p_sheets[MONTH_REVIEW_SHEET].addCell(new Label(
                                    col++, row, data.jobId, temp_normalFormat));
                        }
                        p_sheets[MONTH_REVIEW_SHEET].addCell(new Label(col++,
                                row, data.jobName, temp_normalFormat));
                    }
                    p_sheets[MONTH_REVIEW_SHEET].setColumnView(col - 1, 50);
                    p_sheets[MONTH_REVIEW_SHEET].addCell(new Label(col++, row,
                            data.projectDesc, temp_normalFormat));
                    p_sheets[MONTH_REVIEW_SHEET].setColumnView(col - 1, 22);

                    if (data.wasExportFailed)
                    {
                        /*p_sheets[MONTH_SHEET].addCell(new DateTime(col++, row,
                                data.creationDate, failed_dateFormat));*/
                        p_sheets[MONTH_REVIEW_SHEET].addCell(new DateTime(col++, row,
                                data.creationDate, failed_dateFormat));
                        p_sheets[MONTH_REVIEW_SHEET].setColumnView(col - 1, 15);
                        p_sheets[MONTH_REVIEW_SHEET].addCell(new DateTime(col++, row,
                                data.creationDate, failed_timeFormat));
                        p_sheets[MONTH_REVIEW_SHEET].setColumnView(col - 1, 18);
                        p_sheets[MONTH_REVIEW_SHEET].addCell(new DateTime(col++, row,
                                data.exportDate, failed_dateFormat));
                        p_sheets[MONTH_REVIEW_SHEET].setColumnView(col - 1, 15);
                        p_sheets[MONTH_REVIEW_SHEET].addCell(new DateTime(col++, row,
                                data.exportDate, failed_timeFormat));
                    }
                    else
                    {
                        /*p_sheets[MONTH_SHEET].addCell(new Label(col++, row,
                                dateFormat.format(data.creationDate)));*/
                        p_sheets[MONTH_REVIEW_SHEET].addCell(new Label(col++, row,
                                dateFormat.format(data.creationDate)));
                        p_sheets[MONTH_REVIEW_SHEET].setColumnView(col - 1, 15);
                        p_sheets[MONTH_REVIEW_SHEET].addCell(new Label(col++, row,
                                timeFormat.format(data.creationDate)));
                        p_sheets[MONTH_REVIEW_SHEET].setColumnView(col - 1, 18);
                        String labelExportDate = "";
                        if (data.wasExported)
                        {
                            labelExportDate = dateFormat.format(data.exportDate);
                        }
                        p_sheets[MONTH_REVIEW_SHEET].addCell(new Label(col++, row,
                                labelExportDate));
                        p_sheets[MONTH_REVIEW_SHEET].setColumnView(col - 1, 15);
                        String labelExportTime = "";
                        if (data.wasExported)
                        {
                            labelExportTime = timeFormat.format(data.exportDate);
                        }
                        p_sheets[MONTH_REVIEW_SHEET].addCell(new Label(col++, row,
                                labelExportTime));
                        
                    }
                    p_sheets[MONTH_REVIEW_SHEET].setColumnView(col - 1, 18);
                    p_sheets[MONTH_REVIEW_SHEET].addCell(new Label(col++, row,
                            data.targetLang, temp_normalFormat));
                    p_sheets[MONTH_REVIEW_SHEET].addCell(new Number(col++, row,
                            data.repetitionWordCount, temp_normalFormat));

                    numwidth = 10;
                    p_sheets[MONTH_REVIEW_SHEET].setColumnView(col - 1,
                            numwidth);
                    p_sheets[MONTH_REVIEW_SHEET].addCell(new Number(col++, row,
                            data.segmentTmWordCount, temp_normalFormat));
                    p_sheets[MONTH_REVIEW_SHEET].setColumnView(col - 1,
                            numwidth);

                    if (p_data.useInContext)
                    {
                        p_sheets[MONTH_REVIEW_SHEET].addCell(new Number(col++,
                                row, data.inContextMatchWordCount,
                                temp_normalFormat));
                        p_sheets[MONTH_REVIEW_SHEET].setColumnView(col - 1,
                                numwidth);
                    }
                    if(p_data.useDefaultContext)
                    {
                        p_sheets[MONTH_REVIEW_SHEET].addCell(new Number(col++,
                                row, data.contextMatchWordCount,
                                temp_normalFormat));
                        p_sheets[MONTH_REVIEW_SHEET].setColumnView(col - 1,
                                numwidth);
                    }

                    p_sheets[MONTH_REVIEW_SHEET].addCell(new Number(col++, row,
                            data.fuzzyMatchWordCount, temp_normalFormat));
                    p_sheets[MONTH_REVIEW_SHEET].setColumnView(col - 1,
                            numwidth);

                    p_sheets[MONTH_REVIEW_SHEET].addCell(new Number(col++, row,
                            data.noMatchWordCount, temp_normalFormat));
                    p_sheets[MONTH_REVIEW_SHEET].setColumnView(col - 1,
                            numwidth);
                    p_sheets[MONTH_REVIEW_SHEET].addCell(new Number(col++, row,
                            data.totalWordCount, temp_normalFormat));
                    p_sheets[MONTH_REVIEW_SHEET].setColumnView(col - 1,
                            numwidth);

                    moneywidth = 12;
                    p_sheets[MONTH_REVIEW_SHEET]
                            .addCell(new Number(
                                    col++,
                                    row,
                                    asDouble(data.repetitionWordCountCostForDellReview),
                                    temp_moneyFormat));
                    p_sheets[MONTH_REVIEW_SHEET].setColumnView(col - 1,
                            moneywidth);
                    p_sheets[MONTH_REVIEW_SHEET].addCell(new Number(col++, row,
                            asDouble(data.segmentTmWordCountCostForDellReview),
                            temp_moneyFormat));
                    p_sheets[MONTH_REVIEW_SHEET].setColumnView(col - 1,
                            moneywidth);

                    if (p_data.useInContext)
                    {
                        p_sheets[MONTH_REVIEW_SHEET]
                                .addCell(new Number(
                                        col++,
                                        row,
                                        asDouble(data.inContextMatchWordCountCostForDellReview),
                                        temp_moneyFormat));
                        p_sheets[MONTH_REVIEW_SHEET].setColumnView(col - 1,
                                moneywidth);
                    }

                    if(p_data.useDefaultContext)
                    {
                        p_sheets[MONTH_REVIEW_SHEET]
                                 .addCell(new Number(
                                         col++,
                                         row,
                                         asDouble(data.contextMatchWordCountCostForDellReview),
                                         temp_moneyFormat));
                         p_sheets[MONTH_REVIEW_SHEET].setColumnView(col - 1,
                                 moneywidth);
                    }
                    
                    p_sheets[MONTH_REVIEW_SHEET]
                            .addCell(new Number(
                                    col++,
                                    row,
                                    asDouble(data.fuzzyMatchWordCountCostForDellReview),
                                    temp_moneyFormat));
                    p_sheets[MONTH_REVIEW_SHEET].setColumnView(col - 1,
                            moneywidth);

                    p_sheets[MONTH_REVIEW_SHEET].addCell(new Number(col++, row,
                            asDouble(data.noMatchWordCountCostForDellReview),
                            temp_moneyFormat));
                    p_sheets[MONTH_REVIEW_SHEET].setColumnView(col - 1,
                            moneywidth);

                    reviewTotalWordCountCost = reviewTotalWordCountCost
                            .add(data.totalWordCountCostForDellReview);

                    // Writes the "Translation Total" column.
                    p_sheets[MONTH_REVIEW_SHEET]
                            .addCell(new Number(col++, row,
                                    asDouble(data.totalWordCountCost),
                                    temp_moneyFormat));
                    p_sheets[MONTH_REVIEW_SHEET].setColumnView(col - 1,
                            moneywidth);
                    // Writes the "Review" column.
                    p_sheets[MONTH_REVIEW_SHEET].addCell(new Number(col++, row,
                            asDouble(data.totalWordCountCostForDellReview),
                            temp_moneyFormat));
                    p_sheets[MONTH_REVIEW_SHEET].setColumnView(col - 1,
                            moneywidth);

                    // add "job total" cost over the locales
                    if (localeIter.hasNext() == false)
                    {
                        p_sheets[MONTH_REVIEW_SHEET].addCell(new Number(col++,
                                row, asDouble(projectTotalWordCountCost
                                        .add(reviewTotalWordCountCost)),
                                temp_moneyFormat));
                    }
                    else
                    {
                        p_sheets[MONTH_REVIEW_SHEET].addCell(new Blank(col++,
                                row, temp_moneyFormat));
                    }
                    p_sheets[MONTH_REVIEW_SHEET].setColumnView(col - 1,
                            moneywidth);
                    p_rows[MONTH_REVIEW_SHEET].inc();
                }// End if (p_includeExReview && data.containsDellReview)
            }// End loop while (localeIter.hasNext())

            p_rows[MONTH_SHEET].inc();
            if (p_includeExReview && containsDellReview)
            {
                p_rows[MONTH_REVIEW_SHEET].inc();
            }

        }// End loop while (projectIter.hasNext())

        p_rows[MONTH_SHEET].inc();
        addTotals(p_sheets[MONTH_SHEET], MONTH_SHEET, p_rows[MONTH_SHEET],
                p_data, bundle);

        if (p_includeExReview)
        {
            p_rows[MONTH_REVIEW_SHEET].inc();
            addTotals(p_sheets[MONTH_REVIEW_SHEET], MONTH_REVIEW_SHEET,
                    p_rows[MONTH_REVIEW_SHEET], p_data, bundle);
        }
        
        if (totalCol != null)
        {
            addTotalsPerLang(p_sheets[MONTH_SHEET], MONTH_SHEET, p_rows[MONTH_SHEET],
                    p_data, bundle);
        }
    }
    
    private void addTotalsPerLang(WritableSheet p_sheet, final int p_sheetCategory,
            IntHolder p_row, MyData p_data, ResourceBundle bundle) throws Exception
    {
        WritableFont subTotalFont = new WritableFont(WritableFont.ARIAL, 10,
                WritableFont.BOLD, false, UnderlineStyle.NO_UNDERLINE,
                jxl.format.Colour.BLACK);
        WritableCellFormat subTotalFormat = new WritableCellFormat(subTotalFont);
        subTotalFormat.setShrinkToFit(false);
        String title = bundle.getString("lb_total_cost_per_lang");
        
        int row = p_row.getValue() + 4; // skip a row
        ArrayList<String> locales = new ArrayList<String>(totalCost
                .keySet());
        Collections.sort(locales);
        
        p_sheet.addCell(new Label(7, row, title, subTotalFormat));
        
        File imgFile = File.createTempFile("img", ".png");
        JfreeCharUtil t = new JfreeCharUtil();
        JfreeCharUtil.drawPieChart2D("", totalCostDate, imgFile);
        WritableImage img = new WritableImage(12, row - 1, 7, 25, imgFile);
        p_sheet.addImage(img);
        
        WritableCellFormat moneyFormat = getMoneyFormat();
        
        for (String locale : locales)
        {
            List<Integer> indexs = totalCost.get(locale);
            StringBuffer values = new StringBuffer();
            for (Integer index : indexs)
            {
                if (values.length() == 0)
                {
                    values.append("SUM(");
                }
                else
                {
                    values.append("+");
                }
                
                values.append(totalCol).append(index);
            }
            values.append(")");
            
            p_sheet.addCell(new Label(9, row, locale));
            p_sheet.addCell(new Formula(10, row++, values.toString(), moneyFormat));
        }       
    }
    
    private WritableCellFormat getMoneyFormat() throws WriteException
    {
           String euroJavaNumberFormat = getCurrencyNumberFormat();
            NumberFormat euroNumberFormat = new NumberFormat(euroJavaNumberFormat);
            
           WritableCellFormat moneyFormat = new WritableCellFormat(
                    euroNumberFormat);
            moneyFormat.setWrap(false);
            moneyFormat.setShrinkToFit(false);
            
            return moneyFormat;
    }

    private void addTotals(WritableSheet p_sheet, final int p_sheetCategory,
            IntHolder p_row, MyData p_data, ResourceBundle bundle) throws Exception
    {
        int row = p_row.getValue() + 1; // skip a row
        WritableFont subTotalFont = new WritableFont(WritableFont.ARIAL, 10,
                WritableFont.BOLD, false, UnderlineStyle.NO_UNDERLINE,
                jxl.format.Colour.BLACK);

        WritableCellFormat subTotalFormat = new WritableCellFormat(subTotalFont);
        subTotalFormat.setWrap(true);
        subTotalFormat.setShrinkToFit(false);
        subTotalFormat.setBorder(jxl.format.Border.TOP,
                jxl.format.BorderLineStyle.THIN);
        subTotalFormat.setBorder(jxl.format.Border.BOTTOM,
                jxl.format.BorderLineStyle.THIN);
        String title = bundle.getString("lb_totals");
        
        subTotalFormat.setBackground(jxl.format.Colour.GRAY_25);

        // java.text.DecimalFormat dformat = (java.text.DecimalFormat)
        java.text.NumberFormat.getCurrencyInstance(Locale.US);

        WritableCellFormat moneyFormat = getMoneyFormat();
        moneyFormat.setBackground(jxl.format.Colour.GRAY_25);
        moneyFormat.setBorder(jxl.format.Border.TOP,
                jxl.format.BorderLineStyle.THIN);
        moneyFormat.setBorder(jxl.format.Border.BOTTOM,
                jxl.format.BorderLineStyle.THIN);

        // For "Add Job Id into online job report" issue
        char sumStartCellCol = 'I';
        int c = 8;
        if (isJobIdVisible)
        {
            sumStartCellCol = 'J';
            c = 9;
        }

        p_sheet.addCell(new Label(0, row, title, subTotalFormat));
        // modify the number 3 to "sumStartCellCol-B" for "Add Job Id into
        // online job report" issue
        p_sheet.mergeCells(0, row, sumStartCellCol - 'B', row);
        int lastRow = p_row.getValue() - 2;

        // add in word count totals
        // word counts

        // For "Add job id in online report issue"
        p_sheet.addCell(new Formula(c++, row, "SUM(" + sumStartCellCol + "5:"
                + sumStartCellCol + lastRow + ")", subTotalFormat));
        sumStartCellCol++;
        p_sheet.addCell(new Formula(c++, row, "SUM(" + sumStartCellCol + "5:"
                + sumStartCellCol + lastRow + ")", subTotalFormat));
        sumStartCellCol++;
        p_sheet.addCell(new Formula(c++, row, "SUM(" + sumStartCellCol + "5:"
                + sumStartCellCol + lastRow + ")", subTotalFormat));
        sumStartCellCol++;
        p_sheet.addCell(new Formula(c++, row, "SUM(" + sumStartCellCol + "5:"
                + sumStartCellCol + lastRow + ")", subTotalFormat));
        sumStartCellCol++;
        p_sheet.addCell(new Formula(c++, row, "SUM(" + sumStartCellCol + "5:"
                + sumStartCellCol + lastRow + ")", subTotalFormat));
        sumStartCellCol++;

        if (p_data.useInContext)
        {
            p_sheet.addCell(new Formula(c++, row, "SUM(" + sumStartCellCol
                    + "5:" + sumStartCellCol + lastRow + ")", subTotalFormat));
            sumStartCellCol++;
        }
        if(p_data.useDefaultContext)
        {
              p_sheet.addCell(new Formula(c++, row, "SUM(" + sumStartCellCol
                        + "5:" + sumStartCellCol + lastRow + ")", subTotalFormat));
                sumStartCellCol++;
        }

        // word count costs
        p_sheet.addCell(new Formula(c++, row, "SUM(" + sumStartCellCol + "5"
                + ":" + sumStartCellCol + lastRow + ")", moneyFormat));
        sumStartCellCol++;
        p_sheet.addCell(new Formula(c++, row, "SUM(" + sumStartCellCol + "5"
                + ":" + sumStartCellCol + lastRow + ")", moneyFormat));
        sumStartCellCol++;
        p_sheet.addCell(new Formula(c++, row, "SUM(" + sumStartCellCol + "5"
                + ":" + sumStartCellCol + lastRow + ")", moneyFormat));
        sumStartCellCol++;
        p_sheet.addCell(new Formula(c++, row, "SUM(" + sumStartCellCol + "5"
                + ":" + sumStartCellCol + lastRow + ")", moneyFormat));
        sumStartCellCol++;
        p_sheet.addCell(new Formula(c++, row, "SUM(" + sumStartCellCol + "5"
                + ":" + sumStartCellCol + lastRow + ")", moneyFormat));
        sumStartCellCol++;
        p_sheet.addCell(new Formula(c++, row, "SUM(" + sumStartCellCol + "5"
                + ":" + sumStartCellCol + lastRow + ")", moneyFormat));
        sumStartCellCol++;

        if (p_data.useInContext)
        {
            p_sheet
                    .addCell(new Formula(c++, row, "SUM(" + sumStartCellCol
                            + "5" + ":" + sumStartCellCol + lastRow + ")",
                            moneyFormat));
            sumStartCellCol++;
        }
        if(p_data.useDefaultContext)
        {
            p_sheet
            .addCell(new Formula(c++, row, "SUM(" + sumStartCellCol
                    + "5" + ":" + sumStartCellCol + lastRow + ")",
                    moneyFormat));
            sumStartCellCol++;
        }
        if (p_sheetCategory == MONTH_REVIEW_SHEET)
        {
            // For "Job id not showing in external review tabs" Issue
            p_sheet
                    .addCell(new Formula(c++, row, "SUM(" + sumStartCellCol
                            + "5" + ":" + sumStartCellCol + lastRow + ")",
                            moneyFormat));
        }

        // add an extra column for Dell Tracking Use
        p_sheet.addCell(new Label(c++, row, "", moneyFormat));
    }

    private double asDouble(BigDecimal d)
    {
        // BigDecimal v = d.setScale(SCALE_EXCEL,BigDecimal.ROUND_HALF_UP);
        return d.doubleValue();
    }

    /**
     * Adds the given float to the BigDecimal after scaling it to 3(SCALE)
     * decimal points of precision and rounding half up. If you don't do this,
     * then the float 0.255 will become 0.254999995231628 Returns a new
     * BigDecimal which is the sum of a and p_f
     */
    private BigDecimal add(BigDecimal p_a, float p_f)
    {
        String floatString = Float.toString(p_f);
        BigDecimal bd = new BigDecimal(floatString);
        BigDecimal sbd = bd.setScale(SCALE, BigDecimal.ROUND_HALF_UP);
        return p_a.add(sbd);
    }

    /**
     * Returns a list of jobs that are in a different project but should be
     * treated as if they're in this project.
     */
    private void getJobsInWrongProject(MyData p_data)
    {
        try
        {
            p_data.wrongJobs.addAll(readJobNames(p_data));
            Iterator<Job> iter = p_data.wrongJobs.iterator();
            while (iter.hasNext())
            {
                Job j = (Job) iter.next();
                // p_data.wrongJobNames.add(j.getJobName());
                p_data.wrongJobNames.add(Long.toString(j.getId()));
            }
        }
        catch (Exception e)
        {
            s_logger.error(
                    "Failed to add jobs which are in the 'wrong' project.", e);
        }
    }

    /**
     * Opens up the file "jobsInWrongDivision.txt" and returns only the jobs
     * that should be mapped to the requested projects
     */
    private ArrayList<Job> readJobNames(MyData p_data) throws Exception
    {
        ArrayList<Job> wrongJobs = new ArrayList<Job>();
        SystemConfiguration sc = SystemConfiguration.getInstance();
        StringBuffer mapFile = new StringBuffer(
                sc
                        .getStringParameter(SystemConfigParamNames.GLOBALSIGHT_HOME_DIRECTORY));
        mapFile.append(File.separator);
        mapFile.append("jobsInWrongDivision.txt");
        File f = new File(mapFile.toString());
        if (f.exists() == false)
        {
            if (p_data.warnedAboutMissingWrongJobsFile == false)
            {
                s_logger.info("jobsInWrongDivision.txt file not found.");
                p_data.warnedAboutMissingWrongJobsFile = true;
            }
            return wrongJobs;
        }
        BufferedReader reader = new BufferedReader(new FileReader(f));
        String line = null;

        while ((line = reader.readLine()) != null)
        {
            if (line.startsWith("#") || line.length() == 0) continue;
            String jobname = null;
            String projectname = null;
            try
            {
                // the format is jobname = projectname
                String[] tokens = line.split("=");
                jobname = tokens[0].trim();
                projectname = tokens[1].trim();
                Project p = ServerProxy.getProjectHandler().getProjectByName(
                        projectname);
                Long newProjectId = p.getIdAsLong();

                JobSearchParameters sp = new JobSearchParameters();
                sp.setJobName(jobname);
                ArrayList<Job> jobs = new ArrayList<Job>(ServerProxy
                        .getJobHandler().getJobs(sp));
                Job j = (Job) jobs.get(0);

                if (p_data.wantsAllProjects == false
                        && p_data.projectIdList.contains(newProjectId) == false)
                {
                    p_data.ignoreJobs.add(j);
                    continue;
                }

                // but if the wrong job is actually currently in a project we
                // are reporting on, then skip it as well
                // if the project id list contains the old project but not the
                // new project
                Long oldProjectId = j.getL10nProfile().getProject()
                        .getIdAsLong();
                if (p_data.projectIdList.contains(oldProjectId)
                        && !p_data.projectIdList.contains(newProjectId))
                {
                    p_data.ignoreJobs.add(j);
                    continue;
                }

                // add the Job to the job list, and add the mapping for job id
                // to project
                wrongJobs.add(j);
                p_data.wrongJobMap.put(new Long(j.getId()), p);
            }
            catch (Exception e)
            {
                s_logger
                        .warn("Ignoring mapping line for "
                                + jobname
                                + " => "
                                + projectname
                                + ". Either the job doesn't exist, the project doesn't exist, or both.");
            }
        }
        reader.close();
        return wrongJobs;
    }

    // see if the job is in the wrong job list, if so, use it's new project
    // and not the original one
    private String getProjectDesc(MyData p_data, Job p_job)
    {
        Project p = (Project) p_data.wrongJobMap.get(new Long(p_job.getId()));
        if (p == null) p = p_job.getL10nProfile().getProject();
        String d = p.getDescription();
        String desc = null;
        if (d == null || d.length() == 0)
            desc = p.getName();
        else
            desc = p.getName() + ": " + d;
        return desc;
    }

    private ArrayList<Job> getWrongJobsForThisMonth(MyData p_data, int p_month,
            int p_year)
    {
        ArrayList<Job> wrongJobsThisMonth = new ArrayList<Job>();
        Iterator<?> iter = p_data.wrongJobs.iterator();
        Calendar cal = Calendar.getInstance();
        while (iter.hasNext())
        {
            Job j = (Job) iter.next();
            cal.setTime(j.getCreateDate());
            if (p_month == cal.get(Calendar.MONTH)
                    && p_year == cal.get(Calendar.YEAR))
            {
                wrongJobsThisMonth.add(j);
            }
        }
        return wrongJobsThisMonth;
    }

    /**
     * For "Add Job Id into online job report" issue set the value of
     * "isJobIdVisible" for control display the job id whether or not.
     */
    private boolean isJobIdVisible(HttpServletRequest request)
    {
        String requestJobIdVisible = request.getParameter("jobIdVisible");
        boolean jobIdVisible = false;
        if (requestJobIdVisible != null && requestJobIdVisible.equals("true"))
        {
            jobIdVisible = true;
        }
        return jobIdVisible;
    }

    private boolean getUseInContext(HashSet<Job> jobs)
    {
        for (Job job : jobs)
        {
            if (PageHandler.isInContextMatch(job))
            {
                return true;
            }
        }

        return false;
    }
    
    private boolean getUseDefaultContext(HashSet<Job> jobs)
    {
        for(Job job : jobs)
        {
            if(PageHandler.isDefaultContextMatch(job))
            {
                return true;
            }
        }
        return false;
    }

    private String getCurrencyNumberFormat()
    {
        return symbol + "###,###,##0.000;(" + symbol + "###,###,##0.000)";
    }

}