package com.globalsight.everest.webapp.pagehandler.administration.reports;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.format.CellFormat;
import jxl.format.Colour;
import jxl.format.UnderlineStyle;
import jxl.write.DateFormat;
import jxl.write.DateTime;
import jxl.write.Label;
import jxl.write.Number;
import jxl.write.NumberFormats;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

import com.globalsight.everest.foundation.SearchCriteriaParameters;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.jobhandler.JobSearchParameters;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.taskmanager.Task;
import com.globalsight.everest.taskmanager.TaskInfo;
import com.globalsight.everest.util.comparator.JobComparator;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.pagehandler.projects.workflows.JobSearchConstants;
import com.globalsight.everest.workflowmanager.Workflow;
import com.globalsight.everest.workflowmanager.WorkflowManagerWLRemote;
import com.globalsight.log.GlobalSightCategory;

public class ActivityDurationReport
{
    private String dateFormat = "dd-MM-yyyy HH:mm";
    private Locale uiLocale = null;

    private WritableWorkbook _workbook = null;
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

    private WritableCellFormat _intFormat = new WritableCellFormat(
            NumberFormats.INTEGER);
    private WritableCellFormat _dateFormat = new WritableCellFormat(
            new DateFormat(dateFormat));

    private static GlobalSightCategory s_logger = (GlobalSightCategory) GlobalSightCategory
            .getLogger("Reports");

    /**
     * Generates the Excel report and spits it to the outputstream The report
     * consists of all in progress workflows that are currently at a reviewOnly
     * stage.
     * 
     * @return File
     * @exception Exception
     */
    public void generateReport(HttpServletRequest p_request,
            HttpServletResponse p_response) throws Exception
    {
        _wfMgr = ServerProxy.getWorkflowManager();
        _currentTimeMillis = System.currentTimeMillis();
        uiLocale = (Locale) p_request.getSession().getAttribute(
                WebAppConstants.UILOCALE);

        WorkbookSettings settings = new WorkbookSettings();
        settings.setSuppressWarnings(true);
        _workbook = Workbook.createWorkbook(p_response.getOutputStream(),
                settings);

        addJobs(p_request);

        _workbook.write();
        _workbook.close();
    }

    /**
     * Adds the table header to the sheet
     * 
     * @param p_sheet
     */
    private void addHeader(WritableSheet sheet, ResourceBundle bundle)
            throws Exception
    {
        // headerFont is black bold on light grey
        WritableFont headerFont = new WritableFont(WritableFont.TIMES, 11,
                WritableFont.BOLD, false, UnderlineStyle.NO_UNDERLINE,
                jxl.format.Colour.BLACK);
        WritableCellFormat headerFormat = new WritableCellFormat(headerFont);
        headerFormat.setWrap(true);
        headerFormat.setBackground(jxl.format.Colour.GRAY_25);
        headerFormat.setShrinkToFit(false);
        headerFormat.setBorder(jxl.format.Border.ALL,
                jxl.format.BorderLineStyle.THICK, jxl.format.Colour.BLACK);

        int row = 0;
        sheet.addCell(new Label(COL_JOB_ID, row, bundle.getString("lb_job_id"),
                headerFormat));
        sheet.setColumnView(COL_JOB_ID, 10);
        sheet.addCell(new Label(COL_JOB_NAME, row, bundle
                .getString("lb_job_name"), headerFormat));
        sheet.setColumnView(COL_JOB_NAME, 30);
        sheet.addCell(new Label(COL_WORDCOUNT, row, bundle
                .getString("lb_word_count"), headerFormat));
        sheet.addCell(new Label(COL_LOCALE, row, bundle.getString("lb_locale"),
                headerFormat));
        sheet.addCell(new Label(COL_WORKFLOW_STATUS, row, bundle
                .getString("lb_workflow_status"), headerFormat));
        sheet.setColumnView(COL_WORKFLOW_STATUS, 25);

        sheet.addCell(new Label(COL_ACTIVITY_ID, row, bundle
                .getString("lb_activity_id"), headerFormat));
        sheet.setColumnView(COL_ACTIVITY_ID, 25);
        sheet.addCell(new Label(COL_ACTIVITY_NAME, row, bundle
                .getString("lb_activity_name"), headerFormat));
        sheet.setColumnView(COL_ACTIVITY_NAME, 25);
        sheet.addCell(new Label(COL_ACTIVITY_STATUS, row, bundle
                .getString("lb_activity_status"), headerFormat));
        sheet.setColumnView(COL_ACTIVITY_STATUS, 25);
        sheet.addCell(new Label(COL_ACTIVITY_AVAILABLE, row, bundle
                .getString("lb_available"), headerFormat));
        sheet.setColumnView(COL_ACTIVITY_AVAILABLE, 20);
        sheet.addCell(new Label(COL_ACTIVITY_ACCEPTED, row, bundle
                .getString("lb_accepted"), headerFormat));
        sheet.setColumnView(COL_ACTIVITY_ACCEPTED, 25);
        sheet.addCell(new Label(COL_ACTIVITY_COMPLETED, row, bundle
                .getString("lb_completed"), headerFormat));
        sheet.setColumnView(COL_ACTIVITY_COMPLETED, 20);
        sheet.addCell(new Label(COL_ACTIVITY_DURATION, row, bundle
                .getString("lb_duration_minutes"), headerFormat));
        sheet.setColumnView(COL_ACTIVITY_DURATION, 20);
        sheet.addCell(new Label(COL_ACTIVITY_EXPORT, row, bundle
                .getString("lb_exported"), headerFormat));
        sheet.setColumnView(COL_ACTIVITY_EXPORT, 20);
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

    /**
     * Get parameters from request perepare to get jobs.
     */
    private void addJobs(HttpServletRequest p_request) throws Exception
    {
        // print out the request parameters
        String[] paramJobId = p_request.getParameterValues("jobId");
        String[] paramTrgLocales = p_request
                .getParameterValues("targetLocalesList");
        String dateFormtParameter = p_request.getParameter("dateFormat");
        JobSearchParameters searchParams = getSearchParams(p_request);
        ResourceBundle bundle = PageHandler.getBundle(p_request.getSession());
        addJobs(paramJobId, paramTrgLocales, dateFormtParameter, searchParams,
                bundle);
    }

    /**
     * Gets the jobs and outputs workflow information.
     * 
     * @exception Exception
     */
    private void addJobs(String[] paramJobId, String[] paramTrgLocales,
            String dateFormtParameter, JobSearchParameters searchParams,
            ResourceBundle bundle) throws Exception
    {

        if (dateFormtParameter != null)
        {
            dateFormat = new String(dateFormtParameter);
            _dateFormat = new WritableCellFormat(new DateFormat(dateFormat));
            _intFormat = new WritableCellFormat(NumberFormats.INTEGER);
        }

        // Create a worksheet and add the header

        WritableSheet sheet = _workbook.createSheet(bundle
                .getString("lb_activity_duration_report"), 0);
        addHeader(sheet, bundle);

        ArrayList jobs = new ArrayList();
        if (paramJobId != null && "*".equals(paramJobId[0]))
        {
            jobs.addAll(ServerProxy.getJobHandler().getJobs(searchParams));
            // sort jobs by job name
            Collections.sort(jobs, new JobComparator(Locale.US));
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

        Iterator jobIter = jobs.iterator();
        int row = 1; // 1 header row already filled
        while (jobIter.hasNext())
        {
            Job job = (Job) jobIter.next();
            Collection c = job.getWorkflows();
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
                    row = addWorkflow(job, w, sheet, row, bundle);
                    row++;
                }
            }
        }

    }

    private int addWorkflow(Job job, Workflow w, WritableSheet sheet, int row,
            ResourceBundle bundle) throws Exception
    {

        // Get all activities of this workflow
        List taskInfos = null;

        taskInfos = _wfMgr.getTaskInfosInDefaultPathWithSkip(w);

        if (taskInfos == null || taskInfos.size() == 0)
        {
            // Job information, common to all workflows of this job
            sheet.addCell(new Number(COL_JOB_ID, row, job.getJobId(),
                    _intFormat));
            sheet.addCell(new Label(COL_JOB_NAME, row, job.getJobName()));
            sheet.addCell(new Number(COL_WORDCOUNT, row, job.getWordCount(),
                    _intFormat));

            // Workflow specific information, common to all activities
            sheet.addCell(new Label(COL_LOCALE, row, w.getTargetLocale()
                    .getDisplayName(uiLocale)));
            sheet.addCell(new Label(COL_WORKFLOW_STATUS, row, w.getState()));

            sheet.addCell(new Label(COL_ACTIVITY_NAME, row, bundle
                    .getString("lb_no_tasks_in_default_path")));
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
                            sheet.addCell(new DateTime(COL_ACTIVITY_AVAILABLE,
                                    incompleteActivityRow, ti
                                            .getCompletedDate(), _dateFormat));

                            sheet.addCell(new Number(COL_ACTIVITY_DURATION,
                                    incompleteActivityRow, taskDuration,
                                    _intFormat));
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
                CellFormat defaultLabelFormat = new WritableCellFormat();
                // Job information, common to all workflows of this job
                sheet.addCell(new Number(COL_JOB_ID, row, job.getJobId(),
                        (isExportFailed) ? redFormat() : _intFormat));
                sheet.addCell(new Label(COL_JOB_NAME, row, job.getJobName(),
                        (isExportFailed) ? redFormat() : defaultLabelFormat));
                sheet.addCell(new Number(COL_WORDCOUNT, row,
                        job.getWordCount(), (isExportFailed) ? redFormat()
                                : _intFormat));
                // Workflow specific information, common to all activities
                sheet.addCell(new Label(COL_LOCALE, row, w.getTargetLocale()
                        .getDisplayName(uiLocale),
                        (isExportFailed) ? redFormat() : defaultLabelFormat));
                sheet.addCell(new Label(COL_WORKFLOW_STATUS, row, w.getState(),
                        (isExportFailed) ? redFormat() : defaultLabelFormat));

                sheet.addCell(new Number(COL_ACTIVITY_ID, row, ti.getId(),
                        (isExportFailed) ? redFormat() : _intFormat));
                sheet.addCell(new Label(COL_ACTIVITY_NAME, row, ti
                        .getTaskDisplayName(), (isExportFailed) ? redFormat()
                        : defaultLabelFormat));

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
                switch (ti.getState())
                {
                case ACTIVE:
                    sheet
                            .addCell(new Label(COL_ACTIVITY_STATUS, row, bundle
                                    .getString("lb_available"),
                                    (isExportFailed) ? redFormat()
                                            : defaultLabelFormat));
                    incompleteActivity = ti;
                    incompleteActivityRow = row;
                    break;
                case ACCEPTED:
                    sheet
                            .addCell(new Label(COL_ACTIVITY_STATUS, row, bundle
                                    .getString("lb_accepted"),
                                    (isExportFailed) ? redFormat()
                                            : defaultLabelFormat));
                    incompleteActivity = ti;
                    incompleteActivityRow = row;
                    break;
                case COMPLETED:
                    sheet
                            .addCell(new Label(COL_ACTIVITY_STATUS, row, bundle
                                    .getString("lb_completed_report"),
                                    (isExportFailed) ? redFormat()
                                            : defaultLabelFormat));
                    break;
                case SKIP:
                    sheet
                            .addCell(new Label(COL_ACTIVITY_STATUS, row, bundle
                                    .getString("lb_skipped"),
                                    (isExportFailed) ? redFormat()
                                            : defaultLabelFormat));
                    skipped = true;
                    break;
                case UNKNOWN:
                default:
                    sheet.addCell(new Label(COL_ACTIVITY_STATUS, row, ti
                            .getStateAsString(), (isExportFailed) ? redFormat()
                            : defaultLabelFormat));
                }

                if (skipped)
                {
                    sheet
                            .addCell(new Label(COL_ACTIVITY_AVAILABLE, row,
                                    bundle.getString("lb_skipped"),
                                    (isExportFailed) ? redFormat()
                                            : defaultLabelFormat));
                }
                else
                {

                    if (incompleteActivity != null
                            && incompleteActivityRow < row)
                        sheet.addCell(new Label(COL_ACTIVITY_AVAILABLE, row,
                                bundle.getString("lb_loop_dates"),
                                (isExportFailed) ? redFormat()
                                        : defaultLabelFormat));
                    else
                        sheet.addCell(new DateTime(COL_ACTIVITY_AVAILABLE, row,
                                new Date(taskBecomeAvailableTime),
                                (isExportFailed) ? redFormat() : _dateFormat));

                }

                if (skipped)
                {
                    sheet
                            .addCell(new Label(COL_ACTIVITY_ACCEPTED, row,
                                    bundle.getString("lb_skipped"),
                                    (isExportFailed) ? redFormat()
                                            : defaultLabelFormat));
                }
                else
                {

                    if (ti.getAcceptedDate() != null)
                    {
                        sheet.addCell(new DateTime(COL_ACTIVITY_ACCEPTED, row,
                                ti.getAcceptedDate(),
                                (isExportFailed) ? redFormat() : _dateFormat));
                    }
                    else
                    {
                        sheet.addCell(new Label(COL_ACTIVITY_ACCEPTED, row,
                                bundle.getString("lb_not_accepted_report"),
                                (isExportFailed) ? redFormat()
                                        : defaultLabelFormat));
                    }
                }

                long taskDurationEndMillis = -1;

                if (skipped)
                {
                    sheet
                            .addCell(new Label(COL_ACTIVITY_COMPLETED, row,
                                    bundle.getString("lb_skipped"),
                                    (isExportFailed) ? redFormat()
                                            : defaultLabelFormat));
                }
                else
                {

                    if (ti.getState() != COMPLETED)
                    {
                        sheet.addCell(new Label(COL_ACTIVITY_COMPLETED, row,
                                bundle.getString("lb_not_completed_report"),
                                (isExportFailed) ? redFormat()
                                        : defaultLabelFormat));
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
                        sheet.addCell(new DateTime(COL_ACTIVITY_COMPLETED, row,
                                ti.getCompletedDate(),
                                (isExportFailed) ? redFormat() : _dateFormat));
                        taskDurationEndMillis = ti.getCompletedDate().getTime();
                    }
                }

                if (skipped)
                {
                    sheet
                            .addCell(new Label(COL_ACTIVITY_DURATION, row,
                                    bundle.getString("lb_skipped"),
                                    (isExportFailed) ? redFormat()
                                            : defaultLabelFormat));
                }
                else
                {
                    long taskDuration = taskDurationEndMillis
                            - taskBecomeAvailableTime;

                    // Make this minutes
                    taskDuration /= 1000; // get it in seconds
                    taskDuration /= 60; // in minutes
                    sheet.addCell(new Number(COL_ACTIVITY_DURATION, row,
                            taskDuration, (isExportFailed) ? redFormat()
                                    : _intFormat));
                }

                Date exportDate = ti.getExportDate();
                if (isExportFailed)
                {
                    sheet.addCell(new Label(COL_ACTIVITY_EXPORT, row, bundle
                            .getString("lb_exported_failed"), redFormat()));
                }
                else
                {
                    if (exportDate != null)
                    {
                        sheet.addCell(new DateTime(COL_ACTIVITY_EXPORT, row,
                                exportDate, _dateFormat));
                    }

                    prevTask = ti;
                }
            }
        }
        return (row + 1);
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
}
