<%@ page
    	contentType="text/html; charset=UTF-8"
		errorPage="/envoy/common/error.jsp"
		import="java.util.*,com.globalsight.everest.taskmanager.Task,
                com.globalsight.util.edit.EditUtil,
                com.globalsight.everest.permission.Permission,
                com.globalsight.everest.permission.PermissionSet,
                com.globalsight.everest.edit.offline.OfflineEditManager,
                com.globalsight.everest.util.system.SystemConfigParamNames,
                com.globalsight.everest.util.system.SystemConfiguration,
                com.globalsight.everest.webapp.WebAppConstants,
                com.globalsight.everest.webapp.javabean.NavigationBean,
                com.globalsight.everest.webapp.pagehandler.administration.reports.ReportConstants,
                com.globalsight.everest.webapp.pagehandler.PageHandler,
                com.globalsight.everest.webapp.pagehandler.offline.OfflineConstants,
                com.globalsight.everest.webapp.pagehandler.tasks.TaskHelper,
                com.globalsight.everest.workflowmanager.Workflow,
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
<jsp:useBean id="detail" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request" />
<jsp:useBean id="comment" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request" />
<jsp:useBean id="cancel" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request" />
<%
    SessionManager sessionManager = 
        (SessionManager)session.getAttribute(WebAppConstants.SESSION_MANAGER);
    ResourceBundle bundle = PageHandler.getBundle(session);
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
    // links
    String downloadUrl = download.getPageURL();
    String uploadUrl = upload.getPageURL();
    String uploadReportUrl = uploadreport.getPageURL();
    String commentUrl = comment.getPageURL();
    String cancelUrl = cancel.getPageURL();
    String downloadReportUrl = downloadreport.getPageURL();
    // labels
    String reportType = null;
    String downloadInstruction = null;
    String downloadHelper = null;
    String workOfflineUrl = null;
    if (review_only)
    {
       	reportType = ReportConstants.REVIEWERS_COMMENTS_REPORT;
       	downloadInstruction = bundle.getString("helper_text_download_language_instruction");
       	downloadHelper = EditUtil.toJavascript(bundle.getString("helper_text_download_LSO"));
       	workOfflineUrl = downloadReportUrl;
    }
    else
    {
		reportType = ReportConstants.TRANSLATIONS_EDIT_REPORT;
        downloadInstruction = bundle.getString("helper_text_download_translation_instruction");
        downloadHelper = EditUtil.toJavascript(bundle.getString("helper_text_download_TER"));
        workOfflineUrl = downloadUrl;
    }

    String title = bundle.getString("lb_download_report");
    String lbCancel = bundle.getString("lb_cancel");
    String labelActivity = bundle.getString("lb_activity") + bundle.getString("lb_colon");
    String labelJobName =  bundle.getString("lb_job") + bundle.getString("lb_colon");
    String lbDetails = bundle.getString("lb_details");
    String lbWorkoffline = bundle.getString("lb_work_offline");
    String lbComments = bundle.getString("lb_comments");
    String lbDownload = bundle.getString("lb_tab_download");
    String lbUpload = bundle.getString("lb_tab_upload");

    String lbDownloadReport = bundle.getString("lb_download_report");
    String lbUploadReport = bundle.getString("lb_upload_report");
    String lbStartDownload = bundle.getString("lb_download_start");

    sessionManager.setAttribute(WebAppConstants.TARGETVIEW_LOCALE, targetLanguage);

    // Get data for the Hints table
    String jobName = task.getJobName();

%>
<HTML>
<HEAD>
<TITLE><%=title%></TITLE>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/utilityScripts.js"></SCRIPT>
<script type="text/javascript" SRC="/globalsight/dojo/dojo.js"></script>

<SCRIPT LANGUAGE="JavaScript">

function showProgressBar()
{
  var div = dojo.byId('loadingDiv');
  div.style.visibility = "visible";
}

var WIDTH = 400;
var needWarning = false;
var guideNode = "myActivitiesUpload";
var helpFile = "<%=bundle.getString("help_download")%>";

var finished = false;
var msg = "";

function doOnLoad()
{
  loadGuides();
}

function download()
{
	finished = false;
	
	var downloadButton = dojo.byId('downloadButton');
	downloadButton.disabled = true;
	showProgressBar();
	
	var obj = {
			inputJobIDS : "<%=task.getJobId()%>",
			targetLocalesList: "<%=task.getTargetLocale().getId()%>",
			reportType:"<%=reportType%>"
	}	
	
    dojo.xhrPost(
    {
       url:"/globalsight/ControlServlet?linkName=generateReports&pageName=JOBREPORTS&action=generateReport",
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
	msg = '<%=bundle.getString("helper_test_download_report_cancel")%>';
	changeWait();
	
	var obj = {
			inputJobIDS : "<%=task.getJobId()%>",
			targetLocalesList: "<%=task.getTargetLocale().getId()%>",
			reportType:"<%=reportType%>"
		}

    dojo.xhrPost(
    {
       url:"/globalsight/ControlServlet?linkName=generateReports&pageName=JOBREPORTS&action=cancelReport",
       handleAs: "text", 
       content:obj,
       load:function(data)
       {
    	   location.replace('<%=cancelUrl%>');
       },
       error:function(error)
       {
           alert(error.message);
       }
   });
}
</SCRIPT>
<%@ include file="/envoy/common/warning.jspIncl" %>
</HEAD>

<BODY LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0" MARGINWIDTH="0" MARGINHEIGHT="0"
    ONLOAD="doOnLoad()">
<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>

<iframe id="idReport" name="idReport" src="about:blank" style="display:none"></iframe>
<FORM name="ReportForm" METHOD="POST" TARGET="idReport"
 ACTION="/globalsight/ControlServlet?linkName=generateReports&pageName=JOBREPORTS&action=getReport">
<input type="hidden" name="<%=ReportConstants.JOB_IDS%>" value="<%=task.getJobId()%>">
<input type="hidden" name="<%=ReportConstants.TARGETLOCALE_LIST%>" value="<%=task.getTargetLocale().getId()%>">
<input type="hidden" name="<%=ReportConstants.REPORT_TYPE%>" value="<%=reportType%>">
</FORM>

<DIV ID="contentLayer" STYLE=" POSITION: ABSOLUTE; Z-INDEX: 9; TOP: 108px; LEFT: 20px; RIGHT: 20px;">

<P CLASS="mainHeading"><%=labelJobName%> <%=jobName%></P>


<TABLE CELLPADDING=0 CELLSPACING=0 BORDER=0 CLASS=standardText>
<TR>
<TD WIDTH=500>
<%=downloadHelper%>
</TD>
</TR>
</TABLE>

<P></P>

<TABLE CELLPADDING="0" CELLSPACING="0" BORDER="0">
<TR>
<TD>
<!-- Tabs table -->
<TABLE CELLPADDING="0" CELLSPACING="0" BORDER="0">
<TR>
  <TD CLASS="tableHeadingListOff"><IMG SRC="/globalsight/images/tab_left_gray.gif" BORDER="0"><A CLASS="sortHREFWhite" HREF="<%=detailUrl%>"><%=lbDetails%></A><IMG SRC="/globalsight/images/tab_right_gray.gif" BORDER="0"></TD>
  <TD WIDTH="2"></TD>
  <amb:permission name="<%=Permission.ACTIVITIES_COMMENTS_VIEW%>">
  <TD CLASS="tableHeadingListOff"><IMG SRC="/globalsight/images/tab_left_gray.gif" BORDER="0"><A CLASS="sortHREFWhite" HREF="<%=commentUrl%>"><%=lbComments%></A><IMG SRC="/globalsight/images/tab_right_gray.gif" BORDER="0"></TD>
  <TD WIDTH="2"></TD>
  </amb:permission>
  <TD CLASS="tableHeadingListOn"><IMG SRC="/globalsight/images/tab_left_blue.gif" BORDER="0"><A CLASS="sortHREFWhite" HREF="<%=workOfflineUrl%>"><%=lbWorkoffline%></A><IMG SRC="/globalsight/images/tab_right_blue.gif" BORDER="0"></TD>
  <TD WIDTH="2"></TD>
</TR>
</TABLE>
<!-- End Tabs table -->
<p>
<!-- Tabs table -->
<TABLE CELLPADDING="0" CELLSPACING="0" BORDER="0">
<TR>
<%
if (!review_only)
{
%>
  <TD CLASS="tableHeadingListOff"><IMG SRC="/globalsight/images/tab_left_gray.gif" BORDER="0"><A CLASS="sortHREFWhite" HREF="<%=downloadUrl%>"><%=lbDownload%></A><IMG SRC="/globalsight/images/tab_right_gray.gif" BORDER="0"></TD>
  <TD WIDTH="2"></TD>
  <TD CLASS="tableHeadingListOff"><IMG SRC="/globalsight/images/tab_left_gray.gif" BORDER="0"><A CLASS="sortHREFWhite" HREF="<%=uploadUrl%>"><%=lbUpload%></A><IMG SRC="/globalsight/images/tab_right_gray.gif" BORDER="0"></TD>
  <TD WIDTH="2"></TD>
<%
}
%>
  <TD CLASS="tableHeadingListOn"><IMG SRC="/globalsight/images/tab_left_blue.gif" BORDER="0"><A ONCLICK='submitForm()' CLASS="sortHREFWhite" ><%=lbDownloadReport%></A><IMG SRC="/globalsight/images/tab_right_blue.gif" BORDER="0"></TD>
  <TD WIDTH="2"></TD>
  <TD CLASS="tableHeadingListOff"><IMG SRC="/globalsight/images/tab_left_gray.gif" BORDER="0"><A CLASS="sortHREFWhite" HREF="<%=uploadReportUrl%>"><%=lbUploadReport%></A><IMG SRC="/globalsight/images/tab_right_gray.gif" BORDER="0"></TD>
</TR>
</TABLE>
<!-- End Tabs table -->
</TD>

<TD ALIGN="RIGHT" VALIGN="BOTTOM" NOWRAP></TD>
</TR>

<TR>
<TD CLASS="tableHeadingBasic" COLSPAN="2" HEIGHT=1 WIDTH="500"><IMG SRC="/globalsight/images/spacer.gif" HEIGHT="1" WIDTH="1"></TD>
</TR>

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
</P>
<BR>

<TABLE CELLPADDING="2" CELLSPACING="0" BORDER="0" CLASS="standardText">
    <TD></TD>
    <TD ><%=downloadInstruction%></TD>
  </TR>
    <TR>
    <TD>&nbsp;</TD>
    <TD></TD>
     </TR>
  <TR>
    <TD>&nbsp;</TD>
    <TD id="loadingDiv">
        
    </TD>
  </TR>
</TABLE>
<P>
		
<INPUT TYPE="BUTTON" NAME="<%=lbCancel%>" VALUE="<%=lbCancel%>"
    ONCLICK="cancelReport()">   
<INPUT id="downloadButton" TYPE="BUTTON" NAME="<%=lbStartDownload%>" VALUE="<%=lbStartDownload%>"
	ONCLICK="download()">

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
