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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
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

import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.foundation.SearchCriteriaParameters;
import com.globalsight.everest.localemgr.LocaleManagerLocal;
import com.globalsight.everest.projecthandler.Project;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.taskmanager.Task;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.pagehandler.administration.reports.bo.ReportsData;
import com.globalsight.everest.webapp.pagehandler.administration.reports.util.CurrencyThreadLocal;
import com.globalsight.everest.webapp.pagehandler.administration.reports.util.ProjectWorkflowData;
import com.globalsight.everest.webapp.pagehandler.administration.reports.util.ReviewerVendorPoReportDataAssembler;
import com.globalsight.everest.webapp.pagehandler.administration.reports.util.XlsReportData;
import com.globalsight.everest.webapp.pagehandler.projects.workflows.JobSearchConstants;
import com.globalsight.everest.workflow.Activity;
import com.globalsight.util.IntHolder;
import com.globalsight.util.SortUtil;

public class ReviewerVendorPoXlsReportHelper
{
    private static Logger s_logger = Logger
            .getLogger("ReviewerVendorPoXlsReportHelper");
    private static final String DEFAULT_DATE_FORMAT = "MM/dd/yyyy";
    private ResourceBundle bundle = null;
    private String userId = null;
    private HttpServletRequest request = null;
    private HttpServletResponse response = null;
    private XlsReportData data = null;
    private CellStyle contentStyle = null;
    private CellStyle headerStyle = null;
    private CellStyle dateStyle = null;
    private CellStyle moneyStyle = null;
    private CellStyle wrongJobStyle = null;
    private CellStyle subTotalStyle = null;
    private CellStyle totalMoneyStyle = null;
    String EMEA = CompanyWrapper.getCurrentCompanyName();

    public ReviewerVendorPoXlsReportHelper(HttpServletRequest p_request,
            HttpServletResponse p_response) throws Exception
    {
        this.request = p_request;
        this.response = p_response;

        String currency = request.getParameter("currency");
        CurrencyThreadLocal.setCurrency(currency);
        userId = (String) request.getSession(false).getAttribute(
                WebAppConstants.USER_NAME);

        ReviewerVendorPoReportDataAssembler reportDataAssembler = new ReviewerVendorPoReportDataAssembler(
                p_request);

        reportDataAssembler.setProjectIdList(userId);
        reportDataAssembler.setTargetLangList();
        reportDataAssembler.setActivityNameList();
        reportDataAssembler.setJobStateList();
        // set all the jobs that were originally imported with the wrong project
        // the users want to pretend that these jobs are in this project
        reportDataAssembler.setJobsInWrongProject();

        // set ProjectDate for report
        boolean recalculateFinishedWorkflow = false;
        String recalcParam = request.getParameter("recalc");
        if (recalcParam != null && recalcParam.length() > 0)
        {
            recalculateFinishedWorkflow = Boolean.valueOf(recalcParam)
                    .booleanValue();
        }
        reportDataAssembler.setProjectData(recalculateFinishedWorkflow);

        data = reportDataAssembler.getXlsReportData();
    }

    /**
     * Generates the Excel report as a temp file and returns the temp file.
     * 
     * @return File
     * @exception Exception
     */
    public void generateReport() throws Exception
    {
        bundle = PageHandler.getBundle(request.getSession());
        
        Workbook p_workbook = new SXSSFWorkbook();

        HashMap projectMap = data.projectMap;

        data.dellSheet = p_workbook.createSheet(
        		bundle.getString("lb_globalsight_matches"));
        data.tradosSheet = p_workbook.createSheet(
                bundle.getString("jobinfo.tradosmatches"));

        addTitle(p_workbook, data.dellSheet);
        addHeaderForDellMatches(p_workbook);
        addTitle(p_workbook, data.tradosSheet);
        addHeaderForTradosMatches(p_workbook);

        IntHolder row = new IntHolder(4);
        writeProjectDataForDellMatches(p_workbook, projectMap, row);
        row = new IntHolder(4);
        writeProjectDataForTradosMatches(p_workbook, projectMap, row);

        Sheet paramsSheet = p_workbook.createSheet(
                bundle.getString("lb_criteria"));
        writeParamsSheet(p_workbook, paramsSheet);
        List<Long> reportJobIDS = new ArrayList(data.jobIdList);
        // Cancel Duplicate Request
        if (ReportHelper.checkReportsDataInProgressStatus(userId,
                reportJobIDS, getReportType()))
        {
            String message = "Cancle Review Vendor Report: " + userId + ", "
                    + reportJobIDS;
            s_logger.info(message);
            response.sendError(response.SC_NO_CONTENT);
            return;
        }
        // Set ReportsData.
        ReportHelper.setReportsData(userId, reportJobIDS, getReportType(),
                0, ReportsData.STATUS_INPROGRESS);

        ServletOutputStream out = response.getOutputStream();
        p_workbook.write(out);
        out.close();
        ((SXSSFWorkbook)p_workbook).dispose();

        // Set ReportsData.
        ReportHelper.setReportsData(userId, reportJobIDS, getReportType(),
                100, ReportsData.STATUS_FINISHED);
    }

    private void addTitle(Workbook p_workbook, Sheet p_sheet) throws Exception
    {
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
        Cell titleCell = getCell(titleRow, 0);
        titleCell.setCellValue(EMEA + " " + bundle.getString("lb_reviewer_po"));
        titleCell.setCellStyle(titleStyle);
        p_sheet.setColumnWidth(0, 20 * 256);
    }

    /**
     * Adds the table header for the Dell Matches sheet
     * 
     */
    private void addHeaderForDellMatches(Workbook p_workbook) throws Exception
    {
        Sheet theSheet = data.dellSheet;
        int c = 0;
        Row headerRow = getRow(theSheet, 2);
        Cell cell_A = getCell(headerRow, c);
        cell_A.setCellValue(bundle.getString("lb_job_id"));
        cell_A.setCellStyle(getHeaderStyle(p_workbook));
        theSheet.addMergedRegion(new CellRangeAddress(2, 3, c, c));
        setRegionStyle(theSheet, new CellRangeAddress(2, 3, c, c), 
        		getHeaderStyle(p_workbook));
        c++;
        Cell cell_B = getCell(headerRow, c);
        cell_B.setCellValue(bundle.getString("lb_job_name"));
        cell_B.setCellStyle(getHeaderStyle(p_workbook));
        theSheet.addMergedRegion(new CellRangeAddress(2, 3, c, c));
        setRegionStyle(theSheet, new CellRangeAddress(2, 3, c, c),
        		getHeaderStyle(p_workbook));
        c++;
        Cell cell_C = getCell(headerRow, c);
        cell_C.setCellValue(bundle.getString("lb_po_number_report"));
        cell_C.setCellStyle(getHeaderStyle(p_workbook));
        theSheet.addMergedRegion(new CellRangeAddress(2, 3, c, c));
        setRegionStyle(theSheet, new CellRangeAddress(2, 3, c, c), 
        		getHeaderStyle(p_workbook));
        theSheet.setColumnWidth(c, 15 * 256);
        c++;
        Cell cell_D = getCell(headerRow, c);
        cell_D.setCellValue(bundle.getString("lb_description"));
        cell_D.setCellStyle(getHeaderStyle(p_workbook));
        theSheet.addMergedRegion(new CellRangeAddress(2, 3, c, c));
        setRegionStyle(theSheet, new CellRangeAddress(2, 3, c, c), 
        		getHeaderStyle(p_workbook));
        theSheet.setColumnWidth(c, 15 * 256);
        c++;
        Cell cell_E = getCell(headerRow, c);
        cell_E.setCellValue(bundle.getString("lb_creation_date"));
        cell_E.setCellStyle(getHeaderStyle(p_workbook));
        theSheet.addMergedRegion(new CellRangeAddress(2, 3, c, c));
        setRegionStyle(theSheet, new CellRangeAddress(2, 3, c, c), 
        		getHeaderStyle(p_workbook));
        c++;
        Cell cell_F = getCell(headerRow, c);
        cell_F.setCellValue(bundle.getString("lb_activity_name"));
        cell_F.setCellStyle(getHeaderStyle(p_workbook));
        theSheet.addMergedRegion(new CellRangeAddress(2, 3, c, c));
        setRegionStyle(theSheet, new CellRangeAddress(2, 3, c, c),
        		getHeaderStyle(p_workbook));
        theSheet.setColumnWidth(c, 20 * 256);
        c++;
        Cell cell_G = getCell(headerRow, c);
        cell_G.setCellValue(bundle.getString("lb_accepted_reviewer_date"));
        cell_G.setCellStyle(getHeaderStyle(p_workbook));
        theSheet.addMergedRegion(new CellRangeAddress(2, 3, c, c));
        setRegionStyle(theSheet, new CellRangeAddress(2, 3, c, c),
        		getHeaderStyle(p_workbook));
        theSheet.setColumnWidth(c, 15 * 256);
        c++;
        Cell cell_H = getCell(headerRow, c);
        cell_H.setCellValue(bundle.getString("lb_lang"));
        cell_H.setCellStyle(getHeaderStyle(p_workbook));
        theSheet.addMergedRegion(new CellRangeAddress(2, 3, c, c));
        setRegionStyle(theSheet, new CellRangeAddress(2, 3, c, c),
        		getHeaderStyle(p_workbook));
        c++;
        Cell cell_I = getCell(headerRow, c);
        cell_I.setCellValue(bundle.getString("lb_word_count"));
        cell_I.setCellStyle(getHeaderStyle(p_workbook));
        theSheet.addMergedRegion(new CellRangeAddress(2, 3, c, c));
        setRegionStyle(theSheet, new CellRangeAddress(2, 3, c, c),
        		getHeaderStyle(p_workbook));
        theSheet.setColumnWidth(c, 15 * 256);
        
        c++;
        Cell cell_J = getCell(headerRow, c);
        cell_J.setCellValue(bundle.getString("jobinfo.tmmatches.invoice"));
        cell_J.setCellStyle(getHeaderStyle(p_workbook));
        theSheet.addMergedRegion(new CellRangeAddress(2, 3, c, c));
        setRegionStyle(theSheet, new CellRangeAddress(2, 3, c, c), 
        		getHeaderStyle(p_workbook));
        c++;
        Cell cell_K = getCell(headerRow, c);
        cell_K.setCellValue(bundle.getString("lb_tracking"));
        cell_K.setCellStyle(getHeaderStyle(p_workbook));
        theSheet.addMergedRegion(new CellRangeAddress(2, 3, c, c));
        setRegionStyle(theSheet, new CellRangeAddress(2, 3, c, c), 
        		getHeaderStyle(p_workbook));
    }
   
    /**
     * Adds the table header for the Trados Matches sheet
     * 
     */
    private void addHeaderForTradosMatches(Workbook p_workbook) throws Exception
    {
        Sheet theSheet = data.tradosSheet;
        int c = 0;
        Row secRow = getRow(theSheet, 1);
        Row thirRow = getRow(theSheet, 2);
        Row fourRow = getRow(theSheet, 3);
        
        Cell cell_Ldfl = getCell(secRow, 0);
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

        if (data.headers[0] != null)
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
        cell_G.setCellValue(bundle
        		.getString("jobinfo.tradosmatches.invoice.per100matches"));
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
        if (data.headers[0] != null)
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

        if (data.headers[0] != null)
        {
        	theSheet.addMergedRegion(new CellRangeAddress(2, 2, c, c + 9));
            setRegionStyle(theSheet, new CellRangeAddress(2, 2, c, c + 9), 
            		getHeaderStyle(p_workbook));
        }
        else
        {
        	theSheet.addMergedRegion(new CellRangeAddress(2, 2, c, c + 8));
            setRegionStyle(theSheet, new CellRangeAddress(2, 2, c, c + 8), 
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
        if (data.headers[0] != null)
        {
        	Cell cell_InContext = getCell(fourRow, c++);
        	cell_InContext.setCellValue(bundle.getString("lb_in_context_tm"));
        	cell_InContext.setCellStyle(getHeaderStyle(p_workbook));
        }

        Cell cell_Total_Invoice = getCell(fourRow, c++);
        cell_Total_Invoice.setCellValue(bundle.getString("lb_translation_total"));
        cell_Total_Invoice.setCellStyle(getHeaderStyle(p_workbook));
        
        Cell cell_Review = getCell(fourRow, c++);
        cell_Review.setCellValue(bundle.getString("lb_review"));
        cell_Review.setCellStyle(getHeaderStyle(p_workbook));
        
        Cell cell_JobTotal = getCell(fourRow, c++);
        cell_JobTotal.setCellValue(bundle
        		.getString("jobinfo.tmmatches.invoice.jobtotal"));
        cell_JobTotal.setCellStyle(getHeaderStyle(p_workbook));

        Cell cell_Tracking = getCell(thirRow, c);
        cell_Tracking.setCellValue(bundle.getString("lb_tracking"));
        cell_Tracking.setCellStyle(getHeaderStyle(p_workbook));
        theSheet.addMergedRegion(new CellRangeAddress(2, 3, c, c));
        setRegionStyle(theSheet, new CellRangeAddress(2, 3, c, c), 
        		getHeaderStyle(p_workbook));
    }
    
    private void writeProjectDataForDellMatches(Workbook p_workbook,
    		HashMap p_projectMap, IntHolder p_row) throws Exception
    {
        Sheet theSheet = data.dellSheet;
        ArrayList projects = new ArrayList(p_projectMap.keySet());
        SortUtil.sort(projects);
        Iterator projectIter = projects.iterator();

        while (projectIter.hasNext())
        {
            String jobName = (String) projectIter.next();
            boolean isWrongJob = data.wrongJobNames.contains(jobName);
            HashMap localeMap = (HashMap) p_projectMap.get(jobName);
            ArrayList locales = new ArrayList(localeMap.keySet());
            SortUtil.sort(locales);
            Iterator localeIter = locales.iterator();

            while (localeIter.hasNext())
            {
                int row = p_row.getValue();
                Row theRow = getRow(theSheet, row);
                int col = 0;
                String localeName = (String) localeIter.next();
                ProjectWorkflowData data = (ProjectWorkflowData) localeMap
                        .get(localeName);

                // Job Id
                // Job Name
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
                	cell_A.setCellStyle(getContentStyle(p_workbook));
                	cell_B.setCellStyle(getContentStyle(p_workbook));
                }
                theSheet.setColumnWidth(col - 2, 5 * 256);
                theSheet.setColumnWidth(col - 1, 50 * 256);

                // PO Number
                Cell cell_C = getCell(theRow, col++);
            	cell_C.setCellValue("");
            	cell_C.setCellStyle(getContentStyle(p_workbook));

                // Description
            	Cell cell_D = getCell(theRow, col++);
            	cell_D.setCellValue(data.projectDesc);
            	cell_D.setCellStyle(getContentStyle(p_workbook));
                theSheet.setColumnWidth(col - 1, 22 * 256);

                // Create Date
                Cell cell_E = getCell(theRow, col++);
            	cell_E.setCellValue(data.creationDate);
            	cell_E.setCellStyle(getDateStyle(p_workbook));
                theSheet.setColumnWidth(col - 1, 15 * 256);

                Cell cell_F = getCell(theRow, col++);
            	cell_F.setCellValue(data.currentActivityName);
            	cell_F.setCellStyle(getContentStyle(p_workbook));

                // Accepted Reviewer Date
            	Cell cell_G = getCell(theRow, col++);
                if (data.dellReviewActivityState == Task.STATE_REJECTED)
                {
                	cell_G.setCellValue(bundle.getString("lb_rejected"));
                	cell_G.setCellStyle(getContentStyle(p_workbook));
                }
                else if (data.acceptedReviewerDate == null)
                {
                	cell_G.setCellValue(bundle.getString("lb_not_accepted"));
                	cell_G.setCellStyle(getContentStyle(p_workbook));
                }
                else
                {
                	cell_G.setCellValue(data.acceptedReviewerDate);
                	cell_G.setCellStyle(getDateStyle(p_workbook));
                }
                theSheet.setColumnWidth(col - 1, 22 * 256);

                // Lang
                Cell cell_H = getCell(theRow, col++);
                cell_H.setCellValue(data.targetLang);
                cell_H.setCellStyle(getContentStyle(p_workbook));

                // Word Count
            	Cell cell_I = getCell(theRow, col++);
            	cell_I.setCellValue(data.dellTotalWordCount);
            	cell_I.setCellStyle(getContentStyle(p_workbook));

                // Invoice
            	Cell cell_J = getCell(theRow, col++);
                if ((data.dellReviewActivityState == Task.STATE_REJECTED)
                        || (data.acceptedReviewerDate == null))
                {
                	cell_J.setCellValue(0);
                	cell_J.setCellStyle(getMoneyStyle(p_workbook));
                }
                else
                {
                	cell_J.setCellValue(asDouble(data.dellTotalWordCountCostForDellReview));
                	cell_J.setCellStyle(getMoneyStyle(p_workbook));
                }

                p_row.inc();
            }
        }

        p_row.inc();
        p_row.inc();
        addTotalsForDellMatches(p_workbook, p_row);
    }

    /** Adds totals */
    private void addTotalsForDellMatches(Workbook p_workbook, IntHolder p_row) throws Exception
    {
        Sheet theSheet = data.dellSheet;
        // Totals
        int totalsRow = p_row.getValue() + 1; // skip a row
        Row totalRow = getRow(theSheet, totalsRow);
        Cell cell_A = getCell(totalRow, 0);
        cell_A.setCellValue(bundle.getString("lb_totals"));
        cell_A.setCellStyle(getSubTotalStyle(p_workbook));
        theSheet.addMergedRegion(new CellRangeAddress(totalsRow, totalsRow, 0, 7));
        setRegionStyle(theSheet, new CellRangeAddress(totalsRow, totalsRow, 0, 7), 
        		getSubTotalStyle(p_workbook));

        int lastRow = p_row.getValue() - 2;
        int c = 8;
        // Word Count
        Cell cell_I = getCell(totalRow, c++);
        cell_I.setCellFormula("SUM(I5" + ":I" + lastRow + ")");
        cell_I.setCellStyle(getSubTotalStyle(p_workbook));
        // Invoice
        Cell cell_J = getCell(totalRow, c++);
        cell_J.setCellFormula("SUM(J5" + ":J" + lastRow + ")");
        cell_J.setCellStyle(getTotalMoneyStyle(p_workbook));
        // add an extra column for Dell Tracking Use
        Cell cell_K = getCell(totalRow, c++);
        cell_K.setCellValue("");
        cell_K.setCellStyle(getTotalMoneyStyle(p_workbook));
    }

    private void writeProjectDataForTradosMatches(Workbook p_workbook, HashMap p_projectMap,
            IntHolder p_row) throws Exception
    {
        Sheet theSheet = data.tradosSheet;
        ArrayList projects = new ArrayList(p_projectMap.keySet());
        SortUtil.sort(projects);
        Iterator projectIter = projects.iterator();

        while (projectIter.hasNext())
        {
            String jobName = (String) projectIter.next();
            boolean isWrongJob = data.wrongJobNames.contains(jobName);
            HashMap localeMap = (HashMap) p_projectMap.get(jobName);
            ArrayList locales = new ArrayList(localeMap.keySet());
            SortUtil.sort(locales);
            HashMap<String, String> jobLocale = new HashMap<String, String>();
            Iterator localeIter = locales.iterator();
            while (localeIter.hasNext())
            {
                int row = p_row.getValue();
                int col = 0;
                Row theRow = getRow(theSheet, row);
                String localeName = (String) localeIter.next();
                ProjectWorkflowData data = (ProjectWorkflowData) localeMap
                        .get(localeName);

                if (jobLocale.size() != 0
                        && jobLocale.get(jobName + data.targetLang) != null)
                {
                    continue;
                }
                else
                {
                    jobLocale.put(jobName + data.targetLang, jobName
                            + data.targetLang);
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
                	cell_A.setCellStyle(getContentStyle(p_workbook));
                	cell_B.setCellStyle(getContentStyle(p_workbook));
                }
                theSheet.setColumnWidth(col - 2, 5 * 256);
                theSheet.setColumnWidth(col - 1, 50 * 256);
                Cell cell_C = getCell(theRow, col++);// PO number
            	cell_C.setCellValue("");
            	cell_C.setCellStyle(getContentStyle(p_workbook)); 
            	
            	Cell cell_D = getCell(theRow, col++);
            	cell_D.setCellValue(data.projectDesc);
            	cell_D.setCellStyle(getContentStyle(p_workbook));
                theSheet.setColumnWidth(col - 1, 22 * 256);
                /* data.creationDate.toString())); */
                Cell cell_E = getCell(theRow, col++);
            	cell_E.setCellValue(data.creationDate);
            	cell_E.setCellStyle(getDateStyle(p_workbook));
                theSheet.setColumnWidth(col - 1, 15 * 256);
                
                Cell cell_F = getCell(theRow, col++);
            	cell_F.setCellValue(data.targetLang);
            	cell_F.setCellStyle(getContentStyle(p_workbook));
            	
            	Cell cell_G = getCell(theRow, col++);
            	cell_G.setCellValue(data.trados100WordCount);
            	cell_G.setCellStyle(getContentStyle(p_workbook));
                int numwidth = 10;
                theSheet.setColumnWidth(col - 1, numwidth * 256);
                
                Cell cell_H = getCell(theRow, col++);
            	cell_H.setCellValue(data.trados95to99WordCount);
            	cell_H.setCellStyle(getContentStyle(p_workbook));
                theSheet.setColumnWidth(col - 1, numwidth * 256);

                Cell cell_I = getCell(theRow, col++);
            	cell_I.setCellValue(data.trados85to94WordCount);
            	cell_I.setCellStyle(getContentStyle(p_workbook));
                theSheet.setColumnWidth(col - 1, numwidth * 256);

                Cell cell_J = getCell(theRow, col++);
            	cell_J.setCellValue(data.trados75to84WordCount);
            	cell_J.setCellStyle(getContentStyle(p_workbook));
                theSheet.setColumnWidth(col - 1, numwidth * 256);
                
                Cell cell_K = getCell(theRow, col++);
            	cell_K.setCellValue(data.tradosNoMatchWordCount);
            	cell_K.setCellStyle(getContentStyle(p_workbook));
                theSheet.setColumnWidth(col - 1, numwidth * 256);
                
                Cell cell_L = getCell(theRow, col++);
            	cell_L.setCellValue(data.tradosRepsWordCount);
            	cell_L.setCellStyle(getContentStyle(p_workbook));
                theSheet.setColumnWidth(col - 1, numwidth * 256);
                if (this.data.headers[0] != null)
                {
                	Cell cell_InContext = getCell(theRow, col++);
                	cell_InContext.setCellValue(data.tradosInContextWordCount);
                	cell_InContext.setCellStyle(getContentStyle(p_workbook));
                    theSheet.setColumnWidth(col - 1, numwidth * 256);
                }

                Cell cell_Total = getCell(theRow, col++);
                cell_Total.setCellValue(data.tradosTotalWordCount);
                cell_Total.setCellStyle(getContentStyle(p_workbook));
                theSheet.setColumnWidth(col - 1, numwidth * 256);

                int moneywidth = 12;
                Cell cell_100Cost = getCell(theRow, col++);
                cell_100Cost.setCellValue(asDouble(data.trados100WordCountCost));
                cell_100Cost.setCellStyle(getMoneyStyle(p_workbook));
                theSheet.setColumnWidth(col - 1, moneywidth * 256);
                
                Cell cell_95_99 = getCell(theRow, col++);
                cell_95_99.setCellValue(asDouble(data.trados95to99WordCountCost));
                cell_95_99.setCellStyle(getMoneyStyle(p_workbook));
                theSheet.setColumnWidth(col - 1, moneywidth * 256);

                Cell cell_85_94 = getCell(theRow, col++);
                cell_85_94.setCellValue(asDouble(data.trados85to94WordCountCost));
                cell_85_94.setCellStyle(getMoneyStyle(p_workbook));
                theSheet.setColumnWidth(col - 1, moneywidth * 256);

                Cell cell_75_84 = getCell(theRow, col++);
                cell_75_84.setCellValue(asDouble(data.trados75to84WordCountCost));
                cell_75_84.setCellStyle(getMoneyStyle(p_workbook));
                theSheet.setColumnWidth(col - 1, moneywidth * 256);
                
                Cell cell_NoMatch = getCell(theRow, col++);
                cell_NoMatch.setCellValue(asDouble(data.tradosNoMatchWordCountCost));
                cell_NoMatch.setCellStyle(getMoneyStyle(p_workbook));
                theSheet.setColumnWidth(col - 1, moneywidth * 256);

                Cell cell_Reps = getCell(theRow, col++);
            	cell_Reps.setCellValue(asDouble(data.tradosRepsWordCountCost));
            	cell_Reps.setCellStyle(getMoneyStyle(p_workbook));
                theSheet.setColumnWidth(col - 1, moneywidth * 256);
                if (this.data.headers[0] != null)
                {
                	Cell cell_InContext = getCell(theRow, col++);
                	cell_InContext.setCellValue(asDouble(data.tradosInContextWordCountCost));
                	cell_InContext.setCellStyle(getMoneyStyle(p_workbook));
                    theSheet.setColumnWidth(col - 1, moneywidth * 256);
                }

                // theSheet.addCell(new
                // Number(col++,row,asDouble(data.tradosTotalWordCountCost),moneyFormat));
                // theSheet.setColumnView(col -1,moneywidth);
                Cell cell_Translation = getCell(theRow, col++);
                cell_Translation.setCellValue(asDouble(data.tradosTotalWordCountCostForTranslation));
                cell_Translation.setCellStyle(getMoneyStyle(p_workbook));
                theSheet.setColumnWidth(col - 1, moneywidth * 256);
                
                Cell cell_Review = getCell(theRow, col++);
                cell_Review.setCellValue(asDouble(data.tradosTotalWordCountCostForDellReview));
                cell_Review.setCellStyle(getMoneyStyle(p_workbook));
                theSheet.setColumnWidth(col - 1, moneywidth * 256);
                
                Cell cell_JobTotal = getCell(theRow, col++);
                cell_JobTotal.setCellValue(asDouble(data.tradosTotalWordCountCost));
                cell_JobTotal.setCellStyle(getMoneyStyle(p_workbook));
                theSheet.setColumnWidth(col - 1, moneywidth * 256);

                p_row.inc();
            }
        }

        p_row.inc();
        p_row.inc();
        addTotalsForTradosMatches(p_workbook, p_row);
    }

    /** Adds the totals and sub-total formulas */
    private void addTotalsForTradosMatches(Workbook p_workbook, IntHolder p_row) throws Exception
    {
        Sheet theSheet = data.tradosSheet;
        int row = p_row.getValue() + 1; // skip a row
        String title = bundle.getString("lb_totals");

        Row totalRow = getRow(theSheet, row);
        Cell cell_A = getCell(totalRow, 0);
        cell_A.setCellValue(title);
        cell_A.setCellStyle(getSubTotalStyle(p_workbook));
        theSheet.addMergedRegion(new CellRangeAddress(row, row, 0, 5));
        setRegionStyle(theSheet, new CellRangeAddress(row, row, 0, 5), 
        		getSubTotalStyle(p_workbook));
        int lastRow = p_row.getValue() - 2;

        // add in word count totals
        int c = 6;
        // word counts
        if (data.headers[0] != null)
        {
        	Cell cell_G = getCell(totalRow, c++);
        	cell_G.setCellFormula("SUM(G5:G" + lastRow + ")");
        	cell_G.setCellStyle(getSubTotalStyle(p_workbook));
        	
        	Cell cell_H = getCell(totalRow, c++);
        	cell_H.setCellFormula("SUM(H5:H" + lastRow + ")");
        	cell_H.setCellStyle(getSubTotalStyle(p_workbook));
        	
        	Cell cell_I = getCell(totalRow, c++);
        	cell_I.setCellFormula("SUM(I5:I" + lastRow + ")");
        	cell_I.setCellStyle(getSubTotalStyle(p_workbook));
        	
        	Cell cell_J = getCell(totalRow, c++);
        	cell_J.setCellFormula("SUM(J5:J" + lastRow + ")");
        	cell_J.setCellStyle(getSubTotalStyle(p_workbook));
        	
        	Cell cell_K = getCell(totalRow, c++);
        	cell_K.setCellFormula("SUM(K5:K" + lastRow + ")");
        	cell_K.setCellStyle(getSubTotalStyle(p_workbook));
        	
        	Cell cell_L = getCell(totalRow, c++);
        	cell_L.setCellFormula("SUM(L5:L" + lastRow + ")");
        	cell_L.setCellStyle(getSubTotalStyle(p_workbook));
        	
        	Cell cell_M = getCell(totalRow, c++);
        	cell_M.setCellFormula("SUM(M5:M" + lastRow + ")");
        	cell_M.setCellStyle(getSubTotalStyle(p_workbook));
        	
        	Cell cell_N = getCell(totalRow, c++);
        	cell_N.setCellFormula("SUM(N5:N" + lastRow + ")");
        	cell_N.setCellStyle(getSubTotalStyle(p_workbook));
        	// word count costs
        	Cell cell_O = getCell(totalRow, c++);
        	cell_O.setCellFormula("SUM(O5:O" + lastRow + ")");
        	cell_O.setCellStyle(getTotalMoneyStyle(p_workbook));
        	
        	Cell cell_P = getCell(totalRow, c++);
        	cell_P.setCellFormula("SUM(P5:P" + lastRow + ")");
        	cell_P.setCellStyle(getTotalMoneyStyle(p_workbook));
        	
        	Cell cell_Q = getCell(totalRow, c++);
        	cell_Q.setCellFormula("SUM(Q5:Q" + lastRow + ")");
        	cell_Q.setCellStyle(getTotalMoneyStyle(p_workbook));
        	
        	Cell cell_R = getCell(totalRow, c++);
        	cell_R.setCellFormula("SUM(R5:R" + lastRow + ")");
        	cell_R.setCellStyle(getTotalMoneyStyle(p_workbook));
        	
        	Cell cell_S = getCell(totalRow, c++);
        	cell_S.setCellFormula("SUM(S5:S" + lastRow + ")");
        	cell_S.setCellStyle(getTotalMoneyStyle(p_workbook));
        	
        	Cell cell_T = getCell(totalRow, c++);
        	cell_T.setCellFormula("SUM(T5:T" + lastRow + ")");
        	cell_T.setCellStyle(getTotalMoneyStyle(p_workbook));
        	
        	Cell cell_U = getCell(totalRow, c++);
        	cell_U.setCellFormula("SUM(U5:U" + lastRow + ")");
        	cell_U.setCellStyle(getTotalMoneyStyle(p_workbook));
        	
        	Cell cell_V = getCell(totalRow, c++);
        	cell_V.setCellFormula("SUM(V5:V" + lastRow + ")");
        	cell_V.setCellStyle(getTotalMoneyStyle(p_workbook));
        	
        	Cell cell_W = getCell(totalRow, c++);
        	cell_W.setCellFormula("SUM(W5:W" + lastRow + ")");
        	cell_W.setCellStyle(getTotalMoneyStyle(p_workbook));
        	
        	Cell cell_X = getCell(totalRow, c++);
        	cell_X.setCellFormula("SUM(X5:X" + lastRow + ")");
        	cell_X.setCellStyle(getTotalMoneyStyle(p_workbook));
        }
        else
        {
        	Cell cell_G = getCell(totalRow, c++);
        	cell_G.setCellFormula("SUM(G5:G" + lastRow + ")");
        	cell_G.setCellStyle(getSubTotalStyle(p_workbook));
        	
        	Cell cell_H = getCell(totalRow, c++);
        	cell_H.setCellFormula("SUM(H5:H" + lastRow + ")");
        	cell_H.setCellStyle(getSubTotalStyle(p_workbook));
        	
        	Cell cell_I = getCell(totalRow, c++);
        	cell_I.setCellFormula("SUM(I5:I" + lastRow + ")");
        	cell_I.setCellStyle(getSubTotalStyle(p_workbook));
        	
        	Cell cell_J = getCell(totalRow, c++);
        	cell_J.setCellFormula("SUM(J5:J" + lastRow + ")");
        	cell_J.setCellStyle(getSubTotalStyle(p_workbook));
        	
        	Cell cell_K = getCell(totalRow, c++);
        	cell_K.setCellFormula("SUM(K5:K" + lastRow + ")");
        	cell_K.setCellStyle(getSubTotalStyle(p_workbook));
        	
        	Cell cell_L = getCell(totalRow, c++);
        	cell_L.setCellFormula("SUM(L5:L" + lastRow + ")");
        	cell_L.setCellStyle(getSubTotalStyle(p_workbook));
        	
        	Cell cell_M = getCell(totalRow, c++);
        	cell_M.setCellFormula("SUM(M5:M" + lastRow + ")");
        	cell_M.setCellStyle(getSubTotalStyle(p_workbook));
        	// word count costs
        	Cell cell_N = getCell(totalRow, c++);
        	cell_N.setCellFormula("SUM(N5:N" + lastRow + ")");
        	cell_N.setCellStyle(getTotalMoneyStyle(p_workbook));
        	
        	Cell cell_O = getCell(totalRow, c++);
        	cell_O.setCellFormula("SUM(O5:O" + lastRow + ")");
        	cell_O.setCellStyle(getTotalMoneyStyle(p_workbook));
        	
        	Cell cell_P = getCell(totalRow, c++);
        	cell_P.setCellFormula("SUM(P5:P" + lastRow + ")");
        	cell_P.setCellStyle(getTotalMoneyStyle(p_workbook));
        	
        	Cell cell_Q = getCell(totalRow, c++);
        	cell_Q.setCellFormula("SUM(Q5:Q" + lastRow + ")");
        	cell_Q.setCellStyle(getTotalMoneyStyle(p_workbook));
        	
        	Cell cell_R = getCell(totalRow, c++);
        	cell_R.setCellFormula("SUM(R5:R" + lastRow + ")");
        	cell_R.setCellStyle(getTotalMoneyStyle(p_workbook));
        	
        	Cell cell_S = getCell(totalRow, c++);
        	cell_S.setCellFormula("SUM(S5:S" + lastRow + ")");
        	cell_S.setCellStyle(getTotalMoneyStyle(p_workbook));
        	
        	Cell cell_T = getCell(totalRow, c++);
        	cell_T.setCellFormula("SUM(T5:T" + lastRow + ")");
        	cell_T.setCellStyle(getTotalMoneyStyle(p_workbook));
        	
        	Cell cell_U = getCell(totalRow, c++);
        	cell_U.setCellFormula("SUM(U5:U" + lastRow + ")");
        	cell_U.setCellStyle(getTotalMoneyStyle(p_workbook));
        	
        	Cell cell_V = getCell(totalRow, c++);
        	cell_V.setCellFormula("SUM(V5:V" + lastRow + ")");
        	cell_V.setCellStyle(getTotalMoneyStyle(p_workbook));
        }

        // add an extra column for Dell Tracking Use
        Cell cell_Last = getCell(totalRow, c++);
        cell_Last.setCellValue("");
        cell_Last.setCellStyle(getTotalMoneyStyle(p_workbook));
    }

    private void writeParamsSheet(Workbook p_workbook, Sheet paramsSheet) throws Exception
    {
    	Row firRow = getRow(paramsSheet, 0);
        Row secRow = getRow(paramsSheet, 1);
        Row thirRow = getRow(paramsSheet, 2);
        
        Cell cell_A_Title = getCell(firRow, 0);
        cell_A_Title.setCellValue(bundle
                .getString("lb_report_criteria"));
        cell_A_Title.setCellStyle(getContentStyle(p_workbook));
        paramsSheet.setColumnWidth(0, 50 * 256);

        Cell cell_A_Header = getCell(secRow, 0);
        if (data.wantsAllProjects)
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
            Iterator iter = data.projectIdList.iterator();
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
                    s_logger.error("Failed to get project." + e);
                }

                Row theRow = getRow(paramsSheet, r);
                String v = projectName + " ("
                        + bundle.getString("lb_report_id") + "="
                        + pid.toString() + ")";
                Cell cell_A = getCell(theRow, 0);
                cell_A.setCellValue(v);
                cell_A.setCellStyle(getContentStyle(p_workbook));
                r++;
            }
        }
        // add the date criteria
        String fromMsg = request.getParameter(JobSearchConstants.CREATION_START);
        String untilMsg = request.getParameter(JobSearchConstants.CREATION_END);

        Cell cell_B_Header = getCell(secRow, 1);
        cell_B_Header.setCellValue(bundle.getString("lb_from") + ":");
        cell_B_Header.setCellStyle(getContentStyle(p_workbook));
        
        Cell cell_B = getCell(thirRow, 1);
        cell_B.setCellValue(fromMsg);
        cell_B.setCellStyle(getContentStyle(p_workbook));
        paramsSheet.setColumnWidth(1, 20 * 256);
        
        Cell cell_C_Header = getCell(secRow, 2);
        cell_C_Header.setCellValue(bundle.getString("lb_until") + ":");
        cell_C_Header.setCellStyle(getContentStyle(p_workbook));
        
        Cell cell_C = getCell(thirRow, 2);
        cell_C.setCellValue(untilMsg);
        cell_C.setCellStyle(getContentStyle(p_workbook));
        paramsSheet.setColumnWidth(2, 20 * 256);

        // add the target lang criteria
        paramsSheet.setColumnWidth(3, 20 * 256);
        Cell cell_D_Header = getCell(secRow, 3);
        if (data.wantsAllTargetLangs)
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
            Iterator iter = data.targetLangList.iterator();
            int r = 2;
            LocaleManagerLocal manager = new LocaleManagerLocal();
            while (iter.hasNext())
            {
                String lang = (String) iter.next();
                Row theRow = getRow(paramsSheet, r++);
                Cell cell_D = getCell(theRow, 3);
                cell_D.setCellValue(manager
                		.getLocaleById(Long.valueOf(lang)).toString());
                cell_D.setCellStyle(getContentStyle(p_workbook));
            }
        }
        paramsSheet.setColumnWidth(4, 20 * 256);
        Cell cell_E_Header = getCell(secRow, 4);
        if (data.allJobStatus)
        {
        	cell_E_Header.setCellValue(bundle
                    .getString("lb_job_status")
                    + ": "
                    + bundle.getString("all"));
        	cell_E_Header.setCellStyle(getContentStyle(p_workbook));
        }
        else
        {
        	cell_E_Header.setCellValue(bundle
                    .getString("lb_job_status")
                    + ": ");
        	cell_E_Header.setCellStyle(getContentStyle(p_workbook));
            Iterator iter = data.jobStatusList.iterator();
            int r = 2;
            while (iter.hasNext())
            {
                String jobStatus = (String) iter.next();
                Row theRow = getRow(paramsSheet, r);
                Cell cell_E = getCell(theRow, 4);
                cell_E.setCellValue(jobStatus);
                cell_E.setCellStyle(getContentStyle(p_workbook));
                r++;
            }
        }
        paramsSheet.setColumnWidth(5, 30 * 256);
        Cell cell_F_Header = getCell(secRow, 5);
        if (data.allActivities)
        {
        	cell_F_Header.setCellValue(bundle
                    .getString("lb_activity_name")
                    + ": "
                    + bundle.getString("all"));
        	cell_F_Header.setCellStyle(getContentStyle(p_workbook));
        }
        else
        {
        	cell_F_Header.setCellValue(bundle
                    .getString("lb_activity_name")
                    + ": ");
        	cell_F_Header.setCellStyle(getContentStyle(p_workbook));
            Iterator iter = data.activityNameList.iterator();
            int r = 2;
            while (iter.hasNext())
            {
                String activityName = (String) iter.next();
                Activity activity = (Activity) ServerProxy.getJobHandler()
                        .getActivity(activityName);
                Row theRow = getRow(paramsSheet, r);
                Cell cell_F = getCell(theRow, 5);
                cell_F.setCellValue(activity.getDisplayName());
                cell_F.setCellStyle(getContentStyle(p_workbook));
                r++;
            }
        }
    }
    
    private String getMessage(String creationLabel, String creationOptionLabel)
    {
        String condition = request.getParameter(creationOptionLabel);

        String result = "N/A";
        result = request.getParameter(creationLabel) + " ";

        if (SearchCriteriaParameters.NOW.equals(condition))
            result = result + bundle.getString("lb_now");
        else if (SearchCriteriaParameters.DAYS_AGO.equals(condition))
            result = result + bundle.getString("lb_days_ago");
        else if (SearchCriteriaParameters.HOURS_AGO.equals(condition))
            result = result + bundle.getString("lb_hours_ago");
        else if (SearchCriteriaParameters.WEEKS_AGO.equals(condition))
            result = result + bundle.getString("lb_weeks_ago");
        else if (SearchCriteriaParameters.MONTHS_AGO.equals(condition))
            result = result + bundle.getString("lb_months_ago");

        return result;
    }
        
    private double asDouble(BigDecimal d)
    {
        // BigDecimal v = d.setScale(SCALE_EXCEL,BigDecimal.ROUND_HALF_UP);
        return d.doubleValue();
    }
    
    private CellStyle getHeaderStyle(Workbook p_workbook){
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
    
    private CellStyle getDateStyle(Workbook p_workbook) throws Exception
    {
        if (dateStyle == null)
        {
        	Font defaultFont = p_workbook.createFont();
            defaultFont.setFontName("Arial");
            defaultFont.setFontHeightInPoints((short) 10);

            DataFormat format = p_workbook.createDataFormat();
            CellStyle cs = p_workbook.createCellStyle();
            cs.setFont(defaultFont);
            cs.setDataFormat(format.getFormat(DEFAULT_DATE_FORMAT));
            cs.setWrapText(false);
            
            dateStyle = cs;
        }

        return dateStyle;
    }
    
    private CellStyle getMoneyStyle(Workbook p_workbook) throws Exception
    {
        if (moneyStyle == null)
        {
        	Font defaultFont = p_workbook.createFont();
            defaultFont.setFontName("Arial");
            defaultFont.setFontHeightInPoints((short) 10);
        	
        	DataFormat euroNumberFormat = p_workbook.createDataFormat();
            CellStyle cs = p_workbook.createCellStyle();
            cs.setFont(defaultFont);
            cs.setWrapText(false);
            cs.setDataFormat(euroNumberFormat
            		.getFormat(CurrencyThreadLocal.getMoneyFormatString()));
            
            moneyStyle = cs;
        }

        return moneyStyle;
    }
    
    private CellStyle getWrongJobStyle(Workbook p_workbook) throws Exception
    {
        if (wrongJobStyle == null)
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
        if (subTotalStyle == null)
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
        if (totalMoneyStyle == null)
        {
        	DataFormat euroNumberFormat = p_workbook.createDataFormat();
            CellStyle cs = p_workbook.createCellStyle();
            cs.setDataFormat(euroNumberFormat
            		.getFormat(CurrencyThreadLocal.getMoneyFormatString()));
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
        return ReportConstants.REVIEWER_VENDOR_PO_REPORT;
    }
}
