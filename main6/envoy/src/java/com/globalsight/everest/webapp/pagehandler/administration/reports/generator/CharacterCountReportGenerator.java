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
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.page.SourcePage;
import com.globalsight.everest.page.TargetPage;
import com.globalsight.everest.persistence.tuv.SegmentTuUtil;
import com.globalsight.everest.persistence.tuv.SegmentTuvUtil;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.tuv.Tuv;
import com.globalsight.everest.util.comparator.GlobalSightLocaleComparator;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.pagehandler.administration.reports.ReportConstants;
import com.globalsight.everest.webapp.pagehandler.administration.reports.ReportHelper;
import com.globalsight.everest.webapp.pagehandler.administration.reports.bo.ReportsData;
import com.globalsight.everest.webapp.pagehandler.administration.users.UserUtil;
import com.globalsight.everest.workflowmanager.Workflow;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.resourcebundle.ResourceBundleConstants;
import com.globalsight.util.resourcebundle.SystemResourceBundle;

/**
 * Character Count Report Generator
 */
public class CharacterCountReportGenerator implements ReportGenerator
{
	private HttpServletRequest request = null;
    protected Locale m_uiLocale = null;
    protected String m_companyName = "";
    protected List<Long> m_jobIDS = new ArrayList<Long>();
    protected List<GlobalSightLocale> m_targetLocales = new ArrayList<GlobalSightLocale>();
    String m_userId;
    private ResourceBundle m_bundle;

    private CellStyle contentStyle = null;
    private CellStyle headerStyle = null;

    public static final int LANGUAGE_HEADER_ROW = 3;
    public static final int LANGUAGE_INFO_ROW = 4;
    public static final int SEGMENT_HEADER_ROW = 6;
    public static final int SEGMENT_START_ROW = 7;
    
    public CharacterCountReportGenerator(String p_currentCompanyName)
    {
        m_companyName = p_currentCompanyName;
        CompanyThreadLocal.getInstance().setValue(m_companyName);
        m_uiLocale = Locale.US;
        m_bundle = SystemResourceBundle.getInstance().getResourceBundle(
                ResourceBundleConstants.LOCALE_RESOURCE_NAME, m_uiLocale);
    }
    
    public CharacterCountReportGenerator(String p_currentCompanyName, String p_userId)
    {
        m_companyName = p_currentCompanyName;
        CompanyThreadLocal.getInstance().setValue(m_companyName);
        m_uiLocale = Locale.US;
        m_bundle = SystemResourceBundle.getInstance().getResourceBundle(
                ResourceBundleConstants.LOCALE_RESOURCE_NAME, m_uiLocale);
        m_userId = p_userId;
    }

    /**
     * Constructor. Create a helper to generate the report
     * 
     * @param p_request
     *            the request
     * @param p_response
     *            the response
     * @throws Exception
     */
    public CharacterCountReportGenerator(HttpServletRequest p_request,
            HttpServletResponse p_response) throws Exception
    {
    	request = p_request;
        HttpSession session = p_request.getSession();
        m_userId = (String) session.getAttribute(WebAppConstants.USER_NAME);
        m_uiLocale = (Locale) p_request.getSession().getAttribute(
                WebAppConstants.UILOCALE);
        if (m_uiLocale == null)
        {
            m_uiLocale = Locale.US;
        }

        m_bundle = PageHandler.getBundle(request.getSession());

        m_companyName = UserUtil.getCurrentCompanyName(p_request);
        CompanyThreadLocal.getInstance().setValue(m_companyName);

        if (p_request.getParameter(ReportConstants.JOB_IDS) != null)
        {
            m_jobIDS = ReportHelper.getListOfLong(p_request
                    .getParameter(ReportConstants.JOB_IDS));
            GlobalSightLocaleComparator comparator = new GlobalSightLocaleComparator(
                    GlobalSightLocaleComparator.ISO_CODE, m_uiLocale);
            m_targetLocales = ReportHelper.getTargetLocaleList(p_request
                    .getParameterValues(ReportConstants.TARGETLOCALE_LIST),
                    comparator);
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
     *            Company Name, used for checking permission.
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
     * Generates Excel reports
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
        int finishedJobNum = 0;
        for (long jobID : p_jobIDS)
        {               
            // Cancel generate reports.
            if (isCancelled())
                return null;

            Job job = ServerProxy.getJobHandler().getJobById(jobID);
            if (job == null)
                continue;
            setAllCellStyleNull();
            
            File file = ReportHelper.getXLSReportFile(getReportType(), job);
            Workbook workBook = new SXSSFWorkbook();
            createReport(workBook, job, p_targetLocales);
            
            FileOutputStream out = new FileOutputStream(file);
            workBook.write(out);
            out.close();
            ((SXSSFWorkbook)workBook).dispose();
            workBooks.add(file);

            // Sets Reports Percent.
            setPercent(++finishedJobNum);
        }

        return ReportHelper.moveReports(workBooks, m_userId);
    }

    /**
     * Create one excel file report per job. Within each excel spreadsheet, use
     * one tab per target language.
     * 
     * @param p_workbook
     *            excel file
     * @param p_job
     *            job
     * @param p_targetLangIDS
     *            target language id list
     * @throws Exception
     */
    private void createReport(Workbook p_workbook, Job p_job,
            List<GlobalSightLocale> p_targetLangIDS) throws Exception
    {
        List<GlobalSightLocale> jobTL = ReportHelper.getTargetLocals(p_job);
        for (GlobalSightLocale trgLocale : p_targetLangIDS)
        {
            if (!jobTL.contains(trgLocale))
                continue;
            
            // Create One Report Sheet/Tab
            Sheet sheet = p_workbook.createSheet(trgLocale.toString());
            
            // add title
            addTitle(p_workbook, sheet);
            
            // Add Locale Pair Header
            addLanguageHeader(p_workbook, sheet);
            
            // Add Segment Header
            addSegmentHeader(p_workbook, sheet);
            
            // Write Language Information
            String srcLang = p_job.getSourceLocale().getDisplayName(m_uiLocale);
            String trgLang = trgLocale.getDisplayName(m_uiLocale);
            writeLanguageInfo(p_workbook, sheet, srcLang, trgLang, p_job);
            
            // Write Segments Information
            writeCharacterCountSegmentInfo(p_workbook, sheet, p_job, trgLang, "",
                    SEGMENT_START_ROW);
        }
    }

    /**
     * Add title to the sheet
     * 
     * @param p_workBook
     * @param p_sheet
     *            the sheet
     * @throws Exception
     */
    private void addTitle(Workbook p_workBook, Sheet p_sheet)
            throws Exception
    {
    	ResourceBundle bundle = m_bundle;
    	// Title font is black bold on white
        Font titleFont = p_workBook.createFont();
        titleFont.setUnderline(Font.U_NONE);
        titleFont.setFontName("Times");
        titleFont.setFontHeightInPoints((short) 14);
        titleFont.setBoldweight(Font.BOLDWEIGHT_BOLD);
        CellStyle cs = p_workBook.createCellStyle();
        cs.setFont(titleFont);
        
        Row titleRow = getRow(p_sheet, 0);
        Cell titleCell = getCell(titleRow, 0);
        titleCell.setCellValue(bundle.getString("character_count_report"));
        titleCell.setCellStyle(cs);
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
    	ResourceBundle bundle = m_bundle;
    	
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
        col++;
        
        Cell jobIdCell = getCell(langRow, col);
        jobIdCell.setCellValue(bundle.getString("jobinfo.jobid"));
        jobIdCell.setCellStyle(getHeaderStyle(p_workBook));
    }

    /**
     * Add segment header to the sheet
     * 
     * @param p_workBook
     * @param p_sheet
     *            the sheet
     * @throws Exception
     */
    private void addSegmentHeader(Workbook p_workBook, Sheet p_sheet) throws Exception
    {
    	ResourceBundle bundle = m_bundle;
        int col = 0;
        int row = SEGMENT_HEADER_ROW;
        Row segHeaderRow = getRow(p_sheet, row);

        Cell cell_A = getCell(segHeaderRow, col);
        cell_A.setCellValue(bundle.getString("lb_segment_id"));
        cell_A.setCellStyle(getHeaderStyle(p_workBook));
        p_sheet.setColumnWidth(col, 20 * 256);
        col++;
        
        Cell cell_B = getCell(segHeaderRow, col);
        cell_B.setCellValue(bundle.getString("lb_source_segment"));
        cell_B.setCellStyle(getHeaderStyle(p_workBook));
        p_sheet.setColumnWidth(col, 40 * 256);
        col++;
        
        Cell cell_C = getCell(segHeaderRow, col);
        cell_C.setCellValue(bundle.getString("lb_source_character_count"));
        cell_C.setCellStyle(getHeaderStyle(p_workBook));
        p_sheet.setColumnWidth(col, 30 * 256);
        col++;

        Cell cell_D = getCell(segHeaderRow, col);
        cell_D.setCellValue(bundle.getString("lb_target_segment"));
        cell_D.setCellStyle(getHeaderStyle(p_workBook));
        p_sheet.setColumnWidth(col, 40 * 256);
        col++;
        
        Cell cell_E = getCell(segHeaderRow, col);
        cell_E.setCellValue(bundle.getString("lb_target_character_count"));
        cell_E.setCellStyle(getHeaderStyle(p_workBook));
        p_sheet.setColumnWidth(col, 30 * 256);
    }

    /**
     * Write the Job information into the header
     * 
     * @param p_workbook
     *            workbook
     * @param p_sheet
     *            sheet
     * @param p_sourceLang
     *            the displayed source language
     * @param p_targetLang
     *            the displayed target language
     * @param p_job
     * 			  the job
     * @throws Exception
     */
    private void writeLanguageInfo(Workbook p_workbook, Sheet p_sheet,
            String p_sourceLang, String p_targetLang, Job p_job) throws Exception
    {
    	int col = 0;
        int row = LANGUAGE_INFO_ROW;
        Row langInfoRow = getRow(p_sheet, row);
        
        // Source Language
        Cell srcLangCell = getCell(langInfoRow, col++);
        srcLangCell.setCellValue(p_sourceLang);
        srcLangCell.setCellStyle(getContentStyle(p_workbook));
        
        // Target Language
        Cell trgLangCell = getCell(langInfoRow, col++);
        trgLangCell.setCellValue(p_targetLang);
        trgLangCell.setCellStyle(getContentStyle(p_workbook));
        
        // Job ID
        Cell jobIdCell = getCell(langInfoRow, col++);
        jobIdCell.setCellValue(p_job.getId());
        jobIdCell.setCellStyle(getContentStyle(p_workbook));
    }

    /**
     * Write segment information into each row of the sheet
     * 
     * @param p_workBook
     *            the workBook
     * @param p_sheet
     *            the sheet
     * @param p_job
     *            the job data for report
     * @param p_targetLang
     *            the target language
     * @param p_srcPageId
     * 			  the source page id
     * @param p_row
     *            the segment row in sheet
     * @throws Exception
     */
    private void writeCharacterCountSegmentInfo(Workbook p_workBook, Sheet p_sheet,
    		Job p_job, String p_targetLang, String p_srcPageId, int p_row)
            throws Exception
    {
        Vector<TargetPage> targetPages = new Vector<TargetPage>();

        long jobId = p_job.getId();

        for (Workflow workflow : p_job.getWorkflows())
        {
            if (Workflow.PENDING.equals(workflow.getState())
                    || Workflow.CANCELLED.equals(workflow.getState())
                    || Workflow.EXPORT_FAILED.equals(workflow.getState())
                    || Workflow.IMPORT_FAILED.equals(workflow.getState()))
            {
                continue;
            }
            if (p_targetLang
                    .equals(workflow.getTargetLocale().getDisplayName()))
            {
                targetPages = workflow.getTargetPages();
            }
        }
        if (!targetPages.isEmpty())
        {
            String sourceSegmentString = null;
            String targetSegmentString = null;

            for (int i = 0; i < targetPages.size(); i++)
            {
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
                List sourceTuvs = SegmentTuvUtil.getSourceTuvs(sourcePage);
                List targetTuvs = SegmentTuvUtil.getTargetTuvs(targetPage);

                for (int j = 0; j < targetTuvs.size(); j++)
                {
                    int col = 0;
                    Tuv targetTuv = (Tuv) targetTuvs.get(j);
                    Tuv sourceTuv = (Tuv) sourceTuvs.get(j);
                    sourceSegmentString = sourceTuv.getGxmlElement()
                            .getTextValue();
                    targetSegmentString = targetTuv.getGxmlElement()
                            .getTextValue();
                    
                    CellStyle contentStyle = getContentStyle(p_workBook);
                    Row currentRow = getRow(p_sheet, p_row);
                    
                    // Segment id
                    Cell cell_A = getCell(currentRow, col);
                    cell_A.setCellValue(sourceTuv.getTu(jobId).getId());
                    cell_A.setCellStyle(contentStyle);
                    col++;

                    // Source segment
                    Cell cell_B = getCell(currentRow, col);
                    cell_B.setCellValue(sourceSegmentString);
                    cell_B.setCellStyle(contentStyle);
                    col++;;

                    // Source Character count
                    Cell cell_C = getCell(currentRow, col);
                    cell_C.setCellValue(sourceSegmentString.length());
                    cell_C.setCellStyle(contentStyle);
                    col++;

                    // Target segment
                    Cell cell_D = getCell(currentRow, col);
                    cell_D.setCellValue(targetSegmentString);
                    cell_D.setCellStyle(contentStyle);
                    col++;

                    // Target Character count
                    Cell cell_E = getCell(currentRow, col);
                    cell_E.setCellValue(targetSegmentString.length());
                    cell_E.setCellStyle(contentStyle);
                    
                    p_row++;
                }
            }
        }
    }

    private void setAllCellStyleNull()
    {
        this.headerStyle = null;
        this.contentStyle = null;
    }
    
    private CellStyle getHeaderStyle(Workbook p_workbook) throws Exception
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
    public String getReportType()
    {
        return ReportConstants.CHARACTER_COUNT_REPORT;
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
}