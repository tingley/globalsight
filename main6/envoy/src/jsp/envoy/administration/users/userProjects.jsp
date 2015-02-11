<%@ taglib uri="/WEB-INF/tlds/globalsight.tld" prefix="amb" %>
<%@ page contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="java.util.*,com.globalsight.everest.webapp.javabean.NavigationBean,
            com.globalsight.util.resourcebundle.ResourceBundleConstants,
            com.globalsight.util.resourcebundle.SystemResourceBundle,
            com.globalsight.everest.util.comparator.UserComparator,
            com.globalsight.everest.company.CompanyWrapper,
            com.globalsight.everest.webapp.WebAppConstants,
            com.globalsight.everest.webapp.pagehandler.PageHandler,
            com.globalsight.everest.webapp.pagehandler.administration.users.CreateUserWrapper,
            com.globalsight.everest.webapp.pagehandler.administration.users.ModifyUserWrapper,
            com.globalsight.everest.webapp.pagehandler.administration.users.UserStateConstants,
            com.globalsight.everest.webapp.pagehandler.administration.users.UserConstants,
            com.globalsight.everest.foundation.User,
            com.globalsight.everest.projecthandler.Project,
            com.globalsight.everest.projecthandler.ProjectInfo,
            com.globalsight.everest.permission.Permission,
            com.globalsight.everest.permission.PermissionSet,
            java.text.MessageFormat,
            java.util.ArrayList,
            java.util.List,
            java.util.Iterator,
            java.util.Locale,
            java.util.Set,
            java.util.Vector,
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
<jsp:useBean id="next" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="self" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="cancelEdit" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="cancelNew" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<%
ResourceBundle bundle = PageHandler.getBundle(session);
SessionManager sessionMgr =
  (SessionManager)session.getAttribute(WebAppConstants.SESSION_MANAGER);
String companyName = null;
String userName = null;
String[] companies = CompanyWrapper.getAllCompanyNames();
PermissionSet perms = (PermissionSet)session.getAttribute(WebAppConstants.PERMISSIONS);

// check if creating/editing a user or vendor
boolean edit = true;
if (sessionMgr.getAttribute("editUser") == null)
    edit = false;
boolean vendor = true;
if (sessionMgr.getAttribute("vendor") == null)
    vendor = false;


// initial company name
if (edit)
{
    ModifyUserWrapper wrapper = (ModifyUserWrapper)sessionMgr.getAttribute(
      UserConstants.MODIFY_USER_WRAPPER);
    companyName = wrapper.getCompanyName(); 
    userName = wrapper.getUserName();
} else
{
    CreateUserWrapper wrapper = (CreateUserWrapper)sessionMgr.getAttribute(
      UserConstants.CREATE_USER_WRAPPER);
    companyName = wrapper.getCompanyName();
    userName = wrapper.getUserName();
}

String saveUrl = save.getPageURL()+"&action=" +
  WebAppConstants.USER_ACTION_CREATE_USER;
String doneUrl = done.getPageURL()+"&action=" +
  WebAppConstants.USER_ACTION_MODIFY_USER_PROJECTS;
String prevUrl = prev.getPageURL()+"&action=previous";
String nextUrl = next.getPageURL()+"&action=next";
String cancelUrl = null;
if (edit)
    cancelUrl = cancelEdit.getPageURL()+"&action=cancelEdit";
else
    cancelUrl = cancelNew.getPageURL()+"&action=cancelNew";
String selfUrl = self.getPageURL();

String title;
if (vendor)
{
    if (edit)
    {
        title =  bundle.getString("lb_edit") + " " + bundle.getString("lb_vendor");
    } 
    else
    {
        title =  bundle.getString("lb_new") + " " + bundle.getString("lb_vendor");
    }
    doneUrl = done.getPageURL()+"&action=doneProjects";
}
else
{
    if (edit)
    {
        title= bundle.getString("lb_edit_user");
    }
    else
    {
        title= bundle.getString("lb_new_user");
    }
}
title = title +  " - " + bundle.getString("lb_projects");


// Button names
String cancelButton = bundle.getString("lb_cancel");
String prevButton = bundle.getString("lb_previous");
String nextButton = bundle.getString("lb_next");
String saveButton = bundle.getString("lb_save");
String doneButton = bundle.getString("lb_done");

User user = (User)sessionMgr.getAttribute(WebAppConstants.USER);
// Data for the page
List availableProjects = (List)request.getAttribute("availableProjects");
List addedProjects = (List)request.getAttribute("addedProjects");
String addedProjectsIds = "";

// Initialize addedProjectsIds
if (addedProjects != null && addedProjects.size() > 0)
{
    for(int i = 0; i < addedProjects.size(); i ++)
    {
        Project project = (Project)addedProjects.get(i);
        if (addedProjectsIds != null && addedProjectsIds.trim() != "")
        {
            addedProjectsIds += ",";
        }
        addedProjectsIds += project.getId();
    }
}

%>
<% if (edit) { %>
<jsp:useBean id="projects" scope="request" class="java.util.ArrayList" />
<% } %>
<HTML>
<!-- This JSP is envoy/administration/users/userProjects.jsp-->
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
<% if (vendor) { %>
var helpFile = "<%=bundle.getString("help_vendors_projects")%>";
<% } else { %>
var helpFile = "<%=bundle.getString("help_user_projects")%>";
<% } %>

function submitForm(formAction)
{
    if (!projectForm.companies)
    {
       saveUserIds();
    } else 
    {
       addIdsToField();
    }
    trimToField();
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
       projectForm.action = "<%=prevUrl%>";
       projectForm.submit();
       return;
    }
    else if (formAction == "next")
    {
       projectForm.action = "<%=nextUrl%>";
       projectForm.submit();
       return;
    }
    else if (formAction == "saveUsers")
    {
        // From edit
        <%
            Boolean inAllP = (Boolean)request.getAttribute("future");
            if (inAllP.booleanValue() == true && !perms.getPermissionFor(Permission.GET_ALL_PROJECTS))
            {
        %>
        projectForm.action = "<%=cancelUrl%>";
        projectForm.submit();
        return;
        <%
            } else {
        %>
        projectForm.action = "<%=doneUrl%>";
        <%
            }
        %>
    }
    else if (formAction == "save")
    {
        // From new
        projectForm.action = "<%=saveUrl%>";
    }
    else
    {
        projectForm.action = formAction;
    }

    projectForm.submit();
}

//
// Return true if this User is already part of the project
//
function projectInList(id)
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
function addAll()
{
    var from = projectForm.from;
    var to = projectForm.to;

    if (projectForm.allProjects.checked)
    {
        // move all available projects to added projects
        for (var i = 0; i < from.length; i++)
        {
            if (projectInList(from.options[i].value))
            {
                continue;
            }

            if (first == true)
            {
<%
                if (addedProjects == null || addedProjects.size() == 0)
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
		//for GBS-1995,by fan
		//set left list is empty
		for (var i = 0; i < from.length; )
		{
			from.options[i] = null;
		}
    }
}

function addProject()
{
    var from = projectForm.from;
    var to = projectForm.to;

    if (from.selectedIndex == -1)
    {
        // put up error message
        alert("<%= bundle.getString("jsmsg_select_project") %>");
        return;
    }

    for (var i = 0; i < from.length; i++)
    {
        if (from.options[i].selected)
        {
            if (projectInList(from.options[i].value))
            {
                continue;
            }

            if (first == true)
            {
<%
                if (addedProjects == null || addedProjects.size() == 0)
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
}

function removeProject()
{
    if (projectForm.allProjects && projectForm.allProjects.checked)
    {
        alert("<%=bundle.getString("jsmsg_remove_project")%>");
        return;
    }

    var from = projectForm.from;
    var to = projectForm.to;

    if (to.selectedIndex == -1)
    {
        alert("<%= bundle.getString("jsmsg_select_project") %>");
        return;
    }

    for (var i = 0; i < to.length; i++)
    {
        if (to.options[i].selected)
        {
            // delete this project in "toField"
            var options_string = projectForm.toField.value;
            var options = options_string.split(",");
            for (var loop = 0; loop < options.length; loop ++)
            {
                if (to.options[i].value == options[loop])
                {
                    options[loop] = null;
                }
            }

            // save options into "toField"
            var result_string = "";
            for (var loop = 0; loop < options.length; loop ++)
            {
                if (options[loop] == null) continue;

                if (result_string != "")
                {
                    result_string += ",";
                }
                result_string += options[loop];
            }

            projectForm.toField.value = result_string;

		    //for GBS-1995,by fan
		    //add selected element to left list
		    var len = from.options.length;
            from.options[len] = new Option(to.options[i].text, to.options[i].value);

            to.options[i] = null;
            i--;
        }
    }
}

function trimToField()
{
    var options_string = projectForm.toField.value;
    var options = options_string.split(",");
    var result = new Array();
    for (var i = 0; i < options.length; i ++)
    {
        if (isInList(options[i], result))
        {
            continue;
        } else
        {
            var len = result.length;
            result[len] = options[i];
        }
    }
    // save result Array to "toField"
    var result_string = "";
    for (var loop = 0; loop < result.length; loop ++)
    {
        if (result[loop] == null) continue;

        if (result_string != "")
        {
            result_string += ",";
        }

        result_string += result[loop];
    }
    projectForm.toField.value = result_string;

}

function isInList(o, list)
{
    if (list.length == 0) return false;
    for (var i = 0 ; i < list.length; i ++)
    {
        if (o == list[i]) return true;
    }
    return false;
}


// Add projectIds in "to" selection to hidden input "toField"
function addIdsToField()
{
    if (!projectForm.to) return;

    var selectedCompanyName = projectForm.companies.value
    var to = projectForm.to;
    var options_string = projectForm.toField.value;

    for (var loop = 0; loop < to.options.length; loop++)
    {
        if (options_string != "")
        {
            options_string += ",";
        }

        options_string += to.options[loop].value;
    }

    projectForm.toField.value = options_string;

}

// Save projectIds
function saveUserIds()
{
    if (!projectForm.to) return;

    var to = projectForm.to;
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

    projectForm.toField.value = options_string;
}

function setProjects() 
{
    addIdsToField();
    trimToField();
    var from = projectForm.from;
    var to = projectForm.to;
    var selectedCompanyName = projectForm.companies.value;

    from.options.length = 0;
    to.options.length = 0;
    if (selectedCompanyName == "-1")
    {
        from.options.length = 0;
        to.options.length = 0;
        return;
    }
    <%
    if (availableProjects != null && availableProjects.size() > 0)
    {
    %>
        var fromIndex = 0;
        var toIndex = 0;
    <%
        // set projects in "from" selection
        for(int i = 0; i < availableProjects.size(); i ++)
        {
             Project project = (Project)availableProjects.get(i);
             String projectCompany = CompanyWrapper.getCompanyNameById(project.getCompanyId());
    %>
             if (selectedCompanyName == "<%= projectCompany %>")
             {
            	 //for GBS-1995,by fan, default set the left list is empty
                 from.options[fromIndex] = null;
                 fromIndex ++;

                 // set projects in "to" selection
                 var options_string = projectForm.toField.value;
                 var options = options_string.split(",");
                 if (isInList(<%= project.getId()%>, options))
                 {
                     to.options[toIndex] = new Option("<%= project.getName()%>","<%= project.getId()%>");
                     toIndex ++;
                 }
             }
    <%
        }
    }
    %>


}

//adjust select tag width, by fan 
function changeSelectWidth(selected){
	if(selected.options[selected.selectedIndex].text.length*7 >= 220)  selected.style.width=selected.options[selected.selectedIndex].text.length*7 + 'px';
	else selected.style.width=200;
}

</SCRIPT>
</HEAD>
<BODY LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0" MARGINWIDTH="0" MARGINHEIGHT="0"
    ONLOAD="loadGuides()">
<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>
    <DIV ID="contentLayer" STYLE=" POSITION: ABSOLUTE; Z-INDEX: 9; TOP: 108; LEFT: 20px; RIGHT: 20px;">
    <span class="mainHeading">
        <%= title %>
    </span>
    <p>
    <% if (!edit) { %>
        <div class="standardText">
            <% if (vendor) {%>
                <%=bundle.getString("msg_default_projects_vendors")%>
            <% } else { %>
                <%=bundle.getString("msg_default_projects")%>
            <% } %>

            <%=bundle.getString("lb_use")%>
            <input type="button" name="removedButton" value=" << " >
            <%=bundle.getString("lb_below")%>.
        </div>
    <% } %>
<form name="projectForm" method="post">
<input type="hidden" name="toField" value=<%= addedProjectsIds%>>
<table border="0" bordercolor="green" cellpadding="0" cellspacing="0" class="standardText">
<tr>
<td colspan="2">
<b><%= bundle.getString("lb_user_name")%>:</b>&nbsp;<%= userName %>
</td>
</tr>

<% if (perms.getPermissionFor(Permission.GET_ALL_PROJECTS)) { %>
  <tr>
    <td colspan="3">
      <input type="checkbox" name="allProjects" id="idAllProjects" onclick="addAll()"
<%
      Boolean inAll = (Boolean)request.getAttribute("future");
      if (inAll.booleanValue() == true)
        out.println(" checked");      
%>
>
      <label for="idAllProjects">
      <%=bundle.getString("lb_add_user_to_project")%>
      </label>
    </td>
  </tr>
<% } else { %>
<% if (edit) { %>
  <tr>
    <td colspan="3"><%=bundle.getString("lb_project_default_list")%></td>
  </tr>
  <tr>
    <td colspan=3>
      <TABLE CELLPADDING=0 CELLSPACING=0 BORDER=0 CLASS="standardText">
        <tr VALIGN="TOP">
          <td ALIGN="RIGHT">
             <amb:tableNav bean="projects" key="project"
                    pageUrl="self" />
          </td>
        </tr>
        <tr>
         <td>
           <amb:table bean="projects" id="project" key="project"
            dataClass="com.globalsight.everest.projecthandler.Project" pageUrl="self"
            emptyTableMsg="msg_no_projects" >
           <amb:column label="lb_name" wrap="false" width="50px">
             <%=project.getName()%>
           </amb:column>
           <amb:column label="lb_project_manager" wrap="false" width="50px">
                <%
                    User pm = project.getProjectManager();
                    out.print(pm.getFirstName() + " " + pm.getLastName());
                %>
           </amb:column>
           <amb:column label="lb_description" width="250px">
             <% out.print(project.getDescription() == null ? "" :
                    project.getDescription()); %>
           </amb:column>
           </amb:table>
        </td>
       </tr>
	<tr>
	  <td>
<%
     } // edit edit
    } // end else 
%><%
    Boolean inAll = (Boolean)request.getAttribute("future");
    if (inAll.booleanValue() == true && !perms.getPermissionFor(Permission.GET_ALL_PROJECTS))
    {
        out.println("<tr><td>&nbsp;</td></tr><tr><td colspan=3>");
        if (vendor)
            out.println(bundle.getString("jsmsg_project_disabled_vendor") + "</td></tr>");
        else
            out.println(bundle.getString("jsmsg_project_disabled") + "</td></tr>");
    } else {
%>

<%  // if new user is in Welocalize, company selection will be displayed.
    if("Transware".equals(companyName)) {  %>
    <tr><td colspan="3"><br></td></tr>
    <tr>
      <td colspan="3">
      <%= bundle.getString("lb_project_companies")%><SPAN CLASS="asterisk">*</SPAN>:
      <select name="companies" onchange="javascript: setProjects();">
        <option value="-1"><%= bundle.getString("lb_choose")%></option>
        <% for(int i = 0; i < companies.length; i ++)
           {
             out.println("<option value=\"" + companies[i] + "\">" + companies[i] + "</option>");
           }
        %>
      </select>
      </td>
    </tr>
    <tr><td colspan="3"><br></td></tr>
    <tr>
      <td class="standardText"><%=bundle.getString("lb_available")%>:</td>
      <td>&nbsp;</td>
      <td class="standardText"><%=bundle.getString("lb_added")%>:</td>
    </tr>
    <tr>
      <td width="20%">
	<select name="from" multiple class="standardText" size=15 style="width:200px" onchange="changeSelectWidth(this)">

	</select>
      </td>
      <td align="center">
	<table class="standardText">
	  <tr>
	    <td>
	      <input type="button" name="addButton" value=" >> "
	      onclick="addProject()"><br>
	    </td>
	  </tr>
	  <tr><td>&nbsp;</td></tr>
	  <tr>
	    <td>
	      <input type="button" name="removedButton" value=" << "
	      onclick="removeProject()">
	    </td>
	  </tr>
	</table>
      </td>
      <td>
	<select name="to" multiple class="standardText" size=15 style="width:200px" onchange="changeSelectWidth(this)" >

	</select>
      </td>
    </tr>
  <% } else {%>
    <tr>
      <td class="standardText"><%=bundle.getString("lb_available")%>:</td>
      <td>&nbsp;</td>
      <td class="standardText"><%=bundle.getString("lb_added")%>:</td>
    </tr>
    <tr>
      <td width="20%">
	<select name="from" multiple class="standardText" size=15 style="width:200px" onchange="changeSelectWidth(this)">
<%
	  if (availableProjects != null)
	  {
	    for (int i = 0; i < availableProjects.size(); i++)
	    {
			Project project = (Project)availableProjects.get(i);
			//for GBS-1995,by fan
			//don't display the element in the left list ,if the the element is existed in the right list.

			boolean isExist = false;  //if the project is existed in the right list, return true.
			for (int j = 0; j < addedProjects.size(); j++)
			{
				Project addedProject = (Project)addedProjects.get(j);
				if(addedProject.getName().equals(project.getName())) isExist = true;
			}
			if(!isExist)
			{
		%>
				<option value="<%=project.getId()%>"><%=project.getName()%></option>
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
	      onclick="addProject()"><br>
	    </td>
	  </tr>
	  <tr><td>&nbsp;</td></tr>
	  <tr>
	    <td>
	      <input type="button" name="removedButton" value=" << "
	      onclick="removeProject()">
	    </td>
	  </tr>
	</table>
      </td>
      <td>
	<select name="to" multiple class="standardText" size=15 style="width:200px" onchange="changeSelectWidth(this)">
<%
        if (addedProjects != null && addedProjects.size() != 0)
	{
	  for (int i = 0; i < addedProjects.size(); i++)
	  {
	    Project project = (Project)addedProjects.get(i);

%>
	  <option value="<%=project.getId()%>"><%=project.getName()%></option>
<%
	  }
	}
%>
	</select>
      </td>
    </tr>

  <% } %>
<% } %>
    <tr>
      <td colspan="3" style="padding-top:10px">
	<input type="button" name="<%=cancelButton %>" value="<%=cancelButton%>"
	onclick="submitForm('cancel')">
    <% if (!edit) { %>
        <input type="button" name="<%=prevButton %>" value="<%=prevButton %>"
        onclick="submitForm('prev')">
        <input type="button" name="<%=nextButton %>" value="<%=nextButton %>"
        onclick="submitForm('next')">
    <% } else { %>
        <input type="button" name="<%=doneButton %>" value="<%=doneButton %>"
        onclick="submitForm('saveUsers')">
    <% } %>
      </td>
    </tr>
</table>
</form>
</BODY>
</HTML>
