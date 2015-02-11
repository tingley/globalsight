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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.Vector;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.jobhandler.JobSearchParameters;
import com.globalsight.everest.projecthandler.Project;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.taskmanager.Task;
import com.globalsight.everest.taskmanager.TaskAssignee;
import com.globalsight.everest.taskmanager.TaskInfo;
import com.globalsight.everest.util.comparator.JobComparator;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.pagehandler.administration.reports.bo.ReportsData;
import com.globalsight.everest.webapp.pagehandler.administration.reports.generator.ReportGeneratorHandler;
import com.globalsight.everest.webapp.pagehandler.administration.users.UserUtil;
import com.globalsight.everest.workflow.Activity;
import com.globalsight.everest.workflow.EnvoyWorkItem;
import com.globalsight.everest.workflow.WorkflowConstants;
import com.globalsight.everest.workflow.WorkflowTaskInstance;
import com.globalsight.everest.workflowmanager.Workflow;
import com.globalsight.everest.workflowmanager.WorkflowOwner;
import com.globalsight.ling.tm2.persistence.DbUtil;
import com.globalsight.util.IntHolder;
import com.globalsight.util.SortUtil;
import com.globalsight.util.StringUtil;

/**
 * Process the Job status excel report.
 * 
 * @author Silver.Chen
 * 
 */
public class JobStatusXlsReportProcessor implements ReportsProcessor
{
    private static Logger s_logger = Logger.getLogger(REPORTS);
//    private static Map<String, ReportsData> m_reportsDataMap = new ConcurrentHashMap<String, ReportsData>();
    private Locale uiLocale = Locale.US;
    private ResourceBundle bundle = null;
    private String userId = null;

    private CellStyle titleStyle = null;
    private CellStyle headerStyle = null;
    private CellStyle contentStyle = null;
    private CellStyle redStyle = null;
    private CellStyle rejectedStyle = null;
    private CellStyle wrapStyle = null;

    SimpleDateFormat dateFormat = null;
    private List<Long> m_jobIDS = null;

    public JobStatusXlsReportProcessor()
    {
        s_logger.debug("Job Status Report..");
    }

    /*
     * @see ReportsProcessor#generateReport(HttpServletRequest,
     * HttpServletResponse)
     */
    public void generateReport(HttpServletRequest p_request,
            HttpServletResponse p_response) throws Exception
    {
        dateFormat = new SimpleDateFormat(
                p_request.getParameter(PARAM_DATE_FORMAT));
        uiLocale = (Locale) p_request.getSession().getAttribute(
                WebAppConstants.UILOCALE);
        bundle = PageHandler.getBundle(p_request.getSession());
        userId = (String) p_request.getSession(false).getAttribute(
                WebAppConstants.USER_NAME);
        String companyName = UserUtil.getCurrentCompanyName(p_request);
        if (!UserUtil.isBlank(companyName))
        {
            CompanyThreadLocal.getInstance().setValue(companyName);
        }

        Workbook p_workbook = new SXSSFWorkbook();
        Sheet sheet = p_workbook.createSheet(bundle.getString("lb_job_status"));

        // Add Title
        addTitle(p_workbook, sheet);

        // add header
        addHeader(p_workbook, sheet);

        //
        addJobs(p_workbook, sheet, p_request, p_response);

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

    private void addTitle(Workbook p_workbook, Sheet p_sheet)
    {
        String EMEA = CompanyWrapper.getCurrentCompanyName();
        Row titleRow = getRow(p_sheet, 0);
        Cell titleCell = getCell(titleRow, 0);
        titleCell.setCellValue(EMEA + " " + bundle.getString("lb_job_status"));
        titleCell.setCellStyle(getTitleStyle(p_workbook));
        p_sheet.setColumnWidth(0, 22 * 256);
    }

    /**
     * Adds the table header to the sheet
     * 
     * @param p_sheet
     *            the sheet to be created in the report
     * 
     * @throws Exception
     */
    private void addHeader(Workbook p_workbook, Sheet p_sheet)throws Exception
    {
        CellStyle headerCs = getHeaderStyle(p_workbook);

        int col = 0;
        Row headerRow = getRow(p_sheet, 3);
        
        Cell cell_A = getCell(headerRow, col++);
        cell_A.setCellValue(bundle.getString("jobinfo.jobid"));
        cell_A.setCellStyle(headerCs);
        p_sheet.setColumnWidth(col - 1, 10 * 256);
        
        Cell cell_B = getCell(headerRow, col++);
        cell_B.setCellValue(bundle.getString("lb_job"));
        cell_B.setCellStyle(headerCs);
        p_sheet.setColumnWidth(col - 1, 50 * 256);
        
        Cell cell_C = getCell(headerRow, col++);
        cell_C.setCellValue(bundle.getString("lb_lang"));
        cell_C.setCellStyle(headerCs);
        p_sheet.setColumnWidth(col - 1, 25 * 256);
        
        Cell cell_D = getCell(headerRow, col++);
        cell_D.setCellValue(bundle.getString("lb_word_count"));
        cell_D.setCellStyle(headerCs);
        p_sheet.setColumnWidth(col - 1, 20 * 256);
        
        Cell cell_E = getCell(headerRow, col++);
        cell_E.setCellValue(bundle.getString("lb_job_kickoff_date"));
        cell_E.setCellStyle(headerCs);
        p_sheet.setColumnWidth(col - 1, 25 * 256);
        
        Cell cell_F = getCell(headerRow, col++);
        cell_F.setCellValue(bundle.getString("lb_date_due_to_review"));
        cell_F.setCellStyle(headerCs);
        p_sheet.setColumnWidth(col - 1, 25 * 256);

        Cell cell_G = getCell(headerRow, col++);
        cell_G.setCellValue(bundle.getString("lb_actual_date_to_review"));
        cell_G.setCellStyle(headerCs);
        p_sheet.setColumnWidth(col - 1, 25 * 256);
        
        Cell cell_H = getCell(headerRow, col++);
        cell_H.setCellValue(bundle.getString("lb_current_activity"));
        cell_H.setCellStyle(headerCs);
        p_sheet.setColumnWidth(col - 1, 20 * 256);
        
        Cell cell_I = getCell(headerRow, col++);
        cell_I.setCellValue(bundle.getString("lb_reviewer_accepted"));
        cell_I.setCellStyle(headerCs);
        p_sheet.setColumnWidth(col - 1, 25 * 256);
        
        Cell cell_J = getCell(headerRow, col++);
        cell_J.setCellValue(bundle.getString("lb_reviewer_name"));
        cell_J.setCellStyle(headerCs);
        p_sheet.setColumnWidth(col - 1, 20 * 256);
        
        Cell cell_K = getCell(headerRow, col++);
        cell_K.setCellValue(bundle.getString("lb_Due_reviewer_complete"));
        cell_K.setCellStyle(headerCs);
        p_sheet.setColumnWidth(col - 1, 25 * 256);
        
        Cell cell_L = getCell(headerRow, col++);
        cell_L.setCellValue(bundle.getString("lb_actual_reviewer_complete"));
        cell_L.setCellStyle(headerCs);
        p_sheet.setColumnWidth(col - 1, 25 * 256);
        
        Cell cell_M = getCell(headerRow, col++);
        cell_M.setCellValue(bundle.getString("jobinfo.status.estimatedjobcompletion"));
        cell_M.setCellStyle(headerCs);
        p_sheet.setColumnWidth(col - 1, 25 * 256);
        
        Cell cell_N = getCell(headerRow, col++);
        cell_N.setCellValue(bundle.getString("jobinfo.status.actualjobcompletion"));
        cell_N.setCellStyle(headerCs);
        p_sheet.setColumnWidth(col - 1, 25 * 256);
        
        Cell cell_O = getCell(headerRow, col++);
        cell_O.setCellValue(bundle.getString("lb_pm"));
        cell_O.setCellStyle(headerCs);
        p_sheet.setColumnWidth(col - 1, 25 * 256);
    }

    /**
     * Gets the jobs and outputs workflow information.
     * 
     * @throws Exception
     */
    private void addJobs(Workbook p_workbook, Sheet p_sheet,
            HttpServletRequest p_request, HttpServletResponse p_response)
            throws Exception
    {
        String[] jobIds = p_request.getParameterValues(PARAM_JOB_ID);
        List<Job> jobList = new ArrayList<Job>();

        if (jobIds != null && PARAM_SELECTED_ALL.equals(jobIds[0]))
        {
            // search jobs based on the params
            jobList.addAll(ServerProxy.getJobHandler().getJobs(
                    getSearchParams(p_request)));
            // sort jobs by job name
            SortUtil.sort(jobList, new JobComparator(Locale.US));
        }
        else
        {
            // just get the selected jobs
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
        Set<String> trgLocaleList = new HashSet<String>();
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

        m_jobIDS = ReportHelper.getJobIDS(jobList);
        // Cancel Duplicate Request
        if (ReportHelper.checkReportsDataInProgressStatus(userId,
                m_jobIDS, getReportType()))
        {
            p_workbook = null;
            p_response.sendError(HttpServletResponse.SC_NO_CONTENT);
            return;
        }
        // Set m_reportsDataMap.
        ReportHelper.setReportsData(userId, m_jobIDS, getReportType(),
                0, ReportsData.STATUS_INPROGRESS);

        IntHolder row = new IntHolder(4);
        for (Job j : jobList)
        {
            for (Workflow w : j.getWorkflows())
            {
                if (isCancelled())
                {
                    p_workbook = null;
                    return;
                }
                String state = w.getState();
                // skip certain workflow whose target locale is not selected
                String trgLocale = Long.toString(w.getTargetLocale().getId());
                if (!wantsAllLocales && !trgLocaleList.contains(trgLocale))
                {
                    continue;
                }
                if (Workflow.READY_TO_BE_DISPATCHED.equals(state)
                        || Workflow.EXPORTED.equals(state)
                        || Workflow.DISPATCHED.equals(state)
                        || Workflow.LOCALIZED.equals(state)
                        || Workflow.EXPORT_FAILED.equals(state)
                        || Workflow.ARCHIVED.equals(state))
                {
                    addWorkflow(p_request, p_workbook, p_sheet, j, w, row, bundle);
                }
            }
        }

        // Set m_reportsDataMap.
        ReportHelper.setReportsData(userId, m_jobIDS, getReportType(),
                100, ReportsData.STATUS_FINISHED);
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
            Workbook p_workbook, Sheet p_sheet, Job p_job, Workflow p_workflow,
            IntHolder p_row, ResourceBundle bundle) throws Exception
    {
        int c = 0;
        Row row = getRow(p_sheet, p_row.getValue());

        // Job ID column
        Cell cell_A = getCell(row, c++);
        cell_A.setCellValue(p_job.getJobId());
        cell_A.setCellStyle(getContentStyle(p_workbook));

        // Job Name column
        Cell cell_B = getCell(row, c++);
        cell_B.setCellValue(p_job.getJobName());
        cell_B.setCellStyle(getContentStyle(p_workbook));

        // Target Language column
        Cell cell_C = getCell(row, c++);
        cell_C.setCellValue(p_workflow.getTargetLocale()
                .getDisplayName(uiLocale));
        cell_C.setCellStyle(getContentStyle(p_workbook));

        // Word Count column
        Cell cell_D = getCell(row, c++);
        cell_D.setCellValue(p_workflow.getTotalWordCount());
        cell_D.setCellStyle(getContentStyle(p_workbook));

        // Job Kick off date column
        Cell cell_E = getCell(row, c++);
        cell_E.setCellValue(dateFormat.format(p_job.getCreateDate()));
        cell_E.setCellStyle(getContentStyle(p_workbook));

        // Date due to review column -- For the current workflow, find the
        // activity called "Dell_Review", then insert the Estimated Completion
        // Date (and time) for the activity prior to the "Dell_Review" activity.
        Task priorTask = null;
        Task revTask = null;
        TaskInfo priorTaskInfo = null;
        TaskInfo revTaskInfo = null;
        List<TaskInfo> taskInfos = ServerProxy.getWorkflowManager()
                .getTaskInfosInDefaultPath(p_workflow);
        if (taskInfos == null)
            taskInfos = new ArrayList<TaskInfo>();
        for (TaskInfo ti : taskInfos)
        {
            if (ti.getType() == Task.TYPE_REVIEW)
            {
                revTaskInfo = ti;
                revTask = (Task) p_workflow.getTasks().get(revTaskInfo.getId());
                if (priorTaskInfo != null)
                {
                    priorTask = (Task) p_workflow.getTasks().get(
                            priorTaskInfo.getId());
                }
                break;
            }
            else
            {
                priorTaskInfo = ti;
            }
        }

        Cell cell_F = getCell(row, c++);;
        Cell cell_G = getCell(row, c++);;
        if (revTask == null)
        {
            cell_F.setCellValue(bundle.getString("lb_no_review"));
            cell_F.setCellStyle(getContentStyle(p_workbook));

            cell_G.setCellValue(bundle.getString("lb_no_review"));
            cell_G.setCellStyle(getContentStyle(p_workbook));
        }
        else if (priorTaskInfo != null && priorTask != null)
        {
            if (priorTaskInfo.getCompleteByDate() != null)
            {
                cell_F.setCellValue(dateFormat
                        .format(priorTaskInfo.getCompleteByDate()));
                cell_F.setCellStyle(getContentStyle(p_workbook));
            }
            else
            {
                cell_F.setCellValue(bundle.getString("lb_na"));
                cell_F.setCellStyle(getContentStyle(p_workbook));
            }

            // Actual date to Review column: Insert the date (and time) the
            // "Dell_Review"
            // activity became available for acceptance in the current workflow.
            if (priorTask.getCompletedDate() != null)
            {
                cell_G.setCellValue(dateFormat.format(priorTask
                        .getCompletedDate()));
                cell_G.setCellStyle(getContentStyle(p_workbook));
            }
            else
            {
            	cell_G.setCellValue(bundle.getString("lb_na"));
            	cell_G.setCellStyle(getContentStyle(p_workbook));
            }
        }
        else
        {
            cell_F.setCellValue(bundle.getString("lb_na"));
            cell_F.setCellStyle(getContentStyle(p_workbook));

            cell_G.setCellValue(bundle.getString("lb_na"));
            cell_G.setCellStyle(getContentStyle(p_workbook));
        }

        // Current Activity column: Currently active activity in the current
        // workflow.
        Map<Long, WorkflowTaskInstance> activeTasks = null;
        try
        {
            activeTasks = ServerProxy.getWorkflowServer()
                    .getActiveTasksForWorkflow(p_workflow.getId());
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

        Cell cell_H = getCell(row, c++);
        if (tasks != null && tasks.length > 0)
        {
            // assume just one active task for now
            activeTask = (WorkflowTaskInstance) tasks[0];
            // only write out if the Activity type is review only
            Activity a = activeTask.getActivity();
            cell_H.setCellValue(a.getDisplayName());
            cell_H.setCellStyle(getContentStyle(p_workbook));
        }
        else
        {
            String state = p_workflow.getState();
            if (Workflow.LOCALIZED.equals(state))
            {
            	cell_H.setCellValue(bundle.getString("lb_localized"));
            	cell_H.setCellStyle(getContentStyle(p_workbook));
            }
            else if (Workflow.EXPORTED.equals(state))
            {
                cell_H.setCellValue(bundle.getString("lb_exported"));
                cell_H.setCellStyle(getContentStyle(p_workbook));
            }
            else if (Workflow.EXPORT_FAILED.equals(state))
            {
                cell_H.setCellValue(bundle.getString("lb_exported_failed"));
                cell_H.setCellStyle(getContentStyle(p_workbook));
                // if the workflow is EXPORT_FAILED, color the line in red
                wasExportFailed = true;
            }
            else if (Workflow.READY_TO_BE_DISPATCHED.equals(state))
            {
            	cell_H.setCellValue(bundle.getString("lb_not_yet_dispatched"));
            	cell_H.setCellStyle(getContentStyle(p_workbook));
            }
            else if (Workflow.ARCHIVED.equals(state))
            {
            	cell_H.setCellValue(bundle.getString("lb_archived"));
            	cell_H.setCellStyle(getContentStyle(p_workbook));
            }
            else
            {
            	cell_H.setCellValue(bundle.getString("lb_unknown"));
            	cell_H.setCellStyle(getContentStyle(p_workbook));
            }
        }

        boolean wasRejected = false;
        String rejectorName = null;
        boolean revAccepted = false;
        // Reviewer accepted column: If the "Dell_Review" activity has been
        // accepted, insert the date (and time) it was accepted. If not accepted
        // yet, put "not accepted yet".
        Cell cell_I = getCell(row, c++);
        if (revTask != null)
        {
            if (revTask.getAcceptor() != null)
            {
                revAccepted = true;
                if (revTask.getAcceptedDate() != null)
                {
                	cell_I.setCellValue(dateFormat.format(revTask
                            .getAcceptedDate()));
                	cell_I.setCellStyle(getContentStyle(p_workbook));
                }
                else
                {
                	cell_I.setCellValue(bundle.getString("lb_na"));
                	cell_I.setCellStyle(getContentStyle(p_workbook));
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
                            rejectorName = UserUtil.getUserNameById(ewi
                                    .getAssignee());
                            break;
                        }
                    }
                }
                if (wasRejected)
                {
                	cell_I.setCellValue(bundle
                            .getString("lb_rejected_by") + " " + rejectorName);
                	cell_I.setCellStyle(getRejectedStyle(p_workbook));
                }
                else
                {
                	cell_I.setCellValue(bundle
                            .getString("lb_not_accepted_yet"));
                	cell_I.setCellStyle(getContentStyle(p_workbook));
                }
            }
        }
        else
        {
        	cell_I.setCellValue(bundle.getString("lb_no_review"));
        	cell_I.setCellStyle(getContentStyle(p_workbook));
        }

        // Reviewer name column: If not accepted, Insert all assignee(s)
        // assigned to the Dell_Review activity in the current workflow,
        // delimited by commas; add multiple names if there are multiple
        // assignees, until reaching 10 (cut it off at 10 names).
        // If activity is accepted, put in name of accepter.
        Cell cell_J = getCell(row, c++);
        if (revTask == null)
        {
            cell_J.setCellValue(bundle.getString("lb_no_review"));
            cell_J.setCellStyle(getContentStyle(p_workbook));
        }
        else if (revAccepted)
        {
            // get accepter somehow
        	cell_J.setCellValue(UserUtil.getUserNameById(revTask.getAcceptor()));
        	cell_J.setCellStyle(getContentStyle(p_workbook));
        }
        else
        {
            String reviewerName = getReviewerName(revTaskInfo); 
            cell_J.setCellValue(reviewerName);
            cell_J.setCellStyle(getContentStyle(p_workbook));
        }

        // Due reviewer complete column: Insert the Estimated Completion Date
        // (and time) for the "Dell_Review" activity.
        Cell cell_K = getCell(row, c++);
        if (revTask != null && revTask.getEstimatedCompletionDate() != null)
        {
            cell_K.setCellValue(dateFormat.format(revTask
                    .getEstimatedCompletionDate()));
            cell_K.setCellStyle(getContentStyle(p_workbook));
        }
        else if (revTask != null)
        {
        	cell_K.setCellValue(bundle.getString("lb_na"));
        	cell_K.setCellStyle(getContentStyle(p_workbook));
        }
        else
        {
        	cell_K.setCellValue(bundle.getString("lb_no_review"));
        	cell_K.setCellStyle(getContentStyle(p_workbook));
        }

        // Actual reviewer complete column: Insert the Actual Completion Date
        // (and time)
        // for the "Dell_Review" activity.
        Cell cell_L = getCell(row, c++);
        if (revTask != null && revTask.getCompletedDate() != null)
        {
            cell_L.setCellValue(dateFormat.format(revTask
                    .getCompletedDate()));
            cell_L.setCellStyle(getContentStyle(p_workbook));
        }
        else if (revTask != null)
        {
            if (wasRejected)
            {
            	cell_L.setCellValue(bundle
                        .getString("lb_rejected_by") + " " + rejectorName);
            	cell_L.setCellStyle(getRejectedStyle(p_workbook));
            }
            else
            {
            	cell_L.setCellValue(bundle.getString("lb_not_yet_completed"));
            	cell_L.setCellStyle(getContentStyle(p_workbook));
            }
        }
        else
        {
        	cell_L.setCellValue(bundle.getString("lb_no_review"));
        	cell_L.setCellStyle(getContentStyle(p_workbook));
        }

        // Estimated job completion column: Insert the estimated completion date
        // (and time) for each workflow.
        Cell cell_M = getCell(row, c++);
        if (p_workflow.getEstimatedCompletionDate() != null)
        {
            cell_M.setCellValue(dateFormat.format(p_workflow
        			.getEstimatedCompletionDate()));
            cell_M.setCellStyle(getContentStyle(p_workbook));
        }
        else
        {
        	cell_M.setCellValue(bundle.getString("lb_na"));
        	cell_M.setCellStyle(getContentStyle(p_workbook));
        }

        // Actual job completion column: Insert the date (and time) the workflow
        // was exported.
        Cell cell_N = getCell(row, c++);
        if (p_workflow.getCompletedDate() != null)
        {
            cell_N.setCellValue(dateFormat.format(p_workflow
                    .getCompletedDate()));
            cell_N.setCellStyle(getContentStyle(p_workbook));
        }
        else
        {
            if (wasRejected)
            {
            	cell_N.setCellValue(bundle
                        .getString("lb_rejected_by") + " " + rejectorName);
            	cell_N.setCellStyle(getRejectedStyle(p_workbook));
            }
            else
            {
            	cell_N.setCellValue(bundle.getString("lb_not_yet_completed"));
            	cell_N.setCellStyle(getContentStyle(p_workbook));
            }
        }

        // Dell PM column
        Cell cell_O = getCell(row, c++);
        String pmString = getPMColumnString(p_workflow);
        if (!StringUtil.isEmpty(pmString))
        {
            cell_O.setCellValue(pmString);
            cell_O.setCellStyle(getWrapStyle(p_workbook));
        }
        else
        {
        	cell_O.setCellValue(bundle.getString("lb_na"));
        	cell_O.setCellStyle(getContentStyle(p_workbook));
        }

        p_row.inc();

        // Rewrite current row in "RED" style.
        if (wasExportFailed)
        {
            Cell cell = null;
            for (int i = 0; i < c; i++)
            {
                cell = getCell(row, i);
                cell.setCellStyle(getRedStyle(p_workbook));
            }
        }
    }

    /**
     * Get current active activity display name for specified workflow.
     * @param p_workflowId
     * @return
     */
    private String getActiveActivityName(long p_workflowId)
    {
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try
        {
            con = DbUtil.getConnection();
            String sql = " SELECT a.display_name "
                       + " FROM activity a, " 
                       + " (SELECT ti.name FROM task_info ti WHERE ti.workflow_ID = ? AND ti.state = 'ACTIVE') AS b "
                       + " WHERE a.name = b.name";
            ps = con.prepareStatement(sql);
            ps.setLong(1, p_workflowId);
            rs = ps.executeQuery();
            if (rs == null)
                return null;
            if (rs.next())
            {
                return rs.getString(1);
            }
        }
        catch (Exception e)
        {
            s_logger.error("Fail to get active activity name for workflowId "
                    + p_workflowId, e);
        }
        finally
        {
            DbUtil.silentClose(rs);
            DbUtil.silentClose(ps);
            DbUtil.silentReturnConnection(con);
        }

        return null;
    }

    // For "Reviewer Name" column (index : J)
    private String getReviewerName(TaskInfo ti)
    {
        if (ti == null)
            return "";

        List<TaskAssignee> assigneeList = ti.getTaskAssignees();
        if (assigneeList == null) {
            assigneeList = new ArrayList<TaskAssignee>();
        }

        StringBuffer assignees = new StringBuffer();
        int count = 0;
        Iterator iter = assigneeList.iterator();
        while (iter.hasNext())
        {
            TaskAssignee assignee = (TaskAssignee) iter.next();
            assignees.append(UserUtil.getUserNameById(assignee.getUserId()));
            if (count++ == 10)
            {
                break;
            }
            else
            {
                if (iter.hasNext())
                    assignees.append(",");
            }
        }

        return assignees.toString();
    }

    // For "PM" column (index: O)
    private String getPMColumnString(Workflow p_workflow)
    {
        StringBuffer owners = new StringBuffer();

        try
        {
            String currentCompanyName = CompanyWrapper.getCurrentCompanyName();
            List workflowOwners = p_workflow.getWorkflowOwners();
            if (workflowOwners != null && workflowOwners.size() > 0
                    && currentCompanyName != null)
            {
                Iterator ownerIter = workflowOwners.iterator();
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
                            owners.append(UserUtil.getUserNameById(owner
                                    .getOwnerId()));
                            addedFirst = true;
                        }
                        else
                        {
                            owners.append(",")
                                    .append(UserUtil.getUserNameById(owner
                                            .getOwnerId()));
                        }
                    }
                }
            }
        }
        catch (Exception ignore)
        {
            // this should not break the report generating for other jobs.
            return null;
        }

        return owners.toString();
    }

    /**
     * Returns search params used to find the in progress jobs for all PMs
     * 
     * @return JobSearchParams
     */
    private JobSearchParameters getSearchParams(HttpServletRequest p_request)
    {
        JobSearchParameters sp = new JobSearchParameters();
        SimpleDateFormat simpleDate = new SimpleDateFormat("MM/dd/yyyy");
        
        String[] status = p_request.getParameterValues(PARAM_STATUS);
        // search by statusList
        List<String> statusList = new ArrayList<String>();
        if (status != null && !PARAM_SELECTED_ALL.equals(status[0]))
        {
            for (int i = 0; i < status.length; i++)
            {
                statusList.add(status[i]);
            }
        }
        else
        {
            // just do a query for all ready, in progress, localized, exported,
            // archived and export failed jobs.
            statusList.add(Job.READY_TO_BE_DISPATCHED);
            statusList.add(Job.DISPATCHED);
            statusList.add(Job.LOCALIZED);
            statusList.add(Job.EXPORTED);
            statusList.add(Job.EXPORT_FAIL);
            statusList.add(Job.ARCHIVED);
        }
        sp.setJobState(statusList);

        String[] projectIds = p_request.getParameterValues(PARAM_PROJECT_ID);
        // search by project
        List<Long> projectIdList = new ArrayList<Long>();

        if (projectIds != null && !PARAM_SELECTED_ALL.equals(projectIds[0]))
        {
            for (int i = 0; i < projectIds.length; i++)
            {
                projectIdList.add(new Long(projectIds[i]));
            }
        }
        else
        {
            try
            {
                List<Project> projectList = (ArrayList<Project>) ServerProxy
                        .getProjectHandler().getProjectsByUser(userId);
                for (Project project : projectList)
                {
                    projectIdList.add(project.getIdAsLong());
                }
            }
            catch (Exception e)
            {
            }
        }
        sp.setProjectId(projectIdList);
        try {
        	String createDateStartCount = p_request
        			.getParameter(PARAM_CREATION_START);
        	if (createDateStartCount != null && createDateStartCount != "")
        	{
        		sp.setCreationStart(simpleDate.parse(createDateStartCount));
        	}
        	
        	String createDateEndCount = p_request.getParameter(PARAM_CREATION_END);
        	 if (createDateEndCount != null && createDateEndCount != "")
             {
        		 Date date = simpleDate.parse(createDateEndCount);
        		 long endLong = date.getTime()+(24*60*60*1000-1);
                 sp.setCreationEnd(new Date(endLong));
             }
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

            return sp;
    }

    private CellStyle getTitleStyle(Workbook p_workbook)
    {
        if (titleStyle == null)
        {
            Font titleFont = p_workbook.createFont();
            titleFont.setUnderline(Font.U_NONE);
            titleFont.setFontName("Times");
            titleFont.setFontHeightInPoints((short) 14);
            titleFont.setBoldweight(Font.BOLDWEIGHT_BOLD);

            CellStyle cs = p_workbook.createCellStyle();
            cs.setFont(titleFont);

            titleStyle = cs;
        }

        return titleStyle;
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

    private CellStyle getRejectedStyle(Workbook p_workbook) throws Exception
    {
    	if(rejectedStyle == null)
    	{
	    	Font rejectedFont = p_workbook.createFont();
	    	rejectedFont.setFontName("Times");
	    	rejectedFont.setUnderline(Font.U_NONE);
	    	rejectedFont.setFontHeightInPoints((short) 11);
	    	rejectedFont.setBoldweight(Font.BOLDWEIGHT_BOLD);
	    	rejectedFont.setColor(IndexedColors.RED.getIndex());
	    	
	    	CellStyle style = p_workbook.createCellStyle();
            style.setFont(rejectedFont);
            style.setWrapText(true);
            
            rejectedStyle = style;
    	}
    	
        return rejectedStyle;
    }

    private CellStyle getRedStyle(Workbook p_workbook) throws Exception
    {
    	if(redStyle == null)
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

            redStyle = style;
    	}

        return redStyle;
    }

    /**
     * Returns a cell format that the wrap has been set to true.
     */
    private CellStyle getWrapStyle(Workbook p_workbook) throws Exception
    {
    	if(wrapStyle == null)
    	{
    		CellStyle style = p_workbook.createCellStyle();
    		style.setWrapText(true);
    		wrapStyle = style;
    	}

        return wrapStyle;
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
        return ReportConstants.JOB_STATUS_REPORT;
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
