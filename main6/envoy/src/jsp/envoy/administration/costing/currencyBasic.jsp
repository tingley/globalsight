<%@ page contentType="text/html; charset=UTF-8"
         errorPage="/envoy/common/activityError.jsp"
         import="java.util.*, com.globalsight.everest.servlet.util.SessionManager,
                  com.globalsight.everest.webapp.WebAppConstants,
                  com.globalsight.everest.webapp.javabean.NavigationBean,
                  com.globalsight.everest.webapp.pagehandler.PageHandler,
                  com.globalsight.everest.webapp.pagehandler.administration.costing.currency.CurrencyConstants,
                  com.globalsight.everest.costing.Currency,
                  com.globalsight.everest.costing.IsoCurrency,
                  com.globalsight.everest.webapp.webnavigation.LinkHelper,
                  com.globalsight.everest.servlet.util.ServerProxy,
                  com.globalsight.everest.servlet.EnvoyServletException,
                  com.globalsight.util.GeneralException,
                  java.text.MessageFormat,
                  java.util.ResourceBundle,
                  java.util.Locale,
                  java.util.Vector"
          session="true"
%>
<jsp:useBean id="cancel" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<jsp:useBean id="save" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<%
    ResourceBundle bundle = PageHandler.getBundle(session);
    SessionManager sessionMgr = (SessionManager)session.getAttribute(WebAppConstants.SESSION_MANAGER);
    Locale uiLocale = (Locale)session.getAttribute(WebAppConstants.UILOCALE);
    
    // UI fields

    // Labels, etc
    String lbcancel = bundle.getString("lb_cancel");
    String lbsave = bundle.getString("lb_save");

    boolean edit = false;
    String saveURL = save.getPageURL();
    String title = null;
    if (request.getAttribute("edit") != null)
    {
        edit = true;
        saveURL +=  "&action=" + CurrencyConstants.EDIT;
        title = bundle.getString("lb_edit") + " " + bundle.getString("lb_currency");
    }
    else
    {
        saveURL +=  "&action=" + CurrencyConstants.CREATE;
        title = bundle.getString("lb_new") + " " + bundle.getString("lb_currency");
    }
    
    String cancelURL = cancel.getPageURL() + "&action=" + CurrencyConstants.CANCEL;

    // Data
    String pivot = (String)request.getAttribute("pivot");
    Vector isos = (Vector)request.getAttribute(CurrencyConstants.ISO_CURRENCY);
    Currency currency = (Currency)sessionMgr.getAttribute(CurrencyConstants.CURRENCY);
    String currencyName = "";
    float conversion = 0;
    if (currency != null)
    {
        currencyName = currency.getDisplayName(uiLocale);
        conversion = currency.getConversionFactor();
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
<%@ include file="/envoy/common/warning.jspIncl" %>


<script language="JavaScript">
var needWarning = true;
var objectName = "<%=bundle.getString("lb_currency")%>";
var guideNode="currency";
var helpFile = "<%=bundle.getString("help_currency_create_modify")%>"; 

function submitForm(formAction)
{
    if (formAction == "cancel")
    {
        currencyForm.action = "<%=cancelURL%>";
        currencyForm.submit();
    }
    if (formAction == "save")
    {
        if (confirmForm()) 
        {
            currencyForm.action = "<%=saveURL%>";
            currencyForm.submit();
        }
    }
}

//
// Check required fields.
//
function confirmForm()
{
    if ("<%=edit%>" != "true" && currencyForm.displayCurr.selectedIndex == 0)
    {
        alert("<%=bundle.getString("jsmsg_select_display_currency")%>");
        return false;
    }
    if (currencyForm.conversion.value == "")
    {
        alert("<%=bundle.getString("lb_conversion_factor")%>" + "<%= bundle.getString("jsmsg_numeric") %>");
        currencyForm.conversion.value = "0.0"
        return false;
    }
    if (!isFloat(currencyForm.conversion.value))
    {
        alert("<%=bundle.getString("lb_conversion_factor")%>" + "<%= bundle.getString("jsmsg_numeric") %>");
        return false;
    }
    
    if (isZero(currencyForm.conversion.value))
    {
        alert("<%=bundle.getString("jsmsg_cannot_be_zero")%>");
        return false;
    }
    
    return true;
}

function isFloat(field)
{
    var j = 0;
    for (var i = 0; i < field.length; i++)
    {
        if ((field.charAt(i) < "0" || field.charAt(i) > "9") && field.charAt(i) != ".")
        {
            return false;
        }
        if  (field.charAt(i) == ".") {
            if (j ++ > 0) {
                return false;
            }    
        } 
    }
    return true;
}

function isZero(field)
{   
    return parseFloat(field) == 0;
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
    <br>
    <br>

<form name="currencyForm" method="post" action="">

<table border="0" cellspacing="2" cellpadding="2" class="standardText">
  <tr><td></td></tr>
  <tr valign="top">
    <td>
      <table border="0" class="standardText" cellpadding="2">
        <tr>
          <td>
            <%=bundle.getString("lb_pivot_currency")%><span class="asterisk">*</span>:
          </td>
          <td>
                <%=pivot%>
          </td>
        </tr>
        <tr>
          <td valign="center">
            <%=bundle.getString("lb_display_currency")%><span class="asterisk">*</span>:
          </td>
          <td>
            <% if (edit) { 
                out.println(currencyName);
            } else {
                out.println("<select name=displayCurr>");
                out.println("<option value=-1>" + bundle.getString("lb_choose") + 
                            "</option>");
                for (int i = 0; i < isos.size(); i++)
                {
                    IsoCurrency iso = (IsoCurrency) isos.get(i);
                        out.println("<option value=" + iso.getCode() + ">" +
                                  iso.getDisplayName(uiLocale) + "</option>");
                }
                out.println("</select>");
            } %>
          </td>
        </tr>
        <tr>
          <td valign="top">
            <%=bundle.getString("lb_conversion_factor")%><span class="asterisk">*</span>:
          </td>
          <td>
            <input type="text" name="conversion" value="<%=conversion%>">
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
  </td>
</tr>
</table>
</form>

