<%@ page isErrorPage="true" 
    contentType="text/html; charset=UTF-8"
    import="java.util.*,
            com.globalsight.everest.servlet.ControlServlet,
            com.globalsight.everest.webapp.javabean.ErrorBean,
            com.globalsight.util.resourcebundle.ResourceBundleConstants,
            com.globalsight.util.resourcebundle.SystemResourceBundle,
            java.util.Locale,
            java.util.ResourceBundle"
    session="false"
%>
<jsp:useBean id="error" scope="request"
 class="com.globalsight.everest.webapp.javabean.ErrorBean" />
<%
    ControlServlet.handleJSPException(exception);

    ResourceBundle bundle = 
      SystemResourceBundle.getInstance().getResourceBundle(
        ResourceBundleConstants.LOCALE_RESOURCE_NAME, Locale.US);

    String title = bundle.getString("lb_system_error");
%>
<HTML>
<HEAD>
<!-- error_nav.jsp -->
<META HTTP-EQUIV="Pragma" CONTENT="no-cache">
<TITLE>GlobalSight: <%=title %></TITLE>
</HEAD>
<BODY LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0" MARGINWIDTH="0" MARGINHEIGHT="0">
<DIV ID="contentLayer" STYLE=" POSITION: ABSOLUTE; Z-INDEX: 9; TOP: 108px; LEFT: 20px; RIGHT: 20px;">

<SPAN CLASS="mainHeading">
<%=title%>
</SPAN>
<P>

<TABLE WIDTH="643" CELLPADDING="0" CELLSPACING="0" BORDER="0">
<TR>
    <TD VALIGN="TOP" ALIGN="LEFT" WIDTH="643">

<P><B>System 4 has encountered an unexpected exception in the navigation JSPs</B></P>
<H2> Click <A HREF="/globalsight/wl"> here </A> to return to System4. </H2>
<PRE>
<%
String msg = error.getMessage(); 
out.println("<B>System 4 Message:</B><BR>");
if (msg != null)
{
  out.println(msg);
}
else
{
  out.println("<I>missing...</I>");
}
out.println("<BR>");

if (error.getException() != null)
{
  msg = error.getException().getMessage();

  out.println("<B>Exception Message:</B><BR>");
  if (msg != null)
  {
    out.println(msg);
  }
  else
  {
    out.println("<I>missing...</I>");
  }
  out.println("<BR>");

  msg = error.getException().getOriginalStackTrace();

  out.println("<B>Exception Stacktrace:</B><BR>");
  if (msg != null)
  {
    out.println(msg);
  }
  else
  {
    out.println("<I>missing...</I>");
  }
  out.println("<BR>");
}
%>
</PRE>      
</TD>
</TR>
</TABLE>
</DIV>
</BODY>
</HTML>

