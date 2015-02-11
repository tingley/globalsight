<%@ page
    contentType="text/html; charset=UTF-8"
    errorPage="error.jsp"
    import="java.util.*,com.globalsight.util.resourcebundle.ResourceBundleConstants,
            com.globalsight.util.resourcebundle.SystemResourceBundle,
            com.globalsight.everest.servlet.util.SessionManager,
            com.globalsight.everest.webapp.WebAppConstants,
            com.globalsight.everest.webapp.javabean.NavigationBean,
            com.globalsight.everest.webapp.pagehandler.PageHandler,
            com.globalsight.everest.webapp.pagehandler.edit.online.EditorState,
            com.globalsight.everest.webapp.pagehandler.edit.online.EditorConstants,
            java.util.Locale,
            java.util.ResourceBundle"
    session="true"
%>
<jsp:useBean id="skin" scope="application"
 class="com.globalsight.everest.webapp.javabean.SkinBean"/>
<%
ResourceBundle bundle = PageHandler.getBundle(session);

SessionManager sessionMgr = (SessionManager)session.getAttribute(
  WebAppConstants.SESSION_MANAGER);
EditorState state =
  (EditorState)sessionMgr.getAttribute(WebAppConstants.EDITORSTATE);

String lb_previous_segment = bundle.getString("lb_previous_segment");
String lb_next_segment = bundle.getString("lb_next_segment");
String lb_save = bundle.getString("lb_save");
String img_previous = "/globalsight/images/editorPreviousSegment.gif";
String img_next = "/globalsight/images/editorNextSegment.gif";

img_previous = "<IMG SRC=\"" + img_previous + "\" BORDER=\"0\">";
img_next = "<IMG SRC=\"" + img_next + "\" BORDER=\"0\">";
%>
<HTML>
<HEAD>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
</HEAD>
<BODY BGCOLOR="<%=skin.getProperty("skin.editor.bgColor")%>">
<DIV ID="sourceBoxTitle"
     STYLE="POSITION: ABSOLUTE; Z-INDEX: 10; TOP: 0px; LEFT: 5px; RIGHT: 5px; width=100%">
<TABLE WIDTH="100%" CELLPADDING="0" CELLSPACING="0" BORDER="0">
<TR CLASS="tableHeadingBasic">
  <TD VALIGN="TOP"><BR>&nbsp;<A CLASS="sortHREFWhite"
    HREF="#" onclick="parent.doRefresh(-1,false); return false;"
    onfocus="this.blur();"><%=img_previous%></A>&nbsp;</TD>
  <TD VALIGN="TOP" WIDTH="33%"><BR><A CLASS="sortHREFWhite"
    HREF="#" onclick="parent.doRefresh(-1,false); return false;"
    onfocus="this.blur();"><%=lb_previous_segment%></A></TD>
  <TD VALIGN="TOP" WIDTH="33%" ALIGN="CENTER"><BR><A CLASS="sortHREFWhite"
    HREF="#" onclick="parent.doRefresh(0,true); return false;"
    onfocus="this.blur();"><%=lb_save%></A></TD>
  <TD VALIGN="TOP" WIDTH="33%" ALIGN="RIGHT"><BR><A CLASS="sortHREFWhite"
    HREF="#" onclick="parent.doRefresh(1,false); return false;"
    onfocus="this.blur();"><%=lb_next_segment%></A></TD>
  <TD VALIGN="TOP"><BR>&nbsp;<A CLASS="sortHREFWhite"
    HREF="#" onclick="parent.doRefresh(1,false); return false;"
    onfocus="this.blur();"><%=img_next%></A>&nbsp;&nbsp;</TD>
</TR>
</TABLE>
</DIV>
</BODY>
</HTML>
