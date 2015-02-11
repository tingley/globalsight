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

boolean b_reviewMode = state.isReviewMode();

String url_options = options.getPageURL();

String lb_title = bundle.getString("lb_main_editor_options");
String lb_options = lb_title;

String lb_ok     = bundle.getString("lb_apply");
String lb_cancel = bundle.getString("lb_close");

String lb_colorNote = bundle.getString("msg_colors_apply_to_preview");

String lb_linkColor        = bundle.getString("lb_normal_link_color");
String lb_activeLinkColor  = bundle.getString("lb_active_link_color");
String lb_visitedLinkColor = bundle.getString("lb_visited_link_color");
String lb_preview100MatchColor = bundle.getString("lb_Color_100match");
String lb_previewIceMatchColor = bundle.getString("lb_Color_ICEmatch");
String lb_previewNonMatchColor = bundle.getString("lb_Color_Nonmatch");
String lb_autoUnlockSegments = bundle.getString("lb_auto_unlock_segments");
String lb_autoSyncSegments = bundle.getString("lb_auto_sync_segments"); 
String lb_defaultLayout = bundle.getString("lb_default_layout");
String lb_layout_stv = bundle.getString("lb_layout_source_target_vertical");
String lb_layout_sth = bundle.getString("lb_layout_source_target_horizontal");
String lb_layout_s   = bundle.getString("lb_layout_source");
String lb_layout_t   = bundle.getString("lb_layout_target");
String lb_defaultViewmode = bundle.getString("lb_default_viewmode");
String lb_viewmode_preview = bundle.getString("lb_preview_if_available");
String lb_viewmode_text = bundle.getString("lb_text");
String lb_viewmode_list = bundle.getString("lb_list");
String lb_max_segment_number_in_editor = bundle.getString("lb_max_segment_number_in_editor");
String lb_segment_number_range_in_editor = bundle.getString("lb_segment_number_range_in_editor");
String msg_invalid_max_segments_num = bundle.getString("msg_max_segment_number_error");

String linkColor        = (String)request.getAttribute(UserParamNames.HYPERLINK_COLOR);
String activeLinkColor  = (String)request.getAttribute(UserParamNames.ACTIVE_HYPERLINK_COLOR);
String visitedLinkColor = (String)request.getAttribute(UserParamNames.VISITED_HYPERLINK_COLOR);

String preview100MatchColor = (String)request.getAttribute(UserParamNames.PREVIEW_100MATCH_COLOR);
String previewIceMatchColor = (String)request.getAttribute(UserParamNames.PREVIEW_ICEMATCH_COLOR);
String previewNonMatchColor = (String)request.getAttribute(UserParamNames.PREVIEW_NONMATCH_COLOR);

String override = (String)request.getAttribute(UserParamNames.HYPERLINK_COLOR_OVERRIDE);
String autoUnlock = (String)request.getAttribute(UserParamNames.EDITOR_AUTO_UNLOCK);
String autoSync = (String)request.getAttribute(UserParamNames.EDITOR_AUTO_SYNC);
String layout = (String)request.getAttribute(UserParamNames.EDITOR_LAYOUT);
String viewmode = (String)request.getAttribute(UserParamNames.EDITOR_VIEWMODE);
String maxSegmentsNum = (String)request.getAttribute(UserParamNames.EDITOR_SEGMENTS_MAX_NUM);

String errorMsg = (String)request.getAttribute(WebAppConstants.USER_PARAMS_ERROR);
%>
<HTML>
<HEAD>
<TITLE><%=lb_title%></TITLE>
<SCRIPT SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<SCRIPT SRC="/globalsight/includes/formValidation.js"></SCRIPT>
<SCRIPT src="/globalsight/envoy/terminology/viewer/error.js"></SCRIPT>
<SCRIPT>
var errorMessage = "<%=EditUtil.toJavascript(errorMsg)%>";

var g_reviewMode = eval("<%=b_reviewMode%>");

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

    if (!g_reviewMode)
    {
        field = form.autounlock;
        if (field.checked)
        {
          form.<%=UserParamNames.EDITOR_AUTO_UNLOCK%>.value = "1";
        }
        else
        {
          form.<%=UserParamNames.EDITOR_AUTO_UNLOCK%>.value = "0";
        }
    }
    field = form.autosync;
    if (field.checked)
    {
      form.<%=UserParamNames.EDITOR_AUTO_SYNC%>.value = "1";
    }
    else
    {
      form.<%=UserParamNames.EDITOR_AUTO_SYNC%>.value = "0";
    }

    if (form.layout[0].checked)
    {
      form.<%=UserParamNames.EDITOR_LAYOUT%>.value = "source_target_vertical";
    }
    else if (form.layout[1].checked)
    {
      form.<%=UserParamNames.EDITOR_LAYOUT%>.value = "source_target_horizontal";
    }
    else if (form.layout[2].checked)
    {
      form.<%=UserParamNames.EDITOR_LAYOUT%>.value = "source";
    }
    else if (form.layout[3].checked)
    {
      form.<%=UserParamNames.EDITOR_LAYOUT%>.value = "target";
    }

    if (form.viewmode[0].checked)
    {
      form.<%=UserParamNames.EDITOR_VIEWMODE%>.value = "<%=EditorConstants.VIEWMODE_PREVIEW%>";
    }
    else if (form.viewmode[1].checked)
    {
      form.<%=UserParamNames.EDITOR_VIEWMODE%>.value = "<%=EditorConstants.VIEWMODE_TEXT%>";
    }
    else if (form.viewmode[2].checked)
    {
      form.<%=UserParamNames.EDITOR_VIEWMODE%>.value = "<%=EditorConstants.VIEWMODE_DETAIL%>";
    }

    var maxSegNum = document.getElementById('idMaxSegmentsNum').value;
    var canSubmit = false;
    if (maxSegNum != null)
    {
        try {
            //maxSegNum can be 0, or [20,1000]
            if(!isNaN(maxSegNum)
                    && ( (Math.round(parseInt(maxSegNum)) >= 20 && Math.round(parseInt(maxSegNum)) <= 1000)
                            || Math.round(parseInt(maxSegNum)) == 0) )
            {
                canSubmit = true;
                document.getElementById('idMaxSegmentsNum').value = Math.round(parseInt(maxSegNum));
            }
        } catch (e) {}
    }

    if (canSubmit)
    {
        form.submit();
    } 
    else
    {
    	alert("<%=msg_invalid_max_segments_num%>");
        form.apply.disabled = false;
        form.cancel.disabled = false;
        idBody.style.cursor = "default";
    	return false;
    }
}

function initForm()
{
    if (!g_reviewMode)
    {
      if ("<%=autoUnlock%>" == "1") form.autounlock.checked = true;
    }
      if ("<%=autoSync%>" == "1") form.autosync.checked = true;

    if ("<%=layout%>" == "target")
      form.layout[3].checked = true;
    else if ("<%=layout%>" == "source")
      form.layout[2].checked = true;
    else if ("<%=layout%>" == "source_target_horizontal")
      form.layout[1].checked = true;
    else
      form.layout[0].checked = true;

    if ("<%=viewmode%>" == "<%=EditorConstants.VIEWMODE_DETAIL%>")
      form.viewmode[2].checked = true;
    else if ("<%=viewmode%>" ==  "<%=EditorConstants.VIEWMODE_TEXT%>")
      form.viewmode[1].checked = true;
    else
      form.viewmode[0].checked = true;
}

function closeThis()
{
    window.opener.refresh(0);
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
<SPAN CLASS="mainHeading"><%=lb_options%></SPAN>
<P>
<FORM ACTION="<%=url_options%>" METHOD="post" name="form">
<INPUT type="hidden" name="__save" VALUE="yes">
<% if (!b_reviewMode) { %>
<INPUT type="hidden" name="<%=UserParamNames.EDITOR_AUTO_UNLOCK%>" value="mlah!">
<% } %>
<INPUT type="hidden" name="<%=UserParamNames.EDITOR_AUTO_SYNC%>" value="1">
<INPUT type="hidden" name="<%=UserParamNames.EDITOR_LAYOUT%>" value="<%=layout%>">
<INPUT type="hidden" name="<%=UserParamNames.EDITOR_VIEWMODE%>" value="<%=viewmode%>">
<INPUT type="hidden" name="<%=UserParamNames.HYPERLINK_COLOR_OVERRIDE%>" VALUE="<%=override%>">
<P class="standardText"><%=lb_colorNote%></P>
<TABLE>
  <TR>
    <TD><SPAN class="standardText"><%=lb_linkColor%></SPAN></TD>
    <TD><INPUT type="text" size="20" name="<%=UserParamNames.HYPERLINK_COLOR%>"
      value="<%=linkColor%>"></TD>
  </TR>
  <TR>
    <TD><SPAN class="standardText"><%=lb_activeLinkColor%></SPAN></TD>
    <TD><INPUT type="text" size="20" name="<%=UserParamNames.ACTIVE_HYPERLINK_COLOR%>"
      value="<%=activeLinkColor%>"></TD>
  </TR>
  <TR>
    <TD><SPAN class="standardText"><%=lb_visitedLinkColor%></SPAN></TD>
    <TD><INPUT type="text" size="20" name="<%=UserParamNames.VISITED_HYPERLINK_COLOR%>"
      value="<%=visitedLinkColor%>"></TD>
  </TR>
  <TR>
    <TD colspan="2"><SPAN class="standardText"><%=bundle.getString("lb_Preview_ColorCode")%></SPAN></TD>
  </TR>
  <TR>
    <TD>&nbsp;&nbsp;&nbsp;&nbsp;<SPAN class="standardText"><%=lb_preview100MatchColor%></SPAN></TD>
    <TD><SELECT name="<%=UserParamNames.PREVIEW_100MATCH_COLOR%>">
    <option <%=("Black".equalsIgnoreCase(preview100MatchColor)) ? "selected=\"selected\"" : "" %> value="Black">Black</option>
    <option <%=("White".equalsIgnoreCase(preview100MatchColor)) ? "selected=\"selected\"" : "" %> value="White">White</option>
    <option <%=("Red".equalsIgnoreCase(preview100MatchColor)) ? "selected=\"selected\"" : "" %> value="Red">Red</option>
    <option <%=("Green".equalsIgnoreCase(preview100MatchColor)) ? "selected=\"selected\"" : "" %> value="Green">Green</option>
    <option <%=("Blue".equalsIgnoreCase(preview100MatchColor)) ? "selected=\"selected\"" : "" %> value="Blue">Blue</option>
    <option <%=("Cyan".equalsIgnoreCase(preview100MatchColor)) ? "selected=\"selected\"" : "" %> value="Cyan">Cyan</option>
    <option <%=("Magenta".equalsIgnoreCase(preview100MatchColor)) ? "selected=\"selected\"" : "" %> value="Magenta">Magenta</option>
    <option <%=("Yellow".equalsIgnoreCase(preview100MatchColor)) ? "selected=\"selected\"" : "" %> value="Yellow">Yellow</option>
    <option <%=("Mauve".equalsIgnoreCase(preview100MatchColor)) ? "selected=\"selected\"" : "" %> value="Mauve">Mauve</option>
    <option <%=("Olive".equalsIgnoreCase(preview100MatchColor)) ? "selected=\"selected\"" : "" %> value="Olive">Olive</option>
    </SELECT></TD>
  </TR>
  <TR>
    <TD>&nbsp;&nbsp;&nbsp;&nbsp;<SPAN class="standardText"><%=lb_previewIceMatchColor%></SPAN></TD>
    <TD><SELECT name="<%=UserParamNames.PREVIEW_ICEMATCH_COLOR%>">
    <option <%=("Black".equalsIgnoreCase(previewIceMatchColor)) ? "selected=\"selected\"" : "" %> value="Black">Black</option>
    <option <%=("White".equalsIgnoreCase(previewIceMatchColor)) ? "selected=\"selected\"" : "" %> value="White">White</option>
    <option <%=("Red".equalsIgnoreCase(previewIceMatchColor)) ? "selected=\"selected\"" : "" %> value="Red">Red</option>
    <option <%=("Green".equalsIgnoreCase(previewIceMatchColor)) ? "selected=\"selected\"" : "" %> value="Green">Green</option>
    <option <%=("Blue".equalsIgnoreCase(previewIceMatchColor)) ? "selected=\"selected\"" : "" %> value="Blue">Blue</option>
    <option <%=("Cyan".equalsIgnoreCase(previewIceMatchColor)) ? "selected=\"selected\"" : "" %> value="Cyan">Cyan</option>
    <option <%=("Magenta".equalsIgnoreCase(previewIceMatchColor)) ? "selected=\"selected\"" : "" %> value="Magenta">Magenta</option>
    <option <%=("Yellow".equalsIgnoreCase(previewIceMatchColor)) ? "selected=\"selected\"" : "" %> value="Yellow">Yellow</option>
    <option <%=("Mauve".equalsIgnoreCase(previewIceMatchColor)) ? "selected=\"selected\"" : "" %> value="Mauve">Mauve</option>
    <option <%=("Olive".equalsIgnoreCase(previewIceMatchColor)) ? "selected=\"selected\"" : "" %> value="Olive">Olive</option>
    </SELECT></TD>
  </TR>
  <TR>
    <TD>&nbsp;&nbsp;&nbsp;&nbsp;<SPAN class="standardText"><%=lb_previewNonMatchColor%></SPAN></TD>
    <TD><SELECT name="<%=UserParamNames.PREVIEW_NONMATCH_COLOR%>">
    <option <%=("Black".equalsIgnoreCase(previewNonMatchColor)) ? "selected=\"selected\"" : "" %> value="Black">Black</option>
    <option <%=("White".equalsIgnoreCase(previewNonMatchColor)) ? "selected=\"selected\"" : "" %> value="White">White</option>
    <option <%=("Red".equalsIgnoreCase(previewNonMatchColor)) ? "selected=\"selected\"" : "" %> value="Red">Red</option>
    <option <%=("Green".equalsIgnoreCase(previewNonMatchColor)) ? "selected=\"selected\"" : "" %> value="Green">Green</option>
    <option <%=("Blue".equalsIgnoreCase(previewNonMatchColor)) ? "selected=\"selected\"" : "" %> value="Blue">Blue</option>
    <option <%=("Cyan".equalsIgnoreCase(previewNonMatchColor)) ? "selected=\"selected\"" : "" %> value="Cyan">Cyan</option>
    <option <%=("Magenta".equalsIgnoreCase(previewNonMatchColor)) ? "selected=\"selected\"" : "" %> value="Magenta">Magenta</option>
    <option <%=("Yellow".equalsIgnoreCase(previewNonMatchColor)) ? "selected=\"selected\"" : "" %> value="Yellow">Yellow</option>
    <option <%=("Mauve".equalsIgnoreCase(previewNonMatchColor)) ? "selected=\"selected\"" : "" %> value="Mauve">Mauve</option>
    <option <%=("Olive".equalsIgnoreCase(previewNonMatchColor)) ? "selected=\"selected\"" : "" %> value="Olive">Olive</option>
    </SELECT></TD>
  </TR>
<% if (!b_reviewMode) { %>
  <TR>
    <TD><SPAN class="standardText"><%=lb_autoUnlockSegments%></SPAN></TD>
    <TD><INPUT type="checkbox" name="autounlock"></TD>
  </TR>
<% } %>
  <TR>
    <TD><SPAN class="standardText"><%=lb_autoSyncSegments%></SPAN></TD>
    <TD><INPUT type="checkbox" name="autosync"></TD>
  </TR>
  <TR>
    <TD VALIGN="top"><SPAN class="standardText"><%=lb_defaultLayout%></SPAN></TD>
    <TD VALIGN="top" class="standardText">
      <INPUT type="radio" name="layout" id="id_layout1">
      <LABEL for="id_layout1"><%=lb_layout_stv%></LABEL><BR>
      <INPUT type="radio" name="layout" id="id_layout2">
      <LABEL for="id_layout2"><%=lb_layout_sth%></LABEL><BR>
      <INPUT type="radio" name="layout" id="id_layout3">
      <LABEL for="id_layout3"><%=lb_layout_s%></LABEL><BR>
      <INPUT type="radio" name="layout" id="id_layout4">
      <LABEL for="id_layout4"><%=lb_layout_t%></LABEL>
    </TD>
  </TR>
  <TR>
    <TD VALIGN="top"><SPAN class="standardText"><%=lb_defaultViewmode%></SPAN></TD>
    <TD VALIGN="top" class="standardText">
      <INPUT type="radio" name="viewmode" id="id_mode1">
      <LABEL for="id_mode1"><%=lb_viewmode_preview%></LABEL><BR>
      <INPUT type="radio" name="viewmode" id="id_mode2">
      <LABEL for="id_mode2"><%=lb_viewmode_text%></LABEL><BR>
      <INPUT type="radio" name="viewmode" id="id_mode3">
      <LABEL for="id_mode3"><%=lb_viewmode_list%></LABEL>
    </TD>
  </TR>
  <TR>
    <TD>
      <SPAN class="standardText"><%=lb_max_segment_number_in_editor%>&nbsp;</SPAN><br/>
      <SPAN class="standardText"><%=lb_segment_number_range_in_editor%>:</SPAN>
    </TD>
    <TD>
      <INPUT type="text" size="20" maxlength="10" id="idMaxSegmentsNum"
          name="<%=UserParamNames.EDITOR_SEGMENTS_MAX_NUM%>" value="<%=maxSegmentsNum%>" />
    </TD>
  </TR>
</TABLE>
<P>
<CENTER>
<INPUT type="button" value="<%=lb_ok%>" name="apply" onclick="javascript:checkForm();"> &nbsp;&nbsp;&nbsp;
<INPUT type="button" onclick="closeThis()" value="<%=lb_cancel%>" name="cancel">
</CENTER>
</FORM>
</BODY>
</HTML>
