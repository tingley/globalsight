<%@ page
    contentType="text/xml; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="java.util.*,com.globalsight.everest.webapp.webnavigation.LinkHelper,
        javax.servlet.ServletInputStream,
        java.util.ResourceBundle,
        java.io.PrintWriter,
        java.io.IOException,
        com.globalsight.util.GeneralException,
        com.globalsight.util.edit.EditUtil,
        com.globalsight.everest.webapp.pagehandler.PageHandler,
        com.globalsight.terminology.ITermbase,
        com.globalsight.terminology.TermbaseException,
        com.globalsight.terminology.TermbaseExceptionMessages,
        com.globalsight.everest.webapp.WebAppConstants
        "
    session="true"
%>
<%!
public String readInputStream(javax.servlet.ServletInputStream p_stream)
  throws IOException
{
    byte[] b = new byte[8192];
    StringBuffer sb = new StringBuffer();
    int total = 0, cnt = 0;
    while ((cnt = p_stream.readLine(b, total, 8192)) >= 0)
    {
      String ss = new String(b, total, cnt, "UTF8");
      sb.append(ss);
      total += cnt;
    }

    System.err.println("XML = `" + sb.toString() + "'");

    return sb.toString();
}
%>
<%
ResourceBundle bundle = PageHandler.getBundle(session);

String result;

try
{
    ITermbase termbase = (ITermbase)session.getAttribute(
        WebAppConstants.TERMBASE);

    String xml = readInputStream(request.getInputStream());

    termbase.updateEntry(204, xml, "");

    result = "<ok></ok>";
}
catch (TermbaseException ex)
{
    // Let client know about the exception
    result = "<exception>" + EditUtil.encodeXmlEntities(ex.getMessage() +
        "@@@@@" + EditUtil.encodeXmlEntities(ex.getStackTraceString())) +
        "</exception>";
}
catch (Throwable ex)
{
    result = "<exception>unexpected exception" +
        "@@@@@" + EditUtil.encodeXmlEntities(
            GeneralException.getStackTraceString(ex)) +
        "</exception>";
}
PrintWriter writer = response.getWriter();
writer.write(result);
%>
