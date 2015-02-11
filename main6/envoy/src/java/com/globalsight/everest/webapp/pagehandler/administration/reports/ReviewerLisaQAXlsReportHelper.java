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

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.Vector;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

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
import com.globalsight.everest.comment.IssueHistory;
import com.globalsight.everest.comment.IssueImpl;
import com.globalsight.everest.comment.IssueOptions;
import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.edit.CommentHelper;
import com.globalsight.everest.integration.ling.LingServerProxy;
import com.globalsight.everest.integration.ling.tm2.LeverageMatch;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.localemgr.LocaleManagerLocal;
import com.globalsight.everest.page.SourcePage;
import com.globalsight.everest.page.TargetPage;
import com.globalsight.everest.persistence.tuv.SegmentTuvUtil;
import com.globalsight.everest.projecthandler.TranslationMemoryProfile;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.tuv.Tuv;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.pagehandler.administration.company.Select;
import com.globalsight.everest.webapp.pagehandler.administration.users.UserUtil;
import com.globalsight.everest.workflowmanager.Workflow;
import com.globalsight.ling.tm.LeverageMatchLingManager;
import com.globalsight.ling.tm.LeverageSegment;
import com.globalsight.ling.tw.PseudoConstants;
import com.globalsight.ling.tw.PseudoData;
import com.globalsight.terminology.ITermbase;
import com.globalsight.terminology.ITermbaseManager;
import com.globalsight.terminology.termleverager.TermLeverageManager;
import com.globalsight.terminology.termleverager.TermLeverageMatch;
import com.globalsight.terminology.termleverager.TermLeverageOptions;
import com.globalsight.util.GeneralException;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.StringUtil;
import com.globalsight.util.edit.EditUtil;

/**
 * Now, this helper is only used for creating Implemented Comments Check Report
 * (ICC)
 */
public class ReviewerLisaQAXlsReportHelper
{
    private HttpServletRequest request = null;

    private HttpServletResponse response = null;

    private SessionManager sessionMgr = null;
    
    private ResourceBundle bundle = null;

    // private NumberFormat percent = null;

    private CellStyle contentStyle = null;

    private CellStyle rtlContentStyle = null;

    private CellStyle headerStyle = null;

    // private int m_reportType = IMPLEMENTED_COMMENTS_CHECK_REPORT;

    private static final String DEFAULT_DATE_FORMAT = "MM/dd/yy hh:mm:ss a z";

    // public static final int TRANSLATIONS_EDIT_REPORT = 2;
    public static final int IMPLEMENTED_COMMENTS_CHECK_REPORT = 3;
    public static final int CATEGORY_FAILURE_COLUMN = 7;
    private static final String CATEGORY_FAILURE_DROP_DOWN_LIST = "categoryFailureDropDownList";

    public static final int SEGMENT_START_ROW = 7;
    public static final int SEGMENT_HEADER_ROW = 6;
    public static final int LANGUAGE_HEADER_ROW = 3;
    public static final int LANGUAGE_INFO_ROW = 4;

    private CellStyle hightLightStyle = null;
    private CellStyle hightLightRightStyle = null;

    private CellStyle unlockedStyle = null;
    private CellStyle unlockedRightStyle = null;
    private Locale uiLocale = new Locale("en", "US");

    /**
     * Constructor. Create a helper to generate the report
     * 
     * @param p_request
     *            the request
     * @param p_response
     *            the response
     * @throws Exception
     */
    public ReviewerLisaQAXlsReportHelper(HttpServletRequest p_request,
            HttpServletResponse p_response, int p_reportType) throws Exception
    {
        request = p_request;
        response = p_response;
        HttpSession session = p_request.getSession();
        /*
         * percent = NumberFormat.getPercentInstance((Locale) session
         * .getAttribute(WebAppConstants.UILOCALE));
         */
        sessionMgr = (SessionManager) session
                .getAttribute(WebAppConstants.SESSION_MANAGER);

        String companyName = UserUtil.getCurrentCompanyName(request);
        CompanyThreadLocal.getInstance().setValue(companyName);
        uiLocale = (Locale) request.getSession().getAttribute(
                WebAppConstants.UILOCALE);
    }

    /**
     * Generates Excel report
     * 
     * @throws Exception
     */
    public void generateReport() throws Exception
    {
    	bundle = PageHandler.getBundle(request.getSession());
    	
        Workbook workbook = new SXSSFWorkbook();
        String dateFormat = request.getParameter(WebAppConstants.DATE_FORMAT);
        if (dateFormat == null)
        {
            dateFormat = DEFAULT_DATE_FORMAT;
        }

        createReport(workbook, dateFormat);
        
        ServletOutputStream out = response.getOutputStream();
        workbook.write(out);
        out.close();
    }

    private void addSegmentHeaderICC(Workbook p_workbook, Sheet p_sheet) throws Exception
    {
        ResourceBundle bundle = PageHandler.getBundle(request.getSession());
        int col = 0;
        int row = SEGMENT_HEADER_ROW;
        Row segHeaderRow = getRow(p_sheet, row);
        Cell cell_A = getCell(segHeaderRow, col++);
        cell_A.setCellValue( bundle
                .getString("lb_job_id_report"));
        cell_A.setCellStyle(getHeaderStyle(p_workbook));
        p_sheet.setColumnWidth(col - 1, 20 * 256);
        
        Cell cell_B = getCell(segHeaderRow, col++);
        cell_B.setCellValue( bundle
                .getString("lb_segment_id"));
        cell_B.setCellStyle(getHeaderStyle(p_workbook));
        p_sheet.setColumnWidth(col - 1, 20 * 256);

        Cell cell_C = getCell(segHeaderRow, col++);
        cell_C.setCellValue( bundle
                .getString("lb_page_name"));
        cell_C.setCellStyle(getHeaderStyle(p_workbook));
        p_sheet.setColumnWidth(col - 1, 20 * 256);

        Cell cell_D = getCell(segHeaderRow, col++);
        cell_D.setCellValue( bundle
                .getString("lb_source_segment"));
        cell_D.setCellStyle(getHeaderStyle(p_workbook));
        p_sheet.setColumnWidth(col - 1, 40 * 256);
        
        Cell cell_E = getCell(segHeaderRow, col++);
        cell_E.setCellValue( bundle
                .getString("lb_target_segment"));
        cell_E.setCellStyle(getHeaderStyle(p_workbook));
        p_sheet.setColumnWidth(col - 1, 20 * 256);
        
        Cell cell_F = getCell(segHeaderRow, col++);
        cell_F.setCellValue( bundle.getString("lb_sid"));
        cell_F.setCellStyle(getHeaderStyle(p_workbook));
        p_sheet.setColumnWidth(col - 1, 40 * 256);

        Cell cell_G = getCell(segHeaderRow, col++);
        cell_G.setCellValue( bundle.getString("lb_comment_free"));
        cell_G.setCellStyle(getHeaderStyle(p_workbook));
        p_sheet.setColumnWidth(col - 1, 40 * 256);

        Cell cell_H = getCell(segHeaderRow, col++);
        cell_H.setCellValue( bundle.getString("lb_category_failure"));
        cell_H.setCellStyle(getHeaderStyle(p_workbook));
        p_sheet.setColumnWidth(col - 1, 30 * 256);
        
        Cell cell_I = getCell(segHeaderRow, col++);
        cell_I.setCellValue( bundle.getString("lb_tm_match_original"));
        cell_I.setCellStyle(getHeaderStyle(p_workbook));
        p_sheet.setColumnWidth(col - 1, 30 * 256);
        
        Cell cell_J = getCell(segHeaderRow, col++);
        cell_J.setCellValue( bundle.getString("lb_glossary_source"));
        cell_J.setCellStyle(getHeaderStyle(p_workbook));
        p_sheet.setColumnWidth(col - 1, 30 * 256);
        
        Cell cell_K = getCell(segHeaderRow, col++);
        cell_K.setCellValue( bundle.getString("lb_glossary_target"));
        cell_K.setCellStyle(getHeaderStyle(p_workbook));
        p_sheet.setColumnWidth(col - 1, 30 * 256);
    }

    /**
     * Add job header to the sheet
     * 
     * @param p_sheet
     *            the sheet
     * @throws Exception
     */
    private void addLanguageHeader(Workbook p_workbok, Sheet p_sheet) throws Exception
    {
        ResourceBundle bundle = PageHandler.getBundle(request.getSession());

        int col = 0;
        int row = LANGUAGE_HEADER_ROW;
        Row langHeaderRow = getRow(p_sheet, row);
        Cell cell_A = getCell(langHeaderRow, col++);
        cell_A.setCellValue(bundle
                .getString("lb_source_language"));
        cell_A.setCellStyle(getHeaderStyle(p_workbok));
        p_sheet.setColumnWidth(col - 1, 30 * 256);
        
        Cell cell_B = getCell(langHeaderRow, col++);
        cell_B.setCellValue(bundle
                .getString("lb_target_language"));
        cell_B.setCellStyle(getHeaderStyle(p_workbok));
        p_sheet.setColumnWidth(col - 1, 30 * 256);
    }

    /**
     * Add title to the sheet
     * 
     * @param p_sheet
     *            the sheet
     * @throws Exception
     */
    private void addTitle(Workbook p_workbook, Sheet p_sheet) throws Exception
    {
        ResourceBundle bundle = PageHandler.getBundle(request.getSession());
        // Title font is black bold on white
        Font titleFont = p_workbook.createFont();
        titleFont.setUnderline(Font.U_NONE);
        titleFont.setFontName("Times");
        titleFont.setFontHeightInPoints((short) 14);
        titleFont.setBoldweight(Font.BOLDWEIGHT_BOLD);
        titleFont.setColor(IndexedColors.BLACK.getIndex());
        
        CellStyle titleStyle = p_workbook.createCellStyle();
        titleStyle.setFont(titleFont);
        titleStyle.setWrapText(false);
        
        Row titleRow = getRow(p_sheet, 0);
        Cell titleCell = getCell(titleRow, 0);
        titleCell.setCellValue(bundle
                .getString("implemented_comments_check_report"));
        titleCell.setCellStyle(titleStyle);
        p_sheet.setColumnWidth(0, 40 * 256);
    }

    /**
     * Create the report
     * 
     * @throws Exception
     */
    private void createReport(Workbook p_workbook, String p_dateFormat) throws Exception
    {
        // Create Sheet
        Sheet sheet = p_workbook.createSheet(bundle.getString("lb_icc"));

        // Add Title
        addTitle(p_workbook, sheet);

        // Add Locale Pair Header and Segment Header
        addLanguageHeader(p_workbook, sheet);
        
        addSegmentHeaderICC(p_workbook, sheet);
        
        createCategoryFailureNameArea(p_workbook);

        // Insert Data into Report
        writeLanguageInfo(p_workbook, sheet);
        
        writeSegmentInfoICC(p_workbook, sheet, p_dateFormat);
    }

    /**
     * Write the Job information into the header
     * 
     * @param p_sheet
     *            the sheet
     * @throws Exception
     */
    private void writeLanguageInfo(Workbook p_workbook, Sheet p_sheet) throws Exception
    {
        int col = 0;
        int row = LANGUAGE_INFO_ROW;
        long jobId = 0;

        if (request.getParameter(WebAppConstants.JOB_ID) != null
                && !"*".equals(request.getParameter(WebAppConstants.JOB_ID)))
        {
            // Request from main reports entrance.
            jobId = Long.valueOf(request.getParameter(WebAppConstants.JOB_ID));
            LocaleManagerLocal manager = new LocaleManagerLocal();
            writeLanguageInfo(p_workbook, p_sheet, jobId, manager.
            		getLocaleById(Long.valueOf(request.
            		getParameter(WebAppConstants.TARGET_LANGUAGE))).getDisplayName(),
                    request.getParameter(WebAppConstants.DATE_FORMAT));
        }
        else if (sessionMgr.getAttribute(WebAppConstants.JOB_ID) != null)
        {
            // Request from pop up editor or work offline page.
            Object jobIdObject = sessionMgr
                    .getAttribute(WebAppConstants.JOB_ID);
            if (jobIdObject instanceof String)
            {
                jobId = Long.parseLong((String) jobIdObject);
            }
            else if (jobIdObject instanceof Long)
            {
                jobId = (Long) jobIdObject;
            }
            writeLanguageInfo(p_workbook, p_sheet, jobId,
                    (String) sessionMgr
                            .getAttribute(WebAppConstants.TARGETVIEW_LOCALE),
                    DEFAULT_DATE_FORMAT);
        }
        else
        {
            // If no job exists, just set the cell blank.
        	Row infoRow = getRow(p_sheet, row);
        	Cell cell_A = getCell(infoRow, col++);
        	cell_A.setCellValue("");
        	cell_A.setCellStyle(getContentStyle(p_workbook));
        	
        	Cell cell_B = getCell(infoRow, col++);
        	cell_B.setCellValue("");
        	cell_B.setCellStyle(getContentStyle(p_workbook));
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

    /**
     * Write the Job information into the header
     * 
     * @param p_sheet
     *            the sheet
     * @param p_jobId
     *            the job id
     * @param p_targetLang
     *            the displayed target language
     * @param p_dateFormat
     *            the date format
     * @throws Exception
     */
    private void writeLanguageInfo(Workbook p_workbook, Sheet p_sheet, long p_jobId,
            String p_targetLang, String p_dateFormat) throws Exception
    {
        Job job = ServerProxy.getJobHandler().getJobById(p_jobId);
        int col = 0;
        int row = LANGUAGE_INFO_ROW;

        // Source Language
        Row langInfoRow = getRow(p_sheet, row);
        Cell cell_A = getCell(langInfoRow, col++);
        cell_A.setCellValue(job.getSourceLocale()
                .getDisplayName(uiLocale));
        cell_A.setCellStyle(getContentStyle(p_workbook));
        p_sheet.setColumnWidth(col - 1, 30 * 256);

        // Target Language
        Cell cell_B = getCell(langInfoRow, col++);
        cell_B.setCellValue(p_targetLang);
        cell_B.setCellStyle(getContentStyle(p_workbook));
        p_sheet.setColumnWidth(col - 1, 30 * 256);

    }

//    private List<Long> getAllTLIDS() throws Exception
//    {
//        List<Long> result = new ArrayList<Long>();
//        for (Iterator it = ServerProxy.getLocaleManager().getAllTargetLocales()
//                .iterator(); it.hasNext();)
//        {
//            GlobalSightLocale gsl = (GlobalSightLocale) it.next();
//            result.add(gsl.getId());
//        }
//        return result;
//    }

    /**
     * Write segment information into each row of the sheet for Implemented
     * Comments Check Report.
     * 
     * @param p_sheet
     *            the sheet
     * @param p_dateFormat
     *            data format
     * @throws Exception
     */
    private void writeSegmentInfoICC(Workbook p_workbook, Sheet p_sheet, String p_dateFormat)
            throws Exception
    {
        int row = SEGMENT_START_ROW;
        if (request.getParameter(WebAppConstants.JOB_ID) != null
                && !"*".equals(request.getParameter(WebAppConstants.JOB_ID)))
        {
            // request from main reports entrance
            long jobId = Long.valueOf(request
                    .getParameter(WebAppConstants.JOB_ID));
            String targetLang = request
                    .getParameter(WebAppConstants.TARGET_LANGUAGE);
            writeSegmentInfoICC(p_workbook, p_sheet, jobId, targetLang, "", row);
        }
        else
        {
            // If no job exists, just set the cell blank
            writeBlank(p_workbook, p_sheet, row, 10);
        }

    }

    /**
     * For Implemented Comments Check Report, Write segment information into
     * each row of the sheet.
     * 
     * @see writeSegmentInfoRCR(WritableSheet, long, long, String, int)
     */
    private int writeSegmentInfoICC(Workbook p_workbook, Sheet p_sheet, long p_jobId,
            String p_targetLang, String p_srcPageId, int p_row)
            throws Exception
    {
        Job job = ServerProxy.getJobHandler().getJobById(p_jobId);
        long companyId = job.getCompanyId();
        Vector<TargetPage> targetPages = new Vector<TargetPage>();

        TranslationMemoryProfile tmp = null;
        List<String> excludItems = null;
        GlobalSightLocale targetLocale = null;
        for (Workflow workflow : job.getWorkflows())
        {
            if (Workflow.PENDING.equals(workflow.getState())
                    || Workflow.CANCELLED.equals(workflow.getState())
                    // || Workflow.EXPORT_FAILED.equals(workflow.getState())
                    || Workflow.IMPORT_FAILED.equals(workflow.getState()))
            {
                continue;
            }
            if (p_targetLang
                    .equals(String.valueOf(workflow.getTargetLocale().getId())))
            {
                targetLocale = workflow.getTargetLocale();
                targetPages = workflow.getTargetPages();
                tmp = workflow.getJob().getL10nProfile()
                        .getTranslationMemoryProfile();
                if (tmp != null)
                {
                    excludItems = new ArrayList<String>(
                            tmp.getJobExcludeTuTypes());
                }
            }
        }

        if (targetPages.isEmpty())
        {
            // If no corresponding target page exists
            // and the row is the initial row, then set the cell blank
            if (SEGMENT_START_ROW == p_row)
            {
                writeBlank(p_workbook , p_sheet, SEGMENT_START_ROW, 10);
            }
        }
        else
        {
            LeverageMatchLingManager leverageMatchLingManager = LingServerProxy
                    .getLeverageMatchLingManager();
            TermLeverageManager termLeverageManager = ServerProxy
                    .getTermLeverageManager();

            Locale sourcePageLocale = job.getSourceLocale().getLocale();
            Locale targetPageLocale = targetLocale.getLocale();
            Map<Long, Set<TermLeverageMatch>> termLeverageMatchResultMap = null;
            TermLeverageOptions termLeverageOptions = getTermLeverageOptions(
                    sourcePageLocale, targetPageLocale, job.getL10nProfile()
                            .getProject().getTermbaseName(),
                    String.valueOf(job.getCompanyId()));
            if (termLeverageOptions != null)
            {
                termLeverageMatchResultMap = termLeverageManager
                        .getTermMatchesForPages(job.getSourcePages(),
                                targetLocale);
            }
            
            String category = null;
            PseudoData pData = new PseudoData();
            pData.setMode(PseudoConstants.PSEUDO_COMPACT);
            String sourceSegmentString = null;
            String targetSegmentString = null;
            String sid = null;
            for (int i = 0; i < targetPages.size(); i++)
            {
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
                List sourceTuvs = SegmentTuvUtil.getSourceTuvs(sourcePage);
                List targetTuvs = SegmentTuvUtil.getTargetTuvs(targetPage);
                Map<Long, LeverageSegment> exactMatches = leverageMatchLingManager
                        .getExactMatches(sourcePage.getIdAsLong(), new Long(
                                targetPage.getLocaleId()));
                Map<Long, Set<LeverageMatch>> leverageMatcheMap = leverageMatchLingManager
                        .getFuzzyMatches(sourcePage.getIdAsLong(), new Long(
                                targetPage.getLocaleId()));

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
                    int col = 0;
                    Tuv targetTuv = (Tuv) targetTuvs.get(j);
                    Tuv sourceTuv = (Tuv) sourceTuvs.get(j);
                    category = sourceTuv.getTu(companyId).getTuType();
                    if (excludItems != null && excludItems.contains(category))
                    {
                        continue;
                    }

                    // Comment
                    List issueHistories = null;
                    String lastComment = "";
                    String failure = "";
                    Date issueCreatedDate = null;
                    Issue issue = issuesMap.get(targetTuv.getId());
                    if (issue != null)
                    {
                        issueHistories = issue.getHistory();
                        failure = issue.getCategory();
                    }
                    if (issueHistories != null && issueHistories.size() > 0)
                    {
                        IssueHistory issueHistory = (IssueHistory) issueHistories
                                .get(0);
                        lastComment = issueHistory.getComment();
                        issueCreatedDate = issueHistory.dateReportedAsDate();
                    }

                    sourceSegmentString = sourceTuv.getGxmlElement()
                            .getTextValue();
                    sid = sourceTuv.getSid();
                    targetSegmentString = targetTuv.getGxmlElement()
                            .getTextValue();
                    Date targetSegmentModifiedDate = targetTuv
                            .getLastModified();

                    StringBuilder matches = getMatches(exactMatches,
                    		leverageMatcheMap, targetTuv, sourceTuv);

                    // Get Terminology/Glossary Source and Target.
                    String sourceTerms = "";
                    String targetTerms = "";
                    if (termLeverageMatchResultMap != null)
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

                    // For Implemented Comments Check Report (LSO Extension)
                    if (lastComment.length() == 0)
                    {
                        continue;
                    }

                    CellStyle  style = getContentStyle(p_workbook);
                    // Set color format for target segment changed
                    if (targetSegmentModifiedDate.before(issueCreatedDate))
                    {
                    	style = getRedStyle(p_workbook);
                    }

                    // Job id
                    Row row = getRow(p_sheet, p_row);
                    Cell cell_A = getCell(row, col++);
                    cell_A.setCellValue(p_jobId);
                    cell_A.setCellStyle(style);
                    p_sheet.setColumnWidth(col - 1, 20 * 256);
                    // Segment id
                    Cell cell_B = getCell(row, col++);
                    cell_B.setCellValue(sourceTuv.getTu(companyId).getId());
                    cell_B.setCellStyle(style);
                    p_sheet.setColumnWidth(col - 1, 20 * 256);

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

                    // TargetPage id
                    Cell cell_C = getCell(row, col++);
                    cell_C.setCellValue(Name);
                    cell_C.setCellStyle(style);
                    p_sheet.setColumnWidth(col - 1, 20 * 256);

                    // Source segment
                    CellStyle sourceStyle = m_rtlSourceLocale ? getRtlContentStyle(p_workbook)
                            : style;
                    String srcContent = m_rtlSourceLocale ? EditUtil
                            .toRtlString(sourceSegmentString)
                            : sourceSegmentString;

                    Cell cell_D = getCell(row, col++);
                    cell_D.setCellValue(srcContent);
                    cell_D.setCellStyle(sourceStyle);
                    p_sheet.setColumnWidth(col - 1, 40 * 256);

                    // Target segment
                    CellStyle targetStyle = m_rtlTargetLocale ? getRtlContentStyle(p_workbook)
                            : style;
                    String content = m_rtlTargetLocale ? EditUtil
                            .toRtlString(targetSegmentString)
                            : targetSegmentString;

                    Cell cell_E = getCell(row, col++);
                    cell_E.setCellValue(content);
                    cell_E.setCellStyle(targetStyle);
                    p_sheet.setColumnWidth(col - 1, 40 * 256);

                    Cell cell_F = getCell(row, col++);
                    cell_F.setCellValue(sid);
                    cell_F.setCellStyle(style);
                    p_sheet.setColumnWidth(col - 1, 40 * 256);

                    CellStyle commentStyle = m_rtlTargetLocale ? getUnlockedRightStyle(p_workbook)
                            : getUnlockedStyle(p_workbook);
                    // Set color format for target segment changed
                    if (targetSegmentModifiedDate.before(issueCreatedDate))
                    {
                        commentStyle = m_rtlTargetLocale ? getRedRightStyle(p_workbook)
                                : getRedStyle(p_workbook);
                    }

                    if (m_rtlTargetLocale)
                    {
                        lastComment = EditUtil.toRtlString(lastComment);
                    }

                    Cell cell_G = getCell(row, col++);
                    cell_G.setCellValue(lastComment);
                    cell_G.setCellStyle(commentStyle);
                    p_sheet.setColumnWidth(col - 1, 40 * 256);

                    // Category failure
                    CellStyle cfStyle = getUnlockedStyle(p_workbook);

                    // Set color format for target segment changed
                    if (targetSegmentModifiedDate.before(issueCreatedDate))
                    {
                    	cfStyle = getRedStyle(p_workbook);
                    }

                    Cell cell_H = getCell(row, col++);
                    cell_H.setCellValue(failure);
                    cell_H.setCellStyle(cfStyle);
                    p_sheet.setColumnWidth(col - 1, 30 * 256);

                    // TM match
                    Cell cell_I = getCell(row, col++);
                    cell_I.setCellValue(matches.toString());
                    cell_I.setCellStyle(style);
                    p_sheet.setColumnWidth(col - 1, 30 * 256);

                    // Glossary source
                    Cell cell_J = getCell(row, col++);
                    cell_J.setCellValue(sourceTerms);
                    cell_J.setCellStyle(style);
                    p_sheet.setColumnWidth(col - 1, 30 * 256);

                    // Glossary target
                    Cell cell_K = getCell(row, col++);
                    cell_K.setCellValue(targetTerms);
                    cell_K.setCellStyle(style);
                    p_sheet.setColumnWidth(col - 1, 30 * 256);
                    p_row++;
                }
            }
            // Add category failure drop down list here.
            addCategoryFailureValidation(p_sheet, SEGMENT_START_ROW, p_row,
                    CATEGORY_FAILURE_COLUMN, CATEGORY_FAILURE_COLUMN);
        }

        return p_row;
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
    private void writeBlank(Workbook p_workbook, Sheet p_sheet, int p_row, int p_colLen)
            throws Exception
    {
        for (int col = 0; col < p_colLen; col++)
        {
        	Row row = getRow(p_sheet, p_row);
        	Cell cell = getCell(row, col++);
        	cell.setCellValue("");
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

        String companyName = UserUtil.getCurrentCompanyName(request);
        if (CompanyWrapper.isSuperCompanyName(companyName))
            return true;

        String companyId = CompanyWrapper.getCompanyIdByName(companyName);
        if (companyId != null && companyId.equals(p_job.getCompanyId()))
            return true;

        return false;
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
    
    /**
     * Add category failure drop down list. It is from "J8" to "Jn".
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
    
    private List<String> getFailureCategoriesList()
    {
        List<String> result = new ArrayList<String>();

        ResourceBundle bundle = PageHandler.getBundle(request.getSession());
        String currentCompanyId = CompanyThreadLocal.getInstance().getValue();
        List failureCategories = IssueOptions.getAllCategories(bundle,
                currentCompanyId);
        for (int i = 0; i < failureCategories.size(); i++)
        {
            Select aCategory = (Select) failureCategories.get(i);
            result.add(aCategory.getValue());
        }
        return result;
    }
    
    /**
     * Get TM matches.
     */
    private StringBuilder getMatches(Map exactMatches, Map leverageMatcheMap,
    		Tuv targetTuv, Tuv sourceTuv)
    {
    	StringBuilder matches = new StringBuilder();
        Set<LeverageMatch> leverageMatches = (Set<LeverageMatch>) leverageMatcheMap.get(sourceTuv
                .getIdAsLong());
        if (exactMatches.get(sourceTuv.getIdAsLong()) != null)
        {
            matches.append(StringUtil.formatPCT(100));
        }
        else if (leverageMatches != null)
        {
            int count = 0;
            for (Iterator<LeverageMatch> ite = leverageMatches
                    .iterator(); ite.hasNext();)
            {
                LeverageMatch leverageMatch = (LeverageMatch) ite
                        .next();
                if ((leverageMatches.size() > 1))
                {
                    matches = matches
                            .append(++count)
                            .append(", ")
                            .append(StringUtil
                                    .formatPCT(leverageMatch
                                            .getScoreNum()))
                            .append("\r\n");
                }
                else
                {
                    matches.append(StringUtil
                            .formatPCT(leverageMatch.getScoreNum()));
                    break;
                }

            }
        }
        else
        {
            matches.append(bundle.getString("lb_no_match_report"));
        }
        if (targetTuv.isRepeated())
        {
            matches.append("\r\n")
                    .append(bundle
                            .getString("jobinfo.tradosmatches.invoice.repeated"));
        }
        else if (targetTuv.getRepetitionOfId() > 0)
        {
            matches.append("\r\n")
                    .append(bundle
                            .getString("jobinfo.tradosmatches.invoice.repetition"));
        }
        return matches;
    }
    
    private CellStyle getRedRightStyle(Workbook p_workbook) throws Exception
    {
        CellStyle style = p_workbook.createCellStyle();
        Font font = p_workbook.createFont();
        if (hightLightRightStyle == null)
        {
            font.setBoldweight(Font.BOLDWEIGHT_BOLD);
            font.setColor(IndexedColors.BLACK.getIndex());
            font.setUnderline(Font.U_NONE);
            font.setFontName("Arial");
            font.setFontHeightInPoints((short) 10);

            style.setWrapText(true);
            style.setAlignment(CellStyle.ALIGN_RIGHT);
            style.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
            style.setFillPattern(CellStyle.SOLID_FOREGROUND );
            style.setFillForegroundColor(IndexedColors.ROSE.getIndex());
            hightLightRightStyle = style;
        }

        return hightLightRightStyle;
    }

    private CellStyle getRedStyle(Workbook p_workbook) throws Exception
    {
    	CellStyle style = p_workbook.createCellStyle();
        Font font = p_workbook.createFont();
        if (hightLightStyle == null)
        {
        	font.setBoldweight(Font.BOLDWEIGHT_BOLD);
            font.setColor(IndexedColors.BLACK.getIndex());
            font.setUnderline(Font.U_NONE);
            font.setFontName("Arial");
            font.setFontHeightInPoints((short) 10);

            style.setWrapText(true);
            style.setAlignment(CellStyle.ALIGN_LEFT);
            style.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
            style.setFillPattern(CellStyle.SOLID_FOREGROUND );
            style.setFillForegroundColor(IndexedColors.ROSE.getIndex());
            hightLightStyle = style;
        }

        return hightLightStyle;
    }
    
    
    /**
     * 
     * @return format of the unlocked cell
     * @throws Exception
     */
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

            unlockedRightStyle = style;
        }

        return unlockedRightStyle;
    }

    /**
     * 
     * @return format of the report content
     * @throws Exception
     */
    private CellStyle getRtlContentStyle(Workbook p_workbook) throws Exception
    {
        if (rtlContentStyle == null)
        {
        	CellStyle style = p_workbook.createCellStyle();
            Font font = p_workbook.createFont();
            font.setFontName("Arial");
            font.setFontHeightInPoints((short) 10);
            style.setFont(font);
            style.setWrapText(true);
            style.setAlignment(CellStyle.ALIGN_LEFT);

            rtlContentStyle = style;
        }

        return rtlContentStyle;
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
            Font font = p_workbook.createFont();
            font.setFontName("Arial");
            font.setFontHeightInPoints((short) 10);
            style.setFont(font);
            style.setWrapText(true);
            style.setAlignment(CellStyle.ALIGN_LEFT);

            contentStyle = style;
        }

        return contentStyle;
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

}
