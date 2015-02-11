<%@ page 
    contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="java.util.*,com.globalsight.everest.webapp.webnavigation.LinkHelper,
        com.globalsight.util.progress.IProcessStatusListener,
        com.globalsight.util.progress.ProcessStatus,
        com.globalsight.importer.IImportManager,
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
 class="com.globalsight.everest.webapp.javabean.NavigationBean"/>
  <jsp:useBean id="cancel" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean"/>
<jsp:useBean id="refresh" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean"/>
<%!
IImportManager m_manager = null;
javax.servlet.jsp.JspWriter m_out = null;
%>
<%
ResourceBundle bundle = PageHandler.getBundle(session);
SessionManager sessionMgr = (SessionManager)session.getAttribute(
  WebAppConstants.SESSION_MANAGER);

ProcessStatus m_status = null;
m_status = (ProcessStatus)sessionMgr.getAttribute(WebAppConstants.TM_TM_STATUS);
int counter    = 0;
int percentage = -1;
String message = "";
if(m_status != null)
{
  counter    = m_status.getCounter();
  percentage = m_status.getPercentage();
  message = m_status.getMessage();
}
String urlOk = ok.getPageURL();
String urlRefresh = refresh.getPageURL() +
    "&" + WebAppConstants.TM_ACTION +
    "=" + WebAppConstants.TM_ACTION_REFRESH;
String urlCancel = cancel.getPageURL() +
    "&" + WebAppConstants.TM_ACTION +
    "=" + WebAppConstants.TM_ACTION_CANCEL_IMPORT;
String refreshUrl = urlRefresh;
String refreshMetaTag = PageHandler.getRefreshMetaTagForProgressBar(refreshUrl);

String str_projectName =
  (String)sessionMgr.getAttribute(WebAppConstants.TM_TM_NAME);
String str_projectId =
  (String)sessionMgr.getAttribute(WebAppConstants.TM_TM_ID);

Object[] args = { str_projectName };
MessageFormat format = new MessageFormat(
  "Importing schedule data for project: {0}");
String lb_import_into_project = format.format(args);

// Perform error handling, then clear out session attribute.
String errorScript = "";
String error = (String)sessionMgr.getAttribute(WebAppConstants.TM_ERROR);
if (error != null)
{
  errorScript = "var error = new Error();" +
    "error.message = '" + EditUtil.toJavascript(bundle.getString("lb_import_error")) + "';" + 
    "error.description = '" + EditUtil.toJavascript(error) +
    "'; showError(error);";
}
sessionMgr.removeElement(WebAppConstants.TM_ERROR);
%>
<HTML>
<HEAD>
<%if((percentage > -1) && (percentage < 100)) {%>
<%=refreshMetaTag%>
<%}%>
<TITLE><%=bundle.getString("lb_import_progress")%></TITLE>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>
<%@ include file="/includes/compatibility.jspIncl" %>
<SCRIPT language="Javascript" src="/globalsight/includes/library.js"></SCRIPT>
<!-- To get showError and showWarning functions -->
<SCRIPT language="Javascript" src="envoy/tm/management/protocol.js"></SCRIPT>
<SCRIPT LANGUAGE="JavaScript">
var needWarning = false;
var objectName = "";
var guideNode = "projects";
var helpFile = "<%=bundle.getString("help_project_schedule_importing")%>";
</SCRIPT>
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
#idLinks       { position: absolute; left: 20; top: 314; z-index: 1; }
</STYLE>
<SCRIPT language="Javascript1.2">
var WIDTH = 400;

eval("<%=errorScript%>");

function showMessage(message)
{
    var div = document.createElement("DIV");
    div.innerHTML = message;//div.innerText = message;
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
  //
  idProgress.innerHTML = entryCount.toString(10) + " entries (" +
    percentage.toString(10) + "%)";

  //idProgressBar.style.pixelWidth = Math.round((percentage / 100) * WIDTH);
  idProgressBar.style.width = Math.round((percentage / 100) * WIDTH);
  //if(window.navigator.userAgent.indexOf("Firefox")>0 || window.navigator.userAgent.indexOf("Chrome")>0)
  //{
    idProgressBar.style.minHeight = '15px';
	idProgressBar.innerHTML='&nbsp';    
  //}
  
  if (message != null && message != "")
  {
    showMessage(message);
  }
}

function copyToClipboard()
{
  var range = document.body.createTextRange();
  range.moveToElementText(idMessages);
  window.clipboardData.setData("Text", range.text);
  window.status = "<%=EditUtil.toJavascript(bundle.getString("jsmsg_messages_copied_to_clipboard"))%>";
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
    window.location.href = "<%=urlRefresh%>";
}
</SCRIPT>
</HEAD>
<BODY LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0" MARGINWIDTH="0"
  MARGINHEIGHT="0" CLASS="standardText" oncontextmenu="return false"
  ONLOAD="loadGuides()">
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>

<DIV ID="contentLayer" 
 STYLE=" POSITION: ABSOLUTE; Z-INDEX: 8; TOP: 108px; LEFT: 20px; RIGHT: 20px;">
    
<SPAN CLASS="mainHeading" id="idHeading"><%=lb_import_into_project%></SPAN><BR>
<SPAN CLASS="standardTextItalic" id="idPleaseWait">
<%=bundle.getString("msg_please_wait_untill_import_finished")%>
</SPAN>

<DIV id="idProgressContainer">
  <DIV id="idProgress">0%</DIV>
</DIV>
<DIV id="idProgressBar"></DIV>

<BR>
<DIV id="idMessagesHeader" class="header"><%=bundle.getString("lb_import_msg")%></DIV>
<DIV id="idMessages"></DIV>

<%
if (error == null)
{
  out.flush();
  m_out = out;

  // Initiate server-side import and respond to import status events.
  m_manager = (IImportManager)sessionMgr.getAttribute(
    WebAppConstants.TM_IMPORTER);
  if(m_status != null)
  {
     m_out.print("<SCRIPT>");
     m_out.print("showProgress(" + counter + "," + percentage + ",'");
     if(message != null)
     {
         m_out.print(EditUtil.toJavascript(message));
     }
     m_out.println("');");
     m_out.print("</SCRIPT>");
     m_out.flush();
     if(percentage >= 100)
     {
         m_manager.detachListener((IProcessStatusListener)m_status);
         sessionMgr.removeElement(WebAppConstants.TM_TM_STATUS);
     }
 }
}
else
{
  out.print("<SCRIPT>showMessage(\"" + EditUtil.toJavascript(error) +
    "\")</SCRIPT>");
}
%>
<BR>
<DIV id="idLinks">
<% if(percentage > 99) { %>
<INPUT TYPE="BUTTON" NAME="OK" VALUE="<%=bundle.getString("lb_ok")%>" onclick="doOk()"> 
<%} else {%>
<INPUT TYPE="BUTTON" NAME="CANCEL" VALUE="<%=bundle.getString("lb_cancel")%>" onclick="doCancel()"> 
<INPUT TYPE="BUTTON" NAME="REFRESH" VALUE="<%=bundle.getString("lb_refresh")%>" onclick="doRefresh()"> 
<%}%>
</DIV>


</DIV>

</BODY>
</HTML>
