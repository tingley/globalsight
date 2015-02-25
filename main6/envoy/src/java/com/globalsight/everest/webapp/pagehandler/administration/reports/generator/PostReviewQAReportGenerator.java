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
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
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
import org.apache.poi.ss.usermodel.DataValidation;
import org.apache.poi.ss.usermodel.DataValidationConstraint;
import org.apache.poi.ss.usermodel.DataValidationHelper;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Name;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

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
import com.globalsight.everest.tuv.TaskTuv;
import com.globalsight.everest.tuv.Tuv;
import com.globalsight.everest.tuv.TuvManager;
import com.globalsight.everest.util.comparator.GlobalSightLocaleComparator;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.administration.company.Select;
import com.globalsight.everest.webapp.pagehandler.administration.reports.ReportConstants;
import com.globalsight.everest.webapp.pagehandler.administration.reports.ReportHelper;
import com.globalsight.everest.webapp.pagehandler.administration.users.UserUtil;
import com.globalsight.everest.webapp.pagehandler.edit.online.OnlineTagHelper;
import com.globalsight.everest.workflowmanager.Workflow;
import com.globalsight.ling.tm.LeverageMatchLingManager;
import com.globalsight.ling.tm2.leverage.LeverageUtil;
import com.globalsight.ling.tw.PseudoConstants;
import com.globalsight.ling.tw.PseudoData;
import com.globalsight.ling.tw.TmxPseudo;
import com.globalsight.terminology.ITermbase;
import com.globalsight.terminology.ITermbaseManager;
import com.globalsight.terminology.termleverager.TermLeverageManager;
import com.globalsight.terminology.termleverager.TermLeverageMatch;
import com.globalsight.terminology.termleverager.TermLeverageOptions;
import com.globalsight.util.GeneralException;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.SortUtil;
import com.globalsight.util.StringUtil;
import com.globalsight.util.edit.EditUtil;
import com.globalsight.util.edit.GxmlUtil;
import com.globalsight.util.gxml.GxmlElement;
import com.globalsight.util.gxml.GxmlNames;
import com.globalsight.util.resourcebundle.ResourceBundleConstants;
import com.globalsight.util.resourcebundle.SystemResourceBundle;

/**
 * This Generator is used for creating Translations Edit Report (Include
 * Translations Edit Report in offline download report page)
 */
public class PostReviewQAReportGenerator implements ReportGenerator, Cancelable
{
    private static final Logger logger = Logger
            .getLogger(PostReviewQAReportGenerator.class);

    private static final String CATEGORY_FAILURE_DROP_DOWN_LIST = "categoryFailureDropDownList";
    private static final String QUALITY_ASSESSMENT_LIST = "qualityAssessmentList";
    private static final String MARKET_SUITABILITY_LIST = "marketSuitabilityList";

    private CellStyle headerStyle = null;
    private CellStyle contentStyle = null;
    private CellStyle rtlContentStyle = null;
    private CellStyle unlockedStyle = null;
    private CellStyle unlockedRightStyle = null;

    public static final int LANGUAGE_HEADER_ROW = 3;
    public static final int LANGUAGE_INFO_ROW = 4;
    public static final int SEGMENT_HEADER_ROW = 10;
    public static final int SEGMENT_START_ROW = 11;
    // "E" column, index 4
    public static final int CATEGORY_FAILURE_COLUMN = 4;
    // "F" column, index 5
    public static final int COMMENT_STATUS_COLUMN = 5;
    // "G" column, index 6
    public static final int PRIORITY_COLUMN = 6;

    private Locale m_uiLocale;
    private String m_companyName = "";
    protected List<Long> m_jobIDS = new ArrayList<Long>();
    protected List<GlobalSightLocale> m_targetLocales = new ArrayList<GlobalSightLocale>();
    private String m_dateFormat;
    private ResourceBundle m_bundle;
    String m_userId;

    private boolean cancel = false;

    public PostReviewQAReportGenerator(String p_currentCompanyName)
    {
        m_companyName = p_currentCompanyName;
        CompanyThreadLocal.getInstance().setValue(m_companyName);
        m_uiLocale = Locale.US;
        m_bundle = SystemResourceBundle.getInstance().getResourceBundle(
                ResourceBundleConstants.LOCALE_RESOURCE_NAME, m_uiLocale);
    }

    public PostReviewQAReportGenerator(String p_currentCompanyName,
            String p_userId)
    {
        m_companyName = p_currentCompanyName;
        CompanyThreadLocal.getInstance().setValue(m_companyName);
        m_uiLocale = Locale.US;
        m_bundle = SystemResourceBundle.getInstance().getResourceBundle(
                ResourceBundleConstants.LOCALE_RESOURCE_NAME, m_uiLocale);
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
    public PostReviewQAReportGenerator(HttpServletRequest p_request,
            HttpServletResponse p_response) throws Exception
    {
        HttpSession session = p_request.getSession();
        m_userId = (String) session.getAttribute(WebAppConstants.USER_NAME);
        m_uiLocale = (Locale) session.getAttribute(WebAppConstants.UILOCALE);
        if (m_uiLocale == null)
        {
            m_uiLocale = Locale.US;
        }
        m_bundle = SystemResourceBundle.getInstance().getResourceBundle(
                ResourceBundleConstants.LOCALE_RESOURCE_NAME, m_uiLocale);

        m_dateFormat = p_request.getParameter(WebAppConstants.DATE_FORMAT);
        if (m_dateFormat == null)
        {
            m_dateFormat = DEFAULT_DATE_FORMAT;
        }

        m_jobIDS = ReportHelper.getListOfLong(p_request
                .getParameter(ReportConstants.JOB_IDS));
        GlobalSightLocaleComparator comparator = new GlobalSightLocaleComparator(
                GlobalSightLocaleComparator.ISO_CODE, m_uiLocale);
        m_targetLocales = ReportHelper
                .getTargetLocaleList(p_request
                        .getParameterValues(ReportConstants.TARGETLOCALE_LIST),
                        comparator);

        m_companyName = UserUtil.getCurrentCompanyName(p_request);
        if (CompanyWrapper.isSuperCompanyName(m_companyName)
                && m_jobIDS != null && m_jobIDS.size() > 0)
        {
            Job job = ServerProxy.getJobHandler().getJobById(m_jobIDS.get(0));
            m_companyName = CompanyWrapper.getCompanyNameById(job
                    .getCompanyId());
        }
        CompanyThreadLocal.getInstance().setValue(m_companyName);
    }

    @Override
    public File[] generateReports(List<Long> p_jobIDS,
            List<GlobalSightLocale> p_targetLocales) throws Exception
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
            List<GlobalSightLocale> p_targetLocales, String p_dateFormat)
            throws Exception
    {
        // Till now, only support one target locale.
        GlobalSightLocale trgLocale = p_targetLocales.get(0);

        // Create Sheet
        Sheet sheet = p_workbook.createSheet(trgLocale.toString());
        sheet.protectSheet("");

        // Add Title
        addTitle(p_workbook, sheet);

        // Add hidden info "PRR_taskID" for uploading.
        addHidenInfoForUpload(p_workbook, sheet, p_job, trgLocale);

        // Add Locale Pair Header
        addLanguageHeader(p_workbook, sheet);

        // Add Quality Assessment
        addQuaAssessment(p_workbook, sheet);

        // Add Suitability for Market
        addSuitMarket(p_workbook, sheet);

        // Add Reviewer Comment
        addReviwerCommet(p_workbook, sheet);

        // Add Segment Header
        addSegmentHeader(p_workbook, sheet);

        // Create Name Areas for drop down list.
        createCategoryFailureNameArea(p_workbook);

        createQualityAssessmentNameArea(p_workbook);

        createMarketSuitabilityNameArea(p_workbook);

        // Insert Data into Report
        String srcLang = p_job.getSourceLocale().getDisplayName(m_uiLocale);
        String trgLang = trgLocale.getDisplayName(m_uiLocale);
        writeLanguageInfo(p_workbook, sheet, srcLang, trgLang);
        writeQualityAssessmentInfo(p_workbook, sheet, p_job, trgLocale);
        writeMarketSuitabilityInfo(p_workbook, sheet, p_job, trgLocale);
        writeReviewerComment(p_workbook, sheet);

        writeSegmentInfo(p_workbook, sheet, p_job, trgLocale, "", p_dateFormat,
                SEGMENT_START_ROW);

        addQualityAssessmentValidation(sheet, 6, 6, 1, 1);
        addMarketSuitabilityValidation(sheet, 7, 7, 1, 1);
    }

    private void addMarketSuitabilityValidation(Sheet p_sheet, int startRow,
            int lastRow, int startColumn, int lastColumn)
    {
        // Add category failure drop down list here.
        DataValidationHelper dvHelper = p_sheet.getDataValidationHelper();
        DataValidationConstraint dvConstraint = dvHelper
                .createFormulaListConstraint(MARKET_SUITABILITY_LIST);
        CellRangeAddressList addressList = new CellRangeAddressList(startRow,
                lastRow, startColumn, lastColumn);
        DataValidation validation = dvHelper.createValidation(dvConstraint,
                addressList);
        validation.setSuppressDropDownArrow(true);
        validation.setShowErrorBox(true);
        p_sheet.addValidationData(validation);
    }

    private void addQualityAssessmentValidation(Sheet p_sheet, int startRow,
            int lastRow, int startColumn, int lastColumn)
    {
        // Add category failure drop down list here.
        DataValidationHelper dvHelper = p_sheet.getDataValidationHelper();
        DataValidationConstraint dvConstraint = dvHelper
                .createFormulaListConstraint(QUALITY_ASSESSMENT_LIST);
        CellRangeAddressList addressList = new CellRangeAddressList(startRow,
                lastRow, startColumn, lastColumn);
        DataValidation validation = dvHelper.createValidation(dvConstraint,
                addressList);
        validation.setSuppressDropDownArrow(true);
        validation.setShowErrorBox(true);
        p_sheet.addValidationData(validation);
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
        Font titleFont = p_workBook.createFont();
        titleFont.setUnderline(Font.U_NONE);
        titleFont.setFontName("Times");
        titleFont.setFontHeightInPoints((short) 14);
        titleFont.setBoldweight(Font.BOLDWEIGHT_BOLD);
        CellStyle cs = p_workBook.createCellStyle();
        cs.setFont(titleFont);

        Row titleRow = getRow(p_sheet, 0);
        Cell titleCell = getCell(titleRow, 0);
        titleCell.setCellValue(m_bundle.getString("lb_post_review_qa_report"));
        titleCell.setCellStyle(cs);
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
    private void addHidenInfoForUpload(Workbook p_workbook, Sheet p_sheet,
            Job p_job, GlobalSightLocale p_targetLocale) throws Exception
    {
        String reportInfo = "";
        for (Workflow wf : p_job.getWorkflows())
        {
            if (p_targetLocale.getId() == wf.getTargetLocale().getId())
            {
                Collection tasks = ServerProxy.getTaskManager()
                        .getCurrentTasks(wf.getId());
                if (tasks != null)
                {
                    for (Iterator it = tasks.iterator(); it.hasNext();)
                    {
                        Task task = (Task) it.next();
                        reportInfo = ReportConstants.POST_REVIEW_REPORT_ABBREVIATION
                                + "_" + task.getId();
                    }
                }
            }
        }

        Row titleRow = getRow(p_sheet, 0);
        Cell taskIdCell = getCell(titleRow, 26);
        taskIdCell.setCellValue(reportInfo);
        taskIdCell.setCellStyle(contentStyle);

        p_sheet.setColumnHidden(26, true);
    }

    /**
     * Add language header to the sheet
     * 
     * @param p_workBook
     * @param p_sheet
     *            the sheet
     * @throws Exception
     */
    private void addLanguageHeader(Workbook p_workBook, Sheet p_sheet)
            throws Exception
    {
        int col = 0;
        int row = LANGUAGE_HEADER_ROW;

        Row langRow = getRow(p_sheet, row);
        Cell srcLangCell = getCell(langRow, col);
        srcLangCell.setCellValue(m_bundle.getString("lb_source_language"));
        srcLangCell.setCellStyle(getHeaderStyle(p_workBook));
        col++;

        Cell trgLangCell = getCell(langRow, col);
        trgLangCell.setCellValue(m_bundle.getString("lb_target_language"));
        trgLangCell.setCellStyle(getHeaderStyle(p_workBook));
    }

    private void addQuaAssessment(Workbook p_workBook, Sheet p_sheet)
            throws Exception
    {
        int col = 0;
        int row = 6;

        Row langRow = getRow(p_sheet, row);
        Cell quaAssessmentCell = getCell(langRow, col);
        quaAssessmentCell.setCellValue(m_bundle
                .getString("lb_quality_assessment"));
        quaAssessmentCell.setCellStyle(getHeaderStyle(p_workBook));
    }

    private void addSuitMarket(Workbook p_workBook, Sheet p_sheet)
            throws Exception
    {
        int col = 0;
        int row = 7;

        Row langRow = getRow(p_sheet, row);
        Cell marSuitCell = getCell(langRow, col);
        marSuitCell.setCellValue(m_bundle.getString("lb_market_suitability"));
        marSuitCell.setCellStyle(getHeaderStyle(p_workBook));
    }

    private void addReviwerCommet(Workbook p_workBook, Sheet p_sheet)
            throws Exception
    {
        int col = 0;
        int row = 8;

        Row langRow = getRow(p_sheet, row);
        Cell commentCell = getCell(langRow, col);
        commentCell.setCellValue(m_bundle.getString("lb_reviewer_comment"));
        commentCell.setCellStyle(getHeaderStyle(p_workBook));
    }

    /**
     * Add segment header to the sheet
     * 
     * @param p_workBook
     * @param p_sheet
     *            the sheet
     * @throws Exception
     */
    private void addSegmentHeader(Workbook p_workBook, Sheet p_sheet)
            throws Exception
    {
        int col = 0;
        int row = SEGMENT_HEADER_ROW;
        Row segHeaderRow = getRow(p_sheet, row);

        Cell cell_A = getCell(segHeaderRow, col);
        cell_A.setCellValue(m_bundle.getString("lb_source_segment"));
        cell_A.setCellStyle(getHeaderStyle(p_workBook));
        p_sheet.setColumnWidth(col, 40 * 256);
        col++;

        Cell cell_B = getCell(segHeaderRow, col);
        cell_B.setCellValue(m_bundle.getString("lb_original_target_segment"));
        cell_B.setCellStyle(getHeaderStyle(p_workBook));
        p_sheet.setColumnWidth(col, 40 * 256);
        col++;

        Cell cell_C = getCell(segHeaderRow, col);
        cell_C.setCellValue(m_bundle.getString("lb_updated_target_segment"));
        cell_C.setCellStyle(getHeaderStyle(p_workBook));
        p_sheet.setColumnWidth(col, 40 * 256);
        col++;

        Cell cell_D = getCell(segHeaderRow, col);
        cell_D.setCellValue(m_bundle.getString("lb_comments_history"));
        cell_D.setCellStyle(getHeaderStyle(p_workBook));
        p_sheet.setColumnWidth(col, 40 * 256);
        col++;

        Cell cell_E = getCell(segHeaderRow, col);
        cell_E.setCellValue(m_bundle.getString("lb_category_failure"));
        cell_E.setCellStyle(getHeaderStyle(p_workBook));
        p_sheet.setColumnWidth(col, 40 * 256);
        col++;

        Cell cell_F = getCell(segHeaderRow, col);
        cell_F.setCellValue(m_bundle.getString("lb_comment_status"));
        cell_F.setCellStyle(getHeaderStyle(p_workBook));
        p_sheet.setColumnWidth(col, 15 * 256);
        col++;

        Cell cell_G = getCell(segHeaderRow, col);
        cell_G.setCellValue(m_bundle.getString("lb_priority"));
        cell_G.setCellStyle(getHeaderStyle(p_workBook));
        p_sheet.setColumnWidth(col, 15 * 256);
        col++;

        Cell cell_H = getCell(segHeaderRow, col);
        cell_H.setCellValue(m_bundle.getString("lb_tm_match_original"));
        cell_H.setCellStyle(getHeaderStyle(p_workBook));
        p_sheet.setColumnWidth(col, 20 * 256);
        col++;

        Cell cell_I = getCell(segHeaderRow, col);
        cell_I.setCellValue(m_bundle.getString("lb_glossary_source"));
        cell_I.setCellStyle(getHeaderStyle(p_workBook));
        p_sheet.setColumnWidth(col, 25 * 256);
        col++;

        Cell cell_J = getCell(segHeaderRow, col);
        cell_J.setCellValue(m_bundle.getString("lb_glossary_target"));
        cell_J.setCellStyle(getHeaderStyle(p_workBook));
        p_sheet.setColumnWidth(col, 25 * 256);
        col++;

        Cell cell_K = getCell(segHeaderRow, col);
        cell_K.setCellValue(m_bundle.getString("lb_job_id_report"));
        cell_K.setCellStyle(getHeaderStyle(p_workBook));
        p_sheet.setColumnWidth(col, 15 * 256);
        col++;

        Cell cell_L = getCell(segHeaderRow, col);
        cell_L.setCellValue(m_bundle.getString("lb_segment_id"));
        cell_L.setCellStyle(getHeaderStyle(p_workBook));
        p_sheet.setColumnWidth(col, 15 * 256);
        col++;

        Cell cell_M = getCell(segHeaderRow, col);
        cell_M.setCellValue(m_bundle.getString("lb_page_name"));
        cell_M.setCellStyle(getHeaderStyle(p_workBook));
        p_sheet.setColumnWidth(col, 25 * 256);
        col++;

        Cell cell_N = getCell(segHeaderRow, col);
        cell_N.setCellValue(m_bundle.getString("lb_sid"));
        cell_N.setCellStyle(getHeaderStyle(p_workBook));
        p_sheet.setColumnWidth(col, 15 * 256);
        col++;
    }

    private void writeLanguageInfo(Workbook p_workbook, Sheet p_sheet,
            String p_sourceLang, String p_targetLang) throws Exception
    {
        int col = 0;
        int row = LANGUAGE_INFO_ROW;
        CellStyle contentStyle = getContentStyle(p_workbook);
        Row langInfoRow = getRow(p_sheet, row);

        // Source Language
        Cell srcLangCell = getCell(langInfoRow, col++);
        srcLangCell.setCellValue(p_sourceLang);
        srcLangCell.setCellStyle(contentStyle);

        // Target Language
        Cell trgLangCell = getCell(langInfoRow, col++);
        trgLangCell.setCellValue(p_targetLang);
        trgLangCell.setCellStyle(contentStyle);
    }

    private void writeQualityAssessmentInfo(Workbook p_workbook, Sheet p_sheet, Job p_job, GlobalSightLocale p_targetLocale)
            throws Exception
    {
        String qualityAssessment = null;
        int col = 1;
        int row = 6;
        CellStyle contentStyle = p_workbook.createCellStyle();
        contentStyle.cloneStyleFrom(getContentStyle(p_workbook));
        contentStyle.setBorderTop(CellStyle.BORDER_THIN);
        contentStyle.setBorderRight(CellStyle.BORDER_THIN);
        contentStyle.setBorderBottom(CellStyle.BORDER_THIN);
        contentStyle.setBorderLeft(CellStyle.BORDER_THIN);
        contentStyle.setLocked(false);
        Row langInfoRow = getRow(p_sheet, row);
        Cell qualityCell = getCell(langInfoRow, col++);
        for (Workflow wf : p_job.getWorkflows())
        {
            if (p_targetLocale.getId() == wf.getTargetLocale().getId())
            {
                Collection tasks = ServerProxy.getTaskManager()
                        .getCurrentTasks(wf.getId());
                if (tasks != null)
                {
                    for (Iterator it = tasks.iterator(); it.hasNext();)
                    {
                        Task task = (Task) it.next();
                        qualityAssessment = task.getQualityAssessment();
                    }
                }
            }
        }
        if (qualityAssessment != null && !"".endsWith(qualityAssessment))
        {
            qualityCell.setCellValue(qualityAssessment);
        }
        else
        {
            qualityCell.setCellValue("");
        }

        qualityCell.setCellStyle(contentStyle);

    }

    private void writeMarketSuitabilityInfo(Workbook p_workbook, Sheet p_sheet, Job p_job, GlobalSightLocale p_targetLocale)
            throws Exception
    {
        String marketSuitabilty = null;
        int col = 1;
        int row = 7;
        CellStyle contentStyle = p_workbook.createCellStyle();
        contentStyle.cloneStyleFrom(getContentStyle(p_workbook));
        contentStyle.setBorderTop(CellStyle.BORDER_THIN);
        contentStyle.setBorderRight(CellStyle.BORDER_THIN);
        contentStyle.setBorderBottom(CellStyle.BORDER_THIN);
        contentStyle.setBorderLeft(CellStyle.BORDER_THIN);
        contentStyle.setLocked(false);
        Row langInfoRow = getRow(p_sheet, row);
        Cell marketCell = getCell(langInfoRow, col++);
        for (Workflow wf : p_job.getWorkflows())
        {
            if (p_targetLocale.getId() == wf.getTargetLocale().getId())
            {
                Collection tasks = ServerProxy.getTaskManager()
                        .getCurrentTasks(wf.getId());
                if (tasks != null)
                {
                    for (Iterator it = tasks.iterator(); it.hasNext();)
                    {
                        Task task = (Task) it.next();
                        marketSuitabilty = task.getMarketSuitability();
                    }
                }
            }
        }
        if (marketSuitabilty != null && !"".endsWith(marketSuitabilty))
        {
            marketCell.setCellValue(marketSuitabilty);
        }
        else
        {
            marketCell.setCellValue("");
        }
        marketCell.setCellStyle(contentStyle);

    }

    private void writeReviewerComment(Workbook p_workbook, Sheet p_sheet)
            throws Exception
    {
        int col = 1;
        int row = 8;
        CellStyle contentStyle = p_workbook.createCellStyle();
        contentStyle.cloneStyleFrom(getContentStyle(p_workbook));
        contentStyle.setBorderTop(CellStyle.BORDER_THIN);
        contentStyle.setBorderRight(CellStyle.BORDER_THIN);
        contentStyle.setBorderBottom(CellStyle.BORDER_THIN);
        contentStyle.setBorderLeft(CellStyle.BORDER_THIN);
        contentStyle.setLocked(false);
        Row langInfoRow = getRow(p_sheet, row);

        Cell commentCell = getCell(langInfoRow, col++);
        commentCell.setCellValue("");
        commentCell.setCellStyle(contentStyle);

    }

    /**
     * For Post-Review QA Report, Write segment information into each row of the
     * sheet.
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
    @SuppressWarnings(
    { "rawtypes", "unchecked" })
    private int writeSegmentInfo(Workbook p_workBook, Sheet p_sheet, Job p_job,
            GlobalSightLocale p_targetLocale, String p_srcPageId,
            String p_dateFormat, int p_row) throws Exception
    {
        boolean review_only = false;
        Vector<TargetPage> targetPages = new Vector<TargetPage>();

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
                tmp = workflow.getJob().getL10nProfile()
                        .getTranslationMemoryProfile();
                if (tmp != null)
                {
                    excludItems = tmp.getJobExcludeTuTypes();
                }
                Collection tasks = ServerProxy.getTaskManager()
                        .getCurrentTasks(workflow.getId());
                if (tasks != null)
                {
                    for (Iterator it = tasks.iterator(); it.hasNext();)
                    {
                        Task task = (Task) it.next();
                        review_only = task.isType(Task.TYPE_REVIEW);
                        
                    }
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
            LeverageMatchLingManager lmLingManager = LingServerProxy
                    .getLeverageMatchLingManager();
            TermLeverageManager termLeverageManager = ServerProxy
                    .getTermLeverageManager();

            Locale sourcePageLocale = p_job.getSourceLocale().getLocale();
            Locale targetPageLocale = p_targetLocale.getLocale();
            TermLeverageOptions termLeverageOptions = getTermLeverageOptions(
                    sourcePageLocale, targetPageLocale, p_job.getL10nProfile()
                            .getProject().getTermbaseName(),
                    String.valueOf(p_job.getCompanyId()));
            Map<Long, Set<TermLeverageMatch>> termLeverageMatchResultMap = null;
            if (termLeverageOptions != null)
            {
                termLeverageMatchResultMap = termLeverageManager
                        .getTermMatchesForPages(p_job.getSourcePages(),
                                p_targetLocale);
            }

            String category = null;
            PseudoData pData = new PseudoData();
            pData.setMode(PseudoConstants.PSEUDO_COMPACT);
            String sid = null;
            Set<Integer> rowsWithCommentSet = new HashSet<Integer>();
            for (int i = 0; i < targetPages.size(); i++)
            {
                if (cancel)
                    return 0;

                TargetPage targetPage = (TargetPage) targetPages.get(i);
                SourcePage sourcePage = targetPage.getSourcePage();

                if (!"".equals(p_srcPageId)
                        && !p_srcPageId.equals(String.valueOf(sourcePage
                                .getId())))
                {
                    // ignore the source pages not equal to the one
                    // if the request comes from pop up editor
                    continue;
                }

                SegmentTuUtil.getTusBySourcePageId(sourcePage.getId());
                List sourceTuvs = SegmentTuvUtil.getSourceTuvs(sourcePage);
                List targetTuvs = SegmentTuvUtil.getTargetTuvs(targetPage);

                MatchTypeStatistics tuvMatchTypes = lmLingManager
                        .getMatchTypesForStatistics(sourcePage.getIdAsLong(),
                                targetPage.getLocaleId(),
                                p_job.getLeverageMatchThreshold());
                Map<Long, Set<LeverageMatch>> fuzzyLeverageMatchMap = lmLingManager
                        .getFuzzyMatches(sourcePage.getIdAsLong(), new Long(
                                targetPage.getLocaleId()));
                Map<Long, Tuv> allTuvMap = this.getAllTuvsMap(targetPage);

                sourcePageLocale = sourcePage.getGlobalSightLocale()
                        .getLocale();
                targetPageLocale = targetPage.getGlobalSightLocale()
                        .getLocale();

                boolean m_rtlSourceLocale = EditUtil
                        .isRTLLocale(sourcePageLocale.toString());
                boolean m_rtlTargetLocale = EditUtil
                        .isRTLLocale(targetPageLocale.toString());

                // Find segment all comments belong to this target page
                Map<Long, IssueImpl> issuesMap = CommentHelper
                        .getIssuesMap(targetPage.getId());

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
                    List<IssueHistory> issueHistories = null;
                    String commentHistory = "";
                    String failure = "";
                    String commentStatus = "";
                    String lastComment = "";
                    String priority = "";
                    Issue issue = issuesMap.get(targetTuv.getId());
                    if (issue != null)
                    {
                        issueHistories = issue.getHistory();
                        failure = issue.getCategory();
                        commentStatus = issue.getStatus();
                        priority = issue.getPriority();
                    }
                    if (issueHistories != null && issueHistories.size() > 0)
                    {
                        for (IssueHistory issueHistory : issueHistories)
                        {
                            commentHistory += issueHistory.getComment() + "\r";
                        }
                        lastComment = issueHistories.get(0).getComment();
                    }

                    sid = sourceTuv.getSid();

                    StringBuilder matches = getMatches(fuzzyLeverageMatchMap,
                            tuvMatchTypes, excludItems, sourceTuvs, targetTuvs,
                            sourceTuv, targetTuv, p_job.getId());

                    // Get Terminology/Glossary Source and Target.
                    String sourceTerms = "";
                    String targetTerms = "";
                    if (termLeverageMatchResultMap != null
                            && termLeverageMatchResultMap.size() > 0)
                    {
                        Set<TermLeverageMatch> termLeverageMatchSet = termLeverageMatchResultMap
                                .get(sourceTuv.getId());
                        if (termLeverageMatchSet != null
                                && termLeverageMatchSet.size() > 0)
                        {
                            TermLeverageMatch tlm = termLeverageMatchSet
                                    .iterator().next();
                            sourceTerms = tlm.getMatchedSourceTerm();
                            targetTerms = tlm.getMatchedTargetTerm();
                        }
                    }

                    Row currentRow = getRow(p_sheet, p_row);

                    // Source segment with compact tags
                    CellStyle srcStyle = m_rtlSourceLocale ? getRtlContentStyle(p_workBook)
                            : getContentStyle(p_workBook);
                    Cell cell_A = getCell(currentRow, col);
                    cell_A.setCellValue(getSegment(pData, sourceTuv,
                            m_rtlSourceLocale, p_job.getId()));
                    cell_A.setCellStyle(srcStyle);
                    col++;

                    // Target segment with compact tags
                    CellStyle trgStyle = m_rtlTargetLocale ? getRtlContentStyle(p_workBook)
                            : getContentStyle(p_workBook);
                    String previousSegments = getPreviousSegments(allTuvMap,
                            targetTuv.getId(), targetTuv, p_job.getId(), pData);
                    Cell cell_B = getCell(currentRow, col);
                    cell_B.setCellValue(previousSegments);
                    cell_B.setCellStyle(trgStyle);
                    col++;

                    // Modify the translation
                  CellStyle modifyTranslationStyle = m_rtlTargetLocale ?
                     getUnlockedRightStyle(p_workBook): getUnlockedStyle(p_workBook);
                     modifyTranslationStyle.setLocked(false);
                    Cell cell_C = getCell(currentRow, col);
                    cell_C.setCellValue(getSegment(pData, targetTuv,
                            m_rtlTargetLocale, p_job.getId()));
                    if (review_only)
                    {
                        modifyTranslationStyle.setLocked(true);
                    }
                    cell_C.setCellStyle(modifyTranslationStyle);
                    col++;

                    // Reviewers Comments
                    Cell cell_D = getCell(currentRow, col);
                    cell_D.setCellValue(commentHistory);
                    cell_D.setCellStyle(getContentStyle(p_workBook));
                    col++;

                    // Category failure
                    Cell cell_E = getCell(currentRow, col);
                    cell_E.setCellValue(failure);
                    cell_E.setCellStyle(getContentStyle(p_workBook));
                    col++;

                    // Comment Status
                    Cell cell_F = getCell(currentRow, col);
                    cell_F.setCellValue(commentStatus);
                    CellStyle commentCS = p_workBook.createCellStyle();
                    commentCS.cloneStyleFrom(getContentStyle(p_workBook));
//                    commentCS.setLocked(false);
                    cell_F.setCellStyle(commentCS);
                    // add comment status drop down list for current row.
                    String[] statusArray = getCommentStatusList(lastComment);
                    if (statusArray.length > 1)
                    {
                        rowsWithCommentSet.add(p_row);
                    }
                    col++;

                    // Priority
                    Cell cell_G = getCell(currentRow, col);
                    cell_G.setCellValue(priority);
                    cell_G.setCellStyle(getContentStyle(p_workBook));
                    col++;

                    // TM match
                    Cell cell_H = getCell(currentRow, col);
                    cell_H.setCellValue(matches.toString());
                    cell_H.setCellStyle(getContentStyle(p_workBook));
                    col++;

                    // Glossary source
                    Cell cell_I = getCell(currentRow, col);
                    cell_I.setCellValue(sourceTerms);
                    cell_I.setCellStyle(getContentStyle(p_workBook));
                    col++;

                    // Glossary target
                    Cell cell_J = getCell(currentRow, col);
                    cell_J.setCellValue(targetTerms);
                    cell_J.setCellStyle(getContentStyle(p_workBook));
                    col++;

                    // Job Id
                    Cell cell_K = getCell(currentRow, col);
                    cell_K.setCellValue(p_job.getId());
                    cell_K.setCellStyle(getContentStyle(p_workBook));
                    col++;

                    // Segment id
                    Cell cell_L = getCell(currentRow, col);
                    cell_L.setCellValue(sourceTuv.getTu(p_job.getId()).getId());
                    cell_L.setCellStyle(getContentStyle(p_workBook));
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
                    Cell cell_M = getCell(currentRow, col);
                    cell_M.setCellValue(name);
                    cell_M.setCellStyle(getContentStyle(p_workBook));
                    col++;

                    // SID
                    Cell cell_N = getCell(currentRow, col);
                    cell_N.setCellValue(sid);
                    cell_N.setCellStyle(getContentStyle(p_workBook));
                    col++;

                    p_row++;
                }
            }
            // Add comment status
            addCommentStatus(p_sheet, rowsWithCommentSet, p_row);
            // Add category failure drop down list here.
            addCategoryFailureValidation(p_sheet, SEGMENT_START_ROW, p_row - 1,
                    CATEGORY_FAILURE_COLUMN, CATEGORY_FAILURE_COLUMN);
            addPriority(p_sheet, SEGMENT_START_ROW, p_row - 1, PRIORITY_COLUMN,
                    PRIORITY_COLUMN);
        }

        return p_row;
    }

    private String getPreviousSegments(Map<Long, Tuv> allTargetTuvsMap,
            long p_trgTuvId, Tuv tuv, long p_jobId, PseudoData pData)
            throws Exception
    {
        List previousTaskTuvs = new ArrayList();
        TuvManager tuvManager = ServerProxy.getTuvManager();
        previousTaskTuvs = tuvManager.getPreviousTaskTuvs(p_trgTuvId, 10);
        String previousSegments = "";
        String previousSegment = null;
        String targetSegmentString = null;
        String dataType = null;
        List previous = new ArrayList();
        dataType = tuv.getDataType(p_jobId);
        pData.setAddables(dataType);
        TmxPseudo.tmx2Pseudo(tuv.getGxmlExcludeTopTags(), pData);
        targetSegmentString = (pData.getPTagSourceString());
        if (previousTaskTuvs.size() > 0)
        {
            SortUtil.sort(previousTaskTuvs, new Comparator()
            {
                public int compare(Object p_taskTuvA, Object p_taskTuvB)
                {
                    TaskTuv a = (TaskTuv) p_taskTuvA;
                    TaskTuv b = (TaskTuv) p_taskTuvB;
                    return a.getTask().getCompletedDate()
                            .compareTo(b.getTask().getCompletedDate());
                }
            });

            int len = previousTaskTuvs.size() - 2;
            int lastReviewerCount = 0;

            while ((len >= 0)
                    && (((TaskTuv) previousTaskTuvs.get(len)).getTask()
                            .getType() == Task.TYPE_REVIEW))
            {
                // Review only task does not change segments
                lastReviewerCount++;
                len--;
            }

            int beforeLastReviewerCount = previousTaskTuvs.size()
                    - lastReviewerCount - 1;

            if (beforeLastReviewerCount == 1)
            {
                String previousSeg = "";
                TaskTuv taskTuv = (TaskTuv) previousTaskTuvs.get(0);
                Tuv previousTuv = allTargetTuvsMap.get(taskTuv
                        .getPreviousTuvId());
                // generally it should not be null for performance
                if (previousTuv != null)
                {
                    dataType = previousTuv.getDataType(p_jobId);
                    pData.setAddables(dataType);
                    TmxPseudo.tmx2Pseudo(previousTuv.getGxmlExcludeTopTags(), pData);
                    previousSeg = pData.getPTagSourceString();
                }
                else
                {
                    dataType = taskTuv.getTuv(p_jobId).getDataType(p_jobId);
                    pData.setAddables(dataType);
                    TmxPseudo.tmx2Pseudo(taskTuv.getTuv(p_jobId).getGxmlExcludeTopTags(), pData);
                    previousSeg = pData.getPTagSourceString();
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
                    Tuv previousTuv = allTargetTuvsMap.get(taskTuv
                            .getPreviousTuvId());
                    // generally it should not be null for performance
                    if (previousTuv != null)
                    {
                        dataType = previousTuv.getDataType(p_jobId);
                        pData.setAddables(dataType);
                        TmxPseudo.tmx2Pseudo(previousTuv.getGxmlExcludeTopTags(), pData);
                        previousSegment = pData.getPTagSourceString();
                    }
                    else
                    {
                        dataType = taskTuv.getTuv(p_jobId).getDataType(p_jobId);
                        pData.setAddables(dataType);
                        TmxPseudo.tmx2Pseudo(taskTuv.getTuv(p_jobId).getGxmlExcludeTopTags(), pData);
                        previousSegment = pData.getPTagSourceString();
                    }
                    if (!previous.contains(previousSegment))
                    {
                        previous.add(previousSegment);
                    }
                }
                String lastInPrevious = (String) previous
                        .get(previous.size() - 1);
                if (lastInPrevious.equals(targetSegmentString))
                {
                    previous.remove(previous.size() - 1);
                }

                for (int pi = 0; pi < previous.size(); pi++)
                {
                    previousSegment = (String) previous.get(pi);
                    if (previous.size() > 1)
                    {
                        previousSegments = previousSegments + "{"
                                + previousSegment + "}\r\n";
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

    private void addCommentStatus(Sheet p_sheet,
            Set<Integer> rowsWithCommentSet, int last_row)
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
            addCommentStatusValidation(p_sheet, dvHelper, dvConstraintOne,
                    addressListOne);
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
                        cellAddress = new CellRangeAddress(startRow, endRow,
                                COMMENT_STATUS_COLUMN, COMMENT_STATUS_COLUMN);
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
                        cellAddress = new CellRangeAddress(startRow, endRow,
                                COMMENT_STATUS_COLUMN, COMMENT_STATUS_COLUMN);
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

            addCommentStatusValidation(p_sheet, dvHelper, dvConstraintAll,
                    addressListAll);
            addCommentStatusValidation(p_sheet, dvHelper, dvConstraintOne,
                    addressListOne);
        }
    }

    /**
     * Populates a term leverage options object.
     */
    private TermLeverageOptions getTermLeverageOptions(Locale p_sourceLocale,
            Locale p_targetLocale, String p_termbaseName, String p_companyId)
            throws Exception
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
                termbase = manager.connect(p_termbaseName,
                        ITermbase.SYSTEM_USER, "", p_companyId);
            }
            else
            {
                termbase = manager.connect(p_termbaseName,
                        ITermbase.SYSTEM_USER, "");
            }

            // add source locale and lang names
            options.setSourcePageLocale(sourceLocale);
            ArrayList sourceLangNames = termbase
                    .getLanguagesByLocale(sourceLocale.toString());

            for (int i = 0, max = sourceLangNames.size(); i < max; i++)
            {
                String langName = (String) sourceLangNames.get(i);

                options.addSourcePageLocale2LangName(langName);
            }

            // add target locales and lang names
            ArrayList targetLangNames = termbase
                    .getLanguagesByLocale(targetLocale.toString());
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

    private CellStyle getHeaderStyle(Workbook p_workbook) throws Exception
    {
        if (headerStyle == null)
        {
            Font font = p_workbook.createFont();
            font.setBoldweight(Font.BOLDWEIGHT_BOLD);
            font.setColor(IndexedColors.BLACK.getIndex());
            font.setUnderline(Font.U_NONE);
            font.setFontName("Times");
            font.setFontHeightInPoints((short) 11);

            CellStyle cs = p_workbook.createCellStyle();
            cs.setFont(font);
            cs.setWrapText(true);
            cs.setFillPattern(CellStyle.SOLID_FOREGROUND);
            cs.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            cs.setBorderTop(CellStyle.BORDER_THIN);
            cs.setBorderRight(CellStyle.BORDER_THIN);
            cs.setBorderBottom(CellStyle.BORDER_THIN);
            cs.setBorderLeft(CellStyle.BORDER_THIN);

            headerStyle = cs;
        }

        return headerStyle;
    }

    private CellStyle getContentStyle(Workbook p_workbook) throws Exception
    {
        if (contentStyle == null)
        {
            CellStyle style = p_workbook.createCellStyle();
            style.setWrapText(true);
            style.setAlignment(CellStyle.ALIGN_LEFT);
            style.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
            Font font = p_workbook.createFont();
            font.setFontName("Arial");
            font.setFontHeightInPoints((short) 10);
            style.setFont(font);

            contentStyle = style;
        }

        return contentStyle;
    }

    private CellStyle getRtlContentStyle(Workbook p_workbook) throws Exception
    {
        if (rtlContentStyle == null)
        {
            Font font = p_workbook.createFont();
            font.setFontName("Arial");
            font.setFontHeightInPoints((short) 10);

            CellStyle style = p_workbook.createCellStyle();
            style.setFont(font);
            style.setWrapText(true);
            style.setAlignment(CellStyle.ALIGN_RIGHT);
            style.setVerticalAlignment(CellStyle.VERTICAL_CENTER);

            rtlContentStyle = style;
        }

        return rtlContentStyle;
    }

    private CellStyle getUnlockedStyle(Workbook p_workbook) throws Exception
    {
        if (unlockedStyle == null)
        {
            Font font = p_workbook.createFont();
            font.setFontName("Arial");
            font.setFontHeightInPoints((short) 10);

            CellStyle style = p_workbook.createCellStyle();
            style.setFont(font);
            style.setLocked(false);
            style.setWrapText(true);
            style.setAlignment(CellStyle.ALIGN_LEFT);
            style.setVerticalAlignment(CellStyle.VERTICAL_CENTER);

            unlockedStyle = style;
        }

        return unlockedStyle;
    }

    private CellStyle getUnlockedRightStyle(Workbook p_workbook)
            throws Exception
    {
        if (unlockedRightStyle == null)
        {
            Font font = p_workbook.createFont();
            font.setFontName("Arial");
            font.setFontHeightInPoints((short) 10);

            CellStyle style = p_workbook.createCellStyle();
            style.setFont(font);
            style.setLocked(false);
            style.setWrapText(true);
            style.setAlignment(CellStyle.ALIGN_RIGHT);
            style.setVerticalAlignment(CellStyle.VERTICAL_CENTER);

            unlockedRightStyle = style;
        }

        return unlockedRightStyle;
    }

    private List<String> getFailureCategoriesList()
    {
        List<String> result = new ArrayList<String>();

        String currentCompanyId = CompanyThreadLocal.getInstance().getValue();
        List failureCategories = IssueOptions.getAllCategories(m_bundle,
                currentCompanyId);
        for (int i = 0; i < failureCategories.size(); i++)
        {
            Select aCategory = (Select) failureCategories.get(i);
            result.add(aCategory.getValue());
        }

        return result;
    }

    private List<String> getQualityAssessmentList()
    {
        List<String> result = new ArrayList<String>();

        String currentCompanyId = CompanyThreadLocal.getInstance().getValue();
        List qualityCategories = IssueOptions.getAllQualityCategories(m_bundle,
                currentCompanyId);
        for (int i = 0; i < qualityCategories.size(); i++)
        {
            Select aCategory = (Select) qualityCategories.get(i);
            result.add(aCategory.getValue());
        }

        return result;
    }

    private List<String> getMarketSuitabilityList()
    {
        List<String> result = new ArrayList<String>();

        String currentCompanyId = CompanyThreadLocal.getInstance().getValue();
        List marketCategories = IssueOptions.getAllMarketCategories(m_bundle,
                currentCompanyId);
        for (int i = 0; i < marketCategories.size(); i++)
        {
            Select aCategory = (Select) marketCategories.get(i);
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
    private void writeBlank(Sheet p_sheet, int p_row, int p_colLen)
            throws Exception
    {
        for (int col = 0; col < p_colLen; col++)
        {
            Row row = p_sheet.getRow(p_row);
            Cell cell = getCell(row, col);
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
        return ReportConstants.POST_REVIEW_QA_REPORT;
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

    private Sheet getSheet(Workbook p_workbook, int index)
    {
        Sheet sheet = p_workbook.getSheetAt(index);
        if (sheet == null)
            sheet = p_workbook.createSheet();
        return sheet;
    }

    private Row getRow(Sheet p_sheet, int p_col)
    {
        Row row = p_sheet.getRow(p_col);
        if (row == null)
            row = p_sheet.createRow(p_col);
        return row;
    }

    private Cell getCell(Row p_row, int index)
    {
        Cell cell = p_row.getCell(index);
        if (cell == null)
            cell = p_row.createCell(index);
        return cell;
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
            // Ensure the name area is written only one time,otherwise it has
            // problem when open generated excel file.
            if (p_workbook.getNumberOfSheets() == 1)
            {
                Sheet firstSheet = getSheet(p_workbook, 0);
                List<String> categories = getFailureCategoriesList();
                // Set the categories in "AA" column, starts with row 8.
                int col = 26;
                for (int i = 0; i < categories.size(); i++)
                {
                    Row row = getRow(firstSheet, SEGMENT_START_ROW + i);
                    Cell cell = getCell(row, col);
                    cell.setCellValue(categories.get(i));
                }

                String formula = firstSheet.getSheetName() + "!$AA$"
                        + (SEGMENT_START_ROW + 1) + ":$AA$"
                        + (SEGMENT_START_ROW + categories.size());
                Name name = p_workbook.createName();
                name.setRefersToFormula(formula);
                name.setNameName(CATEGORY_FAILURE_DROP_DOWN_LIST);

                // Hide "AA" column
                firstSheet.setColumnHidden(26, true);
            }
        }
        catch (Exception e)
        {
            logger.error(
                    "Error when create hidden area for category failures.", e);
        }
    }

    private void createQualityAssessmentNameArea(Workbook p_workbook)
    {
        try
        {
            // Ensure the name area is written only one time,otherwise it has
            // problem when open generated excel file.
            if (p_workbook.getNumberOfSheets() == 1)
            {
                Sheet firstSheet = getSheet(p_workbook, 0);
                List<String> qualityCategories = getQualityAssessmentList();
                // Set the categories in "AA" column, starts with row 8.
                int col = 27;
                for (int i = 0; i < qualityCategories.size(); i++)
                {
                    Row row = getRow(firstSheet, SEGMENT_START_ROW + i);
                    Cell cell = getCell(row, col);
                    cell.setCellValue(qualityCategories.get(i));
                }

                String formula = firstSheet.getSheetName() + "!$AB$"
                        + (SEGMENT_START_ROW + 1) + ":$AB$"
                        + (SEGMENT_START_ROW + qualityCategories.size());
                Name name = p_workbook.createName();
                name.setRefersToFormula(formula);
                name.setNameName(QUALITY_ASSESSMENT_LIST);

                // Hide "AB" column
                firstSheet.setColumnHidden(27, true);
            }
        }
        catch (Exception e)
        {
            logger.error(
                    "Error when create hidden area for category failures.", e);
        }
    }

    private void createMarketSuitabilityNameArea(Workbook p_workbook)
    {
        try
        {
            // Ensure the name area is written only one time,otherwise it has
            // problem when open generated excel file.
            if (p_workbook.getNumberOfSheets() == 1)
            {
                Sheet firstSheet = getSheet(p_workbook, 0);
                List<String> marketCategories = getMarketSuitabilityList();
                // Set the categories in "AC" column, starts with row 11.
                int col = 28;
                for (int i = 0; i < marketCategories.size(); i++)
                {
                    Row row = getRow(firstSheet, SEGMENT_START_ROW + i);
                    Cell cell = getCell(row, col);
                    cell.setCellValue(marketCategories.get(i));
                }

                String formula = firstSheet.getSheetName() + "!$AC$"
                        + (SEGMENT_START_ROW + 1) + ":$AC$"
                        + (SEGMENT_START_ROW + marketCategories.size());
                Name name = p_workbook.createName();
                name.setRefersToFormula(formula);
                name.setNameName(MARKET_SUITABILITY_LIST);

                // Hide "AC" column
                firstSheet.setColumnHidden(28, true);
            }
        }
        catch (Exception e)
        {
            logger.error(
                    "Error when create hidden area for category failures.", e);
        }
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

    private void addCommentStatusValidation(Sheet p_sheet,
            DataValidationHelper dvHelper,
            DataValidationConstraint dvConstraint,
            CellRangeAddressList addressList)
    {
        if (addressList == null || addressList.countRanges() == 0)
            return;

        DataValidation validationOne = dvHelper.createValidation(dvConstraint,
                addressList);
        validationOne.setSuppressDropDownArrow(true);
        validationOne.setShowErrorBox(true);
        p_sheet.addValidationData(validationOne);
    }

    /**
     * Add category failure drop down list.
     * 
     * @param p_sheet
     * @param startRow
     * @param lastRow
     * @param startColumn
     * @param lastColumn
     */
    private void addCategoryFailureValidation(Sheet p_sheet, int startRow,
            int lastRow, int startColumn, int lastColumn)
    {
        // Add category failure drop down list here.
        DataValidationHelper dvHelper = p_sheet.getDataValidationHelper();
        DataValidationConstraint dvConstraint = dvHelper
                .createFormulaListConstraint(CATEGORY_FAILURE_DROP_DOWN_LIST);
        CellRangeAddressList addressList = new CellRangeAddressList(startRow,
                lastRow, startColumn, lastColumn);
        DataValidation validation = dvHelper.createValidation(dvConstraint,
                addressList);
        validation.setSuppressDropDownArrow(true);
        validation.setShowErrorBox(true);
        p_sheet.addValidationData(validation);
    }

    private void addPriority(Sheet p_sheet, int startRow, int lastRow,
            int startColumn, int lastColumn)
    {
        // Add category failure drop down list here.
        HashMap priorities = new HashMap();
        DataValidationHelper dvHelper = p_sheet.getDataValidationHelper();
        priorities.putAll(IssueOptions.getAllPriorities());
        String[] allpriorities = new String[priorities.size()];
        priorities.values().toArray(allpriorities);
        DataValidationConstraint dvConstraint = dvHelper
                .createExplicitListConstraint(allpriorities);
        CellRangeAddressList addressList = new CellRangeAddressList(startRow,
                lastRow, startColumn, lastColumn);
        DataValidation validation = dvHelper.createValidation(dvConstraint,
                addressList);
        validation.setSuppressDropDownArrow(true);
        validation.setShowErrorBox(true);
        p_sheet.addValidationData(validation);
    }

    /**
     * Get TM matches.
     */
    private StringBuilder getMatches(Map fuzzyLeverageMatchMap,
            MatchTypeStatistics tuvMatchTypes,
            Vector<String> excludedItemTypes, List sourceTuvs, List targetTuvs,
            Tuv sourceTuv, Tuv targetTuv, long p_jobId)
    {
        StringBuilder matches = new StringBuilder();

        Set fuzzyLeverageMatches = (Set) fuzzyLeverageMatchMap.get(sourceTuv
                .getIdAsLong());
        if (LeverageUtil.isIncontextMatch(sourceTuv, sourceTuvs, targetTuvs,
                tuvMatchTypes, excludedItemTypes, p_jobId))
        {
            matches.append(m_bundle.getString("lb_in_context_match"));
        }
        else if (LeverageUtil.isExactMatch(sourceTuv, tuvMatchTypes))
        {
            matches.append(StringUtil.formatPCT(100));
        }
        else if (fuzzyLeverageMatches != null)
        {
            int count = 0;
            for (Iterator ite = fuzzyLeverageMatches.iterator(); ite.hasNext();)
            {
                LeverageMatch leverageMatch = (LeverageMatch) ite.next();
                if ((fuzzyLeverageMatches.size() > 1))
                {
                    matches.append(++count)
                            .append(", ")
                            .append(StringUtil.formatPCT(leverageMatch
                                    .getScoreNum())).append("\r\n");
                }
                else
                {
                    matches.append(StringUtil.formatPCT(leverageMatch
                            .getScoreNum()));
                    break;
                }
            }
        }
        else
        {
            matches.append(m_bundle.getString("lb_no_match_report"));
        }

        if (targetTuv.isRepeated())
        {
            matches.append("\r\n")
                    .append(m_bundle
                            .getString("jobinfo.tradosmatches.invoice.repeated"));
        }
        else if (targetTuv.getRepetitionOfId() > 0)
        {
            matches.append("\r\n")
                    .append(m_bundle
                            .getString("jobinfo.tradosmatches.invoice.repetition"));
        }

        return matches;
    }

    private String getSegment(PseudoData pData, Tuv tuv, boolean m_rtlLocale,
            long p_jobId)
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
                    content.append("\r\n#").append(tuId).append(":")
                            .append(subId).append("\n")
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

    private String getCompactPtagString(GxmlElement p_gxmlElement,
            String p_dataType)
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

        return ReportHelper.getReportFile(p_reportType, p_job,
                ReportConstants.EXTENSION_XLSX, langInfo);
    }

    public void setUserId(String p_userId)
    {
        this.m_userId = p_userId;
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
}
