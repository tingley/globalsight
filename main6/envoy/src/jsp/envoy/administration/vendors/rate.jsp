<%@ page contentType="text/html; charset=UTF-8"
         errorPage="/envoy/common/activityError.jsp"
         import="java.util.*, com.globalsight.everest.servlet.util.SessionManager,
                  com.globalsight.everest.webapp.WebAppConstants,
                  com.globalsight.everest.webapp.javabean.NavigationBean,
                  com.globalsight.everest.webapp.pagehandler.PageHandler,
                  com.globalsight.everest.vendormanagement.Rating,
                  com.globalsight.everest.vendormanagement.Vendor,
                  com.globalsight.util.resourcebundle.ResourceBundleConstants,
                  com.globalsight.util.resourcebundle.SystemResourceBundle,
                  com.globalsight.everest.webapp.webnavigation.LinkHelper,
                  com.globalsight.everest.servlet.util.ServerProxy,
                  com.globalsight.everest.servlet.EnvoyServletException,
                  com.globalsight.everest.util.system.SystemConfigParamNames,
                  com.globalsight.everest.util.system.SystemConfiguration,
                  com.globalsight.util.GeneralException,
                  java.text.MessageFormat,
                  java.util.Locale,
                  java.util.ResourceBundle"
          session="true"
%>
<jsp:useBean id="cancel" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="save" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<%
    ResourceBundle bundle = PageHandler.getBundle(session);
    SessionManager sessionMgr = (SessionManager)session.getAttribute(WebAppConstants.SESSION_MANAGER);

    // Labels, etc
    String edit = (String)sessionMgr.getAttribute("edit");
    String title= bundle.getString("lb_vendors") + " - ";
    if (edit == null)
        title += bundle.getString("lb_new_rate");
    else
        title += bundle.getString("lb_edit") + " " + bundle.getString("lb_rate");
    String lbcancel = bundle.getString("lb_cancel");
    String lbsave = bundle.getString("lb_save");
    String lbgood = bundle.getString("lb_good");
    String lbaverage = bundle.getString("lb_average");
    String lbpoor = bundle.getString("lb_poor");

    String cancelURL = cancel.getPageURL() + "&action=cancel";
    String saveURL = save.getPageURL() + "&action=save";

    // Data
    String comment = "";
    Rating rating = (Rating)sessionMgr.getAttribute("rating");
    String value = "1";
    if (rating != null)
    {
        value = String.valueOf(rating.getValue());
        comment = rating.getComment();
        if (comment == null) comment = "";
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


<script language="JavaScript">
var needWarning = false;
var objectName = "<%=bundle.getString("lb_vendors")%>";
var guideNode="projects";
var helpFile = "<%=bundle.getString("help_vendors_rate")%>";

function submitForm(formAction)
{
    if (formAction == "cancel")
    {
       if (confirmJump())
       {
           rateForm.action = "<%=cancelURL%>";
       }
       else
       {
          return false;
       }
    }
    else if (formAction == "save")
    {
        rateForm.action = "<%=saveURL%>";
    }
    rateForm.submit();
}

</script>
</head>

<body leftmargin="0" rightrmargin="0" topmargin="0" marginwidth="0" marginheight="0" onload="loadGuides()">
<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>
    <div id="contentLayer" style="position: absolute; z-index: 9; top: 108; left: 20px; right: 20px;">
<br>
    <span class="mainHeading">
        <%=title%>
    </span>
    <p>

<form name="rateForm" method="post" action="">

<table border="0" cellspacing="2" cellpadding="2" class="standardText">
  <tr><td></td></tr>
  <tr valign="top">
    <td>
      <table border="0" class="standardText" cellpadding="2">
        <tr>
          <td>
            <%=bundle.getString("lb_rating")%><span class="asterisk">*</span>:
          </td>
          <td>
            <select name="ratingField">
<%
                out.println("<option value='1' ");
                if (value.equals("1")) out.print("selected ");
                out.println(">1 (" + lbgood + ")</option>");

                out.println("<option value='2' ");
                if (value.equals("2")) out.print("selected ");
                out.println(">2 (" + lbaverage + ")</option>");

                out.println("<option value='3' ");
                if (value.equals("3")) out.print("selected ");
                out.println(">3 (" + lbpoor + ")</option>");
%>
            </select>
          </td>
        </tr>
        <tr>
        <td>
            <%=bundle.getString("lb_comment")%>:
          </td>
          <td>
            <textarea rows="3" cols="30" name="comment"><%=comment%></textarea>
          </td>
        </tr>
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

