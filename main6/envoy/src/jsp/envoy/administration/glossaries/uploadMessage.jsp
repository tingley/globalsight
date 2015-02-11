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

String lb_title = bundle.getString("lb_message_window");
String lb_status = bundle.getString("lb_status");
String lb_close = bundle.getString("lb_close");
%>
<HTML>
<HEAD>
<TITLE><%=lb_title%></TITLE>
<STYLE>
BODY          { margin: 5px; }
.link         { color: blue; cursor: hand; }
</STYLE>
<SCRIPT SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<SCRIPT>
function doOnLoad()
{
  var message = window.dialogArguments;
  idMessage.innerHTML = message;
}
</SCRIPT>
</HEAD>

<BODY id="idBody" onload="doOnLoad();">
<H3><%= lb_status %></H3>

<DIV id="idMessage"></DIV>

<BR/>
<DIV align="center">
  <INPUT TYPE="BUTTON" NAME="<%=lb_close%>" VALUE="<%=lb_close%>" 
   onclick="window.close()">     
</DIV>

</BODY>
</HTML>
