<%@ page
    contentType="text/html; charset=UTF-8"
    errorPage="error.jsp"
    import="java.util.*,com.globalsight.everest.webapp.pagehandler.PageHandler,
            com.globalsight.everest.webapp.pagehandler.edit.online.EditorConstants,
            com.globalsight.everest.webapp.pagehandler.edit.online.EditorState,
            com.globalsight.everest.webapp.WebAppConstants,
            com.globalsight.everest.servlet.util.SessionManager,
            com.globalsight.everest.edit.online.SegmentView,
            com.globalsight.util.edit.EditUtil,
            java.util.Locale,
            java.util.ResourceBundle"
    session="true"
%><%
// **********************************************************
// The BIDI editor has been retired. Use se_target.jsp.
// **********************************************************
ResourceBundle bundle = PageHandler.getBundle(session);

SessionManager sessionMgr = (SessionManager)session.getAttribute(
  WebAppConstants.SESSION_MANAGER);
EditorState state =
  (EditorState)sessionMgr.getAttribute(WebAppConstants.EDITORSTATE);
SegmentView view =
  (SegmentView)sessionMgr.getAttribute(WebAppConstants.SEGMENTVIEW);

String lb_target = bundle.getString("lb_target");

String str_langAttr = EditUtil.getLanguageAttributes(
  state.getTargetLocale(), view.isLocalizable());
%>
<HTML>
<HEAD>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<STYLE>
A, A:hover, A:active, A:visited, A:link { color: blue; text-decoration: none}

#textarea { font-size: 10pt; font-family: Courier; }

body {
	border: 0;
	margin: 0;
}

iframe {
	margin-top: 5px;
	width: 95%;
	height: 100%;
	border: 2px inset;
}

td.coolButton {
	font-family: Arial;
	font-size: 14px;
	width: 16px;
	height: 16px;
	font: menu;
}
</STYLE>
<script type="text/javascript" src="/globalsight/envoy/edit/online/stringbuilder.js"></script>
<script type="text/javascript" src="/globalsight/envoy/edit/online/richedit.js"></script>
<script type="text/javascript" src="/globalsight/envoy/edit/online/coolbuttons.js"></script>
<SCRIPT LANGUAGE="JavaScript">
function debug()
{
  var edit = document.all.edit;
  alert("HTML:\n\n" + edit.getHTML() +
    "\n\nText:\n\n'" + edit.getText() + "'");
  edit.focus();
}

var o_textbox = null;
var b_inited = false;

// Callback for onload() of se_main.jsp.
function PostLoad()
{
}

function init()
{
  if (!b_inited)
  {
    b_inited = true;
    o_textbox = document.all.edit;

    fixSize();

    var all = document.all;
    var l = all.length;
    for (var i = 0; i < l; i++) {
	if (all[i].tagName != "INPUT" && all[i].tagName != "TEXTAREA")
		all[i].unselectable = "on";
    }
  }
  else
  {
    o_textbox.focus();
  }

  deactivateButtons();
}

function deactivateButtons()
{
  if (!parent.parent.HasFormattingTags())
  {
    disable(idBold);
    disable(idItalic);   
    disable(idUnderline);
    o_textbox.enableFormattingKeys(false);
  }

  if (!parent.parent.HasPTags())
  {
    disable(idBr);
    disable(idNbsp);
    if (!parent.parent.openPtags())
    {
    	disable(idPtag);
    }
  }
}

function fixSize()
{
    o_textbox = document.all.edit;

    o_textbox.style.top = toolBar.offsetHeight + 10;
    o_textbox.style.height = Math.max(0,
      document.body.offsetHeight - toolBar.offsetHeight - 45);
    o_textbox.style.width = Math.max(0, document.body.offsetWidth - 20);
}

window.onresize = fixSize;

function insertPTag()
{
  var ptag;
  var res = null;

  // TODO: need to get list of required ptags
  var ptags = applet.getPtagString();
  var args = ptags.split(",");
   
  res = showModalDialog("/globalsight/envoy/edit/online/selectptag.jsp", args,
    "font-family:Courier; font-size:12; " +
    "dialogWidth:20em; dialogHeight:25em; status:no;");

  if (res != null)
  {
      if (res["ptag"] != null)
      {
        o_textbox.insertPTag(res["ptag"]);
      }
  }

  o_textbox.focus();
}

function SetVerbosePTags(flag)
{
  o_textbox.setVerbosePTags(flag);
}

function SetSegment(s, changed)
{
  o_textbox.setHTML(s);
  o_textbox.focus();

  if (!changed)
  {
    idSourceSegment.innerHTML = s;
  } 
}

function GetSegment()
{
  var res = o_textbox.getText();
  return res;
}

function SetFocus()
{
  o_textbox.focus();
}

function IsChanged()
{
  var s_oldSegment = idSourceSegment.innerText;
  var s_newSegment = GetSegment();

  if (s_oldSegment != s_newSegment)
  {
    // alert("Segments differ:\n\n" + "'" + s_oldSegment +
    //   "'\n\n'" + s_newSegment + "'");
    return true;
  }

  return false;
}
      
function InsertTerm(term)
{
  o_textbox.focus();
  o_textbox.insertText(term);
}

function CanClose()
{
  return !IsChanged();
}
</SCRIPT>
</HEAD>
<BODY scroll="no">
<APPLET
  style="display:inline"
  archive="/globalsight/applet/lib/online.jar"
  code="com.globalsight.ling.tw.online.OnlineApplet"
  id="applet"
  name="applet"
  width="0"
  height="0">
</APPLET>
<div id="idSourceSegment" style="display:none"></div>
<HR COLOR="#0C1476" WIDTH="95%">

<table cellspacing="0" id="toolBar" width="98%">
  <tr>
    <td align="left">
      &nbsp;<SPAN CLASS="standardTextBold"><%=lb_target%></SPAN>
    </td>
    <td width="100%">&nbsp;</td>
    <td align="right">
      <table cellspacing="0">
	<tr>
<%--	  
<td class="coolButton" onclick="debug()">
<nobr>&nbsp;Debug&nbsp;</nobr>
</td>
--%>
      <td class="coolButton" id="idSup"
	    onclick="o_textbox.makeSup(); o_textbox.frameWindow.focus();">
	    &nbsp;[superscript<sup>2</sup>]&nbsp;
	  </td>
      <td class="coolButton" id="idSub"
	    onclick="o_textbox.makeSub(); o_textbox.frameWindow.focus();">
	    &nbsp;[subscript<sub>2</sub>]&nbsp;
	  </td>
	  <td class="coolButton" id="idBold"
	    onclick="o_textbox.makeBold(); o_textbox.frameWindow.focus();">
	    &nbsp;<B>[bold]</B>&nbsp;
	  </td>
	  <td class="coolButton" id="idItalic"
	    onclick="o_textbox.makeItalic(); o_textbox.frameWindow.focus();">
	    &nbsp;<i>[italic]</i>&nbsp;
	  </td>
	  <td class="coolButton" id="idUnderline"
	    onclick="o_textbox.makeUnderline(); o_textbox.frameWindow.focus();">
	    &nbsp;<u>[underline]</u>&nbsp;
	  </td>
	  <td class="coolButton" id="idBr"
	    onclick="o_textbox.addBr(); o_textbox.frameWindow.focus();">
	    &nbsp;[br]&nbsp;
	  </td>
	  <td class="coolButton" id="idNbsp"
	    onclick="o_textbox.addNbsp(); o_textbox.frameWindow.focus();">
	    &nbsp;[nbsp]&nbsp;
	  </td>
	  <td class="coolButton" id="idPtag" onclick="insertPTag()">
	    &nbsp;[ptag...]&nbsp;
	  </td>
	</tr>
      </table>
    </td>
  </tr>
</table>
<iframe frameborder="0" id="edit" src="about:blank" class="richEdit" align="center"
  usebr="true" <%=str_langAttr%> onblur="return false"
  oneditinit="init();"></iframe>
  <script>
parent.parent.finishFrame();
</script>

</BODY>
</HTML>
