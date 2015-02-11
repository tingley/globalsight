<%@ page
    contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="java.util.*,com.globalsight.everest.webapp.webnavigation.LinkHelper,
        java.util.ResourceBundle,
        com.globalsight.util.edit.EditUtil,
        com.globalsight.everest.webapp.pagehandler.PageHandler,
        com.globalsight.everest.webapp.WebAppConstants,
        com.globalsight.everest.servlet.util.SessionManager"
    session="true"
%>
<%
ResourceBundle bundle = PageHandler.getBundle(session);

String lb_click_to_copy_to_clipboard =
  bundle.getString("lb_click_to_copy_to_clipboard");
String lb_close = bundle.getString("lb_close");
String lb_copy_to_clipboard = bundle.getString("lb_copy_to_clipboard");
String lb_hide_stacktrace = bundle.getString("lb_hide_stacktrace");
String lb_show_stacktrace = bundle.getString("lb_show_stacktrace");
String lb_system_error = bundle.getString("lb_system_error");
%>
<html>
<head>
<title><%=lb_system_error%></title>
<SCRIPT SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<style>
BODY          { margin: 5px; }

#idMessage    { height:  40px; width: 425px; overflow: auto; }
#idLinks      { height:  20px; width: 425px; overflow: none; }
#idHidden     { height: 165px; width: 430px; }
#idStackTrace { height: 165px; width: 430px; overflow: auto;
                border-style: inset; border-width: 3px; }
.link         { color: blue; cursor: hand; text-decoration: underline; }
</style>
<script>
var g_arg;

function showStackTrace()
{
  //window.dialogHeight = "400px";
  // window.dialogWidth  = "575px";
  idHidden.style.display = "block";
  idLink.innerText = "<%=EditUtil.toJavascript(lb_hide_stacktrace)%>";
  idLink.onclick = hideStackTrace;
  idLink1.style.display = "inline";
}

function hideStackTrace()
{
  idHidden.style.display = "none";
  idLink.innerText = "<%=EditUtil.toJavascript(lb_show_stacktrace)%>";
  idLink.onclick = showStackTrace;
  idLink1.style.display = "none";
  //window.dialogHeight = "300px";
  //window.dialogWidth  = "450px";
}

function copyToClipboard()
{
  window.clipboardData.setData("Text", g_arg.description);
}

function closeWindow()
{
  window.close();
}

function doOnLoad()
{
  window.dialogHeight = "300px";
  window.dialogWidth  = "450px";

  // arg is a Javascript Error() object with .message and .description.
  g_arg = window.dialogArguments;

  idMessage.innerText = g_arg.message;
  idStackTrace.innerText = g_arg.description;

  idClose.onclick = closeWindow;
}
</script>
</head>

<body id="idBody" onload="doOnLoad();">
<!--<h3><%=lb_system_error%></h3>-->

<div id="idMessage"></div>

<BR>

<div id="idLinks">
<span id="idLink" class="link" onclick="showStackTrace()">
<%=EditUtil.toJavascript(lb_show_stacktrace)%></span>
&nbsp;&nbsp;
<span id="idLink1" class="link" style="display:none" onclick="copyToClipboard()">
<%=EditUtil.toJavascript(lb_copy_to_clipboard)%></span>
&nbsp;&nbsp;
<span id="idClose" class="link"><%=lb_close%></span>
</div>

<div id="idHidden" style="display:none;">
  <pre id="idStackTrace" onclick="copyToClipboard()"
   TITLE="<%=lb_click_to_copy_to_clipboard%>"></pre>
</div>
</body>
</html>
