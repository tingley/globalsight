<%@ page
    contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="java.util.*,com.globalsight.everest.webapp.webnavigation.LinkHelper,
        java.util.ResourceBundle,
        com.globalsight.util.edit.EditUtil,
        com.globalsight.terminology.exporter.ExportOptions,
        com.globalsight.everest.servlet.util.SessionManager,
        com.globalsight.everest.webapp.pagehandler.PageHandler,
        com.globalsight.everest.webapp.WebAppConstants"
    session="true"
%>
<jsp:useBean id="nextXML" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="nextCSV" scope="request"
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

String urlNextXML  = nextXML.getPageURL();
String urlNextCSV  = nextCSV.getPageURL();
String urlPrevious = previous.getPageURL();
String urlCancel   = cancel.getPageURL();

String lb_title= bundle.getString("lb_terminology_output_file_options");
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
<SCRIPT language="Javascript" src="envoy/terminology/management/import.js"></SCRIPT>
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
    oDummyForm.action = url;
    oDummyForm.submit();
}

function doNext()
{
    var form = document.oDummyForm;
    var sel = form.oFormat;
    var type = sel.options[sel.selectedIndex].value;
    if (type == "<%=ExportOptions.TYPE_CSV%>")
    {
      oDummyForm.action = "<%=urlNextCSV%>";
    }
    else
    {
    	oDummyForm.action = "<%=urlNextXML%>";
    }
    oDummyForm.submit();
}

function fillEncodings()
{
    var form = document.oDummyForm;
    var options = form.oEncoding.options;
    for (i = options.length; i >= 1; --i)
    {
        options.remove(i-1);
    }

    for (var key in Encodings)
    {
        // IE 5.0 adds prototype functions as keys
        if (key == 'shift' || key == 'unshift' || key == 'splice' ||
            key == 'push' || key == 'pop')
        {
          continue;
        }

        var option = document.createElement("OPTION");
        option.text = key;
        option.value = key;
        options.add(option);
    }
}

function doOnLoad()
{
   // Load the Guides
   loadGuides();

   fillEncodings();
}
</SCRIPT>
<%@ include file="/envoy/common/shortcutIcon.jspIncl" %>
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

<FORM NAME="oDummyForm" method="post">

<div style="margin-bottom:10px">
<%=bundle.getString("lb_terminology_export_format")%>
  <select name="oFormat" id="idFormat">
    <option value="<%=ExportOptions.TYPE_XML%>"><%=bundle.getString("lb_terminology_globalsight_format")%></option>
    <option value="<%=ExportOptions.TYPE_MTF%>"><%=bundle.getString("lb_terminology_multiterm_ix_format")%></option>
    <option value="<%=ExportOptions.TYPE_CSV%>"><%=bundle.getString("lb_terminology_csv_format")%></option>
<%--
    <option value="<%=ExportOptions.TYPE_MTW%>"><%=bundle.getString("lb_terminology_multiterm_1x_format")%></option>
    <option value="<%=ExportOptions.TYPE_RTF%>"><%=bundle.getString("lb_terminology_rtf_format")%></option>
    <option value="<%=ExportOptions.TYPE_TBX%>"><%=bundle.getString("lb_terminology_tbx_xlt_format")%></option>
--%>
  </select>
</div>

<div style="margin-bottom:10px">
  <%= bundle.getString("lb_terminology_import_encoding") %><SELECT name="oEncoding" id="idEncoding"></SELECT>
</div>

<BR>

<DIV id="idButtons" align="left">
<button TABINDEX="0" onclick="doCancel();"><%=bundle.getString("lb_cancel")%></button>&nbsp;
<button TABINDEX="0" onclick="doPrevious();"><%=bundle.getString("lb_previous")%></button>&nbsp;
<button TABINDEX="0" onclick="doNext();"><%=bundle.getString("lb_next")%></button>
</DIV>

</FORM>

</BODY>
</HTML>
