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
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.jobhandler.JobSearchParameters;
import com.globalsight.everest.projecthandler.Project;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.util.comparator.GlobalSightLocaleComparator;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.pagehandler.administration.reports.ReportConstants;
import com.globalsight.everest.webapp.pagehandler.administration.reports.ReportHelper;
import com.globalsight.everest.webapp.pagehandler.administration.reports.bo.ReportSearchOptions;
import com.globalsight.everest.webapp.pagehandler.administration.reports.bo.ReportWordCount;
import com.globalsight.everest.webapp.pagehandler.administration.reports.util.ReportUtil;
import com.globalsight.everest.workflowmanager.Workflow;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.SortUtil;

/**
 * This Report Generator is used for creating Summary Report, which have 4
 * sheet: 1) Monthly Sheet: Display the data by month. 2) Leveraging Sheet:
 * Display the leveraging data. 3) Costs Sheet: Display costs. 4) Criteria
 * Sheet: Display the search option from page.
 * 
 * @Date Sep 7, 2012
 */
public class SummaryReportGenerator implements
        ReportGenerator
{
    private static Logger logger = Logger.getLogger("SummaryReportGenerator");

    private String[] headers = new String[2];
    public static final int ROWNUMBER = 2;
    private static final String MONEY_FORMAT = "###,###,##0.000";
    private static final String DOUBLE_FORMAT = "#,##0.00";
    private static final String PERCENT_FORMAT = "00%";
    private String moneyFormatString = null;
    private ResourceBundle bundle = null;
    private  CellStyle contentStyle = null;
    private  CellStyle headerStyle = null;
    private  CellStyle headerOrangeStyle = null;
    private  CellStyle floatStyle = null;
    private  CellStyle floatSumStyle = null;
    private  CellStyle moneyStyle = null;
    private  CellStyle moneySumStyle = null;
    private  CellStyle percentStyle = null;
    private  CellStyle percentSumStyle = null;
    private boolean isHidden;
    private String userId = null;

    public SummaryReportGenerator()
    {
    	isHidden = true; // set hidden
    }

    /**
     * Generates the Excel report as a temp file and returns the temp file.
     * 
     * @return File
     * @exception Exception
     */
    public File[] generateReports(ReportSearchOptions p_searchOptions)
            throws Exception
    {
        bundle = p_searchOptions.getBundle();
        moneyFormatString = ReportUtil.getCurrencySymbol(p_searchOptions
        		.getCurrency()) + MONEY_FORMAT;
        File file = ReportHelper.getXLSReportFile(getReportType(), null);
        Workbook workbook = new SXSSFWorkbook();
        String monthlySheetName = bundle.getString("lb_monthly");
        String leveragingSheetName = bundle.getString("lb_state_leveraging");
        String costsSheetName = bundle.getString("lb_costs");
        String criteriaSheetName = bundle.getString("lb_criteria");

        // Generates Summary Word Count Data.
        Map<String, ReportWordCount> summaryWordCount = getSummaryWordCount(p_searchOptions);

        // Create Monthly Sheet
        Sheet monthlySheet = workbook.createSheet(monthlySheetName);
        createMonthlySheet(workbook, monthlySheet, p_searchOptions, summaryWordCount);

        // Create Leveraging Sheet
        Sheet leveragingSheet = workbook.createSheet(
                leveragingSheetName);
        createLeveragingSheet(workbook, leveragingSheet,summaryWordCount);

        // Create Costs Sheet
        Sheet costsSheet = workbook.createSheet(costsSheetName);
        createLeveragingSheet(workbook, costsSheet,summaryWordCount);
        createCostsSheet(workbook, costsSheet, p_searchOptions, summaryWordCount);
        costsSheet.setZoom(9, 10);

        // Create Criteria Sheet
        Sheet criteriaSheet = workbook.createSheet(criteriaSheetName);
        createCriteriaSheet(workbook, criteriaSheet, p_searchOptions, summaryWordCount);

        FileOutputStream out = new FileOutputStream(file);
        workbook.write(out);
        out.close();
        ((SXSSFWorkbook)workbook).dispose();

        List<File> workBooks = new ArrayList<File>();
        workBooks.add(file);
        return ReportHelper.moveReports(workBooks, p_searchOptions.getUserId());
    }

    /**
     * Creates Monthly Sheet
     */
    private void createMonthlySheet(Workbook p_workbook, Sheet p_sheet,
            ReportSearchOptions p_options,
            Map<String, ReportWordCount> p_wordCounts) throws Exception
    {
    	List<String> searchMonths = p_options.getMonths();
    	
        CellStyle style = getHeaderStyle(p_workbook);

        CellStyle style1 = getHeaderStyle(p_workbook, null, null, null, null);

        CellStyle styleTB = getHeaderStyle(p_workbook, CellStyle.BORDER_THIN,
        		null, CellStyle.BORDER_THIN, null);

        CellStyle styleTB2 = getHeaderStyle(p_workbook, CellStyle.BORDER_THIN,
        		null, CellStyle.BORDER_THIN, null);
        styleTB2.setAlignment(CellStyle.ALIGN_RIGHT);

        CellStyle styleLR = getHeaderStyle(p_workbook, null,
        		CellStyle.BORDER_THIN, null, CellStyle.BORDER_THIN);
        
        int row = ROWNUMBER, column = 0;

        p_sheet.setColumnWidth(0, 12 * 256);

        Cell cell_A_Header = getCell(getRow(p_sheet, row), column++);
        cell_A_Header.setCellValue(bundle.getString("lb_sumOfTotal"));
        cell_A_Header.setCellStyle(style);
        p_sheet.addMergedRegion(new CellRangeAddress(row, row,
        		column, column + searchMonths.size()));
        setRegionStyle(p_sheet, new CellRangeAddress(row, row,
        		column, column + searchMonths.size()), style);
        Cell cell_B_Header = getCell(getRow(p_sheet, row), column);
        cell_B_Header.setCellValue(bundle.getString("lb_month"));
        cell_B_Header.setCellStyle(style);

        row++;
        column = 0;
        Cell cell_A = getCell(getRow(p_sheet, row), column++);
        cell_A.setCellValue(bundle.getString("lb_lang"));
        cell_A.setCellStyle(style);
        for (String yearAndMonth : searchMonths)
        {
        	Cell cell_Month = getCell(getRow(p_sheet, row), column++);
        	cell_Month.setCellValue(Double.valueOf(yearAndMonth.substring(4)));
        	cell_Month.setCellStyle(styleTB);
        }
        p_sheet.setColumnWidth(column, 10 * 256);
        Cell cell_LocaleTotal = getCell(getRow(p_sheet, row), column++);
        cell_LocaleTotal.setCellValue(bundle.getString("lb_grandTotal"));
        cell_LocaleTotal.setCellStyle(style);

        // Adds a hidden column, for Excel Sum Check Error.
        Row hiddenRow = getRow(p_sheet, ++row);
        hiddenRow.setZeroHeight(isHidden);
        getRow(p_sheet, row -1).setHeight(p_sheet.getDefaultRowHeight());

        int dataRow = ++row;
        column = 0;
        double totalWordCount = 0;
        Set<String> locales = getLocals(p_wordCounts);
        for (String locale : locales)
        {
        	Cell cell_A_Locale = getCell(getRow(p_sheet, row), column++);
        	cell_A_Locale.setCellValue(locale);
        	cell_A_Locale.setCellStyle(styleLR);
            for (String yearAndMonth : searchMonths)
            {
                ReportWordCount reportWordCount = p_wordCounts
                        .get(getWordCountMapKey(locale, yearAndMonth));
                if (reportWordCount != null)
                {
                    totalWordCount = reportWordCount.getTradosTotalWordCount();
                }
                addNumberCell(p_sheet, column++, row, totalWordCount, style1);
                totalWordCount = 0;
            }
            Cell cell_LocaleTotal_Month = getCell(getRow(p_sheet, row), column);
            cell_LocaleTotal_Month.setCellFormula(getSumOfRow(1, column - 1, row));
            cell_LocaleTotal_Month.setCellStyle(styleLR);
            row++;
            column = 0;
        }

        if (row > (ROWNUMBER + 3))
        {
            column = 0;
            Cell cell_GrandTotal = getCell(getRow(p_sheet, row), column++);
            cell_GrandTotal.setCellValue(bundle.getString("lb_grandTotal"));
            cell_GrandTotal.setCellStyle(style);
            for (int i = 0; i < searchMonths.size(); i++)
            {
            	Cell cell_MonthTotal = getCell(getRow(p_sheet, row), column);
            	cell_MonthTotal.setCellFormula(getSumOfColumn(
                        dataRow, row - 1, column));
            	cell_MonthTotal.setCellStyle(styleTB);
                column++;
            }
            Cell cell_Total = getCell(getRow(p_sheet, row), column);
            cell_Total.setCellFormula(getSumOfColumn(
                    dataRow, row - 1, column));
            cell_Total.setCellStyle(style);
        }
    }

    /**
     * Creates Leveraging Sheet
     */
    private void createLeveragingSheet(Workbook p_workbook, Sheet p_sheet,
            Map<String, ReportWordCount> p_wordCounts) throws Exception
    {
    	addLeveragingSheetHeader(p_workbook, p_sheet);
    	addLeveragingSheetData(p_workbook, p_sheet, p_wordCounts);
    }
    
    private void addLeveragingSheetHeader(Workbook p_workbook, Sheet p_sheet)
    	throws Exception
	{
    	int row = ROWNUMBER, column = 0;

        Row thirRow = getRow(p_sheet, row);
        Cell cell_A = getCell(thirRow, column++);
        cell_A.setCellValue(bundle.getString("lb_lang"));
        cell_A.setCellStyle(getHeaderOrangeStyle(p_workbook));
        
        Cell cell_B = getCell(thirRow, column++);
        cell_B.setCellValue(bundle
        		.getString("jobinfo.tradosmatches.invoice.per100matches"));
        cell_B.setCellStyle(getHeaderOrangeStyle(p_workbook));
        
        Cell cell_C = getCell(thirRow, column++);
        cell_C.setCellValue(bundle.getString("lb_95_99"));
        cell_C.setCellStyle(getHeaderOrangeStyle(p_workbook));
        
        Cell cell_D = getCell(thirRow, column++);
        cell_D.setCellValue(bundle.getString("lb_85_94"));
        cell_D.setCellStyle(getHeaderOrangeStyle(p_workbook));
        
        Cell cell_E = getCell(thirRow, column++);
        cell_E.setCellValue(bundle.getString("lb_75_84"));
        cell_E.setCellStyle(getHeaderOrangeStyle(p_workbook));
        
        Cell cell_F = getCell(thirRow, column++);
        cell_F.setCellValue(bundle.getString("lb_no_match"));
        cell_F.setCellStyle(getHeaderOrangeStyle(p_workbook));
        
        p_sheet.setColumnWidth(column, 10 * 256);
        Cell cell_G = getCell(thirRow, column++);
        cell_G.setCellValue(bundle.getString("lb_repetition_word_cnt"));
        cell_G.setCellStyle(getHeaderOrangeStyle(p_workbook));

        if (headers[0] != null)
        {
            p_sheet.setColumnWidth(column, 10 * 256);
            Cell cell_InContext = getCell(thirRow, column++);
            cell_InContext.setCellValue(bundle.getString("lb_in_context_tm"));
            cell_InContext.setCellStyle(getHeaderOrangeStyle(p_workbook));
        }
        if (headers[1] != null)
        {
            p_sheet.setColumnWidth(column, 10 * 256);
            Cell cell_Context = getCell(thirRow, column++);
            cell_Context.setCellValue(bundle.getString("lb_context_matches"));
            cell_Context.setCellStyle(getHeaderOrangeStyle(p_workbook));
        }

        Cell cell_Total = getCell(thirRow, column++);
        cell_Total.setCellValue(bundle.getString("lb_total"));
        cell_Total.setCellStyle(getHeaderOrangeStyle(p_workbook));
        
        p_sheet.setColumnWidth(column, 13 * 256);
        Cell cell_Leveraging = getCell(thirRow, column++);
        cell_Leveraging.setCellValue(bundle.getString("lb_leveraging"));
        cell_Leveraging.setCellStyle(getHeaderOrangeStyle(p_workbook));
	}
    
    private void addLeveragingSheetData(Workbook p_workbook, Sheet p_sheet,
    		Map<String, ReportWordCount> p_wordCounts)throws Exception
	{
    	// Prepare report data
        Map<String, ReportWordCount> sumWordCounts = new HashMap<String, ReportWordCount>();
        for (String key : p_wordCounts.keySet())
        {
            ReportWordCount monthWordCount = p_wordCounts.get(key);
            String sumKey = key.substring(0, 5);
            ReportWordCount sumWordCount = sumWordCounts.get(sumKey);
            if (sumWordCount == null)
            {
                sumWordCounts.put(sumKey, monthWordCount.clone());
            }
            else
            {
                sumWordCount.addTradosWordCount(monthWordCount);
            }
        }

        // Display report data.
        int row = ROWNUMBER + 1;
        int column = 0;
        List<String> sumKeyList = new ArrayList<String>(sumWordCounts.keySet());
        SortUtil.sort(sumKeyList);
        for (String sumKey : sumKeyList)
        {
            ReportWordCount sumWordCount = sumWordCounts.get(sumKey);
            if (sumWordCount != null)
            {
            	Cell cell_A_Lang = getCell(getRow(p_sheet, row), column++);
            	cell_A_Lang.setCellValue(sumKey);
            	cell_A_Lang.setCellStyle(getHeaderStyle(p_workbook));
            	
                addNumberCell(p_sheet, column++, row,
                        sumWordCount.getTrados100WordCount(),
                        getHeaderStyle(p_workbook));
                
                addNumberCell(p_sheet, column++, row,
                        sumWordCount.getTrados95to99WordCount(),
                        getHeaderStyle(p_workbook));
                
                addNumberCell(p_sheet, column++, row,
                        sumWordCount.getTrados85to94WordCount(), 
                        getHeaderStyle(p_workbook));
                
                addNumberCell(p_sheet, column++, row,
                        sumWordCount.getTrados75to84WordCount(), 
                        getHeaderStyle(p_workbook));
                
                addNumberCell(p_sheet, column++, row,
                        sumWordCount.getTradosNoMatchWordCount()
                                + sumWordCount.getTrados50to74WordCount(),
                                getHeaderStyle(p_workbook));
                
                addNumberCell(p_sheet, column++, row,
                        sumWordCount.getTradosRepsWordCount(), 
                        getHeaderStyle(p_workbook));
                
                if (headers[0] != null)
                {
                    addNumberCell(p_sheet, column++, row,
                            sumWordCount.getTradosInContextMatchWordCount(),
                            getHeaderStyle(p_workbook));
                }
                if (headers[1] != null)
                {
                    addNumberCell(p_sheet, column++, row,
                            sumWordCount.getTradosContextMatchWordCount(),
                            getHeaderStyle(p_workbook));
                }
                
                addNumberCell(p_sheet, column++, row,
                        sumWordCount.getTradosTotalWordCount(), getHeaderStyle(p_workbook));
                String leveraging = "(1-F" + (row + 1) + "/"
                        + getColumnName(column - 1) + (row + 1) + ")*100";
                
                Cell cell_TotalLeveraging = getCell(getRow(p_sheet, row), column++);
                cell_TotalLeveraging.setCellFormula(leveraging);
                cell_TotalLeveraging.setCellStyle(getFloatStyle(p_workbook));

                row++;
                column = 0;
            }
        }
        
        addLeveragingSheetTotal(p_workbook, p_sheet, row);
	}
    
    private void addLeveragingSheetTotal(Workbook p_workbook, Sheet p_sheet,
    		int row)throws Exception
	{
    	// Total Row
        if (row > (ROWNUMBER + 1))
        {
            int column = 0;
            Cell cell_GrandTotal = getCell(getRow(p_sheet, row), column++);
            cell_GrandTotal.setCellValue(bundle.getString("lb_grandTotal"));
            cell_GrandTotal.setCellStyle(getHeaderOrangeStyle(p_workbook));
            while (column < (p_sheet.getRow(row - 1).getPhysicalNumberOfCells() - 1))
            {
            	Cell totalCell = getCell(getRow(p_sheet, row), column);
            	totalCell.setCellFormula(getSumOfColumn(
                        ROWNUMBER + 1, row - 1, column));
            	totalCell.setCellStyle(getHeaderOrangeStyle(p_workbook));
                column++;
            }
            String leveraging = "(1-F" + (row + 1) + "/"
                    + getColumnName(column - 1) + (row + 1) + ")*100";
            
            Cell cell_TotalLeveraging = getCell(getRow(p_sheet, row), column++);
            cell_TotalLeveraging.setCellFormula(leveraging);
            cell_TotalLeveraging.setCellStyle(getFloatSumStyle(p_workbook));
        }
	}
    
    /**
     * Creates Leveraging Sheet
     */
    private void createCostsSheet(Workbook p_workbook, Sheet p_sheet,
            ReportSearchOptions p_options,
            Map<String, ReportWordCount> p_wordCounts) throws Exception
    {
        int rowLen = p_sheet.getPhysicalNumberOfRows();
        int colLen = p_sheet.getRow(2).getPhysicalNumberOfCells();
        int wordTotalCol = colLen - 2;
        int row = ROWNUMBER, column = colLen - 1;
        int costCol;
        Map<String, Double> p_ratesMap = null;
        for(int r = 2; r < rowLen + ROWNUMBER; r++){
        	Row theRow = getRow(p_sheet, r);
        	theRow.removeCell(getCell(theRow, column));
        }
        p_sheet.removeColumnBreak(column);
        // Rates Columns
        for (int dis = column - 1; column < colLen + dis - 2; column++)
        {
        	Cell cell_From = p_sheet.getRow(row).getCell(column - dis);
        	Cell cell_To = getCell(p_sheet.getRow(row), column);
        	cell_To.setCellValue(cell_From.getStringCellValue());
        	cell_To.setCellStyle(cell_From.getCellStyle());
            p_sheet.setColumnWidth(column, p_sheet.getColumnWidth(column - dis));
            // Adds Rates for Match Type
            for (int rateRow = row + 1; rateRow <= rowLen; rateRow++)
            {
                String matchType = p_sheet.getRow(ROWNUMBER)
                	.getCell(column).getStringCellValue();
                String targetLocale = p_sheet.getRow(rateRow)
                	.getCell(0).getStringCellValue();
                double rate = getRate(matchType, targetLocale, p_ratesMap);
                addNumberCell(p_sheet, column, rateRow, rate, getMoneyStyle(p_workbook));
            }
        }

        // Cost Columns Head
        costCol = column;
        p_sheet.setColumnWidth(column, 20 * 256);
        Cell cell_CostWithLeveraging = getCell(getRow(p_sheet, row), column++);
        cell_CostWithLeveraging.setCellValue(bundle
                .getString("lb_report_costWithLeveraging"));
        cell_CostWithLeveraging.setCellStyle(getHeaderOrangeStyle(p_workbook));
        
        p_sheet.setColumnWidth(column, 20 * 256);
        Cell cell_CostNoLeveraging = getCell(getRow(p_sheet, row), column++);
        cell_CostNoLeveraging.setCellValue(bundle
        		.getString("lb_report_costNoLeveraging"));
        cell_CostNoLeveraging.setCellStyle(getHeaderOrangeStyle(p_workbook));
        
        p_sheet.setColumnWidth(column, 15 * 256);
        Cell cell_Savings = getCell(getRow(p_sheet, row), column++);
        cell_Savings.setCellValue(bundle.getString("lb_savings"));
        cell_Savings.setCellStyle(getHeaderOrangeStyle(p_workbook));
        
        Cell cell_Percent = getCell(getRow(p_sheet, row), column++);
        cell_Percent.setCellValue("%");
        cell_Percent.setCellStyle(getHeaderOrangeStyle(p_workbook));
        // Cost Columns Data
        for (row = ROWNUMBER + 1; row < (rowLen + ROWNUMBER); row++)
        {
            String leveragingForm = getCostWithLeveraging(1, wordTotalCol - 1,
                    wordTotalCol, (row + 1));
            String noLeveragingForm = getColumnName(wordTotalCol) + (row + 1)
                    + "*" + getColumnName(wordTotalCol + 5) + (row + 1);
            String savingForm = getColumnName(costCol + 1) + (row + 1) + "-"
                    + getColumnName(costCol) + (row + 1);
            String percent = getColumnName(costCol + 2) + (row + 1) + "/"
                    + getColumnName(costCol + 1) + (row + 1);

            Row theRow = getRow(p_sheet, row);
            Cell cell_Leveraging = getCell(theRow, costCol);
            cell_Leveraging.setCellFormula(leveragingForm);
            cell_Leveraging.setCellStyle(getMoneyStyle(p_workbook));
            
            Cell cell_NoLeveraging = getCell(theRow, costCol + 1);
            cell_NoLeveraging.setCellFormula(noLeveragingForm);
            cell_NoLeveraging.setCellStyle(getMoneyStyle(p_workbook));
            
            Cell cell_Saving = getCell(theRow, costCol + 2);
            cell_Saving.setCellFormula(savingForm);
            cell_Saving.setCellStyle(getMoneyStyle(p_workbook));
            
            Cell cell_PercentData = getCell(theRow, costCol + 3);
            cell_PercentData.setCellFormula(percent);
            cell_PercentData.setCellStyle(getPercentStyle(p_workbook));
        }

        if (rowLen > 1)
        {
            row = rowLen + 1;
            column = 1;
            for (; column < colLen - 1; column++)
            {
            	Cell cell_Total = getCell(getRow(p_sheet, row), column);
            	cell_Total.setCellFormula(getSumOfColumn(
                        ROWNUMBER + 1, row - 1, column));
            	cell_Total.setCellStyle(getHeaderOrangeStyle(p_workbook));
            }
            for (; column < costCol; column++)
            {
            	Cell cell = getCell(getRow(p_sheet, row), column);
                cell.setCellValue("");
                cell.setCellStyle(getHeaderOrangeStyle(p_workbook));
            }

            // Summary Cost Columns
            Cell cell_SumLeveraging = getCell(getRow(p_sheet, row), column);
            cell_SumLeveraging.setCellFormula(getSumOfColumn(
                    ROWNUMBER + 1, row - 1, column++));
            cell_SumLeveraging.setCellStyle(getMoneySumStyle(p_workbook));
            
            Cell cell_SumNoLeveraging = getCell(getRow(p_sheet, row), column);
            cell_SumNoLeveraging.setCellFormula(getSumOfColumn(
                    ROWNUMBER + 1, row - 1, column++));
            cell_SumNoLeveraging.setCellStyle(getMoneySumStyle(p_workbook));
            
            Cell cell_SumSaving = getCell(getRow(p_sheet, row), column);
            cell_SumSaving.setCellFormula(getSumOfColumn(
                    ROWNUMBER + 1, row - 1, column++));
            cell_SumSaving.setCellStyle(getMoneySumStyle(p_workbook));
            
            String percent = getColumnName(column - 1) + (row + 1) + "/"
                    + getColumnName(column - 2) + (row + 1);
            Cell cell_AvgPercent = getCell(getRow(p_sheet, row), column);
            cell_AvgPercent.setCellFormula(percent);
            cell_AvgPercent.setCellStyle(getPercentSumStyle(p_workbook));
        }
    }
    
    private void createCriteriaSheet(Workbook p_workbook, Sheet p_sheet,
            ReportSearchOptions p_options,
            Map<String, ReportWordCount> p_wordCounts) throws Exception
    {
        StringBuffer temp = new StringBuffer();
        int row = -1;
        String mark = ": ";
        p_sheet.setColumnWidth(0, 20 * 256);
        p_sheet.setColumnWidth(1, 50 * 256);
        Cell cell_A_Title = getCell(getRow(p_sheet, ++row), 0);
        cell_A_Title.setCellValue(bundle.getString("lb_report_criteria"));
        cell_A_Title.setCellStyle(getContentStyle(p_workbook));
        
        Cell cell_CompanyName = getCell(getRow(p_sheet, ++row), 0);
        cell_CompanyName.setCellValue(bundle.getString("lb_company_name") + mark);
        cell_CompanyName.setCellStyle(getContentStyle(p_workbook));
        
        Cell cell_CompanyNameData = getCell(getRow(p_sheet, row), 1);
        cell_CompanyNameData.setCellValue(p_options.getCurrentCompanyName());
        cell_CompanyNameData.setCellStyle(getContentStyle(p_workbook));
        
        Cell cell_StartDate = getCell(getRow(p_sheet, ++row), 0);
        cell_StartDate.setCellValue(bundle
                .getString("lb_report_startDate") + mark);
        cell_StartDate.setCellStyle(getContentStyle(p_workbook));
        
        Cell cell_StartDateData = getCell(getRow(p_sheet, row), 1);
        cell_StartDateData.setCellValue(p_options.getStartDateStr());
        cell_StartDateData.setCellStyle(getContentStyle(p_workbook));
        
        Cell cell_EndDate = getCell(getRow(p_sheet, ++row), 0);
        cell_EndDate.setCellValue(bundle
                .getString("lb_report_endDate") + mark);
        cell_EndDate.setCellStyle(getContentStyle(p_workbook));
        
        Cell cell_EndDateData = getCell(getRow(p_sheet, row), 1);
        cell_EndDateData.setCellValue(p_options.getEndDateStr());
        cell_EndDateData.setCellStyle(getContentStyle(p_workbook));

        // Project Search option
        Cell cell_Project = getCell(getRow(p_sheet, ++row), 0);
        cell_Project.setCellValue(bundle
        		.getString("lb_project") + mark);
        cell_Project.setCellStyle(getContentStyle(p_workbook));        
        if (p_options.isAllProjects())
        {    
        	temp.setLength(0);
        	temp.append(bundle.getString("lb_all"));     
        }
        else
        {
            temp.setLength(0);
            for (long projectId : p_options.getProjectIdList())
            {
                Project proj = ServerProxy.getProjectHandler().getProjectById(
                        projectId);
                temp.append(proj.getName()).append(",");
            }
            temp = new StringBuffer(temp.substring(0, temp.length() -1));
        }
        Cell cell_ProjectData = getCell(getRow(p_sheet, row), 1);
        cell_ProjectData.setCellValue(temp.toString());
        cell_ProjectData.setCellStyle(getContentStyle(p_workbook));

        // Status Search option
        Cell cell_Status = getCell(getRow(p_sheet, ++row), 0);
        cell_Status.setCellValue(bundle
        		.getString("lb_status") + mark);
        cell_Status.setCellStyle(getContentStyle(p_workbook));
        if (p_options.isAllJobStatus())
        {          
        	temp.setLength(0);
        	temp.append(bundle.getString("lb_all")); 
        }
        else
        {
            temp.setLength(0);
            for (String status : p_options.getJobStatusList())
            {
                temp.append(ReportHelper.getJobStatusDisplayName(status))
                        .append(",");
            }
            temp = new StringBuffer(temp.substring(0, temp.length() -1));
        }
        Cell cell_StatusData = getCell(getRow(p_sheet, row), 1);
        cell_StatusData.setCellValue(temp.toString());
        cell_StatusData.setCellStyle(getContentStyle(p_workbook));

        // Target Locales Search option
        Cell cell_TargetLang = getCell(getRow(p_sheet, ++row), 0);
        cell_TargetLang.setCellValue(bundle
        		.getString("lb_target_language") + mark);
        cell_TargetLang.setCellStyle(getContentStyle(p_workbook));
        if (p_options.isAllTargetLangs())
        {
            
        	temp.setLength(0);
        	temp.append(bundle.getString("lb_all"));
        }
        else
        {
            temp.setLength(0);
            for (GlobalSightLocale gl : p_options.getTargetLocaleList())
            {
                temp.append(gl.getDisplayName()).append(",");
            }
            temp = new StringBuffer(temp.substring(0, temp.length() -1));
        }
        Cell cell_TargetLangData = getCell(getRow(p_sheet, row), 1);
        cell_TargetLangData.setCellValue(temp.toString());
        cell_TargetLangData.setCellStyle(getContentStyle(p_workbook));

        // Currency Search Option
        Cell cell_Currency = getCell(getRow(p_sheet, ++row), 0);
        cell_Currency.setCellValue(bundle
                .getString("lb_currency") + mark);
        cell_Currency.setCellStyle(getContentStyle(p_workbook));
        
        Cell cell_CurrencyData = getCell(getRow(p_sheet, row), 1);
        cell_CurrencyData.setCellValue(p_options.getCurrency());
        cell_CurrencyData.setCellStyle(getContentStyle(p_workbook));
    }

    public ReportSearchOptions getSearchOptions(HttpServletRequest p_request)
    {
        ReportSearchOptions options = new ReportSearchOptions();
        userId = (String) p_request.getSession(false).getAttribute(WebAppConstants.USER_NAME);

        options.setBundle(PageHandler.getBundle(p_request.getSession(false)));
        options.setStartDate(p_request.getParameter("startDate"));
        options.setEndDate(p_request.getParameter("endDate"));
        options.setAllJobIds(true);
        setProjectIdList(p_request, options);
        setJobStatusList(p_request, options);
        setTargetLangList(p_request, options);
        options.setCurrency(p_request.getParameter("currency"));
        options.setCurrentCompany(CompanyThreadLocal.getInstance().getValue());
        options.setUserId(userId);

        return options;
    }

    /**
     * Gets the Summary Word Count, by month and targetLocale. Return the
     * SummaryWordCount Map, such as Map<fr_FR-201202, ReportWordCount>.
     */
    private Map<String, ReportWordCount> getSummaryWordCount(
            ReportSearchOptions p_options) throws Exception
    {
        Map<String, ReportWordCount> summaryDatas = new HashMap<String, ReportWordCount>();
        HashSet<Job> jobs = new HashSet<Job>();
        if (p_options.isAllJobIds())
        {
            jobs.addAll(ServerProxy.getJobHandler().getJobs(
                    getSearchParams(p_options)));
        }
        else
        {
            for (int i = 0; i < p_options.getJobIdList().size(); i++)
            {
                Job j = ServerProxy.getJobHandler().getJobById(
                        p_options.getJobIdList().get(i));
                jobs.add(j);
            }
        }

        Calendar createDate = Calendar.getInstance();
        for(Job j: jobs)
        {
            createDate.setTime(j.getCreateDate());
            int createDateYear = createDate.get(Calendar.YEAR);
            int createDateMonth = createDate.get(Calendar.MONTH) + 1;

            // only handle jobs in these states
            if (!(Job.READY_TO_BE_DISPATCHED.equals(j.getState())
                    || Job.DISPATCHED.equals(j.getState())
                    || Job.LOCALIZED.equals(j.getState())
                    || Job.EXPORTED.equals(j.getState())
                    || Job.EXPORT_FAIL.equals(j.getState()) || Job.ARCHIVED
                        .equals(j.getState())))
            {
                continue;
            }

            for (Workflow w : j.getWorkflows())
            {
                String state = w.getState();
                // skip certain workflows
                if (!(Workflow.READY_TO_BE_DISPATCHED.equals(state)
                        || Workflow.EXPORTED.equals(state)
                        || Workflow.DISPATCHED.equals(state)
                        || Workflow.LOCALIZED.equals(state)
                        || Workflow.EXPORT_FAILED.equals(state) || Workflow.ARCHIVED
                            .equals(state)))
                {
                    continue;
                }

                if (!p_options.isAllTargetLangs()
                        && !p_options.getTargetLocaleList().contains(
                                w.getTargetLocale()))
                {
                    continue;
                }

                String key = getWordCountMapKey(w.getTargetLocale(),
                        createDateYear, createDateMonth);
                ReportWordCount reportWordCount = (ReportWordCount) summaryDatas
                        .get(key);
                if (reportWordCount == null)
                {
                    reportWordCount = new ReportWordCount();
                    reportWordCount.setTargetLocale(w.getTargetLocale());
                    reportWordCount.setYearMonth(createDateYear,
                            createDateMonth);
                    summaryDatas.put(key, reportWordCount);
                }

                if (PageHandler.isInContextMatch(w.getJob()))
                {
                    reportWordCount.addTrados100WordCount(w
                            .getSegmentTmWordCount());
                    reportWordCount.addTradosInContextMatchWordCount(w
                            .getInContextMatchWordCount());

                    if (w.getInContextMatchWordCount() > 0)
                    {
                        if (headers[0] == null)
                        {
                            headers[0] = "";
                        }
                        headers[0] = headers[0] + w.getJob().getJobId();
                    }
                }
                else
                {
                    reportWordCount.addTrados100WordCount(w
                            .getTotalExactMatchWordCount());
                    reportWordCount.addTradosInContextMatchWordCount(w
                            .getNoUseInContextMatchWordCount());
                    reportWordCount.addTradosContextMatchWordCount(0);
                }

                reportWordCount.addTrados95to99WordCount(w
                        .getThresholdHiFuzzyWordCount());
                reportWordCount.addTrados85to94WordCount(w
                        .getThresholdMedHiFuzzyWordCount());
                reportWordCount.addTrados75to84WordCount(w
                        .getThresholdMedFuzzyWordCount());
                reportWordCount.addTrados50to74WordCount(w
                        .getThresholdLowFuzzyWordCount());
                reportWordCount.addTradosNoMatchWordCount(w
                        .getThresholdNoMatchWordCount());
                reportWordCount.addTradosRepsWordCount(w
                        .getRepetitionWordCount());
            }
        }
        return summaryDatas;
    }

    private String getWordCountMapKey(GlobalSightLocale p_locale, int p_year,
            int p_month)
    {
        return getWordCountMapKey(p_locale, p_year + "" + p_month);
    }

    private String getWordCountMapKey(GlobalSightLocale p_locale,
            String p_yearAndMonth)
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
        for (String key : summaryDatas.keySet())
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
                try
                {
                    List<Project> projectList = (ArrayList<Project>) ServerProxy
                            .getProjectHandler().getProjectsByUser(userId);
                    for (Project project : projectList)
                    {
                        p_options.addProjectId(String.valueOf(project
                                .getIdAsLong()));
                    }
                    break;
                }
                catch (Exception e)
                {
                }
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
        String[] targetLangs = p_request
                .getParameterValues(ReportConstants.TARGETLOCALE_LIST);
        try
        {
            targetLocaleList = ReportHelper.getTargetLocaleList(targetLangs,
                    new GlobalSightLocaleComparator(
                            GlobalSightLocaleComparator.ISO_CODE, Locale.US));
        }
        catch (Exception e)
        {
            logger.error("Get Target Locales Error.", e);
        }

        p_options.setTargetLocaleList(targetLocaleList);

        if (targetLocaleList != null
                && targetLocaleList.size() == ReportHelper.getAllTargetLocales(
                        null, -1).size())
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
        
        sp.setProjectId(p_options.getProjectIdList());

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

    private double getRate(String p_matchType, String p_targetLocale,
            Map<String, Double> p_ratesMap)
    {
        double defaultRate = 0.000;
        if (p_ratesMap == null || p_ratesMap.size() == 0)
            return defaultRate;

        Double rate = p_ratesMap.get(p_matchType + "-" + p_targetLocale);
        return rate == null ? rate : defaultRate;
    }

    private void addNumberCell(Sheet p_sheet, int p_column, int p_row,
            double p_value, CellStyle cs)
    {
        try
        {
        	Cell cell = getCell(getRow(p_sheet, p_row), p_column);
        	cell.setCellValue(p_value);
            if (cs != null)
                cell.setCellStyle(cs);
        }
        catch (Exception e)
        {
            logger.warn("addNumberCell Error1.[" + p_sheet.getSheetName() + ", "
                    + p_column + ", " + p_row + ", " + p_value + "]", e);
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
    
    private CellStyle getHeaderStyle(Workbook p_workbook, Short top, Short right,
    		Short bottom, Short left){
    	Font headerFont = p_workbook.createFont();
        headerFont.setColor(IndexedColors.BLACK.getIndex());
        headerFont.setUnderline(Font.U_NONE);
        headerFont.setFontName("Arial");
        headerFont.setFontHeightInPoints((short) 9);
    	
        CellStyle cs = p_workbook.createCellStyle();
        cs.setFont(headerFont);
    	cs.setWrapText(true);
        if(top != null){        	
        	cs.setBorderTop(top);
        }
        if(right != null){      	
        	cs.setBorderRight(right);
        }
        if(bottom != null){      	
        	cs.setBorderBottom(bottom);
        }
        if(left != null){	
        	cs.setBorderLeft(left);
        }
        
        return cs;
    }
    
    private CellStyle getHeaderStyle(Workbook p_workbook){
    	if(headerStyle == null)
    	{   		   		
    		headerStyle = getCommonHeaderStyle(p_workbook);
    	}
        return headerStyle;
    }
    
    private CellStyle getCommonHeaderStyle(Workbook p_workbook)
    {		
		Font headerFont = p_workbook.createFont();
		headerFont.setColor(IndexedColors.BLACK.getIndex());
		headerFont.setUnderline(Font.U_NONE);
		headerFont.setFontName("Arial");
		headerFont.setFontHeightInPoints((short) 9);
		
		CellStyle cs = p_workbook.createCellStyle();
		cs.setFont(headerFont);
		cs.setWrapText(true);    	
		cs.setBorderTop(CellStyle.BORDER_THIN);     	
		cs.setBorderRight(CellStyle.BORDER_THIN);   	
		cs.setBorderBottom(CellStyle.BORDER_THIN);
		cs.setBorderLeft(CellStyle.BORDER_THIN);
		return cs;
    }
    
    private CellStyle getHeaderOrangeStyle(Workbook p_workbook){
    	if(headerOrangeStyle == null)
    	{  		
    		CellStyle cs = getCommonHeaderStyle(p_workbook);
    		cs.setFillPattern(CellStyle.SOLID_FOREGROUND );
    		cs.setFillForegroundColor(IndexedColors.ORANGE.getIndex());
    		
    		headerOrangeStyle = cs;
    	}
        return headerOrangeStyle;
    }
    
    private CellStyle getFloatStyle(Workbook p_workbook){
    	if(floatStyle == null)
    	{   		
    		DataFormat formatDouble = p_workbook.createDataFormat();
    		CellStyle cs = getCommonHeaderStyle(p_workbook);
    		cs.setDataFormat(formatDouble.getFormat(DOUBLE_FORMAT));
    		
    		floatStyle = cs;
    	}
        return floatStyle;
    }
    
    private CellStyle getFloatSumStyle(Workbook p_workbook){
    	if(floatSumStyle == null)
    	{   		
    		DataFormat formatDouble = p_workbook.createDataFormat();
    		CellStyle cs = getCommonHeaderStyle(p_workbook);
    		cs.setFillPattern(CellStyle.SOLID_FOREGROUND );
    		cs.setFillForegroundColor(IndexedColors.ORANGE.getIndex());
    		cs.setDataFormat(formatDouble.getFormat(DOUBLE_FORMAT));
   		
    		floatSumStyle = cs;
    	}
        return floatSumStyle;
    }
    
    private CellStyle getMoneyStyle(Workbook p_workbook){
    	if(moneyStyle == null)
    	{   		
    		DataFormat moneyFormat = p_workbook.createDataFormat();
    		CellStyle cs = getCommonHeaderStyle(p_workbook);
    		cs.setDataFormat(moneyFormat.getFormat(moneyFormatString));
   		
    		moneyStyle = cs;
    	}
        return moneyStyle;
    }
    
    private CellStyle getMoneySumStyle(Workbook p_workbook){
    	if(moneySumStyle == null)
    	{   		
    		DataFormat moneyFormat = p_workbook.createDataFormat();
    		CellStyle cs = getCommonHeaderStyle(p_workbook);
            cs.setDataFormat(moneyFormat.getFormat(moneyFormatString));
            cs.setFillPattern(CellStyle.SOLID_FOREGROUND );
            cs.setFillForegroundColor(IndexedColors.ORANGE.getIndex());
   		
    		moneySumStyle = cs;
    	}
        return moneySumStyle;
    }
    
    private CellStyle getPercentStyle(Workbook p_workbook){
    	if(percentStyle == null)
    	{
    		DataFormat percentFormat = p_workbook.createDataFormat();   		
    		CellStyle cs = getCommonHeaderStyle(p_workbook);
    		cs.setDataFormat(percentFormat.getFormat(PERCENT_FORMAT));
   		
            percentStyle = cs;
    	}
        return percentStyle;
    }
    
    private CellStyle getPercentSumStyle(Workbook p_workbook){
    	if(percentSumStyle == null)
    	{
    		DataFormat percentFormat = p_workbook.createDataFormat();
    		
    		CellStyle cs = getCommonHeaderStyle(p_workbook);
    		cs.setDataFormat(percentFormat.getFormat(PERCENT_FORMAT));
    		cs.setFillPattern(CellStyle.SOLID_FOREGROUND );
    		cs.setFillForegroundColor(IndexedColors.ORANGE.getIndex());
    		
    		percentSumStyle = cs;
    	}
        return percentSumStyle;
    }
    
    private CellStyle getContentStyle(Workbook p_workbook) throws Exception
    {
        if (contentStyle == null)
        {
            CellStyle style = p_workbook.createCellStyle();
            Font font = p_workbook.createFont();
            font.setFontName("Arial");
            font.setFontHeightInPoints((short) 10);
            style.setFont(font);

            contentStyle = style;
        }

        return contentStyle;
    }
    
    public void setRegionStyle(Sheet sheet, CellRangeAddress cellRangeAddress,
            CellStyle cs)
    {
        for (int i = cellRangeAddress.getFirstRow(); i <= cellRangeAddress
                .getLastRow(); i++)
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
}
