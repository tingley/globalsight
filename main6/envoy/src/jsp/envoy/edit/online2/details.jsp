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
            java.util.Locale,
            java.util.ResourceBundle"
    session="true"
%>
<%
ResourceBundle bundle = PageHandler.getBundle(session);

SessionManager sessionMgr = (SessionManager)session.getAttribute(
  WebAppConstants.SESSION_MANAGER);

String lb_title = bundle.getString("lb_segment_details");
String lb_details = lb_title;

String lb_segmentId = bundle.getString("lb_segmentId");
String lb_segmentFormat = bundle.getString("lb_segmentFormat");
String lb_segmentType = bundle.getString("lb_segmentType");
String lb_wordCount = bundle.getString("lb_totalWordCount");
String lb_close = bundle.getString("lb_close");
String lb_tagInfo = bundle.getString("lb_tagInfo");
%>
<HTML>
<HEAD>
<TITLE><%=lb_title%></TITLE>
<SCRIPT SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<SCRIPT>
function closeThis()
{
    window.close();
}

function doLoad()
{
  window.focus();
  idOk.focus();
}
</SCRIPT>
</HEAD>
<BODY onload="doLoad()" oncontextmenu="return false">
<SPAN CLASS="mainHeading"><%=lb_details%></SPAN>
<P>
<TABLE CELLPADDING="2" CELLSPACING="0" BORDER="0">
  <TR class="standardText">
    <TD><B><%=lb_segmentId%>:</B></TD>
    <TD><SCRIPT>document.write(window.opener.GetSegmentId());</SCRIPT></TD>
  </TR>
  <TR class="standardText">
    <TD><B><%=lb_segmentFormat%>:</B></TD>
    <TD><SCRIPT>document.write(window.opener.GetSegmentFormat());</SCRIPT></TD>
  </TR>
  <TR class="standardText">
    <TD><B><%=lb_segmentType%>:</B></TD>
    <TD><SCRIPT>document.write(window.opener.GetSegmentType());</SCRIPT></TD>
  </TR>
  <!-- data not available 
  <TR class="standardText">
    <TD><B><%=lb_wordCount%>:</B></TD>
    <TD><SCRIPT>document.write(window.opener.GetSegmentWordCount());</SCRIPT></TD>
  </TR>
  -->
  </TR>
    <TR>
  	<TD class="standardText" valign = top nowrap><B><SCRIPT>if(window.opener.GetDetails()){document.write("<%=lb_tagInfo%>:")}</SCRIPT></B></TD>
  	<TD><table><SCRIPT>document.write(window.opener.GetDetails().replace(/<tr>/g, "<TR valign=top>"));</SCRIPT></table></TD>
  </TR>
 </TABLE>
<P>
<CENTER>
<INPUT TYPE="BUTTON" VALUE="<%=lb_close%>" ONCLICK="closeThis()" id="idOk">
</CENTER>
</BODY>
</HTML>
