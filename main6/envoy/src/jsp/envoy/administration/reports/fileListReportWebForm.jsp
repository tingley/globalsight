<%@ page contentType="text/html; charset=UTF-8"
         errorPage="/envoy/common/activityError.jsp"
         import="java.util.*, com.globalsight.everest.servlet.util.SessionManager,
                  com.globalsight.everest.webapp.pagehandler.PageHandler,
                  com.globalsight.everest.webapp.pagehandler.projects.workflows.JobSearchConstants,
                  com.globalsight.everest.foundation.SearchCriteriaParameters,
                  com.globalsight.everest.projecthandler.Project,
                  com.globalsight.everest.jobhandler.Job,
                  com.globalsight.util.GlobalSightLocale,
                  com.globalsight.everest.company.CompanyWrapper,
                  java.util.Locale,
                  java.util.ResourceBundle,
                  com.globalsight.everest.company.CompanyThreadLocal,
                  com.globalsight.everest.webapp.pagehandler.administration.users.UserUtil,
                  com.globalsight.everest.usermgr.UserLdapHelper,
                  com.globalsight.everest.webapp.WebAppConstants,
                  com.globalsight.everest.jobhandler.JobImpl,
                  com.globalsight.everest.util.comparator.GlobalSightLocaleComparator,
                  com.globalsight.everest.workflowmanager.Workflow"
          session="true"
%>
<%  
    String EMEA = CompanyWrapper.getCurrentCompanyName();
    //Multi-Company: get current user's company from the session  

    ResourceBundle bundle = PageHandler.getBundle(session);
    SessionManager sessionMgr = (SessionManager)session.getAttribute(WebAppConstants.SESSION_MANAGER);
    
    Locale uiLocale = (Locale) session
                .getAttribute(WebAppConstants.UILOCALE);
    ArrayList<JobImpl> jobList = (ArrayList<JobImpl>)sessionMgr.getAttribute("jobList");
    ArrayList<Project> projectList = (ArrayList<Project>)sessionMgr.getAttribute("projectList");
    ArrayList<GlobalSightLocale> targetLocales = (ArrayList<GlobalSightLocale>)sessionMgr.getAttribute("targetLocales");
    
    // Field names
    String creationStart = JobSearchConstants.CREATION_START;
    String creationStartOptions = JobSearchConstants.CREATION_START_OPTIONS;
    String creationEnd = JobSearchConstants.CREATION_END;
    String creationEndOptions = JobSearchConstants.CREATION_END_OPTIONS;
    
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

function contains(array, item)
{
  for(var i=0;i<array.length;i++)
  {
    if(array[i]=="*"||array[i]==item)
    {
      return true;
    }
  }
  return false;
}

function filterJob()
{
   searchForm.jobNameList.options.length=0;
   
   //selected job status
   var currSelectValueJobStatus = new Array();
   for(i=0;i<searchForm.jobStatus.length;i++)
   {
      var op= searchForm.jobStatus.options[i];
      if(op.selected)
      {
          currSelectValueJobStatus.push(op.value);
      }
   } 
   
   //selected target locales
   var currSelectValueTargetLocale = new Array();
   for(i=0;i<searchForm.targetLocalesList.length;i++)
   {
      var op= searchForm.targetLocalesList.options[i];
      if(op.selected)
      {
          currSelectValueTargetLocale.push(op.value);
      }
   }
   
   //selected project
   var currSelectValueProject = new Array();
   for(i=0;i<searchForm.projectNameList.length;i++)
   {
      var op= searchForm.projectNameList.options[i];
      if(op.selected)
      {
          currSelectValueProject.push(op.value);
      }
   }
   
   <%
     Iterator it = jobList.iterator();
     while (it.hasNext())
     {
          Job j = (Job) it.next();
          %>
          if(contains(currSelectValueProject, "<%=j.getProjectId()%>"))
          {
            if(contains(currSelectValueJobStatus, "<%=j.getState()%>"))
            {
               var isLocaleFlag = "false";
               <%
               Collection c = j.getWorkflows();
               Iterator wfIter = c.iterator();
               while (wfIter.hasNext())
               {
                   Workflow w = (Workflow) wfIter.next();
                   String state = w.getState();
                   if(Workflow.CANCELLED.equals(state))
                   {
                      continue;
                   }
                   // skip certain workflow whose target locale is not selected
                   String trgLocale = w.getTargetLocale().toString();
                   %>
                   if(contains(currSelectValueTargetLocale,"<%=trgLocale%>"))
                   {
                      isLocaleFlag = "true";
                   }
                   <%
                }
                %>
                if(isLocaleFlag=="true")
                {
                   var varItem = new Option("<%=j.getJobName()%>", "<%=j.getId()%>");
		           varItem.setAttribute("title","<%=j.getJobName()%>");
                   searchForm.jobNameList.options.add(varItem);
                }
           }
         }
   <%
     }
   %>
   if(searchForm.jobNameList.options.length==0)
   {
     searchForm.submitButton.disabled=true;
   }
   else
   {
     searchForm.submitButton.disabled=false;
   }
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
<%=bundle.getString("select_the_appropriate_job")%>
</SPAN>
</TD></TR></TABLE>

<form name="searchForm" method="post" action="/globalsight/envoy/administration/reports/fileListXlsReport.jsp">

<table border="0" cellspacing="2" cellpadding="2" class="standardText">

<tr>
<td class="standardText"><%=bundle.getString("lb_job_name")%>:</td>
<td class="standardText" VALIGN="BOTTOM">
<select id = "jobNameList" name="jobNameList" MULTIPLE size="6" style="width:300px">
<%
         Iterator iterJob = jobList.iterator();
         while (iterJob.hasNext())
         {
             Job j = (Job) iterJob.next();
%>
<option title="<%=j.getJobName()%>" VALUE="<%=j.getJobId()%>"><%=j.getJobName()%></OPTION>
<%
         }
%>
</select>
</td>
</tr>

<tr>
<td class="standardText"><%=bundle.getString("lb_project")%>:</td>
<td class="standardText" VALIGN="BOTTOM">
<select id="projectNameList" name="projectNameList" MULTIPLE size=4 onChange="filterJob()">
<option VALUE="*" SELECTED>&lt;<%=bundle.getString("all")%>&gt;</OPTION>
<%
         Iterator iterProject = projectList.iterator();
         while (iterProject.hasNext())
         {
             Project p = (Project) iterProject.next();
%>
<option VALUE="<%=p.getId()%>"><%=p.getName()%></OPTION>
<%
         }
%>
</select>
</td>
</tr>

<tr>
<td class="standardText"><%=bundle.getString("lb_job_status")%>:</td>
<td class="standardText" VALIGN="BOTTOM">
<select id="jobStatus" name="jobStatus" multiple="true" size=4 onChange="filterJob()">
<option value="*" selected>&lt;<%=bundle.getString("all")%>&gt;</OPTION>
<option VALUE="<%=Job.READY_TO_BE_DISPATCHED%>"><%=bundle.getString("lb_ready")%></OPTION>
<option VALUE="<%=Job.DISPATCHED%>"><%=bundle.getString("lb_inprogress")%></OPTION>
<option VALUE="<%=Job.LOCALIZED%>"><%=bundle.getString("lb_localized")%></OPTION>
<option VALUE="<%=Job.EXPORTED%>"><%=bundle.getString("lb_exported")%></OPTION>
<option VALUE="<%=Job.EXPORT_FAIL%>"><%=bundle.getString("lb_exported_failed")%></OPTION>
<option VALUE="<%=Job.ARCHIVED%>"><%=bundle.getString("lb_archived")%></OPTION>
</select>
</tr>

<tr>
<td class="standardText"><%=bundle.getString("lb_target_locales")%>:</td>
<td class="standardText" VALIGN="BOTTOM">
<select id="targetLocalesList" name="targetLocalesList" multiple="true" size=4 onChange="filterJob()">
<option value="*" selected>&lt;<%=bundle.getString("all")%>&gt;</OPTION>
<%
         Collections.sort(targetLocales, new GlobalSightLocaleComparator(Locale.getDefault()));
         Iterator iterLocale = targetLocales.iterator();
         while(iterLocale.hasNext())
         {
             GlobalSightLocale gsLocale = (GlobalSightLocale) iterLocale.next();
%>
<option VALUE="<%=gsLocale.toString()%>"><%=gsLocale.getDisplayName(uiLocale)%></OPTION>
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
<td><%=bundle.getString("lb_export_as")%></td>
<td>
<input type="radio" name="exportFormat" value="xls" checked>XLS<br>
<input type="radio" name="exportFormat" value="csv">CSV
</td>
</tr>

<tr>
<td><INPUT id="submitButton" name="submitButton" type="BUTTON" VALUE="<%=bundle.getString("lb_shutdownSubmit")%>" onClick="submitForm()"></td>
<TD><INPUT type="BUTTON" VALUE="<%=bundle.getString("lb_cancel")%>" onClick="window.close()"></TD>
</tr>
</table>
</form>
<BODY>
</HTML>

