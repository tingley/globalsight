<%@page import="com.globalsight.ling.common.URLEncoder"%>
<%@ page
        contentType="text/html; charset=UTF-8"
        errorPage="/envoy/common/error.jsp"
        import="java.util.*,com.globalsight.everest.taskmanager.Task,
                com.globalsight.everest.taskmanager.TaskImpl,
                com.globalsight.util.edit.EditUtil,
                com.globalsight.cxe.entity.fileprofile.FileProfile,
                com.globalsight.everest.costing.AmountOfWork,
                com.globalsight.everest.costing.Rate,
                com.globalsight.everest.permission.Permission,
                com.globalsight.everest.permission.PermissionSet,
                com.globalsight.everest.edit.offline.OfflineEditManager,
                com.globalsight.everest.secondarytargetfile.SecondaryTargetFile,
                com.globalsight.everest.util.system.SystemConfigParamNames,
                com.globalsight.everest.util.system.SystemConfiguration,
                com.globalsight.everest.webapp.WebAppConstants,
                com.globalsight.everest.webapp.javabean.NavigationBean,
                com.globalsight.everest.projecthandler.ProjectImpl,
                com.globalsight.everest.workflowmanager.WorkflowImpl,
                com.globalsight.everest.webapp.pagehandler.administration.reports.ReportConstants,
                com.globalsight.everest.webapp.pagehandler.PageHandler,
                com.globalsight.everest.webapp.pagehandler.offline.OfflineConstants,
                com.globalsight.everest.webapp.pagehandler.tasks.TaskHelper,
                com.globalsight.everest.webapp.pagehandler.tasks.TaskDetailHandler, 
                com.globalsight.everest.workflowmanager.Workflow,
                com.globalsight.everest.servlet.util.ServerProxy,
                com.globalsight.everest.servlet.util.SessionManager,
                com.globalsight.everest.webapp.pagehandler.administration.customer.download.DownloadFileHandler,
                com.globalsight.everest.webapp.pagehandler.projects.workflows.JobManagementHandler,
                com.globalsight.everest.workflow.ConditionNodeTargetInfo,
                com.globalsight.everest.page.TargetPage,
                com.globalsight.everest.util.system.SystemConfigParamNames,
                com.globalsight.everest.jobhandler.Job,
                com.globalsight.everest.company.CompanyWrapper,
                com.globalsight.everest.qachecks.QACheckerHelper,
                com.globalsight.everest.qachecks.DITAQACheckerHelper,
                com.globalsight.everest.foundation.Timestamp,
                com.globalsight.everest.foundation.User,
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
<jsp:useBean id="upload" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="uploadreport" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="downloadreport" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="downloadQAReport" scope="request" class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="uploadQAReport" scope="request" class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="downloadDitaReport" scope="request" class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="uploadDitaReport" scope="request" class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="detail" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request" />
<jsp:useBean id="comment" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request" />
<jsp:useBean id="cancel" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request" />
<jsp:useBean id="taskScorecard" scope="request" class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="export" scope="request" class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="updateLeverage" scope="request" class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="previous" scope="request" class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="accept" scope="request" class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="reject" scope="request" class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="finish" scope="request" class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="taskSecondaryTargetFiles" scope="request" class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="wordcountList" scope="request" class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<%
    SessionManager sessionMgr = 
        (SessionManager)session.getAttribute(WebAppConstants.SESSION_MANAGER);
    ResourceBundle bundle = PageHandler.getBundle(session);
    PermissionSet perms = (PermissionSet) session.getAttribute(WebAppConstants.PERMISSIONS);
    boolean reviewCommentReport= perms.getPermissionFor(Permission.REPORTS_LANGUAGE_SIGN_OFF);
    boolean reviewCommentSimpleReport= perms.getPermissionFor(Permission.REPORTS_LANGUAGE_SIGN_OFF_SIMPLE);
    Task task = (Task)TaskHelper.retrieveObject(session, WebAppConstants.WORK_OBJECT);
    int state = task.getState();
    long task_id = task.getId();
    boolean review_only = task.isType(Task.TYPE_REVIEW);
    String targetLanguage = task.getTargetLocale().getDisplayName();

    String detailUrl = detail.getPageURL() + 
        "&" + WebAppConstants.TASK_ACTION + 
        "=" + WebAppConstants.TASK_ACTION_RETRIEVE + 
        "&" + WebAppConstants.TASK_STATE + 
        "=" + state +
        "&" + WebAppConstants.TASK_ID + 
        "=" + task_id;
    
    String secondaryTargetFilesUrl = taskSecondaryTargetFiles.getPageURL() +
        "&" + WebAppConstants.TASK_ACTION +
        "=" + WebAppConstants.TASK_ACTION_RETRIEVE +
        "&" + WebAppConstants.TASK_STATE +
        "=" + state +
        "&" + WebAppConstants.TASK_ID +
        "=" + task_id;

    String scorecardUrl = taskScorecard.getPageURL() +
        "&" + WebAppConstants.TASK_ACTION +
        "=" + WebAppConstants.TASK_ACTION_SCORECARD +
        "&" + WebAppConstants.TASK_STATE +
        "=" + state +
        "&" + WebAppConstants.TASK_ID +
        "=" + task_id;
    String downloadUrl = download.getPageURL()+
            "&" + WebAppConstants.TASK_STATE +
            "=" + state +
            "&" + WebAppConstants.TASK_ID +
            "=" + task_id;
    String uploadUrl = upload.getPageURL()+
            "&" + WebAppConstants.TASK_STATE +
            "=" + state +
            "&" + WebAppConstants.TASK_ID +
            "=" + task_id;
    String uploadReportUrl = uploadreport.getPageURL()+
            "&" + WebAppConstants.TASK_STATE +
            "=" + state +
            "&" + WebAppConstants.TASK_ID +
            "=" + task_id;
    String commentUrl = comment.getPageURL()+
            "&" + WebAppConstants.TASK_STATE +
            "=" + state +
            "&" + WebAppConstants.TASK_ID +
            "=" + task_id;
    String cancelUrl = cancel.getPageURL() +
            "&" + WebAppConstants.TASK_ACTION + 
            "=" + WebAppConstants.TASK_ACTION_RETRIEVE +
            "&" + WebAppConstants.TASK_STATE +
            "=" + state +
            "&" + WebAppConstants.TASK_ID +
            "=" + task_id;
    String downloadReportUrl = downloadreport.getPageURL()+
            "&" + WebAppConstants.TASK_STATE +
            "=" + state +
            "&" + WebAppConstants.TASK_ID +
            "=" + task_id;

    String downloadDitaReportUrl = downloadDitaReport.getPageURL() +
            "&" + WebAppConstants.TASK_STATE +
            "=" + state +
            "&" + WebAppConstants.TASK_ID +
            "=" + task_id;

    String uploadDitaReportUrl = uploadDitaReport.getPageURL() +
            "&" + WebAppConstants.TASK_STATE +
            "=" + state +
            "&" + WebAppConstants.TASK_ID +
            "=" + task_id;
    
    String downloadQAReportUrl = downloadQAReport.getPageURL() +
            "&" + WebAppConstants.TASK_STATE +
            "=" + state +
            "&" + WebAppConstants.TASK_ID +
            "=" + task_id;

    String uploadQAReportUrl = uploadQAReport.getPageURL() +
            "&" + WebAppConstants.TASK_STATE +
            "=" + state +
            "&" + WebAppConstants.TASK_ID +
            "=" + task_id;

    // labels
    String title = bundle.getString("lb_download_dita_qa_report");
    String lbCancel = bundle.getString("lb_cancel");
    String lbRecentReports = bundle.getString("lb_recent_dita_reports");
    String labelActivity = bundle.getString("lb_activity") + bundle.getString("lb_colon");
    String labelJobName =  bundle.getString("lb_job_name") + bundle.getString("lb_colon");
    String lbDetails = bundle.getString("lb_details");
    String lbWorkoffline = bundle.getString("lb_work_offline");
    String lbComments = bundle.getString("lb_comments");
    String lbDownload = bundle.getString("lb_tab_download");
    String labelSave = bundle.getString("lb_save");

    sessionMgr.setAttribute(WebAppConstants.TARGETVIEW_LOCALE, targetLanguage);

    // Get data for the Hints table
    String jobName = task.getJobName();

    Task theTask = (Task)TaskHelper.retrieveObject(session, WebAppConstants.WORK_OBJECT);
    TaskImpl taskImpl = (TaskImpl)theTask;
    int isReportUploadCheck = taskImpl.getIsReportUploadCheck();
    int isUploaded = taskImpl.getIsReportUploaded();
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
    boolean showQAChecksTab = QACheckerHelper.isShowQAChecksTab(theTask);
    boolean showDITAQAChecksTab = DITAQACheckerHelper.isShowDITAChecksTab(theTask);

    String activityName = task.getTaskDisplayName();
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
                        "=" + task_id;
    
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
        String delayTimeKey = userId + task_id;
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
    String stfStatusMessage = "null";   // Secondary Target Files Status
    String isExportSTF = "false";       // Whether export Secondary Target Files
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

    String labelReportQAChecks = bundle.getString("lb_activity_qa_checks");
    String regenerateReportConfirm = bundle.getString("lb_run_dita_qa_checks_generate_report_confirm");
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
<script type="text/javascript" SRC="/globalsight/dojo/dojo.js"></script>

<SCRIPT LANGUAGE="JavaScript">
var taskId = <%=task_id%>;
function showProgressBar()
{
  var div = dojo.byId('loadingDiv');
  div.style.visibility = "visible";
}

var needWarning = false;
var guideNode = "myActivitiesUpload";
var helpFile = "<%=bundle.getString("help_download_dita")%>";

var finished = false;
var msg = "";

function doOnLoad()
{
  loadGuides();
}

function download()
{
	if (!confirm("<%=regenerateReportConfirm%>"))
		return;

	finished = false;

    var downloadButton = dojo.byId('downloadButton');
    downloadButton.disabled = true;
    showProgressBar();
    var obj = {
            taskId: "<%=task.getId()%>",
			random:Math.random()
    }
    
    dojo.xhrPost(
    {
       url:"/globalsight/ControlServlet?activityName=ditaReports&action=generateDitaReport",
       handleAs: "text", 
       content:obj,
       load:function(data)
       {
            finished = true;

            var downloadButton = dojo.byId('downloadButton');
            downloadButton.disabled = false;
                
            var div = dojo.byId('loadingDiv');
            div.style.visibility = "hidden";

            ReportForm.submit();
       },
       error:function(error)
       {
           finished = true;
       }
   });

    msg = '<%=bundle.getString("helper_test_download_report_generate")%>';
    changeWait();
}

var i = 0;

function changeWait()
{
    if (!finished)
    {
        var div = dojo.byId('loadingDiv');
        i++;
        if (i == 10)
        {
            i = 0;
        }

        var txt = msg;
        for (j = 0; j < i; j++)
        {
            txt += ".";             
        }

        div.innerHTML = txt;

        setTimeout(changeWait, 1000);
    }
}

function cancelReport()
{
	location.replace('<%=cancelUrl%>');
}

function recentReports()
{
    var url = "/globalsight/ControlServlet?activityName=ditaReports&taskId=<%=task.getId()%>";
    var specs = "width=600,height=490,toolbar=no,location=no,directories=no,status=no,menubar=no,scrollbars=no,resizable=no";
    specs += ",left="+ (screen.width)/20  + ",top=" + (screen.width)/20;
    controlWindow = window.open(url, "Recent DITA QA Reports", specs);
    controlWindow.focus();
}

// Highlight "QA Checks" tab
$(document).ready(function(){
    $("#qaChecksTab").removeClass("tableHeadingListOff");
    $("#qaChecksTab").addClass("tableHeadingListOn");
    $("#qaChecksTab img:first").attr("src","/globalsight/images/tab_left_blue.gif");
    $("#qaChecksTab img:last").attr("src","/globalsight/images/tab_right_blue.gif");
});
</SCRIPT>
<%@ include file="/envoy/common/warning.jspIncl" %>
</HEAD>

<BODY LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0" MARGINWIDTH="0" MARGINHEIGHT="0" ONLOAD="doOnLoad()">
<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>

<iframe id="idReport" name="idReport" src="about:blank" style="display:none"></iframe>
<FORM name="ReportForm" METHOD="POST" TARGET="idReport"
 ACTION="/globalsight/ControlServlet?activityName=ditaReports&action=getDitaReport">
    <input type="hidden" name="taskId" value="<%=task.getId() %>" />
</FORM>

<DIV ID="contentLayer" STYLE=" POSITION: ABSOLUTE; Z-INDEX: 9; TOP: 108px; LEFT: 20px; RIGHT: 20px;">
<TABLE CELLPADDING="0" CELLSPACING="0" BORDER="0">
<TR>
    <TD>
        <!-- Tabs table -->
        <TABLE CELLPADDING="0" CELLSPACING="0" BORDER="0">
            <TR><td><%@ include file="/envoy/tasks/includeTaskSummaryTabs.jspIncl" %></td></TR>
        </TABLE>
        <!-- End Tabs table -->
        <p>
        <!-- Tabs table -->
        <TABLE CELLPADDING="0" CELLSPACING="0" BORDER="0">
            <TR>
            <%if (showQAChecksTab) {%>
                <TD CLASS="tableHeadingListOff"><IMG SRC="/globalsight/images/tab_left_gray.gif" BORDER="0">
                    <A CLASS="sortHREFWhite" HREF="<%=downloadQAReportUrl%>"><%=bundle.getString("lb_download_qa_report")%></A><IMG SRC="/globalsight/images/tab_right_gray.gif" BORDER="0"></TD>
                <TD WIDTH="2"></TD>
                <TD CLASS="tableHeadingListOff"><IMG SRC="/globalsight/images/tab_left_gray.gif" BORDER="0">
                    <A CLASS="sortHREFWhite" HREF="<%=uploadQAReportUrl%>"><%=bundle.getString("lb_upload_qa_report")%></A><IMG SRC="/globalsight/images/tab_right_gray.gif" BORDER="0"></TD>
                <TD WIDTH="2"></TD>
            <%} %>
                <TD CLASS="tableHeadingListOn"><IMG SRC="/globalsight/images/tab_left_blue.gif" BORDER="0">
                    <A ONCLICK='submitForm()' CLASS="sortHREFWhite" HREF="<%=downloadDitaReportUrl%>"><%=bundle.getString("lb_download_dita_qa_report")%></A><IMG SRC="/globalsight/images/tab_right_blue.gif" BORDER="0"></TD>
                <TD WIDTH="2"></TD>
                <TD CLASS="tableHeadingListOff"><IMG SRC="/globalsight/images/tab_left_gray.gif" BORDER="0">
                    <A CLASS="sortHREFWhite" HREF="<%=uploadDitaReportUrl%>"><%=bundle.getString("lb_upload_dita_qa_report")%></A><IMG SRC="/globalsight/images/tab_right_gray.gif" BORDER="0"></TD>
            </TR>
        </TABLE>
        <!-- End Tabs table -->
    </TD>
    <TD ALIGN="RIGHT" VALIGN="BOTTOM" NOWRAP></TD>
</TR>

<TR><TD CLASS="tableHeadingBasic" COLSPAN="2" HEIGHT=1 WIDTH="500"><IMG SRC="/globalsight/images/spacer.gif" HEIGHT="1" WIDTH="1"></TD></TR>
<TR><TD COLSPAN="2">&nbsp;</TD></TR>
<TR><TD COLSPAN="2" WIDTH="500" CLASS="standardText"><%=bundle.getString("helper_text_download_dita_report_instruction")%></TD></TR>
<TR><TD COLSPAN="2">&nbsp;</TD></TR>
<TR><TD COLSPAN="2" id="loadingDiv" ALIGN="left" CLASS="standardText"></TD></TR>
<TR><TD COLSPAN="2">&nbsp;</TD></TR>
<TR>
    <TD COLSPAN="2" WIDTH="500">
        <INPUT TYPE="BUTTON" CLASS="standardText" NAME="<%=lbCancel%>" VALUE="<%=lbCancel%>" ONCLICK="cancelReport()">
        <INPUT id="recentReportsButton" TYPE="BUTTON" CLASS="standardText" NAME="recentDitaQaReports" VALUE="<%=lbRecentReports%>" ONCLICK="recentReports();">
        <INPUT id="downloadButton" TYPE="BUTTON" CLASS="standardText" NAME="runDitaChecks" VALUE="<%=bundle.getString("lb_run_dita_qa_checks_generate_report")%>" ONCLICK="download();">
    </TD>
</TR>
</TABLE>
</DIV>

</BODY>
</HTML>
