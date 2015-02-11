<%@ page
    contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="java.util.*,
			    com.globalsight.everest.webapp.webnavigation.LinkHelper,
			    com.globalsight.util.progress.IProcessStatusListener,
			    com.globalsight.util.progress.ProcessStatus,
			    com.globalsight.everest.edit.offline.OfflineEditManager,
			    com.globalsight.everest.edit.offline.OfflineEditManagerLocal,
			    com.globalsight.everest.webapp.pagehandler.offline.OfflineConstants,
			    com.globalsight.everest.edit.offline.OEMProcessStatus,java.util.ResourceBundle,
			    com.globalsight.everest.webapp.pagehandler.tasks.TaskHelper,
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
    ResourceBundle bundle = PageHandler.getBundle(session);
    String lb_cancel = bundle.getString("lb_cancel");
    String lb_refresh = bundle.getString("lb_refresh");
    String lb_done = bundle.getString("lb_done");
    String lb_back = bundle.getString("lb_upload_again_report");
    String lb_messages = bundle.getString("lb_messages");
    String lb_uploading_progress = bundle.getString("lb_uploading_progress");
    String lb_upload = bundle.getString("lb_processing_upload_file");
    String lb_please_wait = bundle.getString("lb_please_wait");
    // success/failure message
    String successMsg = bundle.getString("msg_upload_success");
    String cancelMsg = bundle.getString("msg_upld_cancel");
    String bigRedErrorTitle = bundle
            .getString("msg_upload_error_title");
    String errorMsg = bundle.getString("msg_upload_failure");
    String uploadErrors = bundle.getString("msg_upload_view_error");
    
    SessionManager sessionMgr = (SessionManager) session
            .getAttribute(WebAppConstants.SESSION_MANAGER);
    Task task = (Task) TaskHelper.retrieveObject(session, WebAppConstants.WORK_OBJECT);
    
    StringBuffer urlDone = new StringBuffer();
    urlDone.append(done.getPageURL());
    
    urlDone.append("&");
    urlDone.append(WebAppConstants.TASK_ACTION);
    urlDone.append("=");
    urlDone.append(WebAppConstants.TASK_ACTION_RETRIEVE);
    urlDone.append("&");
    urlDone.append(WebAppConstants.TASK_ID);
    urlDone.append("=");
    urlDone.append(task.getId());
    urlDone.append("&");
    urlDone.append(WebAppConstants.TASK_STATE);
    urlDone.append("=");
    urlDone.append(task.getState());
    
    String urlBack = back.getPageURL() + "&" + WebAppConstants.TASK_ID + "=" + task.getId();
    
    String progressUrl = refresh.getPageURL() + "&action=" + WebAppConstants.UPLOAD_ACTION_PROGRESS + "&taskId=" + task.getId();
    String refreshUrl = refresh.getPageURL() + "&action=" + WebAppConstants.UPLOAD_ACTION_REFRESH;
    
    OEMProcessStatus status = (OEMProcessStatus) sessionMgr
            .getAttribute(WebAppConstants.UPLOAD_STATUS);
    int counter = 0;
    int percentage = 0;
    ArrayList messages = null;
    if (status != null)
    {
        counter = status.getCounter();
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
<link href="/globalsight/jquery/jQueryUI.redmond.css" rel="stylesheet" type="text/css" />
<script src="/globalsight/jquery/jquery-1.6.4.min.js"></script>
<script src="/globalsight/jquery/jquery-ui-1.8.18.custom.min.js" type="text/javascript"></script>
<STYLE type="text/css">
#idProgressContainer { border: solid 1px <%=skin.getProperty("skin.list.borderColor")%>; z-index: 1; 
                 position: absolute; top: 42; left: 20; width: 400; }
#idProgressBar { background-color: #a6b8ce; z-index: 0;
                 border: solid 1px <%=skin.getProperty("skin.list.borderColor")%>; 
                 position: absolute; top: 42; left: 20; width: 0; height: 15;}
#idProgress    { text-align: center; z-index: 2; font-weight: bold; }
#idMessagesHeader { top: 72; left: 20; font-weight: bold;}
#translationStatus { left: 20; font-weight: bold;}
#idMessages    { overflow: auto; z-index: 0; top: 102; left: 20;}
#idLinks       { left: 20; top: 414; z-index: 1; }
#idCancel      { position: absolute; left: 20; top: 414; z-index: 1; }
#passMsg  { color: green;  }
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
var cancelProcess = false;

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
	var reg = /[0-9]+%$/;
	var isInt = reg.test(message);
	if (isInt && idMessages.hasChildNodes)
	{
       var x = idMessages.lastChild;
	   var content = x.innerHTML;
	   content = content.replace(reg, "");
	   content += message;
	   x.innerHTML = content;
	}
   else
   {
	    var div = document.createElement("DIV");
	    div.innerHTML = message;
	    idMessages.appendChild(div);
	    div.scrollIntoView(false);
   }
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
	cancelProcess = true;
	location.replace('<%=cancel.getPageURL()%>');
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

function showFinalPassFailMessage(state, message)
{
    var div = document.createElement("DIV");

    if(state==2) //pass 
    {
    	if (idMessages.hasChildNodes)
	    {
	      var x = idMessages.lastChild;
		  var content = x.innerHTML;
		  var reg = /[0-9]+%$/;
		  var isInt = reg.test(content);

		  if (isInt)
		  {
			  content = content.replace(reg, "");
			  content += "100%";
			  x.innerHTML = content;
		  }					  
	    }
	    
        div.innerHTML = "<P STYLE='color: green;' >OK.<BR><BR> " + "<%=successMsg%>";
        idMessages.appendChild(div);
        div.scrollIntoView(false); 
    }
    else if(state==3) // fail
    {
        div.innerHTML = "<br><span align='left' class='headingError'><%=bigRedErrorTitle%><BR><BR>"
             + message + "</span>" + "<p style='color:black'>For further information on how to resolve this issue, please refer to <a href='http://www.globalsight.com/wiki/index.php/Troubleshooting_translation_problems' class='standardHREF' target='_blank'>GlobalSight troubleshooting documentation</a>.</p>";
        idMessages.appendChild(div);
        div.scrollIntoView(false);
    }
    else
    {
        alert("Unknown result");
    }
}
function done(state, message)
{

  idPleaseWait.style.visibility = 'hidden';

  idCancelOk.value = "<%=lb_back%>";
  idCancelOk.onclick = doBack;

  idRefreshResult.value = "<%=lb_done%>";
  idRefreshResult.onclick = doDone;
  idRefreshResult.disabled = false;

  showFinalPassFailMessage(state, message);
}

function doOnLoad()
{
  loadGuides();
  
  <%if (messages != null)
    {
	  for (int i = 0, max = messages.size(); i < max; i++)
      {
          String msg = (String) messages.get(i);
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
    {%>
      parent.showProgress(counter, percentage, "");
  <%}%>
  
  callServer("<%=progressUrl%>");
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

<div style="position:absolute;top:72;left:20">
	<table cellspacing=0 cellpadding=0 border=0 class=standardText>
	<tr>
		<td>
			<DIV id="warningDiv" style = "display:none;color:red"><%=lb_uploading_progress%></DIV>
		</td>
	</tr>
	<tr>
		<td>
			<DIV id="idMessagesHeader" class="header"><br/><%=lb_messages%></DIV>
		</td>
	</tr>
	<tr>
		<td>
			<DIV id="idMessages"></DIV>
		</td>
	</tr>
	<tr>
		<td>
			<DIV id="idLinks" style="width:500px">
				<INPUT TYPE="BUTTON" class=standardText VALUE="<%=lb_cancel%>" id="idCancelOk" onclick="doCancel()"> &nbsp;
			  	<INPUT TYPE="BUTTON" class=standardText VALUE="<%=lb_done%>" id="idRefreshResult" onclick="doDone()" disabled="true">
			</DIV>
		</td>
	</tr>
	</table>
</div>


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
  xmlHttp.open("POST", url, true);
  xmlHttp.onreadystatechange = updatePage;
  xmlHttp.send(null);
}

function updatePage() 
{
	 var warningDiv = document.getElementById("warningDiv");
	 warningDiv.style.display = "block";
	 
	if (xmlHttp.readyState == 4) 
  	{
   		if (xmlHttp.status == 200) 
   		{
   			if (cancelProcess)
   				return;
   			
       		var response = xmlHttp.responseText;
       		var statusInfo = response;
       		
       		if(statusInfo != null && statusInfo.length != 0)
			{
       			var result = eval("(" + statusInfo + ")");
       			
            	percentage = result.percentage;
            	counter = result.counter;
            	upldState = 2 ;
            	if(percentage >= 100)
            	{
            		var msg = result.msg;
            		if (msg != null){
            			for (var i = 0; i < msg.length; i++)
                       	{
    						var m = msg[i];
    						showProgress(counter, percentage, m);
                       	}
            		}
            		
					showProgress(counter, percentage, "");
					var message = "";
					if (result.errMsg != null && result.errMsg.length > 0){
						message = result.errMsg;
						upldState = 3;
					}
               		done(upldState, message);
               		warningDiv.style.display = "none";
            	}
            	else
            	{
            		var msg = result.msg;
            		if (msg != null){
            			for (var i = 0; i < msg.length; i++)
                       	{
    						var m = msg[i];
    						showProgress(counter, percentage, m);
                       	}
            		}           		
               		
            		if (result.process != null && result.process.length > 0)
            		{
            			showProgress(counter, percentage, result.process);
            		}

			   		setTimeout('callServer("<%=refreshUrl%>")',1000);
            	}
      		}
      		else
      		{
		   		setTimeout('callServer("<%=refreshUrl%>")',1000);
      		}
   		}
	}
}
</SCRIPT>