<%@ page
    contentType="text/xml; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="java.util.*,com.globalsight.everest.webapp.webnavigation.LinkHelper,
        java.util.ResourceBundle,
        java.io.PrintWriter,
        com.globalsight.util.edit.EditUtil,
        com.globalsight.everest.webapp.pagehandler.PageHandler,
        com.globalsight.terminology.ITermbase,
        com.globalsight.terminology.TermbaseException,
        com.globalsight.terminology.TermbaseExceptionMessages,
        com.globalsight.everest.webapp.WebAppConstants
        "
    session="true"
%>
<%

ResourceBundle bundle = PageHandler.getBundle(session);

String hitlist;
try
{
    ITermbase termbase = (ITermbase)session.getAttribute(
        WebAppConstants.TERMBASE);

    String index = (String)request.getParameter(
        WebAppConstants.TERMBASE_SOURCE);
    String targetLan = (String)request.getParameter("target");
    String start = (String)request.getParameter(
        WebAppConstants.TERMBASE_QUERY);
    int direction = Integer.parseInt(
        (String)request.getParameter("DIRECTION"));

    // Caller escaped the string twice (Unicode --> %uXXXX --> %xx).
    // Weblogic unescaped once, and here we unescape the %u's.
    if (start.length() > 0)
    {
        start = EditUtil.unescape(start);
        start = start.trim();
    }

    if (index.length() > 0)
    {
        index = EditUtil.unescape(index);
    }

    hitlist = termbase.browse(index, targetLan, start, direction, 20);
}
catch (TermbaseException ex)
{
    // Let client know about the exception
    hitlist = "<exception>" + EditUtil.encodeXmlEntities(ex.getMessage() +
        "@@@@@" + EditUtil.encodeXmlEntities(ex.getStackTraceString())) +
        "</exception>";
}
PrintWriter writer = response.getWriter();
writer.write(hitlist);
%>
