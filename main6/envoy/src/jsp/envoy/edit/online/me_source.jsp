<%@ page
    contentType="text/html; charset=UTF-8"
    errorPage="error.jsp"
    import="com.globalsight.util.resourcebundle.ResourceBundleConstants,
            com.globalsight.util.resourcebundle.SystemResourceBundle,
            com.globalsight.everest.webapp.WebAppConstants,
            com.globalsight.everest.webapp.javabean.NavigationBean,
            com.globalsight.everest.webapp.pagehandler.PageHandler,
            com.globalsight.everest.webapp.pagehandler.edit.online.EditorState,
            com.globalsight.everest.webapp.pagehandler.edit.online.EditorConstants,
            com.globalsight.everest.servlet.util.SessionManager,
            java.util.*"
    session="true"
%>
<%
ResourceBundle bundle = PageHandler.getBundle(session);

SessionManager sessionMgr = (SessionManager)session.getAttribute(
  WebAppConstants.SESSION_MANAGER);
EditorState state =
  (EditorState)sessionMgr.getAttribute(WebAppConstants.EDITORSTATE);

String lb_id = bundle.getString("lb_id");
String lb_segment = bundle.getString("lb_segment");
String lb_loadingPreview = bundle.getString("lb_loading_preview");

EditorState.Layout layout = state.getLayout();
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

pre {
	font-family: Arial, Helvetica, sans-serif;
	font-size: 10pt;
}
</STYLE>
<SCRIPT src="/globalsight/envoy/terminology/viewer/error.js" defer></SCRIPT>
<SCRIPT src="/globalsight/envoy/edit/snippets/snippet.js" defer></SCRIPT>
<SCRIPT src="/globalsight/envoy/edit/online/editsnippets.js" defer></SCRIPT>
<script type="text/javascript" SRC="/globalsight/dojo/dojo.js"></script>
<SCRIPT>
dojo.require("dijit.Dialog");

var g_targetLocale = "<%=str_targetLocale%>";

function showProgressBar()
{
  try
  {
	  var div = dojo.byId('src_prograssbar');
	  div.style.visibility = "visible";
  }
  catch(e)
  {
  }
}

// functions from me_target that should be available to the segment editor.
function HighlightSegment(tuId, tuvId, subId) {}
function UnhighlightSegment(tuId, tuvId, subId) {}
function SaveSegment(tuId, tuvId, subId, segment) {}
function Refresh() {}
function doUnload() {}

function cancelEvent()
{
    window.event.returnValue = false;
    window.event.cancelBubble = true;
    return false;
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

function doScroll()
{
    if (!otherPane) otherPane = parent.parent.target;
    if (!pageToScroll) pageToScroll = otherPane.content;

    if (parent.mode == otherPane.mode)
    {
        pageToScroll.scroll(self.document.body.scrollLeft,
          self.document.body.scrollTop);
    }
}

function doLoad()
{
  // <% if (i_viewMode == EditorConstants.VIEWMODE_PREVIEW) { %>
  disableLinks();
  // <% } %>
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
	lastScrollY = 0;
	function resetLocation(p_eid) {
	try	{
		var diffY;
		if (document.documentElement && document.documentElement.scrollTop)
			diffY = document.documentElement.scrollTop;
		else if (document.body)
			diffY = document.body.scrollTop
		else {/*Netscape stuff*/
		}
		percent = .1 * (diffY - lastScrollY);
		if (percent > 0)
			percent = Math.ceil(percent);
		else
			percent = Math.floor(percent);
		document.getElementById(p_eid).style.top = parseInt(document
				.getElementById(p_eid).style.top)
				+ percent + "px";
		lastScrollY = lastScrollY + percent;
	} catch(e) {
  	}
	}
	window.setInterval("resetLocation(\"src_prograssbar\")", 1);
//-->
</script>

</HEAD>
<BODY onscroll="<%=str_scrollHandler%>" onload="doLoad()" onerror="return true">

<!-- Object for the Snippet Editor Dialog - invoked by script. -->
<div id=idSnippetEditorDialog
     style="behavior: url('/globalsight/envoy/edit/snippets/SnippetEditor.htc');
            display: none;"></div>

<div dojoType="dijit.Dialog" id="src_prograssbar" style="top: 300px; left: 100px;">
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
      <TD ALIGN="CENTER"><%=lb_id%></TD>
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
