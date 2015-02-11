<%@ page contentType="text/html; charset=UTF-8"
         errorPage="/envoy/common/activityError.jsp"
         import="java.util.*, com.globalsight.everest.servlet.util.SessionManager,
                  com.globalsight.everest.costing.Rate,
                  com.globalsight.everest.webapp.WebAppConstants,
                  com.globalsight.everest.webapp.javabean.NavigationBean,
                  com.globalsight.everest.webapp.pagehandler.PageHandler,
                  com.globalsight.everest.workflow.Activity,
                  com.globalsight.everest.foundation.SearchCriteriaParameters,
                  com.globalsight.everest.vendormanagement.Vendor,
                  com.globalsight.everest.vendormanagement.VendorSearchParameters,
                  com.globalsight.util.resourcebundle.ResourceBundleConstants,
                  com.globalsight.util.resourcebundle.SystemResourceBundle,
                  com.globalsight.everest.webapp.webnavigation.LinkHelper,
                  com.globalsight.everest.servlet.util.ServerProxy,
                  com.globalsight.everest.servlet.EnvoyServletException,
                  com.globalsight.everest.util.system.SystemConfigParamNames,
                  com.globalsight.everest.util.system.SystemConfiguration,
                  com.globalsight.util.GeneralException,
                  com.globalsight.util.GlobalSightLocale,
                  java.text.MessageFormat,
                  java.util.Locale,
                  java.util.ResourceBundle"
          session="true"
%>
<jsp:useBean id="cancel" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="search" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<%
    ResourceBundle bundle = PageHandler.getBundle(session);
    SessionManager sessionMgr = (SessionManager)session.getAttribute(WebAppConstants.SESSION_MANAGER);
    Locale uiLocale = (Locale)session.getAttribute(WebAppConstants.UILOCALE);

    // Labels, etc
    String title= bundle.getString("lb_search_vendors");
    String lbcancel = bundle.getString("lb_cancel");
    String lbsearch = bundle.getString("lb_search");

    String cancelURL = cancel.getPageURL() + "&action=cancel";
    String searchURL = search.getPageURL() + "&action=search";

    // Data
    List srcLocales = (List)request.getAttribute("srcLocales");
    List targLocales = (List)request.getAttribute("targLocales");
    Integer[] costingRates = (Integer[])request.getAttribute("costingRates");
    List activities = (List)request.getAttribute("activities");
    String customPage = (String)request.getAttribute("customPage");

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
var guideNode="vendors";
var helpFile = "<%=bundle.getString("help_vendors_search")%>";

function submitForm(formAction)
{
    if (formAction == "cancel")
    {
       if (confirmJump())
       {
           searchForm.action = "<%=cancelURL%>";
       }
       else
       {
          return false;
       }
    }
    else if (formAction == "search")
    {
        if (validate() == false)
        {
            alert("<%=bundle.getString("jsmsg_cost_integer")%>");
            return;
        }
        searchForm.action = "<%=searchURL%>";
    }
    searchForm.submit();
}

function validate()
{
    if (searchForm.cost.value != "" && isNaN(parseInt(searchForm.cost.value)))
        return false;
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
            <select name="nameTypeField">
                <option value='<%=VendorSearchParameters.USER_ID%>'><%= bundle.getString("lb_user_name") %></option>
                <option value='<%=VendorSearchParameters.VENDOR_FIRST_NAME%>'><%= bundle.getString("lb_first_name") %></option>
                <option value='<%=VendorSearchParameters.VENDOR_LAST_NAME%>'><%= bundle.getString("lb_last_name") %></option>
            </select>
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
            <%=bundle.getString("lb_company_name")%>:
          </td>
          <td class="standardText">
            <select name="companyOptions">
                <option value='<%=SearchCriteriaParameters.BEGINS_WITH%>'><%= bundle.getString("lb_begins_with") %></option>
                <option value='<%=SearchCriteriaParameters.ENDS_WITH%>'><%= bundle.getString("lb_ends_with") %></option>
                <option value='<%=SearchCriteriaParameters.CONTAINS%>'><%= bundle.getString("lb_contains") %></option>
            </select>
            <input type="text" size="30" name="company">
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
                    long lpId = locale.getId();
                    out.println("<option value=" + lpId + ">" + disp + "</option>");
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
                    long lpId = locale.getId();
                    out.println("<option value=" + lpId + ">" + disp + "</option>");
                }
            }
%>
            </select>
          </td>
        </tr>
        <tr>
          <td class="standardText">
            <%=bundle.getString("lb_rate")%>:
          </td>
          <td>
            <select name="costingRate" class="standardText">
<%
            if (costingRates != null)
            {
                for (int i =0; i < costingRates.length; i++)
                {
                    String disp = bundle.getString("lb_rate_type_"+costingRates[i]);
                    out.println("<option value=" + costingRates[i] + ">" + disp + "</option>");
                }
            }
%>
            </select>
            <select name="costingOptions">
                <option value='<%=SearchCriteriaParameters.LESS_THAN%>'><%= bundle.getString("lb_less_than") %></option>
                <option value='<%=SearchCriteriaParameters.GREATER_THAN%>'><%= bundle.getString("lb_greater_than") %></option>
                <option value='<%=SearchCriteriaParameters.EQUALS%>'><%= bundle.getString("lb_equal_to") %></option>
            </select>
            <input type="text" name="cost">
          </td>
        </tr>
        <tr>
          <td class="standardText">
            <%=bundle.getString("lb_activity_type")%>:
          </td>
          <td>
            <select name="activities">
                <option value='-1'></option>
<%
            if (activities != null)
            {
                for (int i=0; i < activities.size(); i++)
                {
                    Activity activity = (Activity)activities.get(i);
                    out.println("<option value=" + activity.getId() + ">" + activity.getActivityName() + "</option>");
                }
            }
%>
            </select>
          </td>
        </tr>
<%
        if (customPage != null && !customPage.equals(""))
        {
%>
            <tr>
              <td class="standardText">
                <%=customPage%>&nbsp;<%=bundle.getString("lb_keywords")%>:
              </td>
              <td>
                <input type="text" name="keywords" size="50">(<%=bundle.getString("lb_comma_delimited")%>)
              </td>
            </tr>
<%
        }
%>
          <tr>
              <td class="standardText">
                <input type="checkbox" name="caseSensitive">
                <%=bundle.getString("lb_tm_search_match_case")%>
            </td>
          </tr>
          <tr><td>&nbsp;</td></tr>
      <tr>
        <td>
          <input type="button" name="<%=lbcancel%>" value="<%=lbcancel%>"
            onclick="submitForm('cancel')">
          <input type="button" name="<%=lbsearch%>" value="<%=lbsearch%>"
            onclick="submitForm('search')">
        </td>
      </tr>
    </table>
</form>

