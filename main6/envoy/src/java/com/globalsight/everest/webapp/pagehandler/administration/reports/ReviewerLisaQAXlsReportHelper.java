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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
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

import jxl.SheetSettings;
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
import com.globalsight.everest.comment.IssueOptions;
import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.edit.CommentHelper;
import com.globalsight.everest.edit.SegmentRepetitions;
import com.globalsight.everest.integration.ling.LingServerProxy;
import com.globalsight.everest.integration.ling.tm2.LeverageMatch;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.page.SourcePage;
import com.globalsight.everest.page.TargetPage;
import com.globalsight.everest.projecthandler.TranslationMemoryProfile;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.taskmanager.Task;
import com.globalsight.everest.tuv.TaskTuv;
import com.globalsight.everest.tuv.Tuv;
import com.globalsight.everest.tuv.TuvManager;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.pagehandler.administration.company.Select;
import com.globalsight.everest.webapp.pagehandler.administration.users.UserUtil;
import com.globalsight.everest.workflowmanager.Workflow;
import com.globalsight.ling.tm.LeverageMatchLingManager;
import com.globalsight.ling.tw.PseudoConstants;
import com.globalsight.ling.tw.PseudoData;
import com.globalsight.ling.tw.TmxPseudo;
import com.globalsight.terminology.ITermbase;
import com.globalsight.terminology.ITermbaseManager;
import com.globalsight.terminology.termleverager.TermLeverageManager;
import com.globalsight.terminology.termleverager.TermLeverageOptions;
import com.globalsight.terminology.termleverager.TermLeverageResult;
import com.globalsight.terminology.termleverager.TermLeverageResult.MatchRecord;
import com.globalsight.terminology.termleverager.TermLeverageResult.MatchRecordList;
import com.globalsight.terminology.termleverager.TermLeverageResult.TargetTerm;
import com.globalsight.util.GeneralException;
import com.globalsight.util.StringUtil;
import com.globalsight.util.edit.EditUtil;

public class ReviewerLisaQAXlsReportHelper
{
    private HttpServletRequest request = null;

    private HttpServletResponse response = null;

    private SessionManager sessionMgr = null;

    private WritableWorkbook m_workbook = null;

//    private NumberFormat percent = null;

    private WritableCellFormat contentFormat = null;
    
    private WritableCellFormat rtlContentFormat = null;

    private WritableCellFormat headerFormat = null;

    private int m_reportType = COMMENTS_ANALYSIS;

    private static final String DEFAULT_DATE_FORMAT = "MM/dd/yy hh:mm:ss a z";

    public static final int LANGUAGE_SIGN_OFF = 0;

    public static final int COMMENTS_ANALYSIS = 1;

    public static final int TRANSLATIONS_EDIT_REPORT = 2;

    public static final int SEGMENT_START_ROW = 7;

    public static final int SEGMENT_HEADER_ROW = 6;

    public static final int LANGUAGE_HEADER_ROW = 3;

    public static final int LANGUAGE_INFO_ROW = 4;

    private boolean isLSOExt = false;

    private WritableCellFormat hightLightFormat = null;
    private WritableCellFormat hightLightRightFormat = null;

    private WritableCellFormat unlockedFormat = null;
    private WritableCellFormat unlockedRightFormat = null;
    private Locale uiLocale = new Locale("en", "US");

//    private static List failureCategories = null;

//    static
//    {
//        failureCategories.remove(Issue.CATEGORY_SPELLING);
//        failureCategories.add(Issue.CATEGORY_SPELLING.replaceAll(",", ""));
//    }

    private List<String> getFailureCategoriesList()
    {
        List<String> result = new ArrayList<String>();
        
        ResourceBundle bundle = PageHandler.getBundle(request.getSession());
        String currentCompanyId = CompanyThreadLocal.getInstance().getValue();
        List failureCategories = IssueOptions.getAllCategories(bundle, currentCompanyId);
        for (int i = 0; i < failureCategories.size(); i++)
        {
            Select aCategory = (Select) failureCategories.get(i);
            String cat = aCategory.getValue();
            result.add(cat);
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
        /*percent = NumberFormat.getPercentInstance((Locale) session
                .getAttribute(WebAppConstants.UILOCALE));   */
        sessionMgr = (SessionManager) session
                .getAttribute(WebAppConstants.SESSION_MANAGER);
        m_reportType = p_reportType;

        // For Implemented Comments Check
        String isLSOExtStr = (String) p_request.getParameter("isLSOExt");
        isLSOExt = isLSOExtStr == null ? false : new Boolean(isLSOExtStr)
                .booleanValue();

        String companyName = UserUtil.getCurrentCompanyName(request);
        CompanyThreadLocal.getInstance().setValue(companyName);
        uiLocale = (Locale)request.getSession().getAttribute(WebAppConstants.UILOCALE);
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

    /**
     * Add header to the sheet
     * 
     * @param p_sheet
     *            the sheet
     * @throws Exception
     */
    private void addHeader(WritableSheet p_sheet) throws Exception
    {
        // add title
        addTitle(p_sheet);
        // add job header
        addLanguageHeader(p_sheet);
        // add segment header
        addSegmentHeader(p_sheet);
    }

    /**
     * Add segment header to the sheet
     * 
     * @param p_sheet
     *            the sheet
     * @throws Exception
     */
    private void addSegmentHeader(WritableSheet p_sheet) throws Exception
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

		//Fix for GBS-1484
        if(m_reportType==LANGUAGE_SIGN_OFF)
        {
        	p_sheet.addCell(new Label(col++, row, bundle
                    .getString("lb_page_name"), getHeaderFormat()));
        }
        else
        {
        	p_sheet.addCell(new Label(col++, row, bundle
                    .getString("lb_targetpage_id"), getHeaderFormat()));
        }
        p_sheet.setColumnView(col - 1, 20);

        p_sheet.addCell(new Label(col++, row, bundle
                .getString("lb_source_segment"), getHeaderFormat()));
        p_sheet.setColumnView(col - 1, 40);
        p_sheet.addCell(new Label(col++, row, bundle
                .getString("lb_target_segment"), getHeaderFormat()));
        p_sheet.addCell(new Label(col++, row, bundle.getString("lb_sid"),
                getHeaderFormat()));
        p_sheet.setColumnView(col - 1, 40);

        switch (m_reportType)
        {
        case LANGUAGE_SIGN_OFF:
            p_sheet.addCell(new Label(col++, row, bundle
                    .getString("lb_comment_free"), getHeaderFormat()));
            p_sheet.setColumnView(col - 1, 40);
            break;
        case COMMENTS_ANALYSIS:
            p_sheet.addCell(new Label(col++, row, bundle
                    .getString("lb_character_count"), getHeaderFormat()));
            p_sheet.setColumnView(col - 1, 30);
            p_sheet.addCell(new Label(col++, row, bundle
                    .getString("lb_comments"), getHeaderFormat()));
            p_sheet.setColumnView(col - 1, 40);
            break;
        case TRANSLATIONS_EDIT_REPORT:
            p_sheet.addCell(new Label(col++, row, bundle
                    .getString("lb_target_segment_with_compact_tags"),
                    getHeaderFormat()));
            p_sheet.setColumnView(col - 1, 40);

            p_sheet.addCell(new Label(col++, row, bundle
                    .getString("lb_required_translation"), getHeaderFormat()));
            p_sheet.setColumnView(col - 1, 40);
            
            p_sheet.addCell(new Label(col++, row, bundle
                    .getString("lb_comments"), getHeaderFormat()));
            break;
        }

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

        if (m_reportType == COMMENTS_ANALYSIS)
        {
            p_sheet
                    .addCell(new Label(col++, row, bundle
                            .getString("lb_previous_segment_report"),
                            getHeaderFormat()));
            p_sheet.setColumnView(col - 1, 40);
        }
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
        switch (m_reportType)
        {
        case LANGUAGE_SIGN_OFF:
            if (isLSOExt)
                p_sheet.addCell(new Label(0, 0, bundle
                        .getString("implemented_comments_check_report"),
                        titleFormat));
            else
                p_sheet.addCell(new Label(0, 0, bundle
                        .getString("review_reviewers_comments"), titleFormat));
            break;
        case COMMENTS_ANALYSIS:
            p_sheet.addCell(new Label(0, 0,
                    bundle.getString("review_comments"), titleFormat));
            break;
        case TRANSLATIONS_EDIT_REPORT:
            p_sheet.addCell(new Label(0, 0, bundle
                    .getString("lb_translation_edit_report"), titleFormat));
            break;
        }
        p_sheet.setColumnView(0, 40);
    }

    /**
     * Create the report
     * 
     * @throws Exception
     */
    private void createReport(String p_dateFormat) throws Exception
    {
        WritableSheet sheet = getSheet();
        writeLanguageInfo(sheet);
        writeSegmentInfo(sheet, p_dateFormat);
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
        switch (m_reportType)
        {
        case LANGUAGE_SIGN_OFF:
            if (isLSOExt)
            {
                sheet = m_workbook.createSheet(bundle.getString("lb_icc"), 0);
            }
            else
            {
                sheet = m_workbook.createSheet(bundle.getString("lb_lso"), 0);
                SheetSettings ss = sheet.getSettings();
                ss.setProtected(true);
            }
            break;
        case COMMENTS_ANALYSIS:
            sheet = m_workbook.createSheet(bundle.getString("lb_car"), 0);
            break;
        case TRANSLATIONS_EDIT_REPORT:
            sheet = m_workbook.createSheet(bundle.getString("lb_ter"), 0);
            SheetSettings ss = sheet.getSettings();
            ss.setProtected(true);
            break;
        }

        addHeader(sheet);
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

        if (request.getParameter(WebAppConstants.JOB_ID) != null
                && !"*".equals(request.getParameter(WebAppConstants.JOB_ID)))
        {
            // Request from main reports entrance.
            writeLanguageInfo(p_sheet, request
                    .getParameter(WebAppConstants.JOB_ID), EditUtil
                    .utf8ToUnicode(request
                            .getParameter(WebAppConstants.TARGET_LANGUAGE)),
                    request.getParameter(WebAppConstants.DATE_FORMAT));
        }
        else if (sessionMgr.getAttribute(WebAppConstants.JOB_ID) != null)
        {
            // Request from pop up editor or work offline page.
            writeLanguageInfo(p_sheet, (String) sessionMgr
                    .getAttribute(WebAppConstants.JOB_ID), (String) sessionMgr
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
            Locale p_targetLocale) throws Exception
    {
        TermLeverageOptions options = null;

        Locale sourceLocale = p_sourceLocale;
        Locale targetLocale = p_targetLocale;
        String termbaseName = null;
        String companyId = null;
        if (request.getParameter(WebAppConstants.JOB_ID) != null
                && !"*".equals(request.getParameter(WebAppConstants.JOB_ID)))
        {
            // Request from main reports entrance.
            Job job = ServerProxy.getJobHandler().getJobById(
                    Long
                            .parseLong(request
                                    .getParameter(WebAppConstants.JOB_ID)));
            termbaseName = job.getL10nProfile().getProject().getTermbaseName();
            companyId = job.getCompanyId();
        }
        else if (sessionMgr.getAttribute(WebAppConstants.JOB_ID) != null)
        {
            // Request from pop up editor or work offline page.
            Job job = ServerProxy.getJobHandler().getJobById(
                    Long.parseLong((String) sessionMgr
                            .getAttribute(WebAppConstants.JOB_ID)));
            termbaseName = job.getL10nProfile().getProject().getTermbaseName();
            companyId = job.getCompanyId();
        }

        try
        {
            ITermbaseManager manager = ServerProxy.getTermbaseManager();
            long termbaseId;
            if (companyId != null) {
                termbaseId = manager.getTermbaseId(termbaseName, companyId);    
            } else {
                termbaseId = manager.getTermbaseId(termbaseName);
            }

            // If termbase does not exist, return null options.
            if (termbaseId == -1)
            {
                return null;
            }

            options = new TermLeverageOptions();
            options.addTermBase(termbaseName);
            options.setLoadTargetTerms(true);
            options.setSaveToDatabase(false);

            // fuzzy threshold set by object constructor - use defaults.
            options.setFuzzyThreshold(0);

            ITermbase termbase = null;
            if (companyId != null) {
                termbase = manager.connect(termbaseName, ITermbase.SYSTEM_USER,
                        "", companyId);
            } else {
                termbase = manager.connect(termbaseName,ITermbase.SYSTEM_USER, "");
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
    private void writeLanguageInfo(WritableSheet p_sheet, String p_jobId,
            String p_targetLang, String p_dateFormat) throws Exception
    {
        Job job = ServerProxy.getJobHandler().getJobById(
                Long.parseLong(p_jobId));
        int col = 0;
        int row = LANGUAGE_INFO_ROW;

        // Source Language
        p_sheet.addCell(new Label(col++, row, job.getSourceLocale()
                .getDisplayName(uiLocale), getContentFormat()));
        p_sheet.setColumnView(col - 1, 30);

        // Target Language
        p_sheet
                .addCell(new Label(col++, row, p_targetLang, getContentFormat()));
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

    private WritableCellFeatures getSelectFeatures() throws Exception
    {
        WritableCellFeatures features = new WritableCellFeatures();
        features.setDataValidationList(getFailureCategoriesList());
        return features;
    }

    /**
     * Write segment information into each row of the sheet
     * 
     * @param p_sheet
     *            the sheet
     * @throws Exception
     */
    private void writeSegmentInfo(WritableSheet p_sheet, String p_dateFormat)
            throws Exception
    {
        if (m_reportType == COMMENTS_ANALYSIS)
        {
            if (request.getParameter(WebAppConstants.JOB_ID) != null
                    && !"*"
                            .equals(request
                                    .getParameter(WebAppConstants.JOB_ID)))
            {
                // request from main reports entrance
                writeCommentsAnalysisSegmentInfo(
                        p_sheet,
                        request.getParameter(WebAppConstants.JOB_ID),
                        EditUtil.utf8ToUnicode(request
                                .getParameter(WebAppConstants.TARGET_LANGUAGE)),
                        "", p_dateFormat);
            }
            else if (sessionMgr.getAttribute(WebAppConstants.JOB_ID) != null)
            {
                // request from pop up editor
                writeCommentsAnalysisSegmentInfo(
                        p_sheet,
                        (String) sessionMgr
                                .getAttribute(WebAppConstants.JOB_ID),
                        (String) sessionMgr
                                .getAttribute(WebAppConstants.TARGETVIEW_LOCALE),
                        (String) sessionMgr
                                .getAttribute(WebAppConstants.SOURCE_PAGE_ID),
                        p_dateFormat);
            }
            else
            {
                // If no job exists, just set the cell blank
                writeCommentsAnalysisBlank(p_sheet);

            }
        }
        else
        {
            if (request.getParameter(WebAppConstants.JOB_ID) != null
                    && !"*"
                            .equals(request
                                    .getParameter(WebAppConstants.JOB_ID)))
            {
                // request from main reports entrance
                writeLanguageSignOffSegmentInfo(
                        p_sheet,
                        request.getParameter(WebAppConstants.JOB_ID),
                        EditUtil.utf8ToUnicode(request
                                .getParameter(WebAppConstants.TARGET_LANGUAGE)),
                        "");
            }
            else if (sessionMgr.getAttribute(WebAppConstants.JOB_ID) != null)
            {
                // request from pop up editor
                writeLanguageSignOffSegmentInfo(
                        p_sheet,
                        (String) sessionMgr
                                .getAttribute(WebAppConstants.JOB_ID),
                        (String) sessionMgr
                                .getAttribute(WebAppConstants.TARGETVIEW_LOCALE),
                        "");

            }
            else
            {
                // If no job exists, just set the cell blank
                writeLanguageSignOffBlank(p_sheet);
            }
        }

    }

    /**
     * Write segment information of Language Sign-off report into each row of
     * the sheet
     * 
     * @param p_sheet
     *            the sheet
     * @param p_jobId
     *            the job id
     * @param p_targetLang
     *            the displayed target language
     * @param p_srcPageId
     *            the source page id
     * @throws Exception
     */
    private void writeLanguageSignOffSegmentInfo(WritableSheet p_sheet,
            String p_jobId, String p_targetLang, String p_srcPageId)
            throws Exception
    {
        ResourceBundle bundle = PageHandler.getBundle(request.getSession());
        Job job = ServerProxy.getJobHandler().getJobById(
                Long.parseLong(p_jobId));
        Collection wfs = job.getWorkflows();
        Iterator it = wfs.iterator();
        Vector targetPages = new Vector();

        TranslationMemoryProfile tmp = null;
        List excludItems = null;

        while (it.hasNext())
        {
            Workflow workflow = (Workflow) it.next();

            if (Workflow.PENDING.equals(workflow.getState())
                    || Workflow.CANCELLED.equals(workflow.getState())
                    || Workflow.EXPORT_FAILED.equals(workflow.getState())
                    || Workflow.IMPORT_FAILED.equals(workflow.getState()))
            {
                continue;
            }
            if (p_targetLang
                    .equals(workflow.getTargetLocale().getDisplayName()))
            {
                targetPages = workflow.getTargetPages();
                tmp = workflow.getJob().getL10nProfile()
                        .getTranslationMemoryProfile();
                if (tmp != null)
                {
                    excludItems = new ArrayList(tmp.getJobExcludeTuTypes());
                }
            }
        }

        if (targetPages.isEmpty())
        {
            // If no corresponding target page exists, set the cell blank
            writeLanguageSignOffBlank(p_sheet);
        }
        else
        {
            int row = SEGMENT_START_ROW;
            long jobId = Long.parseLong(p_jobId);

            LeverageMatchLingManager leverageMatchLingManager = LingServerProxy
                    .getLeverageMatchLingManager();
            TermLeverageManager termLeverageManager = ServerProxy
                    .getTermLeverageManager();

            CommentManager commentManager = ServerProxy.getCommentManager();

            ArrayList allSourceTuvs = new ArrayList();
            for (int i = 0; i < targetPages.size(); i++)
            {
                TargetPage targetPage = (TargetPage) targetPages.get(i);
                SourcePage sourcePage = targetPage.getSourcePage();
                List sourceTuvs = getPageTuvs(sourcePage);
                allSourceTuvs.addAll(sourceTuvs);
            }

            SegmentRepetitions segmentRepetitions = new SegmentRepetitions(
                    allSourceTuvs);

            String category = null;
            PseudoData pData = new PseudoData();
            pData.setMode(PseudoConstants.PSEUDO_COMPACT);
            String sourceSegmentString = null;
            String targetSegmentString = null;
            String sid = null;
            String pTagSourceString = null;
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
                        sourcePage.getIdAsLong(), new Long(targetPage
                                .getLocaleId()));
                Map leverageMatcheMap = leverageMatchLingManager
                        .getFuzzyMatches(sourcePage.getIdAsLong(), new Long(
                                targetPage.getLocaleId()));

                Locale sourcePageLocale = sourcePage.getGlobalSightLocale()
                        .getLocale();
                Locale targetPageLocale = targetPage.getGlobalSightLocale()
                        .getLocale();
                
                boolean m_rtlSourceLocale = EditUtil.isRTLLocale(sourcePageLocale.toString());
                boolean m_rtlTargetLocale = EditUtil.isRTLLocale(targetPageLocale.toString());
                
                TermLeverageOptions termLeverageOptions = getTermLeverageOptions(
                        sourcePageLocale, targetPageLocale);

                TermLeverageResult termLeverageResult = null;
                if (termLeverageOptions != null)
                {
                    termLeverageResult = termLeverageManager.leverageTerms(
                            sourceTuvs, termLeverageOptions, sourcePage.getCompanyId());
                }
                // Find segment all comments belong to this target page
                List issues = null;
                issues = commentManager.getIssues(Issue.TYPE_SEGMENT, String
                        .valueOf(targetPage.getId())
                        + "_");

                for (int j = 0; j < targetTuvs.size(); j++)
                {
                    int col = 0;
                    Tuv targetTuv = (Tuv) targetTuvs.get(j);
                    Tuv sourceTuv = (Tuv) sourceTuvs.get(j);
                    String targetLanguage = targetTuv.getGlobalSightLocale()
                            .getLanguage();

                    category = sourceTuv.getTu().getTuType();
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
                                    targetPage.getId(), targetTuv.getTu()
                                            .getId(), targetTuv.getId(), 0);

                            if (logicKey.equals(issue.getLogicalKey()))
                            {
                                issueHistories = issue.getHistory();
                                failure = issue.getCategory();
//                                if (Issue.CATEGORY_SPELLING
//                                        .equalsIgnoreCase(failure))
//                                {
//                                    failure = failure.replaceAll(",", "");
//                                }
                                break;
                            }
                        }
                    }
                    if (issueHistories != null && issueHistories.size() > 0)
                    {
                        // int lastIndex = issueHistories.size() - 1;
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
                                        + StringUtil.formatPCT(
                                             leverageMatch.getScoreNum())
                                        + "\r\n";
                            }
                            else
                            {
                                matches = matches
                                        + StringUtil.formatPCT(
                                             leverageMatch.getScoreNum());
                                break;
                            }

                        }
                    }
                    else
                    {
                        matches = bundle.getString("lb_no_match_report");
                    }
                    if (sourceTuv.getTu().isRepeated()) {
                        matches += "\r\n"
                                + bundle
                                        .getString("jobinfo.tradosmatches.invoice.repeated");
                    } else if (sourceTuv.getTu().getRepetitionOfId() != 0) {
                        matches += "\r\n"
                                + bundle
                                        .getString("jobinfo.tradosmatches.invoice.repetition");
                    }

                    MatchRecordList matchRecordList = null;
                    String sourceTerms = "";
                    String targetTerms = "";

                    if (termLeverageResult != null)
                    {
                        matchRecordList = termLeverageResult
                                .getMatchesForTuv(sourceTuv);
                    }

                    if (matchRecordList != null && matchRecordList.size() > 0)
                    {
                        String sourceTerm = null;
                        String targetTerm = null;
                        int maxScore = 0;
                        int score = 0;
                        int flag = 0;
                        if (matchRecordList.size() > 1)
                        {
                            for (int ni = 0; ni < matchRecordList.size(); ni++)
                            {
                                MatchRecord mr = (MatchRecord) matchRecordList
                                        .get(ni);
                                score = mr.getScore();
                                if (sourceSegmentString.indexOf(mr
                                        .getMatchedSourceTerm()) < 0)
                                {
                                    continue;
                                }
                                if (maxScore < score)
                                {
                                    maxScore = score;
                                    flag = ni;
                                }
                            }
                        }
                        MatchRecord matchRecord = (MatchRecord) matchRecordList
                                .get(flag);
                        sourceTerm = matchRecord.getMatchedSourceTerm();
                        if (sourceSegmentString.indexOf(sourceTerm) != -1)
                        {
                            List targets = matchRecord.getSourceTerm()
                                    .getTargetTerms();
                            {
                                for (int ti = 0; ti < targets.size(); ti++)
                                {
                                    TargetTerm tt = (TargetTerm) targets
                                            .get(ti);
                                    String targetTermLocale = tt.getLocale();
                                    // Get the target term by language
                                    if (targetLanguage.equals(targetTermLocale))
                                    {
                                        targetTerm = tt.getMatchedTargetTerm();
                                        sourceTerms = sourceTerm;
                                        targetTerms = targetTerm;
                                        break;
                                    }

                                }
                            }

                        }

                    }

                    // For Implemented Comments Check Report (LSO Extension)
                    if (isLSOExt && lastComment.length() == 0)
                    {
                        continue;
                    }

                    WritableCellFormat format = getContentFormat();
                    // Set color format for target segment changed
                    if (isLSOExt
                            && targetSegmentModifiedDate
                                    .before(issueCreatedDate))
                    {
                        format = redFormat();
                    }

                    // Job id
                    p_sheet.addCell(new Number(col++, row, jobId, format));
                    p_sheet.setColumnView(col - 1, 20);
                    // Segment id
                    p_sheet.addCell(new Number(col++, row, sourceTuv.getTu()
                            .getId(), format));
                    p_sheet.setColumnView(col - 1, 20);

                    //Fix for GBS-1484
                    String externalPageId = sourcePage.getExternalPageId();
                    String[] pathNames = externalPageId.split("\\\\");
                    String Name = pathNames[pathNames.length-1];
                    boolean temp = pathNames[0].contains(")");
                    if(temp)
                    {
                    	String[] firstNames = pathNames[0].split("\\)");
                        String detailName = firstNames[0];
                        Name = Name+detailName+")";
                    }
                    	
                    // TargetPage id
                    if(m_reportType==LANGUAGE_SIGN_OFF)
                    {
                    	p_sheet.addCell(new Label(col++, row, Name,
                                format));
                    }
                    else
                    {
                    	p_sheet.addCell(new Number(col++, row, targetPage.getId(),
                                format));
                    }
                    p_sheet.setColumnView(col - 1, 20);

                    // Source segment
                    WritableCellFormat sourceFormat = m_rtlSourceLocale ? getRtlContentFormat()
                            : format;
                    String srcContent = m_rtlSourceLocale ? EditUtil
                            .toRtlString(sourceSegmentString)
                            : sourceSegmentString;
                    
                    p_sheet.addCell(new Label(col++, row, srcContent,
                            sourceFormat));
                    p_sheet.setColumnView(col - 1, 40);

                    // Target segment
                    WritableCellFormat targetFormat = m_rtlTargetLocale ? getRtlContentFormat()
                            : format;
                    String content = m_rtlTargetLocale ? EditUtil
                            .toRtlString(targetSegmentString) : targetSegmentString;

                    p_sheet.addCell(new Label(col++, row, content,
                            targetFormat));
                    p_sheet.setColumnView(col - 1, 40);

                    p_sheet.addCell(new Label(col++, row, sid, format));
                    p_sheet.setColumnView(col - 1, 40);

                    if (m_reportType == TRANSLATIONS_EDIT_REPORT)
                    {
                        // Target segment with compact tags
                        pData.setAddables(targetTuv.getDataType());
                        TmxPseudo.tmx2Pseudo(targetTuv.getGxmlExcludeTopTags(),
                                pData);
                        pTagSourceString = pData.getPTagSourceString();
                        String sContent = pTagSourceString;
                        if (sContent.equalsIgnoreCase(targetSegmentString))
                        {
                            sContent = "";
                        }
                        else if (m_rtlTargetLocale)
                        {
                            sContent = EditUtil.toRtlString(sContent);
                        }
                        
                        p_sheet.addCell(new Label(col++, row, sContent, targetFormat));
                        p_sheet.setColumnView(col - 1, 40);

                        WritableCellFormat requiredTranslationFormat = m_rtlTargetLocale ? unlockedRightFormat()
                                : unlockedFormat();
                        // Required translation
                        p_sheet.addCell(new Label(col++, row, "",
                                requiredTranslationFormat));
                        p_sheet.setColumnView(col - 1, 40);

                        p_sheet.addCell(new Label(col++, row, lastComment,
                                format));
                        p_sheet.setColumnView(col - 1, 40);
                    }
                    else
                    {
                        // Comment (free hand text)
                        WritableCellFormat commentFormat = m_rtlTargetLocale ? unlockedRightFormat()
                                : unlockedFormat();
                        // Set color format for target segment changed
                        if (isLSOExt
                                && targetSegmentModifiedDate
                                        .before(issueCreatedDate))
                        {
                            commentFormat = m_rtlTargetLocale ? redRightFormat()
                                    : redFormat();
                        }

                        if (m_rtlTargetLocale)
                        {
                            lastComment = EditUtil.toRtlString(lastComment);
                        }
                        
                        p_sheet.addCell(new Label(col++, row, lastComment,
                                commentFormat));
                        p_sheet.setColumnView(col - 1, 40);
                    }

                    // Category failure
                    WritableCellFormat cfFormat = format;
                    if (m_reportType != TRANSLATIONS_EDIT_REPORT)
                        cfFormat = unlockedFormat();

                    // Set color format for target segment changed
                    if (isLSOExt
                            && targetSegmentModifiedDate
                                    .before(issueCreatedDate))
                    {
                        cfFormat = redFormat();
                    }
                    Label dropdown = null;
                    
//                    String currentCompanyId = CompanyThreadLocal.getInstance().getValue();
//                    failureCategories = IssueOptions.getAllCategories(bundle, currentCompanyId);
//                    List<String> failureCategories = getFailureCategoriesList();
//                    int index = failureCategories.indexOf(failure);
//                    if (index != -1)
//                    {
                        dropdown = new Label(col++, row, failure, cfFormat);
                        dropdown.setCellFeatures(getSelectFeatures());
//                    }
//                    else
//                    {
//                        dropdown = new Label(col++, row, failure, cfFormat);
//                        dropdown.setCellFeatures(getSelectFeatures());
//                    }
                    p_sheet.addCell(dropdown);
                    p_sheet.setColumnView(col - 1, 30);

                    // TM match
                    p_sheet.addCell(new Label(col++, row, matches, format));
                    p_sheet.setColumnView(col - 1, 30);

                    // Glossary source
                    p_sheet.addCell(new Label(col++, row, sourceTerms, format));
                    p_sheet.setColumnView(col - 1, 30);

                    // Glossary target
                    p_sheet.addCell(new Label(col++, row, targetTerms, format));
                    p_sheet.setColumnView(col - 1, 30);
                    row++;
                }
            }
        }
    }

    /**
     * Write segment information into each row of the sheet
     * 
     * @param p_sheet
     *            the sheet
     * @param p_jobId
     *            the job id
     * @param p_targetLang
     *            the displayed target language
     * @param p_srcPageId
     *            the source page id
     * @throws Exception
     */
    private void writeCommentsAnalysisSegmentInfo(WritableSheet p_sheet,
            String p_jobId, String p_targetLang, String p_srcPageId,
            String p_dateFormat) throws Exception
    {
        SimpleDateFormat dateFormat = new SimpleDateFormat(p_dateFormat);
        ResourceBundle bundle = PageHandler.getBundle(request.getSession());
        Job job = ServerProxy.getJobHandler().getJobById(
                Long.parseLong(p_jobId));
        Collection wfs = job.getWorkflows();
        Iterator it = wfs.iterator();
        Vector targetPages = new Vector();
        // SimpleDateFormat dateFormat = new SimpleDateFormat(p_dateFormat);

        TranslationMemoryProfile tmp = null;
        List excludItems = null;

        while (it.hasNext())
        {
            Workflow workflow = (Workflow) it.next();

            if (Workflow.PENDING.equals(workflow.getState())
                    || Workflow.CANCELLED.equals(workflow.getState())
                    || Workflow.EXPORT_FAILED.equals(workflow.getState())
                    || Workflow.IMPORT_FAILED.equals(workflow.getState()))
            {
                continue;
            }
            if (p_targetLang
                    .equals(workflow.getTargetLocale().getDisplayName()))
            {
                targetPages = workflow.getTargetPages();
                tmp = workflow.getJob().getL10nProfile()
                        .getTranslationMemoryProfile();
                if (tmp != null)
                {
                    excludItems = new ArrayList(tmp.getJobExcludeTuTypes());
                }

            }
        }

        if (targetPages.isEmpty())
        {
            // If no corresponding target page exists, set the cell blank
            writeCommentsAnalysisBlank(p_sheet);
        }
        else
        {
            int row = SEGMENT_START_ROW;
            long jobId = Long.parseLong(p_jobId);
            LeverageMatchLingManager leverageMatchLingManager = LingServerProxy
                    .getLeverageMatchLingManager();
            TermLeverageManager termLeverageManager = ServerProxy
                    .getTermLeverageManager();
            TuvManager tuvManager = ServerProxy.getTuvManager();

            ArrayList allSourceTuvs = new ArrayList();
            for (int i = 0; i < targetPages.size(); i++)
            {
                TargetPage targetPage = (TargetPage) targetPages.get(i);
                SourcePage sourcePage = targetPage.getSourcePage();
                List sourceTuvs = getPageTuvs(sourcePage);
                allSourceTuvs.addAll(sourceTuvs);
            }
            SegmentRepetitions segmentRepetitions = new SegmentRepetitions(
                    allSourceTuvs);

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

                // Leverage TM
                Map exactMatches = leverageMatchLingManager.getExactMatches(
                        sourcePage.getIdAsLong(), new Long(targetPage
                                .getLocaleId()));
                Map leverageMatcheMap = leverageMatchLingManager
                        .getFuzzyMatches(sourcePage.getIdAsLong(), new Long(
                                targetPage.getLocaleId()));

                // Leverage Term
                Locale sourcePageLocale = sourcePage.getGlobalSightLocale()
                        .getLocale();
                Locale targetPageLocale = targetPage.getGlobalSightLocale()
                        .getLocale();
                TermLeverageOptions termLeverageOptions = getTermLeverageOptions(
                        sourcePageLocale, targetPageLocale);

                TermLeverageResult termLeverageResult = null;
                if (termLeverageOptions != null)
                {
                    termLeverageResult = termLeverageManager.leverageTerms(
                            sourceTuvs, termLeverageOptions, sourcePage.getCompanyId());
                }

                // Find segment comments
                List issues = null;
                CommentManager commentManager = ServerProxy.getCommentManager();
                issues = commentManager.getIssues(Issue.TYPE_SEGMENT, String
                        .valueOf(targetPage.getId())
                        + "_");

                boolean rtlSourceLocale = EditUtil.isRTLLocale(sourcePageLocale.toString());
                boolean rtlTargetLocale = EditUtil.isRTLLocale(targetPageLocale.toString());
                
                String category = null;
                for (int j = 0; j < targetTuvs.size(); j++)
                {
                    int col = 0;
                    Tuv targetTuv = (Tuv) targetTuvs.get(j);
                    String targetLanguage = targetTuv.getGlobalSightLocale()
                            .getLanguage();
                    Tuv sourceTuv = (Tuv) sourceTuvs.get(j);

                    sourceSegmentString = sourceTuv.getGxmlElement()
                            .getTextValue();
                    targetSegmentString = targetTuv.getGxmlElement()
                            .getTextValue();
                    sid = sourceTuv.getSid();

                    category = sourceTuv.getTu().getTuType();
                    if (excludItems != null && excludItems.contains(category))
                    {
                        continue;
                    }

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
                                        + StringUtil.formatPCT(
                                             leverageMatch.getScoreNum())
                                        + "\r\n";
                            }
                            else
                            {
                                matches = matches
                                        + StringUtil.formatPCT(
                                             leverageMatch.getScoreNum());
                                break;
                            }
                        }
                    }
                    else
                    {
                        matches = bundle.getString("lb_no_match_report");
                    }

                    if (segmentRepetitions.getIdenticalTuvs(sourceTuv) != null)
                    {
                        matches = matches + "\r\n" + bundle.getString("jobinfo.tradosmatches.invoice.repetition");
                    }

                    MatchRecordList matchRecordList = null;
                    String sourceTerms = "";
                    String targetTerms = "";

                    if (termLeverageResult != null)
                    {
                        matchRecordList = termLeverageResult
                                .getMatchesForTuv(sourceTuv);
                    }

                    if (matchRecordList != null && matchRecordList.size() != 0)
                    {
                        String sourceTerm = null;
                        String targetTerm = null;
                        int maxScore = 0;
                        int score = 0;
                        int flag = 0;
                        if (matchRecordList.size() > 1)
                        {
                            for (int ni = 0; ni < matchRecordList.size(); ni++)
                            {
                                MatchRecord mr = (MatchRecord) matchRecordList
                                        .get(ni);
                                score = mr.getScore();
                                if (sourceSegmentString.indexOf(mr
                                        .getMatchedSourceTerm()) < 0)
                                {
                                    continue;
                                }
                                if (maxScore < score)
                                {
                                    maxScore = score;
                                    flag = ni;
                                }
                            }
                        }
                        MatchRecord matchRecord = (MatchRecord) matchRecordList
                                .get(flag);
                        sourceTerm = matchRecord.getMatchedSourceTerm();
                        if (!(sourceSegmentString.indexOf(sourceTerm) < 0))
                        {
                            List targets = matchRecord.getSourceTerm()
                                    .getTargetTerms();
                            {
                                for (int ti = 0; ti < targets.size(); ti++)
                                {
                                    TargetTerm tt = (TargetTerm) targets
                                            .get(ti);
                                    String targetTermLocale = tt.getLocale();
                                    // Get the target term by language
                                    if (targetLanguage.equals(targetTermLocale))
                                    {
                                        targetTerm = tt.getMatchedTargetTerm();
                                        sourceTerms = sourceTerm;
                                        targetTerms = targetTerm;
                                        break;
                                    }

                                }
                            }

                        }

                    }

                    List issueHistories = new ArrayList();
                    String failure = "";
                    if (issues != null)
                    {
                        for (int m = 0; m < issues.size(); m++)
                        {
                            Issue issue = (Issue) issues.get(m);
                            if (targetTuv.getId() == issue.getLevelObjectId())
                            {
                                issueHistories = issue.getHistory();
                                failure = issue.getCategory();
//                                if (Issue.CATEGORY_SPELLING
//                                        .equalsIgnoreCase(failure))
//                                {
//                                    failure = failure.replaceAll(",", "");
//                                }
                                break;
                            }
                        }
                    }

                    String comments = "";
                    for (int k = 0; k < issueHistories.size(); k++)
                    {
                        IssueHistory issueHistory = (IssueHistory) issueHistories
                                .get(k);
                        String date = dateFormat.format(issueHistory
                                .dateReportedAsDate());
                        comments = comments + "[" + date + "     "
                                + issueHistory.reportedBy() + "]:\r\n"
                                + issueHistory.getComment();
                        if (k != issueHistories.size() - 1)
                        {
                            comments = comments + "\r\n";
                        }
                    }

                    List previousTaskTuvs = new ArrayList();
                    previousTaskTuvs = tuvManager.getPreviousTaskTuvs(targetTuv
                            .getId(), 10);
                    String previousSegments = "";
                    String previousSegment = null;
                    List previous = new ArrayList();
                    if (previousTaskTuvs.size() > 0)
                    {
                        Collections.sort(previousTaskTuvs, new Comparator()
                        {
                            public int compare(Object p_taskTuvA,
                                    Object p_taskTuvB)
                            {
                                TaskTuv a = (TaskTuv) p_taskTuvA;
                                TaskTuv b = (TaskTuv) p_taskTuvB;
                                return a.getTask().getCompletedDate()
                                        .compareTo(
                                                b.getTask().getCompletedDate());
                            }
                        });

                        int len = previousTaskTuvs.size() - 2;
                        int lastReviewerCount = 0;

                        while ((len >= 0)
                                && (((TaskTuv) previousTaskTuvs.get(len))
                                        .getTask().getType() == Task.TYPE_REVIEW))
                        {
                            // Review only task does not change segments
                            lastReviewerCount++;
                            len--;
                        }

                        int beforeLastReviewerCount = previousTaskTuvs.size()
                                - lastReviewerCount - 1;

                        if (beforeLastReviewerCount == 1)
                        {
                            TaskTuv taskTuv = (TaskTuv) previousTaskTuvs.get(0);
                            String previousSeg = taskTuv.getTuv()
                                    .getGxmlElement().getTextValue();
                            if (!previousSeg.equals(targetSegmentString))
                            {
                                previousSegments = previousSegments
                                        + previousSeg;
                            }
                        }
                        else
                        {
                            for (int k = 0; k <= beforeLastReviewerCount; k++)
                            {
                                TaskTuv taskTuv = (TaskTuv) previousTaskTuvs
                                        .get(k);
                                previousSegment = taskTuv.getTuv()
                                        .getGxmlElement().getTextValue();

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
                                    previousSegments = previousSegments
                                            + previousSegment;
                                }
                            }
                        }
                    }

                    // Job id
                    p_sheet.addCell(new Number(col++, row, jobId,
                            getContentFormat()));
                    p_sheet.setColumnView(col - 1, 20);

                    // Segment id
                    p_sheet.addCell(new Number(col++, row, sourceTuv.getTu()
                            .getId(), getContentFormat()));
                    p_sheet.setColumnView(col - 1, 20);

                    // TargetPage id
                    p_sheet.addCell(new Number(col++, row, targetPage.getId(),
                            getContentFormat()));
                    p_sheet.setColumnView(col - 1, 20);


                    WritableCellFormat sourceFormat = rtlSourceLocale ? getRtlContentFormat()
                            : getContentFormat();
                    
                    String srcContent = rtlSourceLocale ? EditUtil
                            .toRtlString(sourceSegmentString) : sourceSegmentString;
                            
                    // Source segment
                    p_sheet.addCell(new Label(col++, row, srcContent,
                            sourceFormat));
                    p_sheet.setColumnView(col - 1, 40);

                    WritableCellFormat targetFormat = rtlTargetLocale ? getRtlContentFormat()
                            : getContentFormat();
                    String content = rtlTargetLocale ? EditUtil
                            .toRtlString(targetSegmentString) : targetSegmentString;
                            
                    // Target segment
                    p_sheet.addCell(new Label(col++, row, content,
                            targetFormat));
                    p_sheet.setColumnView(col - 1, 40);

                    // Sid
                    p_sheet.addCell(new Label(col++, row, sid,
                            getContentFormat()));
                    p_sheet.setColumnView(col - 1, 40);
                            
                    // Character count
                    p_sheet.addCell(new Label(col++, row, String
                            .valueOf(targetSegmentString.length()),
                            getContentFormat()));
                    p_sheet.setColumnView(col - 1, 30);

                    WritableCellFormat commentFormat = rtlTargetLocale ? getRtlContentFormat()
                            : getContentFormat();
                    String commentContent = rtlTargetLocale ? EditUtil
                            .toRtlString(comments) : comments;
                            
                    // Comments
                    p_sheet.addCell(new Label(col++, row, commentContent,
                            commentFormat));
                    p_sheet.setColumnView(col - 1, 50);

                    // Category failure
                    Label dropdown = null;
//                    List<String> failureCategories = getFailureCategoriesList();
//                    int index = failureCategories.indexOf(failure);
//                    if (index != -1)
//                    {
                        dropdown = new Label(col++, row, failure, getContentFormat());
                        dropdown.setCellFeatures(getSelectFeatures());
//                    }
//                    else
//                    {
//                        dropdown = new Label(col++, row, failure, getContentFormat());
//                        dropdown.setCellFeatures(getSelectFeatures());
//                    }
                    p_sheet.addCell(dropdown);
                    p_sheet.setColumnView(col - 1, 30);

                    // TM match
                    p_sheet.addCell(new Label(col++, row, matches,
                            getContentFormat()));
                    p_sheet.setColumnView(col - 1, 30);

                    // Glossary source
                    p_sheet.addCell(new Label(col++, row, sourceTerms,
                            getContentFormat()));
                    p_sheet.setColumnView(col - 1, 30);

                    // Glossary target
                    p_sheet.addCell(new Label(col++, row, targetTerms,
                            getContentFormat()));
                    p_sheet.setColumnView(col - 1, 30);

                    // Previous segment
                    p_sheet.addCell(new Label(col++, row, previousSegments,
                            getContentFormat()));
                    p_sheet.setColumnView(col - 1, 40);

                    row++;
                }
            }
        }
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
     * @throws Exception
     */
    private void writeCommentsAnalysisBlank(WritableSheet p_sheet)
            throws Exception
    {
        int col = 0;
        int row = 7;
        p_sheet.addCell(new Label(col++, row, ""));
        p_sheet.addCell(new Label(col++, row, ""));
        p_sheet.addCell(new Label(col++, row, ""));
        p_sheet.addCell(new Label(col++, row, ""));
        p_sheet.addCell(new Label(col++, row, ""));
        p_sheet.addCell(new Label(col++, row, ""));
        p_sheet.addCell(new Label(col++, row, ""));
        p_sheet.addCell(new Label(col++, row, ""));
        p_sheet.addCell(new Label(col++, row, ""));
        p_sheet.addCell(new Label(col++, row, ""));
        p_sheet.addCell(new Label(col++, row, ""));
        p_sheet.addCell(new Label(col++, row, ""));
    }

    private void writeLanguageSignOffBlank(WritableSheet p_sheet)
            throws Exception
    {
        int col = 0;
        int row = 7;
        p_sheet.addCell(new Label(col++, row, ""));
        p_sheet.addCell(new Label(col++, row, ""));
        p_sheet.addCell(new Label(col++, row, ""));
        p_sheet.addCell(new Label(col++, row, ""));
        p_sheet.addCell(new Label(col++, row, ""));
        p_sheet.addCell(new Label(col++, row, ""));
        p_sheet.addCell(new Label(col++, row, ""));
        p_sheet.addCell(new Label(col++, row, ""));
        p_sheet.addCell(new Label(col++, row, ""));
        p_sheet.addCell(new Label(col++, row, ""));
        if (m_reportType == TRANSLATIONS_EDIT_REPORT)
        {
            p_sheet.addCell(new Label(col++, row, ""));
        }
    }

    /**
     * Get tuvs of source page from database. Return a list
     * 
     * @param p_sourcePage
     *            source page
     * @throws Exception
     */
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
    private List getPageTuvs(TargetPage p_targetPage) throws Exception
    {
        return new ArrayList(ServerProxy.getTuvManager()
                .getTargetTuvsForStatistics(p_targetPage));
    }
}
