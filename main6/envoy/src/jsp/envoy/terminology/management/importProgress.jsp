<%@ page
    contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="java.util.*,com.globalsight.everest.webapp.webnavigation.LinkHelper,
        java.util.ResourceBundle,
        java.text.MessageFormat,
        com.globalsight.util.edit.EditUtil,
        com.globalsight.everest.webapp.pagehandler.PageHandler,
        com.globalsight.everest.webapp.WebAppConstants,
        com.globalsight.everest.servlet.util.SessionManager,
        java.io.IOException"
    session="true"
%>
<%@ include file="/envoy/common/header.jspIncl" %>
<jsp:useBean id="ok" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean"/>
<jsp:useBean id="cancel" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean"/>
<jsp:useBean id="refresh" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean"/>
<%
ResourceBundle bundle = PageHandler.getBundle(session);
SessionManager sessionMgr = (SessionManager)session.getAttribute(
  WebAppConstants.SESSION_MANAGER);

String urlOk = ok.getPageURL();
String urlRefresh = refresh.getPageURL() +
    "&" + WebAppConstants.TERMBASE_ACTION +
    "=" + WebAppConstants.TERMBASE_ACTION_REFRESH;
String urlCancel = cancel.getPageURL() +
    "&" + WebAppConstants.TERMBASE_ACTION +
    "=" + WebAppConstants.TERMBASE_ACTION_CANCEL_IMPORT;
String pageURLCancel = cancel.getPageURL();

String str_termbaseName =
  (String)sessionMgr.getAttribute(WebAppConstants.TERMBASE_TB_NAME);
String str_termbaseId =
  (String)sessionMgr.getAttribute(WebAppConstants.TERMBASE_TB_ID);
String xmlImportOptions =
  (String)sessionMgr.getAttribute(WebAppConstants.TERMBASE_IMPORT_OPTIONS);

Object[] args = { str_termbaseName };
MessageFormat format = new MessageFormat(
  bundle.getString("lb_import_into_termbase"));
String lb_import_into_termbase = format.format(args);
String lb_failed_analysis = bundle.getString("jsmsg_tb_import_failed_analyze");
String lb_copiedToClipboard = bundle.getString("jsmsg_messages_copied_to_clipboard");
String lb_browseTermbase = bundle.getString("lb_browse_termbase");
String lb_OK = bundle.getString("lb_ok");

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
%>
<HTML>
<!-- This is envoy\terminology\management\importProgress.jsp -->
<HEAD>
<TITLE><%=bundle.getString("lb_import_progress")%></TITLE>
<STYLE>
#idProgressContainer { border: solid 1px <%=skin.getProperty("skin.list.borderColor")%>; z-index: 1; 
                 position: absolute; top: 42; left: 20; width: 400; }
#idProgressBar { background-color: #a6b8ce; z-index: 0;
                 border: solid 1px <%=skin.getProperty("skin.list.borderColor")%>; 
                 position: absolute; top: 42; left: 20; width: 0; }
#idProgress    { text-align: center; z-index: 2; font-weight: bold; }
#idMessagesHeader { position: absolute; top: 72; left: 20; font-weight: bold;}
#idMessages    { position: absolute; overflow: auto; z-index: 0; 
                 top: 102; left: 20; height: 80; width: 400; }
#idLinks       { position: absolute; left: 20; top: 314; z-index: 1; }
</STYLE>
<SCRIPT SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>
<%@ include file="/includes/compatibility.jspIncl" %>
<SCRIPT SRC="/globalsight/includes/library.js"></SCRIPT>
<SCRIPT SRC="envoy/terminology/management/protocol.js"></SCRIPT>
<SCRIPT SRC="envoy/terminology/viewer/viewerAPI.js"></SCRIPT>
<SCRIPT>
var needWarning = false;
var objectName = "";
var guideNode = "terminology";
var helpFile = "<%=bundle.getString("help_termbase_import_importing")%>";

var WIDTH = 400;
var xmlImportOptions = 
	'<%=xmlImportOptions.replace("\\", "\\\\").replace("\r","").replace("\n","").replace("'", "\\'").trim()%>';

eval("<%=errorScript%>");

function showMessage(message)
{
    var div = document.createElement("DIV");
    div.innerHTML = message;//div.innerText = message;
    idMessages.appendChild(div);

    if (idMessages.style.pixelHeight < 80)
    {
      idMessages.style.pixelHeight = 80;
    }

    idMessages.style.pixelHeight += 40;

    if (idMessages.style.pixelHeight > 200)
    {
      idMessages.style.pixelHeight = 200;
    }

    div.scrollIntoView(false);
}

function showProgress(entryCount, percentage, message)
{
  // idProgress.innerText = entryCount.toString(10) + " <%=bundle.getString("lb_entries").toLowerCase(Locale.ENGLISH) %> (" +
  idProgress.innerHTML = entryCount.toString(10) + " <%=bundle.getString("lb_entries").toLowerCase(Locale.ENGLISH) %> (" +
    percentage.toString(10) + "%)";

  //idProgressBar.style.pixelWidth = Math.round((percentage / 100) * WIDTH);
  idProgressBar.style.width = Math.round((percentage / 100) * WIDTH);
  if(window.navigator.userAgent.indexOf("Firefox")>0)
  {
    idProgressBar.style.minHeight = '15px';
	idProgressBar.innerHTML='&nbsp';    
  }
  
  if (message != null && message != "")
  {
    showMessage(message);
  }
}

function copyToClipboard()
{
  var range = document.body.createTextRange();
  range.moveToElementText(idMessages);
  window.clipboardData.setData("Text", range.text);
  window.status = "<%=EditUtil.toJavascript(lb_copiedToClipboard)%>";
}

function doOk()
{
    window.location.href = "<%=urlOk%>";
}

function doCancel()
{
    window.location.href = "<%=urlCancel%>";
}

function doRefresh()
{
    idFrame.document.location = idFrame.document.location;
}

function showTermbase()
{
  ShowTermbase('<%=str_termbaseId%>');
}

function checkAnalysisError()
{
    var dom;
    if(window.navigator.userAgent.indexOf("MSIE")>0)
    {
      dom = oImportOptions.XMLDocument;
    }
    else if(window.DOMParser)
    { 
      var parser = new DOMParser();
      dom = parser.parseFromString(xmlImportOptions,"text/xml");
    }
    var node = dom.selectSingleNode("/importOptions/fileOptions/errorMessage");
    var errorMessage = node.text;
    if (errorMessage != "")
    {
        node.text = "";

        showWarning("<%=EditUtil.toJavascript(lb_failed_analysis)%>" +
          errorMessage);

        oForm.action = "<%=pageURLCancel +
          "&" + WebAppConstants.TERMBASE_ACTION +
          "=" + WebAppConstants.TERMBASE_ACTION_SET_IMPORT_OPTIONS%>";
          
        if(window.navigator.userAgent.indexOf("MSIE")>0)
        {
      		oForm.importoptions.value = oImportOptions.xml;
        }
        else if(window.DOMParser)
        { 
          	oForm.importoptions.value = XML.getDomString(result.domImportOptions);
        }
        oForm.submit();
        return true;
    }

    return false;
}

function doOnLoad()
{
    loadGuides();
    if (checkAnalysisError())
    {
        return;
    }
}
function done()
{
  document.getElementById("idPleaseWait").style.visibility = 'hidden';

  document.getElementById("idCancelOk").value = "<%=lb_OK%>";
  document.getElementById("idCancelOk").onclick = doOk;

  document.getElementById("idRefreshResult").value = "<%=lb_browseTermbase%>";
  document.getElementById("idRefreshResult").onclick = showTermbase;
  document.getElementById("idRefreshResult").focus();
}
</SCRIPT>
</HEAD>
<BODY LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0" MARGINWIDTH="0"
  MARGINHEIGHT="0" CLASS="standardText" oncontextmenu="return false"
  ONLOAD="doOnLoad()">
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>

<DIV ID="contentLayer" 
 STYLE=" POSITION: ABSOLUTE; Z-INDEX: 8; TOP: 108px; LEFT: 20px; RIGHT: 20px;">
    
<SPAN CLASS="mainHeading" id="idHeading"><%=lb_import_into_termbase%></SPAN>
<BR>
<SPAN CLASS="standardTextItalic" id="idPleaseWait">
  <%=bundle.getString("msg_please_wait_untill_import_finished")%>
</SPAN>
<P></P>

<DIV id="idProgressContainer">
  <DIV id="idProgress">0%</DIV>
</DIV>
<DIV id="idProgressBar"></DIV>

<BR>
<DIV id="idMessagesHeader" class="header"><%=bundle.getString("lb_import_msg")%></DIV>
<DIV id="idMessages"></DIV>

<DIV id="idLinks" style="width:500px">
  <INPUT TYPE="BUTTON" VALUE="<%=bundle.getString("lb_cancel")%>"
   id="idCancelOk" onclick="doCancel()"> &nbsp;
  <INPUT TYPE="BUTTON" VALUE="<%=bundle.getString("lb_refresh")%>"
   id="idRefreshResult" onclick="doRefresh()">
</DIV>

<IFRAME id="idFrame" src="<%=urlRefresh%>" style="display:none"></IFRAME>

<p/><p/><p/><p/>
<XML id="oImportOptions" style="display:none"><%=xmlImportOptions%></XML>

<FORM NAME="oForm" ACTION="" METHOD="post">
<INPUT TYPE="hidden" NAME="importoptions"
 VALUE="ImportOptions XML goes here"></INPUT>
</FORM>
</DIV>
</BODY>
</HTML>
