<%@ page contentType="text/html; charset=UTF-8"
    import="java.util.*,com.globalsight.everest.webapp.pagehandler.administration.shutdown.ShutdownMainHandler"
%><%
//    response.setHeader("Pragma", "No-cache");
//    response.setDateHeader("Expires", 0);
//    response.setHeader("Cache-Control", "no-cache");
%>
<HTML>
<HEAD>
<!----- BEGIN SHUTDOWN POPUP.JSP--->
<%
if (ShutdownMainHandler.shutdownBannerEnabled() &&
    ShutdownMainHandler.isShuttingDown() == true)
{
//Boolean warnedAlready = (Boolean)session.getAttribute("warnedAboutShutdown");
//System.out.println("warnedAboutShutdown=" + warnedAlready);
//if (warnedAlready == null)
//{
%>
<SCRIPT LANGUAGE="JavaScript">
var url = "/globalsight/envoy/common/shutdownBanner.jsp";
var mwidth = 450;
var mheight = 100; // minimum height
eval('window.showModelessDialog(url,"SHUTDOWN MESSAGE","help:0;resizable:0;status:0;dialogWidth:'+mwidth+'px;dialogHeight:'+mheight+'px")');
</SCRIPT>
<%
//session.setAttribute("warnedAboutShutdown", Boolean.TRUE);
//}
}
%>
</HEAD>
<BODY>
</BODY>
</HTML>
<!----- END SHUTDOWN POPUP ---->	

