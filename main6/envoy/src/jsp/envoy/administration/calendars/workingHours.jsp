<%@ page contentType="text/html; charset=UTF-8"
         errorPage="/envoy/common/activityError.jsp"
         import="com.globalsight.everest.servlet.util.SessionManager,
                  com.globalsight.everest.webapp.WebAppConstants,
                  com.globalsight.everest.webapp.pagehandler.PageHandler,
                  com.globalsight.everest.webapp.pagehandler.administration.calendars.CalendarConstants,
                  com.globalsight.calendar.BaseFluxCalendar,
                  com.globalsight.calendar.WorkingDay,
                  com.globalsight.calendar.WorkingHour,
                  java.util.ArrayList,
                  java.util.List,
                  java.util.Locale,
                  java.util.ResourceBundle"
          session="true"
%>
<%
    ResourceBundle bundle = PageHandler.getBundle(session);
    Locale uiLocale = (Locale)session.getAttribute(WebAppConstants.UILOCALE);
    SessionManager sessionManager = (SessionManager)session.getAttribute(WebAppConstants.SESSION_MANAGER);

    // UI Fields
    String from1Field = CalendarConstants.FROM1_FIELD;
    String from2Field = CalendarConstants.FROM2_FIELD;
    String from3Field = CalendarConstants.FROM3_FIELD;
    String from4Field = CalendarConstants.FROM4_FIELD;
    String from5Field = CalendarConstants.FROM5_FIELD;
    String to1Field = CalendarConstants.TO1_FIELD;
    String to2Field = CalendarConstants.TO2_FIELD;
    String to3Field = CalendarConstants.TO3_FIELD;
    String to4Field = CalendarConstants.TO4_FIELD;
    String to5Field = CalendarConstants.TO5_FIELD;
    String frommin1Field = CalendarConstants.FROMMIN1_FIELD;
    String frommin2Field = CalendarConstants.FROMMIN2_FIELD;
    String frommin3Field = CalendarConstants.FROMMIN3_FIELD;
    String frommin4Field = CalendarConstants.FROMMIN4_FIELD;
    String frommin5Field = CalendarConstants.FROMMIN5_FIELD;
    String tomin1Field = CalendarConstants.TOMIN1_FIELD;
    String tomin2Field = CalendarConstants.TOMIN2_FIELD;
    String tomin3Field = CalendarConstants.TOMIN3_FIELD;
    String tomin4Field = CalendarConstants.TOMIN4_FIELD;
    String tomin5Field = CalendarConstants.TOMIN5_FIELD;
    String monTimeField = CalendarConstants.MON_TIME_FIELD;
    String tueTimeField = CalendarConstants.TUE_TIME_FIELD;
    String wedTimeField = CalendarConstants.WED_TIME_FIELD;
    String thuTimeField = CalendarConstants.THU_TIME_FIELD;
    String friTimeField = CalendarConstants.FRI_TIME_FIELD;
    String satTimeField = CalendarConstants.SAT_TIME_FIELD;
    String sunTimeField = CalendarConstants.SUN_TIME_FIELD;
    String dayField = CalendarConstants.DAY_FIELD;

    String daysOfWeek[] = new String[7];
    daysOfWeek[0] = bundle.getString("lb_sunday");
    daysOfWeek[1] = bundle.getString("lb_monday");
    daysOfWeek[2] = bundle.getString("lb_tuesday");
    daysOfWeek[3] = bundle.getString("lb_wednesday");
    daysOfWeek[4] = bundle.getString("lb_thursday");
    daysOfWeek[5] = bundle.getString("lb_friday");
    daysOfWeek[6] = bundle.getString("lb_saturday");

    String hours[] = new String[24];
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

    BaseFluxCalendar cal = (BaseFluxCalendar)
         sessionManager.getAttribute(CalendarConstants.CALENDAR);
    if (cal == null)
    {
        // see if using default system cal
        cal = (BaseFluxCalendar) sessionManager.getAttribute("sysCal");
    }
    String noTimes = "-1, 0, -1, 0, -1, 0, -1, 0, -1, 0, -1, 0, -1, 0, -1, 0, -1, 0, -1, 0";
    String monTime = noTimes;
    String tueTime = noTimes;
    String wedTime = noTimes;
    String thuTime = noTimes;
    String friTime = noTimes;
    String satTime = noTimes;
    String sunTime = noTimes;
    List workingDays = new ArrayList();
    // Data if editing calendar
    if (cal != null)
    {
        workingDays = cal.getWorkingDays();
        for (int i = 0; i < workingDays.size(); i++)
        {
            String buf = "";
            WorkingDay wd = (WorkingDay) workingDays.get(i);
            if (wd.getCalendarAssociationState() !=
               com.globalsight.calendar.CalendarConstants.DELETED)
            {
               for (int j = 1; j < 7; j++)
               {
                   if (j != 1)
                   {
                       buf += ", ";
                   }
                   WorkingHour wh = wd.getWorkingHourByOrder(j);
                   if (wh != null)
                   {
                       buf +=  wh.getStartHour() + ", " + wh.getStartMinute() +
                           ", " + wh.getEndHour() + ", " + wh.getEndMinute();
                   }
                   else
                   {
                       buf += "-1, 0, -1, 0";
                   }
               }
               switch (wd.getDay())
               {
                   case 1:
                       sunTime = buf;
                       break;
                   case 2:
                       monTime = buf;
                       break;
                   case 3:
                       tueTime = buf;
                       break;
                   case 4:
                       wedTime = buf;
                       break;
                   case 5:
                       thuTime = buf;
                       break;
                   case 6:
                       friTime = buf;
                       break;
                   case 7:
                       satTime = buf;
                       break;
               }
            }
        }
    }
%>


<script>
var selectedDay = 0;

//
// Update the From and To fields according to the selected day.
//
function updateTimes()
{
    form = document.calForm;
    var timeStr;
    switch (selectedDay)
    {
        case 0:
            timeStr = new String(form.<%=monTimeField%>.value);
            break;
        case 1:
            timeStr = new String(form.<%=tueTimeField%>.value);
            break;
        case 2:
            timeStr = new String(form.<%=wedTimeField%>.value);
            break;
        case 3:
            timeStr = new String(form.<%=thuTimeField%>.value);
            break;
        case 4:
            timeStr = new String(form.<%=friTimeField%>.value);
            break;
        case 5:
            timeStr = new String(form.<%=satTimeField%>.value);
            break;
        case 6:
            timeStr = new String(form.<%=sunTimeField%>.value);
            break;
    }
    times = timeStr.split(", ");
    form.<%=from1Field%>.selectedIndex = parseInt(times[0])+1; 
    form.<%=frommin1Field%>.selectedIndex = times[1]; 
    form.<%=to1Field%>.selectedIndex = parseInt(times[2])+1;
    form.<%=tomin1Field%>.selectedIndex = times[3];
    form.<%=from2Field%>.selectedIndex = parseInt(times[4])+1;
    form.<%=frommin2Field%>.selectedIndex = times[5];
    form.<%=to2Field%>.selectedIndex = parseInt(times[6])+1;
    form.<%=tomin2Field%>.selectedIndex = times[7];
    form.<%=from3Field%>.selectedIndex = parseInt(times[8])+1;
    form.<%=frommin3Field%>.selectedIndex = times[9];
    form.<%=to3Field%>.selectedIndex = parseInt(times[10])+1;
    form.<%=tomin3Field%>.selectedIndex = times[11];
    form.<%=from4Field%>.selectedIndex = parseInt(times[12])+1;
    form.<%=frommin4Field%>.selectedIndex = times[13];
    form.<%=to4Field%>.selectedIndex = parseInt(times[14])+1;
    form.<%=tomin4Field%>.selectedIndex = times[15];
    form.<%=from5Field%>.selectedIndex = parseInt(times[16])+1;
    form.<%=frommin5Field%>.selectedIndex = times[17];
    form.<%=to5Field%>.selectedIndex = parseInt(times[18])+1;
    form.<%=tomin5Field%>.selectedIndex = times[19];
}

//
// Validate that if a from is set, that a to is set and
// vice versa.
// Save the current times for the previously selected day.
// The option values are 1-7, (sun-sat).
//
function saveTimes() 
{
    var buf = "";
    form = document.calForm;
<%
    for (int i = 1; i <= 5; i++)
    {
        String fromhour = "from" + i + "Field";
        String frommin = "frommin" + i + "Field";
        String tohour = "to" + i + "Field";
        String tomin = "tomin" + i + "Field";
%>
        fromHour =
           parseInt(form.<%=fromhour%>.options[form.<%=fromhour%>.selectedIndex].value);
        fromMin =
           parseInt(form.<%=frommin%>.options[form.<%=frommin%>.selectedIndex].value);
        toHour =
           parseInt(form.<%=tohour%>.options[form.<%=tohour%>.selectedIndex].value);
        toMin =
           parseInt(form.<%=tomin%>.options[form.<%=tomin%>.selectedIndex].value);
        if (fromHour > toHour ||
            (fromHour == toHour && fromMin > toMin) || 
            (fromHour == toHour && fromMin == toMin &&
             fromHour != -1 && fromMin != -1))
        {
            alert("<%=bundle.getString("jsmsg_time")%>");
            form.<%=dayField%>.selectedIndex = selectedDay; 
            return false;
        }
        buf += fromHour + ", " + fromMin + ", " + toHour + ", " + toMin + ", ";
            
<%
    }
%>
    switch (selectedDay)
    {
        case 0:
            form.<%=monTimeField%>.value = buf;
            break;
        case 1:
            form.<%=tueTimeField%>.value = buf;
            break;
        case 2:
            form.<%=wedTimeField%>.value = buf;
            break;
        case 3:
            form.<%=thuTimeField%>.value = buf;
            break;
        case 4:
            form.<%=friTimeField%>.value = buf;
            break;
        case 5:
            form.<%=satTimeField%>.value = buf;
            break;
        case 6:
            form.<%=sunTimeField%>.value = buf;
            break;
    }

    selectedDay = form.<%=dayField%>.selectedIndex;
    return true;

}

</script>

      <fieldset>
        <legend><%=bundle.getString("lb_working_hours")%></legend>
          <table class="standardText">
          <tr>
            <td>
              <%=bundle.getString("lb_working_day")%>:
              <select name="<%=dayField%>" onChange="saveTimes(); updateTimes();">
<%
                      int j;
                      for (int i = 1; i < daysOfWeek.length; i++)
                      {
                        j = i+1;
                        out.println("<option value='" + j + "'>" + daysOfWeek[i] + "</option>");
                      }
                      out.println("<option value='" + 1 + "'>" + daysOfWeek[0] + "</option>");
%>
              </select>
            </td>
          </tr>
          <tr>
            <td nowrap>
              <%=bundle.getString("lb_from")%>:
              <select name="<%=from1Field%>">
                <option value="-1">&nbsp;</option>
<%
                for (int i = 0; i < hours.length; i++)
                {
                    out.println("<option value='" + i + "'");
                    out.println(">" + hours[i] + "</option>");
                }
%>
              </select>
              <select name="<%=frommin1Field%>">
                <option value="0">:00</option>
                <option value="1">:15</option>
                <option value="2">:30</option>
                <option value="3">:45</option>
              </select>
              &nbsp;<%=bundle.getString("lb_to") %>:
              <select name="<%=to1Field%>">
                <option value="-1">&nbsp;</option>
<%
                for (int i = 0; i < hours.length; i++)
                {
                    out.println("<option value='" + i + "'");
                    out.println(">" + hours[i] + "</option>");
                }
%>
              </select>
              <select name="<%=tomin1Field%>">
                <option value="0">:00</option>
                <option value="1">:15</option>
                <option value="2">:30</option>
                <option value="3">:45</option>
              </select>
            </td>
          </tr>
          <tr>
            <td nowrap>
              <%=bundle.getString("lb_from")%>:
              <select name="<%=from2Field%>">
                <option value="-1">&nbsp;</option>
<%
                for (int i = 0; i < hours.length; i++)
                {
                    out.println("<option value='" + i + "'");
                    out.println(">" + hours[i] + "</option>");
                }
%>
              </select>
              <select name="<%=frommin2Field%>">
                <option value="0">:00</option>
                <option value="1">:15</option>
                <option value="2">:30</option>
                <option value="3">:45</option>
              </select>
              &nbsp;<%=bundle.getString("lb_to") %>:
              <select name="<%=to2Field%>">
                <option value="-1">&nbsp;</option>
<%
                for (int i = 0; i < hours.length; i++)
                {
                    out.println("<option value='" + i + "'");
                    out.println(">" + hours[i] + "</option>");
                }
%>
              </select>
              <select name="<%=tomin2Field%>">
                <option value="0">:00</option>
                <option value="1">:15</option>
                <option value="2">:30</option>
                <option value="3">:45</option>
              </select>
            </td>
          </tr>
          <tr>
            <td nowrap>
              <%=bundle.getString("lb_from")%>:
              <select name="<%=from3Field%>">
                <option value="-1">&nbsp;</option>
<%
                for (int i = 0; i < hours.length; i++)
                {
                    out.println("<option value='" + i + "'");
                    out.println(">" + hours[i] + "</option>");
                }
%>
              </select>
              <select name="<%=frommin3Field%>">
                <option value="0">:00</option>
                <option value="1">:15</option>
                <option value="2">:30</option>
                <option value="3">:45</option>
              </select>
              &nbsp;<%=bundle.getString("lb_to") %>:
              <select name="<%=to3Field%>">
                <option value="-1">&nbsp;</option>
<%
                for (int i = 0; i < hours.length; i++)
                {
                    out.println("<option value='" + i + "'");
                    out.println(">" + hours[i] + "</option>");
                }
%>
              </select>
              <select name="<%=tomin3Field%>">
                <option value="0">:00</option>
                <option value="1">:15</option>
                <option value="2">:30</option>
                <option value="3">:45</option>
              </select>
            </td>
          </tr>
          <tr>
            <td nowrap>
              <%=bundle.getString("lb_from")%>:
              <select name="<%=from4Field%>">
                <option value="-1">&nbsp;</option>
<%
                for (int i = 0; i < hours.length; i++)
                {
                    out.println("<option value='" + i + "'");
                    out.println(">" + hours[i] + "</option>");
                }
%>
              </select>
              <select name="<%=frommin4Field%>">
                <option value="0">:00</option>
                <option value="1">:15</option>
                <option value="2">:30</option>
                <option value="3">:45</option>
              </select>
              &nbsp;<%=bundle.getString("lb_to") %>:
              <select name="<%=to4Field%>">
                <option value="-1">&nbsp;</option>
<%
                for (int i = 0; i < hours.length; i++)
                {
                    out.println("<option value='" + i + "'");
                    out.println(">" + hours[i] + "</option>");
                }
%>
              </select>
              <select name="<%=tomin4Field%>">
                <option value="0">:00</option>
                <option value="1">:15</option>
                <option value="2">:30</option>
                <option value="3">:45</option>
              </select>
            </td>
          </tr>
          <tr>
            <td nowrap>
              <%=bundle.getString("lb_from")%>:
              <select name="<%=from5Field%>">
                <option value="-1">&nbsp;</option>
<%
                for (int i = 0; i < hours.length; i++)
                {
                    out.println("<option value='" + i + "'");
                    out.println(">" + hours[i] + "</option>");
                }
%>
              </select>
              <select name="<%=frommin5Field%>">
                <option value="0">:00</option>
                <option value="1">:15</option>
                <option value="2">:30</option>
                <option value="3">:45</option>
              </select>
              &nbsp;<%=bundle.getString("lb_to") %>:
              <select name="<%=to5Field%>">
                <option value="-1">&nbsp;</option>
<%
                for (int i = 0; i < hours.length; i++)
                {
                    out.println("<option value='" + i + "'");
                    out.println(">" + hours[i] + "</option>");
                }
%>
              </select>
              <select name="<%=tomin5Field%>">
                <option value="0">:00</option>
                <option value="1">:15</option>
                <option value="2">:30</option>
                <option value="3">:45</option>
              </select>
            </td>
          </tr>
        </table>
      </fieldset>
<input type="hidden" name="<%=monTimeField%>" value="<%=monTime%>">
<input type="hidden" name="<%=tueTimeField%>" value="<%=tueTime%>">
<input type="hidden" name="<%=wedTimeField%>" value="<%=wedTime%>">
<input type="hidden" name="<%=thuTimeField%>" value="<%=thuTime%>">
<input type="hidden" name="<%=friTimeField%>" value="<%=friTime%>">
<input type="hidden" name="<%=satTimeField%>" value="<%=satTime%>">
<input type="hidden" name="<%=sunTimeField%>" value="<%=sunTime%>">

<script>updateTimes()</script>


