<%@ page contentType="text/html; charset=UTF-8"
         errorPage="/envoy/common/activityError.jsp"
         import="java.util.*,
                  com.globalsight.everest.webapp.WebAppConstants,
                  com.globalsight.everest.webapp.pagehandler.administration.reports.ReportConstants,
                  com.globalsight.everest.webapp.pagehandler.administration.reports.ReportJobInfo,
                  com.globalsight.everest.webapp.pagehandler.PageHandler,
                  com.globalsight.everest.jobhandler.Job,
                  com.globalsight.util.GlobalSightLocale,
                  java.util.Locale,
                  java.util.ResourceBundle"
          session="true"
%>
<%
    List<ReportJobInfo> reportJobInfoList = 
        (List<ReportJobInfo>) request.getAttribute(ReportConstants.REPORTJOBINFO_LIST);
    ArrayList<GlobalSightLocale> targetLocales = 
        (ArrayList<GlobalSightLocale>) request.getAttribute(ReportConstants.TARGETLOCALE_LIST);

    Locale uiLocale = (Locale) session.getAttribute(WebAppConstants.UILOCALE);
    if (uiLocale == null)
    {
    	uiLocale = Locale.US;
    }

   	ResourceBundle bundle = PageHandler.getBundle(session);
   	String formAction = "/globalsight/ControlServlet?linkName=generateReports&pageName=JOBREPORTS"
        + "&action=" + ReportConstants.GENERATE_REPORTS;
%>
<html>
<!-- This JSP is: /envoy/administration/reports/TranslationVerificationReportWebForm.jsp-->
<head>
<title><%=bundle.getString("translation_verification_report_web_form")%></title>
<script type="text/javascript" src="/globalsight/envoy/administration/reports/report.js"></script>
<script type="text/javascript" src="/globalsight/jquery/jquery-1.6.4.min.js"></script>
<SCRIPT SRC="/globalsight/includes/library.js"></SCRIPT>
<script type="text/javascript">
function doSubmit()
{
	if(document.getElementsByName("reportOn")[0].checked)
	{
		var jobID = lisaQAForm.jobId.value;
		var jobIDArr = jobID.split(",");
		if(jobIDArr.length>1)
		{
			alert('<%=bundle.getString("lb_invalid_jobid")%>');
        	return;
		}
        var idInput=$("#jobName").find("option");
		var idArray=new Array();
		idInput.each(function()
		{
			idArray.push({"jobId":$(this).val()});
		});
			
	    if(!validateIDS(jobIDArr, idArray))
	        {
	        	alert('<%=bundle.getString("lb_invalid_jobid")%>');
	        	return;
	        } 
	}
	else
	{
		var jobID = lisaQAForm.jobName.value;
		if(jobID == "*")
		{
			alert('<%=bundle.getString("msg_invalid_jobName")%>');
			return;
		}
	}
	
	document.getElementById("inputJobIDS").value = jobID;
	lisaQAForm.submit();
}

function doOnload()
{
	// Set the jobIds as default check. 
	setDisableTRWrapper("idTRJobName");
}

function setDisableTRWrapper(trid)
{
	if(trid == "idTRJobId")
	{
		setDisableTR("idTRJobId", true);
		setDisableTR("idTRJobName", false);
	}
	else if(trid == "idTRJobName")
	{
		setDisableTR("idTRJobId", false);
		setDisableTR("idTRJobName", true);
	}
}

function filterTargetLocale()
{
	if(document.getElementsByName("reportOn")[0].checked)
	{
		var jobID = lisaQAForm.jobId.value;
		if(!isNumeric(jobID)){
			alert('<%=bundle.getString("lb_invalid_jobid")%>');
			return;
		}
	}else{
		var jobID = lisaQAForm.jobName.value;
		if(!isNumeric(jobID)){
			alert('<%=bundle.getString("msg_invalid_jobName")%>');
			return;
		}
	}
	$("#targetLocalesList").find("option").remove();
	var url ="${self.pageURL}&action=ajax"
	$.getJSON(url,{jobId:jobID},function(data){
		$(data).each(function(i, item){
			var sel = document.getElementById("targetLocalesList");
			var option = new Option(item.targetLocName, item.targetLocId);
			sel.options.add(option);
		});
	});
}

function isNumeric(str){
	if (str.startsWith("0"))
		return false;
	return /^(-|\+)?\d+(\.\d+)?$/.test(str);
}
</script>
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
                <tr id="idTRJobId">
                    <td><input type="radio" name="reportOn" checked onclick="setDisableTRWrapper('idTRJobName');" value="jobId"/><%=bundle.getString("lb_job_id")%></td>
                    <td><input type="text" id="jobId" name="jobId" value=""></td>
                </tr>
                 <tr>
                 <td/><td/> 
                </tr>
                <tr>
                <td/><td/>
                </tr>
                <tr id="idTRJobName">
                    <td><input type="radio" name="reportOn" onclick="setDisableTRWrapper('idTRJobId');" value="jobName"/><%=bundle.getString("lb_job_name")%>:</td>
                    <td class="standardText" VALIGN="BOTTOM">
            <select id="jobName" name="jobName" style="width:300px">
<%
          	if (reportJobInfoList == null || reportJobInfoList.size() == 0)
            {
%>              <option VALUE="*"><%=bundle.getString("no_job")%></option>
<%          }
            else
            {
                for (ReportJobInfo j : reportJobInfoList)
                {
%>
                <option title="<%=j.getJobName()%>" VALUE="<%=j.getJobId()%>"><%=j.getJobName()%></option>
<%
                }
           }
%>
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
            <select name="targetLocalesList" id="targetLocalesList">
<%
            if (reportJobInfoList == null || reportJobInfoList.size() == 0)
            {
%>
                <option VALUE="*"><%=bundle.getString("no_job")%></option>
<%
            }
            else
            {
                for (GlobalSightLocale gsl : targetLocales)
                {
%>              <option VALUE="<%=gsl.getId()%>"><%=gsl.getDisplayName(uiLocale)%></option><%
                }
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
        <TD><INPUT type="BUTTON" VALUE="<%=bundle.getString("lb_shutdownSubmit")%>" onClick="doSubmit();"></TD>
        <TD><INPUT type="BUTTON" VALUE="<%=bundle.getString("lb_cancel")%>" onClick="window.close()"></TD>
    </tr>
</table>
</form>
</BODY>
</HTML>