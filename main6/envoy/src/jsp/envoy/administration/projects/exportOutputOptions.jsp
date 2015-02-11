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

String xmlExportOptions =
  (String)sessionMgr.getAttribute(WebAppConstants.TM_EXPORT_OPTIONS);
String projectName =
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

String lb_title = "Project Schedule Output Options";
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
<SCRIPT language="Javascript" src="/globalsight/includes/library.js"></SCRIPT>
<!-- To get showError and showWarning functions -->
<SCRIPT language="Javascript" src="/globalsight/envoy/tm/management/protocol.js"></SCRIPT>
<SCRIPT LANGUAGE="JavaScript">
var needWarning = false;
var objectName = "";
var guideNode = "projects";
var helpFile = "<%=bundle.getString("help_project_schedule_exportoptions")%>";
var xmlExportOptions = '<%=xmlExportOptions.trim()%>';
</SCRIPT>
<SCRIPT language="Javascript">
eval("<%=errorScript%>");

// no space allowed as separator
var Delimitor      = new Array();
Delimitor['tab']   = 0;
Delimitor[';']     = 1;
Delimitor[',']     = 2;
Delimitor['other'] = 3;

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
  var dom = $.parseXML(xmlExportOptions);
  var node,check;

  // OUTPUT OPTIONS
  node = $(dom).find("exportOptions outputOptions");

  if (form.oHeader.checked)
  {
     $(node).find("header").text("true");
  }
  else
  {
     $(node).find("header").text("false");
  }

  var value = '';
  for (var key in Delimitor)
  {
     // IE 5.0 adds prototype functions as keys
     if (key == 'shift' || key == 'unshift' || key == 'splice' ||
         key == 'push' || key == 'pop')
     {
        continue;
     }

     var index = Delimitor[key];
     if (form.oDelimit[index].checked)
     {
        value = key;
        break;
     }
  }

  if (value == 'other')
  {
      value = form.oDelimitText.value;

      if (value == "")
      {
        return new Result(
          "<%=EditUtil.toJavascript(bundle.getString("jsmsg_tb_import_missing_separator"))%>",
          "<%=EditUtil.toJavascript(bundle.getString("jsmsg_tb_import_specify_separator"))%>",
          form.oDelimitText);
      }
  }

  if (value == "space" || value.startsWith(" "))
  {
    return new Result(
      "The separator cannot be a space character", "", form.oDelimitText);
  }

  $(node).find("separator").text(value);

  var dateformat = form.oDateFormat.options[form.oDateFormat.selectedIndex].value;
  $(node).find("subtype").text(dateformat);

  result.dom = dom;
  return result;
}

function parseExportOptions()
{
  var form = document.oDummyForm;
  var dom = $.parseXML(xmlExportOptions);
  var nodes, node,header,separator;

  node = $(dom).find("exportOptions outputOptions");
  header = $(node).find("header").text();
  separator = $(node).find("separator").text();

  checkValue(form.oHeader, header);

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

<%--
  var count = dom.selectSingleNode("/exportOptions/fileOptions/entryCount").text;
  idEntryCount.innerText = count;

  if (parseInt(count) == 0)
  {
    idWarning.style.display = '';
  }
--%>
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
    oForm.exportoptions.value = getDomString(result.dom);

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

<XML id="oExportOptions" style="display:none"><%=xmlExportOptions%></XML>

<DIV ID="contentLayer"
 STYLE=" POSITION: ABSOLUTE; Z-INDEX: 9; TOP: 108px; LEFT: 20px; RIGHT: 20px;">
<DIV CLASS="mainHeading" ID="idHeading"><%=lb_title%></DIV>
<BR>

<%--
<DIV id="idWarning" style="color: red; display: none">
Note: no data found, nothing to export.
<BR><BR>
</DIV>

<DIV><%=bundle.getString("lb_tm_expected_no_of_entries_to_be_exported")%>
<SPAN id="idEntryCount" style="font-weight: bold"></SPAN>.</DIV>
--%>

<FORM NAME="oDummyForm">

<div style="margin-bottom:10px">
<label for="idHeader">Write header line containing column names:</label>
<INPUT type="checkbox" name="oHeader" id="idHeader"></INPUT>
</div>

<div style="margin-bottom:10px">
<%=bundle.getString("lb_terminology_col_delimiter")%>
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
<%--
  <input type="radio" name="oDelimit" id="idSpace">
  <label for="idSpace">
  <%=bundle.getString("lb_terminology_import_delimiter_space")%>
  </label>
--%>
  <input type="radio" name="oDelimit" id="idOther" onclick="idDelimitText.focus()">
  <label for="idOther">
  <%=bundle.getString("lb_terminology_import_delimiter_other")%>
  </label>
  <input type="text"  name="oDelimitText" id="idDelimitText" size="2"
   onclick="idOther.click()" onfocus="select()"></input>
</div>

<div style="margin-bottom:10px">
<label for="idDateFormat">Date format:</label>
<select name="oDateFormat" id="idDateFormat">
  <option value="MM/dd/yyyy">MM/dd/yyyy</option>
  <option value="MM/dd/yy">MM/dd/yy</option>
  <option value="dd/MM/yyyy">dd/MM/yyyy</option>
  <option value="dd/MM/yy">dd/MM/yy</option>
</select>
</div>

<BR>
</FORM>

<FORM NAME="oForm" ACTION="" METHOD="post">
<INPUT TYPE="hidden" NAME="exportoptions"
 VALUE="ExportOptions XML goes here"></INPUT>

<DIV id="idButtons" align="left">
<INPUT TYPE=BUTTON  TABINDEX="0" onclick="doCancel();"   value="<%=bundle.getString("lb_cancel")%>">&nbsp;
<INPUT TYPE=BUTTON  TABINDEX="0" onclick="doPrevious();" value="<%=bundle.getString("lb_previous")%>">&nbsp;
<INPUT TYPE=BUTTON  TABINDEX="0" onclick="doNext();"     value="<%=bundle.getString("lb_export1")%>">
</DIV>

</FORM>

</BODY>
</HTML>
