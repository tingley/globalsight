<%@ page contentType="text/html; charset=UTF-8"
         errorPage="/envoy/common/activityError.jsp"
         import="java.util.*, com.globalsight.everest.servlet.util.SessionManager,
                  com.globalsight.everest.webapp.WebAppConstants,
                  com.globalsight.everest.webapp.javabean.NavigationBean,
                  com.globalsight.everest.webapp.pagehandler.PageHandler,
                  com.globalsight.everest.webapp.pagehandler.projects.workflows.JobSearchConstants,
                  com.globalsight.util.resourcebundle.ResourceBundleConstants,
                  com.globalsight.util.resourcebundle.SystemResourceBundle,
                  com.globalsight.everest.company.CompanyWrapper,
                  com.globalsight.everest.foundation.SearchCriteriaParameters,
                  com.globalsight.everest.jobhandler.Job,
                  com.globalsight.everest.taskmanager.Task,
                  com.globalsight.everest.webapp.webnavigation.LinkHelper,
                  com.globalsight.everest.servlet.util.ServerProxy,
                  com.globalsight.everest.servlet.EnvoyServletException,
                  com.globalsight.everest.util.comparator.StringComparator,
                  com.globalsight.everest.util.system.SystemConfigParamNames,
                  com.globalsight.everest.util.system.SystemConfiguration,
                  com.globalsight.util.GeneralException,
                  com.globalsight.util.GlobalSightLocale,
                  com.globalsight.util.SortUtil,
                  com.globalsight.everest.webapp.pagehandler.administration.users.UserHandlerHelper,
                  com.globalsight.everest.projecthandler.Project,
                  com.globalsight.everest.workflow.WorkflowConstants,
                  java.text.MessageFormat,
                  java.util.Locale,
                  java.util.ResourceBundle,
                  java.util.TreeSet,
                  java.util.List"
          session="true"
%>
<jsp:useBean id="taskSearch" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<%
    ResourceBundle bundle = PageHandler.getBundle(session);
    SessionManager sessionMgr = (SessionManager)session.getAttribute(WebAppConstants.SESSION_MANAGER);
    Locale uiLocale = (Locale)session.getAttribute(WebAppConstants.UILOCALE);
    User user = (User)sessionMgr.getAttribute(WebAppConstants.USER);
    String userId = user.getUserId();

    // Labels, etc
    String title= bundle.getString("lb_my_activities") + " - " + bundle.getString("lb_search");
    String lbsearch = bundle.getString("lb_search");
    String lbclear = bundle.getString("lb_clear");

    String searchURL = taskSearch.getPageURL() + "&action=" + JobSearchConstants.TASK_SEARCH_COOKIE + "&listType=advSearch";

     if (request.getAttribute("badresults") != null)
     {
        searchURL = "/globalsight/ControlServlet?linkName=taskSearch&pageName=ST&action=taskSearch-";
     }

    // Field names
    String nameField = JobSearchConstants.NAME_FIELD;
    String nameOptions = JobSearchConstants.NAME_OPTIONS;
    String idField = JobSearchConstants.ID_FIELD;
    String idOptions = JobSearchConstants.ID_OPTIONS;
    String actNameField = JobSearchConstants.ACT_NAME_FIELD;
    String statusOptions = JobSearchConstants.STATUS_OPTIONS;
    String companyOptions = JobSearchConstants.COMPANY_OPTIONS;
    String srcLocale = JobSearchConstants.SRC_LOCALE;
    String targLocale = JobSearchConstants.TARG_LOCALE;
    String priorityOptions = JobSearchConstants.PRIORITY_OPTIONS;
    String acceptanceStart = JobSearchConstants.ACCEPTANCE_START;
    String acceptanceStartOptions = JobSearchConstants.ACCEPTANCE_START_OPTIONS;
    String acceptanceEnd = JobSearchConstants.ACCEPTANCE_END;
    String acceptanceEndOptions = JobSearchConstants.ACCEPTANCE_END_OPTIONS;
    String completionStart = JobSearchConstants.EST_COMPLETION_START;
    String completionStartOptions = JobSearchConstants.EST_COMPLETION_START_OPTIONS;
    String completionEnd = JobSearchConstants.EST_COMPLETION_END;
    String completionEndOptions = JobSearchConstants.EST_COMPLETION_END_OPTIONS;


    // Data
    List srcLocales = (List)request.getAttribute("srcLocales");
    List targLocales = (List)request.getAttribute("targLocales");
    String cookieName = JobSearchConstants.TASK_SEARCH_COOKIE + userId.hashCode();
    Cookie cookie = (Cookie)sessionMgr.getAttribute(cookieName);
    String searchCriteria = "";
    if (cookie != null)
        searchCriteria = cookie.getValue();
        
%>
<html>
<head>
    <meta http-equiv="content-type" content="text/html;charset=UTF-8">
    <title><%= title %></title>
    <script language="JavaScript" SRC="/globalsight/includes/utilityScripts.js"></script>
    <script language="JavaScript" SRC="/globalsight/includes/setStyleSheet.js"></script>
    <%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
    <%@ include file="/envoy/common/warning.jspIncl" %>


<script language="JavaScript">
var needWarning = false;
var objectName = "";
var guideNode="myActivities";
var helpFile = "<%=bundle.getString("help_activity_search")%>";

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

function isInteger(value) {
    if (value == "") return true;
    return (parseInt(value) == value);
}

function validateForm()
{
    if (!isInteger(searchForm.<%=idField%>.value))
        return ('<%=bundle.getString("jsmsg_job_search_bad_jobId")%>');
    if (!isInteger(searchForm.<%=acceptanceStart%>.value))
        return ('<%=bundle.getString("jsmsg_job_search_bad_date")%>');
    if (!isInteger(searchForm.<%=acceptanceEnd%>.value))
        return ('<%=bundle.getString("jsmsg_job_search_bad_date")%>');
    if (!isInteger(searchForm.<%=completionStart%>.value))
        return ('<%=bundle.getString("jsmsg_job_search_bad_date")%>');
    if (!isInteger(searchForm.<%=completionEnd%>.value))
        return ('<%=bundle.getString("jsmsg_job_search_bad_date")%>');
    if (searchForm.<%=acceptanceStart%>.value != "" &&
             getOption(searchForm.<%=acceptanceStartOptions%>) == -1)
        return ('<%=bundle.getString("jsmsg_job_search_bad_date2")%>');
    if (searchForm.<%=acceptanceEnd%>.value != "" &&
             getOption(searchForm.<%=acceptanceEndOptions%>) == -1)
        return ('<%=bundle.getString("jsmsg_job_search_bad_date2")%>');
    if (searchForm.<%=completionStart%>.value != "" &&
             getOption(searchForm.<%=completionStartOptions%>) == -1)
        return ('<%=bundle.getString("jsmsg_job_search_bad_date2")%>');
    if (searchForm.<%=completionEnd%>.value != "" &&
             getOption(searchForm.<%=completionEndOptions%>) == -1)
        return ('<%=bundle.getString("jsmsg_job_search_bad_date2")%>');
    if (searchForm.<%=acceptanceStart%>.value != "" &&
             searchForm.<%=acceptanceEnd%>.value == "" &&
             getOption(searchForm.<%=acceptanceEndOptions%>) !=
             '<%=SearchCriteriaParameters.NOW%>')
        return ('<%=bundle.getString("jsmsg_job_search_bad_date3")%>');
    if (searchForm.<%=acceptanceEnd%>.value != "" &&
             searchForm.<%=acceptanceStart%>.value == "")
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
    searchForm.<%=actNameField%>.value = "";
    searchForm.<%=srcLocale%>.options[0].selected = true;
    searchForm.<%=targLocale%>.options[0].selected = true;
    searchForm.<%=priorityOptions%>.options[0].selected = true;
    searchForm.<%=acceptanceStart%>.value = "";
    searchForm.<%=acceptanceStartOptions%>.options[0].selected = true;
    searchForm.<%=acceptanceEnd%>.value = "";
    searchForm.<%=acceptanceEndOptions%>.options[0].selected = true;
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
              "<%=actNameField%>=" + searchForm.<%=actNameField%>.value + ":" +
              "<%=statusOptions%>=" + getOption(searchForm.<%=statusOptions%>) + ":" +
              "<%=companyOptions%>=" + getOption(searchForm.<%=companyOptions%>) + ":" +
              "<%=srcLocale%>=" + getOption(searchForm.<%=srcLocale%>) + ":" +
              "<%=targLocale%>=" + getOption(searchForm.<%=targLocale%>) + ":" +
              "<%=priorityOptions%>=" + getOption(searchForm.<%=priorityOptions%>) + ":" +
              "<%=acceptanceStart%>=" + searchForm.<%=acceptanceStart%>.value + ":" +
              "<%=acceptanceStartOptions%>=" + getOption(searchForm.<%=acceptanceStartOptions%>) + ":" +
              "<%=acceptanceEnd%>=" + searchForm.<%=acceptanceEnd%>.value + ":" +
              "<%=acceptanceEndOptions%>=" + getOption(searchForm.<%=acceptanceEndOptions%>) +":" +
              "<%=completionStart%>=" + searchForm.<%=completionStart%>.value + ":" +
              "<%=completionStartOptions%>=" + getOption(searchForm.<%=completionStartOptions%>) + ":" +
              "<%=completionEnd%>=" + searchForm.<%=completionEnd%>.value + ":" +
              "<%=completionEndOptions%>=" + getOption(searchForm.<%=completionEndOptions%>) + ":";
              
    document.cookie = "<%=cookieName%>=" + escape(buf);
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

/*
 * If user selected "now", then blank out the preceeding numeric field.
 */
function checkNow(field, text)
{
    if (field.options[1].selected)
        text.value = "";
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
</script>
</head>

<body leftmargin="0" rightrmargin="0" topmargin="0" marginwidth="0" marginheight="0" onload="loadGuides()">
<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>
    <div id="contentLayer" style="position: absolute; z-index: 9; top: 108; left: 20px; right: 20px;">
    <span class="mainHeading">
        <%=title%>
    </span>
     <%
        String noresults = (String)request.getAttribute("noresults");
        if (noresults != null)
             out.println("<div style='color:red'>" + noresults + "</div>"); %>
     <% if (request.getAttribute("badresults") != null)
         out.println("<div style='color:red'>" +  request.getAttribute("badresults") + "</div>"); %>
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
            <%=bundle.getString("lb_activity_name")%>:
          </td>
          <td>
            <input type="text" size="30" name="<%=actNameField%>">
          </td>
        </tr>
        <tr>
          <td class="standardText">
            <%=bundle.getString("lb_status")%><span class="asterisk">*</span>:
          </td>
          <td class="standardText">
            <select name="<%=statusOptions%>">
                <option value='<%=Task.STATE_ACTIVE%>'><%= bundle.getString("lb_available") %></option>
                <option value='<%=Task.STATE_ACCEPTED%>'><%= bundle.getString("lb_inprogress") %></option>
<!--            <option value='<%=WorkflowConstants.TASK_GSEDITION_IN_PROGESS%>'><%= bundle.getString("lb_inprogress") %>(<%= bundle.getString("lb_gsedition") %>)</option>
 -->
                <option value='<%=Task.STATE_COMPLETED%>'><%= bundle.getString("lb_finished") %></option>
                <option value='<%=Task.STATE_REJECTED%>'><%= bundle.getString("lb_rejected") %></option>
                <option value='<%=Task.STATE_ALL%>'><%= bundle.getString("lb_all_status") %></option> 
            </select>
          </td>
        </tr>
        <tr>
          <td class="standardText">
            <%=bundle.getString("lb_company")%>:
          </td>
          <%
          ArrayList<String> companyList = new ArrayList<String>();
          String companyName = CompanyWrapper.getCurrentCompanyName();
          companyList.add(companyName);
          if(CompanyWrapper.isSuperCompanyName(companyName))
          {
              List projectList =  UserHandlerHelper.getProjectsByUser(userId);
              for(int i = 0; i < projectList.size(); i++)
              {
                 long companyId = ((Project) projectList.get(i)).getCompanyId();
                 companyName =  CompanyWrapper.getCompanyNameById(companyId);
                 if (!companyList.contains(companyName))
                 {
                     companyList.add(companyName);
                 }
              }
          }
          StringComparator comparator = new StringComparator(Locale.getDefault());
          SortUtil.sort(companyList, comparator);
          Iterator companyIterator = companyList.iterator();
          %>
          <td>
              <select name="<%=companyOptions%>">
              <%while(companyIterator.hasNext()){
              String com = (String)(companyIterator.next());%>
                  <option value='<%= com %>'><%= com %></option>
              <%}%>
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
            <%=bundle.getString("lb_acceptance_date_range")%>:
          </td>
        </tr>
        <tr>
          <td class="standardText" style="padding-left:70px" colspan=2>
            <%=bundle.getString("lb_starts")%>:
            <input type="text" name="<%=acceptanceStart%>" size="3" maxlength="9">
            <select name="<%=acceptanceStartOptions%>">
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
            <input type="text" name="<%=acceptanceEnd%>" size="3" maxlength="9">
            <select name="<%=acceptanceEndOptions%>" onChange="checkNow(this, searchForm.<%=acceptanceEnd%>)">
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

    // activity name field
    setField("<%=actNameField%>", searchForm.<%=actNameField%>, searchCriteria, false);

    // status option
    setField("<%=statusOptions%>", searchForm.<%=statusOptions%>, searchCriteria, true);

    // source locale option
    setField("<%=srcLocale%>", searchForm.<%=srcLocale%>, searchCriteria, true);

    // target locale option
    setField("<%=targLocale%>", searchForm.<%=targLocale%>, searchCriteria, true);

    // priority option
    setField("<%=priorityOptions%>", searchForm.<%=priorityOptions%>, searchCriteria, true);

    // acceptance date start 
    setField("<%=acceptanceStart%>", searchForm.<%=acceptanceStart%>, searchCriteria, false);
    // acceptance date start option
    setField("<%=acceptanceStartOptions%>", searchForm.<%=acceptanceStartOptions%>, searchCriteria, true);

    // acceptance date end 
    setField("<%=acceptanceEnd%>", searchForm.<%=acceptanceEnd%>, searchCriteria, false);

    // acceptance date end option
    setField("<%=acceptanceEndOptions%>", searchForm.<%=acceptanceEndOptions%>, searchCriteria, true);

    // completion date end 
    setField("<%=completionStart%>", searchForm.<%=completionStart%>, searchCriteria, false);

    // completion date start option
    setField("<%=completionStartOptions%>", searchForm.<%=completionStartOptions%>, searchCriteria, true);

    // completion end start 
    setField("<%=completionEnd%>", searchForm.<%=completionEnd%>, searchCriteria, false);

    // completion date end option
    setField("<%=completionEndOptions%>", searchForm.<%=completionEndOptions%>, searchCriteria, true);

}
</script>
</form>

