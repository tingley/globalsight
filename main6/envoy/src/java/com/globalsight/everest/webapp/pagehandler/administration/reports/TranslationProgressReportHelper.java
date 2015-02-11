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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.format.Alignment;
import jxl.format.UnderlineStyle;
import jxl.format.VerticalAlignment;
import jxl.write.Label;
import jxl.write.Number;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

import org.apache.log4j.Logger;

import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.costing.BigDecimalHelper;
import com.globalsight.everest.edit.online.OnlineEditorManagerLocal;
import com.globalsight.everest.foundation.SearchCriteriaParameters;
import com.globalsight.everest.integration.ling.LingServerProxy;
import com.globalsight.everest.integration.ling.tm2.MatchTypeStatistics;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.jobhandler.JobSearchParameters;
import com.globalsight.everest.localemgr.LocaleManagerException;
import com.globalsight.everest.page.PageManager;
import com.globalsight.everest.page.SourcePage;
import com.globalsight.everest.page.TargetPage;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.tuv.Tuv;
import com.globalsight.everest.tuv.TuvManager;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.pagehandler.administration.reports.bo.ReportsData;
import com.globalsight.everest.webapp.pagehandler.administration.users.UserUtil;
import com.globalsight.everest.webapp.pagehandler.projects.workflows.JobSearchConstants;
import com.globalsight.everest.workflowmanager.Workflow;
import com.globalsight.ling.tm.LeverageMatchLingManager;
import com.globalsight.util.GeneralException;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.IntHolder;

public class TranslationProgressReportHelper
{
	private static Logger s_logger = Logger
			.getLogger("Reports");
	
	private static Map<String, ReportsData> m_reportsDataMap = 
            new ConcurrentHashMap<String, ReportsData>();
	
	public WritableWorkbook m_workbook = null;

	public static String JOB_ID = "jobId";

	public static String PROJECT_ID = "projectId";

	public static String SOURCE_LOCALES = "sourceLocalesList";

	public static String TARGET_LOCALES = "targetLocalesList";

	private WritableCellFormat contentFormat = null;

	private GlobalSightLocale m_sourceLocale = null;

	private GlobalSightLocale m_targetLocale = null;

	private NumberFormat percent = null;

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
		HttpSession session = p_request.getSession();
		percent = NumberFormat.getPercentInstance((Locale) session
				.getAttribute(WebAppConstants.UILOCALE));
		WorkbookSettings settings = new WorkbookSettings();
		settings.setSuppressWarnings(true);
		settings.setEncoding("UTF-8");
		m_workbook = Workbook.createWorkbook(p_response.getOutputStream(),
				settings);
		addJobs(p_request, p_response);
        if (m_workbook != null)
        {
            m_workbook.write();
            m_workbook.close();
        }
	}

	/**
	 * Gets the jobs and outputs tasks information.
	 * 
	 * @exception Exception
	 */
    private void addJobs(HttpServletRequest p_request,
            HttpServletResponse p_response) throws Exception
    {
	    String userId = (String) p_request.getSession().getAttribute(
                WebAppConstants.USER_NAME);
	    ResourceBundle bundle = PageHandler.getBundle(p_request.getSession());
		// print out the request parameters
		String[] paramJobId = p_request.getParameterValues(JOB_ID);
		String[] paramTargetLocales = p_request
				.getParameterValues(TARGET_LOCALES);
		String[] paramSourceLocales = p_request
				.getParameterValues(SOURCE_LOCALES);
		WritableSheet sheet = m_workbook.createSheet(bundle.getString("lb_lisa_qa"), 0);
		// *******
		// add header
		// *******
		addHeader(sheet, bundle);
		// *******
		// add srcLocale and tgtLocale
		// *******
        Locale uiLocale = (Locale) p_request.getSession().getAttribute(
                WebAppConstants.UILOCALE);
		sheet.addCell(new Label(0, 4, m_sourceLocale.getDisplayName(uiLocale),
				getContentFormat()));
		sheet.addCell(new Label(1, 4, m_targetLocale.getDisplayName(uiLocale),
				getContentFormat()));
		// *******
		// get jobs
		// *******
		ArrayList jobs = new ArrayList();
		if (paramJobId != null && "*".equals(paramJobId[0]))
		{
			// do a search based on the params
			JobSearchParameters searchParams = getSearchParams(p_request);
			jobs.addAll(ServerProxy.getJobHandler().getJobs(searchParams));
		}
		else
		{
			// get choosed jobs
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
		
		List<Long> reportJobIDS = ReportHelper.getJobIDS(jobs);
        // Cancel Duplicate Request
        if (ReportHelper.checkReportsDataMap(m_reportsDataMap, userId,
                reportJobIDS, null))
        {
            m_workbook = null;
            p_response.sendError(p_response.SC_NO_CONTENT);
            return;
        }
        // Set m_reportsDataMap.
        ReportHelper.setReportsDataMap(m_reportsDataMap, userId, reportJobIDS,
                null, 0, ReportsData.STATUS_INPROGRESS);
        
		// *******
		// seperate jobs by Division
		// *******
		Hashtable projects = new Hashtable();
		for (Iterator iterator = jobs.iterator(); iterator.hasNext();)
		{
			Job job = (Job) iterator.next();
			String projectName = job.getL10nProfile().getProject().getName();
			ArrayList jobList = null;
			if (projects.containsKey(projectName))
			{
				jobList = (ArrayList) projects.get(projectName);
			}
			else
			{
				jobList = new ArrayList();
				projects.put(projectName, jobList);
			}
			jobList.add(job);
		}
		// *******
		// add jobs into sheet
		// *******
		IntHolder row = new IntHolder(7);
		for (Enumeration e = projects.keys(); e.hasMoreElements();)
		{
			String projectName = (String) e.nextElement();
			sheet.addCell(new Label(0, row.value, projectName,
					getContentFormat()));
			ArrayList jobList = (ArrayList) projects.get(projectName);
			for (Iterator iterator = jobList.iterator(); iterator.hasNext();)
			{
				Job job = (Job) iterator.next();
				addJobPages(sheet, job, row, paramSourceLocales,
						paramTargetLocales);
			}
		}
		
		// Set m_reportsDataMap.
		ReportHelper.setReportsDataMap(m_reportsDataMap, userId, reportJobIDS,
		                null, 100, ReportsData.STATUS_FINISHED);
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
		ArrayList projectIdList = new ArrayList();
		boolean wantsAllProjects = false;
		for (int i = 0; i < paramProjectIds.length; i++)
		{
			String id = paramProjectIds[i];
			if (id.equals("*"))
			{
				wantsAllProjects = true;
				break;
			}
			else
			{
				projectIdList.add(new Long(id));
			}
		}

		if (!wantsAllProjects)
		{
			sp.setProjectId(projectIdList);
		}

		// job time
		String paramCreateDateStartCount = p_request
				.getParameter(JobSearchConstants.CREATION_START);
		String paramCreateDateStartOpts = p_request
				.getParameter(JobSearchConstants.CREATION_START_OPTIONS);
		if ("-1".equals(paramCreateDateStartOpts) == false)
		{
			sp.setCreationStart(new Integer(paramCreateDateStartCount));
			sp.setCreationStartCondition(paramCreateDateStartOpts);
		}

		String paramCreateDateEndCount = p_request
				.getParameter(JobSearchConstants.CREATION_END);
		String paramCreateDateEndOpts = p_request
				.getParameter(JobSearchConstants.CREATION_END_OPTIONS);
		if (SearchCriteriaParameters.NOW.equals(paramCreateDateEndOpts))
		{
			sp.setCreationEnd(new java.util.Date());
		}
		else if ("-1".equals(paramCreateDateEndOpts) == false)
		{
			sp.setCreationEnd(new Integer(paramCreateDateEndCount));
			sp.setCreationEndCondition(paramCreateDateEndOpts);
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
			m_sourceLocale = ServerProxy.getLocaleManager().getLocaleByString(
					paramSourceLocales[0]);
		}

		if (paramTargetLocales != null && !"*".equals(paramTargetLocales[0]))
		{
			m_targetLocale = ServerProxy.getLocaleManager().getLocaleByString(
					paramTargetLocales[0]);
		}
	}

	/**
	 * Adds the table header to the sheet
	 * 
	 * @param p_sheet
	 */
	private void addHeader(WritableSheet p_sheet, ResourceBundle bundle) throws Exception
	{
		// title font is black bold on white
		// String EMEA = CompanyWrapper.getCurrentCompanyName();
		WritableFont titleFont = new WritableFont(WritableFont.TIMES, 14,
				WritableFont.BOLD, false, UnderlineStyle.NO_UNDERLINE,
				jxl.format.Colour.BLACK);
		WritableCellFormat titleFormat = new WritableCellFormat(titleFont);
		titleFormat.setWrap(false);
		titleFormat.setShrinkToFit(false);
		p_sheet.addCell(new Label(0, 0, bundle.getString("review_translation_progress_report"),
				titleFormat));
		p_sheet.setColumnView(0, 22);

		// headerFont is black bold on light grey
		WritableFont headerFont = new WritableFont(WritableFont.TIMES, 11,
				WritableFont.BOLD, false, UnderlineStyle.NO_UNDERLINE,
				jxl.format.Colour.BLACK);
		WritableCellFormat headerFormat = new WritableCellFormat(headerFont);
		headerFormat.setWrap(true);
		headerFormat.setBackground(jxl.format.Colour.GRAY_25);
		headerFormat.setShrinkToFit(false);
		headerFormat.setBorder(jxl.format.Border.ALL,
				jxl.format.BorderLineStyle.THIN, jxl.format.Colour.BLACK);
		int col = 0;
		int row = 3;
		p_sheet.addCell(new Label(col++, row, bundle.getString("lb_source_language"), headerFormat));
		p_sheet.setColumnView(col - 1, 27);
		p_sheet.addCell(new Label(col++, row, bundle.getString("lb_target_language"), headerFormat));
		p_sheet.setColumnView(col - 1, 27);
		col = 0;
		row = 6;
		p_sheet.addCell(new Label(col++, row, bundle.getString("lb_project"), headerFormat));
		p_sheet.setColumnView(col - 1, 27);
		p_sheet.addCell(new Label(col++, row, bundle.getString("lb_job_id_report"), headerFormat));
		p_sheet.setColumnView(col - 1, 27);
		p_sheet.addCell(new Label(col++, row, bundle.getString("lb_job_name"), headerFormat));
		p_sheet.setColumnView(col - 1, 30);
		p_sheet.addCell(new Label(col++, row, bundle.getString("lb_document_name"), headerFormat));
		p_sheet.setColumnView(col - 1, 40);
		p_sheet.addCell(new Label(col++, row, bundle.getString("lb_total_translated_text"),
				headerFormat));
		p_sheet.setColumnView(col - 1, 15);
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
	private void addJobPages(WritableSheet sheet, Job job, IntHolder row,
			String[] paramSourceLocales, String[] paramTargetLocales)
			throws Exception
	{
		// return if source locale are not selected
		GlobalSightLocale gsl = job.getSourceLocale();
		if (!gsl.toString().equals(paramSourceLocales[0])) return;
		// get basic parameters
		String jobName = job.getJobName();
		long jobId = job.getId();
		Collection workflows = job.getWorkflows();
		Workflow selectedWF = null;
		String tLocale = paramTargetLocales[0];
		for (Iterator iterator = workflows.iterator(); iterator.hasNext();)
		{
			Workflow wf = (Workflow) iterator.next();
			String wfLocale = wf.getTargetLocale().toString();
			if (wfLocale.equals(tLocale))
			{
				selectedWF = wf;
				break;
			}
		}

		if (selectedWF != null)
		{
			Vector targetPages = selectedWF.getTargetPages();
			for (Iterator iterator = targetPages.iterator(); iterator.hasNext();)
			{
				TargetPage tp = (TargetPage) iterator.next();
				int c = 1;
				// 8.2 Job id
				sheet.addCell(new Number(c++, row.value, jobId,
						getContentFormat()));
				// 8.2 job name
				sheet.addCell(new Label(c++, row.value, jobName,
						getContentFormat()));
				// 8.3 document name
				String fileName = tp.getSourcePage().getExternalPageId();
                // fileName = SourcePage.filtSpecialFile(fileName);
				sheet.addCell(new Label(c++, row.value, fileName,
						getContentFormat()));
				// 8.4 total translated text
				double p = Integer.parseInt(getTranslatedPercentage(""
						+ tp.getId())) / 100.0;
				sheet.addCell(new Label(c++, row.value, percent.format(p),
						getContentFormat()));
				row.inc();
			}
		}
	}

	/**
	 * 
	 * @return format of the report content
	 * @throws Exception
	 */
	private WritableCellFormat getContentFormat() throws Exception
	{
		if (contentFormat == null)
		{
			WritableCellFormat format = new WritableCellFormat();
			format.setWrap(true);
			format.setShrinkToFit(false);
			format.setAlignment(Alignment.LEFT);
			format.setVerticalAlignment(VerticalAlignment.CENTRE);

			contentFormat = format;
		}

		return contentFormat;
	}

	/**
	 * Gets the translated percentage
	 * 
	 * @param trgPageId
	 *            target page Id
	 * @return translated percentage
	 * @throws Exception
	 */
	public static String getTranslatedPercentage(String trgPageId)
			throws EnvoyServletException
	{
		int classTotal = 0;
		int translatedCounts = 0;
		long p_trgPageId = Long.parseLong(trgPageId);
		PageManager m_pageManager = ServerProxy.getPageManager();
		LeverageMatchLingManager m_lingManager = LingServerProxy
				.getLeverageMatchLingManager();
		TuvManager m_tuvManager = ServerProxy.getTuvManager();
		TargetPage targetPage = null;

		try
		{
			targetPage = m_pageManager.getTargetPage(p_trgPageId);
			SourcePage sourcePage = targetPage.getSourcePage();
			ArrayList targetTuvs = new ArrayList(m_tuvManager
					.getTargetTuvsForStatistics(targetPage));
			ArrayList sourceTuvs = new ArrayList(m_tuvManager
					.getSourceTuvsForStatistics(sourcePage));
			Long targetLocaleId = targetPage.getGlobalSightLocale()
					.getIdAsLong();
			
			m_lingManager.setIncludeMtMatches(true);
			MatchTypeStatistics tuvMatchTypes = m_lingManager
					.getMatchTypesForStatistics(sourcePage.getIdAsLong(),
							targetLocaleId, 0);

			for (int i = 0; i < targetTuvs.size(); i++)
			{
				Tuv sourceTuv = (Tuv) sourceTuvs.get(i);
				Tuv targetTuv = (Tuv) targetTuvs.get(i);

				if (targetTuv.isLocalized())
				{
					translatedCounts++;
				}
				else
				{
					int state2 = tuvMatchTypes.getLingManagerMatchType(
							sourceTuv.getId(),
							OnlineEditorManagerLocal.DUMMY_SUBID);

					switch (state2)
					{
						case LeverageMatchLingManager.EXACT:
							translatedCounts++;
							break;
						case LeverageMatchLingManager.UNVERIFIED:
							translatedCounts++;
							break;
					}

				}

				classTotal++;
			}
		}
		catch (EnvoyServletException e)
		{
			throw new EnvoyServletException(e);
		}
		catch (RemoteException e)
		{
			throw new EnvoyServletException(e);
		}

		int translatedPercentage = 100;
		if (classTotal != 0)
		{
			translatedPercentage = Math.round(BigDecimalHelper.divide(
					translatedCounts * 100, classTotal));
		}

		return String.valueOf(translatedPercentage);
	}

}