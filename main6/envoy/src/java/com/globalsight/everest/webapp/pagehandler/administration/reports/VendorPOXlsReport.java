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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.costing.Cost;
import com.globalsight.everest.costing.CostByWordCount;
import com.globalsight.everest.costing.Currency;
import com.globalsight.everest.foundation.SearchCriteriaParameters;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.jobhandler.JobSearchParameters;
import com.globalsight.everest.localemgr.LocaleManagerLocal;
import com.globalsight.everest.projecthandler.Project;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.util.comparator.JobComparator;
import com.globalsight.everest.util.system.SystemConfigParamNames;
import com.globalsight.everest.util.system.SystemConfiguration;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.pagehandler.administration.reports.bo.ReportsData;
import com.globalsight.everest.webapp.pagehandler.administration.reports.generator.ReportGeneratorHandler;
import com.globalsight.everest.webapp.pagehandler.administration.reports.util.ReportUtil;
import com.globalsight.everest.webapp.pagehandler.projects.workflows.JobSearchConstants;
import com.globalsight.everest.workflowmanager.Workflow;
import com.globalsight.util.IntHolder;
import com.globalsight.util.SortUtil;

public class VendorPOXlsReport
{
    private static Logger s_logger = Logger.getLogger("Reports");

    // defines a 0 format for a 3 decimal precision point BigDecimal
    private static final String BIG_DECIMAL_ZERO_STRING = "0.000";

    // the big decimal scale to use for internal math
    private static int SCALE = 3;

    private ResourceBundle bundle = null;
    private String userId = null;
    private CellStyle contentStyle = null;
    private CellStyle redCellStyle = null;
    private CellStyle headerStyle = null;
    private CellStyle dateStyle = null;
    private CellStyle failedDateStyle = null;
    private CellStyle moneyStyle = null;
    private CellStyle failedMoneyStyle = null;
    private CellStyle wrongJobStyle = null;
    private CellStyle subTotalStyle = null;
    private CellStyle totalMoneyStyle = null;

    /* The symbol of the currency from the request */
    private String symbol = null;
    private String currency = null;
    private List<Long> m_jobIDS = null;

    private void init(HttpServletRequest p_request, MyData p_data)
    {
        currency = p_request.getParameter("currency");
        symbol = ReportUtil.getCurrencySymbol(currency);
        setJobNamesList(p_request, p_data);
        setProjectIdList(p_request, p_data);
        setJobStatusList(p_request, p_data);
        setTargetLangList(p_request, p_data);
    }

    public VendorPOXlsReport(HttpServletRequest request,
            HttpServletResponse response) throws Exception
    {
    	 userId = (String) request.getSession(false).getAttribute(
                 WebAppConstants.USER_NAME);
        MyData data = new MyData();
        init(request, data);
        generateReport(request, response, data);
    }

    /**
     * 
     * The inner class for store some useful parameter
     * 
     */
    private class MyData
    {
        private boolean wantsAllProjects = false;
        private boolean wantsAllTargetLangs = false;
        private ArrayList<Long> projectIdList = new ArrayList<Long>();
        private ArrayList<String> targetLangList = new ArrayList<String>();
        private boolean wantsAllJobNames = false;
        private boolean wantsAllJobStatus = false;
        private ArrayList<Long> jobIdList = new ArrayList<Long>();
        private ArrayList<String> jobStatusList = new ArrayList<String>();
        // maps jobs to new project
        private Hashtable<Long, Project> wrongJobMap = new Hashtable<Long, Project>();
        private boolean warnedAboutMissingWrongJobsFile = false;
        private ArrayList<Job> wrongJobs = new ArrayList<Job>();
        private ArrayList<String> wrongJobNames = new ArrayList<String>();
        // wrong jobs which may be queried for the project which should be
        // ignored
        private ArrayList<Job> ignoreJobs = new ArrayList<Job>();
        private Sheet dellSheet = null;
        private Sheet tradosSheet = null;
        private String[] headers = null;
    };

    /**
     * Generates the Excel report as a temp file and returns the temp file.
     * 
     * @return File
     * @exception Exception
     */
    private void generateReport(HttpServletRequest p_request,
            HttpServletResponse p_response, MyData p_data) throws Exception
    {
        bundle = PageHandler.getBundle(p_request.getSession());
        String EMEA = CompanyWrapper.getCurrentCompanyName();
        s_logger.debug("generateReport---, company name: " + EMEA);
        
        Workbook p_workbook = new SXSSFWorkbook();
        boolean recalculateFinishedWorkflow = false;
        String recalcParam = p_request.getParameter("recalc");
        if (recalcParam != null && recalcParam.length() > 0)
        {
            recalculateFinishedWorkflow = java.lang.Boolean
                    .valueOf(recalcParam).booleanValue();
        }
        
        // get all the jobs that were originally imported with the wrong project
        // the users want to pretend that these jobs are in this project
        getJobsInWrongProject(p_data);
        HashMap projectMap = getProjectData(p_request, p_response,
                recalculateFinishedWorkflow, p_data);
        
        if (projectMap != null)
        {
            p_data.dellSheet = p_workbook.createSheet(
                    EMEA + " " + bundle.getString("lb_matches"));
            p_data.tradosSheet = p_workbook.createSheet(
                    bundle.getString("jobinfo.tradosmatches"));
            addTitle(p_workbook, p_data.dellSheet);
            addHeaderForDellMatches(p_workbook, p_data);
            addTitle(p_workbook, p_data.tradosSheet);
            addHeaderForTradosMatches(p_workbook, p_data);
            IntHolder row = new IntHolder(4);
            writeProjectDataForDellMatches(p_workbook, projectMap, row, p_data);
            row = new IntHolder(4);
            writeProjectDataForTradosMatches(p_workbook, projectMap, row, p_data);
            
            Sheet paramsSheet = p_workbook.createSheet(bundle.getString("lb_criteria"));
            writeParamsSheet(p_workbook, paramsSheet, p_data, p_request);

            ServletOutputStream out = p_response.getOutputStream();
            p_workbook.write(out);
            out.close();
            ((SXSSFWorkbook)p_workbook).dispose();

            // Set ReportsData.
            ReportHelper.setReportsData(userId, m_jobIDS, getReportType(), 100, ReportsData.STATUS_FINISHED);
        }
    }
    
    private void addTitle(Workbook p_workbook, Sheet p_sheet)
    throws Exception
    {
        // title font is black bold on white
        String EMEA = CompanyWrapper.getCurrentCompanyName();
        Font titleFont = p_workbook.createFont();
        titleFont.setUnderline(Font.U_NONE);
        titleFont.setFontName("Arial");
        titleFont.setFontHeightInPoints((short) 14);
        titleFont.setBoldweight(Font.BOLDWEIGHT_BOLD);
        titleFont.setColor(IndexedColors.BLACK.getIndex());
        CellStyle titleStyle = p_workbook.createCellStyle();
        titleStyle.setWrapText(false);
        titleStyle.setFont(titleFont);
        
        Row titleRow = getRow(p_sheet, 0);
        Cell cell_A_Title = getCell(titleRow, 0);
        cell_A_Title.setCellValue(EMEA + " " + bundle.getString("lb_po"));
        cell_A_Title.setCellStyle(titleStyle);
        p_sheet.setColumnWidth(0, 20 * 256);
    }

    /**
     * Adds the table header for the Dell Matches sheet
     * 
     */
    private void addHeaderForDellMatches(Workbook p_workbook, MyData p_data)
            throws Exception
    {
        Sheet theSheet = p_data.dellSheet;
        int c = 0;
        Row thirRow = getRow(theSheet, 2);
        Row fourRow = getRow(theSheet, 3);
        
        Cell cell_A = getCell(thirRow, c);
        cell_A.setCellValue(bundle.getString("lb_job_id"));
        cell_A.setCellStyle(getHeaderStyle(p_workbook));
        theSheet.addMergedRegion(new CellRangeAddress(2, 3, c, c));
        setRegionStyle(theSheet, new CellRangeAddress(2, 3, c, c),
        		getHeaderStyle(p_workbook));
        c++;
        Cell cell_B = getCell(thirRow, c);
        cell_B.setCellValue(bundle.getString("lb_job"));
        cell_B.setCellStyle(getHeaderStyle(p_workbook));
        theSheet.addMergedRegion(new CellRangeAddress(2, 3, c, c));
        setRegionStyle(theSheet, new CellRangeAddress(2, 3, c, c), 
        		getHeaderStyle(p_workbook));
        c++;
        Cell cell_C = getCell(thirRow, c);
        cell_C.setCellValue(bundle.getString("lb_po_number_report"));
        cell_C.setCellStyle(getHeaderStyle(p_workbook));
        theSheet.addMergedRegion(new CellRangeAddress(2, 3, c, c));
        setRegionStyle(theSheet, new CellRangeAddress(2, 3, c, c), 
        		getHeaderStyle(p_workbook));
        theSheet.setColumnWidth(c, 15 * 256);
        c++;
        Cell cell_D = getCell(thirRow, c);
        cell_D.setCellValue(bundle.getString("lb_description"));
        cell_D.setCellStyle(getHeaderStyle(p_workbook));
        theSheet.addMergedRegion(new CellRangeAddress(2, 3, c, c));
        setRegionStyle(theSheet, new CellRangeAddress(2, 3, c, c),
        		getHeaderStyle(p_workbook));
        theSheet.setColumnWidth(c, 15 * 256);
        c++;
        Cell cell_E = getCell(thirRow, c);
        cell_E.setCellValue(bundle.getString("lb_creation_date"));
        cell_E.setCellStyle(getHeaderStyle(p_workbook));
        theSheet.addMergedRegion(new CellRangeAddress(2, 3, c, c));
        setRegionStyle(theSheet, new CellRangeAddress(2, 3, c, c), 
        		getHeaderStyle(p_workbook));
        c++;
        Cell cell_F = getCell(thirRow, c);
        cell_F.setCellValue(bundle.getString("lb_lang"));
        cell_F.setCellStyle(getHeaderStyle(p_workbook));
        theSheet.addMergedRegion(new CellRangeAddress(2, 3, c, c));
        setRegionStyle(theSheet, new CellRangeAddress(2, 3, c, c), 
        		getHeaderStyle(p_workbook));
        c++;
        Cell cell_G_Header = getCell(thirRow, c);
        cell_G_Header.setCellValue(bundle.getString("lb_word_counts"));
        cell_G_Header.setCellStyle(getHeaderStyle(p_workbook));

        if (p_data.headers[0] != null)
        {
        	theSheet.addMergedRegion(new CellRangeAddress(2, 2, c, c + 5));
            setRegionStyle(theSheet, new CellRangeAddress(2, 2, c, c + 5), 
            		getHeaderStyle(p_workbook));
        }
        else
        {
        	theSheet.addMergedRegion(new CellRangeAddress(2, 2, c, c + 4));
            setRegionStyle(theSheet, new CellRangeAddress(2, 2, c, c + 4), 
            		getHeaderStyle(p_workbook));
        }

        Cell cell_G = getCell(fourRow, c++);
        cell_G.setCellValue(bundle.getString("jobinfo.tmmatches.wordcounts.internalreps"));
        cell_G.setCellStyle(getHeaderStyle(p_workbook));
        
        Cell cell_H = getCell(fourRow, c++);
        cell_H.setCellValue(bundle.getString("jobinfo.tmmatches.wordcounts.exactmatches"));
        cell_H.setCellStyle(getHeaderStyle(p_workbook));
        if (p_data.headers[0] != null)
        {
        	Cell cell_InContext = getCell(fourRow, c++);
        	cell_InContext.setCellValue(bundle
            		.getString("jobinfo.tmmatches.wordcounts.incontextmatches"));
        	cell_InContext.setCellStyle(getHeaderStyle(p_workbook));
        }
        Cell cell_FuzzyMatches = getCell(fourRow, c++);
        cell_FuzzyMatches.setCellValue(bundle
        		.getString("jobinfo.tmmatches.wordcounts.fuzzymatches"));
        cell_FuzzyMatches.setCellStyle(getHeaderStyle(p_workbook));
        
        Cell cell_Newwords = getCell(fourRow, c++);
        cell_Newwords.setCellValue(bundle.getString("jobinfo.tmmatches.wordcounts.newwords"));
        cell_Newwords.setCellStyle(getHeaderStyle(p_workbook));
        
        Cell cell_Total = getCell(fourRow, c++);
        cell_Total.setCellValue(bundle.getString("lb_total"));
        cell_Total.setCellStyle(getHeaderStyle(p_workbook));

        Cell cell_Invoice = getCell(thirRow, c);
        cell_Invoice.setCellValue(bundle.getString("jobinfo.tmmatches.invoice"));
        cell_Invoice.setCellStyle(getHeaderStyle(p_workbook));

        if (p_data.headers[0] != null)
        {
        	theSheet.addMergedRegion(new CellRangeAddress(2, 2, c, c + 5));
            setRegionStyle(theSheet, new CellRangeAddress(2, 2, c, c + 5),
            		getHeaderStyle(p_workbook));
        }
        else
        {
        	theSheet.addMergedRegion(new CellRangeAddress(2, 2, c, c + 4));
            setRegionStyle(theSheet, new CellRangeAddress(2, 2, c, c + 4),
            		getHeaderStyle(p_workbook));
        }

        Cell cell_InternalReps = getCell(fourRow, c++);
        cell_InternalReps.setCellValue(bundle
        		.getString("jobinfo.tmmatches.invoice.internalreps"));
        cell_InternalReps.setCellStyle(getHeaderStyle(p_workbook));
        
        Cell cell_ExactMatches = getCell(fourRow, c++);
        cell_ExactMatches.setCellValue(bundle
        		.getString("jobinfo.tmmatches.invoice.exactmatches"));
        cell_ExactMatches.setCellStyle(getHeaderStyle(p_workbook));
        if (p_data.headers[0] != null)
        {
        	Cell cell_InContext = getCell(fourRow, c++);
        	cell_InContext.setCellValue(bundle
            		.getString("jobinfo.tmmatches.invoice.incontextmatches"));
        	cell_InContext.setCellStyle(getHeaderStyle(p_workbook));
        }

        Cell cell_FuzzyMatches_Invoice = getCell(fourRow, c++);
        cell_FuzzyMatches_Invoice.setCellValue(bundle
        		.getString("jobinfo.tmmatches.invoice.fuzzymatches"));
        cell_FuzzyMatches_Invoice.setCellStyle(getHeaderStyle(p_workbook));
        
        Cell cell_NewWords_Invoice = getCell(fourRow, c++);
        cell_NewWords_Invoice.setCellValue(bundle
        		.getString("jobinfo.tmmatches.invoice.newwords"));
        cell_NewWords_Invoice.setCellStyle(getHeaderStyle(p_workbook));
        
        Cell cell_Total_Invoice = getCell(fourRow, c++);
        cell_Total_Invoice.setCellValue(bundle.getString("lb_total"));
        cell_Total_Invoice.setCellStyle(getHeaderStyle(p_workbook));
        
        theSheet.addMergedRegion(new CellRangeAddress(2, 3, c, c));
        setRegionStyle(theSheet, new CellRangeAddress(2, 3, c, c), getContentStyle(p_workbook));
    }
    
    /**
     * Adds the table header for the Trados Matches sheet
     * 
     */
    private void addHeaderForTradosMatches(Workbook p_workbook, MyData p_data)
    	throws Exception
    {
        Sheet theSheet = p_data.tradosSheet;
        int c = 0;
        Row secRow = getRow(theSheet, 1);
        Row thirRow = getRow(theSheet, 2);
        Row fourRow = getRow(theSheet, 3);
        
        Cell cell_Ldfl = getCell(secRow, c);
        cell_Ldfl.setCellValue(bundle.getString("lb_desp_file_list"));
        cell_Ldfl.setCellStyle(getContentStyle(p_workbook));
        
        Cell cell_A = getCell(thirRow, c);
        cell_A.setCellValue(bundle.getString("lb_job_id"));
        cell_A.setCellStyle(getHeaderStyle(p_workbook));
        theSheet.addMergedRegion(new CellRangeAddress(2, 3, c, c));
        setRegionStyle(theSheet, new CellRangeAddress(2, 3, c, c), 
        		getHeaderStyle(p_workbook));
        c++;
        Cell cell_B = getCell(thirRow, c);
        cell_B.setCellValue(bundle.getString("lb_job"));
        cell_B.setCellStyle(getHeaderStyle(p_workbook));
        theSheet.addMergedRegion(new CellRangeAddress(2, 3, c, c));
        setRegionStyle(theSheet, new CellRangeAddress(2, 3, c, c), 
        		getHeaderStyle(p_workbook));
        c++;
        Cell cell_C = getCell(thirRow, c);
        cell_C.setCellValue(bundle.getString("lb_po_number_report"));
        cell_C.setCellStyle(getHeaderStyle(p_workbook));
        theSheet.addMergedRegion(new CellRangeAddress(2, 3, c, c));
        setRegionStyle(theSheet, new CellRangeAddress(2, 3, c, c), 
        		getHeaderStyle(p_workbook));
        c++;
        Cell cell_D = getCell(thirRow, c);
        cell_D.setCellValue(bundle.getString("reportDesc"));
        cell_D.setCellStyle(getHeaderStyle(p_workbook));
        theSheet.addMergedRegion(new CellRangeAddress(2, 3, c, c));
        setRegionStyle(theSheet, new CellRangeAddress(2, 3, c, c), 
        		getHeaderStyle(p_workbook));
        theSheet.setColumnWidth(c, 15 * 256);
        c++;
        Cell cell_E = getCell(thirRow, c);
        cell_E.setCellValue(bundle.getString("lb_creation_date"));
        cell_E.setCellStyle(getHeaderStyle(p_workbook));
        theSheet.addMergedRegion(new CellRangeAddress(2, 3, c, c));
        setRegionStyle(theSheet, new CellRangeAddress(2, 3, c, c), 
        		getHeaderStyle(p_workbook));
        c++;
        Cell cell_F = getCell(thirRow, c);
        cell_F.setCellValue(bundle.getString("lb_lang"));
        cell_F.setCellStyle(getHeaderStyle(p_workbook));
        theSheet.addMergedRegion(new CellRangeAddress(2, 3, c, c));
        setRegionStyle(theSheet, new CellRangeAddress(2, 3, c, c), 
        		getHeaderStyle(p_workbook));
        c++;
        Cell cell_G_Header = getCell(thirRow, c);
        cell_G_Header.setCellValue(bundle.getString("jobinfo.tmmatches.wordcounts"));
        cell_G_Header.setCellStyle(getHeaderStyle(p_workbook));

        if (p_data.headers[0] != null)
        {
        	theSheet.addMergedRegion(new CellRangeAddress(2, 2, c, c + 7));
        	setRegionStyle(theSheet, new CellRangeAddress(2, 2, c, c + 7),
        			getHeaderStyle(p_workbook));
        }
        else
        {
        	theSheet.addMergedRegion(new CellRangeAddress(2, 2, c, c + 6));
        	setRegionStyle(theSheet, new CellRangeAddress(2, 2, c, c + 6),
        			getHeaderStyle(p_workbook));
        }

        Cell cell_G = getCell(fourRow, c++);
        cell_G.setCellValue(bundle.getString("jobinfo.tradosmatches.invoice.per100matches"));
        cell_G.setCellStyle(getHeaderStyle(p_workbook));
        
        Cell cell_H = getCell(fourRow, c++);
        cell_H.setCellValue(bundle.getString("lb_95_99"));
        cell_H.setCellStyle(getHeaderStyle(p_workbook));
        
        Cell cell_I = getCell(fourRow, c++);
        cell_I.setCellValue(bundle.getString("lb_85_94"));
        cell_I.setCellStyle(getHeaderStyle(p_workbook));
        
        Cell cell_J = getCell(fourRow, c++);
        cell_J.setCellValue(bundle.getString("lb_75_84") + "*");
        cell_J.setCellStyle(getHeaderStyle(p_workbook));
        
        Cell cell_K = getCell(fourRow, c++);
        cell_K.setCellValue(bundle.getString("lb_no_match"));
        cell_K.setCellStyle(getHeaderStyle(p_workbook));
        
        Cell cell_L = getCell(fourRow, c++);
        cell_L.setCellValue(bundle.getString("lb_repetition_word_cnt"));
        cell_L.setCellStyle(getHeaderStyle(p_workbook));
        if (p_data.headers[0] != null)
        {
        	Cell cell_InContext = getCell(fourRow, c++);
        	cell_InContext.setCellValue(bundle.getString("lb_in_context_tm"));
        	cell_InContext.setCellStyle(getHeaderStyle(p_workbook));
        }
        Cell cell_Total = getCell(fourRow, c++);
        cell_Total.setCellValue(bundle.getString("lb_total"));
        cell_Total.setCellStyle(getHeaderStyle(p_workbook));

        Cell cell_Invoice = getCell(thirRow, c);
        cell_Invoice.setCellValue(bundle.getString("jobinfo.tmmatches.invoice"));
        cell_Invoice.setCellStyle(getHeaderStyle(p_workbook));
        if (p_data.headers[0] != null)
        {
        	theSheet.addMergedRegion(new CellRangeAddress(2, 2, c, c + 7));
            setRegionStyle(theSheet, new CellRangeAddress(2, 2, c, c + 7),
            		getHeaderStyle(p_workbook));
        }
        else
        {
        	theSheet.addMergedRegion(new CellRangeAddress(2, 2, c, c + 6));
            setRegionStyle(theSheet, new CellRangeAddress(2, 2, c, c + 6),
            		getHeaderStyle(p_workbook));
        }

        Cell cell_Per100Matches = getCell(fourRow, c++);
        cell_Per100Matches.setCellValue(bundle
        		.getString("jobinfo.tradosmatches.invoice.per100matches"));
        cell_Per100Matches.setCellStyle(getHeaderStyle(p_workbook));
        
        Cell cell_95_99 = getCell(fourRow, c++);
        cell_95_99.setCellValue(bundle.getString("lb_95_99"));
        cell_95_99.setCellStyle(getHeaderStyle(p_workbook));
        
        Cell cell_85_94 = getCell(fourRow, c++);
        cell_85_94.setCellValue(bundle.getString("lb_85_94"));
        cell_85_94.setCellStyle(getHeaderStyle(p_workbook));
        
        Cell cell_75_84 = getCell(fourRow, c++);
        cell_75_84.setCellValue(bundle.getString("lb_75_84") + "*");
        cell_75_84.setCellStyle(getHeaderStyle(p_workbook));
        
        Cell cell_NoMatch = getCell(fourRow, c++);
        cell_NoMatch.setCellValue(bundle.getString("lb_no_match"));
        cell_NoMatch.setCellStyle(getHeaderStyle(p_workbook));
        
        Cell cell_Repetition = getCell(fourRow, c++);
        cell_Repetition.setCellValue(bundle.getString("lb_repetition_word_cnt"));
        cell_Repetition.setCellStyle(getHeaderStyle(p_workbook));
        if (p_data.headers[0] != null)
        {
        	Cell cell_InContext = getCell(fourRow, c++);
        	cell_InContext.setCellValue(bundle.getString("lb_in_context_match"));
        	cell_InContext.setCellStyle(getHeaderStyle(p_workbook));
        }
        Cell cell_Total_Invoice = getCell(fourRow, c++);
        cell_Total_Invoice.setCellValue(bundle.getString("lb_total"));
        cell_Total_Invoice.setCellStyle(getHeaderStyle(p_workbook));
    }

    public void writeProjectDataForDellMatches(Workbook p_workbook, HashMap p_projectMap,
            IntHolder p_row, MyData p_data) throws Exception
    {
        Sheet theSheet = p_data.dellSheet;
        ArrayList projects = new ArrayList(p_projectMap.keySet());
        SortUtil.sort(projects);
        Iterator projectIter = projects.iterator();

        while (projectIter.hasNext())
        {
            String jobName = (String) projectIter.next();
            boolean isWrongJob = p_data.wrongJobNames.contains(jobName);
            HashMap localeMap = (HashMap) p_projectMap.get(jobName);
            ArrayList locales = new ArrayList(localeMap.keySet());
            SortUtil.sort(locales);
            Iterator localeIter = locales.iterator();
            BigDecimal projectTotalWordCountCost = new BigDecimal(
                    BIG_DECIMAL_ZERO_STRING);
            while (localeIter.hasNext())
            {
                int row = p_row.getValue();
                int col = 0;
                Row theRow = getRow(theSheet, row);
                String localeName = (String) localeIter.next();
                ProjectWorkflowData data = (ProjectWorkflowData) localeMap
                        .get(localeName);
                CellStyle temp_dateStyle = getDateStyle(p_workbook);
                CellStyle temp_moneyStyle = getMoneyStyle(p_workbook);
                CellStyle temp_normalStyle = getContentStyle(p_workbook);
                // WritableCellFormat temp_wordCountValueRightFormat =
                // wordCountValueRightFormat;
                if (data.wasExportFailed)
                {
                	temp_dateStyle = getFailedDateStyle(p_workbook);
                    temp_moneyStyle = getFailedMoneyStyle(p_workbook);
                    temp_normalStyle = getRedCellStyle(p_workbook);

                }
                Cell cell_A = getCell(theRow, col++);
                cell_A.setCellValue(data.jobId);
                
                Cell cell_B = getCell(theRow, col++);
                cell_B.setCellValue(data.jobName);
                if (isWrongJob)
                {
                	cell_A.setCellStyle(getWrongJobStyle(p_workbook));
                	cell_B.setCellStyle(getWrongJobStyle(p_workbook));
                }
                else
                {
                	cell_A.setCellStyle(temp_normalStyle);
                	cell_B.setCellStyle(temp_normalStyle);
                }
                theSheet.setColumnWidth(col - 2, 5 * 256);
                theSheet.setColumnWidth(col - 1, 50 * 256);
                Cell cell_C = getCell(theRow, col++);
            	cell_C.setCellValue(data.poNumber);
            	cell_C.setCellStyle(temp_normalStyle); // PO number
            	
            	Cell cell_D = getCell(theRow, col++);
            	cell_D.setCellValue(data.projectDesc);
            	cell_D.setCellStyle(temp_normalStyle);
                theSheet.setColumnWidth(col - 1, 22 * 256);
                /* data.creationDate.toString())); */
                Cell cell_E = getCell(theRow, col++);
            	cell_E.setCellValue(data.creationDate);
            	cell_E.setCellStyle(temp_dateStyle);
                theSheet.setColumnWidth(col - 1, 15 * 256);
                
                Cell cell_F = getCell(theRow, col++);
                cell_F.setCellValue(data.targetLang);
                cell_F.setCellStyle(temp_normalStyle);
            	
            	Cell cell_G = getCell(theRow, col++);
            	cell_G.setCellValue(data.dellInternalRepsWordCount);
            	cell_G.setCellStyle(temp_normalStyle);
                int numwidth = 10;
                theSheet.setColumnWidth(col - 1, numwidth * 256);
                Cell cell_H = getCell(theRow, col++);
            	cell_H.setCellValue(data.dellExactMatchWordCount);
            	cell_H.setCellStyle(temp_normalStyle);
                theSheet.setColumnWidth(col - 1, numwidth * 256);

                if (p_data.headers[0] != null)
                {
                	Cell cell_InContext = getCell(theRow, col++);
                	cell_InContext.setCellValue(data.dellInContextMatchWordCount);
                	cell_InContext.setCellStyle(temp_normalStyle);
                    theSheet.setColumnWidth(col - 1, numwidth * 256);
                }

                Cell cell_FuzzyMatch = getCell(theRow, col++);
                cell_FuzzyMatch.setCellValue(data.dellFuzzyMatchWordCount);
                cell_FuzzyMatch.setCellStyle(temp_normalStyle);
                theSheet.setColumnWidth(col - 1, numwidth * 256);

                Cell cell_NewWords = getCell(theRow, col++);
            	cell_NewWords.setCellValue(data.dellNewWordsWordCount);
            	cell_NewWords.setCellStyle(temp_normalStyle);
                theSheet.setColumnWidth(col - 1, numwidth * 256);
                
                Cell cell_Total = getCell(theRow, col++);
                cell_Total.setCellValue(data.dellTotalWordCount);
                cell_Total.setCellStyle(temp_normalStyle);
                theSheet.setColumnWidth(col - 1, numwidth * 256);

                int moneywidth = 12;
                Cell cell_InternalReps = getCell(theRow, col++);
                cell_InternalReps.setCellValue(asDouble(data.dellInternalRepsWordCountCost));
                cell_InternalReps.setCellStyle(temp_moneyStyle);
                theSheet.setColumnWidth(col - 1, moneywidth * 256);
                
                Cell cell_ExactMatch = getCell(theRow, col++);
                cell_ExactMatch.setCellValue(asDouble(data.dellExactMatchWordCountCost));
                cell_ExactMatch.setCellStyle(temp_moneyStyle);
                theSheet.setColumnWidth(col - 1, moneywidth * 256);

                if (p_data.headers[0] != null)
                {
                	Cell cell_InContext = getCell(theRow, col++);
                	cell_InContext.setCellValue(asDouble(data.dellInContextMatchWordCountCost));
                	cell_InContext.setCellStyle(temp_moneyStyle);
                    theSheet.setColumnWidth(col - 1, moneywidth * 256);
                }

                Cell cell_FuzzyMatch_Invoice = getCell(theRow, col++);
                cell_FuzzyMatch_Invoice.setCellValue(asDouble(data.dellFuzzyMatchWordCountCost));
                cell_FuzzyMatch_Invoice.setCellStyle(temp_moneyStyle);
                theSheet.setColumnWidth(col - 1, moneywidth * 256);

                Cell cell_NewWords_Invoice = getCell(theRow, col++);
                cell_NewWords_Invoice.setCellValue(asDouble(data.dellNewWordsWordCountCost));
                cell_NewWords_Invoice.setCellStyle(temp_moneyStyle);
                theSheet.setColumnWidth(col - 1, moneywidth * 256);
                
                Cell cell_Total_Invoice = getCell(theRow, col++);
                cell_Total_Invoice.setCellValue(asDouble(data.dellTotalWordCountCost));
                cell_Total_Invoice.setCellStyle(temp_moneyStyle);
                theSheet.setColumnWidth(col - 1, moneywidth * 256);

                p_row.inc();
            }
        }

        p_row.inc();
        p_row.inc();
        addTotalsForDellMatches(p_workbook, p_data, p_row, bundle);

    }

    /** Adds the totals and sub-total formulas */
    private void addTotalsForDellMatches(Workbook p_workbook, MyData p_data, IntHolder p_row,
            ResourceBundle bundle) throws Exception
    {
        Sheet theSheet = p_data.dellSheet;
        int row = p_row.getValue() + 1; // skip a row
        
        String title = bundle.getString("lb_totals");
        Row theRow = getRow(theSheet, row);
        Cell cell_A = getCell(theRow, 0);
        cell_A.setCellValue(title);
        cell_A.setCellStyle(getSubTotalStyle(p_workbook));

        theSheet.addMergedRegion(new CellRangeAddress(row, row, 0, 5));
        setRegionStyle(theSheet, new CellRangeAddress(row, row, 0, 5), 
        		getSubTotalStyle(p_workbook));
        int lastRow = p_row.getValue() - 2;

        // add in word count totals
        int c = 6;
        if (p_data.headers[0] != null)
        {
            // word counts
        	Cell cell_G = getCell(theRow, c++);
        	cell_G.setCellFormula("SUM(G5:G" + lastRow + ")");
        	cell_G.setCellStyle(getSubTotalStyle(p_workbook));
        	
        	Cell cell_H = getCell(theRow, c++);
        	cell_H.setCellFormula("SUM(H5:H" + lastRow + ")");
        	cell_H.setCellStyle(getSubTotalStyle(p_workbook));
        	
        	Cell cell_I = getCell(theRow, c++);
        	cell_I.setCellFormula("SUM(I5:I" + lastRow + ")");
        	cell_I.setCellStyle(getSubTotalStyle(p_workbook));
        	
        	Cell cell_J = getCell(theRow, c++);
        	cell_J.setCellFormula("SUM(J5:J" + lastRow + ")");
        	cell_J.setCellStyle(getSubTotalStyle(p_workbook));
        	
        	Cell cell_K = getCell(theRow, c++);
        	cell_K.setCellFormula("SUM(K5:K" + lastRow + ")");
        	cell_K.setCellStyle(getSubTotalStyle(p_workbook));
        	
        	Cell cell_L = getCell(theRow, c++);
        	cell_L.setCellFormula("SUM(L5:L" + lastRow + ")");
        	cell_L.setCellStyle(getSubTotalStyle(p_workbook));
        	// word count costs
        	Cell cell_M = getCell(theRow, c++);
        	cell_M.setCellFormula("SUM(M5:M" + lastRow + ")");
        	cell_M.setCellStyle(getTotalMoneyStyle(p_workbook));
        	
        	Cell cell_N = getCell(theRow, c++);
        	cell_N.setCellFormula("SUM(N5:N" + lastRow + ")");
        	cell_N.setCellStyle(getTotalMoneyStyle(p_workbook));
        	
        	Cell cell_O = getCell(theRow, c++);
        	cell_O.setCellFormula("SUM(O5:O" + lastRow + ")");
        	cell_O.setCellStyle(getTotalMoneyStyle(p_workbook));
        	
        	Cell cell_P = getCell(theRow, c++);
        	cell_P.setCellFormula("SUM(P5:P" + lastRow + ")");
        	cell_P.setCellStyle(getTotalMoneyStyle(p_workbook));
        	
        	Cell cell_Q = getCell(theRow, c++);
        	cell_Q.setCellFormula("SUM(Q5:Q" + lastRow + ")");
        	cell_Q.setCellStyle(getTotalMoneyStyle(p_workbook));
        	
        	Cell cell_R = getCell(theRow, c++);
        	cell_R.setCellFormula("SUM(R5:R" + lastRow + ")");
        	cell_R.setCellStyle(getTotalMoneyStyle(p_workbook));
        }
        else
        {
        	 // word counts
        	Cell cell_G = getCell(theRow, c++);
        	cell_G.setCellFormula("SUM(G5:G" + lastRow + ")");
        	cell_G.setCellStyle(getSubTotalStyle(p_workbook));
        	
        	Cell cell_H = getCell(theRow, c++);
        	cell_H.setCellFormula("SUM(H5:H" + lastRow + ")");
        	cell_H.setCellStyle(getSubTotalStyle(p_workbook));
        	
        	Cell cell_I = getCell(theRow, c++);
        	cell_I.setCellFormula("SUM(I5:I" + lastRow + ")");
        	cell_I.setCellStyle(getSubTotalStyle(p_workbook));
        	
        	Cell cell_J = getCell(theRow, c++);
        	cell_J.setCellFormula("SUM(J5:J" + lastRow + ")");
        	cell_J.setCellStyle(getSubTotalStyle(p_workbook));
        	
        	Cell cell_K = getCell(theRow, c++);
        	cell_K.setCellFormula("SUM(K5:K" + lastRow + ")");
        	cell_K.setCellStyle(getSubTotalStyle(p_workbook));
        	// word count costs
        	Cell cell_L = getCell(theRow, c++);
        	cell_L.setCellFormula("SUM(L5:L" + lastRow + ")");
        	cell_L.setCellStyle(getTotalMoneyStyle(p_workbook));
        	
        	Cell cell_M = getCell(theRow, c++);
        	cell_M.setCellFormula("SUM(M5:M" + lastRow + ")");
        	cell_M.setCellStyle(getTotalMoneyStyle(p_workbook));
        	
        	Cell cell_N = getCell(theRow, c++);
        	cell_N.setCellFormula("SUM(N5:N" + lastRow + ")");
        	cell_N.setCellStyle(getTotalMoneyStyle(p_workbook));
        	
        	Cell cell_O = getCell(theRow, c++);
        	cell_O.setCellFormula("SUM(O5:O" + lastRow + ")");
        	cell_O.setCellStyle(getTotalMoneyStyle(p_workbook));
        	
        	Cell cell_P = getCell(theRow, c++);
        	cell_P.setCellFormula("SUM(P5:P" + lastRow + ")");
        	cell_P.setCellStyle(getTotalMoneyStyle(p_workbook));
        }
    }

    public void writeProjectDataForTradosMatches(Workbook p_workbook, HashMap p_projectMap,
            IntHolder p_row, MyData p_data) throws Exception
    {
        Sheet theSheet = p_data.tradosSheet;
        ArrayList projects = new ArrayList(p_projectMap.keySet());
        SortUtil.sort(projects);
        Iterator projectIter = projects.iterator();

        while (projectIter.hasNext())
        {
            String jobName = (String) projectIter.next();
            boolean isWrongJob = p_data.wrongJobNames.contains(jobName);
            HashMap localeMap = (HashMap) p_projectMap.get(jobName);
            ArrayList locales = new ArrayList(localeMap.keySet());
            SortUtil.sort(locales);
            Iterator localeIter = locales.iterator();
            BigDecimal projectTotalWordCountCost = new BigDecimal(
                    BIG_DECIMAL_ZERO_STRING);
            while (localeIter.hasNext())
            {
                int row = p_row.getValue();
                int col = 0;
                Row theRow = getRow(theSheet, row);
                String localeName = (String) localeIter.next();
                ProjectWorkflowData data = (ProjectWorkflowData) localeMap
                        .get(localeName);

                CellStyle temp_dateStyle = getDateStyle(p_workbook);
                CellStyle temp_moneyStyle = getMoneyStyle(p_workbook);
                CellStyle temp_normalStyle = getContentStyle(p_workbook);
                // WritableCellFormat temp_wordCountValueRightFormat =
                // wordCountValueRightFormat;
                if (data.wasExportFailed)
                {
                	temp_dateStyle = getFailedDateStyle(p_workbook);
                    temp_moneyStyle = getFailedMoneyStyle(p_workbook);
                    temp_normalStyle = getRedCellStyle(p_workbook);
                }

                Cell cell_A = getCell(theRow, col++);
                cell_A.setCellValue(data.jobId);
                
                Cell cell_B = getCell(theRow, col++);
                cell_B.setCellValue(data.jobName);
                if (isWrongJob)
                {
                	cell_A.setCellStyle(getWrongJobStyle(p_workbook));
                	cell_B.setCellStyle(getWrongJobStyle(p_workbook));
                }
                else
                {
                	cell_A.setCellStyle(temp_normalStyle);;
                	cell_B.setCellStyle(temp_normalStyle);
                }
                theSheet.setColumnWidth(col - 2, 5 * 256);
                theSheet.setColumnWidth(col - 1, 50 * 256);
                Cell cell_C = getCell(theRow, col++);
            	cell_C.setCellValue(data.poNumber);
            	cell_C.setCellStyle(temp_normalStyle); // PO number
            	
            	Cell cell_D = getCell(theRow, col++);
            	cell_D.setCellValue(data.projectDesc);
            	cell_D.setCellStyle(temp_normalStyle);
                theSheet.setColumnWidth(col - 1, 22 * 256);
                /* data.creationDate.toString())); */
                Cell cell_E = getCell(theRow, col++);
            	cell_E.setCellValue(data.creationDate);
            	cell_E.setCellStyle(temp_dateStyle);
                theSheet.setColumnWidth(col - 1, 15 * 256);
                
                Cell cell_F = getCell(theRow, col++);
            	cell_F.setCellValue(data.targetLang);
            	cell_F.setCellStyle(temp_normalStyle);
            	
            	Cell cell_G = getCell(theRow, col++);
            	cell_G.setCellValue(data.trados100WordCount);
            	cell_G.setCellStyle(temp_normalStyle);
                int numwidth = 10;
                theSheet.setColumnWidth(col - 1, numwidth * 256);
                
                Cell cell_H = getCell(theRow, col++);
            	cell_H.setCellValue(data.trados95to99WordCount);
            	cell_H.setCellStyle(temp_normalStyle);
                theSheet.setColumnWidth(col - 1, numwidth * 256);
                
                Cell cell_I = getCell(theRow, col++);
                cell_I.setCellValue(data.trados85to94WordCount);
                cell_I.setCellStyle(temp_normalStyle);
                theSheet.setColumnWidth(col - 1, numwidth * 256);
                
                Cell cell_J = getCell(theRow, col++);
                cell_J.setCellValue(data.trados75to84WordCount);
                cell_J.setCellStyle(temp_normalStyle);
                theSheet.setColumnWidth(col - 1, numwidth * 256);
                
                Cell cell_K = getCell(theRow, col++);
            	cell_K.setCellValue(data.tradosNoMatchWordCount
                        + data.trados50to74WordCount);
            	cell_K.setCellStyle(temp_normalStyle);
                theSheet.setColumnWidth(col - 1, numwidth * 256);
                
                Cell cell_L = getCell(theRow, col++);
            	cell_L.setCellValue(data.tradosRepsWordCount);
            	cell_L.setCellStyle(temp_normalStyle);
                theSheet.setColumnWidth(col - 1, numwidth * 256);
                if (p_data.headers[0] != null)
                {
                	Cell cell_InContext = getCell(theRow, col++);
                	cell_InContext.setCellValue(data.tradosInContextMatchWordCount);
                	cell_InContext.setCellStyle(temp_normalStyle);
                    theSheet.setColumnWidth(col - 1, numwidth * 256);
                }

                Cell cell_Total = getCell(theRow, col++);
                cell_Total.setCellValue(data.tradosTotalWordCount);
                cell_Total.setCellStyle(temp_normalStyle);
                theSheet.setColumnWidth(col - 1, numwidth * 256);

                int moneywidth = 12;
                Cell cell_100Cost = getCell(theRow, col++);
                cell_100Cost.setCellValue(asDouble(data.trados100WordCountCost));
                cell_100Cost.setCellStyle(temp_moneyStyle);
                theSheet.setColumnWidth(col - 1, moneywidth * 256);

                Cell cell_95_99 = getCell(theRow, col++);
                cell_95_99.setCellValue(asDouble(data.trados95to99WordCountCost));
            	cell_95_99.setCellStyle(temp_moneyStyle);
                theSheet.setColumnWidth(col - 1, moneywidth * 256);

                Cell cell_85_94 = getCell(theRow, col++);
                cell_85_94.setCellValue(asDouble(data.trados85to94WordCountCost));
                cell_85_94.setCellStyle(temp_moneyStyle);
                theSheet.setColumnWidth(col - 1, moneywidth * 256);

                Cell cell_75_84 = getCell(theRow, col++);
                cell_75_84.setCellValue(asDouble(data.trados75to84WordCountCost));
                cell_75_84.setCellStyle(temp_moneyStyle);
                theSheet.setColumnWidth(col - 1, moneywidth * 256);
                
                Cell cell_NoMatch = getCell(theRow, col++);
                cell_NoMatch.setCellValue(asDouble(data.tradosNoMatchWordCountCost));
                cell_NoMatch.setCellStyle(temp_moneyStyle);
                theSheet.setColumnWidth(col - 1, moneywidth * 256);

                Cell cell_Reps = getCell(theRow, col++);
                cell_Reps.setCellValue(asDouble(data.tradosRepsWordCountCost));
                cell_Reps.setCellStyle(temp_moneyStyle);
                theSheet.setColumnWidth(col - 1, moneywidth * 256);

                if (p_data.headers[0] != null)
                {
                	Cell cell_InContext = getCell(theRow, col++);
                	cell_InContext.setCellValue(asDouble(data.tradosInContextWordCountCost));
                	cell_InContext.setCellStyle(temp_moneyStyle);
                    theSheet.setColumnWidth(col - 1, moneywidth * 256);
                }
                
                Cell cell_TotalCost = getCell(theRow, col++);
                cell_TotalCost.setCellValue(asDouble(data.tradosTotalWordCountCost));
                cell_TotalCost.setCellStyle(temp_moneyStyle);
                theSheet.setColumnWidth(col - 1, moneywidth * 256);

                p_row.inc();
            }
        }

        p_row.inc();
        p_row.inc();
        addTotalsForTradosMatches(p_workbook, p_data, p_row, bundle);
    }

    /** Adds the totals and sub-total formulas */
    private void addTotalsForTradosMatches(Workbook p_workbook, MyData p_data, 
    		IntHolder p_row, ResourceBundle bundle) throws Exception
    {
        Sheet theSheet = p_data.tradosSheet;
        int row = p_row.getValue() + 1; // skip a row
        
        String title = bundle.getString("lb_totals");
        
        Row theRow = getRow(theSheet, row);
        Cell cell_A = getCell(theRow, 0);
        cell_A.setCellValue(title);
        cell_A.setCellStyle(getSubTotalStyle(p_workbook));
        theSheet.addMergedRegion(new CellRangeAddress(row, row, 0, 5));
        setRegionStyle(theSheet, new CellRangeAddress(row, row, 0, 5), 
        		getSubTotalStyle(p_workbook));
        
        int lastRow = p_row.getValue() - 2;

        // add in word count totals
        int c = 6;
        if (p_data.headers[0] != null)
        {
        	// word counts
        	Cell cell_G = getCell(theRow, c++);
        	cell_G.setCellFormula("SUM(G5:G" + lastRow + ")");
        	cell_G.setCellStyle(getSubTotalStyle(p_workbook));
        	
        	Cell cell_H = getCell(theRow, c++);
        	cell_H.setCellFormula("SUM(H5:H" + lastRow + ")");
        	cell_H.setCellStyle(getSubTotalStyle(p_workbook));
        	
        	Cell cell_I = getCell(theRow, c++);
        	cell_I.setCellFormula("SUM(I5:I" + lastRow + ")");
        	cell_I.setCellStyle(getSubTotalStyle(p_workbook));
        	
        	Cell cell_J = getCell(theRow, c++);
        	cell_J.setCellFormula("SUM(J5:J" + lastRow + ")");
        	cell_J.setCellStyle(getSubTotalStyle(p_workbook));
        	
        	Cell cell_K = getCell(theRow, c++);
        	cell_K.setCellFormula("SUM(K5:K" + lastRow + ")");
        	cell_K.setCellStyle(getSubTotalStyle(p_workbook));
        	
        	Cell cell_L = getCell(theRow, c++);
        	cell_L.setCellFormula("SUM(L5:L" + lastRow + ")");
        	cell_L.setCellStyle(getSubTotalStyle(p_workbook));
        	
        	Cell cell_M = getCell(theRow, c++);
        	cell_M.setCellFormula("SUM(M5:M" + lastRow + ")");
        	cell_M.setCellStyle(getSubTotalStyle(p_workbook));
        	
        	Cell cell_N = getCell(theRow, c++);
        	cell_N.setCellFormula("SUM(N5:N" + lastRow + ")");
        	cell_N.setCellStyle(getSubTotalStyle(p_workbook));
        	// word count costs
        	Cell cell_O = getCell(theRow, c++);
        	cell_O.setCellFormula("SUM(O5:O" + lastRow + ")");
        	cell_O.setCellStyle(getTotalMoneyStyle(p_workbook));
        	
        	Cell cell_P = getCell(theRow, c++);
        	cell_P.setCellFormula("SUM(P5:P" + lastRow + ")");
        	cell_P.setCellStyle(getTotalMoneyStyle(p_workbook));
        	
        	Cell cell_Q = getCell(theRow, c++);
        	cell_Q.setCellFormula("SUM(Q5:Q" + lastRow + ")");
        	cell_Q.setCellStyle(getTotalMoneyStyle(p_workbook));
        	
        	Cell cell_R = getCell(theRow, c++);
        	cell_R.setCellFormula("SUM(R5:R" + lastRow + ")");
        	cell_R.setCellStyle(getTotalMoneyStyle(p_workbook));
        	
        	Cell cell_S = getCell(theRow, c++);
        	cell_S.setCellFormula("SUM(S5:S" + lastRow + ")");
        	cell_S.setCellStyle(getTotalMoneyStyle(p_workbook));
        	
        	Cell cell_T = getCell(theRow, c++);
        	cell_T.setCellFormula("SUM(T5:T" + lastRow + ")");
        	cell_T.setCellStyle(getTotalMoneyStyle(p_workbook));
        	
        	Cell cell_U = getCell(theRow, c++);
        	cell_U.setCellFormula("SUM(U5:U" + lastRow + ")");
        	cell_U.setCellStyle(getTotalMoneyStyle(p_workbook));
        	
        	Cell cell_V = getCell(theRow, c++);
        	cell_V.setCellFormula("SUM(V5:V" + lastRow + ")");
        	cell_V.setCellStyle(getTotalMoneyStyle(p_workbook));
        }
        else
        {
        	// word counts
        	Cell cell_G = getCell(theRow, c++);
        	cell_G.setCellFormula("SUM(G5:G" + lastRow + ")");
        	cell_G.setCellStyle(getSubTotalStyle(p_workbook));
        	
        	Cell cell_H = getCell(theRow, c++);
        	cell_H.setCellFormula("SUM(H5:H" + lastRow + ")");
        	cell_H.setCellStyle(getSubTotalStyle(p_workbook));
        	
        	Cell cell_I = getCell(theRow, c++);
        	cell_I.setCellFormula("SUM(I5:I" + lastRow + ")");
        	cell_I.setCellStyle(getSubTotalStyle(p_workbook));
        	
        	Cell cell_J = getCell(theRow, c++);
        	cell_J.setCellFormula("SUM(J5:J" + lastRow + ")");
        	cell_J.setCellStyle(getSubTotalStyle(p_workbook));
        	
        	Cell cell_K = getCell(theRow, c++);
        	cell_K.setCellFormula("SUM(K5:K" + lastRow + ")");
        	cell_K.setCellStyle(getSubTotalStyle(p_workbook));
        	
        	Cell cell_L = getCell(theRow, c++);
        	cell_L.setCellFormula("SUM(L5:L" + lastRow + ")");
        	cell_L.setCellStyle(getSubTotalStyle(p_workbook));
        	
        	Cell cell_M = getCell(theRow, c++);
        	cell_M.setCellFormula("SUM(M5:M" + lastRow + ")");
        	cell_M.setCellStyle(getSubTotalStyle(p_workbook));
        	// word count costs
        	Cell cell_N = getCell(theRow, c++);
        	cell_N.setCellFormula("SUM(N5:N" + lastRow + ")");
        	cell_N.setCellStyle(getSubTotalStyle(p_workbook));
        	
        	Cell cell_O = getCell(theRow, c++);
        	cell_O.setCellFormula("SUM(O5:O" + lastRow + ")");
        	cell_O.setCellStyle(getTotalMoneyStyle(p_workbook));
        	
        	Cell cell_P = getCell(theRow, c++);
        	cell_P.setCellFormula("SUM(P5:P" + lastRow + ")");
        	cell_P.setCellStyle(getTotalMoneyStyle(p_workbook));
        	
        	Cell cell_Q = getCell(theRow, c++);
        	cell_Q.setCellFormula("SUM(Q5:Q" + lastRow + ")");
        	cell_Q.setCellStyle(getTotalMoneyStyle(p_workbook));
        	
        	Cell cell_R = getCell(theRow, c++);
        	cell_R.setCellFormula("SUM(R5:R" + lastRow + ")");
        	cell_R.setCellStyle(getTotalMoneyStyle(p_workbook));
        	
        	Cell cell_S = getCell(theRow, c++);
        	cell_S.setCellFormula("SUM(S5:S" + lastRow + ")");
        	cell_S.setCellStyle(getTotalMoneyStyle(p_workbook));
        	
        	Cell cell_T = getCell(theRow, c++);
        	cell_T.setCellFormula("SUM(T5:T" + lastRow + ")");
        	cell_T.setCellStyle(getTotalMoneyStyle(p_workbook));
        }
    }

    private void writeParamsSheet(Workbook p_workbook, Sheet paramsSheet,
    		MyData p_data,HttpServletRequest p_request) throws Exception
    {
    	Row firRow = getRow(paramsSheet, 0);
        Row secRow = getRow(paramsSheet, 1);
        Row thirRow = getRow(paramsSheet, 2);
        Cell cell_A_Title = getCell(firRow, 0);
        cell_A_Title.setCellValue(bundle.getString("lb_report_criteria"));
        cell_A_Title.setCellStyle(getContentStyle(p_workbook));
        paramsSheet.setColumnWidth(0, 50 * 256);

        Cell cell_A_Header = getCell(secRow, 0);
        if (p_data.wantsAllProjects)
        {
        	cell_A_Header.setCellValue(bundle
                    .getString("lb_selected_projects")
                    + " "
                    + bundle.getString("all"));
        	cell_A_Header.setCellStyle(getContentStyle(p_workbook));
        }
        else
        {
        	cell_A_Header.setCellValue(bundle
                    .getString("lb_selected_projects"));
        	cell_A_Header.setCellStyle(getContentStyle(p_workbook));
            Iterator<Long> iter = p_data.projectIdList.iterator();
            int r = 2;
            while (iter.hasNext())
            {
                Long pid = (Long) iter.next();
                String projectName = "??";
                try
                {
                    Project p = ServerProxy.getProjectHandler().getProjectById(
                            pid.longValue());
                    projectName = p.getName();
                }
                catch (Exception e)
                {
                }
                String v = projectName + " ("
                        + bundle.getString("lb_report_id") + "="
                        + pid.toString() + ")";
                Row theRow = getRow(paramsSheet, r);
                Cell cell_A = getCell(theRow, 0);
                cell_A.setCellValue(v);
                cell_A.setCellStyle(getContentStyle(p_workbook));
                r++;
            }
        }

        // add the date criteria
        String paramCreateDateStartCount = p_request
                .getParameter(JobSearchConstants.CREATION_START);
//        String paramCreateDateStartOpts = p_request
//                .getParameter(JobSearchConstants.CREATION_START_OPTIONS);
        String paramCreateDateEndCount = p_request
                .getParameter(JobSearchConstants.CREATION_END);
//        String paramCreateDateEndOpts = p_request
//                .getParameter(JobSearchConstants.CREATION_END_OPTIONS);
        Cell cell_B_Header = getCell(secRow, 1);
        cell_B_Header.setCellValue(bundle.getString("lb_from") + ":");
        cell_B_Header.setCellStyle(getContentStyle(p_workbook));
//        String fromMsg = paramCreateDateStartCount
//                + " "
//                + getDateCritieraConditionValue(
//                        paramCreateDateStartOpts);
//        String untilMsg = paramCreateDateEndCount
//                + " "
//                + getDateCritieraConditionValue(
//                        paramCreateDateEndOpts);
        Cell cell_B = getCell(thirRow, 1);
        cell_B.setCellValue(paramCreateDateStartCount);
        cell_B.setCellStyle(getContentStyle(p_workbook));
        
        Cell cell_C_Header = getCell(secRow, 2);
        cell_C_Header.setCellValue(bundle.getString("lb_until") + ":");
        cell_C_Header.setCellStyle(getContentStyle(p_workbook));
        
        Cell cell_C = getCell(thirRow, 2);
        cell_C.setCellValue(paramCreateDateEndCount);
        cell_C.setCellStyle(getContentStyle(p_workbook));

        // add the target lang criteria
        Cell cell_D_Header = getCell(secRow, 3);
        if (p_data.wantsAllTargetLangs)
        {
        	cell_D_Header.setCellValue(bundle
                    .getString("lb_selected_langs")
                    + " "
                    + bundle.getString("all"));
        	cell_D_Header.setCellStyle(getContentStyle(p_workbook));
        }
        else
        {
        	cell_D_Header.setCellValue(bundle
                    .getString("lb_selected_langs"));
        	cell_D_Header.setCellStyle(getContentStyle(p_workbook));
            Iterator<String> iter = p_data.targetLangList.iterator();
            int r = 2;
            LocaleManagerLocal manager = new LocaleManagerLocal();
            while (iter.hasNext())
            {
                String lang = iter.next();
                Row theRow = getRow(paramsSheet, r);
                Cell cell_D = getCell(theRow, 3);
                cell_D.setCellValue(manager
                		.getLocaleById(Long.valueOf(lang)).toString());
                cell_D.setCellStyle(getContentStyle(p_workbook));
                r++;
            }
            paramsSheet.setColumnWidth(3, 15 * 256);
        }
    }
    private String getDateCritieraConditionValue(String p_condition)
    {
        String value = "N/A";
        if (SearchCriteriaParameters.NOW.equals(p_condition))
            value = bundle.getString("lb_now");
        else if (SearchCriteriaParameters.DAYS_AGO.equals(p_condition))
            value = bundle.getString("lb_days_ago");
        else if (SearchCriteriaParameters.HOURS_AGO.equals(p_condition))
            value = bundle.getString("lb_hours_ago");
        else if (SearchCriteriaParameters.WEEKS_AGO.equals(p_condition))
            value = bundle.getString("lb_weeks_ago");
        else if (SearchCriteriaParameters.MONTHS_AGO.equals(p_condition))
            value = bundle.getString("lb_months_ago");
        return value;
    }

    /**
     * Adds data for the exported and in-progress jobs. Archived jobs are
     * ignored for now.
     * 
     * @return HashMap
     * @exception Exception
     */

    private HashMap getProjectData(HttpServletRequest p_request, HttpServletResponse p_response,
            boolean p_recalculateFinishedWorkflow, MyData p_data)
            throws Exception
    {
        ArrayList queriedJobs = new ArrayList();
        if (p_data.wantsAllJobNames)
        {
            queriedJobs.addAll(ServerProxy.getJobHandler().getJobs(
                    getSearchParams(p_request, p_data)));
            // sort jobs by job name
            SortUtil.sort(queriedJobs, new JobComparator(Locale.US));
        }
        else
        {
            for (int i = 0; i < p_data.jobIdList.size(); i++)
            {
                Job j = ServerProxy.getJobHandler().getJobById(
                        p_data.jobIdList.get(i));
                queriedJobs.add(j);
            }
        }

        ArrayList<Job> wrongJobs = getWrongJobs(p_data);
        // now create a Set of all the jobs
        HashSet<Job> jobs = new HashSet<Job>();
        jobs.addAll(queriedJobs);
        jobs.addAll(wrongJobs);
        jobs.removeAll(p_data.ignoreJobs);

        p_data.headers = getHeaders(jobs);

        m_jobIDS = ReportHelper.getJobIDS(new ArrayList<Job>(jobs));
        // Cancel Duplicate Request
        if (ReportHelper.checkReportsDataInProgressStatus(userId,
                m_jobIDS, getReportType()))
        {
            p_response.sendError(p_response.SC_NO_CONTENT);
            return null;
        }
        // Set ReportsData.
        ReportHelper.setReportsData(userId, m_jobIDS, getReportType(),
                0, ReportsData.STATUS_INPROGRESS);
        
        // first iterate through the Jobs and group by Project/workflow because
        // Dell
        // doesn't want to see actual Jobs
        HashMap projectMap = new HashMap();
        Currency pivotCurrency = ServerProxy.getCostingEngine()
                .getCurrencyByName(ReportUtil.getCurrencyName(currency),
                        CompanyThreadLocal.getInstance().getValue());
        for(Job j: jobs)
        {
            if (isCancelled())
            {
                p_response.sendError(p_response.SC_NO_CONTENT);
                return null;
            }
            // only handle jobs in these states
            if (!(Job.READY_TO_BE_DISPATCHED.equals(j.getState())
                    || Job.DISPATCHED.equals(j.getState())
                    || Job.EXPORTED.equals(j.getState())
                    || Job.EXPORT_FAIL.equals(j.getState())
                    || Job.ARCHIVED.equals(j.getState()) || Job.LOCALIZED
                        .equals(j.getState())))
            {
                continue;
            }

            Collection<Workflow> c = j.getWorkflows();
            Iterator<Workflow> wfIter = c.iterator();
            String projectDesc = getProjectDesc(p_data, j);
            while (wfIter.hasNext())
            {
                Workflow w = wfIter.next();
                // TranslationMemoryProfile tmProfile =
                // w.getJob().getL10nProfile().getTranslationMemoryProfile();
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
                // String targetLang =w.getTargetLocale().getLanguageCode();
                String targetLang = Long.toString(w.getTargetLocale().getId());

                if (!p_data.wantsAllTargetLangs
                        && !p_data.targetLangList.contains(targetLang))
                {
                    continue;
                }

                String jobName = j.getJobName();
                long jobId = j.getId();
                HashMap localeMap = (HashMap) projectMap.get(Long
                        .toString(jobId));
                if (localeMap == null)
                {
                    localeMap = new HashMap();
                    projectMap.put(Long.toString(jobId), localeMap);
                }

                ProjectWorkflowData data = (ProjectWorkflowData) localeMap
                        .get(targetLang);
                if (data == null)
                {
                    data = new ProjectWorkflowData();
                    localeMap.put(targetLang, data);
                    data.jobName = jobName;
                    data.jobId = jobId;
                    data.poNumber = j.getQuotePoNumber() == null ? "" : j
                            .getQuotePoNumber();
                    data.projectDesc = projectDesc;
                    data.targetLang = w.getTargetLocale().toString();
                    data.creationDate = j.getCreateDate();
                }
                if (Workflow.EXPORT_FAILED.equals(state))
                {
                    // if the workflow is EXPORT_FAILED, color the line in red
                    data.wasExportFailed = true;
                }
                // now add or amend the data in the ProjectWorkflowData based on
                // this next workflow
                if (j.getCreateDate().before(data.creationDate))
                    data.creationDate = j.getCreateDate();

                data.repetitionWordCount += w.getRepetitionWordCount();
                data.dellInternalRepsWordCount += w.getRepetitionWordCount();
                data.tradosRepsWordCount = data.dellInternalRepsWordCount;

                data.lowFuzzyMatchWordCount += w
                        .getThresholdLowFuzzyWordCount();
                data.medFuzzyMatchWordCount += w
                        .getThresholdMedFuzzyWordCount();
                data.medHiFuzzyMatchWordCount += w
                        .getThresholdMedHiFuzzyWordCount();
                data.hiFuzzyMatchWordCount += w.getThresholdHiFuzzyWordCount();

                // the Dell fuzzyMatchWordCount is the sum of the top 3
                // categories
                data.dellFuzzyMatchWordCount = data.medFuzzyMatchWordCount
                        + data.medHiFuzzyMatchWordCount
                        + data.hiFuzzyMatchWordCount;
                data.trados95to99WordCount = data.hiFuzzyMatchWordCount;
                data.trados85to94WordCount = data.medHiFuzzyMatchWordCount;
                data.trados75to84WordCount = data.medFuzzyMatchWordCount;
                data.trados50to74WordCount = data.lowFuzzyMatchWordCount;
                // no match word count, do not consider Threshold

                // add the lowest fuzzies and sublev match to nomatch
                data.dellNewWordsWordCount = w.getNoMatchWordCount()
                        + w.getLowFuzzyMatchWordCount();
                data.tradosNoMatchWordCount += w.getThresholdNoMatchWordCount();
                if (PageHandler.isInContextMatch(w.getJob()))
                {
                    data.segmentTmWordCount += w.getSegmentTmWordCount();
                    data.dellExactMatchWordCount += w.getSegmentTmWordCount();
                    data.dellInContextMatchWordCount += w
                            .getInContextMatchWordCount();
                }
                else
                {
                    data.segmentTmWordCount += w.getTotalExactMatchWordCount();
                    data.dellExactMatchWordCount += w
                            .getTotalExactMatchWordCount();
                    data.dellInContextMatchWordCount += w
                            .getNoUseInContextMatchWordCount();
                    data.contextMatchWordCount += 0;
                }
                data.trados100WordCount = data.dellExactMatchWordCount;
                data.tradosInContextMatchWordCount = data.dellInContextMatchWordCount;
                data.tradosContextMatchWordCount = data.contextMatchWordCount;
                data.dellContextMatchWordCount = data.contextMatchWordCount;
                data.totalWordCount += w.getTotalWordCount();

                data.dellTotalWordCount = data.dellFuzzyMatchWordCount
                        + data.dellInternalRepsWordCount
                        + data.dellExactMatchWordCount
                        + data.dellNewWordsWordCount
                        + data.dellInContextMatchWordCount
                        + data.dellContextMatchWordCount;
                data.tradosTotalWordCount = data.trados100WordCount
                        + data.tradosInContextMatchWordCount
                        + data.tradosContextMatchWordCount
                        + data.trados95to99WordCount
                        + data.trados85to94WordCount
                        + data.trados75to84WordCount
                        + data.trados50to74WordCount
                        + data.tradosNoMatchWordCount
                        + data.tradosRepsWordCount;

                Cost wfCost = ServerProxy.getCostingEngine().calculateCost(w,
                        pivotCurrency, p_recalculateFinishedWorkflow,
                        Cost.EXPENSE, p_recalculateFinishedWorkflow);
                CostByWordCount costByWordCount = wfCost.getCostByWordCount();
                if (costByWordCount != null)
                {
                    // repetition costs
                    data.repetitionWordCountCost = add(
                            data.repetitionWordCountCost,
                            costByWordCount.getRepetitionCost());
                    data.dellInternalRepsWordCountCost = data.repetitionWordCountCost;
                    data.tradosRepsWordCountCost = data.repetitionWordCountCost;

                    // exact match costs
                    if (PageHandler.isInContextMatch(w.getJob()))
                    {
                        // data.contextMatchWordCountCost =
                        // add(data.contextMatchWordCountCost,
                        // costByWordCount.getContextMatchCost());
                        data.inContextMatchWordCountCost = add(
                                data.inContextMatchWordCountCost,
                                costByWordCount.getInContextMatchCost());
                        data.segmentTmWordCountCost = add(
                                data.segmentTmWordCountCost,
                                costByWordCount.getSegmentTmMatchCost());
                    }
                    else
                    {
                        data.inContextMatchWordCountCost = add(
                                data.inContextMatchWordCountCost,
                                costByWordCount.getNoUseInContextMatchCost());
                        data.segmentTmWordCountCost = add(
                                data.segmentTmWordCountCost,
                                costByWordCount.getNoUseExactMatchCost());
                        data.contextMatchWordCountCost = add(new BigDecimal(
                                BIG_DECIMAL_ZERO_STRING),
                                costByWordCount.getContextMatchCost());
                    }
                    data.dellExactMatchWordCountCost = data.segmentTmWordCountCost;
                    data.trados100WordCountCost = data.segmentTmWordCountCost;
                    data.dellInContextMatchWordCountCost = data.inContextMatchWordCountCost;
                    data.tradosInContextWordCountCost = data.inContextMatchWordCountCost;
                    data.tradosContextWordCountCost = data.contextMatchWordCountCost;
                    data.dellContextMatchWordCountCost = data.contextMatchWordCountCost;
                    // fuzzy match costs
                    data.lowFuzzyMatchWordCountCost = add(
                            data.lowFuzzyMatchWordCountCost,
                            costByWordCount.getLowFuzzyMatchCost());
                    data.medFuzzyMatchWordCountCost = add(
                            data.medFuzzyMatchWordCountCost,
                            costByWordCount.getMedFuzzyMatchCost());
                    data.medHiFuzzyMatchWordCountCost = add(
                            data.medHiFuzzyMatchWordCountCost,
                            costByWordCount.getMedHiFuzzyMatchCost());
                    data.hiFuzzyMatchWordCountCost = add(
                            data.hiFuzzyMatchWordCountCost,
                            costByWordCount.getHiFuzzyMatchCost());
                    // Dell fuzzy match cost is the sum of the top three fuzzy
                    // match categories
                    data.dellFuzzyMatchWordCountCost = data.medFuzzyMatchWordCountCost
                            .add(data.medHiFuzzyMatchWordCountCost).add(
                                    data.hiFuzzyMatchWordCountCost);
                    // Trados breakdown for fuzzy
                    data.trados95to99WordCountCost = data.hiFuzzyMatchWordCountCost;
                    data.trados85to94WordCountCost = data.medHiFuzzyMatchWordCountCost;
                    data.trados75to84WordCountCost = data.medFuzzyMatchWordCountCost;
                    data.trados50to74WordCountCost = data.lowFuzzyMatchWordCountCost;

                    // new words, no match costs
                    data.noMatchWordCountCost = add(data.noMatchWordCountCost,
                            costByWordCount.getNoMatchCost());
                    data.tradosNoMatchWordCountCost = data.noMatchWordCountCost;
                    data.dellNewWordsWordCountCost = data.noMatchWordCountCost
                            .add(data.lowFuzzyMatchWordCountCost);

                    // totals
                    data.dellTotalWordCountCost = data.dellInternalRepsWordCountCost
                            .add(data.dellExactMatchWordCountCost)
                            .add(data.dellInContextMatchWordCountCost)
                            .add(data.dellContextMatchWordCountCost)
                            .add(data.dellFuzzyMatchWordCountCost)
                            .add(data.dellNewWordsWordCountCost);
                    data.totalWordCountCost = data.dellTotalWordCountCost;
                    data.tradosTotalWordCountCost = data.trados100WordCountCost
                            .add(data.tradosInContextWordCountCost)
                            .add(data.tradosContextWordCountCost)
                            .add(data.trados95to99WordCountCost)
                            .add(data.trados85to94WordCountCost)
                            .add(data.trados75to84WordCountCost)
                            .add(data.trados50to74WordCountCost)
                            .add(data.tradosRepsWordCountCost)
                            .add(data.tradosNoMatchWordCountCost);
                }
            }
            // now recalculate the job level cost if the work flow was
            // recalculated
            if (p_recalculateFinishedWorkflow)
            {
                ServerProxy.getCostingEngine().reCostJob(j, pivotCurrency,
                        Cost.EXPENSE);
            }
        }
        return projectMap;
    }

    private String[] getHeaders(HashSet<Job> jobs)
    {
        String[] headers = new String[1];
        for(Job job: jobs)
        {
            if (PageHandler.isInContextMatch(job))
            {
                headers[0] = "In Context Match";
            }
        }
        return headers;
    }

    /**
     * Class used to group data by project and target language
     */
    private class ProjectWorkflowData
    {
        public String jobName;
        public long jobId = -1;
        public String poNumber = "";
        public String projectDesc;
        public Date creationDate;
        public String targetLang;

        /* Dell values */
        public long dellInternalRepsWordCount = 0;
        public long dellExactMatchWordCount = 0;
        public long dellInContextMatchWordCount = 0;
        public long dellContextMatchWordCount = 0;
        public long dellFuzzyMatchWordCount = 0;
        public long dellNewWordsWordCount = 0;
        public long dellTotalWordCount = 0;

        // "Trados" values
        public long trados100WordCount = 0;
        public long tradosInContextMatchWordCount = 0;
        public long tradosContextMatchWordCount = 0;
        public long trados95to99WordCount = 0;
        public long trados85to94WordCount = 0;
        public long trados75to84WordCount = 0;
        public long trados50to74WordCount = 0;
        public long tradosNoMatchWordCount = 0;
        public long tradosRepsWordCount = 0;
        public long tradosTotalWordCount = 0;

        /* word counts */
        public long repetitionWordCount = 0;
        public long lowFuzzyMatchWordCount = 0;
        public long medFuzzyMatchWordCount = 0;
        public long medHiFuzzyMatchWordCount = 0;
        public long hiFuzzyMatchWordCount = 0;
        public long contextMatchWordCount = 0;
        public long segmentTmWordCount = 0;
        public long totalWordCount = 0;

        /* word count costs */
        public BigDecimal repetitionWordCountCost = new BigDecimal(
                BIG_DECIMAL_ZERO_STRING);
        public BigDecimal lowFuzzyMatchWordCountCost = new BigDecimal(
                BIG_DECIMAL_ZERO_STRING);
        public BigDecimal medFuzzyMatchWordCountCost = new BigDecimal(
                BIG_DECIMAL_ZERO_STRING);
        public BigDecimal medHiFuzzyMatchWordCountCost = new BigDecimal(
                BIG_DECIMAL_ZERO_STRING);
        public BigDecimal hiFuzzyMatchWordCountCost = new BigDecimal(
                BIG_DECIMAL_ZERO_STRING);
        public BigDecimal contextMatchWordCountCost = new BigDecimal(
                BIG_DECIMAL_ZERO_STRING);
        public BigDecimal inContextMatchWordCountCost = new BigDecimal(
                BIG_DECIMAL_ZERO_STRING);
        public BigDecimal segmentTmWordCountCost = new BigDecimal(
                BIG_DECIMAL_ZERO_STRING);
        public BigDecimal noMatchWordCountCost = new BigDecimal(
                BIG_DECIMAL_ZERO_STRING);
        public BigDecimal totalWordCountCost = new BigDecimal(
                BIG_DECIMAL_ZERO_STRING);

        /* Dell values */
        public BigDecimal dellInternalRepsWordCountCost = new BigDecimal(
                BIG_DECIMAL_ZERO_STRING);
        public BigDecimal dellExactMatchWordCountCost = new BigDecimal(
                BIG_DECIMAL_ZERO_STRING);
        public BigDecimal dellInContextMatchWordCountCost = new BigDecimal(
                BIG_DECIMAL_ZERO_STRING);
        public BigDecimal dellContextMatchWordCountCost = new BigDecimal(
                BIG_DECIMAL_ZERO_STRING);
        public BigDecimal dellFuzzyMatchWordCountCost = new BigDecimal(
                BIG_DECIMAL_ZERO_STRING);
        public BigDecimal dellNewWordsWordCountCost = new BigDecimal(
                BIG_DECIMAL_ZERO_STRING);
        public BigDecimal dellTotalWordCountCost = new BigDecimal(
                BIG_DECIMAL_ZERO_STRING);

        /* Trados values */
        public BigDecimal trados100WordCountCost = new BigDecimal(
                BIG_DECIMAL_ZERO_STRING);
        public BigDecimal tradosInContextWordCountCost = new BigDecimal(
                BIG_DECIMAL_ZERO_STRING);
        public BigDecimal tradosContextWordCountCost = new BigDecimal(
                BIG_DECIMAL_ZERO_STRING);
        public BigDecimal trados95to99WordCountCost = new BigDecimal(
                BIG_DECIMAL_ZERO_STRING);
        public BigDecimal trados85to94WordCountCost = new BigDecimal(
                BIG_DECIMAL_ZERO_STRING);
        public BigDecimal trados75to84WordCountCost = new BigDecimal(
                BIG_DECIMAL_ZERO_STRING);
        public BigDecimal trados50to74WordCountCost = new BigDecimal(
                BIG_DECIMAL_ZERO_STRING);
        public BigDecimal tradosRepsWordCountCost = new BigDecimal(
                BIG_DECIMAL_ZERO_STRING);
        public BigDecimal tradosTotalWordCountCost = new BigDecimal(
                BIG_DECIMAL_ZERO_STRING);
        public BigDecimal tradosNoMatchWordCountCost = new BigDecimal(
                BIG_DECIMAL_ZERO_STRING);

        public boolean wasExportFailed = false;

        public ProjectWorkflowData()
        {
        }
    }

    private void setJobNamesList(HttpServletRequest p_request, MyData p_data)
    {
        String[] jobIds = p_request.getParameterValues("jobId");
        for (int i = 0; i < jobIds.length; i++)
        {
            String id = jobIds[i];
            if ("*".equals(id))
            {
                p_data.wantsAllJobNames = true;
                break;
            }
            else
            {
                p_data.jobIdList.add(new Long(id));
            }

        }
    }

    private void setJobStatusList(HttpServletRequest p_request, MyData p_data)
    {
        String[] jobStatus = p_request.getParameterValues("status");
        for (int i = 0; i < jobStatus.length; i++)
        {
            String status = jobStatus[i];
            if ("*".equals(status))
            {
                p_data.wantsAllJobStatus = true;
                break;
            }
            else
            {
                p_data.jobStatusList.add(status);

            }
        }
    }

    private void setProjectIdList(HttpServletRequest p_request, MyData p_data)
    {
        // set the project Id
        String[] projectIds = p_request.getParameterValues("projectId");

        for (int i = 0; i < projectIds.length; i++)
        {
            String id = projectIds[i];
            if ("*".equals(id))
            {
                p_data.wantsAllProjects = true;
                try
                {
                    List<Project> projectList = (ArrayList<Project>) ServerProxy
                            .getProjectHandler().getProjectsByUser(userId);
                    for (Project project : projectList)
                    {
                        p_data.projectIdList.add(project.getIdAsLong());
                    }
                    break;
                }
                catch (Exception e)
                {
                }
            }
            else
            {
                p_data.projectIdList.add(new Long(id));
            }
        }
    }

    private void setTargetLangList(HttpServletRequest p_request, MyData p_data)
    {
        // set the target lang
        String[] targetLangs = p_request.getParameterValues("targetLang");
        for (int i = 0; i < targetLangs.length; i++)
        {
            String id = targetLangs[i];
            if (id.equals("*") == false)
                p_data.targetLangList.add(id);
            else
            {
                p_data.wantsAllTargetLangs = true;
                break;
            }
        }
    }

    /**
     * Returns search params used to find the jobs based on state
     * (READY,EXPORTED,LOCALIZED,DISPATCHED,export failed, archived), and
     * creation date during the range
     * 
     * @return JobSearchParams
     */
    private JobSearchParameters getSearchParams(HttpServletRequest p_request,
            MyData p_data)
    {
        JobSearchParameters sp = new JobSearchParameters();
        
        sp.setProjectId(p_data.projectIdList);

        if (!p_data.wantsAllJobStatus)
        {
            sp.setJobState(p_data.jobStatusList);
        }
        else
        {
            // job state EXPORTED,DISPATCHED,LOCALIZED,EXPORTED FAILED
            ArrayList<String> list = new ArrayList<String>();
            list.add(Job.READY_TO_BE_DISPATCHED);
            list.add(Job.DISPATCHED);
            list.add(Job.LOCALIZED);
            list.add(Job.EXPORTED);
            list.add(Job.EXPORT_FAIL);
            list.add(Job.ARCHIVED);
            sp.setJobState(list);
        }

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd/yyyy");
        try {
        	String paramCreateDateStartCount = p_request
        			.getParameter(JobSearchConstants.CREATION_START);
        	if (paramCreateDateStartCount != null && paramCreateDateStartCount != "")
        	{
        		sp.setCreationStart(simpleDateFormat.parse(paramCreateDateStartCount));
        	}
        	
        	 String paramCreateDateEndCount = p_request
                     .getParameter(JobSearchConstants.CREATION_END);
            if (paramCreateDateEndCount != null && paramCreateDateEndCount != "")
             {
            	Date date = simpleDateFormat.parse(paramCreateDateEndCount);
            	long endLong = date.getTime()+(24*60*60*1000-1);
                 sp.setCreationEnd(new Date(endLong));
             }
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        return sp;
    }
    
    private double asDouble(BigDecimal d)
    {
        // BigDecimal v = d.setScale(SCALE_EXCEL,BigDecimal.ROUND_HALF_UP);
        return d.doubleValue();
    }

    /**
     * Adds the given float to the BigDecimal after scaling it to 3(SCALE)
     * decimal points of precision and rounding half up. If you don't do this,
     * then the float 0.255 will become 0.254999995231628 Returns a new
     * BigDecimal which is the sum of a and p_f
     */
    private BigDecimal add(BigDecimal p_a, float p_f)
    {
        String floatString = Float.toString(p_f);
        BigDecimal bd = new BigDecimal(floatString);
        BigDecimal sbd = bd.setScale(SCALE, BigDecimal.ROUND_HALF_UP);
        return p_a.add(sbd);
    }

    /**
     * Returns a list of jobs that are in a different project but should be
     * treated as if they're in this project.
     */
    private void getJobsInWrongProject(MyData p_data)
    {
        try
        {
            p_data.wrongJobs.addAll(readJobNames(p_data));
            Iterator<Job> iter = p_data.wrongJobs.iterator();
            while (iter.hasNext())
            {
                Job j = iter.next();
                p_data.wrongJobNames.add(j.getJobName());
            }
        }
        catch (Exception e)
        {
            s_logger.error(
                    "Failed to add jobs which are in the 'wrong' project.", e);
        }
    }

    /**
     * Opens up the file "jobsInWrongDivision.txt" and returns only the jobs
     * that should be mapped to the requested projects
     */
    private ArrayList readJobNames(MyData p_data) throws Exception
    {
        ArrayList wrongJobs = new ArrayList();
        SystemConfiguration sc = SystemConfiguration.getInstance();
        StringBuffer mapFile = new StringBuffer(
                sc.getStringParameter(SystemConfigParamNames.GLOBALSIGHT_HOME_DIRECTORY));
        mapFile.append(File.separator);
        mapFile.append("jobsInWrongDivision.txt");
        File f = new File(mapFile.toString());
        if (f.exists() == false)
        {
            if (p_data.warnedAboutMissingWrongJobsFile == false)
            {
                s_logger.warn("jobsInWrongDivision.txt file not found.");
                p_data.warnedAboutMissingWrongJobsFile = true;
            }
            return wrongJobs;
        }
        BufferedReader reader = new BufferedReader(new FileReader(f));
        String line = null;

        while ((line = reader.readLine()) != null)
        {
            if (line.startsWith("#") || line.length() == 0)
                continue;
            String jobname = null;
            String projectname = null;
            try
            {
                // the format is jobname = projectname
                String[] tokens = line.split("=");
                jobname = tokens[0].trim();
                projectname = tokens[1].trim();
                Project p = ServerProxy.getProjectHandler().getProjectByName(
                        projectname);
                Long newProjectId = p.getIdAsLong();

                JobSearchParameters sp = new JobSearchParameters();
                sp.setJobName(jobname);
                ArrayList jobs = new ArrayList(ServerProxy.getJobHandler()
                        .getJobs(sp));
                Job j = (Job) jobs.get(0);

                if (p_data.wantsAllProjects == false
                        && p_data.projectIdList.contains(newProjectId) == false)
                {
                    p_data.ignoreJobs.add(j);
                    continue;
                }

                // but if the wrong job is actually currently in a project we
                // are reporting on, then skip it as well
                // if the project id list contains the old project but not the
                // new project
                Long oldProjectId = j.getL10nProfile().getProject()
                        .getIdAsLong();
                if (p_data.projectIdList.contains(oldProjectId)
                        && !p_data.projectIdList.contains(newProjectId))
                {
                    p_data.ignoreJobs.add(j);
                    continue;
                }

                // add the Job to the job list, and add the mapping for job id
                // to project
                wrongJobs.add(j);
                p_data.wrongJobMap.put(new Long(j.getId()), p);
            }
            catch (Exception e)
            {
                s_logger.warn("Ignoring mapping line for "
                        + jobname
                        + " => "
                        + projectname
                        + ". Either the job doesn't exist, the project doesn't exist, or both.");
            }
        }
        reader.close();
        return wrongJobs;
    }

    // see if the job is in the wrong job list, if so, use it's new project
    // and not the original one
    private String getProjectDesc(MyData p_data, Job p_job)
    {
        Project p = (Project) p_data.wrongJobMap.get(new Long(p_job.getId()));
        if (p == null)
            p = p_job.getL10nProfile().getProject();
        String d = p.getDescription();
        String desc = null;
        if (d == null || d.length() == 0)
            desc = p.getName();
        else
            desc = p.getName() + ": " + d;
        return desc;
    }

    // returns jobs in the specified criteria date range
    private ArrayList<Job> getWrongJobs(MyData p_data)
    {
        ArrayList<Job> wrongJobs = new ArrayList<Job>();
        Iterator<Job> iter = p_data.wrongJobs.iterator();
        Calendar cal = Calendar.getInstance();
        while (iter.hasNext())
        {
            Job j = iter.next();
            cal.setTime(j.getCreateDate());

            // //check the job's time against the search criteria
            wrongJobs.add(j);
        }
        return wrongJobs;
    }

    private String getNumberFormatString()
    {
        return symbol + "###,###,##0.000;(" + symbol + "###,###,##0.000)";
    }
    
    private CellStyle getHeaderStyle(Workbook p_workbook)
    {
    	if(headerStyle == null)
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
    
    private CellStyle getRedCellStyle(Workbook p_workbook) throws Exception
    {
    	if(redCellStyle == null)
    	{   		
    		Font redFont = p_workbook.createFont();
    		redFont.setFontName("Arial");
    		redFont.setUnderline(Font.U_NONE);
    		redFont.setFontHeightInPoints((short) 10);
	    	
	    	CellStyle style = p_workbook.createCellStyle();
            style.setFont(redFont);
            style.setWrapText(true);
            style.setFillPattern(CellStyle.SOLID_FOREGROUND );
            style.setFillForegroundColor(IndexedColors.RED.getIndex());
            
            redCellStyle = style;
    	}
    	return redCellStyle;
    }
    
    private CellStyle getDateStyle(Workbook p_workbook) throws Exception
    {
    	if(dateStyle == null)
    	{   		
    		Font dateFont = p_workbook.createFont();
            dateFont.setFontName("Arial");
            dateFont.setFontHeightInPoints((short) 10);

            DataFormat format = p_workbook.createDataFormat();
            CellStyle cs = p_workbook.createCellStyle();
            cs.setFont(dateFont);
            cs.setDataFormat(format.getFormat("M/d/yy"));
            cs.setWrapText(false);
            
            dateStyle = cs;
    	}
    	return dateStyle;
    }
    
    private CellStyle getFailedDateStyle(Workbook p_workbook) throws Exception
    {
    	if(failedDateStyle == null)
    	{   		
    		Font dateFont = p_workbook.createFont();
            dateFont.setFontName("Arial");
            dateFont.setFontHeightInPoints((short) 10);

            DataFormat format = p_workbook.createDataFormat();
            CellStyle cs = p_workbook.createCellStyle();
            cs.setFont(dateFont);
            cs.setDataFormat(format.getFormat("M/d/yy"));
            cs.setWrapText(false);
            cs.setFillPattern(CellStyle.SOLID_FOREGROUND );
            cs.setFillForegroundColor(IndexedColors.RED.getIndex());
            
            failedDateStyle = cs;
    	}
    	return failedDateStyle;
    }
    
    private CellStyle getMoneyStyle(Workbook p_workbook) throws Exception
    {
    	if(moneyStyle == null)
    	{
    		String euroJavaNumberFormat = getNumberFormatString();
            DataFormat euroNumberFormat = p_workbook.createDataFormat();
            
    		CellStyle cs = p_workbook.createCellStyle();
    		cs.setDataFormat(euroNumberFormat.getFormat(euroJavaNumberFormat));
            cs.setWrapText(false);
            
            moneyStyle = cs;
    	}
    	return moneyStyle;
    }
    
    private CellStyle getFailedMoneyStyle(Workbook p_workbook) throws Exception
    {
    	if(failedMoneyStyle == null)
    	{
    		String euroJavaNumberFormat = getNumberFormatString();
            DataFormat euroNumberFormat = p_workbook.createDataFormat();
            
    		CellStyle cs = p_workbook.createCellStyle();
    		cs.setWrapText(false);
    		cs.setDataFormat(euroNumberFormat.getFormat(euroJavaNumberFormat));
    		cs.setFillPattern(CellStyle.SOLID_FOREGROUND );
    		cs.setFillForegroundColor(IndexedColors.RED.getIndex());
            
            failedMoneyStyle = cs;
    	}
    	return failedMoneyStyle;
    }
    
    private CellStyle getWrongJobStyle(Workbook p_workbook) throws Exception
    {
    	if(wrongJobStyle == null)
    	{
    		Font wrongJobFont = p_workbook.createFont();
            wrongJobFont.setBoldweight(Font.BOLDWEIGHT_BOLD);
            wrongJobFont.setColor(IndexedColors.GREY_50_PERCENT.getIndex());
            wrongJobFont.setUnderline(Font.U_NONE);
            wrongJobFont.setFontName("Times");
            wrongJobFont.setFontHeightInPoints((short) 11);
            
            CellStyle cs = p_workbook.createCellStyle();
            cs.setFont(wrongJobFont);
            
    		wrongJobStyle = cs;
    	}
    	return wrongJobStyle;
    }
    
    private CellStyle getSubTotalStyle(Workbook p_workbook) throws Exception
    {
    	if(subTotalStyle == null)
    	{
    		Font subTotalFont = p_workbook.createFont();
        	subTotalFont.setBoldweight(Font.BOLDWEIGHT_BOLD);
        	subTotalFont.setColor(IndexedColors.BLACK.getIndex());
        	subTotalFont.setUnderline(Font.U_NONE);
        	subTotalFont.setFontName("Arial");
        	subTotalFont.setFontHeightInPoints((short) 10);
        	
            CellStyle cs = p_workbook.createCellStyle();
            cs.setFont(subTotalFont);
            cs.setWrapText(true);
            cs.setBorderTop(CellStyle.BORDER_THIN);
            cs.setBorderBottom(CellStyle.BORDER_THIN);
            cs.setFillPattern(CellStyle.SOLID_FOREGROUND );
            cs.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            
            subTotalStyle = cs;
    	}
    	return subTotalStyle;
    }
    
    private CellStyle getTotalMoneyStyle(Workbook p_workbook) throws Exception
    {
    	if(totalMoneyStyle == null)
    	{
    		String euroJavaNumberFormat = getNumberFormatString();
            DataFormat euroNumberFormat = p_workbook.createDataFormat();
            CellStyle cs = p_workbook.createCellStyle();
            cs.setDataFormat(euroNumberFormat.getFormat(euroJavaNumberFormat));
            cs.setWrapText(false);
            cs.setBorderTop(CellStyle.BORDER_THIN);
            cs.setBorderBottom(CellStyle.BORDER_THIN);
            cs.setFillPattern(CellStyle.SOLID_FOREGROUND );
            cs.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            
            totalMoneyStyle = cs;
    	}
    	return totalMoneyStyle;
    }
    
    public void setRegionStyle(Sheet sheet, CellRangeAddress cellRangeAddress,
    		CellStyle cs) {
    		for (int i = cellRangeAddress.getFirstRow(); i <= cellRangeAddress.getLastRow();
    			i++) {
    			Row row = getRow(sheet, i);
    			for (int j = cellRangeAddress.getFirstColumn(); 
    				j <= cellRangeAddress.getLastColumn(); j++) {
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
    
    public String getReportType()
    {
        return ReportConstants.VENDOR_PO_REPORT;
    }
    
    public boolean isCancelled()
    {
        ReportsData data = ReportGeneratorHandler.getReportsMap(userId,
                m_jobIDS, getReportType());
        if (data != null)
            return data.isCancle();

        return false;
    }
}
