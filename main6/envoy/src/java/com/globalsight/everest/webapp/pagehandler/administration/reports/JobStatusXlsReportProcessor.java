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
import java.util.ResourceBundle;
import java.util.Vector;
import java.util.Collections;
import java.util.Date;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.Map;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.foundation.SearchCriteriaParameters;
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.jobhandler.JobSearchParameters;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.taskmanager.Task;
import com.globalsight.everest.taskmanager.TaskAssignee;
import com.globalsight.everest.taskmanager.TaskInfo;
import com.globalsight.everest.util.comparator.JobComparator;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.pagehandler.administration.users.UserUtil;
import com.globalsight.everest.workflow.Activity;
import com.globalsight.everest.workflow.EnvoyWorkItem;
import com.globalsight.everest.workflow.WorkflowConstants;
import com.globalsight.everest.workflow.WorkflowTaskInstance;
import com.globalsight.everest.workflowmanager.Workflow;
import com.globalsight.everest.workflowmanager.WorkflowOwner;
import com.globalsight.log.GlobalSightCategory;
import com.globalsight.util.IntHolder;

import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.format.CellFormat;
import jxl.format.Colour;
import jxl.format.Border;
import jxl.format.BorderLineStyle;
import jxl.format.UnderlineStyle;
import jxl.write.Label;
import jxl.write.Number;
import jxl.write.WritableCell;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

/**
 * Process the Job status excel report.
 * 
 * @author Silver.Chen
 * 
 */
public class JobStatusXlsReportProcessor implements ReportsProcessor
{
	private static GlobalSightCategory s_logger = (GlobalSightCategory) GlobalSightCategory
			.getLogger(REPORTS);

	private WritableWorkbook m_workbook = null;
	private Locale uiLocale = null;

	public JobStatusXlsReportProcessor()
	{
		s_logger.debug("Job Status Report..");
	}

	/*
     * @see ReportsProcessor#generateReport(HttpServletRequest,
     *      HttpServletResponse)
     */
	public void generateReport(HttpServletRequest p_request,
			HttpServletResponse p_response) throws Exception
	{
	    uiLocale = (Locale) p_request.getSession().getAttribute(
                WebAppConstants.UILOCALE);
		String companyName = UserUtil.getCurrentCompanyName(p_request);
		if (!UserUtil.isBlank(companyName))
		{
			CompanyThreadLocal.getInstance().setValue(companyName);
		}

		WorkbookSettings settings = new WorkbookSettings();
		settings.setSuppressWarnings(true);
		m_workbook = Workbook.createWorkbook(p_response.getOutputStream(),
				settings);
		addJobs(p_request);
		m_workbook.write();
		m_workbook.close();
	}

	/**
     * Gets the jobs and outputs workflow information.
     * 
     * @throws Exception
     */
	private void addJobs(HttpServletRequest p_request) throws Exception
	{
	    ResourceBundle bundle = PageHandler.getBundle(p_request.getSession());
		WritableSheet sheet = m_workbook.createSheet(bundle.getString("lb_job_status"), 0);
		addHeader(sheet, bundle);

		String[] jobIds = p_request.getParameterValues(PARAM_JOB_ID);
		List jobList = new ArrayList();

		if (jobIds != null && PARAM_SELECTED_ALL.equals(jobIds[0]))
		{
			// search jobs based on the params
			jobList.addAll(ServerProxy.getJobHandler().getJobs(
					getSearchParams(p_request)));
			// sort jobs by job name
			Collections.sort(jobList, new JobComparator(Locale.US));
		}
		else
		{
			// just get the chosen jobs
			for (int i = 0; i < jobIds.length; i++)
			{
				Job j = ServerProxy.getJobHandler().getJobById(
						Long.parseLong(jobIds[i]));
				jobList.add(j);
			}
		}

		String[] trgLocales = p_request
				.getParameterValues(PARAM_TARGET_LOCALES_LIST);
		boolean wantsAllLocales = false;
		Set trgLocaleList = new HashSet();
		if (trgLocales != null && !PARAM_SELECTED_ALL.equals(trgLocales[0]))
		{
			for (int i = 0; i < trgLocales.length; i++)
			{
				trgLocaleList.add(trgLocales[i]);
			}
		}
		else
		{
			wantsAllLocales = true;
		}

		Iterator jobIter = jobList.iterator();
		IntHolder row = new IntHolder(4);
		while (jobIter.hasNext())
		{
			Job j = (Job) jobIter.next();
			Collection c = j.getWorkflows();
			Iterator wfIter = c.iterator();
			while (wfIter.hasNext())
			{
				Workflow w = (Workflow) wfIter.next();
				String state = w.getState();
				// skip certain workflow whose target locale is not selected
				String trgLocale = w.getTargetLocale().toString();
				if (!wantsAllLocales && !trgLocaleList.contains(trgLocale))
				{
					continue;
				}
				if (Workflow.EXPORTED.equals(state)
						|| Workflow.DISPATCHED.equals(state)
						|| Workflow.LOCALIZED.equals(state)
						|| Workflow.EXPORT_FAILED.equals(state))
				{
					addWorkflow(p_request, sheet, j, w, row, bundle);
				}
			}
		}
	}

	/**
     * Gets the task for the workflow and outputs page information.
     * 
     * @param p_request
     * @param p_sheet
     * @param p_job
     * @param p_workflow
     * @param p_row
     * 
     * @throws Exception
     */
	private void addWorkflow(HttpServletRequest p_request,
			WritableSheet p_sheet, Job p_job, Workflow p_workflow,
			IntHolder p_row, ResourceBundle bundle) throws Exception
	{
		SimpleDateFormat dateFormat = new SimpleDateFormat(p_request
				.getParameter(PARAM_DATE_FORMAT));

		int c = 0;
		int r = p_row.getValue();

		// Job ID column
		p_sheet.addCell(new Label(c++, r, Long.toString(p_job.getJobId())));

		// Job Name column
		p_sheet.addCell(new Label(c++, r, p_job.getJobName()));

		// Target Language column
		p_sheet.addCell(new Label(c++, r, p_workflow.getTargetLocale()
				.getDisplayName(uiLocale)));

		// Word Count column
		p_sheet.addCell(new Number(c++, r, p_job.getWordCount()));

		// Job Kick off date column
		p_sheet.addCell(new Label(c++, r, dateFormat.format(p_job
				.getCreateDate())));

		// Date due to review column -- For the current workflow, find the
		// activity
		// called "Dell_Review", then insert the Estimated Completion Date (and
		// time)
		// for the activity prior to the "Dell_Review" activity.
		List taskInfos = ServerProxy.getWorkflowManager()
				.getTaskInfosInDefaultPath(p_workflow);
		if (taskInfos == null) taskInfos = new ArrayList();

		Iterator taskIter = taskInfos.iterator();

		Task priorTask = null;
		Task revTask = null;
		TaskInfo priorTaskInfo = null;
		TaskInfo revTaskInfo = null;

		while (taskIter.hasNext())
		{
			TaskInfo ti = (TaskInfo) taskIter.next();
			if (ti.getType() == Task.TYPE_REVIEW)
			{
				revTaskInfo = ti;
				revTask = ServerProxy.getTaskManager().getTask(
						revTaskInfo.getId());

				if (priorTaskInfo != null)
				{
					priorTask = ServerProxy.getTaskManager().getTask(
							priorTaskInfo.getId());
				}
				break;
			}
			else
			{
				priorTaskInfo = ti;
			}
		}

		if (revTask == null)
		{
			p_sheet.addCell(new Label(c++, r, bundle.getString("lb_no_review")));
			p_sheet.addCell(new Label(c++, r, bundle.getString("lb_no_review")));
		}
		else if (priorTaskInfo != null && priorTask != null)
		{
			if (priorTaskInfo.getCompleteByDate() != null)
				p_sheet.addCell(new Label(c++, r, dateFormat
						.format(priorTaskInfo.getCompleteByDate())));
			else
				p_sheet.addCell(new Label(c++, r, bundle.getString("lb_na")));

			// Actual date to Review column: Insert the date (and time) the
			// "Dell_Review"
			// activity became available for acceptance in the current workflow.
			if (priorTask.getCompletedDate() != null)
				p_sheet.addCell(new Label(c++, r, dateFormat.format(priorTask
						.getCompletedDate())));
			else
				p_sheet.addCell(new Label(c++, r, bundle.getString("lb_na")));
		}
		else
		{
			p_sheet.addCell(new Label(c++, r, bundle.getString("lb_na")));
			p_sheet.addCell(new Label(c++, r, bundle.getString("lb_na")));
		}

		HttpSession session = p_request.getSession();

		// Current Activity column: Currently active activity in the current
		// workflow.
		Map activeTasks = null;
		try
		{
			activeTasks = ServerProxy.getWorkflowServer()
					.getActiveTasksForWorkflow(session.getId(),
							p_workflow.getId());
		}
		catch (Exception e)
		{
			activeTasks = null;
			// just log the message since we don't want a full stack trace to
			// clog the log
			s_logger.error("Failed to get active tasks for workflow "
					+ p_workflow.getId() + " " + e.getMessage());
		}
		boolean wasExportFailed = false;

		// for now we'll only have one active task
		Object[] tasks = (activeTasks == null) ? null : activeTasks.values()
				.toArray();
		WorkflowTaskInstance activeTask = null;
		if (tasks != null && tasks.length > 0)
		{
			// assume just one active task for now
			activeTask = (WorkflowTaskInstance) tasks[0];
			// only write out if the Activity type is review only
			Activity a = activeTask.getActivity();
			p_sheet.addCell(new Label(c++, r, a.getDisplayName()));
		}
		else
		{
			String state = p_workflow.getState();
			if (Workflow.LOCALIZED.equals(state))
			{
				p_sheet.addCell(new Label(c++, r, bundle.getString("lb_localized")));
			}
			else if (Workflow.EXPORTED.equals(state))
			{
				p_sheet.addCell(new Label(c++, r, bundle.getString("lb_exported")));
			}
			else if (Workflow.EXPORT_FAILED.equals(state))
			{
				p_sheet.addCell(new Label(c++, r, bundle.getString("lb_exported_failed")));
				// if the workflow is EXPORT_FAILED, color the line in red
				wasExportFailed = true;
			}
			else if (Workflow.READY_TO_BE_DISPATCHED.equals(state))
			{
				p_sheet.addCell(new Label(c++, r, bundle.getString("lb_not_yet_dispatched")));
			}
			else
			{
				p_sheet.addCell(new Label(c++, r, bundle.getString("lb_unknown")));
			}
		}

		boolean wasRejected = false;
		String rejectorName = null;
		boolean revAccepted = false;
		// Reviewer accepted column: If the "Dell_Review" activity has been
		// accepted,
		// insert the date (and time) it was accepted. If not accepted yet,
		// put "not accepted yet".
		if (revTask != null)
		{
			if (revTask.getAcceptor() != null)
			{
				revAccepted = true;
				if (revTask.getAcceptedDate() != null)
				{
					p_sheet.addCell(new Label(c++, r, dateFormat.format(revTask
							.getAcceptedDate())));
				}
				else
				{
					p_sheet.addCell(new Label(c++, r, bundle.getString("lb_na")));
				}
			}
			else
			{
				// see if this task has been rejected and if so color the line
				// in red
				if (activeTask != null)
				{
					Vector workItems = activeTask.getWorkItems();
					for (int i = 0; i < workItems.size(); i++)
					{
						EnvoyWorkItem ewi = (EnvoyWorkItem) workItems.get(i);
						if (WorkflowConstants.TASK_DECLINED == ewi
								.getWorkItemState())
						{
							wasRejected = true;
							rejectorName = ewi.getAssignee();
							break;
						}
					}
				}
				if (wasRejected)
				{
					p_sheet.addCell(new Label(c++, r, bundle.getString("lb_rejected_by") + " "
							+ rejectorName, rejectedFormat()));
				}
				else
				{
					p_sheet.addCell(new Label(c++, r, bundle.getString("lb_not_accepted_yet")));
				}
			}
		}
		else
		{
			p_sheet.addCell(new Label(c++, r, bundle.getString("lb_no_review")));
		}
		// Reviewer name column: If not accepted, Insert all assignee(s)
		// assigned to the Dell_Review activity in the current workflow,
		// delimited by commas; add multiple names if there are multiple
		// assignees, until reaching 10 (cut it off at 10 names).
		// If activity is accepted, put in name of accepter.
		if (revTask == null)
		{
			p_sheet.addCell(new Label(c++, r, bundle.getString("lb_no_review")));
		}
		else if (revAccepted)
		{
			// get accepter somehow
			p_sheet.addCell(new Label(c++, r, revTask.getAcceptor()));
		}
		else
		{
			// get assignees
			List assigneeList = null;
			if (revTaskInfo != null)
			{
				assigneeList = revTaskInfo.getTaskAssignees();
			}
			if (assigneeList == null)
			{
				assigneeList = new ArrayList();
			}

			StringBuffer assignees = new StringBuffer();
			int count = 0;
			Iterator iter = assigneeList.iterator();
			while (iter.hasNext())
			{
				TaskAssignee assignee = (TaskAssignee) iter.next();
				assignees.append(assignee.getUserId());
				if (count++ == 10)
				{
					break;
				}
				else
				{
					if (iter.hasNext()) assignees.append(",");
				}
			}

			p_sheet.addCell(new Label(c++, r, assignees.toString()));
		}

		// Due reviewer complete column: Insert the Estimated Completion Date
		// (and time) for the "Dell_Review" activity.
		if (revTask != null && revTask.getEstimatedCompletionDate() != null)
			p_sheet.addCell(new Label(c++, r, dateFormat.format(revTask
					.getEstimatedCompletionDate())));
		else if (revTask != null)
			p_sheet.addCell(new Label(c++, r, bundle.getString("lb_na")));
		else
			p_sheet.addCell(new Label(c++, r, bundle.getString("lb_no_review")));

		// Actual reviewer complete column: Insert the Actual Completion Date
		// (and time)
		// for the "Dell_Review" activity.
		if (revTask != null && revTask.getCompletedDate() != null)
		{
			p_sheet.addCell(new Label(c++, r, dateFormat.format(revTask
					.getCompletedDate())));
		}
		else if (revTask != null)
		{
			if (wasRejected)
			{
				p_sheet.addCell(new Label(c++, r, bundle.getString("lb_rejected_by") + " "
						+ rejectorName, rejectedFormat()));
			}
			else
			{
				p_sheet.addCell(new Label(c++, r, bundle.getString("lb_not_yet_completed")));
			}
		}
		else
			p_sheet.addCell(new Label(c++, r, bundle.getString("lb_no_review")));

		// Estimated job completion column: Insert the estimated completion date
		// (and time)
		// for each workflow.
		if (p_workflow.getEstimatedCompletionDate() != null)
			p_sheet.addCell(new Label(c++, r, dateFormat.format(p_workflow
					.getEstimatedCompletionDate())));
		else
			p_sheet.addCell(new Label(c++, r, bundle.getString("lb_na")));

		// Actual job completion column: Insert the date (and time) the workflow
		// was exported.
		if (p_workflow.getCompletedDate() != null)
		{
			p_sheet.addCell(new Label(c++, r, dateFormat.format(p_workflow
					.getCompletedDate())));
		}
		else
		{
			if (wasRejected)
			{
				p_sheet.addCell(new Label(c++, r, bundle.getString("lb_rejected_by") + " "
						+ rejectorName, rejectedFormat()));
			}
			else
			{
				p_sheet.addCell(new Label(c++, r, bundle.getString("lb_not_yet_completed")));
			}
		}

		// Dell PM column. Insert the DELL PM name for the workflow.
		// These users are listed as Workflow Managers in each Workflow
		// Template.
		// So look up the workflow template used to create the desired workflow
		// instance
		// in the job being queried. Then find and retrieve the Workflow Manager
		// users
		// in that workflow template whose user profile contains the value of
		// "Dell PM"
		// in the Company field. If there are more than one, delimit by comma up
		// to 10 names.
		List workflowOwners = p_workflow.getWorkflowOwners();
		String currentCompanyName = CompanyWrapper.getCurrentCompanyName();
		if (workflowOwners != null && workflowOwners.size() > 0 && currentCompanyName != null)
		{
			Iterator ownerIter = workflowOwners.iterator();
			StringBuffer owners = new StringBuffer();
			boolean addedFirst = false;
			while (ownerIter.hasNext())
			{
				WorkflowOwner owner = (WorkflowOwner) ownerIter.next();
				User user = ServerProxy.getUserManager().getUser(
						owner.getOwnerId());
				if (currentCompanyName.equals(user.getCompanyName()))
				{
					if (addedFirst == false)
					{
						owners.append(owner.getOwnerId());
						addedFirst = true;
					}
					else
					{
						owners.append(",").append(owner.getOwnerId());
					}
				}
			}

			p_sheet.addCell(new Label(c, r, owners.toString(), wrapFormat()));
		}
		else
		{
			p_sheet.addCell(new Label(c, r, bundle.getString("lb_na")));
		}

		p_row.inc();

		if (wasExportFailed)
		{
			WritableCell wc = null;
			for (int i = 0; i <= c; i++)
			{
				wc = p_sheet.getWritableCell(i, r);
				if (i == 3)
					p_sheet.addCell(new Number(i, r, new Double(wc
							.getContents()).doubleValue(), redFormat()));
				else
					p_sheet.addCell(new Label(i, r, wc.getContents(),
							redFormat()));
			}
		}
	}

	private CellFormat rejectedFormat() throws Exception
	{
		WritableFont rejectedFont = new WritableFont(WritableFont.TIMES, 11,
				WritableFont.BOLD, false, UnderlineStyle.NO_UNDERLINE,
				Colour.RED);
		WritableCellFormat rejectedFormat = new WritableCellFormat(rejectedFont);
		rejectedFormat.setWrap(true);

		return rejectedFormat;
	}

	private CellFormat redFormat() throws Exception
	{
		WritableFont redFont = new WritableFont(WritableFont.ARIAL, 10,
				WritableFont.NO_BOLD, false, UnderlineStyle.NO_UNDERLINE,
				Colour.BLACK);
		WritableCellFormat redFormat = new WritableCellFormat(redFont);
		redFormat.setBackground(Colour.RED);
		redFormat.setWrap(true);

		return redFormat;
	}

	/**
	 * Returns a cell format that the wrap has been set to true.
	 */
	private CellFormat wrapFormat() throws Exception
	{
		WritableCellFormat wrapFormat = new WritableCellFormat();
		wrapFormat.setWrap(true);
		
		return wrapFormat;
	}
	
	/**
     * Returns search params used to find the in progress jobs for all PMs
     * 
     * @return JobSearchParams
     */
	private JobSearchParameters getSearchParams(HttpServletRequest p_request)
	{
		JobSearchParameters sp = new JobSearchParameters();

		String[] status = p_request.getParameterValues(PARAM_STATUS);
		// search by statusList
		List statusList = new ArrayList();
		if (status != null && !PARAM_SELECTED_ALL.equals(status[0]))
		{
			for (int i = 0; i < status.length; i++)
			{
				statusList.add(status[i]);
			}
		}
		else
		{
			// just do a query for all in progress jobs, localized, exported and
			// export failed.
			statusList.add(Job.DISPATCHED);
			statusList.add(Job.LOCALIZED);
			statusList.add(Job.EXPORTED);
			statusList.add(Job.EXPORT_FAIL);
		}
		sp.setJobState(statusList);

		String[] projectIds = p_request.getParameterValues(PARAM_PROJECT_ID);
		// search by project
		List projectIdList = new ArrayList();

		if (projectIds != null && !PARAM_SELECTED_ALL.equals(projectIds[0]))
		{
			for (int i = 0; i < projectIds.length; i++)
			{
				projectIdList.add(new Long(projectIds[i]));
			}
			sp.setProjectId(projectIdList);
		}

		String createDateStartCount = p_request
				.getParameter(PARAM_CREATION_START);
		String createDateStartOpts = p_request
				.getParameter(PARAM_CREATION_START_OPTIONS);

		if (!PARAM_SELECTED_NONE.equals(createDateStartOpts))
		{
			sp.setCreationStart(new Integer(createDateStartCount));
			sp.setCreationStartCondition(createDateStartOpts);
		}

		String createDateEndCount = p_request.getParameter(PARAM_CREATION_END);
		String createDateEndOpts = p_request
				.getParameter(PARAM_CREATION_END_OPTIONS);

		if (SearchCriteriaParameters.NOW.equals(createDateEndOpts))
		{
			sp.setCreationEnd(new Date());
		}
		else if (!PARAM_SELECTED_NONE.equals(createDateEndOpts))
		{
			sp.setCreationEnd(new Integer(createDateEndCount));
			sp.setCreationEndCondition(createDateEndOpts);
		}

		return sp;
	}

	/**
     * Adds the table header to the sheet
     * 
     * @param p_sheet
     *            the sheet to be created in the report
     * 
     * @throws Exception
     */
	private void addHeader(WritableSheet p_sheet, ResourceBundle bundle) throws Exception
	{
		String EMEA = CompanyWrapper.getCurrentCompanyName();
		// title font is set black bold on white
		WritableFont titleFont = new WritableFont(WritableFont.TIMES, 14,
				WritableFont.BOLD, false, UnderlineStyle.NO_UNDERLINE,
				Colour.BLACK);
		WritableCellFormat titleFormat = new WritableCellFormat(titleFont);
		titleFormat.setWrap(false);
		titleFormat.setShrinkToFit(false);
		
		p_sheet.addCell(new Label(0, 0, EMEA + " " + bundle.getString("lb_job_status"), titleFormat));
		p_sheet.setColumnView(0, 22);

		// headerFont is set black bold on light grey
		WritableFont headerFont = new WritableFont(WritableFont.TIMES, 11,
				WritableFont.BOLD, false, UnderlineStyle.NO_UNDERLINE,
				Colour.BLACK);
		WritableCellFormat headerFormat = new WritableCellFormat(headerFont);
		headerFormat.setWrap(true);
		headerFormat.setBackground(Colour.GRAY_25);
		headerFormat.setShrinkToFit(false);
		headerFormat.setBorder(Border.ALL, BorderLineStyle.THICK, Colour.BLACK);

		int col = 0;
		int row = 3;
		p_sheet.addCell(new Label(col++, row, bundle.getString("jobinfo.jobid"), headerFormat));
		p_sheet.setColumnView(col - 1, 10);
		p_sheet.addCell(new Label(col++, row, bundle.getString("lb_job"), headerFormat));
		p_sheet.setColumnView(col - 1, 50);
		p_sheet.addCell(new Label(col++, row, bundle.getString("lb_lang"), headerFormat));
		p_sheet.setColumnView(col - 1, 25);
		p_sheet.addCell(new Label(col++, row, bundle.getString("lb_word_count"), headerFormat));
		p_sheet.addCell(new Label(col++, row, bundle.getString("lb_job_kickoff_date"),
				headerFormat));
		p_sheet.setColumnView(col - 1, 25);

		p_sheet.addCell(new Label(col++, row, bundle.getString("lb_date_due_to_review"),
				headerFormat));
		p_sheet.setColumnView(col - 1, 25);
		p_sheet.addCell(new Label(col++, row, bundle.getString("lb_actual_date_to_review"),
				headerFormat));
		p_sheet.setColumnView(col - 1, 25);
		p_sheet.addCell(new Label(col++, row, bundle.getString("lb_current_activity"),
				headerFormat));
		p_sheet.setColumnView(col - 1, 20);
		p_sheet.addCell(new Label(col++, row, bundle.getString("lb_reviewer_accepted"),
				headerFormat));
		p_sheet.setColumnView(col - 1, 25);
		p_sheet.addCell(new Label(col++, row, bundle.getString("lb_reviewer_name"), headerFormat));
		p_sheet.setColumnView(col - 1, 20);
		p_sheet.addCell(new Label(col++, row, bundle.getString("lb_Due_reviewer_complete"),
				headerFormat));
		p_sheet.setColumnView(col - 1, 25);
		p_sheet.addCell(new Label(col++, row, bundle.getString("lb_actual_reviewer_complete"),
				headerFormat));
		p_sheet.setColumnView(col - 1, 25);
		p_sheet.addCell(new Label(col++, row, bundle.getString("jobinfo.status.estimatedjobcompletion"),
				headerFormat));
		p_sheet.setColumnView(col - 1, 25);
		p_sheet.addCell(new Label(col++, row, bundle.getString("jobinfo.status.actualjobcompletion"),
				headerFormat));
		p_sheet.setColumnView(col - 1, 25);
		p_sheet.addCell(new Label(col++, row, bundle.getString("lb_pm"), headerFormat));
		p_sheet.setColumnView(col - 1, 25);
	}
}
