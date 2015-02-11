<%@ page contentType="text/html; charset=UTF-8"
         errorPage="/envoy/common/activityError.jsp"
         import="java.util.*,
                 com.globalsight.everest.webapp.pagehandler.administration.reports.ReportConstants,
                 com.globalsight.everest.webapp.pagehandler.administration.reports.ReportJobInfo,
                 com.globalsight.everest.webapp.pagehandler.PageHandler,
                 com.globalsight.everest.webapp.WebAppConstants,
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

    List<ReportJobInfo> jobList = (ArrayList<ReportJobInfo>)
        request.getAttribute(ReportConstants.REPORTJOBINFO_LIST);
    List<GlobalSightLocale> targetLocales = (ArrayList<GlobalSightLocale>)
        request.getAttribute(ReportConstants.TARGETLOCALE_LIST);

   	ResourceBundle bundle = PageHandler.getBundle(session);
%>
<html>
<!-- This JSP is: envoy\administration\reports\ImplementedCommentsCheckReportWebForm.jsp-->
<head>
<title><%=bundle.getString("implemented_comments_check_report_web_form")%></title>
<script language="javascript">
function submitForm()
{
  var jobIdOption = document.getElementById("jobId");
  var jobId = jobIdOption.options[jobIdOption.selectedIndex].value;
  var languageOption = document.getElementById("targetLang");//document.all.targetLang;
  var localeId = languageOption.options[languageOption.selectedIndex].value;
  if(localeId == 0 || jobId == 0)
  {
      alert("<%=bundle.getString("msg_report_select_name_language")%>");
  }
  else
  {
      ImplementedChkForm.submit();
  }
}
</script>
</head>
<body leftmargin="0" rightrmargin="0" topmargin="0" marginwidth="0" marginheight="0" bgcolor="LIGHTGREY">
<TABLE WIDTH="100%" BGCOLOR="WHITE">
<TR><TD ALIGN="CENTER"><IMG SRC="/globalsight/images/logo_header.gif"></TD></TR>
</TABLE><BR>
<span class="mainHeading"><B><%=bundle.getString("implemented_comments_check_web_form")%></B></span>
<BR><BR>
<TABLE WIDTH="80%">
    <TR><TD><SPAN CLASS="smallText"><%=bundle.getString("optionally_select_a_job")%></SPAN></TD></TR>
</TABLE>

<form name="ImplementedChkForm" method="post" action="/globalsight/envoy/administration/reports/ImplementedCommentsCheckReport.jsp">

<table border="0" cellspacing="2" cellpadding="2" class="standardText">
    <tr>
        <td class="standardText"><%=bundle.getString("lb_job_name")%>:</td>
        <td class="standardText" VALIGN="BOTTOM">
        <select name="jobId" id="jobId" style="width:300px">
<%  
        if (jobList == null || jobList.size() == 0)
    	{
%>          <option VALUE="*"><%=bundle.getString("no_job")%></option>
<%      }
        else
        {
            for (ReportJobInfo job : jobList)
            {
%>          <option title="<%=job.getJobName()%>" VALUE="<%=job.getJobId()%>"><%=job.getJobName()%></option>
<%          }
        }
%>
        </select>
        </td>
    </tr>

    <tr>
        <td class="standardText"><%=bundle.getString("lb_target_locales")%>:</td>
        <td class="standardText" VALIGN="BOTTOM">
        <select name="targetLang" id="targetLang">
<%
        if (jobList == null || jobList.size() == 0)
	    {
%>           <option VALUE="*"><%=bundle.getString("no_job")%></option>
<%      }
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
<%      }
%>
        </select>
        </td>
    </tr>
    <tr>
        <td><input type="button" VALUE="<%=bundle.getString("lb_shutdownSubmit")%>" onClick="submitForm()"></td>
        <TD><INPUT type="BUTTON" VALUE="<%=bundle.getString("lb_cancel")%>" onClick="window.close()"></TD>
    </tr>
</table>
</form>
<BODY>
</HTML>