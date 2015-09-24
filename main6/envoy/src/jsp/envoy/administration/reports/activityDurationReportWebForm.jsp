<%@ page contentType="text/html; charset=UTF-8"
         errorPage="/envoy/common/activityError.jsp"
         import="java.util.*,
                  com.globalsight.everest.webapp.WebAppConstants,
                  com.globalsight.everest.webapp.pagehandler.PageHandler,
                  com.globalsight.everest.webapp.pagehandler.administration.reports.ReportConstants,
                  com.globalsight.everest.webapp.pagehandler.administration.reports.ReportJobInfo,
                  com.globalsight.everest.webapp.pagehandler.administration.reports.bo.ReportsData,
                  com.globalsight.everest.webapp.pagehandler.projects.workflows.JobSearchConstants,
                  com.globalsight.everest.foundation.SearchCriteriaParameters,
                  com.globalsight.everest.projecthandler.Project,
                  com.globalsight.everest.jobhandler.Job,
                  com.globalsight.util.GlobalSightLocale,
                  com.globalsight.util.edit.EditUtil,
                  com.globalsight.everest.company.CompanyWrapper,
                  java.util.Locale,
                  java.util.ResourceBundle"
          session="true"
%>
<%
    String EMEA = CompanyWrapper.getCurrentCompanyName();
    ResourceBundle bundle = PageHandler.getBundle(session);
    Locale uiLocale = (Locale)session.getAttribute(WebAppConstants.UILOCALE);
    
    // Field names
    String creationStart = JobSearchConstants.CREATION_START;
    String creationStartOptions = JobSearchConstants.CREATION_START_OPTIONS;
    String creationEnd = JobSearchConstants.CREATION_END;
    String creationEndOptions = JobSearchConstants.CREATION_END_OPTIONS;

    List<ReportJobInfo> jobList = (ArrayList<ReportJobInfo>)
        request.getAttribute(ReportConstants.REPORTJOBINFO_LIST);
    List<Project> projectList = (ArrayList<Project>)
        request.getAttribute(ReportConstants.PROJECT_LIST);
    List<GlobalSightLocale> targetLocales = (ArrayList<GlobalSightLocale>)
        request.getAttribute(ReportConstants.TARGETLOCALE_LIST);
    
    String basicAction = "/globalsight/ControlServlet?linkName=generateReports&pageName=JOBREPORTS";
    String formAction = basicAction + "&action=" + ReportConstants.GENERATE_REPORTS;
%>
<html>
<!-- This JSP is: /envoy/administration/reports/activityDurationReportWebForm.jsp-->
<head>
<title><%= EMEA%> <%=bundle.getString("activity_duration_report_web_form")%></title>
</head>
<body leftmargin="0" rightmargin="0" topmargin="0" marginwidth="0" marginheight="0" bgcolor="LIGHTGREY" onLoad="doOnload()">
<link href="/globalsight/jquery/jQueryUI.redmond.css" rel="stylesheet" type="text/css"/>
<script type="text/javascript" src="/globalsight/envoy/administration/reports/report.js"></script>
<script type="text/javascript" src="/globalsight/jquery/jquery-1.6.4.min.js"></script>
<script type="text/javascript" src="/globalsight/jquery/jquery-ui-1.8.18.custom.min.js"></script>
<script type="text/javascript">
var inProgressStatus = "<%=ReportsData.STATUS_INPROGRESS%>";

//Set the jobs data for js(jobInfos)
var jobInfos = new Array();
<%
for(int i=0; i<jobList.size(); i++)  
{
    ReportJobInfo j = jobList.get(i);
%>
	jobInfos[<%=i%>] = new JobInfo(<%=j.getJobId()%>, "<%=EditUtil.encodeTohtml(j.getJobName())%>", <%=j.getProjectId()%>, "<%=j.getJobState()%>", "<%=j.getTargetLocalesStr()%>");
<%
}
%>

function setDisableTRWrapper(trid)
{
	if(trid == "idTRJobIds")
	{
		setDisableTR("idTRJobIds", true);
		setDisableTR("idTRJobNames", false);
		setDisableTR("idTRProject", false);
		setDisableTR("idTRJobStatus", false);
		filterJob();
	}
	else if(trid == "idTRJobNames")
	{
		setDisableTR("idTRJobIds", false);
		setDisableTR("idTRJobNames", true);
		setDisableTR("idTRProject", true);
		setDisableTR("idTRJobStatus", true);
	}
}
$(document).ready(function(){
	$("#csf").datepicker({
		changeMonth: true,
		showOtherMonths: true,
		selectOtherMonths: true,
		onSelect: function( selectedDate ) {
			$("#cef").datepicker( "option", "minDate", selectedDate );
		}
	});
	$("#cef").datepicker({
		changeMonth: true,
		showOtherMonths: true,
		selectOtherMonths: true,
		onSelect: function( selectedDate ) {
			$("#csf").datepicker( "option", "maxDate", selectedDate );
		}
	});
});

function defautSelect(){
	var jobIdsval;
	
	jobIdsval=$("#jobNameList").val();
	
	if(jobIdsval)return;
     var ops=$("#jobNameList").children();
     if(ops.length==0){
      return ('No job name(s) is(are) selected.');
     }else{
      ops.attr("selected", true);
     }
}

function dataSelectAll(){
	var startVal=searchForm.<%=creationStart%>.value;
	if(startVal){
		defautSelect();
		  return ""; 
	}
	
	var endVal=searchForm.<%=creationEnd%>.value;
	if(endVal){
		defautSelect();
        return ""; 
	}
	  return "";
}

//The function for canceling the report.
function fnDoCancel() {
  $.ajax({
    type: 'POST',
    dataType: 'json',
    url: '<%=basicAction + "&action=" + ReportConstants.ACTION_GET_REPORTSDATA%>',
    data: $("form[name='searchForm']").serialize(),
    success: function(data) {
      if (data != null && data.status == inProgressStatus) {
        if (confirm("<%=bundle.getString("msg_cancel_report")%>")) {
          $.ajax({
            type: 'POST',
            dataType: 'json',
            url: '<%=basicAction + "&action=" + ReportConstants.ACTION_CANCEL_REPORTS%>',
            data: $("form[name='searchForm']").serialize(),
            success: function (data) {}
          });
        } else {
          return;
        }
      }
      else
      {
    	 window.close();
      }
    }
  });
}

function submitForm() {
	var msg =  dataSelectAll();
   	if (msg != "")
   	{
    	alert(msg);
    	return;
   	}
   
   	alertInfo = null;
	var jobIDArr = fnGetSelectedJobIds();
	if(jobIDArr == null || jobIDArr.length == 0)
	{
		if(alertInfo != null)
			alert(alertInfo); 
		return;	
	}	

	if(isContainValidTargetLocale(jobIDArr, getSelValueArr("targetLocalsList"), jobInfos))
	{
		alert("<%=bundle.getString("msg_invalid_targetLocales")%>");
		return;
	}
	
	var startVal=searchForm.<%=creationStart%>.value;
	var endVal=searchForm.<%=creationEnd%>.value;
	
	if((!startVal) &&(!endVal))
	{
		$("#dateRange").val("N")
	}else{
		$("#dateRange").val("Y")
	}

	document.getElementById("inputJobIDS").value = jobIDArr.toString(); 
  $.ajax({
      type: 'POST',
      dataType: 'json',
      url: '<%=basicAction + "&action=" + ReportConstants.ACTION_GET_REPORTSDATA%>',
      data: $("form[name='searchForm']").serialize(),
      success: function (data) {
        if(data != null && data.status == inProgressStatus) {
          alert('<%=bundle.getString("msg_duplilcate_report")%>');
        } 
        else if(data != null && data.error){
        	alert(data.error);
        }
        	else {
          $("form[name='searchForm']").submit();
        }
      }
  });
}

function fnGetSelectedJobIds()
{
	var jobIDArr = new Array();
	if(document.getElementsByName("reportOn")[0].checked)
	{
		var jobIDText = document.getElementById("jobIds").value;
		jobIDText = jobIDText.replace(/(^\s*)|(\s*$)/g, "");	
		if(jobIDText.substr(0, 1) == "," || jobIDText.substr(jobIDText.length-1, jobIDText.length) == ",")
		{
			alertInfo = '<%=bundle.getString("lb_invalid_jobid")%>';			
			return;
		}
		jobIDArr = jobIDText.split(",");
		if(!validateIDS(jobIDArr, jobInfos))
		{
			alertInfo = '<%=bundle.getString("lb_invalid_jobid")%>';
			return;
		}
	}else{
		var selObj = document.getElementById("jobNameList");
		for (i=0; i<selObj.options.length; i++) 
		{
			if (selObj.options[i].selected) 
			{
				jobIDArr.push(selObj.options[i].value);
			}
		}
		
		if(!validateIDS(jobIDArr, jobInfos))
	    {
			alertInfo = '<%=bundle.getString("msg_invalid_jobName")%>';
			return;
	    }
	}	
/* 	jobIDArr.sort(sortNumber); */
	
	return jobIDArr;
}

function filterJob(){
	if(document.getElementsByName("reportOn")[0].checked)
	{
		return;
	}
	
	var jobNameList = document.getElementById("jobNameList");
	var projectNameList = document.getElementById("projectId");
	var jobStatus = document.getElementById("status");
	var targetLocalesList = document.getElementById("targetLocalsList");
	
	// selected project 
	var currSelectValueProject = new Array();
	for(i=0;i<projectNameList.length;i++)
	{
		var op= projectNameList.options[i];
		if(op.selected)
		{
	    	currSelectValueProject.push(op.value);
		}
	}
	
	// selected job status 
	var currSelectValueJobStatus = new Array();
	for(i=0;i<jobStatus.length;i++)
	{
		var op= jobStatus.options[i];
		if(op.selected)
		{
	    	currSelectValueJobStatus.push(op.value);
		}
	} 
	   
	// selected target locales 
	var currSelectValueTargetLocale = new Array();
	for(i=0;i<targetLocalesList.length;i++)
	{
		var op= targetLocalesList.options[i];
		if(op.selected)
		{
	    	currSelectValueTargetLocale.push(op.value);
		}
	}
	jobNameList.options.length=0;
	
	// Insert jobNameList select options 
	for(var i=0; i<jobInfos.length; i++)
	{
		if(contains(currSelectValueProject, jobInfos[i].projectId)
			&& contains(currSelectValueJobStatus, jobInfos[i].jobStatus)
			&& containsArray(currSelectValueTargetLocale, jobInfos[i].targetLocals))
		{
			addOption("jobNameList", jobInfos[i].jobName, jobInfos[i].jobId);
		}
	}
}
function sortNumber(a,b) 
{ 
	return a - b 
}

function doOnload()
{
	// Initial jobNameList select options 
	for(var i=0; i<jobInfos.length; i++)
	{
		addOption("jobNameList", jobInfos[i].jobName, jobInfos[i].jobId);
	}
	
	// Set the jobIds as default check. 
	setDisableTRWrapper("idTRJobNames");
}
</script>
<TABLE WIDTH="100%" BGCOLOR="WHITE">
<TR><TD ALIGN="CENTER"><IMG SRC="/globalsight/images/logo_header.gif"></TD></TR>
</TABLE><BR>
<span class="mainHeading"><B><%= EMEA%> <%=bundle.getString("activity_duration_report_web_form")%></B></span>
<BR><BR>
<TABLE WIDTH="80%">
<TR><TD>
<SPAN CLASS="smallText">
<%=bundle.getString("optionally_submit_generate")%> <%=bundle.getString("hold_the_shift")%></SPAN>
</TD></TR></TABLE>

<form name="searchForm" method="post" action="<%=formAction%>">
<input type="hidden" name="<%=ReportConstants.REPORT_TYPE%>" value="<%=ReportConstants.ACTIVITY_DURATION_REPORT%>">
<input type="hidden" id="inputJobIDS" name="inputJobIDS">
<input type="hidden" id="dateRange" name="dateRange">

<table border="0" cellspacing="2" cellpadding="2" class="standardText">
  	<tr>
        <td class="standardText"><%=bundle.getString("lb_report_on")%></td>
        <td class="standardText" VALIGN="BOTTOM">
            <table cellspacing=0>
                <tr id="idTRJobIds">
                    <td><input type="radio" name="reportOn" checked onclick="setDisableTRWrapper('idTRJobNames');" value="jobIds"/><%=bundle.getString("lb_job_ids")%></td>
                    <td><input type="text" id="jobIds" name="jobIds" value=""><%=bundle.getString("lb_job_ids_description")%></td>
                </tr>
                <tr id="idTRJobNames">
                    <td><input type="radio" name="reportOn" onclick="setDisableTRWrapper('idTRJobIds');" value="jobNames"/><%=bundle.getString("lb_job_name")%>:</td>
                    <td class="standardText" VALIGN="BOTTOM"><select id="jobNameList" name="jobNameList" MULTIPLE size="6" style="width:300px;min-height:90px;"></select></td>
                </tr>
            </table>
        </td>
    </tr>

    <tr id="idTRProject">
        <td class="standardText"><%=bundle.getString("lb_project")%>:</td>
        <td class="standardText" VALIGN="BOTTOM">
        <select id="projectId" name="projectId" MULTIPLE size=4 onchange="filterJob()">
            <option VALUE="*" SELECTED>&lt;<%=bundle.getString("all")%>&gt;</OPTION>
<%          for (Project p : projectList)
            {
%>          <option VALUE="<%=p.getId()%>"><%=p.getName()%></OPTION>
<%          }
%>
        </select>
        </td>
    </tr>

    <tr id="idTRJobStatus">
        <td class="standardText"><%=bundle.getString("lb_status")%><span class="asterisk">*</span>:</td>
        <td class="standardText" VALIGN="BOTTOM">
        <select id="status" name="status" MULTIPLE size=4 onchange="filterJob()">
            <option value="*" SELECTED>&lt;<%=bundle.getString("all")%>&gt;</OPTION>
            <option value='<%=Job.READY_TO_BE_DISPATCHED%>'><%= bundle.getString("lb_ready") %></option>
            <option value='<%=Job.DISPATCHED%>'><%= bundle.getString("lb_inprogress") %></option>
            <option value='<%=Job.LOCALIZED%>'><%= bundle.getString("lb_localized") %></option>
            <option value='<%=Job.EXPORTED%>'><%= bundle.getString("lb_exported") %></option>
            <option value='<%=Job.EXPORT_FAIL%>'><%= bundle.getString("lb_exported_failed") %></option>
            <option value='<%=Job.ARCHIVED%>'><%= bundle.getString("lb_archived") %></option>
        </select>
        </td>
    </tr>

    <tr>
        <td class="standardText"><%=bundle.getString("lb_target_locales")%>*:</td>
        <td class="standardText" VALIGN="BOTTOM">
        <select id="targetLocalsList" name="targetLocalesList" multiple="true" size=4 onchange="filterJob()">
            <option value="*" selected>&lt;<%=bundle.getString("all")%>&gt;</OPTION>
<%
            for(GlobalSightLocale gsLocale : targetLocales)
            {
%>          <option VALUE="<%=gsLocale.getId()%>"><%=gsLocale.getDisplayName(uiLocale)%></OPTION>
<%          }
%>
        </select>
        </td>
    </tr>

    <tr>
        <td class="standardText" colspan=2><%=bundle.getString("lb_creation_date_range")%>:</td>
    </tr>
    <tr>
        <td class="standardText" style="padding-left:70px" colspan=2 VALIGN="BOTTOM">
            <%=bundle.getString("lb_starts")%>:
            <input type="text" id="csf" name="<%=creationStart%>">
            <%=bundle.getString("lb_ends")%>:
            <input type="text" id="cef" name="<%=creationEnd%>">
        </td>
    </tr>

    <tr>
        <td class="standardText"><%=bundle.getString("date_display_format")%>:</td>
        <td class="standardText" VALIGN="BOTTOM">
        <select name="dateFormat">
<%
        String dateFormats[] = new String[4];
        int i=0;
        dateFormats[i++] = "MM/dd/yy hh:mm:ss";
        dateFormats[i++] = "MM/dd/yy HH:mm:ss";
        dateFormats[i++] = "yyyy/MM/dd HH:mm:ss";
        dateFormats[i++] = "yyyy/MM/dd hh:mm:ss";
        for (i=0;i<dateFormats.length;i++) {
 %>
            <OPTION VALUE="<%=dateFormats[i]%>"><%=dateFormats[i]%></OPTION>
<%      }
%>
        </select>
        </td>
    </tr>
    <tr>
        <td><input type="BUTTON" VALUE="<%=bundle.getString("lb_shutdownSubmit")%>" onClick="submitForm()"></td>
        <TD><INPUT type="BUTTON" VALUE="<%=bundle.getString("lb_cancel")%>" onClick="fnDoCancel();"></TD>
    </tr>
</table>
</form>
<BODY>
</HTML>
