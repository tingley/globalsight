<%@ taglib uri="/WEB-INF/tlds/globalsight.tld" prefix="amb" %>
<%@ page contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/activityError.jsp"
    import="com.globalsight.everest.servlet.util.SessionManager,
            com.globalsight.everest.webapp.WebAppConstants,
            com.globalsight.everest.webapp.javabean.NavigationBean,
            com.globalsight.everest.webapp.pagehandler.PageHandler,
            com.globalsight.everest.webapp.pagehandler.administration.config.segmentationrulefile.SegmentationRuleConstant,
            com.globalsight.everest.webapp.webnavigation.LinkHelper,
            com.globalsight.everest.servlet.EnvoyServletException,
            com.globalsight.util.edit.EditUtil,
            com.globalsight.util.GeneralException,
            java.text.MessageFormat,
            java.util.*"
    session="true"
%>
<jsp:useBean id="upload" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="cancel" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<%
    ResourceBundle bundle = PageHandler.getBundle(session);
    Locale uiLocale = (Locale)session.getAttribute(WebAppConstants.UILOCALE);
    SessionManager sessionMgr = (SessionManager)session.getAttribute(
      WebAppConstants.SESSION_MANAGER);

    String uploadURL = upload.getPageURL() + "&action=" + SegmentationRuleConstant.UPLOAD;
    String cancelURL = cancel.getPageURL() + "&action=" + SegmentationRuleConstant.CANCEL;
%>
<html>
<head>
<META HTTP-EQUIV="content-type" CONTENT="text/html;charset=UTF-8">
<title><%=bundle.getString("lb_segmentation_rule_upload_file")%></title>
<script SRC="/globalsight/includes/utilityScripts.js"></script>
<script SRC="/globalsight/includes/setStyleSheet.js"></script>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>
<script>
var needWarning = true;
var objectName = "<%=bundle.getString("lb_segmentation_rule")%>";
var guideNode="segmentationRules";
var helpFile = "<%=bundle.getString("help_segmentation_rules_basic_screen")%>";

function submitForm(formAction)
{
	if (formAction == "cancel")
    {
        fileForm.action = "<%=cancelURL%>";
        fileForm.submit();
    }
	else if (formAction == "upload")
	{
		var file_name = fileForm.filename.value;
		var file_extension = file_name.substring(file_name.lastIndexOf("."), file_name.length);
		var accept_extension = "*.srx,*.xml,*.txt";
		
		if(file_name == "")
		{
			alert("<%=bundle.getString("lb_select_files_to_upload")%>");
		}
		else if(accept_extension.indexOf(file_extension.toLowerCase()) == -1)
		{
			alert("<%=bundle.getString("lb_rules_files_extension")%>" + "\n" + accept_extension);
		}
		else
		{
			fileForm.submit();
		}
	}
}

function checkExtension()
{
	var file_name = fileForm.filename.value;
	var file_extension = file_name.substring(file_name.lastIndexOf("."), file_name.length);
	var accept_extension = "*.srx,*.xml,*.txt";
	
	if(accept_extension.indexOf(file_extension.toLowerCase()) == -1)
	{
		alert("<%=bundle.getString("lb_rules_files_extension")%>" + "\n" + accept_extension);
	}
}
</script>
</head>
<body leftmargin="0" rightrmargin="0" topmargin="0" marginwidth="0" marginheight="0"
      onload="loadGuides()">
<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>
<div id="contentLayer" style="position: absolute; z-index: 9; top: 108; left: 20px; right: 20px;">
<span class="mainHeading"><%=bundle.getString("lb_segmentation_rule_upload_file")%></span>
<br>
<br>
<table border="0" cellspacing="2" cellpadding="2" class="standardText">
  <tr><td></td></tr>
  <FORM NAME="fileForm" ACTION="<%=uploadURL%>"
	  	 ENCTYPE="multipart/form-data" METHOD="post">
  <tr>
  <td>
  <%=bundle.getString("lb_upload_file")%> 
  &nbsp;&nbsp;&nbsp;&nbsp;
  <INPUT TYPE="file" NAME="filename" id="filename" SIZE=40 onchange="checkExtension()">
  </td>
  </tr>
  <tr>
  <td>
  <amb:permission name="<%=Permission.SEGMENTATIONRULE_NEW%>" >
  <INPUT TYPE="BUTTON" VALUE="<%=bundle.getString("lb_upload")%>"
  onclick="submitForm('upload');">
 </amb:permission>
 &nbsp;&nbsp;&nbsp;&nbsp;
 <INPUT TYPE="BUTTON" VALUE="<%=bundle.getString("lb_cancel")%>"
 onclick="submitForm('cancel');">
  </td>
  </tr>
  </FORM>
</table>
</body>
</html>
