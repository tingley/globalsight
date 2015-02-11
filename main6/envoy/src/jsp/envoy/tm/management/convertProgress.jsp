<%@ page contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="java.util.*,com.globalsight.everest.webapp.webnavigation.LinkHelper,java.util.ResourceBundle,java.text.MessageFormat,com.globalsight.util.edit.EditUtil,com.globalsight.everest.webapp.pagehandler.PageHandler,com.globalsight.everest.webapp.WebAppConstants,com.globalsight.everest.servlet.util.SessionManager,java.io.IOException"
    session="true"%>
<%@ include file="/envoy/common/header.jspIncl"%>
<jsp:useBean id="self" scope="request"
    class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="ok" scope="request"
    class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<%
    ResourceBundle bundle = PageHandler.getBundle(session);
    SessionManager sessionMgr = (SessionManager) session
            .getAttribute(WebAppConstants.SESSION_MANAGER);

    String urlSelf = self.getPageURL() + "&"
            + WebAppConstants.TM_ACTION + "=";
    String urlRefresh = urlSelf + WebAppConstants.TM_ACTION_CONVERT_REFRESH;
    String urlCancel = urlSelf + WebAppConstants.TM_ACTION_CONVERT_CANCEL;
    String urlOk = ok.getPageURL();

    String str_databaseName = (String) sessionMgr
            .getAttribute(WebAppConstants.TM_TM_NAME);
    String str_databaseId = (String) sessionMgr
            .getAttribute(WebAppConstants.TM_TM_ID);

    Object[] args = { str_databaseName };
    MessageFormat format = new MessageFormat(bundle
            .getString("lb_tm_convert_to_tm3"));
    String lb_convert_to_tm3 = format.format(args);

    String lb_copiedToClipboard = bundle
            .getString("jsmsg_messages_copied_to_clipboard");
    String lb_next = bundle.getString("lb_next");

    // Perform error handling, then clear out session attribute.
    String errorScript = "";
    String error = (String) sessionMgr
            .getAttribute(WebAppConstants.TM_ERROR);
    if (error != null) {
        errorScript = "var error = new Error();"
                + "error.message = '"
                + EditUtil.toJavascript(bundle
                        .getString("lb_import_error")) + "';"
                + "error.description = '"
                + EditUtil.toJavascript(error) + "'; showError(error);";
    }
    sessionMgr.removeElement(WebAppConstants.TM_ERROR);

%>
<HTML>
<HEAD>
<TITLE><%=bundle.getString("lb_tm_convert_progress")%></TITLE>
<STYLE>
#idProgressContainer {
    border: solid 1px <%=       skin .     
         getProperty("skin.list.borderColor") %>;
    z-index: 1;
    position: absolute;
    top: 82;
    left: 20;
    width: 400;
}

#idProgressBar {
    background-color: #a6b8ce;
    z-index: 0;
    border: solid 1px <%=       skin .     
         getProperty("skin.list.borderColor") %>;
    position: absolute;
    top: 82;
    left: 20;
    width: 0;
}

#idProgress {
    text-align: center;
    z-index: 2;
    font-weight: bold;
}

#idMessagesHeader {
    position: absolute;
    top: 72;
    left: 20;
    font-weight: bold;
}

#idMessages {
    position: absolute;
    overflow: auto;
    z-index: 0;
    top: 62;
    left: 20;
    height: 80;
    width: 400;
    font-weight: bold;
}

#idLinks {
    position: absolute;
    left: 20;
    top: 154;
    z-index: 1;
}
</STYLE>
<SCRIPT SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl"%>
<%@ include file="/envoy/common/warning.jspIncl"%>
<SCRIPT SRC="/globalsight/includes/library.js"></SCRIPT>
<SCRIPT>

var xmlHttp = getXmlHttp();
var isRunning = true;

function getXmlHttp()
{
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
    
    return xmlHttp;
}

function callServer(url)
{
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
       if (response != "end")
       {
    	   isRuning = true;
           var status = response.split("|");
           showProgress(status[0], status[1]);
           if (status[2] == "true")
           {
               done();
           }
           else
           {
               setTimeout('callServer("<%=urlRefresh%>")',500);
           }
       }
   }
  }
}

var needWarning = false;
var objectName = "";
var guideNode = "tm";
var helpFile = "<%=bundle.getString("help_progressbar")%>";

var WIDTH = 400;

eval("<%=errorScript%>");

function showProgress(percentage, message)
{
   idProgress.innerHTML = percentage.toString(10) + "%";

   idProgressBar.style.width = Math.round((percentage / 100) * WIDTH);
   if (window.navigator.userAgent.indexOf("Firefox")>0 || window.navigator.userAgent.indexOf("Chrome")>0)
   {
     idProgressBar.style.height = 15;
     idProgressBar.innerHTML='&nbsp';
   }

   if (message != null && message != "")
   {
      idMessages.innerHTML = message;
   }
}

function doOk()
{
    window.location.href = "<%=urlOk%>";
}

function done()
{
  isRunning = false;
  document.getElementById("idOk").disabled = false;
  document.getElementById("idCancel").disabled = true;
  document.getElementById("idRefresh").disabled = true;
}

function doCancel() {
    if (confirm("<%=bundle.getString("msg_tm3_convert_cancel_confirm")%>")) {
		callServer("<%=urlCancel%>");
		document.getElementById("idOk").disabled = false;
	    document.getElementById("idCancel").disabled = true;
		document.getElementById("idRefresh").disabled = false;
	}
    window.location.href = "<%=urlOk%>";
}

function doRefresh() {
	callServer("<%=urlRefresh%>");
}

function doOnLoad()
{
  loadGuides();
  callServer("<%=urlRefresh%>");
}

</SCRIPT>
</HEAD>
<BODY LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0" MARGINWIDTH="0"
    MARGINHEIGHT="0" CLASS="standardText" oncontextmenu="return false"
    ONLOAD="doOnLoad()">
<%@ include file="/envoy/common/navigation.jspIncl"%>
<%@ include file="/envoy/wizards/guides.jspIncl"%>

<DIV ID="contentLayer"
    STYLE="POSITION: ABSOLUTE; Z-INDEX: 8; TOP: 108px; LEFT: 20px; RIGHT: 20px;">

<SPAN CLASS="mainHeading" id="idHeading"><%=lb_convert_to_tm3%></SPAN>
<BR>
<SPAN CLASS="standardTextItalic" id="idPleaseWait"> <%=bundle
                                    .getString("msg_please_wait_untill_convert_finished")%>
</SPAN> <BR>

<DIV id="idMessages" class="header"></DIV>
<DIV id="idProgressContainer">
<DIV id="idProgress">0%</DIV>
</DIV>
<DIV id="idProgressBar"></DIV>

<BR>
<DIV id="idLinks" style="width: 500px">
<INPUT TYPE="BUTTON"
    NAME="OK" style="width: 60px;"
    VALUE="<%=bundle.getString("lb_ok")%>" id="idOk"
    onclick="doOk()" disabled>&nbsp;&nbsp;
<INPUT TYPE="BUTTON"
    NAME="btnCancel" style="width: 60px;"
    VALUE="<%=bundle.getString("lb_cancel")%>" id="idCancel"
    onclick="doCancel()">&nbsp;&nbsp;
<INPUT TYPE="BUTTON"
    NAME="btnRefresh" style="width: 60px;"
    VALUE="<%=bundle.getString("lb_refresh")%>" id="idRefresh"
    onclick="doRefresh()">&nbsp;&nbsp;
    
<BR>
</DIV>
</DIV>
</BODY>
</HTML>
