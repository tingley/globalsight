<%@ page 
    contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="java.util.*,com.globalsight.everest.webapp.webnavigation.LinkHelper,
            com.globalsight.util.progress.ProcessStatus,
            com.globalsight.everest.tm.searchreplace.SearchReplaceManager,
            java.util.ResourceBundle,
            java.text.MessageFormat,
            com.globalsight.util.edit.EditUtil,
            com.globalsight.everest.webapp.pagehandler.PageHandler,
            com.globalsight.everest.webapp.WebAppConstants,
            com.globalsight.everest.servlet.util.SessionManager"
    session="true"
%>
<%@ include file="/envoy/common/header.jspIncl" %>
<jsp:useBean id="done" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean"/>
 <jsp:useBean id="cancel" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean"/>
<jsp:useBean id="refresh" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean"/>
<%
ResourceBundle bundle = PageHandler.getBundle(session);
SessionManager sessionMgr = (SessionManager)session.getAttribute(
  WebAppConstants.SESSION_MANAGER);
ProcessStatus m_status =
  (ProcessStatus)sessionMgr.getAttribute(WebAppConstants.TM_TM_STATUS);

int counter    = 0;
int percentage = 0;
String message = "";
if (m_status != null)
{
    counter = m_status.getCounter();
    percentage = m_status.getPercentage();
    message = m_status.getMessage();
}

String urlDone = done.getPageURL() + 
    "&" + WebAppConstants.TM_SEARCH_STATE_PARAM +
    "=" + WebAppConstants.TM_SEARCH_STATE_NORMAL;
String urlRefresh = refresh.getPageURL() +
    "&" + WebAppConstants.TM_SEARCH_STATE_PARAM +
    "=" + WebAppConstants.TM_ACTION_REFRESH;
String urlCancel = cancel.getPageURL() +
    "&" + WebAppConstants.TM_SEARCH_STATE_PARAM +
    "=" + WebAppConstants.TM_ACTION_CANCEL_SEARCH;
String refreshUrl = urlRefresh;
String refreshMetaTag =
  PageHandler.getRefreshMetaTagForProgressBar(refreshUrl);

String str_tmName =
  (String)sessionMgr.getAttribute(WebAppConstants.TM_TM_NAME);
String str_tmId =
  (String)sessionMgr.getAttribute(WebAppConstants.TM_TM_ID);

Object[] args = { str_tmName };
String title = bundle.getString("lb_search_tm") + " " + str_tmName;
String lb_searching_tm = title + "...";

String lb_copiedToClipboard =
  bundle.getString("jsmsg_messages_copied_to_clipboard");
String lb_continue = bundle.getString("lb_continue");
String lb_cancel = bundle.getString("lb_cancel");
String lb_search_msg = bundle.getString("lb_search_msg");
String lb_please_wait = bundle.getString("lb_pls_wait");

// Perform error handling, then clear out session attribute.
String errorScript = "";
String error = (String)sessionMgr.getAttribute(WebAppConstants.TM_ERROR);
if (error != null)
{
  errorScript = "var error = new Error();" +
    "error.message = '" + EditUtil.toJavascript(bundle.getString("lb_import_error")) + "';" + 
    "error.description = '" + EditUtil.toJavascript(error) +
    "'; showError(error);";
}
sessionMgr.removeElement(WebAppConstants.TM_ERROR);
%>
<HTML>
<HEAD>
<% if (percentage < 100) { out.print(refreshMetaTag); } %>
<TITLE><%=title%></TITLE>
<SCRIPT SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>
<SCRIPT src="/globalsight/includes/library.js"></SCRIPT>
<SCRIPT SRC="envoy/terminology/management/protocol.js"></SCRIPT>
<STYLE>
#idProgressContainer {
	border: solid 1px <%=skin.getProperty("skin.list.borderColor")%>;
	z-index: 1; position: absolute; top: 42; left: 20; width: 400;
}
#idProgressBar {
	background-color: #a6b8ce; z-index: 0;
	border: solid 1px <%=skin.getProperty("skin.list.borderColor")%>; 
	position: absolute; top: 42; left: 20; width: 0;
}
#idProgress    { text-align: center; z-index: 2; font-weight: bold; }
#idMessagesHeader { position: absolute; top: 72; left: 20; font-weight: bold;}
#idMessages    { position: absolute; overflow: auto; z-index: 0; 
                 top: 102; left: 20; height: 80; width: 400; }
#idLinks       { position: absolute; left: 20; top: 314; z-index: 1; }
#idCancel      { position: absolute; left: 20; top: 314; z-index: 1; }
</STYLE>
<SCRIPT>
var needWarning = false;
var objectName = "";
var guideNode = "tm";
var helpFile = "<%=bundle.getString("help_progressbar")%>";

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
  idProgress.innerHTML = percentage.toString(10) + "%";

  idProgressBar.style.width = Math.round((percentage / 100) * WIDTH);
  if(window.navigator.userAgent.indexOf("Firefox")>0 || window.navigator.userAgent.indexOf("Chrome")>0)
  {
    idProgressBar.style.height = 15;
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
    window.location.href = "<%=urlRefresh%>";
}

function doOnLoad()
{
    loadGuides();
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
    
<SPAN CLASS="mainHeading" id="idHeading"><%=lb_searching_tm%></SPAN><BR>
<% if (percentage < 100) { %>
<SPAN CLASS="standardTextItalic" id="idPleaseWait"><%=lb_please_wait%>...</SPAN>
<% } %>

<DIV id="idProgressContainer">
  <DIV id="idProgress">0%</DIV>
</DIV>
<DIV id="idProgressBar"></DIV>

<BR>
<DIV id="idMessagesHeader" class="header"><%=lb_search_msg%>:</DIV>
<DIV id="idMessages"></DIV>

<%
if (error == null)
{
  out.flush();

  if (m_status != null)
  {
      out.print("<SCRIPT>");
      out.print("showProgress(" + counter + "," + percentage + ",'");
      if (message != null)
      {
          out.print(EditUtil.toJavascript(message));
      }
      out.println("');");
      out.print("</SCRIPT>");
      out.flush();

      if (percentage >= 100)
      {
          // Do not remove status object here.
          // The Status contains results that are used
          // by the next screen showing results...
      }
  }
}
else
{
  out.print("<SCRIPT>showMessage(\"" + EditUtil.toJavascript(error) +
    "\")</SCRIPT>");
}
%>

<% if (percentage >= 100) { %>
<DIV id="idLinks">
  <INPUT TYPE="BUTTON" VALUE="<%=lb_continue%>" onclick="doOk()">
</DIV>
<%} else {%>
<DIV id="idLinks">
  <INPUT TYPE="BUTTON" VALUE="<%=bundle.getString("lb_cancel")%>"
  onclick="doCancel()"> 
  <INPUT TYPE="BUTTON" VALUE="<%=bundle.getString("lb_refresh")%>"
   onclick="doRefresh()"> 
</DIV>
<%}%>
</DIV>
</BODY>
</HTML>                                                 
