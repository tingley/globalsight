<%@ page
    contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="java.util.*,com.globalsight.everest.webapp.webnavigation.LinkHelper,
        com.globalsight.util.progress.ProcessStatus,
        com.globalsight.exporter.IExportManager,
        java.util.ResourceBundle,
        java.text.MessageFormat,
        com.globalsight.util.edit.EditUtil,
        com.globalsight.everest.webapp.pagehandler.PageHandler,
        com.globalsight.everest.webapp.WebAppConstants,
        com.globalsight.everest.servlet.util.SessionManager,
        java.io.IOException"
    session="true"
%>
<%@ include file="/envoy/common/header.jspIncl" %>
<jsp:useBean id="ok" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
 <jsp:useBean id="cancel" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean"/>
<jsp:useBean id="refresh" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean"/>
<%
ResourceBundle bundle = PageHandler.getBundle(session);
SessionManager sessionMgr = (SessionManager)session.getAttribute(
  WebAppConstants.SESSION_MANAGER);

String xmlExportOptions =
  ((String)sessionMgr.getAttribute(WebAppConstants.TM_EXPORT_OPTIONS)).replaceAll("\"","&quot;");
String str_databaseName =
  (String)sessionMgr.getAttribute(WebAppConstants.TM_TM_NAME);
String str_databaseId =
  (String)sessionMgr.getAttribute(WebAppConstants.TM_TM_ID);

String urlOk = ok.getPageURL();
String urlRefresh = refresh.getPageURL() +
    "&" + WebAppConstants.TM_ACTION +
    "=" + WebAppConstants.TM_ACTION_REFRESH;
String urlCancel = cancel.getPageURL() +
    "&" + WebAppConstants.TM_ACTION +
    "=" + WebAppConstants.TM_ACTION_CANCEL_EXPORT;

String lb_title = bundle.getString("lb_tm_exporting") + " " + str_databaseName;
String lb_downloadFile = bundle.getString("lb_download_export_file");
String lb_OK = bundle.getString("lb_ok");

// Perform error handling, then clear out session attribute.
String errorScript = "";
String error = (String)sessionMgr.getAttribute(WebAppConstants.TM_ERROR);
if (error != null)
{
  errorScript = "var error = new Error();" +
    "error.message = '" + EditUtil.toJavascript(bundle.getString("lb_export_error")) + "';" +
    "error.description = '" + EditUtil.toJavascript(error) +
    "'; showError(error);";
}
sessionMgr.removeElement(WebAppConstants.TM_ERROR);
%>
<HTML XMLNS:gs>
<!-- This is envoy\tm\management\exportProgress.jsp -->
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
#idLinks       { position: absolute; left: 20; top: 246; width: 400; z-index: 1; }
.clickable     { cursor: hand; cursor:pointer }

#idLinks A,
#idLinks A:active,
#idLinks A:hover,
#idLinks A:link,
#idLinks A:visited      { color: blue; text-decoration: none; }
#tmxLogo       { position: absolute; left: 20; top: 310; width: 400; z-index: 1; }
</STYLE>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>
<SCRIPT type="text/javascript" src="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<SCRIPT type="text/javascript" src="/globalsight/includes/library.js"></SCRIPT>
<SCRIPT type="text/javascript" src="envoy/tm/management/protocol.js"></SCRIPT>
<SCRIPT type="text/javascript" src="/globalsight/jquery/jquery-1.6.4.min.js"></SCRIPT>
<SCRIPT>
var needWarning = false;
var objectName = "";
var guideNode = "tm";
var helpFile = "<%=bundle.getString("help_tm_exportprogress")%>";
var xmlExportOptions = "<%=xmlExportOptions.replace("\n","").replace("\r","").trim()%>";

var WIDTH = 400;

eval("<%=errorScript%>");

var g_filename = "";

function showMessage(message)
{
    var div = document.createElement("DIV");
    div.innerHTML = message;
	
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

    // div.scrollIntoView(false);
}

// Show Progress Bar.
function showProgress(entryCount, percentage, message)
{
  $("#idProgress").html(entryCount.toString(10) + " <%=bundle.getString("lb_entries").toLowerCase() %> (" 
		  + percentage.toString(10) + "%)");

  $("#idProgressBar").width(Math.round((percentage / 100) * WIDTH));
  if(!$.browser.msie)
  {
	$("#idProgressBar").width($("#idProgressBar").width() + 2);
	$("#idProgressBar").css({height:'17px', border:'0'});
  }
  else if ($.browser.version > 9) // IE 10 Version
  {
	$("#idProgressBar").width($("#idProgressBar").width() + 2);
	$("#idProgressBar").css({height:'16px', border:'0'});
  }
  
  if (message != null && message != "")
  {
    showMessage(message);
  }
}

function parseExportOptions()
{
  var $xml = $( $.parseXML( xmlExportOptions ) );
  g_filename = $xml.find("exportOptions > fileOptions > fileName").text();
  document.getElementById("idExportfile").href =
    "/globalsight/exports?file=" + g_filename + "&fileType=tm ";
}

function downloadFile()
{
  document.getElementById("idDownload").src = 
	  '/globalsight/exports?file=' + g_filename + '&fileType=tm' + '&zip=true';
}

function showResources()
{
  var s = idResources.style;

  if (s.display == '')
  {
    s.display = 'none';
    idResourcesHandle.src = '/globalsight/images/expandArrow.gif';
    idResourcesHandle.title = 'Show';
    tmxLogo.style.top = 310;
  }
  else
  {
    s.display = '';
    idResourcesHandle.src = '/globalsight/images/collapseArrow.gif';
    idResourcesHandle.title = 'Hide';
    tmxLogo.style.top = 405;
  }
}

function doContextMenu()
{
  var e = window.event;

  if (e.srcElement.id == "idExportfile" || e.srcElement.id == "idResource")
  {
    return true;
  }

  return false;
}

function doOk()
{
	window.location.href = "<%=urlOk%>";
}

function doCancel()
{
    window.location.href = "<%=urlCancel%>";
}

function doRefresh()
{
    idFrame.document.location = idFrame.document.location;
}

function done()
{
  document.getElementById("idPleaseWait").style.visibility = 'hidden';
  document.getElementById("idAdditionalResources").style.display = 'block';

  document.getElementById("idCancelOk").value = "<%=lb_OK%>";
  document.getElementById("idCancelOk").onclick = doOk;

  document.getElementById("idRefreshResult").value = "<%=lb_downloadFile%>";
  document.getElementById("idRefreshResult").onclick = downloadFile;
}

function doOnLoad()
{
   loadGuides();

   parseExportOptions();
}
</SCRIPT>
</HEAD>
<BODY LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0" MARGINWIDTH="0"
 MARGINHEIGHT="0" CLASS="standardText" oncontextmenu="return doContextMenu()"
 onload="doOnLoad()">
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>

<DIV  ID="contentLayer"
 STYLE=" POSITION: ABSOLUTE; Z-INDEX: 8; TOP: 108px; LEFT: 20px; RIGHT: 20px;">

<SPAN CLASS="mainHeading" id="idHeading"><%=lb_title%></SPAN>
<BR>
<SPAN CLASS="standardTextItalic" id="idPleaseWait">
  <%=bundle.getString("lb_tm_wait_until_export_finished")%>
</SPAN>

<DIV id="idProgressContainer">
  <DIV id="idProgress">0%</DIV>
</DIV>
<DIV id="idProgressBar"></DIV>

<BR>
<DIV id="idMessagesHeader" class="header">
	<%=bundle.getString("lb_tm_export_messages")%></DIV>
<DIV id="idMessages"></DIV>

<DIV id="idLinks" align="left">
  <INPUT TYPE="BUTTON" NAME="CANCEL" VALUE="<%=bundle.getString("lb_cancel")%>"
   id="idCancelOk" onclick="doCancel()"> &nbsp;&nbsp;
  <INPUT TYPE="BUTTON" NAME="REFRESH" VALUE="<%=bundle.getString("lb_refresh")%>"
   id="idRefreshResult" onclick="doRefresh()">
  <BR>
  <DIV id="idAdditionalResources" style="display:none">
    <%=bundle.getString("lb_tm_additional_resource") %>
    <img src="/globalsight/images/expandArrow.gif" class="clickable"
     id="idResourcesHandle" onclick="showResources()" title="Show">
    <div id="idResources" style="margin-left: 50px; display: none;">
       <table border=1 style="border-collapse: collapse">
	 <col align="right" CLASS="standardText"
	 style="padding-left: 5px; padding-right: 5px;">
	 <col align="left" CLASS="standardText"
	 style="padding-left: 10px; padding-right: 10px;">
	 <tr>
	   <td CLASS="standardText"><%=bundle.getString("lb_tm_export_file") %>:</td>
	   <td CLASS="standardText"><A href="filled in by script" id="idExportfile" target="_new">
	     <%=bundle.getString("lb_tm_export_file")%></A>
	   </td>
	 </tr>
	 <tr>
	   <td CLASS="standardText"><%=bundle.getString("lb_tm_tms_dtd") %> v1.4:</td>
	   <td CLASS="standardText"><A href="/globalsight/downloadresource?file=tmx14.dtd"
	     id="idResource" target="_new">tmx14.dtd</A>
	   </td>
	 </tr>
	 <tr>
	   <td CLASS="standardText"><%=bundle.getString("lb_tm_gs_tmx_dtd") %>:</td>
	   <td CLASS="standardText"><A href="/globalsight/downloadresource?file=tmx-gs.dtd"
	     id="idResource" target="_new">tmx-gs.dtd</A>
	   </td>
	 </tr>
       </table>
    </div>
  </DIV>
</DIV>

<DIV ID="tmxLogo">
<TABLE>
<TR><TD ALIGN="LEFT"><IMG SRC="/globalsight/images/TMX.gif"></TD></tr>
<TR><TD ALIGN="LEFT"><SPAN CLASS="smallText"><%=bundle.getString("lb_tmx_logo_text1")%><BR><%=bundle.getString("lb_tmx_logo_text2")%></SPAN></TD></TR>
</TABLE>
</DIV>

<IFRAME id="idFrame" src="<%=urlRefresh%>" style="display:none"></IFRAME>
<IFRAME id="idDownload" NAME='download' SRC='about:blank' WIDTH='0' HEIGHT='0' style="display:none"></IFRAME>

</BODY>
</HTML>
