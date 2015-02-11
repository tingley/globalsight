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
<title>System Error</title>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<style>
BODY          { margin: 5px; }

#idStackTrace { border-style: inset; border-width: 3px; }
.link         { color: blue; cursor: pointer; text-decoration: underline; }
</style>
<SCRIPT   LANGUAGE="JavaScript">  
   <!--
   //make the window in the center position  
   function   centerWindow()   
   {
       var xMax = screen.width;
       var yMax = screen.height;
       
       window.moveTo(xMax/2 - 100, yMax/2 - 100 - 80);
   }
     
   centerWindow();  
  //-->  
</SCRIPT>
<script language="Javascript">
var str_stacktrace;

function enableStackTrace()
{
  idLink.style.display = "inline";
  idLink.innerText = "<%=EditUtil.toJavascript(lb_show_stacktrace)%>";
  idLink.onclick = showStackTrace;

  idLink1.style.display = "none";
  idLink1.innerText = "<%=EditUtil.toJavascript(lb_copy_to_clipboard)%>";
  idLink1.onclick = copyToClipboard;
}

function showStackTrace()
{
  window.dialogHeight = "400px";
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
  window.dialogHeight = "300px";
  window.dialogWidth  = "450px";
}

function copyToClipboard()
{
  window.clipboardData.setData("Text", str_stacktrace);
}

function closeWindow()
{
  window.close();
}

function doOnLoad()
{
  window.dialogHeight = "300px";
  window.dialogWidth  = "450px";

  // arg is a Javascript string
  var arg = window.dialogArguments;
  var errorstring = arg;
  var re = new RegExp("@@@@@", "g");
  var res = errorstring.match(re);

  if (res == null)
  {
    idMessage.innerText = errorstring;
  }
  else
  {
    idMessage.innerText = errorstring.substring(0, res.index);
    str_stacktrace = errorstring.substring(res.lastIndex);
    idStackTrace.innerText = str_stacktrace;
    enableStackTrace();
  }

  idClose.onclick = closeWindow;
}
</script>
</head>

<body id="idBody" onload="doOnLoad();">
<h3><%=lb_system_error%></h3>

<div id="idMessage"></div>

<BR>

<div>
<span id="idClose" class="link"><%=lb_close%></span>
&nbsp;&nbsp;
<span id="idLink" class="link" style="display:none"></span>
&nbsp;&nbsp;
<span id="idLink1" class="link" style="display:none"></span>
</div>

<div id="idHidden" style="display:none; height:200; width:430; overflow:auto;">
  <pre id="idStackTrace" onclick="copyToClipboard()"
   TITLE="<%=lb_click_to_copy_to_clipboard%>"></pre>
</div>
</body>
</html>
