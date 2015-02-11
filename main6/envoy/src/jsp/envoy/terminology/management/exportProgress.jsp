<%@ page
    contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="
        com.globalsight.everest.company.CompanyThreadLocal,
        com.globalsight.everest.foundation.User,
        com.globalsight.everest.servlet.util.SessionManager,
        com.globalsight.everest.webapp.pagehandler.administration.users.UserUtil,
        com.globalsight.everest.webapp.pagehandler.PageHandler,
        com.globalsight.everest.webapp.webnavigation.LinkHelper,
        com.globalsight.everest.webapp.WebAppConstants,
        com.globalsight.exporter.IExportManager,
        com.globalsight.util.edit.EditUtil,
        com.globalsight.util.progress.IProcessStatusListener,
        java.util.*,
        java.util.ResourceBundle,
        java.text.MessageFormat,
        java.io.IOException"
    session="true"
%>
<%@ include file="/envoy/common/header.jspIncl" %>
<jsp:useBean id="ok" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="cancel" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="refresh" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<%
ResourceBundle bundle = PageHandler.getBundle(session);
SessionManager sessionMgr = (SessionManager)session.getAttribute(
  WebAppConstants.SESSION_MANAGER);

String xmlExportOptions =
  (String)sessionMgr.getAttribute(WebAppConstants.TERMBASE_EXPORT_OPTIONS);
String str_termbaseName =
  (String)sessionMgr.getAttribute(WebAppConstants.TERMBASE_TB_NAME);
String str_termbaseId =
  (String)sessionMgr.getAttribute(WebAppConstants.TERMBASE_TB_ID);

String urlOk = ok.getPageURL();
String urlRefresh = refresh.getPageURL() +
    "&" + WebAppConstants.TERMBASE_ACTION +
    "=" + WebAppConstants.TERMBASE_ACTION_REFRESH;
String urlCancel = cancel.getPageURL() +
    "&" + WebAppConstants.TERMBASE_ACTION +
    "=" + WebAppConstants.TERMBASE_ACTION_CANCEL_EXPORT;

String lb_title = bundle.getString("lb_terminology_exporting") + str_termbaseName;
String lb_downloadFile = bundle.getString("lb_download_export_file");
String lb_OK = bundle.getString("lb_ok");

// Perform error handling, then clear out session attribute.
String errorScript = "";
String error = (String)sessionMgr.getAttribute(WebAppConstants.TERMBASE_ERROR);
if (error != null)
{
  errorScript = "var error = new Error();" +
    "error.message = '" + EditUtil.toJavascript(bundle.getString("lb_import_error")) + "';" +
    "error.description = '" + EditUtil.toJavascript(error) +
    "'; showError(error);";
}
sessionMgr.removeElement(WebAppConstants.TERMBASE_ERROR);
%>
<HTML XMLNS:gs>
<HEAD>
<TITLE><%=lb_title%></TITLE>
<STYLE>
#idProgressContainer { border: solid 1px <%=skin.getProperty("skin.list.borderColor")%>; z-index: 1; 
                 position: absolute; top: 42; left: 20; width: 400; }
#idProgressBar { background-color: #a6b8ce; z-index: 0;
                 border: solid 1px <%=skin.getProperty("skin.list.borderColor")%>; 
                 position: absolute; top: 42; left: 20; width: 0; }
#idProgress    { text-align: center; z-index: 2; font-weight: bold; }
#idMessagesHeader { position: absolute; top: 72; left: 20; font-weight: bold;}
#idMessages    { position: absolute; overflow: auto; z-index: 0;
                 top: 102; left: 20; height: 80; width: 400; }
#idLinks       { position: absolute; left: 20; top: 246; z-index: 1; }

#idLinks A,
#idLinks A:active,
#idLinks A:hover,
#idLinks A:link,
#idLinks A:visited      { color: blue; text-decoration: none; }
</STYLE>
<SCRIPT SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>
<%@ include file="/includes/compatibility.jspIncl" %>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/jquery/jquery-1.6.4.min.js"></SCRIPT>
<SCRIPT SRC="/globalsight/includes/library.js"></SCRIPT>
<SCRIPT SRC="envoy/terminology/management/protocol.js"></SCRIPT>
<SCRIPT>
var needWarning = false;
var objectName = "";
var guideNode = "terminology";
var helpFile = "<%=bundle.getString("help_termbase_exportprogress")%>";
var xmlExportOptions = 
	'<%=xmlExportOptions.replace("\\", "\\\\").replace("\r","").replace("\n","").trim()%>';

var WIDTH = 400;

eval("<%=errorScript%>");

var g_filename = "";

function showMessage(message)
{
    var div = document.createElement("DIV");
    div.innerText = message;
    idMessages.appendChild(div);

    if (idMessages.style.pixelHeight < 80)
    {
      idMessages.style.pixelHeight = 80;
    }

    idMessages.style.pixelHeight += 40;

    if (idMessages.style.pixelHeight > 120)
    {
      idMessages.style.pixelHeight = 120;
    }

    div.scrollIntoView(false);
}

function showProgress(entryCount, percentage, message)
{
  idProgress.innerText = entryCount.toString(10) + " entries (" +
    percentage.toString(10) + "%)";

  //idProgressBar.style.pixelWidth = Math.round((percentage / 100) * WIDTH);
  idProgressBar.style.width = Math.round((percentage / 100) * WIDTH);
  
  idProgressBar.style.minHeight = '15px';
  idProgressBar.innerHTML='&nbsp';    

  if (message != null && message != "")
  {
    showMessage(message);
  }
}

function parseExportOptions()
{
  var dom;
  dom = $.parseXML(xmlExportOptions);
  
  var node = $(dom).find("exportOptions fileOptions");
  g_filename = $(node).find("fileName").text();
<%
String companyFolder = "";
if (!"1".equals(CompanyThreadLocal.getInstance().getValue()))
{
    companyFolder = "/" + ((User) sessionMgr.getAttribute(WebAppConstants.USER)).getCompanyName();
}
%>
  //idExportfile.href =
    //"/globalsight/<%=IExportManager.EXPORT_DIRECTORY + companyFolder%>/" + g_filename;
}

function downloadFile()
{
  // window.open('/exports?file=' + g_filename + '&zip=true', '_blank');
  
  //idDownload.location.href = 
  document.getElementById("idDownload").src = 
    '/globalsight/exports?file=' + g_filename + '&zip=true';
}

function doContextMenu()
{
  var e = window.event;

  if (e.srcElement == idExportfile)
  {
    return true;
  }

  return false;
}

function doOk()
{
    window.navigate("<%=urlOk%>");
}

function doRefresh()
{
    idFrame.document.location = idFrame.document.location;
}

function doCancel()
{
    window.navigate("<%=urlCancel%>");
}

function done()
{
  idPleaseWait.style.visibility = 'hidden';
  //idDownloadLink.style.visibility = 'visible';

  idCancelOk.value = "<%=lb_OK%>";
  idCancelOk.onclick = doOk;

  idRefreshResult.value = "<%=lb_downloadFile%>";
  idRefreshResult.onclick = downloadFile;
  idRefreshResult.focus();
}

function doOnLoad()
{
   parseExportOptions();

   // Load the Guides
   loadGuides();
}
</SCRIPT>
</HEAD>
<BODY LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0" MARGINWIDTH="0"
 MARGINHEIGHT="0" CLASS="standardText" oncontextmenu="return doContextMenu()"
 onload="doOnLoad()">
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>

<DIV style="display:none">
<XML id="oExportOptions"><%=xmlExportOptions%></XML>
</DIV>

<DIV ID="contentLayer"
 STYLE=" POSITION: ABSOLUTE; Z-INDEX: 8; TOP: 108px; LEFT: 20px; RIGHT: 20px;">

<SPAN CLASS="mainHeading" id="idHeading"><%=lb_title%></SPAN>
<BR>
<SPAN CLASS="standardTextItalic" id="idPleaseWait">
<%=bundle.getString("lb_terminology_wait_until_export_finished")%>
</SPAN>

<DIV id="idProgressContainer">
  <DIV id="idProgress">0%</DIV>
</DIV>
<DIV id="idProgressBar"></DIV>

<BR>
<DIV id="idMessagesHeader" class="header"><%=bundle.getString("lb_terminology_export_messages")%></DIV>
<DIV id="idMessages"></DIV>

<DIV id="idLinks" style="width:500px">
  <INPUT TYPE="BUTTON" VALUE="<%=bundle.getString("lb_cancel")%>"
   id="idCancelOk" onclick="doCancel()"> &nbsp;
  <INPUT TYPE="BUTTON" VALUE="<%=bundle.getString("lb_refresh")%>"
   id="idRefreshResult" onclick="doRefresh()">

  <!--<DIV id="idDownloadLink" style="visibility: hidden">
    <BR>
    <%=bundle.getString("lb_terminology_download_the")%>
    <A href="" id="idExportfile" target="_new">
    <%=bundle.getString("lb_terminology_export_file")%></A>.
  </DIV>-->
</DIV>

</DIV>

<IFRAME id="idFrame" src="<%=urlRefresh%>" style="display:none"></IFRAME>
<IFRAME id="idDownload" NAME='download' SRC='about:blank' WIDTH='0' HEIGHT='0'></IFRAME>

</BODY>
</HTML>
