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
package com.globalsight.everest.webapp.pagehandler.administration.reports.generator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.math.BigDecimal;
import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

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

import org.apache.log4j.Logger;

import com.globalsight.cxe.entity.fileprofile.FileProfile;
import com.globalsight.cxe.entity.knownformattype.KnownFormatType;
import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.costing.BigDecimalHelper;
import com.globalsight.everest.costing.Cost;
import com.globalsight.everest.costing.CostByWordCount;
import com.globalsight.everest.costing.CostingException;
import com.globalsight.everest.costing.Currency;
import com.globalsight.everest.costing.FlatSurcharge;
import com.globalsight.everest.costing.PercentageSurcharge;
import com.globalsight.everest.costing.Surcharge;
import com.globalsight.everest.foundation.L10nProfile;
import com.globalsight.everest.foundation.SearchCriteriaParameters;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.jobhandler.JobSearchParameters;
import com.globalsight.everest.projecthandler.Project;
import com.globalsight.everest.projecthandler.TranslationMemoryProfile;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.taskmanager.Task;
import com.globalsight.everest.util.system.SystemConfigParamNames;
import com.globalsight.everest.util.system.SystemConfiguration;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.pagehandler.administration.reports.ReportConstants;
import com.globalsight.everest.webapp.pagehandler.administration.reports.ReportHelper;
import com.globalsight.everest.webapp.pagehandler.administration.reports.XlsReports;
import com.globalsight.everest.webapp.pagehandler.administration.reports.util.ReportUtil;
import com.globalsight.everest.webapp.pagehandler.administration.users.UserUtil;
import com.globalsight.everest.webapp.pagehandler.projects.workflows.JobSearchConstants;
import com.globalsight.everest.workflowmanager.Workflow;
import com.globalsight.util.AmbFileStoragePathUtils;
import com.globalsight.util.GeneralException;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.IntHolder;
import com.globalsight.util.JfreeCharUtil;
import com.globalsight.util.SortUtil;

/**
 * Online Jobs Report Generator
 * 
 * @Date Feb 14, 2012
 */
public class OnlineJobsReportGenerator extends XlsReports implements
        ReportGenerator
{
    private static Logger logger = Logger
            .getLogger(OnlineJobsReportGenerator.class.getName());

    // defines a 0 format for a 3 decimal precision point BigDecimal
    private static final String BIG_DECIMAL_ZERO_STRING = "0.000";

    // For "Dell Review Report Issue".
    private static String DELL_REVIEW = "Dell_Review";

    private static ResourceBundle m_bundle;

    private static final int MONTH_SHEET = 0;

    private static final int MONTH_REVIEW_SHEET = 1;

    // For "Add Job Id into online job report" issue
    private boolean isJobIdVisible = true;

    // the big decimal scale to use for internal math
    private static int SCALE = 3;

    private Calendar m_calendar = Calendar.getInstance();

    /* The symbol of the currency from the request */
    private String symbol = null;

    private Hashtable<String, List<Integer>> totalCost = new Hashtable<String, List<Integer>>();
    private HashMap<String, Double> totalCostDate = new HashMap<String, Double>();
    private String totalCol = null;
    static final String SEPARATOR = "!!gs";
    private MyData m_data;
    private HttpServletRequest m_request = null;
    private String m_companyName;
    String m_userId;

    public OnlineJobsReportGenerator(HttpServletRequest p_request,
            HttpServletResponse p_response) throws Exception
    {
        HttpSession session = p_request.getSession();
        m_userId = (String) session.getAttribute(WebAppConstants.USER_NAME);
        m_request = p_request;
        m_bundle = PageHandler.getBundle(p_request.getSession());
        m_companyName = UserUtil.getCurrentCompanyName(p_request);
        CompanyThreadLocal.getInstance().setValue(m_companyName);
        m_data = new MyData();
        m_data.setCurrency(p_request.getParameter("currency"));
        symbol = ReportUtil.getCurrencySymbol(m_data.getCurrency());
        m_data.setTradosStyle(p_request.getParameter("reportStyle"));
        m_data.setDateFormatString(p_request.getParameter("dateFormat"));
        setProjectIdList(p_request, m_data);
        setLocpIdList(p_request, m_data);
        setTargetLocales(p_request, m_data);
        // get all the jobs that were originally imported with the wrong project
        // the users want to pretend that these jobs are in this project
        if (p_request.getParameterValues("status") != null)
            getJobsInWrongProject(m_data);
    }

    public void sendReports(List<Long> p_jobIDS,
            List<GlobalSightLocale> p_targetLocales,
            HttpServletResponse p_response) throws Exception
    {
        File[] reports = generateReports(p_jobIDS, p_targetLocales);
        ReportHelper.sendFiles(reports, getReportType(), p_response);
    }

    @Override
    public File[] generateReports(List<Long> p_jobIDS,
            List<GlobalSightLocale> p_targetLocales) throws Exception
    {
        // Generate Report from HttpServletRequest.
        if (p_jobIDS == null || p_jobIDS.size() == 0)
        {
            return generateReport(m_request);
        }

        File file = ReportHelper.getXLSReportFile(getReportType(), null);
        WorkbookSettings settings = new WorkbookSettings();
        settings.setSuppressWarnings(true);
        WritableWorkbook workbook = Workbook.createWorkbook(file, settings);
        HashSet<Job> jobs = new HashSet<Job>(
                ReportHelper.getJobListByIDS(p_jobIDS));
        m_data.wantsAllLocales = true;
        m_data.wantsAllProjects = true;
        m_data.setDateFormatString("MM/dd/yy hh:mm:ss a z");
        m_data.jobIds = p_jobIDS;
        HashMap<String, HashMap<String, ProjectWorkflowData>> projectMap = reportDataMap(
                jobs, false, false);
        if (projectMap != null && projectMap.size() > 0)
        {
            // Create sheets in Workbook.
            createSheets(workbook, projectMap, false);
            workbook.write();
            workbook.close();
            return new File[]
            { file };
        }
        else
        {
            return new File[0];
        }
    }

    /**
     * Generates the Excel report as a temp file and returns the temp file. The
     * report consists of all jobs for the given PM, this year, on a month by
     * month basis.
     * 
     * @return File
     * @exception Exception
     */
    private File[] generateReport(HttpServletRequest p_request)
            throws Exception
    {
        String[] months = getMonths(m_bundle);
        File file = ReportHelper.getXLSReportFile(getReportType(), null);
        WorkbookSettings settings = new WorkbookSettings();
        settings.setSuppressWarnings(true);
        WritableWorkbook workbook = Workbook.createWorkbook(file, settings);
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
        if (year != m_calendar.get(Calendar.YEAR))
            currentMonth = 11;

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

        // For "Add Job Id into online job report" issue
        isJobIdVisible = isJobIdVisible(p_request);
        String isReportForYear = p_request.getParameter("reportForThisYear");
        String isReportForDetail = p_request.getParameter("reportForDetail");
        // Generate Report By Year
        if (isReportForYear != null && isReportForYear.equals("on"))
        {
            int i = 0;
            for (i = 0; i <= currentMonth; i++)
            {
                // For "Dell Review Report Issue"
                // Add new parameter "includeExReview" to the signature.
                HashMap<String, HashMap<String, ProjectWorkflowData>> projectMap = getProjectDataForMonth(
                        p_request, i, recalculateFinishedWorkflow,
                        includeExReview);

                // Cancel the report.
                if (isCancelled())
                    return new File[0];

                if (projectMap.size() > 0)
                {
                    // This sheets array has two elements.
                    // sheets[MONTH_SHEET], the original monthSheet.
                    // sheets[MONTH_REVIEW_SHEET], new monthly Dell_Review
                    // sheet.
                    WritableSheet[] sheets = new WritableSheet[2];
                    IntHolder[] rows = new IntHolder[2];

                    String sheetTitle = months[i] + " " + year;
                    sheets[MONTH_SHEET] = workbook.createSheet(sheetTitle, i);
                    if (m_data.isTradosStyle())
                    {
                        addHeaderTradosStyle(sheets[MONTH_SHEET], MONTH_SHEET,
                                m_data, m_bundle);
                    }
                    else
                    {
                        addHeader(sheets[MONTH_SHEET], MONTH_SHEET, m_data,
                                m_bundle);
                    }

                    rows[MONTH_SHEET] = new IntHolder(4);

                    if (includeExReview)
                    {
                        String sheetReviewTitle = m_bundle
                                .getString("lb_review")
                                + " "
                                + months[i]
                                + " "
                                + year;
                        sheets[MONTH_REVIEW_SHEET] = workbook.createSheet(
                                sheetReviewTitle, i);
                        if (m_data.isTradosStyle())
                        {
                            addHeaderTradosStyle(sheets[MONTH_REVIEW_SHEET],
                                    MONTH_REVIEW_SHEET, m_data, m_bundle);
                        }
                        else
                        {
                            addHeader(sheets[MONTH_REVIEW_SHEET],
                                    MONTH_REVIEW_SHEET, m_data, m_bundle);
                        }
                        rows[MONTH_REVIEW_SHEET] = new IntHolder(4);
                    }

                    if (m_data.isTradosStyle())
                    {
                        writeProjectDataTradosStyle(projectMap, sheets,
                                includeExReview, rows, m_data, m_bundle);
                    }
                    else
                    {
                        writeProjectData(projectMap, sheets, includeExReview,
                                rows, m_data, m_bundle);
                    }
                    wroteSomething = true;
                }
            }

            if (!wroteSomething)
            {
                // just write out the first sheet to avoid Excel problems
                i = 0;
                String sheetTitle = months[currentMonth] + " " + year;
                WritableSheet monthSheet = workbook.createSheet(sheetTitle, 0);
                addHeader(monthSheet, m_data, m_bundle);

                if (includeExReview)
                {
                    String sheetReviewTitle = "Review " + months[currentMonth]
                            + " " + year;
                    WritableSheet monthReviewSheet = workbook.createSheet(
                            sheetReviewTitle, 1);
                    if (m_data.isTradosStyle())
                    {
                        addHeaderTradosStyle(monthReviewSheet,
                                MONTH_REVIEW_SHEET, m_data, m_bundle);
                    }
                    else
                    {
                        addHeader(monthReviewSheet, MONTH_REVIEW_SHEET, m_data,
                                m_bundle);
                    }
                }
            }

            WritableSheet paramsSheet = workbook.createSheet(
                    m_bundle.getString("lb_criteria"), ++i);
            paramsSheet.addCell(new Label(0, 0, m_bundle
                    .getString("lb_report_criteria")));
            paramsSheet.setColumnView(0, 50);

            if (m_data.wantsAllProjects)
            {
                paramsSheet.addCell(new Label(0, 1, m_bundle
                        .getString("lb_selected_projects")
                        + " "
                        + m_bundle.getString("all")));
            }
            else
            {
                paramsSheet.addCell(new Label(0, 1, m_bundle
                        .getString("lb_selected_projects")));
                Iterator<Long> iter = m_data.projectIdList.iterator();
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
                    paramsSheet.addCell(new Label(1, r, m_bundle
                            .getString("lb_id") + "=" + pid.toString()));
                    r++;
                }
            }

            paramsSheet.addCell(new Label(2, 0, m_bundle.getString("lb_Year")));
            paramsSheet.addCell(new Label(2, 1, (String) p_request
                    .getParameter("year")));
            paramsSheet.addCell(new Label(3, 0, m_bundle
                    .getString("lb_re_cost_jobs")));
            paramsSheet.addCell(new Label(3, 1, java.lang.Boolean
                    .toString(recalculateFinishedWorkflow)));

            workbook.write();
            workbook.close();
        }
        // Generate Report by Creation Date Range.
        else if (isReportForDetail != null && isReportForDetail.endsWith("on"))
        {
            // For "Dell Review Report Issue"
            // Add new parameter "includeExReview" to the signature.
            HashMap<String, HashMap<String, ProjectWorkflowData>> projectMap = getProjectDataForMonth(
                    p_request, 0, recalculateFinishedWorkflow, includeExReview);

            // Cancel the report.
            if (isCancelled())
                return new File[0];

            // Create sheets in Workbook.
            createSheets(workbook, projectMap, includeExReview);

            // Create "Criteria" Sheet
            WritableSheet paramsSheet = workbook.createSheet(
                    m_bundle.getString("lb_criteria"), 1);
            paramsSheet.addCell(new Label(0, 0, m_bundle
                    .getString("lb_report_criteria")));
            paramsSheet.setColumnView(0, 50);

            if (m_data.wantsAllProjects)
            {
                paramsSheet.addCell(new Label(0, 1, m_bundle
                        .getString("lb_selected_projects")
                        + " "
                        + m_bundle.getString("all")));
            }
            else
            {
                paramsSheet.addCell(new Label(0, 1, m_bundle
                        .getString("lb_selected_projects")));
                Iterator<Long> iter = m_data.projectIdList.iterator();
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
                    paramsSheet.addCell(new Label(1, r, m_bundle
                            .getString("lb_id") + "=" + pid.toString()));
                    r++;
                }
            }

            paramsSheet.addCell(new Label(2, 0, m_bundle.getString("lb_Year")));
            paramsSheet.addCell(new Label(2, 1, (String) p_request
                    .getParameter("year")));
            paramsSheet.addCell(new Label(3, 0, m_bundle
                    .getString("lb_re_cost_jobs")));
            paramsSheet.addCell(new Label(3, 1, java.lang.Boolean
                    .toString(recalculateFinishedWorkflow)));

            workbook.write();
            workbook.close();
        }

        return new File[]
        { file };
    }

    /**
     * Create sheets in Workbook, include 2 sheets by Creation Date Range. The
     * sheets are the original month Sheet("Detail Report") and monthly
     * Dell_Review sheet("Review Detail Report").
     */
    private void createSheets(WritableWorkbook p_workbook,
            HashMap<String, HashMap<String, ProjectWorkflowData>> p_projectMap,
            boolean p_includeExReview) throws Exception
    {
        if (p_projectMap.size() > 0)
        {
            // This sheets array has two elements.
            // sheets[MONTH_SHEET], the original month Sheet("Detail Report").
            // sheets[MONTH_REVIEW_SHEET], new monthly Dell_Review
            // sheet("Review Detail Report").
            WritableSheet[] sheets = new WritableSheet[2];
            IntHolder[] rows = new IntHolder[2];

            String sheetTitle = m_bundle.getString("lb_detail_report");
            sheets[MONTH_SHEET] = p_workbook.createSheet(sheetTitle, 0);
            if (m_data.isTradosStyle())
            {
                addHeaderTradosStyle(sheets[MONTH_SHEET], MONTH_SHEET, m_data,
                        m_bundle);
            }
            else
            {
                addHeader(sheets[MONTH_SHEET], MONTH_SHEET, m_data, m_bundle);
            }

            rows[MONTH_SHEET] = new IntHolder(4);

            if (p_includeExReview)
            {
                String sheetReviewTitle = m_bundle
                        .getString("lb_review_detail_report");
                sheets[MONTH_REVIEW_SHEET] = p_workbook.createSheet(
                        sheetReviewTitle, 0);
                if (m_data.isTradosStyle())
                {
                    addHeaderTradosStyle(sheets[MONTH_REVIEW_SHEET],
                            MONTH_REVIEW_SHEET, m_data, m_bundle);
                }
                else
                {
                    addHeader(sheets[MONTH_REVIEW_SHEET], MONTH_REVIEW_SHEET,
                            m_data, m_bundle);
                }

                rows[MONTH_REVIEW_SHEET] = new IntHolder(4);
            }

            if (m_data.isTradosStyle())
            {
                writeProjectDataTradosStyle(p_projectMap, sheets,
                        p_includeExReview, rows, m_data, m_bundle);
            }
            else
            {
                writeProjectData(p_projectMap, sheets, p_includeExReview, rows,
                        m_data, m_bundle);
            }
        }
    }

    /**
     * For "Dell Review Report Issue" Add a new parameter "includeExReview" to
     * the signature.
     */
    private HashMap<String, HashMap<String, ProjectWorkflowData>> getProjectDataForMonth(
            HttpServletRequest p_request, int p_month,
            boolean p_recalculateFinishedWorkflow, boolean p_includeExReview)
            throws Exception
    {
        String isReportForYear = p_request.getParameter("reportForThisYear");
        String isReportForDetail = p_request.getParameter("reportForDetail");
        JobSearchParameters searchParams = new JobSearchParameters();
        if (isReportForYear != null && isReportForYear.equals("on"))
        {
            searchParams = getSearchParams(p_request, p_month, m_data);
        }
        if (isReportForDetail != null && isReportForDetail.endsWith("on"))
        {
            if (m_data.wantsAllProjects == false)
            {
                searchParams.setProjectId(m_data.projectIdList);
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

        // initial status parameter
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
            {
                searchParams.setJobState(statusList);
            }
            else
            {
                ArrayList<String> list = new ArrayList<String>();
                list.add(Job.READY_TO_BE_DISPATCHED);
                list.add(Job.DISPATCHED);
                list.add(Job.LOCALIZED);
                list.add(Job.EXPORTED);
                list.add(Job.EXPORT_FAIL);
                list.add(Job.ARCHIVED);
                list.add(Job.PENDING);
                searchParams.setJobState(list);
            }
        }

        ArrayList<Job> queriedJobs = statusParams == null ? new ArrayList<Job>()
                : new ArrayList<Job>(ServerProxy.getJobHandler().getJobs(
                        searchParams));
        int year = Integer.parseInt(p_request.getParameter("year"));
        ArrayList<Job> wrongJobsThisMonth = getWrongJobsForThisMonth(m_data,
                p_month, year);
        // now create a Set of all the jobs
        HashSet<Job> jobs = new HashSet<Job>();
        jobs.addAll(queriedJobs);
        jobs.addAll(wrongJobsThisMonth);
        jobs.removeAll(m_data.ignoreJobs);

        setJobIds(jobs);
        return reportDataMap(jobs, p_recalculateFinishedWorkflow,
                p_includeExReview);
    }

    /**
     * Prepares the data for generating the report.
     * 
     * @return HashMap<key, HashMap<TargetLocale, ProjectWorkflowData>>. String
     *         key = getMapKey(companyName, projectDesc, jobId);
     */
    private HashMap<String, HashMap<String, ProjectWorkflowData>> reportDataMap(
            HashSet<Job> p_jobs, boolean p_recalculateFinishedWorkflow,
            boolean p_includeExReview) throws CostingException,
            RemoteException, GeneralException
    {
        m_data.useInContext = getUseInContext(p_jobs);
        m_data.useDefaultContext = getUseDefaultContext(p_jobs);

        // first iterate through the Jobs and group by Project/workflow because
        // Dell doesn't want to see actual Jobs
        HashMap<String, HashMap<String, ProjectWorkflowData>> projectMap = new HashMap<String, HashMap<String, ProjectWorkflowData>>();
        Currency pivotCurrency = ServerProxy.getCostingEngine()
                .getCurrencyByName(
                        ReportUtil.getCurrencyName(m_data.getCurrency()),
                        CompanyThreadLocal.getInstance().getValue());

        for (Job j : p_jobs)
        {
            if (isCancelled())
                return new HashMap<String, HashMap<String, ProjectWorkflowData>>();

            L10nProfile l10nprofile = j.getL10nProfile();
            if (!m_data.wantsAllLocProfile
                    && !m_data.locProfileIdList.contains(l10nprofile.getId()))
            {
                continue;
            }
            String l10nProfileName = l10nprofile.getName();
            String fileProfileNames = j.getFProfileNames();

            String projectDesc = getProjectDesc(m_data, j);
            String jobId = Long.toString(j.getId());
            String jobName = j.getJobName();
            String companyId = String.valueOf(j.getCompanyId());
            String companyName = CompanyWrapper.getCompanyNameById(companyId);

            List<FileProfile> allFileProfiles = j.getAllFileProfiles();

            // Calculate additional charges
            // GBS-1698, Vincent Yan, 2011/02/21
            Cost jobCost = ServerProxy.getCostingEngine().calculateCost(j,
                    pivotCurrency, true, Cost.REVENUE);

            float totalAdditionalCost = jobCost.getEstimatedCost().getAmount();
            float tmpTotalAdditionalCost = totalAdditionalCost;
            ArrayList<Surcharge> surcharges = new ArrayList<Surcharge>(
                    jobCost.getSurcharges());
            if (surcharges != null && surcharges.size() > 0)
            {
                // calculate total additional charges
                Surcharge surcharge = null;
                FlatSurcharge flatSurcharge = null;
                PercentageSurcharge perSurcharge = null;
                ArrayList<Float> perSurcharges = new ArrayList<Float>();
                // At first, add all flat values to total additional charges and
                // add all percentage values to a tmp list in order to calculate
                // the real value later
                for (int i = 0; i < surcharges.size(); i++)
                {
                    surcharge = (Surcharge) surcharges.get(i);
                    if ("FlatSurcharge".equals(surcharge.getType()))
                    {
                        flatSurcharge = (FlatSurcharge) surcharge;
                        totalAdditionalCost += flatSurcharge.getAmount()
                                .getAmount();
                    }
                    else if ("PercentageSurcharge".equals(surcharge.getType()))
                    {
                        perSurcharge = (PercentageSurcharge) surcharge;
                        perSurcharges.add(Float.valueOf(perSurcharge
                                .getPercentage()));
                    }
                }

                // Get percentage additional charges
                float tmpPercentCost = totalAdditionalCost;
                for (Float f : perSurcharges)
                {
                    totalAdditionalCost += tmpPercentCost * f.floatValue();
                }
            }

            // reduce the base estimated cost to get total additional charge
            // cost only
            totalAdditionalCost -= tmpTotalAdditionalCost;

            TranslationMemoryProfile tmProfile = j.getL10nProfile()
                    .getTranslationMemoryProfile();
            boolean isInContextMatch = PageHandler.isInContextMatch(j,
                    tmProfile);
            boolean isDefaultContextMatch = PageHandler
                    .isDefaultContextMatch(j);

            HashMap<Long, Cost> workflowMap = jobCost.getWorkflowCost();

            for (Workflow w : j.getWorkflows())
            {
                String state = w.getState();
                // skip certain workflows
                if (Workflow.IMPORT_FAILED.equals(w.getState())
                        || Workflow.CANCELLED.equals(w.getState())
                        || Workflow.BATCHRESERVED.equals(w.getState()))
                {
                    continue;
                }
                String targetLang = w.getTargetLocale().toString();
                if (!m_data.wantsAllLocales
                        && !m_data.trgLocaleList.contains(targetLang))
                {
                    continue;
                }

                String key = getMapKey(companyName, projectDesc, jobId);
                HashMap<String, ProjectWorkflowData> localeMap = projectMap
                        .get(key);
                if (localeMap == null)
                {
                    localeMap = new HashMap<String, ProjectWorkflowData>();
                    projectMap.put(key, localeMap);
                }

                ProjectWorkflowData data = new ProjectWorkflowData();
                // For "Job id not showing in external review tabs" Issue
                data.jobId = jobId;
                data.jobName = jobName;
                data.projectDesc = projectDesc;
                data.companyName = companyName;
                data.allFileProfiles = allFileProfiles;
                // the property is as same as jobName one to one
                data.l10nProfileName = l10nProfileName;
                data.fileProfileNames = fileProfileNames;
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

                data.repetitionWordCount = w.getRepetitionWordCount();
                data.lowFuzzyMatchWordCount = w.getThresholdLowFuzzyWordCount();
                data.medFuzzyMatchWordCount = w.getThresholdMedFuzzyWordCount();
                data.medHiFuzzyMatchWordCount = w
                        .getThresholdMedHiFuzzyWordCount();
                data.hiFuzzyMatchWordCount = w.getThresholdHiFuzzyWordCount();

                // the fuzzyMatchWordCount is the sum of the top 3 categories
                data.fuzzyMatchWordCount = data.medFuzzyMatchWordCount
                        + data.medHiFuzzyMatchWordCount
                        + data.hiFuzzyMatchWordCount;

                // add the lowest fuzzies and sublev match to nomatch
                data.noMatchWordCount = w.getThresholdNoMatchWordCount()
                        + w.getThresholdLowFuzzyWordCount();

                data.segmentTmWordCount = (isInContextMatch) ? w
                        .getSegmentTmWordCount() : (isDefaultContextMatch) ? w
                        .getTotalExactMatchWordCount()
                        - w.getContextMatchWordCount() : w
                        .getTotalExactMatchWordCount();
                data.contextMatchWordCount = (isDefaultContextMatch) ? w
                        .getContextMatchWordCount() : 0;
                data.inContextMatchWordCount = (isInContextMatch) ? w
                        .getInContextMatchWordCount() : w
                        .getNoUseInContextMatchWordCount();
                data.totalWordCount = w.getTotalWordCount();
                /*
                 * Date da1 = new Date(); // They must be in front of calculate
                 * cost for activity // "Dell_Review".
                 * 
                 * Cost wfCost = ServerProxy.getCostingEngine().calculateCost(w,
                 * pivotCurrency, p_recalculateFinishedWorkflow, Cost.REVENUE,
                 * p_recalculateFinishedWorkflow); Date da2 = new Date();
                 * 
                 * ccc = ccc + (da2.getTime() - da1.getTime());
                 * 
                 * CostByWordCount costByWordCount =
                 * wfCost.getCostByWordCount();
                 */
                Cost wfCost = workflowMap.get(w.getId());
                CostByWordCount costByWordCount = null;

                if (wfCost != null)
                    costByWordCount = wfCost.getCostByWordCount();

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
                            /*
                             * cost = ServerProxy.getCostingEngine()
                             * .calculateCost(task, pivotCurrency,
                             * p_recalculateFinishedWorkflow, Cost.REVENUE);
                             */
                            cost = wfCost.getTaskCost().get(task.getId());

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
                                        : (isDefaultContextMatch) ? dellReviewCostByWordCount
                                                .getDefaultContextExactMatchCost()
                                                : dellReviewCostByWordCount
                                                        .getNoUseExactMatchCost());
                        data.contextMatchWordCountCostForDellReview = add(
                                data.contextMatchWordCountCostForDellReview,
                                (isDefaultContextMatch) ? dellReviewCostByWordCount
                                        .getContextMatchCost() : 0);
                        data.inContextMatchWordCountCostForDellReview = add(
                                data.inContextMatchWordCountCostForDellReview,
                                (isInContextMatch) ? dellReviewCostByWordCount
                                        .getInContextMatchCost()
                                        : dellReviewCostByWordCount
                                                .getNoUseInContextMatchCost());

                        // fuzzy match cost is the sum of the top three fuzzy
                        // match categories
                        data.fuzzyMatchWordCountCostForDellReview = data.medFuzzyMatchWordCountCostForDellReview
                                .add(data.medHiFuzzyMatchWordCountCostForDellReview)
                                .add(data.hiFuzzyMatchWordCountCostForDellReview);

                        // this means that no match cost should include the
                        // lowest fuzzy match category
                        data.noMatchWordCountCostForDellReview = data.noMatchWordCountCostForDellReview
                                .add(data.lowFuzzyMatchWordCountCostForDellReview);

                        data.totalWordCountCostForDellReview = data.repetitionWordCountCostForDellReview
                                .add(data.noMatchWordCountCostForDellReview)
                                .add(data.fuzzyMatchWordCountCostForDellReview)
                                .add(data.segmentTmWordCountCostForDellReview)
                                .add(data.contextMatchWordCountCostForDellReview)
                                .add(data.inContextMatchWordCountCostForDellReview);
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
                            (isInContextMatch) ? costByWordCount
                                    .getSegmentTmMatchCost()
                                    : (isDefaultContextMatch) ? costByWordCount
                                            .getDefaultContextExactMatchCost()
                                            : costByWordCount
                                                    .getNoUseExactMatchCost(),
                            data.segmentTmWordCountCostForDellReview);
                    data.contextMatchWordCountCost = BigDecimalHelper.subtract(
                            (isDefaultContextMatch) ? costByWordCount
                                    .getContextMatchCost() : 0,
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

                    data.totalWordCountCost = data.repetitionWordCountCost
                            .add(data.noMatchWordCountCost)
                            .add(data.fuzzyMatchWordCountCost)
                            .add(data.segmentTmWordCountCost)
                            .add(data.contextMatchWordCountCost)
                            .add(data.inContextMatchWordCountCost);
                }

                data.totalAdditionalCost = add(data.totalAdditionalCost,
                        totalAdditionalCost);

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

    /*
     * Create the key for sorting the data by key.
     */
    private String getMapKey(String p_companyName, String p_projectDesc,
            String p_jobId)
    {
        return p_companyName + SEPARATOR + p_projectDesc + SEPARATOR + p_jobId;
    }

    /*
     * Get the jobId from the key
     * 
     * @see getMapKey
     */
    private String getJobIdFromMapKey(String p_key)
    {
        return p_key.substring(p_key.lastIndexOf(SEPARATOR)
                + SEPARATOR.length());
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

    private Date getExportDate(String targetLang, String jobName)
    {
        String cxePath = AmbFileStoragePathUtils.getCxeDocDirPath();
        String path = cxePath + "/" + targetLang + "/" + jobName;

        File file = new File(path);
        if (!file.exists())
        {
            String newPath = cxePath + "/" + targetLang + "/webservice/"
                    + jobName;
            file = new File(newPath);
        }

        if (file.exists() && file.isDirectory())
        {
            File[] files = file.listFiles();
            if (files.length > 0)
            {
                file = files[0];
            }
        }

        return file.exists() ? new Date(file.lastModified()) : null;
    }

    /**
     * Class used to group data by project and target language
     */
    private class ProjectWorkflowData
    {
        public String l10nProfileName;
        public String fileProfileNames;

        // For "Job id not showing in external review tabs" Issue
        public String jobId;

        public String jobName;

        public String projectDesc;

        public String companyName;

        public List<FileProfile> allFileProfiles;

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

        public BigDecimal totalAdditionalCost = new BigDecimal(
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

    private class MyData
    {
        public boolean wantsAllProjects = false;

        public boolean wantsAllLocProfile = true;

        boolean wantsAllLocales = false;

        private boolean tradosStyle = true;

        private String currency = "US Dollar";

        HashSet<String> trgLocaleList = new HashSet<String>();

        ArrayList<Long> projectIdList = new ArrayList<Long>();

        ArrayList<Long> locProfileIdList = new ArrayList<Long>();

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

        // Summary Start column
        int sumStartCol = 0;
        List<Long> jobIds;

        public int getSumStartCol()
        {
            return sumStartCol;
        }

        public void initSumStartCol(int p_col)
        {
            if (sumStartCol == 0)
                sumStartCol = p_col;
        }

        public String getDateFormatString()
        {
            return dateFormatString;
        }

        public void setDateFormatString(String dateFormatString)
        {
            this.dateFormatString = dateFormatString;
        }

        public boolean isTradosStyle()
        {
            return tradosStyle;
        }

        public void setTradosStyle(boolean tradosStyle)
        {
            this.tradosStyle = tradosStyle;
        }

        public void setTradosStyle(String tradosStyleStr)
        {
            if ("trados".equalsIgnoreCase(tradosStyleStr))
            {
                this.tradosStyle = true;
            }
            else
            {
                this.tradosStyle = false;
            }
        }

        public String getCurrency()
        {
            return currency;
        }

        public void setCurrency(String currency)
        {
            if (currency != null && currency.trim().length() > 0)
                this.currency = currency;
        }
    };

    public MyData getMydata()
    {
        return m_data;
    }

    public void setMydata(MyData m_data)
    {
        this.m_data = m_data;
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

    private void setLocpIdList(HttpServletRequest p_request, MyData p_data)
    {
        // set the project Id
        String[] locprofiles = p_request.getParameterValues("locprofile");

        if (locprofiles != null)
        {
            for (int i = 0; i < locprofiles.length; i++)
            {
                String ids = locprofiles[i];
                if (ids.equals("*") == false)
                {
                    m_data.wantsAllLocProfile = false;
                    // Long p_id = new Long(id);
                    // Collection<? extends Long> c = ServerProxy
                    // .getFileProfilePersistenceManager()
                    // .readFileProfileBy(p_id);
                    String[] locproids = ids.split(",");
                    for (int j = 0; j < locproids.length; j++)
                    {
                        String id = locproids[j];
                        p_data.locProfileIdList.add(new Long(id));
                    }

                }
            }
        }
    }

    private void setTargetLocales(HttpServletRequest p_request, MyData p_data)
    {
        String[] paramTrgLocales = p_request
                .getParameterValues(ReportConstants.TARGETLOCALE_LIST);
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
        list.add(Job.PENDING);
        list.add(Job.ARCHIVED);
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
    private void addHeader(WritableSheet p_sheet, MyData p_data,
            ResourceBundle bundle) throws Exception
    {
        if (p_data.isTradosStyle())
        {
            addHeaderTradosStyle(p_sheet, MONTH_SHEET, p_data, bundle);
        }
        else
        {
            addHeader(p_sheet, MONTH_SHEET, p_data, bundle);
        }
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

        int col = -1;
        // Company Name
        p_sheet.addCell(new Label(++col, 2,
                bundle.getString("lb_company_name"), headerFormat));
        p_sheet.mergeCells(col, 2, col, 3);
        // Project Description
        p_sheet.addCell(new Label(++col, 2, bundle.getString("lb_project"),
                headerFormat));
        p_sheet.mergeCells(col, 2, col, 3);

        // For "Add Job Id into online job report" issue
        if (isJobIdVisible)
        {
            p_sheet.addCell(new Label(++col, 2, bundle.getString("lb_job_id"),
                    headerFormat));
            p_sheet.mergeCells(col, 2, col, 3);
        }

        p_sheet.addCell(new Label(++col, 2, bundle.getString("lb_job_name"),
                headerFormat));
        p_sheet.mergeCells(col, 2, col, 3);
        p_sheet.addCell(new Label(++col, 2, bundle
                .getString("lb_source_file_format"), headerFormat));
        p_sheet.mergeCells(col, 2, col, 3);

        p_sheet.addCell(new Label(++col, 2, bundle.getString("lb_loc_profile"),
                headerFormat));
        p_sheet.mergeCells(col, 2, col, 3);

        p_sheet.addCell(new Label(++col, 2, bundle
                .getString("lb_file_profiles"), headerFormat));
        p_sheet.mergeCells(col, 2, col, 3);

        p_sheet.addCell(new Label(++col, 2, bundle
                .getString("lb_creation_date"), headerFormat));
        p_sheet.mergeCells(col, 2, col, 3);
        p_sheet.addCell(new Label(++col, 2, bundle
                .getString("lb_creation_time"), headerFormat));
        p_sheet.mergeCells(col, 2, col, 3);
        p_sheet.addCell(new Label(++col, 2, bundle.getString("lb_export_date"),
                headerFormat));
        p_sheet.mergeCells(col, 2, col, 3);
        p_sheet.addCell(new Label(++col, 2, bundle.getString("lb_export_time"),
                headerFormat));
        p_sheet.mergeCells(col, 2, col, 3);
        p_sheet.addCell(new Label(++col, 2, bundle.getString("lb_status"),
                headerFormat));
        p_sheet.mergeCells(col, 2, col, 3);
        p_sheet.addCell(new Label(++col, 2, bundle.getString("lb_lang"),
                langFormat));
        p_sheet.mergeCells(col, 2, col, 3);
        p_sheet.addCell(new Label(++col, 2, bundle.getString("lb_word_counts"),
                wordCountFormat));
        if (p_data.useInContext && p_data.useDefaultContext)
        {
            p_sheet.mergeCells(col, 2, col + 6, 2);
        }
        else
        {
            if (p_data.useInContext)
            {
                p_sheet.mergeCells(col, 2, col + 5, 2);
            }
            else if (p_data.useDefaultContext)
            {
                p_sheet.mergeCells(col, 2, col + 5, 2);
            }
            else
            {
                p_sheet.mergeCells(col, 2, col + 4, 2);
            }
        }
        p_sheet.addCell(new Label(col, 3, bundle
                .getString("jobinfo.tmmatches.wordcounts.internalreps"),
                wordCountValueFormat));
        col++;
        p_sheet.addCell(new Label(col, 3, bundle
                .getString("lb_100_exact_matches"), wordCountValueFormat));

        if (p_data.useInContext)
        {
            col++;
            p_sheet.addCell(new Label(col, 3, bundle
                    .getString("lb_in_context_tm"), wordCountValueFormat));
        }

        if (p_data.useDefaultContext)
        {
            col++;
            p_sheet.addCell(new Label(col, 3,
                    bundle.getString("lb_context_tm"), wordCountValueFormat));
        }

        col++;
        p_sheet.addCell(new Label(col, 3, bundle
                .getString("jobinfo.tmmatches.wordcounts.fuzzymatches"),
                wordCountValueFormat));

        col++;
        p_sheet.addCell(new Label(col, 3, bundle
                .getString("jobinfo.tmmatches.wordcounts.newwords"),
                wordCountValueFormat));

        col++;
        p_sheet.addCell(new Label(col, 3, bundle
                .getString("lb_total_source_word_count"),
                wordCountValueRightFormat));

        col++;
        p_sheet.addCell(new Label(col, 2, bundle
                .getString("jobinfo.tmmatches.invoice"), wordCountFormat));

        if (p_sheetCategory == MONTH_SHEET)
        {
            if (p_data.useInContext && p_data.useDefaultContext)
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
            p_sheet.addCell(new Label(col, 3, bundle
                    .getString("jobinfo.tmmatches.invoice.internalreps"),
                    wordCountValueFormat));
            col++;
            p_sheet.addCell(new Label(col, 3, bundle
                    .getString("lb_100_exact_matches"), wordCountValueFormat));
            col++;
            if (p_data.useInContext)
            {
                p_sheet.addCell(new Label(col, 3, bundle
                        .getString("lb_in_context_tm"), wordCountValueFormat));
                col++;
            }
            if (p_data.useDefaultContext)
            {
                p_sheet.addCell(new Label(col, 3, bundle
                        .getString("lb_context_tm"), wordCountValueFormat));
                col++;

            }
            p_sheet.addCell(new Label(col, 3, bundle
                    .getString("jobinfo.tmmatches.wordcounts.fuzzymatches"),
                    wordCountValueFormat));
            col++;
            p_sheet.addCell(new Label(col, 3, bundle
                    .getString("jobinfo.tmmatches.wordcounts.newwords"),
                    wordCountValueFormat));
            col++;
            p_sheet.addCell(new Label(col, 3, bundle
                    .getString("jobinfo.tmmatches.wordcounts.total"),
                    wordCountValueFormat));
            col++;

            p_sheet.addCell(new Label(col, 3, bundle
                    .getString("jobinfo.tmmatches.invoice.additionalCharges"),
                    wordCountValueRightFormat));
            col++;

            p_sheet.addCell(new Label(col, 3, bundle
                    .getString("jobinfo.tmmatches.invoice.jobtotal"),
                    wordCountValueRightFormat));
            col++;

            p_sheet.addCell(new Label(col, 2, bundle.getString("lb_tracking")
                    + " " + EMEA + " " + bundle.getString("lb_use") + ")",
                    headerFormat));
            p_sheet.mergeCells(col, 2, col, 3);

        }
        else if (p_sheetCategory == MONTH_REVIEW_SHEET)
        {
            if (p_data.useInContext && p_data.useDefaultContext)
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
            p_sheet.addCell(new Label(col, 3, bundle
                    .getString("jobinfo.tmmatches.wordcounts.internalreps"),
                    wordCountValueFormat));
            col++;
            p_sheet.addCell(new Label(col, 3, bundle
                    .getString("lb_100_exact_matches"), wordCountValueFormat));
            col++;
            if (p_data.useInContext)
            {
                p_sheet.addCell(new Label(col, 3, bundle
                        .getString("lb_in_context_tm"), wordCountValueFormat));
                col++;
            }
            if (p_data.useDefaultContext)
            {
                p_sheet.addCell(new Label(col, 3, bundle
                        .getString("lb_context_tm"), wordCountValueFormat));
                col++;
            }

            p_sheet.addCell(new Label(col, 3, bundle
                    .getString("jobinfo.tmmatches.wordcounts.fuzzymatches"),
                    wordCountValueFormat));
            col++;
            p_sheet.addCell(new Label(col, 3, bundle
                    .getString("jobinfo.tmmatches.wordcounts.newwords"),
                    wordCountValueFormat));
            col++;
            p_sheet.addCell(new Label(col, 3, bundle
                    .getString("lb_translation_total"), wordCountValueFormat));
            col++;
            p_sheet.addCell(new Label(col, 3, bundle.getString("lb_review"),
                    wordCountValueRightFormat));
            col++;
            p_sheet.addCell(new Label(col, 3, bundle
                    .getString("jobinfo.tmmatches.invoice.jobtotal"),
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

    /**
     * For trados style
     * 
     * @param p_sheet
     * @param p_sheetCategory
     * @param p_data
     * @param bundle
     * @throws Exception
     */
    private void addHeaderTradosStyle(WritableSheet p_sheet,
            final int p_sheetCategory, MyData p_data, ResourceBundle bundle)
            throws Exception
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
        p_sheet.addCell(new Label(0, 1, bundle.getString("lb_desp_file_list")));

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

        int col = -1;

        // Company Name
        p_sheet.addCell(new Label(++col, 2,
                bundle.getString("lb_company_name"), headerFormat));
        p_sheet.mergeCells(col, 2, col, 3);

        // Project Description
        p_sheet.addCell(new Label(++col, 2, bundle.getString("lb_project"),
                headerFormat));
        p_sheet.mergeCells(col, 2, col, 3);

        if (isJobIdVisible)
        {
            p_sheet.addCell(new Label(++col, 2, bundle.getString("lb_job_id"),
                    headerFormat));
            p_sheet.mergeCells(col, 2, col, 3);
        }

        p_sheet.addCell(new Label(++col, 2, bundle.getString("lb_job_name"),
                headerFormat));
        p_sheet.mergeCells(col, 2, col, 3);

        p_sheet.addCell(new Label(++col, 2, bundle
                .getString("lb_source_file_format"), headerFormat));
        p_sheet.mergeCells(col, 2, col, 3);

        p_sheet.addCell(new Label(++col, 2, bundle.getString("lb_loc_profile"),
                headerFormat));
        p_sheet.mergeCells(col, 2, col, 3);

        p_sheet.addCell(new Label(++col, 2, bundle
                .getString("lb_file_profiles"), headerFormat));
        p_sheet.mergeCells(col, 2, col, 3);

        p_sheet.addCell(new Label(++col, 2, bundle
                .getString("lb_creation_date"), headerFormat));
        p_sheet.mergeCells(col, 2, col, 3);
        p_sheet.addCell(new Label(++col, 2, bundle
                .getString("lb_creation_time"), headerFormat));
        p_sheet.mergeCells(col, 2, col, 3);
        p_sheet.addCell(new Label(++col, 2, bundle.getString("lb_export_date"),
                headerFormat));
        p_sheet.mergeCells(col, 2, col, 3);
        p_sheet.addCell(new Label(++col, 2, bundle.getString("lb_export_time"),
                headerFormat));
        p_sheet.mergeCells(col, 2, col, 3);
        p_sheet.addCell(new Label(++col, 2, bundle.getString("lb_status"),
                headerFormat));
        p_sheet.mergeCells(col, 2, col, 3);
        p_sheet.addCell(new Label(++col, 2, bundle.getString("lb_lang"),
                langFormat));
        p_sheet.mergeCells(col, 2, col, 3);
        p_sheet.addCell(new Label(++col, 2, bundle.getString("lb_word_counts"),
                wordCountFormat));
        if (p_data.useInContext && p_data.useDefaultContext)
        {
            p_sheet.mergeCells(col, 2, col + 8, 2);
        }
        else
        {
            if (p_data.useInContext)
            {
                p_sheet.mergeCells(col, 2, col + 7, 2);
            }
            else if (p_data.useDefaultContext)
            {
                p_sheet.mergeCells(col, 2, col + 7, 2);
            }
            else
            {
                p_sheet.mergeCells(col, 2, col + 6, 2);
            }
        }

        p_sheet.addCell(new Label(col, 3, bundle
                .getString("jobinfo.tradosmatches.invoice.per100matches"),
                wordCountValueFormat));

        col++;
        p_sheet.addCell(new Label(col, 3, bundle.getString("lb_95_99"),
                wordCountValueFormat));

        col++;
        p_sheet.addCell(new Label(col, 3, bundle.getString("lb_85_94"),
                wordCountValueFormat));

        col++;
        p_sheet.addCell(new Label(col, 3, bundle.getString("lb_75_84") + "*",
                wordCountValueFormat));

        col++;
        p_sheet.addCell(new Label(col, 3, bundle.getString("lb_no_match"),
                wordCountValueFormat));

        col++;
        p_sheet.addCell(new Label(col, 3, bundle
                .getString("lb_repetition_word_cnt"), wordCountValueFormat));

        if (p_data.useInContext)
        {
            col++;
            p_sheet.addCell(new Label(col, 3, bundle
                    .getString("lb_in_context_tm"), wordCountValueFormat));
        }

        if (p_data.useDefaultContext)
        {
            col++;
            p_sheet.addCell(new Label(col, 3,
                    bundle.getString("lb_context_tm"), wordCountValueFormat));
        }

        col++;
        p_sheet.addCell(new Label(col, 3, bundle.getString("lb_total"),
                wordCountValueRightFormat));

        col++;
        p_sheet.addCell(new Label(col, 2, bundle
                .getString("jobinfo.tmmatches.invoice"), wordCountFormat));

        if (p_sheetCategory == MONTH_SHEET)
        {
            if (p_data.useInContext && p_data.useDefaultContext)
            {
                p_sheet.mergeCells(col, 2, col + 10, 2);
            }
            else
            {
                if (p_data.useInContext || p_data.useDefaultContext)
                {
                    p_sheet.mergeCells(col, 2, col + 9, 2);
                }
                else
                {
                    p_sheet.mergeCells(col, 2, col + 8, 2);
                }
            }

            p_sheet.addCell(new Label(col, 3, bundle
                    .getString("jobinfo.tradosmatches.invoice.per100matches"),
                    wordCountValueFormat));

            col++;
            p_sheet.addCell(new Label(col, 3, bundle.getString("lb_95_99"),
                    wordCountValueFormat));

            col++;
            p_sheet.addCell(new Label(col, 3, bundle.getString("lb_85_94"),
                    wordCountValueFormat));

            col++;
            p_sheet.addCell(new Label(col, 3, bundle.getString("lb_75_84")
                    + "*", wordCountValueFormat));

            col++;
            p_sheet.addCell(new Label(col, 3, bundle.getString("lb_no_match"),
                    wordCountValueFormat));

            col++;
            p_sheet.addCell(new Label(col, 3, bundle
                    .getString("lb_repetition_word_cnt"), wordCountValueFormat));

            col++;
            if (p_data.useInContext)
            {
                p_sheet.addCell(new Label(col, 3, bundle
                        .getString("lb_in_context_tm"), wordCountValueFormat));
                col++;
            }
            if (p_data.useDefaultContext)
            {
                p_sheet.addCell(new Label(col, 3, bundle
                        .getString("lb_context_tm"), wordCountValueFormat));
                col++;

            }

            p_sheet.addCell(new Label(col, 3, bundle
                    .getString("jobinfo.tmmatches.wordcounts.total"),
                    wordCountValueFormat));
            col++;

            p_sheet.addCell(new Label(col, 3, bundle
                    .getString("jobinfo.tmmatches.invoice.additionalCharges"),
                    wordCountValueRightFormat));
            col++;

            p_sheet.addCell(new Label(col, 3, bundle
                    .getString("jobinfo.tmmatches.invoice.jobtotal"),
                    wordCountValueRightFormat));

            col++;
            p_sheet.addCell(new Label(col, 2, bundle.getString("lb_tracking")
                    + " " + EMEA + " " + bundle.getString("lb_use") + ")",
                    headerFormat));
            p_sheet.mergeCells(col, 2, col, 3);

        }
        else if (p_sheetCategory == MONTH_REVIEW_SHEET)
        {
            if (p_data.useInContext && p_data.useDefaultContext)
            {
                p_sheet.mergeCells(col, 2, col + 10, 2);
            }
            else
            {
                if (p_data.useInContext || p_data.useDefaultContext)
                {
                    p_sheet.mergeCells(col, 2, col + 9, 2);
                }
                else
                {
                    p_sheet.mergeCells(col, 2, col + 7, 2);
                }
            }

            p_sheet.addCell(new Label(col, 3, bundle
                    .getString("jobinfo.tradosmatches.invoice.per100matches"),
                    wordCountValueFormat));

            col++;
            p_sheet.addCell(new Label(col, 3, bundle.getString("lb_95_99"),
                    wordCountValueFormat));
            col++;
            p_sheet.addCell(new Label(col, 3, bundle.getString("lb_85_94"),
                    wordCountValueFormat));
            col++;
            p_sheet.addCell(new Label(col, 3, bundle.getString("lb_75_84")
                    + "*", wordCountValueFormat));
            col++;
            p_sheet.addCell(new Label(col, 3, bundle.getString("lb_no_match"),
                    wordCountValueFormat));

            col++;
            p_sheet.addCell(new Label(col, 3, bundle
                    .getString("lb_repetition_word_cnt"), wordCountValueFormat));

            col++;
            if (p_data.useInContext)
            {
                p_sheet.addCell(new Label(col, 3, bundle
                        .getString("lb_in_context_tm"), wordCountValueFormat));
                col++;
            }
            if (p_data.useDefaultContext)
            {
                p_sheet.addCell(new Label(col, 3, bundle
                        .getString("lb_context_tm"), wordCountValueFormat));
                col++;
            }

            p_sheet.addCell(new Label(col, 3, bundle
                    .getString("lb_translation_total"), wordCountValueFormat));
            col++;
            p_sheet.addCell(new Label(col, 3, bundle.getString("lb_review"),
                    wordCountValueRightFormat));
            col++;
            p_sheet.addCell(new Label(col, 3, bundle
                    .getString("jobinfo.tmmatches.invoice.jobtotal"),
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

    /*
     * public void writeProjectData( HashMap<String, HashMap<String,
     * ProjectWorkflowData>> p_projectMap, WritableSheet p_sheet, IntHolder
     * p_row, MyData p_data, ResourceBundle bundle) throws Exception {
     * WritableSheet[] sheets = new WritableSheet[2]; sheets[MONTH_SHEET] =
     * p_sheet; sheets[MONTH_REVIEW_SHEET] = null;
     * 
     * IntHolder[] rows = new IntHolder[2]; rows[MONTH_SHEET] = p_row;
     * rows[MONTH_REVIEW_SHEET] = null;
     * 
     * writeProjectData(p_projectMap, sheets, false, rows, p_data, bundle); }
     */

    public void writeProjectData(
            HashMap<String, HashMap<String, ProjectWorkflowData>> p_projectMap,
            WritableSheet[] p_sheets, boolean p_includeExReview,
            IntHolder[] p_rows, MyData p_data, ResourceBundle bundle)
            throws Exception
    {
        ArrayList<String> keys = new ArrayList<String>(p_projectMap.keySet());
        SortUtil.sort(keys);
        Iterator<String> keysIter = keys.iterator();

        String datetimeFormatString = p_data.getDateFormatString();
        String dateFormatString = datetimeFormatString.substring(0,
                datetimeFormatString.indexOf(" ") - 1);
        String timeFormatString = datetimeFormatString.substring(
                datetimeFormatString.indexOf(" ") + 1,
                datetimeFormatString.length());
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
        failed_timeFormat.setBackground(jxl.format.Colour.RED);
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

        totalCost.clear();
        totalCostDate.clear();
        int finishedJobNum = 0;

        while (keysIter.hasNext())
        {
            // Cancel generate reports.
            if (isCancelled())
                return;
            // Sets Reports Percent.
            setPercent(++finishedJobNum);
            String key = keysIter.next();
            String jobId = getJobIdFromMapKey(key);
            boolean isWrongJob = p_data.wrongJobNames.contains(jobId);
            HashMap<String, ProjectWorkflowData> localeMap = p_projectMap
                    .get(key);
            ArrayList<String> locales = new ArrayList<String>(
                    localeMap.keySet());
            SortUtil.sort(locales);
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

                // Company Name
                p_sheets[MONTH_SHEET].addCell(new Label(col++, row,
                        data.companyName, temp_normalFormat));
                p_sheets[MONTH_SHEET].setColumnView(col - 1, 15);

                // Project Description
                p_sheets[MONTH_SHEET].addCell(new Label(col++, row,
                        data.projectDesc, temp_normalFormat));
                p_sheets[MONTH_SHEET].setColumnView(col - 1, 22);

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
                        getAllSouceFileFormats(data.allFileProfiles),
                        temp_normalFormat));
                p_sheets[MONTH_SHEET].setColumnView(col - 1, 20);

                p_sheets[MONTH_SHEET].addCell(new Label(col++, row,
                        data.l10nProfileName, temp_normalFormat));
                p_sheets[MONTH_SHEET].setColumnView(col - 1, 25);

                p_sheets[MONTH_SHEET].addCell(new Label(col++, row,
                        data.fileProfileNames, temp_normalFormat));
                p_sheets[MONTH_SHEET].setColumnView(col - 1, 20);

                if (data.wasExportFailed)
                {
                    p_sheets[MONTH_SHEET].addCell(new DateTime(col++, row,
                            data.creationDate, failed_dateFormat));
                    p_sheets[MONTH_SHEET].setColumnView(col - 1, 15);
                    p_sheets[MONTH_SHEET].addCell(new DateTime(col++, row,
                            data.creationDate, failed_timeFormat));
                    p_sheets[MONTH_SHEET].setColumnView(col - 1, 18);
                    p_sheets[MONTH_SHEET].addCell(new Label(col++, row, "",
                            failed_dateFormat));
                    p_sheets[MONTH_SHEET].setColumnView(col - 1, 15);
                    p_sheets[MONTH_SHEET].addCell(new Label(col++, row, "",
                            failed_timeFormat));
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
                        labelExportDate = getExportDateStr(dateFormat,
                                data.exportDate);
                    }
                    p_sheets[MONTH_SHEET].addCell(new Label(col++, row,
                            labelExportDate));
                    p_sheets[MONTH_SHEET].setColumnView(col - 1, 15);
                    String labelExportTime = "";
                    if (data.wasExported)
                    {
                        labelExportTime = getExportDateStr(timeFormat,
                                data.exportDate);
                    }
                    p_sheets[MONTH_SHEET].addCell(new Label(col++, row,
                            labelExportTime));

                }
                p_sheets[MONTH_SHEET].setColumnView(col - 1, 18);

                // Status
                p_sheets[MONTH_SHEET].addCell(new Label(col++, row,
                        data.status, temp_normalFormat));
                p_sheets[MONTH_SHEET].setColumnView(col - 1, 18);
                // Language
                p_sheets[MONTH_SHEET].addCell(new Label(col++, row,
                        data.targetLang, temp_normalFormat));
                int numwidth = 10;
                // Summary Start Column
                p_data.initSumStartCol(col);
                p_sheets[MONTH_SHEET].addCell(new Number(col++, row,
                        data.repetitionWordCount, temp_normalFormat));
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

                if (p_data.useDefaultContext)
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
                if (p_data.useDefaultContext)
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
                if (totalCol == null)
                {
                    totalCol = ReportUtil.toChar(col);
                }
                p_sheets[MONTH_SHEET].addCell(new Number(col++, row,
                        asDouble(data.totalWordCountCost), temp_moneyFormat));

                // Show total additional charges
                // p_sheets[MONTH_SHEET].setColumnView(col - 1, moneywidth);
                // p_sheets[MONTH_SHEET].addCell(new Number(col++, row,
                // asDouble(data.totalAdditionalCost), temp_moneyFormat));

                // p_sheets[MONTH_SHEET].setColumnView(col - 1, moneywidth);
                projectTotalWordCountCost = projectTotalWordCountCost
                        .add(data.totalWordCountCost);

                List<Integer> indexs = totalCost.get(data.targetLang);
                if (indexs == null)
                {
                    indexs = new ArrayList<Integer>();
                    totalCost.put(data.targetLang, indexs);
                }
                indexs.add(row + 1);

                Double value = totalCostDate.get(data.targetLang);
                if (value == null)
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
                            asDouble(data.totalAdditionalCost),
                            temp_moneyFormat));
                    p_sheets[MONTH_SHEET].setColumnView(col - 1, moneywidth);

                    p_sheets[MONTH_SHEET].addCell(new Number(col++, row,
                            asDouble(projectTotalWordCountCost)
                                    + asDouble(data.totalAdditionalCost),
                            temp_moneyFormat));
                }
                else
                {
                    p_sheets[MONTH_SHEET].addCell(new Blank(col++, row,
                            temp_moneyFormat));
                    p_sheets[MONTH_SHEET].setColumnView(col - 1, moneywidth);

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
                        /*
                         * p_sheets[MONTH_SHEET].addCell(new DateTime(col++,
                         * row, data.creationDate, failed_dateFormat));
                         */
                        p_sheets[MONTH_REVIEW_SHEET].addCell(new DateTime(
                                col++, row, data.creationDate,
                                failed_dateFormat));
                        p_sheets[MONTH_REVIEW_SHEET].setColumnView(col - 1, 15);
                        p_sheets[MONTH_REVIEW_SHEET].addCell(new DateTime(
                                col++, row, data.creationDate,
                                failed_timeFormat));
                        p_sheets[MONTH_REVIEW_SHEET].setColumnView(col - 1, 18);
                        p_sheets[MONTH_REVIEW_SHEET].addCell(new Label(col++,
                                row, "", failed_dateFormat));
                        p_sheets[MONTH_REVIEW_SHEET].setColumnView(col - 1, 15);
                        p_sheets[MONTH_REVIEW_SHEET].addCell(new Label(col++,
                                row, "", failed_timeFormat));
                    }
                    else
                    {
                        /*
                         * p_sheets[MONTH_SHEET].addCell(new Label(col++, row,
                         * dateFormat.format(data.creationDate)));
                         */
                        p_sheets[MONTH_REVIEW_SHEET].addCell(new Label(col++,
                                row, dateFormat.format(data.creationDate)));
                        p_sheets[MONTH_REVIEW_SHEET].setColumnView(col - 1, 15);
                        p_sheets[MONTH_REVIEW_SHEET].addCell(new Label(col++,
                                row, timeFormat.format(data.creationDate)));
                        p_sheets[MONTH_REVIEW_SHEET].setColumnView(col - 1, 18);
                        String labelExportDate = "";
                        if (data.wasExported)
                        {
                            labelExportDate = getExportDateStr(dateFormat,
                                    data.exportDate);
                        }
                        p_sheets[MONTH_REVIEW_SHEET].addCell(new Label(col++,
                                row, labelExportDate));
                        p_sheets[MONTH_REVIEW_SHEET].setColumnView(col - 1, 15);
                        String labelExportTime = "";
                        if (data.wasExported)
                        {
                            labelExportTime = getExportDateStr(timeFormat,
                                    data.exportDate);
                        }
                        p_sheets[MONTH_REVIEW_SHEET].addCell(new Label(col++,
                                row, labelExportTime));

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
                    if (p_data.useDefaultContext)
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

                    if (p_data.useDefaultContext)
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
            addTotalsPerLang(p_sheets[MONTH_SHEET], MONTH_SHEET,
                    p_rows[MONTH_SHEET], p_data, bundle);
        }
    }

    /**
     * For Trados Style
     * 
     * @param p_projectMap
     * @param p_sheets
     * @param p_includeExReview
     * @param p_rows
     * @param p_data
     * @param bundle
     * @throws Exception
     */
    public void writeProjectDataTradosStyle(
            HashMap<String, HashMap<String, ProjectWorkflowData>> p_projectMap,
            WritableSheet[] p_sheets, boolean p_includeExReview,
            IntHolder[] p_rows, MyData p_data, ResourceBundle bundle)
            throws Exception
    {
        ArrayList<String> keys = new ArrayList<String>(p_projectMap.keySet());
        SortUtil.sort(keys);
        Iterator<String> keysIter = keys.iterator();

        String datetimeFormatString = p_data.getDateFormatString();
        String dateFormatString = datetimeFormatString.substring(0,
                datetimeFormatString.indexOf(" ") - 1);
        String timeFormatString = datetimeFormatString.substring(
                datetimeFormatString.indexOf(" ") + 1,
                datetimeFormatString.length());
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
        failed_timeFormat.setBackground(jxl.format.Colour.RED);
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

        totalCost.clear();
        totalCostDate.clear();
        int finishedJobNum = 0;
        while (keysIter.hasNext())
        {
            // Cancel generate reports.
            if (isCancelled())
                return;
            // Sets Reports Percent.
            setPercent(++finishedJobNum);

            String key = keysIter.next();
            String jobId = getJobIdFromMapKey(key);
            boolean isWrongJob = p_data.wrongJobNames.contains(jobId);
            HashMap<String, ProjectWorkflowData> localeMap = p_projectMap
                    .get(key);
            ArrayList<String> locales = new ArrayList<String>(
                    localeMap.keySet());
            SortUtil.sort(locales);
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

                // Company Name
                p_sheets[MONTH_SHEET].addCell(new Label(col++, row,
                        data.companyName, temp_normalFormat));
                p_sheets[MONTH_SHEET].setColumnView(col - 1, 15);

                // Project Description
                p_sheets[MONTH_SHEET].addCell(new Label(col++, row,
                        data.projectDesc, temp_normalFormat));
                p_sheets[MONTH_SHEET].setColumnView(col - 1, 22);

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
                        getAllSouceFileFormats(data.allFileProfiles),
                        temp_normalFormat));
                p_sheets[MONTH_SHEET].setColumnView(col - 1, 20);

                p_sheets[MONTH_SHEET].addCell(new Label(col++, row,
                        data.l10nProfileName, temp_normalFormat));
                p_sheets[MONTH_SHEET].setColumnView(col - 1, 20);

                p_sheets[MONTH_SHEET].addCell(new Label(col++, row,
                        data.fileProfileNames, temp_normalFormat));
                p_sheets[MONTH_SHEET].setColumnView(col - 1, 20);

                if (data.wasExportFailed)
                {
                    p_sheets[MONTH_SHEET].addCell(new DateTime(col++, row,
                            data.creationDate, failed_dateFormat));
                    p_sheets[MONTH_SHEET].setColumnView(col - 1, 15);
                    p_sheets[MONTH_SHEET].addCell(new DateTime(col++, row,
                            data.creationDate, failed_timeFormat));
                    p_sheets[MONTH_SHEET].setColumnView(col - 1, 18);
                    p_sheets[MONTH_SHEET].addCell(new Label(col++, row, "",
                            failed_dateFormat));
                    p_sheets[MONTH_SHEET].setColumnView(col - 1, 15);
                    p_sheets[MONTH_SHEET].addCell(new Label(col++, row, "",
                            failed_timeFormat));
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
                        labelExportDate = getExportDateStr(dateFormat,
                                data.exportDate);
                    }
                    p_sheets[MONTH_SHEET].addCell(new Label(col++, row,
                            labelExportDate));
                    p_sheets[MONTH_SHEET].setColumnView(col - 1, 15);
                    String labelExportTime = "";
                    if (data.wasExported)
                    {
                        labelExportTime = getExportDateStr(timeFormat,
                                data.exportDate);
                    }
                    p_sheets[MONTH_SHEET].addCell(new Label(col++, row,
                            labelExportTime));

                }
                p_sheets[MONTH_SHEET].setColumnView(col - 1, 18);

                // Status
                p_sheets[MONTH_SHEET].addCell(new Label(col++, row,
                        data.status, temp_normalFormat));
                p_sheets[MONTH_SHEET].setColumnView(col - 1, 18);
                // Language
                p_sheets[MONTH_SHEET].addCell(new Label(col++, row,
                        data.targetLang, temp_normalFormat));

                int numwidth = 10;

                // Summary Start Column
                p_data.initSumStartCol(col);
                p_sheets[MONTH_SHEET].addCell(new Number(col++, row,
                        data.segmentTmWordCount, temp_normalFormat));
                p_sheets[MONTH_SHEET].setColumnView(col - 1, numwidth);

                p_sheets[MONTH_SHEET].addCell(new Number(col++, row,
                        data.hiFuzzyMatchWordCount, temp_normalFormat));
                p_sheets[MONTH_SHEET].setColumnView(col - 1, numwidth);

                p_sheets[MONTH_SHEET].addCell(new Number(col++, row,
                        data.medHiFuzzyMatchWordCount, temp_normalFormat));
                p_sheets[MONTH_SHEET].setColumnView(col - 1, numwidth);

                p_sheets[MONTH_SHEET].addCell(new Number(col++, row,
                        data.medFuzzyMatchWordCount, temp_normalFormat));
                p_sheets[MONTH_SHEET].setColumnView(col - 1, numwidth);

                p_sheets[MONTH_SHEET].addCell(new Number(col++, row,
                        data.noMatchWordCount, temp_normalFormat));
                p_sheets[MONTH_SHEET].setColumnView(col - 1, numwidth);

                p_sheets[MONTH_SHEET].addCell(new Number(col++, row,
                        data.repetitionWordCount, temp_normalFormat));
                p_sheets[MONTH_SHEET].setColumnView(col - 1, numwidth);

                if (p_data.useInContext)
                {
                    p_sheets[MONTH_SHEET].addCell(new Number(col++, row,
                            data.inContextMatchWordCount, temp_normalFormat));
                    p_sheets[MONTH_SHEET].setColumnView(col - 1, numwidth);
                }

                if (p_data.useDefaultContext)
                {
                    p_sheets[MONTH_SHEET].addCell(new Number(col++, row,
                            data.contextMatchWordCount, temp_normalFormat));
                    p_sheets[MONTH_SHEET].setColumnView(col - 1, numwidth);
                }

                p_sheets[MONTH_SHEET].addCell(new Number(col++, row,
                        data.totalWordCount, temp_normalFormat));
                p_sheets[MONTH_SHEET].setColumnView(col - 1, numwidth);

                int moneywidth = 12;

                p_sheets[MONTH_SHEET]
                        .addCell(new Number(col++, row,
                                asDouble(data.segmentTmWordCountCost),
                                temp_moneyFormat));
                p_sheets[MONTH_SHEET].setColumnView(col - 1, moneywidth);

                p_sheets[MONTH_SHEET].addCell(new Number(col++, row,
                        asDouble(data.hiFuzzyMatchWordCountCost),
                        temp_moneyFormat));
                p_sheets[MONTH_SHEET].setColumnView(col - 1, moneywidth);

                p_sheets[MONTH_SHEET].addCell(new Number(col++, row,
                        asDouble(data.medHiFuzzyMatchWordCountCost),
                        temp_moneyFormat));
                p_sheets[MONTH_SHEET].setColumnView(col - 1, moneywidth);

                p_sheets[MONTH_SHEET].addCell(new Number(col++, row,
                        asDouble(data.medFuzzyMatchWordCountCost),
                        temp_moneyFormat));
                p_sheets[MONTH_SHEET].setColumnView(col - 1, moneywidth);

                p_sheets[MONTH_SHEET].addCell(new Number(col++, row,
                        asDouble(data.noMatchWordCountCost), temp_moneyFormat));
                p_sheets[MONTH_SHEET].setColumnView(col - 1, moneywidth);

                p_sheets[MONTH_SHEET].addCell(new Number(col++, row,
                        asDouble(data.repetitionWordCountCost),
                        temp_moneyFormat));
                p_sheets[MONTH_SHEET].setColumnView(col - 1, moneywidth);

                if (p_data.useInContext)
                {
                    p_sheets[MONTH_SHEET].addCell(new Number(col++, row,
                            asDouble(data.inContextMatchWordCountCost),
                            temp_moneyFormat));
                    p_sheets[MONTH_SHEET].setColumnView(col - 1, moneywidth);
                }
                if (p_data.useDefaultContext)
                {
                    p_sheets[MONTH_SHEET].addCell(new Number(col++, row,
                            asDouble(data.contextMatchWordCountCost),
                            temp_moneyFormat));
                    p_sheets[MONTH_SHEET].setColumnView(col - 1, moneywidth);
                }

                if (totalCol == null)
                {
                    totalCol = ReportUtil.toChar(col);
                }
                p_sheets[MONTH_SHEET].addCell(new Number(col++, row,
                        asDouble(data.totalWordCountCost), temp_moneyFormat));

                // Show total additional charges
                // p_sheets[MONTH_SHEET].setColumnView(col - 1, moneywidth);
                // p_sheets[MONTH_SHEET].addCell(new Number(col++, row,
                // asDouble(data.totalAdditionalCost), temp_moneyFormat));

                // p_sheets[MONTH_SHEET].setColumnView(col - 1, moneywidth);
                projectTotalWordCountCost = projectTotalWordCountCost
                        .add(data.totalWordCountCost);

                List<Integer> indexs = totalCost.get(data.targetLang);
                if (indexs == null)
                {
                    indexs = new ArrayList<Integer>();
                    totalCost.put(data.targetLang, indexs);
                }
                indexs.add(row + 1);

                Double value = totalCostDate.get(data.targetLang);
                if (value == null)
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
                            asDouble(data.totalAdditionalCost),
                            temp_moneyFormat));
                    p_sheets[MONTH_SHEET].setColumnView(col - 1, moneywidth);

                    p_sheets[MONTH_SHEET].addCell(new Number(col++, row,
                            asDouble(projectTotalWordCountCost)
                                    + asDouble(data.totalAdditionalCost),
                            temp_moneyFormat));
                }
                else
                {
                    p_sheets[MONTH_SHEET].addCell(new Blank(col++, row,
                            temp_moneyFormat));
                    p_sheets[MONTH_SHEET].setColumnView(col - 1, moneywidth);

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
                        /*
                         * p_sheets[MONTH_SHEET].addCell(new DateTime(col++,
                         * row, data.creationDate, failed_dateFormat));
                         */
                        p_sheets[MONTH_REVIEW_SHEET].addCell(new DateTime(
                                col++, row, data.creationDate,
                                failed_dateFormat));
                        p_sheets[MONTH_REVIEW_SHEET].setColumnView(col - 1, 15);
                        p_sheets[MONTH_REVIEW_SHEET].addCell(new DateTime(
                                col++, row, data.creationDate,
                                failed_timeFormat));
                        p_sheets[MONTH_REVIEW_SHEET].setColumnView(col - 1, 18);
                        p_sheets[MONTH_REVIEW_SHEET].addCell(new Label(col++,
                                row, "", failed_dateFormat));
                        p_sheets[MONTH_REVIEW_SHEET].setColumnView(col - 1, 15);
                        p_sheets[MONTH_REVIEW_SHEET].addCell(new Label(col++,
                                row, "", failed_timeFormat));
                    }
                    else
                    {
                        /*
                         * p_sheets[MONTH_SHEET].addCell(new Label(col++, row,
                         * dateFormat.format(data.creationDate)));
                         */
                        p_sheets[MONTH_REVIEW_SHEET].addCell(new Label(col++,
                                row, dateFormat.format(data.creationDate)));
                        p_sheets[MONTH_REVIEW_SHEET].setColumnView(col - 1, 15);
                        p_sheets[MONTH_REVIEW_SHEET].addCell(new Label(col++,
                                row, timeFormat.format(data.creationDate)));
                        p_sheets[MONTH_REVIEW_SHEET].setColumnView(col - 1, 18);
                        String labelExportDate = "";
                        if (data.wasExported)
                        {
                            labelExportDate = getExportDateStr(dateFormat,
                                    data.exportDate);
                        }
                        p_sheets[MONTH_REVIEW_SHEET].addCell(new Label(col++,
                                row, labelExportDate));
                        p_sheets[MONTH_REVIEW_SHEET].setColumnView(col - 1, 15);
                        String labelExportTime = "";
                        if (data.wasExported)
                        {
                            labelExportTime = getExportDateStr(timeFormat,
                                    data.exportDate);
                        }
                        p_sheets[MONTH_REVIEW_SHEET].addCell(new Label(col++,
                                row, labelExportTime));

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
                    if (p_data.useDefaultContext)
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

                    if (p_data.useDefaultContext)
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
            addTotalsPerLang(p_sheets[MONTH_SHEET], MONTH_SHEET,
                    p_rows[MONTH_SHEET], p_data, bundle);
        }
    }

    private void addTotalsPerLang(WritableSheet p_sheet,
            final int p_sheetCategory, IntHolder p_row, MyData p_data,
            ResourceBundle bundle) throws Exception
    {
        WritableFont subTotalFont = new WritableFont(WritableFont.ARIAL, 10,
                WritableFont.BOLD, false, UnderlineStyle.NO_UNDERLINE,
                jxl.format.Colour.BLACK);
        WritableCellFormat subTotalFormat = new WritableCellFormat(subTotalFont);
        subTotalFormat.setShrinkToFit(false);
        String title = bundle.getString("lb_total_cost_per_lang");

        int row = p_row.getValue() + 4; // skip a row
        ArrayList<String> locales = new ArrayList<String>(totalCost.keySet());
        SortUtil.sort(locales);

        int col = p_data.getSumStartCol();
        p_sheet.addCell(new Label(col - 3, row, title, subTotalFormat));

        File imgFile = File.createTempFile("GSJobChart", ".png");
        JfreeCharUtil.drawPieChart2D("", totalCostDate, imgFile);
        WritableImage img = new WritableImage(15, row - 1, 7, 25, imgFile);
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

            p_sheet.addCell(new Label(col - 1, row, locale));
            p_sheet.addCell(new Formula(col, row++, values.toString(),
                    moneyFormat));
        }

        // Reset total column number for every sheet.
        totalCol = null;
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

    /**
     * Add totals row
     * 
     * @param p_sheet
     * @param p_sheetCategory
     * @param p_row
     * @param p_data
     * @param bundle
     * @throws Exception
     */
    private void addTotals(WritableSheet p_sheet, final int p_sheetCategory,
            IntHolder p_row, MyData p_data, ResourceBundle bundle)
            throws Exception
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

        // Get Summary Start Column
        int c = p_data.getSumStartCol();
        String sumStartCol = getColumnName(c);

        p_sheet.addCell(new Label(0, row, title, subTotalFormat));
        // modify the number 3 to "sumStartCellCol-B" for "Add Job Id into
        // online job report" issue
        p_sheet.mergeCells(0, row, sumStartCol.charAt(0) - 'B', row);
        int lastRow = p_row.getValue() - 2;

        // add in word count totals
        // word counts
        p_sheet.addCell(new Formula(c++, row, "SUM(" + sumStartCol + "5:"
                + sumStartCol + lastRow + ")", subTotalFormat));
        sumStartCol = getColumnName(c);
        p_sheet.addCell(new Formula(c++, row, "SUM(" + sumStartCol + "5:"
                + sumStartCol + lastRow + ")", subTotalFormat));
        sumStartCol = getColumnName(c);
        p_sheet.addCell(new Formula(c++, row, "SUM(" + sumStartCol + "5:"
                + sumStartCol + lastRow + ")", subTotalFormat));
        sumStartCol = getColumnName(c);
        p_sheet.addCell(new Formula(c++, row, "SUM(" + sumStartCol + "5:"
                + sumStartCol + lastRow + ")", subTotalFormat));
        sumStartCol = getColumnName(c);
        p_sheet.addCell(new Formula(c++, row, "SUM(" + sumStartCol + "5:"
                + sumStartCol + lastRow + ")", subTotalFormat));
        sumStartCol = getColumnName(c);
        if (p_data.isTradosStyle())
        {
            p_sheet.addCell(new Formula(c++, row, "SUM(" + sumStartCol + "5:"
                    + sumStartCol + lastRow + ")", subTotalFormat));
            sumStartCol = getColumnName(c);
            p_sheet.addCell(new Formula(c++, row, "SUM(" + sumStartCol + "5:"
                    + sumStartCol + lastRow + ")", subTotalFormat));
            sumStartCol = getColumnName(c);
        }

        if (p_data.useInContext)
        {
            p_sheet.addCell(new Formula(c++, row, "SUM(" + sumStartCol + "5:"
                    + sumStartCol + lastRow + ")", subTotalFormat));
            sumStartCol = getColumnName(c);
        }
        if (p_data.useDefaultContext)
        {
            p_sheet.addCell(new Formula(c++, row, "SUM(" + sumStartCol + "5:"
                    + sumStartCol + lastRow + ")", subTotalFormat));
            sumStartCol = getColumnName(c);
        }

        // word count costs
        p_sheet.addCell(new Formula(c++, row, "SUM(" + sumStartCol + "5" + ":"
                + sumStartCol + lastRow + ")", moneyFormat));
        sumStartCol = getColumnName(c);
        p_sheet.addCell(new Formula(c++, row, "SUM(" + sumStartCol + "5" + ":"
                + sumStartCol + lastRow + ")", moneyFormat));
        sumStartCol = getColumnName(c);
        p_sheet.addCell(new Formula(c++, row, "SUM(" + sumStartCol + "5" + ":"
                + sumStartCol + lastRow + ")", moneyFormat));
        sumStartCol = getColumnName(c);
        p_sheet.addCell(new Formula(c++, row, "SUM(" + sumStartCol + "5" + ":"
                + sumStartCol + lastRow + ")", moneyFormat));
        sumStartCol = getColumnName(c);
        p_sheet.addCell(new Formula(c++, row, "SUM(" + sumStartCol + "5" + ":"
                + sumStartCol + lastRow + ")", moneyFormat));
        sumStartCol = getColumnName(c);
        p_sheet.addCell(new Formula(c++, row, "SUM(" + sumStartCol + "5" + ":"
                + sumStartCol + lastRow + ")", moneyFormat));
        sumStartCol = getColumnName(c);
        p_sheet.addCell(new Formula(c++, row, "SUM(" + sumStartCol + "5" + ":"
                + sumStartCol + lastRow + ")", moneyFormat));
        sumStartCol = getColumnName(c);

        if (p_data.isTradosStyle())
        {
            p_sheet.addCell(new Formula(c++, row, "SUM(" + sumStartCol + "5"
                    + ":" + sumStartCol + lastRow + ")", moneyFormat));
            sumStartCol = getColumnName(c);
            p_sheet.addCell(new Formula(c++, row, "SUM(" + sumStartCol + "5"
                    + ":" + sumStartCol + lastRow + ")", moneyFormat));
            sumStartCol = getColumnName(c);
        }

        if (p_data.useInContext)
        {
            p_sheet.addCell(new Formula(c++, row, "SUM(" + sumStartCol + "5"
                    + ":" + sumStartCol + lastRow + ")", moneyFormat));
            sumStartCol = getColumnName(c);
        }
        if (p_data.useDefaultContext)
        {
            p_sheet.addCell(new Formula(c++, row, "SUM(" + sumStartCol + "5"
                    + ":" + sumStartCol + lastRow + ")", moneyFormat));
            sumStartCol = getColumnName(c);
        }

        /*
         * Canceled useless column in "Review Detail report" Sheet. if
         * (p_sheetCategory == MONTH_REVIEW_SHEET) { // For
         * "Job id not showing in external review tabs" Issue
         * p_sheet.addCell(new Formula(c++, row, "SUM(" + sumStartCol + "5" +
         * ":" + sumStartCol + lastRow + ")", moneyFormat)); }
         */

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
            logger.error(
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
                sc.getStringParameter(SystemConfigParamNames.GLOBALSIGHT_HOME_DIRECTORY));
        mapFile.append(File.separator);
        mapFile.append("jobsInWrongDivision.txt");
        File f = new File(mapFile.toString());
        if (f.exists() == false)
        {
            if (p_data.warnedAboutMissingWrongJobsFile == false)
            {
                logger.info("jobsInWrongDivision.txt file not found.");
                p_data.warnedAboutMissingWrongJobsFile = true;
            }
            return wrongJobs;
        }
        BufferedReader reader = new BufferedReader(new FileReader(f));
        String line = null;

        while ((line = reader.readLine()) != null)
        {
            if (line.startsWith("#") || line.length() == 0)
                continue;
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
                logger.warn("Ignoring mapping line for "
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
        if (p == null)
            p = p_job.getL10nProfile().getProject();
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
        for (Job job : jobs)
        {
            if (PageHandler.isDefaultContextMatch(job))
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

    /**
     * This method is used for getting the column name, "A".."Z" "AA".."AZ"
     * 
     * @param c
     * @return
     */
    private String getColumnName(int c)
    {
        String sumStartCellCol = null;
        if (c > 25)
        {
            sumStartCellCol = "A" + String.valueOf((char) ('A' + c - 26));
        }
        else
        {
            sumStartCellCol = String.valueOf((char) ('A' + c));
        }
        return sumStartCellCol;
    }

    private String getAllSouceFileFormats(List<FileProfile> p_fps)
    {
        Set<String> result = new HashSet<String>();
        KnownFormatType type = null;
        for (FileProfile fp : p_fps)
        {
            long id = fp.getKnownFormatTypeId();
            try
            {
                type = ServerProxy.getFileProfilePersistenceManager()
                        .queryKnownFormatType(id);
            }
            catch (Exception e)
            {
                logger.error("Can't get KnownFormatType in Online Jobs Report",
                        e);
            }

            if (type != null && !type.getName().isEmpty())
            {
                result.add(type.getName());
            }
        }

        String resultStr = result.toString();
        return resultStr.substring(1, resultStr.length() - 1);
    }

    private void setJobIds(HashSet<Job> jobs)
    {
        if (m_data != null
                && (m_data.jobIds == null || m_data.jobIds.size() == 0))
        {
            m_data.jobIds = new ArrayList();
            for (Job job : jobs)
            {
                m_data.jobIds.add(job.getJobId());
            }
        }
    }

    @Override
    public String getReportType()
    {
        return ReportConstants.ONLINE_JOBS_REPORT;
    }

    @Override
    public void setPercent(int p_finishedJobNum)
    {
        ReportGeneratorHandler.setReportsMapByGenerator(m_userId,
                m_data.jobIds, 100 * p_finishedJobNum / m_data.jobIds.size(),
                getReportType());
    }

    @Override
    public boolean isCancelled()
    {
        return ReportGeneratorHandler.isCancelled(m_userId, null,
                getReportType());
    }

    private String getExportDateStr(SimpleDateFormat sdf, Date exportDate)
    {
        if (exportDate == null)
        {
            return "";
        }
        else
        {
            return sdf.format(exportDate);
        }
    }
}
