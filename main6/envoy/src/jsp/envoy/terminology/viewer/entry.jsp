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
        com.globalsight.everest.webapp.WebAppConstants"
    session="true"
%>
<%

ResourceBundle bundle = PageHandler.getBundle(session);

String result;
try
{
    ITermbase termbase = (ITermbase)session.getAttribute(
      WebAppConstants.TERMBASE);

    String conceptId = (String)request.getParameter(
      WebAppConstants.TERMBASE_CONCEPTID);
    String termId = (String)request.getParameter(
      WebAppConstants.TERMBASE_TERMID);
    String source = (String)request.getParameter(
        WebAppConstants.TERMBASE_SOURCE);
    String target = (String)request.getParameter(
        WebAppConstants.TERMBASE_TARGET);

    if (source != null && source.length() > 0)
    {
        source = EditUtil.unescape(source);
    }

    if (target != null && target.length() > 0)
    {
        target = EditUtil.unescape(target);
    }

    result = termbase.getEntry(Long.parseLong(conceptId),
        Long.parseLong(termId), source, target);

    if (result.length() == 0)
    {
        result = "<noresult></noresult>";
    }
    
    //result = "<?xml version=\"1.0\"?>" + result;
}
catch (TermbaseException ex)
{
    // Let client know about the exception
    result =  "<exception>" + EditUtil.encodeXmlEntities(ex.getMessage() +
        "@@@@@" + EditUtil.encodeXmlEntities(ex.getStackTraceString())) +
        "</exception>";
}
PrintWriter writer = response.getWriter();
writer.write(result);
%>
