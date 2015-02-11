<%@ page contentType="text/html; charset=UTF-8"
         errorPage="/envoy/common/activityError.jsp"
         import="java.util.*, com.globalsight.everest.servlet.util.SessionManager,
                  com.globalsight.everest.webapp.WebAppConstants,
                  com.globalsight.everest.util.comparator.WorkflowTemplateInfoComparator,
                  com.globalsight.everest.webapp.javabean.NavigationBean,
                  com.globalsight.everest.webapp.pagehandler.PageHandler,
                  com.globalsight.everest.webapp.pagehandler.administration.calendars.CalendarConstants,
                  com.globalsight.everest.webapp.pagehandler.administration.calendars.HolidayConstants,
                  com.globalsight.util.resourcebundle.ResourceBundleConstants,
                  com.globalsight.util.resourcebundle.SystemResourceBundle,
                  com.globalsight.everest.webapp.webnavigation.LinkHelper,
                  com.globalsight.everest.servlet.util.ServerProxy,
                  com.globalsight.everest.servlet.EnvoyServletException,
                  com.globalsight.everest.util.system.SystemConfigParamNames,
                  com.globalsight.everest.util.system.SystemConfiguration,
                  com.globalsight.calendar.Holiday,
                  com.globalsight.util.GeneralException,
                  java.text.MessageFormat,
                  java.util.Calendar,
                  java.util.Locale,
                  java.util.ResourceBundle"
          session="true"
%>
<jsp:useBean id="cancel" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="save" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<%
    ResourceBundle bundle = PageHandler.getBundle(session);
    Locale uiLocale = (Locale)session.getAttribute(WebAppConstants.UILOCALE);
    SessionManager sessionManager = (SessionManager)session.getAttribute(WebAppConstants.SESSION_MANAGER);

    // UI fields
    String nameField = HolidayConstants.NAME;
    String descField = HolidayConstants.DESC;
    String month1Field = HolidayConstants.MONTH1;
    String dayField = HolidayConstants.DAY;
    String whenField = HolidayConstants.WHEN;
    String dayofweekField = HolidayConstants.DAY_OF_WEEK;
    String month2Field = HolidayConstants.MONTH2;
    String yearField = HolidayConstants.YEAR;
    String startField = HolidayConstants.START;
    String endField = HolidayConstants.END;

    // Labels, etc
    String title= bundle.getString("msg_new_holiday");
    String lbcancel = bundle.getString("lb_cancel");
    String lbsave = bundle.getString("lb_save");

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

    String daysOfWeek[] = new String[7];
    daysOfWeek[0] = bundle.getString("lb_sunday");
    daysOfWeek[1] = bundle.getString("lb_monday");
    daysOfWeek[2] = bundle.getString("lb_tuesday");
    daysOfWeek[3] = bundle.getString("lb_wednesday");
    daysOfWeek[4] = bundle.getString("lb_thursday");
    daysOfWeek[5] = bundle.getString("lb_friday");
    daysOfWeek[6] = bundle.getString("lb_saturday");

    String first = bundle.getString("lb_first");
    String second = bundle.getString("lb_second");
    String third = bundle.getString("lb_third");
    String fourth = bundle.getString("lb_fourth");
    String last = bundle.getString("lb_last");
    String of = bundle.getString("lb_of");

    String cancelURL = cancel.getPageURL() + "&action=" +
               CalendarConstants.CANCEL_ACTION;
    String saveURL = save.getPageURL() + "&action=" +
               CalendarConstants.SAVE_ACTION;

    
    String badDate = bundle.getString("jsmsg_invalid_date");

    // Data if editing holiday
    String holidayName = "";
    String desc = "";
    int dayOfMonth = 0;
    int dayOfWeek = 0;
    int endYear = 0;
    boolean isAbsolute = true;
    int month = 0;
    String weekOfMonth = null;
    Holiday holiday = (Holiday) request.getAttribute("holiday");
    if (holiday != null) 
    {
        title= bundle.getString("msg_edit_holiday");
        holidayName = holiday.getName();
        desc = holiday.getDescription();
        if (desc == null) desc = "";
        month = holiday.getMonth();
        endYear = holiday.getEndingYear().intValue();
        if (holiday.isAbsolute() == true)
        {
            dayOfMonth =  holiday.getDayOfMonth();
        }
        else
        {
            isAbsolute = false;
            weekOfMonth = holiday.getWeekOfMonth();
            dayOfWeek = holiday.getDayOfWeek().intValue();
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
var needWarning = false;
var guideNode="calendars";
var objectName = "<%=bundle.getString("lb_holiday")%>";
var helpFile = "<%=bundle.getString("help_system_holiday_create_edit")%>";

function submitForm(formAction)
{
    holidayForm.formAction.value = formAction;
    if (formAction == "cancel")
    {
       if (confirmJump())
       {
           holidayForm.action = "<%=cancelURL%>";
           holidayForm.submit();
       }
       else
       {
          return false;
       }
    }
    else
    {
        if (confirmForm()) 
        {
            holidayForm.action = "<%=saveURL%>";
            holidayForm.submit();
        }
    }
}

//
// Check required fields and length of desc field.
//
function confirmForm()
{
    var theName = holidayForm.<%=nameField%>.value;
    theName = stripBlanks(theName);

    if (isEmptyString(theName)) {
        alert("<%= bundle.getString("jsmsg_holiday_name") %>");
        holidayForm.<%=nameField%>.value = "";
        holidayForm.<%=nameField%>.focus();
        return false;
    }

    var iChars = "!@#$%^&*()+=[]\\;,./{}|\":<>?";
    for (var i = 0; i < theName.length; i++)
    {
        if (iChars.indexOf(theName.charAt(i)) != -1)
        {
            alert("<%= bundle.getString("lb_name") %>" + "<%= bundle.getString("msg_invalid_entry2") %>");
            return false;
        }
    }

    if (!isNotLongerThan(theName, 40))
    {
        alert("<%= bundle.getString("jsmsg_holiday_len") %>");
        holidayForm.<%=nameField%>.focus();
        return false;
    }

    if (!isNotLongerThan(holidayForm.<%=descField%>.value, 200))
    {
        alert("<%= bundle.getString("jsmsg_description_4000") %>");
        holidayForm.<%=descField%>.focus();
        return false;
    }
    if (holidayForm.<%=endField%>[1].checked == true)
    {
        if (validateYear(holidayForm.<%=yearField%>, holidayForm.<%=yearField%>.selectedIndex) == false)
        {
            return false;
        }
    }
    return true;
}

var today = new Date();
var yearSelected = today.getFullYear();
var daySelected = <%=dayOfMonth%>;

// Update global day variable when user changes day
function updateDay()
{
    df = holidayForm.<%=dayField%>;
    daySelected =  df.options[df.options.selectedIndex].value;
}

//
// Depending on the month, the number of days changes
//
function updateDayList(obj, selectIndex)
{
    var form = document.holidayForm;
    var d = new Date(yearSelected, parseInt(obj.options[selectIndex].value)+1, 0);
    var daysInMonth = d.getDate();
    for (var i = 1; i <= form.<%=dayField%>.length; i++) {
        form.<%=dayField%>.options[i-1] = null;
    }
    for (var i = 1; i <= daysInMonth; i++) {
        form.<%=dayField%>.options[i-1] = new Option(i,i);
    }
    if (daySelected > 0 && daySelected <= daysInMonth)
    {
        form.<%=dayField%>.options[daySelected-1].selected = true;
    }
}

//
// Update the Non-working day radio button.
// If a user selects any new option, the radio
// button to the right of it will automatically
// get selected if it isn't already.
//
// Also disable/enable end date radio button according to the 
// non working day selection
function updateRadio(index)
{
    holidayForm.<%=startField%>[index].checked = true;
    if (index == 1)
    {
        // Disable end date radio buttons because it doesn't make sense
        // to have an end date for something like "first monday in feb".
        // Automatically select no end date.
        holidayForm.<%=endField%>[0].checked = true;
        holidayForm.<%=endField%>[0].disabled = true;
        holidayForm.<%=endField%>[1].disabled = true;
    }
    else
    {
        // make sure end date radio buttons are enabled
        holidayForm.<%=endField%>[0].disabled = false;
        holidayForm.<%=endField%>[1].disabled = false;
    }
}

function getSelectedRadio(group)
{
    for (var i =0; i < group.length; i++)
    {
        if (group[i].checked)
        {
            return i;
        }
    }
}


//
// If user changed year, make sure the selected month/day
// is still valid.
//
function validateYear(obj, selectIndex)
{
    var form = document.holidayForm;
    // update the global variable for yearSelected
    yearSelected = form.<%=yearField%>.options[form.<%=yearField%>.selectedIndex].value;

    // automatically select radio button if it isn't selected
    form.<%=endField%>[1].checked = true;

    // now validate if the date exists
    var index = getSelectedRadio(form.<%=startField%>);
    if (index >= 0 && form.<%=startField%>[index].value == "day")
    {
        var d = new Date(yearSelected, parseInt(form.<%=month1Field%>.options[form.<%=month1Field%>.selectedIndex].value)+1, 0);
        if (form.<%=dayField%>.value > d.getDate())
        {
            alert("<%=badDate%>");
            return false;

        }
    }
    // update the date drop down
    month = holidayForm.<%=month1Field%>;
    selected = month.selectedIndex;
    updateDayList(month, selected);

    return true;
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
    <br>
    <table cellspacing="0" cellpadding="0" border=0 class="standardText" >
      <tr>
        <td width=500>
          <%=bundle.getString("helper_text_holiday")%>
        </td>
      </tr>
    </table>
    <br>


<form name="holidayForm" method="post" action="">
<input type="hidden" name="formAction" value="">
<% if (holiday != null) { %>
<input type="hidden" name="edit" >
<% } %>

<table border="0" cellspacing="2" cellpadding="2" class="standardText">
  <tr><td></td></tr>
  <tr valign="top">
    <td>
      <table border="0" class="standardText">
        <tr>
          <td>
            <%=bundle.getString("lb_name")%><span class="asterisk">*</span>:
          </td>
        </tr>
        <tr>
          <td>
            <input type="textfield" name="<%=nameField%>" size="30" 
                value="<%=holidayName%>">
          </td>
        </tr>
        <tr>
          <td>
            <%=bundle.getString("lb_description")%>:
          </td>
        </tr>
        <tr>
          <td>
            <textarea rows="3" cols="30" name="<%=descField%>"><%=desc%></textarea>
          </td>
        </tr>
        <tr><td>&nbsp;</td></tr>
        <tr>
          <td>
          <fieldset>
            <legend><%=bundle.getString("lb_non_working_day")%> </legend>
            <table border="0" class="standardText">
              <tr>
                <td>
                <input type="radio" name="<%=startField%>" value="day" onclick="updateRadio(0)" 
                <% if (isAbsolute) out.println(" checked "); %>
                >
                <select name="<%=month1Field%>" onChange="updateDayList(this, this.selectedIndex);updateRadio(0)">
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
                <select name="<%=dayField%>" onChange="updateRadio(0);updateDay()" >
<%
                  for (int i = 1; i <= 31; i++) 
                  {
                    out.println("<option value='" + i + "'");
                    if (dayOfMonth == i)
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
                  <input type="radio" name="<%=startField%>" value="dayVaries"  onclick="updateRadio(1)"
                  <% if (!isAbsolute) out.println(" checked "); %>
                    >
                    <select name="<%=whenField%>" onChange="updateRadio(1)" >
                      <option value="1"
                      <% if ("1".equals(weekOfMonth)) out.println(" selected "); %>
                      ><%=first%></option>
                      <option value="2"
                      <% if ("2".equals(weekOfMonth)) out.println(" selected "); %>
                      ><%=second%></option>
                      <option value="3"
                      <% if ("3".equals(weekOfMonth)) out.println(" selected "); %>
                      ><%=third%></option>
                      <option value="4"
                      <% if ("4".equals(weekOfMonth)) out.println(" selected "); %>
                      ><%=fourth%></option>
                      <option value="$"
                      <% if ("$".equals(weekOfMonth)) out.println(" selected "); %>
                      ><%=last%></option>
                    </select>
                    <select name="<%=dayofweekField%>" onChange="updateRadio(1)">
<%
                      int j;
                      for (int i = 0; i < daysOfWeek.length; i++) 
                      {
                        j = i+1;
                        out.println("<option value='" + j + "'");
                        if (dayOfWeek == i+1)
                        {
                            out.println(" selected ");
                        }
                        out.println(">" + daysOfWeek[i] + "</option>");
                      }
%>
                    </select>
                    &nbsp;<%=of%>&nbsp; 
                    <select name="<%=month2Field%>" onChange="updateRadio(1)">
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
                </td>
              </tr>
            </table>
          </fieldset>
          </td>
        </tr>
        <tr><td>&nbsp;</td></tr>
        <tr>
          <td>
            <%=bundle.getString("lb_end_date")%>:
          </td>
        </tr>
        <tr>
          <td style="padding-left:30px">
            <input type="radio" name="<%=endField%>" value="none"
            <% if (endYear == 0) out.println(" checked "); %>
            >&nbsp;<%= bundle.getString("lb_no_end_date") %>
          </td>
        </tr>
        <tr>
          <td style="padding-left:30px">
            <input type="radio" name="<%=endField%>" value="year"
            <% if (endYear != 0)
               {
                  out.println(" checked ");
               }
               else
               {
                  endYear = Calendar.getInstance().get(Calendar.YEAR);
               }
             %>
            >&nbsp;<%=bundle.getString("lb_only")%>
            <select name="<%=yearField%>" onChange="validateYear(this, this.selectedIndex)">
            <% 	Calendar cale = Calendar.getInstance();
                int year = cale.get(Calendar.YEAR);
                for (int i = 2004; i <= year + 1; i ++) {
            %>
              <option value="<%= i %>"
                <% if (endYear == i) out.println(" selected "); %>
                ><%= i %></option>
            <% } %>   
            </select>
          </td>
        </tr>
        <tr><td></td></tr>
        <tr>
          <td>
          <table border="0" bordercolor="red">
            <tr>
              <td>
          
              </td>
            </tr>
          </table>
        </td>
      </tr>
      <tr><td></td></tr>
      <tr>
        <td>
          <input type="button" name="<%=lbcancel%>" value="<%=lbcancel%>"
            onclick="submitForm('cancel')">
          <input type="button" name="<%=lbsave%>" value="<%=lbsave%>"
            onclick="submitForm('save')">
        </td>
      </tr>
    </table>
<script>
month = holidayForm.<%=month1Field%>;
selected = month.selectedIndex;
updateDayList(month, selected);
<% if (!isAbsolute) { %>
updateRadio(1);
<% } %>
</script>
</form>

