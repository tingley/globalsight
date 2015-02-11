<%@ page
    contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="com.globalsight.everest.webapp.webnavigation.LinkHelper,
        com.globalsight.everest.servlet.util.SessionManager,
        com.globalsight.everest.webapp.pagehandler.PageHandler,
        com.globalsight.everest.webapp.WebAppConstants,
        java.util.*"
    session="true"
%>
<jsp:useBean id="next" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="cancel" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<%
ResourceBundle bundle = PageHandler.getBundle(session);
SessionManager sessionMgr = (SessionManager)session.getAttribute(
  WebAppConstants.SESSION_MANAGER);

String xmlImportOptions =
  (String)sessionMgr.getAttribute(WebAppConstants.TERMBASE_IMPORT_OPTIONS);

String urlNext = next.getPageURL();
String urlCancel = cancel.getPageURL();
%>
<HTML XMLNS:gs>
<HEAD>
<TITLE><%=bundle.getString("lb_snippet_import")%></TITLE>
<SCRIPT SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>
<%@ include file="/includes/compatibility.jspIncl" %>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/jquery/jquery-1.6.4.min.js"></SCRIPT>
<SCRIPT language="Javascript" SRC="/globalsight/includes/library.js"></SCRIPT>
<SCRIPT src="envoy/terminology/management/protocol.js"></SCRIPT>
<SCRIPT src="envoy/terminology/management/import.js"></SCRIPT>
<SCRIPT>
var needWarning = false;
var objectName = "";
var guideNode = "snippetimport";
var helpFile = "<%=bundle.getString("help_import_snippet_files")%>";
var xmlImportOptions = '<%=xmlImportOptions.replace("\\", "\\\\").trim()%>';

function Result(message, description, element)
{
    this.message = message;
    this.description = description;
    this.element = element;
    this.dom = null;
}

function doCancel()
{
    window.navigate("<%=urlCancel%>");
}

function doNext()
{
    var result = buildFileOptions();

    if (result.message != null && result.message != "")
    {
        showError(result);
        result.element.focus();
    }
    else
    {
        var url = "<%=urlNext%>";
        
        url +=
            "&<%=WebAppConstants.TERMBASE_ACTION%>" +
            "=<%=WebAppConstants.TERMBASE_ACTION_UPLOAD_FILE%>";

        oForm.action = url;
      	oForm.importoptions.value = getDomString(result.dom);
        
      	oForm.submit();
    }
}

function buildFileOptions()
{
    var result = new Result("", "", null);
    var dom = $.parseXML(xmlImportOptions);
    var node;

    node = $(dom).find("importOptions fileOptions");

    $(node).find("fileName").text(oForm.filename.value);

    var sel = document.oDummyForm.oEncoding;
    $(node).find("fileEncoding").text(sel.options[sel.selectedIndex].value);

    if (oForm.filename.value == "")
    {
        return new Result("Missing filename",
            "please specify a filename.", oForm.filename);
    }

    result.dom=dom;
    return result;
}

function parseFileOptions()
{
    var form = document.oDummyForm;
    var dom = $.parseXML(xmlImportOptions);
    var nodes, node, fileName, fileEncoding;

    node = $(dom).find("importOptions fileOptions");

    fileName = $(node).find("fileName").text();
    fileEncoding = $(node).find("fileEncoding").text();

    id = Encodings[fileEncoding];
    form.oEncoding.selectedIndex = id;
}

function fillEncodings()
{
    var form = document.oDummyForm;
    var options = form.oEncoding.options;
    for (i = options.length; i >= 1; --i)
    {
        options.remove(i-1);
    }

    for (key in Encodings)
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
    parseFileOptions();
    oForm.filename.focus();
}
</SCRIPT>
</HEAD>
<BODY onload="doOnLoad()" LEFTMARGIN="0" RIGHTMARGIN="0" 
        TOPMARGIN="0" MARGINWIDTH="0" MARGINHEIGHT="0"
        CLASS="standardText">
<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>
<DIV ID="contentLayer" 
    STYLE=" POSITION: ABSOLUTE; Z-INDEX: 9; TOP: 108px; LEFT: 20px; RIGHT: 20px;">
<SPAN CLASS="mainHeading" ID="idHeading"><%=bundle.getString("lb_snippet_import")%></SPAN>

<div style="display:none">
<XML id="oImportOptions"><%=xmlImportOptions%></XML>
</div>

<%-- We wrap the form around the file only. --%>
<FORM NAME="oForm" ACTION="" ENCTYPE="multipart/form-data" METHOD="post">
<br>
<DIV style="margin-bottom: 12px">
<LABEL FOR="idFilename" ACCESSKEY="F"><%=bundle.getString("lb_select_import_file")%></LABEL><BR>
<INPUT TYPE="file" NAME="filename" id="idFilename" SIZE="40"></INPUT>
<BR><BR>
</DIV>

<INPUT TYPE="hidden" NAME="importoptions"
 VALUE="ImportOptions XML goes here"></INPUT>
</FORM>

<FORM NAME="oDummyForm">
<B><%=bundle.getString("lb_file_options")%></B>
<DIV id="idCsvOptions">
<TABLE CELLPADDING=2 CELLSPACING=2 BORDER=0 CLASS=standardText>
  <TR>
    <TD COLSPAN=2>&nbsp;</TD>
  </TR>
  <TR VALIGN="TOP">
    <TD WIDTH=100>
      <LABEL FOR="idEncoding" ACCESSKEY="E"><%=bundle.getString("lb_file_encoding")%></LABEL>
    </TD>
    <TD>
      <SELECT name="oEncoding" id="idEncoding"></SELECT>
    </TD>
  </TR>
  <TR>
    <TD COLSPAN=2>&nbsp;</TD>
  </TR>
</TABLE>
</DIV>
</FORM>

<DIV id="idButtons">
  <INPUT TYPE="BUTTON" VALUE="Cancel" ONCLICK="doCancel()"> &nbsp;
  <INPUT TYPE="BUTTON" VALUE="Next" ONCLICK="doNext()">
</DIV>

</DIV>

</BODY>
</HTML>
