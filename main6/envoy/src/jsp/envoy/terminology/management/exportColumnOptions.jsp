<%@ page
    contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="java.util.*,com.globalsight.everest.webapp.webnavigation.LinkHelper,
        java.util.ResourceBundle,
        com.globalsight.util.edit.EditUtil,
        com.globalsight.everest.servlet.util.SessionManager,
        com.globalsight.everest.webapp.pagehandler.PageHandler,
        com.globalsight.everest.webapp.WebAppConstants"
    session="true"
%>
<jsp:useBean id="next" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="previous" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="cancel" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<%
ResourceBundle bundle = PageHandler.getBundle(session);
SessionManager sessionMgr = (SessionManager)session.getAttribute(
  WebAppConstants.SESSION_MANAGER);

String xmlDefinition =
  (String)sessionMgr.getAttribute(WebAppConstants.TERMBASE_DEFINITION);
String xmlExportOptions =
  (String)sessionMgr.getAttribute(WebAppConstants.TERMBASE_EXPORT_OPTIONS);
String termbaseName =
  (String)sessionMgr.getAttribute(WebAppConstants.TERMBASE_TB_NAME);

String urlNext     = next.getPageURL();
String urlPrevious = previous.getPageURL();
String urlCancel   = cancel.getPageURL();

String lb_title = "CSV Output Columns";
%>
<HTML XMLNS:gs>
<HEAD>
<TITLE><%=lb_title%></TITLE>
<STYLE>
{ font: Tahoma Verdana Arial 10pt; }
INPUT, SELECT { font: Tahoma Verdana Arial 10pt; }

LEGEND        { font-size: smaller; font-weight: bold; }
.link         { color: blue; cursor: hand; }
</STYLE>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>
<SCRIPT language="Javascript" SRC="/globalsight/includes/library.js"></SCRIPT>
<SCRIPT LANGUAGE="JavaScript">
var needWarning = false;
var objectName = "";
var guideNode = "terminology";
var helpFile = "<%=bundle.getString("help_termbase_export")%>";
</SCRIPT>
<SCRIPT language="Javascript">
function doCancel()
{
    window.navigate("<%=urlCancel%>");
}

function doPrevious()
{
    var url;

    url = "<%=urlPrevious%>";
    url +=
        "&<%=WebAppConstants.TERMBASE_ACTION%>" +
        "=<%=WebAppConstants.TERMBASE_ACTION_SET_EXPORT_OPTIONS%>";
    window.navigate(url);
}

function doNext()
{
    window.navigate("<%=urlNext%>");
}

function doOnLoad()
{
   // Load the Guides
   loadGuides();

   var dom = oDefinition.XMLDocument;
   var names = dom.selectNodes("/definition/languages/language/name");

   for (i = 0; i < names.length; ++i)
   {
     var name = names.item(i).text;

     oOption = document.createElement("OPTION");
     oOption.text = name;
     oOption.value = name;
     oDummyForm.oLanguages.add(oOption);
   }
}
</SCRIPT>
</HEAD>
<BODY LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0" MARGINWIDTH="0"
 MARGINHEIGHT="0" CLASS="standardText" onload="doOnLoad()">
<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>

<XML id="oDefinition"><%=xmlDefinition%></XML>
<XML id="oExportOptions"><%=xmlExportOptions%></XML>

<FORM NAME="oForm" ACTION="" METHOD="post">
<INPUT TYPE="hidden" NAME="exportoptions"
 VALUE="ExportOptions XML goes here"></INPUT>
</FORM>

<DIV ID="contentLayer"
 STYLE=" POSITION: ABSOLUTE; Z-INDEX: 9; TOP: 108px; LEFT: 20px; RIGHT: 20px;">
<SPAN CLASS="mainHeading" ID="idHeading"><%=lb_title%></SPAN>

<FORM NAME="oDummyForm">

<div style="margin-bottom:10px"></div>

<div style="margin-bottom:10px">
	<%=bundle.getString("lb_terminology_todo_need_popup_dialogs ")%>
</div>

<div style="margin-bottom:10px">
	<%=bundle.getString("lb_terminology_select_language_to_output")%>
	<select name="oLanguages" id="idLanguages" SIZE="3" MULTIPLE
	  ALIGN="top"></select>
<div style="margin-left: 40px">
  <BR>
</div>
</div>
<BR>

<DIV id="idButtons" align="left">
<button TABINDEX="0" onclick="doCancel();"><%=bundle.getString("lb_cancel")%></button>&nbsp;
<button TABINDEX="0" onclick="doPrevious();"><%=bundle.getString("lb_previous")%></button>&nbsp;
<button TABINDEX="0" onclick="doNext();"><%=bundle.getString("lb_export1")%></button>
</DIV>

</FORM>

</BODY>
</HTML>
