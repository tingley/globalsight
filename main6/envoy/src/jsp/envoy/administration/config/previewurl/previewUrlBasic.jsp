<%@ page contentType="text/html; charset=UTF-8"
         errorPage="/envoy/common/activityError.jsp"
         import="java.util.*, com.globalsight.everest.servlet.util.SessionManager,
                  com.globalsight.cxe.entity.previewurl.PreviewUrlImpl,
                  com.globalsight.everest.webapp.WebAppConstants,
                  com.globalsight.everest.webapp.javabean.NavigationBean,
                  com.globalsight.everest.webapp.pagehandler.PageHandler,
                  com.globalsight.everest.webapp.pagehandler.administration.config.previewurl.PreviewUrlMainHandler,
                  com.globalsight.everest.webapp.webnavigation.LinkHelper,
                  com.globalsight.everest.servlet.util.ServerProxy,
                  com.globalsight.everest.servlet.EnvoyServletException,
                  com.globalsight.util.GeneralException,
                  java.text.MessageFormat,
                  java.util.ResourceBundle,
                  java.util.Vector"
          session="true"
%>
<jsp:useBean id="cancel" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="save" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<%
    ResourceBundle bundle = PageHandler.getBundle(session);
    SessionManager sessionMgr = (SessionManager)session.getAttribute(WebAppConstants.SESSION_MANAGER);

    // UI fields

    // Labels, etc
    String lbcancel = bundle.getString("lb_cancel");
    String lbsave = bundle.getString("lb_save");

    String saveURL = save.getPageURL();
    String title = null;
    if (sessionMgr.getAttribute("edit") != null)
    {
        saveURL +=  "&action=edit";
        title = bundle.getString("lb_edit_db_preview_rules");
    }
    else
    {
        saveURL +=  "&action=new";
        title = bundle.getString("msg_new_dynamic_preview_profile");
    }
    
    String cancelURL = cancel.getPageURL() + "&action=cancel";

    // Data
    ArrayList names = (ArrayList)request.getAttribute(PreviewUrlMainHandler.NAMES);
    String pname = "";
    String desc = "";
    String rule = "";
    PreviewUrlImpl preview = (PreviewUrlImpl)sessionMgr.getAttribute(PreviewUrlMainHandler.PREVIEW_KEY);
    if (preview != null)
    {
        pname = preview.getName();
        desc = preview.getDescription();
        if (desc == null) desc = "";
        rule = preview.getContent();
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
<%@ include file="/envoy/common/warning.jspIncl" %>


<script language="JavaScript">
var needWarning = true;
var objectName = "<%=bundle.getString("lb_db_preview_rule")%>";
var guideNode = "dbPreviewRules";
var helpFile = "<%=bundle.getString("help_db_preview_rules_basic_screen")%>";

function submitForm(formAction)
{
    if (formAction == "cancel")
    {
        dbForm.action = "<%=cancelURL%>";
        dbForm.submit();
    }
    if (formAction == "save")
    {
        if (confirmForm()) 
        {
            dbForm.action = "<%=saveURL%>";
            dbForm.submit();
        }
    }
}

//
// Check required fields.
//
function confirmForm()
{
    if (isEmptyString(stripBlanks(dbForm.nameField.value)))
    {
        alert("<%=bundle.getString("jsmsg_db_preview_url_name")%>");
        dbForm.nameField.focus();
        return false;
    }
    if (isEmptyString(stripBlanks(dbForm.ruleField.value)))
    {
        alert("<%=bundle.getString("jsmsg_db_preview_url_rule")%>");
        dbForm.ruleField.focus();
        return false;
    }
    // check for dups
<%
    if (names != null)
    {
        for (int i = 0; i < names.size(); i++)
        {
            String thename = (String)names.get(i);
%>
            if ("<%=thename%>" == dbForm.nameField.value && "<%=thename%>" != "<%=pname%>")
            {
                alert("<%=bundle.getString("jsmsg_duplicate_xmlrulefile")%>");
                return false;
            }
<%
        }
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

<form name="dbForm" method="post" action="">

<table border="0" cellspacing="2" cellpadding="2" class="standardText">
  <tr><td></td></tr>
  <tr valign="top">
    <td>
      <table border="0" class="standardText" cellpadding="2">
        <tr>
          <td>
            <%=bundle.getString("lb_name")%><span class="asterisk">*</span>:
          </td>
          <td>
            <input type="text" name="nameField" value="<%=pname%>" >
          </td>
        </tr>
        <tr>
          <td valign="top">
            <%=bundle.getString("lb_description")%>:
          </td>
          <td>
            <textarea name="descField" rows=4 cols=50><%=desc%></textarea>
          </td>
        </tr>
        <tr>
          <td valign="top">
            <%=bundle.getString("lb_xml_rules")%><span class="asterisk">*</span>:
          </td>
          <td>
            <textarea name="ruleField" rows=10 cols=50><%=rule%></textarea>
          </td>
        </tr>
        <tr><td>&nbsp;</td></tr>
      <tr>
        <td>
          <input type="button" name="cancel" value="<%=lbcancel%>"
            onclick="submitForm('cancel')">
          <input type="button" name="save" value="<%=lbsave%>"
            onclick="submitForm('save')">
        </td>
      </tr>
    </table>
  </td>
</tr>
</table>
</form>

