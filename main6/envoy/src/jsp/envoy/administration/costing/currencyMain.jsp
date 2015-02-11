<%@ taglib uri="/WEB-INF/tlds/globalsight.tld" prefix="amb" %>
<%@ page contentType="text/html; charset=UTF-8"
         errorPage="/envoy/common/error.jsp"
         import="java.util.*,com.globalsight.everest.webapp.javabean.NavigationBean,
         com.globalsight.util.resourcebundle.ResourceBundleConstants,
         com.globalsight.util.resourcebundle.SystemResourceBundle,
         com.globalsight.everest.webapp.pagehandler.administration.costing.currency.CurrencyConstants,
         com.globalsight.everest.util.comparator.CurrencyComparator,
         com.globalsight.everest.webapp.pagehandler.PageHandler,
         com.globalsight.everest.permission.Permission,
         com.globalsight.everest.servlet.util.SessionManager,
         com.globalsight.everest.company.CompanyWrapper,
         java.util.ArrayList, java.util.Locale, java.util.ResourceBundle" 
         session="true" %>

<jsp:useBean id="new1" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="edit" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="self" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="currencies" scope="request" class="java.util.ArrayList" />

<% 
    ResourceBundle bundle = PageHandler.getBundle(session);
    SessionManager sessionMgr = (SessionManager)session.getAttribute(WebAppConstants.SESSION_MANAGER);
    Locale uiLocale = (Locale)session.getAttribute(WebAppConstants.UILOCALE);
    String selfURL = self.getPageURL();
    String newURL = new1.getPageURL() + "&action=" + CurrencyConstants.CREATE;
    String editURL = edit.getPageURL() + "&action=" + CurrencyConstants.EDIT;
    String title= bundle.getString("lb_currency");
    String helperText = bundle.getString("helper_text_currency");

    String pivotCode = (String)request.getAttribute("pivot");
    String preReqData = (String)request.getAttribute("preReqData");
    
    boolean isSuperAdmin = ((Boolean) session.getAttribute(WebAppConstants.IS_SUPER_ADMIN)).booleanValue();
    String companyNameFilterValue = (String) sessionMgr.getAttribute(CurrencyConstants.FILTER_CURRENCY_COMPANY);
    if (companyNameFilterValue == null || companyNameFilterValue.trim().length() == 0)
    {
        companyNameFilterValue = "";
    }

    String currencyNameFilterValue = (String) sessionMgr.getAttribute(CurrencyConstants.FILTER_CURRENCY_NAME);
    if (currencyNameFilterValue == null || currencyNameFilterValue.trim().length() == 0)
    {
        currencyNameFilterValue = "";
    }

    PermissionSet userPermissions = (PermissionSet) session.getAttribute(WebAppConstants.PERMISSIONS);
%>

<HTML>
<HEAD>
<TITLE><%= title %></TITLE>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/radioButtons.js"></SCRIPT>
<SCRIPT SRC="/globalsight/jquery/jquery-1.6.4.min.js"></SCRIPT>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>
<SCRIPT LANGUAGE="JavaScript">
var needWarning = false;
var objectName = "";
var guideNode = "currency";
var helpFile = "<%=bundle.getString("help_currency_main_screen")%>";

function handleSelectAll()
{
    var ch = $("#selectAll").attr("checked");
    if (ch == "checked")
    {
        $("[name='checkboxBtn']").attr("checked", true);
    }
    else
    {
        $("[name='checkboxBtn']").attr("checked", false);
    }
}

function filterItems(e)
{
    e = e ? e : window.event;
    var keyCode = e.which ? e.which : e.keyCode;
    if (keyCode == 13)
    {
        currencyForm.action = "<%=selfURL%>";
        currencyForm.submit();
    }
}

function edit(currencyId)
{
        currencyForm.action = "<%=editURL%>" + "&currencyId=" + currencyId;
        currencyForm.submit();       
}

function create(button)
{
    if (button == "New")
    {
<%
        if (preReqData != null)
        {
%>
            alert("<%=preReqData%>");
            return;
<%
        }
%>
        currencyForm.action = "<%=newURL%>";
        currencyForm.submit();
    }
}
</SCRIPT>
</HEAD>
<BODY LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0" MARGINWIDTH="0" MARGINHEIGHT="0"
    ONLOAD="loadGuides()">
<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>
<DIV ID="contentLayer" STYLE=" POSITION: ABSOLUTE; Z-INDEX: 9; TOP: 108px; LEFT: 20px; RIGHT: 20px; RIGHT: 20px;">
<amb:header title="<%=title%>" helperText="<%=helperText%>" />
<form name="currencyForm" method="post">
    <table cellpadding=0 cellspacing=0 border=0 width="100%" class="standardText">
        <tr valign="top">
          <td align="right">
            <amb:tableNav bean="currencies" key="<%=CurrencyConstants.CURRENCY_KEY%>"
                 pageUrl="self" />
          </td>
        </tr>
        <tr>
          <td>
              <amb:table bean="currencies" id="currency"
                     key="<%=CurrencyConstants.CURRENCY_KEY%>"
                     dataClass="com.globalsight.everest.costing.Currency" pageUrl="self"
                     emptyTableMsg="msg_no_currency" hasFilter="true" >
                <amb:column label="checkbox" width="20px">
                    <input type="checkbox" name="checkboxBtn" value="<%=currency.getIsoCode()%>" />
                </amb:column>
                <amb:column label="lb_currency" width="10%"
                     sortBy="<%=CurrencyComparator.NAME%>" filter="<%=CurrencyConstants.FILTER_CURRENCY_NAME%>" filterValue="<%=currencyNameFilterValue%>" >
                <%
                	if (userPermissions.getPermissionFor(Permission.CURRENCY_EDIT) 
                					&& !currency.getDisplayName(uiLocale).equalsIgnoreCase("US Dollar (USD)")) {
                %>
                    <a href='javascript:void(0);' title="<%=bundle.getString("lb_edit")%>" onclick="edit('<%=currency.getId()%>')"><%=currency.getDisplayName(uiLocale)%></a>
                <%  } else { %>
                    <%=currency.getDisplayName(uiLocale)%>
                <% } %>
                </amb:column>
                <amb:column label="lb_conversion_factor" 
                     sortBy="<%=CurrencyComparator.CONVERSION%>">
                    <%= currency.getConversionFactor() %>
                </amb:column>
                <% if (isSuperAdmin) { %>
                <amb:column label="lb_company_name"
                     sortBy="<%=CurrencyComparator.ASC_COMPANY%>" filter="<%=CurrencyConstants.FILTER_CURRENCY_COMPANY%>" filterValue="<%=companyNameFilterValue%>" >
                    <%=CompanyWrapper.getCompanyNameById(currency.getCompanyId())%>
                </amb:column>
                <% } %>
              </amb:table>
            </td>
         </tr>
         <tr valign="top">
          <td align="right">
            <amb:tableNav bean="currencies" key="<%=CurrencyConstants.CURRENCY_KEY%>"
                pageUrl="self"  scope="10,20,50,All" showTotalCount="false"/>
          </td>
        </tr>
         <tr>
    <td style="padding-top:5px" align="left">
    <amb:permission name="<%=Permission.CURRENCY_NEW%>" >
        <INPUT TYPE="BUTTON" VALUE="<%=bundle.getString("lb_new")%>..." onClick="create('New');">
    </amb:permission>
    </td>
</TR>
</TABLE>
</TD>
</TR>
</TABLE>
</FORM>
</BODY>
</HTML>
