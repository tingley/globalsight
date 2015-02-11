<%@ page
    contentType="text/html; charset=UTF-8"
    errorPage="error.jsp"
    import="com.globalsight.util.resourcebundle.ResourceBundleConstants,
            com.globalsight.everest.webapp.WebAppConstants,
            com.globalsight.everest.webapp.javabean.NavigationBean,
            com.globalsight.everest.webapp.pagehandler.PageHandler,
            com.globalsight.everest.webapp.pagehandler.edit.online.EditorState,
            com.globalsight.everest.webapp.pagehandler.edit.online.EditorConstants,
            com.globalsight.everest.webapp.pagehandler.tasks.TaskHelper,
            com.globalsight.everest.edit.SynchronizationStatus,
            com.globalsight.everest.page.pageexport.ExportConstants,
            com.globalsight.everest.util.system.SystemConfiguration,
            com.globalsight.everest.servlet.util.SessionManager,
            com.globalsight.util.edit.EditUtil,
            java.util.*"
    session="true"
%>
<%!
static String URL_PREVIEW = "/globalsight/CapExportServlet?";
static {
    try
    {
        SystemConfiguration sc = SystemConfiguration.getInstance();
        URL_PREVIEW = sc.getStringParameter(
            SystemConfiguration.CAP_SERVLET_URL);
        if (!URL_PREVIEW.endsWith("?"))
        {
            URL_PREVIEW = URL_PREVIEW + "?";
        }
    }
    catch (Throwable e)
    {
        // well, what do we say to that?
    }
}
%>
<jsp:useBean id="refreshSelf" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean"/>
<jsp:useBean id="segmentEditor" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean"/>
<jsp:useBean id="commentEditor" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean"/>
<%
ResourceBundle bundle = PageHandler.getBundle(session);

SessionManager sessionMgr = (SessionManager)session.getAttribute(
  WebAppConstants.SESSION_MANAGER);
EditorState state =
  (EditorState)sessionMgr.getAttribute(WebAppConstants.EDITORSTATE);
EditorState.Layout layout = state.getLayout();
EditorState.LinkStyles styles = state.getLinkStyles();

Locale uiLocale = (Locale)session.getAttribute(WebAppConstants.UILOCALE);

boolean b_refreshOther = (request.getAttribute("cmtRefreshOtherPane") != null);

// Can't use GET-style url, see the forms below.
String url_refresh = refreshSelf.getPageURL();
String url_segmentEditor = segmentEditor.getPageURL();
String url_commentEditor = commentEditor.getPageURL();

String lb_id = bundle.getString("lb_id");
String lb_segment = bundle.getString("lb_segment");
String lb_comment = "Comment" /*bundle.getString("lb_comment")*/;
String lb_loadingPreview = bundle.getString("lb_loading_preview");

int i_viewMode = layout.getTargetViewMode();
boolean b_singlePage = layout.isSinglePage();
boolean b_autoSync = state.getOptions().getAutoSync();
String str_pageHtml = state.getTargetPageHtml();
String str_targetLocale = state.getTargetLocale().toString();
String str_targetViewLocale = state.getTargetViewLocale().toString();
String str_displayLocale = state.getTargetViewLocale().getDisplayName();
long l_targetPageId = state.getTargetPageId().longValue();
boolean b_isReviewActivity = state.getIsReviewActivity();
boolean b_readOnly = state.isReadOnly();

Boolean assigneeValue = (Boolean)TaskHelper.retrieveObject(
   session, WebAppConstants.IS_ASSIGNEE);
boolean isAssignee = assigneeValue == null ? true :
   assigneeValue.booleanValue();
boolean disableComment = isAssignee && b_readOnly;

long lastTuId  = state.getTuId();
long lastTuvId = state.getTuvId();
long lastSubId = state.getSubId();
String needUpdatePopupEditor = state.getNeedUpdatePopUpEditor();

String str_scrollHandler = "";
if (!b_singlePage && b_autoSync)
{
    str_scrollHandler = "doScroll();";
}

SynchronizationStatus oldStatus = state.getOldSynchronizationStatus();
SynchronizationStatus newStatus = state.getNewSynchronizationStatus();
String syncMessage = null;
boolean syncClose = false;

if (newStatus != null)
{
    String status = newStatus.getStatus();
    if (status.equals(SynchronizationStatus.UPLOAD_FINISHED))
    {
      syncMessage = "Offline translations were uploaded for this page.\n" +
        "Close and re-open the editor.";
      // re-open will clear status
    }
    else if (status.equals(SynchronizationStatus.GXMLUPDATE_STARTED) ||
             status.equals(SynchronizationStatus.GXMLUPDATE_FINISHED))
    {
      syncMessage = "The source page is being edited.\n\n" +
        "The editor will close now. Please wait for the PM to unlock the page.";
      syncClose = true;
      // re-open will clear status
    }
    else if (status.equals(oldStatus.getStatus()))
    {
      // Really: previous upload has finished and new upload has started.
      syncMessage = "Offline translations were uploaded for this page.\n" +
        "Close and re-open the editor.";
      // reload will clear status
    }
    else
    {
      syncMessage = "Offline translations are being uploaded for this page.\n" +
        "You should stop editing and wait until upload has finished.";
      // Show message once.
      state.setOldSynchronizationStatus(newStatus);
      state.setNewSynchronizationStatus(null);
    }
}
%>
<HTML xmlns:gs>
<!-- This is envoy\edit\online\me_target.jsp -->
<HEAD>
<SCRIPT SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<SCRIPT src="/globalsight/envoy/terminology/viewer/error.js" defer></SCRIPT>
<SCRIPT src="/globalsight/envoy/edit/snippets/snippet.js" defer></SCRIPT>
<SCRIPT src="/globalsight/envoy/edit/online/editsnippets.js" defer></SCRIPT>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/tooltip.js"></SCRIPT>
<link rel="STYLESHEET" type="text/css" href="/globalsight/includes/ContextMenu.css">
<script src="/globalsight/includes/ContextMenu.js"></script>
<link type="text/css" rel="StyleSheet" id="cssEditor"
  href="/globalsight/envoy/edit/online/editor.css">
<link type="text/css" rel="StyleSheet" id="cssEditorTouched" href="/globalsight/envoy/edit/online/editorTouched.css">
<link type="text/css" rel="StyleSheet" id="cssEditorTranslated" href="/globalsight/envoy/edit/online/editorTranslated.css">
<link type="text/css" rel="StyleSheet" id="cssEditorUntranslated" href="/globalsight/envoy/edit/online/editorUntranslated.css">
<STYLE>
.editorComment { cursor: hand;cursor:pointer; }

.GSposition,
.GSadded,
.GSdelete,
.GSdeleted    {
                behavior: url("/globalsight/envoy/edit/snippets/gs-tag.htc");
                display: block;
                cursor: hand;
				cursor:pointer;
              }
.GSaddedGeneric {
                behavior: url("/globalsight/envoy/edit/snippets/gs-tag.htc");
                display: block;
              }

<% if (layout.getTargetViewMode() == EditorConstants.VIEWMODE_PREVIEW) { %>
A         { color: <%=styles.m_A_color%>; text-decoration: none}
A:link    { color: <%=styles.m_A_color%>; }
A:hover   { color: <%=styles.m_A_color%>; }
A:active  { color: <%=styles.m_A_active%>; }
A:visited { color: <%=styles.m_A_visited%>; }
<% } else { %>
A         { color: blue; text-decoration: none}
A:link    { color: blue; }
A:hover   { color: blue; }
A:active  { color: blue; }
A:visited { color: blue; }
<% } %>

pre {
	font-family: Arial, Helvetica, sans-serif;
	font-size: 10pt;
}
</STYLE>
<SCRIPT>
HighlightNormal();

var g_targetLocale = "<%=str_targetLocale%>";

var g_lastTuId  = "<%=lastTuId%>";
var g_lastTuvId = "<%=lastTuvId%>";
var g_lastSubId = "<%=lastSubId%>";

var g_targetViewLocale = "<%=str_targetViewLocale%>";
var g_displayLocale = "<%=str_displayLocale%>";

var g_reviewMode = eval("<%=state.isReviewMode()%>");
var g_isReviewActivity = eval("<%=b_isReviewActivity%>");
var g_readOnly = eval("<%=b_readOnly%>");
var g_disableLink = eval("<%=disableComment%>");

var g_syncMessage =
  "<%=syncMessage != null ? EditUtil.toJavascript(syncMessage) : ""%>";
var g_syncClose = eval("<%=syncClose%>");

var w_editor = null;

var segmentEditorHeight = "540";
if (screen.availHeight > 600)
{
    segmentEditorHeight = screen.availHeight - 60;
}

if (eval("<%=b_refreshOther%>"))
{
    try
    {
        window.top.RefreshCommentPane();
    }
    catch (ignore)
    {
    }
}

function getSegment(tuId, tuvId, subId)
{
    var id = "seg" + tuId + "_" + tuvId + "_" + subId;

    return document.getElementById(id);
}

var str_color = "#9932CC";
var o_currentSegment = null;
var o_oldColor = null;

function highlightSegment(o)
{
    if (o != null)
    {
        o.style.border = "2px solid " + str_color;
        o.scrollIntoView(true);
    }
}

function unhighlightSegment(o)
{
    if (o != null)
    {
        o.style.border = "none";
    }
}

function HighlightSegment(tuId, tuvId, subId)
{
    var o = getSegment(tuId, tuvId, subId);

    if (o_currentSegment != null)
    {
        unhighlightSegment(o_currentSegment);
    }

    if (o != null)
    {
        highlightSegment(o);
    }

    o_currentSegment = o;
}

function UnhighlightSegment(tuId, tuvId, subId)
{
    var o = getSegment(tuId, tuvId, subId);

    unhighlightSegment(o);

    o_currentSegment = o;
}

function SaveSegment(tuId, tuvId, subId, segment, ptagFormat)
{
    var o_form = document.SaveForm;

    document.body.style.cursor = "wait";

    o_form.save.value    = segment;
    o_form.refresh.value = "0";
    o_form.tuId.value    = tuId;
    o_form.tuvId.value   = tuvId;
    o_form.subId.value   = subId;
    o_form.ptags.value   = ptagFormat;

    o_form.submit();
}

function SaveComment2(tuId, tuvId, subId, action, title, comment, priority, status, category, share, overwrite)
{
	var o_form = document.CommentForm;

    o_form.tuId.value = tuId;
    o_form.tuvId.value = tuvId;
    o_form.subId.value = subId;
    o_form.cmtAction.value = action;
    o_form.cmtTitle.value = title;
    o_form.cmtComment.value = comment;
    o_form.cmtPriority.value = priority;
    o_form.cmtStatus.value = status;
    o_form.cmtCategory.value = category;
    o_form.cmtShare.value = share;
    o_form.cmtOverwrite.value = overwrite;

    o_form.submit();
}

function SaveComment(tuId, tuvId, subId, action, title, comment, priority, status, category)
{
    var o_form = document.CommentForm;

    o_form.tuId.value = tuId;
    o_form.tuvId.value = tuvId;
    o_form.subId.value = subId;
    o_form.cmtAction.value = action;
    o_form.cmtTitle.value = title;
    o_form.cmtComment.value = comment;
    o_form.cmtPriority.value = priority;
    o_form.cmtStatus.value = status;
    o_form.cmtCategory.value = category;

    o_form.submit();
}

function Refresh()
{
    var o_form = document.RefreshForm;

    document.body.style.cursor = "wait";

    sendCurrentSegment(o_form, o_currentSegment);

    o_form.submit();
}

function sendCurrentSegment(o_form, o_currentSegment)
{
    if (o_currentSegment)
    {
        var id = o_currentSegment.id;
        id = id.substring(3);

        var parts = id.split("_");

        o_form.curTuId.value  = parts[0];
        o_form.curTuvId.value = parts[1];
        o_form.curSubId.value = parts[2];
    }
}

function SwitchTargetLocale(trgLocale)
{
    var o_form = document.SwitchTargetLocaleForm;

    document.body.style.cursor = "wait";

    o_form.trgViewLocale.value = trgLocale;
    o_form.submit();
}

function cancelEvent()
{
    if (window.event != null)
    {
        window.event.returnValue = false;
        window.event.cancelBubble = true;
    }
}

function CanClose()
{
    if (w_editor != null && !w_editor.closed)
    {
        return w_editor.CanClose();
    }

    if (g_reviewMode)
    {
        return window.top.CanCloseReview();
    }

    return true;
}

function RaiseEditor()
{
    if (w_editor != null && !w_editor.closed)
    {
        w_editor.RaiseEditor();
    }
    else if (g_reviewMode)
    {
        window.top.RaiseCommentEditor();
    }
}

// Public method for me_main/me_menu: close either segment or comment editor.
function ForceCloseEditor()
{
    try { w_editor.close(); } catch (ignore) {}
}

function forceCloseEditor(p_type)
{
    if (w_editor != null && !w_editor.closed && w_editor.EditorType == p_type)
    {
        try { w_editor.close(); } catch (ignore) {}
    }

    if (p_type == 'comment')
    {
        window.top.ForceCloseCommentEditor();
    }
}

function editComment(tuId, tuvId, subId)
{
    if (!CanClose())
    {
        cancelEvent();
        RaiseEditor();
    }
    else
    {
        var str_url = "<%=url_commentEditor%>" +
          "&tuId=" + tuId + "&tuvId=" + tuvId + "&subId=" + subId + "&refresh=0";

        forceCloseEditor('segment');

        hideContextMenu();

        w_editor = window.open(str_url, "CommentEditor",
          "width=550,height=610,top=100,left=100"); //resizable,
    }
}

function editSegment(tuId, tuvId, subId)
{
    if (!CanClose())
    {
        cancelEvent();
        RaiseEditor();
    }
    else
    {
        var str_url = "<%=url_segmentEditor%>" +
          "&tuId=" + tuId + "&tuvId=" + tuvId + "&subId=" + subId +
          "&refresh=0&releverage=true";

        forceCloseEditor('comment');

        hideContextMenu();

        w_editor = window.open(str_url, "SegmentEditor",
          "resizable,width=560,height=" + segmentEditorHeight +
           ",top=0,left=0");
    }
}

function SE(tuId, tuvId, subId, p_forceComment)
{
    if (g_disableLink)
    {
        return;
    }
    if (g_reviewMode || p_forceComment)
    {
        editComment(tuId, tuvId, subId);
    }
    else
    {
        editSegment(tuId, tuvId, subId);
    }
}

var g_segTotal = 0;
var g_segCnt = 0;

function canEdit(o)
{
    return o.className && o.className.indexOf('editorSegment') == 0;
}

function findPercentageTouched()
{
    g_segTotal = g_segCnt = 0;

    countPercentageTouched(idPageHtml);

    return g_segTotal == 0 ? 0 : Math.round(g_segCnt * 100 / g_segTotal);
}

function countPercentageTouched(p_o)
{
  var o = p_o.firstChild;
  while (o)
  {
    if (canEdit(o))
    {
      g_segTotal++;

      if (o.className == 'editorSegmentUpdated')
      {
        g_segCnt++;
      }
    }

    if (o.childNodes.length > 0)
    {
      countPercentageTouched(o);
    }

    o = o.nextSibling;
  }
}

function findPercentageTranslated()
{
    g_segTotal = g_segCnt = 0;

    countPercentageTranslated(idPageHtml);

    return g_segTotal == 0 ? 0 : Math.round(g_segCnt * 100 / g_segTotal);
}

function countPercentageTranslated(p_o)
{
  var o = p_o.firstChild;
  while (o)
  {
    if (canEdit(o))
    {
      g_segTotal++;

      if (o.className == 'editorSegmentExact' ||
          o.className == 'editorSegmentLocked' ||
          o.className == 'editorSegmentExcluded' ||
          o.className == 'editorSegmentUpdated' ||
          o.className == 'editorSegmentUnverified')
      {
        g_segCnt++;
      }
    }

    if (o.childNodes.length > 0)
    {
      countPercentageTranslated(o);
    }

    o = o.nextSibling;
  }
}

function findPercentageUntranslated()
{
    g_segTotal = g_segCnt = 0;

    countPercentageUntranslated(idPageHtml);

    return g_segTotal == 0 ? 0 : Math.round(g_segCnt * 100 / g_segTotal);
}

function countPercentageUntranslated(p_o)
{
  var o = p_o.firstChild;
  while (o)
  {
    if (canEdit(o))
    {
      g_segTotal++;

      if (o.className == 'editorSegment' ||
          o.className == 'editorSegmentFuzzy' ||
          o.className == 'editorSegmentRepetition')
      {
        g_segCnt++;
      }
    }

    if (o.childNodes.length > 0)
    {
      countPercentageUntranslated(o);
    }

    o = o.nextSibling;
  }
}

function HighlightTouched()
{
    cssEditor.disabled = true;
    cssEditorTouched.disabled = false;
    cssEditorTranslated.disabled = true;
    cssEditorUntranslated.disabled = true;

    return findPercentageTouched();
}

function HighlightTranslated()
{
    cssEditor.disabled = true;
    cssEditorTouched.disabled = true;
    cssEditorTranslated.disabled = false;
    cssEditorUntranslated.disabled = true;

    return findPercentageTranslated();
}

function HighlightUntranslated()
{
    cssEditor.disabled = true;
    cssEditorTouched.disabled = true;
    cssEditorTranslated.disabled = true;
    cssEditorUntranslated.disabled = false;

    return findPercentageUntranslated();
}

function HighlightNormal()
{
    cssEditor.disabled = false;
    cssEditorTouched.disabled = true;
    cssEditorTranslated.disabled = true;
    cssEditorUntranslated.disabled = true;
}

// throws exception to end recursion
function jumpFirstUntranslated(p_o)
{
  var o = p_o.firstChild;
  while (o)
  {
    if (canEdit(o))
    {
      if (o.className == 'editorSegment' ||
          o.className == 'editorSegmentFuzzy' ||
          o.className == 'editorSegmentRepetition')
      {
        if (o_currentSegment != null)
        {
            unhighlightSegment(o_currentSegment);
        }
        highlightSegment(o);
        throw "done";
      }
    }

    if (o.childNodes.length > 0)
    {
      jumpFirstUntranslated(o);
    }

    o = o.nextSibling;
  }
}

// throws exception to end recursion
function jumpFirstFuzzy(p_o)
{
  var o = p_o.firstChild;
  while (o)
  {
    if (canEdit(o))
    {
      if (o.className == 'editorSegmentFuzzy')
      {
        if (o_currentSegment != null)
        {
            unhighlightSegment(o_currentSegment);
        }
        highlightSegment(o);
        throw "done";
      }
    }

    if (o.childNodes.length > 0)
    {
      jumpFirstFuzzy(o);
    }

    o = o.nextSibling;
  }
}

function JumpFirstUntranslated()
{
    try { jumpFirstUntranslated(idPageHtml); } catch (done) {}
}

function JumpFirstFuzzy()
{
    try { jumpFirstFuzzy(idPageHtml); } catch (done) {}
}

function GetSegmentPreview()
{
    // <% if (i_viewMode == EditorConstants.VIEWMODE_DETAIL) { %>
    return "<table><tbody>" + idPageHtml.innerHTML + "</tbody></table>";
    // <% } else { %>
    return idPageHtml.innerHTML;
    // <% } %>
}

function hideContextMenu()
{
    document.getElementById("idBody").focus();
}

function getSegmentIdFromHref(href)
{
    href = href.substring(href.indexOf('(') + 1);
    href = href.substring(0, href.indexOf(')'));
    return href.split(',');
}

function isEditableSegment(obj)
{
    return (obj.tagName == 'A' && ((obj.className.indexOf('editorSegment') == 0)||(obj.className.indexOf('segmentContext')==0)));
}

function getEditableSegment(obj)
{
    while (obj && !isEditableSegment(obj))
    {
        obj = obj.parentElement || obj.parentNode;//Added for Firefox
    }

    return obj;
}

function contextForX(e)
{
    if(!e) e = window.event;

    var o;
    if(window.event)
    {
    o = e.srcElement;
    }
    else
    {
    o = e.target;
    while(o.nodeType != o.ELEMENT_NODE)
	o = o.parentNode;
    }

    o = getEditableSegment(o);

    if (o)
    {
        if (o.className == 'editorSegmentLocked'||o.className == 'segmentContext')
        {
            contextForReadOnly(o, e);
        }
        else
        {
            contextForSegment(o, e);
        }
    }
    else
    {
        //contextForReadOnly();
    }
}

function contextForSegment(obj, e)
{
    if (g_disableLink)
    {
        return false;
    }
    var ids = getSegmentIdFromHref(obj.href);

    var popupoptions;

    // When in a review activity or in viewer mode, only comments are editable.
    if (g_isReviewActivity || g_readOnly)
    {
      popupoptions = [
        new ContextItem("<B>Add/edit comment</B>",
          function(){editComment(ids[0], ids[1], ids[2])})
        ];
    }
    else if (g_reviewMode)
    {
      popupoptions = [
        new ContextItem("Edit segment",
          function(){editSegment(ids[0], ids[1], ids[2])}),
        new ContextItem("<B>Add/edit comment</B>",
          function(){editComment(ids[0], ids[1], ids[2])})
        ];
    }
    else
    {
      popupoptions = [
        new ContextItem("<B>Edit segment</B>",
          function(){editSegment(ids[0], ids[1], ids[2])}),
        new ContextItem("Add/edit comment",
          function(){editComment(ids[0], ids[1], ids[2])})
        ];
    }
    
    ContextMenu.display(popupoptions, e);
}

function contextForReadOnly(obj, e)
{
    var ids = getSegmentIdFromHref(obj.href);

    var popupoptions = [
        new ContextItem("<B>Add/edit comment</B>",
          function(){editComment(ids[0], ids[1], ids[2])})
        ];

/*
    var popupoptions = [
        new ContextItem("Segment is read-only", function(){ hideContextMenu(); })
        ];
*/

    ContextMenu.display(popupoptions, e);
}

function doUnload()
{
    try { w_editor.close(); } catch (ignore) {}
    window.top.CloseProgressWindow();
}

function cancelNavigation()
{
    window.event.returnValue = false;
    window.event.cancelBubble = true;
    return false;
}

function disableLinks()
{
    // disable links that are not editor links
    var links = document.body.getElementsByTagName("A");
    if (links != null)
    {
        var re = new RegExp("javascript:SE\\(", "");
        for (var i = 0; i < links.length; ++i)
        {
            var link = links(i);
            if (link.href != null && link.href != "" && !link.href.match(re))
            {
                link.onclick = cancelNavigation;
                link.style.cursor = "default";
                link.style.textDecoration = "underline";
            }
        }
    }

    var links = document.body.getElementsByTagName("A_DISABLED");
    if (links != null)
    {
        for (var i = 0; i < links.length; ++i)
        {
            var link = links(i);
            link.runtimeStyle.textDecoration = "underline";
        }
    }
}

var otherPane = parent.parent.source;
var pageToScroll = otherPane ? otherPane.content : null;

function doScroll()
{
    if (!otherPane) otherPane = parent.parent.source;
    if (!pageToScroll) pageToScroll = otherPane.content;

    if (parent.mode == otherPane.mode)
    {
      pageToScroll.scroll(
        self.document.body.scrollLeft, self.document.body.scrollTop);
    }
}

<%--
  The value of str_tuvIds will be set by a script inserted into the
template. The string looks like "&TuvId=103&TuvId=104&TuvId=105...".
--%>
var pv_height = screen.height;
var pv_width =  screen.width * .98;
var tooltip = new Tooltip();

function showProgressBar()
{
   try
   {
       tooltip.show("", 100, 300);
       var prograssBarObj = tooltip.tooltip.children[1];
       fakeProgressByTip(0, prograssBarObj.children[0].children[0], prograssBarObj.children[1], "<%=lb_loadingPreview%>");
   }
   catch(e)
   {
   }
}

function Preview(tuvids)
{
    var url = "<%=URL_PREVIEW%>" +
      "<%=ExportConstants.CXE_REQUEST_TYPE%>=<%=ExportConstants.PREVIEW%>" +
      "&<%=ExportConstants.MESSAGE_ID%>=<%=l_targetPageId%>" +
      "&<%=ExportConstants.UI_LOCALE%>=<%=uiLocale.toString()%>" + tuvids;

    var config = "config='height=" + pv_height + ",width=" + pv_width +
      ",toolbar=no,menubar=no,scrollbars=yes,resizable=yes," +
      "location=no,directories=no,status=yes'";

    preview_window = window.open(url,'',config);
    preview_window.screenX = 0;
    preview_window.screenY = 0;
}

function doLoad()
{
    // add javascript to synchronize scroll bars 
    // by segment id in the pop-up editor	
    if (pageToScroll)
    {
        try
        {
             pageToScroll.document.execCommand("Refresh");
        }catch(e)
        {
        }
    }
	
    ContextMenu.intializeContextMenu();

    parent.parent.parent.SetTargetLocaleInfo(
      g_targetViewLocale, g_displayLocale);

    // <% if (layout.getTargetViewMode() == EditorConstants.VIEWMODE_PREVIEW) { %>
    disableLinks();
    // <% } %>

    try
    {
        HighlightSegment(g_lastTuId, g_lastTuvId, g_lastSubId);
        
        var updatePopupEditorFlag = "<%=state.getNeedUpdatePopUpEditor()%>";
        if (updatePopupEditorFlag != null && updatePopupEditorFlag != "null"
            && g_lastTuId != null && g_lastTuId != "0"
            && g_lastTuvId != null && g_lastTuvId != "0" )
        {
            <%state.setNeedUpdatePopUpEditor(null);%>
            editSegment(g_lastTuId, g_lastTuvId, g_lastSubId);
        }
    }
    catch (ignore)
    {}

    if (g_syncMessage)
    {
        alert(g_syncMessage);
    }

    if (g_syncClose)
    {
        window.top.CloseThis();
    }

    // Invoke method on me_menu.jsp
    parent.parent.parent.menu.updateFileNavigationArrow();
    parent.parent.parent.menu.updatePageNavigationArrow();
}

// add javascript to synchronize scroll bars 
// by segment id in the pop-up editor
function update_tr(id) 
{
	var otherPane = parent.parent.source;
	var pageToScroll = otherPane ? otherPane.content : null;
    
    if(pageToScroll) 
    {
		var target_cell;
		var source_cell;		
		if (document.getElementById) 
		{
		    try
		    {
                    	target_cell = document.getElementById(id);
			source_cell = pageToScroll.document.getElementById(id);
		    }catch(e){}
                }
		else if (document.all)
		{
		    try
		    {
                    	target_cell = document.all[id];
			source_cell = pageToScroll.document.all[id];
		    }catch(e){}
                }
			
		if (target_cell && source_cell) 
		{
			if (target_cell.offsetHeight > source_cell.offsetHeight) 
			{
				source_cell.height = target_cell.offsetHeight;
			}
	
			if (target_cell.offsetHeight < source_cell.offsetHeight) 
			{
				target_cell.height = source_cell.offsetHeight;
			}
		}		
	}
	
}

</SCRIPT>
</HEAD>
<BODY id="idBody" onscroll="<%=str_scrollHandler%>" oncontextmenu="contextForX(event)"
 onload="doLoad()" onunload="doUnload()" >

<DIV ID="formdiv" STYLE="DISPLAY: none">
<FORM name="SaveForm" METHOD="POST" ACTION="<%=url_refresh%>">
<INPUT TYPE="hidden" NAME="save" VALUE="">
<INPUT TYPE="hidden" NAME="refresh" VALUE="0">
<INPUT TYPE="hidden" NAME="tuId" VALUE="">
<INPUT TYPE="hidden" NAME="tuvId" VALUE="">
<INPUT TYPE="hidden" NAME="subId" VALUE="">
<INPUT TYPE="hidden" NAME="ptags" VALUE="">
</FORM>
<FORM name="RefreshForm" METHOD="POST" ACTION="<%=url_refresh%>">
<INPUT TYPE="hidden" NAME="refresh" VALUE="0">
<INPUT TYPE="hidden" NAME="curTuId" VALUE="0">
<INPUT TYPE="hidden" NAME="curTuvId" VALUE="0">
<INPUT TYPE="hidden" NAME="curSubId" VALUE="0">
</FORM>
<FORM name="SwitchTargetLocaleForm" METHOD="POST" ACTION="<%=url_refresh%>">
<INPUT TYPE="hidden" NAME="refresh" VALUE="0">
<INPUT type="hidden" NAME="trgViewLocale" VALUE="">
</FORM>
<FORM name="CommentForm" METHOD="POST" action="<%=url_refresh%>">
<input type="hidden" name="tuId"        value="">
<input type="hidden" name="tuvId"       value="">
<input type="hidden" name="subId"       value="">
<input type="hidden" name="cmtAction"   value="">
<input type="hidden" name="cmtTitle"    value="">
<input type="hidden" name="cmtComment"  value="">
<input type="hidden" name="cmtPriority" value="">
<input type="hidden" name="cmtStatus"   value="">
<input type="hidden" name="cmtCategory"   value="">
<input type="hidden" name="cmtShare"   value="">
<input type="hidden" name="cmtOverwrite"   value="">
</FORM>
</DIV>

<%-- Object for the Snippet Editor Dialog - invoked by script. --%>
<div id="idSnippetEditorDialog"
     style="behavior: url('/globalsight/envoy/edit/snippets/SnippetEditor.htc');
            display: none;"></div>

<% if (i_viewMode == EditorConstants.VIEWMODE_DETAIL) { %>
<TABLE WIDTH="100%" CELLSPACING="0" CELLPADDING="3" BORDER="1"
 style="border-color: lightgrey; border-collapse: collapse; border-style: solid; border-width: 1px;
 		font-family: Arial,Helvetica,sans-serif; font-size: 10pt;">
  <COL WIDTH="1%"  VALIGN="TOP" CLASS="editorId" NOWRAP>
  <% if (state.isReviewMode()) { %>
  <COL WIDTH="1%"  VALIGN="TOP" ALIGN="CENTER" NOWRAP>
  <% } %>
  <COL WIDTH="99%" VALIGN="TOP" CLASS="editorText">

  <THEAD>
    <TR CLASS="tableHeadingGray">
      <TD ALIGN="CENTER"><%=lb_id%></TD>
      <% if (state.isReviewMode()) { %>
      <TD ALIGN="CENTER"><img src="/globalsight/images/comment-transparent.gif"></TD>
      <% } %>
      <TD ALIGN="LEFT"><%=lb_segment%></TD>
    </TR>
  </THEAD>
  <TBODY id="idPageHtml"><%=str_pageHtml%></TBODY>
</TABLE>
<% } else { %>
<DIV id="idPageHtml" style="font-family: Arial,Helvetica,sans-serif; font-size: 10pt;">
	<%=str_pageHtml%>
</DIV>
<% } %>

</BODY>
</HTML>