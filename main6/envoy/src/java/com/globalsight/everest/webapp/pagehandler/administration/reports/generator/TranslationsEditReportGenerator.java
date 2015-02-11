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
import java.util.Collection;
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

import org.apache.log4j.Logger;

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
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.StringUtil;
import com.globalsight.util.edit.EditUtil;
import com.globalsight.util.resourcebundle.ResourceBundleConstants;
import com.globalsight.util.resourcebundle.SystemResourceBundle;

/**
 * This Generator is used for creating Translations Edit Report (Include
 * Translations Edit Report in offline download report page)
 */
public class TranslationsEditReportGenerator implements ReportGenerator,
        Cancelable
{
    private static final Logger logger = 
            Logger.getLogger(TranslationsEditReportGenerator.class);
    
    private WritableCellFormat contentFormat = null;
    private WritableCellFormat rtlContentFormat = null;
    private WritableCellFormat headerFormat = null;
    private WritableCellFormat unlockedFormat = null;
    private WritableCellFormat unlockedRightFormat = null;

    public static final int LANGUAGE_HEADER_ROW = 3;
    public static final int LANGUAGE_INFO_ROW = 4;
    public static final int SEGMENT_HEADER_ROW = 6;
    public static final int SEGMENT_START_ROW = 7;

    private Locale m_uiLocale;
    private String m_companyName = "";
    protected List<Long> m_jobIDS = new ArrayList<Long>();
    protected List<GlobalSightLocale> m_targetLocales = new ArrayList<GlobalSightLocale>();
    private String m_dateFormat;
    private ResourceBundle m_bundle;

    private boolean cancel = false;

    public TranslationsEditReportGenerator(String p_currentCompanyName)
    {
        m_companyName = p_currentCompanyName;
        CompanyThreadLocal.getInstance().setValue(m_companyName);
        m_uiLocale = Locale.US;
        m_bundle = SystemResourceBundle.getInstance().getResourceBundle(
                ResourceBundleConstants.LOCALE_RESOURCE_NAME, m_uiLocale);
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
        m_uiLocale = (Locale) p_request.getSession().getAttribute(
                WebAppConstants.UILOCALE);
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
            m_companyName = CompanyWrapper.getCompanyNameById(job.getCompanyId());
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
            File file = ReportHelper.getXLSReportFile(getReportType(), job);
            WorkbookSettings settings = new WorkbookSettings();
            settings.setSuppressWarnings(true);
            WritableWorkbook workbook = Workbook.createWorkbook(file, settings);
            createReport(workbook, job, p_targetLocales, m_dateFormat);
            workbook.write();
            workbook.close();
            workBooks.add(file);
        }
        return workBooks.toArray(new File[workBooks.size()]);
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
        int col = 0;
        int row = SEGMENT_HEADER_ROW;
        p_sheet.addCell(new Label(col++, row, m_bundle
                .getString("lb_job_id_report"), getHeaderFormat()));
        p_sheet.setColumnView(col - 1, 20);
        p_sheet.addCell(new Label(col++, row, m_bundle
                .getString("lb_segment_id"), getHeaderFormat()));
        p_sheet.setColumnView(col - 1, 20);

        p_sheet.addCell(new Label(col++, row, m_bundle
                .getString("lb_targetpage_id"), getHeaderFormat()));
        p_sheet.setColumnView(col - 1, 20);

        p_sheet.addCell(new Label(col++, row, m_bundle
                .getString("lb_source_segment"), getHeaderFormat()));
        p_sheet.setColumnView(col - 1, 40);
        p_sheet.addCell(new Label(col++, row, m_bundle
                .getString("lb_target_segment"), getHeaderFormat()));

        p_sheet.addCell(new Label(col++, row, m_bundle.getString("lb_sid"),
                getHeaderFormat()));
        p_sheet.setColumnView(col - 1, 40);

        p_sheet.addCell(new Label(col++, row, m_bundle
                .getString("lb_source_segment_with_compact_tags"),
                getHeaderFormat()));
        p_sheet.setColumnView(col - 1, 40);
        p_sheet.addCell(new Label(col++, row, m_bundle
                .getString("lb_target_segment_with_compact_tags"),
                getHeaderFormat()));
        p_sheet.setColumnView(col - 1, 40);

        p_sheet.addCell(new Label(col++, row, m_bundle
                .getString("lb_required_translation"), getHeaderFormat()));
        p_sheet.setColumnView(col - 1, 40);

        p_sheet.addCell(new Label(col++, row,
                m_bundle.getString("lb_comments"), getHeaderFormat()));

        p_sheet.addCell(new Label(col++, row, m_bundle
                .getString("lb_required_comment"), getHeaderFormat()));

        p_sheet.addCell(new Label(col++, row, m_bundle
                .getString("lb_category_failure"), getHeaderFormat()));

        p_sheet.addCell(new Label(col++, row, m_bundle
                .getString("lb_comment_status"), getHeaderFormat()));
        p_sheet.setColumnView(col - 1, 30);
        p_sheet.addCell(new Label(col++, row, m_bundle
                .getString("lb_tm_match_original"), getHeaderFormat()));
        p_sheet.setColumnView(col - 1, 30);

        p_sheet.addCell(new Label(col++, row, m_bundle
                .getString("lb_glossary_source"), getHeaderFormat()));
        p_sheet.setColumnView(col - 1, 30);
        p_sheet.addCell(new Label(col++, row, m_bundle
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
                .getString("lb_translation_edit_report"), titleFormat));
        p_sheet.setColumnView(0, 40);
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
        // Till now, only support one target locale.
        GlobalSightLocale tgtL = p_targetLocales.get(0);

        // Create Sheet
        WritableSheet sheet = p_workbook.createSheet(
                m_bundle.getString("lb_ter"), 0);
        sheet.getSettings().setProtected(true);

        // Add Title
        addTitle(sheet);
        // Add Locale Pair Header
        addLanguageHeader(sheet);
        // Add Segment Header
        addSegmentHeader(sheet);
        // Create Name Areas for drop down list.
        createNameAreas(p_workbook);

        // Insert Data into Report
        writeLanguageInfo(sheet, p_job, tgtL.getDisplayName(m_uiLocale),
                p_dateFormat);
        writeSegmentInfo(sheet, p_job, tgtL.getDisplayName(), "", p_dateFormat,
                SEGMENT_START_ROW);
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
            String p_targetLang, String p_dateFormat) throws Exception
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
     * 
     * @return
     * @throws Exception
     */
    private WritableCellFeatures getSelectFeatures(String p_nameedRange)
            throws Exception
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
                String companyId = CompanyWrapper.getCompanyIdByName(m_companyName);
                List<String> categories = getFailureCategoriesList(companyId);
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

    private List<String> getFailureCategoriesList(String companyId)
    {
        List<String> result = new ArrayList<String>();

        String currentCompanyId = companyId;
        List failureCategories = IssueOptions.getAllCategories(m_bundle,
                currentCompanyId);
        for (int i = 0; i < failureCategories.size(); i++)
        {
            Select aCategory = (Select) failureCategories.get(i);
            result.add(aCategory.getValue());
        }
        return result;
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
    private int writeSegmentInfo(WritableSheet p_sheet, Job p_job,
            String p_targetLang, String p_srcPageId, String p_dateFormat,
            int p_row) throws Exception
    {
        long companyId = p_job.getCompanyId();
        Collection wfs = p_job.getWorkflows();
        Iterator it = wfs.iterator();
        Vector targetPages = new Vector();

        TranslationMemoryProfile tmp = null;
        List excludItems = null;

        while (it.hasNext())
        {
            if (cancel)
                return 0;

            Workflow workflow = (Workflow) it.next();

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
            writeBlank(p_sheet, p_row, 11);
        }
        else
        {
            LeverageMatchLingManager leverageMatchLingManager = LingServerProxy
                    .getLeverageMatchLingManager();
            TermLeverageManager termLeverageManager = ServerProxy
                    .getTermLeverageManager();
            CommentManager commentManager = ServerProxy.getCommentManager();

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

                if (!"".equals(p_srcPageId)
                        && !p_srcPageId.equals(String.valueOf(sourcePage
                                .getId())))
                {
                    // ignore the source pages not equal to the one
                    // if the request comes from pop up editor
                    continue;
                }

                SegmentTuUtil.getTusBySourcePageId(sourcePage.getId());
                List sourceTuvs = getPageTuvs(sourcePage);
                List targetTuvs = getPageTuvs(targetPage);
                Map exactMatches = leverageMatchLingManager.getExactMatches(
                        sourcePage.getIdAsLong(),
                        new Long(targetPage.getLocaleId()));
                Map leverageMatcheMap = leverageMatchLingManager
                        .getFuzzyMatches(sourcePage.getIdAsLong(), new Long(
                                targetPage.getLocaleId()));

                Locale sourcePageLocale = sourcePage.getGlobalSightLocale()
                        .getLocale();
                Locale targetPageLocale = targetPage.getGlobalSightLocale()
                        .getLocale();

                boolean m_rtlSourceLocale = EditUtil
                        .isRTLLocale(sourcePageLocale.toString());
                boolean m_rtlTargetLocale = EditUtil
                        .isRTLLocale(targetPageLocale.toString());

                TermLeverageOptions termLeverageOptions = getTermLeverageOptions(
                        sourcePageLocale, targetPageLocale, p_job
                                .getL10nProfile().getProject()
                                .getTermbaseName(),
                        String.valueOf(p_job.getCompanyId()));

                TermLeverageResult termLeverageResult = null;
                if (termLeverageOptions != null)
                {
                    termLeverageResult = termLeverageManager.leverageTerms(
                            sourceTuvs, termLeverageOptions,
                            String.valueOf(companyId));
                }
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
                    String commentStatus = "";
                    Date issueCreatedDate = null;
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
                                // if (Issue.CATEGORY_SPELLING
                                // .equalsIgnoreCase(failure))
                                // {
                                // failure = failure.replaceAll(",", "");
                                // }
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
                                if (cancel)
                                    return 0;

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
                                    if (cancel)
                                        return 0;

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
                    p_sheet.addCell(new Number(col++, p_row,
                            targetPage.getId(), format));
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

                    WritableCellFormat requiredTranslationFormat = m_rtlTargetLocale ? unlockedRightFormat()
                            : unlockedFormat();
                    // Required translation
                    p_sheet.addCell(new Label(col++, p_row, "",
                            requiredTranslationFormat));
                    p_sheet.setColumnView(col - 1, 40);

                    p_sheet.addCell(new Label(col++, p_row, lastComment, format));
                    p_sheet.setColumnView(col - 1, 40);

                    // Required comment
                    p_sheet.addCell(new Label(col++, p_row, "",
                            unlockedFormat()));
                    p_sheet.setColumnView(col - 1, 40);

                    // Category failure
                    WritableCellFormat cfFormat = format;
                    Label dropdown = null;
                    dropdown = new Label(col++, p_row, failure, cfFormat);
                    dropdown.setCellFeatures(getSelectFeatures(CATEGORY_LIST));
                    p_sheet.addCell(dropdown);
                    p_sheet.setColumnView(col - 1, 30);

                    // Comment Status
                    dropdown = new Label(col++, p_row, commentStatus,
                            unlockedFormat());
                    dropdown.setCellFeatures(getSelectFeaturesCommentStatus(lastComment));
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
}
