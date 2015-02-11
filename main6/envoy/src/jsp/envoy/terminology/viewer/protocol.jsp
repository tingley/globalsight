<%@ page
    contentType="text/xml; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="java.util.*,com.globalsight.everest.webapp.webnavigation.LinkHelper,
        javax.servlet.ServletInputStream,
        java.util.*,
        java.io.*,
        java.rmi.RemoteException,
        org.dom4j.Document,
        org.dom4j.Element,
        org.dom4j.Node,
        com.globalsight.util.XmlParser,
        com.globalsight.util.GeneralException,
        com.globalsight.util.edit.EditUtil,
        com.globalsight.everest.webapp.pagehandler.PageHandler,
        com.globalsight.terminology.ITermbase,
        com.globalsight.terminology.TermbaseException,
        com.globalsight.terminology.TermbaseExceptionMessages,
        com.globalsight.terminology.indexer.IIndexManager,
        com.globalsight.everest.webapp.WebAppConstants
        "
    session="true"
%>
<%!
public String readInputStream(javax.servlet.ServletInputStream p_stream)
  throws IOException
{
    StringBuffer result = new StringBuffer();

    BufferedReader reader = new BufferedReader(
       new InputStreamReader(p_stream, "UTF-8"));

    String line;
    while ((line = reader.readLine()) != null)
    {
        result.append(line).append('\n');
    }

    // System.err.println("XML = `" + result.toString() + "'");

    return result.toString();
}

public Document parseXml(String p_xml)
    throws Exception
{
    XmlParser parser = null;

    try
    {
        parser = XmlParser.hire();
        return parser.parseXml(p_xml);
    }
    finally
    {
        XmlParser.fire(parser);
    }
}

// <statistics></statistics>
public String statistics(ITermbase p_termbase, Element p_root)
    throws TermbaseException, RemoteException
{
    return p_termbase.getStatistics();
}

// <lock steal="true">1234</lock>
public String lockEntry(ITermbase p_termbase, Element p_root)
    throws TermbaseException, RemoteException
{
    boolean stealLock = p_root.attributeValue("steal").equals("true");
    long conceptId = Long.parseLong(p_root.getText().trim());

    String result = p_termbase.lockEntry(conceptId, stealLock);

    if (result.length() == 0)
    {
        result = p_termbase.getLockInfo(conceptId);
    }

    return result;
}

// <unlock conceptid="1234"><lock>.....</lock></unlock>
public String unlockEntry(ITermbase p_termbase, Element p_root)
    throws TermbaseException, RemoteException
{

    String temp = p_root.attributeValue("conceptid");
    long conceptId = Long.parseLong(temp.trim());
    String lock = p_root.element("lock").asXML();

    p_termbase.unlockEntry(conceptId, lock);

    return "<ok>Unlocking entry succeeded.</ok>";
}

// <create><conceptGrp>.</conceptGrp></create>
public String createEntry(ITermbase p_termbase, Element p_root)
    throws TermbaseException, RemoteException
{
    String entry = p_root.element("conceptGrp").asXML();

    long conceptId = p_termbase.addEntry(entry);

    return "<ok>" + conceptId + "</ok>";
}

// <update conceptid="1234"><lock>.</lock><conceptGrp>.</conceptGrp></update>
public String updateEntry(ITermbase p_termbase, Element p_root)
    throws TermbaseException, RemoteException
{
    String temp = p_root.attributeValue("conceptid");
    long conceptId = Long.parseLong(temp.trim());
    String lock = p_root.element("lock").asXML();
    String entry = p_root.element("conceptGrp").asXML();

    p_termbase.updateEntry(conceptId, entry, lock);

    // System.err.println("Updated entry " + conceptId + " with " + entry);

    return "<ok>Update succeeded.</ok>";
}

// <validate conceptid="1234"><conceptGrp>.</conceptGrp></validate>
public String validateEntry(ITermbase p_termbase, Element p_root)
    throws TermbaseException, RemoteException
{
    String temp = p_root.attributeValue("conceptid");
    long conceptId = Long.parseLong(temp.trim());
    String entry = p_root.element("conceptGrp").asXML();

    return p_termbase.validateEntry(/*conceptId,*/ entry);
}

// <delete conceptid="1234"><lock>.</lock></delete>
public String deleteEntry(ITermbase p_termbase, Element p_root)
    throws TermbaseException, RemoteException
{
    String temp = p_root.attributeValue("conceptid");
    long conceptId = Long.parseLong(temp.trim());

    /*
    String lock = "";
    Element e = p_root.element("lock");
    if (e != null)
    {
        lock = e.asXML();
    }
    */
    p_termbase.deleteEntry(conceptId);

    return "<ok>Entry deleted.</ok>";
}

public void reindex(ITermbase p_termbase) {
    try {
        IIndexManager indexer = p_termbase.getIndexer();
        indexer.doIndex();
    }
    catch(Exception e) {
    }
}
%>
<%
ResourceBundle bundle = PageHandler.getBundle(session);

String result = "<error>Protocol error: unknown command.</error>";

try
{
    ITermbase termbase = (ITermbase)session.getAttribute(
        WebAppConstants.TERMBASE);

    String xml = readInputStream(request.getInputStream());

    Document dom = parseXml(xml);

    Element root = dom.getRootElement();
    String command = root.getName();

    if (command.equalsIgnoreCase("lock"))
    {
        result = lockEntry(termbase, root);
    }
    else if (command.equalsIgnoreCase("unlock"))
    {
        result = unlockEntry(termbase, root);
    }
    else if (command.equalsIgnoreCase("create"))
    {
        result = createEntry(termbase, root);
    }
    else if (command.equalsIgnoreCase("update"))
    {
        result = updateEntry(termbase, root);
    }
    else if (command.equalsIgnoreCase("delete"))
    {
        result = deleteEntry(termbase, root);
    }
    else if (command.equalsIgnoreCase("validate"))
    {
        result = validateEntry(termbase, root);
    }
    else if (command.equalsIgnoreCase("statistics"))
    {
        result = statistics(termbase, root);
    }
    else if (command.equalsIgnoreCase("reindex"))
    {
        reindex(termbase);
    }
}
catch (TermbaseException ex)
{
    ex.printStackTrace();

    // Let client know about the exception
    result = "<exception>" + EditUtil.encodeXmlEntities(ex.getMessage() +
        "@@@@@" + EditUtil.encodeXmlEntities(ex.getStackTraceString())) +
        "</exception>";
}
catch (Throwable ex)
{
    ex.printStackTrace();

    result = "<exception>unexpected exception" +
        "@@@@@" + EditUtil.encodeXmlEntities(
            GeneralException.getStackTraceString(ex)) +
        "</exception>";
}
PrintWriter writer = response.getWriter();
writer.write(result);
%>
