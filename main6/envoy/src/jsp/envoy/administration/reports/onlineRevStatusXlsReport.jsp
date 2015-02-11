<%@ page contentType="application/vnd.ms-excel"
        errorPage="/envoy/common/error.jsp"
        import="jxl.*,jxl.write.*,jxl.format.*,
        com.globalsight.everest.servlet.util.ServerProxy,
        com.globalsight.everest.jobhandler.*,
        com.globalsight.everest.foundation.SearchCriteriaParameters,
        com.globalsight.everest.webapp.pagehandler.projects.workflows.JobSearchConstants,
        com.globalsight.everest.foundation.User,
        com.globalsight.everest.workflowmanager.Workflow,
        com.globalsight.everest.workflowmanager.WorkflowOwner,     
        com.globalsight.everest.jobhandler.Job,           
        com.globalsight.everest.page.SourcePage,
        com.globalsight.everest.workflow.WorkflowTaskInstance,
        com.globalsight.everest.workflow.EnvoyWorkItem,
        com.globalsight.everest.workflow.WorkflowConstants,
        com.globalsight.everest.workflow.Activity,
        com.globalsight.everest.projecthandler.Project,
        com.globalsight.util.IntHolder,
        com.globalsight.everest.taskmanager.TaskAssignee,
        com.globalsight.everest.util.comparator.JobComparator,
        com.globalsight.everest.taskmanager.Task,
        com.globalsight.everest.taskmanager.TaskInfo,
        com.globalsight.everest.company.CompanyWrapper,
        jxl.write.Number,
        java.text.SimpleDateFormat,
        java.util.*,
        java.io.*,
        com.globalsight.everest.webapp.pagehandler.PageHandler,
                com.globalsight.everest.webapp.pagehandler.administration.reports.ReportsMainHandler,
                com.globalsight.everest.webapp.pagehandler.administration.reports.CustomExternalReportInfoBean,
                com.globalsight.everest.webapp.WebAppConstants,
                inetsoft.sree.RepletRegistry,
                java.util.Date,
                com.globalsight.ling.common.URLEncoder,
                java.util.Enumeration,
                java.util.ResourceBundle,
                java.util.ArrayList,
                java.util.Iterator,
                org.apache.log4j.Logger,
                com.globalsight.everest.company.CompanyThreadLocal,
                com.globalsight.everest.webapp.pagehandler.administration.users.UserUtil,
                com.globalsight.everest.usermgr.UserLdapHelper,
                com.globalsight.everest.webapp.WebAppConstants" session="true" 
%><%!
    //String EMEA = CompanyWrapper.getCurrentCompanyName();
    private static Logger s_logger = Logger.getLogger("Reports");
    private WritableWorkbook m_workbook = null;
    
    /**
     * Generates the Excel report and spits it to the outputstream
     * The report consists of all in progress workflows that are
     * currently at a reviewOnly stage.
     * 
     * @return File
     * @exception Exception
     */
    private void generateReport(HttpServletRequest p_request, HttpServletResponse p_response) throws Exception
    {
        WorkbookSettings settings = new WorkbookSettings();
        settings.setSuppressWarnings(true);
        m_workbook = Workbook.createWorkbook(p_response.getOutputStream(), settings);
        addJobs(p_request);
        m_workbook.write();
        m_workbook.close();
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
        else if ("-1".equals(paramCreateDateEndOpts)==false)
        {
            sp.setCreationEnd(new Integer(paramCreateDateEndCount));
            sp.setCreationEndCondition(paramCreateDateEndOpts);
        }

        return sp;
    }


    /**
     * Adds the table header to the sheet
     * 
     * @param p_sheet
     */
    private void addHeader(WritableSheet p_sheet, ResourceBundle bundle) throws Exception
    {
        //title font is black bold on white
        String EMEA = CompanyWrapper.getCurrentCompanyName();
        WritableFont titleFont = new WritableFont(WritableFont.TIMES, 
                                             14,
                                             WritableFont.BOLD,
                                             false,
                                             UnderlineStyle.NO_UNDERLINE,
                                             jxl.format.Colour.BLACK);
        WritableCellFormat titleFormat = new WritableCellFormat(titleFont);
        titleFormat.setWrap(false);
        titleFormat.setShrinkToFit(false);
        p_sheet.addCell(new Label(0,0,EMEA + " " + bundle.getString("online_review_status") ,titleFormat));
        p_sheet.setColumnView(0,22);


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
        int col=0;
        int row=3;
        p_sheet.addCell(new Label(col++,row,bundle.getString("job_id"),headerFormat));
        p_sheet.setColumnView(col -1,10);                
        p_sheet.addCell(new Label(col++,row,bundle.getString("lb_job"),headerFormat));
        p_sheet.setColumnView(col -1,50);
        p_sheet.addCell(new Label(col++,row,bundle.getString("lb_lang"),headerFormat));
        p_sheet.addCell(new Label(col++,row,bundle.getString("word_count"),headerFormat));
        p_sheet.addCell(new Label(col++,row,bundle.getString("lb_actual_date_to_review"),headerFormat));
        p_sheet.setColumnView(col -1,25);
        p_sheet.addCell(new Label(col++,row,bundle.getString("lb_current_activity"),headerFormat));
        p_sheet.setColumnView(col -1,20);
        p_sheet.addCell(new Label(col++,row,bundle.getString("lb_reviewer_accepted"),headerFormat));
        p_sheet.setColumnView(col -1,25);
        p_sheet.addCell(new Label(col++,row,bundle.getString("lb_reviewer_name"),headerFormat));
        p_sheet.setColumnView(col -1,30);
        p_sheet.addCell(new Label(col++,row,bundle.getString("lb_tracking"),headerFormat));
        p_sheet.setColumnView(col -1,20);        
    }

    /**
     * Gets the jobs and outputs workflow information.
     * @exception Exception
     */
    private void addJobs(HttpServletRequest p_request) throws Exception
    {
        ResourceBundle bundle = PageHandler.getBundle(p_request.getSession());
        //print out the request parameters
        String[] paramJobId = p_request.getParameterValues("jobId");
        String[] paramTrgLocales = p_request.getParameterValues("targetLocalesList");
        HttpSession session = p_request.getSession();

        WritableSheet sheet = m_workbook.createSheet(bundle.getString("lb_sheet") + "1",0);
        ArrayList jobs = new ArrayList();
        addHeader(sheet, bundle);
        if (paramJobId!=null && "*".equals(paramJobId[0]))
        {
          //do a search based on the params
          JobSearchParameters searchParams = getSearchParams(p_request);
          jobs.addAll(ServerProxy.getJobHandler().getJobs(searchParams));
          //sort jobs by job name
          Collections.sort(jobs, new JobComparator(Locale.US));
        }
        else
        {
          //just get the specific jobs they chose
          for (int i=0; i<paramJobId.length;i++)
          {
           if ("*".equals(paramJobId[i])==false)
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
                                                                            
        Iterator jobIter = jobs.iterator();
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
                //skip certain workflow whose target locale is not selected
                String trgLocale = w.getTargetLocale().toString();
                if (!wantsAllLocales && !trgLocaleList.contains(trgLocale)) 
                {
                	continue;
                }
                if (Workflow.DISPATCHED.equals(state))
                {
                  addWorkflow(p_request,sheet,j,w,row);
                }
            }
        }
    }

    /**
    * Gets the task for the workflow and outputs page information.
    *@exception Exception
    */
    private void addWorkflow(HttpServletRequest p_request, WritableSheet sheet, Job j, Workflow w, IntHolder row) throws Exception
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
        //2.3	Job ID column. Insert Ambassador job number here.
        sheet.addCell(new Label(c++,r, Long.toString(j.getJobId())));
        
        //2.4	Job: Insert Job name here.
        sheet.addCell(new Label(c++,r, j.getJobName()));
        
        //2.5	Lang: Insert each target language identifier for each workflow
        //in the retrieved Job on a different row.
        sheet.addCell(new Label(c++,r,w.getTargetLocale().getLanguageCode()));
        
        //2.6	Word count: Insert source word count for the job.
        sheet.addCell(new Number(c++,r, w.getTotalWordCount()));
        
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
        
        if (revTask==null)
        {
              sheet.addCell(new Label(c++,r, bundle.getString("lb_no_review")));        //2.9
        }
        else if (priorTaskInfo != null && priorTask != null)
        {
        //2.9	Actual date to Review: Insert the date (and time) the "Dell_Review"
        //activity became available for acceptance in the current workflow.
           if (priorTask.getCompletedDate()!=null)
             sheet.addCell(new Label(c++,r, dateFormat.format(priorTask.getCompletedDate())));
           else
              sheet.addCell(new Label(c++,r, bundle.getString("lb_na")));
        }
        else
        {
          sheet.addCell(new Label(c++,r, bundle.getString("lb_na")));
        }
        
        //2.10	Current Activity: Currently active activity in the current workflow.        
        sheet.addCell(new Label(c++,r, activityName));
        
        boolean wasRejected = false;
        String rejectorName = null;

        boolean revAccepted=false;        
        //2.11	Reviewer accepted: If the "Dell_Review" activity has been accepted,
        // insert the date (and time) it was accepted. If not accepted yet,
        // put "not accepted yet".
        if (revTask!=null)
        {
           if (revTask.getAcceptor()!=null)
           {
             revAccepted=true;
             if (revTask.getAcceptedDate()!=null)
               sheet.addCell(new Label(c++,r, dateFormat.format(revTask.getAcceptedDate())));
             else
             {
               sheet.addCell(new Label(c++,r, bundle.getString("lb_na")));    
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
                      WritableFont rejectedFont = new WritableFont(WritableFont.TIMES, 
                                             11,
                                             WritableFont.BOLD,
                                             false,
                                             UnderlineStyle.NO_UNDERLINE,
                                             jxl.format.Colour.RED);
                      WritableCellFormat rejectedFormat = new WritableCellFormat(rejectedFont);
                     sheet.addCell(new Label(c++,r, bundle.getString("lb_rejected_by") + " " + rejectorName,rejectedFormat)); 
             }
             else {
               sheet.addCell(new Label(c++,r, bundle.getString("lb_not_accepted_yet")));             
             }
           }
        }
        else
        {
          sheet.addCell(new Label(c++,r, bundle.getString("lb_no_review")));                     
        }
        
        //2.12	Reviewer name: If not accepted, Insert all assignee(s
        // assigned to the Dell_Review activity in the current workflow,
        //delimited by commas; add multiple names if there are multiple
        //assignees, until reaching 10 (cut it off at 10 names).
        //If activity is accepted, put in name of accepter.
        if (revTask==null)
        {
         sheet.addCell(new Label(c++,r, bundle.getString("lb_no_review")));
        }
        else if (revAccepted)
        {
          //get accepter somehow
          sheet.addCell(new Label(c++,r, UserUtil.getUserNameById(revTask.getAcceptor())));
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
          sheet.addCell(new Label(c++,r, assignees.toString()));
        }
        
          
        row.inc();
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

response.setHeader("Content-Disposition","attachment; filename=OnlineReviewStatus.xls" );
response.setHeader("Expires", "0");
response.setHeader("Cache-Control","must-revalidate, post-check=0,pre-check=0");
response.setHeader("Pragma","public"); 
generateReport(request,response);
%>
