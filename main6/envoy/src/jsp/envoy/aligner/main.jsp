<%@ taglib uri="/WEB-INF/tlds/globalsight.tld" prefix="amb" %>
<%@ page
    contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="java.util.*,com.globalsight.everest.servlet.util.SessionManager,
    com.globalsight.everest.webapp.javabean.NavigationBean,
        com.globalsight.everest.permission.Permission,
        com.globalsight.everest.webapp.WebAppConstants,
        com.globalsight.everest.webapp.pagehandler.PageHandler,
        com.globalsight.everest.webapp.webnavigation.LinkHelper,
        com.globalsight.util.edit.EditUtil,
    java.util.ResourceBundle"
    session="true"
%>
<jsp:useBean id="createpackage" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="downloadpackage" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="uploadpackage" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<%
ResourceBundle bundle = PageHandler.getBundle(session);
SessionManager sessionMgr = (SessionManager)session.getAttribute(
  WebAppConstants.SESSION_MANAGER);

String urlCreate = createpackage.getPageURL();
String urlDownload = downloadpackage.getPageURL();
String urlUpload = uploadpackage.getPageURL();

String lb_title = bundle.getString("lb_corpus_aligner");
String lb_helptext = bundle.getString("helper_text_aligner_packages");
%>
<HTML>
<HEAD>
<TITLE><%=lb_title%></TITLE>
<SCRIPT SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>
<STYLE>
BUTTON { width: 200px; }
</STYLE>
<SCRIPT>
var needWarning = false;
var objectName = "";
var guideNode = "terminology";
var helpFile = "<%=bundle.getString("help_align_main")%>";

function createPackage()
{
    window.location.href = '<%=urlCreate%>';
}

function downloadPackage()
{
    window.location.href = '<%=urlDownload%>';
}

function uploadPackage()
{
    window.location.href = '<%=urlUpload%>';
}

function doLoad()
{
  // This loads the guides in guides.js and the
  loadGuides();
}
</SCRIPT>
</HEAD>
<BODY onload="doLoad();" LEFTMARGIN="0" RIGHTMARGIN="0"
  TOPMARGIN="0" MARGINWIDTH="0" MARGINHEIGHT="0">
<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>

<DIV ID="contentLayer"
 STYLE=" POSITION: ABSOLUTE; Z-INDEX: 9; TOP: 108px; LEFT: 20px; RIGHT: 20px;">

<DIV CLASS="mainHeading"><%=lb_title%></DIV>
<BR>

<TABLE CELLPADDING=0 CELLSPACING=0 BORDER=0 CLASS=standardText>
  <TR>
    <TD WIDTH=538><%=lb_helptext%></TD>
  </TR>
</TABLE>

<BR>

<DIV style="margin-left: 30px">
<amb:permission name="<%=Permission.CORPUS_ALIGNER_CREATE%>" >
  <BUTTON onclick="createPackage()"><%=bundle.getString("lb_aligner_package_create") %></BUTTON>
  <BR><BR>
</amb:permission>
<amb:permission name="<%=Permission.CORPUS_ALIGNER_DOWNLOAD%>" >
  <BUTTON onclick="downloadPackage()"><%=bundle.getString("lb_aligner_package_download") %></BUTTON>
  <BR><BR>
</amb:permission>
<amb:permission name="<%=Permission.CORPUS_ALIGNER_UPLOAD%>" >
  <BUTTON onclick="uploadPackage()"><%=bundle.getString("lb_aligner_package_upload") %></BUTTON>
</amb:permission>
  <BR><BR>
</DIV>

</DIV>
</BODY>
</HTML>
