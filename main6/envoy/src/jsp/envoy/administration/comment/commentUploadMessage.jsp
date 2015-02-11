<%@ page
    contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
	import="java.util.*,
	  com.globalsight.everest.webapp.pagehandler.PageHandler,
	  java.util.ResourceBundle"
    session="true"
%>
<%
	ResourceBundle bundle = PageHandler.getBundle(session);
	String lb_messageWindow = bundle.getString("lb_message_window");
	String lb_close = bundle.getString("lb_close");
	String lb_uploadStatus = bundle.getString("lb_comment_ref_upload_status");
%>
<html>
<head>
<title><%= lb_messageWindow %></title>
<style>
BODY          { margin: 5px; }
.link         { color: blue; cursor: hand; }
</style>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<script language="Javascript">
function doOnLoad()
{
  var message = window.dialogArguments;
  idMessage.innerHTML = message;
}
</script>
</head>

<body id="idBody" onload="doOnLoad();">
<h3><%= lb_uploadStatus %></h3>

<div id="idMessage"></div>

<br/>
<div align="center">
  <span class="link" onclick="window.close()"><%= lb_close %></span>
</div>

</body>
</html>
