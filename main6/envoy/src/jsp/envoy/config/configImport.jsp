<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ page contentType="text/html; charset=UTF-8"
         errorPage="/envoy/common/error.jsp"
         session="true"
         import = "com.globalsight.everest.webapp.WebAppConstants,
         com.globalsight.everest.servlet.util.SessionManager,
         com.globalsight.everest.webapp.pagehandler.PageHandler,
         com.globalsight.everest.company.CompanyWrapper,
         com.globalsight.everest.company.CompanyThreadLocal,
         java.util.ResourceBundle,
         java.util.List"
%>
<jsp:useBean id="startUpload" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<%
SessionManager sessionManager = (SessionManager) session.getAttribute(WebAppConstants.SESSION_MANAGER);
ResourceBundle bundle = PageHandler.getBundle(session);
String lb_cancel = bundle.getString("lb_cancel");
String lb_upload = bundle.getString("lb_upload");
String configsImport = bundle.getString("lb_config_import");
String selectFile = bundle.getString("msg_file_none");
String helper_locale_pair_import = bundle.getString("helper_text_config_import");

String startUploadURL = startUpload.getPageURL()+"&action=startUpload";
String currentCompanyId = CompanyThreadLocal.getInstance().getValue();
String disableUploadFileTypes = CompanyWrapper.getCompanyById(currentCompanyId).getDisableUploadFileTypes();
%>
<html>
<head>
<title><c:out value="<%= configsImport%>"/></title>
<script type="text/javascript" src="/globalsight/jquery/jquery-1.6.4.min.js"></script>
<script type="text/javascript" src="/globalsight/includes/setStyleSheet.js"></script>
<script type="text/javascript" src="/globalsight/includes/ArrayExtension.js"></script>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<script type="text/javascript">
var guideNode = "localePairs";
var helpFile = "<%=bundle.getString("help_configuration_screen")%>";
var startUploadURL = "<%=startUploadURL%>";
var companyId = -1;
var disableUploadFileTypes = "<%=disableUploadFileTypes%>";
function confirmJump()
{
	return true;
}

$(document).ready(function(){
	if(companyId == 1){
		
	}
	$("#cancelBtn").click(function(){
		document.location.href="/globalsight/ControlServlet?activityName=configExportImport";
	});
	
	$("#uploadBtn").click(function(){
		var file = $("#fileInput").val();
		var ext = file.substring(file.lastIndexOf("."));
		if(file == "" || file == undefined || file == null){
			alert("<%=bundle.getString("msg_alert_filter_import")%>");
			return;
		}
		
		$("#progressbar").show();
     	document.configImportForm.action = startUploadURL;
     	document.configImportForm.submit();
	 });
});
</script>
<%@ include file="/envoy/common/shortcutIcon.jspIncl" %>
</head>
<body leftmargin="0" rightmargin="0" topmargin="0" marginwidth="0" marginheight="0" onload="loadGuides()">
<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>
<div id="contentLayer" style="position: absolute; z-index: 9; top: 108; left: 20px; right: 20px;">
<span class='mainHeading'><c:out value="<%= configsImport%>"/></span><p>
<table cellspacing=0 cellpadding=0 border=0 class=standardText><tr><td width="100%"><c:out value="<%=helper_locale_pair_import %>"/></td></tr></table>

<form name="configImportForm" method="post" action=""  ENCTYPE="multipart/form-data">
<TABLE CELLPADDING="2" CELLSPACING="0" BORDER="0" CLASS="standardText">
	<tr>
		<td colspan="2"><c:out value="<%= selectFile%>"/></td>
	</tr>
	<tr>
		<td colspan="2"><input type="file" calss="standardText" size="60" id="fileInput" name="fileInput" onchange="checkAndUpload()"></td>
	</tr>

    <tr>
		<td colspan="2"><span id="progressbar" style="display:none"><img src="/globalsight/images/createjob/progressbar.gif" style="width:400px;height:20px"></img></span></td>
	</tr>
</TABLE>
<br><br>
<input type="button" id="cancelBtn" value="<%= lb_cancel%>" >   
<input type="button" id="uploadBtn" value="<%= lb_upload%>" >
</form>
</div>
</body>
</html>