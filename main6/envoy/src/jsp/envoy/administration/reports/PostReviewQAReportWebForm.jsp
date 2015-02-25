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
<!-- This JSP is: /envoy/administration/reports/LisaPostReviewQAReportWebForm.jsp-->
<head>
<title><%=bundle.getString("post_review_qa_report")%></title>
<script type="text/javascript">
function doSubmit()
{
	var jobID = lisaQAForm.jobId.value;
	if(jobID == "*")
	{
		alert('<%=bundle.getString("msg_invalid_jobName")%>');
		return;
	}
	
	document.getElementById("inputJobIDS").value = jobID;
	lisaQAForm.submit();
}
</script>
</head>
<BODY leftmargin="0" rightrmargin="0" topmargin="0" marginwidth="0" marginheight="0" bgcolor="LIGHTGREY">
<TABLE WIDTH="100%" BGCOLOR="WHITE">
    <TR><TD ALIGN="CENTER"><IMG SRC="/globalsight/images/logo_header.gif"></TD></TR>
</TABLE><BR>

<span class="mainHeading"><B><%=bundle.getString("post_review_qa_report")%></B></span>
<BR><BR>

<TABLE WIDTH="80%">
    <TR><TD><SPAN CLASS="smallText"><%=bundle.getString("optionally_select_a_job")%></SPAN></TD></TR>
</TABLE>

<form name="lisaQAForm" method="post" action="<%=formAction%>">
<input type="hidden" name="<%=ReportConstants.REPORT_TYPE%>" value="<%=ReportConstants.POST_REVIEW_QA_REPORT%>">
<input type="hidden" id="inputJobIDS" name="inputJobIDS">

<table border="0" cellspacing="2" cellpadding="2" class="standardText">
    <tr>
        <td class="standardText"><%=bundle.getString("lb_job_name")%>:</td>
        <td class="standardText" VALIGN="BOTTOM">
            <select name="jobId" style="width:300px">
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

    <tr>
        <td class="standardText"><%=bundle.getString("lb_target_language")%>:</td>
        <td class="standardText" VALIGN="BOTTOM">
            <select name="targetLocalesList">
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
	<!--
    <tr>
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
<%          }
%>          </select>
        </td>
    </tr>
    -->
    <tr>
        <TD><INPUT type="BUTTON" VALUE="<%=bundle.getString("lb_shutdownSubmit")%>" onClick="doSubmit();"></TD>
        <TD><INPUT type="BUTTON" VALUE="<%=bundle.getString("lb_cancel")%>" onClick="window.close()"></TD>
    </tr>
</table>
</form>
</BODY>
</HTML>