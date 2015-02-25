<%@ page
    contentType="text/html; charset=UTF-8"
    errorPage="error.jsp"
    import="com.globalsight.everest.webapp.javabean.NavigationBean,
            com.globalsight.everest.webapp.pagehandler.PageHandler,
            com.globalsight.everest.webapp.pagehandler.edit.online.EditorConstants,
            com.globalsight.everest.webapp.pagehandler.edit.online.EditorState,
            com.globalsight.everest.webapp.WebAppConstants,
            com.globalsight.everest.servlet.util.SessionManager,
            java.util.*,
            com.globalsight.everest.webapp.pagehandler.projects.workflows.JobManagementHandler"
    session="true"
%>
<jsp:useBean id="self" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="pane1" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<jsp:useBean id="pane2" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<%
String pane1URL = pane1.getPageURL();
String pane2URL = pane2.getPageURL();
String selfURL  = self.getPageURL();

String splitAttribute = null;

SessionManager sessionMgr = (SessionManager)session.getAttribute(
  WebAppConstants.SESSION_MANAGER);
EditorState state =
  (EditorState)sessionMgr.getAttribute(WebAppConstants.EDITORSTATE);
EditorState.Layout layout = state.getLayout();

if (layout.isHorizontal())
{
        splitAttribute = "ROWS='12%,44%,44%' ";
}
else
{
        splitAttribute = "COLS='12%,44%,44%' ";
}
%>
<HTML>
<HEAD>
<SCRIPT>
function SwitchTargetLocale(p_locale)
{
    document.location = "<%=selfURL%>&refresh=0&trgViewLocale=" + p_locale;
}

function HighlightSegment(p_tuId, p_tuvId, p_subId)
{
    target.HighlightSegment(p_tuId, p_tuvId, p_subId);
}

function UnhighlightSegment(p_tuId, p_tuvId, p_subId)
{
    target.UnhighlightSegment(p_tuId, p_tuvId, p_subId);
}

function RefreshTargetPane()
{
    target.RefreshTargetPane();
}
</SCRIPT>
</HEAD>
<FRAMESET <%=splitAttribute%> FRAMEBORDER="yes" BORDER="5" id="inctxrv_fset">
<FRAME SRC="envoy/edit/inctxrv/pdf/filelist.jsp" NAME="filelist" SCROLLING="auto" NORESIZE
  MARGINHEIGHT="0" MARGINWIDTH="0">
 <FRAME SRC="<%=pane1URL%>" NAME="source" SCROLLING="auto" NORESIZE
  MARGINHEIGHT="0" MARGINWIDTH="0">
 <FRAME SRC="<%=pane2URL%>" NAME="target" SCROLLING="auto" NORESIZE
  MARGINHEIGHT="0" MARGINWIDTH="0">
</FRAMESET>
</HTML>
