<%@ taglib uri="/WEB-INF/tlds/globalsight.tld" prefix="amb" %>
<%@ page contentType="text/html; charset=UTF-8"
         errorPage="/envoy/common/activityError.jsp"
         import="java.util.*, com.globalsight.everest.servlet.util.SessionManager,
                  com.globalsight.everest.permission.Permission,
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
                  com.globalsight.util.GeneralException,
                  java.text.MessageFormat,
                  java.util.Locale, 
                  java.util.ResourceBundle"
          session="true" 
%>


<%@ include file="/envoy/common/header.jspIncl" %>
<jsp:useBean id="holidays1" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="sysCals1" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="userCals1" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<% 
    // Initialization of labels and data

    ResourceBundle bundle = PageHandler.getBundle(session);
    Locale uiLocale = (Locale)session.getAttribute(WebAppConstants.UILOCALE);
    SessionManager sessionManager =
         (SessionManager)session.getAttribute(WebAppConstants.SESSION_MANAGER); 

    // Titles
    String title= bundle.getString("lb_calendars_holidays");
    String helperText = bundle.getString("helper_text_calendars");

    // URLS
    String sysCalsUrl = sysCals1.getPageURL()+ "&action=" + CalendarConstants.SYS_CALS_ACTION;
    String holidaysUrl = holidays1.getPageURL()+ "&action=" + CalendarConstants.HOLIDAYS_ACTION;
    String userCalsUrl = userCals1.getPageURL()+ "&action=" + CalendarConstants.USER_CALS_ACTION;
                                 
    String tab = request.getParameter("tab");
    String sysTabLeftImage = "/globalsight/images/tab_left_gray.gif";
    String sysTabRightImage = "/globalsight/images/tab_right_gray.gif";
    String sysTabClass = "tableHeadingListOff";
    String holidayTabLeftImage = "/globalsight/images/tab_left_gray.gif";
    String holidayTabRightImage = "/globalsight/images/tab_right_gray.gif";
    String holidayTabClass = "tableHeadingListOff";
    String userTabLeftImage = "/globalsight/images/tab_left_gray.gif";
    String userTabRightImage = "/globalsight/images/tab_right_gray.gif";
    String userTabClass = "tableHeadingListOff";
    if (tab.equals("sys"))
    {
        sysTabLeftImage = "/globalsight/images/tab_left_blue.gif";
        sysTabRightImage = "/globalsight/images/tab_right_blue.gif";
        sysTabClass = "tableHeadingListOn";
    }
    else if (tab.equals("holiday"))
    {
        holidayTabLeftImage = "/globalsight/images/tab_left_blue.gif";
        holidayTabRightImage = "/globalsight/images/tab_right_blue.gif";
        holidayTabClass = "tableHeadingListOn";
    }
    else
    {
        userTabLeftImage = "/globalsight/images/tab_left_blue.gif";
        userTabRightImage = "/globalsight/images/tab_right_blue.gif";
        userTabClass = "tableHeadingListOn";
    }
%>
<html>
<head>
    <meta http-equiv="content-type" content="text/html;charset=UTF-8">
    <title><%= title %></title>
    <script language="JavaScript" SRC="/globalsight/includes/setStyleSheet.js"></script>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
    <script language=JavaScript1.2 SRC="/globalsight/includes/cookieUtil.js"></script>
<%@ include file="/envoy/common/warning.jspIncl" %>

<script language="javascript">
var guideNode="calendars";
</script>

</head>

<body leftmargin="0" rightmargin="0" topmargin="0" marginwidth="0" marginheight="0" onload="loadGuides()">
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>
    <div id="contentLayer" style="position: absolute; z-index: 9; top: 108; left: 20px; right: 20px;">
    <amb:header title="<%=title%>" helperText="<%=helperText%>" />
    <table cellspacing="0" cellpadding="0" border=0 >
      <tr>
        <amb:permission name="<%=Permission.SYS_CAL_VIEW%>" >
        <td class="<%=sysTabClass%>">
          <img src="<%=sysTabLeftImage%>" border="0">
          <a class="sortHREFWhite" href="<%=sysCalsUrl%>">
            <%=bundle.getString("lb_system_calendars")%></a>
          <img src="<%=sysTabRightImage%>" border="0">
        </td>
        </amb:permission>
        <td width="2"></td>
        <amb:permission name="<%=Permission.HOLIDAY_VIEW%>" >
        <td class="<%=holidayTabClass%>">
          <img src="<%=holidayTabLeftImage%>" border="0">
          <a class="sortHREFWhite" href="<%=holidaysUrl%>">
            <%=bundle.getString("lb_holidays")%></a>
          <img src="<%=holidayTabRightImage%>" border="0">
        </td>
        </amb:permission>
        <td width="2"></td>
        <amb:permission name="<%=Permission.USER_CAL_VIEW%>" >
        <td class="<%=userTabClass%>">
          <img src="<%=userTabLeftImage%>" border="0">
          <a class="sortHREFWhite" href="<%=userCalsUrl%>">
            <%=bundle.getString("lb_users_calendars")%></a>
          <img src="<%=userTabRightImage%>" border="0">
        </td>
        </amb:permission>
      </tr>
    </table>
    
