<%@ page contentType="text/html; charset=UTF-8"
        errorPage="/envoy/common/error.jsp"
        import="java.util.*,com.globalsight.everest.vendormanagement.Vendor,
                com.globalsight.everest.vendormanagement.VendorRole,
                com.globalsight.everest.servlet.util.SessionManager,
                com.globalsight.everest.util.system.SystemConfigParamNames,
                com.globalsight.everest.util.system.SystemConfiguration,
                com.globalsight.everest.webapp.WebAppConstants,
                com.globalsight.everest.webapp.javabean.NavigationBean,
                com.globalsight.everest.webapp.pagehandler.PageHandler,
                com.globalsight.everest.webapp.pagehandler.administration.users.UserHandlerHelper,
                com.globalsight.everest.webapp.pagehandler.administration.vendors.VendorConstants,
                com.globalsight.util.GlobalSightLocale,
                java.util.Iterator,
                java.util.Locale,
                java.util.Set,
                java.util.ResourceBundle" 
        session="true" 
%>              
<jsp:useBean id="skin" scope="application" class="com.globalsight.everest.webapp.javabean.SkinBean" />


<% 
    SessionManager sessionMgr = (SessionManager)
             session.getAttribute(WebAppConstants.SESSION_MANAGER);
    ResourceBundle bundle = PageHandler.getBundle(session);        
    Locale uiLocale = (Locale)session.getAttribute(WebAppConstants.UILOCALE);
   
    
    //Labels
    String lbRoles = bundle.getString("lb_roles");;
    String lbSourceLocale = bundle.getString("lb_source_locale");
    String lbTargetLocale = bundle.getString("lb_target_locale");

    // Data
    Set roles = null;
    Vendor vendor = (Vendor)sessionMgr.getAttribute(VendorConstants.VENDOR);
    if (vendor != null)
    {
        roles = vendor.getRoles();
    }
        
%>
<script language="JavaScript" SRC="/globalsight/includes/setStyleSheet.js"></script>
<% if (roles != null && roles.size() > 0) { %>
    <p>
    <table border="0" cellspacing="0" cellpadding="5" style="border:1px solid <%=skin.getProperty("skin.list.borderColor")%>"  width="100%">
        <tr class="tableHeadingBasic">
            <td style="padding-right:90px">
                <%=bundle.getString("lb_source_locale")%>
            </td>
            <td style="padding-right:80px">
                <%=bundle.getString("lb_target_locale")%>
            </td>
            <td style="padding-right:80px">
                <%=bundle.getString("lb_activity")%>
            </td>
        </tr>
<%
        int i = 0;
        for (Iterator ri = roles.iterator() ; ri.hasNext() ; ) {
            String color = (i%2 == 0) ? "#FFFFFF" : "#EEEEEE";
            VendorRole vrole = (VendorRole)ri.next();
%>
            <tr class="standardText" bgcolor="<%=color%>">
                <td style="padding-right:90px">
                    <%= vrole.getLocalePair().getSource().getDisplayName(uiLocale)%>
                </td>
                <td style="padding-right:90px">
                    <%= vrole.getLocalePair().getTarget().getDisplayName(uiLocale)%>
                </td>
                <td style="padding-right:90px">
                    <%= vrole.getActivity().getActivityName()%>
                </td>
            </tr>
<%              i++;
                } // end for %>
    </table>
<% } %>
