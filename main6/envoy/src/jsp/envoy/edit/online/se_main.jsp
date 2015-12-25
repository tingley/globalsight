<%@ page
    contentType="text/html; charset=UTF-8"
    errorPage="error.jsp"
    import="java.util.*,com.globalsight.util.edit.EditUtil,
            com.globalsight.util.edit.GxmlUtil,
            com.globalsight.everest.edit.online.SegmentView,
            com.globalsight.everest.webapp.WebAppConstants,
            com.globalsight.everest.webapp.javabean.NavigationBean,
            com.globalsight.everest.webapp.pagehandler.PageHandler,
            com.globalsight.everest.webapp.pagehandler.edit.online.EditorConstants,
            com.globalsight.everest.webapp.pagehandler.edit.online.EditorState,
            com.globalsight.everest.edit.SynchronizationStatus,
            com.globalsight.everest.servlet.util.SessionManager,
            com.globalsight.util.resourcebundle.ResourceBundleConstants,
            com.globalsight.util.resourcebundle.SystemResourceBundle,
            com.globalsight.everest.foundation.User,
            com.globalsight.ling.common.Text,
            com.globalsight.everest.comment.Issue,
            com.globalsight.everest.comment.IssueHistory,
            com.globalsight.everest.edit.online.CommentView,
            com.globalsight.everest.edit.online.CommentThreadView,
            java.util.Locale,
            java.util.ResourceBundle,
            com.globalsight.util.edit.SegmentUtil2,
            com.globalsight.ling.docproc.IFormatNames"
    session="true"
%>
<jsp:useBean id="topMenu" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean"/>
 <jsp:useBean id="tmSearch" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean"/>
<jsp:useBean id="bottomMenu" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean"/>
<jsp:useBean id="segmentEditor" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean"/>
<jsp:useBean id="bidiEditor" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean"/>
<jsp:useBean id="imageEditor" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean"/>
<jsp:useBean id="textEditor" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean"/>
<jsp:useBean id="details" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean"/>
<jsp:useBean id="options" scope="request"
 class="com.globalsight.everest.webapp.javabean.NavigationBean"/>
<%
ResourceBundle bundle = PageHandler.getBundle(session);

SessionManager sessionMgr = (SessionManager)session.getAttribute(
  WebAppConstants.SESSION_MANAGER);
EditorState state =
  (EditorState)sessionMgr.getAttribute(WebAppConstants.EDITORSTATE);

String str_userId =
  ((User)sessionMgr.getAttribute(WebAppConstants.USER)).getUserId();

SegmentView view =
  (SegmentView)sessionMgr.getAttribute(WebAppConstants.SEGMENTVIEW);
String pagePath = view.getPagePath().toLowerCase();
boolean isOffice = pagePath.endsWith(".docx") || pagePath.endsWith(".pptx") || pagePath.endsWith(".xlsx");

String url_topMenu       = topMenu.getPageURL();
String url_bottomMenu    = bottomMenu.getPageURL();
String url_segmentEditor = segmentEditor.getPageURL();
String url_bidiEditor    = bidiEditor.getPageURL();
String url_imageEditor   = imageEditor.getPageURL();
String url_textEditor    = textEditor.getPageURL();
String url_details       = details.getPageURL();
String url_options       = options.getPageURL();
String url_tmSearch      = tmSearch.getPageURL();

String lb_segmentChanged = bundle.getString("jsmsg_save_segment");
String lb_saveTheChanges = bundle.getString("jsmsg_save_the_changes");
String lb_tm_search      = bundle.getString("lb_tm_search");

String lb_title;
String url_theEditor;
String str_PTagBoxScript;
boolean b_colorPtags = false;

switch (state.getEditorType())
{
case EditorConstants.SE_IMAGEEDITOR:
    lb_title = bundle.getString("lb_image_editor");
    url_theEditor = url_imageEditor;
    str_PTagBoxScript = "menu.HidePTagBox();";
    break;
case EditorConstants.SE_TEXTEDITOR:
    lb_title = bundle.getString("lb_segment_editor");
    url_theEditor = url_textEditor;
    str_PTagBoxScript = "menu.HidePTagBox();";
    break;
case EditorConstants.SE_BIDIEDITOR:
//    lb_title = bundle.getString("lb_segment_editor_bidi");
//    url_theEditor = url_bidiEditor;
//    str_PTagBoxScript = "menu.ShowPTagBox();";
//    b_colorPtags = true;
//    break;
case EditorConstants.SE_SEGMENTEDITOR:
default:
    lb_title = bundle.getString("lb_segment_editor");
    url_theEditor = url_segmentEditor;
    str_PTagBoxScript = "menu.ShowPTagBox();";
    b_colorPtags = true;
    break;
}

String str_sourceSegment = GxmlUtil.getInnerXml(view.getSourceSegment());
String str_targetSegment = GxmlUtil.getInnerXml(view.getTargetSegment());
String str_dataType = view.getDataType();
String str_itemType = view.getItemType();
String str_ptags = state.getPTagFormat();
String sourceHtml = view.getSourceHtmlString(str_ptags);
String targetHtml = view.getTargetHtmlString(str_ptags, b_colorPtags);
String str_srcLocale = state.getSourceLocale().toString();
String str_trgLocale = state.getTargetLocale().toString();
String str_preserveWS = SegmentProtectionManager.isPreserveWhiteSpace(view.getSourceSegment()) ? "true" : "false";
String needUpdatePopupEditor = state.getNeedUpdatePopUpEditor();

// for internal text display
if (str_sourceSegment != null && str_sourceSegment.contains("internal=\"yes\"")
        && str_dataType != null && (str_dataType.equals(IFormatNames.FORMAT_JAVAPROP)
        || str_dataType.equals(IFormatNames.FORMAT_JAVAPROP_MSG)
        || str_dataType.equals(IFormatNames.FORMAT_PLAINTEXT)))
{
    b_colorPtags = true;
    url_theEditor = url_segmentEditor;
}

boolean source_rtl = EditUtil.isRTLLocale(state.getSourceLocale());
boolean b_imageUploaded = view.getImageMapExists();
boolean b_autosave = state.getOptions().getAutoSave();
boolean b_hilitePtags = state.getOptions().getHilitePtags();
int i_dbSegmentLen = 0;
int i_maxSegmentLen = 0; // no limit on CLOB

String str_uiLocale;
Locale uiLocale = (Locale)session.getAttribute(WebAppConstants.UILOCALE);

if (uiLocale == null)
{
    str_uiLocale = "en_US";
}
else
{
    // must return en_US with underscore
    str_uiLocale = uiLocale.toString();
}

long l_tuId  = state.getTuId();
long l_tuvId = state.getTuvId();
long l_subId = state.getSubId();

SynchronizationStatus oldStatus = state.getOldSynchronizationStatus();
SynchronizationStatus newStatus = state.getNewSynchronizationStatus();
String syncMessage = null;
boolean syncClose = false;

if (newStatus != null)
{
    String status = newStatus.getStatus();
    if (status.equals(SynchronizationStatus.UPLOAD_FINISHED))
    {
      syncMessage = bundle.getString("jsmsg_editor_save_offline_uploaded");
      // re-open will clear status
    }
    else if (status.equals(SynchronizationStatus.GXMLUPDATE_STARTED) ||
             status.equals(SynchronizationStatus.GXMLUPDATE_FINISHED))
    {
      syncMessage = bundle.getString("jsmsg_editor_srcpage_is_being_edited");
      syncClose = true;
      // re-open will clear status
    }
    else if (status.equals(oldStatus.getStatus()))
    {
      // Really: previous upload has finished and new upload has started.
      syncMessage = bundle.getString("jsmsg_editor_save_offline_uploaded");
      // reload will clear status
    }
    else
    {
      syncMessage = bundle.getString("jsmsg_editor_save_offline_uploading");
      // Show message once.
      state.setOldSynchronizationStatus(newStatus);
      state.setNewSynchronizationStatus(null);
    } 
}

%>

<%@page import="com.globalsight.everest.edit.SegmentProtectionManager"%>
<HTML>
<!-- This is envoy\edit\online\se_main.jsp-->
<HEAD>
<TITLE><%=lb_title%></TITLE>
<script src="/globalsight/envoy/edit/online/richedit.js"></script>
<script src="/globalsight/includes/ajaxJquery/online.js"></script>
<script type="text/javascript" src="/globalsight/jquery/jquery-1.9.1.min.js"></script>
<SCRIPT type="text/javascript">
window.focus();

var EditorType = 'segment';

var source_segment = "<%=EditUtil.toJavascript(str_sourceSegment)%>";
var target_segment = "<%=EditUtil.toJavascript(str_targetSegment)%>";
var original_target_segment = target_segment;

var datatype = "<%=str_dataType%>";
var itemtype = "<%=str_itemType%>";
var sourcelocale = "<%=str_srcLocale%>";
var targetlocale = "<%=str_trgLocale%>";
var uilocale = "<%=str_uiLocale%>";
var userId = "<%=str_userId%>";
var tuId = "<%=l_tuId%>";
var tuvId = "<%=l_tuvId%>";
var subId = "<%=l_subId%>";
var verbose = "<%=str_ptags%>";
var db_segment_len  = parseInt("<%=i_dbSegmentLen%>");
var max_segment_len = parseInt("<%=i_maxSegmentLen%>");
var db_encoding = "UTF8";
var gsa_encoding = "UTF8";
var imageUploaded = "<%=b_imageUploaded%>";
var colorPtags = "<%=b_colorPtags%>";
var autosave = "<%=b_autosave%>";
var g_hilitePtags = eval("<%=b_hilitePtags%>");

var g_syncMessage = "<%=syncMessage != null ? EditUtil.toJavascript(syncMessage) : ""%>";
var g_syncClose = eval("<%=syncClose%>");

var fr_topmenu = null;
var fr_source = null;
var fr_target = null;
var fr_editor = null;
var w_details = null;
var w_tmsearch=null;
var match_details = null;
var w_options = null;
var w_concordance = null;
var g_initialized = false;
var g_refreshing = false;
var b_initlized = false;
//Used to ensure only refresh pop-up editor ONCE.
var hasRefreshPopupEditorFlag = false;

function setInitlized(status) {
	b_initlized = status;
}

function init()
{
    fr_topmenu = menu;
    fr_editor = editor;
    fr_source = editor.source;
    fr_target = editor.target;
	fr_tm = editor.tm;
    
    //initAllRichEdits();
    initSegments();
    syncMainEditor();
    if(menu.showPTagBox)
	{
		eval("<%=str_PTagBoxScript%>");
		fr_topmenu.PostLoad();
	}
    fr_target.PostLoad();
	if(fr_tm)
	{   if(fr_tm.PostLoad)
		{
		  fr_tm.PostLoad();
		}
	}
    
    HilitePtags(g_hilitePtags);

    if (g_syncMessage)
    {
        alert(g_syncMessage);
    }

    if (g_syncClose)
    {
        doClose();
    }

    g_initialized = true;

    //update parent window
    var needUpdatePopupEditor = "<%=state.getNeedUpdatePopUpEditor()%>";
    var parentRefreshUrl = null;
    if (needUpdatePopupEditor == "next" && !hasRefreshPopupEditorFlag)
    {
    	hasRefreshPopupEditorFlag = true;
    	if(typeof(window.opener.postReviewEditor) == "undefined")
    	{
    		parentRefreshUrl = "/globalsight/ControlServlet?linkName=refresh&pageName=ED2&refresh=11&where=se";
        	window.opener.parent.parent.parent.Refresh(parentRefreshUrl);
    	}
    	else
    	{
        	window.opener.refresh(11);
    	}
    }
    else if (needUpdatePopupEditor == "previous" && !hasRefreshPopupEditorFlag)
    {
    	hasRefreshPopupEditorFlag = true;
    	if(typeof(window.opener.postReviewEditor) == "undefined")
    	{
	    	parentRefreshUrl = "/globalsight/ControlServlet?linkName=refresh&pageName=ED2&refresh=-11&where=se";
	    	window.opener.parent.parent.parent.Refresh(parentRefreshUrl);
    	}
    	else
    	{
	    	window.opener.refresh(-11);
    	}
    }
}

function exit()
{
	try { w_tmsearch.close(); } catch (ignore) {}
    try { w_details.close(); } catch (ignore) {}
    try { w_options.close(); } catch (ignore) {}
    try { w_concordance.close(); } catch (ignore) {}
    try { match_details.close(); } catch (ignore) {}
}

function IsImageEditor()
{
  return fr_editor.IsImageEditor();
}

function syncMainEditor()
{
    window.opener.HighlightSegment(tuId, tuvId, subId);
}

function initSegments()
{
    initTarget(target_segment, false);
    initSource();
}

function initTarget(trg_segment, changed)
{
    var edit_segment = initTarget2(trg_segment);
   
    fr_editor.SetTargetSegment(edit_segment, changed, IsWhitePreserving());
    fr_editor.SetVerbosePTags(verbose == "<%=EditorConstants.PTAGS_VERBOSE%>");
    //alert(edit_segment);
}

function initSource()
{
	var sourceHtml = "<%=EditUtil.toJavascript(sourceHtml)%>";
	showSource(sourceHtml);

    if(<%=source_rtl%>)
    {
        if(editor.source.idSourceCell)
        {
        	editor.source.idSourceCell.dir = 
            	"<%=Text.containsBidiChar(str_sourceSegment) ? "RTL" : "LTR"%>";
        }	
    }
    else
    {
        if(editor.source.idSourceCell)
        {
        	editor.source.idSourceCell.dir = "LTR";
        }
    }
}

function updateTarget(text, changed)
{
    initTarget(text, changed);
    initSource();
}

// This is a transactional type of pattern. Caller must call
// EndGetPTagStrings() when done requesting PTag strings,
// best in a try {} finally {} handler.
function GetPTagString(text, verbose)
{
    return getHtmlSegment(text, false);;
}

// Must reset applet state to source string and its ptags.
function EndGetPTagStrings()
{
	//do nothing. the applet has been removed.
}

function setVerbose()
{
    if (!checkError())
    {
        var text = getTargetDiplomatString();
        var b_changed = fr_target.IsChanged();
        verbose = "<%=EditorConstants.PTAGS_VERBOSE%>";
        updateTarget(text, b_changed);
        return true;
    }
    else
    {
        return false;
    }
}

function setCompact()
{
    if (!checkError())
    {
        var text = getTargetDiplomatString();
        var b_changed = fr_target.IsChanged();
        verbose = "<%=EditorConstants.PTAGS_COMPACT%>";
        updateTarget(text, b_changed);
        return true;
    }
    else
    {
        return false;
    }
}

function HilitePtags(bright)
{
  if(menu.showPTagBox)
  {
  	fr_source.HilitePtags(bright);
  }
  fr_target.HilitePtags(bright);
}

var internalTagMsg = "";

function checkError()
{
	internalTagMsg = "";
    var message;
    if ((message = doErrorCheck()) != null)
    {
        alert(message);
        fr_target.SetFocus();
        return 1;
    }
    if (internalTagMsg != null && internalTagMsg != "")
    {
    	var rrr = confirm("<%=bundle.getString("msg_internal_moved_continue")%>" + "\n\r\t" + internalTagMsg);
    	if (rrr == true)
    		return 0;
    	else
    	{
    		fr_target.SetFocus();
    		return 1;
    	}
    }
    
    if (newTargetReturn != null && newTargetReturn != "")
	{
		SetSegment(newTargetReturn);
	}
    
    return 0;
}



function getTargetDiplomatString()
{
    var text = fr_editor.GetTargetSegment();

    if (text != "")
    {
        return getTargetDiplomat(text);
    }
    else
    {
        return "";
    }
}

function showSource(text)
{
    fr_editor.ShowSourceSegment(text, IsWhitePreserving());
}

function dotmSearch()
{
   var url = "<%=url_tmSearch%>&action=tmSearchPage";
   w_tmsearch = window.open(url, "<%=lb_tm_search%>",
   'location=no,menubar=no,resizable=yes,scrollbars=yes,WIDTH=1600,HEIGHT=400');
}

function doDetails()
{
    w_details = window.open("<%=url_details%>", "Details",
      ("resizable,scrollbars=yes,width=400,height=400"));
}

function doOptions()
{
    w_options = window.open("<%=url_options%>", "Options",
      ("resizable=no,scrollbars=no,status=no,help=no,width=350,height=250"));
}

function IsWhitePreserving()
{
  if (datatype == "plaintext" || <%=str_preserveWS %>)
  {
    return true;
  }

  return false;
}

// Formatting tags are B/I/U
function HasFormattingTags()
{
  if (<%=isOffice%> || datatype == "mif")
  {
	  return true;
  }
	
  if (HasPTags() && itemtype == "text")
  {
    return true;
  }

  return false;
}

function HasOfficeTags()
{
	return <%=isOffice%> || datatype == "mif";
}

function openPtags()
{
	if (datatype == "xlf" || datatype == "xlf2.0" || datatype == "mif" 
	    || datatype == "fm" || datatype == "po" || "office-xml" == datatype)
	{
		return true;
	}
	else
	{
		return false;
	}
}

// PTags are all segment-internal tags, including formatting tags
function HasPTags()
{
  if ("office-xml" == datatype)
  {
	  return false;
  }
	
  if (datatype.indexOf("html") >= 0 ||
      datatype == "cfm" ||
      datatype == "asp" ||
      datatype == "jsp" ||
      datatype == "xml" ||
      datatype == "jhtml" ||
      datatype == "javaprop-html" ||
      datatype == "sgml" ||
      datatype == "xptag" ||
      datatype == "rtf" ||
      datatype.indexOf("-xml") >= 0 ||
      datatype.indexOf("troff") >= 0)
  {
    return true;
  }

  return false;
}

function HasImageUploaded()
{
  return imageUploaded;
}

function GetDetails()
{
    return getPtagToNativeMappingTable();
}

function DoChangePTags(o_select)
{
    if (o_select.selectedIndex == 1)
    {
        if (!setVerbose())
        {
            o_select.selectedIndex = 0;
        }
    }
    else
    {
        if (!setCompact())
        {
            o_select.selectedIndex = 1;
        }
    }
}

function doRevert()
{
    initTarget(target_segment, false);
    initSource();
}

function SetSegment(segment)
{
    initTarget(segment, true);
    initSource();
}

function InsertTerm(term)
{
    fr_editor.InsertTerm(term);
}

function CanClose()
{
    return fr_target.CanClose();
}

function RaiseEditor()
{
    window.focus();
    alert("<%=EditUtil.toJavascript(lb_saveTheChanges)%>")
}

function shouldSave()
{
    if (autosave == "true") return true;
    return confirm("<%=EditUtil.toJavascript(lb_segmentChanged)%>");
}

function doClose()
{
    if (fr_target.IsChanged())
    {
        if (shouldSave())
        {
            if (!checkError())
            {
                window.opener.SaveSegment("<%=l_tuId%>", "<%=l_tuvId%>",
                  "<%=l_subId%>", getTargetDiplomatString(), verbose);
                window.close();
            }
            
        }
        else
        {
            window.opener.Refresh();
            window.close();
        }
    }
    else
    {
        window.opener.editAll(true);
        window.close();
    }
    
    if(window.opener.parent.parent.parent.review) {
        var iframeSrc = '' + window.opener.parent.parent.parent.review.location;
        window.opener.parent.parent.parent.review.location = iframeSrc;
    }
}

function showHourglass()
{
  showHourglassInFrames(self.frames);
}

function showHourglassInFrames(frames)
{
  for (var i = 0; i < frames.length; i++)
  {
    var win = frames[i];
    if (win.frames != null && win.frames.length > 0)
    {
      showHourglassInFrames(win.frames);
    }
    showHourglassInDocument(win.document);
  }
}

function showHourglassInDocument(doc)
{
  var elements = doc.all;
  if(!elements) elements = doc.getElementsByTagName("*");

  for (var j = 0; j < elements.length; j++)
  {
    if(elements[j].style) elements[j].style.cursor = 'wait';
  }
}

function saveFromFirefoxRichedit()
{
    if (!g_initialized) 
	    return;
    
    if (g_refreshing) 
	    return;
    
    g_refreshing = true;
    var text = fr_editor.GetTargetSegment();
	$.ajax({
		type : "POST",
		url : 'OnlineService?action=doErrorCheck2',
		cache : false,
		data : {
			text : text
		},
		dataType : 'text',
		success : function(data) {
			var ob = eval("(" + data+ ")");
			internalTagMsg = ob.internalTagMsg;
			newTargetReturn = ob.newTarget;
			errorMsgReturn = ob.msg;
			
			if (ob.msg != null)
		    {
		        alert(ob.msg);
		        fr_target.SetFocus();
		        
		        g_refreshing = false;
		        return;
		    }
		    if (ob.internalTagMsg != null && ob.internalTagMsg != "")
		    {
		    	var rrr = confirm("<%=bundle.getString("msg_internal_moved_continue")%>" + "\n\r\t" + ob.internalTagMsg);
		    	if (rrr == false)
		    	{
		    		fr_target.SetFocus();
		    		
		    		g_refreshing = false;
		    		return;
		    	}
		    }
		    
		    var o_form = menu.document.Save;

		    if (ob.newTarget != null && ob.newTarget != "")
			{
				o_form.save.value    = ob.newTarget;
			}
	        o_form.refresh.value = 0;
	        o_form.releverage.value = "false";
	        o_form.tuId.value    = "<%=l_tuId%>";
	        o_form.tuvId.value   = "<%=l_tuvId%>";
	        o_form.subId.value   = "<%=l_subId%>";
	        o_form.ptags.value   = verbose;
	        o_form.isClosedComment.value = fr_source.getIsClosedComment();
	        o_form.submit();
		        
		    g_refreshing = false;
		},
		error : function(request, error, status) {
		    g_refreshing = false;
		    alert(error);
		}
	});
}

function doRefresh(direction, save)
{
    if (!g_initialized) return;
    if (g_refreshing) return;
    g_refreshing = true;

    var b_save = false;
    var str_segment;
    //if target is changed or "Save" button is clicked, need "save" target.
    //if "Close Comment" checkbox is changed, close all comments in "refresh" operation.
    if (fr_target.IsChanged() || (!fr_target.IsChanged() && direction==0) )        
    {
        if (save || shouldSave())
        {
            if (!checkError())
            {
                b_save = true;
                str_segment = getTargetDiplomatString();
            }
            else
            {
                g_refreshing = false;
                return;
            }
        }
    }

    showHourglass();

    if (b_save)
    {
        var o_form = menu.document.Save;

        o_form.save.value    = str_segment;
        o_form.refresh.value = direction;
        o_form.releverage.value = "false";
        o_form.tuId.value    = "<%=l_tuId%>";
        o_form.tuvId.value   = "<%=l_tuvId%>";
        o_form.subId.value   = "<%=l_subId%>";
        o_form.ptags.value   = verbose;
        o_form.isClosedComment.value = fr_source.getIsClosedComment();
        o_form.submit();
    }
    else
    {
        var o_form = menu.document.Refresh;
        o_form.refresh.value = direction;
        o_form.releverage.value = "false";
        o_form.tuId.value    = "<%=l_tuId%>";
        o_form.tuvId.value   = "<%=l_tuvId%>";
        o_form.subId.value   = "<%=l_subId%>";
        o_form.ptags.value   = verbose;
        o_form.isClosedComment.value = fr_source.getIsClosedComment();

        o_form.submit();
    }
}

</SCRIPT>
</HEAD>
<script>
function finishFrame() {
	init();
}
</script>

<FRAMESET ROWS="40,99%,40" FRAMEBORDER="no" BORDER="0" FRAMESCAPING="no"
          onload="init()" onbeforeunload="exit()">
  <FRAME NAME="menu" SCROLLING="no" MARGINHEIGHT="0" MARGINWIDTH="0"
         NORESIZE SRC="<%=url_topMenu%>" >
  <FRAME NAME="editor" SCROLLING="no" MARGINHEIGHT="0" MARGINWIDTH="0"
         NORESIZE SRC="<%=url_theEditor%>" >
  <FRAME NAME="bottomMenu" SCROLLING="no" MARGINHEIGHT="0" MARGINWIDTH="0"
         NORESIZE SRC="<%=url_bottomMenu%>" >
</FRAMESET>
</HTML>
