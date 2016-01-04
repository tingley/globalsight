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
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import com.globalsight.everest.comment.Issue;
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
import com.globalsight.everest.webapp.pagehandler.administration.reports.bo.ReportsData;
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
public class ReviewersCommentsSimpleReportGenerator implements ReportGenerator,
        Cancelable
{
    static private final Logger logger = Logger
            .getLogger(ReviewersCommentsSimpleReportGenerator.class);
    
    private static final String CATEGORY_FAILURE_DROP_DOWN_LIST = "categoryFailureDropDownList";

    private CellStyle contentStyle = null;
    private CellStyle iceContentStyle = null;
    private CellStyle exactContentStyle = null; 
    private CellStyle rtlContentStyle = null;
    private CellStyle iceRtlContentStyle = null;
    private CellStyle exactRtlContentStyle = null;
    private CellStyle headerStyle = null;
    private CellStyle unlockedStyle = null;
    private CellStyle iceUnlockedStyle = null;
    private CellStyle exactUnlockedStyle = null;
    private CellStyle unlockedRightStyle = null;
    private CellStyle iceUnlockedRightStyle = null;
    private CellStyle exactUnlockedRightStyle = null;
    private boolean isIncludeCompactTags = false;

    protected String m_companyName = "";
    protected List<Long> m_jobIDS = new ArrayList<Long>();
    protected List<GlobalSightLocale> m_targetLocales = new ArrayList<GlobalSightLocale>();

    // Is Run setPercent/isCancelled function or not.
    private boolean m_isCalculatePercent;

    public static final int LANGUAGE_HEADER_ROW = 3;
    public static final int LANGUAGE_INFO_ROW = 4;
    public static final int SEGMENT_HEADER_ROW = 6;
    public static final int SEGMENT_START_ROW = 7;
    // "D" column, index 4
    public static final int CATEGORY_FAILURE_COLUMN = 3;

    private Locale m_uiLocale = Locale.US;
    String m_userId;
    private ResourceBundle m_bundle;
    private String m_dateFormat;
    private boolean cancel = false;

    public ReviewersCommentsSimpleReportGenerator(String p_cureentCompanyName)
    {
        m_uiLocale = Locale.US;
        m_companyName = p_cureentCompanyName;
        CompanyThreadLocal.getInstance().setValue(m_companyName);
        m_bundle = SystemResourceBundle.getInstance().getResourceBundle(
                ResourceBundleConstants.LOCALE_RESOURCE_NAME, m_uiLocale);
        m_dateFormat = DEFAULT_DATE_FORMAT;

        m_isCalculatePercent = false;
    }
    
    public ReviewersCommentsSimpleReportGenerator(String p_cureentCompanyName, 
    		boolean p_includeCompactTags, String p_userId)
    {
        m_uiLocale = Locale.US;
        m_companyName = p_cureentCompanyName;
        CompanyThreadLocal.getInstance().setValue(m_companyName);
        m_bundle = SystemResourceBundle.getInstance().getResourceBundle(
                ResourceBundleConstants.LOCALE_RESOURCE_NAME, m_uiLocale);
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
    public ReviewersCommentsSimpleReportGenerator(HttpServletRequest p_request,
            HttpServletResponse p_response) throws Exception
    {
        HttpSession session = p_request.getSession();
        m_userId = (String) session.getAttribute(WebAppConstants.USER_NAME);
        if (p_request.getSession().getAttribute(WebAppConstants.UILOCALE) != null)
        {
            m_uiLocale = (Locale) p_request.getSession().getAttribute(
                    WebAppConstants.UILOCALE);
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
            m_companyName = CompanyWrapper.getCompanyNameById(job.getCompanyId());
        }
        CompanyThreadLocal.getInstance().setValue(m_companyName);
        
        m_isCalculatePercent = true;

        String withCompactTags = p_request.getParameter("withCompactTagsSimple");
        if("on".equals(withCompactTags))
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
    public void sendReports(List<Long> p_jobIDS,
            List<GlobalSightLocale> p_targetLocales,
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
    public File[] generateReports(List<Long> p_jobIDS,
            List<GlobalSightLocale> p_targetLocales) throws Exception
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

			if (m_userId == null) {
				m_userId = job.getCreateUserId();
			}

			setAllCellStyleNull();

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
    	boolean categoryFailureDropDownAdded = false;
        List<GlobalSightLocale> jobTL = ReportHelper.getTargetLocals(p_job);
        for (GlobalSightLocale trgLocale : p_targetLocales)
        {
            if (!jobTL.contains(trgLocale))
                continue;

            if (cancel)
                return;

            // Create Report Template
            Sheet sheet = p_workbook.createSheet(trgLocale.toString());
            sheet.protectSheet("");

            // Add Title
            addTitle(p_workbook, sheet);

            // Add hidden info "RCSR_taskID" for uploading.
            addHidenInfoForUpload(p_workbook, sheet, p_job, trgLocale);

            // Add Locale Pair Header
            addLanguageHeader(p_workbook, sheet);

            // Add Segment Header
            addSegmentHeader(p_workbook, sheet);
            
            // Create Name Areas for drop down list.
            if (!categoryFailureDropDownAdded)
            {
                createCategoryFailureNameArea(p_workbook);
                categoryFailureDropDownAdded = true;
            }

            // Insert Language Data
            String srcLang = p_job.getSourceLocale().getDisplayName(m_uiLocale);
            String trgLang = trgLocale.getDisplayName(m_uiLocale);
            writeLanguageInfo(p_workbook, sheet, srcLang, trgLang);

            // Insert Segment Data
            writeSegmentInfo(p_workbook, sheet, p_job, trgLocale, SEGMENT_START_ROW);
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
        Font titleFont = p_workBook.createFont();
        titleFont.setUnderline(Font.U_NONE);
        titleFont.setFontName("Times");
        titleFont.setFontHeightInPoints((short) 14);
        titleFont.setBoldweight(Font.BOLDWEIGHT_BOLD);
        CellStyle cs = p_workBook.createCellStyle();
        cs.setFont(titleFont);

        Row titleRow = getRow(p_sheet, 0);
        Cell titleCell = getCell(titleRow, 0);
        titleCell.setCellValue(m_bundle.getString("review_reviewers_comments_simple"));
        titleCell.setCellStyle(cs);
    }

    /**
     * Add hidden info "RCSR_taskID" for offline uploading. When upload, system
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
                        reportInfo = ReportConstants.REVIEWERS_COMMENTS_SIMPLE_REPORT_ABBREVIATION
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
     * Add job header to the sheet
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

    /**
     * Add segment header to the sheet for Reviewers Comments Report
     * 
     * @param p_sheet
     *            the sheet
     * @param p_row
     *            start row number
     * @throws Exception
     */
    private void addSegmentHeader(Workbook p_workBook, Sheet p_sheet)
            throws Exception
    {
        int col = 0;
        int row = SEGMENT_HEADER_ROW;
        Row segHeaderRow = getRow(p_sheet, row);
        CellStyle headerStyle = getHeaderStyle(p_workBook);
        
        Cell cell_A = getCell(segHeaderRow, col);
        cell_A.setCellValue(m_bundle.getString("lb_source_segment"));
        cell_A.setCellStyle(headerStyle);
        p_sheet.setColumnWidth(col, 40 * 256);
        col++;

        Cell cell_B = getCell(segHeaderRow, col);
        cell_B.setCellValue(m_bundle.getString("lb_target_segment"));
        cell_B.setCellStyle(headerStyle);
        p_sheet.setColumnWidth(col, 40 * 256);
        col++;

        Cell cell_C = getCell(segHeaderRow, col);
        cell_C.setCellValue(m_bundle.getString("reviewers_comments_header"));
        cell_C.setCellStyle(headerStyle);
        p_sheet.setColumnWidth(col, 40 * 256);
        col++;
        
        Cell cell_D = getCell(segHeaderRow, col);
        cell_D.setCellValue(m_bundle.getString("lb_category_failure"));
        cell_D.setCellStyle(headerStyle);
        p_sheet.setColumnWidth(col, 40 * 256);
        col++;
        
        Cell cell_E = getCell(segHeaderRow, col);
        cell_E.setCellValue(m_bundle.getString("lb_approved_glossary_source"));
        cell_E.setCellStyle(headerStyle);
        p_sheet.setColumnWidth(col, 30 * 256);
        col++;

        Cell cell_F = getCell(segHeaderRow, col);
        cell_F.setCellValue(m_bundle.getString("lb_approved_glossary_target"));
        cell_F.setCellStyle(headerStyle);
        p_sheet.setColumnWidth(col, 30 * 256);
        col++;
        
        Cell cell_G = getCell(segHeaderRow, col);
        cell_G.setCellValue(m_bundle.getString("lb_tm_match_original"));
        cell_G.setCellStyle(headerStyle);
        p_sheet.setColumnWidth(col, 20 * 256);
        col++;
        
        Cell cell_H = getCell(segHeaderRow, col);
        cell_H.setCellValue(m_bundle.getString("lb_job_id_report"));
        cell_H.setCellStyle(headerStyle);
        p_sheet.setColumnWidth(col, 15 * 256);
        col++;    

        Cell cell_I = getCell(segHeaderRow, col);
        cell_I.setCellValue(m_bundle.getString("lb_segment_id"));
        cell_I.setCellStyle(headerStyle);
        p_sheet.setColumnWidth(col, 15 * 256);
        col++;
        
        Cell cell_J = getCell(segHeaderRow, col);
        cell_J.setCellValue(m_bundle.getString("lb_page_name"));
        cell_J.setCellStyle(headerStyle);
        p_sheet.setColumnWidth(col, 25 * 256);
        col++;
        
        Cell cell_K = getCell(segHeaderRow, col);
        cell_K.setCellValue(m_bundle.getString("lb_sid"));
        cell_K.setCellStyle(headerStyle);
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
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private int writeSegmentInfo(Workbook p_workBook, Sheet p_sheet, Job p_job,
            GlobalSightLocale p_targetLocale, int p_row) throws Exception
    {
        Vector<TargetPage> targetPages = new Vector<TargetPage>();
        TranslationMemoryProfile tmp = p_job.getL10nProfile()
                .getTranslationMemoryProfile();
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
            for (int i = 0; i < targetPages.size(); i++)
            {
                if (cancel)
                    return 0;

                TargetPage targetPage = (TargetPage) targetPages.get(i);
                SourcePage sourcePage = targetPage.getSourcePage();

                SegmentTuUtil.getTusBySourcePageId(sourcePage.getId());
                List sourceTuvs = SegmentTuvUtil.getSourceTuvs(sourcePage);
                List targetTuvs = SegmentTuvUtil.getTargetTuvs(targetPage);

                MatchTypeStatistics tuvMatchTypes = lmLingManager
                        .getMatchTypesForStatistics(sourcePage.getIdAsLong(),
                                targetPage.getLocaleId(),
                                p_job.getLeverageMatchThreshold());
                Map fuzzyLeverageMatchMap = lmLingManager.getFuzzyMatches(
                        sourcePage.getIdAsLong(), targetPage.getLocaleId());

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
                    category = sourceTuv.getTu(jobId).getTuType();
                    if (excludItems != null && excludItems.contains(category))
                    {
                        continue;
                    }
                    
                    // Comment
                    String failure = "";
                    Issue issue = issuesMap.get(targetTuv.getId());
                    if (issue != null)
                    {
                        failure = issue.getCategory();
                    }
                    sid = sourceTuv.getSid();

                    // TM Match
                    StringBuilder matches = getMatches(fuzzyLeverageMatchMap,
                            tuvMatchTypes, excludItems, sourceTuvs, targetTuvs,
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
                            TermLeverageMatch tlm = termLeverageMatchSet
                                    .iterator().next();
                            sourceTerms = tlm.getMatchedSourceTerm();
                            targetTerms = tlm.getMatchedTargetTerm();
                        }
                    }

                    CellStyle contentStyle;
                    if(matches.toString().equals("In Context Match"))
                    	contentStyle = getIceContentStyle(p_workBook);
                    else if(matches.toString().equals("100%"))
                    	contentStyle = getExactContentStyle(p_workBook);
                    else
                    	contentStyle = getContentStyle(p_workBook);
                    
                    Row currentRow = getRow(p_sheet, p_row);
                    
                    // Source segment
                    CellStyle srcStyle;
                    if(m_rtlSourceLocale)
                    {
                    	 if(matches.toString().equals("In Context Match"))
                    		 srcStyle = getIceRtlContentStyle(p_workBook);
                         else if(matches.toString().equals("100%"))
                        	 srcStyle = getExactRtlContentStyle(p_workBook);
                         else
                        	 srcStyle = getRtlContentStyle(p_workBook);
                    }
                    else
                    {
                    	srcStyle = contentStyle;
                    }
                    Cell cell_A = getCell(currentRow, col);
                    cell_A.setCellValue(getSegment(pData, sourceTuv,
                            m_rtlSourceLocale, jobId));
                    cell_A.setCellStyle(srcStyle);
                    col++;
                    
                    // Target segment
                    CellStyle trgStyle;
                    if(m_rtlTargetLocale)
                    {
                    	 if(matches.toString().equals("In Context Match"))
                    		 trgStyle = getIceRtlContentStyle(p_workBook);
                         else if(matches.toString().equals("100%"))
                        	 trgStyle = getExactRtlContentStyle(p_workBook);
                         else
                        	 trgStyle = getRtlContentStyle(p_workBook);
                    }
                    else
                    {
                    	trgStyle = contentStyle;
                    }
                    Cell cell_B = getCell(currentRow, col);
                    cell_B.setCellValue(getSegment(pData, targetTuv,
                            m_rtlTargetLocale, jobId));
                    cell_B.setCellStyle(trgStyle);
                    col++;
                    
                    //Reviewers comments
                    CellStyle reviewersCommentStyle;
                    if(m_rtlTargetLocale)
                    {
                    	if(matches.toString().equals("In Context Match"))
                    		reviewersCommentStyle = getIceUnlockedRightStyle(p_workBook);
                        else if(matches.toString().equals("100%"))
                        	reviewersCommentStyle = getExactUnlockedRightStyle(p_workBook);
                        else
                        	reviewersCommentStyle = getUnlockedRightStyle(p_workBook);
                    }
                    else
                    {
                    	if(matches.toString().equals("In Context Match"))
                    		reviewersCommentStyle = getIceUnlockedStyle(p_workBook);
                        else if(matches.toString().equals("100%"))
                        	reviewersCommentStyle = getExactUnlockedStyle(p_workBook);
                        else
                        	reviewersCommentStyle = getUnlockedStyle(p_workBook);
                    }
                    Cell cell_C = getCell(currentRow, col);
                    cell_C.setCellValue("");
                    cell_C.setCellStyle(reviewersCommentStyle);
                    col++;
                    
                    // Category failure
                    Cell cell_D = getCell(currentRow, col);
                    cell_D.setCellValue(failure);
                    cell_D.setCellStyle(reviewersCommentStyle);
                    col++;
                    
                    // Glossary source
                    Cell cell_E = getCell(currentRow, col);
                    cell_E.setCellValue(sourceTerms);
                    cell_E.setCellStyle(contentStyle);
                    col++;

                    // Glossary target
                    Cell cell_F = getCell(currentRow, col);
                    cell_F.setCellValue(targetTerms);
                    cell_F.setCellStyle(contentStyle);
                    col++;
                    
                    // TM match
                    Cell cell_G = getCell(currentRow, col);
                    cell_G.setCellValue(matches.toString());
                    cell_G.setCellStyle(contentStyle);
                    col++;
                    
                    // Job id
                    Cell cell_H = getCell(currentRow, col);
                    cell_H.setCellValue(p_job.getId());
                    cell_H.setCellStyle(contentStyle);
                    col++;

                    // SID// Segment id
                    Cell cell_I = getCell(currentRow, col);
                    cell_I.setCellValue(sourceTuv.getTu(jobId).getId());
                    cell_I.setCellStyle(contentStyle);
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
                    Cell cell_J = getCell(currentRow, col);
                    cell_J.setCellValue(name);
                    cell_J.setCellStyle(contentStyle);
                    col++;

                    // SID
                    Cell cell_K = getCell(currentRow, col);
                    cell_K.setCellValue(sid);
                    cell_K.setCellStyle(contentStyle);
                    col++; 

                    p_row++;
                }
            }
            // Add category failure drop down list here.
            addCategoryFailureValidation(p_sheet, SEGMENT_START_ROW, p_row,
                    CATEGORY_FAILURE_COLUMN, CATEGORY_FAILURE_COLUMN);
        }
        
        p_sheet.setColumnHidden(7, true);
        p_sheet.setColumnHidden(8, true);
        p_sheet.setColumnHidden(10, true);

        return p_row;
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

    private void setAllCellStyleNull()
    {
        this.headerStyle = null;
        this.contentStyle = null;
        this.iceContentStyle = null;
        this.exactContentStyle = null;
        this.rtlContentStyle = null;
        this.iceRtlContentStyle = null;
        this.exactRtlContentStyle = null;
        this.unlockedStyle = null;
        this.iceUnlockedStyle = null;
        this.exactUnlockedStyle = null;
        this.unlockedRightStyle = null;
        this.iceUnlockedRightStyle = null;
        this.exactUnlockedRightStyle = null;
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
            cs.setFillPattern(CellStyle.SOLID_FOREGROUND );
            cs.setFillForegroundColor(IndexedColors.GREY_40_PERCENT.getIndex());
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
            style.setBorderTop(CellStyle.BORDER_THIN);
            style.setBorderRight(CellStyle.BORDER_THIN);
            style.setBorderBottom(CellStyle.BORDER_THIN);
            style.setBorderLeft(CellStyle.BORDER_THIN);
            Font font = p_workbook.createFont();
            font.setFontName("Arial");
            font.setFontHeightInPoints((short) 10);
            style.setFont(font);

            contentStyle = style;
        }

        return contentStyle;
    }
    
    private CellStyle getIceContentStyle(Workbook p_workbook) throws Exception
    {
        if (iceContentStyle == null)
        {
            CellStyle style = p_workbook.createCellStyle();
            style.setWrapText(true);
            style.setAlignment(CellStyle.ALIGN_LEFT);
            style.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
            style.setFillPattern(CellStyle.SOLID_FOREGROUND );
            style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            style.setBorderTop(CellStyle.BORDER_THIN);
            style.setBorderRight(CellStyle.BORDER_THIN);
            style.setBorderBottom(CellStyle.BORDER_THIN);
            style.setBorderLeft(CellStyle.BORDER_THIN);
            Font font = p_workbook.createFont();
            font.setFontName("Arial");
            font.setFontHeightInPoints((short) 10);
            style.setFont(font);

            iceContentStyle = style;
        }

        return iceContentStyle;
    }
    
    private CellStyle getExactContentStyle(Workbook p_workbook) throws Exception
    {
        if (exactContentStyle == null)
        {
            CellStyle style = p_workbook.createCellStyle();
            style.setWrapText(true);
            style.setAlignment(CellStyle.ALIGN_LEFT);
            style.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
            style.setFillPattern(CellStyle.SOLID_FOREGROUND );
            style.setFillForegroundColor(IndexedColors.PALE_BLUE.getIndex());
            style.setBorderTop(CellStyle.BORDER_THIN);
            style.setBorderRight(CellStyle.BORDER_THIN);
            style.setBorderBottom(CellStyle.BORDER_THIN);
            style.setBorderLeft(CellStyle.BORDER_THIN);
            Font font = p_workbook.createFont();
            font.setFontName("Arial");
            font.setFontHeightInPoints((short) 10);
            style.setFont(font);

            exactContentStyle = style;
        }

        return exactContentStyle;
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
            style.setBorderTop(CellStyle.BORDER_THIN);
            style.setBorderRight(CellStyle.BORDER_THIN);
            style.setBorderBottom(CellStyle.BORDER_THIN);
            style.setBorderLeft(CellStyle.BORDER_THIN);

            rtlContentStyle = style;
        }

        return rtlContentStyle;
    }
    
    private CellStyle getIceRtlContentStyle(Workbook p_workbook) throws Exception
    {
        if (iceRtlContentStyle == null)
        {
            Font font = p_workbook.createFont();
            font.setFontName("Arial");
            font.setFontHeightInPoints((short) 10);

            CellStyle style = p_workbook.createCellStyle();
            style.setFont(font);
            style.setWrapText(true);
            style.setAlignment(CellStyle.ALIGN_RIGHT);
            style.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
            style.setFillPattern(CellStyle.SOLID_FOREGROUND );
            style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            style.setBorderTop(CellStyle.BORDER_THIN);
            style.setBorderRight(CellStyle.BORDER_THIN);
            style.setBorderBottom(CellStyle.BORDER_THIN);
            style.setBorderLeft(CellStyle.BORDER_THIN);

            iceRtlContentStyle = style;
        }

        return iceRtlContentStyle;
    }
    
    private CellStyle getExactRtlContentStyle(Workbook p_workbook) throws Exception
    {
        if (exactRtlContentStyle == null)
        {
            Font font = p_workbook.createFont();
            font.setFontName("Arial");
            font.setFontHeightInPoints((short) 10);

            CellStyle style = p_workbook.createCellStyle();
            style.setFont(font);
            style.setWrapText(true);
            style.setAlignment(CellStyle.ALIGN_RIGHT);
            style.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
            style.setFillPattern(CellStyle.SOLID_FOREGROUND );
            style.setFillForegroundColor(IndexedColors.PALE_BLUE.getIndex());
            style.setBorderTop(CellStyle.BORDER_THIN);
            style.setBorderRight(CellStyle.BORDER_THIN);
            style.setBorderBottom(CellStyle.BORDER_THIN);
            style.setBorderLeft(CellStyle.BORDER_THIN);

            exactRtlContentStyle = style;
        }

        return exactRtlContentStyle;
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
            style.setBorderTop(CellStyle.BORDER_THIN);
            style.setBorderRight(CellStyle.BORDER_THIN);
            style.setBorderBottom(CellStyle.BORDER_THIN);
            style.setBorderLeft(CellStyle.BORDER_THIN);

            unlockedStyle = style;
        }

        return unlockedStyle;
    }
    
    private CellStyle getIceUnlockedStyle(Workbook p_workbook) throws Exception
    {
        if (iceUnlockedStyle == null)
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
            style.setFillPattern(CellStyle.SOLID_FOREGROUND );
            style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            style.setBorderTop(CellStyle.BORDER_THIN);
            style.setBorderRight(CellStyle.BORDER_THIN);
            style.setBorderBottom(CellStyle.BORDER_THIN);
            style.setBorderLeft(CellStyle.BORDER_THIN);

            iceUnlockedStyle = style;
        }

        return iceUnlockedStyle;
    }
    
    private CellStyle getExactUnlockedStyle(Workbook p_workbook) throws Exception
    {
        if (exactUnlockedStyle == null)
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
            style.setFillPattern(CellStyle.SOLID_FOREGROUND );
            style.setFillForegroundColor(IndexedColors.PALE_BLUE.getIndex());
            style.setBorderTop(CellStyle.BORDER_THIN);
            style.setBorderRight(CellStyle.BORDER_THIN);
            style.setBorderBottom(CellStyle.BORDER_THIN);
            style.setBorderLeft(CellStyle.BORDER_THIN);

            exactUnlockedStyle = style;
        }

        return exactUnlockedStyle;
    }

    private CellStyle getUnlockedRightStyle(Workbook p_workbook) throws Exception
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
            style.setBorderTop(CellStyle.BORDER_THIN);
            style.setBorderRight(CellStyle.BORDER_THIN);
            style.setBorderBottom(CellStyle.BORDER_THIN);
            style.setBorderLeft(CellStyle.BORDER_THIN);

            unlockedRightStyle = style;
        }

        return unlockedRightStyle;
    }
    
    private CellStyle getIceUnlockedRightStyle(Workbook p_workbook) throws Exception
    {
        if (iceUnlockedRightStyle == null)
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
            style.setFillPattern(CellStyle.SOLID_FOREGROUND );
            style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            style.setBorderTop(CellStyle.BORDER_THIN);
            style.setBorderRight(CellStyle.BORDER_THIN);
            style.setBorderBottom(CellStyle.BORDER_THIN);
            style.setBorderLeft(CellStyle.BORDER_THIN);

            iceUnlockedRightStyle = style;
        }

        return iceUnlockedRightStyle;
    }
    
    private CellStyle getExactUnlockedRightStyle(Workbook p_workbook) throws Exception
    {
        if (exactUnlockedRightStyle == null)
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
            style.setFillPattern(CellStyle.SOLID_FOREGROUND );
            style.setFillForegroundColor(IndexedColors.PALE_BLUE.getIndex());
            style.setBorderTop(CellStyle.BORDER_THIN);
            style.setBorderRight(CellStyle.BORDER_THIN);
            style.setBorderBottom(CellStyle.BORDER_THIN);
            style.setBorderLeft(CellStyle.BORDER_THIN);

            exactUnlockedRightStyle = style;
        }

        return exactUnlockedRightStyle;
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
        catch (Exception e)
        {
            logger.error(
                    "Error when create hidden area for category failures.", e);
        }
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
            String cat = aCategory.getValue();
            result.add(cat);
        }
        return result;
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

    /**
     * Get TM matches.
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
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

    @Override
    public String getReportType()
    {
        return ReportConstants.REVIEWERS_COMMENTS_SIMPLE_REPORT;
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
            ReportsData data = ReportGeneratorHandler.getReportsMap(m_userId,
                    m_jobIDS, getReportType());
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
    
    private String getSegment(PseudoData pData, Tuv tuv, boolean m_rtlLocale,
            long p_jobId)
    {
        String result = null;
        StringBuffer content = new StringBuffer();
        List subFlows = tuv.getSubflowsAsGxmlElements();
        long tuId = tuv.getTuId();
        if(isIncludeCompactTags)
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
                    content.append("\r\n#").append(tuId).append(":")
                            .append(subId).append("\n")
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
        // If the Workbook has only one sheet, the report name should contain language pair info, such as en_US_de_DE.
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
        
        return ReportHelper.getReportFile(p_reportType, p_job, ReportConstants.EXTENSION_XLSX, langInfo);
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
