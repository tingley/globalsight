<%@ page
    contentType="text/html; charset=UTF-8"
    errorPage="error.jsp"
    import="com.globalsight.util.edit.EditUtil,
            com.globalsight.util.edit.GxmlUtil,
            com.globalsight.everest.edit.online.PageInfo,
            com.globalsight.everest.webapp.WebAppConstants,
            com.globalsight.everest.webapp.javabean.NavigationBean,
            com.globalsight.everest.webapp.pagehandler.PageHandler,
            com.globalsight.everest.webapp.pagehandler.edit.online.EditorConstants,
	    com.globalsight.everest.webapp.pagehandler.edit.online.EditorState,
            com.globalsight.util.resourcebundle.ResourceBundleConstants,
            com.globalsight.util.resourcebundle.SystemResourceBundle,
            com.globalsight.everest.servlet.util.SessionManager,
            java.util.*"
    session="true"
%>
<%
ResourceBundle bundle = PageHandler.getBundle(session);

SessionManager sessionMgr = (SessionManager)session.getAttribute(
  WebAppConstants.SESSION_MANAGER);
EditorState state =
  (EditorState)sessionMgr.getAttribute(WebAppConstants.EDITORSTATE);
PageInfo info =
  (PageInfo)sessionMgr.getAttribute(WebAppConstants.PAGEINFO);

Locale uiLocale = (Locale)session.getAttribute(WebAppConstants.UILOCALE);

String lb_close   = bundle.getString("lb_close");

String lb_title = bundle.getString("lb_page_info");
String lb_details = lb_title;

String lb_pageId          = bundle.getString("lb_pageId");
String lb_pageName        = bundle.getString("lb_pageName");
String lb_pageFormat      = bundle.getString("lb_pageFormat");
String lb_totalWordCount  = bundle.getString("lb_totalWordCount");
String lb_segmentCount    = bundle.getString("lb_segmentCount");
String lb_sourceLocale    = bundle.getString("lb_source_locale");
String lb_targetLocale    = bundle.getString("lb_target_locale");

String str_pageId         = state.getSourcePageId().toString();
String str_pageName       = info.getPageName();
String str_pageFormat     = info.getPageFormat();
String str_totalWordCount = String.valueOf(info.getWordCount());
String str_segmentCount   = String.valueOf(info.getSegmentCount());
String str_sourceLocale   = state.getSourceLocale().getDisplayName(uiLocale);
String str_targetLocale   = state.getTargetLocale().getDisplayName(uiLocale);

session.removeAttribute(WebAppConstants.PAGEINFO);
%>
<HTML>
<HEAD>
<TITLE><%=lb_title%></TITLE>
<STYLE>
.link { color: blue; text-decoration: underline; cursor: hand;  cursor:pointer;}
</STYLE>
<SCRIPT SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<SCRIPT>
function closeThis()
{
    window.close();
}

function doKeyDown()
{
  var key = event.keyCode;

  if (key == 27) // "ESC"
  {
      event.cancelBubble = true;
      event.returnValue = false;
      closeThis();
  }
}
</SCRIPT>
</HEAD>
<BODY onload="window.focus(); idClose.focus();" onkeydown="doKeyDown()"
 oncontextmenu="return false">
<SPAN class="mainHeading"><%=lb_details%></SPAN>
<HR>
<TABLE WIDTH="100%" CELLPADDING="0" CELLSPACING="0" BORDER="0"
  class="standardText" style="table-layout: fixed">
    <COL width="30%" VALIGN="TOP">
    <COL width="70%" VALIGN="TOP">
  <TBODY>
    <TR>
      <TD><%=lb_pageName%></TD>
      <TD style="width: 100%; word-wrap: break-word"><%=str_pageName%></TD>
    </TR>
    <TR>
      <TD><%=lb_pageId%></TD>
      <TD><%=str_pageId%></TD>
    </TR>
    <TR>
      <TD><%=lb_totalWordCount%></TD>
      <TD><%=str_totalWordCount%></TD>
    </TR>
    <TR>
      <TD><%=lb_segmentCount%></TD>
      <TD><%=str_segmentCount%></TD>
    </TR>
    <TR>
      <TD><%=lb_sourceLocale%></TD>
      <TD><%=str_sourceLocale%></TD>
    </TR>
    <TR>
      <TD><%=lb_targetLocale%></TD>
      <TD><%=str_targetLocale%></TD>
    </TR>
  </TBODY>
</TABLE>
<HR>
<CENTER>
<INPUT TYPE="BUTTON" VALUE="<%=lb_close%>" ID="idClose" ONCLICK="closeThis()">
</CENTER>
</BODY>
</HTML>
