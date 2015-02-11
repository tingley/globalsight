<%@ page
    contentType="text/html; charset=UTF-8"
    errorPage="error.jsp"
    import="java.util.*,com.globalsight.everest.webapp.javabean.NavigationBean,
            com.globalsight.everest.webapp.pagehandler.PageHandler,
            com.globalsight.everest.webapp.pagehandler.edit.online.EditorConstants,
            com.globalsight.everest.webapp.pagehandler.edit.online.EditorState,
            com.globalsight.everest.edit.SynchronizationStatus,
            com.globalsight.everest.servlet.util.SessionManager,
            com.globalsight.everest.webapp.WebAppConstants,
            com.globalsight.util.edit.EditUtil,
            java.util.Locale,
            java.util.ResourceBundle"
    session="true"
%><%
ResourceBundle bundle = PageHandler.getBundle(session);

SessionManager sessionMgr = (SessionManager)session.getAttribute(
  WebAppConstants.SESSION_MANAGER);
EditorState state =
  (EditorState)sessionMgr.getAttribute(WebAppConstants.EDITORSTATE);

SynchronizationStatus oldStatus = state.getOldSynchronizationStatus();
SynchronizationStatus newStatus = state.getNewSynchronizationStatus();
String syncMessage = null;

if (newStatus != null)
{
    if (newStatus.getStatus().equals(SynchronizationStatus.UPLOAD_FINISHED))
    {
      syncMessage = "Offline translations were uploaded for this page.\n" +
        "Close and re-open the editor.";
      // re-open will clear status
    }
    else if (newStatus.getStatus().equals(oldStatus.getStatus()))
    {
      // Really: previous upload has finished and new upload has started.
      syncMessage = "Offline translations were uploaded for this page.\n" +
        "Close and re-open the editor.";
      // reload will clear status
    }
    else
    {
      syncMessage = "Offline translations are being uploaded for this page.\n" +
        "You should stop editing and wait until upload has finished.";
      // Show message once.
      state.setOldSynchronizationStatus(newStatus);
      state.setNewSynchronizationStatus(null);
    } 
}
%>
<HTML>
<HEAD>
<SCRIPT>
var g_syncMessage =
  "<%=syncMessage != null ? EditUtil.toJavascript(syncMessage) : ""%>";

function doLoad()
{
  parent.saveSegmentDone(g_syncMessage);
}
</SCRIPT>
</HEAD>
<BODY onload="doLoad()"></BODY>
</HTML>
