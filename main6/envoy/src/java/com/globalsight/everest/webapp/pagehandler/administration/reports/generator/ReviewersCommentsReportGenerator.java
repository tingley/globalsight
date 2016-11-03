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

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import com.globalsight.everest.category.CategoryType;
import com.globalsight.everest.comment.Issue;
import com.globalsight.everest.comment.IssueHistory;
import com.globalsight.everest.comment.IssueImpl;
import com.globalsight.everest.comment.IssueOptions;
import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.edit.CommentHelper;
import com.globalsight.everest.integration.ling.LingServerProxy;
import com.globalsight.everest.integration.ling.tm2.MatchTypeStatistics;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.page.SourcePage;
import com.globalsight.everest.page.TargetPage;
import com.globalsight.everest.persistence.tuv.SegmentTuUtil;
import com.globalsight.everest.persistence.tuv.SegmentTuvUtil;
import com.globalsight.everest.projecthandler.TranslationMemoryProfile;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.taskmanager.Task;
import com.globalsight.everest.tuv.Tuv;
import com.globalsight.everest.util.comparator.GlobalSightLocaleComparator;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.administration.company.Select;
import com.globalsight.everest.webapp.pagehandler.administration.reports.ReportConstants;
import com.globalsight.everest.webapp.pagehandler.administration.reports.ReportHelper;
import com.globalsight.everest.webapp.pagehandler.administration.reports.bo.ReportsData;
import com.globalsight.everest.webapp.pagehandler.administration.users.UserUtil;
import com.globalsight.everest.webapp.pagehandler.edit.online.OnlineTagHelper;
import com.globalsight.everest.workflow.ScorecardScore;
import com.globalsight.everest.workflow.ScorecardScoreHelper;
import com.globalsight.everest.workflowmanager.Workflow;
import com.globalsight.ling.tm.LeverageMatchLingManager;
import com.globalsight.ling.tw.PseudoConstants;
import com.globalsight.ling.tw.PseudoData;
import com.globalsight.ling.tw.TmxPseudo;
import com.globalsight.terminology.ITermbase;
import com.globalsight.terminology.ITermbaseManager;
import com.globalsight.terminology.termleverager.TermLeverageManager;
import com.globalsight.terminology.termleverager.TermLeverageMatch;
import com.globalsight.terminology.termleverager.TermLeverageOptions;
import com.globalsight.util.ExcelUtil;
import com.globalsight.util.GeneralException;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.ReportStyle;
import com.globalsight.util.StringUtil;
import com.globalsight.util.edit.EditUtil;
import com.globalsight.util.edit.GxmlUtil;
import com.globalsight.util.gxml.GxmlElement;
import com.globalsight.util.gxml.GxmlNames;
import com.globalsight.util.resourcebundle.ResourceBundleConstants;
import com.globalsight.util.resourcebundle.SystemResourceBundle;

/**
 * Reviewers Comments Report Generator, Include Reviewers Comments Report in
 * offline download report.
 */
public class ReviewersCommentsReportGenerator implements ReportGenerator, Cancelable
{
    static private final Logger logger = Logger.getLogger(ReviewersCommentsReportGenerator.class);

    private boolean isIncludeCompactTags = false;

    protected String m_companyName = "";
    protected long companyId = -1L;
    protected List<Long> m_jobIDS = new ArrayList<Long>();
    protected List<GlobalSightLocale> m_targetLocales = new ArrayList<GlobalSightLocale>();

    // Is Run setPercent/isCancelled function or not.
    private boolean m_isCalculatePercent;

    public final int LANGUAGE_HEADER_ROW = 3;
    public final int LANGUAGE_INFO_ROW = 4;
    public int SEGMENT_HEADER_ROW = 6;
    public int SEGMENT_START_ROW = 7;
    public int SCORECARD_START_ROW = 0;
    public int DQF_START_ROW = 0;
    
    // "E" column, index 4
    public final int CATEGORY_FAILURE_COLUMN = 4;
    // "F" column, index 5
    public final int COMMENT_STATUS_COLUMN = 6;
    
    public final int SEVERITY_COLUMN = 5;

    private Locale m_uiLocale = Locale.US;
    String m_userId;
    private ResourceBundle m_bundle;
    private String m_dateFormat;
    private boolean cancel = false;
    
    private boolean isDQFEnabled = false;
    private boolean isScorecradEnabled = false;
    private String fluencyScore = "";
    private String adequacyScore = "";
    private String dqfComment = "";
    private List<ScorecardScore> scores = null;
    private List<String> scorecardCategories = null;
    private String scoreComment = "";
    private boolean needProtect = false;

    private ReportStyle REPORT_STYLE = null;

    public ReviewersCommentsReportGenerator(String p_cureentCompanyName)
    {
        m_uiLocale = Locale.US;
        m_companyName = p_cureentCompanyName;
        CompanyThreadLocal.getInstance().setValue(m_companyName);
        m_bundle = SystemResourceBundle.getInstance()
                .getResourceBundle(ResourceBundleConstants.LOCALE_RESOURCE_NAME, m_uiLocale);
        m_dateFormat = DEFAULT_DATE_FORMAT;

        m_isCalculatePercent = false;
    }

    public ReviewersCommentsReportGenerator(String p_cureentCompanyName,
            boolean p_includeCompactTags, String p_userId)
    {
        m_uiLocale = Locale.US;
        m_companyName = p_cureentCompanyName;
        CompanyThreadLocal.getInstance().setValue(m_companyName);
        m_bundle = SystemResourceBundle.getInstance()
                .getResourceBundle(ResourceBundleConstants.LOCALE_RESOURCE_NAME, m_uiLocale);
        m_dateFormat = DEFAULT_DATE_FORMAT;

        m_isCalculatePercent = false;

        isIncludeCompactTags = p_includeCompactTags;
        m_userId = p_userId;
    }

    /**
     * Constructor. Create a helper to generate the report
     * 
     * @param p_request
     *            the request
     * @param p_response
     *            the response
     * @throws Exception
     */
    public ReviewersCommentsReportGenerator(HttpServletRequest p_request,
            HttpServletResponse p_response) throws Exception
    {
        HttpSession session = p_request.getSession();
        m_userId = (String) session.getAttribute(WebAppConstants.USER_NAME);
        if (p_request.getSession().getAttribute(WebAppConstants.UILOCALE) != null)
        {
            m_uiLocale = (Locale) p_request.getSession().getAttribute(WebAppConstants.UILOCALE);
        }
        m_bundle = SystemResourceBundle.getInstance()
                .getResourceBundle(ResourceBundleConstants.LOCALE_RESOURCE_NAME, m_uiLocale);
        m_dateFormat = p_request.getParameter(WebAppConstants.DATE_FORMAT);
        if (m_dateFormat == null)
        {
            m_dateFormat = DEFAULT_DATE_FORMAT;
        }

        m_jobIDS = ReportHelper.getListOfLong(p_request.getParameter(ReportConstants.JOB_IDS));
        GlobalSightLocaleComparator comparator = new GlobalSightLocaleComparator(
                GlobalSightLocaleComparator.ISO_CODE, m_uiLocale);
        m_targetLocales = ReportHelper.getTargetLocaleList(
                p_request.getParameterValues(ReportConstants.TARGETLOCALE_LIST), comparator);

        m_companyName = UserUtil.getCurrentCompanyName(p_request);
        if (CompanyWrapper.isSuperCompanyName(m_companyName) && m_jobIDS != null
                && m_jobIDS.size() > 0)
        {
            Job job = ServerProxy.getJobHandler().getJobById(m_jobIDS.get(0));
            m_companyName = CompanyWrapper.getCompanyNameById(job.getCompanyId());
        }
        CompanyThreadLocal.getInstance().setValue(m_companyName);

        m_isCalculatePercent = true;

        String withCompactTags = p_request.getParameter("withCompactTags");
        if ("on".equals(withCompactTags))
        {
            isIncludeCompactTags = true;
        }
        else
        {
            isIncludeCompactTags = false;
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
     * Create Reports for download
     * 
     * @param p_jobIDS
     *            Job ID List
     * @param p_targetLocales
     *            Target Locales List
     */
    public File[] generateReports(List<Long> p_jobIDS, List<GlobalSightLocale> p_targetLocales)
            throws Exception
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
        List<File> workBooks = new ArrayList<File>();
        int finishedJobNum = 0;
        for (long jobID : m_jobIDS)
        {
            // Cancel generate reports.
            if (isCancelled())
                return null;

            if (cancel)
                return null;

            // Sets Reports Percent.
            setPercent(++finishedJobNum);

            Job job = ServerProxy.getJobHandler().getJobById(jobID);

            if (job == null)
                continue;

            if (m_userId == null)
            {
                m_userId = job.getCreateUserId();
            }

            Workbook workBook = new SXSSFWorkbook();
            REPORT_STYLE = new ReportStyle(workBook);

            createReport(workBook, job, p_targetLocales, m_dateFormat);
            File file = getFile(getReportType(), job, workBook);
            FileOutputStream out = new FileOutputStream(file);
            workBook.write(out);
            out.close();
            ((SXSSFWorkbook) workBook).dispose();

            workBooks.add(file);
        }

        return ReportHelper.moveReports(workBooks, m_userId);
    }

    /**
     * Create the report
     * 
     * @throws Exception
     */
    private void createReport(Workbook p_workbook, Job p_job,
            List<GlobalSightLocale> p_targetLocales, String p_dateFormat) throws Exception
    {
        boolean categoryFailureDropDownAdded = false;
        List<GlobalSightLocale> jobTL = ReportHelper.getTargetLocals(p_job);
        for (GlobalSightLocale trgLocale : p_targetLocales)
        {
            fluencyScore = "";
            adequacyScore = "";
            dqfComment = "";
            scores = null;
            scoreComment = "";
            isDQFEnabled = false;
            isScorecradEnabled = false;
            needProtect = false;
            
            DQF_START_ROW = 0;
            SCORECARD_START_ROW = 0;
            SEGMENT_HEADER_ROW = 6;
            SEGMENT_START_ROW = 7;

            if (!jobTL.contains(trgLocale))
                continue;

            if (cancel)
                return;

            // Create Report Template
            Sheet sheet = p_workbook.createSheet(trgLocale.toString());
            sheet.protectSheet("");

            // Add Title
            addTitle(p_workbook, sheet);

            // Add hidden info "RCR_taskID" for uploading.
            addHidenInfoForUpload(p_workbook, sheet, p_job, trgLocale);

            // Add Locale Pair Header
            addLanguageHeader(p_workbook, sheet);

            // Add DQF and Scorecard info
            addDQFHeader(p_workbook, sheet);
            
            // Add Segment Header
            addSegmentHeader(p_workbook, sheet);

            // Create Name Areas for drop down list.
            // if (!categoryFailureDropDownAdded)
            // {
            // createCategoryFailureNameArea(p_workbook);
            // createSeverityNameArea(p_workbook);
            // createFluencyNameArea(p_workbook);
            // createAdequacyNameArea(p_workbook);
            // createScorecardNameArea(p_workbook);
            // categoryFailureDropDownAdded = true;
            // }

            // Insert Language Data
            String srcLang = p_job.getSourceLocale().getDisplayName(m_uiLocale);
            String trgLang = trgLocale.getDisplayName(m_uiLocale);
            writeLanguageInfo(p_workbook, sheet, srcLang, trgLang);

            // Insert Segment Data
            writeSegmentInfo(p_workbook, sheet, p_job, trgLocale, SEGMENT_START_ROW);
        }
    }
    
    private void addDQFHeader(Workbook workbook, Sheet sheet) throws Exception {
        int col = 0;
        int row = LANGUAGE_HEADER_ROW;
        Row rowLine = null;
        Cell cell = null;
        boolean isStored = false;
        
        if (isDQFEnabled) {
            isStored = StringUtil.isNotEmpty(dqfComment);
            
            // DQF enabled
            row = DQF_START_ROW;
            rowLine = ExcelUtil.getRow(sheet, row);
            cell = ExcelUtil.getCell(rowLine, col);
            cell.setCellValue(m_bundle.getString("lb_dqf_fluency_only"));
            cell.setCellStyle(REPORT_STYLE.getHeaderStyle());
            
            cell = ExcelUtil.getCell(rowLine, 1);
            if (isStored || needProtect)
            {
                cell.setCellStyle(REPORT_STYLE.getLockedStyle());
            }
            else
            {
                cell.setCellStyle(REPORT_STYLE.getUnlockedStyle());
            }
            cell.setCellValue(fluencyScore);
           
            row++;

            rowLine = ExcelUtil.getRow(sheet, row);
            cell = ExcelUtil.getCell(rowLine, col);
            cell.setCellValue(m_bundle.getString("lb_dqf_adequacy_only"));
            cell.setCellStyle(REPORT_STYLE.getHeaderStyle());
            cell = ExcelUtil.getCell(rowLine, 1);
            if (isStored || needProtect)
            {
                cell.setCellStyle(REPORT_STYLE.getLockedStyle());
            }
            else
            {
                cell.setCellStyle(REPORT_STYLE.getUnlockedStyle());
            }
            cell.setCellValue(adequacyScore);
            row++;
            
            rowLine = ExcelUtil.getRow(sheet, row);
            cell = ExcelUtil.getCell(rowLine, col);
            cell.setCellValue(m_bundle.getString("lb_comment"));
            cell.setCellStyle(REPORT_STYLE.getHeaderStyle());
            cell = ExcelUtil.getCell(rowLine, 1);
            if (isStored || needProtect)
            {
                cell.setCellStyle(REPORT_STYLE.getLockedStyle());
            }
            else
            {
                cell.setCellStyle(REPORT_STYLE.getUnlockedStyle());
            }
            cell.setCellValue(dqfComment);
        }
        if (isScorecradEnabled) {
            isStored = StringUtil.isNotEmpty(scoreComment);
            
            // Scorecard enabled
            row = SCORECARD_START_ROW;
            rowLine = ExcelUtil.getRow(sheet, row);
            cell = ExcelUtil.getCell(rowLine, col);
            cell.setCellValue(m_bundle.getString("lb_scorecard"));
            cell.setCellStyle(REPORT_STYLE.getHeaderStyle());
            row++;
            Hashtable<String, Integer> elements = new Hashtable<String, Integer>();
            if (scorecardCategories != null && scorecardCategories.size()>0) {
                for (String scorecard : scorecardCategories) {
                    rowLine = ExcelUtil.getRow(sheet, row);
                    cell = ExcelUtil.getCell(rowLine, col);
                    cell.setCellValue(scorecard);
                    
                    cell = ExcelUtil.getCell(rowLine, 1);
                    if (needProtect)
                        cell.setCellStyle(REPORT_STYLE.getLockedStyle());
                    else
                        cell.setCellStyle(REPORT_STYLE.getUnlockedStyle());

                    elements.put(scorecard, Integer.valueOf(row));
                    row++;
                }
                String key = "";
                Integer rowValue = 0;
                for (ScorecardScore score : scores)
                {
                    key = score.getScorecardCategory();
                    rowValue = elements.get(key);
                    rowLine = ExcelUtil.getRow(sheet, rowValue);
                    cell = ExcelUtil.getCell(rowLine, 1);
                    if (isStored)
                        cell.setCellStyle(REPORT_STYLE.getLockedStyle());
                    else
                        cell.setCellStyle(REPORT_STYLE.getUnlockedStyle());
                    cell.setCellValue(score.getScore());
                }
            }

            rowLine = ExcelUtil.getRow(sheet, row);
            cell = ExcelUtil.getCell(rowLine, col);
            cell.setCellValue(m_bundle.getString("lb_comment"));
            cell.setCellStyle(REPORT_STYLE.getHeaderStyle());
            cell = ExcelUtil.getCell(rowLine, 1);
            if (isStored || needProtect)
            {
                cell.setCellStyle(REPORT_STYLE.getLockedStyle());
            }
            else
            {
                cell.setCellStyle(REPORT_STYLE.getUnlockedStyle());
            }
            cell.setCellValue(scoreComment);
        }
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
        titleCell.setCellValue(m_bundle.getString("review_reviewers_comments"));
    }

    /**
     * Add hidden info "RCR_taskID" for offline uploading. When upload, system
     * can know the report type and current task ID report generated from.
     * 
     * @param p_workbook
     * @param p_sheet
     * @param p_job
     * @param p_targetLocale
     * @throws Exception
     */
    private void addHidenInfoForUpload(Workbook p_workbook, Sheet p_sheet, Job p_job,
            GlobalSightLocale p_targetLocale) throws Exception
    {
        String reportInfo = "";
        for (Workflow wf : p_job.getWorkflows())
        {
            if (p_targetLocale.getId() == wf.getTargetLocale().getId())
            {
                Collection tasks = ServerProxy.getTaskManager().getCurrentTasks(wf.getId());
                if (tasks != null)
                {
                    for (Iterator it = tasks.iterator(); it.hasNext();)
                    {
                        Task task = (Task) it.next();
                        reportInfo = ReportConstants.REVIEWERS_COMMENTS_REPORT_ABBREVIATION + "_"
                                + task.getId();
                        needProtect = !(task.isType(Task.TYPE_REVIEW) || task.isType(Task.TYPE_REVIEW_EDITABLE));
                    }
                }
                int scoreShowType = wf.getScorecardShowType();
                if (scoreShowType > 1) {
                    isDQFEnabled = true;
                    fluencyScore = wf.getFluencyScore();
                    adequacyScore = wf.getAdequacyScore();
                    dqfComment = wf.getDQFComment();
                    DQF_START_ROW = 6;
                }
                if (scoreShowType > -1 && scoreShowType < 4) {
                    isScorecradEnabled = true;
                    scorecardCategories = ScorecardScoreHelper.getScorecardCategories(wf.getCompanyId(), m_bundle);
                    scores = ScorecardScoreHelper.getScoreByWrkflowId(wf.getId());
                    scoreComment = wf.getScorecardComment();
                    SCORECARD_START_ROW = isDQFEnabled ? 10 : 7;
                }
            }
        }
        if (isDQFEnabled)
        {
            // Only DQF enabled
            SEGMENT_HEADER_ROW = DQF_START_ROW + 4;
        }
        if (isScorecradEnabled)
        {
            // Scorecard enabled or both DQF and scorecard are enabled
            SEGMENT_HEADER_ROW = SCORECARD_START_ROW + scorecardCategories.size() + 3;
        }
        SEGMENT_START_ROW = SEGMENT_HEADER_ROW + 1;
        
        //add additional info for DQF and scorecard used to read data via uploading report
        reportInfo += "_" + DQF_START_ROW + "_" + SCORECARD_START_ROW + "_"
                + SEGMENT_START_ROW;

        Cell cell = ExcelUtil.getCell(p_sheet, 0, 26);
        cell.setCellValue(reportInfo);
        cell.setCellStyle(REPORT_STYLE.getContentStyle());

        p_sheet.setColumnHidden(26, true);
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
        cell.setCellValue(m_bundle.getString("lb_source_language"));
        cell.setCellStyle(REPORT_STYLE.getHeaderStyle());
        col++;

        cell = ExcelUtil.getCell(langRow, col);
        cell.setCellValue(m_bundle.getString("lb_target_language"));
        cell.setCellStyle(REPORT_STYLE.getHeaderStyle());
    }

    /**
     * Add segment header to the sheet for Reviewers Comments Report
     * 
     * @param p_sheet
     *            the sheet
     * @param p_row
     *            start row number
     * @throws Exception
     */
    private void addSegmentHeader(Workbook p_workBook, Sheet p_sheet) throws Exception
    {
        int col = 0;
        int row = SEGMENT_HEADER_ROW;
        Row segHeaderRow = ExcelUtil.getRow(p_sheet, row);
        CellStyle headerStyle = REPORT_STYLE.getHeaderStyle();

        Cell cell = ExcelUtil.getCell(segHeaderRow, col);
        cell.setCellValue(m_bundle.getString("lb_source_segment"));
        cell.setCellStyle(headerStyle);
        p_sheet.setColumnWidth(col, 40 * 256);
        col++;

        cell = ExcelUtil.getCell(segHeaderRow, col);
        cell.setCellValue(m_bundle.getString("lb_target_segment"));
        cell.setCellStyle(headerStyle);
        p_sheet.setColumnWidth(col, 40 * 256);
        col++;

        cell = ExcelUtil.getCell(segHeaderRow, col);
        cell.setCellValue(m_bundle.getString("latest_comments"));
        cell.setCellStyle(headerStyle);
        p_sheet.setColumnWidth(col, 40 * 256);
        col++;

        cell = ExcelUtil.getCell(segHeaderRow, col);
        cell.setCellValue(m_bundle.getString("reviewers_comments_header"));
        cell.setCellStyle(headerStyle);
        p_sheet.setColumnWidth(col, 40 * 256);
        col++;

        cell = ExcelUtil.getCell(segHeaderRow, col);
        cell.setCellValue(m_bundle.getString("lb_category_failure"));
        cell.setCellStyle(headerStyle);
        p_sheet.setColumnWidth(col, 40 * 256);
        col++;

        cell = ExcelUtil.getCell(segHeaderRow, col);
        cell.setCellValue(m_bundle.getString("lb_dqf_severity"));
        cell.setCellStyle(headerStyle);
        p_sheet.setColumnWidth(col, 15 * 256);
        col++;

        cell = ExcelUtil.getCell(segHeaderRow, col);
        cell.setCellValue(m_bundle.getString("lb_comment_status"));
        cell.setCellStyle(headerStyle);
        p_sheet.setColumnWidth(col, 15 * 256);
        col++;

        cell = ExcelUtil.getCell(segHeaderRow, col);
        cell.setCellValue(m_bundle.getString("lb_tm_match_original"));
        cell.setCellStyle(headerStyle);
        p_sheet.setColumnWidth(col, 20 * 256);
        col++;

        cell = ExcelUtil.getCell(segHeaderRow, col);
        cell.setCellValue(m_bundle.getString("lb_glossary_source"));
        cell.setCellStyle(headerStyle);
        p_sheet.setColumnWidth(col, 25 * 256);
        col++;

        cell = ExcelUtil.getCell(segHeaderRow, col);
        cell.setCellValue(m_bundle.getString("lb_glossary_target"));
        cell.setCellStyle(headerStyle);
        p_sheet.setColumnWidth(col, 25 * 256);
        col++;

        cell = ExcelUtil.getCell(segHeaderRow, col);
        cell.setCellValue(m_bundle.getString("lb_job_id_report"));
        cell.setCellStyle(headerStyle);
        p_sheet.setColumnWidth(col, 15 * 256);
        col++;

        cell = ExcelUtil.getCell(segHeaderRow, col);
        cell.setCellValue(m_bundle.getString("lb_segment_id"));
        cell.setCellStyle(headerStyle);
        p_sheet.setColumnWidth(col, 15 * 256);
        col++;

        cell = ExcelUtil.getCell(segHeaderRow, col);
        cell.setCellValue(m_bundle.getString("lb_page_name"));
        cell.setCellStyle(headerStyle);
        p_sheet.setColumnWidth(col, 25 * 256);
        col++;

        cell = ExcelUtil.getCell(segHeaderRow, col);
        cell.setCellValue(m_bundle.getString("lb_sid"));
        cell.setCellStyle(headerStyle);
        p_sheet.setColumnWidth(col, 15 * 256);
        col++;
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
     * For Reviewers Comments Report(Language Sign-off report), Write segment
     * information into each row of the sheet.
     * 
     * @param p_sheet
     *            the sheet
     * @param p_job
     *            the job data of report
     * @param p_targetLocale
     *            the target locale
     * @param p_row
     *            the segment row in sheet
     * @throws Exception
     */
    private int writeSegmentInfo(Workbook p_workBook, Sheet p_sheet, Job p_job,
            GlobalSightLocale p_targetLocale, int p_row) throws Exception
    {
        Collection<TargetPage> targetPages = null;
        TranslationMemoryProfile tmp = p_job.getL10nProfile().getTranslationMemoryProfile();
        Vector<String> excludItems = null;
        if (tmp != null)
        {
            excludItems = tmp.getJobExcludeTuTypes();
        }

        long jobId = p_job.getId();

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
                targetPages = workflow.getTargetPages();
                break;
            }
        }

        if (!targetPages.isEmpty())
        {
            LeverageMatchLingManager lmLingManager = LingServerProxy.getLeverageMatchLingManager();
            TermLeverageManager termLeverageManager = ServerProxy.getTermLeverageManager();

            Locale sourcePageLocale = p_job.getSourceLocale().getLocale();
            Locale targetPageLocale = p_targetLocale.getLocale();
            TermLeverageOptions termLeverageOptions = getTermLeverageOptions(sourcePageLocale,
                    targetPageLocale, p_job.getL10nProfile().getProject().getTermbaseName(),
                    String.valueOf(p_job.getCompanyId()));
            Map<Long, Set<TermLeverageMatch>> termLeverageMatchResultMap = null;
            if (termLeverageOptions != null)
            {
                termLeverageMatchResultMap = termLeverageManager.getTermMatchesForPages(
                        p_job.getSourcePages(), p_targetLocale);
            }

            String category = null;
            PseudoData pData = new PseudoData();
            pData.setMode(PseudoConstants.PSEUDO_COMPACT);
            String sid = null;
            for (TargetPage targetPage : targetPages)
            {
                if (cancel)
                    return 0;

                SourcePage sourcePage = targetPage.getSourcePage();

                SegmentTuUtil.getTusBySourcePageId(sourcePage.getId());
                List sourceTuvs = SegmentTuvUtil.getSourceTuvs(sourcePage);
                List targetTuvs = SegmentTuvUtil.getTargetTuvs(targetPage);

                MatchTypeStatistics tuvMatchTypes = lmLingManager.getMatchTypesForStatistics(
                        sourcePage.getIdAsLong(), targetPage.getLocaleId(),
                        p_job.getLeverageMatchThreshold());
                Map fuzzyLeverageMatchMap = lmLingManager.getFuzzyMatches(sourcePage.getIdAsLong(),
                        targetPage.getLocaleId());

                boolean m_rtlSourceLocale = EditUtil.isRTLLocale(sourcePageLocale.toString());
                boolean m_rtlTargetLocale = EditUtil.isRTLLocale(targetPageLocale.toString());

                // Find segment all comments belong to this target page
                Map<Long, IssueImpl> issuesMap = CommentHelper.getIssuesMap(targetPage.getId());
                CellStyle contentStyle = REPORT_STYLE.getContentStyle();
                CellStyle unlockedStyle = REPORT_STYLE.getUnlockedStyle();
                Cell cell = null;

                for (int j = 0; j < targetTuvs.size(); j++)
                {
                    if (cancel)
                        return 0;

                    int col = 0;
                    Tuv targetTuv = (Tuv) targetTuvs.get(j);
                    Tuv sourceTuv = (Tuv) sourceTuvs.get(j);
                    category = sourceTuv.getTu(jobId).getTuType();
                    if (excludItems != null && excludItems.contains(category))
                    {
                        continue;
                    }

                    // Comment
                    List issueHistories = null;
                    String lastComment = "";
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
                    if (issueHistories != null && issueHistories.size() > 0)
                    {
                        IssueHistory issueHistory = (IssueHistory) issueHistories.get(0);
                        lastComment = issueHistory.getComment();
                    }

                    sid = sourceTuv.getSid();

                    // TM Match
                    StringBuilder matches = ReportGeneratorUtil.getMatches(fuzzyLeverageMatchMap,
                            tuvMatchTypes, excludItems, sourceTuvs, targetTuvs, m_bundle,
                            sourceTuv, targetTuv, jobId);

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

                    // Source segment
                    cell = ExcelUtil.getCell(currentRow, col);
                    cell.setCellValue(getSegment(pData, sourceTuv, m_rtlSourceLocale, jobId));
                    cell.setCellStyle(m_rtlSourceLocale ? REPORT_STYLE.getRtlContentStyle()
                            : contentStyle);
                    col++;

                    // Target segment
                    cell = ExcelUtil.getCell(currentRow, col);
                    cell.setCellValue(getSegment(pData, targetTuv, m_rtlTargetLocale, jobId));
                    cell.setCellStyle(m_rtlTargetLocale ? REPORT_STYLE.getRtlContentStyle()
                            : contentStyle);
                    col++;

                    // Comments
                    if (m_rtlTargetLocale)
                    {
                        lastComment = EditUtil.toRtlString(lastComment);
                    }
                    cell = ExcelUtil.getCell(currentRow, col);
                    cell.setCellValue(lastComment);
                    cell.setCellStyle(m_rtlTargetLocale ? REPORT_STYLE.getRtlContentStyle()
                            : REPORT_STYLE.getContentStyle());
                    col++;

                    // Reviewers comments
                    cell = ExcelUtil.getCell(currentRow, col);
                    cell.setCellValue("");
                    cell.setCellStyle(m_rtlTargetLocale ? REPORT_STYLE.getUnlockedRightStyle()
                            : REPORT_STYLE.getUnlockedStyle());
                    col++;

                    // Category failure
                    cell = ExcelUtil.getCell(currentRow, col);
                    cell.setCellValue(failure);
                    cell.setCellStyle(unlockedStyle);
                    col++;

                    // Severity
                    cell = ExcelUtil.getCell(currentRow, col);
                    cell.setCellValue(severity);
                    cell.setCellStyle(unlockedStyle);
                    col++;

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

                    // Job id
                    cell = ExcelUtil.getCell(currentRow, col);
                    cell.setCellValue(p_job.getId());
                    cell.setCellStyle(contentStyle);
                    col++;

                    // SID// Segment id
                    cell = ExcelUtil.getCell(currentRow, col);
                    cell.setCellValue(sourceTuv.getTu(jobId).getId());
                    cell.setCellStyle(contentStyle);
                    col++;

                    // Fix for GBS-1484
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
                    // Page Name
                    cell = ExcelUtil.getCell(currentRow, col);
                    cell.setCellValue(Name);
                    cell.setCellStyle(contentStyle);
                    col++;

                    // SID
                    cell = ExcelUtil.getCell(currentRow, col);
                    cell.setCellValue(sid);
                    cell.setCellStyle(contentStyle);
                    col++;

                    p_row++;
                }
            }

            // Add category failure drop down list here.
            ExcelUtil.createValidatorList(p_sheet, getFailureCategoriesList(), SEGMENT_START_ROW,
                    p_row - 1, CATEGORY_FAILURE_COLUMN);
            ExcelUtil.createValidatorList(p_sheet, (List<String>) IssueOptions.getAllStatus(),
                    SEGMENT_START_ROW, p_row - 1, COMMENT_STATUS_COLUMN);

            String currentCompanyId = CompanyWrapper.getCurrentCompanyId();
            List<String> categories = CompanyWrapper.getCompanyCategoryNames(m_bundle,
                    currentCompanyId, CategoryType.Severity, true);
            ExcelUtil.createValidatorList(p_sheet, categories, SEGMENT_START_ROW, p_row - 1,
                    SEVERITY_COLUMN);

            if (DQF_START_ROW > 0)
            {
                categories = CompanyWrapper.getCompanyCategoryNames(m_bundle, currentCompanyId,
                        CategoryType.Fluency, true);
                ExcelUtil.createValidatorList(p_sheet, categories, DQF_START_ROW, DQF_START_ROW, 1);

                categories = CompanyWrapper.getCompanyCategoryNames(m_bundle, currentCompanyId,
                        CategoryType.Adequacy, true);
                ExcelUtil.createValidatorList(p_sheet, categories, DQF_START_ROW + 1,
                        DQF_START_ROW + 1, 1);
            }
            if (SCORECARD_START_ROW > 0)
            {
                String[] data = new String[]
                { "5", "4", "3", "2", "1" };
                ExcelUtil.createValidatorList(p_sheet, data, SCORECARD_START_ROW + 1,
                        SCORECARD_START_ROW + scorecardCategories.size(), 1);
            }
        }

        return p_row;
    }

    /**
     * Populates a term leverage options object.
     */
    private TermLeverageOptions getTermLeverageOptions(Locale p_sourceLocale, Locale p_targetLocale,
            String p_termbaseName, String p_companyId) throws Exception
    {
        TermLeverageOptions options = null;

        Locale sourceLocale = p_sourceLocale;
        Locale targetLocale = p_targetLocale;

        try
        {
            ITermbaseManager manager = ServerProxy.getTermbaseManager();
            long termbaseId;
            if (p_companyId != null)
            {
                termbaseId = manager.getTermbaseId(p_termbaseName, p_companyId);
            }
            else
            {
                termbaseId = manager.getTermbaseId(p_termbaseName);
            }

            // If termbase does not exist, return null options.
            if (termbaseId == -1)
            {
                return null;
            }

            options = new TermLeverageOptions();
            options.addTermBase(p_termbaseName);
            options.setLoadTargetTerms(true);
            options.setSaveToDatabase(false);

            // fuzzy threshold set by object constructor - use defaults.
            options.setFuzzyThreshold(0);

            ITermbase termbase = null;
            if (p_companyId != null)
            {
                termbase = manager.connect(p_termbaseName, ITermbase.SYSTEM_USER, "", p_companyId);
            }
            else
            {
                termbase = manager.connect(p_termbaseName, ITermbase.SYSTEM_USER, "");
            }

            // add source locale and lang names
            options.setSourcePageLocale(sourceLocale);
            ArrayList sourceLangNames = termbase.getLanguagesByLocale(sourceLocale.toString());

            for (int i = 0, max = sourceLangNames.size(); i < max; i++)
            {
                String langName = (String) sourceLangNames.get(i);

                options.addSourcePageLocale2LangName(langName);
            }

            // add target locales and lang names
            ArrayList targetLangNames = termbase.getLanguagesByLocale(targetLocale.toString());
            for (int i = 0, max = targetLangNames.size(); i < max; i++)
            {
                String langName = (String) targetLangNames.get(i);
                options.addTargetPageLocale2LangName(targetLocale, langName);
                options.addLangName2Locale(langName, targetLocale);
            }
        }
        catch (Exception ex)
        {
            throw new GeneralException(ex);
        }

        return options;
    }

    /**
     * Check the job data for report, for example "company name". If the job
     * data is correct, then return true.
     */
    protected boolean checkJob(Job p_job)
    {
        if (p_job == null)
            return false;

        if (CompanyWrapper.isSuperCompanyName(m_companyName))
            return true;

        String companyId = CompanyWrapper.getCompanyIdByName(m_companyName);
        if (companyId != null && companyId.equals(p_job.getCompanyId()))
            return true;

        return false;
    }

    private List<String> getFailureCategoriesList()
    {
        List<String> result = new ArrayList<String>();

        String currentCompanyId = CompanyThreadLocal.getInstance().getValue();
        List failureCategories = IssueOptions.getAllCategories(m_bundle, currentCompanyId);
        for (int i = 0; i < failureCategories.size(); i++)
        {
            Select aCategory = (Select) failureCategories.get(i);
            String cat = aCategory.getValue();
            result.add(cat);
        }
        return result;
    }

    @Override
    public String getReportType()
    {
        return ReportConstants.REVIEWERS_COMMENTS_REPORT;
    }

    @Override
    public void setPercent(int p_finishedJobNum)
    {
        // The setPercent function works when from HttpServletRequest.
        if (m_isCalculatePercent)
        {
            ReportGeneratorHandler.setReportsMapByGenerator(m_userId, m_jobIDS,
                    100 * p_finishedJobNum / m_jobIDS.size(), getReportType());
        }
    }

    @Override
    public boolean isCancelled()
    {
        // The isCancelled function works when from HttpServletRequest.
        if (m_isCalculatePercent)
        {
            ReportsData data = ReportGeneratorHandler.getReportsMap(m_userId, m_jobIDS,
                    getReportType());
            if (data != null)
                return data.isCancle();
        }

        return false;
    }

    @Override
    public void cancel()
    {
        cancel = true;
    }

    private String getSegment(PseudoData pData, Tuv tuv, boolean m_rtlLocale, long p_jobId)
    {
        String result = null;
        StringBuffer content = new StringBuffer();
        List subFlows = tuv.getSubflowsAsGxmlElements();
        long tuId = tuv.getTuId();
        if (isIncludeCompactTags)
        {
            String dataType = null;
            try
            {
                dataType = tuv.getDataType(p_jobId);
                pData.setAddables(dataType);
                TmxPseudo.tmx2Pseudo(tuv.getGxmlExcludeTopTags(), pData);
                content.append(pData.getPTagSourceString());

                if (subFlows != null && subFlows.size() > 0)
                {
                    for (int i = 0; i < subFlows.size(); i++)
                    {
                        GxmlElement sub = (GxmlElement) subFlows.get(i);
                        String subId = sub.getAttribute(GxmlNames.SUB_ID);
                        content.append("\r\n#").append(tuId).append(":").append(subId).append("\n")
                                .append(getCompactPtagString(sub, dataType));
                    }
                }
            }
            catch (Exception e)
            {
                logger.error(tuv.getId(), e);
            }
        }
        else
        {
            String mainSeg = tuv.getGxmlElement().getTextValue();
            content.append(mainSeg);

            if (subFlows != null && subFlows.size() > 0)
            {
                for (int i = 0; i < subFlows.size(); i++)
                {
                    GxmlElement sub = (GxmlElement) subFlows.get(i);
                    String subId = sub.getAttribute(GxmlNames.SUB_ID);
                    content.append("\r\n#").append(tuId).append(":").append(subId).append("\n")
                            .append(sub.getTextValue());
                }
            }
        }

        result = content.toString();
        if (m_rtlLocale)
        {
            result = EditUtil.toRtlString(result);
        }

        return result;
    }

    private String getCompactPtagString(GxmlElement p_gxmlElement, String p_dataType)
    {
        String compactPtags = null;
        OnlineTagHelper applet = new OnlineTagHelper();
        try
        {
            String seg = GxmlUtil.getInnerXml(p_gxmlElement);
            applet.setDataType(p_dataType);
            applet.setInputSegment(seg, "", p_dataType);
            compactPtags = applet.getCompact();
        }
        catch (Exception e)
        {
            logger.info("getCompactPtagString Error.", e);
        }

        return compactPtags;
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

    public void setIncludeCompactTags(boolean isWithCompactTags)
    {
        this.isIncludeCompactTags = isWithCompactTags;
    }

    public void setUserId(String p_userId)
    {
        this.m_userId = p_userId;
    }
}
