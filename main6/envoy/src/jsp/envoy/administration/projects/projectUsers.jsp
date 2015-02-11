<%@ page contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="java.util.*,com.globalsight.everest.webapp.javabean.NavigationBean,
         com.globalsight.util.resourcebundle.ResourceBundleConstants,
         com.globalsight.util.resourcebundle.SystemResourceBundle,
         com.globalsight.everest.usermgr.UserInfo,
         com.globalsight.everest.util.comparator.UserInfoComparator,
         com.globalsight.everest.webapp.pagehandler.PageHandler,
         com.globalsight.everest.foundation.User,
         com.globalsight.everest.webapp.pagehandler.administration.users.UserUtil,
         com.globalsight.everest.projecthandler.Project,
         com.globalsight.util.FormUtil,
         java.text.MessageFormat,
         java.util.ArrayList,
         java.util.Iterator,
         java.util.Locale,
         java.util.List,
         java.util.Set,
         java.util.ResourceBundle" 
         session="true"
%>
<jsp:useBean id="skinbean" scope="application"
 class="com.globalsight.everest.webapp.javabean.SkinBean" />
<jsp:useBean id="done" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="save" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="prev" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="self" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="cancel" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<% 
    ResourceBundle bundle = PageHandler.getBundle(session);
    SessionManager sessionManager =
      (SessionManager)session.getAttribute(WebAppConstants.SESSION_MANAGER);
    String saveUrl = save.getPageURL()+"&action=create";
    String doneUrl = done.getPageURL()+"&action=saveUsers";
    String prevUrl = prev.getPageURL()+"&action=previous";
    String selfUrl = self.getPageURL();
    String cancelUrl = cancel.getPageURL()+"&action=cancel";

    String title= bundle.getString("lb_new_project") + " - " +
         bundle.getString("lb_user_information");
    String editTitle= bundle.getString("msg_edit_project") + " - " +
         bundle.getString("lb_user_information");

    // Labels of the column titles
    String nameCol = bundle.getString("lb_name");
    String firstNameCol = bundle.getString("lb_first_name");
    String lastNameCol = bundle.getString("lb_last_name");

    // Button names
    String cancelButton = bundle.getString("lb_cancel");
    String prevButton = bundle.getString("lb_previous");
    String saveButton = bundle.getString("lb_save");
    String doneButton = bundle.getString("lb_done");

    // Data for the page
    List defUsers = (List)request.getAttribute("defUsers");

    // Paging Info
    int pageNum = ((Integer)request.getAttribute("pageNum")).intValue();

    int numPages = ((Integer)request.getAttribute("numPages")).intValue();

    int listSize = defUsers == null ? 0 : defUsers.size();
    int totalDefUsers = ((Integer)request.getAttribute("listSize")).intValue();

    int defUsersPerPage = ((Integer)request.getAttribute(
        "numPerPage")).intValue();
    int defUserPossibleTo = pageNum * defUsersPerPage;
    int defUserTo = defUserPossibleTo > totalDefUsers ? totalDefUsers : defUserPossibleTo;
    int defUserFrom = (defUserTo - listSize) + 1;
    Integer sortChoice = (Integer)sessionManager.getAttribute("sorting");

    Project project = (Project)sessionManager.getAttribute("project");
    ArrayList<String> addedUsersIds = (ArrayList<String>)request.getAttribute("addedUsersIds");
    ArrayList possibleUsers = (ArrayList)sessionManager.getAttribute("possibleUsers");
%>
<HTML>
<HEAD>
<META HTTP-EQUIV="content-type" CONTENT="text/html;charset=UTF-8">
<TITLE><%= title %></TITLE>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>
<SCRIPT LANGUAGE="JavaScript">
    var needWarning = false;
    var objectName = "";
    var guideNode = "projects";
    var helpFile = "<%=bundle.getString("help_project_users")%>";

function submitForm(formAction)
{
    if (formAction == "cancel")
    {
       if (confirmJump())
       {
           projectForm.action = "<%=cancelUrl%>";
           projectForm.submit();
           return;
       }
       else
       {
          return false;
       }
    }
    else if (formAction == "prev")
    {
       if (confirmJump())
       {
           projectForm.action = "<%=prevUrl%>";
           projectForm.submit();
           return;
       }
       else
       {
          return false;
       }
    }
    else if (formAction == "saveUsers")
    {
        // From edit
        projectForm.action = "<%=doneUrl%>";
    }
    else if (formAction == "save")
    {
        // From new
        projectForm.action = "<%=saveUrl%>";
    }
    else
    {
        // Got here from sort or next/prev
        projectForm.action = formAction;
    }
    projectForm.submit();
}

//
// Return true if this User is already part of the project
//
function userInList(id)
{
    var to = projectForm.to;
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
    var from = projectForm.from;
    var to = projectForm.to;
    if (from.selectedIndex == -1)
    {
        // put up error message
        alert("<%= bundle.getString("jsmsg_user_select") %>");
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
                if (addedUsersIds.isEmpty())
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
	var from = projectForm.from;
    var to = projectForm.to;

    if (to.selectedIndex == -1)
    {
        alert("<%= bundle.getString("jsmsg_user_select") %>");
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
    var to = projectForm.to;
    var options_string = "";
    var first = true;
    // Save userids in a comma separated string
    for (loop=0; loop < to.options.length; loop++)
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
    projectForm.toField.value = options_string;
}

//adjust select tag width, by fan 
function changeSelectWidth(selected){
	if(selected.options[selected.selectedIndex].text.length*7 >= 220)  selected.style.width=selected.options[selected.selectedIndex].text.length*7 + 'px';
	else selected.style.width=200;
}

</SCRIPT>
<style type="text/css">
.list {
    border: 1px solid <%=skinbean.getProperty("skin.list.borderColor")%>;
}
</style>
</HEAD>
<BODY LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0" MARGINWIDTH="0" MARGINHEIGHT="0"
    ONLOAD="loadGuides()">
<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>
    <DIV ID="contentLayer" STYLE=" POSITION: ABSOLUTE; Z-INDEX: 9; TOP: 108; LEFT: 20px; RIGHT: 20px;">
    <span class="mainHeading">
    <% if (sessionManager.getAttribute("edit") == null)
            out.println(title);
       else
            out.println(editTitle);
    %>
    </span>
    <p>
    <div class="standardText"><%=bundle.getString("msg_default_users")%>:</div>
    <TABLE CELLPADDING=0 CELLSPACING=0 BORDER=0 CLASS="standardText">
        <TR VALIGN="TOP">
            <TD ALIGN="RIGHT">
        <%
        // Make the Paging widget
        if (listSize > 0)
        {
            Object[] args = {new Integer(defUserFrom), new Integer(defUserTo),
                     new Integer(totalDefUsers)};

            // "Displaying x to y of z"
            out.println(MessageFormat.format(
                    bundle.getString("lb_displaying_records"), args));

            out.println("<br>");
            out.println("&lt; ");

            // The "Previous" link
            if (pageNum == 1) {
                // Don't hyperlink "Previous" if it's the first page
                out.print(bundle.getString("lb_previous"));
            }
            else
            {
                int num = pageNum - 1;
                String prevStr = selfUrl + "&pageNum=" + num +
                                 "&sorting=" + sortChoice;
%>
                <a href="javascript:submitForm('<%=prevStr%>')"><%=bundle.getString("lb_previous")%></A>
<%
            }

            out.print(" ");

            // Print out the paging numbers
            for (int i = 1; i <= numPages; i++)
            {
                // Don't hyperlink the page you're on
                if (i == pageNum)
                {
                    out.print("<b>" + i + "</b>");
                }
                // Hyperlink the other pages
                else
                {
                    String nextStr = selfUrl + "&pageNum=" + i + "&sorting=" + sortChoice;
%>
                    <a href="javascript:submitForm('<%=nextStr%>')"><%=i%></A>
<%
                }
                out.print(" ");
            }
            // The "Next" link
            if (defUserTo >= totalDefUsers) {
                // Don't hyperlink "Next" if it's the last page
                out.print(bundle.getString("lb_next"));
            }
            else
            {
                int num = pageNum + 1;
                String nextStr = selfUrl + "&pageNum=" + num +
                                 "&sorting=" + sortChoice;
%>
                <a href="javascript:submitForm('<%=nextStr%>')"><%=bundle.getString("lb_next")%></A>

<%
            }
            out.println(" &gt;");
        }
%>
          </td>
        <tr>
          <td>
<form name="projectForm" method="post">
<input type="hidden" name="toField" value="<%=(String)request.getAttribute("toField")%>">
<!-- Project data table -->
  <table border="0" cellspacing="0" cellpadding="4" class="list">
    <tr class="tableHeadingBasic" valign="bottom" style="padding-bottom: 3px;">
      <td style="padding-right: 90px;">
        <% String col1 = selfUrl + "&pageNum=" + pageNum + "&sorting=" +
            UserInfoComparator.USERID + "&doSort=true";
        %>
        <a class="sortHREFWhite" href="javascript:submitForm('<%=col1%>')"> <%=nameCol%></a>
      </td>
      <td style="padding-right: 10px;" width="90px" nowrap>
        <% String col2 = selfUrl + "&pageNum=" + pageNum + "&sorting=" +
            UserInfoComparator.FIRSTNAME + "&doSort=true";
        %>
        <a class="sortHREFWhite" href="javascript:submitForm('<%=col2%>')"> <%=firstNameCol%></a>
      </td>
      <td style="padding-right: 10px;">
        <% String col3 = selfUrl + "&pageNum=" + pageNum + "&sorting=" +
            UserInfoComparator.LASTNAME + "&doSort=true";
        %>
        <a class="sortHREFWhite" href="javascript:submitForm('<%=col3%>')"> <%=lastNameCol%></a>
      </td>
    </tr>
<%
        if (listSize == 0)
        {
%>
        <tr>
          <td colspan=3 class='standardText'><%=bundle.getString("msg_no_default_users")%></td>
        </tr>
<%
        }
        else
        {
              for (int i=0; i < listSize; i++)
              {
                String color = (i%2 == 0) ? "#FFFFFF" : "#EEEEEE";
                UserInfo userInfo = (UserInfo)defUsers.get(i);
%>
                <tr style="padding-bottom:5px; padding-top:5px;"
                  valign=top bgcolor="<%=color%>">
                  <td><span class="standardText">
                    <%=userInfo.getUserName()%>
                  </td>
                  <td><span class="standardText">
                    <%=userInfo.getFirstName()%>
                  </td>
                  <td><span class="standardText">
                    <%=userInfo.getLastName()%>
                  </td>
                </tr>
<%
              }
        }
%>
  </tbody>
  </table>
<!-- End Data Table -->
</TD>
</TR>
</TABLE>
<table border="0" cellpadding="0" cellspacing="0">
  <tr><td>&nbsp;</td></tr>
  <tr><td>&nbsp;</td></tr>
  <tr>
    <td class="standardText" colspan="3">
        <%=bundle.getString("msg_add_remove_users")%>
    </td>
  </tr>
  <tr><td>&nbsp;</td></tr>
  <tr>
    <td class="standardText">
        <%=bundle.getString("lb_available")%>:
    </td>
    <td>&nbsp;</td>
    <td class="standardText">
        <%=bundle.getString("lb_added")%>:
    </td>
  </tr>
    <tr>
        <td>
        <select name="from" multiple class="standardText" size=15 style="width:200px" onchange="changeSelectWidth(this)">
<%
		if (possibleUsers != null)
		{
			for (int i = 0; i < possibleUsers.size(); i++)
			{
				UserInfo userInfo = (UserInfo)possibleUsers.get(i);

				//for GBS-1995,by fan
				//don't display the element in the left list ,if the the element is existed in the right list.
				if (!addedUsersIds.isEmpty())
				{
					boolean isExist = false;  //if the user is existed in the right list, return true.

					Iterator<String> iter = addedUsersIds.iterator();

					while(iter.hasNext())
					{
						String userId = iter.next();
						if(userId.equals(userInfo.getUserId())) isExist = true;

					}
					if(!isExist)
					{		
%>
						<option value="<%=userInfo.getUserId()%>" ><%=userInfo.getUserName()%></option>
<%
					}

				}
				else
				{
%>
						<option value="<%=userInfo.getUserId()%>" ><%=userInfo.getUserName()%></option>
<%
				}
			}
		}
%>
        </select>
        </td>
        <td align="center">
          <table>
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
            <select name="to" multiple class="standardText" size=15 style="width:200px" onchange="changeSelectWidth(this)">
<%
                if (!addedUsersIds.isEmpty())
                {
                    Iterator<String> iter = addedUsersIds.iterator();
                    while (iter.hasNext())
                    {
                       String userId = iter.next();

%>
                       <option value="<%=userId%>" ><%=UserUtil.getUserNameById(userId)%></option>
<%
                    }
                }
                else
                {
%>
                   <option>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</option>
<%
                }
%>
            </select>
          </td>
      </tr>
      <tr>
        <td colspan="3" style="padding-top:10px">
          <input type="button" name="<%=cancelButton %>" value="<%=cancelButton %>"
            onclick="submitForm('cancel')">
    <% if (sessionManager.getAttribute("edit") == null) { %>
          <input type="button" name="<%=prevButton %>" value="<%=prevButton %>"
            onclick="submitForm('prev')">
          <input type="button" name="<%=saveButton %>" value="<%=saveButton %>"
            onclick="submitForm('save')">
    
    <% } else { %>
          <input type="button" name="<%=doneButton %>" value="<%=doneButton %>"
            onclick="submitForm('saveUsers')">
    <% } %>
        </td>
      </tr>
</table>

<% String tokenName = FormUtil.getTokenName(FormUtil.Forms.NEW_PROJECT); %>
<input type="hidden" name="<%=tokenName%>" value="<%=request.getAttribute(tokenName)%>" />

</form>
</BODY>
</HTML>
