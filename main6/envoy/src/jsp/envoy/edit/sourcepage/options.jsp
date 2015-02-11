<%@ page
    contentType="text/html; charset=UTF-8"
    errorPage="error.jsp"
    import="java.util.*,com.globalsight.util.edit.EditUtil,
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
            java.util.Locale,
            java.util.ResourceBundle"
    session="true"
%>
<jsp:useBean id="options" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean"/>
<%
ResourceBundle bundle = PageHandler.getBundle(session);

SessionManager sessionMgr = (SessionManager)session.getAttribute(
  WebAppConstants.SESSION_MANAGER);
EditorState state =
  (EditorState)sessionMgr.getAttribute(WebAppConstants.EDITORSTATE);

String url_options = options.getPageURL();

String lb_title = "Inline Editor Options" /*bundle.getString("lb_segment_editor_options")*/;

String lb_ok     = bundle.getString("lb_apply");
String lb_cancel = bundle.getString("lb_close");
//String lb_autosave = bundle.getString("lb_autosave");
String lb_autoUnlockSegments = bundle.getString("lb_auto_unlock_segments");
String lb_autowhite = bundle.getString("lb_autowhite");
String lb_hilitepTags = "Highlight PTags:";
String lb_showMt = "Show machine translation (MT) button:";
//String lb_tmThreshold = bundle.getString("lb_tm_threshold");
//String lb_tbThreshold = bundle.getString("lb_tb_threshold");
//String lb_numericValue = bundle.getString("lb_numeric_value");
//String lb_default_ptagmode = bundle.getString("lb_default_ptagmode");
//String lb_compact = bundle.getString("lb_compact");
//String lb_verbose = bundle.getString("lb_verbose");

//String autosave = (String)request.getAttribute(UserParamNames.EDITOR_AUTO_SAVE_SEGMENT);
String autowhite = (String)request.getAttribute(UserParamNames.EDITOR_AUTO_ADJUST_WHITESPACE);
String autoUnlock = (String)request.getAttribute(UserParamNames.EDITOR_AUTO_UNLOCK);
//String ptagmode = (String)request.getAttribute(UserParamNames.EDITOR_PTAGMODE);
String hiliteptags = (String)request.getAttribute(UserParamNames.EDITOR_PTAGHILITE);
String showMt = (String)request.getAttribute(UserParamNames.EDITOR_SHOW_MT);
//String tmThreshold = (String)request.getAttribute(UserParamNames.TM_MATCHING_THRESHOLD);
//String tbThreshold = (String)request.getAttribute(UserParamNames.TB_MATCHING_THRESHOLD);

//String autosaveChecked = autosave.equals("0") ? "" : "CHECKED";
String autowhiteChecked = autowhite.equals("0") ? "" : "CHECKED";
String ptagHiliteChecked = hiliteptags.equals("0") ? "" : "CHECKED";
String showMtChecked = showMt.equals("0") ? "" : "CHECKED";

boolean b_canShowMt = state.canShowMt();

String errorMsg = (String)request.getAttribute(WebAppConstants.USER_PARAMS_ERROR);
%>
<HTML>
<HEAD>
<TITLE><%=lb_title%></TITLE>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/formValidation.js"></SCRIPT>
<SCRIPT LANGUAGE="Javascript" src="/globalsight/envoy/terminology/viewer/error.js"></SCRIPT>
<SCRIPT LANGUAGE="JavaScript">
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

    field = form.autowhite;
    if (field.checked)
    {
      form.<%=UserParamNames.EDITOR_AUTO_ADJUST_WHITESPACE%>.value = "1";
    }
    else
    {
      form.<%=UserParamNames.EDITOR_AUTO_ADJUST_WHITESPACE%>.value = "0";
    }

    field = form.autounlock;
    if (field.checked)
    {
      form.<%=UserParamNames.EDITOR_AUTO_UNLOCK%>.value = "1";
    }
    else
    {
      form.<%=UserParamNames.EDITOR_AUTO_UNLOCK%>.value = "0";
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

    // <% if (b_canShowMt) { %>
    field = form.showmt;
    if (field.checked)
    {
      form.<%=UserParamNames.EDITOR_SHOW_MT%>.value = "1";
    }
    else
    {
      form.<%=UserParamNames.EDITOR_SHOW_MT%>.value = "0";
    }
    // <% } %>

    applySettings();

    return true;
}

function initForm()
{
  if ("<%=autoUnlock%>" == "1") form.autounlock.checked = true;
}

function applySettings()
{
  try
  {
    window.opener.autowhite = form.autowhite.checked ? "true" : "false";
    window.opener.HilitePtags(form.hiliteptags.checked);
    if (form.autounlock.checked)
    {
      window.opener.unlockSegments();
    }
    // <% if (b_canShowMt) { %>
    window.opener.showMtButton(form.showmt.checked);
    // <% } %>
  }
  catch (e) {}
}

function closeThis()
{
  try { window.close(); } catch (e) {}
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
<BODY style="margin:1ex" onload="init();" id="idBody">
<SPAN CLASS="mainHeading"><%=lb_title%></SPAN>
<P>
<FORM ACTION="<%=url_options%>" METHOD="post" name="form"
 onsubmit="return checkForm();">
<INPUT type="hidden" name="__save" VALUE="yes">
<INPUT type="hidden" name="<%=UserParamNames.EDITOR_AUTO_UNLOCK%>" value="mlah!">
<INPUT type="hidden" name="<%=UserParamNames.EDITOR_AUTO_ADJUST_WHITESPACE%>" value="mlah!">
<INPUT type="hidden" name="<%=UserParamNames.EDITOR_PTAGHILITE%>" value="mlah!">
<INPUT type="hidden" name="<%=UserParamNames.EDITOR_SHOW_MT%>" value="<%=showMt%>">
<TABLE>
  <COL width="70%"><COL width="30%">
  <TBODY>
  <TR>
    <TD><SPAN class="standardText"><%=lb_autowhite%></SPAN></TD>
    <TD><INPUT type="checkbox" name="autowhite" <%=autowhiteChecked%> ></TD>
  </TR>
  <TR>
    <TD><SPAN class="standardText"><%=lb_autoUnlockSegments%></SPAN></TD>
    <TD><INPUT type="checkbox" name="autounlock"></TD>
  </TR>
  <% if (b_canShowMt) { %>
  <TR>
    <TD><SPAN class="standardText"><%=lb_showMt%></SPAN></TD>
    <TD><INPUT type="checkbox" name="showmt" <%=showMtChecked%> ></TD>
  </TR>
  <% } %>
  <TR>
    <TD><SPAN class="standardText"><%=lb_hilitepTags%></SPAN></TD>
    <TD><INPUT type="checkbox" name="hiliteptags" <%=ptagHiliteChecked%> ></TD>
  </TR>
  </TBODY>
</TABLE>
<P>
<CENTER>
<INPUT type="submit" value="<%=lb_ok%>" name="apply">
&nbsp;&nbsp;&nbsp;
<INPUT type="button" onclick="closeThis()" value="<%=lb_cancel%>" name="cancel">
</CENTER>
</FORM>
</BODY>
</HTML>

