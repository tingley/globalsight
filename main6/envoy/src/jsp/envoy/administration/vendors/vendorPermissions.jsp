<%@ taglib uri="/WEB-INF/tlds/globalsight.tld" prefix="amb" %>
<%@ page contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="java.util.*,com.globalsight.everest.webapp.javabean.NavigationBean,
         com.globalsight.util.resourcebundle.ResourceBundleConstants,
         com.globalsight.util.resourcebundle.SystemResourceBundle,
         com.globalsight.everest.foundation.User,
         com.globalsight.everest.permission.PermissionGroup,
         com.globalsight.everest.webapp.pagehandler.PageHandler,
         java.util.ArrayList,
         java.util.Locale, java.util.ResourceBundle" 
         session="true" %>
<jsp:useBean id="skinbean" scope="request"
 class="com.globalsight.everest.webapp.javabean.SkinBean" />
<jsp:useBean id="done" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="create" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="cancelNew" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="cancelEdit" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="prev" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />

<% 
    ResourceBundle bundle = PageHandler.getBundle(session);
    SessionManager sessionMgr =
      (SessionManager)session.getAttribute(WebAppConstants.SESSION_MANAGER);
    String edit = (String) sessionMgr.getAttribute("edit");
    String doneUrl = done.getPageURL()+"&action=donePerms";
    String createUrl = create.getPageURL()+"&action=create";
    String prevUrl = prev.getPageURL()+"&action=prev";
    String cancelUrl = null;

    if (edit != null)
        cancelUrl = cancelEdit.getPageURL() + "&action=cancelEdit";
    else
        cancelUrl = cancelNew.getPageURL() + "&action=cancelNew";
    String title = null;
    if (edit != null)
    {
        title= bundle.getString("lb_edit") + " " + bundle.getString("lb_vendor") +
                 " - " + bundle.getString("lb_permission_groups");
    }
    else
    {
        title= bundle.getString("lb_new") + " " + bundle.getString("lb_vendor") +
                 " - " + bundle.getString("lb_permission_groups");
    }
    // Button names
    String cancelButton = bundle.getString("lb_cancel");
    String doneButton = bundle.getString("lb_done");
    String saveButton = bundle.getString("lb_save");
    String prevButton = bundle.getString("lb_previous");

    // Labels
    String lbAvailable = bundle.getString("lb_available");
    String lbAdded = bundle.getString("lb_added");

    // Data
    ArrayList allPerms = (ArrayList)request.getAttribute("allPerms");
    ArrayList userPermsList = (ArrayList)sessionMgr.getAttribute("userPerms");
%>

<html>
<!-- This JSP is envoy/administration/vendors/vendorPermissions.jsp -->
<head>
<meta http-equiv="content-type" content="text/html;charset=UTF-8">
<title><%= title %></title>
<script language="JavaScript" src="/globalsight/includes/setStyleSheet.js"></script>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>
<script language="JavaScript">
var needWarning = false;
var objectName = "";
var guideNode = "users";
var helpFile = "<%=bundle.getString("help_vendors_permissions")%>";

function submitForm(selectedButton)
{
    savePermGroups();

    if (selectedButton == "Done")
    {
        VendorForm.action = "<%=doneUrl%>";
    }
    else if (selectedButton == "Save")
    {
        VendorForm.action = "<%=createUrl%>";
    }
    else if (selectedButton == "Cancel")
    {
        VendorForm.action = "<%=cancelUrl%>";
    }
    else if (selectedButton == "Prev")
    {
        VendorForm.action = "<%=prevUrl%>";
    }
    VendorForm.submit();
}

//
// Return true if this Permission Group is already assigned to the User
//
function permGroupInList(id)
{
    var to = VendorForm.to;

    for (var i = 0; i < to.length; i++)
    {
        if (to.options[i].value == id)
        {
            return true;
        }
    }

    return false;
}

var first = true;
function addPermGroup()
{
    var from = VendorForm.from;
    var to = VendorForm.to;

    if (from.selectedIndex == -1)
    {
        // put up error message
        alert("<%= bundle.getString("jsmsg_select_permission_group") %>");
        return;
    }
    for (var i = 0; i < from.length; i++)
    {
        if (from.options[i].selected)
        {
            if (permGroupInList(from.options[i].value))
            {
                continue;
            }

            if (first == true)
            {
<%
                if (userPermsList == null || userPermsList.size() == 0)
                {
%>
                to.options[0] = null;
<%
                }
%>
                first = false;
            }

            var len = to.options.length;
            to.options[len] = new Option(from.options[i].text, from.options[i].value);
        }
    }

    savePermGroups();
}

function removePermGroup()
{
    var to = VendorForm.to;

    if (to.selectedIndex == -1)
    {
        alert("<%= bundle.getString("jsmsg_select_permission_group") %>");
        return;
    }

    for (var i = 0; i < to.length; i++)
    {
        if (to.options[i].selected)
        {
            to.options[i] = null;
            i--;
        }
    }

    savePermGroups();
}

function savePermGroups()
{
    if (!VendorForm.to) return;

    var to = VendorForm.to;
    var options_string = "";
    var first = true;

    // Save userids in a comma separated string
    for (var loop = 0; loop < to.options.length; loop++)
    {
        if (first)
        {
            first = false;
        }
        else
        {
            options_string += ",";
        }

        options_string += to.options[loop].value;
    }

    VendorForm.toField.value = options_string;
}
</script>
</head>

<body leftmargin="0" rightmargin="0" topmargin="0" marginwidth="0" marginheight="0" onload="loadGuides()">
<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>

<DIV ID="contentLayer" STYLE=" POSITION: ABSOLUTE; Z-INDEX: 9; TOP: 108; LEFT: 20px; RIGHT: 20px;">
<amb:header title="<%=title%>" />

<form name="VendorForm" method="post">
<input type="hidden" name="toField" >
<table>
    <tr>
        <td>
            <%=lbAvailable%>:
        </td>
        <td>
            &nbsp;
        </td>
        <td>
            <%=lbAdded%>:
        </td>
    </tr>
    <tr>
        <td>
            <select name="from" multiple class="standardText" size=15>
<%
            if (allPerms != null)
            {
                for (int i=0; i < allPerms.size(); i++)
                {
                    PermissionGroup perm = (PermissionGroup)allPerms.get(i);
                    out.println("<option value=\"" + perm.getId() + "\">" +
                                 perm.getName() + "</option>");
                }
            }
%>
            </select>
        </td>
        <td align="center">
            <table class="standardText">
            <tr>
                <td>
                <input type="button" name="addButton" value=" >> "
                onclick="addPermGroup()"><br>
                </td>
              </tr>
              <tr><td>&nbsp;</td></tr>
              <tr>
                <td>
                  <input type="button" name="removedButton" value=" << "
                  onclick="removePermGroup()">
                </td>
              </tr>
            </table>
        </td>
        <td>
            <select name="to" multiple class="standardText" size=15>
<%
            if (userPermsList != null)
            {
                for (int i=0; i < userPermsList.size(); i++)
                {
                    PermissionGroup perm = (PermissionGroup)userPermsList.get(i);
                    out.println("<option value=\"" + perm.getId() + "\">" +
                                 perm.getName() + "</option>");
                }
            }
%>
            </select>
        </td>
    </tr>
</table>
    <P>
    <INPUT TYPE="BUTTON" VALUE="<%=cancelButton%>" onClick="submitForm('Cancel');">
    <% if (edit == null) { %>
    <INPUT TYPE="BUTTON" VALUE="<%=prevButton%>" onClick="submitForm('Prev');">
    <INPUT TYPE="BUTTON" VALUE="<%=saveButton%>" onClick="submitForm('Save');">
    <% } else { %>
    <INPUT TYPE="BUTTON" VALUE="<%=doneButton%>" onClick="submitForm('Done');">
    <% } %>
</form>
</body>
</html>
