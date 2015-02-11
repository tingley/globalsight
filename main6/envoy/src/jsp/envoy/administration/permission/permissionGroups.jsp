<%@ taglib uri="/WEB-INF/tlds/globalsight.tld" prefix="amb" %>
<%@ page contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="com.globalsight.everest.webapp.javabean.NavigationBean,
            com.globalsight.everest.permission.Permission,
            com.globalsight.everest.workflow.Activity,
            com.globalsight.everest.webapp.pagehandler.PageHandler, 
            com.globalsight.everest.servlet.util.SessionManager,
            com.globalsight.everest.webapp.pagehandler.administration.permission.PermissionGroupsHandler, 
            com.globalsight.everest.webapp.pagehandler.administration.permission.PermissionGroupComparator,
              com.globalsight.everest.company.CompanyWrapper,
            java.util.*"
    session="true"
%>
<jsp:useBean id="new1" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="edit" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="remove" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="self" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="details" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="permGroups" scope="request"
 class="java.util.ArrayList" />
<% 
    ResourceBundle bundle = PageHandler.getBundle(session);
	String helperText = bundle.getString("helper_text_permissionggroup_list");
    SessionManager sessionMgr =
      (SessionManager)session.getAttribute(WebAppConstants.SESSION_MANAGER);
 
    String confirmRemove = bundle.getString("msg_confirm_permission_removal");
    String newURL = new1.getPageURL() + "&action=" + PermissionGroupsHandler.CREATE;
    String selfURL = self.getPageURL();
    String editURL = edit.getPageURL() + "&action=" + PermissionGroupsHandler.EDIT;
    String removeURL = remove.getPageURL() + "&action=" + PermissionGroupsHandler.REMOVE;
    String detailsURL = details.getPageURL() + "&action=" + PermissionGroupsHandler.DETAILS;
    String title = bundle.getString("lb_permission_groups");
    String error = (String)request.getAttribute("errorMsg");
    String pCompanyFilter= (String) sessionMgr.getAttribute("pCompanyFilter");
    pCompanyFilter = pCompanyFilter == null ? "" : pCompanyFilter;
    String pNameFilter= (String) sessionMgr.getAttribute("pNameFilter");
    pNameFilter = pNameFilter == null ? "" : pNameFilter;
    if (error == null)
    {
        error = "";
    }
    
    boolean isSuperAdmin = ((Boolean) session.getAttribute(WebAppConstants.IS_SUPER_ADMIN)).booleanValue();
    
    long [] superPermGroupIds = (long []) request.getAttribute(PermissionGroupsHandler.SUPER_PGROUP_IDS);
%>
<HTML>
<HEAD>
<TITLE><%=title%></TITLE>
<SCRIPT SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<SCRIPT SRC="/globalsight/includes/radioButtons.js"></SCRIPT>
<script type="text/javascript" src="/globalsight/jquery/jquery-1.6.4.min.js"></script>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>
<SCRIPT>
var needWarning = false;
var objectName = "";
var guideNode = "permissionGroups";
var helpFile = "<%=bundle.getString("help_permission_groups")%>";
$(
		function(){
			$("#permForm").keydown(function(e){
				if(e.keyCode==13)
				
				{
					
					submitForm("self");
				
				}
				
				});
			
			
		}		
	)



function buttonManagement()
{
	var count = $("input[name='radioBtn']:checked").length;
	if (count > 0) {
		$("#removeBtn").attr("disabled", false);
	    if (count == 1) {
	    	$("#detailsBtn").attr("disabled", false);
		} else {
	    	$("#detailsBtn").attr("disabled", true);
		}
	} else {
        $("#removeBtn").attr("disabled", true);
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
function modifyuser(name){
	
	var url = "<%=editURL%>&radioBtn=" + name;
	permForm.action = url;

	permForm.submit();
	
}
function submitForm(button)
{
    if (button == "New")
    {
        permForm.action = "<%=newURL%>";
    }
	 if (button == "self")
    {
        permForm.action = "<%=selfURL%>";
        
    }
    else 
    {
        if (button == "Remove")
        {
            if (!confirm('<%=confirmRemove%>')) return false;
            var rv="";
			$(":checkbox:checked").each(
				function (i){
					rv+=$(this).val()+" ";
				}
			)
			$(":checkbox:checked").each(
				function (i){
					$(this).val(rv);
				}
			)
            permForm.action = "<%=removeURL%>";
        }
        else if (button == "Details")
        {
            permForm.action = "<%=detailsURL%>";
        }
    }
    permForm.submit();
    return;
}

function enableButtons()
{
	value = getRadioValue(permForm.radioBtn);
    <%
      long id;
      for (int i = 0; i < superPermGroupIds.length; i++)
      {
        id = superPermGroupIds[i];
    %>
        if (value == <%=id%>)
        {
          if (permForm.removeBtn)
            permForm.removeBtn.disabled = true;
          if (permForm.editBtn)
            permForm.editBtn.disabled = false;
          if (permForm.detailsBtn)
            permForm.detailsBtn.disabled = false;
          
          return;
        }
    <%
      }
    %>
    if (permForm.removeBtn)
      permForm.removeBtn.disabled = false;
    if (permForm.editBtn)
      permForm.editBtn.disabled = false;
    if (permForm.detailsBtn)
      permForm.detailsBtn.disabled = false;
}

function filterItems(e)
{
    e = e ? e : window.event;
    var keyCode = e.which ? e.which : e.keyCode;
    if (keyCode == 13)
    {
    	permForm.action = "<%=selfURL%>";
    	permForm.submit();
    }
}
</SCRIPT>
</HEAD>

<BODY LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0" MARGINWIDTH="0"
  MARGINHEIGHT="0" onload="loadGuides()">
<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>
<DIV ID="contentLayer" STYLE=" POSITION: ABSOLUTE; Z-INDEX: 9; TOP: 108px; LEFT: 20px; RIGHT: 20px;">
<amb:header title="<%=title%>" helperText="<%=helperText%>" />

<form name="permForm" id="permForm" method="post">
<div style='color:red'><%=error%></div> 
<table cellpadding=0 cellspacing=0 border=0 class="standardText" width="100%" align="left" style="min-width:1024px;">
  <tr valign="top">
    <td align="right">
      <amb:tableNav bean="permGroups"
         key="<%=PermissionGroupsHandler.PERM_GROUP_KEY%>"
         pageUrl="self" />
    </td>
  </tr>
  <tr>
    <td>
      <amb:table bean="permGroups" id="permissionGroup"
       key="<%=PermissionGroupsHandler.PERM_GROUP_KEY%>"
       dataClass="com.globalsight.everest.permission.PermissionGroup" hasFilter="true"
       pageUrl="self" emptyTableMsg="msg_no_permission_groups" >
       <amb:column label="checkbox" width="2%">
     <input type="checkbox" name="radioBtn" id="<%=permissionGroup.getId()%>" value="<%=permissionGroup.getId()%>" onclick="buttonManagement()">
      </amb:column>
      
      
      <amb:column label="lb_name" sortBy="<%=PermissionGroupComparator.NAME%>" filter="pNameFilter" filterValue="<%=pNameFilter%>" width="20%">
    
       <amb:permission name="<%=Permission.PERMGROUPS_EDIT%>" ><a href='javascript:void(0)' title='Edit permissionGroup' onclick="modifyuser('<%= permissionGroup.getId() %>')">  </amb:permission>
        <%= permissionGroup.getName() %> 
         <amb:permission name="<%=Permission.PERMGROUPS_EDIT%>" ></a></amb:permission>
      </amb:column>
      <amb:column label="lb_description" sortBy="<%=PermissionGroupComparator.DESC%>" width="60%">
      <% out.print(permissionGroup.getDescription() == null ?
       "" : permissionGroup.getDescription()); %>
      </amb:column>
      <% if (isSuperAdmin) { %>
      <amb:column label="lb_company_name" sortBy="<%=PermissionGroupComparator.ASC_COMPANY%>"   filter="pCompanyFilter" filterValue="<%=pCompanyFilter%>" width="120">
        <%=CompanyWrapper.getCompanyNameById(permissionGroup.getCompanyId())%>
      </amb:column>
      <% } %>
      </amb:table>
    </td>
  </tr>
  
   </TR>
    <td>
      <amb:tableNav bean="permGroups"  key="<%=PermissionGroupsHandler.PERM_GROUP_KEY%>" pageUrl="self" scope="10,20,50,All" showTotalCount="false"/>
    </td>
  <TR>
</DIV>
<TR><TD>&nbsp;</TD></TR>
  
  <tr>
    <td style="padding-top:5px" align="left">
    <amb:permission name="<%=Permission.PERMGROUPS_DETAILS%>" >
      <INPUT TYPE="BUTTON" VALUE="<%=bundle.getString("lb_details")%>"
       name="detailsBtn" id="detailsBtn" disabled onclick="submitForm('Details');">
    </amb:permission>
    <amb:permission name="<%=Permission.PERMGROUPS_REMOVE%>" >
      <INPUT TYPE="BUTTON" VALUE="<%=bundle.getString("lb_remove")%>"
       name="removeBtn" id="removeBtn" disabled onclick="submitForm('Remove');">
    </amb:permission>   
    <amb:permission name="<%=Permission.PERMGROUPS_NEW%>" >
      <INPUT TYPE="BUTTON" VALUE="<%=bundle.getString("lb_new")%>..."
       onclick="submitForm('New');">
    </amb:permission>
    </td>
  </tr>
</table>
</form>
</div>
</BODY>
</HTML>

