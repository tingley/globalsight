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
            com.globalsight.everest.webapp.pagehandler.projects.workflows.JobManagementHandler,
            com.globalsight.everest.util.system.SystemConfiguration,
            com.globalsight.everest.util.system.SystemConfigParamNames,
            java.io.File,
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
</STYLE>
<SCRIPT src="/globalsight/envoy/terminology/viewer/error.js" defer></SCRIPT>
<SCRIPT src="/globalsight/envoy/edit/snippets/snippet.js" defer></SCRIPT>
<SCRIPT src="/globalsight/envoy/edit/online/editsnippets.js" defer></SCRIPT>
<script type="text/javascript" src="/globalsight/jquery/jquery-1.6.4.min.js"></script>
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

var g_targetLocale = "<%=str_targetLocale%>";

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
<BODY  onload="doLoad()" onerror="return true">

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
