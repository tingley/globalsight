<%@ page
    contentType="text/javascript; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="java.util.*,
        java.util.ResourceBundle,
        java.text.MessageFormat,
        com.globalsight.util.edit.EditUtil,
        com.globalsight.everest.webapp.pagehandler.PageHandler,
        com.globalsight.everest.webapp.WebAppConstants"
    session="true"
%>
<%
ResourceBundle bundle = PageHandler.getBundle(session);

String lb_no_result = bundle.getString("msg_no_result");
String lb_server_error = bundle.getString("msg_error_in_server_response");
String lb_close_window = bundle.getString("msg_close_window_try_again"); 
String lb_searching = bundle.getString("msg_searching");
String lb_loading = bundle.getString("msg_loading");
String lb_redisplaying = bundle.getString("msg_redisplaying");
%>

var bullet =
  " <img height=9 width=9 src='/globalsight/envoy/terminology/viewer/bullet2.gif'>";

var lb_no_result = "<%=EditUtil.toJavascript(lb_no_result)%>";
var lb_server_error = "<%=EditUtil.toJavascript(lb_server_error)%>";
var lb_close_window = "<%=EditUtil.toJavascript(lb_close_window)%>";
var lb_searching = "<%=EditUtil.toJavascript(lb_searching)%>" + bullet;
var lb_redisplaying = "<%=EditUtil.toJavascript(lb_redisplaying)%>" + bullet;
var lb_loading = "<%=EditUtil.toJavascript(lb_loading)%>" + bullet;
