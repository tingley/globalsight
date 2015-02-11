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
                com.globalsight.everest.webapp.pagehandler.administration.vendors.VendorConstants,
                com.globalsight.util.GlobalSightLocale,
                java.util.Iterator,
                java.util.Locale,
                java.util.Set,
                java.util.ResourceBundle" 
        session="true" 
%>

<jsp:useBean id="prev" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="next" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="cancel" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="add" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>


<% 
    SessionManager sessionMgr = (SessionManager)
             session.getAttribute(WebAppConstants.SESSION_MANAGER);
    ResourceBundle bundle = PageHandler.getBundle(session);        
   
    String addURL = add.getPageURL() + "&action=add";
    String prevURL = prev.getPageURL() + "&action=previous";
    String cancelURL = cancel.getPageURL() + "&action=cancel";
    String nextURL = next.getPageURL() + "&action=next";
    String lbRoles = bundle.getString("lb_roles");;
    String title= bundle.getString("lb_new")  + " " +
                  bundle.getString("lb_vendor") + " - " + lbRoles;
    
    // Labels
    String lbPrev = bundle.getString("lb_previous"); 
    String lbNext = bundle.getString("lb_next"); 
    String lbCancel = bundle.getString("lb_cancel");
    String lbAdd = bundle.getString("lb_add");

    // Data
    Vendor vendor = (Vendor)sessionMgr.getAttribute(VendorConstants.VENDOR);
    Set roles = vendor.getRoles();
        
%>
<html>
<head>
<META HTTP-EQUIV="content-type" content="text/html;charset=UTF-8">
<title><%= title %></title>
<script language="JavaScript" SRC="/globalsight/includes/setStyleSheet.js"></script>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>
<script language="JavaScript" SRC="/globalsight/includes/utilityScripts.js"></script>
<script language="JavaScript">

var needWarning = true;
var objectName = "<%= bundle.getString("lb_vendor") %>";
var guideNode = "";
var helpFile = "<%=bundle.getString("help_vendors_roles")%>";

function submitForm(btnName) {
    if (btnName == "prev")
    {
        vendorForm.action = "<%=prevURL %>";
    }
    else if (btnName == "next")
    {
        vendorForm.action = "<%=nextURL %>";
    }
    else if (btnName == "add")
    {
        if (confirmLocales() == false)
        {
            return;
        }
        if (confirmActivities(vendorForm) == false)
        {
            return;
        }
        vendorForm.action = "<%=addURL %>";
    }
    vendorForm.submit();
}

</script>
</head>
<body leftmargin="0" rightmargin="0" topmargin="0" marginwidth="0" marginheight="0" onload="loadGuides()">
<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>

<div id="contentLayer" style="position: absolute; z-index: 8; top: 108; left: 20px; right: 20px;">

<div class="mainHeading"> <%=title%> </div>
<br>
<span class="standardText"><%= bundle.getString("helper_text_users_roles") %></span>
<p>
<form name="vendorForm" method="post">
<table cellpadding=4 cellspacing=0 border=0 bordercolor="red" class="standardText">
    <tr>
      <td>
        <fieldset style="padding:15px">
          <legend style="font-weight:bolder; font-size:larger">
            <%= bundle.getString("lb_roles")%></legend>
          <table border="0" class="standardText">
            <% if (roles != null && roles.size() > 0) { %>
                <tr><td>&nbsp;</td></tr>
                <tr>
                    <td colspan=2>
                    <jsp:include page="rolesTable.jsp"/>
                    </td>
                </tr>
            <% } %>
                <jsp:include page="localeDropDowns.jsp"/>
                <tr>
                    <td>
                        <jsp:include page="activities.jsp"/>
                    </td>
                </tr>
                <tr>
                    <td>
                    <input type="button" name="<%=lbAdd%>" value="<%=lbAdd%>" 
                        onclick="javascript: submitForm('add')">
                    </td>
                </tr>
           </table>
        </fieldset>
       </td>
    </tr>
</table>
<p>
        <input type="button" name="<%=lbCancel%>" value="<%=lbCancel%>"
            onclick="location.replace('<%=cancelURL%>')">
        <input type="button" name="<%=lbPrev%>" value="<%=lbPrev%>" 
            onclick="javascript: submitForm('prev')">
        <input type="button" name="<%=lbNext%>" value="<%=lbNext%>" 
            onclick="javascript: submitForm('next')">
</table>
</form>
</div>
</body>
</html>
