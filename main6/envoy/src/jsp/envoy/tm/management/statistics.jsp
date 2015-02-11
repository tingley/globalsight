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
<!-- This is /envoy/src/jsp/envoy/tm/management/statistics.jsp -->
<head>
<title><%= bundle.getString("lb_tm_statistics") %></title>
<META HTTP-EQUIV="EXPIRES" CONTENT="0">
<SCRIPT TYPE="text/javascript" SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<SCRIPT TYPE="text/javascript" SRC="/globalsight/includes/library.js"></SCRIPT>
<SCRIPT TYPE="text/javascript" SRC="envoy/tm/management/protocol.js"></SCRIPT>
<SCRIPT TYPE="text/javascript" SRC="/globalsight/jquery/jquery-1.6.4.min.js"></SCRIPT>
<SCRIPT TYPE="text/javascript">
var str_error = "<%=str_error == null ? "" : str_error%>";
var str_tmName = "<%=str_tmName%>";

function parseStatistics()
{ 
  var xmlStr = 
	  "<%=xmlStatistics.replace("\n","").replace("\r","").trim()%>";
  var $xml = $( $.parseXML( xmlStr ) );

  var language, tus, tuvs;
  $("#idTmName").html($xml.find("statistics > tm").text());
  $("#idTmTus").html($xml.find("statistics > tus").text());
  $("#idTmTuvs").html($xml.find("statistics > tuvs").text());

  $("#idTableBody").html("");
  $xml.find("statistics > languages > language").each(function(){
	  language = $(this).find("name").text();
	  tus = $(this).find("tus").text();
	  tuvs = $(this).find("tuvs").text();
	  html = "<tr><td>" + language + "</td><td align='right'>" + tus 
	  		 + "</td><td align='right'>" + tuvs + "</td></tr>";
	  $("#idTableBody").append(html);
  });
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
