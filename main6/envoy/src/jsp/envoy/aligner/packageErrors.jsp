<%@ page
    contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="java.util.*,com.globalsight.everest.webapp.webnavigation.LinkHelper,
        java.util.ResourceBundle,
        com.globalsight.util.edit.EditUtil,
        com.globalsight.everest.aligner.AlignmentStatus,
        com.globalsight.everest.servlet.util.SessionManager,
        com.globalsight.everest.webapp.pagehandler.PageHandler,
        com.globalsight.everest.webapp.WebAppConstants"
    session="true"
%>
<%
ResourceBundle bundle = PageHandler.getBundle(session);
SessionManager sessionMgr = (SessionManager)session.getAttribute(
  WebAppConstants.SESSION_MANAGER);

AlignmentStatus status = (AlignmentStatus)
  sessionMgr.getAttribute(WebAppConstants.GAP_PACKAGEINFO);

String lb_title = status.isError()
  ? bundle.getString("lb_aligner_pakcage_errors") : bundle.getString("lb_aligner_pakcage_warnings");

%>
<HTML>
<HEAD>
<TITLE><%=lb_title%></TITLE>
<SCRIPT SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<STYLE>
BODY {
  margin-top: 10px;
  margin-left: 10px;
  margin-right: 10px;
  margin-bottom: 10px;
}
</STYLE>
</HEAD>
<BODY CLASS="standardText">
<DIV CLASS="mainHeading" ID="idHeading"><%=lb_title%></DIV>
<BR>
<TABLE CELLPADDING=0 CELLSPACING=0 BORDER=0 CLASS=standardText>
  <TR>
    <TD>
    <% if (status.isError()) { %>
    <%=java.text.MessageFormat.format(bundle.getString("helper_text_aligner_package_download_error1"), status.getPackageName()) %>
    <% } else { %>
    <%=java.text.MessageFormat.format(bundle.getString("helper_text_aligner_package_download_error2"), status.getPackageName()) %>
    <% } %>

    <%=bundle.getString("helper_text_aligner_package_download_errorlist") %>:</TD>
  </TR>
  <TR><TD>&nbsp;</TD></TR>
  <TR>
    <TD>
      <UL>
        <%
        List errors = status.getErrorMessages();
        for (int i = 0, max = errors.size(); i < max; i++)
        {
        String error = (String)errors.get(i);

        out.print("<li>");
        out.print(error);
        out.println("</li>");
        }
        %>
      </UL>
    </TD>
  </TR>
</TABLE>
<BR>

<DIV id="idButtons">
<INPUT TYPE="BUTTON" VALUE="<%=bundle.getString("lb_ok")%>"
 onclick="window.close()">
</DIV>

</BODY>
</HTML>
