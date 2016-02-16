<%@ taglib uri="/WEB-INF/tlds/globalsight.tld" prefix="amb" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
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
                 java.text.MessageFormat,
                 java.util.ResourceBundle" session="true" 
%>
<jsp:useBean id="jobComments" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="jobDetails" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="jobAttributes" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="jobReports" scope="request" class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="jobSourceFiles" scope="request" class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="jobCosts" scope="request" class="com.globalsight.everest.webapp.javabean.NavigationBean" />
 <jsp:useBean id="jobScorecard" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
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
    long jobId = Long.valueOf(request.getParameter(JobManagementHandler.JOB_ID));
    
    String jobCommentsURL = jobComments.getPageURL() + "&jobId=" + request.getAttribute("jobId");
	
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
<script type="text/javascript" src="/globalsight/jquery/jquery-1.6.4.min.js"></script>
<script type="text/javascript">
var needWarning = false;
var objectName = "";
var guideNode = "myJobs";
var helpFile = "<%=bundle.getString("help_job_detailsReports")%>";

var msgReportType = "<%=bundle.getString("msg_select_report")%>";
var reportTypeElemName = "<%=ReportConstants.REPORT_TYPE%>"; 
var percentAction = "<%=percentAction%>";
var cancelAction = "<%=cancelAction%>";

function fnCheckAll()
{
	if($("#checkAll").attr("checked")){
		$("input[name='checkReport']").each(function(){
			$(this).attr("checked", true);
			if(this.value == "ReviewersCommentsReport")
			{
				$("#ReviewersIncludeTagsTR").show();
				$("#ReviewersIncludeTags").attr("checked", true);
			}
			else if(this.value == "ReviewersCommentsSimpleReport")
			{
				$("#ReviewersSimpleIncludeTagsTR").show();
				$("#ReviewersSimpleIncludeTags").attr("checked", true);
			}
			else if(this.value == "CharacterCountReport")
			{
				$("#CharacterCountReportIncludeTagsTR").show();
				$("#CharacterCountReportIncludeTags").attr("checked", true);
			}
		})
	}else{
		$("input[name='checkReport']").each(function(){
			$(this).attr("checked", false);
			if(this.value == "ReviewersCommentsReport")
			{
				$("#ReviewersIncludeTagsTR").hide();
				$("#ReviewersIncludeTags").attr("checked", false);
			}
			else if(this.value == "ReviewersCommentsSimpleReport")
			{
				$("#ReviewersSimpleIncludeTagsTR").hide();
				$("#ReviewersSimpleIncludeTags").attr("checked", false);
			}
			else if(this.value == "CharacterCountReport")
			{
				$("#CharacterCountReportIncludeTagsTR").hide();
				$("#CharacterCountReportIncludeTags").attr("checked", false);
			}
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

function checkSaveUnlSeg(obj)
{
	if(obj.value == "ReviewersCommentsReport")
	{
		if(obj.checked)
		{
			 $("#ReviewersIncludeTagsTR").show();
		}
		else
		{
			$("#ReviewersIncludeTagsTR").hide();
			$("#ReviewersIncludeTags").attr("checked", false);
		}
	}
	else if(obj.value == "ReviewersCommentsSimpleReport")
	{
		if(obj.checked)
		{
			$("#ReviewersSimpleIncludeTagsTR").show();
		}
		else
		{
			$("#ReviewersSimpleIncludeTagsTR").hide();
			$("#ReviewersSimpleIncludeTags").attr("checked", false);
		}
	}
	else if(obj.value == "CharacterCountReport")
	{
		if(obj.checked)
		{
			$("#CharacterCountReportIncludeTagsTR").show();
		}
		else
		{
			$("#CharacterCountReportIncludeTagsTR").hide();
			$("#CharacterCountReportIncludeTags").attr("checked", false);
		}
	}
}

$(document).ready(function(){
	// Modify the css of Reports tab.
	$("#jobReportsTab").removeClass("tableHeadingListOff");
	$("#jobReportsTab").addClass("tableHeadingListOn");
	$("#jobReportsTab img:first").attr("src","/globalsight/images/tab_left_blue.gif");
	$("#jobReportsTab img:last").attr("src","/globalsight/images/tab_right_blue.gif");
	$('#checkAll').click(fnCheckAll);
	$("#ReviewersIncludeTagsTR").hide();
	$("#ReviewersSimpleIncludeTagsTR").hide();
	$("#CharacterCountReportIncludeTagsTR").hide();
});
</script>
</HEAD>
<BODY LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0" MARGINWIDTH="0" MARGINHEIGHT="0"
    ONLOAD="loadGuides()">
<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>
<DIV ID="contentLayer" STYLE=" POSITION: ABSOLUTE; Z-INDEX: 9; TOP: 108; LEFT: 20px; RIGHT: 20px;">

<div id="includeSummaryTabs" class="standardText">
	<%@ include file="/envoy/projects/workflows/includeJobSummaryTabs.jspIncl" %>
</div>
<div style="clear:both"></div>
<form name="ReportsForm" method="post" action="<%=generateAction%>">
<input type="hidden" name="<%=ReportConstants.JOB_IDS%>" value="<%=jobId%>">
<input type="hidden" name="<%=ReportConstants.TARGETLOCALE_LIST%>" value="*">
<input type="hidden" name="<%=ReportConstants.REPORT_TYPE%>" value="">
<input type="hidden" name="currency" value="US Dollar (USD)">
<input type="hidden" name="reportStyle" value="trados">
<p></p>
<SPAN CLASS="standardText"><B><%=bundle.getString("lb_available_reports")%></B></SPAN>
<TABLE CLASS="standardText" CELLSPACING="0" CELLPADDING="2" style="border:solid 1px slategray;">
<% if (userPerms.getPermissionFor(Permission.REPORTS_MAIN)) { %>
		<TR>
			<TD CLASS="tableHeadingBasic"><input type="checkbox" id="checkAll"></TD>
    		<TD CLASS="tableHeadingBasic" style="width:250px;"><%=bundle.getString("reportName")%></TD>
    		<TD CLASS="tableHeadingBasic" style="width:100px;"><%=bundle.getString("exportAs")%></TD>
        	<TD CLASS="tableHeadingBasic"><%=bundle.getString("reportDesc")%></TD>
    	</TR>
    
    <% 
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
            		<option value="xlsx">XLSX</option>
            		<option value="csv">CSV</option>
            	</select>
        	</TD>
         	<TD><%=bundle.getString("file_list_report_desc")%></TD>
        </TR>  
    <% } %> 
    
    	<amb:permission name="<%=Permission.REPORTS_LANGUAGE_SIGN_OFF%>">
        <TR BGCOLOR="<%=toggleBgColor(rowNum++)%>" CLASS="standardText">
        	<TD><input type="checkbox" name="checkReport" onclick="checkSaveUnlSeg(this)" value="<%=ReportConstants.REVIEWERS_COMMENTS_REPORT%>"></TD>
            <TD>
          		<A CLASS=standardHREF href="javascript:;" onclick="javascript:fnGenerateReport(this.parentNode.parentNode);">
             		<%=bundle.getString("review_reviewers_comments")%>
				</A>
            </TD>
            <TD>XLSX</TD>
            <TD><%=bundle.getString("review_reviewers_comments_desc")%></TD>
        </TR>
        <TR id="ReviewersIncludeTagsTR" CLASS="standardText">
        	<TD>&nbsp;&nbsp;&nbsp;&nbsp;</TD>
        	<TD>
        		<INPUT TYPE="checkbox" ID="ReviewersIncludeTags" NAME="withCompactTags"><%=bundle.getString("with_compact_tags")%>
        	</TD>
        	<TD>&nbsp;&nbsp;&nbsp;&nbsp;</TD>
        	<TD>&nbsp;&nbsp;&nbsp;&nbsp;</TD>
        </TR>
        </amb:permission>
        
        <amb:permission name="<%=Permission.REPORTS_LANGUAGE_SIGN_OFF_SIMPLE%>">
        <TR BGCOLOR="<%=toggleBgColor(rowNum++)%>" CLASS="standardText">
        	<TD><input type="checkbox" name="checkReport" onclick="checkSaveUnlSeg(this)" value="<%=ReportConstants.REVIEWERS_COMMENTS_SIMPLE_REPORT%>"></TD>
            <TD>
          		<A CLASS=standardHREF href="javascript:;" onclick="javascript:fnGenerateReport(this.parentNode.parentNode);">
             		<%=bundle.getString("review_reviewers_comments_simple")%>
				</A>
            </TD>
            <TD>XLSX</TD>
            <TD><%=bundle.getString("review_reviewers_comments_simple_desc")%></TD>
        </TR>
         <TR id="ReviewersSimpleIncludeTagsTR" CLASS="standardText">
         	<TD>&nbsp;&nbsp;&nbsp;&nbsp;</TD>
        	<TD>
        		<INPUT TYPE="checkbox" ID="ReviewersSimpleIncludeTags" NAME="withCompactTagsSimple"><%=bundle.getString("with_compact_tags")%>
        	</TD>
        	<TD>&nbsp;&nbsp;&nbsp;&nbsp;</TD>
        	<TD>&nbsp;&nbsp;&nbsp;&nbsp;</TD>
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
            <TD>XLSX</TD>
            <TD><%=bundle.getString("review_comments_desc")%></TD>
        </TR>
        </amb:permission>
        
        <amb:permission name="<%=Permission.REPORTS_CHARACTER_COUNT%>">
        <TR BGCOLOR="<%=toggleBgColor(rowNum++)%>" CLASS="standardText">
        	<TD><input type="checkbox" name="checkReport"  onclick="checkSaveUnlSeg(this)"  value="<%=ReportConstants.CHARACTER_COUNT_REPORT%>"></TD>
            <TD>
				<A CLASS=standardHREF href="javascript:;" onclick="javascript:fnGenerateReport(this.parentNode.parentNode);">
             		<%=bundle.getString("character_count_report")%>
				</A>
            </TD>
            <TD>XLSX</TD>
            <TD><%=bundle.getString("character_count_report_desc")%></TD>
            <TR id="CharacterCountReportIncludeTagsTR" CLASS="standardText">
         	<TD>&nbsp;&nbsp;&nbsp;&nbsp;</TD>
        	<TD>
        		<INPUT TYPE="checkbox" ID="CharacterCountReportIncludeTags" NAME="withCompactTagsCCR"><%=bundle.getString("with_compact_tags")%>
        	</TD>
        	<TD>&nbsp;&nbsp;&nbsp;&nbsp;</TD>
        	<TD>&nbsp;&nbsp;&nbsp;&nbsp;</TD>
        </TR>
        </TR>
        </amb:permission>
        
         
    	<TR><TD colspan="2"><input type="button" onClick="fnGenerateReports();" value="Run Reports"  CLASS="standardText"><TD></TR>  
<% } else { %>
		<EM><%=bundle.getString("no_reports_permissions")%></EM>
<% } %>
</TABLE>
</form>
</DIV>


</BODY>
</HTML>