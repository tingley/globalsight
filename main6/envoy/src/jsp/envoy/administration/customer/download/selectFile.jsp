<%@ page
    contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="java.util.*,
            org.apache.log4j.Logger,
            com.globalsight.cxe.entity.fileprofile.FileProfile,
            com.globalsight.everest.servlet.util.SessionManager,
            com.globalsight.everest.webapp.WebAppConstants,
            com.globalsight.everest.webapp.javabean.NavigationBean,
            com.globalsight.everest.webapp.pagehandler.PageHandler,
            com.globalsight.everest.webapp.pagehandler.administration.customer.download.DownloadFileHandler,
            com.globalsight.everest.servlet.EnvoyServletException,
            com.globalsight.util.SortUtil,
            com.globalsight.util.AmbFileStoragePathUtils,
            java.io.File,
            java.io.IOException,
            com.globalsight.ling.common.URLDecoder,
            com.globalsight.ling.common.URLEncoder,
            java.util.ArrayList,
            java.util.Collections,
            java.util.Enumeration,
            java.util.HashSet,
            java.util.List,
            java.util.ResourceBundle"
    session="true" %>
<jsp:useBean id="selectFile" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="downloadApplet" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="done" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<%
    ResourceBundle bundle = PageHandler.getBundle(session);
    SessionManager sessionMgr = (SessionManager)session.getAttribute(WebAppConstants.SESSION_MANAGER);
    List list = (List)sessionMgr.getAttribute(DownloadFileHandler.DOWNLOAD_JOB_LOCALES);
    String moduleLink="/globalsight/ControlServlet?activityName=";
    String title = bundle.getString("lb_select_files_to_download");
    String selectFileURL = selectFile.getPageURL();
    String downloadAppletURL = downloadApplet.getPageURL();
    String doneURL = done.getPageURL();
    String taskId = (String)request.getAttribute(WebAppConstants.TASK_ID);
    String taskState = (String)request.getAttribute(WebAppConstants.TASK_STATE);
    String jobId = (String)request.getAttribute(WebAppConstants.JOB_ID);
    String jobIds = ((String)request.getAttribute(DownloadFileHandler.PARAM_JOB_ID )).replaceAll(" ",",");
    String wfIds = (String)request.getAttribute(DownloadFileHandler.PARAM_WORKFLOW_ID);
    String companyFolderPath = (String)request.getAttribute(DownloadFileHandler.PARAM_COMPANY_FOLDER_PATH);
    if(wfIds != null)
    {
    	wfIds = wfIds.replaceAll(" ",",");
    }
%>
<HTML>
<!-- This JSP is: /envoy/administration/customer/download/selectFile.jsp -->
<HEAD>
<META HTTP-EQUIV="content-type" CONTENT="text/html;charset=UTF-8">
<TITLE><%= title %></TITLE>
<style type="text/css">
table td,table td * {
	vertical-align: top;
}

.btnCancel {
	width: 60px; font-size: smaller;
}
</style>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/utilityScripts.js"></SCRIPT>
<script type="text/javascript" src="/globalsight/jquery/jquery-1.6.4.min.js"></script>
<script type="text/javascript" src="/globalsight/jquery/jquery-ui-1.8.18.custom.min.js"></script>
<script type="text/javascript" src="/globalsight/jquery/dynatree-1.2.4/jquery.cookie.js"></script>
<script type="text/javascript" src="/globalsight/jquery/dynatree-1.2.4/jquery.dynatree.min.js"></script>
<link href="/globalsight/jquery/dynatree-1.2.4/skin-vista/ui.dynatree.css" rel="stylesheet" type="text/css">
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>
<%@ include file="/envoy/common/constants.jspIncl" %>
<SCRIPT LANGUAGE="JavaScript">
var needWarning = false;
var objectName = "";
var guideNode = "import";
var companyFolderPath = "<%=companyFolderPath%>";
var helpFile = "<%=bundle.getString("help_customer_download")%>";

function submitForm(action)
{
    var selNodes = $("#treeDIV").dynatree("getSelectedNodes");
   	if(selNodes.length == 0)
   	{
   		alert("Please select files.");
   		return;
   	}
   	var selectedFiles = "";
   	for(var i=0; i<selNodes.length; i++){
       	if(selNodes[i].data.isFolder != true)
       	{
           	var path = selNodes[i].data.key.replace(companyFolderPath + "/","");
           	path = encodeURIComponent(path.replace(/%C2%A0/g, "%20"));
    		selectedFiles += ("," + path);
       	}
   	}
   	selectedFiles = selectedFiles.substring(1,selectedFiles.length);
    $("#selectedFileList").val(selectedFiles);
 
    // Go to the Download Applet screen
	var isChecked = $("#includeLocaleCheckbox").is(":checked"); 
    downloadFilesForm.action = "<%=downloadAppletURL%>" + "&action=download&taskId="+<%=taskId%>+"&state="+<%=taskState%>+"&isChecked="+isChecked;
    downloadFilesForm.submit();
}

$(document).ready(function () {		
    // --- Initialize Files tree
    $("#treeDIV").dynatree({
        title: "Lazy loading sample",
        persist: false,
        checkbox: true,
        selectMode: 3,
        initAjax: {
            url: "/globalsight/ControlServlet?linkName=selectFile&pageName=CUST_FILE&action=getDownloadFileList&"+"<%=DownloadFileHandler.PARAM_JOB_ID %>"+"="+"<%=jobIds%>"
            	+"&"+"<%=DownloadFileHandler.PARAM_WORKFLOW_ID %>"+"="+"<%=wfIds%>"+"&"+"<%=DownloadFileHandler.TASK_ID %>"+"="+"<%=taskId%>"
        },
        onSelect: function(select, node) {
            // Get a list of all selected nodes, and convert to a key array:
            var selKeys = $.map(node.tree.getSelectedNodes(), function(node){
              return node.data.key;
            });

            // Get a list of all selected TOP nodes
            var selRootNodes = node.tree.getSelectedNodes(true);
            // ... and convert to a key array:
            var selRootKeys = $.map(selRootNodes, function(node){
              return node.data.key;
            });
        },
        onDblClick: function(node, event) {
            node.toggleSelect();
        },
        onKeydown: function(node, event) {
			if( event.which == 32 ) {
              node.toggleSelect();
              return false;
            }
        }
    });

    // Expand All Button
    $("#expandBtn").click(function () {
    	$("#treeDIV").dynatree("getRoot").visit(function (node) {
    	    node.expand(true);
    	});
    });
    
    // Collapse All Button
    $("#collapseBtn").click(function () {
    	$("#treeDIV").dynatree("getRoot").visit(function (node) {
    	    node.expand(false);
    	});
    });

});

function fnSelectAll(){
	var isChecked = $("#control").is(":checked");
	$("#treeDIV").dynatree("getRoot").visit(function(node){
		node.select(isChecked);
	});
}

function cancelButton(){
	if (location.search.indexOf("redirectToWorkflow") > -1)
	{
		var jobId = location.search.split("&")[4].split("=")[1];
		location.href = "/globalsight/ControlServlet?linkName=jobDetails&pageName=DTLS&jobId=" + jobId;
	}
	else
	{
		if(document.referrer.length >0)
			location.href = document.referrer;
		else
			history.go(-1);
	}
}
</script>
</HEAD>
<BODY LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0" MARGINWIDTH="0" MARGINHEIGHT="0"
    ONLOAD="loadGuides()">
<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>
<DIV ID="contentLayer" STYLE=" POSITION: ABSOLUTE; Z-INDEX: 9; TOP: 108px; LEFT: 20px; RIGHT: 20px;">

<SPAN CLASS="mainHeading"><%=title%></SPAN>
<P></P>

<TABLE CELLPADDING=0 CELLSPACING=0 BORDER=0 CLASS=standardText>
  <TR>
    <TD><%=bundle.getString("helper_text_customerdownload_choose_files")%></TD>
  </TR>
</TABLE>
<P></P>

<table CELLSPACING="0" CELLPADDING="0" style="border:0px solid black">
<tr VALIGN="middle">
	<td style="border:1px solid black;width:50px;height:20px;background-color:#738eb5;" align="center">
		<input type="checkbox" id="control" title="Select/Deselect All" onClick="fnSelectAll();">
	</td>
	<td style="width:1px"></td>
	<td style="border:1px solid black;background-color:#738eb5;">
		<input type="button" id="expandBtn" class="button_out" style="background-image:url('/globalsight/includes/dtree_checkbox/img/expand_all.gif')" title="Expand All">
	</td>
	<td style="width:1px"></td>
	<td style="border:1px solid black;background-color:#738eb5;">
		<input type="button" id="collapseBtn" class="button_out" style="background-image:url('/globalsight/includes/dtree_checkbox/img/collapse_all.gif')" title="Collapse All">
	</td>
	<td style="width:6px"></td>
		<td align="center">
		<input type="checkbox" id="includeLocaleCheckbox" >
	</td>
	<td CLASS=standardText><%=bundle.getString("lb_include_language_when_download")%>
	</td>
</tr>
<tr><td style="height:3px" colspan="7"></td></tr>
</table>
<div id="treeDIV" style="height:400px;width:750px">
<!-- When using initAjax, it may be nice to put a throbber here, that spins until the initial content is loaded: -->
</div>
<div>&nbsp;</div>
<DIV ALIGN="left" STYLE="POSITION: ABSOLUTE; WIDTH:200">
<FORM METHOD="post" NAME="downloadFilesForm">
<INPUT NAME="fileAction" VALUE="download" TYPE="HIDDEN">
<INPUT ID="selectedFileList" NAME="selectedFileList" VALUE="" TYPE="HIDDEN">
<INPUT TYPE=BUTTON VALUE="<%=bundle.getString("lb_return")%>" ONCLICK="cancelButton();">
<INPUT TYPE=BUTTON VALUE="<%=bundle.getString("lb_download_zip")%>" ONCLICK="submitForm('download');">
</FORM>
</DIV>
</BODY>
</HTML>
