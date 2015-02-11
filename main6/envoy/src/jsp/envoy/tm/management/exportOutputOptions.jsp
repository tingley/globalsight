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
<jsp:useBean id="prev" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="cancel" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<%
ResourceBundle bundle = PageHandler.getBundle(session);
SessionManager sessionMgr = (SessionManager)session.getAttribute(
  WebAppConstants.SESSION_MANAGER);

String xmlDefinition =
  (String)sessionMgr.getAttribute(WebAppConstants.TM_DEFINITION);
String xmlExportOptions =
  (String)sessionMgr.getAttribute(WebAppConstants.TM_EXPORT_OPTIONS);
String termbaseName =
  (String)sessionMgr.getAttribute(WebAppConstants.TM_TM_NAME);

// Perform error handling, then clear out session attribute.
String errorScript = "";
String error = (String)sessionMgr.getAttribute(WebAppConstants.TM_ERROR);
if (error != null)
{
  errorScript = "var error = new Error();" +
    "error.message = '" + EditUtil.toJavascript(bundle.getString("lb_export_error")) + "';" +
    "error.description = '" + EditUtil.toJavascript(error) +
    "'; showError(error);";
}
sessionMgr.removeElement(WebAppConstants.TM_ERROR);

String urlNext   = next.getPageURL();
String urlPrev   = prev.getPageURL();
String urlCancel = cancel.getPageURL();

String lb_title = bundle.getString("lb_tm_output_options");
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
<SCRIPT language="Javascript" SRC="/globalsight/includes/library.js"></SCRIPT>
<SCRIPT language="Javascript" src="envoy/tm/management/protocol.js"></SCRIPT>
<SCRIPT LANGUAGE="JavaScript">
var needWarning = false;
var objectName = "";
var guideNode = "tm";
var helpFile = "<%=bundle.getString("help_tm_exportoptions")%>";
var xmlDefinition	 = "<%=xmlDefinition%>";
var xmlExportOptions = "<%=xmlExportOptions%>";
</SCRIPT>
<SCRIPT language="Javascript">
eval("<%=errorScript%>");

function Result(message, description, element, dom)
{
    this.message = message;
    this.description = description;
    this.element = element;
    this.dom = dom;
}

function buildExportOptions()
{
  var result = new Result("", "", null, null);
  var form = document.oDummyForm;
  var dom ;
  var node;
  var check;

  if(window.navigator.userAgent.indexOf("MSIE")>0)
  {
    dom = oExportOptions.XMLDocument;
  }
  else if(window.DOMParser)
  { 
    var parser = new DOMParser();
    dom = parser.parseFromString(xmlExportOptions,"text/xml");
  }

  // OUTPUT OPTIONS
  node = dom.selectSingleNode("/exportOptions/outputOptions");
  if (form.oSystemFields.checked)
  {
     node.selectSingleNode("systemFields").text = "true";
  }
  else
  {
     node.selectSingleNode("systemFields").text = "false";
  }

  result.dom=dom;
  return result;
}

function parseExportOptions()
{
  var form = document.oDummyForm;
  var dom;
  var nodes, node;
  var systemFields;

  if(window.navigator.userAgent.indexOf("MSIE")>0)
  {
    dom = oExportOptions.XMLDocument;
  }
  else if(window.DOMParser)
  { 
    var parser = new DOMParser();
    dom = parser.parseFromString(xmlExportOptions,"text/xml");
  }

  node = dom.selectSingleNode("/exportOptions/outputOptions");
  systemFields = node.selectSingleNode("systemFields").text;

  checkValue(form.oSystemFields, systemFields);

  var count = dom.selectSingleNode("/exportOptions/fileOptions/entryCount").text;
  document.getElementById("idEntryCount").innerText = count;//idEntryCount.innerText = count;

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
	var result = buildExportOptions();
    var url;

    url = "<%=urlPrev%>";
    url +=
        "&<%=WebAppConstants.TM_ACTION%>" +
        "=<%=WebAppConstants.TM_ACTION_SET_EXPORT_OPTIONS%>";

    oForm.action = url;
    if(window.navigator.userAgent.indexOf("MSIE")>0)
    {
    	oForm.exportoptions.value = oExportOptions.xml;
    }
    else if(window.DOMParser)
    { 
    	oForm.exportoptions.value = XML.getDomString(result.dom);
    }  
    oForm.submit();
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
            "&<%=WebAppConstants.TM_ACTION%>" +
            "=<%=WebAppConstants.TM_ACTION_START_EXPORT%>";

        oForm.action = url;
        if(window.navigator.userAgent.indexOf("MSIE")>0)
        {
        	oForm.exportoptions.value = oExportOptions.xml;
        }
        else if(window.DOMParser)
        { 
        	oForm.exportoptions.value = XML.getDomString(result.dom);
        }  
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

<XML id="oDefinition"><%=xmlDefinition%></XML>
<XML id="oExportOptions"><%=xmlExportOptions%></XML>

<FORM NAME="oForm" ACTION="" METHOD="post">
<INPUT TYPE="hidden" NAME="exportoptions"
 VALUE="ExportOptions XML goes here"></INPUT>
</FORM>

<DIV ID="contentLayer"
 STYLE=" POSITION: ABSOLUTE; Z-INDEX: 9; TOP: 108px; LEFT: 20px; RIGHT: 20px;">
<DIV CLASS="mainHeading" ID="idHeading"><%=lb_title%></DIV>
<BR>

<DIV id="idWarning" style="color: red; display: none">
<%=bundle.getString("lb_tm_note_tm_is_empty_or_selected_language")%>
<BR><BR>
</DIV>

<DIV><%=bundle.getString("lb_tm_expected_no_of_entries_to_be_exported")%>
<SPAN id="idEntryCount" style="font-weight: bold"></SPAN>.</DIV>

<FORM NAME="oDummyForm">

<div style="margin-bottom:10px">
<label for="idSystemFields"><%=bundle.getString("lb_tm_output_system_fields")%></label>
<INPUT type="checkbox" name="oSystemFields" id="idSystemFields"></INPUT>
</div>
</FORM>
<BR>

<DIV id="idButtons" align="left">
<button TABINDEX="0" onclick="doCancel();"><%=bundle.getString("lb_cancel")%></button>&nbsp;
<button TABINDEX="0" onclick="doPrevious();"><%=bundle.getString("lb_previous")%></button>&nbsp;
<button TABINDEX="0" onclick="doNext();"><%=bundle.getString("lb_export1")%></button>
</DIV>


<BR>
<TABLE>
<TR><TD ALIGN="LEFT"><IMG SRC="/globalsight/images/TMX.gif"></TD></tr>
<TR><TD ALIGN="LEFT"><SPAN CLASS="smallText"><%=bundle.getString("lb_tmx_logo_text1")%><BR><%=bundle.getString("lb_tmx_logo_text2")%></SPAN></TD></TR>
</TABLE>
</BODY>
</HTML>
