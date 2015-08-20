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
<SCRIPT SRC="/globalsight/includes/setStyleSheet.js"></SCRIPT>
<script SRC="/globalsight/includes/spellcheck.js"></script>
<script SRC="/globalsight/spellchecker/jsp/spellcheck.js"></script>
<script SRC="/globalsight/xdespellchecker/noapplet/SpellCheckNoApplet.js"></script>
<script SRC="/globalsight/envoy/edit/online/coolbuttons.js"></script>
<STYLE>
A, A:hover, A:active, A:visited, A:link { color: blue; text-decoration: none}
td.coolButton {
	font-family: Arial;
	font-size: 14px;
	width: 16px;
	height: 16px;
	font: menu;
}

img.middle {
    margin-top: 2px;
}
</STYLE>
<SCRIPT>
var o_textbox = null;
var b_changed = false;

var g_SC_GSA = new SC_GSA_Parameters();
var g_SC_XDE = new SC_XDE_Parameters();
var g_canSpellcheck = true;
var w_scwin = null;

var sc_customDict = null;
var sc_dict;
var sc_uiLang;

function spellCheck()
{
    if (g_SC_XDE.isLanguageSupported(parent.parent.targetlocale))
    {
        // XDE spell checking

        if (!sc_customDict)
        {
          sc_dict = g_SC_XDE.getSystemDict(parent.parent.targetlocale);
          sc_customDict = g_SC_XDE.getCustomDict(
            parent.parent.userId, parent.parent.targetlocale);
          sc_uiLang = g_SC_XDE.getUiLanguage(parent.parent.uilocale);

          frmSC.language.value = sc_dict;
        }

        //alert("XDE spell checking using dict `" + sc_dict +
        //  "', customdict `" + sc_customDict + "'");

        w_scwin = doSpell(this, 'frmSC.language', 'textarea&typectrl=textarea',
          false, sc_customDict, sc_uiLang);
    }
    else 
    {
        // GlobalSight spell checking (supports all languages)

        if (!sc_customDict)
        {
          sc_dict = g_SC_GSA.getSystemDict(parent.parent.targetlocale);
          sc_customDict = g_SC_GSA.getCustomDict(
            parent.parent.userId, parent.parent.targetlocale);
        }

        //alert("GlobalSight spell checking using dict `" + sc_dict +
        //  "', customdict `" + sc_customDict + "'");

        w_scwin = scSpell(this, 'textarea&typectrl=textarea',
          parent.parent.targetlocale, parent.parent.uilocale,
          sc_dict, sc_customDict);
    }
}

function doOnBeforeUnload()
{
  try { w_scwin.close(); } catch (ignore) {};
}

// Callback for se_main.jsp.
function HilitePtags(bright)
{
}

// Callback for onload() of se_main.jsp.
function PostLoad()
{
}

function init()
{
    o_textbox = document.targetForm.targetText;
    if (document.recalc)
    {
		targetLayer.style.setExpression("pixelWidth", "document.body.clientWidth");
		targetLayer.style.setExpression("pixelHeight", "document.body.clientHeight - 35");

		o_textbox.style.setExpression("pixelWidth", "document.body.clientWidth - 20");
		o_textbox.style.setExpression("pixelHeight",  "document.body.clientHeight - 60");
    } else {
		targetLayer.style.width = (document.body.clientWidth - 20) + 'px';
		//targetLayer.style.height = (document.body.clientHeight - 35) + 'px';

		o_textbox.style.width = (document.body.clientWidth - 35) + 'px';
		o_textbox.style.height = (document.body.clientHeight - 70) + 'px'; 
	}
    
    o_textbox.select();
}

function addBrackets()
{
	 o_textbox.focus();
	 var r = document.selection.createRange();         
	 if (r != null)
     {
	    var s = r.htmlText;
        r.text = "[[" + s + "]";
     }	
}

function doKeyDown()
{
	if(navigator.userAgent.indexOf("Firefox")!=-1)
	{
		var event = getEvent();
        var key = event.keyCode;
        // keys with ctrl modifier
        if (event.ctrlKey && !event.altKey)
        {
	        if (key == 81) // "Q" 
	        {
	        event.stopPropagation();
	        event.preventDefault();
	        parent.parent.doClose();
	        return;
	        }
	        else if (key == 83) // "S" 
	        {
	        event.stopPropagation();
	        event.preventDefault();
	        parent.parent.doRefresh(0, true);
	        return;
	        }
	        else if (key == 33) // PAGE UP
	        {
	        event.stopPropagation();
	        event.preventDefault();
	        parent.parent.doRefresh(-1, false);
	        return;
	        }
	        else if (key == 34) // PAGE DOWN
	        {
	        event.stopPropagation();
	        event.preventDefault();
	        parent.parent.doRefresh(1, false);
	        return;
	        }
        }
	}
	else
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
	  // keys with ctrl modifier
	  if (event.ctrlKey && !event.altKey)
	  {
		    if (key == 81) // "Q" 
		    {
		      event.cancelBubble = true;
		      event.returnValue = false;
		      parent.parent.doClose();
		      return;
		    }
		    else if (key == 83) // "S" 
		    {
		      event.cancelBubble = true;
		      event.returnValue = false;
		      parent.parent.doRefresh(0, true);
		      return;
		    }
		    else if (key == 33) // PAGE UP
		    {
		      event.cancelBubble = true;
		      event.returnValue = false;
		      parent.parent.doRefresh(-1, false);
		      return;
		    }
		    else if (key == 34) // PAGE DOWN
		    {
		      event.cancelBubble = true;
		      event.returnValue = false;
		      parent.parent.doRefresh(1, false);
		      return;
		    }
      }
	}
	
}

function SetSegment(s, changed)
{
    o_textbox.value = s;
    o_textbox.blur();
    setTimeout("", 0);
    o_textbox.focus();
   // o_textbox.select();

    if (changed)
    {
	SetChanged();
    }
    else
    {
        SetUnchanged();
    }
}

function GetSegment()
{
    return o_textbox.value;
}

function SetFocus()
{
    o_textbox.focus();
}

function IsChanged()
{
    var result;

    // IE5.0 only fires onchanged before onblur when the element has focus
    o_textbox.focus();
    o_textbox.blur();
    o_textbox.focus();
    result = b_changed;

    return result;
}
      
function SetChanged()
{
    b_changed = true;
}

function SetUnchanged()
{
    b_changed = false;
}

function InsertTerm(term)
{
    if(document.all) {
        var range = o_textbox.createTextRange();
        range.moveStart("textedit");
        range.text = term;
    }
    else {
        o_textbox.focus();
	      var original = o_textbox.value;
	      o_textbox.value = term + original;
    }
}

function CanClose()
{
    return !IsChanged();
}

function addLre()
{
  addText("\u202a");
}

function addPdf()
{
  addText("\u202c");
}

function addText(strText)
{
  clipboardData.setData("text", strText);
  o_textbox.focus();
  document.execCommand("paste");   
}

</SCRIPT>
</HEAD>
<BODY scroll="no" onbeforeunload="doOnBeforeUnload();" onresize="init();">

<HR COLOR="#0C1476" WIDTH="95%">

<TABLE cellspacing="0" id="toolBar" width="98%">
<TR>
  <TD align="left">
    &nbsp;<SPAN CLASS="standardTextBold"><%=lb_target%></SPAN>
  </TD>
  <td width="100%">&nbsp;</td>
  <td align="right">
    <table cellspacing="0">
      <tr>
	<td class="coolButton" id="idSpellCheck" onclick="spellCheck()">
	  <img src="/globalsight/envoy/edit/online2/Spellcheck2.gif">
	</td>
	<td class="coolButton" id="idBrackets" onclick="addBrackets()"  title="Square Brackets">
	    <img src="/globalsight/envoy/edit/online2/Brackets.gif" class="middle">	    
	</td>
	<td class="coolButton" id="idButLtr" onclick="addLre()">
	    &nbsp;[LRE]&nbsp;
	</td>
	<td class="coolButton" id="idButRtl" onclick="addPdf()">
	    &nbsp;[PDF]&nbsp;
	</td>
      </tr>
    </table>
  </td>
</TR>
</TABLE>

<DIV ID="targetLayer" STYLE="position: relative; left: 0px; top: 5px; right:20px; overflow: auto;">
<TABLE CELLPADDING="1" CELLSPACING="0" BORDER="0" width="98%">
<TR>
  <TD><IMG SRC="/globalsight/images/spacer.gif" WIDTH="10" HEIGHT="1"></TD>
  <TD VALIGN="TOP" align="left">
    <FORM NAME="targetForm" style="display: inline">
    <TEXTAREA WRAP COLS="42" ROWS="4" CLASS="standardText" <%=str_langAttr%>
     NAME="targetText" ID="textarea"
     onchange="SetChanged()" onkeydown="doKeyDown()"
     TABINDEX="1" STYLE="overflow: auto;"></TEXTAREA>
    </FORM>
  </TD>
</TR>
</TABLE>
</DIV>

<form name="frmSC"><input type="hidden" name="language" value=""></form>
<script>
init();
parent.parent.finishFrame();
</script>

</BODY>
</HTML>
