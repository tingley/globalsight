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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ResourceBundle;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.localemgr.LocaleManagerException;
import com.globalsight.everest.page.SourcePage;
import com.globalsight.everest.persistence.tuv.BigTableUtil;
import com.globalsight.everest.persistence.tuv.SegmentTuvUtil;
import com.globalsight.everest.persistence.tuv.TuvQueryConstants;
import com.globalsight.everest.projecthandler.Project;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.tuv.TuvImpl;
import com.globalsight.everest.tuv.TuvPerplexity;
import com.globalsight.everest.tuv.TuvState;
import com.globalsight.everest.util.comparator.GlobalSightLocaleComparator;
import com.globalsight.everest.util.comparator.JobComparator;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.pagehandler.administration.reports.ReportConstants;
import com.globalsight.everest.webapp.pagehandler.administration.reports.ReportHelper;
import com.globalsight.everest.webapp.pagehandler.administration.users.UserUtil;
import com.globalsight.everest.workflowmanager.Workflow;
import com.globalsight.ling.tm2.persistence.DbUtil;
import com.globalsight.machineTranslation.MTHelper;
import com.globalsight.reports.ter.TERtest;
import com.globalsight.util.GeneralException;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.IntHolder;
import com.globalsight.util.ReportStyle;
import com.globalsight.util.SortUtil;
import com.globalsight.util.StringUtil;
import com.globalsight.util.edit.EditUtil;
import com.globalsight.util.gxml.GxmlElement;

public class MTPostEditDistanceReportGenerator implements ReportGenerator
{
    static private final Logger logger = Logger.getLogger(MTPostEditDistanceReportGenerator.class);

    private static int SUMMARY_HEADER_ROW = 2;
    private static int DETAIL_HEADER_ROW = 2;

    private CellStyle contentStyle = null;
    private CellStyle rtlContentStyle = null;
    private CellStyle headerStyle = null;

    protected ResourceBundle m_bundle = null;
    protected HttpServletRequest m_request = null;
    protected String m_companyName = "";
    private RequestData data = new RequestData();
    private String m_userId;
    private String INCLUDE_SEGS_FROM_LATEST = "latest";
    private String INCLUDE_SEGS_FROM_ALL = "all";
    private String includeSegsFrom = INCLUDE_SEGS_FROM_LATEST;// default

    private NumberFormat twoDigitFormater = null;
    private NumberFormat threeDigitFormater = null;
    private boolean usePerplexity = false;
    private boolean isIncludeInternalText = true;
    private ReportStyle m_style = null;

    private static final String GET_DETAILED_DATA_SQL1 = "SELECT t1.Id as tuvId, t2.tuId, t2.source, t2.MT, t1.segment_string AS target, t2.mt_name, t1.state "
            + " FROM " + TuvQueryConstants.TUV_TABLE_PLACEHOLDER + " AS t1,"
            + " (SELECT lm.source_page_id, lm.original_source_tuv_id AS tuvId, tuv.tu_id AS tuId, tuv.segment_string AS source, lm.matched_text_string AS MT, lm.mt_name"
            + "  FROM " + TuvQueryConstants.TUV_TABLE_PLACEHOLDER + " AS tuv, "
            + TuvQueryConstants.LM_TABLE_PLACEHOLDER + " AS lm"
            + "  WHERE tuv.id = lm.original_source_tuv_id"
            + "  AND lm.source_page_id IN (_SOURCE_PAGE_IDS_)" + "  AND lm.target_locale_id = ?"
            + "  AND lm.order_num = 301" + "  AND lm.sub_id = '0'"
            + "  AND tuv.state != 'OUT_OF_DATE') AS t2" + "  WHERE t1.tu_id = t2.tuId"
            + "  AND t1.locale_id = ?";
    private static final String GET_DETAILED_DATA_SQL2 = " AND t1.state != 'OUT_OF_DATE'";
    private static final String GET_DETAILED_DATA_SQL3 = " ORDER BY t1.tu_id ASC, t1.id ASC";

    private class RequestData
    {
        // Report on job Ids
        boolean reportOnJobIds = true;

        @SuppressWarnings("unused")
        boolean wantsAllProjects = false;

        @SuppressWarnings("unused")
        boolean wantsAllLocales = false;

        // Need to search job by projects, status, date
        boolean needSearchJob = false;

        ArrayList<GlobalSightLocale> trgLocaleList = new ArrayList<GlobalSightLocale>();

        List<Long> projectIdList = new ArrayList<Long>();

        List<String> jobStateList = new ArrayList<String>();

        List<String> workflowStateList = new ArrayList<String>();

        List<Long> jobIds = new ArrayList<Long>();
    };

    // for junit test only
    public MTPostEditDistanceReportGenerator()
    {

    }

    public MTPostEditDistanceReportGenerator(HttpServletRequest p_request,
            HttpServletResponse p_response)
            throws LocaleManagerException, RemoteException, GeneralException
    {
        usePerplexity = CompanyWrapper.isUsePerplexity();
        HttpSession session = p_request.getSession();
        m_userId = (String) session.getAttribute(WebAppConstants.USER_NAME);
        m_bundle = PageHandler.getBundle(p_request.getSession());
        m_request = p_request;
        m_companyName = UserUtil.getCurrentCompanyName(p_request);
        CompanyThreadLocal.getInstance().setValue(m_companyName);

        includeSegsFrom = m_request.getParameter("includeSegsFrom");
        String includeInternalText = p_request.getParameter("includeInternalText");
        isIncludeInternalText = "on".equals(includeInternalText) ? true : false;

        setProjectIdList(p_request);
        setTargetLocales(p_request);
        setStates(p_request);
    }

    public void sendReports(List<Long> p_jobIDS, List<GlobalSightLocale> p_targetLocales,
            HttpServletResponse p_response) throws Exception
    {
        if (p_jobIDS == null || p_jobIDS.size() == 0)
        {
            p_jobIDS = data.jobIds;
            p_targetLocales = data.trgLocaleList;
        }
        else if (data.jobIds == null || data.jobIds.size() == 0)
        {
            data.jobIds = p_jobIDS;
        }

        File[] reports = generateReports(p_jobIDS, p_targetLocales);
        ReportHelper.sendFiles(reports, getReportType(), p_response);
    }

    @Override
    public File[] generateReports(List<Long> p_jobIDS,
            List<GlobalSightLocale> p_selectedTargetLocales) throws Exception
    {
        setAllCellStyleNull();

        if (p_jobIDS == null || p_jobIDS.size() == 0)
        {
            p_jobIDS = data.jobIds;
            p_selectedTargetLocales = data.trgLocaleList;
        }
        else if (data.jobIds == null || data.jobIds.size() == 0)
        {
            data.jobIds = p_jobIDS;
        }

        List<Job> jobs = ReportHelper.getJobListByIDS(p_jobIDS);

        filterInvalidJobs(jobs);

        // Find MTed workflows/locales
        List<Job> mtedJobs = new ArrayList<Job>();
        List<GlobalSightLocale> mtedTargetLocales = new ArrayList<GlobalSightLocale>();
        findMTedJobs(jobs, p_selectedTargetLocales, mtedJobs, mtedTargetLocales);

        Workbook workBook = new XSSFWorkbook();
        m_style = new ReportStyle(workBook);
        Sheet summarySheet = workBook.createSheet(m_bundle.getString("lb_summary"));
        Map<GlobalSightLocale, Sheet> detailedSheets = new HashMap<GlobalSightLocale, Sheet>();
        for (GlobalSightLocale locale : mtedTargetLocales)
        {
            detailedSheets.put(locale, workBook.createSheet(locale.toString()));
        }

        // Add report title in every sheet
        addReportTitle(workBook, summarySheet);
        addHeaderForSummary(workBook, summarySheet);

        Set<Entry<GlobalSightLocale, Sheet>> entries = detailedSheets.entrySet();
        Map<String, List<DetailedData>> dataMap = new HashMap<String, List<DetailedData>>();
        for (Entry<GlobalSightLocale, Sheet> entry : entries)
        {
            Sheet sheet = entry.getValue();

            addReportTitle(workBook, sheet);

            addHeaderForDetail(workBook, sheet);

            GlobalSightLocale targetLocale = entry.getKey();
            addPostEditDetail(workBook, sheet, mtedJobs, targetLocale, dataMap);
        }

        addJobsForSummary(workBook, summarySheet, mtedJobs, p_selectedTargetLocales, dataMap);

        File file = ReportHelper.getXLSReportFile(getReportType(), null);
        if (workBook != null)
        {
            FileOutputStream out = new FileOutputStream(file);
            workBook.write(out);
            out.close();
        }

        List<File> workBooks = new ArrayList<File>();
        workBooks.add(file);
        return ReportHelper.moveReports(workBooks, m_userId);
    }

    /**
     * Find MTed jobs and target locales.
     */
    private void findMTedJobs(List<Job> jobs, List<GlobalSightLocale> p_selectedTargetLocales,
            List<Job> mtedJobs, List<GlobalSightLocale> mtedTargetLocales)
    {
        Set<GlobalSightLocale> selectedTargetLocales = new HashSet<GlobalSightLocale>();
        selectedTargetLocales.addAll(p_selectedTargetLocales);

        Set<Job> mtedJobsSet = new HashSet<Job>();
        Set<GlobalSightLocale> mtedTargetLocalesSet = new HashSet<GlobalSightLocale>();
        for (Job job : jobs)
        {
            for (Workflow w : job.getWorkflows())
            {
                GlobalSightLocale gsl = w.getTargetLocale();
                if (w.getMtTotalWordCount() > 0 && selectedTargetLocales.contains(gsl))
                {
                    mtedTargetLocalesSet.add(w.getTargetLocale());
                    mtedJobsSet.add(job);
                }
            }
        }

        mtedTargetLocales.addAll(mtedTargetLocalesSet);
        SortUtil.sort(mtedTargetLocales,
                new GlobalSightLocaleComparator(GlobalSightLocaleComparator.ISO_CODE, Locale.US));

        mtedJobs.addAll(mtedJobsSet);
        SortUtil.sort(mtedJobs, new JobComparator(JobComparator.JOB_ID, Locale.US));
    }

    private void addReportTitle(Workbook p_workBook, Sheet p_sheet) throws Exception
    {
        Font titleFont = p_workBook.createFont();
        titleFont.setUnderline(Font.U_NONE);
        titleFont.setFontName("Arial");
        titleFont.setFontHeightInPoints((short) 14);
        titleFont.setBoldweight(Font.BOLDWEIGHT_BOLD);
        titleFont.setColor(IndexedColors.BLACK.getIndex());
        CellStyle titleStyle = p_workBook.createCellStyle();
        titleStyle.setWrapText(false);
        titleStyle.setFont(titleFont);

        Row firstRow = getRow(p_sheet, 0);
        Cell titleCell = getCell(firstRow, 0);
        titleCell.setCellValue(m_bundle.getString("mt_post_edit_distance_report"));
        titleCell.setCellStyle(titleStyle);

        p_sheet.setColumnWidth(0, 15 * 256);
    }

    /**
     * Adds the table header to the sheet
     * 
     * @param p_sheet
     */
    private void addHeaderForSummary(Workbook p_workBook, Sheet p_sheet)
    {
        int col = 0;
        int row = SUMMARY_HEADER_ROW;
        Row summaryHeaderRow = getRow(p_sheet, row);

        Cell cell_A = getCell(summaryHeaderRow, col);
        cell_A.setCellValue(m_bundle.getString("lb_company"));
        cell_A.setCellStyle(getHeaderStyle(p_workBook));
        p_sheet.setColumnWidth(col, 20 * 256);
        col++;

        Cell cell_B = getCell(summaryHeaderRow, col);
        cell_B.setCellValue(m_bundle.getString("lb_job_id"));
        cell_B.setCellStyle(getHeaderStyle(p_workBook));
        p_sheet.setColumnWidth(col, 20 * 256);
        col++;

        Cell cell_C = getCell(summaryHeaderRow, col);
        cell_C.setCellValue(m_bundle.getString("lb_job_name"));
        cell_C.setCellStyle(getHeaderStyle(p_workBook));
        p_sheet.setColumnWidth(col, 30 * 256);
        col++;

        Cell cell_D = getCell(summaryHeaderRow, col);
        cell_D.setCellValue(m_bundle.getString("lb_language"));
        cell_D.setCellStyle(getHeaderStyle(p_workBook));
        p_sheet.setColumnWidth(col, 20 * 256);
        col++;

        Cell cell_E = getCell(summaryHeaderRow, col);
        cell_E.setCellValue(m_bundle.getString("lb_workflow_state"));
        cell_E.setCellStyle(getHeaderStyle(p_workBook));
        p_sheet.setColumnWidth(col, 20 * 256);
        col++;

        Cell cell_F = getCell(summaryHeaderRow, col);
        cell_F.setCellValue(m_bundle.getString("lb_mt_word_count"));
        cell_F.setCellStyle(getHeaderStyle(p_workBook));
        p_sheet.setColumnWidth(col, 20 * 256);
        col++;

        if (usePerplexity)
        {
            Cell cell = getCell(summaryHeaderRow, col);
            cell.setCellValue(m_bundle.getString("lb_perplexity_wordcount"));
            cell.setCellStyle(getHeaderStyle(p_workBook));
            p_sheet.setColumnWidth(col, 20 * 256);
            col++;
        }

        Cell cell_G = getCell(summaryHeaderRow, col);
        cell_G.setCellValue(m_bundle.getString("lb_total_word_count"));
        cell_G.setCellStyle(getHeaderStyle(p_workBook));
        p_sheet.setColumnWidth(col, 20 * 256);
        col++;

        Cell cell_H = getCell(summaryHeaderRow, col);
        cell_H.setCellValue(m_bundle.getString("lb_average_edit_distance"));
        cell_H.setCellStyle(getHeaderStyle(p_workBook));
        p_sheet.setColumnWidth(col, 30 * 256);
        col++;

        Cell cell_I = getCell(summaryHeaderRow, col);
        cell_I.setCellValue(m_bundle.getString("lb_translation_error_rate"));
        cell_I.setCellStyle(getHeaderStyle(p_workBook));
        p_sheet.setColumnWidth(col, 20 * 256);
        col++;

        Cell cell_J = getCell(summaryHeaderRow, col);
        cell_J.setCellValue(m_bundle.getString("lb_engine_name"));
        cell_J.setCellStyle(getHeaderStyle(p_workBook));
        p_sheet.setColumnWidth(col, 20 * 256);
        col++;
    }

    private void addHeaderForDetail(Workbook p_workBook, Sheet p_sheet)
    {
        int col = 0;
        int row = DETAIL_HEADER_ROW;
        Row detailHeaderRow = getRow(p_sheet, row);

        Cell cell_A = getCell(detailHeaderRow, col);
        cell_A.setCellValue(m_bundle.getString("lb_job_id"));
        cell_A.setCellStyle(getHeaderStyle(p_workBook));
        p_sheet.setColumnWidth(col, 10 * 256);
        col++;

        Cell cell_B = getCell(detailHeaderRow, col);
        cell_B.setCellValue(m_bundle.getString("lb_job_name"));
        cell_B.setCellStyle(getHeaderStyle(p_workBook));
        p_sheet.setColumnWidth(col, 30 * 256);
        col++;

        Cell cell_C = getCell(detailHeaderRow, col);
        cell_C.setCellValue(m_bundle.getString("lb_tu_id"));
        cell_C.setCellStyle(getHeaderStyle(p_workBook));
        p_sheet.setColumnWidth(col, 10 * 256);
        col++;

        Cell cell_D = getCell(detailHeaderRow, col);
        cell_D.setCellValue(m_bundle.getString("lb_post_edit"));
        cell_D.setCellStyle(getHeaderStyle(p_workBook));
        p_sheet.setColumnWidth(col, 15 * 256);
        col++;

        Cell cell_E = getCell(detailHeaderRow, col);
        cell_E.setCellValue(m_bundle.getString("lb_translation_error_rate"));
        cell_E.setCellStyle(getHeaderStyle(p_workBook));
        p_sheet.setColumnWidth(col, 20 * 256);
        col++;

        Cell cell_F = getCell(detailHeaderRow, col);
        cell_F.setCellValue(m_bundle.getString("lb_source"));
        cell_F.setCellStyle(getHeaderStyle(p_workBook));
        p_sheet.setColumnWidth(col, 80 * 256);
        col++;

        Cell cell_G = getCell(detailHeaderRow, col);
        cell_G.setCellValue(m_bundle.getString("lb_tm_mt"));
        cell_G.setCellStyle(getHeaderStyle(p_workBook));
        p_sheet.setColumnWidth(col, 80 * 256);
        col++;

        Cell cell_H = getCell(detailHeaderRow, col);
        cell_H.setCellValue(m_bundle.getString("lb_translated_text"));
        cell_H.setCellStyle(getHeaderStyle(p_workBook));
        p_sheet.setColumnWidth(col, 80 * 256);
        col++;

        // For GBS-4495 perplexity score on MT
        if (usePerplexity)
        {
            Cell cell_I = getCell(detailHeaderRow, col);
            cell_I.setCellValue(m_bundle.getString("lb_source_perplexity"));
            cell_I.setCellStyle(getHeaderStyle(p_workBook));
            p_sheet.setColumnWidth(col, 15 * 256);
            col++;

            Cell cell_J = getCell(detailHeaderRow, col);
            cell_J.setCellValue(m_bundle.getString("lb_target_perplexity"));
            cell_J.setCellStyle(getHeaderStyle(p_workBook));
            p_sheet.setColumnWidth(col, 15 * 256);
            col++;

            Cell cell_K = getCell(detailHeaderRow, col);
            cell_K.setCellValue(m_bundle.getString("lb_pass_fail"));
            cell_K.setCellStyle(getHeaderStyle(p_workBook));
            p_sheet.setColumnWidth(col, 15 * 256);
            col++;
        }
    }

    private void addPostEditDetail(Workbook p_workBook, Sheet p_sheet, List<Job> jobs,
            GlobalSightLocale targetLocale, Map<String, List<DetailedData>> dataMap)
            throws Exception
    {
        IntHolder row = new IntHolder(3);
        for (Job job : jobs)
        {
            GlobalSightLocale sourceLocale = job.getSourceLocale();
            long jobId = job.getJobId();
            String jobName = job.getJobName();
            String sourcePageIds = getSourcePageIds(job);
            String sql = getSql(job, sourcePageIds);

            List<DetailedData> detailedDatas = getDetailedData(sql, jobId, jobName, targetLocale);
            if (INCLUDE_SEGS_FROM_ALL.equalsIgnoreCase(includeSegsFrom))
            {
                // Ensure translations for same TU are in order by translation
                // sequence, latest segment should be the last one for a TU.
                detailedDatas = sortDetailedData(detailedDatas);
                // Remove duplicated translations
                // detailedDatas = removeDuplicates(detailedDatas);
            }

            if (detailedDatas.size() > 0)
            {
                addDetailedDatas(p_workBook, p_sheet, detailedDatas, row, sourceLocale,
                        targetLocale);

                // "Summary" sheet ALWAYS need "latest" translation only.
                if (INCLUDE_SEGS_FROM_ALL.equalsIgnoreCase(includeSegsFrom))
                {
                    removeOutOfDateData(detailedDatas);
                }
                dataMap.put(getKey(jobId, targetLocale.getId()), detailedDatas);
            }
        }
    }

    private String getKey(long jobId, long targetLocaleId)
    {
        return jobId + "-" + targetLocaleId;
    }

    private List<DetailedData> sortDetailedData(List<DetailedData> detailedDatas)
    {
        List<DetailedData> newList = new ArrayList<DetailedData>();
        DetailedData curActiveData = null;
        for (DetailedData currData : detailedDatas)
        {
            if (TuvState.OUT_OF_DATE.getName().equalsIgnoreCase(currData.getState()))
            {
                newList.add(currData);
            }
            else
            {
                if (curActiveData != null && curActiveData.getTuId() != currData.getTuId())
                {
                    newList.add(curActiveData);
                }
                curActiveData = currData;
            }
        }

        if (curActiveData != null)
        {
            newList.add(curActiveData);
        }

        return newList;
    }

    /**
     * Remove duplicated translations(same tuId, same source/MT/target).
     */
    @SuppressWarnings("unused")
    private List<DetailedData> removeDuplicates(List<DetailedData> detailedDatas)
    {
        List<DetailedData> detailedDatasNoDuplicates = new ArrayList<DetailedData>();
        long previousTuId = -1;
        String previousTarget = null;
        for (int i = detailedDatas.size() - 1; i >= 0; i--)
        {
            DetailedData data = detailedDatas.get(i);
            if (data.getTuId() != previousTuId || !data.getTarget().equals(previousTarget)
                    || !TuvState.OUT_OF_DATE.getName().equalsIgnoreCase(data.getState()))
            {
                detailedDatasNoDuplicates.add(0, data);
            }

            previousTuId = data.getTuId();
            previousTarget = data.getTarget();
        }

        return detailedDatasNoDuplicates;
    }

    /**
     * For "Summary" sheet, it should ALWAYS include latest segments only.
     * 
     * @param detailedDatas
     */
    private void removeOutOfDateData(List<DetailedData> detailedDatas)
    {
        for (Iterator<DetailedData> it = detailedDatas.iterator(); it.hasNext();)
        {
            DetailedData data = it.next();
            if (TuvState.OUT_OF_DATE.getName().equalsIgnoreCase(data.getState()))
            {
                it.remove();
            }
        }
    }

    /**
     * Return source page IDs comma separated like "1,2,3".
     */
    @SuppressWarnings("rawtypes")
    private String getSourcePageIds(Job job)
    {
        StringBuilder spIds = new StringBuilder();
        Iterator spIter = job.getSourcePages().iterator();
        while (spIter.hasNext())
        {
            SourcePage sp = (SourcePage) spIter.next();
            spIds.append(sp.getId()).append(",");
        }

        String result = spIds.toString();
        if (result.endsWith(","))
        {
            return result.substring(0, result.length() - 1);
        }
        return "";
    }

    private String getSql(Job job, String sourcePageIds) throws Exception
    {
        String tuvTable = BigTableUtil.getTuvTableJobDataInByJobId(job.getId());
        String lmTable = BigTableUtil.getLMTableJobDataInByJobId(job.getId());
        String sql = null;
        if (INCLUDE_SEGS_FROM_ALL.equalsIgnoreCase(includeSegsFrom))
        {
            sql = GET_DETAILED_DATA_SQL1 + GET_DETAILED_DATA_SQL3;
        }
        else
        {
            sql = GET_DETAILED_DATA_SQL1 + GET_DETAILED_DATA_SQL2 + GET_DETAILED_DATA_SQL3;
        }

        return sql.replace(TuvQueryConstants.TUV_TABLE_PLACEHOLDER, tuvTable)
                .replace(TuvQueryConstants.LM_TABLE_PLACEHOLDER, lmTable)
                .replace("_SOURCE_PAGE_IDS_", sourcePageIds);
    }

    private List<DetailedData> getDetailedData(String sql, long jobId, String jobName,
            GlobalSightLocale targetLocale) throws Exception
    {
        List<DetailedData> detailedDatas = new ArrayList<DetailedData>();

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try
        {
            conn = DbUtil.getConnection();
            ps = conn.prepareStatement(sql.toString());
            ps.setLong(1, targetLocale.getIdAsLong());
            ps.setLong(2, targetLocale.getIdAsLong());
            rs = ps.executeQuery();

            while (rs.next())
            {
                DetailedData data = new DetailedData();
                data.setJobId(jobId);
                data.setJobName(jobName);

                long tuvId = rs.getLong("tuvId");
                data.setTuvId(tuvId);

                long tuId = rs.getLong("tuId");
                data.setTuId(tuId);

                String source = rs.getString("source");
                if (isIncludeInternalText)
                {
                    source = MTHelper.getGxmlElement(source).getTextValueWithInternalTextMark();
                }
                else
                {
                    source = MTHelper.getGxmlElement(source).getTextValueWithoutInternalText();
                }
                data.setSource(source);

                String mt = rs.getString("MT");
                if (isIncludeInternalText)
                {
                    mt = MTHelper.getGxmlElement(mt).getTextValueWithInternalTextMark();
                }
                else
                {
                    mt = MTHelper.getGxmlElement(mt).getTextValueWithoutInternalText();
                }
                data.setMt(mt);

                String target = rs.getString("target");
                if (isIncludeInternalText)
                {
                    target = MTHelper.getGxmlElement(target).getTextValueWithInternalTextMark();
                }
                else
                {
                    target = MTHelper.getGxmlElement(target).getTextValueWithoutInternalText();
                }
                data.setTarget(target);

                int levenshteinDistance = StringUtils.getLevenshteinDistance(mt, target);
                data.setLevenshteinDistance(levenshteinDistance);

                String mtName = rs.getString("mt_name");
                data.setMtEngineName(mtName);

                String state = rs.getString("state");
                data.setState(state);

                detailedDatas.add(data);
            }
        }
        catch (Exception e)
        {
            throw e;
        }
        finally
        {
            DbUtil.silentClose(rs);
            DbUtil.silentClose(ps);
            DbUtil.silentReturnConnection(conn);
        }

        return detailedDatas;
    }

    private void addDetailedDatas(Workbook p_workBook, Sheet p_sheet,
            List<DetailedData> detailedDatas, IntHolder row, GlobalSightLocale sourceLocale,
            GlobalSightLocale targetLocale) throws Exception
    {
        LinkedHashMap<String, ArrayList<String>> hypsegs = new LinkedHashMap<String, ArrayList<String>>();
        LinkedHashMap<String, ArrayList<String>> refsegs = new LinkedHashMap<String, ArrayList<String>>();
        ArrayList<String> hyps = new ArrayList<String>();
        ArrayList<String> refs = new ArrayList<String>();

        boolean rtlSourceLocale = EditUtil.isRTLLocale(sourceLocale.toString());
        boolean rtlTargetLocale = EditUtil.isRTLLocale(targetLocale.toString());
        for (DetailedData data : detailedDatas)
        {
            int col = 0;
            Row curRow = getRow(p_sheet, row.value);

            Cell cell_A = getCell(curRow, col);
            cell_A.setCellValue(data.getJobId());
            cell_A.setCellStyle(getContentStyle(p_workBook));
            col++;

            Cell cell_B = getCell(curRow, col);
            cell_B.setCellValue(data.getJobName());
            cell_B.setCellStyle(getContentStyle(p_workBook));
            col++;

            Cell cell_C = getCell(curRow, col);
            cell_C.setCellValue(data.getTuId());
            cell_C.setCellStyle(getContentStyle(p_workBook));
            col++;

            Cell cell_D = getCell(curRow, col);
            cell_D.setCellValue(
                    Double.valueOf(this.get2DigitFormater().format(data.getPostEditDistance())));
            cell_D.setCellStyle(getContentStyle(p_workBook));
            col++;

            hyps.clear();
            refs.clear();
            hypsegs.clear();
            refsegs.clear();
            Cell cell_E = getCell(curRow, col);
            hyps.add(data.getMt());
            hypsegs.put(String.valueOf(data.getTuId()), hyps);
            refs.add(data.getTarget());
            refsegs.put(String.valueOf(data.getTuId()), refs);
            cell_E.setCellValue(calculateTER(hypsegs, refsegs));
            cell_E.setCellStyle(getContentStyle(p_workBook));
            col++;

            Cell cell_F = getCell(curRow, col);
            String source = data.getSource();
            setCellForInternalText(cell_F, source, rtlSourceLocale);
            CellStyle srcStyle = rtlSourceLocale ? getRtlContentStyle(p_workBook)
                    : getContentStyle(p_workBook);
            cell_F.setCellStyle(srcStyle);
            col++;

            Cell cell_G = getCell(curRow, col);
            String mt = data.getMt();
            setCellForInternalText(cell_G, mt, rtlSourceLocale);
            CellStyle mtStyle = rtlTargetLocale ? getRtlContentStyle(p_workBook)
                    : getContentStyle(p_workBook);
            cell_G.setCellStyle(mtStyle);
            col++;

            Cell cell_H = getCell(curRow, col);
            String target = data.getTarget();
            setCellForInternalText(cell_H, target, rtlSourceLocale);
            CellStyle targetStyle = rtlTargetLocale ? getRtlContentStyle(p_workBook)
                    : getContentStyle(p_workBook);
            cell_H.setCellStyle(targetStyle);
            col++;

            // For GBS-4495 perplexity score on MT
            if (usePerplexity)
            {
                TuvImpl tuv1 = SegmentTuvUtil.getTuvById(data.getTuvId(), data.getJobId());
                TuvPerplexity perplexity = tuv1.loadPerplexity();

                Cell cell_I = getCell(curRow, col);
                String sourcePreplexity = Double.toString(perplexity.getPerplexitySource());
                cell_I.setCellValue(rtlTargetLocale ? EditUtil.toRtlString(sourcePreplexity)
                        : sourcePreplexity);
                CellStyle sourcePreplexityStyle = rtlTargetLocale ? getRtlContentStyle(p_workBook)
                        : getContentStyle(p_workBook);
                cell_I.setCellStyle(sourcePreplexityStyle);
                col++;

                Cell cell_J = getCell(curRow, col);
                String targetPreplexity = Double.toString(perplexity.getPerplexityTarget());
                cell_J.setCellValue(rtlTargetLocale ? EditUtil.toRtlString(targetPreplexity)
                        : targetPreplexity);
                CellStyle targetPreplexityStyle = rtlTargetLocale ? getRtlContentStyle(p_workBook)
                        : getContentStyle(p_workBook);
                cell_J.setCellStyle(targetPreplexityStyle);
                col++;

                Cell cell_K = getCell(curRow, col);
                String passFail = perplexity.getPerplexityResult() ? m_bundle.getString("lb_pass")
                        : m_bundle.getString("lb_fail");
                cell_K.setCellValue(rtlTargetLocale ? EditUtil.toRtlString(passFail) : passFail);
                CellStyle passFailStyle = rtlTargetLocale ? getRtlContentStyle(p_workBook)
                        : getContentStyle(p_workBook);
                cell_K.setCellStyle(passFailStyle);
                col++;
            }

            row.inc();
        }
    }

    /**
     * Sets the cell value for internal text.
     * 
     * @since GBS-4663
     */
    private void setCellForInternalText(Cell p_cell, String content, boolean rtlLocale)
    {
        if (content.indexOf(GxmlElement.GS_INTERNAL_BPT) != -1)
        {
            String contentWithoutMark = StringUtil.replace(content, GxmlElement.GS_INTERNAL_BPT,
                    "");
            contentWithoutMark = StringUtil.replace(contentWithoutMark, GxmlElement.GS_INTERNAL_EPT,
                    "");
            contentWithoutMark = rtlLocale ? EditUtil.toRtlString(contentWithoutMark)
                    : contentWithoutMark;
            XSSFRichTextString ts = new XSSFRichTextString(contentWithoutMark);
            while (content.indexOf(GxmlElement.GS_INTERNAL_BPT) != -1)
            {
                int internalBpt = content.indexOf(GxmlElement.GS_INTERNAL_BPT);
                content = content.substring(0, internalBpt)
                        + content.substring(internalBpt + GxmlElement.GS_INTERNAL_BPT.length());
                int internalEpt = content.indexOf(GxmlElement.GS_INTERNAL_EPT);
                content = content.substring(0, internalEpt)
                        + content.substring(internalEpt + GxmlElement.GS_INTERNAL_EPT.length());

                ts.applyFont(rtlLocale ? internalBpt + 1 : internalBpt,
                        rtlLocale ? internalEpt + 1 : internalEpt, m_style.getInternalFont());
                ts.applyFont(rtlLocale ? internalEpt + 1 : internalEpt, contentWithoutMark.length(),
                        m_style.getContentFont());
            }
            p_cell.setCellValue(ts);
        }
        else
        {
            p_cell.setCellValue(rtlLocale ? EditUtil.toRtlString(content) : content);
        }
    }

    /**
     * Gets the jobs and outputs workflow information.
     * 
     * @exception Exception
     */
    private void addJobsForSummary(Workbook p_workbook, Sheet p_sheet, List<Job> p_jobs,
            List<GlobalSightLocale> p_targetLocales, Map<String, List<DetailedData>> dataMap)
            throws Exception
    {
        // If no MTed data in all jobs
        if (dataMap == null || dataMap.isEmpty())
        {
            Row thirdRow = getRow(p_sheet, 3);
            Cell cell_A = getCell(thirdRow, 0);
            cell_A.setCellValue(m_bundle.getString("lb_no_mt_data"));
            CellStyle style = getContentStyle(p_workbook);
            style.setWrapText(false);
            cell_A.setCellStyle(style);
            return;
        }

        IntHolder row = new IntHolder(3);
        int finishedJobNum = 0;
        for (Job job : p_jobs)
        {
            // Cancel generate reports.
            if (isCancelled())
            {
                p_workbook = null;
                printMsg("Cancelled Report Generator. " + job);
                return;
            }

            // Sets Reports Percent.
            setPercent(++finishedJobNum);

            for (Workflow wf : job.getWorkflows())
            {
                String key = getKey(job.getJobId(), wf.getTargetLocale().getId());
                List<DetailedData> detailedData = dataMap.get(key);
                if (detailedData == null || detailedData.size() == 0)
                {
                    continue;
                }

                String state = wf.getState();
                if (Workflow.DISPATCHED.equals(state) || Workflow.EXPORTED.equals(state)
                        || Workflow.EXPORT_FAILED.equals(state) || Workflow.ARCHIVED.equals(state)
                        || Workflow.LOCALIZED.equals(state))
                {
                    addWorkflowSummary(p_workbook, p_sheet, job, wf, detailedData, row);
                }
            }
            printMsg("Finished Job: " + job);
        }
    }

    /**
     * Gets the task for the workflow and outputs page information.
     * 
     * @exception Exception
     */
    private void addWorkflowSummary(Workbook workBook, Sheet sheet, Job job, Workflow workflow,
            List<DetailedData> detailedData, IntHolder row) throws Exception
    {
        int col = 0;
        Row curRow = getRow(sheet, row.value);

        LinkedHashMap<String, ArrayList<String>> hypsegs = new LinkedHashMap<String, ArrayList<String>>();
        LinkedHashMap<String, ArrayList<String>> refsegs = new LinkedHashMap<String, ArrayList<String>>();

        // Company
        Cell cell_A = getCell(curRow, col);
        cell_A.setCellValue(getCompanyName(job.getCompanyId()));
        cell_A.setCellStyle(getContentStyle(workBook));
        col++;

        // Job ID
        Cell cell_B = getCell(curRow, col);
        cell_B.setCellValue(job.getJobId());
        cell_B.setCellStyle(getContentStyle(workBook));
        col++;

        // Job Name
        Cell cell_C = getCell(curRow, col);
        cell_C.setCellValue(job.getJobName());
        cell_C.setCellStyle(getContentStyle(workBook));
        col++;

        // Language
        Cell cell_D = getCell(curRow, col);
        cell_D.setCellValue(workflow.getTargetLocale().toString());
        cell_D.setCellStyle(getContentStyle(workBook));
        col++;

        // Workflow State
        Cell cell_E = getCell(curRow, col);
        cell_E.setCellValue(workflow.getState());
        cell_E.setCellStyle(getContentStyle(workBook));
        col++;

        // MT Word Count
        Cell cell_F = getCell(curRow, col);
        cell_F.setCellValue(workflow.getMtTotalWordCount());
        cell_F.setCellStyle(getContentStyle(workBook));
        col++;

        // Perplexity passed word count
        if (usePerplexity)
        {
            Cell cell = getCell(curRow, col);
            cell.setCellValue(workflow.getPerplexityWordCount());
            cell.setCellStyle(getContentStyle(workBook));
            col++;
        }

        // Total Word Count
        Cell cell_G = getCell(curRow, col);
        cell_G.setCellValue(workflow.getTotalWordCount());
        cell_G.setCellStyle(getContentStyle(workBook));
        col++;

        // Average Edit Distance/char
        int totalLevDistance = 0;
        int totalTargetLength = 0;
        String mtEngineName = null;
        for (DetailedData data : detailedData)
        {
            totalLevDistance += data.getLevenshteinDistance();
            totalTargetLength += data.getTarget().length();
            if (mtEngineName == null)
            {
                mtEngineName = data.getMtEngineName();
                if (mtEngineName != null && mtEngineName.toLowerCase().endsWith("_mt"))
                {
                    mtEngineName = mtEngineName.substring(0, mtEngineName.length() - 3);
                }
            }

            ArrayList<String> hyps = new ArrayList<String>();
            hyps.add(data.getMt());
            hypsegs.put(String.valueOf(data.getTuId()), hyps);

            ArrayList<String> refs = new ArrayList<String>();
            refs.add(data.getTarget());
            refsegs.put(String.valueOf(data.getTuId()), refs);
        }
        float averageEditDistance = ((float) totalLevDistance / (float) totalTargetLength * 100);
        Cell cell_H = getCell(curRow, col);
        cell_H.setCellValue(Double.valueOf(this.get2DigitFormater().format(averageEditDistance)));
        cell_H.setCellStyle(getContentStyle(workBook));
        col++;

        // Translation Error Rate (TER)
        Cell cell_I = getCell(curRow, col);
        cell_I.setCellValue(calculateTER(hypsegs, refsegs));
        cell_I.setCellStyle(getContentStyle(workBook));
        col++;

        // Engine Name
        Cell cell_J = getCell(curRow, col);
        cell_J.setCellValue(mtEngineName);
        cell_J.setCellStyle(getContentStyle(workBook));
        col++;

        row.inc();
    }

    @Override
    public String getReportType()
    {
        return ReportConstants.MT_POST_EDIT_DISTANCE_REPORT;
    }

    private void setProjectIdList(HttpServletRequest p_request)
    {
        data.projectIdList.clear();
        data.wantsAllProjects = false;
        // set the project Id
        String[] projectIds = p_request.getParameterValues("projectNameList");
        if (projectIds != null)
        {
            for (int i = 0; i < projectIds.length; i++)
            {
                String id = projectIds[i];
                if (id.equals("*"))
                {
                    data.wantsAllProjects = true;
                    break;
                }
                else
                {
                    data.projectIdList.add(new Long(id));
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void setTargetLocales(HttpServletRequest p_request)
            throws LocaleManagerException, RemoteException, GeneralException
    {
        data.trgLocaleList.clear();
        data.wantsAllLocales = false;
        String[] paramTrgLocales = p_request.getParameterValues(ReportConstants.TARGETLOCALE_LIST);
        if (paramTrgLocales != null)
        {
            for (int i = 0; i < paramTrgLocales.length; i++)
            {
                if ("*".equals(paramTrgLocales[i]))
                {
                    data.wantsAllLocales = true;
                    data.trgLocaleList = new ArrayList<GlobalSightLocale>(
                            ServerProxy.getLocaleManager().getAllTargetLocales());
                    break;
                }
                else
                {
                    long localeId = Long.parseLong(paramTrgLocales[i]);
                    GlobalSightLocale locale = null;
                    try
                    {
                        locale = ServerProxy.getLocaleManager().getLocaleById(localeId);
                    }
                    catch (Exception e)
                    {
                    }

                    if (locale != null && !data.trgLocaleList.contains(locale))
                    {
                        data.trgLocaleList.add(locale);
                    }
                }
            }
        }
    }

    /**
     * Set job and workflow status
     * 
     * @param p_request
     */
    private void setStates(HttpServletRequest p_request)
    {
        String reportOn = (String) p_request.getParameter("reportOn");
        data.reportOnJobIds = "jobIds".equals(reportOn);

        if (data.reportOnJobIds)
        {
            // Report on Job IDs
            data.jobIds.clear();
            String jobIdsStr = p_request.getParameter("jobIds");
            if (jobIdsStr != null && jobIdsStr.length() != 0)
            {
                String[] jobIds = jobIdsStr.split(",");
                long jobId = 0;
                for (int i = 0; i < jobIds.length; i++)
                {
                    try
                    {
                        jobId = Long.valueOf(jobIds[i].trim());
                    }
                    catch (Exception e)
                    {
                        continue;
                    }
                    if (!data.jobIds.contains(jobId))
                    {
                        data.jobIds.add(jobId);
                    }
                }
            }
            data.needSearchJob = false;
        }
        else
        {
            String[] jobIds = p_request.getParameterValues("jobNameList");
            data.needSearchJob = (jobIds == null);
            if (!data.needSearchJob)
            {
                data.jobIds.clear();
                long jobId = 0;
                for (int i = 0; i < jobIds.length; i++)
                {
                    jobId = Long.valueOf(jobIds[i]);
                    if (!data.jobIds.contains(jobId))
                    {
                        data.jobIds.add(jobId);
                    }
                }
            }
            else
            {
                data.jobStateList.clear();
                data.workflowStateList.clear();
                String[] states = p_request.getParameterValues("jobStatus");
                List<String> lst = new ArrayList<String>();
                if (states != null)
                {
                    lst = Arrays.asList(states);
                }
                if (lst.contains("*"))
                {
                    data.jobStateList.add(Job.DISPATCHED);
                    data.workflowStateList.add(Workflow.DISPATCHED);
                    data.jobStateList.add(Job.LOCALIZED);
                    data.workflowStateList.add(Workflow.LOCALIZED);
                    data.jobStateList.add(Job.EXPORTED);
                    data.jobStateList.add(Job.EXPORT_FAIL);
                    data.workflowStateList.add(Workflow.EXPORTED);
                    data.workflowStateList.add(Workflow.EXPORT_FAILED);
                    data.jobStateList.add(Job.ARCHIVED);
                    data.workflowStateList.add(Workflow.ARCHIVED);
                }
                else
                {
                    if (lst.contains(Job.DISPATCHED))
                    {
                        data.jobStateList.add(Job.DISPATCHED);
                        data.workflowStateList.add(Workflow.DISPATCHED);
                    }
                    if (lst.contains(Job.LOCALIZED))
                    {
                        data.jobStateList.add(Job.LOCALIZED);
                        data.workflowStateList.add(Workflow.LOCALIZED);
                    }
                    if (lst.contains(Job.EXPORTED))
                    {
                        data.jobStateList.add(Job.EXPORTED);
                        data.workflowStateList.add(Workflow.EXPORTED);
                    }
                    if (lst.contains(Job.EXPORT_FAIL))
                    {
                        data.jobStateList.add(Job.EXPORT_FAIL);
                        data.workflowStateList.add(Workflow.EXPORT_FAILED);
                    }
                    if (lst.contains(Job.ARCHIVED))
                    {
                        data.jobStateList.add(Job.ARCHIVED);
                        data.workflowStateList.add(Workflow.ARCHIVED);
                    }
                }
            }
        }
    }

    private String getCompanyName(long companyId) throws Exception
    {
        return ServerProxy.getJobHandler().getCompanyById(companyId).getCompanyName();
    }

    /**
     * Calculate to return the average translation error rate.
     */
    private Double calculateTER(LinkedHashMap<String, ArrayList<String>> hypsegs,
            LinkedHashMap<String, ArrayList<String>> refsegs)
    {
        // in below args, only the "-N" is useful. The rest is fake parameters.
        String ref = "ref.txt";
        String hyp = "hyp.txt";
        String[] args = new String[]
        { "-r", ref, "-h", hyp, "-o", "sum", "-n", "outputFileName", "-N" };
        boolean writeToFile = true;
        double totalTer = new TERtest().calculateTER(args, hypsegs, refsegs, writeToFile);
        return Double.valueOf(this.get2DigitFormater().format(totalTer * 100));
    }

    /**
     * Ensure jobs belong to current user's projects.
     */
    private void filterInvalidJobs(List<Job> jobs)
    {
        try
        {
            ArrayList<Project> projectList = (ArrayList<Project>) ServerProxy.getProjectHandler()
                    .getProjectsByUser(m_userId);

            Set<Long> projectIds = new HashSet<Long>();
            if (projectList != null && projectList.size() > 0)
            {
                for (Project pro : projectList)
                {
                    projectIds.add(pro.getIdAsLong());
                }
            }

            for (Iterator<Job> it = jobs.iterator(); it.hasNext();)
            {
                if (!projectIds.contains(it.next().getProjectId()))
                {
                    it.remove();
                }
            }
        }
        catch (Exception e)
        {
            logger.error(e);
        }
    }

    private void setAllCellStyleNull()
    {
        this.contentStyle = null;
        this.headerStyle = null;
        this.rtlContentStyle = null;
    }

    private CellStyle getHeaderStyle(Workbook p_workbook)
    {
        if (headerStyle == null)
        {
            Font headerFont = p_workbook.createFont();
            headerFont.setBoldweight(Font.BOLDWEIGHT_BOLD);
            headerFont.setColor(IndexedColors.BLACK.getIndex());
            headerFont.setUnderline(Font.U_NONE);
            headerFont.setFontName("Arial");
            headerFont.setFontHeightInPoints((short) 9);

            CellStyle cs = p_workbook.createCellStyle();
            cs.setFont(headerFont);
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

    public void setRegionStyle(Sheet sheet, CellRangeAddress cellRangeAddress, CellStyle cs)
    {
        for (int i = cellRangeAddress.getFirstRow(); i <= cellRangeAddress.getLastRow(); i++)
        {
            Row row = getRow(sheet, i);
            for (int j = cellRangeAddress.getFirstColumn(); j <= cellRangeAddress
                    .getLastColumn(); j++)
            {
                Cell cell = getCell(row, j);
                cell.setCellStyle(cs);
            }
        }
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

    @Override
    public void setPercent(int p_finishedJobNum)
    {
        ReportGeneratorHandler.setReportsMapByGenerator(m_userId, data.jobIds,
                100 * p_finishedJobNum / data.jobIds.size(), getReportType());
    }

    @Override
    public boolean isCancelled()
    {
        return ReportGeneratorHandler.isCancelled(m_userId, null, getReportType());
    }

    private void printMsg(String p_msg)
    {

    }

    private NumberFormat get2DigitFormater()
    {
        if (this.twoDigitFormater == null)
        {
            twoDigitFormater = DecimalFormat.getInstance();
            twoDigitFormater.setMaximumFractionDigits(2);
            twoDigitFormater.setMinimumFractionDigits(2);
            twoDigitFormater.setGroupingUsed(false);
        }
        return this.twoDigitFormater;
    }

    private NumberFormat get3DigitFormater()
    {
        if (this.threeDigitFormater == null)
        {
            threeDigitFormater = DecimalFormat.getInstance();
            threeDigitFormater.setMaximumFractionDigits(3);
            threeDigitFormater.setMinimumFractionDigits(3);
            threeDigitFormater.setGroupingUsed(false);
        }
        return this.threeDigitFormater;
    }

    private class DetailedData
    {
        long jobId = 0;
        String jobName = null;
        long tuId = 0;
        private long tuvId = 0;
        String source, mt, target;
        private int levenshteinDistance = 0;
        private String mtEngineName = null;
        private String state = null;

        public long getJobId()
        {
            return jobId;
        }

        public void setJobId(long jobId)
        {
            this.jobId = jobId;
        }

        public String getJobName()
        {
            return jobName;
        }

        public void setJobName(String jobName)
        {
            this.jobName = jobName;
        }

        public long getTuId()
        {
            return tuId;
        }

        public void setTuId(long tuId)
        {
            this.tuId = tuId;
        }

        public long getTuvId()
        {
            return tuvId;
        }

        public void setTuvId(long tuvId)
        {
            this.tuvId = tuvId;
        }

        public String getSource()
        {
            return this.source;
        }

        public void setSource(String source)
        {
            this.source = source;
        }

        public String getMt()
        {
            return this.mt;
        }

        public void setMt(String mt)
        {
            this.mt = mt;
        }

        public String getTarget()
        {
            return this.target;
        }

        public void setTarget(String target)
        {
            this.target = target;
        }

        public int getLevenshteinDistance()
        {
            return levenshteinDistance;
        }

        public void setLevenshteinDistance(int levenshteinDistance)
        {
            this.levenshteinDistance = levenshteinDistance;
        }

        public void setState(String state)
        {
            this.state = state;
        }

        public String getState()
        {
            return this.state;
        }

        // Utility
        public float getPostEditDistance()
        {
            return ((float) levenshteinDistance / (float) target.length()) * 100;
        }

        public String getMtEngineName()
        {
            return mtEngineName;
        }

        public void setMtEngineName(String mtEngineName)
        {
            this.mtEngineName = mtEngineName;
        }
    };
}