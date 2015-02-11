<%@ taglib uri="/WEB-INF/tlds/globalsight.tld" prefix="amb" %>
<%@ page 
        contentType="text/html; charset=UTF-8"
        errorPage="/envoy/common/error.jsp"
        import="java.util.*,com.globalsight.everest.securitymgr.FieldSecurity,
                com.globalsight.everest.securitymgr.UserSecureFields,
                com.globalsight.everest.servlet.util.SessionManager,
                com.globalsight.everest.util.system.SystemConfigParamNames,
                com.globalsight.everest.util.system.SystemConfiguration,
                com.globalsight.everest.foundation.User,
                com.globalsight.everest.webapp.WebAppConstants,
                com.globalsight.everest.webapp.javabean.NavigationBean,
                com.globalsight.everest.webapp.pagehandler.PageHandler,
                com.globalsight.everest.webapp.pagehandler.administration.users.UserConstants,
                com.globalsight.util.GlobalSightLocale,
                java.util.Iterator,
                java.util.Locale,
                java.util.ResourceBundle" 
        session="true" 
%>
<%@ include file="/envoy/common/installedModules.jspIncl" %>
<% 
    ResourceBundle bundle = PageHandler.getBundle(session);        
   
    //Labels
    String lbUserName = bundle.getString("lb_user_name");
    String lbFirstName = bundle.getString("lb_first_name");
    String lbLastName = bundle.getString("lb_last_name");
	String lbTitle = bundle.getString("lb_title");
	String lbCompanyName = bundle.getString("lb_company_name");
	String lbWssePwd = bundle.getString("lb_wsse_password");
	String lbAccessLevel = bundle.getString("lb_access_level");
	String lbVAccessLevel = bundle.getString("lb_access_level_for_vm");
    String lbOK = bundle.getString("lb_ok");    
        

	//User UI locale 
	Locale userUiLocale = (Locale)session.getAttribute(WebAppConstants.UILOCALE); 	
	
    SessionManager sessionMgr =
         (SessionManager) session.getAttribute(WebAppConstants.SESSION_MANAGER);

    // Get the data
    FieldSecurity hash = (FieldSecurity)
         sessionMgr.getAttribute("security");
    User user = (User) sessionMgr.getAttribute(UserConstants.USER);

    String firstName = user.getFirstName();
    String lastName = user.getLastName();
    String userTitle = user.getTitle();
    if (userTitle == null||"null".equals(userTitle)) userTitle = "";
    String companyName = user.getCompanyName();
    if (companyName == null) companyName = "";
    String userName  = user.getUserName();
    if (userName == null) userName = "";
    String ambGroupName  = (String)request.getAttribute("ambGroup");
    if (ambGroupName == null) ambGroupName = "";
    String vendorGroupName  = (String)request.getAttribute("vendorGroup");
    if (vendorGroupName == null) vendorGroupName = "";
    String wssePwd = user.getWssePassword();
%>
<!-- This JSP is envoy/administration/users/viewBasic.jsp  -->
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
                <%= lbUserName %>:
            </td>
            <td class="standardTextBold">
                <amb:readOnlyData value="<%= userName %>" />
            </td>
        </tr>
        <tr>
            <td class="standardText">
                <%= lbFirstName %>:
            </td>
            <td class="standardTextBold">
                <amb:readOnlyData access='<%=(String)hash.get(UserSecureFields.FIRST_NAME)%>'
                     value="<%= firstName %>" />
            </td>
        </tr>
        <tr>
            <td class="standardText">
                <%= lbLastName%>
            </td>
            <td class="standardTextBold">
                <amb:readOnlyData access='<%=(String)hash.get(UserSecureFields.LAST_NAME)%>'
                     value="<%= lastName %>" />
            </td>
        </tr>
        <tr>
            <td class="standardText">
                <%= lbTitle%>:
            </td>
            <td class="standardTextBold">
                <amb:readOnlyData access='<%=(String)hash.get(UserSecureFields.TITLE)%>'
                     value="<%= userTitle %>" />
            </td>
        </tr>
        <tr>
            <td class="standardText">
                <%= lbCompanyName%>:
            </td>
            <td class="standardTextBold">
                <amb:readOnlyData access='<%=(String)hash.get(UserSecureFields.COMPANY)%>'
                     value="<%= companyName %>" />
            </td>
        </tr>
        <tr>
            <td class="standardText">
                <%= lbWssePwd%>:
            </td>
            <td class="standardTextBold">
                <amb:readOnlyData access='<%=(String)hash.get(UserSecureFields.WSSE_PASSWORD)%>'
                     value="<%= wssePwd %>" />
            </td>
        </tr>
        <tr>
            <td class="standardText">
                <%= lbAccessLevel%>:
            </td>
            <td class="standardTextBold">
                <amb:readOnlyData value="<%= ambGroupName %>" />
            </td>
        </tr>
<% if (b_vendorManagement) { %>
        <tr>
            <td class="standardText">
                <%= lbVAccessLevel%>:
            </td>
            <td class="standardTextBold">
                <amb:readOnlyData value="<%= vendorGroupName %>" />
            </td>
        </tr>
<% } %>
    </table>
    </table>
    </td>
    </tr>
</table>
