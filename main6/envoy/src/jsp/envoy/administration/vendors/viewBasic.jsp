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
                com.globalsight.util.GlobalSightLocale,
                com.globalsight.everest.webapp.pagehandler.administration.users.UserUtil,
                java.util.Iterator,
                java.util.Locale,
                java.util.ResourceBundle" 
        session="true" 
%>

<% 
    ResourceBundle bundle = PageHandler.getBundle(session);        
   
    //Labels
    String lbVendorId = bundle.getString("lb_vendor_id");
    String lbFirstName = bundle.getString("lb_first_name");
    String lbLastName = bundle.getString("lb_last_name");
	String lbTitle = bundle.getString("lb_title");
    String lbType = bundle.getString("lb_type");    
	String lbCompanyName = bundle.getString("lb_company_name");
	String lbNotes = bundle.getString("lb_notes");
    String lbAmbassadorAccess = bundle.getString("lb_ambassador_access");
    String lbStatus = bundle.getString("lb_status");
    String lbUsername = bundle.getString("lb_user_name");
    String lbInternal = bundle.getString("lb_internal");    
    String lbExternal = bundle.getString("lb_external");    
    String lbAllowed = bundle.getString("lb_allowed");    
    String lbNotAllowed = bundle.getString("lb_not_allowed");    
    String lbChoose = bundle.getString("lb_choose");    
    String lbApproved = bundle.getString("lb_approved");    
    String lbRejected = bundle.getString("lb_rejected");    
    String lbPending = bundle.getString("lb_pending");    
    String lbOnHold = bundle.getString("lb_on_hold");    

    String lbOK = bundle.getString("lb_ok");    
    String lbRoles = bundle.getString("lb_roles");    
    String lbContact = bundle.getString("lb_contact_info");    
    String lbCV = bundle.getString("lb_cv_resume");    
    String lbProjects = bundle.getString("lb_projects");    
        

	//User UI locale 
	Locale userUiLocale = (Locale)session.getAttribute(WebAppConstants.UILOCALE); 	
	
    SessionManager sessionMgr =
         (SessionManager) session.getAttribute(WebAppConstants.SESSION_MANAGER);

    // Get the data
    FieldSecurity hash = (FieldSecurity)
         sessionMgr.getAttribute(VendorConstants.FIELD_SECURITY_CHECK_PROJS);
    Vendor vendor = (Vendor) sessionMgr.getAttribute(VendorConstants.VENDOR);

    String vendorId = vendor.getCustomVendorId();
    String firstName = vendor.getFirstName();
    String lastName = vendor.getLastName();
    String userTitle = vendor.getTitle();
    if (userTitle == null) userTitle = "";
    String type;
    if (vendor.isInternalVendor()) 
        type = lbInternal;
    else
        type = lbExternal;
    String companyName = vendor.getCompanyName();
    if (companyName == null) companyName = "";
    String notes = vendor.getNotes();
    String access;
    if (vendor.useInAmbassador())
        access = lbAllowed;
    else
        access = lbNotAllowed;
    String status = vendor.getStatus();
    
    String userName  = UserUtil.getUserNameById(vendor.getUserId());
    if (userName == null) userName = "";
%>
<!-- This JSP is envoy/administration/vendors/viewBasic.jsp -->
<script language="JavaScript" SRC="/globalsight/includes/setStyleSheet.js"></script>
<p>
<table cellspacing="0" cellpadding="1" border="0" class="detailText" width="100%">
  <tr>
     <td bgcolor="D6CFB2">
        <table cellpadding=4 cellspacing=0 border=0
                    class=detailText bgcolor="WHITE" width="100%">
        <tr valign="TOP">
            <td style="background:D6CFB2; font-weight:bold; font-size:larger" colspan=2><%=bundle.getString("lb_basic_information")%></td>
        </tr>
        <tr>
            <td class="standardText" width="25%">
                <%= lbVendorId %>:
            </td>
            <td class="standardTextBold">
                <amb:readOnlyData access='<%=(String)hash.get(VendorSecureFields.CUSTOM_ID)%>'
                     value="<%= vendorId %>" />
            </td>
        </tr>
        <tr>
            <td class="standardText">
                <%= lbFirstName %>:
            </td>
            <td class="standardTextBold">
                <amb:readOnlyData access='<%=(String)hash.get(VendorSecureFields.FIRST_NAME)%>'
                     value="<%= firstName %>" />
            </td>
        </tr>
        <tr>
            <td class="standardText">
                <%= lbLastName%>
            </td>
            <td class="standardTextBold">
                <amb:readOnlyData access='<%=(String)hash.get(VendorSecureFields.LAST_NAME)%>'
                     value="<%= lastName %>" />
            </td>
        </tr>
        <tr>
            <td class="standardText">
                <%= lbTitle%>:
            </td>
            <td class="standardTextBold">
                <amb:readOnlyData access='<%=(String)hash.get(VendorSecureFields.TITLE)%>'
                     value="<%= userTitle %>" />
            </td>
        </tr>
        <tr>
            <td class="standardText">
                <%= lbCompanyName%>:
            </td>
            <td class="standardTextBold">
                <amb:readOnlyData access='<%=(String)hash.get(VendorSecureFields.COMPANY)%>'
                     value="<%= companyName %>" />
            </td>
        </tr>
        <tr>
            <td class="standardText">
                <%= lbType%>:
            </td>
            <td class="standardTextBold">
                <amb:readOnlyData access='<%=(String)hash.get(VendorSecureFields.IS_INTERNAL)%>'
                     value="<%= type %>" />
            </td>
        </tr>
        <tr>
            <td class="standardText">
                <%= lbNotes%>:
            </td>
            <td class="standardTextBold">
                <amb:readOnlyData access='<%=(String)hash.get(VendorSecureFields.NOTES)%>'
                     value="<%= notes %>" />
            </td>
        </tr>
        <tr>
            <td class="standardText">
                <%= lbAmbassadorAccess%>:
            </td>
            <td class="standardTextBold">
                <amb:readOnlyData access='<%=(String)hash.get(VendorSecureFields.AMBASSADOR_ACCESS)%>'
                     value="<%= access %>" />
            </td>
        </tr>
        <tr>
            <td class="standardText">
                <%= lbStatus%>:
            </td>
            <td class="standardTextBold">
                <amb:readOnlyData access='<%=(String)hash.get(VendorSecureFields.STATUS)%>'
                     value="<%= bundle.getString(status) %>" />
            </td>
        </tr>
        <tr>
            <td class="standardText">
                <%= lbUsername%>:
            </td>
            <td class="standardTextBold">
                <amb:readOnlyData access='<%=(String)hash.get(VendorSecureFields.USERNAME)%>'
                     value="<%= userName %>" />
            </td>
        </tr>
        </td>
        </tr>
    </table>
    </td>
    </tr>
</table>
