<%@ page
    contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="java.util.*,com.globalsight.everest.webapp.webnavigation.LinkHelper,
        java.util.ResourceBundle,
        com.globalsight.util.edit.EditUtil,
        com.globalsight.everest.projecthandler.importer.ImportOptions,
        com.globalsight.everest.servlet.util.SessionManager,
        com.globalsight.everest.webapp.pagehandler.PageHandler,
        com.globalsight.everest.webapp.WebAppConstants"
    session="true"
%>
<jsp:useBean id="nextXml" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="nextCsv" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="cancel" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<%
ResourceBundle bundle = PageHandler.getBundle(session);
SessionManager sessionMgr = (SessionManager)session.getAttribute(
  WebAppConstants.SESSION_MANAGER);

String xmlImportOptions =
  (String)sessionMgr.getAttribute(WebAppConstants.TM_IMPORT_OPTIONS);

String urlNextXml = nextXml.getPageURL();
String urlNextCsv = nextCsv.getPageURL();
String urlCancel  = cancel.getPageURL();

String lb_title = "Project Schedule Import";
String lb_project_export_description = "This screen allows you to import schedule data for localization participants that work on the selected project.";
%>
<HTML XMLNS:gs>
<HEAD>
<TITLE><%=lb_title%></TITLE>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>
<%@ include file="/includes/compatibility.jspIncl" %>
<SCRIPT language="Javascript" SRC="/globalsight/includes/library.js"></SCRIPT>
<!-- To get Encodings of input files -->
<SCRIPT language="Javascript" src="envoy/terminology/management/import.js"></SCRIPT>
<!-- To get showError and showWarning functions -->
<SCRIPT language="Javascript" src="envoy/tm/management/protocol.js"></SCRIPT>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/jquery/jquery-1.6.4.min.js"></SCRIPT>
<SCRIPT LANGUAGE="JavaScript">
var needWarning = false;
var objectName = "";
var guideNode = "projects";
var helpFile = "<%=bundle.getString("help_project_schedule_import")%>";
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
        var url;
        var dom = result.dom;
        
        var node = $(dom).find("importOptions fileOptions fileType");

        if (node.text() == "<%=ImportOptions.TYPE_XML%>")
        {
            url = "<%=urlNextXml%>";
        }
        else if (node.text() == "<%=ImportOptions.TYPE_CSV%>")
        {
            url = "<%=urlNextCsv%>";
        }

        url +=
            "&<%=WebAppConstants.TM_ACTION%>" +
            "=<%=WebAppConstants.TM_ACTION_UPLOAD_FILE%>";

        oForm.action = url;
        
        oForm.importoptions.value = getDomString(result.dom);
        oForm.submit();
    }
}

function checkExtension(path)
{
  if (path != null && path != "")
  {
    var index = path.lastIndexOf(".");
    if (index < 0)
    {
      return;
    }

    var form = document.oDummyForm;
    var ext = path.substring(index + 1);

<%--
    if (ext.toLowerCase() == "xml")
    {
        form.oType[0].click();
    }
    else if (ext.toLowerCase() == "txt")
    {
        form.oType[1].click();
    }
    else if (ext.toLowerCase() == "csv")
    {
        form.oType[1].click();
    }
--%>
  }
}

function setFileType()
{
<%--
   var form = document.oDummyForm;

   if (form.oType[0].checked)
   {
      // XML, no CSV options
      idCsvOptions.disabled = true;
      form.oEncoding.disabled = true;
   }
   else
   {
      // CSV, show CSV options
      idCsvOptions.disabled = false;
      form.oEncoding.disabled = false;
   }
--%>
}

function buildFileOptions()
{
    var result = new Result("", "", null);
    var dom = $.parseXML(xmlImportOptions);
    var node;

    
    node = $(dom).find("importOptions fileOptions");

    $(node).find("fileName").text(document.oForm.filename.value);

    $(node).find("fileType").text("<%=ImportOptions.TYPE_CSV%>");

<%--
    if (document.oDummyForm.oType[0].checked)
    {
       node.selectSingleNode("fileType").text = "<%=ImportOptions.TYPE_XML%>";
    }
    else if (document.oDummyForm.oType[1].checked)
    {
       node.selectSingleNode("fileType").text = "<%=ImportOptions.TYPE_CSV%>";
    }
--%>

    var sel = document.oDummyForm.oEncoding;
    $(node).find("fileEncoding").text(sel.options[sel.selectedIndex].value);

    var separator = $(node).find("separator");
    for (var key in Delimitor)
    {
       // IE 5.0 adds prototype functions as keys
       if (key == 'shift' || key == 'unshift' || key == 'splice' ||
           key == 'push' || key == 'pop')
       {
          continue;
       }

       var index = Delimitor[key];
       if (document.oDummyForm.oDelimit[index].checked)
       {
          separator.text(key);
          break;
       }
    }

    if (separator.text() == "other")
    {
        var value = document.oDummyForm.oDelimitText.value;
        if (value == "")
        {
          return new Result(
            "<%=EditUtil.toJavascript(bundle.getString("jsmsg_tb_import_missing_separator"))%>",
            "<%=EditUtil.toJavascript(bundle.getString("jsmsg_tb_import_specify_separator"))%>",
            document.oDummyForm.oDelimitText);
        }

        $(node).find("separator").text(value);
    }
    if (document.oDummyForm.oIgnoreHeader.checked)
    {
       $(node).find("ignoreHeader").text("true");
    }
    else
    {
    	$(node).find("ignoreHeader").text("false");
    }

    if (oForm.filename.value == "")
    {
        return new Result(
          "<%=EditUtil.toJavascript(bundle.getString("jsmsg_tb_import_missing_filename"))%>",
          "<%=EditUtil.toJavascript(bundle.getString("jsmsg_tb_import_specify_filename"))%>",
          oForm.filename);
    }

    result.dom=dom;
    return result;
}

function parseFileOptions()
{
    var form = document.oDummyForm;
    var dom = $.parseXML(xmlImportOptions);
    var nodes, node, fileName, fileType, fileEncoding;
    var separator, ignoreHeader;
    
    node = $(dom).find("importOptions fileOptions");

    fileName = $(node).find("fileName").text();
    fileType = $(node).find("fileType").text();
    fileEncoding = $(node).find("fileEncoding").text();
    separator = $(node).find("separator").text();
    ignoreHeader = $(node).find("ignoreHeader").text();
<%--
    if (fileType == "<%=ImportOptions.TYPE_XML%>")
    {
        form.oType[0].checked = 'true';
    }
    else if (fileType == "<%=ImportOptions.TYPE_CSV%>")
    {
        form.oType[1].checked = 'true';
    }
--%>

    if (ignoreHeader == "true")
    {
        form.oIgnoreHeader.checked = 'true';
    }

    var found = 0;
    var index = 0;

    if (separator != "")
    {
        for (var i in Delimitor)
        {
            if (separator == i)
            {
                index = Delimitor[separator];
                form.oDelimit[index].checked = 'true';
                found = 1;
                break;
            }
        }
        if (found == 0)
        {
            index = Delimitor['other'];
            form.oDelimit[index].checked = 'true';
            form.oDelimitText.value = separator;
        }
    }

    index = Encodings[fileEncoding];
    form.oEncoding.selectedIndex = index;
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
    parseFileOptions();
    setFileType();
    oDummyForm.oDelimitText.blur();
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
<SPAN CLASS="mainHeading" ID="idHeading"><%=lb_title%></SPAN>
<P></P>
<TABLE CELLPADDING=0 CELLSPACING=0 BORDER=0 CLASS=standardText>
  <TR>
    <TD WIDTH=500><%=lb_project_export_description%></TD>
  </TR>
</TABLE>
<P></P>

<XML id="oImportOptions" style="display:none"><%=xmlImportOptions%></XML>

<%-- We wrap the form around the file only. --%>
<FORM NAME="oForm" ACTION="" ENCTYPE="multipart/form-data" METHOD="post">

<DIV style="margin-bottom: 12px">
<%=bundle.getString("lb_terminology_select_import_file")%><BR>
<INPUT TYPE="file" NAME="filename" id="idFilename"
 onchange="checkExtension(this.value)" SIZE=40></INPUT>
<BR><BR>
</DIV>

<INPUT TYPE="hidden" NAME="importoptions"
 VALUE="ImportOptions XML goes here"></INPUT>
</FORM>

<FORM NAME="oDummyForm">
<B><%=bundle.getString("lb_terminology_import_file_options")%></B>
<DIV>
<TABLE CELLPADDING=2 CELLSPACING=2 BORDER=0 CLASS=standardText>
  <TR VALIGN="TOP">
    <TD WIDTH=100><%=bundle.getString("lb_terminology_import_format")%></TD>
    <TD><B>CSV</B>
<%--
      <input type="radio" name="oType" id="idXml"
      CHECKED="true" onclick="setFileType()">
      <label for="idXml">
      <%=bundle.getString("lb_terminology_import_format_xml")%>
      </label>
      <BR>
      <input type="radio" name="oType" id="idCsv" onclick="setFileType()">
      <label for="idCsv">
      <%=bundle.getString("lb_terminology_import_format_csv")%>
      </label>
      <SPAN CLASS="smallText">
      <%=bundle.getString("lb_terminology_csv_note")%>
      </SPAN>
--%>
    </TD>
  </TR>
</TABLE>
</DIV>
  
<DIV id="idCsvOptions">
<TABLE CELLPADDING=2 CELLSPACING=2 BORDER=0 CLASS=standardText>
  <TR>
    <TD COLSPAN=2>&nbsp;</TD>
  </TR>
  <TR VALIGN="TOP">
    <TD WIDTH=100><%=bundle.getString("lb_terminology_import_encoding")%></TD>
    <TD><SELECT name="oEncoding" id="idEncoding"></SELECT></TD>
  </TR>
  <TR>
    <TD COLSPAN=2>&nbsp;</TD>
  </TR>
  <TR VALIGN="TOP">
    <TD><%=bundle.getString("lb_terminology_col_delimiter")%></TD>
    <TD>
      <input type="radio" name="oDelimit" id="idTab">
      <label for="idTab">
      <%=bundle.getString("lb_terminology_import_delimiter_tab")%>
      </label><BR>
      <input type="radio" name="oDelimit" id="idSemicolon">
      <label for="idSemicolon">
      <%=bundle.getString("lb_terminology_import_delimiter_semicolon")%>
      </label><BR>
      <input type="radio" name="oDelimit" id="idComma" CHECKED="true">
      <label for="idComma">
      <%=bundle.getString("lb_terminology_import_delimiter_comma")%>
      </label><BR>
      <input type="radio" name="oDelimit" id="idSpace">
      <label for="idSpace">
      <%=bundle.getString("lb_terminology_import_delimiter_space")%>
      </label><BR>
      <input type="radio" name="oDelimit" id="idOther" onclick="idDelimitText.focus()">
      <label for="idOther">
      <%=bundle.getString("lb_terminology_import_delimiter_other")%>
      </label> 
      <input type="text"  name="oDelimitText" id="idDelimitText" size="2"
      onclick="idOther.click()" onfocus="select()"></input>
    </TD>
  </TR>
</TABLE>
<BR>
<%=bundle.getString("lb_terminology_import_skip_header")%>
<input type="checkbox" name="oIgnoreHeader" id="idIgnoreHeader"></input>
</DIV>

</FORM>

<DIV id="idButtons">
<INPUT TYPE="BUTTON" VALUE="<%=bundle.getString("lb_cancel")%>"
 onclick="doCancel()">
<INPUT TYPE="BUTTON" VALUE="<%=bundle.getString("lb_next")%>"
 onclick="doNext()">
</DIV>

</DIV>

</BODY>
</HTML>
