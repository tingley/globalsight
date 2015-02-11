<%@ page contentType="text/html; charset=UTF-8"
         errorPage="/envoy/common/activityError.jsp"
         import="java.util.*, com.globalsight.everest.servlet.util.SessionManager,
                  com.globalsight.everest.foundation.Timestamp,
                  com.globalsight.everest.webapp.WebAppConstants,
                  com.globalsight.everest.webapp.javabean.NavigationBean,
                  com.globalsight.everest.webapp.pagehandler.PageHandler,
                  com.globalsight.everest.webapp.pagehandler.administration.calendars.CalendarConstants,
                  com.globalsight.util.resourcebundle.ResourceBundleConstants,
                  com.globalsight.util.resourcebundle.SystemResourceBundle,
                  com.globalsight.everest.webapp.webnavigation.LinkHelper,
                  com.globalsight.everest.servlet.util.ServerProxy,
                  com.globalsight.everest.servlet.EnvoyServletException,
                  com.globalsight.everest.util.system.SystemConfigParamNames,
                  com.globalsight.everest.util.system.SystemConfiguration,
                  com.globalsight.calendar.ReservedTime,
                  com.globalsight.calendar.UserFluxCalendar,
                  com.globalsight.util.GeneralException,
                  java.text.MessageFormat,
                  java.util.ArrayList,
                  java.util.Calendar,
                  java.util.Locale,
                  java.util.ResourceBundle"
          session="true"
%>
<style type="text/css">
.day {
    font-famliy:Arial; font-size:12px; font-weight: bold;
}
</style>
<jsp:useBean id="cancel" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="save" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<%
    ResourceBundle bundle = PageHandler.getBundle(session);
    Locale uiLocale = (Locale)session.getAttribute(WebAppConstants.UILOCALE);
    SessionManager sessionManager = (SessionManager)session.getAttribute(WebAppConstants.SESSION_MANAGER);

    // UI Fields
    String subjectField = CalendarConstants.SUBJECT_FIELD;
    String monthField = CalendarConstants.MONTH_FIELD;
    String yearField = CalendarConstants.YEAR_FIELD;
    String dayField = CalendarConstants.DAY_FIELD;
    String startHourField = CalendarConstants.START_HOUR_FIELD;
    String startMinField = CalendarConstants.START_MIN_FIELD;
    String endHourField = CalendarConstants.END_HOUR_FIELD;
    String endMinField = CalendarConstants.END_MIN_FIELD;
    String endMonthField = CalendarConstants.END_MONTH_FIELD;
    String endDayField = CalendarConstants.END_DAY_FIELD;
    String endYearField = CalendarConstants.END_YEAR_FIELD;

    // Labels, etc
    String badDate = bundle.getString("jsmsg_invalid_date");
    String title= bundle.getString("lb_new_event");

    String lbcancel = bundle.getString("lb_cancel");
    String lbsave = bundle.getString("lb_save");

    String cancelURL = cancel.getPageURL() + "&action=" + CalendarConstants.CANCEL_ACTION;
    String saveURL = save.getPageURL() + "&action=" + CalendarConstants.SAVE_ACTION;

    String months[] = new String[12];
    months[0] = bundle.getString("lb_january");
    months[1] = bundle.getString("lb_february");
    months[2] = bundle.getString("lb_march");
    months[3] = bundle.getString("lb_april");
    months[4] = bundle.getString("lb_may");
    months[5] = bundle.getString("lb_june");
    months[6] = bundle.getString("lb_july");
    months[7] = bundle.getString("lb_august");
    months[8] = bundle.getString("lb_september");
    months[9] = bundle.getString("lb_october");
    months[10] = bundle.getString("lb_november");
    months[11] = bundle.getString("lb_december");

    String hours[] = new String[25];
    hours[0] = "12 midnight";
    for (int i = 1; i < 12; i++)
    {
        hours[i] = i + " am";
    }
    hours[12] = "12 noon";
    for (int i = 13; i < 24; i++)
    {
        hours[i] = i-12 + " pm";
    }
    hours[24] = "12 midnight";

    // Data if editing reserved time
    Timestamp timestamp =
         (Timestamp)sessionManager.getAttribute("reservedTimeDate");
    String subject = "";
    int month = timestamp.getMonth();
    int day = timestamp.getDayOfMonth();
    int year = timestamp.getYear();
    int endMonth = month;
    int endDay = day;
    int endYear = year;
    boolean allDay = true;
    boolean allDayPersonal = false;
    boolean oneDayOnly = true;
    int startHour = -1;
    int startMin = 0;
    int endHour = -1;
    int endMin = 0;
    ReservedTime rt = (ReservedTime)sessionManager.getAttribute("reservedTime");
    if (rt != null)
    {
        title= bundle.getString("lb_edit_event");
        subject = rt.getSubject();
        Timestamp ts = rt.getStartTimestamp();
        month = ts.getMonth();
        day = ts.getDayOfMonth();
        year = ts.getYear();
        ts = rt.getEndTimestamp();
        endMonth = ts.getMonth();
        endDay = ts.getDayOfMonth();
        endYear = ts.getYear();
        startHour = rt.getStartHour();
        startMin = rt.getStartMinute();
        endHour = rt.getEndHour();
        endMin = rt.getEndMinute();
        oneDayOnly = rt.isOneDayOnly();
        allDay = rt.isAllDay();
        if (allDay)
        {
            startHour = -1;
            startMin = 0;
            endHour = -1;
            endMin = 0;
            if (rt.getType() == ReservedTime.TYPE_PERSONAL)
            {
                allDayPersonal = true;
                allDay = false;
            }
        }
    }

%>
<html>
<head>
    <meta http-equiv="content-type" content="text/html;charset=UTF-8">
    <title><%= title %></title>
    <script language="JavaScript" SRC="/globalsight/includes/utilityScripts.js"></script>
    <script language="JavaScript" SRC="/globalsight/includes/setStyleSheet.js"></script>
    <%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
    <%@ include file="/envoy/common/warning.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>

<script language="JavaScript">
var needWarning = true;
var objectName = "<%=bundle.getString("lb_event")%>";
var guideNode = "calendars";
var helpFile = "<%=bundle.getString("help_events_edit")%>";

function submitForm(formAction)
{
    if (formAction == "cancel")
    {
       if (confirmJump())
       {
           rtForm.action = "<%=cancelURL%>";
           rtForm.submit();
       }
       else
       {
          return false;
       }
    }
    if (formAction == "save")
    {
        if (confirmForm())
        {
            rtForm.action = "<%=saveURL%>";
            rtForm.submit();
        }
    }
}

// Confirm that the start date is less than the end date
// Return false if not.
function confirmDates()
{
    var startdate = new Date(rtForm.<%=yearField%>.value,
             rtForm.<%=monthField%>.value, rtForm.<%=dayField%>.value);
    var enddate = new Date(rtForm.<%=endYearField%>.value,
             rtForm.<%=endMonthField%>.value, rtForm.<%=endDayField%>.value);

    if (startdate.getTime() <= enddate.getTime())
    {
        return true;
    }
    return false;
}

//
// Do some validation before submitting
//
function confirmForm()
{
    var theName = rtForm.<%=subjectField%>.value;
    theName = stripBlanks(theName);

    // Subject field must be filled in
    if (isEmptyString(rtForm.<%=subjectField%>.value)) {
        alert("<%=bundle.getString("jsmsg_subject")%>");
        rtForm.<%=subjectField%>.value = "";
        rtForm.<%=subjectField%>.focus();
        return false;
    }

    // End date must not be greater than start date
    var selectedRadioBtn = null;
    for (i = 0; i < rtForm.dateRadio.length; i++)
    {
        if (rtForm.dateRadio[i].checked == true)
        {
            selectedRadioBtn = rtForm.dateRadio[i].value;
        }
    }
    if (selectedRadioBtn == "multiDays")
    {
        if (confirmDates() == false)
        {
            alert("<%=bundle.getString("jsmsg_selected_dates")%>");
            return false;
        }
    }

    // The start time must be earlier than the end time
    for (i = 0; i < rtForm.timeRadio.length; i++)
    {
        if (rtForm.timeRadio[i].checked == true)
        {
            selectedRadioBtn = rtForm.timeRadio[i].value;
        }
    }
    if (selectedRadioBtn == "partial")
    {
        fromHour = rtForm.<%=startHourField%>.options[rtForm.<%=startHourField%>.selectedIndex].value;
        fromMin = rtForm.<%=startMinField%>.options[rtForm.<%=startMinField%>.selectedIndex].value;
        toHour = rtForm.<%=endHourField%>.options[rtForm.<%=endHourField%>.selectedIndex].value;
        toMin = rtForm.<%=endMinField%>.options[rtForm.<%=endMinField%>.selectedIndex].value;
        if (parseInt(fromHour) > parseInt(toHour) ||
            (parseInt(fromHour) == parseInt(toHour) &&
             parseInt(fromMin) > parseInt(toMin)))
        {
            alert("<%=bundle.getString("jsmsg_time2")%>");
            return false;
        }
    }
    return true;
}

function updateTimeRadio()
{
    rtForm.timeRadio[2].checked = true;
}

function updateDateRadio()
{
    rtForm.dateRadio[1].checked = true;
}

//
// Depending on the month/year, the number of days changes
//
function updateDayList(yearField, monthField, dayField)
{
    obj = eval("rtForm." + monthField);
    index = obj.selectedIndex;
    month = obj.options[index].value;
    obj = eval("rtForm." + yearField);
    index = obj.selectedIndex;
    year = obj.options[index].value;
    obj = eval("rtForm." + dayField);
    dayIndex = obj.selectedIndex;
    var date = new Date(year, parseInt(month)+1, 0);
        
    var daysInMonth = date.getDate();
    for (var i = 1; i <= obj.length; i++) {
        obj.options[i-1] = null;
    }
    for (var i = 1; i <= daysInMonth; i++) {
        obj.options[i-1] = new Option(i,i);
    }
    if (dayIndex < daysInMonth)
    {
        obj.options[dayIndex].selected = true;
    }
}

</script>
</head>

<body leftmargin="0" rightrmargin="0" topmargin="0" marginwidth="0" marginheight="0" onload="loadGuides()">
<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>
    <div id="contentLayer" style="position: absolute; z-index: 9; top: 108; left: 20px; right: 20px;">
    <span class="mainHeading"> <%=title%> </span>
    <br>


<form name="rtForm" method="post" action="">
<input type="hidden" name="formAction" value="">
<% if (rt != null) { %>
<input type="hidden" name="edit" >
<% } %>

<table border="0" bordercolor="red" cellspacing="2" cellpadding="2" class="standardText">
  <tr><td></td></tr>
  <tr valign="top">
    <td width="15%">
      <%=bundle.getString("lb_subject")%><span class="asterisk">*</span>:
    </td>
    <td>
      <input type="text" name="<%=subjectField%>" maxlength="80" size="40"
        value="<%=subject%>">
    </td>
  </tr>
  <tr>
    <td>
       <%=bundle.getString("lb_date")%>:
    </td>
    <td>
      <select name="<%=monthField%>" onchange="updateDayList('<%=yearField%>', '<%=monthField%>', '<%=dayField%>')">
<%
          for (int i = 0; i < months.length; i++)
          {
            out.println("<option value='" + i + "'");
            if (month == i)
            {
                out.println(" selected ");
            }
            out.println(">" + months[i] + "</option>");
          }
%>

      </select>
      <select name="<%=dayField%>">
<%
          for (int i = 1; i <= 31; i++)
          {
            out.println("<option value='" + i + "'");
            if (day == i)
            {
                out.println(" selected ");
            }
            out.println(">" + i + "</option>");
          }
%>
      </select>
      <select name="<%=yearField%>" onchange="updateDayList('<%=yearField%>', '<%=monthField%>', '<%=dayField%>')">
          <% 	Calendar cale = Calendar.getInstance();
                int thisyear = cale.get(Calendar.YEAR);
                for (int i = 2004; i < thisyear + 1; i ++) {
                %>
          <option value=<%= i %>
            <% if (year == i) out.println(" selected "); %>
            ><%= i %></option>
          <% } %>
    </select>
    </td>
  </tr>
  <tr><td></td></tr>
  <tr>
    <td>
      <%=bundle.getString("lb_time")%>:
    </td>
    <td>
      <input type="radio" name="timeRadio" value="allDay"
      <% if (allDay) out.print(" checked"); %> >
      <%=bundle.getString("lb_all_day")%>
    </td>
  </tr>
  <tr>
    <td>&nbsp;</td>
    <td>
      <input type="radio" name="timeRadio" value="allDayPersonal"
      <% if (allDayPersonal) out.print(" checked"); %> >
      <%=bundle.getString("lb_all_day")%> - <%=bundle.getString("lb_personal")%>
    </td>
  </tr>
  <tr>
    <td>&nbsp;</td>
    <td colspan="2">
      <input type="radio" name="timeRadio" value="partial"
      <% if (allDay == false && allDayPersonal == false) out.print(" checked"); %> >
      <%=bundle.getString("lb_starts_at")%>
          <select name="<%=startHourField%>" onchange="updateTimeRadio()">
            <option value="-1">&nbsp;</option>
<%
            for (int i = 0; i < hours.length; i++)
            {
                out.print("<option value='" + i + "'");
                if (startHour == i) out.print(" selected");
                out.println(">" + hours[i] + "</option>");
            }
%>
          </select>
          <select name="<%=startMinField%>" onchange="updateTimeRadio()">
<%
            out.print("<option value='0'");
            if (startMin == 0) out.print(" selected ");
            out.println(">:00</option>");
            out.print("<option value='15'");
            if (startMin == 15) out.print(" selected ");
            out.println(">:15</option>");
            out.print("<option value='30'");
            if (startMin == 30) out.print(" selected ");
            out.println(">:30</option>");
            out.print("<option value='45'");
            if (startMin == 45) out.print(" selected ");
            out.println(">:45</option>");
%>
          </select>
          &nbsp;&nbsp;<%=bundle.getString("lb_ends_at")%>
          <select name="<%=endHourField%>" onchange="updateTimeRadio()">
            <option value="-1">&nbsp;</option>
<%
            for (int i = 0; i < hours.length; i++)
            {
                out.print("<option value='" + i + "'");
                if (endHour == i) out.print(" selected");
                out.println(">" + hours[i] + "</option>");
            }
%>
          </select>
          <select name="<%=endMinField%>" onchange="updateTimeRadio()">
<%
            out.print("<option value='0'");
            if (endMin == 0) out.print(" selected ");
            out.println(">:00</option>");
            out.print("<option value='15'");
            if (endMin == 15) out.print(" selected ");
            out.println(">:15</option>");
            out.print("<option value='30'");
            if (endMin == 30) out.print(" selected ");
            out.println(">:30</option>");
            out.print("<option value='45'");
            if (endMin == 45) out.print(" selected ");
            out.println(">:45</option>");
%>
          </select>
    </td>
  </tr>
  <tr><td></td></tr>
  <tr>
    <td>
      <%= bundle.getString("lb_end_date") %>:
    </td>
    <td colspan="2">
      <input type="radio" name="dateRadio" value="oneDayOnly"
      <% if (oneDayOnly) out.print(" checked"); %> >
      <%=bundle.getString("lb_one_day_only")%>
    </td>
  </tr>
  <tr>
    <td>&nbsp;</td>
    <td colspan="2">
      <input type="radio" name="dateRadio" value="multiDays"
      <% if (oneDayOnly == false) out.print(" checked"); %> >
      <%=bundle.getString("lb_until")%>
      <select name="<%=endMonthField%>" onchange="updateDayList('<%=endYearField%>', '<%=endMonthField%>', '<%=endDayField%>');updateDateRadio(this)">
<%
          for (int i = 0; i < months.length; i++)
          {
            out.println("<option value='" + i + "'");
            if (endMonth == i)
            {
                out.println(" selected ");
            }
            out.println(">" + months[i] + "</option>");
          }
%>

      </select>
      <select name="<%=endDayField%>" onchange="updateDateRadio(this)" >
<%
          for (int i = 1; i <= 31; i++)
          {
            out.println("<option value='" + i + "'");
            if (endDay == i)
            {
                out.println(" selected ");
            }
            out.println(">" + i + "</option>");
          }
%>
      </select>
      <select name="<%=endYearField%>" onchange="updateDayList('<%=endYearField%>', '<%=endMonthField%>', '<%=endDayField%>');updateDateRadio(this)">
          <% 	for (int i = 2004; i < thisyear + 1; i ++) {
                %>
          <option value=<%= i %>
            <% if (endYear == i) out.println(" selected "); %>
            ><%= i %></option>
          <% } %>
    </select>
    </td>
  </tr>
  <tr><td></td></tr>
  <tr>
    <td colspan="2">
      <input type="button" name="<%=lbcancel%>" value="<%=lbcancel%>"
        onclick="submitForm('cancel')">
      <input type="button" name="<%=lbsave%>" value="<%=lbsave%>"
        onclick="submitForm('save')">
    </td>
  </tr>
</table>
</form>

