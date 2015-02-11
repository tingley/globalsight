<%@ page
    contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="java.util.*,com.globalsight.everest.webapp.WebAppConstants,
            com.globalsight.everest.servlet.util.SessionManager,
            com.globalsight.everest.webapp.javabean.NavigationBean,
            com.globalsight.everest.webapp.pagehandler.PageHandler,
            com.globalsight.util.GlobalSightLocale,
            com.globalsight.util.edit.EditUtil,
            com.globalsight.util.resourcebundle.ResourceBundleConstants,
            java.util.ArrayList,
            java.util.Iterator,
            java.util.Locale,
            java.util.ResourceBundle,
            java.util.Iterator,
            com.globalsight.everest.corpus.CorpusDoc,            
            com.globalsight.everest.corpus.CorpusDocGroup,                        
            com.globalsight.everest.corpus.CorpusContext,
            com.globalsight.everest.webapp.pagehandler.tm.corpus.CorpusViewBean,
            com.globalsight.everest.webapp.pagehandler.tm.corpus.CorpusXsltHelper,            
            com.globalsight.everest.webapp.pagehandler.tm.corpus.ViewCorpusMainHandler"
    session="true"
%><%
ResourceBundle bundle = PageHandler.getBundle(session);
int num = Integer.parseInt((String) request.getParameter("number")); //info number
SessionManager sessionMgr =
  (SessionManager)session.getAttribute(WebAppConstants.SESSION_MANAGER);

ArrayList contextBeans =
  (ArrayList)sessionMgr.getAttribute(ViewCorpusMainHandler.CONTEXT_BEANS);
CorpusXsltHelper xsltHelper =
  (CorpusXsltHelper)sessionMgr.getAttribute(ViewCorpusMainHandler.XSLT_HELPER);

CorpusViewBean info = (CorpusViewBean)contextBeans.get(num);
String name = info.getSourceCorpusDoc().getCorpusDocGroup().getCorpusName();
%><html>
<HEAD>
<META HTTP-EQUIV="Cache-Control" CONTENT="no-cache">
<META HTTP-EQUIV="Expires" CONTENT="0">
<META HTTP-EQUIV="Content-Type" CONTENT="text/html; charset=UTF-8">
<TITLE><%=bundle.getString("lb_corpus_fullContext")%>: <%=name%></TITLE>
<frameset rows="8,92">
  <frameset cols="50%,50%">
    <frame name="srcNav" src="sLocaleHeader.jsp?number=<%=num%>"
     scrolling="no" frameborder="0" marginwidth="0" marginheight="0"
     noresize>
    <frame name="trgNav" src="tLocaleHeader.jsp?number=<%=num%>"
     scrolling="no" frameborder="0" marginwidth="0" marginheight="0"
     noresize>
  </frameset>
  <frameset COLS="50%,50%">
    <frame name="srcFrame" src="sXml.jsp?number=<%=num%>&view=text#focus"
     scrolling="auto" frameborder="1" marginwidth="8" marginheight="8"
     noresize>
    <frame name="trgFrame" src="tXml.jsp?number=<%=num%>&view=text#focus"
     scrolling="auto" frameborder="1" marginwidth="8" marginheight="8"
     noresize>
  </frameset>
</frameset>
</HEAD>
</html>

