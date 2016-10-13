<%@ taglib uri="/WEB-INF/tlds/globalsight.tld" prefix="amb" %>
<%@ page 
        contentType="text/html; charset=UTF-8"
        errorPage="/envoy/common/error.jsp"
        import="java.util.*,com.globalsight.everest.securitymgr.FieldSecurity,
                com.globalsight.everest.securitymgr.VendorSecureFields,
                com.globalsight.everest.servlet.util.SessionManager,
                com.globalsight.everest.util.system.SystemConfigParamNames,
                com.globalsight.everest.util.system.SystemConfiguration,
                com.globalsight.everest.vendormanagement.Vendor,
                com.globalsight.everest.webapp.WebAppConstants,
                com.globalsight.everest.webapp.javabean.NavigationBean,
                com.globalsight.everest.webapp.pagehandler.PageHandler,
                com.globalsight.everest.webapp.pagehandler.administration.vendors.VendorConstants,
                com.globalsight.everest.foundation.User,
                com.globalsight.util.GlobalSightLocale,
                java.util.Iterator,
                java.util.Locale,
                java.util.ResourceBundle" 
        session="true" 
%>


<% 
    ResourceBundle bundle = PageHandler.getBundle(session);        

   
    String pagetitle= bundle.getString("lb_view_vendor") + " - " +
                    bundle.getString("lb_contact_information");
    
        

    SessionManager sessionMgr =
         (SessionManager) session.getAttribute(WebAppConstants.SESSION_MANAGER);

    // Field level security
    FieldSecurity hash = (FieldSecurity)
         sessionMgr.getAttribute(VendorConstants.FIELD_SECURITY_CHECK_PROJS);

    //User UI locale
    Locale userUILocale = (Locale)session.getAttribute(WebAppConstants.UILOCALE);

    // Get the data
    String pageContent = (String)request.getAttribute("pageContent");
    if (pageContent == null) pageContent = "";

%>

<script language="JavaScript" SRC="/globalsight/includes/setStyleSheet.js"></script>
<p>
          <%=pageContent%>
