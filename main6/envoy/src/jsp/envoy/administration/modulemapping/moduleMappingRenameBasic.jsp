<%@ page contentType="text/html; charset=UTF-8"
         errorPage="/envoy/common/activityError.jsp"
         import="java.util.*, com.globalsight.everest.servlet.util.SessionManager,
                  com.globalsight.everest.webapp.WebAppConstants,
                  com.globalsight.everest.webapp.javabean.NavigationBean,
                  com.globalsight.everest.webapp.pagehandler.PageHandler,
                  com.globalsight.everest.webapp.webnavigation.LinkHelper,
                  com.globalsight.everest.servlet.util.ServerProxy,
                  com.globalsight.everest.servlet.EnvoyServletException,
                  com.globalsight.util.GeneralException,
                  java.util.ResourceBundle,
                  com.globalsight.everest.webapp.pagehandler.administration.cvsconfig.modulemapping.ModuleMappingConstants,
      			  com.globalsight.everest.cvsconfig.modulemapping.*,
      			  com.globalsight.everest.webapp.pagehandler.administration.localepairs.LocalePairConstants,
      			  com.globalsight.util.GlobalSightLocale"
          session="true" 
%>
<jsp:useBean id="cancel" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="save" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="createRename" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="updateRename" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="removeRename" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<%
    ResourceBundle bundle = PageHandler.getBundle(session);
    Locale uiLocale = (Locale)session.getAttribute(WebAppConstants.UILOCALE);
    SessionManager sessionMgr = (SessionManager)session.getAttribute(WebAppConstants.SESSION_MANAGER);

    // UI fields

    // Labels, etc
    String lbcancel = bundle.getString("lb_cancel");
    String lbsave = bundle.getString("lb_save");

    String title = bundle.getString("lb_new") + " " + bundle.getString("lb_cvs_module_mapping_rename");
    String saveURL = save.getPageURL() +  "&action=" + ModuleMappingConstants.UPDATE_RENAME;
    String cancelURL = cancel.getPageURL() + "&action=" + ModuleMappingConstants.CANCEL;

    ModuleMappingRename mmr = (ModuleMappingRename)sessionMgr.getAttribute(ModuleMappingConstants.MODULE_MAPPING_RENAME_KEY);
    String sourceName = "", targetName = "";
    long mmrId = 0;
    if (mmr != null) {
    	mmrId = mmr.getId();
    	sourceName = mmr.getSourceName();
    	targetName = mmr.getTargetName();
    }
%>



<html>
<head>
    <meta http-equiv="content-type" content="text/html;charset=UTF-8">
    <title><%= title %></title>
    <script language="JavaScript" SRC="/globalsight/includes/utilityScripts.js"></script>
    <script language="JavaScript" SRC="/globalsight/includes/setStyleSheet.js"></script>
    <%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
    <%@ include file="/envoy/common/warning.jspIncl" %>
<style>
SELECT { behavior: url(/globalsight/includes/SmartSelect.htc); }
</style>

<script language="JavaScript">
var needWarning = true;
var objectName = "<%=bundle.getString("lb_cvs_module_mapping_rename")%>";
var guideNode="rename";
var helpFile = "#";

function submitForm(formAction)
{
    if (formAction == "cancel")
    {
        currentForm.action = "<%=cancelURL%>";
        currentForm.submit();
    }
    if (formAction == "save")
    {
        if (confirmForm()) 
        {
        	currentForm.action = "<%=saveURL%>";
        	currentForm.submit();
        }
    }
	return;
}

//
// Check required fields.
//
function confirmForm()
{
    if (currentForm.sourceName.value == "")
    {
        alert("<%=bundle.getString("jsmsg_cvs_mm_source_name")%>");
        return false;
    }
    if (currentForm.targetName.value == 0)
    {
        alert("<%=bundle.getString("jsmsg_cvs_mm_target_name")%>");
        return false;
    }
	<%
	Set<ModuleMappingRename> mmrs = (Set)sessionMgr.getAttribute(ModuleMappingConstants.MODULE_MAPPING_RENAME_LIST);
	for (ModuleMappingRename m_mmr : mmrs) {
		if (mmrId == m_mmr.getId())
			continue;
		%>
		if (currentForm.sourceName.value == "<%=m_mmr.getSourceName()%>") {
			alert("<%=bundle.getString("jsmsg_cvs_mm_source_name_exist")%>");
			return false;
		}
		<%
	}
	%>
    return true;
}


</script>
</head>

<body leftmargin="0" rightrmargin="0" topmargin="0" marginwidth="0" marginheight="0" onload="loadGuides()">
<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>
    <div id="contentLayer" style="position: absolute; z-index: 9; top: 108; left: 20px; right: 20px;">
    <span class="mainHeading">
        <%=title%>
    </span>
    <br>
    <br>

<form name="currentForm" method="post" action="">

<table border="0" cellspacing="4" cellpadding="2" class="standardText">
  <tr><td></td></tr>
  <tr valign="top">
    <td>
      <table border="0" class="standardText" cellpadding="2">
        <tr>
          <td class="standardText">
            <%=bundle.getString("lb_cvs_mm_source_name")%><span class="asterisk">*</span>:
          </td>
          <td>
            <input type="text" name="sourceName" value="<%=sourceName %>"/>
          </td>
        </tr>
        <tr>
          <td>
            <%=bundle.getString("lb_cvs_mm_target_name")%><span class="asterisk">*</span>:
          </td>
          <td>
            <input type="text" name="targetName" value="<%=targetName %>"/>
          </td>
        </tr>
      </table></td></tr>
      <tr><td>&nbsp;</td></tr>
      <tr>
        <td>
          <input type="button" name="<%=lbcancel%>" value="<%=lbcancel%>"
            onclick="submitForm('cancel')">
          <input type="button" name="<%=lbsave%>" value="<%=lbsave%>"
            onclick="submitForm('save')">
        </td>
      </tr>
    </table>
</form>

