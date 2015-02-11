<%@ page contentType="text/html; charset=UTF-8"
         errorPage="/envoy/common/activityError.jsp"
         import="java.util.*, com.globalsight.everest.servlet.util.SessionManager,
                  com.globalsight.everest.permission.PermissionManager,
                  com.globalsight.everest.permission.PermissionGroup,                  
                  com.globalsight.everest.webapp.WebAppConstants,
                  com.globalsight.everest.webapp.javabean.NavigationBean,
                  com.globalsight.everest.webapp.pagehandler.PageHandler,
                  com.globalsight.everest.webapp.pagehandler.administration.users.UserHandlerHelper,
                  com.globalsight.everest.webapp.pagehandler.administration.users.UserSearchParams,
                  com.globalsight.util.resourcebundle.ResourceBundleConstants,
                  com.globalsight.util.resourcebundle.SystemResourceBundle,
                  com.globalsight.everest.foundation.User,
                  com.globalsight.everest.webapp.webnavigation.LinkHelper,
                  com.globalsight.everest.servlet.util.ServerProxy,
                  com.globalsight.everest.servlet.EnvoyServletException,
                  com.globalsight.everest.util.comparator.GlobalSightLocaleComparator,
                  com.globalsight.everest.util.comparator.StringComparator,
                  com.globalsight.everest.util.system.SystemConfigParamNames,
                  com.globalsight.everest.util.system.SystemConfiguration,
                  com.globalsight.util.GeneralException,
                  com.globalsight.util.GlobalSightLocale,
                  com.globalsight.util.SortUtil,
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
    String userName = (String)session.getAttribute(WebAppConstants.USER_NAME);
    User user = UserHandlerHelper.getUser(userName);
    String userId = user.getUserId();

    // Labels, etc
    String title= bundle.getString("lb_users") + " - " + bundle.getString("lb_search");
    String lbsearch = bundle.getString("lb_search");
    String lbcancel = bundle.getString("lb_cancel");
    
    String searchUrl = search.getPageURL() + "&action=search";
    String cancelUrl = cancel.getPageURL() + "&action=cancel";

    // Data
    List srcLocales = (List)request.getAttribute("srcLocales");
    List targLocales = (List)request.getAttribute("targLocales");
    String[] levels = (String[])request.getAttribute("levels");
        

%>
<html>
<!-- This JSP is envoy/administration/users/advSearch.jsp -->
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
var guideNode="users";
var helpFile = "<%=bundle.getString("help_user_search")%>";

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
            <%=bundle.getString("lb_user_name")%>:
          </td>
          <td class="standardText">
            <select name="nameTypeOptions">
                <option value='<%=UserSearchParams.USERNAME_TYPE%>'><%= bundle.getString("lb_user_name") %></option>
                <option value='<%=UserSearchParams.FIRSTNAME_TYPE%>'><%= bundle.getString("lb_first_name") %></option>
                <option value='<%=UserSearchParams.LASTNAME_TYPE%>'><%= bundle.getString("lb_last_name") %></option>
            </select>
            <select name="nameOptions">
                <option value='<%=UserSearchParams.BEGINSWITH_FILTER%>'><%= bundle.getString("lb_begins_with") %></option>
                <option value='<%=UserSearchParams.ENDSWITH_FILTER%>'><%= bundle.getString("lb_ends_with") %></option>
                <option value='<%=UserSearchParams.CONTAINS_FILTER%>'><%= bundle.getString("lb_contains") %></option>
            </select>
            <input type="text" size="30" name="nameField">
          </td>
        </tr>
        <tr>
          <td class="standardText">
            <%=bundle.getString("lb_source_locale")%>:
          </td>
          <td>
            <select name="srcLocale" class="standardText">
            <option value=""></option>
                <%
            if (srcLocales != null)
            {
                SortUtil.sort(srcLocales, new GlobalSightLocaleComparator(Locale.getDefault()));
                for (int i = 0; i < srcLocales.size();  i++)
                {
                    GlobalSightLocale locale = (GlobalSightLocale)srcLocales.get(i);
                    String disp = locale.getDisplayName(uiLocale);
                    out.println("<option value=" + locale.toString() + ">" + disp + "</option>");
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
            <option value=""></option>
              <%
              if (targLocales != null)
              {
                SortUtil.sort(targLocales, new GlobalSightLocaleComparator(Locale.getDefault()));
                for (int i = 0; i < targLocales.size();  i++)
                {
                  GlobalSightLocale locale = (GlobalSightLocale)targLocales.get(i);
                  String disp = locale.getDisplayName(uiLocale);
                  out.println("<option value=" + locale.toString() + ">" + disp + "</option>");
                }
              }
              %>
             </select>
          </td>
        </tr>
        <tr>
          <td class="standardText">
          <%=bundle.getString("lb_permission_group")%>
          </td>
          <td>
            <select name="permissionGroup" class="standardText">
            <option value=""></option>
            <%
              Collection permGroups = Permission.getPermissionManager().getAllPermissionGroups();
              Iterator iter = permGroups.iterator();
              ArrayList<String> perms = new ArrayList<String>();
              while (iter.hasNext())
              {
                  perms.add(((PermissionGroup) iter.next()).getName());
              }
              SortUtil.sort(perms, new StringComparator(Locale.getDefault()));
              iter = perms.iterator();
              while (iter.hasNext())
              {
                  String pg = (String)iter.next();
                  %>
                  <OPTION VALUE="<%=pg%>"><%=pg%></OPTION>
                  <%
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

