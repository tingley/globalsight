<%@ page 
    contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="java.util.*,com.globalsight.everest.webapp.webnavigation.LinkHelper,
        com.globalsight.util.progress.IProcessStatusListener,
        com.globalsight.util.progress.ProcessStatus,
        com.globalsight.terminology.searchreplace.SearchReplaceParams,
        com.globalsight.terminology.searchreplace.SearchResults,
        java.util.ResourceBundle,
        java.text.MessageFormat,
        com.globalsight.util.edit.EditUtil,
        com.globalsight.everest.webapp.pagehandler.PageHandler,
        com.globalsight.everest.webapp.WebAppConstants,
        com.globalsight.everest.servlet.util.SessionManager,
        java.io.IOException,
        java.rmi.Remote,
        java.rmi.RemoteException"
    session="true"
%>
<%@ include file="/envoy/common/header.jspIncl" %>
<jsp:useBean id="again" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean"/>
<jsp:useBean id="cancel" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean"/>
<jsp:useBean id="done" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean"/>
<jsp:useBean id="refresh" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean"/>
<%
ResourceBundle bundle = PageHandler.getBundle(session);
SessionManager sessionMgr = (SessionManager)session.getAttribute(
  WebAppConstants.SESSION_MANAGER);
String urlRefresh = refresh.getPageURL() +
    "&" + WebAppConstants.TERMBASE_ACTION +
    "=" + WebAppConstants.TERMBASE_ACTION_REFRESH;

String urlAgain = again.getPageURL();
String urlCancel = cancel.getPageURL();
String urlDone = done.getPageURL();

String str_termbaseName =
  (String)sessionMgr.getAttribute(WebAppConstants.TERMBASE_TB_NAME);
String str_termbaseId =
  (String)sessionMgr.getAttribute(WebAppConstants.TERMBASE_TB_ID);

Object[] args = { str_termbaseName };
MessageFormat format = new MessageFormat(bundle.getString("lb_termbase_replace_in"));
String lb_replacing_termbase = format.format(args);

String lb_copiedToClipboard = bundle.getString("jsmsg_messages_copied_to_clipboard");
String lb_done = bundle.getString("lb_done");
String lb_cancel = bundle.getString("lb_cancel");
String lb_again = bundle.getString("lb_search_again");
String lb_replace_msg = bundle.getString("lb_replace_msg");
String lb_please_wait = bundle.getString("lb_pls_wait");

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

// TODO: remove other params from sessionMgr.
%>
<HTML>
<HEAD>
<TITLE><%=lb_replacing_termbase%></TITLE>
<SCRIPT SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>
<SCRIPT src="/globalsight/includes/library.js"></SCRIPT>
<SCRIPT SRC="envoy/terminology/management/protocol.js"></SCRIPT>
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
#idCancel      { position: absolute; left: 20; top: 314; z-index: 1; }
</STYLE>
<SCRIPT language="Javascript">
var needWarning = false;
var objectName = "";
var guideNode = "terminology";
var helpFile = "<%=bundle.getString("help_termbase_maintenance_searching")%>";

var WIDTH = 400;

eval("<%=errorScript%>");

function showMessage(message)
{
    var div = document.createElement("DIV");
    div.innerText = message;
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
  idProgress.innerText = entryCount.toString(10) + " replacements (" +
    percentage.toString(10) + "%)";

  idProgressBar.style.pixelWidth = Math.round((percentage / 100) * WIDTH);

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

function doAgain()
{
    window.location.href = "<%=urlAgain%>";
}

function doCancel()
{
    window.location.href = "<%=urlCancel%>";
}

function doOk()
{
    window.location.href = "<%=urlDone%>";
}

function doRefresh()
{
    idFrame.document.location = idFrame.document.location;
}

function done()
{
  idPleaseWait.style.visibility = 'hidden';

  idCancelOk.value = "<%=lb_done%>";
  idCancelOk.onclick = doOk;

  idRefreshResult.value = "<%=lb_again%>";
  idRefreshResult.onclick = doAgain;
}

function doOnLoad()
{
  loadGuides();
}

</SCRIPT>
</HEAD>
<BODY LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0" MARGINWIDTH="0"
  MARGINHEIGHT="0" CLASS="standardText" _oncontextmenu="return false"
  ONLOAD="doOnLoad()">
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>

<DIV ID="contentLayer" 
 STYLE=" POSITION: ABSOLUTE; Z-INDEX: 8; TOP: 108px; LEFT: 20px; RIGHT: 20px;">
    
<SPAN CLASS="mainHeading" id="idHeading"><%=lb_replacing_termbase%></SPAN>
<BR>
<SPAN CLASS="standardTextItalic" id="idPleaseWait"><%=lb_please_wait%>...</SPAN>

<DIV id="idProgressContainer">
  <DIV id="idProgress">0%</DIV>
</DIV>
<DIV id="idProgressBar"></DIV>

<BR>
<DIV id="idMessagesHeader" class="header"><%=lb_replace_msg%>:</DIV>
<DIV id="idMessages"></DIV>

<DIV id="idLinks" style="width:500px">
  <INPUT TYPE="BUTTON" VALUE="<%=bundle.getString("lb_cancel")%>"
   id="idCancelOk" onclick="doCancel()"> &nbsp;
  <INPUT TYPE="BUTTON" VALUE="<%=bundle.getString("lb_refresh")%>"
   id="idRefreshResult" onclick="doRefresh()">
</DIV>

<IFRAME id="idFrame" src="<%=urlRefresh%>" style="display:none"></IFRAME>

</DIV>
</BODY>
</HTML>
