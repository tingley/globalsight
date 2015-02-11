<%@ page
    contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="java.util.*,com.globalsight.everest.webapp.WebAppConstants,
            com.globalsight.everest.servlet.util.SessionManager,
            com.globalsight.util.GlobalSightLocale,
            com.globalsight.util.resourcebundle.ResourceBundleConstants,
            java.util.ArrayList,
            java.util.Locale,
            java.util.ResourceBundle,
            com.globalsight.everest.corpus.CorpusDoc,
            com.globalsight.everest.webapp.pagehandler.PageHandler,
            com.globalsight.everest.webapp.pagehandler.tm.corpus.CorpusViewBean,
            com.globalsight.everest.webapp.pagehandler.tm.corpus.CorpusXsltHelper,            
            com.globalsight.everest.webapp.pagehandler.tm.corpus.ViewCorpusMainHandler"
    session="true"
%><%
ResourceBundle bundle = PageHandler.getBundle(session);
SessionManager sessionMgr = (SessionManager)session.getAttribute(WebAppConstants.SESSION_MANAGER);
ArrayList contextBeans = (ArrayList) sessionMgr.getAttribute(ViewCorpusMainHandler.CONTEXT_BEANS);
CorpusViewBean info = (CorpusViewBean) contextBeans.get(0);
String sourceLocale = info.getSourceCorpusDoc().getLocale().getDisplayName();
String num=request.getParameter("number");
%><HTML>
<HEAD>
<SCRIPT LANGUAGE="JAVASCRIPT">
function changeViewMode(viewMode)
{
   var url = 'sXml.jsp?number=<%=num%>&view=' + viewMode + '#focus';
   parent.srcFrame.location = url;
}
</SCRIPT>
</HEAD>
<BODY BGCOLOR="beige">
<CENTER>
<%=sourceLocale%><BR>
<INPUT TYPE="radio" NAME="view" VALUE="text" CHECKED  ONCLICK="changeViewMode('text')">
<%=bundle.getString("lb_corpus_view_text")%></INPUT>
<INPUT TYPE="radio" NAME="view" VALUE="list" ONCLICK="changeViewMode('list')">
<%=bundle.getString("lb_corpus_view_list")%></INPUT>
</CENTER>
</BODY>
</HTML>

