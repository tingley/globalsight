<%@ page contentType="application/vnd.ms-excel"
        errorPage="/envoy/common/error.jsp"
        import="jxl.*,jxl.write.*,jxl.format.*,
        com.globalsight.everest.servlet.util.ServerProxy,
        com.globalsight.everest.jobhandler.*,
        com.globalsight.everest.foundation.SearchCriteriaParameters,
        com.globalsight.everest.webapp.pagehandler.projects.workflows.JobSearchConstants,
        com.globalsight.everest.foundation.User,
        com.globalsight.everest.workflowmanager.Workflow,
		com.globalsight.everest.workflowmanager.WorkflowManagerWLRemote,
        com.globalsight.everest.workflowmanager.WorkflowOwner,     
        com.globalsight.everest.jobhandler.Job,           
        com.globalsight.everest.page.SourcePage,
        com.globalsight.everest.workflow.WorkflowTaskInstance,
        com.globalsight.everest.workflow.EnvoyWorkItem,
        com.globalsight.everest.workflow.WorkflowConstants,
        com.globalsight.everest.workflow.Activity,
        com.globalsight.everest.projecthandler.Project,
        com.globalsight.util.IntHolder,
        com.globalsight.util.SortUtil,
        com.globalsight.everest.taskmanager.TaskAssignee,
        com.globalsight.everest.util.comparator.JobComparator,
        com.globalsight.everest.taskmanager.Task,
        com.globalsight.everest.taskmanager.TaskInfo,
        com.globalsight.everest.webapp.pagehandler.administration.reports.ActivityDurationReport,
        jxl.write.Number,
        java.text.SimpleDateFormat,
        java.util.*,java.io.*,com.globalsight.everest.webapp.pagehandler.PageHandler,
                com.globalsight.everest.webapp.pagehandler.administration.reports.ReportsMainHandler,
                com.globalsight.everest.webapp.pagehandler.administration.reports.CustomExternalReportInfoBean, 
                com.globalsight.everest.company.CompanyThreadLocal,
                com.globalsight.everest.webapp.pagehandler.administration.users.UserUtil,
                com.globalsight.everest.usermgr.UserLdapHelper, 
                com.globalsight.everest.webapp.WebAppConstants,
                inetsoft.sree.RepletRegistry,
                java.util.Date,
                com.globalsight.ling.common.URLEncoder,
                java.util.Enumeration,
                java.util.ResourceBundle,
                java.util.ArrayList,
                java.util.Iterator,
                org.apache.log4j.Logger" session="true"
%><%!
	private final String STR_ACTIVITY_NOT_ACCEPTED = "not accepted";
	private final String STR_ACTIVITY_NOT_COMPLETED = "not completed";
	private final String STR_ACTIVITY_STATUS_ACCEPTED = "Accepted";
	private final String STR_ACTIVITY_STATUS_AVAILABLE = "Available";
	private final String STR_ACTIVITY_STATUS_COMPLETED = "Completed";
	private final String STR_ACTIVITY_STATUS_SKIP= "Skipped";
	private final String STR_DATES_FROM_PREVIOUS_PASS = "loop dates";
	private final String STR_HEADER_ACTIVITY_ACCEPTED_DATE = "Accepted";
	private final String STR_HEADER_ACTIVITY_AVAILABLE_DATE = "Available";
	private final String STR_HEADER_ACTIVITY_COMPLETED_DATE = "Completed";
	private final String STR_HEADER_ACTIVITY_DURATION = "Duration (minutes)";
	private final String STR_HEADER_ACTIVITY_EXPORT= "Exported";
	private final String STR_HEADER_ACTIVITY_ID = "Activity ID";
	private final String STR_HEADER_ACTIVITY_NAME = "Activity Name";
	private final String STR_HEADER_ACTIVITY_STATUS = "Activity Status";
	private final String STR_HEADER_JOB_ID = "Job ID";
	private final String STR_HEADER_JOB_NAME = "Job Name";
	private final String STR_HEADER_TARGET_LOCALE = "Locale";
	private final String STR_HEADER_WORDCOUNT = "Word Count";
	private final String STR_HEADER_WORKFLOW_STATUS = "Workflow Status";
	private final String STR_NO_TASKS_IN_DEFAULT_PATH = "No tasks in default path";
	private final String STR_WORKSHEET_1_TITLE = "Activity Duration Report";
	private String dateFormat = "dd-MM-yyyy HH:mm";
	
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
	private final int COL_ACTIVITY_EXPORT= 12;
	
	private WritableCellFormat _intFormat =
		new WritableCellFormat( NumberFormats.INTEGER );
	private WritableCellFormat _dateFormat =
		new WritableCellFormat( new DateFormat( dateFormat ) );
    
    private static Logger s_logger =
        Logger.getLogger("Reports");
	 
	/**
     * Generates the Excel report and spits it to the outputstream
     * The report consists of all in progress workflows that are
     * currently at a reviewOnly stage.
     * 
     * @return File
     * @exception Exception
     */
    public void generateReport( HttpServletRequest p_request, HttpServletResponse p_response ) throws Exception
    {
    	_wfMgr = ServerProxy.getWorkflowManager();
    	_currentTimeMillis = System.currentTimeMillis();
    	
        WorkbookSettings settings = new WorkbookSettings();
        settings.setSuppressWarnings( true );
        _workbook = Workbook.createWorkbook( p_response.getOutputStream(), settings );
        
        addJobs(p_request);
        
        _workbook.write();
        _workbook.close();
    }

    /**
     * Adds the table header to the sheet
     * 
     * @param p_sheet
     */
    private void addHeader( WritableSheet sheet ) throws Exception {
        //headerFont is black bold on light grey
        WritableFont headerFont = new WritableFont(WritableFont.TIMES, 
                                                   11,
                                                   WritableFont.BOLD,
                                                   false,
                                                   UnderlineStyle.NO_UNDERLINE,
                                                   jxl.format.Colour.BLACK);
        WritableCellFormat headerFormat = new WritableCellFormat(headerFont);
        headerFormat.setWrap(true);
        headerFormat.setBackground(jxl.format.Colour.GRAY_25);
        headerFormat.setShrinkToFit(false);
        headerFormat.setBorder(jxl.format.Border.ALL,
                               jxl.format.BorderLineStyle.THICK,
                               jxl.format.Colour.BLACK);
        
		int row=0;
        sheet.addCell(new Label(COL_JOB_ID,row,STR_HEADER_JOB_ID,headerFormat));
        sheet.setColumnView(COL_JOB_ID,10);                
        sheet.addCell(new Label(COL_JOB_NAME,row,STR_HEADER_JOB_NAME,headerFormat));
        sheet.setColumnView(COL_JOB_NAME,30);        
        sheet.addCell(new Label(COL_WORDCOUNT,row,STR_HEADER_WORDCOUNT,headerFormat));
        sheet.addCell(new Label(COL_LOCALE,row,STR_HEADER_TARGET_LOCALE,headerFormat));
        sheet.addCell(new Label(COL_WORKFLOW_STATUS,row,STR_HEADER_WORKFLOW_STATUS,headerFormat));
        sheet.setColumnView(COL_WORKFLOW_STATUS,25);
        
        sheet.addCell(new Label(COL_ACTIVITY_ID,row,STR_HEADER_ACTIVITY_ID,headerFormat));
        sheet.setColumnView(COL_ACTIVITY_ID,25);
        sheet.addCell(new Label(COL_ACTIVITY_NAME,row,STR_HEADER_ACTIVITY_NAME,headerFormat));
        sheet.setColumnView(COL_ACTIVITY_NAME,25);
        sheet.addCell(new Label(COL_ACTIVITY_STATUS,row,STR_HEADER_ACTIVITY_STATUS,headerFormat));
        sheet.setColumnView(COL_ACTIVITY_STATUS,25);
        sheet.addCell(new Label(COL_ACTIVITY_AVAILABLE,row,STR_HEADER_ACTIVITY_AVAILABLE_DATE,headerFormat));
        sheet.setColumnView(COL_ACTIVITY_AVAILABLE,20);
        sheet.addCell(new Label(COL_ACTIVITY_ACCEPTED,row,STR_HEADER_ACTIVITY_ACCEPTED_DATE,headerFormat));
        sheet.setColumnView(COL_ACTIVITY_ACCEPTED,25);
        sheet.addCell(new Label(COL_ACTIVITY_COMPLETED,row,STR_HEADER_ACTIVITY_COMPLETED_DATE,headerFormat));
        sheet.setColumnView(COL_ACTIVITY_COMPLETED,20);
        sheet.addCell(new Label(COL_ACTIVITY_DURATION,row,STR_HEADER_ACTIVITY_DURATION,headerFormat));
        sheet.setColumnView(COL_ACTIVITY_DURATION,20);
        sheet.addCell(new Label(COL_ACTIVITY_EXPORT,row,STR_HEADER_ACTIVITY_EXPORT,headerFormat));
        sheet.setColumnView(COL_ACTIVITY_EXPORT,20);
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
        String[] paramProjectIds = p_request.getParameterValues("projectId");
        String[] paramStatus = p_request.getParameterValues("status");
        JobSearchParameters sp = new JobSearchParameters();
        
        ArrayList stateList = new ArrayList();        
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
            stateList.add(Job.EXPORT_FAIL);
        }
        sp.setJobState(stateList);
        
        //search by project        
        ArrayList projectIdList = new ArrayList();
        boolean wantsAllProjects = false;
        for (int i=0; i < paramProjectIds.length; i++)
        {
            String id = paramProjectIds[i];
            if (id.equals("*")==false)
                projectIdList.add(new Long(id));
            else
            {
                wantsAllProjects=true;
                break;
            }
        }
        if (wantsAllProjects==false)
        {
            sp.setProjectId(projectIdList);
        }
        
        String paramCreateDateStartCount = p_request.getParameter(JobSearchConstants.CREATION_START);
        String paramCreateDateStartOpts = p_request.getParameter(JobSearchConstants.CREATION_START_OPTIONS);
        // "-1" is a default value that indicate user does not put value in web form.
        // so ignore creation start date for search parameter.
        if ("-1".equals(paramCreateDateStartOpts)==false)
        {
            sp.setCreationStart(new Integer(paramCreateDateStartCount));
            sp.setCreationStartCondition(paramCreateDateStartOpts);
        }
        
        String paramCreateDateEndCount = p_request.getParameter(JobSearchConstants.CREATION_END);
        String paramCreateDateEndOpts = p_request.getParameter(JobSearchConstants.CREATION_END_OPTIONS);
        if (SearchCriteriaParameters.NOW.equals(paramCreateDateEndOpts))
        {
            sp.setCreationEnd(new java.util.Date());
        }
        // "-1" is a default value that indicate user does not put value in web form.
        // so ignore creation end date for search parameter.
        else if ("-1".equals(paramCreateDateEndOpts)==false)
        {
            sp.setCreationEnd(new Integer(paramCreateDateEndCount));
            sp.setCreationEndCondition(paramCreateDateEndOpts);
        }
        
        return sp;
    }
    
    /**
     * Get parameters from request perepare to get jobs.
     */
    private void addJobs(HttpServletRequest p_request) throws Exception {
        // print out the request parameters
        String[] paramJobId = p_request.getParameterValues("jobId");
        String[] paramTrgLocales = p_request.getParameterValues("targetLocalesList");
        String dateFormtParameter = p_request.getParameter("dateFormat");
        JobSearchParameters searchParams = getSearchParams(p_request);
        addJobs(paramJobId, paramTrgLocales, dateFormtParameter, searchParams);
    }

    /**
     * Gets the jobs and outputs workflow information.
     * @exception Exception
     */
    private void addJobs(String[] paramJobId,
            String[] paramTrgLocales, 
            String dateFormtParameter,
            JobSearchParameters searchParams) throws Exception {        
        
        if (dateFormtParameter != null) {
            dateFormat = new String(dateFormtParameter);
            _dateFormat =
                new WritableCellFormat( new DateFormat( dateFormat ) );
            _intFormat =
                new WritableCellFormat( NumberFormats.INTEGER );
        }

    	// Create a worksheet and add the header
        WritableSheet sheet = _workbook.createSheet( STR_WORKSHEET_1_TITLE, 0 );
        addHeader( sheet );
        
        ArrayList jobs = new ArrayList();
        if (paramJobId!=null && "*".equals(paramJobId[0]))
        {
            jobs.addAll(ServerProxy.getJobHandler().getJobs(searchParams));
            //sort jobs by job name
            SortUtil.sort(jobs, new JobComparator(Locale.US));
        }
        else
        {
            //just get the specific jobs they chose
            for (int i=0; i<paramJobId.length;i++)
            {
                if ("*".equals(paramJobId[i])==false)
                {
                    long jobId = Long.parseLong(paramJobId[i]);
                    Job job = ServerProxy.getJobHandler().getJobById(jobId);
                    jobs.add(job);
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
    	
        Iterator jobIter = jobs.iterator();
        int row = 1;	// 1 header row already filled
        while (jobIter.hasNext())
        {
            Job job = (Job) jobIter.next();
            Collection c = job.getWorkflows();
            Iterator wfIter = c.iterator();
            while (wfIter.hasNext())
            {
                Workflow w = (Workflow) wfIter.next();
                String state = w.getState();
                //skip certain workflow whose target locale is not selected
                String trgLocale = w.getTargetLocale().toString();
                if (!wantsAllLocales && !trgLocaleList.contains(trgLocale)) 
                {
                    continue;
                }
                if (Workflow.EXPORTED.equals(state) ||
                        Workflow.DISPATCHED.equals(state) ||
                        Workflow.LOCALIZED.equals(state))
                {
                    row = addWorkflow( job, w, sheet, row );
		    row++;
                }
            }
        }
        
    }
    
    private int addWorkflow( Job job, Workflow w, WritableSheet sheet, int row ) throws Exception {
        
        // Get all activities of this workflow
        List taskInfos = null;
        
        taskInfos = _wfMgr.getTaskInfosInDefaultPathWithSkip( w );       
    	
    	if( taskInfos == null || taskInfos.size() == 0 ) {
	        // Job information, common to all workflows of this job
            sheet.addCell( new Number( COL_JOB_ID, row, job.getJobId(), _intFormat ) );
            sheet.addCell( new Label( COL_JOB_NAME, row, job.getJobName() ) );
            sheet.addCell( new Number( COL_WORDCOUNT, row, w.getTotalWordCount(), _intFormat ) );
	        
	        // Workflow specific information, common to all activities
	        sheet.addCell( new Label( COL_LOCALE, row, w.getTargetLocale().getDisplayName() ) );
	        sheet.addCell( new Label( COL_WORKFLOW_STATUS, row, w.getState() ) );
	        
            sheet.addCell( new Label( COL_ACTIVITY_NAME, row, STR_NO_TASKS_IN_DEFAULT_PATH ) );
    	} else {
    		final int ACCEPTED = Task.STATE_ACCEPTED;	// 8
    		final int ACTIVE = Task.STATE_ACTIVE;		// 3
		final int COMPLETED = 5; 
		final int SKIP = Task.STATE_SKIP; 
            // Task.STATE_COMPLETED is -1;	
            // But completed task goes here change to 5
    		final int UNKNOWN = Task.STATE_NOT_KNOWN;	// 0
    		
    		// Which row stores the information of the incomplete activity
            int incompleteActivityRow = -1;
            TaskInfo incompleteActivity = null;
            int firstRow = -1;
    		
    		Iterator it = taskInfos.iterator();
    		TaskInfo prevTask = null;
    		
    		// Write out information for each task, ie. Activity
    		while( it.hasNext() ) {
    			TaskInfo ti = (TaskInfo)it.next();
			boolean skipped = false;
    			// Ignore activities that have not even become available yet
    			if( ti.getState() == UNKNOWN )
    				continue;
    			
    			/*
    			 * We have following activities for an activity that has not been completed
    			 * which essentially means that the workflow was moved back to a previous
    			 * activity.
    			 * In this case, the time when the incomplete activity become available is
    			 * incorrect and needs to be adjusted. Additionally, the information of (at
    			 * least) the following activity is not correct either, so we ignore all of
    			 * the following activities for now
    			 */
    			if( incompleteActivity != null ) {
    				// Is this the last activity that has been done before the workflow
    				// moved 'backwards'? If no, we can just ignore this activity
    				if( !it.hasNext() ) {
	    				/* This is the last activity that was done before the workflow was
	    				 * moved 'backwards'. Our completion time needs to be the the time
	    				 * the new active instance of the previously performed activity became
	    				 * available (again).
	    				 */
                        if (ti.getCompletedDate() != null) {
	        	        long taskBecomeAvailableTime = ti.getCompletedDate().getTime();
	    				long taskDurationEndMillis = _currentTimeMillis; // we know its incomplete
	    				long taskDuration = taskDurationEndMillis - taskBecomeAvailableTime;
	    				// Make this minutes
	    				taskDuration /= 1000;	// get it in seconds
	    				taskDuration /= 60;		// in minutes
	    				
	    				// Update the row displaying that incomplete activity
	        	        sheet.addCell( new DateTime( COL_ACTIVITY_AVAILABLE, incompleteActivityRow,
                                    ti.getCompletedDate(), _dateFormat));
                            
	        	        sheet.addCell( new Number( COL_ACTIVITY_DURATION, incompleteActivityRow,
	        	        		taskDuration, _intFormat ) );
                        } else {
                            s_logger.error("No Completed data for this task:" +
                                    ti.toString());    
                        }
    				}
    				// do not ignore the entry though
    			}
    			
    			if( firstRow == -1 ) {
    				firstRow = row;
    			} else // next row starts
	    	        row++;
    			
    	        // Job information, common to all workflows of this job
                sheet.addCell( new Number( COL_JOB_ID, row, job.getJobId(), _intFormat ) );
                sheet.addCell( new Label( COL_JOB_NAME, row, job.getJobName() ) );
                sheet.addCell( new Number( COL_WORDCOUNT, row, w.getTotalWordCount(), _intFormat ) );
    	        
    	        // Workflow specific information, common to all activities
    	        sheet.addCell( new Label( COL_LOCALE, row, w.getTargetLocale().getDisplayName() ) );
    	        sheet.addCell( new Label( COL_WORKFLOW_STATUS, row, w.getState() ) );
    	        
    	        sheet.addCell( new Number( COL_ACTIVITY_ID, row, ti.getId(), _intFormat ) );
    	        sheet.addCell( new Label( COL_ACTIVITY_NAME, row, ti.getTaskDisplayName() ) );
    	        
    	        long taskBecomeAvailableTime = 0;
                if (job.getCreateDate() == null) {
                    s_logger.error("No Create data for this job:" +
                            job.toString());
                    return row;                  
                } 
                
                if( prevTask == null )  {
                    taskBecomeAvailableTime = job.getCreateDate().getTime();
                } else {
                    if(prevTask.getCompletedDate() != null) {
    	        	taskBecomeAvailableTime = prevTask.getCompletedDate().getTime();
                    } else {
                        taskBecomeAvailableTime = job.getCreateDate().getTime();
                    }
                }
    	        	
    	        // is this a iteration or the first time, ie. are completed and
    	        // accepted date from previous iteration or from this one
    	        switch( ti.getState() ) {
    	        case ACTIVE:
	    	        sheet.addCell( new Label( COL_ACTIVITY_STATUS, row, STR_ACTIVITY_STATUS_AVAILABLE ) );
	    	        incompleteActivity = ti;
	    	        incompleteActivityRow = row;
    	        	break;
    	        case ACCEPTED:
	    	        sheet.addCell( new Label( COL_ACTIVITY_STATUS, row, STR_ACTIVITY_STATUS_ACCEPTED ) );
	    	        incompleteActivity = ti;
	    	        incompleteActivityRow = row;
    	        	break;
    	        case COMPLETED:
	    	        sheet.addCell( new Label( COL_ACTIVITY_STATUS, row, STR_ACTIVITY_STATUS_COMPLETED ) );
    	        	break;
		case SKIP:
	    	        sheet.addCell( new Label( COL_ACTIVITY_STATUS, row, STR_ACTIVITY_STATUS_SKIP) );
			skipped = true;
    	        	break;
    	        case UNKNOWN:
    	        default:
	    	        sheet.addCell( new Label( COL_ACTIVITY_STATUS, row, ti.getStateAsString() ) );
    	        }

		if(skipped) {
		  sheet.addCell(new Label(COL_ACTIVITY_AVAILABLE, row, STR_ACTIVITY_STATUS_SKIP));
		} else {

		  if( incompleteActivity != null && incompleteActivityRow < row )
			  sheet.addCell( new Label( COL_ACTIVITY_AVAILABLE, row, STR_DATES_FROM_PREVIOUS_PASS ) );
		  else
			  sheet.addCell( new DateTime( COL_ACTIVITY_AVAILABLE, row,
					  new Date(taskBecomeAvailableTime), _dateFormat ) );
		  

		}
    	        
		if(skipped) {
		  sheet.addCell(new Label(COL_ACTIVITY_ACCEPTED, row, STR_ACTIVITY_STATUS_SKIP));
		} else {

		  if( ti.getAcceptedDate() != null ) {                    
			  sheet.addCell( new DateTime( COL_ACTIVITY_ACCEPTED, row,
					  ti.getAcceptedDate(), _dateFormat ) );
		  } else {
			  sheet.addCell( new Label( COL_ACTIVITY_ACCEPTED, row, STR_ACTIVITY_NOT_ACCEPTED ) );
		  }
		}

		long taskDurationEndMillis = -1;
    	        
		if(skipped) {
		  sheet.addCell(new Label(COL_ACTIVITY_COMPLETED, row, STR_ACTIVITY_STATUS_SKIP));
		} else {

		  if( ti.getState() != COMPLETED ) {
		  sheet.addCell( new Label( COL_ACTIVITY_COMPLETED, row, STR_ACTIVITY_NOT_COMPLETED ) );
			  taskDurationEndMillis = _currentTimeMillis;
		  } else {
		    if (ti.getCompletedDate() == null) {
			prevTask = ti;
			s_logger.error("No Completed data for this task:" +
				ti.toString());
			continue;
		    }
		    sheet.addCell( new DateTime( COL_ACTIVITY_COMPLETED, row,
				  ti.getCompletedDate(), _dateFormat ) );
		    taskDurationEndMillis = ti.getCompletedDate().getTime();
		  }
		}

		if(skipped) {
		  sheet.addCell(new Label(COL_ACTIVITY_DURATION, row, STR_ACTIVITY_STATUS_SKIP));
		} else {
		  long taskDuration = taskDurationEndMillis - taskBecomeAvailableTime;
				  
				  // Make this minutes
		  taskDuration /= 1000;	// get it in seconds
		  taskDuration /= 60;		// in minutes
		  sheet.addCell( new Number( COL_ACTIVITY_DURATION, row, taskDuration, _intFormat ) );
		}


		Date exportDate = ti.getExportDate();
		if(exportDate != null) {
		  sheet.addCell( new DateTime( COL_ACTIVITY_EXPORT, row, exportDate, _dateFormat) );
		}
				
    			prevTask = ti;
    		}
    	}
		return (row+1);
    }

%><%
//Multi-Company: get current user's company from the session
HttpSession userSession = request.getSession(false);
String companyName = (String)userSession.getAttribute(WebAppConstants.SELECTED_COMPANY_NAME_FOR_SUPER_PM);
if (UserUtil.isBlank(companyName))
{
    companyName = (String)userSession.getAttribute(UserLdapHelper.LDAP_ATTR_COMPANY);
}
if (companyName != null)
{
    CompanyThreadLocal.getInstance().setValue(companyName);
}
ActivityDurationReport activityDurationReport = new ActivityDurationReport();

response.setHeader("Content-Disposition","attachment; filename=ActivityDuration.xls" );
response.setHeader("Expires", "0");
response.setHeader("Cache-Control","must-revalidate, post-check=0,pre-check=0");
response.setHeader("Pragma","public"); 
activityDurationReport.generateReport( request, response );
out.clear();out = pageContext.pushBody();
%>
