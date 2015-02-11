<%@ taglib uri="/WEB-INF/tlds/globalsight.tld" prefix="amb" %>
<%@ page contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="java.util.*,com.globalsight.everest.webapp.javabean.NavigationBean,
    com.globalsight.everest.servlet.util.SessionManager,
     com.globalsight.everest.permission.PermissionGroup, 
     com.globalsight.everest.util.comparator.StringComparator,
     com.globalsight.everest.util.comparator.GlobalSightLocaleComparator,
     com.globalsight.util.GlobalSightLocale,
         com.globalsight.util.resourcebundle.ResourceBundleConstants,
         com.globalsight.util.resourcebundle.SystemResourceBundle,
         com.globalsight.everest.securitymgr.FieldSecurity,
         com.globalsight.everest.webapp.WebAppConstants,
         com.globalsight.everest.webapp.pagehandler.PageHandler,
         com.globalsight.everest.webapp.pagehandler.administration.users.UserConstants,
         com.globalsight.everest.webapp.pagehandler.administration.users.UserComparator,
         com.globalsight.everest.webapp.pagehandler.administration.users.UserSearchParams,
         com.globalsight.everest.permission.Permission,
         com.globalsight.everest.permission.PermissionSet,         
         com.globalsight.everest.foundation.User,
         com.globalsight.everest.company.CompanyWrapper,
         com.globalsight.util.edit.EditUtil,
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
<jsp:useBean id="export" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="imports" scope="request"
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
    String impUrl = imports.getPageURL() + "&action=importUser";
    String exportUrl = export.getPageURL() + "&action=exportUser";

    String title= bundle.getString("lb_users");
    String helperText = bundle.getString("helper_text_user_list");

    String confidential = "[" + bundle.getString("lb_confidential") + "]";

    // Button names
    String newButton = bundle.getString("lb_new");
    String editButton = bundle.getString("lb_edit");
    String detailsButton = bundle.getString("lb_details");
    String removeButton = bundle.getString("lb_remove");
    String searchButton = bundle.getString("lb_search");
    String exportButton = bundle.getString("lb_export");
	String importButton = bundle.getString("lb_import1");
    
    ArrayList securities = (ArrayList)request.getAttribute("securities");
    String deps = (String)request.getAttribute(UserConstants.DEPENDENCIES);
    UserSearchParams fromSearch = (UserSearchParams)sessionMgr.getAttribute("fromSearch");
    String emptyMsg  = "msg_no_users";
    if (fromSearch != null)
         emptyMsg = "msg_no_users_search";
    String preReqData = (String)request.getAttribute("preReqData");

    // UserID of the user who is logged in
    boolean isSuperAdmin = ((Boolean) session.getAttribute(WebAppConstants.IS_SUPER_ADMIN)).booleanValue();
    
    String error = (String)request.getAttribute("errorMsg");
    if (error == null)
    {
        error = "";
    }

	String uNameFilter = (String) sessionMgr.getAttribute("uNameFilter");
	uNameFilter = uNameFilter == null ? "" : uNameFilter;
	String ufNameFilter = (String) sessionMgr.getAttribute("ufNameFilter");
	ufNameFilter = ufNameFilter == null ? "" : ufNameFilter;
	String ulNameFilter = (String) sessionMgr.getAttribute("ulNameFilter");
	ulNameFilter = ulNameFilter == null ? "" : ulNameFilter;
	String uEmailFilter = (String) sessionMgr.getAttribute("uEmailFilter");
	uEmailFilter = uEmailFilter == null ? "" : uEmailFilter;
	String uCompanyFilter = (String) sessionMgr.getAttribute("uCompanyFilter");
	uCompanyFilter = uCompanyFilter == null ? "" : uCompanyFilter;
 	String uProjectFilter = (String) sessionMgr.getAttribute("uProjectFilter");
 	uProjectFilter = uProjectFilter == null ? "" : uProjectFilter;
    String uPermissionFilter = (String) sessionMgr.getAttribute("uPermissionFilter");
    uPermissionFilter = uPermissionFilter == null ? "" : uPermissionFilter;
	
	 // Labels, etc
    String lbsearch = bundle.getString("lb_search");
    String lbcancel = bundle.getString("lb_cancel");
    
    Locale uiLocale = (Locale)session.getAttribute(WebAppConstants.UILOCALE);
    // Data
    List srcLocales = (List)request.getAttribute("srcLocales");
    List targLocales = (List)request.getAttribute("targLocales");
    String[] levels = (String[])request.getAttribute("levels");
%>
<html>
<head>
<!-- This JSP is: envoy/administration/users/userMain.jsp -->
<meta http-equiv="content-type" content="text/html;charset=UTF-8">
<title><%= title %></title>
<script language="JavaScript" SRC="/globalsight/includes/setStyleSheet.js"></script>
<script language="JavaScript" SRC="/globalsight/includes/radioButtons.js"></script>
<script type="text/javascript" src="/globalsight/jquery/jquery-1.6.4.min.js"></script>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>
<script language="JavaScript">
var needWarning = false;
var objectName = "";
var guideNode = "users";
var helpFile = "<%=bundle.getString("help_users_main_screen")%>";

<%  ArrayList userlist = (ArrayList) request.getAttribute("users");
%>
$(
	function(){
		$("#userForm").keydown(function(e){

			if(e.keyCode==13)
			{
				submitForm("Search");
			}
			});
	}
)
function buttonManagement()
{
	var count = $("input[name='radioBtn']:checked").length;
	if (count > 0) {
		$("#removeBtn").attr("disabled", false);
		$("#exportBtn").attr("disabled", false);
	    if (count == 1) {
	    	$("#editBtn").attr("disabled", false);
	    	$("#detailsBtn").attr("disabled", false);
		} else {
			$("#editBtn").attr("disabled", true);
	    	$("#detailsBtn").attr("disabled", true);
		}
	} else {
        $("#editBtn").attr("disabled", true);
        $("#removeBtn").attr("disabled", true);
	    $("#exportBtn").attr("disabled", true);
		$("#detailsBtn").attr("disabled", true);
	}
}

function handleSelectAll() {
	var ch = $("#selectAll").attr("checked");
	if (ch == "checked") {
		$("[name='radioBtn']").attr("checked", true);
	} else {
		$("[name='radioBtn']").attr("checked", false);
	}
	buttonManagement();
}

function modifyuser(name)
{
    var url = "<%=editUrl%>&radioBtn=" + name;
	userForm.action = url;
    userForm.submit();
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
    	var url ="<%=searchUrl%>";
		userForm.action=url;
	
        userForm.submit();
        return;
    }
    if (selectedButton == "AdvSearch")
    {
		
    	 $("#dialogAdSearch").dialog({width: 600, height: 450});
		
    	
        return;
    }
    if(selectedButton=="Cancel"){
    	$("#dialogAdSearch").dialog("close");
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
    else if (selectedButton == "Export")
    {
    	userForm.action = "<%=exportUrl%>";
    }
    else if (selectedButton == "Import")
    {
    	userForm.action = "<%=impUrl%>";
    }

    userForm.submit();
}

function filterItems(e)
{
    e = e ? e : window.event;
    var keyCode = e.which ? e.which : e.keyCode;
    if (keyCode == 13)
    {
    	userForm.action = "<%=selfUrl%>";
    	userForm.submit();
    }
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

<form name="userForm" id="userForm" method="post" >
      

<p>
<div style='color:red'><%=error%></div> 
    <table cellpadding=0 cellspacing=0 border=0 class="standardText" width="100%" align="left" style="min-width:1024px;">
        <tr valign="top">
          <td align="right">
            <amb:tableNav bean="users" key="<%=UserConstants.USER_KEY%>"
                 pageUrl="self" />
          </td>
        <tr>
          <td>
  <% int i = 0; %>
  <amb:table bean="users" id="user" key="<%=UserConstants.USER_KEY%>"
         dataClass="com.globalsight.everest.foundation.User" pageUrl="self" hasFilter="true"
         emptyTableMsg="<%=emptyMsg%>" >
    <amb:column label="checkbox" width="20px">
    	<input type="checkbox" name="radioBtn" id="<%=user.getUserId()%>" value="<%=user.getUserId()%>" onclick="buttonManagement()">
    </amb:column>

    <amb:column label="lb_user_name" sortBy="<%=UserComparator.USERNAME%>" filter="uNameFilter" filterValue="<%=uNameFilter%>" width="120">
    <%  if (perms.getPermissionFor(Permission.USERS_EDIT)) { %>
        <a href="javascript:void(0)" title="Edit user" onclick="modifyuser('<%=user.getUserId()%>');"><%=user.getUserName()%></a>
    <%  } else { %>
        <%=user.getUserName() %>
    <%  } %>
    </amb:column>
    <amb:column label="lb_first_name" sortBy="<%=UserComparator.FIRSTNAME%>" filter="ufNameFilter" filterValue="<%=ufNameFilter%>" width="120">
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
    <amb:column label="lb_last_name" sortBy="<%=UserComparator.LASTNAME%>" filter="ulNameFilter" filterValue="<%=ulNameFilter%>" width="120">
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
    
    <amb:column label="lb_project" sortBy="<%=UserComparator.PROJECT%>" filter="uProjectFilter" filterValue="<%=uProjectFilter%>" width="120">
    <%
        String name = user.getProjectNames();

        out.print(name);
    %>
    </amb:column>
  	<amb:column label="lb_email" sortBy="<%=UserComparator.EMAIL%>" filter="uEmailFilter" filterValue="<%=uEmailFilter%>"   width="120">
    <%
        String name = user.getEmail();

        out.print(name== null ? "" : name);
    %>
    </amb:column>
		<amb:column label="lb_permission_group" sortBy="<%=UserComparator.PERMISSION%>"  filter="uPermissionFilter" filterValue="<%=uPermissionFilter%>"  width="120">
    <%
        String name = user.getPermissiongNames();

        out.print(name);
    %>
    </amb:column>


    <% if (isSuperAdmin) { %>
    <amb:column label="lb_company_name" sortBy="<%=UserComparator.ASC_COMPANY%>" filter="uCompanyFilter" filterValue="<%=uCompanyFilter%>" width="120">
      <%=user.getCompanyName()%>
    </amb:column>
    <amb:column label="lb_active" width="20px">
    <%
    	if (user.isActive())
    	{
            out.println("<img src=/globalsight/images/checkmark.gif height=9 width=13 hspace=10 vspace=3>");
    	}
    %>
    </amb:column>
    <% } %>
  </amb:table>
</TD>
</TR>
  </TR>
    <td>
      <amb:tableNav bean="users" key="<%=UserConstants.USER_KEY%>" pageUrl="self" scope="10,20,50,All" showTotalCount="false"/>
    </td>
  <TR>
</DIV>
<TR><TD>&nbsp;</TD></TR>

<TR>
<TD align="left">
    <P>
    <amb:permission name="<%=Permission.USERS_IMPORT%>" >
    <INPUT TYPE="BUTTON" VALUE="<%=importButton%>" onClick="submitForm('Import');"
        id="importBtn">
    </amb:permission>
    <amb:permission name="<%=Permission.USERS_EXPORT%>" >
    <INPUT TYPE="BUTTON" VALUE="<%=exportButton%>" onClick="submitForm('Export');"
        id="exportBtn" disabled>
    </amb:permission>
    <amb:permission name="<%=Permission.USERS_REMOVE%>" >
    <INPUT TYPE="BUTTON" VALUE="<%=removeButton%>" onClick="submitForm('Remove');"
        id="removeBtn" disabled>
    </amb:permission>
    <INPUT TYPE="BUTTON" VALUE="<%=detailsButton%>..." onClick="submitForm('Details');"
        id="detailsBtn" disabled>
    <amb:permission name="<%=Permission.USERS_NEW%>" >
    <INPUT TYPE="BUTTON" VALUE="<%=newButton%>..." onClick="submitForm('New');">
    </amb:permission>

</TD>
</TR>
</TABLE>
</form>
</body>
</html>
