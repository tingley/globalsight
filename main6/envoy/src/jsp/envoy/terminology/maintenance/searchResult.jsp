<%@ page
    contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="java.util.*,com.globalsight.everest.webapp.webnavigation.LinkHelper,
        java.util.ResourceBundle,
        com.globalsight.terminology.searchreplace.SearchResult,
        com.globalsight.everest.servlet.util.SessionManager,
        com.globalsight.everest.webapp.pagehandler.PageHandler,
        com.globalsight.everest.webapp.WebAppConstants"
    session="true"
%>
<jsp:useBean id="replace" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<%
ResourceBundle bundle = PageHandler.getBundle(session);
SessionManager sessionMgr = (SessionManager)session.getAttribute(
  WebAppConstants.SESSION_MANAGER);
String urlReplace = replace.getPageURL();
ArrayList list = (ArrayList) sessionMgr.getAttribute("searchResults");
%>
<HTML>
<head>
<link type="text/css" rel="StyleSheet" href="/globalsight/includes/stylesIE.jsp" />
<SCRIPT src="/globalsight/envoy/terminology/viewer/viewerAPI.js"></SCRIPT>
<script>
    var size = <%=list.size()%>;
    
    if(size == 0) {
        parent.document.getElementById("replaceButton").disabled = true;
    }
    
    function doReplace(rContent) {
        if(!ischecked()) {
            alert('<%=bundle.getString("msg_maintance_no_replace")%>');
            return false;
        }
        else {
            var url = '<%=urlReplace%>&action=replace&size=<%=list.size()%>';
            document.getElementById("replaceContent").value = rContent;
            ReplaceForm.action = url;   
            ReplaceForm.submit();
        }
    }
    
    function checkAll() {
        for(var i = 0; i < size; i++)
        {
            var name = "checkbox" + i;
            var element = document.getElementById(name);
            element.checked = document.getElementById("checkboxall").checked;
        }
    }
    
    function ischecked() {
        for(var i = 0; i < size; i++)
        {
            var name = "checkbox" + i;
            var element = document.getElementById(name);
            if(element.checked) {
                return true;
            }
        }
        
        return false;
    }
</script>
</head>
<Body>
<FORM id="ReplaceForm" name="form" method="post" action="">
<input type="hidden" id="replaceContent" name="replaceContent">
<TABLE id="idTable" CELLPADDING=2 CELLSPACING=0 BORDER=1
  CLASS="standardText" style="border-collapse: collapse">

    <TR class="tableHeadingBasic" >
      <td><input type="checkbox" id="checkboxall" onclick="checkAll()" width="30"></td>
      <TD align="center"  width="60">Entry ID</TD>
      <TD align="center"  width="280">Field Content</TD>
      <TD align="center" width="100">Type</TD>
    </TR>
    <%
    String tbid = (String)sessionMgr.getAttribute(WebAppConstants.TERMBASE_TB_ID);
    for(int i= 0; i < list.size(); i++) {
        SearchResult result = (SearchResult) list.get(i);
        
    %>
    <TR class="tableRowOdd" >
        <td><input type="checkbox" id="checkbox<%=i%>" name="checkbox<%=i%>" value="<%=result.getLevelId()%>,<%=result.getField()%>"></td>
      <TD align="left"><a href="#" onclick="ShowTermbaseConceptTerm('<%=tbid%>','<%=result.getConceptId()%>', '<%=result.getLevelId()%>');"><%=result.getConceptId()%></a></TD>
      <TD align="left"><%=result.getField()%></TD>
      <TD align="left"><%=result.getType()%></TD>
    <%
    }
    %>
   </TR>
</TABLE>
</form>
</Body>
</HTML>