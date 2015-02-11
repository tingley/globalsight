<%@ taglib uri="/WEB-INF/tlds/globalsight.tld" prefix="amb" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ page contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="java.util.*,com.globalsight.everest.webapp.javabean.NavigationBean,
         com.globalsight.util.resourcebundle.ResourceBundleConstants,
         com.globalsight.util.resourcebundle.SystemResourceBundle,
         com.globalsight.everest.permission.Permission,
         com.globalsight.everest.servlet.util.SessionManager,
         com.globalsight.everest.util.comparator.LocProfileComparator,
         com.globalsight.everest.webapp.pagehandler.projects.l10nprofiles.LocProfileStateConstants, 
         com.globalsight.everest.webapp.pagehandler.PageHandler, 
         com.globalsight.everest.foundation.BasicL10nProfileInfo,
         com.globalsight.everest.util.system.SystemConfigParamNames,
         com.globalsight.everest.util.system.SystemConfiguration,
		 com.globalsight.everest.company.CompanyWrapper,
         java.util.ArrayList,
         java.util.Locale, java.util.ResourceBundle,
         com.globalsight.everest.webapp.pagehandler.projects.l10nprofiles.LocProfileHandlerHelper"
         session="true" %>

<jsp:useBean id="self" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="new1" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="mod1" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="duplicate" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="details" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="remove" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="locprofiles" class="java.util.ArrayList" scope="request"/>

<%
    ResourceBundle bundle = PageHandler.getBundle(session);
    String selfURL = self.getPageURL();
    String newURL = new1.getPageURL() + "&action=new";
    String modifyURL = mod1.getPageURL() + "&action=edit";
    String dupURL = duplicate.getPageURL() + "&action=dup";
    String detailsURL = details.getPageURL() + "&action=details";
    String removeURL = remove.getPageURL() + "&action=remove";
    String title = bundle.getString("lb_loc_profiles");
    String helperText = bundle.getString("helper_text_loc_profile_main");

    SessionManager sessionMgr = (SessionManager)
               session.getAttribute(WebAppConstants.SESSION_MANAGER);
    String deps = (String)sessionMgr.getAttribute(LocProfileStateConstants.DEPENDENCIES);
    String preReqData = (String)request.getAttribute("preReqData");
    
    String l10nProfilesNameFilter = (String) sessionMgr.getAttribute("L10nProfilesNameFilter");
    String l10nProfilesCompanyNameFilter = (String) sessionMgr.getAttribute("L10nProfilesCompanyNameFilter");
    String l10nProfilesTMPFilter = (String) sessionMgr.getAttribute("L10nProfilesTMPFilter");
    String l10nProfilesProjectFilter = (String) sessionMgr.getAttribute("L10nProfilesProjectFilter");

    // Is mass duplication enabled
    boolean duplicationEnabled = false;
    try
    {
        SystemConfiguration sc = SystemConfiguration.getInstance();
        duplicationEnabled =
           sc.getBooleanParameter(SystemConfigParamNames.DUPLICATION_OF_OBJECTS_ALLOWED);
    }
    catch (Exception ge)
    {
        // assumes disabled.
    }
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
var guideNode = "locProfiles";
var helpFile = "<%=bundle.getString("help_localization_profiles_main_screen")%>";
var checkNewMessage;

function enableButtons()
{
	if ($(":checked").not($("option")).not($("#selectAll")).length == 1) {
		if(locprofileForm.detailsBtn){
		    locprofileForm.detailsBtn.disabled = false;
		}
		if(locprofileForm.removeBtn){
			locprofileForm.removeBtn.disabled = false;
		}
		if(locprofileForm.editBtn){
			locprofileForm.editBtn.disabled = false;
		}
<% if (duplicationEnabled) { %>
		if(locprofileForm.dupBtn){
    		locprofileForm.dupBtn.disabled = false;
		}
<% } %>
	}
	else {
		if(locprofileForm.detailsBtn){
		    locprofileForm.detailsBtn.disabled = true;
		}
		if(locprofileForm.removeBtn){
			locprofileForm.removeBtn.disabled = true;
		}
		if(locprofileForm.editBtn){
			locprofileForm.editBtn.disabled = true;
		}
<% if (duplicationEnabled) { %>
		if(locprofileForm.dupBtn){
    		locprofileForm.dupBtn.disabled = true;
		}
<% } %>
	}
}

function submitForm(button)
{
    if (button == "New")
    {
    	if (checkNewMessage!=undefined && checkNewMessage != "nomessage")
        {
            alert(checkNewMessage);
            return;
        }
        locprofileForm.action = "<%=newURL%>";
    }
    else
    {
        value = getRadioValue(locprofileForm.radioBtn);
		if (button == "Remove")
        {
            if (!confirm("<%=bundle.getString("msg_confirm_locprofile_removal")%>"))
                 return false;
            locprofileForm.action = "<%=removeURL%>";
        }
        else if (button == "Details")
        {
            locprofileForm.action = "<%=detailsURL%>";
        }
        else if (button == "Dup")
        {
            if (document.getElementById(value + "_workflowNum").value > 1) {
                 alert("<%=bundle.getString("jsmsg_loc_profiles_cannot_dup")%>");
                 return;
            }
            locprofileForm.action = "<%=dupURL%>";
        }
    }
    locprofileForm.submit();
}

function editL10nProfile(l10nProflieId){
	var url = "<%=modifyURL%>&radioBtn=" + l10nProflieId;
	$("[name=locprofileForm]").attr("action",url).submit();
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
		$("#locprofileForm").attr("action", actionUrl).submit();
	}
}

<%
if (userPermissions.getPermissionFor(Permission.LOCPROFILES_NEW)) {
%>
	$(document).ready(function(){
		sendAjaxForCheckNewMessage();
	});
<%
}
%>

function sendAjaxForCheckNewMessage(){
	$("#newButton").attr("disabled","disabled");
	var url = "${self.pageURL}" + "&action=ajax&t="+new Date().getTime();
	$.get(url,function(data){
		checkNewMessage = data;
		$("#newButton").removeAttr("disabled");
	})
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
    sessionMgr.removeElement(LocProfileStateConstants.DEPENDENCIES);
%>
    <amb:header title="<%=title%>" helperText="<%=deps%>" />
<%   } else {  %>
    <amb:header title="<%=title%>" helperText="<%=helperText%>" />
<% }  %>

<form id="locprofileForm" name="locprofileForm" method="post">
    <table cellpadding=0 cellspacing=0 border=0 class="standardText" width="100%" style="min-width:1024px;">

        <tr valign="top">
          <td align="right">
            <amb:tableNav bean="locprofiles"
                 key="<%=LocProfileStateConstants.LOCPROFILE_KEY%>"
                 pageUrl="self" />
          </td>
        </tr>
        <tr>
          <td>
              <amb:table bean="locprofiles" id="locprofile"
                     key="<%=LocProfileStateConstants.LOCPROFILE_KEY%>"
                     dataClass="com.globalsight.everest.foundation.BasicL10nProfileInfo"
                     pageUrl="self"
                     emptyTableMsg="msg_no_locprofiles" hasFilter="true">
                <amb:column label="checkbox" width="2%">
                    <input type="checkbox" name="radioBtn" value="<%=locprofile.getProfileId()%>"
                        onclick="enableButtons()">
                    <input type="hidden" id="<%=locprofile.getProfileId()%>_workflowNum" value="<%=locprofile.getWorkflowTemplateCount() %>">
                </amb:column>
                <amb:column label="lb_name" sortBy="<%=LocProfileComparator.NAME%>" filter="L10nProfilesNameFilter" filterValue="<%=l10nProfilesNameFilter%>" width="10%">
               	<%
					if (userPermissions.getPermissionFor(Permission.LOCPROFILES_EDIT))
					    out.print("<a href='javascript:void(0)' title='Edit Localization Profiles' onclick='editL10nProfile(" + locprofile.getProfileId() + ")'>" + locprofile.getName() + "</a>");
					else
					  	out.print(locprofile.getName());
				%>
                </amb:column>
                <amb:column label="lb_description" sortBy="<%=LocProfileComparator.DESC%>" width="20%">
                     <% out.print(locprofile.getDescription() == null ?
                         "" : locprofile.getDescription()); %>
                </amb:column>
                <% if (isSuperAdmin) { %>
                <amb:column label="lb_company_name" sortBy="<%=LocProfileComparator.ASC_COMPANY%>" filter="L10nProfilesCompanyNameFilter" filterValue="<%=l10nProfilesCompanyNameFilter%>" width="8%">
                    <%=CompanyWrapper.getCompanyNameById(locprofile.getCompanyId())%>
                </amb:column>
                <% } %>
                 <amb:column label="lb_tm_profiles" sortBy="<%=LocProfileComparator.TMPROFILE%>" filter="L10nProfilesTMPFilter" filterValue="<%=l10nProfilesTMPFilter%>" width="20%">
                     <%= locprofile.getTmProfileName()%>
                </amb:column>
                 <amb:column label="lb_project" sortBy="<%=LocProfileComparator.PROJECT%>" filter="L10nProfilesProjectFilter" filterValue="<%=l10nProfilesProjectFilter%>" width="10%">
                     <%= locprofile.getProjectName()%>
                </amb:column>
                 <amb:column label="lb_source_locale" sortBy="<%=LocProfileComparator.SOURCE_LOCALE%>" width="20%">
                     <%= locprofile.getSrcLocaleName()%>
                </amb:column>
                 <amb:column label="lb_workflow_dispatch" sortBy="<%=LocProfileComparator.WORKFLOW_DISPATCH%>" width="10%">
					 <%
					 	if(locprofile.getIsAutoDispatch() == 'Y'){
					 	    out.println(bundle.getString("lb_automatic"));
					 	}else{
					 	    out.println(bundle.getString("lb_manual"));
					 	}
					 %>
                </amb:column>
              </amb:table>
            </td>
         </tr>
         <tr>
         	<td>
				<amb:tableNav bean="locprofiles" key="<%=LocProfileStateConstants.LOCPROFILE_KEY%>" pageUrl="self" scope="10,20,50,All" showTotalCount="false"/>         	
         	</td>
         </tr>
         <tr>
		    <td style="padding-top:5px" align="left">
		    <amb:permission name="<%=Permission.LOCPROFILES_REMOVE%>" >
		        <INPUT TYPE="BUTTON" VALUE="<%=bundle.getString("lb_remove")%>"
		            name="removeBtn" disabled onClick="submitForm('Remove');">
		    </amb:permission>
		    <amb:permission name="<%=Permission.LOCPROFILES_DETAILS%>" >
		        <INPUT TYPE="BUTTON" VALUE="<%=bundle.getString("lb_details")%>..."
		            name="detailsBtn" disabled onClick="submitForm('Details');">
		    </amb:permission>
<% if (duplicationEnabled) { %>
    <amb:permission name="<%=Permission.LOCPROFILES_DUP%>" >
        <INPUT TYPE="BUTTON" VALUE="<%=bundle.getString("lb_duplicate")%>..."
            name="dupBtn" disabled onClick="submitForm('Dup');">
    </amb:permission>
<%  } %>
		    <amb:permission name="<%=Permission.LOCPROFILES_NEW%>" >
		        <INPUT id="newButton" TYPE="BUTTON" VALUE="<%=bundle.getString("lb_new")%>..."
		            onClick="submitForm('New');">
		    </amb:permission>
		    </td>
		 </tr>

</TABLE>
</TD>
</TR>
</TABLE>
</form>
</BODY>

