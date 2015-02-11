<%@ page contentType="text/html; charset=UTF-8"
		errorPage="/envoy/common/error.jsp"
		import="java.util.*,com.globalsight.everest.webapp.javabean.NavigationBean,
         com.globalsight.calendar.Holiday,
         com.globalsight.everest.servlet.util.SessionManager,
         com.globalsight.everest.servlet.EnvoyServletException,
         com.globalsight.everest.projecthandler.WorkflowTemplateInfo,
         com.globalsight.everest.webapp.pagehandler.PageHandler, 
         com.globalsight.everest.webapp.WebAppConstants,
         com.globalsight.everest.webapp.pagehandler.administration.calendars.CalendarConstants,
         com.globalsight.everest.webapp.pagehandler.administration.calendars.HolidayConstants,
         com.globalsight.util.resourcebundle.ResourceBundleConstants,
         com.globalsight.everest.servlet.util.ServerProxy,
         com.globalsight.everest.foundation.LocalePair,
         com.globalsight.everest.foundation.User,
         com.globalsight.util.GlobalSightLocale,
         java.util.Locale,
         java.util.List,
         java.util.Vector,
         java.util.ResourceBundle"
		 session="true" %>
         
<jsp:useBean id="cancelHol" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="save"   class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="new1"   class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>


<%
    ResourceBundle bundle = PageHandler.getBundle(session);
    SessionManager sessionMgr = 
      (SessionManager)session.getAttribute(WebAppConstants.SESSION_MANAGER);
    Locale uiLocale = (Locale)session.getAttribute(WebAppConstants.UILOCALE);

    // Buttons
    String lbSave = bundle.getString("lb_save");
    String lbCancel = bundle.getString("lb_cancel");
    String lbCreate = bundle.getString("lb_create_holiday");

    // UI fields
    String addField = HolidayConstants.ADD_HOLIDAYS;
    String removeField = HolidayConstants.REMOVE_HOLIDAYS;
    String updatedField = HolidayConstants.UPDATED_HOLIDAYS;

    String added =  (String) sessionMgr.getAttribute(HolidayConstants.ADD_HOLIDAYS);
    if (added == null)
         added = "";
    String removed =  (String) sessionMgr.getAttribute(HolidayConstants.REMOVE_HOLIDAYS);
    if (removed == null)
         removed = "";

    List allHolidays = (List)request.getAttribute(HolidayConstants.HOLIDAY_LIST);
    List calHolidays = (List)sessionMgr.getAttribute(HolidayConstants.CAL_HOLIDAY_LIST);

    // links for the save and cancel buttons
    String cancelURL = cancelHol.getPageURL() + "&action=" 
                      + CalendarConstants.CANCEL_HOL_ACTION;
    String saveURL = save.getPageURL() + "&action=" 
                     + HolidayConstants.SAVE_ACTION;
    String createURL = new1.getPageURL() + "&action=" 
                     + HolidayConstants.NEW_ACTION;
   
    // Titles                                 
    String title = bundle.getString("lb_holidays");

%>

<html>
<head>
<meta http-equiv="content-type" content="text/html;charset=UTF-8">
<meta http-equiv="Pragma" content="no-cache">
<title><%= title %></title>
<script language="JavaScript" SRC="/globalsight/includes/utilityScripts.js"></script>
<script language="JavaScript" SRC="/globalsight/includes/setStyleSheet.js"></script>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>
<script language="JavaScript">
var needWarning = true;
var guideNode="calendars";
var objectName = "<%= bundle.getString("lb_holiday") %>";
var helpFile = "<%=bundle.getString("help_system_calendar_assign_holidays")%>";

var added = "<%=added%>";
var removed = "<%=removed%>";
var updated = "";

//
// Remove the holiday(s) from the list
//
function remove()
{
    var to = holidayForm.to;
    
    if (to.selectedIndex == -1)
    {
	    alert("<%= bundle.getString("jsmsg_holiday_select") %>");
        return;
    }
    for (var i = 0; i < to.length; i++)
    {
        if (to.options[i].selected)
        {
            removed += to.options[i].value + ",";
            to.options[i] = null;
            i--;
        }
    }
}

var first = true;
//
// Add holiday(s) to the list
//
function addHoliday()
{
    var from = holidayForm.from;
    var to = holidayForm.to;
    if (from.selectedIndex == -1)
    {
        // put up error message
	    alert("<%= bundle.getString("jsmsg_holiday_select") %>");
        return;
    }
    for (var i = 0; i < from.length; i++)
    {
        if (from.options[i].selected)
        {
            if (holidayInList(from.options[i].value))
            {
                alert("<%= bundle.getString("jsmsg_holiday_list") %>");
                return;
            }
            if (first == true)
            {
<%
                if (calHolidays.size() == 0)
                {
%>
                    to.options[0] = null;
<%
                }
%>
                first = false;
            }
            var len = to.options.length;
            to.options[len] = new Option(from.options[i].text, from.options[i].value);
            added += from.options[i].value + ",";
        }
    }
}

//
// Return true if this holiday is already part of the calendar
//
function holidayInList(id)
{
    var to = holidayForm.to;
    for (var i = 0; i < to.length; i++)
    {
        if (to.options[i].value == id)
        {
            return true;
        }
    }
    return false;
}

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
        // Submit the form
        if (added != "")
            holidayForm.<%=addField%>.value = added; 
        if (removed != "")
            holidayForm.<%=removeField%>.value = removed; 

        if (formAction == "save")
            holidayForm.action = "<%=saveURL%>";
        else if (formAction == "create")
        {
            var len = holidayForm.to.options.length;
            for (i=0; i<len; i++)
            {
                updated += holidayForm.to.options[i].value + ",";
                holidayForm.<%=updatedField%>.value = updated;
            }
            holidayForm.action = "<%=createURL%>";
        }

        holidayForm.submit();
    }
}

</script>

</head>
<body leftmargin="0" rightmargin="0" topmargin="0" marginwidth="0" marginheight="0" onload="loadGuides()">
<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>
<div id="contentLayer" style=" position: absolute; z-index: 9; top: 108px; left: 20px; right: 20px;">


<table cellspacing="0" cellpadding="2" border="0">
<tr>
<td colspan="3" class="mainHeading">&nbsp;&nbsp;<%=title%></td>
</tr>
<tr>
<td colspan="3" class="standardText">&nbsp;&nbsp;<%=bundle.getString("lb_select_holiday")%></td>

</tr>
<tr>
<td valign="TOP">
    <!-- left table -->
    <table cellspacing="4" cellpadding="0" border="0" class="standardText">
	<form name="holidayForm" method="post">
        <input type="hidden" name="formAction" value="">
        <input type="hidden" name="<%=addField%>" value="<%=added%>">
        <input type="hidden" name="<%=removeField%>" value="<%=removed%>">
        <input type="hidden" name="<%=updatedField%>" value="">
        <tr>
            <td>
                <%=bundle.getString("lb_available")%>:
            </td>
            <td>
                &nbsp;
            </td>
            <td>
                <%=bundle.getString("lb_added")%>:
            </td>
        </tr>
        <tr>
            <td>
            <select name="from" multiple class="standardText" size=15>
<%
                if (allHolidays != null)
                {
                    int size = allHolidays.size();

                    for (int i=0; i<size; i++)
                    {
                       Holiday holiday = (Holiday)allHolidays.get(i);
%>
                       <option value="<%=holiday.getId()%>" ><%=holiday.getName()%></option>
<%
                    }
                }
%>
            </select>
            </td>
            <td>
              <table>
                <tr>
                  <td>
                    <input type="button" name="addButton" value=" >> " 
                        onclick="addHoliday()"><br>  
                  </td>
                </tr>
                <tr><td>&nbsp;</td></tr>
                <tr>
                  <td>
                    <input type="button" name="removedButton" value=" << " 
                        onclick="remove()">  
                  </td>
                </tr>
              </table>
            </td>
            <td>
            <select name="to" multiple class="standardText" size=15>
<%
                if (calHolidays.size() != 0)
                {
                    int size = calHolidays.size();

                    for (int i=0; i<size; i++)
                    {
                       Holiday holiday = (Holiday)calHolidays.get(i);

%>
                       <option value="<%=holiday.getId()%>" ><%=holiday.getName()%></option>
<%
                    }
                }
                else    
                {
%>
                   <option>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</option>
<%
                }
%>
            </select>
          </td>
        </tr>
    <tr>
    </tr>
    </table>
            <!-- end left table -->
        </td>
        <td width="50">&nbsp;</td>
        <td valign="TOP">          
        </form>
	</td>
    </tr>
    </td>
</tr>


<tr>
    <td class="HREFBold" colspan="2">
        <input type="button" name="<%=lbCancel %>" value="<%=lbCancel %>" 
            onclick="submitForm('cancel')">  
        <input type="button" name="<%=lbSave %>" value="<%=lbSave %>" 
            onclick="submitForm('save')">  
        <input type="button" name="<%=lbCreate %>" value="<%=lbCreate %>" 
            onclick="submitForm('create')">  
    </td>
</tr>
</table>
</div>
</body>
</html>
