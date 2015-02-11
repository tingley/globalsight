<%@ page contentType="text/html; charset=UTF-8"
         errorPage="/envoy/common/activityError.jsp"
         import="java.util.*, com.globalsight.everest.servlet.util.SessionManager,
                  com.globalsight.everest.webapp.WebAppConstants,
                  com.globalsight.everest.webapp.javabean.NavigationBean,
                  com.globalsight.everest.webapp.pagehandler.PageHandler,
                  com.globalsight.everest.webapp.webnavigation.LinkHelper,
                  com.globalsight.everest.servlet.util.ServerProxy,
                  com.globalsight.everest.servlet.EnvoyServletException,
                  com.globalsight.util.FormUtil,
                  com.globalsight.util.GeneralException,
                  com.globalsight.cxe.entity.fileextension.FileExtensionImpl,
                  java.text.MessageFormat,
                  java.util.Iterator,
                  java.util.ResourceBundle"
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

    String saveURL = save.getPageURL() + "&action=create";
    String cancelURL = cancel.getPageURL() + "&action=cancel";
    String title =  bundle.getString("lb_new") + " " + bundle.getString("lb_file_extension");
    

    // Data
    Collection names = (Collection)request.getAttribute("names");

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
var objectName = "<%=bundle.getString("lb_file_extension")%>";
var guideNode="fileExtensions";
var helpFile = "<%=bundle.getString("help_file_extensions_basic")%>";

function submitForm(formAction)
{
    if (formAction == "cancel")
    {
        feForm.action = "<%=cancelURL%>";
        feForm.submit();
    }
    if (formAction == "save")
    {
        if (confirmForm()) 
        {
            feForm.action = "<%=saveURL%>";
            feForm.submit();
        }
    }
}

//
// Check required fields.
// Check duplicate names.
//
function confirmForm()
{
    if (isEmptyString(feForm.nameField.value))
    {
        alert("<%= bundle.getString("jsmsg_file_extension") %>");
        feForm.nameField.value = "";
        feForm.nameField.focus();
        return false;
    }        
    if (hasSpecialChars(feForm.nameField.value))
    {
        alert("<%= bundle.getString("lb_extension") %>" + "<%= bundle.getString("msg_invalid_entry") %>");
        return false;
    }

    // check for dups 
<%
    if (names != null)
    {
        Iterator iter = names.iterator();
        while (iter.hasNext())
        {
            FileExtensionImpl fe = (FileExtensionImpl)iter.next();
%>
            if ("<%=fe.getName()%>".toLowerCase() == feForm.nameField.value.toLowerCase())
            {
                alert("<%=bundle.getString("jsmsg_duplicate_file_extension")%>");
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

<form name="feForm" method="post" action="">

<table border="0" cellspacing="2" cellpadding="2" class="standardText">
  <tr><td></td></tr>
  <tr valign="top">
    <td>
      <table border="0" class="standardText" cellpadding="2">
        <tr>
          <td>
            <%=bundle.getString("lb_extension")%><span class="asterisk">*</span>:
          </td>
          <td>
            <input type="textfield" name="nameField" maxlength="40" size="30" >
          </td>
        </tr>
      <tr>
        <td>
          <input type="button" name="<%=lbcancel%>" value="<%=lbcancel%>"
            onclick="submitForm('cancel')">
          <input type="button" name="<%=lbsave%>" value="<%=lbsave%>"
            onclick="submitForm('save')">
        </td>
      </tr>
    </table>

<% String tokenName = FormUtil.getTokenName(FormUtil.Forms.NEW_FILE_EXTENSION); %>
<input type="hidden" name="<%=tokenName%>" value="<%=request.getAttribute(tokenName)%>" />

</form>
