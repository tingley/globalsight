<%@ page
    contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="java.util.*,com.globalsight.everest.webapp.webnavigation.LinkHelper,
        java.util.ResourceBundle,
        com.globalsight.util.edit.EditUtil,
        com.globalsight.everest.aligner.AlignerPackageOptions,
        com.globalsight.everest.servlet.util.SessionManager,
        com.globalsight.everest.webapp.pagehandler.PageHandler,
        com.globalsight.everest.webapp.WebAppConstants"
    session="true"
%>
<jsp:useBean id="ok" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="previous" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="cancel" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<%!
public String escapeForBackslach(String str) {
		return str.replace("\\", "\\\\");
}
%>
<%
ResourceBundle bundle = PageHandler.getBundle(session);
SessionManager sessionMgr = (SessionManager)session.getAttribute(
  WebAppConstants.SESSION_MANAGER);

AlignerPackageOptions gapOptions = (AlignerPackageOptions)
  sessionMgr.getAttribute(WebAppConstants.GAP_OPTIONS);
String xmlStr = gapOptions.getXml();
String xmlStrForJS = escapeForBackslach(xmlStr);


String urlOk = ok.getPageURL();
String urlPrevious = previous.getPageURL();
String urlCancel = cancel.getPageURL();

String lb_title = bundle.getString("lb_aligner_para");
String lb_helptext = bundle.getString("helper_text_aligner_package_create3");
%>
<HTML>
<HEAD>
<!-- JSP file: createPackage3.jsp -->
<TITLE><%=lb_title%></TITLE>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/jquery/jquery-1.6.4.min.js"></SCRIPT>
<SCRIPT src="/globalsight/includes/library.js"></SCRIPT>
<SCRIPT SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>
<%@ include file="/includes/compatibility.jspIncl" %>
<SCRIPT>
var needWarning = true;
var objectName = "";
var guideNode = "aligner";
var helpFile = "<%=bundle.getString("help_align_createPackage3")%>";
var xmlStrForJS = "<%=xmlStrForJS%>";

function Result(message, description, element, dom)
{
    this.message = message;
    this.description = description;
    this.element = element;
    this.dom = dom;
}

function doCancel()
{
   if (confirmJump())
   {
      window.navigate("<%=urlCancel%>");
   }
   else
   {
      return false;
   }
}

function doPrevious()
{
    var result = buildOptions();

    if (result.message != null && result.message != "")
    {
        alert(result.message);
        result.element.focus();
    }
    else
    {
        url = "<%=urlPrevious%>";

        oForm.action = url;
        oForm.<%=WebAppConstants.GAP_ACTION%>.value =
          '<%=WebAppConstants.GAP_ACTION_SELECTFILES%>';
        
        oForm.gapoptions.value = getDomString(result.dom);
        oForm.submit();
    }
}

function doAlign()
{
    var result = buildOptions();

    if (result.message != null && result.message != "")
    {
        alert(result.message);
        result.element.focus();
    }
    else
    {
        url = "<%=urlOk%>";

        oForm.action = url;
        oForm.<%=WebAppConstants.GAP_ACTION%>.value =
          '<%=WebAppConstants.GAP_ACTION_CREATEPACKAGE%>';
        
        oForm.gapoptions.value = getDomString(result.dom);
        oForm.submit();
    }
}

function buildOptions()
{
    var result = new Result("", "", null,null);
    var dom = $.parseXML(xmlStrForJS);
    var node;
    
    node = $(dom).find("alignerOptions");

    if (document.oDummyForm.idIgnoreFormatting.checked)
    {
       $(node).find("ignoreFormatting").text("true");
    }
    else
    {
    	$(node).find("ignoreFormatting").text("false");
    }
    result.dom = dom;
    return result;
}

function parseOptions()
{
    var form = document.oDummyForm;
    var dom = $.parseXML(xmlStrForJS);
    var nodes, node, ignoreFormatting;

    node = $(dom).find("alignerOptions");

    ignoreFormatting = $(node).find("ignoreFormatting").text();

    if (ignoreFormatting == "true")
    {
        form.idIgnoreFormatting.checked = true;
    }
}

function doOnLoad()
{
    // Load the Guides
    loadGuides();

    parseOptions();
}
</SCRIPT>
</HEAD>
<BODY onload="doOnLoad()" LEFTMARGIN="0" RIGHTMARGIN="0" 
        TOPMARGIN="0" MARGINWIDTH="0" MARGINHEIGHT="0"
        CLASS="standardText">
<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>

<XML id="oOptions"><%=gapOptions.getXml()%></XML>

<DIV ID="contentLayer" 
    STYLE=" POSITION: ABSOLUTE; Z-INDEX: 9; TOP: 108px; LEFT: 20px; RIGHT: 20px;">
<DIV CLASS="mainHeading" ID="idHeading"><%=lb_title%></DIV>
<BR>

<TABLE CELLPADDING=0 CELLSPACING=0 BORDER=0 CLASS=standardText>
  <TR>
    <TD WIDTH=500><%=lb_helptext%></TD>
  </TR>
</TABLE>
<BR>

<FORM NAME="oForm" ACTION="" METHOD="post">
<INPUT TYPE="HIDDEN" NAME="<%=WebAppConstants.GAP_ACTION%>" VALUE="">
<INPUT TYPE="hidden" NAME="gapoptions" VALUE="Options XML goes here">
</FORM>

<FORM NAME="oDummyForm">
<TABLE CELLPADDING=2 CELLSPACING=2 BORDER=0 CLASS=standardText>
  <THEAD>
    <col valign="top" align="right">
    <col valign="top" align="left">
  </THEAD>
  <TBODY>
    <TR>
<%--
      <TD><LABEL for="idIgnoreFormatting">Ignore formatting:</LABEL></TD>
      <TD>
	<input type="checkbox"
	name="idIgnoreFormatting" id="idIgnoreFormatting">
      </TD>
--%>
      <TD><%=bundle.getString("lb_aligmer_para_none") %></TD>
      <TD>
	<input type="hidden"
	name="idIgnoreFormatting" id="idIgnoreFormatting">
      </TD>
    </TR>
  </TBODY>
</TABLE>
</FORM>

<DIV id="idButtons">
<INPUT TYPE="BUTTON" VALUE="<%=bundle.getString("lb_cancel")%>"
 onclick="doCancel()">
&nbsp;
<INPUT TYPE="BUTTON" VALUE="<%=bundle.getString("lb_previous")%>"
 onclick="doPrevious()">
&nbsp;
<INPUT TYPE="BUTTON" VALUE="<%=bundle.getString("lb_create_package") %>"
 onclick="doAlign()">
</DIV>

</DIV>

</BODY>
</HTML>
