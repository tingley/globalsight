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
    String lb_upload = bundle.getString("lb_processing_upload_file");
    String lb_please_wait = bundle.getString("lb_please_wait");
    // success/failure message
    String successMsg = bundle.getString("msg_upload_success");
    String cancelMsg = bundle.getString("msg_upld_cancel");
    String bigRedErrorTitle = bundle
            .getString("msg_upload_error_title");
    String errorMsg = bundle.getString("msg_upload_failure");
    String uploadErrors = bundle.getString("msg_upload_view_error");
    //Constants
    final String AND = "&";
    final String EQUAL = "=";

    SessionManager sessionMgr = (SessionManager) session
            .getAttribute(WebAppConstants.SESSION_MANAGER);
    //Fix for GBS-2191, from task upload page or simple offline upload page
    boolean fromTaskUpload = WebAppConstants.UPLOAD_FROMTASKUPLOAD
            .equals(sessionMgr
                    .getAttribute(WebAppConstants.UPLOAD_ORIGIN));
    String urlBack = back.getPageURL() + "&"
            + WebAppConstants.UPLOAD_ACTION + "="
            + WebAppConstants.UPLOAD_ACTION_BACK;
    Task task = null;
    String url = done.getPageURL();
    StringBuffer urlDone = new StringBuffer();
    urlDone.append(url);
    if(fromTaskUpload)
    {
    	task =(Task) sessionMgr.getAttribute(WebAppConstants.WORK_OBJECT);
        // link to get back to task details page
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
      //GBS 2913
        urlBack += "&"+WebAppConstants.TASK_ID+
                	"="+task.getId();
    }
    String urlCancel = cancel.getPageURL() + "&"
            + WebAppConstants.UPLOAD_ACTION + "="
            + WebAppConstants.UPLOAD_ACTION_CANCEL;
    
    String urlRefresh = refresh.getPageURL() + "&"
            + WebAppConstants.UPLOAD_ACTION + "="
            + WebAppConstants.UPLOAD_ACTION_REFRESH;
    String urlProgress = refresh.getPageURL() + "&"
            + WebAppConstants.UPLOAD_ACTION + "="
            + WebAppConstants.UPLOAD_ACTION_PROGRESS;
    String urlCancelProcess =  refresh.getPageURL() + "&"
            + WebAppConstants.UPLOAD_ACTION + "="
            + WebAppConstants.UPLOAD_ACTION_CANCE_PROGRESS;
    String urlConfirmContinue =  refresh.getPageURL() + "&"
            + WebAppConstants.UPLOAD_ACTION + "="
            + WebAppConstants.UPLOAD_ACTION_CONFIRM_CONTINUE;
    String errorPageUrl = errorPage.getPageURL();
	String translatedTextUrl = refresh.getPageURL() + "&" 
    		+WebAppConstants.UPLOAD_ACTION + "=" 
			+WebAppConstants.TASK_ACTION_TRANSLATED_TEXT_RETRIEVE;
    OEMProcessStatus status = (OEMProcessStatus) sessionMgr
            .getAttribute(WebAppConstants.UPLOAD_STATUS);
    int counter = 0;
    int percentage = 0;
    ArrayList messages = null;
    Set<Long> taskIdsSet = null;
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
#translateds { overflow: auto; z-index: 0; left: 20;}
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

function closeDialog()
{
	document.getElementById('uploadFormDiv').parentNode.style.display = "none";
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

function doContinue()
{
	closeDialog();

	var url = "<%=urlConfirmContinue%>&isContinue=y";
	$.ajax({
	     type: "GET",
	     url: url,
	     cache:false,
			   success: function(data, textStatus){
				   setTimeout('callServer("<%=urlRefresh%>")',100);
			   }
	});
}

function doNotContinue()
{
	closeDialog();
	var url = "<%=urlConfirmContinue%>&isContinue=n";
	$.ajax({
		type: "GET",
		url: url,
		cache: false,
		success: function(data, textStatus){
//		cancelProcess = true;
 	   
 	   var div = document.createElement("DIV");
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
 	   
 	   div.innerHTML = "<P class='headingError' >" + "<%=bundle.getString("msg_internal_moved_cancel")%>" + $("#internalMsg")[0].innerHTML;
        //idMessages.appendChild(div);
        setTimeout('callServer("<%=urlRefresh%>")',100);
        //div.scrollIntoView(false); 
        
        //idPleaseWait.style.visibility = 'hidden';

        //idCancelOk.value = "<%=lb_back%>";
        //idCancelOk.onclick = doBack;

        //idRefreshResult.value = "<%=lb_done%>";
        //idRefreshResult.onclick = doDone;
        //idRefreshResult.disabled = false;
		}
	});
	
}

function doCancel()
{
	var obj = {
			inputJobIDS : "<%=task == null ? -1 :task.getJobId()%>"
	}
	  $.ajax({
		     type: "POST",
		     url: "<%=urlCancelProcess%>",
		     dataType:'json',
		     cache:false,
		     data: obj,
				   success: function(json){
					   cancelProcess = true;
			    	   
			    	   var div = document.createElement("DIV");
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
			    	   
			    	   div.innerHTML = "<P class='headingError' >" + "<%=cancelMsg%>";
			           idMessages.appendChild(div);
			           div.scrollIntoView(false); 
			           
			           idPleaseWait.style.visibility = 'hidden';

			           idCancelOk.value = "<%=lb_back%>";
			           idCancelOk.onclick = doBack;

			           idRefreshResult.value = "<%=lb_done%>";
			           idRefreshResult.onclick = doDone;
			           idRefreshResult.disabled = false;
				   }
		     });
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
  //window.setTimeout("callServer()", 5000, "JavaScript");
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
  callServer("<%=urlProgress%>");
}


function translatedText(taskId){
	var date = new Date();
	if(taskId != null && taskId != ""){
		var translationStatus = document.getElementById("translationStatus");
    	translationStatus.style.display = "block";
		var urlJSON = "<%= translatedTextUrl%>";
		urlJSON += "&taskParam=" + taskId + "&date=" + date;
		callBack(urlJSON);
	}
}

function callBack(urlJSON){
	$.getJSON(urlJSON,function(data){
		 if(data != null){
			 var jobId = data.jobId;
			 var jobName = data.jobName;
			 var filePercents = data.taskId;
			 var outDiv = document.createElement("DIV");
			 var outContent = "<SPAN class=standardText style='font-weight:600;'>JobId :"+jobId+"</SPAN><br/>";
			 outContent += "<SPAN class=standardText style='font-weight:600;'>JobName :"+jobName+"</SPAN><br/>";
			 outDiv.innerHTML = outContent;
			 translateds.appendChild(outDiv);
			 outDiv.scrollIntoView(false); 
			var filePercentsArr = filePercents.substring(0, filePercents.length-2);
			var filePercentArr = filePercentsArr.split("||");
			for(var i = 0; i < filePercentArr.length;i++){
			    var insideDiv = document.createElement("DIV");
			    var namePerc = filePercentArr[i];
			    var namePercArr = namePerc.split("<>");
				var content = "";
				if(namePercArr[1] <100){
				     content += "<SPAN class=standardText style='color:red'>"+namePercArr[0]+"</SPAN>&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp";
					 content += "<SPAN class=standardText style='color:red'>("+namePercArr[1]+"%)</SPAN><br/>";
				}else{
					 content += "<SPAN class=standardText style='color:black'>"+namePercArr[0]+"</SPAN>&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp";
					 content += "<SPAN class=standardText style='color:black'>("+namePercArr[1]+"%)</SPAN><br/>";
				}
				 insideDiv.innerHTML = content;
				translateds.appendChild(insideDiv);
				insideDiv.scrollIntoView(false); 
			 }
		 }
		 var div = document.createElement("DIV");
		 var content="<br/>";
		 div.innerHTML = content;
		translateds.appendChild(div);
		div.scrollIntoView(false); 
	});
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
			<DIV id="warningDiv" style = "display:none;color:red">Offline uploading is in progress, please do not close this page or browser...</DIV>
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
			<DIV id="translationStatus" class = "header" style = "display:none">Translation Status</DIV>
		</td>
	</tr>
	<tr>
		<td>
			<DIV id="translateds"></DIV>
		</td>
	</tr>
	<tr>
		<td>
			<DIV id="idLinks" style="width:500px">
				<INPUT TYPE="BUTTON" VALUE="<%=lb_cancel%>" id="idCancelOk" onclick="doCancel()"> &nbsp;
			  	<INPUT TYPE="BUTTON" VALUE="<%=lb_done%>" id="idRefreshResult" onclick="doDone()" disabled="true">
			</DIV>
		</td>
	</tr>
	</table>
</div>


</DIV>

<div id="uploadFormDiv" title="<%=bundle.getString("msg_internal_moved_title") %>"
    style="display:none" class="standardText">
  <table style="width: 650px;" >
    <tr>
      <td colspan="2">
          <div CLASS="standardText">
              <div style="padding-bottom: 6px; " CLASS="standardText"><%=bundle.getString("msg_internal_moved_continue") %></div>
              <div id="internalMsg" CLASS="standardText">
              </div>
          </div>      		  
	  </td>
    </tr>
    <tr>
      <td colspan="2">&nbsp;</td>     
    </tr>
    <tr class="standardText">
      <td colspan="2"  align="center" valign="middle" >
          <button type="button" onclick="doContinue()" style="width: 50px;" class="standardText"><%=bundle.getString("lb_yes") %></button>
          <button type="button" onclick="doNotContinue();"  style="width: 50px;" ><%=bundle.getString("lb_no") %></button>
      </td>
    </tr>
  </table>
</div>
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
            		//var taskIds = result.taskIdsSet;
            		var taskIsCheckUnTran = result.taskIsCheckUnTran;
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

               		// Display translation percentage regardless there is error or not.
               	    if(taskIsCheckUnTran != null && taskIsCheckUnTran.length > 0)
               	    {
                        for(var i=0;i < taskIsCheckUnTran.length; i++){
                            var taskTran = taskIsCheckUnTran[i];
                            var taskTranArr = taskTran.split(",");
                            var taskId = taskTranArr[0];
                            translatedText(taskId);
                        }
                    }
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

            		if (result.internalTagMiss != null && result.internalTagMiss.length > 0)
            		{
            			 $("#internalMsg")[0].innerHTML = result.internalTagMiss;
            			 $("#internalMsg")[0].className = "standardText";
                		$("#uploadFormDiv").dialog({width: 675, resizable:true});
                		document.getElementById('uploadFormDiv').parentNode.style.display = "inline";
                		document.getElementById('uploadFormDiv').style.display = "inline";
            			 return;
            		}
            		
			   		setTimeout('callServer("<%=urlRefresh%>")',1000);
            	}
      		}
      		else
      		{
		   		setTimeout('callServer("<%=urlRefresh%>")',1000);
      		}
   		}
	}
}
</SCRIPT>