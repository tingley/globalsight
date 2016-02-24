<%@ taglib uri="/WEB-INF/tlds/globalsight.tld" prefix="amb" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ page contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="com.globalsight.everest.jobhandler.Job,
            com.globalsight.everest.permission.Permission,
            com.globalsight.everest.permission.PermissionSet,
            com.globalsight.everest.webapp.WebAppConstants,
            com.globalsight.everest.util.system.SystemConfigParamNames,
            com.globalsight.everest.webapp.pagehandler.projects.workflows.JobManagementHandler,
            com.globalsight.everest.webapp.pagehandler.projects.workflows.AddSourceHandler,
            com.globalsight.everest.webapp.pagehandler.administration.customer.download.DownloadFileHandler,
            com.globalsight.everest.company.Company,
            com.globalsight.everest.projecthandler.Project,
            java.text.MessageFormat,
            java.util.*"
    session="true"
%>
<jsp:useBean id="self" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="pending" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="jobDetails" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
 <jsp:useBean id="jobScorecard" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="jobComments" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="jobAttributes" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="jobReports" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="modify" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="workflowActivities" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="exportError" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="workflowImportError" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="export" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="editPages" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="addWF" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="rateVendor" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="wordcountList" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="download" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
 <jsp:useBean id="assign" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
 <jsp:useBean id="skip" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
 <jsp:useBean id="estimatedCompletionDate" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
 <jsp:useBean id="estimatedTranslateCompletionDate" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
  <jsp:useBean id="addSourceFiles" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="updateLeverage" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="jobSourceFiles" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="jobCosts" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="jobDetailsPDFs" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="translatedTextList" scope="request" 
 class="com.globalsight.everest.webapp.javabean.NavigationBean"/>
<% 
//jobSummary child page needed started.
   ResourceBundle bundle = PageHandler.getBundle(session);
   String jobCommentsURL = jobComments.getPageURL() + "&jobId=" + request.getAttribute("jobId");
   Company company = (Company)request.getAttribute("company");
   boolean enableQAChecks = company.getEnableQAChecks();
   Project project = (Project)request.getAttribute("project");
   boolean allowManualRunningQAChecks = project.getAllowManualQAChecks();
   boolean showButton = true;
   if(company.getId() == 1)
   {
   	showButton = false;
   }
//jobSummary child page needed end.
%>
<html>
<head>
<title><%=bundle.getString("lb_workflows")%></title>
<script src="/globalsight/jquery/jquery-1.6.4.min.js"></script>
<script SRC="/globalsight/includes/setStyleSheet.js"></script>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>
<title><%=bundle.getString("lb_workflows")%></title>
<style>
.myTableHeading{
	padding:12px 2px 2px 2px;
}

#fullbg {
    background-color: Gray;
    display:none;
    z-index:1;
    position:absolute;
    left:0px;
    top:0px;
    filter:Alpha(Opacity=30);
    /* IE */
    -moz-opacity:0.4;
    /* Moz + FF */
    opacity: 0.4;
}

#container {
    position:absolute;
    display: none;
    z-index: 2;
}
</style>
</head>
<body leftmargin="0" rightmargin="0" topmargin="0" marginwidth="0" marginheight="0" onload="loadGuides()"; onunload="closeOpenedWindow();"; id="idBody"  class="tundra">
<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>
<div id="contentLayer"  class="standardText" style="position: absolute; z-index: 9; top: 108px; left: 20px; right: 20px;">
<c:if test="${isUpdatingWordCounts}">
<!-- for UpdateWordCountsProgressBar -->
	<div id="fullbg"></div>
	<div id="container">
		<div id="updateWordCountsProgressBar"></div>
	</div>
</c:if>
<div id="includeSummaryTabs">
	<%@ include file="/envoy/projects/workflows/includeJobSummaryTabs.jspIncl" %>
</div>
<amb:permission name="<%=Permission.JOB_WORKFLOWS_VIEW%>" >
<div id="workflowBlock" name="workflowBlock" style="clear:both;margin:0;padding:0">
<FORM METHOD="post" NAME="downloadFilesForm" style="display:none">
<INPUT NAME="fileAction" VALUE="download" TYPE="HIDDEN">
<INPUT ID="selectedFileList" NAME="selectedFileList" VALUE="" TYPE="HIDDEN">
</FORM>
<form name="workflowForm" id="workflowForm" method="post">
	<input type="hidden" id="downloadWorkflowIds" name="<%=DownloadFileHandler.PARAM_WORKFLOW_ID%>" value=""/>
	<input type="hidden" id="downloadJobId" name="<%=DownloadFileHandler.PARAM_JOB_ID%>" value=""/>
</form>
	<div id="workflowBlockTitle" class="standardTextBold" style="margin:0;padding:0"><%=bundle.getString("lb_workflows")%></div>
	<table cellpadding="2" cellspacing="0" border="0" style="min-width:1024px;width:85%;border:solid 1px slategray">
		<thead>
			<tr>
			    <td class="tableHeadingBasic myTableHeading"><input id="selectAllWorkflows" type="checkbox" onclick="selectAllWorkflows()"></td>
			    <td class="tableHeadingBasic myTableHeading"><span class="whiteBold"><%=bundle.getString("lb_target_locale")%>&nbsp;&nbsp;&nbsp;&nbsp;</span></td>
			    <td class="tableHeadingBasic myTableHeading" style="text-align:center"><%=bundle.getString("lb_word_count")%>&nbsp;&nbsp;&nbsp;</td>
			    <td class="tableHeadingBasic myTableHeading" style="text-align:center"><span class="whiteBold">&nbsp;&nbsp;&nbsp;<%=bundle.getString("lb_percent_complete")%>&nbsp;&nbsp;&nbsp;</span></td>
			    <td class="tableHeadingBasic myTableHeading"><span class="whiteBold"><%=bundle.getString("lb_state")%>&nbsp;&nbsp;&nbsp;</span></td>
			    <td class="tableHeadingBasic myTableHeading"><span class="whiteBold"><%=bundle.getString("lb_current_activity")%>&nbsp;&nbsp;&nbsp;</span></td>
			    <c:if test="${customerAccessGroupIsDell}">
					<amb:permission  name="<%=Permission.JOB_WORKFLOWS_ESTREVIEWSTART%>" >
						<td class="tableHeadingBasic myTableHeading"><span CLASS="whiteBold"><%=bundle.getString("lb_estimated_review_start")%>&nbsp;&nbsp;&nbsp;</span></td>
					</amb:permission>
			    </c:if>
			    <td class="tableHeadingBasic myTableHeading"><span class="whiteBold"><%=bundle.getString("lb_estimated_translate_completion_date")%>&nbsp;&nbsp;&nbsp;</span></td>
			    <td class="tableHeadingBasic myTableHeading"><span class="whiteBold"><%=bundle.getString("lb_estimated_workflow_completion_date")%>&nbsp;&nbsp;&nbsp;</span></td>
			    <td class="tableHeadingBasic myTableHeading"><span class="whiteBold">Uploading&nbsp;&nbsp;&nbsp;</span></td>
			    <td class="tableHeadingBasic myTableHeading"><span class="whiteBold"><%=bundle.getString("lb_priority")%>&nbsp;&nbsp;&nbsp;</span></td>
			    <td class="tableHeadingBasic myTableHeading"><span class="whiteBold"><input type="button" id="translatedTextBtn" value='<%=bundle.getString("lb_translated_text")%>'></span>&nbsp;&nbsp;&nbsp;</td>
			</tr>
		</thead>
		<tbody id="jobWorkflowDisplayList">
		<c:forEach items="${JobWorkflowDisplayList}" var="item">
			 <c:choose>
				<c:when test="${(item.workflow.state == 'EXPORT_FAILED') || (item.workflow.state == 'IMPORT_FAILED')}">
					<tr class="warningText">
				</c:when>
				<c:otherwise>
					<tr class="standardText">
				</c:otherwise>
			</c:choose>
				<td>
					<input name="wfId" class="workflowCheckbox" type="checkbox" onclick="selectWorkflow()" value="${item.workflow.id}"/>
					<input type="hidden" id="wfState_${item.workflow.id}" value="${item.workflow.state}"/>
					<input type="hidden" id="wfIsEditable_${item.workflow.id}" value="${item.isWorkflowEditable}"/>
				</td>
				<td>${item.targetLocaleDisplayName}</td>
				<td style="text-align:center">
					<amb:permission  name="<%=Permission.JOB_WORKFLOWS_WORDCOUNT%>" >
						<a class="standardHREF" href="${wordcountList.pageURL}&wfId=${item.workflow.id}&action=one">
					</amb:permission>
						${item.totalWordCount}
					<amb:permission  name="<%=Permission.JOB_WORKFLOWS_WORDCOUNT%>" >
						</a>
					</amb:permission>
				</td>
				<td style="text-align:center">${item.workflow.percentageCompletion}%</td>
				<td>${item.stateBundleString}</td>
				<td>${item.taskDisplayName}</td>
				<c:if test="${customerAccessGroupIsDell}">
					<amb:permission  name="<%=Permission.JOB_WORKFLOWS_ESTREVIEWSTART%>">
						<td>${item.estimatedStartTimestamp}</td>
					</amb:permission>
			    </c:if>
		 		<td>
			    	<c:if test="${item.workflow.state == 'READY_TO_BE_DISPATCHED' || item.workflow.state == 'DISPATCHED'}">
			    		<amb:permission name="<%=Permission.JOBS_ESTIMATEDTRANSLATECOMPDATE%>">
			    			<a class="standardHREF" style="word-wrap:break-word;word-break:break-all;" href="${estimatedTranslateCompletionDate.pageURL}&jobId=${jobId}">
			    		</amb:permission>
			    	</c:if>
			    	${item.estimatedTranslateCompletionDateTimestamp}
			    	<c:if test="${item.workflow.state == 'READY_TO_BE_DISPATCHED' || item.workflow.state == 'DISPATCHED'}">
			    		<amb:permission name="<%=Permission.JOBS_ESTIMATEDTRANSLATECOMPDATE%>">
			    			</a>
			    		</amb:permission>
			    	</c:if>
			    </td>
			    <td>
			    	<c:if test="${item.workflow.state == 'READY_TO_BE_DISPATCHED' || item.workflow.state == 'DISPATCHED'}">
			    		<amb:permission name="<%=Permission.JOBS_ESTIMATEDCOMPDATE%>">
			    			<a class="standardHREF" style="word-wrap:break-word;word-break:break-all;" href="${estimatedCompletionDate.pageURL}&jobId=${jobId}">
			    		</amb:permission>
			    	</c:if>
			    	${item.estimatedCompletionDateTimestamp}
			    	<c:if test="${item.workflow.state == 'READY_TO_BE_DISPATCHED' || item.workflow.state == 'DISPATCHED'}">
			    		<amb:permission name="<%=Permission.JOBS_ESTIMATEDCOMPDATE%>">
			    			</a>
			    		</amb:permission>
			    	</c:if>
			    </td>
			    <td>
			    	<c:if test="${item.isUploading == 'Yes'}">
			    		<span style="color:red">Yes</span>
			    	</c:if>
			    	<c:if test="${item.isUploading == 'No'}">
			    		No
			    	</c:if>
			    </td>
			    <td style="text-align:left">
				    <select class="workflowPrioritySelect" onchange="changeWorkflowPriority('${item.workflow.id}',this.value)"  style="display:none">
						<c:forEach var="wfPriority" begin="1" end="5" step="1">
							<option value="${wfPriority}" 
								<c:if test="${item.workflow.priority==wfPriority}">selected="selected"</c:if>
							>${wfPriority}
							</option>
						</c:forEach>					
					</select>
					<span class="workflowPriority">${item.workflow.priority}</span>
			    </td>
			    <td style="text-align:center">
			    	<span class="standardText" id="oPara${item.workflow.id}" style = "font-weight:600"></span>
			    </td>
			</tr>
		</c:forEach>
		</tbody>
	</table>
	<div id="workflowButton" style="padding-top:5px;min-width:1024px;width:85%">
	<c:if test="${!isSuperAdmin}">
			<c:if test="${isCustomerAccessGroupInstalled}">
				<amb:permission  name="<%=Permission.JOB_WORKFLOWS_REASSIGN%>" >
		    		<input id="ReAssign" class="standardText radioButton" type="button" name="ReAssign" value="<%=bundle.getString("lb_reassign")%>" onclick="submitForm('ReAssign');"/>
				</amb:permission>				
			 </c:if>
           <amb:permission  name="<%=Permission.JOB_WORKFLOWS_DISCARD%>" >
                <input id="Discard" class="standardText" type="button" name="Discard" value="<%=bundle.getString("lb_discard")%>" onclick="submitForm('Discard');"/>
           </amb:permission>
           <amb:permission  name="<%=Permission.JOB_WORKFLOWS_VIEW_ERROR%>" >
                <input id="ViewError" class="standardText radioButton" type="button" name="ViewError" value="<%=bundle.getString("action_view_error")%>..." onclick="submitForm('ViewError');"/>
           </amb:permission>
           <amb:permission  name="<%=Permission.JOB_UPDATE_LEVERAGE%>" >
                <input id="idUpdateLeverageBtn" class="standardText" type="button" name="UpdateLeverage" value="<%=bundle.getString("lb_update_leverage_title")%>..."  onclick="submitForm('UpdateLeverage');"/>
           </amb:permission>
           <amb:permission  name="<%=Permission.JOB_UPDATE_WORD_COUNTS%>" >
               <input id="UpdateWordCounts" class="standardText" type="button" name="UpdateWordCounts" value="<%=bundle.getString("lb_update_word_counts")%>" onclick="submitForm('UpdateWordCounts');"/>
           </amb:permission>
           <amb:permission  name="<%=Permission.JOB_WORKFLOWS_WORDCOUNT%>" >
               <input id="WordCount" class="standardText" type="button" name="WordCount" value="<%=bundle.getString("lb_detailed_word_counts")%>..." onclick="submitForm('WordCount');"/>
           </amb:permission>
            <amb:permission  name="<%=Permission.JOB_WORKFLOWS_TRANSLATED_TEXT%>" >
               <input id="Translated Text" class="standardText" type="button" name="Translated Text" value="<%=bundle.getString("lb_translated_text")%>..." onclick="submitForm('TranslatedText');"/>
           </amb:permission>
		<c:if test="${isVendorManagementInstalled}">
			<amb:permission  name="<%=Permission.JOB_WORKFLOWS_RATEVENDOR%>" >
				<input id="Rate" class="standardText" type="button" name="Rate" value="<%=bundle.getString("lb_rate_vendor")%>" onclick="submitForm('Rate');"/>
			</amb:permission>
		</c:if>
           <amb:permission  name="<%=Permission.JOB_WORKFLOWS_ARCHIVE%>" >
                <input id="Archive" class="standardText" type="button" name="Archive" value="<%=bundle.getString("lb_archive")%>" onclick="submitForm('Archive');"/>
           </amb:permission>
           <amb:permission  name="<%=Permission.JOB_WORKFLOWS_DETAILS%>" >
                <input id="Details" class="standardText radioButton" type="button" name="Details" value="<%=bundle.getString("lb_details")%>" onclick="submitForm('Details');"/>
           </amb:permission>
           <amb:permission  name="<%=Permission.JOB_WORKFLOWS_EXPORT%>" >
                <input id="Export" class="standardText" type="button" name="Export" value="<%=bundle.getString("lb_export")%>..." onclick="submitForm('Export');"/>
           </amb:permission>
           
           <c:if test="${reimportOption == 0 || reimportOption == 1}">
            <amb:permission  name="<%=Permission.JOB_WORKFLOWS_ADD%>" >
                 <input id="Add" class="standardText" type="button" name="Add" value="<%=bundle.getString("lb_add")%>..." onclick="submitForm('AddWF');"/>
            </amb:permission>
           </c:if>
           <amb:permission  name="<%=Permission.JOB_WORKFLOWS_EDIT%>" >
                <input id="Edit" class="standardText radioButton" type="button" name="Edit" value="<%=bundle.getString("lb_edit")%>..." onclick="submitForm('Edit');"/>
           </amb:permission>
           <amb:permission  name="<%=Permission.JOB_WORKFLOWS_DISPATCH%>" >
                <input id="Dispatch" class="standardText" type="button" name="Dispatch" value="<%=bundle.getString("lb_dispatch")%>" onclick="submitForm('Dispatch');"/>
           </amb:permission>
           <amb:permission name="<%=Permission.JOBS_DOWNLOAD%>" >
               <input id="Download" class="standardText" type="button" name="Download" value="<%=bundle.getString("lb_download")%>..." onClick="submitForm('Download');"/>
           </amb:permission>
           <amb:permission name="<%=Permission.JOB_WORKFLOWS_EXPORT_DOWNLOAD%>" >
               <input id="ExportDownload" class="standardText" type="button" name="ExportDownload" value="<%=bundle.getString("lb_export_download")%>..." onClick="startExport()"/>
           </amb:permission>
           <amb:permission  name="<%=Permission.JOB_WORKFLOWS_SKIP%>" >
               <input id="skip" class="standardText" type="button" name="skip" value="<%=bundle.getString("lb_skip_activity")%>" onClick="submitForm('skip');"/>
           </amb:permission>
           <c:if test="${sending_back_edition}">
           	   <input class="standardText" type="button" name="ReSendingBack" value="<%=bundle.getString("lb_resendingback_edition_job")%>" onclick="submitForm('sendingbackEditionJob');"/>
           </c:if>
            <%if(showButton && enableQAChecks && allowManualRunningQAChecks){ %>
  			<INPUT TYPE="BUTTON" NAME=downloadQAReport VALUE="<%=bundle.getString("lb_download_qa_reports")%>" onClick="submitForm('downloadQAReport')">
    		<% } %>
	</div>
	</c:if>
</div>
</amb:permission>
<span id="exportdownload_progress_content">
    <div id="idExportDownloadProgressDivDownload"
         style='border-style: solid; border-width: 1pt; border-color: #0c1476; background-color: white; display:none; left: 300px; height: 370; width: 500px; position: absolute; top: 150px; z-index: 21'>
        <%@ include file="/envoy/tasks/exportDownloadProgressIncl.jsp" %>
    </div>
</span>
</div>

<script src="/globalsight/jquery/jquery.progressbar.js"></script>
<script src="/globalsight/envoy/projects/workflows/jobDetails.js"></script>
<script type="text/javascript">
var needWarning = false;
var objectName = "";
var guideNode = "myJobs";
var helpFile = "<%=bundle.getString("help_job_workflows")%>";
var w_updateLeverage = null;
var downloadCheck;
var startExportDate;
var exportEnd = false;
var exportDownloadRandom;
var exportFrom = "jobWorkflow";
var exportPercent = 0;

$("#translatedTextBtn").bind("click",function(){
	var temp=document.getElementsByName('wfId');
	var workflowIds="";
    for(var i=0;i<temp.length;i++){
        workflowIds +=temp[i].value+",";
    }
    workflowIds = workflowIds.substring(0,workflowIds.length-1);
    var random = Math.random();
    workflowIds = workflowIds.split(",");
    if(workflowIds == "") return;
    var j = 0;
    var count = setInterval(function translatedTextc(){
    	$.getJSON("${self.pageURL}",{
    		action:"retrieveTranslatedText",
            workflowId:workflowIds[j],
            random:random	
    	},function(data){
    		var workflowId = data.workflowId;
    		var percent = data.percent;
    		var objName = "oPara" + workflowId;
            var obj = document.getElementById(objName);
    		if(percent<100){
    			obj.style.color = "red";
            	obj.innerHTML = "(" + percent + "%)";
    		}
    		else{
    			obj.style.color = "black";
            	obj.innerHTML = "(" + percent + "%)";
    		}
    	});
    	 j++;
    	 if(j>=workflowIds.length)
     		clearInterval(count);
    },200);
}); 

function startExport()
{
	var selectedCheckbox = $(":checkbox:checked:not(#selectAllWorkflows)");
	var selectedCheckboxCounts = selectedCheckbox.length;
	var wfState = "";
	if(selectedCheckboxCounts == 1) {
		wfState = $(":checkbox:checked:not(#selectAllWorkflows) + :hidden").val();
	} else if (selectedCheckboxCounts > 1) {
		for(i = 0;i < selectedCheckboxCounts;i++){
			var aWfId = selectedCheckbox[i].value;
			wfState += $("#wfState_" + aWfId).val();
			wfState += " ";
		}
		wfState = wfState.trim();
	}
	if (wfState.indexOf("BATCH_RESERVED") != -1 ||
	          wfState.indexOf("PENDING") != -1 ||
	          wfState.indexOf("READY_TO_BE_DISPATCHED") != -1 ||
	          wfState.indexOf("IMPORT_FAILED") != -1)
    {
       // You can only archive workflows that are...EXPORTED
       alert("<%=bundle.getString("jsmsg_cannot_export_workflow")%>");
       return false;
    }
    if (wfState.indexOf("EXPORTING") != -1)
    {
       alert("<%=bundle.getString("jsmsg_cannot_operate_workflow_exporting")%>");
       return false;
    }

	
	var workflowIds = getWorkflowIds();
    var random = Math.random();
    exportDownloadRandom = Math.random();
    $.getJSON("/globalsight/TaskListServlet", {
        action:"checkUploadingStatus",
        state:8,
        jobId:${jobId},
        workflowId:workflowIds,
        exportFrom:exportFrom,
        random:random
    }, function(data) {
    	if(data.isUploading)
    	{
    		alert("The activities of the job are uploading. Please wait.");
    	}
    	else
    	{
    		$.getJSON("/globalsight/TaskListServlet", {
                action:"export",
                state:8,
                jobId:${jobId},
                workflowId:workflowIds,
                exportFrom:exportFrom,
                random:random
            }, function(data) {
            	startExportDate = data.startExportDate;
            	exportEnd = false;
            	exportPercent = 0;
            	if(downloadCheck != null)
            	{
            		clearInterval(downloadCheck);
            		downloadCheck = null;
            	}
            	showExportDownloadProgressDiv();
            });
    	}
    });
}

function doExportDownload()
{
	var workflowIds = getWorkflowIds();
    var random = Math.random();
    var exportDownloadMessage = "";
	$.getJSON("/globalsight/TaskListServlet", {
        action:"download",
        state:8,
        jobId:${jobId},
        workflowId:workflowIds,
        startExportDate:startExportDate,
        exportDownloadRandom:exportDownloadRandom,
        exportFrom:exportFrom,
        random:random
    }, function(data) {
    	if(!exportEnd)
	    {
	    	if(data.selectFiles != "")
	    	{
	    		exportEnd = true;
	    		clearInterval(downloadCheck);
	    		downloadCheck = null;
	    		var selectedFiles = "";
	    		$.each(data.selectFiles, function(i, item) {
	    			item = encodeURIComponent(item.replace(/%C2%A0/g, "%20"));
	    			selectedFiles += ("," + item);
	    		});
	    		selectedFiles = selectedFiles.substring(1,selectedFiles.length);
	    		$("#selectedFileList").val(selectedFiles);
	    		downloadFilesForm.action = "/globalsight/ControlServlet?linkName=downloadApplet&pageName=CUST_FILE_Download&action=download&taskId="+null+"&state=8&isChecked="+false;
	    		downloadFilesForm.submit();
	    	}
	    	if(data.percent == 100 && data.selectFiles != "")
    		{
    			exportDownloadMessage = "Finish export. Start download."
    			showExportDownloadProgress("", data.percent, exportDownloadMessage);
    			exportPercent = 0;
    		}
    		if(exportPercent < data.percent && data.percent < 100 )
    		{
    			exportPercent = data.percent;
    			showExportDownloadProgress("", data.percent, exportDownloadMessage);
    		}
		}
    });
}

function showExportDownloadProgressDiv()
{
	if(downloadCheck == null)
	{
	    idExportDownloadMessagesDownload.innerHTML = "";
	    document.getElementById("idExportDownloadProgressDownload").innerHTML = "0%"
	    document.getElementById("idExportDownloadProgressBarDownload").style.width = 0;
	    document.getElementById("idExportDownloadProgressDivDownload").style.display = "";
	    showExportDownloadProgress("", 0 , "Start Export...");
	    downloadCheck = window.setInterval("doExportDownload()", 2000);
	}
}


function getWorkflowIds()
{
	var selectedCheckbox = $(":checkbox:checked:not(#selectAllWorkflows)");
	var selectedCheckboxCounts = selectedCheckbox.length;
	var wfId = "";
	if(selectedCheckboxCounts == 1) {
		wfId = $(":checkbox:checked:not(#selectAllWorkflows)").val();
	} else if (selectedCheckboxCounts > 1) {
		for(i = 0;i < selectedCheckboxCounts;i++){
			var aWfId = selectedCheckbox[i].value;
			wfId += aWfId;
			wfId += " ";
		}
		wfId = wfId.trim();
	}
	return wfId;
}

//jobSummary child page needed started
$(document).ready(function(){
	$("#jobWorkflowsTab").removeClass("tableHeadingListOff");
	$("#jobWorkflowsTab").addClass("tableHeadingListOn");
	$("#jobWorkflowsTab img:first").attr("src","/globalsight/images/tab_left_blue.gif");
	$("#jobWorkflowsTab img:last").attr("src","/globalsight/images/tab_right_blue.gif");
})

//jobSummary child page needed end.

$(document).ready(function(){
	$("#jobWorkflowDisplayList tr:odd").css("background-color","#EEEEEE");
	selectWorkflow();
	<amb:permission name="<%=Permission.JOB_WORKFLOWS_PRIORITY%>">
		$(".workflowPriority").css("display","none");
		$(".workflowPrioritySelect").css("display","inline");
	</amb:permission>
	<c:if test="${isUpdatingWordCounts}">
		var scrollHeight = document.body.scrollHeight-120;
	    var scrollWidth = document.body.scrollWidth-40;
	    $("#fullbg").css({width:scrollWidth, height:scrollHeight, display:"block"});
	    $("#container").css({top:"400px",left:"600px",display:"block"});
		$("#updateWordCountsProgressBar").progressBar();
		setTimeout(updateWordCountsProgress, 500);
	</c:if>
})

$(document).ready(function(){
	if(!$.browser.msie){
		$("#workflowBlockTitle").css("margin-top","20px");
	}
})

<c:if test="${isUpdatingWordCounts}">
function updateWordCountsProgress()
{
	var getPercentageURL = "${self.pageURL}&action=getUpdateWCPercentage&t=" + new Date().getTime();

	$.getJSON(getPercentageURL, function(data) {
		var per = data.updateWCPercentage;
		$("#updateWordCountsProgressBar").progressBar(per);
		if (per < 100) {
			setTimeout(updateWordCountsProgress,1000);
		} else {
			setTimeout(closeBg,1000);
		}
	});
}

function closeBg() {
	$("#fullbg").css("display","none");
	$("#container").css("display","none");
}
</c:if>

function selectAllWorkflows(){
	  var selectAll = $("#selectAllWorkflows").is(":checked");
	  if (selectAll) {
		  $(".workflowCheckbox").attr("checked","true");
	  	  var selectedCheckboxCounts = $(":checkbox:checked:not(#selectAllWorkflows)").length;
		  if(selectedCheckboxCounts == 1){
				$("#workflowButton :button:not(#Previous)").removeAttr("disabled");
		  }else if(selectedCheckboxCounts > 1){
			  $("#workflowButton :button:not(#Previous)").removeAttr("disabled");
			  $(".radioButton").attr("disabled","disabled");
		  }
	  }else{
		  $(".workflowCheckbox").removeAttr("checked");
		  $("#workflowButton :button:not(#Add):not(#Previous)").attr("disabled","disabled");
	  }
}

function selectWorkflow(){
	var selectedCheckboxCounts = $(":checkbox:checked:not(#selectAllWorkflows)").length;
	if(selectedCheckboxCounts == 0){
		$("#workflowButton :button:not(#Add):not(#Previous)").attr("disabled","disabled");
	}else if(selectedCheckboxCounts == 1){
		$("#workflowButton :button:not(#Previous)").removeAttr("disabled");
	}else if(selectedCheckboxCounts > 1){
		$("#workflowButton :button:not(#Previous)").removeAttr("disabled");
		$(".radioButton").attr("disabled","disabled");
	}
}

function submitForm(specificButton){
	var url = "${addSourceFiles.pageURL}&action=canUpdateWorkFlow&jobId=${jobId}&t=" + new Date().getTime();
	$.get(url,function(data){
		if(data==""){
			realSubmitForm(specificButton);
		}else{
			alert(data);
		}
	});
}

function realSubmitForm(specificButton){
	if($.browser.msie){
		String.prototype.trim = function () {
			return this .replace(/^\s\s*/, '' ).replace(/\s\s*$/, '' );
		}
	}
	var selectedCheckbox = $(":checkbox:checked:not(#selectAllWorkflows)");
	var selectedCheckboxCounts = selectedCheckbox.length;
	var wfId = "";
	var wfState = "";
	var wfIsEditable = "";
	if(selectedCheckboxCounts == 1) {
		wfId = $(":checkbox:checked:not(#selectAllWorkflows)").val();
		wfState = $(":checkbox:checked:not(#selectAllWorkflows) + :hidden").val();
		wfIsEditable = $(":checkbox:checked:not(#selectAllWorkflows) + :hidden + :hidden").val();
	} else if (selectedCheckboxCounts > 1) {
		for(i = 0;i < selectedCheckboxCounts;i++){
			var aWfId = selectedCheckbox[i].value;
			wfId += aWfId;
			wfId += " ";
			wfState += $("#wfState_" + aWfId).val();
			wfState += " ";
			wfIsEditable += $("#wfIsEditable_" + aWfId).val();
			wfIsEditable += " ";
		}
		wfId = wfId.trim();
		wfState = wfState.trim();
		wfIsEditable = wfIsEditable.trim();
	}
	//choose 0 or more checkbox, Add button is available.
	if (specificButton == "AddWF")
	{
	    <c:if test="${jobHasPassoloFiles}">
		    if(true){
		        alert("<%=bundle.getString("jsmsg_cannot_add_passolo_workflow")%>");
		        return;
		    }
	    </c:if>
    	<c:if test="${Job.state == 'PENDING' || Job.state == 'IMPORT_FAILED' || Job.state == 'ARCHIVED'}">
	    	if(true){
	            // a pending or archived workflow cannot be modified
	            alert("<%=bundle.getString("jsmsg_cannot_add_pending_workflow")%>");
	            return;
	    	}
    	</c:if>
    	// "Re-Export" can change an "ARCHIVED" job back to "EXPORTED".
        <c:if test="${Job.state == 'EXPORTED' && isJobMigrated == 'true'}">
        if(true){
            // a pending workflow cannot be modified
            alert("You cannot add a workflow to a job that has been EVER archived.");
            return;
        }
    </c:if>
    	var url = "${addWF.pageURL}&jobId=${jobId}";
		$("#workflowForm").attr("action",url);
		$("#workflowForm").submit();	        
    }
	else if (specificButton == "Pending"){
		var url = "${pending.pageURL}&jobId=${jobId}";
		$("#workflowForm").attr("action",url);
		$("#workflowForm").submit();	
	}
	//only when choosing 1 checkbox,below buttons are available.Strart.
	//"ViewError" || "Rate" || "Details" || "Edit" || "Download".
	else if(specificButton == "ReAssign")
	{
		if(wfState.indexOf("READY_TO_BE_DISPATCHED") == -1 && wfState.indexOf("DISPATCHED") == -1){
	         alert("<%=bundle.getString("jsmsg_cannot_reassign_workflow")%>");         
	         return false;
		}else{
			var url = "${assign.pageURL}&wfId=" + wfId + "&jobId=${jobId}";
			$("#workflowForm").attr("action",url);
			$("#workflowForm").submit();
		}
	}
	else if(specificButton == "ViewError")
	{
		var exportErrorWorkflowSelected;
		var importErrorWorkflowSelected;
           if (wfState.indexOf("EXPORT_FAILED") != -1)
           {
               exportErrorWorkflowSelected = true;
           }
           else if (wfState.indexOf("IMPORT_FAILED") != -1)
           {
              importErrorWorkflowSelected = true;
           }
           if (!exportErrorWorkflowSelected && !importErrorWorkflowSelected)
           {
              alert('<%=bundle.getString("jsmsg_workflow_has_no_errors")%>');
				return false;
		} else if (exportErrorWorkflowSelected) {
			var url = "${exportError.pageURL}&errorWF=" + wfId + "&jobId=${jobId}";
			$("#workflowForm").attr("action", url);
			$("#workflowForm").submit();
		} else // import error selected
		{
			var url = "${workflowImportError.pageURL}&errorWF=" + wfId + "&jobId=${jobId}";
			$("#workflowForm").attr("action", url);
			$("#workflowForm").submit();
		}
	}
	else if (specificButton == "Rate") 
	{
		var url = "${rateVendor.pageURL}&action=rate&wfId=" + wfId;
		openRateVendorWindow(url);
		// Don't submit form since we'll display the response
		// in a pop-up window
		return false;
	}
	else if (specificButton == "Details") 
	{
		var url = "${workflowActivities.pageURL}&wfId=" + wfId + "&jobId=${jobId}";
		openActivitiesWindow(url);
		// Don't submit form since we'll display the response
		// in a pop-up window
		return false;
	}
	else if (specificButton == "Edit")
	{
	    if (wfIsEditable == "false")
	    {
	        if (wfState.indexOf("PENDING") != -1 || wfState.indexOf("IMPORT_FAILED") != -1)
	        {
	            // a pending workflow cannot be modified
	            alert("<%=bundle.getString("jsmsg_cannot_edit_pending_workflow")%>");
	        }
	        else
	        {
	            // You cannot edit this workflow because you are not a PM or
	            // the wf is completed
	            alert("<%=bundle.getString("jsmsg_cannot_edit_workflow")%>");
	        }
	        return false;
	    }
	    var url = "${modify.pageURL}&wfId=" + wfId + "&jobId=${jobId}";
	    $("#workflowForm").attr("action", url);
		$("#workflowForm").submit();
	}
	else if (specificButton == "Download")
	{
	    checkDelayTime(wfId);
	    return false;
	}
	//only when choosing 1 checkbox,above buttons are available.End.
	
	//choose 1 or more checkbox, below buttons are available.Start.
	else if (specificButton == "Discard")
	{
	    if (wfState.indexOf("BATCH_RESERVED") != -1)
	    {
            // You can only discard workflows that are...DISPATCHED, READY_TO_BE_DISPATCHED
            alert("<%=bundle.getString("jsmsg_cannot_discard_workflow")%>");
            return false;
	    }
        // Warning!! This will discard the wf from the system...
        if (confirm("<%=bundle.getString("jsmsg_warning")%>" + "\n\n" + 
                  "<%=bundle.getString("jsmsg_discard_workflow")%>"))
        {
            var url = "${jobDetails.pageURL}&discardWF=true&wfId=" + wfId + "&jobId=${jobId}";
            // Add flag if it's the last workflow.  The controlhelper will
            // want to go to a different destination page in that case.
            if (selectedCheckboxCounts == 1)
            {
                url += "&lastWF=true";
            }
    		$("#workflowForm").attr("action",url);
    		$("#workflowForm").submit();
        }
        else
        {
            return false;
        }
	}
	else if(specificButton == "UpdateLeverage")
	{
		var url = "${updateLeverage.pageURL}&action=checkHaveNonReadyWFSelected";
	    $.post(url, {selectedWorkFlows: wfId}, function(data){
		    var dataObj = eval('(' + data + ')');
		    var readyWfIds = dataObj.readyWfIds;
		    if (readyWfIds.length == 0) {
			    alert('<%=bundle.getString("msg_no_ready_workflow_selected")%>');
			    return false;
		    }
		    var nonReadyWfs = dataObj.nonReadyWfs;
		    if (nonReadyWfs.length > 0) {
			    var msg = '<%=bundle.getString("msg_update_leverage_ready_only")%>';
				alert(msg);
			    return false;
		    }
			w_updateLeverage = window.open("${updateLeverage.pageURL}&wfId=" + readyWfIds + "&action=getAvailableJobsForWfs", "UpdateLeverage", "height=580,width=700,resizable=no,scrollbars=no");
	    });
	}
   else if (specificButton == "UpdateWordCounts")
   {
	    var url = "${self.pageURL}&updateWordCounts=yes&wfId=" + wfId + "&jobId=${jobId}";
	    $("#workflowForm").attr("action", url);
	    $("#workflowForm").submit();
   }
   else if (specificButton == "WordCount")
   {
	   	var url = "${wordcountList.pageURL}&action=list&wfId=" + wfId + "&jobId=${jobId}";
	    $("#workflowForm").attr("action", url);
	    $("#workflowForm").submit();
   }
   else if(specificButton == "TranslatedText")
	{
	var url = "${translatedTextList.pageURL}&wfId=" + wfId + "&jobId=${jobId}";
	 $("#workflowForm").attr("action", url);
	    $("#workflowForm").submit(); 
	}
   else if (specificButton == "Archive")
   {
      // You can only archive workflows that are in the EXPORTED state.
      if (wfState.indexOf("ARCHIVED") != -1 ||
          wfState.indexOf("BATCH_RESERVED") != -1 ||
          wfState.indexOf("DISPATCHED") != -1 ||
          wfState.indexOf("EXPORT_FAILED") != -1 ||
          wfState.indexOf("IMPORT_FAILED") != -1 ||
          wfState.indexOf("LOCALIZED") != -1 ||
          wfState.indexOf("PENDING") != -1 ||
          wfState.indexOf("READY_TO_BE_DISPATCHED") != -1)
      {
         // You can only archive workflows that are...EXPORTED
         alert("<%=bundle.getString("jsmsg_cannot_archive_workflow")%>");
         return false;
      }
      if (wfState.indexOf("EXPORTING") != -1)
      {
         alert("<%=bundle.getString("jsmsg_cannot_operate_workflow_exporting")%>");
         return false;
      }
	  var url = "${jobDetails.pageURL}&archiveWF=true&wfId=" + wfId + "&jobId=${jobId}";
	  $("#workflowForm").attr("action", url);
	  $("#workflowForm").submit();
   }
   else if (specificButton == "Export")
   {
      // You cannot export workflows in the following state
      // because no work has been done on the pages, the workflow
      // has not started.
      if (wfState.indexOf("BATCH_RESERVED") != -1 ||
          wfState.indexOf("PENDING") != -1 ||
          wfState.indexOf("IMPORT_FAILED") != -1)
      {
         // You can only archive workflows that are...EXPORTED
         alert("<%=bundle.getString("jsmsg_cannot_export_workflow")%>");
         return false;
      }
      if (wfState.indexOf("EXPORTING") != -1)
      {
         alert("<%=bundle.getString("jsmsg_cannot_operate_workflow_exporting")%>");
         return false;
      }

     	var checkUrl = "${self.pageURL}&checkIsUploadingForExport=true&wfId=" + wfId + "&t=" + new Date().getTime();
		$.post(checkUrl,function(data){
			var url = "${export.pageURL}&wfId=" + wfId + "&exportSelectedWorkflowsOnly=true&jobId=${jobId}";
			if(data == "uploading")
			{
				alert("One or more workflows are uploading. Please wait.");
				return false;
			}
			else
			{
			  $("#workflowForm").attr("action", url);
			  $("#workflowForm").submit();
			}
		});
		   
	  
   }
   else if (specificButton == "Dispatch")
   {
	   <c:if test="${!jobHasSetCostCenter}">
		   alert("<%=bundle.getString("msg_cost_center_empty")%>");
		   return false;
	   </c:if>
       // You can only dispatch workflows that are in the READY_TO_BE_DISPATCHED,
       // state.
       // If user just select one workflow and its state is READY_TO_BE_DISPATCHED,
       // there needs add a leading space 
       var tmpState = " " + wfState;
       if (tmpState.indexOf("ARCHIVED") != -1 ||
           tmpState.indexOf("BATCH_RESERVED") != -1 ||
 
           // Add a leading space so we don't pick up READY_TO_BE_DISPATCHED
           tmpState.indexOf(" DISPATCHED") != -1 ||
 
           tmpState.indexOf("EXPORTED") != -1 ||
           tmpState.indexOf("EXPORT_FAILED") != -1 ||
           tmpState.indexOf("IMPORT_FAILED") != -1 ||
           tmpState.indexOf("LOCALIZED") != -1 ||
           tmpState.indexOf("PENDING") != -1)
       {
          // You can only dispatch workflows that are...READY_TO_BE_DISPATCHED
          alert("<%=bundle.getString("jsmsg_cannot_dispatch_workflow")%>");
          return false;
       }
	   var url = "${jobDetails.pageURL}&dispatchWF=true&wfId=" + wfId + "&jobId=${jobId}";
	   $("#workflowForm").attr("action", url);
	   $("#workflowForm").submit();
   }
   else if (specificButton == "skip")
   {
	   var wfStateArray = new Array();
	   wfStateArray = wfState.split(" ");
	   for(var i=0;i<wfStateArray.length;i++){
           if(wfStateArray[i] != "DISPATCHED")
           {
                alert("<%=bundle.getString("jsmsg_cannot_skip")%>");
        		return false;
           }
	   }
	   var url = "${skip.pageURL}&wfId=" + wfId + "&jobId=${jobId}";
	   $("#workflowForm").attr("action", url);
	   $("#workflowForm").submit();
   }
   else if (specificButton == "sendingbackEditionJob")
   {
	   var url = "${self.pageURL}&action=sendingbackEditionJob&wfId=" + wfId + "&jobId=${jobId}";
	   $("#workflowForm").attr("action", url);
	   $("#workflowForm").submit();
   }
   else if(specificButton == "downloadQAReport")
   {
	   $.ajax({
		   type: "POST",
		   dataType : "text",
		   url: "${self.pageURL}&action=checkDownloadQAReport",
		   data: "wfId=" + wfId + "&jobId=${jobId}",
		   success: function(data){
		      var returnData = eval(data);
	   		  if (returnData.download == "fail")
	          {
	   			alert("<%=bundle.getString("lb_download_qa_reports_message")%>");
	          }
	          else if(returnData.download == "success")
	          {
	        	  $("#workflowForm").attr("action", "${self.pageURL}&action=downloadQAReport");
	       	   	  $("#workflowForm").submit();
	          }
		   },
	   	   error:function(error)
	       {
          		alert(error.message);
           }
		});
   }
}

function checkDelayTime(wfId){
	var url = "${self.pageURL}&obtainTime=true&wfId=" + wfId + "&t=" + new Date().getTime();
	$.get(url,function(data){
		var checkTime = data.split(",");
		var delayTime = checkTime[0];
		var leftTime = checkTime[1];
		if(parseInt(leftTime) > 0){
			alert("<%=bundle.getString("msg_task_download_time")%>".replace("%1", delayTime).replace("%2", leftTime));
		}else{
			$("#downloadJobId").val("${jobId}");
			$("#downloadWorkflowIds").val(wfId);
			var submitUrl = "${download.pageURL}&fromJobDetail=true&firstEntry=true&redirectToWorkflow=${jobId}";
		    $("#workflowForm").attr("action", submitUrl);
			$("#workflowForm").submit();
		}
	});	
}

function changeWorkflowPriority(wfId,priority){
	var url = "${jobDetails.pageURL}&changePriority=true&wfId=" + wfId + "&priority" + wfId + "=" + priority + "&t=" + new Date().getTime();
	$.get(url,function(data){
		if (data == "OK"){
			alert("You have changed the priority of this workflow!");
		}else{
			alert("Failed to change the priority of this workflow!");
		}
	})
}

function closeOpenedWindow()
{
    try { w_updateLeverage.close(); } catch (e) {};
    w_updateLeverage = null;
}
</script>

<script type="text/javascript">
// Set cookie for most recently used job list
var thisjob = "${jobId}:${Job.jobName}";
var cookie = getJobCookie("<%=JobSearchConstants.MRU_JOBS_COOKIE%><%=session.getAttribute(WebAppConstants.USER_NAME).hashCode()%>");
if (cookie.length != 0)
{
    // only save last 3.  make sure this one isn't already on the list.
    var lastjobs = thisjob;
    var jobs = cookie.split(",");
    for (i = 0; i < jobs.length && i < 3; i++)
    {
        if (jobs[i] != thisjob)
        {
            lastjobs += "," + jobs[i];
        }
    }
	setJobCookie(lastjobs,"<%=JobSearchConstants.MRU_JOBS_COOKIE%><%=session.getAttribute(WebAppConstants.USER_NAME).hashCode()%>" + "=");
}
else
{
	setJobCookie(thisjob,"<%=JobSearchConstants.MRU_JOBS_COOKIE%><%=session.getAttribute(WebAppConstants.USER_NAME).hashCode()%>" + "=");
}
</script>
</body>
</html>
