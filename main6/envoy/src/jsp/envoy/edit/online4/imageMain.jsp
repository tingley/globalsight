<%@ taglib uri="/WEB-INF/tlds/globalsight.tld" prefix="amb" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ page
    contentType="text/html; charset=UTF-8"
    errorPage="error.jsp"
    import="java.util.*,java.io.File,
            com.globalsight.everest.webapp.WebAppConstants,
            com.globalsight.everest.webapp.javabean.NavigationBean,
            com.globalsight.everest.webapp.pagehandler.administration.reports.ReportConstants,
            com.globalsight.everest.webapp.pagehandler.PageHandler,
            com.globalsight.everest.servlet.util.SessionManager,
            com.globalsight.util.GlobalSightLocale,
            java.util.Locale,
            java.util.ResourceBundle"
    session="true"
%>
<jsp:useBean id="self" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean"/>
 <%@ include file="/envoy/common/installedModules.jspIncl" %>
<%
ResourceBundle bundle = PageHandler.getBundle(session);
SessionManager sessionMgr = (SessionManager) session
		.getAttribute(WebAppConstants.SESSION_MANAGER);

String lb_title = "Popup editor Image";
String url_self = self.getPageURL();
String uploadUrl = url_self +
"&" + WebAppConstants.UPLOAD_ACTION +
"=" + WebAppConstants.UPLOAD_ACTION_START_UPLOAD;

String lb_close = bundle.getString("lb_close");
String lb_help = bundle.getString("lb_help");
String lb_upload = bundle.getString("lb_upload");
String lb_sourceLocale = bundle.getString("lb_source_locale");
String lb_targetLocale  = bundle.getString("lb_target_locale");
String lb_source_file = bundle.getString("lb_source_file");
String lb_target_file = bundle.getString("lb_target_file");
String lb_upload_image_difExtension_message = bundle.getString("lb_upload_image_difExtension_message");

Locale uiLocale = (Locale)session.getAttribute(WebAppConstants.UILOCALE);
long jobId = Long.valueOf(sessionMgr.getAttribute(WebAppConstants.JOB_ID).toString());
String taskId = (String)sessionMgr.getAttribute(WebAppConstants.TASK_ID);
String srcPageId = (String)sessionMgr.getAttribute(WebAppConstants.SOURCE_PAGE_ID);
String trgPageId = (String)sessionMgr.getAttribute(WebAppConstants.TARGET_PAGE_ID);
uploadUrl += "&" + WebAppConstants.TASK_ID + "=" + taskId + "&"
		+ WebAppConstants.SOURCE_PAGE_ID + "=" + srcPageId + "&"
		+ WebAppConstants.TARGET_PAGE_ID + "=" + trgPageId;
String str_sourceLocale = (String) request.getAttribute("sourceLanguage");
String str_targetLocale = (String) request.getAttribute("targetLanguage");
String sourceImagePath = (String) request.getAttribute("sourceImagePath");
String targetImagePath = (String) request.getAttribute("targetImagePath");
String targetImageName = targetImagePath.split("/")[targetImagePath
		.split("/").length - 1];
String targetImageSuffix = targetImageName.substring(
		targetImageName.indexOf(".") + 1, targetImageName.length());
String isCanUpload = (String)request.getAttribute("isCanUpload");
boolean displayUploadButton = false;
if(isCanUpload != null && Boolean.parseBoolean(isCanUpload))
{
	displayUploadButton = true;
}
Long currentLocaleId = (Long)request.getAttribute("currentLocaleId");
List<GlobalSightLocale> targetLocalesList = (List<GlobalSightLocale>)request.getAttribute("targetLocalesList");
StringBuffer str_targetLocaleBuffer = new StringBuffer();
if (targetLocalesList != null &&  targetLocalesList.size()>1)
{
	str_targetLocaleBuffer.append("<select name='tarLocales' onchange='switchTargetLocale(this[this.selectedIndex].value)' style='font-size: 8pt;'>");
	GlobalSightLocale trg = null;
	for (int i = 0, max = targetLocalesList.size(); i < max; i++)
	{
	    trg = (GlobalSightLocale)targetLocalesList.get(i);
	
	    str_targetLocaleBuffer.append("<option ");
	
		if(currentLocaleId != null)
		{
			 if (trg.getId()== currentLocaleId)
		        {
		            str_targetLocaleBuffer.append("selected ");
		        }
		}
	    str_targetLocaleBuffer.append("value='").append("&jobId="+jobId+"&sourcePageId="+srcPageId+"&trgId="+trg.getId()).append("'>");
	    str_targetLocaleBuffer.append(trg.getDisplayName(uiLocale));
	    str_targetLocaleBuffer.append("</option>");
	}
	str_targetLocaleBuffer.append("</select>");
}
else
{
	str_targetLocaleBuffer.append(str_targetLocale);
}
%>
<HTML>
<HEAD>
<TITLE><%=lb_title%></TITLE>
<STYLE>
.contentTable{width:100%;table-layout:fixed;}
.top {width:100%;height:26px;position:absolute;Z-INDEX: 10; LEFT:0px;top:0px;border-bottom:4px solid;border-bottom-style:ridge;border-bottom-color:#E8E8E8;}

.title {width:100%;height:26px;position:absolute;Z-INDEX: 10;LEFT:0px;top:30px;}
.titleSource{width:50%;height:100%;position:absolute;left:0px;}
.titleTarget{width:50%;height:100%;position:absolute;right:0px;border-left:4px solid;border-left-style:ridge;border-left-color:#E8E8E8;}

.content{width:100%;position:absolute;Z-INDEX: 10;LEFT:0px;top:56px;bottom:27px;height:auto;}
.contentSource{width:49.6%;height:100%;position:absolute;left:0px;overflow:auto;}
.contentTarget{width:50%;height:100%;position:absolute;right:0px;overflow:auto;border-left:4px solid;border-left-style:ridge;border-left-color:#E8E8E8;}

.foot {width:100%;height:26px;position:absolute;Z-INDEX: 10; LEFT: 0px;bottom:0px;}
.footSource{width:50%;height:100%;position:absolute;left:0px;}
.footTarget{width:50%;height:100%;position:absolute;right:0px;border-left:4px solid;border-left-style:ridge;border-left-color:#E8E8E8;}

/* Background colors for issue status, class name is the status token. */
.open   { background-color: red !important; }
.closed { background-color: lawngreen !important; }
</STYLE>
<link href="/globalsight/jquery/jQueryUI.redmond.css" rel="stylesheet" type="text/css"/>
<script type="text/javascript" src="/globalsight/jquery/jquery-1.6.4.min.js"></script>
<script type="text/javascript" src="/globalsight/jquery/jquery-ui-1.8.18.custom.min.js"></script>
<SCRIPT SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<script>
var url_self = "<%=url_self%>";
var helpFile = "<%=bundle.getString("help_main_image_editor")%>";
var reviewModeText = "<%=WebAppConstants.REVIEW_MODE%>";
var w_editor;
window.focus();

function helpSwitch()
{
    // The variable helpFile is defined in each JSP
    helpWindow = window.open(helpFile,'helpWindow',
      'resizable=yes,scrollbars=yes,WIDTH=600,HEIGHT=400');
    helpWindow.focus();
}

function closeWindow()
{
	window.close();
}

 function uploadTragetImage()
 {
	$("#uploadFormDiv").dialog({width:500, height:100,resizable:true});
 }
 
 function uploadFileMethod()
 {
	 var fileName = document.uploadForm.fileFieldName.value;
	 var str= new Array();   
	 var fileArr = fileName.split(".");
	 if(fileArr != null && fileArr.length > 1)
	 {
		 var fileExtension = fileArr[1];
		 if(fileExtension != "<%=targetImageSuffix%>")
		 {
			alert("<%=lb_upload_image_difExtension_message%>");	
			return;
		 }
	 }
	 else
	 {
		alert("<%=lb_upload_image_difExtension_message%>");	
		return;
	 }
	
	 document.uploadForm.action = "<%=uploadUrl%>"+"&fileFieldName="+fileName;
	 document.uploadForm.submit();
	 $('#uploadFormDiv').dialog('close');
 }
 
 function switchTargetLocale(p_locale)
 {
 	document.location = url_self+"&action=refresh" + p_locale;
 }
 
 function CanClose()
 {
 	if(w_editor)
     {
     	w_editor.close();
     }
 	return true;
 }
</script>
</HEAD>
<BODY id="idBody" onload="">
	<input type="hidden" name="<%=ReportConstants.JOB_IDS%>" value="<%=jobId%>">
	<table class="contentTable">
		<tr class="tableHeadingBasic top" ALIGN="right">
			<td colspan="2" width="100%" style="line-height:26px;">
				<A HREF="#" onclick="closeWindow(); return false;" CLASS="HREFBoldWhite"><%=lb_close%></A> |
				<A HREF="#" onclick="helpSwitch(); return false;" CLASS="HREFBoldWhite"><%=lb_help%></A>&nbsp;&nbsp;&nbsp;&nbsp;
			</td>
		</tr>
		<tr CLASS="tableHeadingGray title">
			<td class="titleSource" style="line-height:26px;"><%=lb_source_file%></td>
			<td class="titleTarget" style="line-height:26px;"><%=lb_target_file%></td>
		</tr>
		<tr class="content">
			<td class="contentSource">
			   <a CLASS="standardHREF" HREF="#" >
					<img id="sourceImg" src="<%=sourceImagePath%>" alt="Source File"/>
				</a>
			</td>
			<td class="contentTarget">
				<a CLASS="standardHREF" HREF="<%=targetImagePath%>">
			  		<img id="targetImg" src="<%=targetImagePath%>" alt="Target File"/>
			   </a>
			</td>
		</tr>
		<tr CLASS="tableHeadingBasic foot">
			<td class="footSource" style="line-height:26px;"><%=lb_sourceLocale%>: <%=str_sourceLocale%></td>
			<td class="footTarget" style="line-height:26px;">
				<span style="float:left;">&nbsp;&nbsp;<%=lb_targetLocale%>: <%=str_targetLocaleBuffer.toString()%></span>
				<%
					if(targetLocalesList == null && displayUploadButton){
				%>
			    <span style="float:right;line-height:bottom"><A HREF="#" onclick="uploadTragetImage(); return false;" CLASS="HREFBoldWhite"><%=lb_upload%></A>&nbsp;&nbsp;&nbsp;</span>
				<%
				}
				%>
			</td>
		</tr>
	</table>
	<!-- Hidden DIV -->
	<div execute="" style="display:none;" id="uploadFormDiv" class="standardtext">
	  <FORM ACTION="<%=uploadUrl%>" NAME="uploadForm" METHOD="POST" ENCTYPE="multipart/form-data">
	    <table id = "uploadFormTable" style="width:97%;" class="standardText">
	    	<BR>
	    	<tr>
		    	<td style="width:70%;" align="left" valign="middle">
		    	   <span style="width:20%;"><%=bundle.getString("lb_file")%>:</span>
		          <input type="file" name="fileFieldName" size="60" id="fileUploadDialog" style="height:24px;width:80%;">
		    	</td>
		      <td style="width:30%;" align="right" valign="middle">
		          <input type="button" onclick="uploadFileMethod()" value="<%=bundle.getString("lb_upload")%>">
		          <input type="button" onclick="$('#uploadFormDiv').dialog('close')" value="<%=bundle.getString("lb_close")%>">
		      </td>
	      </tr>
	    </table>
      </FORM>
	</div>
</BODY>
</HTML>