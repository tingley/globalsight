<%@ page contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="java.util.*,com.globalsight.everest.webapp.javabean.NavigationBean,
         com.globalsight.everest.webapp.pagehandler.PageHandler,
         com.globalsight.everest.webapp.WebAppConstants,
         com.globalsight.util.resourcebundle.ResourceBundleConstants,
         com.globalsight.util.resourcebundle.SystemResourceBundle,
         com.globalsight.util.GlobalSightLocale,
         com.globalsight.everest.taskmanager.Task,
         java.text.MessageFormat,
         java.util.Iterator,
         java.util.Locale,
         java.util.ArrayList,
         java.util.ResourceBundle" 
         session="true"
%>
<jsp:useBean id="search" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="cancel" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<% 
    ResourceBundle bundle = PageHandler.getBundle(session);
    SessionManager sessionMgr =
      (SessionManager)session.getAttribute(WebAppConstants.SESSION_MANAGER);
    String searchUrl = search.getPageURL() + "&action=search";
    String cancelUrl = cancel.getPageURL() + "&action=cancel&" +
      WebAppConstants.TASK_STATE + "=" + Task.STATE_ACCEPTED;

    String title= bundle.getString("lb_search");

    // Button names
    String cancelButton = bundle.getString("lb_cancel");
    String searchButton = bundle.getString("lb_search");

    // Data for the page
    ArrayList targetLocales = (ArrayList)sessionMgr.getAttribute("targetLocales");
%>
<html>
<!-- This JSP is: envoy/projects/workflows/searchContent.jsp -->
<head>
<meta http-equiv="content-type" content="text/html;charset=UTF-8">
<title><%= title %></title>
<script SRC="/globalsight/includes/setStyleSheet.js"></script>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>
<script>
var needWarning = false;
var objectName = "";
var guideNode = "myJobs";
var helpFile = "<%=bundle.getString("help_workflow_search")%>";

function submitForm(formAction)
{
    if (searchForm.queryString.value == "")
    {
        alert("<%=bundle.getString("jsmsg_search_string")%>");
        searchForm.queryString.focus();
        return false;
    }

    selectedLocales = getLocales();
    if (selectedLocales == "")
    {
        alert("<%=bundle.getString("jsmsg_search_locales")%>");
        searchForm.locales.focus();
        return false;
    }

    searchForm.action = "<%=searchUrl%>&selectedLocales=" + selectedLocales;

    return true;
}

function doCancel()
{
    //window.navigate("<%=cancelUrl%>");
    window.location = "<%=cancelUrl%>";
}

function getLocales()
{
    var obj = searchForm.locales;
    var selectedLocales = "";
    for (var i = 0; i < obj.length; i++)
    {
        if (obj.options[i].selected)
        {
            selectedLocales += obj.options[i].value + " ";
        }
    }
    return selectedLocales;
}

function setIsCaseSensitive()
{
    if (searchForm.caseSensitive.checked)
        searchForm.isCaseSensitive.value = "true";
    else
        searchForm.isCaseSensitive.value = "false";
}

function doOnload()
{
  loadGuides();

  searchForm.locales.focus();
}
</script>
</head>
<body leftmargin="0" rightmargin="0" topmargin="0" marginwidth="0"
 marginheight="0" onload="doOnload()">
<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>
<DIV ID="contentLayer"
 STYLE=" POSITION: ABSOLUTE; Z-INDEX: 9; TOP: 108; LEFT: 20px; RIGHT: 20px;">
<span class="mainHeading"><%=title%></span>
<div class="standardText"><%=bundle.getString("msg_search_jobs")%></div>
<form name="searchForm" method="post" action="<%=searchUrl%>"
 onsubmit="return submitForm()">
<table border="0" cellpadding="0" cellspacing="0" width="50%">
  <tr>
    <td class="standardText">
      <%=bundle.getString("lb_target_locale")/*lb_target_locales*/%>:
      <span class="asterisk">*</span>:
    </td>
  </tr>
  <tr>
    <td>
      <select name="locales" multiple class="standardText" size=15>
<%
      if (targetLocales != null)
      {
        for (int i = 0; i < targetLocales.size(); i++)
        {
          GlobalSightLocale locale = (GlobalSightLocale)targetLocales.get(i);
%>
	<option value="<%=locale.getId()%>"><%=locale.getDisplayName()%></option>
<%
        }
      }
%>
      </select>
    </td>
  </tr>
  <tr>
    <td class="standardText" style="padding-top:5px">
      <%= bundle.getString("lb_search")%>
      <span class="asterisk">*</span>:
      <input type="text" name="queryString" size="30">
    </td>
  </tr>
  <tr>
    <td class="standardText" style="padding-top:5px">
      <input type="checkbox" name="caseSensitive" onchange="setIsCaseSensitive()">
      <%= bundle.getString("lb_tm_search_match_case")%>
      <input type="hidden" name="isCaseSensitive">
    </td>
  </tr>
  <tr>
    <td style="padding-top:10px">
      <input type="button" name="<%=cancelButton %>" value="<%=cancelButton %>"
      onclick="doCancel()">
      <input type="submit" name="<%=searchButton %>" value="<%=searchButton %>">
    </td>
  </tr>
</table>
</form>
</body>
</html>
