<%@ page
    contentType="text/html; charset=UTF-8"
    errorPage="error.jsp"
    import="com.globalsight.util.edit.EditUtil,
            com.globalsight.util.edit.GxmlUtil,
            com.globalsight.config.UserParamNames,
            com.globalsight.everest.edit.online.SegmentView,
            com.globalsight.everest.webapp.WebAppConstants,
            com.globalsight.everest.webapp.pagehandler.PageHandler,
            com.globalsight.everest.webapp.pagehandler.edit.online.EditorConstants,
     	    com.globalsight.everest.webapp.pagehandler.edit.online.EditorState,
            com.globalsight.everest.servlet.util.SessionManager,
            com.globalsight.util.resourcebundle.ResourceBundleConstants,
            com.globalsight.util.resourcebundle.SystemResourceBundle,
            java.util.*"
    session="true"
%>
<jsp:useBean id="options" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean"/>
<%
ResourceBundle bundle = PageHandler.getBundle(session);

String url_options = options.getPageURL();

String lb_title = bundle.getString("lb_segment_editor_options");
String lb_options = lb_title;

String lb_ok     = bundle.getString("lb_apply");
String lb_cancel = bundle.getString("lb_close");
String lb_autosave = bundle.getString("lb_autosave");
String lb_autowhite = bundle.getString("lb_autowhite");
String lb_tmThreshold = bundle.getString("lb_tm_threshold");
String lb_tbThreshold = bundle.getString("lb_tb_threshold");
String lb_numericValue = bundle.getString("lb_numeric_value");
String lb_default_ptagmode = bundle.getString("lb_default_ptagmode");
String lb_compact = bundle.getString("lb_compact");
String lb_verbose = bundle.getString("lb_verbose");
String lb_hilitepTags = bundle.getString("lb_highlight_ptags");
String lb_iterateSubs = bundle.getString("lb_pre_next_always_iterate_subs");

String autosave = (String)request.getAttribute(UserParamNames.EDITOR_AUTO_SAVE_SEGMENT);
String autosaveChecked = autosave.equals("0") ? "" : "CHECKED";
String autowhite = (String)request.getAttribute(UserParamNames.EDITOR_AUTO_ADJUST_WHITESPACE);
String autowhiteChecked = autowhite.equals("0") ? "" : "CHECKED";
String ptagmode = (String)request.getAttribute(UserParamNames.EDITOR_PTAGMODE);
String hiliteptags = (String)request.getAttribute(UserParamNames.EDITOR_PTAGHILITE);
String ptagHiliteChecked = hiliteptags.equals("0") ? "" : "CHECKED";
String iterateSubs = (String)request.getAttribute(UserParamNames.EDITOR_ITERATE_SUBS);
String iterateSubsChecked = iterateSubs.equals("0") ? "" : "CHECKED";
String tmThreshold = (String)request.getAttribute(UserParamNames.TM_MATCHING_THRESHOLD);
String tbThreshold = (String)request.getAttribute(UserParamNames.TB_MATCHING_THRESHOLD);

String errorMsg = (String)request.getAttribute(WebAppConstants.USER_PARAMS_ERROR);
%>
<HTML>
<HEAD>
<TITLE><%=lb_title%></TITLE>
<SCRIPT SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<SCRIPT SRC="/globalsight/includes/formValidation.js"></SCRIPT>
<SCRIPT SRC="/globalsight/envoy/terminology/viewer/error.js"></SCRIPT>
<SCRIPT>
var errorMessage = "<%=EditUtil.toJavascript(errorMsg)%>";

function showHourglass()
{
    form.apply.disabled = true;
    form.cancel.disabled = true;
    idBody.style.cursor = "wait";
}

function checkForm()
{
    showHourglass();

    var field;

    field = form.autosave;
    if (field.checked)
    {
      form.<%=UserParamNames.EDITOR_AUTO_SAVE_SEGMENT%>.value = "1";
    }
    else
    {
      form.<%=UserParamNames.EDITOR_AUTO_SAVE_SEGMENT%>.value = "0";
    }

    field = form.autowhite;
    if (field.checked)
    {
      form.<%=UserParamNames.EDITOR_AUTO_ADJUST_WHITESPACE%>.value = "1";
    }
    else
    {
      form.<%=UserParamNames.EDITOR_AUTO_ADJUST_WHITESPACE%>.value = "0";
    }

    if (form.ptagmode[0].checked)
    {
      form.<%=UserParamNames.EDITOR_PTAGMODE%>.value = "<%=EditorConstants.PTAGS_COMPACT%>";
    }
    else 
    {
      form.<%=UserParamNames.EDITOR_PTAGMODE%>.value = "<%=EditorConstants.PTAGS_VERBOSE%>";
    }

    field = form.hiliteptags;
    if (field.checked)
    {
      form.<%=UserParamNames.EDITOR_PTAGHILITE%>.value = "1";
    }
    else
    {
      form.<%=UserParamNames.EDITOR_PTAGHILITE%>.value = "0";
    }

    field = form.iteratesubs;
    if (field.checked)
    {
      form.<%=UserParamNames.EDITOR_ITERATE_SUBS%>.value = "1";
    }
    else
    {
      form.<%=UserParamNames.EDITOR_ITERATE_SUBS%>.value = "0";
    }

<%--
    field = form.<%=UserParamNames.TM_MATCHING_THRESHOLD%>;
    if (!isRange(field, 1, 100))
    {
      alert("<%=EditUtil.toJavascript(lb_numericValue)%>");
      field.focus();
      return false;
    }

    field = form.<%=UserParamNames.TB_MATCHING_THRESHOLD%>;
    if (!isRange(field, 1, 100))
    {
      alert("<%=EditUtil.toJavascript(lb_numericValue)%>");
      field.focus();
      return false;
    }
--%>

    applySettings();

    return true;
}

function initForm()
{
  if ("<%=ptagmode%>" == "<%=EditorConstants.PTAGS_COMPACT%>")
  {
    form.ptagmode[0].checked = true;
  }
  else
  {
    form.ptagmode[1].checked = true;
  }
}

function applySettings()
{
  try
  {
    window.opener.autosave = form.autosave.checked ? "true" : "false";
    window.opener.HilitePtags(form.hiliteptags.checked);
  }
  catch (e) {}

  try
  {
    // Thu Jan 20 01:04:27 2005 CvdL: I think autowhite is dead in this UI
    window.opener.autowhite = form.autowhite.checked ? "true" : "false";
  }
  catch (e) {}
}

function closeThis()
{
  try { window.close(); } catch (e) {}
}

function doKeyPress()
{
  var key = event.keyCode;

  if (key == 27) // Escape
  {
    closeThis();
  }
}

function init()
{
    initForm();
    window.focus();

    if (errorMessage != "")
    {
      ShowError(errorMessage);
    }
}
</SCRIPT>
</HEAD>
<BODY id="idBody" style="margin:1ex" onload="init();" onkeypress="doKeyPress()">
<SPAN CLASS="mainHeading"><%=lb_options%></SPAN>
<P>
<FORM ACTION="<%=url_options%>" METHOD="post" name="form"
 onsubmit="return checkForm();">
<INPUT type="hidden" name="__save" VALUE="yes">
<INPUT type="hidden" name="<%=UserParamNames.EDITOR_AUTO_SAVE_SEGMENT%>" value="mlah!">
<INPUT type="hidden" name="<%=UserParamNames.EDITOR_AUTO_ADJUST_WHITESPACE%>" value="mlah!">
<INPUT type="hidden" name="<%=UserParamNames.EDITOR_PTAGMODE%>" value="<%=ptagmode%>">
<INPUT type="hidden" name="<%=UserParamNames.EDITOR_PTAGHILITE%>" value="mlah!">
<INPUT type="hidden" name="<%=UserParamNames.EDITOR_ITERATE_SUBS%>" value="mlah!">
<TABLE>
  <COL width="70%"><COL width="30%">
  <TBODY>  
  <TR>
    <TD><SPAN class="standardText"><%=lb_autosave%></SPAN></TD>
    <TD><INPUT type="checkbox" name="autosave" <%=autosaveChecked%> ></TD>
  </TR>
  <TR>
    <TD><SPAN class="standardText"><%=lb_autowhite%></SPAN></TD>
    <TD><INPUT type="checkbox" name="autowhite" <%=autowhiteChecked%> ></TD>
  </TR>
  <TR>
    <TD VALIGN="top"><SPAN class="standardText"><%=lb_default_ptagmode%></SPAN></TD>
    <TD VALIGN="top" class="standardText">
      <INPUT type="radio" name="ptagmode" id="id_mode1">
      <LABEL for="id_mode1"><%=lb_compact%></LABEL><BR>
      <INPUT type="radio" name="ptagmode" id="id_mode2">
      <LABEL for="id_mode2"><%=lb_verbose%></LABEL>
    </TD>
  </TR>
  <TR>
    <TD><SPAN class="standardText"><%=lb_hilitepTags%>:</SPAN></TD>
    <TD><INPUT type="checkbox" name="hiliteptags" <%=ptagHiliteChecked%> ></TD>
  </TR>
  <TR>
    <TD><SPAN class="standardText"><%=lb_iterateSubs%>:</SPAN></TD>
    <TD><INPUT type="checkbox" name="iteratesubs" <%=iterateSubsChecked%> ></TD>
  </TR>
  <%--
  <TR><TD colspan="2">&nbsp;</TD></TR>
  <TR>
    <TD><SPAN class="standardText"><%=lb_tmThreshold%></SPAN></TD>
    <TD><INPUT type="text" size="3" name="<%=TM_MATCHING_THRESHOLD%>"
      value="<%=tmThreshold%>">%</TD>
  </TR>
  <TR>
    <TD><SPAN class="standardText"><%=lb_tbThreshold%></SPAN></TD>
    <TD><INPUT type="text" size="3" name="<%=TB_MATCHING_THRESHOLD%>"
      value="<%=tbThreshold%>">%</TD>
  </TR>
  --%>
  </TBODY>
</TABLE>
<P>
<CENTER>
<INPUT type="submit" value="<%=lb_ok%>" name="apply"> &nbsp;&nbsp;&nbsp;
<INPUT type="button" onclick="closeThis()" value="<%=lb_cancel%>" name="cancel">
</CENTER>
</FORM>
</BODY>
</HTML>

