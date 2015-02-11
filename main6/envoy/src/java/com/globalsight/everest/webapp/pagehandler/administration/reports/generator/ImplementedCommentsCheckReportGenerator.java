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
import com.globalsight.everest.comment.IssueHistory;
import com.globalsight.everest.comment.IssueImpl;
import com.globalsight.everest.comment.IssueOptions;
import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.edit.CommentHelper;
import com.globalsight.everest.integration.ling.LingServerProxy;
import com.globalsight.everest.integration.ling.tm2.LeverageMatch;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.page.SourcePage;
import com.globalsight.everest.page.TargetPage;
import com.globalsight.everest.persistence.tuv.SegmentTuTuvCacheManager;
import com.globalsight.everest.persistence.tuv.SegmentTuUtil;
import com.globalsight.everest.persistence.tuv.SegmentTuvUtil;
import com.globalsight.everest.projecthandler.TranslationMemoryProfile;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.tuv.Tuv;
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
import com.globalsight.ling.tm.LeverageSegment;
import com.globalsight.terminology.termleverager.TermLeverageManager;
import com.globalsight.terminology.termleverager.TermLeverageMatch;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.StringUtil;
import com.globalsight.util.edit.EditUtil;

public class ImplementedCommentsCheckReportGenerator implements ReportGenerator
{
    private static final Logger logger = Logger
            .getLogger(ImplementedCommentsCheckReportGenerator.class);
    
    final String TAG_COMBINED = "-combined";
    private HttpServletRequest request = null;
    private ResourceBundle bundle = null;

    private CellStyle titleStyle = null;
    private CellStyle contentStyle = null;
    private CellStyle rtlContentStyle = null;
    private CellStyle headerStyle = null;
    private CellStyle unlockedStyle = null;
    private CellStyle unlockedRightStyle = null;
    private CellStyle hightLightRightStyle = null;
    private CellStyle hightLightStyle = null;
    
    protected String m_companyName = "";
    protected List<Long> m_jobIDS = new ArrayList<Long>();
    protected List<GlobalSightLocale> m_targetLocales = new ArrayList<GlobalSightLocale>();
    
    private static final String CATEGORY_FAILURE_DROP_DOWN_LIST = "categoryFailureDropDownList";
    private static final String DEFAULT_DATE_FORMAT = "MM/dd/yy hh:mm:ss a z";
    
    public static final String reportType = ReportConstants.IMPLEMENTED_COMMENTS_CHECK_REPORT;
    public static final int LANGUAGE_HEADER_ROW = 3;
    public static final int LANGUAGE_INFO_ROW = 4;
    public static final int SEGMENT_HEADER_ROW = 6;
    public static final int SEGMENT_START_ROW = 7;
    public static final int CATEGORY_FAILURE_COLUMN = 7;
    
    private Locale uiLocale = new Locale("en", "US");
    String m_userId;

    /**
     * Implemented Comments CheckReport Generator Constructor.
     * 
     * @param p_request
     *            the request
     * @param p_response
     *            the response
     * @throws Exception
     */
    public ImplementedCommentsCheckReportGenerator(HttpServletRequest p_request,
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
        bundle = PageHandler.getBundle(request.getSession());

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
        ArrayList<String> stateList = ReportHelper.getAllJobStatusList();
        stateList.remove(Job.PENDING);

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
        String dateFormat = request.getParameter(WebAppConstants.DATE_FORMAT);
        Set<String> stateSet = new HashSet<String>();
        Set<String> projectSet = new HashSet<String>();
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
			if (job == null || !stateList.contains(job.getState()))
				continue;

			stateSet.add(job.getDisplayState());
			projectSet.add(job.getProject().getName());

			setAllCellStyleNull();
			Workbook workBook = new SXSSFWorkbook();
			createReport(workBook, job, p_targetLocales, dateFormat);
			File file = getFile(reportType, job, workBook);
			FileOutputStream out = new FileOutputStream(file);
			workBook.write(out);
			out.close();
			((SXSSFWorkbook) workBook).dispose();
			workBooks.add(file);

			// Sets Reports Percent.
			setPercent(++finishedJobNum);
		}

        return ReportHelper.moveReports(workBooks, m_userId);
    }

    /**
     * Create the report sheets
     * 
     * @throws Exception
     */
	private void createReport(Workbook p_workbook, Job p_job,
			List<GlobalSightLocale> p_targetLocales, String p_dateFormat)
			throws Exception
	{
		List<GlobalSightLocale> jobTL = ReportHelper.getTargetLocals(p_job);
		for (GlobalSightLocale trgLocale : p_targetLocales)
		{
			if (!jobTL.contains(trgLocale))
				continue;

			String srcLang = p_job.getSourceLocale().getDisplayName(uiLocale);
			String trgLang = trgLocale.getDisplayName(uiLocale);

			// Create One Report Sheet/Tab
			Sheet sheet = null;
			if (p_workbook.getSheet(trgLocale.toString()) != null)
			{
				sheet = p_workbook.getSheet(trgLocale.toString());
			}
			else
			{
				sheet = p_workbook.createSheet(trgLocale.toString());
				sheet.protectSheet("");

				// Add Title
				addTitle(p_workbook, sheet);

				// Add Locale Pair Header
				addLanguageHeader(p_workbook, sheet);

				// Add Segment Header
				addSegmentHeader(p_workbook, sheet);

				// Insert Language Data
				writeLanguageInfo(p_workbook, sheet, srcLang, trgLang);
			}

			// Create Name Areas for drop down list.
			if (p_workbook.getName(CATEGORY_FAILURE_DROP_DOWN_LIST) == null)
			{
				createCategoryFailureNameArea(p_workbook);
			}

    		writeSegmentInfo(p_workbook, sheet, p_job, trgLocale, p_dateFormat,
    					SEGMENT_START_ROW);
		}
	}

    /**
     * Add segment header to the sheet for Implemented Comments Check Report
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
        CellStyle headerStyle = getHeaderStyle(p_workBook);
        
        Cell cell_A = getCell(segHeaderRow, col);
        cell_A.setCellValue(bundle.getString("lb_job_id_report"));
        cell_A.setCellStyle(headerStyle);
        p_sheet.setColumnWidth(col, 30 * 256);
        col++;
        
        Cell cell_B = getCell(segHeaderRow, col);
        cell_B.setCellValue(bundle.getString("lb_segment_id"));
        cell_B.setCellStyle(headerStyle);
        p_sheet.setColumnWidth(col, 30 * 256);
        col++;
        
        Cell cell_C = getCell(segHeaderRow, col);
        cell_C.setCellValue(bundle.getString("lb_page_name"));
        cell_C.setCellStyle(headerStyle);
        p_sheet.setColumnWidth(col, 30 * 256);
        col++;

        Cell cell_D = getCell(segHeaderRow, col);
        cell_D.setCellValue(bundle.getString("lb_source_segment"));
        cell_D.setCellStyle(headerStyle);
        p_sheet.setColumnWidth(col, 40 * 256);
        col++;
        
        Cell cell_E = getCell(segHeaderRow, col);
        cell_E.setCellValue(bundle.getString("lb_target_segment"));
        cell_E.setCellStyle(headerStyle);
        p_sheet.setColumnWidth(col, 30 * 256);
        col++;
        
        Cell cell_F = getCell(segHeaderRow, col);
        cell_F.setCellValue(bundle.getString("lb_sid"));
        cell_F.setCellStyle(headerStyle);
        p_sheet.setColumnWidth(col, 40 * 256);
        col++;

        Cell cell_G = getCell(segHeaderRow, col);
        cell_G.setCellValue(bundle.getString("lb_comment_free"));
        cell_G.setCellStyle(headerStyle);
        p_sheet.setColumnWidth(col, 40 * 256);
        col++;
        
        Cell cell_H = getCell(segHeaderRow, col);
        cell_H.setCellValue(bundle.getString("lb_category_failure"));
        cell_H.setCellStyle(headerStyle);
        p_sheet.setColumnWidth(col, 30 * 256);
        col++;
        
        Cell cell_I = getCell(segHeaderRow, col);
        cell_I.setCellValue(bundle.getString("lb_tm_match_original"));
        cell_I.setCellStyle(headerStyle);
        p_sheet.setColumnWidth(col, 30 * 256);
        col++;

        Cell cell_J = getCell(segHeaderRow, col);
        cell_J.setCellValue(bundle.getString("lb_glossary_source"));
        cell_J.setCellStyle(headerStyle);
        p_sheet.setColumnWidth(col, 30 * 256);
        col++;
        
        Cell cell_K = getCell(segHeaderRow, col);
        cell_K.setCellValue(bundle.getString("lb_glossary_target"));
        cell_K.setCellStyle(headerStyle);
        p_sheet.setColumnWidth(col, 30 * 256);
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
        
        Row langRow = getRow(p_sheet, row);
        Cell srcLangCell = getCell(langRow, col);
        srcLangCell.setCellValue(bundle.getString("lb_source_language"));
        srcLangCell.setCellStyle(getHeaderStyle(p_workBook));
        col++;

        Cell trgLangCell = getCell(langRow, col);
        trgLangCell.setCellValue(bundle.getString("lb_target_language"));
        trgLangCell.setCellStyle(getHeaderStyle(p_workBook));
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
        Row titleRow = getRow(p_sheet, 0);
        Cell titleCell = getCell(titleRow, 0);
        titleCell.setCellValue(bundle.getString("implemented_comments_check_report"));
        titleCell.setCellStyle(getTitleStyle(p_workBook));
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
     * For Implemented Comments Check Report, Write segment information into each row of
     * the sheet.
     * 
     * @param p_workBook
     *            the workBook
     * @param p_sheet
     *            the sheet
     * @param p_job
     *            the job data for report
     * @param p_targetLocale
     *            the target locale
     * @param p_dateFormat
     *            the dateFormat
     * @param p_row
     *            the segment row in sheet
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    private int writeSegmentInfo(Workbook p_workBook, Sheet p_sheet, Job p_job,
            GlobalSightLocale p_targetLocale, String p_dateFormat, int p_row)
            throws Exception
    {
        Vector<TargetPage> targetPages = new Vector<TargetPage>();

        long companyId = p_job.getCompanyId();

        TranslationMemoryProfile tmp = p_job.getL10nProfile()
                .getTranslationMemoryProfile();
        List<String> excludItems = null;
        if (tmp != null)
        {
            excludItems = new ArrayList<String>(tmp.getJobExcludeTuTypes());
        }

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
            	p_targetLocale = workflow.getTargetLocale();
                targetPages = workflow.getTargetPages();
            }
        }
        if (targetPages.isEmpty())
        {
        	if (SEGMENT_START_ROW == p_row)
            {
                writeBlank(p_workBook , p_sheet, SEGMENT_START_ROW, 10);
            }
        }
        else
        {
            LeverageMatchLingManager leverageMatchLingManager = LingServerProxy
                    .getLeverageMatchLingManager();
            TermLeverageManager termLeverageManager = ServerProxy
                    .getTermLeverageManager();
            Locale sourcePageLocale = p_job.getSourceLocale().getLocale();
            Locale targetPageLocale = p_targetLocale.getLocale();
            Map<Long, Set<TermLeverageMatch>> termLeverageMatchResultMap = termLeverageManager
                    .getTermMatchesForPages(p_job.getSourcePages(),
                            p_targetLocale);

            TargetPage targetPage = null;
            SourcePage sourcePage = null;
            String sourceSegmentString = null;
            String targetSegmentString = null;
            String sid = null;
            for (int i = 0; i < targetPages.size(); i++)
            {
                targetPage = (TargetPage) targetPages.get(i);
                sourcePage = targetPage.getSourcePage();
                SegmentTuUtil.getTusBySourcePageId(sourcePage.getId());
                List sourceTuvs = SegmentTuvUtil.getSourceTuvs(sourcePage);
                List targetTuvs = SegmentTuvUtil.getTargetTuvs(targetPage);

                // Leverage TM
                Map<Long, LeverageSegment> exactMatches = leverageMatchLingManager
                        .getExactMatches(sourcePage.getIdAsLong(),
                                targetPage.getLocaleId());
                Map<Long, Set<LeverageMatch>> fuzzyLeverageMatcheMap = leverageMatchLingManager
                        .getFuzzyMatches(sourcePage.getIdAsLong(),
                                targetPage.getLocaleId());

                // Find segment comments
                Map<Long, IssueImpl> issuesMap = CommentHelper
                        .getIssuesMap(targetPage.getId());
                if(issuesMap.isEmpty())
                {
                    continue;
                }
                sourcePageLocale = sourcePage.getGlobalSightLocale()
                        .getLocale();
                targetPageLocale = targetPage.getGlobalSightLocale()
                        .getLocale();
                boolean rtlSourceLocale = EditUtil.isRTLLocale(sourcePageLocale
                        .toString());
                boolean rtlTargetLocale = EditUtil.isRTLLocale(targetPageLocale
                        .toString());

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
                    Date targetSegmentModifiedDate = targetTuv
                            .getLastModified();
                    
                    category = sourceTuv.getTu(companyId).getTuType();
                    if (excludItems != null && excludItems.contains(category))
                    {
                        continue;
                    }
                    
                    StringBuilder matches = getMatches(exactMatches, 
                    		fuzzyLeverageMatcheMap, targetTuv, sourceTuv);

                    List<IssueHistory> issueHistories = new ArrayList<IssueHistory>();
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
                    if("".equals(lastComment))
                    {
                    	continue;
                    }
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

                    CellStyle contentStyle = getContentStyle(p_workBook);
                    Row currentRow = getRow(p_sheet, p_row);
                    // Set color format for target segment changed
                    if (targetSegmentModifiedDate.before(issueCreatedDate))
                    {
                        contentStyle = getRedStyle(p_workBook);
                    }
                    // Job id
                    Cell cell_A = getCell(currentRow, col);
                    cell_A.setCellValue(p_job.getId());
                    cell_A.setCellStyle(contentStyle);
                    col++;

                    // Segment id
                    Cell cell_B = getCell(currentRow, col);
                    cell_B.setCellValue(sourceTuv.getTu(companyId).getId());
                    cell_B.setCellStyle(contentStyle);
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

                    // Page name
                    Cell cell_C = getCell(currentRow, col);
                    cell_C.setCellValue(Name);
                    cell_C.setCellStyle(contentStyle);
                    col++;


                    // Source segment
                    CellStyle srcStyle = rtlSourceLocale ? getRtlContentStyle(p_workBook)
                            : contentStyle;
                    String srcContent = rtlSourceLocale ? EditUtil
                            .toRtlString(sourceSegmentString)
                            : sourceSegmentString;
                    Cell cell_D = getCell(currentRow, col);
                    cell_D.setCellValue(srcContent);
                    cell_D.setCellStyle(srcStyle);
                    col++;

                    // Target segment
                    CellStyle trgStyle  = rtlTargetLocale ? getRtlContentStyle(p_workBook)
                            : contentStyle;
                    String content = rtlTargetLocale ? EditUtil
                            .toRtlString(targetSegmentString)
                            : targetSegmentString;
                    Cell cell_E = getCell(currentRow, col);
                    cell_E.setCellValue(content);
                    cell_E.setCellStyle(trgStyle);
                    col++;

                    // Sid
                    Cell cell_F = getCell(currentRow, col);
                    cell_F.setCellValue(sid);
                    cell_F.setCellStyle(contentStyle);
                    col++;
                    
                    //comment
                    CellStyle commentStyle = rtlTargetLocale ? getUnlockedRightStyle(p_workBook)
                            : getUnlockedStyle(p_workBook);
                    // Set color format for target segment changed
                    if (targetSegmentModifiedDate.before(issueCreatedDate))
                    {
                        commentStyle = rtlTargetLocale ? getRedRightStyle(p_workBook)
                                : getRedStyle(p_workBook);
                    }
                    if (rtlTargetLocale)
                    {
                    	 lastComment = EditUtil.toRtlString(lastComment);
                    }
                    Cell cell_G = getCell(currentRow, col);
                    cell_G.setCellValue(lastComment);
                    cell_G.setCellStyle(commentStyle);
                    col++;

                    // Category failure
                    CellStyle cfStyle = getUnlockedStyle(p_workBook);
                    // Set color format for target segment changed
                    if (targetSegmentModifiedDate.before(issueCreatedDate))
                    {
                        cfStyle = getRedStyle(p_workBook);
                    }
                    Cell cell_H = getCell(currentRow, col);
                    cell_H.setCellValue(failure);
                    cell_H.setCellStyle(cfStyle);
                    col++;

                    // TM match
                    Cell cell_I = getCell(currentRow, col);
                    cell_I.setCellValue(matches.toString());
                    cell_I.setCellStyle(contentStyle);
                    col++;

                    // Glossary source
                    Cell cell_J = getCell(currentRow, col);
                    cell_J.setCellValue(sourceTerms);
                    cell_J.setCellStyle(contentStyle);
                    col++;

                    // Glossary target
                    Cell cell_K = getCell(currentRow, col);
                    cell_K.setCellValue(targetTerms);
                    cell_K.setCellStyle(contentStyle);

                    p_row++;
                }

                SegmentTuTuvCacheManager.clearCache();
                sourceTuvs = null;
                targetTuvs = null;
                exactMatches = null;
                fuzzyLeverageMatcheMap = null;
                issuesMap = null;
            }
            // Add category failure drop down list here.
            addCategoryFailureValidation(p_sheet, SEGMENT_START_ROW, p_row,
                    CATEGORY_FAILURE_COLUMN, CATEGORY_FAILURE_COLUMN);
            
            termLeverageMatchResultMap = null;
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
    
    private List<String> getFailureCategoriesList()
    {
        List<String> result = new ArrayList<String>();

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

    private void setAllCellStyleNull()
    {
        this.titleStyle = null;
        this.headerStyle = null;
        this.contentStyle = null;
        this.rtlContentStyle = null;
    }
    
    private CellStyle getHeaderStyle(Workbook p_workbook)
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

    private CellStyle getContentStyle(Workbook p_workbook)
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

    private CellStyle getRtlContentStyle(Workbook p_workbook)
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
    
    private CellStyle getTitleStyle(Workbook p_workBook)
    {
        if (titleStyle == null)
        {
            // Title font is black bold on white
            Font titleFont = p_workBook.createFont();
            titleFont.setUnderline(Font.U_NONE);
            titleFont.setFontName("Times");
            titleFont.setFontHeightInPoints((short) 14);
            titleFont.setBoldweight(Font.BOLDWEIGHT_BOLD);
            CellStyle cs = p_workBook.createCellStyle();
            cs.setFont(titleFont);
            
            titleStyle = cs;
        }

        return titleStyle;
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

    /**
     * Get TM matches.
     */
    private StringBuilder getMatches(Map<Long, LeverageSegment> exactMatches,
            Map<Long, Set<LeverageMatch>> fuzzyLeverageMatcheMap, Tuv targetTuv, Tuv sourceTuv)
    {
    	StringBuilder matches = new StringBuilder();
    	Set<LeverageMatch>  leverageMatches = (Set<LeverageMatch>) fuzzyLeverageMatcheMap.get(sourceTuv
                .getIdAsLong());

        if (exactMatches.get(sourceTuv.getIdAsLong()) != null)
        {
            matches.append(StringUtil.formatPCT(100));
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
                    matches.append(++count)
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
    
    @Override
    public String getReportType()
    {
        return reportType;
    }

    @Override
    public void setPercent(int p_finishedJobNum)
    {
        ReportGeneratorHandler.setReportsMapByGenerator(m_userId, m_jobIDS, 100
                * p_finishedJobNum / m_jobIDS.size(), getReportType());
    }

    @Override
    public boolean isCancelled()
    {
        ReportsData data = ReportGeneratorHandler.getReportsMap(m_userId,
                m_jobIDS, getReportType());
        if (data != null)
            return data.isCancle();

        return false;
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
            if (srcLang == null)
            {
                Row languageInfoRow = sheet.getRow(LANGUAGE_INFO_ROW);
                if (languageInfoRow != null)
                {
                    srcLang = languageInfoRow.getCell(0).getStringCellValue();
                    srcLang = srcLang.substring(srcLang.indexOf("[") + 1, srcLang.indexOf("]"));
                    trgLang = languageInfoRow.getCell(1).getStringCellValue();
                    trgLang = trgLang.substring(trgLang.indexOf("[") + 1, trgLang.indexOf("]"));
                }
                else
                {
                    Row dataRow = sheet.getRow(sheet.getLastRowNum());
                    if (dataRow != null)
                    {
                        try
                        {
                            long jobId = (long) dataRow.getCell(0).getNumericCellValue();
                            Job job = ServerProxy.getJobHandler().getJobById(jobId);
                            srcLang = job.getSourceLocale().toString();
                        }
                        catch (Exception e)
                        {
                        }

                    }
                }
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
}
