<%@ page contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="java.util.*,com.globalsight.everest.securitymgr.FieldSecurity,
            com.globalsight.everest.securitymgr.VendorSecureFields,
            com.globalsight.everest.vendormanagement.Vendor,
            com.globalsight.everest.webapp.WebAppConstants,
            com.globalsight.everest.webapp.pagehandler.administration.vendors.VendorConstants,
            com.globalsight.everest.servlet.util.SessionManager"
         session="true" %>


<% 
    SessionManager sessionMgr =
      (SessionManager)session.getAttribute(WebAppConstants.SESSION_MANAGER);

    FieldSecurity hash = (FieldSecurity)
        sessionMgr.getAttribute(VendorConstants.FIELD_SECURITY_CHECK_PROJS);
    String access = (String)hash.get(VendorSecureFields.PROJECTS);
    if (access == null) access = "shared";
    Vendor vendor = (Vendor)sessionMgr.getAttribute(VendorConstants.VENDOR);
    if (access.equals("hidden"))
    {
%>
        <jsp:forward page="hiddenProjects.jsp"/>
<%
    }
    else if (vendor.getUser() != null || access.equals("locked"))
    {
%>
        <jsp:forward page="readOnlyProjects.jsp"/>
<%
    } else if (access.equals("shared")) {
%>
        <jsp:forward page="../users/userProjects.jsp"/>
<%
    }
%>
