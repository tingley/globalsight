<%@ taglib uri="/WEB-INF/tlds/globalsight.tld" prefix="amb" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ page contentType="text/html; charset=UTF-8"
         errorPage="/envoy/common/error.jsp"
         import="com.globalsight.everest.foundation.User,
                 com.globalsight.everest.jobhandler.Job,
                 com.globalsight.everest.servlet.util.SessionManager,
                 com.globalsight.everest.webapp.pagehandler.PageHandler,
                 com.globalsight.everest.webapp.pagehandler.projects.workflows.JobDetailsConstants,
                 com.globalsight.everest.webapp.pagehandler.projects.workflows.JobManagementHandler,
                 com.globalsight.everest.webapp.pagehandler.administration.reports.ReportConstants,
                 com.globalsight.everest.webapp.pagehandler.administration.reports.ReportsMainHandler,
                 com.globalsight.everest.webapp.pagehandler.administration.reports.CustomExternalReportInfoBean,
                 com.globalsight.everest.webapp.WebAppConstants,
                 com.globalsight.everest.permission.Permission,
                 com.globalsight.everest.permission.PermissionSet,
                 java.util.ArrayList,
                 java.util.Date,
                 java.util.Iterator,
                 java.util.Locale,
                 java.text.MessageFormat,
                 java.util.ResourceBundle" session="true" 
%>
<jsp:useBean id="jobDetails" scope="request" class="com.globalsight.everest.webapp.javabean.NavigationBean"/>
<jsp:useBean id="jobSourceFiles" scope="request" class="com.globalsight.everest.webapp.javabean.NavigationBean"/>
<jsp:useBean id="jobCosts" scope="request" class="com.globalsight.everest.webapp.javabean.NavigationBean"/>
<jsp:useBean id="jobDetailsPDFs" scope="request" class="com.globalsight.everest.webapp.javabean.NavigationBean"/>
<jsp:useBean id="jobComments" scope="request" class="com.globalsight.everest.webapp.javabean.NavigationBean"/>
<jsp:useBean id="jobAttributes" scope="request" class="com.globalsight.everest.webapp.javabean.NavigationBean"/>
<jsp:useBean id="jobReports" scope="request" class="com.globalsight.everest.webapp.javabean.NavigationBean"/>
<jsp:useBean id="jobScorecard" scope="request" class="com.globalsight.everest.webapp.javabean.NavigationBean"/>
<% 
   ResourceBundle bundle = PageHandler.getBundle(session);
   long jobId = (Long) request.getAttribute("jobId");
   String jobCommentsURL = jobComments.getPageURL() + "&jobId=" + request.getAttribute("jobId");

   SessionManager sessionMgr = (SessionManager)session.getAttribute(WebAppConstants.SESSION_MANAGER);
   User user = (User) sessionMgr.getAttribute(WebAppConstants.USER);

   String title = bundle.getString("lb_job_details") + " " + bundle.getString("lb_pdfs");
   String label_createPDF = bundle.getString("lb_create_pdf");
   String label_download = bundle.getString("lb_download");
   String label_reset = bundle.getString("lb_reset");
%>
<html>
<head>
<title><%=title%></title>
<STYLE>
TR.standardText 
{
    vertical-align: top;
}
</STYLE>
<script SRC="/globalsight/includes/setStyleSheet.js"></script>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>
<script type="text/javascript" src="/globalsight/jquery/jquery-1.6.4.js"></script>
<script type="text/javascript">
var needWarning = false;
var objectName = "";
var guideNode = "myJobs";
var helpFile = "<%=bundle.getString("help_job_details_PDFs")%>";
var basicURL = "<%=jobDetailsPDFs.getPageURL()%>" + "&action=";
var action_createPDF = basicURL + "<%=JobDetailsConstants.ACTION_PDF_CREATE%>";
var action_downloadPDF = basicURL + "<%=JobDetailsConstants.ACTION_PDF_DOWNLOAD%>";
var action_cancel = basicURL + "<%=JobDetailsConstants.ACTION_PDF_CANCEL%>";
var action_viewPDFBO = basicURL + "<%=JobDetailsConstants.ACTION_VIEW_PDFBO%>";
var state_default = "<%=bundle.getString("lb_pdf_state_default")%>";
var state_inprogress = "<%=bundle.getString("lb_inprogress")%>";
var msg_no_workflow = "<%=bundle.getString("msg_no_workflow")%>";
var msg_no_pdf_download = "<%=bundle.getString("msg_no_pdf_download")%>";
var msg_pdfStatus_error = "<%=bundle.getString("msg_pdfStatus_error")%>";


$(document).ready(function(){
	// Modify the css of Tab.
	$("#jobDetailsPDFsTab").removeClass("tableHeadingListOff");
	$("#jobDetailsPDFsTab").addClass("tableHeadingListOn");
	$("#jobDetailsPDFsTab img:first").attr("src","/globalsight/images/tab_left_blue.gif");
	$("#jobDetailsPDFsTab img:last").attr("src","/globalsight/images/tab_right_blue.gif");	
});

function fnCheckAll(){
	if($("#checkAll").attr("checked")){
		$("input[name='dataCheck']").each(function(){
			$(this).attr("checked", true);
		})
	}else{
		$("input[name='dataCheck']").each(function(){
			$(this).attr("checked", false);
		})
	}
}

// The function for create PDF. The PDF Status should be None.
function fnCreatePDF(){
	var wfids = [];
	
	// Check PDF Status
	var errorPdfStaus = false;
	$("input[name='dataCheck']:checked").each(function(i){
		if($(this).closest('tr').children().last().text() != state_default){
			errorPdfStaus = true;
			return false;
		}
	});
	if(errorPdfStaus){
		alert(msg_pdfStatus_error);
		return;
	}
	
	$("input[name='dataCheck']:checked").each(function(i){
		wfids[i] = $(this).val();
		$(this).closest('tr').children().last().html(state_inprogress);
	});
	
	if(wfids.length == 0){
		alert(msg_no_workflow);
		return;
	}
	
	var action = action_createPDF + "&t=" + new Date();
	$.getJSON(action, {'wfids' : wfids.toString()});
}

function fnDownloadPDF(){
	var wfids = [];
	$("input[name='dataCheck']:checked").each(function(i){
		wfids[i] = $(this).val();
	});
	
	if(wfids.length == 0){
		alert(msg_no_workflow);
		return;
	}
	
	$.ajax({
		dataType: "json",
		url: action_viewPDFBO + "&t=" + new Date(),
		data: {'wfids' : wfids.toString()},
		success: function(dataArray){
			var isSubmit = false;
			$.each(dataArray,function(key,val){
				//console.log('Array Index，索引：'+key+'对应的值为：'+val.existPDFFileNumber);
				if(val.existPDFFileNumber > 0){
					isSubmit = true;
					return;
				}
			});
			
			if(isSubmit){
				$("input[name='wfids']").val(wfids);	
				dataForm.action = action_downloadPDF;
				dataForm.submit();
			}else{
				alert(msg_no_pdf_download);
			}
		}
	});
}

// Reset Create PDF
function fnReset(){
	var wfids = [];
	$("input[name='dataCheck']:checked").each(function(i){
		wfids[i] = $(this).val();
		$(this).closest('tr').children().last().html(state_default);
	});
	
	if(wfids.length == 0){
		alert(msg_no_workflow);
		return;
	}
	
	var action = action_cancel + "&t=" + new Date();
	$.getJSON(action, {'wfids' : wfids.toString()});
}
</script>
</head>
<body leftmargin="0" rightmargin="0" topmargin="0" marginwidth="0" marginheight="0" onload="load()"; id="idBody" onunload="unload()" class="tundra">
<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>
<div id="contentLayer" class="standardText" style="position: absolute; z-index: 9; top: 108px; left: 20px; right: 20px;">
<div id="includeSummaryTabs">
	<%@ include file="/envoy/projects/workflows/includeJobSummaryTabs.jspIncl" %>
</div>
<div style="clear:both;padding-top:1em" ></div>
<input type="hidden" name="jobId" value="<%=jobId%>">
<div id="pdfDiv" style="width:900px;">
<form name="dataForm" method="post" action="">
<input type="hidden" name="wfids" value="">
</form>
<div class="standardTextBold" style="margin:0;padding:0"><%=bundle.getString("lb_pdfs")%></div>
<table cellpadding="2" cellspacing="0" border="0" style="min-width:600px;border:solid 1px slategray">
		<thead>
			<tr>
			    <td class="tableHeadingBasic myTableHeading"><input id="checkAll" type="checkbox" onclick="fnCheckAll();"></td>
			    <td class="tableHeadingBasic myTableHeading"><span class="whiteBold"><%=bundle.getString("lb_target_locale")%>&nbsp;&nbsp;&nbsp;&nbsp;</span></td>
			    <td class="wordCountHeadingWhite myTableHeading" style="text-align:center"><%=bundle.getString("lb_word_count")%></td>
			    <td class="tableHeadingBasic myTableHeading" style="text-align:center"><%=bundle.getString("lb_pdfs_file_number")%></td>
			    <td class="tableHeadingBasic myTableHeading" style="text-align:center"><%=bundle.getString("lb_pdfStatus")%></td>
			</tr>
		</thead>
		<tbody>
		<c:forEach items="${JobDetailsPDFList}" var="item">
			<c:choose>
				<c:when test="${(item.wokflowState == 'EXPORT_FAILED') || (item.wokflowState == 'IMPORT_FAILED')}">
					<tr class="warningText">
				</c:when>
				<c:otherwise>
					<tr class="standardText">
				</c:otherwise>
			</c:choose>
				<td>
					<input name="dataCheck" type="checkbox" value="${item.workflowId}"/>
				</td>
				<td>${item.targetLocaleDisplayName}</td>
				<td style="text-align:center">${item.totalWordCount}</td>
				<td style="text-align:center">${item.existPDFFileNumber}/${item.totalPDFFileNumber}</td>
				<td style="text-align:center">${item.statusDisplayName}</td>
			</tr>
		</c:forEach>
		</tbody>
</table>
<div id="buttonDiv" style="padding-top:5px;width:80%">
<input type="button" id="btnCreatePDF" onclick="fnCreatePDF();" value="<%=label_createPDF%>" class="standardText">
<input type="button" id="btnDownPDF" onclick="fnDownloadPDF();" value="<%=label_download%>" class="standardText">
<input type="button" id="btnCancel" onclick="fnReset();" value="<%=label_reset%>" class="standardText">
</div>
</div>
</div>
</body>
</html>
