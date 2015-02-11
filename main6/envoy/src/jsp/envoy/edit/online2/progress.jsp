<%@ page 
    contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="com.globalsight.everest.webapp.webnavigation.LinkHelper,
        java.util.ResourceBundle,
        com.globalsight.everest.webapp.pagehandler.PageHandler,
        com.globalsight.everest.webapp.WebAppConstants,
        com.globalsight.everest.servlet.util.SessionManager"
    session="true"
%>
<%
ResourceBundle bundle = PageHandler.getBundle(session);

String lb_title = bundle.getString("lb_progress_window");
String lb_help = bundle.getString("lb_help");
String lb_close = bundle.getString("lb_close");

String helpFile = bundle.getString("help_progress_window");
%>
<HTML>
<HEAD>
<!-- This JSP page is abandoned since 8.5.2 version Dec. 2013. -->
<TITLE><%=lb_title%></TITLE>
<META http-equiv="expires" CONTENT="0">
<STYLE TYPE="text/css">
@import url("/globalsight/includes/coolbutton2.css");
 BODY   { margin: 10px; background-color: white;
          font-family: Verdana; font-size: 10pt; 
        }
 BUTTON { width: 200px; font-family: Arial; font-size: 10pt; }
.title  { text-align: left; font-weight: bold; }
.text   { text-align: left; font-weight: normal; }
.help   { cursor: hand; color: blue; }

#idPreview { position: absolute; display: none;
             top: 30; left: 200; width: 182; height: 205;
             overflow-x: auto; overflow-y: auto;
             border: 1px solid black;
           }
#idPreviewText { zoom: 35%; /* 30% is a good value */
                 -moz-transform:translate(-30px,-380px) scale(0.5,0.3);/*for firefox*/
               } 
#idPreviewText A { cursor: default; text-decoration: none; }

#butTranslated,
#butUntranslated,
#butTouched,
#butNormal { width: 110px; }

#butFirstUntranslated,
#butFirstFuzzy { width: 150px; }
</STYLE>
<link type="text/css" rel="StyleSheet" id="cssEditorMain"
  href="/globalsight/envoy/edit/online/editor.css">
<link type="text/css" rel="StyleSheet" id="cssEditorMainTouched" disabled
  href="/globalsight/envoy/edit/online/editorTouched.css">
<link type="text/css" rel="StyleSheet" id="cssEditorMainTranslated" disabled
  href="/globalsight/envoy/edit/online/editorTranslated.css">
<link type="text/css" rel="StyleSheet" id="cssEditorMainUntranslated" disabled
  href="/globalsight/envoy/edit/online/editorUntranslated.css">
<link type="text/css" rel="StyleSheet" id="cssEditor"
  href="/globalsight/envoy/edit/online2/editor.css">
<link type="text/css" rel="StyleSheet" id="cssEditorTouched" disabled
  href="/globalsight/envoy/edit/online2/editorTouched.css">
<link type="text/css" rel="StyleSheet" id="cssEditorTranslated" disabled
  href="/globalsight/envoy/edit/online2/editorTranslated.css">
<link type="text/css" rel="StyleSheet" id="cssEditorUntranslated" disabled
  href="/globalsight/envoy/edit/online2/editorUntranslated.css">
<script src="/globalsight/includes/coolbutton2.js"></script>
<SCRIPT>
var g_opener = null;
var g_fromInlineEditor = false;
var isChrome = window.navigator.userAgent.indexOf("Chrome") > -1;
var g_showPreview = false;

var helpFile = "<%=helpFile%>";

function showHelp()
{
    var helpWindow = g_opener.window.open(helpFile, 'helpWindow',
      'resizable=yes,scrollbars=yes,WIDTH=600,HEIGHT=400');
    helpWindow.focus();
}

function doKeyPress()
{
  var key = event.keyCode;

  if (key == 27) // ESC
  {
    window.close();
  }
}

function cancelEvent()
{
    if (window.event != null)
    {
        window.event.returnValue = false;
        window.event.cancelBubble = true;
    }

    return false;
}
  
// nullify editing functions
function edit()
{
  return cancelEvent();
}

function SE()
{
  return cancelEvent();
}
  
function hlTouched()
{
  cssEditorMain.disabled = true;
  cssEditorMainTouched.disabled = false;
  cssEditorMainTranslated.disabled = true;
  cssEditorMainUntranslated.disabled = true;
  cssEditor.disabled = true;
  cssEditorTouched.disabled = false;
  cssEditorTranslated.disabled = true;
  cssEditorUntranslated.disabled = true;
  
  var pct = g_opener.HighlightTouched();
  if (pct)
  {
    if(document.recalc)
    {
    	idTouched.innerText = '(' + pct + '%)';
    }
	else
	{
		idTouched.textContent = '(' + pct + '%)';
	}
  }
}

function hlTranslated()
{
  cssEditorMain.disabled = true;
  cssEditorMainTouched.disabled = true;
  cssEditorMainTranslated.disabled = false;
  cssEditorMainUntranslated.disabled = true;
  cssEditor.disabled = true;
  cssEditorTouched.disabled = true;
  cssEditorTranslated.disabled = false;
  cssEditorUntranslated.disabled = true;
  
  var pct = g_opener.HighlightTranslated();
  if (pct)
  {
    if(document.recalc)
    {
    	idTranslated.innerText = '(' + pct + '%)';
    }
    else
    {
    	idTranslated.textContent = '(' + pct + '%)';
    }
   
  }
}

function hlUntranslated()
{
  cssEditorMain.disabled = true;
  cssEditorMainTouched.disabled = true;
  cssEditorMainTranslated.disabled = true;
  cssEditorMainUntranslated.disabled = false;
  cssEditor.disabled = true;
  cssEditorTouched.disabled = true;
  cssEditorTranslated.disabled = true;
  cssEditorUntranslated.disabled = false;

  var pct = g_opener.HighlightUntranslated();
  if (pct)
  {
  	if(document.recalc)
    {
    	idUntranslated.innerText = '(' + pct + '%)';
    }
    else
    {
    	idUntranslated.textContent = '(' + pct + '%)';
    }
  }
}

function hlNormal()
{
  cssEditorMain.disabled = false;
  cssEditorMainTouched.disabled = true;
  cssEditorMainTranslated.disabled = true;
  cssEditorMainUntranslated.disabled = true;
  cssEditor.disabled = false;
  cssEditorTouched.disabled = true;
  cssEditorTranslated.disabled = true;
  cssEditorUntranslated.disabled = true;
  
  g_opener.HighlightNormal();

  if(document.recalc)
  {
  	 idTouched.innerText = '';
 	 idTranslated.innerText = '';
  	 idUntranslated.innerText = '';
  }
  else
  {
  	 idTouched.textContent = '';
 	 idTranslated.textContent = '';
  	 idUntranslated.textContent = '';
  }
 
}

function togglePreview(p_checked)
{
  butPreview.checked = !p_checked;

  g_showPreview = butPreview.checked;

  var isIE = true;
  if(navigator.userAgent.indexOf("MSIE")>0) 
  {
	  if (g_showPreview)
	  {
	    window.dialogWidth = "400px";
	    idPreview.style.display = 'block';
	    showPreview();
	  }
	  else
	  {
	    window.dialogWidth = "226px";
	    idPreview.style.display = 'none';
	    idPreviewText.innerHTML = '';
	  }
  }
  else
  {
	  if (g_showPreview)
	  {
//	    window.width = "400px";
	    idPreview.style.display = 'block';
	    showPreview();
	  }
	  else
	  {
//	    window.width = "220px";
	    idPreview.style.display = 'none';
	    idPreviewText.innerHTML = '';
	  }
  }

}

function showPreview()
{
  idPreviewText.innerHTML = g_opener.GetSegmentPreview();
  if(isChrome){
	  document.getElementById("idPreview").style.left = 240;
  }
  // disable main editor links
  var links = idPreviewText.getElementsByTagName("A");
  if (links)
  {
      for (var i = 0; i < links.length; ++i)
      {
          var link = links[i];

          link.onclick = cancelEvent;
      }
  }
}

function jumpFirstUntranslated()
{
  g_opener.JumpFirstUntranslated();

  // If too slow, remove.
  if (g_showPreview)
  {
    showPreview();
  }
}

function jumpFirstFuzzy()
{
  g_opener.JumpFirstFuzzy();

  // If too slow, remove.
  if (g_showPreview)
  {
    showPreview();
  }
}

function doRadio(but)
{
    butTranslated.setValue(but == butTranslated);
    butUntranslated.setValue(but == butUntranslated);
    butTouched.setValue(but == butTouched);
    butNormal.setValue(but == butNormal);
}

function doOnLoad()
{
  if(document.recalc)
  {
  	  g_opener = window.dialogArguments._opener;
  	  g_fromInlineEditor = window.dialogArguments._data;
  }
  else
  {
  	  g_opener = window.opener;
  	  g_fromInlineEditor = window.opener.myArguments;
  }
  createButton(butTranslated);
  createButton(butUntranslated);
  createButton(butTouched);
  createButton(butNormal);
  createButton(butFirstUntranslated);
  createButton(butFirstFuzzy);

  butTranslated.setAlwaysUp(true);
  butUntranslated.setAlwaysUp(true);
  butTouched.setAlwaysUp(true);
  butNormal.setAlwaysUp(true);
  butFirstUntranslated.setAlwaysUp(true);
  butFirstFuzzy.setAlwaysUp(true);

  butTranslated.setToggle(true);
  butUntranslated.setToggle(true);
  butTouched.setToggle(true);
  butNormal.setToggle(true);

  butTranslated.setValue(false);
  butUntranslated.setValue(false);
  butTouched.setValue(false);
  butNormal.setValue(true);
  
  try { hlNormal(); } catch (ignore) {}
}

function doOnUnload()
{
  try { hlNormal(); } catch (ignore) {}

}
</SCRIPT>
</HEAD>

<BODY onload="doOnLoad()" onbeforeunload="doOnUnload()" onkeypress="doKeyPress()">
<span style="float:right;margin-right:5px;cursor:pointer" class="help" onclick="showHelp()"><%=lb_help%></span>
<DIV class="title" style="margin-bottom: 6px"><%=lb_title%></DIV>

<DIV class="text" style="margin-bottom: 6px"><%=bundle.getString("lb_highlight_segments") %>:</DIV>

<DIV style="margin-left: 20px; margin-bottom: 6px;">
  <span id="butTranslated" onaction="doRadio(this); hlTranslated()"
  ><%=bundle.getString("lb_translated_text") %></span>
  <span id="idTranslated"></span>
</DIV>
<DIV style="margin-left: 20px; margin-bottom: 6px;">
  <span id="butUntranslated" onaction="doRadio(this); hlUntranslated()"
  ><%=bundle.getString("lb_untranslated_text") %></span>
  <span id="idUntranslated"></span>
</DIV>
<DIV style="margin-left: 20px; margin-bottom: 6px;">
  <span id="butTouched" onaction="doRadio(this); hlTouched()"><%=bundle.getString("lb_touched_text") %></span>
  <span id="idTouched"></span>
</DIV>
<DIV style="margin-left: 20px; margin-bottom: 6px;">
  <span id="butNormal" onaction="doRadio(this); hlNormal()"><%=bundle.getString("lb_reset_to_normal") %></span>
</DIV>
<DIV style="margin-left: 20px; margin-bottom: 6px;">
  <span onclick="togglePreview(butPreview.checked);"><%=bundle.getString("lb_show_zoom") %></span>
  <input id="butPreview" type="checkbox" onclick="togglePreview(!this.checked);"
   tabindex="-1">
</DIV>

<DIV class="text" style="margin-bottom: 6px"><%=bundle.getString("lb_jump_to") %>:</DIV>

<DIV style="margin-left: 20px; margin-bottom: 6px;">
  <span id="butFirstUntranslated" onaction="jumpFirstUntranslated()"
  ><%=bundle.getString("lb_first_untrans_segment") %></span>
</DIV>
<DIV style="margin-left: 20px; margin-bottom: 6px;">
  <span id="butFirstFuzzy" onaction="jumpFirstFuzzy()"
  ><%=bundle.getString("lb_first_fuzzy_segment") %></span>
</DIV>

<%-- Let's see who wants a close button for an idiot window.
<P ALIGN="center">
<BUTTON onclick="window.close();" style="width: 5em"><%=lb_close%></BUTTON>
</P>
--%>

<div id="idPreview"><div id="idPreviewText"></div></div>

</BODY>
</HTML>
