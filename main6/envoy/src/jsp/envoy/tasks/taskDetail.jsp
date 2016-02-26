<%@ taglib uri="/WEB-INF/tlds/globalsight.tld" prefix="amb" %>
<%@ page
    contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/activityError.jsp"
    import="
      com.globalsight.config.UserParamNames,      
      com.globalsight.config.UserParameter,
      com.globalsight.cxe.entity.fileprofile.FileProfile,
      com.globalsight.cxe.entity.fileprofile.FileProfileUtil,
      com.globalsight.everest.comment.CommentFile,      
      com.globalsight.everest.comment.CommentManager,
      com.globalsight.everest.company.CompanyThreadLocal,
      com.globalsight.everest.company.CompanyWrapper,
      com.globalsight.everest.costing.AmountOfWork,
      com.globalsight.everest.costing.Rate,
      com.globalsight.everest.foundation.Timestamp,
      com.globalsight.everest.foundation.User,
      com.globalsight.everest.jobhandler.Job,
      com.globalsight.everest.page.PageWordCounts,
      com.globalsight.everest.page.PrimaryFile,
      com.globalsight.everest.page.SourcePage,
      com.globalsight.everest.page.TargetPage,
      com.globalsight.everest.page.UnextractedFile,
      com.globalsight.everest.page.pageexport.ExportHelper,
      com.globalsight.everest.permission.Permission,
      com.globalsight.everest.permission.PermissionSet,
      com.globalsight.everest.secondarytargetfile.SecondaryTargetFile,
      com.globalsight.everest.servlet.util.ServerProxy,
      com.globalsight.everest.servlet.util.SessionManager,
      com.globalsight.everest.taskmanager.Task,
      com.globalsight.everest.taskmanager.TaskImpl,
      com.globalsight.everest.util.system.SystemConfigParamNames,
      com.globalsight.everest.util.system.SystemConfiguration,
      com.globalsight.everest.webapp.WebAppConstants,
      com.globalsight.everest.webapp.javabean.NavigationBean,
      com.globalsight.everest.webapp.pagehandler.PageHandler,
      com.globalsight.everest.projecthandler.ProjectImpl,
      com.globalsight.everest.workflowmanager.WorkflowImpl,
      com.globalsight.everest.webapp.pagehandler.administration.comment.CommentConstants,
      com.globalsight.everest.webapp.pagehandler.administration.customer.download.DownloadFileHandler,
      com.globalsight.everest.webapp.pagehandler.administration.users.UserHandlerHelper,
      com.globalsight.everest.webapp.pagehandler.offline.OfflineConstants,
      com.globalsight.everest.webapp.pagehandler.projects.workflows.JobManagementHandler,
      com.globalsight.everest.webapp.pagehandler.projects.workflows.JobSearchConstants,
      com.globalsight.everest.webapp.pagehandler.projects.workflows.PageComparator,
      com.globalsight.everest.webapp.pagehandler.projects.workflows.WorkflowHandlerHelper,
      com.globalsight.everest.webapp.pagehandler.tasks.TaskDetailHandler,
      com.globalsight.everest.webapp.pagehandler.tasks.TaskDetailHelper,
      com.globalsight.everest.webapp.pagehandler.tasks.TaskHelper,
      com.globalsight.everest.workflow.Activity,
      com.globalsight.everest.workflow.ConditionNodeTargetInfo,
      com.globalsight.everest.workflowmanager.Workflow,
      com.globalsight.everest.workflowmanager.WorkflowManagerLocal,
      com.globalsight.everest.qachecks.QACheckerHelper,
      com.globalsight.everest.qachecks.DITAQACheckerHelper,
      com.globalsight.ling.common.URLEncoder,
      com.globalsight.util.AmbFileStoragePathUtils,
      com.globalsight.util.date.DateHelper,
      com.globalsight.util.edit.EditUtil,
      com.globalsight.util.StringUtil,
      com.globalsight.everest.webapp.pagehandler.edit.inctxrv.pdf.PreviewPDFHelper,
      java.util.*,
      java.lang.StringBuffer,
      javax.servlet.jsp.JspWriter,
      java.text.MessageFormat,
      java.text.NumberFormat,
      java.util.Locale,
      java.io.File,
      java.io.IOException"
    session="true"
%>
<jsp:useBean id="detail" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="taskSecondaryTargetFiles" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="taskScorecard" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="accept" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="export" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="reject" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="comment" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="previous" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="editor" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="newEditor" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="incontextreiview" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="editorSameWindow" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="finish" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="download" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="upload" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="queryOpenIssuesXml" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="wordcountList" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="pageList" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="dtpDownload" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="dtpUpload" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="addcomment" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="editcomment" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
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
<jsp:useBean id="taskCommentList" scope="request" class="java.util.ArrayList" />
<jsp:useBean id="updateLeverage" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
 <jsp:useBean id="pageSearch" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
  <jsp:useBean id="searchText" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<%
    String thisFileSearch = (String) request.getAttribute(JobManagementHandler.PAGE_SEARCH_PARAM);
    if (thisFileSearch == null)
    {
       thisFileSearch = "";
    }
    String thisFileSearchText = (String) request
			.getAttribute(JobManagementHandler.PAGE_SEARCH_TEXT);
	if (thisFileSearchText == null)
	{
		thisFileSearchText = "";
	}
	String thisSearchLocale = (String) request
			.getAttribute(JobManagementHandler.PAGE_SEARCH_LOCALE);
	if (thisSearchLocale == null)
	{
		thisSearchLocale = "sourceLocale";
	}
    ResourceBundle bundle = PageHandler.getBundle(session);
    SessionManager sessionMgr =
      (SessionManager)session.getAttribute(WebAppConstants.SESSION_MANAGER);
 

    String subTitle = bundle.getString("lb_my_activities") + bundle.getString("lb_colon");
    String title= bundle.getString("lb_TargetFiles");
    
    String addcommentUrl = addcomment.getPageURL() + "&action=addcomment";
    String editcommentUrl = editcomment.getPageURL() + "&action=editcomment";

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
    String labelAcceptBy = bundle.getString("lb_accept_by") + bundle.getString("lb_colon");
    String labelDueDate = bundle.getString("lb_due_date") + bundle.getString("lb_colon");
    String labelCompletedOn = bundle.getString("lb_completed_on") + bundle.getString("lb_colon");
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

    String labelAccept = bundle.getString("lb_accept");
    String labelUpdateLeverage = bundle.getString("lb_update_leverage");
    String labelReject = bundle.getString("lb_reject");
    String labeltTaskCompleted = bundle.getString("lb_taskcompleted");
    String labelAvailable = bundle.getString("lb_available");
    String labelFinished = bundle.getString("lb_finished");
    String labelRejected = bundle.getString("lb_rejected");
    String labelAccepted = bundle.getString("lb_accepted");
    String labelFinishing = bundle.getString("lb_finishing");
    String labelCreateStf = bundle.getString("lb_cstfs");

    String labelYes = bundle.getString("lb_yes");
    String labelNo = bundle.getString("lb_no");
    String labelSave = bundle.getString("lb_save");
    String labelWordCounts = bundle.getString("lb_detailed_word_counts");

    String labelEditorWarning = bundle.getString("lb_editor_warning");
    String labelFinishWarning = bundle.getString("jsmsg_my_activities_finished");
    String labelSelectionWarning = bundle.getString("jsmsg_my_activities_Warning");
    String labelReportUploadCheckWarning = "Translation Edit Report not uploaded";
    String labelReportUploadCheckWarningMessage = bundle.getString("jsmsg_my_activities_translation_edit_report_upload_check");
    List<String> trgPageIdBatches = new ArrayList<String>();
    int translatedTextCount = 10;
    //use to get the translated text
    StringBuffer tarPageIds = new StringBuffer();

    // used by the pageSearch include
    String lb_filter_text = bundle.getString("lb_target_file_filter");

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
    if(theTask.isType(Task.TYPE_REVIEW))
    {
    	labelReportUploadCheckWarning = "Reviewer Comments Report not uploaded";
    	labelReportUploadCheckWarningMessage = bundle.getString("jsmsg_my_activities_reviewer_comments_report_upload_check");
    }
   	boolean isCheckUnTranslatedSegments = project.isCheckUnTranslatedSegments();
    //Urls of the links on this page
    String acceptUrl = accept.getPageURL() + "&" + WebAppConstants.TASK_ACTION +
        "=" + WebAppConstants.TASK_ACTION_ACCEPT+
      	//GBS-2913 Added to the url parameter taskId
        "&"+WebAppConstants.TASK_ID+"="+theTask.getId();
    
    String labelReportQAChecks = bundle.getString("lb_activity_qa_checks");
    boolean showQAChecksTab = QACheckerHelper.isShowQAChecksTab(theTask);
    boolean showDITAQAChecksTab = DITAQACheckerHelper.isShowDITAChecksTab(theTask);

    StringBuffer tmpUrl = new StringBuffer(updateLeverage.getPageURL());
    tmpUrl.append("&").append(WebAppConstants.TASK_ID).append("=").append(theTask.getId()).append("&action=getAvailableJobsForTask");
    String updateLeverageUrl = tmpUrl.toString();
    
    String rejectUrl = reject.getPageURL()+
    		//GBS-2913 Added to the url parameter taskId,state
    		"&"+WebAppConstants.TASK_ID+"="+theTask.getId()+
   			 "&"+WebAppConstants.TASK_STATE+"="+theTask.getState();
    String pageSearchURL = pageSearch.getPageURL()+
					    		//GBS-2913 Added to the url parameter taskId,state
					    		"&"+WebAppConstants.TASK_ID+"="+theTask.getId()+
					   			 "&"+WebAppConstants.TASK_STATE+"="+theTask.getState();
    String wordCountUrl = wordcountList.getPageURL() + "&action=tpList"+
				    		//GBS-2913 Added to the url parameter taskId,state;
						    "&"+WebAppConstants.TASK_ID+
						    "="+theTask.getId()+
						    "&"+WebAppConstants.TASK_STATE+
						    "="+theTask.getState();
    	    
    String pageListUrl = pageList.getPageURL() + "&" + JobManagementHandler.PAGE_SEARCH_PARAM + "=" + thisFileSearch;
    String dtpDownloadURL = dtpDownload.getPageURL();
    String dtpUploadURL = dtpUpload.getPageURL();
    String editorParaUrl = editorSameWindow.getPageURL();
    String editorListUrl = editor.getPageURL();
    String editorReviewUrl = editor.getPageURL() +
       "&" + WebAppConstants.REVIEW_MODE + "=true";
    String newEditorListUrl = newEditor.getPageURL();
    String newEditorReviewUrl = newEditor.getPageURL() +
       "&" + WebAppConstants.REVIEW_MODE + "=true";
    String incontextreviewUrl = incontextreiview.getPageURL();
    String incontextreviewUrlRe = incontextreiview.getPageURL() +
            "&" + WebAppConstants.REVIEW_MODE + "=true";

    String createStfUrl = accept.getPageURL() + "&" + WebAppConstants.TASK_ACTION +
					    	//GBS-2913 Added to the url parameter taskId,state
					        "=" + WebAppConstants.TASK_ACTION_CREATE_STF+
					        "&"+WebAppConstants.TASK_ID+
					        "="+theTask.getId()+
					        "&"+WebAppConstants.TASK_STATE+
					        "="+theTask.getState();
        
    String finishUrl = finish.getPageURL() + "&" + WebAppConstants.TASK_ACTION +
				        "=" + WebAppConstants.TASK_ACTION_FINISH+
				      //GBS-2913 Added to the url parameter taskId,state
				        "&"+WebAppConstants.TASK_ID+
				        "="+theTask.getId()+
				        "&"+WebAppConstants.TASK_STATE+
				        "="+theTask.getState();

    String downloadUrl = download.getPageURL()
				    		//GBS 2913 add taskID and taskState
				    		+ "&" + WebAppConstants.TASK_ID + "=" + theTask.getId()
				    		+ "&" + WebAppConstants.TASK_STATE + "=" + theTask.getState();
    String uploadUrl = upload.getPageURL()
			    		//GBS 2913 add taskID and taskState
			    		+ "&" + WebAppConstants.TASK_ID
			    		+ "=" + theTask.getId()
			    		+ "&" + WebAppConstants.TASK_STATE +
			    		"=" + theTask.getState();
    
    String downloadReportUrl = downloadreport.getPageURL()
					    		//GBS 2913 add taskID and taskState
					    		+ "&" + WebAppConstants.TASK_ID
					    		+ "=" + theTask.getId()
					    		+ "&" + WebAppConstants.TASK_STATE +
					    		"=" + theTask.getState();
    
    String uploadReportUrl = uploadreport.getPageURL()
						    + "&" + WebAppConstants.TASK_ID
							+ "=" + theTask.getId()
							+ "&" + WebAppConstants.TASK_STATE +
							"=" + theTask.getState();

    String downloadQAReportUrl = downloadQAReport.getPageURL()
            + "&" + WebAppConstants.TASK_ID + "=" + theTask.getId()
            + "&" + WebAppConstants.TASK_STATE + "=" + theTask.getState();

    String uploadQAReportUrl = uploadQAReport.getPageURL()
            + "&" + WebAppConstants.TASK_ID + "=" + theTask.getId()
            + "&" + WebAppConstants.TASK_STATE + "=" + theTask.getState();

    String downloadDitaReportUrl = downloadDitaReport.getPageURL()
            + "&" + WebAppConstants.TASK_ID + "=" + theTask.getId()
            + "&" + WebAppConstants.TASK_STATE + "=" + theTask.getState();

    String uploadDitaReportUrl = uploadDitaReport.getPageURL()
            + "&" + WebAppConstants.TASK_ID + "=" + theTask.getId()
            + "&" + WebAppConstants.TASK_STATE + "=" + theTask.getState();

    String searchTextUrl = searchText.getPageURL()
				    		+ "&" + WebAppConstants.TASK_ID
				    		+ "=" + theTask.getId()
				    		+ "&" + WebAppConstants.TASK_STATE +
				    		"=" + theTask.getState();
    
    String dAbbr = bundle.getString("lb_abbreviation_day");
    String hAbbr = bundle.getString("lb_abbreviation_hour");
    String mAbbr = bundle.getString("lb_abbreviation_minute");

    // images to be displayed next to each page/file
    String extractedImage = bundle.getString("img_file_extracted");
    String extractedToolTip = bundle.getString("lb_file_extracted");
    String unExtractedImage = bundle.getString("img_file_unextracted");
    String unExtractedToolTip = bundle.getString("lb_file_unextracted");

    //  Label and its value decided based on the selected state
    String labelABorDBorCODate;
    String valueABorDBorCODate;

    //Get task info
    Locale uiLocale = (Locale)session.getAttribute(WebAppConstants.UILOCALE);
    TimeZone timeZone = (TimeZone)session.getAttribute(WebAppConstants.USER_TIME_ZONE);
    Timestamp ts = new Timestamp(timeZone);
    ts.setLocale(uiLocale);
    String pageId = (String)TaskHelper.retrieveObject(session, WebAppConstants.TASK_DETAILPAGE_ID);
    
    boolean review_only = theTask.isType(Task.TYPE_REVIEW);
     
    List targetPgs = (List)TaskHelper.retrieveObject(session, JobManagementHandler.TARGET_PAGES);

    // calculate size of table based on number of target pages
    int tableSize = 425;
    if (targetPgs.size() < 20)
        tableSize = targetPgs.size() * 20 + 40;
    
    //a param for passing to the openTaskIssues page
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

    String companyName = CompanyWrapper.getCompanyNameById(theTask.getCompanyId());
    CompanyThreadLocal.getInstance().setIdValue(theTask.getCompanyId());

    String stfCreationState = theTask.getStfCreationState();
    String sourceLocale = theTask.getSourceLocale().getDisplayName(uiLocale);
    String targetLocale = theTask.getTargetLocale().getDisplayName(uiLocale);
    ts.setDate(theTask.getEstimatedCompletionDate());
    String dueDate = ts.toString();
    ts.setDate(theTask.getCompletedDate());
    String completedOn = ts.toString();
    String duration = theTask.getTaskDurationAsString(dAbbr, hAbbr, mAbbr);
    ts.setDate(theTask.getEstimatedAcceptanceDate());
    String acceptBy = ts.toString();
    String projName = theTask.getProjectName();
    String projManager = theTask.getProjectManagerName();
    String priority = Integer.toString(theTask.getPriority());
    int state = theTask.getState();
    long task_id = theTask.getId();
    long workflowId = theTask.getWorkflow().getId();
    String task_type = theTask.getTaskType();
    String activityName = theTask.getTaskDisplayName();
    String jobName = theTask.getJobName();
    String jobId = Long.toString(theTask.getJobId());
    long jId = (new Long(jobId)).longValue();
    Job theJob = ServerProxy.getJobHandler().getJobById(jId);
    String sourceWordCount = (new Long(theJob.getWordCount())).toString();
    String locProfileName = theJob.getL10nProfile().getName();

	if (task_type.equals(Task.TYPE_DTP)) {
		tableSize = 360;
    	if (targetPgs.size() < 8)
        	tableSize = targetPgs.size() * 40 + 40;
	}
	
	String taskListStartStr = String.valueOf(session.getAttribute("taskListStart"));
    String previousUrl = previous.getPageURL() + "&taskStatus=" + state + "&taskId=" + task_id;
    if (taskListStartStr != null)
    {
        previousUrl += "&taskListStart=" + taskListStartStr;
    }
    
    String translatedTextUrl = detail.getPageURL() +
        "&" + WebAppConstants.TASK_ACTION +
        "=" + WebAppConstants.TASK_ACTION_TRANSLATED_TEXT_RETRIEVE;
   /* String approveTextUrl = detail.getPageURL().replaceAll("&", "#") +
            "#" + WebAppConstants.TASK_ACTION +
            "=" + WebAppConstants.TASK_ACTION_APPROVE_TUV;*/
   // treeLink.append("&ajaxUrl="+approveTextUrl.replaceAll("&", "#")+"&tfilePath=");
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
    
    String saveUrl = detail.getPageURL() +
        "&" + WebAppConstants.TASK_ACTION +
        "=" + WebAppConstants.TASK_ACTION_SAVEDETAILS +
        "&" + WebAppConstants.TASK_STATE +
        "=" + state +
        "&" + WebAppConstants.TASK_ID +
        "=" + task_id;

    String commentUrl = comment.getPageURL() +
        "&" + WebAppConstants.TASK_ACTION +
        "=" + WebAppConstants.TASK_ACTION_RETRIEVE +
        "&" + WebAppConstants.TASK_STATE +
        "=" + state +
        "&" + WebAppConstants.TASK_ID +
        "=" + task_id;
    boolean alreadyAccepted = false;
    String status = "";
    boolean disableButtons = false;
    boolean isPageDetailOne = TaskHelper.DETAIL_PAGE_1.equals(pageId) ? true:false;
    boolean isHourlyJobCosting = ((Boolean)TaskHelper.retrieveObject(session,
        TaskDetailHandler.TASK_HOURS_STATE)).booleanValue();
    String hoursParam = TaskDetailHandler.TASK_HOURS;

    boolean isPageBasedJobCosting = ((Boolean)TaskHelper.retrieveObject(session,
        TaskDetailHandler.TASK_PAGES_STATE)).booleanValue();
    String pagesParam = TaskDetailHandler.TASK_PAGES;

    String editorSelection = PageHandler.getUserParameter(
       session, UserParamNames.EDITOR_SELECTION).getValue();
    boolean editInSameWindow = editorSelection.equals(UserParamNames.EDITOR_INLINE);
    boolean canEditInSameWindow = true;
    boolean isReviewActivity = theTask.isType(Activity.TYPE_REVIEW);

    //save this in the user's session so it doesn't have to get passed around
    //but don't use SessionMgr so it will persist between web activities
    session.setAttribute(WebAppConstants.IS_LAST_ACTIVITY_REVIEW_ONLY,
                         new Boolean(isReviewActivity));

    if (isReviewActivity ||
        "false".equals(request.getAttribute(WebAppConstants.PARAGRAPH_EDITOR)))
    {
      canEditInSameWindow = false;
      editInSameWindow = false;
    }

    //define task states
    final int stateAvailable = Task.STATE_ACTIVE;
    final int stateInProgress = Task.STATE_ACCEPTED;
    int rowspan = 12;

    //  Majority of cases it is Due Date
    labelABorDBorCODate = labelDueDate;
    valueABorDBorCODate = dueDate;

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
            rowspan = 12;
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

    // Get the date so we can compare it and see
    // if the task is overdue
    Date dt = new Date();

	// Button names
    String newButton = bundle.getString("lb_new1");
    String editButton = bundle.getString("lb_edit1");
    
    PermissionSet perms = (PermissionSet) session.getAttribute(WebAppConstants.PERMISSIONS);
    String access = "";
    if (perms.getPermissionFor(Permission.COMMENT_ACCESS_RESTRICTED))
    {
        access = WebAppConstants.COMMENT_REFERENCE_RESTRICTED_ACCESS;
    }
    else
    {
        access = WebAppConstants.COMMENT_REFERENCE_GENERAL_ACCESS;
    }    
    
    NumberFormat numberFormat = NumberFormat.getInstance(Locale.US);
    numberFormat.setMaximumFractionDigits(1);
    
    // user info
    User user = (User)sessionMgr.getAttribute(WebAppConstants.USER);
    String pmName = user.getUserName();

    ArrayList segmentTargLocales = new ArrayList();
    // For segment comments table
    segmentTargLocales.add((Locale)theTask.getTargetLocale().getLocale());
    String segmentSelectedLocale =
      theTask.getTargetLocale().getLocale().getDisplayName();
        

    // Create the exportLink for the Export button
    StringBuffer exportLink = TaskDetailHelper.getExportLink(theTask, export.getPageURL(), workflowId);
  
    //Create the downloadLink for the download button
    String downloadUrl2 = "/globalsight/ControlServlet" +
            "?linkName=jobDownload&pageName=TK2" + 
            "&firstEntry=true&fromTaskDetail=true";
    StringBuffer downloadLink = TaskDetailHelper.getDownloadLink(theTask, downloadUrl2, workflowId, jobId);
    
    UserParameter param = PageHandler.getUserParameter(session, UserParamNames.PAGENAME_DISPLAY);
    String pagenameDisplay = param.getValue();

    String helpFile;
    if (state == Task.STATE_ACCEPTED)
    {
      helpFile = bundle.getString("help_inprogress_activity_details");
    }
    else
    {
      helpFile = bundle.getString("help_available_activity_details");
    }
    String openSegmentComments = (String)session.getAttribute(JobManagementHandler.OPEN_AND_QUERY_SEGMENT_COMMENTS);
    String closedSegmentComments = (String)session.getAttribute(JobManagementHandler.CLOSED_SEGMENT_COMMENTS);

    // Set which page to back to after drilling down into segment comments
    session.setAttribute("segmentCommentsBackPage","taskComments");

    long startTime = 0; 
    Hashtable delayTimeTable = (Hashtable)sessionMgr.getAttribute(WebAppConstants.TASK_COMPLETE_DELAY_TIME_TABLE);
    String userId = user.getUserId();
    if(delayTimeTable != null)
    {       
        String delayTimeKey = userId + task_id;
        Date startTimeObj = (Date)delayTimeTable.get(delayTimeKey);         
        if(startTimeObj != null)
        {
            startTime = startTimeObj.getTime();
        }
    }

    long startExportTime = 0;   
    Hashtable delayExportTimeTable = (Hashtable)sessionMgr.getAttribute(WebAppConstants.DOWLOAD_DELAY_TIME_TABLE);
    if(delayExportTimeTable != null)
    {
        String delayTimeKey = userId + jobId + workflowId;
        Date startTimeObj = (Date)delayExportTimeTable.get(delayTimeKey);
        if(startTimeObj != null)
        {
            startExportTime = startTimeObj.getTime();   
        }
    }
    
    boolean okForInContextReviewXml = PreviewPDFHelper.isXMLEnabled("" + theTask.getCompanyId());
    boolean okForInContextReviewIndd = PreviewPDFHelper.isInDesignEnabled("" + theTask.getCompanyId());
	boolean okForInContextReviewOffice = PreviewPDFHelper.isOfficeEnabled("" + theTask.getCompanyId());
%>
<HTML>
<HEAD>
<!-- This JSP is envoy/tasks/taskDetail.jsp -->
<TITLE><%= title %></TITLE>
<link rel="STYLESHEET" type="text/css" href="/globalsight/includes/ContextMenu.css">
<link rel="stylesheet" type="text/css" href="/globalsight/jquery/jQueryUI.redmond.css"/>
<style>
.comment {
  position: absolute;
  visibility: hidden;
  width: 400px;
  background-color: lightgrey;
  layer-background-color: lightgrey;
  border: 2px outset white;
}
div.tableContainer {
    border-style: none;    
    overflow: auto;
    }

div.tableContainer2 {
    border-style: none; 
    overflow: auto;
    }

table#scroll {
    width: 99%;     /*100% of container produces horiz. scroll in Mozilla*/
    border: solid 1px slategray;
    }

table#scroll>tbody  {  /* child selector syntax which IE6 and older do not support*/
    overflow: auto;
    height: 268px;
    }

thead#scroll td#scroll  {
    position:relative;
    top: expression(document.getElementById("data").scrollTop-2); /*IE5+ only*/
    }

span.taskComplDialog {
	line-height: 20px;
    font-weight: bold;
	}
</style>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>
<jsp:include flush="true" page="/includes/compatibility.jspIncl"></jsp:include>
<script type="text/javascript" src="/globalsight/includes/setStyleSheet.js"></script>
<script type="text/javascript" src="/globalsight/includes/modalDialog.js"></script>
<script type="text/javascript" src="/globalsight/includes/ContextMenu.js"></script>
<script type="text/javascript" src="/globalsight/includes/ieemu.js"></script>
<script type="text/javascript" src="/globalsight/includes/xmlextras.js"></script>
<script type="text/javascript" src="/globalsight/jquery/jquery-1.6.4.min.js"></script>
<script type="text/javascript" src="/globalsight/jquery/jquery-ui-1.8.18.custom.min.js"></script>
<script language="JavaScript" SRC="/globalsight/includes/utilityScripts.js"></script>
<script type="text/javascript">
var objectName = "";
var guideNode = "myActivities";
var w_editor = null;
var w_updateLeverage = null;
var b_editInSameWindow = eval("<%=editInSameWindow%>");
var b_canEditInSameWindow = eval("<%=canEditInSameWindow%>");
var b_isReviewActivity = eval("<%=isReviewActivity%>");
var needWarning = false;
var helpFile = "<%=helpFile%>";
var pageNames = new Array();
var incontextReviewPDFs = new Array();

var openIssuesDom = XmlDocument.create();
var taskId = <%=task_id%>;

var isFF=navigator.userAgent.indexOf("Firefox") >= 0?true:false;
var tfilePath="";


function cancelEvent(e)
{
    var eobj = (window.event)?(window.event):e;

    eobj.returnValue = false;
    eobj.cancelBubble = true;
}

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

function hideContextMenu()
{
    document.getElementById("idBody").focus();
}

function contextForPage(url, e, displayName)
{
    if(e instanceof Object)
    {
    e.preventDefault();
    e.stopPropagation();
    }
    var popupoptions;
    var lb_context_item_inline_editor;
    var lb_context_item_popup_editor ;
    var fontB1 = "<B>", fontB2 = "</B>";
    var incontextReviewPDF = incontextReviewPDFs[displayName];
    displayName = pageNames[displayName];
    
    var fileName = displayName;
    if (fileName.match(/\)$/))
    {
    	fileName = displayName.substr(0, displayName.lastIndexOf("("));
    	if (fileName.match(/ $/))
    	{
    		fileName = fileName.substr(0, fileName.length - 1);
    	}
    }
    
    var showInContextReview = (1 == incontextReviewPDF);
    var inctxTitle = "Open In Context Review";
    
    if (b_canEditInSameWindow)
    {
    	lb_context_item_inline_editor  = "<%=bundle.getString("lb_context_item_inline_editor") %>";
        lb_context_item_popup_editor   = "<%=bundle.getString("lb_context_item_popup_editor") %>";
        lb_context_item_post_review_editor = "<%=bundle.getString("lb_context_item_post_review_editor") %>";
        
    	if (b_editInSameWindow)
        {
            lb_context_item_inline_editor  = fontB1 + lb_context_item_inline_editor + fontB2;
        }
        else
        {
            lb_context_item_popup_editor   = fontB1 + lb_context_item_popup_editor + fontB2;
        }
      popupoptions = [
        new ContextItem(lb_context_item_inline_editor,
          function(){ openParaEditor(url, e);}),
        new ContextItem(lb_context_item_popup_editor,
          function(){ openListEditor(url, e);}),
        new ContextItem(lb_context_item_post_review_editor,
          function(){ openNewListEditor(url, e);})
        ];
      
      if (showInContextReview)
      {
    	  popupoptions[popupoptions.length] = new ContextItem(inctxTitle,
               function(){openInContextReview(url, e);});
      }
    }
    else
    {
      var title = "<%=bundle.getString("lb_context_item_popup_editor") %>";
      var title2 = "<%=bundle.getString("lb_context_item_post_review_editor") %>";
  
      if (b_isReviewActivity)
      {
        title = "<%=bundle.getString("lb_context_item_review_editor") %>";
      }
  
      popupoptions = [
   		new ContextItem(title, function(){ openListEditor(url, e);}),
   		new ContextItem(title2, function(){ openNewListEditor(url, e);})
        ];
      
      if (showInContextReview)
      {
    	  popupoptions[popupoptions.length] = new ContextItem(inctxTitle,
               function(){ openInContextReview(url, e);});
      }
    }
    ContextMenu.display(popupoptions, e); 
}

function openParaEditor(url, e)
{
    if (!canClose())
    {
        cancelEvent(e);
        raiseSegmentEditor();
    }
    else
    {
      //window.location.href = ("<%=editorParaUrl%>" + url);
      // For issue "(AMB-115) Toolbar behaviour for the Inline editor" to resolve the problem which the scroll jump up fist (no good UE) after closed inline editor
      var inlineUrl =  "<%=editorParaUrl%>" + url+tfilePath;
      window.open(inlineUrl, 'topFrame',
          'resizable,top=0,left=0,height=' + (screen.availHeight - 60) +
          ',width=' + (screen.availWidth - 20));
    }
}

function openInContextReview(url, e)
{
    if (!canClose())
    {
        cancelEvent(e);
        raiseSegmentEditor();
    }
    else
    {
        hideContextMenu();

        if (b_isReviewActivity)
        {
          url = "<%=incontextreviewUrlRe%>" + url;
        }
        else
        {
           url = "<%=incontextreviewUrl%>" + url;
        }
		 url += "&isContextReview=true&pageSearchText="+encodeURI(encodeURI("<%=thisFileSearchText%>"));
         w_editor = window.open(url, 'MainEditor',
          'resizable,top=0,left=0,height=' + (screen.availHeight - 60) +
          ',width=' + (screen.availWidth - 20));
    }
}

function openNewListEditor(url, e)
{
    if (!canClose())
    {
        cancelEvent(e);
        raiseSegmentEditor();
    }
    else
    {
        hideContextMenu();
		
        if (b_isReviewActivity)
        {
          url = "<%=newEditorReviewUrl%>" + url;
        }
        else
        {
           url = "<%=newEditorListUrl%>" + url;
        }
		 url += "&pageSearchText="+encodeURI(encodeURI("<%=thisFileSearchText%>")); 
         w_editor = window.open(url, 'MainEditor',
          'resizable,top=0,left=0,height=' + (screen.availHeight - 60) +
          ',width=' + (screen.availWidth - 20));
    }
}

function openListEditor(url, e)
{
    if (!canClose())
    {
        cancelEvent(e);
        raiseSegmentEditor();
    }
    else
    {
        hideContextMenu();

        if (b_isReviewActivity)
        {
          url = "<%=editorReviewUrl%>" + url;
        }
        else
        {
           url = "<%=editorListUrl%>" + url;
        }
		 url += "&pageSearchText="+encodeURI(encodeURI("<%=thisFileSearchText%>")); 
         w_editor = window.open(url, 'MainEditor',
          'resizable,top=0,left=0,height=' + (screen.availHeight - 60) +
          ',width=' + (screen.availWidth - 20));
    }
}

function openEditorWindow(url, e)
{
	if (b_editInSameWindow)
    {
        openParaEditor(url, e);
    }
    else
    {
        openListEditor(url, e);
    }
}

function canClose()
{
    if (w_editor != null && !w_editor.closed)
    {
        if (!w_editor.CanClose())
        {
            return false;
        }
    }

    return true;
}

function raiseSegmentEditor()
{
    if (w_editor != null && !w_editor.closed)
    {
        w_editor.RaiseSegmentEditor();
    }
}

function doBeforeUnload()
{
    // be unobstrusive and silently close unchanged editor windows.
    if (!canClose())
    {
        return "<%=labelEditorWarning%>";
    }

    return;
}

function doUnload()
{
    if (w_editor != null && !w_editor.closed)
    {
        w_editor.close();
    }
    try { w_updateLeverage.close(); } catch (e) {};

    w_editor = null;
    w_updateLeverage = null;
}

function submitDtpForm(form, buttonClicked, linkParam) {
    if(buttonClicked == "DtpDownload") {
        form.action = "<%=dtpDownloadURL%>&taskAction=<%=WebAppConstants.DTP_DOWNLOAD%>" + linkParam;
        form.submit();
        return;
    } else if(buttonClicked == "DtpUpload") {
        form.action = "<%=dtpUploadURL%>&taskAction=<%=WebAppConstants.DTP_UPLOAD%>" + linkParam;
        form.submit();
        return;
    }
}

function submitCommentForm(selectedButton)
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

function doOnload()
{
  ContextMenu.intializeContextMenu();
  loadGuides();
  if(<%=isCheckUnTranslatedSegments%>){
 	 translatedText();
  }
}

function searchPages(){
	var iChars = "#,%,^,&,+,\\,\',\",<,>.";
	var localesSelect = document.getElementById("pageSearchLocale");
 	var index = localesSelect.selectedIndex;
    var locale = localesSelect.options[index].value;
    var searchText = document.getElementById("pageSearchText").value;
    searchText = ATrim(searchText);
    if(checkSomeSpecialChars(searchText)){
   		alert("<%= bundle.getString("lb_tm_search_text") %>" + "<%= bundle.getString("msg_invalid_entry4") %>" + iChars);
        return false;
   }
    var url = "<%=searchTextUrl%>" + "&pageSearchLocale="+locale+"&pageSearchText="+encodeURI(encodeURI(searchText));
    pageSearchTextForm.action = url;
    pageSearchTextForm.submit();
}
</SCRIPT>
</HEAD>
<BODY LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0" MARGINWIDTH="0" MARGINHEIGHT="0"
    onbeforeunload="return doBeforeUnload()" onload="doOnload()" onunload="doUnload()" id="idBody">
<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>
<DIV ID="contentLayer" STYLE=" POSITION: ABSOLUTE; Z-INDEX: 9; TOP: 108px; LEFT: 20px; RIGHT: 20px;">
<% if (b_catalyst) {%>
<TABLE ALIGN="RIGHT"><TR><TD><IMG SRC="/globalsight/images/logo_alchemy.gif"/></TD></TR></TABLE>
<% } %>

<%@ include file="/envoy/tasks/includeTaskSummaryTabs.jspIncl" %>
<br>
<!-- Lower table -->
<TABLE CELLPADDING="0" CELLSPACING="0" BORDER="0" width="900px">
<TR>
<TD VALIGN="TOP">
<!-- Pages table -->
<amb:permission name="<%=Permission.ACTIVITIES_FILES_VIEW%>" >

    <%@ include file="/envoy/projects/workflows/pageSort.jspIncl" %>
    <% 
    if (task_type.equals(Task.TYPE_TRANSLATION))
    {
    %>
    <%@ include file="/envoy/projects/workflows/pageSearch.jspIncl" %>
	    <FORM name=pageSearchTextForm  id="pageSearchTextForm" method="post" action=""  ENCTYPE="multipart/form-data">
			<table  CELLSPACING="0" CELLPADDING="2" BORDER="0" style="border:solid 1px slategray;background:#DEE3ED;width:100%;height:40">
			  <tr>
			  	<td class="standardText" style="width:8%"><%=bundle.getString("lb_search_in")%>:</td>
			  	<td class="standardText" style="width:14%">
			  		<select id="<%=JobManagementHandler.PAGE_SEARCH_LOCALE%>"  name="<%=JobManagementHandler.PAGE_SEARCH_LOCALE%>" style="width:100%">
			  			<option value="sourceLocale" <%=thisSearchLocale.equals("sourceLocale") ? "selected":""%>><%=bundle.getString("lb_tm_search_source_locale")%></option>
			  			<option value="targetLocale" <%=thisSearchLocale.equals("targetLocale") ? "selected":""%>><%=bundle.getString("lb_tm_search_target_locale")%></option>
			  		</select>
			  	</td>
			    <td class="standardText" style="width:8%"><%=bundle.getString("lb_search_for")%>:</td>
			    <td class="standardText" style="width:45%">
				      <input type="text" maxlength="200" style="width:100%" id = "<%=JobManagementHandler.PAGE_SEARCH_TEXT%>" 
				       name="<%=JobManagementHandler.PAGE_SEARCH_TEXT%>"  value="<%=thisFileSearchText%>">
			    </td>
			    <td class="standardText" style="width:8%">
			      <input type="submit" style="width:100%" value="<%=bundle.getString("lb_search")%>" onclick ="searchPages();">
			    </td>
			    <td class="standardText" style="width:17%"></td>
			  </tr>
			</table> 
		</FORM>
    <% 
    }
    %>
    
	<div class="tableContainer" id="data" style="border:solid 1px slategray;">
    <%
    if (task_type.equals(Task.TYPE_TRANSLATION))
    {
    %>
	    <TABLE CELLSPACING="0" CELLPADDING="2" BORDER="0">
	    <thead id=scroll>
	        <TR CLASS="tableHeadingBasic" >
	            <TD COLSPAN=2 style="padding-top: 2px; padding-bottom: 2px;width:630px;"><A CLASS="sortHREFWhite" 
	            HREF="<%=pageListUrl + "&" + JobManagementHandler.PAGE_SORT_PARAM + "=" + PageComparator.EXTERNAL_PAGE_ID +"&" + WebAppConstants.TASK_STATE + "=" + state + "&" + WebAppConstants.TASK_ID + "=" + task_id%>">
	            <%=labelContentItem%>
	            <SPAN CLASS="smallWhiteItalic">
	            (<%=labelClickToOpen%>)</SPAN></A><%=pageNameSortArrow%></TD>
	            <TD  ALIGN="CENTER" style="padding-top: 2px; padding-bottom: 2px;width:90px;text-align:center"> <A CLASS="sortHREFWhite" HREF="<%=pageListUrl + "&" + JobManagementHandler.PAGE_SORT_PARAM + "=" + PageComparator.WORD_COUNT +"&" + WebAppConstants.TASK_STATE + "=" + state + "&" + WebAppConstants.TASK_ID + "=" + task_id%>" >
	            <%=labelWordCount%></A><%=wordCountSortArrow%></TD>
	            <TD style="padding-top: 2px; padding-bottom: 2px;width:90px;text-align:center"><%=bundle.getString("lb_source")%></TD>
	            <TD style="width:90px;text-align:center;"><INPUT type='BUTTON' onclick='translatedText();' value='<%=bundle.getString("lb_task_translated_text")%>'></TD>
	        </TR>
	    </thead>
	    <tbody>
	        <%
	        StringBuffer treeLink=new StringBuffer();
	        boolean hasEditPerm = perms.getPermissionFor(Permission.ACTIVITIES_FILES_EDIT);
	        int targetPgsSize = targetPgs == null ? 0 : targetPgs.size();
	        if (targetPgsSize > 0)
	        {
	            TargetPage tPage = null;
	            String pageName = null;
	            String pageUrl = null;
	            // for unextracted
	            String modifiedBy = null;
	            String dateStr = null;
	            long fileSize = 0;
	
	            for (int i = 0; i < targetPgs.size(); i++)
	            {
	                tPage = (TargetPage)targetPgs.get(i);
	                long sourcePageId = tPage.getSourcePage().getId();
	                long targetPageId = tPage.getId();
	                tarPageIds.append(String.valueOf(targetPageId) + ",");
	
	                boolean isExtracted =
			          tPage.getPrimaryFileType() == PrimaryFile.EXTRACTED_FILE;
	
	                if (isExtracted)
	                {
	                    pageName = tPage.getExternalPageId();
	
	                    pageUrl =
	                      "&" + WebAppConstants.SOURCE_PAGE_ID + "=" + sourcePageId +
	                      "&" + WebAppConstants.TARGET_PAGE_ID + "=" + targetPageId +
	                      "&" + WebAppConstants.TASK_ID + "=" + theTask.getId();
	                }
	                else
	                {
	                    UnextractedFile unextractedFile =
			              (UnextractedFile)tPage.getPrimaryFile();
	                    pageName = unextractedFile.getStoragePath().replace("\\", "/");
	                    pageUrl = WebAppConstants.UNEXTRACTED_FILES_URL_MAPPING +
	                      pageName;
	                    modifiedBy = unextractedFile.getLastModifiedBy();
	                    modifiedBy = UserHandlerHelper.getUser(modifiedBy).getUserName();
	
	                    // Get the Last Modified date and format it
	                    Date date = unextractedFile.getLastModifiedDate();
	                    ts.setDate(date);
	                    dateStr = ts.toString();
	
	                    // Get the file size and format it
	                    fileSize = unextractedFile.getLength();
	                    long r = fileSize%1024;
	                    fileSize = (r != 0) ? ((fileSize/1024)+1) : fileSize;  //round up
	                }
	                %>
	                <TR VALIGN=TOP>
	                <TD>
	                <IMG SRC="<%=isExtracted ? extractedImage : unExtractedImage%>"
	                    title="<%=isExtracted ? extractedToolTip : unExtractedToolTip%>"
	                    WIDTH=13 HEIGHT=15>
	                </TD>
	                <%	
	                String treeParam="";
	                if (isExtracted)
	                {%>
	                	<TD class='filelist' style='word-wrap : break-word;word-break:break-all;'>
	                	<%	
	                    // print page name and editor link
	                    if (pagenameDisplay.equals(UserParamNames.PAGENAME_DISPLAY_FULL))
	                    {
	                    	treeParam=TaskDetailHelper.printPageLink(out, pageName, pageUrl, hasEditPerm, i);
	                    }
	                    else if (pagenameDisplay.equals(UserParamNames.PAGENAME_DISPLAY_SHORT))
	                    {
	                    	treeParam=TaskDetailHelper.printPageLinkShort(out, pageName, pageUrl, hasEditPerm, i);
	                    }
	                    treeLink.append(treeParam+"?");
						%>
	                    </TD>
	                <%}
	                else
	                {
	                	
	                %>
	                    <TD CLASS="standardText filelist" style="word-break : break-all; overflow:hidden; ">
	                    <A CLASS="standardHREF" HREF="<%=pageUrl%>" target="_blank"><%=pageName%>
	                    </A>
	                    <BR>
	                    <SPAN CLASS="smallText">
	                    <%=bundle.getString("lb_last_modified") +  ": " + dateStr%> -
	                    <%=fileSize%>K<BR>
	                    <%=bundle.getString("lb_modified_by") +  ": " + modifiedBy%>
	                    </SPAN>
	                    </TD>
	                <%
	                }
	                %>
	                    <TD ALIGN="CENTER"><SPAN CLASS="standardText"><%=tPage.getWordCount().getTotalWordCount()%></SPAN></TD>
	                    <TD ALIGN="CENTER">
	                    <%
	                        String fileName = tPage.getSourcePage().getExternalPageId();
	
	                        // For PPT Word Indd issue
	                    	fileName = com.globalsight.everest.webapp.pagehandler.projects.workflows.JobDetailsHandler.filtSpecialFile(fileName);
	                        String sourcefilePath = "/globalsight" + WebAppConstants.VIRTUALDIR_CXEDOCS2 + 
	                          CompanyWrapper.getCompanyNameById(tPage.getSourcePage().getCompanyId()) + 
	                          "/" + fileName;
	                        sourcefilePath = URLEncoder.encodeUrlStr(sourcefilePath);
	                        sourcefilePath = sourcefilePath.replace("%2F", "/");
	                    %>
	                    <A CLASS="standardHREF" HREF="<%=sourcefilePath%>" target="_blank"><%=bundle.getString("lb_click_to_view")%></A></TD>
	                    <TD ALIGN="CENTER"><SPAN CLASS="standardText" ID="<%="oPara" + i%>" style = "font-weight:600"></SPAN></TD>
	            <%
	            
	            }
	            if(tarPageIds.length() != 0)
	            {
	                String[] tpIds = tarPageIds.toString().split(",");
	                StringBuffer tpIdBatch = new StringBuffer();
	                int count = 0;
	                for (String tpId : tpIds)
	                {
	                    if (count == translatedTextCount)
	                    {
	                        trgPageIdBatches.add(tpIdBatch.toString()+",");
	                        tpIdBatch = new StringBuffer();
	                        count = 0;
	                    }

	                    if (tpIdBatch.length() == 0)
                        {
                            tpIdBatch.append(tpId);
                        }
                        else
                        {
                            tpIdBatch.append(",").append(tpId);
                        }
	                    count++;
	                }
	                if (tpIdBatch.length() > 0)
                    {
	                    trgPageIdBatches.add(tpIdBatch.toString()+",");
                    }
	            }
	            sessionMgr.setAttribute("treeLink", treeLink.toString().replaceAll("\\\\", "<").replaceAll("'", "&apos;"));
	            sessionMgr.setAttribute("ajaxUrl",detail.getPageURL());
	        }
	        %>
	        </TR>
<%
    }
    else if (task_type.equals(Task.TYPE_DTP))
    {
%>
    <TABLE CELLSPACING="0" CELLPADDING="2" BORDER="0">
    <thead id=scroll>
      <TR CLASS="tableHeadingBasic">
        <TD COLSPAN=2 style="padding-left: 8px; padding-top: 2px; padding-bottom: 2px"><%=labelContentItem%>
        <SPAN CLASS="smallWhiteItalic"><%=bundle.getString("lb_click_to_view")%></SPAN></TD>
        <TD WIDTH="20" style="padding-left: 8px; padding-top: 2px; padding-bottom: 2px">&nbsp;</TD>
        <TD style="padding-left: 8px; padding-top: 2px; padding-bottom: 2px" align="center"><%=bundle.getString("lb_dtpdownload")%></TD>        
        <TD style="padding-left: 8px; padding-top: 2px; padding-bottom: 2px" align="center"><%=bundle.getString("lb_dtpupload")%></TD>
      </TR>
    </thead>
    <tbody>
<%
	boolean hasEditPerm = perms.getPermissionFor(Permission.ACTIVITIES_FILES_EDIT);
        int targetPgsSize = targetPgs == null ? 0 : targetPgs.size();
        if (targetPgsSize > 0)
        {
            TargetPage tPage = null;
            String pageName = null;
            String pageUrl = null;
            // for unextracted
            String dateStr = null;
            long fileSize = 0;
            for (int i = 0; i < targetPgs.size(); i++)
            {
            	tPage = (TargetPage)targetPgs.get(i);
            	pageName = tPage.getExternalPageId();
                long sourcePageId = tPage.getSourcePage().getId();
                long targetPageId = tPage.getId();

                String fileName = TaskDetailHelper.getFileName(pageName);
                String subFileName = fileName.substring(fileName.indexOf("\\"));
                subFileName = ExportHelper.transformExportedFilename(subFileName, tPage.getGlobalSightLocale().toString());
                
                //String subTargetLocale = targetLocale.substring(targetLocale.indexOf('[')+1,targetLocale.indexOf(']'));
                String exportSubDir = tPage.getExportSubDir();
                String targetFilePath = "/globalsight" + WebAppConstants.VIRTUALDIR_CXEDOCS + 
                    CompanyWrapper.getCompanyNameById(tPage.getSourcePage().getCompanyId()) + 
                    exportSubDir + subFileName;
                pageUrl = "&" + WebAppConstants.TARGET_PAGE_ID + "=" + targetPageId;
     %>
                <TR VALIGN=TOP>
                  <TD>
                    <IMG SRC="<%=extractedImage%>" ALT="<%=extractedToolTip%>" WIDTH=13 HEIGHT=15>
                  </TD>
                  <TD STYLE='word-wrap: break-word;word-break:break-all;'>
                  <A CLASS="standardHREF" HREF="<%=targetFilePath%>" target="_blank"><%=fileName%></A></TD>
                  <TD ALIGN="CENTER">
                    <form name="dtpDownloadForm" method="post">
                      <INPUT CLASS='standardText' TYPE='BUTTON' VALUE='<%=bundle.getString("lb_download")%>' onClick='submitDtpForm(this.form, "DtpDownload", "<%=pageUrl%>")'>
                    </form>
                  </TD>
                  <TD ALIGN="CENTER">
                    <form name="dtpUploadForm" method="post" enctype="multipart/form-data">
                    <TABLE CELLSPACING="0" CELLPADDING="2" BORDER="0">
                    <TR>
                    <TD><INPUT CLASS='standardText' TYPE='file' NAME='file'></TD>
                    <TD><INPUT CLASS='standardText' TYPE='BUTTON' value="<%=bundle.getString("lb_upload")%>" onClick='submitDtpForm(this.form, "DtpUpload", "<%=pageUrl%>")'></TD>
                    </TR>
                    </TABLE>
                    </form>
                  </TD>
                </TR>
            <%
            }
          }
    }
%>
		<tr>
		<td colspan=2>
		<INPUT type=BUTTON style="float:left" onclick='submitHourOrPageForm("wordcounts");' value='<%= labelWordCounts %>...'>
		</td>
		<td colspan=2></td>
		<td>
		<INPUT type=BUTTON style="float:right" onclick="location.href='${detail.pageURL}&taskId=<%= task_id %>&<%= WebAppConstants.TASK_ACTION %>=<%= WebAppConstants.TASK_ACTION_DOWNLOAD_SOURCEPAGES %>'" 
			value="<%=bundle.getString("lb_download_files_in_task_detail")%>"/>
		</td>
		</tr>
 		</tbody>
   	  </TABLE>
	</div>
</amb:permission>
<!-- End Pages table -->

<!-- Primary Unextracted Source File table -->
<%
    perms = (PermissionSet) session.getAttribute(
                    WebAppConstants.PERMISSIONS);
    boolean hasFilesViewPerm = perms.getPermissionFor(Permission.ACTIVITIES_FILES_VIEW);
   List unExtractedSrcs = theTask.getSourcePages(PrimaryFile.UNEXTRACTED_FILE);
   int srcFiles = unExtractedSrcs == null ? 0 : unExtractedSrcs.size();
   if (srcFiles > 0 && hasFilesViewPerm)
   {
   %>
     <BR>
     <div class="tableContainer" id="data" style="border:solid 1px slategray;">
        <TABLE CELLSPACING="0" CELLPADDING="2" BORDER="0">
        <thead id=scroll>
        <COL> <!-- Icon -->
        
        <TR CLASS="tableHeadingBasic">
           <TD COLSPAN=2><%=bundle.getString("lb_primary_source_file_heading")%>
           <SPAN CLASS="smallWhiteItalic">
               (<%=labelClickToView%>)</SPAN>
           </TD>
           <TD WIDTH="20">&nbsp;</TD>
        </TR>
        </THEAD>
        <tbody>
       <%
        for (int i = 0; i < srcFiles; i++)
        {
            SourcePage sp = (SourcePage)unExtractedSrcs.get(i);

            UnextractedFile unextractedSrc = (UnextractedFile)sp.getPrimaryFile();
            String spName = unextractedSrc.getStoragePath().replace("\\", "/");
            String spUrl = WebAppConstants.UNEXTRACTED_FILES_URL_MAPPING +
                           spName;
            spUrl = URLEncoder.encodeUrlStr(spUrl);
    		spUrl = spUrl.replace("%2F", "/");
            String modifier = unextractedSrc.getLastModifiedBy();
            modifier = UserHandlerHelper.getUser(modifier).getUserName();

            // Get the Last Modified date and format it
            ts.setDate(unextractedSrc.getLastModifiedDate());

            // Get the file size and format it
            long sourceFileSize = unextractedSrc.getLength();
            long r = sourceFileSize%1024;
            sourceFileSize = (r != 0) ? ((sourceFileSize/1024)+1) : sourceFileSize;  //round up
            %>
            <TR  VALIGN=TOP CLASS="standardText">
            <TD>
            <IMG SRC="<%=unExtractedImage%>"
                ALT="<%=unExtractedToolTip%>"
                WIDTH=13 HEIGHT=15>
            </TD>
            <TD CLASS="standardText">
            <A CLASS="standardHREF" HREF="<%=spUrl%>" target="_blank"><%=spName%>
            </A>
            <BR>
            <SPAN CLASS="smallText">
            <%= bundle.getString("lb_last_modified") +  ": " + ts.toString()%> -
            <%= sourceFileSize%>K<BR>
            <%= bundle.getString("lb_modified_by") +  ": " + modifier%>
            </SPAN>
            </TD>
        <%}%>
        </TR>
        </TBODY>
	</TABLE>
	</DIV>
    <%
    }%>
<!-- End Primary Unextracted Source File table -->
</TD>
</TR>
</TABLE>
<BR>
<!-- End Lower table -->
</DIV>
<!--// Task Completed Dialog  -->
</BODY>
</HTML>
<SCRIPT LANGUAGE = "JavaScript">

$(document).ready(function(){
	$("#taskTargetFilesTab").removeClass("tableHeadingListOff");
	$("#taskTargetFilesTab").addClass("tableHeadingListOn");
	$("#taskTargetFilesTab img:first").attr("src","/globalsight/images/tab_left_blue.gif");
	$("#taskTargetFilesTab img:last").attr("src","/globalsight/images/tab_right_blue.gif");
})
var j=0;
var k=0;
var xmlHttp = false;
try {
  xmlHttp = new ActiveXObject("Msxml2.XMLHTTP");
} catch (e) {
  try {
    xmlHttp = new ActiveXObject("Microsoft.XMLHTTP");
  } catch (e2) {
    xmlHttp = false;
  }
}

if (!xmlHttp && typeof XMLHttpRequest != 'undefined') {
  xmlHttp = new XMLHttpRequest();
}

function callServer(url)
{
  xmlHttp.open("POST", url, true);
  xmlHttp.onreadystatechange = updatePage;
  xmlHttp.send(null);
}

function updatePage() 
{
  if (xmlHttp.readyState == 4)
  {
    if (xmlHttp.status == 200)
    {
       var response = xmlHttp.responseText;
       var translatedTexts = response;
       if(translatedTexts != null && translatedTexts.length != 0)
       {
            var translatedText = translatedTexts.substring(0, translatedTexts.length - 1);
            var translatedVar = translatedText.split(",");
            k++;
            var num=(j-1)*<%=translatedTextCount%>;
            for(var i=num; i < (translatedVar.length+num); i++)
            {
                var objName = "oPara" + i;
                var obj = document.getElementById(objName);
                if(translatedVar[i-num] < 100){
                	obj.style.color = "red";
                	obj.innerHTML = "(" + translatedVar[i-num] + "%)";
                }else{
                	obj.style.color = "black";
                	obj.innerHTML = "(" + translatedVar[i-num] + "%)";
                }
            }
       }
    }
  }
}
function translatedText()
{
	j=0;
	k=0;
	var resultIds = eval(<%=trgPageIdBatches%>);
	resultIds=resultIds.join(",").split(",,");
	var count = setInterval(function translatedTextc()
	{
		if(j==k)
		{
		    callServer("<%=translatedTextUrl + "&" +TaskDetailHandler.TASK_PAGE_IDS%>" + "=" + resultIds[j]);
		    j++;
		}
        if(j>=resultIds.length)
    	{
    		clearInterval(count);
    	}
	},10);
}
<%
int targetPgsSize = targetPgs == null ? 0 : targetPgs.size();
if (targetPgsSize > 0)
{
	TargetPage tPage = null;
    String pageName = null;
    for (int i = 0; i < targetPgs.size(); i++)
    {
    	tPage = (TargetPage)targetPgs.get(i);
        boolean isExtracted =
          tPage.getPrimaryFileType() == PrimaryFile.EXTRACTED_FILE;
        FileProfile fp = ServerProxy.getFileProfilePersistenceManager().readFileProfile(tPage.getSourcePage().getRequest().getDataSourceId());

        if (isExtracted)
        {
            pageName = tPage.getExternalPageId().replace("\\", "/");
        }
        else
        {
            UnextractedFile unextractedFile =
              (UnextractedFile)tPage.getPrimaryFile();
            pageName = unextractedFile.getStoragePath().replace("\\", "/");
        }
        if (isExtracted)
        {
            String pageNameLow = pageName.toLowerCase();
            boolean isXml = pageNameLow.endsWith(".xml");
            boolean isInDesign = pageNameLow.endsWith(".indd") || pageNameLow.endsWith(".idml");
            boolean isOffice = pageNameLow.endsWith(".docx") || pageNameLow.endsWith(".pptx") || pageNameLow.endsWith(".xlsx");
            
            boolean enableInContextReivew = false;
            if (isXml)
            {
                enableInContextReivew = okForInContextReviewXml ? FileProfileUtil.isXmlPreviewPDF(fp) : false;
            }
            if (isInDesign)
            {
                enableInContextReivew = okForInContextReviewIndd;
            }
            if (isOffice)
            {
                enableInContextReivew = okForInContextReviewOffice;
            }
        %>
           	pageNames[<%=i%>] = "<%=pageName%>";
           	incontextReviewPDFs[<%=i%>] = <%=(enableInContextReivew ? 1 : 0 )%>;
      <%}
    }
}%>

</SCRIPT>