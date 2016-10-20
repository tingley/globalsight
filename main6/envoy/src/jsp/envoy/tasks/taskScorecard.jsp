<%@ taglib uri="/WEB-INF/tlds/globalsight.tld" prefix="amb" %>
<%@ page
    contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/activityError.jsp"
    import="
      com.globalsight.config.UserParamNames,      
      com.globalsight.config.UserParameter,
      com.globalsight.cxe.entity.fileprofile.FileProfile,
      com.globalsight.everest.comment.CommentFile,
      com.globalsight.everest.comment.CommentManagerLocal,      
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
      com.globalsight.everest.webapp.pagehandler.administration.comment.CommentUploadHandler,
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
      com.globalsight.everest.webapp.pagehandler.tasks.TaskDetailHandler,  
      com.globalsight.everest.webapp.pagehandler.administration.company.Select,    
      com.globalsight.everest.webapp.pagehandler.tasks.TaskHelper,
      com.globalsight.everest.webapp.pagehandler.administration.mtprofile.MTProfileHandlerHelper,
      com.globalsight.everest.projecthandler.MachineTranslationProfile,
      com.globalsight.everest.qachecks.QACheckerHelper,
      com.globalsight.everest.qachecks.DITAQACheckerHelper,
      com.globalsight.everest.workflow.Activity,
      com.globalsight.everest.workflow.ConditionNodeTargetInfo,
      com.globalsight.everest.workflowmanager.Workflow,
      com.globalsight.everest.workflowmanager.WorkflowManagerLocal,
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
 <jsp:useBean id="pageSearch" scope="request"
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
//colors to use for the table background
private static final String WHITE_BG         = "#FFFFFF";
private static final String LT_GREY_BG       = "#EEEEEE";
// Toggles the background color of the rows used between WHITE and LT_GREY
private static String toggleBgColor(int p_rowNumber)
{
    return p_rowNumber % 2 == 0 ? WHITE_BG : LT_GREY_BG;  
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
    String title= bundle.getString("lb_scorecard");
    
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
    String labelActivitiesCommentUploadCheckWarningMessage = bundle.getString("jsmsg_my_activities_comment_upload_check");
    int isActivityCommentUploadCheck = taskImpl.getIsActivityCommentUploadCheck();
    int isActivityCommentUploaded = 0;
    ArrayList<CommentFile> cf =  ServerProxy.getCommentManager().getActivityCommentAttachments(theTask);
    if(cf != null && cf.size()>0)
    {
        isActivityCommentUploaded =1;
    }
    WorkflowImpl workflowImpl = (WorkflowImpl) theTask.getWorkflow();
    ProjectImpl project = (ProjectImpl)theTask.getWorkflow().getJob().getProject();
    int scorecardShowType = workflowImpl.getScorecardShowType();
    boolean needScore = false;
    boolean needDQF = false;
    boolean showScore = false, showDQF = false;
    String scorecardComment = (String)sessionMgr.getAttribute("scorecardComment");
    String dqfComment = (String)sessionMgr.getAttribute("dqfComment");
    if (theTask.isType(Task.TYPE_REVIEW) || theTask.isType(Task.TYPE_REVIEW_EDITABLE)) 
    {
        if (scorecardShowType == 1 || scorecardShowType == 3) 
        {
            //Scorecard
            needScore = StringUtil.isEmpty(scorecardComment);
        } else if (scorecardShowType == 3 || scorecardShowType == 5) 
        {
            needDQF = StringUtil.isEmpty(dqfComment);
        }
    }
    switch (scorecardShowType) {
        case -1: 
            break;
        case 0:
        case 1:
            showScore = true;
            break;
        case 2:
        case 3:
            showScore = true;
            showDQF = true;
            break;
        case 4:
        case 5:
            showDQF = true;
            break;
        default:
            break;
    }
    
    if(theTask.isType(Task.TYPE_REVIEW))
    {
    	labelReportUploadCheckWarning = "Reviewer Comments Report not uploaded";
    	labelReportUploadCheckWarningMessage = bundle.getString("jsmsg_my_activities_reviewer_comments_report_upload_check");
    }
    String labelReportQAChecks = bundle.getString("lb_activity_qa_checks");
    boolean showQAChecksTab = QACheckerHelper.isShowQAChecksTab(theTask);
    boolean showDITAQAChecksTab = DITAQACheckerHelper.isShowDITAChecksTab(theTask);
    
   	boolean isCheckUnTranslatedSegments = project.isCheckUnTranslatedSegments();
    //Urls of the links on this page
    String acceptUrl = accept.getPageURL() + "&" + WebAppConstants.TASK_ACTION +
        "=" + WebAppConstants.TASK_ACTION_ACCEPT+
      	//GBS-2913 Added to the url parameter taskId
        "&"+WebAppConstants.TASK_ID+"="+theTask.getId();

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
    boolean review_editable = theTask.isType(Task.TYPE_REVIEW_EDITABLE);
     
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
    
    String saveScorecardUrl = taskScorecard.getPageURL() +
	    "&" + WebAppConstants.TASK_ACTION +
	    "=" + WebAppConstants.TASK_ACTION_SAVE_SCORECARD +
	    "&" + WebAppConstants.TASK_STATE +
	    "=" + state +
	    "&" + WebAppConstants.TASK_ID +
	    "=" + task_id;

    String saveDQFUrl = taskScorecard.getPageURL() +
	    "&" + WebAppConstants.TASK_ACTION +
	    "=" + WebAppConstants.TASK_ACTION_SAVE_DQF +
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
    exportLink.append(workflowId);
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
    downloadLink.append(workflowId);
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

    String helpFile = bundle.getString("help_activity_scorecard");
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
    int rowNum = 0;
    HashMap<String, Integer> scorecardMap = (HashMap<String,Integer>)sessionMgr.getAttribute("scorecard");
    List<Select> scorecardCategories = (List<Select>)sessionMgr.getAttribute("scorecardCategories");
    boolean isScored = (Boolean)sessionMgr.getAttribute("isScored");
    boolean isDQFDone = (Boolean)sessionMgr.getAttribute("isDQFDone");
    int categoryNum = scorecardMap.keySet().size();

    String labelLeverageMT = bundle.getString("lb_leverage_mt");

    String leverageMTUrl = accept.getPageURL() + "&" + WebAppConstants.TASK_ACTION +
        "=leverageMT" + "&" + WebAppConstants.TASK_ID + "=" + theTask.getId();
    
	boolean hasMtProfile = false;
    MachineTranslationProfile mtProfile = MTProfileHandlerHelper.getMtProfileByL10nProfile(
            theJob.getL10nProfile(), workflowImpl.getTargetLocale());
    if (mtProfile != null && mtProfile.isActive())
    {
        hasMtProfile = true;
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

</SCRIPT>
<%@ include file="/envoy/common/shortcutIcon.jspIncl" %>
</HEAD>
<BODY LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0" MARGINWIDTH="0" MARGINHEIGHT="0"
    onbeforeunload="return doBeforeUnload()" onload="doOnload()" onunload="doUnload()" id="idBody">
<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>
<DIV ID="contentLayer" STYLE=" POSITION: ABSOLUTE; Z-INDEX: 9; TOP: 108px; LEFT: 20px; RIGHT: 20px;">
<%@ include file="/envoy/tasks/includeTaskSummaryTabs.jspIncl" %>
<!-- Lower table -->
<div id="scorecardPanel">
<% if (showScore) { %>
<form METHOD="post" name="scorecardForm" action="<%=saveScorecardUrl%>">
<SPAN CLASS="standardText"><p>Translation Scorecard, 1 is poor and 5 is excellent.</p></SPAN>
<TABLE CLASS="standardText" CELLSPACING="0" CELLPADDING="2" style="border:solid 1px slategray;">
<TR CLASS="tableHeadingBasic">
    <TD style="padding-top: 8px; padding-bottom: 8px;text-align:center;width:240px;">Scorecard Category</TD>
    <TD style="width:600px;text-align:center;">Score</TD>
</TR>
<%
	for(Select category: scorecardCategories)
	{%>
		<TR BGCOLOR="<%=toggleBgColor(rowNum++)%>" CLASS="standardText">
		<TD style="padding-top: 8px; padding-bottom: 8px;text-align:left;width:240px;"><%=category.getValue()%></TD> 
		<TD style="width:600px;text-align:left;">
		<input type="radio" name="<%=category.getValue()%>" value="1" <%if(scorecardMap.get(category.getValue()) == 1){%>checked<%}%> <%=isScored ? "disabled" : ""%>>1 
		<input type="radio" name="<%=category.getValue()%>" style="margin-left:45px" value="2" <%if(scorecardMap.get(category.getValue()) == 2){%>checked<%}%> <%=isScored ? "disabled" : ""%>>2 
		<input type="radio" name="<%=category.getValue()%>" style="margin-left:45px" value="3" <%if(scorecardMap.get(category.getValue()) == 3){%>checked<%}%> <%=isScored ? "disabled" : ""%>>3 
		<input type="radio" name="<%=category.getValue()%>" style="margin-left:45px" value="4" <%if(scorecardMap.get(category.getValue()) == 4){%>checked<%}%> <%=isScored ? "disabled" : ""%>>4 
		<input type="radio" name="<%=category.getValue()%>" style="margin-left:45px" value="5" <%if(scorecardMap.get(category.getValue()) == 5){%>checked<%}%> <%=isScored ? "disabled" : ""%>>5
		</TD>
		</TR>
	<%}

%>
<TR BGCOLOR="<%=toggleBgColor(rowNum++)%>" CLASS="standardText">
	<TD style="padding-top: 8px; padding-bottom: 8px;text-align:left;width:240px;"><%=bundle.getString("lb_comment")%></TD>
	<TD style="width:600px;text-align:left;"><TEXTAREA name="scoreComment" id="scoreComment" maxlength="495" style="resize: none;height:80px;width:80%;" <%=isScored ? "disabled" : ""%>><%=scorecardComment %></TEXTAREA></TD>
</TR>
</TABLE>
<br>
    <%if(!isScored){ %>
    <input type="button" value="<%=bundle.getString("lb_save") %>" onclick="submitForm()"/>
    <%} %>
</form>
<% } %>
</div>
<br>
<div id="dqfPanel">
<% if (showDQF) { %>
<form id="dqfForm" name="dqfForm" method="post" action="<%=saveDQFUrl%>">
<SPAN CLASS="standardText"><p><%=bundle.getString("lb_dqf_evaluation") %></p></SPAN>
<TABLE CLASS="standardText" CELLSPACING="0" CELLPADDING="2" style="border:solid 1px slategray;">
<TR CLASS="tableHeadingBasic">
    <TD style="padding-top: 8px; padding-bottom: 8px;text-align:center;width:240px;word-wrap:break-word;">DQF Category</TD>
    <TD style="width:600px;text-align:center;">Score</TD>
</TR>
<%
List<String> fluencyCategories = (List<String>) sessionMgr.getAttribute("fluencyCategories");
List<String> adequacyCategories = (List<String>)sessionMgr.getAttribute("adequacyCategories");
String fluencyScore = (String)sessionMgr.getAttribute("fluencyScore");
if (StringUtil.isEmpty(fluencyScore)) fluencyScore = "";
String adequacyScore = (String)sessionMgr.getAttribute("adequacyScore");
if (StringUtil.isEmpty(adequacyScore)) adequacyScore = "";
if (StringUtil.isEmpty(dqfComment)) dqfComment = "";
%>
<tr bgcolor="#FFFFFF" class="standardText">
    <td style="padding-top: 8px; padding-bottom: 8px;text-align:left;width:240px;word-wrap:break-word;">
        <b><%=bundle.getString("lb_dqf_fluency_only") %></b><br>
        <%=bundle.getString("helper_text_dqf_fluency_note") %>
    </td>
    <td style="width:600px;text-align:left;">
        <% for (String s : fluencyCategories) { %>
        <input type="radio" id="fluencyScore" name="fluencyScore" value="<%=s %>" <%=s.equals(fluencyScore) ? "checked" : "" %> <%=isDQFDone ? "disabled" : ""%>><%=s %></input>
        <% } %>
    </td>
</tr>
<tr bgcolor="#EEEEEE" class="standardText">
    <td style="padding-top: 8px; padding-bottom: 8px;text-align:left;width:240px;word-wrap:break-word;">
        <b><%=bundle.getString("lb_dqf_adequacy_only") %></b><br>
        <%=bundle.getString("helper_text_dqf_adequacy_note") %>
    </td>
    <td style="width:600px;text-align:left;">
        <% for (String s : adequacyCategories) { %>
        <input type="radio" id="adequacyScore" name="adequacyScore" value="<%=s %>" <%=s.equals(adequacyScore) ? "checked" : "" %> <%=isDQFDone ? "disabled" : ""%>><%=s %></input>
        <% } %>
    </td>
</tr>
<tr bgcolor="#FFFFFF" class="standardText">
    <td>
        <%=bundle.getString("lb_comment") %>
    </td>
    <td>
        <textarea name="dqfComment" id="dqfComment" maxlength="495" style="resize: none;height:80px;width:80%;" <%=isDQFDone ? "disabled" : ""%>><%=dqfComment %></textarea>
    </td>
</tr>
</table>
<br>
<% if (!isDQFDone) { %>
    <input type="button" id="saveDQFBtn" name="saveDQFBtn" value="<%=bundle.getString("lb_save") %>" onclick="saveDQF()" />
<% } %>
</form>
<% } %>
</div>
<br>
<!-- End Lower table -->
</DIV>
<!--// Task Completed Dialog  -->
</BODY>
</HTML>
<SCRIPT LANGUAGE = "JavaScript">
$(document).ready(function(){
	$("#tasktaskScorecardTabTab").removeClass("tableHeadingListOff");
	$("#taskScorecardTab").addClass("tableHeadingListOn");
	$("#taskScorecardTab img:first").attr("src","/globalsight/images/tab_left_blue.gif");
	$("#taskScorecardTab img:last").attr("src","/globalsight/images/tab_right_blue.gif");
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

function submitForm()
{
	var allChecked = true;
	var i= 0;
	$('#scorecardPanel :input:radio').each(function(){
		if($(this).is(':checked') && $(this).attr("name") != "RadioBtn")
		{
			i++;
		}
	})
	if(i != <%=categoryNum%>)
	{
		alert('Please score all the options.');
		return;
	}

	if($("#scoreComment").val().trim() == '')
	{
		alert('Please fill in your comment.');
		return;
	}

	scorecardForm.submit();
}

function saveDQF() {
	var i=0;
    $('#dqfPanel :input:radio').each(function(){
        if($(this).is(':checked'))
        {
            i++;
        }
    })
    if(i != 2)
    {
        alert('Please score all DQF fields first.');
        return false;
    }
	
    var comment = $("#dqfComment").val();
    if ($.trim(comment) == "") {
        alert("Please fill in your DQF comment first.");
        return false;
    }
    $("#dqfForm").submit();
}
</SCRIPT>