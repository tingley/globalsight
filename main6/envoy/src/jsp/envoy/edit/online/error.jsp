<%@ page
    contentType="text/html; charset=UTF-8"
    isErrorPage="true"
    import="java.util.*,
            com.globalsight.everest.servlet.ControlServlet,
            com.globalsight.everest.webapp.pagehandler.PageHandler,
                 com.globalsight.everest.webapp.javabean.ErrorBean,
         com.globalsight.util.resourcebundle.ResourceBundleConstants,
         com.globalsight.util.resourcebundle.SystemResourceBundle,
                 com.globalsight.everest.webapp.WebAppConstants,
         java.util.Locale, java.util.ResourceBundle"
    session="true"
%>
<jsp:useBean id="error" scope="request"
 class="com.globalsight.everest.webapp.javabean.ErrorBean" />
<%
    ControlServlet.handleJSPException(exception);

    ResourceBundle bundle = PageHandler.getBundle(session);
    String title = bundle.getString("lb_system_error");
    String noMessage = bundle.getString("msg_no_message");
    String noExceptionMessage = bundle.getString("msg_no_exception_message");
    String exceptionStacktrace  = bundle.getString("msg_exception_stacktrace");
%><html>
<head>
<title>GlobalSight: <%=title%></title>
</head>

<body bgcolor="#e5e5e5"
 leftmargin="0" topmargin="0" marginheight="0" marginwidth="0">
<PRE>
<%
Locale uiLocale = (Locale)session.getAttribute(WebAppConstants.UILOCALE);
String msg = null;

if (error != null)
{
    msg = error.getMessage();

    if (msg != null && msg.length() > 0)
    {
        out.println(msg);
    }
    else
    {
        out.println("<I>" + noMessage + "</I>");
    }
    out.println("<BR>");

    if (error.getException() != null)
    {
        msg = error.getException().getTopLevelMessage(uiLocale);

        if (msg != null && msg.length() > 0)
        {
            out.println(msg);
        }
        else
        {
            out.println("<I>" + noExceptionMessage + "</I>");
        }
        out.println("<BR>");

        out.println( exceptionStacktrace );
        out.println(error.getException().getStackTraceString());
    }
}

out.println("<BR>");
out.println(exception);

%>
</PRE>
</body>
</html>
