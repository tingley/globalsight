<%@ page
    contentType="text/html; charset=UTF-8"
    isErrorPage="true"
    import="java.util.*,
            com.globalsight.everest.servlet.ControlServlet,
            com.globalsight.everest.webapp.pagehandler.PageHandler,
	    com.globalsight.everest.servlet.util.SessionManager,
            com.globalsight.everest.webapp.WebAppConstants,
            java.util.ResourceBundle,
            com.globalsight.everest.webapp.pagehandler.offline.OfflineConstants"
    session="true"
%>
<jsp:useBean id="previous" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<%
    ControlServlet.handleJSPException(exception);

    ResourceBundle bundle = PageHandler.getBundle(session);

    String previousUrl = previous.getPageURL();
 
    // labels
    String title = bundle.getString("msg_upload_error_title");
    String lbPrevious = bundle.getString("lb_previous");

    SessionManager sessionManager = 
        (SessionManager)session.getAttribute(WebAppConstants.SESSION_MANAGER);

    // error message
    String errorHtmlSnippet =
       (String)sessionManager.getAttribute(OfflineConstants.ERROR_MESSAGE);
%>
<HTML>
<HEAD>
<TITLE><%=title%></TITLE>
<SCRIPT SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<%@ include file="/envoy/common/warning.jspIncl" %>
<SCRIPT>
var needWarning = false;
var objectName = "";
var helpFile = "<%=bundle.getString("help_upload_error")%>";
</SCRIPT>
</HEAD>
<BODY LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0" MARGINWIDTH="0" MARGINHEIGHT="0">
<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/common/navigation.jspIncl" %>
<DIV ID="contentLayer" STYLE=" POSITION: ABSOLUTE; Z-INDEX: 9; TOP: 108px; LEFT: 20px; RIGHT: 20px;">

<SPAN CLASS="mainHeading"><%=title%></SPAN>
<P>
<SPAN CLASS=standardText><%= errorHtmlSnippet %></SPAN>

<INPUT TYPE="BUTTON" NAME="<%=lbPrevious%>" VALUE="<%=lbPrevious%>" 
 onclick="location.replace('<%=previousUrl%>')">   
</DIV>
</BODY>
</HTML>
