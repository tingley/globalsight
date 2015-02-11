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
int num = Integer.parseInt((String) request.getParameter("number")); //info number
SessionManager sessionMgr = (SessionManager)session.getAttribute(WebAppConstants.SESSION_MANAGER);
ArrayList contextBeans = (ArrayList) sessionMgr.getAttribute(ViewCorpusMainHandler.CONTEXT_BEANS);
CorpusXsltHelper xsltHelper = (CorpusXsltHelper) sessionMgr.getAttribute(ViewCorpusMainHandler.XSLT_HELPER);
CorpusViewBean info = (CorpusViewBean) contextBeans.get(num);
String url = "/globalsight" + info.getSourceCorpusDoc().getGxmlPath();
CorpusContext sourceContext = info.getSourceContext();
String view=request.getParameter("view");
%><%=xsltHelper.fullHighlight(url, sourceContext.getTuId(),true,view)%>

