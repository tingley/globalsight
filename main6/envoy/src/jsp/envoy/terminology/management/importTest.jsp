<%@ page contentType="text/html; charset=UTF-8"
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
<jsp:useBean id="testrun" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="ok" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="cancel" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="refresh" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<%@ include file="/envoy/common/header.jspIncl" %>
<%
ResourceBundle bundle = PageHandler.getBundle(session);
SessionManager sessionMgr = (SessionManager)session.getAttribute(WebAppConstants.SESSION_MANAGER);
ProcessStatus m_status = null;
m_status = (ProcessStatus)sessionMgr.getAttribute(WebAppConstants.TERMBASE_STATUS);
int counter    = 0;
int percentage = -1;
String message = "";
if(m_status != null)
{
  counter    = m_status.getCounter();
  percentage = m_status.getPercentage();
  message = m_status.getMessage();
}
javax.servlet.jsp.JspWriter m_out = null;
String urlOk = ok.getPageURL();
String urlRefresh = refresh.getPageURL() +
    "&" + WebAppConstants.TERMBASE_ACTION +
    "=" + WebAppConstants.TERMBASE_ACTION_REFRESH;
String urlCancel = cancel.getPageURL() +
    "&" + WebAppConstants.TERMBASE_ACTION +
    "=" + WebAppConstants.TERMBASE_ACTION_CANCEL_IMPORT_TEST;
String refreshUrl = urlRefresh;
String refreshMetaTag = PageHandler.getRefreshMetaTagForProgressBar(refreshUrl);

String tbName = (String)sessionMgr.getAttribute(WebAppConstants.TERMBASE_TB_NAME);

// Perform error handling, then clear out session attribute.
String errorScript = "";
String error = (String)sessionMgr.getAttribute(WebAppConstants.TERMBASE_ERROR);
if (error != null)
{
  errorScript = "var error = new Error();" +
    "error.message = 'Import error';" +
    "error.description = '" + EditUtil.toJavascript(error) +
    "'; showError(error);";
}
sessionMgr.removeElement(WebAppConstants.TERMBASE_ERROR);
%>
<HTML>
<!-- This is envoy\terminology\management\importTest.jsp -->
<HEAD>
<%if((percentage > -1) && (percentage < 100)) {%>
<%=refreshMetaTag%>
<%}%>
<TITLE><%=bundle.getString("lb_testing_import")%></TITLE>
<STYLE>
#idProgressContainer { border: solid 1px <%=skin.getProperty("skin.list.borderColor")%>; z-index: 1; 
                 position: absolute; top: 150; left: 20; width: 400; }
#idProgressBar { background-color: #a6b8ce; z-index: 0;
                 border: solid 1px <%=skin.getProperty("skin.list.borderColor")%>; 
                 position: absolute; top: 150; left: 20; width: 0; }
#idProgress    { text-align: center; z-index: 2; font-weight: bold; }
#idMessagesHeader { position: absolute; top: 180; left: 20; font-weight: bold;}
#idMessages    { position: absolute; overflow: auto; z-index: 0; 
                 top: 210; left: 20; height: 80; width: 400; }
#idLinks       { position: absolute; left: 20; top: 422; z-index: 1; }
</STYLE>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>
<%@ include file="/includes/compatibility.jspIncl" %>
<SCRIPT src="/globalsight/envoy/terminology/management/protocol.js"></SCRIPT>
<SCRIPT LANGUAGE="JavaScript" SRC="envoy/terminology/viewer/viewerAPI.js"></SCRIPT>
<SCRIPT LANGUAGE="JavaScript">
var needWarning = false;
var objectName = "";
var guideNode = "terminology";
var helpFile = "<%=bundle.getString("help_termbase_import")%>";
var isFirefox = window.navigator.userAgent.indexOf("Firefox")>0;

</SCRIPT>
<SCRIPT language="Javascript1.2">
var WIDTH = 400;

eval("<%=errorScript%>");

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

    if (idMessages.style.pixelHeight > 200)
    {
      idMessages.style.pixelHeight = 200;
    }

    div.scrollIntoView(false);
}

function showProgress(entryCount, percentage, message)
{
  idProgress.innerText = entryCount.toString(10) + " entries (" +
    percentage.toString(10) + "%)";

  idProgressBar.style.pixelWidth = Math.round((percentage / 100) * WIDTH);
  idProgressBar.style.width = Math.round((percentage / 100) * WIDTH);
  if(isFirefox || window.navigator.userAgent.indexOf("Chrome")>0 )
  {
    idProgressBar.style.minHeight = '15px';
	idProgressBar.innerHTML='&nbsp';    
  }

  if (message != null && message != "" && message != " ")
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
    window.location.href = "<%=urlOk +
        "&" + WebAppConstants.TERMBASE_ACTION +
        "=" + WebAppConstants.TERMBASE_ACTION_SET_IMPORT_OPTIONS%>";
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
<%@ include file="/envoy/common/shortcutIcon.jspIncl" %>
</HEAD>
<BODY LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0" MARGINWIDTH="0"
  MARGINHEIGHT="0" CLASS="standardText" oncontextmenu="return false">

<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>

<DIV ID="contentLayer" 
    STYLE=" POSITION: ABSOLUTE; Z-INDEX: 8; TOP: 108px; LEFT: 20px; RIGHT: 20px;">

<SPAN CLASS="mainHeading" id="idHeading"><%=bundle.getString("lb_testing_import2")%></SPAN>
<BR>
<SPAN CLASS="standardTextItalic" id="idPleaseWait">
<%=bundle.getString("msg_please_wait_untill_test_import_finished")%>
</SPAN>
</DIV>

<DIV id="idProgressContainer">
<DIV id="idProgress"><%=percentage%>%</DIV>
</DIV>
<DIV id="idProgressBar"></DIV>
<BR>
<DIV id="idMessagesHeader" class="header"><%=bundle.getString("lb_import_msg")%></DIV>
<DIV id="idMessages">
</DIV>
<%
if (error == null)
{
  m_out = out;
  IImportManager importer = (IImportManager)sessionMgr.getAttribute(
      WebAppConstants.TERMBASE_IMPORTER);
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
          importer.detachListener((IProcessStatusListener)m_status);
          sessionMgr.removeElement(WebAppConstants.TERMBASE_STATUS);
      }
  }
}
else
{
  out.print("<SCRIPT>showMessage(\"" + EditUtil.toJavascript(error) +
    "\")</SCRIPT>");
}
%>


<P>
<DIV id="idLinks">
<% if(percentage > 99) { %>
<INPUT TYPE="BUTTON" NAME="OK" VALUE="<%=bundle.getString("lb_ok")%>" onclick="doOk()"> 
<%} else {%>
<INPUT TYPE="BUTTON" NAME="CANCEL" VALUE="<%=bundle.getString("lb_cancel")%>" onclick="doCancel()"> 
<INPUT TYPE="BUTTON" NAME="REFRESH" VALUE="<%=bundle.getString("lb_refresh")%>" onclick="doRefresh()"> 
<%}%>
</DIV>

</BODY>
</HTML>
