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

import com.globalsight.everest.category.CategoryType;
import com.globalsight.everest.comment.Issue;
import com.globalsight.everest.comment.IssueHistory;
import com.globalsight.everest.comment.IssueImpl;
import com.globalsight.everest.comment.IssueOptions;
import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.edit.CommentHelper;
import com.globalsight.everest.foundation.SearchCriteriaParameters;
import com.globalsight.everest.integration.ling.LingServerProxy;
import com.globalsight.everest.integration.ling.tm2.LeverageMatch;
import com.globalsight.everest.integration.ling.tm2.MatchTypeStatistics;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.page.SourcePage;
import com.globalsight.everest.page.TargetPage;
import com.globalsight.everest.persistence.tuv.SegmentTuTuvCacheManager;
import com.globalsight.everest.persistence.tuv.SegmentTuUtil;
import com.globalsight.everest.persistence.tuv.SegmentTuvUtil;
import com.globalsight.everest.projecthandler.TranslationMemoryProfile;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.taskmanager.Task;
import com.globalsight.everest.tuv.TaskTuv;
import com.globalsight.everest.tuv.Tuv;
import com.globalsight.everest.tuv.TuvException;
import com.globalsight.everest.tuv.TuvManager;
import com.globalsight.everest.util.comparator.GlobalSightLocaleComparator;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.pagehandler.administration.company.Select;
import com.globalsight.everest.webapp.pagehandler.administration.reports.ReportConstants;
import com.globalsight.everest.webapp.pagehandler.administration.reports.ReportHelper;
import com.globalsight.everest.webapp.pagehandler.administration.reports.bo.ReportsData;
import com.globalsight.everest.webapp.pagehandler.administration.users.UserUtil;
import com.globalsight.everest.webapp.pagehandler.projects.workflows.JobSearchConstants;
import com.globalsight.everest.workflow.ScorecardScore;
import com.globalsight.everest.workflow.ScorecardScoreHelper;
import com.globalsight.everest.workflowmanager.Workflow;
import com.globalsight.ling.tm.LeverageMatchLingManager;
import com.globalsight.terminology.termleverager.TermLeverageManager;
import com.globalsight.terminology.termleverager.TermLeverageMatch;
import com.globalsight.util.ExcelUtil;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.ReportStyle;
import com.globalsight.util.SortUtil;
import com.globalsight.util.edit.EditUtil;
import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import javax.naming.NamingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.FileOutputStream;
import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Comments Analysis Report Generator Include Comments Analysis Report in popup
 * editor
 */
public class CommentsAnalysisReportGenerator implements ReportGenerator
{
    private static final Logger logger = Logger.getLogger(CommentsAnalysisReportGenerator.class);
    final String TAG_COMBINED = "-combined";
    private HttpServletRequest request = null;
    private ResourceBundle bundle = null;

    private static final String CATEGORY_FAILURE_DROP_DOWN_LIST = "categoryFailureDropDownList";

    private boolean isCombineAllJobs = false;

    private Map<String, Integer> rowsMap = new HashMap<String, Integer>();

    private static final String DEFAULT_DATE_FORMAT = "MM/dd/yy hh:mm:ss a z";

    protected String m_companyName = "";
    protected List<Long> m_jobIDS = new ArrayList<Long>();
    protected List<GlobalSightLocale> m_targetLocales = new ArrayList<GlobalSightLocale>();

    public static final String reportType = ReportConstants.COMMENTS_ANALYSIS_REPORT;

    public static final int LANGUAGE_HEADER_ROW = 3;
    public static final int LANGUAGE_INFO_ROW = 4;
    public  int SEGMENT_HEADER_ROW = 6;
    public int SEGMENT_START_ROW = 7;
    public int SCORECARD_START_ROW = 0;
    public int DQF_START_ROW = 0;
    public int NEXT_DQF_COLUMN = 1;

    // "I" column, index 8
    public int CATEGORY_FAILURE_COLUMN = 10;
    public int SEVERITY_COLUMN = 11;
    public int COMMENT_STATUS_COLUMN = 11;

    private Locale uiLocale = new Locale("en", "US");
    String m_userId;
    private boolean isDQFEnabled = false;
    private boolean isScorecardEnabled = false;
    private String fluencyScore = "";
    private String adequacyScore = "";
    private String dqfComment = "";
    private List<ScorecardScore> scores = null;
    private List<String> scorecardCategories = null;
    private String scoreComment = "";
    private boolean needProtect = false;
    private boolean hasDQFInfo = false;

    private ArrayList<String> jobStateList = null;

    private ReportStyle REPORT_STYLE = null;

    /**
     * Comments Analysis Report Generator Constructor.
     * 
     * @param p_request
     *            the request
     * @param p_response
     *            the response
     * @throws Exception
     */
    public CommentsAnalysisReportGenerator(HttpServletRequest p_request,
            HttpServletResponse p_response) throws Exception
    {
        request = p_request;
        HttpSession session = p_request.getSession();

        m_userId = (String) session.getAttribute(WebAppConstants.USER_NAME);
        m_companyName = UserUtil.getCurrentCompanyName(request);
        CompanyThreadLocal.getInstance().setValue(m_companyName);
        uiLocale = (Locale) session.getAttribute(WebAppConstants.UILOCALE);
        if (uiLocale == null)
        {
            uiLocale = Locale.US;
        }

        if (p_request.getParameter(ReportConstants.JOB_IDS) != null)
        {
            m_jobIDS = ReportHelper.getListOfLong(p_request.getParameter(ReportConstants.JOB_IDS));
            GlobalSightLocaleComparator comparator = new GlobalSightLocaleComparator(
                    GlobalSightLocaleComparator.ISO_CODE, uiLocale);
            m_targetLocales = ReportHelper.getTargetLocaleList(
                    p_request.getParameterValues(ReportConstants.TARGETLOCALE_LIST), comparator);
        }
        bundle = PageHandler.getBundle(request.getSession());

        String combineAllJobs = p_request.getParameter("combineAllJobs");
        if ("on".equals(combineAllJobs))
        {
            isCombineAllJobs = true;
        }
        else
        {
            isCombineAllJobs = false;
        }
    }

    /**
     * Send Report Files to Client
     * 
     * @param p_jobIDS
     *            Job ID List
     * @param p_targetLocales
     *            Target Locales List
     * @param p_companyName
     *            Company Name
     * @param p_response
     *            HttpServletResponse
     * @throws Exception
     */
    public void sendReports(List<Long> p_jobIDS, List<GlobalSightLocale> p_targetLocales,
            HttpServletResponse p_response) throws Exception
    {
        if (p_jobIDS == null || p_jobIDS.size() == 0)
        {
            p_jobIDS = m_jobIDS;
            p_targetLocales = m_targetLocales;
        }
        else if (m_jobIDS == null || m_jobIDS.size() == 0)
        {
            m_jobIDS = p_jobIDS;
        }

        File[] reports = generateReports(p_jobIDS, p_targetLocales);
        ReportHelper.sendFiles(reports, getReportType(), p_response);
    }

    /**
     * Get job state list expect PENDING state
     * @return List of job states
     */
    private ArrayList<String> getJobStateList()
    {
        if (jobStateList == null)
        {
            jobStateList = ReportHelper.getAllJobStatusList();
            jobStateList.remove(Job.PENDING);
        }
        return jobStateList;
    }

    /**
     * Generates Excel report
     * 
     * @param p_jobIDS
     *            Job ID List
     * @param p_targetLocales
     *            Target Locales List
     * @param p_companyName
     *            Company Name
     * @throws Exception
     */
    public File[] generateReports(List<Long> p_jobIDS, List<GlobalSightLocale> p_targetLocales)
            throws Exception
    {
        ArrayList<String> stateList = getJobStateList();

        if (p_jobIDS == null || p_jobIDS.size() == 0)
        {
            p_jobIDS = m_jobIDS;
            p_targetLocales = m_targetLocales;
        }
        else if (m_jobIDS == null || m_jobIDS.size() == 0)
        {
            m_jobIDS = p_jobIDS;
        }
        List<File> workBooks = new ArrayList<File>();
        String dateFormat = request.getParameter(WebAppConstants.DATE_FORMAT);
        Workbook combinedWorkBook = new SXSSFWorkbook();
        REPORT_STYLE = new ReportStyle(combinedWorkBook);
        List<Job> jobsList = new ArrayList<Job>();
        Set<String> stateSet = new HashSet<String>();
        Set<String> projectSet = new HashSet<String>();
        if (dateFormat == null)
        {
            dateFormat = DEFAULT_DATE_FORMAT;
        }

        HashMap<GlobalSightLocale, ArrayList<DQFDataInCAR>> data = null;
        int finishedJobNum = 0;
        data = getDQFInfo(p_jobIDS);

        for (long jobID : p_jobIDS)
        {
            // Cancel generate reports.
            if (isCancelled())
                return null;

            Job job = ServerProxy.getJobHandler().getJobById(jobID);
            if (job == null || !stateList.contains(job.getState()))
                continue;

            stateSet.add(job.getDisplayState());
            projectSet.add(job.getProject().getName());
            if (isCombineAllJobs)
            {
                jobsList.add(job);
                createReport(combinedWorkBook, job, p_targetLocales, dateFormat, data);
            }
            else
            {
                setAllCellStyleNull();
                Workbook workBook = new SXSSFWorkbook();
                REPORT_STYLE = new ReportStyle(workBook);
                createReport(workBook, job, p_targetLocales, dateFormat, data);
                File file = getFile(reportType, job, workBook);
                FileOutputStream out = new FileOutputStream(file);
                workBook.write(out);
                out.close();
                ((SXSSFWorkbook) workBook).dispose();
                workBooks.add(file);
            }

            // Sets Reports Percent.
            setPercent(++finishedJobNum);
        }

        if (isCombineAllJobs)
        {
            File file = getFile(reportType + TAG_COMBINED, null, combinedWorkBook);
            addCriteriaSheet(combinedWorkBook, jobsList, stateSet, projectSet);
            FileOutputStream out = new FileOutputStream(file);
            combinedWorkBook.write(out);
            out.close();
            ((SXSSFWorkbook) combinedWorkBook).dispose();
            workBooks.add(file);
        }

        return ReportHelper.moveReports(workBooks, m_userId);
    }

    /**
     * Get Scorecard/DQF info from all selected jobs
     * @param p_jobIDS
     * @throws RemoteException
     * @throws NamingException
     */
    private HashMap<GlobalSightLocale, ArrayList<DQFDataInCAR>> getDQFInfo(List<Long> p_jobIDS)
            throws RemoteException, NamingException
    {
        ArrayList<String> stateList = getJobStateList();
        HashMap<GlobalSightLocale, ArrayList<DQFDataInCAR>> data = new HashMap<>();
        GlobalSightLocale targetLocale;
        DQFDataInCAR dqfData;
        ArrayList<DQFDataInCAR> tmpList;

        for (long jobId : p_jobIDS)
        {
            Job job = ServerProxy.getJobHandler().getJobById(jobId);
            if (job == null || !stateList.contains(job.getState()))
                continue;

            for (Workflow workflow : job.getWorkflows())
            {
                targetLocale = workflow.getTargetLocale();

                int scoreShowType = workflow.getScorecardShowType();
                if (scoreShowType > -1)
                {
                    //Has Scorecard or DQF info
                    dqfData = new DQFDataInCAR();
                    dqfData.setJobId(job.getJobId());
                    dqfData.setJobName(job.getJobName());
                    dqfData.setWorkflowId(workflow.getId());
                    dqfData.setFluencyScore(workflow.getFluencyScore());
                    dqfData.setAdequacyScore(workflow.getAdequacyScore());
                    dqfData.setDqfComment(workflow.getDQFComment());
                    dqfData.setScorecardComment(workflow.getScorecardComment());
                    dqfData.setScorecards(ScorecardScoreHelper.getScoreByWrkflowId(workflow.getId()));

                    tmpList = data.get(targetLocale);
                    if (tmpList == null)
                        tmpList = new ArrayList<>();
                    tmpList.add(dqfData);
                    data.put(targetLocale, tmpList);
                }
            }
        }
        return data;
    }

    /**
     * Create the report sheets
     * 
     * @throws Exception
     */
    private void createReport(Workbook p_workbook, Job p_job,
            List<GlobalSightLocale> p_targetLocales, String p_dateFormat,
            HashMap<GlobalSightLocale, ArrayList<DQFDataInCAR>> dqfInfo) throws Exception
    {
        List<GlobalSightLocale> jobTL = ReportHelper.getTargetLocals(p_job);
        for (GlobalSightLocale trgLocale : p_targetLocales)
        {
            if (!jobTL.contains(trgLocale))
                continue;

            String srcLang = p_job.getSourceLocale().getDisplayName(uiLocale);
            String trgLang = trgLocale.getDisplayName(uiLocale);

            // Create One Report Sheet/Tab
            Sheet sheet = null;
            int columnIndexOfJobs = 0;
            if (isCombineAllJobs && p_workbook.getSheet(trgLocale.toString()) != null)
            {
                //To judge if current target locale contains workflow which enables DQF/Scorecard
                if (dqfInfo != null && dqfInfo.get(trgLocale) != null && dqfInfo.get(trgLocale).size() > 0)
                {
                    isDQFEnabled = true;
                    isScorecardEnabled = true;
                }
                else
                {
                    isDQFEnabled = false;
                    isScorecardEnabled = false;
                }

                sheet = p_workbook.getSheet(trgLocale.toString());
            }
            else
            {
                fluencyScore = "";
                adequacyScore = "";
                dqfComment = "";
                scores = null;
                scoreComment = "";
                isDQFEnabled = false;
                isScorecardEnabled = false;
                needProtect = false;

                DQF_START_ROW = 0;
                SCORECARD_START_ROW = 0;
                SEGMENT_HEADER_ROW = 6;
                SEGMENT_START_ROW = 7;

                sheet = p_workbook.createSheet(trgLocale.toString());
                sheet.protectSheet("");

                // Add Title
                addTitle(p_workbook, sheet);

                // Add Locale Pair Header
                addLanguageHeader(p_workbook, sheet);

                columnIndexOfJobs = addDQFHeader(p_workbook, sheet, p_job, trgLocale, dqfInfo);

                // Add Segment Header
                addSegmentHeader(p_workbook, sheet);

                // Insert Language Data
                writeLanguageInfo(p_workbook, sheet, srcLang, trgLang);
            }

            // Create Name Areas for drop down list.
            if (p_workbook.getName(CATEGORY_FAILURE_DROP_DOWN_LIST) == null)
            {
                String currentCompanyId = CompanyWrapper.getCurrentCompanyId();
                List<String> categories = CompanyWrapper.getCompanyCategoryNames(bundle,
                        currentCompanyId, CategoryType.SegmentComment, true);;
                ExcelUtil.createValidatorList(p_workbook, CATEGORY_FAILURE_DROP_DOWN_LIST,
                        categories, SEGMENT_START_ROW, columnIndexOfJobs + 1);

                categories = CompanyWrapper.getCompanyCategoryNames(bundle,
                        currentCompanyId, CategoryType.Severity, true);
                ExcelUtil.createValidatorList(p_workbook, "SeverityCategoriesValidator", categories,
                        SEGMENT_START_ROW, columnIndexOfJobs + 3);
            }

            // Insert Segment Data
            if (isCombineAllJobs)
            {
                Integer row = null;
                if (rowsMap.get(trgLang) == null)
                {
                    row = writeSegmentInfo(p_workbook, sheet, p_job, trgLocale, p_dateFormat,
                            SEGMENT_START_ROW);
                }
                else
                {
                    row = writeSegmentInfo(p_workbook, sheet, p_job, trgLocale, p_dateFormat,
                            rowsMap.get(trgLang));
                }
                rowsMap.put(trgLang, row);
            }
            else
            {
                writeSegmentInfo(p_workbook, sheet, p_job, trgLocale, p_dateFormat,
                        SEGMENT_START_ROW);
            }
        }
    }

    private int addDQFHeader(Workbook workbook, Sheet sheet, Job job, GlobalSightLocale targetLocale,
                              HashMap<GlobalSightLocale, ArrayList<DQFDataInCAR>> dqfInfo) throws Exception {
        int col = 0;
        int row = LANGUAGE_HEADER_ROW;
        int columnIndex = 0;
        Row rowLine = null;
        Cell cell = null;
        boolean isStored = false;

        if (job == null || targetLocale == null || dqfInfo == null)
            return 0;

        ArrayList<DQFDataInCAR> data = dqfInfo.get(targetLocale);
        HashMap<String, Integer> elements = new HashMap<String, Integer>();

        if (data != null && data.size() > 0)
        {
            // Current target locale has job(s) which has DQF/Scorecard info
            // Set basic info
            isDQFEnabled = true;
            isScorecardEnabled = true;
            COMMENT_STATUS_COLUMN = 12;
            DQF_START_ROW = 6;
            SCORECARD_START_ROW = isDQFEnabled ? 11 : 7;

            // write DQF/Scorecard header info
            int rowIndex = DQF_START_ROW;
            rowLine = ExcelUtil.getRow(sheet, rowIndex);
            cell = ExcelUtil.getCell(rowLine, 0);
            cell.setCellStyle(REPORT_STYLE.getHeaderStyle());
            cell.setCellValue(bundle.getString("lb_job_name"));

            rowIndex++;
            rowLine = ExcelUtil.getRow(sheet, rowIndex);
            cell = ExcelUtil.getCell(rowLine, 0);
            cell.setCellStyle(REPORT_STYLE.getHeaderStyle());
            cell.setCellValue(bundle.getString("lb_dqf_fluency_only"));

            rowIndex++;
            rowLine = ExcelUtil.getRow(sheet, rowIndex);
            cell = ExcelUtil.getCell(rowLine, 0);
            cell.setCellStyle(REPORT_STYLE.getHeaderStyle());
            cell.setCellValue(bundle.getString("lb_dqf_adequacy_only"));

            rowIndex++;
            rowLine = ExcelUtil.getRow(sheet, rowIndex);
            cell = ExcelUtil.getCell(rowLine, 0);
            cell.setCellStyle(REPORT_STYLE.getHeaderStyle());
            cell.setCellValue(bundle.getString("lb_comment"));

            rowIndex = SCORECARD_START_ROW;
            rowLine = ExcelUtil.getRow(sheet, rowIndex);
            cell = ExcelUtil.getCell(rowLine, 0);
            cell.setCellValue(bundle.getString("lb_scorecard"));
            cell.setCellStyle(REPORT_STYLE.getHeaderStyle());

            rowIndex++;
            long companyId = CompanyWrapper.getCurrentCompany().getId();
            scorecardCategories = ScorecardScoreHelper.getScorecardCategories(companyId, bundle);
            if (scorecardCategories != null && scorecardCategories.size() > 0)
            {
                for (String scorecard : scorecardCategories)
                {
                    rowLine = ExcelUtil.getRow(sheet, rowIndex);
                    cell = ExcelUtil.getCell(rowLine, 0);
                    cell.setCellValue(scorecard);

                    elements.put(scorecard, Integer.valueOf(rowIndex));
                    rowIndex++;
                }

                rowLine = ExcelUtil.getRow(sheet, rowIndex);
                cell = ExcelUtil.getCell(rowLine, 0);
                cell.setCellStyle(REPORT_STYLE.getHeaderStyle());
                cell.setCellValue(bundle.getString("lb_comment"));
            }
            SEGMENT_HEADER_ROW = SCORECARD_START_ROW + scorecardCategories.size() + 3;
            SEGMENT_START_ROW = SEGMENT_HEADER_ROW + 1;

            // Write DQF/Scorecard info
            columnIndex = 1;
            DQFDataInCAR dqfData;
            for (int i=0;i<data.size();i++) {
                dqfData = data.get(i);

                if (!isCombineAllJobs && dqfData.getJobId() != job.getId())
                        continue;

                // Job name
                rowIndex = DQF_START_ROW;
                rowLine = ExcelUtil.getRow(sheet, rowIndex);
                cell = ExcelUtil.getCell(rowLine, columnIndex);
                cell.setCellStyle(REPORT_STYLE.getContentStyle());
                cell.setCellValue(dqfData.getJobName());

                // Fluency
                rowIndex++;
                rowLine = ExcelUtil.getRow(sheet, rowIndex);
                cell = ExcelUtil.getCell(rowLine, columnIndex);
                cell.setCellStyle(REPORT_STYLE.getContentStyle());
                cell.setCellValue(dqfData.getFluencyScore());

                // Adequacy
                rowIndex++;
                rowLine = ExcelUtil.getRow(sheet, rowIndex);
                cell = ExcelUtil.getCell(rowLine, columnIndex);
                cell.setCellStyle(REPORT_STYLE.getContentStyle());
                cell.setCellValue(dqfData.getAdequacyScore());

                // DQF Comment
                rowIndex++;
                rowLine = ExcelUtil.getRow(sheet, rowIndex);
                cell = ExcelUtil.getCell(rowLine, columnIndex);
                cell.setCellStyle(REPORT_STYLE.getContentStyle());
                cell.setCellValue(dqfData.getDqfComment());

                scores = dqfData.getScorecards();
                rowIndex = SCORECARD_START_ROW;
                String key = "";
                Integer rowValue = 0;
                for (ScorecardScore score : scores)
                {
                    key = score.getScorecardCategory();
                    rowValue = elements.get(key);
                    rowLine = ExcelUtil.getRow(sheet, rowValue);
                    cell = ExcelUtil.getCell(rowLine, columnIndex);
                    cell.setCellStyle(REPORT_STYLE.getContentStyle());
                    cell.setCellValue(score.getScore());
                    rowIndex++;
                }

                // Scorecard Comment
                rowIndex++;
                rowLine = ExcelUtil.getRow(sheet, rowIndex);
                cell = ExcelUtil.getCell(rowLine, columnIndex);
                cell.setCellStyle(REPORT_STYLE.getContentStyle());
                cell.setCellValue(dqfData.getScorecardComment());

                columnIndex++;
            }
        }
        if (columnIndex < 26)
            columnIndex = 26;

        return columnIndex;
    }


    /**
     * Add segment header to the sheet for Comments Analysis Report
     * 
     * @param p_workBook
     * @param p_sheet
     *            the sheet
     * @throws Exception
     */
    private void addSegmentHeader(Workbook p_workBook, Sheet p_sheet) throws Exception
    {
        int col = 0;
        int row = SEGMENT_HEADER_ROW;
        Row segHeaderRow = ExcelUtil.getRow(p_sheet, row);
        CellStyle headerStyle = REPORT_STYLE.getHeaderStyle();

        Cell cell = ExcelUtil.getCell(segHeaderRow, col);
        cell.setCellValue(bundle.getString("lb_job_id_report"));
        cell.setCellStyle(headerStyle);
        p_sheet.setColumnWidth(col, 20 * 256);
        col++;

        cell = ExcelUtil.getCell(segHeaderRow, col);
        cell.setCellValue(bundle.getString("lb_job_name"));
        cell.setCellStyle(headerStyle);
        p_sheet.setColumnWidth(col, 40 * 256);
        col++;

        cell = ExcelUtil.getCell(segHeaderRow, col);
        cell.setCellValue(bundle.getString("lb_segment_id"));
        cell.setCellStyle(headerStyle);
        p_sheet.setColumnWidth(col, 20 * 256);
        col++;

        cell = ExcelUtil.getCell(segHeaderRow, col);
        cell.setCellValue(bundle.getString("lb_page_name"));
        cell.setCellStyle(headerStyle);
        p_sheet.setColumnWidth(col, 25 * 256);
        col++;

        cell = ExcelUtil.getCell(segHeaderRow, col);
        cell.setCellValue(bundle.getString("lb_targetpage_id"));
        cell.setCellStyle(headerStyle);
        p_sheet.setColumnWidth(col, 20 * 256);
        col++;

        cell = ExcelUtil.getCell(segHeaderRow, col);
        cell.setCellValue(bundle.getString("lb_source_segment"));
        cell.setCellStyle(headerStyle);
        p_sheet.setColumnWidth(col, 40 * 256);
        col++;

        Cell cell_E = ExcelUtil.getCell(segHeaderRow, col);
        cell_E.setCellValue(bundle.getString("lb_target_segment"));
        cell_E.setCellStyle(headerStyle);
        p_sheet.setColumnWidth(col, 40 * 256);
        col++;

        cell = ExcelUtil.getCell(segHeaderRow, col);
        cell.setCellValue(bundle.getString("lb_sid"));
        cell.setCellStyle(headerStyle);
        p_sheet.setColumnWidth(col, 40 * 256);
        col++;

        cell = ExcelUtil.getCell(segHeaderRow, col);
        cell.setCellValue(bundle.getString("lb_character_count"));
        cell.setCellStyle(headerStyle);
        p_sheet.setColumnWidth(col, 30 * 256);
        col++;

        //TODO: AS a potential requirement of GBS-4638, comments are better to separate into translation and reviewer two parts
        cell = ExcelUtil.getCell(segHeaderRow, col);
        cell.setCellValue(bundle.getString("lb_comments"));
        cell.setCellStyle(headerStyle);
        p_sheet.setColumnWidth(col, 40 * 256);
        col++;

//        cell = ExcelUtil.getCell(segHeaderRow, col);
//        cell.setCellValue(bundle.getString("lb_translator_comments"));
//        cell.setCellStyle(headerStyle);
//        p_sheet.setColumnWidth(col, 40 * 256);
//        col++;

        cell = ExcelUtil.getCell(segHeaderRow, col);
        cell.setCellValue(bundle.getString("lb_category_failure"));
        cell.setCellStyle(headerStyle);
        p_sheet.setColumnWidth(col, 30 * 256);
        col++;

        if (isDQFEnabled)
        {
            cell = ExcelUtil.getCell(segHeaderRow, col);
            cell.setCellValue(bundle.getString("lb_dqf_severity"));
            cell.setCellStyle(headerStyle);
            p_sheet.setColumnWidth(col, 15 * 256);
            col++;
        }

        cell = ExcelUtil.getCell(segHeaderRow, col);
        cell.setCellValue(bundle.getString("lb_comment_status"));
        cell.setCellStyle(headerStyle);
        p_sheet.setColumnWidth(col, 15 * 256);
        col++;


        cell = ExcelUtil.getCell(segHeaderRow, col);
        cell.setCellValue(bundle.getString("lb_tm_match_original"));
        cell.setCellStyle(headerStyle);
        p_sheet.setColumnWidth(col, 30 * 256);
        col++;

        cell = ExcelUtil.getCell(segHeaderRow, col);
        cell.setCellValue(bundle.getString("lb_glossary_source"));
        cell.setCellStyle(headerStyle);
        p_sheet.setColumnWidth(col, 30 * 256);
        col++;

        cell = ExcelUtil.getCell(segHeaderRow, col);
        cell.setCellValue(bundle.getString("lb_glossary_target"));
        cell.setCellStyle(headerStyle);
        p_sheet.setColumnWidth(col, 30 * 256);
        col++;

        cell = ExcelUtil.getCell(segHeaderRow, col);
        cell.setCellValue(bundle.getString("lb_previous_segment_report"));
        cell.setCellStyle(headerStyle);
        p_sheet.setColumnWidth(col, 40 * 256);
    }

    /**
     * Add job header to the sheet
     * 
     * @param p_workBook
     * @param p_sheet
     *            the sheet
     * @throws Exception
     */
    private void addLanguageHeader(Workbook p_workBook, Sheet p_sheet) throws Exception
    {
        int col = 0;
        int row = LANGUAGE_HEADER_ROW;

        Row langRow = ExcelUtil.getRow(p_sheet, row);
        Cell cell = ExcelUtil.getCell(langRow, col);
        cell.setCellValue(bundle.getString("lb_source_language"));
        cell.setCellStyle(REPORT_STYLE.getHeaderStyle());
        col++;

        cell = ExcelUtil.getCell(langRow, col);
        cell.setCellValue(bundle.getString("lb_target_language"));
        cell.setCellStyle(REPORT_STYLE.getHeaderStyle());
    }

    /**
     * Add title to the sheet
     * 
     * @param p_workBook
     * @param p_sheet
     *            the sheet
     * @throws Exception
     */
    private void addTitle(Workbook p_workBook, Sheet p_sheet) throws Exception
    {
        Row titleRow = ExcelUtil.getRow(p_sheet, 0);
        Cell titleCell = ExcelUtil.getCell(titleRow, 0);
        titleCell.setCellStyle(REPORT_STYLE.getTitleStyle());
        titleCell.setCellValue(bundle.getString("review_comments"));
    }

    private void writeLanguageInfo(Workbook p_workbook, Sheet p_sheet, String p_sourceLang,
            String p_targetLang) throws Exception
    {
        int col = 0;
        int row = LANGUAGE_INFO_ROW;

        CellStyle contentStyle = REPORT_STYLE.getContentStyle();
        Row langInfoRow = ExcelUtil.getRow(p_sheet, row);

        // Source Language
        Cell cell = ExcelUtil.getCell(langInfoRow, col++);
        cell.setCellValue(p_sourceLang);
        cell.setCellStyle(contentStyle);

        // Target Language
        cell = ExcelUtil.getCell(langInfoRow, col++);
        cell.setCellValue(p_targetLang);
        cell.setCellStyle(contentStyle);
    }

    /**
     * For Comments Analysis Report, Write segment information into each row of
     * the sheet.
     * 
     * @param p_workBook
     *            the workBook
     * @param p_sheet
     *            the sheet
     * @param p_job
     *            the job data for report
     * @param p_targetLocale
     *            the target locale
     * @param p_dateFormat
     *            the dateFormat
     * @param p_row
     *            the segment row in sheet
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    private int writeSegmentInfo(Workbook p_workBook, Sheet p_sheet, Job p_job,
            GlobalSightLocale p_targetLocale, String p_dateFormat, int p_row) throws Exception
    {
        SimpleDateFormat dateFormat = new SimpleDateFormat(p_dateFormat);
        Collection<TargetPage> targetPages = null;

        long jobId = p_job.getId();
        boolean toShowSeverity = false;

        TranslationMemoryProfile tmp = p_job.getL10nProfile().getTranslationMemoryProfile();
        Vector<String> excludItems = null;

        for (Workflow workflow : p_job.getWorkflows())
        {
            if (Workflow.PENDING.equals(workflow.getState())
                    || Workflow.CANCELLED.equals(workflow.getState())
                    // || Workflow.EXPORT_FAILED.equals(workflow.getState())
                    || Workflow.IMPORT_FAILED.equals(workflow.getState()))
            {
                continue;
            }
            if (p_targetLocale.getId() == workflow.getTargetLocale().getId())
            {
                if (workflow.enableDQF())
                    toShowSeverity = true;
                targetPages = workflow.getTargetPages();
                tmp = workflow.getJob().getL10nProfile().getTranslationMemoryProfile();
                if (tmp != null)
                {
                    excludItems = tmp.getJobExcludeTuTypes();
                }
            }
        }

        if (!targetPages.isEmpty())
        {
            LeverageMatchLingManager leverageMatchLingManager = LingServerProxy
                    .getLeverageMatchLingManager();
            TermLeverageManager termLeverageManager = ServerProxy.getTermLeverageManager();
            Map<Long, Set<TermLeverageMatch>> termLeverageMatchResultMap = termLeverageManager
                    .getTermMatchesForPages(p_job.getSourcePages(), p_targetLocale);

            SourcePage sourcePage = null;
            String sourceSegmentString = null;
            String targetSegmentString = null;
            String sid = null;
            for (TargetPage targetPage : targetPages)
            {
                sourcePage = targetPage.getSourcePage();
                SegmentTuUtil.getTusBySourcePageId(sourcePage.getId());
                List sourceTuvs = SegmentTuvUtil.getSourceTuvs(sourcePage);
                List targetTuvs = SegmentTuvUtil.getTargetTuvs(targetPage);

                // Leverage TM
                MatchTypeStatistics tuvMatchTypes = leverageMatchLingManager
                        .getMatchTypesForStatistics(sourcePage.getIdAsLong(),
                                targetPage.getLocaleId(), p_job.getLeverageMatchThreshold());
                Map<Long, Set<LeverageMatch>> fuzzyLeverageMatcheMap = leverageMatchLingManager
                        .getFuzzyMatches(sourcePage.getIdAsLong(), targetPage.getLocaleId());

                // Find segment comments
                Map<Long, IssueImpl> issuesMap = CommentHelper.getIssuesMap(targetPage.getId());

                // tuvId : Tuv
                Map<Long, Tuv> allTuvMap = this.getAllTuvsMap(targetPage);

                Locale sourcePageLocale = sourcePage.getGlobalSightLocale().getLocale();
                Locale targetPageLocale = targetPage.getGlobalSightLocale().getLocale();
                boolean rtlSourceLocale = EditUtil.isRTLLocale(sourcePageLocale.toString());
                boolean rtlTargetLocale = EditUtil.isRTLLocale(targetPageLocale.toString());

                String category = null;
                for (int j = 0; j < targetTuvs.size(); j++)
                {
                    int col = 0;
                    Tuv targetTuv = (Tuv) targetTuvs.get(j);
                    Tuv sourceTuv = (Tuv) sourceTuvs.get(j);
                    sourceSegmentString = sourceTuv.getGxmlElement().getTextValue();
                    targetSegmentString = targetTuv.getGxmlElement().getTextValue();
                    sid = sourceTuv.getSid();

                    category = sourceTuv.getTu(jobId).getTuType();
                    if (excludItems != null && excludItems.contains(category))
                    {
                        continue;
                    }

                    StringBuilder matches = ReportGeneratorUtil.getMatches(fuzzyLeverageMatcheMap,
                            tuvMatchTypes, excludItems, sourceTuvs, targetTuvs, bundle, sourceTuv,
                            targetTuv, p_job.getId());

                    List<IssueHistory> issueHistories = new ArrayList<IssueHistory>();
                    CellStyle contentStyle = REPORT_STYLE.getContentStyle();
                    CellStyle unlockedStyle = REPORT_STYLE.getUnlockedStyle();
                    Cell cell = null;
                    String failure = "";
                    String commentStatus = "";
                    String severity = "";

                    Issue issue = issuesMap.get(targetTuv.getId());
                    if (issue != null)
                    {
                        issueHistories = issue.getHistory();
                        failure = issue.getCategory();
                        severity = issue.getSeverity();
                        commentStatus = issue.getStatus();
                    }

                    StringBuilder comments = new StringBuilder();
                    StringBuilder reviewerComments = new StringBuilder();
                    int size = issueHistories.size();
                    if (size > 0)
                    {
                        int lastOne = size - 1;
                        IssueHistory issueHistory;
                        String date;
                        for (int k = 0; k < lastOne; k++)
                        {
                            issueHistory = (IssueHistory) issueHistories.get(k);
                            date = dateFormat.format(issueHistory.dateReportedAsDate());
                            if ("T".equals(issueHistory.getType()))
                            {
                                comments.append("[").append(date).append("     ")
                                        .append(UserUtil.getUserNameById(issueHistory.reportedBy()))
                                        .append("]:\r\n").append(issueHistory.getComment()).append("\r\n");
                            }
                            else
                            {
                                reviewerComments.append("[").append(date).append("     ")
                                        .append(UserUtil.getUserNameById(issueHistory.reportedBy()))
                                        .append("]:\r\n").append(issueHistory.getComment()).append("\r\n");
                            }
                        }
                        issueHistory = (IssueHistory) issueHistories.get(lastOne);
                        date = dateFormat.format(issueHistory.dateReportedAsDate());
                        if ("T".equals(issueHistory.getType()))
                        {
                            comments.append("[").append(date).append("     ")
                                    .append(UserUtil.getUserNameById(issueHistory.reportedBy()))
                                    .append("]:\r\n").append(issueHistory.getComment());
                        }
                        else
                        {
                            reviewerComments.append("[").append(date).append("     ")
                                    .append(UserUtil.getUserNameById(issueHistory.reportedBy()))
                                    .append("]:\r\n").append(issueHistory.getComment());
                        }
                    }

                    // Get Terminology/Glossary Source and Target.
                    String sourceTerms = "";
                    String targetTerms = "";
                    if (termLeverageMatchResultMap != null)
                    {
                        Set<TermLeverageMatch> termLeverageMatchSet = termLeverageMatchResultMap
                                .get(sourceTuv.getId());
                        if (termLeverageMatchSet != null)
                        {
                            TermLeverageMatch tlm = termLeverageMatchSet.iterator().next();
                            sourceTerms = tlm.getMatchedSourceTerm();
                            targetTerms = tlm.getMatchedTargetTerm();
                        }
                    }

                    Row currentRow = ExcelUtil.getRow(p_sheet, p_row);

                    // Job id
                    cell = ExcelUtil.getCell(currentRow, col);
                    cell.setCellValue(p_job.getId());
                    cell.setCellStyle(contentStyle);
                    col++;

                    // Job name
                    cell = ExcelUtil.getCell(currentRow, col);
                    cell.setCellValue(p_job.getJobName());
                    cell.setCellStyle(contentStyle);
                    col++;

                    // Segment id
                    cell = ExcelUtil.getCell(currentRow, col);
                    cell.setCellValue(sourceTuv.getTu(jobId).getId());
                    cell.setCellStyle(contentStyle);
                    col++;

                    // Page name
                    String externalPageId = sourcePage.getExternalPageId();
                    String[] pathNames = externalPageId.split("\\\\");
                    String Name = pathNames[pathNames.length - 1];
                    boolean temp = pathNames[0].contains(")");
                    if (temp)
                    {
                        String[] firstNames = pathNames[0].split("\\)");
                        String detailName = firstNames[0];
                        Name = Name + detailName + ")";
                    }

                    cell = ExcelUtil.getCell(currentRow, col);
                    cell.setCellValue(Name);
                    cell.setCellStyle(contentStyle);
                    col++;

                    // TargetPage id
                    cell = ExcelUtil.getCell(currentRow, col);
                    cell.setCellValue(targetPage.getId());
                    cell.setCellStyle(contentStyle);
                    col++;

                    // Source segment
                    CellStyle srcStyle = rtlSourceLocale ? REPORT_STYLE.getRtlContentStyle()
                            : contentStyle;
                    String srcContent = rtlSourceLocale ? EditUtil.toRtlString(sourceSegmentString)
                            : sourceSegmentString;
                    cell = ExcelUtil.getCell(currentRow, col);
                    cell.setCellValue(srcContent);
                    cell.setCellStyle(srcStyle);
                    col++;

                    // Target segment
                    CellStyle trgStyle = rtlTargetLocale ? REPORT_STYLE.getRtlContentStyle()
                            : contentStyle;
                    String content = rtlTargetLocale ? EditUtil.toRtlString(targetSegmentString)
                            : targetSegmentString;
                    cell = ExcelUtil.getCell(currentRow, col);
                    cell.setCellValue(content);
                    cell.setCellStyle(trgStyle);
                    col++;

                    // Sid
                    cell = ExcelUtil.getCell(currentRow, col);
                    cell.setCellValue(sid);
                    cell.setCellStyle(contentStyle);
                    col++;

                    // Character count
                    cell = ExcelUtil.getCell(currentRow, col);
                    cell.setCellValue(targetSegmentString.length());
                    cell.setCellStyle(contentStyle);
                    col++;

                    // Comments
                    CellStyle commentStyle = rtlTargetLocale ? REPORT_STYLE.getUnlockedRightStyle()
                            : REPORT_STYLE.getUnlockedStyle();
                    String commentContent = rtlTargetLocale
                            ? EditUtil.toRtlString(reviewerComments.toString()) : reviewerComments.toString();
                    cell = ExcelUtil.getCell(currentRow, col);
                    cell.setCellValue(commentContent);
                    cell.setCellStyle(commentStyle);
                    col++;

                    // Translator Comments
//                    commentStyle = rtlTargetLocale ? REPORT_STYLE.getUnlockedRightStyle()
//                            : REPORT_STYLE.getUnlockedStyle();
//                    commentContent = rtlTargetLocale
//                            ? EditUtil.toRtlString(comments.toString()) : comments.toString();
//                    cell = ExcelUtil.getCell(currentRow, col);
//                    cell.setCellValue(commentContent);
//                    cell.setCellStyle(commentStyle);
//                    col++;

                    // Category failure
                    cell = ExcelUtil.getCell(currentRow, col);
                    cell.setCellValue(failure);
                    cell.setCellStyle(unlockedStyle);
                    col++;

                    // Severity
                    if (isDQFEnabled)
                    {
                        // Severity
                        cell = ExcelUtil.getCell(currentRow, col);
                        if (!toShowSeverity)
                            severity = "---";
                        cell.setCellValue(severity);
                        cell.setCellStyle(REPORT_STYLE.getLockedStyle());
                        col++;
                    }

                    // Comment Status
                    cell = ExcelUtil.getCell(currentRow, col);
                    cell.setCellValue(commentStatus);
                    cell.setCellStyle(unlockedStyle);
                    col++;

                    // TM match
                    cell = ExcelUtil.getCell(currentRow, col);
                    cell.setCellValue(matches.toString());
                    cell.setCellStyle(contentStyle);
                    col++;

                    // Glossary source
                    cell = ExcelUtil.getCell(currentRow, col);
                    cell.setCellValue(sourceTerms);
                    cell.setCellStyle(contentStyle);
                    col++;

                    // Glossary target
                    cell = ExcelUtil.getCell(currentRow, col);
                    cell.setCellValue(targetTerms);
                    cell.setCellStyle(contentStyle);
                    col++;

                    // Previous segment
                    String previousSegments = getPreviousSegments(allTuvMap, targetTuv.getId(),
                            targetSegmentString, jobId);
                    cell = ExcelUtil.getCell(currentRow, col);
                    cell.setCellValue(previousSegments);
                    cell.setCellStyle(contentStyle);

                    p_row++;
                }

                SegmentTuTuvCacheManager.clearCache();
                sourceTuvs = null;
                targetTuvs = null;
                fuzzyLeverageMatcheMap = null;
                issuesMap = null;
                allTuvMap = null;
            }
            // Add category failure drop down list here.
            addCategoryFailureValidation(p_sheet, SEGMENT_START_ROW, p_row - 1, CATEGORY_FAILURE_COLUMN,
                    CATEGORY_FAILURE_COLUMN);

            termLeverageMatchResultMap = null;

            List<String> categories = null;
            String currentCompanyId = CompanyWrapper.getCurrentCompanyId();

            ExcelUtil.addValidation(p_sheet, "FailureCategoriesValidator", SEGMENT_START_ROW, p_row - 1,
                    CATEGORY_FAILURE_COLUMN, CATEGORY_FAILURE_COLUMN);
            if (isDQFEnabled)
            {
                ExcelUtil.addValidation(p_sheet, "SeverityCategoriesValidator", SEGMENT_START_ROW, p_row - 1,
                        SEVERITY_COLUMN, SEVERITY_COLUMN);

//                categories = CompanyWrapper.getCompanyCategoryNames(bundle, currentCompanyId,
//                        CategoryType.Fluency, true);
//                ExcelUtil.createValidatorList(p_sheet, categories, DQF_START_ROW, DQF_START_ROW, 1);
//
//                categories = CompanyWrapper.getCompanyCategoryNames(bundle, currentCompanyId,
//                        CategoryType.Adequacy, true);
//                ExcelUtil.createValidatorList(p_sheet, categories, DQF_START_ROW + 1,
//                        DQF_START_ROW + 1, 1);
            }

//            if (SCORECARD_START_ROW > 0)
//            {
//                String[] data = new String[]
//                        { "5", "4", "3", "2", "1" };
//                ExcelUtil.createValidatorList(p_sheet, data, SCORECARD_START_ROW + 1,
//                        SCORECARD_START_ROW + scorecardCategories.size(), 1);
//            }
        }

        return p_row;
    }

    private void addCriteriaSheet(Workbook p_workbook, List<Job> p_jobsList, Set<String> stateSet,
            Set<String> projectSet) throws Exception
    {
        Sheet sheet = p_workbook.createSheet(bundle.getString("lb_criteria"));
        StringBuffer temp = new StringBuffer();
        int row = 0;
        int headerColumn = 1;
        int valueColumn = 2;

        Row targetLocalesRow = ExcelUtil.getRow(sheet, row++);
        Row statusRow = ExcelUtil.getRow(sheet, row++);
        Row projectRow = ExcelUtil.getRow(sheet, row++);
        Row dateRangeRow = ExcelUtil.getRow(sheet, row++);
        Row jobsRow = ExcelUtil.getRow(sheet, row);

        CellStyle contentStyle = REPORT_STYLE.getContentStyle();

        Cell cell_TargetLocales = ExcelUtil.getCell(targetLocalesRow, headerColumn);
        cell_TargetLocales.setCellValue(bundle.getString("lb_report_target_locales"));
        cell_TargetLocales.setCellStyle(contentStyle);
        sheet.setColumnWidth(headerColumn, 20 * 256);

        Cell cell_Status = ExcelUtil.getCell(statusRow, headerColumn);
        cell_Status.setCellValue(bundle.getString("job_status"));
        cell_Status.setCellStyle(contentStyle);

        Cell cell_Project = ExcelUtil.getCell(projectRow, headerColumn);
        cell_Project.setCellValue(bundle.getString("lb_report_project"));
        cell_Project.setCellStyle(contentStyle);

        Cell cell_DateRange = ExcelUtil.getCell(dateRangeRow, headerColumn);
        cell_DateRange.setCellValue(bundle.getString("lb_report_date_range"));
        cell_DateRange.setCellStyle(contentStyle);

        Cell cell_Jobs = ExcelUtil.getCell(jobsRow, headerColumn);
        cell_Jobs.setCellValue(bundle.getString("lb_jobs"));
        cell_Jobs.setCellStyle(contentStyle);

        // locale
        Set<String> localeSet = rowsMap.keySet();
        for (String locale : localeSet)
        {
            temp.append(locale + "\n");
        }
        Cell cell_TargetLocalesValue = ExcelUtil.getCell(targetLocalesRow, valueColumn);
        cell_TargetLocalesValue.setCellValue(temp.substring(0, temp.length() - 1));
        cell_TargetLocalesValue.setCellStyle(contentStyle);
        sheet.setColumnWidth(valueColumn, 45 * 256);

        // status
        temp.setLength(0);
        for (String status : stateSet)
        {
            temp.append(status + "\n");
        }
        Cell cell_StatusValue = ExcelUtil.getCell(statusRow, valueColumn);
        cell_StatusValue.setCellValue(temp.substring(0, temp.length() - 1));
        cell_StatusValue.setCellStyle(contentStyle);

        // project
        temp.setLength(0);
        for (String status : projectSet)
        {
            temp.append(status + "\n");
        }
        Cell cell_ProjectValue = ExcelUtil.getCell(projectRow, valueColumn);
        cell_ProjectValue.setCellValue(temp.substring(0, temp.length() - 1));
        cell_ProjectValue.setCellStyle(contentStyle);

        // date range
        temp.setLength(0);
        String startCount = request.getParameter(JobSearchConstants.CREATION_START);
        if (startCount != null && startCount != "")
        {
            temp.append("Starts:" + startCount + "\n");
        }

        String endCount = request.getParameter(JobSearchConstants.CREATION_END);

        if (endCount != null && endCount != "")
        {
            temp.append("Ends:" + endCount);
        }
        Cell cell_DateRangeValue = ExcelUtil.getCell(dateRangeRow, valueColumn);
        cell_DateRangeValue.setCellValue(temp.toString());
        cell_DateRangeValue.setCellStyle(contentStyle);

        // jobs
        int jobRowNum = 4;
        for (Job job : p_jobsList)
        {
            Cell cell_JobValue = ExcelUtil.getCell(ExcelUtil.getRow(sheet, jobRowNum++), valueColumn);
            cell_JobValue.setCellValue(job.getJobId() + "," + job.getJobName());
            cell_JobValue.setCellStyle(contentStyle);
        }
    }

    private String translateOpts(String opt)
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

    private List<String> getFailureCategoriesList()
    {
        List<String> result = new ArrayList<String>();

        String currentCompanyId = CompanyThreadLocal.getInstance().getValue();
        List failureCategories = IssueOptions.getAllCategories(bundle, currentCompanyId);
        for (int i = 0; i < failureCategories.size(); i++)
        {
            Select aCategory = (Select) failureCategories.get(i);
            result.add(aCategory.getValue());
        }
        return result;
    }

    private void setAllCellStyleNull()
    {
    }

    /**
     * Create workbook name areas for category failure drop down list, it is
     * from "AA8" to "AAn".
     * <P>
     * Only write the data of drop down list into the first sheet as it can be
     * referenced from all sheets.
     * </P>
     * <P>
     * The formula is like
     * "[sheetName]!$AA$[startRow]:$AA$[endRow]",i.e."TER!$AA$8:$AA$32".
     * </P>
     */
    private void createCategoryFailureNameArea(Workbook p_workbook)
    {
        try
        {
            Sheet firstSheet = ExcelUtil.getSheet(p_workbook, 0);
            List<String> categories = getFailureCategoriesList();
            // Set the categories in "AA" column, starts with row 8.
            int col = 26;
            for (int i = 0; i < categories.size(); i++)
            {
                Row row = ExcelUtil.getRow(firstSheet, SEGMENT_START_ROW + i);
                Cell cell = ExcelUtil.getCell(row, col);
                cell.setCellValue(categories.get(i));
            }

            String formula = firstSheet.getSheetName() + "!$AA$" + (SEGMENT_START_ROW + 1) + ":$AA$"
                    + (SEGMENT_START_ROW + categories.size());
            Name name = p_workbook.createName();
            name.setRefersToFormula(formula);
            name.setNameName(CATEGORY_FAILURE_DROP_DOWN_LIST);

            // Hide "AA" column
            firstSheet.setColumnHidden(26, true);
        }
        catch (Exception e)
        {
            logger.error("Error when create hidden area for category failures.", e);
        }
    }

    /**
     * Add category failure drop down list. It is from "J8" to "Jn".
     * 
     * @param p_sheet
     * @param startRow
     * @param lastRow
     * @param startColumn
     * @param lastColumn
     */
    private void addCategoryFailureValidation(Sheet p_sheet, int startRow, int lastRow,
            int startColumn, int lastColumn)
    {
        // Add category failure drop down list here.
        DataValidationHelper dvHelper = p_sheet.getDataValidationHelper();
        DataValidationConstraint dvConstraint = dvHelper
                .createFormulaListConstraint(CATEGORY_FAILURE_DROP_DOWN_LIST);
        CellRangeAddressList addressList = new CellRangeAddressList(startRow, lastRow, startColumn,
                lastColumn);
        DataValidation validation = dvHelper.createValidation(dvConstraint, addressList);
        validation.setSuppressDropDownArrow(true);
        validation.setShowErrorBox(true);
        p_sheet.addValidationData(validation);
    }

    @Override
    public String getReportType()
    {
        return reportType;
    }

    @Override
    public void setPercent(int p_finishedJobNum)
    {
        ReportGeneratorHandler.setReportsMapByGenerator(m_userId, m_jobIDS,
                100 * p_finishedJobNum / m_jobIDS.size(), getReportType());
    }

    @Override
    public boolean isCancelled()
    {
        ReportsData data = ReportGeneratorHandler.getReportsMap(m_userId, m_jobIDS,
                getReportType());
        if (data != null)
            return data.isCancle();

        return false;
    }

    private Map<Long, Tuv> getAllTuvsMap(TargetPage targetPage)
    {
        Map<Long, Tuv> result = new HashMap<Long, Tuv>();
        try
        {
            List<Tuv> tuvs = SegmentTuvUtil.getAllTargetTuvs(targetPage);
            for (Tuv tuv : tuvs)
            {
                result.put(tuv.getIdAsLong(), tuv);
            }
        }
        catch (Exception e)
        {

        }

        return result;
    }

    @SuppressWarnings("unchecked")
    private String getPreviousSegments(Map<Long, Tuv> allTargetTuvsMap, long p_trgTuvId,
            String targetSegmentString, long p_jobId) throws TuvException, RemoteException
    {
        List previousTaskTuvs = new ArrayList();
        TuvManager tuvManager = ServerProxy.getTuvManager();
        previousTaskTuvs = tuvManager.getPreviousTaskTuvs(p_trgTuvId, 10);
        String previousSegments = "";
        String previousSegment = null;
        List previous = new ArrayList();
        if (previousTaskTuvs.size() > 0)
        {
            SortUtil.sort(previousTaskTuvs, new Comparator()
            {
                public int compare(Object p_taskTuvA, Object p_taskTuvB)
                {
                    TaskTuv a = (TaskTuv) p_taskTuvA;
                    TaskTuv b = (TaskTuv) p_taskTuvB;
                    return a.getTask().getCompletedDate().compareTo(b.getTask().getCompletedDate());
                }
            });

            int len = previousTaskTuvs.size() - 2;
            int lastReviewerCount = 0;

            while ((len >= 0) && (((TaskTuv) previousTaskTuvs.get(len)).getTask()
                    .getType() == Task.TYPE_REVIEW))
            {
                // Review only task does not change segments
                lastReviewerCount++;
                len--;
            }

            int beforeLastReviewerCount = previousTaskTuvs.size() - lastReviewerCount - 1;

            if (beforeLastReviewerCount == 1)
            {
                String previousSeg = "";
                TaskTuv taskTuv = (TaskTuv) previousTaskTuvs.get(0);
                Tuv previousTuv = allTargetTuvsMap.get(taskTuv.getPreviousTuvId());
                // generally it should not be null for performance
                if (previousTuv != null)
                {
                    previousSeg = previousTuv.getGxmlElement().getTextValue();
                }
                else
                {
                    previousSeg = taskTuv.getTuv(p_jobId).getGxmlElement().getTextValue();
                }
                if (!previousSeg.equals(targetSegmentString))
                {
                    previousSegments = previousSegments + previousSeg;
                }
            }
            else
            {
                for (int k = 0; k <= beforeLastReviewerCount; k++)
                {
                    TaskTuv taskTuv = (TaskTuv) previousTaskTuvs.get(k);
                    Tuv previousTuv = allTargetTuvsMap.get(taskTuv.getPreviousTuvId());
                    // generally it should not be null for performance
                    if (previousTuv != null)
                    {
                        previousSegment = previousTuv.getGxmlElement().getTextValue();
                    }
                    else
                    {
                        previousSegment = taskTuv.getTuv(p_jobId).getGxmlElement().getTextValue();
                    }
                    if (!previous.contains(previousSegment))
                    {
                        previous.add(previousSegment);
                    }
                }
                String lastInPrevious = (String) previous.get(previous.size() - 1);
                if (lastInPrevious.equals(targetSegmentString))
                {
                    previous.remove(previous.size() - 1);
                }

                // GBS-4638, TripAdvisor needs to only show previous segment in first translation activity
                int count = previous.size() > 1 ? 1 : 0;
                for (int pi = 0; pi < count; pi++)
                {
                    previousSegment = (String) previous.get(pi);
                    if (count > 1)
                    {
                        previousSegments = previousSegments + "{" + previousSegment + "}\r\n";
                    }
                    else
                    {
                        previousSegments = previousSegments + previousSegment;
                    }
                }
            }
        }
        return previousSegments;
    }

    /**
     * Create Report File.
     */
    protected File getFile(String p_reportType, Job p_job, Workbook p_workBook)
    {
        String langInfo = null;
        // If the Workbook has only one sheet, the report name should contain
        // language pair info, such as en_US_de_DE.
        if (p_workBook != null && p_workBook.getNumberOfSheets() == 1)
        {
            Sheet sheet = p_workBook.getSheetAt(0);
            String srcLang = null, trgLang = null;
            if (p_job != null)
            {
                srcLang = p_job.getSourceLocale().toString();
            }
            if (srcLang == null)
            {
                Row languageInfoRow = sheet.getRow(LANGUAGE_INFO_ROW);
                if (languageInfoRow != null)
                {
                    srcLang = languageInfoRow.getCell(0).getStringCellValue();
                    srcLang = srcLang.substring(srcLang.indexOf("[") + 1, srcLang.indexOf("]"));
                    trgLang = languageInfoRow.getCell(1).getStringCellValue();
                    trgLang = trgLang.substring(trgLang.indexOf("[") + 1, trgLang.indexOf("]"));
                }
                else
                {
                    Row dataRow = sheet.getRow(sheet.getLastRowNum());
                    if (dataRow != null)
                    {
                        try
                        {
                            long jobId = (long) dataRow.getCell(0).getNumericCellValue();
                            Job job = ServerProxy.getJobHandler().getJobById(jobId);
                            srcLang = job.getSourceLocale().toString();
                        }
                        catch (Exception e)
                        {
                        }

                    }
                }
            }
            if (trgLang == null)
            {
                trgLang = sheet.getSheetName();
            }
            if (srcLang != null && trgLang != null)
            {
                langInfo = srcLang + "_" + trgLang;
            }
        }

        return ReportHelper.getReportFile(p_reportType, p_job, ReportConstants.EXTENSION_XLSX,
                langInfo);
    }
}
