<%@ page
    contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="java.util.*,com.globalsight.everest.webapp.javabean.NavigationBean,
            com.globalsight.everest.edit.online.SegmentVersion,
            com.globalsight.everest.edit.online.SegmentView,
	        com.globalsight.everest.edit.online.SegmentMatchResult,
            com.globalsight.everest.webapp.pagehandler.PageHandler,
            com.globalsight.everest.webapp.pagehandler.edit.online.EditorConstants,
            com.globalsight.everest.webapp.pagehandler.edit.online.EditorState,
            com.globalsight.everest.webapp.WebAppConstants,
            com.globalsight.everest.servlet.util.SessionManager,
	    	com.globalsight.terminology.Hitlist,
	    	com.globalsight.terminology.ITermbase,
	    	com.globalsight.terminology.termleverager.TermLeverageMatchResult,
            com.globalsight.util.edit.EditUtil,
            com.globalsight.util.GlobalSightLocale,
            com.globalsight.ling.common.Text,
            java.util.Collection,
            java.util.Iterator,
            java.util.List,
            java.util.Locale,
            java.util.ResourceBundle,
            java.text.NumberFormat"
    session="true"
%><%@page import="com.globalsight.util.StringUtil"%>

<jsp:useBean id="tminfo" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean"/>
<jsp:useBean id="skin" scope="application"
 class="com.globalsight.everest.webapp.javabean.SkinBean"/>
<%@ include file="/envoy/common/installedModules.jspIncl" %>
<%
ResourceBundle bundle = PageHandler.getBundle(session);
Locale uiLocale = (Locale)session.getAttribute(WebAppConstants.UILOCALE);

String url_tmInfo = tminfo.getPageURL();

String lb_clickToOpenTb = bundle.getString("lb_click_to_open_tb");

SessionManager sessionMgr = (SessionManager)session.getAttribute(
  WebAppConstants.SESSION_MANAGER);
EditorState state =
  (EditorState)sessionMgr.getAttribute(WebAppConstants.EDITORSTATE);
SegmentView view =
  (SegmentView)sessionMgr.getAttribute(WebAppConstants.SEGMENTVIEW);

//
// TB Matches
//
String str_defaultTermbaseName = state.getDefaultTermbaseName();
long l_defaultTermbaseId = state.getDefaultTermbaseId();
boolean b_haveTermbase = (str_defaultTermbaseName != null);
boolean b_rtl = EditUtil.isRTLLocale(state.getTargetViewLocale());

StringBuffer stb_segments = new StringBuffer();
StringBuffer stb_terms = new StringBuffer();

if (view != null)
{
  //
  // TB Matches
  //
  Collection tbMatches = view.getTbMatchResults();

  if (tbMatches != null)
  {
    int i = 0;
    for (Iterator it = tbMatches.iterator(); it.hasNext(); )
    {
      TermLeverageMatchResult p = (TermLeverageMatchResult)it.next();
      Hitlist.Hit sourceHit = p.getSourceHit();

      String str_source = sourceHit.getTerm();

      stb_terms.append("a_tbSegments[");
      stb_terms.append(i);
      stb_terms.append("] = { data: \"");
      stb_terms.append(EditUtil.toJavascript(str_source));
      stb_terms.append("\", hits: new Array() };\n");

      int j = 0;
      for (Iterator it1 = p.getTargetHitIterator(); it1.hasNext(); )
      {
        Hitlist.Hit hit = (Hitlist.Hit)it1.next();

        String str_target = hit.getTerm();
        long l_conceptId = hit.getConceptId();
        long l_termId = hit.getTermId();
        int i_score = hit.getScore();

        stb_terms.append("a_tbSegments[");
        stb_terms.append(i);
        stb_terms.append("].hits[");
        stb_terms.append(j);
        stb_terms.append("] = { data: \"");
        stb_terms.append(EditUtil.toJavascript(str_target));
        stb_terms.append("\", cid: \"");
        stb_terms.append(l_conceptId);
        stb_terms.append("\", tid: \"");
        stb_terms.append(l_termId);
        stb_terms.append("\", score: \"");
        stb_terms.append(i_score);
        stb_terms.append("\"};\n");

        j++;
      }

      i++;
    }
  }

  //
  // TM Matches
  //

  List tmMatches = view.getTmMatchResults();
  if (tmMatches != null)
  {
    int i = 0;
    String locale = "en-US";

    if (!view.isLocalizable())
    {
      locale = EditUtil.toRFC1766(state.getTargetLocale());
    }

    for (Iterator it = tmMatches.iterator(); it.hasNext(); )
    {
        SegmentMatchResult p = (SegmentMatchResult)it.next();

        stb_segments.append("a_tmSegments[");
        stb_segments.append(i++);
        stb_segments.append("] = { data: \"");
        stb_segments.append(EditUtil.toJavascript(p.getMatchContent()));
        stb_segments.append("\", text: \"");
        stb_segments.append(EditUtil.toJavascript(EditUtil.xmlToHtml(
            EditUtil.stripTags(p.getMatchContent()))));
        stb_segments.append("\", label: \"");
        stb_segments.append(StringUtil.formatPercent(p.getMatchPercentage(), 2)).append("%");
        stb_segments.append("\", type: \"");
        stb_segments.append(p.getMatchType());
        stb_segments.append("\", tuvId: \"");
        stb_segments.append(p.getTuvId());
        stb_segments.append("\", srcLocaleId: \"");
        stb_segments.append(state.getSourceLocale().getId());
        stb_segments.append("\", lang: \"");
        stb_segments.append(locale);
        stb_segments.append("\", dir: \"");
        if (EditUtil.isRTLLocale(state.getTargetLocale()))
        {
            stb_segments.append(
                Text.containsBidiChar(p.getMatchContent()) ? "RTL" : "LTR");
        }
        else
        {
            stb_segments.append("LTR");
        }
        stb_segments.append("\" };\n");
    }
  }
}
String str_tmSegments = stb_segments.toString();
String str_terms = stb_terms.toString();

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
String str_dataType = state.getPageFormat();
String title = bundle.getString("lb_tm_window");
%>
<!DOCTYPE HTML PUBLIC "-//IETF//DTD HTML//EN">
<html>
<!-- This is envoy\edit\online2\tmwindow.jsp -->
<head>
<title><%=title %></title>
<SCRIPT SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<SCRIPT SRC="/globalsight/includes/overlib.js"></SCRIPT>
<script type="text/javascript" src="/globalsight/jquery/jquery-1.9.1.min.js"></script>
<script src="/globalsight/envoy/edit/online2/applet.js"></script>
<SCRIPT src="/globalsight/envoy/terminology/viewer/viewerAPI.js"></SCRIPT>
<link type="text/css" rel="StyleSheet"
  href="/globalsight/envoy/edit/online2/editor.css">
<link type="text/css" rel="StyleSheet"
  href="/globalsight/includes/menu/skins/winclassic.css">
<script src="/globalsight/includes/menu/js/poslib.js"></script>
<script src="/globalsight/includes/menu/js/scrollbutton.js"></script>
<script src="/globalsight/includes/menu/js/menu4.js"></script>
<link rel="STYLESHEET" type="text/css" href="/globalsight/includes/ContextMenu.css">
<script src="/globalsight/includes/ContextMenu.js"></script>
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

.header { font-family: Arial, Helvetica, sans-serif;
          font-weight: bold;
          font-size: 10pt;
          color: white;
          background-color: #0C1476;
        }
.help   {
          font-size: 8pt;
          cursor: pointer;
          float: right;
        }
.heading { font-weight: bold;
           font-family: Tahoma, serif;
           font-size: 12pt;
         }
.label { font-weight: bold;
         font-family: Tahoma, serif;
         font-size: 10pt;
       }
.text  { font-family: Tahoma, serif;
         font-size: 10pt;
       }
.nodata {
          font-style: italic;
          font-size: 90%;
          color: gray;
        }
DT      {}
DD      { padding-bottom: .1em;
          margin-left: 1em;
          color: #0C1476;
          font-weight: bold;
          /*behavior: url(/globalsight/envoy/edit/online2/bhvr_term.htc);*/
        }
.clickable { cursor: pointer; }
.clickableLink { cursor: pointer; color: blue; text-decoration: underline; }
#idScore     { /*border-bottom: 1px dotted;*/ text-decoration: underline; }
#idMatchType {
               font-style: italic;
               font-weight: normal;
               font-size: 80%;
             }
#idSource,
#idTarget,
#idTerms  {
            border: 1px solid black;
            font-family: Arial;
            font-size: 10pt;
            padding-left: 2px;
            overflow-y: auto;
          }
</style>
<style type="text/css">
@import url(/globalsight/includes/dojo.css);
@import url(/globalsight/includes/css/menu.css);
</style> 
<script type="text/javascript" djConfig="parseOnLoad: true, isDebug: false" src="/globalsight/dojo/dojo.js"></script>
<script type="text/javascript">
dojo.require("dijit.MenuBar");
dojo.require("dijit.PopupMenuBarItem");
dojo.require("dijit.Menu");
dojo.require("dijit.MenuItem");
</script>
<script>
var isIE = window.navigator.userAgent.indexOf("MSIE")>0;
var isFirefox = window.navigator.userAgent.indexOf("Firefox")>0;
var helpFile = "<%=bundle.getString("help_paragraph_editor_tm")%>";

var uiLocale = "<%=str_uiLocale%>";
var defaultDataType = "<%=str_dataType%>";
var defaultItemType = "text";
var g_defaultTermbaseName = "<%=EditUtil.toJavascript(str_defaultTermbaseName)%>";
var g_defaultTermbaseId = "<%=l_defaultTermbaseId%>";
var g_haveTermbase = eval("<%=b_haveTermbase%>");
var g_selectedTerm = null;

var tmp;

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
actionMenu.add(tmp = new MenuItem("<%=bundle.getString("lb_releverage") %>", releverage));
tmp.mnemonic = "r";
var releverageMni = tmp;

actionMenu.add(tmp = new MenuItem("<%=bundle.getString("lb_browse_termbase") %>", showTermbase));
tmp.mnemonic = "b";
tmp.disabled = !g_haveTermbase;

// Segment Context Menu
var tmContextMenu = new Menu();
tmContextMenu.add(tmp = new MenuItem("<%=bundle.getString("lb_copy_to_editor") %>", copyMatchToEditor));
tmp.mnemonic = "c";
tmContextMenu.add(tmp = new MenuItem("<%=bundle.getString("lb_insert_selection_in_editor") %>",
                  copySelectionToEditor));
tmp.mnemonic = "s";
var copyTmSelectionMni = tmp;

// Termbase Context Menu
var tbContextMenu = new Menu();
tbContextMenu.add(tmp = new MenuItem("<%=bundle.getString("lb_insert_in_editor") %>", copyTermToEditor));
tmp.mnemonic = "c";
tbContextMenu.add(tmp = new MenuItem("<%=bundle.getString("lb_browse_term") %>", browseTerm));
tmp.mnemonic = "b";

var menuBar = new MenuBar();
menuBar.add(tmp = new MenuButton("<%=bundle.getString("lb_actions") %>", actionMenu));

// End Menus

// Context Menu

var lastKeyCode = 0;

function rememberKeyCode()
{
  lastKeyCode = window.event.keyCode;
}

function showContextMenu()
{
  g_selectedTerm = null;

  // Has the user right-clicked the segment match?
  var el = window.event.srcElement;
  while (el != null && el.tagName != "DIV") { el = el.parentNode; }
  var showTmMenu = (el != null && el.id == 'idTarget');

  // Has the user right-clicked a term match?
  var el = window.event.srcElement;
  while (el != null && el.tagName != "DD") { el = el.parentNode; }
  var showTbMenu = (el != null && el.tid != '');

  if (showTbMenu)
  {
    g_selectedTerm = el;
  }

  if ((showTmMenu && a_tmSegments.length > 0) ||
      (showTbMenu && a_tbSegments.length > 0))
  {
    // find left and top
    var left = window.event.screenX;
    var top = window.event.screenY;

    if (showTmMenu)
    {
      if (document.selection.type == 'None')
      {
        copyTmSelectionMni.disabled = true;
      }
      else
      {
        copyTmSelectionMni.disabled = false;
      }
    }
  
    if (showTmMenu)
    {
      tmContextMenu.invalidate();
      tmContextMenu.show( left, top);
    }
    else
    {
      //tbContextMenu.invalidate();
      tbContextMenu.show( left, top);
    }
  }

  event.returnValue = false;
  lastKeyCode = 0
};

if(document.attachEvent){
	document.attachEvent("oncontextmenu", showContextMenu);
	document.attachEvent("onkeyup", rememberKeyCode);
}

// End Context Menu

function doKeyDown()
{
  var key = event.keyCode;
  window.status = String(key);

  if (key == 27) // "ESC"
  {
      event.cancelBubble = true;
      event.returnValue = false;
      window.close();
  }

  if (event.ctrlKey && !event.altKey)
  {
    if (key == 73) // "I"
    {
      event.cancelBubble = true;
      event.returnValue = false;
    }
  }

  if (!event.ctrlKey && event.altKey)
  {
  }
}

var a_tbSegments = new Array();
<%=str_terms%>

var g_segmentIndex = 0;
var a_tmSegments = new Array();
<%=str_tmSegments%>

function goLeftSegment()
{
  if (a_tmSegments.length > 0)
  {
    if (g_segmentIndex == 0)
    {
      g_segmentIndex = a_tmSegments.length - 1;
    }
    else
    {
      --g_segmentIndex;
    }

    showSegmentData(g_segmentIndex);
  }
}

function goRightSegment()
{
  if (a_tmSegments.length > 0)
  {
    if (g_segmentIndex == (a_tmSegments.length - 1))
    {
      g_segmentIndex = 0;
    }
    else
    {
      ++g_segmentIndex;
    }

    showSegmentData(g_segmentIndex);
  }
}

function showSegmentData(index)
{
  var o = a_tmSegments[index];

  idScore.innerHTML = o.label;
  idScore.popup = o.type;
  // idSegmentLabel.innerText = o.label;
  idSegmentLabel.innerHTML = "" + (index + 1) + " of " + a_tmSegments.length;
  idTarget.innerHTML = o.text;

  if (o.lang)
  {
    idTarget.lang = o.lang;
  }
  if (o.dir)
  {
    idTarget.dir = o.dir;
  }

  if (index == 0)
  {
    idTmMatchesPrev.style.visibility = 'hidden';
  }
  else
  {
    idTmMatchesPrev.style.visibility = 'visible';
  }

  if (index == a_tmSegments.length - 1)
  {
    idTmMatchesNext.style.visibility = 'hidden';
  }
  else
  {
    idTmMatchesNext.style.visibility = 'visible';
  }
}

function convertMatches(sourceGxml, datatype)
{
  if (a_tmSegments.length > 0)
  {
    initTmHelper(sourceGxml, datatype);
    for (var i = 0; i < a_tmSegments.length; i++)
    {
      var gxml = a_tmSegments[i].data;
      var html = GetTargetDisplayHtmlForTmPreview2(gxml, datatype, opener.g_ptagsVerbose);
      a_tmSegments[i].text = html;
    }
  }
}

function showTermHits()
{
  var s = "<DL>";

  if (a_tbSegments.length > 0)
  {
    for (var i = 0; i < a_tbSegments.length; i++)
    {
      var srcHit = a_tbSegments[i];
      var term = srcHit.data;
      var hits = srcHit.hits;

      s += "<DT oncontextmenu='context(null,event);'>" + term + "</DT>";

      for (var j = 0; j < hits.length; j++)
      {
		var trgHit = hits[j];
		var trgTerm = trgHit.data;
		var trgCid = trgHit.cid;
		var trgTid = trgHit.tid;
		var trgScore = trgHit.score;

		s += "<DD cid='" + trgCid + "' tid='" + trgTid + "' oncontextmenu='contextForTerminology(null,event);'>";
		s += trgTerm;
		s += "</DD>";
      }
    }
  }

  s += "</DL>";

  idTerms.innerHTML = s;
}

function showHelp()
{
    var helpWindow = window.open(helpFile, 'helpWindow',
      'resizable=yes,scrollbars=yes,WIDTH=600,HEIGHT=400');
    helpWindow.focus();
}

function showTermbase()
{
  if (g_haveTermbase)
  {
    ShowTermbase('<%=l_defaultTermbaseId%>');
  }
}
  
function browseTerm()
{
  if (!g_selectedTerm) return;
  var conceptid = g_selectedTerm.getAttribute("cid");
  var termid = g_selectedTerm.getAttribute("tid");

  ShowTermbaseConceptTerm(g_defaultTermbaseId, conceptid, termid);
}
  
function copyMatchToEditor()
{
  var text = document.getElementById("idTarget").innerHTML;
  opener.setText(text);
}
  
function copySelectionToEditor()
{
  var text;
  if(isIE){
	var range = document.selection.createRange();		  
	text = range.text;
  }
  else
  {
   	text = document.getSelection();
   	if(text=="") return;
  }
  opener.insertText(text);
}

function copyTermToEditor()
{
  if (!g_selectedTerm) return;
  var text = g_selectedTerm.innerText || g_selectedTerm.textContent;
  opener.insertText(text);
}

function releverage()
{
  var url = "<%=url_tmInfo%>&refresh=0&releverage=false";
  window.location = url;
}

function Clear()
{
  	document.getElementById("idSource").innerHTML =  '<span class=nodata><%=bundle.getString("lb_no_data") %></span>';
  	document.getElementById("idScore").innerHTML  = '\u00a0';
  	document.getElementById("idScore").popup = '';
  	//document.getElementById("idSegmentLabel").innerText = ' 0 of 0 '; //\u00a0
  	document.getElementById("idSegmentLabel").innerHTML = ' 0 of 0 ';
	document.getElementById("idTmMatchesPrev").style.visibility = 'hidden';
	document.getElementById("idTmMatchesNext").style.visibility = 'hidden';
	document.getElementById("idTarget").innerHTML = '<span class=nodata><%=bundle.getString("lb_no_data") %></span>';
	document.getElementById("idTarget").dir = "LTR";
	document.getElementById("idTarget").lang = "en-us";
	document.getElementById("idTerms").innerHTML = '<span class=nodata><%=bundle.getString("lb_no_data") %></span>';
}

function doOnload()
{
  opener.showingTmWindow(window);

  Clear();

  if (opener.g_sourceHTML)
  {
    releverageMni.disabled = false;
    document.getElementById("idSource").innerHTML = opener.g_sourceHTML;
    convertMatches(opener.g_sourceGxml, opener.g_datatype);

    if (a_tmSegments.length > 0)
    {
      showSegmentData(0);
    }
    showTermHits();
  }
  else
  {
    releverageMni.disabled = true;
    a_tmSegments.length = 0;
  }

  if (g_haveTermbase)
  {
    idTermbase.innerHTML = g_defaultTermbaseName;
  }

  opener.focus();
  
  ContextMenu.intializeContextMenu();
}

function doBeforeUnload()
{
  try { opener.closingTmWindow(); } catch (ignore) {}
}

//Add menu function for Firefox
var fontGray1 = "<font color='gray'>";
var fontGray2 = "</font>";
var lb_copy_to_editor 				= "<%=bundle.getString("lb_copy_to_editor")%>";
var lb_insert_selection_in_editor 	= "<%=bundle.getString("lb_insert_selection_in_editor")%>";
var lb_corpus_context 				= "<%=bundle.getString("lb_corpus_context")%>";

function contextForSegmentContext(url, e)
{
	if(isIE) 
	{
		return;
	}
	else
    {
    	e.preventDefault();
    	e.stopPropagation();
    }

	var lb_insert_selection = fontGray1+lb_insert_selection_in_editor+fontGray2;
	
    var popupoptions = [
		new ContextItem(lb_copy_to_editor,  	function(){ copyMatchToEditor();}),
		new ContextItem(lb_insert_selection, 	function(){ copySelectionToEditor();}),
		new ContextItem(lb_corpus_context, 		function(){ showCorpus();})
    ];    	
    
    ContextMenu.display(popupoptions, e); 
}

function contextForTerminology(url, e)
{
	if(isIE) 
	{
		return;
	}
	else
    {
    	e.preventDefault();
    	e.stopPropagation();
    }

	g_selectedTerm = e.currentTarget;
	
    var popupoptions = [
		new ContextItem("<%=bundle.getString("lb_insert_in_editor") %>",
				function(){ copyTermToEditor();}),
		new ContextItem("<%=bundle.getString("lb_browse_term") %>",
				function(){ browseTerm();})
    ];    	
    
    ContextMenu.display(popupoptions, e); 
}

//Cancel context menu for Firefox
function context(url, e)
{
	if(isIE) 
	{
		return;
	}
	else
    {
    	e.preventDefault();
    	e.stopPropagation();
    }
}
</script>
</head>
<body id="idBody" onload="doOnload()" onbeforeunload="doBeforeUnload()"
 onkeydown="doKeyDown()">
<div class="header" style="position: absolute; top: 0; left: 0; right: 0;
 width: expression(idBody.clientWidth); height: 30;
 padding-left: 10px; padding-top: 6px;">
    <SPAN id="idHelpViewer" onclick="showHelp()" class="help"
     style="margin-right: 10px;"><%=bundle.getString("lb_help") %></SPAN>
    <SPAN class="help">&nbsp;|&nbsp;</SPAN>
    <SPAN id="idCloseEditor" class="help" onclick="window.close()"><%=bundle.getString("lb_close") %></SPAN>
    <!-- // Cancel menubar for firefox, use dojo instead.
    <SPAN class="help" style="position:relative; top:-4;"><script>menuBar.write()</script>&nbsp;&nbsp;&nbsp;</SPAN>
    <P>TM Results</P>
    -->
    <SPAN class="help">&nbsp;|&nbsp;</SPAN> 
	<div class="help" id="menubar" dojoType="dijit.MenuBar">
      <div dojoType="dijit.PopupMenuBarItem" id="Item Menu">
        <span>Actions</span>
        <div dojoType="dijit.Menu" id="fileMenu">
            <div dojoType="dijit.MenuItem" onClick="releverage();">
                <%=bundle.getString("lb_releverage")%>
            </div>
            <div dojoType="dijit.MenuItem" onClick="showTermbase();">
                <%=bundle.getString("lb_browse_termbase")%>
            </div>
        </div>
      </div>
	</div>
	TM Results
</div>
<div id="idLeft" style="position: absolute; top: 35; left: 0;
  padding-left: 10px; padding-right: 5px;
  width:80%; 
  height: expression(idBody.clientHeight - 35);">
  <div class="label" unselectable="on"><%=bundle.getString("lb_source") %>:</div>
  <div id="idSource" height="100%"></div>
  <div unselectable="on">
  <nobr>
  <span class="label" unselectable="on"><%=bundle.getString("lb_target") %>:</span>
  <IMG SRC="/globalsight/images/previousMatchArrow.gif" height=12 width=6
       style="margin-left: 1px; margin-right: 1px; visibility: hidden;"
       id="idTmMatchesPrev" class="clickable" onclick="goLeftSegment();"
  ><SPAN id="idSegmentLabel" class="text" onclick="return false;"
    unselectable="on"></SPAN
  ><IMG SRC="/globalsight/images/nextMatchArrow.gif" height=12 width=8
	style="margin-left: 1px; margin-right: 1px; visibility: hidden;"
	id="idTmMatchesNext" class="clickable" onclick="goRightSegment();">
  &nbsp;
  <span class="text" unselectable="on"><%=bundle.getString("lb_score") %>: </span>
  <span id="idScore" unselectable="on" class="text" onmouseout="return nd();"
        onmouseover="if (this.popup) return overlib(this.popup);"></span>
  &nbsp;&nbsp;&nbsp;&nbsp;
  <SPAN class="text"><I>(<%=bundle.getString("lb_right_click_match_to_copy") %>)</I></SPAN>
  </nobr>
  </div>
  <div id="idTarget" height="100%" oncontextmenu="contextForSegmentContext(null,event);"></div>
</div>

<div id="idRight" style="position: absolute; top: 35;
  left:81%; *left:79%;
  width: 18%; *width: 20%;
  height:auto;
  padding-left: 5px; padding-right: 10px;">
  <div class="label" unselectable="on"><%=bundle.getString("lb_termbase") %>: <SPAN ID="idTermbase"></SPAN>
  </div>
  <!--height: expression(idBody.clientHeight - 55) -->
  <div id="idTerms" style="position: relative; top: 0; left: 0;
   height:295px; height: expression(idBody.clientHeight - 55)"></div>
</div>

<div id="overDiv" style="position:absolute; visibility:hidden; z-index:1000;"></div>

</body>
</html>
