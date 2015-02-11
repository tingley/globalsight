<%@ page contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="java.util.*,com.globalsight.everest.webapp.WebAppConstants,
            com.globalsight.everest.webapp.pagehandler.PageHandler,
            com.globalsight.everest.webapp.pagehandler.projects.workflows.JobSearchConstants,
            com.globalsight.everest.webapp.pagehandler.administration.workflow.WorkflowTemplateHandlerHelper,
            com.globalsight.everest.webapp.pagehandler.offline.OfflineConstants,
            com.globalsight.everest.webapp.pagehandler.administration.users.UserHandlerHelper,
            com.globalsight.everest.foundation.User,
            java.util.HashSet,
            java.util.List,
            java.util.ResourceBundle,
            java.util.Locale,
            com.globalsight.everest.servlet.util.SessionManager"
         session="true"
%><%
HashSet languageSet = (HashSet) request.getAttribute("languageSet");
SessionManager sessionMgr = (SessionManager)
                    session.getAttribute(WebAppConstants.SESSION_MANAGER);
List tasks = (List)sessionMgr.getAttribute(WebAppConstants.TASK_LIST);
String action = (String)request.getAttribute("action");
String lastSearch = (String)request.getParameter("lastSearch");

// get the Download request flag
Boolean acceptDownloadRequested = 
   (Boolean)request.getAttribute(OfflineConstants.DOWNLOAD_ACCEPT_DOWNLOAD);    
    
// If the list is null, then it's a translator login with no results.  Just show
// a message.
if (tasks == null)
{
%><jsp:forward page="noTranslatorResults.jsp" /><%
 
// if the list is empty and it's not an 'accept & download' action show the search screen
}
else if (tasks.size() == 0 && acceptDownloadRequested == null)
{
    ResourceBundle bundle = PageHandler.getBundle(session);
    if (action != null && action.equals(JobSearchConstants.MINI_TASK_SEARCH_COOKIE) ||
        (lastSearch != null && lastSearch.equals(JobSearchConstants.MINI_TASK_SEARCH_COOKIE)))
    {
%><jsp:forward page="miniSearch.jsp"/>%><%
    } else {
        Locale uiLocale =
            (Locale)session.getAttribute(WebAppConstants.UILOCALE);
        List srcLocales = WorkflowTemplateHandlerHelper.getAllSourceLocales(uiLocale);
        request.setAttribute("srcLocales", srcLocales);
        List targLocales = WorkflowTemplateHandlerHelper.getAllTargetLocales(uiLocale);
        request.setAttribute("targLocales", targLocales);

%><jsp:forward page="taskSearch.jsp"/><%
    }
} else { 
%><jsp:forward page="taskList.jsp"/><%
}
%>

