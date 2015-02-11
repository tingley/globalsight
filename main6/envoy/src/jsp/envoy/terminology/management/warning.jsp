<%@ page 
    contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="java.util.*,com.globalsight.everest.webapp.webnavigation.LinkHelper,
        java.util.ResourceBundle,
        com.globalsight.everest.webapp.pagehandler.PageHandler,
        com.globalsight.everest.webapp.WebAppConstants,
        com.globalsight.everest.servlet.util.SessionManager"
    session="true"
%>
<%
ResourceBundle bundle = PageHandler.getBundle(session);

String lb_message_window = bundle.getString("lb_message_window");
String lb_warning = bundle.getString("lb_warning");
String lb_close = bundle.getString("lb_close");
%>
<html>
<head>
<title><%=lb_message_window%></title>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<style>
BODY          { margin: 5px; }
.link         { color: blue; cursor: hand; }
</style>
<script language="Javascript">
function doOnLoad()
{
  var message = window.dialogArguments;
  idMessage.innerHTML = message;//idMessage.innerText = message;
}
</script>
</head>

<body id="idBody" onload="doOnLoad();">
<SPAN CLASS="mainHeading"><%=lb_warning%></SPAN>
<SPAN CLASS="standardText">

<div id="idMessage"></div>

<P>

<DIV ALIGN="CENTER">
<INPUT TYPE="BUTTON" VALUE="<%=lb_close%>" onclick="window.close()">
</DIV>

</SPAN>

</body>
</html>
