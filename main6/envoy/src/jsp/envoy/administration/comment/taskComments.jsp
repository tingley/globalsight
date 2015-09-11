<%@page import="java.io.File"%><%@page import="com.globalsight.ling.common.URLEncoder"%>

<%@ taglib uri="/WEB-INF/tlds/globalsight.tld" prefix="amb" %>
<%@ page contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="java.util.*,com.globalsight.everest.webapp.javabean.NavigationBean,
    	 com.globalsight.everest.costing.AmountOfWork,
    	 com.globalsight.everest.costing.Rate,
    	 com.globalsight.everest.company.CompanyWrapper,
    	 com.globalsight.cxe.entity.fileprofile.FileProfile,
         com.globalsight.util.edit.EditUtil,
         com.globalsight.util.GlobalSightLocale,
         com.globalsight.everest.taskmanager.Task,
         com.globalsight.everest.taskmanager.TaskImpl,
         com.globalsight.util.resourcebundle.ResourceBundleConstants,
         com.globalsight.util.resourcebundle.SystemResourceBundle,
         com.globalsight.everest.secondarytargetfile.SecondaryTargetFile,
         com.globalsight.everest.projecthandler.ProjectImpl,
         com.globalsight.everest.workflowmanager.WorkflowImpl,
         com.globalsight.everest.jobhandler.Job,
         com.globalsight.everest.comment.Comment,
         com.globalsight.everest.comment.CommentFile,
         com.globalsight.everest.comment.CommentUpload,
         com.globalsight.everest.comment.CommentManager,
         com.globalsight.everest.foundation.Timestamp,
         com.globalsight.everest.page.TargetPage,
         com.globalsight.everest.permission.Permission,
         com.globalsight.everest.permission.PermissionSet,
         com.globalsight.everest.servlet.util.ServerProxy,
         com.globalsight.everest.servlet.util.SessionManager,
         com.globalsight.everest.webapp.WebAppConstants,
         com.globalsight.everest.webapp.tags.TableConstants,        
         com.globalsight.everest.webapp.pagehandler.offline.OfflineConstants,
         com.globalsight.everest.webapp.pagehandler.PageHandler,
         com.globalsight.everest.webapp.pagehandler.tasks.TaskHelper,
         com.globalsight.everest.webapp.pagehandler.tasks.TaskDetailHandler,
         com.globalsight.everest.qachecks.QACheckerHelper,
         com.globalsight.everest.qachecks.DITAQACheckerHelper,
         com.globalsight.everest.webapp.pagehandler.administration.comment.LocaleCommentsSummary,
         com.globalsight.everest.webapp.pagehandler.administration.comment.LocaleCommentsComparator,
         com.globalsight.everest.webapp.pagehandler.administration.comment.PageCommentsSummary,
         com.globalsight.everest.webapp.pagehandler.administration.comment.CommentConstants,
         com.globalsight.everest.webapp.pagehandler.administration.customer.download.DownloadFileHandler,
         com.globalsight.everest.webapp.pagehandler.projects.workflows.JobManagementHandler,
         com.globalsight.everest.workflow.ConditionNodeTargetInfo,
         com.globalsight.everest.util.comparator.CommentComparator,
      	 com.globalsight.everest.util.system.SystemConfigParamNames,
         com.globalsight.everest.foundation.User,
         com.globalsight.everest.webapp.pagehandler.administration.users.UserUtil,
         com.globalsight.util.AmbFileStoragePathUtils,
         com.globalsight.util.StringUtil,
         java.text.MessageFormat,
         java.text.NumberFormat,
         java.util.Locale, java.util.ResourceBundle,
         java.util.List,
         java.util.ArrayList"
         session="true"
%>
<jsp:useBean id="downloadreport" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="uploadreport" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="downloadQAReport" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="uploadQAReport" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="downloadDitaReport" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="uploadDitaReport" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
 <jsp:useBean id="taskSecondaryTargetFiles" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
 <jsp:useBean id="taskScorecard" scope="request"
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
 <jsp:useBean id="export" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="skinbean" scope="application"
 class="com.globalsight.everest.webapp.javabean.SkinBean" />
<jsp:useBean id="addcomment" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="editcomment" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="downloadcomment" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean"/>
<jsp:useBean id="detail" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="comment" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="download" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="upload" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="segmentComments" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="jobCommentList" scope="request"
 class="java.util.ArrayList" />
<jsp:useBean id="taskCommentList" scope="request"
 class="java.util.ArrayList" />
<jsp:useBean id="segmentCommentList" scope="request"
 class="java.util.ArrayList" />
 <jsp:useBean id="wordcountList" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<%
    ResourceBundle bundle = PageHandler.getBundle(session);
    SessionManager sessionMgr =
      (SessionManager)session.getAttribute(WebAppConstants.SESSION_MANAGER);
    PermissionSet perms =
      (PermissionSet) session.getAttribute(WebAppConstants.PERMISSIONS);
	//Get task info
    Task task = (Task)TaskHelper.retrieveObject(
      session, WebAppConstants.WORK_OBJECT);
	Task theTask = task;
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
    String labelReportQAChecks = bundle.getString("lb_activity_qa_checks");
    boolean showQAChecksTab = QACheckerHelper.isShowQAChecksTab(theTask);
    boolean showDITAQAChecksTab = DITAQACheckerHelper.isShowDITAChecksTab(theTask);
    
    String downloadcommentUrl = downloadcomment.getPageURL() + "&action=downloadFiles"
								+ "&" + JobManagementHandler.JOB_ID
								+ "=" + task.getJobId()+
								//GBS 2913 add task id
								"&" + WebAppConstants.TASK_ID+
								"=" + task.getId();
    
    String addcommentUrl = addcomment.getPageURL() + "&action=addcomment"+
			    				//GBS 2913 add task id and state
								"&"+WebAppConstants.TASK_ID+
								"="+theTask.getId()+
								"&"+WebAppConstants.TASK_STATE+
								"="+theTask.getState()+
								"&toTask=ture";
    String editcommentUrl = editcomment.getPageURL() + "&action=editcomment"+
				    		//GBS 2913 add task id and state
				    		"&"+WebAppConstants.TASK_ID+
							"="+theTask.getId()+
							"&"+WebAppConstants.TASK_STATE+
							"="+theTask.getState()+
							"&toTask=ture";
	    
    boolean review_only = task.isType(Task.TYPE_REVIEW);
    
    String pageId = (String)TaskHelper.retrieveObject(
      session, WebAppConstants.TASK_DETAILPAGE_ID);

    int state = task.getState();
    long task_id = task.getId();
    //Labels of the page

    //Urls of the links on this page
    String commentUrl = comment.getPageURL()+
    		//GBS 2913 add taskId and state	
            "&" + WebAppConstants.TASK_STATE +
            "=" + state +
            "&" + WebAppConstants.TASK_ID +
            "=" + task_id;
    String downloadUrl = download.getPageURL()+
    		//GBS 2913 add taskId and state	
            "&" + WebAppConstants.TASK_STATE +
            "=" + state +
            "&" + WebAppConstants.TASK_ID +
            "=" + task_id;
    String uploadUrl = upload.getPageURL();
    String segcommentsUrl = segmentComments.getPageURL();

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
    
	String downloadReportUrl = downloadreport.getPageURL()+
			//GBS 2913 add taskId and state	
            "&" + WebAppConstants.TASK_STATE +
            "=" + state +
            "&" + WebAppConstants.TASK_ID +
            "=" + task_id;
            
	String uploadReportUrl = uploadreport.getPageURL()
			    + "&" + WebAppConstants.TASK_ID
				+ "=" + task_id
				+ "&" + WebAppConstants.TASK_STATE +
				"=" + state;

    String downloadQAReportUrl = downloadQAReport.getPageURL()
            + "&" + WebAppConstants.TASK_ID + "=" + theTask.getId()
            + "&" + WebAppConstants.TASK_STATE + "=" + theTask.getState();

    String uploadQAReportUrl = uploadQAReport.getPageURL()
            + "&" + WebAppConstants.TASK_ID + "=" + theTask.getId()
            + "&" + WebAppConstants.TASK_STATE + "=" + theTask.getState();

    String downloadDitaReportUrl = downloadDitaReport.getPageURL()
            + "&" + WebAppConstants.TASK_ID + "=" + task_id
            + "&" + WebAppConstants.TASK_STATE + "=" + state;

    String uploadDitaReportUrl = uploadDitaReport.getPageURL()
            + "&" + WebAppConstants.TASK_ID + "=" + task_id
            + "&" + WebAppConstants.TASK_STATE + "=" + state;

    boolean alreadyAccepted = false;
    boolean disableButtons = false;
    boolean isPageDetailOne = TaskHelper.DETAIL_PAGE_1.equals(pageId)? true:false;;

    //Non-null value for a project manager
    Boolean assigneeValue = (Boolean)TaskHelper.retrieveObject(
      session, WebAppConstants.IS_ASSIGNEE);
    boolean isAssignee = assigneeValue == null ? true :
      assigneeValue.booleanValue();
    boolean enableComment = state != Task.STATE_FINISHING && (!isAssignee || (isAssignee && state == Task.STATE_ACCEPTED));

    if (!isAssignee)
    {
      disableButtons = true;
    }

    String title= bundle.getString("lb_comments");

    // Labels of the column titles
    String creatorCol = bundle.getString("lb_comment_creator");
    String dateCol = bundle.getString("lb_date_created");
    String commentsCol = bundle.getString("lb_comments");
    String attachmentCol = bundle.getString("lb_attached_files");

    String jobName = (String)sessionMgr.getAttribute("jobName");

    // Button names
    String newButton = bundle.getString("lb_new1");
    String editButton = bundle.getString("lb_edit1");

    String access = "";
    if (perms.getPermissionFor(Permission.COMMENT_ACCESS_RESTRICTED))
    {
        access = WebAppConstants.COMMENT_REFERENCE_RESTRICTED_ACCESS;
    }
    else
    {
        access = WebAppConstants.COMMENT_REFERENCE_GENERAL_ACCESS;
    }

    // get date/time format
    // NOTE: The system4 standard is to **not** format date and time according
    // to the UILOCALE as in (Locale)session.getAttribute(WebAppConstants.UILOCALE)
    NumberFormat numberFormat = NumberFormat.getInstance(Locale.US);
    numberFormat.setMaximumFractionDigits(1);

    // user info
    User user = (User)sessionMgr.getAttribute(WebAppConstants.USER);
    String pmName = user.getUserName();

    ArrayList segmentTargLocales = new ArrayList();
    // For segment comments table
    segmentTargLocales.add((Locale)task.getTargetLocale().getLocale());
    String segmentSelectedLocale =
      task.getTargetLocale().getLocale().getDisplayName();
    String path = "";
    
%>
<%
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
	String activityName = theTask.getTaskDisplayName();
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
    String labelSave = bundle.getString("lb_save");
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
    tmpUrl.append("&").append(WebAppConstants.TASK_ID).append("=").append(task_id).append("&action=getAvailableJobsForTask");
    String updateLeverageUrl = tmpUrl.toString();
    String taskListStartStr = String.valueOf(session.getAttribute("taskListStart"));
    String previousUrl = previous.getPageURL() + "&taskStatus=" + state + "&taskId=" + task_id;
    if (taskListStartStr != null)
    {
        previousUrl += "&taskListStart=" + taskListStartStr;
    }
    String acceptUrl = accept.getPageURL() + "&" + WebAppConstants.TASK_ACTION +
        				"=" + WebAppConstants.TASK_ACTION_ACCEPT+
        				//GBS 2913 add taskId and state
        				"&" + WebAppConstants.TASK_ID+
        				"=" + task_id;
        				
    String rejectUrl = reject.getPageURL()+
    		//GBS 2913 add taskId and state
			"&" + WebAppConstants.TASK_ID+
			"=" + task_id+
			"&" + WebAppConstants.TASK_STATE+
			"=" + state;
    String finishUrl = finish.getPageURL() + "&" + WebAppConstants.TASK_ACTION +
				        "=" + WebAppConstants.TASK_ACTION_FINISH+
				      	//GBS 2913 add taskId and state
						"&" + WebAppConstants.TASK_ID+
						"=" + task_id+
						"&" + WebAppConstants.TASK_STATE+
						"=" + state;
    
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
    //GBS 2913 add taskId and state
    downloadLink.append("&");
    downloadLink.append(WebAppConstants.TASK_ID);
    downloadLink.append("=");
    downloadLink.append(task_id);
    downloadLink.append("&");
    downloadLink.append(WebAppConstants.TASK_STATE);
    downloadLink.append("=");
    downloadLink.append(state);
    
    
    final int stateAvailable = Task.STATE_ACTIVE;
    final int stateInProgress = Task.STATE_ACCEPTED;

	//Labels of the page
    String labelActivity = bundle.getString("lb_activity") + bundle.getString("lb_colon");
    String labelCompany = bundle.getString("lb_company") + bundle.getString("lb_colon");
    String labelJob =  bundle.getString("lb_job") + bundle.getString("lb_colon");
    String labelJobName =  bundle.getString("lb_job_name") + bundle.getString("lb_colon");
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
    String labelFinishWarning = bundle.getString("jsmsg_my_activities_finished");

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
    exportLink.append(task_id);
    exportLink.append("&");
    exportLink.append(WebAppConstants.TASK_STATE);
    exportLink.append("=");
    exportLink.append(state);
    
    long startExportTime = 0;
    String userId = user.getUserId();
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
<HTML>
<!-- This is envoy\administration\comment\taskComments.jsp -->
<HEAD>
<TITLE><%= title %></TITLE>
<style>
.comment {
  position: absolute;
  visibility: hidden;
  width: 400px;
  background-color: lightgrey;
  layer-background-color: lightgrey;
  border: 2px outset white;
}
</style>
<script type="text/javascript" src="/globalsight/jquery/jquery-1.6.4.min.js"></script>
<script type="text/javascript" src="/globalsight/jquery/jquery-ui-1.8.18.custom.min.js"></script>
<SCRIPT SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>

<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>
<SCRIPT LANGUAGE="JavaScript">
var taskId = <%=task_id%>;
var needWarning = false;
var objectName = "";
var guideNode = null;
var helpFile = "<%=bundle.getString("help_activity_comments")%>";

function showComment(id)
{
    elem = document.getElementById(id);
    elem.style.visibility = "visible";
}

function closeComment(id)
{
    elem = document.getElementById(id);
    elem.style.visibility = "hidden";
}

function doCheckAll(checkboxName)
{
	for (var i = 0; i < CommentForm.length; i++)
    {
        if (CommentForm.elements[i].type == "checkbox" &&
            CommentForm.elements[i].name == checkboxName)
        {
            CommentForm.elements[i].checked = true;
        }
    }
}

function doClearAll(checkboxName)
{
	for (var i = 0; i < CommentForm.length; i++)
    {
        if (CommentForm.elements[i].type == "checkbox" &&
            CommentForm.elements[i].name == checkboxName)
        {
            CommentForm.elements[i].checked = false;
        }
    }
	
}

function enableButtons(buttonID)
{
	var button = document.getElementById(buttonID);

	if(button!=null)
	{
		button.disabled = false;
	}
}

function submitFormForJob(selectedButton)
{
    var radio = null;
    if (CommentForm.jobradioBtn != null)
    {
        radio = getSelectedRadio(CommentForm.jobradioBtn);
        
    }
    // otherwise do the following
    if (selectedButton == 'New')
    {
    	CommentForm.saveCommStatus.value = "saveJobCommentFromActivity";
        CommentForm.action = "/globalsight/ControlServlet?linkName=addcomment&pageName=JOBCOMMENTS&action=addcomment&toTask=ture&taskId="+<%=task_id%>+"&state="+<%= state%>;
        CommentForm.submit();
        return;
    }

    if (radio == -1)
    {
        alert("<%= bundle.getString("jsmsg_select_comment") %>");
        return false;
    }    
    
    CommentForm.action = "/globalsight/ControlServlet?linkName=editcomment&pageName=JOBCOMMENTS&action=editcomment&toTask=ture&taskId="+<%=task_id%>+"&state="+<%= state%>;
    CommentForm.submit();
}

function submitForm(selectedButton)
{
    var radio = null;
    if (CommentForm.radioBtn != null)
    {
        radio = getSelectedRadio(CommentForm.radioBtn);
    }
    // otherwise do the following
    if (selectedButton == 'New')
    {
        CommentForm.action = "<%=addcommentUrl%>";
        CommentForm.submit();
        return;
    }
    if (selectedButton == 'DownloadFiles')
    {
   		if( hasSelectCheckBox() )
    	{
	    	CommentForm.action = "<%=downloadcommentUrl%>";
	    	CommentForm.submit();
	    	return;
    	}
    	else
    	{
    		alert("<%= bundle.getString("jsmsg_select_comment_file") %>");
    		return;
    	}
    }
    else if (radio == -1)
    {
        alert("<%= bundle.getString("jsmsg_select_comment") %>");
        return false;
    }
    else if (radio == null)
    {
        // I'm sure the user knows what's going on without alert.
        return false;
    }
    CommentForm.action = "<%=editcommentUrl%>";
    CommentForm.submit();
}

function hasSelectCheckBox()
{
	for(var i = 0; i < CommentForm.length; i++)
	{
		var e = CommentForm.elements[i];
		if(e.type == 'checkbox' &&
			(e.name == 'checkboxBtn' || e.name == 'ActivityCheckboxBtn') &&
			e.checked)
		{
			return true;
		}
	}
	return false;
}

function getSelectedRadio(buttonGroup)
{
   // returns the array number of the selected radio button or -1
   // if no button is selected
   if (buttonGroup[0])
   {
      // if the button group is an array (one button is not an array)
      for (var i = 0; i < buttonGroup.length; i++)
      {
         if (buttonGroup[i].checked)
         {
            return i;
         }
      }
   }
   else
   {
      // if the one button is checked, return zero
      if (buttonGroup.checked) { return 0; } 
   }
   // if we get to this point, no radio button is selected
   return -1;
}

//for GBS-2599
function handleMultiSelectAll_1() {
	if (CommentForm) {
		if (CommentForm.multiSelectAll_1.checked) {
			doCheckAll('checkboxBtn');
	    }
	    else {
			doClearAll('checkboxBtn');
	    }
	}
}
function handleMultiSelectAll_2() {
	if (CommentForm) {
		if (CommentForm.multiSelectAll_2.checked) {
			doCheckAll('ActivityCheckboxBtn');
	    }
	    else {
			doClearAll('ActivityCheckboxBtn');
	    }
	}
}
</SCRIPT>
<style type="text/css">
.list {
    border: 1px solid <%=skinbean.getProperty("skin.list.borderColor")%>;
}
</style>
</HEAD>
<BODY LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0" MARGINWIDTH="0"
 MARGINHEIGHT="0" onload="loadGuides()">
<link rel="STYLESHEET" type="text/css" href="/globalsight/includes/ContextMenu.css">
<link rel="stylesheet" type="text/css" href="/globalsight/jquery/jQueryUI.redmond.css"/>
<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>
<DIV ID="contentLayer" STYLE=" POSITION: ABSOLUTE; Z-INDEX: 9; TOP: 108; LEFT: 20px; RIGHT: 20px;">
<!-- Tabs table -->
<TABLE CELLPADDING="0" CELLSPACING="0" BORDER="0">
  <TR>
    <td><%@ include file="/envoy/tasks/includeTaskSummaryTabs.jspIncl" %></td>
  </TR>
</TABLE>
<!-- End Tabs table -->

<form name="CommentForm" method="post">
<input type="hidden" name="saveCommStatus">
<!-- Job Comments data table -->
<br>
<amb:permission name="<%=Permission.ACTIVITIES_JOB_COMMENTS_VIEW%>"> 	  
<table cellpadding=0 cellspacing=0 border=0 class="standardText" width="80%" style="min-width:1024px;">
  <tr>
    <td>
      <b><%=bundle.getString("lb_job")%> <%=bundle.getString("lb_comments")%></b>
    </td>
  </tr>

  <tr valign="top">
    <td align="right">
      <amb:tableNav bean="jobCommentList"
      key="<%=CommentConstants.JOB_COMMENT_KEY%>" pageUrl="comment" />
    </td>
  </tr>
  <tr>
    <td>
      <amb:table bean="jobCommentList" id="jobComment"
      key="<%=CommentConstants.JOB_COMMENT_KEY%>"
      dataClass="com.globalsight.everest.comment.Comment" pageUrl="comment"
      emptyTableMsg="msg_comments_none_for_job" >
            <amb:column label="" width="15px">
              <amb:permission name="<%=Permission.ACTIVITIES_JOB_COMMENTS_EDIT%>" >
                <input type="radio" name="jobradioBtn" value="<%=jobComment.getId()%>" 
               	   	  onclick="enableButtons('jobCommEditBtn')">
              </amb:permission>
            </amb:column>
            <amb:column label="lb_comment_creator" sortBy="<%=CommentComparator.CREATOR%>" width="10%">
                <%=UserUtil.getUserNameById(jobComment.getCreatorId())%>
            </amb:column>
            <amb:column label="lb_date_created" sortBy="<%=CommentComparator.DATE%>" width="15%">
                <%=jobComment.getCreatedDate()%>
            </amb:column>
            <amb:column label="lb_comments" width="45%" style="word-wrap:break-word;word-break:break-all">
            <div style="width:45%">
                <%
                    String com = jobComment.getComment();
                    if (com.length() > 200)
                    {
                        int idx = com.indexOf(' ', 200);
                        if (idx > 0)
                            com = com.substring(0, idx);
                     %>
                        <%= com %>
                        <div onclick="javascript:showComment('j<%= jobComment.getId()%>');" style="cursor:hand">[more...]</div>
                        <div id=j<%=jobComment.getId()%> class="comment"><%= jobComment.getComment()%><div onclick="closeComment('j<%= jobComment.getId()%>');"><span style="cursor: hand; color:blue">[Close]</span></div></div>
                    <%} else {
	                    out.println(jobComment.getComment());
                    } %>
            </div>
            </amb:column>
			<amb:column label="multiCheckbox_1" align="right" width="5px"></amb:column>
            <amb:column label="lb_attached_files" width="30%" style="word-wrap:break-word;word-break:break-all">
            <%
                 String commentId = (new Long(jobComment.getId())).toString();
                 ArrayList commentReferences = null;
                 CommentManager mgr = null;
                 try
                 {
                     mgr = ServerProxy.getCommentManager();
                     commentReferences = mgr.getCommentReferences(commentId , access, true);
                 }
                 catch(Exception  e)
                 {
                     System.out.println("JobComments.jsp::Error getting Comment References");
                 }
                 if (commentReferences != null)
                 {
                	if(commentReferences.iterator().hasNext()){%>
                		
                	<%
                	}

                    for (Iterator it = commentReferences.iterator(); it.hasNext();)
                    {
                        CommentFile file = (CommentFile)it.next();
                        // round size to nearest 1024bytes (1k) - like win-explorer.
                        // adjust for empty file
                        long filesize = file.getFileSize() < 3 ? 0 : file.getFileSize();
                        if(filesize != 0)
                        {
                            filesize = (filesize%1024!=0) ?
                                 ((filesize/1024)+1)/*round up*/ : filesize/1024;
                        }

%>						
                        <div style="width:100%">
      					<amb:permission name="<%=Permission.ACTIVITIES_JOB_COMMENTS_DOWNLOAD%>" >
                        	<input type="checkbox" id="<%=commentId%>" name="checkboxBtn" value="<%=commentId + ":" + file.getFileAccess() + ":" + file.getFilename()%>">
                        </amb:permission>

                        <IMG SRC="/globalsight/images/file_paperclip.gif" ALT="<%=bundle.getString("lb_reference_file")%>" HEIGHT=15 WIDTH=13>
						<%
						path = "/globalsight/".concat(AmbFileStoragePathUtils.COMMENT_REFERENCE_SUB_DIR).concat(File.separator).concat(commentId);
						path += File.separator.concat(file.getFileAccess()).concat(File.separator).concat(file.getFilename());
						path = URLEncoder.encodeUrlStr(path);
						%>
                        <A class="standardHREF" target="_blank" href="<%=path %>">
                        <%=EditUtil.encodeHtmlEntities(file.getFilename())%>
                        </A>
                        <SPAN CLASS=smallText>
                        <%=numberFormat.format(filesize)%>k
<%
                        if (file.getFileAccess().equals("Restricted"))
                        {
%>
                            <SPAN STYLE="color: red">
                                (<%=bundle.getString("lb_restricted")%>)&nbsp;
                            </SPAN>
<%
                        }
%>
                        </SPAN>
						</div>
                        <br>
<%
                    }
                  }
%>
        </amb:column>    
      </amb:table>
    </td>
  </tr>
  <tr>
  	 <td align="right" style="padding-top:6px">
		 <%--for gbs-2599
		 amb:permission name="<%=Permission.ACTIVITIES_JOB_COMMENTS_DOWNLOAD%>" >
		          <A CLASS="standardHREF" HREF="#"
		              onClick="doCheckAll('checkboxBtn'); return false;"
		              onFocus="this.blur();">CheckAll</A> |
			  	  <A CLASS="standardHREF" HREF="#"
		              onClick="doClearAll('checkboxBtn'); return false;"
		              onFocus="this.blur();">ClearAll</A>
		 </amb:permission--%>
<%
		 if (state !=  Task.STATE_FINISHING) {
%>
    <amb:permission name="<%=Permission.ACTIVITIES_JOB_COMMENTS_EDIT%>" >
            <INPUT TYPE="BUTTON" id="jobCommEditBtn" VALUE="<%=editButton%>" disabled onClick="submitFormForJob('Edit');">
         </amb:permission>
         <amb:permission name="<%=Permission.ACTIVITIES_JOB_COMMENTS_NEW%>" >
            <INPUT TYPE="BUTTON" VALUE="<%=newButton%>" onClick="submitFormForJob('New');">
         </amb:permission>
<% } %>
	 </td>
  </tr>
</table>
<p>
</amb:permission>

<!-- Task Comments data table -->
<table cellpadding=0 cellspacing=0 border=0 class="standardText" width="80%" style="min-width:1024px;">
  <tr>
    <td>
      <b><%=bundle.getString("lb_activity")%> <%=bundle.getString("lb_comments")%>: </b><%=task.getTaskDisplayName()%>
    </td>
  </tr>
  <tr>
    <td><%=task.getTargetLocale().getDisplayName()%></td>
  </tr>
  <tr valign="top">
    <td align="right">
      <amb:tableNav bean="taskCommentList" key="<%=CommentConstants.TASK_COMMENT_KEY%>" pageUrl="comment" />
    </td>
  </tr>
  <tr>
    <td>
      <amb:table bean="taskCommentList" id="commentObj"
      key="<%=CommentConstants.TASK_COMMENT_KEY%>"
      dataClass="com.globalsight.everest.comment.Comment" pageUrl="comment"
      emptyTableMsg="msg_comments_none_for_activity" >
            <amb:column label="" width="15px">
               <input type="radio" name="radioBtn" value="<%=commentObj.getId()%>">
            </amb:column>
            <amb:column label="lb_comment_creator" sortBy="<%=CommentComparator.CREATOR%>" width="10%">
                <%=UserUtil.getUserNameById(commentObj.getCreatorId())%>
            </amb:column>
            <amb:column label="lb_date_created" sortBy="<%=CommentComparator.DATE%>" width="15%">
                <%=commentObj.getCreatedDate()%>
            </amb:column>
            <amb:column label="lb_comments" width="45%" style="word-wrap:break-word;word-break:break-all">
            <div style="width:45%">
                <%
                    String com = commentObj.getComment();
                    if (com.length() > 200)
                    {
                        int idx = com.indexOf(' ', 200);
                        if (idx > 0)
                            com = com.substring(0, idx);
                        %>
                        <%= com %>
                        <div onclick="javascript:showComment('t<%= commentObj.getId() %>');" style="cursor:hand">[more...]</div>
                        <div id=t<%=commentObj.getId()%> class="comment"><%= commentObj.getComment() %><div onclick="closeComment('t<%= commentObj.getId() %>');"><span style="cursor: hand; color:blue">[Close]</span></div></div>
                    <%}
                    else
                        out.println(commentObj.getComment());
                %>
            </div>
            </amb:column>
			<amb:column label="multiCheckbox_2" align="right" width="5px"></amb:column>
            <amb:column label="lb_attached_files" width="30%" style="word-wrap:break-word;word-break:break-all">

<%
                 String commentId = (new Long(commentObj.getId())).toString();
                 ArrayList commentReferences = null;
                 CommentManager mgr = null;
                 try
                 {
                     mgr = ServerProxy.getCommentManager();
                     commentReferences = mgr.getCommentReferences(commentId , access, true);
                 }
                 catch(Exception  e)
                 {
                     System.out.println("JobComments.jsp::Error getting Comment References");
                 }

                 if (commentReferences != null)
                 {
                 	if(commentReferences.iterator().hasNext()){%>
	            		
	            	<%
	            	}

                    for (Iterator it = commentReferences.iterator(); it.hasNext();)
                    {
                        CommentFile file = (CommentFile)it.next();
                        // round size to nearest 1024bytes (1k) - like win-explorer.
                        // adjust for empty file
                        long filesize = file.getFileSize() < 3 ? 0 : file.getFileSize();
                        if(filesize != 0)
                        {
                            filesize = (filesize%1024!=0) ?
                                 ((filesize/1024)+1)/*round up*/ : filesize/1024;
                        }

%>
                        <div style="width:100%">
						<amb:permission name="<%=Permission.ACTIVITIES_COMMENTS_DOWNLOAD%>" >
                        	<input type="checkbox" id="<%=commentId%>" name="ActivityCheckboxBtn" value="<%=commentId + ":" + file.getFileAccess() + ":" + file.getFilename()%>">
                        </amb:permission>
                        <IMG SRC="/globalsight/images/file_paperclip.gif" ALT="<%=bundle.getString("lb_reference_file")%>" HEIGHT=15 WIDTH=13>

<%
path = "/globalsight/".concat(AmbFileStoragePathUtils.COMMENT_REFERENCE_SUB_DIR).concat(File.separator).concat(commentId);
path += File.separator.concat(file.getFileAccess()).concat(File.separator).concat(file.getFilename());
path = URLEncoder.encodeUrlStr(path);
%>
                        <A class="standardHREF" target="_blank" href="<%=path %>">
                        <%=EditUtil.encodeHtmlEntities(file.getFilename())%>
                        </A>
                        <SPAN CLASS=smallText>
                        <%=numberFormat.format(filesize)%>k
<%
                        if (file.getFileAccess().equals("Restricted"))
                        {
%>
                            <SPAN STYLE="color: red">
                                (<%=bundle.getString("lb_restricted")%>)&nbsp;
                            </SPAN>
<%
                        }
%>
                        </SPAN>
						</div>
                        <br>
<%
                    }
                  }
%>
        </amb:column>
      </amb:table>
      <!-- End Data Table -->
    </td>
  </tr>
  <TR>
    <TD align="right" style="padding-top:6px">
      <P>
      <%--for gbs-2599
	  amb:permission name="<%=Permission.ACTIVITIES_COMMENTS_DOWNLOAD%>" >
	      <A CLASS="standardHREF" HREF="#"
	              onClick="doCheckAll('ActivityCheckboxBtn'); return false;"
	              onFocus="this.blur();">CheckAll</A> |
		  <A CLASS="standardHREF" HREF="#"
	              onClick="doClearAll('ActivityCheckboxBtn'); return false;"
	              onFocus="this.blur();">ClearAll</A>
      </amb:permission--%>
      <%if (enableComment){%>
      <amb:permission name="<%=Permission.ACTIVITIES_COMMENTS_EDIT%>" >
      <INPUT TYPE="BUTTON" VALUE="<%=editButton%>" onClick="submitForm('Edit');" <%= (taskCommentList==null||taskCommentList.size()==0)?"DISABLED":""%>>
      </amb:permission>
      <amb:permission name="<%=Permission.ACTIVITIES_COMMENTS_NEW%>" >
      <INPUT TYPE="BUTTON" VALUE="<%=newButton%>" onClick="submitForm('New');">
      </amb:permission>
      <amb:permission name="<%=Permission.ACTIVITIES_COMMENTS_DOWNLOAD%>" >
      <input type="Button" value="Download Files" onClick="submitForm('DownloadFiles');"/>
      </amb:permission>
      <%}%>
    </TD>
  </TR>
</TABLE>
<P>
<TABLE width="80%" style="min-width:1024px;">
  <TR>
    <TD id="test">
      <!-- Segment Comments data table -->
      <%@ include file="/envoy/administration/comment/taskSegmentTable.jspIncl" %>
    </TD>
  </TR>
</TABLE>
</FORM>
</BODY>
</HTML>
<SCRIPT LANGUAGE = "JavaScript">
$(document).ready(function(){
	$("#taskCommentsTab").removeClass("tableHeadingListOff");
	$("#taskCommentsTab").addClass("tableHeadingListOn");
	$("#taskCommentsTab img:first").attr("src","/globalsight/images/tab_left_blue.gif");
	$("#taskCommentsTab img:last").attr("src","/globalsight/images/tab_right_blue.gif");
})
</SCRIPT>