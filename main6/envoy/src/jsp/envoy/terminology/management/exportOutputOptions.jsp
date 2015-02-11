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

// Perform error handling, then clear out session attribute.
String errorScript = "";
String error = (String)sessionMgr.getAttribute(WebAppConstants.TERMBASE_ERROR);
if (error != null)
{
  errorScript = "var error = new Error();" +
    "error.message = '" + EditUtil.toJavascript(bundle.getString("lb_import_error")) + "';" +
    "error.description = '" + EditUtil.toJavascript(error) +
    "'; showError(error);";
}
sessionMgr.removeElement(WebAppConstants.TERMBASE_ERROR);

String urlNext     = next.getPageURL();
String urlPrevious = previous.getPageURL();
String urlCancel   = cancel.getPageURL();

String lb_title = bundle.getString("lb_terminology_output_options");
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
<%@ include file="/includes/compatibility.jspIncl" %>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/jquery/jquery-1.6.4.min.js"></SCRIPT>
<SCRIPT language="Javascript" SRC="/globalsight/includes/library.js"></SCRIPT>
<SCRIPT language="Javascript" src="envoy/terminology/management/protocol.js"></SCRIPT>
<SCRIPT LANGUAGE="JavaScript">
var needWarning = false;
var objectName = "";
var guideNode = "terminology";
var helpFile = "<%=bundle.getString("help_termbase_exportoptions")%>";
var xmlExportOptions = 
	'<%=xmlExportOptions.replace("\\", "\\\\").replace("\r","").replace("\n","").trim()%>';
</SCRIPT>
<SCRIPT language="Javascript">
eval("<%=errorScript%>");

function Result(message, description, element)
{
    this.message = message;
    this.description = description;
    this.element = element;
    this.dom = null;
}

function buildExportOptions()
{
  var result = new Result("", "", null);
  var form = document.oDummyForm;
  var dom,node,check ;

  dom = $.parseXML(xmlExportOptions);
  
  // SELECT OPTIONS
  node = $(dom).find("exportOptions outputOptions");

  if (form.oSystemFields.checked)
  {
	  $(node).find("systemFields").text("true");
  }
  else
  {
     $(node).find("systemFields").text("false");
  }

  result.dom = dom;
  return result;
}

function parseExportOptions()
{
  var form = document.oDummyForm;
  var dom,nodes, node;
  var systemFields;

  dom = $.parseXML(xmlExportOptions);
  
  node = $(dom).find("exportOptions outputOptions");
  systemFields = $(node).find("systemFields").text();

  checkValue(form.oSystemFields, systemFields);

  var count = $(dom).find("exportOptions fileOptions entryCount").text();
  idEntryCount.innerText = count;

  if (parseInt(count) == 0)
  {
    idWarning.style.display = '';
  }
}

function checkValue(check, value)
{
  if (value == "false")
  {
    check.checked = false;
  }
  else
  {
    check.checked = true;
  }
}

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
    var result = buildExportOptions();

    if (result.message != null && result.message != "")
    {
        showError(result);
        result.element.focus();
    }
    else
    {
        var url;

        url = "<%=urlNext%>";
        url +=
            "&<%=WebAppConstants.TERMBASE_ACTION%>" +
            "=<%=WebAppConstants.TERMBASE_ACTION_START_EXPORT%>";

        oForm.action = url;
        
        oForm.exportoptions.value = getDomString(result.dom);
        oForm.submit();
    }
}

function doOnLoad()
{
   // Load the Guides
   loadGuides();

   parseExportOptions();
}
</SCRIPT>
</HEAD>
<BODY LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0" MARGINWIDTH="0"
 MARGINHEIGHT="0" CLASS="standardText" onload="doOnLoad()">
<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>

<DIV style="display:none">
<XML id="oDefinition"><%=xmlDefinition%></XML>
<XML id="oExportOptions"><%=xmlExportOptions%></XML>
</DIV>

<FORM NAME="oForm" ACTION="" METHOD="post">
<INPUT TYPE="hidden" NAME="exportoptions"
 VALUE="ExportOptions XML goes here"></INPUT>
</FORM>

<DIV ID="contentLayer"
 STYLE=" POSITION: ABSOLUTE; Z-INDEX: 9; TOP: 108px; LEFT: 20px; RIGHT: 20px;">
<DIV CLASS="mainHeading" ID="idHeading"><%=lb_title%></DIV>
<BR>

<DIV id="idWarning" style="color: red; display: none;">
	<%=bundle.getString("lb_terminology_note_termbase_empty")%>
<BR><BR>
</DIV>

<DIV>
	Entries with only 1 term will be ignored.
</DIV>

<DIV>
	<%=bundle.getString("lb_terminology_expected_number_of_entries_to_be_exported")%>
<SPAN id="idEntryCount" style="font-weight: bold"></SPAN>.
</DIV>

<FORM NAME="oDummyForm" method="post">

<div style="margin-bottom:10px">
<label for="idSystemFields"><%=bundle.getString("lb_terminology_output_system_fields")%></label>
<INPUT type="checkbox" name="oSystemFields" id="idSystemFields"></INPUT>
</div>

<BR>

<DIV id="idButtons" align="left">
<input type="button" TABINDEX="0" onclick="doCancel();" 	value="<%=bundle.getString("lb_cancel")%>">&nbsp;
<input type="button" TABINDEX="0" onclick="doPrevious();"	value="<%=bundle.getString("lb_previous")%>">&nbsp;
<input type="button" TABINDEX="0" onclick="doNext();"		value="<%=bundle.getString("lb_export1")%>">
</DIV>

</FORM>

</BODY>
</HTML>
