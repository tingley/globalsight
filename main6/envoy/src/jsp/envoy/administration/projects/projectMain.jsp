<%@ taglib uri="/WEB-INF/tlds/globalsight.tld" prefix="amb" %>
<%@ page contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="java.util.*,com.globalsight.everest.webapp.javabean.NavigationBean,
         com.globalsight.util.resourcebundle.ResourceBundleConstants,
         com.globalsight.util.resourcebundle.SystemResourceBundle,
         com.globalsight.everest.permission.Permission,
         com.globalsight.everest.permission.PermissionSet,
         com.globalsight.everest.util.comparator.ProjectComparator,
         com.globalsight.everest.webapp.pagehandler.PageHandler,
         com.globalsight.everest.webapp.pagehandler.administration.projects.ProjectMainHandler,
         com.globalsight.everest.projecthandler.ProjectInfo,
         com.globalsight.everest.foundation.User,
         com.globalsight.everest.servlet.util.SessionManager,
         com.globalsight.everest.company.CompanyWrapper,
         com.globalsight.everest.webapp.WebAppConstants,
         com.globalsight.util.edit.EditUtil,
         java.text.MessageFormat,
         java.util.Locale,
         java.util.ResourceBundle,
         java.util.List,
         java.util.ArrayList" 
         session="true" %>
<jsp:useBean id="skinbean" scope="application"
 class="com.globalsight.everest.webapp.javabean.SkinBean" />
<jsp:useBean id="self" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="new1" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="modify" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="_import" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="_export" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="remove" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="projects" scope="request" class="java.util.ArrayList" />

<% 
    ResourceBundle bundle = PageHandler.getBundle(session);
    SessionManager sessionManager =
      (SessionManager)session.getAttribute(WebAppConstants.SESSION_MANAGER);
    PermissionSet perms=(PermissionSet)session.getAttribute(WebAppConstants.PERMISSIONS);

    String selfUrl = self.getPageURL();
    String newUrl = new1.getPageURL()+"&action=new";
    String editUrl = modify.getPageURL()+"&action=edit";
    String importSchedUrl = _import.getPageURL();
    String exportUrl = _export.getPageURL();
    String removeUrl = remove.getPageURL()+"&action=remove";

    String subTitle = "";
    String title= bundle.getString("lb_projects");
    String moduleLink="/globalsight/ControlServlet?activityName=";

    // Button names
    String newButton = bundle.getString("lb_new1");
    String editButton = bundle.getString("lb_edit1");
    String importButton = bundle.getString("lb_import_schedules");
    String exportButton = bundle.getString("lb_export_schedules");
    String removeButton = bundle.getString("lb_remove");

    // user info
    User user = (User)sessionManager.getAttribute(WebAppConstants.USER);
    String pmName = user.getUserName();
    String pNameFilter = (String) sessionManager.getAttribute("pNameFilter");
    pNameFilter = pNameFilter == null ? "" : pNameFilter;
    String cNameFilter = (String) sessionManager.getAttribute("cNameFilter");
    cNameFilter = cNameFilter == null ? "" : cNameFilter;
    boolean isSuperAdmin = ((Boolean) session.getAttribute(WebAppConstants.IS_SUPER_ADMIN)).booleanValue();
    
    String error = (String) sessionManager.getAttribute(WebAppConstants.PROJECT_ERROR);
	
%>
<HTML XMLNS:gs>
<!-- This JSP is envoy/administration/projects/projectMain.jsp -->
<HEAD>
<META HTTP-EQUIV="content-type" CONTENT="text/html;charset=UTF-8">
<TITLE><%= title %></TITLE>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<script type="text/javascript" src="/globalsight/jquery/jquery-1.6.4.min.js"></script>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>
<SCRIPT LANGUAGE="JavaScript">
    var needWarning = false;
    var objectName = "";
    var guideNode = "projects";
    var helpFile = "<%=bundle.getString("help_projects_main_screen")%>";
    $(
    		function(){
    			$("#ProjectForm").keydown(function(e){
    				if(e.keyCode==13)
    				
    				{
    					submitForm("self");
    				}
    				
    				});
    			
    			
    		}		
    	)

    function handleSelectAll() {
    	var ch = $("#selectAll").attr("checked");
    	if (ch == "checked") {
    		$("[name='radioBtn']").attr("checked", true);
    	} else {
    		$("[name='radioBtn']").attr("checked", false);
    	}
    	buttonManagement();
    }
    function buttonManagement()
    {
    	
    	var count = $("input[name='radioBtn']:checked").length;
    	if (count > 0) {
    		$("#removeBtn").attr("disabled", false);
    		if (count == 1) {
    			 $("#removeBtn").attr("disabled", false);
    			$("#exportBtn").attr("disabled", false);
    			$("#importBtn").attr("disabled", false);
    		} else {
    			 $("#removeBtn").attr("disabled", true);
    			$("#exportBtn").attr("disabled", true);
    			$("#importBtn").attr("disabled", true);
    		}
    	} else {
            $("#removeBtn").attr("disabled", true);
            $("#exportBtn").attr("disabled", true);
            $("#importBtn").attr("disabled", true);
    	}
    }
    function modifyuser(name){
    	
    	var url = "<%=editUrl%>&radioBtn=" + name;
    	ProjectForm.action = url;

    	ProjectForm.submit();
    	
    }
function enableButtons()
{
    if (ProjectForm.editBtn) {
        ProjectForm.editBtn.disabled = false;
    }
    if (ProjectForm.removeBtn) {
		ProjectForm.removeBtn.disabled = false;
    }
    <% if(b_calendaring) { %>
        if (ProjectForm.exportBtn) {
            ProjectForm.exportBtn.disabled = false;
        }
        if (ProjectForm.importBtn) {
            ProjectForm.importBtn.disabled = false;
        }
    <% } %>
}

function submitForm(selectedButton)
{
    var checked = false;
    var selectedRadioBtn = null;
    if (ProjectForm.radioBtn != null)
    {
        // If more than one radio button is displayed, the length attribute of
        // the radio button array will be non-zero, so find which one is checked
        if (ProjectForm.radioBtn.length)
        {
            for (i = 0; !checked && i < ProjectForm.radioBtn.length; i++)
            {
                if (ProjectForm.radioBtn[i].checked == true)
                {
                    checked = true;
                    selectedRadioBtn = ProjectForm.radioBtn[i].value;
                }
             }
        }
        // If only one is displayed, there is no radio button array, so
        // just check if the single radio button is checked
        else
        {
            if (ProjectForm.radioBtn.checked == true)
            {
                checked = true;
                selectedRadioBtn = ProjectForm.radioBtn.value;
            }
        }
    }
    // otherwise do the following
    if (selectedButton == 'New')
    {
        ProjectForm.action = "<%=newUrl%>";
        ProjectForm.submit();
        return;
    }
    if (selectedButton == 'self')
    {
        ProjectForm.action = "<%=selfUrl%>";
        ProjectForm.submit();
        return;
    }
    else if (!checked)
    {
        alert("<%= bundle.getString("jsmsg_select_project") %>");
        return false;
    }

    values = selectedRadioBtn.split(",");
    if (selectedButton == 'Edit')
    {
        if (!<%=perms.getPermissionFor(Permission.PROJECTS_EDIT)%> && values[1] != "<%=pmName%>")
        {
            alert("<%=bundle.getString("jsmsg_cannot_edit_project") %>");
            return;
        }
        ProjectForm.action = "<%=editUrl %>";
    }
    else if (selectedButton == 'Import')
    {       
        if (!<%=perms.getPermissionFor(Permission.PROJECTS_IMPORT)%> && values[1] != "<%=pmName%>")
        {
            alert("<%=bundle.getString("jsmsg_cannot_import_project") %>");
            return;
        }
        ProjectForm.action = "<%=importSchedUrl%>" +
        "&<%=WebAppConstants.TM_ACTION%>=<%=WebAppConstants.TM_ACTION_IMPORT%>";
    }
    else if (selectedButton == 'Export')
    {
        if (!<%=perms.getPermissionFor(Permission.PROJECTS_EXPORT)%> && values[1] != "<%=pmName%>")
        {
            alert("<%=bundle.getString("jsmsg_cannot_export_project") %>");
            return;
        }
        ProjectForm.action = "<%=exportUrl%>" +
        "&<%=WebAppConstants.TM_ACTION%>=<%=WebAppConstants.TM_ACTION_EXPORT%>";
    }
    else if (selectedButton == 'Remove')
    {
        if (!<%=perms.getPermissionFor(Permission.PROJECTS_REMOVE)%> && values[1] != "<%=pmName%>")
        {
        	alert("<%=bundle.getString("jsmsg_cannot_remove_project") %>");
        	return;
        }
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
        ProjectForm.action = "<%=removeUrl%>";
    }

    ProjectForm.submit();
}

function filterItems(e)
{
    e = e ? e : window.event;
    var keyCode = e.which ? e.which : e.keyCode;
    if (keyCode == 13)
    {
    	ProjectForm.action = "<%=selfUrl%>";
    	ProjectForm.submit();
    }
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

<% if (error != null) {
	sessionManager.removeElement(WebAppConstants.PROJECT_ERROR);
%>
    <amb:header title="<%=title%>" helperText="<%=error%>" />
<%   } else {  %>
    <amb:header title="<%=title%>"/>
<% }  %>
    
    <form name="ProjectForm" id="ProjectForm" method="post">
    <TABLE cellpadding=0 cellspacing=0 border=0 class="standardText" width="100%" align="left" style="min-width:1024px;">
      <TR VALIGN="TOP">
        <TD ALIGN="RIGHT">
          <amb:tableNav bean="projects" key="<%=ProjectMainHandler.PROJECT_KEY%>"
                     pageUrl="self" />
        </td>
      </tr>
      <tr>
        <td>
          <amb:table bean="projects" id="proj" key="<%=ProjectMainHandler.PROJECT_KEY%>" hasFilter="true"
           dataClass="com.globalsight.everest.projecthandler.ProjectInfo" pageUrl="self"
           emptyTableMsg="msg_no_projects" >
            <amb:column label="checkbox" width="2%">
            <input type="checkbox" name="radioBtn" id="<%=user.getUserId()%>" value="<%=proj.getProjectId()%>" onclick="buttonManagement()">
            </amb:column>
            <amb:column label="lb_name" sortBy="<%=ProjectComparator.PROJECTNAME%>" filter="pNameFilter" filterValue="<%=pNameFilter%>" width="20%">
             <amb:permission name="<%=Permission.PROJECTS_EDIT%>" > <a href='javascript:void(0)' title='Edit project' onclick="modifyuser('<%= proj.getProjectId() %>')"> </amb:permission>
               <%=proj.getName()%> 
              <amb:permission name="<%=Permission.PROJECTS_EDIT%>" > </a> </amb:permission>
            </amb:column>
            <amb:column label="lb_description" sortBy="<%=ProjectComparator.DESCRIPTION%>" >
              <%=proj.getDescription()%>
            </amb:column>
            <amb:column label="lb_project_manager"
             sortBy="<%=ProjectComparator.PROJECTMANAGER%>" width="10%">
              <%=proj.getProjectManagerName()%>
            </amb:column>
            <amb:column label="lb_termbase" sortBy="<%=ProjectComparator.TERMBASE%>" width="10%">
              <% out.print(proj.getTermbaseName() == null ? "" : proj.getTermbaseName()); %>
            </amb:column>
            <amb:column label="lb_pmcost_tag" sortBy="<%=ProjectComparator.PMCOST%>"  width="70px">
              <% out.print(proj.getPMCost()*100); %>
            </amb:column>
            <amb:column label="lb_project_reviewOnlyAutoAccept" sortBy="<%=ProjectComparator.BOOLEANACCEPT%>"  width="150px">
              <% out.print(proj.isReviewOnlyAutoAccept() == true ? "yes" : "no"); %>
            </amb:column>
            <amb:column label="lb_project_reviewOnlyAutoSend" sortBy="<%=ProjectComparator.BOOLEANSEND%>"  width="150px">
              <% out.print(proj.isReviewOnlyAutoSend() == true ? "yes" : "no"); %>
            </amb:column>
            <amb:column label="lb_project_AutoAcceptPMTask" sortBy="<%=ProjectComparator.BOOLEANTASK%>"  width="150px">
               <% out.print(proj.isAutoAcceptPMTask() == true ? "yes" : "no"); %>
            </amb:column>
            <amb:column label="lb_project_checkUnTransSeg" sortBy="<%=ProjectComparator.BOOLEAN_CHECKUNTRANSLATEDSEGMENTS%>"  width="150px">
               <% out.print(proj.isCheckUnTranslatedSegments() == true ? "yes" : "no"); %>
            </amb:column>
            <% if (perms.getPermissionFor(Permission.ATTRIBUTE_GROUP_VIEW)) { %>
            <amb:column label="lb_attribute_group" sortBy="<%=ProjectComparator.ATTRIBUTESETNAME%>"  width="100px">
              <%=proj.getAttributeSetName()%>
            </amb:column>
            <% } %>
            <% if (isSuperAdmin) { %>
            <amb:column label="lb_company_name" sortBy="<%=ProjectComparator.ASC_COMPANY%>"  filter="cNameFilter" filterValue="<%=cNameFilter%>" width="100px">
              <%=CompanyWrapper.getCompanyNameById(proj.getCompanyId())%>
            </amb:column>
            <% } %>
          </amb:table>
</TD>
</TR>

</TR>
    <td>
      <amb:tableNav bean="projects"  key="<%=ProjectMainHandler.PROJECT_KEY%>" pageUrl="self" scope="10,20,50,All" showTotalCount="false"/>
    </td>
  <TR>
</DIV>
<TR><TD>&nbsp;</TD></TR>

<TR>
<TD>
<DIV ID="DownloadButtonLayer" ALIGN="left" STYLE="visibility: visible">
    <P>
    <%if(b_calendaring) { %>
<amb:permission name="<%=Permission.PROJECTS_IMPORT%>" >
    <INPUT TYPE="BUTTON" VALUE="<%=importButton%>" onClick="submitForm('Import');"
        name="importBtn" id="importBtn" disabled />
</amb:permission>
<amb:permission name="<%=Permission.PROJECTS_EXPORT%>" >
    <INPUT TYPE="BUTTON" VALUE="<%=exportButton%>" onClick="submitForm('Export');"
        name="exportBtn" id="exportBtn" disabled />
</amb:permission>
    <% } %>

<amb:permission name="<%=Permission.PROJECTS_NEW%>" >
    <INPUT TYPE="BUTTON" VALUE="<%=newButton%>" onClick="submitForm('New');" />
</amb:permission>
<amb:permission name="<%=Permission.PROJECTS_REMOVE%>" >
    <INPUT TYPE="BUTTON" VALUE="<%=removeButton%>" onClick="submitForm('Remove');"
    	name="removeBtn" id="removeBtn" disabled />
</amb:permission>
</DIV>
</TD>
</TR>
</TABLE>
</FORM>
</BODY>
</HTML>
