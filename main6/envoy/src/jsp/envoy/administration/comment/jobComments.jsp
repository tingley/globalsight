<%@page import="java.io.File"%><%@page import="com.globalsight.ling.common.URLEncoder"%>

<%@ taglib uri="/WEB-INF/tlds/globalsight.tld" prefix="amb" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ page contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="java.util.*,com.globalsight.everest.webapp.javabean.NavigationBean,
         com.globalsight.util.GlobalSightLocale,
         com.globalsight.util.edit.EditUtil,
         com.globalsight.util.resourcebundle.ResourceBundleConstants,
         com.globalsight.util.resourcebundle.SystemResourceBundle,
         com.globalsight.everest.jobhandler.Job,
         com.globalsight.everest.comment.Comment,
         com.globalsight.everest.comment.CommentFile,
         com.globalsight.everest.comment.CommentUpload,
         com.globalsight.everest.comment.CommentManager,
         com.globalsight.everest.comment.TaskCommentInfo,
         com.globalsight.everest.page.TargetPage,
         com.globalsight.everest.permission.Permission,
         com.globalsight.everest.permission.PermissionSet,
         com.globalsight.everest.servlet.util.ServerProxy,
         com.globalsight.everest.webapp.pagehandler.PageHandler,
         com.globalsight.everest.webapp.tags.TableConstants,
         com.globalsight.everest.webapp.pagehandler.administration.comment.CommentConstants,
         com.globalsight.everest.webapp.pagehandler.administration.comment.LocaleCommentsSummary,
         com.globalsight.everest.webapp.pagehandler.administration.comment.LocaleCommentsComparator,
         com.globalsight.everest.webapp.pagehandler.administration.comment.PageCommentsSummary,
         com.globalsight.everest.webapp.pagehandler.projects.workflows.JobManagementHandler,
         com.globalsight.everest.util.comparator.CommentComparator,
         com.globalsight.everest.util.comparator.JavaLocaleComparator,
         com.globalsight.everest.util.comparator.TaskCommentInfoComparator,
         com.globalsight.everest.foundation.User,
         com.globalsight.everest.webapp.pagehandler.administration.users.UserUtil,
         com.globalsight.util.AmbFileStoragePathUtils,
         java.text.MessageFormat,
         java.text.NumberFormat,
         java.util.Locale, java.util.ResourceBundle" 
         session="true" %>
<jsp:useBean id="skinbean" scope="application" class="com.globalsight.everest.webapp.javabean.SkinBean" />
<jsp:useBean id="addcomment" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="editcomment" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="downloadcomment" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="jobDetailsPDFs" scope="request" class="com.globalsight.everest.webapp.javabean.NavigationBean"/>
<jsp:useBean id="jobComments" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="jobDetails" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="jobAttributes" scope="request" class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="jobReports" scope="request" class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="segmentComments" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="jobCommentList" class="java.util.ArrayList" scope="request"/>
<jsp:useBean id="taskCommentList" class="java.util.ArrayList" scope="request"/>
<jsp:useBean id="segmentCommentList" class="java.util.ArrayList" scope="request"/>
<jsp:useBean id="jobSourceFiles" scope="request" class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="jobCosts" scope="request" class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="editPages" scope="request" class="com.globalsight.everest.webapp.javabean.NavigationBean" />
 <jsp:useBean id="jobScorecard" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />

<% 
	// Set which page to back to after drilling down into segment comments
	session.setAttribute("segmentCommentsBackPage","jobComments");
    ResourceBundle bundle = PageHandler.getBundle(session);
    SessionManager sessionMgr =
      (SessionManager)session.getAttribute(WebAppConstants.SESSION_MANAGER);
    PermissionSet perms = (PermissionSet) session.getAttribute(WebAppConstants.PERMISSIONS);
	String downloadcommentUrl = downloadcomment.getPageURL() + "&action=downloadFiles"
									+ "&" + JobManagementHandler.JOB_ID
									+ "=" + sessionMgr.getAttribute(JobManagementHandler.JOB_ID);
    String addcommentUrl = addcomment.getPageURL() + "&action=addcomment"
    								//GBS-2913 enable tabbed browsing,add jobID
						    		+ "&" + JobManagementHandler.JOB_ID
						            + "=" + sessionMgr.getAttribute(JobManagementHandler.JOB_ID)
						            + "&toJob=ture";
    String editcommentUrl = editcomment.getPageURL() + "&action=editcomment"
					    		//GBS-2913 enable tabbed browsing,add jobID
					    		+ "&" + JobManagementHandler.JOB_ID
					            + "=" + sessionMgr.getAttribute(JobManagementHandler.JOB_ID)
					            + "&toJob=ture";
    String jobDetailsURL = jobDetails.getPageURL()
                             + "&" + JobManagementHandler.JOB_ID
                             + "=" + sessionMgr.getAttribute(JobManagementHandler.JOB_ID);
    String jobAttributesURL = jobAttributes.getPageURL() 
                             + "&" + JobManagementHandler.JOB_ID 
                             + "=" + sessionMgr.getAttribute(JobManagementHandler.JOB_ID);
    String jobCommentsURL = jobComments.getPageURL()
                             + "&" + JobManagementHandler.JOB_ID
                             + "=" + sessionMgr.getAttribute(JobManagementHandler.JOB_ID);
    String jobReportsURL = jobReports.getPageURL() 
    						 + "&" + JobManagementHandler.JOB_ID 
    						 + "=" + sessionMgr.getAttribute(JobManagementHandler.JOB_ID);
    String segcommentsUrl = segmentComments.getPageURL();

    String title= bundle.getString("lb_comments");

    // tab labels
    String labelDetails = bundle.getString("lb_details");
    String labelComments = bundle.getString("lb_comments");;


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

    // For task comments table
    ArrayList targLocales = (ArrayList)sessionMgr.getAttribute("targetLocales");
    String selectedLocale = (String)request.getAttribute("selectedLocale");

    // For segment comments table
    ArrayList segmentTargLocales = (ArrayList)sessionMgr.getAttribute("targetLocalesForSegments");
    String segmentSelectedLocale = (String)request.getAttribute("segmentSelectedLocale");
    
    String path = "";

%>
<html>
<!-- This JSP is envoy\administration\comment\jobComments.jsp -->
<head>
<META HTTP-EQUIV="content-type" CONTENT="text/html;charset=UTF-8">
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
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<SCRIPT SRC="/globalsight/includes/radioButtons.js"></SCRIPT>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/jquery/jquery-1.6.4.min.js"></SCRIPT>

<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>
<SCRIPT LANGUAGE="JavaScript">
    var needWarning = false;
    var guideNode = "myJobs";
    var objectName = "";
    var helpFile = "<%=bundle.getString("help_job_comments")%>";

function enableButtons(buttonName)
{
    if (buttonName == 'editBtn') {
        CommentForm.editBtn.disabled = false;
        if (CommentForm.editActBtn != null)
        CommentForm.editActBtn.disabled = true;        
    } else {
        CommentForm.editActBtn.disabled = false;        
        if (CommentForm.editBtn != null)
        CommentForm.editBtn.disabled = true;
    }
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
    		var showLocaleSelectObj = document.getElementById('showLocaleSelect');
    		var localeValue = showLocaleSelectObj.options[showLocaleSelectObj.selectedIndex].value;
    		
	    	CommentForm.action = "<%=downloadcommentUrl%>&localeValue=" + localeValue;
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

function getSelectedRadio(buttonGroup)
{
   // returns the array number of the selected radio button or -1 if no button is selected
   if (buttonGroup[0])
   { // if the button group is an array (one button is not an array)
      for (var i=0; i<buttonGroup.length; i++)
      {
         if (buttonGroup[i].checked)
         {
            return i;
         }
      }
   }
   else
   {
      if (buttonGroup.checked) { return 0; } // if the one button is checked, return zero
   }
   // if we get to this point, no radio button is selected
   return -1;
}

var lastSelected; 
function updateTaskTable()
{
    var value = CommentForm.showSelect.options[CommentForm.showSelect.selectedIndex].value;
    document.getElementById(value).style.display = "block";

    lastSelected.style.display = "none";
    lastSelected = document.getElementById(value);
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

var segmentLastSelected; 
function updateSegmentTable()
{
    var value = CommentForm.segmentShowSelect.options[CommentForm.segmentShowSelect.selectedIndex].value;
    document.getElementById(value).style.display = "block";

    segmentLastSelected.style.display = "none";
    segmentLastSelected = document.getElementById(value);
}

function initSelections()
{
<% if (selectedLocale == null) { %>
    lastSelected = document.getElementById("allLocales");
<% } else { %>
    lastSelected = document.getElementById(CommentForm.showSelect.options[CommentForm.showSelect.selectedIndex].value);
<% } %>
<% if (segmentSelectedLocale == null || "s_allSegmentLocales".equals(segmentSelectedLocale)) { %>
    segmentLastSelected = document.getElementById("s_allSegmentLocales");
<% } else { %>
    segmentLastSelected = document.getElementById(CommentForm.segmentShowSelect.options[CommentForm.segmentShowSelect.selectedIndex].value);
<% } %>
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
$(document).ready(function(){
	$("[name*='multiSelectAll_']").parent().css("width","2px");
})

//jobSummary child page needed started
$(document).ready(function(){
	<amb:permission  name="<%=Permission.JOB_COMMENTS_VIEW%>" >
		$("#jobCommentsTab").removeClass("tableHeadingListOff");
		$("#jobCommentsTab").addClass("tableHeadingListOn");
		$("#jobCommentsTab img:first").attr("src","/globalsight/images/tab_left_blue.gif");
		$("#jobCommentsTab img:last").attr("src","/globalsight/images/tab_right_blue.gif");
	</amb:permission>
	if(!$.browser.msie){
		$("#jobComentsBlock").css("margin-top","20px");
	}
})

//jobSummary child page needed end.
</SCRIPT>
</head>
<BODY LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0" MARGINWIDTH="0" MARGINHEIGHT="0"
    ONLOAD="loadGuides(); initSelections();">
<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>
<DIV ID="contentLayer" STYLE=" POSITION: ABSOLUTE; Z-INDEX: 9; TOP: 108; LEFT: 20px; RIGHT: 20px;">

<div id="includeSummaryTabs" class="standardText">
	<%@ include file="/envoy/projects/workflows/includeJobSummaryTabs.jspIncl" %>
</div>

<div id="jobComentsBlock"/>
	<form name="CommentForm" method="post">
	<!-- Comments data table -->
	    <table cellpadding=0 cellspacing=0 border=0 class="standardText" width="80%" style="min-width:1024px;">
	        <tr>
	            <td><b><%=bundle.getString("lb_job")%> <%=bundle.getString("lb_comments")%>
	            </b></td>
	        </tr>
	
	        <tr valign="top">
	          <td align="right">
	            <amb:tableNav bean="jobCommentList" key="<%=CommentConstants.JOB_COMMENT_KEY%>" pageUrl="jobComments" />
	          </td>
	        </tr>
	        <tr>
	          <td>
	          <amb:table bean="jobCommentList" id="comment" key="<%=CommentConstants.JOB_COMMENT_KEY%>"
	             dataClass="com.globalsight.everest.comment.Comment" pageUrl="jobComments"
	             emptyTableMsg="msg_comments_none_for_job" >
	            <amb:column label="" width="15px">
	          <amb:permission name="<%=Permission.JOB_COMMENTS_EDIT%>" >
	                <input type="radio" name="radioBtn" value="<%=comment.getId()%>"
	                    onclick="enableButtons('editBtn')">
	           </amb:permission>
	            </amb:column>
	            <amb:column label="lb_comment_creator" sortBy="<%=CommentComparator.CREATOR%>" width="10%">
	                <%=UserUtil.getUserNameById(comment.getCreatorId())%>
	            </amb:column>
	            <amb:column label="lb_date_created" sortBy="<%=CommentComparator.DATE%>" width="15%">
	                <%=comment.getCreatedDate()%>
	            </amb:column>
	            <amb:column label="lb_comments" width="45%" style="word-wrap:break-word;word-break:break-all">
	            <div style='width:45%'>
	                <%
	                    String com = comment.getComment();
	                    if (com.length() > 200)
	                    {
	                        int idx = com.indexOf(' ', 200);
	                        if (idx > 0)
	                            com = com.substring(0, idx);
	                        out.println(com);
	                        out.println("<div onclick=\"javascript:showComment('j" + comment.getId() + "');\" style=\"cursor:hand\">[more...]</div>");
	                        out.println("<div id=j" + comment.getId() + " class=\"comment\">" + comment.getComment() + "<div onclick=closeComment('j" + comment.getId() + "');><span style=\"cursor: hand; color:blue\">[Close]</span></div></div>");
	                    }
	                    else
	                        out.println(comment.getComment());
	                %>
	            </div>
	            </amb:column>
				<amb:column label="multiCheckbox_1" align="right" width="5px"></amb:column>
	            <amb:column label="lb_attached_files" width="30%" style="word-wrap:break-word;word-break:break-all">
	            <div style='width:100%'>
	            <%
	                 String commentId = (new Long(comment.getId())).toString();
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
	      					<amb:permission name="<%=Permission.ACTIVITIES_COMMENTS_DOWNLOAD%>" >
	                        	<input type="checkbox" id="<%=comment.getId()%>" name="checkboxBtn" value="<%=comment.getId() + ":" + file.getFileAccess() + ":" + file.getFilename()%>">
	                        </amb:permission>
	                        <IMG SRC="/globalsight/images/file_paperclip.gif" ALT="<%=bundle.getString("lb_reference_file")%>" HEIGHT=15 WIDTH=13>
	
	<%
	path = "/globalsight/".concat(AmbFileStoragePathUtils.COMMENT_REFERENCE_SUB_DIR).concat(File.separator).concat(commentId);
	path += File.separator.concat(file.getFileAccess()).concat(File.separator).concat(file.getFilename());
	path = URLEncoder.encodeUrlStr(path);
	%>
	                        <A class="standardHREF" target="_blank" href="<%=path %>"><%=EditUtil.encodeHtmlEntities(file.getFilename())%></A>
	                        <SPAN CLASS=smallText>
	                        <%=numberFormat.format(filesize)%>k
	<%
	                        if (file.getFileAccess().equals("Restricted"))
	                        {
	%>
	                            <SPAN STYLE="color: red">(<%=bundle.getString("lb_restricted")%>)&nbsp;</SPAN>
	<%
	                        }
	%>
	                        </SPAN>
	                        <br>
	<%
	                    }
	                  }
	%>
	        </div>
	        </amb:column>
	  </amb:table>
	</td>
	</tr> 
	<tr>
	
	<td align="right" style="padding-top:6px">
		<%--for gbs-2599
		amb:permission name="<%=Permission.ACTIVITIES_COMMENTS_DOWNLOAD%>" >
			<A CLASS="standardHREF" HREF="#"
			                            onClick="doCheckAll('checkboxBtn'); return false;"
			                            onFocus="this.blur();"><%=bundle.getString("lb_check_all")%></A> |
			<A CLASS="standardHREF" HREF="#"
			                            onClick="doClearAll('checkboxBtn'); return false;"
			                            onFocus="this.blur();"><%=bundle.getString("lb_clear_all")%></A>
	    </amb:permission--%>
	    <amb:permission name="<%=Permission.JOB_COMMENTS_EDIT%>" >
	    <INPUT TYPE="BUTTON" NAME=editBtn VALUE="<%=editButton%>" disabled onClick="submitForm('Edit');">
	    </amb:permission>
	    <amb:permission name="<%=Permission.JOB_COMMENTS_NEW%>" >
	    <INPUT TYPE="BUTTON" VALUE="<%=newButton%>" onClick="submitForm('New');">
	    </amb:permission>
	
	</td>
	</tr>
	<tr>
	    <td style="padding-top:4px">
	<!-- Task Comments data table -->
	        <%@ include file="/envoy/administration/comment/taskTable.jspIncl" %>
	    </td>
	</tr>
	<tr><td>&nbsp;</td></tr>
	<tr>
	    <td>
	<!-- Segment Comments data table -->
	        <%@ include file="/envoy/administration/comment/segmentTable.jspIncl" %>
	    </td>
	</tr>
	</td>
	</table>
	<p>
	<p>
	</form>
</div>
</div>
</body>
</html>
