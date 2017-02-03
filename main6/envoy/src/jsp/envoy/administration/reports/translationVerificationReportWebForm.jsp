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
var alertInfo;

function validateForm()
{
    if(lisaQAForm.reportOnJobId.checked)
    {
        var jobIDArr =  lisaQAForm.jobIds.value.split(",");

		if(!validateIDS(jobIDArr, null))
        {
        	$("#jobNameList").attr("selected", true);
           return ('<%=bundle.getString("lb_invalid_jobid")%>');
        }
    }
    if(lisaQAForm.reportOnJobName.checked) {
        var len = $("#jobNameList").find("option:selected").length;
        if(len == 0) {
            var ops = $("#jobNameList").children();
            if(ops.length == 0) {
                return ('<%=bundle.getString("msg_invalid_jobName")%>');
            } else {
                ops.attr("selected", true);
            }
	    }
	}
    return "";
}

function doSubmit()
{
	var msg = validateForm();
    if (msg != "")
    {
        alert(msg);
        return;
    }
	var jobIDArr = fnGetSelectedJobIds();
	if(jobIDArr == null || jobIDArr.length == 0)
	{
		if(alertInfo != null)
			alert(alertInfo); 
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
	var jobIDArr = new Array();
	if(lisaQAForm.reportOnJobId.checked)
	{
		var jobIDText = document.getElementById("jobIds").value;
		jobIDText = jobIDText.replace(/(^\s*)|(\s*$)/g, "");	
		if(jobIDText.substr(0, 1) == "," || jobIDText.substr(jobIDText.length-1, jobIDText.length) == ",")
		{
			alertInfo = '<%=bundle.getString("lb_invalid_jobid")%>';
			return;
		}
		jobIDArr = jobIDText.split(",");
		if(!validateIDS(jobIDArr, null))
        {
			alertInfo = '<%=bundle.getString("lb_invalid_jobid")%>';
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
		
		if(!validateIDS(jobIDArr, null))
	    {
			alertInfo = '<%=bundle.getString("msg_invalid_jobName")%>';
			return;
	    }
	}
	jobIDArr.sort(sortNumber);
	
	return jobIDArr;
}

function sortNumber(a,b) 
{ 
	return a - b 
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

        var url ="${self.pageURL}&activityName=xlsReportTranslationVerification&action=getReportJobInfo";
        $.getJSON(url, function(data) {
			reportJobInfo = data;
			filterJob2();
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

    $(reportJobInfo).each(function(i, item) {
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
                    <td><input type="text" id="jobIds" name="jobIds" value=""><%=bundle.getString("lb_job_ids_description")%></td>
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
                    <select id="jobNameList" name="jobNameList" MULTIPLE size="6" style="width:300px;min-height:90px;" disabled>
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