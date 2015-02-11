<%@ page contentType="text/html; charset=UTF-8"
         errorPage="/envoy/common/activityError.jsp"
         import="com.globalsight.everest.servlet.util.SessionManager,
                 com.globalsight.util.edit.EditUtil,
                 com.globalsight.everest.webapp.WebAppConstants,
                 com.globalsight.everest.webapp.javabean.NavigationBean,
                 com.globalsight.everest.webapp.pagehandler.PageHandler,
                 com.globalsight.everest.webapp.pagehandler.administration.permission.PermGroupBasicHandler,
                 com.globalsight.everest.permission.Permission,
                 com.globalsight.everest.permission.PermissionGroup,
                 com.globalsight.everest.webapp.webnavigation.LinkHelper,
                 com.globalsight.everest.servlet.util.ServerProxy,
                 com.globalsight.everest.servlet.EnvoyServletException,
                 com.globalsight.util.GeneralException,
                 java.util.*"
          session="true"
%>
<jsp:useBean id="cancel" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="next" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="perms" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="users" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="save" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<%
    ResourceBundle bundle = PageHandler.getBundle(session);
    Locale uiLocale = (Locale)session.getAttribute(WebAppConstants.UILOCALE);
    SessionManager sessionMgr =
      (SessionManager)session.getAttribute(WebAppConstants.SESSION_MANAGER);

    // UI fields

    // Labels, etc
    String lbcancel = bundle.getString("lb_cancel");
    String lbnext = bundle.getString("lb_next");
    String lbsave = bundle.getString("lb_save");
    String lbusers = bundle.getString("lb_users");
    String lbperms = bundle.getString("lb_permissions");

    String nextURL = next.getPageURL() + "&action=create";
    String saveURL = save.getPageURL() + "&action=save";
    String permsURL = perms.getPageURL() + "&action=perms";
    String usersURL = users.getPageURL() + "&action=users";

    boolean edit = false;
    String title = null;
    String helpFile = null;
    if (sessionMgr.getAttribute("edit") != null)
    {
        edit = true;
        helpFile = bundle.getString("help_permission_edit_group_basic");
        title = bundle.getString("lb_edit") + " " +
          bundle.getString("lb_permission_group") + " - " +
          bundle.getString("lb_basic_information");
    }
    else
    {
        helpFile = bundle.getString("help_permission_new_group_basic");
        title = bundle.getString("lb_new") + " " +
          bundle.getString("lb_permission_group") + " - " +
          bundle.getString("lb_basic_information");
    }
    
    String cancelURL = cancel.getPageURL() + "&action=cancel";

    // Data
    ArrayList names = (ArrayList)request.getAttribute("names");
    PermissionGroup permGroup =
      (PermissionGroup)sessionMgr.getAttribute("permGroup");
    String permGroupName = "";
    String desc = "";
    if (permGroup != null)
    {
        permGroupName = permGroup.getName();
        desc = permGroup.getDescription();
        if (desc == null) desc = "";
    }
%>
<html>
<head>
<title><%=title%></title>
<script SRC="/globalsight/includes/utilityScripts.js"></script>
<script SRC="/globalsight/includes/setStyleSheet.js"></script>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>
<script>
var needWarning = true;
var objectName = "<%=bundle.getString("lb_permission_group")%>";
var guideNode="permissionGroups";
var helpFile = "<%=helpFile%>";

function submitForm(formAction)
{
    if (formAction == "cancel")
    {
        if (confirmJump())
            permForm.action = "<%=cancelURL%>";
    }
    else
    {
        if (!confirmForm()) return;
        if (formAction == "next")
        {
            permForm.action = "<%=nextURL%>";
        }
        else if (formAction == "users")
        {
            permForm.action = "<%=usersURL%>";
        }
        else if (formAction == "perms")
        {
            permForm.action = "<%=permsURL%>";
        }
        else if (formAction == "save")
        {
            permForm.action = "<%=saveURL%>";
        }
    }
    permForm.submit();
}

//
// Check required fields.
// Check duplicate group name.
//
function confirmForm()
{
    if (isEmptyString(permForm.nameField.value))
    {
        alert("<%=EditUtil.toJavascript(bundle.getString("jsmsg_perm_name"))%>");
        permForm.nameField.value = "";
        permForm.nameField.focus();
        return false;
    }        
    if (hasSpecialChars(permForm.nameField.value))
    {
        alert("<%=EditUtil.toJavascript(bundle.getString("lb_name"))%>" +
          "<%=EditUtil.toJavascript(bundle.getString("msg_invalid_entry"))%>");
        return false;
    }

    // check for dups 
<%
    if (names != null)
    {
        for (int i = 0; i < names.size(); i++)
        {
            String permname = (String)names.get(i);
%>
            if ("<%=permname%>".toLowerCase() == permForm.nameField.value.toLowerCase())
            {
                alert("<%=EditUtil.toJavascript(bundle.getString("msg_duplicate_perm_group"))%>");
                return false;
            }
<%
        }
    }
%>

    return true;
}


</script>
</head>

<body leftmargin="0" rightrmargin="0" topmargin="0" marginwidth="0"
 marginheight="0" onload="loadGuides()">
<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>
<div id="contentLayer" style="position: absolute; z-index: 9; top: 108; left: 20px; right: 20px;">
<span class="mainHeading"><%=title%></span>
<br>
<br>

<form name="permForm" method="post" action="">

<table border="0" cellspacing="2" cellpadding="2" class="standardText">
  <tr><td></td></tr>
  <tr valign="top">
    <td>
      <table border="0" class="standardText" cellpadding="2">
	<tr>
	  <td>
	    <%=bundle.getString("lb_name")%><span class="asterisk">*</span>:
	  </td>
	  <td>
	    <input type="textfield" name="nameField" <%=(edit? "disabled" : "")%> maxlength="40" size="30"
	    value="<%=permGroupName%>">
	  </td>
	</tr>
	<tr>
	  <td valign="top">
	    <%=bundle.getString("lb_description")%>:
	  </td>
	  <td colspan="2">
	    <textarea rows="6" cols="40" name="descField"><%=desc%></textarea>
	  </td>
	</tr>
	<tr><td>&nbsp;</td></tr>
	<tr>
	  <td colspan=2>
	    <input type="button" name="cancel" value="<%=lbcancel%>"
            onclick="submitForm('cancel')">
<% if (edit) { %>
	    <input type="button" name="perms" value="<%=lbperms%>"
            onclick="submitForm('perms')">
	    <input type="button" name="users" value="<%=lbusers%>"
            onclick="submitForm('users')">
	    <input type="button" name="save" value="<%=lbsave%>"
            onclick="submitForm('save')">
<% } else { %>
	    <input type="button" name="next" value="<%=lbnext%>"
            onclick="submitForm('next')">
<% } %>
	  </td>
	</tr>
      </table>
    </td>
  </tr>
</table>
</form>
</div>
</body>
</html>
