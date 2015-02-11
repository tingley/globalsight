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
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeSet;

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
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.util.comparator.GlobalSightLocaleComparator;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.pagehandler.administration.company.Select;
import com.globalsight.everest.webapp.pagehandler.administration.reports.ReportConstants;
import com.globalsight.everest.webapp.pagehandler.administration.reports.ReportHelper;
import com.globalsight.everest.webapp.pagehandler.administration.reports.bo.ReportsData;
import com.globalsight.everest.webapp.pagehandler.administration.users.UserUtil;
import com.globalsight.everest.workflow.ScorecardScore;
import com.globalsight.everest.workflow.ScorecardScoreHelper;
import com.globalsight.everest.workflowmanager.Workflow;
import com.globalsight.everest.workflowmanager.WorkflowImpl;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.edit.EditUtil;

/**
 * Comments Analysis Report Generator Include Comments Analysis Report in popup
 * editor
 */
public class ScorecardReportGenerator implements ReportGenerator
{
    private static final Logger logger = Logger
            .getLogger(ScorecardReportGenerator.class);
    private HttpServletRequest request = null;
    private ResourceBundle bundle = null;

    private CellStyle titleStyle = null;
    private CellStyle contentStyle = null;
    private CellStyle rtlContentStyle = null;
    private CellStyle headerStyle = null;
    private CellStyle unlockedStyle = null;
    private CellStyle unlockedRightStyle = null;
    private CellStyle under3Style = null;
    private CellStyle under4Style = null;
    private CellStyle under5Style = null;
    private CellStyle equal5Style = null;

    protected String m_companyName = "";
    protected List<Long> m_jobIDS = new ArrayList<Long>();
    protected List<GlobalSightLocale> m_targetLocales = new ArrayList<GlobalSightLocale>();
    protected List<Select> categoryList = new ArrayList<Select>();

    public static final String reportType = ReportConstants.SCORECARD_REPORT;

    public static final int SEGMENT_HEADER_ROW = 2;
    public static final int SEGMENT_START_ROW = 3;
    
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
    public ScorecardReportGenerator(HttpServletRequest p_request,
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
        categoryList = initSelectList(
    			getScorecardCategories(CompanyThreadLocal.getInstance().getValue()),
    			PageHandler.getBundle(p_request.getSession()));
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
        Workbook workBook = new SXSSFWorkbook();

    	createReport(workBook, p_jobIDS, p_targetLocales, stateList);
        
    	File file = getFile(reportType, null, workBook);
        FileOutputStream out = new FileOutputStream(file);
        workBook.write(out);
        out.close();
        ((SXSSFWorkbook)workBook).dispose();
        workBooks.add(file);

        return ReportHelper.moveReports(workBooks, m_userId);
    }

    /**
     * Create the report sheets
     * 
     * @throws Exception
     */
    private void createReport(Workbook p_workbook, List<Long> p_jobIds,
            List<GlobalSightLocale> p_targetLocales, ArrayList<String> stateList)
            throws Exception
    {
    	Sheet sheet = p_workbook.createSheet("Scorecard");
    	// Add Title
        addTitle(p_workbook, sheet);
        // Add Segment Header
        addSegmentHeader(p_workbook, sheet);
        
        HashMap<String, HashMap<String, Integer>> socreSum = 
        		new HashMap<String, HashMap<String, Integer>>();
        HashMap<String, HashMap<String, Integer>> socreNum = 
    		new HashMap<String, HashMap<String, Integer>>();
        List<GlobalSightLocale> sortLocales = sortLocales(p_targetLocales);
        for(GlobalSightLocale locale: sortLocales)
        {
        	HashMap<String, Integer> localeSum = new HashMap<String, Integer>();
        	HashMap<String, Integer> localeNum = new HashMap<String, Integer>();
        	for(Select category: categoryList)
        	{
        		localeSum.put(category.getValue(), 0);
        		localeNum.put(category.getValue(), 0);
        	}
        	socreSum.put(locale.toString(), localeSum);
        	socreNum.put(locale.toString(), localeNum);
        }
        Set<GlobalSightLocale> tempLocaleSet = new HashSet<GlobalSightLocale>();
    	
        // Insert score Data
    	int finishedJobNum = 0;
    	int row = SEGMENT_START_ROW;
    	DecimalFormat numFormat = new DecimalFormat("#.00");
    	for(long jobId:p_jobIds)
    	{
    		// Cancel generate reports.
    		if (isCancelled())
    			return;
    		
    		Job job = ServerProxy.getJobHandler().getJobById(jobId);
    		if (job == null || !stateList.contains(job.getState()))
    			continue;
    		
    		List<GlobalSightLocale> jobTL = ReportHelper.getTargetLocals(job);
    		for (GlobalSightLocale trgLocale : sortLocales)
            {
                if (!jobTL.contains(trgLocale))
                    continue;

                for(Workflow workflow: job.getWorkflows())
                {
                	if(workflow.getTargetLocale().equals(trgLocale))
                	{
                		row = writeSegmentInfo(p_workbook, sheet, job, workflow, 
                				row, numFormat, trgLocale, socreSum, socreNum, tempLocaleSet);
                	}
                }
            }
    		
    		// Sets Reports Percent.
    		setPercent(++finishedJobNum);
    	}
    	
    	addStatisticsSheet(p_workbook, socreSum, socreNum, tempLocaleSet, numFormat);
    }
    
    private List<GlobalSightLocale> sortLocales(List<GlobalSightLocale> p_targetLocales)
    {
    	Set<String> localeStringSet = new TreeSet<String>();
    	for(GlobalSightLocale locale:p_targetLocales)
    	{
    		localeStringSet.add(locale.toString());
    	}
    	
    	List<GlobalSightLocale> sortLocales = new ArrayList<GlobalSightLocale>();
    	for(String localeString : localeStringSet)
    	{
    		for(GlobalSightLocale locale:p_targetLocales)
    		{
    			if(locale.toString().equals(localeString))
    			{
    				sortLocales.add(locale);
    				break;
    			}
    		}
    	}
    	return sortLocales;
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
        cell_A.setCellValue(bundle.getString("lb_job_id"));
        cell_A.setCellStyle(headerStyle);
        p_sheet.setColumnWidth(col, 10 * 256);
        col++;
        
        Cell cell_B = getCell(segHeaderRow, col);
        cell_B.setCellValue(bundle.getString("lb_job_name"));
        cell_B.setCellStyle(headerStyle);
        p_sheet.setColumnWidth(col, 30 * 256);
        col++;
        
        Cell cell_C = getCell(segHeaderRow, col);
        cell_C.setCellValue(bundle.getString("lb_target_locale"));
        cell_C.setCellStyle(headerStyle);
        p_sheet.setColumnWidth(col, 14 * 256);
        col++;
        
        for(Select select:categoryList)
        {
        	Cell cell_category = getCell(segHeaderRow, col);
        	cell_category.setCellValue(select.getValue());
        	cell_category.setCellStyle(headerStyle);
        	p_sheet.setColumnWidth(col, 21 * 256);
            col++;
        }

        Cell cell_AVG = getCell(segHeaderRow, col);
        cell_AVG.setCellValue(bundle.getString("lb_avg_score"));
        cell_AVG.setCellStyle(headerStyle);
        p_sheet.setColumnWidth(col, 13 * 256);
        col++;
        
        Cell cell_Overall = getCell(segHeaderRow, col);
        cell_Overall.setCellValue("Overall");
        cell_Overall.setCellStyle(headerStyle);
        p_sheet.setColumnWidth(col, 10 * 256);
        col++;
        
        Cell cell_Comments = getCell(segHeaderRow, col);
        cell_Comments.setCellValue(bundle.getString("lb_comments"));
        cell_Comments.setCellStyle(headerStyle);
        p_sheet.setColumnWidth(col, 100 * 256);
        col++;
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
        titleCell.setCellValue(bundle.getString("scorecard_report"));
        titleCell.setCellStyle(getTitleStyle(p_workBook));
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
            Workflow p_workflow, int p_row, DecimalFormat numFormat,
            GlobalSightLocale trgLocale,
            HashMap<String, HashMap<String, Integer>> socreSum,
            HashMap<String, HashMap<String, Integer>> socreNum,
            Set<GlobalSightLocale> tempLocaleSet) throws Exception
    {
        List<ScorecardScore> scoreList = ScorecardScoreHelper.getScoreByWrkflowId(p_workflow.getId());
        contentStyle = getContentStyle(p_workBook);
        if (scoreList.size() > 0)
        {
        	int col = 0;
        	Row currentRow = getRow(p_sheet, p_row);
            // Job id
            Cell cell_A = getCell(currentRow, col);
            cell_A.setCellValue(p_job.getId());
            cell_A.setCellStyle(contentStyle);
            col++;

            // job name
            Cell cell_B = getCell(currentRow, col);
            cell_B.setCellValue(p_job.getJobName());
            cell_B.setCellStyle(contentStyle);
            col++;

            // TargetPage id
            Cell cell_C = getCell(currentRow, col);
            cell_C.setCellValue(p_workflow.getTargetLocale().toString());
            cell_C.setCellStyle(contentStyle);
            col++;

            int workflowScoreSum = 0;
            int workflowScoreNum = 0;
            //Scores
            for(Select select:categoryList)
            {
            	Cell cell_score = getCell(currentRow, col);
            	int scoreValue = 0;
            	for(ScorecardScore score:scoreList)
            	{
            		if(score.getScorecardCategory().equals(select.getValue()))
            		{
            			scoreValue = score.getScore();
            			String tl = trgLocale.toString();
            			String categoryString = select.getValue();
            			
            			socreSum.get(tl).put(categoryString, 
            					socreSum.get(tl).get(categoryString) + scoreValue);
            			workflowScoreSum = workflowScoreSum + scoreValue;
            			socreNum.get(tl).put(categoryString, 
            					socreNum.get(tl).get(categoryString) + 1);
            			workflowScoreNum++;
            			
            			tempLocaleSet.add(trgLocale);
            			break;
            		}
            	}
            	if(scoreValue > 0)
            	{
            		cell_score.setCellValue(scoreValue);
            		cell_score.setCellStyle(getScoreCellStyle(p_workBook, 
            				(double)scoreValue));
            	}
            	else 
            	{
            		cell_score.setCellValue("");
            		cell_score.setCellStyle(contentStyle);
				}
                col++;
            }

            // AVG Score
            Cell cell_AvgScore = getCell(currentRow, col);
            double avgScore = 0;
            if(workflowScoreNum > 0)
    		{
    			avgScore = (double)workflowScoreSum/workflowScoreNum;
    			cell_AvgScore.setCellValue(Double.parseDouble(numFormat.format(avgScore)));
    			cell_AvgScore.setCellStyle(getScoreCellStyle(p_workBook,avgScore));
    		}
    		else
    		{
    			cell_AvgScore.setCellValue("");
    			cell_AvgScore.setCellStyle(contentStyle);
    		}
            col++;
            
            // Overall
            Cell cell_Overall = getCell(currentRow, col);
            if(avgScore > 0 && avgScore < 3)
    		{
            	cell_Overall.setCellValue("Negative");
    		}
            else if(avgScore >= 3)
            {
            	cell_Overall.setCellValue("Positive");
            }
            cell_Overall.setCellStyle(contentStyle);
            col++;

            // Comments
            boolean rtlTargetLocale = EditUtil.isRTLLocale(p_workflow
            		.getTargetLocale().toString());
            CellStyle commentStyle = rtlTargetLocale ? getUnlockedRightStyle(p_workBook)
                    : getUnlockedStyle(p_workBook);
            Cell cell_H = getCell(currentRow, col);
            cell_H.setCellValue(((WorkflowImpl)p_workflow).getScorecardComment());
            cell_H.setCellStyle(commentStyle);

            p_row++; 
        }

        return p_row;
    }

    private void addStatisticsSheet(Workbook p_workbook,
    		HashMap<String, HashMap<String, Integer>> socreSum,
    		HashMap<String, HashMap<String, Integer>> socreNum,
    		Set<GlobalSightLocale> p_targetLocales, 
    		DecimalFormat numFormat) throws Exception
    {
    	Sheet statisticsSheet = p_workbook.createSheet("Statistics");
    	int row = 0;
    	int col = 0;
    	//header
    	Row headerRow = getRow(statisticsSheet, row);
    	
    	CellStyle headerStyle = getHeaderStyle(p_workbook);
    	Cell localeHeaderCell = getCell(headerRow, col);
    	localeHeaderCell.setCellValue(bundle.getString("lb_target_locale"));
    	localeHeaderCell.setCellStyle(headerStyle);
    	statisticsSheet.setColumnWidth(col, 14 * 256);
    	col++;
    	
    	for(Select category: categoryList)
    	{
    		Cell categoryCell = getCell(headerRow, col);
    		categoryCell.setCellValue(category.getValue());
    		categoryCell.setCellStyle(headerStyle);
    		statisticsSheet.setColumnWidth(col, 21 * 256);
        	col++;
    	}
    	
    	Cell avgHeaderCell = getCell(headerRow, col);
    	avgHeaderCell.setCellValue(bundle.getString("lb_avg_score"));
    	avgHeaderCell.setCellStyle(headerStyle);
    	statisticsSheet.setColumnWidth(col, 13 * 256);

    	HashMap<String,Integer> categoyAvgSum = new HashMap<String,Integer>();
    	HashMap<String,Integer> categoyAvgNum = new HashMap<String,Integer>();
    	int totalSum = 0;
    	int totalNum = 0;
    	List<GlobalSightLocale> scortLocales = sortLocales(
    			new ArrayList<GlobalSightLocale>(p_targetLocales));
    	//segment
        for(GlobalSightLocale locale: scortLocales)
        {
        	col = 0;
        	row++;
        	String tl = locale.toString();
        	
        	Row scoreRow = getRow(statisticsSheet, row);
        	Cell localeCell = getCell(scoreRow, col);
        	localeCell.setCellValue(tl);
        	localeCell.setCellStyle(contentStyle);
        	col++;
        	
        	int avgSum = 0;
        	int avgNum = 0;
        	for(Select category: categoryList)
        	{
        		String cv = category.getValue();
        		if(socreSum.get(tl).get(cv) != 0)
        		{
        			Cell scoreCell = getCell(scoreRow, col);
        			double avgScore = (double)socreSum.get(tl).get(cv)/socreNum.get(tl).get(cv);
        			scoreCell.setCellValue(Double.parseDouble(numFormat.format(avgScore)));
        			scoreCell.setCellStyle(getScoreCellStyle(p_workbook,avgScore));
                	col++;
                	//Statistics for locale
                	avgSum = avgSum + socreSum.get(tl).get(cv);
                	avgNum = avgNum + socreNum.get(tl).get(cv);
                	
                	//Statistics for category
                	if(categoyAvgSum.get(cv) != null)
                	{
                		categoyAvgSum.put(cv,categoyAvgSum.get(cv) +  socreSum.get(tl).get(cv));
                		categoyAvgNum.put(cv,categoyAvgNum.get(cv) +  socreNum.get(tl).get(cv));
                	}
                	else
                	{
                		categoyAvgSum.put(cv, socreSum.get(tl).get(cv));
                		categoyAvgNum.put(cv, socreNum.get(tl).get(cv));
                	}
                	
                	//Statistics for total
                	totalSum = totalSum + socreSum.get(tl).get(cv);
                	totalNum = totalNum + socreNum.get(tl).get(cv);
        		}
        		else
        		{
        			Cell scoreCell = getCell(scoreRow, col);
        			scoreCell.setCellValue("");
        			scoreCell.setCellStyle(contentStyle);
                	col++;
        		}
        	}
        	
        	Cell avgCell = getCell(scoreRow, col);
        	if(avgNum > 0)
        	{
        		double avgScore = (double)avgSum/avgNum;
        		avgCell.setCellValue(Double.parseDouble(numFormat.format(avgScore)));
        		avgCell.setCellStyle(getScoreCellStyle(p_workbook,avgScore));
        	}
        	else
        	{
        		avgCell.setCellValue("");
        		avgCell.setCellStyle(contentStyle);
        	}
        	col++;
        }
        
        // avg 
        row++;
        col = 0;
        Row avgRow = getRow(statisticsSheet, row);
        
        Cell avgCell = getCell(avgRow, col);
        if(totalNum != 0)
        {
           	avgCell.setCellValue(bundle.getString("lb_avg_score"));
        	avgCell.setCellStyle(contentStyle);
        }
    	col++;
    	
    	for(Select category: categoryList)
    	{
    		Cell categoryAvgCell = getCell(avgRow, col);
    		if(categoyAvgSum.get(category.getValue()) != null)
    		{
    			double avgScore = (double)categoyAvgSum.get(category.getValue())/
    								categoyAvgNum.get(category.getValue());
    			categoryAvgCell.setCellValue(Double.parseDouble(numFormat.format(avgScore)));
    			categoryAvgCell.setCellStyle(getScoreCellStyle(p_workbook,avgScore));
    		}
    		col++;
    	}
    	
    	Cell totalAvgCell = getCell(avgRow, col);
    	double avgScore = 0;
    	if(totalNum != 0)
    	{
    		avgScore = (double)totalSum/totalNum;
    		totalAvgCell.setCellValue(Double.parseDouble(numFormat.format(avgScore)));
    		totalAvgCell.setCellStyle(getScoreCellStyle(p_workbook,avgScore));
    	}
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
            cs.setVerticalAlignment(CellStyle.VERTICAL_CENTER);

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
            style.setBorderTop(CellStyle.BORDER_THIN);
            style.setBorderRight(CellStyle.BORDER_THIN);
            style.setBorderBottom(CellStyle.BORDER_THIN);
            style.setBorderLeft(CellStyle.BORDER_THIN);
            style.setVerticalAlignment(CellStyle.VERTICAL_CENTER);

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
            style.setBorderTop(CellStyle.BORDER_THIN);
            style.setBorderRight(CellStyle.BORDER_THIN);
            style.setBorderBottom(CellStyle.BORDER_THIN);
            style.setBorderLeft(CellStyle.BORDER_THIN);
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
            style.setBorderTop(CellStyle.BORDER_THIN);
            style.setBorderRight(CellStyle.BORDER_THIN);
            style.setBorderBottom(CellStyle.BORDER_THIN);
            style.setBorderLeft(CellStyle.BORDER_THIN);
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
            style.setBorderTop(CellStyle.BORDER_THIN);
            style.setBorderRight(CellStyle.BORDER_THIN);
            style.setBorderBottom(CellStyle.BORDER_THIN);
            style.setBorderLeft(CellStyle.BORDER_THIN);
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
    
    private CellStyle getScoreCellStyle(Workbook p_workbook, double score)
    {
    	if(score < 3)
    	{
    		return getUnder3Style(p_workbook);
    	}
    	else if (score < 4)
    	{
    		return getUnder4Style(p_workbook);
    	}
    	else if (score < 5)
    	{
    		return getUnder5Style(p_workbook);
    	}
    	else
    	{
    		return getEqual5Style(p_workbook);
    	}
    }
    
    private CellStyle getEqual5Style(Workbook p_workbook)
    {
    	if(equal5Style == null)
    	{
    		Font font = p_workbook.createFont();
    		font.setColor(IndexedColors.BLACK.getIndex());
    		font.setUnderline(Font.U_NONE);
    		font.setFontName("Arial");
    		font.setFontHeightInPoints((short) 9);
    		
    		CellStyle cs = p_workbook.createCellStyle();
    		cs.setFont(font);
    		cs.setWrapText(true);
    		cs.setFillPattern(CellStyle.SOLID_FOREGROUND );
    		cs.setFillForegroundColor(IndexedColors.BRIGHT_GREEN.getIndex());
    		cs.setBorderTop(CellStyle.BORDER_THIN);
            cs.setBorderRight(CellStyle.BORDER_THIN);
            cs.setBorderBottom(CellStyle.BORDER_THIN);
            cs.setBorderLeft(CellStyle.BORDER_THIN);
            cs.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
    		
    		equal5Style = cs;
    	}
		
		return equal5Style;
    }
    
    private CellStyle getUnder5Style(Workbook p_workbook)
    {
    	if(under5Style == null)
    	{
    		Font font = p_workbook.createFont();
    		font.setColor(IndexedColors.BLACK.getIndex());
    		font.setUnderline(Font.U_NONE);
    		font.setFontName("Arial");
    		font.setFontHeightInPoints((short) 9);
    		
    		CellStyle cs = p_workbook.createCellStyle();
    		cs.setFont(font);
    		cs.setWrapText(true);
    		cs.setFillPattern(CellStyle.SOLID_FOREGROUND );
    		cs.setFillForegroundColor(IndexedColors.LIME.getIndex());
    		cs.setBorderTop(CellStyle.BORDER_THIN);
            cs.setBorderRight(CellStyle.BORDER_THIN);
            cs.setBorderBottom(CellStyle.BORDER_THIN);
            cs.setBorderLeft(CellStyle.BORDER_THIN);
            cs.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
    		
    		under5Style = cs;
    	}
		
		return under5Style;
    }
    
    private CellStyle getUnder4Style(Workbook p_workbook)
    {
    	if(under4Style == null)
    	{
    		Font font = p_workbook.createFont();
    		font.setColor(IndexedColors.BLACK.getIndex());
    		font.setUnderline(Font.U_NONE);
    		font.setFontName("Arial");
    		font.setFontHeightInPoints((short) 9);
    		
    		CellStyle cs = p_workbook.createCellStyle();
    		cs.setFont(font);
    		cs.setWrapText(true);
    		cs.setFillPattern(CellStyle.SOLID_FOREGROUND );
    		cs.setFillForegroundColor(IndexedColors.LIGHT_ORANGE.getIndex());
    		cs.setBorderTop(CellStyle.BORDER_THIN);
            cs.setBorderRight(CellStyle.BORDER_THIN);
            cs.setBorderBottom(CellStyle.BORDER_THIN);
            cs.setBorderLeft(CellStyle.BORDER_THIN);
            cs.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
    		
    		under4Style = cs;
    	}
		
		return under4Style;
    }
    
    private CellStyle getUnder3Style(Workbook p_workbook)
    {
    	if(under3Style == null)
    	{
    		Font font = p_workbook.createFont();
    		font.setColor(IndexedColors.BLACK.getIndex());
    		font.setUnderline(Font.U_NONE);
    		font.setFontName("Arial");
    		font.setFontHeightInPoints((short) 9);
    		
    		CellStyle cs = p_workbook.createCellStyle();
    		cs.setFont(font);
    		cs.setWrapText(true);
    		cs.setFillPattern(CellStyle.SOLID_FOREGROUND );
    		cs.setFillForegroundColor(IndexedColors.ROSE.getIndex());
    		cs.setBorderTop(CellStyle.BORDER_THIN);
            cs.setBorderRight(CellStyle.BORDER_THIN);
            cs.setBorderBottom(CellStyle.BORDER_THIN);
            cs.setBorderLeft(CellStyle.BORDER_THIN);
            cs.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
    		
    		under3Style = cs;
    	}
		
		return under3Style;
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
    
    
    private List<String> getScorecardCategories(String companyId)
    {
    	List<String> scorecardCategories = CompanyWrapper
			.getCompanyScorecardCategoryList(companyId);
    	return scorecardCategories;
    }
    
    private List<Select> initSelectList(List<String> keyList, ResourceBundle bundle)
    {
        List<Select> list = new ArrayList<Select>();
        for (String key : keyList)
        {
            String valueOfSelect = "";
            try
            {
                valueOfSelect = bundle.getString(key);
            }
            catch (MissingResourceException e)
            {
                valueOfSelect = key;
            }
            // we should put value both at key and value places
            Select option = new Select(key, valueOfSelect);
            list.add(option);
        }
        return list;
    }
}
