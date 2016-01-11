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

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.math.BigDecimal;
import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

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
import org.apache.poi.xssf.usermodel.XSSFClientAnchor;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.ss.usermodel.Drawing;

import com.globalsight.cxe.entity.fileprofile.FileProfile;
import com.globalsight.cxe.entity.knownformattype.KnownFormatType;
import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.costing.BigDecimalHelper;
import com.globalsight.everest.costing.Cost;
import com.globalsight.everest.costing.CostByWordCount;
import com.globalsight.everest.costing.CostingException;
import com.globalsight.everest.costing.Currency;
import com.globalsight.everest.costing.FlatSurcharge;
import com.globalsight.everest.costing.PercentageSurcharge;
import com.globalsight.everest.costing.Surcharge;
import com.globalsight.everest.foundation.L10nProfile;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.jobhandler.JobSearchParameters;
import com.globalsight.everest.projecthandler.Project;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.taskmanager.Task;
import com.globalsight.everest.util.system.SystemConfigParamNames;
import com.globalsight.everest.util.system.SystemConfiguration;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.pagehandler.administration.reports.ReportConstants;
import com.globalsight.everest.webapp.pagehandler.administration.reports.ReportHelper;
import com.globalsight.everest.webapp.pagehandler.administration.reports.util.ReportUtil;
import com.globalsight.everest.webapp.pagehandler.administration.users.UserUtil;
import com.globalsight.everest.webapp.pagehandler.projects.workflows.JobSearchConstants;
import com.globalsight.everest.workflowmanager.Workflow;
import com.globalsight.util.AmbFileStoragePathUtils;
import com.globalsight.util.GeneralException;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.IntHolder;
import com.globalsight.util.JfreeCharUtil;
import com.globalsight.util.SortUtil;

/**
 * Online Jobs Report Generator
 * 
 * @Date Feb 14, 2012
 */
public class OnlineJobsReportGenerator implements
        ReportGenerator
{
    private static Logger logger = Logger
            .getLogger(OnlineJobsReportGenerator.class.getName());
    // defines a 0 format for a 3 decimal precision point BigDecimal
    private static final String BIG_DECIMAL_ZERO_STRING = "0.000";
    // For "Dell Review Report Issue".
    private static String DELL_REVIEW = "Dell_Review";
    private static ResourceBundle m_bundle = null;
    private static final int MONTH_SHEET = 0;
    private static final int MONTH_REVIEW_SHEET = 1;
    // the big decimal scale to use for internal math
    private static int SCALE = 3;
    // For "Add Job Id into online job report" issue
    private boolean isJobIdVisible = true;
    private Calendar m_calendar = Calendar.getInstance();
    /* The symbol of the currency from the request */
    private String symbol = null;

    private Hashtable<String, List<Integer>> totalCost = new Hashtable<String, List<Integer>>();
    private HashMap<String, Double> totalCostDate = new HashMap<String, Double>();
    private String totalCol = null;
    static final String SEPARATOR = "!!gs";
    private MyData m_data;
    private HttpServletRequest m_request = null;
    private CellStyle headerStyle = null;
    private CellStyle contentStyle = null;
    private CellStyle redCellStyle = null;
    private CellStyle moneyStyle = null;
    private CellStyle dateStyle = null;
    private CellStyle failedDateStyle = null;
    private CellStyle failedTimeStyle = null;
    private CellStyle failedMoneyStyle = null;
    private CellStyle wrongJobStyle = null;
    private CellStyle subTotalStyle = null;
    private CellStyle totalMoneyStyle = null;
    private SimpleDateFormat dateFormat = null;
    private SimpleDateFormat timeFormat = null;
    private String m_companyName = null;
    private String m_userId = null;

    public OnlineJobsReportGenerator(HttpServletRequest p_request,
            HttpServletResponse p_response) throws Exception
    {
        HttpSession session = p_request.getSession(false);
        m_userId = (String) session.getAttribute(WebAppConstants.USER_NAME);
        m_request = p_request;
        m_bundle = PageHandler.getBundle(p_request.getSession());
        m_companyName = UserUtil.getCurrentCompanyName(p_request);
        CompanyThreadLocal.getInstance().setValue(m_companyName);
        m_data = new MyData();
        m_data.setCurrency(p_request.getParameter("currency"));
        symbol = ReportUtil.getCurrencySymbol(m_data.getCurrency());
        m_data.setTradosStyle(p_request.getParameter("reportStyle"));
        m_data.setDateFormatString(p_request.getParameter("dateFormat"));
        setProjectIdList(p_request);
        setLocpIdList(p_request);
        setTargetLocales(p_request);
        // get all the jobs that were originally imported with the wrong project
        // the users want to pretend that these jobs are in this project
        if (p_request.getParameterValues("status") != null)
            getJobsInWrongProject();
    }

    public void sendReports(List<Long> p_jobIDS,
            List<GlobalSightLocale> p_targetLocales,
            HttpServletResponse p_response) throws Exception
    {
        File[] reports = generateReports(p_jobIDS, p_targetLocales);
        ReportHelper.sendFiles(reports, getReportType(), p_response);
    }

    @Override
    public File[] generateReports(List<Long> p_jobIDS,
            List<GlobalSightLocale> p_targetLocales) throws Exception
    {
        // Generate Report from HttpServletRequest.
        if (p_jobIDS == null || p_jobIDS.size() == 0)
        {
            return generateReport(m_request);
        }

        File file = ReportHelper.getXLSReportFile(getReportType(), null);
        Workbook workbook = new SXSSFWorkbook();
        HashSet<Job> jobs = new HashSet<Job>(
                ReportHelper.getJobListByIDS(p_jobIDS));
        m_data.wantsAllLocales = true;
        m_data.wantsAllProjects = true;
        m_data.setDateFormatString("MM/dd/yy hh:mm:ss a z");
        m_data.jobIds = p_jobIDS;
        HashMap<String, HashMap<String, ProjectWorkflowData>> projectMap = reportDataMap(
                jobs, false, false);
        if (projectMap != null && projectMap.size() > 0)
        {
            // Create sheets in Workbook.
            createSheets(workbook, projectMap, false);
            FileOutputStream out = new FileOutputStream(file);
            workbook.write(out);
            out.close();
            ((SXSSFWorkbook)workbook).dispose();
            List<File> workBooks = new ArrayList<File>();
            workBooks.add(file);
            return ReportHelper.moveReports(workBooks, m_userId);
        }
        else
        {
            return new File[0];
        }
    }

    /**
     * Generates the Excel report as a temp file and returns the temp file. The
     * report consists of all jobs for the given PM, this year, on a month by
     * month basis.
     * 
     * @return File
     * @exception Exception
     */
    private File[] generateReport(HttpServletRequest p_request)
            throws Exception
    {
        String[] months = getMonths();
        File file = ReportHelper.getXLSReportFile(getReportType(), null);
        Workbook p_workbook = new SXSSFWorkbook();
        int currentMonth = m_calendar.get(Calendar.MONTH);
        int year;
        if (p_request.getParameter("year") != null)
        {
            year = Integer.parseInt(p_request.getParameter("year"));
        }
        else
        {
            year = m_calendar.get(Calendar.YEAR);
        }
        if (year != m_calendar.get(Calendar.YEAR))
            currentMonth = 11;

        boolean wroteSomething = false;
        boolean recalculateFinishedWorkflow = false;
        String recalcParam = p_request.getParameter("recalc");
        if (recalcParam != null && recalcParam.length() > 0)
        {
            recalculateFinishedWorkflow = java.lang.Boolean
                    .valueOf(recalcParam).booleanValue();
        }

        // For "Dell Review Report Issue"
        // Gets the tag that marks whether needs to separates "Dell_Review" or
        // not.
        // Default value is "false".
        boolean includeExReview = false;

        String reviewParam = p_request.getParameter("review");
        if ((reviewParam != null) && (reviewParam.length() > 0))
        {
            includeExReview = java.lang.Boolean.valueOf(reviewParam)
                    .booleanValue();
        }

        // For "Add Job Id into online job report" issue
        isJobIdVisible = isJobIdVisible(p_request);
        String isReportForYear = p_request.getParameter("reportForThisYear");
        String isReportForDetail = p_request.getParameter("reportForDetail");
        // Generate Report By Year
        if (isReportForYear != null && isReportForYear.equals("on"))
        {
            int i = 0;
            for (i = 0; i <= currentMonth; i++)
            {
                // For "Dell Review Report Issue"
                // Add new parameter "includeExReview" to the signature.
                HashMap<String, HashMap<String, ProjectWorkflowData>> projectMap = getProjectDataForMonth(
                        p_request, i, recalculateFinishedWorkflow,
                        includeExReview);

                // Cancel the report.
                if (isCancelled())
                    return new File[0];

                if (projectMap.size() > 0)
                {
                    // This sheets array has two elements.
                    // sheets[MONTH_SHEET], the original monthSheet.
                    // sheets[MONTH_REVIEW_SHEET], new monthly Dell_Review
                    // sheet.
                    Sheet[] sheets = new Sheet[2];
                    IntHolder[] rows = new IntHolder[2];

                    String sheetTitle = months[i] + " " + year;
                    sheets[MONTH_SHEET] = p_workbook.createSheet(sheetTitle);
                    if (m_data.isTradosStyle())
                    {
                    	addTitle(p_workbook, sheets[MONTH_SHEET]);
                        addHeaderTradosStyle(p_workbook, sheets[MONTH_SHEET], MONTH_SHEET);
                    }
                    else
                    {
                    	addTitle(p_workbook, sheets[MONTH_SHEET]);
                        addHeader(p_workbook, sheets[MONTH_SHEET], MONTH_SHEET);
                    }

                    rows[MONTH_SHEET] = new IntHolder(4);

                    if (includeExReview)
                    {
                        String sheetReviewTitle = m_bundle
                                .getString("lb_review")
                                + " "
                                + months[i]
                                + " "
                                + year;
                        sheets[MONTH_REVIEW_SHEET] = p_workbook.createSheet(
                                sheetReviewTitle);
                        if (m_data.isTradosStyle())
                        {
                        	addTitle(p_workbook, sheets[MONTH_REVIEW_SHEET]);
                            addHeaderTradosStyle(p_workbook, sheets[MONTH_REVIEW_SHEET],
                                    MONTH_REVIEW_SHEET);
                        }
                        else
                        {
                        	addTitle(p_workbook, sheets[MONTH_REVIEW_SHEET]);
                            addHeader(p_workbook, sheets[MONTH_REVIEW_SHEET],
                                    MONTH_REVIEW_SHEET);
                        }
                        rows[MONTH_REVIEW_SHEET] = new IntHolder(4);
                    }

                    if (m_data.isTradosStyle())
                    {
                        writeProjectDataTradosStyle(projectMap,p_workbook, sheets,
                                includeExReview, rows);
                    }
                    else
                    {
                        writeProjectData(projectMap,p_workbook, sheets, includeExReview,
                                rows);
                    }
                    wroteSomething = true;
                }
            }

            if (!wroteSomething)
            {
                // just write out the first sheet to avoid Excel problems
                i = 0;
                String sheetTitle = months[currentMonth] + " " + year;
                Sheet monthSheet = p_workbook.createSheet(sheetTitle);
                if (m_data.isTradosStyle())
                {
                	addTitle(p_workbook, monthSheet);
                    addHeaderTradosStyle(p_workbook, monthSheet, MONTH_SHEET);
                }
                else
                {
                	addTitle(p_workbook, monthSheet);
                    addHeader(p_workbook, monthSheet, MONTH_SHEET);
                }

                if (includeExReview)
                {
                    String sheetReviewTitle = "Review " + months[currentMonth]
                            + " " + year;
                    Sheet monthReviewSheet = p_workbook.createSheet(
                            sheetReviewTitle);
                    if (m_data.isTradosStyle())
                    {
                    	addTitle(p_workbook, monthReviewSheet);
                        addHeaderTradosStyle(p_workbook, monthReviewSheet,
                                MONTH_REVIEW_SHEET);
                    }
                    else
                    {
                    	addTitle(p_workbook, monthReviewSheet);
                        addHeader(p_workbook, monthReviewSheet, MONTH_REVIEW_SHEET);
                    }
                }
            }

            // Create "Criteria" Sheet
            addParamsSheet(p_workbook,(String) p_request
                    .getParameter("year") , recalculateFinishedWorkflow);

            FileOutputStream out = new FileOutputStream(file);
            p_workbook.write(out);
            out.close();
            ((SXSSFWorkbook)p_workbook).dispose();
        }
        // Generate Report by Creation Date Range.
        else if (isReportForDetail != null && isReportForDetail.endsWith("on"))
        {
            // For "Dell Review Report Issue"
            // Add new parameter "includeExReview" to the signature.
            HashMap<String, HashMap<String, ProjectWorkflowData>> projectMap = getProjectDataForMonth(
                    p_request, 0, recalculateFinishedWorkflow, includeExReview);

            // Cancel the report.
            if (isCancelled())
                return new File[0];

            // Create sheets in Workbook.
            createSheets(p_workbook, projectMap, includeExReview);

            // Create "Criteria" Sheet
            addParamsSheet(p_workbook, (String) p_request
                    .getParameter("year"), recalculateFinishedWorkflow);
            
            FileOutputStream out = new FileOutputStream(file);
            p_workbook.write(out);
            out.close();
            ((SXSSFWorkbook)p_workbook).dispose();
        }

        List<File> workBooks = new ArrayList<File>();
        workBooks.add(file);
        return ReportHelper.moveReports(workBooks, m_userId);
    }

    /**
     * Create sheets in Workbook, include 2 sheets by Creation Date Range. The
     * sheets are the original month Sheet("Detail Report") and monthly
     * Dell_Review sheet("Review Detail Report").
     */
    private void createSheets(Workbook p_workbook,
            HashMap<String, HashMap<String, ProjectWorkflowData>> p_projectMap,
            boolean p_includeExReview) throws Exception
    {
        if (p_projectMap.size() > 0)
        {
            // This sheets array has two elements.
            // sheets[MONTH_SHEET], the original month Sheet("Detail Report").
            // sheets[MONTH_REVIEW_SHEET], new monthly Dell_Review
            // sheet("Review Detail Report").
            Sheet[] sheets = new Sheet[2];
            IntHolder[] rows = new IntHolder[2];

            String sheetTitle = m_bundle.getString("lb_detail_report");
            sheets[MONTH_SHEET] = p_workbook.createSheet(sheetTitle);
            if (m_data.isTradosStyle())
            {
            	addTitle(p_workbook, sheets[MONTH_SHEET]);
                addHeaderTradosStyle(p_workbook, sheets[MONTH_SHEET], MONTH_SHEET);
            }
            else
            {
            	addTitle(p_workbook, sheets[MONTH_SHEET]);
                addHeader(p_workbook, sheets[MONTH_SHEET], MONTH_SHEET);
            }

            rows[MONTH_SHEET] = new IntHolder(4);

            if (p_includeExReview)
            {
                String sheetReviewTitle = m_bundle
                        .getString("lb_review_detail_report");
                sheets[MONTH_REVIEW_SHEET] = p_workbook.createSheet(
                        sheetReviewTitle);
                if (m_data.isTradosStyle())
                {
                	addTitle(p_workbook, sheets[MONTH_REVIEW_SHEET]);
                    addHeaderTradosStyle(p_workbook, sheets[MONTH_REVIEW_SHEET],
                            MONTH_REVIEW_SHEET);
                }
                else
                {
                	addTitle(p_workbook, sheets[MONTH_REVIEW_SHEET]);
                    addHeader(p_workbook, sheets[MONTH_REVIEW_SHEET], MONTH_REVIEW_SHEET);
                }

                rows[MONTH_REVIEW_SHEET] = new IntHolder(4);
            }

            if (m_data.isTradosStyle())
            {
                writeProjectDataTradosStyle(p_projectMap,p_workbook,  sheets,
                        p_includeExReview, rows);
            }
            else
            {
                writeProjectData(p_projectMap,p_workbook,  sheets, p_includeExReview, rows);
            }
        }
    }
    
    private void addTitle(Workbook p_workbook,
    		Sheet p_sheet) throws Exception
	{
    	String EMEA = CompanyWrapper.getCurrentCompanyName();
        Font titleFont = p_workbook.createFont();
        titleFont.setUnderline(Font.U_NONE);
        titleFont.setFontName("Arial");
        titleFont.setFontHeightInPoints((short) 14);
        titleFont.setBoldweight(Font.BOLDWEIGHT_BOLD);
        CellStyle titleStyle = p_workbook.createCellStyle();
        titleStyle.setWrapText(false);
        titleStyle.setFont(titleFont);
        
        Row firRow = getRow(p_sheet, 0);
        Cell titleCell = getCell(firRow, 0);
        titleCell.setCellValue(EMEA + " "
                + m_bundle.getString("lb_online"));
        titleCell.setCellStyle(titleStyle);
        p_sheet.setColumnWidth(0, 20 * 256);
	}

    private void addHeader(Workbook p_workbook, Sheet p_sheet, 
    		final int p_sheetCategory) throws Exception
    {
    	String EMEA = CompanyWrapper.getCurrentCompanyName();
        int col = -1;
        Row thirRow = getRow(p_sheet, 2);
        Row fourRow = getRow(p_sheet, 3);
        // Company Name
        Cell cell_A = getCell(thirRow, ++col);
        cell_A.setCellValue(m_bundle.getString("lb_company_name"));
        cell_A.setCellStyle(getHeaderStyle(p_workbook));
        p_sheet.addMergedRegion(new CellRangeAddress(2, 3, col, col));
        setRegionStyle(p_sheet, new CellRangeAddress(2, 3, col, col), 
        		getHeaderStyle(p_workbook));
        // Project Description
        Cell cell_B = getCell(thirRow, ++col);
        cell_B.setCellValue(m_bundle.getString("lb_project"));
        cell_B.setCellStyle(getHeaderStyle(p_workbook));
        p_sheet.addMergedRegion(new CellRangeAddress(2, 3, col, col));
        setRegionStyle(p_sheet, new CellRangeAddress(2, 3, col, col), 
        		getHeaderStyle(p_workbook));

        // For "Add Job Id into online job report" issue
        if (isJobIdVisible)
        {
        	Cell cell_C = getCell(thirRow, ++col);
            cell_C.setCellValue(m_bundle.getString("lb_job_id"));
            cell_C.setCellStyle(getHeaderStyle(p_workbook));
            p_sheet.addMergedRegion(new CellRangeAddress(2, 3, col, col));
            setRegionStyle(p_sheet, new CellRangeAddress(2, 3, col, col),
            		getHeaderStyle(p_workbook));
        }

        Cell cell_CorD = getCell(thirRow, ++col);
        cell_CorD.setCellValue(m_bundle.getString("lb_job_name"));
        cell_CorD.setCellStyle(getHeaderStyle(p_workbook));
        p_sheet.addMergedRegion(new CellRangeAddress(2, 3, col, col));
        setRegionStyle(p_sheet, new CellRangeAddress(2, 3, col, col), 
        		getHeaderStyle(p_workbook));
        
        Cell cell_DorE = getCell(thirRow, ++col);
        cell_DorE.setCellValue(m_bundle.getString("lb_source_file_format"));
        cell_DorE.setCellStyle(getHeaderStyle(p_workbook));
        p_sheet.addMergedRegion(new CellRangeAddress(2, 3, col, col));
        setRegionStyle(p_sheet, new CellRangeAddress(2, 3, col, col),
        		getHeaderStyle(p_workbook));
        
        Cell cell_EorF = getCell(thirRow, ++col);
        cell_EorF.setCellValue(m_bundle.getString("lb_loc_profile"));
        cell_EorF.setCellStyle(getHeaderStyle(p_workbook));
        p_sheet.addMergedRegion(new CellRangeAddress(2, 3, col, col));
        setRegionStyle(p_sheet, new CellRangeAddress(2, 3, col, col),
        		getHeaderStyle(p_workbook));

        Cell cell_ForG = getCell(thirRow, ++col);
        cell_ForG.setCellValue(m_bundle.getString("lb_file_profiles"));
        cell_ForG.setCellStyle(getHeaderStyle(p_workbook));
        p_sheet.addMergedRegion(new CellRangeAddress(2, 3, col, col));
        setRegionStyle(p_sheet, new CellRangeAddress(2, 3, col, col), 
        		getHeaderStyle(p_workbook));

        Cell cell_GorH = getCell(thirRow, ++col);
        cell_GorH.setCellValue(m_bundle.getString("lb_creation_date"));
        cell_GorH.setCellStyle(getHeaderStyle(p_workbook));
        p_sheet.addMergedRegion(new CellRangeAddress(2, 3, col, col));
        setRegionStyle(p_sheet, new CellRangeAddress(2, 3, col, col), 
        		getHeaderStyle(p_workbook));

        Cell cell_HorI = getCell(thirRow, ++col);
        cell_HorI.setCellValue(m_bundle.getString("lb_creation_time"));
        cell_HorI.setCellStyle(getHeaderStyle(p_workbook));
        p_sheet.addMergedRegion(new CellRangeAddress(2, 3, col, col));
        setRegionStyle(p_sheet, new CellRangeAddress(2, 3, col, col),
        		getHeaderStyle(p_workbook));
        
        Cell cell_IorJ = getCell(thirRow, ++col);
        cell_IorJ.setCellValue(m_bundle.getString("lb_export_date"));
        cell_IorJ.setCellStyle(getHeaderStyle(p_workbook));
        p_sheet.addMergedRegion(new CellRangeAddress(2, 3, col, col));
        setRegionStyle(p_sheet, new CellRangeAddress(2, 3, col, col), 
        		getHeaderStyle(p_workbook));
        
        Cell cell_JorK = getCell(thirRow, ++col);
        cell_JorK.setCellValue(m_bundle.getString("lb_export_time"));
        cell_JorK.setCellStyle(getHeaderStyle(p_workbook));
        p_sheet.addMergedRegion(new CellRangeAddress(2, 3, col, col));
        setRegionStyle(p_sheet, new CellRangeAddress(2, 3, col, col),
        		getHeaderStyle(p_workbook));
        
        Cell cell_KorL = getCell(thirRow, ++col);
        cell_KorL.setCellValue(m_bundle.getString("lb_status"));
        cell_KorL.setCellStyle(getHeaderStyle(p_workbook));
        p_sheet.addMergedRegion(new CellRangeAddress(2, 3, col, col));
        setRegionStyle(p_sheet, new CellRangeAddress(2, 3, col, col), 
        		getHeaderStyle(p_workbook));
        
        Cell cell_LorM = getCell(thirRow, ++col);
        cell_LorM.setCellValue(m_bundle.getString("lb_lang"));
        cell_LorM.setCellStyle(getHeaderStyle(p_workbook));
        p_sheet.addMergedRegion(new CellRangeAddress(2, 3, col, col));
        setRegionStyle(p_sheet, new CellRangeAddress(2, 3, col, col), 
        		getHeaderStyle(p_workbook));
        
        Cell cell_MorN_Header = getCell(thirRow, ++col);
        cell_MorN_Header.setCellValue(m_bundle.getString("lb_word_counts"));
        cell_MorN_Header.setCellStyle(getHeaderStyle(p_workbook));

        if (m_data.useInContext)
        {
        	p_sheet.addMergedRegion(new CellRangeAddress(2, 2, col, col + 5));
            setRegionStyle(p_sheet, new CellRangeAddress(2, 2, col, col + 5), 
            		getHeaderStyle(p_workbook));
        }
        else
        {
        	p_sheet.addMergedRegion(new CellRangeAddress(2, 2, col, col + 4));
            setRegionStyle(p_sheet, new CellRangeAddress(2, 2, col, col + 4), 
            		getHeaderStyle(p_workbook));
        }

        Cell cell_MorN = getCell(fourRow, col);
        cell_MorN.setCellValue(m_bundle
        		.getString("jobinfo.tmmatches.wordcounts.internalreps"));
        cell_MorN.setCellStyle(getHeaderStyle(p_workbook));
        
        col++;
        Cell cell_NorO = getCell(fourRow, col);
        cell_NorO.setCellValue(m_bundle.getString("lb_100_exact_matches"));
        cell_NorO.setCellStyle(getHeaderStyle(p_workbook));

        if (m_data.useInContext)
        {
            col++;
            Cell cell_InContext = getCell(fourRow, col);
            cell_InContext.setCellValue(m_bundle
                   .getString("lb_in_context_tm"));
            cell_InContext.setCellStyle(getHeaderStyle(p_workbook));
        }

        col++;
        Cell cell_FuzzyMatches = getCell(fourRow, col);
        cell_FuzzyMatches.setCellValue(m_bundle
               .getString("jobinfo.tmmatches.wordcounts.fuzzymatches"));
        cell_FuzzyMatches.setCellStyle(getHeaderStyle(p_workbook));

        col++;
        Cell cell_NewWords = getCell(fourRow, col);
        cell_NewWords.setCellValue(m_bundle
               .getString("jobinfo.tmmatches.wordcounts.newwords"));
        cell_NewWords.setCellStyle(getHeaderStyle(p_workbook));

        col++;
        Cell cell_Total = getCell(fourRow, col);
        cell_Total.setCellValue(m_bundle
               .getString("lb_total_source_word_count"));
        cell_Total.setCellStyle(getHeaderStyle(p_workbook));

        col++;
        Cell cell_Invoice = getCell(thirRow, col);
        cell_Invoice.setCellValue(m_bundle
               .getString("jobinfo.tmmatches.invoice"));
        cell_Invoice.setCellStyle(getHeaderStyle(p_workbook));

        if (p_sheetCategory == MONTH_SHEET)
        {
            if (m_data.useInContext)
            {
            	p_sheet.addMergedRegion(new CellRangeAddress(2, 2, col, col + 7));
                setRegionStyle(p_sheet, new CellRangeAddress(2, 2, col, col + 7),
                		getHeaderStyle(p_workbook));
            }
            else
            {
            	p_sheet.addMergedRegion(new CellRangeAddress(2, 2, col, col + 6));
                setRegionStyle(p_sheet, new CellRangeAddress(2, 2, col, col + 6),
                		getHeaderStyle(p_workbook));
            }

            Cell cell_InternalReps = getCell(fourRow, col);
            cell_InternalReps.setCellValue(m_bundle
                   .getString("jobinfo.tmmatches.invoice.internalreps"));
            cell_InternalReps.setCellStyle(getHeaderStyle(p_workbook));
            col++;
            
            Cell cell_ExactMatches = getCell(fourRow, col);
            cell_ExactMatches.setCellValue(m_bundle
                   .getString("lb_100_exact_matches"));
            cell_ExactMatches.setCellStyle(getHeaderStyle(p_workbook));
            col++;
            if (m_data.useInContext)
            {
            	Cell cell_InContext = getCell(fourRow, col);
                cell_InContext.setCellValue(m_bundle
                       .getString("lb_in_context_tm"));
                cell_InContext.setCellStyle(getHeaderStyle(p_workbook));
                col++;
            }

            Cell cell_FM_Invoice = getCell(fourRow, col);
            cell_FM_Invoice.setCellValue(m_bundle
                   .getString("jobinfo.tmmatches.wordcounts.fuzzymatches"));
            cell_FM_Invoice.setCellStyle(getHeaderStyle(p_workbook));
            col++;
            
            Cell cell_NW_Invoice = getCell(fourRow, col);
            cell_NW_Invoice.setCellValue(m_bundle
                   .getString("jobinfo.tmmatches.wordcounts.newwords"));
            cell_NW_Invoice.setCellStyle(getHeaderStyle(p_workbook));
            col++;
            
            Cell cell_Total_Invoice = getCell(fourRow, col);
            cell_Total_Invoice.setCellValue(m_bundle
                   .getString("jobinfo.tmmatches.wordcounts.total"));
            cell_Total_Invoice.setCellStyle(getHeaderStyle(p_workbook));
            col++;

            Cell cell_AdditionalCharges = getCell(fourRow, col);
            cell_AdditionalCharges.setCellValue(m_bundle
                   .getString("jobinfo.tmmatches.invoice.additionalCharges"));
            cell_AdditionalCharges.setCellStyle(getHeaderStyle(p_workbook));
            col++;

            Cell cell_JobTotal = getCell(fourRow, col);
            cell_JobTotal.setCellValue(m_bundle
                   .getString("jobinfo.tmmatches.invoice.jobtotal"));
            cell_JobTotal.setCellStyle(getHeaderStyle(p_workbook));
            col++;

            Cell cell_Tracking = getCell(thirRow, col);
            cell_Tracking.setCellValue(m_bundle.getString("lb_tracking")
                    + " " + EMEA + " " + m_bundle.getString("lb_use") + ")");
            cell_Tracking.setCellStyle(getHeaderStyle(p_workbook));
            
            p_sheet.addMergedRegion(new CellRangeAddress(2, 3, col, col));
            setRegionStyle(p_sheet, new CellRangeAddress(2, 3, col, col),
            		getHeaderStyle(p_workbook));

        }
        else if (p_sheetCategory == MONTH_REVIEW_SHEET)
        {
            if (m_data.useInContext)
            {
            	p_sheet.addMergedRegion(new CellRangeAddress(2, 2, col, col + 7));
                setRegionStyle(p_sheet, new CellRangeAddress(2, 2, col, col + 7),
                		getHeaderStyle(p_workbook));
            }
            else
            {
            	p_sheet.addMergedRegion(new CellRangeAddress(2, 2, col, col + 6));
                setRegionStyle(p_sheet, new CellRangeAddress(2, 2, col, col + 6),
                		getHeaderStyle(p_workbook));
            }

            Cell cell_InternalReps = getCell(fourRow, col);
            cell_InternalReps.setCellValue(m_bundle
                   .getString("jobinfo.tmmatches.wordcounts.internalreps"));
            cell_InternalReps.setCellStyle(getHeaderStyle(p_workbook));
            col++;
            
            Cell cell_ExactMatches = getCell(fourRow, col);
            cell_ExactMatches.setCellValue(m_bundle
                   .getString("lb_100_exact_matches"));
            cell_ExactMatches.setCellStyle(getHeaderStyle(p_workbook));
            col++;
            if (m_data.useInContext)
            {
            	Cell cell_InContext = getCell(fourRow, col);
            	cell_InContext.setCellValue(m_bundle
                       .getString("lb_in_context_tm"));
            	cell_InContext.setCellStyle(getHeaderStyle(p_workbook));
                col++;
            }

            Cell cell_FM_Invoice = getCell(fourRow, col);
            cell_FM_Invoice.setCellValue(m_bundle
                   .getString("jobinfo.tmmatches.wordcounts.fuzzymatches"));
            cell_FM_Invoice.setCellStyle(getHeaderStyle(p_workbook));
            col++;
            
            Cell cell_NW_Invoice = getCell(fourRow, col);
            cell_NW_Invoice.setCellValue(m_bundle
                   .getString("jobinfo.tmmatches.wordcounts.newwords"));
            cell_NW_Invoice.setCellStyle(getHeaderStyle(p_workbook));
            col++;
            
            Cell cell_Total_Invoice = getCell(fourRow, col);
            cell_Total_Invoice.setCellValue(m_bundle
                   .getString("lb_translation_total"));
            cell_Total_Invoice.setCellStyle(getHeaderStyle(p_workbook));
            col++;
            
            Cell cell_Review = getCell(fourRow, col);
            cell_Review.setCellValue(m_bundle
                   .getString("lb_review"));
            cell_Review.setCellStyle(getHeaderStyle(p_workbook));
            col++;
            
            Cell cell_JobTotal = getCell(fourRow, col);
            cell_JobTotal.setCellValue(m_bundle
                   .getString("jobinfo.tmmatches.invoice.jobtotal"));
            cell_JobTotal.setCellStyle(getHeaderStyle(p_workbook));
            col++;
            
            Cell cell_Tracking = getCell(thirRow, col);
            cell_Tracking.setCellValue(m_bundle.getString("lb_tracking")
                    + " " + EMEA + " " + m_bundle.getString("lb_use") + ")");
            cell_Tracking.setCellStyle(getHeaderStyle(p_workbook));
            
            p_sheet.addMergedRegion(new CellRangeAddress(2, 3, col, col));
            setRegionStyle(p_sheet, new CellRangeAddress(2, 3, col, col),
            		getHeaderStyle(p_workbook));
        }
        else
        {
            // Should never go here.
        }
    }

    /**
     * For trados style
     * 
     * @param p_sheet
     * @param p_sheetCategory
     * @param p_data
     * @param bundle
     * @throws Exception
     */
    private void addHeaderTradosStyle(Workbook p_workbook, Sheet p_sheet,
            final int p_sheetCategory)
            throws Exception
    {
        String EMEA = CompanyWrapper.getCurrentCompanyName();
        
        Row secRow = getRow(p_sheet, 1);
        Cell cell_Ldfl = getCell(secRow, 0);
        cell_Ldfl.setCellValue(m_bundle.getString("lb_desp_file_list"));
        cell_Ldfl.setCellStyle(getContentStyle(p_workbook));
        
        int col = -1;
        Row thirRow = getRow(p_sheet, 2);
        Row fourRow = getRow(p_sheet, 3);
        // Company Name
        Cell cell_A = getCell(thirRow, ++col);
        cell_A.setCellValue(m_bundle.getString("lb_company_name"));
        cell_A.setCellStyle(getHeaderStyle(p_workbook));
        p_sheet.addMergedRegion(new CellRangeAddress(2, 3, col, col));
        setRegionStyle(p_sheet, new CellRangeAddress(2, 3, col, col), 
        		getHeaderStyle(p_workbook));

        // Project Description
        Cell cell_B = getCell(thirRow, ++col);
        cell_B.setCellValue(m_bundle.getString("lb_project"));
        cell_B.setCellStyle(getHeaderStyle(p_workbook));
        p_sheet.addMergedRegion(new CellRangeAddress(2, 3, col, col));
        setRegionStyle(p_sheet, new CellRangeAddress(2, 3, col, col),
        		getHeaderStyle(p_workbook));

        if (isJobIdVisible)
        {
        	Cell cell_C = getCell(thirRow, ++col);
            cell_C.setCellValue(m_bundle.getString("lb_job_id"));
            cell_C.setCellStyle(getHeaderStyle(p_workbook));
            p_sheet.addMergedRegion(new CellRangeAddress(2, 3, col, col));
            setRegionStyle(p_sheet, new CellRangeAddress(2, 3, col, col),
            		getHeaderStyle(p_workbook));
        }
        
        Cell cell_CorD = getCell(thirRow, ++col);
        cell_CorD.setCellValue(m_bundle.getString("lb_job_name"));
        cell_CorD.setCellStyle(getHeaderStyle(p_workbook));
        p_sheet.addMergedRegion(new CellRangeAddress(2, 3, col, col));
        setRegionStyle(p_sheet, new CellRangeAddress(2, 3, col, col), 
        		getHeaderStyle(p_workbook));

        Cell cell_DorE = getCell(thirRow, ++col);
        cell_DorE.setCellValue(m_bundle.getString("lb_source_file_format"));
        cell_DorE.setCellStyle(getHeaderStyle(p_workbook));
        p_sheet.addMergedRegion(new CellRangeAddress(2, 3, col, col));
        setRegionStyle(p_sheet, new CellRangeAddress(2, 3, col, col), 
        		getHeaderStyle(p_workbook));

        Cell cell_EorF = getCell(thirRow, ++col);
        cell_EorF.setCellValue(m_bundle.getString("lb_loc_profile"));
        cell_EorF.setCellStyle(getHeaderStyle(p_workbook));
        p_sheet.addMergedRegion(new CellRangeAddress(2, 3, col, col));
        setRegionStyle(p_sheet, new CellRangeAddress(2, 3, col, col), 
        		getHeaderStyle(p_workbook));
        
        Cell cell_ForG = getCell(thirRow, ++col);
        cell_ForG.setCellValue(m_bundle.getString("lb_file_profiles"));
        cell_ForG.setCellStyle(getHeaderStyle(p_workbook));
        p_sheet.addMergedRegion(new CellRangeAddress(2, 3, col, col));
        setRegionStyle(p_sheet, new CellRangeAddress(2, 3, col, col), 
        		getHeaderStyle(p_workbook));

        Cell cell_GorH = getCell(thirRow, ++col);
        cell_GorH.setCellValue(m_bundle.getString("lb_creation_date"));
        cell_GorH.setCellStyle(getHeaderStyle(p_workbook));
        p_sheet.addMergedRegion(new CellRangeAddress(2, 3, col, col));
        setRegionStyle(p_sheet, new CellRangeAddress(2, 3, col, col), 
        		getHeaderStyle(p_workbook));

        Cell cell_HorI = getCell(thirRow, ++col);
        cell_HorI.setCellValue(m_bundle.getString("lb_creation_time"));
        cell_HorI.setCellStyle(getHeaderStyle(p_workbook));
        p_sheet.addMergedRegion(new CellRangeAddress(2, 3, col, col));
        setRegionStyle(p_sheet, new CellRangeAddress(2, 3, col, col),
        		getHeaderStyle(p_workbook));
        
        Cell cell_IorJ = getCell(thirRow, ++col);
        cell_IorJ.setCellValue(m_bundle.getString("lb_export_date"));
        cell_IorJ.setCellStyle(getHeaderStyle(p_workbook));
        p_sheet.addMergedRegion(new CellRangeAddress(2, 3, col, col));
        setRegionStyle(p_sheet, new CellRangeAddress(2, 3, col, col), 
        		getHeaderStyle(p_workbook));
        
        Cell cell_JorK = getCell(thirRow, ++col);
        cell_JorK.setCellValue(m_bundle.getString("lb_export_time"));
        cell_JorK.setCellStyle(getHeaderStyle(p_workbook));
        p_sheet.addMergedRegion(new CellRangeAddress(2, 3, col, col));
        setRegionStyle(p_sheet, new CellRangeAddress(2, 3, col, col),
        		getHeaderStyle(p_workbook));
        
        Cell cell_KorL = getCell(thirRow, ++col);
        cell_KorL.setCellValue(m_bundle.getString("lb_status"));
        cell_KorL.setCellStyle(getHeaderStyle(p_workbook));
        p_sheet.addMergedRegion(new CellRangeAddress(2, 3, col, col));
        setRegionStyle(p_sheet, new CellRangeAddress(2, 3, col, col),
        		getHeaderStyle(p_workbook));
        
        Cell cell_LorM = getCell(thirRow, ++col);
        cell_LorM.setCellValue(m_bundle.getString("lb_lang"));
        cell_LorM.setCellStyle(getHeaderStyle(p_workbook));
        p_sheet.addMergedRegion(new CellRangeAddress(2, 3, col, col));
        setRegionStyle(p_sheet, new CellRangeAddress(2, 3, col, col),
        		getHeaderStyle(p_workbook));
        
        Cell cell_MorN_Header = getCell(thirRow, ++col);
        cell_MorN_Header.setCellValue(m_bundle.getString("lb_word_counts"));
        cell_MorN_Header.setCellStyle(getHeaderStyle(p_workbook));

        if (m_data.useInContext)
        {
        	p_sheet.addMergedRegion(new CellRangeAddress(2, 2, col, col + 7));
            setRegionStyle(p_sheet, new CellRangeAddress(2, 2, col, col + 7), 
            		getHeaderStyle(p_workbook));
        }
        else
        {
        	p_sheet.addMergedRegion(new CellRangeAddress(2, 2, col, col + 6));
            setRegionStyle(p_sheet, new CellRangeAddress(2, 2, col, col + 6), 
            		getHeaderStyle(p_workbook));
        }

        Cell cell_MorN = getCell(fourRow, col);
        cell_MorN.setCellValue(m_bundle.getString("jobinfo.tradosmatches.invoice.per100matches"));
        cell_MorN.setCellStyle(getHeaderStyle(p_workbook));

        col++;
        Cell cell_NorO = getCell(fourRow, col);
        cell_NorO.setCellValue(m_bundle.getString("lb_95_99"));
        cell_NorO.setCellStyle(getHeaderStyle(p_workbook));

        col++;
        Cell cell_OorP = getCell(fourRow, col);
        cell_OorP.setCellValue(m_bundle.getString("lb_85_94"));
        cell_OorP.setCellStyle(getHeaderStyle(p_workbook));

        col++;
        Cell cell_PorQ = getCell(fourRow, col);
        cell_PorQ.setCellValue(m_bundle.getString("lb_75_84") + "*");
        cell_PorQ.setCellStyle(getHeaderStyle(p_workbook));

        col++;
        Cell cell_QorR = getCell(fourRow, col);
        cell_QorR.setCellValue(m_bundle.getString("lb_no_match"));
        cell_QorR.setCellStyle(getHeaderStyle(p_workbook));
        
        col++;
        Cell cell_RorS = getCell(fourRow, col);
        cell_RorS.setCellValue(m_bundle.getString("lb_repetition_word_cnt"));
        cell_RorS.setCellStyle(getHeaderStyle(p_workbook));

        if (m_data.useInContext)
        {
            col++;
            Cell cell_InContext = getCell(fourRow, col);
            cell_InContext.setCellValue(m_bundle.getString("lb_in_context_tm"));
            cell_InContext.setCellStyle(getHeaderStyle(p_workbook));
        }

        col++;
        Cell cell_Total = getCell(fourRow, col);
        cell_Total.setCellValue(m_bundle.getString("lb_total"));
        cell_Total.setCellStyle(getHeaderStyle(p_workbook));

        col++;
        Cell cell_Invoice = getCell(thirRow, col);
        cell_Invoice.setCellValue(m_bundle.getString("jobinfo.tmmatches.invoice"));
        cell_Invoice.setCellStyle(getHeaderStyle(p_workbook));

        if (p_sheetCategory == MONTH_SHEET)
        {
            if (m_data.useInContext)
            {
            	p_sheet.addMergedRegion(new CellRangeAddress(2, 2, col, col + 9));
                setRegionStyle(p_sheet, new CellRangeAddress(2, 2, col, col + 9),
                		getHeaderStyle(p_workbook));
            }
            else
            {
            	p_sheet.addMergedRegion(new CellRangeAddress(2, 2, col, col + 8));
                setRegionStyle(p_sheet, new CellRangeAddress(2, 2, col, col + 8),
                		getHeaderStyle(p_workbook));
            }

            Cell cell_Per100Matches = getCell(fourRow, col);
            cell_Per100Matches.setCellValue(m_bundle
            		.getString("jobinfo.tradosmatches.invoice.per100matches"));
            cell_Per100Matches.setCellStyle(getHeaderStyle(p_workbook));

            col++;
            Cell cell_95_99 = getCell(fourRow, col);
            cell_95_99.setCellValue(m_bundle.getString("lb_95_99"));
            cell_95_99.setCellStyle(getHeaderStyle(p_workbook));

            col++;
            Cell cell_85_94 = getCell(fourRow, col);
            cell_85_94.setCellValue(m_bundle.getString("lb_85_94"));
            cell_85_94.setCellStyle(getHeaderStyle(p_workbook));

            col++;
            Cell cell_75_84 = getCell(fourRow, col);
            cell_75_84.setCellValue(m_bundle.getString("lb_75_84") + "*");
            cell_75_84.setCellStyle(getHeaderStyle(p_workbook));

            col++;
            Cell cell_NoMatch = getCell(fourRow, col);
            cell_NoMatch.setCellValue(m_bundle.getString("lb_no_match"));
            cell_NoMatch.setCellStyle(getHeaderStyle(p_workbook));

            col++;
            Cell cell_WordCount = getCell(fourRow, col);
            cell_WordCount.setCellValue(m_bundle.getString("lb_repetition_word_cnt"));
            cell_WordCount.setCellStyle(getHeaderStyle(p_workbook));

            col++;
            if (m_data.useInContext)
            {
            	Cell cell_InContext = getCell(fourRow, col);
            	cell_InContext.setCellValue(m_bundle.getString("lb_in_context_tm"));
            	cell_InContext.setCellStyle(getHeaderStyle(p_workbook));
                col++;
            }

            Cell cell_Total_Invoice = getCell(fourRow, col);
            cell_Total_Invoice.setCellValue(m_bundle
                    .getString("jobinfo.tmmatches.wordcounts.total"));
            cell_Total_Invoice.setCellStyle(getHeaderStyle(p_workbook));
            col++;

            Cell cell_AdditionalCharges = getCell(fourRow, col);
            cell_AdditionalCharges.setCellValue(m_bundle
                    .getString("jobinfo.tmmatches.invoice.additionalCharges"));
            cell_AdditionalCharges.setCellStyle(getHeaderStyle(p_workbook));
            col++;

            Cell cell_JobTotal = getCell(fourRow, col);
            cell_JobTotal.setCellValue(m_bundle
                    .getString("jobinfo.tmmatches.invoice.jobtotal"));
            cell_JobTotal.setCellStyle(getHeaderStyle(p_workbook));
            col++;
            
            Cell cell_Tracking = getCell(thirRow, col);
            cell_Tracking.setCellValue(m_bundle.getString("lb_tracking")
                    + " " + EMEA + " " + m_bundle.getString("lb_use") + ")");
            cell_Tracking.setCellStyle(getHeaderStyle(p_workbook));
            
            p_sheet.addMergedRegion(new CellRangeAddress(2, 3, col, col));
            setRegionStyle(p_sheet, new CellRangeAddress(2, 3, col, col),
            		getHeaderStyle(p_workbook));

        }
        else if (p_sheetCategory == MONTH_REVIEW_SHEET)
        {
            if (m_data.useInContext)
            {
            	p_sheet.addMergedRegion(new CellRangeAddress(2, 2, col, col + 9));
                setRegionStyle(p_sheet, new CellRangeAddress(2, 2, col, col + 9),
                		getHeaderStyle(p_workbook));
            }
            else
            {
            	p_sheet.addMergedRegion(new CellRangeAddress(2, 2, col, col + 7));
                setRegionStyle(p_sheet, new CellRangeAddress(2, 2, col, col + 7),
                		getHeaderStyle(p_workbook));
            }

            Cell cell_Per100Matches = getCell(fourRow, col);
            cell_Per100Matches.setCellValue(m_bundle
                    .getString("jobinfo.tradosmatches.invoice.per100matches"));
            cell_Per100Matches.setCellStyle(getHeaderStyle(p_workbook));

            col++;
            Cell cell_95_99 = getCell(fourRow, col);
            cell_95_99.setCellValue(m_bundle.getString("lb_95_99"));
            cell_95_99.setCellStyle(getHeaderStyle(p_workbook));
            
            col++;
            Cell cell_85_94 = getCell(fourRow, col);
            cell_85_94.setCellValue(m_bundle.getString("lb_85_94"));
            cell_85_94.setCellStyle(getHeaderStyle(p_workbook));
            
            col++;
            Cell cell_75_84 = getCell(fourRow, col);
            cell_75_84.setCellValue(m_bundle.getString("lb_75_84"));
            cell_75_84.setCellStyle(getHeaderStyle(p_workbook));
            
            col++;
            Cell cell_NoMatch = getCell(fourRow, col);
            cell_NoMatch.setCellValue(m_bundle.getString("lb_no_match"));
            cell_NoMatch.setCellStyle(getHeaderStyle(p_workbook));

            col++;
            Cell cell_WoerCount = getCell(fourRow, col);
            cell_WoerCount.setCellValue(m_bundle.getString("lb_repetition_word_cnt"));
            cell_WoerCount.setCellStyle(getHeaderStyle(p_workbook));

            col++;
            if (m_data.useInContext)
            {
            	Cell cell_InContext = getCell(fourRow, col);
            	cell_InContext.setCellValue(m_bundle.getString("lb_in_context_tm"));
            	cell_InContext.setCellStyle(getHeaderStyle(p_workbook));
                col++;
            }

            Cell cell_TranTotal = getCell(fourRow, col);
            cell_TranTotal.setCellValue(m_bundle.getString("lb_translation_total"));
            cell_TranTotal.setCellStyle(getHeaderStyle(p_workbook));
            
            col++;
            Cell cell_Review = getCell(fourRow, col);
            cell_Review.setCellValue(m_bundle.getString("lb_review"));
            cell_Review.setCellStyle(getHeaderStyle(p_workbook));
            
            col++;
            Cell cell_JobTotal = getCell(fourRow, col);
            cell_JobTotal.setCellValue(m_bundle
            		.getString("jobinfo.tmmatches.invoice.jobtotal"));
            cell_JobTotal.setCellStyle(getHeaderStyle(p_workbook));

            col++;
            Cell cell_Tracking = getCell(thirRow, col);
            cell_Tracking.setCellValue(m_bundle.getString("lb_tracking")
                    + " " + EMEA + " " + m_bundle.getString("lb_use") + ")");
            cell_Tracking.setCellStyle(getHeaderStyle(p_workbook));
            p_sheet.addMergedRegion(new CellRangeAddress(2, 3, col, col));
            setRegionStyle(p_sheet, new CellRangeAddress(2, 3, col, col),
            		getHeaderStyle(p_workbook));
        }
        else
        {
            // Should never go here.
        }
    }

    /*
     * public void writeProjectData( HashMap<String, HashMap<String,
     * ProjectWorkflowData>> p_projectMap, WritableSheet p_sheet, IntHolder
     * p_row, MyData p_data, ResourceBundle bundle) throws Exception {
     * WritableSheet[] sheets = new WritableSheet[2]; sheets[MONTH_SHEET] =
     * p_sheet; sheets[MONTH_REVIEW_SHEET] = null;
     * 
     * IntHolder[] rows = new IntHolder[2]; rows[MONTH_SHEET] = p_row;
     * rows[MONTH_REVIEW_SHEET] = null;
     * 
     * writeProjectData(p_projectMap, sheets, false, rows, p_data, bundle); }
     */

    private void writeProjectData(
            HashMap<String, HashMap<String, ProjectWorkflowData>> p_projectMap,
            Workbook p_workbook, Sheet[] p_sheets, boolean p_includeExReview,
            IntHolder[] p_rows)
            throws Exception
    {
        ArrayList<String> keys = new ArrayList<String>(p_projectMap.keySet());
        SortUtil.sort(keys);
        Iterator<String> keysIter = keys.iterator();

        totalCost.clear();
        totalCostDate.clear();
        int finishedJobNum = 0;

        while (keysIter.hasNext())
        {
            // Cancel generate reports.
            if (isCancelled())
                return;
            // Sets Reports Percent.
            setPercent(++finishedJobNum);
            String key = keysIter.next();
            String jobId = getJobIdFromMapKey(key);
            boolean isWrongJob = m_data.wrongJobNames.contains(jobId);
            HashMap<String, ProjectWorkflowData> localeMap = p_projectMap
                    .get(key);
            ArrayList<String> locales = new ArrayList<String>(
                    localeMap.keySet());
            SortUtil.sort(locales);
            Iterator<String> localeIter = locales.iterator();
            BigDecimal projectTotalWordCountCost = new BigDecimal(
                    BIG_DECIMAL_ZERO_STRING);
            BigDecimal reviewTotalWordCountCost = new BigDecimal(
                    BIG_DECIMAL_ZERO_STRING);

            // Counts the review costing total and the translation costing
            // total.
            String localeName = null;
            ProjectWorkflowData data = null;

            // Marks whether needs to add a blank line.
            boolean containsDellReview = false;

            while (localeIter.hasNext())
            {
                int col = 0;
                localeName = (String) localeIter.next();
                data = (ProjectWorkflowData) localeMap.get(localeName);

                int row = p_rows[MONTH_SHEET].getValue();
                Row theRow = getRow(p_sheets[MONTH_SHEET], row);

                CellStyle temp_moneyStyle = getMoneyStyle(p_workbook);
                CellStyle temp_normalStyle = getContentStyle(p_workbook);

                if (data.wasExportFailed)
                {
                	temp_moneyStyle = getFailedMoneyStyle(p_workbook);
                	temp_normalStyle =  getRedCellStyle(p_workbook);
                }

                // Company Name
                Cell cell_A = getCell(theRow, col++);
                cell_A.setCellValue(data.companyName);
                cell_A.setCellStyle(temp_normalStyle);
                p_sheets[MONTH_SHEET].setColumnWidth(col - 1, 15 * 256);

                // Project Description
                Cell cell_B = getCell(theRow, col++);
                cell_B.setCellValue(data.projectDesc);
                cell_B.setCellStyle(temp_normalStyle);
                p_sheets[MONTH_SHEET].setColumnWidth(col - 1, 22 * 256);

                if (isWrongJob)
                {
                    // For "Add Job Id into online job report" issue
                    if (isJobIdVisible)
                    {
                    	Cell cell_C = getCell(theRow, col++);
                        cell_C.setCellValue(Long.valueOf(jobId));
                        cell_C.setCellStyle(getWrongJobStyle(p_workbook));
                    }

                    Cell cell_CorD = getCell(theRow, col++);
                    cell_CorD.setCellValue(data.jobName);
                    cell_CorD.setCellStyle(getWrongJobStyle(p_workbook));
                }
                else
                {
                    // For "Add Job Id into online job report" issue
                    if (isJobIdVisible)
                    {
                    	Cell cell_C = getCell(theRow, col++);
                    	cell_C.setCellValue(Long.valueOf(jobId));
                    	cell_C.setCellStyle(temp_normalStyle);
                    }
                    Cell cell_CorD = getCell(theRow, col++);
                    cell_CorD.setCellValue(data.jobName);
                    cell_CorD.setCellStyle(temp_normalStyle);
                }
                p_sheets[MONTH_SHEET].setColumnWidth(col - 1, 50 * 256);

                Cell cell_DorE = getCell(theRow, col++);
                cell_DorE.setCellValue(getAllSouceFileFormats(data.allFileProfiles));
                cell_DorE.setCellStyle(temp_normalStyle);
                p_sheets[MONTH_SHEET].setColumnWidth(col - 1, 20 * 256);

                Cell cell_EorF = getCell(theRow, col++);
                cell_EorF.setCellValue(data.l10nProfileName);
                cell_EorF.setCellStyle(temp_normalStyle);
                p_sheets[MONTH_SHEET].setColumnWidth(col - 1, 25 * 256);

                Cell cell_ForG = getCell(theRow, col++);
                cell_ForG.setCellValue(data.fileProfileNames);
                cell_ForG.setCellStyle(temp_normalStyle);
                p_sheets[MONTH_SHEET].setColumnWidth(col - 1, 20 * 256);

                if (data.wasExportFailed)
                {
                	Cell cell_GorH = getCell(theRow, col++);
                	cell_GorH.setCellValue(data.creationDate);
                	cell_GorH.setCellStyle(getFailedDateStyle(p_workbook));
                    p_sheets[MONTH_SHEET].setColumnWidth(col - 1, 15 * 256);
                    Cell cell_HorI = getCell(theRow, col++);
                    cell_HorI.setCellValue(data.creationDate);
                    cell_HorI.setCellStyle(getFailedTimeStyle(p_workbook));
                    p_sheets[MONTH_SHEET].setColumnWidth(col - 1, 18 * 256);
                    Cell cell_IorJ = getCell(theRow, col++);
                    cell_IorJ.setCellValue("");
                    cell_IorJ.setCellStyle(getFailedDateStyle(p_workbook));
                    p_sheets[MONTH_SHEET].setColumnWidth(col - 1, 15 * 256);
                    Cell cell_JorK = getCell(theRow, col++);
                    cell_JorK.setCellValue("");
                    cell_JorK.setCellStyle(getFailedTimeStyle(p_workbook));
                }
                else
                {
                	Cell cell_GorH = getCell(theRow, col++);
                	cell_GorH.setCellValue(getDateFormat().format(data.creationDate));
                	cell_GorH.setCellStyle(getDateStyle(p_workbook));
                    p_sheets[MONTH_SHEET].setColumnWidth(col - 1, 15 * 256);
                    Cell cell_HorI = getCell(theRow, col++);
                    cell_HorI.setCellValue(getTimeFormat().format(data.creationDate));
                    cell_HorI.setCellStyle(getContentStyle(p_workbook));
                    p_sheets[MONTH_SHEET].setColumnWidth(col - 1, 18 * 256);
                    Cell cell_IorJ = getCell(theRow, col++);
                    if (data.wasExported)
                    {
                        cell_IorJ.setCellValue(getDateFormat().format(data.exportDate));
                    }else {
                    	cell_IorJ.setCellValue("");
					}
                    cell_IorJ.setCellStyle(getDateStyle(p_workbook));
                    p_sheets[MONTH_SHEET].setColumnWidth(col - 1, 15 * 256);
                    String labelExportTime = "";
                    if (data.wasExported)
                    {
                        labelExportTime = getExportDateStr(getTimeFormat(),
                                data.exportDate);
                    }
                    Cell cell_JorK = getCell(theRow, col++);
                    cell_JorK.setCellValue(labelExportTime);
                    cell_JorK.setCellStyle(getContentStyle(p_workbook));

                }
                p_sheets[MONTH_SHEET].setColumnWidth(col - 1, 18 * 256);

                // Status
                Cell cell_KorL = getCell(theRow, col++);
                cell_KorL.setCellValue(data.status);
                cell_KorL.setCellStyle(temp_normalStyle);
                p_sheets[MONTH_SHEET].setColumnWidth(col - 1, 18 * 256);
                // Language
                Cell cell_LorM = getCell(theRow, col++);
                cell_LorM.setCellValue(data.targetLang);
                cell_LorM.setCellStyle(temp_normalStyle);
                int numwidth = 10;
                // Summary Start Collumn
                m_data.initSumStartCol(col);
                Cell cell_MorN = getCell(theRow, col++);
                cell_MorN.setCellValue(data.repetitionWordCount);
                cell_MorN.setCellStyle(temp_normalStyle);
                p_sheets[MONTH_SHEET].setColumnWidth(col - 1, numwidth * 256);

                Cell cell_NorO = getCell(theRow, col++);
                cell_NorO.setCellValue(data.segmentTmWordCount);
                cell_NorO.setCellStyle(temp_normalStyle);
                p_sheets[MONTH_SHEET].setColumnWidth(col - 1, numwidth * 256);

                if (m_data.useInContext)
                {
                	Cell cell_InContext = getCell(theRow, col++);
                	cell_InContext.setCellValue(data.inContextMatchWordCount);
                	cell_InContext.setCellStyle(temp_normalStyle);
                    p_sheets[MONTH_SHEET].setColumnWidth(col - 1, numwidth * 256);
                }

                Cell cell_FuzzyMatch = getCell(theRow, col++);
                cell_FuzzyMatch.setCellValue(data.fuzzyMatchWordCount);
                cell_FuzzyMatch.setCellStyle(temp_normalStyle);
                p_sheets[MONTH_SHEET].setColumnWidth(col - 1, numwidth * 256);

                Cell cell_NoMatch = getCell(theRow, col++);
                cell_NoMatch.setCellValue(data.noMatchWordCount);
                cell_NoMatch.setCellStyle(temp_normalStyle);
                p_sheets[MONTH_SHEET].setColumnWidth(col - 1, numwidth * 256);
                
                Cell cell_TotalWordCount = getCell(theRow, col++);
                cell_TotalWordCount.setCellValue(data.totalWordCount);
                cell_TotalWordCount.setCellStyle(temp_normalStyle);
                p_sheets[MONTH_SHEET].setColumnWidth(col - 1, numwidth * 256);

                int moneywidth = 12;
                Cell cell_RepetitionWordCountCost = getCell(theRow, col++);
                cell_RepetitionWordCountCost.setCellValue(asDouble(data.repetitionWordCountCost));
                cell_RepetitionWordCountCost.setCellStyle(temp_moneyStyle);
                p_sheets[MONTH_SHEET].setColumnWidth(col - 1, moneywidth * 256);
                
                Cell cell_SegmentTmWordCountCost = getCell(theRow, col++);
                cell_SegmentTmWordCountCost.setCellValue(asDouble(data.segmentTmWordCountCost));
                cell_SegmentTmWordCountCost.setCellStyle(temp_moneyStyle);
                p_sheets[MONTH_SHEET].setColumnWidth(col - 1, moneywidth * 256);
                if (m_data.useInContext)
                {
                	Cell cell_InContext = getCell(theRow, col++);
                    cell_InContext.setCellValue(asDouble(data.inContextMatchWordCountCost));
                    cell_InContext.setCellStyle(temp_moneyStyle);
                    p_sheets[MONTH_SHEET].setColumnWidth(col - 1, moneywidth * 256);
                }
                
                Cell cell_FuzzyMatchCost = getCell(theRow, col++);
                cell_FuzzyMatchCost.setCellValue(asDouble(data.fuzzyMatchWordCountCost));
                cell_FuzzyMatchCost.setCellStyle(temp_moneyStyle);
                p_sheets[MONTH_SHEET].setColumnWidth(col - 1, moneywidth * 256);

                Cell cell_NoMatchCost = getCell(theRow, col++);
                cell_NoMatchCost.setCellValue(asDouble(data.noMatchWordCountCost));
                cell_NoMatchCost.setCellStyle(temp_moneyStyle);
                p_sheets[MONTH_SHEET].setColumnWidth(col - 1, moneywidth * 256);
                if (totalCol == null)
                {
                    totalCol = ReportUtil.toChar(col);
                }
                Cell cell_TotalCost = getCell(theRow, col++);
                cell_TotalCost.setCellValue(asDouble(data.totalWordCountCost));
                cell_TotalCost.setCellStyle(temp_moneyStyle);

                // Show total additional charges
                // p_sheets[MONTH_SHEET].setColumnView(col - 1, moneywidth);
                // p_sheets[MONTH_SHEET].addCell(new Number(col++, row,
                // asDouble(data.totalAdditionalCost), temp_moneyFormat));

                // p_sheets[MONTH_SHEET].setColumnView(col - 1, moneywidth);
                projectTotalWordCountCost = projectTotalWordCountCost
                        .add(data.totalWordCountCost);

                List<Integer> indexs = totalCost.get(data.targetLang);
                if (indexs == null)
                {
                    indexs = new ArrayList<Integer>();
                    totalCost.put(data.targetLang, indexs);
                }
                indexs.add(row + 1);

                Double value = totalCostDate.get(data.targetLang);
                if (value == null)
                {
                    value = 0.0;
                }
                value += asDouble(data.totalWordCountCost);
                totalCostDate.put(data.targetLang, value);

                // add a "project total" summary cost over the locales
                // map(1,moneyFormat)
                if (localeIter.hasNext() == false)
                {
                	Cell cell_TotalAdditionalCost = getCell(theRow, col++);
                	cell_TotalAdditionalCost.setCellValue(asDouble(data.totalAdditionalCost));
                	cell_TotalAdditionalCost.setCellStyle(temp_moneyStyle);
                    p_sheets[MONTH_SHEET].setColumnWidth(col - 1, moneywidth * 256);

                    Cell cell_ProjectTotalCost = getCell(theRow, col++);
                    cell_ProjectTotalCost.setCellValue(asDouble(projectTotalWordCountCost)
                            + asDouble(data.totalAdditionalCost));
                    cell_ProjectTotalCost.setCellStyle(temp_moneyStyle);
                }
                else
                {
                	Cell cell_TotalAdditionalCost = getCell(theRow, col++);
                	cell_TotalAdditionalCost.setCellStyle(temp_moneyStyle);
                    p_sheets[MONTH_SHEET].setColumnWidth(col - 1, moneywidth * 256);

                    Cell cell_ProjectTotalCost = getCell(theRow, col++);
                    cell_ProjectTotalCost.setCellStyle(temp_moneyStyle);
                }
                p_sheets[MONTH_SHEET].setColumnWidth(col - 1, moneywidth * 256);
                p_rows[MONTH_SHEET].inc();

                // Write data into MONTH_REVIEW_SHEET
                if (p_includeExReview && data.containsDellReview)
                {
                    containsDellReview = true;

                    row = p_rows[MONTH_REVIEW_SHEET].getValue();
                    theRow = getRow(p_sheets[MONTH_REVIEW_SHEET], row);
                    col = 0;
                    
                    // Company Name
                    Cell cell_A_Review = getCell(theRow, col++);
                    cell_A_Review.setCellValue(data.companyName);
                    cell_A_Review.setCellStyle(temp_normalStyle);
                    p_sheets[MONTH_REVIEW_SHEET].setColumnWidth(col - 1, 15 * 256);

                    // Project Description
                    Cell cell_B_Review = getCell(theRow, col++);
                    cell_B_Review.setCellValue(data.projectDesc);
                    cell_B_Review.setCellStyle(temp_normalStyle);
                    p_sheets[MONTH_REVIEW_SHEET].setColumnWidth(col - 1, 22 * 256);

                    if (isWrongJob)
                    {
                        // For "Job id not showing in external review tabs"
                        // Issue
                        if (isJobIdVisible)
                        {
                        	Cell cell_C_Review = getCell(theRow, col++);
                            cell_C_Review.setCellValue(data.jobId);
                            cell_C_Review.setCellStyle(getWrongJobStyle(p_workbook));
                        }
                        Cell cell_CorD_Review = getCell(theRow, col++);
                        cell_CorD_Review.setCellValue(data.jobName);
                        cell_CorD_Review.setCellStyle(getWrongJobStyle(p_workbook));
                    }
                    else
                    {
                        // For "Job id not showing in external review tabs"
                        // Issue
                        if (isJobIdVisible)
                        {
                        	Cell cell_C_Review = getCell(theRow, col++);
                        	cell_C_Review.setCellValue(data.jobId);
                        	cell_C_Review.setCellStyle(temp_normalStyle);
                        }
                        Cell cell_CorD_Review = getCell(theRow, col++);
                        cell_CorD_Review.setCellValue(data.jobName);
                        cell_CorD_Review.setCellStyle(temp_normalStyle);
                    }
                    p_sheets[MONTH_REVIEW_SHEET].setColumnWidth(col - 1, 50 * 256);
                    Cell cell_DorE_Review = getCell(theRow, col++);
                    cell_DorE_Review.setCellValue(data.projectDesc);
                    cell_DorE_Review.setCellStyle(temp_normalStyle);
                    p_sheets[MONTH_REVIEW_SHEET].setColumnWidth(col - 1, 22 * 256);

                    if (data.wasExportFailed)
                    {
                        /*
                         * p_sheets[MONTH_SHEET].addCell(new DateTime(col++,
                         * row, data.creationDate, failed_dateFormat));
                         */
                    	Cell cell_EorF_Review = getCell(theRow, col++);
                    	cell_EorF_Review.setCellValue(data.creationDate);
                    	cell_EorF_Review.setCellStyle(getFailedDateStyle(p_workbook));
                        p_sheets[MONTH_REVIEW_SHEET].setColumnWidth(col - 1, 15 * 256);
                        Cell cell_ForG_Review = getCell(theRow, col++);
                        cell_ForG_Review.setCellValue(data.creationDate);
                        cell_ForG_Review.setCellStyle(getFailedTimeStyle(p_workbook));
                        p_sheets[MONTH_REVIEW_SHEET].setColumnWidth(col - 1, 18 * 256);
                        Cell cell_GorH_Review = getCell(theRow, col++);
                        cell_GorH_Review.setCellValue("");
                        cell_GorH_Review.setCellStyle(getFailedDateStyle(p_workbook));
                        p_sheets[MONTH_REVIEW_SHEET].setColumnWidth(col - 1, 15 * 256);
                        Cell cell_HorI_Review = getCell(theRow, col++);
                        cell_HorI_Review.setCellValue("");
                        cell_HorI_Review.setCellStyle(getFailedTimeStyle(p_workbook));
                    }
                    else
                    {
                        /*
                         * p_sheets[MONTH_SHEET].addCell(new Label(col++, row,
                         * dateFormat.format(data.creationDate)));
                         */
                    	Cell cell_EorF_Review = getCell(theRow, col++);
                    	cell_EorF_Review.setCellValue(getDateFormat().format(data.creationDate));
                    	cell_EorF_Review.setCellStyle(getContentStyle(p_workbook));
                        p_sheets[MONTH_REVIEW_SHEET].setColumnWidth(col - 1, 15 * 256);
                        Cell cell_ForG_Review = getCell(theRow, col++);
                        cell_ForG_Review.setCellValue(getTimeFormat().format(data.creationDate));
                        cell_ForG_Review.setCellStyle(getContentStyle(p_workbook));
                        p_sheets[MONTH_REVIEW_SHEET].setColumnWidth(col - 1, 18 * 256);
                        String labelExportDate = "";
                        if (data.wasExported)
                        {
                            labelExportDate = getExportDateStr(getDateFormat(),
                                    data.exportDate);
                        }
                        Cell cell_GorH_Review = getCell(theRow, col++);
                        cell_GorH_Review.setCellValue(labelExportDate);
                        cell_GorH_Review.setCellStyle(getContentStyle(p_workbook));
                        p_sheets[MONTH_REVIEW_SHEET].setColumnWidth(col - 1, 15 * 256);
                        String labelExportTime = "";
                        if (data.wasExported)
                        {
                            labelExportTime = getExportDateStr(getTimeFormat(),
                                    data.exportDate);
                        }
                        Cell cell_HorI_Review = getCell(theRow, col++);
                        cell_HorI_Review.setCellValue(labelExportTime);
                        cell_HorI_Review.setCellStyle(getContentStyle(p_workbook));

                    }
                    p_sheets[MONTH_REVIEW_SHEET].setColumnWidth(col - 1, 18 * 256);
                    Cell cell_IorJ_Review = getCell(theRow, col++);
                    cell_IorJ_Review.setCellValue(data.targetLang);
                    cell_IorJ_Review.setCellStyle(temp_normalStyle);
                    
                    Cell cell_JorK_Review = getCell(theRow, col++);
                    cell_JorK_Review.setCellValue(data.repetitionWordCount);
                    cell_JorK_Review.setCellStyle(temp_normalStyle);

                    numwidth = 10;
                    p_sheets[MONTH_REVIEW_SHEET].setColumnWidth(col - 1,
                            numwidth * 256);
                    Cell cell_KorL_Review = getCell(theRow, col++);
                    cell_KorL_Review.setCellValue(data.segmentTmWordCount);
                    cell_KorL_Review.setCellStyle(temp_normalStyle);
                    p_sheets[MONTH_REVIEW_SHEET].setColumnWidth(col - 1,
                            numwidth * 256);

                    if (m_data.useInContext)
                    {
                    	Cell cell_InContext = getCell(theRow, col++);
                    	cell_InContext.setCellValue(data.inContextMatchWordCount);
                    	cell_InContext.setCellStyle(temp_normalStyle);
                        p_sheets[MONTH_REVIEW_SHEET].setColumnWidth(col - 1,
                                numwidth * 256);
                    }

                    Cell cell_FuzzyMatch_Review = getCell(theRow, col++);
                    cell_FuzzyMatch_Review.setCellValue(data.fuzzyMatchWordCount);
                    cell_FuzzyMatch_Review.setCellStyle(temp_normalStyle);
                    p_sheets[MONTH_REVIEW_SHEET].setColumnWidth(col - 1,
                            numwidth * 256);

                    Cell cell_NoMatch_Review = getCell(theRow, col++);
                    cell_NoMatch_Review.setCellValue(data.noMatchWordCount);
                    cell_NoMatch_Review.setCellStyle(temp_normalStyle);
                    p_sheets[MONTH_REVIEW_SHEET].setColumnWidth(col - 1,
                            numwidth * 256);
                    
                    Cell cell_Total_Review = getCell(theRow, col++);
                    cell_Total_Review.setCellValue(data.totalWordCount);
                    cell_Total_Review.setCellStyle(temp_normalStyle);
                    p_sheets[MONTH_REVIEW_SHEET].setColumnWidth(col - 1,
                            numwidth * 256);

                    moneywidth = 12;
                    Cell cell_RepetitionCost_Review = getCell(theRow, col++);
                    cell_RepetitionCost_Review
                    	.setCellValue(asDouble(data.repetitionWordCountCostForDellReview));
                    cell_RepetitionCost_Review.setCellStyle(temp_moneyStyle);
                    p_sheets[MONTH_REVIEW_SHEET].setColumnWidth(col - 1,
                            moneywidth * 256);
                    Cell cell_SegmentCost_Review = getCell(theRow, col++);
                    cell_SegmentCost_Review
                    	.setCellValue(asDouble(data.segmentTmWordCountCostForDellReview));
                    cell_SegmentCost_Review.setCellStyle(temp_moneyStyle);
                    p_sheets[MONTH_REVIEW_SHEET].setColumnWidth(col - 1,
                            moneywidth * 256);

                    if (m_data.useInContext)
                    {
                    	Cell cell_InContext = getCell(theRow, col++);
                    	cell_InContext.setCellValue(asDouble(data
                    			.inContextMatchWordCountCostForDellReview));
                    	cell_InContext.setCellStyle(temp_moneyStyle);
                        p_sheets[MONTH_REVIEW_SHEET].setColumnWidth(col - 1,
                                moneywidth * 256);
                    }

                    Cell cell_FuzzyMatchCost_Review = getCell(theRow, col++);
                    cell_FuzzyMatchCost_Review.setCellValue(asDouble(data
                    		.fuzzyMatchWordCountCostForDellReview));
                    cell_FuzzyMatchCost_Review.setCellStyle(temp_moneyStyle);
                    p_sheets[MONTH_REVIEW_SHEET].setColumnWidth(col - 1,
                            moneywidth * 256);

                    Cell cell_NoMatchCost_Review = getCell(theRow, col++);
                    cell_NoMatchCost_Review.setCellValue(asDouble(data
                    		.noMatchWordCountCostForDellReview));
                    cell_NoMatchCost_Review.setCellStyle(temp_moneyStyle);
                    p_sheets[MONTH_REVIEW_SHEET].setColumnWidth(col - 1,
                            moneywidth * 256);

                    reviewTotalWordCountCost = reviewTotalWordCountCost
                            .add(data.totalWordCountCostForDellReview);

                    // Writes the "Translation Total" column.
                    Cell cell_TotalCost_Review = getCell(theRow, col++);
                    cell_TotalCost_Review.setCellValue(asDouble(data.totalWordCountCost));
                    cell_TotalCost_Review.setCellStyle(temp_moneyStyle);
                    p_sheets[MONTH_REVIEW_SHEET].setColumnWidth(col - 1,
                            moneywidth * 256);
                    // Writes the "Review" column.
                    Cell cell_Review = getCell(theRow, col++);
                    cell_Review.setCellValue(asDouble(data.totalWordCountCostForDellReview));
                    cell_Review.setCellStyle(temp_moneyStyle);
                    p_sheets[MONTH_REVIEW_SHEET].setColumnWidth(col - 1,
                            moneywidth * 256);

                    // add "job total" cost over the locales
                    if (localeIter.hasNext() == false)
                    {
                    	Cell cell_JobTotal = getCell(theRow, col++);
                    	cell_JobTotal.setCellValue(asDouble(projectTotalWordCountCost
                                .add(reviewTotalWordCountCost)));
                    	cell_JobTotal.setCellStyle(temp_moneyStyle);
                    }
                    else
                    {
                    	Cell cell_JobTotal = getCell(theRow, col++);
                    	cell_JobTotal.setCellStyle(temp_moneyStyle);
                    }
                    p_sheets[MONTH_REVIEW_SHEET].setColumnWidth(col - 1,
                            moneywidth * 256);
                    p_rows[MONTH_REVIEW_SHEET].inc();
                }// End if (p_includeExReview && data.containsDellReview)
            }// End loop while (localeIter.hasNext())

            p_rows[MONTH_SHEET].inc();
            if (p_includeExReview && containsDellReview)
            {
                p_rows[MONTH_REVIEW_SHEET].inc();
            }

        }// End loop while (projectIter.hasNext())

        p_rows[MONTH_SHEET].inc();
        addTotals(p_workbook, p_sheets[MONTH_SHEET], MONTH_SHEET, p_rows[MONTH_SHEET]);

        if (p_includeExReview)
        {
            p_rows[MONTH_REVIEW_SHEET].inc();
            addTotals(p_workbook, p_sheets[MONTH_REVIEW_SHEET], MONTH_REVIEW_SHEET,
                    p_rows[MONTH_REVIEW_SHEET]);
        }

        if (totalCol != null)
        {
            addTotalsPerLang(p_workbook, p_sheets[MONTH_SHEET], MONTH_SHEET,
                    p_rows[MONTH_SHEET]);
        }
    }

    /**
     * For Trados Style
     * 
     * @param p_projectMap
     * @param p_sheets
     * @param p_includeExReview
     * @param p_rows
     * @param p_data
     * @param bundle
     * @throws Exception
     */
    private void writeProjectDataTradosStyle(
            HashMap<String, HashMap<String, ProjectWorkflowData>> p_projectMap,
            Workbook p_workbook, Sheet[] p_sheets, boolean p_includeExReview,
            IntHolder[] p_rows)
            throws Exception
    {
        ArrayList<String> keys = new ArrayList<String>(p_projectMap.keySet());
        SortUtil.sort(keys);
        Iterator<String> keysIter = keys.iterator();

        totalCost.clear();
        totalCostDate.clear();
        int finishedJobNum = 0;
        while (keysIter.hasNext())
        {
            // Cancel generate reports.
            if (isCancelled())
                return;
            // Sets Reports Percent.
            setPercent(++finishedJobNum);

            String key = keysIter.next();
            String jobId = getJobIdFromMapKey(key);
            boolean isWrongJob = m_data.wrongJobNames.contains(jobId);
            HashMap<String, ProjectWorkflowData> localeMap = p_projectMap
                    .get(key);
            ArrayList<String> locales = new ArrayList<String>(
                    localeMap.keySet());
            SortUtil.sort(locales);
            Iterator<String> localeIter = locales.iterator();
            BigDecimal projectTotalWordCountCost = new BigDecimal(
                    BIG_DECIMAL_ZERO_STRING);
            BigDecimal reviewTotalWordCountCost = new BigDecimal(
                    BIG_DECIMAL_ZERO_STRING);

            // Counts the review costing total and the translation costing
            // total.
            String localeName = null;
            ProjectWorkflowData data = null;

            // Marks whether needs to add a blank line.
            boolean containsDellReview = false;

            while (localeIter.hasNext())
            {
                int col = 0;
                localeName = (String) localeIter.next();
                data = (ProjectWorkflowData) localeMap.get(localeName);

                int row = p_rows[MONTH_SHEET].getValue();
                Row theRow = getRow(p_sheets[MONTH_SHEET], row);
                
                CellStyle temp_moneyStyle = getMoneyStyle(p_workbook);
                CellStyle temp_normalStyle = getContentStyle(p_workbook);

                if (data.wasExportFailed)
                {
                	temp_moneyStyle = getFailedMoneyStyle(p_workbook);
                	temp_normalStyle =  getRedCellStyle(p_workbook);
                }

                // Company Name
                Cell cell_A = getCell(theRow, col++);
                cell_A.setCellValue(data.companyName);
                cell_A.setCellStyle(temp_normalStyle);
                p_sheets[MONTH_SHEET].setColumnWidth(col - 1, 15 * 256);

                // Project Description
                Cell cell_B = getCell(theRow, col++);
                cell_B.setCellValue(data.projectDesc);
                cell_B.setCellStyle(temp_normalStyle);
                p_sheets[MONTH_SHEET].setColumnWidth(col - 1, 22 * 256);

                if (isWrongJob)
                {
                    // For "Add Job Id into online job report" issue
                    if (isJobIdVisible)
                    {
                    	Cell cell_C = getCell(theRow, col++);
                        cell_C.setCellValue(Long.valueOf(jobId));
                        cell_C.setCellStyle(getWrongJobStyle(p_workbook));
                    }

                    Cell cell_CorD = getCell(theRow, col++);
                    cell_CorD.setCellValue(data.jobName);
                    cell_CorD.setCellStyle(getWrongJobStyle(p_workbook));
                }
                else
                {
                    // For "Add Job Id into online job report" issue
                    if (isJobIdVisible)
                    {
                    	Cell cell_C = getCell(theRow, col++);
                    	cell_C.setCellValue(Long.valueOf(jobId));
                    	cell_C.setCellStyle(temp_normalStyle);
                    }
                    Cell cell_CorD = getCell(theRow, col++);
                    cell_CorD.setCellValue(data.jobName);
                    cell_CorD.setCellStyle(temp_normalStyle);
                }
                p_sheets[MONTH_SHEET].setColumnWidth(col - 1, 50 * 256);
                
                Cell cell_DorE = getCell(theRow, col++);
                cell_DorE.setCellValue(getAllSouceFileFormats(data.allFileProfiles));
                cell_DorE.setCellStyle(temp_normalStyle);
                p_sheets[MONTH_SHEET].setColumnWidth(col - 1, 20 * 256);
                
                Cell cell_EorF = getCell(theRow, col++);
                cell_EorF.setCellValue(data.l10nProfileName);
                cell_EorF.setCellStyle(temp_normalStyle);
                p_sheets[MONTH_SHEET].setColumnWidth(col - 1, 20 * 256);
                
                Cell cell_ForG = getCell(theRow, col++);
                cell_ForG.setCellValue(data.fileProfileNames);
                cell_ForG.setCellStyle(temp_normalStyle);
                p_sheets[MONTH_SHEET].setColumnWidth(col - 1, 20 * 256);

                if (data.wasExportFailed)
                {
                	Cell cell_GorH = getCell(theRow, col++);
                	cell_GorH.setCellValue(data.creationDate);
                	cell_GorH.setCellStyle(getFailedDateStyle(p_workbook));
                    p_sheets[MONTH_SHEET].setColumnWidth(col - 1, 15 * 256);
                    
                    Cell cell_HorI = getCell(theRow, col++);
                    cell_HorI.setCellValue(data.creationDate);
                    cell_HorI.setCellStyle(getFailedTimeStyle(p_workbook));
                    p_sheets[MONTH_SHEET].setColumnWidth(col - 1, 18 * 256);
                    
                    Cell cell_IorJ = getCell(theRow, col++);
                    cell_IorJ.setCellValue("");
                    cell_IorJ.setCellStyle(getFailedDateStyle(p_workbook));
                    p_sheets[MONTH_SHEET].setColumnWidth(col - 1, 15 * 256);
                    
                    Cell cell_JorK = getCell(theRow, col++);
                    cell_JorK.setCellValue("");
                    cell_JorK.setCellStyle(getFailedTimeStyle(p_workbook));
                }
                else
                {
                	Cell cell_GorH = getCell(theRow, col++);
                	cell_GorH.setCellValue(getDateFormat().format(data.creationDate));
                	cell_GorH.setCellStyle(getDateStyle(p_workbook));
                    p_sheets[MONTH_SHEET].setColumnWidth(col - 1, 15 * 256);
                    
                    Cell cell_HorI = getCell(theRow, col++);
                    cell_HorI.setCellValue(getTimeFormat().format(data.creationDate));
                    cell_HorI.setCellStyle(getContentStyle(p_workbook));
                    p_sheets[MONTH_SHEET].setColumnWidth(col - 1, 18 * 256);
                    
                    Cell cell_IorJ = getCell(theRow, col++);
                    if (data.wasExported)
                    {
                        cell_IorJ.setCellValue(getDateFormat().format(data.exportDate));
                    }else {
                    	cell_IorJ.setCellValue("");
					}
                    cell_IorJ.setCellStyle(getDateStyle(p_workbook));
                    p_sheets[MONTH_SHEET].setColumnWidth(col - 1, 15 * 256);
                    
                    String labelExportTime = "";
                    if (data.wasExported)
                    {
                        labelExportTime = getExportDateStr(getTimeFormat(),
                                data.exportDate);
                    }
                    Cell cell_JorK = getCell(theRow, col++);
                    cell_JorK.setCellValue(labelExportTime);
                    cell_JorK.setCellStyle(getContentStyle(p_workbook));

                }
                p_sheets[MONTH_SHEET].setColumnWidth(col - 1, 18 * 256);

                // Status
                Cell cell_KorL = getCell(theRow, col++);
                cell_KorL.setCellValue(data.status);
                cell_KorL.setCellStyle(temp_normalStyle);
                p_sheets[MONTH_SHEET].setColumnWidth(col - 1, 18 * 256);
                // Language
                Cell cell_LorM = getCell(theRow, col++);
                cell_LorM.setCellValue(data.targetLang);
                cell_LorM.setCellStyle(temp_normalStyle);

                int numwidth = 10;

                // Summary Start Column
                m_data.initSumStartCol(col);
                
                Cell cell_MorN = getCell(theRow, col++);
                cell_MorN.setCellValue(data.segmentTmWordCount);
                cell_MorN.setCellStyle(temp_normalStyle);
                p_sheets[MONTH_SHEET].setColumnWidth(col - 1, numwidth * 256);
                
                Cell cell_NorO = getCell(theRow, col++);
                cell_NorO.setCellValue(data.hiFuzzyMatchWordCount);
                cell_NorO.setCellStyle(temp_normalStyle);
                p_sheets[MONTH_SHEET].setColumnWidth(col - 1, numwidth * 256);

                Cell cell_OorP = getCell(theRow, col++);
                cell_OorP.setCellValue(data.medHiFuzzyMatchWordCount);
                cell_OorP.setCellStyle(temp_normalStyle);
                p_sheets[MONTH_SHEET].setColumnWidth(col - 1, numwidth * 256);

                Cell cell_PorQ = getCell(theRow, col++);
                cell_PorQ.setCellValue(data.medFuzzyMatchWordCount);
                cell_PorQ.setCellStyle(temp_normalStyle);
                p_sheets[MONTH_SHEET].setColumnWidth(col - 1, numwidth * 256);
                
                Cell cell_QorR = getCell(theRow, col++);
                cell_QorR.setCellValue(data.noMatchWordCount);
                cell_QorR.setCellStyle(temp_normalStyle);
                p_sheets[MONTH_SHEET].setColumnWidth(col - 1, numwidth * 256);

                Cell cell_RorS = getCell(theRow, col++);
                cell_RorS.setCellValue(data.repetitionWordCount);
                cell_RorS.setCellStyle(temp_normalStyle);
                p_sheets[MONTH_SHEET].setColumnWidth(col - 1, numwidth * 256);
                
                if (m_data.useInContext)
                {
                	Cell cell_InContext = getCell(theRow, col++);
                    cell_InContext.setCellValue(data.inContextMatchWordCount);
                    cell_InContext.setCellStyle(temp_normalStyle);
                    p_sheets[MONTH_SHEET].setColumnWidth(col - 1, numwidth * 256);
                }

                Cell cell_Total = getCell(theRow, col++);
                cell_Total.setCellValue(data.totalWordCount);
                cell_Total.setCellStyle(temp_normalStyle);
                p_sheets[MONTH_SHEET].setColumnWidth(col - 1, numwidth * 256);

                int moneywidth = 12;

                Cell cell_SegmentCost = getCell(theRow, col++);
                cell_SegmentCost.setCellValue(asDouble(data.segmentTmWordCountCost));
                cell_SegmentCost.setCellStyle(temp_moneyStyle);
                p_sheets[MONTH_SHEET].setColumnWidth(col - 1, moneywidth * 256);
                
                Cell cell_HiFuzzyMatchCost = getCell(theRow, col++);
                cell_HiFuzzyMatchCost.setCellValue(asDouble(data
                		.hiFuzzyMatchWordCountCost));
                cell_HiFuzzyMatchCost.setCellStyle(temp_moneyStyle);
                p_sheets[MONTH_SHEET].setColumnWidth(col - 1, moneywidth * 256);
                
                Cell cell_MedHiFuzzyMatchCost = getCell(theRow, col++);
                cell_MedHiFuzzyMatchCost.setCellValue(asDouble(data
                		.medHiFuzzyMatchWordCountCost));
                cell_MedHiFuzzyMatchCost.setCellStyle(temp_moneyStyle);
                p_sheets[MONTH_SHEET].setColumnWidth(col - 1, moneywidth * 256);

                Cell cell_MedFuzzyMatchCost = getCell(theRow, col++);
                cell_MedFuzzyMatchCost.setCellValue(asDouble(data.medFuzzyMatchWordCountCost));
                cell_MedFuzzyMatchCost.setCellStyle(temp_moneyStyle);
                p_sheets[MONTH_SHEET].setColumnWidth(col - 1, moneywidth * 256);
                
                Cell cell_NoMatchCost = getCell(theRow, col++);
                cell_NoMatchCost.setCellValue(asDouble(data.noMatchWordCountCost));
                cell_NoMatchCost.setCellStyle(temp_moneyStyle);
                p_sheets[MONTH_SHEET].setColumnWidth(col - 1, moneywidth * 256);

                Cell cell_RepetitionCost = getCell(theRow, col++);
                cell_RepetitionCost.setCellValue(asDouble(data.repetitionWordCountCost));
                cell_RepetitionCost.setCellStyle(temp_moneyStyle);
                p_sheets[MONTH_SHEET].setColumnWidth(col - 1, moneywidth * 256);

                if (m_data.useInContext)
                {           	
                	Cell cell_InContextCost = getCell(theRow, col++);
                	cell_InContextCost.setCellValue(asDouble(data.inContextMatchWordCountCost));
                	cell_InContextCost.setCellStyle(temp_moneyStyle);
                    p_sheets[MONTH_SHEET].setColumnWidth(col - 1, moneywidth * 256);
                }

                if (totalCol == null)
                {
                    totalCol = ReportUtil.toChar(col);
                }
                Cell cell_TotalCost = getCell(theRow, col++);
                cell_TotalCost.setCellValue(asDouble(data.totalWordCountCost));
                cell_TotalCost.setCellStyle(temp_moneyStyle);

                // Show total additional charges
                // p_sheets[MONTH_SHEET].setColumnView(col - 1, moneywidth);
                // p_sheets[MONTH_SHEET].addCell(new Number(col++, row,
                // asDouble(data.totalAdditionalCost), temp_moneyFormat));

                // p_sheets[MONTH_SHEET].setColumnView(col - 1, moneywidth);
                projectTotalWordCountCost = projectTotalWordCountCost
                        .add(data.totalWordCountCost);

                List<Integer> indexs = totalCost.get(data.targetLang);
                if (indexs == null)
                {
                    indexs = new ArrayList<Integer>();
                    totalCost.put(data.targetLang, indexs);
                }
                indexs.add(row + 1);

                Double value = totalCostDate.get(data.targetLang);
                if (value == null)
                {
                    value = 0.0;
                }
                value += asDouble(data.totalWordCountCost);
                totalCostDate.put(data.targetLang, value);

                // add a "project total" summary cost over the locales
                // map(1,moneyFormat)
                if (localeIter.hasNext() == false)
                {
                	Cell cell_TotalAdditionalCost = getCell(theRow, col++);
                	cell_TotalAdditionalCost.setCellValue(asDouble(data.totalAdditionalCost));
                	cell_TotalAdditionalCost.setCellStyle(temp_moneyStyle);
                    p_sheets[MONTH_SHEET].setColumnWidth(col - 1, moneywidth * 256);

                    Cell cell_ProjectTotalCost = getCell(theRow, col++);
                    cell_ProjectTotalCost.setCellValue(asDouble(projectTotalWordCountCost)
                            + asDouble(data.totalAdditionalCost));
                    cell_ProjectTotalCost.setCellStyle(temp_moneyStyle);
                }
                else
                {
                	Cell cell_TotalAdditionalCost = getCell(theRow, col++);
                	cell_TotalAdditionalCost.setCellStyle(temp_moneyStyle);
                    p_sheets[MONTH_SHEET].setColumnWidth(col - 1, moneywidth * 256);

                    Cell cell_ProjectTotalCost = getCell(theRow, col++);
                    cell_ProjectTotalCost.setCellStyle(temp_moneyStyle);
                }
                p_sheets[MONTH_SHEET].setColumnWidth(col - 1, moneywidth * 256);
                p_rows[MONTH_SHEET].inc();

                // Write data into MONTH_REVIEW_SHEET
                if (p_includeExReview && data.containsDellReview)
                {
                    containsDellReview = true;

                    row = p_rows[MONTH_REVIEW_SHEET].getValue();
                    col = 0;

                    // Company Name
                    Cell cell_A_Review = getCell(theRow, col++);
                    cell_A_Review.setCellValue(data.companyName);
                    cell_A_Review.setCellStyle(temp_normalStyle);
                    p_sheets[MONTH_REVIEW_SHEET].setColumnWidth(col - 1, 15 * 256);

                    // Project Description
                    Cell cell_B_Review = getCell(theRow, col++);
                    cell_B_Review.setCellValue(data.projectDesc);
                    cell_B_Review.setCellStyle(temp_normalStyle);
                    p_sheets[MONTH_REVIEW_SHEET].setColumnWidth(col - 1, 22 * 256);
                    
                    if (isWrongJob)
                    {
                        // For "Job id not showing in external review tabs"
                        // Issue
                        if (isJobIdVisible)
                        {
                        	Cell cell_C_Review = getCell(theRow, col++);
                        	cell_C_Review.setCellValue(data.jobId);
                        	cell_C_Review.setCellStyle(getWrongJobStyle(p_workbook));
                        }
                        Cell cell_CorD_Review = getCell(theRow, col++);
                        cell_CorD_Review.setCellValue(data.jobName);
                        cell_CorD_Review.setCellStyle(getWrongJobStyle(p_workbook));
                    }
                    else
                    {
                        // For "Job id not showing in external review tabs"
                        // Issue
                        if (isJobIdVisible)
                        {
                        	Cell cell_C_Review = getCell(theRow, col++);
                        	cell_C_Review.setCellValue(data.jobId);
                        	cell_C_Review.setCellStyle(temp_normalStyle);
                        }
                        Cell cell_CorD_Review = getCell(theRow, col++);
                        cell_CorD_Review.setCellValue(data.jobName);
                        cell_CorD_Review.setCellStyle(temp_normalStyle);
                    }
                    p_sheets[MONTH_REVIEW_SHEET].setColumnWidth(col - 1, 50 * 256);
                    Cell cell_DorE_Review = getCell(theRow, col++);
                    cell_DorE_Review.setCellValue(data.projectDesc);
                    cell_DorE_Review.setCellStyle(temp_normalStyle);
                    p_sheets[MONTH_REVIEW_SHEET].setColumnWidth(col - 1, 22 * 256);

                    if (data.wasExportFailed)
                    {
                        /*
                         * p_sheets[MONTH_SHEET].addCell(new DateTime(col++,
                         * row, data.creationDate, failed_dateFormat));
                         */
                    	Cell cell_EorF_Review = getCell(theRow, col++);
                    	cell_EorF_Review.setCellValue(data.creationDate);
                    	cell_EorF_Review.setCellStyle(getFailedDateStyle(p_workbook));
                        p_sheets[MONTH_REVIEW_SHEET].setColumnWidth(col - 1, 15 * 256);
                        
                        Cell cell_ForG_Review = getCell(theRow, col++);
                        cell_ForG_Review.setCellValue(data.creationDate);
                        cell_ForG_Review.setCellStyle(getFailedTimeStyle(p_workbook));
                        p_sheets[MONTH_REVIEW_SHEET].setColumnWidth(col - 1, 18 * 256);
                        
                        Cell cell_GorH_Review = getCell(theRow, col++);
                        cell_GorH_Review.setCellValue("");
                        cell_GorH_Review.setCellStyle(getFailedDateStyle(p_workbook));
                        p_sheets[MONTH_REVIEW_SHEET].setColumnWidth(col - 1, 15 * 256);
                        
                        Cell cell_HorI_Review = getCell(theRow, col++);
                        cell_HorI_Review.setCellValue("");
                        cell_HorI_Review.setCellStyle(getFailedTimeStyle(p_workbook));
                    }
                    else
                    {
                        /*
                         * p_sheets[MONTH_SHEET].addCell(new Label(col++, row,
                         * dateFormat.format(data.creationDate)));
                         */
                    	Cell cell_EorF_Review = getCell(theRow, col++);
                    	cell_EorF_Review.setCellValue(getDateFormat().format(data.creationDate));
                        cell_EorF_Review.setCellStyle(getContentStyle(p_workbook));
                        p_sheets[MONTH_REVIEW_SHEET].setColumnWidth(col - 1, 15 * 256);
                        
                        Cell cell_ForG_Review = getCell(theRow, col++);
                        cell_ForG_Review.setCellValue(getTimeFormat().format(data.creationDate));
                        cell_ForG_Review.setCellStyle(getContentStyle(p_workbook));
                        p_sheets[MONTH_REVIEW_SHEET].setColumnWidth(col - 1, 18 * 256);
                        
                        String labelExportDate = "";
                        if (data.wasExported)
                        {
                            labelExportDate = getExportDateStr(getDateFormat(),
                                    data.exportDate);
                        }
                        Cell cell_GorH_Review = getCell(theRow, col++);
                        cell_GorH_Review.setCellValue(labelExportDate);
                        cell_GorH_Review.setCellStyle(getContentStyle(p_workbook));
                        p_sheets[MONTH_REVIEW_SHEET].setColumnWidth(col - 1, 15 * 256);
                        
                        String labelExportTime = "";
                        if (data.wasExported)
                        {
                            labelExportTime = getExportDateStr(getTimeFormat(),
                                    data.exportDate);
                        }
                        Cell cell_HorI_Review = getCell(theRow, col++);
                        cell_HorI_Review.setCellValue(labelExportTime);
                        cell_HorI_Review.setCellStyle(getContentStyle(p_workbook));

                    }
                    p_sheets[MONTH_REVIEW_SHEET].setColumnWidth(col - 1, 18 * 256);
                    Cell cell_IorJ_Review = getCell(theRow, col++);
                    cell_IorJ_Review.setCellValue(data.targetLang);
                    cell_IorJ_Review.setCellStyle(temp_normalStyle);
                    
                    Cell cell_JorK_Review = getCell(theRow, col++);
                    cell_JorK_Review.setCellValue(data.repetitionWordCount);
                    cell_JorK_Review.setCellStyle(temp_normalStyle);

                    numwidth = 10;
                    p_sheets[MONTH_REVIEW_SHEET].setColumnWidth(col - 1,
                            numwidth * 256);
                    Cell cell_KorL_Review = getCell(theRow, col++);
                    cell_KorL_Review.setCellValue(data.segmentTmWordCount);
                    cell_KorL_Review.setCellStyle(temp_normalStyle);
                    p_sheets[MONTH_REVIEW_SHEET].setColumnWidth(col - 1,
                            numwidth * 256);

                    if (m_data.useInContext)
                    {
                    	Cell cell_InContext = getCell(theRow, col++);
                    	cell_InContext.setCellValue(data.inContextMatchWordCount);
                    	cell_InContext.setCellStyle(temp_normalStyle);
                        p_sheets[MONTH_REVIEW_SHEET].setColumnWidth(col - 1,
                                numwidth * 256);
                    }
                    
                    Cell cell_FuzzyMatch_Review = getCell(theRow, col++);
                    cell_FuzzyMatch_Review.setCellValue(data.fuzzyMatchWordCount);
                    cell_FuzzyMatch_Review.setCellStyle(temp_normalStyle);
                    p_sheets[MONTH_REVIEW_SHEET].setColumnWidth(col - 1,
                            numwidth * 256);

                    Cell cell_NoMatch_Review = getCell(theRow, col++);
                    cell_NoMatch_Review.setCellValue(data.noMatchWordCount);
                    cell_NoMatch_Review.setCellStyle(temp_normalStyle);
                    p_sheets[MONTH_REVIEW_SHEET].setColumnWidth(col - 1,
                            numwidth * 256);
                    
                    Cell cell_Total_Review = getCell(theRow, col++);
                    cell_Total_Review.setCellValue(data.totalWordCount);
                    cell_Total_Review.setCellStyle(temp_normalStyle);
                    p_sheets[MONTH_REVIEW_SHEET].setColumnWidth(col - 1,
                            numwidth * 256);

                    moneywidth = 12;
                    Cell cell_RepetitionCost_Review = getCell(theRow, col++);
                    cell_RepetitionCost_Review.setCellValue(asDouble(data
                    		.repetitionWordCountCostForDellReview));
                    cell_RepetitionCost_Review.setCellStyle(temp_moneyStyle);
                    p_sheets[MONTH_REVIEW_SHEET].setColumnWidth(col - 1,
                            moneywidth * 256);
                    Cell cell_SegmentCost_Review = getCell(theRow, col++);
                    cell_SegmentCost_Review.setCellValue(asDouble(data
                    		.segmentTmWordCountCostForDellReview));
                    cell_SegmentCost_Review.setCellStyle(temp_moneyStyle);
                    p_sheets[MONTH_REVIEW_SHEET].setColumnWidth(col - 1,
                            moneywidth * 256);

                    if (m_data.useInContext)
                    {
                    	Cell cell_InContext = getCell(theRow, col++);
                    	cell_InContext.setCellValue(asDouble(data
                    			.inContextMatchWordCountCostForDellReview));
                    	cell_InContext.setCellStyle(temp_moneyStyle);
                        p_sheets[MONTH_REVIEW_SHEET].setColumnWidth(col - 1,
                                moneywidth * 256);
                    }

                    Cell cell_FuzzyMatchCost_Review = getCell(theRow, col++);
                    cell_FuzzyMatchCost_Review.setCellValue(asDouble(data
                    		.fuzzyMatchWordCountCostForDellReview));
                    cell_FuzzyMatchCost_Review.setCellStyle(temp_moneyStyle);
                    p_sheets[MONTH_REVIEW_SHEET].setColumnWidth(col - 1,
                            moneywidth * 256);

                    Cell cell_NoMatchCost_Review = getCell(theRow, col++);
                    cell_NoMatchCost_Review.setCellValue(asDouble(data
                    		.noMatchWordCountCostForDellReview));
                    cell_NoMatchCost_Review.setCellStyle(temp_moneyStyle);
                    p_sheets[MONTH_REVIEW_SHEET].setColumnWidth(col - 1,
                            moneywidth * 256);

                    reviewTotalWordCountCost = reviewTotalWordCountCost
                            .add(data.totalWordCountCostForDellReview);

                    // Writes the "Translation Total" column.
                    Cell cell_TotalCost_Review = getCell(theRow, col++);
                    cell_TotalCost_Review.setCellValue(asDouble(data.totalWordCountCost));
                    cell_TotalCost_Review.setCellStyle(temp_moneyStyle);
                    p_sheets[MONTH_REVIEW_SHEET].setColumnWidth(col - 1,
                            moneywidth * 256);
                    // Writes the "Review" column.
                    Cell cell_Rview = getCell(theRow, col++);
                    cell_Rview.setCellValue(asDouble(data.totalWordCountCostForDellReview));
                    cell_Rview.setCellStyle(temp_moneyStyle);
                    p_sheets[MONTH_REVIEW_SHEET].setColumnWidth(col - 1,
                            moneywidth * 256);

                    // add "job total" cost over the locales
                    if (localeIter.hasNext() == false)
                    {
                    	Cell cell_JobTotal = getCell(theRow, col++);
                    	cell_JobTotal.setCellValue(asDouble(projectTotalWordCountCost
                                .add(reviewTotalWordCountCost)));
                    	cell_JobTotal.setCellStyle(temp_moneyStyle);
                    }
                    else
                    {
                    	Cell cell_JobTotal = getCell(theRow, col++);
                    	cell_JobTotal.setCellStyle(temp_moneyStyle);;
                    }
                    p_sheets[MONTH_REVIEW_SHEET].setColumnWidth(col - 1,
                            moneywidth * 256);
                    p_rows[MONTH_REVIEW_SHEET].inc();
                }// End if (p_includeExReview && data.containsDellReview)
            }// End loop while (localeIter.hasNext())

            p_rows[MONTH_SHEET].inc();
            if (p_includeExReview && containsDellReview)
            {
                p_rows[MONTH_REVIEW_SHEET].inc();
            }

        }// End loop while (projectIter.hasNext())

        p_rows[MONTH_SHEET].inc();
        addTotals(p_workbook, p_sheets[MONTH_SHEET], MONTH_SHEET, p_rows[MONTH_SHEET]);

        if (p_includeExReview)
        {
            p_rows[MONTH_REVIEW_SHEET].inc();
            addTotals(p_workbook, p_sheets[MONTH_REVIEW_SHEET], MONTH_REVIEW_SHEET,
                    p_rows[MONTH_REVIEW_SHEET]);
        }

        if (totalCol != null)
        {
            addTotalsPerLang(p_workbook, p_sheets[MONTH_SHEET], MONTH_SHEET,
                    p_rows[MONTH_SHEET]);
        }
    }

    private void addTotalsPerLang(Workbook p_workbook, Sheet p_sheet,
            final int p_sheetCategory, IntHolder p_row) throws Exception
    {
    	Font subTotalFont = p_workbook.createFont();
    	subTotalFont.setBoldweight(Font.BOLDWEIGHT_BOLD);
    	subTotalFont.setColor(IndexedColors.BLACK.getIndex());
    	subTotalFont.setUnderline(Font.U_NONE);
    	subTotalFont.setFontName("Arial");
    	subTotalFont.setFontHeightInPoints((short) 10);
    	
        CellStyle subTotalStyle = p_workbook.createCellStyle();
        subTotalStyle.setFont(subTotalFont);
        
        String title = m_bundle.getString("lb_total_cost_per_lang");
        int row = p_row.getValue() + 4; // skip a row
        ArrayList<String> locales = new ArrayList<String>(totalCost.keySet());
        SortUtil.sort(locales);

        int col = m_data.getSumStartCol();
        Row theRow = getRow(p_sheet, row);
        
        Cell cell_Title = getCell(theRow, col - 3);
        cell_Title.setCellValue(title);
        cell_Title.setCellStyle(subTotalStyle);

        File imgFile = File.createTempFile("GSJobChart", ".png");
        JfreeCharUtil.drawPieChart2D("", totalCostDate, imgFile);
        Drawing patriarch = p_sheet.createDrawingPatriarch();
        XSSFClientAnchor anchor = new XSSFClientAnchor(0, 0, 0, 0, 15, row - 1, 22, row + 24);      
        ByteArrayOutputStream img_bytes = new ByteArrayOutputStream();
        InputStream is = imgFile.toURI().toURL().openStream();
        int b;
        while ((b = is.read()) != -1){
            img_bytes.write(b);
        }
        is.close();
        int i = p_workbook.addPicture(img_bytes.toByteArray(), SXSSFWorkbook.PICTURE_TYPE_PNG);
        patriarch.createPicture(anchor, i);
        for (String locale : locales)
        {
            List<Integer> indexs = totalCost.get(locale);
            StringBuffer values = new StringBuffer();
            for (Integer index : indexs)
            {
                if (values.length() == 0)
                {
                    values.append("SUM(");
                }
                else
                {
                    values.append("+");
                }

                values.append(totalCol).append(index);
            }
            values.append(")");

            Row perRow = getRow(p_sheet, row);
            
            Cell cell_Locale = getCell(perRow, col - 1);
            cell_Locale.setCellValue(locale);
            cell_Locale.setCellStyle(getContentStyle(p_workbook));
            Cell cell_Total = getCell(perRow, col);
            cell_Total.setCellFormula(values.toString());
            cell_Total.setCellStyle(getMoneyStyle(p_workbook));
            row++;
        }

        // Reset total column number for every sheet.
        totalCol = null;
    }

    /**
     * Add totals row
     * 
     * @param p_sheet
     * @param p_sheetCategory
     * @param p_row
     * @param p_data
     * @param bundle
     * @throws Exception
     */
    private void addTotals(Workbook p_workbook, Sheet p_sheet, final int p_sheetCategory,
            IntHolder p_row)
            throws Exception
    {
        int row = p_row.getValue() + 1; // skip a row

        String title = m_bundle.getString("lb_totals");
        java.text.NumberFormat.getCurrencyInstance(Locale.US);
        // Get Summary Start Column
        int c = m_data.getSumStartCol();
        String sumStartCol = getColumnName(c);
        Row theRow = getRow(p_sheet, row);

        Cell cell_A = getCell(theRow, 0);
        cell_A.setCellValue(title);
        cell_A.setCellStyle(getSubTotalStyle(p_workbook));
        // modify the number 3 to "sumStartCellCol-B" for "Add Job Id into
        // online job report" issue
        p_sheet.addMergedRegion(new CellRangeAddress(row, row, 0, sumStartCol.charAt(0) - 'B'));
        setRegionStyle(p_sheet, new CellRangeAddress(row, row, 0, sumStartCol.charAt(0) - 'B'),
        		getSubTotalStyle(p_workbook));
        int lastRow = p_row.getValue() - 2;

        // add in word count totals
        // word counts
        Cell cell_B = getCell(theRow, c++);
        cell_B.setCellFormula("SUM(" + sumStartCol + "5:"
                + sumStartCol + lastRow + ")");
        cell_B.setCellStyle(getSubTotalStyle(p_workbook));
        
        sumStartCol = getColumnName(c);
        Cell cell_C = getCell(theRow, c++);
        cell_C.setCellFormula("SUM(" + sumStartCol + "5:"
                + sumStartCol + lastRow + ")");
        cell_C.setCellStyle(getSubTotalStyle(p_workbook));
        
        sumStartCol = getColumnName(c);
        Cell cell_D = getCell(theRow, c++);
        cell_D.setCellFormula("SUM(" + sumStartCol + "5:"
                + sumStartCol + lastRow + ")");
        cell_D.setCellStyle(getSubTotalStyle(p_workbook));
        
        sumStartCol = getColumnName(c);
        Cell cell_E = getCell(theRow, c++);
        cell_E.setCellFormula("SUM(" + sumStartCol + "5:"
                + sumStartCol + lastRow + ")");
        cell_E.setCellStyle(getSubTotalStyle(p_workbook));
        
        sumStartCol = getColumnName(c);
        Cell cell_F = getCell(theRow, c++);
        cell_F.setCellFormula("SUM(" + sumStartCol + "5:"
                + sumStartCol + lastRow + ")");
        cell_F.setCellStyle(getSubTotalStyle(p_workbook));
        
        sumStartCol = getColumnName(c);
        if (m_data.isTradosStyle())
        {
        	Cell cell_G = getCell(theRow, c++);
            cell_G.setCellFormula("SUM(" + sumStartCol + "5:"
                    + sumStartCol + lastRow + ")");
            cell_G.setCellStyle(getSubTotalStyle(p_workbook));
            sumStartCol = getColumnName(c);
            
            Cell cell_H = getCell(theRow, c++);
            cell_H.setCellFormula("SUM(" + sumStartCol + "5:"
                    + sumStartCol + lastRow + ")");
            cell_H.setCellStyle(getSubTotalStyle(p_workbook));
            sumStartCol = getColumnName(c);
        }

        if (m_data.useInContext)
        {
        	Cell cell_InContext = getCell(theRow, c++);
        	cell_InContext.setCellFormula("SUM(" + sumStartCol + "5:"
                    + sumStartCol + lastRow + ")");
        	cell_InContext.setCellStyle(getSubTotalStyle(p_workbook));
            sumStartCol = getColumnName(c);
        }

        // word count costs
        Cell cell_K = getCell(theRow, c++);
        cell_K.setCellFormula("SUM(" + sumStartCol + "5:"
                + sumStartCol + lastRow + ")");
        cell_K.setCellStyle(getTotalMoneyStyle(p_workbook));
        sumStartCol = getColumnName(c);
        
        Cell cell_L = getCell(theRow, c++);
        cell_L.setCellFormula("SUM(" + sumStartCol + "5:"
                + sumStartCol + lastRow + ")");
        cell_L.setCellStyle(getTotalMoneyStyle(p_workbook));
        sumStartCol = getColumnName(c);
        
        Cell cell_M = getCell(theRow, c++);
        cell_M.setCellFormula("SUM(" + sumStartCol + "5:"
                + sumStartCol + lastRow + ")");
        cell_M.setCellStyle(getTotalMoneyStyle(p_workbook));
        sumStartCol = getColumnName(c);
        
        Cell cell_N = getCell(theRow, c++);
        cell_N.setCellFormula("SUM(" + sumStartCol + "5:"
                + sumStartCol + lastRow + ")");
        cell_N.setCellStyle(getTotalMoneyStyle(p_workbook));
        sumStartCol = getColumnName(c);
        
        Cell cell_O = getCell(theRow, c++);
        cell_O.setCellFormula("SUM(" + sumStartCol + "5:"
                + sumStartCol + lastRow + ")");
        cell_O.setCellStyle(getTotalMoneyStyle(p_workbook));
        sumStartCol = getColumnName(c);
        
        Cell cell_P = getCell(theRow, c++);
        cell_P.setCellFormula("SUM(" + sumStartCol + "5:"
                + sumStartCol + lastRow + ")");
        cell_P.setCellStyle(getTotalMoneyStyle(p_workbook));
        sumStartCol = getColumnName(c);
        
        Cell cell_Q = getCell(theRow, c++);
        cell_Q.setCellFormula("SUM(" + sumStartCol + "5:"
                + sumStartCol + lastRow + ")");
        cell_Q.setCellStyle(getTotalMoneyStyle(p_workbook));
        sumStartCol = getColumnName(c);

        if (m_data.isTradosStyle())
        {
        	Cell cell_R = getCell(theRow, c++);
            cell_R.setCellFormula("SUM(" + sumStartCol + "5:"
                    + sumStartCol + lastRow + ")");
            cell_R.setCellStyle(getTotalMoneyStyle(p_workbook));
            sumStartCol = getColumnName(c);
            
            Cell cell_S = getCell(theRow, c++);
            cell_S.setCellFormula("SUM(" + sumStartCol + "5:"
                    + sumStartCol + lastRow + ")");
            cell_S.setCellStyle(getTotalMoneyStyle(p_workbook));
            sumStartCol = getColumnName(c);
        }

        if (m_data.useInContext)
        {
        	Cell cell_InContext = getCell(theRow, c++);
            cell_InContext.setCellFormula("SUM(" + sumStartCol + "5:"
                    + sumStartCol + lastRow + ")");
            cell_InContext.setCellStyle(getTotalMoneyStyle(p_workbook));
            sumStartCol = getColumnName(c);
        }

        // add an extra column for Dell Tracking Use
        Cell cell_V = getCell(theRow, c++);
        cell_V.setCellValue("");
        cell_V.setCellStyle(getTotalMoneyStyle(p_workbook));
    }
    
    private void addParamsSheet(Workbook p_workbook, String year,
    		boolean recalculateFinishedWorkflow) throws Exception
    {
    	Sheet paramsSheet = p_workbook.createSheet(
                m_bundle.getString("lb_criteria"));
        Row firRow = getRow(paramsSheet, 0);
        Cell cell_A_Header = getCell(firRow, 0);
        cell_A_Header.setCellValue(m_bundle
                .getString("lb_report_criteria"));
        cell_A_Header.setCellStyle(getContentStyle(p_workbook));
        paramsSheet.setColumnWidth(0, 50 * 256);

        Row secRow = getRow(paramsSheet, 1);
        Cell cell_A_Project = getCell(secRow, 0);
        if (m_data.wantsAllProjects)
        {
        	cell_A_Project.setCellValue(m_bundle
                    .getString("lb_selected_projects")
                    + " "
                    + m_bundle.getString("all"));
        	cell_A_Project.setCellStyle(getContentStyle(p_workbook));
        }
        else
        {
        	cell_A_Project.setCellValue(m_bundle
                    .getString("lb_selected_projects"));
        	cell_A_Project.setCellStyle(getContentStyle(p_workbook));
            Iterator<Long> iter = m_data.projectIdList.iterator();
            int r = 3;
            while (iter.hasNext())
            {
                Long pid = (Long) iter.next();
                String projectName = "??";
                try
                {
                    Project p = ServerProxy.getProjectHandler()
                            .getProjectById(pid.longValue());
                    projectName = p.getName();
                }
                catch (Exception e)
                {
                }
                Row row = getRow(paramsSheet, r);
                Cell cell_A = getCell(row, 0);
                cell_A.setCellValue(projectName);
                cell_A.setCellStyle(getContentStyle(p_workbook));
                
                Cell cell_B = getCell(row, 1);
                cell_B.setCellValue(m_bundle
                        .getString("lb_id") + "=" + pid.toString());
                cell_B.setCellStyle(getContentStyle(p_workbook));
                r++;
            }
        }

        Cell cell_C_Header = getCell(firRow, 2);
        cell_C_Header.setCellValue(m_bundle
                .getString("lb_Year"));
        cell_C_Header.setCellStyle(getContentStyle(p_workbook));
        
        Cell cell_C = getCell(secRow, 2);
        cell_C.setCellValue(year);
        cell_C.setCellStyle(getContentStyle(p_workbook));
        
        Cell cell_D_Header = getCell(firRow, 3);
        cell_D_Header.setCellValue(m_bundle
                .getString("lb_re_cost_jobs"));
        cell_D_Header.setCellStyle(getContentStyle(p_workbook));
        
        Cell cell_D = getCell(secRow, 3);
        cell_D.setCellValue(java.lang.Boolean
                .toString(recalculateFinishedWorkflow));
        cell_D.setCellStyle(getContentStyle(p_workbook));
    }
    
    /**
     * For "Dell Review Report Issue" Add a new parameter "includeExReview" to
     * the signature.
     */
    private HashMap<String, HashMap<String, ProjectWorkflowData>> getProjectDataForMonth(
            HttpServletRequest p_request, int p_month,
            boolean p_recalculateFinishedWorkflow, boolean p_includeExReview)
            throws Exception
    {
    	SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd/yyyy");
        String isReportForYear = p_request.getParameter("reportForThisYear");
        String isReportForDetail = p_request.getParameter("reportForDetail");
        JobSearchParameters searchParams = new JobSearchParameters();
        if (isReportForYear != null && isReportForYear.equals("on"))
        {
            searchParams = getSearchParams(p_request, p_month);
        }
        if (isReportForDetail != null && isReportForDetail.endsWith("on"))
        {
            searchParams.setProjectId(m_data.projectIdList);

            String paramCreateDateStartCount = p_request
                    .getParameter(JobSearchConstants.CREATION_START);
            if (paramCreateDateStartCount != null && paramCreateDateStartCount != "")
            {
            	
                searchParams.setCreationStart(simpleDateFormat.parse(paramCreateDateStartCount));
            }

            String paramCreateDateEndCount = p_request
                    .getParameter(JobSearchConstants.CREATION_END);
           if (paramCreateDateEndCount != null && paramCreateDateEndCount != "")
            {
        	   Date date = simpleDateFormat.parse(paramCreateDateEndCount);
        	   long endLong = date.getTime()+(24*60*60*1000-1);
                searchParams.setCreationEnd(new Date(endLong));
            }

        }

        // initial status parameter
        String[] statusParams = p_request.getParameterValues("status");
        ArrayList<String> statusList = new ArrayList<String>();
        if (statusParams != null)
        {
            boolean allStatus = false;
            for (int i = 0; i < statusParams.length; i++)
            {
                String status = statusParams[i];
                if (status.equals("*") == false)
                {
                    if (Job.PENDING.equals(status))
                    {
                        statusList.add(Job.BATCHRESERVED);
                        statusList.add(Job.PENDING);
                        statusList.add(Job.IMPORTFAILED);
                        statusList.add(Job.ADD_FILE);
                        statusList.add(Job.DELETE_FILE);
                    }
                    else
                    {
                        statusList.add(status);
                    }
                }
                else
                {
                    allStatus = true;
                    break;
                }
            }
            if (!allStatus)
            {
                searchParams.setJobState(statusList);
            }
            else
            {
                ArrayList<String> list = new ArrayList<String>();
                list.add(Job.READY_TO_BE_DISPATCHED);
                list.add(Job.DISPATCHED);
                list.add(Job.LOCALIZED);
                list.add(Job.EXPORTED);
                list.add(Job.EXPORT_FAIL);
                list.add(Job.ARCHIVED);
                list.add(Job.PENDING);
                searchParams.setJobState(list);
            }
        }

        ArrayList<Job> queriedJobs = statusParams == null ? new ArrayList<Job>()
                : new ArrayList<Job>(ServerProxy.getJobHandler().getJobs(
                        searchParams));
        int year = Integer.parseInt(p_request.getParameter("year"));
        ArrayList<Job> wrongJobsThisMonth = getWrongJobsForThisMonth(p_month, year);
        // now create a Set of all the jobs
        HashSet<Job> jobs = new HashSet<Job>();
        jobs.addAll(queriedJobs);
        jobs.addAll(wrongJobsThisMonth);
        jobs.removeAll(m_data.ignoreJobs);

        setJobIds(jobs);
        return reportDataMap(jobs, p_recalculateFinishedWorkflow,
                p_includeExReview);
    }

    /**
     * Prepares the data for generating the report.
     * 
     * @return HashMap<key, HashMap<TargetLocale, ProjectWorkflowData>>. String
     *         key = getMapKey(companyName, projectDesc, jobId);
     */
    private HashMap<String, HashMap<String, ProjectWorkflowData>> reportDataMap(
            HashSet<Job> p_jobs, boolean p_recalculateFinishedWorkflow,
            boolean p_includeExReview) throws CostingException,
            RemoteException, GeneralException
    {
        m_data.useInContext = getUseInContext(p_jobs);

        // first iterate through the Jobs and group by Project/workflow because
        // Dell doesn't want to see actual Jobs
        HashMap<String, HashMap<String, ProjectWorkflowData>> projectMap = new HashMap<String, HashMap<String, ProjectWorkflowData>>();
        Currency pivotCurrency = ServerProxy.getCostingEngine()
                .getCurrencyByName(
                        ReportUtil.getCurrencyName(m_data.getCurrency()),
                        CompanyThreadLocal.getInstance().getValue());

        for (Job j : p_jobs)
        {
            if (isCancelled())
                return new HashMap<String, HashMap<String, ProjectWorkflowData>>();

            L10nProfile l10nprofile = j.getL10nProfile();
            if (!m_data.wantsAllLocProfile
                    && !m_data.locProfileIdList.contains(l10nprofile.getId()))
            {
                continue;
            }
            String l10nProfileName = l10nprofile.getName();
            String fileProfileNames = j.getFProfileNames();

            String projectDesc = getProjectDesc(j);
            String jobId = Long.toString(j.getId());
            String jobName = j.getJobName();
            String companyId = String.valueOf(j.getCompanyId());
            String companyName = CompanyWrapper.getCompanyNameById(companyId);

            List<FileProfile> allFileProfiles = j.getAllFileProfiles();

            // Calculate additional charges
            // GBS-1698, Vincent Yan, 2011/02/21
            Cost jobCost = ServerProxy.getCostingEngine().calculateCost(j,
                    pivotCurrency, true, Cost.REVENUE);

            float totalAdditionalCost = jobCost.getEstimatedCost().getAmount();
            float tmpTotalAdditionalCost = totalAdditionalCost;
            ArrayList<Surcharge> surcharges = new ArrayList<Surcharge>(
                    jobCost.getSurcharges());
            if (surcharges != null && surcharges.size() > 0)
            {
                // calculate total additional charges
                Surcharge surcharge = null;
                FlatSurcharge flatSurcharge = null;
                PercentageSurcharge perSurcharge = null;
                ArrayList<Float> perSurcharges = new ArrayList<Float>();
                // At first, add all flat values to total additional charges and
                // add all percentage values to a tmp list in order to calculate
                // the real value later
                for (int i = 0; i < surcharges.size(); i++)
                {
                    surcharge = (Surcharge) surcharges.get(i);
                    if ("FlatSurcharge".equals(surcharge.getType()))
                    {
                        flatSurcharge = (FlatSurcharge) surcharge;
                        totalAdditionalCost += flatSurcharge.getAmount()
                                .getAmount();
                    }
                    else if ("PercentageSurcharge".equals(surcharge.getType()))
                    {
                        perSurcharge = (PercentageSurcharge) surcharge;
                        perSurcharges.add(Float.valueOf(perSurcharge
                                .getPercentage()));
                    }
                }

                // Get percentage additional charges
                float tmpPercentCost = totalAdditionalCost;
                for (Float f : perSurcharges)
                {
                    totalAdditionalCost += tmpPercentCost * f.floatValue();
                }
            }

            // reduce the base estimated cost to get total additional charge
            // cost only
            totalAdditionalCost -= tmpTotalAdditionalCost;

            boolean isInContextMatch = PageHandler.isInContextMatch(j);
            HashMap<Long, Cost> workflowMap = jobCost.getWorkflowCost();
            for (Workflow w : j.getWorkflows())
            {
                String state = w.getState();
                // skip certain workflows
                if (Workflow.IMPORT_FAILED.equals(w.getState())
                        || Workflow.CANCELLED.equals(w.getState())
                        || Workflow.BATCHRESERVED.equals(w.getState()))
                {
                    continue;
                }
                String targetLang = Long.toString(w.getTargetLocale().getId());
                if (!m_data.wantsAllLocales
                        && !m_data.trgLocaleList.contains(targetLang))
                {
                    continue;
                }

                String key = getMapKey(companyName, projectDesc, jobId);
                HashMap<String, ProjectWorkflowData> localeMap = projectMap
                        .get(key);
                if (localeMap == null)
                {
                    localeMap = new HashMap<String, ProjectWorkflowData>();
                    projectMap.put(key, localeMap);
                }

                ProjectWorkflowData data = new ProjectWorkflowData();
                // For "Job id not showing in external review tabs" Issue
                data.jobId = jobId;
                data.jobName = jobName;
                data.projectDesc = projectDesc;
                data.companyName = companyName;
                data.allFileProfiles = allFileProfiles;
                // the property is as same as jobName one to one
                data.l10nProfileName = l10nProfileName;
                data.fileProfileNames = fileProfileNames;
                data.targetLang = w.getTargetLocale().toString();
                data.creationDate = j.getCreateDate();
                data.status = j.getDisplayState();
                if (Workflow.EXPORTED.equals(state))
                {
                    data.wasExported = true;
                    data.exportDate = getExportDate(w.getTargetLocale().toString(), 
                    		jobName ,jobId);
                }
                if (Workflow.EXPORT_FAILED.equals(state))
                {
                    // if the workflow is EXPORT_FAILED, color the line in red
                    data.wasExportFailed = true;
                    data.exportDate = getExportDate(w.getTargetLocale().toString(), 
                    		jobName ,jobId);
                }

                // now add or amend the data in the ProjectWorkflowData based on
                // this next workflow
                if (j.getCreateDate().before(data.creationDate))
                    data.creationDate = j.getCreateDate();

                data.repetitionWordCount = w.getRepetitionWordCount();
                data.lowFuzzyMatchWordCount = w.getThresholdLowFuzzyWordCount();
                data.medFuzzyMatchWordCount = w.getThresholdMedFuzzyWordCount();
                data.medHiFuzzyMatchWordCount = w
                        .getThresholdMedHiFuzzyWordCount();
                data.hiFuzzyMatchWordCount = w.getThresholdHiFuzzyWordCount();

                // the fuzzyMatchWordCount is the sum of the top 3 categories
                data.fuzzyMatchWordCount = data.medFuzzyMatchWordCount
                        + data.medHiFuzzyMatchWordCount
                        + data.hiFuzzyMatchWordCount;

                // add the lowest fuzzies and sublev match to nomatch
                data.noMatchWordCount = w.getThresholdNoMatchWordCount()
                        + w.getThresholdLowFuzzyWordCount();

                data.segmentTmWordCount = (isInContextMatch) ? w
                        .getSegmentTmWordCount() : w.getTotalExactMatchWordCount();
                data.contextMatchWordCount = 0;
                data.inContextMatchWordCount = (isInContextMatch) ? w
                        .getInContextMatchWordCount() : w
                        .getNoUseInContextMatchWordCount();
                data.totalWordCount = w.getTotalWordCount();
                /*
                 * Date da1 = new Date(); // They must be in front of calculate
                 * cost for activity // "Dell_Review".
                 * 
                 * Cost wfCost = ServerProxy.getCostingEngine().calculateCost(w,
                 * pivotCurrency, p_recalculateFinishedWorkflow, Cost.REVENUE,
                 * p_recalculateFinishedWorkflow); Date da2 = new Date();
                 * 
                 * ccc = ccc + (da2.getTime() - da1.getTime());
                 * 
                 * CostByWordCount costByWordCount =
                 * wfCost.getCostByWordCount();
                 */
                Cost wfCost = workflowMap.get(w.getId());
                CostByWordCount costByWordCount = null;

                if (wfCost != null)
                    costByWordCount = wfCost.getCostByWordCount();

                // For "Dell Review Report Issue".
                // Calculates the cost for activity named "Dell_Review".
                if (p_includeExReview)
                {
                    Cost dellReviewCost = null;

                    Iterator<?> it = w.getTasks().values().iterator();
                    Task task = null;
                    while (it.hasNext())
                    {
                        task = (Task) it.next();
                        Cost cost = null;
                        // For "Job id not showing in external review tabs"
                        // Issue
                        SystemConfiguration config = SystemConfiguration
                                .getInstance();
                        String reportActivityName = config
                                .getStringParameter(SystemConfigParamNames.REPORTS_ACTIVITY);
                        if (reportActivityName != null
                                && !"".equals(reportActivityName))
                        {
                            DELL_REVIEW = reportActivityName;
                        }
                        if (DELL_REVIEW.equals(task.getTaskDisplayName()))
                        {
                            data.containsDellReview = true;
                            /*
                             * cost = ServerProxy.getCostingEngine()
                             * .calculateCost(task, pivotCurrency,
                             * p_recalculateFinishedWorkflow, Cost.REVENUE);
                             */
                            cost = wfCost.getTaskCost().get(task.getId());

                            if (dellReviewCost == null)
                            {
                                dellReviewCost = cost;
                            }
                            else
                            {
                                dellReviewCost.add(cost);
                            }
                        }
                    }

                    CostByWordCount dellReviewCostByWordCount = null;
                    if (data.containsDellReview)
                    {
                        dellReviewCostByWordCount = dellReviewCost
                                .getCostByWordCount();
                    }
                    if (dellReviewCostByWordCount != null)
                    {
                        data.repetitionWordCountCostForDellReview = add(
                                data.repetitionWordCountCostForDellReview,
                                dellReviewCostByWordCount.getRepetitionCost());
                        data.noMatchWordCountCostForDellReview = add(
                                data.noMatchWordCountCostForDellReview,
                                dellReviewCostByWordCount.getNoMatchCost());
                        data.lowFuzzyMatchWordCountCostForDellReview = add(
                                data.lowFuzzyMatchWordCountCostForDellReview,
                                dellReviewCostByWordCount
                                        .getLowFuzzyMatchCost());
                        data.medFuzzyMatchWordCountCostForDellReview = add(
                                data.medFuzzyMatchWordCountCostForDellReview,
                                dellReviewCostByWordCount
                                        .getMedFuzzyMatchCost());
                        data.medHiFuzzyMatchWordCountCostForDellReview = add(
                                data.medHiFuzzyMatchWordCountCostForDellReview,
                                dellReviewCostByWordCount
                                        .getMedHiFuzzyMatchCost());
                        data.hiFuzzyMatchWordCountCostForDellReview = add(
                                data.hiFuzzyMatchWordCountCostForDellReview,
                                dellReviewCostByWordCount.getHiFuzzyMatchCost());
                        data.segmentTmWordCountCostForDellReview = add(
                                data.segmentTmWordCountCostForDellReview,
                                (isInContextMatch) ? dellReviewCostByWordCount.getSegmentTmMatchCost()
                                        : dellReviewCostByWordCount.getNoUseExactMatchCost());
                        data.contextMatchWordCountCostForDellReview = add(
                                data.contextMatchWordCountCostForDellReview, 0);
                        data.inContextMatchWordCountCostForDellReview = add(
                                data.inContextMatchWordCountCostForDellReview,
                                (isInContextMatch) ? dellReviewCostByWordCount
                                        .getInContextMatchCost()
                                        : dellReviewCostByWordCount
                                                .getNoUseInContextMatchCost());

                        // fuzzy match cost is the sum of the top three fuzzy
                        // match categories
                        data.fuzzyMatchWordCountCostForDellReview = data.medFuzzyMatchWordCountCostForDellReview
                                .add(data.medHiFuzzyMatchWordCountCostForDellReview)
                                .add(data.hiFuzzyMatchWordCountCostForDellReview);

                        // this means that no match cost should include the
                        // lowest fuzzy match category
                        data.noMatchWordCountCostForDellReview = data.noMatchWordCountCostForDellReview
                                .add(data.lowFuzzyMatchWordCountCostForDellReview);

                        data.totalWordCountCostForDellReview = data.repetitionWordCountCostForDellReview
                                .add(data.noMatchWordCountCostForDellReview)
                                .add(data.fuzzyMatchWordCountCostForDellReview)
                                .add(data.segmentTmWordCountCostForDellReview)
                                .add(data.contextMatchWordCountCostForDellReview)
                                .add(data.inContextMatchWordCountCostForDellReview);
                    }
                }

                if (costByWordCount != null)
                {
                    data.repetitionWordCountCost = BigDecimalHelper.subtract(
                            costByWordCount.getRepetitionCost(),
                            data.repetitionWordCountCostForDellReview);
                    data.noMatchWordCountCost = BigDecimalHelper.subtract(
                            costByWordCount.getNoMatchCost(),
                            data.noMatchWordCountCostForDellReview);
                    data.lowFuzzyMatchWordCountCost = BigDecimalHelper
                            .subtract(
                                    costByWordCount.getLowFuzzyMatchCost(),
                                    data.lowFuzzyMatchWordCountCostForDellReview);
                    data.medFuzzyMatchWordCountCost = BigDecimalHelper
                            .subtract(
                                    costByWordCount.getMedFuzzyMatchCost(),
                                    data.medFuzzyMatchWordCountCostForDellReview);
                    data.medHiFuzzyMatchWordCountCost = BigDecimalHelper
                            .subtract(
                                    costByWordCount.getMedHiFuzzyMatchCost(),
                                    data.medHiFuzzyMatchWordCountCostForDellReview);
                    data.hiFuzzyMatchWordCountCost = BigDecimalHelper.subtract(
                            costByWordCount.getHiFuzzyMatchCost(),
                            data.hiFuzzyMatchWordCountCostForDellReview);
                    data.segmentTmWordCountCost = BigDecimalHelper.subtract(
                            (isInContextMatch) ? costByWordCount.getSegmentTmMatchCost()
                                    : costByWordCount.getNoUseExactMatchCost(),
                            data.segmentTmWordCountCostForDellReview);
                    data.contextMatchWordCountCost = BigDecimalHelper.subtract(
                            0, data.contextMatchWordCountCostForDellReview);
                    data.inContextMatchWordCountCost = BigDecimalHelper
                            .subtract(
                                    (isInContextMatch) ? costByWordCount
                                            .getInContextMatchCost()
                                            : costByWordCount
                                                    .getNoUseInContextMatchCost(),
                                    data.inContextMatchWordCountCostForDellReview);

                    // fuzzy match cost is the sum of the top three fuzzy match
                    // categories
                    data.fuzzyMatchWordCountCost = data.medFuzzyMatchWordCountCost
                            .add(data.medHiFuzzyMatchWordCountCost).add(
                                    data.hiFuzzyMatchWordCountCost);

                    // this means that no match cost should include the lowest
                    // fuzzy match category
                    data.noMatchWordCountCost = data.noMatchWordCountCost
                            .add(data.lowFuzzyMatchWordCountCost);

                    data.totalWordCountCost = data.repetitionWordCountCost
                            .add(data.noMatchWordCountCost)
                            .add(data.fuzzyMatchWordCountCost)
                            .add(data.segmentTmWordCountCost)
                            .add(data.contextMatchWordCountCost)
                            .add(data.inContextMatchWordCountCost);
                }

                data.totalAdditionalCost = add(data.totalAdditionalCost,
                        totalAdditionalCost);

                localeMap.put(targetLang, data);
            }

            // now recalculate the job level cost if the workflow was
            // recalculated
            if (p_recalculateFinishedWorkflow)
            {
                ServerProxy.getCostingEngine().reCostJob(j, pivotCurrency,
                        Cost.REVENUE);
            }
        }

        return projectMap;
    }

    /*
     * Create the key for sorting the data by key.
     */
    private String getMapKey(String p_companyName, String p_projectDesc,
            String p_jobId)
    {
        return p_companyName + SEPARATOR + p_projectDesc + SEPARATOR + p_jobId;
    }

    /*
     * Get the jobId from the key
     * 
     * @see getMapKey
     */
    private String getJobIdFromMapKey(String p_key)
    {
        return p_key.substring(p_key.lastIndexOf(SEPARATOR)
                + SEPARATOR.length());
    }

    private String[] getMonths()
    {
        String[] months = new String[12];
        months[0] = m_bundle.getString("lb_january");
        months[1] = m_bundle.getString("lb_february");
        months[2] = m_bundle.getString("lb_march");
        months[3] = m_bundle.getString("lb_april");
        months[4] = m_bundle.getString("lb_may");
        months[5] = m_bundle.getString("lb_june");
        months[6] = m_bundle.getString("lb_july");
        months[7] = m_bundle.getString("lb_august");
        months[8] = m_bundle.getString("lb_september");
        months[9] = m_bundle.getString("lb_october");
        months[10] = m_bundle.getString("lb_november");
        months[11] = m_bundle.getString("lb_december");

        return months;
    }

    private Date getExportDate(String targetLang, String jobName, String jobId)
    {
    	try 
    	{
			Job job = ServerProxy.getJobHandler().getJobById(Long.parseLong(jobId));
			String cxePath = AmbFileStoragePathUtils.getCxeDocDirPath(job.getCompanyId());
			String path = cxePath + "/" + targetLang + "/" + jobName;
			
			File file = new File(path);
			if (!file.exists())
			{
				String newPath = cxePath + "/" + targetLang + "/webservice/"
				+ jobName;
				file = new File(newPath);
			}
			
			if (!file.exists())
			{
				String newPath =cxePath + "/" + targetLang + "/" + jobId;
				file = new File(newPath);
			}
			
			if (!file.exists())
			{
				String newPath = cxePath + "/" + targetLang + "/webservice/"
				+ jobId;
				file = new File(newPath);
			}
			
			
			if (file.exists() && file.isDirectory())
			{
				File[] files = file.listFiles();
				if (files.length > 0)
				{
					file = files[0];
				}
			}
			
			return file.exists() ? new Date(file.lastModified()) : null;
		} 
    	catch (Exception e) 
		{
    		logger.error("Cannot find job by job id [" + jobId + "]", e);
		}
    	return null;
    }

    /**
     * Class used to group data by project and target language
     */
    private class ProjectWorkflowData
    {
        public String l10nProfileName;
        public String fileProfileNames;

        // For "Job id not showing in external review tabs" Issue
        public String jobId;

        public String jobName;

        public String projectDesc;

        public String companyName;

        public List<FileProfile> allFileProfiles;

        public Date creationDate; // earliest job creation date this month

        public String status;

        public Date exportDate;

        public String targetLang;

        /* word counts */
        public long repetitionWordCount = 0;

        public long lowFuzzyMatchWordCount = 0;

        public long medFuzzyMatchWordCount = 0;

        public long medHiFuzzyMatchWordCount = 0;

        public long hiFuzzyMatchWordCount = 0;

        public long contextMatchWordCount = 0;

        public long inContextMatchWordCount = 0;

        public long segmentTmWordCount = 0;

        public long noMatchWordCount = 0;

        public long totalWordCount = 0;

        // Dell wants to see anything 75% and above as a fuzzy match
        // so this is the sum of all except the lowest band
        public long fuzzyMatchWordCount = 0;

        /* word count costs */
        public BigDecimal fuzzyMatchWordCountCost = new BigDecimal(
                BIG_DECIMAL_ZERO_STRING);

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

        public BigDecimal totalAdditionalCost = new BigDecimal(
                BIG_DECIMAL_ZERO_STRING);

        public boolean containsDellReview = false;

        /* Word count costs for activity named "Dell_Review" */
        public BigDecimal fuzzyMatchWordCountCostForDellReview = new BigDecimal(
                BIG_DECIMAL_ZERO_STRING);

        public BigDecimal repetitionWordCountCostForDellReview = new BigDecimal(
                BIG_DECIMAL_ZERO_STRING);

        public BigDecimal lowFuzzyMatchWordCountCostForDellReview = new BigDecimal(
                BIG_DECIMAL_ZERO_STRING);

        public BigDecimal medFuzzyMatchWordCountCostForDellReview = new BigDecimal(
                BIG_DECIMAL_ZERO_STRING);

        public BigDecimal medHiFuzzyMatchWordCountCostForDellReview = new BigDecimal(
                BIG_DECIMAL_ZERO_STRING);

        public BigDecimal hiFuzzyMatchWordCountCostForDellReview = new BigDecimal(
                BIG_DECIMAL_ZERO_STRING);

        public BigDecimal contextMatchWordCountCostForDellReview = new BigDecimal(
                BIG_DECIMAL_ZERO_STRING);

        public BigDecimal inContextMatchWordCountCostForDellReview = new BigDecimal(
                BIG_DECIMAL_ZERO_STRING);

        public BigDecimal segmentTmWordCountCostForDellReview = new BigDecimal(
                BIG_DECIMAL_ZERO_STRING);

        public BigDecimal noMatchWordCountCostForDellReview = new BigDecimal(
                BIG_DECIMAL_ZERO_STRING);

        public BigDecimal totalWordCountCostForDellReview = new BigDecimal(
                BIG_DECIMAL_ZERO_STRING);

        public boolean wasExportFailed = false;

        public boolean wasExported = false;

        public ProjectWorkflowData()
        {
        }
    }

    private class MyData
    {
        public boolean wantsAllProjects = false;

        public boolean wantsAllLocProfile = true;

        boolean wantsAllLocales = false;

        private boolean tradosStyle = true;

        private String currency = "US Dollar";

        HashSet<String> trgLocaleList = new HashSet<String>();

        ArrayList<Long> projectIdList = new ArrayList<Long>();

        ArrayList<Long> locProfileIdList = new ArrayList<Long>();

        Hashtable<Long, Project> wrongJobMap = new Hashtable<Long, Project>(); // maps

        // jobs
        // to
        // new
        // project

        boolean warnedAboutMissingWrongJobsFile = false;

        ArrayList<Job> wrongJobs = new ArrayList<Job>();

        ArrayList<String> wrongJobNames = new ArrayList<String>();

        String dateFormatString = "MM/dd/yy hh:mm:ss a z";

        // wrong jobs which may be queried for the project which should be
        // ignored
        ArrayList<Job> ignoreJobs = new ArrayList<Job>();

        boolean useInContext = false;

        // Summary Start column
        int sumStartCol = 0;
        List<Long> jobIds;

        public int getSumStartCol()
        {
            return sumStartCol;
        }

        public void initSumStartCol(int p_col)
        {
            if (sumStartCol == 0)
                sumStartCol = p_col;
        }

        public String getDateFormatString()
        {
            return dateFormatString;
        }

        public void setDateFormatString(String dateFormatString)
        {
            this.dateFormatString = dateFormatString;
        }

        public boolean isTradosStyle()
        {
            return tradosStyle;
        }

        public void setTradosStyle(boolean tradosStyle)
        {
            this.tradosStyle = tradosStyle;
        }

        public void setTradosStyle(String tradosStyleStr)
        {
            if ("trados".equalsIgnoreCase(tradosStyleStr))
            {
                this.tradosStyle = true;
            }
            else
            {
                this.tradosStyle = false;
            }
        }

        public String getCurrency()
        {
            return currency;
        }

        public void setCurrency(String currency)
        {
            if (currency != null && currency.trim().length() > 0)
                this.currency = currency;
        }
    };

    public MyData getMydata()
    {
        return m_data;
    }

    public void setMydata(MyData m_data)
    {
        this.m_data = m_data;
    }

    private void setProjectIdList(HttpServletRequest p_request)
    {
        // set the project Id
        String[] projectIds = p_request.getParameterValues("projectId");

        if (projectIds != null)
        {
            for (int i = 0; i < projectIds.length; i++)
            {
                String id = projectIds[i];
                if (id.equals("*") == false)
                {
                    m_data.projectIdList.add(new Long(id));
                }
                else
                {
                    m_data.wantsAllProjects = true;
                    try
                    {
                        List<Project> projectList = (ArrayList<Project>) ServerProxy
                                .getProjectHandler()
                                .getProjectsByUser(m_userId);
                        for (Project project : projectList)
                        {
                            m_data.projectIdList.add(project.getIdAsLong());
                        }
                        break;
                    }
                    catch (Exception e)
                    {
                    }
                }
            }
        }
    }

    private void setLocpIdList(HttpServletRequest p_request)
    {
        // set the project Id
        String[] locprofiles = p_request.getParameterValues("locprofile");

        if (locprofiles != null)
        {
            for (int i = 0; i < locprofiles.length; i++)
            {
                String ids = locprofiles[i];
                if (ids.equals("*") == false)
                {
                    m_data.wantsAllLocProfile = false;
                    // Long p_id = new Long(id);
                    // Collection<? extends Long> c = ServerProxy
                    // .getFileProfilePersistenceManager()
                    // .readFileProfileBy(p_id);
                    String[] locproids = ids.split(",");
                    for (int j = 0; j < locproids.length; j++)
                    {
                        String id = locproids[j];
                        m_data.locProfileIdList.add(new Long(id));
                    }

                }
            }
        }
    }

    private void setTargetLocales(HttpServletRequest p_request)
    {
        String[] paramTrgLocales = p_request
                .getParameterValues(ReportConstants.TARGETLOCALE_LIST);
        if (paramTrgLocales != null)
        {
            for (int i = 0; i < paramTrgLocales.length; i++)
            {
                if ("*".equals(paramTrgLocales[i]))
                {
                    m_data.wantsAllLocales = true;
                    break;
                }
                else
                {
                    m_data.trgLocaleList.add(paramTrgLocales[i]);
                }
            }
        }
    }

    /**
     * Returns search params used to find the jobs based on state
     * (READY,EXPORTED,LOCALIZED,DISPATCHED), and creation date during the
     * particular month.
     * 
     * @return JobSearchParams
     */
    private JobSearchParameters getSearchParams(HttpServletRequest p_request,
            int p_month)
    {
        JobSearchParameters sp = new JobSearchParameters();
        
        sp.setProjectId(m_data.projectIdList);

        // job state EXPORTED,DISPATCHED,LOCALIZED
        ArrayList<String> list = new ArrayList<String>();
        list.add(Job.READY_TO_BE_DISPATCHED);
        list.add(Job.DISPATCHED);
        list.add(Job.LOCALIZED);
        list.add(Job.EXPORTED);
        list.add(Job.EXPORT_FAIL);
        list.add(Job.PENDING);
        list.add(Job.ARCHIVED);
        sp.setJobState(list);

        // find out which year they want to run the report for
        int year = Integer.parseInt(p_request.getParameter("year"));

        // find the first day of the month
        GregorianCalendar gcal = new GregorianCalendar(year, p_month, 1);
        gcal.setLenient(true);
        Date firstOfTheMonth = gcal.getTime();
        gcal.add(Calendar.MONTH, 1);
        Date firstOfNextMonth = gcal.getTime();
        sp.setCreationStart(firstOfTheMonth);
        sp.setCreationEnd(firstOfNextMonth);
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
    private void getJobsInWrongProject()
    {
        try
        {
            m_data.wrongJobs.addAll(readJobNames());
            for(Job j: m_data.wrongJobs)
            {
                // p_data.wrongJobNames.add(j.getJobName());
                m_data.wrongJobNames.add(Long.toString(j.getId()));
            }
        }
        catch (Exception e)
        {
            logger.error(
                    "Failed to add jobs which are in the 'wrong' project.", e);
        }
    }

    /**
     * Opens up the file "jobsInWrongDivision.txt" and returns only the jobs
     * that should be mapped to the requested projects
     */
    private ArrayList<Job> readJobNames() throws Exception
    {
        ArrayList<Job> wrongJobs = new ArrayList<Job>();
        SystemConfiguration sc = SystemConfiguration.getInstance();
        StringBuffer mapFile = new StringBuffer(
                sc.getStringParameter(SystemConfigParamNames.GLOBALSIGHT_HOME_DIRECTORY));
        mapFile.append(File.separator);
        mapFile.append("jobsInWrongDivision.txt");
        File f = new File(mapFile.toString());
        if (f.exists() == false)
        {
            if (m_data.warnedAboutMissingWrongJobsFile == false)
            {
                logger.info("jobsInWrongDivision.txt file not found.");
                m_data.warnedAboutMissingWrongJobsFile = true;
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
                ArrayList<Job> jobs = new ArrayList<Job>(ServerProxy
                        .getJobHandler().getJobs(sp));
                Job j = (Job) jobs.get(0);

                if (m_data.wantsAllProjects == false
                        && m_data.projectIdList.contains(newProjectId) == false)
                {
                	m_data.ignoreJobs.add(j);
                    continue;
                }

                // but if the wrong job is actually currently in a project we
                // are reporting on, then skip it as well
                // if the project id list contains the old project but not the
                // new project
                Long oldProjectId = j.getL10nProfile().getProject()
                        .getIdAsLong();
                if (m_data.projectIdList.contains(oldProjectId)
                        && !m_data.projectIdList.contains(newProjectId))
                {
                	m_data.ignoreJobs.add(j);
                    continue;
                }

                // add the Job to the job list, and add the mapping for job id
                // to project
                wrongJobs.add(j);
                m_data.wrongJobMap.put(new Long(j.getId()), p);
            }
            catch (Exception e)
            {
                logger.warn("Ignoring mapping line for "
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
    private String getProjectDesc(Job p_job)
    {
        Project p = (Project) m_data.wrongJobMap.get(new Long(p_job.getId()));
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

    private ArrayList<Job> getWrongJobsForThisMonth(int p_month,
            int p_year)
    {
        ArrayList<Job> wrongJobsThisMonth = new ArrayList<Job>();
        Calendar cal = Calendar.getInstance();
        for(Job j: m_data.wrongJobs)
        {
            cal.setTime(j.getCreateDate());
            if (p_month == cal.get(Calendar.MONTH)
                    && p_year == cal.get(Calendar.YEAR))
            {
                wrongJobsThisMonth.add(j);
            }
        }
        return wrongJobsThisMonth;
    }

    /**
     * For "Add Job Id into online job report" issue set the value of
     * "isJobIdVisible" for control display the job id whether or not.
     */
    private boolean isJobIdVisible(HttpServletRequest request)
    {
        String requestJobIdVisible = request.getParameter("jobIdVisible");
        boolean jobIdVisible = false;
        if (requestJobIdVisible != null && requestJobIdVisible.equals("true"))
        {
            jobIdVisible = true;
        }
        return jobIdVisible;
    }

    private boolean getUseInContext(HashSet<Job> jobs)
    {
        for (Job job : jobs)
        {
            if (PageHandler.isInContextMatch(job))
            {
                return true;
            }
        }

        return false;
    }

    private String getCurrencyNumberFormat()
    {
        return symbol + "###,###,##0.000;(" + symbol + "###,###,##0.000)";
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

    private String getAllSouceFileFormats(List<FileProfile> p_fps)
    {
        Set<String> result = new HashSet<String>();
        KnownFormatType type = null;
        for (FileProfile fp : p_fps)
        {
            long id = fp.getKnownFormatTypeId();
            try
            {
                type = ServerProxy.getFileProfilePersistenceManager()
                        .queryKnownFormatType(id);
            }
            catch (Exception e)
            {
                logger.error("Can't get KnownFormatType in Online Jobs Report",
                        e);
            }

            if (type != null && !type.getName().isEmpty())
            {
                result.add(type.getName());
            }
        }

        String resultStr = result.toString();
        return resultStr.substring(1, resultStr.length() - 1);
    }

    private void setJobIds(HashSet<Job> jobs)
    {
        if (m_data != null
                && (m_data.jobIds == null || m_data.jobIds.size() == 0))
        {
            m_data.jobIds = new ArrayList();
            for (Job job : jobs)
            {
                m_data.jobIds.add(job.getJobId());
            }
        }
    }

    @Override
    public String getReportType()
    {
        return ReportConstants.ONLINE_JOBS_REPORT;
    }

    @Override
    public void setPercent(int p_finishedJobNum)
    {
        ReportGeneratorHandler.setReportsMapByGenerator(m_userId,
                m_data.jobIds, 100 * p_finishedJobNum / m_data.jobIds.size(),
                getReportType());
    }

    @Override
    public boolean isCancelled()
    {
        return ReportGeneratorHandler.isCancelled(m_userId, null,
                getReportType());
    }

    private String getExportDateStr(SimpleDateFormat sdf, Date exportDate)
    {
        if (exportDate == null)
        {
            return "";
        }
        else
        {
            return sdf.format(exportDate);
        }
    }
    
    private CellStyle getHeaderStyle(Workbook p_workbook){
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
    	if(dateStyle == null)
    	{   		
    		DataFormat dataFormat = p_workbook.createDataFormat();
    		
    		Font dateFont = p_workbook.createFont();
            dateFont.setFontName("Arial");
            dateFont.setFontHeightInPoints((short) 10);
    		
    		CellStyle cs = p_workbook.createCellStyle();
    		cs.setWrapText(false);
    		cs.setFont(dateFont);
    		cs.setDataFormat(dataFormat.getFormat("M/d/yy"));
            
            dateStyle = cs;
    	}
    	return dateStyle;
    }
    
    private CellStyle getMoneyStyle(Workbook p_workbook) throws Exception
    {
    	if(moneyStyle == null){  		
    		String euroJavaNumberFormat = getCurrencyNumberFormat();
    		DataFormat euroNumberFormat = p_workbook.createDataFormat();
    		
    		CellStyle cs = p_workbook.createCellStyle();
    		cs.setDataFormat(euroNumberFormat.getFormat(euroJavaNumberFormat));
    		cs.setWrapText(false);
    		
    		moneyStyle = cs;
    	}

        return moneyStyle;
    }
    
    private CellStyle getRedCellStyle(Workbook p_workbook) throws Exception
    {
    	if(redCellStyle == null)
    	{   		
    		Font redFont = p_workbook.createFont();
    		redFont.setFontName("Arial");
    		redFont.setUnderline(Font.U_NONE);
    		redFont.setFontHeightInPoints((short) 10);
	    	
	    	CellStyle cs = p_workbook.createCellStyle();
	    	cs.setFont(redFont);
	    	cs.setWrapText(true);
	    	cs.setFillPattern(CellStyle.SOLID_FOREGROUND );
	    	cs.setFillForegroundColor(IndexedColors.RED.getIndex());
            
            redCellStyle = cs;
    	}
    	return redCellStyle;
    }
    
    private CellStyle getFailedDateStyle(Workbook p_workbook) throws Exception
    {
    	if(failedDateStyle == null)
    	{
    		DataFormat dataFormat = p_workbook.createDataFormat();
    		
    		Font dateFont = p_workbook.createFont();
            dateFont.setFontName("Arial");
            dateFont.setFontHeightInPoints((short) 10);
    		
    		CellStyle cs = p_workbook.createCellStyle();
    		cs.setWrapText(false);
    		cs.setFont(dateFont);
    		cs.setDataFormat(dataFormat.getFormat("M/d/yy"));
    		cs.setFillPattern(CellStyle.SOLID_FOREGROUND );
    		cs.setFillForegroundColor(IndexedColors.RED.getIndex()); 
    		failedDateStyle = cs;
    	}
        return failedDateStyle;
    }
    
    private CellStyle getFailedTimeStyle(Workbook p_workbook) throws Exception
    {
    	if(failedTimeStyle == null)
    	{
    		DataFormat dataFormat = p_workbook.createDataFormat();
    		
    		CellStyle cs = p_workbook.createCellStyle();
    		cs.setWrapText(false);
    		cs.setDataFormat(dataFormat.getFormat("h:mm:ss AM/PM"));
    		cs.setFillPattern(CellStyle.SOLID_FOREGROUND );
    		cs.setFillForegroundColor(IndexedColors.RED.getIndex()); 
    		failedTimeStyle = cs;
    	}
        return failedTimeStyle;
    }
    
    private CellStyle getFailedMoneyStyle(Workbook p_workbook) throws Exception
    {
    	if(failedMoneyStyle == null)
    	{
    		String euroJavaNumberFormat = getCurrencyNumberFormat();
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
    		String euroJavaNumberFormat = getCurrencyNumberFormat();
    		DataFormat euroNumberFormat = p_workbook.createDataFormat();
    		
    		CellStyle cs = p_workbook.createCellStyle();
    		cs.setDataFormat(euroNumberFormat.getFormat(euroJavaNumberFormat));
    		cs.setWrapText(false);
    		cs.setFillPattern(CellStyle.SOLID_FOREGROUND );
            cs.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            cs.setBorderTop(CellStyle.BORDER_THIN);
            cs.setBorderBottom(CellStyle.BORDER_THIN);
            
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
    
    private SimpleDateFormat getDateFormat()
    {
    	if(dateFormat == null)
    	{
	    	String datetimeFormatString = m_data.getDateFormatString();
	        String dateFormatString = datetimeFormatString.substring(0,
	                datetimeFormatString.indexOf(" ") - 1);
	        SimpleDateFormat format = new SimpleDateFormat(dateFormatString);
	        
	        dateFormat = format;
    	}
    	return dateFormat;
    }
    
    private SimpleDateFormat getTimeFormat()
    {
    	if(timeFormat == null)
    	{
    		String datetimeFormatString = m_data.getDateFormatString();
    		String timeFormatString = datetimeFormatString.substring(
    				datetimeFormatString.indexOf(" ") + 1,
    				datetimeFormatString.length());
    		SimpleDateFormat format = new SimpleDateFormat(timeFormatString);
    		
    		timeFormat = format;
    	}
    	return timeFormat;
    }
}