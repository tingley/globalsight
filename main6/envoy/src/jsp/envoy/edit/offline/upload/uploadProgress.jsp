<%@ page
    contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="java.util.*,com.globalsight.everest.webapp.webnavigation.LinkHelper,
        com.globalsight.util.progress.IProcessStatusListener,
        com.globalsight.util.progress.ProcessStatus,
        com.globalsight.everest.edit.offline.OfflineEditManager,
        com.globalsight.everest.edit.offline.OfflineEditManagerLocal,
        com.globalsight.everest.webapp.pagehandler.offline.OfflineConstants,
        com.globalsight.everest.edit.offline.OEMProcessStatus,
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
<jsp:useBean id="errorPage" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean"/>
<%
   //Constants
   final String AND = "&";
   final String EQUAL = "=";

   ResourceBundle bundle = PageHandler.getBundle(session);

   
   SessionManager sessionMgr = (SessionManager)session.getAttribute(
      WebAppConstants.SESSION_MANAGER);
   Task task = (Task)sessionMgr.getAttribute(WebAppConstants.WORK_OBJECT);
   String url = done.getPageURL(); 
   
   // link to get back to task details page
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
    "&" + WebAppConstants.UPLOAD_ACTION +
    "=" + WebAppConstants.UPLOAD_ACTION_CANCEL;
String urlBack = back.getPageURL() + 
    "&" + WebAppConstants.UPLOAD_ACTION +
    "=" + WebAppConstants.UPLOAD_ACTION_BACK;
String urlRefresh = refresh.getPageURL() +
    "&" + WebAppConstants.UPLOAD_ACTION +
    "=" + WebAppConstants.UPLOAD_ACTION_REFRESH;
String urlProgress = refresh.getPageURL() +
    "&" + WebAppConstants.UPLOAD_ACTION +
    "=" + WebAppConstants.UPLOAD_ACTION_PROGRESS;
String errorPageUrl = errorPage.getPageURL();
String lb_cancel = bundle.getString("lb_cancel");
String lb_refresh = bundle.getString("lb_refresh");
String lb_done = bundle.getString("lb_done");
String lb_back = bundle.getString("lb_upload_again_report");
String lb_messages = bundle.getString("lb_messages");
String lb_upload = bundle.getString("lb_processing_upload_file");
String lb_please_wait = bundle.getString("lb_please_wait");
// success/failure message
String successMsg = bundle.getString("msg_upload_success");
String bigRedErrorTitle = bundle.getString("msg_upload_error_title");
String errorMsg = bundle.getString("msg_upload_failure");
String uploadErrors = bundle.getString("msg_upload_view_error");
OEMProcessStatus status = (OEMProcessStatus)sessionMgr.getAttribute(WebAppConstants.UPLOAD_STATUS);
int counter = 0;
int percentage = 0;
ArrayList messages = null;
if(status != null)
{
     counter    = status.getCounter();
     percentage = status.getPercentage();
     messages = status.giveMessages();
}

%>
<HTML>
<HEAD>
<TITLE><%=lb_upload%></TITLE>
<SCRIPT SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>
<SCRIPT src="/globalsight/includes/library.js"></SCRIPT>
<SCRIPT SRC="envoy/terminology/management/protocol.js"></SCRIPT>
<STYLE type="text/css">
<!--
#idProgressContainer { border: solid 1px <%=skin.getProperty("skin.list.borderColor")%>; z-index: 1; 
                 position: absolute; top: 42; left: 20; width: 400; }
#idProgressBar { background-color: #a6b8ce; z-index: 0;
                 border: solid 1px <%=skin.getProperty("skin.list.borderColor")%>; 
                 position: absolute; top: 42; left: 20; width: 0; height: 15;}
#idProgress    { text-align: center; z-index: 2; font-weight: bold; }
#idMessagesHeader { position: absolute; top: 72; left: 20; font-weight: bold;}
#idMessages    { position: absolute; overflow: auto; z-index: 0; 
                 top: 102; left: 20; height: 80; width: 400; }
#idLinks       { position: absolute; left: 20; top: 414; z-index: 1; }
#idCancel      { position: absolute; left: 20; top: 414; z-index: 1; }
#passMsg  { color: green;  }
-->
</STYLE>
<SCRIPT>
var needWarning = false;
var objectName = "";
var guideNode = "myActivitiesUpload";
var helpFile = "<%=bundle.getString("help_upload")%>";
var initDone = false;
var counter = <%=counter%>;
var percentage = <%=percentage%>;
var upldState = 1;
var WIDTH = 400;

function isIE(){ //ie? 
if (window.navigator.userAgent.toLowerCase().indexOf("msie")>=1) 
    return true; 
else 
    return false; 
} 

if(!isIE()){ //firefox innerText define
    HTMLElement.prototype.__defineGetter__("innerText", 
    function(){
        var anyString = "";
        var childS = this.childNodes;
        for(var i=0; i<childS.length; i++) { 
            if(childS[i].nodeType==1)
                //anyString += childS[i].tagName=="BR" ? "/n" : childS[i].innerText;
                anyString += childS[i].innerText;
            else if(childS[i].nodeType==3)
                anyString += childS[i].nodeValue;
        }
        return anyString;
    } 
    ); 
    HTMLElement.prototype.__defineSetter__("innerText", 
    function(sText){
        this.textContent=sText; 
    } 
    ); 
}

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
      idMessages.style.pixelHeight = 250;
    }

    div.scrollIntoView(false);
}

function showProgress(entryCount, percentage, message)
{
  idProgress.innerText = entryCount.toString(10) + " files (" +
    percentage.toString(10) + "%)";

  //idProgressBar.style.pixelWidth = Math.round((percentage / 100) * WIDTH);
  idProgressBar.style.width = Math.round((percentage / 100) * WIDTH);
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

function showFinalSuccessMessage()
{
    document.all.PassMsgLayer.style.visibility = "visible";
}

function showFinalPassFailMessage(state)
{
    var div = document.createElement("DIV");

    if(state==2) //pass 
    {
        div.innerHTML = "<P STYLE='color: green;' >OK.<BR><BR> " + "<%= successMsg %>";
        idMessages.appendChild(div);
        div.scrollIntoView(false); 
    }
    else if(state==3) // fail
    {
        div.innerHTML = "<P ALIGN='LEFT' CLASS='headingError'><A CLASS='headingError' HREF='<%=errorPageUrl%>'><%= bigRedErrorTitle %></A><BR><BR><%= errorMsg %><BR><BR><A CLASS='standardHREF' HREF='<%=errorPageUrl%>'><%= uploadErrors %></A></P>";
        idMessages.appendChild(div);
        div.scrollIntoView(false);        
    }
    else
    {
        alert("Unknown result");
    }
}
function done(state)
{

  idPleaseWait.style.visibility = 'hidden';

  idCancelOk.value = "<%=lb_back%>";
  idCancelOk.onclick = doBack;

  idRefreshResult.value = "<%=lb_done%>";
  idRefreshResult.onclick = doDone;
  idRefreshResult.disabled = false;

  showFinalPassFailMessage(state);
}

function doOnLoad()
{
  loadGuides();
  //window.setTimeout("callServer()", 5000, "JavaScript");
  <%
       if (messages != null)
       {
         for (int i = 0, max = messages.size(); i < max; i++)
         {
           String msg = (String)messages.get(i);
           out.print("parent.showProgress(");
           out.print(counter);
           out.print(",");
           out.print(percentage);
           out.print(",'");
           out.print(EditUtil.toJavascript(msg));
           out.println("');");
         }
       }
       else
       {
%>
                parent.showProgress(counter, percentage, "");
<%
       }
%>
  callServer("<%=urlProgress%>");
}

</SCRIPT>
</HEAD>
<BODY LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0" MARGINWIDTH="0"
  MARGINHEIGHT="0" CLASS="standardText" _oncontextmenu="return false"
  ONLOAD="doOnLoad()">
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>
<DIV ID="contentLayer" STYLE=" POSITION: ABSOLUTE; Z-INDEX: 8; TOP: 100px; LEFT: 20px; RIGHT: 20px;">
<SPAN CLASS="mainHeading" id="idHeading"><%=lb_upload%></SPAN><BR>
<SPAN CLASS="standardTextItalic" id="idPleaseWait"><%=lb_please_wait%></SPAN>

<DIV id="idProgressContainer">
  <DIV id="idProgress">0%</DIV>
</DIV>
<DIV id="idProgressBar"></DIV>

<DIV id="idLinks" style="width:500px">
  <INPUT TYPE="BUTTON" VALUE="<%=lb_cancel%>"
   id="idCancelOk" onclick="doCancel()"> &nbsp;
  <INPUT TYPE="BUTTON" VALUE="<%=lb_done%>"
   id="idRefreshResult" onclick="doDone()" disabled = "true">
  <BR>
</DIV>

<BR>
<DIV id="idMessagesHeader" class="header"><%=lb_messages%></DIV>
<DIV id="idMessages"></DIV>
</DIV>

</BODY>
</HTML>         

<SCRIPT LANGUAGE = "JavaScript">
var xmlHttp = false;
try {
  xmlHttp = new ActiveXObject("Msxml2.XMLHTTP");
} catch (e) {
  try {
    xmlHttp = new ActiveXObject("Microsoft.XMLHTTP");
  } catch (e2) {
    xmlHttp = false;
  }
}
if (!xmlHttp && typeof XMLHttpRequest != 'undefined') {
  xmlHttp = new XMLHttpRequest();
}

function callServer(url)
{
  //var url = "<%=urlRefresh%>";
  xmlHttp.open("POST", url, true);
  xmlHttp.onreadystatechange = updatePage;
  xmlHttp.send(null);
}
function updatePage() 
{
  if (xmlHttp.readyState == 4) 
  {
   if (xmlHttp.status == 200) 
   {
       var response = xmlHttp.responseText;
       var statusInfo = response;
       if(statusInfo != null && statusInfo.length != 0)
       {
            var status = statusInfo.split(",");
            percentage = status[0];
            counter = status[1];
            upldState = (status[2] == "true") ? 2 : 3;
            if(percentage >= 100)
            {
               showProgress(counter, percentage, "");
               done(upldState);
            }
            else
            {
               if(status[3] != 'undefined' && status[3].length != 0)
               {
                   for (var i = 3; i < status.length; i++)
                   {
                      var msg = status[i];
                      showProgress(counter, percentage, msg);
                   }
               }
               else
               {
                  showProgress(counter, percentage, "");
               }

			   setTimeout('callServer("<%=urlRefresh%>")',100);
            }
      }
      else
      {
		   setTimeout('callServer("<%=urlRefresh%>")',100);
      }
   }
  }
}
</SCRIPT>