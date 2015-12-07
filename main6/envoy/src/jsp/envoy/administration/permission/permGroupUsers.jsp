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
                 com.globalsight.everest.permission.PermissionSet,
                 com.globalsight.everest.foundation.User,
                 com.globalsight.everest.webapp.webnavigation.LinkHelper,
                 com.globalsight.everest.servlet.util.ServerProxy,
                 com.globalsight.everest.servlet.EnvoyServletException,
                 com.globalsight.util.FormUtil,
                 com.globalsight.util.GeneralException,
                 java.util.*"
          session="true"
%>
<jsp:useBean id="cancel" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="cancelEdit" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="prev" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="save" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="done" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<%
    ResourceBundle bundle = PageHandler.getBundle(session);
    Locale uiLocale = (Locale)session.getAttribute(WebAppConstants.UILOCALE);
    SessionManager sessionMgr =
      (SessionManager)session.getAttribute(WebAppConstants.SESSION_MANAGER);

    // UI fields

    // Labels, etc
    String lbcancel = bundle.getString("lb_cancel");
    String lbprev = bundle.getString("lb_previous");
    String lbsave = bundle.getString("lb_save");
    String lbdone = bundle.getString("lb_done");
    String lbpermissions = bundle.getString("lb_permissions");
    String lbAvailable = bundle.getString("lb_available");
    String lbAdded = bundle.getString("lb_added");

    String prevURL = prev.getPageURL() + "&action=prev";
    String saveURL = save.getPageURL() + "&action=create";
    String doneURL = done.getPageURL() + "&action=doneUsers";
    String cancelURL = null;

    boolean edit = false;
    String title = null;
    String helpFile = null;
    if (sessionMgr.getAttribute("edit") != null)
    {
        edit = true;
        helpFile = bundle.getString("help_permission_edit_group_user");
        String pgname = (String)sessionMgr.getAttribute("permGroupName");
        title = bundle.getString("lb_edit") + " " +
          bundle.getString("lb_permission_group") +
          "(" + pgname + ")" + " - " + bundle.getString("lb_users");
        cancelURL = cancelEdit.getPageURL() + "&action=cancel";
    }
    else
    {
        helpFile = bundle.getString("help_permission_new_group_user");
        title = bundle.getString("lb_new") + " " +
          bundle.getString("lb_permission_group") + " - " +
          bundle.getString("lb_users");
        cancelURL = cancel.getPageURL() + "&action=cancel";
    }

    // Data
    Vector allUsers = (Vector)sessionMgr.getAttribute("allUsers");
    ArrayList usersForPermGroup =
      (ArrayList)sessionMgr.getAttribute("usersForPermGroup");
%>
<html>
<head>
<title><%=title%></title>
<script SRC="/globalsight/includes/utilityScripts.js"></script>
<script SRC="/globalsight/envoy/administration/permission/tree.js"></script>
<script SRC="/globalsight/includes/setStyleSheet.js"></script>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>
<script>
var needWarning = false;
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
    else if (formAction == "save")
    {
        saveUserIds();
        permForm.action = "<%=saveURL%>";
    }
    else if (formAction == "done")
    {
        saveUserIds();
        permForm.action = "<%=doneURL%>";
    }
    else if (formAction == "prev")
    {
        saveUserIds();
        permForm.action = "<%=prevURL%>";
    }
    permForm.submit();
}


//
// Return true if this Permission Group is already assigned to the User
//
function userInList(id)
{
    var to = permForm.to;

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
function addUser()
{
    var from = permForm.from;
    var to = permForm.to;

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
            if (userInList(from.options[i].value))
            {
                continue;
            }

            if (first == true)
            {
<%
                if (usersForPermGroup == null || usersForPermGroup.size() == 0)
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

			//for GBS-1995,by fan
		    //set the selected element of left list is empty
		    from.options[i] = null;
            i--;
        }
    }

    saveUserIds();
}

function removeUser()
{
	var from = permForm.from;
    var to = permForm.to;

    if (to.selectedIndex == -1)
    {
        alert("<%= bundle.getString("jsmsg_select_permission_group") %>");
        return;
    }

    for (var i = 0; i < to.length; i++)
    {
        if (to.options[i].selected)
        {

			//for GBS-1995,by fan
		    //add selected element to left list
		    var len = from.options.length;
            from.options[len] = new Option(to.options[i].text, to.options[i].value);

            to.options[i] = null;
            i--;
        }
    }

    saveUserIds();
}

function saveUserIds()
{
    if (!permForm.to) return;

    var to = permForm.to;
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

    permForm.toField.value = options_string;
}

//adjust select tag width, by fan 
function changeSelectWidth(selected){
	if(selected.options[selected.selectedIndex].text.length*7 >= 220)  selected.style.width=selected.options[selected.selectedIndex].text.length*7 + 'px';
	else selected.style.width=200;
}
</script>
</head>

<body id="idBody" leftmargin="0" rightrmargin="0" topmargin="0" marginwidth="0"
 marginheight="0" onload="loadGuides()">
<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>
<div id="contentLayer" style="position: absolute; z-index: 9; top: 108; left: 20px; right: 20px;">
<span class="mainHeading"><%=title%></span>
<br>
<br>

<form name="permForm" method="post" action="">
<input type="hidden" name="toField">
<table>
  <tr>
    <td class=standardText><%=lbAvailable%>:</td>
    <td>&nbsp;</td>
    <td class=standardText><%=lbAdded%>:</td>
  </tr>
  <tr>
    <td>
      <select name="from" multiple class="standardText" size=20 style="width:200px" onchange="changeSelectWidth(this)">
<%
            if (allUsers != null)
            {
                for (int i = 0; i < allUsers.size(); i++)
                {
                    User user = (User)allUsers.elementAt(i);

					//for GBS-1995,by fan
					//don't display the element in the left list ,if the the element is existed in the right list.
					if (usersForPermGroup != null)
					{
						boolean isExist = false;  //if the user is existed in the right list, return true.
						for (int j = 0; j < usersForPermGroup.size(); j++)
						{
							User addedUser= (User)usersForPermGroup.get(j);
							if(addedUser.getUserName().equals(user.getUserName())) isExist = true;
						}
						if(!isExist)
						{		
%>
							<option value="<%=user.getUserId()%>" ><%=user.getUserName()%></option>
<%
						}
					}
					else
					{
%>
							<option value="<%=user.getUserId()%>" ><%=user.getUserName()%></option>
<%
					}
				
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
	    onclick="addUser()"><br>
	  </td>
	</tr>
	<tr><td>&nbsp;</td></tr>
	<tr>
	  <td>
	    <input type="button" name="removedButton" value=" << "
	    onclick="removeUser()">
	  </td>
	</tr>
      </table>
    </td>
    <td>
      <select name="to" multiple class="standardText" size=20 style="width:200px" onchange="changeSelectWidth(this)">
<%
            if (usersForPermGroup != null)
            {
                for (int i = 0; i < usersForPermGroup.size(); i++)
                {
                    User user= (User)usersForPermGroup.get(i);
                    out.println("<option value=\"" + user.getUserId() + "\">" +
                                 user.getUserName() + "</option>");
                }
            }
%>
      </select>
    </td>
  </tr>
</table>

<P>
<INPUT TYPE="BUTTON" VALUE="<%=lbcancel%>" onclick="submitForm('cancel');">
<% if (!edit) { %>
<INPUT TYPE="BUTTON" VALUE="<%=lbprev%>" onclick="submitForm('prev');">
<INPUT TYPE="BUTTON" VALUE="<%=lbsave%>" onclick="submitForm('save');">

<% String tokenName = FormUtil.getTokenName(FormUtil.Forms.NEW_PERMISSION_GROUP); %>
<input type="hidden" name="<%=tokenName%>" value="<%=request.getAttribute(tokenName)%>" />

<% } else { %>
<INPUT TYPE="BUTTON" VALUE="<%=lbdone%>" onclick="submitForm('done');">
<% } %>

</form>
</div>
</body>
</html>
