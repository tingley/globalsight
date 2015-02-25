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
import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import com.globalsight.everest.comment.Issue;
import com.globalsight.everest.comment.IssueHistory;
import com.globalsight.everest.comment.IssueImpl;
import com.globalsight.everest.comment.IssueOptions;
import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.edit.CommentHelper;
import com.globalsight.everest.foundation.SearchCriteriaParameters;
import com.globalsight.everest.integration.ling.LingServerProxy;
import com.globalsight.everest.integration.ling.tm2.LeverageMatch;
import com.globalsight.everest.integration.ling.tm2.MatchTypeStatistics;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.page.SourcePage;
import com.globalsight.everest.page.TargetPage;
import com.globalsight.everest.persistence.tuv.SegmentTuTuvCacheManager;
import com.globalsight.everest.persistence.tuv.SegmentTuUtil;
import com.globalsight.everest.persistence.tuv.SegmentTuvUtil;
import com.globalsight.everest.projecthandler.TranslationMemoryProfile;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.taskmanager.Task;
import com.globalsight.everest.tuv.TaskTuv;
import com.globalsight.everest.tuv.Tuv;
import com.globalsight.everest.tuv.TuvException;
import com.globalsight.everest.tuv.TuvManager;
import com.globalsight.everest.util.comparator.GlobalSightLocaleComparator;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.pagehandler.administration.company.Select;
import com.globalsight.everest.webapp.pagehandler.administration.reports.ReportConstants;
import com.globalsight.everest.webapp.pagehandler.administration.reports.ReportHelper;
import com.globalsight.everest.webapp.pagehandler.administration.reports.bo.ReportsData;
import com.globalsight.everest.webapp.pagehandler.administration.users.UserUtil;
import com.globalsight.everest.webapp.pagehandler.projects.workflows.JobSearchConstants;
import com.globalsight.everest.workflowmanager.Workflow;
import com.globalsight.ling.tm.LeverageMatchLingManager;
import com.globalsight.ling.tm2.leverage.LeverageUtil;
import com.globalsight.terminology.termleverager.TermLeverageManager;
import com.globalsight.terminology.termleverager.TermLeverageMatch;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.SortUtil;
import com.globalsight.util.StringUtil;
import com.globalsight.util.edit.EditUtil;

/**
 * Comments Analysis Report Generator Include Comments Analysis Report in popup
 * editor
 */
public class CommentsAnalysisReportGenerator implements ReportGenerator
{
    private static final Logger logger = Logger
            .getLogger(CommentsAnalysisReportGenerator.class);
    final String TAG_COMBINED = "-combined";
    private HttpServletRequest request = null;
    private ResourceBundle bundle = null;

    private static final String CATEGORY_FAILURE_DROP_DOWN_LIST = "categoryFailureDropDownList";

    private CellStyle titleStyle = null;
    private CellStyle contentStyle = null;
    private CellStyle rtlContentStyle = null;
    private CellStyle headerStyle = null;
    private CellStyle unlockedStyle = null;
    private CellStyle unlockedRightStyle = null;
    private boolean isCombineAllJobs = false;
    
    private Map<String, Integer> rowsMap = new HashMap<String, Integer>();

    private static final String DEFAULT_DATE_FORMAT = "MM/dd/yy hh:mm:ss a z";

    protected String m_companyName = "";
    protected List<Long> m_jobIDS = new ArrayList<Long>();
    protected List<GlobalSightLocale> m_targetLocales = new ArrayList<GlobalSightLocale>();

    public static final String reportType = ReportConstants.COMMENTS_ANALYSIS_REPORT;

    public static final int LANGUAGE_HEADER_ROW = 3;
    public static final int LANGUAGE_INFO_ROW = 4;
    public static final int SEGMENT_HEADER_ROW = 6;
    public static final int SEGMENT_START_ROW = 7;
    
    // "I" column, index 8
    public static final int CATEGORY_FAILURE_COLUMN = 8;

    private Locale uiLocale = new Locale("en", "US");
    String m_userId;

    /**
     * Comments Analysis Report Generator Constructor.
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
        bundle = PageHandler.getBundle(request.getSession());
        
        String combineAllJobs = p_request.getParameter("combineAllJobs");
        if("on".equals(combineAllJobs))
        {
        	isCombineAllJobs = true;
        }
        else
        {
        	isCombineAllJobs = false;
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
        Workbook combinedWorkBook = new SXSSFWorkbook();
        List<Job> jobsList = new ArrayList<Job>();
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
            if(isCombineAllJobs)
            {
            	jobsList.add(job);
            	createReport(combinedWorkBook, job, p_targetLocales, dateFormat);
            }
            else 
            {
                setAllCellStyleNull();
                Workbook workBook = new SXSSFWorkbook();
                createReport(workBook, job, p_targetLocales, dateFormat);
                File file = getFile(reportType, job, workBook);
                FileOutputStream out = new FileOutputStream(file);
                workBook.write(out);
                out.close();
                ((SXSSFWorkbook)workBook).dispose();
                workBooks.add(file);
			}

            // Sets Reports Percent.
            setPercent(++finishedJobNum);
        }
        
        if(isCombineAllJobs)
        {
        	File file = getFile(reportType + TAG_COMBINED, null, combinedWorkBook);
        	addCriteriaSheet(combinedWorkBook, jobsList, stateSet, projectSet);
            FileOutputStream out = new FileOutputStream(file);
            combinedWorkBook.write(out);
            out.close();
            ((SXSSFWorkbook)combinedWorkBook).dispose();
            workBooks.add(file);
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
            if(isCombineAllJobs && p_workbook.getSheet(trgLocale.toString()) != null)
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

            // Insert Segment Data
            if(isCombineAllJobs)
            {
            	Integer row = null;
            	if(rowsMap.get(trgLang) == null)
            	{
	            	row = writeSegmentInfo(p_workbook, sheet, p_job, trgLocale, p_dateFormat,
	            			SEGMENT_START_ROW);
            	}
            	else
            	{
            		row = writeSegmentInfo(p_workbook, sheet, p_job, trgLocale, p_dateFormat,
            				rowsMap.get(trgLang));
				}
            	rowsMap.put(trgLang, row);
            }
            else
            {
            	writeSegmentInfo(p_workbook, sheet, p_job, trgLocale, p_dateFormat,
            			SEGMENT_START_ROW);
            }
        }
    }

    /**
     * Add segment header to the sheet for Comments Analysis Report
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
        p_sheet.setColumnWidth(col, 20 * 256);
        col++;
        
        Cell cell_B = getCell(segHeaderRow, col);
        cell_B.setCellValue(bundle.getString("lb_segment_id"));
        cell_B.setCellStyle(headerStyle);
        p_sheet.setColumnWidth(col, 20 * 256);
        col++;
        
        Cell cell_C = getCell(segHeaderRow, col);
        cell_C.setCellValue(bundle.getString("lb_targetpage_id"));
        cell_C.setCellStyle(headerStyle);
        p_sheet.setColumnWidth(col, 20 * 256);
        col++;

        Cell cell_D = getCell(segHeaderRow, col);
        cell_D.setCellValue(bundle.getString("lb_source_segment"));
        cell_D.setCellStyle(headerStyle);
        p_sheet.setColumnWidth(col, 40 * 256);
        col++;
        
        Cell cell_E = getCell(segHeaderRow, col);
        cell_E.setCellValue(bundle.getString("lb_target_segment"));
        cell_E.setCellStyle(headerStyle);
        p_sheet.setColumnWidth(col, 40 * 256);
        col++;
        
        Cell cell_F = getCell(segHeaderRow, col);
        cell_F.setCellValue(bundle.getString("lb_sid"));
        cell_F.setCellStyle(headerStyle);
        p_sheet.setColumnWidth(col, 40 * 256);
        col++;

        Cell cell_G = getCell(segHeaderRow, col);
        cell_G.setCellValue(bundle.getString("lb_character_count"));
        cell_G.setCellStyle(headerStyle);
        p_sheet.setColumnWidth(col, 30 * 256);
        col++;
        
        Cell cell_H = getCell(segHeaderRow, col);
        cell_H.setCellValue(bundle.getString("lb_comments"));
        cell_H.setCellStyle(headerStyle);
        p_sheet.setColumnWidth(col, 40 * 256);
        col++;
        
        Cell cell_I = getCell(segHeaderRow, col);
        cell_I.setCellValue(bundle.getString("lb_category_failure"));
        cell_I.setCellStyle(headerStyle);
        p_sheet.setColumnWidth(col, 30 * 256);
        col++;

        Cell cell_J = getCell(segHeaderRow, col);
        cell_J.setCellValue(bundle.getString("lb_tm_match_original"));
        cell_J.setCellStyle(headerStyle);
        p_sheet.setColumnWidth(col, 30 * 256);
        col++;
        
        Cell cell_K = getCell(segHeaderRow, col);
        cell_K.setCellValue(bundle.getString("lb_glossary_source"));
        cell_K.setCellStyle(headerStyle);
        p_sheet.setColumnWidth(col, 30 * 256);
        col++;
        
        Cell cell_L = getCell(segHeaderRow, col);
        cell_L.setCellValue(bundle.getString("lb_glossary_target"));
        cell_L.setCellStyle(headerStyle);
        p_sheet.setColumnWidth(col, 30 * 256);
        col++;
        
        Cell cell_M = getCell(segHeaderRow, col);
        cell_M.setCellValue(bundle.getString("lb_previous_segment_report"));
        cell_M.setCellStyle(headerStyle);
        p_sheet.setColumnWidth(col, 40 * 256);
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
        titleCell.setCellValue(bundle.getString("review_comments"));
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
     * For Comments Analysis Report, Write segment information into each row of
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
        SimpleDateFormat dateFormat = new SimpleDateFormat(p_dateFormat);
        Vector<TargetPage> targetPages = new Vector<TargetPage>();

        long jobId = p_job.getId();

        TranslationMemoryProfile tmp = p_job.getL10nProfile()
                .getTranslationMemoryProfile();
        Vector<String> excludItems = null;

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
                tmp = workflow.getJob().getL10nProfile()
                        .getTranslationMemoryProfile();
                if (tmp != null)
                {
                    excludItems = tmp.getJobExcludeTuTypes();
                }
            }
        }

        if (!targetPages.isEmpty())
        {
            LeverageMatchLingManager leverageMatchLingManager = LingServerProxy
                    .getLeverageMatchLingManager();
            TermLeverageManager termLeverageManager = ServerProxy
                    .getTermLeverageManager();
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
                MatchTypeStatistics tuvMatchTypes = leverageMatchLingManager
                        .getMatchTypesForStatistics(
                                sourcePage.getIdAsLong(),
                                targetPage.getLocaleId(),
                                p_job.getLeverageMatchThreshold());
                Map<Long, Set<LeverageMatch>> fuzzyLeverageMatcheMap = leverageMatchLingManager
                        .getFuzzyMatches(sourcePage.getIdAsLong(),
                                targetPage.getLocaleId());

                // Find segment comments
                Map<Long, IssueImpl> issuesMap = CommentHelper
                        .getIssuesMap(targetPage.getId());
                
                // tuvId : Tuv
                Map<Long, Tuv> allTuvMap = this.getAllTuvsMap(targetPage);

                Locale sourcePageLocale = sourcePage.getGlobalSightLocale()
                        .getLocale();
                Locale targetPageLocale = targetPage.getGlobalSightLocale()
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

                    category = sourceTuv.getTu(jobId).getTuType();
                    if (excludItems != null && excludItems.contains(category))
                    {
                        continue;
                    }
                    
                    StringBuilder matches = getMatches(fuzzyLeverageMatcheMap,
                            tuvMatchTypes, excludItems, sourceTuvs, targetTuvs,
                            sourceTuv, targetTuv, p_job.getId());

                    List<IssueHistory> issueHistories = new ArrayList<IssueHistory>();
                    String failure = "";
                    Issue issue = issuesMap.get(targetTuv.getId());
                    if (issue != null)
                    {
                        issueHistories = issue.getHistory();
                        failure = issue.getCategory();
                    }

                    StringBuilder comments = new StringBuilder();
                    for (int k = 0; k < issueHistories.size(); k++)
                    {
                        IssueHistory issueHistory = (IssueHistory) issueHistories.get(k);
                        String date = dateFormat.format(issueHistory
                                .dateReportedAsDate());
                        comments.append("[")
                                .append(date)
                                .append("     ")
                                .append(UserUtil.getUserNameById(issueHistory
                                        .reportedBy())).append("]:\r\n")
                                .append(issueHistory.getComment());
                        if (k != issueHistories.size() - 1)
                        {
                            comments.append("\r\n");
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

                    CellStyle contentStyle = getContentStyle(p_workBook);
                    Row currentRow = getRow(p_sheet, p_row);
                    
                    // Job id
                    Cell cell_A = getCell(currentRow, col);
                    cell_A.setCellValue(p_job.getId());
                    cell_A.setCellStyle(contentStyle);
                    col++;

                    // Segment id
                    Cell cell_B = getCell(currentRow, col);
                    cell_B.setCellValue(sourceTuv.getTu(jobId).getId());
                    cell_B.setCellStyle(contentStyle);
                    col++;

                    // TargetPage id
                    Cell cell_C = getCell(currentRow, col);
                    cell_C.setCellValue(targetPage.getId());
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

                    // Character count
                    Cell cell_G = getCell(currentRow, col);
                    cell_G.setCellValue(targetSegmentString.length());
                    cell_G.setCellStyle(contentStyle);
                    col++;

                    // Comments
                    CellStyle commentStyle = rtlTargetLocale ? getUnlockedRightStyle(p_workBook)
                            : getUnlockedStyle(p_workBook);
                    String commentContent = rtlTargetLocale ? EditUtil
                            .toRtlString(comments.toString()) : comments
                            .toString();
                    Cell cell_H = getCell(currentRow, col);
                    cell_H.setCellValue(commentContent);
                    cell_H.setCellStyle(commentStyle);
                    col++;

                    // Category failure
                    CellStyle unlockedStyle = getUnlockedStyle(p_workBook);
                    Cell cell_I = getCell(currentRow, col);
                    cell_I.setCellValue(failure);
                    cell_I.setCellStyle(unlockedStyle);
                    col++;

                    // TM match
                    Cell cell_J = getCell(currentRow, col);
                    cell_J.setCellValue(matches.toString());
                    cell_J.setCellStyle(contentStyle);
                    col++;

                    // Glossary source
                    Cell cell_K = getCell(currentRow, col);
                    cell_K.setCellValue(sourceTerms);
                    cell_K.setCellStyle(contentStyle);
                    col++;

                    // Glossary target
                    Cell cell_L = getCell(currentRow, col);
                    cell_L.setCellValue(targetTerms);
                    cell_L.setCellStyle(contentStyle);
                    col++;

                    // Previous segment
                    String previousSegments = getPreviousSegments(allTuvMap,
                            targetTuv.getId(), targetSegmentString, jobId);
                    Cell cell_M = getCell(currentRow, col);
                    cell_M.setCellValue(previousSegments);
                    cell_M.setCellStyle(contentStyle);

                    p_row++;
                }

                SegmentTuTuvCacheManager.clearCache();
                sourceTuvs = null;
                targetTuvs = null;
                fuzzyLeverageMatcheMap = null;
                issuesMap = null;
				allTuvMap = null;
            }
            // Add category failure drop down list here.
            addCategoryFailureValidation(p_sheet, SEGMENT_START_ROW, p_row,
                    CATEGORY_FAILURE_COLUMN, CATEGORY_FAILURE_COLUMN);
            
            termLeverageMatchResultMap = null;
        }

        return p_row;
    }

    private void addCriteriaSheet(Workbook p_workbook, List<Job> p_jobsList,
    		Set<String> stateSet, Set<String> projectSet) throws Exception
    {
        Sheet sheet = p_workbook.createSheet(bundle.getString("lb_criteria"));
        StringBuffer temp = new StringBuffer();
        int row = 0;
        int headerColumn = 1;
        int valueColumn = 2;

        Row targetLocalesRow = getRow(sheet, row++);
        Row statusRow = getRow(sheet, row++);
        Row projectRow = getRow(sheet, row++);
        Row dateRangeRow = getRow(sheet, row++);
        Row jobsRow = getRow(sheet, row);

        Cell cell_TargetLocales = getCell(targetLocalesRow, headerColumn);
        cell_TargetLocales.setCellValue(bundle.
        		getString("lb_report_target_locales"));
        cell_TargetLocales.setCellStyle(getContentStyle(p_workbook));
        sheet.setColumnWidth(headerColumn, 20 * 256);
        
        Cell cell_Status = getCell(statusRow, headerColumn);
        cell_Status.setCellValue(bundle.getString("job_status"));
        cell_Status.setCellStyle(getContentStyle(p_workbook));
        
        Cell cell_Project = getCell(projectRow, headerColumn);
        cell_Project.setCellValue(bundle.getString("lb_report_project"));
        cell_Project.setCellStyle(getContentStyle(p_workbook));

        Cell cell_DateRange = getCell(dateRangeRow, headerColumn);
        cell_DateRange.setCellValue(bundle.getString("lb_report_date_range"));
        cell_DateRange.setCellStyle(getContentStyle(p_workbook));

        Cell cell_Jobs = getCell(jobsRow, headerColumn);
        cell_Jobs.setCellValue(bundle.getString("lb_jobs"));
        cell_Jobs.setCellStyle(getContentStyle(p_workbook));

        //locale
        Set<String> localeSet = rowsMap.keySet();
        for (String locale : localeSet)
        {
            temp.append(locale + "\n");
        }
        Cell cell_TargetLocalesValue = getCell(targetLocalesRow, valueColumn);
        cell_TargetLocalesValue.setCellValue(temp.substring(0,
                temp.length() - 1));
        cell_TargetLocalesValue.setCellStyle(getContentStyle(p_workbook));
        sheet.setColumnWidth(valueColumn, 45 * 256);
        
        //status
        temp.setLength(0);
        for(String status: stateSet)
        {
        	temp.append(status + "\n");
        }
        Cell cell_StatusValue = getCell(statusRow, valueColumn);
        cell_StatusValue.setCellValue(temp.substring(0,
                temp.length() - 1));
        cell_StatusValue.setCellStyle(getContentStyle(p_workbook));
        
     	//project
        temp.setLength(0);
        for(String status: projectSet)
        {
        	temp.append(status + "\n");
        }
        Cell cell_ProjectValue = getCell(projectRow, valueColumn);
        cell_ProjectValue.setCellValue(temp.substring(0,
                temp.length() - 1));
        cell_ProjectValue.setCellStyle(getContentStyle(p_workbook));

        //date range
        temp.setLength(0);
        String startCount = request
                .getParameter(JobSearchConstants.CREATION_START);
        if (startCount != null && startCount != "")
        {
            temp.append("Starts:" + startCount + "\n");
        }

        String endCount = request.getParameter(JobSearchConstants.CREATION_END);

        if (endCount != null && endCount != "")
        {
            temp.append("Ends:" + endCount);
        }
        Cell cell_DateRangeValue = getCell(dateRangeRow, valueColumn);
        cell_DateRangeValue.setCellValue(temp.toString());
        cell_DateRangeValue.setCellStyle(getContentStyle(p_workbook));

        //jobs
        int jobRowNum = 4;
        for (Job job : p_jobsList)
        {
            Cell cell_JobValue = getCell(getRow(sheet, jobRowNum++),
                    valueColumn);
            cell_JobValue.setCellValue(job.getJobId() + "," + job.getJobName());
            cell_JobValue.setCellStyle(getContentStyle(p_workbook));
        }
    }
    
    private String translateOpts(String opt)
    {
        String optLabel = "";
        if (SearchCriteriaParameters.NOW.equals(opt))
        {
            optLabel = bundle.getString("lb_now");
        }
        else if (SearchCriteriaParameters.HOURS_AGO.equals(opt))
        {
            optLabel = bundle.getString("lb_hours_ago");
        }
        else if (SearchCriteriaParameters.DAYS_AGO.equals(opt))
        {
            optLabel = bundle.getString("lb_days_ago");
        }
        else if (SearchCriteriaParameters.WEEKS_AGO.equals(opt))
        {
            optLabel = bundle.getString("lb_weeks_ago");
        }
        else if (SearchCriteriaParameters.MONTHS_AGO.equals(opt))
        {
            optLabel = bundle.getString("lb_months_ago");
        }

        return optLabel;
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
        this.unlockedStyle = null;
        this.unlockedRightStyle = null;
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

    private CellStyle getUnlockedStyle(Workbook p_workbook)
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
            matches.append(bundle.getString("lb_in_context_match"));
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
    
    @SuppressWarnings("unchecked")
    private String getPreviousSegments(Map<Long, Tuv> allTargetTuvsMap,
            long p_trgTuvId, String targetSegmentString, long p_jobId)
            throws TuvException, RemoteException
    {
        List previousTaskTuvs = new ArrayList();
        TuvManager tuvManager = ServerProxy.getTuvManager();
        previousTaskTuvs = tuvManager.getPreviousTaskTuvs(
                p_trgTuvId, 10);
        String previousSegments = "";
        String previousSegment = null;
        List previous = new ArrayList();
        if (previousTaskTuvs.size() > 0)
        {
            SortUtil.sort(previousTaskTuvs, new Comparator()
            {
                public int compare(Object p_taskTuvA,
                        Object p_taskTuvB)
                {
                    TaskTuv a = (TaskTuv) p_taskTuvA;
                    TaskTuv b = (TaskTuv) p_taskTuvB;
                    return a.getTask()
                            .getCompletedDate()
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
                String previousSeg = "";
                TaskTuv taskTuv = (TaskTuv) previousTaskTuvs.get(0);
                Tuv previousTuv = allTargetTuvsMap.get(taskTuv.getPreviousTuvId());
                // generally it should not be null for performance
                if (previousTuv != null)
                {
                    previousSeg = previousTuv.getGxmlElement().getTextValue();
                }
                else
                {
                    previousSeg = taskTuv.getTuv(p_jobId).getGxmlElement()
                            .getTextValue();
                }
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
                    TaskTuv taskTuv = (TaskTuv) previousTaskTuvs.get(k);
                    Tuv previousTuv = allTargetTuvsMap.get(taskTuv.getPreviousTuvId());
                    // generally it should not be null for performance
                    if (previousTuv != null)
                    {
                        previousSegment = previousTuv.getGxmlElement().getTextValue();
                    }
                    else
                    {
                        previousSegment = taskTuv.getTuv(p_jobId)
                                .getGxmlElement().getTextValue();
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
                        previousSegments = previousSegments
                                + previousSegment;
                    }
                }
            }
        }
        return previousSegments;
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
