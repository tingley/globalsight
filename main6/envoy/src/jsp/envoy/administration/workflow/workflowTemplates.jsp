<%@ taglib uri="/WEB-INF/tlds/globalsight.tld" prefix="amb" %>
<%@ page 
    contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/activityError.jsp"
    import="com.globalsight.everest.projecthandler.WorkflowTemplateInfo, 
            com.globalsight.everest.webapp.pagehandler.administration.workflow.WorkflowTemplateConstants,
            com.globalsight.everest.util.comparator.WorkflowTemplateInfoComparator,
            com.globalsight.everest.servlet.util.SessionManager,                 
            com.globalsight.everest.permission.Permission,                 
            com.globalsight.everest.webapp.WebAppConstants,
            com.globalsight.everest.webapp.javabean.NavigationBean,
            com.globalsight.everest.webapp.pagehandler.PageHandler,
            com.globalsight.util.edit.EditUtil,
            com.globalsight.util.resourcebundle.ResourceBundleConstants,
            com.globalsight.util.resourcebundle.SystemResourceBundle,
            com.globalsight.everest.webapp.webnavigation.LinkHelper,
            com.globalsight.everest.servlet.util.ServerProxy,
            com.globalsight.everest.servlet.EnvoyServletException,
            com.globalsight.everest.util.system.SystemConfigParamNames,
            com.globalsight.everest.util.system.SystemConfiguration,
            com.globalsight.everest.foundation.SearchCriteriaParameters,
            com.globalsight.everest.projecthandler.WfTemplateSearchParameters,
            com.globalsight.util.GeneralException,          
            com.globalsight.everest.company.CompanyWrapper,
            com.globalsight.everest.webapp.pagehandler.administration.users.UserUtil,
            java.text.MessageFormat,
	    com.globalsight.util.GlobalSightLocale,
            com.globalsight.everest.projecthandler.ProjectInfo,
            java.util.*"
    session="true" 
%>
<jsp:useBean id="self" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="modify" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="duplicate" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="_import" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="templates" scope="request" class="java.util.ArrayList" />
<% 
    ResourceBundle bundle = PageHandler.getBundle(session);
    Locale uiLocale = (Locale)session.getAttribute(WebAppConstants.UILOCALE);
    SessionManager sessionManager =
      (SessionManager)session.getAttribute(WebAppConstants.SESSION_MANAGER);    
    String title= bundle.getString("lb_workflows");
    String helperText = bundle.getString("msg_wf_template_info");
                                 
    String confirmRemove = bundle.getString("msg_confirm_wf_removal");
    //Button names
    String newButton = bundle.getString("lb_new");
    String editButton = bundle.getString("lb_edit");
    String removeButton = bundle.getString("lb_remove");
    String dupButton = bundle.getString("lb_duplicate");
    String impButton = bundle.getString("lb_import");
    String expButton = bundle.getString("lb_export");

    //Urls of the links on this page
    String action = WorkflowTemplateConstants.ACTION; 
    String selfUrl = self.getPageURL();
    String removeUrl = selfUrl + "&" + action + "=" + WorkflowTemplateConstants.REMOVE_ACTION;
    String expUrl = selfUrl + "&" + action + "=" + WorkflowTemplateConstants.EXPORT_ACTION;
    String impUrl = _import.getPageURL() + "&" + action + "=" + WorkflowTemplateConstants.IMPORT_ACTION;
    String newUrl = selfUrl + "&" + action + "=" + WorkflowTemplateConstants.NEW_ACTION;
    String modifyUrl = modify.getPageURL()+ "&" + action + "=" + WorkflowTemplateConstants.EDIT_ACTION;
    String dupUrl = duplicate.getPageURL()+ "&" + action + "=" + WorkflowTemplateConstants.DUPLICATE_ACTION;
    String filterUrl = selfUrl + "&" + action + "=" + WebAppConstants.FILTER_SEARCH;
    
    boolean isSuperAdmin = ((Boolean) session.getAttribute(WebAppConstants.IS_SUPER_ADMIN)).booleanValue();
    
    // messages                           
    String removeWarning = bundle.getString("jsmsg_wf_template_remove");    
    WfTemplateSearchParameters fromSearch =
      (WfTemplateSearchParameters)sessionManager.getAttribute("fromSearch");
    String emptyMsg = "msg_no_workflows";
    if (fromSearch != null)
    {
        emptyMsg = "msg_no_workflows_search";
    }
    PermissionSet userPermissions = (PermissionSet) session.getAttribute(WebAppConstants.PERMISSIONS);
    //For dropbox
    List srcLocales = (List)request.getAttribute(WorkflowTemplateConstants.SOURCE_LOCALES);
    List targLocales = (List)request.getAttribute(WorkflowTemplateConstants.TARGET_LOCALES);
    List projectInfos = (List)request.getAttribute(WorkflowTemplateConstants.PROJECTS);
	//Filter Parameters
    String nameFilter = (String) sessionManager.getAttribute("nameField");
    String srcLocaleFilter = (String) sessionManager.getAttribute("srcLocale");
    String targLocaleFilter = (String) sessionManager.getAttribute("targLocale");
    String projectFilter = (String) sessionManager.getAttribute("project");
    String companyNameFilter = (String) sessionManager.getAttribute("companyName");
%>
<%!
// Is mass duplication enabled
static boolean s_duplicationEnabled = false;

static {
    try
    {
        SystemConfiguration sc = SystemConfiguration.getInstance();
        s_duplicationEnabled =
           sc.getBooleanParameter(SystemConfigParamNames.DUPLICATION_OF_OBJECTS_ALLOWED);
    }
    catch (Exception ge)
    {
        // assumes disabled.
    }
}
%>
<HTML>
<HEAD>
<TITLE><%=title%></TITLE>
<SCRIPT SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<SCRIPT SRC="/globalsight/includes/radioButtons.js"></SCRIPT>
<SCRIPT SRC="/globalsight/includes/cookieUtil.js"></SCRIPT>
<script type="text/javascript" src="/globalsight/jquery/jquery-1.6.4.min.js"></script>
<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>
<SCRIPT>
var needWarning = false;
var guideNode = "workflows";
var helpFile = "<%=bundle.getString("help_workflows")%>";

function enableButtons()
{
	var count = $("input[name='RadioBtn']:checked").length;
	if (count > 0) {
		if (WfTemplateForm.removeBtn)
	        WfTemplateForm.removeBtn.disabled = false;
	    if (count == 1) {
	    	<% if (s_duplicationEnabled) { %>
		    if (WfTemplateForm.dupBtn)
		        WfTemplateForm.dupBtn.disabled = false;
		<% } %>
		    if (WfTemplateForm.expBtn)
		        WfTemplateForm.expBtn.disabled = false;
		} else {
			<% if (s_duplicationEnabled) { %>
		    if (WfTemplateForm.dupBtn)
		        WfTemplateForm.dupBtn.disabled = true;
		<% } %>
		    if (WfTemplateForm.expBtn)
		        WfTemplateForm.expBtn.disabled = true;
		}
	} else {
		<% if (s_duplicationEnabled) { %>
	    if (WfTemplateForm.dupBtn)
	        WfTemplateForm.dupBtn.disabled = true;
	<% } %>
	    if (WfTemplateForm.removeBtn)
	        WfTemplateForm.removeBtn.disabled = true;
	    if (WfTemplateForm.expBtn)
	        WfTemplateForm.expBtn.disabled = true;
	}
}

function submitForm(selectedButton) 
{
   var checked = false;        
   var selectedRadioBtn = "";
   if (WfTemplateForm.RadioBtn != null) 
   {
      // If more than one radio button is displayed, the length attribute of the 
      // radio button array will be non-zero, so find which 
      // one is checked
      if (WfTemplateForm.RadioBtn.length)
      {
          for (i = 0; !checked && i < WfTemplateForm.RadioBtn.length; i++) 
          {
              if (WfTemplateForm.RadioBtn[i].checked == true) 
              {
                  selectedRadioBtn += WfTemplateForm.RadioBtn[i].value +" ";
              }
          }
          checked = true;
      }
      // If only one is displayed, there is no radio button array, so
      // just check if the single radio button is checked
      else 
      {
          if (WfTemplateForm.RadioBtn.checked == true)
          {
              checked = true;
              selectedRadioBtn = WfTemplateForm.RadioBtn.value;
          }
      }
   }
   
   if (selectedButton=='New')
   {          
      WfTemplateForm.action = "<%=newUrl%>";
      WfTemplateForm.submit();
      return;
   }
   else if (selectedButton=='Import')
   {          
      WfTemplateForm.action = "<%=impUrl%>";
      WfTemplateForm.submit();
      return;
   }

   // otherwise do the following
   if (!checked) 
   {
       alert("<%=EditUtil.toJavascript(bundle.getString("jsmsg_wf_template_select"))%>");
       return false;
   }
   else 
   {
      if (selectedButton == 'Remove')
      {   
         if(!confirm('<%=EditUtil.toJavascript(confirmRemove)%>')) return false;
        WfTemplateForm.action = "<%=removeUrl%>&<%=WorkflowTemplateConstants.WF_TEMPLATE_INFO_ID%>=" + selectedRadioBtn;
      }
      else if (selectedButton == 'Duplicate')
      {          
         WfTemplateForm.action = "<%=dupUrl%>&<%=WorkflowTemplateConstants.WF_TEMPLATE_INFO_ID%>=" + selectedRadioBtn;
      }
      else if (selectedButton == 'Export')
      {          
         WfTemplateForm.action = "<%=expUrl%>&<%=WorkflowTemplateConstants.WF_TEMPLATE_INFO_ID%>=" + selectedRadioBtn;
      }

      WfTemplateForm.submit();
   }               
}

function handleSelectAll() {
	  var selectAll = $("#selectAll").is(":checked");
	  if (selectAll) {
		  $(":checkbox").attr("checked","true");
	  }else{
		  $(":checkbox").removeAttr("checked");
	  }
	  enableButtons();
}

function editWorkflow(workflowId){
	var url = "<%=modifyUrl%>&wfTemplateInfoId=" + workflowId;
	$("[name=WfTemplateForm]").attr("action",url).submit();
}

function filterItems(e) {
	e = e ? e : window.event;
    var keyCode = e.which ? e.which : e.keyCode;
	if (keyCode == 222) {
		alert("Invalid character \"\'\" is input.");
		return false;
	}
	if (keyCode == 13) {
		WfTemplateForm.action = "<%=filterUrl%>";
		WfTemplateForm.submit();
	}
}

function filterSelectItems(e) {
	e = e ? e : window.event;
    var keyCode = e.which ? e.which : e.keyCode;
	if (keyCode == 13) {
		WfTemplateForm.action = "<%=filterUrl%>";
		WfTemplateForm.submit();
	}
}

</SCRIPT>
</HEAD>
<BODY LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0" MARGINWIDTH="0" MARGINHEIGHT="0" 
 onload="loadGuides()">
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>
<DIV ID="contentLayer" STYLE=" POSITION: ABSOLUTE; Z-INDEX: 9; TOP: 108; LEFT: 20px; RIGHT: 20px;">
<amb:header title="<%=title%>" helperText="<%=helperText%>" />

<FORM NAME="WfTemplateForm" METHOD="POST">

<table cellpadding=0 cellspacing=0 border=0 CLASS="standardText" width="100%" style="min-width:1024px;">
  <tr valign="top">    
    <td align="right">
      <amb:tableNav bean="templates" key="<%=WorkflowTemplateConstants.KEY%>"
       pageUrl="self" />
    </td>
  </tr>
  <tr>
    <td>
      <amb:table bean="templates" id="wft"
                 key="<%=WorkflowTemplateConstants.KEY%>"
                 dataClass="com.globalsight.everest.projecthandler.WorkflowTemplateInfo"
                 pageUrl="self" 
                 emptyTableMsg="<%=emptyMsg%>" hasFilter="true">
      <amb:column label="checkbox" width="2%">
          <input type="checkbox" name="RadioBtn" value="<%=wft.getId()%>" onclick="enableButtons()" >
      </amb:column>
      <amb:column label="lb_name" sortBy="<%=WorkflowTemplateInfoComparator.NAME%>" filter="nameField" filterValue="<%=nameFilter%>" width="13%">
               	<%
					if (userPermissions.getPermissionFor(Permission.WORKFLOWS_EDIT))
					    out.print("<a href='javascript:void(0)' title='Edit Workflow' onclick='editWorkflow(" + wft.getId() + ")'>" + wft.getName() + "</a>");
					else
					  	out.print(wft.getName());
				%>
      </amb:column>
      <amb:column label="lb_description" sortBy="<%=WorkflowTemplateInfoComparator.DESCRIPTION%>" width="15%">
          <%=(wft.getDescription() == null ? "" : wft.getDescription())%>
      </amb:column>
      <amb:column label="lb_source_locale" sortBy="<%=WorkflowTemplateInfoComparator.SOURCE_LOCALE%>" filter="srcLocale" filterValue="<%=srcLocaleFilter%>" width="15%">
          <%=wft.getSourceLocale().getDisplayName(uiLocale)%>
      </amb:column>
      <amb:column label="lb_target_locale" sortBy="<%=WorkflowTemplateInfoComparator.TARGET_LOCALE%>" filter="targLocale" filterValue="<%=targLocaleFilter%>" width="15%">
          <%=wft.getTargetLocale().getDisplayName(uiLocale)%>
      </amb:column>
      <amb:column label="lb_project" sortBy="<%=WorkflowTemplateInfoComparator.PROJECT%>" filter="project" filterValue="<%=projectFilter%>" width="8%">
          <%=wft.getProject().getName()%>
      </amb:column>
      <amb:column label="lb_project_manager" sortBy="<%=WorkflowTemplateInfoComparator.PROJECTMGR%>" width="8%">
          <%=UserUtil.getUserNameById(wft.getProjectManagerId())%>
      </amb:column>
      <amb:column label="lb_target_encoding" sortBy="<%=WorkflowTemplateInfoComparator.TARGET_ENCODING%>"  width="8%">
          <%=wft.getEncoding()%>
      </amb:column>
      <amb:column label="lb_workflow_type" sortBy="<%=WorkflowTemplateInfoComparator.WORKFLOW_TYPE%>" width="8%">
      
      <%// ======================= ugly, code directly =================== %>
	<%String workflowType = wft.getWorkflowType();
      out.print(bundle.getString(wft.getWorkflowType()));
      %>
	  </amb:column>
      <% if (isSuperAdmin) { %>
      <amb:column label="lb_company_name" sortBy="<%=WorkflowTemplateInfoComparator.ASC_COMPANY%>"  filter="workflowPageCompanyName" filterValue="<%=companyNameFilter%>" width="8%">
          <%=CompanyWrapper.getCompanyNameById(wft.getCompanyId())%>
      </amb:column>
      <% } %>
      </amb:table>
    </td>
  </tr>
  <tr>
  	<td>
		<amb:tableNav bean="templates" key="<%=WorkflowTemplateConstants.KEY%>" pageUrl="self" scope="10,20,50,All" showTotalCount="false"/>         	
  	</td>
  </tr> 
  <tr>
    <td style="padding-top:5px">
      <DIV ID="DownloadButtonLayer" ALIGN="left" STYLE="visibility:visible">
    <amb:permission name="<%=Permission.WORKFLOWS_REMOVE%>" >
      <INPUT TYPE="BUTTON" name="removeBtn" VALUE="<%=removeButton%>"
      disabled onclick="submitForm('Remove');"> 
    </amb:permission>
<% if (s_duplicationEnabled) { %>
    <amb:permission name="<%=Permission.WORKFLOWS_DUPLICATE%>" >
      <INPUT TYPE="BUTTON" name="dupBtn" VALUE="<%=dupButton%>..."
      disabled onclick="submitForm('Duplicate');">    
    </amb:permission>
<% } %>

      <INPUT TYPE="BUTTON" name="expBtn" VALUE="<%=expButton%>"
      disabled onclick="submitForm('Export');">    
      <INPUT TYPE="BUTTON" name="impBtn" VALUE="<%=impButton%>"
      onclick="submitForm('Import');">    
    <amb:permission name="<%=Permission.WORKFLOWS_NEW%>" >
      <INPUT TYPE="BUTTON" VALUE="<%=newButton%>..."
      onclick="submitForm('New');">    
    </amb:permission>
      </DIV>
    </td>
  </tr>
</table>
</FORM>
</DIV>
</BODY>
</HTML>
