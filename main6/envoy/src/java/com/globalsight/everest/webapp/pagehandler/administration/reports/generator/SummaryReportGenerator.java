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
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;

import jxl.CellView;
import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.format.Alignment;
import jxl.format.UnderlineStyle;
import jxl.write.Formula;
import jxl.write.Label;
import jxl.write.Number;
import jxl.write.NumberFormats;
import jxl.write.WritableCell;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

import org.apache.log4j.Logger;

import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.jobhandler.JobSearchParameters;
import com.globalsight.everest.projecthandler.Project;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.util.comparator.GlobalSightLocaleComparator;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.pagehandler.administration.reports.ReportConstants;
import com.globalsight.everest.webapp.pagehandler.administration.reports.ReportHelper;
import com.globalsight.everest.webapp.pagehandler.administration.reports.XlsReports;
import com.globalsight.everest.webapp.pagehandler.administration.reports.bo.ReportSearchOptions;
import com.globalsight.everest.webapp.pagehandler.administration.reports.bo.ReportWordCount;
import com.globalsight.everest.workflowmanager.Workflow;
import com.globalsight.util.GlobalSightLocale;

/**
 * This Report Generator is used for creating Summary Report, which have 4 sheet:
 * 1) Monthly Sheet: Display the data by month.
 * 2) Leveraging Sheet: Display the leveraging data.
 * 3) Costs Sheet: Display costs.
 * 4) Criteria Sheet: Display the search option from page. 
 * 
 * @Date Sep 7, 2012
 */
public class SummaryReportGenerator extends XlsReports implements ReportGenerator
{
    private static Logger logger = Logger.getLogger("SummaryReportGenerator");
    
    private String[] headers = new String[2];
    public static final int ROWNUMBER = 2;
    private CellView hiddenCellView;
    
    public SummaryReportGenerator()
    {
        hiddenCellView = new CellView();
        hiddenCellView.setHidden(true); //set hidden
    }
    
    /**
     * Generates the Excel report as a temp file and returns the temp file.
     * 
     * @return File
     * @exception Exception
     */
    public File[] generateReports(ReportSearchOptions p_searchOptions) throws Exception
    {
        ResourceBundle bundle = p_searchOptions.getBundle();
        WorkbookSettings settings = new WorkbookSettings();
        settings.setSuppressWarnings(true);
        settings.setCellValidationDisabled(true);
        File file = ReportHelper.getXLSReportFile(getReportType(), null);
        WritableWorkbook workbook = Workbook.createWorkbook(file, settings);
        String monthlySheetName = bundle.getString("lb_monthly");
        String leveragingSheetName = bundle.getString("lb_state_leveraging");
        String costsSheetName = bundle.getString("lb_costs");
        String criteriaSheetName = bundle.getString("lb_criteria");
        
        // Generates Summary Word Count Data.
        Map<String, ReportWordCount> summaryWordCount = getSummaryWordCount(p_searchOptions);
        
        int sheetNum = 0;
        // Create Monthly Sheet
        WritableSheet monthlySheet = workbook.createSheet(monthlySheetName, sheetNum++);
        createMonthlySheet(monthlySheet, p_searchOptions, summaryWordCount);
        
        // Create Leveraging Sheet
        WritableSheet leveragingSheet = workbook.createSheet(leveragingSheetName, sheetNum++);
        createLeveragingSheet(leveragingSheet, p_searchOptions, summaryWordCount);
        
        // Create Costs Sheet
        workbook.copySheet(leveragingSheetName, costsSheetName, sheetNum++);
        WritableSheet costsSheet = workbook.getSheet(costsSheetName);
        createCostsSheet(costsSheet, p_searchOptions, summaryWordCount);
        costsSheet.getSettings().setZoomFactor(90);
        
        // Create Criteria Sheet
        WritableSheet criteriaSheet = workbook.createSheet(criteriaSheetName, sheetNum++);
        createCriteriaSheet(criteriaSheet, p_searchOptions, summaryWordCount);
        
        workbook.write();
        workbook.close();
        
        return new File[]{file};
    }
    
    public ReportSearchOptions getSearchOptions(HttpServletRequest p_request)
    {
        ReportSearchOptions options = new ReportSearchOptions();
        
        options.setBundle(PageHandler.getBundle(p_request.getSession(false)));
        options.setStartDate(p_request.getParameter("startDate"));
        options.setEndDate(p_request.getParameter("endDate"));
        options.setAllJobIds(true);
        setProjectIdList(p_request, options);
        setJobStatusList(p_request, options);
        setTargetLangList(p_request, options);
        options.setCurrency(p_request.getParameter("currency"));
        options.setCurrentCompany(CompanyThreadLocal.getInstance().getValue());
        
        return options;
    }
    
    /**
     * Gets the Summary Word Count, by month and targetLocale.
     * Return the SummaryWordCount Map, such as Map<fr_FR-201202, ReportWordCount>.
     */
    private Map<String, ReportWordCount> getSummaryWordCount(ReportSearchOptions p_options)
            throws Exception
    {
        Map<String, ReportWordCount> summaryDatas = new HashMap<String, ReportWordCount>();
        ArrayList queriedJobs = new ArrayList();
        if (p_options.isAllJobIds())
        {
            queriedJobs.addAll(ServerProxy.getJobHandler().getJobs(
                    getSearchParams(p_options)));
        }
        else
        {
            for (int i = 0; i < p_options.getJobIdList().size(); i++)
            {
                Job j = ServerProxy.getJobHandler().getJobById(p_options.getJobIdList().get(i));
                queriedJobs.add(j);
            }
        }

        HashSet<Job> jobs = new HashSet<Job>();
        jobs.addAll(queriedJobs);
        Iterator<Job> jobIter = jobs.iterator();

        while (jobIter.hasNext())
        {
            Job j = jobIter.next();
            Calendar createDate = Calendar.getInstance();
            createDate.setTime(j.getCreateDate());
            int createDateYear = createDate.get(Calendar.YEAR);
            int createDateMonth = createDate.get(Calendar.MONTH) + 1;

            // only handle jobs in these states
            if (!(Job.READY_TO_BE_DISPATCHED.equals(j.getState())
                    || Job.DISPATCHED.equals(j.getState())
                    || Job.LOCALIZED.equals(j.getState())
                    || Job.EXPORTED.equals(j.getState())
                    || Job.EXPORT_FAIL.equals(j.getState()) 
                    || Job.ARCHIVED.equals(j.getState())))
            {
                continue;
            }

            Iterator<Workflow> wfIter = j.getWorkflows().iterator();
            while (wfIter.hasNext())
            {
                Workflow w = wfIter.next();
                String state = w.getState();
                // skip certain workflows
                if (!(Workflow.READY_TO_BE_DISPATCHED.equals(state)
                        || Workflow.EXPORTED.equals(state)
                        || Workflow.DISPATCHED.equals(state)
                        || Workflow.LOCALIZED.equals(state)
                        || Workflow.EXPORT_FAILED.equals(state) 
                        || Workflow.ARCHIVED.equals(state)))
                {
                    continue;
                }

                if (!p_options.isAllTargetLangs()
                        && !p_options.getTargetLocaleList().contains(w.getTargetLocale()))
                {
                    continue;
                }
                
                String key = getWordCountMapKey(w.getTargetLocale(), createDateYear, createDateMonth);
                ReportWordCount reportWordCount = (ReportWordCount) summaryDatas.get(key);
                if (reportWordCount == null)
                {
                    reportWordCount = new ReportWordCount();
                    reportWordCount.setTargetLocale(w.getTargetLocale());
                    reportWordCount.setYearMonth(createDateYear, createDateMonth);
                    summaryDatas.put(key, reportWordCount);
                }

                if (PageHandler.isInContextMatch(w.getJob()))
                {
                    reportWordCount.addTrados100WordCount(w.getSegmentTmWordCount());
                    reportWordCount.addTradosInContextMatchWordCount(w.getInContextMatchWordCount());
                    
                    if (w.getInContextMatchWordCount() > 0)
                    {
                        if (headers[0] == null)
                        {
                            headers[0] = "";
                        }
                        headers[0] = headers[0] + w.getJob().getJobId();
                    }
                }
                else if (PageHandler.isDefaultContextMatch(w.getJob()))
                {
                    reportWordCount.addTrados100WordCount(w.getTotalExactMatchWordCount() - w.getContextMatchWordCount());
                    reportWordCount.addTradosContextMatchWordCount(w.getContextMatchWordCount());
                    
                    if (w.getContextMatchWordCount() > 0)
                    {
                        if (headers[1] == null)
                        {
                            headers[1] = "";
                        }
                        headers[1] = headers[1] + w.getJob().getJobId();
                    }
                }
                else
                {
                    reportWordCount.addTrados100WordCount(w.getTotalExactMatchWordCount());
                    reportWordCount.addTradosInContextMatchWordCount(w.getNoUseInContextMatchWordCount());
                    reportWordCount.addTradosContextMatchWordCount(0);
                }
                
                reportWordCount.addTrados95to99WordCount(w.getThresholdHiFuzzyWordCount());
                reportWordCount.addTrados85to94WordCount(w.getThresholdMedHiFuzzyWordCount());
                reportWordCount.addTrados75to84WordCount(w.getThresholdMedFuzzyWordCount());
                reportWordCount.addTrados50to74WordCount(w.getThresholdLowFuzzyWordCount());
                reportWordCount.addTradosNoMatchWordCount(w.getThresholdNoMatchWordCount());
                reportWordCount.addTradosRepsWordCount(w.getRepetitionWordCount());
            }
        }
        return summaryDatas;
    }

    private String getWordCountMapKey(GlobalSightLocale p_locale, int p_year, int p_month)
    {
        return getWordCountMapKey(p_locale, p_year + "" + p_month);
    }
    
    private String getWordCountMapKey(GlobalSightLocale p_locale, String p_yearAndMonth)
    {
        return getWordCountMapKey(p_locale.toString(), p_yearAndMonth);
    }
    
    private String getWordCountMapKey(String p_localeStr, String p_yearAndMonth)
    {
        return p_localeStr + "-" + p_yearAndMonth;
    }
    
    private Set<String> getLocals(Map<String, ?> summaryDatas)
    {
        Set<String> result = new TreeSet<String>();
        for(String key : summaryDatas.keySet())
        {
            result.add(key.substring(0, 5));
        }
        return result;
    }
    
    private void setProjectIdList(HttpServletRequest p_request,
            ReportSearchOptions p_options)
    {
        // set the project Id
        String[] projectIds = p_request.getParameterValues("projectId");

        for (int i = 0; i < projectIds.length; i++)
        {
            String id = projectIds[i];
            if ("*".equals(id))
            {
                p_options.setAllProjects(true);
                break;
            }
            else
            {
                p_options.addProjectId(id);
            }
        }
    }
    
    private void setJobStatusList(HttpServletRequest p_request,
            ReportSearchOptions p_options)
    {
        String[] jobStatus = p_request.getParameterValues("status");
        for (int i = 0; i < jobStatus.length; i++)
        {
            String status = jobStatus[i];
            if ("*".equals(status))
            {
                p_options.setAllJobStatus(true);
                break;
            }
            else
            {
                p_options.addJobStatusList(status);
            }
        }
    }

    private void setTargetLangList(HttpServletRequest p_request,
            ReportSearchOptions p_options)
    {
        List<GlobalSightLocale> targetLocaleList = null;
        String[] targetLangs = p_request.getParameterValues(ReportConstants.TARGETLOCALE_LIST);
        try
        {
            targetLocaleList = ReportHelper.getTargetLocaleList(targetLangs,
                    new GlobalSightLocaleComparator(GlobalSightLocaleComparator.ISO_CODE, Locale.US));
        }
        catch (Exception e)
        {
            logger.error("Get Target Locales Error.", e);
        }
        
        p_options.setTargetLocaleList(targetLocaleList);
        
        if (targetLocaleList != null
                && targetLocaleList.size() == ReportHelper.getAllTargetLocales(null, -1).size())
        {
            p_options.setAllTargetLangs(true);
        }
    }

    /**
     * Returns search params used to find the jobs based on state
     * (READY,EXPORTED,LOCALIZED,DISPATCHED,export failed, archived), and
     * creation date during the range
     * 
     * @return JobSearchParams
     */
    private JobSearchParameters getSearchParams(ReportSearchOptions p_options)
    {
        JobSearchParameters sp = new JobSearchParameters();
        if (!p_options.isAllProjects())
        {
            sp.setProjectId(p_options.getProjectIdList());
        }

        if (!p_options.isAllJobStatus())
        {
            sp.setJobState(p_options.getJobStatusList());
        }
        else
        {
            // job state EXPORTED,DISPATCHED,LOCALIZED,EXPORTED FAILED
            ArrayList<String> list = ReportHelper.getAllJobStatusList();
            list.remove(Job.PENDING);
            sp.setJobState(list);
        }

        sp.setCreationStart(p_options.getStartDate());
        sp.setCreationEnd(p_options.getEndDate());

        return sp;
    }

    /**
     * Creates Monthly Sheet
     */
    private void createMonthlySheet(WritableSheet p_sheet,
            ReportSearchOptions p_options,
            Map<String, ReportWordCount> p_wordCounts) throws Exception
    {
        List<String> searchMonths = p_options.getMonths();
        WritableFont headerFont = new WritableFont(WritableFont.ARIAL, 9,
                WritableFont.NO_BOLD, false, UnderlineStyle.NO_UNDERLINE,
                jxl.format.Colour.BLACK);

        WritableCellFormat format = new WritableCellFormat(headerFont);
        format.setShrinkToFit(false);
        format.setBorder(jxl.format.Border.TOP, 
                jxl.format.BorderLineStyle.THIN);
        format.setBorder(jxl.format.Border.BOTTOM, 
                jxl.format.BorderLineStyle.THIN);
        format.setBorder(jxl.format.Border.LEFT,
                jxl.format.BorderLineStyle.THIN);
        format.setBorder(jxl.format.Border.RIGHT,
                jxl.format.BorderLineStyle.THIN);
        
        WritableCellFormat format1 = new WritableCellFormat(headerFont);
        
        WritableCellFormat formatTB = new WritableCellFormat(headerFont);
        formatTB.setWrap(true);
        formatTB.setShrinkToFit(false);
        formatTB.setBorder(jxl.format.Border.TOP, 
                jxl.format.BorderLineStyle.THIN);
        formatTB.setBorder(jxl.format.Border.BOTTOM, 
                jxl.format.BorderLineStyle.THIN);
        
        WritableCellFormat formatTB2 = new WritableCellFormat(headerFont);
        formatTB2.setWrap(true);
        formatTB2.setShrinkToFit(false);
        formatTB2.setBorder(jxl.format.Border.TOP, 
                jxl.format.BorderLineStyle.THIN);
        formatTB2.setBorder(jxl.format.Border.BOTTOM, 
                jxl.format.BorderLineStyle.THIN);
        formatTB2.setAlignment(Alignment.RIGHT);
        
        WritableCellFormat formatLR = new WritableCellFormat(headerFont);
        formatLR.setWrap(true);
        formatLR.setShrinkToFit(false);
        formatLR.setBorder(jxl.format.Border.LEFT,
                jxl.format.BorderLineStyle.THIN);
        formatLR.setBorder(jxl.format.Border.RIGHT, 
                jxl.format.BorderLineStyle.THIN);

        ResourceBundle bundle = p_options.getBundle();
        int row = ROWNUMBER, column = 0;
        
        p_sheet.setColumnView(0, 12);
        p_sheet.addCell(new Label(column++, row, bundle.getString("lb_sumOfTotal"),
                format));
        p_sheet.mergeCells(column, row, column + searchMonths.size(), row);
        p_sheet.addCell(new Label(column, row, bundle.getString("lb_month"),
                format));
        
        row++; column = 0;
        p_sheet.addCell(new Label(column++, row, bundle.getString("lb_lang"),
                format));
        for(String yearAndMonth : searchMonths)
        {
            addNumberCell(p_sheet, column++, row, yearAndMonth.substring(4), formatTB);
        }
        p_sheet.setColumnView(column, 10);
        p_sheet.addCell(new Label(column++, row, bundle.getString("lb_grandTotal"),
                format));
        
        // Adds a hidden column, for Excel Sum Check Error.
        p_sheet.setRowView(++row, hiddenCellView);
        
        int dataRow = ++row; column = 0;
        double totalWordCount = 0;
        Set<String> locales = getLocals(p_wordCounts);
        for(String locale : locales)
        {
            p_sheet.addCell(new Label(column++, row, locale, formatLR));
            for(String yearAndMonth : searchMonths)
            {
                ReportWordCount reportWordCount = p_wordCounts.get(getWordCountMapKey(locale, yearAndMonth));
                if (reportWordCount != null)
                {
                    totalWordCount = reportWordCount.getTradosTotalWordCount();
                }
                addNumberCell(p_sheet, column++, row, totalWordCount, format1);
                totalWordCount = 0;
            }
            p_sheet.addCell(new Formula(column, row, getSumOfRow(1, column - 1, row), formatLR));
            row++;
            column = 0;
        }
        
        if (row > (ROWNUMBER + 3))
        {
            column = 0;
            p_sheet.addCell(new Label(column++, row, bundle
                    .getString("lb_grandTotal"), format));
            for (int i = 0; i < searchMonths.size(); i++)
            {
                p_sheet.addCell(new Formula(column, row, getSumOfColumn(
                        dataRow, row - 1, column), formatTB));
                column++;
            }
            p_sheet.addCell(new Formula(column, row, getSumOfColumn(dataRow,
                    row - 1, column), format));
        }
    }

    /**
     * Creates Leveraging Sheet
     */
    private void createLeveragingSheet(WritableSheet p_sheet,
            ReportSearchOptions p_options,
            Map<String, ReportWordCount> p_wordCounts) throws Exception
    {
        // headerFont is black bold on light grey
        WritableFont headerFont = new WritableFont(WritableFont.ARIAL, 9,
                WritableFont.NO_BOLD, false, UnderlineStyle.NO_UNDERLINE,
                jxl.format.Colour.BLACK);

        WritableCellFormat formatHead = new WritableCellFormat(headerFont);
        formatHead.setWrap(true);
        formatHead.setBackground(jxl.format.Colour.ORANGE);
        formatHead.setShrinkToFit(false);
        formatHead.setBorder(jxl.format.Border.TOP, 
                jxl.format.BorderLineStyle.THIN);
        formatHead.setBorder(jxl.format.Border.BOTTOM, 
                jxl.format.BorderLineStyle.THIN);
        formatHead.setBorder(jxl.format.Border.LEFT,
                jxl.format.BorderLineStyle.THIN);
        formatHead.setBorder(jxl.format.Border.RIGHT,
                jxl.format.BorderLineStyle.THIN);
        
        WritableCellFormat format = new WritableCellFormat(headerFont);
        format.setWrap(true);
        format.setShrinkToFit(false);
        format.setBorder(jxl.format.Border.TOP, 
                jxl.format.BorderLineStyle.THIN);
        format.setBorder(jxl.format.Border.BOTTOM, 
                jxl.format.BorderLineStyle.THIN);
        format.setBorder(jxl.format.Border.LEFT,
                jxl.format.BorderLineStyle.THIN);
        format.setBorder(jxl.format.Border.RIGHT,
                jxl.format.BorderLineStyle.THIN);
        
        WritableCellFormat floatFormat = new WritableCellFormat (NumberFormats.FLOAT); 
        floatFormat.setBorder(jxl.format.Border.TOP, 
                jxl.format.BorderLineStyle.THIN);
        floatFormat.setBorder(jxl.format.Border.BOTTOM, 
                jxl.format.BorderLineStyle.THIN);
        floatFormat.setBorder(jxl.format.Border.LEFT,
                jxl.format.BorderLineStyle.THIN);
        floatFormat.setBorder(jxl.format.Border.RIGHT,
                jxl.format.BorderLineStyle.THIN);
        
        WritableCellFormat floatFormatSum = new WritableCellFormat(floatFormat);
        floatFormatSum.setBackground(formatHead.getBackgroundColour());

        ResourceBundle bundle = p_options.getBundle();
        int row = ROWNUMBER, column = 0;
        
        p_sheet.addCell(new Label(column++, row, bundle.getString("lb_lang"),
                formatHead));
        p_sheet.addCell(new Label(column++, row, bundle.getString("jobinfo.tradosmatches.invoice.per100matches"),
                formatHead));
        p_sheet.addCell(new Label(column++, row, bundle.getString("lb_95_99"),
                formatHead));
        p_sheet.addCell(new Label(column++, row, bundle.getString("lb_85_94"),
                formatHead));
        p_sheet.addCell(new Label(column++, row, bundle.getString("lb_75_84"),
                formatHead));
        p_sheet.addCell(new Label(column++, row, bundle.getString("lb_no_match"),
                formatHead));
        p_sheet.setColumnView(column, 10);
        p_sheet.addCell(new Label(column++, row, bundle.getString("lb_repetition_word_cnt"),
                formatHead));
        
        if (headers[0] != null)
        {
            p_sheet.setColumnView(column, 10);
            p_sheet.addCell(new Label(column++, row, bundle.getString("lb_in_context_tm"), formatHead));
        }
        if (headers[1] != null)
        {
            p_sheet.setColumnView(column, 10);
            p_sheet.addCell(new Label(column++, row, bundle.getString("lb_context_matches"), formatHead));
        }
        
        p_sheet.addCell(new Label(column++, row, bundle.getString("lb_total"),
                formatHead));
        p_sheet.setColumnView(column, 13);
        p_sheet.addCell(new Label(column++, row, bundle.getString("lb_leveraging"),
                formatHead));
        
        // Prepare report data
        Map<String, ReportWordCount> sumWordCounts = new HashMap<String, ReportWordCount>();
        for(String key : p_wordCounts.keySet())
        {
            ReportWordCount monthWordCount = p_wordCounts.get(key);
            String sumKey = key.substring(0, 5);
            ReportWordCount sumWordCount = sumWordCounts.get(sumKey);
            if(sumWordCount == null)
            {
                sumWordCounts.put(sumKey, monthWordCount);
            }
            else
            {
                sumWordCount.addTradosWordCount(monthWordCount);
            }
        }
        
        // Display report data.
        row = ROWNUMBER + 1; column = 0;
        List<String> sumKeyList = new ArrayList<String>(sumWordCounts.keySet());
        Collections.sort(sumKeyList);
        for(String sumKey : sumKeyList)
        {
            ReportWordCount sumWordCount = sumWordCounts.get(sumKey);
            if (sumWordCount != null)
            {
                p_sheet.addCell(new Label(column++, row, sumKey, format));
                addNumberCell(p_sheet, column++, row, sumWordCount.getTrados100WordCount(), format);
                addNumberCell(p_sheet, column++, row, sumWordCount.getTrados95to99WordCount(), format);
                addNumberCell(p_sheet, column++, row, sumWordCount.getTrados85to94WordCount(), format);
                addNumberCell(p_sheet, column++, row, sumWordCount.getTrados75to84WordCount(), format);
                addNumberCell(p_sheet, column++, row, sumWordCount.getTradosNoMatchWordCount() + sumWordCount.getTrados50to74WordCount(), format);
                addNumberCell(p_sheet, column++, row, sumWordCount.getTradosRepsWordCount(), format);
                if (headers[0] != null)
                {
                    addNumberCell(p_sheet, column++, row, sumWordCount.getTradosInContextMatchWordCount(), format);
                }
                if (headers[1] != null)
                {
                    addNumberCell(p_sheet, column++, row, sumWordCount.getTradosContextMatchWordCount(), format);
                }
                addNumberCell(p_sheet, column++, row, sumWordCount.getTradosTotalWordCount(), format);
                String leveraging = "(1-F" + (row + 1) + "/" + getColumnName(column - 1) + (row + 1) + ")*100";
                WritableCell leveragingCell = new Formula(column++, row, leveraging);
                leveragingCell.setCellFormat(floatFormat);
                p_sheet.addCell(leveragingCell);              
                
                row++;
                column = 0;
            }
        }
        
        // Total Row
        if (row > (ROWNUMBER + 1))
        {
            column = 0;
            p_sheet.addCell(new Label(column++, row, bundle
                    .getString("lb_grandTotal"), formatHead));
            while (column < (p_sheet.getColumns() - 1))
            {
                p_sheet.addCell(new Formula(column, row, getSumOfColumn(
                        ROWNUMBER + 1, row - 1, column), formatHead));
                column++;
            }
            String leveraging = "(1-F" + (row + 1) + "/" + getColumnName(column - 1) + (row + 1) + ")*100";
            WritableCell leveragingCell = new Formula(column++, row, leveraging);
            leveragingCell.setCellFormat(floatFormatSum);
            p_sheet.addCell(leveragingCell);
        }
    }
    
    /**
     * Creates Leveraging Sheet
     */
    private void createCostsSheet(WritableSheet p_sheet,
            ReportSearchOptions p_options,
            Map<String, ReportWordCount> p_wordCounts) throws Exception
    {
        ResourceBundle bundle = p_options.getBundle();
        int rowLen = p_sheet.getRows();
        int colLen = p_sheet.getColumns();
        int wordTotalCol = colLen - 2;
        int row = ROWNUMBER, column = colLen - 1;
        int costCol;
        Map<String, Double> p_ratesMap = null;
        
        WritableCellFormat headerFormat = (WritableCellFormat) p_sheet.getCell(1, row).getCellFormat();
        WritableCellFormat moneyFormat = ReportHelper.getMoneyFormat(p_options.getCurrency());
        moneyFormat.setWrap(true);
        moneyFormat.setShrinkToFit(false);
        moneyFormat.setBorder(jxl.format.Border.TOP, 
                jxl.format.BorderLineStyle.THIN);
        moneyFormat.setBorder(jxl.format.Border.BOTTOM, 
                jxl.format.BorderLineStyle.THIN);
        moneyFormat.setBorder(jxl.format.Border.LEFT,
                jxl.format.BorderLineStyle.THIN);
        moneyFormat.setBorder(jxl.format.Border.RIGHT,
                jxl.format.BorderLineStyle.THIN);
        
        WritableCellFormat moneyFormatSum = new WritableCellFormat(moneyFormat);
        moneyFormatSum.setBackground(headerFormat.getBackgroundColour());
        
        WritableCellFormat percentFormat = new WritableCellFormat (NumberFormats.PERCENT_INTEGER); 
        percentFormat.setBorder(jxl.format.Border.TOP, 
                jxl.format.BorderLineStyle.THIN);
        percentFormat.setBorder(jxl.format.Border.BOTTOM, 
                jxl.format.BorderLineStyle.THIN);
        percentFormat.setBorder(jxl.format.Border.LEFT,
                jxl.format.BorderLineStyle.THIN);
        percentFormat.setBorder(jxl.format.Border.RIGHT,
                jxl.format.BorderLineStyle.THIN);
        
        WritableCellFormat percentFormatSum = new WritableCellFormat(percentFormat);
        percentFormatSum.setBackground(headerFormat.getBackgroundColour());
        
        p_sheet.removeColumn(column);    
        // Rates Columns
        for (int dis = column - 1; column < colLen + dis - 2; column++)
        {
            WritableCell cell = (WritableCell) p_sheet.getCell(column - dis, row);
            cell = cell.copyTo(column, row);
            p_sheet.addCell(cell);
            p_sheet.setColumnView(column, p_sheet.getColumnView(column - dis));
            // Adds Rates for Match Type
            for (int rateRow = row + 1; rateRow <= rowLen - 2; rateRow++)
            {
                String matchType = p_sheet.getCell(column, ROWNUMBER).getContents();
                String targetLocale = p_sheet.getCell(0, rateRow).getContents();
                double rate = getRate(matchType, targetLocale, p_ratesMap);
                addNumberCell(p_sheet, column, rateRow, rate, moneyFormat);
            }
        }
        
        // Cost Columns Head
        costCol = column;
        p_sheet.setColumnView(column, 20);
        p_sheet.addCell(new Label(column++, row, bundle.getString("lb_report_costWithLeveraging"), headerFormat));
        p_sheet.setColumnView(column, 20);
        p_sheet.addCell(new Label(column++, row, bundle.getString("lb_report_costNoLeveraging"), headerFormat));
        p_sheet.setColumnView(column, 15);
        p_sheet.addCell(new Label(column++, row, bundle.getString("lb_savings"), headerFormat));
        p_sheet.addCell(new Label(column++, row, "%", headerFormat));
        // Cost Columns Data  
        for (row = ROWNUMBER + 1; row < (rowLen - 1); row++)
        {
            String leveragingForm = getCostWithLeveraging(1, wordTotalCol - 1, wordTotalCol, (row + 1));
            String noLeveragingForm = getColumnName(wordTotalCol) + (row + 1) + "*" + getColumnName(wordTotalCol + 5) + (row + 1);
            String savingForm = getColumnName(costCol + 1) + (row + 1) + "-" + getColumnName(costCol) + (row + 1);
            String percent = getColumnName(costCol + 2) + (row + 1) + "/" + getColumnName(costCol + 1) + (row + 1);
            
            p_sheet.addCell(new Formula(costCol, row, leveragingForm, moneyFormat));
            p_sheet.addCell(new Formula(costCol + 1, row, noLeveragingForm, moneyFormat));
            p_sheet.addCell(new Formula(costCol + 2, row, savingForm, moneyFormat));
            p_sheet.addCell(new Formula(costCol + 3, row, percent, percentFormat));
        }
        
        if (rowLen > (ROWNUMBER + 1))
        {
            row = rowLen - 1;
            column = 1;
            for (; column < colLen - 1; column++)
            {
                p_sheet.addCell(new Formula(column, row, getSumOfColumn(
                        ROWNUMBER + 1, row - 1, column), headerFormat));
            }
            for (; column < costCol; column++)
            {
                p_sheet.addCell(new Label(column, row, "", headerFormat));
            }

            // Summary Cost Columns
            p_sheet.addCell(new Formula(column, row, getSumOfColumn(
                    ROWNUMBER + 1, row - 1, column++), moneyFormatSum));
            p_sheet.addCell(new Formula(column, row, getSumOfColumn(
                    ROWNUMBER + 1, row - 1, column++), moneyFormatSum));
            p_sheet.addCell(new Formula(column, row, getSumOfColumn(
                    ROWNUMBER + 1, row - 1, column++), moneyFormatSum));
            String percent = getColumnName(column - 1) + (row + 1) + "/" + getColumnName(column -  2) + (row + 1);
            p_sheet.addCell(new Formula(column, row, percent, percentFormatSum));
        }
    }
    
    private void createCriteriaSheet(WritableSheet p_sheet,
            ReportSearchOptions p_options,
            Map<String, ReportWordCount> p_wordCounts) throws Exception
    {
        ResourceBundle bundle = p_options.getBundle();
        StringBuffer temp = new StringBuffer();
        int row = -1;
        String mark = ": ";
        p_sheet.setColumnView(0, 20);
        p_sheet.setColumnView(1, 50);
        p_sheet.addCell(new Label(0, ++row, bundle.getString("lb_report_criteria")));
        p_sheet.addCell(new Label(0, ++row, bundle.getString("lb_company_name") + mark));
        p_sheet.addCell(new Label(1, row, p_options.getCurrentCompanyName()));
        p_sheet.addCell(new Label(0, ++row, bundle.getString("lb_report_startDate") + mark));
        p_sheet.addCell(new Label(1, row, p_options.getStartDateStr()));
        p_sheet.addCell(new Label(0, ++row, bundle.getString("lb_report_endDate") + mark));
        p_sheet.addCell(new Label(1, row, p_options.getEndDateStr()));
        
        // Project Search option
        if(p_options.isAllProjects())
        {
            p_sheet.addCell(new Label(0, ++row, bundle.getString("lb_project") + mark));
            p_sheet.addCell(new Label(1, row, bundle.getString("lb_all")));
        }
        else
        {
            temp.setLength(0);
            for(long projectId : p_options.getProjectIdList())
            {
                Project proj = ServerProxy.getProjectHandler().getProjectById(projectId);
                temp.append(proj.getName()).append(", ");
            }
            p_sheet.addCell(new Label(0, ++row, bundle.getString("lb_project") + mark));            
            p_sheet.addCell(new Label(1, row, temp.toString()));
        }
        
        // Status Search option
        if(p_options.isAllJobStatus())
        {
            p_sheet.addCell(new Label(0, ++row, bundle.getString("lb_status") + mark));
            p_sheet.addCell(new Label(1, row, bundle.getString("lb_all")));
        }
        else
        {
            temp.setLength(0);
            for(String status : p_options.getJobStatusList())
            {
                temp.append(ReportHelper.getJobStatusDisplayName(status)).append(", ");
            }
            p_sheet.addCell(new Label(0, ++row, bundle.getString("lb_status") + mark));            
            p_sheet.addCell(new Label(1, row, temp.toString()));
        }
        
        // Target Locales Search option
        if(p_options.isAllTargetLangs())
        {
            p_sheet.addCell(new Label(0, ++row, bundle.getString("lb_target_language") + mark));
            p_sheet.addCell(new Label(1, row, bundle.getString("lb_all")));
        }
        else
        {
            temp.setLength(0);
            for(GlobalSightLocale gl : p_options.getTargetLocaleList())
            {
                temp.append(gl.getDisplayName()).append(", ");
            }
            p_sheet.addCell(new Label(0, ++row, bundle.getString("lb_target_language") + mark));            
            p_sheet.addCell(new Label(1, row, temp.toString()));
        }
        
        // Currency Search Option
        p_sheet.addCell(new Label(0, ++row, bundle.getString("lb_currency") + mark));
        p_sheet.addCell(new Label(1, row, p_options.getCurrency()));
    }
    
    private String getCostWithLeveraging(int p_colStart, int p_colEnd,
            int p_distance, int p_row)
    {
        StringBuffer result = new StringBuffer();
        for (int col = p_colStart; col <= p_colEnd; col++)
        {
            result.append("(").append(getColumnName(col)).append(p_row)
                    .append("*").append(getColumnName(col + p_distance))
                    .append(p_row).append(")").append("+");
        }
        return result.substring(0, result.length() - 1);
    }
    
    private double getRate(String p_matchType, String p_targetLocale, Map<String, Double> p_ratesMap)
    {
        double defaultRate = 0.000;
        if(p_ratesMap == null || p_ratesMap.size() == 0)
            return defaultRate;
        
        Double rate = p_ratesMap.get(p_matchType + "-" + p_targetLocale);
        return rate == null ? rate : defaultRate;
    }
    
    private void addNumberCell(WritableSheet p_sheet, int p_column, int p_row,
            double p_value, WritableCellFormat p_format)
    {
        try
        {
            WritableCell cell = new Number(p_column, p_row, p_value);
            if (p_format != null)
                cell.setCellFormat(p_format);
            p_sheet.addCell(cell);
        }
        catch (RowsExceededException e)
        {
            logger.warn("addNumberCell Error1.[" + p_sheet.getName() + ", "
                    + p_column + ", " + p_row + ", " + p_value + "]", e);
        }
        catch (WriteException e)
        {
            logger.warn("addNumberCell Error2.[" + p_sheet.getName() + ", "
                    + p_column + ", " + p_row + ", " + p_value + "]", e);
        }
    }
    
    private void addNumberCell(WritableSheet p_sheet, int p_column, int p_row,
            String p_value, WritableCellFormat p_format)
    {
        if (p_value != null && p_value.trim().length() > 0)
        {
            addNumberCell(p_sheet, p_column, p_row, Double.valueOf(p_value),
                    p_format);
        }
    }
    
    /**
     * This method is used for getting the column name, "A".."Z" "AA".."AZ"
     * 
     * @param c
     * @return
     */
    private String getColumnName(int c)
    {
        String sumStartCellCol = null;
        if (c > 25)
        {
            sumStartCellCol = "A" + String.valueOf((char) ('A' + c - 26));
        }
        else
        {
            sumStartCellCol = String.valueOf((char) ('A' + c));
        }
        return sumStartCellCol;
    }
    
    private String getSumOfRow(int p_columnStartIndex, int p_columnEndIndex,
            int p_row)
    {
        int row = p_row + 1;
        return "SUM(" + getColumnName(p_columnStartIndex) + row + ":"
                + getColumnName(p_columnEndIndex) + row + ")";
    }
    
    private String getSumOfColumn(int p_rowStartIndex, int p_rowEndIndex,
            int p_column)
    {
        String columnName = getColumnName(p_column);
        int rowStart = p_rowStartIndex + 1;
        int rowEnd = p_rowEndIndex + 1;
        return "SUM(" + columnName + rowStart + ":" + columnName + rowEnd + ")";
    }
    
    @Override
    public File[] generateReports(List<Long> p_jobIDS,
            List<GlobalSightLocale> p_targetLocales) throws Exception
    {
        return null;
    }

    @Override
    public String getReportType()
    {
        return ReportConstants.SUMMARY_REPORT;
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
}
