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
<jsp:useBean id="prev" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="next" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="cancel" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="reanalyze" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="testrun" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<%
ResourceBundle bundle = PageHandler.getBundle(session);
SessionManager sessionMgr = (SessionManager)session.getAttribute(
  WebAppConstants.SESSION_MANAGER);

String xmlDefinition =
  (String)sessionMgr.getAttribute(WebAppConstants.TERMBASE_DEFINITION);
String xmlImportOptions =
  (String)sessionMgr.getAttribute(WebAppConstants.TERMBASE_IMPORT_OPTIONS);

String urlCancel    = cancel.getPageURL();
String urlNext      = next.getPageURL();
String urlPrev      = prev.getPageURL();
String urlReanalyze = reanalyze.getPageURL();
String urlTestrun   = testrun.getPageURL();

String lb_no_col_mapped = bundle.getString("jsmsg_tb_import_no_col_mapped");
String lb_missing_separator =
  bundle.getString("jsmsg_tb_import_missing_separator_character");
String lb_failed_analysis = bundle.getString("jsmsg_tb_import_failed_analyze");
String lb_only_one_column =
  bundle.getString("jsmsg_tb_import_detected_one_col_or_less");

String lb_column_header = bundle.getString("lb_column_header");
String lb_data_sample = bundle.getString("lb_data_sample");
String lb_type = bundle.getString("lb_type");
String lb_associated_column = bundle.getString("lb_associated_column");
String lb_term_language = bundle.getString("lb_term_language2");
String lb_properties = bundle.getString("lb_properties");

// Perform error handling, then clear out session attribute.
sessionMgr.removeElement(WebAppConstants.TERMBASE_ERROR);

%>
<HTML XMLNS:gs>
<!-- This is envoy\terminology\management\importColumns.jsp -->
<HEAD>
<TITLE><%=bundle.getString("lb_terminology_verify_column")%></TITLE>
<STYLE>
/* Note: the font/font size in INPUT and TEXTAREA is different...  */
#idGeneral,
#idColumns   { margin-top: 5pt; }

FORM         { display: inline; }
TEXTAREA     { overflow: auto; }
</STYLE>
<SCRIPT SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>
<%@ include file="/includes/compatibility.jspIncl" %>
<SCRIPT src="/globalsight/includes/library.js"></SCRIPT>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/jquery/jquery-1.6.4.min.js"></SCRIPT>
<SCRIPT src="/globalsight/envoy/terminology/management/protocol.js"></SCRIPT>
<SCRIPT src="/globalsight/envoy/terminology/management/import.js"></SCRIPT>
<SCRIPT src="/globalsight/envoy/terminology/management/importObjects_js.jsp"></SCRIPT>
<SCRIPT src="/globalsight/envoy/terminology/management/importColumns_js.jsp"></SCRIPT>
<SCRIPT>
var needWarning = false;
var objectName = "";
var guideNode = "terminology";
var helpFile = "<%=bundle.getString("help_termbase_import_columns")%>";
var xmlDefinition = "<%=xmlDefinition%>";
var xmlImportOptions = 
	'<%=xmlImportOptions.replace("\\", "\\\\")%>';

function Result(message, element, domImportOptions)
{
    this.message = message;
    this.element = element;
    this.domImportOptions = domImportOptions;
}

function doCancel()
{
    window.location.href = "<%=urlCancel%>";
}

function doNext()
{
    var result = buildOptions();

    if (result.message != null && result.message != "")
    {
      showWarning(result.message);
      if (result.element)
      {
         result.element.focus();
      }
    }
    else
    {
      oForm.action = "<%=urlNext +
        "&" + WebAppConstants.TERMBASE_ACTION +
        "=" + WebAppConstants.TERMBASE_ACTION_SET_IMPORT_OPTIONS%>";

      oForm.importoptions.value = getDomString(result.domImportOptions);
      
      oForm.submit();
    }
}

<%--
function doPrev()
{
    var result = buildOptions();

    if (result.message != null && result.message != "")
    {
      showWarning(result.message);
      if (result.element)
      {
        result.element.focus();
      }
    }
    else
    {
      oForm.action = "<%=urlPrev +
        "&" + WebAppConstants.TERMBASE_ACTION +
        "=" + WebAppConstants.TERMBASE_ACTION_SET_IMPORT_OPTIONS%>";

      oForm.importoptions.value = oImportOptions.xml;
      oForm.submit();
    }
}
--%>

function doReAnalyze()
{
    result = buildFileOptions();

    if (result.message != null && result.message != "")
    {
      showWarning(result.message);
      if (result.element)
      {
        result.element.focus();
      }
      return;
    }
    else
    {
      oForm.action = "<%=urlReanalyze +
        "&" + WebAppConstants.TERMBASE_ACTION +
        "=" + WebAppConstants.TERMBASE_ACTION_ANALYZE_FILE%>";

      oForm.importoptions.value = getDomString(result.domImportOptions);
      
      oForm.submit();
    }
}

function doTestrun()
{
    var result = buildOptions();

    if (result.message != null && result.message != "")
    {
      showWarning(result.message);
      if (result.element)
      {
        result.element.focus();
      }
      return;
    }

    result = buildFileOptions();
    if (result.message != null && result.message != "")
    {
      showWarning(result.message);
      if (result.element)
      {
          result.element.focus();
      }
      return;
    }
    else
    {
      oForm.action = "<%=urlTestrun +
        "&" + WebAppConstants.TERMBASE_ACTION +
        "=" + WebAppConstants.TERMBASE_ACTION_TEST_IMPORT%>";

      oForm.importoptions.value = getDomString(result.domImportOptions);
      
      oForm.submit();
    }
}

function buildOptions()
{
    var result = new Result("", null,null);

    var dom;
    dom = $.parseXML(xmlImportOptions);
    var node;

    node = $(dom).find("importOptions columnOptions");
    var len = node.children().length;
    while(len > 0)
    {
   		node.children().eq(0).remove();
   		len = node.children().length;
    }

    var foundTerm = false;

    for (i = 0; i < aColumns.length; ++i)
    {
        var oCol = aColumns[i];

        var elem = dom.createElement("column");
        node.append(elem);
        var len = $(dom).find("importOptions columnOptions column").length;
        elem = $(dom).find("importOptions columnOptions column").eq(len-1);
        
        var name = dom.createElement("name");
        var example = dom.createElement("example");
        var type = dom.createElement("type");
        var termLanguage = dom.createElement("termLanguage");
        var encoding = dom.createElement("encoding");
        var assocCol = dom.createElement("associatedColumn");
        elem.append(name);
        elem.append(example);
        elem.append(type);
        elem.append(termLanguage);
        elem.append(encoding);
        elem.append(assocCol);
        
        $(elem).attr("id",oCol.id);
        $(elem).find("name").text(oCol.name);
        $(elem).find("example").text(oCol.example);
        $(elem).find("type").text(oCol.type);
        $(elem).find("termLanguage").text(oCol.termLanguage);
        $(elem).find("encoding").text(oCol.encoding);
        $(elem).find("associatedColumn").text(oCol.associatedColumn);
        
        if (oCol.type == "term")
        {
           foundTerm = true;
        }
    }

    if (!foundTerm)
    {
        return new Result("<%=EditUtil.toJavascript(lb_no_col_mapped)%>",
          null);
    }
    result.domImportOptions=dom;
    return result;
}

function buildFileOptions()
{
    var result = new Result("", null, null);
    var dom,node;

	dom = $.parseXML(xmlImportOptions);
    node = $(dom).find("importOptions fileOptions");
    var separator = $(node).find("separator");

    for (var key in Delimitor)
    {
       // IE 5.0 adds prototype functions as keys
       if (key == "shift" || key == "unshift" || key == "splice" ||
           key == "push" || key == "pop")
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
            "<%=EditUtil.toJavascript(lb_missing_separator)%>",
            document.oDummyForm.oDelimitText);
        }

        separator.text(value);
    }
    if (document.oDummyForm.oIgnoreHeader.checked)
    {
    	$(node).find("ignoreHeader").text("true");
    }
    else
    {
       $(node).find("ignoreHeader").text("false");
    }
    result.domImportOptions=dom;
    return result;
}

function parseColumns()
{
    var dom;
    var nodes, node, id, name, example, type, encoding;
    var associatedColumn, termLanguage;

    dom = $.parseXML(xmlImportOptions);
    
    nodes = $(dom).find("importOptions columnOptions column");
	
    for (i = 0; i < nodes.length; ++i)
    {
        node = nodes[i];//node = nodes.item(i)

        id = node.getAttribute("id");

        if ($(node).find("name"))
        {
            name = $(node).find("name").text();
        }
        if ($(node).find("example"))
        {
            example = $(node).find("example").text();
        }
        if ($(node).find("type"))
        {
            type = $(node).find("type").text();
        }
        if ($(node).find("encoding"))
        {
            encoding = $(node).find("encoding").text();
        }
        if ($(node).find("associatedColumn"))
        {
            associatedColumn = $(node).find("associatedColumn").text();
        }
        if ($(node).find("termLanguage"))
        {
            termLanguage = $(node).find("termLanguage").text();
        }
        aColumns.push(new Column(id, name, example, type,
            encoding, associatedColumn, termLanguage));
    }

    showColumns();
}

function checkAnalysisError()
{
    var dom;
    dom = $.parseXML(xmlImportOptions);
    
    var node = $(dom).find("importOptions fileOptions errorMessage");
    var errorMessage = node.text();

    if (errorMessage != "")
    {
        node.text = "";

        showWarning("<%=EditUtil.toJavascript(lb_failed_analysis)%>" +
          errorMessage);

        oForm.action = "<%=urlPrev +
          "&" + WebAppConstants.TERMBASE_ACTION +
          "=" + WebAppConstants.TERMBASE_ACTION_SET_IMPORT_OPTIONS%>";

      	oForm.importoptions.value = getDomString(oImportOptions);

      	oForm.submit();
        return true;
    }

    return false;
}

function parseFileOptions()
{
    var form = document.oDummyForm;
    var dom ;    
    var nodes, node, fileName, fileType, fileEncoding;
    var separator, ignoreHeader;

    dom = $.parseXML(xmlImportOptions);
    
    node = $(dom).find("importOptions fileOptions");

    separator = $(node).find("separator").text();
    ignoreHeader = $(node).find("ignoreHeader").text();
	
    idDelimiterSpan.innerText = separator;

    var found = 0;
    var index = 0;

    if (separator != "")
    {
        for (var i in Delimitor)
        {
            if (separator == i)
            {
                index  = Delimitor[separator];
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
    if (ignoreHeader == "true")
    {
        form.oIgnoreHeader.checked = 'true';
    }
}

function doOnLoad()
{
    // Load the Guides
    loadGuides();

    if (checkAnalysisError())
    {
        return;
    }

    var dom;
    dom = $.parseXML(xmlDefinition);
    
    addCustomFields(dom);

    parseFileOptions();
    parseColumns();

    if (aColumns.length <= 1)
    {
       var message = "<%=EditUtil.toJavascript(lb_only_one_column)%>";
       showWarning(message);
       oDummyForm.oDelimit[0].focus();
    }
    else
    {
      oDummyForm.oDelimitText.blur();
    }
}
</SCRIPT>
</HEAD>
<BODY ID="idBody" onload="doOnLoad()" LEFTMARGIN="0" RIGHTMARGIN="0"
 TOPMARGIN="0" MARGINWIDTH="0" MARGINHEIGHT="0" CLASS="standardText">
<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>

<DIV ID="contentLayer"
 STYLE="POSITION: ABSOLUTE; Z-INDEX: 9; TOP: 108px; LEFT: 20px; RIGHT: 20px;">

<SPAN CLASS="mainHeading" id="idHeading">
<%=bundle.getString("lb_terminology_verify_csv_col")%>
</SPAN>
<P></P>
<TABLE CELLPADDING=0 CELLSPACING=0 BORDER=0 CLASS=standardText>
  <TR>
    <TD WIDTH=500>
      <%=bundle.getString("helper_text_terminology_import_columns")%>
    </TD>
  </TR>
</TABLE>
<P></P>

<DIV style="display:none">
<XML id="oDefinition" ><%=xmlDefinition%></XML>
<XML id="oImportOptions" ><%=xmlImportOptions%></XML>
</DIV>

<FORM NAME="oForm" ACTION="" METHOD="post">
<INPUT TYPE="hidden" NAME="importoptions"
 VALUE="ImportOptions XML goes here"></INPUT>
</FORM>

<DIV><B><%=bundle.getString("lb_terminology_csv_delimiter")%></B></DIV>
<FORM NAME="oDummyForm" ACTION="" METHOD="post">
  <div>
  <%=bundle.getString("helper_text_terminology_cvs_delimiter")%>
  </div>
  <input type="radio" name="oDelimit" id="idTab" CHECKED="true">
  <label for="idTab">
  <%=bundle.getString("lb_terminology_import_delimiter_tab")%>
  </label>
  <input type="radio" name="oDelimit" id="idSemicolon">
  <label for="idSemicolon">
  <%=bundle.getString("lb_terminology_import_delimiter_semicolon")%>
  </label>
  <input type="radio" name="oDelimit" id="idComma">
  <label for="idComma">
  <%=bundle.getString("lb_terminology_import_delimiter_comma")%>
  </label>
  <input type="radio" name="oDelimit" id="idSpace">
  <label for="idSpace">
  <%=bundle.getString("lb_terminology_import_delimiter_space")%>
  </label>
  <input type="radio" name="oDelimit" id="idOther" onclick="idDelimitText.focus()">
  <label for="idOther">
  <%=bundle.getString("lb_terminology_import_delimiter_other")%>
  </label>
  <input type="text"  name="oDelimitText" id="idDelimitText" size="2"
   onclick="idOther.click()" onfocus="select()"></input>
  <br>
  <LABEL FOR="idIgnoreHeader"><%=bundle.getString("lb_terminology_import_skip_header")%></LABEL>
  <input type="checkbox" name="oIgnoreHeader" id="idIgnoreHeader"></input>
  &nbsp;&nbsp;&nbsp;
  <input type="button" onclick="doReAnalyze();" value="<%=bundle.getString("lb_reanalyze")%>">
  &nbsp;&nbsp;&nbsp;
  <input type="button" onclick="doTestrun();" value="<%=bundle.getString("lb_test_import")%>">
  </span>
</FORM>

<P></P>

<B><%=bundle.getString("lb_column_details")%></B>
<DIV>
<%=bundle.getString("helper_text_column_details")%>
</DIV>
  <TABLE id="idColumns" CLASS="standardText" CELLPADDING=2 CELLSPACING=0>
    <THEAD>
      <TR CLASS="tableHeadingBasic">
        <TD><%=lb_column_header%>&nbsp;&nbsp;&nbsp;&nbsp;</TD>
        <TD><%=lb_data_sample%>&nbsp;&nbsp;&nbsp;&nbsp;</TD>
    <%--<TD>Encoding</TD>--%>
        <TD><%=lb_type%>&nbsp;&nbsp;&nbsp;&nbsp;</TD>
        <TD><%=lb_associated_column%>&nbsp;&nbsp;&nbsp;&nbsp;</TD>
        <TD><%=lb_term_language%>&nbsp;&nbsp;&nbsp;&nbsp;</TD>
        <TD><%=lb_properties%></TD>
      </TR>
    </THEAD>
    <TBODY id="idColumnsBody"/>
  </TABLE>

<P></P>
<DIV>
<INPUT TYPE="BUTTON" NAME="Cancel" VALUE="<%=bundle.getString("lb_cancel")%>"
 onclick="doCancel()">
<INPUT TYPE="BUTTON" NAME="Next" VALUE="<%=bundle.getString("lb_next")%>"
 onclick="doNext()">
</DIV>
<P></P>

</DIV>
</BODY>
</HTML>
