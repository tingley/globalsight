<%@ page contentType="text/html; charset=UTF-8"
	errorPage="/envoy/common/error.jsp"
	import="com.globalsight.everest.webapp.javabean.NavigationBean,
	com.globalsight.everest.webapp.pagehandler.PageHandler,
	com.globalsight.everest.localemgr.CodeSet,
	com.globalsight.everest.webapp.pagehandler.administration.config.uilocale.UILocaleConstant,
	com.globalsight.everest.webapp.pagehandler.administration.config.uilocale.UILocale,
	java.util.*"
	session="true"%>
<%@ taglib uri="/WEB-INF/tlds/globalsight.tld" prefix="amb"%>
<jsp:useBean id="upload" scope="request" class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="previous" scope="request" class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="self" scope="request" class="com.globalsight.everest.webapp.javabean.NavigationBean" />

<%
	ResourceBundle bundle = PageHandler.getBundle(session);

	String uploadURL = upload.getPageURL() + "&action="
			+ UILocaleConstant.UPLOAD;
	String previousURL = previous.getPageURL() + "&action="
    		+ UILocaleConstant.PREVIOUS;
    
	String helperText = bundle.getString("helper_text_uilocale_uploader");
    String title = bundle.getString("lb_upload_resource");
    Collection encodings = (Collection) request.getAttribute("encodings");
    
    String lastEncoding = (String) request.getAttribute(UILocaleConstant.LAST_ENCODING);
    String uploadMsg = (String) request.getAttribute(UILocaleConstant.UPLOAD_MSG);
    boolean isJustUpload = uploadMsg != null;
    Object uploadResult = request.getAttribute(UILocaleConstant.UPLOAD_RESULT);
    boolean isUploadSuccess = (uploadResult == null) ? false : "true".equalsIgnoreCase(uploadResult.toString());
%>
<%@page import="com.globalsight.util.edit.EditUtil"%>
<HTML>
<!-- uilocaleuploader.jsp -->
<HEAD>
<TITLE><%=title%></TITLE>
<script SRC="/globalsight/includes/utilityScripts.js"></script>
<SCRIPT SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<SCRIPT SRC="/globalsight/includes/radioButtons.js"></SCRIPT>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/dojo.js"></SCRIPT>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl"%>
<%@ include file="/envoy/common/warning.jspIncl"%>
<SCRIPT>

String.prototype.trim = function() { return this.replace(/^\s+|\s+$/, ''); };

var needWarning = false;
var objectName = "";
var guideNode = "systemParameter";
var helpFile = "<%=bundle.getString("help_uilocale_main")%>";
var msgUploadFormat = "<%=bundle.getString("msg_upload_uilocale_resource_format")%>";
var msgUploadEmpty = msgUploadFormat;
var msgUploadEncodingNon = "<%=bundle.getString("msg_upload_uilocale_encoding")%>";
var useSelect = false;

function setUseSelect(vl)
{
	useSelect = vl;

	uploadResForm.selectEncoding.disabled = !useSelect;
	uploadResForm.txtEncoding.disabled = useSelect;

	if (useSelect)
	{
		uploadResForm.txtEncoding.value = "";
	}
	else
	{
		uploadResForm.selectEncoding.selectedIndex = 0;
	}
}

function previous()
{
    uploadResForm.action="<%=previousURL%>";
    uploadResForm.submit();
}

function handleKeyPress(e)
{
	var key=e.keyCode || e.which;
	if (key==13)
	{
		uploadRes();
	}
}

function uploadRes()
{
	var filename = uploadResForm.resFile.value.toLowerCase();
	var filenamelen = filename.length;

	if (filenamelen == 0)
	{
		alert(msgUploadEmpty);
		return;
	}

	if (!(filename.lastIndexOf(".zip") == filenamelen - 4 || filename.lastIndexOf(".properties") == filenamelen - 11))
	{
		alert(msgUploadFormat);
		return;
	}

	var encodingValue = (useSelect) ? uploadResForm.selectEncoding.value : uploadResForm.txtEncoding.value;
	if(encodingValue) encodingValue = encodingValue.trim();

	if (encodingValue == "" || encodingValue == "-1")
	{
		alert(msgUploadEncodingNon);
		return;
	}

	uploadResForm.<%=UILocaleConstant.ENCODING %>.value = encodingValue;
	uploadResForm.action="<%=uploadURL%>";
	uploadResForm.submit();
}
</SCRIPT>
</HEAD>
<BODY LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0" MARGINWIDTH="0" MARGINHEIGHT="0" onload="loadGuides()">
<%@ include file="/envoy/common/header.jspIncl"%>
<%@ include file="/envoy/common/navigation.jspIncl"%>
<%@ include file="/envoy/wizards/guides.jspIncl"%>
<DIV ID="contentLayer" STYLE="POSITION: ABSOLUTE; Z-INDEX: 9; TOP: 108px; LEFT: 20px; RIGHT: 20px;"><amb:header title="<%=title%>" helperText="<%=helperText%>" />

<div style="float: left">
<form name="uploadResForm" METHOD="POST" ENCTYPE="multipart/form-data">
<table class="standardText">
    <tr>
        <td><%=bundle.getString("lb_file")%><span class="asterisk">*</span>:</td>
        <td><input type="file" name="resFile" SIZE="40"></td>
    </tr>
    <tr>
        <td valign="top"><%=bundle.getString("lb_uilocale_file_encoding")%><span class="asterisk">*</span>:</td>
		<td>
		<input type="radio" name="encodingSrc" checked="checked"  onclick="setUseSelect(false)" />
		<input type="text" name="txtEncoding" maxlength="30" value="<%=(lastEncoding == null) ? "" : lastEncoding %>" onkeypress="handleKeyPress(event)" />
		&nbsp;&nbsp;
		<input type="radio" name="encodingSrc" onclick="setUseSelect(true)" />
		<select name="selectEncoding" disabled="disabled">
			<option value="-1" selected><%=bundle.getString("lb_choose")%></option>
			<%
            for (Iterator it = encodings.iterator(); it.hasNext();)
            {
                CodeSet code = (CodeSet)it.next();
                out.println("<option value='" + code.getCodeSet() + "'>" +
                         code.getCodeSet() + "</option>");
            }
			%>
		</select>
		<input type="hidden" name="<%=UILocaleConstant.ENCODING %>" value="" />
		</td>
	</tr>
	<tr>
		<td colspan="2">&nbsp;</td>
	</tr>
	<tr>
		<td colspan="2">
		<input type="button" value="<%=bundle.getString("lb_previous") %>" onclick="previous()">
		<% if (userPerms.getPermissionFor(Permission.UILOCALE_UPLOAD_RES)) { %>
		<input type="button" value="<%=bundle.getString("lb_upload") %>" onclick="uploadRes()"> 
		<% } %>
		</td>
	</tr>
	<tr>
		<td colspan="2">&nbsp;</td>
	</tr>
	<tr>
		<td>&nbsp;</td>
		<td>
		<% if (isJustUpload) { %> <span
			style="color: <%=(isUploadSuccess)? "green" : "red" %>"> <%=(isUploadSuccess)? bundle.getString("msg_upload_uilocale_successful") : bundle.getString("lb_upload_failed") %>
		<BR />
		<%=uploadMsg%> </span> <% } %>
		</td>
	</tr>
</table>
</form>
</div>

</DIV>
</BODY>
</HTML>