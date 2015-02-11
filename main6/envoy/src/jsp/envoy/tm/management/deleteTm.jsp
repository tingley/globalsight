<%@ page
    contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="java.util.*,
        com.globalsight.everest.webapp.webnavigation.LinkHelper,
        com.globalsight.util.progress.ProcessMonitor,
        java.util.ResourceBundle,
        java.text.MessageFormat,
        com.globalsight.util.edit.EditUtil,
        com.globalsight.everest.webapp.pagehandler.PageHandler,
        com.globalsight.everest.webapp.WebAppConstants,
        com.globalsight.everest.servlet.util.SessionManager,
        java.io.IOException"
    session="true"
%>
<jsp:useBean id="ok" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
 <jsp:useBean id="cancel" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean"/>
<jsp:useBean id="refresh" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean"/>
<jsp:useBean id="status" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean"/>
<%
ResourceBundle bundle = PageHandler.getBundle(session);
SessionManager sessionMgr = (SessionManager)session.getAttribute(
  WebAppConstants.SESSION_MANAGER);

ProcessMonitor processMonitor =
  (ProcessMonitor)sessionMgr.getAttribute(WebAppConstants.TM_REMOVER);

boolean hasFinished = processMonitor.hasFinished();
int counter = processMonitor.getCounter();
int percentage = processMonitor.getPercentage();
String errorState = Boolean.toString(processMonitor.isError());
String replacingMessage = processMonitor.getReplacingMessage();

String urlOk = ok.getPageURL();
String urlRefresh = refresh.getPageURL();
String urlCancel = cancel.getPageURL() +
    "&" + WebAppConstants.TM_ACTION +
    "=" + WebAppConstants.TM_ACTION_CANCEL;
String urlStatus = status.getPageURL();

String lb_title = bundle.getString("lb_tm_removing");
String lb_OK = bundle.getString("lb_ok");

// Perform error handling, then clear out session attribute.
String errorScript = "";
String error = (String)sessionMgr.getAttribute(WebAppConstants.TM_ERROR);
if (error != null)
{
  errorScript = "var error = new Error();" + "error.message = '" +
    bundle.getString("lb_tm_remove_error") + "';" +
    "error.description = '" + error +
    "'; showError(error);";
}
sessionMgr.removeElement(WebAppConstants.TM_ERROR);
%>
<HTML XMLNS:gs>
<HEAD>
<TITLE><%=lb_title%></TITLE>
<STYLE>
#idProgressContainer { border: solid 1px #0C1476; z-index: 1; 
                 position: absolute; top: 42; left: 20; width: 400; }
#idProgressBar { background-color: #a6b8ce; z-index: 0;
                 border: solid 1px #0C1476%>; 
                 position: absolute; top: 42; left: 20; width: 0; }
#idProgress    { text-align: center; z-index: 2; font-weight: bold; }
#idMessagesHeader { position: absolute; top: 72; left: 20; font-weight: bold;}
#idMessage     { position: absolute; overflow: auto; z-index: 0;
                 top: 102; left: 20; height: 240; width: 400;}
#idLinks       { position: absolute; left: 20; top: 366; width: 400; z-index: 1; }
.clickable     { cursor: hand; }

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
<SCRIPT SRC="/globalsight/includes/library.js"></SCRIPT>
<SCRIPT SRC="/globalsight/includes/xmlHttpInit.js"></SCRIPT>
<SCRIPT SRC="/globalsight/includes/progressBarUpdate.js"></SCRIPT>

<SCRIPT>
var needWarning = false;
var objectName = "";
var guideNode = "tm";
var helpFile = "<%=bundle.getString("help_tm_remove")%>";

<%=errorScript%> 

function doOk()
{
    window.navigate("<%=urlOk%>");
}

function doCancel()
{
    window.location.href = "<%=urlCancel%>";
}

function doRefresh()
{
    window.location.href = "<%=urlRefresh%>";
}

function done()
{
  idPleaseWait.style.visibility = 'hidden';

  idCancelOk.value = "<%=lb_OK%>";
  idCancelOk.onclick = doOk;

//  idRefreshResult.disabled = true;
}

var wheel = ["|", "/", "-", "\\"];
var wheelCounter = 0;

function getProgressText(counter, percentage)
{
    var wheelIndex = wheelCounter++ % 4;
    return percentage + "%   " + (percentage != 100 ? wheel[wheelIndex] : "");
}

function doOnLoad()
{
   loadGuides();

   initProgressBar("<%=urlStatus%>", idProgressBar,
       400, idProgress, idMessage, null, 3000, done, getProgressText);

   // initial progress bar values
   setProgressData(<%=counter%>, <%=percentage%>, "<%=errorState%>",
       "<%=EditUtil.decodeXmlEntities(replacingMessage)%>", null);

   if ("<%=hasFinished%>" == "true")
   {
       done();
   }
   else
   {
       // initiate AJAX call
       setTimeout("progressBarUpdate()", 3000);
   }
}
</SCRIPT>
</HEAD>
<BODY LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0" MARGINWIDTH="0"
 MARGINHEIGHT="0" CLASS="standardText" onload="doOnLoad()">
<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>


<DIV  ID="contentLayer"
 STYLE=" POSITION: ABSOLUTE; Z-INDEX: 8; TOP: 108px; LEFT: 20px; RIGHT: 20px;">

<SPAN CLASS="mainHeading" id="idHeading"><%=lb_title%></SPAN>
<BR>
<SPAN CLASS="standardTextItalic" id="idPleaseWait">
  <%=bundle.getString("lb_tm_wait_until_tm_remove_finished")%>
</SPAN>

<DIV id="idProgressContainer">
  <DIV id="idProgress"></DIV>
</DIV>
<DIV id="idProgressBar"></DIV>

<BR>
<DIV id="idMessagesHeader" class="header">
	<%=bundle.getString("lb_tm_remove_messages")%></DIV>
<DIV id="idMessage"></DIV>

<DIV id="idLinks" align="left">
  <INPUT TYPE="BUTTON" NAME="CANCEL" VALUE="<%=bundle.getString("lb_cancel")%>"
   id="idCancelOk" onclick="doCancel()"> &nbsp;&nbsp;
  <INPUT TYPE="BUTTON" NAME="REFRESH" VALUE="<%=bundle.getString("lb_refresh")%>"
   id="idRefreshResult" onclick="doRefresh()">
  <BR>
</DIV>


</BODY>
</HTML>
