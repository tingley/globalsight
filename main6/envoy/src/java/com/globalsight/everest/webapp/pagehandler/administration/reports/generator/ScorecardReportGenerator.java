/**
 * Copyright 2009 Welocalize, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.
 * 
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 * 
 */
package com.globalsight.everest.webapp.pagehandler.administration.reports.generator;

import java.io.File;
import java.io.FileOutputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import com.globalsight.everest.category.CategoryType;
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.util.comparator.GlobalSightLocaleComparator;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.pagehandler.administration.reports.ReportConstants;
import com.globalsight.everest.webapp.pagehandler.administration.reports.ReportHelper;
import com.globalsight.everest.webapp.pagehandler.administration.reports.bo.ReportsData;
import com.globalsight.everest.workflow.ScorecardScore;
import com.globalsight.everest.workflow.ScorecardScoreHelper;
import com.globalsight.everest.workflowmanager.Workflow;
import com.globalsight.everest.workflowmanager.WorkflowImpl;
import com.globalsight.util.ExcelUtil;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.ReportStyle;
import com.globalsight.util.edit.EditUtil;

/**
 * Comments Analysis Report Generator Include Comments Analysis Report in popup
 * editor
 */
public class ScorecardReportGenerator implements ReportGenerator
{
    private static final Logger logger = Logger.getLogger(ScorecardReportGenerator.class);
    private ReportStyle REPORT_STYLE = null;

    protected long companyId = -1L;
    protected List<Long> defaultJobIds = new ArrayList<Long>();
    protected List<GlobalSightLocale> defaultTargetLocales = new ArrayList<GlobalSightLocale>();
    protected List<String> scorecardCategories = new ArrayList<String>();

    public final String REPORT_TYPE = ReportConstants.SCORECARD_REPORT;

    public final int SEGMENT_HEADER_ROW = 2;
    public final int SEGMENT_START_ROW = 3;
    public final int CATEGORY_FAILURE_COLUMN = 8;

    private final String SCORECARD_SHEET_NAME = "Scorecard";
    private final String STATISTICS_SHEET_NAME = "Statistics";

    private int targetLocaleCount = 0;
    private int scorecardCategoryCount = 0;
    private ResourceBundle bundle = null;
    private Locale uiLocale = new Locale("en", "US");

    private String userId;
    private HashMap<String, Integer> scorecardColumnsInScorecard = null;
    private HashMap<String, Integer> scorecardColumnsInStatistics = null;
    private HashMap<String, LocaleScore> localeScores = new HashMap<String, LocaleScore>();

    private DecimalFormat numFormat = new DecimalFormat("#.00");

    /**
     * Comments Analysis Report Generator Constructor.
     * 
     * @param request
     *            the request
     * @param response
     *            the response
     * @throws Exception
     */
    public ScorecardReportGenerator(HttpServletRequest request, HttpServletResponse response)
            throws Exception
    {
        HttpSession session = request.getSession();
        bundle = PageHandler.getBundle(session);

        userId = (String) session.getAttribute(WebAppConstants.USER_NAME);
        companyId = CompanyWrapper.getCurrentCompanyIdAsLong();
        uiLocale = (Locale) session.getAttribute(WebAppConstants.UILOCALE);
        if (uiLocale == null)
        {
            uiLocale = Locale.US;
        }

        if (request.getParameter(ReportConstants.JOB_IDS) != null)
        {
            defaultJobIds = ReportHelper.getListOfLong(request
                    .getParameter(ReportConstants.JOB_IDS));
            GlobalSightLocaleComparator comparator = new GlobalSightLocaleComparator(
                    GlobalSightLocaleComparator.ISO_CODE, uiLocale);
            defaultTargetLocales = ReportHelper.getTargetLocaleList(
                    request.getParameterValues(ReportConstants.TARGETLOCALE_LIST), comparator);
        }
        scorecardCategories = CompanyWrapper.getCompanyCategoryNames(bundle,
                String.valueOf(companyId), CategoryType.ScoreCard, true);
        scorecardCategoryCount = scorecardCategories.size();
        scorecardColumnsInScorecard = new HashMap<String, Integer>(scorecardCategories.size());
        scorecardColumnsInStatistics = new HashMap<String, Integer>(scorecardCategories.size());
    }

    /**
     * Send Report Files to Client
     * 
     * @param jobIds
     *            Job ID List
     * @param targetLocales
     *            Target Locales List
     * @param p_companyName
     *            Company Name
     * @param response
     *            HttpServletResponse
     * @throws Exception
     */
    public void sendReports(List<Long> jobIds, List<GlobalSightLocale> targetLocales,
            HttpServletResponse response) throws Exception
    {
        jobIds = (jobIds == null || jobIds.size() == 0) ? defaultJobIds : jobIds;
        targetLocales = (targetLocales == null || targetLocales.size() == 0) ? defaultTargetLocales
                : targetLocales;

        File[] reports = generateReports(jobIds, targetLocales);
        ReportHelper.sendFiles(reports, getReportType(), response);
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
    public File[] generateReports(List<Long> jobIds, List<GlobalSightLocale> targetLocales)
            throws Exception
    {
        ArrayList<String> stateList = ReportHelper.getAllJobStatusList();
        stateList.remove(Job.PENDING);

        List<File> workbooks = new ArrayList<File>();
        Workbook workBook = new SXSSFWorkbook();
        REPORT_STYLE = new ReportStyle(workBook);

        createReport(workBook, jobIds, targetLocales, stateList);

        File file = getFile(REPORT_TYPE, null, workBook);
        FileOutputStream out = new FileOutputStream(file);
        workBook.write(out);
        out.close();
        ((SXSSFWorkbook) workBook).dispose();
        workbooks.add(file);

        return ReportHelper.moveReports(workbooks, userId);
    }

    /**
     * Create the report sheets
     * 
     * @throws Exception
     */
    private void createReport(Workbook workbook, List<Long> jobIds,
            List<GlobalSightLocale> targetLocales, ArrayList<String> stateList) throws Exception
    {
        Sheet scorecardSheet = workbook.createSheet(SCORECARD_SHEET_NAME);
        Sheet statisticsSheet = workbook.createSheet(STATISTICS_SHEET_NAME);

        // Add Title
        addTitle(scorecardSheet);

        // Add Segment Header
        addScorecardHeader(scorecardSheet);
        addStatisticsHeader(statisticsSheet);

        List<GlobalSightLocale> sortLocales = sortLocales(targetLocales);

        writeSegmentInfo(workbook, jobIds, sortLocales, stateList);

        writeStatisticsAverage(workbook, targetLocaleCount);
    }

    private void writeStatisticsAverage(Workbook workbook, int count)
    {
        int row = 1;
        int col = 0;
        Sheet statisticsSheet = workbook.getSheet(STATISTICS_SHEET_NAME);
        Cell cell = null;
        CellStyle contentStyle = REPORT_STYLE.getContentStyle();
        if (localeScores != null && localeScores.size() > 0)
        {
            List<Map.Entry<String, LocaleScore>> list = new ArrayList<Map.Entry<String, LocaleScore>>(
                    localeScores.entrySet());
            Collections.sort(list, new Comparator<Map.Entry<String, LocaleScore>>()
            {
                @Override
                public int compare(Entry<String, LocaleScore> o1, Entry<String, LocaleScore> o2)
                {
                    return o1.getKey().compareTo(o2.getKey());
                }
            });

            // write score of target locale into Statistics sheet
            String targetLocale, category;
            double score = 0.00d;
            int column = 0;
            LocaleScore localeScore;
            HashMap<String, Double> scorecardData;
            for (Map.Entry<String, LocaleScore> maps : list)
            {
                targetLocale = maps.getKey();
                localeScore = localeScores.get(targetLocale);

                cell = ExcelUtil.getCell(statisticsSheet, row, col);
                cell.setCellStyle(contentStyle);
                cell.setCellValue(localeScore.getTargetLocale());

                scorecardData = localeScore.getScorecardScore();
                for (Iterator<String> it = scorecardData.keySet().iterator(); it.hasNext();)
                {
                    category = it.next();
                    score = Double.parseDouble(numFormat.format(scorecardData.get(category)));
                    column = scorecardColumnsInStatistics.get(category);

                    cell = ExcelUtil.getCell(statisticsSheet, row, column);
                    cell.setCellStyle(getScoreCellStyle(score));
                    cell.setCellValue(score);
                }

                cell = ExcelUtil.getCell(statisticsSheet, row, col + scorecardCategoryCount + 1);
                score = Double.parseDouble(numFormat.format(localeScore.getAvgScore()));
                cell.setCellStyle(getScoreCellStyle(score));
                cell.setCellValue(score);

                row++;
            }
        }

        Row rowLine = ExcelUtil.getRow(statisticsSheet, row);
        cell = ExcelUtil.getCell(rowLine, col);
        cell.setCellStyle(REPORT_STYLE.getContentStyle());
        cell.setCellValue("AVG Score");
        col++;

        for (int i = 0; i <= scorecardCategoryCount; i++)
        {
            double avgScore = getAvgScore(statisticsSheet, row, col + i);
            cell = ExcelUtil.getCell(rowLine, col + i);
            cell.setCellStyle(getScoreCellStyle(avgScore));
            cell.setCellValue(avgScore);
        }
    }

    private double getAvgScore(Sheet sheet, int lastRowNumber, int col)
    {
        // Becuase the header line is used 0 line. So if lastRowNumber is 1, it means that
        // there is no data in Statistics sheet.
        if (lastRowNumber <= 1)
            return 0.00d;

        double score, totalScore = 0.00d;
        String tmp = null;

        for (int i = 1; i < lastRowNumber; i++)
        {
            tmp = ExcelUtil.getCellValue(sheet, i, col, true);
            score = tmp == null || tmp.equals("") ? 0.00d : Double.parseDouble(tmp);
            totalScore += score;
        }

        int count = lastRowNumber - 1;
        score = totalScore / count;
        return Double.parseDouble(numFormat.format(score));
    }

    private List<GlobalSightLocale> sortLocales(List<GlobalSightLocale> targetLocales)
    {
        List<GlobalSightLocale> sortLocales = new ArrayList<GlobalSightLocale>();
        if (targetLocales == null || targetLocales.size() == 0)
            return sortLocales;

        HashMap<String, GlobalSightLocale> tmpLocales = new HashMap<String, GlobalSightLocale>(
                targetLocales.size());
        Set<String> localeStringSet = new TreeSet<String>();
        String tmp = null;
        for (GlobalSightLocale locale : targetLocales)
        {
            tmp = locale.toString();
            tmpLocales.put(tmp, locale);
            localeStringSet.add(tmp);
        }

        Object[] locales = localeStringSet.toArray();
        for (Object locale : locales)
        {
            sortLocales.add(tmpLocales.get((String) locale));
        }

        return sortLocales;
    }

    /**
     * Add segment header to the sheet for Comments Analysis Report
     * 
     * @param sheet
     *            the sheet
     * @throws Exception
     */
    private void addScorecardHeader(Sheet sheet) throws Exception
    {
        int col = 0;
        int row = SEGMENT_HEADER_ROW;
        Row segHeaderRow = ExcelUtil.getRow(sheet, row);
        CellStyle headerStyle = REPORT_STYLE.getHeaderStyle();

        Cell cell = ExcelUtil.getCell(segHeaderRow, col);
        cell.setCellValue(bundle.getString("lb_job_id"));
        cell.setCellStyle(headerStyle);
        sheet.setColumnWidth(col, 10 * 256);
        col++;

        cell = ExcelUtil.getCell(segHeaderRow, col);
        cell.setCellValue(bundle.getString("lb_job_name"));
        cell.setCellStyle(headerStyle);
        sheet.setColumnWidth(col, 30 * 256);
        col++;

        cell = ExcelUtil.getCell(segHeaderRow, col);
        cell.setCellValue(bundle.getString("lb_target_locale"));
        cell.setCellStyle(headerStyle);
        sheet.setColumnWidth(col, 14 * 256);
        col++;

        for (String scorecard : scorecardCategories)
        {
            cell = ExcelUtil.getCell(segHeaderRow, col);
            cell.setCellValue(scorecard);
            cell.setCellStyle(headerStyle);
            sheet.setColumnWidth(col, 21 * 256);

            scorecardColumnsInScorecard.put(scorecard, col);

            col++;
        }

        cell = ExcelUtil.getCell(segHeaderRow, col);
        cell.setCellValue(bundle.getString("lb_avg_score"));
        cell.setCellStyle(headerStyle);
        sheet.setColumnWidth(col, 13 * 256);
        col++;

        cell = ExcelUtil.getCell(segHeaderRow, col);
        cell.setCellValue("Overall");
        cell.setCellStyle(headerStyle);
        sheet.setColumnWidth(col, 10 * 256);
        col++;

        cell = ExcelUtil.getCell(segHeaderRow, col);
        cell.setCellValue(bundle.getString("lb_comments"));
        cell.setCellStyle(headerStyle);
        sheet.setColumnWidth(col, 40 * 256);
        col++;

        cell = ExcelUtil.getCell(segHeaderRow, col);
        cell.setCellValue(bundle.getString("lb_dqf_fluency"));
        cell.setCellStyle(headerStyle);
        sheet.setColumnWidth(col, 15 * 256);
        col++;

        cell = ExcelUtil.getCell(segHeaderRow, col);
        cell.setCellValue(bundle.getString("lb_dqf_adequacy"));
        cell.setCellStyle(headerStyle);
        sheet.setColumnWidth(col, 15 * 256);
        col++;

        cell = ExcelUtil.getCell(segHeaderRow, col);
        cell.setCellValue(bundle.getString("lb_dqf_comments"));
        cell.setCellStyle(headerStyle);
        sheet.setColumnWidth(col, 40 * 256);
    }

    private void addStatisticsHeader(Sheet sheet) throws Exception
    {
        int col = 0;
        int row = 0;
        Row segHeaderRow = ExcelUtil.getRow(sheet, row);
        CellStyle headerStyle = REPORT_STYLE.getHeaderStyle();

        Cell cell = ExcelUtil.getCell(segHeaderRow, col);
        cell.setCellValue(bundle.getString("lb_target_locale"));
        cell.setCellStyle(headerStyle);
        sheet.setColumnWidth(col, 14 * 256);
        col++;

        for (String scorecard : scorecardCategories)
        {
            cell = ExcelUtil.getCell(segHeaderRow, col);
            cell.setCellValue(scorecard);
            cell.setCellStyle(headerStyle);
            sheet.setColumnWidth(col, 21 * 256);

            scorecardColumnsInStatistics.put(scorecard, col);

            col++;
        }

        cell = ExcelUtil.getCell(segHeaderRow, col);
        cell.setCellValue(bundle.getString("lb_avg_score"));
        cell.setCellStyle(headerStyle);
        sheet.setColumnWidth(col, 13 * 256);
        col++;
    }

    /**
     * Add title to the sheet
     * 
     * @param sheet
     *            the sheet
     * @throws Exception
     */
    private void addTitle(Sheet sheet) throws Exception
    {
        Row titleRow = ExcelUtil.getRow(sheet, 0);
        Cell titleCell = ExcelUtil.getCell(titleRow, 0);
        titleCell.setCellValue(bundle.getString("scorecard_report"));
        titleCell.setCellStyle(REPORT_STYLE.getTitleStyle());
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
    private int writeSegmentInfo(Workbook workbook, List<Long> jobIds,
            List<GlobalSightLocale> targetLocales, ArrayList<String> stateList) throws Exception
    {
        int finishedJobNum = 0;
        int row = SEGMENT_START_ROW;
        targetLocaleCount = 0;
        int result = 0;
        for (long jobId : jobIds)
        {
            // Cancel generate reports.
            if (isCancelled())
                return -1;

            Job job = ServerProxy.getJobHandler().getJobById(jobId);
            if (job == null || !stateList.contains(job.getState()))
                continue;

            List<GlobalSightLocale> jobTargetLocales = ReportHelper.getTargetLocals(job);
            for (GlobalSightLocale targetLocale : targetLocales)
            {
                if (!jobTargetLocales.contains(targetLocale))
                    continue;

                for (Workflow workflow : job.getWorkflows())
                {
                    if (workflow.getTargetLocale().equals(targetLocale))
                    {
                        result = writeOneLocale(job, workflow, workbook, row);
                        row += result;
                        targetLocaleCount += result;
                    }
                }
            }

            // Sets Reports Percent.
            setPercent(++finishedJobNum);
        }

        return 0;
    }

    private int writeOneLocale(Job job, Workflow workflow, Workbook workbook, int row)
    {
        List<ScorecardScore> scoreList = ScorecardScoreHelper.getScoreByWrkflowId(workflow.getId());
        CellStyle contentStyle = REPORT_STYLE.getContentStyle();
        String targetLocale;
        if (scoreList != null && scoreList.size() > 0)
        {
            int col = 0;

            Sheet scorecardSheet = workbook.getSheet(SCORECARD_SHEET_NAME);

            Row currentRow = ExcelUtil.getRow(scorecardSheet, row);
            // Job id
            Cell cell = ExcelUtil.getCell(currentRow, col);
            cell.setCellValue(job.getId());
            cell.setCellStyle(contentStyle);
            col++;

            // job name
            cell = ExcelUtil.getCell(currentRow, col);
            cell.setCellValue(job.getJobName());
            cell.setCellStyle(contentStyle);
            col++;

            // TargetPage id
            targetLocale = workflow.getTargetLocale().toString();
            cell = ExcelUtil.getCell(currentRow, col);
            cell.setCellValue(targetLocale);
            cell.setCellStyle(contentStyle);
            col++;

            String category = null;
            Integer column = null;
            int totalScore = 0, score = 0;
            HashMap<String, Double> scoreData = new HashMap<String, Double>();
            for (ScorecardScore scorecardScore : scoreList)
            {
                // Write scorecard score into Scorecard sheet
                category = scorecardScore.getScorecardCategory();
                score = scorecardScore.getScore();
                score = score < 0 ? 0 : score;
                totalScore += score;
                scoreData.put(category, (double) score);
                column = scorecardColumnsInScorecard.get(category);
                if (column != null && column > 0)
                {
                    cell = ExcelUtil.getCell(currentRow, column);
                    cell.setCellStyle(getScoreCellStyle(score));
                    cell.setCellValue(score);
                }
            }

            col += scorecardCategoryCount;
            double avgScore = (double) totalScore / scorecardCategoryCount;
            avgScore = Double.parseDouble(numFormat.format(avgScore));

            LocaleScore localeScore = localeScores.get(targetLocale);
            if (localeScore == null)
            {
                localeScore = new LocaleScore();
                localeScore.setTargetLocale(targetLocale);
                localeScore.setScorecardScore(scoreData);
                localeScore.setAvgScore(avgScore);
                localeScores.put(targetLocale, localeScore);
            }
            else
            {
                localeScore.addScore(scoreList);
            }

            // write average score into Scorecard sheet
            cell = ExcelUtil.getCell(currentRow, col);
            cell.setCellStyle(getScoreCellStyle(avgScore));
            cell.setCellValue(avgScore);
            col++;

            // Overall
            cell = ExcelUtil.getCell(currentRow, col);
            if (avgScore > 0 && avgScore < 3)
            {
                cell.setCellValue("Negative");
            }
            else if (avgScore >= 3)
            {
                cell.setCellValue("Positive");
            }
            cell.setCellStyle(contentStyle);
            col++;

            // Comments
            boolean rtlTargetLocale = EditUtil.isRTLLocale(targetLocale);
            CellStyle commentStyle = rtlTargetLocale ? REPORT_STYLE.getUnlockedRightStyle()
                    : REPORT_STYLE.getUnlockedStyle();
            cell = ExcelUtil.getCell(currentRow, col);
            cell.setCellValue(((WorkflowImpl) workflow).getScorecardComment());
            cell.setCellStyle(commentStyle);
            col++;

            // DQF Fluency
            cell = ExcelUtil.getCell(currentRow, col);
            cell.setCellStyle(contentStyle);
            cell.setCellValue(workflow.getFluencyScore());
            col++;

            // DQF Adequacy
            cell = ExcelUtil.getCell(currentRow, col);
            cell.setCellStyle(contentStyle);
            cell.setCellValue(workflow.getAdequacyScore());
            col++;

            // DQF Comment
            cell = ExcelUtil.getCell(currentRow, col);
            cell.setCellStyle(commentStyle);
            cell.setCellValue(workflow.getDQFComment());
            col++;

            return 1;
        }
        else
            return 0;
    }

    private CellStyle getScoreCellStyle(double score)
    {
        if (score < 3)
        {
            return getUnder3Style();
        }
        else if (score < 4)
        {
            return getUnder4Style();
        }
        else if (score < 5)
        {
            return getUnder5Style();
        }
        else
        {
            return getEqual5Style();
        }
    }

    private CellStyle getEqual5Style()
    {
        return REPORT_STYLE.getColorStyle(IndexedColors.BRIGHT_GREEN.getIndex());
    }

    private CellStyle getUnder5Style()
    {
        return REPORT_STYLE.getColorStyle(IndexedColors.LIME.getIndex());
    }

    private CellStyle getUnder4Style()
    {
        return REPORT_STYLE.getColorStyle(IndexedColors.LIGHT_ORANGE.getIndex());
    }

    private CellStyle getUnder3Style()
    {
        return REPORT_STYLE.getColorStyle(IndexedColors.ROSE.getIndex());
    }

    @Override
    public String getReportType()
    {
        return REPORT_TYPE;
    }

    @Override
    public void setPercent(int finishedJobNum)
    {
        ReportGeneratorHandler.setReportsMapByGenerator(userId, defaultJobIds, 100 * finishedJobNum
                / defaultJobIds.size(), getReportType());
    }

    @Override
    public boolean isCancelled()
    {
        ReportsData data = ReportGeneratorHandler.getReportsMap(userId, defaultJobIds,
                getReportType());
        if (data != null)
            return data.isCancle();

        return false;
    }

    /**
     * Create Report File.
     */
    protected File getFile(String reportType, Job job, Workbook workbook)
    {
        String langInfo = null;
        // If the Workbook has only one sheet, the report name should contain
        // language pair info, such as en_US_de_DE.
        if (workbook != null && workbook.getNumberOfSheets() == 1)
        {
            Sheet sheet = workbook.getSheetAt(0);
            String srcLang = null, trgLang = null;
            if (job != null)
            {
                srcLang = job.getSourceLocale().toString();
            }
            if (srcLang == null)
            {
                Row languageInfoRow = sheet.getRow(1);
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
                            Job firstJob = ServerProxy.getJobHandler().getJobById(jobId);
                            srcLang = firstJob.getSourceLocale().toString();
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

        return ReportHelper
                .getReportFile(reportType, job, ReportConstants.EXTENSION_XLSX, langInfo);
    }

    class LocaleScore
    {
        private String targetLocale = "";
        private int count = 1;
        private double avgScore = 0.00d;
        private HashMap<String, Double> scorecardScore = new HashMap<String, Double>();

        private void addScore(List<ScorecardScore> scores)
        {
            if (scores == null || scores.size() == 0)
                return;

            count++;
            int size = scores.size();
            String category;
            int totalScore = 0;
            double score = 0.00d;
            for (ScorecardScore s : scores)
            {
                category = s.getScorecardCategory();
                totalScore += s.getScore();
                scorecardScore.put(category, (s.getScore() + scorecardScore.get(category)) / count);
            }
            avgScore = (avgScore + (totalScore / size)) / 2;
        }

        public String getTargetLocale()
        {
            return targetLocale;
        }

        public void setTargetLocale(String targetLocale)
        {
            this.targetLocale = targetLocale;
        }

        public double getAvgScore()
        {
            return avgScore;
        }

        public void setAvgScore(double avgScore)
        {
            this.avgScore = avgScore;
        }

        public HashMap<String, Double> getScorecardScore()
        {
            return scorecardScore;
        }

        public void setScorecardScore(HashMap<String, Double> scorecardScore)
        {
            this.scorecardScore = scorecardScore;
        }
    }
}
