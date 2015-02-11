package com.globalsight.everest.webapp.pagehandler.administration.reports;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Vector;

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
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import com.globalsight.everest.company.CompanyWrapper;
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
import com.globalsight.everest.webapp.pagehandler.projects.workflows.JobSearchConstants;
import com.globalsight.everest.workflow.Activity;
import com.globalsight.everest.workflow.EnvoyWorkItem;
import com.globalsight.everest.workflow.WorkflowConstants;
import com.globalsight.everest.workflow.WorkflowTaskInstance;
import com.globalsight.everest.workflowmanager.Workflow;
import com.globalsight.util.IntHolder;
import com.globalsight.util.SortUtil;

public class OnlineRevStatusXlsReportGenerator
{
	//String EMEA = CompanyWrapper.getCurrentCompanyName();
    private static Logger s_logger = Logger.getLogger("Reports");
    private ResourceBundle bundle = null;
    private String[] paramJobId = null;
    private String[] paramTrgLocales = null;
    private String[] paramProjectIds = null;
    private String[] paramStatus = null;
    private CellStyle contentStyle = null;
    private CellStyle headerStyle = null;
    private String userId = null;
    private List<Long> m_jobIDS = null;

    /**
     * Generates the Excel report and spits it to the outputstream
     * The report consists of all in progress workflows that are
     * currently at a reviewOnly stage.
     * 
     * @return File
     * @exception Exception
     */
    public void generateReport(HttpServletRequest p_request, HttpServletResponse p_response) throws Exception
    {
    	bundle = PageHandler.getBundle(p_request.getSession());
        //print out the request parameters
        paramJobId = p_request.getParameterValues("jobId");
        paramTrgLocales = p_request.getParameterValues("targetLocalesList");
        paramProjectIds = p_request.getParameterValues("projectId");
        paramStatus = p_request.getParameterValues("status");
        userId = (String) p_request.getSession(false).getAttribute(
                WebAppConstants.USER_NAME);
    	
    	Workbook p_workbook = new SXSSFWorkbook();
    	Sheet sheet = p_workbook.createSheet(bundle.getString("lb_sheet") + "1");
    	addTitle(p_workbook, sheet);
    	addHeader(p_workbook, sheet);
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
    
    private void addTitle(Workbook p_workbook, Sheet p_sheet) throws Exception
    {
    	String EMEA = CompanyWrapper.getCurrentCompanyName();
        Font titleFont = p_workbook.createFont();
        titleFont.setUnderline(Font.U_NONE);
        titleFont.setFontName("Times");
        titleFont.setFontHeightInPoints((short) 14);
        titleFont.setBoldweight(Font.BOLDWEIGHT_BOLD);
        titleFont.setColor(IndexedColors.BLACK.getIndex());
        CellStyle titleStyle = p_workbook.createCellStyle();
        titleStyle.setFont(titleFont);
        titleStyle.setWrapText(false);
        
        Row titleRow = getRow(p_sheet, 0);
        Cell titleCell = getCell(titleRow, 0);
        titleCell.setCellValue(EMEA + " " + bundle.getString("online_review_status"));
        titleCell.setCellStyle(titleStyle);
        p_sheet.setColumnWidth(0, 22 * 256);
    }   
    
    /**
     * Adds the table header to the sheet
     * 
     * @param p_sheet
     */
    private void addHeader(Workbook p_workbook, Sheet p_sheet) throws Exception
    {
        int col=0;
        int row=3;
        Row headerRow = getRow(p_sheet, row);
        Cell cell_A = getCell(headerRow, col++);
        cell_A.setCellValue(bundle.getString("job_id"));
        cell_A.setCellStyle(getHeaderStyle(p_workbook));
        p_sheet.setColumnWidth(col -1,10 * 256);
        
        Cell cell_B = getCell(headerRow, col++);
        cell_B.setCellValue(bundle.getString("lb_job"));
        cell_B.setCellStyle(getHeaderStyle(p_workbook));
        p_sheet.setColumnWidth(col -1,50 * 256);
        
        Cell cell_C = getCell(headerRow, col++);
        cell_C.setCellValue(bundle.getString("lb_lang"));
        cell_C.setCellStyle(getHeaderStyle(p_workbook));
        
        Cell cell_D = getCell(headerRow, col++);
        cell_D.setCellValue(bundle.getString("word_count"));
        cell_D.setCellStyle(getHeaderStyle(p_workbook));
        p_sheet.setColumnWidth(col -1,15 * 256);
        
        Cell cell_E = getCell(headerRow, col++);
        cell_E.setCellValue(bundle.getString("lb_actual_date_to_review"));
        cell_E.setCellStyle(getHeaderStyle(p_workbook));
        p_sheet.setColumnWidth(col -1,25 * 256);
        
        Cell cell_F = getCell(headerRow, col++);
        cell_F.setCellValue(bundle.getString("lb_current_activity"));
        cell_F.setCellStyle(getHeaderStyle(p_workbook));
        p_sheet.setColumnWidth(col -1,20 * 256);
        
        Cell cell_G = getCell(headerRow, col++);
        cell_G.setCellValue(bundle.getString("lb_reviewer_accepted"));
        cell_G.setCellStyle(getHeaderStyle(p_workbook));
        p_sheet.setColumnWidth(col -1,25 * 256);
        
        Cell cell_H = getCell(headerRow, col++);
        cell_H.setCellValue(bundle.getString("lb_reviewer_name"));
        cell_H.setCellStyle(getHeaderStyle(p_workbook));
        p_sheet.setColumnWidth(col -1,30 * 256);
        
        Cell cell_I = getCell(headerRow, col++);
        cell_I.setCellValue(bundle.getString("lb_tracking"));
        cell_I.setCellStyle(getHeaderStyle(p_workbook));
        p_sheet.setColumnWidth(col -1,20 * 256);        
    }

    /**
     * Gets the jobs and outputs workflow information.
     * @Return the write flag, whether write the workbook to OutputStream.
     * @exception Exception
     */
    private void addJobs(Workbook p_workbook, Sheet p_sheet, 
            HttpServletRequest p_request, HttpServletResponse p_response) throws Exception
    {
        ArrayList<Job> jobs = new ArrayList<Job>();
        if (paramJobId != null && "*".equals(paramJobId[0]))
        {
            // do a search based on the params
            JobSearchParameters searchParams = getSearchParams(p_request);
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
                    Job j = ServerProxy.getJobHandler().getJobById(jobId);
                    jobs.add(j);
                }
            }
        }
        
        boolean wantsAllLocales = false;
        HashSet trgLocaleList = new HashSet();
        if(paramTrgLocales != null)
        {
        	for (int i=0; i<paramTrgLocales.length; i++)
        	{
        		if("*".equals(paramTrgLocales[i]))
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
          
		m_jobIDS = ReportHelper.getJobIDS(jobs);
        // Cancel Duplicate Request
        if (ReportHelper.checkReportsDataInProgressStatus(userId,
        		m_jobIDS, getReportType()))
        {
            p_response.sendError(p_response.SC_NO_CONTENT);
            return;
        }
        // Set m_reportsDataMap.
        ReportHelper.setReportsData(userId, m_jobIDS, getReportType(),
        		0, ReportsData.STATUS_INPROGRESS);
        IntHolder row = new IntHolder(4);
        for(Job j: jobs)
        {
            Collection c = j.getWorkflows();
            Iterator wfIter = c.iterator();
            while (wfIter.hasNext())
            {
                if (isCancelled())
                {
                    return;
                }
                Workflow w = (Workflow) wfIter.next();
                String state = w.getState();
                //skip certain workflow whose target locale is not selected
                String trgLocale = Long.toString(w.getTargetLocale().getId());
                if (!wantsAllLocales && !trgLocaleList.contains(trgLocale)) 
                {
                	continue;
                }
                if (Workflow.DISPATCHED.equals(state))
                {
                    addWorkflow(p_workbook, p_request,p_sheet,j,w,row);
                }
            }
        }
        
     	// Set m_reportsDataMap.
        ReportHelper.setReportsData(userId, m_jobIDS,
        		getReportType(), 100, ReportsData.STATUS_FINISHED);
    }

    /**
    * Gets the task for the workflow and outputs page information.
    * @exception Exception
    */
    private void addWorkflow(Workbook p_workbook, HttpServletRequest p_request, Sheet p_sheet, Job j, Workflow w, IntHolder row) throws Exception
    {
        SimpleDateFormat dateFormat = new SimpleDateFormat(p_request.getParameter("dateFormat"));
        HttpSession session = p_request.getSession();
        ResourceBundle bundle = PageHandler.getBundle(session);
        
        Map activeTasks = null;
        try {
         activeTasks = ServerProxy.getWorkflowServer().getActiveTasksForWorkflow(w.getId());
        }
        catch (Exception e)
        {
          activeTasks=null;
          //just log the message since we don't want a full stack trace to clog the log
          s_logger.error("Failed to get active tasks for workflow " + w.getId() + " " + e.getMessage());
        }
        
        // for now we'll only have one active task
        Object[] tasks = (activeTasks == null) ? null :
                         activeTasks.values().toArray();
        WorkflowTaskInstance activeTask = null;
        String activityName = null;
        if (tasks != null && tasks.length > 0)
        {
            //assume just one active task for now
            activeTask = (WorkflowTaskInstance)tasks[0];
            //only write out if the Activity type is review only
            Activity a = activeTask.getActivity();
            activityName = a.getDisplayName();
        }
        
        //don't output anything for this workflow if the task currently is one of the following
        if (activityName==null || "Translation".equalsIgnoreCase(activityName) ||
            "TW_POST_Translation".equalsIgnoreCase(activityName) ||
            "Translation_Post_Review".equalsIgnoreCase(activityName))
        
        {
          return;
        }
        
        int c=0;
        int r=row.getValue();
        Row workbflowRow = getRow(p_sheet, r);
        //2.3	Job ID column. Insert Ambassador job number here.
        Cell cell_A = getCell(workbflowRow, c++);
        cell_A.setCellValue(j.getJobId());
        cell_A.setCellStyle(getContentStyle(p_workbook));
        
        //2.4	Job: Insert Job name here.
        Cell cell_B = getCell(workbflowRow, c++);
        cell_B.setCellValue(j.getJobName());
        cell_B.setCellStyle(getContentStyle(p_workbook));
        
        //2.5	Lang: Insert each target language identifier for each workflow
        //in the retrieved Job on a different row.
        Cell cell_C = getCell(workbflowRow, c++);
        cell_C.setCellValue(w.getTargetLocale().getLanguageCode());
        cell_C.setCellStyle(getContentStyle(p_workbook));
        
        //2.6	Word count: Insert source word count for the job.
        Cell cell_D = getCell(workbflowRow, c++);
        cell_D.setCellValue(w.getTotalWordCount());
        cell_D.setCellStyle(getContentStyle(p_workbook));
        
        //2.8 date due to review -- For the current workflow, find the activity 
        //called "Dell_Review", then insert the Estimated Completion Date (and time)
        // for the activity prior to the "Dell_Review" activity.
        List taskInfos = ServerProxy.getWorkflowManager().getTaskInfosInDefaultPath(w);
        if (taskInfos==null)
          taskInfos=new ArrayList();
        Iterator taskIter = taskInfos.iterator();
        Task priorTask = null;
        TaskInfo priorTaskInfo = null;
        TaskInfo revTaskInfo = null;
        Task revTask = null;
        while (taskIter.hasNext())
        {
          TaskInfo ti = (TaskInfo)taskIter.next();
          if (ti.getType()==Task.TYPE_REVIEW)
          {
            revTaskInfo = ti;
            revTask = ServerProxy.getTaskManager().getTask(revTaskInfo.getId());
            if (priorTaskInfo!=null) {
               priorTask = ServerProxy.getTaskManager().getTask(priorTaskInfo.getId());
            }
            break;
          }
          else
          {
            priorTaskInfo = ti;
          }
        }
        
        //2.9	Actual date to Review: Insert the date (and time) the "Dell_Review"
        //activity became available for acceptance in the current workflow.
        Cell cell_E = getCell(workbflowRow, c++);
        if (revTask==null)
        {
            cell_E.setCellValue(bundle.getString("lb_no_review"));
            cell_E.setCellStyle(getContentStyle(p_workbook));
        }
        else if (priorTaskInfo != null && priorTask != null 
        		&& priorTask.getCompletedDate() != null)
        {
        	cell_E.setCellValue(dateFormat.format(priorTask.getCompletedDate()));
    	    cell_E.setCellStyle(getContentStyle(p_workbook));
        }
        else
        {
        	cell_E.setCellValue(bundle.getString("lb_na"));
        	cell_E.setCellStyle(getContentStyle(p_workbook));
        }
        
        //2.10	Current Activity: Currently active activity in the current workflow.   
        Cell cell_F = getCell(workbflowRow, c++);
        cell_F.setCellValue(activityName);
        cell_F.setCellStyle(getContentStyle(p_workbook));
        
        boolean wasRejected = false;
        String rejectorName = null;

        boolean revAccepted=false;        
        //2.11	Reviewer accepted: If the "Dell_Review" activity has been accepted,
        // insert the date (and time) it was accepted. If not accepted yet,
        // put "not accepted yet".
        Cell cell_G = getCell(workbflowRow, c++);
        if (revTask!=null)
        {
           if (revTask.getAcceptor()!=null)
           {
             revAccepted=true;
             if (revTask.getAcceptedDate()!=null){
            	 cell_G.setCellValue(dateFormat.format(revTask.getAcceptedDate()));
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
             //see if this task has been rejected and if so color the line in red
             if (activeTask!=null)
             {
               Vector workItems = activeTask.getWorkItems();
               for (int i=0; i < workItems.size(); i++)
               {
                   EnvoyWorkItem ewi = (EnvoyWorkItem) workItems.get(i);
                   if (WorkflowConstants.TASK_DECLINED == ewi.getWorkItemState()) {
                     wasRejected=true;
                     rejectorName = UserUtil.getUserNameById(ewi.getAssignee());
                     break;
                   }
               }
             }
             
             if (wasRejected) {
            	 Font rejectedFont = p_workbook.createFont();
            	 rejectedFont.setUnderline(Font.U_NONE);
                 rejectedFont.setFontName("Times");
                 rejectedFont.setFontHeightInPoints((short) 11);
                 rejectedFont.setBoldweight(Font.BOLDWEIGHT_BOLD);
                 rejectedFont.setColor(IndexedColors.RED.getIndex());
                 CellStyle rejectedStyle = p_workbook.createCellStyle();
                 rejectedStyle.setFont(rejectedFont);
                 
                 cell_G.setCellValue(bundle.getString("lb_rejected_by") + " " + rejectorName);
                 cell_G.setCellStyle(rejectedStyle);
             }
             else {
            	 cell_G.setCellValue(bundle.getString("lb_not_accepted_yet"));
            	 cell_G.setCellStyle(getContentStyle(p_workbook));            
             }
           }
        }
        else
        {
        	cell_G.setCellValue(bundle.getString("lb_no_review"));
        	cell_G.setCellStyle(getContentStyle(p_workbook));                    
        }
        
        //2.12	Reviewer name: If not accepted, Insert all assignee(s
        // assigned to the Dell_Review activity in the current workflow,
        //delimited by commas; add multiple names if there are multiple
        //assignees, until reaching 10 (cut it off at 10 names).
        //If activity is accepted, put in name of accepter.
        Cell cell_H = getCell(workbflowRow, c++);
        if (revTask==null)
        {
        	cell_H.setCellValue(bundle.getString("lb_no_review"));
        	cell_H.setCellStyle(getContentStyle(p_workbook));
        }
        else if (revAccepted)
        {
        	//get accepter somehow
         	cell_H.setCellValue(UserUtil.getUserNameById(revTask.getAcceptor()));
            cell_H.setCellStyle(getContentStyle(p_workbook));
        }
        else
        {
			//get assignees
			List assigneeList = null;
			if (revTaskInfo!=null)
				assigneeList = revTaskInfo.getTaskAssignees();
			if (assigneeList==null)
			{
				assigneeList=new ArrayList();
			}

			StringBuffer assignees = new StringBuffer();
			int count = 0;
			Iterator iter = assigneeList.iterator();
			while (iter.hasNext())
			{
				TaskAssignee assignee = (TaskAssignee) iter.next();
				assignees.append(UserUtil.getUserNameById(assignee.getUserId()));
				if (count++ == 10) {
					break;
				}
				else
				{
					if (iter.hasNext())
						assignees.append(",");
				}
			}
			cell_H.setCellValue(assignees.toString());
			cell_H.setCellStyle(getContentStyle(p_workbook));
        }     
          
        row.inc();
    }

    /**
     * Returns search params used to find the in progress
     * jobs for all PMs
     * 
     * @return JobSearchParams
     */
    private JobSearchParameters getSearchParams(HttpServletRequest p_request)
    throws Exception
    {
        JobSearchParameters sp = new JobSearchParameters();

        ArrayList<String> stateList = new ArrayList<String>();        
        if (paramStatus != null && "*".equals(paramStatus[0])==false)
        {
          for (int i=0; i < paramStatus.length; i++)
          {
            stateList.add(paramStatus[i]);
          }
        }
        else
        {
          //just do a query for all in progress jobs, localized, and exported
          stateList.add(Job.DISPATCHED);
          stateList.add(Job.LOCALIZED);
          stateList.add(Job.EXPORTED);
        }
        sp.setJobState(stateList);

        //search by project        
        ArrayList<Long> projectIdList = new ArrayList<Long>();
        for (int i = 0; i < paramProjectIds.length; i++)
        {
            String id = paramProjectIds[i];
            if (id.equals("*"))
            {
                try
                {
                    List<Project> projectList = (ArrayList<Project>) ServerProxy
                            .getProjectHandler().getProjectsByUser(userId);
                    for (Project project : projectList)
                    {
                        projectIdList.add(project.getIdAsLong());
                    }
                    break;
                }
                catch (Exception e)
                {
                }
            }
            else
            {
                projectIdList.add(new Long(id));
            }
        }
        
        sp.setProjectId(projectIdList);
        
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd/yyyy");
        String paramCreateDateStartCount = p_request.getParameter(JobSearchConstants.CREATION_START);
        if (paramCreateDateStartCount != null && paramCreateDateStartCount !="")
        {
                sp.setCreationStart(simpleDateFormat.parse(paramCreateDateStartCount));
        }

        String paramCreateDateEndCount = p_request.getParameter(JobSearchConstants.CREATION_END);
        if (paramCreateDateEndCount != null && paramCreateDateEndCount != "")
        {
        	Date date = simpleDateFormat.parse(paramCreateDateEndCount);
        	long endLong = date.getTime()+(24*60*60*1000-1);
            sp.setCreationEnd(new Date(endLong));
        }

        return sp;
    }
    
    private CellStyle getHeaderStyle(Workbook p_workbook){
    	if (headerStyle == null)
        { 
	    	Font headerFont = p_workbook.createFont();
	        headerFont.setBoldweight(Font.BOLDWEIGHT_BOLD);
	        headerFont.setColor(IndexedColors.BLACK.getIndex());
	        headerFont.setUnderline(Font.U_NONE);
	        headerFont.setFontName("Times");
	        headerFont.setFontHeightInPoints((short) 11);
	    	
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
        return ReportConstants.ONLINE_REVIEW_STATUS_REPORT;
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