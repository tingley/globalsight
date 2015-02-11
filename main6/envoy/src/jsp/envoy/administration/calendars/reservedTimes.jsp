<%@ taglib uri="/WEB-INF/tlds/globalsight.tld" prefix="amb" %>
<%@ page contentType="text/html; charset=UTF-8"
         errorPage="/envoy/common/activityError.jsp"
         import="java.util.*, com.globalsight.everest.servlet.util.SessionManager,
                  com.globalsight.everest.webapp.WebAppConstants,
                  com.globalsight.everest.util.comparator.ReservedTimeComparator,
                  com.globalsight.everest.webapp.javabean.NavigationBean,
                  com.globalsight.everest.webapp.pagehandler.PageHandler,
                  com.globalsight.everest.webapp.pagehandler.administration.calendars.CalendarConstants,
                  com.globalsight.everest.webapp.pagehandler.administration.calendars.HolidayConstants,
                  com.globalsight.everest.webapp.pagehandler.administration.calendars.ReservedTimeState,
                  com.globalsight.util.resourcebundle.ResourceBundleConstants,
                  com.globalsight.util.resourcebundle.SystemResourceBundle,
                  com.globalsight.everest.webapp.webnavigation.LinkHelper,
                  com.globalsight.everest.servlet.util.ServerProxy,
                  com.globalsight.everest.servlet.EnvoyServletException,
                  com.globalsight.everest.util.system.SystemConfigParamNames,
                  com.globalsight.everest.util.system.SystemConfiguration,
                  com.globalsight.everest.foundation.Timestamp,
                  com.globalsight.calendar.ReservedTime,
                  com.globalsight.util.GeneralException,
                  java.text.MessageFormat,
                  java.util.Locale,
                  java.util.List,
                  java.util.ArrayList,
                  java.util.ResourceBundle,
                  java.util.TimeZone"
          session="true" 
%>
<%
response.setHeader( "Pragma ", "yes-cache");
response.setHeader("Cache-Control", "yes-store");
response.setHeader("Cache-Control", "yes-cache");
%>

<jsp:useBean id="cancel" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="save" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="new1" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="edit" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="self" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="detail" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="reservedTimes" class="java.util.ArrayList" scope="request"/>
<% 
    // Initialization of labels and data

    ResourceBundle bundle = PageHandler.getBundle(session);
    Locale uiLocale = (Locale)session.getAttribute(WebAppConstants.UILOCALE);
    TimeZone timezone = (TimeZone)session.getAttribute(WebAppConstants.USER_TIME_ZONE);
    SessionManager sessionManager =
         (SessionManager)session.getAttribute(WebAppConstants.SESSION_MANAGER);    
    // Titles
    String title= bundle.getString("lb_events_and_activities");
                                 
    String confirmRemove = bundle.getString("msg_confirm_time_removal");

    // Button names
    String newButton = bundle.getString("lb_new");
    String editButton = bundle.getString("lb_edit");
    String removeButton = bundle.getString("lb_remove");
    String doneButton = bundle.getString("lb_done");
    String cancelButton = bundle.getString("lb_cancel");

    // Urls of the links on this page
    String cancelUrl = cancel.getPageURL() + "&action=" + CalendarConstants.CANCEL_ACTION;
    String saveUrl = save.getPageURL() + "&action=" + CalendarConstants.SAVE_ACTION;
    String newUrl = new1.getPageURL() + "&action=" + CalendarConstants.NEW_ACTION;
    String removeUrl = self.getPageURL() + "&action=" + CalendarConstants.REMOVE_ACTION;
    String editUrl = edit.getPageURL() + "&action=" + CalendarConstants.EDIT_ACTION;
    String detailUrl = detail.getPageURL() +"&action=" + CalendarConstants.ACTIVITY_ACTION;
    String selfUrl = self.getPageURL();

    Timestamp rtDate = (Timestamp)sessionManager.getAttribute("reservedTimeDate");

%>
<html>
<head>
    <meta http-equiv="content-type" content="text/html;charset=UTF-8">
    <title><%= title %></title>
    <script language="JavaScript" SRC="/globalsight/includes/setStyleSheet.js"></script>
    <script language="JavaScript1.2" SRC="/globalsight/includes/cookieUtil.js"></script>
    <%@ include file="/envoy/common/header.jspIncl" %>
    <%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
    <%@ include file="/envoy/common/warning.jspIncl" %>

<script language="JavaScript">
var needWarning = true;
var objectName = "<%=bundle.getString("lb_events")%>";
var guideNode = "calendars";
var helpFile = "<%=bundle.getString("help_events_view")%>";

function submitForm(selectedButton)
{
    if (selectedButton == 'Done')
    {
        RTSForm.action = "<%=saveUrl %>";
        RTSForm.submit();
        return true;
    }
    else if (selectedButton == 'New')
    {
        RTSForm.action = "<%=newUrl %>";
        RTSForm.submit();
        return true;
    }
    else if (selectedButton == 'Cancel')
    {
       if (confirmJump())
       {
            RTSForm.action = "<%=cancelUrl %>";
            RTSForm.submit();
            return true;
       }
       else
       {
          return false;
       }
    }
    var checked = false;
    var selectedRadioBtn = null;
    var listIndex = 0;
    if (RTSForm.radioBtn != null)
    {
        // If more than one radio button is displayed, the length attribute of
        // the radio button array will be non-zero, so find which one is checked
        if (RTSForm.radioBtn.length)
        {
            for (i = 0; !checked && i < RTSForm.radioBtn.length; i++)
            {
                if (RTSForm.radioBtn[i].checked == true)
                {
                    checked = true;
                    selectedRadioBtn = RTSForm.radioBtn[i].value;
                    listIndex = i;
                }
             }
        }
        // If only one is displayed, there is no radio button array, so
        // just check if the single radio button is checked
        else
        {
            if (RTSForm.radioBtn.checked == true)
            {
                checked = true;
                selectedRadioBtn = RTSForm.radioBtn.value;
            }
        }
    }
    // otherwise do the following
    if (!checked)
    {
        alert("<%= bundle.getString("jsmsg_select_reserved_time") %>");
        return false;
    }

    // The value is "reservedTimeId,taskId".  Split into an array.
    items = selectedRadioBtn.split(",");

    if (selectedButton == 'Edit')
    {
        //RTSForm.action = "<%=editUrl %>&id=" + items[0] + "&index=" +listIndex;
        if (items[1] != "null")
        {
            alert("<%=bundle.getString("jsmsg_cannot_edit_reserved_time") %>");
            return false;
        }
        RTSForm.action = "<%=editUrl %>&index=" + items[0];
    }
    else if (selectedButton == 'Remove')
    {
        if (items[1] != "null")
        {
            alert("<%=bundle.getString("jsmsg_cannot_remove_reserved_time") %>");
            return false;
        }
        if (confirm("<%=confirmRemove%>") == false)
            return false;
        RTSForm.action = "<%=removeUrl %>&index=" + items[0];
    }
    RTSForm.submit();
}


</script>

</head>
<body leftmargin="0" rightrmargin="0" topmargin="0" marginwidth="0" marginheight
="0" onload="loadGuides()">
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>
<div id="contentLayer" style="position: absolute; z-index: 9; top: 108; left: 20px; right: 20px;">
    <span class="mainHeading"> <%=title%> - 
    <%= rtDate.toString() %>
    </span>
    <br>


<form name="RTSForm" method="post">
    <table cellpadding=0 cellspacing=0 border=0 class="standardText">
        <tr valign="top">
            <td align="right">
            <amb:tableNav bean="reservedTimes" key="<%=CalendarConstants.RT_KEY%>"
                 pageUrl="self" />
        </td>
      </tr>
      <tr>
        <td>
          <% int i = 0; %>
          <amb:table bean="reservedTimes" id="rtState" key="<%=CalendarConstants.RT_KEY%>"
                 dataClass="com.globalsight.everest.webapp.pagehandler.administration.calendars.ReservedTimeState" pageUrl="self" emptyTableMsg="msg_no_events" >
            <amb:column label="">
                <% ReservedTime rt = rtState.getReservedTime(); %>
                <input type="radio" name="radioBtn" value="<%=i++%>,<%=rt.getTaskId()%>">
            </amb:column>
            <amb:column label="lb_name" sortBy="<%=ReservedTimeComparator.NAME%>">
                <%
                    ReservedTime rt = rtState.getReservedTime();
                    String type = rt.getType();
                    if (rt.getTaskId() != null)
                    {
                        String buf = "<a class=standardHREF href='" +
                                     detailUrl + "&taskAction=getTask&taskId=" +
                                     rt.getTaskId() + "&state=";
                        if (type.equals(ReservedTime.TYPE_PROPOSED))
                        {
                            buf += "3'>";
                        } else {
                            buf += "8'>";
                        }
                        out.print(buf);
                    }
                    out.print(rt.getDisplaySubject());
                    if (rt.getTaskId() != null)
                    {
                        out.print("</a>");
                    }
                 %>
            </amb:column>
            <amb:column label="lb_type" sortBy="<%=ReservedTimeComparator.TYPE%>">
                 <%
                    ReservedTime rt = rtState.getReservedTime();
                    String type = rt.getType();
                    if (type.equals(ReservedTime.TYPE_ACTIVITY))
                    {
                        out.print(bundle.getString("lb_activity_accepted"));
                    }
                    else if (type.equals(ReservedTime.TYPE_BUFFER))
                    {
                        out.print(bundle.getString("lb_activity_buffer"));
                    }
                    else if (type.equals(ReservedTime.TYPE_EVENT))
                    {
                        out.print(bundle.getString("lb_event"));
                    }
                    else if (type.equals(ReservedTime.TYPE_PROPOSED))
                    {
                        out.print(bundle.getString("lb_activity_assigned"));
                    }
                    else if (type.equals(ReservedTime.TYPE_PERSONAL))
                    {
                        out.print(bundle.getString("lb_event") + " (" + 
                            bundle.getString("lb_personal2") + ")");
                    }
                 %>
            </amb:column>
            <amb:column label="lb_time" sortBy="<%=ReservedTimeComparator.TIME%>">
                 <%
                    ReservedTime rt = rtState.getReservedTime();
                    if (rt.getStartHour() == 0 && rt.getEndHour() == 0 &&
                        rt.getStartMinute() == 0 && rt.getEndMinute() == 0)
                    {
                        out.print(bundle.getString("lb_all_day"));
                    }
                    else
                    {
                        Timestamp start = rt.getStartTimestamp();
                        start.setLocale(uiLocale);
                        start.setTimeZone(timezone);
                        Timestamp end = rt.getEndTimestamp();
                        end.setLocale(uiLocale);
                        end.setTimeZone(timezone);
                        out.print(start + " - " + end);
                    }
                 %>
            </amb:column>
          </amb:table>
        </td>
    </tr>
</div>
<tr><td>&nbsp;</td></tr>

<tr>
<td>
<div id="DownloadButtonLayer" align="right" style="visibility: visible">
    <p>
    <input type="button" value="<%=cancelButton%>" onClick="submitForm('Cancel');">
    <input type="button" value="<%=doneButton%>" onClick="submitForm('Done');">
    <input type="button" value="<%=removeButton%>" onClick="submitForm('Remove');">
    <input type="button" value="<%=editButton%>..." onClick="submitForm('Edit') ">
    <input type="button" value="<%=newButton%>..." onClick="submitForm('New');">
</div>
</td>
</tr>
</table>
</form>
