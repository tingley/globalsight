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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.format.UnderlineStyle;
import jxl.write.DateFormats;
import jxl.write.DateTime;
import jxl.write.Formula;
import jxl.write.Label;
import jxl.write.Number;
import jxl.write.NumberFormat;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

import org.apache.log4j.Logger;

import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.costing.Cost;
import com.globalsight.everest.costing.CostByWordCount;
import com.globalsight.everest.costing.Currency;
import com.globalsight.everest.foundation.SearchCriteriaParameters;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.jobhandler.JobSearchParameters;
import com.globalsight.everest.projecthandler.Project;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.util.comparator.JobComparator;
import com.globalsight.everest.util.system.SystemConfigParamNames;
import com.globalsight.everest.util.system.SystemConfiguration;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.pagehandler.administration.reports.bo.ReportsData;
import com.globalsight.everest.webapp.pagehandler.administration.reports.util.ReportUtil;
import com.globalsight.everest.webapp.pagehandler.projects.workflows.JobSearchConstants;
import com.globalsight.everest.workflowmanager.Workflow;
import com.globalsight.util.IntHolder;
import com.globalsight.util.SortUtil;

public class VendorPOXlsReport extends XlsReports
{
    private static Logger s_logger = Logger.getLogger("Reports");

    // defines a 0 format for a 3 decimal precision point BigDecimal
    private static final String BIG_DECIMAL_ZERO_STRING = "0.000";

    // the big decimal scale to use for internal math
    private static int SCALE = 3;

    private WritableWorkbook m_workbook = null;

    /* The symbol of the currency from the request */
    private String symbol = null;
    private String currency = null;

    private void init(HttpServletRequest p_request, MyData p_data)
    {
        currency = p_request.getParameter("currency");
        symbol = ReportUtil.getCurrencySymbol(currency);
        setJobNamesList(p_request, p_data);
        setProjectIdList(p_request, p_data);
        setJobStatusList(p_request, p_data);
        setTargetLangList(p_request, p_data);
    }

    public VendorPOXlsReport(HttpServletRequest request,
            HttpServletResponse response) throws Exception
    {
        MyData data = new MyData();
        init(request, data);
        generateReport(request, response, data);
    }

    /**
     * 
     * The inner class for store some useful parameter
     * 
     */
    private class MyData
    {
        private boolean wantsAllProjects = false;
        private boolean wantsAllTargetLangs = false;
        private ArrayList<Long> projectIdList = new ArrayList<Long>();
        private ArrayList<String> targetLangList = new ArrayList<String>();
        private boolean wantsAllJobNames = false;
        private boolean wantsAllJobStatus = false;
        private ArrayList<Long> jobIdList = new ArrayList<Long>();
        private ArrayList<String> jobStatusList = new ArrayList<String>();
        // maps jobs to new project
        private Hashtable<Long, Project> wrongJobMap = new Hashtable<Long, Project>();
        private boolean warnedAboutMissingWrongJobsFile = false;
        private ArrayList<Job> wrongJobs = new ArrayList<Job>();
        private ArrayList<String> wrongJobNames = new ArrayList<String>();
        // wrong jobs which may be queried for the project which should be
        // ignored
        private ArrayList<Job> ignoreJobs = new ArrayList<Job>();
        private WritableSheet dellSheet = null;
        private WritableSheet tradosSheet = null;
        private String[] headers = null;
    };

    /**
     * Generates the Excel report as a temp file and returns the temp file.
     * 
     * @return File
     * @exception Exception
     */
    private void generateReport(HttpServletRequest p_request,
            HttpServletResponse p_response, MyData p_data) throws Exception
    {
        String userId = (String) p_request.getSession().getAttribute(
                WebAppConstants.USER_NAME);
        ResourceBundle bundle = PageHandler.getBundle(p_request.getSession());
        String EMEA = CompanyWrapper.getCurrentCompanyName();
        s_logger.debug("generateReport---, company name: " + EMEA);
        WorkbookSettings settings = new WorkbookSettings();
        settings.setSuppressWarnings(true);
        m_workbook = Workbook.createWorkbook(p_response.getOutputStream(),
                settings);
        boolean recalculateFinishedWorkflow = false;
        String recalcParam = p_request.getParameter("recalc");
        if (recalcParam != null && recalcParam.length() > 0)
        {
            recalculateFinishedWorkflow = java.lang.Boolean
                    .valueOf(recalcParam).booleanValue();
        }

        // get all the jobs that were originally imported with the wrong project
        // the users want to pretend that these jobs are in this project
        getJobsInWrongProject(p_data);
        HashMap projectMap = getProjectData(p_request,
                recalculateFinishedWorkflow, p_data);
        p_data.dellSheet = m_workbook.createSheet(
                EMEA + " " + bundle.getString("lb_matches"), 0);
        p_data.tradosSheet = m_workbook.createSheet(
                bundle.getString("jobinfo.tradosmatches"), 1);
        List<Long> reportJobIDS = null;
        // Cancel Duplicate Request
        if (ReportHelper.checkReportsDataInProgressStatus(userId,
                reportJobIDS, getReportType()))
        {
            p_response.sendError(p_response.SC_NO_CONTENT);
            return;
        }
        // Set ReportsData.
        ReportHelper.setReportsData(userId, reportJobIDS, getReportType(),
                0, ReportsData.STATUS_INPROGRESS);

        addHeaderForDellMatches(p_data, bundle);
        addHeaderForTradosMatches(p_data, bundle);
        IntHolder row = new IntHolder(4);
        writeProjectDataForDellMatches(projectMap, row, p_data, bundle);
        row = new IntHolder(4);
        writeProjectDataForTradosMatches(projectMap, row, p_data, bundle);

        WritableSheet paramsSheet = m_workbook.createSheet(
                bundle.getString("lb_criteria"), 2);
        paramsSheet.addCell(new Label(0, 0, bundle
                .getString("lb_report_criteria")));
        paramsSheet.setColumnView(0, 50);

        if (p_data.wantsAllProjects)
        {
            paramsSheet.addCell(new Label(0, 1, bundle
                    .getString("lb_selected_projects")
                    + " "
                    + bundle.getString("all")));
        }
        else
        {
            paramsSheet.addCell(new Label(0, 1, bundle
                    .getString("lb_selected_projects")));
            Iterator<Long> iter = p_data.projectIdList.iterator();
            int r = 2;
            while (iter.hasNext())
            {
                Long pid = (Long) iter.next();
                String projectName = "??";
                try
                {
                    Project p = ServerProxy.getProjectHandler().getProjectById(
                            pid.longValue());
                    projectName = p.getName();
                }
                catch (Exception e)
                {
                }
                String v = projectName + " ("
                        + bundle.getString("lb_report_id") + "="
                        + pid.toString() + ")";
                paramsSheet.addCell(new Label(0, r, v));
                r++;
            }
        }

        // add the date criteria
        String paramCreateDateStartCount = p_request
                .getParameter(JobSearchConstants.CREATION_START);
        String paramCreateDateStartOpts = p_request
                .getParameter(JobSearchConstants.CREATION_START_OPTIONS);
        String paramCreateDateEndCount = p_request
                .getParameter(JobSearchConstants.CREATION_END);
        String paramCreateDateEndOpts = p_request
                .getParameter(JobSearchConstants.CREATION_END_OPTIONS);
        paramsSheet.addCell(new Label(1, 1, bundle.getString("lb_from") + ":"));
        String fromMsg = paramCreateDateStartCount
                + " "
                + getDateCritieraConditionValue(p_request,
                        paramCreateDateStartOpts);
        String untilMsg = paramCreateDateEndCount
                + " "
                + getDateCritieraConditionValue(p_request,
                        paramCreateDateEndOpts);
        paramsSheet.addCell(new Label(1, 2, fromMsg));
        paramsSheet
                .addCell(new Label(2, 1, bundle.getString("lb_until") + ":"));
        paramsSheet.addCell(new Label(2, 2, untilMsg));

        // add the target lang criteria
        if (p_data.wantsAllTargetLangs)
        {
            paramsSheet.addCell(new Label(3, 1, bundle
                    .getString("lb_selected_langs")
                    + " "
                    + bundle.getString("all")));
        }
        else
        {
            paramsSheet.addCell(new Label(3, 1, bundle
                    .getString("lb_selected_langs")));
            Iterator<String> iter = p_data.targetLangList.iterator();
            int r = 2;
            while (iter.hasNext())
            {
                String lang = iter.next();
                paramsSheet.addCell(new Label(3, r, lang));
                r++;
            }
            paramsSheet.setColumnView(3, 15);

        }

        m_workbook.write();
        m_workbook.close();

        // Set ReportsData.
        ReportHelper.setReportsData(userId, reportJobIDS, getReportType(),
                100, ReportsData.STATUS_FINISHED);
    }

    private String getDateCritieraConditionValue(HttpServletRequest p_request,
            String p_condition)
    {
        ResourceBundle bundle = PageHandler.getBundle(p_request
                .getSession(false));
        String value = "N/A";
        if (SearchCriteriaParameters.NOW.equals(p_condition))
            value = bundle.getString("lb_now");
        else if (SearchCriteriaParameters.DAYS_AGO.equals(p_condition))
            value = bundle.getString("lb_days_ago");
        else if (SearchCriteriaParameters.HOURS_AGO.equals(p_condition))
            value = bundle.getString("lb_hours_ago");
        else if (SearchCriteriaParameters.WEEKS_AGO.equals(p_condition))
            value = bundle.getString("lb_weeks_ago");
        else if (SearchCriteriaParameters.MONTHS_AGO.equals(p_condition))
            value = bundle.getString("lb_months_ago");
        return value;
    }

    /**
     * Adds data for the exported and in-progress jobs. Archived jobs are
     * ignored for now.
     * 
     * @return HashMap
     * @exception Exception
     */

    private HashMap getProjectData(HttpServletRequest p_request,
            boolean p_recalculateFinishedWorkflow, MyData p_data)
            throws Exception
    {
        ArrayList queriedJobs = new ArrayList();
        if (p_data.wantsAllJobNames)
        {
            queriedJobs.addAll(ServerProxy.getJobHandler().getJobs(
                    getSearchParams(p_request, p_data)));
            // sort jobs by job name
            SortUtil.sort(queriedJobs, new JobComparator(Locale.US));
        }
        else
        {
            for (int i = 0; i < p_data.jobIdList.size(); i++)
            {
                Job j = ServerProxy.getJobHandler().getJobById(
                        p_data.jobIdList.get(i));
                queriedJobs.add(j);
            }
        }

        ArrayList<Job> wrongJobs = getWrongJobs(p_data);
        // now create a Set of all the jobs
        HashSet<Job> jobs = new HashSet<Job>();
        jobs.addAll(queriedJobs);
        jobs.addAll(wrongJobs);
        jobs.removeAll(p_data.ignoreJobs);
        Iterator<Job> jobIter = jobs.iterator();

        p_data.headers = getHeaders(jobIter);

        jobIter = jobs.iterator();

        // first iterate through the Jobs and group by Project/workflow because
        // Dell
        // doesn't want to see actual Jobs
        HashMap projectMap = new HashMap();
        Currency pivotCurrency = ServerProxy.getCostingEngine()
                .getCurrencyByName(ReportUtil.getCurrencyName(currency),
                        CompanyThreadLocal.getInstance().getValue());
        while (jobIter.hasNext())
        {
            Job j = jobIter.next();

            // only handle jobs in these states
            if (!(Job.READY_TO_BE_DISPATCHED.equals(j.getState())
                    || Job.DISPATCHED.equals(j.getState())
                    || Job.EXPORTED.equals(j.getState())
                    || Job.EXPORT_FAIL.equals(j.getState())
                    || Job.ARCHIVED.equals(j.getState()) || Job.LOCALIZED
                        .equals(j.getState())))
            {
                continue;
            }

            Collection<Workflow> c = j.getWorkflows();
            Iterator<Workflow> wfIter = c.iterator();
            String projectDesc = getProjectDesc(p_data, j);
            while (wfIter.hasNext())
            {
                Workflow w = wfIter.next();
                // TranslationMemoryProfile tmProfile =
                // w.getJob().getL10nProfile().getTranslationMemoryProfile();
                String state = w.getState();
                // skip certain workflows
                if (!(Workflow.READY_TO_BE_DISPATCHED.equals(state)
                        || Workflow.EXPORTED.equals(state)
                        || Workflow.DISPATCHED.equals(state)
                        || Workflow.LOCALIZED.equals(state)
                        || Workflow.EXPORT_FAILED.equals(state) || Workflow.ARCHIVED
                            .equals(state)))
                {
                    continue;
                }
                // String targetLang =w.getTargetLocale().getLanguageCode();
                String targetLang = w.getTargetLocale().toString();

                if (!p_data.wantsAllTargetLangs
                        && !p_data.targetLangList.contains(targetLang))
                {
                    continue;
                }

                String jobName = j.getJobName();
                long jobId = j.getId();
                HashMap localeMap = (HashMap) projectMap.get(Long
                        .toString(jobId));
                if (localeMap == null)
                {
                    localeMap = new HashMap();
                    projectMap.put(Long.toString(jobId), localeMap);
                }

                ProjectWorkflowData data = (ProjectWorkflowData) localeMap
                        .get(targetLang);
                if (data == null)
                {
                    data = new ProjectWorkflowData();
                    localeMap.put(targetLang, data);
                    data.jobName = jobName;
                    data.jobId = jobId;
                    data.poNumber = j.getQuotePoNumber() == null ? "" : j
                            .getQuotePoNumber();
                    data.projectDesc = projectDesc;
                    data.targetLang = targetLang;
                    data.creationDate = j.getCreateDate();
                }
                if (Workflow.EXPORT_FAILED.equals(state))
                {
                    // if the workflow is EXPORT_FAILED, color the line in red
                    data.wasExportFailed = true;
                }
                // now add or amend the data in the ProjectWorkflowData based on
                // this next workflow
                if (j.getCreateDate().before(data.creationDate))
                    data.creationDate = j.getCreateDate();

                data.repetitionWordCount += w.getRepetitionWordCount();
                data.dellInternalRepsWordCount += w.getRepetitionWordCount();
                data.tradosRepsWordCount = data.dellInternalRepsWordCount;

                data.lowFuzzyMatchWordCount += w
                        .getThresholdLowFuzzyWordCount();
                data.medFuzzyMatchWordCount += w
                        .getThresholdMedFuzzyWordCount();
                data.medHiFuzzyMatchWordCount += w
                        .getThresholdMedHiFuzzyWordCount();
                data.hiFuzzyMatchWordCount += w.getThresholdHiFuzzyWordCount();

                // the Dell fuzzyMatchWordCount is the sum of the top 3
                // categories
                data.dellFuzzyMatchWordCount = data.medFuzzyMatchWordCount
                        + data.medHiFuzzyMatchWordCount
                        + data.hiFuzzyMatchWordCount;
                data.trados95to99WordCount = data.hiFuzzyMatchWordCount;
                data.trados85to94WordCount = data.medHiFuzzyMatchWordCount;
                data.trados75to84WordCount = data.medFuzzyMatchWordCount;
                data.trados50to74WordCount = data.lowFuzzyMatchWordCount;
                // no match word count, do not consider Threshold

                // add the lowest fuzzies and sublev match to nomatch
                data.dellNewWordsWordCount = w.getNoMatchWordCount()
                        + w.getLowFuzzyMatchWordCount();
                data.tradosNoMatchWordCount += w.getThresholdNoMatchWordCount();
                if (PageHandler.isInContextMatch(w.getJob()))
                {
                    data.segmentTmWordCount += w.getSegmentTmWordCount();
                    data.dellExactMatchWordCount += w.getSegmentTmWordCount();
                    data.dellInContextMatchWordCount += w
                            .getInContextMatchWordCount();
                }
                else if (PageHandler.isDefaultContextMatch(w.getJob()))
                {
                    data.segmentTmWordCount += w.getTotalExactMatchWordCount()
                            - w.getContextMatchWordCount();
                    data.dellExactMatchWordCount += data.segmentTmWordCount;
                    data.contextMatchWordCount += w.getContextMatchWordCount();
                }
                else
                {
                    data.segmentTmWordCount += w.getTotalExactMatchWordCount();
                    data.dellExactMatchWordCount += w
                            .getTotalExactMatchWordCount();
                    data.dellInContextMatchWordCount += w
                            .getNoUseInContextMatchWordCount();
                    data.contextMatchWordCount += 0;
                }
                data.trados100WordCount = data.dellExactMatchWordCount;
                data.tradosInContextMatchWordCount = data.dellInContextMatchWordCount;
                data.tradosContextMatchWordCount = data.contextMatchWordCount;
                data.dellContextMatchWordCount = data.contextMatchWordCount;
                data.totalWordCount += w.getTotalWordCount();

                data.dellTotalWordCount = data.dellFuzzyMatchWordCount
                        + data.dellInternalRepsWordCount
                        + data.dellExactMatchWordCount
                        + data.dellNewWordsWordCount
                        + data.dellInContextMatchWordCount
                        + data.dellContextMatchWordCount;
                data.tradosTotalWordCount = data.trados100WordCount
                        + data.tradosInContextMatchWordCount
                        + data.tradosContextMatchWordCount
                        + data.trados95to99WordCount
                        + data.trados85to94WordCount
                        + data.trados75to84WordCount
                        + data.trados50to74WordCount
                        + data.tradosNoMatchWordCount
                        + data.tradosRepsWordCount;

                Cost wfCost = ServerProxy.getCostingEngine().calculateCost(w,
                        pivotCurrency, p_recalculateFinishedWorkflow,
                        Cost.EXPENSE, p_recalculateFinishedWorkflow);
                CostByWordCount costByWordCount = wfCost.getCostByWordCount();
                if (costByWordCount != null)
                {
                    // repetition costs
                    data.repetitionWordCountCost = add(
                            data.repetitionWordCountCost,
                            costByWordCount.getRepetitionCost());
                    data.dellInternalRepsWordCountCost = data.repetitionWordCountCost;
                    data.tradosRepsWordCountCost = data.repetitionWordCountCost;

                    // exact match costs
                    if (PageHandler.isInContextMatch(w.getJob()))
                    {
                        // data.contextMatchWordCountCost =
                        // add(data.contextMatchWordCountCost,
                        // costByWordCount.getContextMatchCost());
                        data.inContextMatchWordCountCost = add(
                                data.inContextMatchWordCountCost,
                                costByWordCount.getInContextMatchCost());
                        data.segmentTmWordCountCost = add(
                                data.segmentTmWordCountCost,
                                costByWordCount.getSegmentTmMatchCost());
                    }
                    else if (PageHandler.isDefaultContextMatch(w.getJob()))
                    {
                        data.contextMatchWordCountCost = add(
                                data.contextMatchWordCountCost,
                                costByWordCount.getContextMatchCost());
                        data.segmentTmWordCountCost = add(
                                data.segmentTmWordCountCost,
                                costByWordCount
                                        .getDefaultContextExactMatchCost());
                    }
                    else
                    {
                        data.inContextMatchWordCountCost = add(
                                data.inContextMatchWordCountCost,
                                costByWordCount.getNoUseInContextMatchCost());
                        data.segmentTmWordCountCost = add(
                                data.segmentTmWordCountCost,
                                costByWordCount.getNoUseExactMatchCost());
                        data.contextMatchWordCountCost = add(new BigDecimal(
                                BIG_DECIMAL_ZERO_STRING),
                                costByWordCount.getContextMatchCost());
                    }
                    data.dellExactMatchWordCountCost = data.segmentTmWordCountCost;
                    data.trados100WordCountCost = data.segmentTmWordCountCost;
                    data.dellInContextMatchWordCountCost = data.inContextMatchWordCountCost;
                    data.tradosInContextWordCountCost = data.inContextMatchWordCountCost;
                    data.tradosContextWordCountCost = data.contextMatchWordCountCost;
                    data.dellContextMatchWordCountCost = data.contextMatchWordCountCost;
                    // fuzzy match costs
                    data.lowFuzzyMatchWordCountCost = add(
                            data.lowFuzzyMatchWordCountCost,
                            costByWordCount.getLowFuzzyMatchCost());
                    data.medFuzzyMatchWordCountCost = add(
                            data.medFuzzyMatchWordCountCost,
                            costByWordCount.getMedFuzzyMatchCost());
                    data.medHiFuzzyMatchWordCountCost = add(
                            data.medHiFuzzyMatchWordCountCost,
                            costByWordCount.getMedHiFuzzyMatchCost());
                    data.hiFuzzyMatchWordCountCost = add(
                            data.hiFuzzyMatchWordCountCost,
                            costByWordCount.getHiFuzzyMatchCost());
                    // Dell fuzzy match cost is the sum of the top three fuzzy
                    // match categories
                    data.dellFuzzyMatchWordCountCost = data.medFuzzyMatchWordCountCost
                            .add(data.medHiFuzzyMatchWordCountCost).add(
                                    data.hiFuzzyMatchWordCountCost);
                    // Trados breakdown for fuzzy
                    data.trados95to99WordCountCost = data.hiFuzzyMatchWordCountCost;
                    data.trados85to94WordCountCost = data.medHiFuzzyMatchWordCountCost;
                    data.trados75to84WordCountCost = data.medFuzzyMatchWordCountCost;
                    data.trados50to74WordCountCost = data.lowFuzzyMatchWordCountCost;

                    // new words, no match costs
                    data.noMatchWordCountCost = add(data.noMatchWordCountCost,
                            costByWordCount.getNoMatchCost());
                    data.tradosNoMatchWordCountCost = data.noMatchWordCountCost;
                    data.dellNewWordsWordCountCost = data.noMatchWordCountCost
                            .add(data.lowFuzzyMatchWordCountCost);

                    // totals
                    data.dellTotalWordCountCost = data.dellInternalRepsWordCountCost
                            .add(data.dellExactMatchWordCountCost)
                            .add(data.dellInContextMatchWordCountCost)
                            .add(data.dellContextMatchWordCountCost)
                            .add(data.dellFuzzyMatchWordCountCost)
                            .add(data.dellNewWordsWordCountCost);
                    data.totalWordCountCost = data.dellTotalWordCountCost;
                    data.tradosTotalWordCountCost = data.trados100WordCountCost
                            .add(data.tradosInContextWordCountCost)
                            .add(data.tradosContextWordCountCost)
                            .add(data.trados95to99WordCountCost)
                            .add(data.trados85to94WordCountCost)
                            .add(data.trados75to84WordCountCost)
                            .add(data.trados50to74WordCountCost)
                            .add(data.tradosRepsWordCountCost)
                            .add(data.tradosNoMatchWordCountCost);
                }
            }
            // now recalculate the job level cost if the work flow was
            // recalculated
            if (p_recalculateFinishedWorkflow)
            {
                ServerProxy.getCostingEngine().reCostJob(j, pivotCurrency,
                        Cost.EXPENSE);
            }
        }
        return projectMap;
    }

    private String[] getHeaders(Iterator<Job> iter)
    {
        String[] headers = new String[2];
        while (iter.hasNext())
        {
            Job job = iter.next();
            if (PageHandler.isInContextMatch(job))
            {
                headers[0] = "In Context Match";
            }
            if (PageHandler.isDefaultContextMatch(job))
            {
                headers[1] = "Context Match";
            }
        }
        return headers;
    }

    /**
     * Class used to group data by project and target language
     */
    private class ProjectWorkflowData
    {
        public String jobName;
        public long jobId = -1;
        public String poNumber = "";
        public String projectDesc;
        public Date creationDate;
        public String targetLang;

        /* Dell values */
        public long dellInternalRepsWordCount = 0;
        public long dellExactMatchWordCount = 0;
        public long dellInContextMatchWordCount = 0;
        public long dellContextMatchWordCount = 0;
        public long dellFuzzyMatchWordCount = 0;
        public long dellNewWordsWordCount = 0;
        public long dellTotalWordCount = 0;

        // "Trados" values
        public long trados100WordCount = 0;
        public long tradosInContextMatchWordCount = 0;
        public long tradosContextMatchWordCount = 0;
        public long trados95to99WordCount = 0;
        public long trados85to94WordCount = 0;
        public long trados75to84WordCount = 0;
        public long trados50to74WordCount = 0;
        public long tradosNoMatchWordCount = 0;
        public long tradosRepsWordCount = 0;
        public long tradosTotalWordCount = 0;

        /* word counts */
        public long repetitionWordCount = 0;
        public long lowFuzzyMatchWordCount = 0;
        public long medFuzzyMatchWordCount = 0;
        public long medHiFuzzyMatchWordCount = 0;
        public long hiFuzzyMatchWordCount = 0;
        public long contextMatchWordCount = 0;
        public long segmentTmWordCount = 0;
        public long totalWordCount = 0;

        /* word count costs */
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

        /* Dell values */
        public BigDecimal dellInternalRepsWordCountCost = new BigDecimal(
                BIG_DECIMAL_ZERO_STRING);
        public BigDecimal dellExactMatchWordCountCost = new BigDecimal(
                BIG_DECIMAL_ZERO_STRING);
        public BigDecimal dellInContextMatchWordCountCost = new BigDecimal(
                BIG_DECIMAL_ZERO_STRING);
        public BigDecimal dellContextMatchWordCountCost = new BigDecimal(
                BIG_DECIMAL_ZERO_STRING);
        public BigDecimal dellFuzzyMatchWordCountCost = new BigDecimal(
                BIG_DECIMAL_ZERO_STRING);
        public BigDecimal dellNewWordsWordCountCost = new BigDecimal(
                BIG_DECIMAL_ZERO_STRING);
        public BigDecimal dellTotalWordCountCost = new BigDecimal(
                BIG_DECIMAL_ZERO_STRING);

        /* Trados values */
        public BigDecimal trados100WordCountCost = new BigDecimal(
                BIG_DECIMAL_ZERO_STRING);
        public BigDecimal tradosInContextWordCountCost = new BigDecimal(
                BIG_DECIMAL_ZERO_STRING);
        public BigDecimal tradosContextWordCountCost = new BigDecimal(
                BIG_DECIMAL_ZERO_STRING);
        public BigDecimal trados95to99WordCountCost = new BigDecimal(
                BIG_DECIMAL_ZERO_STRING);
        public BigDecimal trados85to94WordCountCost = new BigDecimal(
                BIG_DECIMAL_ZERO_STRING);
        public BigDecimal trados75to84WordCountCost = new BigDecimal(
                BIG_DECIMAL_ZERO_STRING);
        public BigDecimal trados50to74WordCountCost = new BigDecimal(
                BIG_DECIMAL_ZERO_STRING);
        public BigDecimal tradosRepsWordCountCost = new BigDecimal(
                BIG_DECIMAL_ZERO_STRING);
        public BigDecimal tradosTotalWordCountCost = new BigDecimal(
                BIG_DECIMAL_ZERO_STRING);
        public BigDecimal tradosNoMatchWordCountCost = new BigDecimal(
                BIG_DECIMAL_ZERO_STRING);

        public boolean wasExportFailed = false;

        public ProjectWorkflowData()
        {
        }
    }

    private void setJobNamesList(HttpServletRequest p_request, MyData p_data)
    {
        String[] jobIds = p_request.getParameterValues("jobId");
        for (int i = 0; i < jobIds.length; i++)
        {
            String id = jobIds[i];
            if ("*".equals(id))
            {
                p_data.wantsAllJobNames = true;
                break;
            }
            else
            {
                p_data.jobIdList.add(new Long(id));
            }

        }
    }

    private void setJobStatusList(HttpServletRequest p_request, MyData p_data)
    {
        String[] jobStatus = p_request.getParameterValues("status");
        for (int i = 0; i < jobStatus.length; i++)
        {
            String status = jobStatus[i];
            if ("*".equals(status))
            {
                p_data.wantsAllJobStatus = true;
                break;
            }
            else
            {
                p_data.jobStatusList.add(status);

            }
        }
    }

    private void setProjectIdList(HttpServletRequest p_request, MyData p_data)
    {
        // set the project Id
        String[] projectIds = p_request.getParameterValues("projectId");

        for (int i = 0; i < projectIds.length; i++)
        {
            String id = projectIds[i];
            if ("*".equals(id))
            {
                p_data.wantsAllProjects = true;
                break;
            }
            else
            {
                p_data.projectIdList.add(new Long(id));
            }
        }
    }

    private void setTargetLangList(HttpServletRequest p_request, MyData p_data)
    {
        // set the target lang
        String[] targetLangs = p_request.getParameterValues("targetLang");
        for (int i = 0; i < targetLangs.length; i++)
        {
            String id = targetLangs[i];
            if (id.equals("*") == false)
                p_data.targetLangList.add(id);
            else
            {
                p_data.wantsAllTargetLangs = true;
                break;
            }
        }
    }

    /**
     * Returns search params used to find the jobs based on state
     * (READY,EXPORTED,LOCALIZED,DISPATCHED,export failed, archived), and
     * creation date during the range
     * 
     * @return JobSearchParams
     */
    private JobSearchParameters getSearchParams(HttpServletRequest p_request,
            MyData p_data)
    {
        JobSearchParameters sp = new JobSearchParameters();
        if (!p_data.wantsAllProjects)
        {
            sp.setProjectId(p_data.projectIdList);
        }

        if (!p_data.wantsAllJobStatus)
        {
            sp.setJobState(p_data.jobStatusList);
        }
        else
        {
            // job state EXPORTED,DISPATCHED,LOCALIZED,EXPORTED FAILED
            ArrayList<String> list = new ArrayList<String>();
            list.add(Job.READY_TO_BE_DISPATCHED);
            list.add(Job.DISPATCHED);
            list.add(Job.LOCALIZED);
            list.add(Job.EXPORTED);
            list.add(Job.EXPORT_FAIL);
            list.add(Job.ARCHIVED);
            sp.setJobState(list);
        }

        String paramCreateDateStartCount = p_request
                .getParameter(JobSearchConstants.CREATION_START);
        String paramCreateDateStartOpts = p_request
                .getParameter(JobSearchConstants.CREATION_START_OPTIONS);
        if ("-1".equals(paramCreateDateStartOpts) == false)
        {
            sp.setCreationStart(new Integer(paramCreateDateStartCount));
            sp.setCreationStartCondition(paramCreateDateStartOpts);
        }

        String paramCreateDateEndCount = p_request
                .getParameter(JobSearchConstants.CREATION_END);
        String paramCreateDateEndOpts = p_request
                .getParameter(JobSearchConstants.CREATION_END_OPTIONS);
        if (SearchCriteriaParameters.NOW.equals(paramCreateDateEndOpts))
        {
            sp.setCreationEnd(new java.util.Date());
        }
        else if ("-1".equals(paramCreateDateEndOpts) == false)
        {
            sp.setCreationEnd(new Integer(paramCreateDateEndCount));
            sp.setCreationEndCondition(paramCreateDateEndOpts);
        }

        return sp;
    }

    /**
     * Adds the table header for the Dell Matches sheet
     * 
     */
    private void addHeaderForDellMatches(MyData p_data, ResourceBundle bundle)
            throws Exception
    {
        WritableSheet theSheet = p_data.dellSheet;
        // title font is black bold on white
        String EMEA = CompanyWrapper.getCurrentCompanyName();
        WritableFont titleFont = new WritableFont(WritableFont.ARIAL, 14,
                WritableFont.BOLD, false, UnderlineStyle.NO_UNDERLINE,
                jxl.format.Colour.BLACK);
        WritableCellFormat titleFormat = new WritableCellFormat(titleFont);
        titleFormat.setWrap(false);
        titleFormat.setShrinkToFit(false);

        theSheet.addCell(new Label(0, 0,
                EMEA + " " + bundle.getString("lb_po"), titleFormat));
        theSheet.setColumnView(0, 20);

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

        int c = 0;
        theSheet.addCell(new Label(c, 2, bundle.getString("lb_job_id"),
                headerFormat));
        theSheet.mergeCells(c, 2, c, 3);
        c++;
        theSheet.addCell(new Label(c, 2, bundle.getString("lb_job"),
                headerFormat));
        theSheet.mergeCells(c, 2, c, 3);
        c++;
        theSheet.addCell(new Label(c, 2, bundle
                .getString("lb_po_number_report"), headerFormat));
        theSheet.mergeCells(c, 2, c, 3);
        c++;
        theSheet.addCell(new Label(c, 2, bundle.getString("lb_description"),
                headerFormat));
        theSheet.mergeCells(c, 2, c, 3);
        theSheet.setColumnView(c, 15);
        c++;
        theSheet.addCell(new Label(c, 2, bundle.getString("lb_creation_date"),
                headerFormat));
        theSheet.mergeCells(c, 2, c, 3);
        c++;
        theSheet.addCell(new Label(c, 2, bundle.getString("lb_lang"),
                langFormat));
        theSheet.mergeCells(c, 2, c, 3);
        c++;
        theSheet.addCell(new Label(c, 2, bundle.getString("lb_word_counts"),
                wordCountFormat));
        if (p_data.headers[0] != null && p_data.headers[1] != null)
        {
            theSheet.mergeCells(c, 2, c + 6, 2);
        }
        else
        {
            if (p_data.headers[0] != null || p_data.headers[1] != null)
            {
                theSheet.mergeCells(c, 2, c + 5, 2);
            }
            else
            {
                theSheet.mergeCells(c, 2, c + 4, 2);
            }

        }
        theSheet.addCell(new Label(c++, 3, bundle
                .getString("jobinfo.tmmatches.wordcounts.internalreps"),
                wordCountValueFormat));
        theSheet.addCell(new Label(c++, 3, bundle
                .getString("jobinfo.tmmatches.wordcounts.exactmatches"),
                wordCountValueFormat));
        if (p_data.headers[0] != null)
        {
            theSheet.addCell(new Label(
                    c++,
                    3,
                    bundle.getString("jobinfo.tmmatches.wordcounts.incontextmatches"),
                    wordCountValueFormat));
        }
        if (p_data.headers[1] != null)
        {
            theSheet.addCell(new Label(c++, 3, bundle
                    .getString("lb_context_tm"), wordCountValueFormat));
        }
        theSheet.addCell(new Label(c++, 3, bundle
                .getString("jobinfo.tmmatches.wordcounts.fuzzymatches"),
                wordCountValueFormat));
        theSheet.addCell(new Label(c++, 3, bundle
                .getString("jobinfo.tmmatches.wordcounts.newwords"),
                wordCountValueFormat));
        theSheet.addCell(new Label(c++, 3, bundle.getString("lb_total"),
                wordCountValueRightFormat));

        theSheet.addCell(new Label(c, 2, bundle
                .getString("jobinfo.tmmatches.invoice"), wordCountFormat));
        if (p_data.headers[0] != null && p_data.headers[1] != null)
        {
            theSheet.mergeCells(c, 2, c + 6, 2);
        }
        else
        {
            if (p_data.headers[0] != null || p_data.headers[1] != null)
            {
                theSheet.mergeCells(c, 2, c + 5, 2);
            }
            else
            {
                theSheet.mergeCells(c, 2, c + 4, 2);
            }
        }
        theSheet.addCell(new Label(c++, 3, bundle
                .getString("jobinfo.tmmatches.invoice.internalreps"),
                wordCountValueFormat));
        theSheet.addCell(new Label(c++, 3, bundle
                .getString("jobinfo.tmmatches.invoice.exactmatches"),
                wordCountValueFormat));
        if (p_data.headers[0] != null)
        {
            theSheet.addCell(new Label(c++, 3, bundle
                    .getString("jobinfo.tmmatches.invoice.incontextmatches"),
                    wordCountValueFormat));
        }
        if (p_data.headers[1] != null)
        {
            theSheet.addCell(new Label(c++, 3, bundle
                    .getString("lb_context_tm"), wordCountValueFormat));
        }
        theSheet.addCell(new Label(c++, 3, bundle
                .getString("jobinfo.tmmatches.invoice.fuzzymatches"),
                wordCountValueFormat));
        theSheet.addCell(new Label(c++, 3, bundle
                .getString("jobinfo.tmmatches.invoice.newwords"),
                wordCountValueFormat));
        theSheet.addCell(new Label(c++, 3, bundle.getString("lb_total"),
                wordCountValueRightFormat));
        theSheet.mergeCells(c, 2, c, 3);
    }

    /**
     * Adds the table header for the Trados Matches sheet
     * 
     */
    private void addHeaderForTradosMatches(MyData p_data, ResourceBundle bundle)
            throws Exception
    {
        WritableSheet theSheet = p_data.tradosSheet;
        String EMEA = CompanyWrapper.getCurrentCompanyName();
        // title font is black bold on white
        WritableFont titleFont = new WritableFont(WritableFont.ARIAL, 14,
                WritableFont.BOLD, false, UnderlineStyle.NO_UNDERLINE,
                jxl.format.Colour.BLACK);
        WritableCellFormat titleFormat = new WritableCellFormat(titleFont);
        titleFormat.setWrap(false);
        titleFormat.setShrinkToFit(false);

        theSheet.addCell(new Label(0, 0,
                EMEA + " " + bundle.getString("lb_po"), titleFormat));
        theSheet.setColumnView(0, 20);

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

        int c = 0;
        theSheet.addCell(new Label(0, 1, bundle.getString("lb_desp_file_list")));
        theSheet.addCell(new Label(c, 2, bundle.getString("lb_job_id"),
                headerFormat));
        theSheet.mergeCells(c, 2, c, 3);
        c++;
        theSheet.addCell(new Label(c, 2, bundle.getString("lb_job"),
                headerFormat));
        theSheet.mergeCells(c, 2, c, 3);
        c++;
        theSheet.addCell(new Label(c, 2, bundle
                .getString("lb_po_number_report"), headerFormat));
        theSheet.mergeCells(c, 2, c, 3);
        c++;
        theSheet.addCell(new Label(c, 2, bundle.getString("reportDesc"),
                headerFormat));
        theSheet.mergeCells(c, 2, c, 3);
        theSheet.setColumnView(c, 15);
        c++;
        theSheet.addCell(new Label(c, 2, bundle.getString("lb_creation_date"),
                headerFormat));
        theSheet.mergeCells(c, 2, c, 3);
        c++;
        theSheet.addCell(new Label(c, 2, bundle.getString("lb_lang"),
                langFormat));
        theSheet.mergeCells(c, 2, c, 3);
        c++;
        theSheet.addCell(new Label(c, 2, bundle
                .getString("jobinfo.tmmatches.wordcounts"), wordCountFormat));
        if (p_data.headers[0] != null && p_data.headers[1] != null)
        {
            theSheet.mergeCells(c, 2, c + 8, 2);
        }
        else
        {
            if (p_data.headers[0] != null || p_data.headers[1] != null)
            {
                theSheet.mergeCells(c, 2, c + 7, 2);
            }
            else
            {
                theSheet.mergeCells(c, 2, c + 6, 2);
            }
        }
        theSheet.addCell(new Label(c++, 3, bundle
                .getString("jobinfo.tradosmatches.invoice.per100matches"),
                wordCountValueFormat));
        theSheet.addCell(new Label(c++, 3, bundle.getString("lb_95_99"),
                wordCountValueFormat));
        theSheet.addCell(new Label(c++, 3, bundle.getString("lb_85_94"),
                wordCountValueFormat));
        theSheet.addCell(new Label(c++, 3, bundle.getString("lb_75_84") + "*",
                wordCountValueFormat));
        theSheet.addCell(new Label(c++, 3, bundle.getString("lb_no_match"),
                wordCountValueRightFormat));
        theSheet.addCell(new Label(c++, 3, bundle
                .getString("lb_repetition_word_cnt"), wordCountValueRightFormat));
        if (p_data.headers[0] != null)
        {
            theSheet.addCell(new Label(c++, 3, bundle
                    .getString("lb_in_context_tm"), wordCountValueFormat));
        }
        if (p_data.headers[1] != null)
        {
            theSheet.addCell(new Label(c++, 3, bundle
                    .getString("lb_context_matches"), wordCountValueFormat));
        }
        theSheet.addCell(new Label(c++, 3, bundle.getString("lb_total"),
                wordCountValueRightFormat));

        theSheet.addCell(new Label(c, 2, bundle
                .getString("jobinfo.tmmatches.invoice"), wordCountFormat));
        if (p_data.headers[0] != null && p_data.headers[1] != null)
        {
            theSheet.mergeCells(c, 2, c + 8, 2);
        }
        else
        {
            if (p_data.headers[0] != null || p_data.headers[1] != null)
            {
                theSheet.mergeCells(c, 2, c + 7, 2);
            }
            else
            {
                theSheet.mergeCells(c, 2, c + 6, 2);
            }
        }
        theSheet.addCell(new Label(c++, 3, bundle
                .getString("jobinfo.tradosmatches.invoice.per100matches"),
                wordCountValueFormat));
        theSheet.addCell(new Label(c++, 3, bundle.getString("lb_95_99"),
                wordCountValueFormat));
        theSheet.addCell(new Label(c++, 3, bundle.getString("lb_85_94"),
                wordCountValueFormat));
        theSheet.addCell(new Label(c++, 3, bundle.getString("lb_75_84") + "*",
                wordCountValueFormat));
        theSheet.addCell(new Label(c++, 3, bundle.getString("lb_no_match"),
                wordCountValueRightFormat));
        theSheet.addCell(new Label(c++, 3, bundle
                .getString("lb_repetition_word_cnt"), wordCountValueRightFormat));
        if (p_data.headers[0] != null)
        {
            theSheet.addCell(new Label(c++, 3, bundle
                    .getString("lb_in_context_match"), wordCountValueFormat));
        }
        if (p_data.headers[1] != null)
        {
            theSheet.addCell(new Label(c++, 3, bundle
                    .getString("lb_context_match"), wordCountValueFormat));
        }
        theSheet.addCell(new Label(c++, 3, bundle.getString("lb_total"),
                wordCountValueRightFormat));
    }

    public void writeProjectDataForDellMatches(HashMap p_projectMap,
            IntHolder p_row, MyData p_data, ResourceBundle bundle)
            throws Exception
    {
        WritableSheet theSheet = p_data.dellSheet;
        ArrayList projects = new ArrayList(p_projectMap.keySet());
        SortUtil.sort(projects);
        Iterator projectIter = projects.iterator();

        WritableCellFormat dateFormat = new WritableCellFormat(
                DateFormats.DEFAULT);
        dateFormat.setWrap(false);
        dateFormat.setShrinkToFit(false);

        java.text.DecimalFormat dformat = (java.text.DecimalFormat) java.text.NumberFormat
                .getCurrencyInstance(Locale.US);
        String euroJavaNumberFormat = getNumberFormatString();
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

        WritableCellFormat moneyRightFormat = new WritableCellFormat(
                moneyFormat);
        moneyRightFormat.setBorder(jxl.format.Border.RIGHT,
                jxl.format.BorderLineStyle.THICK);

        jxl.format.CellFormat normalFormat = getNormalFormat();
        jxl.format.CellFormat failedNormalFormat = getRedColorFormat();

        WritableCellFormat failed_dateFormat = new WritableCellFormat(
                DateFormats.DEFAULT);
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
            String jobName = (String) projectIter.next();
            boolean isWrongJob = p_data.wrongJobNames.contains(jobName);
            HashMap localeMap = (HashMap) p_projectMap.get(jobName);
            ArrayList locales = new ArrayList(localeMap.keySet());
            SortUtil.sort(locales);
            Iterator localeIter = locales.iterator();
            BigDecimal projectTotalWordCountCost = new BigDecimal(
                    BIG_DECIMAL_ZERO_STRING);
            while (localeIter.hasNext())
            {
                int row = p_row.getValue();
                int col = 0;
                String localeName = (String) localeIter.next();
                ProjectWorkflowData data = (ProjectWorkflowData) localeMap
                        .get(localeName);
                WritableCellFormat temp_dateFormat = dateFormat;
                WritableCellFormat temp_moneyFormat = moneyFormat;
                jxl.format.CellFormat temp_normalFormat = normalFormat;
                // WritableCellFormat temp_wordCountValueRightFormat =
                // wordCountValueRightFormat;
                if (data.wasExportFailed)
                {
                    temp_dateFormat = failed_dateFormat;
                    temp_moneyFormat = failed_moneyFormat;
                    temp_normalFormat = failedNormalFormat;

                }
                if (isWrongJob)
                {
                    theSheet.addCell(new Number(col++, row, data.jobId,
                            wrongJobFormat));
                    theSheet.addCell(new Label(col++, row, data.jobName,
                            wrongJobFormat));
                }
                else
                {
                    theSheet.addCell(new Number(col++, row, data.jobId,
                            temp_normalFormat));
                    theSheet.addCell(new Label(col++, row, data.jobName,
                            temp_normalFormat));
                }
                theSheet.setColumnView(col - 2, 5);
                theSheet.setColumnView(col - 1, 50);
                theSheet.addCell(new Label(col++, row, data.poNumber,
                        temp_normalFormat)); // PO number
                theSheet.addCell(new Label(col++, row, data.projectDesc,
                        temp_normalFormat));
                theSheet.setColumnView(col - 1, 22);
                /* data.creationDate.toString())); */
                theSheet.addCell(new DateTime(col++, row, data.creationDate,
                        temp_dateFormat));
                theSheet.setColumnView(col - 1, 15);
                theSheet.addCell(new Label(col++, row, data.targetLang,
                        temp_normalFormat));
                theSheet.addCell(new Number(col++, row,
                        data.dellInternalRepsWordCount, temp_normalFormat));
                int numwidth = 10;
                theSheet.setColumnView(col - 1, numwidth);
                theSheet.addCell(new Number(col++, row,
                        data.dellExactMatchWordCount, temp_normalFormat));
                theSheet.setColumnView(col - 1, numwidth);

                if (p_data.headers[0] != null)
                {
                    theSheet.addCell(new Number(col++, row,
                            data.dellInContextMatchWordCount, temp_normalFormat));
                    theSheet.setColumnView(col - 1, numwidth);
                }
                if (p_data.headers[1] != null)
                {
                    theSheet.addCell(new Number(col++, row,
                            data.dellContextMatchWordCount, temp_normalFormat));
                    theSheet.setColumnView(col - 1, numwidth);
                }
                theSheet.addCell(new Number(col++, row,
                        data.dellFuzzyMatchWordCount, temp_normalFormat));
                theSheet.setColumnView(col - 1, numwidth);

                theSheet.addCell(new Number(col++, row,
                        data.dellNewWordsWordCount, temp_normalFormat));
                theSheet.setColumnView(col - 1, numwidth);
                theSheet.addCell(new Number(col++, row,
                        data.dellTotalWordCount, temp_normalFormat));
                theSheet.setColumnView(col - 1, numwidth);

                int moneywidth = 12;
                theSheet.addCell(new Number(col++, row,
                        asDouble(data.dellInternalRepsWordCountCost),
                        temp_moneyFormat));
                theSheet.setColumnView(col - 1, moneywidth);
                theSheet.addCell(new Number(col++, row,
                        asDouble(data.dellExactMatchWordCountCost),
                        temp_moneyFormat));
                theSheet.setColumnView(col - 1, moneywidth);

                if (p_data.headers[0] != null)
                {
                    theSheet.addCell(new Number(col++, row,
                            asDouble(data.dellInContextMatchWordCountCost),
                            temp_moneyFormat));
                    theSheet.setColumnView(col - 1, moneywidth);
                }
                if (p_data.headers[1] != null)
                {
                    theSheet.addCell(new Number(col++, row,
                            asDouble(data.dellContextMatchWordCountCost),
                            temp_moneyFormat));
                    theSheet.setColumnView(col - 1, moneywidth);
                }
                theSheet.addCell(new Number(col++, row,
                        asDouble(data.dellFuzzyMatchWordCountCost),
                        temp_moneyFormat));
                theSheet.setColumnView(col - 1, moneywidth);

                theSheet.addCell(new Number(col++, row,
                        asDouble(data.dellNewWordsWordCountCost),
                        temp_moneyFormat));
                theSheet.setColumnView(col - 1, moneywidth);
                theSheet.addCell(new Number(col++, row,
                        asDouble(data.dellTotalWordCountCost), temp_moneyFormat));
                theSheet.setColumnView(col - 1, moneywidth);
                // projectTotalWordCountCost =
                // projectTotalWordCountCost.add(data.totalWordCountCost);

                // //add a "project total" summary cost over the locales
                // / if (localeIter.hasNext() == false)
                // {
                // theSheet.addCell(new
                // Number(col++,row,asDouble(projectTotalWordCountCost),moneyFormat));
                // }
                // else
                // {
                // theSheet.addCell(new Blank(col++,row,moneyFormat));
                // }
                // theSheet.setColumnView(col -1,moneywidth);

                p_row.inc();
            }
        }

        p_row.inc();
        p_row.inc();
        addTotalsForDellMatches(p_data, p_row, bundle);

    }

    /** Adds the totals and sub-total formulas */
    private void addTotalsForDellMatches(MyData p_data, IntHolder p_row,
            ResourceBundle bundle) throws Exception
    {
        WritableSheet theSheet = p_data.dellSheet;
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

        java.text.DecimalFormat dformat = (java.text.DecimalFormat) java.text.NumberFormat
                .getCurrencyInstance(Locale.US);
        String euroJavaNumberFormat = getNumberFormatString();
        NumberFormat euroNumberFormat = new NumberFormat(euroJavaNumberFormat);
        WritableCellFormat moneyFormat = new WritableCellFormat(
                euroNumberFormat);
        moneyFormat.setWrap(false);
        moneyFormat.setShrinkToFit(false);
        moneyFormat.setBorder(jxl.format.Border.TOP,
                jxl.format.BorderLineStyle.THIN);
        moneyFormat.setBorder(jxl.format.Border.BOTTOM,
                jxl.format.BorderLineStyle.THIN);
        moneyFormat.setBackground(jxl.format.Colour.GRAY_25);

        theSheet.addCell(new Label(0, row, title, subTotalFormat));
        theSheet.mergeCells(0, row, 5, row);
        int lastRow = p_row.getValue() - 2;

        // add in word count totals
        int c = 6;

        if (p_data.headers[0] != null && p_data.headers[1] != null)
        {

            // word counts
            theSheet.addCell(new Formula(c++, row, "SUM(G5:G" + lastRow + ")",
                    subTotalFormat));
            theSheet.addCell(new Formula(c++, row, "SUM(H5:H" + lastRow + ")",
                    subTotalFormat));
            theSheet.addCell(new Formula(c++, row, "SUM(I5:I" + lastRow + ")",
                    subTotalFormat));
            theSheet.addCell(new Formula(c++, row, "SUM(J5" + ":J" + lastRow
                    + ")", subTotalFormat));
            theSheet.addCell(new Formula(c++, row, "SUM(K5" + ":K" + lastRow
                    + ")", subTotalFormat));
            theSheet.addCell(new Formula(c++, row, "SUM(L5" + ":L" + lastRow
                    + ")", subTotalFormat));
            theSheet.addCell(new Formula(c++, row, "SUM(M5" + ":M" + lastRow
                    + ")", subTotalFormat));
            // word count costs

            theSheet.addCell(new Formula(c++, row, "SUM(N5" + ":N" + lastRow
                    + ")", moneyFormat));
            theSheet.addCell(new Formula(c++, row, "SUM(O5" + ":O" + lastRow
                    + ")", moneyFormat));
            theSheet.addCell(new Formula(c++, row, "SUM(P5" + ":P" + lastRow
                    + ")", moneyFormat));
            theSheet.addCell(new Formula(c++, row, "SUM(Q5" + ":Q" + lastRow
                    + ")", moneyFormat));
            theSheet.addCell(new Formula(c++, row, "SUM(R5" + ":R" + lastRow
                    + ")", moneyFormat));
            theSheet.addCell(new Formula(c++, row, "SUM(S5" + ":S" + lastRow
                    + ")", moneyFormat));
            theSheet.addCell(new Formula(c++, row, "SUM(T5" + ":T" + lastRow
                    + ")", moneyFormat));
        }
        else
        {
            if (p_data.headers[0] != null || p_data.headers[1] != null)
            {
                // word counts
                theSheet.addCell(new Formula(c++, row, "SUM(G5:G" + lastRow
                        + ")", subTotalFormat));
                theSheet.addCell(new Formula(c++, row, "SUM(H5:H" + lastRow
                        + ")", subTotalFormat));
                theSheet.addCell(new Formula(c++, row, "SUM(I5:I" + lastRow
                        + ")", subTotalFormat));
                theSheet.addCell(new Formula(c++, row, "SUM(J5" + ":J"
                        + lastRow + ")", subTotalFormat));
                theSheet.addCell(new Formula(c++, row, "SUM(K5" + ":K"
                        + lastRow + ")", subTotalFormat));
                theSheet.addCell(new Formula(c++, row, "SUM(L5" + ":L"
                        + lastRow + ")", subTotalFormat));
                // word count costs
                theSheet.addCell(new Formula(c++, row, "SUM(M5" + ":M"
                        + lastRow + ")", moneyFormat));
                theSheet.addCell(new Formula(c++, row, "SUM(N5" + ":N"
                        + lastRow + ")", moneyFormat));
                theSheet.addCell(new Formula(c++, row, "SUM(O5" + ":O"
                        + lastRow + ")", moneyFormat));
                theSheet.addCell(new Formula(c++, row, "SUM(P5" + ":P"
                        + lastRow + ")", moneyFormat));
                theSheet.addCell(new Formula(c++, row, "SUM(Q5" + ":Q"
                        + lastRow + ")", moneyFormat));
                theSheet.addCell(new Formula(c++, row, "SUM(R5" + ":R"
                        + lastRow + ")", moneyFormat));
            }
            else
            {
                // word counts
                theSheet.addCell(new Formula(c++, row, "SUM(G5:G" + lastRow
                        + ")", subTotalFormat));
                theSheet.addCell(new Formula(c++, row, "SUM(H5:H" + lastRow
                        + ")", subTotalFormat));
                theSheet.addCell(new Formula(c++, row, "SUM(I5:I" + lastRow
                        + ")", subTotalFormat));
                theSheet.addCell(new Formula(c++, row, "SUM(J5" + ":J"
                        + lastRow + ")", subTotalFormat));
                theSheet.addCell(new Formula(c++, row, "SUM(K5" + ":K"
                        + lastRow + ")", subTotalFormat));
                // word count costs
                theSheet.addCell(new Formula(c++, row, "SUM(L5" + ":L"
                        + lastRow + ")", moneyFormat));
                theSheet.addCell(new Formula(c++, row, "SUM(M5" + ":M"
                        + lastRow + ")", moneyFormat));
                theSheet.addCell(new Formula(c++, row, "SUM(N5" + ":N"
                        + lastRow + ")", moneyFormat));
                theSheet.addCell(new Formula(c++, row, "SUM(O5" + ":O"
                        + lastRow + ")", moneyFormat));
                theSheet.addCell(new Formula(c++, row, "SUM(P5" + ":P"
                        + lastRow + ")", moneyFormat));
            }
        }
    }

    public void writeProjectDataForTradosMatches(HashMap p_projectMap,
            IntHolder p_row, MyData p_data, ResourceBundle bundle)
            throws Exception
    {
        WritableSheet theSheet = p_data.tradosSheet;
        ArrayList projects = new ArrayList(p_projectMap.keySet());
        SortUtil.sort(projects);
        Iterator projectIter = projects.iterator();

        WritableCellFormat dateFormat = new WritableCellFormat(
                DateFormats.DEFAULT);
        dateFormat.setWrap(false);
        dateFormat.setShrinkToFit(false);

        java.text.DecimalFormat dformat = (java.text.DecimalFormat) java.text.NumberFormat
                .getCurrencyInstance(Locale.US);
        String euroJavaNumberFormat = getNumberFormatString();
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

        WritableCellFormat moneyRightFormat = new WritableCellFormat(
                moneyFormat);
        moneyRightFormat.setBorder(jxl.format.Border.RIGHT,
                jxl.format.BorderLineStyle.THICK);

        jxl.format.CellFormat normalFormat = getNormalFormat();
        jxl.format.CellFormat failedNormalFormat = getRedColorFormat();

        WritableCellFormat failed_dateFormat = new WritableCellFormat(
                DateFormats.DEFAULT);
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
            String jobName = (String) projectIter.next();
            boolean isWrongJob = p_data.wrongJobNames.contains(jobName);
            HashMap localeMap = (HashMap) p_projectMap.get(jobName);
            ArrayList locales = new ArrayList(localeMap.keySet());
            SortUtil.sort(locales);
            Iterator localeIter = locales.iterator();
            BigDecimal projectTotalWordCountCost = new BigDecimal(
                    BIG_DECIMAL_ZERO_STRING);
            while (localeIter.hasNext())
            {
                int row = p_row.getValue();
                int col = 0;
                String localeName = (String) localeIter.next();
                ProjectWorkflowData data = (ProjectWorkflowData) localeMap
                        .get(localeName);

                WritableCellFormat temp_dateFormat = dateFormat;
                WritableCellFormat temp_moneyFormat = moneyFormat;
                jxl.format.CellFormat temp_normalFormat = normalFormat;
                // WritableCellFormat temp_wordCountValueRightFormat =
                // wordCountValueRightFormat;
                if (data.wasExportFailed)
                {
                    temp_dateFormat = failed_dateFormat;
                    temp_moneyFormat = failed_moneyFormat;
                    temp_normalFormat = failedNormalFormat;
                }

                if (isWrongJob)
                {
                    theSheet.addCell(new Number(col++, row, data.jobId,
                            wrongJobFormat));
                    theSheet.addCell(new Label(col++, row, data.jobName,
                            wrongJobFormat));
                }
                else
                {
                    theSheet.addCell(new Number(col++, row, data.jobId,
                            temp_normalFormat));
                    theSheet.addCell(new Label(col++, row, data.jobName,
                            temp_normalFormat));
                }
                theSheet.setColumnView(col - 2, 5);
                theSheet.setColumnView(col - 1, 50);
                theSheet.addCell(new Label(col++, row, data.poNumber,
                        temp_normalFormat)); // PO number
                theSheet.addCell(new Label(col++, row, data.projectDesc,
                        temp_normalFormat));
                theSheet.setColumnView(col - 1, 22);
                /* data.creationDate.toString())); */
                theSheet.addCell(new DateTime(col++, row, data.creationDate,
                        temp_dateFormat));
                theSheet.setColumnView(col - 1, 15);
                theSheet.addCell(new Label(col++, row, data.targetLang,
                        temp_normalFormat));

                theSheet.addCell(new Number(col++, row,
                        data.trados100WordCount, temp_normalFormat));
                int numwidth = 10;
                theSheet.setColumnView(col - 1, numwidth);

                theSheet.addCell(new Number(col++, row,
                        data.trados95to99WordCount, temp_normalFormat));
                theSheet.setColumnView(col - 1, numwidth);

                theSheet.addCell(new Number(col++, row,
                        data.trados85to94WordCount, temp_normalFormat));
                theSheet.setColumnView(col - 1, numwidth);

                theSheet.addCell(new Number(col++, row,
                        data.trados75to84WordCount, temp_normalFormat));
                theSheet.setColumnView(col - 1, numwidth);
                theSheet.addCell(new Number(col++, row,
                        data.tradosNoMatchWordCount
                                + data.trados50to74WordCount, temp_normalFormat));
                theSheet.setColumnView(col - 1, numwidth);
                theSheet.addCell(new Number(col++, row,
                        data.tradosRepsWordCount, temp_normalFormat));
                theSheet.setColumnView(col - 1, numwidth);
                if (p_data.headers[0] != null)
                {
                    theSheet.addCell(new Number(col++, row,
                            data.tradosInContextMatchWordCount,
                            temp_normalFormat));
                    theSheet.setColumnView(col - 1, numwidth);
                }
                if (p_data.headers[1] != null)
                {
                    theSheet.addCell(new Number(col++, row,
                            data.tradosContextMatchWordCount, temp_normalFormat));
                    theSheet.setColumnView(col - 1, numwidth);
                }
                theSheet.addCell(new Number(col++, row,
                        data.tradosTotalWordCount, temp_normalFormat));
                theSheet.setColumnView(col - 1, numwidth);

                int moneywidth = 12;
                theSheet.addCell(new Number(col++, row,
                        asDouble(data.trados100WordCountCost), temp_moneyFormat));
                theSheet.setColumnView(col - 1, moneywidth);

                theSheet.addCell(new Number(col++, row,
                        asDouble(data.trados95to99WordCountCost),
                        temp_moneyFormat));
                theSheet.setColumnView(col - 1, moneywidth);

                theSheet.addCell(new Number(col++, row,
                        asDouble(data.trados85to94WordCountCost),
                        temp_moneyFormat));
                theSheet.setColumnView(col - 1, moneywidth);

                theSheet.addCell(new Number(col++, row,
                        asDouble(data.trados75to84WordCountCost),
                        temp_moneyFormat));
                theSheet.setColumnView(col - 1, moneywidth);
                theSheet.addCell(new Number(col++, row,
                        asDouble(data.tradosNoMatchWordCountCost),
                        temp_moneyFormat));
                theSheet.setColumnView(col - 1, moneywidth);

                theSheet.addCell(new Number(col++, row,
                        asDouble(data.tradosRepsWordCountCost),
                        temp_moneyFormat));
                theSheet.setColumnView(col - 1, moneywidth);

                if (p_data.headers[0] != null)
                {
                    theSheet.addCell(new Number(col++, row,
                            asDouble(data.tradosInContextWordCountCost),
                            temp_moneyFormat));
                    theSheet.setColumnView(col - 1, moneywidth);
                }
                if (p_data.headers[1] != null)
                {
                    theSheet.addCell(new Number(col++, row,
                            asDouble(data.tradosContextWordCountCost),
                            temp_moneyFormat));
                    theSheet.setColumnView(col - 1, moneywidth);
                }

                theSheet.addCell(new Number(col++, row,
                        asDouble(data.tradosTotalWordCountCost),
                        temp_moneyFormat));
                theSheet.setColumnView(col - 1, moneywidth);

                p_row.inc();
            }
        }

        p_row.inc();
        p_row.inc();
        addTotalsForTradosMatches(p_data, p_row, bundle);
    }

    /** Adds the totals and sub-total formulas */
    private void addTotalsForTradosMatches(MyData p_data, IntHolder p_row,
            ResourceBundle bundle) throws Exception
    {
        WritableSheet theSheet = p_data.tradosSheet;
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

        java.text.DecimalFormat dformat = (java.text.DecimalFormat) java.text.NumberFormat
                .getCurrencyInstance(Locale.US);
        String euroJavaNumberFormat = getNumberFormatString();
        NumberFormat euroNumberFormat = new NumberFormat(euroJavaNumberFormat);
        WritableCellFormat moneyFormat = new WritableCellFormat(
                euroNumberFormat);
        moneyFormat.setWrap(false);
        moneyFormat.setShrinkToFit(false);
        moneyFormat.setBorder(jxl.format.Border.TOP,
                jxl.format.BorderLineStyle.THIN);
        moneyFormat.setBorder(jxl.format.Border.BOTTOM,
                jxl.format.BorderLineStyle.THIN);
        moneyFormat.setBackground(jxl.format.Colour.GRAY_25);

        theSheet.addCell(new Label(0, row, title, subTotalFormat));
        theSheet.mergeCells(0, row, 5, row);
        int lastRow = p_row.getValue() - 2;

        // add in word count totals
        int c = 6;

        if (p_data.headers[0] != null && p_data.headers[1] != null)
        {
            // word counts
            theSheet.addCell(new Formula(c++, row, "SUM(G5:G" + lastRow + ")",
                    subTotalFormat));
            theSheet.addCell(new Formula(c++, row, "SUM(H5:H" + lastRow + ")",
                    subTotalFormat));
            theSheet.addCell(new Formula(c++, row, "SUM(I5:I" + lastRow + ")",
                    subTotalFormat));
            theSheet.addCell(new Formula(c++, row, "SUM(J5:J" + lastRow + ")",
                    subTotalFormat));
            theSheet.addCell(new Formula(c++, row, "SUM(K5:K" + lastRow + ")",
                    subTotalFormat));
            theSheet.addCell(new Formula(c++, row, "SUM(L5:L" + lastRow + ")",
                    subTotalFormat));
            theSheet.addCell(new Formula(c++, row, "SUM(M5:M" + lastRow + ")",
                    subTotalFormat));
            theSheet.addCell(new Formula(c++, row, "SUM(N5" + ":N" + lastRow
                    + ")", subTotalFormat));
            theSheet.addCell(new Formula(c++, row, "SUM(O5" + ":O" + lastRow
                    + ")", subTotalFormat));

            // word count costs

            theSheet.addCell(new Formula(c++, row, "SUM(P5" + ":P" + lastRow
                    + ")", moneyFormat));
            theSheet.addCell(new Formula(c++, row, "SUM(Q5" + ":Q" + lastRow
                    + ")", moneyFormat));
            theSheet.addCell(new Formula(c++, row, "SUM(R5" + ":R" + lastRow
                    + ")", moneyFormat));
            theSheet.addCell(new Formula(c++, row, "SUM(S5" + ":S" + lastRow
                    + ")", moneyFormat));
            theSheet.addCell(new Formula(c++, row, "SUM(T5" + ":T" + lastRow
                    + ")", moneyFormat));
            theSheet.addCell(new Formula(c++, row, "SUM(U5" + ":U" + lastRow
                    + ")", moneyFormat));
            theSheet.addCell(new Formula(c++, row, "SUM(V5" + ":V" + lastRow
                    + ")", moneyFormat));
            theSheet.addCell(new Formula(c++, row, "SUM(W5" + ":W" + lastRow
                    + ")", moneyFormat));
            theSheet.addCell(new Formula(c++, row, "SUM(X5" + ":X" + lastRow
                    + ")", moneyFormat));
        }
        else
        {
            if (p_data.headers[0] != null || p_data.headers[1] != null)
            {
                // word counts
                theSheet.addCell(new Formula(c++, row, "SUM(G5:G" + lastRow
                        + ")", subTotalFormat));
                theSheet.addCell(new Formula(c++, row, "SUM(H5:H" + lastRow
                        + ")", subTotalFormat));
                theSheet.addCell(new Formula(c++, row, "SUM(I5:I" + lastRow
                        + ")", subTotalFormat));
                theSheet.addCell(new Formula(c++, row, "SUM(J5:J" + lastRow
                        + ")", subTotalFormat));
                theSheet.addCell(new Formula(c++, row, "SUM(K5:K" + lastRow
                        + ")", subTotalFormat));
                theSheet.addCell(new Formula(c++, row, "SUM(L5:L" + lastRow
                        + ")", subTotalFormat));
                theSheet.addCell(new Formula(c++, row, "SUM(M5:M" + lastRow
                        + ")", subTotalFormat));
                theSheet.addCell(new Formula(c++, row, "SUM(N5" + ":N"
                        + lastRow + ")", subTotalFormat));

                // word count costs
                theSheet.addCell(new Formula(c++, row, "SUM(O5" + ":O"
                        + lastRow + ")", moneyFormat));
                theSheet.addCell(new Formula(c++, row, "SUM(P5" + ":P"
                        + lastRow + ")", moneyFormat));
                theSheet.addCell(new Formula(c++, row, "SUM(Q5" + ":Q"
                        + lastRow + ")", moneyFormat));
                theSheet.addCell(new Formula(c++, row, "SUM(R5" + ":R"
                        + lastRow + ")", moneyFormat));
                theSheet.addCell(new Formula(c++, row, "SUM(S5" + ":S"
                        + lastRow + ")", moneyFormat));
                theSheet.addCell(new Formula(c++, row, "SUM(T5" + ":T"
                        + lastRow + ")", moneyFormat));
                theSheet.addCell(new Formula(c++, row, "SUM(U5" + ":U"
                        + lastRow + ")", moneyFormat));
                theSheet.addCell(new Formula(c++, row, "SUM(V5" + ":V"
                        + lastRow + ")", moneyFormat));
            }
            else
            {
                // word counts
                theSheet.addCell(new Formula(c++, row, "SUM(G5:G" + lastRow
                        + ")", subTotalFormat));
                theSheet.addCell(new Formula(c++, row, "SUM(H5:H" + lastRow
                        + ")", subTotalFormat));
                theSheet.addCell(new Formula(c++, row, "SUM(I5:I" + lastRow
                        + ")", subTotalFormat));
                theSheet.addCell(new Formula(c++, row, "SUM(J5:J" + lastRow
                        + ")", subTotalFormat));
                theSheet.addCell(new Formula(c++, row, "SUM(K5:K" + lastRow
                        + ")", subTotalFormat));
                theSheet.addCell(new Formula(c++, row, "SUM(L5:L" + lastRow
                        + ")", subTotalFormat));
                theSheet.addCell(new Formula(c++, row, "SUM(M5:M" + lastRow
                        + ")", subTotalFormat));

                // word count costs
                theSheet.addCell(new Formula(c++, row, "SUM(N5" + ":N"
                        + lastRow + ")", moneyFormat));
                theSheet.addCell(new Formula(c++, row, "SUM(O5" + ":O"
                        + lastRow + ")", moneyFormat));
                theSheet.addCell(new Formula(c++, row, "SUM(P5" + ":P"
                        + lastRow + ")", moneyFormat));
                theSheet.addCell(new Formula(c++, row, "SUM(Q5" + ":Q"
                        + lastRow + ")", moneyFormat));
                theSheet.addCell(new Formula(c++, row, "SUM(R5" + ":R"
                        + lastRow + ")", moneyFormat));
                theSheet.addCell(new Formula(c++, row, "SUM(S5" + ":S"
                        + lastRow + ")", moneyFormat));
                theSheet.addCell(new Formula(c++, row, "SUM(T5" + ":T"
                        + lastRow + ")", moneyFormat));

            }
        }
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
                Job j = iter.next();
                p_data.wrongJobNames.add(j.getJobName());
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
    private ArrayList readJobNames(MyData p_data) throws Exception
    {
        ArrayList wrongJobs = new ArrayList();
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
                s_logger.warn("jobsInWrongDivision.txt file not found.");
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
                ArrayList jobs = new ArrayList(ServerProxy.getJobHandler()
                        .getJobs(sp));
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
                s_logger.warn("Ignoring mapping line for "
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

    // returns jobs in the specified criteria date range
    private ArrayList<Job> getWrongJobs(MyData p_data)
    {
        ArrayList<Job> wrongJobs = new ArrayList<Job>();
        Iterator<Job> iter = p_data.wrongJobs.iterator();
        Calendar cal = Calendar.getInstance();
        while (iter.hasNext())
        {
            Job j = iter.next();
            cal.setTime(j.getCreateDate());

            // //check the job's time against the search criteria
            wrongJobs.add(j);
        }
        return wrongJobs;
    }

    private String getNumberFormatString()
    {
        return symbol + "###,###,##0.000;(" + symbol + "###,###,##0.000)";
    }
    
    public String getReportType()
    {
        return ReportConstants.VENDOR_PO_REPORT;
    }
}
