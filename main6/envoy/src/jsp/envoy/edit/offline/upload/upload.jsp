<%@page import="com.globalsight.ling.common.URLEncoder"%>
<%@ taglib uri="/WEB-INF/tlds/globalsight.tld" prefix="amb" %>
<%@ page
    	contentType="text/html; charset=UTF-8"
		errorPage="/envoy/common/error.jsp"
		import="java.util.*,com.globalsight.everest.taskmanager.Task,
				com.globalsight.everest.taskmanager.TaskImpl,
				com.globalsight.cxe.entity.fileprofile.FileProfile,
                com.globalsight.everest.costing.AmountOfWork,
                com.globalsight.everest.costing.Rate,
                com.globalsight.util.edit.EditUtil,
                com.globalsight.everest.permission.Permission,
                com.globalsight.everest.permission.PermissionSet,
                com.globalsight.everest.edit.offline.OfflineEditManager,
                com.globalsight.everest.util.system.SystemConfigParamNames,
                com.globalsight.everest.secondarytargetfile.SecondaryTargetFile,
                com.globalsight.everest.util.system.SystemConfiguration,
                com.globalsight.everest.webapp.WebAppConstants,
                com.globalsight.everest.webapp.javabean.NavigationBean,
                com.globalsight.everest.webapp.pagehandler.PageHandler,
                com.globalsight.everest.webapp.pagehandler.offline.OfflineConstants,
                com.globalsight.everest.webapp.pagehandler.tasks.TaskHelper,
                com.globalsight.everest.webapp.pagehandler.tasks.TaskDetailHandler, 
                com.globalsight.everest.projecthandler.ProjectImpl,
                com.globalsight.everest.workflowmanager.WorkflowImpl,
                com.globalsight.everest.workflowmanager.Workflow,
                com.globalsight.everest.servlet.util.ServerProxy,
                com.globalsight.everest.page.TargetPage,
                com.globalsight.everest.qachecks.QACheckerHelper,
                com.globalsight.everest.qachecks.DITAQACheckerHelper,
                com.globalsight.everest.util.system.SystemConfigParamNames,
	            com.globalsight.everest.webapp.pagehandler.administration.customer.download.DownloadFileHandler,
	            com.globalsight.everest.webapp.pagehandler.projects.workflows.JobManagementHandler,
	            com.globalsight.everest.workflow.ConditionNodeTargetInfo,
	            com.globalsight.everest.jobhandler.Job,
	            com.globalsight.everest.company.CompanyWrapper,
	            com.globalsight.everest.foundation.Timestamp,
	            com.globalsight.everest.foundation.User,
            	com.globalsight.everest.servlet.util.SessionManager,
            	com.globalsight.util.StringUtil,
                java.text.MessageFormat,
                java.util.Hashtable, 
                java.util.Iterator,
                java.util.List, 
                java.util.Map, 
                java.util.ResourceBundle,
                java.util.TreeMap" 
         session="true"
%>

<jsp:useBean id="download" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="detail" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request" />
<jsp:useBean id="comment" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request" />
<jsp:useBean id="cancel" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request" />
<jsp:useBean id="startupload" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request" />
<jsp:useBean id="errorPage" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="supportFiles" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="done" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="downloadreport" scope="request" class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="uploadreport" scope="request" class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="downloadQAReport" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="uploadQAReport" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="downloadDitaReport" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="uploadDitaReport" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="taskScorecard" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
 <jsp:useBean id="export" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
  <jsp:useBean id="updateLeverage" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
 <jsp:useBean id="previous" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
 <jsp:useBean id="accept" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
 <jsp:useBean id="reject" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
 <jsp:useBean id="finish" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
  <jsp:useBean id="taskSecondaryTargetFiles" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
   <jsp:useBean id="wordcountList" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<%
    SessionManager sessionMgr = 
        (SessionManager)session.getAttribute(WebAppConstants.SESSION_MANAGER);
    //From task upload or simple offline upload
    boolean fromTaskUpload = WebAppConstants.UPLOAD_FROMTASKUPLOAD
            .equals(sessionMgr.getAttribute(WebAppConstants.UPLOAD_ORIGIN));
    ResourceBundle bundle = PageHandler.getBundle(session);
    
    // labels
    String title = bundle.getString("lb_tab_upload");
    String instruction = bundle.getString("msg_upload");
    String lbCancel = bundle.getString("lb_cancel");
    String labelActivity = bundle.getString("lb_activity") + bundle.getString("lb_colon");
    String labelJobName =  bundle.getString("lb_job_name") + bundle.getString("lb_colon");
    String lbDetails = bundle.getString("lb_details");
    String lbWorkoffline = bundle.getString("lb_work_offline");
    String lbComments = bundle.getString("lb_comments");
    String lbDownload = bundle.getString("lb_tab_download");
    String lbUpload = bundle.getString("lb_tab_upload");
    String lbDownloadReport = bundle.getString("lb_download_report");
    String lbUploadReport = bundle.getString("lb_upload_report");
    String lbStartUpload = bundle.getString("lb_upload_start");
    String lbLastError = bundle.getString("lb_last_error");
    String lb_refresh = bundle.getString("lb_refresh");
    String labelSave = bundle.getString("lb_save");
    String lb_search_msg = "Please wait. Uploading files...";
    
    Task task = null;
    int state;
    long task_id;
    boolean review_only;
    int isReportUploadCheck = 0;
    int isUploaded = 0;
    String activityName=null;
    String jobName=null;
    // links
    String detailUrl=null;
    String secondaryTargetFilesUrl = null;
    String scorecardUrl = null;
    String downloadUrl=null;
    String commentUrl=null;
    String downloadReportUrl=null;
    String uploadReportUrl=null;
    String downloadQAReportUrl = null;
    String uploadQAReportUrl = null;
    String downloadDitaReportUrl = null;
    String uploadDitaReportUrl = null;
    String taskId = "";
    
    String urlDone = done.getPageURL() + 
            "&" + WebAppConstants.UPLOAD_ACTION +
            "=" + WebAppConstants.UPLOAD_ACTION_DONE;;
    String uploadUrl = startupload.getPageURL() +
            "&" + WebAppConstants.UPLOAD_ACTION +
            "=" + WebAppConstants.UPLOAD_ACTION_START_UPLOAD;;
    
    String supportFilesUrl = supportFiles.getPageURL();
    String errorPageUrl = errorPage.getPageURL();
    
    String helpTextUpload = bundle.getString("helper_text_offline_upload");
    String cancelUrl = cancel.getPageURL();
    String taskParam = null;
    if(fromTaskUpload)
    {
        task = (Task)TaskHelper.retrieveObject(session, WebAppConstants.WORK_OBJECT);
        state = task.getState();
        task_id = task.getId();
        review_only = task.isType(Task.TYPE_REVIEW);

        taskParam = "&" + WebAppConstants.TASK_STATE + 
	                "=" + state +
	                "&" + WebAppConstants.TASK_ID + 
	                "=" + task_id;
        detailUrl = detail.getPageURL() + 
            "&" + WebAppConstants.TASK_ACTION + 
            "=" + WebAppConstants.TASK_ACTION_RETRIEVE 
            + taskParam;
            
        
        secondaryTargetFilesUrl = taskSecondaryTargetFiles.getPageURL() +
		    "&" + WebAppConstants.TASK_ACTION +
		    "=" + WebAppConstants.TASK_ACTION_RETRIEVE +
		    "&" + WebAppConstants.TASK_STATE 
		    + taskParam;
        scorecardUrl = taskScorecard.getPageURL() +
		    "&" + WebAppConstants.TASK_ACTION +
		    "=" + WebAppConstants.TASK_ACTION_SCORECARD +
		    "&" + WebAppConstants.TASK_STATE +
		    "=" + state +
		    "&" + WebAppConstants.TASK_ID +
		    "=" + task_id;
        
        downloadUrl = download.getPageURL()
        		//GBS 2913 add taskId and state
        		+ taskParam;
        downloadReportUrl = downloadreport.getPageURL()
        		//GBS 2913 add taskId and state
        		+ taskParam;
        uploadReportUrl = uploadreport.getPageURL()
        		//GBS 2913 add taskId and state
        		+ taskParam;

        downloadQAReportUrl = downloadQAReport.getPageURL() + taskParam;
        uploadQAReportUrl = uploadQAReport.getPageURL() + taskParam;

        downloadDitaReportUrl = downloadDitaReport.getPageURL() + taskParam;
        uploadDitaReportUrl = uploadDitaReport.getPageURL() + taskParam;

        commentUrl = comment.getPageURL()
        		//GBS 2913 add taskId and state
        		+ taskParam;
        uploadUrl += taskParam;
        
        cancelUrl += taskParam;
        // Get data for the Hints table
        activityName = task.getTaskDisplayName();
        jobName = task.getJobName();
        helpTextUpload = bundle.getString("helper_text_upload");
        taskId = String.valueOf(task_id);
    }

    // control name
    String fileFieldName = OfflineConstants.UPLOAD_FILE_FIELD;

    boolean hasPrevError
       = (String)sessionMgr.getAttribute(OfflineConstants.ERROR_MESSAGE) == null ? false : true;

    // Determine if LocalizationParticipants (translators) are allowed
    // to upload Support Files. 
    PermissionSet perms = (PermissionSet) session.getAttribute(
                    WebAppConstants.PERMISSIONS);
    boolean b_supportFileUpload =
         perms.getPermissionFor(Permission.ACTIVITIES_UPLOAD_SUPPORT_FILES);
%>
<HTML>
<HEAD>
<TITLE><%=title%></TITLE>
<link rel="STYLESHEET" type="text/css" href="/globalsight/includes/ContextMenu.css">
<link rel="stylesheet" type="text/css" href="/globalsight/jquery/jQueryUI.redmond.css"/>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<script type="text/javascript" src="/globalsight/jquery/jquery-1.6.4.min.js"></script>
<script type="text/javascript" src="/globalsight/jquery/jquery-ui-1.8.18.custom.min.js"></script>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/utilityScripts.js"></SCRIPT>

<SCRIPT LANGUAGE="JavaScript">
var WIDTH = 400;
var needWarning = false;
var guideNode = "myActivitiesUpload";
var helpFile = "<%=bundle.getString("help_upload")%>";

function submitForm()
{
    ignoreClose = true;
    if (document.layers)
    {
        theForm = document.contentLayer.document.uploadForm;
    }
    else
    {
        theForm = document.uploadForm;
    }
    if (!isEmptyString(theForm.<%=fileFieldName%>.value))
    {
       theForm.action = "<%=uploadUrl%>" +
           "&" + theForm.<%=fileFieldName%> +
           "=" + theForm.<%=fileFieldName%>.value;
       theForm.submit();
    }
    else
    {
        alert("<%= bundle.getString("jsmsg_upload_no_file") %>");
    }
}

function doOnLoad()
{
  loadGuides();
}

$(document).ready(function(){
	$("#taskWorkOfflineTab").removeClass("tableHeadingListOff");
	$("#taskWorkOfflineTab").addClass("tableHeadingListOn");
	$("#taskWorkOfflineTab img:first").attr("src","/globalsight/images/tab_left_blue.gif");
	$("#taskWorkOfflineTab img:last").attr("src","/globalsight/images/tab_right_blue.gif");
})
</SCRIPT>
<%@ include file="/envoy/common/warning.jspIncl" %>
</HEAD>

<BODY LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0" MARGINWIDTH="0" MARGINHEIGHT="0"
    ONLOAD="doOnLoad()">
<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>

<DIV ID="contentLayer" STYLE=" POSITION: ABSOLUTE; Z-INDEX: 9; TOP: 108px; LEFT: 20px; RIGHT: 20px;">
<%if(fromTaskUpload){%>
<%} else {%>
<P CLASS="mainHeading"><%=bundle.getString("lb_offline_upload")%></P>
<%} %>
<TABLE CELLPADDING="0" CELLSPACING="0" BORDER="0">
<%if(fromTaskUpload){
	Task theTask = (Task)TaskHelper.retrieveObject(session, WebAppConstants.WORK_OBJECT);
	TaskImpl taskImpl = (TaskImpl)theTask;
    isReportUploadCheck = taskImpl.getIsReportUploadCheck();
    isUploaded = taskImpl.getIsReportUploaded();
	WorkflowImpl workflowImpl = (WorkflowImpl) theTask.getWorkflow();
    ProjectImpl project = (ProjectImpl)theTask.getWorkflow().getJob().getProject();
    boolean needScore = false;
    if(StringUtil.isEmpty(workflowImpl.getScorecardComment()) &&
    		workflowImpl.getScorecardShowType() == 1 &&
    		theTask.isType(Task.TYPE_REVIEW))
    {
    	needScore = true;
    }
    String labelReportUploadCheckWarning = "Translation Edit Report not uploaded";
    String labelReportUploadCheckWarningMessage = bundle.getString("jsmsg_my_activities_translation_edit_report_upload_check");
    if(theTask.isType(Task.TYPE_REVIEW))
    {
    	labelReportUploadCheckWarning = "Reviewer Comments Report not uploaded";
    	labelReportUploadCheckWarningMessage = bundle.getString("jsmsg_my_activities_reviewer_comments_report_upload_check");
    }
    String labelReportQAChecks = bundle.getString("lb_activity_qa_checks");
    boolean showQAChecksTab = QACheckerHelper.isShowQAChecksTab(theTask);
    boolean showDITAQAChecksTab = DITAQACheckerHelper.isShowDITAChecksTab(theTask);
    
	state = theTask.getState();
	review_only = theTask.isType(Task.TYPE_REVIEW);
	String pageId = (String)TaskHelper.retrieveObject(session, WebAppConstants.TASK_DETAILPAGE_ID);
	boolean isPageDetailOne = TaskHelper.DETAIL_PAGE_1.equals(pageId) ? true:false;
	boolean alreadyAccepted = false;
    boolean disableButtons = false;
	Date dt = new Date();
	String labelABorDBorCODate = bundle.getString("lb_due_date") + bundle.getString("lb_colon");
	TimeZone timeZone = (TimeZone)session.getAttribute(WebAppConstants.USER_TIME_ZONE);
    Timestamp ts = new Timestamp(timeZone);
	ts.setDate(theTask.getEstimatedCompletionDate());
	String valueABorDBorCODate = ts.toString();
	String projName = theTask.getProjectName();
    String projManager = theTask.getProjectManagerName();
	String companyName = CompanyWrapper.getCompanyNameById(theTask.getCompanyId());
	Locale uiLocale = (Locale)session.getAttribute(WebAppConstants.UILOCALE);
	String sourceLocale = theTask.getSourceLocale().getDisplayName(uiLocale);
    String targetLocale = theTask.getTargetLocale().getDisplayName(uiLocale);
	String priority = Integer.toString(theTask.getPriority());
	String jobId = Long.toString(theTask.getJobId());
    long jId = (new Long(jobId)).longValue();
    Job theJob = ServerProxy.getJobHandler().getJobById(jId);
    String sourceWordCount = (new Long(theJob.getWordCount())).toString();
    String locProfileName = theJob.getL10nProfile().getName();
    String labelYes = bundle.getString("lb_yes");
    String labelNo = bundle.getString("lb_no");
    String dAbbr = bundle.getString("lb_abbreviation_day");
    String hAbbr = bundle.getString("lb_abbreviation_hour");
    String mAbbr = bundle.getString("lb_abbreviation_minute");
    String duration = theTask.getTaskDurationAsString(dAbbr, hAbbr, mAbbr);
    String openSegmentComments = (String)session.getAttribute(JobManagementHandler.OPEN_AND_QUERY_SEGMENT_COMMENTS);
    String closedSegmentComments = (String)session.getAttribute(JobManagementHandler.CLOSED_SEGMENT_COMMENTS);
    String labelAccept = bundle.getString("lb_accept");
    String labelReject = bundle.getString("lb_reject");
    String labelAvailable = bundle.getString("lb_available");
    String labelFinished = bundle.getString("lb_finished");
    String labelRejected = bundle.getString("lb_rejected");
    String labelAccepted = bundle.getString("lb_accepted");
    String labelFinishing = bundle.getString("lb_finishing");
    String labelCompletedOn = bundle.getString("lb_completed_on") + bundle.getString("lb_colon");
    ts.setDate(theTask.getCompletedDate());
    String completedOn = ts.toString();
    String labelAcceptBy = bundle.getString("lb_accept_by") + bundle.getString("lb_colon");
    ts.setDate(theTask.getEstimatedAcceptanceDate());
    String acceptBy = ts.toString();
    String status = "";
	switch (state)
    {
    case Task.STATE_ACCEPTED:
        status = labelAccepted;
        isPageDetailOne = false;
        break;
    case Task.STATE_COMPLETED:
        status = labelFinished;
        disableButtons = true;
        labelABorDBorCODate = labelCompletedOn;
        valueABorDBorCODate = completedOn;
        break;
    case Task.STATE_REJECTED:
        status = labelRejected;
        disableButtons = true;
        break;
    case Task.STATE_DEACTIVE:
        alreadyAccepted = true;
        break;
    case Task.STATE_ACTIVE:
        status = labelAvailable;
        labelABorDBorCODate = labelAcceptBy;
        valueABorDBorCODate = acceptBy;
        break;
    case Task.STATE_DISPATCHED_TO_TRANSLATION:
        status = labelAccepted;
        isPageDetailOne = false;
        disableButtons = true;
        break;
    case Task.STATE_IN_TRANSLATION:
        status = labelAccepted;
        isPageDetailOne = false;
        disableButtons = true;
        break;
    case Task.STATE_TRANSLATION_COMPLETED:
        status = labelAccepted;
        isPageDetailOne = false;
        break;
    case Task.STATE_REDEAY_DISPATCH_GSEDTION:
        status = labelAccepted;
        isPageDetailOne = false;
        disableButtons = true;
        break;
    case Task.STATE_FINISHING:
        status = labelFinishing;
        isPageDetailOne = false;
        disableButtons = true;
        break;
    default:
        break;
    }
    
    StringBuffer tmpUrl = new StringBuffer(updateLeverage.getPageURL());
    tmpUrl.append("&").append(WebAppConstants.TASK_ID).append("=").append(theTask.getId()).append("&action=getAvailableJobsForTask");
    String updateLeverageUrl = tmpUrl.toString();
    String taskListStartStr = String.valueOf(session.getAttribute("taskListStart"));
    String previousUrl = previous.getPageURL() + "&taskStatus=" + state + "&taskId=" + theTask.getId();
    if (taskListStartStr != null)
    {
        previousUrl += "&taskListStart=" + taskListStartStr;
    }
    String acceptUrl = accept.getPageURL() + 
			    		"&" + WebAppConstants.TASK_ACTION +
			        	"=" + WebAppConstants.TASK_ACTION_ACCEPT+
			        	//GBS add task id
			        	"&" + WebAppConstants.TASK_ID+
			        	"=" + theTask.getId();
    String rejectUrl = reject.getPageURL()+
		    		//GBS 2913 add taskId and state
					"&" + WebAppConstants.TASK_ID+
					"=" + theTask.getId()+
					"&" + WebAppConstants.TASK_STATE+
					"=" + theTask.getState();
    String finishUrl = finish.getPageURL() + 
		    		"&" + WebAppConstants.TASK_ACTION +
		        	"=" + WebAppConstants.TASK_ACTION_FINISH+
		        	//GBS 2913 add taskId and state
					"&" + WebAppConstants.TASK_ID+
					"=" + theTask.getId()+
					"&" + WebAppConstants.TASK_STATE+
					"=" + theTask.getState();
    
    String wordCountUrl = wordcountList.getPageURL() + "&action=tpList"+
						//GBS-2913 Added to the url parameter taskId,state;
					    "&"+WebAppConstants.TASK_ID+
					    "="+theTask.getId()+
					    "&"+WebAppConstants.TASK_STATE+
					    "="+theTask.getState();
	String saveUrl = detail.getPageURL() +
						"&" + WebAppConstants.TASK_ACTION +
						"=" + WebAppConstants.TASK_ACTION_SAVEDETAILS +
						"&" + WebAppConstants.TASK_STATE +
						"=" + state +
						"&" + WebAppConstants.TASK_ID +
						"=" + theTask.getId();
    
    StringBuffer downloadLink = new StringBuffer("/globalsight/ControlServlet" +
                                "?linkName=jobDownload&pageName=TK2" + 
                                "&firstEntry=true&fromTaskDetail=true");
    downloadLink.append("&");
    downloadLink.append(DownloadFileHandler.PARAM_JOB_ID);
    downloadLink.append("=");
    downloadLink.append(jobId);
    downloadLink.append("&");
    downloadLink.append(DownloadFileHandler.PARAM_WORKFLOW_ID);
    downloadLink.append("=");
    downloadLink.append(theTask.getWorkflow().getId());
  	//GBS-2913 Added to the url parameter taskId
    downloadLink.append("&");
    downloadLink.append(WebAppConstants.TASK_ID);
    downloadLink.append("=");
    downloadLink.append(theTask.getId());
    downloadLink.append("&");
    downloadLink.append(WebAppConstants.TASK_STATE);
    downloadLink.append("=");
    downloadLink.append(theTask.getState());
    
    final int stateAvailable = Task.STATE_ACTIVE;
    final int stateInProgress = Task.STATE_ACCEPTED;

	//Labels of the page
    String labelCompany = bundle.getString("lb_company") + bundle.getString("lb_colon");
    String labelJob =  bundle.getString("lb_job") + bundle.getString("lb_colon");
    String labelJobId =  bundle.getString("lb_job_id") + bundle.getString("lb_colon");
    String labelProject = bundle.getString("lb_project") + bundle.getString("lb_colon");
    String labelProjectManager = bundle.getString("lb_project_manager") + bundle.getString("lb_colon");
    String labelSourceLocale = bundle.getString("lb_source_locale") + bundle.getString("lb_colon");
    String labelTargetLocale = bundle.getString("lb_target_locale") + bundle.getString("lb_colon");
    String labelTimeToComplete = bundle.getString("lb_time_complete") + bundle.getString("lb_colon");
    String labelOverdue = bundle.getString("lb_overdue") + bundle.getString("lb_colon");
    String labelStatus = bundle.getString("lb_status") + bundle.getString("lb_colon");
    String labelPriority = bundle.getString("lb_priority") + bundle.getString("lb_colon");
    String labelSelectActivity = bundle.getString("lb_selectActiviy") + bundle.getString("lb_colon");
    String labelHours = bundle.getString("lb_hours_capitalized") + bundle.getString("lb_colon");
    String labelPages = bundle.getString("lb_pages_capitalized") + bundle.getString("lb_colon");
    String labelLocProfile = bundle.getString("lb_loc_profile") + bundle.getString("lb_colon");

    String labelTargetFiles = bundle.getString("lb_TargetFiles");
    String labelSecondaryTargetFiles = bundle.getString("lb_secondary_target_files");
    String labelWorkoffline = bundle.getString("lb_work_offline");
    String labelScorecard = bundle.getString("lb_scorecard");
    String labelComments = bundle.getString("lb_comments");
    String labelContentItem = bundle.getString("lb_primary_target_files");
    String labelClickToOpen = bundle.getString("lb_clk_to_open");
    String labelClickToView = bundle.getString("lb_click_to_view");
    String labelWordCount = bundle.getString("lb_word_count");
    String labelTotalWordCount = bundle.getString("lb_source_word_count_total");
    String labeltTaskCompleted = bundle.getString("lb_taskcompleted");
    String labelUpdateLeverage = bundle.getString("lb_update_leverage");
    String labelFinishWarning = bundle.getString("jsmsg_my_activities_finished");
 	// Create the exportLink for the Export button
    StringBuffer exportLink = new StringBuffer(export.getPageURL());
    exportLink.append("&");
    exportLink.append(JobManagementHandler.WF_ID);
    exportLink.append("=");
    exportLink.append(theTask.getWorkflow().getId());
    exportLink.append("&");
    exportLink.append(JobManagementHandler.EXPORT_SELECTED_WORKFLOWS_ONLY_PARAM);
    exportLink.append("=true");
  	//GBS 2913 add taskId and state
    exportLink.append("&");
    exportLink.append(WebAppConstants.TASK_ID);
    exportLink.append("=");
    exportLink.append(theTask.getId());
    exportLink.append("&");
    exportLink.append(WebAppConstants.TASK_STATE);
    exportLink.append("=");
    exportLink.append(theTask.getState());
    
    long startExportTime = 0;
    String userId = null;
    try{	
	    User user = (User)sessionMgr.getAttribute(WebAppConstants.USER);
	    userId = user.getUserId();
    }catch(Exception e){}
    Hashtable delayExportTimeTable = (Hashtable)sessionMgr.getAttribute(WebAppConstants.DOWLOAD_DELAY_TIME_TABLE);
    if(delayExportTimeTable != null)
    {
        String delayTimeKey = userId + jobId + theTask.getWorkflow().getId();
        Date startTimeObj = (Date)delayExportTimeTable.get(delayTimeKey);
        if(startTimeObj != null)
        {
            startExportTime = startTimeObj.getTime();   
        }
    }
    
    long startTime = 0; 
    Hashtable delayTimeTable = (Hashtable)sessionMgr.getAttribute(WebAppConstants.TASK_COMPLETE_DELAY_TIME_TABLE);
    if(delayTimeTable != null)
    {       
        String delayTimeKey = userId + taskId;
        Date startTimeObj = (Date)delayTimeTable.get(delayTimeKey);         
        if(startTimeObj != null)
        {
            startTime = startTimeObj.getTime();
        }
    }
    
    boolean isHourlyJobCosting = ((Boolean)TaskHelper.retrieveObject(session,
        TaskDetailHandler.TASK_HOURS_STATE)).booleanValue();
    String hoursParam = TaskDetailHandler.TASK_HOURS;

    boolean isPageBasedJobCosting = ((Boolean)TaskHelper.retrieveObject(session,
        TaskDetailHandler.TASK_PAGES_STATE)).booleanValue();
    String pagesParam = TaskDetailHandler.TASK_PAGES;
      
    String labelSelectionWarning = bundle.getString("jsmsg_my_activities_Warning");
    List targetPgs = (List)TaskHelper.retrieveObject(session, JobManagementHandler.TARGET_PAGES);
    StringBuffer targetPageIdParameter = new StringBuffer(""); 
    List<FileProfile> fileProfiles = new ArrayList<FileProfile>();
    for (Iterator it = targetPgs.iterator(); it.hasNext(); )
    {
        TargetPage tp = (TargetPage) it.next();
        targetPageIdParameter.append("&targetPgId=");
        targetPageIdParameter.append(tp.getId());
        FileProfile fp = ServerProxy.getFileProfilePersistenceManager().getFileProfileById(tp.getSourcePage().getRequest().getDataSourceId(), true);
        fileProfiles.add(fp);
    }
    String stfCreationState = theTask.getStfCreationState();
    String stfStatusMessage = "null";	// Secondary Target Files Status
	String isExportSTF = "false";		// Whether export Secondary Target Files
    if (Task.IN_PROGRESS.equals(stfCreationState))
    {
		stfStatusMessage = "inprogress";
    }
    else if (Task.FAILED.equals(stfCreationState))
    {
		stfStatusMessage = "failed";
	}
    for(FileProfile fp:fileProfiles)
    {
        long formatType = fp.getKnownFormatTypeId();
        boolean defaultStfExport = fp.byDefaultExportStf();
        if(!(formatType ==((long)23)))
        {
           if(defaultStfExport)
           {
               isExportSTF = "true";
           }
        }
    }
%>
<SCRIPT LANGUAGE="JavaScript">
var taskId = <%=theTask.getId()%>;
</SCRIPT>
<TR>
<TD>
<!-- Tabs table -->
<TABLE CELLPADDING="0" CELLSPACING="0" BORDER="0">
<TR>
  <TD><%@ include file="/envoy/tasks/includeTaskSummaryTabs.jspIncl" %></TD>
</TR>
</TABLE>
<!-- End Tabs table -->
<p>
<!-- Tabs table -->
<TABLE CELLPADDING="0" CELLSPACING="0" BORDER="0">
<TR>
  <TD CLASS="tableHeadingListOff"><IMG SRC="/globalsight/images/tab_left_gray.gif" BORDER="0"><A CLASS="sortHREFWhite" HREF="<%=downloadUrl%>"><%=lbDownload%></A><IMG SRC="/globalsight/images/tab_right_gray.gif" BORDER="0"></TD>
  <TD WIDTH="2"></TD>
  <TD CLASS="tableHeadingListOn"><IMG SRC="/globalsight/images/tab_left_blue.gif" BORDER="0"><A ONCLICK='submitForm()' CLASS="sortHREFWhite" ><%=lbUpload%></A><IMG SRC="/globalsight/images/tab_right_blue.gif" BORDER="0"></TD>
  <%if(isReportUploadCheck == 1 || perms.getPermissionFor(Permission.REPORTS_TRANSLATIONS_EDIT) || perms.getPermissionFor(Permission.REPORTS_POST_REVIEW_QA) || perms.getPermissionFor(Permission.REPORTS_TRANSLATIONS_VERIFICATION)) {%>
  <TD WIDTH="2"></TD>
  <TD CLASS="tableHeadingListOff"><IMG SRC="/globalsight/images/tab_left_gray.gif" BORDER="0"><A CLASS="sortHREFWhite" HREF="<%=downloadReportUrl%>"><%=lbDownloadReport%></A><IMG SRC="/globalsight/images/tab_right_gray.gif" BORDER="0"></TD>
  <TD WIDTH="2"></TD>
  <TD CLASS="tableHeadingListOff"><IMG SRC="/globalsight/images/tab_left_gray.gif" BORDER="0"><A CLASS="sortHREFWhite" HREF="<%=uploadReportUrl%>"><%=lbUploadReport%></A><IMG SRC="/globalsight/images/tab_right_gray.gif" BORDER="0"></TD>
  <%} %>
</TR>
</TABLE>
<!-- End Tabs table -->
</TD>

<TD ALIGN="RIGHT" VALIGN="BOTTOM" NOWRAP></TD>
</TR>

<TR>
<TD CLASS="tableHeadingBasic" COLSPAN="2" HEIGHT=1 WIDTH="500"><IMG SRC="/globalsight/images/spacer.gif" HEIGHT="1" WIDTH="1"></TD>
</TR>
<% }%>

<TR>
<TD COLSPAN="2">&nbsp;</TD>
</TR>

<TR>
<TD COLSPAN="2">


<!-- Lower table -->
<TABLE CELLPADDING="2" CELLSPACING="0" BORDER="0" CLASS="standardText">
<TR>
<TD VALIGN="TOP" WIDTH="500">
<P>
<%=bundle.getString("lb_upload_follow_instructions")%>
</P>
<BR>

<TABLE CELLPADDING="2" CELLSPACING="0" BORDER="0" CLASS="standardText">
    <TD><IMG SRC="/globalsight/images/1.gif" HEIGHT=23 WIDTH=23></TD>
    <TD><%=bundle.getString("lb_upload_click_browse")%></TD>
  </TR>
  <TR>
    <TD>&nbsp;</TD>
    <TD>
      <FORM ACTION="<%=uploadUrl%>" NAME="uploadForm" METHOD="POST" ENCTYPE="multipart/form-data">
      <BR>
      <INPUT TYPE="file"  CLASS="standardText" SIZE="40" NAME="<%=fileFieldName%>">
      </FORM>
    </TD>
  </TR> 
  <TR>
    <TD><IMG SRC="/globalsight/images/2.gif" HEIGHT=23 WIDTH=23></TD>
    <TD><%=bundle.getString("lb_upload_click_upload")%></TD>
  </TR>
  <TR>
    <TD COLSPAN=2>&nbsp;</TD>
  </TR>
  <TR>
    <TD><IMG SRC="/globalsight/images/3.gif" HEIGHT=23 WIDTH=23></TD>
    <TD><%=bundle.getString("lb_upload_repeat")%></TD>
  </TR>
</TABLE>
<P>
		
<INPUT TYPE="BUTTON" NAME="<%=lbCancel%>" VALUE="<%=lbCancel%>" 
    ONCLICK="location.replace('<%=cancelUrl%>')">   
<INPUT TYPE="BUTTON" NAME="<%=lbStartUpload%>"
	VALUE="<%=lbStartUpload%>" onclick="submitForm()">

<%
    if(hasPrevError)
    {
%>
    <P><A CLASS="standardHREF" HREF="<%=errorPageUrl%>" ><%= lbLastError %></A></P>
<%    }
%>

</TD>
</TR>
</TABLE>
<!-- End lower table-->
</TD>
</TR>
</TABLE>

</DIV>
</BODY>
</HTML>