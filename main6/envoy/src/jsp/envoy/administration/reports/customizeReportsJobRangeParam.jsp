<%@ page contentType="text/html; charset=UTF-8"
         errorPage="/envoy/common/activityError.jsp"
         import="java.util.*, com.globalsight.everest.servlet.util.SessionManager,
                  com.globalsight.everest.webapp.WebAppConstants,
                  com.globalsight.everest.webapp.javabean.NavigationBean,
                  com.globalsight.everest.webapp.pagehandler.PageHandler,
                  com.globalsight.everest.webapp.pagehandler.projects.workflows.JobSearchConstants,
                  com.globalsight.everest.foundation.SearchCriteriaParameters,
                  com.globalsight.everest.projecthandler.Project,
                  com.globalsight.everest.jobhandler.Job,
                  com.globalsight.util.GlobalSightLocale,
                  java.text.MessageFormat,
                  java.util.ResourceBundle,
                  java.util.List"
          session="true"
%>
<%
    ResourceBundle bundle = PageHandler.getBundle(session);
	Locale uiLocale = (Locale)session.getAttribute(WebAppConstants.UILOCALE);
    if (uiLocale == null)
    {
    	uiLocale = Locale.ENGLISH;
    }
    // Field names
    String creationStart = JobSearchConstants.CREATION_START;
    String creationStartOptions = JobSearchConstants.CREATION_START_OPTIONS;
    String creationEnd = JobSearchConstants.CREATION_END;
    String creationEndOptions = JobSearchConstants.CREATION_END_OPTIONS;
%>

<jsp:useBean id="next" scope="request" class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="jobList" scope="request" class="java.util.ArrayList" />
<jsp:useBean id="projectList" scope="request" class="java.util.ArrayList" />
<jsp:useBean id="targetLocaleList" scope="request" class="java.util.ArrayList" />

<%
    String nextUrl = next.getPageURL() + "&action=" + WebAppConstants.ACTION_JOB_RANGE;
%>
<html>
<head>
<title><%=bundle.getString("lb_customize_reports_params_title")%></title>
</head>
<body leftmargin="0" rightrmargin="0" topmargin="0" marginwidth="0" marginheight="0"
bgcolor="LIGHTGREY">
<SCRIPT LANGUAGE="JAVASCRIPT">
// If user selected "now", then blank out the preceeding numeric field.
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

function validateForm()
{
    if ((-1 != searchForm.<%=creationStartOptions%>.value) &&
        (searchForm.<%=creationStart%>.value == ""))
        return ('<%=bundle.getString("jsmsg_job_search_bad_date")%>');
    if ((-1 != searchForm.<%=creationEndOptions%>.value) &&
    	("<%=SearchCriteriaParameters.NOW%>" != searchForm.<%=creationEndOptions%>.value) &&
        (searchForm.<%=creationEnd%>.value == ""))
        return ('<%=bundle.getString("jsmsg_job_search_bad_date")%>');
    if (!isInteger(searchForm.<%=creationStart%>.value))
        return ('<%=bundle.getString("jsmsg_job_search_bad_date")%>');
    if (!isInteger(searchForm.<%=creationEnd%>.value))
        return ('<%=bundle.getString("jsmsg_job_search_bad_date")%>');
    return "";
}

function submitForm()
{
   var msg = validateForm();
   if (msg != "")
   {
    alert(msg);
    return;
   }
   else
   {
    searchForm.action="<%=nextUrl%>";
    searchForm.submit();
   }
}

</script>

<TABLE WIDTH="100%" BGCOLOR="WHITE">
<TR><TD ALIGN="CENTER"><IMG SRC="/globalsight/images/logo_header.gif"></TD></TR>
</TABLE><BR>
<span class="mainHeading"><B><%=bundle.getString("lb_customize_reports")%></B></span>
<BR><BR>
<TABLE WIDTH="80%">
<TR><TD>
<SPAN CLASS="smallText"><%=bundle.getString("lb_customize_reports_params_tip1")%></SPAN>
</TD></TR></TABLE>

<form name="searchForm" method="post">

<table border="0" cellspacing="2" cellpadding="2" class="standardText">
<tr>
<td class="standardText"><%=bundle.getString("lb_job_name")%>:</td>
<td class="standardText" VALIGN="BOTTOM">
<select name="jobId" MULTIPLE size="6" style="width:300px">
<option value="*" SELECTED><B>&lt;<%=bundle.getString("lb_all")%>&gt;</B></OPTION>
<%  
    Job job = null;
    for (Iterator iter = jobList.iterator(); iter.hasNext();)
    {
        job = (Job) iter.next();
%>
<option title="<%=job.getJobName()%>" VALUE="<%=job.getJobId()%>"><%=job.getJobName()%></OPTION>
<%
    }
%>
</select>
</td>
</tr>

<tr>
<td class="standardText"><%=bundle.getString("lb_project")%>:</td>
<td class="standardText" VALIGN="BOTTOM">
<select name="projectId" MULTIPLE size=4>
<option VALUE="*" SELECTED>&lt;<%=bundle.getString("lb_all")%>&gt;</OPTION>
<%
    Project project = null;
    for (Iterator iter = projectList.iterator(); iter.hasNext();)
    {
        project = (Project) iter.next();
%>
<option VALUE="<%=project.getId()%>"><%=project.getName()%></OPTION>
<%
    }
%>
</select>
</td>
</tr>

<tr>
<td class="standardText">
<%=bundle.getString("lb_status")%><span class="asterisk">*</span>:
</td>
<td class="standardText" VALIGN="BOTTOM">
<select name="status" MULTIPLE size=4>
<option value="*" SELECTED>&lt;<%=bundle.getString("lb_all")%>&gt;</OPTION>
<option value="<%=Job.READY_TO_BE_DISPATCHED%>"><%= bundle.getString("lb_ready") %></option>
<option value="<%=Job.DISPATCHED%>"><%= bundle.getString("lb_inprogress") %></option>
<option value="<%=Job.LOCALIZED%>"><%= bundle.getString("lb_localized") %></option>
<option value="<%=Job.EXPORTED%>"><%= bundle.getString("lb_exported") %></option>
<option value="<%=Job.ARCHIVED%>"><%= bundle.getString("lb_archived") %></option>
</select>
</td>
</tr>

<tr>
<td class="standardText">
<%=bundle.getString("lb_target_locales")%><span class="asterisk">*</span>:
</td>
<td class="standardText" VALIGN="BOTTOM">
<select name="targetLocale" MULTIPLE size=4>
<option VALUE="*" SELECTED>&lt;<%=bundle.getString("lb_all")%>&gt;</OPTION>
<%
    GlobalSightLocale targetLocale = null;
    for (Iterator iter = targetLocaleList.iterator(); iter.hasNext();)
    {
        targetLocale = (GlobalSightLocale) iter.next();
%>
<option VALUE="<%=targetLocale.toString()%>"><%=targetLocale.getDisplayName(uiLocale)%></OPTION>
<%
    }
%>
</select>
</td>
</tr>

<tr>
<td class="standardText" colspan=2>
<%=bundle.getString("lb_creation_date_range")%>:
</td>
</tr>
<tr>
<td class="standardText" style="padding-left:70px" colspan=2 VALIGN="BOTTOM">
<%=bundle.getString("lb_starts")%>:
<input type="text" name="<%=creationStart%>" size="3" maxlength="9">
<select name="<%=creationStartOptions%>">
<option value='-1'></option>
<option value='<%=SearchCriteriaParameters.HOURS_AGO%>'><%=bundle.getString("lb_hours_ago")%></option>
<option value='<%=SearchCriteriaParameters.DAYS_AGO%>'><%=bundle.getString("lb_days_ago")%></option>
<option value='<%=SearchCriteriaParameters.WEEKS_AGO%>'><%=bundle.getString("lb_weeks_ago")%></option>
<option value='<%=SearchCriteriaParameters.MONTHS_AGO%>'><%=bundle.getString("lb_months_ago")%></option>
</select>
<%=bundle.getString("lb_ends")%>:
<input type="text" name="<%=creationEnd%>" size="3" maxlength="9">
<select name="<%=creationEndOptions%>" onChange="checkNow(this, searchForm.<%=creationEnd%>)">
<option value='-1'></option>
<option value='<%=SearchCriteriaParameters.NOW%>'><%=bundle.getString("lb_now")%></option>
<option value='<%=SearchCriteriaParameters.HOURS_AGO%>'><%=bundle.getString("lb_hours_ago")%></option>
<option value='<%=SearchCriteriaParameters.DAYS_AGO%>'><%=bundle.getString("lb_days_ago")%></option>
<option value='<%=SearchCriteriaParameters.WEEKS_AGO%>'><%=bundle.getString("lb_weeks_ago")%></option>
<option value='<%=SearchCriteriaParameters.MONTHS_AGO%>'><%=bundle.getString("lb_months_ago")%></option>
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
<td><input type="BUTTON" VALUE="<%=bundle.getString("lb_cancel")%>" onClick="window.close()"></td>
<TD><INPUT type="BUTTON" VALUE=<%=bundle.getString("lb_next")%> onClick="submitForm()"></TD>
</tr>
</table>
</form>
</body>
</html>

