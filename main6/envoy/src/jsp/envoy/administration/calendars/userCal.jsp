<%@ page contentType="text/html; charset=UTF-8"
         errorPage="/envoy/common/activityError.jsp"
         import="java.util.*, com.globalsight.everest.servlet.util.SessionManager,
                  com.globalsight.everest.webapp.WebAppConstants,
                  com.globalsight.everest.util.comparator.ReservedTimeComparator,
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
                  com.globalsight.calendar.FluxCalendar,
                  com.globalsight.calendar.UserFluxCalendar,
                  com.globalsight.util.GeneralException,
                  com.globalsight.everest.webapp.pagehandler.administration.users.CreateUserWrapper,        
                  com.globalsight.everest.webapp.pagehandler.administration.users.UserUtil,           
                  java.text.MessageFormat,
                  java.util.ArrayList,
                  java.util.Locale,
                  java.util.Calendar,
                  java.util.ResourceBundle"
          session="true"
%>
<style type="text/css">
.day {
    font-famliy:Arial; font-size:12px; font-weight: bold;
}
</style>
<jsp:useBean id="prev" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="cancel" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="done" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="next" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="reservedTimes" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="apply" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="changeDate" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="changeBase" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<%
    ResourceBundle bundle = PageHandler.getBundle(session);
    Locale uiLocale = (Locale)session.getAttribute(WebAppConstants.UILOCALE);
    SessionManager sessionManager = (SessionManager)session.getAttribute(WebAppConstants.SESSION_MANAGER);

    // UI Fields
    String baseCalField = CalendarConstants.BASE_CAL_FIELD;
    String tzField = CalendarConstants.TZ_FIELD;
    String monthField = CalendarConstants.MONTH_FIELD;
    String yearField = CalendarConstants.YEAR_FIELD;
    String bufferField = CalendarConstants.BUFFER_FIELD;

    // Labels, etc
    String title= bundle.getString("msg_new_user_cal");
    String editMsg= bundle.getString("jsmsg_edit_event");

    String lbcancel = bundle.getString("lb_cancel");
    String lbprev = bundle.getString("lb_previous");
    String lbnext = bundle.getString("lb_next");
    String lbapply = bundle.getString("lb_apply");
    String lbdone = bundle.getString("lb_done");
    String lbsave = bundle.getString("lb_save");

    String cancelURL = cancel.getPageURL() + "&action=" + CalendarConstants.CANCEL_ACTION;
    String prevURL = prev.getPageURL() + "&action=" + CalendarConstants.PREVIOUS_ACTION;
    String saveURL = done.getPageURL() + "&action=" + CalendarConstants.SAVE_ACTION + "&userAction=" + CalendarConstants.SAVE_ACTION;
    String nextURL = next.getPageURL() + "&action=" + CalendarConstants.NEXT_ACTION;
    String rtURL = reservedTimes.getPageURL() + "&action=" + CalendarConstants.RESERVED_TIMES_ACTION;
    String applyURL = apply.getPageURL() + "&action=" + CalendarConstants.APPLY_ACTION;
    String changeDateURL = changeDate.getPageURL() + "&action=" + CalendarConstants.CHANGE_DATE_ACTION;
    String changeBaseURL = changeBase.getPageURL() + "&action=" + CalendarConstants.CHANGE_BASE_ACTION;

    // Data if editing calendar
    String selectedCal = (String)request.getAttribute("selectedCal");
    long baseCalId = 0;
    int[] days = (int[])request.getAttribute(CalendarConstants.DAY_STATE);
    int viewMonth, viewYear;
    int schedBuffer = 0;
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
    if (selectedCal != null)
    {
        baseCalId = Long.parseLong(selectedCal);
    }
    String tzId;
    UserFluxCalendar cal = (UserFluxCalendar)
         sessionManager.getAttribute(CalendarConstants.CALENDAR);
    if (cal != null)
    {
        title= bundle.getString("msg_edit_user_calendar");
        baseCalId = cal.getParentCalendarId();
        tzId = cal.getTimeZoneId();
        schedBuffer = cal.getActivityBuffer();
    }
    else
    {
        // Get time zone from default system calendar
        FluxCalendar sysCal =
            (FluxCalendar) sessionManager.getAttribute("sysCal");
        tzId = sysCal.getTimeZoneId();
    }

    String userName = "";
    CreateUserWrapper wrapper;
    
    wrapper = (CreateUserWrapper)sessionManager.getAttribute(WebAppConstants.MODIFY_USER_WRAPPER);
    
    if (wrapper == null)
    {
        wrapper = (CreateUserWrapper)sessionManager.getAttribute(WebAppConstants.CREATE_USER_WRAPPER);
    } 
    
    if (wrapper != null) {
        userName = wrapper.getUserName();
    } else if (cal != null) {
        userName = UserUtil.getUserNameById(cal.getOwnerUserId());
    } else {
    	userName = UserUtil.getUserNameById((String)session.getAttribute(WebAppConstants.USER_NAME));
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
var needWarning = false;
var guideNode="calendars";
var objectName = "<%=bundle.getString("lb_calendar")%>";
var helpFile = "<%=bundle.getString("help_user_calendar_edit")%>";

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
    else if (formAction == "next")
    {
        saveTimes();
        if (confirmForm())
        {
            calForm.action = "<%=nextURL%>";
            calForm.submit();
        }
    }
    else if (formAction == "save")
    {
        saveTimes();
        if (confirmForm())
        {
            calForm.action = "<%=saveURL%>";
            calForm.submit();
        }
    }
    else if (formAction == "apply")
    {
        saveTimes();
        if (confirmForm())
        {
            calForm.action = "<%=applyURL%>";
            calForm.submit();
        }
    }
    else if (formAction == "prev")
    {
        saveTimes();
        calForm.action = "<%=prevURL%>";
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

function isWorkingDay(day)
{
    value = new String(day.value);
    times = value.split(", ");
    for (i = 0; i < times.length; i++)
    {
        if (times[i] > 0) 
        {
            return true;
        }
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
    dataArray[<%=i%>] = "<%=days[i]%>";
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
    var tooltip="<%=editMsg%>";
    myMonth = fBuildCal(iYear, iMonth);
    document.write("<table border='1' class='standardText'>")
    document.write("<tr>");
    document.write("<td align='center' class='day'>" + myMonth[0][0] + "</td>");
    document.write("<td align='center' class='day'>" + myMonth[0][1] + "</td>");
    document.write("<td align='center' class='day'>" + myMonth[0][2] + "</td>");
    document.write("<td align='center' class='day'>" + myMonth[0][3] + "</td>");
    document.write("<td align='center' class='day'>" + myMonth[0][4] + "</td>");
    document.write("<td align='center' class='day'>" + myMonth[0][5] + "</td>");
    document.write("<td align='center' class='day'>" + myMonth[0][6] + "</td>");
    document.write("</tr>");
    for (w = 1; w < 7; w++) {
        document.write("<tr>")
        for (d = 0; d < 7; d++) {
            if (!isNaN(myMonth[w][d])) {
                if (dataArray[myMonth[w][d]] == "<%=CalendarConstants.RESERVEDTIME%>")
                {
                    tdstyle="color:#738EB5;cursor:pointer";
                }
                else if (dataArray[myMonth[w][d]] == "<%=CalendarConstants.NONWORKINGDAY%>")
                {
                    tdstyle="color:red;cursor:pointer";
                }
                else
                {
                    tdstyle="color:black;cursor:pointer";
                }
                document.write("<td title='" + tooltip + "' align='left' valign='top' width='" + iCellWidth + "' height='" + iCellHeight + "' id=calCell style=" + tdstyle + " onclick=\"goToReservedTime('" + myMonth[w][d] + "')\" >");
                document.write("<font id=calDateText  style='font-family:Arial;font-size:" + sDateTextSize + ";font-WEIGHT:" + sDateTextWeight + "' >" + myMonth[w][d] + "</font>");
            } else {
                // not a day in the month
                document.write("<td align='left' valign='top' width='" + iCellWidth + "' height='" + iCellHeight + "' id=calCell >");
                document.write("<font id=calDateText  style='font-family:Arial;font-size:" + sDateTextSize + ";font-WEIGHT:" + sDateTextWeight + "' ></font>");
            }
            document.write("</td>")
        }
        document.write("</tr>");
    }
    document.write("</table>")
}

function goToReservedTime(day)
{
    selected = calForm.<%=monthField%>.selectedIndex;
    month = calForm.<%=monthField%>.options[selected].value;
    monthStr = "";
    switch (parseInt(month))
    {
        case 1:
            monthStr = "<%=bundle.getString("lb_january")%>";
            break;
        case 2:
            monthStr = "<%=bundle.getString("lb_february")%>";
            break;
        case 3:
            monthStr = "<%=bundle.getString("lb_march")%>";
            break;
        case 4:
            monthStr = "<%=bundle.getString("lb_april")%>";
            break;
        case 5:
            monthStr = "<%=bundle.getString("lb_may")%>";
            break;
        case 6:
            monthStr = "<%=bundle.getString("lb_june")%>";
            break;
        case 7:
            monthStr = "<%=bundle.getString("lb_july")%>";
            break;
        case 8:
            monthStr = "<%=bundle.getString("lb_august")%>";
            break;
        case 9:
            monthStr = "<%=bundle.getString("lb_september")%>";
            break;
        case 10:
            monthStr = "<%=bundle.getString("lb_october")%>";
            break;
        case 11:
            monthStr = "<%=bundle.getString("lb_november")%>";
            break;
        case 12:
            monthStr = "<%=bundle.getString("lb_december")%>";
            break;
    }
    selected = calForm.<%=yearField%>.selectedIndex;
    year = calForm.<%=yearField%>.options[selected].value;
    calForm.action = "<%=rtURL%>&monthStr=" + monthStr + "&month=" + month +
                            "&day=" + day + "&year=" + year +
                            "&sorting=" + <%=ReservedTimeComparator.TIME %>;
    saveTimes();
    calForm.submit();
}

var selectedBaseCal = <%=baseCalId%>;

function changeBaseCal(list)
{
    msg = "<%=bundle.getString("jsmsg_base_calendar")%>";
    if (confirm(msg))
    {
        selectedBaseCal = list.selected; 
        calForm.action = "<%=changeBaseURL%>";
        calForm.submit();
    }
    else
    {
        for (var i = 0; i < list.length; i++)
        {
            if (list.options[i].value == selectedBaseCal)
                list.options[i].selected = true;
        }
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
    <p>
    <table cellspacing="0" cellpadding="0" border=0 class="standardText" >
      <tr>
        <td width=500>
          <%=bundle.getString("helper_text_user_calendar")%>
        </td>
      </tr>
    </table>
    <br>


<form name="calForm" method="post" action="">
<input type="hidden" name="formAction" value="">

<table border="0" bordercolor="red" cellspacing="2" cellpadding="2" class="standardText">
  <tr><td></td></tr>
  <tr valign="top">
    <td width="15%">
      <%=bundle.getString("lb_user_name")%>:
    </td>
    <td>
      <%= userName %>
    </td>
  </tr>  
  <tr valign="top">
    <td width="15%">
      <%=bundle.getString("lb_base_calendar")%>:
    </td>
    <td>
      <select name="<%=baseCalField%>" onchange="changeBaseCal(this)">
<%
            FluxCalendar fcal = null;
            ArrayList list = (ArrayList)request.getAttribute("allCals");
            for (int i = 0; i < list.size(); i++)
            {
                fcal = (FluxCalendar)list.get(i);
                long id = fcal.getId();

                out.println("<option value='" + id + "'");
                if (id == baseCalId)
                {
                    out.println(" selected ");
                }
                out.println(">" + fcal.getName() + "</option>");
            }
%>
      </select>
    </td>
    <td rowSpan="2">
        <%=bundle.getString("lb_buffer_for_activity")%>:
        <select name="<%=bufferField%>">
<%
        for (int i = 0; i <= 24; i++)
        {
            out.println("<option value='" + i + "'");
            if (i == schedBuffer)
            {
                out.println(" selected ");
            }
            out.println(">" + i + "</option>");
        }
%>
        </select>
        <%=bundle.getString("lb_hours")%>
    </td>
   </tr>
    <tr>
      <td>
        <%=bundle.getString("lb_time_zone")%>:
      </td>
      <td>
        <select name="<%=tzField%>">
<%
            list = (ArrayList)request.getAttribute("tzs");
            for (int i = 0; i < list.size(); i++)
            {
                String id = (String)list.get(i);

                out.println("<option value='" + id + "'");
                if (TimeZone.getTimeZone(id).getOffset(0) == TimeZone.getTimeZone(tzId).getOffset(0))               {
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
                <td style="background:red">
                    &nbsp;
                </td>
                <td>
                  <%= bundle.getString("lb_non_working_holiday") %>
                </td>
              </tr>
              <tr>
                <td style="background:#738EB5" width="16" height="16">
                    &nbsp;
                </td>
                <td>
                  <%= bundle.getString("lb_event_activity") %>
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
<% if (request.getAttribute("fromCalList") != null)
   {
%>
      <input type="button" name="<%=lbapply%>" value="<%=lbapply%>"
        onclick="submitForm('apply')">
<%
    } else if (request.getAttribute("fromMyAcct") == null &&
               request.getAttribute("fromUserEdit") == null) {
%>
      <input type="button" name="<%=lbprev%>" value="<%=lbprev%>"
        onclick="submitForm('prev')">
<%
    }
    if (request.getAttribute("fromCalList") != null)
    {
%>
      <input type="button" name="<%=lbdone%>" value="<%=lbdone%>"
        onclick="submitForm('save')">
<%
    } else if (request.getAttribute("fromUserEdit") != null ||
               request.getAttribute("fromMyAcct") != null) {
%>
      <input type="button" name="<%=lbdone%>" value="<%=lbdone%>"
        onclick="submitForm('save')">
<%
    } else {
%>
      <input type="button" name="<%=lbnext%>" value="<%=lbnext%>"
        onclick="submitForm('next')">
<%
    }
%>

    </td>
  </tr>
</table>
</form>

<script language="JavaScript">
calForm.<%=monthField%>.options[<%=viewMonth%>].selected = true;
for (i = 0; i < calForm.<%=yearField%>.length; i++)
    if (calForm.<%=yearField%>.options[i].value == <%=viewYear%>)
        calForm.<%=yearField%>.options[i].selected = true;
</script>

