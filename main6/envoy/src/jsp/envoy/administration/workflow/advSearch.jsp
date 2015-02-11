<%@ page contentType="text/html; charset=UTF-8"
         errorPage="/envoy/common/activityError.jsp"
         import=" com.globalsight.everest.servlet.util.SessionManager,
                  com.globalsight.everest.webapp.WebAppConstants,
                  com.globalsight.everest.webapp.javabean.NavigationBean,
                  com.globalsight.everest.webapp.pagehandler.PageHandler,
                  com.globalsight.everest.webapp.pagehandler.administration.workflow.WorkflowTemplateConstants,
                  com.globalsight.everest.projecthandler.ProjectInfo,
                  com.globalsight.util.resourcebundle.ResourceBundleConstants,
                  com.globalsight.util.resourcebundle.SystemResourceBundle,
                  com.globalsight.everest.webapp.webnavigation.LinkHelper,
                  com.globalsight.everest.servlet.util.ServerProxy,
                  com.globalsight.everest.servlet.EnvoyServletException,
                  com.globalsight.everest.util.system.SystemConfigParamNames,
                  com.globalsight.everest.util.system.SystemConfiguration,
                  com.globalsight.everest.foundation.SearchCriteriaParameters,
                  com.globalsight.util.GeneralException,
                  com.globalsight.util.GlobalSightLocale,
                  java.text.MessageFormat,
                  java.util.Locale,
                  java.util.ResourceBundle"
          session="true"
%>
<jsp:useBean id="search" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="cancel" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<%
    ResourceBundle bundle = PageHandler.getBundle(session);
    SessionManager sessionMgr = (SessionManager)session.getAttribute(WebAppConstants.SESSION_MANAGER);
    Locale uiLocale = (Locale)session.getAttribute(WebAppConstants.UILOCALE);

    // Labels, etc
    String title= bundle.getString("lb_workflows") + " - " + bundle.getString("lb_search");
    String lbsearch = bundle.getString("lb_search");
    String lbcancel = bundle.getString("lb_cancel");
    
    String searchUrl = search.getPageURL() + "&action=" + WorkflowTemplateConstants.ADV_SEARCH_ACTION;
    String cancelUrl = cancel.getPageURL() + "&action=" + WorkflowTemplateConstants.CANCEL_ACTION;

    // Data
    List srcLocales = (List)request.getAttribute(WorkflowTemplateConstants.SOURCE_LOCALES);
    List targLocales = (List)request.getAttribute(WorkflowTemplateConstants.TARGET_LOCALES);
    List projectInfos = (List)request.getAttribute(WorkflowTemplateConstants.PROJECTS);

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
var objectName = "";
var guideNode="workflows";
var helpFile = "";

function submitForm(formAction)
{
    if (formAction == "search")
        searchForm.action = "<%=searchUrl%>";
    else
        searchForm.action = "<%=cancelUrl%>";
    searchForm.submit();
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
     <%
         String noresults = (String)request.getAttribute("noresults");
         if (noresults != null)
         {
            out.println("<div style='color:red'>");
            out.println(noresults);
            out.println("</div>");
         }
     %>
    <p>

<form name="searchForm" method="post" action="">

<table border="0" cellspacing="2" cellpadding="2" class="standardText">
  <tr><td></td></tr>
  <tr valign="top">
    <td>
      <table border="0" class="standardText" cellpadding="2">
        <tr>
          <td class="standardText">
            <%=bundle.getString("lb_name")%>:
          </td>
          <td class="standardText">
            <select name="nameOptions">
                <option value='<%=SearchCriteriaParameters.BEGINS_WITH%>'><%= bundle.getString("lb_begins_with") %></option>
                <option value='<%=SearchCriteriaParameters.ENDS_WITH%>'><%= bundle.getString("lb_ends_with") %></option>
                <option value='<%=SearchCriteriaParameters.CONTAINS%>'><%= bundle.getString("lb_contains") %></option>
            </select>
            <input type="text" size="30" name="nameField">
          </td>
        </tr>
        <tr>
          <td class="standardText">
            <%=bundle.getString("lb_project")%>:
          </td>
          <td>
            <select name="project" class="standardText">
            <option value=""></option>
                <%
            if (projectInfos != null)
            {
                for (int i = 0; i < projectInfos.size();  i++)
                {
                    ProjectInfo p = (ProjectInfo)projectInfos.get(i);
                    out.println("<option value=" + p.getProjectId() + ">" +
                             p.getName() + "</option>");
                }
            }
            %>
            </select>
          </td>
        </tr>
        <tr>
          <td class="standardText">
            <%=bundle.getString("lb_source_locale")%>:
          </td>
          <td>
            <select name="srcLocale" class="standardText">
            <option value="-1"></option>
                <%
            if (srcLocales != null)
            {
                for (int i = 0; i < srcLocales.size();  i++)
                {
                    GlobalSightLocale locale = (GlobalSightLocale)srcLocales.get(i);
                    String disp = locale.getDisplayName(uiLocale);
                    out.println("<option value=" + locale.getId() + ">" + disp + "</option>");
                }
            }
            %>
            </select>
          </td>
        </tr>
        <tr>
          <td class="standardText">
            <%=bundle.getString("lb_target_locale")%>:
          </td>
          <td>
            <select name="targLocale" class="standardText">
            <option value="-1"></option>
              <%
              if (targLocales != null)
              {
                for (int i = 0; i < targLocales.size();  i++)
                {
                  GlobalSightLocale locale = (GlobalSightLocale)targLocales.get(i);
                  String disp = locale.getDisplayName(uiLocale);
                  out.println("<option value=" + locale.getId() + ">" + disp + "</option>");
                }
              }
              %>
             </select>
          </td>
        </tr>
        <tr><td>&nbsp;</td></tr>
        <tr>
          <td>
            <input type="button" name="search" value="<%=lbsearch%>"
            onclick="submitForm('search')">
            <input type="button" name="cancel" value="<%=lbcancel%>"
            onclick="submitForm('cancel')">
          </td>
        </tr>
</table>
</form>

