<%@ page
    contentType="text/html; charset=UTF-8"
    errorPage="error.jsp"
    import="com.globalsight.util.resourcebundle.ResourceBundleConstants,
            com.globalsight.util.resourcebundle.SystemResourceBundle,
            com.globalsight.everest.webapp.WebAppConstants,
            com.globalsight.everest.webapp.javabean.NavigationBean,
            com.globalsight.everest.webapp.pagehandler.PageHandler,
            com.globalsight.everest.webapp.pagehandler.tasks.TaskHelper,
            com.globalsight.everest.webapp.pagehandler.edit.online.EditorState,
            com.globalsight.everest.webapp.pagehandler.edit.online.EditorConstants,
            com.globalsight.everest.servlet.util.SessionManager,
            com.globalsight.everest.webapp.pagehandler.projects.workflows.JobManagementHandler,
            com.globalsight.everest.util.system.SystemConfiguration,
            com.globalsight.everest.util.system.SystemConfigParamNames,
            java.io.File,
            java.util.*"
    session="true"
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

String url_segmentEditor = segmentEditor.getPageURL();
String url_commentEditor = commentEditor.getPageURL();
String url_refresh = refreshSelf.getPageURL();

String lb_id = bundle.getString("lb_id");
String lb_segment = bundle.getString("lb_segment");
String lb_loadingPreview = bundle.getString("lb_loading_preview");

int i_viewMode = layout.getSourceViewMode();
boolean b_singlePage = layout.isSinglePage();
boolean b_autoSync = state.getOptions().getAutoSync(); 
String str_pageHtml  = state.getSourcePageHtml(layout.getSourceViewMode());
String str_targetLocale = state.getTargetLocale().toString();
String str_segmentCount = String.valueOf(state.getPageInfo().getSegmentCount());

String str_scrollHandler = "";
if (!b_singlePage && b_autoSync)
{
    str_scrollHandler = "doScroll();";
}

boolean b_isReviewActivity = state.getIsReviewActivity();
boolean b_readOnly = state.isReadOnly();
Boolean assigneeValue = (Boolean)TaskHelper.retrieveObject(
        session, WebAppConstants.IS_ASSIGNEE);
     boolean isAssignee = assigneeValue == null ? true :
        assigneeValue.booleanValue();
boolean disableComment = isAssignee && b_readOnly;

SystemConfiguration systemConfig = SystemConfiguration.getInstance();
String gsHome = systemConfig.getStringParameter(SystemConfigParamNames.GLOBALSIGHT_HOME_DIRECTORY);
String jsPath = gsHome + "\\jboss\\server\\standalone\\deployments\\globalsight.ear\\globalsight-web.war\\javaScriptClient\\sortIntegrationSource.js";
File jsFile = new File(jsPath);
%>
<HTML>
<!-- This is envoy\edit\online\me_source.jsp -->
<HEAD>
<SCRIPT SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<STYLE>
.GSposition,.GSadded,.GSdelete,.GSdeleted {
	behavior: url("/globalsight/envoy/edit/snippets/gs-tag.htc");
	display: block;
	cursor: hand;
	cursor: pointer;
}
.alt { background:#EEEEEE;} 
pre {
	font-family: Arial, Helvetica, sans-serif;
	font-size: 10pt;
}
.searchText {
	background-color : #FFFF00;
}

.editorComment { cursor: hand;cursor:pointer; }

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
<SCRIPT src="/globalsight/envoy/terminology/viewer/error.js" defer></SCRIPT>
<SCRIPT src="/globalsight/envoy/edit/snippets/snippet.js" defer></SCRIPT>
<SCRIPT src="/globalsight/envoy/edit/online/editsnippets.js" defer></SCRIPT>
<script type="text/javascript" src="/globalsight/jquery/jquery-1.6.4.min.js"></script>
<link rel="STYLESHEET" type="text/css" href="/globalsight/includes/ContextMenu.css">
<script src="/globalsight/includes/ContextMenu.js"></script>
<%if(jsFile.exists()){ %>
<link href="/globalsight/javaScriptClient/jqueryUI/css/smoothness/jquery-ui-1.9.1.custom.min.css" rel="stylesheet"></link>
<script src="/globalsight/javaScriptClient/jqueryUI/js/jquery-1.8.2.min.js"></script>
<script src="/globalsight/javaScriptClient/jqueryUI/js/jquery-ui-1.9.1.custom.min.js"></script>
<script src="/globalsight/javaScriptClient/sortIntegrationSource.js" ></script>
<%} %>
<SCRIPT>
var modeId="<%=i_viewMode %>";
var modeFrom = "source";
var segFilter="";
var jsonUrl=this.location+"&dataFormat=json"+"&srcViewMode=" + modeId+"&random="+Math.random();
var isReviwMode;
var showFinish = false;

var g_reviewMode = eval("<%=state.isReviewMode()%>");
var g_isReviewActivity = eval("<%=b_isReviewActivity%>");
var g_readOnly = eval("<%=b_readOnly%>");
var g_disableLink = eval("<%=disableComment%>");

var g_targetLocale = "<%=str_targetLocale%>";

var w_editor = null;

var segmentEditorHeight = "540";
if (screen.availHeight > 600)
{
    segmentEditorHeight = screen.availHeight - 60;
}

function showProgressBar()
{
  try
  {
	  var div = document.getElementById('src_prograssbar');
	  div.style.visibility = "visible";
  }
  catch(e)
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

function SE(tuId, tuvId, subId, p_forceComment)
{
    HighlightSegment(tuId, tuvId, subId);
  
  if (typeof(parent.parent.target.content.findSegment) != "undefined")
  {
	  var format;
	  var donotMove = false;
	  var pageNum = false;
	  var repIndex = 1;
	  var tgtSegmentNoTag;
	  
	  if (typeof(window.parent.parent.parent.localData) != "undefined"
			  && typeof(window.parent.parent.parent.localData.source) != "undefined"
			  && typeof(window.parent.parent.parent.localData.target) != "undefined")
	  {
		  format = window.parent.parent.parent.localData.source[0].format;
		  
		  for(var i0 = 0; i0 < window.parent.parent.parent.localData.target.length; i0++)
		  {
			  var seg0 = window.parent.parent.parent.localData.target[i0];
			  if (seg0.tuId == tuId)
		      {
				  tgtSegmentNoTag = seg0.segmentNoTag;
				  break;
		      }
		  }
	  }
	  else
	 {
		  var xmlHttp=null;
		    try
		    {// Firefox, Opera 8.0+, Safari, IE7
		    xmlHttp=new XMLHttpRequest();
		    }
		  catch(e)
		    {// Old IE
		    try
		      {
		      xmlHttp=new ActiveXObject("Microsoft.XMLHTTP");
		      }
		    catch(e)
		      {
		      alert ("Your browser does not support XMLHTTP!");
		      return;  
		      }
		    }
		  var url=this.location + "&action=getTargetSegment&tuid=" + tuId + "&tuvid=" + tuvId + "&subid=" + subId;
		  xmlHttp.open("GET",url,false);
		  xmlHttp.send(null);
		  tgtSegmentNoTag = xmlHttp.responseText;
	 }
	  
      parent.parent.target.content.findSegment(format, tuId, tgtSegmentNoTag, "", donotMove, pageNum, repIndex);
  }
}

function Refresh() {}
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
          "&tuId=" + tuId + "&tuvId=" + tuvId + "&subId=" + subId + "&refresh=0&fromPage=source";

        forceCloseEditor('segment');

        hideContextMenu();

        w_editor = window.open(str_url, "CommentEditor",
          "width=550,height=610,top=100,left=100"); //resizable,
    }
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
    
    parent.parent.target.content.Refresh();
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
    
    parent.parent.target.content.Refresh();
    
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
    
    parent.parent.target.content.Refresh();
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
          "&refresh=0&releverage=false&fromPage=source";

        forceCloseEditor('comment');

        hideContextMenu();

        w_editor = window.open(str_url, "SegmentEditor",
          "resizable,width=560,height=" + segmentEditorHeight +
           ",top=0,left=0");
    }
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
    return (obj.tagName == 'A' && ((obj.className.indexOf('editorSegment') != -1)||(obj.className.indexOf('segmentContext') != -1)));
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
    	var targetClass;
    	try {
    	    var	targetObjs = parent.parent.parent.localData.target;
    		var tdid = o.parentElement.id;
    		var tuidArray = tdid.substr(3).split("_");
    		var tuid = tuidArray[0];
    		var subid = tuidArray[2];
    		var objsLength = targetObjs.length;
    		
    		for(var iii = 0 ; iii < objsLength ; iii++)
    		{
    			var i_obj = targetObjs[iii];
    			
    			if (i_obj.tuId == tuid && i_obj.subId == subid)
    			{
    				targetClass = i_obj.mainstyle;
    				break;
    			}
    		} 
    	} catch (ex) {}
    	
        if (targetClass)
        {
        	if (targetClass.indexOf('editorSegmentLocked') != -1 
            		|| (targetClass.indexOf('segmentContextUnlock') == -1 
            				&& targetClass.indexOf('segmentContext') != -1))
        	{
        		//contextForReadOnly(o, e);
        	}
        	else
        	{
        		contextForSegment(o, e);
        	}
            
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

function Preview()
{
    alert("Source Preview should not be possible.\n" +
          "It must be removed from the template.");
}

function disableLinks()
{
    // disable all links
    var links = document.body.getElementsByTagName("A");
    if (links != null)
    {
        for (var i = 0; i < links.length; ++i)
        {
            var link = links(i);
            if (link.href != "")
            {
                link.onclick = cancelEvent;
                link.style.cursor = "default";
                link.style.textDecoration = "underline";
            }
        }
    }
}

var otherPane = parent.parent.target;
var pageToScroll = otherPane ? otherPane.content : null;

$(window).ready(function(){
    $(window).scroll(function(){
    	<%=str_scrollHandler%>
    }); 
});

function doScroll()
{
    if (!otherPane) otherPane = parent.parent.target;
    if (!pageToScroll) pageToScroll = otherPane.content;

    if (parent.mode == otherPane.mode)
    {
        pageToScroll.scroll(self.document.body.scrollLeft,
          self.document.body.scrollTop);
    }
    
    resetLocation("src_prograssbar");
}

function doLoad()
{
  // <% if (i_viewMode == EditorConstants.VIEWMODE_PREVIEW) { %>
  //disableLinks();
  // <% } %>
  
	ContextMenu.intializeContextMenu();
}

// add javascript to synchronize scroll bars 
// by segment id in the pop-up editor
function update_tr(id) 
{
	var otherPane = parent.parent.target;
	var pageToScroll = otherPane ? otherPane.content : null;
    
    if(pageToScroll) 
    {
		var target_cell;
		var source_cell;	
		if (document.getElementById) 
		{
		    try
		    {
			source_cell = document.getElementById(id);
			target_cell = pageToScroll.document.getElementById(id);
		    }catch(e){}
                }
		else if (document.all) 
		{
			source_cell = document.all[id];
			target_cell = pageToScroll.document.all[id];
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
<script type="text/javascript" src="/globalsight/jquery/me_table.js"></script>
<style type="text/css">
<!--
#src_prograssbar {
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
		try	{
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
 onload="doLoad()" onunload="doUnload()" onerror="return true">
 
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

<!-- Object for the Snippet Editor Dialog - invoked by script. -->
<div id=idSnippetEditorDialog
     style="behavior: url('/globalsight/envoy/edit/snippets/SnippetEditor.htc');
            display: none;"></div>

<div id="src_prograssbar" style="top: 300px; left: 100px;">
	<br /> <img alt="<%=lb_loadingPreview %>" src="/globalsight/includes/loading.gif"> <%=lb_loadingPreview %> <br />
</div>

<% if (i_viewMode == EditorConstants.VIEWMODE_DETAIL) { %>
<TABLE WIDTH="100%" CELLSPACING="0" CELLPADDING="3" BORDER="1"
 style="border-color: lightgrey; border-collapse: collapse; border-style: solid; border-width: 1px;
 		font-family: Arial, Helvetica, sans-serif;font-size: 10pt;">
  
  <COL WIDTH="1%"  VALIGN="TOP" CLASS="editorId" NOWRAP>
  <COL WIDTH="99%" VALIGN="TOP" CLASS="editorText">
  
  <THEAD>
    <TR CLASS="tableHeadingGray" style="height:19pt;">
      <TD ALIGN="CENTER" class="sourceTempClass"><%=lb_id%></TD>
      <TD ALIGN="LEFT"><%=lb_segment%></TD>
    </TR>
  </THEAD>
  <TBODY id="idPageHtml"><%=str_pageHtml%></TBODY>
</TABLE>
<% } else { %>
<DIV id="idPageHtml" style="font-family: Arial, Helvetica, sans-serif;font-size: 10pt;">
	<%=str_pageHtml%>
</DIV>
<% } %>
<input type="hidden" id="segmentCount" name="segmentCount" value="<%=str_segmentCount %>" />
</BODY>
</HTML>
