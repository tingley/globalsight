<%@ taglib uri="/WEB-INF/tlds/globalsight.tld" prefix="amb" %>
<%@ page contentType="text/html; charset=UTF-8"
         errorPage="/envoy/common/activityError.jsp"
         import="java.util.*, com.globalsight.everest.servlet.util.SessionManager,
                  com.globalsight.everest.permission.Permission,
                  com.globalsight.everest.webapp.WebAppConstants,
                  com.globalsight.everest.util.comparator.CalendarComparator,
                  com.globalsight.everest.webapp.javabean.NavigationBean,
                  com.globalsight.everest.webapp.pagehandler.PageHandler,
                  com.globalsight.everest.webapp.pagehandler.administration.calendars.CalendarConstants,
                  com.globalsight.everest.webapp.pagehandler.administration.calendars.CalendarHelper,
                  com.globalsight.util.resourcebundle.ResourceBundleConstants,
                  com.globalsight.util.resourcebundle.SystemResourceBundle,
                  com.globalsight.everest.webapp.webnavigation.LinkHelper,
                  com.globalsight.everest.servlet.util.ServerProxy,
                  com.globalsight.everest.servlet.EnvoyServletException,
                  com.globalsight.everest.util.system.SystemConfigParamNames,
                  com.globalsight.everest.util.system.SystemConfiguration,
                  com.globalsight.calendar.FluxCalendar,
                  com.globalsight.util.GeneralException,
                  java.text.MessageFormat,
                  java.util.ArrayList,
                  java.util.Locale,
                  java.util.Calendar,
                  java.util.ResourceBundle"
          session="true"
%>
<jsp:useBean id="apply" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="save" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="cancel" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="holidays1" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="changeDate" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<%
    ResourceBundle bundle = PageHandler.getBundle(session);
    Locale uiLocale = (Locale)session.getAttribute(WebAppConstants.UILOCALE);
    SessionManager sessionManager = (SessionManager)session.getAttribute(WebAppConstants.SESSION_MANAGER);

    // UI Fields
    String nameField = CalendarConstants.NAME_FIELD;
    String tzField = CalendarConstants.TZ_FIELD;
    String defCalField = CalendarConstants.DEF_CAL_FIELD;
    String bizHoursField = CalendarConstants.BIZ_HOURS_FIELD;
    String monthField = CalendarConstants.MONTH_FIELD;
    String yearField = CalendarConstants.YEAR_FIELD;
    String dayField = CalendarConstants.DAY_FIELD;

    // Labels, etc
    String title= bundle.getString("msg_new_sys_cal");

    String lbcancel = bundle.getString("lb_cancel");
    String lbapply = bundle.getString("lb_apply");
    String lbdone = bundle.getString("lb_done");

    String cancelURL = cancel.getPageURL() + "&action=" + CalendarConstants.CANCEL_ACTION;
    String applyURL = apply.getPageURL() + "&action=" + CalendarConstants.APPLY_ACTION;
    String saveURL = save.getPageURL() + "&action=" + CalendarConstants.SAVE_ACTION;
    String holidaysURL = holidays1.getPageURL() + "&action=" + CalendarConstants.CAL_HOLIDAYS_ACTION;
    String changeDateURL = changeDate.getPageURL() + "&action=" + CalendarConstants.CHANGE_DATE_ACTION;

    // Data if editing calendar
    String sysCalName = "";
    String tzId = "";
    int bizHours = 8;
    int[] days = null;
    int viewMonth, viewYear;
    String buf = (String)sessionManager.getAttribute(CalendarConstants.VIEWMONTH);
    if (buf != null)
    {
        viewMonth = Integer.parseInt(buf);
        buf = (String)sessionManager.getAttribute(CalendarConstants.VIEWYEAR);
        viewYear = Integer.parseInt(buf);
    } 
    else
    {
        // default to this month and year
        viewMonth = Calendar.getInstance().get(Calendar.MONTH);
        viewYear = Calendar.getInstance().get(Calendar.YEAR);
    }
    
    FluxCalendar cal = (FluxCalendar)
         sessionManager.getAttribute(CalendarConstants.CALENDAR);
    if (cal != null)
    {
        title= bundle.getString("msg_edit_calendar");
        sysCalName = cal.getName();
        tzId = cal.getTimeZoneId();
        bizHours = cal.getHoursPerDay();
        days = (int[])request.getAttribute(CalendarConstants.DAY_STATE);
    }
    else
    {
        days = new int[32];
        for (int i=0; i < 32; i++)
        {
            days[i] = CalendarConstants.NONWORKINGDAY;
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


<script language="JavaScript">
var needWarning = false;
var guideNode="calendars";
var objectName = "<%=bundle.getString("lb_calendar")%>";
var helpFile = "<%=bundle.getString("help_system_calendar_create_edit")%>";

var existingCals = new Array();
<%
    ArrayList cals = null;
    try {
        cals = (ArrayList)CalendarHelper.getAllCalendars();
    } catch (Exception e) {
    }
    if (cals != null)
    {
        for (int i =0; i < cals.size(); i++)
        {
            FluxCalendar existing = (FluxCalendar) cals.get(i);
%>
            existingCals[<%=i%>] = "<%=existing.getName()%>";
<%
        }
    }
%>

function submitForm(formAction)
{
    calForm.formAction.value = formAction;
    if (formAction == "cancel")
    {
       if (confirmJump())
       {
           calForm.action = "<%=cancelURL%>";
           calForm.submit();
       }
       else
       {
          return false;
       }
    }
    else if (formAction == "apply" || formAction == "done")
    {
        if (saveTimes() == false)
        {
            return;
        }
        if (confirmForm())
        {
            if (formAction == "apply")
            {
                calForm.action = "<%=applyURL%>";
            }
            else 
            {
                calForm.action = "<%=saveURL%>";
            }
            calForm.submit();
        }
    }
    else if (formAction == "holidays")
    {
        saveTimes();
        calForm.action = "<%=holidaysURL%>";
        calForm.submit();
    }
    else if (formAction == "changeDate")
    {
        saveTimes();
        if (confirmForm())
        {
            calForm.action = "<%=changeDateURL%>";
            calForm.submit();
        }
    }
}

//
// Do some validation before submitting
//
function confirmForm()
{
    // Name is a required field
    var theName = calForm.<%=nameField%>.value;
    theName = stripBlanks(theName);

    if (isEmptyString(theName)) {
        alert("<%= bundle.getString("jsmsg_calendar_name") %>");
        calForm.<%=nameField%>.value = "";
        calForm.<%=nameField%>.focus();
        return false;
    }

    if (!isNotLongerThan(theName, 40)) {
        alert("<%= bundle.getString("jsmsg_calendar_len") %>");
        calForm.<%=nameField%>.focus();
        return false;
    }

    if (hasSpecialChars(theName))
    {
        alert("<%= bundle.getString("lb_name") %>" + "<%= bundle.getString("msg_invalid_entry") %>");
        return false;
    }


    <% if (sysCalName == "") { %>
    if (isDupName())
    {
        alert("<%=bundle.getString("msg_duplicate_calendar_name")%>");
        return false;
    }
    <% } %>

    // must have at least 1 working day
    if (isWorkingDay(calForm.<%=CalendarConstants.MON_TIME_FIELD%>)) return true;
    if (isWorkingDay(calForm.<%=CalendarConstants.TUE_TIME_FIELD%>)) return true;
    if (isWorkingDay(calForm.<%=CalendarConstants.WED_TIME_FIELD%>)) return true;
    if (isWorkingDay(calForm.<%=CalendarConstants.THU_TIME_FIELD%>)) return true;
    if (isWorkingDay(calForm.<%=CalendarConstants.FRI_TIME_FIELD%>)) return true;
    if (isWorkingDay(calForm.<%=CalendarConstants.SAT_TIME_FIELD%>)) return true;
    if (isWorkingDay(calForm.<%=CalendarConstants.SUN_TIME_FIELD%>)) return true;
    
    alert("<%= bundle.getString("jsmsg_calendar_times") %>");
    return false;
}

function isDupName()
{
    calname = calForm.<%=nameField%>.value;
    for (i=0; i < existingCals.length; i++)
    {
        if (existingCals[i] == calname)
            return true;
    }
    return false;
}

function isWorkingDay(day)
{
    var value = new String(day.value);
    var times = value.split(", ");
    
    for (i = 0; i < times.length; )
    {
        if ((times[i] > -1 || times[i+1] > 0) && (times[i+2] > -1 || times[i+3] > 0)) 
        {
            return true;
        }
        i += 4;
    }
    return false;
}

var dDate = new Date();
var dCurMonth = dDate.getMonth();
var dCurDayOfMonth = dDate.getDate();
var dCurYear = dDate.getFullYear();
var objPrevElement = new Object();

function fGetDaysInMonth(iMonth, iYear) {
    var dPrevDate = new Date(iYear, iMonth, 0);
    return dPrevDate.getDate();
}

var dataArray = new Array(32);
<%
for (int i= 0; i < 32; i++)
{
%>
    dataArray[<%=i%>] = <%=days[i]%>;
<%
}
%>

var aMonth = new Array();
function fBuildCal(iYear, iMonth) {
    aMonth[0] = new Array(7);
    aMonth[1] = new Array(7);
    aMonth[2] = new Array(7);
    aMonth[3] = new Array(7);
    aMonth[4] = new Array(7);
    aMonth[5] = new Array(7);
    aMonth[6] = new Array(7);
    var dCalDate = new Date(iYear, iMonth-1, 1);
    var iDayOfFirst = dCalDate.getDay();
    var iDaysInMonth = fGetDaysInMonth(iMonth, iYear);
    var iVarDate = 1;
    var i, d, w;
    aMonth[0][0] = '<%=bundle.getString("lb_s")%>';
    aMonth[0][1] = '<%=bundle.getString("lb_m")%>';
    aMonth[0][2] = '<%=bundle.getString("lb_t")%>';
    aMonth[0][3] = '<%=bundle.getString("lb_w")%>';
    aMonth[0][4] = '<%=bundle.getString("lb_th")%>';
    aMonth[0][5] = '<%=bundle.getString("lb_f")%>';
    aMonth[0][6] = '<%=bundle.getString("lb_sa")%>';
    for (d = iDayOfFirst; d < 7; d++) {
        aMonth[1][d] = iVarDate++;
    }
    for (w = 2; w < 7; w++) {
        for (d = 0; d < 7; d++) {
            if (iVarDate <= iDaysInMonth) {
                aMonth[w][d] = iVarDate++;
            }
        }
    }
    return aMonth;
}

function fDrawCal(iYear, iMonth, iCellWidth, iCellHeight, sDateTextSize, sDateTextWeight) {
    var myMonth;
    myMonth = fBuildCal(iYear, iMonth);
    document.write("<table border='1' class='standardText'>")
    document.write("<tr>");
    document.write("<td align='center' style='FONT-FAMILY:Arial;FONT-SIZE:12px;FONT-WEIGHT: bold'>" + myMonth[0][0] + "</td>");
    document.write("<td align='center' style='FONT-FAMILY:Arial;FONT-SIZE:12px;FONT-WEIGHT: bold'>" + myMonth[0][1] + "</td>");
    document.write("<td align='center' style='FONT-FAMILY:Arial;FONT-SIZE:12px;FONT-WEIGHT: bold'>" + myMonth[0][2] + "</td>");
    document.write("<td align='center' style='FONT-FAMILY:Arial;FONT-SIZE:12px;FONT-WEIGHT: bold'>" + myMonth[0][3] + "</td>");
    document.write("<td align='center' style='FONT-FAMILY:Arial;FONT-SIZE:12px;FONT-WEIGHT: bold'>" + myMonth[0][4] + "</td>");
    document.write("<td align='center' style='FONT-FAMILY:Arial;FONT-SIZE:12px;FONT-WEIGHT: bold'>" + myMonth[0][5] + "</td>");
    document.write("<td align='center' style='FONT-FAMILY:Arial;FONT-SIZE:12px;FONT-WEIGHT: bold'>" + myMonth[0][6] + "</td>");
    document.write("</tr>");
    for (w = 1; w < 7; w++) {
        document.write("<tr>")
        for (d = 0; d < 7; d++) {
            if (!isNaN(myMonth[w][d])) {
                if (dataArray[myMonth[w][d]] == "<%=CalendarConstants.NONWORKINGDAY%>")
                {
                    tdstyle="color:red";
                }
                else
                {
                    tdstyle="color:black";
                }
                document.write("<td align='left' valign='top' width='" + iCellWidth + "' height='" + iCellHeight + "' id=calCell style=" + tdstyle + " >");
                document.write("<font id=calDateText  style='FONT-FAMILY:Arial;FONT-SIZE:" + sDateTextSize + ";FONT-WEIGHT:" + sDateTextWeight + "' >" + myMonth[w][d] + "</font>");
            } else {
                document.write("<td align='left' valign='top' width='" + iCellWidth + "' height='" + iCellHeight + "' id=calCell >");
                document.write("<font id=calDateText  style='FONT-FAMILY:Arial;FONT-SIZE:" + sDateTextSize + ";FONT-WEIGHT:" + sDateTextWeight + "' ></font>");
            }
            document.write("</td>")
        }
        document.write("</tr>");
    }
    document.write("</table>")
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




<form name="calForm" method="post" action="">
<input type="hidden" name="formAction" value="">
<input type="hidden" name="calSelectedDate" value="">

<table border="0" bordercolor="red" cellspacing="2" cellpadding="2" class="standardText">
  <tr><td></td></tr>
  <tr valign="top">
    <td width="15%">
      <%=bundle.getString("lb_name")%><span class="asterisk">*</span>:
    </td>
    <td>
      <input name="<%=nameField%>" type="textfield" size="30"
            value="<%=sysCalName%>">
    </td>
    <td>
      <%=bundle.getString("lb_business_hours")%>:
      <select name="<%=bizHoursField%>">
<%
          for (int i = 1; i < 25; i++)
          {
              out.println("<option value='" + i + "'");
              if (bizHours == i)
              {
                  out.println(" selected ");
              }
              out.println(">" + i + "</option>");
          }
%>
       </select>
     </td>
   </tr>
    <tr>
      <td>
        <%=bundle.getString("lb_time_zone")%>:
      </td>
      <td>
        <select name="<%=tzField%>">
<%
            ArrayList list = (ArrayList)request.getAttribute("tzs");
            for (int i = 0; i < list.size(); i++)
            { String id = (String)list.get(i);

                out.println("<option value='" + id + "'");
                if (TimeZone.getTimeZone(id).getOffset(0) == TimeZone.getTimeZone(tzId).getOffset(0))                {
                    out.println(" selected ");
                }
                out.println(">" + id + "  " + bundle.getString(id) + "</option>");
            }
%>
        </select>
      </td>
      <td rowSpan="2">
        &nbsp;
      </td>
        </tr>
        <tr>
          <td>
        <amb:permission name="<%=Permission.HOLIDAY_EDIT%>" >
            <a href="javascript:submitForm('holidays')"><%=bundle.getString("lb_holidays...")%></a>
        </amb:permission>
          </td>
          <td>&nbsp;</td>
        </tr>
    </td>
  <tr>
    <td colspan="2">
      <table border="0" bordercolor="blue" class="standardText">
        <tr>
          <td>
            <select name="<%=monthField%>" onchange="submitForm('changeDate')" >
              <option value="0"><%=bundle.getString("lb_january")%></option>
              <option value="1"><%=bundle.getString("lb_february")%></option>
              <option value="2"><%=bundle.getString("lb_march")%></option>
              <option value="3"><%=bundle.getString("lb_april")%></option>
              <option value="4"><%=bundle.getString("lb_may")%></option>
              <option value="5"><%=bundle.getString("lb_june")%></option>
              <option value="6"><%=bundle.getString("lb_july")%></option>
              <option value="7"><%=bundle.getString("lb_august")%></option>
              <option value="8"><%=bundle.getString("lb_september")%></option>
              <option value="9"><%=bundle.getString("lb_october")%></option>
              <option value="10"><%=bundle.getString("lb_november")%></option>
              <option value="11"><%=bundle.getString("lb_december")%></option>
            </select>
      
            <select name="<%=yearField%>" onchange="submitForm('changeDate')" >
            <% 	Calendar cale = Calendar.getInstance();
                int year = cale.get(Calendar.YEAR);
                for (int i = 2004; i <= year + 1; i ++) {
                %>
              <option value="<%= i %>"><%= i %></option>
              <% } %>
            </select>
          </td>
        </tr>
        <tr>
          <td>
            <script language="JavaScript">
            fDrawCal(<%=viewYear%>, <%=viewMonth%>+1, 20, 20, "12px", "bold", 1);
            </script>
          </td>
          <td valign="top">
            <%= bundle.getString("lb_legend") %>:
            <table border="0" class="standardText">
              <tr>
                <td style="background:red" width="18" height="14">
                    &nbsp;
                </td>
                <td>
                  <%= bundle.getString("lb_non_working_holiday") %>
                </td>
              </tr>
            </table>
           </td>
        </tr>
      </table>
    </td>
    <td>
      <jsp:include page="workingHours.jsp" />
    </td>
  </tr>
  <tr><td></td></tr>
  <tr>
    <td colspan="2">
      <input type="button" name="<%=lbcancel%>" value="<%=lbcancel%>"
        onclick="submitForm('cancel')">
      <input type="button" name="<%=lbapply%>" value="<%=lbapply%>"
        onclick="submitForm('apply')">
      <input type="button" name="<%=lbdone%>" value="<%=lbdone%>"
        onclick="submitForm('done')">
    </td>
  </tr>
</table>
</form>

<script>
calForm.<%=monthField%>.options[<%=viewMonth%>].selected = true;
for (i = 0; i < calForm.<%=yearField%>.length; i++)
    if (calForm.<%=yearField%>.options[i].value == <%=viewYear%>)
        calForm.<%=yearField%>.options[i].selected = true;
</script>

