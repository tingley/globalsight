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
         com.globalsight.everest.servlet.util.ServerProxy,
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

    String newURL = new1.getPageURL() + "&action=" + CurrencyConstants.CREATE;
    String editURL = edit.getPageURL() + "&action=" + CurrencyConstants.EDIT;
    String title= bundle.getString("lb_currency");
    String helperText = bundle.getString("helper_text_currency");

    String pivotCode = (String)request.getAttribute("pivot");
    String preReqData = (String)request.getAttribute("preReqData");
    
    boolean isSuperAdmin = ((Boolean) session.getAttribute(WebAppConstants.IS_SUPER_ADMIN)).booleanValue();
%>

<HTML>
<HEAD>
<TITLE><%= title %></TITLE>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/radioButtons.js"></SCRIPT>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>
<SCRIPT LANGUAGE="JavaScript">
var needWarning = false;
var objectName = "";
var guideNode = "currency";
var helpFile = "<%=bundle.getString("help_currency_main_screen")%>";

function buttonCheck(radio)
{
    if (currencyForm.edit)
    {
        if (getRadioValue(radio) == "<%=pivotCode%>")
            currencyForm.edit.disabled = true;
        else
            currencyForm.edit.disabled = false;
    }
        
}

function submitForm(button)
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
    }
    else
    {
        value = getRadioValue(currencyForm.radioBtn);
        currencyForm.action = "<%=editURL%>" + "&code=" + value;
    }
    currencyForm.submit();
    return;

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
    <table cellpadding=0 cellspacing=0 border=0 class="standardText">
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
                     emptyTableMsg="msg_no_currency" >
                <amb:column label="">
                    <input type="radio" name="radioBtn" value="<%=currency.getIsoCode()%>"
                        onclick='buttonCheck(this)'>
                </amb:column>
                <amb:column label="lb_currency" width="250"
                     sortBy="<%=CurrencyComparator.NAME%>">
                    <%= currency.getDisplayName(uiLocale)%>
                </amb:column>
                <amb:column label="lb_conversion_factor" width="250"
                     sortBy="<%=CurrencyComparator.CONVERSION%>">
                    <%= currency.getConversionFactor() %>
                </amb:column>
                <% if (isSuperAdmin) { %>
                <amb:column label="lb_company_name" width="120"
                     sortBy="<%=CurrencyComparator.ASC_COMPANY%>">
                    <%=ServerProxy.getJobHandler().getCompanyById(Long.parseLong(currency.getCompanyId())).getCompanyName()%>
                </amb:column>
                <% } %>
              </amb:table>
            </td>
         </tr>
         <tr>
    <td style="padding-top:5px" align="right">
    <amb:permission name="<%=Permission.CURRENCY_EDIT%>" >
        <INPUT TYPE="BUTTON" VALUE="<%=bundle.getString("lb_edit")%>" name="edit"
             onClick="submitForm('Edit');" disabled>
    </amb:permission>
    <amb:permission name="<%=Permission.CURRENCY_NEW%>" >
        <INPUT TYPE="BUTTON" VALUE="<%=bundle.getString("lb_new")%>..." onClick="submitForm('New');">
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
