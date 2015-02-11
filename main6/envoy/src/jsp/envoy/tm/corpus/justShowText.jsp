<%@ page
    contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="java.util.*,com.globalsight.everest.webapp.pagehandler.tm.corpus.CorpusXsltHelper"            
    session="true"
%><%
String url = request.getParameter("gxmlUrl");
String docName = request.getParameter("docName");
CorpusXsltHelper xsltHelper = new CorpusXsltHelper(request);
String view="text";
Long tuvIdToHighlight = new Long(-1);
%><TITLE><%=docName%></TITLE>
<%=xsltHelper.fullHighlight(url, tuvIdToHighlight, true, view)%>

