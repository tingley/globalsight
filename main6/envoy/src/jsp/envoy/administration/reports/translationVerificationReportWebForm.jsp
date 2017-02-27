<%@ page contentType="text/html; charset=UTF-8"
         errorPage="/envoy/common/activityError.jsp"
         import="java.util.*,
                  com.globalsight.everest.webapp.WebAppConstants,
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
    ArrayList<GlobalSightLocale> targetLocales = 
        (ArrayList<GlobalSightLocale>) request.getAttribute(ReportConstants.TARGETLOCALE_LIST);

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
<!-- This JSP is: /envoy/administration/reports/TranslationVerificationReportWebForm.jsp-->
<head>
<title><%=bundle.getString("translation_verification_report_web_form")%></title>
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
    		url : '${self.pageURL}&activityName=xlsReportTranslationVerification&action=getReportJobInfo',
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
		jobIDArr = jobIDText.split(",");
		if(jobIDArr.length>1)
		{
			alert('<%=bundle.getString("lb_invalid_jobid_one")%>');
        	return;
		}
		if(!isNumeric(jobIDText)){
			alert('<%=bundle.getString("msg_invalid_jobId")%>');
			return;
		}
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
    		url : '${self.pageURL}&activityName=xlsReportTranslationVerification&action=getReportJobInfo',
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

    $(reportJobInfo).each(function(i, item) {
    	var varItem = new Option(item.jobName, item.jobId);
        varItem.setAttribute("title", item.jobName);
        lisaQAForm.jobNameList.options.add(varItem);
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

function filterTargetLocale()
{
	var jobID = lisaQAForm.jobNameList.value;
	if(!isNumeric(jobID)){
		alert('<%=bundle.getString("msg_invalid_jobName")%>');
		return;
	}
	$("#targetLocalesList").find("option").remove();
	var url ="${self.pageURL}&action=ajaxTERS";
	$.getJSON(url,{jobId:jobID},function(data){
		$(data).each(function(i, item){
			var sel = document.getElementById("targetLocalesList");
			var option = new Option(item.targetLocName, item.targetLocId);
			sel.options.add(option); 
		});
	});
}

function setDisableTRWrapper(trid)
{
	if(trid == "idTRJobIds")
    {
        setDisableTR("idTRJobIds", true);
        setDisableTR("idTRJobNames", false);
        filterJob();
    }
    else if(trid == "idTRJobNames")
    {
    	lisaQAForm.submitButton.disabled=false;
        setDisableTR("idTRJobIds", false);
        setDisableTR("idTRJobNames", true);
    }
}
</script>
<%@ include file="/envoy/common/shortcutIcon.jspIncl" %>
</head>
<BODY leftmargin="0" rightrmargin="0" topmargin="0" marginwidth="0" marginheight="0" bgcolor="LIGHTGREY" onLoad="doOnload()">
<TABLE WIDTH="100%" BGCOLOR="WHITE">
    <TR><TD ALIGN="CENTER"><IMG SRC="/globalsight/images/logo_header.gif"></TD></TR>
</TABLE><BR>

<span class="mainHeading"><B><%=bundle.getString("translation_verification_report_web_form")%></B></span>
<BR><BR>

<TABLE WIDTH="80%">
    <TR><TD><SPAN CLASS="smallText"><%=bundle.getString("optionally_select_a_job")%></SPAN></TD></TR>
</TABLE>

<form name="lisaQAForm" method="post" action="<%=formAction%>">
<input type="hidden" name="<%=ReportConstants.REPORT_TYPE%>" value="<%=ReportConstants.TRANSLATION_VERIFICATION_REPORT%>">
<input type="hidden" id="inputJobIDS" name="inputJobIDS">

<table border="0" cellspacing="2" cellpadding="2" class="standardText">
    <tr>
   	<td class="standardText"><%=bundle.getString("lb_report_on")%></td>
    	  <td class="standardText" VALIGN="BOTTOM">
            <table cellspacing=0>
                <tr id="idTRJobIds">
                    <td><input type="radio" id="reportOnJobId" name="reportOn" checked onclick="setDisableTRWrapper('idTRJobNames');" value="jobIds"/><%=bundle.getString("lb_job_id")%></td>
                    <td><input type="text" id="jobIds" name="jobIds" value=""></td>
                </tr>
                 <tr>
                 <td/><td/> 
                </tr>
                <tr>
                <td/><td/>
                </tr>
                <tr id="idTRJobNames">
                    <td><input type="radio" id="reportOnJobName" name="reportOn" onclick="setDisableTRWrapper('idTRJobIds');" value="jobNames"/><%=bundle.getString("lb_job_name")%>:</td>
                    <td class="standardText" VALIGN="BOTTOM">
                    <select id="jobNameList" name="jobNameList" style="width:300px;" onChange="filterTargetLocale()">
                	</select>
                    </td>
                </tr>
            </table>
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
            <select id="targetLocalesList" name="targetLocalesList">
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