<%@page import="com.globalsight.everest.webapp.pagehandler.tasks.TaskListHandler"%>
<%@ taglib uri="/WEB-INF/tlds/globalsight.tld" prefix="amb" %>
<%@ page 
        contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/activityError.jsp"
        import="java.util.*, com.globalsight.everest.page.PageWordCounts,
                 com.globalsight.config.UserParamNames,
                 com.globalsight.everest.page.TargetPage,
                 com.globalsight.everest.permission.Permission,
                 com.globalsight.everest.permission.PermissionSet,
                 com.globalsight.everest.foundation.Timestamp,
                 com.globalsight.everest.servlet.util.SessionManager,
                 com.globalsight.everest.secondarytargetfile.SecondaryTargetFile,
                 com.globalsight.everest.taskmanager.Task,
                 com.globalsight.everest.util.system.SystemConfigParamNames,
                 com.globalsight.everest.util.system.SystemConfiguration,
                 com.globalsight.everest.webapp.WebAppConstants,
                 com.globalsight.everest.webapp.javabean.NavigationBean,
                 com.globalsight.everest.webapp.pagehandler.PageHandler,
                 com.globalsight.everest.webapp.pagehandler.offline.OfflineConstants,
                 com.globalsight.everest.webapp.pagehandler.projects.workflows.JobManagementHandler,  
                 com.globalsight.everest.webapp.pagehandler.administration.customer.download.DownloadFileHandler,
                 com.globalsight.everest.webapp.pagehandler.tasks.WorkflowTaskDataComparator,
                 com.globalsight.everest.webapp.pagehandler.administration.users.*,
                 com.globalsight.util.resourcebundle.ResourceBundleConstants,
                 com.globalsight.util.resourcebundle.SystemResourceBundle,
                 com.globalsight.everest.workflow.WorkflowConstants,
                 com.globalsight.everest.jobhandler.JobHandler,
                 com.globalsight.everest.servlet.util.ServerProxy,
                 java.lang.Boolean,
                 java.text.MessageFormat,
                 java.util.Date,
                 java.util.HashSet,
                 java.util.Iterator,
                 java.util.List,
                 java.util.Locale, 
                 java.util.TimeZone,
                 java.util.ResourceBundle"
        session="true" 
%>
<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>
<jsp:useBean id="self" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="detail" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="invokedownload" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>    
<jsp:useBean id="previousPage" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="nextPage" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="export" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="search" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="wordcountlist" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<% 
   //user id
    User currentUser =(User)((SessionManager)session.getAttribute(WebAppConstants.SESSION_MANAGER)).getAttribute(WebAppConstants.USER);
    boolean showCompany =false;
    if(CompanyWrapper.isSuperCompanyName(currentUser.getCompanyName())){
      showCompany =true;
    }

    //Constants
    final String AND = "&";
    final String EQUAL = "=";
    
    ResourceBundle bundle = PageHandler.getBundle(session);
    SessionManager sessionMgr = (SessionManager)session.getAttribute(WebAppConstants.SESSION_MANAGER);
    PermissionSet perms = (PermissionSet)session.getAttribute(WebAppConstants.PERMISSIONS);
    String title= bundle.getString("lb_my_activities");
    
    String userFormat = PageHandler.getUserParameter(session, UserParamNames.DOWNLOAD_OPTION_FORMAT).getValue();
    boolean isCombinedFormat = false;
    if (OfflineConstants.FORMAT_RTF_TRADOS.equals(userFormat)
    		|| OfflineConstants.FORMAT_RTF_TRADOS_OPTIMIZED.equals(userFormat))
    {
    	isCombinedFormat = true;
    }
    
    boolean canManageProjects = perms.getPermissionFor(Permission.PROJECTS_MANAGE);
    boolean canManageWorkflows = perms.getPermissionFor(Permission.PROJECTS_MANAGE_WORKFLOWS);

    boolean userMayExportAll = true;
    boolean userMayExportInProgress = true;
    boolean isL10nParticipant = false;

    if (!perms.getPermissionFor(Permission.ACTIVITIES_EXPORT))
        userMayExportAll=false;

    if (!perms.getPermissionFor(Permission.ACTIVITIES_EXPORT_INPROGRESS))
        userMayExportInProgress=false;

    //Labels of the column titles
    String labelJobId = bundle.getString("lb_job_id");
    String labelJobName = bundle.getString("lb_job_name");
    String labelCompanyName = bundle.getString("lb_company");
    String labelActivity = bundle.getString("lb_activity"); 
    String labelPriority = bundle.getString("lb_priority");
    String labelSourceLocale = bundle.getString("lb_source_locale");
    String labelTargetLocale = bundle.getString("lb_target_locale"); 
    String lablelSource = bundle.getString("lb_source");
    String lablelTarget = bundle.getString("lb_target");
    String labelAcceptBy = bundle.getString("lb_accept_by");
    String labelDueDate = bundle.getString("lb_due_date");
    String labelCompletedOn = bundle.getString("lb_completed_on");
    String labelAssignees = bundle.getString("lb_assignees");
    String labelWordCount = bundle.getString("lb_source_word_count");
    String msgAcceptDownloadDisabled = bundle.getString("msg_accept_download_disabled");
    String dAbbr = bundle.getString("lb_abbreviation_day");
    String hAbbr = bundle.getString("lb_abbreviation_hour");
    String mAbbr = bundle.getString("lb_abbreviation_minute");
    String offlineUpload = bundle.getString("lb_offline_upload");
         
    //Urls of the links on this page
    String selfUrl = self.getPageURL();
    String detailUrl = detail.getPageURL() + "&" + WebAppConstants.TASK_ACTION + 
        "=" + WebAppConstants.TASK_ACTION_RETRIEVE;
    String previousPageUrl = previousPage.getPageURL();
    String nextPageUrl = nextPage.getPageURL();
    String searchUrl = search.getPageURL() + "&action=search";
    //String wordCountUrl = wordcount.getPageURL();
    String wordCountListUrl = wordcountlist.getPageURL();    

    String acceptAndDownloadUrl = self.getPageURL() + "&" + WebAppConstants.TASK_ACTION + 
        "=" + WebAppConstants.TASK_ACTION_ACCEPT_AND_DOWNLOAD;
    String sendDownloadFileUrl = invokedownload.getPageURL();
    String url_offlineUploadPage =
            "/globalsight/ControlServlet?activityName=simpleofflineupload";
    // Label and links for dates (accepted by, due by, or completed) 
    // decided based on the selected state
    String labelABorDBorCODate;
    String urlABorDBorCOColLink; 
    
    //define task states 
    final int stateAvailable = Task.STATE_ACTIVE;
    final int stateInProgress = Task.STATE_ACCEPTED;
    final int stateFinished = Task.STATE_COMPLETED;
    final int stateRejected = Task.STATE_REJECTED;
    final int stateAll = Task.STATE_ALL;

    //Names of the HttpRequest parameters, defined in constants
    String taskListParam = WebAppConstants.TASK_LIST;
    String stateParam = WebAppConstants.TASK_STATE;
    String taskTypeParam = WebAppConstants.TASK_TYPE;
    String columnNumParam = WebAppConstants.MYACT_COL_SORT_ID;
    String taskIdParam = WebAppConstants.TASK_ID;
    String taskListStartParam = WebAppConstants.TASK_LIST_START;
    String isRefreshParam = WebAppConstants.IS_REFRESH;
    String sortAscParam = WebAppConstants.MYACT_SORT_ASC;

    //get tasks, state and selected column passed by PageHandler
    List tasks = (List)sessionMgr.getAttribute(taskListParam);

    int state = ((Integer)request.getAttribute(stateParam)).intValue();
    HashSet languageSet = (HashSet) request.getAttribute("languageSet");
    String selectedColumn = ((Integer)request.getAttribute(columnNumParam)).toString();
    String sortAscStr = (String)request.getAttribute(sortAscParam); // should never be null
    boolean sortAsc = sortAscStr.toLowerCase().equals("true") ? true : false;
    
    String errorMsg = (String) sessionMgr.getAttribute("taskList_errorMsg");

    // common info used for column header links

    // reverse sort directive
    StringBuffer sortBuffer = new StringBuffer();
    sortBuffer.append(AND);
    sortBuffer.append(sortAscParam);
    sortBuffer.append(EQUAL);
    sortBuffer.append(sortAscStr.toLowerCase().equals("true") ? "false" : "true"); // reverse
    String reverseSortParam = sortBuffer.toString();

    StringBuffer prefixBuffer = new StringBuffer();
    prefixBuffer.append(selfUrl);
    prefixBuffer.append(AND);
    prefixBuffer.append(columnNumParam);
    prefixBuffer.append(EQUAL);
    String prefix = prefixBuffer.toString();

    StringBuffer suffixBuffer = new StringBuffer();
    suffixBuffer.append(AND);
    suffixBuffer.append(stateParam);
    suffixBuffer.append(EQUAL);
    String suffix = suffixBuffer.toString();

    StringBuffer sb = new StringBuffer();
    sb.append(prefix);
    sb.append(selectedColumn);  // get passed to other task pages
    sb.append(suffix);
    String commonLink = sb.toString();

    // column header suffix
    StringBuffer headerSuffixBuffer = new StringBuffer();
    headerSuffixBuffer.append(suffix);
    headerSuffixBuffer.append(state);
    String headerSuffix = headerSuffixBuffer.toString();


    // Get control names used by normal download page
    // We use these to set the request parameters - same as from download page   
    String requestParamFileFormat = OfflineConstants.FORMAT_SELECTOR;
    String requestParamEditor = OfflineConstants.EDITOR_SELECTOR;
    String requestParamEncoding = OfflineConstants.ENCODING_SELECTOR;
    String requestParamPtagFormat = OfflineConstants.PTAG_SELECTOR;
    String requestParamTMEditType = OfflineConstants.TM_EDIT_TYPE;
    String requestParamResInsMode = OfflineConstants.RES_INS_SELECTOR;
    String requestParamAcceptDownloadRequest = OfflineConstants.DOWNLOAD_ACCEPT_DOWNLOAD;
    String requestParamDownloadAction = WebAppConstants.DOWNLOAD_ACTION;
    String startDownload = WebAppConstants.DOWNLOAD_ACTION_START_DOWNLOAD;

    // get the Download request flag
    Boolean acceptDownloadRequested =
        (Boolean)request.getAttribute(requestParamAcceptDownloadRequest);
    if( acceptDownloadRequested == null)
    {
        acceptDownloadRequested = new Boolean(false);
    }
    
    Boolean isDownloadCombined = (Boolean)request.getAttribute("isDownloadCombined");
    if( isDownloadCombined == null)
    {
        isDownloadCombined = new Boolean(false);
    }
    
    String taskParam =
        (String)request.getAttribute("taskParam");
    if(taskParam == null)
    {
        taskParam = "";
    }
   
    String downloadFileFormat = 
        PageHandler.getUserParameter
        (session, UserParamNames.DOWNLOAD_OPTION_FORMAT).getValue();
    String downloadNameEditor = 
        PageHandler.getUserParameter
        (session, UserParamNames.DOWNLOAD_OPTION_EDITOR).getValue();
    String downloadNameEncoding = 
        PageHandler.getUserParameter
        (session, UserParamNames.DOWNLOAD_OPTION_ENCODING).getValue();
    String downloadNamePtagFormat = 
        PageHandler.getUserParameter
        (session, UserParamNames.DOWNLOAD_OPTION_PLACEHOLDER).getValue();
    String downloadNameTMEditType = 
            PageHandler.getUserParameter
            (session, UserParamNames.DOWNLOAD_OPTION_TM_EDIT_TYPE).getValue();
    String downloadNameResInsMode = 
        PageHandler.getUserParameter
        (session, UserParamNames.DOWNLOAD_OPTION_RESINSSELECT).getValue();
    
    int dateColumn = 4;
    int tableWidth = 675;
    int colspan = 11;
    int statusSelected = 0;
    String helperText = "";
    String helpFile = bundle.getString("help_main");
    String subTitle = "";
    String tdSizeInProgress = "";
    switch (state)
    {
        case stateAvailable:
            statusSelected = 0;
            dateColumn = 7;
            tableWidth = 795;
            colspan = 12;
            //  To maintain the screen real estate, the accept-by column is displayed
            // instead of "Due Date" when showing available tasks.
            labelABorDBorCODate = labelAcceptBy;
            urlABorDBorCOColLink = prefix + WorkflowTaskDataComparator.ACCEPT_DATE + headerSuffix;
            helperText = bundle.getString("helper_text_task_available");
            helpFile = bundle.getString("help_available_activities");
            subTitle = bundle.getString("lb_available");
            tdSizeInProgress = "";
            break;
        case stateInProgress:
            statusSelected = 1;
            labelABorDBorCODate = labelDueDate;
            urlABorDBorCOColLink = prefix + WorkflowTaskDataComparator.DUE_DATE + headerSuffix;
            helperText = bundle.getString("helper_text_task_inprogress");
            helpFile = bundle.getString("help_inprogress_activities");
            subTitle = bundle.getString("lb_inprogress");
            sessionMgr.setAttribute(JobManagementHandler.EXPORT_INIT_PARAM,"tasks");
            tdSizeInProgress = "width:280px";
            break;
        case WorkflowConstants.TASK_GSEDITION_IN_PROGESS:
            statusSelected = 2;
            labelABorDBorCODate = labelDueDate;
            urlABorDBorCOColLink = prefix + WorkflowTaskDataComparator.DUE_DATE + headerSuffix;
            helperText = bundle.getString("lb_improgress_gs_helptext");
            helpFile = bundle.getString("help_inprogress_activities");
            subTitle = bundle.getString("lb_inprogress") + "(" + bundle.getString("lb_gsedition") + ")";
            sessionMgr.setAttribute(JobManagementHandler.EXPORT_INIT_PARAM,"tasks");
            tdSizeInProgress = "";
            break;
        
        case stateFinished:
            statusSelected = 3;
            dateColumn = 8;
            labelABorDBorCODate = labelCompletedOn;
            urlABorDBorCOColLink = prefix + WorkflowTaskDataComparator.COMPLETED_DATE + headerSuffix;
            helperText = bundle.getString("helper_text_task_finished");
            helpFile = bundle.getString("help_finished_activities");
            subTitle = bundle.getString("lb_finished");
            tdSizeInProgress = "";
            break;
        case stateRejected:
            statusSelected = 4;
            labelABorDBorCODate = labelDueDate;
            urlABorDBorCOColLink = prefix + WorkflowTaskDataComparator.DUE_DATE + headerSuffix;
            helperText = bundle.getString("helper_text_task_rejected");
            helpFile = bundle.getString("help_rejected_activities");
            subTitle = bundle.getString("lb_rejected");
            tdSizeInProgress = "";
            break;
        case Task.STATE_ALL:
            statusSelected = 5;
            subTitle = bundle.getString("lb_all_status");
            dateColumn = 7;
            labelABorDBorCODate = labelAcceptBy;
            urlABorDBorCOColLink = prefix + WorkflowTaskDataComparator.ACCEPT_DATE + headerSuffix;
            tdSizeInProgress = "";
            break;
        default:
            statusSelected = 0;
            dateColumn = 7;
            labelABorDBorCODate = labelAcceptBy;
            urlABorDBorCOColLink = prefix + WorkflowTaskDataComparator.ACCEPT_DATE + headerSuffix;
            tdSizeInProgress = "";
            break;
    }             
    
    // Get tasksPerPage from the SystemParameter table
    int tasksPerPage = 20;
    SystemConfiguration sc = SystemConfiguration.getInstance();
    tasksPerPage = sc.getIntParameter(SystemConfigParamNames.RECORDS_PER_PAGE_TASKS);  
    boolean searchEnabled = false;
    try
    {
        searchEnabled =
               sc.getBooleanParameter(SystemConfigParamNames.JOB_SEARCH_REPLACE_ALLOWED);
    }
    catch (Exception ge)
    {
        // assumes disabled.
    }

    // Convert taskListStartStr into an int
    String taskListStartStr = String.valueOf(request.getAttribute("taskListStart"));
    if (taskListStartStr == null) 
    {
        String taskId = request.getParameter("taskId");
        if (taskId != null)
        {
            long id = Long.parseLong(taskId);
            boolean isFind = false;
            
            for (int i = 0; i < tasks.size(); i++)
            {
                Task task = (Task)tasks.get(i);
                if (id == task.getId()) 
                {
                    isFind = true;
                    int start = i/tasksPerPage;
                    start *=tasksPerPage;
                    taskListStartStr = String.valueOf(start);
                    break;
                }
            }
            
            if (!isFind)
            {
                taskListStartStr = "0";
            }
        }
        else
        {
            taskListStartStr = "0";
        }
    }
    
    wordCountListUrl = wordCountListUrl + "&" + taskListStartParam + "=" + taskListStartStr;
    
    // Note that these to integers are the *indexes* of the list
    // so for display we'll add 1 so it will look good for the user. 
    // This is also we I subtract 1 from taskListEnd below, so that
    // taskListEnd will be an index value
    int taskListStart = Integer.parseInt(taskListStartStr);
    int taskListEnd = (taskListStart + tasksPerPage) > tasks.size() ?
        tasks.size() : (taskListStart + tasksPerPage);
    taskListEnd = taskListEnd - 1;

    String pagingUrl = selfUrl;

       
    // Build sort column directives
    // NOTE: the link for urlABorDBorCOColLink is built based on the state, 
    //       see the "state"-switch statement above
    String jobPriorityColLink = prefix + WorkflowTaskDataComparator.PRIORITY + headerSuffix;
    String jobIdColLink = prefix + WorkflowTaskDataComparator.JOB_ID + headerSuffix;
    String activityColLink = prefix + WorkflowTaskDataComparator.JOB_NAME + headerSuffix;
    String companyColLink = prefix + WorkflowTaskDataComparator.COMPANY_NAME + headerSuffix;
    String totalWordCountColLink = prefix + WorkflowTaskDataComparator.TOTAL_WRDCNT + headerSuffix;
    String priorityColLink = prefix + WorkflowTaskDataComparator.PRIORITY + headerSuffix;
    String estCompDateColLink = prefix + WorkflowTaskDataComparator.EST_COMP_DATE + headerSuffix;

    // Build the reverse sort directive
    // get sort arrow image (up -or- down)
    String imgSortArrow = sortAsc ? bundle.getString("img_arrowUp") : bundle.getString("img_arrowDown");
    StringBuffer tmp = new StringBuffer();
    tmp.append("<IMG SRC=\"");
    tmp.append(imgSortArrow);
    tmp.append("\" WIDTH=7 HEIGHT=4 HSPACE=1 BORDER=0>");
    imgSortArrow = tmp.toString();

    // set sort arrow image and the reverse sort directive
    // (doing it this way keeps the HTML cleaner below)
    String jobPrioritySortArrow, jobIdSortArrow, jobSortArrow, companySortArrow, exactSortArrow, fuzzySortArrow, noMatchSortArrow, repetitionSortArrow, totalWrdCntSortArrow, ABorDBorCODateSortArrow, estCompDateSortArrow;
    jobPrioritySortArrow = jobIdSortArrow = jobSortArrow = companySortArrow =  exactSortArrow = fuzzySortArrow = noMatchSortArrow = repetitionSortArrow = totalWrdCntSortArrow = ABorDBorCODateSortArrow = estCompDateSortArrow = "";
    switch(Integer.parseInt(selectedColumn))
    {
        case WorkflowTaskDataComparator.PRIORITY:
            jobPriorityColLink = jobPriorityColLink + reverseSortParam;
            jobPrioritySortArrow = imgSortArrow;
            break;
        case WorkflowTaskDataComparator.JOB_ID:
            jobIdColLink = jobIdColLink + reverseSortParam;
            jobIdSortArrow = imgSortArrow;
            break;
        case WorkflowTaskDataComparator.JOB_NAME:
            activityColLink = activityColLink + reverseSortParam;
            jobSortArrow = imgSortArrow;
        case WorkflowTaskDataComparator.COMPANY_NAME:
            companyColLink = companyColLink + reverseSortParam;
            companySortArrow = imgSortArrow;
        break;
        case WorkflowTaskDataComparator.TOTAL_WRDCNT:
            totalWordCountColLink = totalWordCountColLink + reverseSortParam; 
            totalWrdCntSortArrow = imgSortArrow;
            break;
        case WorkflowTaskDataComparator.ACCEPT_DATE:
            if(state == stateAvailable || state == stateAll)
            {
                urlABorDBorCOColLink = urlABorDBorCOColLink + reverseSortParam;
                ABorDBorCODateSortArrow = imgSortArrow;
            }
            break;
        case WorkflowTaskDataComparator.DUE_DATE:
            if( state == stateInProgress || state == stateRejected )
            {
                urlABorDBorCOColLink = urlABorDBorCOColLink + reverseSortParam;
                ABorDBorCODateSortArrow = imgSortArrow;
            }
            break;
        case WorkflowTaskDataComparator.COMPLETED_DATE:
            if(state == stateFinished)
            {
                urlABorDBorCOColLink = urlABorDBorCOColLink + reverseSortParam;
                ABorDBorCODateSortArrow = imgSortArrow;
            }
            break;
        case WorkflowTaskDataComparator.EST_COMP_DATE:
            estCompDateColLink = estCompDateColLink + reverseSortParam;
            estCompDateSortArrow = imgSortArrow;
            break;
        default:
            break;
    }
    
    Locale uiLocale = (Locale)session.getAttribute(WebAppConstants.UILOCALE);
    TimeZone timeZone = (TimeZone)session.getAttribute(WebAppConstants.USER_TIME_ZONE);
    
    boolean isShowComments = perms.getPermissionFor(Permission.ACTIVITIES_JOB_COMMENTS_VIEW) || perms.getPermissionFor(Permission.ACTIVITIES_COMMENTS_VIEW);
%>
<HTML>
<!-- This JSP is: envoy/tasks/taskList.jsp -->
<HEAD>
<META HTTP-EQUIV="content-type" CONTENT="text/html;charset=UTF-8">
<TITLE><%= title %></TITLE>
<link rel="STYLESHEET" type="text/css" href="/globalsight/includes/ContextMenu.css">
<SCRIPT SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>
<%@ include file="/envoy/common/constants.jspIncl" %>
<SCRIPT SRC="/globalsight/includes/cookieUtil.js"></SCRIPT>
<SCRIPT SRC="/globalsight/includes/radioButtons.js"></SCRIPT>
<script src="/globalsight/includes/ContextMenu.js"></script>
<script src="/globalsight/includes/ieemu.js"></script>
<script type="text/javascript" src="/globalsight/jquery/jquery-1.6.4.min.js"></script>
<SCRIPT>
	function contextForTab(secondaryTargetFilesUrl, workofflineUrl, taskState, taskId, e)
	{
	    if(e instanceof Object)
	    {
		    e.preventDefault();
		    e.stopPropagation();
	    }
	
	    var popupoptions;

	    if(workofflineUrl != '')
		{
			if(secondaryTargetFilesUrl != '')
			{
	        	popupoptions = [
	   	      	      new ContextItem("<%=bundle.getString("lb_TargetFiles") %>", function(){ location.href="/globalsight/ControlServlet?linkName=detail&pageName=TK1&taskAction=getTask&taskType=TRANSZ&state=" + taskState + "&taskId=" + taskId;}) 
	   	      	 	  ,new ContextItem("<%=bundle.getString("lb_secondary_target_files") %>", function(){ location.href=secondaryTargetFilesUrl;}) 
					  <% if(isShowComments){%>
	   	      	      ,new ContextItem("<%=bundle.getString("lb_comments") %>", function(){ location.href="/globalsight/ControlServlet?linkName=comment&pageName=TK2&taskAction=getTask&state=" + taskState + "&taskId=" + taskId;})
	   	      	      <%} %>
	   	      	      ,new ContextItem("<%=bundle.getString("lb_work_offline") %>", function(){ location.href=workofflineUrl;}) 
	   	      	    ];
			}
			else
			{
				popupoptions = [
	   	      	      new ContextItem("<%=bundle.getString("lb_TargetFiles") %>", function(){ location.href="/globalsight/ControlServlet?linkName=detail&pageName=TK1&taskAction=getTask&taskType=TRANSZ&state=" + taskState + "&taskId=" + taskId;}) 
					  <% if(isShowComments){%>
	   	      	      ,new ContextItem("<%=bundle.getString("lb_comments") %>", function(){ location.href="/globalsight/ControlServlet?linkName=comment&pageName=TK2&taskAction=getTask&state=" + taskState + "&taskId=" + taskId;})
	   	      	      <%} %>
	   	      	      ,new ContextItem("<%=bundle.getString("lb_work_offline") %>", function(){ location.href=workofflineUrl;}) 
	   	      	    ];
			}
		}
		else
		{
			if(secondaryTargetFilesUrl != '')
			{
				popupoptions = [
		      	      new ContextItem("<%=bundle.getString("lb_TargetFiles") %>", function(){ location.href="/globalsight/ControlServlet?linkName=detail&pageName=TK1&taskAction=getTask&taskType=TRANSZ&state=" + taskState + "&taskId=" + taskId;}) 
		      	      ,new ContextItem("<%=bundle.getString("lb_secondary_target_files") %>", function(){ location.href=secondaryTargetFilesUrl;})
		      	      <% if(isShowComments){%>
		      	      ,new ContextItem("<%=bundle.getString("lb_comments") %>", function(){ location.href="/globalsight/ControlServlet?linkName=comment&pageName=TK2&taskAction=getTask&state=" + taskState + "&taskId=" + taskId;})
		      	      <%} %>
			      	];
			}
			else
			{
				popupoptions = [
		      	      new ContextItem("<%=bundle.getString("lb_TargetFiles") %>", function(){ location.href="/globalsight/ControlServlet?linkName=detail&pageName=TK1&taskAction=getTask&taskType=TRANSZ&state=" + taskState + "&taskId=" + taskId;}) 
		      	      <% if(isShowComments){%>
		      	      ,new ContextItem("<%=bundle.getString("lb_comments") %>", function(){ location.href="/globalsight/ControlServlet?linkName=comment&pageName=TK2&taskAction=getTask&state=" + taskState + "&taskId=" + taskId;})
		      	      <%} %>
		      	];
			}
		}
	
	    ContextMenu.display(popupoptions, e);
	}
	
    var needWarning = false;
    var objectName = "";
    guideNode = "myActivities";
    var dwnldOpt = null;
    var dwnldOptValid = false;

    var isAssigneeList = new Array();
    var acceptAndDownloadUrls = new Array();
    var wfIds = new Array();
    var jobIds = new Array();
    var taskIds = new Array();
    var sourceLocales = new Array();
    var targetLocales = new Array();
    var l10nProfileIds = new Array();
    var companyNames = new Array();
    var helpFile = "<%=helpFile%>";

    // Constructor
    // Creates a default download options object by reading the client coookies.
    // If cookie does not exist, its value is set to false.
    function ClientDownloadOptions()
    {
        this.fileFormat = "<%= downloadFileFormat %>";
        this.editor = "<%= downloadNameEditor %>";
        this.encoding = "<%= downloadNameEncoding %>";
        this.ptagFormat = "<%= downloadNamePtagFormat %>";
        this.resInsMode = "<%= downloadNameResInsMode %>";
        this.tmEditType = "<%= downloadNameTMEditType %>";
    }

    function verifyClientDownloadOptions()
    {
        // retrieve default options from client-side cookies
        dwnldOpt = new ClientDownloadOptions();
        // validate
        // note: editExact may or may not be present, so we handle it in the page handler
        if( !dwnldOpt.fileFormat || !dwnldOpt.editor || !dwnldOpt.encoding || !dwnldOpt.ptagFormat || !dwnldOpt.resInsMode)
        {
           alert("<%= msgAcceptDownloadDisabled %>");
           return false;
        }
        else
        {
           return true;
        }
    }

    function checkForDownloadRequest()
    {
        if(<%= acceptDownloadRequested %>)
        {
            if ("<%=taskParam%>".length>0)
            {
            	var actionValue = "downloadALLOfflineFiles";
            	if (<%= isDownloadCombined %>)
            	{
            		actionValue = "downloadALLOfflineFilesCombined";
            	}
            	
                var action = "<%=selfUrl + "&" + WebAppConstants.TASK_ACTION + "="%>" + actionValue;
                action += "&taskParam=" + "<%=taskParam%>";
                ActivityForm.action = action;
                showProgressDiv();
                ActivityForm.submit();
            }
            else
            {
                dwnldOpt = new ClientDownloadOptions();
    
                // Send the download request to the sendDownloadFileHandler.
                // If the server sends a zip, the window will switch to the
                // open/save as dialog.
    
                document.location.replace("<%= sendDownloadFileUrl %>"
                    + "&" + "<%= requestParamAcceptDownloadRequest %>" + "=" + <%= acceptDownloadRequested %>
                    + "&" + "<%= requestParamDownloadAction %>" + "=" + "<%= startDownload %>"
                    + "&" + "<%= requestParamFileFormat %>" + "=" + dwnldOpt.fileFormat
                    + "&" + "<%= requestParamEditor %>" + "=" + dwnldOpt.editor
                    + "&" + "<%= requestParamEncoding %>" + "=" + dwnldOpt.encoding
                    + "&" + "<%= requestParamPtagFormat %>" + "=" + dwnldOpt.ptagFormat
                    + "&" + "<%= requestParamResInsMode %>" + "=" + dwnldOpt.resInsMode
                    + "&" + "<%= WebAppConstants.DOWNLOAD_ACTION %>" + "=" + "<%= WebAppConstants.DOWNLOAD_ACTION_START_DOWNLOAD %>"
                    + "&" + "<%= requestParamTMEditType %>" + "=" + dwnldOpt.tmEditType
                    );
            }        
        }
         
        // Only show the download button if something is available to download
        if (ActivityForm.SelectedActivity) 
        {
            document.all.DownloadButtonLayer.style.visibility = "visible";
        }
        
        // Load the guides
        loadGuides();

        ContextMenu.intializeContextMenu();
        
        updateButtonState();
        
        <% if (errorMsg != null && errorMsg.length() > 0) {%>
        showProgressDivError();
        // alert("<%= errorMsg %>");
        <% } %>      
    }
    var selectedTasks = "";
    function submitForm(selectedButton)
    {
    	// Ensuer at least one task is selected
        if (!isRadioChecked(ActivityForm.SelectedActivity))
        {
            return false;
        }

        selectedTasks = "";
       
       <% if (state == stateAvailable || state == stateInProgress) { %>
            var valuesArray;
            var indexes = selectedIndex();
            if (indexes.length == 0)
            {
                alert ("<%= bundle.getString("jsmsg_please_select_a_row") %>");
                return false;
            }

            if (indexes.length > 0)
            {
                if (ActivityForm.SelectedActivity.length)
                {
                    for (var i = 0; i < ActivityForm.SelectedActivity.length; i++)
                    {
                        if (ActivityForm.SelectedActivity[i].checked == true)
                        {
                            if( selectedTasks != "" )
                            {
                                selectedTasks += " "; // must add a [white space] delimiter
                            }
                            selectedTasks += taskIds[i];
                        }
                    }
                }
                // If only one radio button is displayed, there is no radio button array, so
                // just check if the single radio button is checked
                else
                {
                    if (ActivityForm.SelectedActivity.checked == true)
                    {
                        selectedTasks += taskIds[0];
                    }
                }
           }
     
        <% } %>

       //Accept all tasks button
       if (selectedButton == "AcceptAll")
        {
            var action = "<%=selfUrl + "&" + WebAppConstants.TASK_ACTION + "=acceptAllTasks"%>";
            action += "&taskParam=" + selectedTasks;
            ActivityForm.action = action;
            ActivityForm.submit();

            ActivityForm.AcceptAllButton.disabled = true;
            return;
        }
		//click "TranslatedText"  open new window
      // if( selectedButton == "TranslatedText"){
    	 // url += "&taskParam=" + selectedTasks + "&jobId=" + jobId + "&jobName=" + jobName;
    	 // window.open(url, "TranslateText", "resizable=no,scrollbars=yes,width=1000,height=500,hotkeys=yes");
		 //return;
      // }
       //click "TranslatedText" add new column
       if(selectedButton == "TranslatedText"){
    	   var urlJSON = "<%=selfUrl + "&" + WebAppConstants.TASK_ACTION + "=retrieveTranslatedText"%>";
    	   urlJSON += "&taskParam=" + selectedTasks;
    	   $.getJSON(urlJSON,function(data){
    		   if(data.progress){
    			   var data = data.progress;
        		   var taskPrens = data.substring(0, data.length - 1);
        		   var taskPrenAllArr = taskPrens.split(",");
        		   for(var i = 0;i < taskPrenAllArr.length; i++){
        		   		var taskPren = taskPrenAllArr[i];
        		   		var taskPrenArr = taskPren.split("_");
        		   		var objName = "translated"+taskPrenArr[0];
        		   		var obj = document.getElementById(objName);
        		   		if(taskPrenArr[1] < 100){
        		   			obj.style.color = "red";
        		   			obj.innerHTML = "(" + taskPrenArr[1] + "%)";
        		   		}else{
        		   			obj.style.color = "black";
        		   			obj.innerHTML = "(" + taskPrenArr[1] + "%)";
        		   		}
        		   		
        		   }
    		   }
    	   });
    	   return;
       }
       // Batch complete activity button
       if (selectedButton == "CompleteActivity")
       {
			    //for GBS-1939
				var urlJSON = "<%=selfUrl + "&" + WebAppConstants.TASK_ACTION + "=selectedTasksStatus"%>";
				urlJSON += "&taskParam=" + selectedTasks + "&fresh=" + Math.random();
				$.getJSON(urlJSON, function(data) {
					if (data.isUploadingJobName)
					{
						alert("<%=bundle.getString("jsmsg_my_activities_multi_cannotcomplete_uploading")%>" +"\n" + data.isUploadingJobName);
					}
					if (data.isFinishedTaskId)
					{
						var confirmInfo="";
						var action = "<%=selfUrl + "&" + WebAppConstants.TASK_ACTION + "=completeActivity"%>";
						action += "&taskParam=" + data.isFinishedTaskId;
						ActivityForm.action = action;
						if(selectedTasks!=data.isFinishedTaskId)
						{
							confirmInfo ="Tasks that are not 100% translated can't be completed, system will ignore them and complete the rest. Are you sure to continue?";
						}
						else
						{
							confirmInfo='<%=bundle.getString("jsmsg_batch_complete_activity")%>';
						}
						if(confirm(confirmInfo))
						{
							ActivityForm.submit();
						}
					}
					else
					{
						 alert ("The selected activities are not 100% translated, can not be completed.");
					}
				});
           return;
       }
       
       // Batch complete workflow button
       if (selectedButton == "CompleteWorkflow")
       {
           var urlJSON = "<%=selfUrl + "&" + WebAppConstants.TASK_ACTION + "=selectedTasksStatus"%>";
           urlJSON += "&taskParam=" + selectedTasks + "&fresh=" + Math.random();
           $.getJSON(urlJSON, function(data) {
               if (data.isUploadingJobName)
               {
                   alert("<%=bundle.getString("jsmsg_my_activities_multi_cannotcomplete_workflow_uploading")%>" +"\n" + data.isUploadingJobName);
               }
                 
               if (data.isFinishedTaskId)
               {
                   var action = "<%=selfUrl + "&" + WebAppConstants.TASK_ACTION + "=completeWorkflow"%>";
                   action += "&taskParam=" + data.isFinishedTaskId;
                   ActivityForm.action = action;

                   if(selectedTasks!=data.isFinishedTaskId)
                   {
                       confirmInfo ="Tasks that are not 100% translated can't be completed, system will ignore them and complete the rest. Are you sure to continue?";
                   }
                   else
                   {
                       confirmInfo='<%=bundle.getString("jsmsg_batch_complete_workflow")%>';
                   }
                   if(confirm(confirmInfo)) {
                      ActivityForm.submit();
				   }
                }
                else
                {
                    alert ("The selected activities are not 100% translated, can not be completed.");
                }
            });
           return;
       }

       // Offline download should ignore zero word-count tasks.
       if (selectedButton == "DownloadAllOfflineFiles" 
           || selectedButton == "DownloadAllOfflineFilesCombined" 
           || selectedButton == "DownloadCombined" 
           || selectedButton == "Download" )
       {
           var urlCheckZeroWordCountTask = "<%=selfUrl + "&" + WebAppConstants.TASK_ACTION + "=filterZeroWCTasksForOfflineDownload" %>";
           urlCheckZeroWordCountTask += "&taskParam=" + selectedTasks;
           $.getJSON(urlCheckZeroWordCountTask, function(data)
           {
               selectedTasks = data.selectedTaskIds;
               if (selectedTasks == "0")
               {
                   alert("Zero wordcount task will not be offline downloaded.");
               }
               else if(selectedButton == "DownloadAllOfflineFiles")
               {
                   var action = "<%=selfUrl + "&" + WebAppConstants.TASK_ACTION +"=downloadALLOfflineFiles"%>";
                   action += "&taskParam=" + selectedTasks;
                   ActivityForm.action = action;
                   showProgressDiv();
                   ActivityForm.submit();
               }
               else if(selectedButton == "DownloadAllOfflineFilesCombined")
               {
                   var action = "<%=selfUrl + "&" + WebAppConstants.TASK_ACTION +"=downloadALLOfflineFilesCombined"%>";
                   action += "&taskParam=" + selectedTasks;
                   ActivityForm.action = action;
                   showProgressDiv();
                   ActivityForm.submit();
               }
               else if(selectedButton == "DownloadCombined")
               {
                   var action = "<%=selfUrl + "&" + WebAppConstants.TASK_ACTION +"=DownloadCombined"%>";
                   action += "&taskParam=" + selectedTasks;
                   ActivityForm.action = action;
                   ActivityForm.submit();
               }
               else if (selectedButton == "Download")
               {
                   // Check to see if their Download options are set
                   if (verifyClientDownloadOptions() == false)
                   {
                       return false;
                   }
                   var indexes = findSelectedButtons();
                   var index = indexes[0];
                   var action = acceptAndDownloadUrls[index];
                   action += "&taskParam=" + selectedTasks;
                   ActivityForm.action = action;
                   ActivityForm.submit();
               }
           });
           return;
       }
       
       if (selectedButton == "WordCount")
       {
           var action = "<%=wordCountListUrl%>" + "&action=wclist";
           var indexes = findSelectedButtons();
           if (indexes != "")
           {
               // Get word count details for all activities
               action += "&<%=JobManagementHandler.WF_ID%>="
               for (var j=0;j<indexes.length;j++) {
                  var idx = indexes[j];
                  action += wfIds[idx] + ',';
               }
           }
           ActivityForm.action = action;
           ActivityForm.submit();
           return;
       }

       var indexes = findSelectedButtons();
       if (selectedButton=='Search')
       {
           addHiddenSelected(indexes);
           action = "<%=searchUrl%>" + "&search=";
           for (var j=0;j<indexes.length;j++) {
               var idx = indexes[j];
               action += jobIds[idx] + ' ';
           }
           ActivityForm.action = action;
        }
        else
        {
            addHiddenSelected(indexes);
           <%
            StringBuffer exportLink2 = new StringBuffer(export.getPageURL());
            exportLink2.append("&");
            exportLink2.append(JobManagementHandler.EXPORT_SELECTED_WORKFLOWS_ONLY_PARAM);
            exportLink2.append("=true&");
            StringBuffer workflowIdParam= new StringBuffer(JobManagementHandler.WF_ID);
            workflowIdParam.append("=");
            %>
            var workflowIdParam = '<%=workflowIdParam.toString()%>';
            var action = '<%=exportLink2.toString()%>';
            for (var j=0;j<indexes.length;j++) {
                var idx = indexes[j];
                action += workflowIdParam + wfIds[idx] + '&';
            }
            ActivityForm.action = action;
        }

        ActivityForm.submit();
    }

    function showProgressDiv()
    {
        idMessagesDownload.innerHTML = "";
        document.getElementById("idProgressDownload").innerHTML = "0%"
        document.getElementById("idProgressBarDownload").style.width = 0;
        document.getElementById("idProgressDivDownload").style.display = "";
        o_intervalRefresh = window.setInterval("doProgressRefresh()", 300);
    }
    
    function showProgressDivError()
    {
        document.getElementById("idProgressDivDownload").style.display = "";
        doProgressRefresh();
        
        if (o_intervalRefresh)
            window.clearInterval(o_intervalRefresh);
    }
    
    function enableDownloadIfAssignee()
    {
    <% if (state == stateAvailable) {%>
      var indexes = findSelectedButtons();
      var index = indexes[0];
      if (isAssigneeList[index] == 'true')
      {
            if (ActivityForm.DownloadButton)
                ActivityForm.DownloadButton.disabled = false;
            
            if (ActivityForm.CombinedButton)
                ActivityForm.CombinedButton.disabled = false;
      }
      else
      {
            if (ActivityForm.DownloadButton)
                ActivityForm.DownloadButton.disabled = true;
            
            if (ActivityForm.CombinedButton)
                ActivityForm.CombinedButton.disabled = true;
      }
      <% } %>      
    }

    function findSelectedButtons()
    {
       // If more than one radio button is displayed, the length attribute of the 
       // radio button array will be non-zero, so find which 
        // one is checked
       var j=0;
       var indexes = new Array();

       if (ActivityForm.SelectedActivity.length)
        {
           for (i = 0; i < ActivityForm.SelectedActivity.length; i++)
           {
            if (ActivityForm.SelectedActivity[i].checked == true) 
               {
                   indexes[j] = ActivityForm.SelectedActivity[i].value;
                   j++;
               }
           }
        }
        else {
               // If only one is displayed, there is no radio button array, so
               // just check if the single radio button is checked
                if (ActivityForm.SelectedActivity.checked == true)
                {
                    indexes[0] = ActivityForm.SelectedActivity.value;
                }
            }
            
      return indexes;
    }    

    function addHiddenSelected(indexes)
    {
        if (!ActivityForm.SelectedActivityHidden) return;
        var j=indexes.length;
        if (ActivityForm.SelectedActivityHidden.length)
        {
           for (i = 0; i < ActivityForm.SelectedActivityHidden.length; i++) 
           {
            if (ActivityForm.SelectedActivityHidden[i].checked == true)
               {
                   indexes[j] = ActivityForm.SelectedActivityHidden[i].value;
                   j++;
               }
           }
        }
        else
        {
            // one or none
            if (ActivityForm.SelectedActivityHidden.checked == true)
            {
                indexes[j] = ActivityForm.SelectedActivityHidden.value;
            }
        }
    }

    function setButtonState()
    {
       if (!ActivityForm.SelectedActivityHidden) 
          return;
       j = 0;
       if (ActivityForm.SelectedActivityHidden.length  != 0)
       {
           document.ActivityForm.ExportButton.disabled = true;
       }
       else
       {
           document.ActivityForm.ExportButton.disabled = false;
       }
       
    }
    
    function updateButtonState() {
      <% if (state == stateAvailable) {%>
      if (selectedIndex().length == 1) {
        ActivityForm.WordCountButton.disabled = false;
        if (ActivityForm.ExportButton) {
          ActivityForm.ExportButton.disabled = false;
        }
        if (ActivityForm.CombinedButton) {
            ActivityForm.CombinedButton.disabled = false;
          }
      } else {
        ActivityForm.WordCountButton.disabled = true;
        if (ActivityForm.ExportButton) {
          ActivityForm.ExportButton.disabled = true;
        }
      }
      <% } %>
      
      <% if (state == stateInProgress) {%>
      if (selectedIndex().length == 1) {
        ActivityForm.WordCountButton.disabled = false;
        if (ActivityForm.SearchButton) {
          ActivityForm.SearchButton.disabled = false;
        }
        if (ActivityForm.ExportButton) {
          ActivityForm.ExportButton.disabled = false;
        }
        if (ActivityForm.DownloadCombinedButton) {
          ActivityForm.DownloadCombinedButton.disabled = false;
        }
      } else {
        ActivityForm.WordCountButton.disabled = true;
        if (ActivityForm.SearchButton) {
          ActivityForm.SearchButton.disabled = true;
        }
        if (ActivityForm.ExportButton) {
          ActivityForm.ExportButton.disabled = true;
        }
      }
      <% } %>
      
      <% if (state == stateAvailable || state == stateInProgress) {%>
      <% if (!isCombinedFormat) {%>
      if (ActivityForm.DownloadCombinedButton) {
          ActivityForm.DownloadCombinedButton.disabled = true;
        }
      if (ActivityForm.CombinedButton) {
          ActivityForm.CombinedButton.disabled = true;
        }
      <% } else { %>
      // check if disable download combined 
      // have same source locale and target locale
      if (selectedIndex().length > 1)
      {
      	var allindex = selectedIndex();
      	var tlocale_0 = targetLocales[allindex[0]];
      	var slocale_0 = sourceLocales[allindex[0]];
      	var companyName_0 = companyNames[allindex[0]];
      	for(var i = 1; i < allindex.length; i++)
      	{
      		var cindex = allindex[i];
      		var tlocale = targetLocales[cindex];
      		var slocale = sourceLocales[cindex];
      		var companyName = companyNames[cindex];
          	
          	if (tlocale == tlocale_0 && slocale == slocale_0
          			&& companyName == companyName_0)
          	{
          		if (ActivityForm.DownloadCombinedButton) 
          		{
          			ActivityForm.DownloadCombinedButton.disabled = false;
                }
          		if (ActivityForm.CombinedButton) 
          		{
          			ActivityForm.CombinedButton.disabled = false;
                }
          		continue;
          	}
          	else
          	{
          		if (ActivityForm.DownloadCombinedButton) 
          		{
          			ActivityForm.DownloadCombinedButton.disabled = true;
                }
          		if (ActivityForm.CombinedButton) 
          		{
          			ActivityForm.CombinedButton.disabled = true;
                }
          		break;
          	}
      	}
      }
      <% } %>
      <% } %>
      
      if (selectedIndex().length == 0)
      {
          if (ActivityForm.DownloadCombinedButton) 
          {
              ActivityForm.DownloadCombinedButton.disabled = false;
          }
          if (ActivityForm.CombinedButton) 
          {
              ActivityForm.CombinedButton.disabled = false;
          }
      }
    }
    
    function selectedIndex() 
    {
       var selectedIndex = new Array();
        
       var checkboxes = ActivityForm.SelectedActivity;
       if (checkboxes != null) {
         if (checkboxes.length) {
            for (var i = 0; i < checkboxes.length; i++) {
                var checkbox = checkboxes[i];
                if (checkbox.checked) {
                    selectedIndex.push(i);
                }
            }
         } else {
            if (checkboxes.checked) {
                selectedIndex.push(0);
            }
         }
       }
       return selectedIndex;
    }
    
    function wordcountLink(id)
    {
        var inputElem = document.getElementById(id);
        var id = inputElem.value;
        var action = "<%=wordCountListUrl%>" + "&action=wclist" 
        action += "&<%=JobManagementHandler.WF_ID%>=" + wfIds[id] ;
        ActivityForm.action = action;
        ActivityForm.submit();
    }

    //for GBS-2599
    function handleSelectAll() {
        if (ActivityForm && ActivityForm.selectAll) {
            if (ActivityForm.selectAll.checked) {
                checkAllWithName('ActivityForm', 'SelectedActivity');
                updateButtonState();
            }
            else {
                clearAll('ActivityForm');
                updateButtonState();
            }
        }
    }
</SCRIPT>
<STYLE type="text/css">
.list {
    border: 1px solid <%=skin.getProperty("skin.list.borderColor")%>;
}
.headerCell {
    padding-left: 10px; 
    padding-top: 4px; 
    padding-bottom: 4px;
}
</STYLE>
</HEAD>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/utilityScripts.js"></SCRIPT>
<BODY onload="checkForDownloadRequest()" LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0" 
    MARGINWIDTH="0" MARGINHEIGHT="0">
    <DIV ID="contentLayer" STYLE=" POSITION: ABSOLUTE; Z-INDEX: 9; TOP: 108; LEFT: 20px; RIGHT: 20px;">
    
     <TABLE BORDER=0 WIDTH="100%">
        <TR VALIGN="TOP">
            <TD COLSPAN=2>
                <SPAN CLASS="mainHeading">
                <%=title%>&nbsp;-&nbsp;<%=subTitle%>
                </SPAN>
            </TD>
<% if (b_catalyst) {%>
<TD ALIGN="RIGHT"><IMG SRC="/globalsight/images/logo_alchemy.gif"/></TD>
<% } %>

        </TR>
        <TR VALIGN="TOP" CLASS=standardText>    
            <TD WIDTH=500><%=helperText%><P>
            </TD>
        </TR>
      </TABLE>

      <%@ include file="miniSearch.jspIncl" %>

        <TABLE CELLSPACING="0" CELLPADDING="0" BORDER="0">
            <TR>
                <TD CLASS=standardText align=right>         
        <%
        int numOfPages = tasks.size()/tasksPerPage;
        if (tasks.size()%tasksPerPage != 0) numOfPages++;
        int curPage = taskListStart/tasksPerPage + 1;
        int numOfPagesInGroup = WebAppConstants.NUMBER_OF_PAGES_IN_GROUP;
        int pagesOnLeftOrRight = numOfPagesInGroup/2;
        String display = "";
        // Make the Paging widget
        if (tasks.size() > 0) 
        {
            Object[] args = {new Integer(taskListStart + 1), 
                             new Integer(taskListEnd + 1), 
                             new Integer(tasks.size())};  

            // String built is (for example): "Displaying 1 - 20 of 35"
            out.println(MessageFormat.format(bundle.getString("lb_displaying_records"), args)); 
            out.println("<BR>");
             
            // The "First" link
            if (taskListStart == 0) 
            {
                // Don't hyperlink "First" if it's the first page
            %>
                <SPAN CLASS=standardTextGray><%= bundle.getString("lb_first") %></SPAN> |
            <%}
            else 
            {%>
                <A CLASS=standardHREF HREF="<%= pagingUrl %>&taskListStart=0"><%= bundle.getString("lb_first") %></A> |
            <%}
            // The "Previous" link
            if (taskListStart == 0) 
            {
                // Don't hyperlink "Previous" if it's the first page
            %>
                <SPAN CLASS=standardTextGray><%= bundle.getString("lb_previous") %></SPAN> |
            <%}
            else 
            {%>
                <A CLASS=standardHREF HREF="<%= pagingUrl %>&taskListStart=<%= taskListStart - tasksPerPage %>"><%= bundle.getString("lb_previous") %> </A> |
            <%}

            // Show page numbers 1 2 3 4 5 etc...
            for( int i=1; i<= numOfPages; i++)
            {
                int topTask = (tasksPerPage * i) - tasksPerPage;
                if(
                   ((curPage <= pagesOnLeftOrRight) 
                    && (i <= numOfPagesInGroup)) 
                   ||
                   (((numOfPages - curPage) <= pagesOnLeftOrRight) 
                    && (i > (numOfPages - numOfPagesInGroup)))
                   || 
                   ((i<=(curPage + pagesOnLeftOrRight))
                    && (i>=(curPage - pagesOnLeftOrRight))) 
                   )
                {
                    if(taskListStart == topTask)
                    {
                        // Don't hyperlink this page if it's current
                    %>
                        <SPAN CLASS=standardTextGray><%= i %></SPAN>&nbsp
                    <%}
                    else 
                    {%>
                        <A CLASS=standardHREF HREF="<%= pagingUrl %>&taskListStart=<%= topTask %>"><%= i %></A>&nbsp
                    <%}
                }
            }

            // The "Next" link
            if ((taskListStart + tasksPerPage) >= tasks.size()) 
            {
                // Don't hyperlink "Next" if it's the last page
            %>
               |&nbsp<SPAN CLASS=standardTextGray><%= bundle.getString("lb_next") %></SPAN> |
            <%}
            else 
            {%>
                |&nbsp<A CLASS=standardHREF HREF="<%= pagingUrl %>&taskListStart=<%= taskListStart + tasksPerPage %>"><%= bundle.getString("lb_next") %></A> |
            <%}

            // The "Last" link
            int lastTask = tasks.size() - 1; // Index of last task
            int numTasksOnLastPage = tasks.size() % tasksPerPage == 0 ? tasksPerPage : tasks.size() % tasksPerPage;
            if ((lastTask - taskListStart) < tasksPerPage) 
            {
                // Don't hyperlink "Last" if it's the Last page
            %>
               <SPAN CLASS=standardTextGray><%= bundle.getString("lb_last") %></SPAN>
            <%}
            else 
            {%>
                <A CLASS=standardHREF HREF="<%= pagingUrl %>&taskListStart=<%= lastTask - (numTasksOnLastPage - 1)%>"> <%= bundle.getString("lb_last")%></A>
            <%}
        }
        else
        {
            // Displaying zero records
        %>
            <%= bundle.getString("lb_displaying_zero")%><BR>
        <%}%>
        </SPAN>
        <!-- End Paging Layer -->
        </td>
        </tr>
        <tr>
        <td>
     
                      
<FORM NAME="ActivityForm" METHOD="POST">
<!-- Data Table -->
<TABLE BORDER="0" CELLSPACING="0" CELLPADDING="6" CLASS="list">
    <COL>   <!-- Radio button (page icon) for download -->
    <COL>   <!-- Priority -->
    <COL>   <!-- Overdue (clock icon) -->
    <COL>   <!-- Job ID -->
    <COL width="180px">  <!-- Job Name -->
    <COL>   <!-- Spacer column -->
    <COL>   <!-- Due/Accept By -->
    <%
        if (state == stateAvailable) 
        {%>
            <COL>  <!-- Time to Complete -->
        <%}
    %>
    <COL>  <!-- Locales -->
    <TBODY>
        <TR CLASS="tableHeadingBasic" VALIGN="BOTTOM" STYLE="padding-bottom: 3px;">
            <TD>
            <% if ((state == stateInProgress || state == stateAvailable) && tasks != null && tasks.size() > 0) { %>
                <input type="checkbox" onclick="handleSelectAll()" name="selectAll"/>
            <% } %>         
            </TD>
            <TD STYLE="padding-left: 3px;padding-right: 3px;" ALIGN="CENTER"><A CLASS="sortHREFWhite" HREF="<%=jobPriorityColLink%>"><IMG SRC="/globalsight/images/exclamation_point_white.gif" HEIGHT=12 WIDTH=7 ALT="Priority" BORDER=0></A><%=jobPrioritySortArrow%></TD>
            <TD STYLE="padding-left: 3px;padding-right: 3px;" ALIGN="CENTER"><IMG SRC="/globalsight/images/clock.gif" HEIGHT=12 WIDTH=12 ALT="Overdue"></TD>
            <TD CLASS="headerCell"><A CLASS="sortHREFWhite" HREF="<%=jobIdColLink%>"><%=labelJobId%><%=jobIdSortArrow%></A></TD>
            <TD CLASS="headerCell"><A CLASS="sortHREFWhite" HREF="<%=activityColLink%>"><%=labelJobName%><%=jobSortArrow%></A></TD>

            <TD CLASS="headerCell"><A CLASS="sortHREFWhite" HREF="<%=totalWordCountColLink%>"><%=labelWordCount%><%=totalWrdCntSortArrow%></A></TD>
            <%
            	if(state == stateInProgress || state == stateAvailable){
            %>
            	<TD CLASS="headerCell"><%=bundle.getString("lb_translated_text") %></TD>
            <%
            	}
            %>
            
            <TD CLASS="headerCell" ><A CLASS="sortHREFWhite" HREF="<%=urlABorDBorCOColLink%>"><%=labelABorDBorCODate%><%=ABorDBorCODateSortArrow%></A></TD>
        <%
            if (state == stateAvailable) 
            {
        %>
                <TD CLASS="headerCell"><A CLASS="sortHREFWhite" HREF="<%=estCompDateColLink%>"><%=bundle.getString("lb_estimated_activity_completion_date")%><%=estCompDateSortArrow%></A></TD>
        <%
            }
        %>
            <TD STYLE="padding-right: 10px; width:300px" CLASS="headerCell"><SPAN CLASS="sortHREFWhite"><%=bundle.getString("lb_locales")%></SPAN></TD>
            <TD STYLE="padding-right: 10px; <%=tdSizeInProgress%>" CLASS="headerCell"><SPAN CLASS="sortHREFWhite"><%=bundle.getString("lb_task_type")%></SPAN></TD>
        <%if(state == Task.STATE_ALL || state == WorkflowConstants.TASK_GSEDITION_IN_PROGESS){%>    
            <TD STYLE="padding-right: 10px;" CLASS="headerCell"><SPAN CLASS="sortHREFWhite"><%=bundle.getString("lb_task_status")%></SPAN></TD>
        <%}%>
        <%if(showCompany){%>
            <TD CLASS="headerCell"><A CLASS="sortHREFWhite" HREF="<%=companyColLink%>"><%=labelCompanyName%><%=companySortArrow%></A></TD>
        <%}%>
        </TR>
        
        <%
        int i = 0;
        if (tasks != null)
        {
            int totalWordCount = 0;

            Date dt = new Date();
            Timestamp ts = new Timestamp(timeZone);
            ts.setLocale(uiLocale);
            int javascript_array_index = 0;

            for (i = taskListStart; i <= taskListEnd; i++, javascript_array_index++)
            {
                totalWordCount = 0;

                String color = (i%2 == 0) ? "#FFFFFF" : "#EEEEEE";
                Task tsk = (Task)tasks.get(i);
                // build word counts
                totalWordCount = tsk.getWorkflow().getTotalWordCount();
                String taskType = tsk.getTaskType();
                String thistaskState = tsk.getStateAsString();
                String jobId = Long.toString(tsk.getJobId());
                String job = tsk.getJobName();
                String company = CompanyWrapper.getCompanyNameById(tsk.getCompanyId());
                String activity = tsk.getTaskDisplayName();
                String priority = Integer.toString(tsk.getPriority());
                String sourceLocale = tsk.getSourceLocale().getDisplayName(uiLocale);
                String targetLocale = tsk.getTargetLocale().getDisplayName(uiLocale);
                String valueABorDBorCODate;
                String assignees = canManageProjects ? tsk.getAllAssigneesAsString() : "";
                String pmAssigneeTable = "";
                
                JobHandler jobHandler = ServerProxy.getJobHandler(); 
                String l10nProfileId = jobHandler.getL10nProfileByJobId(tsk.getJobId()) + "";
                
                if (canManageProjects) {
                    StringBuffer pmAssigneeTableBuffer = new StringBuffer();
                    pmAssigneeTableBuffer.append("<TABLE CELLPADDING=0 CELLSPACING=0 BORDER=0 CLASS=smallText>");
                    pmAssigneeTableBuffer.append("<TR VALIGN=TOP>");
                    pmAssigneeTableBuffer.append("<TD NOWRAP>" + labelAssignees + ":&nbsp;</TD>");
                    pmAssigneeTableBuffer.append("<TD NOWRAP>" + assignees + "</TD>");
                    pmAssigneeTableBuffer.append("</TR>");
                    pmAssigneeTableBuffer.append("</TABLE>");
                    pmAssigneeTable = pmAssigneeTableBuffer.toString();
                }
                
                // build the normal task link
                StringBuffer detailBuffer = new StringBuffer();
                
                detailBuffer.append(AND);
                detailBuffer.append(taskIdParam);
                detailBuffer.append(EQUAL);
                detailBuffer.append(tsk.getId());
                detailBuffer.append(AND);
                detailBuffer.append(stateParam);
                detailBuffer.append(EQUAL);
                detailBuffer.append(state);
                detailBuffer.append(AND);
                detailBuffer.append(taskTypeParam);
                detailBuffer.append(EQUAL);
                detailBuffer.append(taskType);
                String detailLink = detailUrl + detailBuffer.toString();
                String acceptAndDownloadLink = acceptAndDownloadUrl + detailBuffer.toString();
                
                StringBuffer exportLink = new StringBuffer(export.getPageURL());
                exportLink.append("&");
                exportLink.append(JobManagementHandler.WF_ID);
                exportLink.append("=");
                exportLink.append(tsk.getWorkflow().getId());
                exportLink.append("&");
                exportLink.append(JobManagementHandler.EXPORT_SELECTED_WORKFLOWS_ONLY_PARAM);
                exportLink.append("=true");
                
                
                //  Based on the state (task tab selected) we either show
                //  "Accept By" or "Due By" or "Completed On" value
                switch (state)
                {
                    case stateAvailable:
                        ts.setDate(tsk.getEstimatedAcceptanceDate());
                        valueABorDBorCODate = ts.toString();
                        break;
                    case stateFinished:
                        ts.setDate(tsk.getCompletedDate());
                        valueABorDBorCODate = ts.toString();
                        break;
                    case stateAll:
                        if (thistaskState.equals(Task.STATE_ACTIVE_STR)) {
                            ts.setDate(tsk.getEstimatedAcceptanceDate());
                        } else {
                            ts.setDate(tsk.getAcceptedDate());
                        }
                        valueABorDBorCODate = ts.toString();
                        break;
                    default:
                        ts.setDate(tsk.getEstimatedCompletionDate());
                        valueABorDBorCODate = ts.toString();
                        break;
                }
				%>
                <TR STYLE="padding-bottom: 5px; padding-top: 5px;" VALIGN=TOP BGCOLOR="<%= color %>">
                <%
                // Create the radio button for 'Instant Download".
                // If PM, make sure he's one of the assignees, otherwise don't show 
                // the radio button.
                Boolean isAssignee = Boolean.TRUE;
                if (canManageProjects) {
                    if (tsk.getAllAssignees() == null) {
                        isAssignee = Boolean.FALSE;
                    } else
                        isAssignee = new Boolean(tsk.getAllAssignees().contains((String)session.getAttribute(WebAppConstants.USER_NAME))); 
                }
				%>
                <TD>
                <%
                if(state != stateRejected)
                {
                    String buttonType = "radio";
                    if (state == stateInProgress || state == stateAvailable)
                        buttonType = "checkbox";
				%>
                    <INPUT id="radio<%= i %>" TYPE="<%= buttonType %>" NAME="SelectedActivity" VALUE="<%= javascript_array_index %>" onClick="javascript:updateButtonState()">
                <%}%>
                </TD>
                
<SCRIPT LANGUAGE="JavaScript1.2">
                isAssigneeList[<%=javascript_array_index%>] = "<%=isAssignee.toString()%>";
                acceptAndDownloadUrls[<%=javascript_array_index%>] = "<%=acceptAndDownloadLink%>";
                wfIds[<%=javascript_array_index%>] = "<%=tsk.getWorkflow().getId()%>";
                jobIds[<%=javascript_array_index%>] = "<%=tsk.getJobId()%>";
                taskIds[<%=javascript_array_index%>] = "<%=tsk.getId()%>";
                targetLocales[<%=javascript_array_index%>] = "<%=targetLocale%>";
                sourceLocales[<%=javascript_array_index%>] = "<%=sourceLocale%>";
                l10nProfileIds[<%=javascript_array_index%>] = "<%=l10nProfileId%>";
                companyNames[<%=javascript_array_index%>] = "<%=company%>";
</script>
                <TD ALIGN=LEFT STYLE="padding-left: 3px; padding-top: 7px;"><SPAN CLASS=standardText><%= priority %></SPAN></TD>
				<%                
                // Overdue column
                if ((thistaskState.equals(Task.STATE_ACTIVE_STR) && tsk.getEstimatedAcceptanceDate() != null && dt.after(tsk.getEstimatedAcceptanceDate())) ||
                    (thistaskState.equals(Task.STATE_ACCEPTED_STR) && tsk.getEstimatedCompletionDate() != null && dt.after(tsk.getEstimatedCompletionDate())))
                {%>
                    <TD ALIGN=CENTER><IMG SRC="/globalsight/images/dot_red.gif" HEIGHT=8 WIDTH=8 ALT="Overdue" VSPACE=6></TD>
                <%}
                else 
                {%>
                    <TD>&nbsp;</TD>
                <%}%>
                	<TD CLASS=standardText ALIGN=RIGHT><%= jobId %></TD>
				<%
                ///fix: pendding on WorkflowServer getTask for state-completed 
                // For "All Status" issue
                if (thistaskState.equals(Task.COMPLETED)  || tsk.getState() == Task.STATE_REJECTED || tsk.getState() == Task.STATE_FINISHING) 
                {%>
                    <TD STYLE="width:210px; padding-left: 10px; word-wrap:break-word; word-break:break-all" CLASS=standardText>
                    <SCRIPT language="javascript">if (navigator.userAgent.indexOf('Firefox') >= 0){document.write("<DIV style='width:200px'>");}</SCRIPT>
                    <B><%= job %></B>
                    <SCRIPT language="javascript">if (navigator.userAgent.indexOf('Firefox') >= 0){document.write("</DIV>")}</SCRIPT>
                    </TD>
                <%} 
                else
                {
                	perms = (PermissionSet) session.getAttribute(WebAppConstants.PERMISSIONS);
                	boolean isPageDetailOne = true;
                	boolean disableButtons = false;
                	boolean workoffline = perms.getPermissionFor(Permission.ACTIVITIES_WORKOFFLINE);
                	boolean review_only = tsk.isType(Task.TYPE_REVIEW);
                	boolean languageSignOff = perms.getPermissionFor(Permission.ACTIVITIES_WORKOFFLINE);
                	switch (tsk.getState())
                    {
                        case Task.STATE_ACCEPTED:
                            isPageDetailOne = false;
                            break;
                        case Task.STATE_DISPATCHED_TO_TRANSLATION:
                            isPageDetailOne = false;
                            disableButtons = true;
                            break;
                        case Task.STATE_IN_TRANSLATION:
                            isPageDetailOne = false;
                            disableButtons = true;
                            break;
                        case Task.STATE_TRANSLATION_COMPLETED:
                            isPageDetailOne = false;
                            break;
                        case Task.STATE_REDEAY_DISPATCH_GSEDTION:
                            isPageDetailOne = false;
                            disableButtons = true;
                            break;
                        default:
                            break;
                    }
                	String workofflineUrl = "";
                	if (!isPageDetailOne)
            		{
            		      if (!disableButtons && workoffline)
            		      {
            		          
            		        if (review_only && languageSignOff)
            		        {
            		        	workofflineUrl = "/globalsight/ControlServlet?linkName=downloadreport&pageName=TK2&state=" + tsk.getState() + "&taskId="+tsk.getId();
            		        }
            		        else
            		        {
            		        	workofflineUrl = "/globalsight/ControlServlet?linkName=download&pageName=TK2&state=" + tsk.getState() + "&taskId="+tsk.getId(); 			 
            		        }
            		    }
            		}
            		String secondaryTargetFilesUrl = "";
            		boolean isShowSecondaryTargetFile = !perms.getPermissionFor(Permission.ACTIVITIES_SECONDARYTARGETFILE);
        			if(isShowSecondaryTargetFile)
        			{
        				Set<SecondaryTargetFile> stfs = 
        					tsk.getWorkflow().getSecondaryTargetFiles();
        			   int size1 = stfs == null ? 0 : stfs.size();
        			   String stfCreationState = tsk.getStfCreationState();
        			   if ((stfCreationState != null && stfCreationState.length() > 0) || size1 > 0)
        			   {
        				   secondaryTargetFilesUrl = "/globalsight/ControlServlet?linkName=taskSecondaryTargetFiles&pageName=TK2&taskAction=getTask&state=" + tsk.getState() + "&taskId="+tsk.getId(); 
        			   }
        			}
            		%>
                	
                    <TD STYLE="padding-left: 10px; word-wrap:break-word; word-break:break-all">
                    <SCRIPT language="javascript">if (navigator.userAgent.indexOf('Firefox') >= 0){document.write("<DIV style='width:200px'>");}</SCRIPT>
                    <B><A CLASS="standardHREF" HREF="<%= detailLink %>" oncontextmenu="contextForTab('<%= secondaryTargetFilesUrl %>','<%= workofflineUrl %>','<%= tsk.getState() %>','<%= tsk.getId() %>',event)"><%= job %></A></B><BR>
                    <SPAN CLASS=smallText><%= labelActivity %>: <%= activity %><BR><%= pmAssigneeTable %></SPAN>
                    <SCRIPT language="javascript">if (navigator.userAgent.indexOf('Firefox') >= 0){document.write("</DIV>")}</SCRIPT>
                    </TD>
                <%}%>

                <TD ALIGN=CENTER><SPAN CLASS=standardText><B>
                <% if (tsk.getState() == Task.STATE_REJECTED) {%>
                    <%= totalWordCount %>
                <% } else {%>
                   <a class=standardHREF href="javascript:wordcountLink('radio<%= i%>');"><%= totalWordCount %></a>
                <%}%>
                </B></SPAN></TD>
                
                <% if(state == stateInProgress || state == stateAvailable){%>
                	<TD STYLE="padding-left: 10px;"><SPAN CLASS=standardText  ID="translated<%= tsk.getId()%>" style ="font-weight:600"></SPAN></TD>
                <%}%>
                	<TD STYLE="padding-left: 10px;"><SPAN CLASS=standardText><%= valueABorDBorCODate %></SPAN></TD>
                <% if (state == stateAvailable) 
                {
                    ts.setDate(tsk.getEstimatedCompletionDate());
                    %>
                    <TD STYLE="padding-left: 10px;"><SPAN CLASS=standardText><%= ts %></SPAN></TD>
                <%}%>
                <TD STYLE="padding-left: 10px;padding-right: 10px;"><SPAN CLASS=smallText><%= lablelSource %> : <%= sourceLocale %> <BR> <%= lablelTarget %> : <%= targetLocale %></SPAN></TD>
                <% if (Task.TYPE_TRANSLATION.equals(taskType))
                {%>
                    <TD STYLE="padding-left: 10px;padding-right: 10px;"><SPAN CLASS=standardText>Translation</SPAN></TD>
                <%} else 
                {%>
                    <TD STYLE="padding-left: 10px;padding-right: 10px;"><SPAN CLASS=standardText><%= taskType %></SPAN></TD>
                <% }
                if(state == Task.STATE_ALL || state == WorkflowConstants.TASK_GSEDITION_IN_PROGESS)
                {
                    String taskStateString = null;
                    
                    // For "All Status" issue
                    int taskStateValue = tsk.getState();
                    if(taskStateValue == Task.STATE_REJECTED)
                    {
                            taskStateString = bundle.getString("lb_rejected");
                    }
                    else if(taskStateValue == Task.STATE_FINISHING)
                    {
                            taskStateString = bundle.getString("lb_finishing");
                    }
                    else if(thistaskState.equals("ACTIVE"))
                    {
                            taskStateString = bundle.getString("lb_available");
                    }
                    else if(thistaskState.equals("ACCEPTED"))
                    {
                            taskStateString = bundle.getString("lb_inprogress");
                    }
                    else if(thistaskState.equals("COMPLETED"))
                    {
                            taskStateString = bundle.getString("lb_finished");
                    }
                    else if(thistaskState.equals(Task.STATE_DISPATCHED_TO_TRANSLATION_STR))
                    {
                            taskStateString = bundle.getString("lb_state_dispatched_to_translator");
                    }
                    else if(thistaskState.equals(Task.STATE_IN_TRANSLATION_STR))
                    {
                            taskStateString = bundle.getString("lb_state_in_translation");
                    }
                    else if(thistaskState.equals(Task.STATE_TRANSLATION_COMPLETED_STR))
                    {
                            taskStateString = bundle.getString("lb_state_completed");
                    }
                    else if(thistaskState.equals(Task.STATE_REDEAY_DISPATCH_GSEDTION_STR))
                    {
                            taskStateString = bundle.getString("lb_state_ready_dispatch");
                    }

                      if(taskStateString != null)
                      {%>
                            <TD STYLE="padding-left: 10px;padding-right: 10px;"><SPAN CLASS=standardText><%= taskStateString %></SPAN></TD>
                      <%}
                }
                if(showCompany){%>
                	<TD STYLE="padding-left: 10px; word-wrap:break-word" CLASS=standardText><B><%= company %></A></TD>
                <% }%>
                </TR>               
            <% }
            // If search enabled, create hidden fields for all other activities
            // not shown on this page (for the check all pages link)
            // disable it
            if (false && searchEnabled)
            {%>
                <div id='restofactivities' style="display:none">
                <% for (i = 0; i < taskListStart; i++, javascript_array_index++)
                {
                    Task tsk = (Task)tasks.get(i);
%>
                    <INPUT TYPE=checkbox NAME="SelectedActivityHidden" style="display:none" VALUE="<%= javascript_array_index%>">
                    <script>
                    jobIds[<%=javascript_array_index%>] = "<%=tsk.getJobId()%>";
                    wfIds[<%=javascript_array_index%>] = "<%=tsk.getWorkflow().getId()%>";
                    taskIds[<%=javascript_array_index%>] = "<%=tsk.getId()%>";
                    </script>
<%
                }
                for (i = taskListEnd+1; i < tasks.size(); i++, javascript_array_index++)
                {
                    Task tsk = (Task)tasks.get(i);
%>
                    <INPUT TYPE=checkbox NAME="SelectedActivityHidden" style="display:none" VALUE="<%= javascript_array_index %>">
                    <script>
                    jobIds[<%=javascript_array_index%>] = "<%=tsk.getJobId()%>";
                    wfIds[<%=javascript_array_index%>] = "<%=tsk.getWorkflow().getId()%>";
                    taskIds[<%=javascript_array_index%>] = "<%=tsk.getId()%>";
                    </script>
<%
                }%>
                </div>
            <%}
        }
        
%>
                    </TBODY>
                </TABLE>
<% if ((state == stateInProgress || state == stateAvailable) && tasks != null && tasks.size() > 0) { %>
<TABLE>
   <TR>
        <TD CLASS="standardText">
            <DIV ID="CheckAllLayer" STYLE="visibility: visible">
                <!--for gbs-2599
                A CLASS="standardHREF"
                   HREF="javascript:checkAllWithName('ActivityForm', 'SelectedActivity'); updateButtonState(); ">
                   <%=bundle.getString("lb_check_all")%></A--> 
<% if (state == stateInProgress && (canManageWorkflows || canManageProjects) && searchEnabled) { %>
                <A CLASS="standardHREF"
                   HREF="javascript:checkAll('ActivityForm'); updateButtonState();"><%=bundle.getString("lb_check_all_pages")%></A> 
<% } %>
                <!--for gbs-2599
                A CLASS="standardHREF"
                   HREF="javascript:clearAll('ActivityForm');updateButtonState();">
                   <%=bundle.getString("lb_clear_all")%></A-->
            </DIV>
         </TD>
     </TR>                
</TABLE>
<INPUT TYPE="HIDDEN" NAME="<%=JobManagementHandler.EXPORT_MULTIPLE_ACTIVITIES_PARAM%>" VALUE="true">
<% } %>
<DIV ID="DownloadButtonLayer" ALIGN="RIGHT" STYLE="visibility: hidden">
<!-- Add Accepting all tasks button  -->
<%    
if (state==stateAvailable) {
%>
    <amb:permission name="<%=Permission.ACTIVITIES_ACCEPT_ALL%>" >
    <INPUT TYPE="BUTTON" NAME="AcceptAllButton" VALUE="<%=bundle.getString("lb_accept")%>" onClick="submitForm('AcceptAll');">
    </amb:permission>
<% } %>

<%
if (state==stateAvailable || state==stateInProgress) {
%>
    <INPUT TYPE="BUTTON" NAME="TranslatedTextButton" VALUE="<%=bundle.getString("lb_translated_text") %>"  onClick = "submitForm('TranslatedText');">
    <amb:permission name="<%=Permission.ACTIVITIES_BATCH_COMPLETE_ACTIVITY%>" >
    <INPUT TYPE="BUTTON" NAME="CompleteActivityButton" VALUE="<%=bundle.getString("lb_complete_activity")%>" onClick="submitForm('CompleteActivity');">
    </amb:permission>
<% } %>
<%
if (state==stateAvailable || state==stateInProgress) {
%>
    <amb:permission name="<%=Permission.ACTIVITIES_BATCH_COMPLETE_WORKFLOW%>" >
    <INPUT TYPE="BUTTON" NAME="CompleteWorkflowButton" VALUE="<%=bundle.getString("lb_complete_workflow")%>" onClick="submitForm('CompleteWorkflow');">
    </amb:permission>
<% } %>
<!--  End of -->
    <INPUT TYPE="BUTTON" NAME="WordCountButton" VALUE="<%=bundle.getString("lb_detailed_word_counts")%>..." onClick="submitForm('WordCount');"
    <% if (state == stateAvailable || state == stateInProgress) { out.println(" DISABLED"); } %>
    >
<% if (searchEnabled && state==stateInProgress) { %>
    <amb:permission name="<%=Permission.ACTIVITIES_SEARCHREPLACE%>" >
    <INPUT TYPE="BUTTON" NAME="SearchButton" VALUE="<%=bundle.getString("lb_search_replace")%>..." onClick="submitForm('Search');"
    <% if (state == stateInProgress) { out.println(" DISABLED"); } %>
    >        
    </amb:permission>
<% } %>    
<% if (userMayExportAll || (state == stateInProgress && userMayExportInProgress)) { %>
    <amb:permission name="<%=Permission.ACTIVITIES_EXPORT%>" >
    <INPUT TYPE="BUTTON" NAME="ExportButton" VALUE="<%=bundle.getString("lb_export")%>..." onClick="submitForm('Export');" 
    <% if (state == stateAvailable || state == stateInProgress) { out.println(" DISABLED"); } %>
    >
    </amb:permission>
<% } %>
<% if (state == stateAvailable) { %>
    <amb:permission name="<%=Permission.ACTIVITIES_WORKOFFLINE%>" >
    <INPUT TYPE="BUTTON" NAME="DownloadButton" VALUE="<%=bundle.getString("lb_download")%>..." onClick="submitForm('Download');">
    </amb:permission>
<% } %>
<% if (state == stateInProgress) { %>
    <amb:permission name="<%=Permission.ACTIVITIES_DOWNLOAD_ALL%>" >
    <INPUT TYPE="BUTTON" NAME="DownloadAllButton" VALUE="<%=bundle.getString("lb_download")%>..." onClick="submitForm('DownloadAllOfflineFiles');" >
    </amb:permission>
<% } %>
<% if (state == stateAvailable) { %>
    <amb:permission name="<%=Permission.ACTIVITIES_WORKOFFLINE%>" >
    <amb:permission name="<%=Permission.ACTIVITIES_DOWNLOAD_COMBINED%>" >
    <INPUT TYPE="BUTTON" NAME="CombinedButton" VALUE="<%=bundle.getString("lb_download_combined")%>..." onClick="submitForm('DownloadCombined');">
    </amb:permission>
    </amb:permission>
<% } %>
<% if (state == stateInProgress) { %>
    <amb:permission name="<%=Permission.ACTIVITIES_DOWNLOAD_ALL%>" >
    <amb:permission name="<%=Permission.ACTIVITIES_DOWNLOAD_COMBINED%>" >
    <INPUT TYPE="BUTTON" NAME="DownloadCombinedButton" VALUE="<%=bundle.getString("lb_download_combined")%>..." onClick="submitForm('DownloadAllOfflineFilesCombined');" >
    </amb:permission>
    </amb:permission>
<% } %>
    <amb:permission name="<%=Permission.ACTIVITIES_OFFLINEUPLOAD_FROMANYACTIVITY%>" >
    <INPUT TYPE="BUTTON" NAME="offlineUpload" VALUE="<%=bundle.getString("lb_offline_upload")%>" onClick="window.location.href = '<%=url_offlineUploadPage%>'">
    </amb:permission>
</DIV>
<%
Iterator iter = languageSet.iterator();
while (iter.hasNext())
{
    String lang= (String) iter.next();
    %><INPUT TYPE="HIDDEN" NAME="languageSet" VALUE="<%=lang%>"/>
<%
}
%>
</FORM>
</TD>
</TR>
</TABLE>

</DIV>
<br>
    <span id="progress_content">
        <div id='idProgressDivDownload'
            style='border-style: solid; border-width: 1pt; border-color: #0c1476; background-color: white; display:none; left: 300px; height: 370; width: 500px; position: absolute; top: 150px; z-index: 21'>
            <%@ include file="/envoy/edit/offline/download/downloadProgressIncl.jsp" %>
        </div>
    </span>
    <%!
//TODO should pull up to a common util class
private String qualifyActivity(String activity){
  int index = activity.lastIndexOf("_");
  if(index<0) return activity;
  return activity.substring(0,index);
}
%>
</BODY>
</HTML>