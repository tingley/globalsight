package com.globalsight.everest.webapp.pagehandler.administration.reports;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

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
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import com.globalsight.everest.foundation.SearchCriteriaParameters;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.jobhandler.JobSearchParameters;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.taskmanager.Task;
import com.globalsight.everest.taskmanager.TaskInfo;
import com.globalsight.everest.util.comparator.JobComparator;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.pagehandler.administration.reports.bo.ReportsData;
import com.globalsight.everest.webapp.pagehandler.projects.workflows.JobSearchConstants;
import com.globalsight.everest.workflowmanager.Workflow;
import com.globalsight.everest.workflowmanager.WorkflowManagerWLRemote;
import com.globalsight.util.SortUtil;

public class ActivityDurationReportGenerator
{
    private String dateFormat = "dd-MM-yyyy HH:mm";
    private Locale uiLocale = null;

    private WorkflowManagerWLRemote _wfMgr = null;
    private long _currentTimeMillis = -1;

    private final int COL_JOB_ID = 0;
    private final int COL_JOB_NAME = 1;
    private final int COL_WORDCOUNT = 2;
    private final int COL_LOCALE = 3;
    private final int COL_WORKFLOW_STATUS = 4;
    private final int COL_ACTIVITY_ID = 5;
    private final int COL_ACTIVITY_NAME = 6;
    private final int COL_ACTIVITY_STATUS = 7;
    private final int COL_ACTIVITY_AVAILABLE = 8;
    private final int COL_ACTIVITY_ACCEPTED = 9;
    private final int COL_ACTIVITY_COMPLETED = 10;
    private final int COL_ACTIVITY_DURATION = 11;
    private final int COL_ACTIVITY_EXPORT = 12;
    
    private String[] paramJobId = null;
    private String[] paramTrgLocales = null;
    private String dateFormtParameter = null;
    private JobSearchParameters searchParams = null;
    private ResourceBundle bundle = null;
    private String userId = null;
    private CellStyle intCellStyle = null;
    private CellStyle headerStyle = null;
    private CellStyle contentStyle = null;
    private CellStyle redCellStyle = null;
    private CellStyle redDateCellStyle = null;
    
    private static Logger s_logger = Logger.getLogger("Reports");

    /**
     * Generates the Excel report and spits it to the outputstream The report
     * consists of all in progress workflows that are currently at a reviewOnly
     * stage.
     * 
     * @return File
     * @exception Exception
     */
    public File[] generateReport(HttpServletRequest p_request,
            HttpServletResponse p_response) throws Exception
    {
        _wfMgr = ServerProxy.getWorkflowManager();
        _currentTimeMillis = System.currentTimeMillis();
        uiLocale = (Locale) p_request.getSession().getAttribute(
                WebAppConstants.UILOCALE);
        bundle = PageHandler.getBundle(p_request.getSession());
        userId = (String) p_request.getSession().getAttribute(
                WebAppConstants.USER_NAME);
        paramJobId = p_request.getParameterValues("jobId");
        paramTrgLocales = p_request
                .getParameterValues("targetLocalesList");
        dateFormtParameter = p_request.getParameter("dateFormat");
        searchParams = getSearchParams(p_request);
       
        Workbook p_workbook = new SXSSFWorkbook();
        Sheet sheet = p_workbook.createSheet(bundle.getString("lb_activity_duration_report"));
        addHeader(p_workbook,sheet);
        addJobs(p_workbook, p_response, sheet);
        if (p_workbook != null)
        {
        	File file = ReportHelper.getXLSReportFile(getReportType(), null);
        	FileOutputStream out = new FileOutputStream(file);
	        p_workbook.write(out);
	        out.close();
	        List<File> workBooks = new ArrayList<File>();
            workBooks.add(file);
	        return ReportHelper.moveReports(workBooks, userId);
        }
        
        return null;
    }

    /**
     * Adds the table header to the sheet
     * 
     * @param p_sheet
     */
    private void addHeader(Workbook p_workbook, Sheet p_sheet)
            throws Exception
    {
        Row headerRow = getRow(p_sheet, 0);
        Cell cell_A = getCell(headerRow, COL_JOB_ID);
        cell_A.setCellValue(bundle.getString("lb_job_id"));
        cell_A.setCellStyle(getHeaderStyle(p_workbook));
        p_sheet.setColumnWidth(COL_JOB_ID, 10 * 256);
        
        Cell cell_B = getCell(headerRow, COL_JOB_NAME);
        cell_B.setCellValue(bundle.getString("lb_job_name"));
        cell_B.setCellStyle(getHeaderStyle(p_workbook));
        p_sheet.setColumnWidth(COL_JOB_NAME, 30 * 256);
        
        Cell cell_C = getCell(headerRow, COL_WORDCOUNT);
        cell_C.setCellValue(bundle.getString("lb_word_count"));
        cell_C.setCellStyle(getHeaderStyle(p_workbook));
        p_sheet.setColumnWidth(COL_WORDCOUNT, 15 * 256);
        
        Cell cell_D = getCell(headerRow, COL_LOCALE);
        cell_D.setCellValue(bundle.getString("lb_locale"));
        cell_D.setCellStyle(getHeaderStyle(p_workbook));
        p_sheet.setColumnWidth(COL_LOCALE, 25 * 256);
        
        Cell cell_E = getCell(headerRow, COL_WORKFLOW_STATUS);
        cell_E.setCellValue(bundle.getString("lb_workflow_status"));
        cell_E.setCellStyle(getHeaderStyle(p_workbook));
        p_sheet.setColumnWidth(COL_WORKFLOW_STATUS, 25 * 256);
        
        Cell cell_F = getCell(headerRow, COL_ACTIVITY_ID);
        cell_F.setCellValue(bundle.getString("lb_activity_id"));
        cell_F.setCellStyle(getHeaderStyle(p_workbook));
        p_sheet.setColumnWidth(COL_ACTIVITY_ID, 25 * 256);

        Cell cell_G = getCell(headerRow, COL_ACTIVITY_NAME);
        cell_G.setCellValue(bundle.getString("lb_activity_name"));
        cell_G.setCellStyle(getHeaderStyle(p_workbook));
        p_sheet.setColumnWidth(COL_ACTIVITY_NAME, 25 * 256);
        
        Cell cell_H = getCell(headerRow, COL_ACTIVITY_STATUS);
        cell_H.setCellValue(bundle.getString("lb_activity_status"));
        cell_H.setCellStyle(getHeaderStyle(p_workbook));
        p_sheet.setColumnWidth(COL_ACTIVITY_STATUS, 25 * 256);
        
        Cell cell_I = getCell(headerRow, COL_ACTIVITY_AVAILABLE);
        cell_I.setCellValue(bundle.getString("lb_available"));
        cell_I.setCellStyle(getHeaderStyle(p_workbook));
        p_sheet.setColumnWidth(COL_ACTIVITY_AVAILABLE, 20 * 256);
        
        Cell cell_J = getCell(headerRow, COL_ACTIVITY_ACCEPTED);
        cell_J.setCellValue(bundle.getString("lb_accepted"));
        cell_J.setCellStyle(getHeaderStyle(p_workbook));
        p_sheet.setColumnWidth(COL_ACTIVITY_ACCEPTED, 25 * 256);
        
        Cell cell_K = getCell(headerRow, COL_ACTIVITY_COMPLETED);
        cell_K.setCellValue(bundle.getString("lb_completed"));
        cell_K.setCellStyle(getHeaderStyle(p_workbook));
        p_sheet.setColumnWidth(COL_ACTIVITY_COMPLETED, 20 * 256);
        
        Cell cell_L = getCell(headerRow, COL_ACTIVITY_DURATION);
        cell_L.setCellValue(bundle.getString("lb_duration_minutes"));
        cell_L.setCellStyle(getHeaderStyle(p_workbook));
        p_sheet.setColumnWidth(COL_ACTIVITY_DURATION, 20 * 256);
        
        Cell cell_M = getCell(headerRow, COL_ACTIVITY_EXPORT);
        cell_M.setCellValue(bundle.getString("lb_exported"));
        cell_M.setCellStyle(getHeaderStyle(p_workbook));
        p_sheet.setColumnWidth(COL_ACTIVITY_EXPORT, 20 * 256);
    }
    
    /**
     * Gets the jobs and outputs workflow information.
     * 
     * @exception Exception
     */
    private void addJobs(Workbook p_workbook, HttpServletResponse p_response,
    		Sheet p_sheet) throws Exception
    {
        if (dateFormtParameter != null)
        {
            dateFormat = new String(dateFormtParameter);
        }

        ArrayList<Job> jobs = new ArrayList<Job>();
        if (paramJobId != null && "*".equals(paramJobId[0]))
        {
            jobs.addAll(ServerProxy.getJobHandler().getJobs(searchParams));
            // sort jobs by job name
            SortUtil.sort(jobs, new JobComparator(Locale.US));
        }
        else
        {
            // just get the specific jobs they chose
            for (int i = 0; i < paramJobId.length; i++)
            {
                if ("*".equals(paramJobId[i]) == false)
                {
                    long jobId = Long.parseLong(paramJobId[i]);
                    Job job = ServerProxy.getJobHandler().getJobById(jobId);
                    jobs.add(job);
                }
            }
        }

        boolean wantsAllLocales = false;
        HashSet trgLocaleList = new HashSet();
        if (paramTrgLocales != null)
        {
            for (int i = 0; i < paramTrgLocales.length; i++)
            {
                if ("*".equals(paramTrgLocales[i]))
                {
                    wantsAllLocales = true;
                    break;
                }
                else
                {
                    trgLocaleList.add(paramTrgLocales[i]);
                }
            }
        }

        List<Long> reportJobIDS = ReportHelper.getJobIDS(jobs);
        // Cancel Duplicate Request
        if (ReportHelper.checkReportsDataInProgressStatus(userId, reportJobIDS, getReportType()))
        {
            p_workbook = null;
            p_response.sendError(p_response.SC_NO_CONTENT);
            return;
        }
        // Set ReportsData.
        ReportHelper.setReportsData(userId, reportJobIDS, getReportType(), 
                0, ReportsData.STATUS_INPROGRESS);
        int row = 1; // 1 header row already filled
        for (Job job: jobs)
        {
            Collection c = job.getWorkflows();
            Iterator wfIter = c.iterator();
            while (wfIter.hasNext())
            {
                Workflow w = (Workflow) wfIter.next();
                String state = w.getState();
                // skip certain workflow whose target locale is not selected
                String trgLocale = Long.toString(w.getTargetLocale().getId());
                if (!wantsAllLocales && !trgLocaleList.contains(trgLocale))
                {
                    continue;
                }
                if (Workflow.EXPORTED.equals(state)
                        || Workflow.DISPATCHED.equals(state)
                        || Workflow.LOCALIZED.equals(state)
                        || Workflow.EXPORT_FAILED.equals(state)
                        || Workflow.ARCHIVED.equals(state))
                {
                    row = addWorkflow(job, w,p_workbook, p_sheet, row);
                    row++;
                }
            }
        }

        // Set ReportsData.
        ReportHelper.setReportsData(userId, reportJobIDS, getReportType(), 
                100, ReportsData.STATUS_FINISHED);
    }

    private int addWorkflow(Job job, Workflow w, Workbook p_workbook, Sheet p_sheet,
    		int row) throws Exception
    {

        // Get all activities of this workflow
        List taskInfos = null;

        taskInfos = _wfMgr.getTaskInfosInDefaultPathWithSkip(w);

        if (taskInfos == null || taskInfos.size() == 0)
        {
            // Job information, common to all workflows of this job
        	Row p_row = getRow(p_sheet, row);
        	Cell cell_A = getCell(p_row, COL_JOB_ID);
        	cell_A.setCellValue(job.getJobId());
        	cell_A.setCellStyle(getIntCellStyle(p_workbook));
        	
        	Cell cell_B = getCell(p_row, COL_JOB_NAME);
        	cell_B.setCellValue(job.getJobName());
        	cell_B.setCellStyle(getContentStyle(p_workbook));
        	
        	Cell cell_C = getCell(p_row, COL_WORDCOUNT);
        	cell_C.setCellValue(w.getTotalWordCount());
        	cell_C.setCellStyle(getIntCellStyle(p_workbook));
        	
            // Workflow specific information, common to all activities
        	Cell cell_D = getCell(p_row, COL_LOCALE);
        	cell_D.setCellValue(w.getTargetLocale().getDisplayName(uiLocale));
        	cell_D.setCellStyle(getContentStyle(p_workbook));
        	
        	Cell cell_E = getCell(p_row, COL_WORKFLOW_STATUS);
        	cell_E.setCellValue(w.getState());
        	cell_E.setCellStyle(getContentStyle(p_workbook));

        	Cell cell_G = getCell(p_row, COL_ACTIVITY_NAME);
        	cell_G.setCellValue(bundle.getString("lb_no_tasks_in_default_path"));
        	cell_G.setCellStyle(getContentStyle(p_workbook));
        }
        else
        {
            final int ACCEPTED = Task.STATE_ACCEPTED; // 8
            final int ACTIVE = Task.STATE_ACTIVE; // 3
            final int COMPLETED = 5;
            final int SKIP = Task.STATE_SKIP;
            // Task.STATE_COMPLETED is -1;
            // But completed task goes here change to 5
            final int UNKNOWN = Task.STATE_NOT_KNOWN; // 0

            // Which row stores the information of the incomplete activity
            int incompleteActivityRow = -1;
            TaskInfo incompleteActivity = null;
            int firstRow = -1;

            Iterator it = taskInfos.iterator();
            TaskInfo prevTask = null;

            // Write out information for each task, ie. Activity
            while (it.hasNext())
            {
                TaskInfo ti = (TaskInfo) it.next();
                boolean skipped = false;
                // Ignore activities that have not even become available yet
                if (ti.getState() == UNKNOWN)
                    continue;

                /*
                 * We have following activities for an activity that has not
                 * been completed which essentially means that the workflow was
                 * moved back to a previous activity. In this case, the time
                 * when the incomplete activity become available is incorrect
                 * and needs to be adjusted. Additionally, the information of
                 * (at least) the following activity is not correct either, so
                 * we ignore all of the following activities for now
                 */
                if (incompleteActivity != null)
                {
                    // Is this the last activity that has been done before the
                    // workflow
                    // moved 'backwards'? If no, we can just ignore this
                    // activity
                    if (!it.hasNext())
                    {
                        /*
                         * This is the last activity that was done before the
                         * workflow was moved 'backwards'. Our completion time
                         * needs to be the the time the new active instance of
                         * the previously performed activity became available
                         * (again).
                         */
                        if (ti.getCompletedDate() != null)
                        {
                            long taskBecomeAvailableTime = ti
                                    .getCompletedDate().getTime();
                            long taskDurationEndMillis = _currentTimeMillis; // we
                                                                             // know
                                                                             // its
                                                                             // incomplete
                            long taskDuration = taskDurationEndMillis
                                    - taskBecomeAvailableTime;
                            // Make this minutes
                            taskDuration /= 1000; // get it in seconds
                            taskDuration /= 60; // in minutes

                            // Update the row displaying that incomplete
                            // activity
                            Row p_row = getRow(p_sheet, incompleteActivityRow);
                            Cell cell_I = getCell(p_row, COL_ACTIVITY_AVAILABLE);
                            cell_I.setCellValue(ti.getCompletedDate());
                            cell_I.setCellStyle(getDateCellStyle(p_workbook));
                            
                            Cell cell_L = getCell(p_row, COL_ACTIVITY_DURATION);
                            cell_L.setCellValue(taskDuration);
                            cell_L.setCellStyle(getIntCellStyle(p_workbook));
                        }
                        else
                        {
                            s_logger.error("No Completed data for this task:"
                                    + ti.toString());
                        }
                    }
                    // do not ignore the entry though
                }

                if (firstRow == -1)
                {
                    firstRow = row;
                }
                else
                    // next row starts
                    row++;
                boolean isExportFailed = false;
                if (job.EXPORT_FAIL.equals(job.getState()))
                {
                    isExportFailed = true;
                }
                // Job information, common to all workflows of this job
                Row p_row = getRow(p_sheet, row);
                Cell cell_A = getCell(p_row, COL_JOB_ID);
                cell_A.setCellValue(job.getJobId());
                cell_A.setCellStyle((isExportFailed) ? getRedCellStyle(p_workbook) :
                	getIntCellStyle(p_workbook));
                
                Cell cell_B = getCell(p_row, COL_JOB_NAME);
                cell_B.setCellValue(job.getJobName());
                cell_B.setCellStyle((isExportFailed) ? getRedCellStyle(p_workbook) :
                	getContentStyle(p_workbook));
                
                Cell cell_C = getCell(p_row, COL_WORDCOUNT);
                cell_C.setCellValue(w.getTotalWordCount());
                cell_C.setCellStyle((isExportFailed) ? getRedCellStyle(p_workbook) :
                	getIntCellStyle(p_workbook));
                
                // Workflow specific information, common to all activities
                Cell cell_D = getCell(p_row, COL_LOCALE);
                cell_D.setCellValue(w.getTargetLocale().getDisplayName(uiLocale));
                cell_D.setCellStyle((isExportFailed) ? getRedCellStyle(p_workbook) :
                	getContentStyle(p_workbook));
                
                Cell cell_E = getCell(p_row, COL_WORKFLOW_STATUS);
                cell_E.setCellValue(w.getState());
                cell_E.setCellStyle((isExportFailed) ? getRedCellStyle(p_workbook) :
                	getContentStyle(p_workbook));
                
                Cell cell_F = getCell(p_row, COL_ACTIVITY_ID);
                cell_F.setCellValue(ti.getId());
                cell_F.setCellStyle((isExportFailed) ? getRedCellStyle(p_workbook) :
                	getIntCellStyle(p_workbook));

                Cell cell_G = getCell(p_row, COL_ACTIVITY_NAME);
                cell_G.setCellValue(ti.getTaskDisplayName());
                cell_G.setCellStyle((isExportFailed) ? getRedCellStyle(p_workbook) :
                	getContentStyle(p_workbook));

                long taskBecomeAvailableTime = 0;
                if (job.getCreateDate() == null)
                {
                    s_logger.error("No Create data for this job:"
                            + job.toString());
                    return row;
                }

                if (prevTask == null)
                {
                    taskBecomeAvailableTime = job.getCreateDate().getTime();
                }
                else
                {
                    if (prevTask.getCompletedDate() != null)
                    {
                        taskBecomeAvailableTime = prevTask.getCompletedDate()
                                .getTime();
                    }
                    else
                    {
                        taskBecomeAvailableTime = job.getCreateDate().getTime();
                    }
                }

                // is this a iteration or the first time, ie. are completed and
                // accepted date from previous iteration or from this one
                Cell cell_H = getCell(p_row, COL_ACTIVITY_STATUS);
                switch (ti.getState())
                {
                    case ACTIVE:
                        cell_H.setCellValue(bundle.getString("lb_available"));
                        cell_H.setCellStyle((isExportFailed) ? getRedCellStyle(p_workbook) :
                        	getContentStyle(p_workbook));
                        incompleteActivity = ti;
                        incompleteActivityRow = row;
                        break;
                    case ACCEPTED:
                    	cell_H.setCellValue(bundle.getString("lb_accepted"));
                    	cell_H.setCellStyle((isExportFailed) ? getRedCellStyle(p_workbook) :
                        	getContentStyle(p_workbook));
                        incompleteActivity = ti;
                        incompleteActivityRow = row;
                        break;
                    case COMPLETED:
                    	cell_H.setCellValue(bundle.getString("lb_completed_report"));
                        cell_H.setCellStyle((isExportFailed) ? getRedCellStyle(p_workbook) :
                        	getContentStyle(p_workbook));
                        break;
                    case SKIP:
                    	cell_H.setCellValue(bundle.getString("lb_skipped"));
                    	cell_H.setCellStyle((isExportFailed) ? getRedCellStyle(p_workbook) :
                        	getContentStyle(p_workbook));
                        skipped = true;
                        break;
                    case UNKNOWN:
                    default:
                    	cell_H.setCellValue(ti.getStateAsString());
                    	cell_H.setCellStyle((isExportFailed) ? getRedCellStyle(p_workbook) :
                        	getContentStyle(p_workbook));
                }

                // Available Date Column
                Cell cell_I = getCell(p_row, COL_ACTIVITY_AVAILABLE);
                if (skipped)
                {
                    cell_I.setCellValue(bundle.getString("lb_skipped"));
                    cell_I.setCellStyle((isExportFailed) ? getRedCellStyle(p_workbook) :
                    	getContentStyle(p_workbook));
                }
                else
                {
                    if (incompleteActivity != null
                            && incompleteActivityRow < row)
                    {
                    	cell_I.setCellValue(bundle.getString("lb_loop_dates"));
                    	cell_I.setCellStyle((isExportFailed) ? getRedCellStyle(p_workbook) :
                        	getContentStyle(p_workbook));
                    }
                    else{
                    	cell_I.setCellValue(new Date(taskBecomeAvailableTime));
                    	cell_I.setCellStyle((isExportFailed) ? getRedDateCellStyle(p_workbook) :
                        	getDateCellStyle(p_workbook));
                    }

                }

                // Accepted Date Column
                Cell cell_J = getCell(p_row, COL_ACTIVITY_ACCEPTED);
                if (skipped)
                {
                	cell_J.setCellValue(bundle.getString("lb_skipped"));
                	cell_J.setCellStyle((isExportFailed) ? getRedCellStyle(p_workbook) :
                    	getContentStyle(p_workbook));
                }
                else
                {

                    if (ti.getAcceptedDate() != null)
                    {
                    	cell_J.setCellValue(ti.getAcceptedDate());
                    	cell_J.setCellStyle((isExportFailed) ? getRedDateCellStyle(p_workbook) :
                        	getDateCellStyle(p_workbook));
                    }
                    else
                    {
                    	cell_J.setCellValue(bundle.getString("lb_not_accepted_report"));
                    	cell_J.setCellStyle((isExportFailed) ? getRedCellStyle(p_workbook) :
                        	getContentStyle(p_workbook));
                    }
                }

                long taskDurationEndMillis = -1;
                // Completed Date Column
                Cell cell_K = getCell(p_row, COL_ACTIVITY_COMPLETED);
                if (skipped)
                {
                    cell_K.setCellValue(bundle.getString("lb_skipped"));
                    cell_K.setCellStyle((isExportFailed) ? getRedCellStyle(p_workbook) :
                    	getContentStyle(p_workbook));
                }
                else
                {

                    if (ti.getState() != COMPLETED)
                    {
                    	cell_K.setCellValue(bundle.getString("lb_not_completed_report"));
                    	cell_K.setCellStyle((isExportFailed) ? getRedCellStyle(p_workbook) :
                        	getContentStyle(p_workbook));
                        taskDurationEndMillis = _currentTimeMillis;
                    }
                    else
                    {
                        if (ti.getCompletedDate() == null)
                        {
                            prevTask = ti;
                            s_logger.error("No Completed data for this task:"
                                    + ti.toString());
                            continue;
                        }
                        cell_K.setCellValue(ti.getCompletedDate());
                        cell_K.setCellStyle((isExportFailed) ? getRedDateCellStyle(p_workbook) :
                        	getDateCellStyle(p_workbook));
                        taskDurationEndMillis = ti.getCompletedDate().getTime();
                    }
                }

                // Duration(Minutes) Column
                Cell cell_L = getCell(p_row, COL_ACTIVITY_DURATION);
                if (skipped)
                {
                    cell_L.setCellValue(bundle.getString("lb_skipped"));
                    cell_L.setCellStyle((isExportFailed) ? getRedCellStyle(p_workbook) :
                    	getContentStyle(p_workbook));
                }
                else
                {
                    long taskDuration = taskDurationEndMillis
                            - taskBecomeAvailableTime;

                    // Make this minutes
                    taskDuration /= 1000; // get it in seconds
                    taskDuration /= 60; // in minutes
                    cell_L.setCellValue(taskDuration);
                    cell_L.setCellStyle((isExportFailed) ? getRedCellStyle(p_workbook) :
                    	getIntCellStyle(p_workbook));
                }

                // Exported Date Column
                Date exportDate = ti.getExportDate();
                Cell cell_M = getCell(p_row, COL_ACTIVITY_EXPORT);
                if (isExportFailed)
                {
                	cell_M.setCellValue(bundle.getString("lb_exported_failed"));
                	cell_M.setCellStyle(getRedCellStyle(p_workbook));
                }
                else
                {
                    if (exportDate != null)
                    {
                    	cell_M.setCellValue(exportDate);
                    	cell_M.setCellStyle(getDateCellStyle(p_workbook));
                    }

                    prevTask = ti;
                }
            }
        }
        return (row + 1);
    }


    /**
     * Returns search params used to find the in progress jobs for all PMs
     * 
     * @return JobSearchParams
     */
    private JobSearchParameters getSearchParams(HttpServletRequest p_request)
            throws Exception
    {
        String[] paramProjectIds = p_request.getParameterValues("projectId");
        String[] paramStatus = p_request.getParameterValues("status");
        JobSearchParameters sp = new JobSearchParameters();

        ArrayList stateList = new ArrayList();
        if (paramStatus != null && "*".equals(paramStatus[0]) == false)
        {

            for (int i = 0; i < paramStatus.length; i++)
            {
                stateList.add(paramStatus[i]);
            }
        }
        else
        {
            // just do a query for all in progress jobs, localized, and exported
            stateList.add(Job.DISPATCHED);
            stateList.add(Job.LOCALIZED);
            stateList.add(Job.EXPORTED);
            stateList.add(Job.EXPORT_FAIL);
        }
        sp.setJobState(stateList);

        // search by project
        ArrayList projectIdList = new ArrayList();
        boolean wantsAllProjects = false;
        for (int i = 0; i < paramProjectIds.length; i++)
        {
            String id = paramProjectIds[i];
            if (id.equals("*") == false)
                projectIdList.add(new Long(id));
            else
            {
                wantsAllProjects = true;
                break;
            }
        }
        if (wantsAllProjects == false)
        {
            sp.setProjectId(projectIdList);
        }

        String paramCreateDateStartCount = p_request
                .getParameter(JobSearchConstants.CREATION_START);
        String paramCreateDateStartOpts = p_request
                .getParameter(JobSearchConstants.CREATION_START_OPTIONS);
        // "-1" is a default value that indicate user does not put value in web
        // form.
        // so ignore creation start date for search parameter.
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
        // "-1" is a default value that indicate user does not put value in web
        // form.
        // so ignore creation end date for search parameter.
        else if ("-1".equals(paramCreateDateEndOpts) == false)
        {
            sp.setCreationEnd(new Integer(paramCreateDateEndCount));
            sp.setCreationEndCondition(paramCreateDateEndOpts);
        }

        return sp;
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
    
    private CellStyle getRedDateCellStyle(Workbook p_workbook)
    {
        if (redDateCellStyle == null)
        {
            Font redFont = p_workbook.createFont();
            redFont.setFontName("Arial");
            redFont.setUnderline(Font.U_NONE);
            redFont.setFontHeightInPoints((short) 10);

            redDateCellStyle = p_workbook.createCellStyle();
            redDateCellStyle.setFont(redFont);
            redDateCellStyle.setWrapText(true);
            redDateCellStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
            redDateCellStyle.setFillForegroundColor(IndexedColors.RED.getIndex());
            redDateCellStyle.setDataFormat(p_workbook.createDataFormat().getFormat(dateFormat));
        }

        return redDateCellStyle;
    }
    
    private CellStyle getIntCellStyle(Workbook p_workbook){
    	if(intCellStyle == null)
    	{
    		CellStyle style = p_workbook.createCellStyle();
    		DataFormat format = p_workbook.createDataFormat();
    		style.setDataFormat(format.getFormat("0"));
    		intCellStyle = style;
    	}
    	return intCellStyle;
    }
    
    private CellStyle getDateCellStyle(Workbook p_workbook){
		CellStyle style = p_workbook.createCellStyle();
		DataFormat format = p_workbook.createDataFormat();
		style.setDataFormat(format.getFormat(dateFormat));
		return style;
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
        return ReportConstants.ACTIVITY_DURATION_REPORT;
    }
}
