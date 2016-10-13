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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.pagehandler.administration.reports.bo.ReportsData;
import com.globalsight.everest.webapp.pagehandler.administration.reports.util.ProjectWorkflowData;
import com.globalsight.everest.webapp.pagehandler.administration.reports.util.SlaReportDataAssembler;
import com.globalsight.everest.webapp.pagehandler.administration.reports.util.XlsReportData;
import com.globalsight.everest.webapp.pagehandler.administration.users.UserUtil;
import com.globalsight.util.IntHolder;
import com.globalsight.util.SortUtil;

public class SlaXlsReportHelper
{
    private static Logger s_logger = Logger.getLogger(SlaXlsReportHelper.class);
    private static final String euroJavaNumberFormat = "\u20AC###,###,##0.000;(\u20AC###,###,##0.000)";
    private HttpServletRequest request = null;
    private ResourceBundle bundle = null;
    private String userId = null;
    private HttpServletResponse response = null;
    private XlsReportData data = null;
    private CellStyle headerStyle = null;
    private CellStyle contentStyle = null;
    private CellStyle contentRightStyle = null;
    private static Map<String, ReportsData> m_reportsDataMap = new ConcurrentHashMap<String, ReportsData>();

    public SlaXlsReportHelper(HttpServletRequest p_request,
            HttpServletResponse p_response) throws Exception
    {
        this.request = p_request;
        this.response = p_response;
        bundle = PageHandler.getBundle(p_request.getSession());
        
        HttpSession session=p_request.getSession(false);
        userId=(String) session.getAttribute(WebAppConstants.USER_NAME);

        String companyName = UserUtil.getCurrentCompanyName(p_request);
        if (!UserUtil.isBlank(companyName))
        {
            CompanyThreadLocal.getInstance().setValue(companyName);
        }

        SlaReportDataAssembler reportDataAssembler = new SlaReportDataAssembler(
                p_request);

        reportDataAssembler.setJobIdList();
        reportDataAssembler.setProjectIdList(userId);
        reportDataAssembler.setStatusList();
        reportDataAssembler.setTargetLangList();
        reportDataAssembler.setDateFormat();
        reportDataAssembler.setProjectData();

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
        userId = (String) request.getSession(false).getAttribute(
                WebAppConstants.USER_NAME);
        List<Long> reportJobIDS = new ArrayList<Long>(data.jobIdList);
        // Cancel Duplicate Request
        if (ReportHelper.checkReportsDataInProgressStatus(userId,
                reportJobIDS, getReportType()))
        {
            response.sendError(response.SC_NO_CONTENT);
            return;
        }
        // Set ReportsData.
        ReportHelper.setReportsData(userId, reportJobIDS, getReportType(),
                0, ReportsData.STATUS_INPROGRESS);

        Workbook workbook = new SXSSFWorkbook();

        HashMap projectMap = data.projectMap;

        data.generalSheet = workbook.createSheet(bundle.getString("lb_sheet")
                + "1");

        addTitle(workbook);
        
        addHeader(workbook);

        IntHolder row = new IntHolder(4);
        writeProjectData(workbook, projectMap, row);

        ServletOutputStream out = response.getOutputStream();
        workbook.write(out);
        out.close();
        ((SXSSFWorkbook)workbook).dispose();

        // Set ReportsData.
        ReportHelper.setReportsData(userId, reportJobIDS, getReportType(),
                100, ReportsData.STATUS_FINISHED);
    }

    private void addTitle(Workbook p_workbook) throws Exception
    {
    	Sheet theSheet = data.generalSheet;
        // title font is black bold on white
        Font titleFont = p_workbook.createFont();
        titleFont.setUnderline(Font.U_NONE);
        titleFont.setFontName("Arial");
        titleFont.setFontHeightInPoints((short) 14);
        titleFont.setBoldweight(Font.BOLDWEIGHT_BOLD);
        titleFont.setColor(IndexedColors.BLACK.getIndex());
        CellStyle titleStyle = p_workbook.createCellStyle();
        titleStyle.setWrapText(false);
        titleStyle.setFont(titleFont);
        
        Cell titleCell = getCell(getRow(theSheet, 0), 0);
        titleCell.setCellValue(bundle
                .getString("translation_sla_performance"));
        titleCell.setCellStyle(titleStyle);
        theSheet.setColumnWidth(0, 20 * 256);
    }
    
    /**
     * Adds the table header for the Dell Matches sheet
     * 
     */
    private void addHeader(Workbook p_workbook) throws Exception
    {
        Sheet theSheet = data.generalSheet;
        int c = 0;
        Row headerRow = getRow(theSheet, 2);
        Cell cell_A = getCell(headerRow, c);
        cell_A.setCellValue(bundle.getString("lb_job_id"));
        cell_A.setCellStyle(getHeaderStyle(p_workbook));
        theSheet.addMergedRegion(new CellRangeAddress(2, 3, c, c));
        setRegionStyle(theSheet, new CellRangeAddress(2, 3, c, c),
        		getHeaderStyle(p_workbook));
        theSheet.setColumnWidth(c, 7 * 256);
        c++;
        Cell cell_B = getCell(headerRow, c);
        cell_B.setCellValue(bundle.getString("lb_job_name"));
        cell_B.setCellStyle(getHeaderStyle(p_workbook));
        theSheet.addMergedRegion(new CellRangeAddress(2, 3, c, c));
        setRegionStyle(theSheet, new CellRangeAddress(2, 3, c, c), 
        		getHeaderStyle(p_workbook));
        theSheet.setColumnWidth(c, 40 * 256);
        c++;
        Cell cell_C = getCell(headerRow, c);
        cell_C.setCellValue(bundle.getString("lb_workflow"));
        cell_C.setCellStyle(getHeaderStyle(p_workbook));
        theSheet.addMergedRegion(new CellRangeAddress(2, 3, c, c));
        setRegionStyle(theSheet, new CellRangeAddress(2, 3, c, c), 
        		getHeaderStyle(p_workbook));
        theSheet.setColumnWidth(c, 25 * 256);
        c++;
        Cell cell_D = getCell(headerRow, c);
        cell_D.setCellValue(bundle.getString("lb_language"));
        cell_D.setCellStyle(getHeaderStyle(p_workbook));
        theSheet.addMergedRegion(new CellRangeAddress(2, 3, c, c));
        setRegionStyle(theSheet, new CellRangeAddress(2, 3, c, c),
        		getHeaderStyle(p_workbook));
        theSheet.setColumnWidth(c, 12 * 256);
        c++;
        Cell cell_E = getCell(headerRow, c);
        cell_E.setCellValue(bundle.getString("lb_word_count"));
        cell_E.setCellStyle(getHeaderStyle(p_workbook));
        theSheet.addMergedRegion(new CellRangeAddress(2, 3, c, c));
        setRegionStyle(theSheet, new CellRangeAddress(2, 3, c, c), 
        		getHeaderStyle(p_workbook));
        theSheet.setColumnWidth(c, 7 * 256);
        c++;
        Cell cell_F = getCell(headerRow, c);
        cell_F.setCellValue(bundle.getString("lb_current_activity"));
        cell_F.setCellStyle(getHeaderStyle(p_workbook));
        theSheet.addMergedRegion(new CellRangeAddress(2, 3, c, c));
        setRegionStyle(theSheet, new CellRangeAddress(2, 3, c, c), 
        		getHeaderStyle(p_workbook));
        theSheet.setColumnWidth(c, 14 * 256);
        c++;
        Cell cell_G = getCell(headerRow, c);
        cell_G.setCellValue(bundle.getString("lb_translation_start_date"));
        cell_G.setCellStyle(getHeaderStyle(p_workbook));
        theSheet.addMergedRegion(new CellRangeAddress(2, 3, c, c));
        setRegionStyle(theSheet, new CellRangeAddress(2, 3, c, c),
        		getHeaderStyle(p_workbook));
        theSheet.setColumnWidth(c, 28 * 256);
        c++;
        Cell cell_H = getCell(headerRow, c);
        cell_H.setCellValue(bundle.getString("lb_translation_due_date"));
        cell_H.setCellStyle(getHeaderStyle(p_workbook));
        theSheet.addMergedRegion(new CellRangeAddress(2, 3, c, c));
        setRegionStyle(theSheet, new CellRangeAddress(2, 3, c, c), 
        		getHeaderStyle(p_workbook));
        theSheet.setColumnWidth(c, 28 * 256);
        c++;
        Cell cell_I = getCell(headerRow, c);
        cell_I.setCellValue(bundle.getString("lb_translation_finish_date"));
        cell_I.setCellStyle(getHeaderStyle(p_workbook));
        theSheet.addMergedRegion(new CellRangeAddress(2, 3, c, c));
        setRegionStyle(theSheet, new CellRangeAddress(2, 3, c, c),
        		getHeaderStyle(p_workbook));
        theSheet.setColumnWidth(c, 28 * 256);
        c++;
        Cell cell_J = getCell(headerRow, c);
        cell_J.setCellValue(bundle.getString("lb_on_time"));
        cell_J.setCellStyle(getHeaderStyle(p_workbook));
        theSheet.addMergedRegion(new CellRangeAddress(2, 3, c, c));
        setRegionStyle(theSheet, new CellRangeAddress(2, 3, c, c),
        		getHeaderStyle(p_workbook));
        theSheet.setColumnWidth(c, 28 * 256);
        c++;
        Cell cell_K = getCell(headerRow, c);
        cell_K.setCellValue(bundle.getString("lb_leadtime"));
        cell_K.setCellStyle(getHeaderStyle(p_workbook));
        theSheet.addMergedRegion(new CellRangeAddress(2, 3, c, c));
        setRegionStyle(theSheet, new CellRangeAddress(2, 3, c, c), 
        		getHeaderStyle(p_workbook));
        theSheet.setColumnWidth(c, 12 * 256);
        c++;
        Cell cell_L = getCell(headerRow, c);
        cell_L.setCellValue(bundle.getString("lb_actual_performance"));
        cell_L.setCellStyle(getHeaderStyle(p_workbook));
        theSheet.addMergedRegion(new CellRangeAddress(2, 3, c, c));
        setRegionStyle(theSheet, new CellRangeAddress(2, 3, c, c), 
        		getHeaderStyle(p_workbook));
        theSheet.setColumnWidth(c, 14 * 256);
    }

    private void writeProjectData(Workbook p_workbook, HashMap p_projectMap,
    		IntHolder p_row) throws Exception
    {
        Sheet theSheet = data.generalSheet;
        ArrayList projects = new ArrayList(p_projectMap.keySet());
        SortUtil.sort(projects);
        Iterator projectIter = projects.iterator();

        SimpleDateFormat dateFormat = data.dateFormat;

        while (projectIter.hasNext())
        {
            String jobName = (String) projectIter.next();
            HashMap localeMap = (HashMap) p_projectMap.get(jobName);
            ArrayList locales = new ArrayList(localeMap.keySet());
            SortUtil.sort(locales);
            Iterator localeIter = locales.iterator();

            while (localeIter.hasNext())
            {
                int row = p_row.getValue();
                Row theRow = getRow(theSheet, row);
                int col = 0;
                String localeId = (String) localeIter.next();
                ProjectWorkflowData data = (ProjectWorkflowData) localeMap
                        .get(localeId);

                // Job Id
                Cell cell_A = getCell(theRow, col++);
                cell_A.setCellValue(data.jobId);
                cell_A.setCellStyle(getContentRightStyle(p_workbook));

                // Job Name
                Cell cell_B = getCell(theRow, col++);
                cell_B.setCellValue(data.jobName);
                cell_B.setCellStyle(getContentStyle(p_workbook));

                // Workflow Name
                Cell cell_C = getCell(theRow, col++);
                cell_C.setCellValue(data.workflowName);
                cell_C.setCellStyle(getContentStyle(p_workbook));

                // Lang
                Cell cell_D = getCell(theRow, col++);
                cell_D.setCellValue(data.targetLang);
                cell_D.setCellStyle(getContentStyle(p_workbook));

                // Word Count
                Cell cell_E = getCell(theRow, col++);
                cell_E.setCellValue(data.totalWordCount);
                cell_E.setCellStyle(getContentRightStyle(p_workbook));

                // Current Activity
                Cell cell_F = getCell(theRow, col++);
                cell_F.setCellValue(data.currentActivityName);
                cell_F.setCellStyle(getContentStyle(p_workbook));

                // Translation Start Date
                Cell cell_G = getCell(theRow, col++);
                cell_G.setCellValue(dateFormat
                        .format(data.creationDate));
                cell_G.setCellStyle(getContentStyle(p_workbook));

                // Translation Due Date
                Cell cell_H = getCell(theRow, col++);
                if (data.estimatedTranslateCompletionDate == null)
                {
                    cell_H.setCellValue(bundle
                            .getString("lb_no_translation"));
                    cell_H.setCellStyle(getContentStyle(p_workbook));
                }
                else
                {
                	cell_H.setCellValue(dateFormat
                            .format(data.estimatedTranslateCompletionDate));
                	cell_H.setCellStyle(getContentStyle(p_workbook));
                }

                // Translation Finish Date
                Cell cell_I = getCell(theRow, col++);
                if (data.actualTranslateCompletionDate == null)
                {
                	cell_I.setCellValue("");
                	cell_I.setCellStyle(getContentStyle(p_workbook));
                }
                else
                {
                	cell_I.setCellValue(dateFormat
                            .format(data.actualTranslateCompletionDate));
                	cell_I.setCellStyle(getContentStyle(p_workbook));
                }

                // On Time
                Cell cell_J = getCell(theRow, col++);
                if (data.actualTranslateCompletionDate == null)
                {
                	cell_J.setCellValue("");
                	cell_J.setCellStyle(getContentStyle(p_workbook));
                }
                else if (data.actualTranslateCompletionDate
                        .before(data.estimatedTranslateCompletionDate))
                {
                	cell_J.setCellValue(bundle.getString("lb_yes"));
                	cell_J.setCellStyle(getContentStyle(p_workbook));
                }
                else
                {
                	cell_J.setCellValue(bundle.getString("lb_no"));
                	cell_J.setCellStyle(getContentStyle(p_workbook));
                }

                // Leadtime
                if ((data.leadtime == null) || ("".equals(data.leadtime)))
                {
                    // keep blank if "No translation"
                    col++;
                }
                else
                {
                	Cell cell_K = getCell(theRow, col++);
                    cell_K.setCellValue(data.leadtime);
                    cell_K.setCellStyle(getContentStyle(p_workbook));
                }

                // Actual Performance
                Cell cell_L = getCell(theRow, col++);
                if ((data.actualPerformance == null)
                        || ("".equals(data.actualPerformance)))
                {
                	cell_L.setCellValue(bundle
                            .getString("lb_not_completed"));
                	cell_L.setCellStyle(getContentStyle(p_workbook));
                }
                else
                {
                	cell_L.setCellValue(data.actualPerformance);
                	cell_L.setCellStyle(getContentStyle(p_workbook));
                }

                p_row.inc();
            }
        }

        p_row.inc();
        p_row.inc();
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
    
    private CellStyle getHeaderStyle(Workbook p_workbook) throws Exception
    {
        if (headerStyle == null)
        {
            Font font = p_workbook.createFont();
            font.setBoldweight(Font.BOLDWEIGHT_BOLD);
            font.setColor(IndexedColors.BLACK.getIndex());
            font.setUnderline(Font.U_NONE);
            font.setFontName("Arial");
            font.setFontHeightInPoints((short) 9);

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
    
    private CellStyle getContentRightStyle(Workbook p_workbook) throws Exception
    {
        if (contentRightStyle == null)
        {
            CellStyle style = p_workbook.createCellStyle();
            style.setWrapText(true);
            style.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
            Font font = p_workbook.createFont();
            font.setFontName("Arial");
            font.setFontHeightInPoints((short) 10);
            style.setFont(font);

            contentRightStyle = style;
        }

        return contentRightStyle;
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
        return ReportConstants.TRANSLATION_SLA_PERFORMANCE_REPORT;
    }
}
