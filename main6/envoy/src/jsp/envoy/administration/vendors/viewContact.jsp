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
    
    //Labels
    String lbAddress = bundle.getString("lb_address");
    String lbHomePhone = bundle.getString("lb_home_phone");
    String lbWorkPhone = bundle.getString("lb_work_phone");
    String lbCellPhone = bundle.getString("lb_cell_phone");
    String lbFax = bundle.getString("lb_fax");
    String lbEmail = bundle.getString("lb_email");
    String lbEmailLanguage = bundle.getString("lb_email_language");

        

    SessionManager sessionMgr =
         (SessionManager) session.getAttribute(WebAppConstants.SESSION_MANAGER);

    // Field level security
    FieldSecurity hash = (FieldSecurity)
         sessionMgr.getAttribute(VendorConstants.FIELD_SECURITY_CHECK_PROJS);

    //User UI locale
    Locale userUILocale = (Locale)session.getAttribute(WebAppConstants.UILOCALE);

    // Get the data
    Vendor vendor = (Vendor) sessionMgr.getAttribute(VendorConstants.VENDOR);

    String address = vendor.getAddress();
    if (address == null) address = "";
    String homePhone = vendor.getPhoneNumber(User.PhoneType.HOME);
    if (homePhone == null) homePhone = "";
    String workPhone = vendor.getPhoneNumber(User.PhoneType.OFFICE);
    if (workPhone == null) workPhone = "";
    String cellPhone = vendor.getPhoneNumber(User.PhoneType.CELL);
    if (cellPhone == null) cellPhone = "";
    String fax = vendor.getPhoneNumber(User.PhoneType.FAX);
    if (fax == null) fax = "";
    String email = vendor.getEmail();
    if (email == null) email = "";
    Locale locale = PageHandler.getUILocale(vendor.getDefaultUILocale());
    String emailLanguage = locale.getDisplayLanguage(userUILocale);
%>

<script language="JavaScript" SRC="/globalsight/includes/setStyleSheet.js"></script>
<p>
<table cellspacing="0" cellpadding="1" border="0" class="detailText" width="100%">
  <tr>
     <td bgcolor="D6CFB2">
        <table cellpadding=4 cellspacing=0 border=0
                    class=detailText bgcolor="WHITE" width="100%">
        <tr valign="TOP">
            <td style="background:D6CFB2; font-weight:bold; font-size:larger" colspan=2><%=bundle.getString("lb_contact_information")%></td>
        </tr>
        <tr>
            <td class="standardText" width="25%">
                <%= lbAddress %>:
            </td>
            <td class="standardTextBold">
                <amb:readOnlyData access='<%=(String)hash.get(VendorSecureFields.ADDRESS)%>'
                     value="<%= address %>" />
            </td>
        </tr>
        <tr>
            <td class="standardText">
                <%= lbHomePhone %>:
            </td>
            <td class="standardTextBold">
                <amb:readOnlyData access='<%=(String)hash.get(VendorSecureFields.HOME_PHONE)%>'
                     value="<%= homePhone %>" />
            </td>
        </tr>
        <tr>
            <td class="standardText">
                <%= lbWorkPhone%>:
            </td>
            <td class="standardTextBold">
                <amb:readOnlyData access='<%=(String)hash.get(VendorSecureFields.WORK_PHONE)%>'
                     value="<%= workPhone %>" />
            </td>
        </tr>
        <tr>
            <td class="standardText">
                <%= lbCellPhone%>:
            </td>
            <td class="standardTextBold">
                <amb:readOnlyData access='<%=(String)hash.get(VendorSecureFields.CELL_PHONE)%>'
                     value="<%= cellPhone %>" />
            </td>
        </tr>
        <tr>
            <td class="standardText">
                <%= lbFax%>:
            </td>
            <td class="standardTextBold">
                <amb:readOnlyData access='<%=(String)hash.get(VendorSecureFields.FAX)%>'
                     value="<%= fax %>" />
            </td>
        </tr>
        <tr>
            <td class="standardText">
                <%= lbEmail%>:
            </td>
            <td class="standardTextBold">
                <amb:readOnlyData access='<%=(String)hash.get(VendorSecureFields.EMAIL)%>'
                     value="<%= email %>" />
            </td>
        </tr>
        <tr>
            <td class="standardText">
                <%= lbEmailLanguage%>:
            </td>
            <td class="standardTextBold">
                <amb:readOnlyData access='<%=(String)hash.get(VendorSecureFields.EMAIL_LANGUAGE)%>'
                     value="<%= emailLanguage %>" />
            </td>
        </tr>
        </td>
        </tr>
    </table>
   </td>
  </tr>
</table>
