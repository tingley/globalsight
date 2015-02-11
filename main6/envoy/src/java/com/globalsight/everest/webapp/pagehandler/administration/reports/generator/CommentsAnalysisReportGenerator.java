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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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

import com.globalsight.everest.comment.CommentManager;
import com.globalsight.everest.comment.Issue;
import com.globalsight.everest.comment.IssueHistory;
import com.globalsight.everest.comment.IssueOptions;
import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.integration.ling.LingServerProxy;
import com.globalsight.everest.integration.ling.tm2.LeverageMatch;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.page.SourcePage;
import com.globalsight.everest.page.TargetPage;
import com.globalsight.everest.persistence.tuv.SegmentTuUtil;
import com.globalsight.everest.projecthandler.TranslationMemoryProfile;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.taskmanager.Task;
import com.globalsight.everest.tuv.TaskTuv;
import com.globalsight.everest.tuv.Tuv;
import com.globalsight.everest.tuv.TuvManager;
import com.globalsight.everest.util.comparator.GlobalSightLocaleComparator;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.pagehandler.administration.company.Select;
import com.globalsight.everest.webapp.pagehandler.administration.reports.ReportConstants;
import com.globalsight.everest.webapp.pagehandler.administration.reports.ReportHelper;
import com.globalsight.everest.webapp.pagehandler.administration.reports.bo.ReportsData;
import com.globalsight.everest.webapp.pagehandler.administration.users.UserUtil;
import com.globalsight.everest.workflowmanager.Workflow;
import com.globalsight.ling.tm.LeverageMatchLingManager;
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
 * Comments Analysis Report Generator Include Comments Analysis Report in popup
 * editor
 */
public class CommentsAnalysisReportGenerator implements ReportGenerator
{
    private HttpServletRequest request = null;

    private WritableCellFormat contentFormat = null;
    private WritableCellFormat headerFormat = null;
    private WritableCellFormat rtlContentFormat = null;

    private static final String DEFAULT_DATE_FORMAT = "MM/dd/yy hh:mm:ss a z";

    protected String m_companyName = "";
    protected List<Long> m_jobIDS = new ArrayList<Long>();
    protected List<GlobalSightLocale> m_targetLocales = new ArrayList<GlobalSightLocale>();

    public static final String reportType = ReportConstants.COMMENTS_ANALYSIS_REPORT;

    public static final int LANGUAGE_HEADER_ROW = 3;
    public static final int LANGUAGE_INFO_ROW = 4;
    public static final int SEGMENT_HEADER_ROW = 6;
    public static final int SEGMENT_START_ROW = 7;

    private Locale uiLocale = new Locale("en", "US");
    String m_userId;

    /**
     * Constructor. Create a helper to generate the report
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
        uiLocale = (Locale) request.getSession().getAttribute(
                WebAppConstants.UILOCALE);
        if (uiLocale == null)
        {
            uiLocale = Locale.US;
        }

        if (p_request.getParameter(ReportConstants.JOB_IDS) != null)
        {
            m_jobIDS = ReportHelper.getListOfLong(p_request
                    .getParameter(ReportConstants.JOB_IDS));
            GlobalSightLocaleComparator comparator = new GlobalSightLocaleComparator(
                    GlobalSightLocaleComparator.ISO_CODE, uiLocale);
            m_targetLocales = ReportHelper.getTargetLocaleList(p_request
                    .getParameterValues(ReportConstants.TARGETLOCALE_LIST),
                    comparator);
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
        WorkbookSettings settings = new WorkbookSettings();
        settings.setSuppressWarnings(true);
        String dateFormat = request.getParameter(WebAppConstants.DATE_FORMAT);
        if (dateFormat == null)
        {
            dateFormat = DEFAULT_DATE_FORMAT;
        }
        int finishedJobNum = 0;
        for (long jobID : p_jobIDS)
        {
            // Cancel generate reports.
            if (isCancelled())
                return null;

            Job job = ServerProxy.getJobHandler().getJobById(jobID);
            if (job == null)
                continue;

            File file = ReportHelper.getXLSReportFile(reportType, job);
            WritableWorkbook workbook = Workbook.createWorkbook(file, settings);
            setWritableCellFormatNull();
            createReport(workbook, job, p_targetLocales, dateFormat);
            workbook.write();
            workbook.close();
            workBooks.add(file);

            // Sets Reports Percent.
            setPercent(++finishedJobNum);
        }

        return workBooks.toArray(new File[workBooks.size()]);
    }

    /**
     * Create the report sheets
     * 
     * @throws Exception
     */
    private void createReport(WritableWorkbook p_workbook, Job p_job,
            List<GlobalSightLocale> p_targetLangIDS, String p_dateFormat)
            throws Exception
    {
        List<GlobalSightLocale> jobTL = ReportHelper.getTargetLocals(p_job);
        for (GlobalSightLocale tgtL : p_targetLangIDS)
        {
            if (!jobTL.contains(tgtL))
                continue;
            // Create One Report Sheet/Tab
            WritableSheet sheet = p_workbook.createSheet(tgtL.toString(),
                    p_workbook.getNumberOfSheets());
            // Add Title
            addTitle(sheet);
            // Add Locale Pair Header
            addLanguageHeader(sheet);
            // Add Segment Header
            addSegmentHeader(sheet, SEGMENT_HEADER_ROW);

            // Insert Language Data
            writeLanguageInfo(sheet, p_job, tgtL.getDisplayName(uiLocale));
            // Insert Segment Data
            writeSegmentInfo(sheet, p_job, tgtL, p_dateFormat,
                    SEGMENT_START_ROW);
        }
    }

    /**
     * Add segment header to the sheet for Comments Analysis Report
     * 
     * @param p_sheet
     *            the sheet
     * @param p_row
     *            start row number
     * @throws Exception
     */
    private void addSegmentHeader(WritableSheet p_sheet, int p_row)
            throws Exception
    {
        ResourceBundle bundle = PageHandler.getBundle(request.getSession());
        int col = 0;
        p_sheet.addCell(new Label(col++, p_row, bundle
                .getString("lb_job_id_report"), getHeaderFormat()));
        p_sheet.setColumnView(col - 1, 20);
        p_sheet.addCell(new Label(col++, p_row, bundle
                .getString("lb_segment_id"), getHeaderFormat()));
        p_sheet.setColumnView(col - 1, 20);

        p_sheet.addCell(new Label(col++, p_row, bundle
                .getString("lb_targetpage_id"), getHeaderFormat()));
        p_sheet.setColumnView(col - 1, 20);

        p_sheet.addCell(new Label(col++, p_row, bundle
                .getString("lb_source_segment"), getHeaderFormat()));
        p_sheet.setColumnView(col - 1, 40);

        p_sheet.addCell(new Label(col++, p_row, bundle
                .getString("lb_target_segment"), getHeaderFormat()));
        p_sheet.addCell(new Label(col++, p_row, bundle.getString("lb_sid"),
                getHeaderFormat()));
        p_sheet.setColumnView(col - 1, 40);

        p_sheet.addCell(new Label(col++, p_row, bundle
                .getString("lb_character_count"), getHeaderFormat()));
        p_sheet.setColumnView(col - 1, 30);
        p_sheet.addCell(new Label(col++, p_row,
                bundle.getString("lb_comments"), getHeaderFormat()));
        p_sheet.setColumnView(col - 1, 40);

        p_sheet.addCell(new Label(col++, p_row, bundle
                .getString("lb_category_failure"), getHeaderFormat()));
        p_sheet.setColumnView(col - 1, 30);
        p_sheet.addCell(new Label(col++, p_row, bundle
                .getString("lb_tm_match_original"), getHeaderFormat()));
        p_sheet.setColumnView(col - 1, 30);
        p_sheet.addCell(new Label(col++, p_row, bundle
                .getString("lb_glossary_source"), getHeaderFormat()));
        p_sheet.setColumnView(col - 1, 30);
        p_sheet.addCell(new Label(col++, p_row, bundle
                .getString("lb_glossary_target"), getHeaderFormat()));
        p_sheet.setColumnView(col - 1, 30);

        p_sheet.addCell(new Label(col++, p_row, bundle
                .getString("lb_previous_segment_report"), getHeaderFormat()));
        p_sheet.setColumnView(col - 1, 40);
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
        p_sheet.addCell(new Label(0, 0, bundle.getString("review_comments"),
                titleFormat));
        p_sheet.setColumnView(0, 40);
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
    private void writeLanguageInfo(WritableSheet p_sheet, Job p_job,
            String p_targetLang) throws Exception
    {
        int col = 0;
        int row = LANGUAGE_INFO_ROW;

        // Source Language
        p_sheet.addCell(new Label(col++, row, p_job.getSourceLocale()
                .getDisplayName(uiLocale), getContentFormat()));
        p_sheet.setColumnView(col - 1, 30);

        // Target Language
        p_sheet.addCell(new Label(col++, row, p_targetLang, getContentFormat()));
        p_sheet.setColumnView(col - 1, 30);

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
     * For Comments Analysis Report, Write segment information into each row of
     * the sheet.
     * 
     * @param p_sheet
     *            the sheet
     * @param p_job
     *            the job data for report
     * @param p_targetLocale
     *            the target locale
     * @param p_row
     *            the segment row in sheet
     * @throws Exception
     */
    private int writeSegmentInfo(WritableSheet p_sheet, Job p_job,
            GlobalSightLocale p_targetLocale, String p_dateFormat, int p_row)
            throws Exception
    {
        SimpleDateFormat dateFormat = new SimpleDateFormat(p_dateFormat);
        ResourceBundle bundle = PageHandler.getBundle(request.getSession());
        Vector<TargetPage> targetPages = new Vector<TargetPage>();

        String companyId = p_job.getCompanyId();

        TranslationMemoryProfile tmp = p_job.getL10nProfile()
                .getTranslationMemoryProfile();
        List<String> excludItems = null;
        if (tmp != null)
        {
            excludItems = new ArrayList<String>(tmp.getJobExcludeTuTypes());
        }

        for (Iterator<Workflow> it = p_job.getWorkflows().iterator(); it.hasNext();)
        {
            Workflow workflow = (Workflow) it.next();
            if (Workflow.PENDING.equals(workflow.getState())
                    || Workflow.CANCELLED.equals(workflow.getState())
                    // || Workflow.EXPORT_FAILED.equals(workflow.getState())
                    || Workflow.IMPORT_FAILED.equals(workflow.getState()))
            {
                continue;
            }
            if (p_targetLocale.getId() == workflow.getTargetLocale()
                    .getId())
            {
                targetPages = workflow.getTargetPages();
                break;
            }
        }

        if (!targetPages.isEmpty())
        {
            LeverageMatchLingManager leverageMatchLingManager = LingServerProxy
                    .getLeverageMatchLingManager();
            TermLeverageManager termLeverageManager = ServerProxy
                    .getTermLeverageManager();
            TuvManager tuvManager = ServerProxy.getTuvManager();
            Map<Long, Set<TermLeverageMatch>> termLeverageMatchResultMap = termLeverageManager
                    .getTermMatchesForPages(
                            new HashSet<SourcePage>(p_job.getSourcePages()),
                            p_targetLocale);

            String sourceSegmentString = null;
            String targetSegmentString = null;
            String sid = null;
            for (int i = 0; i < targetPages.size(); i++)
            {
                TargetPage targetPage = (TargetPage) targetPages.get(i);
                SourcePage sourcePage = targetPage.getSourcePage();
                SegmentTuUtil.getTusBySourcePageId(sourcePage.getId());
                List sourceTuvs = getPageTuvs(sourcePage);
                List targetTuvs = getPageTuvs(targetPage);

                // Leverage TM
                Map exactMatches = leverageMatchLingManager
                        .getExactMatches(sourcePage.getIdAsLong(),
                                new Long(targetPage.getLocaleId()));
                Map leverageMatcheMap = leverageMatchLingManager
                        .getFuzzyMatches(sourcePage.getIdAsLong(),
                                new Long(targetPage.getLocaleId()));

                // Find segment comments
                List issues = null;
                CommentManager commentManager = ServerProxy
                        .getCommentManager();
                issues = commentManager.getIssues(Issue.TYPE_SEGMENT,
                        String.valueOf(targetPage.getId()) + "\\_");

                Locale sourcePageLocale = sourcePage.getGlobalSightLocale()
                        .getLocale();
                Locale targetPageLocale = targetPage.getGlobalSightLocale()
                        .getLocale();
                boolean rtlSourceLocale = EditUtil
                        .isRTLLocale(sourcePageLocale.toString());
                boolean rtlTargetLocale = EditUtil
                        .isRTLLocale(targetPageLocale.toString());

                String category = null;
                for (int j = 0; j < targetTuvs.size(); j++)
                {
                    int col = 0;
                    Tuv targetTuv = (Tuv) targetTuvs.get(j);
                    Tuv sourceTuv = (Tuv) sourceTuvs.get(j);
                    sourceSegmentString = sourceTuv.getGxmlElement()
                            .getTextValue();
                    targetSegmentString = targetTuv.getGxmlElement()
                            .getTextValue();
                    sid = sourceTuv.getSid();

                    category = sourceTuv.getTu(companyId).getTuType();
                    if (excludItems != null
                            && excludItems.contains(category))
                    {
                        continue;
                    }

                    String matches = "";
                    Set leverageMatches = (Set) leverageMatcheMap
                            .get(sourceTuv.getIdAsLong());

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
                                        + StringUtil
                                                .formatPCT(leverageMatch
                                                        .getScoreNum())
                                        + "\r\n";
                            }
                            else
                            {
                                matches = matches
                                        + StringUtil
                                                .formatPCT(leverageMatch
                                                        .getScoreNum());
                                break;
                            }
                        }
                    }
                    else
                    {
                        matches = bundle.getString("lb_no_match_report");
                    }
                    if (sourceTuv.getTu(companyId).isRepeated())
                    {
                        matches += "\r\n"
                                + bundle.getString("jobinfo.tradosmatches.invoice.repeated");
                    }
                    else if (sourceTuv.getTu(companyId).getRepetitionOfId() != 0)
                    {
                        matches += "\r\n"
                                + bundle.getString("jobinfo.tradosmatches.invoice.repetition");
                    }

                    List issueHistories = new ArrayList();
                    String failure = "";
                    if (issues != null)
                    {
                        for (int m = 0; m < issues.size(); m++)
                        {
                            Issue issue = (Issue) issues.get(m);
                            if (targetTuv.getId() == issue
                                    .getLevelObjectId())
                            {
                                issueHistories = issue.getHistory();
                                failure = issue.getCategory();
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
                            comments = comments
                                    + "["
                                    + date
                                    + "     "
                                    + UserUtil.getUserNameById(issueHistory
                                            .reportedBy()) + "]:\r\n"
                                    + issueHistory.getComment();
                            if (k != issueHistories.size() - 1)
                        {
                            comments = comments + "\r\n";
                        }
                    }

                    List previousTaskTuvs = new ArrayList();
                    previousTaskTuvs = tuvManager.getPreviousTaskTuvs(
                            targetTuv.getId(), 10);
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
                                return a.getTask()
                                        .getCompletedDate()
                                        .compareTo(
                                                b.getTask()
                                                        .getCompletedDate());
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

                        int beforeLastReviewerCount = previousTaskTuvs
                                .size() - lastReviewerCount - 1;

                        if (beforeLastReviewerCount == 1)
                        {
                            TaskTuv taskTuv = (TaskTuv) previousTaskTuvs
                                    .get(0);
                            String previousSeg = taskTuv
                                    .getTuv(p_job.getCompanyId())
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
                                previousSegment = taskTuv
                                        .getTuv(p_job.getCompanyId())
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
                                    previousSegments = previousSegments
                                            + "{" + previousSegment
                                            + "}\r\n";
                                }
                                else
                                {
                                    previousSegments = previousSegments
                                            + previousSegment;
                                }
                            }
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
                            TermLeverageMatch tlm = termLeverageMatchSet
                                    .iterator().next();
                            sourceTerms = tlm.getMatchedSourceTerm();
                            targetTerms = tlm.getMatchedTargetTerm();
                        }
                    }

                    // Job id
                    p_sheet.addCell(new Number(col++, p_row, p_job.getId(),
                            getContentFormat()));
                    p_sheet.setColumnView(col - 1, 20);

                    // Segment id
                    p_sheet.addCell(new Number(col++, p_row, sourceTuv
                            .getTu(companyId).getId(), getContentFormat()));
                    p_sheet.setColumnView(col - 1, 20);

                    // TargetPage id
                    p_sheet.addCell(new Number(col++, p_row, targetPage
                            .getId(), getContentFormat()));
                    p_sheet.setColumnView(col - 1, 20);

                    // Source segment
                    WritableCellFormat sourceFormat = rtlSourceLocale ? getRtlContentFormat()
                            : getContentFormat();
                    String srcContent = rtlSourceLocale ? EditUtil
                            .toRtlString(sourceSegmentString)
                            : sourceSegmentString;
                    p_sheet.addCell(new Label(col++, p_row, srcContent,
                            sourceFormat));
                    p_sheet.setColumnView(col - 1, 40);

                    // Target segment
                    WritableCellFormat targetFormat = rtlTargetLocale ? getRtlContentFormat()
                            : getContentFormat();
                    String content = rtlTargetLocale ? EditUtil
                            .toRtlString(targetSegmentString)
                            : targetSegmentString;
                    p_sheet.addCell(new Label(col++, p_row, content,
                            targetFormat));
                    p_sheet.setColumnView(col - 1, 40);

                    // Sid
                    p_sheet.addCell(new Label(col++, p_row, sid,
                            getContentFormat()));
                    p_sheet.setColumnView(col - 1, 40);

                    // Character count
                    p_sheet.addCell(new Label(col++, p_row, String
                            .valueOf(targetSegmentString.length()),
                            getContentFormat()));
                    p_sheet.setColumnView(col - 1, 30);

                    WritableCellFormat commentFormat = rtlTargetLocale ? getRtlContentFormat()
                            : getContentFormat();
                    String commentContent = rtlTargetLocale ? EditUtil
                            .toRtlString(comments) : comments;

                    // Comments
                    p_sheet.addCell(new Label(col++, p_row, commentContent,
                            commentFormat));
                    p_sheet.setColumnView(col - 1, 50);

                    // Category failure
                    Label dropdown = null;
                    // List<String> failureCategories =
                    // getFailureCategoriesList();
                    // int index = failureCategories.indexOf(failure);
                    // if (index != -1)
                    // {
                    dropdown = new Label(col++, p_row, failure,
                            getContentFormat());
                    dropdown.setCellFeatures(getSelectFeatures());
                    // }
                    // else
                    // {
                    // dropdown = new Label(col++, p_row, failure,
                    // getContentFormat());
                    // dropdown.setCellFeatures(getSelectFeatures());
                    // }
                    p_sheet.addCell(dropdown);
                    p_sheet.setColumnView(col - 1, 30);

                    // TM match
                    p_sheet.addCell(new Label(col++, p_row, matches,
                            getContentFormat()));
                    p_sheet.setColumnView(col - 1, 30);

                    // Glossary source
                    p_sheet.addCell(new Label(col++, p_row, sourceTerms,
                            getContentFormat()));
                    p_sheet.setColumnView(col - 1, 30);

                    // Glossary target
                    p_sheet.addCell(new Label(col++, p_row, targetTerms,
                            getContentFormat()));
                    p_sheet.setColumnView(col - 1, 30);

                    // Previous segment
                    p_sheet.addCell(new Label(col++, p_row,
                            previousSegments, getContentFormat()));
                    p_sheet.setColumnView(col - 1, 40);

                    p_row++;
                }
            }
        }

        return p_row;
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
            String cat = aCategory.getValue();
            result.add(cat.replace(",", " "));
        }
        return result;
    }

    private void setWritableCellFormatNull()
    {
        contentFormat = null;
        headerFormat = null;
        rtlContentFormat = null;
    }

    @Override
    public String getReportType()
    {
        return reportType;
    }

    @Override
    public void setPercent(int p_finishedJobNum)
    {
        ReportGeneratorHandler.setReportsMapByGenerator(m_userId, m_jobIDS, 100
                * p_finishedJobNum / m_jobIDS.size());
    }

    @Override
    public boolean isCancelled()
    {
        ReportsData data = ReportGeneratorHandler.getReportsMap(m_userId,
                m_jobIDS);
        if (data != null)
            return data.isCancle();

        return false;
    }
}
