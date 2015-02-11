<%@ page
    contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="java.util.*,com.globalsight.everest.webapp.javabean.NavigationBean,
            com.globalsight.everest.webapp.pagehandler.PageHandler,
            com.globalsight.everest.webapp.pagehandler.edit.online.EditorConstants,
            com.globalsight.everest.webapp.pagehandler.edit.online.EditorState,
            com.globalsight.util.GlobalSightLocale,
            com.globalsight.util.edit.EditUtil,
            com.globalsight.everest.taskmanager.Task,
            com.globalsight.everest.foundation.User,
            com.globalsight.everest.servlet.util.SessionManager,
            com.globalsight.everest.webapp.WebAppConstants,
            java.util.Locale,
            java.util.ResourceBundle"
    session="true"
%>
<jsp:useBean id="load" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean"/>
<jsp:useBean id="save" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean"/>
<jsp:useBean id="close" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean"/>
<jsp:useBean id="validation" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean"/>
<jsp:useBean id="pageInfo" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean"/>
<jsp:useBean id="options" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean"/>
<!--
http://localhost:7001/globalsight/ControlServlet?linkName=sourceEditor&pageName=DTLS&sourcePageId=1004&jobId=1004
http://localhost:7001/globalsight/ControlServlet?linkName=sourceEditor&pageName=DTLS&sourcePageId=1007&jobId=1005
http://localhost:7001/globalsight/ControlServlet?linkName=sourceEditor&pageName=DTLS&sourcePageId=1013&jobId=1007
http://localhost:7001/globalsight/ControlServlet?linkName=sourceEditor&pageName=DTLS&sourcePageId=1015&jobId=1008

Skeletons will be consolidated into the minimum pieces necessary to maintain
valid GXML structure.
Translatables will be consolidated as well so that adjacent segments are
contained in the same translatable.

SKELETON:
    - edit
SKELETON selection:
        + extract as translatable
        + extract as localizable
        - insert GS tag (at end of skel, after selection)

GS TAG:
        - edit (dialog)
        - delete (combines skeleton before+after)

TRANSLATABLE:
        - edit
        - insert segment before/after
        + unextract (combine with prev+next skel)
        - delete (drop and combine prev+next skel)

LOCALIZABLE:
        - edit
        + unextract (combine with prev+next skel)
        - delete (drop and combine prev+next skel)

-->
<%
ResourceBundle bundle = PageHandler.getBundle(session);
Locale uiLocale = (Locale)session.getAttribute(WebAppConstants.UILOCALE);

SessionManager sessionMgr = (SessionManager)session.getAttribute(
  WebAppConstants.SESSION_MANAGER);
EditorState state =
  (EditorState)sessionMgr.getAttribute(WebAppConstants.EDITORSTATE);

String str_userId =
  ((User)sessionMgr.getAttribute(WebAppConstants.USER)).getUserId();

String url_load       = load.getPageURL();
String url_save       = save.getPageURL();
// url_close is below
String url_validation = validation.getPageURL();
String url_pageInfo   = pageInfo.getPageURL();
String url_options    = options.getPageURL();

String str_pageHtml = state.getTargetPageHtml();
String str_dataType = state.getPageFormat();

String str_pageName = state.getSourcePageName();
int ii;
if ((ii = str_pageName.lastIndexOf('\\')) >= 0 ||
    (ii = str_pageName.lastIndexOf('/')) >= 0)
{
  str_pageName = str_pageName.substring(ii + 1);
}

boolean b_readonly = state.isReadOnly();

// TODO: need state for extraction review and full source page editing (+GSTag)

String lb_title = "GXML Editor" /*bundle.getString("lb_main_editor")*/;
String lb_pageTitle = lb_title + " - " + str_pageName;
String lb_loading = "Loading...";
String lb_saving = "Saving...";
String lb_validating = "Validating...";

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

// Close goes back to job details
String jobId = (String)request.getParameter(WebAppConstants.JOB_ID);
String closeUrl = close.getPageURL() +
   "&" + WebAppConstants.JOB_ID + "=" + jobId + "&fromJobs=true";
%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<html>
<head>
<title><%=lb_pageTitle%></title>
<script src="/globalsight/envoy/edit/online2/applet.js"></script>
<script src="/globalsight/envoy/edit/online2/stringbuilder.js"></script>
<script src="/globalsight/envoy/edit/sourcepage/editor.js"></script>
<script language="JavaScript1.5" src="/globalsight/includes/ieemu.js"></script>
<script src="/globalsight/includes/coolbutton2.js"></script>
<link type="text/css" rel="StyleSheet"
  href="/globalsight/includes/coolbutton2.css">
<link type="text/css" rel="StyleSheet"
  href="/globalsight/envoy/edit/sourcepage/editor.css">
<link type="text/css" rel="StyleSheet"
  href="/globalsight/includes/menu/skins/winclassic.css">
<link type="text/css" rel="StyleSheet"
  href="/globalsight/includes/ContextMenu.css">
<script src="/globalsight/includes/menu/js/poslib.js"></script>
<script src="/globalsight/includes/menu/js/scrollbutton.js"></script>
<script src="/globalsight/includes/menu/js/menu4.js"></script>
<script src="/globalsight/includes/ContextMenu.js"></script>
<script src="/globalsight/includes/xmlextras.js"></script>
<script src="/globalsight/envoy/terminology/viewer/error.js"></script>
<style>
.menu-button .middle { width: 100%; }
.menu-bar {
            /* the border causes the vertical line between menu and "close" */
            border-right: 2px groove; width: 53px;
            display: inline; background: #0C1476;
          }
.menu-bar .menu-button {
           background: #0C1476; color: white;
           font-size: 8pt;
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
          cursor: hand;
          float: right;
        }
.legend {
          font: menu; font-size: 10pt !important;
        }
  
/* #idEditor   { font-family: Arial Unicode MS, Arial, sans-serif; } */
#idEditor   {
              font-family: Courier New, monospace;
              font-size: small;
            }
#idEditor P {
                margin-top: 0px;
                margin-bottom: 0px;
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
<script>
// segment that has "focus", i.e. was opened last.
var g_current = null;

var g_uiLocale = "<%=str_uiLocale%>";
var g_userId = "<%=str_userId%>";
var g_sourceLocale = "<%=str_sourceLocale%>";
var g_defaultDataType = "<%=str_dataType%>";
var g_datatype;
var g_defaultItemType = "text";
var g_itemtype;
var g_readonly = eval("<%=b_readonly%>");

var w_validation = null;
var w_pageinfo = null;
var w_options = null;

var helpFile = "<%=bundle.getString("help_paragraph_editor")%>";

function showHelp()
{
    var helpWindow = window.open(helpFile, 'helpWindow',
      'resizable=yes,scrollbars=yes,WIDTH=600,HEIGHT=400');
    helpWindow.focus();
}

// Menus

Menu.prototype.cssFile = "/globalsight/includes/menu/skins/winclassic.css";
Menu.prototype.mouseHoverDisabled = false;
Menu.prototype.showTimeout = 5;
Menu.prototype.closeTimeout = 5;
MenuButton.prototype.subMenuDirection = "vertical";
Menu.keyboardAccelKey = -1;
Menu.keyboardAccelProperty = "";

// Main Menu
var actionMenu = new Menu();
actionMenu.add(tmp = new MenuItem("Preview GXML", previewGxml));
tmp.mnemonic = "p";
actionMenu.add(tmp = new MenuItem("Preview Original", previewOrigFormat));
tmp.mnemonic = "o";
if (isHtmlDatatype(g_defaultDataType))
{
    actionMenu.add(tmp = new MenuItem("Preview Source", previewSource));
    tmp.mnemonic = "r";
    actionMenu.add(tmp = new MenuItem("Preview Target...", previewTarget));
    tmp.mnemonic = "t";
}
actionMenu.add(tmp = new MenuItem("Validate", validateGxml));
tmp.mnemonic = "v";
actionMenu.add(tmp = new MenuItem("Save &amp; Close", saveGxml));
tmp.mnemonic = "s";
//actionMenu.add(tmp = new MenuItem("Options", null));
//tmp.disabled = true;
//tmp.mnemonic = "o";

var menuBar = new MenuBar();
menuBar.add(tmp = new MenuButton("Actions", actionMenu));

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

function cancelEvent()
{
    event.cancelBubble = true;
    event.returnValue = false;
    return false;
}

function doKeyPress()
{
    var key = event.keyCode;
    var ch = String.fromCharCode(event.keyCode);

    if (ch.length > 0 ||
        key == 46 || // DELETE
        key == 13 || // RETURN
        key == 8)    // BACKSPACE
    {
        g_dirty = true;
    }
}

function doKeyDown()
{
    var key = event.keyCode;
    // window.status = String(key);

    if (event.ctrlKey && (key == 46 || key == 8)) // ^DEL and ^BKSP
    {
        g_dirty = true;
    }

    if (event.keyCode == 13) // "RETURN"
    {
        // Find out if event happened in editable text
        if (document.selection.type != 'Control')
        {
            var oRange = document.selection.createRange();

            oRange.pasteHTML("<BR>&nbsp;");
            //var oRange = document.selection.createRange();
            oRange.moveStart('character', -1);
            oRange.select();
            document.selection.clear();
        }
        return cancelEvent();
    }

    if (event.shiftKey && !event.ctrlKey && !event.altKey)
    {
        if (event.keyCode == 121) // Shift-F10
        {
            // TODO: make Shift-F10 bring up the correct context menu
            return cancelEvent();
        }
    }

    if (event.ctrlKey && !event.altKey)
    {
        if (key == 66) // Ctrl-B
        {
            return cancelEvent();
        }
        else if (key == 73) // Ctrl-I
        {
            return cancelEvent();
        }
        else if (key == 75) // Ctrl-K
        {
            return cancelEvent();
        }
        else if (key == 76) // Ctrl-L
        {
            return cancelEvent();
        }
        else if (key == 83) // Ctrl-S
        {
            return cancelEvent();
        }
        else if (key == 85) // Ctrl-U
        {
            return cancelEvent();
        }
        else if (key == 87) // Ctrl-W (closes window)
        {
            return cancelEvent();
        }
    }

    if (!event.ctrlKey && event.altKey)
    {
    }
}

var g_newString = '';
var g_canCut = true;
var g_canPaste = true;

function fnBeforeCut()
{
    var rng = document.selection.createRange();
    var parent = rng.parentElement();
    if (parent == idEditor)
    {
        window.status = "Selection not within single element, can't cut.";
        g_canCut = false;
        event.returnValue = true;
        return true;
    }

    g_canCut = true;
    g_newString = document.selection.createRange().text;
    // window.status = 'cutting text: ' + g_newString;
    event.returnValue = false;
    return false;
}

function fnCut()
{
    if (g_canCut)
    {
        document.selection.createRange().text = '';
        window.clipboardData.setData("Text", g_newString);
        g_dirty = true;
    }
    
    return false;
}

function fnBeforeCopy()
{
    g_newString = document.selection.createRange().text;
    event.returnValue = false;
    return false;
}

function fnCopy()
{
    window.clipboardData.setData("Text", g_newString);
    return false;
}

function fnBeforePaste()
{
    var rng = document.selection.createRange();
    var parent = rng.parentElement();
    if (parent == idEditor)
    {
        window.status = "Selection not within single element, can't paste.";
        g_canPaste = false;
        event.returnValue = true;
        return true;
    }

    g_canPaste = true;
    event.returnValue = false;
    return false;
}

function fnPaste()
{
    if (g_canPaste)
    {
        var text = window.clipboardData.getData("Text");
        text = encodeHtmlEntities(text);
        text = text.replace(/\r?\n/g, "<BR>");

        document.selection.createRange().pasteHTML(text);

        g_dirty = true;
    }

    event.returnValue = false;
    return false;
}

function initEditor()
{
    var dom = XmlDocument.create();
    dom.preserveWhiteSpace = true;
    dom.async = false;
    dom.load('<%=url_load%>&action=load');

    //debug(dom.xml);

    if (dom.documentElement.nodeName == 'exception')
    {
        ShowError(dom.documentElement.text);
        alert("The editor cannot be initialized and will be closed.");
        closeEditor();
    }
    else
    {
        initEditorFromGxml(dom, idEditor);
    }

    window.setTimeout("idLoading.style.display = 'none';", 250, "JavaScript");
    window.defaultStatus = '';
}

function validateGxml()
{
    idBody.style.cursor = 'wait';
    idValidating.style.display = 'block';

    window.setTimeout("validateGxml2()", 10);
}

function validateGxml2()
{
    try
    {
        if (!verifyGsTags(idEditor))
        {
            alert("GS region tags are unbalanced, please fix.");
            return;
        }

        var dom = HtmlToGxml(idEditor);

        var xmlhttp = XmlHttp.create();
        var async = false;
        xmlhttp.open('POST', '<%=url_load%>&action=validate', async);
        xmlhttp.send(dom);

        //debug("Response = " + xmlhttp.responseXML.xml);

        var response = xmlhttp.responseXML;
        if (response.documentElement.nodeName == 'exception')
        {
            ShowError(response.documentElement.text);
        }
        else if (response.documentElement.nodeName == 'errors')
        {
            showValidationMessages(response.documentElement);
        }
        else
        {
            alert("The source page was validated successfully.");
        }
    }
    finally
    {
        idBody.style.cursor = 'auto';
        idValidating.style.display = 'none';
    }
}

function showValidationMessages(p_elem)
{
    if (w_validation && !w_validation.closed)
    {
        w_validation.close();
    }
    w_validation = window.showModelessDialog('<%=url_validation%>',
        { element: p_elem, window: window },
        'dialogWidth:600px;dialogHeight:400px;' +
        'toolbar:no;scrollbars:yes;status:no;resizable:yes;help:no;');
}

function saveGxml()
{
    if (!isEditableDatatype(g_gxmlDatatype))
    {
        alert("You were warned. Only HTML files can be edited.");
        return;
    }

    if (!verifyGsTags(idEditor))
    {
        alert("GS region tags are unbalanced, please fix.");
        return;
    }

    idBody.style.cursor = 'wait';
    idSaving.style.display = 'block';

    window.setTimeout("saveGxml2()", 10);
}

function saveGxml2()
{
    var b_close = false;

    try
    {
        var dom = HtmlToGxml(idEditor);

        var xmlhttp = XmlHttp.create();
        var async = false;
        xmlhttp.open('POST', '<%=url_load%>&action=save', async);
        xmlhttp.send(dom);

        //debug("Response = " + xmlhttp.responseXML.xml);

        var response = xmlhttp.responseXML;
        if (response.documentElement.nodeName == 'exception')
        {
            ShowErrors(response.documentElement.text);
        }
        else
        {
            b_close = true;
            alert("The source page was accepted by the server and is now being updated in the background.\n\nPlease wait until you receive the confirmation email.\n\The editor will close now.");
        }
    }
    finally
    {
        idBody.style.cursor = 'auto';
        idSaving.style.display = 'none';
    }

    if (b_close)
    {
        g_dirty = false;
        closeEditor();
    }
}

function closeEditor()
{
    if (g_dirty && !confirm("The editor contains unsaved changes.\n\n" +
        "Press OK to close the editor or CANCEL to continue editing."))
    {
        return;
    }

    window.navigate("<%=closeUrl%>");
}

function previewSource()
{
    idBody.style.cursor = 'wait';
    idLoading.style.display = 'block';

    window.setTimeout("previewTarget2(g_sourceLocale)", 10);
}

function previewTarget()
{
    if (!isHtmlDatatype(g_gxmlDatatype))
    {
        alert("Only HTML-based formats can be previewed.");
        return;
    }

    if (!verifyGsTags(idEditor))
    {
        alert("GS region tags are unbalanced, please fix.");
        return;
    }

    var res = showModalDialog(
        '/globalsight/envoy/edit/sourcepage/selTargetLocale.jsp', null,
        "dialogHeight:212px; dialogWidth:330px; center:yes; " +
        "resizable:no; status:no; help:no;");

    if (res)
    {
        idBody.style.cursor = 'wait';
        idLoading.style.display = 'block';

        window.setTimeout("previewTarget2('" + res + "')", 10);
    }
}

function previewTarget2(p_locale)
{
    try
    {
        var dom = HtmlToGxml(idEditor);

        var xmlhttp = XmlHttp.create();
        var async = false;
        xmlhttp.open('POST',
          '<%=url_load%>&action=preview&locale=' + p_locale, async);
        xmlhttp.send(dom);

        //debug("Response = " + xmlhttp.responseXML.xml);

        var response = xmlhttp.responseXML;
        if (response.documentElement.nodeName == 'exception')
        {
            ShowError(response.documentElement.text);
        }
        else
        {
            w_preview = window.open('', 'gxmlPreview',
                'toolbar=yes,scrollbars=yes,status=no,resizable=yes');
            var d = w_preview.document.open('text/html');
            d.write(response.documentElement.text);
            d.close();
        }
    }
    finally
    {
        idBody.style.cursor = 'auto';
        idLoading.style.display = 'none';
    }
}

function setButtonState()
{
    if (document.getElementById('idBut1'))
    {
        idBut1.setEnabled(true);
    }
    idBut2.setEnabled(true);
    idBut3.setEnabled(true);
    idBut4.setEnabled(true);
    idBut5.setEnabled(true);
    if (isHtmlDatatype(g_defaultDataType))
    {
        idBut6.setEnabled(true);
        idBut7.setEnabled(true);
    }
    else
    {
        idBut6.setEnabled(false);
        idBut7.setEnabled(false);
    }
}

function doInit()
{
    ContextMenu.intializeContextMenu();

    var table = document.getElementById("idMenuBar");
    var cells = table.rows[0].cells;
    for (var i = 0; i < cells.length; i++)
    {
        // 7 is a spacer cell (6 without init button)
        if (i == 4 || i > 6) continue;
        createButton(cells[i]);
    }
    setButtonState();

    document.recalc(true);

    // Check for shutdown every 5 minutes
    checkShutdown(1000 * 60 * 5);

    window.setTimeout("initEditor();", 10, "Javascript");
}

function doExit()
{
    try { w_preview.close(); } catch(ignore) {}
    try { w_validation.close(); } catch(ignore) {}
}
</SCRIPT>
</head>

<body id="idBody" onkeydown="return doKeyDown()" onkeypress="return doKeyPress()"
 oncontextmenu="return contextForX(this)"
 onload="doInit();" onbeforeunload="doExit()" scroll="no">

<div id="idLoading" style="position: absolute; top: 100; left: 200;
background-color: lightgrey; color: black; text-align: center;
border: 2px solid black; padding: 10 100; font-size: 14pt; z-index: 99;">
<%=lb_loading%> <img src="/globalsight/envoy/edit/online2/bullet2.gif">
</div>
<div id="idValidating" style="position: absolute; top: 100; left: 200;
background-color: lightgrey; color: black; text-align: center; display: none;
border: 2px solid black; padding: 10 100; font-size: 14pt; z-index: 99;">
<%=lb_validating%> <img src="/globalsight/envoy/edit/online2/bullet2.gif">
</div>
<div id="idSaving" style="position: absolute; top: 100; left: 200;
background-color: lightgrey; color: black; text-align: center; display: none;
border: 2px solid black; padding: 10 100; font-size: 14pt; z-index: 99;">
<%=lb_saving%> <img src="/globalsight/envoy/edit/online2/bullet2.gif">
</div>

<div class="header" style="position: absolute; top: 0; left: 0; right: 0;
 width: expression(idBody.clientWidth); height: 30;
 padding-left: 10px; padding-top: 6px;">
    <SPAN id="idHelpViewer" onclick="showHelp()" class="help"
     style="margin-right: 10px;">Help</SPAN>
    <SPAN class="help">&nbsp;|&nbsp;</SPAN>
    <SPAN id="idCloseEditor" class="help" onclick="closeEditor()">Close</SPAN>
    <SPAN class="help" style="position:relative; top:-4;"><script>menuBar.write()</script>&nbsp;&nbsp;&nbsp;</SPAN>

    <P><%=lb_title%>: <span id="idPagename"><%=str_pageName%></span></P>
</div>

<div id="idMenuBarDiv" class="toolBar"
 style="position: absolute; top: 30; left: 0;
 width: expression(idBody.clientWidth);">
<table id="idMenuBar" cellspacing="0" cellpadding="0" cellspacing="0">
  <tr>
<!--
    <td id="idBut1" onaction="initEditor()" style="font-size: 9pt;">Init</td>
-->
    <td id="idBut3" onaction="previewGxml()" style="font-size: 9pt;"
      >Preview GXML</td>
    <td id="idBut4" onaction="previewOrigFormat()" style="font-size: 9pt;"
      >Preview Original</td>
    <td id="idBut5" onaction="previewSource()" style="font-size: 9pt;"
      >Preview Source</td>
    <td id="idBut6" onaction="previewTarget()" style="font-size: 9pt;"
      >Preview Target</td>
    <td width="12px"></td>
    <td id="idBut2" onaction="validateGxml()" style="font-size: 9pt;"
      >Validate</td>
    <td id="idBut7" onaction="saveGxml()" style="font-size: 9pt;"
      >Save &amp; Close&nbsp;</td>
    <td width="100px"></td>
    <td class="legend" nowrap><div class="translatable" style="border: 0px"
      title="Marks translatable text or translatable Javascript strings."
      >&nbsp;translatable&nbsp;</div></td>
    <td width="10px"></td>
    <td class="legend" nowrap><div class="localizable"
      title="Marks localizable tokens like URLs."
      >&nbsp;localizable&nbsp;</div></td>
    <td width="10px"></td>
    <td class="legend" nowrap><div class="gs"
      title="Marks locale-specific additions or deletions of text."
      >&nbsp;GS Tag&nbsp;</div></td>
  </tr>
</table>
</div>

<div id="idEditor" contenteditable style="position: absolute;
  top: 47; left: 0;  
  width: expression(idBody.clientWidth);
  height: expression(idBody.clientHeight - 47);
  border: solid 0 black; overflow: auto; padding: 8px;"
  onbeforecopy="return fnBeforeCopy()" oncopy="return fnCopy()"
  onbeforecut="return fnBeforeCut()" oncut="return fnCut()"
  onbeforepaste="return fnBeforePaste()" onpaste="return fnPaste()"
  ondragstart="return false"></div>

<iframe id="idDummy" name="idDummy" src="about:blank" style="display:none"></iframe>
<iframe id="idShutdown" name="idShutdown" src="about:blank" style="display:none"></iframe>

<FORM name="PreviewForm" METHOD="POST" TARGET="gxmlPreview"
 ACTION="/globalsight/wl">
<INPUT TYPE="hidden" NAME="locale" VALUE="">
<INPUT TYPE="hidden" NAME="gxml" VALUE="">
</FORM>

<FORM name="ShutdownForm" METHOD="GET" TARGET="idShutdown"
 ACTION="/globalsight/envoy/common/shutdownPopup.jsp">
<INPUT TYPE="hidden" NAME="a" VALUE="">
</FORM>
</body>
</html>
