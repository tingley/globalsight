<%@ page contentType="text/html; charset=UTF-8"
         errorPage="/envoy/common/activityError.jsp"
         import="java.util.*, com.globalsight.everest.servlet.util.SessionManager,
                  com.globalsight.everest.webapp.WebAppConstants,
                  com.globalsight.everest.webapp.javabean.NavigationBean,
                  com.globalsight.everest.webapp.pagehandler.PageHandler,
                  com.globalsight.everest.webapp.pagehandler.administration.users.UserHandlerHelper,
                  com.globalsight.util.resourcebundle.ResourceBundleConstants,
                  com.globalsight.util.resourcebundle.SystemResourceBundle,
                  com.globalsight.everest.foundation.SearchCriteriaParameters,
                  com.globalsight.everest.foundation.User,
                  com.globalsight.everest.jobhandler.Job,
                  com.globalsight.everest.projecthandler.ProjectInfo,
                  com.globalsight.everest.webapp.webnavigation.LinkHelper,
                  com.globalsight.everest.servlet.util.ServerProxy,
                  com.globalsight.everest.servlet.EnvoyServletException,
                  com.globalsight.everest.util.system.SystemConfigParamNames,
                  com.globalsight.everest.util.system.SystemConfiguration,
                  com.globalsight.util.GeneralException,
                  com.globalsight.util.GlobalSightLocale,
                  java.text.MessageFormat,
                  java.util.Locale,
                  java.util.ResourceBundle"
          session="true"
%>
<%
    ResourceBundle bundle = PageHandler.getBundle(session);
    SessionManager sessionMgr = (SessionManager)session.getAttribute(WebAppConstants.SESSION_MANAGER);

    // Labels, etc
    String title= bundle.getString("lb_jobs") + " - " + bundle.getString("lb_search");
    String lbsearch = bundle.getString("lb_search");

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
var objectName = "";
var guideNode="myJobs";
var helpFile = "<%=bundle.getString("help_job_details")%>";


</script>
</head>

<body leftmargin="0" rightrmargin="0" topmargin="0" marginwidth="0" marginheight="0" onload="loadGuides()">
<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>
    <div id="contentLayer" style="position: absolute; z-index: 9; top: 108; left: 20px; right: 20px;">
    <span class="mainHeading">
        <%=title%>
    </span>
     <% if (request.getAttribute("noresults") != null)
             out.println("<div style='color:red'>" + bundle.getString("msg_job_search_no_match") + "</div>"); %>
    <p>

<%@ include file="miniSearch.jspIncl" %>

</form>

