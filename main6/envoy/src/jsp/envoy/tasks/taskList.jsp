<%@ page
        contentType="text/html; charset=UTF-8"
        errorPage="/envoy/common/activityError.jsp"
        import="com.globalsight.everest.permission.Permission,
        		com.globalsight.config.UserParamNames,
                com.globalsight.everest.permission.PermissionSet,
                com.globalsight.everest.servlet.util.SessionManager,
                com.globalsight.everest.foundation.SearchCriteriaParameters,
                com.globalsight.everest.webapp.pagehandler.projects.workflows.JobSearchConstants,
                com.globalsight.everest.taskmanager.Task,
                com.globalsight.everest.webapp.WebAppConstants,
                com.globalsight.everest.webapp.pagehandler.offline.OfflineConstants,
                com.globalsight.everest.webapp.pagehandler.PageHandler,
                com.globalsight.everest.webapp.pagehandler.tasks.TaskListConstants,
                com.globalsight.everest.webapp.pagehandler.tasks.TaskListParams,
                com.globalsight.util.StringUtil"
        session="true"
        %>
<%@ page import="java.util.*" %>

<jsp:useBean id="self" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="detail" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="invokedownload" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="previousPage" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="nextPage" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="export" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="search" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="wordcountlist" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>

<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>

<%
    final String AND = "&";
    final String EQUAL = "=";

    ResourceBundle bundle = PageHandler.getBundle(session);
    SessionManager sessionManager = (SessionManager) session.getAttribute(WebAppConstants.SESSION_MANAGER);

    TaskListParams taskListParams = (TaskListParams) sessionManager.getMyactivitiesAttribute(TaskListConstants.TASK_LIST_PARAMS);

    //Labels of the page components
    String title = bundle.getString("lb_my_activities");
    String errorMessage = (String) sessionManager.getAttribute("taskList_errorMsg");
    String acceptanceStart = JobSearchConstants.ACCEPTANCE_START;
    String acceptanceStartOptions = JobSearchConstants.ACCEPTANCE_START_OPTIONS;
    String acceptanceEnd = JobSearchConstants.ACCEPTANCE_END;
    String acceptanceEndOptions = JobSearchConstants.ACCEPTANCE_END_OPTIONS;
    String completionStart = JobSearchConstants.EST_COMPLETION_START;
    String completionStartOptions = JobSearchConstants.EST_COMPLETION_START_OPTIONS;
    String completionEnd = JobSearchConstants.EST_COMPLETION_END;
    String completionEndOptions = JobSearchConstants.EST_COMPLETION_END_OPTIONS;

    String helpText = "";
    String helpFile = "";
    int state = taskListParams.getTaskState();
    String stateString = "";
    String taskDateLabel = bundle.getString("lb_accept_by");
    switch (state) {
        case 3: //Available
            stateString = bundle.getString("lb_available");
            helpText = bundle.getString("helper_text_task_available");
            helpFile = bundle.getString("help_available_activities");
            break;
        case 8: //In Progress
            taskDateLabel = bundle.getString("lb_due_date");
            stateString = bundle.getString("lb_inprogress");
            helpText = bundle.getString("helper_text_task_inprogress");
            helpFile = bundle.getString("help_inprogress_activities");
            break;
        case -1: //Finish
            taskDateLabel = bundle.getString("lb_completed_on");
            stateString = bundle.getString("lb_finished");
            helpText = bundle.getString("helper_text_task_finished");
            helpFile = bundle.getString("help_finished_activities");
            break;
        case 6: //Reject
            taskDateLabel = bundle.getString("lb_due_date");
            stateString = bundle.getString("lb_rejected");
            helpText = bundle.getString("helper_text_task_rejected");
            helpFile = bundle.getString("help_rejected_activities");
            break;
        default: //All
            stateString = bundle.getString("lb_all_status");
            helpText = "";
            helpFile = bundle.getString("help_main");
            break;
    }

    //Urls of the links on this page
    String selfUrl = self.getPageURL();
    String detailUrl = detail.getPageURL() + "&" + WebAppConstants.TASK_ACTION +
            "=" + WebAppConstants.TASK_ACTION_RETRIEVE;
    String searchUrl = search.getPageURL() + "&action=search";
    String wordCountListUrl = wordcountlist.getPageURL();
    String acceptAndDownloadUrl = self.getPageURL() + "&" + WebAppConstants.TASK_ACTION +
            "=" + WebAppConstants.TASK_ACTION_ACCEPT_AND_DOWNLOAD;
    String sendDownloadFileUrl = invokedownload.getPageURL();
    String offlineUploadPageUrl =
            "/globalsight/ControlServlet?activityName=simpleofflineupload";
	String exportUrl = export.getPageURL();

    //define task states
    final int stateAvailable = Task.STATE_ACTIVE;
    final int stateInProgress = Task.STATE_ACCEPTED;
    final int stateFinished = Task.STATE_COMPLETED;
    final int stateRejected = Task.STATE_REJECTED;
    final int stateAll = Task.STATE_ALL;

    int currentTaskState = taskListParams.getTaskState();
    String sortColumn = taskListParams.getSortColumn();
    boolean isDateSort = false;
    if(StringUtil.isEmpty(sortColumn))
    	sortColumn = "";
    if(sortColumn.endsWith("Date"))
    	isDateSort = true;
    boolean isAscSort = taskListParams.isAscSort();
    String sortType = isAscSort ? "asc": "desc";
    HashMap<String,String> filters = taskListParams.getFilters();
    String acceptanceStartFilter = filters.get("acceptanceStartFilter");
    if(StringUtil.isEmpty(acceptanceStartFilter))
    	acceptanceStartFilter = "";
    String acceptanceStartOptionsFilter = filters.get("acceptanceStartOptionsFilter");
    if(StringUtil.isEmpty(acceptanceStartOptionsFilter))
    	acceptanceStartOptionsFilter = "";
    String acceptanceEndFilter = filters.get("acceptanceEndFilter");
    if(StringUtil.isEmpty(acceptanceEndFilter))
    	acceptanceEndFilter = "";
    String acceptanceEndOptionsFilter = filters.get("acceptanceEndOptionsFilter");
    if(StringUtil.isEmpty(acceptanceEndOptionsFilter))
    	acceptanceEndOptionsFilter = "";
    String completionStartFilter = filters.get("completionStartFilter");
    if(StringUtil.isEmpty(completionStartFilter))
    	completionStartFilter = "";
    String completionStartOptionsFilter = filters.get("completionStartOptionsFilter");
    if(StringUtil.isEmpty(completionStartOptionsFilter))
    	completionStartOptionsFilter = "";
    String completionEndFilter = filters.get("completionEndFilter");
    if(StringUtil.isEmpty(completionEndFilter))
    	completionEndFilter = "";
    String completionEndOptionsFilter = filters.get("completionEndOptionsFilter");
    if(StringUtil.isEmpty(completionEndOptionsFilter))
    	completionEndOptionsFilter = "";
    String priorityFilter = filters.get("priorityFilter");
    if(StringUtil.isEmpty(priorityFilter))
    	priorityFilter = "";
    String jobIdFilter = filters.get("jobIdFilter");
    if(StringUtil.isEmpty(jobIdFilter))
    	jobIdFilter = "";
    String jobIdOption = filters.get("jobIdOption");
    if(StringUtil.isEmpty(jobIdOption))
    	jobIdOption = "";
    String jobNameFilter = filters.get("jobNameFilter");
    if(StringUtil.isEmpty(jobNameFilter))
    	jobNameFilter = "";
    String activityNameFilter = filters.get("activityNameFilter");
    if(StringUtil.isEmpty(activityNameFilter))
    	activityNameFilter = "";
    String assigneesNameFilter = filters.get("assigneesNameFilter");
    if(StringUtil.isEmpty(assigneesNameFilter))
    	assigneesNameFilter = "";
    String sourceLocaleFilter = filters.get("sourceLocaleFilter");
    if(StringUtil.isEmpty(sourceLocaleFilter))
    	sourceLocaleFilter = "";
    String targetLocaleFilter = filters.get("targetLocaleFilter");
    if(StringUtil.isEmpty(targetLocaleFilter))
    	targetLocaleFilter = "";
    String companyFilter = filters.get("companyFilter");
    if(StringUtil.isEmpty(companyFilter))
    	companyFilter = "";
    String advancedSearch = filters.get("advancedSearch");
    if(StringUtil.isEmpty(advancedSearch))
    	advancedSearch = "false";
    
    Date today = new Date();
    
    String userFormat = PageHandler.getUserParameter(session, UserParamNames.DOWNLOAD_OPTION_FORMAT).getValue();
    boolean isCombinedFormat = false;
    if (OfflineConstants.FORMAT_RTF_TRADOS.equals(userFormat)
    		|| OfflineConstants.FORMAT_RTF_TRADOS_OPTIMIZED.equals(userFormat)
    		|| OfflineConstants.FORMAT_OMEGAT_NAME.equals(userFormat))
    {
    	isCombinedFormat = true;
    }
    
    String badresults = (String)sessionManager.getMyactivitiesAttribute("badresults");
    if(badresults == null)
    	badresults = "";
    sessionManager.setMyactivitiesAttribute("badresults","");
    Boolean acceptDownloadRequested =
        (Boolean)request.getAttribute(OfflineConstants.DOWNLOAD_ACCEPT_DOWNLOAD);
    if( acceptDownloadRequested == null)
    {
        acceptDownloadRequested = new Boolean(false);
    }
    String taskParam =(String)request.getAttribute("taskParam");
    if(taskParam == null)
    {
        taskParam = "";
    }
    Boolean isDownloadCombined = (Boolean)request.getAttribute("isDownloadCombined");
    if( isDownloadCombined == null)
    {
        isDownloadCombined = new Boolean(false);
    }
    String errorMsg = (String) sessionManager.getAttribute("taskList_errorMsg");
    if( errorMsg == null || errorMsg.equals("null"))
    {
    	errorMsg = "";
    }
%>

<html>
<!-- This JSP is: envoy/tasks/taskList.jsp -->
<head>
    <meta http-equiv="content-type" content="text/html;charset=UTF-8">
    <title><%= title %></title>
    <link rel="STYLESHEET" type="text/css" href="/globalsight/includes/taskList.css">
    <link rel="STYLESHEET" type="text/css" href="/globalsight/includes/ContextMenu.css">
    <SCRIPT SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
    <script type="text/javascript" src="/globalsight/jquery/jquery-1.6.4.min.js"></script>
    <script type="text/javascript" src="/globalsight/includes/utilityScripts.js"></script>
    <%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
    <%@ include file="/envoy/common/warning.jspIncl" %>
    <%@ include file="/envoy/common/constants.jspIncl" %>
    <script type="text/javascript">
    var guideNode = "myActivities";
   	var $ascImg = $("<img border=0 width=7 hspace=1 height=4 src=\"/globalsight/images/sort-up.gif\">");
   	var $descImg = $("<img border=0 width=7 hspace=1 height=4 src=\"/globalsight/images/sort-down.gif\">");
    var sortColumn = "<%=sortColumn%>";
    var isDateSort = <%=isDateSort%>;
    var sortType = "<%=sortType%>";
    var sourceLocale = "<%=sourceLocaleFilter%>";
    var targetLocale = "<%=targetLocaleFilter%>";
    var company = "<%=companyFilter%>";
    var advancedSearch = "<%=advancedSearch%>";
    var showDateForm = false;
    $(document).ready(function() {
    	$("#jobIdOption").val("<%=jobIdOption%>");
		$("#jobIdFilter").val("<%=jobIdFilter%>");
    	$("#jobNameFilter").val("<%=jobNameFilter%>");
    	$("#activityNameFilter").val("<%=activityNameFilter%>");
    	$("#assigneesNameFilter").val("<%=assigneesNameFilter%>");
    	$("#priorityFilter").val("<%=priorityFilter%>");
    	$("#acceptanceStartFilter").val("<%=acceptanceStartFilter%>");
    	$("#acceptanceStartOptionsFilter").val("<%=acceptanceStartOptionsFilter%>");
    	$("#acceptanceEndFilter").val("<%=acceptanceEndFilter%>");
    	$("#acceptanceEndOptionsFilter").val("<%=acceptanceEndOptionsFilter%>");
    	$("#completionStartFilter").val("<%=completionStartFilter%>");
    	$("#completionStartOptionsFilter").val("<%=completionStartOptionsFilter%>");
    	$("#completionEndFilter").val("<%=completionEndFilter%>");
    	$("#completionEndOptionsFilter").val("<%=completionEndOptionsFilter%>");
    	rmoveSortImg();
    	var sortColumnString = sortColumn;
    	if(isDateSort)
    		sortColumnString = "taskDate";
		if (sortType == "asc") 
			$("#"+sortColumnString+"Sort").html($ascImg);
		 else 
			$("#"+sortColumnString+"Sort").html($descImg);
		changeSearchType();
    });

    function checkNow(field, text)
    {
        if (field.options[1].selected)
            text.value = "";
    }
	
    function changeSearchType()
    {
		if(advancedSearch == "true")
		{
			$("#dateForm").show();
			showDateForm = true;
			$("#searchTaskAdvanced").hide();
			$("#advanced").hide();
			$("#simple").show();
			advancedSearch = "false";
		}
		else
		{
			$("#dateForm").hide();
			showDateForm = false;
			$("#searchTaskAdvanced").show();
			$("#advanced").show();
			$("#simple").hide();
			$("#acceptanceStartFilter").val("");
	    	$("#acceptanceStartOptionsFilter").val("");
	    	$("#acceptanceEndFilter").val("");
	    	$("#acceptanceEndOptionsFilter").val("");
	    	$("#completionStartFilter").val("");
	    	$("#completionStartOptionsFilter").val("");
	    	$("#completionEndFilter").val("");
	    	$("#completionEndOptionsFilter").val("");
			advancedSearch = "true";
		}
    }

    </script>
</head>
<body>
<form id="hiddenForm">
    <input type="hidden" id="selfUrl" value="<%=selfUrl %>" />
    <input type="hidden" id="detailUrl" value="<%=detailUrl %>" />
    <input type="hidden" id="wordCountListUrl" value="<%=wordCountListUrl %>" />
    <input type="hidden" id="acceptAndDownloadUrl" value="<%=acceptAndDownloadUrl %>" />
    <input type="hidden" id="sendDownloadFileUrl" value="<%=sendDownloadFileUrl %>" />
    <input type="hidden" id="offlineUploadUrl" value="<%=offlineUploadPageUrl %>" />
    <input type="hidden" id="searchUrl" value="<%=searchUrl %>" />
    <input type="hidden" id="exportUrl" value="<%=exportUrl %>" />
    <input type="hidden" id="actionString" value="<%=WebAppConstants.TASK_ACTION %>" />
	<input type="hidden" id="state" value="<%=state%>" />
	<input type="hidden" id="perPageCount" value="<%=taskListParams.getPerPageCount() %>" />
	<input type="hidden" id="pageNumber" value="<%=taskListParams.getPageNumber() %>" />
	<input type="hidden" id="isSuperUser" value="<%=taskListParams.isSuperUser() %>" />
	<input type="hidden" id="acceptDownloadRequested" value="<%=acceptDownloadRequested %>" />
	<input type="hidden" id="taskParam" value="<%=taskParam %>" />
	<input type="hidden" id="isDownloadCombined" value="<%=isDownloadCombined %>" />
	<input type="hidden" id="errorMsg" value="<%=errorMsg %>" />
	<input type="hidden" id="isCombinedFormat" value="<%=isCombinedFormat %>" />
</form>
<FORM METHOD="post" NAME="downloadFilesForm" style="display:none">
<INPUT NAME="fileAction" VALUE="download" TYPE="HIDDEN">
<INPUT ID="selectedFileList" NAME="selectedFileList" VALUE="" TYPE="HIDDEN">
</FORM>
<div id="contentLayer" style=" POSITION: ABSOLUTE; Z-INDEX: 9; TOP: 108; LEFT: 20px; RIGHT: 20px;">
    <!-- Sub page title -->
    <div class="mainHeading">
        <%=bundle.getString("lb_my_activities") %> - <span id="stateString"><%=stateString%></span><br>
    </div>
    <div class="standardText" id="helpText" style="padding-top:10px;"></div>
    <div class="standardText" style="padding-top:10px;color:red"><%=badresults%></div>
    <!-- Current task state -->
    <div class="standardText" style="margin-bottom:5px;margin-top:7px;">
        <%=bundle.getString("lb_task_status") %>: <select id="taskStates"></select> <input id="searchTaskAdvanced" class="searchTask" type="button" value="Search"/>
        <a id="advanced" class="link" href="#" onclick="changeSearchType()">Advanced...</a>
		<a id="simple" class="link" href="#" onclick="changeSearchType()">Simple...</a>
        <form id="dateForm" name="dateForm" method="post" action="">
        <table style="margin-top:5px;">
        	<tr>
	          <td class="standardText" >
	            <%=bundle.getString("lb_acceptance_date_range")%>:
	          </td>
	          <td class="standardText"  nowrap>
	            <%=bundle.getString("lb_starts")%>:
	            <input type="text" id="acceptanceStartFilter" name="<%=acceptanceStart%>" size="3" maxlength="9">
	            <select id="acceptanceStartOptionsFilter" name="<%=acceptanceStartOptions%>">
	                <option value=''></option>
	                <option value='<%=SearchCriteriaParameters.HOURS_AGO%>'><%=bundle.getString("lb_hours_ago")%></option>
	                <option value='<%=SearchCriteriaParameters.DAYS_AGO%>'><%=bundle.getString("lb_days_ago")%></option>
	                <option value='<%=SearchCriteriaParameters.WEEKS_AGO%>'><%=bundle.getString("lb_weeks_ago")%></option>
	                <option value='<%=SearchCriteriaParameters.MONTHS_AGO%>'><%=bundle.getString("lb_months_ago")%></option>
	                <option value='<%=SearchCriteriaParameters.HOURS_FROM_NOW%>'><%=bundle.getString("lb_hours_from_now")%></option>
	                <option value='<%=SearchCriteriaParameters.DAYS_FROM_NOW%>'><%=bundle.getString("lb_days_from_now")%></option>
	                <option value='<%=SearchCriteriaParameters.WEEKS_FROM_NOW%>'><%=bundle.getString("lb_weeks_from_now")%></option>
	                <option value='<%=SearchCriteriaParameters.MONTHS_FROM_NOW%>'><%=bundle.getString("lb_months_from_now")%></option>
	            </select>
	            <%=bundle.getString("lb_ends")%>:
	            <input type="text" id="acceptanceEndFilter" name="<%=acceptanceEnd%>" size="3" maxlength="9">
	            <select id="acceptanceEndOptionsFilter" name="<%=acceptanceEndOptions%>" onChange="checkNow(this, dateForm.<%=acceptanceEnd%>)">
	                <option value=''></option>
	                <option value='<%=SearchCriteriaParameters.NOW%>'><%=bundle.getString("lb_now")%></option>
	                <option value='<%=SearchCriteriaParameters.HOURS_AGO%>'><%=bundle.getString("lb_hours_ago")%></option>
	                <option value='<%=SearchCriteriaParameters.DAYS_AGO%>'><%=bundle.getString("lb_days_ago")%></option>
	                <option value='<%=SearchCriteriaParameters.WEEKS_AGO%>'><%=bundle.getString("lb_weeks_ago")%></option>
	                <option value='<%=SearchCriteriaParameters.MONTHS_AGO%>'><%=bundle.getString("lb_months_ago")%></option>
	                <option value='<%=SearchCriteriaParameters.HOURS_FROM_NOW%>'><%=bundle.getString("lb_hours_from_now")%></option>
	                <option value='<%=SearchCriteriaParameters.DAYS_FROM_NOW%>'><%=bundle.getString("lb_days_from_now")%></option>
	                <option value='<%=SearchCriteriaParameters.WEEKS_FROM_NOW%>'><%=bundle.getString("lb_weeks_from_now")%></option>
	                <option value='<%=SearchCriteriaParameters.MONTHS_FROM_NOW%>'><%=bundle.getString("lb_months_from_now")%></option>
	            </select>
	          </td>
	          </tr>
	          <tr>
	          <td class="standardText" >
	            <%=bundle.getString("lb_estimated_completion_date")%>&nbsp;<%=bundle.getString("lb_range")%>:
	          </td>
	          <td class="standardText"  nowrap>
	            <%=bundle.getString("lb_starts")%>:
	            <input type="text" id="completionStartFilter" name="<%=completionStart%>" size="3" maxlength="9">
	            <select id="completionStartOptionsFilter" name="<%=completionStartOptions%>">
	                <option value=''></option>
	                <option value='<%=SearchCriteriaParameters.HOURS_AGO%>'><%=bundle.getString("lb_hours_ago")%></option>
	                <option value='<%=SearchCriteriaParameters.DAYS_AGO%>'><%=bundle.getString("lb_days_ago")%></option>
	                <option value='<%=SearchCriteriaParameters.WEEKS_AGO%>'><%=bundle.getString("lb_weeks_ago")%></option>
	                <option value='<%=SearchCriteriaParameters.MONTHS_AGO%>'><%=bundle.getString("lb_months_ago")%></option>
	                <option value='<%=SearchCriteriaParameters.HOURS_FROM_NOW%>'><%=bundle.getString("lb_hours_from_now")%></option>
	                <option value='<%=SearchCriteriaParameters.DAYS_FROM_NOW%>'><%=bundle.getString("lb_days_from_now")%></option>
	                <option value='<%=SearchCriteriaParameters.WEEKS_FROM_NOW%>'><%=bundle.getString("lb_weeks_from_now")%></option>
	                <option value='<%=SearchCriteriaParameters.MONTHS_FROM_NOW%>'><%=bundle.getString("lb_months_from_now")%></option>
	            </select>
	            <%=bundle.getString("lb_ends")%>:
	            <input type="text" id="completionEndFilter" name="<%=completionEnd%>" size="3" maxlength="9">
	            <select id="completionEndOptionsFilter" name="<%=completionEndOptions%>" onChange="checkNow(this, dateForm.<%=completionEnd%>)">
	                <option value=''></option>
	                <option value='<%=SearchCriteriaParameters.NOW%>'><%=bundle.getString("lb_now")%></option>
	                <option value='<%=SearchCriteriaParameters.HOURS_AGO%>'><%=bundle.getString("lb_hours_ago")%></option>
	                <option value='<%=SearchCriteriaParameters.DAYS_AGO%>'><%=bundle.getString("lb_days_ago")%></option>
	                <option value='<%=SearchCriteriaParameters.WEEKS_AGO%>'><%=bundle.getString("lb_weeks_ago")%></option>
	                <option value='<%=SearchCriteriaParameters.MONTHS_AGO%>'><%=bundle.getString("lb_months_ago")%></option>
	                <option value='<%=SearchCriteriaParameters.HOURS_FROM_NOW%>'><%=bundle.getString("lb_hours_from_now")%></option>
	                <option value='<%=SearchCriteriaParameters.DAYS_FROM_NOW%>'><%=bundle.getString("lb_days_from_now")%></option>
	                <option value='<%=SearchCriteriaParameters.WEEKS_FROM_NOW%>'><%=bundle.getString("lb_weeks_from_now")%></option>
	                <option value='<%=SearchCriteriaParameters.MONTHS_FROM_NOW%>'><%=bundle.getString("lb_months_from_now")%></option>
	            </select>
	          </td>
	          <td><input id="searchTaskSimple" class="searchTask" type="button" value="Search"/></td>
        	</tr>
        </table>
        </form>
    </div>
    <!-- Page bar -->
    <div class="pagebar">
        <div align="right" class="standardText pageNavBar">
            <span class="recordInfo"></span>&nbsp;&nbsp;<span class="pageNav"></span>
        </div>
    </div>
    <!-- Data list -->
    <form id="listForm" method="post">
        <input type="hidden" id="currentTaskState" value="<%=currentTaskState%>" />
        <div id="list" width="100%">  
            <table border="0" id="dataList" class="listborder" cellspacing="0" cellpadding="1" width="100%">
                <thead>
                    <tr class="tableHeadingBasic">
                        <th class="smallCell selectAll" width="5px"><input type="checkbox" id="selectAllCbx"></th>
                        <th class="smallCell priorityItem" id="priorityItem" width="5px">!<span id="prioritySort"></span></th>
                        <th class="smallCell overdueItem" width="5px"><img src="/globalsight/images/clock.gif" height="12" width="12" alt="Overdue"></th>
                        <th class="headerCell jobIdItem" id="jobIdItem" width=7%><%=bundle.getString("lb_job_id") %><span id="jobIdSort"></span></th>
                        <th class="headerCell jobNameItem" id="jobNameItem"><%=bundle.getString("lb_job_name") %><span id="jobNameSort"></span></th>
                        <th class="headerCell isUploadingItem" id="isUploadingItem">Uploading</th>
                        <th class="headerCell activityItem" id="activityNameItem"><%=bundle.getString("lb_activity") %><span id="activityNameSort"></span></th>
                        <th class="headerCell assigneeItem"><%=bundle.getString("lb_assignees") %></th>
                        <th class="headerCell sourceWordCountItem" id="sourceWordCountItem"><%=bundle.getString("lb_source_word_count") %><span id="sourceWordCountSort"></span></th>
                        <th class="headerCell translatedTextItem translatedText"><%=bundle.getString("lb_translated_text") %></th>
                        <th class="headerCell sourceLocaleItem" id="sourceLocaleItem"><%=bundle.getString("lb_source_locale") %><span id="sourceLocaleSort"></span></th>
                        <th class="headerCell targetLocaleItem" id="targetLocaleItem"><%=bundle.getString("lb_target_locale") %><span id="targetLocaleSort"></span></th>
                        <th class="headerCell taskDateItem taskDate" id="taskDateItem"><span id="taskDateLabel"><%=taskDateLabel %></span><span id="taskDateSort"></span></th>
                        <th class="headerCell ecdItem ecdDate"><%=bundle.getString("lb_estimated_completion_date") %></th>
                        <th class="headerCell taskStatusItem taskStatus"><%=bundle.getString("lb_task_status") %></th>
                        <th class="headerCell companyItem company"><%=bundle.getString("lb_company") %></th>
                    </tr>
                    <tr class="tableHeadingFilter">
                        <th class="smallCell">&nbsp;</th>
                        <th class="smallCell">
                       		<select id="priorityFilter" class="filterSelect">
		                        <option value=''></option>
				                <option value='1'>1</option>
				                <option value='2'>2</option>
				                <option value='3'>3</option>
				                <option value='4'>4</option>
				                <option value='5'>5</option>
	                        </select>
                        </th>
                        <th class="smallCell">&nbsp;</th>
                        <th class="jobIdItem" nowrap>
	                        <select id="jobIdOption">
		                        <option value='<%=SearchCriteriaParameters.EQUALS%>'>=</option>
				                <option value='<%=SearchCriteriaParameters.GREATER_THAN%>'>&gt;</option>
				                <option value='<%=SearchCriteriaParameters.LESS_THAN%>'>&lt;</option>
	                        </select>
	                        <input class="standardText jobIdField" type="text" id="jobIdFilter" name="jobIdFilter" />
                        </th>
                        <th class="jobNameItem"><input class="standardText" type="text" id="jobNameFilter" name="jobNameFilter" /></th>
                        <th class="isUploadingItem">&nbsp;</th>
                        <th class="activityItem"><input class="standardText" type="text" id="activityNameFilter" name="activityNameFilter" /></th>
                        <th class="assigneeItem"><input class="standardText" type="text" id="assigneesNameFilter" name="assigneesNameFilter" /></th>
                        <th class="sourceWordCountItem">&nbsp;</th>
                        <th class="translatedTextItem translatedText">&nbsp;</th>
                        <th class="sourceLocaleItem"><select id="sourceLocaleFilter" class="standardText filterSelect" ></select></th>
                        <th class="targetLocaleItem"><select id="targetLocaleFilter" class="standardText filterSelect" ></select></th>
                        <th class="taskDate">&nbsp;</th>
                        <th class="ecdItem ecdDate">&nbsp;</th>
                        <th class="taskStatusItem taskStatus">&nbsp;</th>
                        <th class="companyItem company"><select id="companyFilter" class="standardText filterSelect" ></select></th>
                    </tr>
                </thead>
                <tbody>
                </tbody>
            </table>
        </div>
	    <div class="pagebar">
	        <div align="right" class="standardText pageNavBar" style="padding-top:5px">
		        Display #:
		        <select id="perPageSet">
		           <option value="10">10</option>
		           <option value="20">20</option>
		           <option value="50">50</option>
                   <option value="100">100</option>
		           <option value="200">200</option>
		        </select>
		        &nbsp;&nbsp;<span class="recordInfo"></span>&nbsp;&nbsp;<span class="pageNav"></span>
	        </div>
	        <div id="buttons" align="left">
	        	<br>
	            <input type="button" id="acceptBtn" name="acceptBtn" value="Accept">
	            <input type="button" id="translatedTextBtn" name="translatedTextBtn" value="Translated Text" class="hide">
	            <input type="button" id="completeActivityBtn" name="completeActivityBtn" value="Complete Activity" class="hide">
	            <input type="button" id="completeWorkflowBtn" name="completeWorkflowBtn" value="Complete Workflow" class="hide">
	            <input type="button" id="detailWordCountBtn" name="detailWordCountBtn" value="Detailed Word Count..." class="hide">
	            <input type="button" id="searchReplaceBtn" name="searchReplaceBtn" value="Search/Replace..." class="hide">
	            <input type="button" id="exportBtn" name="exportBtn" value="Export..." class="hide">
	            <input type="button" id="exportDownloadBtn" name="exportDownloadBtn" value="Export & Download...">
	            <input type="button" id="downloadBtn" name="downloadBtn" value="Offline Download..." class="hide">
	            <input type="button" id="downloadCombinedBtn" name="downloadCombinedBtn" value="Offline Download Combined..." class="hide">
	            <input type="button" id="offlineUploadBtn" name="offlineUploadBtn" value="Offline Upload" class="hide">
	        </div>
	    </div>
    </form>
</div>
<span id="progress_content">
    <div id="idProgressDivDownload"
         style='border-style: solid; border-width: 1pt; border-color: #0c1476; background-color: white; display:none; left: 300px; height: 370; width: 500px; position: absolute; top: 150px; z-index: 21'>
        <%@ include file="/envoy/edit/offline/download/downloadProgressIncl.jsp" %>
    </div>
</span>
<span id="exportdownload_progress_content">
    <div id="idExportDownloadProgressDivDownload"
         style='border-style: solid; border-width: 1pt; border-color: #0c1476; background-color: white; display:none; left: 300px; height: 370; width: 500px; position: absolute; top: 150px; z-index: 21'>
        <%@ include file="/envoy/tasks/exportDownloadProgressIncl.jsp" %>
    </div>
</span>
</BODY>
<SCRIPT SRC="/globalsight/includes/cookieUtil.js"></SCRIPT>
<SCRIPT SRC="/globalsight/includes/radioButtons.js"></SCRIPT>
<script src="/globalsight/includes/ContextMenu.js"></script>
<script src="/globalsight/includes/ieemu.js"></script>
<script type="text/javascript" src="/globalsight/envoy/tasks/taskList.js"></script>
<script src="/globalsight/includes/jquery.contextmenu.r2.packed.js"></script>
</HTML>