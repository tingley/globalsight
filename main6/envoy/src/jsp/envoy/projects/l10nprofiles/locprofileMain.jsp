<%@ taglib uri="/WEB-INF/tlds/globalsight.tld" prefix="amb" %>
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
         com.globalsight.everest.servlet.util.ServerProxy,
         java.util.ArrayList,
         java.util.Locale, java.util.ResourceBundle"
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
    
    boolean isSuperAdmin = ((Boolean) session.getAttribute(WebAppConstants.IS_SUPER_ADMIN)).booleanValue();
%>
<HTML>
<HEAD>
<META HTTP-EQUIV="content-type" CONTENT="text/html;charset=UTF-8">
<TITLE><%= title %></TITLE>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/radioButtons.js"></SCRIPT>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>
<SCRIPT LANGUAGE="JavaScript">
    var needWarning = false;
    var objectName = "";
    var guideNode = "locProfiles";
    var helpFile = "<%=bundle.getString("help_localization_profiles_main_screen")%>";

function enableButtons()
{
    if (locprofileForm.removeBtn)
        locprofileForm.removeBtn.disabled = false;
    if (locprofileForm.editBtn)
        locprofileForm.editBtn.disabled = false;
    if (locprofileForm.detailsBtn)
        locprofileForm.detailsBtn.disabled = false;
<% if (duplicationEnabled) { %>
    if (locprofileForm.dupBtn)
        locprofileForm.dupBtn.disabled = false;
<% } %>
}

function submitForm(button)
{
    if (button == "New")
    {
<%
        if (preReqData != null)
        {
%>
            alert("<%=preReqData%>");
            return;
<%
        }
%>
        locprofileForm.action = "<%=newURL%>";
    }
    else
    {
        value = getRadioValue(locprofileForm.radioBtn);
        if (button == "Edit")
        {
            locprofileForm.action = "<%=modifyURL%>";
        }
        else if (button == "Remove")
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

<form name="locprofileForm" method="post">
    <table cellpadding=0 cellspacing=0 border=0 class="standardText">

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
                     emptyTableMsg="msg_no_locprofiles" >
                <amb:column label="" width="10px">
                    <input type="radio" name="radioBtn" value="<%=locprofile.getProfileId()%>"
                        onclick="enableButtons()">
                    <input type="hidden" id="<%=locprofile.getProfileId()%>_workflowNum" value="<%=locprofile.getWorkflowTemplateCount() %>">
                </amb:column>
                <amb:column label="lb_name" sortBy="<%=LocProfileComparator.NAME%>"
                    width="150px">
                    <%= locprofile.getName() %>
                </amb:column>
                <amb:column label="lb_description" sortBy="<%=LocProfileComparator.DESC%>">
                     <% out.print(locprofile.getDescription() == null ?
                         "" : locprofile.getDescription()); %>
                </amb:column>
                <% if (isSuperAdmin) { %>
                <amb:column label="lb_company_name" sortBy="<%=LocProfileComparator.ASC_COMPANY%>">
                    <%=ServerProxy.getJobHandler().getCompanyById(Long.parseLong(locprofile.getCompanyId())).getCompanyName()%>
                </amb:column>
                <% } %>
              </amb:table>
            </td>
         </tr>
         <tr>
    <td style="padding-top:5px" align="right">
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
    <amb:permission name="<%=Permission.LOCPROFILES_EDIT%>" >
        <INPUT TYPE="BUTTON" VALUE="<%=bundle.getString("lb_edit")%>..."
            name="editBtn" disabled onClick="submitForm('Edit');">
    </amb:permission>
    <amb:permission name="<%=Permission.LOCPROFILES_NEW%>" >
        <INPUT TYPE="BUTTON" VALUE="<%=bundle.getString("lb_new")%>..."
            onClick="submitForm('New');">
    </amb:permission>
    </td>
</TR>
</TABLE>
</TD>
</TR>
</TABLE>
</form>
</BODY>

