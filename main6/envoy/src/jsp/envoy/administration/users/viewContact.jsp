<%@ taglib uri="/WEB-INF/tlds/globalsight.tld" prefix="amb" %>
<%@ page 
        contentType="text/html; charset=UTF-8"
        errorPage="/envoy/common/error.jsp"
        import="java.util.*,com.globalsight.everest.securitymgr.FieldSecurity,
                com.globalsight.everest.securitymgr.UserSecureFields,
                com.globalsight.everest.servlet.util.SessionManager,
                com.globalsight.everest.util.system.SystemConfigParamNames,
                com.globalsight.everest.util.system.SystemConfiguration,
                com.globalsight.everest.webapp.WebAppConstants,
                com.globalsight.everest.webapp.javabean.NavigationBean,
                com.globalsight.everest.webapp.pagehandler.PageHandler,
                com.globalsight.everest.webapp.pagehandler.administration.users.UserConstants,
                com.globalsight.everest.foundation.User,
                com.globalsight.util.GlobalSightLocale,
                com.globalsight.everest.permission.Permission,
         		com.globalsight.everest.permission.PermissionSet, 
                java.util.Iterator,
                java.util.Locale,
                java.util.ResourceBundle" 
        session="true" 
%>


<% 
    ResourceBundle bundle = PageHandler.getBundle(session);        

   
    String pagetitle= bundle.getString("lb_view") + " " +
                     bundle.getString("lb_user") + " - " +
                     bundle.getString("lb_contact_information");
    
    //Labels
    String lbAddress = bundle.getString("lb_address");
    String lbHomePhone = bundle.getString("lb_home_phone");
    String lbWorkPhone = bundle.getString("lb_work_phone");
    String lbCellPhone = bundle.getString("lb_cell_phone");
    String lbFax = bundle.getString("lb_fax");
    String lbEmail = bundle.getString("lb_email");
    String lbCcEmail = bundle.getString("lb_cc_email");
    String lbBccEmail = bundle.getString("lb_bcc_email");
    String lbEmailLanguage = bundle.getString("lb_email_language");

        

    SessionManager sessionMgr =
         (SessionManager) session.getAttribute(WebAppConstants.SESSION_MANAGER);

    // Field level security
    FieldSecurity hash = (FieldSecurity)
         sessionMgr.getAttribute("security");

    //User UI locale
    Locale userUILocale = (Locale)session.getAttribute(WebAppConstants.UILOCALE);

    // Get the data
    User user = (User) sessionMgr.getAttribute(UserConstants.USER);

    String address = user.getAddress();
    if (address == null||"null".equals(address)) address = "";
    String homePhone = user.getHomePhoneNumber();
    if (homePhone == null||"null".equals(homePhone)) homePhone = "";
    String workPhone = user.getOfficePhoneNumber();
    if (workPhone == null||"null".equals(workPhone)) workPhone = "";
    String cellPhone = user.getCellPhoneNumber();
    if (cellPhone == null||"null".equals(cellPhone)) cellPhone = "";
    String fax = user.getFaxPhoneNumber();
    if (fax == null||"null".equals(fax)) fax = "";
    String email = user.getEmail();
    if (email == null||"null".equals(email)) email = "";
    String ccEmail = user.getCCEmail();
    if (ccEmail == null||"null".equals(ccEmail)) ccEmail = "";
    String bccEmail = user.getBCCEmail();
    if (bccEmail == null||"null".equals(bccEmail)) bccEmail = "";
    Locale locale = PageHandler.getUILocale(user.getDefaultUILocale());
    String emailLanguage = locale.getDisplayLanguage(userUILocale);
%>
<!-- This JSP is envoy/administration/users/viewContact.jsp -->
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
                <amb:readOnlyData access='<%=(String)hash.get(UserSecureFields.ADDRESS)%>'
                     value="<%= address %>" />
            </td>
        </tr>
        <tr>
            <td class="standardText">
                <%= lbHomePhone %>:
            </td>
            <td class="standardTextBold">
                <amb:readOnlyData access='<%=(String)hash.get(UserSecureFields.HOME_PHONE)%>'
                     value="<%= homePhone %>" />
            </td>
        </tr>
        <tr>
            <td class="standardText">
                <%= lbWorkPhone%>:
            </td>
            <td class="standardTextBold">
                <amb:readOnlyData access='<%=(String)hash.get(UserSecureFields.WORK_PHONE)%>'
                     value="<%= workPhone %>" />
            </td>
        </tr>
        <tr>
            <td class="standardText">
                <%= lbCellPhone%>:
            </td>
            <td class="standardTextBold">
                <amb:readOnlyData access='<%=(String)hash.get(UserSecureFields.CELL_PHONE)%>'
                     value="<%= cellPhone %>" />
            </td>
        </tr>
        <tr>
            <td class="standardText">
                <%= lbFax%>:
            </td>
            <td class="standardTextBold">
                <amb:readOnlyData access='<%=(String)hash.get(UserSecureFields.FAX)%>'
                     value="<%= fax %>" />
            </td>
        </tr>
        <tr>
            <td class="standardText">
                <%= lbEmail%>:
            </td>
            <td class="standardTextBold">
                <amb:readOnlyData access='<%=(String)hash.get(UserSecureFields.EMAIL_ADDRESS)%>'
                     value="<%= email %>" />
            </td>
        </tr>
        <amb:permission  name="<%=Permission.USERS_ACCESS_CCEMAIL%>" >
	         <tr>
	            <td class="standardText">
	                <%= lbCcEmail%>:
	            </td>
	            <td class="standardTextBold">
	                <amb:readOnlyData access='<%=(String)hash.get(UserSecureFields.CC_EMAIL_ADDRESS)%>'
	                     value="<%= ccEmail %>" />
	            </td>
	         </tr>
        </amb:permission>
        <amb:permission  name="<%=Permission.USERS_ACCESS_BCCEMAIL%>" >
	        <tr>
	            <td class="standardText">
	                <%= lbBccEmail%>:
	            </td>
	            <td class="standardTextBold">
	                <amb:readOnlyData access='<%=(String)hash.get(UserSecureFields.BCC_EMAIL_ADDRESS)%>'
	                     value="<%= bccEmail %>" />
	            </td>
	        </tr>
	    </amb:permission>
        <tr>
            <td class="standardText">
                <%= lbEmailLanguage%>:
            </td>
            <td class="standardTextBold">
                <amb:readOnlyData access='<%=(String)hash.get(UserSecureFields.EMAIL_LANGUAGE)%>'
                     value="<%= emailLanguage %>" />
            </td>
        </tr>
        </td>
        </tr>
    </table>
   </td>
  </tr>
</table>
