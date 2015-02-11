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
            com.globalsight.everest.corpus.CorpusTm,
            com.globalsight.everest.webapp.pagehandler.tm.corpus.CorpusViewBean,
            com.globalsight.everest.webapp.pagehandler.tm.corpus.CorpusXsltHelper,
            com.globalsight.util.AmbFileStoragePathUtils,
            com.globalsight.everest.webapp.pagehandler.tm.corpus.ViewCorpusMainHandler"
    session="true"
%><%
ResourceBundle bundle = PageHandler.getBundle(session);
String clearCache = request.getParameter("clearCache");
SessionManager sessionMgr = (SessionManager)session.getAttribute(WebAppConstants.SESSION_MANAGER);
ArrayList contextBeans = (ArrayList) sessionMgr.getAttribute(ViewCorpusMainHandler.CONTEXT_BEANS);

if (clearCache != null)
    sessionMgr.removeElement(ViewCorpusMainHandler.CONTEXT_BEANS);

CorpusXsltHelper xsltHelper = (CorpusXsltHelper) sessionMgr.getAttribute(ViewCorpusMainHandler.XSLT_HELPER);
long numMatches = (contextBeans == null) ? 0 : contextBeans.size();
CorpusViewBean info = null;
String srcLocale = null;
String targetLocale = null;
Long localeDbId = null;
Long tuvId = null;

if (numMatches > 0)
{
    info = (CorpusViewBean) contextBeans.get(0);
    srcLocale = info.getSourceCorpusDoc().getLocale().getDisplayName();
    targetLocale = info.getTargetCorpusDoc().getLocale().getDisplayName();
    localeDbId = info.getSourceCorpusDoc().getLocale().getIdAsLong();
    tuvId = info.getTargetContext().getTuvId();
}

String showDeleteParam = request.getParameter("showDelete");
boolean showDelete = false;
if ("true".equals(showDeleteParam))
    showDelete=true;

%>
<HTML>
<!-- This is \envoy\tm\corpus\viewCorpusMatches.jsp -->
<HEAD>
<META HTTP-EQUIV="Content-Type" CONTENT="text/html; charset=UTF-8">
<TITLE>GlobalSight: Corpus Match Results</TITLE>
<STYLE>
A { color: blue; }
</STYLE>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/utilityScripts.js"></SCRIPT>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/library.js"></SCRIPT>
<SCRIPT LANGUAGE="JavaScript">
var wins = new Array();
var numwins = 0;

function deleteCorpusDoc(cuvId)
{
   var url = "/globalsight/envoy/tm/corpus/deleteCorpus.jsp?cuvId=" + cuvId;
   var name = "delete" + numwins;
   var agree = confirm('<%=bundle.getString("jsmsg_corpus_deldoc")%>');
   if (agree) {
      window.showModalDialog(url,null,"center:yes; help:no; resizable:no; status:no; dialogWidth: 300px; dialogHeight: 180px; ");
      var refreshUrl = "/globalsight/ControlServlet?activityName=viewCorpusMatches&tuvId=" + <%=tuvId%> + "&localeDbId=" + <%=localeDbId%> + "&showDelete=true&clearCache=true";
      window.location.href = refreshUrl;
      return false;
   }
   else
   {
      return true;
   }
}

function viewSideBySideContext(num)
{
   var url = "/globalsight/envoy/tm/corpus/viewSideBySideContext.jsp?number=" + num;
   var name = "context" + numwins;
   wins[numwins++] = window.open(url,name,'location=no,menubar=no,resizable=yes,scrollbars=yes,WIDTH=800,HEIGHT=600');
}

function showSrcDoc(url)
{
   var name = "sDoc" + numwins;

   //Modify for save ppt file in IE,refer GBS-1128.
   wins[numwins++] = window.location = url;
   //wins[numwins++] = window.open(url, name, 'location=no,menubar=no,resizable=yes,scrollbars=yes,WIDTH=800,HEIGHT=600');
}

function showTargetDoc(url)
{
   var name = "tDoc" + numwins;

   //Modify for save ppt file in IE,refer GBS-1128.
   wins[numwins++] = window.location = url;
   //wins[numwins++] = window.open(url, name, 'location=no,menubar=no,resizable=yes,scrollbars=yes,WIDTH=800,HEIGHT=600');
}


function doOnUnload()
{
   for (var i=0; i < numwins; i++) {
      try { wins[i].close(); } catch (ignore) {}
   }
}

</SCRIPT>
</HEAD>
<BODY LEFTMARGIN="10" RIGHTMARGIN="10" TOPMARGIN="10" MARGINWIDTH="0" MARGINHEIGHT="0" CLASS="standardText" onunload="doOnUnload()">
<CENTER>
<H3><%=bundle.getString("lb_corpus_matches")%></H3>
</CENTER>
<% if (numMatches >0) { %>
<EM><%=bundle.getString("lb_source_locale")%>: <%=srcLocale%>
<BR>
<%=bundle.getString("lb_target_locale")%>: <%=targetLocale%>
<BR>
<B><%=bundle.getString("lb_corpus_numMatches")%> <%=numMatches%></B>
<BR>
<HR width="100%">

<%
Iterator iter = contextBeans.iterator();
int num = 0;
while (iter.hasNext())
{
    info = (CorpusViewBean) iter.next();
    CorpusDoc srcDoc = info.getSourceCorpusDoc();
    CorpusDoc targetDoc = info.getTargetCorpusDoc();
    CorpusContext srcContext = info.getSourceContext();
    CorpusContext targetContext = info.getTargetContext();
    long targetDocId = targetDoc.getId();

    int sIdx = -1;
    int tIdx = -1;
    String sTitle ="";
    String tTitle="";

    if (CorpusTm.isStoringNativeFormatDocs())
    {
        sIdx = srcDoc.getNativeFormatPath().lastIndexOf("/");
        tIdx = targetDoc.getNativeFormatPath().lastIndexOf("/");
        sTitle = srcDoc.getNativeFormatPath().substring(sIdx);
        tTitle = targetDoc.getNativeFormatPath().substring(tIdx);
    }
%>
<B><font SIZE="+1"><%= srcDoc.getCorpusDocGroup().getCorpusName()%></b></FONT>
<% if (showDelete) { %>
&nbsp;&nbsp;&nbsp;&nbsp;
<A HREF="#" ONCLICK="deleteCorpusDoc(<%=targetDocId%>)">
(<%=bundle.getString("lb_remove")%>)</A>
<% } %>
<BR>

<EM><FONT SIZE="-1">
<A HREF="#" ONCLICK="viewSideBySideContext(<%=num%>)">
[<%=bundle.getString("lb_corpus_fullContext")%>]</A>
</FONT></EM>

<% if (CorpusTm.isStoringNativeFormatDocs()) {%>
<B>&#x2015;</B>
<EM><FONT SIZE="-1">
<%
    String srcNativeFormatPath = srcDoc.getNativeFormatPath();
    String fileStoragePath = AmbFileStoragePathUtils.getFileStorageDirPath();
    String srcFullPath = fileStoragePath + srcNativeFormatPath;
    java.io.File srcFile = new java.io.File(srcFullPath);
    if (srcFile.exists()) 
    {
%>
    <A HREF="#" ONCLICK="showSrcDoc('/globalsight<%=srcDoc.getNativeFormatPath()%>')" TITLE="<%=sTitle%>">[<%=bundle.getString("lb_corpus_srcDoc")%>]</A>
<%  } else {  %>
    [<%=bundle.getString("lb_corpus_srcDoc")%>]
<%  } %>
</FONT></EM>
<B>&#x2015;</B>
<EM><FONT SIZE="-1">
<%
    String trgMativeFormatPath = targetDoc.getNativeFormatPath();
    String trgFullPath = fileStoragePath + trgMativeFormatPath;
    java.io.File trgFile = new java.io.File(trgFullPath);
    if (trgFile.exists()) 
    {
%>
    <A HREF="#" ONCLICK="showTargetDoc('/globalsight<%=trgMativeFormatPath%>')" TITLE="<%=tTitle%>">[<%=bundle.getString("lb_corpus_targetDoc")%>]</A>
<%  } else {  %>
    [<%=bundle.getString("lb_corpus_targetDoc")%>]
<%  } %>

</FONT></EM>
<% } %>

<TABLE CELLPADDING="0" CELLSPACING="0" BORDER="0"
  style="border-collapse: collapse; margin-top: 2px;">
  <TR VALIGN=TOP BGCOLOR="#FFFFFF">
    <TD style="border-right: 2px solid black;">
      <TABLE CLASS="standardText">
	<TR VALIGN=TOP>
	  <TD WIDTH=300 STYLE="word-wrap: break-word;">
	    <%=xsltHelper.partialHighlight(srcContext.getPartialContext(),
	    srcContext.getTuId(), true)%>
	  </TD>
	</TR>
      </TABLE>
    </TD>
    <TD>
      <TABLE CLASS="standardText" BGCOLOR="#FFFFFF">
	<TR VALIGN=TOP>
	  <TD WIDTH=300 STYLE="word-wrap: break-word;">
	    <%=xsltHelper.partialHighlight(targetContext.getPartialContext(),
	    targetContext.getTuId(), false)%>
	  </TD>
	</TR>
      </TABLE>
    </TD>
  </TR>
</TABLE>
<BR>
<%
num++;
}
}
else
{
%>
<I><%=bundle.getString("lb_corpus_noMatches")%></I>
<% } %>
<BR>
<CENTER>
  <INPUT TYPE="BUTTON" onclick="window.close()"
  VALUE="<%=bundle.getString("lb_close")%>"></INPUT>
</CENTER>
</BODY>
</HTML>
<%
//clean out the arraylist
if (clearCache != null)
{
    contextBeans.clear();
    contextBeans = null;
}
%>

