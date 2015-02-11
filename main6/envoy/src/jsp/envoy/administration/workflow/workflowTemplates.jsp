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
            com.globalsight.everest.servlet.util.ServerProxy,
            java.text.MessageFormat,
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
<jsp:useBean id="search" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="advsearch" scope="request"
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
    String searchButton = bundle.getString("lb_search");

    //Urls of the links on this page
    String action = WorkflowTemplateConstants.ACTION; 
    String selfUrl = self.getPageURL();
    String removeUrl = selfUrl + "&" + action + "=" + WorkflowTemplateConstants.REMOVE_ACTION;
    String expUrl = selfUrl + "&" + action + "=" + WorkflowTemplateConstants.EXPORT_ACTION;
    String impUrl = _import.getPageURL() + "&" + action + "=" + WorkflowTemplateConstants.IMPORT_ACTION;
    String newUrl = selfUrl + "&" + action + "=" + WorkflowTemplateConstants.NEW_ACTION;
    String modifyUrl = modify.getPageURL()+ "&" + action + "=" + WorkflowTemplateConstants.EDIT_ACTION;
    String dupUrl = duplicate.getPageURL()+ "&" + action + "=" + WorkflowTemplateConstants.DUPLICATE_ACTION;
    String advsearchUrl = advsearch.getPageURL() + "&" + action + "=" + WorkflowTemplateConstants.ADV_SEARCH_ACTION;
    String searchUrl = search.getPageURL() + "&" + action + "=" + WorkflowTemplateConstants.SEARCH_ACTION;
    
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
<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>
<SCRIPT>
var needWarning = false;
var guideNode = "workflows";
var helpFile = "<%=bundle.getString("help_workflows")%>";

function enableButtons()
{
<% if (s_duplicationEnabled) { %>
    if (WfTemplateForm.dupBtn)
        WfTemplateForm.dupBtn.disabled = false;
<% } %>
    if (WfTemplateForm.removeBtn)
        WfTemplateForm.removeBtn.disabled = false;
    if (WfTemplateForm.editBtn)
        WfTemplateForm.editBtn.disabled = false;
    if (WfTemplateForm.editBtn)
        WfTemplateForm.expBtn.disabled = false;
}

function submitForm(selectedButton) 
{
   var checked = false;        
   var selectedRadioBtn = null;
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
                  checked = true;
                  selectedRadioBtn = WfTemplateForm.RadioBtn[i].value;
              }
          }
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
   else if (selectedButton=='Search')
   {          
      WfTemplateForm.action = "<%=searchUrl%>";
      WfTemplateForm.submit();
      return;
   }
   else if (selectedButton=='Search')
   {          
      WfTemplateForm.action = "<%=advsearchUrl%>";
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
      else if (selectedButton == 'Edit')
      {          
         WfTemplateForm.action = "<%=modifyUrl%>&<%=WorkflowTemplateConstants.WF_TEMPLATE_INFO_ID%>=" + selectedRadioBtn;
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
</SCRIPT>
</HEAD>
<BODY LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0" MARGINWIDTH="0" MARGINHEIGHT="0" 
 onload="loadGuides()">
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>
<DIV ID="contentLayer" STYLE=" POSITION: ABSOLUTE; Z-INDEX: 9; TOP: 108; LEFT: 20px; RIGHT: 20px;">
<amb:header title="<%=title%>" helperText="<%=helperText%>" />

<FORM NAME="WfTemplateForm" METHOD="POST">
<table border="0" class="standardText" cellpadding="2">
  <tr>
    <td class="standardText">
      <%=bundle.getString("lb_name")%>:
    </td>
    <td class="standardText">
      <select name="nameOptions">
	<option value='<%=SearchCriteriaParameters.BEGINS_WITH%>'>
	  <%= bundle.getString("lb_begins_with") %>
	</option>
	<option value='<%=SearchCriteriaParameters.ENDS_WITH%>'>
	  <%= bundle.getString("lb_ends_with") %>
	</option>
	<option value='<%=SearchCriteriaParameters.CONTAINS%>'>
	  <%= bundle.getString("lb_contains") %>
	</option>
      </select>
      <input type="text" size="30" name="nameField">
    </td>
    <td>
      <input type="button" value="<%=searchButton%>..." onclick="submitForm('Search');">
    </td>
    <td class="standardText" style="padding-bottom: 2px">
      <a class="standardHREF" href="<%=advsearchUrl%>"><%=bundle.getString("lb_advanced_search") %></a>
    </td>
  </tr>
</table>

<p>

<table cellpadding=0 cellspacing=0 border=0 CLASS="standardText">
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
                 emptyTableMsg="<%=emptyMsg%>" >
      <amb:column label="" width="20px">
          <input type=radio name=RadioBtn value="<%=wft.getId()%>" onclick="enableButtons()" >
      </amb:column>
      <amb:column label="lb_name" width="100px" sortBy="<%=WorkflowTemplateInfoComparator.NAME%>">
          <%= wft.getName() %>
      </amb:column>
      <amb:column label="lb_description" width="200px" sortBy="<%=WorkflowTemplateInfoComparator.DESCRIPTION%>">
          <%=(wft.getDescription() == null ? "" : wft.getDescription())%>
      </amb:column>
      <amb:column label="lb_locale_pair" width="200px" sortBy="<%=WorkflowTemplateInfoComparator.LOCALEPAIR%>">
          <%=wft.getSourceLocale().getDisplayName(uiLocale)%> &#x2192;
          <%=wft.getTargetLocale().getDisplayName(uiLocale)%>
      </amb:column>
      <amb:column label="lb_project_manager" width="100px"  sortBy="<%=WorkflowTemplateInfoComparator.PROJECTMGR%>">
          <%=wft.getProjectManagerId()%>
      </amb:column>
      <amb:column label="lb_workflow_type" width="100px" sortBy="<%=WorkflowTemplateInfoComparator.WORKFLOW_TYPE%>">
      
      <%// ======================= ugly, code directly =================== %>
	<%String workflowType = wft.getWorkflowType();
      out.print(bundle.getString(wft.getWorkflowType()));
      %>
	  </amb:column>
      <% if (isSuperAdmin) { %>
      <amb:column label="lb_company_name" sortBy="<%=WorkflowTemplateInfoComparator.ASC_COMPANY%>">
          <%=ServerProxy.getJobHandler().getCompanyById(Long.parseLong(wft.getCompanyId())).getCompanyName()%>
      </amb:column>
      <% } %>
      </amb:table>
    </td>
  </tr>
  <tr>
    <td style="padding-top:5px">
      <DIV ID="DownloadButtonLayer" ALIGN="RIGHT" STYLE="visibility: visible">
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
    <amb:permission name="<%=Permission.WORKFLOWS_EDIT%>" >
      <INPUT TYPE="BUTTON" name="editBtn" VALUE="<%=editButton%>..."
      disabled onclick="submitForm('Edit');">
    </amb:permission>
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
