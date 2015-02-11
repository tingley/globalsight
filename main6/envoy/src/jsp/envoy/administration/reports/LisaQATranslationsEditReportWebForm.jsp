<%@ page contentType="text/html; charset=UTF-8"
         errorPage="/envoy/common/activityError.jsp"
         import="java.util.*, com.globalsight.everest.servlet.util.SessionManager,
                  com.globalsight.everest.webapp.WebAppConstants,
                  com.globalsight.everest.workflowmanager.Workflow,
                  com.globalsight.everest.webapp.javabean.NavigationBean,
                  com.globalsight.everest.webapp.pagehandler.administration.reports.ReportConstants,
                  com.globalsight.everest.webapp.pagehandler.PageHandler,
                  com.globalsight.everest.util.comparator.JobComparator,
                  com.globalsight.everest.util.comparator.LocaleComparator,
                  com.globalsight.everest.jobhandler.Job,
                  com.globalsight.everest.servlet.util.ServerProxy,
                  com.globalsight.util.GlobalSightLocale,
                  java.util.ResourceBundle,
                  com.globalsight.everest.webapp.pagehandler.PageHandler,
                  java.util.Locale,
                  java.util.ResourceBundle"
          session="true"
%>
<%
    SessionManager sessionMgr = (SessionManager)session.getAttribute(WebAppConstants.SESSION_MANAGER);
    Locale uiLocale = (Locale)session.getAttribute(WebAppConstants.UILOCALE);
    if (uiLocale == null)
    {
    	uiLocale = Locale.ENGLISH;
    }
    
    Vector stateList = new Vector();
    stateList.add(Job.DISPATCHED);
    stateList.add(Job.LOCALIZED);
    stateList.add(Job.EXPORTED);
    stateList.add(Job.READY_TO_BE_DISPATCHED);
    stateList.add(Job.EXPORT_FAIL);
    stateList.add(Job.ARCHIVED);

    // get jobs by a state list
    Collection jobs = ServerProxy.getJobHandler().getJobsByStateList(stateList);
   	List jobList = null;
   	if (jobs != null && !jobs.isEmpty())
   	{
	   	jobList = new ArrayList(jobs);
	    Collections.sort(jobList, new JobComparator(JobComparator.NAME,uiLocale));
    }
   	ResourceBundle bundle = PageHandler.getBundle(session);
   	String formAction = "/globalsight/ControlServlet?linkName=generateReports&pageName=JOBREPORTS"
        + "&action=" + ReportConstants.GENERATE_REPORTS;
%>
<html>
<!-- This JSP is: /envoy/administration/reports/LisaQATranslationsEditReportWebForm.jsp-->
<head>
<title><%=bundle.getString("language_web_form")%></title>
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
<BODY leftmargin="0" rightrmargin="0" topmargin="0" marginwidth="0" marginheight="0"
bgcolor="LIGHTGREY">
<TABLE WIDTH="100%" BGCOLOR="WHITE">
<TR><TD ALIGN="CENTER"><IMG SRC="/globalsight/images/logo_header.gif"></TD></TR>
</TABLE><BR>
<span class="mainHeading"><B><%=bundle.getString("translations_web_form")%></B></span>
<BR><BR>
<TABLE WIDTH="80%">
<TR><TD>
<SPAN CLASS="smallText">
<%=bundle.getString("optionally_select_a_job")%></SPAN>
</TD></TR></TABLE>

<form name="lisaQAForm" method="post" action="<%=formAction%>">
<input type="hidden" name="<%=ReportConstants.REPORT_TYPE%>" value="<%=ReportConstants.TRANSLATIONS_EDIT_REPORT%>">
<input type="hidden" id="inputJobIDS" name="inputJobIDS">

<table border="0" cellspacing="2" cellpadding="2" class="standardText">
<tr>
<td class="standardText"><%=bundle.getString("lb_job_name")%>:</td>
<td class="standardText" VALIGN="BOTTOM">
<select name="jobId" style="width:300px">
<%  
	if (jobList == null)
	{
%>
	<option VALUE="*"><%=bundle.getString("no_job")%></option>
<% 	
	}
	else
	{
		Iterator iter = jobList.iterator();
        while (iter.hasNext())
        {
            Job j = (Job) iter.next();
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
	if (jobList == null)
	{
%>
	<option VALUE="*"><%=bundle.getString("no_job")%></option>
<%
	}
	else
	{
		Vector targetLocales = ServerProxy.getLocaleManager().getAllTargetLocales();
		int sortColumn = 1;
        LocaleComparator localeComparator = new LocaleComparator(sortColumn, uiLocale);
        Collections.sort(targetLocales, localeComparator);
        Iterator it = targetLocales.iterator();
		while (it.hasNext())
		{
		 	GlobalSightLocale gsl = (GlobalSightLocale) it.next();
            %><option VALUE="<%=gsl.getId()%>"><%=gsl.getDisplayName(uiLocale)%></option><%
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
<%}%>
</select>
</td>
</tr>
<tr>
<TD><INPUT type="BUTTON" VALUE="<%=bundle.getString("lb_shutdownSubmit")%>" onClick="doSubmit();"></TD>
<TD><INPUT type="BUTTON" VALUE="<%=bundle.getString("lb_cancel")%>" onClick="window.close()"></TD>
</tr>
</table>
</form>
</BODY>
</HTML>