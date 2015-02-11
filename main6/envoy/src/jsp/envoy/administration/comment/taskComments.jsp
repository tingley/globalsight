<%@page import="java.io.File"%><%@page import="com.globalsight.ling.common.URLEncoder"%>

<%@ taglib uri="/WEB-INF/tlds/globalsight.tld" prefix="amb" %>
<%@ page contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="java.util.*,com.globalsight.everest.webapp.javabean.NavigationBean,
         com.globalsight.util.edit.EditUtil,
         com.globalsight.util.GlobalSightLocale,
         com.globalsight.everest.taskmanager.Task,
         com.globalsight.util.resourcebundle.ResourceBundleConstants,
         com.globalsight.util.resourcebundle.SystemResourceBundle,
         com.globalsight.everest.jobhandler.Job,
         com.globalsight.everest.comment.Comment,
         com.globalsight.everest.comment.CommentFile,
         com.globalsight.everest.comment.CommentUpload,
         com.globalsight.everest.comment.CommentManager,
         com.globalsight.everest.page.TargetPage,
         com.globalsight.everest.permission.Permission,
         com.globalsight.everest.permission.PermissionSet,
         com.globalsight.everest.servlet.util.ServerProxy,
         com.globalsight.everest.webapp.tags.TableConstants,
         com.globalsight.everest.webapp.pagehandler.PageHandler,
         com.globalsight.everest.webapp.pagehandler.tasks.TaskHelper,
         com.globalsight.everest.webapp.pagehandler.administration.comment.LocaleCommentsSummary,
         com.globalsight.everest.webapp.pagehandler.administration.comment.LocaleCommentsComparator,
         com.globalsight.everest.webapp.pagehandler.administration.comment.PageCommentsSummary,
         com.globalsight.everest.webapp.pagehandler.administration.comment.CommentConstants,
         com.globalsight.everest.webapp.pagehandler.projects.workflows.JobManagementHandler,
         com.globalsight.everest.util.comparator.CommentComparator,
         com.globalsight.everest.foundation.User,
         com.globalsight.everest.webapp.pagehandler.administration.users.UserUtil,
         com.globalsight.util.AmbFileStoragePathUtils,
         java.text.MessageFormat,
         java.text.NumberFormat,
         java.util.Locale, java.util.ResourceBundle,
         java.util.List,
         java.util.ArrayList"
         session="true"
%>
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
<jsp:useBean id="downloadreport" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<%
    ResourceBundle bundle = PageHandler.getBundle(session);
    SessionManager sessionMgr =
      (SessionManager)session.getAttribute(WebAppConstants.SESSION_MANAGER);
    PermissionSet perms =
      (PermissionSet) session.getAttribute(WebAppConstants.PERMISSIONS);
    String addcommentUrl = addcomment.getPageURL() + "&action=addcomment";
    String editcommentUrl = editcomment.getPageURL() + "&action=editcomment";
	//Get task info
    Task task = (Task)TaskHelper.retrieveObject(
      session, WebAppConstants.WORK_OBJECT);
    String downloadcommentUrl = downloadcomment.getPageURL() + "&action=downloadFiles"
	+ "&" + JobManagementHandler.JOB_ID
	+ "=" + task.getJobId();;
    
    boolean review_only = task.isType(Task.TYPE_REVIEW);
    
    String pageId = (String)TaskHelper.retrieveObject(
      session, WebAppConstants.TASK_DETAILPAGE_ID);

    int state = task.getState();
    long task_id = task.getId();
    //Labels of the page
    String labelDetails = bundle.getString("lb_details");
    String labelWorkoffline = bundle.getString("lb_work_offline");
    String labelComments = bundle.getString("lb_comments");

    //Urls of the links on this page
    String commentUrl = comment.getPageURL();
    String downloadUrl = download.getPageURL();
    String uploadUrl = upload.getPageURL();
    String segcommentsUrl = segmentComments.getPageURL();

    String detailUrl = detail.getPageURL() +
        "&" + WebAppConstants.TASK_ACTION +
        "=" + WebAppConstants.TASK_ACTION_RETRIEVE +
        "&" + WebAppConstants.TASK_STATE +
        "=" + state +
        "&" + WebAppConstants.TASK_ID +
        "=" + task_id;
	String downloadReportUrl = downloadreport.getPageURL();
    boolean alreadyAccepted = false;
    boolean disableButtons = false;
    boolean isPageDetailOne = TaskHelper.DETAIL_PAGE_1.equals(pageId);

    //  Majority of cases it is Due Date
    //
    switch (state)
    {
        case Task.STATE_ACCEPTED:
            isPageDetailOne = false;
            break;
        case Task.STATE_COMPLETED:
            disableButtons = true;
            break;
        case Task.STATE_REJECTED:
            disableButtons = true;
            break;
        case Task.STATE_DEACTIVE:
            alreadyAccepted = true;
            break;
        default:
            break;
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

    String title= bundle.getString("lb_comments");

    // Labels of the column titles
    String creatorCol = bundle.getString("lb_comment_creator");
    String dateCol = bundle.getString("lb_date_created");
    String commentsCol = bundle.getString("lb_comments");
    String attachmentCol = bundle.getString("lb_attached_files");

    String labelJobName =  bundle.getString("lb_job") + bundle.getString("lb_colon");
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
<SCRIPT SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>

<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>
<SCRIPT LANGUAGE="JavaScript">
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
        CommentForm.action = "/globalsight/ControlServlet?linkName=addcomment&pageName=JOBCOMMENTS&action=addcomment";
        CommentForm.submit();
        return;
    }

    if (radio == -1)
    {
        alert("<%= bundle.getString("jsmsg_select_comment") %>");
        return false;
    }    
    
    CommentForm.action = "/globalsight/ControlServlet?linkName=editcomment&pageName=JOBCOMMENTS&action=editcomment";
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
<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>
<DIV ID="contentLayer" STYLE=" POSITION: ABSOLUTE; Z-INDEX: 9; TOP: 108; LEFT: 20px; RIGHT: 20px;">
<SPAN CLASS="mainHeading"><%=labelJobName%> <%=jobName%></SPAN>

<p>
<!-- Tabs table -->
<TABLE CELLPADDING="0" CELLSPACING="0" BORDER="0">
  <TR>
    <TD CLASS="tableHeadingListOff"><IMG SRC="/globalsight/images/tab_left_gray.gif" BORDER="0"><A CLASS="sortHREFWhite" HREF="<%=detailUrl%>"><%=labelDetails%></A><IMG SRC="/globalsight/images/tab_right_gray.gif" BORDER="0"></TD>
    <TD WIDTH="2"></TD>
    <TD CLASS="tableHeadingListOn"><IMG SRC="/globalsight/images/tab_left_blue.gif" BORDER="0"><A CLASS="sortHREFWhite" HREF="<%=commentUrl%>"><%=labelComments%></A><IMG SRC="/globalsight/images/tab_right_blue.gif" BORDER="0"></TD>
    <TD WIDTH="2"></TD>
        <%

        //Print tabs for detail page two
        if (!isPageDetailOne)
        {
            // userPerms defined in navigation.jspIncl
            boolean hasDownloadPerm = userPerms.getPermissionFor(Permission.ACTIVITIES_WORKOFFLINE);
            // The download tab and behaviour
            if (!disableButtons && hasDownloadPerm)
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
    </TD>
  </TR>
</TABLE>
<!-- End Tabs table -->

<form name="CommentForm" method="post">
<input type="hidden" name="saveCommStatus">
<!-- Job Comments data table -->

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
                        out.println(com);
                        out.println("<div onclick=\"javascript:showComment('j" + jobComment.getId() + "');\" style=\"cursor:hand\">[more...]</div>");
                        out.println("<div id=j" + jobComment.getId() + " class=\"comment\">" + jobComment.getComment() + "<div onclick=closeComment('j" + jobComment.getId() + "');><span style=\"cursor: hand; color:blue\">[Close]</span></div></div>");
                    }
                    else
                        out.println(jobComment.getComment());
                %>
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
  	 <td align="right" >
		 <%--for gbs-2599
		 amb:permission name="<%=Permission.ACTIVITIES_JOB_COMMENTS_DOWNLOAD%>" >
		          <A CLASS="standardHREF" HREF="#"
		              onClick="doCheckAll('checkboxBtn'); return false;"
		              onFocus="this.blur();">CheckAll</A> |
			  	  <A CLASS="standardHREF" HREF="#"
		              onClick="doClearAll('checkboxBtn'); return false;"
		              onFocus="this.blur();">ClearAll</A>
		 </amb:permission--%>
		 <amb:permission name="<%=Permission.ACTIVITIES_JOB_COMMENTS_EDIT%>" >
            <INPUT TYPE="BUTTON" id="jobCommEditBtn" VALUE="<%=editButton%>" disabled onClick="submitFormForJob('Edit');">
         </amb:permission>
         <amb:permission name="<%=Permission.ACTIVITIES_JOB_COMMENTS_NEW%>" >
            <INPUT TYPE="BUTTON" VALUE="<%=newButton%>" onClick="submitFormForJob('New');">
         </amb:permission>
	 </td>
  </tr>
</table>
<p>
</amb:permission>

<!-- Task Comments data table -->
<table cellpadding=0 cellspacing=0 border=0 class="standardText" width="80%" style="min-width:1024px;">
  <tr>
    <td>
      <b><%=bundle.getString("lb_activity")%><%=bundle.getString("lb_comments")%>: </b><%=task.getTaskDisplayName()%>
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
                        out.println(com);
                        out.println("<div onclick=\"javascript:showComment('t" + commentObj.getId() + "');\" style=\"cursor:hand\">[more...]</div>");
                        out.println("<div id=t" + commentObj.getId() + " class=\"comment\">" + commentObj.getComment() + "<div onclick=closeComment('t" + commentObj.getId() + "');><span style=\"cursor: hand; color:blue\">[Close]</span></div></div>");
                    }
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
  <tr><td>&nbsp;</td></tr>
  <TR>
    <TD align=right>
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
    <TD>
      <!-- Segment Comments data table -->
      <%@ include file="/envoy/administration/comment/taskSegmentTable.jspIncl" %>
    </TD>
  </TR>
</TABLE>
</FORM>
</BODY>
</HTML>
