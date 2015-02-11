<%@ page contentType="text/html; charset=UTF-8"
         errorPage="/envoy/common/activityError.jsp"
         import="java.util.*, com.globalsight.everest.servlet.util.SessionManager,
                  com.globalsight.everest.webapp.WebAppConstants,
                  com.globalsight.everest.webapp.javabean.NavigationBean,
                  com.globalsight.everest.webapp.pagehandler.PageHandler,
                  com.globalsight.everest.webapp.pagehandler.administration.localepairs.LocalePairConstants,
                  com.globalsight.everest.foundation.LocalePair,
                  com.globalsight.everest.webapp.webnavigation.LinkHelper,
                  com.globalsight.everest.servlet.util.ServerProxy,
                  com.globalsight.everest.servlet.EnvoyServletException,
                  com.globalsight.util.GeneralException,
                  com.globalsight.util.GlobalSightLocale,
                  java.text.MessageFormat,
                  java.util.Locale,
                  java.util.ResourceBundle,
                  java.util.Vector"
          session="true"
%>
<jsp:useBean id="cancel" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="save" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<%
    ResourceBundle bundle = PageHandler.getBundle(session);
    Locale uiLocale = (Locale)session.getAttribute(WebAppConstants.UILOCALE);
    SessionManager sessionMgr = (SessionManager)session.getAttribute(WebAppConstants.SESSION_MANAGER);

    // UI fields

    // Labels, etc
    String lbcancel = bundle.getString("lb_cancel");
    String lbsave = bundle.getString("lb_save");

    String title = bundle.getString("lb_new") + " " + bundle.getString("lb_locale_pair");
    String saveURL = save.getPageURL() +  "&action=" + LocalePairConstants.CREATE;;
    String cancelURL = cancel.getPageURL() + "&action=" + LocalePairConstants.CANCEL;

    Vector locales = (Vector)request.getAttribute(LocalePairConstants.LOCALES);
%>
<html>
<head>
    <meta http-equiv="content-type" content="text/html;charset=UTF-8">
    <title><%= title %></title>
    <script language="JavaScript" SRC="/globalsight/includes/utilityScripts.js"></script>
    <script language="JavaScript" SRC="/globalsight/includes/setStyleSheet.js"></script>
    <%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
    <%@ include file="/envoy/common/warning.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>

<style>
SELECT { behavior: url(/globalsight/includes/SmartSelect.htc); }
</style>

<script language="JavaScript">
var needWarning = true;
var objectName = "<%=bundle.getString("lb_locale_pair")%>";
var guideNode="localePair";
var helpFile = "<%=bundle.getString("help_locale_pairs_basic_screen")%>";

function submitForm(formAction)
{
    if (formAction == "cancel")
    {
        lpForm.action = "<%=cancelURL%>";
        lpForm.submit();
    }
    if (formAction == "save")
    {
        if (confirmForm()) 
        {
            lpForm.action = "<%=saveURL%>";
            lpForm.submit();
        }
    }
}

//
// Check required fields.
// Check duplicate lp name.
//
function confirmForm()
{
    if (lpForm.sourceLocale.selectedIndex == 0)
    {
        alert("<%=bundle.getString("jsmsg_local_pair_select_source")%>");
        return false;
    }
    if (lpForm.targetLocale.selectedIndex == 0)
    {
        alert("<%=bundle.getString("jsmsg_local_pair_select_target")%>");
        return false;
    }

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

<form name="lpForm" method="post" action="">

<table border="0" cellspacing="4" cellpadding="2" class="standardText">
  <tr><td></td></tr>
  <tr valign="top">
    <td>
      <table border="0" class="standardText" cellpadding="2">
        <tr>
          <td class="standardText">
            <%=bundle.getString("lb_source_locale")%><span class="asterisk">*</span>:
          </td>
          <td>
            <select name="sourceLocale" class="standardText">
            <% 
                out.println("<option value=\"-1\">&nbsp;</option>");
                for (int i = 0; i < locales.size(); i++)
                {
                    GlobalSightLocale locale = (GlobalSightLocale)locales.elementAt(i);
                    out.println("<option value=\"" + locale.getId() + "\">" + 
                                locale.getDisplayName(uiLocale) + "</option>");
                }
            %>
            </select>
          </td>
        </tr>
        <tr>
          <td>
            <%=bundle.getString("lb_target_locale")%><span class="asterisk">*</span>:
          </td>
          <td>
            <select name="targetLocale" class="standardText">
            <% 
                out.println("<option value=\"-1\">&nbsp;</option>");
                for (int i = 0; i < locales.size(); i++)
                {
                    GlobalSightLocale locale = (GlobalSightLocale)locales.elementAt(i);
                    out.println("<option value=\"" + locale.getId() + "\">" + 
                                locale.getDisplayName(uiLocale) + "</option>");
                }
            %>
            </select>
          </td>
        </tr>
      <tr><td colspan="2">&nbsp;</td></tr>
      <tr>
        <td colspan="2">
          <input type="button" name="<%=lbcancel%>" value="<%=lbcancel%>"
            onclick="submitForm('cancel')">
          <input type="button" name="<%=lbsave%>" value="<%=lbsave%>"
            onclick="submitForm('save')">
        </td>
      </tr>
    </table>
</form>

