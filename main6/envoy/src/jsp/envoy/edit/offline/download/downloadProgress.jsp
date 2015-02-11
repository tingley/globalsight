<%@ page
    contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="java.util.*,com.globalsight.everest.webapp.webnavigation.LinkHelper,
        com.globalsight.util.progress.IProcessStatusListener,
        com.globalsight.util.progress.ProcessStatus,
        com.globalsight.everest.edit.offline.OfflineEditManager,
        com.globalsight.everest.edit.offline.OfflineEditManagerLocal,
        java.util.ResourceBundle,
        java.text.MessageFormat,
        com.globalsight.util.edit.EditUtil,
        com.globalsight.everest.webapp.pagehandler.PageHandler,
        com.globalsight.everest.webapp.WebAppConstants,
        com.globalsight.everest.servlet.util.SessionManager,
        com.globalsight.everest.taskmanager.Task,
        java.io.IOException"
    session="true"
%>
<%@ include file="/envoy/common/header.jspIncl" %>
<jsp:useBean id="done" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean"/>
 <jsp:useBean id="cancel" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean"/>
 <jsp:useBean id="back" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean"/>
<jsp:useBean id="refresh" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean"/>
<jsp:useBean id="senddownloadfile" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<%
   //Constants
   final String AND = "&";
   final String EQUAL = "=";
   
   ResourceBundle bundle = PageHandler.getBundle(session);

   
   SessionManager sessionMgr = (SessionManager)session.getAttribute(
      WebAppConstants.SESSION_MANAGER);
  
   Task task = (Task)sessionMgr.getAttribute(WebAppConstants.WORK_OBJECT);
   String url = done.getPageURL(); 
   
   // build the normal task link
   StringBuffer urlDone = new StringBuffer();
   urlDone.append(url);
   urlDone.append(AND);
   urlDone.append(WebAppConstants.TASK_ACTION);
   urlDone.append(EQUAL);
   urlDone.append(WebAppConstants.TASK_ACTION_RETRIEVE);
   urlDone.append(AND);
   urlDone.append(WebAppConstants.TASK_ID);
   urlDone.append(EQUAL);
   urlDone.append(task.getId());
   urlDone.append(AND);
   urlDone.append(WebAppConstants.TASK_STATE);
   urlDone.append(EQUAL);
   urlDone.append(task.getState());

String urlCancel = cancel.getPageURL() +
    "&" + WebAppConstants.DOWNLOAD_ACTION +
    "=" + WebAppConstants.DOWNLOAD_ACTION_CANCEL+
    //GBS 2913 add task id and state
    "&" + WebAppConstants.TASK_ID+
    "=" + task.getId()+
    "&" + WebAppConstants.TASK_STATE+
    "=" + task.getState();
String urlBack = back.getPageURL() + 
    "&" + WebAppConstants.DOWNLOAD_ACTION +
    "=" + WebAppConstants.DOWNLOAD_ACTION_BACK+
    //GBS 2913 add task id and state
    "&" + WebAppConstants.TASK_ID+
    "=" + task.getId()+
    "&" + WebAppConstants.TASK_STATE+
    "=" + task.getState();
String urlRefresh = refresh.getPageURL() +
    "&" + WebAppConstants.DOWNLOAD_ACTION +
    "=" + WebAppConstants.DOWNLOAD_ACTION_REFRESH+
    //GBS 2913 add task id and state
    "&" + WebAppConstants.TASK_ID+
    "=" + task.getId()+
    "&" + WebAppConstants.TASK_STATE+
    "=" + task.getState();
String urlDownloadZip = senddownloadfile.getPageURL() + 
    "&" + WebAppConstants.DOWNLOAD_ACTION +
    "=" + WebAppConstants.DOWNLOAD_ACTION_DONE+
    //GBS 2913 add task id and state
    "&" + WebAppConstants.TASK_ID+
    "=" + task.getId()+
    "&" + WebAppConstants.TASK_STATE+
    "=" + task.getState();

String lb_download_zip_file = bundle.getString("lb_download_zip_file");
String lb_cancel = bundle.getString("lb_cancel");
String lb_refresh = bundle.getString("lb_refresh");
String lb_done = bundle.getString("lb_done");
String lb_back = bundle.getString("lb_download_again");
String lb_messages = bundle.getString("lb_messages");
String lb_download = bundle.getString("lb_preparing_download_file");
String lb_please_wait = bundle.getString("lb_please_wait");
%>
<HTML>
<HEAD>
<TITLE><%=lb_download%></TITLE>
<SCRIPT SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>
<SCRIPT src="/globalsight/includes/library.js"></SCRIPT>
<SCRIPT SRC="envoy/terminology/management/protocol.js"></SCRIPT>
<STYLE>
#idProgressContainer { border: solid 1px <%=skin.getProperty("skin.list.borderColor")%>; z-index: 1; 
                 position: absolute; top: 42; left: 20; width: 400; }
#idProgressBar { background-color: #a6b8ce; z-index: 0;
                 border: solid 1px <%=skin.getProperty("skin.list.borderColor")%>; 
                 position: absolute; top: 42; left: 20; width: 0; }
#idProgress    { text-align: center; z-index: 2; font-weight: bold;}
#idMessagesHeader { position: absolute; top: 72; left: 20; font-weight: bold;}
#idMessages    { position: absolute; overflow: auto; z-index: 0; 
                 top: 102; left: 20; height: 80; width: 400; }
#idLinks       { position: absolute; left: 20; top: 314; z-index: 1; }
#idCancel      { position: absolute; left: 20; top: 314; z-index: 1; }
</STYLE>
<SCRIPT>
var needWarning = false;
var objectName = "";
var guideNode = "myActivitiesDownload";
var helpFile = "<%=bundle.getString("help_progressbar")%>";

var WIDTH = 400;

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

    if (idMessages.style.pixelHeight > 200)
    {
      idMessages.style.pixelHeight = 200;
    }

    div.scrollIntoView(false);
}

function showProgress(entryCount, percentage, message)
{
  
  document.getElementById("idProgress").innerHTML = 
	  entryCount.toString(10) + " files (" + percentage.toString(10) + "%)";

  document.getElementById("idProgressBar").style.width = 
	  Math.round((percentage / 100) * WIDTH);
  //if(window.navigator.userAgent.indexOf("Firefox")>0 || window.navigator.userAgent.indexOf("Chrome")>0) {
	  document.getElementById("idProgressBar").style.height = 15;
  //}

  if (message != null && message != "")
  {
    showMessage(message);
  }
}

function doCancel()
{
    window.location.href = "<%=urlCancel%>";
}

function doBack()
{
    window.location.href = "<%=urlBack%>";
}

function doDone()
{
    window.location.href = "<%=urlDone%>";
}

function doRefresh()
{
    idFrame.document.location = idFrame.document.location;
}

function doDownload()
{
    idFrame.document.location = "<%=urlDownloadZip%>";
}

function done(canDownload)
{
  idPleaseWait.style.visibility = 'hidden';

  idCancelOk.value = "<%=lb_back%>";
  idCancelOk.onclick = doBack;

  idRefreshResult.value = "<%=lb_done%>";
  idRefreshResult.onclick = doDone;

  if (canDownload)
  {
    doDownload();
  }
}

function doOnLoad()
{
  loadGuides();
}

</SCRIPT>
</HEAD>
<BODY LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0" MARGINWIDTH="0"
  MARGINHEIGHT="0" CLASS="standardText" _oncontextmenu="return false"
  ONLOAD="doOnLoad()">
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>

<DIV ID="contentLayer" 
 STYLE=" POSITION: ABSOLUTE; Z-INDEX: 8; TOP: 108px; LEFT: 20px; RIGHT: 20px;">
    
<SPAN CLASS="mainHeading" id="idHeading"><%=lb_download%></SPAN><BR>
<SPAN CLASS="standardTextItalic" id="idPleaseWait"><%=lb_please_wait%></SPAN>

<DIV id="idProgressContainer">
  <DIV id="idProgress">0%</DIV>
</DIV>
<DIV id="idProgressBar"></DIV>

<BR>
<DIV id="idMessagesHeader" class="header"><%=lb_messages%></DIV>
<DIV id="idMessages"></DIV>

<DIV id="idLinks" style="width:500px">
  <INPUT TYPE="BUTTON" VALUE="<%=lb_cancel%>"
   id="idCancelOk" onclick="doCancel()"> &nbsp;
  <INPUT TYPE="BUTTON" VALUE="<%=lb_refresh%>"
   id="idRefreshResult" onclick="doRefresh()">
</DIV>

<IFRAME id="idFrame" name="idFrame" src="<%=urlRefresh%>" style="display:none"></IFRAME>

</DIV>
</DIV>
</BODY>
</HTML>                                                 
