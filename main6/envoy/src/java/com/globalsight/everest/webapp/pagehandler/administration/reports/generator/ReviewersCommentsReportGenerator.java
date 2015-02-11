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
import java.util.ArrayList;
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

import jxl.CellView;
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

import org.apache.log4j.Logger;

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
import com.globalsight.everest.persistence.tuv.SegmentTuUtil;
import com.globalsight.everest.projecthandler.TranslationMemoryProfile;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.tuv.Tuv;
import com.globalsight.everest.util.comparator.GlobalSightLocaleComparator;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.administration.company.Select;
import com.globalsight.everest.webapp.pagehandler.administration.reports.ReportConstants;
import com.globalsight.everest.webapp.pagehandler.administration.reports.ReportHelper;
import com.globalsight.everest.webapp.pagehandler.administration.reports.bo.ReportsData;
import com.globalsight.everest.webapp.pagehandler.administration.users.UserUtil;
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
import com.globalsight.util.GeneralException;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.StringUtil;
import com.globalsight.util.edit.EditUtil;
import com.globalsight.util.resourcebundle.ResourceBundleConstants;
import com.globalsight.util.resourcebundle.SystemResourceBundle;

/**
 * Reviewers Comments Report Generator, Include Reviewers Comments Report in
 * offline download report.
 */
public class ReviewersCommentsReportGenerator implements ReportGenerator,
        Cancelable
{
    static private final Logger logger = Logger
            .getLogger(ReviewersCommentsReportGenerator.class);

    private WritableCellFormat contentFormat = null;
    private WritableCellFormat headerFormat = null;
    private WritableCellFormat rtlContentFormat = null;

    protected String m_companyName = "";
    protected List<Long> m_jobIDS = new ArrayList<Long>();
    protected List<GlobalSightLocale> m_targetLocales = new ArrayList<GlobalSightLocale>();

    public static final String reportType = ReportConstants.REVIEWERS_COMMENTS_REPORT;

    // Is Run setPercent/isCancelled function or not.
    private boolean m_isCalculatePercent;

    public static final int LANGUAGE_HEADER_ROW = 3;
    public static final int LANGUAGE_INFO_ROW = 4;
    public static final int SEGMENT_HEADER_ROW = 6;
    public static final int SEGMENT_START_ROW = 7;

    private WritableCellFormat unlockedFormat = null;
    private WritableCellFormat unlockedRightFormat = null;
    private Locale m_uiLocale = Locale.US;
    String m_userId;
    private ResourceBundle m_bundle;
    private String m_dateFormat;
    private boolean cancel = false;

    public ReviewersCommentsReportGenerator(String p_cureentCompanyName)
    {
        m_uiLocale = Locale.US;
        m_companyName = p_cureentCompanyName;
        CompanyThreadLocal.getInstance().setValue(m_companyName);
        m_bundle = SystemResourceBundle.getInstance().getResourceBundle(
                ResourceBundleConstants.LOCALE_RESOURCE_NAME, m_uiLocale);
        m_dateFormat = DEFAULT_DATE_FORMAT;

        m_isCalculatePercent = false;
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
        WorkbookSettings settings = new WorkbookSettings();
        settings.setSuppressWarnings(true);
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

            File file = ReportHelper.getXLSReportFile(getReportType(), job);
            WritableWorkbook workbook = Workbook.createWorkbook(file, settings);
            setWritableCellFormatNull();
            createReport(workbook, job, p_targetLocales, m_dateFormat);
            workbook.write();
            workbook.close();
            workBooks.add(file);
        }

        return workBooks.toArray(new File[workBooks.size()]);
    }

    /**
     * Create the report
     * 
     * @throws Exception
     */
    private void createReport(WritableWorkbook p_workbook, Job p_job,
            List<GlobalSightLocale> p_targetLocales, String p_dateFormat)
            throws Exception
    {
        List<GlobalSightLocale> jobTL = ReportHelper.getTargetLocals(p_job);
        for (GlobalSightLocale tgtL : p_targetLocales)
        {
            if (!jobTL.contains(tgtL))
                continue;

            if (cancel)
                return;

            // Create Report Template
            WritableSheet sheet = p_workbook.createSheet(tgtL.toString(),
                    p_workbook.getNumberOfSheets());
            sheet.getSettings().setProtected(true);

            // Add Title
            addTitle(sheet);
            // Add Locale Pair Header
            addLanguageHeader(sheet);
            // Add Segment Header
            addSegmentHeader(sheet, SEGMENT_HEADER_ROW);
            // Create Name Areas for drop down list.
            createNameAreas(p_workbook);

            // Insert Language Data
            writeLanguageInfo(sheet, p_job, tgtL.getDisplayName(m_uiLocale));
            // Insert Segment Data
            writeSegmentInfo(sheet, p_job, tgtL, SEGMENT_START_ROW);
        }
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
        // Title font is black bold on white
        WritableFont titleFont = new WritableFont(WritableFont.TIMES, 14,
                WritableFont.BOLD, false, UnderlineStyle.NO_UNDERLINE,
                jxl.format.Colour.BLACK);
        WritableCellFormat titleFormat = new WritableCellFormat(titleFont);
        titleFormat.setWrap(false);
        titleFormat.setShrinkToFit(false);
        p_sheet.addCell(new Label(0, 0, m_bundle
                .getString("review_reviewers_comments"), titleFormat));
        p_sheet.setColumnView(0, 40);
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
    private void addSegmentHeader(WritableSheet p_sheet, int p_row)
            throws Exception
    {
        int col = 0;
        p_sheet.addCell(new Label(col++, p_row, m_bundle
                .getString("lb_job_id_report"), getHeaderFormat()));
        p_sheet.setColumnView(col - 1, 20);
        p_sheet.addCell(new Label(col++, p_row, m_bundle
                .getString("lb_segment_id"), getHeaderFormat()));
        p_sheet.setColumnView(col - 1, 20);

        // Fix for GBS-1484
        p_sheet.addCell(new Label(col++, p_row, m_bundle
                .getString("lb_page_name"), getHeaderFormat()));
        p_sheet.setColumnView(col - 1, 20);

        p_sheet.addCell(new Label(col++, p_row, m_bundle
                .getString("lb_source_segment"), getHeaderFormat()));
        p_sheet.setColumnView(col - 1, 40);

        p_sheet.addCell(new Label(col++, p_row, m_bundle
                .getString("lb_target_segment"), getHeaderFormat()));
        p_sheet.addCell(new Label(col++, p_row, m_bundle.getString("lb_sid"),
                getHeaderFormat()));
        p_sheet.setColumnView(col - 1, 40);
        p_sheet.addCell(new Label(col++, p_row, m_bundle
                .getString("lb_source_segment_with_compact_tags"),
                getHeaderFormat()));
        p_sheet.setColumnView(col - 1, 40);
        p_sheet.addCell(new Label(col++, p_row, m_bundle
                .getString("lb_target_segment_with_compact_tags"),
                getHeaderFormat()));
        p_sheet.setColumnView(col - 1, 40);

        p_sheet.addCell(new Label(col++, p_row, m_bundle
                .getString("lb_comment_free"), getHeaderFormat()));
        p_sheet.setColumnView(col - 1, 40);

        p_sheet.addCell(new Label(col++, p_row, m_bundle
                .getString("lb_category_failure"), getHeaderFormat()));
        p_sheet.setColumnView(col - 1, 30);

        p_sheet.addCell(new Label(col++, p_row, m_bundle
                .getString("lb_comment_status"), getHeaderFormat()));
        p_sheet.setColumnView(col - 1, 30);

        p_sheet.addCell(new Label(col++, p_row, m_bundle
                .getString("lb_tm_match_original"), getHeaderFormat()));
        p_sheet.setColumnView(col - 1, 30);
        p_sheet.addCell(new Label(col++, p_row, m_bundle
                .getString("lb_glossary_source"), getHeaderFormat()));
        p_sheet.setColumnView(col - 1, 30);
        p_sheet.addCell(new Label(col++, p_row, m_bundle
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
        int col = 0;
        int row = LANGUAGE_HEADER_ROW;
        p_sheet.addCell(new Label(col++, row, m_bundle
                .getString("lb_source_language"), getHeaderFormat()));
        p_sheet.setColumnView(col - 1, 30);
        p_sheet.addCell(new Label(col++, row, m_bundle
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
                .getDisplayName(m_uiLocale), getContentFormat()));
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
     */
    private WritableCellFeatures getSelectFeatures(String p_nameedRange) throws Exception
    {
        WritableCellFeatures features = new WritableCellFeatures();
        features.setDataValidationRange(p_nameedRange);
        return features;
    }
    
    /**
     * Create Workbook Name Areas for Drop Down List,
     * Only write the data of drop down list into the first sheet.
     */
    private void createNameAreas(WritableWorkbook p_workbook)
    {
        try
        {
            if (p_workbook.getSheets().length == 1)
            {
                WritableSheet sheet = p_workbook.getSheet(0);
                List<String> categories = getFailureCategoriesList();
                int col = sheet.getColumns() + 1;
                for (int i = 0; i < categories.size(); i++)
                {
                    sheet.addCell(new Label(col, SEGMENT_START_ROW + i,
                            categories.get(i)));
                }
                // Hidden Category Column.
                CellView cellView = new CellView();
                cellView.setHidden(true);
                sheet.setColumnView(col, cellView);
                
                p_workbook.addNameArea(CATEGORY_LIST, sheet, 
                        col, SEGMENT_START_ROW, 
                        col, SEGMENT_START_ROW + categories.size());
            }
        }
        catch (Exception e)
        {
            logger.error("Reviewers Comments Report Write Category Error." + e);
        }
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

    /**
     * Get category status
     */
    private WritableCellFeatures getSelectFeaturesCommentStatus()
            throws Exception
    {
        List<String> status = new ArrayList<String>();
        status.addAll(IssueOptions.getAllStatus());
        WritableCellFeatures features = new WritableCellFeatures();
        features.setDataValidationList(status);
        return features;
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
    private int writeSegmentInfo(WritableSheet p_sheet, Job p_job,
            GlobalSightLocale p_targetLocale, int p_row) throws Exception
    {
        Vector targetPages = new Vector();
        TranslationMemoryProfile tmp = p_job.getL10nProfile()
                .getTranslationMemoryProfile();
        List<String> excludItems = null;
        if (tmp != null)
        {
            excludItems = new ArrayList<String>(tmp.getJobExcludeTuTypes());
        }

        long companyId = p_job.getCompanyId();

        for (Iterator it = p_job.getWorkflows().iterator(); it.hasNext();)
        {
            Workflow workflow = (Workflow) it.next();

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
            LeverageMatchLingManager leverageMatchLingManager = LingServerProxy
                    .getLeverageMatchLingManager();
            TermLeverageManager termLeverageManager = ServerProxy
                    .getTermLeverageManager();

            CommentManager commentManager = ServerProxy.getCommentManager();

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
                        .getTermMatchesForPages(
                                new HashSet<SourcePage>(p_job.getSourcePages()),
                                p_targetLocale);
            }

            String category = null;
            PseudoData pData = new PseudoData();
            pData.setMode(PseudoConstants.PSEUDO_COMPACT);
            String sourceSegmentString = null;
            String targetSegmentString = null;
            String sid = null;
            for (int i = 0; i < targetPages.size(); i++)
            {
                if (cancel)
                    return 0;

                TargetPage targetPage = (TargetPage) targetPages.get(i);
                SourcePage sourcePage = targetPage.getSourcePage();
                SegmentTuUtil.getTusBySourcePageId(sourcePage.getId());
                List sourceTuvs = getPageTuvs(sourcePage);
                List targetTuvs = getPageTuvs(targetPage);
                Map exactMatches = leverageMatchLingManager.getExactMatches(
                        sourcePage.getIdAsLong(),
                        new Long(targetPage.getLocaleId()));
                Map leverageMatcheMap = leverageMatchLingManager
                        .getFuzzyMatches(sourcePage.getIdAsLong(), new Long(
                                targetPage.getLocaleId()));

                boolean m_rtlSourceLocale = EditUtil
                        .isRTLLocale(sourcePageLocale.toString());
                boolean m_rtlTargetLocale = EditUtil
                        .isRTLLocale(targetPageLocale.toString());

                // Find segment all comments belong to this target page
                List<IssueImpl> issues = commentManager.getIssues(
                        Issue.TYPE_SEGMENT, targetPage.getId());

                for (int j = 0; j < targetTuvs.size(); j++)
                {
                    if (cancel)
                        return 0;

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
                    String commentStatus = "";
                    if (issues != null)
                    {
                        for (int m = 0; m < issues.size(); m++)
                        {
                            if (cancel)
                                return 0;

                            Issue issue = (Issue) issues.get(m);
                            String logicKey = CommentHelper.makeLogicalKey(
                                    targetPage.getId(),
                                    targetTuv.getTu(companyId).getId(),
                                    targetTuv.getId(), 0);

                            if (logicKey.equals(issue.getLogicalKey()))
                            {
                                issueHistories = issue.getHistory();
                                failure = issue.getCategory();
                                commentStatus = issue.getStatus();
                                break;
                            }
                        }
                    }
                    if (issueHistories != null && issueHistories.size() > 0)
                    {
                        IssueHistory issueHistory = (IssueHistory) issueHistories
                                .get(0);
                        lastComment = issueHistory.getComment();
                    }

                    // Source segment and Target segment
                    sourceSegmentString = sourceTuv.getGxmlElement()
                            .getTextValue();
                    sid = sourceTuv.getSid();
                    targetSegmentString = targetTuv.getGxmlElement()
                            .getTextValue();

                    // TM Match
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
                            if (cancel)
                                return 0;

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
                        matches = m_bundle.getString("lb_no_match_report");
                    }
                    if (targetTuv.isRepeated())
                    {
                        matches += "\r\n"
                                + m_bundle
                                        .getString("jobinfo.tradosmatches.invoice.repeated");
                    }
                    else if (targetTuv.getRepetitionOfId() > 0)
                    {
                        matches += "\r\n"
                                + m_bundle
                                        .getString("jobinfo.tradosmatches.invoice.repetition");
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

                    WritableCellFormat format = getContentFormat();

                    // Job id
                    p_sheet.addCell(new Number(col++, p_row, p_job.getId(),
                            format));
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

                    // Source segment with compact tags
                    pData.setAddables(sourceTuv.getDataType(companyId));
                    TmxPseudo.tmx2Pseudo(sourceTuv.getGxmlExcludeTopTags(),
                            pData);
                    String sContent = pData.getPTagSourceString();
                    if (sContent.equalsIgnoreCase(sourceSegmentString))
                    {
                        sContent = "";
                    }
                    else if (m_rtlTargetLocale)
                    {
                        sContent = EditUtil.toRtlString(sContent);
                    }

                    p_sheet.addCell(new Label(col++, p_row, sContent,
                            targetFormat));
                    p_sheet.setColumnView(col - 1, 40);

                    // Target segment with compact tags
                    pData.setAddables(targetTuv.getDataType(companyId));
                    TmxPseudo.tmx2Pseudo(targetTuv.getGxmlExcludeTopTags(),
                            pData);
                    sContent = pData.getPTagSourceString();
                    if (sContent.equalsIgnoreCase(targetSegmentString))
                    {
                        sContent = "";
                    }
                    else if (m_rtlTargetLocale)
                    {
                        sContent = EditUtil.toRtlString(sContent);
                    }

                    p_sheet.addCell(new Label(col++, p_row, sContent,
                            targetFormat));
                    p_sheet.setColumnView(col - 1, 40);

                    WritableCellFormat commentFormat = m_rtlTargetLocale ? unlockedRightFormat()
                            : unlockedFormat();
                    if (m_rtlTargetLocale)
                    {
                        lastComment = EditUtil.toRtlString(lastComment);
                    }

                    p_sheet.addCell(new Label(col++, p_row, lastComment,
                            commentFormat));
                    p_sheet.setColumnView(col - 1, 40);

                    // Category failure
                    WritableCellFormat cfFormat = unlockedFormat();

                    Label dropdown = null;
                    dropdown = new Label(col++, p_row, failure, cfFormat);
                    dropdown.setCellFeatures(getSelectFeatures(CATEGORY_LIST));
                    p_sheet.addCell(dropdown);
                    p_sheet.setColumnView(col - 1, 30);

                    dropdown = new Label(col++, p_row, commentStatus, cfFormat);
                    dropdown.setCellFeatures(getSelectFeaturesCommentStatus());
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

    private void setWritableCellFormatNull()
    {
        contentFormat = null;
        headerFormat = null;
        rtlContentFormat = null;
        unlockedFormat = null;
        unlockedRightFormat = null;
    }

    @Override
    public String getReportType()
    {
        return reportType;
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
}
