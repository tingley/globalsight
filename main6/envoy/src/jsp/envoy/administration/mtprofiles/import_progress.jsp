<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ page contentType="text/html; charset=UTF-8"
         errorPage="/envoy/common/error.jsp"
         session="true" 
          import = "com.globalsight.everest.webapp.pagehandler.PageHandler,
         java.util.ResourceBundle"
%>
<%
ResourceBundle bundle = PageHandler.getBundle(session);
String lb_mtp_import = bundle.getString("lb_mtp_import");
String lb_processing_import_file = bundle.getString("lb_processing_import_file");
String lb_please_wait = bundle.getString("lb_please_wait");
String lb_ok = bundle.getString("lb_ok");
String lb_refresh = bundle.getString("lb_refresh");
String lb_back = bundle.getString("lb_back");
%>
<html>
<head>
<title><c:out value="<%=lb_mtp_import %>"/></title>
<STYLE type="text/css">
#idProgressContainer { border: solid 1px #0C1476; z-index: 1; 
                 position: absolute; top: 42; left: 20; width: 400; height:15px}
#idProgress    { text-align: center; z-index: 2; font-weight: bold; }
#idProgressBar { background-color: #a6b8ce; z-index: 0;
                 border: solid 1px #0C1476; 
                 position: absolute; top: 42; left: 20; width: 0; height:15px}
</STYLE>
<script type="text/javascript" src="/globalsight/jquery/jquery-1.6.4.min.js"></script>
<script type="text/javascript" src="/globalsight/includes/utilityScripts.js"></script>
<script type="text/javascript" src="/globalsight/includes/setStyleSheet.js"></script>
<script type="text/javascript" src="/globalsight/includes/ArrayExtension.js"></script>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<script type="text/javascript">
var guideNode = "mtProfiles";
var helpFile = "<%=bundle.getString("help_mtprofile_import")%>";
function confirmJump()
{
	return true;
}

$(document).ready(function(){
	$("#okBtn").click(function(){
		document.location.href="/globalsight/ControlServlet?activityName=mtProfiles";
	});
	$("#backBtn").click(function(){
		document.location.href="/globalsight/ControlServlet?linkName=mtpImport&pageName=MTP&formAction=import";
	});
	$("#refreshBtn").click(function(){
		$("#idProgressBar").stop(true);
		refreshBar();
	});
});

function doOnLoad() {
	loadGuides();
	startUpload();
	refreshBar();
}

function startUpload() {
	$.post("/globalsight/ControlServlet?pageName=MTPIMPORT&linkName=startUpload&formAction=doImport");
}

function refreshBar() {
	if ($.browser.msie) {
		$("#idProgressBar").height(17);
	}
	$.get("/globalsight/ControlServlet?pageName=MTPIMPORT&linkName=startUpload&formAction=refreshProgress", 
		{"no":Math.random()},
		function(data){
			var tmp = data.split("&");
			var percentage = tmp[0];
			var errorMsg = tmp[1];
			
			$("#msgBox").html(errorMsg);
			var wii = parseInt(percentage) / 100 * 400;
			$("#idProgressBar").animate({width : wii}, function(){
				$("#idProgress").html(percentage + "%");
				
				if (percentage == 100) {
					$("#okBtn").show();
					$("#refreshBtn").hide();
					$("#idPleaseWait").hide();
				} else {
					setTimeout('refreshBar()',1000);
				}
			});
		});
}
</script>

</head>
<body CLASS="standardText" leftmargin="0" rightmargin="0" topmargin="0" marginwidth="0" marginheight="0" onload="doOnLoad()">
<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>
<div id="contentLayer" style="position: absolute; z-index: 9; top: 108; left: 20px; right: 20px;">
<SPAN CLASS="mainHeading" id="idHeading"><%=lb_processing_import_file %></SPAN><BR>
<SPAN CLASS="standardTextItalic" id="idPleaseWait"><%=lb_please_wait %></SPAN>

<DIV id="idProgressContainer">
  <DIV id="idProgress">0%</DIV>
</DIV>
<DIV id="idProgressBar"></DIV>
<TABLE CELLPADDING="2" CELLSPACING="0" BORDER="0" CLASS="standardText">
<tr><td height="50px">&nbsp;</td></tr>
</TABLE>
<TABLE CELLPADDING="2" CELLSPACING="0" BORDER="0" CLASS="standardText">
<tr>
	<td id="msgBox" height="25px" style="padding-left:20px"></td>
</tr>
</TABLE>
<br><br>
<input type="button" id="okBtn" value="<%=lb_ok %>" style="display:none">
<input type="button" id="refreshBtn" value="<%=lb_refresh %>">
<input type="button" id="backBtn" value="<%= lb_back%>" >
</div>
</body>
</html>