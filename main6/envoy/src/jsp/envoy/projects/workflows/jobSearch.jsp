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
                  com.globalsight.everest.foundation.User,
                  com.globalsight.everest.jobhandler.Job,
                  com.globalsight.everest.jobhandler.JobSearchParameters,
                  com.globalsight.everest.projecthandler.ProjectInfo,
                  com.globalsight.everest.webapp.webnavigation.LinkHelper,
                  com.globalsight.everest.servlet.util.ServerProxy,
                  com.globalsight.everest.servlet.EnvoyServletException,
                  com.globalsight.everest.util.system.SystemConfigParamNames,
                  com.globalsight.everest.util.system.SystemConfiguration,
                  com.globalsight.util.GeneralException,
                  com.globalsight.util.GlobalSightLocale,
                  java.text.MessageFormat,
                  java.util.Locale,
                  java.util.ResourceBundle,
                  java.util.List"
          session="true"
%>
<jsp:useBean id="jobSearch" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<%
    ResourceBundle bundle = PageHandler.getBundle(session);
    SessionManager sessionMgr = (SessionManager)session.getAttribute(WebAppConstants.SESSION_MANAGER);
    Locale uiLocale = (Locale)session.getAttribute(WebAppConstants.UILOCALE);
    String userName = (String)session.getAttribute(WebAppConstants.USER_NAME);
    User user = UserHandlerHelper.getUser(userName);
    String userId = user.getUserId();

    // Labels, etc
    String title= bundle.getString("lb_jobs") + " - " + bundle.getString("lb_search");
    String lbsearch = bundle.getString("lb_search");
    String lbclear = bundle.getString("lb_clear");
    
    String type = (String)sessionMgr.getAttribute("destinationPage");
    if (type == null)
        type = "inprogress";
    
    String searchURL = jobSearch.getPageURL() + "&searchType=" + JobSearchConstants.JOB_SEARCH_COOKIE + "&fromRequest=true";;
     if (request.getAttribute("badresults") != null)
     {
        searchURL = "/globalsight/ControlServlet?linkName=jobSearch&pageName=SJ&searchType=jobSearch-";
     }

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

    // Data
    List srcLocales = (List)request.getAttribute("srcLocales");
    List targLocales = (List)request.getAttribute("targLocales");
    List projects = (List)request.getAttribute("projects");
    String cookieName = JobSearchConstants.JOB_SEARCH_COOKIE + userId.hashCode();
    Cookie cookie = (Cookie)sessionMgr.getAttribute(cookieName);
    String searchCriteria = "";
    if (cookie != null)
    {
        searchCriteria = cookie.getValue();
    }
%>
<html>
<head>
<title><%=title%></title>
<script SRC="/globalsight/includes/utilityScripts.js"></script>
<script SRC="/globalsight/includes/setStyleSheet.js"></script>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>

<script>
var needWarning = false;
var objectName = "";
var guideNode="myJobs";
var helpFile = "<%=bundle.getString("help_job_search")%>";

function submitForm(formAction)
{
    if (formAction == "search")
    {
        res = validateForm();
        if (res != "")
        {
            alert(res);
            return;
        }
        setSearchCookie();
        searchForm.action = "<%=searchURL%>";
    }
    searchForm.submit();
}

function isInteger(value)
{
    if (value == "") return true;
    return (parseInt(value) == value);
}

function validateForm()
{
    if (!isInteger(searchForm.<%=idField%>.value))
        return ('<%=bundle.getString("jsmsg_job_search_bad_jobId")%>');
    if (!isInteger(searchForm.<%=creationStart%>.value))
        return ('<%=bundle.getString("jsmsg_job_search_bad_date")%>');
    if (!isInteger(searchForm.<%=creationEnd%>.value))
        return ('<%=bundle.getString("jsmsg_job_search_bad_date")%>');
    if (!isInteger(searchForm.<%=completionStart%>.value))
        return ('<%=bundle.getString("jsmsg_job_search_bad_date")%>');
    if (!isInteger(searchForm.<%=completionEnd%>.value))
        return ('<%=bundle.getString("jsmsg_job_search_bad_date")%>');
    if (searchForm.<%=creationStart%>.value != "" &&
             getOption(searchForm.<%=creationStartOptions%>) == -1)
        return ('<%=bundle.getString("jsmsg_job_search_bad_date2")%>');
    if (searchForm.<%=creationEnd%>.value != "" &&
             getOption(searchForm.<%=creationEndOptions%>) == -1)
        return ('<%=bundle.getString("jsmsg_job_search_bad_date2")%>');
    if (searchForm.<%=completionStart%>.value != "" &&
             getOption(searchForm.<%=completionStartOptions%>) == -1)
        return ('<%=bundle.getString("jsmsg_job_search_bad_date2")%>');
    if (searchForm.<%=completionEnd%>.value != "" &&
             getOption(searchForm.<%=completionEndOptions%>) == -1)
        return ('<%=bundle.getString("jsmsg_job_search_bad_date2")%>');
    if (searchForm.<%=creationStart%>.value != "" &&
             searchForm.<%=creationEnd%>.value == "" &&
             getOption(searchForm.<%=creationEndOptions%>) !=
             '<%=SearchCriteriaParameters.NOW%>')
        return ('<%=bundle.getString("jsmsg_job_search_bad_date3")%>');
    if (searchForm.<%=creationEnd%>.value != "" &&
             searchForm.<%=creationStart%>.value == "")
        return ('<%=bundle.getString("jsmsg_job_search_bad_date3")%>');
    if (searchForm.<%=completionStart%>.value != "" &&
             searchForm.<%=completionEnd%>.value == "" &&
             getOption(searchForm.<%=completionEndOptions%>) !=
             '<%=SearchCriteriaParameters.NOW%>')
        return ('<%=bundle.getString("jsmsg_job_search_bad_date3")%>');
    if (searchForm.<%=completionEnd%>.value != "" &&
             searchForm.<%=completionStart%>.value == "")
        return ('<%=bundle.getString("jsmsg_job_search_bad_date3")%>');
    return "";
}

function clearFields()
{
    searchForm.<%=nameField%>.value = "";
    searchForm.<%=idField%>.value = "";
    searchForm.<%=projectOptions%>.options[0].selected = true;
    searchForm.<%=srcLocale%>.options[0].selected = true;
    searchForm.<%=targLocale%>.options[0].selected = true;
    searchForm.<%=priorityOptions%>.options[0].selected = true;
    searchForm.<%=creationStart%>.value = "";
    searchForm.<%=creationStartOptions%>.options[0].selected = true;
    searchForm.<%=creationEnd%>.value = "";
    searchForm.<%=creationEndOptions%>.options[0].selected = true;
    searchForm.<%=completionStart%>.value = "";
    searchForm.<%=completionStartOptions%>.options[0].selected = true;
    searchForm.<%=completionEnd%>.value = "";
    searchForm.<%=completionEndOptions%>.options[0].selected = true;
}

function setSearchCookie()
{
    var buf = "<%=nameOptions%>=" + getOption(searchForm.<%=nameOptions%>) + ":" +
              "<%=nameField%>=" + searchForm.<%=nameField%>.value + ":" +
              "<%=idOptions%>=" + getOption(searchForm.<%=idOptions%>) + ":" +
              "<%=idField%>=" + searchForm.<%=idField%>.value + ":" +
              "<%=statusOptions%>=" + getOption(searchForm.<%=statusOptions%>) + ":" +
              "<%=projectOptions%>=" + getOption(searchForm.<%=projectOptions%>) + ":" +
              "<%=srcLocale%>=" + getOption(searchForm.<%=srcLocale%>) + ":" +
              "<%=targLocale%>=" + getOption(searchForm.<%=targLocale%>) + ":" +
              "<%=priorityOptions%>=" + getOption(searchForm.<%=priorityOptions%>) + ":" +
              "<%=creationStart%>=" + searchForm.<%=creationStart%>.value + ":" +
              "<%=creationStartOptions%>=" + getOption(searchForm.<%=creationStartOptions%>) + ":" +
              "<%=creationEnd%>=" + searchForm.<%=creationEnd%>.value + ":" +
              "<%=creationEndOptions%>=" + getOption(searchForm.<%=creationEndOptions%>) + ":" +
              "<%=completionStart%>=" + searchForm.<%=completionStart%>.value + ":" +
              "<%=completionStartOptions%>=" + getOption(searchForm.<%=completionStartOptions%>) + ":" +
              "<%=completionEnd%>=" + searchForm.<%=completionEnd%>.value + ":" +
              "<%=completionEndOptions%>=" + getOption(searchForm.<%=completionEndOptions%>) + ":";
    var today = new Date();
    var expires = new Date(today.getTime() + (365 * 86400000));
    document.cookie = "<%=cookieName%>=" + buf + ";EXPIRES=" + expires.toGMTString() + ";PATH=" + escape("/");
}

function setField(fieldname, field, searchCriteria, option)
{
    idx = searchCriteria.indexOf(fieldname);
    if (idx > -1)
    {
        var len = fieldname.length + idx + 1;
        var end = searchCriteria.indexOf(":", len);
        var value = searchCriteria.substr(len, end-(len));
        if (option == true)
            setOption(field, value);
        else
            field.value = value;
    }
}

function getOption(field)
{
    for (var i=0; i < field.length; i++)
    {
        if (field.options[i].selected)
        {
            return field.options[i].value;
        }
    }
    return "";
}

function setOption(field, value)
{
    for (var i=0; i < field.length; i++)
    {
        if (field.options[i].value == value)
        {
            field.selectedIndex = i;
            return;
        }
    }
}

// If user selected "now", then blank out the preceeding numeric field.
function checkNow(field, text)
{
    if (field.options[1].selected)
        text.value = "";
}
</script>
</head>
<body leftmargin="0" rightrmargin="0" topmargin="0" marginwidth="0" marginheight="0" onload="loadGuides()">
<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>
<div id="contentLayer" style="position: absolute; z-index: 9; top: 108; left: 20px; right: 20px;">
<span class="mainHeading"><%=title%></span>
     <%
         String noresults = (String)request.getAttribute("noresults");
         if (noresults != null)
         {
            out.println("<div style='color:red'>");
            String searchType = request.getParameter("searchType");
            out.println(noresults);
            out.println("</div>");
         }
     %>
     <% if (request.getAttribute("badresults") != null)
             out.println("<div style='color:red'>" +  request.getAttribute("badresults") + "</div>"); 
            searchURL = "/globalsight/ControlServlet?linkName=jobSearch&pageName=SJ&action=jobSearch-";
      %>
    <p>

<form name="searchForm" method="post" action="">

<table border="0" cellspacing="2" cellpadding="2" class="standardText">
  <tr><td></td></tr>
  <tr valign="top">
    <td>
      <table border="0" class="standardText" cellpadding="2">
        <tr>
          <td class="standardText">
            <%=bundle.getString("lb_job_name")%>:
          </td>
          <td class="standardText">
            <select name="<%=nameOptions%>">
                <option value='<%=SearchCriteriaParameters.BEGINS_WITH%>'><%= bundle.getString("lb_begins_with") %></option>
                <option value='<%=SearchCriteriaParameters.ENDS_WITH%>'><%= bundle.getString("lb_ends_with") %></option>
                <option value='<%=SearchCriteriaParameters.CONTAINS%>'><%= bundle.getString("lb_contains") %></option>
            </select>
            <input type="text" size="30" name="<%=nameField%>">
            </td>
            </tr>
            <tr>
            <td class="standardText">
            <%=bundle.getString("lb_job_id")%>:
            </td>
            <td class="standardText">
            <select name="<%=idOptions%>">
            <option value='<%=SearchCriteriaParameters.EQUALS%>'><%= bundle.getString("lb_equals") %></option>
            <option value='<%=SearchCriteriaParameters.GREATER_THAN%>'><%= bundle.getString("lb_greater_than") %></option>
            <option value='<%=SearchCriteriaParameters.LESS_THAN%>'><%= bundle.getString("lb_less_than") %></option>
            </select>
            <input type="text" size="30" name="<%=idField%>">
            </td>
            </tr>
            <tr>
            <td class="standardText">
            <%=bundle.getString("lb_status")%><span class="asterisk">*</span>:
            </td>
            <td class="standardText">
            <select name="<%=statusOptions%>">
            <option value='<%=Job.PENDING%>'><%= bundle.getString("lb_pending") %></option>
            <option value='<%=Job.READY_TO_BE_DISPATCHED%>'><%= bundle.getString("lb_ready") %></option>
            <option value='<%=Job.DISPATCHED%>'><%= bundle.getString("lb_inprogress") %></option>
            <option value='<%=Job.LOCALIZED%>'><%= bundle.getString("lb_localized") %></option>
            <option value='<%=Job.EXPORTED%>'><%= bundle.getString("lb_exported") %></option>
            <option value='<%=Job.ARCHIVED%>'><%= bundle.getString("lb_archived") %></option>
            <option value='<%=Job.ALLSTATUS%>'><%= bundle.getString("lb_all_status") %></option>
            </select>
            </td>
            </tr>
            <tr>
            <td class="standardText">
            <%=bundle.getString("lb_project")%>:
            </td>
            <td class="standardText">
            <select name="<%=projectOptions%>">
            <option value="-1">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</option>
    <%
if (projects != null)
{
    for (int i=0; i < projects.size(); i++)
    {
        ProjectInfo p = (ProjectInfo)projects.get(i);
        String projectName = p.getName();
        long projectId = p.getProjectId();
        out.println("<option value='" + projectId + "'>" +
                projectName + "</option>");
    }
}
%>
</select>
</td>
</tr>
<tr>
<td class="standardText">
<%=bundle.getString("lb_source_locale")%>:
</td>
<td>
<select name="<%=srcLocale%>" class="standardText">
<option value="-1"></option>
    <%
if (srcLocales != null)
{
    for (int i = 0; i < srcLocales.size();  i++)
    {
        GlobalSightLocale locale = (GlobalSightLocale)srcLocales.get(i);
        String disp = locale.getDisplayName(uiLocale);
        long lpId = locale.getId();
        out.println("<option value=" + lpId + ">" + disp + "</option>");
    }
}
%>
</select>
</td>
</tr>
<tr>
<td class="standardText">
<%=bundle.getString("lb_target_locale")%>:
</td>
<td>
<select name="<%=targLocale%>" class="standardText">
<option value="-1"></option>
    <%
if (targLocales != null)
{
    for (int i = 0; i < targLocales.size();  i++)
    {
        GlobalSightLocale locale = (GlobalSightLocale)targLocales.get(i);
        String disp = locale.getDisplayName(uiLocale);
        long lpId = locale.getId();
        out.println("<option value=" + lpId + ">" + disp + "</option>");
    }
}
%>
</select>
</td>
</tr>
<tr>
<td class="standardText">
<%=bundle.getString("lb_priority")%>:
</td>
<td class="standardText">
<select name="<%=priorityOptions%>">
<option value='-1'></option>
<option value='1'>1</option>
<option value='2'>2</option>
<option value='3'>3</option>
<option value='4'>4</option>
<option value='5'>5</option>
</select>
</td>
</tr>
<tr>
<td class="standardText" colspan=2>
<%=bundle.getString("lb_creation_date_range")%>:
</td>
</tr>
<tr>
<td class="standardText" style="padding-left:70px" colspan=2>
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
<td class="standardText" colspan=2>
<%=bundle.getString("lb_estimated_completion_date")%>&nbsp;<%=bundle.getString("lb_range")%>:
</td>
</tr>
<tr>
<td class="standardText" style="padding-left:70px" colspan=2>
<%=bundle.getString("lb_starts")%>:
<input type="text" name="<%=completionStart%>" size="3" maxlength="9">
<select name="<%=completionStartOptions%>">
<option value='-1'></option>
<option value='<%=SearchCriteriaParameters.HOURS_AGO%>'><%=bundle.getString("lb_hours_ago")%></option>
<option value='<%=SearchCriteriaParameters.DAYS_AGO%>'><%=bundle.getString("lb_days_ago")%></option>
<option value='<%=SearchCriteriaParameters.WEEKS_AGO%>'><%=bundle.getString("lb_weeks_ago")%></option>
<option value='<%=SearchCriteriaParameters.MONTHS_AGO%>'><%=bundle.getString("lb_months_ago")%></option>
<option value='<%=SearchCriteriaParameters.HOURS_FROM_NOW%>'><%=bundle.getString("lb_hours_from_now")%></option>
<option value='<%=SearchCriteriaParameters.DAYS_FROM_NOW%>'><%=bundle.getString("lb_days_from_now")%></option>
<option value='<%=SearchCriteriaParameters.WEEKS_FROM_NOW%>'><%=bundle.getString("lb_weeks_from_now")%></option>
<option value='<%=SearchCriteriaParameters.MONTHS_FROM_NOW%>'><%=bundle.getString("lb_months_from_now")%></option>
</select>
<%=bundle.getString("lb_ends")%>:
<input type="text" name="<%=completionEnd%>" size="3" maxlength="9">
<select name="<%=completionEndOptions%>" onChange="checkNow(this, searchForm.<%=completionEnd%>)">
<option value='-1'></option>
<option value='<%=SearchCriteriaParameters.NOW%>'><%=bundle.getString("lb_now")%></option>
<option value='<%=SearchCriteriaParameters.HOURS_AGO%>'><%=bundle.getString("lb_hours_ago")%></option>
<option value='<%=SearchCriteriaParameters.DAYS_AGO%>'><%=bundle.getString("lb_days_ago")%></option>
<option value='<%=SearchCriteriaParameters.WEEKS_AGO%>'><%=bundle.getString("lb_weeks_ago")%></option>
<option value='<%=SearchCriteriaParameters.MONTHS_AGO%>'><%=bundle.getString("lb_months_ago")%></option>
<option value='<%=SearchCriteriaParameters.HOURS_FROM_NOW%>'><%=bundle.getString("lb_hours_from_now")%></option>
<option value='<%=SearchCriteriaParameters.DAYS_FROM_NOW%>'><%=bundle.getString("lb_days_from_now")%></option>
<option value='<%=SearchCriteriaParameters.WEEKS_FROM_NOW%>'><%=bundle.getString("lb_weeks_from_now")%></option>
<option value='<%=SearchCriteriaParameters.MONTHS_FROM_NOW%>'><%=bundle.getString("lb_months_from_now")%></option>
</select>
</td>
</tr>
<tr><td>&nbsp;</td></tr>
<tr>
<td>
<input type="button" name="<%=lbsearch%>" value="<%=lbsearch%>"
onclick="submitForm('search')">
<input type="button" name="<%=lbclear%>" value="<%=lbclear%>"
onclick="javascript:clearFields()">
</td>
</tr>
</table>
<script>
<!-- fill in default values -->
var searchCriteria = "<%=searchCriteria%>";
if (searchCriteria.length > 0)
{
    // job name option
    setField("<%=nameOptions%>", searchForm.<%=nameOptions%>, searchCriteria, true);

    // job name field
    setField("<%=nameField%>", searchForm.<%=nameField%>, searchCriteria, false);

    // job id option
    setField("<%=idOptions%>", searchForm.<%=idOptions%>, searchCriteria, true);

    // job id 
    setField("<%=idField%>", searchForm.<%=idField%>, searchCriteria, false);

    // status option
    setField("<%=statusOptions%>", searchForm.<%=statusOptions%>, searchCriteria, true);

    // project option
    setField("<%=projectOptions%>", searchForm.<%=projectOptions%>, searchCriteria, true);

    // source locale option
    setField("<%=srcLocale%>", searchForm.<%=srcLocale%>, searchCriteria, true);

    // target locale option
    setField("<%=targLocale%>", searchForm.<%=targLocale%>, searchCriteria, true);

    // priority option
    setField("<%=priorityOptions%>", searchForm.<%=priorityOptions%>, searchCriteria, true);

    // creation date start 
    setField("<%=creationStart%>", searchForm.<%=creationStart%>, searchCriteria, false);

    // creation date start option
    setField("<%=creationStartOptions%>", searchForm.<%=creationStartOptions%>, searchCriteria, true);

    // creation date end 
    setField("<%=creationEnd%>", searchForm.<%=creationEnd%>, searchCriteria, false);

    // creation date end option
    setField("<%=creationEndOptions%>", searchForm.<%=creationEndOptions%>, searchCriteria, true);

    // completion date end 
    setField("<%=completionStart%>", searchForm.<%=completionStart%>, searchCriteria, false);

    // completion date start option
    setField("<%=completionStartOptions%>", searchForm.<%=completionStartOptions%>, searchCriteria, true);

    // completion end start 
    setField("<%=completionEnd%>", searchForm.<%=completionEnd%>, searchCriteria, false);

    // completion date end option
    setField("<%=completionEndOptions%>", searchForm.<%=completionEndOptions%>, searchCriteria, true);

}
else
{
    // set default status to In Progress
    //setOption(searchForm.<%=statusOptions%>, "<%=Job.DISPATCHED%>");
    <%
    // Always set the status no matter what
    if (type.equals("pending")) {
%>
        setOption(searchForm.<%=statusOptions%>, "<%=Job.PENDING%>");
<%  } else if (type.equals("ready")) { %>
        setOption(searchForm.<%=statusOptions%>, "<%=Job.READY_TO_BE_DISPATCHED%>");
<%  } else if (type.equals("inprogress")) { %>
        setOption(searchForm.<%=statusOptions%>, "<%=Job.DISPATCHED%>");
<%  } else if (type.equals("localized")) { %>
        setOption(searchForm.<%=statusOptions%>, "<%=Job.LOCALIZED%>");
<%  } else if (type.equals("dtpinprogress")) { %>
        setOption(searchForm.<%=statusOptions%>, "<%=Job.DTPINPROGRESS%>");
<%  } else if (type.equals("exported")) { %>
        setOption(searchForm.<%=statusOptions%>, "<%=Job.EXPORTED%>");
<%  } else if (type.equals("archived")) { %>
        setOption(searchForm.<%=statusOptions%>, "<%=Job.ARCHIVED%>");
<%  } else if (type.equals("allStatus")) { %>
        setOption(searchForm.<%=statusOptions%>, "<%=Job.ALLSTATUS%>");        
<%  } %>
}
</script>
</form>

