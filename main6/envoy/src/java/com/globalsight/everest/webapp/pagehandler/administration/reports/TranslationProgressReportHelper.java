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

import java.rmi.RemoteException;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.ServletOutputStream;
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
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.foundation.SearchCriteriaParameters;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.jobhandler.JobSearchParameters;
import com.globalsight.everest.localemgr.LocaleManagerException;
import com.globalsight.everest.page.TargetPage;
import com.globalsight.everest.persistence.tuv.SegmentTuvUtil;
import com.globalsight.everest.projecthandler.Project;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.pagehandler.administration.reports.bo.ReportsData;
import com.globalsight.everest.webapp.pagehandler.administration.reports.generator.ReportGeneratorHandler;
import com.globalsight.everest.webapp.pagehandler.administration.users.UserUtil;
import com.globalsight.everest.webapp.pagehandler.projects.workflows.JobSearchConstants;
import com.globalsight.everest.workflowmanager.Workflow;
import com.globalsight.util.GeneralException;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.IntHolder;

public class TranslationProgressReportHelper
{
    private static Logger s_logger = Logger.getLogger("Reports");

	private static Map<String, ReportsData> m_reportsDataMap = 
            new ConcurrentHashMap<String, ReportsData>();
	
	private ResourceBundle bundle = null;

	public static String JOB_ID = "jobId";

	public static String PROJECT_ID = "projectId";

	public static String SOURCE_LOCALES = "sourceLocalesList";

	public static String TARGET_LOCALES = "targetLocalesList";

	private CellStyle contentStyle = null;
	private CellStyle percentStyle = null;
	private CellStyle headerStyle = null;

	private GlobalSightLocale m_sourceLocale = null;

	private GlobalSightLocale m_targetLocale = null;

	private NumberFormat percent = null;
    private String userId = null;
    private List<Long> m_jobIDS = null;

	public TranslationProgressReportHelper()
	{

	}

	/**
	 * Generates the Excel report and spits it to the outputstream The report
	 * consists of all in progress tasks
	 * 
	 * @return File
	 * @exception Exception
	 */
	public void generateReport(HttpServletRequest p_request,
			HttpServletResponse p_response) throws Exception
	{
		String companyName = UserUtil.getCurrentCompanyName(p_request);
		CompanyThreadLocal.getInstance().setValue(companyName);

		setLocale(p_request);

		p_request.setCharacterEncoding("UTF-8");
		HttpSession session = p_request.getSession(false);
        userId = (String) session.getAttribute(WebAppConstants.USER_NAME);
		percent = NumberFormat.getPercentInstance((Locale) session
				.getAttribute(WebAppConstants.UILOCALE));
		
		Workbook p_workbook = new SXSSFWorkbook();
		addJobs(p_workbook, p_request, p_response);
        // Cancelled the report, return nothing.
        if (isCancelled())
        {
            p_response.sendError(p_response.SC_NO_CONTENT);
            return;
        }
        
        ServletOutputStream out = p_response.getOutputStream();
        p_workbook.write(out);
        out.close();
        ((SXSSFWorkbook)p_workbook).dispose();
	}

	/**
	 * Gets the jobs and outputs tasks information.
	 * 
	 * @exception Exception
	 */
    private void addJobs(Workbook p_workbook, HttpServletRequest p_request,
            HttpServletResponse p_response) throws Exception
    {
	    bundle = PageHandler.getBundle(p_request.getSession());
		// print out the request parameters
		String[] paramJobId = p_request.getParameterValues(JOB_ID);
		String[] paramTargetLocales = p_request
				.getParameterValues(TARGET_LOCALES);
		String[] paramSourceLocales = p_request
				.getParameterValues(SOURCE_LOCALES);
		Sheet sheet = p_workbook.createSheet(bundle.getString("lb_lisa_qa"));
		
		addTitle(p_workbook, sheet);
		
		addLanguageHeader(p_workbook, sheet);
		
		addSegmentHeader(p_workbook, sheet);
		
		Locale uiLocale = (Locale) p_request.getSession().getAttribute(
				WebAppConstants.UILOCALE);
		String srcLang = m_sourceLocale.getDisplayName(uiLocale);
		String trgLang = m_targetLocale.getDisplayName(uiLocale);
		writeLanguageInfo(p_workbook, sheet, srcLang, trgLang);

		// get jobs
		ArrayList<Job> jobs = new ArrayList<Job>();
		if (paramJobId != null && "*".equals(paramJobId[0]))
		{
			JobSearchParameters searchParams = getSearchParams(p_request);
			jobs.addAll(ServerProxy.getJobHandler().getJobs(searchParams));
		}
		else
		{
			for (int i = 0; i < paramJobId.length; i++)
			{
				if ("*".equals(paramJobId[i]) == false)
				{
					long jobId = Long.parseLong(paramJobId[i]);
					Job j = ServerProxy.getJobHandler().getJobById(jobId);
					jobs.add(j);
				}
			}
		}
		
		m_jobIDS = ReportHelper.getJobIDS(jobs);
        // Cancel Duplicate Request
        if (ReportHelper.checkReportsDataInProgressStatus(userId,
                m_jobIDS, getReportType()))
        {
            p_workbook = null;
            p_response.sendError(HttpServletResponse.SC_NO_CONTENT);
            return;
        }
        // Set ReportsData.
        ReportHelper.setReportsData(userId, m_jobIDS, getReportType(),
                0, ReportsData.STATUS_INPROGRESS);
        
		// Separate jobs by Division
		Hashtable<String, List<Job>> projects = new Hashtable<String, List<Job>>();
		String tLocale = paramTargetLocales[0];
		for (Job job : jobs)
		{
            if (isCancelled())
            {
                return;
            }
			boolean containTarLocale = false;
			for (Workflow wf : job.getWorkflows())
			{
				String wfLocale = Long.toString(wf.getTargetLocale().getId());
				if (wfLocale.equals(tLocale))
				{
					containTarLocale = true;
					break;
				}
			}
			if(!containTarLocale)
			{
				continue;
			}
			
			String projectName = job.getL10nProfile().getProject().getName();
			List<Job> jobList = null;
			if (projects.containsKey(projectName))
			{
				jobList = projects.get(projectName);
			}
			else
			{
				jobList = new ArrayList<Job>();
				projects.put(projectName, jobList);
			}
			jobList.add(job);
		}

		// add jobs into sheet
		IntHolder row = new IntHolder(7);
		for (Enumeration<String> e = projects.keys(); e.hasMoreElements();)
		{
			String projectName = (String) e.nextElement();
			Cell cell_A_ProjectName = getCell(getRow(sheet, row.value), 0);
			cell_A_ProjectName.setCellValue(projectName);
			cell_A_ProjectName.setCellStyle(getContentStyle(p_workbook));
			List<Job> jobList = projects.get(projectName);
			for (Job job : jobList)
			{
                if (isCancelled())
                {
                    p_workbook = null;
                    return;
                }
				addJobPages(p_workbook, sheet, job, row, paramSourceLocales,
						paramTargetLocales);
			}
		}

		// Set ReportsData.
		ReportHelper.setReportsData(userId, m_jobIDS, getReportType(),
		                100, ReportsData.STATUS_FINISHED);
	}
    
    private void addTitle(Workbook p_workbook, Sheet p_sheet) throws Exception
    {
    	// title font is black bold on white
		// String EMEA = CompanyWrapper.getCurrentCompanyName();
		Font titleFont = p_workbook.createFont();
        titleFont.setUnderline(Font.U_NONE);
        titleFont.setFontName("Times");
        titleFont.setFontHeightInPoints((short) 14);
        titleFont.setBoldweight(Font.BOLDWEIGHT_BOLD);
        titleFont.setColor(IndexedColors.BLACK.getIndex());
        CellStyle titleStyle = p_workbook.createCellStyle();
        titleStyle.setWrapText(false);
        titleStyle.setFont(titleFont);

        Row titleRow = getRow(p_sheet, 0);
        Cell cell_A_Title = getCell(titleRow, 0);
        cell_A_Title.setCellValue(bundle
        		.getString("review_translation_progress_report"));
        cell_A_Title.setCellStyle(titleStyle);
		p_sheet.setColumnWidth(0, 22 * 256);
    }
    
    private void addLanguageHeader(Workbook p_workbook, Sheet p_sheet) throws Exception
    {
    	int col = 0;
		int row = 3;
		Row headerRow = getRow(p_sheet, row);
		Cell cell_SourceLang = getCell(headerRow, col++);
		cell_SourceLang.setCellValue(bundle
        		.getString("lb_source_language"));
		cell_SourceLang.setCellStyle(getHeaderStyle(p_workbook));
		p_sheet.setColumnWidth(col - 1, 27 * 256);
		
		Cell cell_TargetLang = getCell(headerRow, col++);
		cell_TargetLang.setCellValue(bundle
        		.getString("lb_target_language"));
		cell_TargetLang.setCellStyle(getHeaderStyle(p_workbook));
		p_sheet.setColumnWidth(col - 1, 27 * 256);
    }
    
    /**
	 * Adds the table header to the sheet
	 * 
	 * @param p_sheet
	 */
    private void addSegmentHeader(Workbook p_workbook, Sheet p_sheet)
            throws Exception
	{ 		
		int col = 0;
		int row = 6;
		Row secHeaderRow = getRow(p_sheet, row);
		Cell cell_A = getCell(secHeaderRow, col++);
        cell_A.setCellValue(bundle
        		.getString("lb_project"));
        cell_A.setCellStyle(getHeaderStyle(p_workbook));
		p_sheet.setColumnWidth(col - 1, 27 * 256);
		
		Cell cell_B = getCell(secHeaderRow, col++);
		cell_B.setCellValue(bundle
        		.getString("lb_job_id_report"));
		cell_B.setCellStyle(getHeaderStyle(p_workbook));
		p_sheet.setColumnWidth(col - 1, 27 * 256);
		
		Cell cell_C = getCell(secHeaderRow, col++);
		cell_C.setCellValue(bundle
        		.getString("lb_job_name"));
		cell_C.setCellStyle(getHeaderStyle(p_workbook));
		p_sheet.setColumnWidth(col - 1, 30 * 256);
		
		Cell cell_D = getCell(secHeaderRow, col++);
		cell_D.setCellValue(bundle
        		.getString("lb_document_name"));
		cell_D.setCellStyle(getHeaderStyle(p_workbook));
		p_sheet.setColumnWidth(col - 1, 40 * 256);
		
		Cell cell_E = getCell(secHeaderRow, col++);
		cell_E.setCellValue(bundle
        		.getString("lb_total_translated_text"));
		cell_E.setCellStyle(getHeaderStyle(p_workbook));
		p_sheet.setColumnWidth(col - 1, 20 * 256);
	}

	private void writeLanguageInfo(Workbook p_workbook, Sheet sheet,
			String srcLang, String trgLang) throws Exception
	{
		Row localeRow = getRow(sheet, 4);
        Cell cell_A = getCell(localeRow, 0);
        cell_A.setCellValue(srcLang);
        cell_A.setCellStyle(getContentStyle(p_workbook));
        Cell cell_B = getCell(localeRow, 1);
        cell_B.setCellValue(trgLang);
        cell_B.setCellStyle(getContentStyle(p_workbook));
	}
	
	/**
	 * add pages in a job
	 * 
	 * @param sheet
	 * @param job
	 * @param row
	 * @param paramSourceLocales
	 * @param paramTargetLocales
	 * @throws Exception
	 */
    private void addJobPages(Workbook p_workbook, Sheet p_sheet, Job job,
            IntHolder row, String[] paramSourceLocales,
            String[] paramTargetLocales) throws Exception
	{
		// return if source locale are not selected
		GlobalSightLocale gsl = job.getSourceLocale();
        if (!(Long.toString(gsl.getId())).equals(paramSourceLocales[0]))
            return;

        // get basic parameters
		String jobName = job.getJobName();
		long jobId = job.getId();
		Workflow selectedWF = null;
		String tLocale = paramTargetLocales[0];
		for (Workflow wf : job.getWorkflows())
		{
			String wfLocale = Long.toString(wf.getTargetLocale().getId());
			if (wfLocale.equals(tLocale))
			{
				selectedWF = wf;
				break;
			}
		}

		if (selectedWF != null)
		{
			Vector<TargetPage> targetPages = selectedWF.getTargetPages();
			for (TargetPage tp : targetPages)
			{
				int c = 1;
				// 8.2 Job id
				Row tehRow = getRow(p_sheet, row.value);
				Cell cell_B = getCell(tehRow, c++);
				cell_B.setCellValue(jobId);
				cell_B.setCellStyle(getContentStyle(p_workbook));
				// 8.2 job name
				Cell cell_C = getCell(tehRow, c++);
				cell_C.setCellValue(jobName);
				cell_C.setCellStyle(getContentStyle(p_workbook));
				// 8.3 document name
				String fileName = tp.getSourcePage().getExternalPageId();
                // fileName = SourcePage.filtSpecialFile(fileName);
				Cell cell_D = getCell(tehRow, c++);
				cell_D.setCellValue(fileName);
				cell_D.setCellStyle(getContentStyle(p_workbook));
				// 8.4 total translated text
                double p = SegmentTuvUtil
                        .getTranslatedPercentageForTargetPage(tp.getId()) / 100.0;
				Cell cell_E = getCell(tehRow, c++);
				cell_E.setCellValue(p);
				cell_E.setCellStyle(getPercentStyle(p_workbook));
				row.inc();
			}
		}
	}
	
	/**
	 * Returns search params used to find the in progress jobs for all PMs
	 * 
	 * @return JobSearchParams
	 */
	private JobSearchParameters getSearchParams(HttpServletRequest p_request)
			throws Exception
	{
		String[] paramProjectIds = p_request.getParameterValues(PROJECT_ID);

		JobSearchParameters sp = new JobSearchParameters();

        // job status
        ArrayList<String> stateList = new ArrayList<String>();
        // just do a query for all in progress jobs, localized, and exported
        stateList.add(Job.DISPATCHED);
        stateList.add(Job.LOCALIZED);
        stateList.add(Job.EXPORTED);
        // }
		sp.setJobState(stateList);

		// source locales and target locales
		if (m_sourceLocale == null || m_targetLocale == null)
		{
			setLocale(p_request);
		}
		sp.setSourceLocale(m_sourceLocale);
		sp.setTargetLocale(m_targetLocale);

		// search by project
		ArrayList<Long> projectIdList = new ArrayList<Long>();
		boolean wantsAllProjects = false;
		for (int i = 0; i < paramProjectIds.length; i++)
        {
            String id = paramProjectIds[i];
            if (id.equals("*"))
            {
                wantsAllProjects = true;
                for (Project project : (ArrayList<Project>) ServerProxy
                        .getProjectHandler().getProjectsByUser(userId))
                {
                    projectIdList.add(project.getIdAsLong());
                }
                break;
            }
            else
            {
                projectIdList.add(new Long(id));
            }
        }
		
        sp.setProjectId(projectIdList);
		
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd/yyyy");
		// job time
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

		return sp;
	}

	private void setLocale(HttpServletRequest p_request)
			throws LocaleManagerException, RemoteException, GeneralException
	{
		String[] paramSourceLocales = p_request
				.getParameterValues(SOURCE_LOCALES);
		String[] paramTargetLocales = p_request
				.getParameterValues(TARGET_LOCALES);

		if (paramSourceLocales != null && !"*".equals(paramSourceLocales[0]))
		{
			m_sourceLocale = ServerProxy.getLocaleManager().getLocaleById(
					Long.valueOf(paramSourceLocales[0]));
		}

		if (paramTargetLocales != null && !"*".equals(paramTargetLocales[0]))
		{
			m_targetLocale = ServerProxy.getLocaleManager().getLocaleById(
					Long.valueOf(paramTargetLocales[0]));
		}
	}

	public String getReportType()
    {
        return ReportConstants.TRANSLATION_PROGRESS_REPORT;
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
            Font font = p_workbook.createFont();
            font.setFontName("Arial");
            font.setFontHeightInPoints((short) 10);
            style.setFont(font);
            style.setWrapText(true);
            style.setAlignment(CellStyle.ALIGN_LEFT);

            contentStyle = style;
        }

        return contentStyle;
    }
	
	private CellStyle getPercentStyle(Workbook p_workbook) throws Exception
    {
        if (percentStyle == null)
        {
        	DataFormat format = p_workbook.createDataFormat();
            CellStyle style = p_workbook.createCellStyle();
            Font font = p_workbook.createFont();
            font.setFontName("Arial");
            font.setFontHeightInPoints((short) 10);
            style.setDataFormat(format.getFormat("0%"));
            style.setFont(font);
            style.setWrapText(true);
            style.setAlignment(CellStyle.ALIGN_LEFT);

            percentStyle = style;
        }

        return percentStyle;
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
    
    public boolean isCancelled()
    {
        ReportsData data = ReportGeneratorHandler.getReportsMap(userId,
                m_jobIDS, getReportType());
        if (data != null)
            return data.isCancle();

        return false;
    }
}