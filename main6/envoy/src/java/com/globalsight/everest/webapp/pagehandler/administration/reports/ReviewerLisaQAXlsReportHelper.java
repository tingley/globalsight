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

import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.format.Alignment;
import jxl.format.CellFormat;
import jxl.format.UnderlineStyle;
import jxl.format.VerticalAlignment;
import jxl.write.Label;
import jxl.write.Number;
import jxl.write.WritableCellFeatures;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;

import com.globalsight.everest.comment.CommentManager;
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
import com.globalsight.everest.page.SourcePage;
import com.globalsight.everest.page.TargetPage;
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

    private WritableWorkbook m_workbook = null;

    // private NumberFormat percent = null;

    private WritableCellFormat contentFormat = null;

    private WritableCellFormat rtlContentFormat = null;

    private WritableCellFormat headerFormat = null;

    // private int m_reportType = IMPLEMENTED_COMMENTS_CHECK_REPORT;

    private static final String DEFAULT_DATE_FORMAT = "MM/dd/yy hh:mm:ss a z";

    // public static final int TRANSLATIONS_EDIT_REPORT = 2;
    public static final int IMPLEMENTED_COMMENTS_CHECK_REPORT = 3;

    public static final int SEGMENT_START_ROW = 7;
    public static final int SEGMENT_HEADER_ROW = 6;
    public static final int LANGUAGE_HEADER_ROW = 3;
    public static final int LANGUAGE_INFO_ROW = 4;

    private WritableCellFormat hightLightFormat = null;
    private WritableCellFormat hightLightRightFormat = null;

    private WritableCellFormat unlockedFormat = null;
    private WritableCellFormat unlockedRightFormat = null;
    private Locale uiLocale = new Locale("en", "US");

    private List<String> getFailureCategoriesList(String companyId)
    {
        List<String> result = new ArrayList<String>();

        ResourceBundle bundle = PageHandler.getBundle(request.getSession());
        List failureCategories = IssueOptions.getAllCategories(bundle,
                companyId);
        for (int i = 0; i < failureCategories.size(); i++)
        {
            Select aCategory = (Select) failureCategories.get(i);
            String cat = aCategory.getValue();
            result.add(cat.replace(",", " "));
        }
        return result;
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
        WorkbookSettings settings = new WorkbookSettings();
        settings.setSuppressWarnings(true);
        m_workbook = Workbook.createWorkbook(response.getOutputStream(),
                settings);
        String dateFormat = request.getParameter(WebAppConstants.DATE_FORMAT);
        if (dateFormat == null)
        {
            dateFormat = DEFAULT_DATE_FORMAT;
        }

        createReport(dateFormat);
        m_workbook.write();
        m_workbook.close();
    }

    private void addSegmentHeaderICC(WritableSheet p_sheet) throws Exception
    {
        ResourceBundle bundle = PageHandler.getBundle(request.getSession());
        int col = 0;
        int row = SEGMENT_HEADER_ROW;
        p_sheet.addCell(new Label(col++, row, bundle
                .getString("lb_job_id_report"), getHeaderFormat()));
        p_sheet.setColumnView(col - 1, 20);
        p_sheet.addCell(new Label(col++, row,
                bundle.getString("lb_segment_id"), getHeaderFormat()));
        p_sheet.setColumnView(col - 1, 20);

        p_sheet.addCell(new Label(col++, row, bundle.getString("lb_page_name"),
                getHeaderFormat()));
        p_sheet.setColumnView(col - 1, 20);

        p_sheet.addCell(new Label(col++, row, bundle
                .getString("lb_source_segment"), getHeaderFormat()));
        p_sheet.setColumnView(col - 1, 40);
        p_sheet.addCell(new Label(col++, row, bundle
                .getString("lb_target_segment"), getHeaderFormat()));
        p_sheet.addCell(new Label(col++, row, bundle.getString("lb_sid"),
                getHeaderFormat()));
        p_sheet.setColumnView(col - 1, 40);

        p_sheet.addCell(new Label(col++, row, bundle
                .getString("lb_comment_free"), getHeaderFormat()));
        p_sheet.setColumnView(col - 1, 40);

        p_sheet.addCell(new Label(col++, row, bundle
                .getString("lb_category_failure"), getHeaderFormat()));
        p_sheet.setColumnView(col - 1, 30);
        p_sheet.addCell(new Label(col++, row, bundle
                .getString("lb_tm_match_original"), getHeaderFormat()));
        p_sheet.setColumnView(col - 1, 30);
        p_sheet.addCell(new Label(col++, row, bundle
                .getString("lb_glossary_source"), getHeaderFormat()));
        p_sheet.setColumnView(col - 1, 30);
        p_sheet.addCell(new Label(col++, row, bundle
                .getString("lb_glossary_target"), getHeaderFormat()));
        p_sheet.setColumnView(col - 1, 30);
    }

    /**
     * Add job header to the sheet
     * 
     * @param p_sheet
     *            the sheet
     * @throws Exception
     */
    private void addLanguageHeader(WritableSheet p_sheet) throws Exception
    {
        ResourceBundle bundle = PageHandler.getBundle(request.getSession());

        int col = 0;
        int row = LANGUAGE_HEADER_ROW;
        p_sheet.addCell(new Label(col++, row, bundle
                .getString("lb_source_language"), getHeaderFormat()));
        p_sheet.setColumnView(col - 1, 30);
        p_sheet.addCell(new Label(col++, row, bundle
                .getString("lb_target_language"), getHeaderFormat()));
        p_sheet.setColumnView(col - 1, 30);
    }

    /**
     * 
     * @return format of the sheet header
     * @throws Exception
     */
    private CellFormat getHeaderFormat() throws Exception
    {
        if (headerFormat == null)
        {
            WritableFont headerFont = new WritableFont(WritableFont.TIMES, 11,
                    WritableFont.BOLD, false, UnderlineStyle.NO_UNDERLINE,
                    jxl.format.Colour.BLACK);
            WritableCellFormat format = new WritableCellFormat(headerFont);
            format.setWrap(true);
            format.setShrinkToFit(false);
            format.setBackground(jxl.format.Colour.GRAY_25);
            format.setBorder(jxl.format.Border.ALL,
                    jxl.format.BorderLineStyle.THIN, jxl.format.Colour.BLACK);

            headerFormat = format;
        }

        return headerFormat;
    }

    /**
     * Add title to the sheet
     * 
     * @param p_sheet
     *            the sheet
     * @throws Exception
     */
    private void addTitle(WritableSheet p_sheet) throws Exception
    {
        ResourceBundle bundle = PageHandler.getBundle(request.getSession());
        // Title font is black bold on white
        WritableFont titleFont = new WritableFont(WritableFont.TIMES, 14,
                WritableFont.BOLD, false, UnderlineStyle.NO_UNDERLINE,
                jxl.format.Colour.BLACK);
        WritableCellFormat titleFormat = new WritableCellFormat(titleFont);
        titleFormat.setWrap(false);
        titleFormat.setShrinkToFit(false);
        p_sheet.addCell(new Label(0, 0, bundle
                .getString("implemented_comments_check_report"), titleFormat));
        p_sheet.setColumnView(0, 40);
    }

    /**
     * Create the report
     * 
     * @throws Exception
     */
    private void createReport(String p_dateFormat) throws Exception
    {
        // Create Report Template
        WritableSheet sheet = getSheet();

        // Insert Data into Report
        writeLanguageInfo(sheet);
        writeSegmentInfoICC(sheet, p_dateFormat);
    }

    /**
     * Create sheet and specify its name.
     * 
     * @throws Exception
     */
    private WritableSheet getSheet() throws Exception
    {
        ResourceBundle bundle = PageHandler.getBundle(request.getSession());
        WritableSheet sheet = null;

        // Create Sheet
        sheet = m_workbook.createSheet(bundle.getString("lb_icc"), 0);

        // Add Title
        addTitle(sheet);

        // Add Locale Pair Header and Segment Header
        addLanguageHeader(sheet);
        addSegmentHeaderICC(sheet);

        return sheet;
    }

    /**
     * Write the Job information into the header
     * 
     * @param p_sheet
     *            the sheet
     * @throws Exception
     */
    private void writeLanguageInfo(WritableSheet p_sheet) throws Exception
    {
        int col = 0;
        int row = LANGUAGE_INFO_ROW;
        long jobId = 0;

        if (request.getParameter(WebAppConstants.JOB_ID) != null
                && !"*".equals(request.getParameter(WebAppConstants.JOB_ID)))
        {
            // Request from main reports entrance.
            jobId = Long.valueOf(request.getParameter(WebAppConstants.JOB_ID));
            writeLanguageInfo(p_sheet, jobId, EditUtil.utf8ToUnicode(request
                    .getParameter(WebAppConstants.TARGET_LANGUAGE)),
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
            writeLanguageInfo(p_sheet, jobId,
                    (String) sessionMgr
                            .getAttribute(WebAppConstants.TARGETVIEW_LOCALE),
                    DEFAULT_DATE_FORMAT);
        }
        else
        {
            // If no job exists, just set the cell blank.
            p_sheet.addCell(new Label(col++, row, ""));
            p_sheet.addCell(new Label(col++, row, ""));
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
    private void writeLanguageInfo(WritableSheet p_sheet, long p_jobId,
            String p_targetLang, String p_dateFormat) throws Exception
    {
        Job job = ServerProxy.getJobHandler().getJobById(p_jobId);
        int col = 0;
        int row = LANGUAGE_INFO_ROW;

        // Source Language
        p_sheet.addCell(new Label(col++, row, job.getSourceLocale()
                .getDisplayName(uiLocale), getContentFormat()));
        p_sheet.setColumnView(col - 1, 30);

        // Target Language
        p_sheet.addCell(new Label(col++, row, p_targetLang, getContentFormat()));
        p_sheet.setColumnView(col - 1, 30);

    }

    /**
     * 
     * @return format of the unlocked cell
     * @throws Exception
     */
    private WritableCellFormat unlockedFormat() throws Exception
    {
        if (unlockedFormat == null)
        {
            WritableCellFormat format = new WritableCellFormat();
            format.setLocked(false);
            format.setWrap(true);
            format.setShrinkToFit(false);
            format.setAlignment(Alignment.LEFT);
            format.setVerticalAlignment(VerticalAlignment.CENTRE);

            unlockedFormat = format;
        }

        return unlockedFormat;
    }

    private WritableCellFormat unlockedRightFormat() throws Exception
    {
        if (unlockedRightFormat == null)
        {
            WritableCellFormat format = new WritableCellFormat();
            format.setLocked(false);
            format.setWrap(true);
            format.setShrinkToFit(false);
            format.setAlignment(Alignment.RIGHT);
            format.setVerticalAlignment(VerticalAlignment.CENTRE);

            unlockedRightFormat = format;
        }

        return unlockedRightFormat;
    }

    /**
     * 
     * @return format of the report content
     * @throws Exception
     */
    private WritableCellFormat getContentFormat() throws Exception
    {
        if (contentFormat == null)
        {
            WritableCellFormat format = new WritableCellFormat();
            format.setWrap(true);
            format.setShrinkToFit(false);
            format.setAlignment(Alignment.LEFT);
            format.setVerticalAlignment(VerticalAlignment.CENTRE);

            contentFormat = format;
        }

        return contentFormat;
    }

    /**
     * 
     * @return format of the report content
     * @throws Exception
     */
    private WritableCellFormat getRtlContentFormat() throws Exception
    {
        if (rtlContentFormat == null)
        {
            WritableCellFormat format = new WritableCellFormat();
            format.setWrap(true);
            format.setShrinkToFit(false);
            format.setAlignment(Alignment.RIGHT);
            format.setVerticalAlignment(VerticalAlignment.CENTRE);

            rtlContentFormat = format;
        }

        return rtlContentFormat;
    }

    /**
     * Get category failures
     * 
     * @return
     * @throws Exception
     */
    private WritableCellFeatures getSelectFeatures(String companyId)
            throws Exception
    {
        WritableCellFeatures features = new WritableCellFeatures();
        features.setDataValidationList(getFailureCategoriesList(companyId));
        return features;
    }

    /**
     * Get category status
     */
    private WritableCellFeatures getSelectFeaturesCommentStatus(String comment)
            throws Exception
    {
        List<String> status = new ArrayList<String>();
        if ("".equals(comment))
        {
            status.add(Issue.STATUS_QUERY);
        }
        else
        {
            status.addAll(IssueOptions.getAllStatus());
        }

        WritableCellFeatures features = new WritableCellFeatures();
        features.setDataValidationList(status);
        return features;
    }

    private List<Long> getAllTLIDS() throws Exception
    {
        List<Long> result = new ArrayList<Long>();
        for (Iterator it = ServerProxy.getLocaleManager().getAllTargetLocales()
                .iterator(); it.hasNext();)
        {
            GlobalSightLocale gsl = (GlobalSightLocale) it.next();
            result.add(gsl.getId());
        }
        return result;
    }

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
    private void writeSegmentInfoICC(WritableSheet p_sheet, String p_dateFormat)
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
            writeSegmentInfoICC(p_sheet, jobId, targetLang, "", row);
        }
        else
        {
            // If no job exists, just set the cell blank
            writeBlank(p_sheet, row, 10);
        }

    }

    /**
     * For Implemented Comments Check Report, Write segment information into
     * each row of the sheet.
     * 
     * @see writeSegmentInfoRCR(WritableSheet, long, long, String, int)
     */
    private int writeSegmentInfoICC(WritableSheet p_sheet, long p_jobId,
            String p_targetLang, String p_srcPageId, int p_row)
            throws Exception
    {
        ResourceBundle bundle = PageHandler.getBundle(request.getSession());
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
                    .equals(workflow.getTargetLocale().getDisplayName()))
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
                writeBlank(p_sheet, SEGMENT_START_ROW, 10);
            }
        }
        else
        {
            LeverageMatchLingManager leverageMatchLingManager = LingServerProxy
                    .getLeverageMatchLingManager();
            TermLeverageManager termLeverageManager = ServerProxy
                    .getTermLeverageManager();
            CommentManager commentManager = ServerProxy.getCommentManager();

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
                        .getTermMatchesForPages(
                                new HashSet<SourcePage>(job.getSourcePages()),
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
                List targetTuvs = getPageTuvs(targetPage);
                List sourceTuvs = getPageTuvs(sourcePage);
                Map exactMatches = leverageMatchLingManager.getExactMatches(
                        sourcePage.getIdAsLong(),
                        new Long(targetPage.getLocaleId()));
                Map leverageMatcheMap = leverageMatchLingManager
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
                List<IssueImpl> issues = commentManager.getIssues(
                        Issue.TYPE_SEGMENT, targetPage.getId());

                for (int j = 0; j < targetTuvs.size(); j++)
                {
                    int col = 0;
                    Tuv targetTuv = (Tuv) targetTuvs.get(j);
                    Tuv sourceTuv = (Tuv) sourceTuvs.get(j);
                    String targetLanguage = targetTuv.getGlobalSightLocale()
                            .getLanguage();

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
                    if (issues != null)
                    {
                        for (int m = 0; m < issues.size(); m++)
                        {
                            Issue issue = (Issue) issues.get(m);
                            String logicKey = CommentHelper.makeLogicalKey(
                                    targetPage.getId(),
                                    targetTuv.getTu(companyId).getId(),
                                    targetTuv.getId(), 0);

                            if (logicKey.equals(issue.getLogicalKey()))
                            {
                                issueHistories = issue.getHistory();
                                failure = issue.getCategory();
                                break;
                            }
                        }
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

                    String matches = "";
                    Set leverageMatches = (Set) leverageMatcheMap.get(sourceTuv
                            .getIdAsLong());
                    if (exactMatches.get(sourceTuv.getIdAsLong()) != null)
                    {
                        matches = StringUtil.formatPCT(100);
                    }
                    else if (leverageMatches != null)
                    {
                        int count = 0;
                        for (Iterator ite = leverageMatches.iterator(); ite
                                .hasNext();)
                        {
                            LeverageMatch leverageMatch = (LeverageMatch) ite
                                    .next();
                            if ((leverageMatches.size() > 1))
                            {
                                matches = matches
                                        + (++count)
                                        + ", "
                                        + StringUtil.formatPCT(leverageMatch
                                                .getScoreNum()) + "\r\n";
                            }
                            else
                            {
                                matches = matches
                                        + StringUtil.formatPCT(leverageMatch
                                                .getScoreNum());
                                break;
                            }

                        }
                    }
                    else
                    {
                        matches = bundle.getString("lb_no_match_report");
                    }
                    if (targetTuv.isRepeated())
                    {
                        matches += "\r\n"
                                + bundle.getString("jobinfo.tradosmatches.invoice.repeated");
                    }
                    else if (targetTuv.getRepetitionOfId() > 0)
                    {
                        matches += "\r\n"
                                + bundle.getString("jobinfo.tradosmatches.invoice.repetition");
                    }

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

                    WritableCellFormat format = getContentFormat();
                    // Set color format for target segment changed
                    if (targetSegmentModifiedDate.before(issueCreatedDate))
                    {
                        format = redFormat();
                    }

                    // Job id
                    p_sheet.addCell(new Number(col++, p_row, p_jobId, format));
                    p_sheet.setColumnView(col - 1, 20);
                    // Segment id
                    p_sheet.addCell(new Number(col++, p_row, sourceTuv.getTu(
                            companyId).getId(), format));
                    p_sheet.setColumnView(col - 1, 20);

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
                    p_sheet.addCell(new Label(col++, p_row, Name, format));
                    p_sheet.setColumnView(col - 1, 20);

                    // Source segment
                    WritableCellFormat sourceFormat = m_rtlSourceLocale ? getRtlContentFormat()
                            : format;
                    String srcContent = m_rtlSourceLocale ? EditUtil
                            .toRtlString(sourceSegmentString)
                            : sourceSegmentString;

                    p_sheet.addCell(new Label(col++, p_row, srcContent,
                            sourceFormat));
                    p_sheet.setColumnView(col - 1, 40);

                    // Target segment
                    WritableCellFormat targetFormat = m_rtlTargetLocale ? getRtlContentFormat()
                            : format;
                    String content = m_rtlTargetLocale ? EditUtil
                            .toRtlString(targetSegmentString)
                            : targetSegmentString;

                    p_sheet.addCell(new Label(col++, p_row, content,
                            targetFormat));
                    p_sheet.setColumnView(col - 1, 40);

                    p_sheet.addCell(new Label(col++, p_row, sid, format));
                    p_sheet.setColumnView(col - 1, 40);

                    WritableCellFormat commentFormat = m_rtlTargetLocale ? unlockedRightFormat()
                            : unlockedFormat();
                    // Set color format for target segment changed
                    if (targetSegmentModifiedDate.before(issueCreatedDate))
                    {
                        commentFormat = m_rtlTargetLocale ? redRightFormat()
                                : redFormat();
                    }

                    if (m_rtlTargetLocale)
                    {
                        lastComment = EditUtil.toRtlString(lastComment);
                    }

                    p_sheet.addCell(new Label(col++, p_row, lastComment,
                            commentFormat));
                    p_sheet.setColumnView(col - 1, 40);

                    // Category failure
                    WritableCellFormat cfFormat = unlockedFormat();

                    // Set color format for target segment changed
                    if (targetSegmentModifiedDate.before(issueCreatedDate))
                    {
                        cfFormat = redFormat();
                    }
                    Label dropdown = null;

                    // String currentCompanyId =
                    // CompanyThreadLocal.getInstance().getValue();
                    // failureCategories = IssueOptions.getAllCategories(bundle,
                    // currentCompanyId);
                    // List<String> failureCategories =
                    // getFailureCategoriesList();
                    // int index = failureCategories.indexOf(failure);
                    // if (index != -1)
                    // {
                    dropdown = new Label(col++, p_row, failure, cfFormat);
                    dropdown.setCellFeatures(getSelectFeatures(String.valueOf(companyId)));
                    // }
                    // else
                    // {
                    // dropdown = new Label(col++, p_row, failure, cfFormat);
                    // dropdown.setCellFeatures(getSelectFeatures());
                    // }
                    p_sheet.addCell(dropdown);
                    p_sheet.setColumnView(col - 1, 30);

                    // TM match
                    p_sheet.addCell(new Label(col++, p_row, matches, format));
                    p_sheet.setColumnView(col - 1, 30);

                    // Glossary source
                    p_sheet.addCell(new Label(col++, p_row, sourceTerms, format));
                    p_sheet.setColumnView(col - 1, 30);

                    // Glossary target
                    p_sheet.addCell(new Label(col++, p_row, targetTerms, format));
                    p_sheet.setColumnView(col - 1, 30);
                    p_row++;
                }
            }
        }

        return p_row;
    }

    private WritableCellFormat redRightFormat() throws WriteException
    {
        WritableCellFormat wcfFC = null;
        WritableFont wfc = null;
        if (hightLightRightFormat == null)
        {
            wfc = new WritableFont(WritableFont.ARIAL, 10,
                    WritableFont.NO_BOLD, false, UnderlineStyle.NO_UNDERLINE,
                    jxl.format.Colour.BLACK);
            wcfFC = new WritableCellFormat(wfc);
            wcfFC.setWrap(true);
            wcfFC.setShrinkToFit(false);
            wcfFC.setAlignment(Alignment.RIGHT);
            wcfFC.setVerticalAlignment(VerticalAlignment.CENTRE);
            wcfFC.setBackground(jxl.format.Colour.ROSE);
            hightLightRightFormat = wcfFC;
        }

        return hightLightRightFormat;
    }

    private WritableCellFormat redFormat() throws WriteException
    {
        WritableCellFormat wcfFC = null;
        WritableFont wfc = null;
        if (hightLightFormat == null)
        {
            wfc = new WritableFont(WritableFont.ARIAL, 10,
                    WritableFont.NO_BOLD, false, UnderlineStyle.NO_UNDERLINE,
                    jxl.format.Colour.BLACK);
            wcfFC = new WritableCellFormat(wfc);
            wcfFC.setWrap(true);
            wcfFC.setShrinkToFit(false);
            wcfFC.setAlignment(Alignment.LEFT);
            wcfFC.setVerticalAlignment(VerticalAlignment.CENTRE);
            wcfFC.setBackground(jxl.format.Colour.ROSE);
            hightLightFormat = wcfFC;
        }

        return hightLightFormat;
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
    private void writeBlank(WritableSheet p_sheet, int p_row, int p_colLen)
            throws Exception
    {
        for (int col = 0; col < p_colLen; col++)
        {
            p_sheet.addCell(new Label(col++, p_row, ""));
        }
    }

    /**
     * Get tuvs of source page from database. Return a list
     * 
     * @param p_sourcePage
     *            source page
     * @throws Exception
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private List getPageTuvs(SourcePage p_sourcePage) throws Exception
    {
        return new ArrayList(ServerProxy.getTuvManager()
                .getSourceTuvsForStatistics(p_sourcePage));
    }

    /**
     * Get tuvs of target page from database. Return a list
     * 
     * @param p_targetPage
     *            target page
     * @throws Exception
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private List getPageTuvs(TargetPage p_targetPage) throws Exception
    {
        return new ArrayList(ServerProxy.getTuvManager()
                .getTargetTuvsForStatistics(p_targetPage));
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
}
