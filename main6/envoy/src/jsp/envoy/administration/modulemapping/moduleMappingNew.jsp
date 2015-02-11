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
<%@page import="com.globalsight.everest.cvsconfig.CVSServer"%>
<jsp:useBean id="cancel" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="save" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="change" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<%
    ResourceBundle bundle = PageHandler.getBundle(session);
    Locale uiLocale = (Locale)session.getAttribute(WebAppConstants.UILOCALE);
    SessionManager sessionMgr = (SessionManager)session.getAttribute(WebAppConstants.SESSION_MANAGER);

    // Labels, etc
    String lbcancel = bundle.getString("lb_cancel");
    String lbsave = bundle.getString("lb_save");

    String saveURL = save.getPageURL() + "&action=" + ModuleMappingConstants.CREATE;
    String cancelURL = cancel.getPageURL() + "&action=" + ModuleMappingConstants.CANCEL;
    String changeURL = change.getPageURL() + "&action=Change";
    
    String title = bundle.getString("lb_new") + " " + bundle.getString("lb_cvs_module_mapping");
    
    //Set data
    String cvsServer = (String)request.getAttribute("cvsServer");
    String sourceLocale = (String)request.getAttribute("sourceLocale");
    String sourceModule = (String)request.getAttribute("sourceModule");
    Vector sourceLocales = (Vector)request.getAttribute(ModuleMappingConstants.SOURCE_LOCALE_PAIRS);
    Vector targetLocales = (Vector)request.getAttribute(ModuleMappingConstants.TARGET_LOCALE_PAIRS);
    ArrayList<CVSServer> servers = (ArrayList)request.getAttribute(CVSConfigConstants.CVS_SERVER_LIST);
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
var objectName = "<%=bundle.getString("lb_cvs_module_mapping")%>";
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
    if (!isSelectionMade(currentForm.cvsServer)) {
        alert("<%=bundle.getString("jsmsg_cvs_module_mapping_selected")%>");
        return false;
    }
    if (currentForm.sourceLocale.selectedIndex == 0)
    {
        alert("<%=bundle.getString("jsmsg_cvs_mm_source_locale")%>");
        return false;
    }
    if (currentForm.sourceModule.value == "") {
        alert("<%=bundle.getString("jsmsg_cvs_mm_source_module")%>");
        return false;
    }
    var count = currentForm.count.value;
    var sF, tF;
    var sM, tM;
    var canRun = true;
    sM = currentForm.sourceModule.value;
    if (sM.lastIndexOf("\\") < sM.lastIndexOf(".")) {
        sF = true;
    } else {
        sF = false;
    }
    for (var i=0;i<count;i++) {
      if (!canRun)
          break;
      if (document.getElementById("targetLocale" + i).checked) {
          tM = document.getElementById("targetModule" + i).value;
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
        alert("<%=bundle.getString("jsmsg_cvs_mm_can_not_run") %>");
        return false;
    }

    return true;
}

function moduleSelect(mode) {
    var value = currentForm.cvsServer.value;
    if (!isSelectionMade(currentForm.cvsServer)) {
        alert("<%=bundle.getString("jsmsg_cvs_module_selected")%>");
        return;
    }
    window.open("/globalsight/envoy/administration/modulemapping/fileSelect.jsp?mode="+mode+"&serverId="+value, mode, "height=500px,width=500px, resizable=yes, scrollbars=yes");
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

<table border="0" cellspacing="4" cellpadding="2" class="standardText">
  <tr><td></td></tr>
  <tr valign="top">
    <td>
      <table border="0" class="standardText" cellpadding="2">
        <tr>
          <td class="standardText"><%=bundle.getString("lb_cvs_server") %><span class="asterisk">*</span>:
          </td>
          <td colspan="4" align="left">
            <select name="cvsServer" class="standardText">
		    <option value="-1"><%=bundle.getString("lb_choose")%></option>
            <% 
                StringBuilder sb = null;
                String serverName, repositoryName, content;
                for (CVSServer s : servers)
                {
                    serverName = s.getName();
                    repositoryName = s.getRepository();
                    content = serverName + "-" + repositoryName;
                    if (String.valueOf(s.getId()).equals(cvsServer))
                        out.println("<option value=\"" + s.getId() + "\" selected>" + content + "</option>");
                    else
                    	out.println("<option value=\"" + s.getId() + "\">" + content + "</option>");
                }
            %>
            </select>
          </td>
        </tr>
        <tr>
          <td class="standardText">
            <%=bundle.getString("lb_cvs_mm_source_locale")%><span class="asterisk">*</span>:
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
            <%=bundle.getString("lb_cvs_mm_source_module")%><span class="asterisk">*</span>:
          </td>
          <td colspan="4" align="left">
            <input type="text" name="sourceModule" size="40" value="<%=sourceModule %>" editable="false"/>&nbsp;&nbsp;
            <input type="button" name="sourceSelect" value="<%=bundle.getString("lb_select")%>"
            onclick="moduleSelect('Source')">
          </td>
        </tr>
        
        <%
        int i = 0;
        for (i = 0; i < targetLocales.size(); i++) {
        	locale = (GlobalSightLocale)targetLocales.elementAt(i);
        	%>
        	<tr>
        	   <td class="standardText"><%=bundle.getString("lb_cvs_mm_target_locale")%></td>
        	   <td><input type="checkbox" id="targetLocale<%=i%>" name="targetLocale<%=i %>" value="<%=locale.getId() %>"/><%=locale.getDisplayName(uiLocale) %></td>
        	   <td class="standardText"><%=bundle.getString("lb_cvs_mm_target_module")%></td>
        	   <td>
        	       <input type="text" id="targetModule<%=i %>" name="targetModule<%=i %>" value="" size="40" editable="false" />&nbsp;
                   <input type="button" name="moduleSelectBtn" value="<%=bundle.getString("lb_select") %>" onclick="moduleSelect('<%=i %>')"/>        	       
        	   </td>
               <td><input type="checkbox" id="subfolder<%=i %>" name="subfolder<%=i %>" value="1"><%=bundle.getString("msg_cvs_mm_auto_create_subfolder") %></td>        	   
        	</tr>
        	<%
        }
        %>
        
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
