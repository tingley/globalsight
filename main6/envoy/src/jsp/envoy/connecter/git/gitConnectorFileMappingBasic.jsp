<%@ page contentType="text/html; charset=UTF-8"
         errorPage="/envoy/common/activityError.jsp"
         import="java.util.*, com.globalsight.everest.servlet.util.SessionManager,
                  com.globalsight.everest.webapp.WebAppConstants,
                  com.globalsight.everest.webapp.javabean.NavigationBean,
                  com.globalsight.everest.webapp.pagehandler.PageHandler,
                  com.globalsight.everest.webapp.webnavigation.LinkHelper,
                  com.globalsight.everest.servlet.util.ServerProxy,
                  com.globalsight.cxe.entity.gitconnector.GitConnectorFileMapping,
                  com.globalsight.cxe.entity.gitconnector.GitConnector,
                  com.globalsight.everest.servlet.EnvoyServletException,
                  com.globalsight.util.GeneralException,
                  java.util.ResourceBundle,
      			  com.globalsight.everest.webapp.pagehandler.administration.localepairs.LocalePairConstants,
      			  com.globalsight.util.GlobalSightLocale"
          session="true" 
%>
<%@page import="com.globalsight.everest.cvsconfig.CVSServer"%>
<jsp:useBean id="cancel" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="save" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<%
    ResourceBundle bundle = PageHandler.getBundle(session);
    Locale uiLocale = (Locale)session.getAttribute(WebAppConstants.UILOCALE);
    SessionManager sessionMgr = (SessionManager)session.getAttribute(WebAppConstants.SESSION_MANAGER);

    String title = "";
    title = bundle.getString("lb_edit") + " " + bundle.getString("lb_git_connector_file_mapping");
    
    String lbcancel = bundle.getString("lb_cancel");
    String lbsave = bundle.getString("lb_save");

    //Set data
    Vector sourceLocales = (Vector)request.getAttribute("sourceLocalePairs");
    Vector targetLocales = (Vector)request.getAttribute("targetLocalePairs");
    
    GitConnectorFileMapping gcfm =  (GitConnectorFileMapping)request.getAttribute("gitConnectorFileMappingKey");
    long gcfmId = gcfm.getId();
    GitConnector connector = (GitConnector) request.getAttribute("gitConnector");
    long gitConnectorId = connector.getId();
    long companyId = connector.getCompanyId();
    String saveURL = save.getPageURL() + "&action=update&gitConnectorId=" + gitConnectorId + "&id=" + gcfmId;
    String cancelURL = cancel.getPageURL() + "&gitConnectorId=" + gitConnectorId;
    String subFolderMapped = (String)request.getAttribute("subFolderMapped");
    
    String sourceLocale = "", sourceMappingPath = "", targetLocale = "", targetMappingPath = "";
   	sourceLocale = gcfm.getSourceLocale();
   	sourceMappingPath = gcfm.getSourceMappingPath();
   	targetLocale = gcfm.getTargetLocale();
   	targetMappingPath = gcfm.getTargetMappingPath();
%>



<%@page import="com.globalsight.util.GlobalSightLocale"%>
<%@page import="com.globalsight.everest.foundation.LocalePair"%>
<%@page import="com.globalsight.everest.cvsconfig.CVSModule"%>
<%@page import="com.globalsight.everest.webapp.pagehandler.administration.cvsconfig.CVSConfigConstants"%>
<html>
<head>
    <meta http-equiv="content-type" content="text/html;charset=UTF-8">
    <title><%= title %></title>
    <script language="JavaScript" SRC="/globalsight/includes/utilityScripts.js"></script>
    <script language="JavaScript" SRC="/globalsight/includes/setStyleSheet.js"></script>
    <%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
    <%@ include file="/envoy/common/warning.jspIncl" %>

	<SCRIPT SRC="/globalsight/includes/radioButtons.js"></SCRIPT>

<style>
SELECT { behavior: url(/globalsight/includes/SmartSelect.htc); }
</style>

<script language="JavaScript">
var needWarning = true;
var objectName = "<%=bundle.getString("lb_git_connector_file_mapping")%>";
var guideNode="rename";
var helpFile = "<%=bundle.getString("help_git_connector_file_mapping_modify")%>";

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
	if (currentForm.sourceLocale.selectedIndex == 0)
    {
        alert("<%=bundle.getString("jsmsg_git_file_mapping_source_locale")%>");
        return false;
    }
    if (currentForm.sourceMappingPath.value == "") {
        alert("<%=bundle.getString("jsmsg_git_file_mapping_source_mapping_path")%>");
        return false;
    }

    if (currentForm.targetLocale.selectedIndex == 0)
    {
        alert("<%=bundle.getString("jsmsg_git_file_mapping_target_locale")%>");
        return false;
    }
    if (currentForm.targetMappingPath.value == "") {
        alert("<%=bundle.getString("jsmsg_git_file_mapping_target_mapping_path")%>");
        return false;
    }
    
    var sF, tF;
    var sM, tM;
    sM = currentForm.sourceMappingPath.value;
    tM = currentForm.targetMappingPath.value;
    if (sM.lastIndexOf("\\") < sM.lastIndexOf(".")) {
        sF = true;
    } else {
        sF = false;
    }
    if (tM.lastIndexOf("\\") < tM.lastIndexOf(".")) {
        tF = true;
    } else {
        tF = false;
    }
    if (!sF && tF) {
        alert("<%=bundle.getString("jsmsg_git_file_mapping_can_not_run") %>");
        return false;
    }
    return true;
}

function MappingPathSelect(mode) 
{
    window.open("/globalsight/envoy/connecter/git/gitConnectorFileSelect.jsp?mode="+mode+"&gitConnectorId="+<%=gitConnectorId%>, mode, "height=500px,width=500px, resizable=yes, scrollbars=yes");
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
<input type="hidden" name="companyId" value="<%=companyId%>"/>
<table border="0" cellspacing="4" cellpadding="2" class="standardText">
  <tr><td></td></tr>
  <tr valign="top">
    <td>
      <table border="0" class="standardText" cellpadding="2">
      	<tr>
          <td class="standardText">
            Git Connector Name:
          </td>
          <td colspan="4" align="left" class="standardText">
            <%=connector.getName() %>
          </td>
        </tr>
        <tr>
          <td class="standardText">
            <%=bundle.getString("lb_source_locale")%><span class="asterisk">*</span>:
          </td>
          <td>
            <select name="sourceLocale" class="standardText">
            <option value="-1"><%=bundle.getString("lb_choose")%></option>
            <% 
                for (int i = 0; i < sourceLocales.size(); i++)
                {
                	GlobalSightLocale locale = (GlobalSightLocale)sourceLocales.elementAt(i);
                	if (!locale.toString().equals(sourceLocale))
                    	out.println("<option value=\"" + locale.getId() + "\">" + locale.getDisplayName(uiLocale) + "</option>");
                	else
                		out.println("<option value=\"" + locale.getId() + "\" selected>" + locale.getDisplayName(uiLocale) + "</option>");
                }
            %>
            </select>
          </td>
        </tr>
        <tr>
          <td class="standardText">
            <%=bundle.getString("lb_source_mapping_path")%><span class="asterisk">*</span>:
          </td>
          <td>
            <input type="text" name="sourceMappingPath" value="<%=sourceMappingPath %>" size="40" editable="false" readonly="true"/>&nbsp;&nbsp;
            <input type="button" name="sourceSelect" value="<%=bundle.getString("lb_select")%>"
            onclick="MappingPathSelect('Source')">
          </td>
        </tr>
        <tr>
          <td>
            <%=bundle.getString("lb_target_locale")%><span class="asterisk">*</span>:
          </td>
          <td>
            <select name="targetLocale" class="standardText">
            <option value="-1"><%=bundle.getString("lb_choose")%></option>
            <% 
	            for (int i = 0; i < targetLocales.size(); i++)
	            {
	            	GlobalSightLocale locale = (GlobalSightLocale)targetLocales.elementAt(i);
	            	if (!locale.toString().equals(targetLocale))
	                	out.println("<option value=\"" + locale.getId() + "\">" + locale.getDisplayName(uiLocale) + "</option>");
	            	else
	            		out.println("<option value=\"" + locale.getId() + "\" selected>" + locale.getDisplayName(uiLocale) + "</option>");
	            }
            %>
            </select>
          </td>
        </tr>
        <tr>
          <td class="standardText">
            <%=bundle.getString("lb_target_mapping_path")%><span class="asterisk">*</span>:
          </td>
          <td>
            <input type="text" name="targetMappingPath" value="<%=targetMappingPath %>" size="40"  editable="false" />
            <input type="button" name="targetSelect" value="<%=bundle.getString("lb_select")%>"
            onclick="MappingPathSelect('Target')">
          </td>
        </tr>
        <tr>
          <td class="standardText">
            &nbsp;
          </td>
          <td>
            <input type="checkbox" name="subfolder" value="1" <%=("1").equals(subFolderMapped)?"checked":"" %>><%=bundle.getString("msg_git_connector_file_mapping_auto_create_subfolder") %>
          </td>
        </tr>
      </table></td></tr>
      <tr>
        <td>
		 <div align="right">
          <input type="button" name="<%=lbcancel%>" value="<%=lbcancel%>"
            onclick="submitForm('cancel')">
          <input type="button" name="<%=lbsave%>" value="<%=lbsave%>"
            onclick="submitForm('save')">
         </div>
        </td>
      </tr>
    </table>
</form>

