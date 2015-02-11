<%@ taglib uri="/WEB-INF/tlds/globalsight.tld" prefix="amb" %>
<%@ page contentType="text/html; charset=UTF-8"
         errorPage="/envoy/common/activityError.jsp"
         import="java.util.*, com.globalsight.everest.servlet.util.SessionManager,
                  com.globalsight.everest.permission.Permission,
                  com.globalsight.everest.webapp.WebAppConstants,
                  com.globalsight.everest.util.comparator.UserCalendarComparator,
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
                  com.globalsight.calendar.UserFluxCalendar,
                  com.globalsight.everest.servlet.util.ServerProxy,                  
                  com.globalsight.everest.webapp.pagehandler.administration.users.UserUtil,  
                  java.text.MessageFormat,
                  java.util.Locale, 
                  java.util.ResourceBundle,
                  java.util.TimeZone"
          session="true" 
%>
<jsp:include page="calTabs.jsp" >
  <jsp:param name="tab" value="user"/>
</jsp:include>
<jsp:useBean id="skin" scope="application" class="com.globalsight.everest.webapp.javabean.SkinBean" />
<jsp:useBean id="userCals1" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="userCals" class="java.util.ArrayList" scope="request" />

<% 
    // Initialization of labels and data

    ResourceBundle bundle = PageHandler.getBundle(session);
    Locale uiLocale = (Locale)session.getAttribute(WebAppConstants.UILOCALE);
    SessionManager sessionManager =
         (SessionManager)session.getAttribute(WebAppConstants.SESSION_MANAGER);    
    // Titles
    String userCalTitle= bundle.getString("lb_system_calendars");
                                 
    String confirmRemove = bundle.getString("msg_confirm_wf_removal");

    // Button names
    String editButton = bundle.getString("lb_edit");

    // Urls of the links on this page
    String selfUrl = userCals1.getPageURL();
    String editUrl = selfUrl + "&action=" + CalendarConstants.EDIT_ACTION;
    
    boolean isSuperAdmin = ((Boolean) session.getAttribute(WebAppConstants.IS_SUPER_ADMIN)).booleanValue();

%>
<html>
<head>
    <meta http-equiv="content-type" content="text/html;charset=UTF-8">
    <title><%= userCalTitle %></title>
    <script language="JavaScript" SRC="/globalsight/includes/setStyleSheet.js"></script>

<script language="JavaScript">
var needWarning = false;
var guideNode = "calendars";
var objectName = "<%=bundle.getString("lb_calendar")%>";
var helpFile = "<%=bundle.getString("help_user_calendars")%>";

function submitForm(selectedButton)
{
    var checked = false;
    var selectedRadioBtn = null;
    if (UserCalForm.radioBtn != null)
    {
        // If more than one radio button is displayed, the length attribute of
        // the radio button array will be non-zero, so find which one is checked
        if (UserCalForm.radioBtn.length)
        {
            for (i = 0; !checked && i < UserCalForm.radioBtn.length; i++)
            {
                if (UserCalForm.radioBtn[i].checked == true)
                {
                    checked = true;
                    selectedRadioBtn = UserCalForm.radioBtn[i].value;
                }
             }
        }
        // If only one is displayed, there is no radio button array, so
        // just check if the single radio button is checked
        else
        {
            if (UserCalForm.radioBtn.checked == true)
            {
                checked = true;
                selectedRadioBtn = UserCalForm.radioBtn.value;
            }
        }
    }
    if (!checked)
    {
        alert("<%= bundle.getString("jsmsg_select_calendar") %>");
        return false;
    }
    UserCalForm.action = "<%=editUrl %>&id=" + selectedRadioBtn;
    UserCalForm.submit();
}

function enableButtons()
{
    UserCalForm.editBtn.disabled = false;
}

</script>

</head>

<form name="UserCalForm" method="post">
    <table cellpadding=0 cellspacing=0 border=0 class="standardText">
      <tr valign="top">
        <td align="right">
            <amb:tableNav bean="userCals" key="<%=CalendarConstants.USER_CAL_KEY%>"
                 pageUrl="userCals1" />
        </td>
      </tr>
      <tr>
        <td>
          <amb:table bean="userCals" id="cal" key="<%=CalendarConstants.USER_CAL_KEY%>"
                 dataClass="com.globalsight.calendar.UserFluxCalendar" pageUrl="userCals1"
                 emptyTableMsg="msg_no_usercals" >
            <amb:column label="" width="20px">
                <input type="radio" name="radioBtn" value="<%=cal.getId()%>"
                    onclick="enableButtons()">
            </amb:column>
            <amb:column label="lb_user_name"  sortBy="<%=UserCalendarComparator.NAME%>">
                <%=UserUtil.getUserNameById(cal.getOwnerUserId())%>
            </amb:column>
            <amb:column label="lb_time_zone" width="200px"
             sortBy="<%=UserCalendarComparator.TIMEZONE%>">
                <%=cal.getTimeZone().getDisplayName()%>
            </amb:column>
            <% if (isSuperAdmin) { %>
            <amb:column label="lb_company_name" sortBy="<%=UserCalendarComparator.ASC_COMPANY%>">
                <%
		          	  long companyId = ServerProxy.getCalendarManager().findCalendarById(cal.getParentCalendarId()).getCompanyId();
		          	  String companyName = CompanyWrapper.getCompanyNameById(companyId);
                	  out.print(companyName);
                %>
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
    <td style="padding-top:5px" align="right">
    <amb:permission name="<%=Permission.USER_CAL_EDIT%>" >
    <input type="button" value="<%=editButton%>..." name="editBtn" disabled
         onClick="submitForm('Edit') ">
    </amb:permission>
</div>
</td>
</tr>
<tr><td>&nbsp</td></tr>
</table>
</form>
