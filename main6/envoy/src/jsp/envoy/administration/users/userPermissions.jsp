<%@ taglib uri="/WEB-INF/tlds/globalsight.tld" prefix="amb" %>
<%@ page contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="java.util.*,com.globalsight.everest.webapp.javabean.NavigationBean,
         com.globalsight.util.resourcebundle.ResourceBundleConstants,
         com.globalsight.util.resourcebundle.SystemResourceBundle,
         com.globalsight.everest.foundation.User,
         com.globalsight.everest.permission.PermissionGroup,
         com.globalsight.everest.webapp.pagehandler.PageHandler,
         com.globalsight.everest.webapp.pagehandler.administration.users.CreateUserWrapper,
         com.globalsight.everest.webapp.pagehandler.administration.users.ModifyUserWrapper,
         com.globalsight.everest.webapp.pagehandler.administration.users.UserConstants,
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
    String doneUrl = done.getPageURL()+"&action=donePermission";
    String createUrl = create.getPageURL() + "&action=" +
      WebAppConstants.USER_ACTION_CREATE_USER;
    String prevUrl = prev.getPageURL() + "&action=prev";
    String cancelUrl = null;

    if (edit != null)
        cancelUrl = cancelEdit.getPageURL() + "&action=cancelEdit";
    else
        cancelUrl = cancelNew.getPageURL() + "&action=cancelNew";
    String title = null;
    if (edit != null)
    {
        title= bundle.getString("lb_edit") + " " + bundle.getString("lb_user") +
          " - " + bundle.getString("lb_permission_groups");
    }
    else
    {
        title= bundle.getString("lb_new") + " " + bundle.getString("lb_user") +
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
    
    String lbUserName = bundle.getString("lb_user_name");

    // Data
    ArrayList allPerms = (ArrayList)request.getAttribute("allPerms");
    ArrayList userPermsList = (ArrayList)sessionMgr.getAttribute("userPerms");
    
    String userName = "";
    if (edit != null)
    {
        ModifyUserWrapper wrapper = (ModifyUserWrapper)sessionMgr.getAttribute(
          UserConstants.MODIFY_USER_WRAPPER);
        userName = wrapper.getUserName();
    } else
    {
        CreateUserWrapper wrapper = (CreateUserWrapper)sessionMgr.getAttribute(
          UserConstants.CREATE_USER_WRAPPER);
        userName = wrapper.getUserName();
    }
    
    //Is current user Super User?
    boolean isCurrentUserSuperUser = ((Boolean) request.getAttribute("isCurrentUserSuperUser")).booleanValue();      
%>
<html>
<!-- This JSP is envoy/administration/users/userPermissions.jsp -->
<head>
<title><%= title %></title>
<script src="/globalsight/includes/setStyleSheet.js"></script>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>
<script language="JavaScript">
var needWarning = false;
var objectName = "";
var guideNode = "users";
var helpFile = "<%=bundle.getString("help_user_permissions")%>";

function submitForm(selectedButton)
{
    savePermGroups();

    if (selectedButton == "Done")
    {
        UserForm.action = "<%=doneUrl%>";
    }
    else if (selectedButton == "Save")
    {
        UserForm.action = "<%=createUrl%>";
    }
    else if (selectedButton == "Cancel")
    {
        UserForm.action = "<%=cancelUrl%>";
    }
    else if (selectedButton == "Prev")
    {
        UserForm.action = "<%=prevUrl%>";
    }
    UserForm.submit();
}

//
// Return true if this Permission Group is already assigned to the User
//
function permGroupInList(id)
{
    var to = UserForm.to;

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
    var from = UserForm.from;
    var to = UserForm.to;

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

			//for GBS-1995,by fan
		    //set the selected element of left list is empty
		    from.options[i] = null;
            i--;
        }
    }

    savePermGroups();
}

function removePermGroup()
{
	var from = UserForm.from;
    var to = UserForm.to;

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

    savePermGroups();
}

function savePermGroups()
{
    if (!UserForm.to) return;

    var to = UserForm.to;
    var options_string = "";
    var first = true;

    if (UserForm.to.type == "select-multiple")
    {
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
		}
		else
		{
		    for (var loop = 0; loop < to.options.length; loop++)
		    {
		        if (to.options[loop].selected)
		        {
		            options_string = to.options[loop].value;
		            break;
		        }
		    }
		}

    UserForm.toField.value = options_string;
}

//adjust select tag width, by fan 
function changeSelectWidth(selected){
	if(selected.options[selected.selectedIndex].text.length*7 >= 220)  selected.style.width=selected.options[selected.selectedIndex].text.length*7 + 'px';
	else selected.style.width=200;
}
</script>
</head>

<body leftmargin="0" rightmargin="0" topmargin="0" marginwidth="0"
 marginheight="0" onload="loadGuides()">
<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>

<DIV ID="contentLayer" STYLE=" POSITION: ABSOLUTE; Z-INDEX: 9; TOP: 108; LEFT: 20px; RIGHT: 20px;">
<amb:header title="<%=title%>" />

<form name="UserForm" method="post">
<input type="hidden" name="toField">
<% 
  if (!isCurrentUserSuperUser)
  {
%>
<table>
  <tr>
    <td class="standardText" nowrap>
      <b><%=lbUserName%>:</b>&nbsp;<%= userName %>
    </td>
    <td></td>
    <td>
    </td>
    <td></td>
  </tr>
  <tr>
    <td class="standardTextBold">
      <%=lbAvailable%>:
    </td>
    <td>&nbsp;</td>
    <td class="standardTextBold">
      <%=lbAdded%>:
    </td>
  </tr>
  <tr>
    <td>
      <select name="from" multiple class="standardText" size=15 style="width:200px" onchange="changeSelectWidth(this)">   
<%
            if (allPerms != null)
            {
                for (int i=0; i < allPerms.size(); i++)
                {
                    PermissionGroup perm = (PermissionGroup)allPerms.get(i);
//                    out.println("<option value=\"" + perm.getId() + "\">" +
//                                 perm.getName() + "</option>");

					//for GBS-1995,by fan
					//don't display the element in the left list ,if the the element is existed in the right list.
					if (userPermsList != null)
					{
						boolean isExist = false;  //if the user is existed in the right list, return true.
						for (int j = 0; j < userPermsList.size(); j++)
						{
							PermissionGroup addedPerm = (PermissionGroup)userPermsList.get(j);
							if(addedPerm.getName().equals(perm.getName())) isExist = true;
						}
						if(!isExist)
						{		
%>
							<option value="<%=perm.getId()%>" ><%=perm.getName()%></option>
<%
						}
					}
					else
					{
%>
							<option value="<%=perm.getId()%>" ><%=perm.getName()%></option>
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
      <select name="to" multiple class="standardText" size=15 style="width:200px" onchange="changeSelectWidth(this)">    
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
<%
  }
  else
  {
%>
<table>
  <tr>
    <td class="standardTextBold">
      <%=lbAvailable%>:
    </td>
    <td>&nbsp;</td>
    <td>
      <select name="to" class="standardText" size="15" style="width:200px" onchange="changeSelectWidth(this)">
<%
						PermissionGroup curPerm = null;
				    if (userPermsList != null && userPermsList.size() > 0)
				    {
				        curPerm = (PermissionGroup)userPermsList.get(0);
				    }
				    
            if (allPerms != null)
            {
                for (int i=0; i < allPerms.size(); i++)
                {
                    PermissionGroup perm = (PermissionGroup)allPerms.get(i);
                    StringBuffer sb = new StringBuffer();
                    sb.append("<option value=\"").append(perm.getId()).append("\"");
                    if (curPerm != null && (curPerm.getId() == perm.getId()))
                    {
                        sb.append("selected");
                    }
                    sb.append(">").append(perm.getName()).append("</option>");
                    out.println(sb.toString());
                }
            }
%>        
      </select>
    </td>
  </tr>
</table>
<%
  }
%>
<P>
<INPUT TYPE="BUTTON" VALUE="<%=cancelButton%>" onClick="submitForm('Cancel');">
<% if (edit == null) { %>
<INPUT TYPE="BUTTON" VALUE="<%=prevButton%>" onClick="submitForm('Prev');">
<INPUT TYPE="BUTTON" VALUE="<%=saveButton%>" onClick="submitForm('Save');">
<% } else { %>
<INPUT TYPE="BUTTON" VALUE="<%=doneButton%>" onClick="submitForm('Done');">
<% } %>
</form>
</div>
</body>
</html>
