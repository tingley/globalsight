<%@ page
    contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="java.util.*,com.globalsight.everest.webapp.webnavigation.LinkHelper,
        java.util.ResourceBundle,
        com.globalsight.everest.projecthandler.exporter.ExportOptions,
        com.globalsight.util.edit.EditUtil,
        com.globalsight.everest.servlet.util.SessionManager,
        com.globalsight.everest.webapp.pagehandler.PageHandler,
        com.globalsight.everest.webapp.WebAppConstants"
    session="true"
%>
<%--
State machinery for export:

export --> exportFileOptions --> exportOutputOptions --> exportProgress

export.jsp sets <selectOptions>
exportFileOptions sets <fileOptions>
exportOutputOptions.jsp sets <outputOptions>
exportProgress.jsp runs the export and allows to download result file

--%>
<jsp:useBean id="next" scope="request"
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
Object[] projectUserNames = 
  (Object[])sessionMgr.getAttribute(WebAppConstants.USER_NAMES);

StringBuffer str_usersArray = new StringBuffer();
  
for (int i = 0, max = projectUserNames.length; i < max; i++)
{
  String userName = (String) projectUserNames[i];
  str_usersArray.append("users[");
  str_usersArray.append(i);
  str_usersArray.append("] = \"");
  str_usersArray.append(EditUtil.toJavascript(userName));
  str_usersArray.append("\";");
}
  
String urlNext   = next.getPageURL();
String urlCancel = cancel.getPageURL();

String lb_title = "Project Schedule Export";
%>
<HTML XMLNS:gs>
<!-- This is envoy\administration\projects\export.jsp -->
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
<!-- To get Encodings of output files -->
<SCRIPT language="Javascript" src="/globalsight/envoy/terminology/management/import.js"></SCRIPT>
<!-- To get showError and showWarning functions -->
<SCRIPT language="Javascript" src="/globalsight/envoy/tm/management/protocol.js"></SCRIPT>
<SCRIPT LANGUAGE="JavaScript">
var needWarning = false;
var objectName = "";
var guideNode = "projects";
var helpFile = "<%=bundle.getString("help_project_schedule_export")%>";
var xmlExportOptions = '<%=xmlExportOptions.trim()%>';

var users = new Array();
eval("<%=EditUtil.toJavascript(str_usersArray.toString())%>");

function Result(message, description, element)
{
    this.message = message;
    this.description = description;
    this.element = element;
    this.dom = null;
}

function parseExportOptions()
{
  var form = document.oDummyForm;
  var dom = $.parseXML(xmlExportOptions);
  var nodes, node;
  var selectMode, selectFilter, fileType, fileEncoding;

  node = $(dom).find("exportOptions selectOptions");
  selectMode = $(node).find("selectMode").text();
  selectFilter = $(node).find("selectFilter").text();

  node = $(dom).find("/exportOptions/fileOptions");
  fileType = $(node).find("fileType").text();
  fileEncoding = $(node).find("fileEncoding").text();

  if (selectMode == "<%=ExportOptions.SELECT_FILTERED%>")
  {
    form.oEntries[1].checked = true;
  }

  selectValue(form.oEncoding, fileEncoding);

<%--
  if (fileType == "<%=ExportOptions.TYPE_CSV%>")
  {
    form.oType[0].click();
  }
  else if (fileType == "<%=ExportOptions.TYPE_XML%>")
  {
    form.oType[1].click();
  }
--%>
}

function buildExportOptions()
{
  var result = new Result("", "", null);
  var form = document.oDummyForm;
  var dom = $.parseXML(xmlExportOptions);
  var node,sel;

  // SELECT OPTIONS
  node = $(dom).find("exportOptions selectOptions");

  if (form.oEntries[0].checked)
  {
     $(node).find("selectMode").text("<%=ExportOptions.SELECT_ALL%>");
  }
  else
  {
	  $(node).find("selectMode").text("<%=ExportOptions.SELECT_FILTERED%>");
     var val = form.oUser;
     $(node).find("selectFilter").text(val.options[val.selectedIndex].value);
  }

  // FILE OPTIONS
  node = $(dom).find("exportOptions fileOptions");

  $(node).find("fileType").text("<%=ExportOptions.TYPE_CSV%>");

<%--
  if (form.oType[0].checked)
  {
    node.selectSingleNode("fileType").text =
       "<%=ExportOptions.TYPE_CSV%>";
  }
  else if (form.oType[1].checked)
  {
    node.selectSingleNode("fileType").text =
       "<%=ExportOptions.TYPE_XML%>";
  }
--%>

  var sel = form.oEncoding;
  $(node).find("fileEncoding").text(sel.options[sel.selectedIndex].value);

  result.dom=dom;
  return result;
}

function doCancel()
{
    window.navigate("<%=urlCancel%>");
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
        var url = "<%=urlNext +
            "&" + WebAppConstants.TM_ACTION +
            "=" + WebAppConstants.TM_ACTION_ANALYZE_TM%>";

        oForm.action = url;
        oForm.exportoptions.value = getDomString(result.dom);
        
        oForm.submit();
    }
}

function doAllUsers()
{
  document.oDummyForm.oUser.disabled = true;
}

function doByUser()
{
  document.oDummyForm.oEntries[1].click();
  document.oDummyForm.oUser.disabled = false;
}

function doTypeChanged()
{
  var form = document.oDummyForm;
  var select = form.oEncoding;

  // XML uses UTF-8. Everything else can select encoding.
  if (form.oType[0].checked)
  {
    selectValue(select, "UTF-8")
    select.disabled = true;
  }
  else
  {
    select.disabled = false;
  }
}

function selectValue(select, value)
{
  for (i = 0; i < select.options.length; ++i)
  {
    if (select.options[i].value == value)
    {
      select.selectedIndex = i;
      return;
    }
  }
}

function fillUsers()
{
  for (var i = 0; i < users.length; ++i)
  {
    var user = users[i];

    oOption = document.createElement("OPTION");
    oOption.text = user;
    oOption.value = user;
    oDummyForm.oUser.add(oOption);
  }
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
   fillUsers();

   parseExportOptions();

   selectValue(oDummyForm.oEncoding, "UTF-8");
}
</SCRIPT>
</HEAD>
<BODY LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0" MARGINWIDTH="0"
 MARGINHEIGHT="0" CLASS="standardText" onload="doOnLoad()">
<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>

<DIV style="display:none">
<XML id="oExportOptions"><%=xmlExportOptions%></XML>
</DIV>

<DIV ID="contentLayer"
 STYLE=" POSITION: ABSOLUTE; Z-INDEX: 9; TOP: 108px; LEFT: 20px; RIGHT: 20px;">
<DIV CLASS="mainHeading" ID="idHeading"><%=lb_title%></DIV>
<BR>

<FORM NAME="oDummyForm">

<div style="margin-bottom:10px">Select users whose schedules should be exported:<BR>
<div style="margin-left: 40px">
<input type="radio" name="oEntries" id="idByUser1" CHECKED
  onclick="doAllUsers()">
  <label for="idByUser1">All Users</label>
<br>
<input type="radio" name="oEntries" id="idByUser2"
  onclick="idUserList.disabled = false; idUserList.focus();">
  <label for="idByUser2">Only user</label>
  <select name="oUser" id="idUserList" disabled
    onchange="doByUser()"></select>
<br>
</div>
</div>

<BR>
<div style="margin-bottom:10px">
<TABLE CELLPADDING=2 CELLSPACING=2 BORDER=0 CLASS=standardText>
  <TR VALIGN="TOP">
    <TD WIDTH=100><%=bundle.getString("lb_terminology_export_format")%></TD>
    <TD>
      <B>CSV</B>
<%--
      <input type="radio" name="oType" id="idCsv" CHECKED="true"
      onclick="doTypeChanged()"><label for="idCsv">CSV</label>
      <input type="radio" name="oType" id="idXml" 
      onclick="doTypeChanged()"><label for="idXml">XML</label><BR>
--%>
    </TD>
  </TR>
  <TR>
    <TD>
      <%=bundle.getString("lb_file_encoding")%>
    </TD>
    <TD>
      <SELECT name="oEncoding" id="idEncoding"></SELECT>
    </TD>
  </TR>
</TABLE>

<BR>
</FORM>

<FORM NAME="oForm" ACTION="" METHOD="post">
<INPUT TYPE="hidden" NAME="exportoptions"
 VALUE="ExportOptions XML goes here"></INPUT>

<DIV id="idButtons" align="left">
<button TABINDEX="0" onclick="doCancel();"><%=bundle.getString("lb_cancel")%></button>&nbsp;
<button TABINDEX="0" onclick="doNext();"><%=bundle.getString("lb_next")%></button>
</DIV>
</FORM>

</BODY>
</HTML>
