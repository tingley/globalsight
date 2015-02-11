<%@ page
    contentType="text/xml; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="java.util.*,com.globalsight.everest.webapp.webnavigation.LinkHelper,
        java.util.ResourceBundle,
        java.io.PrintWriter,
        com.globalsight.util.edit.EditUtil,
        com.globalsight.everest.webapp.pagehandler.PageHandler,
        com.globalsight.terminology.ITermbase,
        com.globalsight.terminology.IUserdataManager,
        com.globalsight.terminology.TermbaseException,
        com.globalsight.terminology.TermbaseExceptionMessages,
        com.globalsight.everest.webapp.WebAppConstants
        "
    session="true"
%>
<%

ResourceBundle bundle = PageHandler.getBundle(session);

String result;
try
{
    ITermbase termbase = (ITermbase)session.getAttribute(
      WebAppConstants.TERMBASE);

    // If there is a default input model, load it.
    IUserdataManager userdata = termbase.getUserdataManager();
    result = userdata.getDefaultObject(IUserdataManager.TYPE_INPUTMODEL);

    if (result.length() == 0)
    {
        result = "<noresult></noresult>"; // "<noresult></noresult>";
    }
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
