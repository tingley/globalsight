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
                 java.util.*"
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

    String tbId = (String)sessionMgr.getAttribute("tbId");
    String tbName = (String)sessionMgr.getAttribute("tbName");
    // UI fields

    // Labels, etc
    String lbcancel = bundle.getString("lb_cancel");
    String lbsave = bundle.getString("lb_save");
    String lbAvailable = bundle.getString("lb_available");
    String lbAdded = bundle.getString("lb_added");

    String saveURL = save.getPageURL() + "&"+WebAppConstants.TERMBASE_ACTION+"="
                     +WebAppConstants.TERMBASE_ACTION_SAVEUSERS+"&" 
                     + WebAppConstants.RADIO_BUTTON + "=" + tbId;
    String cancelURL = cancel.getPageURL();

    String title = null;
    String helpFile = null;
    helpFile = bundle.getString("help_terminology_users");
    title = bundle.getString("lb_edit") + " " +
          bundle.getString("lb_termbase") +
          "(" + tbName+ ")" + " - " + bundle.getString("lb_users");

    // Data
    Vector avaliableUsers = (Vector)sessionMgr.getAttribute("availableUsers");
    ArrayList usersAdded =  (ArrayList)sessionMgr.getAttribute("usersAdded");
%>
<!-- This JSP is tbUsers.jsp -->
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
var guideNode = "terminology";
var helpFile = "<%=helpFile%>";

function submitForm(formAction)
{
    if (formAction == "cancel")
    {
        if (confirm("<%= bundle.getString("jsmsg_jump_warning_1")%>"
            +" <%=bundle.getString("lb_termbase")%>"+". "
            +"<%=bundle.getString("jsmsg_jump_warning_2")%>"))
        {
        	tbUsersForm.action = "<%=cancelURL%>";
        }
        else
        {
            return;
        }
    }
    else if (formAction == "save")
    {
        saveUserIds();
        tbUsersForm.action = "<%=saveURL%>";
    }
    tbUsersForm.submit();
}

var first = true;
function addUser()
{
    var from = tbUsersForm.from;
    var to = tbUsersForm.to;

    if (from.selectedIndex == -1)
    {
        // put up error message
        alert("<%= bundle.getString("jsmsg_select_user")%>");
        return;
    }

    for (var i = from.length-1; i >= 0 ; i--)
    {
        if (from.options[i].selected)
        {
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
            
            from.options[i] = null;
        }
    }

    saveUserIds();
}

function removeUser()
{
	var from = tbUsersForm.from;
    var to = tbUsersForm.to;

    if (to.selectedIndex == -1)
    {
        alert("<%= bundle.getString("jsmsg_select_user") %>");
        return;
    }

    for (var i = to.length-1; i >= 0; i--)
    {
        if (to.options[i].selected)
        {
		    var len = from.options.length;
            from.options[len] = new Option(to.options[i].text, to.options[i].value);
            to.options[i] = null;
        }
    }

    saveUserIds();
}

function saveUserIds()
{
    if (!tbUsersForm.to) return;

    var to = tbUsersForm.to;
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

    tbUsersForm.toField.value = options_string;
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

<form name="tbUsersForm" method="post" action="">
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
					if (usersAdded != null)
					{
						boolean isExist = false;  //if the user is existed in the right list, return true.
						for (int j = 0; j < usersAdded.size(); j++)
						{
						    User addedUser = (User)usersAdded.get(j);
							if(addedUser.getUserId().equals(user.getUserId())) isExist = true;
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
      <select name="to" multiple class="standardText" size=20>
<%
            if (usersAdded != null)
            {
                for (int i = 0; i < usersAdded.size(); i++)
                {
                    User user= (User)usersAdded.get(i);
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
<INPUT TYPE="BUTTON" VALUE="<%=lbsave%>" onclick="submitForm('save');">
</form>
</div>
</body>
</html>
