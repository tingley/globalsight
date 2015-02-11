<%@ taglib uri="/WEB-INF/tlds/globalsight.tld" prefix="amb" %>
<%@ page contentType="text/html; charset=UTF-8"
         errorPage="/envoy/common/activityError.jsp"
         import="java.util.*, com.globalsight.everest.servlet.util.SessionManager,
                  com.globalsight.everest.webapp.WebAppConstants,
                  com.globalsight.everest.permission.Permission,
                  com.globalsight.everest.util.comparator.HolidayComparator,
                  com.globalsight.everest.webapp.javabean.NavigationBean,
                  com.globalsight.everest.webapp.pagehandler.PageHandler,
                  com.globalsight.everest.webapp.pagehandler.administration.calendars.HolidayConstants,
                  com.globalsight.util.resourcebundle.ResourceBundleConstants,
                  com.globalsight.util.resourcebundle.SystemResourceBundle,
                  com.globalsight.everest.webapp.webnavigation.LinkHelper,
                  com.globalsight.everest.company.CompanyWrapper,
                  com.globalsight.everest.servlet.EnvoyServletException,
                  com.globalsight.everest.util.system.SystemConfigParamNames,
                  com.globalsight.everest.util.system.SystemConfiguration,
                  com.globalsight.util.GeneralException,
                  com.globalsight.calendar.Holiday,
                  java.text.MessageFormat,
                  java.util.Locale,
                  java.util.List,
                  java.util.ArrayList,
                  java.util.ResourceBundle"
          session="true" 
%>
<jsp:include page="calTabs.jsp" >
  <jsp:param name="tab" value="holiday"/>
</jsp:include>
<jsp:useBean id="skin" scope="application" class="com.globalsight.everest.webapp.javabean.SkinBean" />
<jsp:useBean id="holidays1" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="holidays" class="java.util.ArrayList" scope="request"/>

<% 
    // Initialization of labels and data

    ResourceBundle bundle = PageHandler.getBundle(session);
    Locale uiLocale = (Locale)session.getAttribute(WebAppConstants.UILOCALE);
    SessionManager sessionManager =
         (SessionManager)session.getAttribute(WebAppConstants.SESSION_MANAGER);    
    // Titles
    String holidayTitle= bundle.getString("lb_holidays");
                                 
    String confirmRemove = bundle.getString("jsmsg_warning_holiday_removal");

    // Button names
    String newButton = bundle.getString("lb_new");
    String editButton = bundle.getString("lb_edit");
    String removeButton = bundle.getString("lb_remove");

    // Urls of the links on this page
    String selfUrl = holidays1.getPageURL();
    String newUrl = selfUrl + "&action=" + HolidayConstants.NEW_ACTION;
    String removeUrl = selfUrl + "&action=" + HolidayConstants.REMOVE_HOL_ACTION;
    String editUrl = selfUrl + "&action=" + HolidayConstants.EDIT_ACTION;
    
    boolean isSuperAdmin = ((Boolean) session.getAttribute(WebAppConstants.IS_SUPER_ADMIN)).booleanValue();

%>
<html>
<head>
    <meta http-equiv="content-type" content="text/html;charset=UTF-8">
    <title><%= holidayTitle %></title>
    <script language="JavaScript" SRC="/globalsight/includes/setStyleSheet.js"></script>

<script language="JavaScript">
var needWarning = false;
var guideNode="calendars";
var objectName = "<%=bundle.getString("lb_holiday")%>";
var helpFile = "<%=bundle.getString("help_system_holidays")%>";

function enableButtons()
{
    HolidayForm.removeBtn.disabled = false;
    HolidayForm.editBtn.disabled = false;
}

function submitForm(selectedButton)
{
    var checked = false;
    var selectedRadioBtn = null;
    if (HolidayForm.radioBtn != null)
    {
        // If more than one radio button is displayed, the length attribute of
        // the radio button array will be non-zero, so find which one is checked
        if (HolidayForm.radioBtn.length)
        {
            for (i = 0; !checked && i < HolidayForm.radioBtn.length; i++)
            {
                if (HolidayForm.radioBtn[i].checked == true)
                {
                    checked = true;
                    selectedRadioBtn = HolidayForm.radioBtn[i].value;
                }
             }
        }
        // If only one is displayed, there is no radio button array, so
        // just check if the single radio button is checked
        else
        {
            if (HolidayForm.radioBtn.checked == true)
            {
                checked = true;
                selectedRadioBtn = HolidayForm.radioBtn.value;
            }
        }
    }
    // otherwise do the following
    if (selectedButton != 'New' && !checked)
    {
        alert("<%= bundle.getString("jsmsg_select_holiday") %>");
        return false;
    }

    if (selectedButton == 'Remove')
    {
        if (!confirm('<%=confirmRemove%>')) return false;
        HolidayForm.action = "<%=removeUrl%>&id=" + selectedRadioBtn;
    }
    else if (selectedButton == 'New')
    {
        HolidayForm.action = "<%=newUrl %>";
    }
    else if (selectedButton == 'Edit')
    {
        HolidayForm.action = "<%=editUrl%>&id=" + selectedRadioBtn;
    }
    HolidayForm.submit();
}


</script>

</head>

<form name="HolidayForm" method="post">
    <table cellpadding=0 cellspacing=0 border=0 class="standardText">
      <tr valign="top">
        <td align="right">
            <amb:tableNav bean="holidays" key="<%=HolidayConstants.HOLIDAY_KEY%>"
                 pageUrl="holidays1" />
        </td>
      </tr>
      <tr>
        <td>
          <amb:table bean="holidays" id="holiday" key="<%=HolidayConstants.HOLIDAY_KEY%>"
                 dataClass="com.globalsight.calendar.Holiday" pageUrl="holidays1"
                 emptyTableMsg="msg_no_holidays" >
            <amb:column label="" width="20px">
                <input type="radio" name="radioBtn" value="<%=holiday.getId()%>"
                    onclick="enableButtons()">
            </amb:column>
            <amb:column label="lb_name" sortBy="<%=HolidayComparator.NAME%>">
                <%=holiday.getName()%>
            </amb:column>
            <amb:column label="lb_description" sortBy="<%=HolidayComparator.DESC%>"
                width="200px">
                <% out.print(holiday.getDescription() == null ? "" : holiday.getDescription()); %>
            </amb:column>
            <% if (isSuperAdmin) { %>
            <amb:column label="lb_company_name" sortBy="<%=HolidayComparator.ASC_COMPANY%>">
              <%=CompanyWrapper.getCompanyNameById(holiday.getCompanyId())%>
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
    <amb:permission name="<%=Permission.HOLIDAY_REMOVE%>" >
    <input type="button" name="removeBtn" disabled
         value="<%=removeButton%>" onClick="submitForm('Remove');">
    </amb:permission>
    <amb:permission name="<%=Permission.HOLIDAY_EDIT%>" >
    <input type="button" name="editBtn" disabled
         value="<%=editButton%>..." onClick="submitForm('Edit') ">
    </amb:permission>
    <amb:permission name="<%=Permission.HOLIDAY_NEW%>" >
    <input type="button" value="<%=newButton%>..." onClick="submitForm('New');">
    </amb:permission>
</div>
</td>
</tr>
</table>
</form>
