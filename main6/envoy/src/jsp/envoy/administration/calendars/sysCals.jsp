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
                  com.globalsight.util.resourcebundle.ResourceBundleConstants,
                  com.globalsight.util.resourcebundle.SystemResourceBundle,
                  com.globalsight.everest.webapp.webnavigation.LinkHelper,
                  com.globalsight.everest.company.CompanyWrapper,
                  com.globalsight.everest.servlet.EnvoyServletException,
                  com.globalsight.everest.util.system.SystemConfigParamNames,
                  com.globalsight.everest.util.system.SystemConfiguration,
                  com.globalsight.util.GeneralException,
                  com.globalsight.calendar.FluxCalendar,                 
                  java.text.MessageFormat,
                  java.util.Locale, 
                  java.util.ResourceBundle,
                  java.util.TimeZone"
          session="true" 
%>
<jsp:include page="calTabs.jsp" >
  <jsp:param name="tab" value="sys"/>
</jsp:include>
<jsp:useBean id="skin" scope="application" class="com.globalsight.everest.webapp.javabean.SkinBean" />
<jsp:useBean id="sysCals1" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="sysCals" class="java.util.ArrayList" scope="request" />


<% 
    // Initialization of labels and data

    ResourceBundle bundle = PageHandler.getBundle(session);
    Locale uiLocale = (Locale)session.getAttribute(WebAppConstants.UILOCALE);
    SessionManager sessionManager =
         (SessionManager)session.getAttribute(WebAppConstants.SESSION_MANAGER);    
    // Titles
    String sysCalTitle= bundle.getString("lb_system_calendars");
                                 
    String confirmRemove = bundle.getString("msg_confirm_cal_removal");

    // Button names
    String newButton = bundle.getString("lb_new");
    String editButton = bundle.getString("lb_edit");
    String removeButton = bundle.getString("lb_remove");
    String makeDefButton = bundle.getString("lb_make_default");
    String dupButton = bundle.getString("lb_duplicate");

    // Urls of the links on this page
    String selfUrl = sysCals1.getPageURL();
    String newUrl = selfUrl + "&action=" + CalendarConstants.NEW_ACTION;
    String editUrl = selfUrl + "&action=" + CalendarConstants.EDIT_ACTION;
    String makeDefUrl = selfUrl + "&action=" + CalendarConstants.MAKE_DEFAULT_ACTION;
    String removeUrl = selfUrl + "&action=" + CalendarConstants.REMOVE_ACTION;
    String dupUrl = selfUrl + "&action=" + CalendarConstants.DUPLICATE_ACTION;

    boolean isSuperAdmin = ((Boolean) session.getAttribute(WebAppConstants.IS_SUPER_ADMIN)).booleanValue();
%>
<html>
<head>
    <meta http-equiv="content-type" content="text/html;charset=UTF-8">
    <title><%= sysCalTitle %></title>
    <script language="JavaScript" SRC="/globalsight/includes/setStyleSheet.js"></script>

<script language="JavaScript">
var needWarning = false;
var guideNode = "calendars";
var objectName = "<%=bundle.getString("lb_calendar")%>";
var helpFile = "<%=bundle.getString("help_system_calendars")%>";

function enableButtons()
{
    SysCalForm.dupBtn.disabled = false;
    SysCalForm.defBtn.disabled = false;
    SysCalForm.removeBtn.disabled = false;
    SysCalForm.editBtn.disabled = false;
}

function submitForm(selectedButton)
{
    var checked = false;
    var selectedRadioBtn = null;
    if (SysCalForm.radioBtn != null)
    {
        // If more than one radio button is displayed, the length attribute of
        // the radio button array will be non-zero, so find which one is checked
        if (SysCalForm.radioBtn.length)
        {
            for (i = 0; !checked && i < SysCalForm.radioBtn.length; i++)
            {
                if (SysCalForm.radioBtn[i].checked == true)
                {
                    checked = true;
                    selectedRadioBtn = SysCalForm.radioBtn[i].value;
                }
             }
        }
        // If only one is displayed, there is no radio button array, so
        // just check if the single radio button is checked
        else
        {
            if (SysCalForm.radioBtn.checked == true)
            {
                checked = true;
                selectedRadioBtn = SysCalForm.radioBtn.value;
            }
        }
    }
    // otherwise do the following
    if (selectedButton == 'New')
    {
        SysCalForm.action = "<%=newUrl %>";
        SysCalForm.submit();
        return;
    }
    else if (!checked)
    {
        alert("<%= bundle.getString("jsmsg_select_calendar") %>");
        return false;
    }

    var data = selectedRadioBtn.split(",");
    if (selectedButton == 'Remove')
    {
        if (data[1] == "true")
        {
            alert("<%=bundle.getString("jsmsg_cannot_remove_sys_calendar")%>");
            return false;
        }
        if (!confirm('<%=confirmRemove%>')) return false;
        SysCalForm.action = "<%=removeUrl%>&id=" + data[0];
    }
    else if (selectedButton == 'Default')
    {
        if (data[1] == "true")
        {
            alert("<%=bundle.getString("jsmsg_aleady_default_cal")%>");
            return false;
        }
        SysCalForm.action = "<%=makeDefUrl %>&id=" + data[0];
    }
    else if (selectedButton == 'Edit')
    {
        SysCalForm.action = "<%=editUrl %>&id=" + data[0];
    }
    else if (selectedButton == 'Duplicate')
    {
        SysCalForm.action = "<%=dupUrl %>&id=" + data[0];
    }
    SysCalForm.submit();
}
</script>

</head>

<form name="SysCalForm" method="post">
    <table cellpadding=0 cellspacing=0 border=0 class="standardText">
      <tr valign="top">
        <td align="right">
            <amb:tableNav bean="sysCals" key="<%=CalendarConstants.SYS_CAL_KEY%>"
                 pageUrl="sysCals1" />
        </td>
      </tr>
      <tr>
        <td>
          <amb:table bean="sysCals" id="cal" key="<%=CalendarConstants.SYS_CAL_KEY%>"
                 dataClass="com.globalsight.calendar.FluxCalendar" pageUrl="sysCals1"
                 emptyTableMsg="msg_no_syscals" >
            <amb:column label="" width="20px">
                <input type="radio" name="radioBtn" onclick="enableButtons()"
                 value="<%=cal.getId()%>,<%=cal.isDefault()%>">
            </amb:column>
            <amb:column label="lb_name" sortBy="<%=CalendarComparator.NAME%>">
                <%=cal.getName()%>
            </amb:column>
            <amb:column label="lb_time_zone" sortBy="<%=CalendarComparator.TIMEZONE%>">
                <%=cal.getTimeZoneId()%>
            </amb:column>
            <amb:column label="lb_default">
                <% if (cal.isDefault() == true)
                   {
                %>
                    <img src="/globalsight/images/checkmark.gif"
                     height=9 width=13 hspace=10 vspace=3>
                <% 
                   }
                %>
            </amb:column>
            <% if (isSuperAdmin) { %>
            <amb:column label="lb_company_name" sortBy="<%=CalendarComparator.ASC_COMPANY%>">
              <%=CompanyWrapper.getCompanyNameById(cal.getCompanyId())%>
            </amb:column>
            <% } %>
          </amb:table>
        </td>
    </tr>
</div>
<tr><td>&nbsp;</td></tr>


<tr>
<td>
<div id="DownloadButtonLayer" align="right" style="visibility: visible">
    <p>
    <amb:permission name="<%=Permission.SYS_CAL_DUP%>" > 
    <input type="button" name="dupBtn" disabled
         value="<%=dupButton%>" onClick="submitForm('Duplicate');">
    </amb:permission>
    <amb:permission name="<%=Permission.SYS_CAL_DEFAULT%>" > 
    <input type="button" name="defBtn" disabled
         value="<%=makeDefButton%>" onClick="submitForm('Default');">
    </amb:permission>
    <amb:permission name="<%=Permission.SYS_CAL_REMOVE%>" > 
    <input type="button" name="removeBtn" disabled
         value="<%=removeButton%>" onClick="submitForm('Remove');">
    </amb:permission>
    <amb:permission name="<%=Permission.SYS_CAL_EDIT%>" > 
    <input type="button" name="editBtn" disabled
         value="<%=editButton%>..." onClick="submitForm('Edit') ">
    </amb:permission>
    <amb:permission name="<%=Permission.SYS_CAL_NEW%>" > 
    <input type="button" value="<%=newButton%>..." onClick="submitForm('New');">
    </amb:permission>
</div>
</td>
</tr>
</table>
</form>
