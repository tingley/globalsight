<%@ page
    contentType="text/html; charset=UTF-8"
    errorPage="/envoy/common/error.jsp"
    import="java.util.*,com.globalsight.everest.webapp.webnavigation.LinkHelper,
        java.util.ResourceBundle,
        java.text.MessageFormat,
	com.globalsight.everest.foundation.User,
        com.globalsight.util.edit.EditUtil,
        com.globalsight.everest.webapp.pagehandler.PageHandler,
        com.globalsight.everest.webapp.WebAppConstants"
    session="true"
%>
<jsp:useBean id="search" class="com.globalsight.everest.webapp.javabean.NavigationBean" scope="request"/>
<%

ResourceBundle bundle = PageHandler.getBundle(session);

String searchURL = search.getPageURL();
    
Locale uiLocale = (Locale)session.getAttribute(WebAppConstants.UILOCALE);
String str_uiLocale;
if (uiLocale == null)
{
    str_uiLocale = "en_US";
}
else
{
    // must return xx_YY with underscore
    str_uiLocale = uiLocale.toString();
}

String str_userId = "";
boolean b_anonymous = true;
boolean b_isReviewOnlyActivity = false;
Boolean b = (Boolean)session.getAttribute(
    WebAppConstants.IS_LAST_ACTIVITY_REVIEW_ONLY);
if (b!=null)
{
    b_isReviewOnlyActivity = b.booleanValue();
}


User user;
if ((user = PageHandler.getUser(session)) != null)
{
    str_userId = user.getUserId();
    b_anonymous = false;
}

String lb_termbase = bundle.getString("lb_termbase1");
String lb_source = bundle.getString("lb_source1");
String lb_target = bundle.getString("lb_target1");
String lb_query = bundle.getString("lb_query");
String lb_execute = bundle.getString("lb_execute");
String lb_tooltip = bundle.getString("helper_text_terminology_viewer_queries");
String lb_terms_found = bundle.getString("lb_terms_found");
String lb_term_details = bundle.getString("lb_term_details");
String lb_initializing = bundle.getString("lb_initializing");
String lb_info = bundle.getString("lb_info");
String lb_help = bundle.getString("lb_help");
String lb_edit = bundle.getString("lb_edit");
String lb_new = bundle.getString("lb_new");
String lb_print = bundle.getString("lb_print");
String msg_reIndex = bundle.getString("msg_term_save_reIndex");

String helpFileEditor = bundle.getString("help_termbase_editor");
String helpFileViewer;
if (b_anonymous)
{
  helpFileViewer = bundle.getString("help_termbase_browse_anon");
}
else
{
  helpFileViewer = bundle.getString("help_termbase_browse");
}

String str_name = (String)session.getAttribute(
  WebAppConstants.TERMBASE_NAME);
String str_conceptid = (String)session.getAttribute(
  WebAppConstants.TERMBASE_CONCEPTID);
String str_termid = (String)session.getAttribute(
  WebAppConstants.TERMBASE_TERMID);

Object[] args = { str_name };
MessageFormat format = new MessageFormat(
  bundle.getString("lb_termbase_viewer"));
String lb_title = format.format(args);

if (str_conceptid == null)
{
  str_conceptid = "0";
}
if (str_termid == null)
{
  str_termid = "0";
}
%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<html xmlns:ie xmlns:m="http://www.w3.org/1998/Math/MathML">
<!-- This is terminology\viewer\index.jsp -->
<head>
  
<TITLE><%=lb_title%></TITLE>
<OBJECT ID="MathPlayer" CLASSID="clsid:32F66A20-7614-11D4-BD11-00104BD3F987"></OBJECT>
<?IMPORT NAMESPACE="m" IMPLEMENTATION="#MathPlayer" ?>
<STYLE type="text/css" MEDIA="all">
@import url("/globalsight/envoy/terminology/viewer/viewer.css");
@import url("/globalsight/envoy/terminology/viewer/editor.css");
</STYLE>
<script src="/globalsight/includes/library.js"></script>
<script src="/globalsight/includes/xmlextras.js"></script>
<script src="/globalsight/envoy/terminology/viewer/error.js" DEFER></script>
<script src="/globalsight/envoy/terminology/viewer/splitter.js" ></script>
<script src="/globalsight/envoy/terminology/viewer/SelectableFields.js"></script>
<script src="/globalsight/envoy/terminology/management/objects_js.jsp"></script>
<script src="/globalsight/envoy/terminology/viewer/entry.js"></script>
<script src="/globalsight/envoy/terminology/viewer/entryValidation.js" DEFER></script>
<script src="/globalsight/envoy/terminology/viewer/layout.js" DEFER></script>
<script src="/globalsight/envoy/terminology/viewer/editor.js"></script>
<script src="/globalsight/includes/menu/js/poslib.js"></script>
<script src="/globalsight/includes/menu/js/scrollbutton.js"></script>
<script src="/globalsight/includes/menu/js/menu4.js"></script>
<script src="/globalsight/includes/ASCIIMathML-LGPL.js" DEFER></script>
<SCRIPT SRC="/globalsight/envoy/terminology/viewer/viewer_resources_js.jsp"></SCRIPT>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/jquery/jquery-1.6.4.min.js"></SCRIPT>
<script src="/globalsight/envoy/terminology/viewer/viewer.js" ></script>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/Ajax.js"></SCRIPT>
<SCRIPT LANGUAGE="JavaScript" SRC="/globalsight/includes/dojo.js"></SCRIPT>

<script>
////////////localize content////////////////
var query_no_empty='<%=bundle.getString("msg_termbrowse_query_no_empty")%>';
var reindexing_warning='<%=bundle.getString("reindexing_warning")%>';
var msg_entry_modify_sucessfully = '<%=bundle.getString("msg_entry_modify_sucessfully")%>';
var msg_entry_add_sucessfully = '<%=bundle.getString("msg_entry_add_sucessfully")%>';
///////////////////////////////////////////
var g_uiLocale = "<%=str_uiLocale%>";
var g_userId = "<%=str_userId%>";
var g_anonymous = "<%=b_anonymous%>";
var g_canEditMath = false;
var g_isReviewOnlyActivity = "<%=b_isReviewOnlyActivity%>";

var helpFileViewer = "<%=helpFileViewer%>";
var helpFileEditor = "<%=helpFileEditor%>";

var searchItem ="<%=searchURL%>" + "&action=searchTermHitList";
var searchEntryURL ="<%=searchURL%>" + "&action=searchEntry";
var ControllerURL ="<%=searchURL%>";

function showHelpViewer()
{
    var helpWindow = window.open(helpFileViewer, 'helpWindow',
      'resizable=yes,scrollbars=yes,WIDTH=600,HEIGHT=400');
    helpWindow.focus();
}

function showHelpEditor()
{
    var helpWindow = window.open(helpFileEditor, 'helpWindow',
      'resizable=yes,scrollbars=yes,WIDTH=600,HEIGHT=400');
    helpWindow.focus();
}

function doLoad()
{
    splitterLeft.style.height = idBody.clientHeight - 36;
    splitterRight.style.height = idBody.clientHeight;
    splitterRight.style.left = idBody.clientWidth;
    idViewer.style.width = idBody.clientWidth;
    idViewerMenuArea.style.width = splitterLeft.style.left;
    idViewerMenuArea.style.height = idBody.clientHeight - turnPXStringTInt(idViewerHeader.style.height);
    idQuery.style.width = turnPXStringTInt(splitterLeft.style.left) - 15;
  
    idViewerMenu.style.width = idViewerMenuArea.style.width;
    idViewerMenu.style.height = 190;
    idHitListHeader.style.top = idViewerMenu.style.height;

    idHitList.style.top = turnPXStringTInt(idHitListHeader.style.top) + turnPXStringTInt(idHitListHeader.style.height) + 10;
    idHitList.style.height = turnPXStringTInt(idViewerMenuArea.style.height) 
                             - turnPXStringTInt(idViewerMenu.style.height)
                             -30;

    idViewerArea.style.left = turnPXStringTInt(splitterLeft.style.left) + turnPXStringTInt(splitterLeft.style.width);
    idViewerArea.style.width = turnPXStringTInt(splitterRight.style.left) - turnPXStringTInt(splitterLeft.style.left) - turnPXStringTInt(splitterLeft.style.width);
    idViewerArea.style.height = document.body.clientHeight - turnPXStringTInt(idViewerHeader.style.height);
    idViewerEntry.style.height = turnPXStringTInt(idViewerArea.style.height) - turnPXStringTInt(idViewerEntryHeader.style.height);

    idEditor.style.display = 'none';
    idEditor.style.left = turnPXStringTInt(splitterRight.style.left) + turnPXStringTInt(splitterRight.style.width);

    g_canEditMath = (AMisMathMLavailable() == null);
    window.focus();
    
    var idCreateButton = document.getElementById("idCreateButton");
    var idEditButton = document.getElementById("idEditButton");
    var idPrintViewerButton = document.getElementById("idPrintViewerButton");

    idCreateButton.disabled = false;
    idEditButton.disabled = true;
    idPrintViewerButton.disabled = true;

    GetInputModel(); 
    drag("splitterLeft");  
    drag("splitterRight");
    SetDefinition();
    doOnload('<%=b_anonymous%>','<%=str_name%>','<%=str_conceptid%>','<%=str_termid%>');
    
 	// Set the source language with input value, and keep target different with source.
    setSrcAndTrgLanguage("English");
}

function infoStatistic(event) {
    var evt = event ? event : (window.event ? window.event : null);
    evt.cancelBubble = true;
    evt.returnValue = false;

    ShowStatistics();
}

function StrToXML(strXML)
{
    var xmlDoc;
    
    if (window.ActiveXObject){
        xmlDoc = new ActiveXObject('Msxml2.DOMDocument');
    }
    else {
        xmlDoc = document.implementation.createDocument("", "", null);
    }
    
    xmlDoc.loadXML('<?xml version="1.0" encoding="unicode"?>' + strXML);

    return xmlDoc;
}

function doEditorKeydown(event)
{
    var evt = event ? event : (window.event ? window.event : null);
    var key = evt.keyCode;
    
    if (key == 13) { // Return
            execute();
            return false;
    }
}
</script>
</HEAD>

<body id="idBody" style="overflow: hidden" onunload="doOnunload();"
  onload="doLoad();">

  <div id="splitterLeft" style="position: absolute; top: 36; left: 225;
    height: 300; width: 1;border:1px outset;z-index:1000;background-color:white"></div>
      
  <div id="splitterRight" style="position: absolute; top: 0; height: 100%; 
     width: 1; border:1px outset;height: 300; z-index:1001;background-color:white" ></div>

<!-- Viewer Pane -->
 
<DIV id="idViewer" style="position: absolute; top: 0; left: 0; height: 100%;">
 
  <div id="idViewerHeader" style=" position: absolute; top: 0; left: 0; width: 100%; height: 35;
   border-bottom: 1px solid black;">  

    <SPAN id="idCloseWindow" onclick="window.close()" 
     style="float: right; margin-right: 10px; margin-top: 9px;"
     TITLE="<%=bundle.getString("lb_close_this_window") %>"><img src="/globalsight/images/close.gif" align="baseline"></SPAN>

    <SPAN id="idHelpViewer" onclick="showHelpViewer()" class="help"
     style="float: right; margin-right: 10px; margin-top: 7px;"><%=lb_help%></SPAN>
    <SPAN id="idInfo" class="help"
     style="float: right; margin-right: 10px; margin-top: 7px;" onclick="infoStatistic(event);"><%=lb_info%></SPAN>
    <DIV style="margin-left: 7px; margin-top: 7px; margin-right: 7px;">
      <SPAN CLASS="mainHeading"><%=lb_termbase%></SPAN>
      <SPAN id="idTermbase" class="normal">
        <SPAN class="feedback"><%=lb_initializing%></SPAN>
      </SPAN>
    </DIV>
  </div> <!-- idViewerHeader -->

  <div id="idViewerMenuArea" style="position: absolute; top: 36; left: 0;" onkeydown="return doEditorKeydown(event)">

    <div id="idViewerMenu" style="position: absolute;
     top: 0; left: 0;  width: 100%; padding-left: 7px;">
     <table border="0">
        <tr>
            <td>
                <table border=0>
                    
                <tr>
                    <td>
                        <LABEL style="font-size: 14;"><%=lb_source%></LABEL>
                    </td>
                    
                    <td align="left">
                        <SELECT ACCESSKEY="s" id="idSource" size="1" onchange="doChange()"></SELECT>
                    </td>
                </tr>
        
                <tr>
                    <td>
                        <LABEL style="font-size: 14;"><%=lb_target%></LABEL>
                    </td>
                    <td align="left">
                        <SELECT ACCESSKEY="t" id="idTarget" size="1"></SELECT>
                    </td>
                </tr>
            </table>
        </td>
        </tr>
        <tr>
            <td >
                <LABEL  style="font-size: 14;"id="lngQuery"><%=lb_query%></LABEL>
            </td>
        </tr>
        <tr>
            <td>
                <INPUT ACCESSKEY="q" id="idQuery">
            </td>
        </tr>
        <tr>
            <td>
                <LABEL style="font-size: 14;">Search Type:</LABEL>
                <SELECT id="searchType">
                <OPTION VALUE="fuzzy" SELECTED><%=bundle.getString("lb_fuzzy")%>
                <OPTION VALUE="exact" ><%=bundle.getString("lb_exact")%>
                </SELECT>
            </td>
        </tr>
        <tr>
            <td>
                <INPUT type="submit" id="idExecute" VALUE="<%=lb_execute%>" onclick="execute()">
                <INPUT type="hidden" id="idPreferences" class="preferences">
            </td>
        </tr>
    </table>
      
    </div> <!-- idViewerMenu -->

    <div id="idHitListHeader" class="header" style="position: absolute; width: 100%;
      height: 20; padding-left: 2px;background-image: url(/globalsight/images/r_h_bg.jpg)">
      <nobr>
        <span id="idPreviousHits"  title="<%=bundle.getString("lb_browse_previous") %>">
          <a href="#"><img src="/globalsight/images/arrowPre.gif" align="baseline" onclick="viewHitsPre();" border=0></a>
        </span>
        <%=lb_terms_found%>
        <span id="idNextHits"  title="<%=bundle.getString("lb_browse_next") %>">
            <a href="#"><img src="/globalsight/images/arrowNext.gif" align="baseline" border=0 onClick="viewHitsNext()"></a>
        </span>
      </nobr>
    </div>
    
    <div id="idHitList" style="overflow: auto; position: absolute;width: 100%;">

    </div> <!-- idHitList -->

  </div> <!-- idViewerMenuArea -->

  <div id="idViewerArea" style="overflow: hidden; position: absolute; top: 36;">
    <DIV id="idViewerEntryHeader" class="header"
     style="position: absolute; top: 0; left: 0; height: 25; width: 100%;
     background-image: url(/globalsight/images/r_h_bg.jpg)">
      <table>
	      <tr>
	        <td><nobr id="idTermDetails">&nbsp;&nbsp;<%=lb_term_details%></nobr></td>

          <td>&nbsp;</td>
	        <td> <INPUT TYPE=button id="idCreateButton" value="<%=lb_new%>" onclick="StartEditing(true)" ></td>
	        <td> <INPUT TYPE=button id="idEditButton" value="<%=lb_edit%>" onclick="StartEditing(false)" ></td>
	        <td> <INPUT TYPE=button id="idPrintViewerButton" value="<%=lb_print%>" onclick="PrintEntry(false)" ></td>
	        <td>&nbsp;</td>
	        <td>
	          <nobr>
	            <span id="idHistoryBack"  title="<%=bundle.getString("lb_history_back") %>" style="display:none">
	              <a href="#"><img id="arrowPre" src="/globalsight/images/arrowPre.gif" onClick="historyBack()" border=0 ></a>
	            </span>&nbsp;&nbsp;
	            <span id="idHistoryForward" title="<%=bundle.getString("lb_history_forward")%>" style="display:none">
	              <a href="#"><img id="arrowNext" src="/globalsight/images/arrowNext.gif" onClick="historyNext()" border=0 ></a>
	            </span>
	         </nobr>
	       </td>
	     </tr>
      </table>
    </DIV> <!-- idViewerEntryheader -->

    <DIV id="idViewerEntry" style="overflow: auto; position: absolute;top: 25; width: 100%;background-color:f8eec9">
     
    </DIV> <!-- idViewerEntry -->
  </div> <!-- idViewerArea -->
</DIV> <!-- idViewer -->

<!-- Editor Pane -->

<div id="idEditor" style=" position: absolute;top: 0;height: 100%;">

  <div id="idEditorHeader"
   style="overflow: visible; position: absolute;
   top: 0; left: 0; width: 100%; height: 35;
   border-bottom: 1px solid black;">
    <SPAN id="idCloseEditor" onclick="StopEditing(true)" 
     style="float: right; margin-right: 7px; margin-top: 7px;"
     TITLE="Close Editor"><img src="/globalsight/images/close.gif" align="baseline"></SPAN>

    <SPAN id="idHelpEditor" onclick="showHelpEditor()" class="help"
     style="float: right; margin-right: 10px; margin-top: 7px;"><%=lb_help%></SPAN>
    <div style="margin-left: 7px; margin-top: 7px; margin-right: 7px;">
      <span class="mainHeading">Entry Editor: </span>
      <span class="feedback"></span>
    </div>
  </div> <!-- idEditorHeader -->

  <div id="idEditorMenu" UNSELECTABLE="on"
   style="position: absolute; top: 36; width: 145;">
    <div class="menuHeader">Editor Menu</div>

    <span id="idValidateEntry" class="menuItem" onclick="ValidateEntry()"
     UNSELECTABLE="on">Validate Entry</span><br>
    <span id="idApproveEntry" class="menuItem" onclick="ApproveEntry()"
     UNSELECTABLE="on">Approve Entry</span><br>
    <span id="idSaveEntry" class="menuItem" onclick="SaveEntry('<%=msg_reIndex%>');"
     UNSELECTABLE="on">Save Entry</span><br>

    <hr width="90%">

    <span id="idDeleteEntry" class="menuItem" onclick="DeleteEntry()"
     UNSELECTABLE="on">Delete Entry</span>

    <hr width="90%">

    <span id="idAddLanguage" class="menuItem" onclick="AddLanguage()"
     UNSELECTABLE="on">Add Language...</span><br>
    <span id="idRemoveLanguage" class="menuItem" onclick="RemoveLanguage()"
     UNSELECTABLE="on">Remove Language</span>

    <hr width="90%">

    <span id="idAddTerm" class="menuItem" onclick="AddTerm()"
     UNSELECTABLE="on">Add Term...</span><br>
    <span id="idRemoveTerm" class="menuItem" onclick="RemoveTerm()"
     UNSELECTABLE="on">Remove Term</span>

    <hr width="90%">

    <div class="menuLabel" UNSELECTABLE="on">Add Field:</div>
      <span id="idAddFieldTo" class="menuSubItem"
       onclick="AddFieldToCurrent()"
       UNSELECTABLE="on">to current field...</span><br>
      <span id="idAddFieldAfter" class="menuSubItem"
       onclick="AddFieldAfterCurrent()"
       UNSELECTABLE="on">after current field...</span><br>
    <span id="idEditField" class="menuItem" onclick="EditField()"
     UNSELECTABLE="on">Edit Field</span><br>
    <span id="idRemoveField" class="menuItem" onclick="RemoveField(event)"
     UNSELECTABLE="on">Remove Field</span>

<%-- Provisions for multiple Input Models
    <hr width="90%">

    <div class="menuLabel" UNSELECTABLE="on">Use Input Model:</div>

      <select class="menuLabel" style="width: 95%">
        <option>Default<option>Test<option>None (admin only)
      </select>
    
      <div id="idViewIM" class="menuSubItem" onclick=""
       UNSELECTABLE="on">View</div>
      <div id="idEditIM" class="menuSubItem" onclick=""
       UNSELECTABLE="on">Edit...</div>
--%>

    <hr width="90%">

  </div> <!-- idEditorMenu -->

  <div id="idEditorArea" onkeydown="doKeydown(event,this);"
    style="overflow: auto; position: absolute; top: 36; left: 0;">
    
    <div id="idEditorEntryHeader" class="header"
      style="position: absolute; top: 0; left: 0; height: 25; width: 100%;
      padding-top: 3px;background-image: url(/globalsight/images/r_h_bg.jpg)">
          &nbsp;&nbsp;Entry Details
    </div>

    <div id="idEditorEntry"
     style="overflow: auto; position: absolute; top: 25;left: 0;
        width: 100%;background-color: f4f8e7;font-size: 14;"></div>

  </div> <!-- idEditorArea -->
</div> <!-- idEditor -->

<div id="idSaving" style="position: absolute; top: 200; left: 400;
  display: none; background-color: lightgrey; color: black; text-align: center;
  border: 2px solid black; padding: 10 100; font-size: 14pt; z-index: 101;">
  Saving... <img src="/globalsight/envoy/edit/online2/bullet2.gif"> 
</div>

<div id="idReIndexing" style="position: absolute; top: 200; left: 400;
  display: none; background-color: lightgrey; color: black; text-align: center;
  border: 2px solid black; padding: 10 100; font-size: 14pt; z-index: 101;">
  Re-indexing... <img src="/globalsight/envoy/edit/online2/bullet2.gif"> 
</div>

<div id="idViewerHistory" style="display:none"></div>

<script>
    var idBody = document.getElementById("idBody");
    var splitterRight = document.getElementById("splitterRight");
    var splitterLeft = document.getElementById("splitterLeft");
    var idViewerHeader = document.getElementById("idViewerHeader");
    var idViewer = document.getElementById("idViewer");
    var idViewer = document.getElementById("idViewer");
    var idViewerMenuArea = document.getElementById("idViewerMenuArea");
    var idQuery = document.getElementById("idQuery");
    var idViewerMenu = document.getElementById("idViewerMenu");
    var idHitListHeader = document.getElementById("idHitListHeader");
    var idHitList = document.getElementById("idHitList");
    var idViewerArea = document.getElementById("idViewerArea");
    var idViewerEntry = document.getElementById("idViewerEntry");
    var idViewerEntryHeader = document.getElementById("idViewerEntryHeader");
    var idEditor = document.getElementById("idEditor");
    var idEditorHeader = document.getElementById("idEditorHeader");
    var idEditorArea = document.getElementById("idEditorArea");
    var idEditorMenu = document.getElementById("idEditorMenu");
    var idEditorEntry = document.getElementById("idEditorEntry");
    var idEditorEntryHeader = document.getElementById("idEditorEntryHeader");
    var idEditorArea = document.getElementById("idEditorArea");
</script>
</body>
</html>
