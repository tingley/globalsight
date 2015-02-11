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

String xmlImportOptions =
  (String)sessionMgr.getAttribute(WebAppConstants.TM_IMPORT_OPTIONS);

String urlCancel    = cancel.getPageURL();
String urlNext      = next.getPageURL();
String urlPrev      = prev.getPageURL();
String urlReanalyze = reanalyze.getPageURL();
String urlTestrun   = testrun.getPageURL();

// Perform error handling, then clear out session attribute.
// Done by checkAnalysisError().
sessionMgr.removeElement(WebAppConstants.TM_ERROR);
%>
<HTML XMLNS:gs>
<!-- This is \envoy\administration\projects\importColumns.jsp -->
<HEAD>
<TITLE><%=bundle.getString("lb_terminology_verify_column")%></TITLE>
<STYLE>
/* Note: the font/font size in INPUT and TEXTAREA is different...  */
#idGeneral,
#idColumns   { margin-top: 5pt; }

FORM         { display: inline; }
TEXTAREA     { overflow: auto; }
.header      { font: bold 10pt verdana; color: maroon; }
.link        { color: blue; cursor: hand; margin-bottom: 2pt; }
</STYLE>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>
<%@ include file="/includes/compatibility.jspIncl" %>
<SCRIPT language="Javascript" src="/globalsight/includes/library.js"></SCRIPT>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/jquery/jquery-1.6.4.min.js"></SCRIPT>
<SCRIPT language="Javascript" src="/globalsight/envoy/terminology/management/import.js"></SCRIPT>
<SCRIPT language="Javascript" src="/globalsight/envoy/terminology/management/protocol.js"></SCRIPT>
<SCRIPT language="Javascript" src="/globalsight/envoy/administration/projects/importObjects_js.jsp"></SCRIPT>
<SCRIPT language="Javascript" src="/globalsight/envoy/administration/projects/importColumns_js.jsp"></SCRIPT>
<SCRIPT LANGUAGE="JavaScript">
var needWarning = false;
var objectName = "";
var guideNode = "projects";
var helpFile = "<%=bundle.getString("help_project_schedule_importcolumns")%>";
var xmlImportOptions = '<%=xmlImportOptions.replace("\\", "\\\\")%>';

function Result(message, element)
{
    this.message = message;
    this.element = element;
    this.dom = null;
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
        "&" + WebAppConstants.TM_ACTION +
        "=" + WebAppConstants.TM_ACTION_SET_IMPORT_OPTIONS%>";

      oForm.importoptions.value = getDomString(result.dom);
      
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
        "&" + WebAppConstants.TM_ACTION +
        "=" + WebAppConstants.TM_ACTION_SET_IMPORT_OPTIONS%>";

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
        "&" + WebAppConstants.TM_ACTION +
        "=" + WebAppConstants.TM_ACTION_ANALYZE_FILE%>";

      oForm.importoptions.value = getDomString(result.dom);
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
        "&" + WebAppConstants.TM_ACTION +
        "=" + WebAppConstants.TM_ACTION_TEST_IMPORT%>";

      oForm.importoptions.value = getDomString(result.dom);
      
      oForm.submit();
    }
}

function buildOptions()
{
    var result = new Result("", null);
	var dom = $.parseXML(xmlImportOptions);
    var node;

    node = $(dom).find("importOptions columnOptions");
    var len = node.children().length;
    while(len > 0)
    {
   		node.children().eq(0).remove();
   		len = node.children().length;
    }
    
    for (i = 0; i < aColumns.length; ++i)
    {
        var oCol = aColumns[i];

        var id = dom.createAttribute("id");
        var elem = dom.createElement("column");
        node.append(elem);
        var len = $(node).find("column").length;
        elem = $(node).find("column").eq(len-1);
        
        var name = dom.createElement("name");
        var example = dom.createElement("example");
        var type = dom.createElement("type");
        var subtype = dom.createElement("subtype");

        elem.append(name);
        elem.append(example);
        elem.append(type);
        elem.append(subtype);
        
        $(elem).attr("id",oCol.id);
        $(elem).find("name").text(oCol.name);
        $(elem).find("example").text(oCol.example);
        $(elem).find("type").text(oCol.type);
        $(elem).find("subtype").text(oCol.subtype);
    }

    result.dom = dom;
    return result;
}

function buildFileOptions()
{
    var result = new Result("", null);
	var dom = $.parseXML(xmlImportOptions);
    var node;

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
            "<%=EditUtil.toJavascript(bundle.getString("jsmsg_tb_import_missing_separator_character"))%>",
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

    result.dom = dom;
    return result;
}

function parseColumns()
{
    var dom = $.parseXML(xmlImportOptions);
    var nodes, node, id, name, example, type, subtype;

    nodes = $(dom).find("importOptions columnOptions column");

    for (i = 0; i < nodes.length; ++i)
    {
        node = nodes[i];//node = nodes.item(i);

        id = $(node).attr("id");

        if($(node).find("name"))
        {
            name = $(node).find("name").text();
        }
        if($(node).find("example"))
        {
            example = $(node).find("example").text();
        }
        if($(node).find("type"))
        {
            type = $(node).find("type").text();
        }
        if($(node).find("subtype"))
        {
            subtype = $(node).find("subtype").text();
        }

        aColumns.push(new Column(id, name, example, type, subtype));
    }

    showColumns();
}

function checkAnalysisError()
{
    var dom = $.parseXML(xmlImportOptions);
    
    var node = $(dom).find("importOptions fileOptions errorMessage");
    var errorMessage = node.text();
    if (errorMessage != "")
    {
        node.text("");

        showWarning("<%=EditUtil.toJavascript(bundle.getString("jsmsg_tb_import_failed_analyze"))%>" + errorMessage);

        oForm.action = "<%=urlPrev +
          "&" + WebAppConstants.TM_ACTION +
          "=" + WebAppConstants.TM_ACTION_SET_IMPORT_OPTIONS%>";

        oForm.importoptions.value = getDomString(dom);
        oForm.submit();
        return true;
    }

    return false;
}

function parseFileOptions()
{
    var form = document.oDummyForm;
    var dom = $.parseXML(xmlImportOptions);
    
    var nodes, node, fileName, fileType, fileEncoding;
    var separator, ignoreHeader;

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

    parseFileOptions();
    parseColumns();

    if (aColumns.length <= 1)
    {
       var message = "<%=EditUtil.toJavascript(bundle.getString("jsmsg_tb_import_detected_one_col_or_less"))%>";
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
<BR>
<TABLE CELLPADDING=0 CELLSPACING=0 BORDER=0 CLASS=standardText>
<TR>
<TD WIDTH=500>
<%=bundle.getString("helper_text_terminology_import_columns")%>
</TD>
</TR>
</TABLE>
<BR>

<DIV style="display:none">
<XML id="oImportOptions"><%=xmlImportOptions%></XML>
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

<P>

<B><%=bundle.getString("lb_column_details")%></B>
<DIV>
<%=bundle.getString("helper_text_column_details")%>
</DIV>
  <TABLE id="idColumns" CLASS="standardText" CELLPADDING=2 CELLSPACING=0>
    <THEAD>
      <TR CLASS="tableHeadingBasic">
        <TD><%=bundle.getString("lb_column_header")%>&nbsp;&nbsp;&nbsp;&nbsp;</TD>
        <TD><%=bundle.getString("lb_data_sample")%>&nbsp;&nbsp;&nbsp;&nbsp;</TD>
        <TD><%=bundle.getString("lb_type")%>&nbsp;&nbsp;&nbsp;&nbsp;</TD>
        <TD>Subtype&nbsp;&nbsp;&nbsp;&nbsp;</TD>
        <TD><%=bundle.getString("lb_properties")%></TD>
      </TR>
    </THEAD>
    <TBODY id="idColumnsBody"/>
  </TABLE>

<BR>
<DIV>
<INPUT TYPE="BUTTON" NAME="Cancel" VALUE="<%=bundle.getString("lb_cancel")%>"
 onclick="doCancel()">
<INPUT TYPE="BUTTON" NAME="Next" VALUE="<%=bundle.getString("lb_next")%>"
 onclick="doNext()">
</DIV>
<BR>

</DIV>
</BODY>
</HTML>
