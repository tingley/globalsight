<%@ taglib uri="/WEB-INF/tlds/globalsight.tld" prefix="amb" %>
<%@ page contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="java.util.*,com.globalsight.everest.webapp.javabean.NavigationBean,
         com.globalsight.util.resourcebundle.ResourceBundleConstants,
         com.globalsight.util.resourcebundle.SystemResourceBundle,
         com.globalsight.everest.securitymgr.FieldSecurity,
         com.globalsight.everest.webapp.WebAppConstants,
         com.globalsight.everest.webapp.pagehandler.PageHandler,
         com.globalsight.everest.webapp.pagehandler.administration.users.UserConstants,
         com.globalsight.everest.webapp.pagehandler.administration.users.UserComparator,
         com.globalsight.everest.webapp.pagehandler.administration.users.UserHandlerHelper,
         com.globalsight.everest.webapp.pagehandler.administration.users.UserSearchParams,
         com.globalsight.everest.permission.Permission,
         com.globalsight.everest.permission.PermissionSet,         
         com.globalsight.everest.foundation.User,
         com.globalsight.everest.company.CompanyWrapper,
         java.text.MessageFormat,
         java.util.ArrayList,
         java.util.Locale, java.util.ResourceBundle, java.util.Vector" 
         session="true" %>
<jsp:useBean id="skinbean" scope="application"
 class="com.globalsight.everest.webapp.javabean.SkinBean" />
<jsp:useBean id="self" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="new1" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="edit" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="details" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="remove" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="advsearch" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="search" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="users" scope="request" class="java.util.ArrayList" />

<% 
    ResourceBundle bundle = PageHandler.getBundle(session);
    SessionManager sessionMgr =
      (SessionManager)session.getAttribute(WebAppConstants.SESSION_MANAGER);
    PermissionSet perms = (PermissionSet) session.getAttribute(WebAppConstants.PERMISSIONS);
    String selfUrl = self.getPageURL();
    String detailsUrl = details.getPageURL()+"&action=details";
    String newUrl = new1.getPageURL()+"&action=new";
    String editUrl = edit.getPageURL()+"&action=edit";
    String removeUrl = remove.getPageURL()+"&action=remove";
    String advsearchUrl = advsearch.getPageURL()+"&action=search";
    String searchUrl = search.getPageURL()+"&action=search";

    String title= bundle.getString("lb_users");
    String helperText = bundle.getString("helper_text_user_list");

    String confidential = "[" + bundle.getString("lb_confidential") + "]";

    // Button names
    String newButton = bundle.getString("lb_new");
    String editButton = bundle.getString("lb_edit");
    String detailsButton = bundle.getString("lb_details");
    String removeButton = bundle.getString("lb_remove");
    String searchButton = bundle.getString("lb_search");

    
    ArrayList securities = (ArrayList)request.getAttribute("securities");
    String deps = (String)request.getAttribute(UserConstants.DEPENDENCIES);
    UserSearchParams fromSearch = (UserSearchParams)sessionMgr.getAttribute("fromSearch");
    String emptyMsg  = "msg_no_users";
    if (fromSearch != null)
         emptyMsg = "msg_no_users_search";
    String preReqData = (String)request.getAttribute("preReqData");

    // UserID of the user who is logged in
    String loggedInUser = ((User)sessionMgr.getAttribute(WebAppConstants.USER)).getUserId();
    
    boolean isSuperAdmin = ((Boolean) session.getAttribute(WebAppConstants.IS_SUPER_ADMIN)).booleanValue();
    
    String error = (String)request.getAttribute("errorMsg");
    if (error == null)
    {
        error = "";
    }
%>
<html>
<head>
<!-- This JSP is: envoy/administration/users/userMain.jsp -->
<meta http-equiv="content-type" content="text/html;charset=UTF-8">
<title><%= title %></title>
<script language="JavaScript" SRC="/globalsight/includes/setStyleSheet.js"></script>
<script language="JavaScript" SRC="/globalsight/includes/radioButtons.js"></script>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>
<script language="JavaScript">
    var needWarning = false;
    var objectName = "";
    var guideNode = "users";
    var helpFile = "<%=bundle.getString("help_users_main_screen")%>";


<%  ArrayList userlist = (ArrayList) request.getAttribute("users");
%>

function enableButtons(obj)
{
    userId = obj.value;
    userForm.detailsBtn.disabled = false;
    if (userForm.editBtn)
        userForm.editBtn.disabled = true;
    if (userForm.removeBtn)
        userForm.removeBtn.disabled = true;

    //simplified perms, if a user can remove, he can remove anyone
    //if he can edit, he can edit anyone, including himself
    <% if (perms.getPermissionFor(Permission.USERS_EDIT)) { %>
        if (userForm.editBtn)
            userForm.editBtn.disabled = false;
    <% } %>
    <% if (perms.getPermissionFor(Permission.USERS_REMOVE)) { %>
        if (userForm.removeBtn)
            userForm.removeBtn.disabled = false;
    <% } %>
}

function submitForm(selectedButton)
{
    // otherwise do the following
    if (selectedButton == 'New')
    {
<%/*
        if (preReqData != null)
        {
%>
            alert("<%=preReqData%>");
            return;
<%
        }*/
%>
        userForm.action = "<%=newUrl%>";
        userForm.submit();
        return;
    }

    if (selectedButton == "Search")
    {
        userForm.action = "<%=searchUrl%>";
        userForm.submit();
        return;
    }
    if (selectedButton == "AdvSearch")
    {
        userForm.action = "<%=advsearchUrl%>";
        userForm.submit();
        return;
    }

    if (selectedButton == "Details")
    {
        userForm.action = "<%=detailsUrl%>";
    }
    else if (selectedButton == "Edit")
    {
        userForm.action = "<%=editUrl%>";
    }
    else if (selectedButton == "Remove")
    {
        if (!confirm("<%=bundle.getString("msg_confirm_user_removal")%>"))
        {
            return;
        }
        userForm.action = "<%=removeUrl%>";
    }

    userForm.submit();
}
</script>
</head>
<body leftmargin="0" rightmargin="0" topmargin="0" marginwidth="0" marginheight="0" onload="loadGuides()">
<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>


    <DIV ID="contentLayer" STYLE=" POSITION: ABSOLUTE; Z-INDEX: 9; TOP: 108; LEFT: 20px; RIGHT: 20px;">
<% if (deps != null) {
    sessionMgr.removeElement(UserConstants.DEPENDENCIES);
%>
    <amb:header title="<%=title%>" helperText="<%=deps%>" />
<%   } else {  %>
    <amb:header title="<%=title%>" helperText="<%=helperText%>" />
<% }  %>

<form name="userForm" method="post">
      <table border="0" class="standardText" cellpadding="2">
        <tr>
          <td class="standardText">
            <%=bundle.getString("lb_user_name")%>:
          </td>
          <td class="standardText">
            <select name="nameTypeOptions">
                <option value='<%=UserSearchParams.USERNAME_TYPE%>'><%= bundle.getString("lb_user_name") %></option>
                <option value='<%=UserSearchParams.FIRSTNAME_TYPE%>'><%= bundle.getString("lb_first_name") %></option>
                <option value='<%=UserSearchParams.LASTNAME_TYPE%>'><%= bundle.getString("lb_last_name") %></option>
            </select>
            <select name="nameOptions">
                <option value='<%=UserSearchParams.BEGINSWITH_FILTER%>'><%= bundle.getString("lb_begins_with") %></option>
                <option value='<%=UserSearchParams.ENDSWITH_FILTER%>'><%= bundle.getString("lb_ends_with") %></option>
                <option value='<%=UserSearchParams.CONTAINS_FILTER%>'><%= bundle.getString("lb_contains") %></option>
            </select>
            <input type="text" size="30" name="nameField">
          </td>
          <td>
            <input type="button" value="<%=searchButton%>..." onClick="submitForm('Search');">
          </td>
          <td class="standardText" style="padding-bottom: 2px">
            <a class="standardHREF" href="<%=advsearchUrl%>"><%= bundle.getString("lb_advanced_search") %></a>
          </td>
        </tr>
      </table>

<p>
<div style='color:red'><%=error%></div> 
    <table cellpadding=0 cellspacing=0 border=0 class="standardText">
        <tr valign="top">
          <td align="right">
            <amb:tableNav bean="users" key="<%=UserConstants.USER_KEY%>"
                 pageUrl="self" />
          </td>
        <tr>
          <td>
  <% int i = 0; %>
  <amb:table bean="users" id="user" key="<%=UserConstants.USER_KEY%>"
         dataClass="com.globalsight.everest.foundation.User" pageUrl="self"
         emptyTableMsg="<%=emptyMsg%>" >
    <amb:column label="" width="20px">
        <input type="radio" name="radioBtn" value="<%=user.getUserId()%>"
            onclick="enableButtons(this)">
    </amb:column>
    <amb:column label="lb_user_name" sortBy="<%=UserComparator.USERNAME%>" width="120">
        <%= user.getUserId() %>
    </amb:column>
    <amb:column label="lb_first_name" sortBy="<%=UserComparator.FIRSTNAME%>" width="120">
    <%
    String name = user.getFirstName();
    if (securities != null)
    {
        FieldSecurity security = (FieldSecurity)securities.get(i);
        String access = (String)security.get("firstName");
        if (access.equals("hidden"))
        {
            name = confidential;
        }
    }
        out.print(name);
    %>
    </amb:column>
    <amb:column label="lb_last_name" sortBy="<%=UserComparator.LASTNAME%>" width="120">
    <%

        String name = user.getLastName();
        if (securities != null)
        {
            FieldSecurity security = (FieldSecurity)securities.get(i);
            String access = (String)security.get("lastName");
            if (access.equals("hidden"))
            {
                name = confidential;
            }
        }

        out.print(name);
    %>
    </amb:column>
    <% if (isSuperAdmin) { %>
    <amb:column label="lb_company_name" sortBy="<%=UserComparator.ASC_COMPANY%>" width="120">
      <%=user.getCompanyName()%>
    </amb:column>
    <amb:column label="lb_active" width="20px">
    <%
        if (user.isActive())
            out.println("<img src=/globalsight/images/checkmark.gif height=9 width=13 hspace=10 vspace=3>");
    %>
    </amb:column>
    <% } %>
  </amb:table>

</TD>
</TR>
</DIV>
<TR><TD>&nbsp;</TD></TR>

<TR>
<TD align="right">
    <P>
    <amb:permission name="<%=Permission.USERS_REMOVE%>" >
    <INPUT TYPE="BUTTON" VALUE="<%=removeButton%>" onClick="submitForm('Remove');"
        name="removeBtn" disabled>
    </amb:permission>
    <INPUT TYPE="BUTTON" VALUE="<%=detailsButton%>..." onClick="submitForm('Details');"
        name="detailsBtn" disabled>
    <amb:permission name="<%=Permission.USERS_EDIT%>" >
    <INPUT TYPE="BUTTON" VALUE="<%=editButton%>..." onClick="submitForm('Edit');"
        name="editBtn" disabled>
    </amb:permission>
    <amb:permission name="<%=Permission.USERS_NEW%>" >
    <INPUT TYPE="BUTTON" VALUE="<%=newButton%>..." onClick="submitForm('New');">
    </amb:permission>

</TD>
</TR>
</TABLE>
</form>
</body>
</html>
