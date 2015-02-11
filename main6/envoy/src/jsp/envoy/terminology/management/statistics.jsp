<%@ page
    contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="java.util.*,com.globalsight.everest.servlet.util.SessionManager,
	com.globalsight.everest.webapp.javabean.NavigationBean,
        com.globalsight.everest.webapp.WebAppConstants,
        com.globalsight.everest.webapp.pagehandler.PageHandler,
        com.globalsight.everest.webapp.webnavigation.LinkHelper,
        com.globalsight.util.edit.EditUtil,
	java.util.ResourceBundle"
    session="true"
%>
<%
ResourceBundle bundle = PageHandler.getBundle(session);
SessionManager sessionMgr = (SessionManager)session.getAttribute(
  WebAppConstants.SESSION_MANAGER);

String xmlStatistics =
  (String)sessionMgr.getAttribute(WebAppConstants.TERMBASE_STATISTICS);
sessionMgr.removeElement(WebAppConstants.TERMBASE_STATISTICS);

%>
<html>
<head>
<title><%=bundle.getString("lb_tb_statistics")%></title>
<META HTTP-EQUIV="EXPIRES" CONTENT="0">
<STYLE></STYLE>
<%@ include file="/includes/compatibility.jspIncl" %>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/library.js"></SCRIPT>
<SCRIPT language="Javascript">
function parseStatistics(dom)
{
  var tbody = idTableBody;

  for (var i = tbody.rows.length; i > 0; --i)
  {
     tbody.deleteRow(i-1);
  }

  var row, cell;

  var nodes, node;
  var termbase, entries, terms, language, number;

  termbase = dom.selectSingleNode('/statistics/termbase').text;
  entries  = dom.selectSingleNode('/statistics/concepts').text;
  terms    = dom.selectSingleNode('/statistics/terms').text;

  idTermbaseName.innerHTML = termbase;//idTermbaseName.innerText = termbase;
  idTermbaseEntries.innerHTML = entries;
  idTermbaseTerms.innerHTML = terms;

  nodes = dom.selectNodes('/statistics/indexes/index');
  for (var i = 0; i < nodes.length; ++i)
  {
    node = nodes[i];

    row = tbody.insertRow(i);
    var j=0;

    language = node.selectSingleNode('language').text;
    number = node.selectSingleNode('terms').text;

    cell = row.insertCell(j++);
    cell.innerHTML = language;

    cell = row.insertCell(j++);
    cell.colSpan = 2;
    cell.align = 'right';
    cell.innerHTML = number;
  }
}

function init()
{
  var dom;
  var xmlStatisticsStr	= 
	  "<%=xmlStatistics.replace("\n","").replace("\r","").trim()%>";
  if(window.navigator.userAgent.indexOf("MSIE")>0)
  {
	dom = xmlStatistics.XMLDocument;
  }
  else if(window.DOMParser)
  { 
	var parser = new DOMParser();
	dom = parser.parseFromString(xmlStatisticsStr,"text/xml");
  }

  parseStatistics(dom);
  idOk.focus();
}

function doClose(ok)
{
  window.close();
}

function doKeypress()
{
  var key = event.keyCode;

  if (key == 27) // Escape
  {
    doClose(false);
  }
  else if (key == 13) // Return
  {
    doClose(true);
  }
}

function doLoad()
{
  init();
  window.focus();
}
</script>
</head>

<body onload="doLoad()" onkeypress="doKeypress()">
<XML id="xmlStatistics" style="display:none"><%=xmlStatistics%></XML>

<DIV ID="contentLayer"
  STYLE="POSITION: ABSOLUTE; TOP: 10px; LEFT: 10px;width:96%">
<DIV ID="idHeading" CLASS="mainHeading"><%=bundle.getString("lb_tb_statistics")%></DIV>
<BR>

<DIV ID="idStatistics">
<TABLE id="idTable" width="100%"
  CELLPADDING=1 CELLSPACING=0 BORDER=1 CLASS="standardText"
  style="border-collapse: collapse">
  <THEAD>
    <TR style="background-color: #a6b8ce">
      <TD width="50%" align="left" >
	<%=bundle.getString("lb_termbase")%></TD>
      <TD width="25%" align="right">
	<%=bundle.getString("lb_entries")%></TD>
      <TD width="25%" align="right"><%=bundle.getString("lb_total_terms") %></TD>
    </TR>
    <TR>
      <TD align="left"  id="idTermbaseName"></TD>
      <TD align="right" id="idTermbaseEntries"></TD>
      <TD align="right" id="idTermbaseTerms"></TD>
    </TR>
    <TR style="background-color: #b6c8de; border-top-color: #b6c8de">
      <TD width="50%" valign="top" align="left">
	<%=bundle.getString("lb_language")%></TD>
      <TD colspan=2 width="50%" valign="top" align="right">
	<%=bundle.getString("lb_terms")%></TD>
    </TR>
  </THEAD>
  <TBODY id="idTableBody"></TBODY>
</TABLE>
</DIV>

<BR>
<DIV ALIGN="CENTER">
<INPUT id="idOk" TYPE=BUTTON VALUE="<%=bundle.getString("lb_close")%>"
 TABINDEX="1" onclick="doClose();">
</DIV>

</DIV>
</body>
</html>
