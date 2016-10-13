<%@ page contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="java.util.*,com.globalsight.everest.vendormanagement.Vendor,
            com.globalsight.everest.webapp.WebAppConstants,
            com.globalsight.everest.webapp.WebAppConstants,
            com.globalsight.everest.servlet.util.SessionManager,
            java.util.Locale"
         session="true" %>


<% 
    SessionManager sessionMgr =
      (SessionManager)session.getAttribute(WebAppConstants.SESSION_MANAGER);
    String wrongLocale = (String)request.getAttribute("wrongLocale");

    if (wrongLocale != null)
    {
%>
        <jsp:include page="designerNotAllowed.jsp"/>
<%
    } else {
%>
        <jsp:include page="customFormDesigner.jsp"/>
<%
    }
%>
