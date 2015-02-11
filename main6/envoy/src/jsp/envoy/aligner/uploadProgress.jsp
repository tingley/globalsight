<%@ page
    contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="java.util.*,com.globalsight.everest.webapp.webnavigation.LinkHelper,
        java.util.ResourceBundle,
        java.util.ArrayList,
        com.globalsight.util.edit.EditUtil,
        com.globalsight.everest.tm.importer.ImportOptions,
        com.globalsight.everest.servlet.util.SessionManager,
        com.globalsight.everest.webapp.pagehandler.PageHandler,
        com.globalsight.everest.webapp.WebAppConstants,
        com.globalsight.everest.aligner.AlignerPackageUploadOptions"
    session="true"
%>
<jsp:useBean id="ok" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="upload" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<%
ResourceBundle bundle = PageHandler.getBundle(session);
SessionManager sessionMgr = (SessionManager)session.getAttribute(
  WebAppConstants.SESSION_MANAGER);

AlignerPackageUploadOptions gapOptions = (AlignerPackageUploadOptions)
  sessionMgr.getAttribute(WebAppConstants.GAP_OPTIONS);

String urlOK     = ok.getPageURL();
String urlUpload = upload.getPageURL();

String lb_title = bundle.getString("lb_aligner_package_uploaded");
%>
<HTML>
<HEAD>
<TITLE><%=lb_title%></TITLE>
<SCRIPT SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>
<%@ include file="/includes/compatibility.jspIncl" %>
<SCRIPT SRC="/globalsight/includes/library.js"></SCRIPT>
<SCRIPT>
var needWarning = false;
var objectName = "";
var guideNode = "aligner";
var helpFile = "<%=bundle.getString("help_align_main")%>";

function doOk()
{
    window.navigate("<%=urlOK%>");
}

function doUpload()
{
    window.navigate("<%=urlUpload%>");
}

function doOnLoad()
{
    // Load the Guides
    loadGuides();
}
</SCRIPT>
</HEAD>
<BODY LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0" MARGINWIDTH="0"
 MARGINHEIGHT="0" CLASS="standardText" onload="doOnLoad()">
<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>
<DIV ID="contentLayer"
    STYLE=" POSITION: ABSOLUTE; Z-INDEX: 9; TOP: 108px; LEFT: 20px; RIGHT: 20px;">

<DIV CLASS="mainHeading" ID="idHeading"><%=lb_title%></DIV>
<BR>

<TABLE CELLPADDING=0 CELLSPACING=0 BORDER=0 CLASS=standardText>
  <TR>
    <TD WIDTH=500>
    <%=java.text.MessageFormat.format(bundle.getString("helper_text_aligner_package_upload_status"), gapOptions.getFileName(), gapOptions.getTmName()) %>
    </TD>
  </TR>
</TABLE>
<BR>

<DIV id="idButtons">
<INPUT TYPE="BUTTON" VALUE="<%=bundle.getString("lb_upload_again") %>" onclick="doUpload()">
&nbsp;&nbsp;&nbsp;
<INPUT TYPE="BUTTON" VALUE="<%=bundle.getString("lb_ok")%>"
 onclick="doOk()">
</DIV>

</DIV>

</BODY>
</HTML>
