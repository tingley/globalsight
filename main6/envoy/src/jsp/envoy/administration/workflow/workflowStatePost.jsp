<%@page import="com.globalsight.everest.webapp.pagehandler.administration.workflow.WorkflowStatePostConstants"%>
<%@ taglib uri="/WEB-INF/tlds/globalsight.tld" prefix="amb" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ page contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="java.util.*,com.globalsight.everest.webapp.javabean.NavigationBean,
         com.globalsight.util.resourcebundle.ResourceBundleConstants,
         com.globalsight.util.resourcebundle.SystemResourceBundle,
         com.globalsight.everest.permission.Permission,
         com.globalsight.everest.util.comparator.WorkflowStatePostComparator,
         com.globalsight.everest.servlet.util.SessionManager,
         com.globalsight.everest.webapp.pagehandler.PageHandler, 
         com.globalsight.everest.util.system.SystemConfigParamNames,
         com.globalsight.everest.util.system.SystemConfiguration,
         com.globalsight.everest.permission.PermissionSet,
		 com.globalsight.everest.company.CompanyWrapper,
		 com.globalsight.everest.webapp.WebAppConstants,
         java.util.ArrayList,
         java.util.Locale, 
         java.util.ResourceBundle"
         session="true" %>

<jsp:useBean id="self" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="new1" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="modify" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="remove" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="wfStatePostProfiles" class="java.util.ArrayList" scope="request"/>

<%
    ResourceBundle bundle = PageHandler.getBundle(session);
    String selfURL = self.getPageURL();
    String newURL = new1.getPageURL() + "&action=new";
    String modifyURL = modify.getPageURL() + "&action=edit";
    String removeURL = remove.getPageURL() + "&action=remove";
    String title = bundle.getString("lb_workflow_state_post_profiles");
    String helperText = bundle.getString("helper_text_workflow_state_post_profile_main");
    SessionManager sessionMgr = (SessionManager)
               session.getAttribute(WebAppConstants.SESSION_MANAGER);
    String deps = (String)sessionMgr.getAttribute(WorkflowStatePostConstants.DEPENDENCIES);
    String preReqData = (String)request.getAttribute("preReqData");
    
    String nameFilter = (String) sessionMgr.getAttribute("nameFilter");
    String wfStatePostCompanyNameFilter = (String) sessionMgr.getAttribute("wfStatePostCompanyNameFilter");
    String listenerURLFilter = (String) sessionMgr.getAttribute("listenerURLFilter");
    PermissionSet userPermissions = (PermissionSet) session.getAttribute(WebAppConstants.PERMISSIONS);
    boolean isSuperAdmin = ((Boolean) session.getAttribute(WebAppConstants.IS_SUPER_ADMIN)).booleanValue();
%>
<HTML>
<HEAD>
<META HTTP-EQUIV="content-type" CONTENT="text/html;charset=UTF-8">
<TITLE><%= title %></TITLE>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/radioButtons.js"></SCRIPT>
<script type="text/javascript" src="/globalsight/jquery/jquery-1.6.4.min.js"></script>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>
<SCRIPT LANGUAGE="JavaScript">
var needWarning = false;
var objectName = "";
var guideNode = "wfStatePostProfiles";
var helpFile = "<%=bundle.getString("help_workflow_state_post_profiles_main_screen")%>";
		function enableButtons()
		{
			if ($(":checked").not($("option")).not($("#selectAll")).length == 1) {
				workflowStatePostForm.removeBtn.disabled = false;
			}
			else {
				workflowStatePostForm.removeBtn.disabled = true;
			}
		}

		function submitForm(button)
		{
		    if (button == "New")
		    {
		    	workflowStatePostForm.action = "<%=newURL%>";
		    	workflowStatePostForm.submit();
		    }
		    else if(button == "Remove")
		    {
		        var value = getRadioValue(workflowStatePostForm.radioBtn);
		        if (!confirm("<%=bundle.getString("msg_confirm_workflow_state_post_profile_removal")%>"))
		            return false;
		        var url = "<%=selfURL%>&action=check&wfStatePostId="+value;
		        $.ajax({
		        	   type: "POST",
		        	   url: url,
		        	   dataType:"json",
		        	   success: function(data){
		        		   var wrong = data.wrong;
				 		   if(wrong != "needRemove"){
				 			   alert("The selected workflow state post profile is in use, cannot be removed.");
				 			  workflowStatePostForm.action = "<%=selfURL %>"
				 				 workflowStatePostForm.submit();
				 		   }else{
				 			  workflowStatePostForm.action = "<%=removeURL%>";
				 			 workflowStatePostForm.submit();
				 		   }
		        	   }
		        	});
		    }
		}

		function editwfStatePostProfile(wfStatePostId){
			var url = "<%=modifyURL%>&radioBtn=" + wfStatePostId;
			$("[name=workflowStatePostForm]").attr("action",url).submit();
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

		function filterItems(e) {
			e = e ? e : window.event;
		    var keyCode = e.which ? e.which : e.keyCode;
			if (keyCode == 222) {
				alert("Invalid character \"\'\" is input.");
				return false;
			}
			if (keyCode == 13) {
				var actionUrl = "<%=selfURL%>&action=filterSearch";
				$("#workflowStatePostForm").attr("action", actionUrl).submit();
			}
		}
</SCRIPT>
</HEAD>
<BODY LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0" MARGINWIDTH="0" MARGINHEIGHT="0"
    ONLOAD="loadGuides()">
<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>
<DIV ID="contentLayer" STYLE=" POSITION: ABSOLUTE; Z-INDEX: 9; TOP: 108px; LEFT: 20px; RIGHT: 20px;">
<% if (deps != null) {
    sessionMgr.removeElement(WorkflowStatePostConstants.DEPENDENCIES);
%>
    <amb:header title="<%=title%>" helperText="<%=deps%>" />
<%   } else {  %>
    <amb:header title="<%=title%>" helperText="<%=helperText%>" />
<% }  %>

<form id="workflowStatePostForm" name="workflowStatePostForm" method="post">
    <table cellpadding=0 cellspacing=0 border=0 class="standardText" width="100%" style="min-width:1024px;">

        <tr valign="top">
          <td align="right">
            <amb:tableNav bean="wfStatePostProfiles"
                 key="<%=WorkflowStatePostConstants.WFSPPROFILE_KEY%>"
                 pageUrl="self" />
          </td>
        </tr>
        <tr>
          <td>
              <amb:table bean="wfStatePostProfiles" id="wfStatePostProfile"
                     key="<%=WorkflowStatePostConstants.WFSPPROFILE_KEY%>"
                     dataClass="com.globalsight.everest.workflowmanager.WorkflowStatePosts"
                     pageUrl="self"
                     emptyTableMsg="msg_no_workflow_state_post_profile" hasFilter="true">
                <amb:column label="checkbox" width="2%">
                    <input type="checkbox" name="radioBtn" value="<%=wfStatePostProfile.getId()%>"
                        onclick="enableButtons()">
                </amb:column>
                <amb:column label="lb_name" sortBy="<%=WorkflowStatePostComparator.NAME%>" filter="nameFilter" filterValue="<%=nameFilter%>" width="10%">
               	<%
					    out.print("<a href='javascript:void(0)' title='Edit Workflow State Post Profiles' onclick='editwfStatePostProfile(" + wfStatePostProfile.getId() + ")'>" + wfStatePostProfile.getName() + "</a>");
				%>
                </amb:column>
                <amb:column label="lb_description" sortBy="<%=WorkflowStatePostComparator.DESCRIPTION%>" width="20%">
                     <% out.print(wfStatePostProfile.getDescription() == null ?
                         "" : wfStatePostProfile.getDescription()); %>
                </amb:column>
                 <amb:column label="lb_listener_url" sortBy="<%=WorkflowStatePostComparator.LISTENER_URL%>" filter="listenerURLFilter" filterValue="<%=listenerURLFilter%>" width="20%">
                     <%= wfStatePostProfile.getListenerURL()%>
                </amb:column>
                 <amb:column label="lb_secret_key" sortBy="<%=WorkflowStatePostComparator.SECRET_KEY%>"  width="10%">
                     <%= wfStatePostProfile.getSecretKey()%>
                </amb:column>
                <% if (isSuperAdmin) { %>
                <amb:column label="lb_company_name" sortBy="<%=WorkflowStatePostComparator.ASC_COMPANY%>" filter="wfStatePostCompanyNameFilter" filterValue="<%=wfStatePostCompanyNameFilter%>" width="8%">
                    <%=CompanyWrapper.getCompanyNameById(wfStatePostProfile.getCompanyId())%>
                </amb:column>
                <% } %>
              </amb:table>
            </td>
         </tr>
         <tr>
         	<td>
				<amb:tableNav bean="wfStatePostProfiles" key="<%=WorkflowStatePostConstants.WFSPPROFILE_KEY%>" pageUrl="self" scope="10,20,50,All" showTotalCount="false"/>         	
         	</td>
         </tr>
         <tr>
		    <td style="padding-top:5px" align="left">
		        <INPUT TYPE="BUTTON" VALUE="<%=bundle.getString("lb_remove")%>"
		            name="removeBtn" disabled onClick="submitForm('Remove');">
		            
		        <INPUT id="newButton" TYPE="BUTTON" VALUE="<%=bundle.getString("lb_new")%>..."
		            onClick="submitForm('New');">
		    </td>
		 </tr>

</TABLE>
</TD>
</TR>
</TABLE>
</form>
</BODY>

