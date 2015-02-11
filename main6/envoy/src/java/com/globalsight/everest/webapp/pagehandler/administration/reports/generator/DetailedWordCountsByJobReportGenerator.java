package com.globalsight.everest.webapp.pagehandler.administration.reports.generator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.format.UnderlineStyle;
import jxl.write.Formula;
import jxl.write.Label;
import jxl.write.Number;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

import com.csvreader.CsvWriter;
import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.foundation.SearchCriteriaParameters;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.jobhandler.JobSearchParameters;
import com.globalsight.everest.localemgr.LocaleManagerException;
import com.globalsight.everest.page.TargetPage;
import com.globalsight.everest.projecthandler.Project;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.util.comparator.JobComparator;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.pagehandler.administration.reports.ReportConstants;
import com.globalsight.everest.webapp.pagehandler.administration.reports.ReportHelper;
import com.globalsight.everest.webapp.pagehandler.administration.reports.bo.ReportsData;
import com.globalsight.everest.webapp.pagehandler.administration.users.UserUtil;
import com.globalsight.everest.webapp.pagehandler.projects.workflows.JobSearchConstants;
import com.globalsight.everest.workflowmanager.Workflow;
import com.globalsight.util.GeneralException;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.IntHolder;

/**
 * Used for generate Detailed Word Counts by Job.
 */
public class DetailedWordCountsByJobReportGenerator implements ReportGenerator
{
    private WritableWorkbook m_workbook = null;
    private CsvWriter csvWriter = null;
    private SimpleDateFormat dateFormat = new SimpleDateFormat(
            "MM/dd/yy hh:mm:ss a z");
    private boolean useInContext = false;
    private boolean useDefaultContext = false;
    private int wordCountCol = 0;
    protected ResourceBundle m_bundle = null;
    protected HttpServletRequest m_request = null;
    protected String m_companyName = "";
    private RequestData data = new RequestData();
    String m_userId;

    private class RequestData
    {
        // Report on job Ids
        boolean reportOnJobIds = true;

        boolean wantsAllProjects = false;

        boolean wantsAllLocales = false;

        // Need to search job by projects, status, date
        boolean needSearchJob = false;

        boolean exportWithXls = true;

        ArrayList<GlobalSightLocale> trgLocaleList = new ArrayList<GlobalSightLocale>();

        List<Long> projectIdList = new ArrayList<Long>();

        List<String> jobStateList = new ArrayList<String>();

        List<String> workflowStateList = new ArrayList<String>();

        List<Long> jobIds = new ArrayList<Long>();

    };

    public DetailedWordCountsByJobReportGenerator(HttpServletRequest p_request,
            HttpServletResponse p_response) throws LocaleManagerException,
            RemoteException, GeneralException
    {
        HttpSession session = p_request.getSession();
        m_userId = (String) session.getAttribute(WebAppConstants.USER_NAME);
        m_bundle = PageHandler.getBundle(p_request.getSession());
        m_request = p_request;
        m_companyName = UserUtil.getCurrentCompanyName(p_request);
        CompanyThreadLocal.getInstance().setValue(m_companyName);

        setProjectIdList(p_request);
        setTargetLocales(p_request);
        setStates(p_request);
        setExportFormat(p_request);
        String dateFormatString = p_request.getParameter("dateFormat");
        if (dateFormatString != null && !dateFormatString.equals(""))
        {
            dateFormat = new SimpleDateFormat(dateFormatString);
        }
    }

    public void sendReports(List<Long> p_jobIDS,
            List<GlobalSightLocale> p_targetLocales,
            HttpServletResponse p_response) throws Exception
    {
        if (p_jobIDS == null || p_jobIDS.size() == 0)
        {
            p_jobIDS = data.jobIds;
            p_targetLocales = data.trgLocaleList;
        }
        else if (data.jobIds == null || data.jobIds.size() == 0)
        {
            data.jobIds = p_jobIDS;
        }

        File[] reports = generateReports(p_jobIDS, p_targetLocales);
        ReportHelper.sendFiles(reports, getReportType(), p_response);
    }

    @Override
    public File[] generateReports(List<Long> p_jobIDS,
            List<GlobalSightLocale> p_targetLocales) throws Exception
    {
        if (p_jobIDS == null || p_jobIDS.size() == 0)
        {
            p_jobIDS = data.jobIds;
            p_targetLocales = data.trgLocaleList;
        }
        else if (data.jobIds == null || data.jobIds.size() == 0)
        {
            data.jobIds = p_jobIDS;
        }

        ArrayList<Job> jobs = ReportHelper.getJobListByIDS(p_jobIDS);
        File file;
        if (data.exportWithXls)
        {
            WorkbookSettings settings = new WorkbookSettings();
            settings.setSuppressWarnings(true);
            file = ReportHelper.getXLSReportFile(getReportType(), null);
            m_workbook = Workbook.createWorkbook(file, settings);
            addJobs(jobs, p_targetLocales);
            addCriteriaSheet(m_request);
            m_workbook.write();
            m_workbook.close();
        }
        else
        {
            file = ReportHelper.getReportFile(getReportType(), null, ".csv");
            OutputStream out = new FileOutputStream(file);
            csvWriter = new CsvWriter(out, ',', Charset.forName("UTF-8"));
            addJobsForCsv(jobs, p_targetLocales);
            csvWriter.close();
        }

        return new File[]{ file };
    }

    @Override
    public String getReportType()
    {
        return ReportConstants.DETAILED_WORDCOUNTS_REPORT;
    }

    private void setProjectIdList(HttpServletRequest p_request)
    {
        data.projectIdList.clear();
        data.wantsAllProjects = false;
        // set the project Id
        String[] projectIds = p_request.getParameterValues("projectNameList");
        if (projectIds != null)
        {
            for (int i = 0; i < projectIds.length; i++)
            {
                String id = projectIds[i];
                if (id.equals("*"))
                {
                    data.wantsAllProjects = true;
                    break;
                }
                else
                {
                    data.projectIdList.add(new Long(id));
                }
            }
        }
    }

    private void setTargetLocales(HttpServletRequest p_request)
            throws LocaleManagerException, RemoteException, GeneralException
    {
        data.trgLocaleList.clear();
        data.wantsAllLocales = false;
        String[] paramTrgLocales = p_request
                .getParameterValues(ReportConstants.TARGETLOCALE_LIST);
        if (paramTrgLocales != null)
        {
            for (int i = 0; i < paramTrgLocales.length; i++)
            {
                if ("*".equals(paramTrgLocales[i]))
                {
                    data.wantsAllLocales = true;
                    data.trgLocaleList = new ArrayList<GlobalSightLocale>(
                            ServerProxy.getLocaleManager()
                                    .getAllTargetLocales());
                    break;
                }
                else
                {
                    long localeId = Long.parseLong(paramTrgLocales[i]);
                    GlobalSightLocale locale = null;
                    try
                    {
                        locale = ServerProxy.getLocaleManager().getLocaleById(
                                localeId);
                    }
                    catch (Exception e)
                    {
                    }

                    if (locale != null && !data.trgLocaleList.contains(locale))
                    {
                        data.trgLocaleList.add(locale);
                    }
                }
            }
        }
    }

    /**
     * Set job and workflow status
     * 
     * @param p_request
     */
    private void setStates(HttpServletRequest p_request)
    {
        String reportOn = (String) p_request.getParameter("reportOn");
        data.reportOnJobIds = "jobIds".equals(reportOn);

        if (data.reportOnJobIds)
        {
            // Report on Job IDs
            data.jobIds.clear();
            String jobIdsStr = p_request.getParameter("jobIds");
            if (jobIdsStr != null && jobIdsStr.length() != 0)
            {
                String[] jobIds = jobIdsStr.split(",");
                long jobId = 0;
                for (int i = 0; i < jobIds.length; i++)
                {
                    try
                    {
                        jobId = Long.valueOf(jobIds[i].trim());
                    }
                    catch (Exception e)
                    {
                        continue;
                    }
                    if (!data.jobIds.contains(jobId))
                    {
                        data.jobIds.add(jobId);
                    }
                }
            }
            data.needSearchJob = false;
        }
        else
        {
            String[] jobIds = p_request.getParameterValues("jobNameList");
            data.needSearchJob = (jobIds == null);
            if (!data.needSearchJob)
            {
                data.jobIds.clear();
                long jobId = 0;
                for (int i = 0; i < jobIds.length; i++)
                {
                    jobId = Long.valueOf(jobIds[i]);
                    if (!data.jobIds.contains(jobId))
                    {
                        data.jobIds.add(jobId);
                    }
                }
            }
            else
            {
                data.jobStateList.clear();
                data.workflowStateList.clear();
                String[] states = p_request.getParameterValues("jobStatus");
                List lst = new ArrayList();
                if (states != null)
                {
                    lst = Arrays.asList(states);
                }
                if (lst.contains("*"))
                {
                    data.jobStateList.add(Job.READY_TO_BE_DISPATCHED);
                    data.workflowStateList.add(Workflow.READY_TO_BE_DISPATCHED);
                    data.jobStateList.add(Job.PENDING);
                    data.workflowStateList.add(Workflow.PENDING);
                    data.jobStateList.add(Job.DISPATCHED);
                    data.workflowStateList.add(Workflow.DISPATCHED);
                    data.jobStateList.add(Job.LOCALIZED);
                    data.workflowStateList.add(Workflow.LOCALIZED);
                    data.jobStateList.add(Job.EXPORTED);
                    data.jobStateList.add(Job.EXPORT_FAIL);
                    data.workflowStateList.add(Workflow.EXPORTED);
                    data.workflowStateList.add(Workflow.EXPORT_FAILED);
                    data.jobStateList.add(Job.ARCHIVED);
                    data.workflowStateList.add(Workflow.ARCHIVED);
                }
                else
                {
                    if (lst.contains(Job.READY_TO_BE_DISPATCHED))
                    {
                        data.jobStateList.add(Job.READY_TO_BE_DISPATCHED);
                        data.workflowStateList
                                .add(Workflow.READY_TO_BE_DISPATCHED);
                    }
                    if (lst.contains(Job.PENDING))
                    {
                        data.jobStateList.add(Job.PENDING);
                        data.workflowStateList.add(Workflow.PENDING);
                    }
                    if (lst.contains(Job.DISPATCHED))
                    {
                        data.jobStateList.add(Job.DISPATCHED);
                        data.workflowStateList.add(Workflow.DISPATCHED);
                    }
                    if (lst.contains(Job.LOCALIZED))
                    {
                        data.jobStateList.add(Job.LOCALIZED);
                        data.workflowStateList.add(Workflow.LOCALIZED);
                    }
                    if (lst.contains(Job.EXPORTED))
                    {
                        data.jobStateList.add(Job.EXPORTED);
                        data.workflowStateList.add(Workflow.EXPORTED);
                    }
                    if (lst.contains(Job.EXPORT_FAIL))
                    {
                        data.jobStateList.add(Job.EXPORT_FAIL);
                        data.workflowStateList.add(Workflow.EXPORT_FAILED);
                    }
                    if (lst.contains(Job.ARCHIVED))
                    {
                        data.jobStateList.add(Job.ARCHIVED);
                        data.workflowStateList.add(Workflow.ARCHIVED);
                    }
                }
            }
        }
    }

    /**
     * Set Export Format
     * 
     * @param p_request
     */
    public void setExportFormat(HttpServletRequest p_request)
    {
        String exportFormat = p_request.getParameter("exportFormat");
        if (exportFormat != null)
            exportFormat = exportFormat.toLowerCase();
        data.exportWithXls = "xls".equals(exportFormat);
    }

    /**
     * Add criteria sheet
     * 
     * @param p_request
     * @throws Exception
     */
    private void addCriteriaSheet(HttpServletRequest p_request)
            throws Exception
    {
        WritableSheet paramsSheet = m_workbook.createSheet(
                m_bundle.getString("lb_criteria"), 1);
        paramsSheet.addCell(new Label(0, 0, m_bundle
                .getString("lb_report_criteria")));
        paramsSheet.setColumnView(0, 50);

        if (data.wantsAllProjects)
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
            Iterator<Long> iter = data.projectIdList.iterator();
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
                paramsSheet.addCell(new Label(0, r, projectName));
                paramsSheet.addCell(new Label(1, r, m_bundle.getString("lb_id")
                        + "=" + pid.toString()));
                r++;
            }
        }

        String from = "";
        String to = "";
        String startCount = p_request
                .getParameter(JobSearchConstants.CREATION_START);
        String startOpts = p_request
                .getParameter(JobSearchConstants.CREATION_START_OPTIONS);
        if (startOpts != null && !"-1".equals(startOpts))
        {
            from = startCount + " " + translateOpts(startOpts, m_bundle);
        }

        String endCount = p_request
                .getParameter(JobSearchConstants.CREATION_END);
        String endOpts = p_request
                .getParameter(JobSearchConstants.CREATION_END_OPTIONS);

        if (startOpts != null && !"-1".equals(endOpts))
        {
            to = endCount + " " + translateOpts(endOpts, m_bundle);
        }

        paramsSheet
                .addCell(new Label(2, 1, m_bundle.getString("lb_from") + ":"));
        paramsSheet.addCell(new Label(2, 2, from));
        paramsSheet.addCell(new Label(3, 1, m_bundle.getString("lb_until")
                + ":"));
        paramsSheet.addCell(new Label(3, 2, to));

        if (data.wantsAllLocales)
        {
            paramsSheet.addCell(new Label(4, 1, m_bundle
                    .getString("lb_selected_langs")
                    + " "
                    + m_bundle.getString("all")));
        }
        else
        {
            paramsSheet.addCell(new Label(4, 1, m_bundle
                    .getString("lb_selected_langs")));
            Iterator<GlobalSightLocale> iter = data.trgLocaleList.iterator();
            int r = 2;
            while (iter.hasNext())
            {
                String locale = iter.next().toString();
                paramsSheet.addCell(new Label(4, r, locale));
                r++;
            }
        }

    }

    private String translateOpts(String opt, ResourceBundle bundle)
    {
        String optLabel = "";
        if (SearchCriteriaParameters.NOW.equals(opt))
        {
            optLabel = bundle.getString("lb_now");
        }
        else if (SearchCriteriaParameters.HOURS_AGO.equals(opt))
        {
            optLabel = bundle.getString("lb_hours_ago");
        }
        else if (SearchCriteriaParameters.DAYS_AGO.equals(opt))
        {
            optLabel = bundle.getString("lb_days_ago");
        }
        else if (SearchCriteriaParameters.WEEKS_AGO.equals(opt))
        {
            optLabel = bundle.getString("lb_weeks_ago");
        }
        else if (SearchCriteriaParameters.MONTHS_AGO.equals(opt))
        {
            optLabel = bundle.getString("lb_months_ago");
        }

        return optLabel;
    }

    /**
     * Returns search params used to find the in progress jobs for all PMs
     * 
     * @return JobSearchParams
     */
    private JobSearchParameters getSearchParams(HttpServletRequest p_request)
            throws Exception
    {

        JobSearchParameters sp = new JobSearchParameters();

        sp.setJobState(data.jobStateList);

        if (!data.wantsAllProjects)
        {
            sp.setProjectId(data.projectIdList);
        }

        String paramCreateDateStartCount = p_request
                .getParameter(JobSearchConstants.CREATION_START);
        String paramCreateDateStartOpts = p_request
                .getParameter(JobSearchConstants.CREATION_START_OPTIONS);
        if (!"-1".equals(paramCreateDateStartOpts))
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
        else if (!"-1".equals(paramCreateDateEndOpts))
        {
            sp.setCreationEnd(new Integer(paramCreateDateEndCount));
            sp.setCreationEndCondition(paramCreateDateEndOpts);
        }

        return sp;
    }

    /**
     * Adds the table header to the sheet
     * 
     * @param p_sheet
     */
    private void addHeader(WritableSheet p_sheet, ResourceBundle bundle)
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
                + bundle.getString("file_list_report"), titleFormat));
        p_sheet.addCell(new Label(0, 1, bundle.getString("lb_desp_file_list")));
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

        int col = 0;

        p_sheet.addCell(new Label(col, 2, bundle.getString("lb_job_id"),
                headerFormat));
        p_sheet.setColumnView(col, 15);
        p_sheet.mergeCells(col, 2, col, 3);
        col++;
        p_sheet.addCell(new Label(col, 2, bundle.getString("lb_job_name"),
                headerFormat));
        p_sheet.setColumnView(col, 30);
        p_sheet.mergeCells(col, 2, col, 3);
        col++;
        p_sheet.addCell(new Label(col, 2, bundle.getString("lb_file_path"),
                headerFormat));
        p_sheet.setColumnView(col, 55);
        p_sheet.mergeCells(col, 2, col, 3);
        col++;
        p_sheet.addCell(new Label(col, 2, bundle.getString("lb_file_name"),
                headerFormat));
        p_sheet.setColumnView(col, 25);
        p_sheet.mergeCells(col, 2, col, 3);
        col++;
        p_sheet.addCell(new Label(col, 2, bundle.getString("reportDesc"),
                headerFormat));
        p_sheet.setColumnView(col, 30);
        p_sheet.mergeCells(col, 2, col, 3);
        col++;
        p_sheet.addCell(new Label(col, 2, bundle.getString("lb_creation_date"),
                headerFormat));
        p_sheet.setColumnView(col, 20);
        p_sheet.mergeCells(col, 2, col, 3);
        col++;
        p_sheet.addCell(new Label(col, 2, bundle.getString("lb_lang"),
                langFormat));
        p_sheet.mergeCells(col, 2, col, 3);
        col++;
        p_sheet.addCell(new Label(col, 2, bundle.getString("lb_word_counts"),
                wordCountFormat));
        wordCountCol = col;
        int span = 6;
        if (useDefaultContext && useInContext)
        {
            span += 2;
        }
        else if (useDefaultContext || useInContext)
        {
            span += 1;
        }

        p_sheet.mergeCells(col, 2, col + span, 2);

        p_sheet.addCell(new Label(col, 3, bundle
                .getString("jobinfo.tradosmatches.wordcounts.per100matches"),
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

        col++;
        if (useDefaultContext)
        {
            p_sheet.addCell(new Label(col, 3, bundle
                    .getString("lb_context_tm_report"), wordCountValueFormat));
        }

        if (useInContext)
        {
            if (useDefaultContext)
                col++;
            p_sheet.addCell(new Label(col, 3, bundle
                    .getString("lb_in_context_tm"), wordCountValueFormat));
        }

        if (useDefaultContext || useInContext)
            col++;
        p_sheet.addCell(new Label(col, 3, bundle.getString("lb_total"),
                wordCountValueRightFormat));

    }

    /**
     * Adds the header for CSV File
     * 
     * @param p_sheet
     * @author Leon.Song
     */
    private void addHeaderForCsv(ResourceBundle bundle) throws Exception
    {
        csvWriter.write(bundle.getString("lb_job_id"));
        csvWriter.write(bundle.getString("lb_job_name"));
        csvWriter.write(bundle.getString("lb_file_path"));
        csvWriter.write(bundle.getString("lb_file_name"));
        csvWriter.write(bundle.getString("reportDesc"));
        csvWriter.write(bundle.getString("lb_creation_date"));
        csvWriter.write(bundle.getString("lb_lang"));
        csvWriter.write(bundle
                .getString("jobinfo.tradosmatches.wordcounts.per100matches"));
        csvWriter.write(bundle.getString("lb_95_99"));
        csvWriter.write(bundle.getString("lb_85_94"));
        csvWriter.write(bundle.getString("lb_75_84") + "*");
        csvWriter.write(bundle.getString("lb_no_match"));
        csvWriter.write(bundle.getString("lb_repetition_word_cnt"));
        if (useDefaultContext)
        {
            csvWriter.write(bundle.getString("lb_context_tm_report"));
        }
        if (useInContext)
        {
            csvWriter.write(bundle.getString("lb_in_context_tm"));
        }
        csvWriter.write(bundle.getString("lb_total"));
        csvWriter.endRecord();
    }

    /**
     * Gets the jobs and outputs workflow information.
     * 
     * @exception Exception
     */
    private void addJobs(ArrayList<Job> p_jobs,
            List<GlobalSightLocale> p_targetLocales) throws Exception
    {
        WritableSheet sheet = m_workbook.createSheet(
                m_bundle.getString("jobinfo.tradosmatches"), 0);

        // sort jobs by job name
        Collections.sort(p_jobs, new JobComparator(Locale.US));

        getUseInContextInfos(p_jobs, data.wantsAllLocales, data.trgLocaleList);

        addHeader(sheet, m_bundle);

        Iterator jobIter = p_jobs.iterator();
        IntHolder row = new IntHolder(4);
        int finishedJobNum = 0;
        while (jobIter.hasNext())
        {
            // Cancel generate reports.
            if (isCancelled())
                return;
            // Sets Reports Percent.
            setPercent(++finishedJobNum);

            Job j = (Job) jobIter.next();
            Collection c = j.getWorkflows();
            Iterator wfIter = c.iterator();
            while (wfIter.hasNext())
            {
                Workflow w = (Workflow) wfIter.next();
                String state = w.getState();
                // skip certain workflow whose target locale is not selected
                if (!p_targetLocales.contains(w.getTargetLocale()))
                {
                    continue;
                }
                // if (data.workflowStateList.contains(state))
                if (Workflow.DISPATCHED.equals(state)
                        || Workflow.READY_TO_BE_DISPATCHED.equals(state)
                        || Workflow.PENDING.equals(state)
                        || Workflow.EXPORTED.equals(state)
                        || Workflow.EXPORT_FAILED.equals(state)
                        || Workflow.ARCHIVED.equals(state)
                        || Workflow.LOCALIZED.equals(state))
                {
                    addWorkflow(sheet, j, w, row);
                }
            }
        }

        row.inc();
        addTotals(sheet, row, m_bundle);
    }

    /**
     * Gets the jobs and outputs workflow information for CSV File.
     * 
     * @exception Exception
     * @author Leon.Song
     */
    private void addJobsForCsv(ArrayList<Job> p_jobs,
            List<GlobalSightLocale> p_targetLocales) throws Exception
    {
        // sort jobs by job name
        Collections.sort(p_jobs, new JobComparator(Locale.US));
        getUseInContextInfos(p_jobs, data.wantsAllLocales, data.trgLocaleList);

        addHeaderForCsv(m_bundle);

        Iterator jobIter = p_jobs.iterator();
        IntHolder row = new IntHolder(4);
        int finishedJobNum = 0;
        while (jobIter.hasNext())
        {
            // Cancel generate reports.
            if (isCancelled())
                return;
            // Sets Reports Percent.
            setPercent(++finishedJobNum);

            Job j = (Job) jobIter.next();
            Collection c = j.getWorkflows();
            Iterator wfIter = c.iterator();
            while (wfIter.hasNext())
            {
                Workflow w = (Workflow) wfIter.next();
                String state = w.getState();
                // skip certain workflow whose target locale is not selected
                if (!p_targetLocales.contains(w.getTargetLocale()))
                {
                    continue;
                }
                // if (data.workflowStateList.contains(state))
                if (Workflow.DISPATCHED.equals(state)
                        || Workflow.READY_TO_BE_DISPATCHED.equals(state)
                        || Workflow.PENDING.equals(state)
                        || Workflow.EXPORTED.equals(state)
                        || Workflow.EXPORT_FAILED.equals(state)
                        || Workflow.ARCHIVED.equals(state)
                        || Workflow.LOCALIZED.equals(state))
                {
                    addWorkflowForCsv(j, w);
                }
            }
        }
    }

    /**
     * Add totals for Excel File
     * 
     * @param p_sheet
     * @param p_row
     * @param bundle
     * @throws Exception
     */
    private void addTotals(WritableSheet p_sheet, IntHolder p_row,
            ResourceBundle bundle) throws Exception
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

        // For "Add Job Id into online job report" issue
        char sumStartCellCol = 'H';
        int c = 7;

        p_sheet.addCell(new Label(0, row, title, subTotalFormat));
        p_sheet.mergeCells(0, row, sumStartCellCol - 'B', row);
        int lastRow = p_row.getValue() - 1;

        // add in word count totals
        // word counts

        p_sheet.addCell(new Formula(c++, row, "SUM(" + sumStartCellCol + "5:"
                + sumStartCellCol + lastRow + ")", subTotalFormat)); // 100%
        // Matches
        sumStartCellCol++;
        p_sheet.addCell(new Formula(c++, row, "SUM(" + sumStartCellCol + "5:"
                + sumStartCellCol + lastRow + ")", subTotalFormat)); // 95-99%
        sumStartCellCol++;
        p_sheet.addCell(new Formula(c++, row, "SUM(" + sumStartCellCol + "5:"
                + sumStartCellCol + lastRow + ")", subTotalFormat)); // 85-94%
        sumStartCellCol++;
        p_sheet.addCell(new Formula(c++, row, "SUM(" + sumStartCellCol + "5:"
                + sumStartCellCol + lastRow + ")", subTotalFormat)); // 75-84%*
        sumStartCellCol++;
        p_sheet.addCell(new Formula(c++, row, "SUM(" + sumStartCellCol + "5:"
                + sumStartCellCol + lastRow + ")", subTotalFormat)); // No Match
        sumStartCellCol++;
        p_sheet.addCell(new Formula(c++, row, "SUM(" + sumStartCellCol + "5:"
                + sumStartCellCol + lastRow + ")", subTotalFormat)); // Repetitions
        sumStartCellCol++;
        if (useDefaultContext)
        {
            p_sheet.addCell(new Formula(c++, row, "SUM(" + sumStartCellCol
                    + "5:" + sumStartCellCol + lastRow + ")", subTotalFormat));
            sumStartCellCol++;
        }

        if (useInContext)
        {
            p_sheet.addCell(new Formula(c++, row, "SUM(" + sumStartCellCol
                    + "5:" + sumStartCellCol + lastRow + ")", subTotalFormat));
            sumStartCellCol++;
        }
        p_sheet.addCell(new Formula(c++, row, "SUM(" + sumStartCellCol + "5:"
                + sumStartCellCol + lastRow + ")", subTotalFormat)); // Total
        sumStartCellCol++;

    }

    /**
     * Get use In context info
     * 
     * @param jobs
     * @param wantsAllLocales
     * @param trgLocales
     */
    private void getUseInContextInfos(List<Job> jobs, boolean wantsAllLocales,
            List<GlobalSightLocale> trgLocales)
    {
        for (Job j : jobs)
        {
            for (Workflow wf : j.getWorkflows())
            {
                String state = wf.getState();

                // skip certain workflow whose target locale is not selected
                GlobalSightLocale trgLocale = wf.getTargetLocale();
                if (!wantsAllLocales && !trgLocales.contains(trgLocale))
                {
                    continue;
                }

                // if (data.workflowStateList.contains(state))
                if (Workflow.DISPATCHED.equals(state)
                        || Workflow.READY_TO_BE_DISPATCHED.equals(state)
                        || Workflow.PENDING.equals(state)
                        || Workflow.EXPORTED.equals(state)
                        || Workflow.EXPORT_FAILED.equals(state)
                        || Workflow.ARCHIVED.equals(state)
                        || Workflow.LOCALIZED.equals(state))
                {
                    for (TargetPage tp : wf.getTargetPages())
                    {
                        boolean isInContextMatch = PageHandler
                                .isInContextMatch(tp.getSourcePage()
                                        .getRequest().getJob());
                        boolean isDefaultContextMatch = PageHandler
                                .isDefaultContextMatch(tp);
                        if (isInContextMatch)
                        {
                            useInContext = true;
                        }
                        if (isDefaultContextMatch)
                        {
                            useDefaultContext = true;
                        }
                    }
                }
            }
        }
    }

    private String getProjectDesc(Job p_job)
    {
        Project p = p_job.getL10nProfile().getProject();
        String d = p.getDescription();
        String desc = null;
        if (d == null || d.length() == 0)
            desc = p.getName();
        else
            desc = p.getName() + ": " + d;
        return desc;
    }

    /**
     * Gets the task for the workflow and outputs page information.
     * 
     * @exception Exception
     */
    private void addWorkflow(WritableSheet p_sheet, Job p_job,
            Workflow p_workflow, IntHolder p_row) throws Exception
    {

        // write word count and file info
        for (TargetPage tg : p_workflow.getTargetPages())
        {
            // write job id, job name, description, creation date, creation
            // time, language
            p_sheet.addCell(new Label(0, p_row.value, "" + p_job.getId())); // job
            // id
            p_sheet.addCell(new Label(1, p_row.value, p_job.getJobName())); // job
            // name
            p_sheet.addCell(new Label(4, p_row.value, getProjectDesc(p_job))); // description
            p_sheet.addCell(new Label(5, p_row.value, dateFormat.format(p_job
                    .getCreateDate()))); // creation date
            p_sheet.addCell(new Label(6, p_row.value, p_workflow
                    .getTargetLocale().toString())); // language

            try
            {
                addWordCount(tg, p_row, p_sheet);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

            p_row.inc();
        }

    }

    /**
     * Gets the task for the workflow and outputs page information for CSV File.
     * 
     * @exception Exception
     * @author Leon.Song
     */
    private void addWorkflowForCsv(Job p_job, Workflow p_workflow)
            throws Exception
    {
        // write word count and file info
        for (TargetPage tg : p_workflow.getTargetPages())
        {
            boolean isInContextMatch = PageHandler.isInContextMatch(tg
                    .getSourcePage().getRequest().getJob());
            boolean isDefaultContextMatch = PageHandler
                    .isDefaultContextMatch(tg.getSourcePage().getRequest()
                            .getJob());
            // 100% match
            int segmentTmWordCount = 0;
            // in context word match
            int inContextWordCount = 0;
            boolean isUseDefaultContextMatch = PageHandler
                    .isDefaultContextMatch(tg);
            int contextMatchWC = tg.getWordCount().getContextMatchWordCount();
            if (isInContextMatch)
            {
                // 100% match
                segmentTmWordCount = tg.getWordCount().getSegmentTmWordCount();
                // in context word match
                inContextWordCount = tg.getWordCount().getInContextWordCount();
                contextMatchWC = 0;
            }
            else
            {
                if (isUseDefaultContextMatch)
                {
                    segmentTmWordCount = tg.getWordCount()
                            .getTotalExactMatchWordCount() - contextMatchWC;
                }
                else
                {
                    // 100% match
                    segmentTmWordCount = tg.getWordCount()
                            .getTotalExactMatchWordCount();
                    contextMatchWC = 0;
                }
            }
            // 95% match
            int hiFuzzyWordCount = tg.getWordCount().getHiFuzzyWordCount();
            // 85% match
            int medHiFuzzyWordCount = tg.getWordCount()
                    .getMedHiFuzzyWordCount();
            // 85% match
            int medFuzzyWordCount = tg.getWordCount().getMedFuzzyWordCount();
            // 50% match
            int lowFuzzyWordCount = tg.getWordCount().getLowFuzzyWordCount();
            // no match
            int unmatchedWordCount = tg.getWordCount().getUnmatchedWordCount();
            int lb_repetition_word_cnt = tg.getWordCount()
                    .getRepetitionWordCount();
            // int lb_sub_levmatch_repetition_word_cnt = tg.getWordCount()
            // .getSubLevRepetitionWordCount();
            int totalWords = tg.getWordCount().getTotalWordCount();

            String fileFullName = tg.getSourcePage().getExternalPageId();
            String filePath = fileFullName;
            String fileName = " ";
            if (fileFullName.indexOf("/") > -1)
            {
                fileName = fileFullName.substring(
                        fileFullName.lastIndexOf("/") + 1,
                        fileFullName.length());
                filePath = fileFullName.substring(0,
                        fileFullName.lastIndexOf("/"));
            }
            else if (fileFullName.indexOf("\\") > -1)
            {
                fileName = fileFullName.substring(
                        fileFullName.lastIndexOf("\\") + 1,
                        fileFullName.length());
                filePath = fileFullName.substring(0,
                        fileFullName.lastIndexOf("\\"));
            }

            // write job id, job name, description, creation date, creation
            // time, language
            csvWriter.write(String.valueOf(p_job.getId()));
            csvWriter.write(p_job.getJobName());
            csvWriter.write(filePath);
            csvWriter.write(fileName);
            csvWriter.write(getProjectDesc(p_job));
            csvWriter.write(dateFormat.format(p_job.getCreateDate()));
            // date
            csvWriter.write(p_workflow.getTargetLocale().toString());
            // write the information of word count
            csvWriter.write(String.valueOf(segmentTmWordCount));
            csvWriter.write(String.valueOf(hiFuzzyWordCount));
            csvWriter.write(String.valueOf(medHiFuzzyWordCount));
            csvWriter.write(String.valueOf(medFuzzyWordCount));
            csvWriter.write(String.valueOf(unmatchedWordCount
                    + lowFuzzyWordCount));
            csvWriter.write(String.valueOf(lb_repetition_word_cnt));
            if (useDefaultContext)
            {
                if (isDefaultContextMatch)
                {
                    csvWriter.write(String.valueOf(contextMatchWC));
                }
                else
                {
                    csvWriter.write(String.valueOf(0));
                }
            }
            if (useInContext)
            {
                if (isInContextMatch)
                {
                    csvWriter.write(String.valueOf(inContextWordCount));
                }
                else
                {
                    csvWriter.write(String.valueOf(0));
                }
            }
            csvWriter.write(String.valueOf(totalWords));
            csvWriter.endRecord();
        }
    }

    /**
     * Add word count for Excel File
     * 
     * @param tg
     * @param p_row
     * @param p_sheet
     * @return
     * @throws Exception
     */
    public int addWordCount(TargetPage tg, IntHolder p_row,
            WritableSheet p_sheet) throws Exception
    {
        boolean isInContextMatch = PageHandler.isInContextMatch(tg
                .getSourcePage().getRequest().getJob());
        boolean isDefaultContextMatch = PageHandler.isDefaultContextMatch(tg
                .getSourcePage().getRequest().getJob());
        // 100% match
        int segmentTmWordCount = 0;
        // in context word match
        int inContextWordCount = 0;
        boolean isUseDefaultContextMatch = PageHandler
                .isDefaultContextMatch(tg);
        int contextMatchWC = tg.getWordCount().getContextMatchWordCount();
        if (isInContextMatch)
        {
            // 100% match
            segmentTmWordCount = tg.getWordCount().getSegmentTmWordCount();
            // in context word match
            inContextWordCount = tg.getWordCount().getInContextWordCount();

            contextMatchWC = 0;
        }
        else
        {
            if (isUseDefaultContextMatch)
            {
                segmentTmWordCount = tg.getWordCount()
                        .getTotalExactMatchWordCount() - contextMatchWC;
            }
            else
            {
                segmentTmWordCount = tg.getWordCount()
                        .getTotalExactMatchWordCount();
                contextMatchWC = 0;
            }
        }
        int hiFuzzyWordCount = tg.getWordCount().getThresholdHiFuzzyWordCount();
        int medHiFuzzyWordCount = tg.getWordCount()
                .getThresholdMedHiFuzzyWordCount();
        int medFuzzyWordCount = tg.getWordCount()
                .getThresholdMedFuzzyWordCount();
        int lowFuzzyWordCount = tg.getWordCount().getLowFuzzyWordCount();
        int unmatchedWordCount = tg.getWordCount().getUnmatchedWordCount();
        int lb_repetition_word_cnt = tg.getWordCount().getRepetitionWordCount();
        int totalWords = tg.getWordCount().getTotalWordCount();

        String fileFullName = tg.getSourcePage().getExternalPageId();
        String filePath = fileFullName;
        String fileName = " ";
        if (fileFullName.indexOf("/") > -1)
        {
            fileName = fileFullName.substring(
                    fileFullName.lastIndexOf("/") + 1, fileFullName.length());
            filePath = fileFullName.substring(0, fileFullName.lastIndexOf("/"));
        }
        else if (fileFullName.indexOf("\\") > -1)
        {
            fileName = fileFullName.substring(
                    fileFullName.lastIndexOf("\\") + 1, fileFullName.length());
            filePath = fileFullName
                    .substring(0, fileFullName.lastIndexOf("\\"));
        }

        p_sheet.addCell(new Label(2, p_row.value, filePath));
        p_sheet.addCell(new Label(3, p_row.value, fileName));

        // write the information of word count
        int cursor = wordCountCol;
        p_sheet.addCell(new Number(cursor, p_row.value, segmentTmWordCount)); // 100%
        p_sheet.addCell(new Number(++cursor, p_row.value, hiFuzzyWordCount)); // 95-99%
        p_sheet.addCell(new Number(++cursor, p_row.value, medHiFuzzyWordCount)); // 85-94%
        p_sheet.addCell(new Number(++cursor, p_row.value, medFuzzyWordCount)); // 75-84%
        p_sheet.addCell(new Number(++cursor, p_row.value, unmatchedWordCount
                + lowFuzzyWordCount)); // no match
        p_sheet.addCell(new Number(++cursor, p_row.value,
                lb_repetition_word_cnt));
        cursor++;
        if (useDefaultContext)
        {
            if (isDefaultContextMatch)
            {
                p_sheet.addCell(new Number(cursor, p_row.value, contextMatchWC));
            }
            else
            {
                p_sheet.addCell(new Number(cursor, p_row.value, 0));
            }
        }
        if (useInContext)
        {
            if (useDefaultContext)
                cursor++;

            if (isInContextMatch)
            {
                p_sheet.addCell(new Number(cursor, p_row.value,
                        inContextWordCount));
            }
            else
            {
                p_sheet.addCell(new Number(cursor, p_row.value, 0));
            }
        }

        if (useDefaultContext || useInContext)
            cursor++;

        p_sheet.addCell(new Number(cursor, p_row.value, totalWords));

        return totalWords;

    }

    @Override
    public void setPercent(int p_finishedJobNum)
    {
        ReportGeneratorHandler.setReportsMapByGenerator(m_userId, data.jobIds,
                100 * p_finishedJobNum / data.jobIds.size());
    }

    @Override
    public boolean isCancelled()
    {
        ReportsData rData = ReportGeneratorHandler.getReportsMap(m_userId,
                data.jobIds);
        if (rData != null)
            return rData.isCancle();

        return false;
    }
}