<%@ page
    contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="java.util.*,
            org.apache.log4j.Logger,
            com.globalsight.everest.webapp.WebAppConstants,
            com.globalsight.everest.servlet.util.SessionManager,
            com.globalsight.everest.webapp.pagehandler.PageHandler,
            com.globalsight.everest.servlet.EnvoyServletException,
            com.globalsight.util.resourcebundle.ResourceBundleConstants,
            java.util.Locale,
            java.util.ResourceBundle,
            com.globalsight.everest.corpus.CorpusDoc,            
            com.globalsight.everest.corpus.CorpusDocGroup,                        
            com.globalsight.everest.corpus.CorpusManagerLocal,                                    
            com.globalsight.everest.servlet.util.ServerProxy"
    session="true"
%><%
ResourceBundle bundle = PageHandler.getBundle(session);
Locale uiLocale = (Locale)session.getAttribute(WebAppConstants.UILOCALE);
SessionManager sessionMgr = (SessionManager)session.getAttribute(
  WebAppConstants.SESSION_MANAGER);
Logger logger = Logger.getLogger(CorpusManagerLocal.class.getName());
if (sessionMgr == null)
    throw new EnvoyServletException(new Exception("not logged in."));

String cuvIdParam = request.getParameter("cuvId");
Long cuvId = Long.valueOf(cuvIdParam);

//first look up the corpus doc
boolean foundDoc = true;
boolean deleted = true;
CorpusDoc doc = null;
String docName = "";
String docLocale = "";

try {
    doc = ServerProxy.getCorpusManager().getCorpusDoc(cuvId);
}
catch (Exception e)
{
    foundDoc = false;
}

if (foundDoc)
{
    try {
        docName = doc.getCorpusDocGroup().getCorpusName();
        docLocale = doc.getLocale().toString();
        doc = null;
        ServerProxy.getCorpusManager().removeCorpusDoc(cuvId);
    }
    catch (Exception e)
    {
        deleted = false;
        logger.error("Failed to delete corpus doc " + cuvId, e);
    }
}
%><HTML>
<HEAD>
<META HTTP-EQUIV="Content-Type" CONTENT="text/html; charset=UTF-8">
<META HTTP-EQUIV="Cache-Control" CONTENT="no-cache">
<META HTTP-EQUIV="Expires" CONTENT="0">
<TITLE><%=bundle.getString("lb_corpus_del_title")%></TITLE>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/utilityScripts.js"></SCRIPT>
</HEAD>
<BODY LEFTMARGIN="10" RIGHTMARGIN="10" TOPMARGIN="10" MARGINWIDTH="0" MARGINHEIGHT="0" CLASS="standardText">
<BR><BR>
<CENTER>
<B>
<% if (foundDoc) {%>
<% if (deleted) {%>
<%=bundle.getString("lb_corpus_del_ok")%> <%=docName%> (<%=docLocale%>)
<% } else { %>
<%=bundle.getString("lb_corpus_del_bad")%> <%=docName%> (<%=docLocale%>)
<% } %>
<% } else {%>
<%=bundle.getString("lb_corpus_del_nf")%>
<% } %>
</B>
<BR><BR>
<INPUT TYPE="BUTTON" VALUE="<%=bundle.getString("lb_ok")%>" ONCLICK="javascript:window.close()"></INPUT>
</CENTER>
</HTML>

