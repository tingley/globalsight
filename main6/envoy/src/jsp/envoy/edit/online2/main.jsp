<%@ page
    contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="java.util.*,com.globalsight.everest.webapp.javabean.NavigationBean,
            com.globalsight.everest.webapp.pagehandler.PageHandler,
            com.globalsight.everest.webapp.pagehandler.edit.online.EditorConstants,
            com.globalsight.everest.webapp.pagehandler.edit.online.EditorState,
            com.globalsight.everest.edit.SynchronizationStatus,
            com.globalsight.util.edit.EditUtil,
            com.globalsight.everest.taskmanager.Task,
            com.globalsight.everest.webapp.pagehandler.tasks.TaskHelper,
            com.globalsight.everest.foundation.User,
            com.globalsight.everest.edit.online.PaginateInfo,
            com.globalsight.everest.servlet.util.SessionManager,
            com.globalsight.everest.webapp.WebAppConstants,
            java.util.Locale,
            java.util.ResourceBundle,
            com.globalsight.util.edit.SegmentUtil2"
    session="true"
%>
<jsp:useBean id="save" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean"/>
<jsp:useBean id="close" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean"/>
<jsp:useBean id="pageInfo" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean"/>
<jsp:useBean id="tminfo" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean"/>
<jsp:useBean id="options" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean"/>
<jsp:useBean id="concordance" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean"/>
<jsp:useBean id="splitmerge" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean"/>
<jsp:useBean id="refresh" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean"/>
<jsp:useBean id="search" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean"/>
  
<%@ include file="/envoy/common/installedModules.jspIncl" %>
<%
ResourceBundle bundle = PageHandler.getBundle(session);
Locale uiLocale = (Locale)session.getAttribute(WebAppConstants.UILOCALE);

String lb_select_ptag = bundle.getString("lb_select_ptag");
String lb_select_ptag1 = bundle.getString("lb_select_ptag1");
String lb_no_ptags_in_segment = bundle.getString("lb_no_ptags_in_segment");
String lb_close = bundle.getString("lb_close");

SessionManager sessionMgr = (SessionManager)session.getAttribute(
  WebAppConstants.SESSION_MANAGER);
EditorState state =
  (EditorState)sessionMgr.getAttribute(WebAppConstants.EDITORSTATE);

String str_userId =
  ((User)sessionMgr.getAttribute(WebAppConstants.USER)).getUserId();

String url_save     = save.getPageURL();
String url_pageInfo = pageInfo.getPageURL();
String url_tmInfo   = tminfo.getPageURL();
String url_options  = options.getPageURL();
String url_concordance = concordance.getPageURL();
String url_splitmerge  = splitmerge.getPageURL();
String url_refresh = refresh.getPageURL();
String url_search = search.getPageURL();

String str_pageHtml = state.getTargetPageHtml();
String str_dataType = state.getPageFormat();
String str_pageName = state.getSimpleSourcePageName();

boolean b_readonly = state.isReadOnly();

String lb_title = bundle.getString("lb_inline_editor");
lb_title = lb_title + " - " + str_pageName;
String lb_loading = bundle.getString("msg_loading");
String lb_saving  = bundle.getString("msg_saving");
String lb_concordance = bundle.getString("lb_concordance");
String lb_compactTags = bundle.getString("lb_editor_compact_tags");
String lb_verboseTags = bundle.getString("lb_editor_verbose_tags");
String lb_fileNavigation = bundle.getString("lb_fileNavigation");
String lb_pageNavigation = bundle.getString("lb_pageNavigation");

//file navigation (default unavailable)
String lb_prevFile = "<IMG SRC='/globalsight/images/editorPreviousPagex.gif' BORDER=0 HSPACE=1 VSPACE=2>";
String lb_nextFile = "<IMG SRC='/globalsight/images/editorNextPagex.gif' BORDER=0 HSPACE=1 VSPACE=2>";
//page navigation (default unavailable)
String lb_prevPage = "<IMG SRC='/globalsight/images/editorPreviousPagex.gif' BORDER=0 HSPACE=1 VSPACE=2>";
String lb_nextPage = "<IMG SRC='/globalsight/images/editorNextPagex.gif' BORDER=0 HSPACE=1 VSPACE=2>";

PaginateInfo pi = state.getPaginateInfo();

// Todo: re-enable length check for target translations
int i_dbSegmentLen = 0;
int i_maxSegmentLen = 0; // no limit on CLOB
boolean b_adjustWS = state.getOptions().getAutoAdjustWhitespace();
boolean b_autoUnlock = state.getOptions().getAutoUnlock();
boolean b_hilitePtags = state.getOptions().getHilitePtags();
boolean b_showMt = state.getOptions().getShowMt();
boolean b_canShowMt = state.canShowMt();
boolean b_canEditAll = state.canEditAll();

String str_uiLocale;
if (uiLocale == null)
{
    str_uiLocale = "en_US";
}
else
{
    // must return en_US with underscore
    str_uiLocale = uiLocale.toString();
}

String str_sourceLocale = state.getSourceLocale().toString();
String str_targetLocale = state.getTargetLocale().toString();
String str_sourceLocale_dis = (uiLocale == null)? str_sourceLocale : state.getSourceLocale().getDisplayName(uiLocale);
String str_targetLocale_dis = (uiLocale == null)? str_targetLocale : state.getTargetLocale().getDisplayName(uiLocale);
String str_ptags = state.getPTagFormat();
boolean b_ptagsVerbose =
  str_ptags.equals(EditorConstants.PTAGS_VERBOSE);
  
boolean b_sourceRtl = EditUtil.isRTLLocale(state.getSourceLocale());
boolean b_targetRtl = EditUtil.isRTLLocale(state.getTargetLocale());

String currenttuv = (String)request.getAttribute("currenttuv");

// Close goes back to task details
String taskId = (String)request.getParameter(WebAppConstants.TASK_ID);
// Get task info
Task task = (Task)TaskHelper.retrieveObject(session, WebAppConstants.WORK_OBJECT);
int task_state = task.getState();

String closeUrl = close.getPageURL() +
   "&" + WebAppConstants.TASK_ACTION +
   "=" + WebAppConstants.TASK_ACTION_RETRIEVE +
   "&" + WebAppConstants.TASK_STATE +
   "=" + task_state +
   "&" + WebAppConstants.TASK_ID +
   "=" + taskId;

SynchronizationStatus oldStatus = state.getOldSynchronizationStatus();
SynchronizationStatus newStatus = state.getNewSynchronizationStatus();
String syncMessage = null;
boolean syncClose = false;

if (newStatus != null)
{
    String status = newStatus.getStatus();
    if (status.equals(SynchronizationStatus.GXMLUPDATE_STARTED) ||
        status.equals(SynchronizationStatus.GXMLUPDATE_FINISHED))
    {
      syncMessage = bundle.getString("jsmsg_editor_srcpage_is_being_edited");
      syncClose = true;
    }
}
%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<html>
<!-- This is envoy\edit\online2\main.jsp -->
<head>
<title><%=lb_title%></title>
<script src="/globalsight/envoy/edit/online2/applet.js"></script>
<script src="/globalsight/envoy/edit/online2/stringbuilder.js"></script>
<script src="/globalsight/envoy/edit/online2/richedit.js"></script>
<script src="/globalsight/includes/machinetranslation.js"></script>
<script src="/globalsight/includes/spellcheck.js"></script>
<script src="/globalsight/spellchecker/jsp/spellcheck.js"></script>
<script src="/globalsight/xdespellchecker/noapplet/SpellCheckNoApplet.js"></script>
<script language="JavaScript1.5" src="/globalsight/includes/ieemu.js"></script>
<script src="/globalsight/includes/coolbutton2.js"></script>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/dnd/DragAndDrop.js"></SCRIPT>
<link type="text/css" rel="StyleSheet" href="/globalsight/includes/coolbutton2.css">

<link type="text/css" rel="StyleSheet" id="cssEditor" 
  href="/globalsight/envoy/edit/online2/editor.css">
<link type="text/css" rel="StyleSheet" id="cssEditorTouched" disabled
  href="/globalsight/envoy/edit/online2/editorTouched.css">
<link type="text/css" rel="StyleSheet" id="cssEditorTranslated" disabled
  href="/globalsight/envoy/edit/online2/editorTranslated.css">
<link type="text/css" rel="StyleSheet" id="cssEditorUntranslated" disabled
  href="/globalsight/envoy/edit/online2/editorUntranslated.css">

<link type="text/css" rel="StyleSheet"
  href="/globalsight/includes/menu/skins/winclassic.css">
<link type="text/css" rel="StyleSheet" id="cssPtag"
  href="/globalsight/envoy/edit/online2/ptag.css">
<script src="/globalsight/includes/menu/js/poslib.js"></script>
<script src="/globalsight/includes/menu/js/scrollbutton.js"></script>
<script src="/globalsight/includes/menu/js/menu4.js"></script>
<script src="/globalsight/includes/menu/js/xmenu.js"></script>
<script SRC="/globalsight/includes/filter/StringBuffer.js"></script>
<style>
.menu-button .middle { width: 100%; }
.menu-bar {
            /* the border causes the vertical line between menu and "close" */
            border-right: 2px groove; width: 53px;
            display: inline; background: #0C1476;
          }
.menu-bar .menu-button {
           background: #0C1476; color: white;
}

{ font-family: Arial; }

.header { font-family: Arial, Helvetica, sans-serif;
          font-weight: bold;
          font-size: 10pt;
          color: white;
          background-color: #0C1476;
        }
.toolBar { font-size: 10pt; background-color: ButtonFace; }
.help   {
          font-size: 8pt;
          cursor: hand; cursor: pointer;
          float: right;
        }

#idSegments P { display: inline;
                font-family: Arial Unicode MS, Arial, sans-serif;
              }

#idEditor1      {
                  background-color: #CCFFFF;
                  color: black;
                  width: 100%;
                  margin: 2px;
                  border: 1px solid black;
                  font-family: Courier New;
                  font-size: 16px; /*font-size: x-small;*/
                  word-wrap: break-word;
                  overflow: visible;
                }
                
                

/* <% if (b_readonly) { %> */
#idBody {
        background-image: url(/globalsight/images/read-only.gif);
        background-repeat: no-repeat;
        background-attachment: fixed;
        background-position: center center;
        }
/* <% } %> */

</style>

<SCRIPT type="text/javascript">
var isIE = window.navigator.userAgent.indexOf("MSIE")>0;
var isFirefox = window.navigator.userAgent.indexOf("Firefox")>0;

//Added for firefox issue: image filter on Menu Bar.
var imgIdArr,imgSrcArr,imgGraySrcArr;
var graySub = "pic/gray";

// segment that has "focus", i.e. was opened last.
var g_current = null;
// segment that is being edited (the P object; null if none)
var g_target = null;
var g_sourceGxml = null;
var g_targetGxml = null;
var g_sourceHTML = null;
var g_targetHTML = null;
var g_preserveWhitespace = null;
var g_dataFormat = "<%=state.getPageFormat() %>";
var g_uiLocale = "<%=str_uiLocale%>";
var g_userId = "<%=str_userId%>";
var g_sourceLocale = "<%=str_sourceLocale%>";
var g_targetLocale = "<%=str_targetLocale%>";
var g_sourceRtl = eval("<%=b_sourceRtl%>");
var g_targetRtl = eval("<%=b_targetRtl%>");
var g_defaultDataType = "<%=str_dataType%>";
var g_datatype;
var g_defaultItemType = "text";
var g_itemtype;
var db_segment_len  = parseInt("<%=i_dbSegmentLen%>");
var max_segment_len = parseInt("<%=i_maxSegmentLen%>");
var db_encoding = "UTF8";
var gsa_encoding = "UTF8";
var g_canEditAll = eval("<%=b_canEditAll%>");
var g_adjustWS = eval("<%=b_adjustWS%>");
var g_autoUnlock = eval("<%=b_autoUnlock%>");
var g_readonly = eval("<%=b_readonly%>");
var g_hilitePtags = eval("<%=b_hilitePtags%>");

var o_textbox = null;
var w_tmwin = null;
var w_scwin = null;
var w_pageinfo = null;
var w_options = null;
var w_concordance = null;
var w_progress = null;
var w_search = null;

var g_canShowMt = eval("<%=b_canShowMt%>");
var g_showMt = eval("<%=b_showMt%>");
var g_canUseMt = false;
var g_MT = new MT_Parameters();
var g_SC_GSA = new SC_GSA_Parameters();
var g_SC_XDE = new SC_XDE_Parameters();
var g_canSpellcheck = true;

var g_syncMessage =
  "<%=syncMessage != null ? EditUtil.toJavascript(syncMessage) : ""%>";
var g_syncClose = eval("<%=syncClose%>");

// TUV to open after split/merge refresh
var openOnLoad = "<%=currenttuv%>";

var MERGE_START = "start";
var MERGE_MIDDLE = "middle";
var MERGE_END = "end";

var helpFile = "<%=bundle.getString("help_paragraph_editor")%>";
var verbose = "<%=str_ptags%>";
var g_ptagsVerbose = <%=b_ptagsVerbose%>;
var isMF = navigator.userAgent.indexOf("Firefox") != -1;

// Menus

Menu.prototype.cssFile = "/globalsight/includes/menu/skins/winclassic.css";
Menu.prototype.mouseHoverDisabled = false;
Menu.prototype.showTimeout = 5;
Menu.prototype.closeTimeout = 5;
MenuButton.prototype.subMenuDirection = "vertical";
// Menu.keyboardAccelKey = -1;
// Menu.keyboardAccelProperty = "";

// Main Menu
var actionMenu = new Menu();
actionMenu.add(tmp = new MenuItem("<%=bundle.getString("lb_unlock_segments")%>", unlockSegments));
tmp.mnemonic = "u";
<% if (!b_canEditAll) { %>
tmp.disabled = true;
<% } %>
actionMenu.add(tmp = new MenuItem("<%=bundle.getString("lb_search") %>...", searchByUserSid));
tmp.mnemonic = "s";
actionMenu.add(tmp = new MenuItem("<%=bundle.getString("lb_progress") %>...", showProgressWindow));
tmp.mnemonic = "h";
<% if (b_corpus) { %>
actionMenu.add(tmp = new MenuItem("<%=bundle.getString("lb_concordance") %>...", doConcordance));
tmp.mnemonic = "c";
<% } %>
actionMenu.add(tmp = new MenuItem("<%=bundle.getString("lb_page_info") %>...", showPageInfo));
tmp.mnemonic = "p";
actionMenu.add(tmp = new MenuItem("<%=bundle.getString("lb_options") %>...", showOptions));
tmp.mnemonic = "o";

var menuBar = new MenuBar();
menuBar.add(tmp = new MenuButton("<%=bundle.getString("lb_actions") %>", actionMenu));

// End Menus

function debug(s)
{
  alert(s);
}

function checkShutdown(interval)
{
  ShutdownForm.submit();
  setTimeout("checkShutdown(" + interval + ")", interval);
}

var g_lastKeyHome = false;

function cancelEvent()
{
  var event = getEvent();
  event.returnValue = false;
  event.cancelBubble = true;
  return false;
}

function doKeyPress()
{
  var event = getEvent();
  var key;
  if (document.recalc)
  {
	  key = event.keyCode;
  }
  else
	{
	  key = event.charCode;
	}

  if (event.ctrlKey && !event.altKey)
  {
    if (key == 83) // Ctrl-S
    {
      return cancelEvent();
    }
  }

  if (!event.ctrlKey && event.altKey)
  {
    if (key == 36) // Alt-Home (cannot cancel)
    {
      g_lastKeyHome = true;
      return cancelEvent();
    }
  }
}

function fnIsSelection()
{
  var el = document.getElementById("idEditor2");
  if(el==null) return false;
  var selStr = document.getElementById(el.id).contentWindow.getSelection().toString();
  if(selStr.length>0)
	  return true;
  else
	  return false;
}

function doKeyUp()
{
  g_lastKeyHome = false;
}

function fnGetKey(event)
{
  var tempKEY;
  if (document.recalc)
  {
	  tempKEY = event.keyCode;
  } else {
	  //key = event.charCode;
	  
	  //Fix for GBS-1017
	  tempKEY = event.keyCode;
  }
  return tempKEY;
}

function doKeyDown()
{
  var event = getEvent();
  var key = fnGetKey(event);
  // window.status = String(key);

  if (key == 27) // "ESC"
  {
      Close();
      return cancelEvent();
  }

  // CONTROL KEYS
  if (event.ctrlKey && !event.altKey)
  {
    // Shortcut for text in frame
    var el = document.getElementById("idEditor2");
	if (key == 66 && fnIsSelection()) // "B"
	{
      //makeBold();
      if (el.formattingKeys) el.setBold();
	  return cancelEvent();
	}
    else if (key == 73 && fnIsSelection()) // "I":italic
	{
      //makeItalic();
      if (el.formattingKeys) el.setItalic();
	  return cancelEvent();
	}
	else if (key == 85 && fnIsSelection()) // "U"
	{
	  //makeUnderline();
	  if (el.formattingKeys) el.setUnderline();
	  return cancelEvent();
	}
	
	if (key == 73) // Ctrl-I
    {
      idButTm.setValue(!idButTm.getValue());
      showTmWindow();
      return cancelEvent();
    }
    else if (key == 83) // Ctrl-S
    {
      save();
      return cancelEvent();
    }
  }

  // ALT KEYS
  else if (!event.ctrlKey && event.altKey)
  {
    if (key == 36) // Alt-Home (cannot cancel only warn)
    {
      g_lastKeyHome = true;
      return cancelEvent();
    }
    else if (key == 78) // Alt-N
    {
      showNextMatch();
      return cancelEvent();
    }
    else if (key == 84) // Alt-T
    {
      idButTm.setValue(!idButTm.getValue());
      showTmWindow();
      return cancelEvent();
    }
    else if (key == 46) // Alt-Del
    {
      restoreSource();
      return cancelEvent();
    }
    else if (key == 219) // Alt-[
    {
      openNext(true);
      return cancelEvent();
    }
    else if (key == 221) // Alt-]
    {
      save();
      return cancelEvent();
    }
    else if (key == 220) // Alt-\
    {
      if (save()) openNext(true);
      return cancelEvent();
    }
  }

  // CTRL-ALT KEYS
  else if (event.ctrlKey && event.altKey)
  {
    if (key == 36) // Alt-Ctrl-Home
    {
      openNext(true);
      return cancelEvent();
    }
    else if (key == 35) // Alt-Ctrl-End
    {
      save();
      return cancelEvent();
    }
    else if (key == 107) // Alt-Ctrl-Num+
    {
      if (save()) openNext(true);
      return cancelEvent();
    }
    else if (key == 33) // Alt-Ctrl-PgUp
    {
      splitSegments();
      return cancelEvent();
    }
    else if (key == 34) // Alt-Ctrl-PgDn
    {
      mergeSegments();
      return cancelEvent();
    }
  }
}

// OPEN-GET
function edit(o) {  openGet(o, true); }
function forceEdit(o) { Close(); openGet(o, true); }
function openGet(o, p_showTarget)
{
  if (g_target) return;
  if (g_readonly)
  {
    alert("<%=bundle.getString("jsmsg_editor_read_only_mode") %>");
    return;
  }
  if (isLocked(o))
  {
    if (g_canEditAll)
    {
      alert("<%=bundle.getString("jsmsg_editor_segment_unlock_to_edit") %>");
    }
    else
    {
      alert("<%=bundle.getString("jsmsg_editor_segment_locked") %>");
    }

    return;
  }
  var height = o.style.posHeight;

  g_current = g_target = o;

  g_preserveWhitespace = (o.getAttribute("preserveWhitespace") && "yes" == o.getAttribute("preserveWhitespace"));
  g_sourceGxml = o.getAttribute("source");
  g_targetGxml = o.getAttribute("target");
  g_datatype = o.getAttribute("datatype") ? o.getAttribute("datatype") : g_defaultDataType;
  g_itemtype = o.getAttribute("itemtype") ? o.getAttribute("itemtype") : g_defaultItemType;
  //g_targetHTML = GetTargetDisplayHtml(g_targetGxml, g_datatype);
  g_targetHTML = InitDisplayHtml(g_targetGxml, g_datatype, g_ptagsVerbose, g_preserveWhitespace, g_dataFormat);
  //g_sourceHTML = GetSourceDisplayHtml(g_sourceGxml, g_datatype);
  g_sourceHTML = InitDisplayHtml(g_sourceGxml, g_datatype, g_ptagsVerbose, g_preserveWhitespace, g_dataFormat);
  var src = "<div id=idEditor1 dir=" + (g_sourceRtl ? "rtl" : "ltr") + "></div>";
  var trg = "<iframe frameborder='1' id='idEditor2' name='idEditor2' src='about:blank' class='richEdit' style='width: 100%; height: 100%;' align='center' usebr='true' onblur='return false' oneditinit='initRichtext();' value='' dir=" + (g_targetRtl ? "rtl" : "ltr") + "></iframe>";

  var tmp = "<table width='100%'>" +
  "<tr><td width='100%' valign=top>" + src + "</td></tr>" +
  "<tr><td width='100%' valign=top>" + trg + "</td></tr></table>";

  o.innerHTML = tmp;
  o.oldClassName = o.className;
  o.className = '';
  tmp = null;
  initAllRichEdits();
  document.getElementById("idEditor1").innerHTML = g_sourceHTML;

//  o_textbox = document.all.idEditor2;
   o_textbox = document.getElementById("idEditor2");
   
  if (p_showTarget)
  {
    o_textbox.setHTML(g_targetHTML);
  }
  else
  {
    o_textbox.setHTML(g_sourceHTML);
  }
  o_textbox.enableFormattingKeys(true);
  o_textbox.setVerbosePTags(g_ptagsVerbose);
  // resize text box so entire translation is visible.
  // alert(o_textbox.style.pixelHeight + " " + o_textbox.scrollHeight);
  // o_textbox.style.pixelHeight = o_textbox.style.pixelHeight * 2;
  //o_textbox.scrollIntoView(false);
  o_textbox.focus();
 
  HilitePtags(g_hilitePtags);
  setButtonState();

  showMatches();

  if(o_textbox.contentWindow.document.addEventListener)
  {
     o_textbox.contentWindow.document.addEventListener("keydown", function(){frameDoKeyDown();}, false);
  }

  //Added for firefox issue: image filter on Menu Bar.
  fnImageChangeWrapper();
}

function frameDoKeyDown()
{
	var event = getEvent();
	var key = fnGetKey(event);

	if (key == 27) // "ESC"
	{
	    Close();
	    return cancelEvent();
	}

	if(event.ctrlKey || event.altKey)
	{
		parent.focus();
	    doKeyDown();
	}
}

function openNext(p_showTarget)
{
  if (!g_current)
  {
    // look for the first
    g_current = idSegments.firstChild;
  }
  else
  {
    // look for the next
    g_current = g_current.nextSibling;
  }

  // check they can be edited and are not locked.
  while (g_current &&
    !(canEdit(g_current) && !isLocked(g_current) && !isMergeMiddleEnd(g_current)))
  {
    g_current = g_current.nextSibling;
  }

  if (g_current)
  {
    openGet(g_current, p_showTarget);
  }
}

function restoreSource()
{
  if (!g_target) return;

  o_textbox.setHTML(g_sourceHTML);
}

// This method opens a segment by ID (after split/merge) assuming the
// segment has previously been opened, so all safety checks are OFF.
function openByTuvId(p_id)
{
  if(document.recalc)
  {
    g_current = idSegments.firstChild;

  // check they can be edited and are not locked.
  	while (g_current)
  	{
	    var ids = g_current.id.split('_');
	    var tuvId = ids[2];
	
	    if (tuvId == p_id)
	    {
	      break;
	    }
	
	    g_current = g_current.nextSibling;
	  
  	}
  }
  else
  {
  	  g_current = idSegments.firstChild;
  	  while (g_current)
  	  {
  	  		if(g_current.id)
  	  		{
  	  			var ids = g_current.id.split('_');
  	  			var tuvId = ids[2];
  	  			if (tuvId == p_id)
			    {
			      break;
			    }
  	  		}
  	  		g_current = g_current.nextSibling;
  	  }
  }

  if (g_current)
  {
    openGet(g_current, true);
  }
}

function canEdit(o)
{
  return o.className && o.className.indexOf('segment') == 0;
}

function isLocked(o)
{
  return o.className && (o.className == 'segmentLocked' || o.className == 'segmentContext');
}

function isMerged(o)
{
  var isMer = o.getAttribute("isMerged");
  return isMer == MERGE_START || isMer == MERGE_MIDDLE || isMer == MERGE_END;
  /*return o.isMerged == MERGE_START || o.isMerged == MERGE_MIDDLE ||
    o.isMerged == MERGE_END;*/
}

function isMergeStart(o)
{
  return o.getAttribute("isMerged") == MERGE_START;
  //return o.isMerged == MERGE_START;
}

function isMergeMiddleEnd(o)
{
  return o.getAttribute("isMerged") == MERGE_MIDDLE || o.getAttribute("isMerged") == MERGE_END;
}

function isMergeEnd(o)
{
  return o.getAttribute("isMerged") == MERGE_END;
}

function wasLocked(o)
{
  return o.oldClassName && o.oldClassName == 'segmentLocked';
}

function unlock(o)
{
  if (o.className == 'segmentLocked')
  {
    o.className = 'segmentExact';
  }
  else if (o.className == 'segmentContext')
  {
    o.className = 'segmentContextEditable';
  }
}

function canSplit(o)
{
  return isMerged(o);
}

function canMerge(o)
{
  return getNextSegmentInParagraph(o) != null;
}

//replace all "\\ " to "\\\n"
//for successfactors tool
function adjustCarriageReturn(target)
{
    var len = target.indexOf("\\ ");
    if(len != -1)
    {
        target = target.replace(/\\ /gi, "\\\n");
    }
    
    return target;
}
// SET-CLOSE
// returns true when the editor box could be closed (or is not open)
function save()
{
    if (!g_target) return true;
    var needSave = true;

    if(null==o_textbox)
    {
       o_textbox = document.getElementById("idEditor2");
    }
    var newTarget = o_textbox.getHTML();
    newTarget = RemoveNbsp(newTarget);
    //added for successfactors tool
    newTarget = adjustCarriageReturn(newTarget);

    if (newTarget == g_targetHTML)
    {
        needSave = false;
    }

    if (g_adjustWS)
    {
        newTarget = AdjustWhitespace(g_sourceHTML, newTarget);
    }

    //debug("Cleaned HTML=" + newTarget);
    var ptagstring = DisplayHtmlToPTagString(newTarget);
    //debug("Old PTAG string = " + ptagstring);

    var result;
    if (!checkError(ptagstring))
    {
        if (needSave && wasLocked(g_target))
        {
            if (!g_canEditAll)
            {
                alert("<%=bundle.getString("jsmsg_editor_segment_locked") %>");
                return false;
            }
            else if (!confirm("<%=bundle.getString("jsmsg_editor_segment_unlock_to_save") %>"))
            {
                return false;
            }
        }

        g_targetGxml = applet.getTargetDiplomat(ptagstring);
        //debug("New GXML = `" + g_targetGxml + "'");
        g_targetHTML = GetTargetDisplayHtmlForPreview(g_targetGxml, g_datatype);
        //debug("New display HTML = `" + g_targetHTML + "'");

        if (needSave && saveSegment(g_target.id, g_targetGxml))
        {
            // TODO: error handling. For now, restore original content.
            Close();
            return false;
        }
        g_target.setAttribute("target",g_targetGxml);
        g_target.innerHTML = g_targetHTML;
        g_target.edited = true;
        g_target.className = 'segmentUpdated';
        g_target.oldClassName = null;
        g_current = g_target;
        g_target = null;
        o_textbox = null;

        setButtonState();

        g_sourceHTML = g_targetHTML = null;

        try { ptagwin.close(); } catch (ignore) {}
        try { detailswin.close(); } catch (ignore) {}
        try { w_scwin.close(); } catch (ignore) {};

        if (w_tmwin && !w_tmwin.closed) { w_tmwin.Clear(); }

        result = true;
    }
    else
    {
        result = false;
    }

    //Added for firefox issue: image filter on Menu Bar.
    fnImageSetGrayColorAndListener();

    return result;
}

// CLOSE
function Close()
{
  if (!g_target) return;

  g_targetHTML = GetTargetDisplayHtmlForPreview(g_targetGxml, g_datatype);
  g_target.innerHTML = g_targetHTML;
  g_target.className = g_target.oldClassName;
  o_textbox = null;
  g_current = g_target;
  g_target = null;
  
  setButtonState();

  g_sourceHTML = g_targetHTML = null;

  try { ptagwin.close(); } catch (ignore) {}
  try { detailswin.close(); } catch (ignore) {}
  try { w_scwin.close(); } catch (ignore) {};
  try { w_search.close(); } catch (e) {};

  if (w_tmwin && !w_tmwin.closed) { w_tmwin.Clear(); }

  //Added for firefox issue: image filter on Menu Bar.
  fnImageSetGrayColorAndListener();
}

function checkError(ptagstring)
{
    var message;
    if ((message = doErrorCheck(ptagstring)) != null)
    {
        alert(message);
        o_textbox.frameWindow.focus();
        return 1;
    }
    return 0;
}

function doErrorCheck(ptagstring)
{
    applet.setUntranslateStyle(<%="\"" + SegmentUtil2.getTAGS() + "\""%>);
    var msg = applet.errorCheck(ptagstring, g_sourceGxml,
      max_segment_len, gsa_encoding, db_segment_len, db_encoding);

    if (msg == "" || msg == null || msg == "null")
    {
        return null;
    }
    else
    {
        return msg;
    }
}

function saveSegment(p_segid, p_targetGxml)
{
  idSaving.style.display = 'block';

  var ids = p_segid.split('_');

  var tuId  = ids[1];
  var tuvId = ids[2];
  var subId = ids[3];

  var form = document.SaveForm;
  form.save.value = p_targetGxml;
  form.tuId.value = tuId;
  form.tuvId.value = tuvId;
  form.subId.value = subId;
  form.submit();
}

function saveSegmentDone(p_syncMessage, p_close)
{
  idSaving.style.display = 'none';

  if (p_syncMessage)
  {
    alert(p_syncMessage);
  }

  if (p_close)
  {
    closeEditor();
  }
}

function canClose(p_autoClose)
{
  var result = true;

  if (g_target)
  {
      var newTarget = o_textbox.getHTML();
      if (newTarget != g_targetHTML)
      {
          o_textbox.scrollIntoView(false);
          o_textbox.focus();

          result = !confirm("<%=bundle.getString("jsmsg_editor_confirm_unsave") %>");
      }

      if (result && p_autoClose)
      {
         Close();
      }
  }

  return result;
}

function closeEditor()
{
  if (canClose(false))
  {
    //window.navigate("<%=closeUrl%>");
    // For issue "(AMB-115) Toolbar behaviour for the Inline editor" to resolve the problem which the scroll jump up fist (no good UE) after closed inline editor
     window.close();
  }
  
  try { w_search.close(); } catch (e) {};
}

// Formatting tags are B/I/U
function HasFormattingTags()
{
  if (HasPTags() && g_itemtype == "text")
  {
    return true;
  }

  return false;
}

function openPtags()
{
	return g_datatype == "xlf";
}

function HasPTags()
{
  if (g_datatype.indexOf("html") >= 0 ||
      g_datatype == "cfm" ||
      g_datatype == "asp" ||
      g_datatype == "jsp" ||
      g_datatype == "xml" ||
      g_datatype == "jhtml" ||
      g_datatype == "javaprop-html" ||
      g_datatype == "sgml" ||
      g_datatype == "xptag" ||
      g_datatype == "rtf" ||
      g_datatype.indexOf("troff") >= 0)
  {
    return true;
  }

  return false;
}

function setButtonState()
{
  if (g_target)
  {
    PTagBox.disabled=false;
    if (g_canSpellcheck) idButSpellCheck.setEnabled(true);
    idButUndo.setEnabled(true);
    idButRedo.setEnabled(true);

    idButMerge.setEnabled(canMerge(g_target));
    idButSplit.setEnabled(canSplit(g_target));

    idButDetails.setEnabled(true);
    idButMt.setEnabled(g_canUseMt); //------------------------------------
    idButRtl.setEnabled(true);
    idButLtr.setEnabled(true);
    idButCr.setEnabled(true);
	idBrackets.setEnabled(true);

    if (HasFormattingTags())
    {
      idButBold.setEnabled(true);
      idButItalic.setEnabled(true);
      idButUnderline.setEnabled(true);

      o_textbox.enableFormattingKeys(true);
    }
    else
    {
      idButBold.setEnabled(false);
      idButItalic.setEnabled(false);
      idButUnderline.setEnabled(false);     

      o_textbox.enableFormattingKeys(false);
    }

    if (HasPTags())
    {
      idButBr.setEnabled(true);
      idButNbsp.setEnabled(true);
      idButPtags.setEnabled(true);
    }
    else
    {
      idButBr.setEnabled(false);
      idButNbsp.setEnabled(false);
      idButPtags.setEnabled(false);
    }

    if (openPtags())
    {
    	idButPtags.setEnabled(true);
    }
  }
  else
  {
    PTagBox.disabled=true;
    idButSpellCheck.setEnabled(false);
    idButUndo.setEnabled(false);
    idButRedo.setEnabled(false);
    idButBold.setEnabled(false);
    idButItalic.setEnabled(false);
    idButUnderline.setEnabled(false);
    idBrackets.setEnabled(false);
    idButLtr.setEnabled(false);
    idButRtl.setEnabled(false);
    idButCr.setEnabled(false);
    idButBr.setEnabled(false);
    idButNbsp.setEnabled(false);
    idButPtags.setEnabled(false);
    idButDetails.setEnabled(false);
    idButMt.setEnabled(false);
    idButMerge.setEnabled(false);
    idButSplit.setEnabled(false);
  }
}

function makeUndo()
{
  if (!g_target) return;

  o_textbox.undo();  
  o_textbox.frameWindow.focus();
}

function makeRedo()
{
  if (!g_target) return;

  o_textbox.redo();
  o_textbox.frameWindow.focus();
}

function makeBold()
{
  if (!g_target) return;
  
  //o_textbox.setBold();
  o_textbox.makeBold();
  o_textbox.frameWindow.focus();
}

function makeItalic()
{
  if (!g_target) return;

  o_textbox.makeItalic();
  o_textbox.frameWindow.focus();
}

function makeUnderline()
{
  if (!g_target) return;

  o_textbox.makeUnderline();
  o_textbox.frameWindow.focus();
}

function makeLtr()
{
  if (!g_target) return;

  o_textbox.makeLtr();
  o_textbox.frameWindow.focus();
}

function makeRtl()
{
  if (!g_target) return;

  o_textbox.makeRtl();
  o_textbox.frameWindow.focus();
}

function addBr()
{
  if (!g_target) return;

  o_textbox.addBr();
  o_textbox.frameWindow.focus();
}

function addLre()
{
  if (!g_target) return;

  o_textbox.addLre();
  o_textbox.frameWindow.focus();
}

function addPdf()
{
  if (!g_target) return;

  o_textbox.addPdf();
  o_textbox.frameWindow.focus();
}

function addCr()
{
  if (!g_target) return;

  o_textbox.addCr();
  o_textbox.frameWindow.focus();
}

function addNbsp()
{
  if (!g_target) return;

  o_textbox.addNbsp();
  o_textbox.frameWindow.focus();
}

function setText(text)
{
  if (!g_target) return;

  //o_textbox.setText(text);
  o_textbox.setHTML(text);
  o_textbox.frameWindow.focus();
}

function insertText(text)
{
  if (!g_target) return;

  o_textbox.insertText(text);
  o_textbox.frameWindow.focus();
}

function addBrackets()
{
  if (!g_target) return;

  o_textbox.addBrackets();
  o_textbox.frameWindow.focus();
}

var ptagwin = null;
var args = null;
var flag = false;
function showPTags()
{
  if (!g_target) return;

  if(document.recalc)
  {
  	if (!ptagwin || ptagwin.closed)
  	{
    	var ptags = applet.getPtagString();
    	args = { _opener: window, _data: ptags.split(",") };
   
    		ptagwin = showModelessDialog("/globalsight/envoy/edit/online/selectptag.jsp",
      			args, "dialogWidth:10em; dialogHeight:15em; status:no; help:no;");
    }
  	else
  	{
    	ptagwin.focus();
  	}
  }
  else
  {
  	var ptags = applet.getPtagString();
  	var data = ptags.split(",");
  	if (ptags.length > 0 )
  	{
    	if(!flag)
    	{
    		for (var i = 0; i < data.length; i += 2)
    		{
      			addRow(data[i], data[i+1]);
    		}
    	}
  	}
  	else
  	{
    	idTags.innerHTML = "<%=lb_no_ptags_in_segment%>";
  	}
  	flag = true;
  	var divElement = document.getElementById("selectPtag");		 
	divElement.style.visibility='visible';	
  }
}
function addRow(tag1, tag2)
{
  var table = document.getElementById('idTable');
  var row =   document.createElement("TR");
  var cell1 = document.createElement("TD");
  var cell2 = document.createElement("TD");

  cell1.value = tag1;
  cell1.innerHTML =
    "<SPAN class='link' onclick='doClick(this)'>" + tag1 + "</SPAN>";

  if (tag2)
  {
    cell2.value = tag2;
    cell2.innerHTML = 
      "<SPAN class='link' onclick='doClick(this)'>" + tag2 + "</SPAN>";
  }
  else
  {
    cell2.innerText = '\u00a0';
  }

  row.appendChild(cell1);
  row.appendChild(cell2);
  table.appendChild(row);
}

function doClick(elem)
{
 InsertPTag(elem.parentNode.value);
}

function GetSegmentId()
{
  if (!g_target) return null;
  var ids = g_target.id.split('_');
  return ids[2];
}

function GetSegmentFormat()
{
  if (!g_target) return null;
  return g_datatype;
}

function GetSegmentType()
{
  if (!g_target) return null;
  return g_itemtype;
}

function GetSegmentWordCount()
{
  if (!g_target) return null;
  // This data is not available.
  return -1;
}

function GetDetails()
{
  if (!g_target) return null;

  return applet.getPtagToNativeMappingTable();
}

var detailswin = null;
function showDetails()
{
  if (!g_target) return;

  if (!detailswin || detailswin.closed)
  {
    detailswin = window.open(
      "/globalsight/envoy/edit/online2/details.jsp", "Details",
      "resizable,scrollbars=yes,width=400,height=400");
  }
  else
  {
    detailswin.focus();
  }
}

function InsertPTag(tag)
{
  o_textbox.insertPTag(tag);
}

function navigatePage(offset)
{
	if (canClose(false))
    {
	    var pre = "<IMG SRC='/globalsight/images/editorPreviousPagex.gif' BORDER=0 HSPACE=1 VSPACE=2>";
	    var next = "<IMG SRC='/globalsight/images/editorNextPagex.gif' BORDER=0 HSPACE=1 VSPACE=2>";
		document.getElementById("fileNavPre").innerHTML = pre;
		document.getElementById("fileNavNext").innerHTML = next;
		
    	var str_url = "<%=url_refresh%>&refresh=" + offset;
		document.location = str_url;
    }
}

function openInfoWin()
{
  // TODO: open window with minimum vertical size.
  var y = screen.height / 4;

  window.moveTo(0, y);
  window.resizeTo(screen.width-20, screen.height - y);

  var url = "<%=url_tmInfo%>"; //"/globalsight/envoy/edit/online2/tmwindow.jsp";
  return window.open(url, "TM",
      "status=false,help=no,width=" + (screen.width-20) + ",height=" + (y-27) +
      ",top=0,left=0,resizable=yes,toolbar=no");
}

var sc_customDict = null;
var sc_dict = null;
var sc_uiLang = null;

function spellCheck()
{
    if (!g_target) return;

    if (g_SC_XDE.isLanguageSupported(g_targetLocale))
    {
        // XDE spell checking

        if (!sc_customDict)
        {
          sc_dict = g_SC_XDE.getSystemDict(g_targetLocale);
          sc_customDict = g_SC_XDE.getCustomDict(g_userId, g_targetLocale);
          sc_uiLang = g_SC_XDE.getUiLanguage(g_uiLocale);

          frmSC.language.value = sc_dict;
        }

        alert("<%=bundle.getString("jsmsg_editor_xde_spell_check_note")%>".replace("%1", sc_dict).replace("%2", sc_customDict));

        w_scwin = doSpell(this, 'frmSC.language', 'idEditor2&typectrl=xdeedit',
          false, sc_customDict, sc_uiLang);
    }
    else
    {
        // GlobalSight spell checking (supports all languages)

        if (!sc_customDict)
        {
          sc_dict = g_SC_GSA.getSystemDict(g_targetLocale);
          sc_customDict = g_SC_GSA.getCustomDict(g_userId, g_targetLocale);
        }

        //alert("GlobalSight spell checking using dict `" + sc_dict +
        //  "', customdict `" + sc_customDict + "'");

        w_scwin = scSpell(this, 'idEditor2&typectrl=xdeedit',
          g_targetLocale, g_uiLocale, sc_dict, sc_customDict);
    }
}

function showTmWindow()
{
  if (idButTm.getValue() == false)
  {
    // hide window
    if (w_tmwin && !w_tmwin.closed)
    {
      w_tmwin.close();
    }
    w_tmwin = null;
  }
  else
  {
    // show window
    if (!w_tmwin || w_tmwin.closed)
    {
      w_tmwin = openInfoWin();
      showMatches();
    }

    try { w_tmwin.focus(); } catch (ignoreonie55) {}
  }
}

function closingTmWindow()
{
  w_tmwin = null;
  idButTm.setValue(false);
}

function showingTmWindow(win)
{
  w_tmwin = win;
  idButTm.setValue(true);
}

function showMatches()
{
  if (!g_target || !w_tmwin || w_tmwin.closed)
  {
    return;
  }

  var ids = g_target.id.split('_');

  // To releverage the current segment, add "&releverage=true" to the URL
  var url = "<%=url_tmInfo%>&refresh=0&releverage=true" +
    "&tuId=" + ids[1] + "&tuvId=" + ids[2] + "&subId=" + ids[3];

  w_tmwin.location.href = url;
}

function showNextMatch()
{
  if (!g_target || !w_tmwin || w_tmwin.closed)
  {
    return;
  }

  try
  {
    w_tmwin.goRightSegment();
  }
  catch (ex)
  {
  }
}

function showHelp()
{
    var helpWindow = window.open(helpFile, 'helpWindow',
      'resizable=yes,scrollbars=yes,WIDTH=600,HEIGHT=400');
    helpWindow.focus();
}

function showOptions()
{
    w_options = window.open("<%=url_options%>", "Options",
      ("resizable=no,scrollbars=no,status=no,help=no,width=350,height=250"));
}

function showPageInfo()
{
    w_pageinfo = window.open("<%=url_pageInfo%>", "MEPageInfo",
      ("resizable,width=400,height=400"));
}

//show the corpus browser for concordance searches
function doConcordance()
{
   var url = "<%=url_concordance%>&fromEditor=true";
   w_concordance = window.open(url, "<%=lb_concordance%>",
   'location=no,menubar=no,resizable=yes,scrollbars=yes,WIDTH=800,HEIGHT=600');
}

function unlockSegments()
{
  var current = idSegments.firstChild;

  while (current)
  {
    if (isLocked(current))
    {
      unlock(current);
    }
    current = current.nextSibling;
  }
}

function getNextSegmentInParagraph(current)
{
  var next = current.nextSibling;

  while (next && canEdit(next))
  {
    if (!isMerged(next))
    {
      return next;
    }

    next = next.nextSibling;
  }

  return null;
}

function getLastMergedSegment(current)
{
    var tmp = current;

    while (!isMergeEnd(tmp))
    {
        tmp = tmp.nextSibling;
    }

    return tmp;
}

function mergeSegments()
{
  if (!g_current) return;

  var next = getNextSegmentInParagraph(g_current);

  if (!next) return;
  if (isLocked(next))
  {
    if (g_canEditAll)
    {
      alert("<%=bundle.getString("jsmsg_editor_segment_unlock_to_merge")%>");
    }
    else
    {
      alert("<%=bundle.getString("jsmsg_editor_segment_cannot_merge")%>");
    }

    return;
  }

  Close();

  if(!isIE)
  {
	fnImageChangeWrapper();
  }

  var ids = g_current.id.split('_');
  var tuv1 = ids[2];
  var ids = next.id.split('_');
  var tuv2 = ids[2];

  var form = document.SplitMergeForm;
  form.action.value = 'merge';
  form.tuv1.value = tuv1;
  form.tuv2.value = tuv2;
  form.submit();
}

function splitSegments()
{
  if (!g_current || !isMerged(g_current)) return;

  var last = getLastMergedSegment(g_current);

  if (!last) return;

  Close();

  if(!isIE)
  {
	fnImageChangeWrapper();
  }

  var ids = g_current.id.split('_');
  var tuv1 = ids[2];
  var ids = last.id.split('_');
  var tuv2 = ids[2];

  var form = document.SplitMergeForm;
  form.action.value = 'split';
  form.tuv1.value = tuv1;
  form.tuv2.value = tuv2;
  form.location.value = 'bottom'; // top or bottom
  form.submit();
}

function initRichtext()
{
    //o_textbox = document.all.idEditor2;
    o_textbox = document.getElementById("idEditor2");
}

function HilitePtags(bright)
{
    g_hilitePtags = bright;

    var css = document.styleSheets.item('cssPtag');
    if(!document.recalc)
    {
    	var rule = css.cssRules[0];
    }
    else
    {
    	var rule = css.rules[0];
    }
    
    rule.style.color = bright ? '#3366FF' : '#808080';
    if (g_target)
    {
      o_textbox.setPtagColor(bright ? '#3366FF' : '#808080');
    }
}

function searchByUserSid() 
{
  var url = "<%=url_search%>&search=true";
  
  if(document.recalc)
  {
  	if (!w_search || w_search.closed)
  	{
   		w_search = window.open(url,"","height=380px, width=600px,status=no,resizable=yes,modal=no"); 
  	}
  	else
  	{
    	w_search.focus();
  	}
  }
  else
  {
      if (!w_search || w_search.closed)
      {
      		window.myAction=this;
      		window.myArguments=true;
        	w_search = window.open(url,"","height=380px, width=600px,status=no,resizable=yes,modal=no"); 
    	}
    	else
  	  {
    	    w_search.focus();
  	  }
  }
}

function showProgressWindow()
{
  if(document.recalc)
  {
  	if (!w_progress || w_progress.closed)
  	{
    	var args = { _opener: window, _data: true };

   		w_progress = showModelessDialog(
      		"/globalsight/envoy/edit/online2/progress.jsp", args,
      		"dialogWidth:226px; dialogHeight:280px; status:no; help:no;");
  	}
  	else
  	{
    	w_progress.focus();
  	}
  }
  else
  {
  		window.myAction=this;
  		window.myArguments=true;
  		var url = "/globalsight/envoy/edit/online2/progress.jsp";
    	w_progress = window.open(url,"","height=280px, width=220px,status=no,modal=yes"); 
  }
}

function HighlightTouched()
{
  cssEditor.disabled = true;
  cssEditorTouched.disabled = false;
  cssEditorTranslated.disabled = true;
  cssEditorUntranslated.disabled = true;

  var total = cnt = 0;
  var o = idSegments.firstChild;
  while (o)
  {
    if (canEdit(o))
    {
      total++;

      if (o.className == 'segmentUpdated')
      {
        cnt++;
      }
    }
    o = o.nextSibling;
  }

  return total == 0 ? 0 : Math.round(cnt * 100 / total);
}

function HighlightTranslated()
{
  cssEditor.disabled = true;
  cssEditorTouched.disabled = true;
  cssEditorTranslated.disabled = false;
  cssEditorUntranslated.disabled = true;
  var total = cnt = 0;
  var o = idSegments.firstChild;
  while (o)
  {
    if (canEdit(o))
    {
      total++;
      if (o.className == 'segmentExact' ||
          o.className == 'segmentLocked' ||
          o.className == 'segmentUpdated' ||
          o.className == 'segmentUnverified' ||
          o.className == 'segmentContext')
      {
        cnt++;
      }
    }
    o = o.nextSibling;
  }

  return total == 0 ? 100 : Math.round(cnt * 100 / total);
}

function HighlightUntranslated()
{
  cssEditor.disabled = true;
  cssEditorTouched.disabled = true;
  cssEditorTranslated.disabled = true;
  cssEditorUntranslated.disabled = false;

  var total = cnt = 0;
  var o = idSegments.firstChild;
  while (o)
  {
    if (canEdit(o))
    {
      total++;

      if (o.className == 'segment' ||
          o.className == 'segmentFuzzy' ||
          o.className == 'segmentRepetition')
      {
        cnt++;
      }
    }
    o = o.nextSibling;
  }

  return total == 0 ? 0 : Math.round(cnt * 100 / total);
}

function HighlightNormal()
{
  cssEditor.disabled = false;
  cssEditorTouched.disabled = true;
  cssEditorTranslated.disabled = true;
  cssEditorUntranslated.disabled = true;
}

function JumpFirstUntranslated()
{
  var o = idSegments.firstChild;
  while (o)
  {
    if (canEdit(o))
    {
      if (o.className == 'segment' ||
          o.className == 'segmentFuzzy' ||
          o.className == 'segmentRepetition')
      {
         // highlight segment not implemented yet, open in editor.
         if (canClose(true))
         {
           edit(o);
         }

         return;
      }
    }
    else if (o == g_target &&
       (o.oldClassName == 'segment' ||
        o.oldClassName == 'segmentFuzzy' ||
        o.oldClassName == 'segmentRepetition'))
    {
       return;
    }

    o = o.nextSibling;
  }
}

function JumpFirstFuzzy()
{
  var o = idSegments.firstChild;
  while (o)
  {
    if (canEdit(o))
    {
      if (o.className == 'segmentFuzzy')
      {
         // highlight segment not implemented yet, open in editor.
         if (canClose(true))
         {
           edit(o);
         }

         return;
      }
    }
    else if (o == g_target && o.oldClassName == 'segmentFuzzy')
    {
       return;
    }

    o = o.nextSibling;
  }
}

// This may lose unsaved changes and the screen may flickers on slow machines.
function GetSegmentPreview()
{
  var result;
  var tempHtml;

  var target = g_target;

  if (target)
  {
    tempHtml = o_textbox.getHTML();
    Close();
  }

  result = idSegments.innerHTML;

  if (target)
  {
    // restore focus on current segment
    edit(target);
    o_textbox.setHTML(tempHtml);
  }

  return result;
}

function doInit()
{
  showMtButton(g_showMt);
  HilitePtags(g_hilitePtags);

  if (g_autoUnlock)
  {
    unlockSegments();
  }

  var table = document.getElementById("idMenuBar");
  var cells = table.rows[0].cells;
  for (var i = 0; i < cells.length; i++)
  {
    // 6,19,22 are spacer cells (6,19,22 for bidi)
    if (i == 6 || i == 19 || i == 22) continue;
    createButton(cells[i]);
  }
  // cell 23 is the TM button (23 for bidi)
  cells[23].setToggle(true);
  setButtonState();

  /* GS can always spellcheck
  if (!g_SC.isLanguageSupported(g_targetLocale))
  {
    g_canSpellcheck = false;
    idButSpellCheck.setEnabled(false);
  }
  */

  g_canUseMt = g_MT.isLanguageSupported("google", g_sourceLocale, g_targetLocale);
  idButMt.setEnabled(false);

  if(document.recalc)
  {
  	document.recalc(true);
  }

  applet.setLocale(g_uiLocale);
  applet.setDataType(g_dataFormat);

  idSegments.focus();

  if (openOnLoad)
  {
    openByTuvId(openOnLoad);
  }

  // Check for shutdown every 5 minutes
  checkShutdown(1000 * 60 * 5);

  window.setTimeout("idLoading.style.display = 'none';", 250, "JavaScript");
  window.defaultStatus = '';

  if (g_syncMessage)
  {
      alert(g_syncMessage);
  }
  
  if (g_syncClose)
  {
      closeEditor();
  }

  if(!isIE)
  {
	  idSegments.style.width = idBody.clientWidth - 40;
	  idSegments.style.height = idBody.clientHeight - 95;
  }  
}

function doExit()
{
  if (g_lastKeyHome)
  {
    g_lastKeyHome = false;
    event.returnValue = "Use Alt-Ctrl-Home to open the next segment.";
    event.cancelBubble = true;
    return event.returnValue;
  }

  try { w_tmwin.close(); } catch (e) {};
  try { w_scwin.close(); } catch (e) {};
  try { w_pageinfo.close(); } catch (e) {};
  try { w_options.close(); } catch (e) {};
  try { w_concordance.close(); } catch (e) {};
  try { w_progress.close(); } catch (e) {};
}

// MT interface

function showMtButton(show)
{
  g_showMt = show;

  //set 'show' to false to hide mt label for now
  show = false;
  if (g_canShowMt && show)
  {
    idButMt.style.display = '';
  }
  else
  {
    idButMt.style.display = 'none';
  }
}

function translate()
{
  if (!g_target) return;

  var text = RemovePtags(idEditor1.innerText);
  mtQuery(text);
}

var mtwin = null;
function mtQuery(p)
{
  var x = window.screenLeft + (Math.max(idBody.clientWidth - 300, 0) / 2);
  var y = window.screenTop  + (Math.max(idBody.clientHeight - 10, 0) / 2)

  mtwin = window.open("/globalsight/envoy/edit/online2/pleasewait.jsp", "MT",
    "status=false,resizable=yes,help=no,width=300,height=10," +
    "left=" + x + ",top=" + y);

  var form = document.getElementById("frmTranslator");
  form.segment.value = p;
  form.submit();

  mtAvailable();
}

function mtAvailable()
{
  var result = null;

  while (!result)
  {
    try
    {
      result = mtwin.document.getElementById("result");
    }
    catch (ex)
    {
      // should sleep for a few (milli)seconds
    }
  }

  var trans = result.innerText;

  // alert("Translation is " + trans);

  o_textbox.setText(trans);

  mtwin.close();
  mtwin = null;

  return trans;
}

function DoChangePTags(o_select)
{
    if (o_select.selectedIndex == 1)
    {
        if (!setVerbose())
        {
            o_select.selectedIndex = 0;
        }
    }
    else
    {
        if (!setCompact())
        {
            o_select.selectedIndex = 1;
        }
    }
}

function setVerbose()
{
	var targetDiplomatString = getTargetDiplomatString();	
    verbose = "<%=EditorConstants.PTAGS_VERBOSE%>";
	g_ptagsVerbose = true;
	
	g_targetHTML = InitDisplayHtml(targetDiplomatString, g_datatype, g_ptagsVerbose, g_preserveWhitespace, g_dataFormat);
	g_sourceHTML = InitDisplayHtml(g_sourceGxml, g_datatype, g_ptagsVerbose, g_preserveWhitespace, g_dataFormat);
	idEditor1.innerHTML = g_sourceHTML; 
	o_textbox.setHTML(g_targetHTML);
	o_textbox.setVerbosePTags(g_ptagsVerbose);
	
    return true;
}

function setCompact()
{
	var targetDiplomatString = getTargetDiplomatString();
    verbose = "<%=EditorConstants.PTAGS_COMPACT%>";
    g_ptagsVerbose = false;
	
	g_targetHTML = InitDisplayHtml(targetDiplomatString, g_datatype, g_ptagsVerbose, g_preserveWhitespace, g_dataFormat);
	g_sourceHTML = InitDisplayHtml(g_sourceGxml, g_datatype, g_ptagsVerbose, g_preserveWhitespace, g_dataFormat);
	idEditor1.innerHTML = g_sourceHTML; 
	o_textbox.setHTML(g_targetHTML);
	o_textbox.setVerbosePTags(g_ptagsVerbose);
	
    return true;
}

function getTargetDiplomatString()
{
    if (!g_target) return true;

    var needSave = true;
    var newTarget = o_textbox.getHTML();

    newTarget = RemoveNbsp(newTarget);
    //added for successfactors tool
    newTarget = adjustCarriageReturn(newTarget);

    if (newTarget == g_targetHTML)
    {
        return g_targetGxml;
    }

    if (g_adjustWS)
    {
        newTarget = AdjustWhitespace(g_sourceHTML, newTarget);
    }

    var ptagstring = DisplayHtmlToPTagString(newTarget);

    if (!checkError(ptagstring))
    {
        g_targetGxml = applet.getTargetDiplomat(ptagstring);
    }
    else
    {
        return false;
    }
	return g_targetGxml;
}

function showMenu()
{
	if(document.recalc)
	{
		return ;
	}
	else
	{
		var tmpElement = document.getElementById("idAction");
		var tmpLeft = tmpElement.offsetLeft;
		var tmpTop = tmpElement.offsetTop + 20;
		var pageLeft = document.body.clientWidth;
		if (tmpLeft + 120 > pageLeft)
		{
			tmpLeft = tmpLeft + tmpElement.offsetWidth - 120;
		}

		var shimmer = document.createElement('iframe');
    	shimmer.id='shimmer';
    	shimmer.style.position='absolute';
    	shimmer.style.width= '120px';
    	shimmer.style.height = '100px';
    	shimmer.style.top = tmpTop + 'px';
   		shimmer.style.left= tmpLeft + 'px';
    	shimmer.style.zIndex='999';
    	shimmer.setAttribute('frameborder','0');
    	document.body.appendChild(shimmer);
		
		var divElement = document.getElementById("menu1");
		if(divElement.style.visibility=='visible')	
		{
			divElement.style.visibility='';
		}
		else
		{
			divElement.style.left = tmpLeft + 'px';
			divElement.style.width = 120+"px";
			divElement.style.height = "auto";
			divElement.style.top = tmpTop + 'px'; 
			divElement.style.visibility='visible';
			divElement.style.backgroundColor = "buttonface";
			divElement.style.border = "3px outset white";
			divElement.style.fontSize = "12pt";
		}
		
	}
}

function hideMenu()
{
	if(document.recalc)
	{
		return ;
	}
	else
	{
		var sd = document.getElementById('menu1');
        sd.style.visibility='hidden';
        var shimmer = document.getElementById('shimmer');
        document.body.removeChild(shimmer);
	}
}
function hideSelectPtag()
{
	var flag = false;
	var divElement = document.getElementById("selectPtag");		
	divElement.style.visibility='hidden';	
}

function disableCSS()
{
    cssEditorTouched.disabled = true;
    cssEditorTranslated.disabled = true;
    cssEditorUntranslated.disabled = true;
}

function fnImageChangeWrapper()
{
  if(isIE) return;
  
  fnImageChange("idImgSpellcheck");
  fnImageChange("idImgUndo");
  fnImageChange("idImgRedo");
  fnImageChange("idImgBold");
  fnImageChange("idImgItalic");
  fnImageChange("idImgUnderline");  
  fnImageChange("idImgBr");
  fnImageChange("idImgNbsp");
  fnImageChange("idImgPTags");
  fnImageChange("idImgBrackets");
  fnImageChange("idImgCr");
  fnImageChange("idImgDetails");
  fnImageChange("idImgMerge");
  fnImageChange("idImgSplit");
  fnImageChange("idImgLtr");
  fnImageChange("idImgRtl")
}

function fnImageChange(id)
{
  if(isIE) return;
	  
  var image = document.getElementById(id);
  image.src = fnImageGetSrc(id, "red");
  image.style.opacity = "";
}

function fnImageSetArray()
{
  var images = document.getElementById("idMenuBar").getElementsByTagName("img");
  imgIdArr = new Array(images.length);
  imgSrcArr = new Array(images.length);
  imgGraySrcArr = new Array(images.length);
  for(var i=0;i<imgIdArr.length;i++)
  {
	var src = images[i].src;
	var index = src.lastIndexOf("/")+1;
	var graySrc = src.substr(0,index)+graySub+src.substr(index);
	
	imgIdArr[i]		 = images[i].id;
	imgSrcArr[i]	 = src;
	imgGraySrcArr[i] = graySrc;
  }
  //alert(imgIdArr.length);	
}

function fnImageGetSrc(id, type)
{
  if(imgIdArr==null)
  {
	fnImageSetArray();
  }
	  
  if(type=="red")
  {
	for(var i=0;i<imgIdArr.length;i++)
  	{
		if(imgIdArr[i]==id)
		{
			return imgSrcArr[i];
		}
  	}
  }
  else
  {
	for(var i=0;i<imgIdArr.length;i++)
	{
		if(imgIdArr[i]==id)
		{
			if("idImgTm" == id)
			{
				return imgSrcArr[i];
			}
			else
			{
				return imgGraySrcArr[i];
			}
			
		}
	}
  }
}

function fnImageSetGrayColorAndListener()
{
  if(isIE) return;
  
  for(var i=0;i<imgIdArr.length;i++)
  {
	var img = document.getElementById(imgIdArr[i]);
	
	img.src = imgGraySrcArr[i];
	img.src = fnImageGetSrc(imgIdArr[i], "gray");
	var opacityStr = img.getAttribute("attr");
	if(opacityStr!=null && "1"!=opacityStr)
	{
		img.style.opacity = "0.4";
	}
	img.addEventListener(
		'mouseover', 
		function(){fnImageListener(this, 'mouseover')},
		false);
	img.addEventListener(
		'mouseout', 
		function(){fnImageListener(this, '')},
		false);
  }
}

function fnImageListener(elem, type)
{
  if("mouseover" == type)
  {
	elem.src = fnImageGetSrc(elem.id, "red");
	if("0.4" != elem.style.opacity)
	{
		elem.parentNode.style.border = "2px outset gray";
	}
  }
  else
  {
	elem.src = fnImageGetSrc(elem.id, "gray");
	elem.parentNode.style.border = "";
  }
  
}

function doOnLoad()
{
  disableCSS();

  // Add image filter function for firefox
  if(!isIE)
  {
	fnImageSetArray();
	fnImageSetGrayColorAndListener();
  }

  updateFileNavigationArrow();
  updatePageNavigationArrow();
}

function updateFileNavigationArrow()
{
	var fileNavPre, fileNavNext;

	var isFirstPage = '<%=state.isFirstPage()%>';
	if (isFirstPage == 'false')
	{
		fileNavPre = "<A HREF='#' onclick='navigatePage(-1); return false;' onfocus='this.blur()'>"
			+ "<IMG SRC='/globalsight/images/editorPreviousPage.gif' BORDER=0 HSPACE=1 VSPACE=2></A>";
			    			
		document.getElementById("fileNavPre").innerHTML = fileNavPre;
	}

	var isLastPage = '<%=state.isLastPage()%>';
	if (isLastPage == 'false')
	{
       fileNavNext = "<A HREF='#' onclick='navigatePage(1); return false;' onfocus='this.blur()'>"
           + "<IMG SRC='/globalsight/images/editorNextPage.gif' BORDER=0 HSPACE=1 VSPACE=2></A>";
       document.getElementById("fileNavNext").innerHTML = fileNavNext;
	}
}

function updatePageNavigationArrow()
{
   var pageNavPre, pageNavNext;

   var isFirstBatch = '<%=state.isFirstBatch()%>';
   if (isFirstBatch == 'false')
   {
       pageNavPre = "<A HREF='#' onclick='navigatePage(-11); return false;' onfocus='this.blur()'>"
           + "<IMG SRC='/globalsight/images/editorPreviousPage.gif' BORDER=0 HSPACE=1 VSPACE=2></A>";
       document.getElementById("pageNavPre").innerHTML = pageNavPre;
   }

   var isLastBatch = '<%=state.isLastBatch()%>';
	if (isLastBatch == 'false')
	{
       pageNavNext = "<A HREF='#' onclick='navigatePage(11); return false;' onfocus='this.blur()'>"
           + "<IMG SRC='/globalsight/images/editorNextPage.gif' BORDER=0 HSPACE=1 VSPACE=2></A>";
       document.getElementById("pageNavNext").innerHTML = pageNavNext;
	}
}

</SCRIPT>
<style type="text/css">
      #menu1  {display:block; width:400px; height:400px; 
                background:white; position:absolute; 
                z-index:1000; visibility:hidden;}
</style>
</head>

<body id="idBody" return onkeydown='doKeyDown()' onkeypress='return doKeyPress()'
 onkeyup="doKeyUp()"  onbeforeunload="doExit()" scroll="no" onload="doOnLoad()">

<div id="idLoading" style="position: absolute; top: 100; left: 200;
background-color: lightgrey; color: black; text-align: center;
border: 2px solid black; padding: 10 100; font-size: 14pt; z-index: 99;">
<%=lb_loading%> <img src="/globalsight/envoy/edit/online2/bullet2.gif">
</div>
<div id="idSaving" style="position: absolute; top: 100; left: 200;
background-color: lightgrey; color: black; text-align: center; display: none;
border: 2px solid black; padding: 10 100; font-size: 14pt; z-index: 99;">
<%=lb_saving%> <img src="/globalsight/envoy/edit/online2/bullet2.gif">
</div>

<form id="frmTranslator" name="frmTranslator" method="post" target="MT"
 style="display: none" action="/globalsight/MtServlet">
<input type="hidden" name="engine" value="babelfish">
<input type="hidden" name="source" value="<%=str_sourceLocale%>">
<input type="hidden" name="target" value="<%=str_targetLocale%>">
<input type="hidden" name="segment" value="">
</form>

<div class="header" style="position: absolute; top: 0; left: 0; right: 0;
 width: expression(idBody.clientWidth); height: 40;
 padding-left: 10px; padding-top: 6px;">
    <SPAN id="idHelpViewer" onclick="showHelp()" class="help"
     style="margin-right: 10px;"><%=bundle.getString("lb_help") %></SPAN>
    <SPAN class="help">&nbsp;|&nbsp;</SPAN>
    <SPAN id="idCloseEditor" class="help" onclick="closeEditor()"><%=bundle.getString("lb_close") %></SPAN>
	<script>
	if (document.recalc)
	{
		document.write("<SPAN class=\"help\" style=\"position:relative; top:-4;\" onclick=\"showMenu();\" >");
	}
	else
	{
		document.write("<SPAN id=\"idAction\" class=\"help\" style=\"position:relative; top:-0;\" onclick=\"showMenu();\" >");
	}
	</script>
    <!-- <SPAN class="help" style="position:relative; top:-4;" onclick="showMenu();" >  -->
    <script>
    if(document.recalc)
	{
    	menuBar.write();
    }
    else
    {
       document.write("Action");
    }
    </script>&nbsp;&nbsp;&nbsp;</SPAN>
<%--
    <P>Page: <span id="idPagename"><%=str_pageName%></span>
--%>
    <span><%=bundle.getString("lb_inline_editor") %>: <%=str_sourceLocale_dis%> &#x2192; <%=str_targetLocale_dis%></span>
    <SPAN>&nbsp;&nbsp;&nbsp;&nbsp;|&nbsp;&nbsp;&nbsp;&nbsp;</SPAN>
    <!-- File Navigation -->
    <SPAN><%=lb_fileNavigation%>
        <label id="fileNavPre"><%=lb_prevFile%></label>
        <label id="fileNavNext"><%=lb_nextFile%></label>
     </SPAN>
     <SPAN>&nbsp;&nbsp;&nbsp;&nbsp;|&nbsp;&nbsp;&nbsp;&nbsp;</SPAN>
     <!-- Page Navigation -->
     <SPAN><%=lb_pageNavigation%>&nbsp;( <%=pi.getCurrentPageNum()%> of <%=pi.getTotalPageNum()%> )
        <label id="pageNavPre"><%=lb_prevPage%></label>
        <label id="pageNavNext"><%=lb_nextPage%></label>
     </SPAN>

</div>
<style type="text/css">
#b_g_date tr{ 
  event:expression(onmouseover = function(){this.style.background='#E3FFC9'},onmouseout = function(){this.style.background='#FFFFFF'}); 
} 
#b_g_date tr:hover{ 
  background:#0C1476; 	/*#0000FF*/
  color:#FFFFFF
} 

#b_g_date td{ 
  height:12px;
  font-size:12px;
} 
</style>
<!--
		<div id = "menu1" onclick = "hideMenu();">
    		<span onclick ="unlockSegments();" onmouseover="ffmenuOver(event)" onmouseout="ffmenuOut(event)">Unlock Segments</span><br>
    		<span onclick ="showProgressWindow();" onmouseover="ffmenuOver(event)" onmouseout="ffmenuOut(event)">Progress...</span><br>
    		<span onclick ="doConcordance();" onmouseover="ffmenuOver(event)" onmouseout="ffmenuOut(event)" >Concordance...</span><br>
    		<span onclick ="showPageInfo();" onmouseover="ffmenuOver(event)" onmouseout="ffmenuOut(event)" >Page Info...</span><br>
    		<span onclick ="showOptions();" onmouseover="ffmenuOver(event)" onmouseout="ffmenuOut(event)" >Options...</span><br>
 		</div>
		-->
		<div id = "menu1" onclick = "hideMenu();">
			<table width="120px" id="b_g_date" border=0>
				<tr><td onclick="unlockSegments();">&nbsp;&nbsp;&nbsp;&nbsp;<%=bundle.getString("lb_unlock_segments")%></td></tr>
				<tr><td onclick="searchByUserSid();">&nbsp;&nbsp;&nbsp;&nbsp;<%=bundle.getString("lb_search")%>...</td></tr>
				<tr><td onclick="showProgressWindow();">&nbsp;&nbsp;&nbsp;&nbsp;<%=bundle.getString("lb_progress") %>...</td></tr>
				<tr><td onclick="doConcordance();">&nbsp;&nbsp;&nbsp;&nbsp;<%=bundle.getString("lb_concordance") %>...</td></tr>
				<tr><td onclick="showPageInfo();">&nbsp;&nbsp;&nbsp;&nbsp;<%=bundle.getString("lb_page_info") %>...</td></tr>
				<tr><td onclick="showOptions();">&nbsp;&nbsp;&nbsp;&nbsp;<%=bundle.getString("lb_options") %>...</td></tr>
			</table>
		</div>
		
<div id='idMenuBarDiv' class='toolBar' style='position: absolute; top: 30; left: 0; 
												width:100%; width:expression(idBody.clientWidth);'>
<table id="idMenuBar" cellspacing="0" cellpadding="0" cellspacing="0">
  <tr>
    <td onaction="openNext(true);" title="<%=bundle.getString("lb_editor_segment_open_tran")%>"
      ><img src="/globalsight/envoy/edit/online2/Open.gif" attr="1" id="idImgOpen"></td>
    <td onaction="openNext(true)" title="<%=bundle.getString("lb_editor_segment_open_tran") %>"
      ><img src="/globalsight/envoy/edit/online2/OpenGet.gif" attr="1" id="idImgOpenGet"></td>
    <td onaction="restoreSource()" title="<%=bundle.getString("lb_editor_segment_restore_src") %>"
      ><img src="/globalsight/envoy/edit/online2/RestoreSource.gif" attr="1" id="idImgRestoreSource"></td>
    <td onaction="if (save()) openNext(true)" title="<%=bundle.getString("lb_editor_segment_save_next") %>"
      ><img src="/globalsight/envoy/edit/online2/SetCloseNextOpenGet.gif" attr="1" id="idImgSetCloseNextOpenGet"></td>
    <td onaction="save()" title="<%=bundle.getString("lb_editor_segment_save_close") %>"
      ><img src="/globalsight/envoy/edit/online2/SetClose.gif" attr="1" id="idImgSetClose"></td>
    <td onaction="Close()" title="<%=bundle.getString("lb_editor_segment_close_nosave") %>"
      ><img src="/globalsight/envoy/edit/online2/Close.gif" attr="1" id="idImgClose"></td>
    <td width="20px"></td>
    <td id="idButSpellCheck" onaction="spellCheck();" title="<%=bundle.getString("lb_spellcheck") %>"
      ><img src="/globalsight/envoy/edit/online2/Spellcheck2.gif" id="idImgSpellcheck"></td>
    <td id="idButUndo" onaction="makeUndo()" title="<%=bundle.getString("lb_undo") %>"
      ><img src="/globalsight/envoy/edit/online2/Undo.gif" id="idImgUndo"></td>
    <td id="idButRedo" onaction="makeRedo()" title="<%=bundle.getString("lb_redo") %>"
      ><img src="/globalsight/envoy/edit/online2/Redo.gif" id="idImgRedo"></td>
    <td id="idButBold" onaction="makeBold()" title="<%=bundle.getString("lb_bold") %>"
      ><img src="/globalsight/envoy/edit/online2/Bold.gif" id="idImgBold"></td>
    <td id="idButItalic" onaction="makeItalic()" title="<%=bundle.getString("lb_italic") %>"
      ><img src="/globalsight/envoy/edit/online2/Italic.gif" id="idImgItalic"></td>
    <td id="idButUnderline" onaction="makeUnderline()" title="<%=bundle.getString("lb_underline") %>"
      ><img src="/globalsight/envoy/edit/online2/Underline.gif" id="idImgUnderline"></td>
    <td id="idButBr" onaction="addBr()" title="<%=bundle.getString("lb_editor_insert_linebr") %>"
      ><img src="/globalsight/envoy/edit/online2/Br.gif" id="idImgBr"></td>
    <td id="idButNbsp" onaction="addNbsp()" title="<%=bundle.getString("lb_editor_insert_nonbreaking_space") %>"
      ><img src="/globalsight/envoy/edit/online2/Nbsp.gif" id="idImgNbsp"></td>
    <td id="idButPtags" onaction="showPTags()" title="<%=bundle.getString("lb_editor_insert_ptag") %>"
      ><img src="/globalsight/envoy/edit/online2/PTag.gif" id="idImgPTags"></td>
    <td id="idBrackets" onaction="addBrackets()" title="<%=bundle.getString("lb_editor_square_brackets") %>"
      ><img src="/globalsight/envoy/edit/online2/Brackets.gif" id="idImgBrackets"></td>     
    <td id="idButCr" onaction="addCr()" title="<%=bundle.getString("lb_editor_carriage_return") %>" 
      ><img src="/globalsight/envoy/edit/online2/Cr1.gif" id="idImgCr"></td>
    <td id="idButDetails" onaction="showDetails()" title="<%=bundle.getString("lb_editor_show_segment_details") %>"
      ><img src="/globalsight/envoy/edit/online2/Details.gif" id="idImgDetails"></td>
    <td width="25px"></td>
    <td id="idButMerge" onaction="mergeSegments()" title="<%=bundle.getString("lb_editor_merge_segments") %>"
      ><img src="/globalsight/envoy/edit/online2/Merge.gif" id="idImgMerge"></td>
    <td id="idButSplit" onaction="splitSegments()" title="<%=bundle.getString("lb_editor_split_segments") %>"
      ><img src="/globalsight/envoy/edit/online2/Split.gif" id="idImgSplit"></td>
    <td width="25px"></td>
    <td id="idButTm" onaction="showTmWindow();" title="<%=bundle.getString("lb_editor_show_tm_window") %>"
      ><img src="/globalsight/envoy/edit/online2/TM.gif" attr="1" id="idImgTm"></td>

    <td id="idButMt" onaction="translate();" title="<%=bundle.getString("lb_editor_translate_with_mt") %>"
      style="display: none"><img src="/globalsight/envoy/edit/online2/MT.gif" id="idImgMT"></td>

    <td id="idButLtr" onaction="addLre()" title="<%=bundle.getString("lb_editor_lre") %>"
      ><img src="/globalsight/envoy/edit/online2/Ltr.gif" id="idImgLtr"></td>

    <td id="idButRtl" onaction="addPdf()" title="<%=bundle.getString("lb_editor_pdf") %>"
      ><img src="/globalsight/envoy/edit/online2/Rtl.gif" id="idImgRtl"></td>
    <td>
	    <SELECT ID="PTagBox" NAME="PTagBox" CLASS="standardText"
	      onchange="DoChangePTags(this)">
			<%
			  if (b_ptagsVerbose)
			  { 
			    out.print("<OPTION name=compact>");
			    out.print(lb_compactTags);
			    out.print("</OPTION>");
			    out.print("<OPTION name=verbose selected>");
			    out.print(lb_verboseTags);
			    out.print("</OPTION>");
			  }
			  else
			  {
			    out.print("<OPTION name=compact selected>");
			    out.print(lb_compactTags);
			    out.print("</OPTION>");
			    out.print("<OPTION name=verbose>");
			    out.print(lb_verboseTags);
			    out.print("</OPTION>");
			  }
			%>
        </SELECT>
  </td>
  </tr>
</table>
</div>

<div id="idSegments" style="position: absolute;
  top: 70; left: 10;
  width: expression(idBody.clientWidth - 20);
  height: expression(idBody.clientHeight - 75);
  border: 1px solid black; overflow: auto; padding: 8px;">
<%=str_pageHtml%>
</DIV>

<APPLET
  style="display:inline"
  archive="/globalsight/applet/lib/online.jar"
  code="com.globalsight.ling.tw.online.OnlineApplet"
  id="applet"
  name="applet"
  width="0"
  height="0" MAYSCRIPT>
</APPLET>
<iframe id="idDummy" name="idDummy" src ="about:blank" style="display:none"></iframe>
<iframe id="idShutdown" name="idShutdown" src ="about:blank" style="display:none"></iframe>

<FORM name="ShutdownForm" METHOD="GET" TARGET="idShutdown"
 ACTION="/globalsight/envoy/common/shutdownPopup.jsp">
<INPUT TYPE="hidden" NAME="a" VALUE="">
</FORM>

<FORM name="SaveForm" METHOD="POST" ACTION="<%=url_save%>" TARGET="idDummy">
<INPUT TYPE="hidden" NAME="save" VALUE="">
<INPUT TYPE="hidden" NAME="tuId" VALUE="">
<INPUT TYPE="hidden" NAME="tuvId" VALUE="">
<INPUT TYPE="hidden" NAME="subId" VALUE="">
</FORM>

<FORM name="SplitMergeForm" METHOD="POST" ACTION="<%=url_splitmerge%>">
<INPUT TYPE="hidden" NAME="action" VALUE="">
<INPUT TYPE="hidden" NAME="tuv1" VALUE="">
<INPUT TYPE="hidden" NAME="tuv2" VALUE="">
<INPUT TYPE="hidden" NAME="location" VALUE="">
<INPUT TYPE="hidden" NAME="<%=WebAppConstants.TASK_ID%>" VALUE="<%=taskId%>">
</FORM>
    <div id = "selectPtag" style='border-style:solid;border-width:1pt; border-color:#0c1476;background-color:white;left:300px;width:300px;height:180px;position:absolute;top:100px;display:block;z-index:1000; visibility:hidden;'>
    	<div id='selectPtagDialog' onmousedown="Drag.init('selectPtag', '1000')" onmouseup ="Drag.release()" style='border-style:solid;border-width:1pt;background-color:#0c1476;width:100%;cursor:hand'>
           <label class='whiteBold' style='color:white;font-family:Arial,Helvetica,sans-serif;font-size:8pt;'>
                               <%=lb_select_ptag1%>
           </label>
        </div>
		<DIV id="idTags">
			<%=lb_select_ptag1%>
			<TABLE ALIGN="center" id="idTable" CELLSPACING="10"></TABLE>
		</DIV>
		<P ALIGN="center" style = "margin-top:30px;">
				<BUTTON ONCLICK="hideSelectPtag();"><%=lb_close%></BUTTON>
		</P>
 	</div>

<form name="frmSC"><input type="hidden" name="language" value=""></form>
<script>
doInit();
</script>

</body>
</html>
