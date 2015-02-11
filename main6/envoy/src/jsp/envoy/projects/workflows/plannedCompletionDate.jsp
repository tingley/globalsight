<%@ page contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="java.util.*,com.globalsight.everest.webapp.javabean.NavigationBean,
         com.globalsight.util.resourcebundle.ResourceBundleConstants,
         com.globalsight.util.resourcebundle.SystemResourceBundle,
         com.globalsight.everest.jobhandler.Job,
         com.globalsight.everest.workflowmanager.Workflow,
         com.globalsight.everest.webapp.pagehandler.PageHandler,
         com.globalsight.everest.webapp.pagehandler.projects.workflows.PlannedCompletionDateHandler,
         com.globalsight.everest.webapp.pagehandler.projects.workflows.JobManagementHandler,
         com.globalsight.everest.webapp.pagehandler.projects.workflows.WorkflowComparator,
         com.globalsight.everest.foundation.Timestamp,
         com.globalsight.everest.foundation.User,
         java.text.MessageFormat,
         java.util.Locale, java.util.ResourceBundle" 
         session="true" %>
<jsp:useBean id="skinbean" scope="application"
 class="com.globalsight.everest.webapp.javabean.SkinBean" />
<jsp:useBean id="self" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="pending" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="ready" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="inprogress" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="localized" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="exported" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="archived" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />

<% 
    ResourceBundle bundle = PageHandler.getBundle(session);
    SessionManager sessionMgr =
      (SessionManager)session.getAttribute(WebAppConstants.SESSION_MANAGER);
    Locale uiLocale = (Locale)session.getAttribute(WebAppConstants.UILOCALE);
    String selfUrl = self.getPageURL();

    String state = (String)sessionMgr.getAttribute(JobManagementHandler.JOB_STATE);

    String saveUrl = null;
    if (state.equals(Job.PENDING) || state.equals(Job.IMPORTFAILED))
    {
        saveUrl = pending.getPageURL();
    }
    else if (state.equals(Job.READY_TO_BE_DISPATCHED))
    {
        saveUrl = ready.getPageURL();
    }
    else if (state.equals(Job.DISPATCHED))
    {
        saveUrl = inprogress.getPageURL();
    }
    else if (state.equals(Job.LOCALIZED))
    {
        saveUrl = localized.getPageURL();
    }
    else if (state.equals(Job.ARCHIVED))
    {
        saveUrl = archived.getPageURL();
    }
    else if (state.equals(Job.EXPORTED) || state.equals(Job.EXPORT_FAIL))
    {
        saveUrl = exported.getPageURL();
    }

    String title= bundle.getString("lb_edit") + " " + bundle.getString("lb_planned_completion_date");

    // Labels of the column titles
    String targLocaleCol = bundle.getString("lb_target_locale");
    String completeCol = bundle.getString("lb_percent_complete");
    String plannedCol = bundle.getString("lb_planned_completion_date") + " (mm/dd/yyyy)";

    // Button names
    String saveButton = bundle.getString("lb_save");
    String cancelButton = bundle.getString("lb_cancel");

    // Data for the page
    Collection wfs = (Collection)request.getAttribute(PlannedCompletionDateHandler.LIST);
    Object[] workflows = wfs.toArray();

    // Paging Info
    int pageNum = ((Integer)request.getAttribute(PlannedCompletionDateHandler.PAGE_NUM)).intValue();

    int numPages = ((Integer)request.getAttribute(PlannedCompletionDateHandler.NUM_PAGES)).intValue();

    int listSize = wfs == null ? 0 : wfs.size();
    int totalWorkflows = ((Integer)request.getAttribute(PlannedCompletionDateHandler.LIST_SIZE)).intValue();

    int workflowsPerPage = ((Integer)request.getAttribute(
        PlannedCompletionDateHandler.NUM_PER_PAGE_STR)).intValue();
    int workflowPossibleTo = pageNum * workflowsPerPage;
    int workflowTo = workflowPossibleTo > totalWorkflows ?
                                 totalWorkflows : workflowPossibleTo;
    int workflowFrom = (workflowTo - listSize) + 1;
    Integer sortChoice = (Integer)sessionMgr.getAttribute(PlannedCompletionDateHandler.SORTING);

%>

<html>
<head>
<meta http-equiv="content-type" content="text/html;charset=UTF-8">
<title><%= title %></title>
<script language="JavaScript" SRC="/globalsight/includes/setStyleSheet.js"></script>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>
<script language="JavaScript">
    var needWarning = false;
    var objectName = "";
    var guideNode = "myJobs";
    var helpFile = "<%=bundle.getString("help_job_edit_planned_completion_date")%>";

function cancel()
{
    WFForm.action = "<%=saveUrl%>";
    WFForm.submit();
}

function save()
{
    WFForm.action = "<%=saveUrl%>" + "&action=" + "<%=JobManagementHandler.PLANNED_COMP_DATE%>";
    WFForm.submit();
}

//
// Depending on the month/year, the number of days changes
//
function updateDayList(monthField, dayField, yearField)
{
    objMonth = eval("WFForm." + monthField);
    index = objMonth.selectedIndex;
    month = objMonth.options[index].value;
    objYear = eval("WFForm." + yearField);
    index = objYear.selectedIndex;
    year = objYear.options[index].value;
    objDay = eval("WFForm." + dayField);
    dayIndex = objDay.selectedIndex + 1;
    var date = new Date(year, parseInt(month), 0);

    var daysInMonth = date.getDate();
    for (var i = 1; i <= objDay.length; i++) {
        objDay.options[i-1] = null;
    }
    for (var i = 1; i <= daysInMonth; i++) {
        objDay.options[i-1] = new Option(i,i);
    }
    if (dayIndex < daysInMonth)
    {
        objDay.options[dayIndex-1].selected = true;
    }
    else
    {
        objDay.options[daysInMonth].selected = true;
    }
}
</script>
<style type="text/css">
.list {
    border: 1px solid <%=skinbean.getProperty("skin.list.borderColor")%>;
}
</style>
</head>
<body leftmargin="0" rightmargin="0" topmargin="0" marginwidth="0" marginheight="0" onload="loadGuides()">
<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>

    <DIV ID="contentLayer" STYLE=" POSITION: ABSOLUTE; Z-INDEX: 9; TOP: 108; LEFT: 20px; RIGHT: 20px;">
    <span class="mainHeading">
    <%=title%>
    </span>
    <table cellpadding=0 cellspacing=0 border=0 class="standardText">
        <tr valign="top">
            <td align="right">
        <%
        // Make the Paging widget
        if (listSize > 0)
        {
            Object[] args = {new Integer(workflowFrom), new Integer(workflowTo),
                     new Integer(totalWorkflows)};

            // "Displaying x to y of z"
            out.println(MessageFormat.format(
                    bundle.getString("lb_displaying_records"), args));

            out.println("<br>");
            out.println("&lt; ");

            // The "Previous" link
            if (pageNum == 1) {
                // Don't hyperlink "Previous" if it's the first page
                out.print(bundle.getString("lb_previous"));
            }
            else
            {
%>
                <a href="<%=selfUrl%>&<%=PlannedCompletionDateHandler.PAGE_NUM%>=<%=pageNum - 1%>&<%=PlannedCompletionDateHandler.SORTING%>=<%=sortChoice%>"><%=bundle.getString("lb_previous")%></A>
<%
            }

            out.print(" ");

            // Print out the paging numbers
            for (int i = 1; i <= numPages; i++)
            {
                // Don't hyperlink the page you're on
                if (i == pageNum)
                {
                    out.print("<b>" + i + "</b>");
                }
                // Hyperlink the other pages
                else
                {
%>
                    <a href="<%=selfUrl%>&<%=PlannedCompletionDateHandler.PAGE_NUM%>=<%=i%>&<%=PlannedCompletionDateHandler.SORTING%>=<%=sortChoice%>"><%=i%></A>
<%
                }
                out.print(" ");
            }
            // The "Next" link
            if (workflowTo >= totalWorkflows) {
                // Don't hyperlink "Next" if it's the last page
                out.print(bundle.getString("lb_next"));
            }
            else
            {
%>
                <a href="<%=selfUrl%>&<%=PlannedCompletionDateHandler.PAGE_NUM%>=<%=pageNum + 1%>&<%=PlannedCompletionDateHandler.SORTING%>=<%=sortChoice%>"><%=bundle.getString("lb_next")%></A>

<%
            }
            out.println(" &gt;");
        }
%>
          </td>
        <tr>
          <td>
<form name="WFForm" method="post">
<!-- Workflow data table -->
  <table border="0" cellspacing="0" cellpadding="5" class="list">
    <tr class="tableHeadingBasic" valign="bottom" style="padding-bottom: 3px;">
      <td>
        <a class="sortHREFWhite" href="<%=selfUrl%>&<%= PlannedCompletionDateHandler.PAGE_NUM%>=<%=pageNum%>&<%=PlannedCompletionDateHandler.SORTING%>=<%=WorkflowComparator.TARG_LOCALE%>&doSort=true&state=<%=state%>"> <%=targLocaleCol%></a>
      </td>
      <td style="padding-left: 20px;" >
        <a class="sortHREFWhite" href="<%=selfUrl%>&<%= PlannedCompletionDateHandler.PAGE_NUM%>=<%=pageNum%>&<%=PlannedCompletionDateHandler.SORTING%>=<%=WorkflowComparator.COMPLETE%>&doSort=true&state=<%=state%>"> <%=completeCol%></a>
      </td>
      <td style="padding-left: 20px;" width="280px">
        <a class="sortHREFWhite" href="<%=selfUrl%>&<%= PlannedCompletionDateHandler.PAGE_NUM%>=<%=pageNum%>&<%=PlannedCompletionDateHandler.SORTING%>=<%=WorkflowComparator.PLANNED_DATE%>&doSort=true&state=<%=state%>"> <%=plannedCol%></a>
      </td>
    </tr>
<%
              Timestamp ts = new Timestamp(Timestamp.DATE);
              StringBuffer editableWFS = new StringBuffer();
              for (int i=0; i < listSize; i++)
              {
                String color = (i%2 == 0) ? "#FFFFFF" : "#EEEEEE";
                Workflow wf = (Workflow)workflows[i];
                boolean enableEdit =
                      Workflow.READY_TO_BE_DISPATCHED.equals(wf.getState()) || 
                      Workflow.DISPATCHED.equals(wf.getState());
                String disabled = enableEdit ? "" : "DISABLED";
                String monthField = "monthField" +  "_" + wf.getId();
                String dayField = "dayField" + "_" + wf.getId();
                String yearField = "yearField" + "_" + wf.getId();
%>
                <tr style="padding-bottom:5px; padding-top:5px;"
                  valign=top bgcolor="<%=color%>">
                  <td class="standardText">
                    <%= wf.getTargetLocale().getDisplayName(uiLocale) %>
                  </td>
                  <td class="standardText" style="padding-left:20px">
                    <%= wf.getPercentageCompletion() %>%
                  </td>
                  <td class="standardText" style="padding-left:20px">
                    <%
                    if (disabled == "")
                    {
                        editableWFS.append(wf.getId());
                        editableWFS.append(",");
                    }
                    ts.setDate(wf.getPlannedCompletionDate());
                    // Month
                    int finalYear = ts.getYear() + 10;
                    int chosenMonth = ts.getMonth() + 1;
                    String selectedMonth;
                    out.print("<select name='" + monthField + "' " + disabled + " class='standardText' onchange='updateDayList(\"" + monthField + "\", \"" + dayField + "\", \"" + yearField + "\" )'>"); 
                    for (int j=1; j < 13; j++)
                    {
                        selectedMonth = chosenMonth == j ? "SELECTED" : "";
                        out.println("<option value=" + j + " " + selectedMonth + " >" + j + "</option>"); 
                    }
                    out.println("</select>");

                    // Day
                    int chosenDay = ts.getDayOfMonth();
                    String selectedDay;
                    out.print("<select name=" + dayField + " " + disabled + " class='standardText' >"); 
                    int max = ts.getActualMaximum(Calendar.DAY_OF_MONTH);
                    for (int j=1; j <= max; j++)
                    {
                        selectedDay = chosenDay == j ? "SELECTED" : "";
                        out.println("<option value=" + j + " " + selectedDay + " >" + j + "</option>"); 
                    }
                    out.println("</select>");

                    // Year
                    int chosenYear = ts.getYear();
                    String selectedYear;
                    out.print("<select name=" + yearField + " " + disabled + " class='standardText' onchange='updateDayList(\"" + monthField + "\", \"" + dayField + "\", \"" + yearField + "\" )'>"); 
                    for (int j = 2004; j < finalYear; j++)
                    {
                        selectedYear = chosenYear == j ? "SELECTED" : "";
                        out.println("<option value=" + j + " " + selectedYear + " >" + j + "</option>"); 
                    }
                    out.println("</select>");
                    %>
                  </td>
                </tr>
<%
            }
%>
            <input type="hidden" name="editableWFS" value="<%=editableWFS%>">
  </tbody>
  </table>
<!-- End Data Table -->
</TD>
</TR>
</DIV>
<TR><TD>&nbsp;</TD></TR>

<TR>
<TD align="right">
    <P>
    <INPUT TYPE="BUTTON" VALUE="<%=cancelButton%>" onClick="cancel();">
    <INPUT TYPE="BUTTON" VALUE="<%=saveButton%>" onClick="save();">
</TD>
</TR>
</TABLE>
</FORM>
</BODY>
</html>
