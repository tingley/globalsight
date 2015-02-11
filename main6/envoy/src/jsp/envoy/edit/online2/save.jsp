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
boolean syncClose = false;

if (newStatus != null)
{
    String status = newStatus.getStatus();
    if (status.equals(SynchronizationStatus.UPLOAD_FINISHED))
    {
      syncMessage = bundle.getString("jsmsg_editor_save_offline_uploaded");
      // re-open will clear status
    }
    else if (status.equals(SynchronizationStatus.GXMLUPDATE_STARTED) ||
             status.equals(SynchronizationStatus.GXMLUPDATE_FINISHED))
    {
      syncMessage = bundle.getString("jsmsg_editor_srcpage_is_being_edited");
      syncClose = true;
      // re-open will clear status
    }
    else if (status.equals(oldStatus.getStatus()))
    {
      // Really: previous upload has finished and new upload has started.
      syncMessage = bundle.getString("jsmsg_editor_save_offline_uploaded");
      // reload will clear status
    }
    else
    {
      syncMessage = bundle.getString("jsmsg_editor_save_offline_uploading");
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
var g_syncClose = eval("<%=syncClose%>");

function doLoad()
{
  parent.saveSegmentDone(g_syncMessage, g_syncClose);
}
</SCRIPT>
</HEAD>
<BODY onload="doLoad()"></BODY>
</HTML>
