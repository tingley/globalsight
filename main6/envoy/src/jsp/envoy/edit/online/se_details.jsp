<%@ page
    contentType="text/html; charset=UTF-8"
    errorPage="error.jsp"
    import="java.util.*,com.globalsight.util.edit.EditUtil,
            com.globalsight.util.edit.GxmlUtil,
            com.globalsight.everest.edit.online.SegmentView,
            com.globalsight.everest.webapp.WebAppConstants,
            com.globalsight.everest.webapp.pagehandler.PageHandler,
            com.globalsight.everest.webapp.pagehandler.edit.online.EditorConstants,
	    com.globalsight.everest.webapp.pagehandler.edit.online.EditorState,
            com.globalsight.everest.servlet.util.SessionManager,
            com.globalsight.util.resourcebundle.ResourceBundleConstants,
            com.globalsight.util.resourcebundle.SystemResourceBundle,
            com.globalsight.everest.webapp.pagehandler.administration.users.UserUtil,
            java.util.Locale,
            java.util.ResourceBundle"
    session="true"
%>
<%
ResourceBundle bundle = PageHandler.getBundle(session);

SessionManager sessionMgr = (SessionManager)session.getAttribute(
  WebAppConstants.SESSION_MANAGER);
EditorState state =
  (EditorState)sessionMgr.getAttribute(WebAppConstants.EDITORSTATE);
SegmentView view =
  (SegmentView)sessionMgr.getAttribute(WebAppConstants.SEGMENTVIEW);

String lb_title = bundle.getString("lb_segment_details");
String lb_details = lb_title;

String lb_segmentId = bundle.getString("lb_segmentId");
String lb_segmentFormat = bundle.getString("lb_segmentFormat");
String lb_segmentType = bundle.getString("lb_segmentType");
String lb_wordCount = bundle.getString("lb_totalWordCount");
String lb_close = bundle.getString("lb_close");
String lb_tagInfo = bundle.getString("lb_tagInfo");

String str_segmentId = String.valueOf(state.getTuvId());
String str_segmentFormat = view.getDataType();
String str_segmentType = view.getItemType();
String str_wordCount = String.valueOf(view.getWordCount());
String str_sid = view.getTargetTuv().getSid();
if (str_sid == null || str_sid.trim().length()==0)
{
    str_sid = "N/A";
}
String str_lastModifyUser = view.getTargetTuv().getLastModifiedUser();
if (str_lastModifyUser == null || str_lastModifyUser.equalsIgnoreCase("xlf")
        || str_lastModifyUser.equalsIgnoreCase("Xliff"))
{
    str_lastModifyUser = "N/A";
}
%>
<HTML>
<HEAD>
<TITLE><%=lb_title%></TITLE>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<SCRIPT LANGUAGE="JavaScript">
function closeThis()
{
    window.close();
}
</SCRIPT>
</HEAD>
<BODY onload="window.focus()" oncontextmenu="return false">
<SPAN CLASS="mainHeading"><%=lb_details%></SPAN>
<P>
<TABLE CELLPADDING="2" CELLSPACING="0" BORDER="0">
  <TR class="standardText">
    <TD><B><%=lb_segmentId%>:</B></TD>
    <TD><%=str_segmentId%></TD>
  </TR>
  <TR class="standardText">
    <TD><B><%=lb_segmentFormat%>:</B></TD>
    <TD><%=str_segmentFormat%></TD>
  </TR>
  <TR class="standardText">
    <TD><B><%=lb_segmentType%>:</B></TD>
    <TD><%=str_segmentType%></TD>
  </TR>
  <TR class="standardText">
    <TD><B><%=lb_wordCount%>:</B></TD>
    <TD><%=str_wordCount%></TD>
  </TR>
  <TR>
  	<TD class="standardText" valign = top nowrap><B><SCRIPT>if(window.opener.GetDetails()){document.write("<%=lb_tagInfo%>:")}</SCRIPT></B></TD>
  	<TD><table><SCRIPT>document.write(window.opener.GetDetails().replace(/<tr>/g, "<TR valign=top>"));</SCRIPT></table></TD>
  </TR>
  <TR class="standardText">
    <TD><B><%=bundle.getString("lb_sid")%>:</B></TD>
    <TD><%=str_sid%></TD>
  </TR>
  <TR class="standardText">
    <TD><B><%=bundle.getString("lb_modify_by")%>:</B></TD>
    <TD><%=UserUtil.getUserNameById(str_lastModifyUser)%></TD>
  </TR>
  
</TABLE>
<P>
<CENTER>
<INPUT TYPE="BUTTON" VALUE="<%=lb_close%>" ONCLICK="closeThis()">
</CENTER>
</BODY>
</HTML>
