<%@ page contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="java.util.*,com.globalsight.everest.vendormanagement.Vendor,
            com.globalsight.everest.servlet.util.SessionManager,
            com.globalsight.everest.util.system.SystemConfigParamNames,
            com.globalsight.everest.util.system.SystemConfiguration,
            com.globalsight.everest.webapp.WebAppConstants,
            com.globalsight.everest.webapp.javabean.NavigationBean,
            com.globalsight.everest.webapp.pagehandler.PageHandler,
            com.globalsight.everest.webapp.pagehandler.administration.users.UserHandlerHelper,
            com.globalsight.everest.foundation.LocalePair,
            com.globalsight.util.GlobalSightLocale,
            java.util.Iterator,
            java.util.Locale,
            java.util.ResourceBundle" 
        session="true" 
%>
<jsp:useBean id="done" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="cancel" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<% 
    SessionManager sessionMgr = (SessionManager)
             session.getAttribute(WebAppConstants.SESSION_MANAGER);
    ResourceBundle bundle = PageHandler.getBundle(session);        
    String edit = (String) sessionMgr.getAttribute("edit");
   
    String doneURL = done.getPageURL() + "&action=doneEditRole";
    String cancelURL = cancel.getPageURL() + "&action=cancel";
    String title = null;
    String lbRoles = bundle.getString("lb_roles");;
    title= bundle.getString("lb_edit") + " " + bundle.getString("lb_vendor") + " - " + lbRoles;
    
    //Labels
    String lbDone = bundle.getString("lb_done"); 
    String lbCancel = bundle.getString("lb_cancel");
    String lbSrcLocale = bundle.getString("lb_source_locale");
    String lbTargLocale = bundle.getString("lb_target_locale");

    // Data
    LocalePair lp = (LocalePair)sessionMgr.getAttribute("localePair");
    GlobalSightLocale srcLocale = lp.getSource();
    GlobalSightLocale targLocale = lp.getTarget();
%>
<html>
<head>
<title><%= title %></title>
<script SRC="/globalsight/includes/setStyleSheet.js"></script>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>
<script SRC="/globalsight/includes/utilityScripts.js"></script>
<script>
var needWarning = true;
var objectName = "<%= bundle.getString("lb_vendor") %>";
var guideNode = "";
var helpFile = "<%=bundle.getString("help_vendors_roles")%>";

function submitForm(btnName)
{
    if (btnName == "done")
    {
        vendorForm.action = "<%=doneURL %>";
        vendorForm.submit();
    }
}

</script>
</head>
<body leftmargin="0" rightmargin="0" topmargin="0" marginwidth="0" marginheight="0" onload="loadGuides()">
<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>

<div id="contentLayer" style="position: absolute; z-index: 8; top: 108; left: 20px; right: 20px;">

<div class="mainHeading"> <%=title%> </div>
<p>
<form name="vendorForm" method=post>
<table cellpadding=4 cellspacing=0 border=0 bordercolor="red" class="standardText">
    <tr>
      <td class="standardText">
        <%= lbSrcLocale %>:
      </td>
      <td class="standardText">
        <%= srcLocale.getDisplayName() %>
      </td>
    </tr>
    <tr>
      <td class="standardText">
        <%= lbTargLocale %>:
      </td>
      <td class="standardText">
        <%= targLocale.getDisplayName() %>
      </td>
    </tr>
    <tr><td>&nbsp;</td></tr>
    <tr>
      <td colspan=2>
        <jsp:include page="activities.jsp"/>
      </td>
    </tr>
</table>
<p>
        <input type="button" name="<%=lbCancel%>" value="<%=lbCancel%>"
            onclick="location.replace('<%=cancelURL%>')">
        <input type="button" name="<%=lbDone%>" value="<%=lbDone%>" 
            onclick="javascript: submitForm('done')">
</table>
</form>
</div>
</body>
</html>
