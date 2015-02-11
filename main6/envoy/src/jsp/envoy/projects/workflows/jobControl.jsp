<%@ page contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="java.util.*,com.globalsight.everest.webapp.WebAppConstants,
            com.globalsight.everest.permission.Permission,
            com.globalsight.everest.permission.PermissionSet,            
            com.globalsight.everest.webapp.pagehandler.PageHandler,
            com.globalsight.everest.webapp.pagehandler.projects.workflows.JobSearchConstants,
            com.globalsight.everest.webapp.pagehandler.projects.workflows.JobManagementHandler,
            com.globalsight.everest.webapp.pagehandler.administration.workflow.WorkflowTemplateHandlerHelper,
            com.globalsight.everest.webapp.pagehandler.administration.users.UserHandlerHelper,
            com.globalsight.everest.foundation.User,
            com.globalsight.everest.servlet.util.SessionManager,
            java.util.ResourceBundle,
            java.util.Locale,
            java.util.List"
         session="true" %>
<%
SessionManager sessionMgr = (SessionManager)
                    session.getAttribute(WebAppConstants.SESSION_MANAGER);
ResourceBundle bundle = PageHandler.getBundle(session);
PermissionSet perms = (PermissionSet) session.getAttribute(WebAppConstants.PERMISSIONS);

String type = (String)sessionMgr.getAttribute("destinationPage");
String searchType = (String)request.getAttribute("searchType");
String scriptlet = (String)request.getAttribute(JobManagementHandler.JOB_SCRIPTLET);
   
    if (type.equals("pending"))
    {
%>
        <jsp:forward page="jobControlPending.jsp"/>
<%
    }
    else if (type.equals("ready"))
    {
%>
        <jsp:forward page="jobControlReady.jsp"/>
<%
    }
    else if (type.equals("inprogress"))
    {
%>
        <jsp:forward page="jobControlInProgress.jsp"/>
<%
    }
    else if (type.equals("localized"))
    {
%>
        <jsp:forward page="jobControlCompleted.jsp"/>
<%
    }
    else if (type.equals("dtpinprogress"))
    {  
%>
        <jsp:forward page="jobControlDtpInProgress.jsp"/>
<%
    }
    else if (type.equals("exported"))
    {
%>
        <jsp:forward page="jobControlExported.jsp"/>
<%
    }
    else if (type.equals("archived"))
    {
%>
        <jsp:forward page="jobArchive.jsp"/>
<%
    }
    else if (type.equals("allStatus"))
    {
%>
        <jsp:forward page="jobControlAll.jsp"/>
<%
    }
%>
