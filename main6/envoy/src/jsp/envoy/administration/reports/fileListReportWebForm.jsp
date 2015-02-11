<%@ page contentType="text/html; charset=UTF-8"
         errorPage="/envoy/common/activityError.jsp"
         import="java.util.*, com.globalsight.everest.servlet.util.SessionManager,
                  com.globalsight.everest.webapp.pagehandler.PageHandler,
                  com.globalsight.everest.webapp.pagehandler.projects.workflows.JobSearchConstants,
                  com.globalsight.everest.foundation.SearchCriteriaParameters,
                  com.globalsight.everest.webapp.pagehandler.administration.vendors.ProjectComparator,
                  com.globalsight.everest.projecthandler.Project,                  
                  com.globalsight.everest.util.comparator.JobComparator,
                  com.globalsight.everest.jobhandler.Job,
                  com.globalsight.everest.servlet.util.ServerProxy,
                  com.globalsight.util.GlobalSightLocale,
                  com.globalsight.everest.company.CompanyWrapper,
                  java.util.Locale,
                  java.util.ResourceBundle,
                  com.globalsight.everest.company.CompanyThreadLocal,
                  com.globalsight.everest.webapp.pagehandler.administration.users.UserUtil,
                  com.globalsight.everest.usermgr.UserLdapHelper,
                  com.globalsight.everest.webapp.WebAppConstants"
          session="true"
%>
<%  String EMEA = CompanyWrapper.getCurrentCompanyName();
    //Multi-Company: get current user's company from the session
    HttpSession userSession = request.getSession(false);
    String companyName = (String)userSession.getAttribute(WebAppConstants.SELECTED_COMPANY_NAME_FOR_SUPER_PM);
    if (UserUtil.isBlank(companyName))
    {
        companyName = (String)userSession.getAttribute(UserLdapHelper.LDAP_ATTR_COMPANY);
    }
    if (companyName != null)
    {
        CompanyThreadLocal.getInstance().setValue(companyName);
    }    

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
<!--  This JSP is: /envoy/administration/reports/fileListReportWebForm.jsp-->
<head>
<title><%= EMEA%> <%=bundle.getString("file_list_report_web_form")%></title>
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
    if(searchForm.reportOnJobId.checked)
    {
        var patrn = /^[0-9,\s]*$/;
        if(searchForm.jobIds.value==""||!patrn.exec(searchForm.jobIds.value))
        {
           return ('<%=bundle.getString("lb_invalid_jobid")%>');
        }
    }
    return "";
}

function setDisable(reportOn)
{
	if(reportOn=="jobStatus")
	{
		searchForm.jobIds.disabled=true;
		searchForm.jobStatus.disabled=false;
		searchForm.projectId.disabled=false;
		searchForm.<%=creationStart%>.disabled=false;
		searchForm.<%=creationStartOptions%>.disabled=false;
		searchForm.<%=creationEnd%>.disabled=false;
		searchForm.<%=creationEndOptions%>.disabled=false;
	}
	else
	{
		searchForm.jobIds.disabled=false;
		searchForm.jobStatus.disabled=true;
		searchForm.projectId.disabled=true;
		searchForm.<%=creationStart%>.disabled=true;
		searchForm.<%=creationStartOptions%>.disabled=true;
		searchForm.<%=creationEnd%>.disabled=true;
		searchForm.<%=creationEndOptions%>.disabled=true;

    }
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
<span class="mainHeading"><B><%= EMEA%> <%=bundle.getString("file_list_report_web_form")%></B></span>
<BR><BR>
<TABLE WIDTH="80%">
<TR><TD>
<SPAN CLASS="smallText">
<%=bundle.getString("select_the_appropriate")%>
</SPAN>
</TD></TR></TABLE>

<form name="searchForm" method="post" action="/globalsight/envoy/administration/reports/fileListXlsReport.jsp">

<table border="0" cellspacing="2" cellpadding="2" class="standardText">
<%
         Vector stateList = new Vector();
         stateList.add(Job.DISPATCHED);
         stateList.add(Job.LOCALIZED);
         stateList.add(Job.EXPORTED);
         stateList.add(Job.PENDING);
         stateList.add(Job.EXPORT_FAIL);
         stateList.add(Job.ARCHIVED);
         stateList.add(Job.READY_TO_BE_DISPATCHED);
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
         }
%>

<tr>
<td class="standardText"><%=bundle.getString("lb_project")%>:</td>
<td class="standardText" VALIGN="BOTTOM">
<select name="projectId" MULTIPLE size=4>
<option VALUE="*" SELECTED>&lt;<%=bundle.getString("all")%>&gt;</OPTION>
<%
         Collections.sort(projects, new ProjectComparator(Locale.US));
         iter = projects.iterator();
         while (iter.hasNext())
         {
             Project p = (Project) iter.next();
%>
<option VALUE="<%=p.getId()%>"><%=p.getName()%></OPTION>
<%
         }
%>
</select>
</td>
</tr>

<tr>
<td class="standardText"><%=bundle.getString("lb_target_locales")%>*:</td>
<td class="standardText" VALIGN="BOTTOM">
<select name="targetLocalesList" multiple="true" size=4>
<option value="*" selected>&lt;<%=bundle.getString("all")%>&gt;</OPTION>
<%
         ArrayList targetLocales = new ArrayList( ServerProxy.getLocaleManager().getAllTargetLocales() );
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

<INPUT NAME="status" TYPE="HIDDEN" VALUE='<%=Job.DISPATCHED%>'/>

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
<td class="standardText"><%=bundle.getString("lb_report_on")%></td>
<td class="standardText" VALIGN="BOTTOM">
<table cellspacing=0>
<tr>
<td>
<input type="radio" id="reportOnStatus" name="reportOn" checked onclick="setDisable('jobStatus')" value="jobStatus"/><%=bundle.getString("lb_job_status")%>:</td>
<td>
<select id="jobStatus" name="jobStatus" multiple="true" size=4>
<option value="*" selected>&lt;<%=bundle.getString("all")%>&gt;</OPTION>
<option VALUE="ready"><%=bundle.getString("lb_ready")%></OPTION>
<option VALUE="progress"><%=bundle.getString("lb_inprogress")%></OPTION>
<option VALUE="localized"><%=bundle.getString("lb_localized")%></OPTION>
<option VALUE="exported"><%=bundle.getString("lb_exported")%></OPTION>
<option VALUE="archived"><%=bundle.getString("lb_archived")%></OPTION>
</select>
</td>
</tr>
<tr>
<td>
<input type="radio" id="reportOnJobId" name="reportOn" onclick="setDisable('jobIds')" value="jobIds"/><%=bundle.getString("lb_job_ids")%>
</td>
<td><input type="text" id="jobIds" name="jobIds" value="" disabled><%=bundle.getString("lb_job_ids_description")%></td>
</tr>
</table>
</td>
</tr>

<tr>
<td><%=bundle.getString("lb_export_as")%></td>
<td>
<input type="radio" name="exportFormat" value="xls" checked>XLS<br>
<input type="radio" name="exportFormat" value="csv">CSV
</td>
</tr>

<tr>
<td><INPUT type="BUTTON" VALUE="<%=bundle.getString("lb_shutdownSubmit")%>" onClick="submitForm()"></td>
<TD><INPUT type="BUTTON" VALUE="<%=bundle.getString("lb_cancel")%>" onClick="window.close()"></TD>
</tr>
</table>
</form>
<BODY>
</HTML>

