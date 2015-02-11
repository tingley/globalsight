<%@ taglib uri="/WEB-INF/tlds/globalsight.tld" prefix="amb" %>
<%@ page
    contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/activityError.jsp"
    import="
      com.globalsight.everest.costing.AmountOfWork,
      com.globalsight.everest.costing.Rate,
      com.globalsight.everest.foundation.Timestamp,
      com.globalsight.everest.jobhandler.Job,
      com.globalsight.everest.permission.Permission,
      com.globalsight.everest.permission.PermissionSet,
      com.globalsight.everest.page.PageWordCounts,
      com.globalsight.everest.page.PrimaryFile,
      com.globalsight.everest.page.UnextractedFile,
      com.globalsight.everest.page.TargetPage,
      com.globalsight.everest.page.SourcePage,
      com.globalsight.everest.secondarytargetfile.SecondaryTargetFile,
      com.globalsight.everest.servlet.util.ServerProxy,
      com.globalsight.everest.taskmanager.Task,
      com.globalsight.everest.webapp.javabean.NavigationBean,
      com.globalsight.everest.webapp.WebAppConstants,
      com.globalsight.everest.webapp.pagehandler.projects.workflows.JobManagementHandler,
      com.globalsight.everest.webapp.pagehandler.projects.workflows.JobSearchConstants,
      com.globalsight.everest.webapp.pagehandler.projects.workflows.PageComparator,
      com.globalsight.everest.webapp.pagehandler.PageHandler,
      com.globalsight.everest.workflowmanager.Workflow,
      com.globalsight.everest.workflow.Activity,
      com.globalsight.everest.workflow.ConditionNodeTargetInfo,
      com.globalsight.everest.webapp.WebAppConstants,
      com.globalsight.everest.webapp.pagehandler.tasks.TaskHelper,
      com.globalsight.everest.webapp.pagehandler.tasks.TaskDetailHandler,
      com.globalsight.util.edit.EditUtil,
      com.globalsight.config.UserParameter,
      com.globalsight.config.UserParamNames,
      com.globalsight.util.date.DateHelper,
      com.globalsight.everest.page.pageexport.ExportHelper,
      com.globalsight.everest.webapp.pagehandler.administration.comment.CommentConstants,
      com.globalsight.everest.comment.CommentManager,
      com.globalsight.everest.comment.CommentFile,
      com.globalsight.util.AmbFileStoragePathUtils,
      com.globalsight.everest.foundation.User,
      com.globalsight.everest.webapp.pagehandler.administration.customer.download.DownloadFileHandler,
      com.globalsight.everest.util.system.SystemConfiguration,
      com.globalsight.everest.util.system.SystemConfigParamNames,
      com.globalsight.everest.workflowmanager.WorkflowManagerLocal,
      com.globalsight.everest.jobhandler.JobEditionInfo,
      java.util.*,
      javax.servlet.jsp.JspWriter,
      java.text.MessageFormat,
      java.text.NumberFormat,
      java.util.Locale,
      java.io.File,
      java.io.IOException,
      com.globalsight.ling.common.URLEncoder,
      com.globalsight.cxe.entity.fileprofile.FileProfile,
      com.globalsight.cxe.persistence.fileprofile.FileProfilePersistenceManager"
   session="true"
%>
<jsp:useBean id="detail" scope="request"
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
<jsp:useBean id="originalSourceFile" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="taskCommentList" scope="request"
 class="java.util.ArrayList" />
<jsp:useBean id="recreateGS" scope="request"
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
private void printPageLink(JspWriter out, String p_page, String p_url, boolean hasEditPerm)
  throws IOException
{
  // Preserve any MsOffice prefixes: (header) en_US/foo/bar.ppt but
  // show them last so the main file names are grouped together

  String pageName = getMainFileName(p_page);
  String subName = getSubFileName(p_page);

  if (subName != null)
  {
    pageName = pageName + " " + subName;
  }

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
  }
  else {
        out.print(pageName);
  }
}

// Prints short file name with full path in tooltip
private void printPageLinkShort(JspWriter out, String p_page, String p_url, boolean hasEditPerm)
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
    out.print(pageName);
    out.print("'>");
    out.print(shortName);
    out.print("</a>");
  } else {
    out.print(pageName);
  }
}
//TODO should pull up to a common util class
private String qualifyActivity(String activity){
  int index = activity.lastIndexOf("_");
  if(index<0) return activity;
  return activity.substring(0,index);
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
    String title= bundle.getString("lb_activity_details");
    
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
    String labelHours = bundle.getString("lb_hours_capitalized")  + bundle.getString("lb_colon");
    String labelPages = bundle.getString("lb_pages_capitalized")  + bundle.getString("lb_colon");

    String labelDetails = bundle.getString("lb_details");
    String labelWorkoffline = bundle.getString("lb_work_offline");
    String labelComments = bundle.getString("lb_comments");
    String labelContentItem = bundle.getString("lb_primary_target_files");
    String labelClickToOpen = bundle.getString("lb_clk_to_open");
    String labelClickToView = bundle.getString("lb_click_to_view");
    String labelWordCount = bundle.getString("lb_word_count");
    String labelTotalWordCount = bundle.getString("lb_source_word_count_total");


    String labelAccept = bundle.getString("lb_accept");
    String labelReject = bundle.getString("lb_reject");
    String labeltTaskCompleted = bundle.getString("lb_taskcompleted");
    String labelAvailable = bundle.getString("lb_available");
    String labelFinished = bundle.getString("lb_finished");
    String labelRejected = bundle.getString("lb_rejected");
    String labelAccepted = bundle.getString("lb_accepted");
    String labelCreateStf = bundle.getString("lb_cstfs");

    String labelYes = bundle.getString("lb_yes");
    String labelNo = bundle.getString("lb_no");
    String labelSave = bundle.getString("lb_save");
    String labelWordCounts = bundle.getString("lb_detailed_word_counts");

    String labelEditorWarning = bundle.getString("lb_editor_warning");
    String labelFinishWarning = bundle.getString("jsmsg_my_activities_finished");
    String labelSelectionWarning = bundle.getString("jsmsg_my_activities_Warning");
    //use to get the translated text
    StringBuffer tarPageIds = new StringBuffer();

    // used by the pageSearch include
    String lb_filter_text = bundle.getString("lb_target_file_filter");

    //Urls of the links on this page
    String acceptUrl = accept.getPageURL() + "&" + WebAppConstants.TASK_ACTION +
        "=" + WebAppConstants.TASK_ACTION_ACCEPT;
    String rejectUrl = reject.getPageURL();

    String wordCountUrl = wordcountList.getPageURL() + "&action=tpList";

    String pageListUrl = pageList.getPageURL() + "&" + JobManagementHandler.PAGE_SEARCH_PARAM + "=" + thisFileSearch;

    String dtpDownloadURL = dtpDownload.getPageURL();
    String dtpUploadURL = dtpUpload.getPageURL();
    
    String editorParaUrl = editorSameWindow.getPageURL();
    String editorListUrl = editor.getPageURL();
    String editorReviewUrl = editor.getPageURL() +
       "&" + WebAppConstants.REVIEW_MODE + "=true";

    String createStfUrl = accept.getPageURL() + "&" + WebAppConstants.TASK_ACTION +
        "=" + WebAppConstants.TASK_ACTION_CREATE_STF;
    String finishUrl = finish.getPageURL() + "&" + WebAppConstants.TASK_ACTION +
        "=" + WebAppConstants.TASK_ACTION_FINISH;
    String recreateGSUrl = recreateGS.getPageURL() + "&" + WebAppConstants.TASK_ACTION +
        "=" + WebAppConstants.RECREATE_EDITION_JOB;
    String downloadUrl = download.getPageURL();
    String uploadUrl = upload.getPageURL();
    
    String downloadReportUrl = downloadreport.getPageURL();
  

    String dAbbr = bundle.getString("lb_abbreviation_day");
    String hAbbr = bundle.getString("lb_abbreviation_hour");
    String mAbbr = bundle.getString("lb_abbreviation_minute");

    // images to be displayed next to each page/file
    String extractedImage = bundle.getString("img_file_extracted");
    String extractedToolTip = bundle.getString("lb_file_extracted");
    String unExtractedImage = bundle.getString("img_file_unextracted");
    String unExtractedToolTip = bundle.getString("lb_file_unextracted");

    //  Label and its value decided based on the selected state
    //
    String labelABorDBorCODate;
    String valueABorDBorCODate;

    //Get task info
    Locale uiLocale = (Locale)session.getAttribute(WebAppConstants.UILOCALE);
    TimeZone timeZone = (TimeZone)session.getAttribute(WebAppConstants.USER_TIME_ZONE);
    Timestamp ts = new Timestamp(timeZone);
    ts.setLocale(uiLocale);
    Task theTask = (Task)TaskHelper.retrieveObject(
      session, WebAppConstants.WORK_OBJECT);
    String pageId = (String)TaskHelper.retrieveObject(
      session, WebAppConstants.TASK_DETAILPAGE_ID);
    
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

	if (task_type.equals(Task.TYPE_DTP)) {
		tableSize = 360;
    	if (targetPgs.size() < 8)
        	tableSize = targetPgs.size() * 40 + 40;
	}

    String previousUrl = previous.getPageURL() + "&taskStatus=" + state + "&taskId=" + task_id;
    String translatedTextUrl = detail.getPageURL() +
        "&" + WebAppConstants.TASK_ACTION +
        "=" + WebAppConstants.TASK_ACTION_TRANSLATED_TEXT_RETRIEVE;

    String detailUrl = detail.getPageURL() +
        "&" + WebAppConstants.TASK_ACTION +
        "=" + WebAppConstants.TASK_ACTION_RETRIEVE +
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
    //
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
        default:
            break;
    }
    //Fix for GBS-1594
    //disableButtons = disableButtons || (stfCreationState != null &&
      //               stfCreationState.equals(Task.IN_PROGRESS));
	String stfStatusMessage = "null";
	String canComplete = "true";
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
               canComplete = "false";
           }
        }
    }
    //Non-null value for a project manager
    Boolean assigneeValue = (Boolean)TaskHelper.retrieveObject(
        session, WebAppConstants.IS_ASSIGNEE);
    boolean isAssignee = assigneeValue == null ? true :
        assigneeValue.booleanValue();
    boolean enableComment = !isAssignee || (isAssignee && state == Task.STATE_ACCEPTED);

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
    
    PermissionSet perms = (PermissionSet) session.getAttribute(
                        WebAppConstants.PERMISSIONS);
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
    //Create the downloadLink for the download button
    StringBuffer downloadLink = new StringBuffer("/globalsight/ControlServlet" +
                                "?linkName=jobDownload&pageName=TK2" + 
                                "&firstEntry=true");
    downloadLink.append("&");
    downloadLink.append(DownloadFileHandler.PARAM_JOB_ID);
    downloadLink.append("=");
    downloadLink.append(jobId);
    downloadLink.append("&");
    downloadLink.append(DownloadFileHandler.PARAM_WORKFLOW_ID);
    downloadLink.append("=");
    downloadLink.append(theTask.getWorkflow().getId());

    UserParameter param = PageHandler.getUserParameter(session,
      UserParamNames.PAGENAME_DISPLAY);
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
<SCRIPT SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<SCRIPT SRC="/globalsight/includes/modalDialog.js"></SCRIPT>
<%@ include file="/envoy/common/warning.jspIncl" %>
<link rel="STYLESHEET" type="text/css" href="/globalsight/includes/ContextMenu.css">
<script src="/globalsight/includes/ContextMenu.js"></script>
<script src="/globalsight/includes/ieemu.js"></script>
<SCRIPT SRC="/globalsight/includes/xmlextras.js"></SCRIPT>
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
</style>
<%@ include file="/includes/compatibility.jspIncl" %>
<SCRIPT>
var dirty = false;
var objectName = "";
var guideNode = "myActivities";
var w_editor = null;
var b_editInSameWindow = eval("<%=editInSameWindow%>");
var b_canEditInSameWindow = eval("<%=canEditInSameWindow%>");
var b_isReviewActivity = eval("<%=isReviewActivity%>");
var needWarning = false;
var helpFile = "<%=helpFile%>";

var conditionUrls = new Array();
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
    if(navigator.userAgent.indexOf("MSIE")==-1)
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
      var inlineUrl =  "<%=editorParaUrl%>" + url;
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

    w_editor = null;
}

function warnAboutRejectBeforeAcceptance(url)
{
   if (confirm('<%=bundle.getString("lb_reject_warning_before_accept")%>')) {
	   location.replace(url);
   }
}

function doReject(urlSent)
{
   var rmsg = '<%=bundle.getString("lb_reject_warning_after_accept")%>';
   
   if (confirm(rmsg)) {
    if (!canClose())
    {
        cancelEvent();
        raiseSegmentEditor();
    }
    else
    {
        location.replace(urlSent);
    }
   }
}

function keepZero()
{
   // check Hours (When Present)
   if (document.DetailsForm.<%= hoursParam %> != null)
   {
	if(document.DetailsForm.<%= hoursParam %>.value == 0)
	{
	  return confirm("<%=bundle.getString("jsmsg_activity_hours_zero")%>");
	}
   }
   // Check Pages ( when Present )
   if (document.DetailsForm.<%= pagesParam %> != null)
   {
	if(document.DetailsForm.<%= pagesParam %>.value == 0)
	{
	  return confirm("<%=bundle.getString("jsmsg_activity_pages_zero")%>");
	}
   }
   return true;
}

function recreateGSEdition(urlSent) {
    location.replace(urlSent);
}

function doFinished(urlSent)
{
	var stfStatusMessage = "<%=stfStatusMessage%>";
    var canComplete = "<%=canComplete%>";
	if(stfStatusMessage!="null"&&canComplete=="false")
	{
        if(stfStatusMessage=="inprogress")
        {
            alert("<%=bundle.getString("jsmsg_my_activities_cannotcomplete_inprogress")%>");
        }
        else if(stfStatusMessage=="failed")
        {
            alert("<%=bundle.getString("jsmsg_my_activities_cannotcomplete_failed")%>");
        }
		return false;
	}
    if(!checkDelayTime())
    {
        return false;
    }
    if(dirty)
    {
        alert( "<%=bundle.getString("jsmsg_activity_details_unsaved")%>");
        return false;
    }
    if(!keepZero())
    {
	return false;
    }

    if (!canClose())
    {
        cancelEvent();
        raiseSegmentEditor();
    }
    else
    {
        var checked = false;
        var selectedRadioBtn = null;
        if (DetailsForm.RadioBtn != null)
        {
           // If more than one radio button is displayed, the length
           // attribute of the radio button array will be non-zero,
           // so find which one is checked
           if (DetailsForm.RadioBtn.length)
           {
               for (i = 0; !checked && i < DetailsForm.RadioBtn.length; i++)
               {
                   if (DetailsForm.RadioBtn[i].checked == true)
                   {
                       checked = true;
                       selectedRadioBtn = DetailsForm.RadioBtn[i].value;
                   }
               }
           }
           // If only one is displayed, there is no radio button array, so
           // just check if the single radio button is checked
           else
           {
               if (DetailsForm.RadioBtn.checked == true)
               {
                   checked = true;
                   selectedRadioBtn = DetailsForm.RadioBtn.value;
               }
           }
           urlSent = conditionUrls[selectedRadioBtn];
        }
        else
        {
           checked = true;
        }

        if (!checked)
        {
           alert("<%= labelSelectionWarning %>");
           return false;
        }
        else
        {
            var warningMessage = '<%=labelFinishWarning%>';
		    if(stfStatusMessage!="null")
			{
		    	if(stfStatusMessage=="inprogress")
		        {
		    		warningMessage = '<%=bundle.getString("jsmsg_my_activities_str_inprogress")%>';
		        }
		        else if(stfStatusMessage=="failed")
		        {
		        	warningMessage = '<%=bundle.getString("jsmsg_my_activities_str_failed")%>';
		        }
			}
            if (! b_isReviewActivity) {
                //check if there are open issues with these target pages, if so,
                //then change the warning message to indicate that.
                <%
                    StringBuffer theURL = new StringBuffer(queryOpenIssuesXml.getPageURL());
                    theURL.append("&taskId=").append(task_id);
                    theURL.append(targetPageIdParameter.toString());
                    theURL.append("&date=").append(System.currentTimeMillis());
                %>
                var theURL = '<%=theURL.toString()%>';
                var dom = XmlDocument.create();
                dom.preserveWhiteSpace = true;
                dom.async = false;
                dom.load(theURL);
                var root = dom.selectSingleNode("/openTaskIssues");
                if (root !=null) {
                   //GBS-344, firefox compatibility
                   var targetPages = null;
                   targetPages = root.selectNodes("./targetPage");
                   /*if(window.navigator.userAgent.indexOf("Firefox")>0){
                      targetPages = dom.selectNodes("./targetPage",root);
                   } else {
                      targetPages = root.selectNodes("./targetPage");
                   }*/
                    if (targetPages.length > 0) {
                        //there is at least one page that has open issues
                        warningMessage= '<%=bundle.getString("msg_task_finish_warning_openissue") %>';
                        if(stfStatusMessage!="null")
			            {
                        	if(stfStatusMessage=="inprogress")
            		        {
            		    		warningMessage = '<%=bundle.getString("msg_task_finish_warning_openissue_stfinprogress")%>';
            		        }
            		        else if(stfStatusMessage=="failed")
            		        {
            		        	warningMessage = '<%=bundle.getString("msg_task_finish_warning_openissue_stffailed")%>';
            		        }
			            }
                        for (var i = 0; i < targetPages.length; i++) {
                            var m_userAgent = navigator.userAgent;
                            var isIE = m_userAgent.indexOf("compatible") > -1 && m_userAgent.indexOf("MSIE") > -1 && m_userAgent.indexOf("Opera") == -1;
                            if(isIE)
                            {
                                warningMessage += unescape(targetPages[i].text) + '\r\n';
                            }
                            else
                            {
                                warningMessage += unescape(targetPages[i].textContent) + '\r\n';
                            }
                        } //endfor
                     }//endif
                } //endif
            }
            if (confirm(warningMessage))
            {
                document.location.replace(urlSent);
            }
        }
    }
}
function submitDtpForm(form, buttonClicked, linkParam)
{
	  if (buttonClicked == "DtpDownload")
	  {
        form.action = "<%=dtpDownloadURL%>&taskAction=<%=WebAppConstants.DTP_DOWNLOAD%>" + linkParam;
    		form.submit();
    		return;
    }
    else if (buttonClicked == "DtpUpload")
    {
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

function submitForm(buttonClicked)
{
    if (buttonClicked == "wordcounts")
    {
        DetailsForm.action = "<%= wordCountUrl %>";
        DetailsForm.submit();
        return;
    }
    if(!validate())
    {
        return false;
    }

    if (document.layers)
    {

        theForm = document.contentLayer.document.DetailsForm;
    }
    else
    {
        theForm = document.all.DetailsForm;
    }

    theForm.action = "<%= saveUrl %>";
    theForm.submit();
}

function clearDirty()
{
    dirty = false;
}

function setDirty()
{
    dirty = true;
}

function validate()
{
    if(dirty)
    {
        // Check hours ( when present )
        if (document.DetailsForm.<%= hoursParam %> != null)
        {
            if(document.DetailsForm.<%= hoursParam %>.value == "")
            {
               alert("<%=bundle.getString("jsmsg_invalid_activity_hours")%>");
               return false;
            }

             if(isNaN(document.DetailsForm.<%= hoursParam %>.value))
             {
                 alert("<%=bundle.getString("jsmsg_invalid_activity_hours")%>");
                 return false;
             }
        }
        // Check Pages ( when present )
        if (document.DetailsForm.<%= pagesParam %> != null)
        {
            if(document.DetailsForm.<%= pagesParam %>.value == "")
            {
               alert("<%=bundle.getString("jsmsg_invalid_activity_pages")%>");
               return false;
            }

             if(isNaN(document.DetailsForm.<%= pagesParam %>.value))
             {
                 alert("<%=bundle.getString("jsmsg_invalid_activity_pages")%>");
                 return false;
             }

             if(Math.round(document.DetailsForm.<%= pagesParam %>.value) !=
                document.DetailsForm.<%= pagesParam %>.value)
             {
                 alert("<%=bundle.getString("jsmsg_invalid_activity_pages")%>");
                 return false;
             }
        }
    }
    return true;
}

function doOnload()
{
  ContextMenu.intializeContextMenu();
  loadGuides();
}

function checkDelayTime()
{
    var start_time = <%=startTime%>;
    var currentTime = <%=new Date().getTime()%>;
    var usedTime = (currentTime - start_time)/1000;
    var delayTime = <%=sessionMgr.getAttribute(SystemConfigParamNames.TASK_COMPLETE_DELAY_TIME)%>;
    var leftTime = parseInt(delayTime - usedTime);
    if(leftTime > 0)
    {
        alert("<%=bundle.getString("msg_task_complete_time") %>".replace("%1", delayTime).replace("%2", leftTime));
        return false;
    }
    return true;
}

function checkDownloadDelayTime()
{
    var start_time = <%=startExportTime%>;
    var currentTime = <%=new Date().getTime()%>;
    var usedTime = (currentTime - start_time)/1000;
    var delayTime = <%=sessionMgr.getAttribute(SystemConfigParamNames.DOWNLOAD_JOB_DELAY_TIME)%>;
    var leftTime = parseInt(delayTime - usedTime);
    if(leftTime > 0)
    {
        alert("<%=bundle.getString("msg_task_download_time") %>".replace("%1", delayTime).replace("%2", leftTime));
        return false;
    }
    return true;
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
<SPAN CLASS="mainHeading"><%=labelJob%> <%=jobName%></SPAN>
<P></P>

<TABLE CELLPADDING=0 CELLSPACING=0 BORDER=0 CLASS=standardText>
  <TR>
    <TD WIDTH=500><%=bundle.getString("helper_text_task_detail")%></TD>
  </TR>
</TABLE>
<P></P>

<TABLE CELLPADDING="0" CELLSPACING="0" BORDER="0">
<TR>
<TD COLSPAN="3">

<TABLE CELLPADDING="0" CELLSPACING="0" BORDER="0">
<TR>
<TD>
<!-- Tabs table -->
<TABLE CELLPADDING="0" CELLSPACING="0" BORDER="0">
<TR>

        <TD CLASS="tableHeadingListOn"><IMG SRC="/globalsight/images/tab_left_blue.gif" BORDER="0"><A CLASS="sortHREFWhite" HREF="<%=detailUrl%>"><%=labelDetails%></A><IMG SRC="/globalsight/images/tab_right_blue.gif" BORDER="0"></TD>
        <TD WIDTH="2"></TD>
        
        <% 
        boolean isShowComments = perms.getPermissionFor(Permission.ACTIVITIES_JOB_COMMENTS_VIEW) || perms.getPermissionFor(Permission.ACTIVITIES_COMMENTS_VIEW);
        if(isShowComments)
        {
        %>
        <TD CLASS="tableHeadingListOff"><IMG SRC="/globalsight/images/tab_left_gray.gif" BORDER="0"><A CLASS="sortHREFWhite" HREF="<%=commentUrl%>"><%=labelComments%></A><IMG SRC="/globalsight/images/tab_right_gray.gif" BORDER="0"></TD>
        <%} %>
        
        <TD WIDTH="2"></TD>
        <%

        perms = (PermissionSet) session.getAttribute(
                        WebAppConstants.PERMISSIONS);
        boolean workoffline = perms.getPermissionFor(Permission.ACTIVITIES_WORKOFFLINE);
        //Print tabs for detail page two
        if (!isPageDetailOne)
        {
            //The download tab and behaviour
            if (!disableButtons && workoffline)
            {
                
                if (review_only)
                {
                %>
                	 <amb:permission name="<%=Permission.REPORTS_LANGUAGE_SIGN_OFF%>" >
                <%
                	 out.print("<TD CLASS=\"tableHeadingListOff\"><IMG SRC=\"/globalsight/images/tab_left_gray.gif\" BORDER=\"0\">");
                	 out.print("<A CLASS=\"sortHREFWhite\" HREF=\"" + downloadReportUrl +
                          "\">" + labelWorkoffline + "</A>");
                     out.print("<IMG SRC=\"/globalsight/images/tab_right_gray.gif\" BORDER=\"0\"></TD>");
                	 out.print("<TD WIDTH=\"2\"></TD>");
                %>
                	 </amb:permission>
                <%
                }
                else
                {
                	  out.print("<TD CLASS=\"tableHeadingListOff\"><IMG SRC=\"/globalsight/images/tab_left_gray.gif\" BORDER=\"0\">");
                	  out.print("<A CLASS=\"sortHREFWhite\" HREF=\"" + downloadUrl +
                          "\">" + labelWorkoffline + "</A>");
                      out.print("<IMG SRC=\"/globalsight/images/tab_right_gray.gif\" BORDER=\"0\"></TD>");
                	  out.print("<TD WIDTH=\"2\"></TD>");
                }
              

               

            }
        }
        %>
</TR>
</TABLE>
<!-- End Tabs table -->
</TD>
<TD ALIGN="RIGHT" VALIGN="BOTTOM" NOWRAP></TD>
</TR>

<TR>
<TD CLASS="tableHeadingBasic" COLSPAN="2" HEIGHT=1><IMG SRC="/globalsight/images/spacer.gif" HEIGHT="1" WIDTH="1"></TD>
</TR>

<TR>
<TD COLSPAN="2">&nbsp;</TD>
</TR>

<TR>
<TD COLSPAN="2">

<!-- Lower table -->
<TABLE CELLPADDING="0" CELLSPACING="0" BORDER="0">
<TR>
<TD VALIGN="TOP">
<!-- Details table -->
    <FORM NAME="DetailsForm" ACTION='<%= detailUrl%>' METHOD="POST">
    <TABLE CELLPADDING="2" CELLSPACING="0" BORDER="0" CLASS="detailText" width="400px">
        <TR VALIGN="TOP">
        <TD CLASS="tableHeadingBasic" COLSPAN="2">
        <%= labelDetails %>
        </TD>
        </TR>
        <TR VALIGN="TOP">
            <TD style="width:150px"><B><%= labelJobName %></B></TD>
            <TD style="word-wrap:break-word;word-break:break-all;width:200px">
            <SCRIPT LANGUAGE = "JavaScript">
            if (navigator.userAgent.indexOf("Firefox") >= 0){
            	document.write("<DIV style=\'width:200px\'>");
            }
			</SCRIPT>
            <%= jobName %>
            <SCRIPT LANGUAGE = "JavaScript">
            if (navigator.userAgent.indexOf("Firefox") >= 0){
            	document.write("</DIV>")
            }</SCRIPT>
            </TD>
        </TR>
        <TR VALIGN="TOP">
            <TD><B><%= labelJobId %></B></TD>
            <TD><%= jobId %></TD>
        </TR>
        <TR VALIGN="TOP">
            <TD><B><%= labelActivity %></B></TD>
            <TD><%= activityName %></TD>
        </TR>
        <TR VALIGN="TOP">
            <TD><B><%= labelCompany %></B></TD>
            <TD><%= companyName %></TD>
        </TR>
        <TR VALIGN="TOP">
            <TD><B><%= labelProject %></B></TD>
            <TD><%= projName %></TD>
        </TR>
        <TR VALIGN="TOP">
            <TD><B><%= labelProjectManager %></B></TD>
            <TD><%= projManager %></TD>
        </TR>
        <TR VALIGN="TOP">
            <TD><B><%= labelTotalWordCount %>:</B></TD>
            <TD><%= sourceWordCount %></TD>
        </TR>
        <TR VALIGN="TOP">
            <TD><B><%= labelPriority %></B></TD>
            <TD><%= priority %></TD>
        </TR>
        <TR VALIGN="TOP">
            <TD><B><%= labelSourceLocale %></B></TD>
            <TD><%= sourceLocale %></TD>
        </TR>
        <TR VALIGN="TOP">
            <TD><B><%= labelTargetLocale %></B></TD>
            <TD><%= targetLocale %></TD>
        </TR>
        <TR VALIGN="TOP">
            <TD><B><%= labelABorDBorCODate %></B></TD>
            <TD><%= valueABorDBorCODate %></TD>
        </TR>
        <%
            if (state == Task.STATE_ACTIVE) {
                out.println("<TR VALIGN=TOP>");
                out.println("<TD><B>" + labelTimeToComplete +
                            "</B></TD>");
                out.println("<TD>" + duration + "</TD>");
                out.println("</TR>");
            }
        %>
        <TR VALIGN="TOP">
            <TD><B><%= labelOverdue %></B></TD>
            <TD><%
            // Overdue column
            if ((state == stateAvailable && dt.after(theTask.getEstimatedAcceptanceDate())) ||
                (state == stateInProgress && dt.after(theTask.getEstimatedCompletionDate())))
            {
                out.print("<SPAN CLASS=warningText>" + labelYes + "</SPAN>");
            }
            else
            {
                out.print(labelNo);
            }
            %>
            </TD>
        </TR>
        <TR VALIGN="TOP">
            <TD><B><%= labelStatus %></B></TD>
            <TD><%= status %></TD>
        </TR>
        <TR VALIGN="TOP">
            <TD COLSPAN="3">&nbsp;&nbsp;&nbsp;</TD>
        </TR>
        <TR VALIGN="TOP">
        <amb:permission name="<%=Permission.ACTIVITIES_COMMENTS_VIEW%>" >
        <TD COLSPAN="3" CLASS="detailText">
        <%
            String openSegments = "<B><A HREF='" + commentUrl + "'>" + openSegmentComments + "</A></B>";
            String closedSegments = "<B><A HREF='" + commentUrl + "'>" + closedSegmentComments + "</A></B>";
            Object[] args = {openSegments, closedSegments}; 
            out.println(MessageFormat.format(bundle.getString("lb_segment_comments"),
                                                                                args)); 
            out.println("<BR>");
        %>
        </TD>
        </amb:permission>
        </TR>
<% if(isHourlyJobCosting)
   {
        // Get current hourly value:
        // We already confirmed that the task does have an hourly rate
        String taskHours = "0.0";
        AmountOfWork aow = theTask.getAmountOfWork(Rate.UnitOfWork.HOURLY);
        if (aow != null)
        {
            // convert to just 2 decimal
            long val = Math.round(aow.getActualAmount()*100); // cents
            taskHours = Double.toString(val/100.0d);
        }
%>
        <TR>
            <TD><B><%= labelHours %></B></TD>
            <TD id="hoursElement">
                <INPUT TYPE="TEXT" onchange="setDirty();" SIZE="5" MAXLENGTH="10"
                NAME="<%= hoursParam %>" CLASS="standardText"
                VALUE="<%= taskHours %>"></INPUT>
            </TD>
        </TR>
<% } %>
<% if(isPageBasedJobCosting)
   {
        // Get page value:
        String taskPages = "0";
        AmountOfWork aow = theTask.getAmountOfWork(Rate.UnitOfWork.PAGE_COUNT);
        if(aow != null)
        {
            Double pages = new Double(aow.getActualAmount());
            taskPages = pages.toString();
        }
%>
        <TR>
            <TD><B><%= labelPages %></B></TD>
            <TD id="pagesElement">
                <INPUT TYPE="TEXT" onchange="setDirty();" SIZE="5" MAXLENGTH="10"
                NAME="<%= pagesParam %>" CLASS="standardText"
                VALUE="<%= taskPages %>"></INPUT>
            </TD>
        </TR>
<% } %>
    </TABLE>    
<!-- End Details table -->

    <BR>

<%
    //Print buttons for detail page one
    perms = (PermissionSet) session.getAttribute(
                    WebAppConstants.PERMISSIONS);
    //boolean hasPerm = perms.getPermissionFor(Permission.ACTIVITIES_ACCEPT);
    if(isPageDetailOne)
    {
        if (alreadyAccepted)
        {
            out.println("<SPAN CLASS=\"warningText\">" +
                bundle.getString("msg_already_accepted") + "</SPAN>");
        }
        else
        {
            	if(!disableButtons && perms.getPermissionFor(Permission.ACTIVITIES_ACCEPT)) {            		
                	out.println("<INPUT TYPE=BUTTON VALUE=\"" + labelAccept + "\" ONCLICK=\"location.replace('" +
                    	acceptUrl + "')\">");
            	}
            	if(!disableButtons && perms.getPermissionFor(Permission.ACTIVITIES_REJECT_BEFORE_ACCEPTING)) { 
                	out.println("<INPUT TYPE=BUTTON VALUE=\"" + labelReject + "\" ONCLICK=\"warnAboutRejectBeforeAcceptance('" +
                    	rejectUrl + "')\">");
                }
        }
    }
    else // Print links for page two
    {
        if (!disableButtons)
        {
            List condNodeInfo = theTask.getConditionNodeTargetInfos();
            String targetUrl = finishUrl;
            if (condNodeInfo != null)
            {
            %>
                <!-- Data Table -->
                <TABLE BORDER="0" CELLSPACING="0" CELLPADDING="3" WIDTH="180" CLASS="detailText">
                <TD COLSPAN="3"><B> <%= labelSelectActivity %></B></TD>
                    <TBODY>
                        <COL WIDTH=10>  <!-- Radio button -->
                        <COL WIDTH=100>  <!-- Column 1 -->
                        <%
                           int listSize = condNodeInfo == null ? 0 :
                                          condNodeInfo.size();
                           String color = "#FFFFFF";
                           //select the first radio button by default
                           String checked = "";//"checked";

                           for (int i=0; i < listSize; i++)
                           {
                              //String color = (i%2 == 0) ? "#FFFFFF" : "#EEEEEE";
                              ConditionNodeTargetInfo cti =
                                (ConditionNodeTargetInfo)condNodeInfo.get(i);

                              String targetNodeName = cti.getTargetNodeName();
                              String arrowName = cti.getArrowName();

                              targetUrl = finishUrl + "&arrow=" + arrowName;
                              //out.println("<TR STYLE=\"padding-bottom: 5px; padding-top: 5px;\" VALIGN=TOP BGCOLOR="+color+">");
                              out.println("<TR>");
                              out.println("<TD NOWRAP VALIGN=\"TOP\"><INPUT TYPE=RADIO NAME=RadioBtn "+
                                 checked +" VALUE=\""+i+"\"></TD>");
                              //checked = "";
                         %>
                         <SCRIPT LANGUAGE="JavaScript1.2">
                            conditionUrls[<%=i%>] = "<%=targetUrl%>";

                        </script>
                         <%
                              out.println("<TD>"+ targetNodeName + "  (" +arrowName+")</TD>");
                              out.println("</TR>");
                           }
                         %>
                    </TBODY>
                </TABLE><BR>
<%          }
            if(perms.getPermissionFor(Permission.ACTIVITIES_REJECT_AFTER_ACCEPTING)) {
            	out.println("<INPUT TYPE=BUTTON VALUE=\"" + labelReject + "\" ONCLICK=\"doReject('" +
                	rejectUrl + "'); return false;\">");
            }
            out.println("<INPUT TYPE=BUTTON VALUE=\"" + labeltTaskCompleted + "\" ONCLICK=\"doFinished('" +
                finishUrl+ "'); return false;\">");
        }
    }
    if (!disableButtons &&
       (perms.getPermissionFor(Permission.ACTIVITIES_EXPORT) ||
        (state == stateInProgress && perms.getPermissionFor(Permission.ACTIVITIES_EXPORT_INPROGRESS))))
    {
            out.println("<INPUT TYPE=BUTTON NAME=ExportButton VALUE=\"" +
                bundle.getString("lb_export") + "...\" ONCLICK=\"location.replace('" +
                exportLink.toString() + "')\">");
    }
    
    if(Task.STATE_REDEAY_DISPATCH_GSEDTION == state) {
        out.println("<INPUT TYPE=BUTTON NAME=ReCreateGSEditionJobButton VALUE=\"" +
                    bundle.getString("lb_recreate_edition_job") + "...\" ONCLICK=\"recreateGSEdition('" +
                    recreateGSUrl + "')\">");
    }

%>
<BR>
<BR>
<%
//<!-- For the issue of "add download button to my activities" -->

    if (!disableButtons &&
       (perms.getPermissionFor(Permission.ACTIVITIES_DOWNLOAD)) )
    {
            out.println("<INPUT TYPE=BUTTON NAME=DownloadButton VALUE=\"" +
                    bundle.getString("lb_download") + "...\" ONCLICK=\"if(checkDownloadDelayTime()){location.replace('" +
                    downloadLink.toString() + "')}\">");
    }

//<!-- End -->
%>

<%
//<!-- For the issue of "add 'Back to Activities' button" -->
     out.println("<INPUT TYPE=BUTTON NAME=\'previous\' VALUE=\"" +
                   bundle.getString("lb_back_to_activities") +
                   "\" ONCLICK=\"location.replace('" +
                   previousUrl+
                   "')\">" );

//<!-- End -->

%>

<% if(isHourlyJobCosting || isPageBasedJobCosting) { %>
    <INPUT type=BUTTON onclick="submitForm('<%= labelSave %>');" value='<%= labelSave %>'>
<% } %>

</TD>
<TD WIDTH="30">&nbsp;</TD>
<TD VALIGN="TOP">
</FORM> 
<!-- End of Details Form The following jspIncl also have forms -->

<!-- Pages table -->
    <amb:permission name="<%=Permission.ACTIVITIES_FILES_VIEW%>" >

    <%@ include file="/envoy/projects/workflows/pageSort.jspIncl" %>
    <% 
    if (task_type.equals(Task.TYPE_TRANSLATION))
    {
    %>
    <%@ include file="/envoy/projects/workflows/pageSearch.jspIncl" %>
    <% 
    }
    %>
    
    <div class="tableContainer" id="data" style="height:200px">
    <%
    if (task_type.equals(Task.TYPE_TRANSLATION))
    {
    %>
    <TABLE CELLSPACING="0" CELLPADDING="2" BORDER="0" width="100%">
    <thead id=scroll>
        <TR CLASS="tableHeadingBasic" >
            <TD COLSPAN=2 style="padding-left: 8px; padding-top: 2px; padding-bottom: 2px"><A CLASS="sortHREFWhite" HREF="<%=pageListUrl + "&" + JobManagementHandler.PAGE_SORT_PARAM + "=" + PageComparator.EXTERNAL_PAGE_ID%>">
            <%=labelContentItem%>
            <SPAN CLASS="smallWhiteItalic">
            (<%=labelClickToOpen%>)</SPAN></A><%=pageNameSortArrow%></TD>
            <TD WIDTH="20%" style="padding-left: 8px; padding-top: 2px; padding-bottom: 2px">&nbsp;</TD>
            <TD style="padding-left: 8px; padding-top: 2px; padding-bottom: 2px"> <A CLASS="sortHREFWhite" HREF="<%=pageListUrl + "&" + JobManagementHandler.PAGE_SORT_PARAM + "=" + PageComparator.WORD_COUNT%>">
            <%=labelWordCount%></A><%=wordCountSortArrow%></TD>
            <TD style="padding-left: 8px; padding-top: 2px; padding-bottom: 2px"><%=bundle.getString("lb_source")%></TD>
            <TD>
            <INPUT type='BUTTON' onclick='translatedText();' style="width:110px;height:27px" value='<%=bundle.getString("lb_task_translated_text")%>'></TD>
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
                    pageName = unextractedFile.getStoragePath();
                    pageUrl = WebAppConstants.UNEXTRACTED_FILES_URL_MAPPING +
                      pageName;
                    modifiedBy = unextractedFile.getLastModifiedBy();

                    // Get the Last Modified date and format it
                    Date date = unextractedFile.getLastModifiedDate();
                    ts.setDate(date);
                    dateStr = ts.toString();

                    // Get the file size and format it
                    fileSize = unextractedFile.getLength();
                    long r = fileSize%1024;
                    fileSize = (r != 0) ? ((fileSize/1024)+1) : fileSize;  //round up
                }
                out.println("<TR VALIGN=TOP>");
                %>
                <TD>
                <IMG SRC="<%=isExtracted ? extractedImage : unExtractedImage%>"
                    title="<%=isExtracted ? extractedToolTip : unExtractedToolTip%>"
                    WIDTH=13 HEIGHT=15>
                </TD>
                <%

                if (isExtracted)
                {
                    out.print("<TD style='word-wrap : break-word;word-break:break-all; width:70%'>");

                    // print page name and editor link
                    if (pagenameDisplay.equals(UserParamNames.PAGENAME_DISPLAY_FULL))
                    {
                      printPageLink(out, pageName, pageUrl, hasEditPerm);
                    }
                    else if (pagenameDisplay.equals(UserParamNames.PAGENAME_DISPLAY_SHORT))
                    {
                      printPageLinkShort(out, pageName, pageUrl, hasEditPerm);
                    }

                    out.print("</TD>");
                }
                else
                {
                %>
                    <TD CLASS="standardText" style="word-break : break-all; overflow:hidden; ">
                    <A CLASS="standardHREF" HREF="<%=pageUrl%>" target="_blank"><%=pageName%>
                    </A>
                    <BR>
                    <SPAN CLASS="smallText">
                    <%out.print(bundle.getString("lb_last_modified") +  ": " + dateStr);%> -
                    <%out.print(fileSize);%>K<BR>
                    <%out.print(bundle.getString("lb_modified_by") +  ": " + modifiedBy);%>
                    </SPAN>
                    </TD>
                <%
                }
                %>
                    <TD WIDTH="20%">&nbsp;</TD>
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
                    <TD ALIGN="CENTER"><SPAN CLASS="standardText"><P ID="<%="oPara" + i%>"></P></SPAN></TD>
            <%
            }
            if(tarPageIds.length() != 0)
            {
               translatedTextUrl = translatedTextUrl + "&" + TaskDetailHandler.TASK_PAGE_IDS + "=" + tarPageIds.toString();
            }
        }
        %>
        </TR>
	</tbody>
	</TABLE>
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

                String fileName = getFileName(pageName);
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
                  <TD WIDTH="20">&nbsp;</TD>
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
            %>
    </tbody>
</TABLE>
<%
    }
%>
</div>
</amb:permission>
<P>
<amb:permission name="<%=Permission.ACTIVITIES_FILES_VIEW%>" >
<INPUT type=BUTTON onclick='submitForm("wordcounts");' value='<%= labelWordCounts %>...'>
</amb:permission>
<!-- End Pages table -->


<!-- STFs table -->
<% 
boolean isShowSecondaryTargetFile = !perms.getPermissionFor(Permission.ACTIVITIES_SECONDARYTARGETFILE);
if(isShowSecondaryTargetFile)
{
   List stfs = theTask.getWorkflow().getSecondaryTargetFiles();
   int size1 = stfs == null ? 0 : stfs.size();
   if ((stfCreationState != null && stfCreationState.length() > 0) || size1 > 0)
   {
%>        
   	 	<BR>
        <BR>
 
     <div class="tableContainer" id="data" style="height:100px;">
        <TABLE CELLSPACING="0" CELLPADDING="2" BORDER="0">
        <thead id=scroll>
        <COL> <!-- Icon -->
        
        <TR CLASS="tableHeadingBasic">
           <TD COLSPAN=2><%=bundle.getString("lb_secondary_target_files")%>
           <SPAN CLASS="smallWhiteItalic">
               (<%=labelClickToView%>)</SPAN>
           </TD>
           <TD WIDTH="20">&nbsp;</TD>
        </TR>
        </THEAD>
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

      <%
         out.println("<TR><TD><INPUT TYPE=BUTTON "+ disableBtn +" VALUE=\"" + labelCreateStf + "\" ONCLICK=\"location.replace('" +
                 createStfUrl + "')\"></TD></TR>");
      }
      else if (stfCreationState == null ||
               Task.COMPLETED.equals(stfCreationState))
      {
            for (int i = 0; i < size1; i++)
            {
                SecondaryTargetFile stf = (SecondaryTargetFile)stfs.get(i);
                String stfName = stf.getStoragePath();
                String stfPath = WebAppConstants.STF_FILES_URL_MAPPING + stfName;
                stfPath = URLEncoder.encodeUrlStr(stfPath);
    			stfPath = stfPath.replace("%2F", "/");
                String modifiedBy = stf.getModifierUserId();

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
                <%out.print(bundle.getString("lb_last_modified") +  ": " + ts.toString());%> -
                <%out.print(size);%>K<BR>
                <%out.print(bundle.getString("lb_modified_by") +  ": " + modifiedBy);%>
                </SPAN>
                </TD>
            <%}%>
            </TR>
    <%}%>
      </TBODY>
      </table>
      </DIV>
<% }
}
%>
<!-- End STFs table -->

<!-- Original Source File -->
<%
WorkflowManagerLocal wml =  new WorkflowManagerLocal();
JobEditionInfo je = wml.getGSEditionJobByJobID(Long.parseLong(jobId));
if(je != null) {
%>
<BR><BR>
    <Div class="tableContainer" id="data" style="height:100%;width:100%">
    <TABLE CELLSPACING="0" CELLPADDING="2" BORDER="0" width=800>
    <THEAD id=scroll>
      <COL> <!-- Icon -->
        
      <TR CLASS="tableHeadingBasic">
        <TD COLSPAN=2 style="width: 100%;"><%=bundle.getString("lb_original_source_file_heading")%>
          <SPAN CLASS="smallWhiteItalic">(<%=labelClickToView%>)</SPAN>
        </TD>
     </TR>
   </THEAD>
   <tbody>
       <%
       StringBuffer fileStorageRoot = new StringBuffer(SystemConfiguration
                .getInstance().getStringParameter(
                        SystemConfigParamNames.FILE_STORAGE_DIR));

       fileStorageRoot = fileStorageRoot.append(File.separator).append(
                WebAppConstants.VIRTUALDIR_TOPLEVEL).append(File.separator).append(
                WebAppConstants.ORIGINAL_SORUCE_FILE).append(File.separator)
                .append(jobName).append(File.separator).append(theTask.getTargetLocale());
                
       File parentFilePath = new File(fileStorageRoot.toString());
       File[] files = parentFilePath.listFiles();
        
    	 if (files != null && files.length > 0){
        	 for (int i=0; i<files.length; i++) {
        			File file = files[i];
        			String fileName = file.getName();
        			String fileLink = originalSourceFile.getPageURL() + 
        			    "&action=downloadSourceFile&local="+theTask.getTargetLocale() + 
        			    "&fileName=" + fileName + "&jobName=" +  jobName;
        			fileLink = URLEncoder.encodeUrlStr(fileLink);
        			fileLink = fileLink.replace("%2F", "/");
       %>
        			<TR  VALIGN=TOP CLASS="standardText">
            			
                  <TD CLASS="standardText">
                  <IMG SRC="<%=unExtractedImage%>"
                    ALT="<%=bundle.getString("lb_original_source_file_heading")%>"  
                    WIDTH=13 HEIGHT=15>
                  <A CLASS="standardHREF" HREF="<%=fileLink%>" target="_blank"><%=fileName%>
                  </A>
                  </TD>
             </TR>  
       <%
           }
       }
       %>
        
    </TBODY>
	</TABLE>
</DIV>
<%}%>

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
        <BR><BR>
        <div class="tableContainer" id="data">
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
            String spName = unextractedSrc.getStoragePath();
            String spUrl = WebAppConstants.UNEXTRACTED_FILES_URL_MAPPING +
                           spName;
            spUrl = URLEncoder.encodeUrlStr(spUrl);
    		spUrl = spUrl.replace("%2F", "/");
            String modifier = unextractedSrc.getLastModifiedBy();

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
            <%out.print(bundle.getString("lb_last_modified") +  ": " + ts.toString());%> -
            <%out.print(sourceFileSize);%>K<BR>
            <%out.print(bundle.getString("lb_modified_by") +  ": " + modifier);%>
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

<!-- Task Comments data table -->
<BR><BR>
<div class="tableContainer2" id="data" style="height:100%;">
<FORM name="CommentForm" method="post">
<TABLE CELLSPACING="0" CELLPADDING="2" BORDER="0">
  <tr STYLE="font-size:12px;font-family:Arial, Helvetica, sans-serif ">
    <td>
      <b><%=bundle.getString("lb_activity")%>
      <%=bundle.getString("lb_comments")%>: </b>
      <%=theTask.getTaskDisplayName()%>
    </td>
  </tr>
  <tr STYLE="font-size:12px;font-family:Arial, Helvetica, sans-serif ">
    <td><%=theTask.getTargetLocale().getDisplayName()%></td>
  </tr>
  <tr valign="top">
    <td align="right">
      <amb:tableNav bean="taskCommentList"
      key="<%=CommentConstants.TASK_COMMENT_KEY%>"
      pageUrl="comment" />
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
            <amb:column label="lb_comment_creator" width="100px">
                <%=commentObj.getCreatorId()%>
            </amb:column>
            <amb:column label="lb_date_created" width="100px">
                <%=commentObj.getCreatedDate()%>
            </amb:column>
            <amb:column label="lb_comments" width="350px" style="word-wrap:break-word;word-break:break-all">
<%				out.print("<SCRIPT language=\"javascript\">if (navigator.userAgent.indexOf(\'Firefox\') >= 0){document.write(\"<DIV style=\'width:350px\'>\");}</SCRIPT>"); %>
                <%
                    String com = commentObj.getComment();
                    if (com.length() > 200)
                    {
                    	int idx = com.indexOf(' ', 200);
                        if (idx > 0)
                            com = com.substring(0, idx);
                        out.println(com);
                        out.println("<div onclick=\"javascript:showComment('t" + commentObj.getId() + "');\" style=\"cursor:pointer\">[more...]</div>");
                        out.println("<div id=t" + commentObj.getId() + " class=\"comment\">" + commentObj.getComment() + "<div onclick=closeComment('t" + commentObj.getId() + "');><span style=\"cursor: pointer; color:blue\">[Close]</span></div></div>");
                    }
                    else
                        out.print(commentObj.getComment());
                %>
<%				out.print("<SCRIPT language=\"javascript\">if (navigator.userAgent.indexOf(\'Firefox\') >= 0){document.write(\"</DIV>\")}</SCRIPT>"); %>
            </amb:column>
            <amb:column label="lb_attached_files" width="200px" style="word-wrap:break-word;word-break:break-all">

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
                     String path = "";
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
						
<%						out.print("<SCRIPT language=\"javascript\">if (navigator.userAgent.indexOf(\'Firefox\') >= 0){document.write(\"<DIV style=\'width:200px\'>\");}</SCRIPT>"); %>
                        <IMG SRC="/globalsight/images/file_paperclip.gif" ALT="<%=bundle.getString("lb_reference_file")%>" HEIGHT=15 WIDTH=13>

<%
path = "/globalsight/".concat(AmbFileStoragePathUtils.COMMENT_REFERENCE_SUB_DIR).concat(File.separator).concat(commentId);
path += File.separator.concat(file.getFileAccess()).concat(File.separator).concat(file.getFilename());
path = URLEncoder.encodeUrlStr(path);
%>
                        <A class="standardHREF" target="_blank" href="<%=path %>">
                        <%=EditUtil.encodeHtmlEntities(file.getFilename())%>
                        </A>
<%						out.print("<SCRIPT language=\"javascript\">if (navigator.userAgent.indexOf(\'Firefox\') >= 0){document.write(\"</DIV>\")}</SCRIPT>"); %>
                        
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
                        <br>
<%
                    }
                  }
%>
        </amb:column>
      </amb:table>
    </td>
  </tr>
  <tr><td>&nbsp;</td></tr>
  <TR>
    <TD align=right>
      <P>
      <%if (enableComment){%>
      <amb:permission name="<%=Permission.ACTIVITIES_COMMENTS_EDIT%>" >
      <INPUT TYPE="BUTTON" VALUE="<%=editButton%>" onClick="submitCommentForm('Edit');" <%= (taskCommentList==null||taskCommentList.size()==0)?"DISABLED":""%>>
      </amb:permission>
      <amb:permission name="<%=Permission.ACTIVITIES_COMMENTS_NEW%>" >
      <INPUT TYPE="BUTTON" VALUE="<%=newButton%>" onClick="submitCommentForm('New');">
      </amb:permission>
      <%}%>
    </TD>
  </TR>
</TABLE>
</FORM>
</DIV>
<!-- End Task Comments data table -->

</TD>
</TR>
</TABLE>
<!-- End Lower table -->
</TD>
</TR>
</TABLE>
</DIV>

</BODY>
</HTML>
<SCRIPT LANGUAGE = "JavaScript">
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
                obj.innerHTML = "(" + translatedVar[i] + "%)";
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