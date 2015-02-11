<%@ page contentType="text/html; charset=UTF-8"
         errorPage="/envoy/common/activityError.jsp"
         import="java.util.*, com.globalsight.everest.servlet.util.SessionManager,
                  com.globalsight.everest.webapp.WebAppConstants,
                  com.globalsight.everest.webapp.javabean.NavigationBean,
                  com.globalsight.everest.webapp.pagehandler.PageHandler,
                  com.globalsight.everest.webapp.pagehandler.administration.users.UserHandlerHelper,
                  com.globalsight.everest.webapp.pagehandler.projects.workflows.JobSearchConstants,
                  com.globalsight.util.resourcebundle.ResourceBundleConstants,
                  com.globalsight.util.resourcebundle.SystemResourceBundle,
                  com.globalsight.everest.foundation.SearchCriteriaParameters,
                  com.globalsight.everest.webapp.pagehandler.administration.vendors.ProjectComparator,
                  com.globalsight.everest.foundation.User,
                  com.globalsight.everest.projecthandler.Project,                  
                  com.globalsight.everest.util.comparator.JobComparator,
                  com.globalsight.everest.jobhandler.Job,
                  com.globalsight.everest.jobhandler.JobSearchParameters,
                  com.globalsight.everest.projecthandler.ProjectInfo,
                  com.globalsight.everest.webapp.webnavigation.LinkHelper,
                  com.globalsight.everest.servlet.util.ServerProxy,
                  com.globalsight.everest.servlet.EnvoyServletException,
                  com.globalsight.everest.util.comparator.GlobalSightLocaleComparator,
                  com.globalsight.everest.util.system.SystemConfigParamNames,
                  com.globalsight.everest.util.system.SystemConfiguration,
                  com.globalsight.util.GeneralException,
                  com.globalsight.util.GlobalSightLocale,
                  com.globalsight.everest.company.CompanyWrapper,
                  java.text.MessageFormat,
                  java.util.Locale,
                  java.util.ResourceBundle,
                  java.util.List,
                  com.globalsight.everest.company.CompanyThreadLocal,
                  com.globalsight.everest.webapp.pagehandler.administration.users.UserUtil,
                  com.globalsight.everest.usermgr.UserLdapHelper,
                  com.globalsight.everest.webapp.WebAppConstants"
          session="true"
%>
<%  
	String EMEA = CompanyWrapper.getCurrentCompanyName();
	
    ResourceBundle bundle = PageHandler.getBundle(session);
    SessionManager sessionMgr = (SessionManager)session.getAttribute(WebAppConstants.SESSION_MANAGER);
    Locale uiLocale = (Locale)session.getAttribute(WebAppConstants.UILOCALE);
    String userName = (String)session.getAttribute(WebAppConstants.USER_NAME);
    
    // Field names
    String nameField = JobSearchConstants.NAME_FIELD;
    String nameOptions = JobSearchConstants.NAME_OPTIONS;
    String idField = JobSearchConstants.ID_FIELD;
    String idOptions = JobSearchConstants.ID_OPTIONS;
    String statusOptions = JobSearchConstants.STATUS_OPTIONS;
    String projectOptions = JobSearchConstants.PROJECT_OPTIONS;
    String srcLocale = JobSearchConstants.SRC_LOCALE;
    String targLocale = JobSearchConstants.TARG_LOCALE;
    String priorityOptions = JobSearchConstants.PRIORITY_OPTIONS;
    String creationStart = JobSearchConstants.CREATION_START;
    String creationStartOptions = JobSearchConstants.CREATION_START_OPTIONS;
    String creationEnd = JobSearchConstants.CREATION_END;
    String creationEndOptions = JobSearchConstants.CREATION_END_OPTIONS;
    String completionStart = JobSearchConstants.EST_COMPLETION_START;
    String completionStartOptions = JobSearchConstants.EST_COMPLETION_START_OPTIONS;
    String completionEnd = JobSearchConstants.EST_COMPLETION_END;
    String completionEndOptions = JobSearchConstants.EST_COMPLETION_END_OPTIONS;
%>
<html>
<!-- This JSP is: /envoy/administration/reports/jobStatusXlsReportWebForm.jsp-->
<head>
<title><%= EMEA%> <%=bundle.getString("word_count_report_web_form")%></title>
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
    searchForm.submit();
}

</script>
<TABLE WIDTH="100%" BGCOLOR="WHITE">
<TR><TD ALIGN="CENTER"><IMG SRC="/globalsight/images/logo_header.gif"></TD></TR>
</TABLE><BR>
<span class="mainHeading"><B><%=bundle.getString("word_count_report_web_form")%></B></span>
<BR><BR>
<TABLE WIDTH="80%">
<TR><TD>
<SPAN CLASS="smallText">
<%=bundle.getString("select_the_appropriate_job")%>
<%=bundle.getString("report_is_deprecated")%>
</SPAN>
</TD></TR></TABLE>

<form name="searchForm" method="post" action="/globalsight/envoy/administration/reports/wordCountReport.jsp">

<table border="0" cellspacing="2" cellpadding="2" class="standardText">
<tr>
<td class="standardText"><%=bundle.getString("lb_job_name")%>:</td>
<td class="standardText" VALIGN="BOTTOM">
<select name="jobId" MULTIPLE size="6" style="width:300px">
<option value="*" SELECTED><B>&lt;<%=bundle.getString("all")%>&gt;</B></OPTION>
<%
         Vector stateList = new Vector();
         stateList.add(Job.READY_TO_BE_DISPATCHED);
         stateList.add(Job.DISPATCHED);
         stateList.add(Job.LOCALIZED);
         stateList.add(Job.EXPORTED);
         stateList.add(Job.ARCHIVED);
         stateList.add(Job.EXPORT_FAIL);
         Collection jobs = ServerProxy.getJobHandler().getJobsByStateList(stateList);
         ArrayList jobList = new ArrayList(jobs);
         Collections.sort(jobList, new JobComparator(JobComparator.NAME,uiLocale));
         Iterator iter = jobList.iterator();
         ArrayList projects = new ArrayList();
         while (iter.hasNext())
         {
             Job j = (Job) iter.next();
             Project p = j.getL10nProfile().getProject();
             if (projects.contains(p)==false)
                 projects.add(p);
%>
<option title="<%=j.getJobName()%>" VALUE="<%=j.getJobId()%>"><%=j.getJobName()%></OPTION>
<%
         }
%>
</select>
</td>
</tr>



<tr>
<td class="standardText"><%=bundle.getString("lb_target_locales")%>*:</td>
<td class="standardText" VALIGN="BOTTOM">
<select name="targetLocalesList" multiple="true" size="4" style="width:200px">
<option value="*" selected>&lt;<%=bundle.getString("all")%>&gt;</OPTION>
<%
         ArrayList targetLocales = new ArrayList( ServerProxy.getLocaleManager().getAllTargetLocales() );
         Collections.sort(targetLocales, new GlobalSightLocaleComparator(Locale.getDefault()));
         for( int i=0; i < targetLocales.size(); i++)
         {
             GlobalSightLocale gsLocale = (GlobalSightLocale) targetLocales.get(i);
%>
<option VALUE="<%=gsLocale.toString()%>"><%=gsLocale.getDisplayName(uiLocale)%></OPTION>
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
<td><input type="BUTTON" VALUE="<%=bundle.getString("lb_shutdownSubmit")%>" onClick="submitForm()"></td>
<TD><INPUT type="BUTTON" VALUE="<%=bundle.getString("lb_cancel")%>" onClick="window.close()"></TD>
</tr>
</table>
</form>

<BODY>
</HTML>

