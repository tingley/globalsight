<%@ page contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="java.util.*,com.globalsight.everest.webapp.WebAppConstants,
            com.globalsight.everest.webapp.pagehandler.PageHandler,
            com.globalsight.everest.servlet.util.SessionManager,
            com.globalsight.everest.securitymgr.FieldSecurity,
            com.globalsight.everest.securitymgr.UserSecureFields,
            java.util.ResourceBundle"
         session="true" %>

<jsp:useBean id="cancelEdit" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<!-- This JSP is envoy/administration/users/projectsAccess.jsp -->
<% 
    String action = cancelEdit.getPageURL() + "&action=cancelEdit";
    ResourceBundle bundle = PageHandler.getBundle(session);
    SessionManager sessionMgr =
      (SessionManager)session.getAttribute(WebAppConstants.SESSION_MANAGER);

    FieldSecurity hash = (FieldSecurity)sessionMgr.getAttribute("securitiesHash");
    String access = (String)hash.get(UserSecureFields.PROJECTS);
    if (access == null) access = "shared";
    if (access.equals("hidden"))
    {
%>
        <jsp:forward page="hiddenProjects.jsp"/>
<%
    }
    else if (access.equals("locked"))
    {
%>
        <jsp:forward page="readOnlyProjects.jsp"/>
<%
    } else {
%>
        <jsp:forward page="userProjects.jsp"/>
<%
    }
%>

