<%@ page
    contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="java.util.*,com.globalsight.everest.webapp.webnavigation.LinkHelper,
        java.util.ResourceBundle,
        com.globalsight.util.edit.EditUtil,
        com.globalsight.terminology.importer.ImportOptions,
        com.globalsight.everest.servlet.util.SessionManager,
        com.globalsight.everest.webapp.pagehandler.PageHandler,
        com.globalsight.everest.webapp.WebAppConstants"
    session="true"
%>
<jsp:useBean id="nextXml" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="nextCsv" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="nextExcel" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="cancel" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<%
ResourceBundle bundle = PageHandler.getBundle(session);
SessionManager sessionMgr = (SessionManager)session.getAttribute(
  WebAppConstants.SESSION_MANAGER);

String xmlDefinition =
  (String)sessionMgr.getAttribute(WebAppConstants.TERMBASE_DEFINITION);
String xmlImportOptions =
  (String)sessionMgr.getAttribute(WebAppConstants.TERMBASE_IMPORT_OPTIONS);

String urlNextXml = nextXml.getPageURL();
String urlNextCsv = nextCsv.getPageURL();
String urlCancel  = cancel.getPageURL();
String urlNextExcel = nextExcel.getPageURL();
%>
<HTML XMLNS:gs>
<!-- This is envoy\terminology\management\import.jsp -->
<HEAD>
<TITLE><%=bundle.getString("lb_terminology_import")%></TITLE>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>
<%@ include file="/includes/compatibility.jspIncl" %>
<SCRIPT language="Javascript" src="/globalsight/includes/library.js"></SCRIPT>
<SCRIPT language="Javascript" src="envoy/terminology/management/protocol.js"></SCRIPT>
<SCRIPT language="Javascript" src="envoy/terminology/management/import.js"></SCRIPT>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/jquery/jquery-1.6.4.min.js"></SCRIPT>
<SCRIPT LANGUAGE="JavaScript">
var needWarning = false;
var objectName = "";
var guideNode = "terminology";
var helpFile = "<%=bundle.getString("help_termbase_import")%>";

</SCRIPT>
<SCRIPT language="Javascript">
function Result(message, description, element, dom)
{
    this.message = message;
    this.description = description;
    this.element = element;
    this.dom = dom;
}

function doCancel()
{
    window.location.href = "<%=urlCancel%>";
}

function doNext()
{
    var result = buildFileOptions();

    if (result.message != null && result.message != "")
    {
        alert(result.message);
        result.element.focus();
        return;
    }
    else
    {
        var url;
        var dom = result.dom;
        
        var node = $(dom).find("importOptions fileOptions fileType");
        var nodeText = node.text();

        if (nodeText == "<%=ImportOptions.TYPE_XML%>" ||
            nodeText == "<%=ImportOptions.TYPE_MTF%>" ||
            nodeText == "<%=ImportOptions.TYPE_TBX%>")
        {
            url = "<%=urlNextXml%>";
        }
        else if (nodeText == "<%=ImportOptions.TYPE_CSV%>")
        {
			url = "<%=urlNextCsv%>";
        }

        url +=
            "&<%=WebAppConstants.TERMBASE_ACTION%>" +
            "=<%=WebAppConstants.TERMBASE_ACTION_UPLOAD_FILE%>";

        oForm.action = url;
        
        oForm.importoptions.value = getDomString(result.dom);
        
        oForm.submit();
        document.getElementById("nextButton").disabled = true;
    }
}

function doExcelImport()
{
    var result = buildFileOptions();

    if(document.getElementById("idFilename").value == "") {
        alert("<%=EditUtil.toJavascript(bundle.getString("jsmsg_tb_import_missing_filename"))%>");
        return;
    }
    
    if (result.message != null && result.message != "")
    {
        showError(result);
        result.element.focus();
    }
    else
    {
        oForm.action = "<%=urlNextExcel%>&<%=WebAppConstants.TERMBASE_ACTION%>" +
            "=<%=WebAppConstants.TERMBASE_ACTION_UPLOAD_IMPORT_EXCEL_FILE%>";
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

    if (ext.toLowerCase() == "xml")
    {
        form.idXml.click();
    }
    else if (ext.toLowerCase() == "xls" || ext.toLowerCase() == "xlsx")
    {
        form.idExcel.click();
    }
    else if (ext.toLowerCase() == "mtf")
    {
        form.idMtf.click();
    }
    else if (ext.toLowerCase() == "txt" || ext.toLowerCase() == "csv" )
    {
        form.idCsv.click();
    }
	if (ext.toLowerCase() == "tbx")
	{
		form.idTbx.click();
	}
  }
}

function setFileType()
{
   var form = document.oDummyForm;

   if (form.idXml.checked || form.idExcel.checked || form.idMtf.checked || form.idTbx.checked)
   {
      // XML/MTF, no CSV options
      if(window.navigator.userAgent.indexOf("MSIE")>0)
      {
      	idCsvOptions.disabled = true;
      }
      else
      {
      	disableDiv("idCsvOptions", true);
      }
      
      form.oEncoding.options[26].selected = true;
      if (form.idExcel.checked)
      {
         document.getElementById('importButton').style.visibility = 'visible';
         document.getElementById('nextButton').style.visibility = 'hidden';
      }
      else
      {
         document.getElementById('importButton').style.visibility = 'hidden';
         document.getElementById('nextButton').style.visibility = 'visible';
      }
   }
   else
   {
      // CSV, show CSV options
      if(window.navigator.userAgent.indexOf("MSIE")>0)
      {
      	idCsvOptions.disabled = false;
      }
      else
      {
      	disableDiv("idCsvOptions", false);
      }

      document.getElementById('importButton').style.visibility = 'hidden';
      document.getElementById('nextButton').style.visibility = 'visible';
   }
}

function buildFileOptions()
{
    var result = new Result("", "", null, null);
   
    var xmlStr = "<%=xmlImportOptions.trim()%>";
    
    dom = $.parseXML(xmlStr);
    var node;

    node = $(dom).find("importOptions fileOptions");
    var fileType = "";
    
    if (document.oDummyForm.idXml.checked)
    {
       	fileType = "<%=ImportOptions.TYPE_XML%>";
    }
    else if (document.oDummyForm.idExcel.checked)
    {
       	fileType = "<%=ImportOptions.TYPE_EXCEL%>";
    }
    else if (document.oDummyForm.idMtf.checked)
    {
       	fileType = "<%=ImportOptions.TYPE_MTF%>";
    }
    else if (document.oDummyForm.idTbx.checked)
    {
    	fileType = "<%=ImportOptions.TYPE_TBX%>";
    }
    else
    {
       	fileType = "<%=ImportOptions.TYPE_CSV%>";
    }
    

    var sel = document.oDummyForm.oEncoding;
    
    $(node).find("fileEncoding").text(sel.options[sel.selectedIndex].value);
    
    $(node).find("fileName").text(document.oForm.filename.value);
    
    $(node).find("fileType").text(fileType);

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
    var isOther = (separator.text() == "other");
    if (isOther)
    {
    	alert("document.oDummyForm.oDelimitText.value="+document.oDummyForm.oDelimitText.value);
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
    result.dom = dom;
    return result;
}

function parseFileOptions()
{
    var form = document.oDummyForm;
    
    var xmlStr = "<%=xmlImportOptions.trim()%>";
    dom = $.parseXML(xmlStr);
    
    var nodes, node, fileName, fileType, fileEncoding;
    var separator, ignoreHeader;

    node = $(dom).find("importOptions fileOptions");
    
    fileName = $(node).find("fileName").text();
    fileType = $(node).find("fileType").text();
    fileEncoding = $(node).find("fileEncoding").text();
    separator = $(node).find("separator").text();
    ignoreHeader = $(node).find("ignoreHeader").text();

    if (fileType == "<%=ImportOptions.TYPE_XML%>")
    {
        form.idXml.checked = 'true';
    }
    else if (fileType == "<%=ImportOptions.TYPE_EXCEL%>")
    {
        form.idExcel.checked = 'true';
    }
    else if (fileType == "<%=ImportOptions.TYPE_MTF%>")
    {
        form.idMtf.checked = 'true';
    }
    else if (fileType == "<%=ImportOptions.TYPE_CSV%>")
    {
    	form.idCsv.checked = 'true';
    }

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
    else
    {
        index = Delimitor['tab'];
        form.oDelimit[index].checked = 'true';
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
        if(window.navigator.userAgent.indexOf("MSIE")>0)
        {
        options.add(option);
        }
        else
        {
        form.oEncoding.appendChild(option);
        }
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
<SPAN CLASS="mainHeading" ID="idHeading"><%=bundle.getString("lb_terminology_import_date")%></SPAN>
<P>
<TABLE CELLPADDING=0 CELLSPACING=0 BORDER=0 CLASS=standardText>
<TR>
<TD WIDTH=500>
<%=bundle.getString("helper_text_terminology_import")%>
</TD>
</TR>
</TABLE>
<P>

<XML id="oDefinition" style="display:none;"><%=xmlDefinition%></XML>
<XML id="oImportOptions" style="display:none;"><%=xmlImportOptions%></XML>

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
    <TD>
      <input type="radio" name="oType" id="idXml"
      CHECKED="true" onclick="setFileType()">
      <label for="idXml">
      <%=bundle.getString("lb_terminology_import_format_xml")%>
      </label>
      <BR>
      <input type="radio" name="oType" id="idTbx"
      onclick="setFileType()">
      <label for="idTbx">
      <%=bundle.getString("lb_terminology_import_format_tbx")%>
      </label>
      <BR>
      <input type="radio" name="oType" id="idExcel" onclick="setFileType()">
      <label for="idExcel">
      <%=bundle.getString("lb_terminology_import_format_excel")%>
      </label>
      <BR>
      <input type="radio" name="oType" id="idMtf"
      onclick="setFileType()">
      <label for="idMtf">
      <%=bundle.getString("lb_terminology_import_format_mtf")%>
      </label>
      <BR>
      <input type="radio" name="oType" id="idCsv" onclick="setFileType()">
      <label for="idCsv">
      <%=bundle.getString("lb_terminology_import_format_csv")%>
      </label>
      <SPAN CLASS="smallText">
      <%=bundle.getString("lb_terminology_csv_note")%>
      </SPAN>
    </TD>
  </TR>
    <TR>
    <TD WIDTH=100><label><%=bundle.getString("lb_terminology_import_encoding")%></label></TD>
    <TD><SELECT name="oEncoding" id="idEncoding"></SELECT></TD>
  </TR>
</TABLE>
</DIV>
  
<DIV id="idCsvOptions">
<TABLE CELLPADDING=2 CELLSPACING=2 BORDER=0 CLASS=standardText>
  <TR>
    <TD COLSPAN=2>&nbsp;</TD>
  </TR>
  <TR VALIGN="TOP">
    <TD><label><%=bundle.getString("lb_terminology_col_delimiter")%></label></TD>
    <TD>
      <input type="radio" name="oDelimit" id="idTab" CHECKED="true">
      <label for="idTab">
      <%=bundle.getString("lb_terminology_import_delimiter_tab")%>
      </label><BR>
      <input type="radio" name="oDelimit" id="idSemicolon">
      <label for="idSemicolon">
      <%=bundle.getString("lb_terminology_import_delimiter_semicolon")%>
      </label><BR>
      <input type="radio" name="oDelimit" id="idComma">
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
<label for="idIgnoreHeader">
<%=bundle.getString("lb_terminology_import_skip_header")%>
</label>
<input type="checkbox" name="oIgnoreHeader" id="idIgnoreHeader"></input>
</DIV>

</FORM>

<DIV id="idButtons">
<INPUT TYPE="BUTTON" VALUE="<%=bundle.getString("lb_cancel")%>"
 onclick="doCancel()">
<INPUT id="nextButton" TYPE="BUTTON" STYLE="visibility:visible" VALUE="<%=bundle.getString("lb_next")%>"
 onclick="doNext()">
<INPUT id="importButton" TYPE="BUTTON" STYLE="visibility:hidden" VALUE="<%=bundle.getString("lb_import")%>"
 onclick="doExcelImport()">
</DIV>

</DIV>

</BODY>
</HTML>
