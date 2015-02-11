<%@ page contentType="application/vnd.ms-excel"
        errorPage="/envoy/common/error.jsp"
        import="jxl.*,jxl.write.*,jxl.format.*,
        com.globalsight.everest.servlet.util.ServerProxy,
        com.globalsight.everest.jobhandler.*,
        com.globalsight.everest.foundation.SearchCriteriaParameters,
        com.globalsight.everest.workflowmanager.Workflow,
        com.globalsight.everest.page.SourcePage,
        com.globalsight.everest.workflow.WorkflowTaskInstance,
        com.globalsight.everest.workflow.Activity,
        com.globalsight.everest.projecthandler.Project,
        com.globalsight.util.IntHolder,
        com.globalsight.util.SortUtil,
        com.globalsight.everest.util.comparator.JobComparator,
        com.globalsight.everest.company.CompanyWrapper,
        jxl.write.Number,
        java.util.*,java.io.*,com.globalsight.everest.webapp.pagehandler.PageHandler,
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
    private void generateReport(HttpServletResponse p_response) throws Exception
    {
        WorkbookSettings settings = new WorkbookSettings();
        settings.setSuppressWarnings(true);
        m_workbook = Workbook.createWorkbook(p_response.getOutputStream(), settings);
        addJobs();
        m_workbook.write();
        m_workbook.close();
    }

    /**
     * Returns search params used to find the in progress
     * jobs for all PMs
     * 
     * @return JobSearchParams
     */
    private JobSearchParameters getSearchParams()
    {
        JobSearchParameters sp = new JobSearchParameters();
        //job state DISPATCHED
        ArrayList list = new ArrayList();
        list.add(Job.DISPATCHED);
        sp.setJobState(list);
        return sp;
    }


    /**
     * Adds the table header to the sheet
     * 
     * @param p_sheet
     */
    private void addHeader(WritableSheet p_sheet) throws Exception
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
        p_sheet.addCell(new Label(0,0,EMEA + " Online",titleFormat));
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
        p_sheet.addCell(new Label(col++,row,"Job",headerFormat)); //project name
        p_sheet.setColumnView(col -1,30);        
        p_sheet.addCell(new Label(col++,row,"Description",headerFormat)); //project desc
        p_sheet.setColumnView(col -1,20);
        p_sheet.addCell(new Label(col++,row,"File",headerFormat)); //file
        p_sheet.setColumnView(col -1,40);        
        p_sheet.addCell(new Label(col++,row,"Lang",headerFormat));
        p_sheet.addCell(new Label(col++,row,"Reviewer",headerFormat));
        p_sheet.setColumnView(col -1,20);                
        p_sheet.addCell(new Label(col++,row,"Activity",headerFormat));
        p_sheet.setColumnView(col -1,20);        
        p_sheet.addCell(new Label(col++,row,"Last Action",headerFormat));
        p_sheet.setColumnView(col -1,30);        
        p_sheet.addCell(new Label(col++,row,"Tracking",headerFormat));
        p_sheet.setColumnView(col -1,20);        
    }

    /**
     * Gets the jobs and outputs workflow information.
     * @exception Exception
     */
    private void addJobs() throws Exception
    {
        WritableSheet sheet = m_workbook.createSheet("Sheet1",0);
        addHeader(sheet);
        JobSearchParameters searchParams = getSearchParams();
        ArrayList jobs = new ArrayList(
                                      ServerProxy.getJobHandler().getJobs(searchParams));
                                      
        //sort jobs by job name
        SortUtil.sort(jobs, new JobComparator(Locale.US));
                                                                            
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
                addWorkflow(sheet,j,w,row);
            }
        }
    }

    /**
    * Gets the task for the workflow and outputs page information.
    *@exception Exception
    */
    private void addWorkflow(WritableSheet sheet, Job j, Workflow w, IntHolder row) throws Exception
    {
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
        if (tasks != null && tasks.length > 0)
        {
            //assume just one active task for now
            WorkflowTaskInstance task = (WorkflowTaskInstance)tasks[0];
            //only write out if the Activity type is review only
            Activity a = task.getActivity();
            if (task.getActivity().isType(Activity.TYPE_REVIEW))
                addPage(sheet,j,w,task,row);
        }
    }

    /**
    * Writes out cell data for all the pages in the job
    * @exception
    */
    private void addPage(WritableSheet sheet, Job j, Workflow w, WorkflowTaskInstance task, IntHolder row) throws Exception
    {
        WritableCellFormat dateFormat = new WritableCellFormat(DateFormats.DEFAULT);
        dateFormat.setWrap(false);
        dateFormat.setShrinkToFit(false);
        Project project = j.getL10nProfile().getProject();
        Iterator spIter = j.getSourcePages().iterator();
        while (spIter.hasNext())
        {
            int c=0;
            int r=row.getValue();
            SourcePage sp = (SourcePage) spIter.next();
            String filename = sp.getExternalPageId();
            sheet.addCell(new Label(c++,r, j.getJobName()));
            sheet.addCell(new Label(c++,r,project.getDescription())); //project description
            sheet.addCell(new Label(c++,r,filename)); //file                
            sheet.addCell(new Label(c++,r,w.getTargetLocale().getLanguageCode())); //lang
            String reviewer = task.getAccepter();
            if (reviewer == null)
            {
                reviewer = task.getAllAssigneesAsString();
                // if there is a line break then more than one user listed
                // so replace to be comma delimited
                reviewer = reviewer.replaceAll("<BR>", ", ");
             }
             
            //make the reviewer field wrappable in case there is a list of users                                                   
            WritableCellFormat reviewerFormat = new WritableCellFormat();
            reviewerFormat.setWrap(true);
            sheet.addCell(new Label(c++,r,reviewer, reviewerFormat)); //reviewer
            
            String taskName = task.getActivityDisplayName();
            sheet.addCell(new Label(c++,r,taskName)); //activity
            java.util.Date creationTime = new Date(task.getCreationTime());
            sheet.addCell(new DateTime(c++,r, creationTime,dateFormat)); //last action
            row.inc();
        }
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

response.setHeader("Content-Disposition","attachment; filename=PendingClientReview.xls" );
response.setHeader("Expires", "0");
response.setHeader("Cache-Control","must-revalidate, post-check=0,pre-check=0");
response.setHeader("Pragma","public"); 
generateReport(response);
%>
