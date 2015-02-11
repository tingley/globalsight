<%@ page contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="java.util.*,com.globalsight.everest.webapp.javabean.NavigationBean,
         com.globalsight.util.resourcebundle.ResourceBundleConstants,
         com.globalsight.util.resourcebundle.SystemResourceBundle,
         com.globalsight.everest.jobhandler.Job,
         com.globalsight.everest.workflowmanager.Workflow,
         com.globalsight.everest.webapp.pagehandler.PageHandler,
         com.globalsight.everest.webapp.pagehandler.projects.workflows.EstimatedTranslateCompletionDateHandler,
         com.globalsight.everest.webapp.pagehandler.projects.workflows.JobManagementHandler,
         com.globalsight.everest.webapp.pagehandler.projects.workflows.WorkflowComparator,
         com.globalsight.everest.foundation.Timestamp,
         com.globalsight.everest.foundation.User,
         java.text.MessageFormat,
         java.util.Locale, java.util.TimeZone, java.util.ResourceBundle"
         session="true" %>
<jsp:useBean id="skinbean" scope="application"
 class="com.globalsight.everest.webapp.javabean.SkinBean" />
<jsp:useBean id="self" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="jobDetails" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="jobInProgress" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="jobReady" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />

<%
    ResourceBundle bundle = PageHandler.getBundle(session);
    SessionManager sessionMgr =
      (SessionManager)session.getAttribute(WebAppConstants.SESSION_MANAGER);
    Locale uiLocale = (Locale)session.getAttribute(WebAppConstants.UILOCALE);
    String selfUrl = self.getPageURL();
    String saveUrl = jobDetails.getPageURL();

    Object from = request.getAttribute("from");

    String title= bundle.getString("lb_edit") + " " + bundle.getString("lb_estimated_translate_completion_date");

    // For sla report issue
    TimeZone timezone = (TimeZone)session.getAttribute(WebAppConstants.USER_TIME_ZONE);

    // Labels of the column titles
    String targLocaleCol = bundle.getString("lb_target_locale");
    String completeCol = bundle.getString("lb_percent_complete");
    String estimatedCol = bundle.getString("lb_estimated_translate_completion_date") + " (mm/dd/yyyy hh:mm)";

    // Button names
    String saveButton = bundle.getString("lb_save");
    String cancelButton = bundle.getString("lb_cancel");

    // Data for the page
    Collection wfs = (Collection)request.getAttribute(EstimatedTranslateCompletionDateHandler.LIST);
    Object[] workflows = wfs.toArray();
    List<Job> jobs = (List<Job>) request.getAttribute("Jobs");

    // Paging Info
    int pageNum = ((Integer)request.getAttribute(EstimatedTranslateCompletionDateHandler.PAGE_NUM)).intValue();

    int numPages = ((Integer)request.getAttribute(EstimatedTranslateCompletionDateHandler.NUM_PAGES)).intValue();

    int listSize = wfs == null ? 0 : wfs.size();
    int totalWorkflows = ((Integer)request.getAttribute(EstimatedTranslateCompletionDateHandler.LIST_SIZE)).intValue();

    int workflowsPerPage = ((Integer)request.getAttribute(
        EstimatedTranslateCompletionDateHandler.NUM_PER_PAGE_STR)).intValue();
    int workflowPossibleTo = pageNum * workflowsPerPage;
    int workflowTo = workflowPossibleTo > totalWorkflows ?
                                 totalWorkflows : workflowPossibleTo;
    int workflowFrom = (workflowTo - listSize) + 1;
    Integer sortChoice = (Integer)sessionMgr.getAttribute(EstimatedTranslateCompletionDateHandler.SORTING);
    
    // save the date fields which can be edited
    List monthList = new ArrayList();
    List dayList = new ArrayList();
    List yearList = new ArrayList();
    List hourList = new ArrayList();
    List minuteList = new ArrayList();
    List<String> workflowIdList = new ArrayList<String>();
    boolean editable = false;
    for (int i=0; i < listSize; i++)
    {
        Workflow wf = (Workflow)workflows[i];
        if (Workflow.READY_TO_BE_DISPATCHED.equals(wf.getState()) ||
           Workflow.DISPATCHED.equals(wf.getState()))
        {
           editable = true;
           String _monthField = "monthField" +  "_" + wf.getId();
           String _dayField = "dayField" + "_" + wf.getId();
           String _yearField = "yearField" + "_" + wf.getId();
           String _hourField = "hourField" + "_" + wf.getId();
           String _minuteField = "minuteField" + "_" + wf.getId();
           monthList.add(_monthField);
           dayList.add(_dayField);
           yearList.add(_yearField);
           hourList.add(_hourField);
           minuteList.add(_minuteField);
           workflowIdList.add("" + wf.getId());
        }
    }

%>

<html>
<head>
<meta http-equiv="content-type" content="text/html;charset=UTF-8">
<title><%= title %></title>
<script language="JavaScript" SRC="/globalsight/includes/setStyleSheet.js"></script>
<script type="text/javascript" src="/globalsight/jquery/jquery-1.6.4.js"></script>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>
<script language="JavaScript">
    var needWarning = true;
    var objectName = "";
    var guideNode = "myJobs";

function cancel()
{
    WFForm.action = "<%=saveUrl%>" + "&action=" + "cancel";
    WFForm.submit();
}

function save()
{
	var ws = $(".workflow");
    var ts = "";
    for (var i = 0; i < ws.length; i++) 
    {
        var w = ws[i];       
        var n = $(w).find("input:checkbox:checked").length;
        if (n > 0)
        {
        	var selects = $(w).find("select");
            var t = $(selects[0]).val() + "/" + $(selects[1]).val() + "/" + $(selects[2]).val() +" " + $(selects[3]).val() + ":" + $(selects[4]).val() + ": 00";
            ts += "|";
            ts += t;
        }                
    }

    var obj = {time: ts};
    $.ajax({
        dataType : "json",
        url : "AjaxService?action=validateTime",
        data : obj,
        contentType : 'application/json;charset=UTF-8',
        success : function(msg) {
            var ob = eval(msg);
            if (ob){
                WFForm.action = "<%=saveUrl%>" + "&action=" + "<%=JobManagementHandler.ESTIMATED_TRANSLATE_COMP_DATE%>";
                WFForm.submit();
            }
            else {
                alert("Can't set date less than today.");
            }
        }
    });
}

function updateEditable(workflowId)
{
    u_checkField = "checkField" + "_" + workflowId;
    u_monthField = "monthField" +  "_" + workflowId;
    u_dayField = "dayField" + "_" + workflowId;
    u_yearField = "yearField" + "_" + workflowId;
    u_hourField = "hourField" + "_" + workflowId;
    u_minuteField = "minuteField" + "_" + workflowId;

    u_objCheckbox = eval("WFForm." + u_checkField);
    u_objMonth = eval("WFForm." + u_monthField);
    u_objYear = eval("WFForm." + u_yearField);
    u_objHour = eval("WFForm." + u_hourField);
    u_objDay = eval("WFForm." + u_dayField);
    u_objMinute = eval("WFForm." + u_minuteField);

    if (u_objCheckbox.checked == true)
    {
        u_objMonth.disabled = false;
        u_objYear.disabled = false;
        u_objHour.disabled = false;
        u_objDay.disabled = false;
        u_objMinute.disabled = false;
    }
    else
    {
        u_objMonth.disabled = true;
        u_objYear.disabled = true;
        u_objHour.disabled = true;
        u_objDay.disabled = true;
        u_objMinute.disabled = true;
    }
}

function updateAll()
{
    objMonth = eval("WFForm.monthField");
    monthIndex = objMonth.selectedIndex;
    objYear = eval("WFForm.yearField");
    yearIndex = objYear.selectedIndex;
    objDay = eval("WFForm.dayField");
    dayIndex = objDay.selectedIndex + 1;
    objHour = eval("WFForm.hourField");
    hourIndex = objHour.selectedIndex;
    objMinute = eval("WFForm.minuteField");
    minuteIndex = objMinute.selectedIndex;

<%
    if (!monthList.isEmpty())
    {
      for (int k = 0; k < monthList.size(); k++)
      {
%>
        monthField = "<%=monthList.get(k)%>";
        dayField = "<%=dayList.get(k)%>";
        yearField = "<%=yearList.get(k)%>";
        hourField = "<%=hourList.get(k)%>";
        minuteField = "<%=minuteList.get(k)%>";

        objMonth = eval("WFForm." + monthField);
        objYear = eval("WFForm." + yearField);
        objHour = eval("WFForm." + hourField);
        objMinute = eval("WFForm." + minuteField);
        objDay = eval("WFForm." + dayField);

        if (objMonth.disabled == true)
        {
        	objMonth.disabled = false;
        	objYear.disabled = false;
        	objHour.disabled = false;
        	objMinute.disabled = false;
        	objDay.disabled = false;
        	wfidStr = "checkField_" + "<%=workflowIdList.get(k)%>";
        	objCheckBox = eval("WFForm." + wfidStr);
        	objCheckBox.checked = true;
        }
            objMonth.options[monthIndex].selected = true;
            month = objMonth.options[monthIndex].value;
            
            objYear.options[yearIndex].selected = true;
            year = objYear.options[yearIndex].value;

            objHour.options[hourIndex].selected = true;
            hour = objHour.options[hourIndex].value;
                        
            objMinute.options[minuteIndex].selected = true;
            minute = objMinute.options[minuteIndex].value;

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
                objDay.options[daysInMonth-1].selected = true;
            }
<%
      }
    }
%>
}

//
// Depending on the month/year, the number of days changes
//
function updateDayList(monthField, dayField, yearField, hourField, minuteField)
{
    objMonth = eval("WFForm." + monthField);
    index = objMonth.selectedIndex;
    month = objMonth.options[index].value;

    objYear = eval("WFForm." + yearField);
    index = objYear.selectedIndex;
    year = objYear.options[index].value;
    
    objHour = eval("WFForm." + hourField);
    index = objHour.selectedIndex;
    hour = objHour.options[index].value;
    
    objMinute = eval("WFForm." + minuteField);
    index = objMinute.selectedIndex;
    minute = objMinute.options[index].value;

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
        objDay.options[daysInMonth-1].selected = true;
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

    <DIV ID="contentLayer" STYLE=" POSITION: ABSOLUTE; Z-INDEX: 9; TOP: 108; LEFT: 20px; RIGHT: 20px; max-height: 800px; overflow: auto ;">
    <div class="mainHeading" style="margin-bottom:8px">
    <%=title%>
    </div>
    <span class="mainHeading">
    &nbsp;
    </span>
    <form name="WFForm" method="post">
    <input type="hidden" id="jobIds" name="jobIds" value="<%=sessionMgr.getAttribute("jobIds")%>">
    <!-- All Workflows data table -->
    <table border="0" cellspacing="0" cellpadding="5" class="list" style="width: 700px">
        <tr class="tableHeadingBasic" valign="bottom" style="padding-bottom: 3px;">
          <td><%=targLocaleCol%></td>
          <td style="padding-left: 20px;" width="280px"><%=estimatedCol%></td>
          <td>
          &nbsp;
          </td>
        </tr>
<%
              Timestamp tsAll = new Timestamp(Timestamp.DATE, timezone);

              String monthField = "monthField";
              String dayField = "dayField";
              String yearField = "yearField";
              String hourField = "hourField";
              String minuteField = "minuteField";
              String disabledAll = editable ? "" : "DISABLED";
%>
              <tr style="padding-bottom:5px; padding-top:5px;"
                  valign=top bgcolor="#FFFFFF">
                  <td class="standardText"><%=bundle.getString("lb_all_target_locales")%></td>

                  <td class="standardText" style="padding-left:20px">
                    <%
                    if (workflows.length > 0 && workflows[0] != null)
                    {
                        tsAll.setDate(((Workflow)workflows[0]).getEstimatedTranslateCompletionDate());
                    }
                    // Month
                    int finalYear = tsAll.getYear() + 10;
                    int chosenMonth = tsAll.getMonth() + 1;

                    String selectedMonth;
                    out.print("<select name='" + monthField + "' " + disabledAll + " class='standardText' onchange='updateDayList(\"" + monthField + "\", \"" + dayField + "\", \"" + yearField + "\", \"" + hourField + "\", \"" + minuteField + "\")'>");
                    for (int j=1; j < 13; j++)
                    {
                        selectedMonth = chosenMonth == j ? "SELECTED" : "";
                        out.println("<option value=" + j + " " + selectedMonth + " >" + j + "</option>");
                    }
                    out.println("</select>");

                    out.println("/");

                    // Day
                    int chosenDay = tsAll.getDayOfMonth();
                    String selectedDay;
                    out.print("<select name=" + dayField + " " + disabledAll + " class='standardText'>");
                    int max = tsAll.getActualMaximum(Calendar.DAY_OF_MONTH);
                    for (int j=1; j <= max; j++)
                    {
                        selectedDay = chosenDay == j ? "SELECTED" : "";
                        out.println("<option value=" + j + " " + selectedDay + " >" + j + "</option>");
                    }
                    out.println("</select>");

                    out.println("/");

                    // Year
                    int chosenYear = tsAll.getYear();
                    String selectedYear;
                    out.print("<select name=" + yearField + " " + disabledAll + " class='standardText' onchange='updateDayList(\"" + monthField + "\", \"" + dayField + "\",\"" + yearField + "\", \"" + hourField + "\", \"" + minuteField + "\")'>");
                    for (int j = 2004; j < finalYear; j++)
                    {
                        selectedYear = chosenYear == j ? "SELECTED" : "";
                        out.println("<option value=" + j + " " + selectedYear + " >" + j + "</option>");
                    }
                    out.println("</select>");
                    
                    out.println("&nbsp;&nbsp;&nbsp;&nbsp;");
                    
                    // Hour
                    int chosenHour = tsAll.getHour();
                    String selectedHour;
                    out.print("<select name=" + hourField + " " + disabledAll + " class='standardText' onchange='updateDayList(\"" + monthField + "\", \"" + dayField + "\",\"" + yearField + "\", \"" + hourField + "\", \"" + minuteField + "\")'>");
                    for (int j = 0; j < 24; j++)
                    {
                        selectedHour = chosenHour == j ? "SELECTED" : "";
                        out.println("<option value=" + j + " " + selectedHour + " >" + j + "</option>");
                    }
                    out.println("</select>");
                    out.println(":");
                    
                    // Minute
                    int chosenMinute = tsAll.getMinute();
                    String selectedMinute;
                    out.print("<select name=" + minuteField + " " + disabledAll + " class='standardText' onchange='updateDayList(\"" + monthField + "\", \"" + dayField + "\",\"" + yearField + "\", \"" + hourField + "\", \"" + minuteField + "\")'>");
                    for (int j = 0; j < 60; j++)
                    {
                        selectedMinute = chosenMinute == j ? "SELECTED" : "";
                        out.println("<option value=" + j + " " + selectedMinute + " >" + j + "</option>");
                    }
                    out.println("</select>");
                    out.println(" (" + timezone.getDisplayName(uiLocale) + ")");
                    %>
                  </td>
                  <TD>
                      <INPUT TYPE="BUTTON" VALUE="<%=bundle.getString("lb_apply_to_all")%>" onClick="updateAll();">
                  </TD>
                </tr>
    </table>
<!-- End Data Table -->
<p>
    <table cellpadding=0 cellspacing=0 border=0 class="standardText">
        <tr valign="top">
            <td align="right">
          </td>
        <tr>
          <td>
<%
StringBuffer editableWFS = new StringBuffer();
for(int is = 0; is < jobs.size(); is++)
{
    Job job = jobs.get(is);
%>
<p>
<div class="mainHeading" style="word-break:break-all; width: 650px">
    <%=bundle.getString("lb_job") %> : <%=job.getJobName() %>
    </div>
    </p>

<!-- Workflow data table -->
  <table border="0" cellspacing="0" cellpadding="5" class="list" style="width: 700px">
    <tr class="tableHeadingBasic" valign="bottom" style="padding-bottom: 3px;">
      <td>
        &nbsp;
      </td>
      <td>
        <a class="sortHREFWhite"> <%=targLocaleCol%></a>
      </td>
      <td style="padding-left: 20px;" >
        <a class="sortHREFWhite"> <%=completeCol%></a>
      </td>
      <td style="padding-left: 20px;" width="280px">
        <a class="sortHREFWhite"> <%=estimatedCol%></a>
      </td>
    </tr>
<%
Collection jobWFS = job.getWorkflows();
workflows = jobWFS.toArray();
listSize = workflows.length;
              
			int icount = 0;
              for (int i=0; i < listSize; i++)
              {
                String color = (icount%2 == 0) ? "#FFFFFF" : "#EEEEEE";
                Workflow wf = (Workflow)workflows[i];
                
               if(wf.getState().equals(Workflow.DISPATCHED)
                || wf.getState().equals(
                        Workflow.READY_TO_BE_DISPATCHED))
               {
               icount++;
                boolean enableEdit = false;
                boolean enableCheck = false;

                if ((Workflow.READY_TO_BE_DISPATCHED.equals(wf.getState()) ||
                     Workflow.DISPATCHED.equals(wf.getState())) &&
                    (wf.getEstimatedTranslateCompletionDate() != null))
                {
                    enableCheck = true;
                    //if (wf.isEstimatedTranslateCompletionDateOverrided())
                    //{
                     //   enableEdit = true;
                    //}
                }

                String checkboxDisabled = enableCheck ? "" : "DISABLED";
                String checkboxchecked = (enableEdit && enableCheck) ? "CHECKED" : "";
                String dateDisabled = enableEdit ? "" : "DISABLED";

                String checkField = "checkField" + "_" + wf.getId();
                monthField = "monthField" +  "_" + wf.getId();
                dayField = "dayField" + "_" + wf.getId();
                yearField = "yearField" + "_" + wf.getId();
                hourField = "hourField" + "_" + wf.getId();
                minuteField = "minuteField" + "_" + wf.getId();
%>
                <tr style="padding-bottom:5px; padding-top:5px;"
                  valign=top bgcolor="<%=color%>" class="workflow">

                  <td class="standardText">
                    <input type='checkbox' name='<%=checkField%>' onclick='updateEditable("<%=wf.getId()%>");' <%=checkboxDisabled%> <%=checkboxchecked%> >
                  </td>

                  <td class="standardText">
                    <%= wf.getTargetLocale().getDisplayName(uiLocale) %>
                  </td>
                  <td class="standardText" style="padding-left:20px">
                    <%= wf.getPercentageCompletion() %>%
                  </td>
                  <td class="standardText" style="padding-left:20px">
                    <%
                    if (enableCheck)
                    {
                        editableWFS.append(wf.getId());
                        editableWFS.append(",");
                    }

                    Timestamp ts = new Timestamp(Timestamp.DATE, timezone);
                    ts.setDate(wf.getEstimatedTranslateCompletionDate());
                    finalYear = ts.getYear() + 10;
                    chosenMonth = ts.getMonth() + 1;
                    chosenDay = ts.getDayOfMonth();
                    chosenYear = ts.getYear();
                    chosenHour = ts.getHour();
                    chosenMinute = ts.getMinute();

                    // Month
                    out.print("<select name='" + monthField + "' " + dateDisabled + " class='standardText' onchange='updateDayList(\"" + monthField + "\", \"" + dayField + "\", \"" + yearField + "\", \"" + hourField + "\", \"" + minuteField + "\")'>");
                    for (int j=1; j < 13; j++)
                    {
                        selectedMonth = chosenMonth == j ? "SELECTED" : "";
                        out.println("<option value=" + j + " " + selectedMonth + " >" + j + "</option>"); 
                    }
                    out.println("</select>");
                    
                    out.println("/");

                    // Day
                    out.print("<select name=" + dayField + " " + dateDisabled + " class='standardText' >");
                    for (int j=1; j <= max; j++)
                    {
                        selectedDay = chosenDay == j ? "SELECTED" : "";
                        out.println("<option value=" + j + " " + selectedDay + " >" + j + "</option>"); 
                    }
                    out.println("</select>");
                    
                    out.println("/");

                    // Year
                    out.print("<select name=" + yearField + " " + dateDisabled + " class='standardText' onchange='updateDayList(\"" + monthField + "\", \"" + dayField + "\", \"" + yearField + "\", \"" + hourField + "\", \"" + minuteField + "\")'>");
                    for (int j = 2004; j < finalYear; j++)
                    {
                        selectedYear = chosenYear == j ? "SELECTED" : "";
                        out.println("<option value=" + j + " " + selectedYear + " >" + j + "</option>"); 
                    }
                    out.println("</select>");
                    
                    out.println("&nbsp;&nbsp;&nbsp;&nbsp;");

                    // Hour
                    out.print("<select name=" + hourField + " " + dateDisabled + " class='standardText' onchange='updateDayList(\"" + monthField + "\", \"" + dayField + "\", \"" + yearField + "\", \"" + hourField + "\", \"" + minuteField + "\")'>");
                    for (int j = 0; j < 24; j++)
                    {
                        selectedHour = chosenHour == j ? "SELECTED" : "";
                        out.println("<option value=" + j + " " + selectedHour + " >" + j + "</option>");
                    }
                    out.println("</select>");
                    out.println(":");
                    
                    // Minute
                    out.print("<select name=" + minuteField + " " + dateDisabled + " class='standardText' onchange='updateDayList(\"" + monthField + "\", \"" + dayField + "\", \"" + yearField + "\", \"" + hourField + "\", \"" + minuteField + "\")'>");
                    for (int j = 0; j < 60; j++)
                    {
                        selectedMinute = chosenMinute == j ? "SELECTED" : "";
                        out.println("<option value=" + j + " " + selectedMinute + " >" + j + "</option>");
                    }
                    out.println("</select>");
                    out.println(" (" + timezone.getDisplayName(uiLocale) + ")");

                    %>
                  </td>
                </tr>
<%
            }
              }
%>
  </tbody>
  </table>
  <%
  }
  %>
  <input type="hidden" name="editableWFS" value="<%=editableWFS%>">
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
<% if (from != null) { %>
    <input type="hidden" name="from" value="<%=from%>" />
<% } %>
</FORM>
</DIV>
</BODY>
</html>
