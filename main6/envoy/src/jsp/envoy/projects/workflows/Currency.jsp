<%@ page
    contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="java.util.*,com.globalsight.everest.webapp.webnavigation.LinkHelper,
        java.util.ResourceBundle,
        com.globalsight.everest.webapp.pagehandler.PageHandler,
        com.globalsight.everest.webapp.WebAppConstants"
    session="true"
%>
<jsp:useBean id="prev" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="next" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="cancel" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="reanalyze" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<%
ResourceBundle bundle = PageHandler.getBundle(session);

String xmlCurrency = "<currencyOptions> <currency> <name> us dollar </name> <value> USD </value> </currency> <currency> <name> pound </name> <value> PND </value> </currency> <currency> <name> Rupee </name> <value> PAISE </value> </currency> </currencyOptions>";

%>
<HTML>
<HEAD>
<TITLE><%=bundle.getString("lb_currencies")%></TITLE>
<SCRIPT LANGUAGE="JavaScript" src="/globalsight/envoy/projects/workflows/Currency.js"> </SCRIPT>
</HEAD>
<BODY ID="idBody" onload="doOnLoad()">
<H2 id="idHeading"><%=bundle.getString("lb_currencies")%></H2>
<XML id="oCurrency"><%=xmlCurrency%></XML>

<FORM NAME="oForm" ACTION="/globalsight/ControlServlet?linkName=progress&pageName=WF3" METHOD="post">
<INPUT TYPE="hidden" NAME="JOB_COSTING_CURRENCY"
 VALUE="<%=bundle.getString("lb_currency_xml_goes_here")%>"></INPUT>
</FORM>

<DIV align="right">
<A Href="#" onclick="changeCurrency('PND');"><%=bundle.getString("lb_change_currency")%></A>&nbsp;
<button TABINDEX="0" onclick="changeCurrency();"><%=bundle.getString("lb_change_currency")%></button>&nbsp;
</DIV>

</BODY>
</HTML>

