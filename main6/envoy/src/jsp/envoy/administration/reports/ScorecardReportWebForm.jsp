<%@ page contentType="text/html; charset=UTF-8"
         errorPage="/envoy/common/activityError.jsp"
         import="java.util.*, 
         	     com.globalsight.everest.jobhandler.Job,
                 com.globalsight.everest.projecthandler.Project,
                 com.globalsight.everest.foundation.SearchCriteriaParameters,
                 com.globalsight.everest.webapp.pagehandler.projects.workflows.JobSearchConstants, 
                 com.globalsight.everest.webapp.WebAppConstants,
                 com.globalsight.everest.webapp.pagehandler.administration.reports.ReportConstants,
                 com.globalsight.everest.webapp.pagehandler.administration.reports.ReportJobInfo,
                 com.globalsight.everest.webapp.pagehandler.administration.reports.bo.ReportsData,
                 com.globalsight.everest.webapp.pagehandler.PageHandler,
                 com.globalsight.util.edit.EditUtil,
                 com.globalsight.util.SortUtil,
                 com.globalsight.everest.util.comparator.GlobalSightLocaleComparator,
                 com.globalsight.util.GlobalSightLocale,
                 java.util.Locale,
                 java.util.ResourceBundle"
          session="true"
%>
<%
    Locale uiLocale = (Locale)session.getAttribute(WebAppConstants.UILOCALE);
    if (uiLocale == null)
    {
    	uiLocale = Locale.ENGLISH;
    }
    ResourceBundle bundle = PageHandler.getBundle(session);
    
   	List<Project> projectList = (ArrayList<Project>)
   	     request.getAttribute(ReportConstants.PROJECT_LIST);
    List<GlobalSightLocale> targetLocales = (ArrayList<GlobalSightLocale>)
         request.getAttribute(ReportConstants.TARGETLOCALE_LIST);

    String basicAction = "/globalsight/ControlServlet?linkName=generateReports&pageName=JOBREPORTS";
    String formAction = basicAction + "&action=" + ReportConstants.GENERATE_REPORTS;
    
    String creationStart = JobSearchConstants.CREATION_START;
    String creationStartOptions = JobSearchConstants.CREATION_START_OPTIONS;
    String creationEnd = JobSearchConstants.CREATION_END;
    String creationEndOptions = JobSearchConstants.CREATION_END_OPTIONS;
    
%>
<html>
<!-- This JSP is: /envoy/administration/reports/ScorecardReportWebForm.jsp-->
<head>
<title><%=bundle.getString("scorecard_report_web_form")%></title>
<script type="text/javascript" src="/globalsight/envoy/administration/reports/report.js"></script>
<script type="text/javascript" src="/globalsight/jquery/jquery-1.6.4.min.js"></script>
<link href="/globalsight/jquery/jQueryUI.redmond.css" rel="stylesheet" type="text/css"/>
<script type="text/javascript" src="/globalsight/jquery/jquery-ui-1.8.18.custom.min.js"></script>
<script type="text/javascript">
var inProgressStatus = "<%=ReportsData.STATUS_INPROGRESS%>";
var reportJobInfo;

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
    	scorecardReportForm.submitButton.disabled=false;
        setDisableTR("idTRJobIds", false);
        setDisableTR("idTRJobNames", true);
        setDisableTR("idTRProject", true);
        setDisableTR("idTRJobStatus", true);
    }
}

function checkNow(field, text)
{
    if (field.options[1].selected)
        text.value = "";
}

function isInteger(value)
{
    if (value == "") return true;
    return (parseInt(value) == value);
}

function defautSelect(){
	var jobIdsval;
	if(document.getElementsByName("reportOn")[0].checked)
	{
		jobIdsval=$("#jobIds").val();
	}else{
		jobIdsval=$("#jobNameList").val();
	}
	if(jobIdsval)return;
	 $("#idTRJobNamesRadio").trigger("click");
     var ops=$("#jobNameList").children();
     if(ops.length==0){
      return ('No job name(s) is(are) selected.');
     }else{
      ops.attr("selected", true);
     }
}

function dataSelectAll(){
	var startVal=scorecardReportForm.<%=creationStart%>.value;
	if(startVal){
		defautSelect();
		  return ""; 
	}
	
	var endVal=scorecardReportForm.<%=creationEnd%>.value;
	if(endVal){
		defautSelect();
        return ""; 
	}
	  return "";
}

//The canAlert should be false if do cancel.
var canAlert = true;

// The function for canceling the report.
function fnDoCancel() {
  canAlert = false;
  var jobIDArr = fnGetSelectedJobIds();
  canAlert = true;
  if(jobIDArr == null || jobIDArr.length == 0)
	 window.close();	
	
  $.ajax({
    type: 'POST',
    dataType: 'json',
    url: '<%=basicAction + "&action=" + ReportConstants.ACTION_GET_REPORTSDATA%>',
    data: {
      'inputJobIDS': jobIDArr.toString(),
      'reportType': $("input[name='reportType']").val()
    },
    success: function(data) {
      if (data != null && data.status == inProgressStatus) {
        if (confirm("<%=bundle.getString("msg_cancel_report")%>")) {
          $.ajax({
            type: 'POST',
            dataType: 'json',
            url: '<%=basicAction + "&action=" + ReportConstants.ACTION_CANCEL_REPORTS%>',
            data: {
              'inputJobIDS': jobIDArr.toString(),
              'reportType': $("input[name='reportType']").val()
            },
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

function doSubmit()
{
	var msg =  dataSelectAll();
   	if (msg != "")
   	{
    	alert(msg);
    	return;
   	}
	var jobIDArr = fnGetSelectedJobIds();
	if(jobIDArr == null || jobIDArr.length == 0)
	{
		return;	
	}
	
	var dataoptions;
	var startVal=scorecardReportForm.<%=creationStart%>.value;
	var endVal=scorecardReportForm.<%=creationEnd%>.value;
	
	if((!startVal) &&(!endVal))
	{
		$("#dateRange").val("N")
		dataoptions={'inputJobIDS': jobIDArr.toString(),
				   'targetLocalesList': getSelValueArr("targetLocalesList").toString(),
				   'reportType': $("input[name='reportType']").val()}
	}else{
		$("#dateRange").val("Y")
		dataoptions={'inputJobIDS': jobIDArr.toString(),
			   'targetLocalesList': getSelValueArr("targetLocalesList").toString(),
			   'reportType': $("input[name='reportType']").val(),
			   'dateRange':'Y',
			   'csf':$("#csf").val(),
			   'cef':$("#cef").val()}
	}

	document.getElementById("inputJobIDS").value = jobIDArr.toString();
	
	// Submit the Form, if possible(No report is generating.)
	$.ajax({
		type: 'POST',
		url:  '<%=basicAction + "&action=" + ReportConstants.ACTION_GET_REPORTSDATA%>',
		data: dataoptions,
		success: function(data) {
					if (data != null && data.status == inProgressStatus) {
				    	alert("<%=bundle.getString("msg_duplilcate_report")%>");
				    } 
					else if (data != null && data.error) {
						alert(data.error);
					}
					else {
				    	$("form[name='scorecardReportForm']").submit();
				    }
    			 },
		dataType: 'json'
	});
}

function fnGetSelectedJobIds()
{
	if (reportJobInfo == null)
    {
		$.ajax({
    		type : "POST",
    		url : '${self.pageURL}&activityName=xlsReportScorecard&action=getReportJobInfo',
    		async : false,
    		dataType : 'text',
    		success : function(data) {
    			reportJobInfo = eval("(" + data + ")");
    		},
    		error : function(request, error, status) {
    			reportJobInfo = "";
    		}
    	});
    }
	return validateJobIds();
}

function validateJobIds()
{
	var jobInfos = new Array();
	$(reportJobInfo).each(function(i, item) {
		jobInfos[i] = new JobInfo(item.jobId, item.jobName, item.projectId, item.jobState, item.targetLocales);
     });
	
	var jobIDArr = new Array();
	if(scorecardReportForm.reportOnJobId.checked)
	{
		var jobIDText = document.getElementById("jobIds").value;
		jobIDText = jobIDText.replace(/(^\s*)|(\s*$)/g, "");
		if(jobIDText.substr(0, 1) == "," || jobIDText.substr(jobIDText.length-1, jobIDText.length) == ","){
			alertInfo = '<%=bundle.getString("lb_invalid_jobid")%>';
			return;
		}
		jobIDArr = jobIDText.split(",");
		if(!validateIDS(jobIDArr, jobInfos))
		{
			if (canAlert)
			{
				alert('<%=bundle.getString("lb_invalid_jobid_exist")%>');
			}
			return;
		}
		
		if(isContainValidTargetLocale(jobIDArr, getSelValueArr("targetLocalesList"), jobInfos))
		{
			if (canAlert)
			{
			    alert("<%=bundle.getString("lb_invalid_target_language")%>");
			}
			return;
		}
	}
	else
	{
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
			alert("<%=bundle.getString("msg_invalid_jobName")%>");
			return;
	    }
	}
	jobIDArr.sort(sortNumber);
	
	return jobIDArr;
}

function filterJob()
{
    if(scorecardReportForm.reportOnJobId.checked)
    {
        return;
    }

    // If job list is null, initialize it first.
    if (reportJobInfo == null)
    {
    	var varItem = new Option("Loading jobs, please wait ...", "-1");
    	scorecardReportForm.jobNameList.options.add(varItem);
    	scorecardReportForm.submitButton.disabled = true;

        $.ajax({
    		type : "POST",
    		url : '${self.pageURL}&activityName=xlsReportScorecard&action=getReportJobInfo',
    		dataType : 'text',
    		success : function(data) {
    			reportJobInfo = eval("(" + data + ")");
    			filterJob2();
    		},
    		error : function(request, error, status) {
    			reportJobInfo = "";
    		}
    	});
    }
    else
    {
        filterJob2();
    }
}

function filterJob2()
{
	scorecardReportForm.jobNameList.options.length = 0;

    //selected job status
    var currSelectValueJobStatus = new Array();
    for(i = 0; i < scorecardReportForm.jobStatus.length; i++)
    {
        var op = scorecardReportForm.jobStatus.options[i];
        if(op.selected)
        {
            currSelectValueJobStatus.push(op.value);
        }
    }

    //selected target locales
    var currSelectValueTargetLocale = new Array();
    for(i = 0; i < scorecardReportForm.targetLocalesList.length; i++)
    {
        var op = scorecardReportForm.targetLocalesList.options[i];
        if(op.selected)
        {
            currSelectValueTargetLocale.push(op.value);
        }
    }

    //selected project
    var currSelectValueProject = new Array();
    for(i = 0; i < scorecardReportForm.projectNameList.length; i++)
    {
        var op = scorecardReportForm.projectNameList.options[i];
        if(op.selected)
        {
            currSelectValueProject.push(op.value);
        }
    }

    $(reportJobInfo).each(function(i, item) {
        if(contains(currSelectValueProject, item.projectId)
 	        && contains(currSelectValueJobStatus, item.jobState))
        {
            var isLocaleFlag = "false";
            $.each(item.targetLocales, function(i, item) {
                if (contains(currSelectValueTargetLocale, item)) {
                    isLocaleFlag = "true";
                    //break the target locales check for performance
                    return false;
                }
            });
            if(isLocaleFlag == "true")
            {
                var varItem = new Option(item.jobName, item.jobId);
                varItem.setAttribute("title", item.jobName);
                scorecardReportForm.jobNameList.options.add(varItem);
            }
        }
     });

    if(scorecardReportForm.jobNameList.options.length==0)
    {
    	scorecardReportForm.submitButton.disabled=true;
    }
    else
    {
    	scorecardReportForm.submitButton.disabled=false;
    }
}

function doOnload()
{
	// Set the jobIds as default check. 
	setDisableTRWrapper("idTRJobNames");
}
</script>
<%@ include file="/envoy/common/shortcutIcon.jspIncl" %>
</head>
<body leftmargin="0" rightmargin="0" topmargin="0" marginwidth="0" marginheight="0" bgcolor="LIGHTGREY" onLoad="doOnload()">
<TABLE WIDTH="100%" BGCOLOR="WHITE">
    <TR><TD ALIGN="CENTER"><IMG SRC="/globalsight/images/logo_header.gif"></TD></TR>
</TABLE><BR>
<span class="mainHeading"><B><%=bundle.getString("scorecard_report_web_form")%></B></span>
<BR><BR>
<TABLE WIDTH="80%">
    <TR><TD><SPAN CLASS="smallText"><%=bundle.getString("optionally_submit_generate")%> <%=bundle.getString("hold_the_shift")%></SPAN></TD></TR>
</TABLE>
<p/><p/>

<form name="scorecardReportForm" method="post" action="<%=formAction%>">
<input type="hidden" name="<%=ReportConstants.REPORT_TYPE%>" value="<%=ReportConstants.SCORECARD_REPORT%>">
<input type="hidden" id="inputJobIDS" name="inputJobIDS">
<input type="hidden" id="dateRange" name="dateRange">
<table border="0" cellspacing="2" cellpadding="2" class="standardText">
    <tr>
        <td class="standardText"><%=bundle.getString("lb_report_on")%></td>
        <td class="standardText" VALIGN="BOTTOM">
            <table cellspacing=0>
            <tr id="idTRJobIds">
                <td><input type="radio" id="reportOnJobId" name="reportOn" checked onclick="setDisableTRWrapper('idTRJobNames');" value="jobIds"/><%=bundle.getString("lb_job_ids")%></td>
                <td><input type="text" id="jobIds" name="jobIds" value=""><%=bundle.getString("lb_job_ids_description")%></td>
            </tr>
            <tr id="idTRJobNames">
                <td><input type="radio" id="reportOnJobName" name="reportOn" onclick="setDisableTRWrapper('idTRJobIds');" value="jobNames"/><%=bundle.getString("lb_job_name")%>:</td>
                <td>
                <select id="jobNameList" name="jobNameList" MULTIPLE size="6" style="width:300px;min-height:90px;" disabled>
                </select>
                </td>
            </tr>
        </table>
        </td>
    </tr>

    <tr id="idTRProject">
        <td class="standardText"><%=bundle.getString("lb_project")%>:</td>
        <td class="standardText" VALIGN="BOTTOM">
        <select id="projectNameList" name="projectNameList" MULTIPLE size=4 onChange="filterJob()" disabled>
            <option VALUE="*" SELECTED>&lt;<%=bundle.getString("all")%>&gt;</OPTION>
<%
            for (Project p : projectList)
            {
%>       		<option VALUE="<%=p.getId()%>"><%=p.getName()%></OPTION>
<%          }
%>
        </select>
        </td>
    </tr>

    <tr id="idTRJobStatus">
        <td class="standardText"><%=bundle.getString("lb_job_status")%>:</td>
        <td class="standardText" VALIGN="BOTTOM">
        <select id="jobStatus" name="jobStatus" multiple="true" size=6 onChange="filterJob()" disabled>
            <option value="*" selected>&lt;<%=bundle.getString("all")%>&gt;</OPTION>
            <option VALUE="<%=Job.READY_TO_BE_DISPATCHED%>"><%=bundle.getString("lb_ready")%></OPTION>
            <option VALUE="<%=Job.DISPATCHED%>"><%=bundle.getString("lb_inprogress")%></OPTION>
            <option VALUE="<%=Job.LOCALIZED%>"><%=bundle.getString("lb_localized")%></OPTION>
            <option VALUE="<%=Job.EXPORTED%>"><%=bundle.getString("lb_exported")%></OPTION>
            <option VALUE="<%=Job.EXPORT_FAIL%>"><%=bundle.getString("lb_exported_failed")%></OPTION>
            <option VALUE="<%=Job.ARCHIVED%>"><%=bundle.getString("lb_archived")%></OPTION>
        </select>
    </tr>

    <tr>
        <td class="standardText"><%=bundle.getString("lb_target_locales")%>:</td>
        <td class="standardText" VALIGN="BOTTOM">
        <select name="targetLocalesList" id="targetLocalesList" MULTIPLE size="4" onChange="filterJob()">
            <option value="*" selected>&lt;<%=bundle.getString("all")%>&gt;</OPTION>
<%
            SortUtil.sort(targetLocales, new GlobalSightLocaleComparator(Locale.getDefault()));
            for (GlobalSightLocale gsl : targetLocales)
            {
%>    		<option VALUE="<%=gsl.getId()%>"><%=gsl.getDisplayName(uiLocale)%></option>
<%          }
%>
        </select>
        </td>
    </tr>

    <tr><td class="standardText" colspan=2><%=bundle.getString("lb_creation_date_range")%>:</td></tr>

    <tr>
        <td class="standardText" style="padding-left:70px" colspan=2 VALIGN="BOTTOM">
            <%=bundle.getString("lb_starts")%>:
            <input type="text" name="<%=creationStart%>" id="csf">
            <%=bundle.getString("lb_ends")%>:
            <input type="text" name="<%=creationEnd%>"  id="cef">
        </td>
    </tr>

    <tr style="display:none">
        <td class="standardText"><%=bundle.getString("date_display_format")%>:</td>
        <td class="standardText" VALIGN="BOTTOM">
        <select name="dateFormat">
<%
            String dateFormats[] = new String[4];
            int i=0;
            dateFormats[i++] = "MM/dd/yy hh:mm:ss a z";
            dateFormats[i++] = "MM/dd/yy HH:mm:ss z";
            dateFormats[i++] = "yyyy/MM/dd HH:mm:ss z";
            dateFormats[i++] = "yyyy/MM/dd hh:mm:ss a z";
            for (i=0;i<dateFormats.length;i++) {
 %>
            <OPTION VALUE="<%=dateFormats[i]%>"><%=dateFormats[i]%></OPTION>
<%          }%>
        </select>
        </td>
    </tr>

    <tr style="height: 20px;"></tr>
    <tr>
        <td><input type="button" id="submitButton" VALUE="<%=bundle.getString("lb_shutdownSubmit")%>"  onClick="doSubmit();"></td>
        <td><input type="button" VALUE="<%=bundle.getString("lb_cancel")%>" onClick="fnDoCancel();"></TD>
    </tr>
</table>
</form>
<BODY>
</HTML>