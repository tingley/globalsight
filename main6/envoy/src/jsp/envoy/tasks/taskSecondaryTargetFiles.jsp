<%@ taglib uri="/WEB-INF/tlds/globalsight.tld" prefix="amb" %>
<%@ page
    contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/activityError.jsp"
    import="
      com.globalsight.config.UserParamNames,      
      com.globalsight.config.UserParameter,
      com.globalsight.cxe.entity.fileprofile.FileProfile,
      com.globalsight.everest.comment.CommentFile,      
      com.globalsight.everest.comment.CommentManager,
      com.globalsight.everest.company.CompanyThreadLocal,
      com.globalsight.everest.company.CompanyWrapper,
      com.globalsight.everest.costing.AmountOfWork,
      com.globalsight.everest.costing.Rate,
      com.globalsight.everest.foundation.Timestamp,
      com.globalsight.everest.foundation.User,
      com.globalsight.everest.jobhandler.Job,
      com.globalsight.everest.projecthandler.ProjectImpl,
      com.globalsight.everest.workflowmanager.WorkflowImpl,
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
      com.globalsight.everest.webapp.pagehandler.administration.comment.CommentConstants,
      com.globalsight.everest.webapp.pagehandler.administration.customer.download.DownloadFileHandler,
      com.globalsight.everest.webapp.pagehandler.administration.users.UserHandlerHelper,
      com.globalsight.everest.webapp.pagehandler.offline.OfflineConstants,
      com.globalsight.everest.webapp.pagehandler.projects.workflows.JobManagementHandler,
      com.globalsight.everest.webapp.pagehandler.projects.workflows.JobSearchConstants,
      com.globalsight.everest.webapp.pagehandler.projects.workflows.PageComparator,
      com.globalsight.everest.webapp.pagehandler.tasks.TaskDetailHandler,      
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
<jsp:useBean id="taskCommentList" scope="request"
 class="java.util.ArrayList" />
<jsp:useBean id="updateLeverage" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<%!

private String getMainFileName(String p_filename)
{
  int index = p_filename.indexOf(")");
  if (index > 0 && p_filename.startsWith("("))
  {
    index++;
    while (Character.isSpace(p_filename.charAt(index)))
    {
      index++;
    }

    return p_filename.substring(index, p_filename.length());
  }

  return p_filename;
}

private String getSubFileName(String p_filename)
{
  int index = p_filename.indexOf(")");
  if (index > 0 && p_filename.startsWith("("))
  {
    return p_filename.substring(0, p_filename.indexOf(")") + 1);
  }

  return null;
}

private String getFileName(String p_page)
{
  String fileName = getMainFileName(p_page);
  String subName = getSubFileName(p_page);

  if (subName != null)
  {
    fileName = fileName + " " + subName;
  }
  
  return fileName;
}

// Prints file name with full path
private String printPageLink(JspWriter out, String p_page, String p_url, boolean hasEditPerm)
  throws IOException
{
  // Preserve any MsOffice prefixes: (header) en_US/foo/bar.ppt but
  // show them last so the main file names are grouped together
//text=en_US\182\endpoint\ch01.htm|sourcePageId=752|targetPageId=3864|taskId=4601?
	
  String pageName = getMainFileName(p_page);
  String subName = getSubFileName(p_page);

  if (subName != null)
  {
    pageName = pageName + " " + subName;
  }
   StringBuffer treeParam=new StringBuffer();
  if (hasEditPerm) {
    out.print("<a href='#'");
    out.print(" onclick=\"openEditorWindow('");
    out.print(p_url);
    out.print("', event); return false;\"");
    out.print(" oncontextmenu=\"contextForPage('");
    out.print(p_url);
    out.print("', event)\"");
    out.print(" onfocus='this.blur();'");
    out.print(" page='");
    out.print(p_url);
    out.print("'");
    out.print(" CLASS='standardHREF'>");
    out.print("<SCRIPT language=\"javascript\">if (navigator.userAgent.indexOf(\'Firefox\') >= 0){document.write(\"<DIV style=\'width:500px\'>\");}</SCRIPT>");
    out.print(pageName);
    out.print("<SCRIPT language=\"javascript\">if (navigator.userAgent.indexOf(\'Firefox\') >= 0){document.write(\"</DIV>\")}</SCRIPT>");
    out.print("</a>");
    treeParam.append("text="+pageName).append(p_url.replaceAll("&", "|")).append("|title="+pageName);
  }
  else {
    out.print(pageName);
  }
 
  return treeParam.toString();
}

// Prints short file name with full path in tooltip
private String printPageLinkShort(JspWriter out, String p_page, String p_url, boolean hasEditPerm)
  throws IOException
{
  // Preserve any MsOffice prefixes: (header) en_US/foo/bar.ppt but
  // show them last so the main file names are grouped together
  String pageName = getMainFileName(p_page);
  String subName = getSubFileName(p_page);
  String shortName = pageName;

  int bslash = shortName.lastIndexOf("\\");
  int fslash = shortName.lastIndexOf("/");
  int index;
  if (bslash > 0 && bslash > fslash)
  {
    shortName = shortName.substring(bslash + 1);
  }
  else if (fslash > 0 && fslash > bslash)
  {
    shortName = shortName.substring(fslash + 1);
  }

  if (subName != null)
  {
    pageName = pageName + " " + subName;
    shortName = shortName + " " + subName;
  }
  StringBuffer treeParam=new StringBuffer();
  if (hasEditPerm)
  {
    out.print("<a href='#'");
    out.print(" onclick=\"openEditorWindow('");
    out.print(p_url);
    out.print("', event); return false;\"");
    out.print(" oncontextmenu=\"contextForPage('");
    out.print(p_url);
    out.print("', event)\"");
    out.print(" onfocus='this.blur();'");
    out.print(" page='");
    out.print(p_url);
    out.print("'");
    out.print(" CLASS='standardHREF' TITLE='");
    out.print(pageName.replace("\'", "&apos;"));
    out.print("'>");
    out.print(shortName);
    out.print("</a>");
    treeParam.append("text="+shortName.replace("\'", "&apos;")).append(p_url.replaceAll("&", "|")).append("|title="+pageName);
  } else {
    out.print(pageName);
  }

 
  return treeParam.toString();
}
%><%
    String thisFileSearch = (String) request.getAttribute(JobManagementHandler.PAGE_SEARCH_PARAM);
    if (thisFileSearch == null)
    {
       thisFileSearch = "";
    }
    ResourceBundle bundle = PageHandler.getBundle(session);
    SessionManager sessionMgr =
      (SessionManager)session.getAttribute(WebAppConstants.SESSION_MANAGER);
 

    String subTitle = bundle.getString("lb_my_activities") + bundle.getString("lb_colon");
    String title= bundle.getString("lb_secondary_target_files");
    
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
    String labelReportQAChecks = bundle.getString("lb_activity_qa_checks");
    boolean showQAChecksTab = QACheckerHelper.isShowQAChecksTab(theTask);
    boolean showDITAQAChecksTab = DITAQACheckerHelper.isShowDITAChecksTab(theTask);

    //Urls of the links on this page
    String acceptUrl = accept.getPageURL() + "&" + WebAppConstants.TASK_ACTION +
        "=" + WebAppConstants.TASK_ACTION_ACCEPT+
        //GBS-2913 Added to the url parameter taskId
        "&"+WebAppConstants.TASK_ID+"="+theTask.getId();;

    StringBuffer tmpUrl = new StringBuffer(updateLeverage.getPageURL());
    tmpUrl.append("&").append(WebAppConstants.TASK_ID).append("=").append(theTask.getId()).append("&action=getAvailableJobsForTask");
    String updateLeverageUrl = tmpUrl.toString();
    
    String rejectUrl = reject.getPageURL()
    		//GBS-2913 Added to the url parameter taskId,state;
    		+"&"+WebAppConstants.TASK_ID+"="+theTask.getId()
            +"&"+WebAppConstants.TASK_STATE+"="+theTask.getState();
    String wordCountUrl = wordcountList.getPageURL() + "&action=tpList&"
    		//GBS-2913 Added to the url parameter taskId,state;
    		+WebAppConstants.TASK_ID+"="+theTask.getId()
            +"&"+WebAppConstants.TASK_STATE+"="+theTask.getState();
    		
    String pageListUrl = pageList.getPageURL() + "&" + JobManagementHandler.PAGE_SEARCH_PARAM + "=" + thisFileSearch;
    String dtpDownloadURL = dtpDownload.getPageURL();
    String dtpUploadURL = dtpUpload.getPageURL();
    String editorParaUrl = editorSameWindow.getPageURL();
    String editorListUrl = editor.getPageURL();
    String editorReviewUrl = editor.getPageURL() +
       "&" + WebAppConstants.REVIEW_MODE + "=true";

    String createStfUrl = accept.getPageURL() + "&" + WebAppConstants.TASK_ACTION +
        "=" + WebAppConstants.TASK_ACTION_CREATE_STF+"&"+WebAppConstants.TASK_ID+"="+theTask.getId()
        +"&"+WebAppConstants.TASK_STATE+"="+theTask.getState();//GBS-2913 Added to the url parameter taskId,state
    String finishUrl = finish.getPageURL() + "&" + WebAppConstants.TASK_ACTION +
        				"=" + WebAppConstants.TASK_ACTION_FINISH+
        				//GBS-2913 Added to the url parameter taskId,state;
        				"&"+WebAppConstants.TASK_ID+"="+theTask.getId()+
        	            "&"+WebAppConstants.TASK_STATE+"="+theTask.getState();
        	    		
    String downloadUrl = download.getPageURL()
    		//GBS 2913 add taskID and taskState
    		+ "&" + WebAppConstants.TASK_ID + "=" + theTask.getId()
    		+ "&" + WebAppConstants.TASK_STATE + "=" + theTask.getState();;
    String uploadUrl = upload.getPageURL()
    		//GBS 2913 add taskID and taskState
    		+ "&" + WebAppConstants.TASK_ID + "=" + theTask.getId()
    		+ "&" + WebAppConstants.TASK_STATE + "=" + theTask.getState();;
    
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
    segmentTargLocales.add((Locale)theTask.getTargetLocale().getLocale());
    String segmentSelectedLocale =
      theTask.getTargetLocale().getLocale().getDisplayName();
        

    // Create the exportLink for the Export button
    StringBuffer exportLink = new StringBuffer(export.getPageURL());
    exportLink.append("&");
    exportLink.append(JobManagementHandler.WF_ID);
    exportLink.append("=");
    exportLink.append(theTask.getWorkflow().getId());
    exportLink.append("&");
    exportLink.append(JobManagementHandler.EXPORT_SELECTED_WORKFLOWS_ONLY_PARAM);
    exportLink.append("=true");
    //GBS-2913 Added to the url parameter taskId
    exportLink.append("&");
    exportLink.append(WebAppConstants.TASK_ID);
    exportLink.append("=");
    exportLink.append(theTask.getId());
    exportLink.append("&");
    exportLink.append(WebAppConstants.TASK_STATE);
    exportLink.append("=");
    exportLink.append(theTask.getState());
    
    //Create the downloadLink for the download button
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
        String delayTimeKey = userId + jobId + theTask.getWorkflow().getId();
        Date startTimeObj = (Date)delayExportTimeTable.get(delayTimeKey);
        if(startTimeObj != null)
        {
            startExportTime = startTimeObj.getTime();   
        }
    }
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
    height: 200 px;
    border-style: none;    
    overflow: auto;
    }

div.tableContainer2 {
    height: 200 px;  /* must be greater than tbody*/
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
<%@ include file="/includes/compatibility.jspIncl" %>
<script type="text/javascript" src="/globalsight/includes/setStyleSheet.js"></script>
<script type="text/javascript" src="/globalsight/includes/modalDialog.js"></script>
<script type="text/javascript" src="/globalsight/includes/ContextMenu.js"></script>
<script type="text/javascript" src="/globalsight/includes/ieemu.js"></script>
<script type="text/javascript" src="/globalsight/includes/xmlextras.js"></script>
<script type="text/javascript" src="/globalsight/jquery/jquery-1.6.4.min.js"></script>
<script type="text/javascript" src="/globalsight/jquery/jquery-ui-1.8.18.custom.min.js"></script>
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

function contextForPage(url, e)
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
    
    if (b_canEditInSameWindow)
    {
    	if (b_editInSameWindow)
        {
            lb_context_item_inline_editor  = "<%=bundle.getString("lb_context_item_inline_editor") %>";
            lb_context_item_popup_editor   = "<%=bundle.getString("lb_context_item_popup_editor") %>";
            lb_context_item_inline_editor  = fontB1 + lb_context_item_inline_editor + fontB2;
        }
        else
        {
        	lb_context_item_inline_editor  = "<%=bundle.getString("lb_context_item_inline_editor") %>";
            lb_context_item_popup_editor   = "<%=bundle.getString("lb_context_item_popup_editor") %>";
            lb_context_item_popup_editor   = fontB1 + lb_context_item_popup_editor + fontB2;
        }
      popupoptions = [
        new ContextItem(lb_context_item_inline_editor,
          function(){ openParaEditor(url, e);}),
        new ContextItem(lb_context_item_popup_editor,
          function(){ openListEditor(url, e);})
        ];
    }
    else
    {
      var title = "<%=bundle.getString("lb_context_item_popup_editor") %>";
  
      if (b_isReviewActivity)
      {
        title = "<%=bundle.getString("lb_context_item_review_editor") %>";
      }
  
      popupoptions = [
   		new ContextItem(title, function(){ openListEditor(url, e);})
        ];
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

function recreateGSEdition(urlSent) {
    location.replace(urlSent);
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
  translatedText();
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
<TABLE CELLPADDING="0" CELLSPACING="0" BORDER="0" width="900px">
<TR>
<TD VALIGN="TOP">
<!-- STFs table -->
<%
   Set<SecondaryTargetFile> stfs = 
        theTask.getWorkflow().getSecondaryTargetFiles();
   int size1 = stfs == null ? 0 : stfs.size();
   if ((stfCreationState != null && stfCreationState.length() > 0) || size1 > 0)
   {
%>
     <div class="tableContainer" id="data" style="border:solid 1px slategray;">
        <TABLE CELLSPACING="0" CELLPADDING="2" BORDER="0">
        <thead id=scroll>
        <COL> <!-- Icon -->
        
        <TR CLASS="tableHeadingBasic">
           <TD COLSPAN=2><%=labelSecondaryTargetFiles%>
           <SPAN CLASS="smallWhiteItalic">
               (<%=labelClickToView%>)</SPAN>
           </TD>
           <TD WIDTH="20">&nbsp;</TD>
        </TR>
        </thread>
        <TBODY>
        
      <%
      if (Task.IN_PROGRESS.equals(stfCreationState))
      {
      %>
         <TR>
             <TD COLSPAN=2><B><I><%=bundle.getString("lb_cstf_in_progress")%></I></B></TD>
             <TD WIDTH="20">&nbsp;</TD>
         </TR>
         <TR>
             <TD COLSPAN=2 WIDTH="370"><%=bundle.getString("msg_cstf_in_progress")%></TD>
             <TD WIDTH="20">&nbsp;</TD>
         </TR>

      <%
      }
      else if (Task.FAILED.equals(stfCreationState))
      {
         String disableBtn = isPageDetailOne ? "DISABLED" : "";
      %>
        <TR>
            <TD COLSPAN=2><SPAN CLASS="warningText"><B><I><%=bundle.getString("lb_cstf_failed")%></I></B></SPAN></TD>
            <TD WIDTH="20">&nbsp;</TD>
        </TR>
        <TR>
            <TD COLSPAN=2 WIDTH="370"><%=bundle.getString("msg_cstf_failed")%></TD>
            <TD WIDTH="20">&nbsp;</TD>
        </TR>
		<TR>
			<TD>
				<INPUT TYPE=BUTTON  <%=disableBtn%> VALUE="<%= labelCreateStf %>" ONCLICK="location.replace('<%= createStfUrl%>')">
            </TD>
        </TR>
      <%
      }
      else if (stfCreationState == null ||
               Task.COMPLETED.equals(stfCreationState))
      {
            for (SecondaryTargetFile stf : stfs)
            {
                String stfName = stf.getStoragePath();
                String stfPath = WebAppConstants.STF_FILES_URL_MAPPING + stfName;
                stfPath = URLEncoder.encodeUrlStr(stfPath);
    			stfPath = stfPath.replace("%2F", "/");
                String modifiedBy = stf.getModifierUserId();
                modifiedBy = UserHandlerHelper.getUser(modifiedBy).getUserName();
                
                // Get the Last Modified date and format it
                ts.setDate(new Date(stf.getLastUpdatedTime()));

                // Get the file size and format it
                long size = stf.getFileSize();
                long r = size%1024;
                size = (r != 0) ? ((size/1024)+1) : size;  //round up
                %>
                <TR  VALIGN=TOP CLASS="standardText">
                <TD>
                <IMG SRC="<%=bundle.getString("img_file_unextracted")%>"
                    ALT="<%=bundle.getString("lb_file_unextracted")%>"
                    WIDTH=13 HEIGHT=15>
                </TD>
                <TD CLASS="standardText">
                <A CLASS="standardHREF" HREF="<%=stfPath%>" target="_blank"><%=stfName%>
                </A>
                <BR>
                <SPAN CLASS="smallText">
                <%= bundle.getString("lb_last_modified") +  ": " + ts.toString()%> -
                <%= size%>K<BR>
                <%= bundle.getString("lb_modified_by") +  ": " + modifiedBy%>
                </SPAN>
                </TD>
            <%}%>
            </TR>
    <%}%>
      </TBODY>
      </table>
      </DIV>
<% }
%>
<!-- End STFs table -->
</TD>
</TR>
</TABLE>
<BR>
</DIV>
</BODY>
</HTML>
<SCRIPT LANGUAGE = "JavaScript">
$(document).ready(function(){
	$("#taskSecondaryTargetFilesTab").removeClass("tableHeadingListOff");
	$("#taskSecondaryTargetFilesTab").addClass("tableHeadingListOn");
	$("#taskSecondaryTargetFilesTab img:first").attr("src","/globalsight/images/tab_left_blue.gif");
	$("#taskSecondaryTargetFilesTab img:last").attr("src","/globalsight/images/tab_right_blue.gif");
})

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
            for(var i = 0; i < translatedVar.length; i++)
            {
                var objName = "oPara" + i;
                var obj = document.getElementById(objName);
                if(translatedVar[i] < 100){
                	obj.style.color = "red";
                	obj.innerHTML = "(" + translatedVar[i] + "%)";
                }else{
                	obj.style.color = "black";
                	obj.innerHTML = "(" + translatedVar[i] + "%)";
                }
            }
       }
    }
  }
}
function translatedText()
{
   callServer("<%=translatedTextUrl%>");
}
</SCRIPT>