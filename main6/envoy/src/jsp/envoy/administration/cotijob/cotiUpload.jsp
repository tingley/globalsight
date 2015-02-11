<%@page import="com.globalsight.everest.coti.util.COTIConstants"%>
<%@page import="com.globalsight.everest.webapp.pagehandler.administration.cotijob.CotiJobComparator"%>
<%@page import="com.globalsight.everest.webapp.pagehandler.administration.cotijob.CotiJobsManagement"%>
<%@ taglib uri="/WEB-INF/tlds/globalsight.tld" prefix="amb" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html; charset=UTF-8"
         errorPage="/envoy/common/error.jsp"
         import="java.util.*,com.globalsight.everest.webapp.javabean.NavigationBean,
    com.globalsight.everest.servlet.util.SessionManager,
     com.globalsight.everest.permission.PermissionGroup, 
     com.globalsight.everest.util.comparator.StringComparator,
     com.globalsight.everest.util.comparator.GlobalSightLocaleComparator,
     com.globalsight.util.GlobalSightLocale,
         com.globalsight.util.resourcebundle.ResourceBundleConstants,
         com.globalsight.util.resourcebundle.SystemResourceBundle,
         com.globalsight.everest.securitymgr.FieldSecurity,
         com.globalsight.everest.webapp.WebAppConstants,
         com.globalsight.everest.webapp.pagehandler.PageHandler,
         com.globalsight.everest.webapp.pagehandler.administration.users.UserConstants,
         com.globalsight.everest.webapp.pagehandler.administration.users.UserComparator,
         com.globalsight.everest.webapp.pagehandler.administration.users.UserSearchParams,
         com.globalsight.everest.permission.Permission,
         com.globalsight.everest.permission.PermissionSet,         
         com.globalsight.everest.foundation.User,
         com.globalsight.everest.company.CompanyWrapper,
         com.globalsight.util.edit.EditUtil,
         java.text.MessageFormat,
         java.util.ArrayList,
         java.util.Locale, java.util.ResourceBundle, java.util.Vector"
         session="true"
%>
<jsp:useBean id="self" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="cancel" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="ok" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<%
	response.setHeader("Pragma","No-cache");
	response.setHeader("Cache-Control","no-store");
	response.setDateHeader("Expires", 0);

    ResourceBundle bundle = PageHandler.getBundle(session);
    
    String title = "Upload COTI package";
    String selfURL = self.getPageURL();
    String cancelURL = cancel.getPageURL();
    String okURL = ok.getPageURL();
    String refreshUrl = selfURL;
    
    String helperText = "Upload your COTI packages here. Supporting zip compressed.";
    String errorResult = (String) request.getAttribute("COTI_errorResult");
    String successResult = (String) request.getAttribute("COTI_successResult");
    
    SessionManager sessionMgr = (SessionManager) session
            .getAttribute(WebAppConstants.SESSION_MANAGER);
%>
<html>
<head>
<link rel="STYLESHEET" type="text/css" href="/globalsight/includes/css/createJob.css"/>
<link rel="STYLESHEET" type="text/css" href="/globalsight/includes/taskList.css">
<META HTTP-EQUIV="Pragma" CONTENT="no-cache">
<title><%= title%></title>
<script type="text/javascript" src="/globalsight/jquery/jquery-1.6.4.min.js"></script>
<script type="text/javascript" src="/globalsight/includes/utilityScripts.js"></script>
<script type="text/javascript" src="/globalsight/includes/setStyleSheet.js"></script>
<script type="text/javascript" src="/globalsight/includes/ArrayExtension.js"></script>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/radioButtons.js"></SCRIPT>
<script type="text/javascript">
var needWarning = false;
var objectName = "";
var guideNode = "";
var helpFile = "<%= bundle.getString("help_coti_job")%>";

var checkBoxValue = "";
var uploadResult = "<%= (successResult == null? "" : successResult) %>";
var errorResult = "<%= (errorResult == null? "" : errorResult) %>";

function loadPage()
{
}

function submitForm(buttonClicked, curJobId)
{
	if (buttonClicked == "Upload")
	{
		UploadForm.action = "<%= selfURL %>" + "&uploadAction=startUpload";
		UploadForm.submit();
		return;
	}
	
	if (buttonClicked == "Cancel")
	{
		UploadForm.action = "<%= okURL %>";
		UploadForm.submit();
		return;
	}
}

</script>

</head>

<body leftmargin="0" rightmargin="0" topmargin="0" marginwidth="0" marginheight="0" onload="loadPage()">
<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>
<%@ include file="/envoy/administration/cotijob/cotiJobSort.jspIncl" %>
<STYLE>
<%--
This stylesheet should be in the HEAD element but the skin bean
is defined in header.jspIncl which must be included in the body.
--%>
.list {
	border: 1px solid <%=skin.getProperty("skin.list.borderColor")%>;
}
.headerCell {
    padding-right: 10px;
    padding-top: 2px;
    padding-bottom: 2px;
}
</STYLE>

<div id="contentLayer" style="position: absolute; z-index: 9; top: 108; left: 20px; right: 20px;">
<TABLE CELLSPACING="0" CELLPADDING="0" BORDER="0">
    <TR VALIGN="TOP">
        <TD COLSPAN=2>
            <SPAN CLASS="mainHeading">
            <%=title%>
            </SPAN>
        </TD>
    </TR>
    <TR VALIGN="TOP" CLASS=standardText>    
        <TD>
        <br>
        <%=helperText%>
        </TD>
    </TR>
    <TR><TD><br/></TD></TR>
    <TR VALIGN="TOP" CLASS=standardText> 
        <TD>
        <span style="color:red"><%= (errorResult == null? "" : errorResult) %></span>
        <span style="color:green"><%= (successResult == null? "" : successResult) %></span>
        </TD>
    </TR>
</TABLE>

<div>
<p>&nbsp;</p>
</div>

<TABLE CELLSPACING="0" CELLPADDING="0" BORDER="0">
<FORM NAME="UploadForm" METHOD="POST" enctype="multipart/form-data">
<TR CLASS=standardText><TD> Choose one COTI package or ZIP file to upload: </TD></TR>
<TR><TD>&nbsp;</TD></TR>
<TR CLASS=standardText><TD><input type="file" name="uploadFile" id="uploadFile" /> </TD></TR>

<TR><TD></TD></TR>

<TR><TD>
<DIV ID="ButtonLayer" ALIGN="LEFT">
<br>
<INPUT TYPE="BUTTON" NAME=Upload id="Upload" VALUE="Upload" onClick="submitForm('Upload');">
<INPUT TYPE="BUTTON" NAME=Cancel id="Cancel" VALUE="Back to List" onClick="submitForm('Cancel');">
</DIV>
</TD></TR>
</FORM>
</TABLE>

</div>
</body>
</html>