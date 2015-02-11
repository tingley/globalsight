<%@ taglib uri="/WEB-INF/tlds/globalsight.tld" prefix="amb" %>
<%@ page contentType="text/html; charset=UTF-8"
         errorPage="/envoy/common/error.jsp"
         import="com.globalsight.everest.webapp.pagehandler.PageHandler,
                 com.globalsight.everest.foundation.User,
                 com.globalsight.everest.webapp.pagehandler.projects.workflows.JobManagementHandler,
                 com.globalsight.everest.webapp.pagehandler.administration.reports.ReportConstants,
                 com.globalsight.everest.webapp.pagehandler.administration.reports.ReportsMainHandler,
                 com.globalsight.everest.webapp.pagehandler.administration.reports.CustomExternalReportInfoBean,
                 com.globalsight.everest.webapp.WebAppConstants,
                 com.globalsight.everest.permission.Permission,
                 com.globalsight.everest.permission.PermissionSet,
                 com.globalsight.everest.company.CompanyWrapper,
                 java.util.ArrayList,
                 java.util.Date,
                 java.util.Iterator,
                 java.util.Locale,
                 java.util.ResourceBundle" session="true" 
%>
<jsp:useBean id="jobComments" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="jobDetails" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="jobAttributes" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="jobReports" scope="request" class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<%!
    //colors to use for the table background
    private static final String WHITE_BG         = "#FFFFFF";
    private static final String LT_GREY_BG       = "#EEEEEE";
	// Toggles the background color of the rows used between WHITE and LT_GREY
    private static String toggleBgColor(int p_rowNumber)
    {
        return p_rowNumber % 2 == 0 ? WHITE_BG : LT_GREY_BG;  
    }
%>
<%
    String companyName = CompanyWrapper.getCurrentCompanyName() + " ";
    ResourceBundle bundle = PageHandler.getBundle(session);
    SessionManager sessionMgr =
        (SessionManager)session.getAttribute(WebAppConstants.SESSION_MANAGER);
    String title= bundle.getString("lb_job_details") + " " + bundle.getString("lb_reports");
    String pagetitle= bundle.getString("lb_globalsight") + bundle.getString("lb_colon") + " " + title;
    int rowNum = 0;
    Locale theUiLocale = (Locale) session.getAttribute(WebAppConstants.UILOCALE);

    boolean hasAtLeastOneReport = false;

    String reportUrl="";
	String reportName="";
	String reportDesc="";
	String reportWindowName="";
	
	// tab labels
    String labelDetails = bundle.getString("lb_details");
    String labelComments = bundle.getString("lb_comments");
	String labelJobName =  bundle.getString("lb_job") + bundle.getString("lb_colon");
    String jobName = (String)sessionMgr.getAttribute("jobName");
    long jobId = Long.valueOf(request.getParameter(JobManagementHandler.JOB_ID));
    
    String jobDetailsURL = jobDetails.getPageURL()
    		+ "&" + JobManagementHandler.JOB_ID
    		+ "=" + jobId;
	String jobAttributesURL = jobAttributes.getPageURL() 
    		+ "&" + JobManagementHandler.JOB_ID 
    		+ "=" + jobId;
	String jobCommentsURL = jobComments.getPageURL()
    		+ "&" + JobManagementHandler.JOB_ID
    		+ "=" + jobId;
	String jobReportsURL = jobReports.getPageURL() 
			+ "&" + JobManagementHandler.JOB_ID 
			+ "=" + jobId;
	
	String basicAction = "/globalsight/ControlServlet?linkName=generateReports&pageName=JOBREPORTS&action=";
	String generateAction = basicAction + ReportConstants.GENERATE_REPORTS;
	String percentAction = basicAction + ReportConstants.ACTION_GET_PERCENT;
	String cancelAction = basicAction + ReportConstants.ACTION_CANCEL_REPORTS;
%>
<HTML>
<HEAD>
<META HTTP-EQUIV="content-type" CONTENT="text/html;charset=UTF-8">
<TITLE><%= title %></TITLE>
<STYLE>
TR.standardText 
{
    vertical-align: top;
}
</STYLE>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>
<script type="text/javascript" src="/globalsight/jquery/jquery-1.6.4.js"></script>
<script type="text/javascript" src="/globalsight/jquery/jquery.progressbar.js"></script>
<script type="text/javascript">
var needWarning = false;
var objectName = "";
var guideNode = "myJobs";
var helpFile = "<%=bundle.getString("help_job_detailsReports")%>";

var msgReportType = "<%=bundle.getString("msg_select_report")%>";
var reportTypeElemName = "<%=ReportConstants.REPORT_TYPE%>"; 
var percentAction = "<%=percentAction%>";
var cancelAction = "<%=cancelAction%>";

$(document).ready(function() {
	$('#checkAll').click(fnCheckAll);
	hideProgressDialog();
});

function fnCheckAll()
{
	if($("#checkAll").prop("checked")){
		$("input[name='checkReport']").each(function(){
			$(this).prop("checked", true);
		})
	}else{
		$("input[name='checkReport']").each(function(){
			$(this).prop("checked", false);
		})
	}
}

/*
function fnCancelDownload()
{
	cancelAction += "&inputJobIDS=" + $("input[name='inputJobIDS']")[0].value + "&t=" + new Date();
	$.ajax({url:cancelAction});
	location.reload();
}
*/

function hideProgressDialog() {
	$('#dialog').hide();
	$("#idProgressBar").progressBar(0);
}

function updateProgress()
{
	getPercentageURL = percentAction + "&inputJobIDS=" + $("input[name='inputJobIDS']")[0].value + "&t=" + new Date();
	$.getJSON(getPercentageURL, function(data) {
		var per = data.percent;
		$("#idProgressBar").progressBar(per);
		if (per < 100) 
		{
			setTimeout(updateProgress,1000);
	    }
    });
}

function showDownlaod()
{
	getPercentageURL = percentAction + "&inputJobIDS=" + $("input[name='inputJobIDS']")[0].value + "&t=" + new Date();
	$.getJSON(getPercentageURL, function(data) {
		if(data.percent<100)
		{
			$("#idProgressBar").progressBar(data.percent);
			$('#dialog').show();
			updateProgress();
		}
    });
}

function fnGenerateReports()
{
	var reportType = "";
	var checks = document.getElementsByName("checkReport");
	for(var i=0; i<checks.length; i++)
	{
		if(checks[i].checked)
		{
			reportType = reportType + checks[i].value + ",";
		}
	}
	reportType = reportType.substring(0, reportType.length-1);
	if(reportType == "")
	{
		alert(msgReportType);
		return;
	}
	document.getElementsByName(reportTypeElemName)[0].value = reportType;
	hideProgressDialog();
	setTimeout(showDownlaod, 1000);
	ReportsForm.submit();
}

function fnGenerateReport(obj)
{
	var reportType = obj.children[0].children[0].value;
	if(reportType == "")
	{
		alert(msgReportType);
		return;
	}
	document.getElementsByName(reportTypeElemName)[0].value = reportType;
	ReportsForm.submit();
}
</script>
</HEAD>
<BODY LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0" MARGINWIDTH="0" MARGINHEIGHT="0"
    ONLOAD="loadGuides()">
<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>
<DIV ID="contentLayer" STYLE=" POSITION: ABSOLUTE; Z-INDEX: 9; TOP: 108; LEFT: 20px; RIGHT: 20px;">

<SPAN CLASS="mainHeading"><%=labelJobName%> <%=jobName%></SPAN>   
<p>
<!-- Tabs table -->
<TABLE CELLPADDING="0" CELLSPACING="0" BORDER="0">
    <TR>
        <TD CLASS="tableHeadingListOff"><IMG SRC="/globalsight/images/tab_left_gray.gif" BORDER="0"><A CLASS="sortHREFWhite" HREF="<%=jobDetailsURL%>"><%=labelDetails%></A><IMG SRC="/globalsight/images/tab_right_gray.gif" BORDER="0"></TD>
        <TD WIDTH="2"></TD>
        <TD CLASS="tableHeadingListOff"><IMG SRC="/globalsight/images/tab_left_gray.gif" BORDER="0"><A CLASS="sortHREFWhite" HREF="<%=jobCommentsURL%>"><%=labelComments%></A><IMG SRC="/globalsight/images/tab_right_gray.gif" BORDER="0"></TD>
        <TD WIDTH="2"></TD>
	    <TD CLASS="tableHeadingListOff">
		    <amb:permission  name="<%=Permission.JOB_ATTRIBUTE_VIEW%>" >
		    <IMG SRC="/globalsight/images/tab_left_gray.gif" BORDER="0">
		    <A CLASS="sortHREFWhite" HREF="<%=jobAttributesURL%>"><%=bundle.getString("lb_job_attributes") %></A>
		    <IMG SRC="/globalsight/images/tab_right_gray.gif" BORDER="0">
		    </amb:permission>
	    </TD>
	    
	    <amb:permission  name="<%=Permission.REPORTS_MAIN%>" >
	    <TD WIDTH="2"></TD>
	    <TD CLASS="tableHeadingListOn">
		    <IMG SRC="/globalsight/images/tab_left_blue.gif" BORDER="0">
		    <A CLASS="sortHREFWhite" HREF="<%=jobReportsURL%>"><%=bundle.getString("lb_reports") %></A>
		    <IMG SRC="/globalsight/images/tab_right_blue.gif" BORDER="0">
	    </TD>
	    </amb:permission>
    </TR>
</TABLE>
<!-- End Tabs table -->
    
<P>
<form name="ReportsForm" method="post" action="<%=generateAction%>">
<input type="hidden" name="<%=ReportConstants.JOB_IDS%>" value="<%=jobId%>">
<input type="hidden" name="<%=ReportConstants.TARGETLOCALE_LIST%>" value="*">
<input type="hidden" name="<%=ReportConstants.REPORT_TYPE%>" value="">
<input type="hidden" name="currency" value="US Dollar (USD)">
<input type="hidden" name="reportStyle" value="trados">
<p></p>
<SPAN CLASS="standardText"><B><%=bundle.getString("lb_available_reports")%></B></SPAN>
<TABLE CLASS="standardText" CELLSPACING="0" CELLPADDING="2" BORDER="0">
<% if (userPerms.getPermissionFor(Permission.REPORTS_MAIN)) { %>
		<TR>
			<TD CLASS="tableHeadingBasic"><input type="checkbox" id="checkAll"></TD>
    		<TD CLASS="tableHeadingBasic" style="width:220px;"><%=bundle.getString("reportName")%></TD>
    		<TD CLASS="tableHeadingBasic" style="width:100px;"><%=bundle.getString("exportAs")%></TD>
        	<TD CLASS="tableHeadingBasic"><%=bundle.getString("reportDesc")%></TD>
    	</TR>
    
    <% if (userPerms.getPermissionFor(Permission.REPORTS_DELL_ONLINE_JOBS)) {
		reportUrl="/globalsight/ControlServlet?activityName=xlsReportOnlineJobs";
        reportName=companyName + bundle.getString("online_jobs");
        reportWindowName="OnlineJobs";
	%>
        <TR BGCOLOR="<%=toggleBgColor(rowNum++)%>" CLASS="standardText">
        	<TD><input type="checkbox" name="checkReport" value="<%=ReportConstants.ONLINE_JOBS_REPORT %>"></TD>
            <TD>
				<A CLASS=standardHREF href="javascript:;" onclick="javascript:fnGenerateReport(this.parentNode.parentNode);">
					<%=reportName%>
				</A>
            </TD>
            <TD>XLS</TD>
            <TD><%=bundle.getString("online_jobs_desc")%></TD>
        </TR>
	<% } 

    if (userPerms.getPermissionFor(Permission.REPORTS_DELL_FILE_LIST))
    {
		reportUrl="/globalsight/ControlServlet?activityName=xlsReportFileList";
    	reportName=companyName + bundle.getString("file_list_report");
    	reportWindowName="FileList";
	%>
		<TR BGCOLOR="<%=toggleBgColor(rowNum++)%>" CLASS="standardText">
        	<TD><input type="checkbox" name="checkReport" value="<%=ReportConstants.DETAILED_WORDCOUNTS_REPORT%>"></TD>
        	<TD>
          		<A CLASS=standardHREF href="javascript:;" onclick="javascript:fnGenerateReport(this.parentNode.parentNode);">
					<%=reportName%>
            	</A>
        	</TD>
        	<TD>
            	<select name="exportFormat">
            		<option value="xls">XLS</option>
            		<option value="csv">CSV</option>
            	</select>
        	</TD>
         	<TD><%=bundle.getString("file_list_report_desc")%></TD>
        </TR>  
    <% } %> 
    
    	<amb:permission name="<%=Permission.REPORTS_LANGUAGE_SIGN_OFF%>">
        <TR BGCOLOR="<%=toggleBgColor(rowNum++)%>" CLASS="standardText">
        	<TD><input type="checkbox" name="checkReport" value="<%=ReportConstants.REVIEWERS_COMMENTS_REPORT%>"></TD>
            <TD>
          		<A CLASS=standardHREF href="javascript:;" onclick="javascript:fnGenerateReport(this.parentNode.parentNode);">
             		<%=bundle.getString("review_reviewers_comments")%>
				</A>
            </TD>
            <TD>XLS</TD>
            <TD><%=bundle.getString("review_reviewers_comments_desc")%></TD>
        </TR>
        </amb:permission>

		<amb:permission name="<%=Permission.REPORTS_COMMENTS_ANALYSIS%>">
        <TR BGCOLOR="<%=toggleBgColor(rowNum++)%>" CLASS="standardText">
        	<TD><input type="checkbox" name="checkReport" value="<%=ReportConstants.COMMENTS_ANALYSIS_REPORT%>"></TD>
            <TD>
          		<A CLASS=standardHREF href="javascript:;" onclick="javascript:fnGenerateReport(this.parentNode.parentNode);">
             		<%=bundle.getString("review_comments")%>
				</A>
            </TD>
            <TD>XLS</TD>
            <TD><%=bundle.getString("review_comments_desc")%></TD>
        </TR>
        </amb:permission>
        
        <amb:permission name="<%=Permission.REPORTS_CHARACTER_COUNT%>">
        <TR BGCOLOR="<%=toggleBgColor(rowNum++)%>" CLASS="standardText">
        	<TD><input type="checkbox" name="checkReport" value="<%=ReportConstants.CHARACTER_COUNT_REPORT%>"></TD>
            <TD>
				<A CLASS=standardHREF href="javascript:;" onclick="javascript:fnGenerateReport(this.parentNode.parentNode);">
             		<%=bundle.getString("character_count_report")%>
				</A>
            </TD>
            <TD>XLS</TD>
            <TD><%=bundle.getString("character_count_report_desc")%></TD>
        </TR>
        </amb:permission>
        
         
    	<TR><TD colspan="2"><input type="button" onClick="fnGenerateReports();" value="Run Reports"  CLASS="standardText"><TD></TR>  
<% } else { %>
		<EM><%=bundle.getString("no_reports_permissions")%></EM>
<% } %>
</TABLE>
<DIV id="dialog" style="width:200px;display:none;">
	<div style="height:10px;"></div>
	<div id="idProgressBar"></div>	
</DIV>
</form>
</DIV>


</BODY>
</HTML>