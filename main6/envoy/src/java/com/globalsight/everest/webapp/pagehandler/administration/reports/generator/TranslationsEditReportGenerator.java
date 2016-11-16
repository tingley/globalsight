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
import com.globalsight.everest.integration.ling.LingServerProxy;
import com.globalsight.everest.integration.ling.tm2.LeverageMatch;
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
import com.globalsight.util.*;
import com.globalsight.util.edit.EditUtil;
import com.globalsight.util.edit.GxmlUtil;
import com.globalsight.util.gxml.GxmlElement;
import com.globalsight.util.gxml.GxmlNames;
import com.globalsight.util.resourcebundle.ResourceBundleConstants;
import com.globalsight.util.resourcebundle.SystemResourceBundle;
import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.FileOutputStream;
import java.util.*;

/**
 * This Generator is used for creating Translations Edit Report (Include
 * Translations Edit Report in offline download report page)
 */
public class TranslationsEditReportGenerator implements ReportGenerator, Cancelable
{
    private static final Logger logger = Logger.getLogger(TranslationsEditReportGenerator.class);

    public final int LANGUAGE_HEADER_ROW = 3;
    public final int LANGUAGE_INFO_ROW = 4;
    public int SEGMENT_HEADER_ROW = 6;
    public int SEGMENT_START_ROW = 7;
    public int SCORECARD_START_ROW = 0;
    public int DQF_START_ROW = 0;
    
    public int CATEGORY_FAILURE_COLUMN = 5;
    public int SEVERITY_COLUMN = 6;
    public int COMMENT_STATUS_COLUMN = 6;

    private Locale m_uiLocale;
    private String m_companyName = "";
    protected List<Long> m_jobIDS = new ArrayList<Long>();
    protected List<GlobalSightLocale> m_targetLocales = new ArrayList<GlobalSightLocale>();
    private String m_dateFormat;
    private ResourceBundle m_bundle;
    String m_userId;

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

    public TranslationsEditReportGenerator(String p_currentCompanyName)
    {
        m_companyName = p_currentCompanyName;
        CompanyThreadLocal.getInstance().setValue(m_companyName);
        m_uiLocale = Locale.US;
        m_bundle = SystemResourceBundle.getInstance()
                .getResourceBundle(ResourceBundleConstants.LOCALE_RESOURCE_NAME, m_uiLocale);
    }

    public TranslationsEditReportGenerator(String p_currentCompanyName, String p_userId)
    {
        m_companyName = p_currentCompanyName;
        CompanyThreadLocal.getInstance().setValue(m_companyName);
        m_uiLocale = Locale.US;
        m_bundle = SystemResourceBundle.getInstance()
                .getResourceBundle(ResourceBundleConstants.LOCALE_RESOURCE_NAME, m_uiLocale);
        m_userId = p_userId;
    }

    /**
     * Constructor.
     * 
     * @param p_request
     *            the request
     * @param p_response
     *            the response
     * @throws Exception
     */
    public TranslationsEditReportGenerator(HttpServletRequest p_request,
            HttpServletResponse p_response) throws Exception
    {
        HttpSession session = p_request.getSession();
        m_userId = (String) session.getAttribute(WebAppConstants.USER_NAME);
        m_uiLocale = (Locale) session.getAttribute(WebAppConstants.UILOCALE);
        if (m_uiLocale == null)
        {
            m_uiLocale = Locale.US;
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
    }

    @Override
    public File[] generateReports(List<Long> p_jobIDS, List<GlobalSightLocale> p_targetLocales)
            throws Exception
    {
        List<File> workBooks = new ArrayList<File>();
        for (long jobID : p_jobIDS)
        {
            if (cancel)
                return new File[0];

            Job job = ServerProxy.getJobHandler().getJobById(jobID);
            if (job == null)
                continue;

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
        // Till now, only support one target locale.
        GlobalSightLocale trgLocale = p_targetLocales.get(0);

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

        // Create Sheet
        Sheet sheet = p_workbook.createSheet(trgLocale.toString());
        sheet.protectSheet("");

        // Add Title
        addTitle(p_workbook, sheet);

        // Add hidden info "TER_taskID" for uploading.
        addHidenInfoForUpload(p_workbook, sheet, p_job, trgLocale);

        // Add Locale Pair Header
        addLanguageHeader(p_workbook, sheet);

        // Add DQF and Scorecard info
        addDQFHeader(p_workbook, sheet);

        // Add Segment Header
        addSegmentHeader(p_workbook, sheet);

        // Insert Data into Report
        String srcLang = p_job.getSourceLocale().getDisplayName(m_uiLocale);
        String trgLang = trgLocale.getDisplayName(m_uiLocale);
        writeLanguageInfo(p_workbook, sheet, srcLang, trgLang);

        // Create Name Areas for drop down list.
        ExcelUtil.createValidatorList(p_workbook, "FailureCategoriesValidator", getFailureCategoriesList(), SEGMENT_START_ROW, 26);

        String currentCompanyId = CompanyThreadLocal.getInstance().getValue();
        List<String> categories = CompanyWrapper.getCompanyCategoryNames(m_bundle,
                currentCompanyId, CategoryType.Severity, true);
        ExcelUtil.createValidatorList(p_workbook, "SeverityCategoriesValidator", categories, SEGMENT_START_ROW, 27);

        int lastRow = writeSegmentInfo(p_workbook, sheet, p_job, trgLocale, "", p_dateFormat, SEGMENT_START_ROW);

        ExcelUtil.addValidation(sheet, "FailureCategoriesValidator", SEGMENT_START_ROW, lastRow - 1,
                CATEGORY_FAILURE_COLUMN, CATEGORY_FAILURE_COLUMN);

        if (DQF_START_ROW > 0)
        {
            ExcelUtil.addValidation(sheet, "SeverityCategoriesValidator", SEGMENT_START_ROW, lastRow - 1,
                    SEVERITY_COLUMN, SEVERITY_COLUMN);

            categories = CompanyWrapper.getCompanyCategoryNames(m_bundle, currentCompanyId,
                    CategoryType.Fluency, true);
            ExcelUtil.createValidatorList(sheet, categories, DQF_START_ROW, DQF_START_ROW, 1);

            categories = CompanyWrapper.getCompanyCategoryNames(m_bundle, currentCompanyId,
                    CategoryType.Adequacy, true);
            ExcelUtil.createValidatorList(sheet, categories, DQF_START_ROW + 1, DQF_START_ROW + 1, 1);
        }
        if (SCORECARD_START_ROW > 0)
        {
            String[] data = new String[] { "5", "4", "3", "2", "1" };
            ExcelUtil.createValidatorList(sheet, data, SCORECARD_START_ROW + 1,
                    SCORECARD_START_ROW + scorecardCategories.size(), 1);
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
                    
                    cell = ExcelUtil.getCell(rowLine, col + 1);
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
        titleCell.setCellValue(m_bundle.getString("lb_translation_edit_report"));
        titleCell.setCellStyle(REPORT_STYLE.getTitleStyle());
    }

    /**
     * Add hidden info "TER_taskID" for offline uploading. When upload, system
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
                        reportInfo = ReportConstants.TRANSLATIONS_EDIT_REPORT_ABBREVIATION + "_"
                                + task.getId();
                        needProtect = !(task.isType(Task.TYPE_REVIEW) || task.isType(Task.TYPE_REVIEW_EDITABLE));
                    }
                }
                int scoreShowType = wf.getScorecardShowType();
                if (scoreShowType > 1)
                {
                    isDQFEnabled = true;
                    fluencyScore = wf.getFluencyScore();
                    adequacyScore = wf.getAdequacyScore();
                    dqfComment = wf.getDQFComment();
                    DQF_START_ROW = 6;

                    COMMENT_STATUS_COLUMN = 7;
                }
                else
                    COMMENT_STATUS_COLUMN = 6;

                if (scoreShowType > -1 && scoreShowType < 4)
                {
                    isScorecradEnabled = true;
                    scorecardCategories = ScorecardScoreHelper.getScorecardCategories(
                            wf.getCompanyId(), m_bundle);
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

        Row titleRow = ExcelUtil.getRow(p_sheet, 0);
        Cell taskIdCell = ExcelUtil.getCell(titleRow, 26);
        taskIdCell.setCellValue(reportInfo);
        taskIdCell.setCellStyle(REPORT_STYLE.getContentStyle());

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
     * Add segment header to the sheet
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

        Cell cell = ExcelUtil.getCell(segHeaderRow, col);
        cell.setCellValue(m_bundle.getString("lb_source_segment"));
        cell.setCellStyle(REPORT_STYLE.getHeaderStyle());
        p_sheet.setColumnWidth(col, 40 * 256);
        col++;

        cell = ExcelUtil.getCell(segHeaderRow, col);
        cell.setCellValue(m_bundle.getString("lb_target_segment"));
        cell.setCellStyle(REPORT_STYLE.getHeaderStyle());
        p_sheet.setColumnWidth(col, 40 * 256);
        col++;

        cell = ExcelUtil.getCell(segHeaderRow, col);
        cell.setCellValue(m_bundle.getString("lb_modify_the_translation"));
        cell.setCellStyle(REPORT_STYLE.getHeaderStyle());
        p_sheet.setColumnWidth(col, 50 * 256);
        col++;

        cell = ExcelUtil.getCell(segHeaderRow, col);
        cell.setCellValue(m_bundle.getString("latest_comments"));
        cell.setCellStyle(REPORT_STYLE.getHeaderStyle());
        p_sheet.setColumnWidth(col, 40 * 256);
        col++;

        cell = ExcelUtil.getCell(segHeaderRow, col);
        cell.setCellValue(m_bundle.getString("translation_comments"));
        cell.setCellStyle(REPORT_STYLE.getHeaderStyle());
        p_sheet.setColumnWidth(col, 40 * 256);
        col++;

        cell = ExcelUtil.getCell(segHeaderRow, col);
        cell.setCellValue(m_bundle.getString("lb_category_failure"));
        cell.setCellStyle(REPORT_STYLE.getHeaderStyle());
        p_sheet.setColumnWidth(col, 40 * 256);
        col++;

        if (isDQFEnabled)
        {
            cell = ExcelUtil.getCell(segHeaderRow, col);
            cell.setCellValue(m_bundle.getString("lb_dqf_severity"));
            cell.setCellStyle(REPORT_STYLE.getHeaderStyle());
            p_sheet.setColumnWidth(col, 15 * 256);
            col++;
        }

        cell = ExcelUtil.getCell(segHeaderRow, col);
        cell.setCellValue(m_bundle.getString("lb_comment_status"));
        cell.setCellStyle(REPORT_STYLE.getHeaderStyle());
        p_sheet.setColumnWidth(col, 15 * 256);
        col++;
        
        cell = ExcelUtil.getCell(segHeaderRow, col);
        cell.setCellValue(m_bundle.getString("lb_tm_match_original"));
        cell.setCellStyle(REPORT_STYLE.getHeaderStyle());
        p_sheet.setColumnWidth(col, 20 * 256);
        col++;

        cell = ExcelUtil.getCell(segHeaderRow, col);
        cell.setCellValue(m_bundle.getString("lb_glossary_source"));
        cell.setCellStyle(REPORT_STYLE.getHeaderStyle());
        p_sheet.setColumnWidth(col, 25 * 256);
        col++;

        cell = ExcelUtil.getCell(segHeaderRow, col);
        cell.setCellValue(m_bundle.getString("lb_glossary_target"));
        cell.setCellStyle(REPORT_STYLE.getHeaderStyle());
        p_sheet.setColumnWidth(col, 25 * 256);
        col++;

        cell = ExcelUtil.getCell(segHeaderRow, col);
        cell.setCellValue(m_bundle.getString("lb_job_id_report"));
        cell.setCellStyle(REPORT_STYLE.getHeaderStyle());
        p_sheet.setColumnWidth(col, 15 * 256);
        col++;

        cell = ExcelUtil.getCell(segHeaderRow, col);
        cell.setCellValue(m_bundle.getString("lb_segment_id"));
        cell.setCellStyle(REPORT_STYLE.getHeaderStyle());
        p_sheet.setColumnWidth(col, 15 * 256);
        col++;

        cell = ExcelUtil.getCell(segHeaderRow, col);
        cell.setCellValue(m_bundle.getString("lb_page_name"));
        cell.setCellStyle(REPORT_STYLE.getHeaderStyle());
        p_sheet.setColumnWidth(col, 25 * 256);
        col++;

        cell = ExcelUtil.getCell(segHeaderRow, col);
        cell.setCellValue(m_bundle.getString("lb_sid"));
        cell.setCellStyle(REPORT_STYLE.getHeaderStyle());
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
     * For Translations Edit Report, Write segment information into each row of
     * the sheet.
     * 
     * @param p_sheet
     *            the sheet
     * @param p_jobId
     *            the job id
     * @param p_targetLang
     *            the target locale String
     * @param p_srcPageId
     *            the source page id
     * @param p_dateFormat
     *            the data format
     * @param p_row
     *            the segment row in sheet
     */
    private int writeSegmentInfo(Workbook p_workBook, Sheet p_sheet, Job p_job,
            GlobalSightLocale p_targetLocale, String p_srcPageId, String p_dateFormat, int p_row)
            throws Exception
    {
        Collection<TargetPage> targetPages = null;

        TranslationMemoryProfile tmp = null;
        Vector<String> excludItems = null;

        for (Workflow workflow : p_job.getWorkflows())
        {
            if (cancel)
                return 0;

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
                tmp = workflow.getJob().getL10nProfile().getTranslationMemoryProfile();
                if (tmp != null)
                {
                    excludItems = tmp.getJobExcludeTuTypes();
                }
            }
        }

        if (targetPages.isEmpty())
        {
            // If no corresponding target page exists, set the cell blank
            writeBlank(p_sheet, p_row, 11);
        }
        else
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
                termLeverageMatchResultMap = termLeverageManager
                        .getTermMatchesForPages(p_job.getSourcePages(), p_targetLocale);
            }

            String category = null;
            PseudoData pData = new PseudoData();
            pData.setMode(PseudoConstants.PSEUDO_COMPACT);
            String sid = null;
            Set<Integer> rowsWithCommentSet = new HashSet<Integer>();
            for (TargetPage targetPage : targetPages)
            {
                if (cancel)
                    return 0;

                SourcePage sourcePage = targetPage.getSourcePage();

                if (!"".equals(p_srcPageId)
                        && !p_srcPageId.equals(String.valueOf(sourcePage.getId())))
                {
                    // ignore the source pages not equal to the one
                    // if the request comes from pop up editor
                    continue;
                }

                SegmentTuUtil.getTusBySourcePageId(sourcePage.getId());
                List sourceTuvs = SegmentTuvUtil.getSourceTuvs(sourcePage);
                List targetTuvs = SegmentTuvUtil.getTargetTuvs(targetPage);

                MatchTypeStatistics tuvMatchTypes = lmLingManager.getMatchTypesForStatistics(
                        sourcePage.getIdAsLong(), targetPage.getLocaleId(),
                        p_job.getLeverageMatchThreshold());
                Map<Long, Set<LeverageMatch>> fuzzyLeverageMatchMap = lmLingManager.getFuzzyMatches(
                        sourcePage.getIdAsLong(), new Long(targetPage.getLocaleId()));

                sourcePageLocale = sourcePage.getGlobalSightLocale().getLocale();
                targetPageLocale = targetPage.getGlobalSightLocale().getLocale();

                boolean m_rtlSourceLocale = EditUtil.isRTLLocale(sourcePageLocale.toString());
                boolean m_rtlTargetLocale = EditUtil.isRTLLocale(targetPageLocale.toString());

                // Find segment all comments belong to this target page
                Map<Long, IssueImpl> issuesMap = CommentHelper.getIssuesMap(targetPage.getId());
                String lastComment, failure, severity, commentStatus;
                List issueHistories;
                Issue issue;
                Cell cell;
                CellStyle contentStyle = REPORT_STYLE.getContentStyle();

                for (int j = 0; j < targetTuvs.size(); j++)
                {
                    if (cancel)
                        return 0;

                    int col = 0;
                    Tuv targetTuv = (Tuv) targetTuvs.get(j);
                    Tuv sourceTuv = (Tuv) sourceTuvs.get(j);

                    category = sourceTuv.getTu(p_job.getId()).getTuType();
                    if (excludItems != null && excludItems.contains(category))
                    {
                        continue;
                    }

                    // Comment
                    issueHistories = null;
                    lastComment = "";
                    failure = "";
                    severity = "";
                    commentStatus = "";
                    issue = issuesMap.get(targetTuv.getId());
                    if (issue != null)
                    {
                        issueHistories = issue.getHistory();
                        failure = issue.getCategory();
                        commentStatus = issue.getStatus();
                        severity = issue.getSeverity();
                    }
                    if (issueHistories != null && issueHistories.size() > 0)
                    {
                        IssueHistory issueHistory = (IssueHistory) issueHistories.get(0);
                        lastComment = issueHistory.getComment();
                    }

                    sid = sourceTuv.getSid();

                    StringBuilder matches = ReportGeneratorUtil.getMatches(fuzzyLeverageMatchMap,
                            tuvMatchTypes, excludItems, sourceTuvs, targetTuvs, m_bundle,
                            sourceTuv, targetTuv, p_job.getId());

                    // Get Terminology/Glossary Source and Target.
                    String sourceTerms = "";
                    String targetTerms = "";
                    if (termLeverageMatchResultMap != null && termLeverageMatchResultMap.size() > 0)
                    {
                        Set<TermLeverageMatch> termLeverageMatchSet = termLeverageMatchResultMap
                                .get(sourceTuv.getId());
                        if (termLeverageMatchSet != null && termLeverageMatchSet.size() > 0)
                        {
                            TermLeverageMatch tlm = termLeverageMatchSet.iterator().next();
                            sourceTerms = tlm.getMatchedSourceTerm();
                            targetTerms = tlm.getMatchedTargetTerm();
                        }
                    }

                    Row currentRow = ExcelUtil.getRow(p_sheet, p_row);

                    // Source segment with compact tags
                    cell = ExcelUtil.getCell(currentRow, col);
                    cell.setCellValue(getSegment(pData, sourceTuv, m_rtlSourceLocale, p_job.getId()));
                    cell.setCellStyle(m_rtlSourceLocale ? REPORT_STYLE.getRtlContentStyle()
                            : contentStyle);
                    col++;

                    // Target segment with compact tags
                    cell = ExcelUtil.getCell(currentRow, col);
                    cell.setCellValue(getSegment(pData, targetTuv, m_rtlTargetLocale, p_job.getId()));
                    cell.setCellStyle(m_rtlTargetLocale ? REPORT_STYLE.getRtlContentStyle()
                            : contentStyle);
                    col++;

                    // Modify the translation
                    cell = ExcelUtil.getCell(currentRow, col);
                    cell.setCellValue("");
                    cell.setCellStyle(m_rtlTargetLocale ? REPORT_STYLE.getUnlockedRightStyle()
                            : REPORT_STYLE.getUnlockedStyle());
                    col++;

                    // Reviewers Comments
                    cell = ExcelUtil.getCell(currentRow, col);
                    cell.setCellValue(lastComment);
                    cell.setCellStyle(contentStyle);
                    col++;

                    // Translators Comments
                    cell = ExcelUtil.getCell(currentRow, col);
                    cell.setCellValue("");
                    cell.setCellStyle(REPORT_STYLE.getUnlockedStyle());
                    col++;

                    // Category failure
                    cell = ExcelUtil.getCell(currentRow, col);
                    cell.setCellValue(failure);
                    cell.setCellStyle(contentStyle);
                    col++;

                    if (isDQFEnabled)
                    {
                        // Severity
                        cell = ExcelUtil.getCell(currentRow, col);
                        cell.setCellValue(severity);
                        cell.setCellStyle(contentStyle);
                        col++;
                    }

                    // Comment Status
                    cell = ExcelUtil.getCell(currentRow, col);
                    cell.setCellValue(commentStatus);
                    cell.setCellStyle(REPORT_STYLE.getUnlockedStyle());

                    // add comment status drop down list for current row.
                    String[] statusArray = getCommentStatusList(lastComment);
                    if (statusArray.length > 1)
                    {
                        rowsWithCommentSet.add(p_row);
                    }
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

                    // Job Id
                    cell = ExcelUtil.getCell(currentRow, col);
                    cell.setCellValue(p_job.getId());
                    cell.setCellStyle(contentStyle);
                    col++;

                    // Segment id
                    cell = ExcelUtil.getCell(currentRow, col);
                    cell.setCellValue(sourceTuv.getTu(p_job.getId()).getId());
                    cell.setCellStyle(contentStyle);
                    col++;

                    // Fix for GBS-1484
                    String externalPageId = sourcePage.getExternalPageId();
                    String[] pathNames = externalPageId.split("\\\\");
                    String name = pathNames[pathNames.length - 1];
                    boolean temp = pathNames[0].contains(")");
                    if (temp)
                    {
                        String[] firstNames = pathNames[0].split("\\)");
                        String detailName = firstNames[0];
                        name = name + detailName + ")";
                    }
                    // Page Name
                    cell = ExcelUtil.getCell(currentRow, col);
                    cell.setCellValue(name);
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

            // Add comment status
            addCommentStatus(p_sheet, rowsWithCommentSet, p_row);
        }

        return p_row;
    }
    
    private void addCommentStatus(Sheet p_sheet, Set<Integer> rowsWithCommentSet, int last_row)
    {
        DataValidationHelper dvHelper = p_sheet.getDataValidationHelper();
        DataValidationConstraint dvConstraintAll = null;
        DataValidationConstraint dvConstraintOne = null;
        CellRangeAddressList addressListOne = new CellRangeAddressList();
        CellRangeAddressList addressListAll = new CellRangeAddressList();
        CellRangeAddress cellAddress = null;

        List<String> status = new ArrayList<String>();
        status.addAll(IssueOptions.getAllStatus());
        String[] allStatus = new String[status.size()];
        status.toArray(allStatus);
        dvConstraintAll = dvHelper.createExplicitListConstraint(allStatus);

        String[] oneStatus =
        { Issue.STATUS_QUERY };
        dvConstraintOne = dvHelper.createExplicitListConstraint(oneStatus);

        if (rowsWithCommentSet.size() == 0)
        {
            cellAddress = new CellRangeAddress(SEGMENT_START_ROW, last_row - 1,
                    COMMENT_STATUS_COLUMN, COMMENT_STATUS_COLUMN);
            addressListOne.addCellRangeAddress(cellAddress);
            addCommentStatusValidation(p_sheet, dvHelper, dvConstraintOne, addressListOne);
        }
        else
        {
            boolean hasComment = false;
            int startRow = SEGMENT_START_ROW;
            int endRow = -1;
            for (int row = SEGMENT_START_ROW; row < last_row; row++)
            {
                if (rowsWithCommentSet.contains(row))
                {
                    if (!hasComment && row != SEGMENT_START_ROW)
                    {
                        endRow = row - 1;
                        cellAddress = new CellRangeAddress(startRow, endRow, COMMENT_STATUS_COLUMN,
                                COMMENT_STATUS_COLUMN);
                        addressListOne.addCellRangeAddress(cellAddress);
                        startRow = row;
                    }
                    hasComment = true;
                }
                else
                {
                    if (hasComment)
                    {
                        endRow = row - 1;
                        cellAddress = new CellRangeAddress(startRow, endRow, COMMENT_STATUS_COLUMN,
                                COMMENT_STATUS_COLUMN);
                        addressListAll.addCellRangeAddress(cellAddress);
                        startRow = row;
                    }
                    hasComment = false;
                }

                if (row == last_row - 1)
                {
                    cellAddress = new CellRangeAddress(startRow, last_row - 1,
                            COMMENT_STATUS_COLUMN, COMMENT_STATUS_COLUMN);
                    if (hasComment)
                    {
                        addressListAll.addCellRangeAddress(cellAddress);
                    }
                    else
                    {
                        addressListOne.addCellRangeAddress(cellAddress);
                    }
                }
            }

            addCommentStatusValidation(p_sheet, dvHelper, dvConstraintAll, addressListAll);
            addCommentStatusValidation(p_sheet, dvHelper, dvConstraintOne, addressListOne);
        }
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

    private List<String> getFailureCategoriesList()
    {
        List<String> result = new ArrayList<String>();

        String currentCompanyId = CompanyThreadLocal.getInstance().getValue();
        List failureCategories = IssueOptions.getAllCategories(m_bundle, currentCompanyId);
        for (int i = 0; i < failureCategories.size(); i++)
        {
            Select aCategory = (Select) failureCategories.get(i);
            result.add(aCategory.getValue());
        }

        return result;
    }

    /**
     * Set the cell blank
     * 
     * @param p_sheet
     *            the sheet
     * @param p_row
     *            the row position
     * @param p_colLen
     *            the blank column length
     * @throws Exception
     */
    private void writeBlank(Sheet p_sheet, int p_row, int p_colLen) throws Exception
    {
        for (int col = 0; col < p_colLen; col++)
        {
            Row row = p_sheet.getRow(p_row);
            Cell cell = ExcelUtil.getCell(row, col);
            cell.setCellValue("");
            col++;
        }
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

    @Override
    public String getReportType()
    {
        return ReportConstants.TRANSLATIONS_EDIT_REPORT;
    }

    @Override
    public void setPercent(int p_finishedJobNum)
    {
    }

    @Override
    public boolean isCancelled()
    {
        return false;
    }

    @Override
    public void cancel()
    {
        cancel = true;
    }

    private String[] getCommentStatusList(String lastComment)
    {
        List<String> status = new ArrayList<String>();
        if (lastComment == null || "".equals(lastComment))
        {
            status.add(Issue.STATUS_QUERY);
        }
        else
        {
            status.addAll(IssueOptions.getAllStatus());
        }

        String[] statusArray = new String[status.size()];
        status.toArray(statusArray);

        return statusArray;
    }

    private void addCommentStatusValidation(Sheet p_sheet, DataValidationHelper dvHelper,
            DataValidationConstraint dvConstraint, CellRangeAddressList addressList)
    {
        if (addressList == null || addressList.countRanges() == 0)
            return;

        DataValidation validationOne = dvHelper.createValidation(dvConstraint, addressList);
        validationOne.setSuppressDropDownArrow(true);
        validationOne.setShowErrorBox(true);
        p_sheet.addValidationData(validationOne);
    }

    private String getSegment(PseudoData pData, Tuv tuv, boolean m_rtlLocale, long p_jobId)
    {
        StringBuffer content = new StringBuffer();
        String dataType = null;
        try
        {
            dataType = tuv.getDataType(p_jobId);
            pData.setAddables(dataType);
            TmxPseudo.tmx2Pseudo(tuv.getGxmlExcludeTopTags(), pData);
            content.append(pData.getPTagSourceString());

            // If there are subflows, output them too.
            List subFlows = tuv.getSubflowsAsGxmlElements();
            if (subFlows != null && subFlows.size() > 0)
            {
                long tuId = tuv.getTuId();
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

        String result = content.toString();
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

    public void setUserId(String p_userId)
    {
        this.m_userId = p_userId;
    }
}
