<%@ page contentType="text/html; charset=UTF-8"
language="java" session="true"
import="java.util.*,java.util.Date,
com.globalsight.diplomat.util.Logger,
com.globalsight.util.resourcebundle.ResourceBundleConstants,
com.globalsight.util.resourcebundle.SystemResourceBundle,
java.util.Locale,
java.util.ResourceBundle"
%>
<%
//response.setHeader("Pragma", "No-cache");
//response.setHeader("Cache-Control", "no-cache");
//response.setDateHeader("Expires", theDate.toString());
response.setContentType("text/html; charset=UTF-8");
Logger theLogger = Logger.getLogger();
theLogger.println(Logger.DEBUG_D, "PreviewUrlFrame: session id=" +request.getSession().getId());

String cxeServer = (String) request.getSession().getAttribute("cxeServer");
String srcLocale = (String) request.getSession().getAttribute("srcLocale");
String tgtLocale = (String) request.getSession().getAttribute("tgtLocale");
String srcEncoding = (String) request.getSession().getAttribute("srcEncoding");
String tgtEncoding = (String) request.getSession().getAttribute("tgtEncoding");
String srcUrlListId = (String) request.getSession().getAttribute("srcUrlListId");
String tgtUrlListId = (String) request.getSession().getAttribute("tgtUrlListId");
//theLogger.println(Logger.DEBUG_D,"PreviewUrlFrame: srcUrlListId= " + srcUrlListId);
//theLogger.println(Logger.DEBUG_D,"PreviewUrlFrame: tgtUrlListId= " + tgtUrlListId);
Integer numSrcUrls = (Integer) request.getSession().getAttribute("numSrcUrls");
Integer numTgtUrls = (Integer) request.getSession().getAttribute("numTgtUrls");
String navsize = "8%"; //size of the navigation frame
String docsize = "92%"; //size of the main frame
//if there is only one URL each, then set the frame size to 0 for both
try {
	if (numSrcUrls.intValue() == 1 && numTgtUrls.intValue() == 1) {
    navsize = "0"; //this is not a typo! netscape cannot handle 0%
    docsize = "100%";
	}
}
catch (Exception e) {
    navsize = "0"; //this is not a typo! netscape cannot handle 0%
    docsize = "100%";
}

ResourceBundle bundle=
    SystemResourceBundle.getInstance().getResourceBundle(
    ResourceBundleConstants.LOCALE_RESOURCE_NAME,
    Locale.getDefault());

String pagetitle = bundle.getString("action_preview");

//Get the date as a trick to force Netscape to re-load the frame
Date theDate = new Date();
long cxeTimeStamp = theDate.getTime();
%><html>
<META HTTP-EQUIV="Cache-Control" CONTENT="no-cache">
<META HTTP-EQUIV="Expires" CONTENT="0">
<META HTTP-EQUIV="Content-Type" CONTENT="text/html; charset=UTF-8">
<HEAD>
<TITLE><%= pagetitle %></TITLE>
<frameset rows="<%=navsize%>,<%=docsize%>">
    <frameset cols="50%,50%">
        <frame name="srcnav" src="<%=cxeServer%>/globalsight/UrlPresentationServlet?type=Source&targetFrame=srcframe&locale=<%=srcLocale%>&encoding=<%=srcEncoding%>&srcUrlListId=<%=srcUrlListId%>&tgtUrlListId=<%=tgtUrlListId%>&cxeTimeStamp=<%=cxeTimeStamp%>" scrolling="no" frameborder="0" marginwidth="0" marginheight="0" noresize>
        <frame name="tgtnav" src="<%=cxeServer%>/globalsight/UrlPresentationServlet?type=Target&targetFrame=tgtframe&locale=<%=tgtLocale%>&encoding=<%=tgtEncoding%>&srcUrlListId=<%=srcUrlListId%>&tgtUrlListId=<%=tgtUrlListId%>&cxeTimeStamp=<%=cxeTimeStamp%>" scrolling="no" frameborder="0" marginwidth="0" marginheight="0" noresize>
    </frameset>
    <frameset COLS="50%,50%">
        <FRAME name="srcframe" src="<%=cxeServer%>/globalsight/cxe/html/blank.html" SCROLLING="auto" frameborder="1" marginwidth="0" marginheight="0" noresize>
        <FRAME name="tgtframe" src="<%=cxeServer%>/globalsight/cxe/html/blank.html" SCROLLING="auto" frameborder="1" marginwidth="0" marginheight="0" noresize>
    </frameset>
</frameset>
</HEAD>
</html>

