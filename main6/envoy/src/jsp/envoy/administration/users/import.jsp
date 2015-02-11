<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ page contentType="text/html; charset=UTF-8"
         errorPage="/envoy/common/error.jsp"
         session="true"
%>
<html>
<head>
<title><c:out value="${lb_user_import}"/></title>
<script type="text/javascript" src="/globalsight/jquery/jquery-1.6.4.min.js"></script>
<script type="text/javascript" src="/globalsight/includes/utilityScripts.js"></script>
<script type="text/javascript" src="/globalsight/includes/setStyleSheet.js"></script>
<script type="text/javascript" src="/globalsight/includes/ArrayExtension.js"></script>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<script type="text/javascript">
var guideNode = "users";
var helpFile = "<c:out value='${help_users_import_screen}'/>";
function confirmJump()
{
	return true;
}

$(document).ready(function(){
	$("#cancelBtn").click(function(){
		document.location.href="/globalsight/ControlServlet?activityName=users";
	});
	
	$("#uploadBtn").click(function(){
		var file = $("#fileInput").val();
		var ext = file.substring(file.lastIndexOf(".") + 1);
		if(ext.toLowerCase() != "xml") {
			alert("<c:out value='${msg_alert_user_import}'/>");
			return;
		}
		var isIgnore = $('input:radio[name="existedUserRadio"]:checked').val();
	    if ("0" == isIgnore) {
	    	document.userImportForm.action += "&ifUserExistedFlag=0";
	    } else {
	    	document.userImportForm.action += "&ifUserExistedFlag=1";
	    }
		$("#progressbar").show();
		document.userImportForm.submit();
	});
});
</script>

</head>
<body leftmargin="0" rightmargin="0" topmargin="0" marginwidth="0" marginheight="0" onload="loadGuides()">
<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>
<div id="contentLayer" style="position: absolute; z-index: 9; top: 108; left: 20px; right: 20px;">
<span class='mainHeading'><c:out value="${lb_user_import}"/></span><p>
<table cellspacing=0 cellpadding=0 border=0 class=standardText><tr><td width="100%"><c:out value="${helper_text_users_import}"/></td></tr></table>

<form name="userImportForm" method="post" action="/globalsight/ControlServlet?pageName=USRIMPORT&linkName=startUpload&action=startUpload" ENCTYPE="multipart/form-data">
<TABLE CELLPADDING="2" CELLSPACING="0" BORDER="0" CLASS="standardText">
	<tr>
		<td colspan="2"><c:out value="${msg_file_none}"/></td>
	</tr>
	<tr>
		<td colspan="2"><input type="file" calss="standardText" size="60" id="fileInput" name="fileInput"></td>
	</tr>
	<tr><td  colspan="2" height="20px">&nbsp;</td></tr>
    <tr>
        <td>If user existed already: </td>
        <td>
            <input id="existedUserRadio1" type="radio" checked value="0" name="existedUserRadio">
            <label for="existedUserRadio1">Ignore</label>
            <input id="existedUserRadio2" type="radio" value="1" name="existedUserRadio">
            <label for="existedUserRadio2">Merge/Overwrite</label>
        </td>
    </tr>
    <tr>
		<td colspan="2"><span id="progressbar" style="display:none"><img src="/globalsight/images/createjob/progressbar.gif" style="width:400px;height:20px"></img></span></td>
	</tr>
</TABLE>
<br><br>
<input type="button" id="cancelBtn" value="<c:out value='${lb_cancel}'/>" >   
<input type="button" id="uploadBtn" value="<c:out value='${lb_upload}'/>" >
</form>
</div>
</body>
</html>