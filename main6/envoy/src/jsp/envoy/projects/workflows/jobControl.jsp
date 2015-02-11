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
Integer num = (Integer)request.getAttribute(JobManagementHandler.NUM_OF_JOBS);

if (0 == num)
{    
    if (searchType != null && searchType.equals(JobSearchConstants.MINI_JOB_SEARCH_COOKIE))
    {
         request.setAttribute("noresults", "true");
%>
        <jsp:forward page="miniSearch.jsp"/>
<%
    } else {
        if (searchType != null && searchType.equals("stateOnly"))
        {
            StringBuffer buf = new StringBuffer(bundle.getString("msg_job_search_no_state"));
            buf.append(" ");
            if (type.equals("pending")) {
                buf.append(bundle.getString("lb_pending"));
            } else if (type.equals("ready")) {
                buf.append(bundle.getString("lb_ready"));
            } else if (type.equals("inprogress")) {
                buf.append(bundle.getString("lb_inprogress"));
            } else if (type.equals("localized")) {
                buf.append(bundle.getString("lb_localized"));
            } else if (type.equals("dtpinprogress")) {
                buf.append(bundle.getString("lb_dtpinprogress"));
            } else if (type.equals("exported")) {
                buf.append(bundle.getString("lb_exported"));
            } else if (type.equals("archived")) {
                buf.append(bundle.getString("lb_archived"));
            } else if (type.equals("allStatus")) {
            	buf.append(bundle.getString("lb_all_status"));
            }
            buf.append(".  ");
            buf.append(bundle.getString("msg_job_search_try"));
            request.setAttribute("noresults", buf.toString());
        }
        else
             request.setAttribute("noresults", bundle.getString("msg_job_search_no_match"));
        Locale uiLocale =
            (Locale)session.getAttribute(WebAppConstants.UILOCALE);
        List srcLocales = WorkflowTemplateHandlerHelper.getAllSourceLocales(uiLocale);
        request.setAttribute("srcLocales", srcLocales);
        List targLocales = WorkflowTemplateHandlerHelper.getAllTargetLocales(uiLocale);
        request.setAttribute("targLocales", targLocales);
        String userName = (String)session.getAttribute(WebAppConstants.USER_NAME);
        List projectInfos;

        if (perms.getPermissionFor(Permission.GET_ALL_PROJECTS))
        {
            projectInfos = WorkflowTemplateHandlerHelper.getAllProjectInfos(uiLocale);
        }
        else if (perms.getPermissionFor(Permission.GET_PROJECTS_I_MANAGE))
        {
            projectInfos = WorkflowTemplateHandlerHelper.getProjectInfosByUser(userName, uiLocale);
        }
        else
        {
            User user = UserHandlerHelper.getUser(userName);
            projectInfos = WorkflowTemplateHandlerHelper.getAllProjectInfosForUser(user, uiLocale);
        }
        request.setAttribute("projects", projectInfos);

%>
        <jsp:forward page="jobSearch.jsp"/>
<%
    }
} else {    
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
}
%>
