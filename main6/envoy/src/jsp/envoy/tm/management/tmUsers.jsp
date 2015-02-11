<%@ page contentType="text/html; charset=UTF-8"
         errorPage="/envoy/common/error.jsp"
         import="com.globalsight.everest.servlet.util.SessionManager,
                 com.globalsight.util.edit.EditUtil,
                 com.globalsight.everest.webapp.WebAppConstants,
                 com.globalsight.everest.webapp.javabean.NavigationBean,
                 com.globalsight.everest.webapp.pagehandler.PageHandler,
                 com.globalsight.everest.foundation.User,
                 com.globalsight.everest.webapp.webnavigation.LinkHelper,
                 com.globalsight.everest.servlet.util.ServerProxy,
                 com.globalsight.everest.servlet.EnvoyServletException,
                 com.globalsight.util.FormUtil,
                 com.globalsight.util.GeneralException,
                 java.util.*,
                 com.globalsight.everest.projecthandler.ProjectTM"
          session="true"
%>
<jsp:useBean id="cancel" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="save" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<%
    ResourceBundle bundle = PageHandler.getBundle(session);
    Locale uiLocale = (Locale)session.getAttribute(WebAppConstants.UILOCALE);
    SessionManager sessionMgr =
      (SessionManager)session.getAttribute(WebAppConstants.SESSION_MANAGER);

    ProjectTM tm = (ProjectTM)sessionMgr.getAttribute("tm");
    // UI fields

    // Labels, etc
    String lbcancel = bundle.getString("lb_cancel");
    String lbsave = bundle.getString("lb_save");
    String lbAvailable = bundle.getString("lb_available");
    String lbAdded = bundle.getString("lb_added");

    String saveURL = save.getPageURL() + "&"+WebAppConstants.TM_ACTION+"="
                     +WebAppConstants.TM_ACTION_SAVEUSERS+"&"
                     +WebAppConstants.RADIO_TM_ID + "=" + tm.getId();
    String cancelURL = cancel.getPageURL();

    String title = null;
    String helpFile = null;
    helpFile = bundle.getString("help_tm_users");
    title = bundle.getString("lb_edit") + " " +
          bundle.getString("lb_tm") +
          "(" + tm.getName() + ")" + " - " + bundle.getString("lb_users");

    // Data
    Vector avaliableUsers = (Vector)sessionMgr.getAttribute("availableUsers");
    ArrayList usersAdded =  (ArrayList)sessionMgr.getAttribute("usersAdded");
%>
<!-- This JSP is tmUsers.jsp -->
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
var objectName = "";
var guideNode = "tm";
var helpFile = "<%=helpFile%>";

function submitForm(formAction)
{
    if (formAction == "cancel")
    {
        if (confirm("<%= bundle.getString("jsmsg_jump_warning_1")%>"
            +" <%=bundle.getString("lb_tm2")%>"+". "
            +"<%=bundle.getString("jsmsg_jump_warning_2")%>"))
        {
        	tmUsersForm.action = "<%=cancelURL%>";
        }
        else
        {
            return;
        }
    }
    else if (formAction == "save")
    {
        saveUserIds();
        tmUsersForm.action = "<%=saveURL%>";
    }
    tmUsersForm.submit();
}


//
// Return true if this TM is already assigned to the User
//
function userInList(id)
{
    var to = tmUsersForm.to;

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
    var from = tmUsersForm.from;
    var to = tmUsersForm.to;

    if (from.selectedIndex == -1)
    {
        // put up error message
        alert("<%= bundle.getString("jsmsg_select_user")%>");
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
                if (usersAdded == null || usersAdded.size() == 0)
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

    saveUserIds();
}

function removeUser()
{
    var to = tmUsersForm.to;

    if (to.selectedIndex == -1)
    {
        alert("<%= bundle.getString("jsmsg_select_user") %>");
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

    saveUserIds();
}

function saveUserIds()
{
    if (!tmUsersForm.to) return;

    var to = tmUsersForm.to;
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

    tmUsersForm.toField.value = options_string;
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

<form name="tmUsersForm" method="post" action="">
<input type="hidden" name="toField">
<table>
  <tr>
    <td class=standardText><%=lbAvailable%>:</td>
    <td>&nbsp;</td>
    <td class=standardText><%=lbAdded%>:</td>
  </tr>
  <tr>
    <td>
      <select name="from" multiple class="standardText" size=20>
<%
            if (avaliableUsers != null)
            {
                for (int i = 0; i < avaliableUsers.size(); i++)
                {
                    User user = (User)avaliableUsers.elementAt(i);
                    out.println("<option value=\"" + user.getUserId() + "\">" +
                            user.getUserId() +"</option>");
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
      <select name="to" multiple class="standardText" size=20>
<%
            if (usersAdded != null)
            {
                for (int i = 0; i < usersAdded.size(); i++)
                {
                    User user= (User)usersAdded.get(i);
                    out.println("<option value=\"" + user.getUserId() + "\">" +
                                 user.getUserId() + "</option>");
                }
            }
%>
      </select>
    </td>
  </tr>
</table>

<P>
<INPUT TYPE="BUTTON" VALUE="<%=lbcancel%>" onclick="submitForm('cancel');">
<INPUT TYPE="BUTTON" VALUE="<%=lbsave%>" onclick="submitForm('save');">
</form>
</div>
</body>
</html>
