<%@ page contentType="text/html; charset=UTF-8"
         errorPage="/envoy/common/activityError.jsp"
         import="java.util.*,
                  com.globalsight.everest.webapp.WebAppConstants,
                  com.globalsight.everest.projecthandler.Project,
                  com.globalsight.everest.webapp.pagehandler.administration.reports.ReportConstants,
                  com.globalsight.everest.webapp.pagehandler.administration.reports.ReportJobInfo,
                  com.globalsight.everest.webapp.pagehandler.administration.reports.bo.ReportsData,
                  com.globalsight.everest.webapp.pagehandler.PageHandler,
                  com.globalsight.everest.jobhandler.Job,
                  com.globalsight.util.GlobalSightLocale,
                  com.globalsight.util.edit.EditUtil,
                  com.globalsight.util.SortUtil,
                  com.globalsight.everest.util.comparator.GlobalSightLocaleComparator,
                  java.util.Locale,
                  java.util.ResourceBundle"
          session="true"
%>
<%
   	List<Project> projectList = (ArrayList<Project>)
      	     request.getAttribute(ReportConstants.PROJECT_LIST);
    List<GlobalSightLocale> targetLocales = (ArrayList<GlobalSightLocale>)
            request.getAttribute(ReportConstants.TARGETLOCALE_LIST);
    Locale uiLocale = (Locale) session.getAttribute(WebAppConstants.UILOCALE);
    if (uiLocale == null)
    {
    	uiLocale = Locale.US;
    }

   	ResourceBundle bundle = PageHandler.getBundle(session);
   	String basicAction = "/globalsight/ControlServlet?linkName=generateReports&pageName=JOBREPORTS";
    String formAction = basicAction + "&action=" + ReportConstants.GENERATE_REPORTS;
%>
<html>
<!-- This JSP is: /envoy/administration/reports/LisaPostReviewQAReportWebForm.jsp-->
<head>
<title><%=bundle.getString("post_review_qa_report")%></title>
<script type="text/javascript" src="/globalsight/envoy/administration/reports/report.js"></script>
<script type="text/javascript" src="/globalsight/jquery/jquery-1.6.4.min.js"></script>
<SCRIPT SRC="/globalsight/includes/library.js"></SCRIPT>
<script type="text/javascript">
var inProgressStatus = "<%=ReportsData.STATUS_INPROGRESS%>";
//All job info list.
var reportJobInfo;

function doSubmit()
{
	var jobIDArr = fnGetSelectedJobIds();
	if(jobIDArr == null || jobIDArr.length == 0)
	{
		return;	
	}

	// Submit the Form, if possible(No report is generating.)
	$.ajax({
		type: 'POST',
		url:  '<%=basicAction + "&action=" + ReportConstants.ACTION_GET_REPORTSDATA%>',
		data: {'inputJobIDS': jobIDArr.toString(),
			   'reportType': $("input[name='reportType']").val()},
		success: function(data) {
					if (data != null && data.status == inProgressStatus) {
		          		alert("<%=bundle.getString("msg_duplilcate_report")%>");
		        	}
					else
					{
						document.getElementById("inputJobIDS").value = jobIDArr.toString();
						$("form[name='lisaQAForm']").submit();
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
    		url : '${self.pageURL}&activityName=xlsReportPostReviewQA&action=getReportJobInfo',
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
	if(lisaQAForm.reportOnJobId.checked)
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
			alert('<%=bundle.getString("lb_invalid_jobid_exist")%>');
			return;
		}
		
		if(isContainValidTargetLocale(jobIDArr, getSelValueArr("targetLocalesList"), jobInfos))
		{
			alert("<%=bundle.getString("lb_invalid_target_language")%>");
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

function doOnload()
{
	// Set the jobIds as default check. 
    setDisableTRWrapper("idTRJobNames");
}

function filterJob()
{
    if(lisaQAForm.reportOnJobId.checked)
    {
        return;
    }

    // If job list is null, initialize it first.
    if (reportJobInfo == null)
    {
    	var varItem = new Option("Loading jobs, please wait ...", "-1");
    	lisaQAForm.jobNameList.options.add(varItem);
    	lisaQAForm.submitButton.disabled = true;

        $.ajax({
    		type : "POST",
    		url : '${self.pageURL}&activityName=xlsReportPostReviewQA&action=getReportJobInfo',
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
	lisaQAForm.jobNameList.options.length = 0;

    //selected job status
    var currSelectValueJobStatus = new Array();
    for(i = 0; i < lisaQAForm.jobStatus.length; i++)
    {
        var op = lisaQAForm.jobStatus.options[i];
        if(op.selected)
        {
            currSelectValueJobStatus.push(op.value);
        }
    }

    //selected target locales
    var currSelectValueTargetLocale = new Array();
    for(i = 0; i < lisaQAForm.targetLocalesList.length; i++)
    {
        var op = lisaQAForm.targetLocalesList.options[i];
        if(op.selected)
        {
            currSelectValueTargetLocale.push(op.value);
        }
    }

    //selected project
    var currSelectValueProject = new Array();
    for(i = 0; i < lisaQAForm.projectNameList.length; i++)
    {
        var op = lisaQAForm.projectNameList.options[i];
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
                lisaQAForm.jobNameList.options.add(varItem);
            }
        }
     });

    if(lisaQAForm.jobNameList.options.length==0)
    {
    	lisaQAForm.submitButton.disabled=true;
    }
    else
    {
    	lisaQAForm.submitButton.disabled=false;
    }
}

//Select JobIds or Job Name. 
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
    	lisaQAForm.submitButton.disabled=false;
        setDisableTR("idTRJobIds", false);
        setDisableTR("idTRJobNames", true);
        setDisableTR("idTRProject", true);
        setDisableTR("idTRJobStatus", true);
    }
}
</script>
<%@ include file="/envoy/common/shortcutIcon.jspIncl" %>
</head>
<BODY leftmargin="0" rightrmargin="0" topmargin="0" marginwidth="0" marginheight="0" bgcolor="LIGHTGREY"  onLoad="doOnload()">
<TABLE WIDTH="100%" BGCOLOR="WHITE">
    <TR><TD ALIGN="CENTER"><IMG SRC="/globalsight/images/logo_header.gif"></TD></TR>
</TABLE><BR>

<span class="mainHeading"><B><%=bundle.getString("post_review_qa_report")%></B></span>
<BR><BR>

<TABLE WIDTH="80%">
    <TR><TD><SPAN CLASS="smallText"><%=bundle.getString("optionally_submit_generate")%><%=bundle.getString("hold_the_shift")%></SPAN></TD></TR>
</TABLE>

<form name="lisaQAForm" method="post" action="<%=formAction%>">
<input type="hidden" name="<%=ReportConstants.REPORT_TYPE%>" value="<%=ReportConstants.POST_REVIEW_QA_REPORT%>">
<input type="hidden" id="inputJobIDS" name="inputJobIDS">

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
            <select id="projectNameList" name="projectNameList" MULTIPLE size="4" onChange="filterJob()">
                <option VALUE="*" SELECTED>&lt;<%=bundle.getString("all")%>&gt;</OPTION>
<%
                Iterator<Project> iterProject = projectList.iterator();
                while (iterProject.hasNext())
                {
                    Project p = iterProject.next();
%>
         	    <option VALUE="<%=p.getId()%>"><%=p.getName()%></OPTION>
<%
                }
%>
            </select>
        </td>
     </tr>

     <tr id="idTRJobStatus">
         <td class="standardText"><%=bundle.getString("lb_job_status")%>:</td>
         <td class="standardText" VALIGN="BOTTOM">
             <select id="jobStatus" name="jobStatus" MULTIPLE size="4" onChange="filterJob()">
                 <option value="*" selected>&lt;<%=bundle.getString("all")%>&gt;</OPTION>
                 <option VALUE="<%=Job.READY_TO_BE_DISPATCHED%>"><%=bundle.getString("lb_ready")%></OPTION>
                 <option VALUE="<%=Job.DISPATCHED%>"><%=bundle.getString("lb_inprogress")%></OPTION>
                 <option VALUE="<%=Job.LOCALIZED%>"><%=bundle.getString("lb_localized")%></OPTION>
                 <option VALUE="<%=Job.EXPORTED%>"><%=bundle.getString("lb_exported")%></OPTION>
                 <option VALUE="<%=Job.EXPORT_FAIL%>"><%=bundle.getString("lb_exported_failed")%></OPTION>
                 <option VALUE="<%=Job.ARCHIVED%>"><%=bundle.getString("lb_archived")%></OPTION>
             </select>
         </td>
    </tr>
 		<tr>
                 <td/><td/> 
                </tr>
                <tr>
                <td/><td/>
                </tr>
    <tr>
        <td class="standardText"><%=bundle.getString("lb_target_language")%>:</td>
        <td class="standardText" VALIGN="BOTTOM">
            <select id="targetLocalesList" name="targetLocalesList" multiple="true" size=5 onChange="filterJob()">
            <option value="*" selected>&lt;<%=bundle.getString("all")%>&gt;</OPTION>
<%
            SortUtil.sort(targetLocales, new GlobalSightLocaleComparator(Locale.getDefault()));
            Iterator iterLocale = targetLocales.iterator();
            while(iterLocale.hasNext())
            {
                GlobalSightLocale gsLocale = (GlobalSightLocale) iterLocale.next();
%>
                <option VALUE="<%=gsLocale.getId()%>"><%=gsLocale.getDisplayName(uiLocale)%></OPTION>
<%
            }
%>
        </select>
        </td>
    </tr>
     <tr>
                 <td/><td/> 
     </tr>
                <tr>
                <td/><td/>
                </tr>
    <tr>
        <TD><INPUT type="BUTTON" id="submitButton" VALUE="<%=bundle.getString("lb_shutdownSubmit")%>" onClick="doSubmit();"></TD>
        <TD><INPUT type="BUTTON" VALUE="<%=bundle.getString("lb_cancel")%>" onClick="window.close()"></TD>
    </tr>
</table>
</form>
</BODY>
</HTML>