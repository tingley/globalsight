<%@ page
    contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="java.util.*,com.globalsight.everest.servlet.util.SessionManager,
        com.globalsight.everest.webapp.javabean.NavigationBean,
        com.globalsight.everest.webapp.WebAppConstants,
        com.globalsight.everest.foundation.SearchCriteriaParameters,
        com.globalsight.everest.webapp.pagehandler.PageHandler,
        com.globalsight.everest.webapp.pagehandler.administration.config.sgmlrulefile.SgmlRuleConstants,
        com.globalsight.everest.webapp.pagehandler.administration.config.sgmlrulefile.AttributeComparator,
        com.globalsight.everest.webapp.pagehandler.administration.config.sgmlrulefile.ElementComparator,
        com.globalsight.everest.webapp.webnavigation.LinkHelper,
        com.globalsight.util.edit.EditUtil,
        com.globalsight.util.SortUtil,
        com.globalsight.ling.sgml.sgmlrules.SgmlRule,
        com.globalsight.ling.sgml.sgmlrules.SgmlRule.Element,
        com.globalsight.ling.sgml.sgmlrules.SgmlRule.Attribute,
        java.text.MessageFormat,
        java.util.Collections,
        java.util.ResourceBundle"
    session="true"
%>
<jsp:useBean id="self" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="done" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="cancel" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="update" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<%
ResourceBundle bundle = PageHandler.getBundle(session);
SessionManager sessionMgr = (SessionManager)session.getAttribute(
  WebAppConstants.SESSION_MANAGER);
Locale uiLocale = (Locale)session.getAttribute(WebAppConstants.UILOCALE);

String selfUrl = self.getPageURL() + "&action=self";
String urlUpdate = update.getPageURL();
String urlDone = done.getPageURL();
String urlCancel = cancel.getPageURL() + "&action=cancel";


// Perform error handling, then clear out session attribute.
String errorScript = "";
String error = (String)sessionMgr.getAttribute("error");
if (error != null)
{
  errorScript = "var error = new Error();" +
    "error.message = '" + EditUtil.toJavascript(bundle.getString("msg_server_error")) + "';" +
    "error.description = '" + EditUtil.toJavascript(error) +
    "'; showError(error);";
}
sessionMgr.removeElement("error");

// Data for search
String criteria = (String) sessionMgr.getAttribute("criteria");
String BW = "";
String EW = "";
String CON = "";
if (criteria != null)
{
    if (criteria.equals(SearchCriteriaParameters.BEGINS_WITH))
        BW = "selected";  
    else if (criteria.equals(SearchCriteriaParameters.ENDS_WITH))
        EW = "selected";  
    else
        CON = "selected";  
}
String searchField = (String) sessionMgr.getAttribute("searchField");
if (searchField == null) searchField = "";

String title = bundle.getString("lb_sgml_rules");
String helper = bundle.getString("lb_sgml_rules");

%>
<HTML>
<HEAD>
<TITLE><%=title %></TITLE>
<STYLE>
LEGEND {
  font-family: Arial, Helvetica, sans-serif;
  font-size: 10pt;
  font-weight:bold;
}
</STYLE>
<SCRIPT SRC="/globalsight/includes/utilityScripts.js"></SCRIPT>
<SCRIPT SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<SCRIPT src="envoy/tm/management/protocol.js"></SCRIPT>
<%@ include file="/envoy/common/warning.jspIncl" %>
<SCRIPT>
var needWarning = false;
var objectName = "";
var guideNode = "sgmlrule";
var helpFile = "<%=bundle.getString("help_sgml_rules_edit_rules")%>";

function doSearch()
{
    //rule = getXml();
    //oForm.xmlResult.value = rule;

    oForm.action = "<%=selfUrl%>" + "&search=true";
    oForm.submit();
}

function doCancel()
{
    window.navigate("<%=urlCancel%>");
}

function doDone()
{
    //rule = getXml();
    //oForm.xmlResult.value = rule;

    oForm.action = "<%=urlDone%>" + "&action=done";
    oForm.submit();
}

function getXml()
{
    var xml = new String("<sgmlrules>");
    for (var i = 0; i < elems.length; i++)
    {
        elem = elems[i];
        ruleStr = "\n<rule>\n";
        idx = elem.indexOf("\t");
        ruleStr += "<element>";
        ruleStr += elem.substring(0, idx);
        ruleStr += "</element>";
        ruleStr += "\n";
        idx2 = elem.indexOf(",");
        ruleStr += "<extract>";
        ruleStr += elem.substring(idx+1, idx2);
        ruleStr += "</extract>";
        ruleStr += "\n";
        ruleStr += "<paired>";
        ruleStr += elem.substring(idx2+1);
        ruleStr += "</paired>";
        ruleStr += "\n";
        attr = attrs[i];   
        if (attr == "")
        {
            xml += ruleStr;
            xml += "<attribute></attribute>\n";
            xml = addTranslatable(xml, "no");
            xml = addType(xml, "text");
            xml += "</rule>\n";
        }
        else
        {
            attrList = attr.split("\t");
            for (var j = 0; j < attrList.length; j++)
            {
                xml += ruleStr;
                values = attrList[j].split(",");
                xml += "<attribute>";
                xml += values[0];
                xml += "</attribute>";
                xml += "\n";
                xml = addTranslatable(xml, values[1]);
                xml = addType(xml, values[2]);
                xml += "</rule>\n";
            }
            // add one with no attr 
            xml += ruleStr;
            xml += "<attribute></attribute>\n";
            xml = addTranslatable(xml, "no");
            xml = addType(xml, "text");
            xml += "</rule>\n";
        }
    }
    xml += "</sgmlrules>";
    return xml;
}

function addTranslatable(str, value)
{
    str += "<translatable>";
    str += value;
    str += "</translatable>";
    str += "\n";
    return str;
}

function addType(str, value)
{
    str += "<type>";
    str += value;
    str += "</type>";
    str += "\n";
    return str;
}

function doLoad()
{
  loadGuides();

  eval("<%=errorScript%>");
}
</SCRIPT>
</HEAD>
<BODY LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0" MARGINWIDTH="0"
  MARGINHEIGHT="0" onload="doLoad();">
<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>
<style type="text/css">
.list {
    border: 1px solid <%=skin.getProperty("skin.list.borderColor")%>;
}
</style>

<DIV ID="contentLayer"
 STYLE=" POSITION: ABSOLUTE; Z-INDEX: 9; TOP: 108px; LEFT: 20px; RIGHT: 20px;">

<SPAN CLASS="mainHeading"><%=bundle.getString("lb_sgml_rules")%> - <%=bundle.getString("lb_dtd")%></SPAN>
<TT><B><%
    SgmlRule rule = (SgmlRule)sessionMgr.getAttribute("rule");
    String publicId = rule.getPublicId();
    String systemId = rule.getSystemId();
    boolean dtd = true;
    if (systemId == null || systemId.equals(""))
        dtd = false;

    out.print(EditUtil.encodeHtmlEntities(publicId) + "</B>");
    if (dtd)
        out.print("(" + EditUtil.encodeHtmlEntities(systemId) + ")");
    %>
</TT>.
<P>

<TABLE CELLPADDING=0 CELLSPACING=0 BORDER=0 CLASS=standardText>
  <TR>
    <TD WIDTH=538><%= bundle.getString("lb_specify_extraction_rules") %></TD>
  </TR>
</TABLE>


<form name="oForm" method="post" action="<%=urlDone%>">

<table>
  <tr>
    <td class="standardText">
      <%=bundle.getString("lb_element")%>:
    </td>
    <td>
      <select name=nameOptions>
         <option value='<%=SearchCriteriaParameters.BEGINS_WITH%>' <%=BW%>><%= bundle.getString("lb_begins_with") %></option>
         <option value='<%=SearchCriteriaParameters.ENDS_WITH%>' <%=EW%>><%= bundle.getString("lb_ends_with") %></option>
         <option value='<%=SearchCriteriaParameters.CONTAINS%>' <%=CON%>><%= bundle.getString("lb_contains") %></option>
       </select>
       <input type="text" size="30" name="searchField" value="<%=searchField%>">
       <input type="button" name="search" value="<%=bundle.getString("lb_search")%>"  onclick="doSearch()">
    </td>
  </tr>
</table>
<br>
<!-- Begin outer table -->
<table border=0 bordercolor=red>
  <tr>
    <td style="padding-right:30px" valign="top">
      <%@ include file="elements.jspIncl" %>
    </td>
    <td valign="top">&nbsp;&nbsp;&nbsp;</td>
    <td valign="top">
      <%@ include file="attributes.jspIncl" %>
    </td>
  </tr>
</table>
<!-- End outer table -->

<DIV id="idButtons">
<!-- Cancel button does not make sense, changes are persisted immediately
<INPUT TYPE="BUTTON" VALUE="<%=bundle.getString("lb_cancel")%>"
 onclick="doCancel()">
-->
<INPUT TYPE="BUTTON" VALUE="<%=bundle.getString("lb_done")%>"
 onclick="doDone()">
</DIV>
<BR>

<input type="hidden" name="xmlResult" value="xml goes here">
<script>
if ("<%=elemRadioIndex%>" != "-1")
{
    updateEditFields("<%=elemRadioIndex%>");
    updateAttrs("<%=elemRadioIndex%>");
}
</script>
</form>

</DIV>
</BODY>
</HTML>
