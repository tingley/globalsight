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
            com.globalsight.everest.edit.online.OnlineEditorConstants,
            com.globalsight.everest.edit.SynchronizationStatus,
            com.globalsight.everest.page.pageexport.ExportConstants,
            com.globalsight.everest.util.system.SystemConfiguration,
            com.globalsight.everest.servlet.util.SessionManager,
            com.globalsight.everest.webapp.pagehandler.projects.workflows.JobManagementHandler,
            com.globalsight.util.edit.EditUtil,
            com.globalsight.everest.util.system.SystemConfigParamNames,
            java.io.File,
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
String b_refreshSource = (String) request.getAttribute("refreshSource");
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

String lb_title = bundle.getString("lb_segment_details");
String lb_details = lb_title;

String lb_segmentId = bundle.getString("lb_segmentId");
String lb_segmentFormat = bundle.getString("lb_segmentFormat");
String lb_segmentType = bundle.getString("lb_segmentType");
String lb_wordCount = bundle.getString("lb_totalWordCount");
String lb_close = bundle.getString("lb_close");
String lb_tagInfo = bundle.getString("lb_tagInfo");

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

String selSegFilter = (String)request.getAttribute("segmentFilter");
StringBuffer str_segmengFilter = new StringBuffer();
str_segmengFilter.append(bundle.getString("segment_filter")).append(":&nbsp;&nbsp;");
str_segmengFilter.append("<select id='segmentFilter' ");
str_segmengFilter.append("onchange='doSegmentFilter(this[this.selectedIndex].value)' ");
str_segmengFilter.append("style='font-size: 8pt;'>");
for(String segFilter : OnlineEditorConstants.SEGMENT_FILTERS)
{
    str_segmengFilter.append("<option ");
    if (segFilter.equals(selSegFilter))
    {
        str_segmengFilter.append("selected ");
    }
    str_segmengFilter.append("value=\""+segFilter+"\">")
                     .append(bundle.getString(segFilter))
                     .append("</option>");
}
str_segmengFilter.append("</select>");

StringBuffer tHead = new StringBuffer();
tHead.append("<table WIDTH='100%' CELLSPACING='0' class='tableHeadingGray'>");
tHead.append("<tr><td>");
tHead.append(lb_segment).append("</td><td align='right'>");
tHead.append(str_segmengFilter);
tHead.append("</td></tr></table>");

SystemConfiguration systemConfig = SystemConfiguration.getInstance();
String gsHome = systemConfig.getStringParameter(SystemConfigParamNames.GLOBALSIGHT_HOME_DIRECTORY);
String jsPath = gsHome + "\\jboss\\server\\standalone\\deployments\\globalsight.ear\\globalsight-web.war\\javaScriptClient\\sortIntegrationTarget.js";
File jsFile = new File(jsPath);
%>
<HTML xmlns:gs>
<!-- This is envoy\edit\online\me_target.jsp -->
<HEAD>
<SCRIPT SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<SCRIPT src="/globalsight/envoy/terminology/viewer/error.js" defer></SCRIPT>
<SCRIPT src="/globalsight/envoy/edit/snippets/snippet.js" defer></SCRIPT>
<SCRIPT src="/globalsight/envoy/edit/online/editsnippets.js" defer></SCRIPT>
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
.alt { background:#EEEEEE;} 
.firsttd {height:'23';width:'32'} 
.lbid{height:'23';width:'3'} 
.repstyle{ALIGN:'CENTER'}
pre {
    font-family: Arial, Helvetica, sans-serif;
    font-size: 10pt;
}
</STYLE>
<script type="text/javascript" src="/globalsight/jquery/jquery-1.6.4.min.js"></script>
<%if(jsFile.exists()){ %>
<link href="/globalsight/javaScriptClient/jqueryUI/css/smoothness/jquery-ui-1.9.1.custom.min.css" rel="stylesheet"></link>
<script src="/globalsight/javaScriptClient/jqueryUI/js/jquery-1.8.2.min.js"></script>
<script src="/globalsight/javaScriptClient/jqueryUI/js/jquery-ui-1.9.1.custom.min.js"></script>
<script src="/globalsight/javaScriptClient/sortIntegrationTarget.js" ></script>
<%} %>
<SCRIPT>

HighlightNormal();
var isReviwMode=<%=state.isReviewMode()%>;
var segFilter="<%=selSegFilter%>";
var modeId="<%=i_viewMode %>";
var modeFrom = "target";
var jsonUrl=this.location+"&dataFormat=json"+"&trgViewMode=" + modeId+"&random="+Math.random();
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
var g_refreshSource = eval("<%="true".equalsIgnoreCase(b_refreshSource)%>");

var g_syncMessage =
  "<%=syncMessage != null ? EditUtil.toJavascript(syncMessage) : ""%>";
var g_syncClose = eval("<%=syncClose%>");

var w_editor = null;

var segmentEditorHeight = "540";

var showFinish = false;






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
    main.localData=null;
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
    main.localData=null;
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
    main.localData=null;
    o_form.submit();
}

function Refresh()
{
    var o_form = document.RefreshForm;

    document.body.style.cursor = "wait";

    sendCurrentSegment(o_form, o_currentSegment);
    main.localData=null;
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

function showDetails(tuId, tuvId, subId){
    if (!CanClose())
    {
        cancelEvent();
        RaiseEditor();
    }
    else
    {
        forceCloseEditor('segment');
        hideContextMenu();
     show(tuId, tuvId, subId);
    }
}

function show(tuId, tuvId, subId){
    var detail = "seg"+tuId+"_"+tuvId+"_"+subId;
    var Y = ($(window).height())/2;
    var X = ($(window).width())/2;
    var scrollTop = $(document).scrollTop();     
    var scrollLeft = $(document).scrollLeft(); 
    $("#showDetails").css({top:Y+scrollTop,left:X+scrollLeft});
    $("#showDetails").html("");
    var str_url = "<%=url_refresh%>" ;
    var str = jsonUrl + str_url;
    var details = "tuId=" + tuId + "&tuvId=" + tuvId + "&subId=" + subId;
    $.post(str,{param:details},function(data){
        var result = eval("("+data+")");
        $("#showDetails").html('<div id="details-title" onmousedown="Milan_StartMove(event,this.parentNode)" onmouseup="Milan_StopMove(event)" style="background:#0C1476; text-align:right;cursor:move;">'
        +'<div><a href="##" style="color:#FFF" onclick="closeDetails()"><%=lb_close%></a></div></div><div style="position: absolute;overflow:auto;background-color:#FFFFFF;width:400px;height:240px"><table cellspacing="5" cellpadding="2" border="0" style="table-layout:fixed;">'
        +'<tr class="standardText"><td noWrap colspan="2"><span class="mainHeading"><%=lb_title%></span></td></tr>'
        +'<tr class="standardText"><td noWrap style="width:100px;"><B><%=lb_segmentId%>:</B></td><td>'+ result.str_segmentId +'</td></tr><tr class="standardText"><td noWrap><B><%=lb_segmentFormat%>:</B></td><td>'+result.str_segmentFormat+'</td></tr>'
        +'<tr class="standardText"><td noWrap><B><%=lb_segmentType%>:</B></td><td>'+ result.str_segmentType +'</td></tr><tr class="standardText"><td><B><%=lb_wordCount%>:</B></td><td>'+ result.str_wordCount +'</td></tr>'
        +'<tr class="standardText"><td noWrap><B><%=lb_tagInfo%>:</B></td><td><table border="0">'+ result.str_segementPtag +'</table></td></tr>'
        +'<tr class="standardText"><td noWrap><B><%=bundle.getString("lb_sid")%>:</B></td><td>'+ result.str_sid +'</td></tr><tr class="standardText"><td><B><%=bundle.getString("lb_modify_by")%>:</B></td><td>'+ result.str_lastModifyUser +'</td></tr></table></div>');
     });
     $("#showDetails").show();       
}

function Milan_StartMove(oEvent,div_id)
{
    var whichButton;
    if(document.all&&oEvent.button==1) whichButton=true;
    else { if(oEvent.button==0)whichButton=true;}
    if(whichButton)
    {
        var oDiv=div_id;
        offset_x=parseInt(oEvent.clientX-oDiv.offsetLeft);
        offset_y=parseInt(oEvent.clientY-oDiv.offsetTop);
        document.documentElement.onmousemove=function(mEvent)
        {   
            var eEvent;
            if(document.all) eEvent=event;
            else{eEvent=mEvent;}
            var oDiv=div_id;
            var x=eEvent.clientX-offset_x;
            var y=eEvent.clientY-offset_y;
            oDiv.style.left=(x)+"px";
            oDiv.style.top=(y)+"px";
            var d_oDiv=document.getElementById("disable_"+oDiv.id);
            d_oDiv.style.left=(x)+"px";
            d_oDiv.style.top=(y)+"px";
        }
    }
}

function Milan_StopMove(oEvent){
    document.documentElement.onmousemove=null; 
    }

function closeDetails(){
    $("#showDetails").css("display", "none");
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
          "width=550,height=750,top=100,left=100"); //resizable,
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
          "&refresh=0&releverage=false";
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
    return (obj.tagName == 'A' && ((obj.className.indexOf('editorSegment') != -1)||(obj.className.indexOf('segmentContext')!=-1)));
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
    popupoptions.push(new ContextItem("Segment details",
            function(){showDetails(ids[0], ids[1], ids[2])}));
    
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

$(window).ready(function(){
    $(window).scroll(function(){
        <%=str_scrollHandler%>
    }); 
});

function doScroll()
{
    if (!otherPane) otherPane = parent.parent.source;
    if (!pageToScroll) pageToScroll = otherPane.content;

    if (parent.mode == otherPane.mode)
    {
      pageToScroll.scroll(
        self.document.body.scrollLeft, self.document.body.scrollTop);
    }
    
    resetLocation("tgt_prograssbar");
}

<%--
  The value of str_tuvIds will be set by a script inserted into the
template. The string looks like "&TuvId=103&TuvId=104&TuvId=105...".
--%>
var pv_height = screen.height;
var pv_width =  screen.width * .98;

function showProgressBar()
{
   try
   {
       var div = document.getElementById('tgt_prograssbar');
       div.style.visibility = "visible";
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
    //if (pageToScroll)
    //{
    //    try
    //    {
    //        pageToScroll.document.execCommand("Refresh");
    //    }catch(e)
    //    {
    //    }
    //}
    
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

    // Update file/page navigation arrows on me_menu.jsp
    // But seems the codes are not quite reliable.
    parent.parent.parent.menu.updateFileNavigationArrow();
    parent.parent.parent.menu.updatePageNavigationArrow();
    setTimeout("checkMenuLoadingFinished()", 2000);
}

function checkMenuLoadingFinished()
{
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

function doSegmentFilter(p_segmentFilter)
{
    parent.parent.parent.SegmentFilter(p_segmentFilter);
}
</SCRIPT>
<script type="text/javascript" src="/globalsight/jquery/me_table.js"></script>
<style type="text/css">
<!--
#tgt_prograssbar {
    visibility: hidden;
    position: absolute;
    background-color: white;
    width: 500px;
    height: 50px;
    text-align: center;
    vertical-align: middle;
    border: 1px solid #ddd;
}
-->
</style>

<script type="text/javascript">
<!--
    function resetLocation(p_eid) {
        try {
            var diffY = 0;
            if (document.documentElement && document.documentElement.scrollTop)
                diffY = document.documentElement.scrollTop;
            else if (document.body)
                diffY = document.body.scrollTop;
            else {/*Netscape stuff*/
            }
            document.getElementById(p_eid).style.top = diffY + 300;
        } catch(e) {
        }
    }
//-->
</script>

</HEAD>
<BODY id="idBody" oncontextmenu="contextForX(event)"
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
            
<div id="tgt_prograssbar" style="top: 300px; left: 100px;">
    <br /> <img alt="<%=lb_loadingPreview %>" src="/globalsight/includes/loading.gif"> <%=lb_loadingPreview %> <br />
</div>

<% if (i_viewMode == EditorConstants.VIEWMODE_DETAIL) { %>
<TABLE WIDTH="100%" CELLSPACING="0" CELLPADDING="3" BORDER="1"
 style="border-color: lightgrey; border-collapse: collapse; border-style: solid; border-width: 1px;
        font-family: Arial,Helvetica,sans-serif; font-size: 10pt;">
  <COL WIDTH="1%" VALIGN="TOP" CLASS="editorId" ID="editorId" NOWRAP>
  <COL WIDTH="99%" VALIGN="TOP" CLASS="editorText">

  <THEAD>
    <TR CLASS="tableHeadingGray">
      <TD ALIGN="CENTER" class="lbid"><%=lb_id%></TD>
      <TD ALIGN="LEFT" class="lcid"><%=tHead.toString()%></TD>
    </TR>
  </THEAD>
  <TBODY id="idPageHtml"><%=str_pageHtml%></TBODY>
</TABLE>
<div id="showDetails" style="display:none;position: absolute;z-index:999;width:400px;height:260px;border-style:ridge;border-width:5pt; border-color:#0C1476">
</div>
<% } else { %>
<DIV id="idPageHtml" style="font-family: Arial,Helvetica,sans-serif; font-size: 10pt;">
    <%=str_pageHtml%>
</DIV>
<% } %>

<script type="text/javascript">
if (g_refreshSource)
{
    try
    {
       var segFilter = document.getElementById("segmentFilter");
       doSegmentFilter(segFilter[segFilter.selectedIndex].value);
    }
    catch (ignore)
    {
    }
}
</script>
</BODY>
</HTML>