<%@ taglib uri="/WEB-INF/tlds/globalsight.tld" prefix="amb" %>
<%@ page
    contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="java.util.*,com.globalsight.util.edit.EditUtil,
            com.globalsight.util.edit.GxmlUtil,
            com.globalsight.util.modules.Modules,
            com.globalsight.config.UserParamNames,
            com.globalsight.everest.permission.Permission,
            com.globalsight.everest.permission.PermissionSet,            
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
<jsp:useBean id="done" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean"/>
<jsp:useBean id="cancel" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean"/>
<%
ResourceBundle bundle = PageHandler.getBundle(session);
PermissionSet perms = (PermissionSet) session.getAttribute(WebAppConstants.PERMISSIONS);

String url_options = done.getPageURL();
String url_cancel    = cancel.getPageURL();

String lb_title = bundle.getString("lb_account_options");
String lb_options = lb_title;
String lb_general_options = bundle.getString("lb_general_options");
String lb_me_options = bundle.getString("lb_main_editor_options");
String lb_se_options = bundle.getString("lb_segment_editor_options");
String lb_report_option = bundle.getString("lb_report_option");

String lb_ok = bundle.getString("lb_done");
String lb_cancel = bundle.getString("lb_cancel");

// Additional notes on the screen
String lb_colorNote = bundle.getString("msg_colors_apply_to_preview");
String lb_numericValue = bundle.getString("lb_numeric_value");

// Parameter descriptions:
String lb_pagename_display = bundle.getString("lb_pagename_display");
String lb_pagename_full = bundle.getString("lb_pagename_full");
String lb_pagename_short = bundle.getString("lb_pagename_short");
String lb_editor_selection = bundle.getString("lb_editor_selection");
String lb_editor_select = bundle.getString("lb_edit_pages_in") + ":";
String lb_editor_same_window =  bundle.getString("lb_inline_editor");
String lb_editor_popup_window = bundle.getString("lb_popup_editor") + " (" + bundle.getString("lb_advanced") + ")";
String lb_autosave = bundle.getString("lb_autosave");
String lb_autowhite = bundle.getString("lb_autowhite");
String lb_abbreviate_report_name = bundle.getString("lb_abbreviate_report_name");
String lb_tmThreshold = bundle.getString("lb_tm_threshold");
String lb_tbThreshold = bundle.getString("lb_tb_threshold");
String lb_linkColor        = bundle.getString("lb_normal_link_color");
String lb_activeLinkColor  = bundle.getString("lb_active_link_color");
String lb_visitedLinkColor = bundle.getString("lb_visited_link_color");
String lb_preview100MatchColor = bundle.getString("lb_Color_100match");
String lb_previewIceMatchColor = bundle.getString("lb_Color_ICEmatch");
String lb_previewNonMatchColor = bundle.getString("lb_Color_Nonmatch");
String lb_autoUnlockSegments = bundle.getString("lb_auto_unlock_segments");
String lb_enableCloseAllComments = bundle.getString("lb_enable_close_all_comments");
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
String lb_default_ptagmode = bundle.getString("lb_default_ptagmode");
String lb_compact = bundle.getString("lb_compact");
String lb_verbose = bundle.getString("lb_verbose");
String lb_max_segment_number_in_editor = bundle.getString("lb_max_segment_number_in_editor");
String lb_segment_number_range_in_editor = bundle.getString("lb_segment_number_range_in_editor");
String msg_invalid_max_segments_num = bundle.getString("msg_max_segment_number_error");

// Parameter values
String pagename = (String)request.getAttribute(UserParamNames.PAGENAME_DISPLAY);
String editor   = (String)request.getAttribute(UserParamNames.EDITOR_SELECTION);
String maxSegmentsNum = (String)request.getAttribute(UserParamNames.EDITOR_SEGMENTS_MAX_NUM);

String override = (String)request.getAttribute(UserParamNames.HYPERLINK_COLOR_OVERRIDE);
String linkColor        = (String)request.getAttribute(UserParamNames.HYPERLINK_COLOR);
String activeLinkColor  = (String)request.getAttribute(UserParamNames.ACTIVE_HYPERLINK_COLOR);
String visitedLinkColor = (String)request.getAttribute(UserParamNames.VISITED_HYPERLINK_COLOR);

String preview100MatchColor = (String)request.getAttribute(UserParamNames.PREVIEW_100MATCH_COLOR);
String previewIceMatchColor = (String)request.getAttribute(UserParamNames.PREVIEW_ICEMATCH_COLOR);
String previewNonMatchColor = (String)request.getAttribute(UserParamNames.PREVIEW_NONMATCH_COLOR);

String autosave = (String)request.getAttribute(UserParamNames.EDITOR_AUTO_SAVE_SEGMENT);
String autosaveChecked = autosave.equals("0") ? "" : "CHECKED";
String autowhite = (String)request.getAttribute(UserParamNames.EDITOR_AUTO_ADJUST_WHITESPACE);
String autowhiteChecked = autowhite.equals("0") ? "" : "CHECKED";
String ptagmode = (String)request.getAttribute(UserParamNames.EDITOR_PTAGMODE);

String abbreReportNname = (String)request.getAttribute(UserParamNames.EDITOR_ABBREVIATE_REPORT_NAME_SEGMENT);
String abbreReportNnameChecked = abbreReportNname.equals("no") ? "" : "CHECKED";

String closeAllComments = (String)request.getAttribute(UserParamNames.EDITOR_SHOW_CLOSEALLCOMMENT);
String closeAllCommentsChecked = closeAllComments.equals("0") ? "" : "CHECKED";

String autoUnlock = (String)request.getAttribute(UserParamNames.EDITOR_AUTO_UNLOCK);
String layout = (String)request.getAttribute(UserParamNames.EDITOR_LAYOUT);
String viewmode = (String)request.getAttribute(UserParamNames.EDITOR_VIEWMODE);

String tmThreshold = (String)request.getAttribute(UserParamNames.TM_MATCHING_THRESHOLD);
String tbThreshold = (String)request.getAttribute(UserParamNames.TB_MATCHING_THRESHOLD);

// CMS option values
String _cmsUsername = (String)request.getAttribute(WebAppConstants.CMS_USER_NAME);;
String _cmsPassword = (String)request.getAttribute(WebAppConstants.CMS_PASSWORD);;

String errorMsg = (String)request.getAttribute(WebAppConstants.USER_PARAMS_ERROR);

boolean b_enableNewEditor = false;
if ("true".equals(request.getAttribute(WebAppConstants.PARAGRAPH_EDITOR)))
{
   b_enableNewEditor = true;
}
%>
<HTML>
<!-- This JSP is: /envoy/tasks/accountOptions.jsp -->
<HEAD>
<TITLE><%=lb_title%></TITLE>
<style type="text/css">
.header2{
	width:500px;
}
</style>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<%@ include file="/envoy/wizards/guidesJavascript.jspIncl" %>
<%@ include file="/envoy/common/warning.jspIncl" %>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/formValidation.js"></SCRIPT>
<SCRIPT LANGUAGE="Javascript" SRC="envoy/terminology/viewer/error.js"></SCRIPT>
<SCRIPT LANGUAGE="JavaScript">
var needWarning = false;
var objectName = "<%= bundle.getString("lb_account_information_my") %>";
var guideNode = "myAccount";
var helpFile = "<%=bundle.getString("help_my_account_options")%>";
</SCRIPT>
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
    var canSubmit = false;
    showHourglass();

    var field;

    if (form.pagename[0].checked)
    {
      form.<%=UserParamNames.PAGENAME_DISPLAY%>.value = "<%=UserParamNames.PAGENAME_DISPLAY_FULL%>";
    }
    else if (form.pagename[1].checked)
    {
      form.<%=UserParamNames.PAGENAME_DISPLAY%>.value = "<%=UserParamNames.PAGENAME_DISPLAY_SHORT%>";
    }
    
<% /*begin activities edit perm check*/
   boolean b_activitiesEdit = perms.getPermissionFor(Permission.ACTIVITIES_FILES_EDIT);
   if (b_activitiesEdit) { %>

<% if (b_enableNewEditor) { %>
    if (form.editor[0].checked)
    {
      form.<%=UserParamNames.EDITOR_SELECTION%>.value = "<%=UserParamNames.EDITOR_INLINE%>";
    }
    else if (form.editor[1].checked)
    {
      form.<%=UserParamNames.EDITOR_SELECTION%>.value = "<%=UserParamNames.EDITOR_POPUP%>";
    }

    var maxSegNum = document.getElementById('idMaxSegmentsNum').value;
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

    if (!canSubmit)
    {
    	alert("<%=msg_invalid_max_segments_num%>");
        form.apply.disabled = false;
        form.cancel.disabled = false;
        idBody.style.cursor = "default";
    	return false;
    }
<%}%>

    field = form.autounlock;
    if (field.checked)
    {
      form.<%=UserParamNames.EDITOR_AUTO_UNLOCK%>.value = "1";
    }
    else
    {
      form.<%=UserParamNames.EDITOR_AUTO_UNLOCK%>.value = "0";
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

    field = form.autosave;
    if (field.checked)
    {
      form.<%=UserParamNames.EDITOR_AUTO_SAVE_SEGMENT%>.value = "1";
    }
    else
    {
      form.<%=UserParamNames.EDITOR_AUTO_SAVE_SEGMENT%>.value = "0";
    }
    
    field = form.closeAllComments;
    if (field.checked)
    {
      form.<%=UserParamNames.EDITOR_SHOW_CLOSEALLCOMMENT%>.value = "1";
    }
    else
    {
      form.<%=UserParamNames.EDITOR_SHOW_CLOSEALLCOMMENT%>.value = "0";
    }

    if (form.ptagmode[0].checked)
    {
      form.<%=UserParamNames.EDITOR_PTAGMODE%>.value = "<%=EditorConstants.PTAGS_COMPACT%>";
    }
    else 
    {
      form.<%=UserParamNames.EDITOR_PTAGMODE%>.value = "<%=EditorConstants.PTAGS_VERBOSE%>";
    }
	<%--field = form.abbreReportNname;
	if (field.checked)
	{--%>
	  form.<%=UserParamNames.EDITOR_ABBREVIATE_REPORT_NAME_SEGMENT%>.value = "yes";
	  <%--}
	else
	{
	  form.<%=UserParamNames.EDITOR_ABBREVIATE_REPORT_NAME_SEGMENT%>.value = "no";
	}--%>
<%--
    field = form.<%=UserParamNames.TM_MATCHING_THRESHOLD%>;
    if (!isRange(field, 1, 100))
    {
      alert("<%=lb_numericValue%>");
      field.focus();
      return false;
    }

    field = form.<%=UserParamNames.TB_MATCHING_THRESHOLD%>;
    if (!isRange(field, 1, 100))
    {
      alert("<%=lb_numericValue%>");
      field.focus();
      return false;
    }
--%>
<% } /*end activities edit perm check*/ %>
    if (canSubmit)
    {
      form.submit();
    }
    
    return true;
}

function initForm()
{
  if ("<%=pagename%>" == "<%=UserParamNames.PAGENAME_DISPLAY_SHORT%>")
    form.pagename[1].checked = true;
  else
    form.pagename[0].checked = true;

<% /*begin activities edit perm check*/
   if (b_activitiesEdit) { %>
<% if (b_enableNewEditor) { %>
  if ("<%=editor%>" == "<%=UserParamNames.EDITOR_POPUP%>")
    form.editor[1].checked = true;
  else
    form.editor[0].checked = true;
<%}%>

  if ("<%=autoUnlock%>" == "1") form.autounlock.checked = true;


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

  if ("<%=ptagmode%>" == "<%=EditorConstants.PTAGS_COMPACT%>")
    form.ptagmode[0].checked = true;
  else
    form.ptagmode[1].checked = true;
<% } /*end activies edit perm check*/%>
}

function init()
{
    initForm();

    window.focus();

    loadGuides();

    if (errorMessage != "")
    {
      ShowError(errorMessage);
    }
}
</SCRIPT>
</HEAD>
<BODY LEFTMARGIN="0" RIGHTMARGIN="0" TOPMARGIN="0" MARGINWIDTH="0"
 MARGINHEIGHT="0" onload="init();" id="idBody">
<%@ include file="/envoy/common/header.jspIncl" %>
<%@ include file="/envoy/common/navigation.jspIncl" %>
<%@ include file="/envoy/wizards/guides.jspIncl" %>

<DIV ID="contentLayer"
 STYLE=" POSITION: ABSOLUTE; Z-INDEX: 9; TOP: 108px; LEFT: 20px; RIGHT: 20px;">

<P CLASS="mainHeading"><%=lb_options%></P>

<FORM ACTION="<%=url_options%>" METHOD="post" name="form" onsubmit="">
<INPUT type="hidden" name="__save" VALUE="yes">
<INPUT type="hidden" name="<%=UserParamNames.PAGENAME_DISPLAY%>" value="<%=pagename%>">
<% if (b_enableNewEditor) { %>
<INPUT type="hidden" name="<%=UserParamNames.EDITOR_SELECTION%>" value="<%=editor%>">
<%}%>
<INPUT type="hidden" name="<%=UserParamNames.EDITOR_AUTO_UNLOCK%>" value="mlah!">
<INPUT type="hidden" name="<%=UserParamNames.EDITOR_AUTO_ADJUST_WHITESPACE%>" value="mlah!">
<INPUT type="hidden" name="<%=UserParamNames.EDITOR_LAYOUT%>" value="<%=layout%>">
<INPUT type="hidden" name="<%=UserParamNames.EDITOR_VIEWMODE%>" value="<%=viewmode%>">
<INPUT type="hidden" name="<%=UserParamNames.EDITOR_AUTO_SAVE_SEGMENT%>" value="mlah!">
<INPUT type="hidden" name="<%=UserParamNames.EDITOR_PTAGMODE%>" value="<%=ptagmode%>">
<INPUT type="hidden" name="<%=UserParamNames.EDITOR_SHOW_CLOSEALLCOMMENT%>" value="mlah!">
<INPUT type="hidden" name="<%=UserParamNames.EDITOR_ABBREVIATE_REPORT_NAME_SEGMENT%>" value="mlah!">

<P class="header2">&nbsp;<%=lb_general_options%></P>
<TABLE>  
  <TR>
    <TD VALIGN="top"><SPAN class="standardText"><%=lb_pagename_display%></SPAN></TD>
    <TD VALIGN="top" class="standardText">
      <INPUT type="radio" name="pagename" id="idPagename1">
      <label for="idPagename1"><%=lb_pagename_full%></LABEL></INPUT><BR>
      <INPUT type="radio" name="pagename" id="idPagename2">
      <label for="idPagename2"><%=lb_pagename_short%></LABEL></INPUT>
    </TD>
  </TR>
</TABLE>

<%-- CMS Options --%>
<% if (Modules.isCmsAdapterInstalled()) {%>
<amb:permission name="<%=Permission.CONTENT_MANAGER%>" >
<P class="header2">&nbsp;<%=bundle.getString("lb_cms_options")%></P>
<TABLE>
  <TR>
    <TD><SPAN class="standardText"><%=bundle.getString("lb_cms_username")%>:</SPAN></TD>
    <TD><INPUT type="text" size="30" name="<%= WebAppConstants.CMS_USER_NAME %>" <%
      if (_cmsUsername != null)
      {
         %>value="<%=_cmsUsername%>"<%
      }
      %>></TD>
  </TR>
  <TR>
    <TD><SPAN class="standardText"><%=bundle.getString("lb_cms_password")%>:</SPAN></TD>
    <TD><INPUT type="password" size="30" name="<%= WebAppConstants.CMS_PASSWORD %>" <%
      if (_cmsUsername != null)
      {
         %>value="<%=_cmsPassword%>"<%
      }
      %>></TD>      
  </TR> 
  
</TABLE>
</amb:permission>
<% }%>

<% if (b_enableNewEditor) { %>
<amb:permission name="<%=Permission.ACTIVITIES_FILES_EDIT%>" >
<P class="header2">&nbsp;<%=lb_editor_selection%></P>
<TABLE>
  <TR>
    <TD VALIGN="top"><SPAN class="standardText"><%=lb_editor_select%></SPAN></TD>
    <TD VALIGN="top" class="standardText">
      <INPUT type="radio" name="editor" id="idEditor1">
      <label for="idEditor1"><%=lb_editor_same_window%></LABEL></INPUT><BR>
      <INPUT type="radio" name="editor" id="idEditor2">
      <label for="idEditor2"><%=lb_editor_popup_window%></LABEL></INPUT>
    </TD>
  </TR>

  <TR>
    <TD>
      <SPAN class="standardText"><%=lb_max_segment_number_in_editor%>&nbsp;</SPAN><br/>
      <SPAN class="standardText"><%=lb_segment_number_range_in_editor%>:</SPAN></TD>
    <TD>
      <INPUT type="text" size="20" maxlength="10" id="idMaxSegmentsNum"
          name="<%=UserParamNames.EDITOR_SEGMENTS_MAX_NUM%>" value="<%=maxSegmentsNum%>" />
    </TD>
  </TR>

</TABLE>
</amb:permission>
<%}%>

<%-- Main Editor Options --%>
<amb:permission name="<%=Permission.ACTIVITIES_FILES_EDIT%>" >
<P class="header2">&nbsp;<%=lb_me_options%></P>
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
  <TR>
    <TD>
      <SPAN class="standardText"><%=lb_autoUnlockSegments%></SPAN>
    </TD>
    <TD><INPUT type="checkbox" name="autounlock" id="idAutoUnlock"></TD>
  </TR>
  <TR>
    <TD>
      <SPAN class="standardText"><%=lb_autoSyncSegments%></SPAN>
    </TD>
    <TD><INPUT type="checkbox" name="autosync" id="idAutoSync"></TD>
  </TR>
  <TR>
    <TD VALIGN="top"><SPAN class="standardText"><%=lb_defaultLayout%></SPAN></TD>
    <TD VALIGN="top" class="standardText">
      <INPUT type="radio" name="layout" id="idLayout1">
      <label for="idLayout1"><%=lb_layout_stv%></label></INPUT><BR>
      <INPUT type="radio" name="layout" id="idLayout2">
      <label for="idLayout2"><%=lb_layout_sth%></label></INPUT><BR>
      <INPUT type="radio" name="layout" id="idLayout3">
      <label for="idLayout3"><%=lb_layout_s%></label></INPUT><BR>
      <INPUT type="radio" name="layout" id="idLayout4">
      <label for="idLayout4"><%=lb_layout_t%></label></INPUT>
    </TD>
  </TR>
  <TR>
    <TD VALIGN="top"><SPAN class="standardText"><%=lb_defaultViewmode%></SPAN></TD>
    <TD VALIGN="top" class="standardText">
      <INPUT type="radio" name="viewmode" id="idViewmode1">
      <label for="idViewmode1"><%=lb_viewmode_preview%></label></INPUT><BR>
      <INPUT type="radio" name="viewmode" id="idViewmode2">
      <label for="idViewmode2"><%=lb_viewmode_text%></label></INPUT><BR>
      <INPUT type="radio" name="viewmode" id="idViewmode3">
      <label for="idViewmode3"><%=lb_viewmode_list%></label></INPUT>
    </TD>
  </TR>
  <TR>
    <TD>
      <SPAN class="standardText"><%=lb_enableCloseAllComments%></SPAN>
    </TD>
    <TD><INPUT type="checkbox" name="closeAllComments" <%=closeAllCommentsChecked%> ></TD>
  </TR>
</TABLE>
</amb:permission>
<%-- Segment Editor Options --%>
<amb:permission name="<%=Permission.ACTIVITIES_FILES_EDIT%>" >
<P class="header2">&nbsp;<%=lb_se_options%></P>
<TABLE>
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
      <INPUT type="radio" name="ptagmode" id="idPtagMode1">
      <label for="idPtagMode1"><%=lb_compact%></label></INPUT><BR>
      <INPUT type="radio" name="ptagmode" id="idPtagMode2">
      <label for="idPtagMode2"><%=lb_verbose%></label></INPUT>
    </TD>
  </TR>
  
  <%--
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
  <TR>
    <TD colspan=2>&nbsp;</TD>
  </TR>
  --%>
</TABLE>
</amb:permission>
<%--Report Options --%>
<!--  
<amb:permission name="<%=Permission.ACTIVITIES_FILES_EDIT%>" >
	<P class="header2">&nbsp;<%=lb_report_option%></P>
	<TABLE>
		<TR>
			<TD><SPAN class="standardText"><%=lb_abbreviate_report_name %></SPAN></TD>
			<TD><INPUT type="checkbox" name="abbreReportNname" <%=abbreReportNnameChecked%> ></TD>
		</TR>
		<TR><TD colspan="2">&nbsp;</TD></TR>
		<TR><TD></TD></TR>
	</TABLE>
</amb:permission>
-->
      <INPUT type="button" name="cancel" value="<%=lb_cancel%>"
      onclick="location.replace('<%=url_cancel%>')">
&nbsp;&nbsp;
      <INPUT type="button" name="apply" value="<%=lb_ok%>" onclick="javascript:checkForm();"> 
</FORM>
</DIV>
</BODY>
</HTML>

