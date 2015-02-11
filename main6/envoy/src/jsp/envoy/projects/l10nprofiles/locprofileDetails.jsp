<%@ taglib uri="/WEB-INF/tlds/globalsight.tld" prefix="amb" %>
<%@ page contentType="text/html; charset=UTF-8"
	errorPage="/envoy/common/error.jsp"
	import="java.util.*,com.globalsight.everest.webapp.javabean.NavigationBean,
	 com.globalsight.everest.foundation.BasicL10nProfile,
	 com.globalsight.everest.webapp.pagehandler.PageHandler,
	 com.globalsight.everest.servlet.util.SessionManager,
	 com.globalsight.everest.webapp.WebAppConstants,
	 com.globalsight.everest.webapp.pagehandler.projects.l10nprofiles.LocProfileStateConstants,
	 com.globalsight.everest.webapp.pagehandler.projects.l10nprofiles.LocProfileHandlerHelper,
	 com.globalsight.everest.webapp.pagehandler.administration.workflow.WorkflowTemplateConstants,
	 com.globalsight.util.GlobalSightLocale,
	 com.globalsight.util.resourcebundle.ResourceBundleConstants,
	 com.globalsight.util.resourcebundle.SystemResourceBundle,
     com.globalsight.everest.util.system.SystemConfigParamNames,
     com.globalsight.everest.util.system.SystemConfiguration,
	 java.lang.Integer, java.util.Locale,
     com.globalsight.util.GeneralException,
	 java.util.ResourceBundle
         "
	 session="true" %>

<jsp:useBean id="ok" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<%
    ResourceBundle bundle = PageHandler.getBundle(session); 
    SessionManager sessionMgr = (SessionManager) request.getSession().getAttribute(WebAppConstants.SESSION_MANAGER);
    Locale uiLocale = (Locale)session.getAttribute(WebAppConstants.UILOCALE);

    String okURL = ok.getPageURL();

    String title = bundle.getString("lb_loc_profile") + "-" + bundle.getString("lb_details");

    // Data
    BasicL10nProfile locprofile = (BasicL10nProfile) request.getAttribute("locprofile");
    String lpName = locprofile.getName();
    String dispatch = bundle.getString("lb_manual");
    if (locprofile.dispatchIsAutomatic()) dispatch = bundle.getString("lb_automatic");
    String srcLocale = locprofile.getSourceLocale().getDisplayName(uiLocale);
    GlobalSightLocale[] targLocales = locprofile.getTargetLocales();
    
%>

<HTML>
<HEAD>
<META HTTP-EQUIV="content-type" CONTENT="text/html;charset=UTF-8">
<TITLE><%= title %></TITLE>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/radioButtons.js"></SCRIPT>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/utilityScripts.js"></SCRIPT>
<%@ include file="/envoy/common/warning.jspIncl" %>
<SCRIPT language="JavaScript">
var needWarning = false;
var objectName = "<%= bundle.getString("lb_loc_profile") %>";
var guideNode = "locProfiles";
var helpFile = "<%=bundle.getString("help_localization_profiles_details")%>";
</SCRIPT>

</HEAD>
<BODY LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0" MARGINWIDTH="0" MARGINHEIGHT="0"
    ONLOAD="loadGuides()">
<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>
<DIV ID="contentLayer" STYLE=" POSITION: ABSOLUTE; Z-INDEX: 9; TOP: 108px; LEFT: 20px; RIGHT: 20px;">
<TABLE CELLSPACING="0" CELLPADDING="2" BORDER="0">
<TR>
<TD COLSPAN="3" CLASS="mainHeading">&nbsp;&nbsp;<%=title%></TD>
</TR>
<TR>

<TD VALIGN="TOP">
<form name="locprofileForm" method="post" action="<%=okURL%>">

<table border="0" cellspacing="2" cellpadding="2" class="standardText">
  <tr><td></td></tr>
  <tr valign="top">
    <td>
      <table border="0" class="standardText" cellpadding="2">
        <tr>
          <td>
            <%=bundle.getString("lb_name")%>:
          </td>
          <td class="standardTextBold">
              <amb:readOnlyData value="<%= lpName %>" />
          </td>
        </tr>
        <tr>
          <td valign="top">
            <%=bundle.getString("lb_workflow_dispatch")%>:
          </td>
          <td class="standardTextBold">
              <amb:readOnlyData value="<%= dispatch %>" />
          </td>
        </tr>
        <tr>
          <td valign="top">
            <%=bundle.getString("lb_source_locale")%>:
          </td>
          <td class="standardTextBold">
              <amb:readOnlyData value="<%= srcLocale %>" />
          </td>
        </tr>
        <tr>
          <td valign="top">
            <%=bundle.getString("lb_target_locales")%>:
          </td>
          <td class="standardTextBold">
<%
              for (int i = 0; i < targLocales.length; i++)
              {
                  String locale = targLocales[i].getDisplayName(uiLocale);
%>
              <amb:readOnlyData value="<%= locale %>" /><br>
<%
              }
%>
          </td>
        </tr>
        <tr><td>&nbsp;</td></tr>
      <tr>
        <td>
          <input type="submit" value="<%=bundle.getString("lb_ok")%>">
        </td>
      </tr>
    </table>
</form>
