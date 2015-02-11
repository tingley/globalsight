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
String title = bundle.getString("lb_term_input_model_edit_term_constraints");
%>
<html>
<head>
<title><%=title %></title>
<META HTTP-EQUIV="EXPIRES" CONTENT="0">
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/library.js"></SCRIPT>
<SCRIPT language="Javascript">
var g_args;

function doClose(ok)
{
  if (ok == true)
  {
    var value;

    if (idRequired.checked)
    {
      value = "required";
    }
    else
    {
      value = "optional";
    }

    if (idMultiple.checked)
    {
      value += ", multiple";
    }

    g_args.setTerm(value);

    window.returnValue = g_args;
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

function doWindowLoad()
{
  g_args = window.dialogArguments;

  idTerm.innerText = g_args.isMainTerm() ? "<%=bundle.getString("lb_term_input_model_main_term") %>" : "<%=bundle.getString("lb_term_input_model_synonyms") %>";
  idTermVerb.innerText = g_args.isMainTerm() ? "<%=bundle.getString("lb_term_input_model_is") %>:" : "<%=bundle.getString("lb_term_input_model_are") %>:";
  idLanguage.innerText = g_args.getLanguage();
  var value = g_args.getTerm();

  if (value.indexOf("required") >= 0)
  {
    idRequired.checked = true;
    idOptional.focus();
  }
  else
  {
    idOptional.checked = true;
    idRequired.focus();
  }

  if (!g_args.isMainTerm()) 
  {
    idRow.style.display = "";
    if (value.indexOf("multiple") >= 0)
    {
      idMultiple.checked = true;
    }
  }
}
</script>
</head>

<body onload="doWindowLoad()" onkeypress="doKeypress()">

<DIV ID="contentLayer"
  STYLE="POSITION: ABSOLUTE; TOP: 10px; LEFT: 10px;">
<SPAN ID="idHeading" CLASS="mainHeading"><%=title %></SPAN>
<BR>
<TABLE CELLPADDING=2 CELLSPACING=2 BORDER=0 CLASS="standardText">
  <TR>
    <TD valign="top" colspan=2>
      <LABEL><SPAN id="idTerm"><%=bundle.getString("lb_terms") %></SPAN> <%=bundle.getString("lb_term_input_model_in_lang") %>:</LABEL>
      <span id="idLanguage"></span>
    </TD>
  </TR>
  <TR>
    <TD rowspan=2 valign=top>
      <SPAN id="idTermVerb"><%=bundle.getString("lb_term_input_model_are") %>:</SPAN>
    </TD>
    <TD>
      <INPUT TYPE="radio" name="m" id="idRequired" TABINDEX="1">
      <LABEL FOR="idRequired"><%=bundle.getString("lb_editor_required") %></LABEL>
      <INPUT TYPE="radio" name="m" id="idOptional" TABINDEX="2">
      <LABEL FOR="idOptional"><%=bundle.getString("lb_editor_optional") %></LABEL>
    </TD>
  </TR>
  <TR id="idRow" style="display: none">
    <TD>
      <INPUT TYPE="checkbox" id="idMultiple" TABINDEX="3">
      <LABEL FOR="idMultiple"><%=bundle.getString("lb_term_input_model_occur_multi_times") %></LABEL>
    </TD>
  </TR>
</TABLE>

<BR>

<DIV ALIGN="CENTER">
<INPUT id="idOk" TYPE=BUTTON VALUE=" <%=bundle.getString("lb_ok") %> " TABINDEX="4" onclick="doClose(true);">
<INPUT TYPE=BUTTON VALUE="<%=bundle.getString("lb_cancel") %>" TABINDEX="5" onclick="doClose(false);">
</DIV>

</DIV>
</body>
</html>
