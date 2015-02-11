<%@ page
    contentType="text/xml; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="java.util.*,com.globalsight.everest.webapp.webnavigation.LinkHelper,
        com.globalsight.everest.servlet.util.SessionManager,
        java.util.ResourceBundle,
        java.io.PrintWriter,
        com.globalsight.everest.webapp.pagehandler.PageHandler,
        com.globalsight.terminology.ITermbase,
        com.globalsight.terminology.TermbaseException,
        com.globalsight.everest.webapp.WebAppConstants"
    session="true"
%>
<%

ResourceBundle bundle = PageHandler.getBundle(session);

String xml;
try
{
    ITermbase termbase = (ITermbase)session.getAttribute(
      WebAppConstants.TERMBASE);

    if (termbase == null)
    {
      SessionManager sessionMgr = (SessionManager)session.getAttribute(
        WebAppConstants.SESSION_MANAGER);
      termbase = (ITermbase)sessionMgr.getAttribute(WebAppConstants.TERMBASE);
    }

    xml = termbase.getDefinition();
}
catch (TermbaseException ex)
{
    // TODO: error handling
    xml = "";
}

PrintWriter writer = response.getWriter();
writer.write(xml);
%>
