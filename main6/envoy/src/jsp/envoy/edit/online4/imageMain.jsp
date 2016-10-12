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
            com.globalsight.everest.webapp.pagehandler.edit.online.EditorState,
            com.globalsight.everest.webapp.pagehandler.edit.online.EditorState.PagePair,
            com.globalsight.everest.util.comparator.GlobalSightLocaleComparator,
            com.globalsight.util.GlobalSightLocale,
            com.globalsight.util.SortUtil,
            java.util.Locale,
            java.util.ResourceBundle"
    session="true"
%>
<jsp:useBean id="self" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean"/>
 <jsp:useBean id="postReviewEditor" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean"/>
 <jsp:useBean id="popupEditor" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean"/>
 <%@ include file="/envoy/common/installedModules.jspIncl" %>
<%
ResourceBundle bundle = PageHandler.getBundle(session);
SessionManager sessionMgr = (SessionManager) session
		.getAttribute(WebAppConstants.SESSION_MANAGER);
EditorState state = (EditorState) sessionMgr.getAttribute(WebAppConstants.EDITORSTATE);
boolean isFistPage = state.isFirstPage();
boolean isLastPage = state.isLastPage();

String lb_title = "Image Editor";
String url_self = self.getPageURL();
String uploadUrl = url_self +
"&" + WebAppConstants.USER_ACTION +
"=" + WebAppConstants.UPLOAD_ACTION_START_UPLOAD;

String lb_close = bundle.getString("lb_close");
String lb_help = bundle.getString("lb_help");
String lb_upload = bundle.getString("lb_upload");
String lb_sourceLocale = bundle.getString("lb_source_locale");
String lb_targetLocale  = bundle.getString("lb_target_locale");
String lb_source_file = bundle.getString("lb_source_file");
String lb_target_file = bundle.getString("lb_target_file");
String lb_fileNavigation = bundle.getString("lb_fileNavigation");
String lb_upload_image_difExtension_message = bundle.getString("lb_upload_image_difExtension_message");
String lb_prevFile = "<IMG SRC='/globalsight/images/editorPreviousPagex.gif' BORDER=0 HSPACE=2 VSPACE=4>";
String lb_nextFile = "<IMG SRC='/globalsight/images/editorNextPagex.gif' BORDER=0 HSPACE=2 VSPACE=4>";

Locale uiLocale = (Locale)session.getAttribute(WebAppConstants.UILOCALE);
String jobId = (String)sessionMgr.getAttribute(WebAppConstants.JOB_ID);
String taskId = (String)sessionMgr.getAttribute(WebAppConstants.TASK_ID);
String srcPageId = (String)sessionMgr.getAttribute(WebAppConstants.SOURCE_PAGE_ID);
String trgPageId = (String)sessionMgr.getAttribute(WebAppConstants.TARGET_PAGE_ID);
uploadUrl += "&" + WebAppConstants.TASK_ID + "=" + taskId + "&"
		+ WebAppConstants.SOURCE_PAGE_ID + "=" + srcPageId + "&"
		+ WebAppConstants.TARGET_PAGE_ID + "=" + trgPageId;
String str_sourceLocale = (String) request.getAttribute("sourceLanguage");
String str_targetLocale = (String) request.getAttribute("targetLanguage");
String isFromActivity = (String) sessionMgr.getAttribute(WebAppConstants.IS_FROM_ACTIVITY);
boolean isActivity = false;
if(isFromActivity != null)
{
	if(isFromActivity.equals("yes"))
	{
		isActivity = true;
	}
}
if(isActivity)
{
	url_self += "&" + WebAppConstants.TASK_ID + "=" + taskId 
	+ "&" + WebAppConstants.SOURCE_PAGE_ID + "=" + srcPageId
	+ "&" + WebAppConstants.TARGET_PAGE_ID + "=" + trgPageId;
	
}
else
{
	url_self += "&" + WebAppConstants.JOB_ID+"="+jobId
			+ "&" + WebAppConstants.TARGET_PAGE_ID + "=" + trgPageId
			+ "&" + WebAppConstants.SOURCE_PAGE_ID + "=" + srcPageId;
}

String getDataUrl = url_self;

String isCanUpload = (String)request.getAttribute("isCanUpload");
boolean displayUploadButton = false;
if(isCanUpload != null && Boolean.parseBoolean(isCanUpload))
{
	displayUploadButton = true;
}

PagePair currentPage = state.getCurrentPage();
String openEditorType = state.getOpenEditorType();
boolean isPicturePreviousFile = currentPage.isPicturePreviousFile();
boolean isPictureNextFile = currentPage.isPictureNextFile();
String url_previousPostRwEditor =  postReviewEditor.getPageURL();
String url_nextPostRwEditor =  postReviewEditor.getPageURL();

String url_previousPopupEditor =  popupEditor.getPageURL();
String url_nextPopupEditor =  popupEditor.getPageURL();

int i_index = state.getPages().indexOf(currentPage);

if(!isPicturePreviousFile && i_index > 0)
{
	PagePair previousPage = state.getPages().get(i_index-1);
	StringBuffer parameter = new StringBuffer();
	if(isActivity)
	{
		parameter.append("&").append(WebAppConstants.TASK_ID).append("=").append(taskId);
		parameter.append("&").append(WebAppConstants.SOURCE_PAGE_ID).append("=").append(previousPage.getSourcePageId());
		parameter.append("&").append(WebAppConstants.TARGET_PAGE_ID).append("=").append(previousPage.getTargetPageId(state.getTargetLocale()));
	}
	else
	{
		parameter.append("&").append(WebAppConstants.JOB_ID).append("=").append(jobId);
		parameter.append("&").append(WebAppConstants.SOURCE_PAGE_ID).append("=").append(previousPage.getSourcePageId());
		parameter.append("&").append(WebAppConstants.TARGET_PAGE_ID).append("=").append(previousPage.getTargetPageId(state.getTargetLocale()));
	}
	parameter.append("&openEditorType=").append(state.getOpenEditorType());
	
	if(openEditorType!= null && openEditorType.equalsIgnoreCase("postReviewEditor"))
	{
		url_previousPostRwEditor += parameter.toString();
	}
	else if(openEditorType!= null && openEditorType.equalsIgnoreCase("popupEditor"))
	{
		url_previousPopupEditor += parameter.toString();
	}
}

if(!isPictureNextFile && i_index < state.getPages().size()-1)
{
	PagePair nextPage = state.getPages().get(i_index+1);
	StringBuffer parameter = new StringBuffer();
	if(isActivity)
	{
		parameter.append("&").append(WebAppConstants.TASK_ID).append("=").append(taskId);
		parameter.append("&").append(WebAppConstants.SOURCE_PAGE_ID).append("=").append(nextPage.getSourcePageId());
		parameter.append("&").append(WebAppConstants.TARGET_PAGE_ID).append("=").append(nextPage.getTargetPageId(state.getTargetLocale()));
	}
	else
	{
		parameter.append("&").append(WebAppConstants.JOB_ID).append("=").append(jobId);
		parameter.append("&").append(WebAppConstants.SOURCE_PAGE_ID).append("=").append(nextPage.getSourcePageId());
		parameter.append("&").append(WebAppConstants.TARGET_PAGE_ID).append("=").append(nextPage.getTargetPageId(state.getTargetLocale()));
	}
	parameter.append("&openEditorType=").append(state.getOpenEditorType());
	
	if(openEditorType!= null && openEditorType.equalsIgnoreCase("postReviewEditor"))
	{
		url_nextPostRwEditor += parameter.toString();
	}
	else if(openEditorType!= null && openEditorType.equalsIgnoreCase("popupEditor"))
	{
		url_nextPopupEditor += parameter.toString();
	}
}

Vector targetLocalesList = state.getJobTargetLocales();
StringBuffer str_targetLocaleBuffer = new StringBuffer();
if(!isActivity){
	if (targetLocalesList != null &&  targetLocalesList.size()>0)
	{
		SortUtil.sort(targetLocalesList, new GlobalSightLocaleComparator(Locale.getDefault()));
		str_targetLocaleBuffer.append("<select name='tarLocales' onchange='switchTargetLocale(this[this.selectedIndex].value)' style='font-size: 8pt;'>");
		GlobalSightLocale trg = null;
		for (int i = 0, max = targetLocalesList.size(); i < max; i++)
		{
		    trg = (GlobalSightLocale)targetLocalesList.get(i);
		
		    str_targetLocaleBuffer.append("<option ");
		
			 if (trg.equals(state.getTargetLocale()))
	         {
	            str_targetLocaleBuffer.append("selected ");
	         }
		    str_targetLocaleBuffer.append("value='").append(trg.toString()).append("'>");
		    str_targetLocaleBuffer.append(trg.getDisplayName(uiLocale));
		    str_targetLocaleBuffer.append("</option>");
		}
		str_targetLocaleBuffer.append("</select>");
	}
}
else
{
	str_targetLocaleBuffer.append(state.getTargetLocale().getDisplayName(uiLocale));
}
%>
<HTML>
<HEAD>
<TITLE><%=lb_title%></TITLE>
<STYLE>
.contentTable{width:100%;table-layout:fixed;}
.top {width:100%;height:40px;position:absolute;Z-INDEX: 10;LEFT:0px;top:0px;border-bottom:4px solid;border-bottom-style:ridge;border-bottom-color:#E8E8E8;}
.topLeft{width:50%;height:100%;position:absolute;left:0px;}
.topRight{width:50%;height:100%;position:absolute;right:0px;}

.title {width:100%;height:26px;position:absolute;Z-INDEX: 10;LEFT:0px;top:44px;}
.titleSource{width:50%;height:100%;position:absolute;left:0px;}
.titleTarget{width:50%;height:100%;position:absolute;right:0px;border-left:4px solid;border-left-style:ridge;border-left-color:#E8E8E8;}

.content{width:100%;position:absolute;Z-INDEX: 10;LEFT:0px;top:70px;bottom:27px;height:auto;}
.contentSource{width:50%;height:100%;position:absolute;left:0px;overflow:auto;}
.contentTarget{width:50%;height:100%;position:absolute;right:0px;overflow:auto;border-left:4px solid;border-left-style:ridge;border-left-color:#E8E8E8;}

.foot {width:100%;height:26px;position:absolute;Z-INDEX: 10; LEFT: 0px;bottom:0px;}
.footSource{width:50%;height:100%;position:absolute;left:0px;}
.footTarget{width:50%;height:100%;position:absolute;right:0px;border-left:4px solid;border-left-style:ridge;border-left-color:#E8E8E8;}

/* Background colors for issue status, class name is the status token. */
.open   { background-color: red !important; }
.closed { background-color: lawngreen !important; }
.ui-helper-clearfix{display:none;}
.hiddenDiv{	position:absolute;	top:0px;	left:0px;	height:100%;width:100%;opacity:0.8;background:#000000;z-index:900;}
</STYLE>
<link href="/globalsight/jquery/jQueryUI.redmond.css" rel="stylesheet" type="text/css"/>
<script type="text/javascript" src="/globalsight/jquery/jquery-1.6.4.min.js"></script>
<script type="text/javascript" src="/globalsight/jquery/jquery-ui-1.8.18.custom.min.js"></script>
<SCRIPT SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<script>
var url_self = "<%=url_self%>";
var getDataUrl = "<%=getDataUrl%>";
var helpFile = "<%=bundle.getString("help_main_image_editor")%>";
var reviewModeText = "<%=WebAppConstants.REVIEW_MODE%>";
var isFirstPage = '<%=isFistPage%>';
var isLastPage = '<%=isLastPage%>';
var isPictureNextFile = '<%=isPictureNextFile%>';
var isPicturePreviousFile = '<%=isPicturePreviousFile%>';
var openEditorType = '<%=openEditorType%>';
var url_previousPostRwEditor = '<%=url_previousPostRwEditor%>';
var url_previousPopupEditor = '<%=url_previousPopupEditor%>';

var url_nextPostRwEditor = '<%=url_nextPostRwEditor%>';
var url_nextPopupEditor = '<%=url_nextPopupEditor%>';

var targetImageSuffix;
var w_editor;

 function uploadTragetImage()
 {
	$("#uploadFormDiv").dialog({width:500, height:100,resizable:true});
 }
 
 function uploadFileMethod()
 {
	 var fileName = document.uploadForm.fileFieldName.value;
	 var fileExtension = fileName.substring(fileName.lastIndexOf(".")+1,fileName.length);

	 if(fileName.indexOf(".") != -1)
	 {
		 if(fileExtension != targetImageSuffix)
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
 	document.location = url_self+"&action=switchTargetLocale&trgViewLocale=" + p_locale;
 }
 
 function RaiseSegmentEditor()
 {
     return menu.RaiseEditor();
 }
</script>
<%@ include file="/envoy/common/shortcutIcon.jspIncl" %>
</HEAD>
<BODY id="idBody" onload="">
	<input type="hidden" name="<%=ReportConstants.JOB_IDS%>" value="<%=jobId%>">
	<table class="contentTable">
		<tr class="tableHeadingBasic top">
			<TD class="topLeft" NOWRAP VALIGN="TOP">
				&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<%=lb_fileNavigation%><BR>
		    	&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<label id="fileNavPre"><%=lb_prevFile%></label>
		    	<label id="fileNavNext"><%=lb_nextFile%></label>
		    </TD>
			<td class="topRight" style="line-height:26px;" align="RIGHT">
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
				<img id="sourceImg" src="" alt="Source File" onload="javascript:scaleImageSize(this)" onclick="javascript:openOriginalImage(this)"/>
			</td>
			<td class="contentTarget">
			    <img id="targetImg" src="" alt="Target File" onload="javascript:scaleImageSize(this)" onclick="javascript:openOriginalImage(this)"/>
			</td>
		</tr>
		<tr CLASS="tableHeadingBasic foot">
			<td class="footSource" style="line-height:26px;"><%=lb_sourceLocale%>: <%=str_sourceLocale%></td>
			<td class="footTarget" style="line-height:26px;">
				<span style="float:left;">&nbsp;&nbsp;<%=lb_targetLocale%>: <%=str_targetLocaleBuffer.toString()%></span>
				<%
					if(isActivity && displayUploadButton){
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
 
 	<div class="hiddenDiv" id="hiddenDiv" style="display:none;"></div>
	<div style="display:none;" class="hiddenImageDiv" id="hiddenImageDiv" style="position: absolute; top: 20px; left: 20px;">
		<img alt="Image" src="" id="hiddenImg" onclick="closeDiv();">
	</div>
</BODY>
<script type="text/javascript" src="/globalsight/envoy/edit/online4/main.js"></script>
</HTML>