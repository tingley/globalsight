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
    "=" + WebAppConstants.TERMBASE_ACTION_CANCEL;

String str_termbaseName =
  (String)sessionMgr.getAttribute(WebAppConstants.TERMBASE_TB_NAME);
String str_termbaseId =
  (String)sessionMgr.getAttribute(WebAppConstants.TERMBASE_TB_ID);

Object[] args = { str_termbaseName };
MessageFormat format = new MessageFormat(bundle.getString("lb_termbase_indexing_onetb"));
String lb_import_into_termbase = format.format(args);

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

String title = bundle.getString("lb_termbase_indexing_progress");
%>
<HTML>
<HEAD>
<TITLE><%=title %></TITLE>
<SCRIPT SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>
<SCRIPT src="/globalsight/includes/library.js"></SCRIPT>
<SCRIPT SRC="envoy/terminology/management/protocol.js"></SCRIPT>
<SCRIPT SRC="envoy/terminology/viewer/viewerAPI.js"></SCRIPT>
<STYLE>
#idProgressContainer {
	border: solid 1px <%=skin.getProperty("skin.list.borderColor")%>;
        z-index: 1; position: absolute; top: 72; left: 20; width: 400;
}
#idProgressBar {
	background-color: #a6b8ce; z-index: 0;
	border: solid 1px <%=skin.getProperty("skin.list.borderColor")%>; 
	position: absolute; top: 72; left: 20; width: 0;
}
#idProgressContainer2 {
	border: solid 1px <%=skin.getProperty("skin.list.borderColor")%>;
        z-index: 1; position: absolute; top: 42; left: 20; width: 400;
}
#idProgressBar2 {
	background-color: #a6b8ce; z-index: 0;
	border: solid 1px <%=skin.getProperty("skin.list.borderColor")%>; 
	position: absolute; top: 42; left: 20; width: 0;
}
#idProgress,
#idProgress2   { text-align: center; z-index: 2; font-weight: bold; }
#idMessagesHeader { position: absolute; top: 102; left: 20; font-weight: bold;}
#idMessages    { position: absolute; overflow: auto; z-index: 0; 
                 top: 132; left: 20; height: 80; width: 400; }
#idLinks       { position: absolute; left: 20; top: 334; z-index: 1; }
</STYLE>
<SCRIPT>
var needWarning = false;
var objectName = "";
var guideNode = "terminology";
var helpFile = "<%=bundle.getString("help_termbase_index_indexing")%>";

var WIDTH = 400;

eval("<%=errorScript%>");

function showMessage(message)
{
    var div = document.createElement("DIV");
    div.innerHTML = message;
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
  idProgress.innerHTML = entryCount.toString(10) + " <%=bundle.getString("lb_entries")%> (" +
    percentage.toString(10) + "%)";

  idProgressBar.style.width = Math.round((percentage / 100) * WIDTH);
  
   idProgressBar.style.height = 15;
   idProgressBar.innerHTML='&nbsp'; 

  if (message)
  {
    showMessage(message);
  }
}

function showProgress2(desc2, percentage2)
{
  idProgress2.innerHTML = desc2 + " (" + percentage2.toString(10) + "%)";

  idProgressBar2.style.width = Math.round((percentage2 / 100) * WIDTH);
  
  idProgressBar2.style.height = 15;
}

function done()
{
  idPleaseWait.style.visibility = 'hidden';

  idCancelOk.value = "<%=lb_OK%>";
  idCancelOk.onclick = doOk;
  idCancelOk.focus();

  idRefreshResult.value = "<%=lb_browseTermbase%>";
  idRefreshResult.onclick = showTermbase;
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
    // alert(idFrame.document.location);
    idFrame.document.location = idFrame.document.location;
}

function showTermbase()
{
  ShowTermbase('<%=str_termbaseId%>');
}
</SCRIPT>
</HEAD>
<BODY LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0" MARGINWIDTH="0"
  MARGINHEIGHT="0" CLASS="standardText" oncontextmenu="return false"
  ONLOAD="loadGuides()">
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>

<DIV ID="contentLayer" 
 STYLE=" POSITION: ABSOLUTE; Z-INDEX: 8; TOP: 108px; LEFT: 20px; RIGHT: 20px;">
    
<SPAN CLASS="mainHeading" id="idHeading"><%=lb_import_into_termbase%></SPAN>
<BR>
<SPAN CLASS="standardTextItalic" id="idPleaseWait">
  <%=bundle.getString("lb_termbase_indexing_wait") %>
</SPAN>

<DIV id="idProgressContainer2">
  <DIV id="idProgress2">0%</DIV>
</DIV>
<DIV id="idProgressBar2"></DIV>

<DIV id="idProgressContainer">
  <DIV id="idProgress">0%</DIV>
</DIV>
<DIV id="idProgressBar"></DIV>

<BR>
<DIV id="idMessagesHeader" class="header"><%=bundle.getString("lb_termbase_indexing_msg") %></DIV>
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
