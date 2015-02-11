<%@ page
    contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="java.util.*,com.globalsight.everest.webapp.webnavigation.LinkHelper,
        java.util.ResourceBundle,
        java.util.ArrayList,
        com.globalsight.util.edit.EditUtil,
        com.globalsight.everest.tm.importer.ImportOptions,
        com.globalsight.everest.servlet.util.SessionManager,
        com.globalsight.everest.webapp.pagehandler.PageHandler,
        com.globalsight.everest.webapp.WebAppConstants,
        com.globalsight.everest.aligner.AlignerPackageUploadOptions"
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

AlignerPackageUploadOptions gapOptions = (AlignerPackageUploadOptions)
  sessionMgr.getAttribute(WebAppConstants.GAP_OPTIONS);

ArrayList tms = (ArrayList)sessionMgr.getAttribute(WebAppConstants.GAP_TMS);

String urlNext   = next.getPageURL();
String urlCancel = cancel.getPageURL();

String lb_title = bundle.getString("lb_aligner_package_upload");
String lb_helptext = bundle.getString("helper_text_aligner_package_upload");
String lb_selectImportFile = bundle.getString("lb_select_import_file");
%>
<HTML>
<HEAD>
<TITLE><%=lb_title%></TITLE>
<SCRIPT SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>
<%@ include file="/includes/compatibility.jspIncl" %>
<SCRIPT SRC="/globalsight/includes/library.js"></SCRIPT>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/jquery/jquery-1.6.4.min.js"></SCRIPT>
<SCRIPT SRC="/globalsight/envoy/terminology/management/protocol.js"></SCRIPT>
<SCRIPT>
var needWarning = false;
var objectName = "";
var guideNode = "aligner";
var helpFile = "<%=bundle.getString("help_align_uploadPackage")%>";
var xmlStr = '<%=gapOptions.getXml().replace("\\", "\\\\").trim()%>';

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

    var colls = [];
	colls = document.getElementById("tmName").options;
	if(colls.length==0)
	{
		return;
	}
    var result = buildOptions();

    if (result.message != null && result.message != "")
    {
        showError(result);
        result.element.focus();
    }
    else
    {
        var url = "<%=urlNext +
            "&" + WebAppConstants.GAP_ACTION +
            "=" + WebAppConstants.GAP_ACTION_UPLOADPACKAGE%>";

        oForm.action = url;
       
        oForm.<%=WebAppConstants.GAP_OPTIONS%>.value = getDomString(result.dom);
        oForm.submit();
    }
}

function buildOptions()
{
    var result = new Result("", "", null);
    var dom = $.parseXML(xmlStr);
    var node;

    node = $(dom).find("fileOptions");
    $(node).find("fileName").text(oForm.filename.value);

    var sel = oDummyForm.tmName;
    node = $(dom).find("tmOptions");
    $(node).find("tmName").text(sel.options[sel.selectedIndex].value);

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

function selectValue(select, value)
{
  for (var i = 0; i < select.options.length; ++i)
  {
    if (select.options[i].value == value)
    {
      select.selectedIndex = i;
      return;
    }
  }
}

function parseOptions()
{
    var form = document.oDummyForm;
    var dom = $.parseXML(xmlStr);
    var nodes, node, fileName, tmName, syncMode;

    node = $(dom).find("fileOptions");
    fileName = $(node).find("fileName").text();

    node = $(dom).find("tmOptions");
    tmName = $(node).find("tmName").text();
    syncMode = $(node).find("syncMode").text();

    document.oForm.filename.value = fileName;
    selectValue(oDummyForm.tmName, tmName);
}

function doOnLoad()
{
    // Load the Guides
    loadGuides();

    parseOptions();

    oForm.filename.focus();
}
</SCRIPT>
</HEAD>
<BODY LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0" MARGINWIDTH="0"
 MARGINHEIGHT="0" CLASS="standardText" onload="doOnLoad()">
<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>
<DIV ID="contentLayer"
    STYLE=" POSITION: ABSOLUTE; Z-INDEX: 9; TOP: 108px; LEFT: 20px; RIGHT: 20px;">

<DIV CLASS="mainHeading" ID="idHeading"><%=lb_title%></DIV>
<BR>

<TABLE CELLPADDING=0 CELLSPACING=0 BORDER=0 CLASS=standardText>
  <TR>
    <TD WIDTH=500><%=lb_helptext%></TD>
  </TR>
</TABLE>
<BR>

<XML id="oOptions" style="display:none"><%=gapOptions.getXml()%></XML>

<%-- We wrap the form around the file and xml options only. --%>
<FORM NAME="oForm" ACTION="" ENCTYPE="multipart/form-data" METHOD="post">
<INPUT TYPE="hidden" NAME="<%=WebAppConstants.GAP_OPTIONS%>" VALUE="">

<DIV style="margin-bottom: 12px"><%=lb_selectImportFile%><BR>
<INPUT TYPE="file" NAME="filename" id="idFilename" SIZE="40">
</DIV>
</FORM>

<BR>

<FORM NAME="oDummyForm">
<DIV class="standardText"><%=bundle.getString("lb_select_tm_to_import_into") %>:
<select name="tmName" id="tmName">
  <%
  for (int i = 0, max = tms.size(); i < max; i++)
  {
    String name = (String)tms.get(i);
  
    out.print("<OPTION value='");
    out.print(name);
    out.print("'>");
    out.print(name);
    out.println("&nbsp;&nbsp;</OPTION>");
  }
  %>
</select>
</DIV>
</FORM>

<DIV id="idButtons">
<INPUT TYPE="BUTTON" VALUE="<%=bundle.getString("lb_cancel")%>"
 onclick="doCancel()">
<INPUT TYPE="BUTTON" VALUE="<%=bundle.getString("lb_upload_package") %>"
 onclick="doNext()">
</DIV>

</DIV>

</BODY>
</HTML>
