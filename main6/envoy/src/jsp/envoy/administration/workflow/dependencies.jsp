<%@ page contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="java.util.*,com.globalsight.util.resourcebundle.ResourceBundleConstants,
            com.globalsight.everest.servlet.util.SessionManager,
            com.globalsight.everest.webapp.WebAppConstants,
            com.globalsight.everest.webapp.pagehandler.PageHandler,
            java.util.ResourceBundle,
            java.util.Enumeration"
    session="true" %>
<jsp:useBean id="previous" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<%    
    ResourceBundle bundle = PageHandler.getBundle(session);
    String previousURL = previous.getPageURL();
    SessionManager sessionManager = (SessionManager)session.getAttribute(WebAppConstants.SESSION_MANAGER);    
    String dependencies = (String)sessionManager.getAttribute("dependencies");
%>
<HTML>
<HEAD>
<TITLE><%=bundle.getString("lb_enter_job_name")%></TITLE>
<SCRIPT SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>
<SCRIPT>
var needWarning = false;
var objectName = "";
var guideNode = "import";
var helpFile = "<%=bundle.getString("help_dependencies_exist")%>";
</SCRIPT>
</HEAD>
<BODY LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0" MARGINWIDTH="0" MARGINHEIGHT="0"
    onload="loadGuides()">
<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>
<DIV ID="contentLayer" STYLE=" POSITION: ABSOLUTE; Z-INDEX: 10; TOP: 108px; LEFT: 20px; RIGHT: 20px;">

<SPAN CLASS="mainHeading"><%=bundle.getString("lb_dependencies")%></SPAN>

<P>
<TABLE CELLPADDING=0 CELLSPACING=0 BORDER=0 CLASS=standardText>
  <TR>
    <TD WIDTH=500><font color="red"><%=dependencies%></font></TD>
  </TR>
</TABLE>
<P>

<INPUT TYPE="BUTTON" VALUE="<%=bundle.getString("lb_ok")%>"
 onclick="location.replace('<%=previousURL%>')">

</DIV>
</BODY>
</HTML>
