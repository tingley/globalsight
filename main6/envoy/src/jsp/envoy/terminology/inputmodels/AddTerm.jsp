<%@ page
    contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="java.util.*,com.globalsight.everest.webapp.webnavigation.LinkHelper,
        java.util.ResourceBundle,
        com.globalsight.util.edit.EditUtil,
        com.globalsight.everest.servlet.util.SessionManager,
        com.globalsight.everest.webapp.pagehandler.PageHandler,
        com.globalsight.everest.webapp.WebAppConstants"
    session="true"
%>
<%
ResourceBundle bundle = PageHandler.getBundle(session);
String title = bundle.getString("lb_term_input_model_add_synonym_constraint");
%>
<html>
<head>
<title><%=title %></title>
<META HTTP-EQUIV="EXPIRES" CONTENT="0">
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/library.js"></SCRIPT>
<script LANGUAGE="JavaScript" src="/globalsight/envoy/terminology/management/FireFox.js"></script>
<SCRIPT language="Javascript">
var g_langlocs;

function initLangLocs()
{
  for (var i = 0; i < g_langlocs.length; ++i)
  {
    var langloc = g_langlocs[i];
    var oOption = new Option(langloc.getLanguage(),i);
    document.getElementById("idLanguage").options.add(oOption);
  }
}

function getSelectedLangLoc()
{
  return g_langlocs[idLanguage.selectedIndex];
}

function doClose(ok)
{
  if (ok == true)
  {
    var langloc = getSelectedLangLoc();
    var term;

    if (idRequired.checked)
    {
      term = "required";
    }
    else
    {
      term = "optional";
    }

    if (idMultiple.checked)
    {
      term += ", multiple";
    }

    langloc.setTerm(term);
    window.returnValue = langloc;
  }
  else
  {
    window.returnValue = null;
  }

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
  g_langlocs = window.dialogArguments;

  initLangLocs();

  idLanguage.disabled = true;

  idOptional.focus();
}
</script>
</head>

<body onload="doLoad()" onkeypress="doKeypress()">

<DIV ID="contentLayer"
  STYLE="POSITION: ABSOLUTE; TOP: 10px; LEFT: 10px;">
<SPAN ID="idHeading" CLASS="mainHeading"><%=title %></SPAN>
<BR>
<TABLE CELLPADDING=2 CELLSPACING=2 BORDER=0 CLASS="standardText">
  <TR>
    <TD colspan=2>
      <LABEL FOR="idLanguage"><%=bundle.getString("lb_term_input_model_synonyms_in_lang") %>:</LABEL>
      <select id="idLanguage" TABINDEX="-1"></select>
    </TD>
  </TR>
  <TR>
    <TD rowspan=2 valign=top>
      <%=bundle.getString("lb_term_input_model_are") %>:
    </TD>
    <TD>
      <INPUT TYPE="radio" name="m" id="idRequired" TABINDEX="2">
      <LABEL FOR="idRequired"><%=bundle.getString("lb_editor_required") %></LABEL>
      <INPUT TYPE="radio" name="m" id="idOptional" TABINDEX="3" checked>
      <LABEL FOR="idOptional"><%=bundle.getString("lb_editor_optional") %></LABEL>
    </TD>
  </TR>
  <TR>
    <TD>
      <INPUT TYPE="checkbox" id="idMultiple" TABINDEX="4" checked>
      <LABEL FOR="idMultiple"><%=bundle.getString("lb_term_input_model_occur_multi_times") %></LABEL>
    </TD>
  </TR>
</TABLE>

<BR>

<DIV ALIGN="CENTER">
<INPUT id="idOk" TYPE=BUTTON VALUE=" <%=bundle.getString("lb_ok") %> " TABINDEX="5" onclick="doClose(true);">
<INPUT TYPE=BUTTON VALUE="<%=bundle.getString("lb_cancel") %>" TABINDEX="6" onclick="doClose(false);">
</DIV>

</DIV>
</body>
</html>
