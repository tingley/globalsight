<%@ page contentType="text/html; charset=UTF-8"
         errorPage="/envoy/common/activityError.jsp"
         import="java.util.*, com.globalsight.everest.servlet.util.SessionManager,
         		  com.globalsight.cxe.entity.gitconnector.GitConnector,
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
<%@page import="com.globalsight.everest.cvsconfig.CVSServer"%>
<jsp:useBean id="cancel" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="save" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="self" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<%
    ResourceBundle bundle = PageHandler.getBundle(session);
    Locale uiLocale = (Locale)session.getAttribute(WebAppConstants.UILOCALE);
    SessionManager sessionMgr = (SessionManager)session.getAttribute(WebAppConstants.SESSION_MANAGER);
    GitConnector connector = (GitConnector) request.getAttribute("gitConnector");
    long gitConnectorId = connector.getId();
    long companyId = connector.getCompanyId();

    // Labels, etc
    String lbcancel = bundle.getString("lb_cancel");
    String lbsave = bundle.getString("lb_save");

    String saveURL = save.getPageURL() + "&action=save";
    String cancelURL = cancel.getPageURL() + "&action=canel";
    String changeURL = self.getPageURL() + "&action=changeSourceLocale";
    
    String title = bundle.getString("lb_new") + " " + bundle.getString("lb_git_connector_file_mapping");
    
    String sourceLocale = (String)request.getAttribute("sourceLocale");
    String sourceMappingPath = (String)request.getAttribute("sourceMappingPath");
    Vector sourceLocales = (Vector)request.getAttribute(ModuleMappingConstants.SOURCE_LOCALE_PAIRS);
    Vector targetLocales = (Vector)request.getAttribute(ModuleMappingConstants.TARGET_LOCALE_PAIRS);
    
%>

<%@page import="com.globalsight.util.GlobalSightLocale"%>
<%@page import="com.globalsight.everest.foundation.LocalePair"%>
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
var helpFile = "<%=bundle.getString("help_git_connector_file_mapping_new")%>";

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
    var count = currentForm.count.value;
    var sF, tF;
    var sM, tM;
    var canRun = true;
    sM = currentForm.sourceMappingPath.value;
    if (sM.lastIndexOf("\\") < sM.lastIndexOf(".")) {
        sF = true;
    } else {
        sF = false;
    }
    for (var i=0;i<count;i++) {
      if (!canRun)
          break;
      if (document.getElementById("targetLocale" + i).checked) {
          tM = document.getElementById("targetMappingPath" + i).value;
          if (tm != "") {
       	    if (tM.lastIndexOf("\\") < tM.lastIndexOf(".")) {
       	        tF = true;
       	    } else {
       	        tF = false;
       	    }
       	    if (!sF && tF) {
           	    canRun = false;
       	    } else {
           	    canRun = true;
       	    }
          } else {
            canRun = false;
          }
      }
    }

    if (!canRun) {
        alert("<%=bundle.getString("jsmsg_git_file_mapping_can_not_run") %>");
        return false;
    }

    return true;
}

function MappingPathSelect(mode) 
{
    window.open("/globalsight/envoy/connecter/git/gitConnectorFileSelect.jsp?mode="+mode+"&gitConnectorId="+<%=gitConnectorId%>, mode, "height=500px,width=500px, resizable=yes, scrollbars=yes");
}

function changeSourceLocale() {
	currentForm.action = "<%=changeURL%>";
	currentForm.submit();
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
<input type="hidden" name="gitConnectorId" value="<%=gitConnectorId%>"/>
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
          <td colspan="4" align="left">
            <select name="sourceLocale" class="standardText" onchange="changeSourceLocale()">
            <option value="-1"><%=bundle.getString("lb_choose")%></option>
            <%
                GlobalSightLocale locale = null;
                for (int i = 0; i < sourceLocales.size(); i++)
                {
                    locale = (GlobalSightLocale)sourceLocales.elementAt(i);
                    if (String.valueOf(locale.getId()).equals(sourceLocale))
                        out.println("<option value=\"" + locale.getId() + "\" selected>" + locale.getDisplayName(uiLocale) + "</option>");
                    else
                    	out.println("<option value=\"" + locale.getId() + "\">" + locale.getDisplayName(uiLocale) + "</option>");
                }
            %>
            </select>
          </td>
        </tr>
        <tr>
          <td class="standardText">
            <%=bundle.getString("lb_source_mapping_path")%><span class="asterisk">*</span>:
          </td>
          <td colspan="4" align="left">
            <input type="text" name="sourceMappingPath" size="40" value="<%=sourceMappingPath %>" editable="false" readonly="true"/>&nbsp;&nbsp;
            <input type="button" name="sourceSelect" value="<%=bundle.getString("lb_select")%>"
            onclick="MappingPathSelect('Source')">
          </td>
        </tr>
        <tr>
        <table class="listborder" border="0" cellpadding="1" cellspacing="0" width="100%">
        	<thead>
	        <tr  class = "tableHeadingBasic" style="height:25px">
	        	<td class="standardText"><%=bundle.getString("lb_target_locale")%></td>
	        	<td class="standardText"><%=bundle.getString("lb_target_mapping_path")%></td>
	        	<td class="standardText"><%=bundle.getString("msg_git_connector_file_mapping_auto_create_subfolder") %></td>    
	        </tr>
	        </thead>
        <tbody>
        <%
        int i = 0;
        String tableRowEvenTM = "tableRowEvenTM";
        String tableRowOddTM = "tableRowOddTM";
        
        for (i = 0; i < targetLocales.size(); i++) {
        	String style = "";
        	locale = (GlobalSightLocale)targetLocales.elementAt(i);
        	if (i % 2 == 0)
        	{
        		style = tableRowEvenTM;
        	}
        	else
        	{
        		style = tableRowOddTM;
        	}
        	%>
        	<tr class="<%=style%>">
        	   <td class="standardText"><input type="checkbox" id="targetLocale<%=i%>" name="targetLocale<%=i %>" value="<%=locale.getId() %>"/><%=locale.getDisplayName(uiLocale) %></td></td>
        	   <td class="standardText">
        	       <input type="text" id="targetMappingPath<%=i %>" name="targetMappingPath<%=i %>" value="" size="40" editable="false" />
        	       <input type="button" name="moduleSelectBtn" value="<%=bundle.getString("lb_select") %>" onclick="MappingPathSelect('<%=i %>')"/>     	       
        	   </td class="standardText">
               <td><input type="checkbox" id="subfolder<%=i %>" name="subfolder<%=i %>" value="1"></td>        	   
        	</tr>
        	<%
        }
        %>
         </tbody>
        </table>
        </tr>
        <input type="hidden" name="count" value="<%=i %>" />
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
