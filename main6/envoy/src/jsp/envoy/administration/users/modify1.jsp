<%@page import="com.globalsight.everest.company.CompanyWrapper"%>
<%@page import="com.globalsight.everest.company.CompanyThreadLocal"%>
<%@page import="com.globalsight.everest.servlet.util.ServerProxy"%>
<%@ taglib uri="/WEB-INF/tlds/globalsight.tld" prefix="amb" %>
<%@ page
    contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="java.util.*,
            com.globalsight.everest.foundation.User,
            com.globalsight.calendar.CalendarManagerLocal,
            com.globalsight.everest.securitymgr.FieldSecurity,
            com.globalsight.everest.securitymgr.UserSecureFields,
            com.globalsight.everest.servlet.util.SessionManager,
            com.globalsight.everest.util.system.SystemConfigParamNames,
            com.globalsight.everest.util.system.SystemConfiguration,
            com.globalsight.everest.vendormanagement.VendorManagementLocal,
            com.globalsight.everest.permission.Permission,
            com.globalsight.everest.permission.PermissionSet,                
            com.globalsight.everest.webapp.WebAppConstants,
            com.globalsight.everest.webapp.javabean.NavigationBean,
            com.globalsight.everest.webapp.pagehandler.PageHandler,
            com.globalsight.everest.webapp.pagehandler.administration.users.ModifyUserWrapper,
            com.globalsight.everest.webapp.pagehandler.administration.users.UserHandlerHelper,
            com.globalsight.everest.webapp.pagehandler.administration.users.UserUtil,
            com.globalsight.everest.webapp.pagehandler.administration.calendars.CalendarConstants,
            com.globalsight.everest.webapp.pagehandler.tasks.TaskHelper,
            com.globalsight.util.GlobalSightLocale,
            com.globalsight.util.edit.EditUtil,
            java.util.Iterator,
            java.util.Locale,
            java.util.ResourceBundle,
            com.globalsight.everest.foundation.SSOUserUtil"
    session="true"
%>
<jsp:useBean id="roles" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="contact" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="cals" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="projects" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="save" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="cancel" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="security" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="perms" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="defaultroles" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<%
ResourceBundle bundle = PageHandler.getBundle(session);
PermissionSet myPerms = (PermissionSet) session.getAttribute(WebAppConstants.PERMISSIONS);
String rolesURL = roles.getPageURL();
String contactURL = contact.getPageURL() + "&action=" +
  WebAppConstants. USER_ACTION_MODIFY_USER_CONTACT;
String calsURL = cals.getPageURL()+ "&action=" +
  CalendarConstants.EDIT_ACTION;
String projectsURL = projects.getPageURL()+ "&action=projects";
String saveURL = save.getPageURL() + "&" + WebAppConstants.USER_ACTION +
  "=" + WebAppConstants.USER_ACTION_MODIFY2_USER;
String cancelURL = cancel.getPageURL();
String securityURL = security.getPageURL() + "&action=security";
String permsURL = perms.getPageURL() + "&action=perms";
String defaultRolesURL = defaultroles.getPageURL() + "&action=defaultroles";
String title= bundle.getString("lb_edit_user") + " - " +
  bundle.getString("lb_basic_information");

//Labels
String lbUserName = bundle.getString("lb_user_name");
String lbSsoUserName = bundle.getString("lb_sso_username");
String lbPassword = bundle.getString("lb_password");
String lbPasswordRepeat = bundle.getString("lb_password_repeat");
String lbFirstName = bundle.getString("lb_first_name");
String lbLastName = bundle.getString("lb_last_name");
String lbTitle = bundle.getString("lb_title");
String lbWssePassword = bundle.getString("lb_wsse_password");
String lbCompanyName = bundle.getString("lb_company_name");
String lbAccessLevel = bundle.getString("lb_access_level");
String lbVendorAccessLevel = /*bundle.getString("lb_access_level")*/"Access Level for<BR>Vendor Management:";
String lbChoose = bundle.getString("lb_choose");
String lbUser = bundle.getString("lb_user");
String lbSave = bundle.getString("lb_save");
String lbCancel = bundle.getString("lb_cancel");
String lbRoles = bundle.getString("lb_roles");
String lbContact = bundle.getString("lb_contact_info");
String lbCalendar = bundle.getString("lb_calendar");
String lbProjects = bundle.getString("lb_projects");
String lbSecurity = bundle.getString("lb_security");
String lbPermissions = bundle.getString("lb_permissions");
String lbDefaultRoles = bundle.getString("lb_default_role");

//Messages
String jsmsgUserName = EditUtil.toJavascript(bundle.getString("jsmsg_users_user_name"));
String jsmsgPassword = EditUtil.toJavascript(bundle.getString("jsmsg_users_password"));
String jsmsgRepeatPassword = EditUtil.toJavascript(bundle.getString("jsmsg_users_repeat_password"));
String jsmsgFirstName = EditUtil.toJavascript(bundle.getString("jsmsg_users_first_name"));
String jsmsgLastName = EditUtil.toJavascript(bundle.getString("jsmsg_users_last_name"));
String jsmsgGroup = EditUtil.toJavascript(bundle.getString("jsmsg_group"));
String jsmsgSelectGroup = EditUtil.toJavascript("At least one group must be selected.");

SessionManager sessionMgr = (SessionManager)session.getAttribute(
  WebAppConstants.SESSION_MANAGER);

User user = (User)sessionMgr.getAttribute(WebAppConstants.USER);

//available UI languages
String[] uiLocales = UserHandlerHelper.getUILocales();

String[] userNames = UserUtil.getAllUserNames();
int size = userNames == null ? 0 : userNames.length;

//User UI locale
Locale userUiLocale = (Locale)session.getAttribute(WebAppConstants.UILOCALE);

// Get the user wrapper off the session manager.
ModifyUserWrapper wrapper = (ModifyUserWrapper)sessionMgr.getAttribute(
  WebAppConstants.MODIFY_USER_WRAPPER);

String userName = wrapper.getUserName();
String ssoUserName = null;
String ssoUserId = wrapper.getSsoUserId();
if (ssoUserId != null)
{
	ssoUserName = UserUtil.getUserNameById(ssoUserId);
}
String firstName = wrapper.getFirstName();
String lastName = wrapper.getLastName();
String password = wrapper.getPassword();
if (password == null || password.equals(""))
{
    password = "";
} else {
    password = "***************************";
}
String repeat = password;
String userTitle = wrapper.getTitle();
if("null".equals(userTitle))userTitle = "";
String wssePassword = wrapper.getWssePassword();
if("null".equals(wssePassword)) wssePassword = "";
String companyName = wrapper.getCompanyName();
boolean enableSSO = SSOUserUtil.isCompanyEnableSSO(companyName);
boolean isActive = false;
if (wrapper.getUser() != null)
{
    isActive = wrapper.getUser().isActive();
}
if (userTitle == null) userTitle = "";
if (wssePassword == null) wssePassword = "";
if (companyName == null) companyName = "";

String[] companies = (String[])sessionMgr.getAttribute("companyNames");

boolean isCompanyKnown = false;
if (companies != null)
{
  for (int i = 0; i < companies.length; i++)
  {
    if (companies[i].equals(companyName))
    {
       isCompanyKnown = true;
    }
  }
}

FieldSecurity hash = (FieldSecurity) sessionMgr.getAttribute("securitiesHash");
String securityPermission = (String)hash.get("security");

%>

<HTML>
<!-- This JSP is envoy/administration/users/modify1.jsp -->
<HEAD>
<META HTTP-EQUIV="content-type" CONTENT="text/html;charset=UTF-8">
<TITLE><%= title %></TITLE>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/utilityScripts.js"></SCRIPT>
<SCRIPT LANGUAGE="JavaScript">
var needWarning = true;
var objectName = "<%= bundle.getString("lb_user") %>";
var guideNode = "users";
var helpFile = "<%=bundle.getString("help_user_information")%>";
var company;

function submitForm(btnName)
{
    var theForm;

    if (document.layers)
    {
        theForm = document.layers.contentLayer.document.userForm;
    }
    else
    {
        theForm = document.all.userForm;
    }

    if (btnName == "save")
    {
        theForm.action = "<%=saveURL %>";
    }
    else if (btnName == "cals")
    {
        theForm.action = "<%=calsURL %>";
    }
    else if (btnName == "contact")
    {
        theForm.action = "<%=contactURL %>";
    }
    else if (btnName == "projects")
    {
        theForm.action = "<%=projectsURL %>";
    }
    else if (btnName == "security")
    {
        theForm.action = "<%=securityURL %>";
    }
    else if (btnName == "perms")
    {
        theForm.action = "<%=permsURL %>";
    }
    else if (btnName == "defaultroles")
    {
        theForm.action = "<%=defaultRolesURL %>";
    }

    if (confirmForm(theForm))
    {
        if (userForm.password)
        {
            userForm.password.disabled = false;
            userForm.passwordConfirm.disabled = false;
        }

        theForm.submit();
    }
}

function confirmForm(formSent)
{
	var theUserName = formSent.userName.value;
	theUserName = stripBlanks(theUserName);
    if (isEmptyString(theUserName))
    {
        alert("<%= jsmsgUserName %>");
        formSent.userName.value = "";
        formSent.userName.focus();
        return false;
    }
    
    <% for (int i = 0; i < size; i++) { 
        if (userName.equals(userNames[i]))
        {
     	   continue;
        }
     %>
        if(theUserName.toLowerCase() == "<%= userNames[i].toLowerCase() %>")
        {
           alert('<%=bundle.getString("jsmsg_duplicate_users")%>');
           return false;
        }
     <% } %>

    if (hasSomeSpecialChars(theUserName))
    {
        alert("<%= lbUserName %>" + "<%= bundle.getString("msg_invalid_entry3") %>");
        return false;
    }
    
    if (formSent.password)
    {
        var thePassword = formSent.password.value;
        thePassword = stripBlanks(thePassword);

        <% if (isActive) { %>
            if (isEmptyString(thePassword)) {
                alert("<%= jsmsgPassword %>");
                formSent.password.value = "";
                formSent.password.focus();
                return false;
            }
        <% } %>

        var theRepeat = formSent.passwordConfirm.value;
        theRepeat = stripBlanks(theRepeat);

        // Make sure the repeated password matches the first
        if (theRepeat != thePassword) {
            alert("<%= bundle.getString("jsmsg_users_repeat_password") %>");
            formSent.passwordConfirm.value = "";
            formSent.password.value = "";
            formSent.password.focus();
            return false;
        }
    }

    if (formSent.firstName)
    {
        var theFirst = formSent.firstName.value;
        theFirst = stripBlanks(theFirst);

        if (isEmptyString(theFirst)) {
            alert("<%=jsmsgFirstName%>");
            formSent.firstName.value = "";
            formSent.firstName.focus();
                return false;
        }
        if (hasSpecialChars(theFirst))
        {
            alert("<%= lbFirstName %>" + "<%= bundle.getString("msg_invalid_entry") %>");
            return false;
        }

    }
    if (formSent.lastName)
    {
        var theLast = formSent.lastName.value;
        theLast = stripBlanks(theLast);

        if (isEmptyString(theLast)) {
            alert("<%=jsmsgLastName%>");
            formSent.lastName.value = "";
            formSent.lastName.focus();
                return false;
        }
        if (hasSpecialChars(theLast))
        {
            alert("<%= lbLastName %>" + "<%= bundle.getString("msg_invalid_entry") %>");
            return false;
        }

    }

<% if (b_vendorManagement) {
   // Users must belong to at least one group, either a GlobalSight
   // or Vendor Management group. (If VM is not installed, one group
   // is selected automatically.)
%>
     
<% } %>

    return true;
}

function doLoad()
{
    loadGuides();
}
</SCRIPT>
</HEAD>
<BODY LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0"
 MARGINWIDTH="0" MARGINHEIGHT="0" ONLOAD="doLoad()">
<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>
<DIV ID="contentLayer" STYLE=" POSITION: ABSOLUTE; Z-INDEX: 8; TOP: 108px; LEFT: 20px; RIGHT: 20px;">

<SPAN CLASS="mainHeading"><%=title%></SPAN>
<P></P>

<TABLE CELLSPACING="0" CELLPADDING="4" BORDER="0" CLASS="standardText">
  <FORM NAME="userForm" ACTION="<%=rolesURL%>" METHOD="post" >
  <input type="hidden" name="company">
  <input type="hidden" name="vgroup">
  <TR>
    <TD VALIGN="TOP"><%= lbUserName %><SPAN CLASS="asterisk">*</SPAN>:</TD>
    <TD >
      <amb:textfield maxlength="40" size="40" name="userName" value="<%= userName %>" />
    </TD>
  </TR>
  <% if (enableSSO) { %>
  <TR>
    <TD VALIGN="TOP"><%= lbSsoUserName %>:</TD>
    <TD >
      <amb:textfield maxlength="40" size="40" name="ssoUserName" value="<%= ssoUserName %>" access='<%=(String)hash.get(UserSecureFields.SSO_USER_NAME)%>' />
    </TD>
  </TR>
  <% } %>
  <TR>
    <TD VALIGN="TOP"><%= lbFirstName %><SPAN CLASS="asterisk">*</SPAN>:</TD>
    <TD >
      <amb:textfield maxlength="40" size="40" name="firstName" value="<%= firstName %>" access='<%=(String)hash.get(UserSecureFields.FIRST_NAME)%>' />
    </TD>
  </TR>
  <TR>
    <TD VALIGN="TOP"><%= lbLastName%><SPAN CLASS="asterisk">*</SPAN>:</TD>
    <TD >
      <amb:textfield maxlength="40" size="40" name="lastName" value="<%= lastName %>" access='<%=(String)hash.get(UserSecureFields.LAST_NAME)%>' />
    </TD>
  </TR>
  
  <!-- Added for alias name, for remember password in Firefox  -->
  <TR style="display:none;">
    <TD></TD>
    <TD>
      <input type="text" name="aliasUserName" value="<%= userName %>"/>
    </TD>
  </TR>
  <!-- End Added for alias name,...  -->
  
  <TR>
    <TD VALIGN="TOP"><%= lbPassword %>
    <% if (isActive) { %>
        <SPAN CLASS="asterisk">*</SPAN>
    <% } %>
    :</TD>
    <TD>
      <amb:password size="20" name="password"
      value="<%=password%>" access='<%=(String)hash.get(UserSecureFields.PASSWORD)%>' />
    </TD>
  </TR>
  <TR>
    <TD VALIGN="TOP"><%= lbPasswordRepeat %>
    <% if (isActive) { %>
        <SPAN CLASS="asterisk">*</SPAN>
    <% } %>
    :</TD>
    <TD >
      <amb:password size="20" name="passwordConfirm"
      value="<%=repeat%>" access='<%=(String)hash.get(UserSecureFields.PASSWORD)%>' />
    </TD>
  </TR>
  <TR>
    <TD VALIGN="TOP"><%= lbTitle %>:</TD>
    <TD>
      <amb:textfield maxlength="40" size="40" name="title" value="<%= userTitle %>" access='<%=(String)hash.get(UserSecureFields.TITLE)%>' />
    </TD>
  </TR>
  <TR>
    <TD VALIGN="TOP"><%= lbWssePassword %>:</TD>
    <TD>
      <amb:textfield maxlength="40" size="40" name="wssePassword" value="<%= wssePassword %>" access='<%=(String)hash.get(UserSecureFields.WSSE_PASSWORD)%>' />
    </TD>
  </TR>
  <TR>
    <TD VALIGN="TOP"><%= lbCompanyName %>:</TD>  
    <TD>
      <B><%=companyName%></B>
      <INPUT TYPE='hidden' NAME='companyName' VALUE='<%=companyName%>'>
      <INPUT TYPE='hidden' NAME='company' VALUE='true'>
    </TD>          
  </TR>
  </FORM>

  <TR>
    <TD COLSPAN="2">&nbsp;</TD>
  </TR>
  <TR>
    <TD COLSPAN="2" ALIGN="RIGHT">
      <INPUT TYPE="BUTTON" NAME="<%=lbCancel%>" VALUE="<%=lbCancel%>"
      onclick="location.replace('<%=cancelURL%>')">
      <INPUT TYPE="BUTTON" NAME="<%=lbRoles%>" VALUE="<%=lbRoles%>..."
      onclick="submitForm('roles')">
      <%
      if (CompanyWrapper.isSuperCompanyName(companyName)) {
      %>
      <amb:permission name="<%=Permission.SET_DEFAULT_ROLES%>" >
        <INPUT TYPE="BUTTON" name="<%=lbDefaultRoles %>" VALUE="<%=lbDefaultRoles%>..." onClick="submitForm('defaultroles');">
      </amb:permission>
      <%
      }
      %>
      
      <INPUT TYPE="BUTTON" NAME="<%=lbContact%>" VALUE="<%=lbContact%>..."
      onclick="submitForm('contact')">
<% if (b_calendaring){ %>
      <INPUT TYPE="BUTTON" NAME="<%=lbCalendar%>" VALUE="<%=lbCalendar%>..."
      onclick="submitForm('cals')">
<%
    }
%>
      <INPUT TYPE="BUTTON" NAME="<%=lbProjects%>" VALUE="<%=lbProjects%>..."
      onclick="submitForm('projects')">
<% if ("shared".equals(securityPermission)) { %>
      <INPUT TYPE="BUTTON" NAME="<%=lbSecurity%>" VALUE="<%=lbSecurity%>..."
      onclick="submitForm('security')">
<%
    }
%>
      <INPUT TYPE="BUTTON" NAME="<%=lbPermissions%>" VALUE="<%=lbPermissions%>..."
      onclick="submitForm('perms')">
      
      <INPUT TYPE="BUTTON" NAME="<%=lbSave%>" VALUE="<%=lbSave%>"
      onclick="submitForm('save')">
    </TD>
  </TR>
</TABLE>
</DIV>
</BODY>
</HTML>
