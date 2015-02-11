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
<jsp:useBean id="ok" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean" />
<%
ResourceBundle bundle = PageHandler.getBundle(session);
SessionManager sessionMgr = (SessionManager)session.getAttribute(
  WebAppConstants.SESSION_MANAGER);

String str_tmName =
  (String)sessionMgr.getAttribute(WebAppConstants.TM_TM_NAME);

String xmlStatistics =
  (String)sessionMgr.getAttribute(WebAppConstants.TM_STATISTICS);
sessionMgr.removeElement(WebAppConstants.TM_STATISTICS);
String tmType = (String)sessionMgr.getAttribute(WebAppConstants.TM_TYPE);

String str_error =
  (String)sessionMgr.getAttribute(WebAppConstants.TM_ERROR);
sessionMgr.removeElement(WebAppConstants.TM_ERROR);

String urlOk = ok.getPageURL();
%>
<html>
<head>
<title><%= bundle.getString("lb_tm_statistics") %></title>
<META HTTP-EQUIV="EXPIRES" CONTENT="0">
<STYLE></STYLE>
<%@ include file="/includes/compatibility.jspIncl" %>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/library.js"></SCRIPT>
<SCRIPT LANGUAGE="Javascript" src="envoy/tm/management/protocol.js"></SCRIPT>
<SCRIPT LANGUAGE="Javascript">
var str_error = "<%=str_error == null ? "" : str_error%>";
var str_tmName = "<%=str_tmName%>";

function parseStatistics()
{ 
  var dom;
  //Mozilla compatibility  
  var xmlStr = 
	  "<%=xmlStatistics.replace("\n","").replace("\r","").trim()%>";

  if(ie)
  {
    dom = oStatistics.XMLDocument;
  }
  else if(window.DOMParser)
  { 
    var parser = new DOMParser();
    dom = parser.parseFromString(xmlStr,"text/xml");
  }

  var tbody = idTableBody;

  for (var i = tbody.rows.length; i > 0; --i)
  {
     tbody.deleteRow(i-1);
  }

  var row, cell;

  var nodes, node;
  var tm, tus, tuvs, language, number;

  tm = dom.selectSingleNode('/statistics/tm').text;
  tus  = dom.selectSingleNode('/statistics/tus').text;
  tuvs = dom.selectSingleNode('/statistics/tuvs').text;

  idTmName.innerHTML = tm;
  idTmTus.innerHTML = tus;
  idTmTuvs.innerHTML = tuvs;

  nodes = dom.selectNodes('//languages/language');
  for (var i = 0; i < nodes.length; ++i)
  {
    node = nodes[i];

    row = tbody.insertRow(i);

    language = node.selectSingleNode('name').text;
    tus  = node.selectSingleNode('tus').text;
    tuvs = node.selectSingleNode('tuvs').text;

    cell = row.insertCell(0);
    cell.innerHTML = language;

    cell = row.insertCell(1);
    cell.align = 'right';
    cell.innerHTML = tus;

    cell = row.insertCell(2);
    cell.align = 'right';
    cell.innerHTML = tuvs;
  }
}

function init()
{
  if (str_error)
  {
    showError(new Error("Server Error", str_error));
  }

  parseStatistics();

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
  window.focus();

  init();

  idOk.focus();
}
</script>
</head>

<body onload="doLoad()" onkeypress="doKeypress()">

<XML id="oStatistics" style="display:none"><%=xmlStatistics%></XML>

<DIV ID="contentLayer"
  STYLE="POSITION: ABSOLUTE; TOP: 10px; LEFT: 10px;width:96%;">
<DIV ID="idHeading" CLASS="mainHeading"><%= bundle.getString("lb_tm_statistics") %></DIV>
<DIV ID="idHeading" CLASS="standardText"><%= bundle.getString("lb_tm_type") %> : <b><i><%=tmType %></i></b></DIV>
<BR>

<DIV ID="idStatistics">
<TABLE id="idTable" width="100%"
  CELLPADDING=1 CELLSPACING=0 BORDER=1 CLASS="standardText"
  style="border-collapse: collapse">
  <THEAD>
    <TR>
      <TD colspan=1 align="left"
        style="background-color: #a6b8ce" nowrap><%=bundle.getString("lb_tm_tm")%>
      </TD>
      <TD colspan=1 align="right"
        style="background-color: #a6b8ce" nowrap><%=bundle.getString("lb_tm_total_tus")%>
      </TD>
      <TD colspan=1 align="right"
        style="background-color: #a6b8ce" nowrap><%=bundle.getString("lb_tm_total_tuvs")%>
      </TD>
    </TR>
    <TR>
      <TD align="left" id="idTmName"></TD>
      <TD align="right"id="idTmTus"></TD>
      <TD align="right"id="idTmTuvs"></TD>
    </TR>
    <TR style="background-color: #b6c8de; border-top-color: #b6c8de">
      <TD width="50%" valign="top" align="left" nowrap><%=bundle.getString("lb_tm_language")%></TD>
      <TD width="25%" valign="top" align="right" nowrap><%=bundle.getString("lb_tm_tus")%></TD>
      <TD width="25%" valign="top" align="right" nowrap><%=bundle.getString("lb_tm_tuvs")%></TD>
    </TR>
  </THEAD>
  <TBODY id="idTableBody"></TBODY>
</TABLE>
</DIV>

<BR>
<DIV ALIGN="CENTER">
<INPUT id="idOk" TYPE=BUTTON VALUE="<%=bundle.getString("lb_close")%>" TABINDEX="1" onclick="doClose();">
</DIV>

</DIV>
</body>
</html>
